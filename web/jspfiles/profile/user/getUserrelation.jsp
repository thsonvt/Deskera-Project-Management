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
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@ page  language="java" %>
<%@ page  import="java.io.*"
          import="java.sql.*" 
          import="java.math.*"
          import="com.krawler.utils.json.*"%>
 <%@ page import="com.krawler.esp.database.*" %>
 <%@ page import="com.krawler.esp.handlers.ProfileHandler" %>
<%@ page import="com.krawler.common.session.SessionExpiredException"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%
if (sessionbean.validateSession(request, response)) { 
    String userid1 = null;
    String userid2 = null;
    String splitstring = null;
    userid1 =request.getParameter("userid1");
    userid2 = request.getParameter("userid2");
    splitstring = dbcon.getUserRelation(userid1, userid2);
    //out.println(" " + splitstring);
    com.krawler.utils.json.base.JSONObject jbj = new com.krawler.utils.json.base.JSONObject();
    jbj.put("valid", "true");
    jbj.put("data", splitstring);
    out.print(jbj.toString());
} else {
    out.println("{\"valid\": false}");
} 
 %>
