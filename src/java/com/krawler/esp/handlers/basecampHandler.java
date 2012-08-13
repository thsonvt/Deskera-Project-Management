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

import com.krawler.common.customcolumn.CCManager;
import com.krawler.common.customcolumn.ColumnSet;
import com.krawler.common.customcolumn.ColumnsMetaData;
import com.krawler.common.customcolumn.CustomColumn;
import com.krawler.common.locale.MessageSourceProxy;
import com.krawler.common.service.ServiceException;
import com.krawler.svnwebclient.configuration.ConfigurationException;
import java.io.File;



import com.krawler.common.util.KrawlerLog;
import com.krawler.common.util.StringUtil;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.krawler.utils.json.base.JSONArray;


import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.krawler.database.DbPool;
import com.krawler.database.DbUtil;
import com.krawler.database.DbPool.Connection;
import com.krawler.esp.servlets.AdminServlet;
import java.util.*;

import javax.servlet.http.*;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.FileItem;
import com.krawler.common.timezone.Timezone;
import com.krawler.esp.company.CompanyDAO;
import com.krawler.esp.company.CompanyDAOImpl;
import com.krawler.esp.project.meter.HealthMeterDAOImpl;
import java.sql.Timestamp;
import org.w3c.dom.Node;

//import javax.
/**
 *
 * @author mosin
 */
public class basecampHandler {

    public static String importUserFromBaseCamp(javax.servlet.http.HttpServletRequest request) {
        String result = "";
        JSONObject res = new JSONObject();
        String errorString = "";
        try {
            res.put("error", "%s");
            String docid = java.util.UUID.randomUUID().toString();
            File f1 = getfile(request, docid);

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
//                Document doc = docBuilder.parse (new File("/home/mosin/krawlercom-20081201125009.xml"));
            Document doc = docBuilder.parse(f1);
            doc.getDocumentElement().normalize();
            NodeList firmInfo = doc.getElementsByTagName("firm");
            NodeList peopel = (getElementFromNodeList(firmInfo)).getElementsByTagName("people");
            NodeList personList = (getElementFromNodeList(peopel)).getElementsByTagName("person");
            int len = personList.getLength();
            for (int i = 0; i < len; i++) {
                if (checkNodeType(personList.item(i))) {
                    Element person = (Element) personList.item(i);
                    JSONObject temp = new JSONObject();
                    temp.put("firstName", person.getElementsByTagName("first-name").item(0).getChildNodes().item(0).getNodeValue());
                    temp.put("lastName", person.getElementsByTagName("last-name").item(0).getChildNodes().item(0).getNodeValue());
                    temp.put("id", person.getElementsByTagName("id").item(0).getChildNodes().item(0).getNodeValue());
                    try {
                        temp.put("username", person.getElementsByTagName("user-name").item(0).getChildNodes().item(0).getNodeValue());
                    } catch (Exception e) {
                        temp.put("username", person.getElementsByTagName("first-name").item(0).getChildNodes().item(0).getNodeValue());
                    }
                    temp.put("email", person.getElementsByTagName("email-address").item(0).getChildNodes().item(0).getNodeValue());
                    res.append("data", temp);
                }
            }

            NodeList projects = doc.getElementsByTagName("projects");
            NodeList projectList = (getElementFromNodeList(projects)).getElementsByTagName("project");
            len = projectList.getLength();
            for (int j = 0; j < len; j++) {
                if (checkNodeType(projectList.item(j))) {
                    Element project = (Element) projectList.item(j);
                    JSONObject temp = new JSONObject();
                    temp.put("projectname", project.getElementsByTagName("name").item(0).getChildNodes().item(0).getNodeValue());
                    temp.put("projectid", project.getElementsByTagName("id").item(0).getChildNodes().item(0).getNodeValue());
                    temp.put("status", project.getElementsByTagName("status").item(0).getChildNodes().item(0).getNodeValue());
                    res.append("projdata", temp);
                }
            }
            res.put("docid", docid);
            result = res.toString();
        } catch (Exception ex) {
            errorString = MessageSourceProxy.getMessage("", null, request);
            result = res.toString();
            KrawlerLog.op.warn("Problem Occured while importing project from base camp from " + ex.toString());
        } finally {

            result = String.format(result, errorString);
            return result;
        }
    }

