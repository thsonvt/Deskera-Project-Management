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
<%@ page import="com.krawler.svnwebclient.web.model.data.*,
		com.krawler.svnwebclient.decorations.IIconDecoration,
                 com.krawler.common.util.KrawlerLog"
%>  
<%@ page import="com.krawler.svnwebclient.SVNWebClientException"%>
<%@ page import="com.krawler.utils.json.base.JSONObject" %>
<%@page import="com.krawler.esp.handlers.AuthHandler"%>
<%@page import="com.krawler.utils.json.base.JSONException"%>
<jsp:useBean id="bean" scope="request" class="com.krawler.svnwebclient.web.controller.RevisionListBean"/>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%
 JSONObject jbj = new JSONObject();
JSONObject jobj = new JSONObject();      
      try {
           	if (sessionbean.validateSession(request, response)) {
                	jbj.put("valid", true);
                        try{
                            bean.execute(request, response);
                            jobj.put("version", bean.getRevisionList().getRevisionsCount());
                            jbj.put("data",jobj.toString());
                        }
                        catch(SVNWebClientException e){
                              KrawlerLog.op.warn("Problem While Showing Revision ListHeader :" +e.toString());
                              out.println("{}");
                        }
                  }
                 else {
                        jbj.put("valid", false);
              }
%>
<%=jbj.toString()%>
<%
         } catch (JSONException jex) {
		out.println("{}");
	  }
%>              


