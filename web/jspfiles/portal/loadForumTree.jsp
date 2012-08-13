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
<%@ page import="com.krawler.esp.portalmsg.forummsgdbcon"%>
<%@page import="com.krawler.common.session.SessionExpiredException"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />

<%
com.krawler.utils.json.base.JSONObject jbj = new com.krawler.utils.json.base.JSONObject();
if (sessionbean.validateSession(request, response)) {
    jbj.put("valid", true); 
    	try {
		String offset = request.getParameter("start");
		String limit = request.getParameter("limit");
		String groupId = request.getParameter("groupId");
		int col = Integer.parseInt(request.getParameter("col"));
		String searchText = java.net.URLDecoder.decode(request.getParameter("searchText").trim());
		String sortFlag = request.getParameter("sortFlag");
                String loginid = com.krawler.esp.handlers.AuthHandler.getUserid(request);
		String treeString = forummsgdbcon.fillTree(offset, limit, groupId,
				searchText, col, sortFlag,loginid);
                jbj.put("data",treeString);
		//out.print(treeString);
	} catch (Exception e) {
            jbj.put("data",e);
		out.print(e);
	}
        out.println(jbj.toString());
} else {
	out.println("{\"valid\": false}");
}   
%>
