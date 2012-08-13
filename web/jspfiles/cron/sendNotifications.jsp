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
    Document   : sendNotifications
    Created on : Jul 12, 2010, 11:05:21 AM
    Author     : Abhay
--%>

<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"   "http://www.w3.org/TR/html4/loose.dtd">
<%@ page import = "java.util.Calendar"%>
<%@ page import = "java.util.Date"%>
<%@ page import = "java.text.SimpleDateFormat"%>
<%@ page import ="com.krawler.esp.database.*" %>
<%@ page import ="com.krawler.database.*"%>
<%@ page import ="com.krawler.common.util.URLUtil"%>
<%@ page import ="com.krawler.common.util.StringUtil"%>
<%@ page import ="com.krawler.common.util.KWLErrorMsgs"%>
<%@ page import ="com.krawler.common.service.ServiceException"%>
<%@ page import ="com.krawler.common.timezone.Timezone"%>
<%@ page import ="com.krawler.utils.json.base.JSONException"%>
<%@ page import = "java.text.ParseException"%>
<%@ page import = "com.krawler.esp.handlers.*"%>
<%@ page import = "java.util.Arrays"%>
<%@ page import = "com.krawler.esp.handlers.calEvent"%>
<%@ page import = "com.krawler.esp.utils.ConfigReader"%>
<%@ page import = "com.krawler.utils.json.base.JSONObject"%>
<%@ page import ="com.krawler.esp.handlers.projdb"%>
<%@ page import ="javax.mail.MessagingException"%>
<%@ page import ="com.krawler.esp.portalmsg.Mail"%>
<%@ page import ="com.krawler.svnwebclient.configuration.ConfigurationException"%>
<%@ page import="com.krawler.esp.web.resource.Links"%>
<%
    DbPool.Connection conn = null;
    JSONObject jtemp = new JSONObject();
    int projectPlan = 1, toDo = 2, calendar = 3;
    String df = "E, MMM dd, yyyy";
    String dfTime = "HH:mm a";
    SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
    java.util.Date d = new Date();
    String javaCurrentTime = sdfDateTime.format(d);
    String bottomTextHTML = "";
    String bottomTextPlain = "";
    String projectid = "";
    String tid = "";
    int auditMode = 0;
    String taskNotificationMainText = "<html><head><title>Notification - Tasks for </title></head><style type='text/css'>"
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
            + "               <p></p>";
    String taskNotificationTaskHTMLText = "<p><b> %s) %s</b> <br> Due On: %s  <br> (%s Complete)- %s Priority</p>";
    String taskNotificationTaskPlainText = "\n %s) %s \n Due On: %s \n (%s Complete)- %s Priority";
    String taskNotificationOtherTaskHTMLText = "<p> %s) %s </p><div style ='margin-left:10px;'><p>Start Date: %s</p><p>Duration: %s</p><p>Priority: %s</p></div>";
    String taskNotificationOtherTaskPlainText = "\n %s) %s \n\nStart Date: %s\n\nDuration: %s\n\nPriority: %s";


    String toDoNotificationMainText = "<html><head><title>Notifications - To-Do task</title></head><style type='text/css'>"
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
            + "<p></p>";
    String toDoNotificationTaskHTMLText = "<p><strong>%d) %s</strong><div style ='margin-left:10px;'><p>Priority: %s </p><p>Details: %s</p></div>";
    String toDoNotificationTaskPlainText = "\n %d) %s ";
    String toDoNotificationOtherTaskHTMLText = "<p> <strong>%d) %s</strong> <div style ='margin-left:10px;'><p>Priority: %s </p><p>Due on: %s </p><p>Details: %s</p></div>";
    String toDoNotificationOtherTaskPlainText = "\n %s) %s \n\nPriority: %s\n\nDue Date: %s\n\nDetails: %s";

    String eventNotificationMainText = "<html><head><title>Notifications - Events</title></head><style type='text/css'>"
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
            + "		<p>Hi <strong>%s</strong>,</p>";
    String eventNotificationHTMLText = "<p><strong>%s) %s</strong> - %s</p><div style ='margin-left:10px;'><p>Location: %s </p><p>Priority: %s </p><p>Time: %s</p></div>";
    String eventNotificationPlainText = "\n %s) %s - %s\n\nLocation: %s\n\nPriority: %s\n\nTime: %s";
    String eventNotificationOtherHTMLText = "<p><strong> %s) %s</strong> -%s</p><div style ='margin-left:10px;'><p>Start Date: %s</p><p>End Date: %s</p>"
            + "<p>Priority: %s</p><p>Location: %s</p></div>";
    String eventNotificationOtherPlainText = "\n %s) %s - %s\n\nStart Date: %s\nEnd Date: %s\nPriority: %s\nLocation: %s";
    String appURL = ConfigReader.getinstance().get("platformURL");

    try {
        conn = DbPool.getConnection();
        DbResults rsCompany = DbUtil.executeQuery(conn, "SELECT * FROM company");
        while (rsCompany.next()) {

            String companyid = rsCompany.getString("companyid");
            if (projdb.isEmailSubscribe(conn, companyid)) {
                int projCount = 0, todoCount = 0, calCount = 0;
                boolean subToPlan = DashboardHandler.isSubscribed(conn, companyid, "proj");
                boolean subToCal = DashboardHandler.isSubscribed(conn, companyid, "cal");
                boolean featureViewProj = DashboardHandler.isFeatureView(conn, companyid, "proj");
                boolean featureViewToDo = DashboardHandler.isFeatureView(conn, companyid, "todo");
                boolean featureViewCal = DashboardHandler.isFeatureView(conn, companyid, "cal");
                boolean localCheck = ConfigReader.getinstance().getBoolean("PMStandAlone", false);
                String uri = "";
                if(localCheck){
                    JSONObject jo = new JSONObject();
                    jo.put("companyid", companyid);
                    jo = APICallHandler.callApp(conn, appURL, jo, companyid, "13");
                    if(!jo.isNull("success") && jo.getBoolean("success")){
                        uri = jo.getString("url");
                    } else {
                        uri = URLUtil.getPageURL(request, Links.loginpageFull, rsCompany.getString("subdomain"));
                    }
                }
                if(!StringUtil.isNullOrEmpty(uri)){
                    bottomTextHTML = String.format("<p>You can log in at:</p><p>%s</p><br/><p> - %s </p></div></body></html>", uri, "");
                    bottomTextPlain = String.format("\n\nYou can log in at:\n%s\n\n - %s ", uri, "");


                    DbResults rsUsers = DbUtil.executeQuery(conn, "SELECT * FROM users INNER JOIN userlogin "
                            + "ON users.userid = userlogin.userid WHERE userlogin.isactive = true AND users.companyid = ?", companyid);
                    while (rsUsers.next()) {
                        String userid = rsUsers.getString("userid");
                        String userName = rsUsers.getString("username");
                        String userFullName = rsUsers.getString("fname").concat(" ").concat(rsUsers.getString("lname"));
                        boolean isPlanSub = false, isToDoSub = false, isCalSub = false;

                        if (ProfileHandler.isUserSubscribedToNotification(conn, userid)) {
                            DbResults rsProjects = DbUtil.executeQuery(conn, "SELECT * FROM project "
                                    + "INNER JOIN projectmembers ON project.projectid = projectmembers.projectid WHERE "
                                    + "projectmembers.inuseflag = true AND projectmembers.status in (3, 4, 5) "
                                    + "AND project.archived = 0 AND projectmembers.userid = ?", userid);

                            String projMailPlain = "", projMailHTML = "";
                            String todoMailPlain = "", todoMailHTML = "";
                            String eventMailPlain = "", eventMailHTML = "";
                            while (rsProjects.next()) {
                                projectid = rsProjects.getString("projectid");
                                String projectName = rsProjects.getString("projectname");
                                int notifVal = rsProjects.getInt("notification_subscription");
                                isPlanSub = false;
                                isToDoSub = false;
                                isCalSub = false;
                                int tempVal = (int) Math.pow(2, projectPlan);
                                if (subToPlan && featureViewProj) {
                                    if ((notifVal & tempVal) == tempVal) {
                                        isPlanSub = true;
                                    }
                                }

                                if (featureViewToDo) {
                                    tempVal = (int) Math.pow(2, toDo);
                                    if ((notifVal & tempVal) == tempVal) {
                                        isToDoSub = true;
                                    }
                                }

                                if (subToCal && featureViewCal) {
                                    tempVal = (int) Math.pow(2, calendar);
                                    if ((notifVal & tempVal) == tempVal) {
                                        isCalSub = true;
                                    }
                                }
                                if (isPlanSub) {
                                    DbResults dueTodayTasks = DbUtil.executeQuery(conn, "SELECT taskid, taskname, duration, startdate, enddate, "
                                            + "percentcomplete, priority FROM proj_task WHERE projectid = ? AND taskid IN "
                                            + "(SELECT taskid FROM proj_taskresourcemapping WHERE resourceid = ?)"
                                            + " AND datediff(date(enddate),date('" + javaCurrentTime + "')) IN (0,1,2) ORDER BY taskindex",
                                            new Object[]{projectid, userid});
                                    projCount++;
                                    int countProj = 1;
                                    String projPlainMsg = "";
                                    String projHTMLMsg = "";
                                    projPlainMsg += String.format("\n\nYou have following Tasks in project: %s\n", projectName);
                                    projHTMLMsg += String.format("<br><p>You have following Tasks in project: <b>%s</b></p>", projectName);
                                    projPlainMsg = projPlainMsg.concat("\nTasks due in next 2 days - \n");
                                    projHTMLMsg = projHTMLMsg.concat("<br><b>Tasks due in next 2 days - </b>");
                                    while (dueTodayTasks.next()) {
                                        String priority = "Low";
                                        int p = dueTodayTasks.getInt("priority");
                                        if (p == 2) {
                                            priority = "High";
                                        } else if (p == 1) {
                                            priority = "Moderate";
                                        }
                                        String pecentComplete = "" + dueTodayTasks.getInt("percentcomplete") + "%";
                                        Date edate = df1.parse(dueTodayTasks.getObject("enddate").toString());
                                        String enddate = df1.format(edate);
                                        projPlainMsg += String.format(taskNotificationTaskPlainText, countProj, dueTodayTasks.getString("taskname"), enddate, pecentComplete, priority);
                                        projHTMLMsg += String.format(taskNotificationTaskHTMLText, countProj++, dueTodayTasks.getString("taskname"), enddate, pecentComplete, priority);
                                    }
                                    if (countProj > 1) {
                                    } else {
                                        projPlainMsg = projPlainMsg.concat("\nThere are no tasks due in next 2 days.\n");
                                        projHTMLMsg = projHTMLMsg.concat("<p>There are <b>no tasks</b> due in next 2 days.</p>");
                                    }

                                    DbResults otherTasks = DbUtil.executeQuery(conn, "SELECT taskid, taskname, duration, startdate, enddate, "
                                            + "percentcomplete, priority FROM proj_task WHERE projectid = ? AND taskid IN "
                                            + "(SELECT taskid FROM proj_taskresourcemapping WHERE resourceid = ?)"
                                            + " AND date(startdate) <= date(adddate('" + javaCurrentTime + "', 7)) AND date(startdate) > date('" + javaCurrentTime + "') ORDER BY taskindex",
                                            new Object[]{projectid, userid});

                                    int otherCountProj = 1;
                                    projHTMLMsg = projHTMLMsg.concat("<br><b>Tasks to follow in this week - </b>");
                                    projPlainMsg = projPlainMsg.concat("\nTasks to follow in this week - \n");
                                    while (otherTasks.next()) {
                                        if (otherCountProj == 1) {
                                        }
                                        String priority = "Low";
                                        int p = otherTasks.getInt("priority");
                                        if (p == 2) {
                                            priority = "High";
                                        } else if (p == 1) {
                                            priority = "Moderate";
                                        }
                                        String dateString = Timezone.toUserTimezone(conn, otherTasks.getObject("startdate").toString(), userid);
                                        dateString = dateFormatHandlers.getUserPrefreanceDate(df, sdfDateTime.parse(dateString));
                                        String duration = otherTasks.getString("duration");
                                        projPlainMsg += String.format(taskNotificationOtherTaskPlainText, otherCountProj, otherTasks.getString("taskname"), dateString, duration, priority);
                                        projHTMLMsg += String.format(taskNotificationOtherTaskHTMLText, otherCountProj++, otherTasks.getString("taskname"), dateString, duration, priority);
                                    }
                                    if (otherCountProj > 1) {
                                    } else {
                                        projPlainMsg = projPlainMsg.concat("\nThere are no tasks starting in this week\n");
                                        projHTMLMsg = projHTMLMsg.concat("<p>There are <b>no tasks</b> starting in this week</p>");
                                    }
                                    if (countProj > 1 || otherCountProj > 1) {
                                        projMailPlain += projPlainMsg;
                                        projMailHTML += projHTMLMsg;
                                    }
                                }
                                if (isToDoSub) {
                                    DbResults dueTodayToDos = DbUtil.executeQuery(conn, "SELECT * FROM todotask WHERE userid = ? AND assignedto = ? "
                                            + " AND datediff(date(duedate),date('" + javaCurrentTime + "')) = 0 AND status = 0",
                                            new Object[]{projectid, userid});
                                    todoCount++;
                                    int countTodo = 1;
                                    String todoPlainMsg = "";
                                    String todoHTMLMsg = "";
                                    todoPlainMsg += String.format("\n\nYou have following To-Dos in the project: %s\n", projectName);
                                    todoHTMLMsg += String.format("<br><p>You have following To-Dos in the project: <b>%s</b></p>", projectName);
                                    todoPlainMsg = todoPlainMsg.concat("\nTo-Dos Due Today - \n");
                                    todoHTMLMsg = todoHTMLMsg.concat("<br><b>To-Dos Due Today - </b>");
                                    while (dueTodayToDos.next()) {
                                        String priority = dueTodayToDos.getString("priority");
                                        if (StringUtil.isNullOrEmpty(priority)) {
                                            priority = "Not Specified";
                                        }
                                        String desc = dueTodayToDos.getString("description");
                                        todoPlainMsg += String.format(toDoNotificationTaskPlainText, countTodo, dueTodayToDos.getString("taskname"));
                                        todoHTMLMsg += String.format(toDoNotificationTaskHTMLText, countTodo++, dueTodayToDos.getString("taskname"), priority, desc);
                                    }
                                    if (countTodo > 1) {
                                    } else {
                                        todoPlainMsg = todoPlainMsg.concat("\nThere are no To-Dos due today\n");
                                        todoHTMLMsg = todoHTMLMsg.concat("<p>There are <b>no To-Dos</b> due today</p>");
                                    }

                                    DbResults otherToDos = DbUtil.executeQuery(conn, "SELECT * FROM todotask WHERE userid = ? AND assignedto = ? "
                                            + " AND date(duedate) <= date(adddate('" + javaCurrentTime + "', 7)) AND date(duedate) > date('" + javaCurrentTime + "') AND status = 0",
                                            new Object[]{projectid, userid});

                                    int otherCountTodo = 1;
                                    todoPlainMsg = todoPlainMsg.concat("\nTo-Dos due in this week - \n");
                                    todoHTMLMsg = todoHTMLMsg.concat("<br><b>To-Dos due in this week - </b>");
                                    while (otherToDos.next()) {
                                        String priority = otherToDos.getString("priority");
                                        if (StringUtil.isNullOrEmpty(priority)) {
                                            priority = "Not Specified";
                                        }
                                        String due = "";
                                        if (!otherToDos.getObject("duedate").toString().contains("1970-01-01")) {
                                            due = otherToDos.getObject("duedate").toString();
                                            due = Timezone.toUserTimezone(conn, due, userid);
                                            due = due.split(" ")[0];
                                        } else {
                                            due = "Not Specified";
                                        }
                                        String desc = otherToDos.getString("description");
                                        todoPlainMsg += String.format(toDoNotificationOtherTaskPlainText, otherCountTodo, otherToDos.getString("taskname"), priority, due, desc);
                                        todoHTMLMsg += String.format(toDoNotificationOtherTaskHTMLText, otherCountTodo++, otherToDos.getString("taskname"), priority, due, desc);
                                    }
                                    if (otherCountTodo > 1) {
                                    } else {
                                        todoPlainMsg = todoPlainMsg.concat("\nThere are no To-Dos due in this week\n");
                                        todoHTMLMsg = todoHTMLMsg.concat("<p>There are <b>no To-Dos</b> due in this week</p>");
                                    }
                                    if (countTodo > 1 || otherCountTodo > 1) {
                                        todoMailPlain += todoPlainMsg;
                                        todoMailHTML += todoHTMLMsg;
                                    }
                                }
                                if (isCalSub) {
                                    DbResults dueTodayEvents = DbUtil.executeQuery(conn, "SELECT calendarevents.subject, calendarevents.cid, cname, descr ,startts,"
                                            + " endts, calendarevents.location, priority, allDay FROM calendarevents"
                                            + " INNER JOIN calendars ON calendars.cid =  calendarevents.cid"
                                            + " WHERE calendars.userid = ? AND calendars.cid NOT IN (SELECT cid FROM networkcalendars WHERE companyid = ?) AND date(endts) = date('" + javaCurrentTime + "')",
                                            new Object[]{projectid, companyid});

                                    int countEvent = 1;
                                    calCount++;
                                    String eventPlainMsg = "";
                                    String eventHTMLMsg = "";
                                    eventPlainMsg += String.format("\n\nYou have following events in Team Calendar of project: %s\n", projectName);
                                    eventHTMLMsg += String.format("<br><p>You have following events in Team Calendar of project: <b>%s</b></p>", projectName);
                                    eventPlainMsg = eventPlainMsg.concat("\nToday's Agenda - \n");
                                    eventHTMLMsg = eventHTMLMsg.concat("<br><b>Today's Agenda - </b>");
                                    SimpleDateFormat sdfTime = new SimpleDateFormat(dfTime);
                                    while (dueTodayEvents.next()) {
                                        String priority = "Low";
                                        String p = dueTodayEvents.getString("priority");
                                        if (p.equals("h")) {
                                            priority = "High";
                                        } else if (p.equals("m")) {
                                            priority = "Moderate";
                                        }
                                        String desc = dueTodayEvents.getString("descr");
                                        boolean allday = dueTodayEvents.getBoolean("allDay");
                                        String time = "All Day";
                                        if (!allday) {
                                            String dateString = Timezone.toUserTimezone(conn, dueTodayEvents.getObject("startts").toString(), userid);
                                            String st = sdfTime.format(sdfDateTime.parse(dateString));
                                            dateString = Timezone.toUserTimezone(conn, dueTodayEvents.getObject("endts").toString(), userid);
                                            String et = sdfTime.format(sdfDateTime.parse(dateString));
                                            time = st.concat(" to ").concat(et);
                                        }
                                        eventPlainMsg += String.format(eventNotificationPlainText, countEvent, dueTodayEvents.getString("subject"), desc,
                                                dueTodayEvents.getString("location"), priority, time);
                                        eventHTMLMsg += String.format(eventNotificationHTMLText, countEvent++, dueTodayEvents.getString("subject"), desc,
                                                dueTodayEvents.getString("location"), priority, time);
                                    }
                                    if (countEvent > 1) {
                                    } else {
                                        eventPlainMsg = eventPlainMsg.concat("\nThere are no events in your agenda today.\n");
                                        eventHTMLMsg = eventHTMLMsg.concat("<p>There are <b>no events</b> in your agenda today.</p>");
                                    }

                                    DbResults otherEvents = DbUtil.executeQuery(conn, "SELECT calendarevents.subject, calendarevents.cid, cname, descr ,startts,"
                                            + " endts, calendarevents.location, priority, allDay FROM calendarevents"
                                            + " INNER JOIN calendars ON calendars.cid =  calendarevents.cid"
                                            + " WHERE calendars.userid = ? AND calendars.cid NOT IN (SELECT cid FROM networkcalendars WHERE companyid = ?)"
                                            + " AND date(startts) <= date(adddate('" + javaCurrentTime + "', 7)) AND date(startts) > date(adddate('" + javaCurrentTime + "', 7))",
                                            new Object[]{projectid, companyid});

                                    int otherCountEvent = 1;
                                    eventPlainMsg = eventPlainMsg.concat("\nEvents in this week - \n");
                                    eventHTMLMsg = eventHTMLMsg.concat("<br><b>Events in this week - </b>");
                                    while (otherEvents.next()) {
                                        String priority = "Low";
                                        String p = otherEvents.getString("priority");
                                        if (p.equals("h")) {
                                            priority = "High";
                                        } else if (p.equals("m")) {
                                            priority = "Moderate";
                                        }
                                        String desc = otherEvents.getString("descr");
                                        boolean allday = otherEvents.getBoolean("allDay");
                                        String st = "", et = "";
                                        if (!allday) {
                                            st = Timezone.toUserTimezone(conn, otherEvents.getObject("startts").toString(), userid);
                                            st = dateFormatHandlers.getUserPrefreanceDate(df, sdfDateTime.parse(st));
                                            et = Timezone.toUserTimezone(conn, otherEvents.getObject("endts").toString(), userid);
                                            et = dateFormatHandlers.getUserPrefreanceDate(df, sdfDateTime.parse(et));
                                        } else {
                                            st = dateFormatHandlers.getUserPrefreanceDate(df, sdfDateTime.parse(otherEvents.getObject("startts").toString()));
                                            et = dateFormatHandlers.getUserPrefreanceDate(df, sdfDateTime.parse(otherEvents.getObject("endts").toString()));
                                        }

                                        eventPlainMsg += String.format(eventNotificationOtherPlainText, otherCountEvent, otherEvents.getString("subject"), desc,
                                                st, et, priority, otherEvents.getString("location"));
                                        eventHTMLMsg += String.format(eventNotificationOtherHTMLText, otherCountEvent++, otherEvents.getString("subject"), desc,
                                                st, et, priority, otherEvents.getString("location"));
                                    }
                                    if (otherCountEvent > 1) {
                                    } else {
                                        eventPlainMsg = eventPlainMsg.concat("\nThere are no events in your agenda this week.\n");
                                        eventHTMLMsg = eventHTMLMsg.concat("<p>There are <b>no events</b> in your agenda this week.</p>");
                                    }
                                    if (countEvent > 1 || otherCountEvent > 1) {
                                        eventMailPlain += eventPlainMsg;
                                        eventMailHTML += eventHTMLMsg;
                                    }
                                }
                            } // projects while end
                            String finalMsgPlain = String.format("\nHi %s,\n", userFullName);
                            String finalMsgHTML = String.format(taskNotificationMainText, userFullName);
                            if (projMailPlain.compareTo("") != 0 && projMailHTML.compareTo("") != 0) {
                                try {
                                    finalMsgPlain += projMailPlain.concat(bottomTextPlain);
                                    finalMsgHTML += projMailHTML.concat(bottomTextHTML);
                                    SendMailHandler.postMail(new String[]{rsUsers.getString("emailid")}, "Task Alerts", finalMsgHTML, finalMsgPlain, CompanyHandler.getSysEmailIdByCompanyID(conn, companyid));
                                } catch (ConfigurationException e) {
                                    e.printStackTrace();
                                } catch (MessagingException e) {
                                    e.printStackTrace();
                                }
                                String params = projCount + ", project plan";
                                String ipAddress = AuthHandler.getIPAddress(request);
                                AuditTrail.insertLog(conn, "411", userid, tid, "", companyid, params, ipAddress, auditMode);
                            }
                            finalMsgPlain = String.format("\nHi %s,\n", userFullName);
                            finalMsgHTML = String.format(toDoNotificationMainText, userFullName);
                            if (todoMailPlain.compareTo("") != 0 && todoMailHTML.compareTo("") != 0) {
                                try {
                                    finalMsgPlain += todoMailPlain.concat(bottomTextPlain);
                                    finalMsgHTML += todoMailHTML.concat(bottomTextHTML);
                                    SendMailHandler.postMail(new String[]{rsUsers.getString("emailid")}, "To-Do Alerts", finalMsgHTML, finalMsgPlain, CompanyHandler.getSysEmailIdByCompanyID(conn, companyid));
                                } catch (ConfigurationException e) {
                                    e.printStackTrace();
                                } catch (MessagingException e) {
                                    e.printStackTrace();
                                }
                                String params = todoCount + ", TODO ";
                                String ipAddress = AuthHandler.getIPAddress(request);
                                AuditTrail.insertLog(conn, "411", userid, tid, "", companyid, params, ipAddress, auditMode);
                            }
                            finalMsgPlain = String.format("\nHi %s,\n", userFullName);
                            finalMsgHTML = String.format(eventNotificationMainText, userFullName);
                            if (eventMailPlain.compareTo("") != 0 && eventMailHTML.compareTo("") != 0) {
                                try {
                                    finalMsgPlain += eventMailPlain.concat(bottomTextPlain);
                                    finalMsgHTML += eventMailHTML.concat(bottomTextHTML);
                                    SendMailHandler.postMail(new String[]{rsUsers.getString("emailid")}, "Calendar Event Alerts", finalMsgHTML, finalMsgPlain, CompanyHandler.getSysEmailIdByCompanyID(conn, companyid));
                                } catch (ConfigurationException e) {
                                    e.printStackTrace();
                                } catch (MessagingException e) {
                                    e.printStackTrace();
                                }
                                String params = calCount + ", Calender";
                                String ipAddress = AuthHandler.getIPAddress(request);
                                AuditTrail.insertLog(conn, "411", userid, tid, "", companyid, params, ipAddress, auditMode);
                            }
                        } // if user has sub end
                    } // users end
                }// check for uri ends
            } // if comp has sub end
        } // comp end
        out.print("success");
        conn.commit();
    } catch (Exception ex) {
        DbPool.quietRollback(conn);
        jtemp.put("success", "false");
        jtemp.put("data", ex.getMessage());
        out.print(jtemp.toString());
    } finally {
        DbPool.quietClose(conn);
    }
%>