    public static File getfile(HttpServletRequest request, String docid) throws ConfigurationException {
        String destinationDirectory = StorageHandler.GetDocStorePath() + StorageHandler.GetFileSeparator() + "baseCamxml";
        java.io.File destDir = new java.io.File(destinationDirectory);

        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        DiskFileUpload fu = new DiskFileUpload();
        String Ext = null;
        File uploadFile = null;
        List fileItems = null;
        try {
            fileItems = fu.parseRequest(request);
        } catch (FileUploadException e) {
            KrawlerLog.op.warn("Problem While Uploading file :" + e.toString());
        }
        for (Iterator i = fileItems.iterator(); i.hasNext();) {
            FileItem fi = (FileItem) i.next();
            if (!fi.isFormField()) {
                String fileName = null;
                try {
                    fileName = new String(fi.getName().getBytes(), "UTF8");
                    if (fileName.contains(".")) {
                        Ext = fileName.substring(fileName.lastIndexOf("."));
                    }
                    if (fi.getSize() != 0) {
                        uploadFile = new File(destinationDirectory + StorageHandler.GetFileSeparator() + docid + ".xml");
                        fi.write(uploadFile);
                    }
                } catch (Exception e) {
                    KrawlerLog.op.warn("Problem While Reading file :"
                            + e.toString());
                }
            }
        }

        return uploadFile;
    }

    public static boolean cheForProject(String projName, String[] projNames) {
        boolean res = false;
        java.util.List l = java.util.Arrays.asList(projNames);
        java.util.HashSet<String> hs = new java.util.HashSet<String>();
        hs.addAll(l);
        res = hs.add(projName);
        return res;
    }

    public static JSONObject getSystemUser(JSONArray userMap, String tempid) {
        JSONObject obj = null;
        try {
            for (int i = 0; i < userMap.length(); i++) {
                if (userMap.getJSONObject(i).getString("id").equals(tempid)) {
                    obj = userMap.getJSONObject(i);
                    break;
                }
            }
        } catch (JSONException ex) {
        }
        return obj;

    }

    public static String getSystemUserid(JSONArray userMap, String tempid) {
        JSONObject obj = null;
        String userid = "";
        try {
            for (int i = 0; i < userMap.length(); i++) {
                obj = userMap.getJSONObject(i);
                if (obj.getString("id").equals(tempid)) {
                    userid = obj.getString("userid");
                    break;
                }
            }
        } catch (JSONException ex) {
        }
        return userid;

    }

    public static long getDiff(Date dt1, Date dt2) {
        Date dt3 = new Date(dt1.getYear(), dt1.getMonth(), dt1.getDate());
        Date dt4 = new Date(dt2.getYear(), dt2.getMonth(), dt2.getDate());

        long diff = (dt3.getTime() - dt4.getTime()) / (1000 * 60 * 60 * 24);
        return diff;

    }

