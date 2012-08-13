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
<%@page import="com.krawler.esp.handlers.CompanyHandler"%>
<%@ page contentType="text/html"%>
<%@ page pageEncoding="UTF-8"%>
<%@ page language="java"%>
<%@ page import="java.util.*"%>
<%@ page import="com.krawler.esp.database.*"%>
<%@ page import="com.krawler.esp.handlers.ServerEventManager"%>
<%@ page import="com.krawler.utils.json.base.JSONObject"%>
<%@ page import="com.krawler.utils.json.base.JSONArray"%>
<%@ page import="com.krawler.esp.handlers.AuthHandler"%>
<%@ page import="com.krawler.utils.json.base.JSONException"%>
<%@ page import="com.krawler.common.session.SessionExpiredException"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%
            if (sessionbean.validateSession(request, response)) {
            int type = Integer.parseInt(request.getParameter("type"));
            switch (type) {
                case 0:
                    try {
                        JSONObject jbj = null;
                        try {
                            jbj = new JSONObject();
                            String sid = null, rsflagstr = null, m = null, rid = null, sname = null, mode = null;
                            mode = request.getParameter("mode");
                            sid = AuthHandler.getUserid(request);       //Caller's UserId
                            sname = request.getParameter("user");
                            rid = request.getParameter("rUserId");
                            m = request.getParameter("chatMessage");
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            java.util.Date dt = new Date();
                            String tempdt=sdf.format(dt);
                            String time=dbcon.toCompanyTimezone(tempdt, AuthHandler.getCompanyid(request));
                            jbj.put("valid", true);
                            jbj.put("data", "");
                            JSONObject jpub = new JSONObject();
                            jpub.put("message", m);
                            jpub.put("mode",mode);
                            jpub.put("timestamp", time);
                            jpub.put("id",sid);
                            jpub.put("sname",sname);
                            jpub.put("uname", AuthHandler.getUserName(request));
                            jpub.append("data", "{'data':["+jpub.toString()+" ]}");
                            Map<String, String> data = new HashMap<String, String>();
                            data.put("data", jpub.toString());
                            ServerEventManager.publish("/" + rid + "/chat", data, this.getServletContext());
                        } catch (JSONException jex) {
                            out.println("{}");
                        }
%>
<%=jbj.toString()%>
<%
                        } catch (SessionExpiredException sex) {
                            out.println("{\"valid\":false}");
                        }
                        break;
            }

            } else {
                out.println("{\"valid\":false}");
            }
%>
