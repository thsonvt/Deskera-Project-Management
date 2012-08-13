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
<%@ page import="com.krawler.esp.database.*" %>
<%@ page import="com.krawler.esp.handlers.AuthHandler"%>
<%@ page import="com.krawler.common.session.SessionExpiredException"%>
<%@ page import="com.krawler.database.DbPool"%>
<%@ page import="com.krawler.database.DbPool.Connection"%>
<%@page import="com.krawler.utils.json.base.JSONException"%>
<%@ page import="com.krawler.common.service.ServiceException"%>
<%@ page import="com.krawler.esp.handlers.*"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%
        com.krawler.database.DbPool.Connection conn = null;
	if (sessionbean.validateSession(request, response)) {
		try {
                        conn = DbPool.getConnection();
                        String ulogin = AuthHandler.getUserid(request);
                        String reqid = ProfileHandler.getUserRequest(conn, ulogin);
                        //String str = reqid.toString();
                        //com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject(str);
                        if(reqid.compareTo("{data:{}}") != 0){
                            com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject(reqid);
                            String[] uids = new String[jobj.getJSONArray("data").length()]; 
                            for(int i=0;i<jobj.getJSONArray("data").length();i++){
                                uids[i] = jobj.getJSONArray("data").getJSONObject(i).getString("userid1");
                            }
                            String tp = FileHandler.getRequestAuthor(conn, uids);
                            com.krawler.utils.json.base.JSONObject getput = new com.krawler.utils.json.base.JSONObject();
                            com.krawler.utils.json.base.JSONObject getInfo = new com.krawler.utils.json.base.JSONObject();                                         
                            com.krawler.utils.json.base.JSONObject jobj1 = new com.krawler.utils.json.base.JSONObject(tp);
                            for(int i=0;i<jobj1.getJSONArray("data").length();i++){
                                getput.put("to",ulogin);
                                getput.put("fname",jobj1.getJSONArray("data").getJSONObject(i).getString("fname"));
                                getput.put("lname",jobj1.getJSONArray("data").getJSONObject(i).getString("lname"));
                                getput.put("from",jobj1.getJSONArray("data").getJSONObject(i).getString("userid"));
                            }
                            getInfo.append("data",getput);
                            out.println(getInfo.toString());
                        }else{
                            out.println("{\"data\": []}");
                        }
                    conn.commit();   
                } catch (SessionExpiredException se) {
                    DbPool.quietRollback(conn);
                    out.println("{\"data\": []}");
		} catch (ServiceException ex) {
                    DbPool.quietRollback(conn);
                    out.println("{\"data\": []}");
                } catch (JSONException jex) {
                    DbPool.quietRollback(conn);
                    out.println("{\"data\": []}");
                } finally {
                    DbPool.quietClose(conn);
                }
	} else {
		out.println("{\"data\": []}");
	}
%>
