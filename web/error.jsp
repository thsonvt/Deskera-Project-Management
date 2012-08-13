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
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="com.krawler.esp.handlers.AuthHandler"%>
<%@page import="com.krawler.database.DbPool"%>
<%@page import="com.krawler.database.DbPool.Connection"%>
<%@ page import="com.krawler.esp.handlers.CompanyHandler" %>
<%@page import="com.krawler.common.service.ServiceException"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%
	String pageTitle = "Access Denied";
	String errReason = request.getParameter("e");
	String newDomain = request.getParameter("n");
	String errMsg = "Sorry, the page you requested doesn't exist.";
	String errImgPath = "pagenotfound";
	String subdomainFromSession = null;

	if (!StringUtil.isNullOrEmpty(errReason)) {
		if ("noaccess".equalsIgnoreCase(errReason)) {
			errImgPath = errReason;
			errMsg = "Sorry, you don't have access to this application";
		} else if ("alreadyloggedin".equalsIgnoreCase(errReason)
				&& !StringUtil.isNullOrEmpty(newDomain)) {
			Connection conn = null;
			try {
				if (sessionbean.validateSession(request, response)) {
					String userid = AuthHandler.getUserid(request);
					String companyid = AuthHandler
							.getCompanyid(request);
					conn = DbPool.getConnection();
					subdomainFromSession = CompanyHandler.getCompanySubdomainByCompanyID(conn, companyid);
					pageTitle = "Already logged in";
					errMsg = "You are already logged in with another Deskera account.";
				}
			} catch (ServiceException sE) {
				//DbPool.quietRollback(conn);
			} finally {
				DbPool.quietClose(conn);
			}
		}
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<title>Deskera - <%=pageTitle%></title>
	<link href="style/error.css" rel="stylesheet" type="text/css" />
	<link rel="shortcut icon" href="images/deskera.png"/>
</head>
<body>
<div id="top">
  <div class="top-wapper"> <a href="./"><div id="logo"></div></a>
    <div>
      <div id="menu">
        <ul>
          <li class="current"><a title="Home" href="./">Home</a></li>
          <li><a title="SignUp" href="http://signup.deskera.com/?ref=error">Sign Up</a></li>
          <li><a title="Blog" href="http://blog.deskera.com/">Blog</a></li>
          <li><a title="Forum" href="http://forum.deskera.com/">Forum</a></li>
          <li ><a title="Feedback" href="http://feedback.deskera.com/">Feedback</a></li>
        </ul>
      </div>
    </div>
  </div>
</div>
<div class="pagenotfound">
  <p style="padding: 0px 0pt 0pt; text-align: center; height: 223px;"><img id="errimg" src="images/<%=errImgPath%>.jpg" width="250" height="235" /></p>
  <p class="error" style="text-align: center; color:#A70808;" id="errmsg"><%=errMsg%></p>
  <div class="content-list" style="width: 91%;">
<%
	if ("alreadyloggedin".equalsIgnoreCase(errReason)
			&& !StringUtil.isNullOrEmpty(newDomain)
			&& !StringUtil.isNullOrEmpty(subdomainFromSession)) {
%>
    <p class="error-hd"><strong>You may do the following:</strong></p>
    <ul>
      <li>Click <a href="jspfiles/signOut2.jsp?n=<%=newDomain%>" class="highlight2">here to sign out from logged-in account</a> and continue to sign in to new account, or</li>
      <li>Click <a href="a/<%=subdomainFromSession%>/" class="highlight2">here to go back to already logged-in Deskera account</a></li>
    </ul>
<%
	} else {
%>
    <p class="error-hd"><strong>You may not be able to visit this page because of:</strong></p>
    <ul>
      <li>An <span class="highlight2">out-of-date bookmark/favourite</span></li>
      <li>A <span class="highlight2">mis-typed address</span></li>
      <li>You have <span class="highlight2">no access</span> to this page</li>
      <li>An error has occurred while processing your request.</li>
	  <li>A link from an <span class="highlight2">out-of-date mail</span></li>
    </ul>
<%
	}
%>
	<div style="clear:both"></div>
	<p class="error-hd">Please contact your Deskera administrator or write to us at <img src="images/support-yellow.gif" width="120" height="14" align="absmiddle" /></p>
  </div>
</div>
</body>
</html>
