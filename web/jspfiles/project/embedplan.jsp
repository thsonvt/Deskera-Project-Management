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
<%@page import ="com.krawler.esp.database.*" %>
<%@ page import="com.krawler.database.DbPool"%>
<%@ page import="com.krawler.database.DbPool.Connection"%>
<%@ page import="com.krawler.common.service.ServiceException"%>
<%@page import="com.krawler.utils.json.base.JSONException"%>
<%@page import="com.krawler.esp.handlers.projdb"%>
<%@page import="com.krawler.esp.handlers.StorageHandler"%>
<%@page import="com.krawler.esp.handlers.dateFormatHandlers"%>
<%@page import="com.krawler.esp.handlers.CompanyHandler"%>
<%@page import="com.krawler.svnwebclient.configuration.ConfigurationException"%>
<%@page import="java.io.File"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="com.krawler.common.util.URLUtil"%>
<%@page import="java.io.FileInputStream"%>
<%@page import="java.io.OutputStream"%>
<%
        String returnStr = null;
        com.krawler.database.DbPool.Connection conn = null;
	try {
            conn = DbPool.getConnection();
            int action = Integer.parseInt(request.getParameter("action"));
            switch(action){
                case 1 : 
                        int offset = Integer.parseInt(request.getParameter("start"));
                        int limit =Integer.parseInt(request.getParameter("limit")); 
                        returnStr = projdb.getTask(conn, request.getParameter("projectid"),offset,limit);
                        if (returnStr.compareTo("{data:{}}") != 0) {
                            com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject(returnStr);
                            com.krawler.utils.json.base.JSONArray jArray = jobj.getJSONArray("data");
                            for (int i = 0; i < jArray.length(); i++) {
                                String sdtStr = jArray.getJSONObject(i).getString("startdate");
                                String edtStr = jArray.getJSONObject(i).getString("enddate");
                                String actsdt = jArray.getJSONObject(i).getString("actualstartdate");

                                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-d HH:mm:ss");
                                java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-d");

                                if (sdtStr.compareTo("") != 0) {
                                    java.util.Date dt = sdf.parse(sdtStr);
                                    sdtStr = sdf1.format(dt);
                                    jArray.getJSONObject(i).remove("startdate");
                                    jArray.getJSONObject(i).put("startdate", sdtStr);
                                }
                                if (edtStr.compareTo("") != 0) {
                                    java.util.Date dt = sdf.parse(edtStr);
                                    edtStr = sdf1.format(dt);
                                    jArray.getJSONObject(i).remove("enddate");
                                    jArray.getJSONObject(i).put("enddate", edtStr);
                                }
                                if (actsdt.compareTo("") != 0) {
                                    java.util.Date dt = sdf.parse(actsdt);
                                    actsdt = sdf1.format(dt);
                                    jArray.getJSONObject(i).remove("actualstartdate");
                                    jArray.getJSONObject(i).put("actstartdate", actsdt);
                                }
                                String predecessor = projdb.getPredecessor(conn, jArray.getJSONObject(i).getString("taskid"));
                                jArray.getJSONObject(i).put("predecessor", predecessor);

                                String resources = projdb.getTaskResourcesNames(conn, jArray.getJSONObject(i).getString("taskid"),request.getParameter("projectid"));
                                jArray.getJSONObject(i).put("resourcename", resources);
                            }

                            returnStr = jobj.toString();
                        }
                        break;
                 case 2 : 
                        String projid = request.getParameter("projectid");
                        String companyid = CompanyHandler.getCompanyIDFromProject(conn, projid);
                        String minmaxProjectDate = projdb.getminmaxProjectDate(conn, projid, companyid);
                        String minDate,maxDate;
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-d HH:mm:ss");
                        java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-d"); 
                        com.krawler.utils.json.base.JSONObject datesObj = new com.krawler.utils.json.base.JSONObject();
                        if(minmaxProjectDate !=null)
                        {   

                            String[] dates = minmaxProjectDate.split(","); 
                            java.util.Date dt = sdf.parse(dates[0]);
                            minDate = sdf1.format(dt);
                            dt = sdf.parse(dates[1]);
                            maxDate = sdf1.format(dt);
                        }
                        else {
                           minDate = sdf1.format(new java.util.Date());
                           maxDate = sdf1.format(new java.util.Date()); 
                        }
                        datesObj.put("mindate", minDate);
                        datesObj.put("maxdate", maxDate);
                        String companyHolidays = projdb.getNWWeekAndCompHolidays(conn, projid);
                        String name = projdb.getProjectName(conn,projid);
                        String df = "";
                        if(!StringUtil.isNullOrEmpty(request.getParameter("df"))){
                            int dfid = Integer.parseInt(request.getParameter("df"));
                            df = dateFormatHandlers.getScriptDateFormat(conn, dfid);
                        }
                        returnStr = "[{data:"+datesObj.toString()+"},{"+companyHolidays.substring(1)+",{name :\""+name+"\"}"+",{df :\""+df+"\"}]";
                        break;
                 case 3 :
                        String pid = request.getParameter("projid");
                        returnStr = projdb.getProjectResources(conn,pid);
                        break;
            }
    } catch (ServiceException ex) {
          DbPool.quietRollback(conn);
          out.println("{\"data\": []}");
    } catch (JSONException jex) {
          DbPool.quietRollback(conn);
          out.println("{\"data\": []}");
    } finally {
          DbPool.quietClose(conn);
          out.print(returnStr);
    }
%>
