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

import com.krawler.common.locale.MessageSourceProxy;
import com.krawler.utils.json.base.JSONException;
import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.KWLErrorMsgs;
import com.krawler.database.DbPool;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.database.DbPool.Connection;
import com.krawler.utils.json.KWLJsonConverter;
import com.krawler.esp.portalmsg.Mail;
import com.krawler.esp.user.UserDAO;
import com.krawler.esp.user.UserDAOImpl;
import com.krawler.esp.utils.ConfigReader;
import com.krawler.svnwebclient.configuration.ConfigurationException;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;

public class ProfileHandler {

    private static String apistr = "remoteapi.jsp";

    public static String getUserRelation(Connection conn, String userid1,
            String userid2) throws ServiceException {
        PreparedStatement pstmt = null;
        if (userid1.compareTo(userid2) == 0) {
            // out.println("{\"data\":[{\"id1\":\""+userid1+"\",\"connstatus\":\"-1\"}]}");
            return "{\"data\":[{\"id1\":\"" + userid1
                    + "\",\"connstatus\":\"-1\"}]}";
        } else {
            ResultSet rs = null;
            try {

                pstmt = conn.prepareStatement("select userrelations.userid1 as id1,userrelations.relationid as connstatus from userrelations "
                        + "inner join userlogin on userrelations.userid2 = userlogin.userid where userlogin.isactive = 1 and "
                        + "userrelations.userid1 = ? and userrelations.userid2 = ? ;");
                pstmt.setString(1, userid1);
                pstmt.setString(2, userid2);
                rs = pstmt.executeQuery();
                if (rs.isBeforeFirst()) {
                    KWLJsonConverter KWL1 = new KWLJsonConverter();

                    String splitstring2 = KWL1.GetJsonForGrid(rs).toString();
                    return " " + splitstring2;
                    // out.println(" " + splitstring2);
                } else {
                    pstmt = conn.prepareStatement("select userrelations.userid1 as id1,userrelations.relationid as connstatus from userrelations "
                            + "inner join userlogin on userrelations.userid1 = userlogin.userid where userlogin.isactive = 1 and "
                            + "userrelations.userid1 = ? and userrelations.userid2 = ? ;");

                    pstmt.setString(1, userid2);
                    pstmt.setString(2, userid1);

                    rs = pstmt.executeQuery();

                    KWLJsonConverter KWL = new KWLJsonConverter();

                    String splitstring = KWL.GetJsonForGrid(rs).toString();
                    rs.close();
                    return " " + splitstring;
                    // out.println(" " + splitstring);

                }

                // rs1.close();

            } catch (SQLException e) {
                throw ServiceException.FAILURE("ProfileHandler.getProjectList", e);
            } finally {
                DbPool.closeStatement(pstmt);
            }

        }
    }

    public static String getUserDetails(Connection conn, String ulogin)
            throws ServiceException {

        ResultSet rs1 = null;
        String tp = null;
        PreparedStatement pstmt1 = null;
        try {
            pstmt1 = conn.prepareStatement("select users.userid, userlogin.username, aboutuser as about, emailid as startdate, address as createdon, designation, contactno as nickname from users inner join userlogin on users.userid = userlogin.userid where users.userid = ?");
            pstmt1.setString(1, ulogin);
            rs1 = pstmt1.executeQuery();
            KWLJsonConverter KWL1 = new KWLJsonConverter();
            tp = KWL1.GetJsonForGrid(rs1).toString();
            // out.println(" "+tp);
            rs1.close();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("ProfileHandler.getProjectList", e);
        } finally {
            DbPool.closeStatement(pstmt1);
        }
        return tp;
    }

