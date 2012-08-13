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
<%@ page import="com.krawler.esp.database.*" %>
<%@ page import="com.krawler.utils.json.base.JSONObject"%>
<%@ page import="com.krawler.esp.handlers.ServerEventManager"%>
<%@ page import="com.krawler.esp.handlers.AuthHandler"%>
<%@ page import="com.krawler.common.session.SessionExpiredException"%>
<%@ page import="java.util.HashMap"%>
<%@ page import="java.util.Map"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%
	if (sessionbean.validateSession(request, response)) {
     
        String respStr = "";
        JSONObject j = new JSONObject();
        String mode = request.getParameter("mode");
        String loginid = request.getParameter("login");
        if (mode.equals("1")) {
            respStr  = dbcon.getFriendlistDetail(AuthHandler.getUserid(request));
            
	} 
        else if (mode.equals("2")) { // send online responce
                String userid = request.getParameter("userid");
                String remoteUser = request.getParameter("remoteUser");
                JSONObject jpub = new JSONObject();
                jpub.append("data",
                                "{'data':[{mode:'online'},{status:'response',userid:'"
                                                + userid + "'}]}");
                Map<String, String> data = new HashMap<String, String>();
                data.put("data", jpub.toString());
                ServerEventManager.publish("/" + remoteUser + "/chat",
                                data, this.getServletContext());
        }
        else if (mode.equals("3")) {
                try {
                        String[] remoteUserIds = request.getParameter(
                                        "remoteUser").split(",");
                        String sid = AuthHandler.getUserid(request);
                        for (int i = 0; i < remoteUserIds.length; i++) {
                                String contactUserId = remoteUserIds[i];
                                JSONObject jpub = new JSONObject();

                                jpub.append("data",
                                                "{'data':[{mode :'online'},{status:'request',userid:'"
                                                                + sid + "'}]}");
                                Map<String, String> data = new HashMap<String, String>();
                                data.put("data", jpub.toString());

                                ServerEventManager.publish("/" + contactUserId
                                                + "/chat", data, this
                                                .getServletContext());
                        }
                } catch (Exception e) {

                }
        }
        else if (mode.equals("4")) {
            String responseString = dbcon
                            .getFriendlistDetails(AuthHandler
                                            .getUserid(request));
            try {
                    if (responseString.compareTo("{data:{}}") != 0) {
                            com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject(
                                            responseString);
                            String sid = AuthHandler.getUserid(request);
                            for (int i = 0; i < jobj.getJSONArray("data")
                                            .length(); i++) {
                                    String contactUserId = jobj.getJSONArray(
                                                    "data").getJSONObject(i).getString(
                                                    "userid");
                                    JSONObject jpub = new JSONObject();

                                    jpub.append("data",
                                                    "{'data':[{mode :'offline'},{status:'request',userid:'"
                                                                    + sid + "'}]}");
                                    Map<String, String> data = new HashMap<String, String>();
                                    data.put("data", jpub.toString());

                                    ServerEventManager.publish("/"
                                                    + contactUserId + "/chat", data,
                                                    this.getServletContext());
                            }
                    }
            } catch (Exception e) {

            }
    }else if (mode.equals("5")) {//publish my activity (deleted user from my contactlist)
				String userid = request.getParameter("userid");
				String remoteUser = request.getParameter("remoteUser");
				JSONObject jpub = new JSONObject();
				jpub.append("data",
						"{'data':[{mode:'delete'},{status:'request',userid:'"
								+ userid + "'}]}");
				Map<String, String> data = new HashMap<String, String>();
				data.put("data", jpub.toString());

				ServerEventManager.publish("/" + remoteUser + "/chat",
						data, this.getServletContext());
			} 
        else if (mode.equals("6")) { //publish my activity (added user to my contactlist)
				String userid = request.getParameter("userid");
				String remoteUser = request
						.getParameter("remoteUser");
				JSONObject jpub = new JSONObject();
                                String userName ="";
				userName = com.krawler.esp.handlers.MessengerHandler.getUserName(request
						.getParameter("userid"));
				jpub.append("data",
						"{'data':[{ mode :'add'},{ status :'request',userid:'"
								+ userid + "',username:'" + userName
								+ "'}]}");
				Map<String, String> data = new HashMap<String, String>();
				data.put("data", jpub.toString());

				ServerEventManager.publish("/" + remoteUser + "/chat",
						data, this.getServletContext());
			}
        else if (mode.equals("7")) { //publish my activity (invite remoteuser)
				String userid = request.getParameter("userid");
				String remoteUser = request
						.getParameter("remoteUser");
				String userName = " [Incoming Request]";
				userName = com.krawler.esp.handlers.MessengerHandler.getUserName(request
						.getParameter("userid"))
						+ userName;
				JSONObject jpub = new JSONObject();
				jpub.append("data",
						"{'data':[{ mode :'invite'},{mode:'request',userid:'"
								+ userid + "',username:'" + userName
								+ "'}]}");
				Map<String, String> data = new HashMap<String, String>();
				data.put("data", jpub.toString());

				ServerEventManager.publish("/" + remoteUser + "/chat",
						data, this.getServletContext());
			}
        
        j.put("valid", true);
        j.put("data", respStr);
        out.println(j.toString());
     
    } else {
            out.println("{\"valid\": false}");
    }
%> 
