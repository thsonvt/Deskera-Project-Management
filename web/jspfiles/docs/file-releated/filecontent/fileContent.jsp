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


<%@ page import="com.krawler.svnwebclient.web.model.data.file.*,
                 java.util.List,
                 java.util.Iterator,
                 java.util.Hashtable,
                 com.krawler.svnwebclient.web.model.Button,
                 com.krawler.svnwebclient.configuration.ConfigurationProvider,
                 org.tmatesoft.svn.core.internal.wc.SVNFileUtil,
                 java.io.File" %>  
<%@page import="com.krawler.utils.json.base.JSONObject"%>
<%@page import="com.krawler.utils.json.base.JSONException"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />          
<jsp:useBean id="bean" scope="request" class="com.krawler.svnwebclient.web.controller.file.FileContentBean"/>
<%
JSONObject jbj = new JSONObject();
try {
		
		if (sessionbean.validateSession(request, response)) {
			   jbj.put("valid", true);
                           Hashtable ht = com.krawler.esp.database.dbcon.getfileinfo(request.getParameter("url"));    
                           int intSize = ((Integer.parseInt((String)ht.get("size")))/1024);

                           java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                                                            "yyyy-MM-dd HH:mm:ss");
                           java.util.Date dt = null;
                           dt = sdf.parse(ht.get("date").toString());
                           java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat(
                                                               "M-d-yyyy h:mm a");

                           String size="";
                           if(intSize>=1)
                               size = intSize+" KB";
                           else
                               size =  ht.get("size") +" Bytes";
                           String Author = com.krawler.esp.handlers.FileHandler.getAuthor(request.getParameter("url"),1);
                           com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject();
                           jobj.put("date",sdf1.format(dt));
                           jobj.put("author", Author);
                           jobj.put("size", com.krawler.esp.handlers.FileHandler.getSizeKb((String)ht.get("size"))+" Kb");
                           jobj.put("comment", java.net.URLEncoder.encode(ht.get("comments").toString(),"UTF-8"));
                           jobj.put("svnname", ht.get("svnname"));
                           jobj.put("docname", ht.get("docname"));
                           jobj.put("version", ht.get("version"));
                           jbj.put("data",jobj.toString());
                  }
               else{
                  jbj.put("valid", false);    
               } 
%>
<%=jbj.toString()%>
<%
 } catch (JSONException jex) {
	 out.println("{}");
 }
%>
