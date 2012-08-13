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

import java.text.ParseException;
import java.util.UUID;

import com.krawler.common.service.ServiceException;
import com.krawler.database.DbPool;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.database.DbPool.Connection;
import com.krawler.utils.json.util.JSONStringer;
import com.krawler.utils.json.base.JSONObject;
import com.krawler.utils.json.base.JSONException;
import javax.servlet.http.HttpServletRequest;
import com.krawler.common.util.StringUtil;
import com.krawler.common.timezone.Timezone;
import com.krawler.esp.handlers.AuthHandler;
import com.krawler.esp.handlers.CompanyHandler;
import java.io.*;
import java.sql.Timestamp;

public class Forum {

    public static String deletePost(Connection conn, String ids[])
            throws ServiceException {
        String treeString = "";
        JSONObject r = new JSONObject();
        String query = null;
        try{
            for (int i = 0; i < ids.length; i++) {
                String id = ids[i];
                JSONObject t = new JSONObject();
                t.put("tid", id);
                if (id.contains("topic")) {
                    query = "Delete FROM krawlerforum_topics  where topic_id=?;";
                    id = id.substring(5);
                    DbUtil.executeUpdate(conn, query, id);
                } else {
                    query = "Delete FROM krawlerforum_posts  where post_id=?;";
                    DbUtil.executeUpdate(conn, query, id);
                }
                r.append("data", t);
            }
            r.put("success", true);
            treeString = r.toString();
        } catch(JSONException e){
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return treeString;
    }

    public static String fillTopicSubTree(Connection conn, String id, int level, String loginid)
            throws ServiceException, JSONException {

        DbResults rs = null;
        DbResults rs1 = null;
        DbResults rs2 = null;
        String treeString = "";
        level++;
        Boolean flag = false;
        Boolean ifread = false;
        String query = "";
        String query1 = "";
        String query2 = "";
        JSONObject jtemp = new JSONObject();
        query = "SELECT post_subject, post_time, concat(fname,' ',lname),post_text,krawlerforum_posts.post_id,userForumPostFlag.flag,userForumPostRead.ifread,users.image,post_poster " +
                "FROM krawlerforum_posts " +
                "left outer join userForumPostRead on userForumPostRead.post_id = krawlerforum_posts.post_id and userForumPostRead.userid = ?" +
                "left outer join userForumPostFlag on userForumPostFlag.post_id = krawlerforum_posts.post_id and userForumPostFlag.userid = ? " +
                "inner join users on users.userid = krawlerforum_posts.post_poster " +
                "where reply_to=? and reply_to_post=?";
        rs = DbUtil.executeQuery(conn, query, new Object[]{loginid, loginid, id, "-999"});

        while (rs.next()) {
            String Uid = rs.getString(9);
            String Pid = rs.getString(5);
            ifread = false;
            if (rs.getObject("ifread") != null) {
                ifread = true;
            }
            flag = false;
            if (rs.getObject("flag") != null) {
                flag = true;
            }
//			query1 = "Select ifread from userForumPostRead where post_id =? and userid=? ";
//			rs1 = DbUtil.executeQuery(conn, query1, new Object[] { Pid, loginid });
//			if (rs1.next()) {
//				ifread = true;
//			}
//                        flag=false;
//			query2 = "Select flag from userForumPostFlag where post_id =? and userid=? ";
//			rs2 = DbUtil.executeQuery(conn, query2, new Object[] { Pid, loginid });
//			if (rs2.next()) {
//				flag = true;
//			}

            String postTime = Timezone.toCompanyTimezone(conn, rs.getObject(2).toString(), CompanyHandler.getCompanyByUser(conn, loginid));
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date d = new java.util.Date();
//            postTime = Timezone.dateTimeRenderer(conn, Timezone.toUserTimezone(conn, sdf.format(d), loginid), postTime, loginid);
            jtemp.put("Subject", java.net.URLEncoder.encode(rs.getString(1)));
//                jtemp.put("Received", sdf1.format(tempdate).toString());
            jtemp.put("Received", postTime);//rs.getObject(2));
            jtemp.put("From", rs.getString(3));
            jtemp.put("Details", "");
            jtemp.put("Flag", flag);
            jtemp.put("ifread", ifread);
            jtemp.put("ID", Pid);
            jtemp.put("Parent", "topic" + id);
            jtemp.put("Level", level);
            String img = StringUtil.getAppsImagePath(Uid, 35);
            jtemp.put("Image", img);
            jtemp.put("User_Id", Uid);
            //treeString += "{Subject: '" + rs.getString(1) + "',Received: '" + sdf1.format(tempdate).toString() + "',From: '" + rs.getString(3) + "',Details: '',Flag:'" + flag + "',ID:'" + Pid + "',ifread: '" + ifread + "',Parent:'topic" + id + "',Level:'" + level + "',Image:'" + rs.getString(8) + "',User_Id:'" + Uid + "'}," + fillPostSubTree(conn, rs.getString(5), level, loginid);
            treeString += jtemp.toString() + "," + fillPostSubTree(conn, rs.getString(5), level, loginid);
        }

        return treeString;
    }

    public static String fillPostSubTree(Connection conn, String id, int level, String loginid)
            throws ServiceException, JSONException {
        DbResults rs = null;
        DbResults rs1 = null;
        DbResults rs2 = null;
        String treeString = "";
        level++;
        Boolean flag = false;
        Boolean ifread = false;
        String query = "";
        String query1 = "";
        String query2 = "";
        JSONObject jtemp = new JSONObject();
        query = "SELECT post_subject, post_time, concat(fname,' ',lname),post_text,krawlerforum_posts.post_id,userForumPostFlag.flag,userForumPostRead.ifread,users.image,post_poster FROM krawlerforum_posts " +
                "left outer join userForumPostRead on userForumPostRead.post_id = krawlerforum_posts.post_id and userForumPostRead.userid = ?" +
                "left outer join userForumPostFlag on userForumPostFlag.post_id = krawlerforum_posts.post_id and userForumPostFlag.userid = ? " +
                "inner join users on users.userid = krawlerforum_posts.post_poster " +
                "where reply_to_post=?;";
        rs = DbUtil.executeQuery(conn, query, new Object[]{loginid, loginid, id});

        while (rs.next()) {
            String Uid = rs.getString(9);
            String Pid = rs.getString(5);
            ifread = false;
            if (rs.getObject("ifread") != null) {
                ifread = true;
            }
            flag = false;
            if (rs.getObject("flag") != null) {
                flag = true;
            }

//			query1 = "Select ifread from userForumPostRead where post_id =? and userid=? ";
//			rs1 = DbUtil.executeQuery(conn, query1, new Object[] { Pid, loginid });
//			if (rs1.next()) {
//				ifread = true;
//			}
//			flag=false;
//			query2 = "Select flag from userForumPostFlag where post_id =? and userid=? ";
//			rs2 = DbUtil.executeQuery(conn, query2, new Object[] { Pid, loginid });
//			if (rs2.next()) {
//				flag = true;
//			}
            String postTime = Timezone.toCompanyTimezone(conn, rs.getObject(2).toString(), CompanyHandler.getCompanyByUser(conn, loginid));
//            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            java.util.Date d = new java.util.Date();
//            postTime = Timezone.dateTimeRenderer(conn, Timezone.toUserTimezone(conn, sdf.format(d), loginid), postTime, loginid);
            jtemp.put("Subject", java.net.URLEncoder.encode(rs.getString(1)));
            jtemp.put("Received", postTime);//rs.getObject(2));
            jtemp.put("From", rs.getString(3));
            jtemp.put("Details", "");
            jtemp.put("Flag", flag);
            jtemp.put("ifread", ifread);
            jtemp.put("ID", Pid);
            jtemp.put("Parent", id);
            jtemp.put("Level", level);
            String img = StringUtil.getAppsImagePath(Uid, 35);
            jtemp.put("Image", img);
            jtemp.put("User_Id", Uid);
            /*treeString += "{Subject: '" + rs.getString(1) 
            + "',Received: '" + sdf1.format(tempdate).toString() + "',From: '" 
            + rs.getString(3) + "',Details: '',Flag:'" + flag + "',ID:'" + Pid + "',ifread: '" 
            + ifread + "',Parent:'" + id + "',Level:'" + level + "',Image:'" + rs.getString(8) 
            + "',User_Id:'" + Uid + "'},"
            + fillPostSubTree(conn, rs.getString(5), level, loginid);*/
            treeString += jtemp.toString() + "," + fillPostSubTree(conn, rs.getString(5), level, loginid);
        }

        return treeString;
    }

    public static String fillTree(Connection conn, String offset, String limit,
            String groupId, String searchText, int col, String sortFlag, String loginid)
            throws ServiceException, JSONException {
        DbResults rs = null;
        DbResults rs1 = null;
        DbResults rs2 = null;
        DbResults rs3 = null;
        String treeString = null;
        int level = 0;
        String pageCount = null;
        Boolean flag = false;
        Boolean ifread = false;
        String colname = "";
        String query3 = null;
        String query2 = null;
//                searchText = searchText.length()==1 ? "": searchText;
        String searchText1 = searchText;
        JSONObject jtemp = new JSONObject();
        if (searchText.length() >= 1) {
            searchText = searchText + "%";
            searchText1 = "% " + searchText;
        } else if (searchText.length() == 0) {
            searchText = "%" + searchText;
            searchText1 = searchText;
        }
        if (col == 0) {
            colname = "topic_title";
        }
        if (col == 1) {
            colname = "userlogin.username";
        }
        if (col == 2) {
            colname = "post_time";
        }
        if (col == 3) {
            colname = "flag";
        }
        String query = "SELECT topic_title, post_time, concat(fname,' ',lname),post_text,topic_id,userForumPostRead.ifread,userForumPostFlag.flag,users.image,topic_poster FROM " + " krawlerforum_topics " +
                "left outer join userForumPostRead on userForumPostRead.post_id = krawlerforum_topics.topic_id and userForumPostRead.userid = ?" +
                "left outer join userForumPostFlag on userForumPostFlag.post_id = krawlerforum_topics.topic_id and userForumPostFlag.userid = ? " +
                "inner join users on users.userid = krawlerforum_topics.topic_poster inner join userlogin on users.userid=userlogin.userid where group_id= ? and " + " ((topic_title like ? or post_text like ? or userlogin.username like ?) or (topic_title like ? or post_text like ? or userlogin.username like ?)) ";
        rs = DbUtil.executeQuery(
                conn, query + " ORDER BY " + colname + " " + sortFlag + " LIMIT ? OFFSET ?;", new Object[]{loginid, loginid, groupId,
            searchText, searchText, searchText,
            searchText1, searchText1, searchText1,
            Integer.parseInt(limit),
            Integer.parseInt(offset)
        });
        rs1 = DbUtil.executeQuery(conn,
                "SELECT count(*) FROM (" + query + ") as temp",//krawlerforum_topics where group_id = ?;",
                new Object[]{loginid, loginid, groupId,
            searchText, searchText, searchText,
            searchText1, searchText1, searchText1
        });
        if (rs1.next()) {
            int temppageCount = (rs1.getInt(1));
            pageCount = Integer.toString(temppageCount);
        }

        treeString = "{'forumCount':" + pageCount + ",'data':[";
        if (rs.size() > 0) {
            while (rs.next()) {
                String Uid = rs.getString(9);
                String Pid = rs.getString(5);
                ifread = false;
                if (rs.getObject("ifread") != null) {
                    ifread = true;
                }
                flag = false;
                if (rs.getObject("flag") != null) {
                    flag = true;
                }
//				query2 = "Select ifread from userForumPostRead where post_id =? and userid=? ";
//				rs2 = DbUtil.executeQuery(conn, query2,
//						new Object[] { Pid, loginid });
//				if (rs2.next()) {
//					ifread = true;
//				}
//				flag=false;
//				query3 = "Select flag from userForumPostFlag where post_id =? and userid=? ";
//				rs3 = DbUtil.executeQuery(conn, query3,
//						new Object[] { Pid, loginid });
//				if (rs3.next()) {
//					flag = true;
//				}
                String postTime = Timezone.toCompanyTimezone(conn, rs.getObject(2).toString(), CompanyHandler.getCompanyByUser(conn, loginid));
//                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                java.util.Date d = new java.util.Date();
//                postTime = Timezone.dateTimeRenderer(conn, Timezone.toUserTimezone(conn, sdf.format(d), loginid), postTime, loginid);
                jtemp.put("Subject", java.net.URLEncoder.encode(rs.getString(1)));
                jtemp.put("Received", postTime);//rs.getObject(2));
                jtemp.put("From", rs.getString(3));
                jtemp.put("Details", "");
                jtemp.put("Flag", flag);
                jtemp.put("ifread", ifread);
                jtemp.put("ID", "topic" + Pid);
                jtemp.put("Parent", "0");
                jtemp.put("Level", level);
                String img = StringUtil.getAppsImagePath(Uid, 35);
                jtemp.put("Image", img);
                jtemp.put("User_Id", Uid);

                treeString += jtemp.toString() + "," + fillTopicSubTree(conn, rs.getString(5), level, loginid);
            /*jtemp.put("Subject", rs.getString(1));
            jtemp.put("Received", sdf1.format(tempdate).toString());
            jtemp.put("From", rs.getString(3));
            jtemp.put("Details", "");
            jtemp.put("Flag", flag);
            jtemp.put("ifread", ifread);
            jtemp.put("ID", "topic" + Pid);
            jtemp.put("Parent", "0");
            jtemp.put("Level", level);
            jtemp.put("Image", rs.getString(8));
            jtemp.put("User_Id", Uid);*/

            /* "{Subject: '" + rs.getString(1) + "',Received: '"
            + sdf1.format(tempdate).toString() + "',From: '"
            + rs.getString(3) + "',Details: '',Flag:'" + flag
            + "',ifread: '" + ifread + "',ID:'topic" + Pid
            + "',Parent:'0',Level:'" + level + "',Image:'"
            + rs.getString(8) + "',User_Id:'" + Uid + "'},"
            + fillTopicSubTree(conn, rs.getString(5), level, loginid);*/
            }
            treeString = treeString.substring(0, (treeString.length() - 1));
        }
        treeString += "]}";
        return treeString;
    }

    public static String insertForumPost(Connection conn, String repto,
            String title, String ptext, String u_id, String group_id,
            int firstReply) throws ServiceException, ParseException, JSONException {
        DbResults rs = null;
        DbResults rs1 = null;
        DbResults rs2 = null;
        String post_id = UUID.randomUUID().toString();
        String topic_id = "1";
        String topic_title = null;
        String UserName = null;
        String Image = null;

        String query = null;
        JSONStringer j = new JSONStringer();
        JSONObject jobj = new JSONObject();
        String temp = null;

        repto = repto;
        title = StringUtil.serverHTMLStripper(title);
        ptext = ptext;
        u_id = u_id;
        group_id = StringUtil.serverHTMLStripper(group_id);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Date d = new java.util.Date();
        java.sql.Timestamp sqlPostDate = new Timestamp(d.getTime());
        if (StringUtil.isNullOrEmpty(repto) /*|| StringUtil.isNullOrEmpty(title)*/ || StringUtil.isNullOrEmpty(u_id) || StringUtil.isNullOrEmpty(group_id)) {
            com.krawler.utils.json.base.JSONObject jerrtemp = new com.krawler.utils.json.base.JSONObject();
            jerrtemp.put("Success", "Fail");
            jobj.append("data", jerrtemp);
            return jobj.toString();
        }
        if (firstReply == 0) {

            query = "INSERT INTO krawlerforum_topics(topic_id, group_id, topic_title, topic_poster, post_time, post_subject,post_text, ifread,flag) VALUES (?, ?, ?, ?, ?, ?,?, ?, ?)";
            DbUtil.executeUpdate(conn, query, new Object[]{post_id, group_id,
                title, u_id, sqlPostDate, ptext, ptext, false, false
            });

            post_id = "topic" + post_id;
        } else if (firstReply == 1) {
            repto = repto.substring(5);

            query = "INSERT INTO krawlerforum_posts(post_id, post_poster, topic_id, group_id, topic_title,post_time, post_subject, post_text, reply_to, flag, ifread,reply_to_post) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
            DbUtil.executeUpdate(conn, query, new Object[]{post_id, u_id,
                topic_id, group_id, topic_title, sqlPostDate, title, ptext, repto,
                false, false, "-999"
            });

            repto = "topic" + repto;
        } else if (firstReply == 2) {
            query = "INSERT INTO krawlerforum_posts(post_id, post_poster, topic_id, group_id, topic_title, post_time, post_subject, post_text, reply_to, flag, ifread,reply_to_post) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
            DbUtil.executeUpdate(conn, query, new Object[]{post_id, u_id,
                topic_id, group_id, topic_title, sqlPostDate, title, ptext, "1", false,
                false, repto
            });

        }

        query = "SELECT userlogin.username,image FROM users inner join userlogin on users.userid=userlogin.userid where users.userid=?;";
        rs2 = DbUtil.executeQuery(conn, query, u_id);

        if (rs2.next()) {
            UserName = (rs2.getString(1));
            Image = (rs2.getString(2));
        }
        String userTime = Timezone.toCompanyTimezone(conn, sqlPostDate.toString(), CompanyHandler.getCompanyByUser(conn, u_id));
        java.util.Date tempdate = sdf.parse(userTime);
        com.krawler.utils.json.base.JSONObject jtemp = new com.krawler.utils.json.base.JSONObject();
        jtemp.put("Success", "Success");
        jtemp.put("ID", post_id);
        jtemp.put("Subject", title);
        jtemp.put("Received", sdf.format(tempdate));
        jtemp.put("From", UserName);
        jtemp.put("Details", "");
        jtemp.put("Flag", "false");
        jtemp.put("Image", Image);
        jtemp.put("Parent", repto);
        jobj.append("data", jtemp);
        /*temp = j.object().key("Success").value("Success").key("ID").value(
        post_id).key("Subject").value(title).key("Received").value(
        sdf1.format(tempdate).toString()).key("From").value(UserName)
        .key("Details").value("").key("Flag").value("false").key(
        "Image").value(Image).key("Parent").value(repto)
        .endObject().toString();*/
        return jobj.toString();
    }

    public static String insertForumPost(Connection conn, HttpServletRequest request) throws ServiceException, ParseException, JSONException {
        org.apache.commons.fileupload.DiskFileUpload fu = new org.apache.commons.fileupload.DiskFileUpload();
        java.util.List fileItems = null;
        org.apache.commons.fileupload.FileItem fi = null;
        int sizeinmb = forummsgcomm.getmaxfilesize(conn, AuthHandler.getCompanyid(request, false));
        long maxsize = sizeinmb * 1024 * 1024;
        boolean fileupload = false;
        fu.setSizeMax(maxsize);
        java.util.HashMap arrParam = new java.util.HashMap();
        JSONObject jobj = new JSONObject();
        try {
            fileItems = fu.parseRequest(request);
        } catch (org.apache.commons.fileupload.FileUploadException e) {
            com.krawler.utils.json.base.JSONObject jerrtemp = new com.krawler.utils.json.base.JSONObject();
            jerrtemp.put("Success", "Fail");
            jerrtemp.put("msg", "Problem while uploading file.");
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
                    jerrtemp.put("msg", "For attachments, maximum file size allowed is " + sizeinmb + " MB");
                    jobj.append("data", jerrtemp);
                    return jobj.toString();
                }
                fileupload = true;
            }
        }
//                destinationDirectory = com.krawler.esp.handlers.StorageHandler
//					.GetDocStorePath()
        int firstReply = Integer.parseInt(request.getParameter("firstReply"));
        String ptext = java.net.URLDecoder.decode(arrParam.get("ptxt").toString());
        String repto = request.getParameter("repto");
        String title = java.net.URLDecoder.decode(arrParam.get("title").toString());
        String u_id = request.getParameter("userId");
        String group_id = request.getParameter("groupId");
        DbResults rs = null;
        DbResults rs1 = null;
        DbResults rs2 = null;
        String post_id = UUID.randomUUID().toString();
        String topic_id = "1";
        String topic_title = null;
        String dateTime = null;
        String UserName = null;
        String Image = null;
        String query = null;
        JSONStringer j = new JSONStringer();