    public static String getNewContacts(Connection conn, String ulogin, String companyid,
            int pagesize, int offset, String ss) throws ServiceException {
        ResultSet rs = null;
        String splitstring2 = null;
        PreparedStatement pstmt = null;
        String[] searchStrObj = new String[]{"users.fname", "users.lname", "concat(users.fname,' ',users.lname)"};
        String myLikeString = StringUtil.getMySearchString(ss, "and", searchStrObj);
        try {

            String GET_COUNT = " Select count(DISTINCT(users.userid)) as count from users inner join userlogin on users.userid=userlogin.userid"
                    + " where users.companyid = ? and users.userid not in "
                    + "( Select users.userid from userrelations inner join users on users.userid = userrelations.userid2 or users.userid = userrelations.userid1"
                    + " where userid1 = ? or userid2 = ? ) and users.userid != ? and userlogin.isactive = 1";

            String GET_USERS = " Select users.userid as id, CONCAT(users.fname,' ',users.lname) as username,users.emailid as emailid,users.address as address,users.contactno as contactno,users.image as image from users inner join userlogin on users.userid=userlogin.userid"
                    + " where users.companyid = ? and users.userid not in"
                    + " ( Select users.userid from userrelations inner join users on users.userid = userrelations.userid2 or users.userid = userrelations.userid1"
                    + " where userid1 = ? or userid2 = ? ) and users.userid != ? and userlogin.isactive = 1";

            pstmt = conn.prepareStatement(GET_COUNT + myLikeString);
            pstmt.setString(1, companyid);
            pstmt.setString(2, ulogin);
            pstmt.setString(3, ulogin);
            pstmt.setString(4, ulogin);
            StringUtil.insertParamSearchString(5, pstmt, ss, searchStrObj.length);
            rs = pstmt.executeQuery();
            rs.next();
            int count1 = rs.getInt("count");
            KWLJsonConverter KWL = new KWLJsonConverter();

            pstmt = conn.prepareStatement(GET_USERS + myLikeString + " LIMIT ? OFFSET ? ");
            pstmt.setString(1, companyid);
            pstmt.setString(2, ulogin);
            pstmt.setString(3, ulogin);
            pstmt.setString(4, ulogin);
            int paramCnt = StringUtil.insertParamSearchString(5, pstmt, ss, searchStrObj.length);
            pstmt.setInt(paramCnt++, pagesize);
            pstmt.setInt(paramCnt++, offset);

            rs = pstmt.executeQuery();

            splitstring2 = KWL.GetJsonForGrid(rs);

            splitstring2 = splitstring2.substring(0, splitstring2.length() - 1);
            splitstring2 += ",\"count\":[" + count1 + "]}";
            if (count1 > 0) {
                splitstring2 = getAppsImageInJSON(splitstring2, "id", "image", 100);
            }
            rs.close();
        } catch (JSONException e) {
            throw ServiceException.FAILURE("ProfileHandler.getNewContacts", e);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("ProfileHandler.getProjectList", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return splitstring2;
    }

    public static String getFriendList(Connection conn, String ulogin,
            int pagesize, int offset, String ss) throws ServiceException {
        ResultSet rs = null;
        String splitstring2 = null;
        PreparedStatement pstmt = null;
        String[] searchStrObj = new String[]{"fname", "lname", "concat(fname,' ',lname)"};
        String subquery = StringUtil.getMySearchString(ss, "and", searchStrObj);
        String subquery1 = " WHERE userid1=? ";
        String subquery2 = " WHERE userid2=? "; 
        if (!StringUtil.isNullOrEmpty(ss)) {
            subquery1 = " inner join users on users.userid = userrelations.userid2 WHERE userid1=? " + subquery;
            subquery2 = " inner join users on users.userid = userrelations.userid1 WHERE userid2=? " + subquery;
        }
        try {
            pstmt = conn.prepareStatement("SELECT count(id) as count FROM "
                    + "(SELECT users.userid AS id FROM users "
                    + "INNER JOIN userrelations ON userrelations.userid1=users.userid "
                    + "INNER JOIN userlogin ON userlogin.userid = users.userid "
                    + "WHERE userlogin.isactive = true and userrelations.userid2=? " + subquery + " "
                    + "UNION SELECT users.userid AS id FROM users "
                    + "INNER JOIN userrelations ON userrelations.userid2=users.userid "
                    + "INNER JOIN userlogin ON userlogin.userid = users.userid "
                    + "WHERE userlogin.isactive = true and userrelations.userid1=? " + subquery + ") AS t");
            pstmt.setString(1, ulogin);
            int paramCnt = StringUtil.insertParamSearchString(2, pstmt, ss, searchStrObj.length);
            pstmt.setString(paramCnt++, ulogin);
            paramCnt = StringUtil.insertParamSearchString(paramCnt, pstmt, ss, searchStrObj.length);
            rs = pstmt.executeQuery();
            rs.next();
            int count1 = rs.getInt("count");
            KWLJsonConverter KWL = new KWLJsonConverter();

            pstmt = conn.prepareStatement("SELECT users.userid AS id, CONCAT(users.fname,' ',users.lname) AS name, "
                    + "image AS img, users.emailid AS email, users.address, users.contactno AS phone, users.aboutuser AS description, "
                    + "userrelations.relationid AS status, 'u' as o FROM users "
                    + "INNER JOIN userrelations ON userrelations.userid1=users.userid "
                    + "INNER JOIN userlogin ON userlogin.userid = users.userid "
                    + "WHERE userlogin.isactive = true and userrelations.userid2=? " + subquery + " "
                    + "UNION SELECT users.userid AS id,CONCAT(users.fname,' ',users.lname) AS name, "
                    + "image AS img, users.emailid AS email, users.address, users.contactno AS phone, users.aboutuser AS description, "
                    + "userrelations.relationid AS status, 'i' as o FROM users "
                    + "INNER JOIN userrelations ON userrelations.userid2=users.userid "
                    + "INNER JOIN userlogin ON userlogin.userid = users.userid "
                    + "WHERE userlogin.isactive = true and userrelations.userid1=? " + subquery + " ORDER BY name LIMIT ? OFFSET ?;");
            pstmt.setString(1, ulogin);
            paramCnt = StringUtil.insertParamSearchString(2, pstmt, ss, searchStrObj.length);
            pstmt.setString(paramCnt++, ulogin);
            paramCnt = StringUtil.insertParamSearchString(paramCnt, pstmt, ss, searchStrObj.length);
            pstmt.setInt(paramCnt++, pagesize);
            pstmt.setInt(paramCnt++, offset);

            rs = pstmt.executeQuery();

            splitstring2 = KWL.GetJsonForGrid(rs);

            splitstring2 = splitstring2.substring(0, splitstring2.length() - 1);
            splitstring2 += ",\"count\":[" + count1 + "]}";
            splitstring2 = getAppsImageInJSON(splitstring2, "id", "img", 100);
            rs.close();
        } catch (JSONException e) {
            throw ServiceException.FAILURE("ProfileHandler.getFriendList", e);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("ProfileHandler.getProjectList", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return splitstring2;
    }

    public static String addContacts(Connection conn, String userid, String requestTo) throws ServiceException, JSONException {
        String str = null;
        int numRows = 0;
        boolean duplicateEntry = false;
        String query = null;
        String sql = "select count(*) as count from userrelations where (userid1 = ? and userid2 = ?) or (userid2 = ? and userid1 = ?)";
        try {
            String[] requestuserid = requestTo.split(",");
            query = "INSERT INTO userrelations(userid1,userid2,relationid) VALUES (?,?,?)";
            for (int i = 0; i < requestuserid.length; i++) {
                DbResults rs2 = DbUtil.executeQuery(conn, sql, new Object[]{userid, requestuserid[i], userid, requestuserid[i]});
                if (rs2.next()) {
                    if (rs2.getInt("count") == 0) {
                        numRows = DbUtil.executeUpdate(conn, query, new Object[]{userid, requestuserid[i], 1});
                    } else {
                        duplicateEntry = true;
                    }
                }
            }
            if (numRows > 0) {
                str = KWLErrorMsgs.rsSuccessErrTrue;
            } else {
                if (duplicateEntry) {
                    str = KWLErrorMsgs.rsSuccessDuplicate;
                } else {
                    str = KWLErrorMsgs.rsSuccessErrFalse;
                }
            }
        } catch (ServiceException ex) {
        } finally {
        }
        return str;
    }

    public static String newAddress(Connection conn, String contactid, String userid, String name, String emailid, String address, String phone) throws ServiceException, SQLException {
        String str = null;
        try {
            str = KWLErrorMsgs.rsSuccessErrFalse;
            int numRows = DbUtil.executeUpdate(conn, "insert into addressbook(contactid,name,emailid,phone,address,userid)" + " values ( ?, ?, ?, ?, ? ,?);",
                    new Object[]{contactid, name, emailid, phone, address, userid});
            if (numRows > 0) {
                str = KWLErrorMsgs.rsSuccessErrTrue;
            }
        } catch (ServiceException ex) {
        } finally {
        }
        return str;
    }

    public static String getMyContacts(Connection conn, String userid, int limit, int start, String ss) throws ServiceException, JSONException {

        String[] userSearchStrObj = new String[]{"users.fname", "users.lname", "concat(users.fname,' ',users.lname)"};
        String myLikeStringUsers = StringUtil.getMySearchString(ss, "and", userSearchStrObj);
        String[] addSearchStrObj = new String[]{"addressbook.name"};
        String myLikeStringAddressbook = StringUtil.getMySearchString(ss, "and", addSearchStrObj);
        com.krawler.utils.json.base.JSONObject jtemp = new com.krawler.utils.json.base.JSONObject();
        com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject();
        String str = null;
        int count = 0;
        String statusStr = null;
        try {
            String GET_COUNT_USERS = " Select count(users.userid) as count from userrelations inner join users on "
                    + " users.userid = userrelations.userid2 or users.userid = userrelations.userid1 "
                    + " INNER JOIN userlogin ON userlogin.userid = users.userid WHERE userlogin.isactive = true and"
                    + " users.userid!=? and (userid1 = ? or userid2 = ?) " + myLikeStringUsers;

            String GET_COUNT_ADDRESS = " select count(name) as count from addressbook where userid = ? " + myLikeStringAddressbook;

            String GET_USERS = " Select concat(users.fname,' ',users.lname) as username,users.userid as userid,users.emailid as emailid,users.address as address,users.contactno as contactno,userrelations.relationid, users.image as image "
                    + " from userrelations inner join users on users.userid = userrelations.userid2 or users.userid = userrelations.userid1"
                    + " INNER JOIN userlogin ON userlogin.userid = users.userid WHERE userlogin.isactive = true and "
                    + " users.userid!=? and (userid1 = ? or userid2 = ?) " + myLikeStringUsers
                    + " union select addressbook.name as username ,addressbook.contactid as userid,addressbook.emailid as emailid,addressbook.address as address,addressbook.phone as contactno, -999 as relationid, '' as image  "
                    + " from addressbook where userid = ? " + myLikeStringAddressbook + " order by username asc limit ? offset ? ";

            String GET_USER_RELATION_COUNT = "select count(*) as count from userrelations where userid1 = ? and userid2 = ?";

            ArrayList al = new ArrayList();
            al.add(userid);
            al.add(userid);
            al.add(userid);
            StringUtil.insertParamSearchString(al, ss, userSearchStrObj.length);
            DbResults rs1 = DbUtil.executeQuery(conn, GET_COUNT_USERS, al.toArray());
            jtemp = new com.krawler.utils.json.base.JSONObject();
            while (rs1.next()) {
                count = rs1.getInt("count");
            }
            al = new ArrayList();
            al.add(userid);
            StringUtil.insertParamSearchString(al, ss, addSearchStrObj.length);
            rs1 = DbUtil.executeQuery(conn, GET_COUNT_ADDRESS, al.toArray());
            while (rs1.next()) {
                count = count + rs1.getInt("count");
            }
            al = new ArrayList();
            al.add(userid);
            al.add(userid);
            al.add(userid);
            StringUtil.insertParamSearchString(al, ss, userSearchStrObj.length);
            al.add(userid);
            StringUtil.insertParamSearchString(al, ss, addSearchStrObj.length);
            al.add(limit);
            al.add(start);
            DbResults rs = DbUtil.executeQuery(conn, GET_USERS, al.toArray());
            while (rs.next()) {
                jtemp = new com.krawler.utils.json.base.JSONObject();
                int status = rs.getInt("relationid");
                jtemp.put("username", rs.getObject("username"));
                jtemp.put("userid", rs.getObject("userid"));
                jtemp.put("emailid", rs.getObject("emailid"));
                jtemp.put("address", rs.getObject("address"));
                jtemp.put("contactno", rs.getObject("contactno"));
                String img = StringUtil.getAppsImagePath(rs.getString("userid"), 100);
                jtemp.put("image", img);

                DbResults rs2 = DbUtil.executeQuery(conn, GET_USER_RELATION_COUNT, new Object[]{userid, rs.getObject("userid")});
                if (rs2.next()) {
                    if (rs2.getInt("count") == 0) {
                        if (status == 1) {
                            statusStr = "Invited";
                        } else {
                            statusStr = "Added";
                        }
                    } else {
                        if (status == 1) {
                            statusStr = "Pending Request";
                        } else {
                            statusStr = "Added";
                        }
                    }
                } else {
                    statusStr = "Added";
                }

                jtemp.put("status", statusStr);
                jtemp.put("statusid", rs.getInt("relationid"));
                jobj.append("data", jtemp);
            }
            if (jobj.isNull("data")) {
                return "{data:[]}";
            }
            jobj.put("count", count);
            str = jobj.toString();
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("admin.getMyContacts", ex);
        } catch (SQLException ex) {
            throw ServiceException.FAILURE("admin.getMyContacts", ex);
        }
        return str;
    }

    public static String getAllContacts(Connection conn, String userid) throws ServiceException, JSONException {

        com.krawler.utils.json.base.JSONObject jtemp = new com.krawler.utils.json.base.JSONObject();
        com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject();
        String str = null;
        try {

            String GET_USERS = "select addressbook.name as username ,addressbook.contactid as userid,addressbook.emailid as emailid,addressbook.address as address,addressbook.phone as contactno"
                    + " from addressbook where userid = ? ";
            ArrayList al = new ArrayList();

            jtemp = new com.krawler.utils.json.base.JSONObject();

            al.add(userid);

            DbResults rs = DbUtil.executeQuery(conn, GET_USERS, al.toArray());
            while (rs.next()) {
                jtemp = new com.krawler.utils.json.base.JSONObject();
                jtemp.put("cusername", rs.getObject("username"));
                jtemp.put("cuserid", rs.getObject("userid"));
                jtemp.put("cemailid", rs.getObject("emailid"));
                jtemp.put("caddress", rs.getObject("address"));
                jtemp.put("ccontactno", rs.getObject("contactno"));
                jobj.append("data", jtemp);
            }
            if (jobj.isNull("data")) {
                return "{data:[]}";
            }
            str = jobj.toString();
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("admin.getMyContacts", ex);
        }
        return str;
    }

    public static String repContact(Connection conn, String userMap) throws ServiceException, JSONException {
        String str = "";
        int nrows = 0, num;

        com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject(userMap);
        com.krawler.utils.json.base.JSONArray jarr = jobj.getJSONArray("userdata");
        try {
            str = KWLErrorMsgs.rsSuccessErrFalse;
            for (int ctr = 0; ctr < jarr.length(); ctr++) {
                String user = (String) jarr.getJSONObject(ctr).get("user");
                String email = (String) jarr.getJSONObject(ctr).get("email");
                String userid = (String) jarr.getJSONObject(ctr).get("userid");
                String username = (String) jarr.getJSONObject(ctr).get("username");
                String emailid = (String) jarr.getJSONObject(ctr).get("emailid");
                String address = (String) jarr.getJSONObject(ctr).get("address");
                String contactno = (String) jarr.getJSONObject(ctr).get("contactno");
                String query = "update addressbook set name = ? ,emailid=?, phone=? ,address=? where userid =? and name=? and emailid=?";
                num = DbUtil.executeUpdate(conn, query, new Object[]{username, emailid, contactno, address, userid, user, email});

                if (num > 0) {
                    nrows++;
                }

                conn.commit();
            }
        } catch (ServiceException e) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        if (nrows > 0) {
            str = KWLErrorMsgs.rsSuccessErrTrue;
        }
        return str;
    }

    public static void updateUserStatus(String userid, String loginstatus) throws ServiceException {
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            String query = "update users set userstatus = ? where userid =?";
            DbUtil.executeUpdate(conn, query, new Object[]{loginstatus, userid});
            conn.commit();
        } catch (ServiceException e) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
    }

    public static String acceptContacts(Connection conn, String userid, String requestTo) throws ServiceException, JSONException {
        String str = null;
        int numRows = 0;
        try {
            String query = null;
            String[] requestuserid = requestTo.split(",");
            query = "UPDATE userrelations set relationid = ? where userid1 = ? and userid2 = ?";
            for (int i = 0; i < requestuserid.length; i++) {
                numRows = DbUtil.executeUpdate(conn, query, new Object[]{3, requestuserid[i], userid});
            }

            if (numRows > 0) {
                str = KWLErrorMsgs.rsSuccessErrTrue;
            } else {
                str = KWLErrorMsgs.rsSuccessErrFalse;
            }
        } catch (ServiceException ex) {
        } finally {
        }
        return str;
    }

    public static String deleteContacts(Connection conn, String userid, String requestTo) throws ServiceException, JSONException {
        String str = null;
        int numRows = 0;
        try {
            String query = null;
            String[] requestuserid = requestTo.split(",");
            query = "DELETE from userrelations where (userid1 = ? and userid2 = ? ) or (userid2 = ? and userid1 =?)";
            for (int i = 0; i < requestuserid.length; i++) {
                numRows = DbUtil.executeUpdate(conn, query, new Object[]{userid, requestuserid[i], userid, requestuserid[i]});
            }
            if (numRows > 0) {
                str = KWLErrorMsgs.rsSuccessErrTrue;
            } else {
                str = KWLErrorMsgs.rsSuccessErrFalse;
            }
        } catch (ServiceException ex) {
        } finally {
        }
        return str;
    }
    //get 3,4,5

    public static String getUserCommunitiesMember(Connection conn, String ulogin,
            int pagesize, int offset) throws ServiceException {
        String splitstring2 = null;
        ResultSet rs = null;
        ResultSet rs1 = null;

        PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) AS count FROM communitymembers WHERE userid=? and status in ( 3, 4, 5);");
        try {
            pstmt.setString(1, ulogin);
            rs = pstmt.executeQuery();
            rs.next();
            int count1 = rs.getInt("count");
            KWLJsonConverter KWL = new KWLJsonConverter();

            PreparedStatement pstmt1 = conn.prepareStatement("SELECT t1.communityid AS id, t1.communityname AS name, t1.aboutcommunity AS description,t1.status, COUNT(*) AS members,t1.createdon,image as img  FROM (select community.*, communitymembers.status FROM communitymembers INNER JOIN community ON communitymembers.communityid=community.communityid WHERE userid=? and communitymembers.status in ( 3, 4, 5)) AS t1 INNER JOIN communitymembers ON communitymembers.communityid=t1.communityid GROUP BY id ORDER BY members DESC LIMIT ? OFFSET ?;");
            pstmt1.setString(1, ulogin);
            pstmt1.setInt(2, pagesize);
            pstmt1.setInt(3, offset);
            rs1 = pstmt1.executeQuery();
            splitstring2 = KWL.GetJsonForGrid(rs1);
            splitstring2 = splitstring2.substring(0, splitstring2.length() - 1);
            splitstring2 += ",\"count\":[" + count1 + "]}";

            rs.close();
            rs1.close();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("ProfileHandler.getProjectList", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return splitstring2;
    }

    public static String getUserCommunities(Connection conn, String ulogin,
            int pagesize, int offset) throws ServiceException {
        String splitstring2 = null;
        ResultSet rs = null;
        ResultSet rs1 = null;

        PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) AS count FROM communitymembers WHERE userid=?;");
        try {
            pstmt.setString(1, ulogin);
            /*
             * pstmt.setInt(2, 3); // 3 = Member pstmt.setInt(3, 4); // 4 =
             * Owner pstmt.setInt(4, 5); // 5 = Moderator
             */

            rs = pstmt.executeQuery();
            rs.next();
            int count1 = rs.getInt("count");
            KWLJsonConverter KWL = new KWLJsonConverter();

            PreparedStatement pstmt1 = conn.prepareStatement("SELECT t1.communityid AS id, t1.communityname AS name, t1.aboutcommunity AS description,t1.status, COUNT(*) AS members,t1.createdon,image as img  FROM (select community.*, communitymembers.status FROM communitymembers INNER JOIN community ON communitymembers.communityid=community.communityid WHERE userid=?) AS t1 INNER JOIN communitymembers ON communitymembers.communityid=t1.communityid GROUP BY id ORDER BY name LIMIT ? OFFSET ?;");
            pstmt1.setString(1, ulogin);
            /*
             * pstmt1.setInt(2, 3); // 3 = Member pstmt1.setInt(3, 4); // 4 =
             * Owner pstmt1.setInt(4, 5); // 5 = Moderator
             */
            pstmt1.setInt(2, pagesize);
            pstmt1.setInt(3, offset);
            rs1 = pstmt1.executeQuery();
            splitstring2 = KWL.GetJsonForGrid(rs1);
            splitstring2 = splitstring2.substring(0, splitstring2.length() - 1);
            splitstring2 += ",\"count\":[" + count1 + "]}";

            rs.close();
            rs1.close();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("ProfileHandler.getProjectList", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return splitstring2;
    }

    public static String getMyTags(Connection conn, String ulogin)
            throws ServiceException {
        ResultSet rs = null;
        String tp = null;
        PreparedStatement pstmt = conn.prepareStatement("select distinct unitags.tagname from "
                + "(SELECT users.userid AS id FROM users "
                + "INNER JOIN userrelations ON userrelations.userid1=users.userid "
                + "INNER JOIN userlogin ON userlogin.userid = users.userid "
                + "WHERE userlogin.isactive = true and userrelations.userid2=? "
                + "UNION "
                + "SELECT users.userid AS id FROM users "
                + "INNER JOIN userrelations ON userrelations.userid2=users.userid "
                + "INNER JOIN userlogin ON userlogin.userid = users.userid "
                + "WHERE userlogin.isactive = true and userrelations.userid1=? "
                + "UNION "
                + "SELECT communitymembers.communityid as id FROM communitymembers "
                + "WHERE userid = ? "
                + "UNION "
                + "SELECT projectmembers.projectid as id FROM projectmembers where userid = ?) as temp "
                + "INNER JOIN profiletags on temp.id = profiletags.id inner join unitags on profiletags.tagid = unitags.tagid ;");
        try {
            pstmt.setString(1, ulogin);
            pstmt.setString(2, ulogin);
            pstmt.setString(3, ulogin);
            pstmt.setString(4, ulogin);
            rs = pstmt.executeQuery();

            KWLJsonConverter KWL1 = new KWLJsonConverter();
            tp = KWL1.GetJsonForGrid(rs).toString();

            rs.close();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("ProfileHandler.getProjectList", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return (" " + tp);
    }

    public static String setUserrelation(Connection conn, String loginid, String companyid,
            String uuserid1, String uuserid2, String relationid1,
            int urelationid, int actionid, java.util.Date dateval,
            java.text.SimpleDateFormat dtformat) throws ServiceException, ParseException, JSONException {

        /*
         *
         *
         * [sy]:PLEASE CHECK if() added for all .next();... query requests
         *
         */

        String query = null;
        boolean rs4 = false;
        String actionName = null;
        String mailFooter = KWLErrorMsgs.mailSystemFooter;

        UserDAO udao = new UserDAOImpl();
//        query = "select userlogin.username from userlogin where userlogin.userid = ? ";
//        DbResults rs2 = DbUtil.executeQuery(conn, query, uuserid1);

//        query = "select userlogin.username from userlogin where userlogin.userid = ? ";
//        DbResults rs3 = DbUtil.executeQuery(conn, query, uuserid2);
        String username1 = udao.getUser(conn, uuserid1).getUserName();
//        if (rs2.next()) {
//            username1 = rs2.getString("username");
//        }
        String username2 = udao.getUser(conn, uuserid2).getUserName();
//        if (rs3.next()) {
//            username2 = rs3.getString("username");
//        }
        int numRows = 0;
//		query = "select actions.actionname from actions where actions.actionid = ? ";
//		DbResults rs1 = DbUtil.executeQuery(conn, query, actionid);
//
//		if(rs1.next()){
//                    actionName = rs1.getString("actionname");
//                }
//                int numRows = 0;
//                if(actionid == 41) {
//                    HashMap info=new HashMap();
//                    String [] ob= new String[3];
//                    info.put("id",uuserid1);
//                    info.put("value",username1);
//                    ob[0]=LogHandler.getTags("u",info);
//                    info.clear();
//
//                    info.put("id",uuserid2);
//                    info.put("value",username2);
//                    ob[1]=LogHandler.getTags("u",info);
//                    info.clear();
//
//                    ob[2]=LogHandler.getActionLogTime(dateval);
//                    actionName = String.format(actionName, (Object[])ob);
//
//                    query = "select count(*) as count from actionlog where `by` = ? and `to` = ? and userid= ?";
//                    DbResults cres = DbUtil.executeQuery(conn, query, new Object[] {
//                                    uuserid2, uuserid1, uuserid1 });
//                    if(cres.next()){
//                        int count1 = cres.getInt("count");
//
//                        if(count1 == 0){
//                            query = "insert into actionlog (`userid`, `datedon`, `by`, `actionid`, `to`, `actiontext`) values ( ?, ?, ?, ?, ?, ?)";
//                            numRows = DbUtil.executeUpdate(conn, query, new Object[] {
//                                                    uuserid1, dtformat.format(dateval), uuserid2, actionid,
//                                                    uuserid1, actionName });
//                        }
//                        else{
//                            query = "update actionlog set `datedon`=?, `actiontext`=? where `by`=? and `actionid`=? and `to`=?";
//                            numRows = DbUtil.executeUpdate(conn, query, new Object[] {
//                                                    dtformat.format(dateval), actionName, uuserid2, actionid, uuserid1 });
//                        }
//                    }
//                }
//
        DbResults rsg = null;
        rsg = DbUtil.executeQuery(
                conn,
                "select count(*) as count from userrelations where (userid1 = ? and userid2 = ?) OR (userid1 = ? and userid2 = ?) ",
                new Object[]{uuserid1, uuserid2, uuserid2, uuserid1});
        if (rsg.next()) {
            int count = rsg.getInt("count");
            if (count == 0) {
                numRows = DbUtil.executeUpdate(
                        conn,
                        "insert into userrelations(relationid, userid1, userid2) values(?,?,?)",
                        new Object[]{urelationid, uuserid1, uuserid2});
            } else {
                if (urelationid == 0) {
                    rsg = DbUtil.executeQuery(
                            conn,
                            "select relationid from userrelations  where (userid1 = ? and userid2 = ?) OR (userid1 = ? and userid2 = ?)",
                            new Object[]{uuserid1, uuserid2, uuserid2, uuserid1});
                    if (rsg.next()) {
                        int recName = rsg.getInt("relationid");
                        numRows = DbUtil.executeUpdate(
                                conn,
                                "DELETE FROM userrelations where (userid1 = ? and userid2 = ?) OR (userid1 = ? and userid2 = ?) ",
                                new Object[]{uuserid1, uuserid2, uuserid2, uuserid1});
                        if (recName != 1) {
                            String msgString = "You have been dropped from " + username2 + "'s network." + mailFooter;
                            String subjectString = "Dropped from " + username2 + "'s network.";
                            String insertMsg = Mail.insertMailMsg(conn, username1, loginid, subjectString, msgString, "1", false, "1", "", "newmsg", "", 3, "", companyid);
                        } else {
                            String msgString = username2 + " has rejected your connection request." + mailFooter;
                            String subjectString = username2 + " has rejected your connection request.";
                            String insertMsg = Mail.insertMailMsg(conn, username1, loginid, subjectString, msgString, "1", false, "1", "", "newmsg", "", 3, "", companyid);
                        }
                    }
                } else {
                    numRows = DbUtil.executeUpdate(
                            conn,
                            "update userrelations set relationid= ? where userid1 = ? and userid2 = ? ",
                            new Object[]{urelationid, uuserid1, uuserid2});
                }
            }
        }
        if (urelationid == 3) {
            rsg = DbUtil.executeQuery(
                    conn,
                    "select username from userlogin where userid = ?",
                    new Object[]{uuserid1});
            if (rsg.next()) {
                String recName = rsg.getString("username");
                String msgString = username2 + " has accepted your connection request." + mailFooter;
                String subjectString = username2 + " has accepted your connection request.";
                String insertMsg = Mail.insertMailMsg(conn, recName, loginid, subjectString, msgString, "1", false, "1", "", "newmsg", "", 3, "", companyid);
            }
        }
        if (numRows == 0) {
            rs4 = false;
        } else {
            rs4 = true;
        }

        if (rs4) {
            return KWLErrorMsgs.rsTrue;
        } else {
            return KWLErrorMsgs.rsFalse;
        }
    }

    public static java.util.ArrayList<String> getFriendList(Connection conn,
            String userid) throws ServiceException {
        java.util.ArrayList<String> flist = new java.util.ArrayList<String>();
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("select userid1 from userrelations where userid2=? and relationid=3 "
                    + "union select userid2 from userrelations where userid1=? and relationid=3");
            pstmt.setString(1, userid);
            pstmt.setString(2, userid);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                flist.add(rs.getString(1));
            }
            rs.close();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("ProfileHandler.getFriendList", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return flist;
    }

    public static String updateProfile(Connection conn, HttpServletRequest request)
            throws ServiceException, SessionExpiredException {
        String msg = "";
        String companyid = AuthHandler.getCompanyid(request);
        String userid = AuthHandler.getUserid(request);
        String username = AuthHandler.getUserName(request);

        DiskFileUpload fu = new DiskFileUpload();
        List fileItems = null;
        FileItem fi = null;
        try {
            fileItems = fu.parseRequest(request);
        } catch (FileUploadException e) {
            throw ServiceException.FAILURE("ProfileHandler.updateProfile", e);
        }
        HashMap arrParam = new HashMap();
        for (Iterator k = fileItems.iterator(); k.hasNext();) {
            try {
                fi = (FileItem) k.next();
                arrParam.put(fi.getFieldName(), fi.getString("UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                throw ServiceException.FAILURE("ProfileHandler.updateProfile", ex);
            }
        }
//		try {
        String fname = StringUtil.serverHTMLStripper(arrParam.get("fname").toString());
        if (StringUtil.isNullOrEmpty(fname)) {
            fname = username;
        }
        String lname = StringUtil.serverHTMLStripper(arrParam.get("lname").toString());
        String emailid = StringUtil.serverHTMLStripper(arrParam.get("emailid").toString().trim().replace(" ", "+"));
        String dateformat = StringUtil.serverHTMLStripper(arrParam.get("dateformat").toString());
        String address = StringUtil.serverHTMLStripper(arrParam.get("address").toString());
        String contactno = StringUtil.serverHTMLStripper(arrParam.get("contactno").toString());
        String about = StringUtil.serverHTMLStripper(arrParam.get("about").toString());
        String desig = StringUtil.serverHTMLStripper(arrParam.get("designation").toString());
        String timezone = StringUtil.serverHTMLStripper(arrParam.get("timezone").toString());
//            String country = StringUtil.serverHTMLStripper(arrParam.get("country").toString());
        boolean isNotification = false;
        if (arrParam.containsKey("notification")) {
            String notification = StringUtil.serverHTMLStripper(arrParam.get("notification").toString());
            if (notification.equals("on")) {
                isNotification = true;
            }
        }
        if (StringUtil.isNullOrEmpty(emailid) || StringUtil.isNullOrEmpty(fname)
                || StringUtil.isNullOrEmpty(lname) || StringUtil.isNullOrEmpty(dateformat)) {
            return "Failure";
        }

        if (!StringUtil.isNullOrEmpty(userid) && !StringUtil.isNullOrEmpty(companyid)) {

//                String sql = "";
//                String platformURL = String.format(com.krawler.esp.utils.ConfigReader.getinstance().get("platformURL"), subdomain);
//                JSONObject jUser = new JSONObject();
//                jUser.put("userid", userid);
//                jUser.put("companyid", companyid);
//                jUser.put("username", username);
//                jUser.put("fname", fname);
//                jUser.put("lname", lname);
//                jUser.put("emailid", emailid);
//                jUser.put("address", address);
//                jUser.put("contactno", contactno);
//                jUser.put("about", about);
//                jUser.put("dateformat", dateformat);
//                jUser.put("timezone", timezone);
//                jUser.put("country", country);
//                jUser.put("roleid", "1");
//
//                // API Call To Platform For Updating User Profile
//                JSONObject resObj = APICallHandler.callApp(conn, platformURL, jUser, companyid, "2");
//                if(!resObj.isNull("success") && resObj.getBoolean("success")) {
            DbUtil.executeUpdate(conn, " UPDATE users SET fname = ?, lname = ?, emailid = ?, "
                    + " dateformat = ?, address = ?, contactno = ?, aboutuser = ?, timezone = ?, designation = ?, notification = ? "
                    + " WHERE userid = ? and companyid = ? ",
                    new Object[]{fname, lname, emailid, dateformat, address,
                        contactno, about, timezone, desig, isNotification, userid, companyid});

            if (arrParam.get("image").toString().length() != 0) {
                genericFileUpload uploader = new genericFileUpload();
                try {
                    uploader.doPost(fileItems, userid, StorageHandler.GetProfileImgStorePath());
                } catch (ConfigurationException ex) {
                    Logger.getLogger(ProfileHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

//		} catch (ConfigurationException e) {
//            msg="<br/>Failure ConfigurationException";
//            Logger.getLogger(ProfileHandler.class.getName()).log(Level.SEVERE,
//                    "Cofiguration Exception While Updating User Profile", e);
//
//        }
        return msg;
    }

    public static String changePassword(Connection conn, HttpServletRequest request, String platformURL)
            throws ServiceException, SessionExpiredException {

        String status = KWLErrorMsgs.rsSuccessFalseNoData;
        PreparedStatement pstmt = null;
        try {
            String pwd = request.getParameter("pass").toString();
            String rpwd = request.getParameter("rpass").toString();
            String oldpwd = request.getParameter("opass").toString();
            String userid = AuthHandler.getUserid(request);
            String companyid = AuthHandler.getCompanyid(request);
            JSONObject jobj = new JSONObject();
            if (pwd.compareTo(rpwd) == 0) {
                pwd = StringUtil.serverHTMLStripper(pwd);
                rpwd = StringUtil.serverHTMLStripper(rpwd);

//                if (!StringUtil.isNullOrEmpty(pwd) && pwd.length() >= 4 && pwd.length() <= 32) {
                // API Call To Platform For Changing Password
//                    String platformURL = URLUtil.platformURL;
                platformURL = Boolean.parseBoolean(ConfigReader.getinstance().get("PMStandAlone")) ? "" : platformURL;
                if (!StringUtil.isNullOrEmpty(platformURL)) {
                    JSONObject userData = new JSONObject();
                    userData.put("pwd", pwd);
                    userData.put("oldpwd", oldpwd);
                    userData.put("userid", userid);
                    userData.put("remoteapikey", ConfigReader.getinstance().get("remoteapikey"));
                    String action = "3";
                    JSONObject resObj = APICallHandler.callApp(conn, platformURL, userData, companyid, action);
                    if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                        pstmt = conn.prepareStatement(" UPDATE userlogin SET password = ? WHERE userid = ? ");
                        pstmt.setString(1, pwd);
                        pstmt.setString(2, userid);
                        int cnt = pstmt.executeUpdate();
                        if (cnt == 1) {
//                                status = "{\"success\":true,\"data\":\"Password changed successfully\"}";
                            jobj.put("success", true);
                            jobj.put("flag", 1);
                        } else {
//                                status = KWLErrorMsgs.errServer;
                            jobj.put("success", true);
                            jobj.put("flag", 4);
                        }
                    } else {
                        if (!resObj.isNull("errorcode") && resObj.getString("errorcode").equals("e10")) {
//                                status = KWLErrorMsgs.errPasswordOld;
                            jobj.put("success", true);
                            jobj.put("flag", 2);
                        } else {
//                                status = KWLErrorMsgs.errServer;
                            jobj.put("success", true);
                            jobj.put("flag", 4);
                        }
                    }
                } else {
                    String query = " SELECT password, emailid FROM userlogin INNER JOIN users "
                            + " ON userlogin.userid = users.userid WHERE userlogin.userid = ? ";
                    PreparedStatement pstmtCheck = conn.prepareStatement(query);
                    pstmtCheck.setString(1, userid);
                    ResultSet rsCheck = pstmtCheck.executeQuery();
                    if (rsCheck.next()) {
                        if (rsCheck.getString("password").equals(oldpwd)) {
                            pstmt = conn.prepareStatement(" UPDATE userlogin SET password = ? WHERE userid = ? ");
                            pstmt.setString(1, pwd);
                            pstmt.setString(2, userid);
                            int cnt = pstmt.executeUpdate();
                            if (cnt == 1) {
                                jobj.put("success", true);
                                jobj.put("flag", 1);
                            } else {
//                                        status = KWLErrorMsgs.errServer;
                                jobj.put("success", true);
                                jobj.put("flag", 4);
                            }
                        } else {
//                                    status = KWLErrorMsgs.errPasswordOld;
                            jobj.put("success", true);
                            jobj.put("flag", 2);
                        }
                    }
                }
//                } else {
//                    status = KWLErrorMsgs.errPasswordMinMaxLength;
//                }
            } else {
//                status = KWLErrorMsgs.errPasswordDoNotMatch;
                jobj.put("success", true);
                jobj.put("flag", 3);
            }
            status = jobj.toString();
        } catch (JSONException ex) {
            Logger.getLogger(ProfileHandler.class.getName()).log(Level.SEVERE,
                    "JSON Exception While Changing Password", ex);
            throw ServiceException.FAILURE("ProfileHandler.checkChangePassword", ex);

        } catch (SQLException ex) {
            Logger.getLogger(ProfileHandler.class.getName()).log(Level.SEVERE,
                    "SQL Exception While Changing Password", ex);
            throw ServiceException.FAILURE("ProfileHandler.checkChangePassword", ex);
        }
        return status;
    }

    public static String getPersonalInfo(Connection conn, HttpServletRequest request, String platformURL)
            throws ServiceException {

        String userData = "";
        ResultSet rs = null;
        PreparedStatement pstmt = null;

        try {

            String userid = AuthHandler.getUserid(request);
            String companyid = AuthHandler.getCompanyid(request);

            if (!StringUtil.isNullOrEmpty(userid) && !StringUtil.isNullOrEmpty(companyid)) {

                boolean sa = Boolean.parseBoolean(ConfigReader.getinstance().get("PMStandAlone"));
                JSONObject resObj = null;
                if(!sa){
                    JSONObject jUser = new JSONObject();
                    jUser.put("userid", userid);
                    jUser.put("companyid", companyid);
                    jUser.put("appid", 1);

                    // API Call To Platform For Fetching User Profile
                    resObj = APICallHandler.callApp(conn, platformURL, jUser, companyid, "1");
                } else {
                    resObj = new JSONObject();
                }
                JSONObject resData = null;

                if (resObj.toString().compareTo("{}") != 0 && resObj.getBoolean("success")) {
                    resData = new JSONObject(resObj.get("data").toString());
                    JSONArray resArr = null;
                    resArr = resData.getJSONArray("data");

                    pstmt = conn.prepareStatement(" SELECT aboutuser, designation, notification FROM users WHERE userid = ? ");
                    pstmt.setString(1, userid);
                    rs = pstmt.executeQuery();
                    if (rs.next()) {
                        String aboutUser = rs.getString("aboutuser");
                        if (!StringUtil.isNullOrEmpty(aboutUser)) {
                            resArr.getJSONObject(0).put("aboutuser", rs.getString("aboutuser"));

                        } else {
                            resArr.getJSONObject(0).put("aboutuser", "");
                        }
                        String desig = rs.getString("designation");
                        if (!StringUtil.isNullOrEmpty(desig)) {
                            resArr.getJSONObject(0).put("desig", rs.getString("designation"));
                        } else {
                            resArr.getJSONObject(0).put("desig", "");
                        }
                        resArr.getJSONObject(0).put("notification", rs.getBoolean("notification"));
                    } else {
                        resArr.getJSONObject(0).put("aboutuser", "");
                        resArr.getJSONObject(0).put("desig", "");
                        resArr.getJSONObject(0).put("notification", false);
                    }

                    JSONObject res = new JSONObject();
                    res.put("data", resArr);
                    userData = res.toString();

                } else {
                    pstmt = conn.prepareStatement(" SELECT userlogin.username, fname, lname, "
                            + " emailid, address, contactno, aboutuser, timezone, country, designation as desig, notification "
                            + " FROM users inner join userlogin on users.userid =userlogin.userid WHERE users.userid = ? ");
                    pstmt.setString(1, userid);
                    rs = pstmt.executeQuery();
                    userData = new KWLJsonConverter().GetJsonForGrid(rs).toString();
                }
                rs.close();
            }

        } catch (JSONException ex) {
            Logger.getLogger(ProfileHandler.class.getName()).log(Level.SEVERE,
                    "JSON Exception While Fetching User Details", ex);
            throw ServiceException.FAILURE("ProfileHandler.getPersonalInfo", ex);

        } catch (SessionExpiredException ex) {
            Logger.getLogger(ProfileHandler.class.getName()).log(Level.SEVERE,
                    "Session Expired Exception While Fetching User Details", ex);
            throw ServiceException.FAILURE("ProfileHandler.getPersonalInfo", ex);

        } catch (SQLException e) {
            Logger.getLogger(ProfileHandler.class.getName()).log(Level.SEVERE,
                    "SQL Exception While Fetching User Profile", e);
            throw ServiceException.FAILURE("ProfileHandler.getPersonalInfo", e);

        } finally {
            DbPool.closeStatement(pstmt);
        }
        return userData;
    }

    public static String getTags(Connection conn, String profileId)
            throws ServiceException {
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        String tp = null;
        try {
            pstmt = conn.prepareStatement("SELECT unitags.tagname FROM unitags inner join profiletags on unitags.tagid = profiletags.tagid where id = ? ;");
            pstmt.setString(1, profileId);
            rs = pstmt.executeQuery();
            KWLJsonConverter KWL1 = new KWLJsonConverter();
            tp = KWL1.GetJsonForGrid(rs).toString();
            rs.close();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("ProfileHandler.getTags", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return tp;
    }

    public static void setTags(Connection conn, String profileId, String tagStr)
            throws ServiceException {
        tagStr = StringUtil.serverHTMLStripper(tagStr);
        profileId = StringUtil.serverHTMLStripper(profileId);
        if (!StringUtil.isNullOrEmpty(tagStr) || !StringUtil.isNullOrEmpty(profileId)) {
            deleteAllTags(conn, profileId);
            ArrayList<String> tagArray = TagUtils.getUniqTagArray(tagStr);
            if (tagArray.size() > 0) {
                for (String tag : tagArray) {
                    String tagid = TagUtils.getTagId(conn, tag);
                    DbUtil.executeUpdate(conn,
                            "insert into profiletags (tagid, id) values(?, ?)",
                            new Object[]{tagid, profileId});
                }
            }
        }
    }

    private static void deleteAllTags(Connection conn, String profileId)
            throws ServiceException {
        DbUtil.executeUpdate(conn, "delete from profiletags where id= ?",
                new Object[]{profileId});
    }

    public static String getUserRequest(Connection conn, String userid)
            throws ServiceException {
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        String tp = null;
        try {
            pstmt = conn.prepareStatement("select userid1 from userrelations ur "
                    + "inner join userlogin ul on ur.userid1 = ul.userid where userid2=? and relationid=? and ul.isactive = true");
            pstmt.setString(1, userid);
            pstmt.setInt(2, 1);
            rs = pstmt.executeQuery();
            KWLJsonConverter KWL1 = new KWLJsonConverter();
            tp = KWL1.GetJsonForGrid(rs).toString();
            rs.close();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("ProfileHandler.userReaquest", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return tp;
    }

    public static boolean setPersonalPrefs(Connection conn, String userid, String timezone, String dateFormat) throws ServiceException {
        boolean result = true;
        int dtFmt = Integer.parseInt(dateFormat);
        DbUtil.executeUpdate(conn, "update users set timezone = ?, dateformat = ? where userid = ?", new Object[]{timezone, dtFmt, userid});
        return result;
    }

    public static String getUserFullName(Connection cn, HttpServletRequest request)
            throws SessionExpiredException, ServiceException {
        String name = null;
        PreparedStatement pstmt = null;
        try {
            String query = "select concat(fname,' ',lname) as fullname from users where userid = ?";
            pstmt = cn.prepareStatement(query);
            pstmt.setString(1, AuthHandler.getUserid(request));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                name = rs.getString("fullname");
            }
            rs.close();
        } catch (SQLException sex) {
            throw ServiceException.FAILURE("AuthHandler.getPreferences", sex);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return name;
    }

    public static String getUserAuthInfo(Connection conn, HttpServletRequest request)
            throws ServiceException {
        String retstr = "{}";
        ResultSet rs = null;
        JSONObject jobj = new JSONObject();
        String symbol = "";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(" SELECT companyname, subdomain, currency, "
                    + " currency.htmlcode FROM company INNER JOIN users ON users.companyid = company.companyid "
                    + " INNER JOIN currency ON company.currency = currency.currencyid "
                    + " INNER JOIN userlogin ON users.userid = userlogin.userid "
                    + " WHERE users.userid = ? AND userlogin.isactive = true ");
            pstmt.setString(1, AuthHandler.getUserid(request));
            rs = pstmt.executeQuery();
            while (rs.next()) {
                jobj.put("companyname", rs.getString("companyname"));
                jobj.put("supprotlink", rs.getString("subdomain") + "@deskera.com");
                jobj.put("currency", rs.getObject("currency"));
                symbol = rs.getString("htmlcode");//"\u0024";//
                jobj.put("currencysymbol", symbol);
            }
            rs.close();
            retstr = jobj.toString();

        } catch (JSONException ex) {
            Logger.getLogger(ProfileHandler.class.getName()).log(Level.SEVERE,
                    "JSON Exception While Users Authentication Information", ex);
            throw ServiceException.FAILURE("ProfileHandler.getUserAuthInfo", ex);

        } catch (SQLException e) {
            throw ServiceException.FAILURE("ProfileHandler.getUserAuthInfo", e);
        } catch (SessionExpiredException e) {
            throw ServiceException.FAILURE("ProfileHandler.getUserAuthInfo", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return retstr;
    }

    public static String getCmpCurrFromProj(Connection conn, String projid)
            throws ServiceException {
        String retstr = "";
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("select currencyname,symbol,currencyid from currency where currencyid = (select currency from company inner join project on project.companyid = company.companyid where projectid = ?)");
            pstmt.setString(1, projid);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                retstr = rs.getString("symbol");
            }
            rs.close();
            //retstr = jobj.toString();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("ProfileHandler.getCmpCurrFromProj", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return retstr;
    }

    public static String getUserid(Connection conn, String username, String companyid)
            throws ServiceException {
        String userid = "";
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("select users.userid from users inner join userlogin on users.userid=userlogin.userid where username = ? and companyid = ?");
            pstmt.setString(1, username);
            pstmt.setString(2, companyid);

            rs = pstmt.executeQuery();
            while (rs.next()) {
                userid = rs.getString("userid");
            }
            rs.close();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("ProfileHandler.getCmpCurrFromProj", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }

        return userid;
    }

    public static String setUserTimezone(Connection conn, String userid, String companyid)
            throws ServiceException {
        String timezone = "";
        timezone = com.krawler.common.timezone.Timezone.getUserCompanyTimeZone(conn, userid, companyid);
        //dbutil is being used here because 'conn' does not commit itself (coz it only retrieves data) in validate.jsp
        DbUtil.executeUpdate("update users set timezone = ? where userid = ?", new Object[]{timezone, userid});
        return timezone;
    }

    public static int getmaxfilesize(Connection conn, String companyid) throws ServiceException {
        int size = 1;
        DbResults rs = null;
        // String pcid = "1";
        String sql = null;
        sql = "select maxfilesize from company where companyid = ?";
        rs = DbUtil.executeQuery(conn, sql, new Object[]{companyid});
        if (rs.next()) {
            size = rs.getInt("maxfilesize");
        }
        return size;
    }

    public static boolean isUserSubscribedToNotification(Connection conn, String userid) throws ServiceException {
        boolean sub = false;
        DbResults rs = DbUtil.executeQuery(conn, "SELECT notification FROM users WHERE userid = ?", userid);
        if (rs.next()) {
            sub = rs.getBoolean("notification");
        }
        return sub;
    }

    /**
     * @param moduleid 1-project plan, 2- todo, 3-Calendar
     */
    public static boolean isUserSubToNotifOnProjModules(Connection conn, String userid, String projectid, int moduleid) throws ServiceException {
        boolean sub = false;
        DbResults rs = DbUtil.executeQuery(conn, "SELECT notification_subscription FROM projectmembers WHERE userid = ? AND projectid = ?",
                new Object[]{userid, projectid});
        if (rs.next()) {
            int notifVal = rs.getInt("notification_subscription");
            int tempVal = (int) Math.pow(2, moduleid);
            if ((notifVal & tempVal) == tempVal) {
                sub = true;
            }
        }
        return sub;
    }

    public static String getAppsImageInJSON(String jsonString, String userkey, String imgKey, int size) throws JSONException {
        com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject();
        jobj = new JSONObject(jsonString);
        int cnt = jobj.getJSONArray("data").length();
        for (int i = 0; i < cnt; i++) {
            String img = jobj.getJSONArray("data").getJSONObject(i).getString(userkey);
            if (!StringUtil.isNullOrEmpty(img)) {
                img = StringUtil.getAppsImagePath(img, size);
                jobj.getJSONArray("data").getJSONObject(i).remove(imgKey);
                jobj.getJSONArray("data").getJSONObject(i).put(imgKey, img);
            }
        }
        return jobj.toString();
    }

    public static void main(String[] a) {
        try {
            String result = "\u0048\u0065\u006C\u006C\u006F World";
            byte[] utf8 = result.getBytes("UTF-8");
            String str = new String(utf8, "UTF-8");
            System.out.print(str);

            Connection conn = DbPool.getConnection();
            String curr = getCmpCurrFromProj(conn, "5990e42f-acee-49f4-9a83-48867db77eb0");
            conn.close();
            String currSymbol = "";
            try {
                char a1 = (char) Integer.parseInt(curr, 16);
                currSymbol = Character.toString(a1);
            } catch (Exception e) {
                currSymbol = curr;
            }
            //byte[] currencybyte = ("\\u00" + curr).getBytes("utf-8");
//            str = new String(currencybyte,"UTF-8");
            System.out.print(currSymbol);
        } catch (Exception ex) {
            Logger.getLogger(ProfileHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
