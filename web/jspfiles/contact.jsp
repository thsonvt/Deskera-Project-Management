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
<%@ page language="java" %>
<%@ page import="com.krawler.common.session.SessionExpiredException"%>
<%@ page import="com.krawler.common.util.StringUtil" %>
<%@ page import="com.krawler.esp.database.*" %>
<%@ page import="com.krawler.esp.handlers.AuthHandler"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%
	if (sessionbean.validateSession(request, response)) {
		try {
                    
                        String type = request.getParameter("type");
                        String result = "";
                        if(type.equals("mycontacts")){
                            int limit=Integer.parseInt(request.getParameter("limit"));
                            int start=Integer.parseInt(request.getParameter("start"));
                            String userid = AuthHandler.getUserid(request);
                            result = dbcon.getMyContacts(userid,limit,start,request.getParameter("ss"));
                        }
                        else if(type.equals("allcontacts")){
                            String userid = AuthHandler.getUserid(request);
                            result = dbcon.getAllContacts(userid);
                        }
                        else if(type.equals("newcontacts")){
                            int limit=Integer.parseInt(request.getParameter("limit"));
                            int start=Integer.parseInt(request.getParameter("start"));
                            String userid = AuthHandler.getUserid(request);
                            String comid = AuthHandler.getCompanyid(request);
                            String ss = "";
                            if (request.getParameter("ss") != null) { 
                                ss = request.getParameter("ss");
                            }	
                            result =  dbcon.getNewContacts(userid,comid,limit,start, request.getParameter("ss"));
                        }
                        else if(type.equals("addContact")){
                            String userid =  AuthHandler.getUserid(request);
                            String requestTo = request.getParameter("requestto");
                            result = dbcon.addContacts(userid,requestTo);
                        }
                         else if(type.equals("acceptContact")){
                            String userid = request.getParameter("userid");
                            String requestTo = request.getParameter("requestto");
                            result = dbcon.acceptContacts(userid,requestTo);
                        }
                        else if(type.equals("deleteContact")){
                            String userid = request.getParameter("userid");
                            String requestTo = request.getParameter("requestto");
                            result = dbcon.deleteContacts(userid,requestTo);
                        }
                        else if(type.equals("repContact")){
                            String userMap = request.getParameter("val");
                            result = dbcon.repContact(userMap);
                        }
                        else if(type.equals("newAddress")){
                            String userid = request.getParameter("userid");
                            String username = request.getParameter("username");
                            String emailid = request.getParameter("emailid");
                            String address = request.getParameter("address");
                            String contactno = request.getParameter("contactno");
                            result = dbcon.newAddress(userid,username,emailid,address,contactno);
                        }
			com.krawler.utils.json.base.JSONObject jbj = new com.krawler.utils.json.base.JSONObject();
                        jbj.put("valid", "true");
                        jbj.put("data", result);
                        out.print(jbj.toString());
		} catch (SessionExpiredException sex) {
			out.println("{\"valid\": false}");
		}
	} else {
		out.println("{\"valid\": false}");
	}
%>
