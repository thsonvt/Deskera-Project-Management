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
<%@ page import="com.krawler.common.util.StringUtil" %>
<%@ page import="com.krawler.esp.database.*" %>
<%@ page import="com.krawler.esp.handlers.AuthHandler"%>
<%@ page import="com.krawler.common.session.SessionExpiredException"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%
	if (sessionbean.validateSession(request, response)) {
		try {
			String ulogin = request.getParameter("login");
			String upagesize = "0";
			String upageno = "0";
			if (ulogin == null || StringUtil.isNullOrEmpty(ulogin)) {
				ulogin = AuthHandler.getUserid(request);
			}
			upagesize = request.getParameter("pageSize");
			upageno = request.getParameter("pageno");

			int offset = (Integer.parseInt(upagesize) * Integer
					.parseInt(upageno));
			int pagesize1 = Integer.parseInt(upagesize);
			String splitString = dbcon.getUserCommunities(ulogin,
					pagesize1, offset);
			out.println(" " + splitString);
		} catch (SessionExpiredException sex) {
			out.println("{\"data\": []}");
		}
	} else {
		out.println("{\"data\": []}");
	}
%>
