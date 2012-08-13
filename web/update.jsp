/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
<%--
    Document   : Script for maintaining version wise DB Changes and also updating them to the DB.
    Created on : Apr 2, 2012, 1:06:13 PM
    Author     : Abhay
--%>

<%@page import="com.krawler.esp.utils.ConfigReader"%>
<%@page import="java.sql.SQLInvalidAuthorizationSpecException"%>
<%@page import="com.mysql.jdbc.exceptions.MySQLInvalidAuthorizationSpecException"%>
<%@page import="com.krawler.common.service.ServiceException"%>
<%@page import="java.sql.SQLException"%>
<%@page import="com.krawler.database.DbPool.Connection"%>
<%@page import="com.krawler.database.DbPool"%>
<%@page import="com.krawler.database.DbUtil"%>
<%@page import="com.krawler.database.DbResults"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%
    Connection conn = null;

    try {
        conn = DbPool.getConnection();

        double version_no = 2.16;   // change to the current version no.
        String dbname = ConfigReader.getinstance().get("dbname");
        double db_version_no = 2.10;

        DbResults rs = DbUtil.executeQuery(conn, " SELECT version_no FROM dbversion WHERE dbname = ? ", dbname);
        if (rs.next()) {
            db_version_no = Double.parseDouble(rs.getObject("version_no").toString());
        }

        if (db_version_no < version_no) {

            if (db_version_no < 2.11) {

                int numrows = DbUtil.executeUpdate(conn, "CREATE TABLE dbversion ("
                        + "`dbname` varchar(256) NOT NULL,"
                        + "`version_no` double(40,3) NOT NULL DEFAULT 1.0,"
                        + "`timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;");

                numrows = DbUtil.executeUpdate(conn, "INSERT INTO dbversion VALUES(?,?,now())", new Object[]{dbname, version_no});


                numrows = DbUtil.executeUpdate(conn, "ALTER TABLE `customreports` "
                        + "ADD COLUMN `paging` BIT  NOT NULL DEFAULT 0 AFTER `createdon`, "
                        + "ADD COLUMN `milestone` BIT  NOT NULL DEFAULT 0 AFTER `paging`");


                numrows = DbUtil.executeUpdate(conn, "CREATE TABLE `selectedreport` ("
                        + "  `userid` VARCHAR(36)  CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,"
                        + "  `reportid` VARCHAR(36)  CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,"
                        + "  CONSTRAINT `selectedreport_fk1` FOREIGN KEY `selectedreport_fk1` (`userid`) REFERENCES `users` (`userid`) ON DELETE CASCADE ON UPDATE CASCADE,"
                        + "  CONSTRAINT `selectedreport_fk2` FOREIGN KEY `selectedreport_fk2` (`reportid`) REFERENCES `customreports` (`reportid`) ON DELETE CASCADE ON UPDATE CASCADE"
                        + ") ENGINE = InnoDB CHARACTER SET utf8 COLLATE utf8_general_ci");

                numrows = DbUtil.executeUpdate(conn, "ALTER TABLE `widgetmanagement` ADD COLUMN `customwidget` BIT(1)  NOT NULL DEFAULT 0 AFTER `helpflag`");

                numrows = DbUtil.executeUpdate(conn, "ALTER TABLE `company` ADD COLUMN `milestonewidget` BIT(1)  NOT NULL DEFAULT 0");

                db_version_no += 0.01;

                numrows = DbUtil.executeUpdate(conn, "UPDATE dbversion SET version_no = ?, timestamp=now() WHERE dbname = ? ", new Object[]{db_version_no, dbname});

            }
            if (db_version_no < 2.12) {

                // There are no db changes for this release.
                db_version_no += 0.01;

                DbUtil.executeUpdate(conn, "UPDATE dbversion SET version_no = ?, timestamp=now() WHERE dbname = ? ", new Object[]{db_version_no, dbname});

            }
            if (db_version_no < 2.13) {

                // DB Changes for localization

                DbUtil.executeUpdate(conn, "DROP TABLE IF EXISTS `language`");

                DbUtil.executeUpdate(conn, "CREATE TABLE `language` ("
                         + "`id` int(11) NOT NULL,"
                         + "`langcode` varchar(10) default NULL,"
                         + "`countrycode` varchar(10) default NULL,"
                         + "`langname` varchar(50) default NULL,"
                         + "PRIMARY KEY  (`id`)"
                         + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;");

                DbUtil.executeUpdate(conn, "ALTER TABLE company ADD COLUMN language INT AFTER `toexpireon`, ADD CONSTRAINT FOREIGN KEY (language) REFERENCES language(id);");

                DbUtil.executeUpdate(conn, "insert into language (id,langcode,countrycode,langname)values(1,'en','US','English (United States)');");
                DbUtil.executeUpdate(conn, "insert into language (id,langcode,countrycode,langname)values(2,'hi','IN','Hindi (India)');");

                DbUtil.executeUpdate(conn, "ALTER TABLE reportscolumn ADD COLUMN `headerkey` varchar(50);");
                DbUtil.executeUpdate(conn, "UPDATE reportscolumn SET headerkey = CONCAT(\"pm.cr.\", REPLACE(columnid, \"_\", \".\"));");

                DbUtil.executeUpdate(conn, "ALTER TABLE `actions` ADD COLUMN `textkey` VARCHAR(256)  DEFAULT NULL AFTER `actiontext`;");

                DbUtil.executeUpdate(conn, "update actions set textkey = replace(actionname, \" \", \".\");");
                DbUtil.executeUpdate(conn, "update actions set textkey = replace(textkey, \")\", \"\");");
                DbUtil.executeUpdate(conn, "update actions set textkey = replace(textkey, \"(\", \"\");");
                DbUtil.executeUpdate(conn, "update actions set textkey = replace(textkey, \"-\", \"\");");
                DbUtil.executeUpdate(conn, "update actions set textkey = replace(textkey, \"/\", \"\");");
                DbUtil.executeUpdate(conn, "update actions set textkey = 'Users.added.to.projects' where actionid = 316;");

                DbUtil.executeUpdate(conn, "ALTER TABLE `helpcontent` ADD COLUMN `titletextkey` VARCHAR(255)  DEFAULT NULL AFTER `title`,"
                                                + "ADD COLUMN `descriptiontextkey` VARCHAR(1255)  DEFAULT NULL AFTER `descp`;");
                DbUtil.executeUpdate(conn, "update helpcontent set titletextkey = REPLACE(title, ' ', '.');");
                DbUtil.executeUpdate(conn, "update helpcontent set titletextkey = replace(titletextkey, \")\", \"\");");
                DbUtil.executeUpdate(conn, "update helpcontent set titletextkey = replace(titletextkey, \"(\", \"\");");
                DbUtil.executeUpdate(conn, "update helpcontent set titletextkey = replace(titletextkey, \"-\", \"\");");
                DbUtil.executeUpdate(conn, "update helpcontent set titletextkey = replace(titletextkey, \"..\", \".\");");
                DbUtil.executeUpdate(conn, "update helpcontent set titletextkey = replace(titletextkey, \":\", \"\");");
                DbUtil.executeUpdate(conn, "update helpcontent set titletextkey = replace(titletextkey, \"/\", \"\");");
                DbUtil.executeUpdate(conn, "update helpcontent set titletextkey = replace(titletextkey, \"!\", \"\");");
                DbUtil.executeUpdate(conn, "update helpcontent set descriptiontextkey = concat(activetab, \".\", titletextkey);");
                DbUtil.executeUpdate(conn, "update helpcontent set titletextkey = 'Activate.Document.Versioning', descriptiontextkey = 'mydocuments.Activate.Document.Versioning' where compid = 'ver_butt' and activetab = 'mydocuments';");
                DbUtil.executeUpdate(conn, "update helpcontent set titletextkey = 'Import.Log', descriptiontextkey = 'importlog.Import.Log' where compid = 25 and activetab = 'importlog';");

                DbUtil.executeUpdate(conn, "alter table importlog add column logkey varchar(50);");
                DbUtil.executeUpdate(conn, "update importlog set logkey='pm.importlog.csvdiscarded' where log='CSV discarded due to incorrect data/mapping.';");
                DbUtil.executeUpdate(conn, "update importlog set logkey='pm.importlog.succPlan' where log='Project plan imported successfully.';");
                DbUtil.executeUpdate(conn, "update importlog set logkey='pm.importlog.discardrow' where log='Some rows have been discarded due to incorrect data.';");
                DbUtil.executeUpdate(conn, "update importlog set logkey = 'pm.contacts.import.csv.log.incorrect' where log = 'CSV discarded due to incorrect data/mapping.';");
                DbUtil.executeUpdate(conn, "update importlog set logkey = 'pm.contacts.import.csv.log.nonewcontacts' where log = 'There are no new contacts to import and duplicates contacts were not allowed.';");
                DbUtil.executeUpdate(conn, "update importlog set logkey = 'pm.contacts.import.csv.log.success' where log = 'Contacts imported successfully.';");
                DbUtil.executeUpdate(conn, "update importlog set logkey = 'pm.contacts.import.csv.log.discarded' where log = 'Some rows have been discarded.';");

                DbUtil.executeUpdate(conn, "ALTER TABLE `importlog` ADD COLUMN `modulenamekey` VARCHAR(50)  DEFAULT NULL AFTER `logkey`;");
                DbUtil.executeUpdate(conn, "update importlog set modulenamekey = if(modulename = 'Contacts', 'pm.contacts.text', 'pm.common.projectplan');");

                // There are DB Changes for User Permission redesign changes. Find the PermissionScript.jsp file in web/ folder and execute it.

                db_version_no += 0.01;

                DbUtil.executeUpdate(conn, "UPDATE dbversion SET version_no = ?, timestamp=now() WHERE dbname = ? ", new Object[]{db_version_no, dbname});
            }
            if (db_version_no < 2.14) {

                // There are DB Changes for correcting widget layouts. Find the correctWidgetLayout.jsp file in web/ folder and execute it.
                db_version_no += 0.01;

                DbUtil.executeUpdate(conn, "UPDATE dbversion SET version_no = ?, timestamp=now() WHERE dbname = ? ", new Object[]{db_version_no, dbname});

            }
            if (db_version_no < 2.15) {

                DbUtil.executeUpdate(conn, "CREATE TABLE `checklist` ("
                    + " `checklistid` VARCHAR(36)  CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,"
                    + " `checklistname` VARCHAR(256)  CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,"
                    + " `checklistdesc` VARCHAR(256)  CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,"
                    + " `createdby` VARCHAR(36)  DEFAULT NULL,"
                    + " `updatedby` VARCHAR(36)  DEFAULT NULL,"
                    + " `createdon` TIMESTAMP  NOT NULL DEFAULT '0000-00-00 00:00:00',"
                    + " `updatedon` TIMESTAMP  NOT NULL DEFAULT '0000-00-00 00:00:00' ON UPDATE CURRENT_TIMESTAMP,"
                    + " `companyid` VARCHAR(36)  CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,"
                    + " PRIMARY KEY (`checklistid`),"
                    + " CONSTRAINT `companyid_ibfk_1` FOREIGN KEY `companyid_ibfk_1` (`companyid`)"
                        + " REFERENCES `company` (`companyid`)"
                        + " ON DELETE CASCADE"
                        + " ON UPDATE CASCADE"
                    + " )"
                    + " ENGINE = InnoDB"
                    + " CHARACTER SET utf8 COLLATE utf8_general_ci"
                    + " COMMENT = 'Stores check lists';");

                DbUtil.executeUpdate(conn, "CREATE TABLE `checklisttasks` ("
                    + " `ctaskid` VARCHAR(36)  CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,"
                    + " `ctaskname` VARCHAR(256)  CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,"
                    + " `checklistid` VARCHAR(36)  CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,"
                    + " PRIMARY KEY (`ctaskid`),"
                    + " CONSTRAINT `checklistid_ibfk_1` FOREIGN KEY `checklistid_ibfk_1` (`checklistid`)"
                        + " REFERENCES `checklist` (`checklistid`)"
                        + " ON DELETE CASCADE"
                        + " ON UPDATE CASCADE"
                    + " )"
                    + " ENGINE = InnoDB"
                    + " CHARACTER SET utf8 COLLATE utf8_general_ci"
                    + " COMMENT = 'Holds check list tasks';");

                DbUtil.executeUpdate(conn, "CREATE TABLE `taskchecklistmapping` ("
                    + " `mappingid` VARCHAR(36)  CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,"
                    + " `taskid` VARCHAR(36)  CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,"
                    + " `checklistid` VARCHAR(36)  CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,"
                    + " `mappedby` VARCHAR(36)  CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,"
                    + " PRIMARY KEY (`mappingid`),"
                    + " CONSTRAINT `taskchecklistmapping_ibfk_1` FOREIGN KEY `taskchecklistmapping_ibfk_1` (`taskid`)"
                        + " REFERENCES `proj_task` (`taskid`)"
                        + " ON DELETE CASCADE"
                        + " ON UPDATE CASCADE,"
                    + " CONSTRAINT `taskchecklistmapping_ibfk_2` FOREIGN KEY `taskchecklistmapping_ibfk_2` (`checklistid`)"
                        + " REFERENCES `checklist` (`checklistid`)"
                        + " ON DELETE CASCADE"
                        + " ON UPDATE CASCADE"
                    + " )"
                    + " ENGINE = InnoDB"
                    + " CHARACTER SET utf8 COLLATE utf8_general_ci"
                    + " COMMENT = 'generates task and checklist mapping id used in todo';");

                DbUtil.executeUpdate(conn, "ALTER TABLE `todotask` ADD COLUMN `ischecklisttask` BIT(1)  NOT NULL DEFAULT 0 AFTER `duedate`;");

                DbUtil.executeUpdate(conn, "ALTER TABLE `company` ADD COLUMN `checklist` BIT(1)  NOT NULL DEFAULT 1 AFTER `milestonewidget`;");

                db_version_no += 0.01;
                DbUtil.executeUpdate(conn, "UPDATE dbversion SET version_no = ?, timestamp=now() WHERE dbname = ? ", new Object[]{db_version_no, dbname});

            }
            if (db_version_no < 2.16) {

                DbUtil.executeUpdate(conn, "CREATE TABLE `docsToConvertList` ("
                    + " `docid` VARCHAR(50)  CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,"
                    + " CONSTRAINT `docstoconvert_fk1` FOREIGN KEY `docstoconvert_fk1` (`docid`)"
                        + " REFERENCES `docs` (`docid`)"
                        + " ON DELETE CASCADE"
                        + " ON UPDATE CASCADE"
                    + " )"
                    + " ENGINE = InnoDB"
                    + " CHARACTER SET utf8 COLLATE utf8_general_ci;");

                DbUtil.executeUpdate(conn, "ALTER TABLE `company` ADD COLUMN `docaccess` BIT(1)  NOT NULL DEFAULT 0 AFTER `checklist`;");

                db_version_no += 0.01;
                DbUtil.executeUpdate(conn, "UPDATE dbversion SET version_no = ?, timestamp=now() WHERE dbname = ? ", new Object[]{db_version_no, dbname});

            }
        }
        conn.commit();
    } catch (ServiceException ex) {
        DbPool.quietRollback(conn);
        out.println(ex.getMessage());
    } finally {
        DbPool.quietClose(conn);
    }


%>
