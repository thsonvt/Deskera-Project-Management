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
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="com.krawler.esp.database.dbcon"
        import="com.krawler.utils.json.base.*"%>
<%@ page import="com.krawler.esp.handlers.AuthHandler"%>
<%@ page import="java.util.*"%>
<%@ page import="com.krawler.esp.handlers.ServerEventManager"%>
<%@ page import="com.krawler.common.util.URLUtil"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler"/>
<%
            String user = request.getParameter("u");
            String pass = request.getParameter("p");
            String demo = request.getParameter("demo");
            String subdomain = URLUtil.getDomainName2(request);
            JSONObject jobj = dbcon.AuthUser(user, pass, subdomain);
            if(demo != null && demo.equals("true")){
                jobj.put("demo", true);
            }
            if (jobj.has("success") && (jobj.get("success").equals(true))) {
                sessionbean.createUserSession(request, response, jobj.getString("username"), jobj.getString("lid"), 
                              jobj.getString("companyid"),jobj.getString("company"));
                sessionbean.setLocale(request, response, jobj.optString("language", null));
%>      
<%=jobj.toString()%>
<%
                try {
                    Object[] remoteUserIds = dbcon.getFriendListArray(jobj.getString("lid"));
                    String sid = jobj.getString("lid");
                    for (int i = 0; i < remoteUserIds.length; i++) {
                        String contactUserId = remoteUserIds[i].toString();
                        JSONObject jpub = new JSONObject();

                        jpub.append("data",
                                "{'data':[{mode :'online'},{status:'request',userid:'" + sid + "'}]}");
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("data", jpub.toString());

                        ServerEventManager.publish("/" + contactUserId + "/chat", data, this.getServletContext());
                    }
                } catch (Exception e) {

                }
            } else {
                JSONObject j = new JSONObject();
                if(jobj.has("message"))
                    j.put("message", jobj.get("message"));
                else
                    j.put("message", "Authentication failed");
                j.put("success", false);
%>
<%=j.toString()%>
<%
            }
%>
