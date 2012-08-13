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
<%@page import="com.krawler.svnwebclient.web.model.data.*,com.krawler.svnwebclient.decorations.IIconDecoration,com.krawler.common.util.KrawlerLog"%>
<%@page import="com.krawler.svnwebclient.SVNWebClientException"%>
<%@page import="com.krawler.utils.json.base.JSONArray"%>
<%@page import="com.krawler.utils.json.base.JSONObject"%>
<%@page import="com.krawler.esp.handlers.FileHandler"%>
<%@page import="com.krawler.utils.json.base.JSONException"%>

<jsp:useBean id="bean" scope="request"
	class="com.krawler.svnwebclient.web.controller.RevisionListBean" />
<%
	try {
		if (bean.execute(request, response)) {
			RevisionList content = bean.getRevisionList();
			JSONObject jobj = new JSONObject();
			JSONArray jarray = new JSONArray();
                        String author="";
                        String comment="";
			int totalVersions = content.getRevisions().size();
			for (int i = 0; i < totalVersions; i++) {
				RevisionList.Element element = (RevisionList.Element) content
						.getRevisions().get(i);
				if (element.isRevisionDecorated()) {
					IIconDecoration decoration = element
							.getRevisionDecoration();
				}
				JSONObject j = new JSONObject();
				j.put("Revision", element.getRevision());
				j.put("Version", totalVersions - i);
				j.put("Age", element.getDate());
                                author = element.getComment().substring(0,element.getComment().indexOf("*userName*"));
				/*j.put("Author", FileHandler.getAuthor(request
						.getParameter("url"), 1));*/
                                j.put("Author", author);                
                                comment = element.getComment().replace(author+"*userName*","");
				j.put("Comment",comment);
				j.put("Download", element.getDownloadUrl());
				jarray.put(j);
			}
			jobj.put("data", jarray);
%>
<%=jobj.toString()%>
<%
		} else {
			KrawlerLog.op
					.warn("Problem While Showing Revision List of File :");
			out.print("error");
		}
	} catch (SVNWebClientException e) {
		KrawlerLog.op
				.warn("Problem While Showing Revision List of File :"
						+ e.toString());
		out.print("error");
	} catch (JSONException e) {
		KrawlerLog.op
				.warn("Problem While Showing Revision List of File :"
						+ e.toString());
		out.print("{\"valid\": true, \"data\":[{\"success\": false}, {\"msg\": \"A problem occurred while showing Revision list of the document.\"}]}");
	}
%>