        String temp = null;
        title = StringUtil.serverHTMLStripper(title);

        group_id = StringUtil.serverHTMLStripper(group_id);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Date d = new java.util.Date();
        java.sql.Timestamp sqlPostDate = new Timestamp(d.getTime());
        if (StringUtil.isNullOrEmpty(repto) /*|| StringUtil.isNullOrEmpty(title)*/ || StringUtil.isNullOrEmpty(u_id) || StringUtil.isNullOrEmpty(group_id)) {
            com.krawler.utils.json.base.JSONObject jerrtemp = new com.krawler.utils.json.base.JSONObject();
            jerrtemp.put("Success", "Fail");
            jobj.append("data", jerrtemp);
            return jobj.toString();
        }
        if (fileupload) {
            forummsgcomm.doPost(conn, fileItems, post_id, sqlPostDate, u_id, "");
        }
        if (firstReply == 0) {

            query = "INSERT INTO krawlerforum_topics(topic_id, group_id, topic_title, topic_poster, post_time, post_subject,post_text, ifread,flag) VALUES (?, ?, ?, ?, ?, ?,?, ?, ?)";
            DbUtil.executeUpdate(conn, query, new Object[]{post_id, group_id,
                title, u_id, sqlPostDate, ptext, ptext, false, false
            });

            post_id = "topic" + post_id;
        } else if (firstReply == 1) {
            repto = repto.substring(5);

            query = "INSERT INTO krawlerforum_posts(post_id, post_poster, topic_id, group_id, topic_title,post_time, post_subject, post_text, reply_to, flag, ifread,reply_to_post) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
            DbUtil.executeUpdate(conn, query, new Object[]{post_id, u_id,
                topic_id, group_id, topic_title, sqlPostDate, title, ptext, repto,
                false, false, "-999"
            });
            
            repto = "topic" + repto;
        } else if (firstReply == 2) {
            query = "INSERT INTO krawlerforum_posts(post_id, post_poster, topic_id, group_id, topic_title, post_time, post_subject, post_text, reply_to, flag, ifread,reply_to_post) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
            DbUtil.executeUpdate(conn, query, new Object[]{post_id, u_id,
                topic_id, group_id, topic_title, sqlPostDate, title, ptext, "1", false,
                false, repto
            });

        }

