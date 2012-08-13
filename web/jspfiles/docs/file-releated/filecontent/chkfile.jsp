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
<%@page import="com.krawler.esp.database.*,java.util.*,com.krawler.utils.json.base.*"%>
<%@page import="com.krawler.esp.handlers.AuthHandler"%>
<%@page import="com.krawler.common.session.SessionExpiredException"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%
	JSONObject jobj = new JSONObject();
	JSONObject jbj = new JSONObject();
	try {
		if (sessionbean.validateSession(request, response)) {
			try {
				
				String docname = request.getParameter("docname");
                //String userid = request.getParameter("userid");
				String userid = AuthHandler.getUserid(request);
				java.io.File fi = new java.io.File(docname);
				docname = fi.getName();
				docname = docname
						.substring((docname.lastIndexOf("\\") + 1));
				Hashtable ht = dbcon.FileChk(docname, userid, request
						.getParameter("groupid"), request
						.getParameter("pcid"));
				ArrayList list = (ArrayList) ht.get("value");
				ArrayList id = (ArrayList) ht.get("id");
				ArrayList username = (ArrayList) ht.get("username");
				jobj.put("success", true);
				java.lang.Integer type = (java.lang.Integer) ht
						.get("type");
				jobj.put("type", type);
				if (type == 1 || type == 2)
					jobj.put("userdocid", ht.get("userdocid"));
				jobj.put("userid", ht.get("docownerid"));
				if (list != null) {
					for (int i = 0; i < list.size(); i++) {
						JSONObject jtemp = new JSONObject();
						jtemp.put("userid", list.get(i));
						jtemp.put("username", username.get(i));
						jtemp.put("docid", id.get(i));
						jobj.append("data", jtemp);
					}
				}
				jbj.put("valid", true);
				jbj.put("data", jobj.toString());
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
