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
    Document   : generateManagerReportFiles
    Created on : Feb 2, 2012, 4:44:19 PM
    Author     : Abhay Kulkarni
    Purpose    : Cron task for creating Report File for each user, which contains 
                progress update and brief details of tasks, that belong to the members in each of his/her projects.
--%>

<%@page import="java.io.FileOutputStream"%>
<%@page import="java.io.File"%>
<%@page import="java.io.FileInputStream"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="com.krawler.esp.project.project.ProjectMember"%>
<%@page import="com.krawler.esp.project.project.ProjectDAOImpl"%>
<%@page import="com.krawler.esp.project.project.ProjectDAO"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.text.DateFormat"%>
<%@page import="com.krawler.esp.company.CompanyDAO"%>
<%@page import="javax.mail.MessagingException"%>
<%@page import="com.krawler.svnwebclient.configuration.ConfigurationException"%>
<%@page import="com.krawler.esp.notification.NotificationTemplate"%>
<%@page import="com.krawler.esp.company.CompanyDAOImpl"%>
<%@page import="com.krawler.esp.web.resource.Links"%>
<%@page import="com.krawler.common.util.*"%>
<%@page import="com.krawler.esp.utils.ConfigReader"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.List"%>
<%@page import="com.krawler.utils.json.base.JSONObject"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.krawler.database.*"%>
<%@page import="com.krawler.esp.handlers.*"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%!
    private String getCSVString(String projName, String res, String task, String dd, String pc, String prio) {
        return "\n\"" + projName + "\",\"" + res + "\",\"" + task + "\",\"" + dd + "\",\"" + pc + "\",\"" + prio + "\"";
    }
%>
<%
    String[] subdomainList = new String[]{"\"secutech\""};
    DbPool.Connection conn = null;
    DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
    String header = "\"Project Name\",\"Resource\",\"Tasks Assigned for this week\",\"Due Date\",\"Current Progress\",\"Priority\"";

    try {
        Date weekStart = null, weekEnd = null, d = new Date();
        Calendar currCal = Calendar.getInstance();
        currCal.setTime(d);
        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.SUNDAY);
        c.setTime(d);

        currCal.add(Calendar.DATE, -(currCal.get(Calendar.DAY_OF_WEEK) - 1));
        weekStart = currCal.getTime();

        currCal.add(Calendar.DATE, 6);
        weekEnd = currCal.getTime();

        conn = DbPool.getConnection();
        DbResults rsCompany = DbUtil.executeQuery(conn, "SELECT companyid, subdomain FROM company WHERE subdomain in (" + StringUtil.join(",", subdomainList) + ")");
        while (rsCompany.next()) {
            String companyid = rsCompany.getString("companyid");
            String subdomain = rsCompany.getString("subdomain");
            
            DbResults rsUsers = DbUtil.executeQuery(conn, "SELECT users.userid, userlogin.username, fname, lname, emailid FROM users INNER JOIN userlogin "
                    + "ON users.userid = userlogin.userid WHERE userlogin.isactive = true AND users.companyid = ?", companyid);
            while (rsUsers.next()) {
                String userid = rsUsers.getString("userid");
                String username = rsUsers.getString("username");

                StringBuilder dataLine = new StringBuilder();
                dataLine.append(header);

                DbResults rsProjects = DbUtil.executeQuery(conn, "SELECT project.projectid, projectname FROM project "
                        + "INNER JOIN projectmembers ON project.projectid = projectmembers.projectid WHERE "
                        + "projectmembers.inuseflag = true AND projectmembers.status = 4 "
                        + "AND project.archived = 0 AND projectmembers.userid = ? ORDER BY projectname", userid);
                while (rsProjects.next()) {
                    String projectid = rsProjects.getString("projectid");
                    String projectName = rsProjects.getString("projectname");

                    ProjectDAO pd = new ProjectDAOImpl();
                    List<ProjectMember> pm = pd.getProjectMembers(conn, projectid);

                    for (ProjectMember member : pm) {
                        String resID = member.getUser().getUserID();
                        String resName = member.getUser().getFirstName().concat(" ").concat(member.getUser().getLastName());
                        if (!resID.equals(userid)) {

                            String taskName = "-", priority = "-", pc = "-", endDate = "-";
                            boolean thereIsATask = false;

                            DbResults todayTasks = DbUtil.executeQuery(conn, "SELECT DISTINCT pt.taskid, taskname, duration, startdate, enddate, "
                                    + "percentcomplete, priority FROM proj_task pt "
                                    + "INNER JOIN proj_taskresourcemapping ptm ON pt.taskid=ptm.taskid "
                                    + "WHERE projectid = ? AND ((startdate <= date(?) AND enddate >= date(?)) "
                                    + "OR (startdate BETWEEN date(?) AND date(?) "
                                    + "OR enddate BETWEEN date(?) AND date(?))) AND ptm.resourceid = ? ORDER BY taskindex", new Object[]{projectid, weekStart, weekEnd, weekStart, weekEnd, weekStart, weekEnd, resID});
                            while (todayTasks.next()) {

                                thereIsATask = true;
                                taskName = todayTasks.getString("taskname");
                                priority = "Low";
                                int p = todayTasks.getInt("priority");
                                if (p == 2) {
                                    priority = "High";
                                } else if (p == 1) {
                                    priority = "Moderate";
                                }
                                pc = todayTasks.getInt("percentcomplete") + "%";
                                Date edate = df1.parse(todayTasks.getObject("enddate").toString());
                                endDate = df1.format(edate);
                                dataLine.append(getCSVString(projectName, resName, taskName, endDate, pc, priority));
                            }

                            if (!thereIsATask) {
                                dataLine.append(getCSVString(projectName, resName, taskName, endDate, pc, priority));
                            }
                        }
                    }
                }

                String destinationDirectory = StorageHandler.GetDocStorePath() + StorageHandler.GetFileSeparator() + "dailyManagerReports";
                File f = new File(destinationDirectory);
                if(!f.exists())
                    f.mkdir();
                f = new File(destinationDirectory 
                        + StorageHandler.GetFileSeparator() 
                        + subdomain.toLowerCase() 
                        + "_" 
                        + username.toLowerCase() 
                        + ".csv");
                if(f.exists()){
                    f.delete();
                }
                f.createNewFile();
                FileOutputStream fop = new FileOutputStream(f);
                byte[] b = dataLine.toString().getBytes();
                fop.write(b);
                fop.close();
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
