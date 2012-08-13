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
package com.krawler.esp.portalmsg;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;

import com.krawler.common.service.ServiceException;
import com.krawler.common.util.KWLErrorMsgs;
import com.krawler.common.util.StringUtil;
import com.krawler.database.DbPool;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.database.DbPool.Connection;
import com.krawler.utils.json.KWLJsonConverter;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.util.JSONStringer;
import com.krawler.utils.json.base.JSONObject;
import com.krawler.common.timezone.Timezone;
import com.krawler.esp.handlers.AuthHandler;
import com.krawler.esp.handlers.CompanyHandler;
import com.krawler.esp.user.UserDAO;
import com.krawler.esp.user.UserDAOImpl;
import java.util.*;

public class Mail {

    public static String MoveMails(Connection conn, String post_idArray,
            String last_folder_id, String dest_folder_id, String loginid)
            throws ServiceException, JSONException {
        String str = "{data:[";
        String str1 = " ";
        String query = null;
        com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject(
                post_idArray);
        if ((last_folder_id.equals(dest_folder_id)) && (Integer.parseInt(dest_folder_id) == 2)) {
            for (int i = 0; i < jobj.getJSONArray("data").length(); i++) {
                String post_id = jobj.getJSONArray("data").getJSONObject(i).getString("post_id");
                String query1 = "delete from mailmessages where post_id = ?";
                int numRows = DbUtil.executeUpdate(conn, query1, post_id);
                if (numRows == 0) {
                    post_id = "-1";
                }
                conn.commit();
                str1 += "{'post_id':'" + post_id + "'},";
            }
        } else {
            for (int i = 0; i < jobj.getJSONArray("data").length(); i++) {
                String post_id = jobj.getJSONArray("data").getJSONObject(i).getString("post_id");
                if (dest_folder_id.equals("2")) {
                    query = "Select to_id,poster_id from mailmessages where post_id =? ";
                    // Object[] params1 = { post_id };
                    DbResults rs1 = DbUtil.executeQuery(conn, query, post_id);
                    while (rs1.next()) {
                        if (loginid.equals(rs1.getString("to_id"))) {
                            dest_folder_id = getDelId(loginid, "0");
                        } else if (loginid.equals(rs1.getString("poster_id"))) {
                            dest_folder_id = getDelId(loginid, "1");
                        }
                    }
                }
                query = "Select folder from mailmessages where post_id =? ";
                // Object[] params1 = { post_id };
                DbResults rs = DbUtil.executeQuery(conn, query, post_id);
                while (rs.next()) {
                    last_folder_id = rs.getString(1);
                }

                query = "Update mailmessages set folder= ? , last_folder_id = ? where post_id = ?";
                // Object[] params2 = { dest_folder_id, last_folder_id, post_idArray
                // };
                int numRows = DbUtil.executeUpdate(conn, query, new Object[]{
                            dest_folder_id, last_folder_id, post_id});
                str1 += "{'post_id':'" + post_id + "'},";
            }
        }
        str1 = str1.substring(0, str1.length() - 1);
        str += str1;
        str += "]}";

        return str;
    }

    public static String deleteforeverMailMsg(Connection conn, String post_id)
            throws ServiceException {

        String query = "delete from mailmessages where post_id = ?";
        // Object[] params = { post_id };
        int numRows = DbUtil.executeUpdate(conn, query, post_id);
        if (numRows == 0) {
            post_id = "-1";
        }

        return post_id;
    }

    public static String restoreMailMsg(Connection conn, String post_id,
            String last_folder_id) throws ServiceException {
        String query = "Select last_folder_id from mailmessages where post_id =? ";
        // Object[] params = { post_id };
        DbResults results = DbUtil.executeQuery(conn, query, post_id);
        while (results.next()) {
            last_folder_id = results.getString(1);
        }

        query = "Update mailmessages set folder= ? , last_folder_id = ? where post_id = ?";
        // Object[] params1 = { last_folder_id, last_folder_id, post_id };
        int numRows = DbUtil.executeUpdate(conn, query, new Object[]{
                    last_folder_id, last_folder_id, post_id});
        if (numRows == 0) {
            post_id = "-1";
        }

        return post_id;
    }

    public static int GetMailMessages(Connection conn, String folderid,
            String loginid, String offset, String limit)
            throws ServiceException {
        int tCount = 0;
        String query = null;
        Object[] params = {null};
        if (folderid.compareTo("0") == 0) {
            query = "Select count(post_id) from mailmessages inner join users on users.userid = mailmessages.poster_id where " + "folder = '0' and to_id = ?";
            params[0] = loginid;
        } else if (folderid.compareTo("1") == 0) {
            query = "Select count(post_id) from mailmessages inner join users on users.userid = mailmessages.to_id where folder = '1' and " + "poster_id = ?";
            params[0] = loginid;
        } else if (folderid.compareTo("4") == 0)// Starred Items
        {
            query = "Select count(post_id) from mailmessages where post_id IN (Select post_id from mailmessages inner join users on " + "users.userid = mailmessages.poster_id where folder = '0' and to_id = ? and flag = true  union " + "Select post_id from mailmessages inner join users on users.userid = mailmessages.to_id where folder = '1' and " + "poster_id = ? and flag = true)";
            params[0] = loginid;
            params[1] = loginid;
        } else {
            query = "Select count(post_id) from mailmessages where post_id IN (Select post_id from mailmessages inner join users on " + "users.userid = mailmessages.poster_id where folder = ? and to_id = ? union Select post_id from mailmessages inner join " + "users on users.userid = mailmessages.to_id where folder = ? and poster_id = ?)";
            params[0] = folderid;
            params[1] = loginid;
            params[2] = folderid;
            params[3] = loginid;

        }

        DbResults rsc = DbUtil.executeQuery(conn, query, params);
        while (rsc.next()) {
            tCount = rsc.getInt(1);
        }

        return tCount;
    }

    public static int GetMailMessagesCount(Connection conn, String folderid,
            String loginid) throws ServiceException {
        int tCount = 0;

        String query = null;
        Object[] params = {null};
        // try{
        if (folderid.compareTo("0") == 0) {
            query = "Select count(post_id) from mailmessages inner join users on users.userid = mailmessages.poster_id where " + "folder = '0' and to_id = ?";
            params[0] = loginid;
        } else if (folderid.compareTo("1") == 0) {
            query = "Select count(post_id) from mailmessages inner join users on users.userid = mailmessages.to_id where folder = '1' and " + "poster_id = ?";
            params[0] = loginid;
        } else if (folderid.compareTo("4") == 0)// Starred Items
        {
            query = "Select count(post_id) from mailmessages where post_id IN (Select post_id from mailmessages inner join users on " + "users.userid = mailmessages.poster_id where folder = '0' and to_id = ? and flag = true  union " + "Select post_id from mailmessages inner join users on users.userid = mailmessages.to_id where folder = '1' and " + "poster_id = ? and flag = true)";
            params[0] = loginid;
            params[1] = loginid;
        } else {
            query = "Select count(post_id) from mailmessages where post_id IN (Select post_id from mailmessages inner join users on " + "users.userid = mailmessages.poster_id where folder = ? and to_id = ? union Select post_id from mailmessages inner join " + "users on users.userid = mailmessages.to_id where folder = ? and poster_id = ?)";
            params[0] = folderid;
            params[1] = loginid;
            params[2] = folderid;
            params[3] = loginid;
        }

        DbResults rsc = DbUtil.executeQuery(conn, query, params);
        while (rsc.next()) {
            tCount = rsc.getInt(1);
        }

        return tCount;
    }

    public static String StarChangeForMail(Connection conn, String post_id,
            boolean flag) throws ServiceException, JSONException {
        String query = null;
        JSONObject res = new JSONObject();
        com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject(
                post_id);
        for (int i = 0; i < jobj.getJSONArray("data").length(); i++) {
            String post_id1 = jobj.getJSONArray("data").getJSONObject(i).getString("post_id");
            query = "Update mailmessages set flag = ? where post_id = ?";
            DbUtil.executeUpdate(conn, query, new Object[]{flag, post_id1});
            JSONObject temp = new JSONObject();
            temp.put("flag", flag);
            temp.put("postid", post_id1);
            res.append("data", temp);
        }
        return res.toString();
    }

