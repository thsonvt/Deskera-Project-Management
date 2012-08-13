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
    Document   : PermissionScript
    Created on : Apr 14, 2012, 11:30:41 AM
    Author     : krawler
--%>

<%@page import="com.krawler.common.service.ServiceException"%>
<%@page import="com.krawler.database.DbResults"%>
<%@page import="java.util.ArrayList"%>
<%@page import="com.krawler.database.DbPool"%>
<%@page import="com.krawler.database.DbUtil"%>
<%@page import="com.krawler.database.DbPool.Connection"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <%
            Connection conn = null;
            ArrayList<String> querylist = new ArrayList<String>();
            String query = "";
            try {
                conn = DbPool.getConnection();

                query = "SET FOREIGN_KEY_CHECKS = 0";
                DbUtil.executeUpdate(conn, query);

                query = "ALTER TABLE featurelist "
                        + "ADD COLUMN displayfeaturename  varchar(50) NOT NULL AFTER featurename ";
                try {
                    DbUtil.executeUpdate(conn, query);
                } catch (ServiceException ex) {
                }

                query = "ALTER TABLE activitieslist "
                        + "ADD COLUMN havesubactivity  bit(1) NOT NULL AFTER displayactivityname ";
                try {
                    DbUtil.executeUpdate(conn, query);
                } catch (ServiceException ex) {
                }

                querylist.clear();
                querylist.add("DROP TABLE IF EXISTS `subactivitylist`");
                query = "CREATE TABLE `subactivitylist` ("
                        + "`parentactivityid`  int(11) NOT NULL ,"
                        + "`parentfeatureid`  int(11) NOT NULL ,"
                        + "`activityid`  int(11) NOT NULL ,"
                        + "`activityname`  varchar(50) NOT NULL ,"
                        + "`displayactivityname`  varchar(50) NOT NULL "
                        + ")";
                querylist.add(query);

                for (int i = 0; i < querylist.size(); i++) {
                    DbUtil.executeUpdate(conn, querylist.get(i).toString());
                }

                querylist.clear();
                querylist.add("DELETE FROM featurelist");
                querylist.add("INSERT INTO `featurelist` (featureid,featurename,displayfeaturename) VALUES (1, 'User', 'User Administration')");
                querylist.add("INSERT INTO `featurelist` (featureid,featurename,displayfeaturename) VALUES (2, 'Project', 'Project Administration')");
                querylist.add("INSERT INTO `featurelist` (featureid,featurename,displayfeaturename) VALUES (3, 'Company', 'Company Administration')");
                querylist.add("INSERT INTO `featurelist` (featureid,featurename,displayfeaturename) VALUES (4, 'AuditTrail', 'Audit Trail')");
                querylist.add("INSERT INTO `featurelist` (featureid,featurename,displayfeaturename) VALUES (5, 'CustomReport', 'Custom Report')");
                for (int i = 0; i < querylist.size(); i++) {
                    DbUtil.executeUpdate(conn, querylist.get(i).toString());
                }

                querylist.clear();
                querylist.add("DELETE FROM activitieslist");
                querylist.add("INSERT INTO `activitieslist` (featureid,activityid,activityname,displayactivityname,havesubactivity) VALUES (1, 1, 'ManagePermission', 'Manage Permission', 0)");
                querylist.add("INSERT INTO `activitieslist` (featureid,activityid,activityname,displayactivityname,havesubactivity) VALUES (1, 2, 'AssignUserToProject', 'Assign User To Project(s)', 0)");
                querylist.add("INSERT INTO `activitieslist` (featureid,activityid,activityname,displayactivityname,havesubactivity) VALUES (2, 1, 'CreateProject', 'Create Project', 0)");
                querylist.add("INSERT INTO `activitieslist` (featureid,activityid,activityname,displayactivityname,havesubactivity) VALUES (2, 2, 'EditProject', 'Edit Project', 0)");
                querylist.add("INSERT INTO `activitieslist` (featureid,activityid,activityname,displayactivityname,havesubactivity) VALUES (2, 3, 'DeleteProject', 'Delete Project', 0)");
                querylist.add("INSERT INTO `activitieslist` (featureid,activityid,activityname,displayactivityname,havesubactivity) VALUES (2, 4, 'ArchiveActive', 'Archive / Activate Projects', 0)");
                querylist.add("INSERT INTO `activitieslist` (featureid,activityid,activityname,displayactivityname,havesubactivity) VALUES (2, 5, 'ManageMembers', 'Manage Members', 1)");
                querylist.add("INSERT INTO `activitieslist` (featureid,activityid,activityname,displayactivityname,havesubactivity) VALUES (3, 1, 'ManageNotifications', 'Notifications Settings', 0)");
                querylist.add("INSERT INTO `activitieslist` (featureid,activityid,activityname,displayactivityname,havesubactivity) VALUES (3, 2, 'ManageFeatures', 'Manage Features ', 0)");
                querylist.add("INSERT INTO `activitieslist` (featureid,activityid,activityname,displayactivityname,havesubactivity) VALUES (3, 3, 'ManageCustomColumn', 'Custom Columns', 0)");
                querylist.add("INSERT INTO `activitieslist` (featureid,activityid,activityname,displayactivityname,havesubactivity) VALUES (3, 4, 'ManageHoliday', 'Company Holidays', 0)");
                querylist.add("INSERT INTO `activitieslist` (featureid,activityid,activityname,displayactivityname,havesubactivity) VALUES (3, 5, 'ModuleSubscription', 'Module Subscriptions', 0)");
                querylist.add("INSERT INTO `activitieslist` (featureid,activityid,activityname,displayactivityname,havesubactivity) VALUES (4, 1, 'AccessAuditTrail', 'Access Audit Log', 0)");
                querylist.add("INSERT INTO `activitieslist` (featureid,activityid,activityname,displayactivityname,havesubactivity) VALUES (5, 1, 'CreateDeleteReports', 'Create / Delete Custom Reports', 0)");
                querylist.add("INSERT INTO `activitieslist` (featureid,activityid,activityname,displayactivityname,havesubactivity) VALUES (5, 2, 'ViewCustomReports', 'View All Projects in Custom Reports', 0)");
                for (int i = 0; i < querylist.size(); i++) {
                    DbUtil.executeUpdate(conn, querylist.get(i).toString());
                }

                querylist.clear();
                querylist.add("INSERT INTO `subactivitylist` VALUES (5, 2, 1, 'AccessAllProjects', 'Manage members of all Projects')");
                querylist.add("INSERT INTO `subactivitylist` VALUES (5, 2, 2, 'AccessOnlyAssigned', 'Manage members only of Assigned Projects')");
                for (int i = 0; i < querylist.size(); i++) {
                    DbUtil.executeUpdate(conn, querylist.get(i).toString());
                }
                query = "select * from userpermissions where featureid = 2";
                DbResults row = DbUtil.executeQuery(conn, query);
                if (!row.next()) {
                    querylist.clear();
                    querylist.add("DROP TABLE IF EXISTS `userpermissions_subactivity`");
                    query = "CREATE TABLE `userpermissions_subactivity` ("
                        + "`userid`  varchar(50) NOT NULL ,"
                        + "`featureid`  int(11) NOT NULL ,"
                        + "`activityid`  int(11) NOT NULL ,"
                        + "`permissions`  int(11) NOT NULL ,"
                        + "CONSTRAINT `userid_constraint` FOREIGN KEY (`userid`) REFERENCES `userlogin` (`userid`) ON DELETE CASCADE ON UPDATE CASCADE,"
                        + "CONSTRAINT `featureid_constraint` FOREIGN KEY (`featureid`) REFERENCES `featurelist` (`featureid`) ON DELETE CASCADE ON UPDATE CASCADE"
                        + ")";
                    querylist.add(query);
                    for (int i = 0; i < querylist.size(); i++) {
                        DbUtil.executeUpdate(conn, querylist.get(i).toString());
                    }
                    query = "delete from userpermissions where featureid not in (1,3,4)";
                    DbUtil.executeUpdate(conn, query);

                    query = "select * from userpermissions where featureid in (3,4) order by featureid asc";
                    DbResults rs = DbUtil.executeQuery(conn, query);
                    while (rs.next()) {
                        querylist.clear();
                        int value = rs.getInt("permissions");
                        int feature = rs.getInt("featureid");
                        String userid = rs.getString("userid");
                        int newValue = 0;
                        int newSubValue = 0;
                        if (feature == 3) {
                            if ((value & 2) == 2) {
                                newValue = 30;
                            }
                            if ((value & 4) == 4) {
                                newValue += 32;
                                newSubValue = 2;
                            }
                        } else if (feature == 4) {
                            if ((value & 2) == 2) {
                                newValue = 26;
                            }

                        }
                        querylist.add("update userpermissions set featureid = " + (feature - 1) + ", permissions = " + newValue + " where userid = '" + userid + "' and featureid = " + feature);
                        if (newSubValue > 0) {
                            querylist.add("insert into userpermissions_subactivity values ('" + userid + "',2,5," + newSubValue + ")");
                        }
                        for (int i = 0; i < querylist.size(); i++) {
                            DbUtil.executeUpdate(conn, querylist.get(i).toString());
                        }
                    }
                    query = "select DISTINCT creator from company";
                    DbResults rs1 = DbUtil.executeQuery(conn, query);
                    while(rs1.next()){
                        String userid = rs1.getString("creator");
                        querylist.clear();
                        querylist.add("delete from userpermissions where userid = '"+userid+"'");
                        querylist.add("delete from userpermissions_subactivity where userid = '"+userid+"'");
                        querylist.add("insert into userpermissions values ('"+userid+"',1,6)");
                        querylist.add("insert into userpermissions values ('"+userid+"',2,62)");
                        querylist.add("insert into userpermissions values ('"+userid+"',3,62)");
                        querylist.add("insert into userpermissions values ('"+userid+"',4,2)");
                        querylist.add("insert into userpermissions values ('"+userid+"',5,6)");
                        querylist.add("insert into userpermissions_subactivity values ('"+userid+"',2,5,2)");
                        
                        for (int i = 0; i < querylist.size(); i++) {
                            DbUtil.executeUpdate(conn, querylist.get(i).toString());
                        }
                    }
                }
                query = "SET FOREIGN_KEY_CHECKS = 1";
                DbUtil.executeUpdate(conn, query);

                conn.commit();
            } catch (ServiceException ex) {
                DbPool.quietRollback(conn);
                System.out.println(ex.getMessage());
            } finally {
                DbPool.quietClose(conn);
            }

        %>
    </body>
</html>
