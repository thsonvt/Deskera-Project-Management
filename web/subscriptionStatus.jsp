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
<%@ page import= "com.krawler.database.DbPool.Connection"%>
<%@ page import= "com.krawler.esp.database.dbcon"%>
<%@ page import="com.krawler.utils.json.KWLJsonConverter" %>
<%@ page import= "com.krawler.common.util.StringUtil"%>
<%@ page import= "com.krawler.common.timezone.Timezone"%>
<%
        String msg = "";
        Connection conn1 = null;
        /*    if ((request.getParameter("details") != null) || ((request.getParameter("isPostBack") != null) &&
        (StringUtil.equal(request.getParameter("UserName"), "ronin") && StringUtil.equal(request.getParameter("Password"), "1234")))) {
        String options = "";
        String getCompany1 = "SELECT concat(companyname, ' (', subdomain,')') AS name, companyid FROM company ORDER BY companyname;";
        Connection conn1 = null;
        try{
        conn1 = DbPool.getConnection();
        PreparedStatement pstmt = conn1.prepareStatement(getCompany1);
        ResultSet rs = pstmt.executeQuery();
        while(rs.next()){
        options += "<option value='" + rs.getString("companyid") + "'>" + rs.getString("name") + "</option>";
        }
        } catch(ServiceException e){

        } finally {
        DbPool.quietClose(conn1);
        }
        msg = "<form action='subscriptionStatus.jsp' method='POST'><input type='hidden' name='details'>" +
        "<select name = 'companyid'>" + options + "</select><input type='submit' value='Submit'></form>" +
        "<div style='margin: auto; width: 80%;'>";*/
        try {
            int mode = Integer.parseInt(request.getParameter("mode"));
            conn1 = DbPool.getConnection();
            PreparedStatement p = null;
            ResultSet r = null;
            String loginid = request.getParameter("loginid");
            switch (mode) {
                case 1:
                    p = conn1.prepareStatement("SELECT concat(companyname, ' (', subdomain,')') AS name, companyid FROM company ORDER BY companyname;");
                    r = p.executeQuery();
                    KWLJsonConverter k = new KWLJsonConverter();
                    com.krawler.utils.json.base.JSONObject t = new com.krawler.utils.json.base.JSONObject();
                    msg = k.GetJsonForGrid(r);
                    t.put("valid", true);
                    t.put("data", msg);
                    msg = t.toString();
                    break;
                case 2:
                    String cid = request.getParameter("companyid");
                    if (!StringUtil.isNullOrEmpty(cid)) {
                        p = conn1.prepareStatement("SELECT companyname,subdomain,createdon,concat(fname, ' ', lname) AS name,users.emailid as email " +
                                "FROM company INNER JOIN users ON company.creator = users.userid WHERE company.companyid=?");
                        p.setString(1, cid);
                        r = p.executeQuery();
                        if (r.next()) {
                            String data = "<TR>" +
                                    "<TD class='projectTD'>" + r.getString("companyname") + "</TD>" +
                                    "<TD class='projectTD'>" + r.getString("subdomain") + "</TD>" +
                                    "<TD class='projectTD'>" + Timezone.toUserTimezone(conn1, r.getString("createdon"), loginid) + "</TD>" +
                                    "<TD class='projectTD'>" + r.getString("email") + "</TD>" +
                                    "<TD class='projectTD'>" + r.getString("name") + "</TD>" +
                                    "</TR>";
                            msg += "Company Details<table style='margin: auto; font-size: 0.9em; width: 95%;' class='kwlAdminTab'>" +
                                    "<thead><tr>" +
                                    "<th><b>Company Name</b></th>" +
                                    "<th><b>Subdomain</b></th>" +
                                    "<th><b>Created On</b></th>" +
                                    "<th><b>e-mail id</b></th>" +
                                    "<th><b>Created By</b></th>" +
                                    "</tr></thead><tbody>" + data + "</tbody></table>";
                            msg += "Subscription Details";
                            PreparedStatement p1 = conn1.prepareStatement("SELECT subid,billdate,numproj,subnum FROM subscriptions WHERE companyid=?");
                            p1.setString(1, cid);
                            ResultSet r1 = p1.executeQuery();
                            data = "";
                            while (r1.next()) {
                                data += "<TR>" +
                                        "<TD class='projectTD'>" + r1.getString("billdate") + "</TD>" +
                                        "<TD class='projectTD' width='50%'>" + r1.getString("numproj") + "</TD>" +
                                        "<TD class='projectTD'>" + r1.getString("subnum") + "</TD>" +
                                        "</TR>";
                                PreparedStatement p2 = conn1.prepareStatement("SELECT paymentid,paytime,refnum,amount,receipt FROM payments WHERE subid=?");
                                p2.setString(1, r1.getString("subid"));
                                ResultSet r2 = p2.executeQuery();
                                String pd = "";
                                while (r2.next()) {
                                    pd += "<TR>" +
                                            "<TD class='projectTD'>" + r2.getString("refnum") + "</TD>" +
                                            "<TD class='projectTD'>" + r2.getString("paytime") + "</TD>" +
                                            "<TD class='projectTD'>" + r2.getString("amount") + "</TD>" +
                                            "</TR>";
                                }
                                if (!StringUtil.isNullOrEmpty(pd)) {
                                    data += "<TR><TD>Payment Details</TD><TD></TD><TD></TD></TR>" +
                                            "<TR><TD></TD><TD colspan=2><TABLE><TR><th><b>Reference No.</b></th>" +
                                            "<th><b>Payment Date</b></th>" +
                                            "<th><b>Paid Amount</b></th>" +
                                            "</TR>" + pd + "</TABLE></TD></TR>";
                                } else {
                                    data += "<TR><TD colspan = 3>No payment done for this subscription data.</TD></TR>";
                                }
                            }
                            if (!StringUtil.isNullOrEmpty(data)) {
                                msg += "<table style='margin: auto; font-size: 0.9em; width: 95%;' class='kwlAdminTab'>" +
                                        "<thead><tr>" +
                                        "<th><b>Subscription Date</b></th>" +
                                        "<th><b>Number Of Project</b></th>" +
                                        "<th><b>Subscription No.</b></th>" +
                                        "</tr></thead><tbody>" + data + "</tbody></table>";
                            } else {
                                msg += "<br>There is no subscription data.";
                            }
                            msg += "</div>";
                        }
                    }
                break;
            }
        } catch (ServiceException e) {
        } finally {
            DbPool.quietClose(conn1);
        }
        /*    } else {
        msg = "<div id='wrapper'><div id='content'><div class='content'>" +
        "<form action='subscriptionStatus.jsp' name='loginForm' id='loginForm' method='POST'><p><label for='UserName' id='UserNameLabel' class='labels'>" +
        "Username:</label><input name='UserName' type='text id='UserName' class='inputbox'/><span id='UserNameRequired' class='errorprompt' title='Username is required.'>*</span>" +
        "</p><p><label for='Password' id='PasswordLabel' class='labels'>Password:</label>" +
        "<input name='Password' type='password' id='Password' class='inputbox'/><span id='PasswordRequired' class='errorprompt' title='Password is required.'>*</span>" +
        "</p><p><input type='submit' name='LoginButton' value='Login' id='LoginButton'/><span id='usrFeedback'></span></p>" +
        "<input type='hidden' name='isPostBack'/></form></div></div></div>";
        }*/
%>
<%=msg%>