    public static String DeleteFoldernameForMailuser(Connection conn,
            String folderid) throws ServiceException {
        String query = null;
        // Object[] params = { null };
        String query1 = "Update mailmessages set folder='2' where folder=?";
        DbUtil.executeUpdate(conn, query1, folderid);
        conn.commit();
        query = "Delete from mailmsgfoldermap where folder_id = ?";
        // params[0] = folderid;
        int numRows = DbUtil.executeUpdate(conn, query, folderid);
        if (numRows == 0) {
            folderid = "-2";
        }
        return folderid;
    }

    public static String UpdateFoldernameForMailuser(Connection conn, String folderid, String foldername, String userid)
            throws ServiceException {
        DbResults rs = null;
        String query = null;
        folderid = StringUtil.serverHTMLStripper(folderid);
        foldername = StringUtil.serverHTMLStripper(foldername);
        userid = StringUtil.serverHTMLStripper(userid);
        if (StringUtil.isNullOrEmpty(folderid) || StringUtil.isNullOrEmpty(foldername) || StringUtil.isNullOrEmpty(userid)) {
            folderid = "-1";
            return folderid;
        }
        int count = 0;
        query = "Select mailmsgfoldermap.folder_name as fname from mailmsgfoldermap "
                + "inner join mailuserfoldersmap on mailmsgfoldermap.folder_id = mailuserfoldersmap.folderid "
                + "where mailuserfoldersmap.userid = ?";
        rs = DbUtil.executeQuery(conn, query, new Object[]{userid});
        while (rs.next()) {
            String fname = rs.getString("fname");
            if (fname.equals(foldername)) {
                count = 1;
            }
        }
        if (count > 0) {
            folderid = "-1";
        } else {
            query = "Update mailmsgfoldermap set folder_name= ? where folder_id = ?";
            int numRows = DbUtil.executeUpdate(conn, query, new Object[]{foldername, folderid});
            if (numRows == 0) {
                folderid = "-2";
            }
        }
        return folderid;
    }

    public static String getFolderidForMailuser(Connection conn, String userid,
            String foldername) throws ServiceException {
        DbResults rs = null;
        String folderid = null;
        String query = null;
        // Object[] params = { null };

        query = "Select count(*) as count from mailmsgfoldermap inner join mailuserfoldersmap on mailmsgfoldermap.folder_id = mailuserfoldersmap.folderid where mailmsgfoldermap.folder_name = ? and mailuserfoldersmap.userid = ?";

        // params[0] = foldername;
        // params[1] = userid;

        rs = DbUtil.executeQuery(conn, query,
                new Object[]{foldername, userid});

        rs.next();
        int count = rs.getInt(1);
        if (count > 0) {
            folderid = "-1";
        } else if (count == 0) {
            folderid = UUID.randomUUID().toString();
            query = "Insert into mailmsgfoldermap (folder_id, folder_name, folder_path) values (?, ?, ?)";

            int numRows = DbUtil.executeUpdate(conn, query, new Object[]{
                        folderid, foldername, "Folders"});

            query = "Insert into mailuserfoldersmap (userid, folderid) values (?, ?)";

            numRows = DbUtil.executeUpdate(conn, query, new Object[]{userid,
                        folderid});

            if (numRows == 0) {
                folderid = "-2";
            }

        }

        return folderid;
    }

    public static String insertMailMsg(Connection conn, String to_id,
            String poster_id, String post_subject, String post_text,
            String folder, Boolean readflag, String last_folder_id,
            String reply_to, String sendflag, String post_id1, int draft,
            String fid, String companyid) throws ServiceException, ParseException, JSONException {
        String query;
        String post_id = "";
        String temp = "";
        JSONObject jobj = new JSONObject();
        String usr = to_id;
        DbResults rs1 = null;
        DbResults rs2 = null;
        int numRows = 0;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Date d = new java.util.Date();
        java.sql.Timestamp sqlPostDate = new java.sql.Timestamp(d.getTime());
        to_id = StringUtil.serverHTMLStripper(to_id);
        poster_id = StringUtil.serverHTMLStripper(poster_id);
        reply_to = StringUtil.serverHTMLStripper(reply_to);
        post_subject = StringUtil.serverHTMLStripper(post_subject);
        folder = StringUtil.serverHTMLStripper(folder);
        last_folder_id = StringUtil.serverHTMLStripper(last_folder_id);
        sendflag = StringUtil.serverHTMLStripper(sendflag);
        fid = StringUtil.serverHTMLStripper(fid);
        post_id1 = StringUtil.serverHTMLStripper(post_id1);
        UserDAO userDao = new UserDAOImpl();
        if (sendflag.compareTo("reply") == 0) {
            if (draft == 1) {
                post_id = UUID.randomUUID().toString();
                folder = getDraftId(poster_id);//"3";
                last_folder_id = "0";
                readflag = false;
                reply_to = "";
                if (fid.compareTo("3") != 0) {
                    if (StringUtil.isNullOrEmpty(to_id) || StringUtil.isNullOrEmpty(poster_id)) {
                        return KWLErrorMsgs.exSuccessFail;
                    }
                    query = "Insert into mailmessages (post_id, to_id, poster_id, post_subject, post_text, post_time, flag, folder, reply_to, last_folder_id, readflag) values ( ?, ?, ?, ?, ?,?,false, ?, ?, ?, ?)";
                    post_id = UUID.randomUUID().toString();
                    numRows = DbUtil.executeUpdate(conn, query, new Object[]{
                                post_id, to_id, poster_id, post_subject, post_text, sqlPostDate,
                                folder, reply_to, last_folder_id, readflag});
                } else {
                    if (StringUtil.isNullOrEmpty(post_id1)) {
                        return KWLErrorMsgs.exSuccessFail;
                    }
                    query = "Update mailmessages set post_subject=?, post_text=? where post_id=?";
                    numRows = DbUtil.executeUpdate(conn, query, new Object[]{
                                post_subject, post_text, post_id1});
                }
            } else if (draft == 2) {
                if (StringUtil.isNullOrEmpty(post_id1) || StringUtil.isNullOrEmpty(to_id) || StringUtil.isNullOrEmpty(poster_id)) {
                    return KWLErrorMsgs.exSuccessFail;
                }
//                query = "SELECT users.userid FROM users inner join userlogin on users.userid=userlogin.userid where userlogin.username=? and users.companyid = ?;";
//                rs2 = DbUtil.executeQuery(conn, query, new Object[]{post_id1, companyid});
//                if (rs2.next()) {
                    to_id = userDao.getUserID(conn, post_id1, companyid);//(rs2.getString(1));
//                }
                query = "Insert into mailmessages (post_id, to_id, poster_id, post_subject, post_text, post_time, flag, folder, reply_to, last_folder_id, readflag) values ( ?, ?, ?, ?, ?, ?,false, ?, ?, ?, ?)";
                post_id = UUID.randomUUID().toString();
                numRows = DbUtil.executeUpdate(conn, query, new Object[]{
                            post_id, to_id, poster_id, post_subject, post_text, sqlPostDate,
                            folder, reply_to, last_folder_id, readflag});
                post_id = UUID.randomUUID().toString();
                folder = "0";
                last_folder_id = "0";
                readflag = false;
                reply_to = "";
                query = "Insert into mailmessages (post_id, to_id, poster_id, post_subject, post_text, post_time, flag, folder, reply_to, last_folder_id, readflag) values ( ?, ?, ?, ?, ?, ?,false, ?, ?, ?, ?)";
                numRows = DbUtil.executeUpdate(conn, query, new Object[]{
                            post_id, to_id, poster_id, post_subject, post_text, sqlPostDate,
                            folder, reply_to, last_folder_id, readflag});
            } else {
                query = "SELECT poster_id FROM mailmessages where post_id=?";
                DbResults rs = DbUtil.executeQuery(conn, query, post_id1);
                while (rs.next()) {
                    to_id = rs.getString(1);
                }
            }
        } else {
            if (draft != 1) {
                if (draft == 3) {
                    if (post_id1.contains("@")) {
                        query = "SELECT userid FROM users where emailid=?;";
                        rs2 = DbUtil.executeQuery(conn, query, to_id);
                        if (rs2.next()) {
                            to_id = (rs2.getString(1));
                        } else {
                            to_id = "-1";   //for invalid username
                        }
                    } else {
                        query = "SELECT users.userid FROM users inner join userlogin on users.userid =userlogin.userid where userlogin.username=? and users.companyid = ?;";
                        rs2 = DbUtil.executeQuery(conn, query, new Object[]{to_id, companyid});
                        if (rs2.next()) {
                            to_id = (rs2.getString(1));
                        } else {
                            to_id = "-1";   //for invalid username
                        }
                    }
                }
                if (to_id.compareTo("-1") != 0) {
                    if (StringUtil.isNullOrEmpty(to_id) || StringUtil.isNullOrEmpty(poster_id)) {
                        return KWLErrorMsgs.exSuccessFail;
                    }
                    query = "Insert into mailmessages (post_id, to_id, poster_id, post_subject, post_text, post_time, flag, folder, reply_to, last_folder_id, readflag) values ( ?, ?, ?, ?, ?, ?,false, ?, ?, ?, ?)";
                    post_id = UUID.randomUUID().toString();
                    numRows = DbUtil.executeUpdate(conn, query, new Object[]{
                                post_id, to_id, poster_id, post_subject, post_text, sqlPostDate,
                                folder, reply_to, last_folder_id, readflag});
                    post_id = UUID.randomUUID().toString();
                    folder = "0";
                    last_folder_id = "0";
                    readflag = false;
                    reply_to = "";
                }
            } else {
                post_id = UUID.randomUUID().toString();
                folder = getDraftId(poster_id);//"3";
                last_folder_id = getDraftId(poster_id);
                readflag = false;
                reply_to = "";
                query = "SELECT users.userid FROM users inner join userlogin on users.userid =userlogin.userid where userlogin.username=? and users.companyid = ?;";
                rs2 = DbUtil.executeQuery(conn, query, new Object[]{to_id, companyid});

                if (rs2.next()) {
                    to_id = (rs2.getString(1));
                } else {
                    to_id = poster_id;
                }
            }
            if (to_id.compareTo("-1") != 0) {
                if (StringUtil.isNullOrEmpty(to_id) || StringUtil.isNullOrEmpty(poster_id)) {
                    return KWLErrorMsgs.exSuccessFail;
                }
                query = "Insert into mailmessages (post_id, to_id, poster_id, post_subject, post_text, post_time, flag, folder, reply_to, last_folder_id, readflag) values ( ?, ?, ?, ?, ?, ?,false, ?, ?, ?, ?)";
                numRows = DbUtil.executeUpdate(conn, query, new Object[]{post_id,
                            to_id, poster_id, post_subject, post_text, sqlPostDate, folder,
                            reply_to, last_folder_id, readflag});
            }
        }
        if (numRows == 0) {
            if (to_id.compareTo("-1") == 0) {
                com.krawler.utils.json.base.JSONObject jtemp = new com.krawler.utils.json.base.JSONObject();
                jtemp.put("Success", "userfail");
                jtemp.put("Subject", usr);
                jobj.append("data", jtemp);
                return jobj.toString();
            } else {
                return KWLErrorMsgs.exSuccessFail;
            }
        } else {
            String dateTime = "";
            String UserName = "";
            String Image = "";
            query = "SELECT userlogin.username,image FROM users inner join userlogin on users.userid=userlogin.userid where users.userid=?;";
            rs2 = DbUtil.executeQuery(conn, query, poster_id);
            if (rs2.next()) {
                UserName = (rs2.getString(1));
                Image = (rs2.getString(2));
            }
            if (draft == 1 && sendflag.compareTo("reply") == 0) {
                query = "SELECT post_time FROM mailmessages where post_id=?;";
                rs1 = DbUtil.executeQuery(conn, query, post_id1);
                if (rs1.next()) {
                    dateTime = (rs1.getObject(1).toString());
                }
            } else {
                query = "SELECT post_time FROM mailmessages where post_id=?;";
                rs1 = DbUtil.executeQuery(conn, query, post_id);
                if (rs1.next()) {
                    dateTime = (rs1.getObject(1).toString());
                }
            }
            String userTime = Timezone.toCompanyTimezone(conn, sqlPostDate.toString(), companyid);
            java.util.Date tempdate = sdf.parse(userTime);
            java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat(
                    "yyyy-MM-dd h:mm a");
            JSONStringer j = new JSONStringer();
            com.krawler.utils.json.base.JSONObject jtemp = new com.krawler.utils.json.base.JSONObject();
            String success = "Success";
            if (folder.compareTo("3") == 0) {
                success = "Draft";
            }
            jtemp.put("Success", success);
            jtemp.put("post_time", sdf1.format(tempdate).toString());
            jtemp.put("flag", "false");
            jtemp.put("post_id", post_id);
            jtemp.put("post_subject", post_subject);
            jtemp.put("post_text", "");
            jtemp.put("poster_id", UserName);
            jtemp.put("readflag", "0");
            jtemp.put("imgsrc", Image);
            jtemp.put("senderid", poster_id);
            jobj.append("data", jtemp);
        /*temp = j.object().key("Success").value("Success").key("post_time")
        .value(sdf1.format(tempdate).toString()).key("flag").value(
        "false").key("post_id").value(post_id).key(
        "post_subject").value(post_subject)
        .key("post_text").value("").key("poster_id")
        .value(UserName).key("readflag").value("0").key("imgsrc")
        .value(Image).key("senderid").value(poster_id).endObject()
        .toString();*/
        }
        return jobj.toString();
    }