        query = "SELECT userlogin.username,image FROM users inner join userlogin on users.userid = userlogin.userid where users.userid=?;";
        rs2 = DbUtil.executeQuery(conn, query, u_id);

        if (rs2.next()) {
            UserName = (rs2.getString(1));
            Image = (rs2.getString(2));
        }
        String userTime = Timezone.toCompanyTimezone(conn, sqlPostDate.toString(), CompanyHandler.getCompanyByUser(conn, u_id));
        java.util.Date tempdate = sdf.parse(userTime);       
        com.krawler.utils.json.base.JSONObject jtemp = new com.krawler.utils.json.base.JSONObject();
        jtemp.put("Success", "Success");
        jtemp.put("ID", post_id);
        jtemp.put("Subject", title);
        jtemp.put("Received", sdf.format(tempdate));
        jtemp.put("From", UserName);
        jtemp.put("Details", "");
        jtemp.put("Flag", "false");
        jtemp.put("Image", Image);
        jtemp.put("Parent", repto);
        jobj.append("data", jtemp);
        /*temp = j.object().key("Success").value("Success").key("ID").value(
        post_id).key("Subject").value(title).key("Received").value(
        sdf1.format(tempdate).toString()).key("From").value(UserName)
        .key("Details").value("").key("Flag").value("false").key(
        "Image").value(Image).key("Parent").value(repto)
        .endObject().toString();*/
        return jobj.toString();
    }

    public static String getPostSubject(String postid, String type) throws ServiceException{
        String sub = "";
        if(type.equals("topic")){
            DbResults rs = DbUtil.executeQuery("SELECT topic_title as post_subject FROM krawlerforum_topics where topic_id = ?", postid);
            if(rs.next()){
                sub = rs.getString("post_subject");
            }
        } else {
            DbResults rs = DbUtil.executeQuery("SELECT post_subject FROM krawlerforum_posts where post_id = ?", postid);
            if(rs.next()){
                sub = rs.getString("post_subject");
            }
        }
        if(StringUtil.isNullOrEmpty(sub))
            sub = "";
        return sub;
    }

