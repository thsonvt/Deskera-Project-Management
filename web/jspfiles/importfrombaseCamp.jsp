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
<%@page import="com.krawler.esp.database.*"%>
<%@page import = "com.krawler.esp.handlers.AuthHandler"%>
<%@ page import="com.krawler.esp.servlets.AdminServlet"%>
<%@ page import="java.io.*"%>
<%@ page import="com.krawler.esp.handlers.basecampHandler"%>
<%@ page import="com.krawler.common.session.SessionExpiredException"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%
if (sessionbean.validateSession(request, response)) {  
        if(request.getParameter("action").equals("1")){
            com.krawler.utils.json.base.JSONObject jbj = new com.krawler.utils.json.base.JSONObject();
            jbj.put("success", "true");
            jbj.put("data",  basecampHandler.importUserFromBaseCamp(request));
            out.print(jbj.toString());
        }else if(request.getParameter("action").equals("2")){
            String userMap = request.getParameter("val");
            String cid = AuthHandler.getCompanyid(request);
            String lid = AuthHandler.getUserid(request);
            String docid = request.getParameter("docid");
            String lName = AuthHandler.getUserName(request);
            String ipAddress = AuthHandler.getIPAddress(request);
            String result = basecampHandler.importProjectFromBaseCampUSerChoice(userMap,cid,lid,docid,lName,ipAddress);
            com.krawler.utils.json.base.JSONObject jbj = new com.krawler.utils.json.base.JSONObject();
            jbj.put("valid", "true");
            jbj.put("data",result);
            out.print(jbj.toString());
        }
} else {
    out.println("{\"valid\": false}");
}
%>
