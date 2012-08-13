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
<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import= "java.io.IOException"%>
<%@ page import= "java.sql.PreparedStatement"%>
<%@ page import= "java.sql.ResultSet"%>
<%@ page import= "java.sql.SQLException"%>
<%@ page import= "com.krawler.common.service.ServiceException"%>
<%@ page import= "com.krawler.database.DbPool"%>
<%@ page import= "com.krawler.database.DbResults"%>
<%@ page import= "com.krawler.database.DbUtil"%>
<%@ page import= "com.krawler.database.DbPool.Connection"%>
<%@ page import= "com.krawler.esp.database.dbcon"%>
<%@ page import= "com.krawler.common.util.StringUtil"%>
<%
            String msg = "";
//            if ((request.getParameter("isPostBack") != null) &&
//                    (StringUtil.equal(request.getParameter("UserName"), "ronin") && StringUtil.equal(request.getParameter("Password"), "1234"))) {
                String query = "SELECT companyname, company.companyid,userlogin.username,website,subdomain,users.emailid " +
                        "FROM company INNER JOIN users ON company.creator = users.userid inner join userlogin on users.userid=userlogin.userid;";
                String countQry = "SELECT COUNT(projectid) AS projcount FROM project WHERE companyid = ?";
                String getProj = "SELECT projectid, projectname FROM project WHERE companyid = ? ";
                String usersQry = "SELECT COUNT(userid) AS usercount FROM users WHERE companyid = ?";
                String projUsers = "SELECT COUNT(userid) AS count FROM projectmembers WHERE projectid = ?";
                PreparedStatement pstmt1 = null;
                PreparedStatement pstmt2 = null;
                PreparedStatement pstmt3 = null;
                PreparedStatement pstmt4 = null;
                PreparedStatement pstmt5 = null;
                ResultSet rs1 = null;
                ResultSet rs2 = null;
                ResultSet rs3 = null;
                ResultSet rs4 = null;
                ResultSet rs5 = null;
                String data = "";
                Connection conn = null;
                try {
                    conn = DbPool.getConnection();
                    int proj_count = 0;
                    int users_count = 0;
                    pstmt1 = conn.prepareStatement(query);
                    rs1 = pstmt1.executeQuery();
                    while (rs1.next()) {
                        pstmt2 = conn.prepareStatement(countQry);
                        pstmt2.setString(1, rs1.getString("companyid"));
                        rs2 = pstmt2.executeQuery();
                        if (rs2.next())
                            proj_count = rs2.getInt("projcount");
                        pstmt3 = conn.prepareStatement(usersQry);
                        pstmt3.setString(1, rs1.getString("companyid"));
                        rs3 = pstmt3.executeQuery();
                        if (rs3.next())
                            users_count = rs3.getInt("usercount");
                        data += "<TR><TD>" + rs1.getString("companyname") + "</TD>" +
                                   "<TD>" + rs1.getString("username") + "</TD>" +
                                   "<TD>" + rs1.getString("subdomain") + "</TD>" +
                                   "<TD>" + rs1.getString("emailid") + "</TD>" +
                                   "<TD>" + Integer.toString(proj_count) + "</TD>" +
                                   "<TD>" + Integer.toString(users_count) + "</TD></TR>";
                        if (proj_count > 0) {
                            pstmt4 = conn.prepareStatement(getProj);
                            pstmt4.setString(1, rs1.getString("companyid"));
                            rs4 = pstmt4.executeQuery();
                            while (rs4.next()) {
                                pstmt5 = conn.prepareStatement(projUsers);
                                pstmt5.setString(1, rs4.getString("projectid"));
                                rs5 = pstmt5.executeQuery();
                                if (rs5.next()) {
                                    data += "<TR><TD></TD><TD></TD><TD></TD><TD></TD>" +
                                            "<TD class='projectTD'>" + rs4.getString("projectname") + "</TD>" +
                                            "<TD class='projectTD'>" + Integer.toString(rs5.getInt("count")) + "</TD></TR>";
                                }
                            }
                        }
                        msg = "<table style='margin: auto; font-size: 1.2em; width: 80%' class='kwlAdminTab'>" +
                            "<thead><tr>" +
                            "<th><b>Company Name</b></th>" +
                            "<th><b>Creator</b></th>" +
                            "<th><b>Subdomain</b></th>" +
                            "<th><b>e-mail id</b></th>" +
                            "<th><b>Projects</b></th>" +
                            "<th><b>Users</b></th></tr></thead><tbody>" + data + "</tbody></table>";
                    }
                } catch (SQLException e) {
                    System.out.println(e);
                    throw ServiceException.FAILURE("SuperUser: ", e);
                } finally {
                    DbPool.closeStatement(pstmt1);
                    DbPool.closeStatement(pstmt2);
                    DbPool.closeStatement(pstmt3);
                    DbPool.closeStatement(pstmt4);
                    DbPool.quietClose(conn);
                }
//        } else {
//            msg = "<div id='wrapper'><div id='content'><div class='content'>" +
//                    "<form action='companystats.jsp' name='loginForm' id='loginForm' method='POST'><p><label for='UserName' id='UserNameLabel' class='labels'>" +
//                    "Username:</label><input name='UserName' type='text id='UserName' class='inputbox'/><span id='UserNameRequired' class='errorprompt' title='Username is required.'>*</span>" +
//                    "</p><p><label for='Password' id='PasswordLabel' class='labels'>Password:</label>" +
//                    "<input name='Password' type='password' id='Password' class='inputbox'/><span id='PasswordRequired' class='errorprompt' title='Password is required.'>*</span>" +
//                    "</p><p><input type='submit' name='LoginButton' value='Login' id='LoginButton'/><span id='usrFeedback'></span></p>" +
//                    "<input type='hidden' name='isPostBack'/></form></div></div></div>";
//        }
%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
        <meta name="robots" content="noindex,nofollow" />
        <title>Application Usage - Deskera</title>
        <link rel="shortcut icon" href="images/deskera.png"/>
    </head>
    <body>
        <%=msg%>
    </body>
</html>
