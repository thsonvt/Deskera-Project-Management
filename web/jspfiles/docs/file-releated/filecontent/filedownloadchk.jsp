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
<%@page import="java.util.Hashtable,
                 com.krawler.svnwebclient.configuration.ConfigurationProvider,
                 org.tmatesoft.svn.core.internal.wc.SVNFileUtil,
                 java.io.File" %>  
<%@page import="com.krawler.esp.handlers.AuthHandler"%>
<%@page import="com.krawler.utils.json.base.JSONObject"%>
<%@page import="com.krawler.utils.json.base.JSONException"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler"/>        
<%
JSONObject jbj = new JSONObject();
try {
		
		if (sessionbean.validateSession(request, response)) {
			jbj.put("valid", true);
                        Hashtable ht = com.krawler.esp.database.dbcon.getfileinfo(request.getParameter("docid"));    
                        //File fp = new File( com.krawler.esp.handlers.StorageHandler.GetDocStorePath(ht.get("storeindex").toString()) + "/56a08913-f7df-4409-93fe-ba5c8f31f6fe/"+ht.get("svnname"));
                            File fp = new File( com.krawler.esp.handlers.StorageHandler.GetDocStorePath(ht.get("storeindex").toString()) + "/"+ht.get("userid").toString()+"/"+ht.get("svnname"));
                            String mimetype  = SVNFileUtil.detectMimeType(fp);
                            boolean isexe  = SVNFileUtil.isExecutable(fp);
                            if(isexe||mimetype!=null)
                            {
                                String doctype = (String)ht.get("doctype");
                                JSONObject jobj = new JSONObject();
                                if(doctype.compareTo("Image")==0)
                                    jobj.put("download", "no");
                                else
                                    jobj.put("download", "yes");
                                jbj.put("data",jobj.toString());
                            }else{
                                JSONObject jobj = new JSONObject();
                                jobj.put("download", "no");
                                jbj.put("data",jobj.toString());
                            }
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
