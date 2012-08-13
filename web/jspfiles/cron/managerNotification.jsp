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
    Document   : managerNotification
    Created on : Jan 31, 2012, 11:38:46 AM
    Author     : Abhay Kulkarni
    Purpose    : Cron task that sends out mails to Project Managers for daily tasks. 
                    Managers must subscribe to the project plan notifications.
--%>

<%@page import="com.krawler.esp.company.CompanyDAO"%>
<%@page import="javax.mail.MessagingException"%>
<%@page import="com.krawler.svnwebclient.configuration.ConfigurationException"%>
<%@page import="com.krawler.esp.notification.NotificationTemplate"%>
<%@page import="com.krawler.esp.company.CompanyDAOImpl"%>
<%@page import="com.krawler.esp.web.resource.Links"%>
<%@page import="com.krawler.common.util.*"%>
<%@page import="com.krawler.esp.utils.ConfigReader"%>
<%@page import="java.util.Date"%>
<%@page import="com.krawler.utils.json.base.JSONObject"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.krawler.database.*"%>
<%@page import="com.krawler.esp.handlers.*"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%
    DbPool.Connection conn = null;
    String df = "E, MMM dd, yyyy";
    SimpleDateFormat formatDate = new SimpleDateFormat(df);
    SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
    Date d = new Date();
    String javaCurrentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(d);
    boolean localCheck = ConfigReader.getinstance().getBoolean("PMStandAlone", false);
    String appURL = ConfigReader.getinstance().get("platformURL");

    try {
        conn = DbPool.getConnection();
        DbResults rsCompany = DbUtil.executeQuery(conn, "SELECT companyid, subdomain FROM company");
        while (rsCompany.next()) {
            String companyid = rsCompany.getString("companyid");
            CompanyDAO cd = new CompanyDAOImpl();
            if (cd.isSubscribedToEmailNotifications(conn, companyid)) {

                int projCount = 0;
                boolean subToPlan = DashboardHandler.isSubscribed(conn, companyid, "proj");
                boolean featureViewProj = DashboardHandler.isFeatureView(conn, companyid, "proj");
                if (subToPlan && featureViewProj) {
                    
                    String uri = URLUtil.getPageURL(request, Links.loginpageFull, rsCompany.getString("subdomain"));
                    if(!localCheck){
                        JSONObject jo = new JSONObject();
                        jo.put("companyid", companyid);
                        jo = APICallHandler.callApp(conn, appURL, jo, companyid, "13");
                        if(!jo.isNull("success") && jo.getBoolean("success") && !StringUtil.isNullOrEmpty(jo.getString("pmurl"))){
                            uri = jo.getString("pmurl");
                        }
                    }
                    
                    DbResults rsUsers = DbUtil.executeQuery(conn, "SELECT users.userid, fname, lname, emailid FROM users INNER JOIN userlogin "
                            + "ON users.userid = userlogin.userid WHERE userlogin.isactive = true AND users.companyid = ?", companyid);
                    while (rsUsers.next()) {
                        String projPlainMsg = "";
                        String projHTMLMsg = "";
                        String userid = rsUsers.getString("userid");
                        if (ProfileHandler.isUserSubscribedToNotification(conn, userid)) {
                            
                            String userFullName = rsUsers.getString("fname").concat(" ").concat(rsUsers.getString("lname"));

                            projPlainMsg = NotificationTemplate.PlainText.Greet_Simple(userFullName);
                            projHTMLMsg = NotificationTemplate.HTMLText.HeaderAndGreet_Simple(userFullName);
                            
                            projPlainMsg += NotificationTemplate.PlainText.FirstLine_ForManagers();
                            projHTMLMsg += NotificationTemplate.HTMLText.FirstLine_ForManagers();
                            
                            DbResults rsProjects = DbUtil.executeQuery(conn, "SELECT project.projectid, projectname FROM project "
                                    + "INNER JOIN projectmembers ON project.projectid = projectmembers.projectid WHERE "
                                    + "projectmembers.inuseflag = true AND projectmembers.status = 4 "
                                    + "AND project.archived = 0 AND projectmembers.userid = ? ORDER BY projectname", userid);
                            while (rsProjects.next()) {
                                String projectid = rsProjects.getString("projectid");
                                String projectName = rsProjects.getString("projectname");

                                int countProj = 1;
                                projCount++;
                                projPlainMsg += NotificationTemplate.PlainText.TaskDetails_ProjectName(projectName);
                                projHTMLMsg += NotificationTemplate.HTMLText.TaskDetails_ProjectName(projectName);

                                DbResults todayTasks = DbUtil.executeQuery(conn, "SELECT DISTINCT pt.taskid, taskname, duration, startdate, enddate, "
                                        + "percentcomplete, priority FROM proj_task pt "
                                        + "INNER JOIN proj_taskresourcemapping ptm ON pt.taskid=ptm.taskid "
                                        + "WHERE projectid = ? AND startdate <= date('" + javaCurrentTime + "') "
                                        + "AND enddate >= date('" + javaCurrentTime + "') ORDER BY taskindex", projectid);
                                while (todayTasks.next()) {

                                    String taskid = todayTasks.getString("taskid");

                                    DbResults res = DbUtil.executeQuery(conn, "SELECT u.userid, CONCAT(u.fname, ' ', u.lname) as fullname "
                                            + "FROM proj_taskresourcemapping ptm "
                                            + "INNER JOIN proj_resources pr ON pr.resourceid=ptm.resourceid "
                                            + "INNER JOIN users u ON pr.resourceid=u.userid "
                                            + "INNER JOIN userlogin ul ON ul.userid=u.userid "
                                            + "INNER JOIN project p ON p.projectid = pr.projid "
                                            + "WHERE ptm.taskid = ? AND p.projectid = ? AND pr.categoryid = 1 AND pr.typeid = 1 AND ul.isactive = 1 "
                                            , new Object[]{taskid, projectid});
                                    String resources = "";
                                    while (res.next()) {
                                        resources = resources.concat(res.getString("userid").equals(userid) ? "me" : res.getString("fullname")).concat(", ");
                                    }
                                    resources = resources.lastIndexOf(",") != -1
                                            ? resources.substring(0, resources.lastIndexOf(",")) : resources;

                                    String priority = "Low";
                                    int p = todayTasks.getInt("priority");
                                    if (p == 2) {
                                        priority = "High";
                                    } else if (p == 1) {
                                        priority = "Moderate";
                                    }
                                    String percentComplete = "" + todayTasks.getInt("percentcomplete") + "%";
                                    Date edate = df1.parse(todayTasks.getObject("enddate").toString());
                                    String enddate = formatDate.format(edate);
                                    projPlainMsg += NotificationTemplate.PlainText.TaskDetails_ForManagers(countProj, todayTasks.getString("taskname"), priority, percentComplete, enddate, resources);
                                    projHTMLMsg += NotificationTemplate.HTMLText.TaskDetails_ForManagers(countProj++, todayTasks.getString("taskname"), priority, percentComplete, enddate, resources);
                                }
                                if (countProj > 1) {
                                } else {
                                    projPlainMsg = projPlainMsg.concat("\nNo ongoing tasks.\n");
                                    projHTMLMsg = projHTMLMsg.concat("No ongoing tasks.");
                                }
                            }
                            if (projPlainMsg.compareTo("") != 0 && projHTMLMsg.compareTo("") != 0 && projCount > 0) {
                                try {
                                    projPlainMsg += NotificationTemplate.PlainText.Footer(uri, "");
                                    projHTMLMsg += NotificationTemplate.HTMLText.Footer(uri, "");
                                    SendMailHandler.postMail(new String[]{rsUsers.getString("emailid")}, KWLErrorMsgs.taskNotificationSubject, projHTMLMsg, projPlainMsg, CompanyHandler.getSysEmailIdByCompanyID(conn, companyid));
                                } catch (ConfigurationException e) {
                                    e.printStackTrace();
                                } catch (MessagingException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }
        out.println("Success");
    } catch (Exception ex) {
        DbPool.quietRollback(conn);
        out.println(ex.getMessage());
    } finally {
        DbPool.quietClose(conn);
    }

%>
