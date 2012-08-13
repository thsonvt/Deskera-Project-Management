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
<%@ page import="java.util.*" %>
<%@ page import="com.krawler.esp.database.*" %>
<%@ page import="com.krawler.esp.handlers.AuthHandler"%>
<%@ page import="com.krawler.esp.handlers.DashboardHandler"%>
<%@page import="com.krawler.utils.json.base.JSONObject"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%
            if (sessionbean.validateSession(request, response)) {
                try {
                    String userId = AuthHandler.getUserid(request);
                    Date dt1=new Date();
                    Date dt2=new Date();
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-d HH:mm:ss");                                       
                    java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("h:mm a");                                                           
                    
                    Calendar cal = Calendar.getInstance();
                    Calendar stCal=Calendar.getInstance();                    
                    cal.setTime(dt1);
                    cal.add(Calendar.HOUR, 24);
                    dt2=cal.getTime();
                    cal.setTime(dt1);                    
                    
                    String res= dbcon.getUserEvents(userId,sdf.format(dt1),sdf.format(dt2));
                    JSONObject resjson = new JSONObject();                                                                        
                    JSONObject recordObj = new JSONObject();                    
                    if(res.compareTo("{data:{}}") != 0) {
                        JSONObject jobj = new JSONObject(res);                            
                        for (int i = 0; i < jobj.getJSONArray("data").length(); i++) {
                            recordObj = new JSONObject();                                                        
                            String reqStr=null;
                            String timeStr=null;
                                                       
                            String calId=jobj.getJSONArray("data").getJSONObject(i).getString("cid");
                            String startts=jobj.getJSONArray("data").getJSONObject(i).getString("startts");
                            String subject=jobj.getJSONArray("data").getJSONObject(i).getString("subject");                            
                            String projId=jobj.getJSONArray("data").getJSONObject(i).getString("projId");
                            String projName=jobj.getJSONArray("data").getJSONObject(i).getString("projName");
                            
                            Date stdt=new Date();
                            stdt=sdf.parse(startts);
                            stCal.setTime(stdt);
                            
                            if(cal.get(Calendar.DAY_OF_MONTH) != stCal.get(Calendar.DAY_OF_MONTH)){
                                timeStr="Tommorow, "+sdf1.format(stdt);
                            }
                            else
                                timeStr=sdf1.format(stdt);

                            if(projId.equals("") == false){
                                reqStr = String.format(DashboardHandler.userEvtFormat, new Object[] {
                                                            timeStr, "x", calId,
                                                            projId, java.net.URLEncoder.encode(projName, "UTF-8"), subject});
                            }
                            /*else{
                                reqStr = String.format(DashboardHandler.userEvtFormat, new Object[] {
                                                            timeStr, "y", calId,
                                                            calId, java.net.URLEncoder.encode(subject, "UTF-8"), subject});
                            }*/
                            recordObj.put("name",reqStr);
                            resjson.append("data", recordObj);                            
                        }
                        out.println(resjson);                                                                    
                    }
                    else{
                        out.println("{\"data\": []}");                        
                    }
                } catch (Exception se) {
                    out.println("{\"data\": []}");
                }
            } else {
                out.println("{\"data\": []}");
            }
%>
