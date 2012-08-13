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
<%@ page contentType="text/html"%>
<%@ page pageEncoding="UTF-8"%>
<%@ page import="com.krawler.esp.database.*"%>
<%@ page import="com.krawler.utils.json.base.JSONObject"%>
<%@ page import="com.krawler.esp.handlers.AuthHandler"%>
<%@ page import="com.krawler.utils.json.base.JSONException"%>
<%@ page import="com.krawler.common.session.SessionExpiredException"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%
	JSONObject jbj = new JSONObject();
	JSONObject resp1 = new JSONObject();
	try {
		if (sessionbean.validateSession(request, response)) {
			try {
				String name = request.getParameter("res");
				String userid = AuthHandler.getUserid(request);
				String docid = request.getParameter("docid");
				String groupid = request.getParameter("groupid");
				String pcid = request.getParameter("pcid");
				String id = "";
				if (groupid.equals("1")) {
					id = userid;
				} else {
					id = pcid;
				}
				if (dbcon.addButtonTag(id, docid, name))
					resp1.put("res", 0);
				else
					resp1.put("res", 1);
				jbj.put("valid", true);
				jbj.put("data", resp1.toString());
			} catch (SessionExpiredException sex) {
				jbj.put("valid", false);
			}
		} else {
			jbj.put("valid", false);
		}
%>
<%=jbj.toString()%>
<%
	} catch (JSONException jex) {
		out.println("{}");
	}
%>
