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
<%@ page language="java" %>
<%@ page import="java.util.*"%>
<%@ page  import="com.krawler.esp.database.*"%>  
<%@ page import="com.krawler.esp.handlers.ServerEventManager"%>
<%@ page import="com.krawler.utils.json.base.JSONObject"%>
<%@page import="com.krawler.esp.handlers.AuthHandler"%>
<%@page import="com.krawler.utils.json.base.JSONException"%>
<%@ page import="com.krawler.common.session.SessionExpiredException"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%
            if (sessionbean.validateSession(request, response)) {
                try {
                    JSONObject jbj = null;
                    try {
                        jbj = new JSONObject();

                        String sid = null, rsflagstr = null, m = null, rid = null;
                        int rsflagint, rdflag;

                        sid = AuthHandler.getUserid(request);

                        rid = request.getParameter("remoteUser");

                        m = request.getParameter("chatMessage");
                        String rUserName = request.getParameter("rUserName");
                        rsflagstr = request.getParameter("rstatus");

                        if (rsflagstr.compareTo("offline") == 0 || rsflagstr.compareTo("Offline") == 0) {
                            rsflagint = 0;
                        } else {
                            rsflagint = 1;
                        }

                        if (rsflagint == 0) {
                            rdflag = 0;
                        } else {
                            rdflag = 999;
                        }
                        jbj.put("valid", true);
                        jbj.put("data", dbcon.insertChatmessages(sid, rid, m, rsflagint, rdflag));

                        JSONObject jpub = new JSONObject();
                        jpub.append("data", "{'data':[{message:'" + m + "',id:'" + sid + "',uname:'" + rUserName + "'}]}");
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("data", jpub.toString());
                        ServerEventManager.publish("/" + rid + "/" + sid + "/chat", data, this.getServletContext());
                    } catch (JSONException jex) {
                        out.println("{}");
                    }

%>
<%=jbj.toString()%>
<%

                } catch (SessionExpiredException sex) {
                    out.println("{\"valid\":false}");
                }
            } else {
                out.println("{\"valid\":false}");
            }
%> 
