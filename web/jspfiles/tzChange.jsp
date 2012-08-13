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
    Document   : tzChange
    Created on : Jun 17, 2011, 11:33:02 AM
    Author     : Ashutosh Singh
    This jsp is to change time part of project plan.
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page language="java"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.FileOutputStream"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="com.krawler.database.DbResults"%>
<%@ page import="com.krawler.database.DbUtil"%>
<%@ page import="com.krawler.database.DbPool"%>
<%@ page import="com.krawler.common.service.ServiceException"%>
<%@ page import="java.net.ConnectException"%>
<%@ page import="java.io.FileNotFoundException"%>
<%@ page import="com.krawler.common.util.StringUtil"%>
<%@ page import="com.krawler.common.timezone.Timezone"%>

<%
    String directory = "/home/krawler/Desktop/tzChanges/"; // Make directory changes accordingly.
    int queryCount = 0;
    try {

        com.krawler.database.DbPool.Connection conn = null;
        conn = DbPool.getConnection();

        String sys_tz = Timezone.getSystemTimezone(conn);
        String frm_tz = "+00:00";
        String startDate = "";
        String endDate = "";
        String actualStartDate = "";
        String taskid = "";

        FileOutputStream f1 = new FileOutputStream(directory + "Company_Project_List.txt");
        FileOutputStream f2 = new FileOutputStream(directory + "Project_Taskdetail_List.txt");
        FileOutputStream f3 = new FileOutputStream(directory + "User_Task.txt");
        FileOutputStream f4 = new FileOutputStream(directory + "projTaskQuery.sql");

        PrintWriter pwd1 = new PrintWriter(f1);
        PrintWriter pwd2 = new PrintWriter(f2);
        PrintWriter pwd3 = new PrintWriter(f3);
        PrintWriter pwd4 = new PrintWriter(f4);

        String query1 = "select distinct p.companyid, c.companyname, p.projectname, p.projectid from project p inner join company c on p.companyid = c.companyid order by companyname";
        String query2 = "select  * from proj_task where projectid = ? order by projectid, taskindex";
        String query3 = "select actionby, userlogin.username, timezone.difference from actionlog inner join users on users.userid = actionlog.actionby inner join userlogin on users.userid= userlogin.userid inner join timezone on timezone.id = users.timezone where actionlog.actionon =? order by `timestamp` desc limit 1";

        DbResults rs1 = DbUtil.executeQuery(conn, query1);
        DbResults rs2 = null;
        DbResults rs3 = null;

        if (rs1.next()) {
            int taskCount = 1;
            int logCount = 1;
            for (int i = 0; i < rs1.size(); i++) { // loop1 : for project
                pwd1.println((i + 1) + "\t" + rs1.getString((i + 1), "companyname") + "\t" + rs1.getString((i + 1), "projectname") + "\n");
                rs2 = DbUtil.executeQuery(conn, query2, rs1.getString((i + 1), "projectid"));
                if (rs2.next()) {
                    pwd2.println((i + 1) + "\t" + rs1.getString((i + 1), "companyname") + "\t" + rs1.getString((i + 1), "projectname") + "\n");
                    for (int j = 0; j < rs2.size(); j++) { // loop2 : for task detail per project

                        taskid = rs2.getString((j + 1), "taskid");
                        startDate = ((rs2.getObject((j + 1), "startdate") != null) ? rs2.getObject((j + 1), "startdate").toString() : "NA");
                        endDate = ((rs2.getObject((j + 1), "enddate") != null) ? rs2.getObject((j + 1), "enddate").toString() : "NA");
                        actualStartDate = ((rs2.getObject((j + 1), "actualstartdate") != null) ? rs2.getObject((j + 1), "actualstartdate").toString() : "NA");

                        pwd2.println(taskCount + ".)  ID : " + taskid + "\n"
                                + "Task Name : " + ((StringUtil.isNullOrEmpty(rs2.getString((j + 1), "taskname"))) ? "NA" : rs2.getString((j + 1), "taskname")) + "\n"
                                + "Start Date : " + startDate + "\n"
                                + "End Date : " + endDate + "\n"
                                + "Actual Start Date : " + actualStartDate + "\n"
                                + "Last Change on : " + ((rs2.getObject((j + 1), "timestamp") != null) ? rs2.getObject((j + 1), "timestamp").toString() : "NA") + "\n\n");
                        taskCount++;

                        rs3 = DbUtil.executeQuery(conn, query3, rs2.getString((j + 1), "taskid"));
                        if (rs3.next()) { // loop3 : for user per task per project from actionlog
                            pwd3.println((i + 1) + "\t" + rs1.getString((i + 1), "companyname") + "\t" + rs1.getString((i + 1), "projectname") + "\n");
                            for (int k = 0; k < rs3.size(); k++) {

                                frm_tz = ((StringUtil.isNullOrEmpty(rs3.getString((k + 1), "difference"))) ? "NA" : rs3.getString((k + 1), "difference"));

                                pwd3.println(logCount + ".) "
                                        + "Task Name : " + ((StringUtil.isNullOrEmpty(rs2.getString((j + 1), "taskname"))) ? "NA" : rs2.getString((j + 1), "taskname")) + "\n"
                                        + "ID : " + taskid + "\n"
                                        + "Last Modified By : " + ((StringUtil.isNullOrEmpty(rs3.getString((k + 1), "username"))) ? "NA" : rs3.getString((k + 1), "username")) + "\n"
                                        + "Userid : " + ((StringUtil.isNullOrEmpty(rs3.getString((k + 1), "actionby"))) ? "NA" : rs3.getString((k + 1), "actionby")) + "\n"
                                        + "Users TimeZone : " + frm_tz + "\n\n");

                                if (!startDate.equalsIgnoreCase("NA") && !endDate.equalsIgnoreCase("NA") && !actualStartDate.equalsIgnoreCase("NA")) {
                                    queryCount++;
                                    pwd4.println("update proj_task set "
                                            + "startdate=(select DATE_FORMAT(CONVERT_TZ('" + startDate + "','" + frm_tz + "','" + sys_tz + "'),'%Y-%m-%d')),"
                                            + "enddate=(select DATE_FORMAT(CONVERT_TZ('" + endDate + "','" + frm_tz + "','" + sys_tz + "'),'%Y-%m-%d')),"
                                            + "actualstartdate=(select DATE_FORMAT(CONVERT_TZ('" + actualStartDate + "','" + frm_tz + "','" + sys_tz + "'),'%Y-%m-%d')) "
                                            + "where proj_task.taskid = '" + taskid + "';\n");
                                }
                                logCount++;

                            } // eof loop3
                        } else {
                            if (!startDate.equalsIgnoreCase("NA") && !endDate.equalsIgnoreCase("NA") && !actualStartDate.equalsIgnoreCase("NA")) {
                                queryCount++;
                                pwd4.println("update proj_task set "
                                        + "startdate=(select DATE_FORMAT('" + startDate + "','%Y-%m-%d')),"
                                        + "enddate=(select DATE_FORMAT('" + endDate + "','%Y-%m-%d')),"
                                        + "actualstartdate=(select DATE_FORMAT('" + actualStartDate + "','%Y-%m-%d')) "
                                        + "where proj_task.taskid = '" + taskid + "';\n");

                            }
                        }
                    } // eof loop2
                }
            } // eof loop1
        }

        pwd1.close();
        pwd2.close();
        pwd3.close();
        pwd4.close();

        f1.close();
        f2.close();
        f3.close();
        f4.close();

        conn.close();
    } catch (ServiceException e) {
        System.out.println("Service Exception " + e);
        e.printStackTrace();
    } catch (FileNotFoundException e) {
        System.out.println("File Exception " + e);
        e.printStackTrace();
    }
%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Script for changing pre-existing data as per Timezone Change </title>
    </head>
    <body>
        <h1>Goto : <%=directory%></h1>
        <br/><p>No. of queries formed : <%=queryCount%></p>
    </body>
</html>