    public static String insertMailMsg(Connection conn, javax.servlet.http.HttpServletRequest request, String companyid) throws ServiceException, ParseException, JSONException {
        org.apache.commons.fileupload.DiskFileUpload fu = new org.apache.commons.fileupload.DiskFileUpload();
        java.util.List fileItems = null;
        org.apache.commons.fileupload.FileItem fi = null;
        int sizeinmb = forummsgcomm.getmaxfilesize(conn, companyid);
        long maxsize = sizeinmb * 1024 * 1024;
        fu.setSizeMax(maxsize);
        boolean fileupload = false;
        java.util.HashMap arrParam = new java.util.HashMap();
        JSONObject jobj = new JSONObject();
        try {
            fileItems = fu.parseRequest(request);
        } catch (org.apache.commons.fileupload.FileUploadException e) {
            com.krawler.utils.json.base.JSONObject jerrtemp = new com.krawler.utils.json.base.JSONObject();
            if(e.getClass().getSimpleName().equalsIgnoreCase("SizeLimitExceededException")){
                jerrtemp.put("msg", "For attachments, maximum file size allowed is " + sizeinmb + "MB");
            } else {
                jerrtemp.put("msg", "Problem while uploading file.");
            }
            jerrtemp.put("Success", "Fail");
            jobj.append("data", jerrtemp);
            return jobj.toString();
        }
        for (java.util.Iterator k = fileItems.iterator(); k.hasNext();) {
            fi = (org.apache.commons.fileupload.FileItem) k.next();
            arrParam.put(fi.getFieldName(), fi.getString());
            if (!fi.isFormField()) {
                if (fi.getSize() > maxsize) {
                    com.krawler.utils.json.base.JSONObject jerrtemp = new com.krawler.utils.json.base.JSONObject();
                    jerrtemp.put("Success", "Fail");
                    jerrtemp.put("msg", "Attachment size should be upto " + sizeinmb + "MB");
                    jobj.append("data", jerrtemp);
                    return jobj.toString();
                }
                fileupload = true;
            }
        }
        String poster_id = request.getParameter("userId");
        String post_subject = StringUtil.serverHTMLStripper(java.net.URLDecoder.decode(arrParam.get("title").toString()));
        String post_text = java.net.URLDecoder.decode(arrParam.get("ptxt").toString());
        String folder = "1";
        Boolean readflag = false;
        String last_folder_id = "1";
        String reply_to = "";
        String sendflag = StringUtil.serverHTMLStripper(request.getParameter("sendflag"));
        String post_id1 = "";
        String to_id = "";
        String msgFor = "";
        String fid = "";
        String sendefolderpostid = "";
        Boolean done = false;
        String[] tos = {};
        if (sendflag.compareTo("reply") == 0) {
            post_id1 = StringUtil.serverHTMLStripper(request.getParameter("repto"));
            msgFor = getUserFromPost(conn, post_id1);
            fid = StringUtil.serverHTMLStripper(request.getParameter("fid"));
            if (fid.compareTo("3") != 0) {
                to_id = msgFor;
            }
        } else if (sendflag.compareTo("newmsg") == 0) {
            tos = request.getParameter("repto").split(";");
            fid = StringUtil.serverHTMLStripper(request.getParameter("fid"));
            msgFor = to_id;
        }
        int draft = Integer.parseInt(request.getParameter("draft"));

        String query;
        String post_id = "";
        String temp = "";
//                JSONObject jobj = new JSONObject();
        String usr = to_id;
        DbResults rs1 = null;
        DbResults rs2 = null;
        int numRows = 0;
        boolean uploaded = false;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Date d = new java.util.Date();
        java.sql.Timestamp sqlPostDate = new java.sql.Timestamp(d.getTime());
        UserDAO userDao = new UserDAOImpl();
        if (sendflag.compareTo("reply") == 0) {
            if (draft == 1) {
                post_id = UUID.randomUUID().toString();
                folder = getDraftId(poster_id);//"3";
                last_folder_id = "0";
                readflag = false;
                reply_to = "";
                if (fid.compareTo("3") != 0) {
                    if (StringUtil.isNullOrEmpty(to_id) || StringUtil.isNullOrEmpty(poster_id)) {
                        return KWLErrorMsgs.exSuccessFail;
                    }
                    query = "Insert into mailmessages (post_id, to_id, poster_id, post_subject, post_text, post_time, flag, folder, reply_to, last_folder_id, readflag) values ( ?, ?, ?, ?, ?,?,false, ?, ?, ?, ?)";
                    post_id = UUID.randomUUID().toString();
                    numRows = DbUtil.executeUpdate(conn, query, new Object[]{
                                post_id, to_id, poster_id, post_subject, post_text, sqlPostDate,
                                folder, reply_to, last_folder_id, readflag});

                } else {
                    if (StringUtil.isNullOrEmpty(post_id1)) {
                        return KWLErrorMsgs.exSuccessFail;
                    }
                    query = "Update mailmessages set post_subject=?, post_text=? where post_id=?";

                    numRows = DbUtil.executeUpdate(conn, query, new Object[]{
                                post_subject, post_text, post_id1});
                }
            } else if (draft == 2) {
                if (StringUtil.isNullOrEmpty(post_id1) || /*StringUtil.isNullOrEmpty(subdomain) || StringUtil.isNullOrEmpty(post_id) ||*/ StringUtil.isNullOrEmpty(to_id) || StringUtil.isNullOrEmpty(poster_id) /*|| StringUtil.isNullOrEmpty(reply_to)*/) {
                    return KWLErrorMsgs.exSuccessFail;
                }

//                query = "SELECT users.userid FROM users inner join userlogin on users.userid=userlogin.userid where username=? and users.companyid = ?;";
//                rs2 = DbUtil.executeQuery(conn, query, new Object[]{post_id1, companyid});
//
//                if (rs2.next()) {
                    to_id = msgFor;
//                }
                query = "Insert into mailmessages (post_id, to_id, poster_id, post_subject, post_text, post_time, flag, folder, reply_to, last_folder_id, readflag) values ( ?, ?, ?, ?, ?, ?,false, ?, ?, ?, ?)";
                post_id = UUID.randomUUID().toString();
                numRows = DbUtil.executeUpdate(conn, query, new Object[]{
                            post_id, to_id, poster_id, post_subject, post_text, sqlPostDate,
                            folder, reply_to, last_folder_id, readflag});

                if (fileupload) {
                    forummsgcomm.doPost(conn, fileItems, post_id, sqlPostDate, poster_id, post_id);
                    uploaded = true;
                }
                post_id = UUID.randomUUID().toString();
                folder = "0";
                last_folder_id = "0";
                readflag = false;
                reply_to = "";
                query = "Insert into mailmessages (post_id, to_id, poster_id, post_subject, post_text, post_time, flag, folder, reply_to, last_folder_id, readflag) values ( ?, ?, ?, ?, ?, ?,false, ?, ?, ?, ?)";

                numRows = DbUtil.executeUpdate(conn, query, new Object[]{
                            post_id, to_id, poster_id, post_subject, post_text, sqlPostDate,
                            folder, reply_to, last_folder_id, readflag});
                if (fileupload) {
                    forummsgcomm.doPost(conn, fileItems, post_id, sqlPostDate, poster_id, post_id);
                }

            } else {
                query = "SELECT poster_id FROM mailmessages where post_id=?";
                DbResults rs = DbUtil.executeQuery(conn, query, post_id1);
                while (rs.next()) {
                    to_id = rs.getString(1);
                }
            }
        } else {
            if (draft != 1) {
                if (draft == 3) {
                    for(int t = 0; t<tos.length; t++){
                        to_id = StringUtil.serverHTMLStripper(tos[t]);
                        if (to_id.contains("@")) {
                            query = "SELECT userid FROM users where emailid=?;";
                            rs2 = DbUtil.executeQuery(conn, query, to_id);

                            if (rs2.next()) {
                                to_id = (rs2.getString(1));
                            } else {
                                usr = to_id;
                                to_id = "-1";   //for invalid username
                            }
                        } else {
                            query = "SELECT users.userid FROM users inner join userlogin on users.userid = userlogin.userid where userlogin.username=? and users.companyid = ?;";
                            rs2 = DbUtil.executeQuery(conn, query, new Object[]{to_id, companyid});

                            if (rs2.next()) {
                                to_id = (rs2.getString(1));
                            } else {
                                usr = AuthHandler.getUserName(conn, to_id);
                                if(StringUtil.isNullOrEmpty(usr)){
                                    usr = to_id;
                                    to_id = "-1";   //for invalid username
                                }
                            }
                        }
                        if (to_id.compareTo("-1") != 0) {
                            if (StringUtil.isNullOrEmpty(to_id) || StringUtil.isNullOrEmpty(poster_id)) {
                                return KWLErrorMsgs.exSuccessFail;
                            }
                            if(fid.compareTo("3") != 0 && t == tos.length - 1){
                                folder = "1";
                                query = "Insert into mailmessages (post_id, to_id, poster_id, post_subject, post_text, post_time, flag, folder, reply_to, last_folder_id, readflag) values ( ?, ?, ?, ?, ?, ?,false, ?, ?, ?, ?)";
                                post_id = UUID.randomUUID().toString();
                                numRows = DbUtil.executeUpdate(conn, query, new Object[]{
                                post_id, to_id, poster_id, post_subject, post_text, sqlPostDate,
                                        folder, reply_to, last_folder_id, readflag});
                            }
                            folder = "0";
                            query = "Insert into mailmessages (post_id, to_id, poster_id, post_subject, post_text, post_time, flag, folder, reply_to, last_folder_id, readflag) values ( ?, ?, ?, ?, ?, ?,false, ?, ?, ?, ?)";
                            post_id = UUID.randomUUID().toString();
                            sendefolderpostid = post_id;
                            numRows = DbUtil.executeUpdate(conn, query, new Object[]{
                                        post_id, to_id, poster_id, post_subject, post_text, sqlPostDate,
                                        folder, reply_to, last_folder_id, readflag});

                            //post_id = UUID.randomUUID().toString();
                            if (fileupload) {
                                forummsgcomm.doPost(conn, fileItems, post_id, sqlPostDate, poster_id, post_id);
                                uploaded = true;
                            }
                            last_folder_id = "0";
                            readflag = false;
                            reply_to = "";
                            done = true;
                        }
                        if (fid.compareTo("3") == 0 && done == true) {//move to this users sent mails
                            post_id = request.getParameter("postid");
                            String tempfolder = "1";
                            DbResults rstemp = null;
                            query = "select poster_id from mailmessages where post_id=?";
                            rstemp = DbUtil.executeQuery(conn, query, new Object[]{post_id});
                            if (rstemp.next()) {

                                query = "update mailmessages set to_id=?, poster_id=?, post_subject=?, post_text=?, post_time=?, flag=false, folder=?, reply_to=?, last_folder_id=?, readflag=? where post_id=?";

                                int tempnumrows = DbUtil.executeUpdate(conn, query, new Object[]{
                                            to_id, poster_id, post_subject, post_text, sqlPostDate, tempfolder,
                                            reply_to, last_folder_id, readflag, post_id});
                                if (tempnumrows == 0) {
                                    if (to_id.compareTo("-1") == 0) {
                                        com.krawler.utils.json.base.JSONObject jtemp = new com.krawler.utils.json.base.JSONObject();
                                        jtemp.put("Success", "userfail");
                                        jtemp.put("Subject", usr);
                                        jobj.append("data", jtemp);
                                        return jobj.toString();
                                    } else {
                                        return KWLErrorMsgs.exSuccessFail;
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                for(int t = 0; t<tos.length; t++){
                    to_id = StringUtil.serverHTMLStripper(tos[t]);
                    post_id = request.getParameter("postid");
                    folder = getDraftId(poster_id);//"3";
                    last_folder_id = getDraftId(poster_id);

                    readflag = false;
                    reply_to = "";
                    query = "SELECT users.userid FROM users inner join userlogin on users.userid = userlogin.userid where userlogin.username=? and users.companyid = ?;";
                    rs2 = DbUtil.executeQuery(conn, query, new Object[]{to_id, companyid});

                    if (rs2.next()) {
                        to_id = (rs2.getString(1));
                    } else {
                        to_id = poster_id;
                    }
                    if (done == false) {
                        if (to_id.compareTo("-1") != 0) {
                            if (StringUtil.isNullOrEmpty(to_id) || StringUtil.isNullOrEmpty(poster_id)) {
                                return KWLErrorMsgs.exSuccessFail;
                            }
                            DbResults rstemp = null;
                            query = "select poster_id from mailmessages where post_id=?";
                            rstemp = DbUtil.executeQuery(conn, query, new Object[]{post_id});
                            if (rstemp.next()) {

                                query = "update mailmessages set to_id=?, poster_id=?, post_subject=?, post_text=?, post_time=?, flag=false, folder=?, reply_to=?, last_folder_id=?, readflag=? where post_id=?";

                                numRows = DbUtil.executeUpdate(conn, query, new Object[]{
                                            to_id, poster_id, post_subject, post_text, sqlPostDate, folder,
                                            reply_to, last_folder_id, readflag, post_id});

                            } else {

                                String tpost_id = UUID.randomUUID().toString();
                                folder = getDraftId(poster_id);//"3";
                                last_folder_id = "0";
                                readflag = false;
                                reply_to = "";
                                if (fid.compareTo("3") != 0) {
                                    if (StringUtil.isNullOrEmpty(to_id) || StringUtil.isNullOrEmpty(poster_id)) {
                                        return KWLErrorMsgs.exSuccessFail;
                                    }
                                    query = "Insert into mailmessages (post_id, to_id, poster_id, post_subject, post_text, post_time, flag, folder, reply_to, last_folder_id, readflag) values ( ?, ?, ?, ?, ?,?,false, ?, ?, ?, ?)";
                                    numRows = DbUtil.executeUpdate(conn, query, new Object[]{
                                                tpost_id, to_id, poster_id, post_subject, post_text, sqlPostDate,
                                                folder, reply_to, last_folder_id, readflag});
                                }
                            }
                        }
                    }
                }
            }
        }
        if (fileupload && !uploaded) {
            forummsgcomm.doPost(conn, fileItems, post_id, sqlPostDate, poster_id, sendefolderpostid);
        }
        if (numRows == 0) {
            if (to_id.compareTo("-1") == 0) {
                com.krawler.utils.json.base.JSONObject jtemp = new com.krawler.utils.json.base.JSONObject();
                jtemp.put("Success", "userfail");
                jtemp.put("Subject", usr);
                jobj.append("data", jtemp);
                return jobj.toString();
            } else {
                return KWLErrorMsgs.exSuccessFail;
            }
        } else {
            String dateTime = "";
            String UserName = "";
            String Image = "";
            query = "SELECT userlogin.username,image FROM users inner join userlogin on users.userid=userlogin.userid where users.userid=?;";
            rs2 = DbUtil.executeQuery(conn, query, poster_id);

            if (rs2.next()) {
                UserName = (rs2.getString(1));
                Image = (rs2.getString(2));
            }
            if (draft == 1 && sendflag.compareTo("reply") == 0) {
                query = "SELECT post_time FROM mailmessages where post_id=?;";
                rs1 = DbUtil.executeQuery(conn, query, post_id1);

                if (rs1.next()) {
                    dateTime = (rs1.getObject(1).toString());
                }
            } else {
                query = "SELECT post_time FROM mailmessages where post_id=?;";
                rs1 = DbUtil.executeQuery(conn, query, post_id);

                if (rs1.next()) {
                    dateTime = (rs1.getObject(1).toString());
                }
            }
            String userTime = Timezone.toCompanyTimezone(conn, sqlPostDate.toString(), companyid);
            java.util.Date tempdate = sdf.parse(userTime);
            java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat(
                    "yyyy-MM-dd h:mm a");
            JSONStringer j = new JSONStringer();
            com.krawler.utils.json.base.JSONObject jtemp = new com.krawler.utils.json.base.JSONObject();
            String success = "Success";
            if (folder.compareTo("3") == 0) {
                success = "Draft";
            }
            jtemp.put("Success", success);
            jtemp.put("post_time", sdf1.format(tempdate).toString());
            jtemp.put("flag", "false");
            jtemp.put("post_id", post_id);
            jtemp.put("post_subject", post_subject);
            jtemp.put("post_text", "");
            jtemp.put("poster_id", UserName);
            jtemp.put("readflag", "0");
            jtemp.put("imgsrc", Image);
            jtemp.put("senderid", poster_id);
            jobj.append("data", jtemp);
        /*temp = j.object().key("Success").value("Success").key("post_time")
        .value(sdf1.format(tempdate).toString()).key("flag").value(
        "false").key("post_id").value(post_id).key(
        "post_subject").value(post_subject)
        .key("post_text").value("").key("poster_id")
        .value(UserName).key("readflag").value("0").key("imgsrc")
        .value(Image).key("senderid").value(poster_id).endObject()
        .toString();*/


        }
        return jobj.toString();
    }

    public static String getMailFolders(Connection conn, String userid)
            throws ServiceException, JSONException {
        String returnString = null;
        String query = null;
        Object[] params = {null};
        DbResults rs = null;
        query = "Select mailmsgfoldermap.folder_id, mailmsgfoldermap.folder_name from mailmsgfoldermap inner join mailuserfoldersmap on mailmsgfoldermap.folder_id = mailuserfoldersmap.folderid where mailuserfoldersmap.userid = ?";
        params[0] = userid;
        rs = DbUtil.executeQuery(conn, query, params);
        String str = "";
        returnString = "[ ";
        while (rs.next()) {
            com.krawler.utils.json.base.JSONObject jtemp = new com.krawler.utils.json.base.JSONObject();
            jtemp.put("folderid", rs.getString(1));
            jtemp.put("foldername", rs.getString(2));
            returnString += jtemp.toString() + ",";
        }
        returnString = returnString.substring(0, returnString.length() - 1);
        returnString += "]";

        return returnString;
    }

    public static String getUserFromPost(Connection conn, String postId)
            throws ServiceException {
        String query = "SELECT poster_id FROM mailmessages where post_id=?";
        String to_id = "";
        DbResults rs = DbUtil.executeQuery(conn, query, postId);
        while (rs.next()) {
            to_id = rs.getString(1);
        }

        return to_id;
    }

    public static int fetchMailCount(Connection conn, String loginid, String i,
            int offset, int limit) throws ServiceException {
        int tCount = 0;
        String query = null;
        DbResults rs = null;
        try {
            if (i.compareTo("0") == 0) {

                query = "Select count(post_id) from mailmessages inner join users on users.userid = mailmessages.poster_id where folder = '0' and to_id = ?";
                rs = DbUtil.executeQuery(conn, query, loginid);
            } else if (i.compareTo("1") == 0) {

                query = "Select count(post_id) from mailmessages inner join users on users.userid = mailmessages.to_id where folder = '1' and poster_id = ?";
                rs = DbUtil.executeQuery(conn, query, loginid);
            } else if (i.compareTo("4") == 0)// Starred Items
            {
                query = "Select count(*) from (Select post_id from mailmessages inner join users on users.userid = mailmessages.poster_id where to_id = ? and flag = true and (folder in ('0',?) or folder in (select folderid from mailuserfoldersmap where userid =?))  union Select post_id from mailmessages inner join users on users.userid = mailmessages.to_id where (folder in ('1','3',?) or folder in (select folderid from mailuserfoldersmap where userid =?)) and poster_id =? and flag = true) as tmp";
                rs = DbUtil.executeQuery(conn, query, new Object[]{loginid, getDelId(loginid, "0"),
                            loginid, getDelId(loginid, "1"), loginid, loginid});
//				query = "Select count(post_id) from mailmessages where post_id IN (Select post_id from mailmessages inner join users on users.userid = mailmessages.poster_id where to_id = ? and flag = true and folder!=2  union Select post_id from mailmessages inner join users on users.userid = mailmessages.to_id where folder = '1' and poster_id = ? and flag = true)";
//				rs = DbUtil.executeQuery(conn, query, new Object[] { loginid,
//						loginid });
            } else if (i.compareTo("3") == 0) {
                query = "Select count(post_id) from mailmessages inner join users on users.userid = mailmessages.to_id where folder = ? and poster_id = ?";
                rs = DbUtil.executeQuery(conn, query, new Object[]{"3",
                            loginid});


            } else {
//                                if(i.equals("3")){
//                                    i=getDraftId(loginid);
//                                }
                String i1 = i;
                if (i.equals("2")) {
                    i = getDelId(loginid, "0");
                    i1 = getDelId(loginid, "1");
                }
                query = "Select count(post_id) from mailmessages where post_id IN (Select post_id from mailmessages inner join users on users.userid = mailmessages.poster_id where folder = ? and to_id = ? union Select post_id from mailmessages inner join users on users.userid = mailmessages.to_id where folder = ? and poster_id =?)";
                rs = DbUtil.executeQuery(conn, query, new Object[]{i,
                            loginid, i1, loginid});
            }

            while (rs.next()) {
                tCount = rs.getInt(1);
            }
        } catch (ServiceException e) {
        }
        return tCount;
    }

    public static String fetchMail(Connection conn, String loginid, String i,
            int offset, int limit) throws ServiceException {
        String query = null;
        ResultSet rs = null;
        KWLJsonConverter KWL = new KWLJsonConverter();
        PreparedStatement stmt = null;
        String tempString = null;
        try {
            String companyid = CompanyHandler.getCompanyByUser(conn, loginid);
            String superAdmin = com.krawler.esp.utils.ConfigReader.getinstance().get("deskerasuperuser");
            if (i.compareTo("0") == 0) {
                query = "Select post_id ,concat(fname,' ',lname) as post_fullname,  userlogin.username as poster_id , '' as post_text , post_subject ," +
                        " post_time , flag, readflag, image as imgsrc, users.userid as senderid,folder  from mailmessages inner join users on users.userid = mailmessages.poster_id inner join userlogin on users.userid=userlogin.userid where folder = ? and to_id = ? ORDER BY post_time DESC LIMIT ? OFFSET ?";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, i);
                stmt.setString(2, loginid);
                stmt.setInt(3, limit);
                stmt.setInt(4, offset);

            } else if (i.compareTo("1") == 0) {
                query = "Select post_id ,concat(fname,' ',lname) as post_fullname, userlogin.username as poster_id, '' as post_text , post_subject , post_time , flag ," +
                        " readflag, image as imgsrc, users.userid as senderid,folder from mailmessages inner join users on users.userid = mailmessages.to_id inner join userlogin on users.userid = userlogin.userid where folder = ? and poster_id = ? ORDER BY post_time DESC LIMIT ? OFFSET ?";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, i);
                stmt.setString(2, loginid);
                stmt.setInt(3, limit);
                stmt.setInt(4, offset);

            } else if (i.compareTo("4") == 0)// Starred Items
            {
                query = "Select * from (Select folder,post_id , concat(fname,' ',lname) as post_fullname, userlogin.username as poster_id , '' as post_text , post_subject , post_time , flag, readflag, image as imgsrc, users.userid as senderid from mailmessages inner join users on users.userid = mailmessages.poster_id inner join userlogin on users.userid = userlogin.userid " +
                        "where to_id = ? and flag = true and (folder in ('0',?) or folder in (select folderid from mailuserfoldersmap where users.userid =?)) " +
                        " union Select folder,post_id , concat(fname,' ',lname) as post_fullname, userlogin.username as poster_id , '' as post_text , post_subject , post_time , flag, readflag, image as imgsrc, users.userid as senderid from mailmessages inner join users on users.userid = mailmessages.to_id inner join userlogin on users.userid =userlogin.userid " +
                        " where (folder in ('1','3',?) or folder in (select folderid from mailuserfoldersmap where users.userid =?)) and poster_id =? and flag = true) as tmp  ORDER BY post_time desc  LIMIT ? OFFSET ?";
                //				query = "Select * from ((Select post_id , concat(fname,' ',lname) as post_fullname, username as poster_id , '' as post_text , post_subject , post_time , flag, readflag, image as imgsrc, userid as senderid from mailmessages inner join users on users.userid = mailmessages.poster_id where to_id = ? and flag = true and folder != 2 ORDER BY post_time DESC) union (Select post_id ,concat(fname,' ',lname) as post_fullname, username as poster_id , '' as post_text , post_subject , post_time , flag, readflag, image as imgsrc, userid as senderid from mailmessages inner join users on users.userid = mailmessages.to_id where folder = '1' and poster_id = ? and flag = true ORDER BY post_time DESC)) as temp  ORDER BY post_time desc  LIMIT ? OFFSET ?";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, loginid);
                stmt.setString(2, getDelId(loginid, "0"));
                stmt.setString(3, loginid);
                stmt.setString(4, getDelId(loginid, "1"));
                stmt.setString(5, loginid);
                stmt.setString(6, loginid);
                stmt.setInt(7, limit);
                stmt.setInt(8, offset);

            } else if (i.compareTo("3") == 0) {
                query = "Select post_id ,concat(fname,' ',lname) as post_fullname, userlogin.username as poster_id , post_text , post_subject , post_time , flag, readflag, image as imgsrc, users.userid as senderid,folder from mailmessages inner join users on users.userid = mailmessages.to_id inner join userlogin on users.userid= userlogin.userid where folder = ? and poster_id = ? ORDER BY post_time desc  LIMIT ? OFFSET ?";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, "3");
                stmt.setString(2, loginid);
                stmt.setInt(3, limit);
                stmt.setInt(4, offset);

            } else {
                String folderid = i;
                query = "Select * from ((Select post_id , concat(fname,' ',lname) as post_fullname, userlogin.username as poster_id , '' as post_text , post_subject , post_time , flag, readflag, image as imgsrc, users.userid as senderid, folder from mailmessages inner join users on users.userid = mailmessages.poster_id inner join userlogin on users.userid =userlogin.userid where folder = ? and to_id = ? ORDER BY post_time DESC)" +
                        " union (Select post_id ,concat(fname,' ',lname) as post_fullname, userlogin.username as poster_id , '' as post_text , post_subject , post_time , flag, readflag, image as imgsrc, users.userid as senderid, folder from mailmessages inner join users on users.userid = mailmessages.to_id inner join userlogin on users.userid = userlogin.userid where folder = ? and poster_id = ? ORDER BY post_time DESC)) as temp  ORDER BY post_time desc LIMIT ? OFFSET ?";
                stmt = conn.prepareStatement(query);
                if (i.equals("2")) {
                    folderid = getDelId(loginid, "0");
                }
                stmt.setString(1, folderid);
                stmt.setString(2, loginid);
                if (i.equals("2")) {
                    folderid = getDelId(loginid, "1");
                }
                stmt.setString(3, folderid);
                stmt.setString(4, loginid);
                stmt.setInt(5, limit);
                stmt.setInt(6, offset);

            }

            rs = stmt.executeQuery();
            JSONObject jobj = new JSONObject();
            JSONObject jtemp = new JSONObject();
            while (rs.next()) {
                jtemp.put("post_id", rs.getObject("post_id"));
                jtemp.put("poster_id", rs.getObject("poster_id"));
                jtemp.put("post_fullname", rs.getObject("post_fullname"));
                jtemp.put("post_text", java.net.URLEncoder.encode(rs.getString("post_text")));
                jtemp.put("post_subject", java.net.URLEncoder.encode(rs.getString("post_subject")));
                String postTime = Timezone.toCompanyTimezone(conn, rs.getString("post_time"), companyid);
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                java.util.Date d = new java.util.Date();
//              postTime = Timezone.dateTimeRenderer(conn, Timezone.toUserTimezone(conn, sdf.format(d), loginid), postTime, loginid);
                jtemp.put("post_time", postTime);//Timezone.toUserTimezone(conn,rs.getString("post_time"),Timezone.getTimeZone(conn,loginid))
                jtemp.put("flag", rs.getObject("flag"));
                jtemp.put("readflag", rs.getObject("readflag"));
                String img = StringUtil.getAppsImagePath(rs.getString("senderid"), 35);
                jtemp.put("imgsrc", img);
                jtemp.put("senderid", rs.getObject("senderid"));
                jtemp.put("folder", rs.getObject("folder"));
                jtemp.put("deskSuperuser", StringUtil.equal(rs.getObject("senderid").toString(), superAdmin) ? "true" : "false");
                if (i.compareTo("4") == 0) {
                    jtemp.put("folder", rs.getObject("folder"));
                }
                jobj.append("data", jtemp);

                jtemp = new JSONObject();
            }
            int tCount = Mail.fetchMailCount(conn, loginid, i, offset, limit);
            jobj.put("totalCount", tCount);
            if (tCount == 0) {
                jobj.put("data", "");
            }
            tempString = jobj.toString();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("Mail.fetchMail", e);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("Mail.fetchMail", e);
        } finally {
            DbPool.closeStatement(stmt);
        }
        return tempString;
    }

    public static int searchMailCount(Connection conn, String searchtext1, String searchtext,
            String folder_id, String loginid, int offset, int limit)
            throws ServiceException {
        int tCount = 0;
        DbResults rs = null;
        try {
//			String sqlquery = "Select count(*) as cnt from ("
//					+ "(Select post_id , fname as poster_id , ''"
//					+ " as post_text , post_subject , post_time , flag, readflag, image as imgsrc, userid as senderid"
//					+ " from mailmessages inner join users on users.userid = mailmessages.poster_id where "
//					+ "to_id=?  and (post_text LIKE ? or post_subject LIKE ? or users.username LIKE ?) ) "
//					+ "union (Select post_id , fname as poster_id , '' as post_text , post_subject , post_time , flag, readflag, image as imgsrc, userid as senderid"
//					+ " from mailmessages inner join users on users.userid = mailmessages.to_id where"
//					+ " poster_id = ? and (post_text LIKE ? or post_subject LIKE ? or users.username LIKE ?) )) as temp";
//                     String sqlquery = "Select count(*) as cnt from (" +
//                            "(Select post_id , username as poster_id,concat_ws(' ',users.fname,users.lname) as  post_fullname, '' as post_text , post_subject , post_time , flag, readflag, image as imgsrc, userid as senderid, folder from mailmessages inner join users on users.userid = mailmessages.poster_id where to_id=? and folder=0  and ((post_text LIKE ? or post_subject LIKE ? or users.fname LIKE ? or users.lname LIKE ?) or (post_text LIKE ? or post_subject LIKE ? or users.fname LIKE ? or users.lname LIKE ?)) ORDER BY post_time DESC) " +
//                            "UNION (Select post_id , username as poster_id ,concat_ws(' ',users.fname,users.lname) as  post_fullname, '' as post_text , post_subject , post_time , flag, readflag, image as imgsrc, userid as senderid, folder from mailmessages inner join users on users.userid = mailmessages.to_id where poster_id = ? and folder =1 and ((post_text LIKE ? or post_subject LIKE ? or users.fname LIKE ? or users.lname LIKE ?) or (post_text LIKE ? or post_subject LIKE ? or users.fname LIKE ? or users.lname LIKE ?)) ORDER BY post_time DESC) " +
//                            "UNION (Select post_id , username as poster_id ,concat_ws(' ',users.fname,users.lname) as  post_fullname, '' as post_text , post_subject , post_time , flag, readflag, image as imgsrc, userid as senderid, folder from mailmessages inner join users on users.userid = mailmessages.poster_id where to_id = ? and folder not in(0, 1) and ((post_text LIKE ? or post_subject LIKE ? or users.fname LIKE ? or users.lname LIKE ?) or (post_text LIKE ? or post_subject LIKE ? or users.fname LIKE ? or users.lname LIKE ?)) ORDER BY post_time DESC) " +
//                            "UNION (Select post_id , username as poster_id ,concat_ws(' ',users.fname,users.lname) as  post_fullname, '' as post_text , post_subject , post_time , flag, readflag, image as imgsrc, userid as senderid, folder from mailmessages inner join users on users.userid = mailmessages.to_id where poster_id = ? and folder not in(0, 1) and ((post_text LIKE ? or post_subject LIKE ? or users.fname LIKE ? or users.lname LIKE ?) or (post_text LIKE ? or post_subject LIKE ? or users.fname LIKE ? or users.lname LIKE ?)) ORDER BY post_time DESC))  " +
//                            "as temp ";
//
//                    rs = DbUtil.executeQuery(conn, sqlquery, new Object[] { loginid,
//					searchtext,searchtext, searchtext, searchtext,searchtext1,searchtext1,searchtext1,searchtext1,
//                                        loginid,searchtext,searchtext, searchtext, searchtext,searchtext1,searchtext1,searchtext1,searchtext1,
//                                        loginid,searchtext,searchtext, searchtext, searchtext,searchtext1,searchtext1,searchtext1,searchtext1,
//                                        loginid,searchtext,searchtext, searchtext, searchtext,searchtext1,searchtext1,searchtext1,searchtext1});
//                     rs = DbUtil.executeQuery(conn, sqlquery, new Object[] { loginid,
//					searchtext, searchtext, searchtext, loginid, searchtext,
//					searchtext, searchtext });
            String sqlquery = "select count(*) as cnt from((Select post_id , username as poster_id,concat_ws(' ',users.fname,users.lname) as  post_fullname, '' as post_text , post_subject , post_time , flag, readflag, image as imgsrc, users.userid as senderid, folder from mailmessages inner join users on users.userid = mailmessages.poster_id inner join userlogin on users.userid=userlogin.userid where " +
                    " to_id=? and (folder in ('0',?,?) or folder in (select folderid from mailuserfoldersmap where userid =?))  and ((post_text LIKE ? or post_subject LIKE ? or users.fname LIKE ? or users.lname LIKE ?) or (post_text LIKE ? or post_subject LIKE ? or users.fname LIKE ? or users.lname LIKE ?)) ORDER BY post_time DESC) UNION " +
                    "(Select post_id , username as poster_id ,concat_ws(' ',users.fname,users.lname) as  post_fullname, '' as post_text , post_subject , post_time , flag, readflag, image as imgsrc, users.userid as senderid, folder from mailmessages inner join users on users.userid = mailmessages.to_id inner join userlogin on users.userid =userlogin.userid " +
                    "where poster_id = ? and (folder in (1,?,?) or folder in (select folderid from mailuserfoldersmap where users.userid =?)) and ((post_text LIKE ? or post_subject LIKE ? or users.fname LIKE ? or users.lname LIKE ?) or (post_text LIKE ? or post_subject LIKE ? or users.fname LIKE ? or users.lname LIKE ?)) ORDER BY post_time DESC)) " +
                    "as temp";
            rs = DbUtil.executeQuery(conn, sqlquery, new Object[]{loginid, getDraftId(loginid), getDelId(loginid, "0"), loginid,
                        searchtext, searchtext, searchtext, searchtext, searchtext1, searchtext1, searchtext1, searchtext1,
                        loginid, getDraftId(loginid), getDelId(loginid, "1"), loginid,
                        searchtext, searchtext, searchtext, searchtext, searchtext1, searchtext1, searchtext1, searchtext1});

            while (rs.next()) {
                tCount = rs.getInt("cnt");
            }

        } // rs.close();
        catch (ServiceException e) {
            throw ServiceException.FAILURE("Mail.searchMailCount", e);
        }
        return tCount;
    }

    public static String searchMail(Connection conn, String searchtext1,
            String searchtext, String folder_id, String loginid, int offset,
            int limit) throws ServiceException, JSONException {
        String tempString = null;
        ResultSet rs = null;
        KWLJsonConverter KWL = new KWLJsonConverter();
        PreparedStatement stmt = null;
        try {
            String companyid = CompanyHandler.getCompanyByUser(conn, loginid);
            String sqlquery1 = "Select * from ((Select post_id , username as poster_id,concat_ws(' ',users.fname,users.lname) as  post_fullname, '' as post_text , post_subject , post_time , flag, readflag, image as imgsrc, users.userid as senderid, folder from mailmessages inner join users on users.userid = mailmessages.poster_id inner join userlogin on users.userid=userlogin.userid where " +
                    " to_id=? and (folder in ('0',?,?) or folder in (select folderid from mailuserfoldersmap where users.userid =?))  and ((post_text LIKE ? or post_subject LIKE ? or users.fname LIKE ? or users.lname LIKE ?) or (post_text LIKE ? or post_subject LIKE ? or users.fname LIKE ? or users.lname LIKE ?)) ) UNION " +
                    " (Select post_id , username as poster_id ,concat_ws(' ',users.fname,users.lname) as  post_fullname, '' as post_text , post_subject , post_time , flag, readflag, image as imgsrc, users.userid as senderid, folder from mailmessages inner join users on users.userid = mailmessages.to_id inner join userlogin on users.userid=userlogin.userid " +
                    " where poster_id = ? and (folder in (1,?,?) or folder in (select folderid from mailuserfoldersmap where users.userid =?)) and ((post_text LIKE ? or post_subject LIKE ? or users.fname LIKE ? or users.lname LIKE ?) or (post_text LIKE ? or post_subject LIKE ? or users.fname LIKE ? or users.lname LIKE ?))))" +
                    " as temp ORDER BY post_time DESC LIMIT  ? OFFSET ?";
            stmt = conn.prepareStatement(sqlquery1);
            stmt.setString(1, loginid);
            stmt.setString(2, getDraftId(loginid));
            stmt.setString(3, getDelId(loginid, "0"));
            stmt.setString(4, loginid);
            stmt.setString(5, searchtext);
            stmt.setString(6, searchtext);
            stmt.setString(7, searchtext);
            stmt.setString(8, searchtext);
            stmt.setString(9, searchtext1);
            stmt.setString(10, searchtext1);
            stmt.setString(11, searchtext1);
            stmt.setString(12, searchtext1);
            stmt.setString(13, loginid);
            stmt.setString(14, getDraftId(loginid));
            stmt.setString(15, getDelId(loginid, "1"));
            stmt.setString(16, loginid);
            stmt.setString(17, searchtext);
            stmt.setString(18, searchtext);
            stmt.setString(19, searchtext);
            stmt.setString(20, searchtext);
            stmt.setString(21, searchtext1);
            stmt.setString(22, searchtext1);
            stmt.setString(23, searchtext1);
            stmt.setString(24, searchtext1);
            stmt.setInt(25, limit);
            stmt.setInt(26, offset);

//			 String sqlquery = "Select * from (" +
//                            "(Select post_id , username as poster_id,concat_ws(' ',users.fname,users.lname) as  post_fullname, '' as post_text , post_subject , post_time , flag, readflag, image as imgsrc, userid as senderid, folder from mailmessages inner join users on users.userid = mailmessages.poster_id where to_id=? and folder=0  and ((post_text LIKE ? or post_subject LIKE ? or users.fname LIKE ? or users.lname LIKE ?) or (post_text LIKE ? or post_subject LIKE ? or users.fname LIKE ? or users.lname LIKE ?)) ORDER BY post_time DESC) " +
//                            "UNION (Select post_id , username as poster_id ,concat_ws(' ',users.fname,users.lname) as  post_fullname, '' as post_text , post_subject , post_time , flag, readflag, image as imgsrc, userid as senderid, folder from mailmessages inner join users on users.userid = mailmessages.to_id where poster_id = ? and folder =1 and ((post_text LIKE ? or post_subject LIKE ? or users.fname LIKE ? or users.lname LIKE ?) or (post_text LIKE ? or post_subject LIKE ? or users.fname LIKE ? or users.lname LIKE ?)) ORDER BY post_time DESC) " +
//                            "UNION (Select post_id , username as poster_id ,concat_ws(' ',users.fname,users.lname) as  post_fullname, '' as post_text , post_subject , post_time , flag, readflag, image as imgsrc, userid as senderid, folder from mailmessages inner join users on users.userid = mailmessages.poster_id where to_id = ? and folder not in(0, 1) and ((post_text LIKE ? or post_subject LIKE ? or users.fname LIKE ? or users.lname LIKE ?) or (post_text LIKE ? or post_subject LIKE ? or users.fname LIKE ? or users.lname LIKE ?)) ORDER BY post_time DESC) " +
//                            "UNION (Select post_id , username as poster_id ,concat_ws(' ',users.fname,users.lname) as  post_fullname, '' as post_text , post_subject , post_time , flag, readflag, image as imgsrc, userid as senderid, folder from mailmessages inner join users on users.userid = mailmessages.to_id where poster_id = ? and folder not in(0, 1) and ((post_text LIKE ? or post_subject LIKE ? or users.fname LIKE ? or users.lname LIKE ?) or (post_text LIKE ? or post_subject LIKE ? or users.fname LIKE ? or users.lname LIKE ?)) ORDER BY post_time DESC))  " +
//                            "as temp LIMIT  ? OFFSET ?";
//                        	stmt = conn.prepareStatement(sqlquery);
//				stmt.setString(1, loginid);
//				stmt.setString(2, searchtext);
//				stmt.setString(3, searchtext);
//                                stmt.setString(4, searchtext);
//                                stmt.setString(5, searchtext);
//                                stmt.setString(6, searchtext1);
//				stmt.setString(7, searchtext1);
//                                stmt.setString(8, searchtext1);
//                                stmt.setString(9, searchtext1);
//				stmt.setString(10, loginid);
//				stmt.setString(11, searchtext);
//				stmt.setString(12, searchtext);
//                                stmt.setString(13, searchtext);
//                                stmt.setString(14, searchtext);
//                                stmt.setString(15, searchtext1);
//				stmt.setString(16, searchtext1);
//                                stmt.setString(17, searchtext1);
//                                stmt.setString(18, searchtext1);
//                                stmt.setString(19, loginid);
//				stmt.setString(20, searchtext);
//				stmt.setString(21, searchtext);
//                                stmt.setString(22, searchtext);
//                                stmt.setString(23, searchtext);
//                                stmt.setString(24, searchtext1);
//				stmt.setString(25, searchtext1);
//                                stmt.setString(26, searchtext1);
//                                stmt.setString(27, searchtext1);
//                                stmt.setString(28, loginid);
//				stmt.setString(29, searchtext);
//				stmt.setString(30, searchtext);
//                                stmt.setString(31, searchtext);
//                                stmt.setString(32, searchtext);
//                                stmt.setString(33, searchtext1);
//				stmt.setString(34, searchtext1);
//                                stmt.setString(35, searchtext1);
//                                stmt.setString(36, searchtext1);
//				stmt.setInt(37, limit);
//				stmt.setInt(38, offset);
            rs = stmt.executeQuery();
            JSONObject jobj = new JSONObject();
            JSONObject jtemp = new JSONObject();
            while (rs.next()) {
                jtemp.put("post_id", rs.getObject("post_id"));
                jtemp.put("poster_id", rs.getObject("poster_id"));
                jtemp.put("post_text", java.net.URLEncoder.encode(rs.getString("post_text")));
                jtemp.put("post_subject", java.net.URLEncoder.encode(rs.getString("post_subject")));
                jtemp.put("post_time", Timezone.toCompanyTimezone(conn, rs.getObject("post_time").toString(), companyid));
                jtemp.put("post_fullname", rs.getObject("post_fullname"));
                jtemp.put("flag", rs.getObject("flag"));
                jtemp.put("readflag", rs.getObject("readflag"));
                jtemp.put("imgsrc", rs.getObject("imgsrc"));
                jtemp.put("senderid", rs.getObject("senderid"));
                jtemp.put("folder", rs.getObject("folder"));
                jobj.append("data", jtemp);

                jtemp = new JSONObject();
            }
            if (jobj.length() == 0) {
                tempString = "{data: {}}";
            } else {
                tempString = jobj.toString();
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("Mail.fetchMail", e);
        } finally {
            DbPool.closeStatement(stmt);
        }
        return tempString;
    }

    public static String UpdateMailReadflag(Connection conn, String post_id)
            throws ServiceException {
        String query = null;
        Object[] params = {true, post_id};

        query = "Update mailmessages set readflag= ? where post_id = ?";
        int numRows = DbUtil.executeUpdate(conn, query, params);
        if (numRows == 0) {
            post_id = "Failure";
        } else {
            post_id = "Success";
        }
        return post_id;
    }

    public static String getDraftId(String userid) {
        return "3";//"draft"+userid;
    }

    public static String getDelId(String userid, String lastFolder) {
        String folderid = "";
        if (lastFolder.equals("0")) {  //lastFolder = 0 means this mail is deleted from inbox
            folderid = "2";
        } else if (lastFolder.equals("1") || lastFolder.equals("3")) { //lastFolder = 1 means this mail is deleted from send items and lastFolder =3 means this mail is deleted from draft
            folderid = "4";
        }
        /*
         *  folderid 2 represent deleted msg which are recived by user
         *  folderid 4 represent deleted msg which are send by user or msg from draft.
         */
        return folderid;
    }
}
