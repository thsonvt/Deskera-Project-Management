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
<%@page import="com.krawler.common.locale.MessageSourceProxy"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="com.krawler.common.util.Constants"%>
<%@page import="com.krawler.utils.json.base.JSONObject"%>
<%@page import="com.krawler.common.util.KWLErrorMsgs"%>
<%@page import="com.krawler.esp.company.Company"%>
<%@page import="com.krawler.esp.company.CompanyDAO"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="com.krawler.esp.web.resource.Links"%>
<%@page import="com.krawler.esp.company.CompanyDAOImpl"%>
<%@page import ="com.krawler.esp.notification.*"%>
<%@page import ="com.krawler.database.DbPool.Connection"%>
<%@page import ="com.krawler.database.DbPool"%>
<%@page import ="com.krawler.esp.handlers.*"%>
<%@page import="com.krawler.esp.handlers.AuthHandler"%>
<%@page import="com.krawler.esp.user.*"%>
<%@page import="com.krawler.esp.handlers.CompanyHandler"%>
<%@page import="com.krawler.common.util.URLUtil"%>
<%@page import="java.util.List"%>
<%@page import="com.krawler.esp.project.project.*"%>
<%@page import="com.krawler.esp.project.task.*"%>
<%@page import="com.krawler.esp.portalmsg.Mail"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%

    if (sessionbean.validateSession(request, response)) {
        Connection conn = null;
        JSONObject jbj = new JSONObject();
        jbj.put("valid", true);
        String result = "";
        
        try {
            conn = DbPool.getConnection();
            String loginid = AuthHandler.getUserid(request);
            String userFullName = AuthHandler.getAuthor(conn, loginid);
            String companyID = AuthHandler.getCompanyid(request);
            DateFormat sdf = new SimpleDateFormat("E, MMM dd, yyyy");
            CompanyDAO cd = new CompanyDAOImpl();
            Company c = cd.getCompany(conn, companyID);
            boolean type = cd.isSubscribedToEmailNotifications(conn, companyID);

            if (type) {

                String subdomain = c.getSubDomain();
                String uri = URLUtil.getPageURL(request, Links.loginpageFull, subdomain);
                String pid = request.getParameter("pid");
                MailNotification mail = new MailNotification();
                mail.setSubject(KWLErrorMsgs.taskNotificationSubject);
                ProjectDAO pd = new ProjectDAOImpl();
                Project project = pd.getProjectById(conn, pid);
                String sender = cd.getSysEmailIdByCompanyID(conn, companyID);
                mail.setSender(sender);
                List<ProjectMember> members = (new ProjectDAOImpl()).getProjectMembers(conn, pid);
                int resCount = members.size();
                int mailCount = 0;
                int overdueCount = new TaskDAOImpl().getOverdueTasks(conn, pid).size();
                for (int i = 0; i < resCount; i++) {
                    User u = members.get(i).getUser();
                    mail.addReciever(u);
                    String m = NotificationTemplate.PlainText.Greet_OverdueTask(u.getFullName(), project.getProjectName());
                    mail.setMessage(m);
                    m = NotificationTemplate.HTMLText.HeaderAndGreet_OverdueTask(u.getFullName(), project.getProjectName());
                    mail.setHtmlMsg(m);
                    List<Task> overdueTask = (new TaskDAOImpl()).getOverdueTasksOfMember(conn, pid, u.getUserID());
                    int taskCount = overdueTask.size();
                    for (int j = 0; j < taskCount; j++) {
                        Task task = overdueTask.get(j);
                        m = NotificationTemplate.HTMLText.TaskDetails_OverdueTask(j + 1, task.getTaskName(), task.getPercentComplete() + "%", sdf.format(task.getEndDate()));
                        mail.appendMessage(NotificationTemplate.PlainText.TaskDetails_OverdueTask(j + 1, task.getTaskName(), task.getPercentComplete() + "%", sdf.format(task.getEndDate())), m);
                    }

                    if (taskCount > 0) {
                        mailCount++;
                        m = NotificationTemplate.HTMLText.Footer(uri, userFullName);
                        mail.appendMessage(NotificationTemplate.PlainText.Footer(uri, userFullName), m);
                        mail.notifyUser();
                        Mail.insertMailMsg(conn, u.getUserName(), loginid, mail.getSubject(), mail.getHtmlMsg(), "1", false, "1", "", "newmsg", "", 3, "", companyID);
                        if (mailCount == 1) {
                            result = u.getFullName();
                        } else if (mailCount == (resCount - 1) && mailCount != 1) {
                            result = result + " and " + u.getFullName();
                        } else {
                            result = result + ", " + u.getFullName();
                        }
                    }
                    mail.removeReciever(u);
                }

                if(overdueCount == 0){
                    result = "overdueError";
                } else if(StringUtil.isNullOrEmpty(result)) {
                    result = "resourceError";
                }
                
                jbj.put("data", result);

                String userName = AuthHandler.getUserName(request);
                String params = userFullName + " (" + userName + "), " + mailCount + ", " + MessageSourceProxy.getMessage("pm.notification.overdue.log.param", null, request);
                String ipAddress = AuthHandler.getIPAddress(request);
                AuditTrail.insertLog(conn, "410", loginid, "", "", companyID, params, ipAddress, 0);

                conn.commit();

            } else {
                result = "typeError";
                jbj.put("data", result);
            }
        } catch (Exception ex) {
            DbPool.quietRollback(conn);
            result = "error";
            jbj.put("data", result);
        } finally {
            DbPool.quietClose(conn);
        }
        out.print(jbj.toString());
    } else {
        out.println("{\"valid\": false}");
    }
%>
