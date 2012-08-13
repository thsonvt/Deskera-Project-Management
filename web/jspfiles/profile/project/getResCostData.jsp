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
<%@page import ="java.util.*,
        java.sql.*,
        javax.servlet.ServletRequest,
        com.krawler.utils.json.base.*,
        com.krawler.esp.database.*" %>
<%@page import="com.krawler.esp.handlers.projectReport"%>
<%@ page import="com.krawler.common.session.SessionExpiredException"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%

            String returnStr = null;
            String Str="";
            try{
                boolean isValidSession = sessionbean.isValidSession(request, response);
                returnStr = projectReport.getProjectReport1Json(request, true, isValidSession);
                JSONObject jobj = new JSONObject(returnStr);
                JSONArray jarr = jobj.getJSONArray("data");
                if( jobj.getJSONArray("data").length()!=0){
                    if(request.getParameter("reporttype").equals("costline")){
                        for(int i=0;i < jobj.getJSONArray("data").length() ; i++){
                                JSONObject temp = jarr.getJSONObject(i);
                                String pName = temp.getString("name");
                                if(pName.length() > 6){
                                    pName = pName.substring(0, 6) + "..";
                                }
                                String cost=temp.getString("cost");
                                Str += pName + ";" +cost+ "\n";
                         }
                    }
                    else{
                        for(int i=0;i < jobj.getJSONArray("data").length() ; i++){
                                JSONObject temp = jarr.getJSONObject(i);
                                String pName = temp.getString("name");
                                if(pName.length() > 6){
                                    pName = pName.substring(0, 6) + "..";
                                }
                                String time=temp.getString("time");
                                Str += pName + ";" +time+ "\n";
                         }
                        }
                }

                out.println(Str);
            }catch(Exception e){
            }
%>