    public static String createProj(Connection conn, String cid, String projName, String createdon, String status) throws ServiceException, ParseException {
        String projid = java.util.UUID.randomUUID().toString();
        int archStatus = 0;
        if (!status.equals("active")) {
            archStatus = 1;
        }
        java.util.Date dt = getEndDate(createdon);
        java.sql.Timestamp ts = Timezone.fromCompanyToSystemTimestamp(conn, new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dt), cid);
        String query = "INSERT INTO project (projectid,projectname,companyid,createdon,image,discription,archived) VALUES (?,?,?,?,?,?,?)";
        /* custom column */
        CustomColumn cc = CCManager.getCustomColumn(cid);
        ColumnsMetaData csmd = cc.getColumnsMetaData(conn, "Project");
        Map values = new HashMap();
        int size = csmd.getColumnCount();
        for(int i=0; i<size; i++){
            values.put(csmd.getCoulmnNo(i), csmd.getColumn(i).getDefaultValue());
        }
        DbUtil.executeUpdate(conn, query, new Object[]{projid, projName, cid, ts, "", "", archStatus});
        (new HealthMeterDAOImpl()).setBaseLineMeter(conn, projid); //health meter setting
        cc.insertColumnsData(conn, values, "project", projid); //custom column
        AdminServlet.setDefaultWorkWeek(conn, projid);
        return projid;
    }

    public static Date getStartDate(String dateString) {
        Date dt1 = null;
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            dt1 = sdf.parse(dateString);
        } catch (ParseException ex) {
            Logger.getLogger(basecampHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dt1;
    }

    public static Date getEndDate(String dateString) {
        Date dt1 = null;
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            dt1 = sdf.parse(dateString);
        } catch (ParseException ex) {
            Logger.getLogger(basecampHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dt1;
    }

//        createTask(conn,cid,projid,title,mileStoneCreated_on,deadline,resourceid,commentBody)
    public static String createTask(Connection conn, String cid, String projid, String title, String startDate, String endDate, String resourceid, String commentBody, int taskindex, JSONArray userMap) throws ServiceException, ParseException {
        String taskid = java.util.UUID.randomUUID().toString();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date stDate = getStartDate(startDate);
        Date enDate = getEndDate(endDate);
        java.sql.Timestamp sqlstDate = new java.sql.Timestamp(stDate.getTime());
        java.sql.Timestamp sqlenDate = Timezone.fromCompanyToSystemTimestamp(conn, sdf.format(enDate), cid);
        long duration = getDiff(sqlenDate/*stDate*/, sqlenDate);
        String inserQuery = "INSERT INTO proj_task(taskid, taskname, duration, startdate, enddate, projectid, "
                + "taskindex, level, parent, actualstartdate, actualduration, percentcomplete, notes, priority, "
                + "isparent) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        DbUtil.executeUpdate(conn, inserQuery, new Object[]{taskid, title, duration,/*sqlstDate*/ sdf.format(sqlenDate), sdf.format(sqlenDate), projid, taskindex, 0, 0, sdf.format(sqlenDate), duration, 0, commentBody, 1, 0});

        String userid = getSystemUserid(userMap, resourceid);
        if (!userid.equals("")) {
            DbUtil.executeUpdate(conn, "insert into proj_taskresourcemapping (taskid,resourceid,resduration) values(?,?,?)", new Object[]{taskid, userid, 0});
        }
        return taskid;
    }

    public static void createresource(Connection conn, String projid, String userId, String userName, int status) throws ServiceException, JSONException {

        String inserQuery = "INSERT INTO proj_resources (resourceid, resourcename, projid, stdrate, typeid, colorcode)"
                + " VALUES (?,?,?,?,?,?)";
        DbUtil.executeUpdate(conn, inserQuery, new Object[]{userId, userName, projid, 0, 1, "#FF0000"});
        String projMemQuery = " INSERT INTO projectmembers (projectid,status,userid,inuseflag) value (?,?,?,?) ";
        DbUtil.executeUpdate(conn, projMemQuery, new Object[]{projid, status, userId, 1});
    }

    public static String createTodo(Connection conn, String name, String des, String parentid, JSONArray userMap,
            String status, boolean group, String assignedTo, String userid, String projectid) throws ServiceException, JSONException {
        String taskid = java.util.UUID.randomUUID().toString();
        JSONObject obj = getSystemUser(userMap, assignedTo);
        int st = status.equals("true") ? 1 : 0;
        if (group) {
            if (obj == null || obj.getString("userid").equals("")) {
                assignedTo = "";
//                    des = FileHandler.getAuthor(userid)+" : "+des;
//                    des = obj.getString("firstName")+" "+obj.getString("lastName")+" : "+des;
            } else {
                assignedTo = obj.getString("userid");
                des = obj.getString("firstName") + " " + obj.getString("lastName") + " : " + des;
            }
        }
        java.sql.Timestamp ts = new java.sql.Timestamp(new java.util.Date().getTime());
        String todoQuery = "insert into todotask (taskid,taskname,description,taskorder,status,parentId,timestamp,userid,grouptype,assignedto,leafflag) "
                + " values(?,?,?,?,?,?,?,?,?,?,?) ";
        DbUtil.executeUpdate(conn, todoQuery, new Object[]{taskid, name, des, 0, st, parentid, ts, projectid, 2, assignedTo, group});
        return taskid;

    }

    public static String insertDiscussionTopic(Connection conn, String projid, String posttitle, String PostAuthId, String postedon, String PostBody, JSONArray userMap, String userid) throws JSONException, ServiceException {
        String topicid = java.util.UUID.randomUUID().toString();
        JSONObject obj = getSystemUser(userMap, PostAuthId);
        getStartDate(postedon);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (obj == null || obj.getString("userid").equals("")) {
            PostAuthId = userid;
        } else {
            PostAuthId = obj.getString("userid");
        }
        java.sql.Timestamp sqlPostDate = Timezone.fromCompanyToSystemTimestamp(conn, sdf.format(getStartDate(postedon)), CompanyHandler.getCompanyIDFromProject(conn, projid));
        String query = "INSERT INTO krawlerforum_topics(topic_id, group_id, topic_title, topic_poster, post_time, post_subject,post_text, ifread,flag) VALUES (?, ?, ?, ?, ?, ?,?, ?, ?)";
        DbUtil.executeUpdate(conn, query, new Object[]{topicid, projid,
                    posttitle, PostAuthId, sqlPostDate, PostBody, PostBody, false, false});
        return topicid;
    }

    public static String insertDiscussionSubPost(Connection conn, String projid, String posttitle, String PostAuthId, String postedon, String PostBody, JSONArray userMap, String userid, String topicid) throws JSONException, ServiceException {
        String postid = java.util.UUID.randomUUID().toString();
        JSONObject obj = getSystemUser(userMap, PostAuthId);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (obj == null || obj.getString("userid").equals("")) {
            PostAuthId = userid;
        } else {
            PostAuthId = obj.getString("userid");
        }
        java.sql.Timestamp sqlPostDate = Timezone.fromCompanyToSystemTimestamp(conn, sdf.format(getStartDate(postedon)), CompanyHandler.getCompanyIDFromProject(conn, projid));
        String query = "INSERT INTO krawlerforum_posts(post_id, post_poster, topic_id, group_id, topic_title,post_time, post_subject, post_text, reply_to, flag, ifread,reply_to_post) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        DbUtil.executeUpdate(conn, query, new Object[]{postid, PostAuthId,
                    1, projid, posttitle, sqlPostDate, posttitle, PostBody, topicid,
                    false, false, "-999"});
        return postid;
    }

    public static String importProjectFromBaseCamp(String userMap, String cid, String lid, String docid, String lName) {
        String result = "";
        String projectName = "MyProject";
        String[] projectNames = {"MyProject", "MyProject1"};
        JSONArray jobj = null;
        Connection conn = null;
        Locale locale = Locale.ENGLISH;
        try {
            jobj = new JSONArray(userMap);
        } catch (JSONException ex) {
        }
        try {
            String destinationDirectory = StorageHandler.GetDocStorePath() + StorageHandler.GetFileSeparator() + "baseCamxml";
            conn = DbPool.getConnection();
            CompanyDAO cd = new CompanyDAOImpl();
            locale = cd.getCompanyLocale(conn, cid);
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(destinationDirectory + StorageHandler.GetFileSeparator() + docid + ".xml"));
            doc.getDocumentElement().normalize();
            NodeList projects = doc.getElementsByTagName("projects");
            NodeList projectList = ((Element) projects.item(0)).getElementsByTagName("project");
            for (int j = 0; j < projectList.getLength(); j++) {
                Element project = (Element) projectList.item(j);
                String projName = project.getElementsByTagName("name").item(0).getChildNodes().item(0).getNodeValue();
                String created_on = project.getElementsByTagName("created-on").item(0).getChildNodes().item(0).getNodeValue();
                project.getElementsByTagName("last-changed-on").item(0).getChildNodes().item(0).getNodeValue();
                String status = project.getElementsByTagName("status").item(0).getChildNodes().item(0).getNodeValue();

                String projid = createProj(conn, cid, projName, created_on, status);


                NodeList userList = ((Element) project.getElementsByTagName("participants").item(0)).getElementsByTagName("person");
                for (int userCount = 0; userCount < userList.getLength(); userCount++) {
                    JSONObject user = getSystemUser(jobj, userList.item(userCount).getChildNodes().item(0).getNodeValue());
                    if (user != null && !user.getString("userid").equals("")) {
                        createresource(conn, projid, user.getString("userid"), user.getString("username"), 3);
                    }
                }
                createresource(conn, projid, lid, lName, 4);


                project.getElementsByTagName("milestones").item(0).getChildNodes().item(0).getNodeValue();
                NodeList milestoneList = ((Element) project.getElementsByTagName("milestones").item(0)).getElementsByTagName("milestone");

                for (int milesNo = 0; milesNo < milestoneList.getLength(); milesNo++) {
                    Element milestone = (Element) milestoneList.item(milesNo);
//                          String completedStatus = milestone.getElementsByTagName("completed").item(0).getChildNodes().item(0).getNodeValue();
//                          if(StringUtil.stringCompareInLowercase(completedStatus,"true")){
//                              String completed_on = milestone.getElementsByTagName("completed-on").item(0).getChildNodes().item(0).getNodeValue();
//                              String completer_id = milestone.getElementsByTagName("completed-on").item(0).getChildNodes().item(0).getNodeValue();
//                          }
                    String title = milestone.getElementsByTagName("title").item(0).getChildNodes().item(0).getNodeValue();
                    String mileStoneCreated_on = milestone.getElementsByTagName("created-on").item(0).getChildNodes().item(0).getNodeValue();
                    String deadline = milestone.getElementsByTagName("deadline").item(0).getChildNodes().item(0).getNodeValue();
                    String resourceid = milestone.getElementsByTagName("responsible-party-id").item(0).getChildNodes().item(0).getNodeValue();
//                              milestone.getElementsByTagName("responsible-party-type").item(0).getChildNodes().item(0).getNodeValue();
//                              milestone.getElementsByTagName("wants-notification").item(0).getChildNodes().item(0).getNodeValue();
                    NodeList commentList = ((Element) milestone.getElementsByTagName("comments").item(0)).getElementsByTagName("comment");
                    String commentBody = "";
                    for (int commentcout = 0; commentcout < commentList.getLength(); commentcout++) {
                        Element comment = (Element) commentList.item(commentcout);
//                                   comment.getElementsByTagName("attachments-count").item(0).getChildNodes().item(0).getNodeValue();
//                                   comment.getElementsByTagName("author-id").item(0).getChildNodes().item(0).getNodeValue();
                        commentBody = commentBody + comment.getElementsByTagName("body").item(0).getChildNodes().item(0).getNodeValue() + "<br>";
//                                   comment.getElementsByTagName("commentable-id").item(0).getChildNodes().item(0).getNodeValue();
//                                   comment.getElementsByTagName("commentable-type").item(0).getChildNodes().item(0).getNodeValue();
//                                   comment.getElementsByTagName("created-at").item(0).getChildNodes().item(0).getNodeValue();
//    //                                   comment.getElementsByTagName("emailed-from").item(0).getChildNodes().item(0).getNodeValue();
//    //                                   commentable-id
                    }
                    createTask(conn, cid, projid, title, mileStoneCreated_on, deadline, resourceid, commentBody, milesNo, jobj);
                    //                              body

                }
                //                         todo-lists
                NodeList todo_lists = ((Element) project.getElementsByTagName("todo-lists").item(0)).getElementsByTagName("todo-list");
                for (int todosNo = 0; todosNo < todo_lists.getLength(); todosNo++) {
                    Element todoList = (Element) todo_lists.item(todosNo);
                    todoList.getElementsByTagName("completed-count").item(0).getChildNodes().item(0).getNodeValue();
                    String des = todoList.getElementsByTagName("description").item(0).getChildNodes().item(0).getNodeValue();
                    //                             todoList.getElementsByTagName("milestone-id").item(0).getChildNodes().item(0).getNodeValue();
                    String name = todoList.getElementsByTagName("name").item(0).getChildNodes().item(0).getNodeValue();
                    todoList.getElementsByTagName("position").item(0).getChildNodes().item(0).getNodeValue();
                    todoList.getElementsByTagName("tracked").item(0).getChildNodes().item(0).getNodeValue();
                    todoList.getElementsByTagName("uncompleted-count").item(0).getChildNodes().item(0).getNodeValue();
                    String toDoStatus = todoList.getElementsByTagName("complete").item(0).getChildNodes().item(0).getNodeValue();

                    //NodeList todo_items = todoList.getElementsByTagName("todo-items");
//                             Connection conn,String des,String parentid,JSONArray userMap,String status,boolean group,String assignedTo,String userid
                    String assignedTo = "";
                    String parentid = createTodo(conn, name, des, "", jobj, toDoStatus, false, assignedTo, lid, projid);
                    NodeList todo_items = ((Element) todoList.getElementsByTagName("todo-items").item(0)).getElementsByTagName("todo-item");


                    for (int todoitemsNo = 0; todoitemsNo < todo_items.getLength(); todoitemsNo++) {
                        Element todoitem = (Element) todo_items.item(todoitemsNo);
                        toDoStatus = todoitem.getElementsByTagName("completed").item(0).getChildNodes().item(0).getNodeValue();
                        des = todoitem.getElementsByTagName("content").item(0).getChildNodes().item(0).getNodeValue();
                        todoitem.getElementsByTagName("created-on").item(0).getChildNodes().item(0).getNodeValue();
                        todoitem.getElementsByTagName("position").item(0).getChildNodes().item(0).getNodeValue();
                        todoitem.getElementsByTagName("todo-list-id").item(0).getChildNodes().item(0).getNodeValue();
                        if (todoitem.getElementsByTagName("responsible-party-id").item(0) != null) {
                            assignedTo = todoitem.getElementsByTagName("responsible-party-id").item(0).getChildNodes().item(0).getNodeValue();
                        }
                        createTodo(conn, des, des, parentid, jobj, toDoStatus, true, assignedTo, lid, projid);
                    }
                }

                NodeList posts = ((Element) project.getElementsByTagName("posts").item(0)).getElementsByTagName("post");
//                         topic_id, group_id, topic_title, topic_poster, post_time, post_subject,post_text, ifread,flag

                for (int postNo = 0; postNo < posts.getLength(); postNo++) {
                    Element singlePost = (Element) posts.item(postNo);
                    String PostAuthId = singlePost.getElementsByTagName("author-id").item(0).getChildNodes().item(0).getNodeValue();
                    String PostBody = "";
                    if (singlePost.getElementsByTagName("body").item(0).getChildNodes().item(0) != null) {
                        PostBody = singlePost.getElementsByTagName("body").item(0).getChildNodes().item(0).getNodeValue();
                    }
                    String PostCommentCount = singlePost.getElementsByTagName("comments-count").item(0).getChildNodes().item(0).getNodeValue();
                    String postedon = singlePost.getElementsByTagName("posted-on").item(0).getChildNodes().item(0).getNodeValue();
                    String posttitle = "[Basecamp] " + singlePost.getElementsByTagName("title").item(0).getChildNodes().item(0).getNodeValue();
                    String topicid = insertDiscussionTopic(conn, projid, posttitle, PostAuthId, postedon, PostBody, jobj, lid);


                    NodeList subPosts = ((Element) singlePost.getElementsByTagName("comments").item(0)).getElementsByTagName("comment");
                    for (int subPostCount = 0; subPostCount < subPosts.getLength(); subPostCount++) {
                        Element subPost = (Element) subPosts.item(subPostCount);
                        String subpostAuthid = subPost.getElementsByTagName("author-id").item(0).getChildNodes().item(0).getNodeValue();
                        String subpostBody = "";
                        if (subPost.getElementsByTagName("body").item(0).getChildNodes().item(0) != null) {
                            subpostBody = subPost.getElementsByTagName("body").item(0).getChildNodes().item(0).getNodeValue();
                        }
                        String subpostCreatedat = subPost.getElementsByTagName("created-at").item(0).getChildNodes().item(0).getNodeValue();
                        insertDiscussionSubPost(conn, projid, posttitle, subpostAuthid, subpostCreatedat, subpostBody, jobj, lid, topicid);
                    }

                }


//                    }
            }  
            conn.commit();
        } catch (Exception ex) {
            KrawlerLog.op.warn("Problem Occured while importing projects from base camp from " + ex.toString());
            DbPool.quietRollback(conn);
            result = MessageSourceProxy.getMessage("pm.project.basecamp.importerror", null, locale);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static JSONObject checkvalidProject(JSONArray data, String tempProjId) {
        JSONObject obj = null;
        try {
            for (int i = 0; i < data.length(); i++) {
                if (data.getJSONObject(i).getString("projectid").equals(tempProjId)) {
                    obj = data.getJSONObject(i);
                    break;
                }
            }
        } catch (JSONException ex) {
        }
        return obj;
    }

    public static String importProjectFromBaseCampUSerChoice(String dataString, String cid, String lid, String docid, String lName,String ipAddress) {
        String result = "";
        String projectName = "MyProject";
        String[] projectNames = {"MyProject", "MyProject1"};
        JSONArray jobj = null;
        Connection conn = null;
        int projcount = 0;
        int todoscount = 0;
        int postscount = 0;
        int milstonecount = 0;
        Locale locale = Locale.ENGLISH;
        try {
            jobj = new JSONArray(dataString);
        } catch (JSONException ex) {
        }
        try {
            String destinationDirectory = StorageHandler.GetDocStorePath() + StorageHandler.GetFileSeparator() + "baseCamxml";
            conn = DbPool.getConnection();
            CompanyDAO cd = new CompanyDAOImpl();
            locale = cd.getCompanyLocale(conn, cid);
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(destinationDirectory + StorageHandler.GetFileSeparator() + docid + ".xml"));
            doc.getDocumentElement().normalize();
            NodeList projects = doc.getElementsByTagName("projects");
            NodeList projectList = (getElementFromNodeList(projects)).getElementsByTagName("project");
            int len = projectList.getLength();
            for (int j = 0; j < len; j++) {
                if (checkNodeType(projectList.item(j))) {
                    Element project = (Element) projectList.item(j);
                    if (jobj == null) {
                        break;
                    }
                    JSONObject projobj = checkvalidProject(jobj.getJSONObject(0).getJSONArray("projdata"), project.getElementsByTagName("id").item(0).getChildNodes().item(0).getNodeValue());
                    if (projobj == null) {
                        continue;
                    }
                    projcount++;
                    String projName = project.getElementsByTagName("name").item(0).getChildNodes().item(0).getNodeValue();
                    String created_on = project.getElementsByTagName("created-on").item(0).getChildNodes().item(0).getNodeValue();
                    project.getElementsByTagName("last-changed-on").item(0).getChildNodes().item(0).getNodeValue();
                    String status = project.getElementsByTagName("status").item(0).getChildNodes().item(0).getNodeValue();

                    String projid = createProj(conn, cid, projName, created_on, status);


                    NodeList userList = ((Element) project.getElementsByTagName("participants").item(0)).getElementsByTagName("person");
                    for (int userCount = 0; userCount < userList.getLength(); userCount++) {
                        JSONObject user = getSystemUser(jobj.getJSONObject(0).getJSONArray("userdata"), userList.item(userCount).getChildNodes().item(0).getNodeValue());
                        if (user != null && !user.getString("userid").equals("")) {
                            try {
                                int stat = 3;
                                if (StringUtil.equal(lid, user.getString("userid"))) {
                                    stat = 4;
                                }
                                createresource(conn, projid, user.getString("userid"), user.getString("username"), stat);
                            } catch (ServiceException ex) {
                            }
                        }
                    }
                    try {
                        createresource(conn, projid, lid, lName, 4);
                    } catch (ServiceException ex) {
                    }
                    if (projobj.getString("milestone").equals("1")) {
                        project.getElementsByTagName("milestones").item(0).getChildNodes().item(0).getNodeValue();
                        NodeList milestoneList = ((Element) project.getElementsByTagName("milestones").item(0)).getElementsByTagName("milestone");

                        for (int milesNo = 0; milesNo < milestoneList.getLength(); milesNo++) {
                            milstonecount++;
                            Element milestone = (Element) milestoneList.item(milesNo);
                            //                          String completedStatus = milestone.getElementsByTagName("completed").item(0).getChildNodes().item(0).getNodeValue();
                            //                          if(StringUtil.stringCompareInLowercase(completedStatus,"true")){
                            //                              String completed_on = milestone.getElementsByTagName("completed-on").item(0).getChildNodes().item(0).getNodeValue();
                            //                              String completer_id = milestone.getElementsByTagName("completed-on").item(0).getChildNodes().item(0).getNodeValue();
                            //                          }
                            String title = milestone.getElementsByTagName("title").item(0).getChildNodes().item(0).getNodeValue();
                            String mileStoneCreated_on = milestone.getElementsByTagName("created-on").item(0).getChildNodes().item(0).getNodeValue();
                            String deadline = milestone.getElementsByTagName("deadline").item(0).getChildNodes().item(0).getNodeValue();
                            String resourceid = milestone.getElementsByTagName("responsible-party-id").item(0).getChildNodes().item(0).getNodeValue();
                            //                              milestone.getElementsByTagName("responsible-party-type").item(0).getChildNodes().item(0).getNodeValue();
                            //                              milestone.getElementsByTagName("wants-notification").item(0).getChildNodes().item(0).getNodeValue();
                            NodeList commentList = ((Element) milestone.getElementsByTagName("comments").item(0)).getElementsByTagName("comment");
                            String commentBody = "";
                            for (int commentcout = 0; commentcout < commentList.getLength(); commentcout++) {
                                Element comment = (Element) commentList.item(commentcout);
                                //                                   comment.getElementsByTagName("attachments-count").item(0).getChildNodes().item(0).getNodeValue();
                                //                                   comment.getElementsByTagName("author-id").item(0).getChildNodes().item(0).getNodeValue();
                                commentBody = commentBody + comment.getElementsByTagName("body").item(0).getChildNodes().item(0).getNodeValue() + "<br>";
                                //                                   comment.getElementsByTagName("commentable-id").item(0).getChildNodes().item(0).getNodeValue();
                                //                                   comment.getElementsByTagName("commentable-type").item(0).getChildNodes().item(0).getNodeValue();
                                //                                   comment.getElementsByTagName("created-at").item(0).getChildNodes().item(0).getNodeValue();
                                //    //                                   comment.getElementsByTagName("emailed-from").item(0).getChildNodes().item(0).getNodeValue();
                                //    //                                   commentable-id
                            }
                            createTask(conn, cid, projid, title, mileStoneCreated_on, deadline, resourceid, commentBody, milesNo, jobj);
                            //                              body

                        }
                    }
                    //                         todo-lists
                    if (projobj.getString("todos").equals("1")) {
                        NodeList todo_lists = ((Element) project.getElementsByTagName("todo-lists").item(0)).getElementsByTagName("todo-list");
                        for (int todosNo = 0; todosNo < todo_lists.getLength(); todosNo++) {
                            todoscount++;
                            Element todoList = (Element) todo_lists.item(todosNo);
                            todoList.getElementsByTagName("completed-count").item(0).getChildNodes().item(0).getNodeValue();
                            String des = "";
                            if (todoList.getElementsByTagName("description").item(0).getChildNodes().item(0) != null) {
                                des = todoList.getElementsByTagName("description").item(0).getChildNodes().item(0).getNodeValue();
                            }
                            //                             todoList.getElementsByTagName("milestone-id").item(0).getChildNodes().item(0).getNodeValue();
                            String name = todoList.getElementsByTagName("name").item(0).getChildNodes().item(0).getNodeValue();
                            todoList.getElementsByTagName("position").item(0).getChildNodes().item(0).getNodeValue();
                            todoList.getElementsByTagName("tracked").item(0).getChildNodes().item(0).getNodeValue();
                            todoList.getElementsByTagName("uncompleted-count").item(0).getChildNodes().item(0).getNodeValue();
                            String toDoStatus = todoList.getElementsByTagName("complete").item(0).getChildNodes().item(0).getNodeValue();

                            //NodeList todo_items = todoList.getElementsByTagName("todo-items");
                            //                             Connection conn,String des,String parentid,JSONArray userMap,String status,boolean group,String assignedTo,String userid
                            String assignedTo = "";
                            String parentid = createTodo(conn, name, des, "", jobj, toDoStatus, false, assignedTo, lid, projid);
                            NodeList todo_items = ((Element) todoList.getElementsByTagName("todo-items").item(0)).getElementsByTagName("todo-item");


                            for (int todoitemsNo = 0; todoitemsNo < todo_items.getLength(); todoitemsNo++) {
                                todoscount++;
                                Element todoitem = (Element) todo_items.item(todoitemsNo);
                                toDoStatus = todoitem.getElementsByTagName("completed").item(0).getChildNodes().item(0).getNodeValue();
                                des = todoitem.getElementsByTagName("content").item(0).getChildNodes().item(0).getNodeValue();
                                todoitem.getElementsByTagName("created-on").item(0).getChildNodes().item(0).getNodeValue();
                                todoitem.getElementsByTagName("position").item(0).getChildNodes().item(0).getNodeValue();
                                todoitem.getElementsByTagName("todo-list-id").item(0).getChildNodes().item(0).getNodeValue();
                                if (todoitem.getElementsByTagName("responsible-party-id").item(0) != null) {
                                    assignedTo = todoitem.getElementsByTagName("responsible-party-id").item(0).getChildNodes().item(0).getNodeValue();
                                }
                                createTodo(conn, des, des, parentid, jobj, toDoStatus, true, assignedTo, lid, projid);
                            }
                        }
                    }
                    if (projobj.getString("post").equals("1")) {
                        NodeList posts = ((Element) project.getElementsByTagName("posts").item(0)).getElementsByTagName("post");
                        //                         topic_id, group_id, topic_title, topic_poster, post_time, post_subject,post_text, ifread,flag

                        for (int postNo = 0; postNo < posts.getLength(); postNo++) {
                            postscount++;
                            Element singlePost = (Element) posts.item(postNo);
                            String PostAuthId = singlePost.getElementsByTagName("author-id").item(0).getChildNodes().item(0).getNodeValue();
                            String PostBody = "";
                            if (singlePost.getElementsByTagName("body").item(0).getChildNodes().item(0) != null) {
                                PostBody = singlePost.getElementsByTagName("body").item(0).getChildNodes().item(0).getNodeValue();
                            }
                            String PostCommentCount = singlePost.getElementsByTagName("comments-count").item(0).getChildNodes().item(0).getNodeValue();
                            String postedon = singlePost.getElementsByTagName("posted-on").item(0).getChildNodes().item(0).getNodeValue();
                            String posttitle = "[Basecamp] " + singlePost.getElementsByTagName("title").item(0).getChildNodes().item(0).getNodeValue();
                            String topicid = insertDiscussionTopic(conn, projid, posttitle, PostAuthId, postedon, PostBody, jobj, lid);


                            NodeList subPosts = ((Element) singlePost.getElementsByTagName("comments").item(0)).getElementsByTagName("comment");
                            for (int subPostCount = 0; subPostCount < subPosts.getLength(); subPostCount++) {
                                postscount++;
                                Element subPost = (Element) subPosts.item(subPostCount);
                                String subpostAuthid = subPost.getElementsByTagName("author-id").item(0).getChildNodes().item(0).getNodeValue();
                                String subpostBody = "";
                                if (subPost.getElementsByTagName("body").item(0).getChildNodes().item(0) != null) {
                                    subpostBody = subPost.getElementsByTagName("body").item(0).getChildNodes().item(0).getNodeValue();
                                }
                                String subpostCreatedat = subPost.getElementsByTagName("created-at").item(0).getChildNodes().item(0).getNodeValue();
                                insertDiscussionSubPost(conn, projid, posttitle, subpostAuthid, subpostCreatedat, subpostBody, jobj, lid, topicid);
                            }

                        }
                    }
                    String userFullName = AuthHandler.getAuthor(conn, lid);
                    String params = userFullName + "(" + lName + ")," + projName;
                    AuditTrail.insertLog(conn, "325", lid, lid, projid, cid, params, ipAddress, 0);
                }
//                    }
            }
            result = String.format(MessageSourceProxy.getMessage("pm.admin.project.basecamp.importsuccess", null, locale), projcount, milstonecount, todoscount, postscount);
            conn.commit();
        } catch (Exception ex) {
            KrawlerLog.op.warn("Problem Occured while importing projects from base camp from " + ex.toString());
            DbPool.quietRollback(conn);
            result = MessageSourceProxy.getMessage("pm.project.basecamp.fileerror", null, locale);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    private static boolean checkNodeType(Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            return true;
        } else {
            return false;
        }
    }

    private static Element getElementFromNodeList(NodeList nl) {
        Element el = null;
        int len = nl.getLength();
        for (int i = 0; i < len; i++) {
            Node n = nl.item(i);
            if (checkNodeType(n)) {
                el = (Element) nl.item(i);
            }
        }
        return el;
    }
}
