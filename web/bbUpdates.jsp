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
    Document   : bbUpdates
    Created on : Apr 20, 2011, 3:48:39 PM
    Author     : Abhay
--%>

<%@page import="com.krawler.common.util.URLUtil"%>
<%@page import="com.krawler.esp.utils.ConfigReader"%>
<%@page import="com.krawler.esp.handlers.CompanyHandler"%>
<%@page import="com.krawler.esp.handlers.projdb"%>
<%@page import="com.krawler.esp.handlers.Forum"%>
<%@page import="com.krawler.database.DbPool.Connection"%>
<%@page import="com.krawler.database.DbPool"%>
<%@page import="com.krawler.esp.handlers.DashboardHandler"%>
<%@page import="com.krawler.common.service.ServiceException"%>
<%@page import="java.sql.SQLException"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="com.krawler.utils.json.base.JSONException"%>
<%@page import="com.krawler.utils.json.base.JSONArray"%>
<%@page import="com.krawler.utils.json.base.JSONObject"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="java.sql.DriverManager"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <style type="text/css">
            body {
                font-family: Tahoma,Verdana,Arial,Helvetica,sans-sarif;
                font-size: 12px;
            }
            .listpanelcontent {
                font-size:12px;
            }
            .overduetask {
                background:url("images/overduetask.png") no-repeat scroll 0 0 transparent !important;
            }
            .taskStatusWidget {
                margin-left:25px;
                width:85%;
            }
            .litetext{
                color: gray;
            }
            .redtext {
                color:#FF6666;
            }
            .statusclr {
                clear:both;
                height:0;
                line-height:0;
                visibility:hidden;
            }
            .statusitemimg {
                width: 16px;
                height: 16px;
                float: left;
                margin-top: 0.5%;
            }
            .statusitemcontent {
                float: left;
                padding-bottom: 0.5%;
                padding-top: 0.5%;
                padding-left: 0.5%;
                width: 94%;
                border-bottom: 1px dotted #cccccc;
            }
            .statuspanelcontentiteminner{
                padding-top: 10px;
                padding-left: 4px;
            }
            .link{
                color: #003C96;
                text-decoration: none;
                cursor: pointer;
            }
            div.link:hover{
                text-decoration: underline;
            }
            .content {
                background-color:white;
                border:10px solid #CCCCCC;
                margin:auto;
                padding:30px;
                text-align:left;
                width:200px;

            }
            #content {
                color:black;
                left:0;
                position:absolute;
                top:25%;
                width:100%;
            }
            #wrapper {
                margin:0;
                padding:0;
                text-align:center;
            }
        </style>
    </head>
    <body>
