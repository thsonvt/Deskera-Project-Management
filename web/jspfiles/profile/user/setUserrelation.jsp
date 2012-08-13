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
import="java.util.*"
import="java.text.SimpleDateFormat"
import="java.util.Date"
import="com.krawler.utils.json.*"%>
<%@ page import="com.krawler.esp.database.*" %>
<%@ page import="com.krawler.esp.handlers.Forum" %>
<%@ page import="com.krawler.utils.json.base.JSONObject"%>
<%@ page import="com.krawler.common.session.SessionExpiredException"%>
<%@page import="com.krawler.common.util.*"%>
<%@page import="com.krawler.esp.handlers.AuthHandler"%>
<%@ page import="com.krawler.esp.handlers.ServerEventManager"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%
if (sessionbean.validateSession(request, response)) {
    java.util.Date dateval = new java.util.Date(System.currentTimeMillis());
    java.text.SimpleDateFormat dtformat = new java.text.SimpleDateFormat("yyyy-MM-d HH:mm:ss");

    int urelationid = 0;
    int actionid = 0;
    String actionName;
    String relationid1 = null;

    String loginid = AuthHandler.getUserid(request);
    String companyid = AuthHandler.getCompanyid(request);
    String uuserid1 =request.getParameter("userid1");
    String uuserid2 = request.getParameter("userid2");
    relationid1 = request.getParameter("relationid");

    urelationid = Integer.parseInt(relationid1);
    switch(urelationid)
    {
        case 3:
                actionid = 41;
                break;
        case 0:
                actionid = 45;
                break;
        case 1:
                actionid = 82;
                break;
    }
    String success=dbcon.setUserrelation(loginid,companyid, uuserid1,uuserid2,relationid1,urelationid,actionid,dateval,dtformat);
    com.krawler.utils.json.base.JSONObject jbj = new com.krawler.utils.json.base.JSONObject();
    jbj.put("valid", "true");
    jbj.put("data", success);
    out.print(jbj.toString());
    try {
        JSONObject jpub = new JSONObject();
        jpub.append("data", "{'data':[{mode :'online'},{status:'request',userid:'" + uuserid1 + "'}]}");
        Map<String, String> data = new HashMap<String, String>();
        data.put("data", jpub.toString());
        ServerEventManager.publish("/" + loginid + "/chat", data, this.getServletContext());
    } catch (Exception e) {

    }
    //out.println(success);
} else {
	out.println("{\"valid\": false}");
}
%>
