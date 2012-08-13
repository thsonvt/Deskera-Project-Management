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
<%@page import="com.krawler.esp.database.*" import="javax.servlet.ServletRequest.*"%>
<%@page import="com.krawler.esp.handlers.AuthHandler"%>
<%@page import="com.krawler.common.session.SessionExpiredException"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%
	if (sessionbean.validateSession(request, response)) {
		try {
			String resultstring = null;
            if(request.getParameter("mode").equals("connection")){
                if (request.getParameter("flag").equals("1")) {
                    resultstring = dbcon.getConnections(AuthHandler.getUserid(request), request.getParameter("docid"));
                } else {
                    resultstring = dbcon.getConnectionsRightGrid(AuthHandler.getUserid(request), request.getParameter("docid"));
                }
            } else if(request.getParameter("mode").equals("project")){
                if (request.getParameter("flag").equals("1")) {
                    resultstring = dbcon.getProjects(AuthHandler.getUserid(request), request.getParameter("docid"));
                } else {
                    resultstring = dbcon.getProjectsRightGrid(AuthHandler.getUserid(request), request.getParameter("docid"));
                }
            }
			out.println(resultstring);
		} catch (SessionExpiredException sex) {
			out.println("{\"data\": []}");
		}
	} else {
		out.println("{\"data\": []}");
	}
%>