<%
                Connection conn = null;
                String msg = "";
                String backBtn = "<input type='button' style='margin-left:20px;width:80px;' onclick='javascript:location.replace(\"%s\");' value='&lt;  Back'/>";
                String backLink = "<center>Click <a href='#' onclick='javascript:location.replace(\"%s\");'>here</a> to go back.</center>";
                String info = "<div id='wrapper'><div id='content'><div class='content'><div id='msg' class='success'><p>%s</p>" +
                                  "</div><div id='btnDiv'>%s</div></div></div></div>";
                try {
                    conn = DbPool.getConnection();
                    String projid = null, userid = request.getParameter("userid");
                    String subdomain = request.getParameter("subdomain");
                    String mobilePagesPath = ConfigReader.getinstance().get("crmURL");

                    String companyid = "";
                    int limit = 3;
                    if (!StringUtil.isNullOrEmpty(userid) && !StringUtil.isNullOrEmpty(subdomain)) {
                        mobilePagesPath = mobilePagesPath + "b/" + subdomain + "/bbmobile/welcomepage.jsp";
//                        mobilePagesPath = "http://crm.deskera.com/b/" + subdomain + "/bbmobile/welcomepage.jsp";
                        backBtn = String.format(backBtn, mobilePagesPath);
                        backLink = String.format(backLink, mobilePagesPath);
                        companyid = CompanyHandler.getCompanyByUser(conn, userid);
                        if (!StringUtil.isNullOrEmpty(companyid)) {
                            StringBuilder finalString = new StringBuilder();
                            StringBuilder overdue = new StringBuilder();
                            boolean sub = DashboardHandler.isSubscribed(conn, companyid, "usrtk");
                            String projectlist = DashboardHandler.getProjectList(conn, userid, 1000, 0, "");
                            JSONObject projListObj = new JSONObject(projectlist);
                            JSONArray projList = projListObj.getJSONArray("data");
                            boolean hasRes = false;
                            for (int i = 0; i < projList.length(); i++) {
                                JSONObject temp = projList.getJSONObject(i);
                                projid = temp.getString("id");
                                JSONObject membership = new JSONObject(Forum.getProjectMembership(conn, userid, projid));
                                JSONObject userProjData = membership.getJSONArray("data").getJSONObject(0);
                                boolean uT = (userProjData.getInt("connstatus") != 4 && userProjData.getInt("planpermission") == 2 && sub);
                                String projName = projdb.getProjectName(conn, projid);
                                int mode = 3;
                                if (userProjData.getInt("connstatus") == 4) {
                                    mode = 4;
                                } else if (uT) {
                                    mode = 6;
                                }
                                ResultSet rsForSubQ = null;
                                String sql = "SELECT datediff(date(now()),date(proj_task.enddate)) AS tasklength," +
                                    "taskname, percentcomplete AS complete,taskid FROM proj_task " +
                                    "WHERE projectid = ? AND (date(proj_task.enddate) < date(now()) AND percentcomplete < 100) ORDER BY tasklength ASC";
                                PreparedStatement p = conn.prepareStatement(sql);
                                p.setString(1, projid);
                                if(mode == 6){
                                    sql = "SELECT datediff(date(now()),date(proj_task.enddate)) AS tasklength," +
                                        "taskname, percentcomplete AS complete,proj_task.taskid AS taskid " +
                                        "FROM proj_task INNER JOIN proj_taskresourcemapping ON proj_task.taskid = proj_taskresourcemapping.taskid " +
                                        "WHERE projectid = ? AND proj_taskresourcemapping.resourceid = ? AND (date(proj_task.enddate) < date(now())) " +
                                        "ORDER BY tasklength ASC";
                                    p = conn.prepareStatement(sql);
                                    p.setString(1, projid);
                                    p.setString(2, userid);
                                }
                                rsForSubQ = p.executeQuery();
                                while (rsForSubQ.next()) {
                                    String resrs = "";
                                    int tasklength = Integer.parseInt(rsForSubQ.getObject("tasklength").toString());
                                    hasRes = false;
                                    if(tasklength <= limit){
                                        String taskName = rsForSubQ.getString("taskname");
                                        overdue.append("<div class=\"statusitemimg overduetask\">&nbsp;</div><div class=\"statusitemcontent\">");
                                        overdue.append("<b>" + taskName + "</b> (" + projName + ")");
                                        overdue.append(" - " + rsForSubQ.getObject("complete").toString() + "%" + " Complete ");
                                        if (tasklength == 1)
                                            resrs = "<span class = 'redtext'>Overdue by 1 day</span>";
                                        else
                                            resrs = "<span class = 'redtext'>Overdue by " + rsForSubQ.getObject("tasklength").toString() + " days</span>";
                                        overdue.append(resrs);
                                        resrs = "";
                                        if(mode == 4){
                                            String r = DashboardHandler.getTaskResources(conn, rsForSubQ.getString("taskid"), projid, userid);
                                            JSONObject res = new JSONObject(r);
                                            if(res.has("data")){
                                                hasRes = true;
                                                JSONArray jarr = res.getJSONArray("data");
                                                String resourceLink = "";
                                                for(int c = 0; c < jarr.length(); c++){
                                                    JSONObject resource = jarr.getJSONObject(c);
                                                    PreparedStatement pstmt = conn.prepareStatement("SELECT count(userid) AS count FROM projectmembers WHERE userid=?");
                                                    pstmt.setString(1,resource.getString("resourceid"));
                                                    ResultSet rs = pstmt.executeQuery();
                                                    if(rs.next() && rs.getInt("count") > 0){
                                                        if(Forum.isInUse(conn, resource.getString("resourceid")))
                                                            resourceLink += "<span style='color: #666666;'>" + resource.getString("resourcename") + "</span>" + ", ";
                                                        else
                                                            resourceLink += "";
                                                    } else
                                                        resourceLink += "<span style='color: #666666;'>" + resource.getString("resourcename") + "</span>" + ", ";
                                                }
                                                resourceLink = resourceLink.substring(0, (resourceLink.length() - 2));
                                                if(!StringUtil.isNullOrEmpty(resourceLink))
                                                    resrs += " [ Assigned to: " + resourceLink + " ]";
                                                overdue.append(DashboardHandler.getContentSpan(resrs));
                                            }
                                        }
                                        taskName = "";
                                        overdue.append("</div>");
                                        if(mode != 4 || !hasRes)
                                            overdue.append("<div class='statusclr'></div>");
                                    }
                                    tasklength = 0;
                                }
                                projid = "";
                            }
                            finalString.append(overdue);
                            if(StringUtil.isNullOrEmpty(finalString.toString())){
                                msg = "There are no critical task updates to display at this time.";
                                finalString.append(String.format(info, msg, backLink));
                            } else {
                                finalString.append("<br>"+backBtn);
                            }
                            out.print(finalString);
                        } else {
                            // companyid not present
                            msg = "Incorrect request. Make sure you have access to this application.";
                            msg = String.format(info, msg, backLink);
                            out.println(msg);
                        }
                    } else {
                        // userid not present
                        msg = "Incorrect request. Make sure you have access to this application.";
                            msg = String.format(info, msg, backLink);
                            out.println(msg);
                    }
                } catch (Exception ex){
                    DbPool.quietRollback(conn);
                    msg = "Incorrect request. Make sure you have access to this application.";
                    msg = String.format(info, msg, backLink);
                    out.println(msg);
                } finally{
                    DbPool.quietClose(conn);
                }

%>

    </body>
</html>
