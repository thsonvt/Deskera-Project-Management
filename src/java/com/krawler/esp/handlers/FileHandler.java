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
package com.krawler.esp.handlers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Hashtable;

import com.krawler.common.service.ServiceException;
import com.krawler.common.timezone.Timezone;
import com.krawler.common.util.StringUtil;
import com.krawler.database.DbPool;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.database.DbPool.Connection;
import com.krawler.esp.database.dbcon;
import com.krawler.esp.user.UserDAO;
import com.krawler.esp.user.UserDAOImpl;
import com.krawler.svnwebclient.configuration.ConfigurationException;
import com.krawler.utils.json.KWLJsonConverter;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileHandler {

    public static void Setversion(Connection conn, String docid, String userid)
            throws ServiceException {
        // changed signature
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("update docs set version='Active' where docid=?");
            pstmt.setString(1, docid);
            pstmt.executeUpdate();
//            LogHandler.makeLogentry(conn, docid, userid, 67, 62, 57, null, null, null);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("File.FileChk", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

    public static String getDocType(Connection conn, String docid)
            throws ServiceException {

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement("select doctype from docs where docid=?");
            pstmt.setString(1, docid);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            } else {
                return "";
            }

        } catch (SQLException e) {
            throw ServiceException.FAILURE("FileHandler.getDocType", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

    public static Hashtable<String, String> getfileinfo(Connection conn,
            String Id) throws ServiceException {
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        Hashtable<String, String> ht = new Hashtable<String, String>();
        try {
            pstmt = conn.prepareStatement("select docdatemod,comments,docsize,docname,version,doctype,svnname,storageindex,userid,docstatus,docper from docs where docid = ?");
            pstmt.setString(1, Id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                ht.put("comments", StringUtil.checkForNull(rs.getString(2)));
                ht.put("size", StringUtil.checkForNull(rs.getString(3)));
                ht.put("docname", StringUtil.checkForNull(rs.getString(4)));
                ht.put("version", StringUtil.checkForNull(rs.getString(5)));
                ht.put("doctype", StringUtil.checkForNull(rs.getString(6)));
                ht.put("svnname", StringUtil.checkForNull(rs.getString(7)));
                ht.put("storeindex", StringUtil.checkForNull(rs.getString(8)));
                ht.put("userid", StringUtil.checkForNull(rs.getString(9)));
                String dts = Timezone.toCompanyTimezone(conn, rs.getString(1), CompanyHandler.getCompanyByUser(conn, rs.getString(9)));
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                java.util.Date dt = sdf.parse(dts);
                ht.put("date", sdf.format(dt));
                ht.put("status", StringUtil.checkForNull(rs.getString(10)));
                ht.put("per", StringUtil.checkForNull(rs.getString(11)));
            }

        } catch (ParseException ex) {
            throw ServiceException.FAILURE("FileHandler.getfileinfo", ex);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("FileHandler.getfileinfo", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return ht;
    }
    public static Hashtable[] getfilesinfo(Connection conn,
            String Id) throws ServiceException {
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        String idS[] = Id.split(",");
        Hashtable ht[] = new Hashtable[idS.length];
        try {
            for (int i = 0; i < idS.length; i++) {
                rs = null;
                pstmt = conn.prepareStatement("select docdatemod,comments,docsize,docname,version,doctype,svnname,storageindex,userid,docstatus,docper from docs where docid = ?");
                pstmt.setString(1, idS[i]);
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    ht[i] = new Hashtable();
                    ht[i].put("comments", StringUtil.checkForNull(rs.getString(2)));
                    ht[i].put("size", StringUtil.checkForNull(rs.getString(3)));
                    ht[i].put("docname", StringUtil.checkForNull(rs.getString(4)));
                    ht[i].put("version", StringUtil.checkForNull(rs.getString(5)));
                    ht[i].put("doctype", StringUtil.checkForNull(rs.getString(6)));
                    ht[i].put("svnname", StringUtil.checkForNull(rs.getString(7)));
                    ht[i].put("storeindex", StringUtil.checkForNull(rs.getString(8)));
                    ht[i].put("userid", StringUtil.checkForNull(rs.getString(9)));
                    String dts = Timezone.toCompanyTimezone(conn, rs.getString(1), CompanyHandler.getCompanyByUser(conn, rs.getString(9)));
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    java.util.Date dt = sdf.parse(dts);
                    ht[i].put("date", sdf.format(dt));
                    ht[i].put("status", StringUtil.checkForNull(rs.getString(10)));
                    ht[i].put("per", StringUtil.checkForNull(rs.getString(11)));
                }
            }
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("FileHandler.getfileinfo", ex);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("FileHandler.getfileinfo", e);
        } catch (Exception e) {
            throw ServiceException.FAILURE("FileHandler.getfileinfo", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return ht;
    }

    public static ArrayList<String> getonlineusers(Connection conn)
            throws ServiceException {
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        ArrayList<String> Id = new ArrayList<String>();
        try {
            pstmt = conn.prepareStatement("select userid from users "
                    + "inner join userlogin on users.userid = userlogin.userid where userlogin.isactive = true and users.userstatus='online'");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Id.add(rs.getString(1));
            }

        } catch (SQLException e) {
            throw ServiceException.FAILURE("FileHandler.getfileinfo", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return Id;
    }

    public static ArrayList<String> getShareddocusers(Connection conn,
            String docid) throws ServiceException {

        ResultSet rs = null;
        PreparedStatement pstmt = null;
        ArrayList<String> Id = new ArrayList<String>();
        try {
            pstmt = conn.prepareStatement("(select docprerel.userid from docprerel inner join userlogin on docprerel.userid = userlogin.userid2 where docid = ? and permission=? and userlogin.isactive = true) "
                    + "union (select userrelations.userid2 from docprerel inner join userrelations on userrelations.userid1=docprerel.userid inner join userlogin on userlogin.userid = userrelations.userid1 where docprerel.docid = ? and docprerel.permission=? and userrelations.relationid=3 "
                    + "and userlogin.isactive = true) "
                    + "union (select userrelations.userid1 from docprerel inner join userrelations on userrelations.userid2=docprerel.userid inner join userlogin on userlogin.userid = userrelations.userid2 where docprerel.docid = ? and docprerel.permission=? and userrelations.relationid=3 "
                    + "and userlogin.isactive = true) "
                    + "union (select users.userid from docprerel inner join users on users.userid=docprerel.userid inner join userlogin on users.userid = userlogin.userid where users.userstatus='online' and docprerel.permission=? and docprerel.docid = ? and userlogin.isactive = true)");
            pstmt.setString(1, docid);
            pstmt.setString(2, "2");
            pstmt.setString(3, docid);
            pstmt.setString(4, "1");
            pstmt.setString(5, docid);
            pstmt.setString(6, "1");
            pstmt.setString(7, "3");
            pstmt.setString(8, docid);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Id.add(rs.getString(1));
            }

        } catch (SQLException e) {
            throw ServiceException.FAILURE("FileHandler.getfileinfo", e);

        } finally {
            DbPool.closeStatement(pstmt);
        }
        return Id;
    }

    public static Hashtable<String, Object> FileChk(Connection conn,
            String docname, String userid, String groupid, String pcid)
            throws ServiceException {

        String sql = null;
        Hashtable<String, Object> responseText = new Hashtable<String, Object>();
        String userdocid = null;
        String docownerid = null;
        DbResults rs = null;
        DbResults rs1 = null;
        DbResults rs2 = null;
        if (groupid.equals("1")) {
            sql = "select docs.docid,docs.userid from docs inner join users on users.userid=docs.userid where docname = ? and users.userid = ? and docs.groupid = ?";
            rs = DbUtil.executeQuery(conn, sql, new Object[]{docname, userid,
                        groupid});
            if (rs.next()) {
                userdocid = rs.getString(1);
                docownerid = rs.getString(2);
                boolean flag = false;
                sql = "select docs.userid,userlogin.username,docs.docid from docprerel inner join docs on docs.docid=docprerel.docid inner join users " +
                        "on users.userid=docs.userid inner join userlogin on users.userid = userlogin.userid "
                        + "where docs.docid in (select docid from docs where docname = ? and userid !=? and userid not in (select userid from userlogin where isactive = false))and docprerel.permission=? and docprerel.userid=? and docprerel.readwrite = ? and docs.groupid = ?";
                rs1 = DbUtil.executeQuery(conn, sql, new Object[]{docname,
                            userid, "2", userid, 1, groupid});

                ArrayList<String> share = new ArrayList<String>();
                ArrayList<String> id = new ArrayList<String>();
                ArrayList<String> uname = new ArrayList<String>();
                while (rs1.next()) {
                    share.add(rs1.getString(1));
                    id.add(rs1.getString(3));
                    uname.add(rs1.getString(2));
                    flag = true;
                }
                if (flag) {
                    responseText.put("type", 1);
                    responseText.put("userdocid", userdocid);
                    responseText.put("value", share);
                    responseText.put("id", id);
                    responseText.put("username", uname);
                } else {
                    responseText.put("type", 2);
                    responseText.put("userdocid", userdocid);
                    responseText.put("docownerid", docownerid);
                }
            } else {
                rs1 = DbUtil.executeQuery(conn, "select count(*) from docs where docname = ?", docname);
                if (rs1.next()) {
                    if (rs1.getInt(1) > 0) {
                        boolean flag = false;
                        sql = "select docs.userid,userlogin.username,docs.docid from docprerel "
                                + "inner join docs on docs.docid=docprerel.docid "
                                + "inner join userlogin on userlogin.userid=docs.userid "
                                + "where docs.docid in (select docid from docs where docname = ? and userid != ? and userid not in (select userid from userlogin where isactive = false))"
                                + "and docprerel.permission=? and docprerel.userid= ? and docprerel.readwrite = ? and docs.groupid = ?";
                        rs2 = DbUtil.executeQuery(conn, sql, new Object[]{docname, userid, "2", userid, 1, groupid});
                        ArrayList<String> share = new ArrayList<String>();
                        ArrayList<String> id = new ArrayList<String>();
                        ArrayList<String> uname = new ArrayList<String>();
                        while (rs2.next()) {
                            share.add(rs2.getString(1));
                            id.add(rs2.getString(3));
                            uname.add(rs2.getString(2));
                            flag = true;
                        }
                        if (flag) {
                            responseText.put("type", 3);
                            responseText.put("value", share);
                            responseText.put("id", id);
                            responseText.put("username", uname);
                        } else {
                            responseText.put("type", 4);
                        }
                    } else {
                        responseText.put("type", 4);
                    }
                }
            }
        } else {
            if (groupid.equals("2")) {
                // sql = "select docs.docid from docprerel inner join docs on
                // docs.docid=docprerel.docid where docs.docid = (select docid
                // from docs where docname = ? and groupid =? )and
                // docprerel.permission=? and docprerel.userid= (select
                // projectid from project where projectname = ?)";
                sql = "select docs.docid from docs inner join docprerel on docs.docid=docprerel.docid inner join project on project.projectid=docprerel.userid "
                        + "where docs.groupid=2 and docs.docname=? and project.projectid=?";
                rs = DbUtil.executeQuery(conn, sql, new Object[]{docname, pcid});
            } else {
                // sql = "select docs.docid from docprerel inner join docs on
                // docs.docid=docprerel.docid where docs.docid = (select docid
                // from docs where docname = ? and groupid =? )and
                // docprerel.permission=? and docprerel.userid= (select
                // communityid from community where communityname = ?)";
                sql = "select docs.docid from docs inner join docprerel on docs.docid=docprerel.docid inner join community on community.communityid=docprerel.userid "
                        + "where docs.groupid=3 and docs.docname=? and community.communityid=?";
                rs = DbUtil.executeQuery(conn, sql, new Object[]{docname, pcid});
            }
            if (rs.next()) {
                responseText.put("type", 2);
                responseText.put("userdocid", rs.getString(1));
            } else {
                responseText.put("type", 4);
            }
        }
        return responseText;

    }

    public static int getSizeKb(String size) {
        int no = ((Integer.parseInt(size)) / 1024);
        if (no >= 1) {
            return no;
        } else {
            return 1;
        }

    }

    public static String getFilename(String name) throws ServiceException {
        String fName = "";
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        com.krawler.database.DbPool.Connection conn = null;

        try {
            conn = DbPool.getConnection();
            pstmt = conn.prepareStatement("select docname from docs where docid = ?");
            String Id = name.substring(0, name.lastIndexOf("."));
            pstmt.setString(1, Id);
            rs = pstmt.executeQuery();
            rs.next();
            fName = rs.getString(1);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("FileHandler.getFilename", e);
        } finally {
            DbPool.quietClose(conn);
        }
        return fName;
    }

    public static ArrayList<DbResults> getSharedDocs(Connection conn,
            String userid, String tagname, String companyid) throws ServiceException {
        ArrayList<DbResults> rs = new ArrayList<DbResults>();
        String sql = null;
        String ownerid = "";
        if (tagname.contains("/") && !tagname.equals("")) {
            ownerid = ProfileHandler.getUserid(conn, tagname.split("/")[1], companyid);
        }
        if (ownerid.equals("")) {
            sql = "SELECT distinct docid, docname, docsize, doctype, docdatemod,docper,docstatus,version,userid,storageindex,docprojper, 1 as readwrite FROM docs where docid in "
                    + "(select docid from docprerel where permission = ? and userid!= ? and userid in (select userid from users where companyid=?)) "
                    + "UNION "
                    + "select distinct docs.docid,docs.docname,docs.docsize, docs.doctype,docs.docdatemod,docs.docper,docs.docstatus,docs.version,docs.userid,docs.storageindex,docprojper, 1 AS readwrite from docs "
                    + "where docs.docid in(select docprerel.docid from docprerel where docprerel.userid in "
                    + "(select  userid2 as userid from userrelations where userid1 = ? union select  userid1 as userid from userrelations where  userid2= ? and relationid = '3') and permission=?) "
                    + "UNION "
                    + "select distinct docs.docid,docs.docname,docs.docsize, docs.doctype,docs.docdatemod,docs.docper,docs.docstatus,docs.version,docs.userid,docs.storageindex ,docprojper, docprerel.readwrite "
                    + "from docs inner join docprerel on docprerel.docid = docs.docid"
                    + " where docs.docid in (select docprerel.docid from docprerel where docprerel.userid = ? and docprerel.permission = ?) "
                    + "UNION "
                    + "SELECT DISTINCT docs.docid,docs.docname,docs.docsize, docs.doctype,docs.docdatemod,docs.docper,docs.docstatus,docs.version,docs.userid,docs.storageindex ,docprojper, docprerel.readwrite "
                    + "FROM docs INNER JOIN docprerel ON docprerel.docid = docs.docid "
                    + "WHERE docs.userid != ? AND docs.docid IN "
                    + "(SELECT docprerel.docid FROM docprerel WHERE docprerel.userid IN "
                    + "(SELECT projectid FROM projectmembers WHERE userid = ?) AND (docprerel.permission = ? OR docprerel.permission = ?)) order by docdatemod ";
            rs.add(DbUtil.executeQuery(conn, sql, new Object[]{"3", userid, companyid, userid, userid, "1", userid, "2", userid, userid, "9", "10"}));
//            sql = "SELECT distinct docid, docname, docsize, doctype, docdatemod,docper,docstatus,version,userid,storageindex,docprojper FROM docs where docid in "
//                    + "(select docid from docprerel where permission = ? and userid!= ? and userid in (select userid from users where companyid=?)) ORDER BY docdatemod DESC; ";
//            rs.add(DbUtil.executeQuery(conn, sql, new Object[]{"3", userid, companyid}));
//            sql = "select distinct docs.docid,docs.docname,docs.docsize, docs.doctype,docs.docdatemod,docs.docper,docs.docstatus,docs.version,docs.userid,docs.storageindex,docprojper, 1 AS readwrite from docs "
//                    + "where docs.docid in(select docprerel.docid from docprerel where docprerel.userid in"
//                    + "(select  userid2 as userid from userrelations where userid1 = ? union select  userid1 as userid from userrelations where  userid2= ? and relationid = '3') and permission=?) ORDER BY docdatemod DESC";
//            rs.add(DbUtil.executeQuery(conn, sql, new Object[]{userid, userid, "1"}));
//            sql = "select distinct docs.docid,docs.docname,docs.docsize, docs.doctype,docs.docdatemod,docs.docper,docs.docstatus,docs.version,docs.userid,docs.storageindex ,docprerel.readwrite,docprojper "
//                    + "from docs inner join docprerel on docprerel.docid = docs.docid"
//                    + " where docs.docid in (select docprerel.docid from docprerel where docprerel.userid = ? and docprerel.permission = ?) ORDER BY docdatemod DESC";
//            rs.add(DbUtil.executeQuery(conn, sql, new Object[]{userid, "2"}));
//            sql = "SELECT DISTINCT docs.docid,docs.docname,docs.docsize, docs.doctype,docs.docdatemod,docs.docper,docs.docstatus,docs.version,docs.userid,docs.storageindex ,docprerel.readwrite,docprojper "
//                    + "FROM docs INNER JOIN docprerel ON docprerel.docid = docs.docid "
//                    + "WHERE docs.userid != ? AND docs.docid IN "
//                    + "(SELECT docprerel.docid FROM docprerel WHERE docprerel.userid IN "
//                    + "(SELECT projectid FROM projectmembers WHERE userid = ?) AND (docprerel.permission = ? OR docprerel.permission = ?)) ORDER BY docdatemod DESC;";
//            rs.add(DbUtil.executeQuery(conn, sql, new Object[]{userid, userid, "9", "10"}));
        } else {
            sql = "SELECT distinct docid, docname, docsize, doctype, docdatemod,docper,docstatus,version,userid,storageindex,1 as readwrite, docprojper "
                    + "FROM docs where docid in (select docid from docprerel where permission = ? and userid!= ? and userid in (select userid from users where companyid=?))"
                    + "and docid in (SELECT docid from docs where userid = ?) "
                    + " UNION "
                    + "select distinct docs.docid,docs.docname,docs.docsize, docs.doctype,docs.docdatemod,docs.docper,docs.docstatus,docs.version,docs.userid,docs.storageindex,1 as readwrite, docprojper "
                    + "from docs " + "where docs.docid in(select docprerel.docid from docprerel where docprerel.userid in"
                    + "(select  userid2 as userid from userrelations where userid1 = ? union select  userid1 as userid from userrelations where  userid2= ? and relationid = '3') and permission=?)"
                    + "and docid in (SELECT docid from docs where userid = ?) "
                    + " UNION "
                    + "select distinct docs.docid,docs.docname,docs.docsize, docs.doctype,docs.docdatemod,docs.docper,docs.docstatus,docs.version,docs.userid,docs.storageindex ,docprerel.readwrite,docprojper "
                    + "from docs inner join docprerel on docprerel.docid = docs.docid "
                    + "where docs.docid in (select docprerel.docid from docprerel where docprerel.userid = ? and docprerel.permission = ?)"
                    + "and  docs.docid in (SELECT docid from docs where userid = ?) "
                    + " UNION "
                    + "SELECT DISTINCT docs.docid,docs.docname,docs.docsize, docs.doctype,docs.docdatemod,docs.docper,docs.docstatus,docs.version,docs.userid,docs.storageindex ,docprerel.readwrite,docprojper "
                    + "FROM docs INNER JOIN docprerel ON docprerel.docid = docs.docid "
                    + "WHERE docs.userid = ? AND docs.docid IN "
                    + "(SELECT docprerel.docid FROM docprerel WHERE docprerel.userid IN "
                    + "(SELECT projectid FROM projectmembers WHERE userid = ?) AND (docprerel.permission = ? OR docprerel.permission = ?)) ";
            rs.add(DbUtil.executeQuery(conn, sql, new Object[]{"3", userid, companyid, ownerid, userid, userid, "1", ownerid, userid, "2", ownerid, ownerid, userid, "9", "10"}));
//            sql = "SELECT distinct docid, docname, docsize, doctype, docdatemod,docper,docstatus,version,userid,storageindex,docprojper "
//                    + "FROM docs where docid in (select docid from docprerel where permission = ? and userid!= ? and userid in (select userid from users where companyid=?))"
//                    + "and docid in (SELECT docid from docs where userid = ?) ORDER BY docdatemod DESC; ";
//            rs.add(DbUtil.executeQuery(conn, sql, new Object[]{"3", userid, companyid, ownerid}));
//            sql = "select distinct docs.docid,docs.docname,docs.docsize, docs.doctype,docs.docdatemod,docs.docper,docs.docstatus,docs.version,docs.userid,docs.storageindex,docprojper "
//                    + "from docs " + "where docs.docid in(select docprerel.docid from docprerel where docprerel.userid in"
//                    + "(select  userid2 as userid from userrelations where userid1 = ? union select  userid1 as userid from userrelations where  userid2= ? and relationid = '3') and permission=?)"
//                    + "and docid in (SELECT docid from docs where userid = ?) ORDER BY docdatemod DESC;";
//            rs.add(DbUtil.executeQuery(conn, sql, new Object[]{userid, userid, "1", ownerid}));
//            sql = "select distinct docs.docid,docs.docname,docs.docsize, docs.doctype,docs.docdatemod,docs.docper,docs.docstatus,docs.version,docs.userid,docs.storageindex ,docprerel.readwrite,docprojper "
//                    + "from docs inner join docprerel on docprerel.docid = docs.docid "
//                    + "where docs.docid in (select docprerel.docid from docprerel where docprerel.userid = ? and docprerel.permission = ?)"
//                    + "and  docs.docid in (SELECT docid from docs where userid = ?) ORDER BY docdatemod DESC;";
//            rs.add(DbUtil.executeQuery(conn, sql, new Object[]{userid, "2", ownerid}));
//            sql = "SELECT DISTINCT docs.docid,docs.docname,docs.docsize, docs.doctype,docs.docdatemod,docs.docper,docs.docstatus,docs.version,docs.userid,docs.storageindex ,docprerel.readwrite,docprojper "
//                    + "FROM docs INNER JOIN docprerel ON docprerel.docid = docs.docid "
//                    + "WHERE docs.userid = ? AND docs.docid IN "
//                    + "(SELECT docprerel.docid FROM docprerel WHERE docprerel.userid IN "
//                    + "(SELECT projectid FROM projectmembers WHERE userid = ?) AND (docprerel.permission = ? OR docprerel.permission = ?)) ORDER BY docdatemod DESC;";
//            rs.add(DbUtil.executeQuery(conn, sql, new Object[]{ownerid, userid, "9", "10"}));
        }
        return rs;
    }

    public static ArrayList<DbResults> getSharedDocsForSpecificProj(Connection conn,
            String userid, String tagname, String companyid, String projid) throws ServiceException {
        ArrayList<DbResults> rs = new ArrayList<DbResults>();
        String sql = null;
        String ownerid = "";
        if (tagname.contains("/") && !tagname.equals("")) {
            ownerid = ProfileHandler.getUserid(conn, tagname.split("/")[1], companyid);
        }
        if (ownerid.equals("")) {
            sql = "SELECT distinct docid, docname, docsize, doctype, docdatemod,docper,docstatus,version,userid,storageindex,docprojper "
                    + "FROM docs where docid in (select docid from docprerel where userid = ? "
                    + "AND (permission = '9' OR permission = '10')) AND userid != ? ORDER BY docdatemod DESC";
            rs.add(DbUtil.executeQuery(conn, sql, new Object[]{projid, userid}));
        } else {
            sql = "SELECT distinct docid, docname, docsize, doctype, docdatemod,docper,docstatus,version,userid,storageindex,docprojper "
                    + "FROM docs where docid in (select docid from docprerel where userid = ? and (permission = '9' OR permission = '10'))"
                    + " and userid = ? ORDER BY docdatemod DESC";
            rs.add(DbUtil.executeQuery(conn, sql, new Object[]{projid, ownerid}));
        }
        return rs;
    }

    public static String getReadWritePermission(String userid, String docid)
            throws ServiceException {
        String rw = "";
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        com.krawler.database.DbPool.Connection conn = null;
        try {
            conn = DbPool.getConnection();
            pstmt = conn.prepareStatement("select readwrite from docprerel where userid = ? and docid = ?");
            pstmt.setString(1, userid);
            pstmt.setString(2, docid);
            rs = pstmt.executeQuery();
            rs.next();
            rw = rs.getString(1);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("FileHandler.getAuthor", e);
        } finally {
            DbPool.quietClose(conn);
        }
        return rw;
    }

    public static ArrayList getSharedPermission(String docid)
            throws ServiceException {
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        com.krawler.database.DbPool.Connection conn = null;
        ArrayList<Integer> permission = new ArrayList<Integer>();
        try {
            conn = DbPool.getConnection();
            pstmt = conn.prepareStatement(" select permission from docprerel where docid = ? ");
            pstmt.setString(1, docid);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                permission.add(Integer.parseInt(rs.getString("permission")));
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("FileHandler.getSharedPermission", e);
        } finally {
            DbPool.quietClose(conn);
        }
        return permission;
    }

    public static String getDocDetails(Connection conn, String docid, String userid)
            throws ServiceException {
        PreparedStatement pstmt = null;
        try {
            KWLJsonConverter kjs = new KWLJsonConverter();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-d HH:mm:ss");
            ResultSet rs = null;
            String companyid = CompanyHandler.getCompanyByUser(conn, userid);
            pstmt = conn.prepareStatement("select * from docs where docid = ?");
            pstmt.setString(1, docid);
            rs = pstmt.executeQuery();
            String json = null;
            json = kjs.GetJsonForGrid(rs);
            if (json.compareTo("{\"data\":{}}") != 0) {
                JSONObject jobj = new JSONObject(json);
                for (int i = 0; i < jobj.getJSONArray("data").length(); i++) {
                    String sdtStr = jobj.getJSONArray("data").getJSONObject(i).getString("docdatemod");
                    sdtStr = Timezone.toCompanyTimezone(conn, sdtStr, companyid);
                    if (sdtStr.compareTo("") != 0) {
                        java.util.Date dt = sdf.parse(sdtStr);
                        sdtStr = sdf.format(dt);
                        jobj.getJSONArray("data").getJSONObject(i).remove("docdatemod");
                        jobj.getJSONArray("data").getJSONObject(i).put("docdatemod", sdtStr);
                    }
                }
            }
            return json;
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("FileHandler.getDocDetails", ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("FileHandler.getDocDetails", ex);
        } catch (SQLException ex) {
            throw ServiceException.FAILURE("FileHandler.getDocDetails", ex);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

    public static String fillGrid(Connection conn, String userid, String tagname, String groupid, String pcid,
            String companyid, String searchJson) throws ServiceException {
        String gridString = null;
        DbResults rs = null;
        String sql = null;
        // JSONStringer j = new JSONStringer();
        ArrayList<String> DefaultTag = new ArrayList<String>();
        DefaultTag.add("shared");
        DefaultTag.add("uncategorized");
        if (groupid.equals("1")) {
            pcid = "1";
        }
        if (tagname == null || tagname.equals("*flag*")) {
            if (groupid.equals("1")) {
                sql = "SELECT docid, docname, docsize, doctype, docdatemod,docper,docstatus,version,docs.userid,storageindex,docprojper FROM docs inner join users on docs.userid=users.userid WHERE docs.userid = ? and groupid = ? ";
                String temp = sql;
                sql = getSearchString(searchJson, sql, companyid);
                sql = sql + " ORDER BY docdatemod DESC";
                temp = temp + " ORDER BY docdatemod DESC";
                rs = DbUtil.executeQuery(conn, sql, new Object[]{userid, groupid});
                ArrayList<DbResults> rs1 = new ArrayList<DbResults>();
                if (temp.compareTo(sql) == 0) {
                    rs1 = getSharedDocs(conn, userid, "", companyid);
                }
                gridString = "{data:[";
                gridString += getJString(rs, conn, userid, groupid, pcid);
                for (int i = 0; i < rs1.size(); i++) {
                    gridString += getJString(rs1.get(i), conn, userid, groupid, pcid);
                }
                if (gridString.charAt(gridString.length() - 1) != '[') {
                    gridString = gridString.substring(0, (gridString.length() - 1));
                }
                gridString += "]}";
            }
            if (groupid.equals("2")) {
                gridString = "{data:[";
                sql = "SELECT docid, docname, docsize, doctype, docdatemod,docper,docstatus,version,userid,storageindex,docprojper "
                        + "FROM docs WHERE docid IN (SELECT docid FROM docprerel WHERE permission = ? AND userid = ?) ORDER BY docdatemod DESC;";
                rs = DbUtil.executeQuery(conn, sql, new Object[]{"5", pcid});
                gridString += getJString(rs, conn, userid, groupid, pcid);
                if (gridString.charAt(gridString.length() - 1) != '[') {
                    gridString = gridString.substring(0, (gridString.length() - 1));
                }
                gridString += "]}";
            }
            if (groupid.equals("3")) {
                gridString = "{data:[";
                sql = "SELECT docid, docname, docsize, doctype, docdatemod,docper,docstatus,docs.version,docs.userid,docs.storageindex,docprojper "
                        + "FROM docs WHERE docid IN (SELECT docid FROM docprerel WHERE permission = ? AND userid = ?) ORDER BY docdatemod DESC; ";
                rs = DbUtil.executeQuery(conn, sql, new Object[]{"6", pcid});
                gridString += getJString(rs, conn, userid, groupid, pcid);
                if (gridString.charAt(gridString.length() - 1) != '[') {
                    gridString = gridString.substring(0, (gridString.length() - 1));
                }
                gridString += "]}";
            }
        } else {
            gridString = "{data:[";
            String Shared = "shared";
            if (DefaultTag.contains(tagname.toLowerCase()) || tagname.toLowerCase().startsWith(Shared + "/")) {
                if (groupid.equals("1") && (StringUtil.stringCompareInLowercase(Shared, tagname) || tagname.toLowerCase().startsWith(Shared + "/"))) {
                    ArrayList<DbResults> rs1 = getSharedDocs(conn, userid, tagname, companyid);
                    for (int i = 0; i < rs1.size(); i++) {
                        gridString += getJString(rs1.get(i), conn, userid, groupid, pcid);
                    }
                } else if (StringUtil.stringCompareInLowercase("uncategorized", tagname)) {
                    if (groupid.equals("1")) {
                        sql = "SELECT DISTINCT docs.docid,docs.docname,docs.docsize,docs.doctype,docs.docdatemod,docs.docper,docs.docstatus,docs.version,docs.userid,docs.storageindex,docprojper "
                                + "FROM docs WHERE docs.userid = ? AND groupid = ? AND docs.docid NOT IN (SELECT docid FROM docstags WHERE id = ?) ORDER BY docdatemod DESC";
                        rs = DbUtil.executeQuery(conn, sql, new Object[]{userid, groupid, userid});
                    } else {
                        sql = "SELECT DISTINCT docs.docid,docs.docname,docs.docsize,docs.doctype,docs.docdatemod,docs.docper,docs.docstatus,docs.version,docs.userid,docs.storageindex,docprojper"
                                + " FROM docs WHERE groupid = ? AND docs.docid IN (SELECT DISTINCT docprerel.docid FROM docprerel "
                                + "WHERE docprerel.userid = ? AND docprerel.permission = ? AND docprerel.docid NOT IN (SELECT docstags.docid FROM docstags WHERE docstags.id = ?) "
                                + "and docprerel.userid not in (select users.userid from userlogin inner join users where users.companyid = ? and isactive = true)) ORDER BY docdatemod DESC";
                        if (groupid.equals("2")) {
                            rs = DbUtil.executeQuery(conn, sql, new Object[]{groupid, pcid, "5", pcid, companyid});
                        } else {
                            rs = DbUtil.executeQuery(conn, sql, new Object[]{groupid, pcid, "6", pcid, companyid});
                        }
                    }
                    gridString += getJString(rs, conn, userid, groupid, pcid);
                }
            } else {
                tagname = TagUtils.checkTag(tagname);
                if (groupid.equals("1")) {
                    sql = "SELECT DISTINCT docs.docid,docs.docname,docs.docsize,docs.doctype,docs.docdatemod,docs.docper,docs.docstatus,docs.version,docs.userid,docs.storageindex,docprojper FROM docs "
                            + "INNER JOIN docstags ON docs.docid = docstags.docid INNER JOIN unitags ON unitags.tagid = docstags.tagid "
                            + "INNER JOIN users ON users.userid = docstags.id WHERE unitags.tagname = ? AND users.userid = ? ORDER BY docdatemod DESC";
                    rs = DbUtil.executeQuery(conn, sql, new Object[]{tagname, userid});
                } else {
                    sql = "SELECT DISTINCT docs.docid,docs.docname,docs.docsize,docs.doctype,docs.docdatemod,docs.docper,docs.docstatus,docs.version,docs.userid,docs.storageindex,docprojper FROM docs "
                            + "INNER JOIN docstags ON docstags.docid = docs.docid INNER JOIN unitags ON unitags.tagid = docstags.tagid "
                            + " WHERE unitags.tagname = ? AND docstags.id = ? AND docs.groupid=? ORDER BY docdatemod DESC;";
                    rs = DbUtil.executeQuery(conn, sql, new Object[]{tagname, pcid, groupid});
                }
                gridString += getJString(rs, conn, userid, groupid, pcid);
            }
            if (gridString.charAt(gridString.length() - 1) != '[') {
                gridString = gridString.substring(0, (gridString.length() - 1));
            }
            gridString += "]}";
        }
        return gridString;
    }

    public static String fillGridForSpecificProject(Connection conn, String userid, String tagname, String groupid, String pcid,
            String companyid, String pid, String searchJson) throws ServiceException {
        String gridString = null;
        DbResults rs = null;
        String sql = null;
        // JSONStringer j = new JSONStringer();
        ArrayList<String> DefaultTag = new ArrayList<String>();
        DefaultTag.add("shared");
        DefaultTag.add("uncategorized");
        if (groupid.equals("1")) {
            pcid = "1";
        }
        if (tagname == null || tagname.equals("*flag*")) {
            if (groupid.equals("1")) {
                sql = "SELECT docid, docname, docsize, doctype, docdatemod,docper,docstatus,version,userid,storageindex,docprojper FROM docs "
                        + "WHERE userid = ? and groupid = ? and docid in (SELECT projectspacificdocs.docid FROM projectspacificdocs where projectid = ?) ";
                String temp = sql;
                sql = getSearchString(searchJson, sql, companyid);
                sql = sql + " ORDER BY docdatemod DESC";
                temp = temp + " ORDER BY docdatemod DESC";
                rs = DbUtil.executeQuery(conn, sql, new Object[]{userid, groupid, pid});
                ArrayList<DbResults> rs1 = new ArrayList<DbResults>();
                if (temp.compareTo(sql) == 0) {
                    rs1 = getSharedDocsForSpecificProj(conn, userid, "", companyid, pid);
                }
                gridString = "{data:[";
                gridString += getJString(rs, conn, userid, groupid, pcid);
                for (int i = 0; i < rs1.size(); i++) {
                    gridString += getJString(rs1.get(i), conn, userid, groupid, pcid);
                }
                if (gridString.charAt(gridString.length() - 1) != '[') {
                    gridString = gridString.substring(0, (gridString.length() - 1));
                }
                gridString += "]}";
            }
        } else {
            gridString = "{data:[";
            String Shared = "shared";
            if (DefaultTag.contains(tagname.toLowerCase()) || tagname.toLowerCase().startsWith(Shared + "/")) {
                if (groupid.equals("1") && (StringUtil.stringCompareInLowercase(Shared, tagname) || tagname.toLowerCase().startsWith(Shared + "/"))) {
                    ArrayList<DbResults> rs1 = getSharedDocsForSpecificProj(conn, userid, tagname, companyid, pid);
                    for (int i = 0; i < rs1.size(); i++) {
                        gridString += getJString(rs1.get(i), conn, userid, groupid, pcid);
                    }
                } else if (StringUtil.stringCompareInLowercase("uncategorized", tagname)) {
                    if (groupid.equals("1")) {
                        sql = "SELECT DISTINCT docs.docid,docs.docname,docs.docsize,docs.doctype,docs.docdatemod,docs.docper,docs.docstatus,docs.version,docs.userid,docs.storageindex,docprojper "
                                + "FROM docs WHERE docs.userid = ? AND groupid = ? AND docs.docid IN (SELECT docid FROM projectspacificdocs WHERE projectid = ?) ORDER BY docdatemod DESC";
                        rs = DbUtil.executeQuery(conn, sql, new Object[]{userid, groupid, pid});
                    } else {
                        sql = "SELECT DISTINCT docs.docid,docs.docname,docs.docsize,docs.doctype,docs.docdatemod,docs.docper,docs.docstatus,docs.version,docs.userid,docs.storageindex,docprojper"
                                + " FROM docs WHERE groupid = ? AND docs.docid IN (SELECT docid FROM projectspacificdocs"
                                + "WHERE projectid = ?) ORDER BY docdatemod DESC";
                        if (groupid.equals("2")) {
                            rs = DbUtil.executeQuery(conn, sql, new Object[]{groupid, pid});
                        } else {
                            rs = DbUtil.executeQuery(conn, sql, new Object[]{groupid, pcid, "6", pcid});
                        }
                    }
                    gridString += getJString(rs, conn, userid, groupid, pcid);
                }
            } else {
                tagname = TagUtils.checkTag(tagname);
                if (groupid.equals("1")) {
                    sql = "SELECT DISTINCT docs.docid,docs.docname,docs.docsize,docs.doctype,docs.docdatemod,docs.docper,docs.docstatus,docs.version,docs.userid,docs.storageindex,docprojper FROM docs "
                            + "INNER JOIN docstags ON docs.docid = docstags.docid INNER JOIN unitags ON unitags.tagid = docstags.tagid "
                            + "INNER JOIN users ON users.userid = docstags.id WHERE unitags.tagname = ? AND users.userid = ? and docs.docid in (select docid from projectspacificdocs where projectid=?) ORDER BY docdatemod DESC";
                    rs = DbUtil.executeQuery(conn, sql, new Object[]{tagname, userid, pid});
                } else {
                    sql = "SELECT DISTINCT docs.docid,docs.docname,docs.docsize,docs.doctype,docs.docdatemod,docs.docper,docs.docstatus,docs.version,docs.userid,docs.storageindex,docprojper FROM docs "
                            + "INNER JOIN docstags ON docstags.docid = docs.docid INNER JOIN unitags ON unitags.tagid = docstags.tagid "
                            + " WHERE unitags.tagname = ? AND docstags.id = ? AND docs.groupid=? ORDER BY docdatemod DESC;";
                    rs = DbUtil.executeQuery(conn, sql, new Object[]{tagname, pcid, groupid});
                }
                gridString += getJString(rs, conn, userid, groupid, pcid);
            }
            if (gridString.charAt(gridString.length() - 1) != '[') {
                gridString = gridString.substring(0, (gridString.length() - 1));
            }
            gridString += "]}";
        }
        return gridString;
    }

    public static String getJString(com.krawler.database.DbResults rsforJson,
            Connection conn, String userid, String groupid, String pcid)
            throws ServiceException {
        JSONObject jobj = null;
        String gridString = "";
        UserDAO udao = new UserDAOImpl();
        try {
            while (rsforJson.next()) {
                String id = "";
                if (groupid.equals("1")) {
                    id = userid;
                } else {
                    id = pcid;
                }
                String tags = TagHandler.getTagsForTabPannel(conn, rsforJson.getString(1), id);
                if (groupid.equals("1") && !userid.equals(rsforJson.getString(9))) {
                    if (tags.length() > 0) {
                        tags += ",Shared/" + udao.getUser(conn,rsforJson.getString(9)).getUserName();
                    } else {
                        tags = "Shared/" + udao.getUser(conn,rsforJson.getString(9)).getUserName();
                    }
                }
                if (tags.length() == 0) {
                    tags += "Uncategorized";
                }
                int flag = 0;
                if (userid.equals(rsforJson.getString(9))) {
                    flag = 1;
                }
                String companyid = CompanyHandler.getCompanyByUser(conn, userid);
                String theTime = Timezone.toCompanyTimezone(conn, rsforJson.getObject(5).toString(), companyid);
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                java.util.Date d = new java.util.Date();
//                theTime = Timezone.dateTimeRenderer(conn, Timezone.toUserTimezone(conn, sdf.format(d), userid), theTime, userid);
                jobj = new JSONObject();
                jobj.put("Id", rsforJson.getString(1));
                jobj.put("Name", rsforJson.getString(2));
                jobj.put("Size", getSizeKb(rsforJson.getString(3)));
                jobj.put("Type", rsforJson.getString(4));
                jobj.put("DateModified", theTime);
                jobj.put("Permission", rsforJson.getObject(6));
                jobj.put("Status", rsforJson.getObject(7));
                jobj.put("version", rsforJson.getObject(8));
                jobj.put("Author", getAuthor(rsforJson.getString(1), 0));
                jobj.put("Tags", tags);
                jobj.put("Owner", flag);
                jobj.put("storeindex", rsforJson.getObject(10));
                jobj.put("docprojper", rsforJson.getString("docprojper"));
                try {
                    jobj.put("readwrite", rsforJson.getObject(11));
                } catch (Exception e) {
                    jobj.put("readwrite", "0");
                }
                gridString += jobj.toString();
                gridString += ",";
            }

        } catch (JSONException e) {
            throw ServiceException.FAILURE("FileHandler.getJString", e);
        }
        // return jobj.toString();
        return gridString;
    }

    public static String getAuthor(String docid, int flag)
            throws ServiceException {
        String author = "";
        if (flag == 1) {
            if (docid.contains(".")) {
                docid = docid.substring(0, docid.lastIndexOf("."));
            }
        }
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        com.krawler.database.DbPool.Connection conn = null;
        try {
            conn = DbPool.getConnection();
            pstmt = conn.prepareStatement("select userid from docs where docid = ?");
            pstmt.setString(1, docid);
            rs = pstmt.executeQuery();
            rs.next();
            author = getAuthor(rs.getString(1));
        } catch (SQLException e) {
            throw ServiceException.FAILURE("FileHandler.getAuthor", e);
        } finally {
            DbPool.quietClose(conn);
        }
        return author;
    }

    public static String getAuthor(String userid) throws ServiceException {
        String fname = "";
        String lname = "";
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        com.krawler.database.DbPool.Connection conn = null;

        try {
            conn = DbPool.getConnection();
            pstmt = conn.prepareStatement("select fname,lname from users where userid = ?");
            pstmt.setString(1, userid);
            rs = pstmt.executeQuery();
            rs.next();
            if (rs.getString(1) != null) {
                fname = rs.getString(1);
            }
            if (rs.getString(2) != null) {
                lname = rs.getString(2);
            }
            //?
//			DbPool.quietClose(conn);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("FileHandler.getAuthor", e);
        } finally {
            DbPool.quietClose(conn);
        }
        return (fname + " " + lname).trim();
    }

//    public static String getUserName(String userid) throws ServiceException {
//        String username = "";
//        ResultSet rs = null;
//        PreparedStatement pstmt = null;
//        com.krawler.database.DbPool.Connection conn = null;
//
//        try {
//            conn = DbPool.getConnection();
//            pstmt = conn.prepareStatement("select username from userlogin where userid = ?");
//            pstmt.setString(1, userid);
//            rs = pstmt.executeQuery();
//            rs.next();
//            if (rs.getString(1) != null) {
//                username = rs.getString(1);
//            }
//        } catch (SQLException e) {
//            throw ServiceException.FAILURE("FileHandler.getAuthor", e);
//        } finally {
//            DbPool.quietClose(conn);
//        }
//        return username;
//    }

    public static String getUserid(String userName) throws ServiceException {
        String userid = "";
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        com.krawler.database.DbPool.Connection conn = null;

        try {
            conn = DbPool.getConnection();
            pstmt = conn.prepareStatement("select userid from userlogin where username = ?");
            pstmt.setString(1, userName);
            rs = pstmt.executeQuery();
            rs.next();
            if (rs.getString(1) != null) {
                userid = rs.getString(1);
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("FileHandler.getAuthor", e);
        } finally {
            DbPool.quietClose(conn);
        }
        return userid;
    }

    public static String deleteDoc(Connection conn, String docid, String userid,
            int flag, String companyid) throws ServiceException {
        String docname = "";
        if (flag == 1) {
//            LogHandler.makeLogentry(conn, docid, userid, 69, 64, 58, docname, null, companyid);
            DbUtil.executeUpdate(conn, "Delete from docs WHERE docid=?", docid);
            DbUtil.executeUpdate(conn, "Delete from projectspacificdocs WHERE docid=?", docid);
        } else {
            DbUtil.executeUpdate(conn,
                    "Delete from docprerel WHERE docid = ? and userid = ?",
                    new Object[]{docid, userid});
            DbUtil.executeUpdate(conn,
                    "Delete from doctagrel where docid = ? and userid = ?",
                    new Object[]{docid, userid});
        }
        return docname;
    }

    public static void deleteProjectSpecificDoc(Connection conn, String projectid) throws ServiceException {
        ArrayList<String> ll = new ArrayList<String>();
        DbResults rs = DbUtil.executeQuery(conn, "SELECT * FROM projectspacificdocs WHERE projectid = ?", projectid);
        while (rs.next()) {
            String docid = rs.getString("docid");
            ll.add(docid);
        }
        while (ll.iterator().hasNext()) {
            String docid = ll.iterator().next();
            FilePermission.Delete(conn, docid);
            deleteDoc(conn, docid, "", 1, "");
            ll.remove(docid);
        }
    }
    /*public static void deleteDoc(Connection conn, String docid, String userid,
    int flag,String companyid) throws ServiceException {
    if (flag == 1) {
    String docname=FileHandler.getDocName(conn, docid);
    LogHandler.makeLogentry(conn, docid, userid, 69, 64, 58, docname, null,companyid);
    int a = DbUtil.executeUpdate(conn,
    "Delete from docs WHERE docid=?", docid);
    } else {
    int a = DbUtil.executeUpdate(conn,
    "Delete from docprerel WHERE docid = ? and userid = ?",
    new Object[] { docid, userid });
    if(a==0){
    DbUtil.executeUpdate(conn,
    "insert into deldocs (docid,userid) values (?,?)",
    new Object[]{docid,userid});
    }
    int b = DbUtil.executeUpdate(conn,
    "Delete from doctagrel where docid = ? and userid = ?",
    new Object[] { docid, userid });
    }
    }*/

    public static void fildoc(Connection conn, String docid, String docname,
            String docsize, String docdatemod, String permission,
            String status, String docrevision, String pcid, String groupid,
            String userid, String type, String comment, String tags,
            String svnName, String projectid) throws ServiceException {

        DbResults rs = null;
        DbResults rs1 = null;
        // String pcid = "1";
        String sql = null;
        String sql1 = null;
        String sql2 = null;
        java.util.Date docdt = null;
        String currentStoreIndex = StorageHandler.GetCurrentStorageIndex();
        if (comment.length() > 80) {
            comment = comment.substring(0, 79);
        }
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            docdt = sdf.parse(docdatemod);

            if (groupid.equals("2")) {
                sql = "select count(*) from docs ds,docprerel dp where ds.docname =? and " + "ds.userid =? and ds.groupid =? and ds.docid =dp.docid and  " + "dp.userid=? and dp.permission='5'";

            } else if (groupid.equals("3")) {
                sql = "select count(*) from docs ds,docprerel dp where ds.docname =? and " + "ds.userid =? and ds.groupid =? and ds.docid =dp.docid and  " + "dp.userid=? and dp.permission='6'";

            } else if (groupid.equals("1")) {
                sql = "select count(*) from docs where  docname = ? and userid =? and groupid =?";
            }

            if (groupid.equals("2") || groupid.equals("3")) {
                rs = DbUtil.executeQuery(conn, sql, new Object[]{docname,
                            userid, groupid, pcid});
            } else {
                rs = DbUtil.executeQuery(conn, sql, new Object[]{docname,
                            userid, groupid});
            }
            rs.next();
            if (rs.getInt(1) == 0) {

                sql = "INSERT INTO docs(docid, docname, docsize,docdatemod,userid,docper,docstatus,docrevision,groupid,doctype,comments,version,svnname,storageindex) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                DbUtil.executeUpdate(conn, sql, new Object[]{docid, docname,
                            docsize, docdt, userid, permission, status,
                            docrevision, groupid, type, comment, "Inactive",
                            svnName, currentStoreIndex});
                String id = "";
                if (!tags.equals("")) {
                    if (groupid.equals("1")) {
                        id = userid;
                    } else {
                        id = pcid;
                    }
                    TagHandler.addButtonTag(conn, id, docid, tags);
                }
                if (!projectid.equals("")) {
                    sql1 = "INSERT INTO projectspacificdocs(docid, projectid) VALUES (?,?)";
                    DbUtil.executeUpdate(conn, sql1, new Object[]{docid, projectid});
                    FilePermission.Insert(conn, docid, projectid, "9", 0);
                    FilePermission.updatePermission(conn, docid, "None", "Selected Projects");
                    if (groupid.equals("1")) {
                        id = userid;
                    } else {
                        id = pcid;
                    }
                    String projname = projdb.getProjectName(conn, projectid);
                    projname = TagUtils.checkTag(projname);
                    TagHandler.addButtonTag(conn, id, docid, projname);
                }
                if (groupid.endsWith("1")) {
                    pcid = "1";
                }

                if (groupid.equals("2")) {
                    DbUtil.executeUpdate(conn, "insert into docprerel (docid,userid,permission) values(?,?,?)", new Object[]{docid, pcid, "5"});
                }
                if (groupid.equals("3")) {
                    DbUtil.executeUpdate(conn, "insert into docprerel (docid,userid,permission) values(?,?,?)", new Object[]{docid, pcid, "6"});
                }

            } else {
                DbUtil.executeUpdate(conn, "UPDATE docs SET docrevision = ?,comments=?,docdatemod=?,docsize=? WHERE docid= ?;",
                        new Object[]{docrevision, comment, docdt, docsize, docid});
            }
            DbUtil.executeUpdate(conn, "INSERT INTO docsToConvertList VALUES(?)", docid);
        } catch (ParseException e) {
            throw ServiceException.FAILURE("FileHandler.fildoc", e);
        }

    }

    public static String getDocName(Connection conn, String docid)
            throws ServiceException {

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String docname = null;
        try {
            pstmt = conn.prepareStatement("select docname from docs where docid=?");
            pstmt.setString(1, docid);
            rs = pstmt.executeQuery();
            rs.next();
            docname = rs.getString(1);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("FileHandler.getDocType", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return docname;

    }

    public static String getRequestAuthor(Connection conn, String[] userid) throws ServiceException {
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        String tp = null;
        KWLJsonConverter KWL = new KWLJsonConverter();
        try {
            String para = new String();
            for (int i = 0; i < userid.length; i++) {
                para += "?";
                if ((i + 1) != userid.length) {
                    para += ",";
                }
            }
            pstmt = conn.prepareStatement("select fname, lname, users.userid, userlogin.username from users inner join userlogin on users.userid=userlogin.userid where users.userid in(" + para + ") ");
            for (int i = 0; i < (userid.length); i++) {
                pstmt.setString(i + 1, userid[i]);
            }
            //pstmt.setString(1, userid);
            rs = pstmt.executeQuery();
            tp = KWL.GetJsonForGrid(rs).toString();
            return tp;
        } catch (SQLException e) {
            throw ServiceException.FAILURE("FileHandler.getAuthor", e);
        } finally {
            DbPool.closeStatement(pstmt);
//			DbPool.quietClose(conn);
        }
    }

    public static String getSearchString(String searchJson, String sql, String companyid) {
        try {
            if (!StringUtil.equal(searchJson, new JSONObject().toString())) {
                JSONObject sj = new JSONObject(searchJson);
                JSONArray ja = sj.getJSONArray("root");
                for (int i = 0; i < ja.length(); i++) {
                    JSONObject jo = ja.getJSONObject(i);
                    String col = jo.getString("column");
                    String xtype = jo.getString("xtype");
                    if (xtype.equals("")) {
                        sql = sql.concat(" AND " + jo.getString("column") + " like '%" + jo.getString("searchText") + "%'");
                    } else if (xtype.equals("datefield")) {
                        String dt = jo.getString("searchText");
                        dt = dbcon.fromCompanyToSystem(companyid, dt);
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                        dt = sdf.format(sdf.parse(dt));
                        sql = sql.concat(" AND DATE(docdatemod) = \"" + dt + "\"");
                    } else if (xtype.equals("combo")) {
                        sql = sql.concat(" AND " + jo.getString("column") + "='" + jo.getString("combosearch") + "'");
                    }
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("FileHandler.getSearchString : " + ex.getMessage(), ex);
        } finally {
            return sql;
        }
    }

    public static void deleteConvertedDoc(String userid, String docid) throws ConfigurationException {
        String dest = StorageHandler.GetDocStorePath()
                .concat(StorageHandler.GetFileSeparator()).concat(userid)
                .concat(StorageHandler.GetFileSeparator()).concat(docid).concat(".swf");
        File f = new File(dest);
        if(f.exists()){
            f.delete();
        }
    }

    public static boolean isConversionComplete(Connection conn, String docID){
        boolean converted = false;
        try {
            DbResults rs = DbUtil.executeQuery(conn, "SELECT 1 FROM docsToConvertList WHERE docid = ?", docID);
            if(!rs.next()){
                converted = true;
            }
        } catch (ServiceException ex) {
            Logger.getLogger(FileHandler.class.getName()).log(Level.SEVERE, "Error while checking", ex);
        }
        return converted;
    }

    /**
     *
     * @param docID name of file
     * @param extention file extention with dot(.)
     * @return true if file exists in doc store path with given extention.
     */
    public static boolean isFileExists(String docID, String extention){
        boolean fileExists = false;
        try {
            String storePath = StorageHandler.GetDocStorePath();
            Hashtable ht = dbcon.getfileinfo(docID);
            String path = storePath.concat(StorageHandler.GetFileSeparator()).concat(ht.get("userid").toString()).concat(StorageHandler.GetFileSeparator()).concat(docID).concat(extention);
            File fp = new File(path);
            if(fp.isFile()){
                fileExists = true;
            }
        } catch (ConfigurationException ex) {
            Logger.getLogger(FileHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return fileExists;
        }
    }
}
