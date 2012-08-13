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

import com.krawler.common.service.ServiceException;
import com.krawler.database.DbPool;
import com.krawler.database.DbUtil;
import com.krawler.database.DbPool.Connection;
import com.krawler.utils.json.KWLJsonConverter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FilePermission {

    public static void updatePermission(Connection conn, String docid,
            String cper, String pper) throws ServiceException {
        DbUtil.executeUpdate(conn, "UPDATE docs SET docper = ?, docprojper = ? WHERE docid= ?;", new Object[]{ cper, pper, docid});
    }

    public static void updateStatus(Connection conn, String userid,
            String docid, String status) throws ServiceException {
        DbUtil.executeUpdate(conn, "UPDATE docs SET docstatus = ? WHERE docid= ?;", new Object[]{ status, docid});
    }

    public static void Delete(Connection conn, String docid)
            throws ServiceException {
        DbUtil.executeUpdate(conn, "Delete from docprerel WHERE docid=?;", docid);
    }

    public static void Insert(Connection conn, String docid, String userid,
            String type, int rw) throws ServiceException {
        DbUtil.executeUpdate(conn, "Insert into docprerel(docid,userid,permission,readwrite) values(?,?,?,?);", new Object[]{docid, userid, type, rw});

    }

    public static String getConnections(Connection conn, String userid,
            String docid) throws ServiceException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        KWLJsonConverter kjs = new KWLJsonConverter();
        String str = null;
        try {
            /*
             * pstmt = conn .prepareStatement("select users.userid as id,
             * username as name from users inner join userfriendmapping on
             * userfriendmapping.friendid = users.userid where
             * userfriendmapping.userid = ? " + /* String sqlstr = "select
             * docprerel.userid, docprerel.readwrite,users.username from
             * docprerel inner join users on users.userid = docprerel.userid
             * where docprerel.docid = ?" + " and docprerel.permission ='2' and
             * docprerel.userid in (select users.userid from users inner join
             * userrelations on userrelations.userid1 = users.userid where
             * userrelations.userid2 = ? and userrelations.relationid = '3'"+
             * "union select users.userid from users inner join userrelations on
             * userrelations.userid2 = users.userid where userrelations.userid1 = ?
             * and userrelations.relationid = '3')";
             */

            String sqlstr = "select users.userid as id,userlogin.username as name from users inner join userrelations on userrelations.userid2= users.userid " +
                "inner join userlogin on userlogin.userid = users.userid where userlogin.isactive = true " +
                "and userrelations.userid1 = ? and userrelations.relationid  = '3' and users.userid not in " +
                    "(select docprerel.userid from docprerel where docprerel.docid = ? and docprerel.permission ='2') " +
                    "union select users.userid as id ,userlogin.username as name from users inner join userrelations on userrelations.userid1 = users.userid " +
                    "inner join userlogin on userlogin.userid = users.userid where userlogin.isactive = true " +
                    "and userrelations.userid2= ? and userrelations.relationid  = '3' and users.userid not in " +
                        "(select docprerel.userid from docprerel where docprerel.docid = ? and docprerel.permission ='2')";

            pstmt = conn.prepareStatement(sqlstr);
            pstmt.setString(1, userid);
            pstmt.setString(2, docid);
            pstmt.setString(3, userid);
            pstmt.setString(4, docid);
            rs = pstmt.executeQuery();
            str = kjs.GetJsonForGrid(rs);

        } catch (SQLException e) {
            throw ServiceException.FAILURE("FilePermission.getConnections", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return str;
    }

    public static String getConnectionsRightGrid(Connection conn,
            String userid, String docid) throws ServiceException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        KWLJsonConverter kjs = new KWLJsonConverter();
        String str = null;
        try {
            /*
             * pstmt = conn .prepareStatement("select users.userid as id,
             * username as name from users inner join userfriendmapping on
             * userfriendmapping.friendid = users.userid where
             * userfriendmapping.userid = ? " + "union select users.userid as
             * id, username as name from users inner join userfriendmapping on
             * userfriendmapping.userid = users.userid where
             * userfriendmapping.friendid = ?");
             */

            /*
             * String sqlstr = "select users.userid as id,username as name from
             * users inner join userrelations on userrelations.userid2=
             * users.userid where userrelations.userid1 = ?" + " and
             * userrelations.relationid = '3' and users.userid in (select
             * docprerel.userid from docprerel where docprerel.docid = ? and
             * docprerel.permission ='2')" + " union select users.userid as id
             * ,username as name from users inner join userrelations on
             * userrelations.userid1 = users.userid where userrelations.userid2= ?
             * and userrelations.relationid = '3' and users.userid in" + "
             * (select docprerel.userid from docprerel where docprerel.docid = ?
             * and docprerel.permission ='2')";
             */
            String companyid = CompanyHandler.getCompanyByUser(conn, userid);
            String sqlstr = "select docprerel.userid as id, docprerel.readwrite as perm ,username as name from docprerel inner join userlogin on userlogin.userid = docprerel.userid " +
                    "where docprerel.docid = ? and docprerel.permission ='2' and docprerel.userid not in (select userlogin.userid from userlogin inner join users on userlogin.userid = users.userid where userlogin.isactive = false and users.companyid = ?) and docprerel.userid in " +
                        "(select users.userid from users inner join userrelations on userrelations.userid1 = users.userid where userrelations.userid2 = ? and userrelations.relationid = '3'" +
                        "union select users.userid  from users inner join userrelations on userrelations.userid2 = users.userid where userrelations.userid1 = ? and userrelations.relationid = '3')";

            pstmt = conn.prepareStatement(sqlstr);

            pstmt.setString(1, docid);
            pstmt.setString(2, docid);
            pstmt.setString(3, userid);
            pstmt.setString(4, userid);
            rs = pstmt.executeQuery();
            str = kjs.GetJsonForGrid(rs);

        } catch (SQLException e) {
            throw ServiceException.FAILURE("FilePermission.getConnections", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return str;
    }

    public static String getDocumentOwner(Connection conn, String docid){
        String o = "";
        try{
            PreparedStatement p = conn.prepareStatement("SELECT userid FROM docs WHERE docid = ?");
            p.setString(1, docid);
            ResultSet r = p.executeQuery();
            if(r.next()){
                o = r.getString("userid");
            }
        } catch (ServiceException ex) {
            Logger.getLogger(FilePermission.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException e){
            
        }
        return o;
    }
    public static String getProjects(Connection conn, String userid, String docid) throws ServiceException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        KWLJsonConverter kjs = new KWLJsonConverter();
        String str = null;
        try {
            String sqlstr = "SELECT project.projectid AS id, projectname AS name " +
                    "FROM projectmembers INNER JOIN project ON project.projectid = projectmembers.projectid " +
                    "WHERE projectmembers.status in (3,4,5) AND projectmembers.userid = ? AND archived = 0 AND project.projectid NOT IN " +
                    "(SELECT userid FROM docprerel WHERE docid = ? AND permission = '9');";
            pstmt = conn.prepareStatement(sqlstr);
            pstmt.setString(1, userid);
            pstmt.setString(2, docid);
            rs = pstmt.executeQuery();
            str = kjs.GetJsonForGrid(rs);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("FilePermission.getConnections", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return str;
    }

    public static String getProjectsRightGrid(Connection conn, String userid, String docid) throws ServiceException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        KWLJsonConverter kjs = new KWLJsonConverter();
        String str = null;
        try {
            String sqlstr = "SELECT projectid AS id, projectname AS name FROM project INNER JOIN docprerel ON project.projectid = docprerel.userid WHERE docid = ? AND permission = '9';";
            pstmt = conn.prepareStatement(sqlstr);
            pstmt.setString(1, docid);
            rs = pstmt.executeQuery();
            str = kjs.GetJsonForGrid(rs);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("FilePermission.getConnections", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return str;
    }
}
