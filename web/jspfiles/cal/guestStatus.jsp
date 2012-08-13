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
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import ="java.util.*"%>
<%@page import ="java.text.*"%>
<%@page import ="java.sql.*"%>
<%@page import="com.krawler.utils.json.*" %>
<%@page import="com.krawler.esp.database.*" %>
<%@page import ="com.krawler.esp.handlers.*"%>
<%@page import="com.krawler.esp.handlers.AuthHandler"%>
<%@ page import="com.krawler.database.DbPool"%>
<%@ page import="com.krawler.database.DbPool.Connection"%>
<%@ page import="com.krawler.common.service.ServiceException"%>

<%
    String eventid =request.getParameter("eid");
    String userid =request.getParameter("userid");
    String loginid = AuthHandler.getUserid(request);
    String companyid = AuthHandler.getCompanyid(request);
    com.krawler.database.DbPool.Connection conn = null;
    try {
            conn = DbPool.getConnection();
            Object isPresent = calEvent.getResponseEvent(conn, eventid,userid);
            if(userid.compareTo(loginid)==0){
                String str = isPresent.toString();
                if(str.compareTo("{data:{}}") != 0){
                    com.krawler.utils.json.base.JSONObject isCheck = new com.krawler.utils.json.base.JSONObject(str);
                    String statuscheck = isCheck.getJSONArray("data").getJSONObject(0).getString("status");
                    if(statuscheck.equals("p")){
                        String responseText = request.getParameter("response");
                        if(responseText.equals("1")){
                            calEvent.updateResponse(conn, eventid,userid,"a");
                            Object getDefaultCal = calEvent.getDefaultCalendar(conn,userid,1);
                            Object getEventDetails = calEvent.getEventDetails(conn, eventid);
                            String eventDetails = getEventDetails.toString();
                            String checkCal = getDefaultCal.toString();
                            if(checkCal.compareTo("{data:{}}") != 0){
                                com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject(checkCal);
                                String cid = jobj.getJSONArray("data").getJSONObject(0).getString("cid");
                                if(eventDetails.compareTo("{data:{}}") != 0){
                                    com.krawler.utils.json.base.JSONObject jobjEvent = new com.krawler.utils.json.base.JSONObject(eventDetails);
                                    String startts = jobjEvent.getJSONArray("data").getJSONObject(0).getString("startts");
                                    String endts = jobjEvent.getJSONArray("data").getJSONObject(0).getString("endts");
                                    String subject = jobjEvent.getJSONArray("data").getJSONObject(0).getString("subject");
                                    String descr = jobjEvent.getJSONArray("data").getJSONObject(0).getString("descr");
                                    String location = jobjEvent.getJSONArray("data").getJSONObject(0).getString("location");
                                    String showas = jobjEvent.getJSONArray("data").getJSONObject(0).getString("showas");
                                    String priority = jobjEvent.getJSONArray("data").getJSONObject(0).getString("priority");
                                    String recpattern = jobjEvent.getJSONArray("data").getJSONObject(0).getString("recpattern");
                                    String recend = jobjEvent.getJSONArray("data").getJSONObject(0).getString("recend");
                                    String resources = jobjEvent.getJSONArray("data").getJSONObject(0).getString("resources");
                                    String resCount =calEvent.insertEvent(conn, cid, startts, endts, subject, descr, location, showas, priority, recpattern, recend, resources, loginid, companyid, "");
                                    if(!resCount.equals("0")){
                                        Object returnStr = calEvent.selectReminder(conn, eventid);
                                        String returnString = returnStr.toString();
                                         if (returnString.compareTo("{data:{}}") != 0) {
                                            com.krawler.utils.json.base.JSONObject jobj1 = new com.krawler.utils.json.base.JSONObject(returnString);
                                            for(int i=0;i<jobj1.getJSONArray("data").length();i++){
                                                calEvent.insertReminder(conn, resCount,jobj1.getJSONArray("data").getJSONObject(i).getString("rtype"),Integer.parseInt(jobj1.getJSONArray("data").getJSONObject(i).getString("rtime")));
                                            }
                                        }

                                        com.krawler.utils.json.base.JSONObject jobjput = new com.krawler.utils.json.base.JSONObject();
                                        com.krawler.utils.json.base.JSONObject jobj1 = new com.krawler.utils.json.base.JSONObject();
                                        jobjput.put("eid",resCount);
                                        jobjput.put("cid",cid);
                                        jobjput.put("startts",startts);
                                        jobjput.put("endts",endts);
                                        jobjput.put("subject",subject);
                                        jobjput.put("descr",descr);
                                        jobjput.put("location",location);
                                        jobjput.put("showas",showas);
                                        jobjput.put("priority",priority);
                                        jobjput.put("recpattern",recpattern);
                                        jobjput.put("recend",recend);
                                        jobjput.put("resources",resources);
                                        jobj1.append("data",jobjput);

                                        Map<String, String> data = new HashMap<String, String>();
                                        data.put("action", "1");
                                        data.put("calView", "0");
                                        data.put("success","true");
                                        data.put("data", jobj1.toString());
                                        ServerEventManager.publish("/calEvent/"+cid, data, this.getServletContext());
                                        out.print("{\"success\":\"true\"}");
                                    }else
                                        out.print("{}\"success\":\"false\"}");
                                }
                            }
                        }else
                            if(responseText.equals("0")){
                                 calEvent.updateResponse(conn, eventid,userid,"r");
                            }
                    }
                    else{
                        out.print("{\"success\":\"Invalid\"}");
                    }
                }else{
                    out.print("{\"success\":\"deleted\"}");
                }
            }else{
                out.print("{\"success\":\"Invalid\"}");
            }
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            System.out.println("calendar guest status :Connection Error:"+ex.toString());
        } catch(Exception e){
            DbPool.quietRollback(conn);
            System.out.println("calendar guest status error:"+e.toString());
        } finally {
            DbPool.quietClose(conn);
        }
 %>
