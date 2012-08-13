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
import com.krawler.common.session.SessionExpiredException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.krawler.common.service.ServiceException;
import com.krawler.common.timezone.Timezone;
import com.krawler.database.DbPool;
import com.krawler.database.DbPool.Connection;
import com.krawler.utils.json.KWLJsonConverter;
import com.krawler.svnwebclient.configuration.*;
import javax.mail.*;
import java.util.*;
import javax.servlet.http.*;
import com.krawler.common.util.*;
import com.krawler.utils.json.base.JSONException;
import com.krawler.common.util.StringUtil;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.esp.company.Company;
import com.krawler.esp.company.CompanyDAO;
import com.krawler.esp.company.CompanyDAOImpl;
import com.krawler.esp.database.dbcon;
import java.text.ParseException;
import com.krawler.esp.portalmsg.Mail;
import com.krawler.esp.project.checklist.CheckListManager;
import com.krawler.esp.project.project.Project;
import com.krawler.esp.project.project.ProjectDAOImpl;
import com.krawler.esp.project.task.Task;
import com.krawler.utils.json.base.JSONObject;
import java.text.SimpleDateFormat;
import com.krawler.esp.web.resource.Links;
import com.krawler.utils.json.base.JSONArray;

public class todolist {

    public static String getToDoTask(Connection conn, String uid, int groupType, String loginid)
            throws ServiceException {
        PreparedStatement pstmt = null;
        String returnStr = "";
        try {
            KWLJsonConverter kjs = new KWLJsonConverter();
            ResultSet rs = null;
            pstmt = conn.prepareStatement("select taskname,taskid,taskorder,status,parentId,assignedto,leafflag,description,duedate,priority,ischecklisttask from todotask where userid = ? and grouptype= ?  order by leafflag,taskorder,timestamp");
            pstmt.setString(1, uid);
            pstmt.setInt(2, groupType);
            rs = pstmt.executeQuery();
            String json1 = null;
            json1 = kjs.GetJsonForGrid(rs);
//            JSONObject json = new JSONObject(json1);
//            if (json1.compareTo("{data:{}}") != 0) {
//                for (int i = 0; i < json.getJSONArray("data").length(); i++) {
//                    String dtStr = json.getJSONArray("data").getJSONObject(i).getString("duedate");
//                    json.getJSONArray("data").getJSONObject(i).remove("duedate");
//                    json.getJSONArray("data").getJSONObject(i).put("duedate", dtStr);
//                }
//            }
            returnStr = json1;
        } catch (SQLException e) {
            throw ServiceException.FAILURE("todolist.getToDoTask", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return returnStr;
    }

    public static String updateToDoTask(Connection conn, String taskname,
            int taskorder, int status, String parentId, String taskid,
            String assignedto, String dueDate, String desc, String priority, String userid) throws ServiceException, JSONException {
        PreparedStatement pstmt = null;
        String tid = "";
        com.krawler.utils.json.base.JSONObject jtemp = new com.krawler.utils.json.base.JSONObject();
        com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject();
        try {
            taskname = StringUtil.serverHTMLStripper(taskname);
            parentId = StringUtil.serverHTMLStripper(parentId);
            java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            dueDate = StringUtil.serverHTMLStripper(dueDate);
            dueDate = Timezone.getUserToGmtTimezone(conn, userid, dueDate);
            dueDate = dueDate.replace('-', '/');
            java.util.Date dd = sdf1.parse(dueDate);
            taskid = StringUtil.serverHTMLStripper(taskid);
            assignedto = StringUtil.serverHTMLStripper(assignedto);
            desc = StringUtil.serverHTMLStripper(desc);
            priority = StringUtil.serverHTMLStripper(priority);
            if (!(StringUtil.isNullOrEmpty(taskname)) && !(StringUtil.isNullOrEmpty(taskid))) {
                pstmt = conn.prepareStatement("update todotask set taskname=?,taskorder=?,"
                        + "status=?,parentId=?,assignedto=?,duedate=?,description=?,priority=? where taskid=?");
                pstmt.setString(1, taskname);
                pstmt.setInt(2, taskorder);
                pstmt.setInt(3, status);
                pstmt.setString(4, parentId);
                pstmt.setString(5, assignedto);
                pstmt.setObject(6, dd);
                pstmt.setString(7, desc);
                pstmt.setString(8, priority);
                pstmt.setString(9, taskid);
                pstmt.executeUpdate();
            }
            CheckListManager cm = new CheckListManager();
            cm.updateAssociatedTaskProgress(conn, taskid, false);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("todolist.updateToDoTask", e);
        } catch (ParseException ex) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        jtemp.put("remoteid", taskid);
        jobj.append("data", jtemp);
        return jobj.toString();
    }

    public static void deleteAllToDoTask(Connection conn, String projid)
            throws ServiceException {
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("delete from todotask where userid = ?");
            pstmt.setString(1, projid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.deleteAllRecords error", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

    public static int getLastTodoTaskIndex(Connection con, String projectid) {
        int index = 0;
        try {
            PreparedStatement p = con.prepareStatement("SELECT count(taskname) AS taskindex FROM todotask WHERE userid = ?");
            p.setString(1, projectid);
            ResultSet r = p.executeQuery();
            if (r.next()) {
                index = r.getInt("taskindex");
            }
        } catch (ServiceException ex) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException e) {
        }
        return index;
    }

    public static String updateToDoTask_Appendchild(Connection conn, String taskname,
            String task_order, String tstatus, String parentId, String taskid,
            String dueDate, String priority, String userid) throws ServiceException, JSONException {
        PreparedStatement pstmt = null;
        String result = "";
        String tid = "";
        com.krawler.utils.json.base.JSONObject jtemp = new com.krawler.utils.json.base.JSONObject();
        com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject();
        java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String[] tasknameArr = taskname.split(",");
        String[] taskorderArr = task_order.split(",");
        String[] parentIdArr = parentId.split(",");
        String[] taskidArr = taskid.split(",");
        String[] dueDateArr = dueDate.split(",");
        String[] priorityArr = priority.split(",");
        String[] statusArr = tstatus.split(",");
        int st = 0, tor = 0;
        try {
            for (int i = 0; i < taskidArr.length; i++) {
                if (!(StringUtil.isNullOrEmpty(tasknameArr[i])) && !(StringUtil.isNullOrEmpty(taskidArr[i]))) {
                    if (taskorderArr[i] != null) {
                        tor = Integer.parseInt(taskorderArr[i]);
                    }
                    if (statusArr[i] != null) {
                        st = Integer.parseInt(statusArr[i]);
                    }
                    dueDateArr[i] = Timezone.getUserToGmtTimezone(conn, userid, dueDateArr[i]).replace('-', '/');
                    java.util.Date dd = sdf1.parse(dueDateArr[i]);
                    pstmt = conn.prepareStatement("update todotask set taskname=?,taskorder=?,status=?,parentId=?,duedate=?,priority=? where taskid=?");
                    pstmt.setString(1, tasknameArr[i]);
                    pstmt.setInt(2, tor);
                    pstmt.setInt(3, st);
                    if (StringUtil.equal(parentIdArr[i], "-")) {
                        parentIdArr[i] = "";
                    }
                    pstmt.setString(4, parentIdArr[i]);
                    pstmt.setObject(5, dd);
                    pstmt.setString(6, priorityArr[i]);
                    pstmt.setString(7, taskidArr[i]);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("todolist.updateToDoTask", e);
        } catch (ParseException ex) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return result;
    }

    public static String updateToDoTask_changestatus(Connection conn, int status, String taskid) throws ServiceException {
        PreparedStatement pstmt = null;
        String result = KWLErrorMsgs.rsSuccessTrue;
        try {
            taskid = StringUtil.serverHTMLStripper(taskid);
            if (!(StringUtil.isNullOrEmpty(taskid))) {
                pstmt = conn.prepareStatement("update todotask set status=? where taskid=?");
                pstmt.setInt(1, status);
                pstmt.setString(2, taskid);
                pstmt.executeUpdate();
                CheckListManager cm = new CheckListManager();
                cm.updateAssociatedTaskProgress(conn, taskid, true);
            }
            pstmt = conn.prepareStatement("update todotask set status=? where parentId=?");
            pstmt.setInt(1, status);
            pstmt.setString(2, taskid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("todolist.updateToDoTask", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return result;
    }

    public static String insertToDoTask(Connection conn, String taskname,
            int taskorder, int status, String parentId, String taskid,
            String userid, int grouptype, String due, boolean leafflag, String localid,
            String priority, String loginid, String desc)
            throws ServiceException, JSONException {

        PreparedStatement pstmt = null;
        String tid = "";
        com.krawler.utils.json.base.JSONObject jtemp = new com.krawler.utils.json.base.JSONObject();
        com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject();

        try {
            taskname = StringUtil.serverHTMLStripper(taskname);
            java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            due = Timezone.getUserToGmtTimezone(conn, loginid, due);
            due = due.replace('-', '/');
            java.util.Date defaultdd = sdf1.parse(due);
            parentId = StringUtil.serverHTMLStripper(parentId);
            userid = StringUtil.serverHTMLStripper(userid);
            priority = StringUtil.serverHTMLStripper(priority);
            /* assignedto = StringUtil.serverHTMLStripper(assignedto);*/
            if (!(StringUtil.isNullOrEmpty(taskname))) {
                pstmt = conn.prepareStatement("insert into todotask (taskid,taskname,taskorder,status,parentId,description,timestamp,userid,grouptype,duedate,leafflag,priority) values(?,?,?,?,?,?,?,?,?,?,?,?)");
                java.sql.Timestamp timestamp = new java.sql.Timestamp(
                        new java.util.Date().getTime());
                tid = UUID.randomUUID().toString();
                pstmt.setString(1, tid);
                pstmt.setString(2, taskname);
                pstmt.setInt(3, taskorder);
                pstmt.setInt(4, status);
                pstmt.setString(5, parentId);
                pstmt.setString(6, desc);
                pstmt.setTimestamp(7, timestamp);
                pstmt.setString(8, userid);
                pstmt.setInt(9, grouptype);
                pstmt.setObject(10, defaultdd);
                pstmt.setBoolean(11, leafflag);
                pstmt.setString(12, priority);
                pstmt.execute();
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("todolist.insertToDoTask", e);
        } catch (ParseException ex) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        jtemp.put("localid", localid);
        jtemp.put("remoteid", tid);
        jobj.append("data", jtemp);
        return jobj.toString();
    }

    public static int insertToDoTaskFromCSV(Connection conn, String taskname,
            int taskorder, int status, String parentId, String taskid,
            String userid, int grouptype, Date due, boolean leafflag, String localid, String priority, String description)
            throws ServiceException {

        PreparedStatement pstmt = null;
        String tid = "";
        int skipped = 0;
        try {
            taskname = StringUtil.serverHTMLStripper(taskname);
            parentId = StringUtil.serverHTMLStripper(parentId);
            userid = StringUtil.serverHTMLStripper(userid);
            priority = StringUtil.serverHTMLStripper(priority);
            if (!(StringUtil.isNullOrEmpty(taskname))) {
                pstmt = conn.prepareStatement("insert into todotask (taskid,taskname,taskorder,status,parentId,timestamp,userid,grouptype,duedate,leafflag,priority,description) values(?,?,?,?,?,?,?,?,?,?,?,?)");
                java.sql.Timestamp timestamp = new java.sql.Timestamp(
                        new java.util.Date().getTime());
                tid = UUID.randomUUID().toString();
                pstmt.setString(1, tid);
                pstmt.setString(2, taskname);
                pstmt.setInt(3, taskorder);
                pstmt.setInt(4, status);
                pstmt.setString(5, parentId);
                pstmt.setTimestamp(6, timestamp);
                pstmt.setString(7, userid);
                pstmt.setInt(8, grouptype);
                pstmt.setObject(9, due);
                pstmt.setBoolean(10, leafflag);
                pstmt.setString(11, priority);
                pstmt.setString(12, description);
                pstmt.execute();
            } else {
                skipped = 1;
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("todolist.insertToDoTask", e);
        } catch (Exception ex) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return skipped;
    }

    public static String deleteToDoTask(Connection conn, String taskid, String leafflag, String userName, String loginid, String companyid, String ipAddress, int auditMode)
            throws ServiceException, JSONException {
        PreparedStatement pstmt = null;
        com.krawler.utils.json.base.JSONObject jtemp = new com.krawler.utils.json.base.JSONObject();
        com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject();
        String[] taskidArr = taskid.split(",");
        String[] leafflagArr = leafflag.split(",");
        String query = "delete from todotask where taskid = ? or parentid = ?";
        try {
            for (int i = 0; i < taskidArr.length; i++) {
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, taskidArr[i]);
                pstmt.setString(2, taskidArr[i]);
                int cnt = pstmt.executeUpdate();
                if (cnt > 0) {
                    if (Boolean.parseBoolean(leafflagArr[i])) {
                        dbcon.InsertAuditLogForTodo(taskidArr[i], userName, "133", loginid, companyid, ipAddress, auditMode);//insert as to-do task
                    } else {
                        dbcon.InsertAuditLogForTodo(taskidArr[i], userName, "136", loginid, companyid, ipAddress, auditMode);//insert as to-do group
                    }
                }
                pstmt.close();
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("todolist.deleteToDoTask", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        jtemp.put("remoteid", taskid);
        jobj.append("data", jtemp);
        return jobj.toString();
    }

    public static int deleteToDoTask(Connection conn, String taskid) throws ServiceException {
        int cnt = 0;
        PreparedStatement pstmt = null;
        String query = "delete from todotask where taskid = ? or parentid = ?";
        try {
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, taskid);
            pstmt.setString(2, taskid);
            cnt = pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("todolist.deleteToDoTask", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return cnt;
    }

    public static int getToDoStatus(Connection conn, String taskid) throws ServiceException {
        return DbUtil.executeQuery(conn, "select status from todotask where taskid = ?", taskid).getInt("status");
    }

    public static String getToDoDetails(Connection conn, String taskid)
            throws ServiceException {
        PreparedStatement pstmt = null;
        try {
            KWLJsonConverter kjs = new KWLJsonConverter();
            ResultSet rs = null;
            pstmt = conn.prepareStatement("select * from todotask where taskid = ?");
            pstmt.setString(1, taskid);
            rs = pstmt.executeQuery();
            String json = null;
            json = kjs.GetJsonForGrid(rs);
            return json;
        } catch (SQLException e) {
            throw ServiceException.FAILURE("todolist.getToDoDetails", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

    public static String sendToDoNotification(Connection conn, HttpServletRequest request)
            throws ServiceException, SessionExpiredException {
        String result = "true";
        String loginid = AuthHandler.getUserid(request);
        int auditMode = 0;
        int resourceCnt = 0;
        String companyid = AuthHandler.getCompanyid(request);
        boolean type = projdb.isEmailSubscribe(conn, companyid);
        if (type) {
            String subdomain = CompanyHandler.getCompanySubdomainByCompanyID(conn, companyid);
            PreparedStatement pstmt = null;
            PreparedStatement pstmtCnt = null;
            String todoquery = "select todotask.taskname,todotask.duedate,todotask.description,todotask.priority "
                    + "from todotask "
                    + "where taskid in (" + request.getParameter("idstr") + ") and assignedto = ? and status = 0";

            String query1 = "select concat(fname,' ',lname)as name,emailid from users where userid = ?";
            String sendername = "";
            String sendermailid = "";
            String username = "";
            String reciver = "";
            String mailid = "";
            String projectname = "";
            String receiverid = "";
            String df = "E, MMM dd, yyyy";
            String[] taskid = request.getParameter("idstr").split(",");
            String[] assignarr = request.getParameter("assignidstr").split(",");
            List l = Arrays.asList(assignarr);
            ArrayList<String> as = new ArrayList<String>();
            HashSet<String> hs = new HashSet<String>();
            hs.addAll(l);
            as.addAll(hs);
            java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String userquery = "select users.emailid,concat(users.fname,' ',users.lname)as username,userlogin.username as reciver, users.userid as receiverid from users INNER JOIN userlogin on users.userid=userlogin.userid  where users.userid=?";
            String projectQuery = "Select projectname from project where projectid = ?";
            String resourceCountQry = " select count(*) as count from ("+userquery+") as tmp ";
            try {
                ResultSet rs = null;
                ResultSet rsCnt = null;
                pstmt = conn.prepareStatement(query1);
                pstmt.setString(1, AuthHandler.getUserid(request));
                rs = pstmt.executeQuery();
                rs.next();
                sendername = rs.getString("name");
//                    sendermailid = rs.getString("emailid");
              //  sendermailid = KWLErrorMsgs.notificationSenderId;
                sendermailid = CompanyHandler.getSysEmailIdByCompanyID(conn, companyid);
                rs.close();
                pstmt.close();
                pstmt = conn.prepareStatement(projectQuery);
                pstmt.setString(1, request.getParameter("userid"));
                rs = pstmt.executeQuery();
                rs.next();
                projectname = rs.getString("projectname");
                rs.close();
                pstmt.close();
                for (int i = 0; i < as.size(); i++) {
                    pstmt = conn.prepareStatement(userquery);
                    pstmt.setString(1, as.get(i).toString());
                    rs = pstmt.executeQuery();
                    pstmtCnt = conn.prepareStatement(resourceCountQry);
                    pstmtCnt.setString(1, as.get(i).toString());
                    rsCnt = pstmtCnt.executeQuery();
                    if(rsCnt.next()){
                       int Cnt = rsCnt.getInt("count");
                       resourceCnt=resourceCnt+Cnt;
                    }
                    if (rs.next()) {
                        username = rs.getString("username");
                        mailid = rs.getString("emailid");
                        reciver = rs.getString("reciver");
                        receiverid = rs.getString("receiverid");
                        rs.close();
                        pstmt.close();
                        pstmt = conn.prepareStatement(todoquery);
                        pstmt.setString(1, as.get(i).toString());
                        rs = pstmt.executeQuery();
                        String pmsg = String.format("Hi %s,\n\nYou have been assigned the following To-Dos in the project: %s\n\nTo-Do:", username, projectname);

                        String htmlmsg = String.format("<html><head><title>Notification - To-Dos for %s </title></head><style type='text/css'>"
                                + "a:link, a:visited, a:active {\n"
                                + " 	color: #03C;"
                                + "}\n"
                                + "body {\n"
                                + "	font-family: tahoma, Helvetica, sans-serif;"
                                + "	color: #000;"
                                + "	font-size: 13px;"
                                + "}\n"
                                + "</style><body>"
                                + "	<div>"
                                + "		<p>Hi <strong>%s</strong>,</p>"
                                + "		<p>You have been assigned the following To-Dos in the project: <strong>%s</strong></p>", projectname, username, projectname);

                        String htmlmsg1 = "";
                        String pmsg1 = "";
                        int count = 1;
                        String due = "";
                        while (rs.next()) {
                            if (!rs.getString("duedate").contains("1970-01-01")) {
                                due = rs.getString("duedate");
                                due = due.split(" ")[0];
                            } else {
                                due = "Not Specified";
                            }
                            pmsg1 = String.format("\n %d) %s ", count, rs.getString("taskname"), due);
                            htmlmsg1 = String.format("<p> <strong>%d) %s</strong> <p>Priority: %s </p><p>Due on: %s </p><p>Details: %s</p>", count++, rs.getString("taskname"), rs.getString("priority"), due, rs.getString("description"));
                            pmsg += pmsg1;
                            htmlmsg += htmlmsg1;
                        }
                        if (count > 1) {
                            try {
                                String uri = URLUtil.getPageURL(request, Links.loginpageFull, subdomain);
                                htmlmsg1 = String.format("<p>You can log in at:</p><p><a href = %s>%s</a></p><br/><p> - %s </p></div></body></html>", uri, uri, sendername);
                                pmsg1 = String.format("\n\nYou can log in at:\n%s\n\n - %s ", uri, sendername);
                                String insertMsg = Mail.insertMailMsg(conn, reciver, loginid, "To-Dos Notification", htmlmsg, "1", false, "1", "", "newmsg", "", 3, "", companyid);
                                pmsg += pmsg1;
                                htmlmsg += htmlmsg1;

                                SendMailHandler.postMail(new String[]{mailid},
                                        "To-Do alerts", htmlmsg, pmsg,
                                        sendermailid);
                            } catch (ConfigurationException e) {
                                result = "false";
                                e.printStackTrace();
                            } catch (MessagingException e) {
                                result = "false";
                                e.printStackTrace();
                            } catch (java.text.ParseException e) {
                                result = "false";
                                e.printStackTrace();
                            } catch (JSONException e) {
                                result = "false";
                                e.printStackTrace();
                            }
                        }
                        rs.close();
                        pstmt.close();
                    }
                }
                    String userName = AuthHandler.getUserName(request);
                    String ipAddress = AuthHandler.getIPAddress(request);
                    String projectid = request.getParameter("userid");
                    String params = AuthHandler.getAuthor(conn, loginid) + " (" + userName + "), " +resourceCnt+",TO-DO";
            AuditTrail.insertLog(conn, "410", loginid, projectid, projectid,companyid, params, ipAddress, auditMode);
            } catch (SQLException e) {
                result = "false";
                throw ServiceException.FAILURE("todolist.getToDoDetails", e);
            } finally {
                DbPool.closeStatement(pstmt);
            }
        } else {
            result = "typeError";
        }
        return result;
    }

    public static String[] getChilds(Connection conn, String taskid) throws ServiceException {
        ArrayList childarr = new ArrayList();
        String[] retarr;
        DbResults rs = DbUtil.executeQuery("select taskid from todotask where parentid=? and leafflag = 1 order by timestamp", new Object[]{taskid});
        while (rs.next()) {
            childarr.add(rs.getString("taskid"));
        }
        retarr = new String[childarr.size()];
        childarr.toArray(retarr);
        return retarr;
    }

    public static String updateParent(Connection conn, String[] taskArr, JSONObject jobj, String projectid)
            throws ServiceException {
        try {
            String sdt = "";
            String edt = "";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            double percentcomplete = 0, totaldur = 0, duration = 0;
            for (int i = 0; i < taskArr.length; i++) {
                percentcomplete = 0;
                duration = 0;
                totaldur = 0;
                DbResults dirChilds = DbUtil.executeQuery(conn, "select taskid, duration, percentcomplete from proj_task where parent = ? and projectid = ?",
                        new Object[]{taskArr[i], projectid});
                while (dirChilds.next()) {
                    String strdur = dirChilds.getString("duration");
                    duration = projdb.getDuration(strdur);
                    percentcomplete += (int) (duration * ((double) dirChilds.getInt("percentcomplete") / 100));
                    totaldur += duration;
                }
                DbResults rs = DbUtil.executeQuery(conn, "select projectid, Min(startdate) as stdate , Max(enddate) as edate from proj_task where parent=? and projectid = ? group by projectid",
                        new Object[]{taskArr[i], projectid});
                if (rs.next()) {
                    double percent = 0;
                    if (totaldur != 0) {
                        percent = ((percentcomplete / totaldur) * 100);
                    }
                    if (!rs.isNull("stdate")) {
                        sdt = rs.getObject("stdate").toString();
                        edt = rs.getObject("edate").toString();
                        Date stdate = sdf.parse(sdt);
                        Date endate = sdf.parse(edt);

                        int[] nonworkweekArr = projdb.getNonWorkWeekDays(conn, rs.getString("projectid"));
                        String holidayArr[] = projdb.getCompHolidays(conn, rs.getString("projectid"), "");
                        String dur = projectReport.calculateWorkingDays(stdate, endate, nonworkweekArr, holidayArr) + "";
                        duration = Double.parseDouble(dur);
                        percentcomplete = (int) percent;

                        DbUtil.executeUpdate(conn, "UPDATE proj_task set startdate=?, enddate=?, duration =?,percentcomplete=? where taskid=?",
                                new Object[]{stdate, endate, duration, percentcomplete, taskArr[i]});
                        for (int j = 0; j < jobj.getJSONArray("data").length(); j++) {
                            if (jobj.getJSONArray("data").getJSONObject(j).getString("taskid").equals(taskArr[i])) {
                                jobj.getJSONArray("data").getJSONObject(j).remove("startdate");
                                jobj.getJSONArray("data").getJSONObject(j).put("startdate", sdf.format(sdf.parse(sdt)));
                                jobj.getJSONArray("data").getJSONObject(j).remove("enddate");
                                jobj.getJSONArray("data").getJSONObject(j).put("enddate", sdf.format(sdf.parse(edt)));
                                jobj.getJSONArray("data").getJSONObject(j).remove("duration");
                                jobj.getJSONArray("data").getJSONObject(j).put("duration", duration);
                                jobj.getJSONArray("data").getJSONObject(j).remove("percentcomplete");
                                jobj.getJSONArray("data").getJSONObject(j).put("percentcomplete", percentcomplete);
                            }
                        }
                    }
                }
            }
            return jobj.toString();
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("todolist.updateparent : " + ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("todolist.updateparent : " + ex.getMessage(), ex);
        }
    }

    public static String addToDoInProjectplan(Connection conn,HttpServletRequest req) throws ServiceException {
        String taskid =req.getParameter("taskid");
        String projectid = req.getParameter("userid");
        String[] taskidArr = taskid.split(",");
        String ret = KWLErrorMsgs.rsSuccessFalse;
        String jret = null;
        JSONObject jObj = new JSONObject();
        int count = 0;
        try {
            String loginid = AuthHandler.getUserid(req);
            DbResults rsCount = DbUtil.executeQuery(conn, "select count(*) as count from proj_task where projectid = ?", projectid);
            if (rsCount.next()) {
                count = rsCount.getInt("count");
            }
            for (int i = 0; i < taskidArr.length; i++) {
                boolean isparent = false;
                DbResults rs = DbUtil.executeQuery(conn, "select taskid,parentid,leafflag, userid as projectid from todotask where taskid=?", new Object[]{taskidArr[i]});
                if (rs.next()) {
                    projectid = rs.getString("projectid");
                    String pid = rs.getString("parentid");
                    boolean l = rs.getBoolean("leafflag");
                    if ((StringUtil.isNullOrEmpty(pid)) && !l) { // is parent (ie todo group) and not leaf
                        String[] childarr = getChilds(conn, taskidArr[i]);
                        if (childarr == null || childarr.length == 0) {
                            jret = insertToProjectPlan(conn, taskidArr[i], projectid, loginid, "0", isparent);// parent insert
                            JSONObject json1 = new JSONObject(jret);
                            json1.remove("isparent");
                            json1.put("isparent", isparent);
                            jObj.append("data", json1);
                        } else { // parent ie todo group
                            isparent = true;
                            jret = insertToProjectPlan(conn, taskidArr[i], projectid, loginid, "0", isparent);
                            JSONObject json1 = new JSONObject(jret);
                            json1.remove("isparent");
                            json1.put("isparent", isparent);
                            jObj.append("data", json1);
                            for (int j = 0; j < childarr.length; j++) {
                                isparent = false;
                                jret = insertToProjectPlan(conn, childarr[j], projectid, loginid, taskidArr[i], isparent);//insert child
                                JSONObject json2 = new JSONObject(jret);
                                json2.remove("isparent");
                                json2.put("isparent", isparent);
                                jObj.append("data", json2);
                            }
                        }
                    } else { // not a parent ie todo task
                        jret = insertToProjectPlan(conn, taskidArr[i], projectid, loginid, "0", isparent);
                        JSONObject json3 = new JSONObject(jret);
                        json3.remove("isparent");
                        json3.put("isparent", false);
                        jObj.append("data", json3);
                    }
                    String query = "delete from todotask where taskid = ? or parentid = ?";
                    int a = DbUtil.executeUpdate(conn, query, new Object[]{taskidArr[i], taskidArr[i]});

                }
            }
            ret = updateParent(conn, taskidArr, jObj, projectid);// updating the Parent node for startdate, enddate, duration, actualduration and percentcomplete
            jObj = new JSONObject();
            jObj.put("projid", projectid);
            jObj.put("userid", loginid);
            jObj.put("data", ret);
            jObj.put("rowindex", String.valueOf(count));
            String projName = projdb.getProjectName(conn, projectid);
            String taskName = projdb.getTaskName(conn, taskid);
            String userFullName = AuthHandler.getAuthor(conn, loginid);
            String companyid = AuthHandler.getCompanyid(req);
            String userName = AuthHandler.getUserName(req);
            String params = userFullName + "(" + userName + ")," + taskName + "," + projName;
            String ipAddress = AuthHandler.getIPAddress(req);
           AuditTrail.insertLog(conn, "130", loginid, loginid, projectid, companyid, params, ipAddress, 0);

        } catch (JSONException ex) {
            throw ServiceException.FAILURE("todolist.addToDoInProjectplan : " + ex.getMessage(), ex);
        }catch (SessionExpiredException ex) {
                Logger.getLogger(todolist.class.getName()).log(Level.SEVERE, null, ex);
            }
        return jObj.toString();
    }

    public static String insertToProjectPlan(Connection conn, String taskid, String projectid, String loginid, String parentid, boolean isparent)
            throws ServiceException {

        com.krawler.utils.json.base.JSONObject json = new com.krawler.utils.json.base.JSONObject();
        String retresult = null;
        try {
            DbResults rs = DbUtil.executeQuery(conn, "select taskid,leafflag,parentid as parent,status, userid as projectid,description as notes,taskname,priority,assignedto as resourcename,duedate as enddate,timestamp as startdate from todotask where taskid = ?", new Object[]{taskid});
            if (rs.next()) {
                java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd");
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                java.util.Date curdate = sdf1.parse("1970-01-01 00:00:00");
                String dur = "0";
                int duration = 0;
                String actdur = "0";
                String tid = rs.getString("taskid");
                if (!StringUtil.isNullOrEmpty(tid)) {
                    json.put("taskid", tid);
                }
                json.put("loginid", loginid);
                boolean lflag = rs.getBoolean("leafflag");
                String level = "";
                if (StringUtil.isNullOrEmpty(parentid)/*parentid.equals("0")*/) {
                    if (!isparent) {
                        json.put("isparent", isparent);
                        json.put("parent", parentid);
                        json.put("level", "1");
                        level = "1";
                    } else {
                        json.put("isparent", lflag ? false : true);
                        json.put("parent", parentid);
                        json.put("level", "0");
                        level = "0";
                    }
                } else {
                    json.put("isparent", isparent);
                    json.put("parent", parentid);
                    json.put("level", "1");
                    level = "1";
                }
                String dendString = rs.getObject("enddate").toString();
                Date dend = sdf1.parse(dendString);
                String notes = rs.getString("notes");
                if (StringUtil.isNullOrEmpty(notes)) {
                    json.put("notes", "");
                } else {
                    json.put("notes", notes);
                }
                String tname = rs.getString("taskname");
                json.put("taskname", tname);
                int status = rs.getInt("status");
                if (status == 1) {
                    status = 100;
                    json.put("percentcomplete", status);
                } else {
                    status = 0;
                    json.put("percentcomplete", status);
                }
                String prio = rs.getString("priority");
                if (StringUtil.isNullOrEmpty(prio) || prio.equalsIgnoreCase("Moderate")) { // correct priority field
                    prio = "1";
                    json.put("priority", prio);
                } else {
                    /*
                     * priority values
                     * 0:- high
                     * 1:- normal/moderate
                     * 2:- low
                     */
                    if (prio.equalsIgnoreCase("Low")) {
                        prio = "2";
                        json.put("priority", prio);
                    } else {
                        prio = "0";
                        json.put("priority", prio);
                    }
                }
                int nonworkweekArr[] = projdb.getNonWorkWeekDays(conn, projectid);
                String holidayArr[] = projdb.getCompHolidays(conn, projectid, "");

                String projStartDate = projdb.getProjectStartDate(conn, projectid, loginid);
                String companyid = CompanyHandler.getCompanyByUser(conn, loginid);
                projStartDate = Timezone.fromCompanyToSystem(conn, projStartDate, companyid);
                Date projstdate = sdf1.parse(projStartDate);

                int resl = dend.compareTo(curdate);
                int result = dend.compareTo(projstdate);

                if (resl == 0 || resl < 0) { // if due date not specified
                    Date presentdate = new Date();
                    int check = presentdate.compareTo(projstdate);

                    if (check > 0) { // if todays date is after project start date
                        String stdt = "";
                        Calendar c = Calendar.getInstance();
                        c.setTime(presentdate);
                        if (Arrays.binarySearch(nonworkweekArr, (c.get(Calendar.DAY_OF_WEEK) - 1)) > -1 || Arrays.binarySearch(holidayArr, sdf1.format(c.getTime())) > -1) {
                            stdt = projdb.getNextWorkingDay(sdf1.format(presentdate), nonworkweekArr, holidayArr);
                        } else {
                            stdt = sdf1.format(presentdate);
                        }
                        Date enddate = projdb.calculateEndDate(conn, sdf1.parse(stdt), "1", nonworkweekArr, holidayArr, loginid);
                        json.put("startdate", stdt);
                        json.put("enddate", sdf.format(enddate));
                        json.put("duration", duration = 1);
                        json.put("actualduration", actdur = "1");
                        json.put("actstartdate", stdt);

                    } else {
                        duration = 1;
                        actdur = "1";
                        String stdt = "";
                        Calendar c = Calendar.getInstance();
                        c.setTime(projstdate);
                        if (Arrays.binarySearch(nonworkweekArr, (c.get(Calendar.DAY_OF_WEEK) - 1)) > -1 || Arrays.binarySearch(holidayArr, sdf1.format(c.getTime())) > -1) {
                            stdt = projdb.getNextWorkingDay(sdf.format(projstdate), nonworkweekArr, holidayArr);
                        } else {
                            stdt = sdf.format(projstdate);
                        }
                        Date enddate = projdb.calculateEndDate(conn, sdf.parse(stdt), String.valueOf(duration), nonworkweekArr, holidayArr, loginid);
                        json.put("startdate", stdt);
                        json.put("duration", duration);
                        json.put("actualduration", actdur);
                        json.put("enddate", sdf.format(enddate));
                        json.put("actstartdate", stdt);
                    }
                } else { // due date is specified

                    if (result > 0) {  // if due date is after project start date
                        String stdt = "";
                        Calendar c = Calendar.getInstance();
                        c.setTime(projstdate);
                        if (Arrays.binarySearch(nonworkweekArr, (c.get(Calendar.DAY_OF_WEEK) - 1)) > -1 || Arrays.binarySearch(holidayArr, sdf1.format(c.getTime())) > -1) {
                            stdt = projdb.getNextWorkingDay(sdf.format(projstdate), nonworkweekArr, holidayArr);
                        } else {
                            stdt = sdf.format(projstdate);
                        }
                        dur = projectReport.calculateWorkingDays(sdf.parse(stdt), dend, nonworkweekArr, holidayArr) + "";
                        duration = Integer.parseInt(dur);
                        actdur = dur;
                        c.setTime(dend);
                        if (Arrays.binarySearch(nonworkweekArr, (c.get(Calendar.DAY_OF_WEEK) - 1)) > -1 || Arrays.binarySearch(holidayArr, sdf1.format(c.getTime())) > -1) {
                            dend = sdf.parse(projdb.getNextWorkingDay(sdf.format(c.getTime()), nonworkweekArr, holidayArr));
                        } else {
                            dend = c.getTime();
                        }
                        json.put("duration", duration);
                        json.put("actualduration", actdur);
                        json.put("startdate", stdt);
                        json.put("enddate", sdf.format(dend));
                        json.put("actstartdate", stdt);

                    } else { // due date is before project start date

                        duration = 1;
                        actdur = "1";
                        String stdt = "";
                        Calendar c = Calendar.getInstance();
                        c.setTime(projstdate);
                        if (Arrays.binarySearch(nonworkweekArr, (c.get(Calendar.DAY_OF_WEEK) - 1)) > -1 || Arrays.binarySearch(holidayArr, sdf1.format(c.getTime())) > -1) {
                            stdt = projdb.getNextWorkingDay(sdf.format(projstdate), nonworkweekArr, holidayArr);
                        } else {
                            stdt = sdf.format(projstdate);
                        }
                        Date enddate = projdb.calculateEndDate(conn, sdf.parse(stdt), String.valueOf(duration), nonworkweekArr, holidayArr, loginid);
                        json.put("startdate", stdt);
                        json.put("duration", duration);
                        json.put("actualduration", actdur);
                        json.put("enddate", sdf.format(enddate));
                        json.put("actstartdate", stdt);
                    }
                }

                int rindex = projdb.getLastTaskIndex(conn, projectid);
                String rowindex = Integer.toString(rindex);
                String links = "";
                json.put("links", links);

                String tempdt = json.getString("startdate");
                json.remove("startdate");
                json.put("startdate", sdf.format(sdf1.parse(tempdt)));
                tempdt = json.getString("enddate");
                json.remove("enddate");
                json.put("enddate", sdf.format(sdf1.parse(tempdt)));
                tempdt = json.getString("actstartdate");
                json.remove("actstartdate");
                json.put("actstartdate", sdf.format(sdf1.parse(tempdt)));

                //  above json created for InsertTask Fuction

                String[] pdates = projdb.InsertTask(conn, json, taskid, projectid, rowindex).split(",");

                /* updation done for publishing data */
                json.remove("startdate");
                json.put("startdate", pdates[0]);
                json.remove("enddate");
                json.put("enddate", pdates[1]);
                json.remove("actstartdate");
                json.put("actstartdate", pdates[2]);
                retresult = json.toString();

                if (!StringUtil.isNullOrEmpty(rs.getString("resourcename"))) { // has resource assigned to it
                    String resourceArray = rs.getString("resourcename");
                    String jsonstr = "[{\"resourceid\":\"" + resourceArray + "\"}]";

                    retresult = projdb.insertTaskResource(conn, jsonstr, taskid, loginid);

                    JSONObject jtemp = new JSONObject(retresult);
                    String temp = jtemp.getString("resources");
                    jtemp.remove("resources");
                    jtemp.put("resourcename", temp);
                    jtemp.put("level", level);
                    jtemp.put("parent", parentid);
                    jtemp.put("priority", prio);
                    retresult = jtemp.toString();

                } else {
                    retresult = json.toString();
                }
            }
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("todolist.insertToProjectPlan : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("todolist.insertToProjectPlan : " + ex.getMessage(), ex);
        }
        return retresult;
    }

    public static int getTaskOrder(Connection conn, String todoID, String projectID, boolean isGroup) throws ServiceException {
        int taskOrder = 0;
        if(isGroup){
            DbResults rs = DbUtil.executeQuery(conn, "SELECT max(taskorder) as taskorder FROM todotask "
                    + "WHERE userid = ? AND leafflag = false GROUP BY userid", projectID);
            if (rs.next()) {
                taskOrder = rs.getInt("taskorder");
            }
        } else {
            DbResults rs = DbUtil.executeQuery(conn, "SELECT count(taskid) as count FROM todotask "
                + "WHERE parentid = ? AND userid = ? AND leafflag = true GROUP BY taskid", new Object[]{todoID, projectID});
            if (rs.next()) {
                taskOrder = rs.getInt("count");
            }
        }
        return taskOrder;
    }

    public static int createCheckListToDoGroup(Connection conn, String toDoGroupID, String checkListName, String projectID, Task t) throws ServiceException {
        int done = 0;
        try {
            Project p = new ProjectDAOImpl().getProject(conn, projectID);
            Locale l = new CompanyDAOImpl().getCompanyLocale(conn, p.getCompanyID());

            String tName = MessageSourceProxy.getMessage("pm.project.plan.cpa.taskno", null, l) + " " + t.getTaskIndex() + " - " + t.getTaskName().concat(" - ").concat(checkListName);

            int taskOrder = getTaskOrder(conn, toDoGroupID, projectID, true);
            PreparedStatement pstmt = null;
            pstmt = conn.prepareStatement("INSERT INTO todotask (taskid,taskname,taskorder,status,parentId,description,timestamp,userid,grouptype,leafflag,priority,ischecklisttask) "
                    + "values(?,?,?,?,?,?,?,?,?,?,?,?)");
            java.sql.Timestamp timestamp = new java.sql.Timestamp(new java.util.Date().getTime());
            pstmt.setString(1, toDoGroupID);
            pstmt.setString(2, tName);
            pstmt.setInt(3, taskOrder);
            pstmt.setInt(4, 0);
            pstmt.setString(5, "");
            pstmt.setString(6, "");
            pstmt.setTimestamp(7, timestamp);
            pstmt.setString(8, projectID);
            pstmt.setInt(9, 2);
            pstmt.setBoolean(10, false);
            pstmt.setString(11, "Moderate");
            pstmt.setBoolean(12, true);
            done = pstmt.executeUpdate();
        } catch (SQLException ex) {
            throw ServiceException.FAILURE("todolist.createCheckListToDoGroup : " + ex.getMessage(), ex);
        }
        return done;
    }

    public static int createCheckListToDoTask(Connection conn, String toDoGroupID, String taskName, String priority, String projectID) throws ServiceException {
        int done = 0;
        try {
            String todoTaskID = UUID.randomUUID().toString();
            int taskOrder = getTaskOrder(conn, toDoGroupID, projectID, false);
            PreparedStatement pstmt = null;
            pstmt = conn.prepareStatement("INSERT INTO todotask (taskid,taskname,taskorder,status,parentId,description,timestamp,userid,grouptype,leafflag,priority,ischecklisttask) "
                    + "values(?,?,?,?,?,?,?,?,?,?,?,?)");
            java.sql.Timestamp timestamp = new java.sql.Timestamp(new java.util.Date().getTime());
            pstmt.setString(1, todoTaskID);
            pstmt.setString(2, taskName);
            pstmt.setInt(3, taskOrder + 1);
            pstmt.setInt(4, 0);
            pstmt.setString(5, toDoGroupID);
            pstmt.setString(6, "");
            pstmt.setTimestamp(7, timestamp);
            pstmt.setString(8, projectID);
            pstmt.setInt(9, 2);
            pstmt.setBoolean(10, true);
            pstmt.setString(11, priority);
            pstmt.setBoolean(12, true);
            done = pstmt.executeUpdate();
        } catch (SQLException ex) {
            throw ServiceException.FAILURE("todolist.createCheckListToDoTask : " + ex.getMessage(), ex);
        }
        return done;
    }

    public static int updateToDoTaskStatus(Connection conn, String taskid, int status) throws ServiceException {
        PreparedStatement pstmt = null;
        int done = 0;
        try {
            pstmt = conn.prepareStatement("update todotask set status=? where taskid=?");
            pstmt.setInt(1, status);
            pstmt.setString(2, taskid);
            done = pstmt.executeUpdate();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("todolist.updateToDoTask", e);
        }
        return done;
    }

    public static JSONArray getCheckListToDos(Connection conn, String mappingID, String projectID) throws ServiceException {
        JSONArray jarr = new JSONArray();
        try {
            DbResults rs = DbUtil.executeQuery(conn, "SELECT * FROM todotask WHERE parentid = ? AND userid = ? AND ischecklisttask = true",
                    new Object[]{mappingID, projectID});
            while (rs.next()) {
                JSONObject jobj = new JSONObject();
                jobj.put("ctaskid", rs.getString("taskid"));
                jobj.put("ctaskname", rs.getString("taskname"));
                jobj.put("status", rs.getInt("status"));
                jobj.put("parentid", rs.getString("parentId"));
                jobj.put("priority", rs.getString("priority"));
                jobj.put("leaf", rs.getBoolean("leafflag"));
                jarr.put(jobj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("todolist.getCheckListToDos", ex);
        }
        return jarr;
    }

    public static String getParentID(Connection conn, String todoID) throws ServiceException {
        String parentid = "";
        DbResults rs = DbUtil.executeQuery(conn, "SELECT parentid FROM todotask WHERE taskid = ?", new Object[]{todoID});
        if (rs.next()) {
            parentid = rs.getString("parentid");
        }
        return parentid;
    }
}