//    public static String insertNewPost(Connection conn, String postid,
//            String c_id, String u_id, String topic, String title, String ptxt,
//            String repto) throws ServiceException {
//        String query = null;
//        Object[] params = {null};
//
//        query = "INSERT INTO krawlerforum_posts(post_id, post_poster, topic_id, group_id, topic_title, topic_poster,post_time, post_subject, post_text, reply_to, flag, ifread) VALUES (?, ?, ?, ?, ?, ?,now(), ?, ?, ?, ?, ?);";
//        params[0] = postid;
//        params[1] = u_id;
//        params[2] = topic;
//        params[3] = c_id;
//        params[4] = "zxcv";
//        params[5] = "cvc";
//        params[6] = title;
//        params[7] = ptxt;
//        params[8] = repto;
//        params[9] = false;
//        params[10] = false;
//        int numRows = DbUtil.executeUpdate(conn, query, params);
//        if (numRows == 0) {
//            return "Failure";
//        } else {
//            return "Success";
//        }
//    }

    public static String markForumFlag(String userid, String postid, int flag) {
        com.krawler.database.DbPool.Connection conn = null;

        int rs = 0;
        String treeString = "";
        String query = "";
        try {
            conn = DbPool.getConnection();
            if (postid.contains("topic")) {
                postid = postid.substring(5);
            }
            if (flag == 1) {
                query = "insert into  userForumPostFlag (post_id,userid,flag) values (?,?,?);";
                rs = DbUtil.executeUpdate(conn, query, new Object[]{postid, userid, true});
            } else if (flag == 0) {
                query = "Delete FROM userForumPostFlag  where post_id=? and userid=?;";
                rs = DbUtil.executeUpdate(conn, query, new Object[]{postid, userid});
            }
            conn.commit();
            if (rs > 0) {
                treeString = "{'data':[{status: 'success'}]}";
            } else {
                treeString = "{'data':[{status: 'failure'}]}";
            }
        } catch (ServiceException e) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }

        return treeString;
    }
}
