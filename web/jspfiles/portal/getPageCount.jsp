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
<%@ page import="com.krawler.esp.portalmsg.*" %>
<%@ page import="com.krawler.utils.json.base.JSONObject"%>
<%@ page import="com.krawler.utils.json.base.JSONException"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%
	try {
		JSONObject jbj = new JSONObject();
		if (sessionbean.validateSession(request, response)) {
			jbj.put("valid", true);
                        String flag = request.getParameter("flag");        
                        if(flag.compareTo("savefolder")==0)
                        {                        
                            String loginid = request.getParameter("loginid");
                            String foldername = request.getParameter("foldername");
                            jbj.put("data", forummsgdbcon.getFolderidForMailuser(loginid, foldername));
                        }
                        else if(flag.compareTo("editfolder")==0)
                        {                                    
                            String foldername = request.getParameter("foldername");
                            String loginid = request.getParameter("loginid");
                            String folderid = request.getParameter("folderid");
                            jbj.put("data", forummsgdbcon.UpdateFoldernameForMailuser(folderid, foldername, loginid));
                        }
                        else if(flag.compareTo("deletefolder")==0)
                        {                                                
                            String folderid = request.getParameter("folderid");
                            jbj.put("data", forummsgdbcon.DeleteFoldernameForMailuser(folderid));
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
