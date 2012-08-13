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
<%--<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@ page  language="java" %>
<%@ page  import="java.io.*"
          import="java.sql.*" 
          import="java.math.*"
          import="com.krawler.utils.json.*"%>
<%@ page import="com.krawler.esp.database.*" %>
<%@ page import="com.krawler.esp.handlers.Forum" %>
<%@ page import="com.krawler.esp.handlers.AuthHandler"%>
<%@ page import="com.krawler.utils.json.JSONException"%>
<%@ page import="com.krawler.common.session.SessionExpiredException"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%
	try {
		if (sessionbean.validateSession(request, response)) {
                        String userid = AuthHandler.getUserid(request);
                        String comid = request.getParameter("comid");
                        int actionid = 0;
                        int urelationid = Integer.parseInt(request.getParameter("status"));
                        switch(urelationid)
                        {
                            case 1:
                                actionid = 11;
                                break;
                            case 3:
                                actionid = 3;
                                break;
                            case 0:
                                actionid = 6;
                                break;
                            case 5:
                                actionid = 2;
                                break;
                            case 4:
                                actionid = 8;
                                break;
                        }

                        out.println(dbcon.setStatusCommunity(userid,comid,urelationid,1));
		} else {
                        out.println("{\"data\":[{\"result\":\"false\"}]}");
		}
	} catch (SessionExpiredException sex) {
                out.println("{\"data\":[{\"result\":\"false\"}]}");
        }
%>--%>
