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
<%@page import ="java.util.*"%>
<%@page import = "java.sql.*"%>
<%@page import = "java.lang.*"%>
<%@page import = "javax.servlet.ServletRequest"%>
<%@ page import="com.krawler.utils.json.*" %>
<%@ page import="com.krawler.esp.database.*" %>
<%@page import ="com.krawler.esp.handlers.*"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="com.krawler.common.util.KWLErrorMsgs"%>
<%@page import="com.krawler.utils.json.base.JSONObject"%>
<%@page import="com.krawler.esp.handlers.AuthHandler"%>
<%@page import="com.krawler.utils.json.base.JSONException"%>
<%@ page import="com.krawler.database.DbPool"%>
<%@ page import="com.krawler.database.DbPool.Connection"%>
<%@ page import="com.krawler.common.service.ServiceException"%>
<%@page import="com.krawler.esp.servlets.ChartDataServlet"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%
	com.krawler.database.DbPool.Connection conn = null;
	try {
                conn = DbPool.getConnection();
		JSONObject jbj = new JSONObject();
		if (sessionbean.validateSession(request, response)) {
			jbj.put("valid", true);
                         
                        ResultSet res = null;
                        String tid = null;
                        String userId = request.getParameter("userid");
                        String action = request.getParameter("action");
                        String projId = request.getParameter("projectid");
                        String rowindex = request.getParameter("rowindex");
                        String loginid = AuthHandler.getUserid(request);
                        String userFullName = AuthHandler.getAuthor(conn, loginid);
                        String userName = AuthHandler.getUserName(request);
                        String projName = projdb.getProjectName(conn, projId);
                        String companyid = AuthHandler.getCompanyid(request);
                        String ipAddress = AuthHandler.getIPAddress(request);
                        int auditMode = 0;
                        String[] pdates;
                        String returnStr =null;
                        if (action.compareTo("update") == 0) {
                            try {
                                tid = request.getParameter("taskid");
                                String jsonstr = request.getParameter("val");
                                com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject(jsonstr);
                                if (jobj.getString("taskid").equals("0")) {
                                    action = "insert";
                                    UUID ud = new UUID(2312, 4123);
                                    tid = ud.randomUUID().toString();
                                    jobj.remove("taskid");
                                    jobj.put("taskid", tid);
                                    jobj.put("loginid", loginid);
                                    pdates = projdb.InsertTask(conn, jobj, tid, projId, rowindex).split(",");
                                    jobj.remove("startdate");
                                    jobj.put("startdate", pdates[0]);
                                    jobj.remove("enddate");
                                    jobj.put("enddate", pdates[1]);
                                    jobj.remove("actstartdate");
                                    jobj.put("actstartdate", pdates[2]);
                                    //projdb.insertBuffer(conn, jobj.toString(), projId, tid, action, Integer.parseInt(rowindex),userId);
                                    String usrid=AuthHandler.getUserid(request);
                                  //  LogHandler.InsertLogForProject(conn, usrid, usrid, projId, 50);
                                    com.krawler.utils.json.base.JSONObject griddata = new com.krawler.utils.json.base.JSONObject();
                                    griddata.append("data",jobj);
                                    Map<String, String> data = new HashMap<String, String>();
                                    data.put("userid", userId);
                                    data.put("action", action);
                                    data.put("data", griddata.toString()); 
                                    data.put("taskid", tid);
                                    data.put("rowindex",rowindex);
                                    ServerEventManager.publish("/projectplan/"+projId, data, this.getServletContext());

                                    String params = userFullName + " ("+ userName +"), " +
                                                    jobj.getString("taskname") + " [" + jobj.getString("duration") + "]" +
                                                    ", " + projName;
                                    AuditTrail.insertLog(conn, "111", loginid, tid, projId, companyid, params, ipAddress, auditMode);
                                    jbj.put("data", "{}");
                                    /*com.krawler.utils.json.base.JSONObject resp = new com.krawler.utils.json.base.JSONObject();
                                    resp.put("taskid",tid);
                                    out.print(resp.toString());*/

                                } else {
                                    pdates = projdb.UpdateTask(conn, jobj, tid, loginid).split(",");
                                    //String tstamp = projdb.insertBuffer(conn, jobj.toString(), projId, tid, action, Integer.parseInt(rowindex),userId);
                                    String usrid=AuthHandler.getUserid(request);
                                  //  LogHandler.InsertLogForProject(conn, usrid, usrid, projId, 50);
                                    projdb.deleteProgressRequest(conn, tid, usrid);
                                    com.krawler.utils.json.base.JSONObject griddata = new com.krawler.utils.json.base.JSONObject();
                                    jobj.remove("startdate");
                                    jobj.put("startdate", pdates[0]);
                                    jobj.remove("enddate");
                                    jobj.put("enddate", pdates[1]);
                                    jobj.remove("actstartdate");
                                    jobj.put("actstartdate", pdates[2]);
                                    griddata.append("data",jobj);
                                    Map<String, String> data = new HashMap<String, String>();
                                    data.put("userid", userId);
                                    data.put("action", action);
                                    data.put("data", griddata.toString()); 
                                    data.put("taskid", tid);
                                    data.put("rowindex",rowindex);
                                    ServerEventManager.publish("/projectplan/"+projId, data, this.getServletContext());

                                    String params = userFullName + " ("+ userName +"), " +
                                                    jobj.getString("taskname") + " [" + jobj.getString("duration") + "]" +
                                                    ", " + projName;
                                    AuditTrail.insertLog(conn, "112", loginid, tid, projId, companyid, params, ipAddress, auditMode);
                                    jbj.put("data", "{}");
                                }
                            } catch (Exception e) {
                                jbj.put("data", "Unable to create " + e.getMessage());
                            }
                        }
                        else if(action.compareTo("updateGroupTask") == 0) {
                            try {
                                String jsonstr = StringUtil.serverHTMLStripper(request.getParameter("val"));
                                com.krawler.utils.json.base.JSONArray jsonArray = new com.krawler.utils.json.base.JSONArray(jsonstr);
                                com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject();
                                String jsonResStr ="[";
                                for (int k = 0; k < jsonArray.length(); k++) {
                                    jobj = jsonArray.getJSONObject(k);
                                    pdates = projdb.UpdateTask(conn, jobj, jobj.getString("taskid"), loginid).split(",");
                                    jobj.remove("startdate");
                                    jobj.put("startdate", pdates[0]);
                                    jobj.remove("enddate");
                                    jobj.put("enddate", pdates[1]);
                                    jobj.remove("actstartdate");
                                    jobj.put("actstartdate", pdates[2]);
                                    jsonResStr = jsonResStr.concat(jobj.toString()).concat(",");

                                    String params = userFullName + " ("+ userName +"), " +
                                                    jobj.getString("taskname") + " [" + jobj.getString("duration") + "]" +
                                                    ", " + projName;
                                    AuditTrail.insertLog(conn, "112", loginid, jobj.getString("taskid"), projId, companyid, params, ipAddress, auditMode);
                                }
                                jsonResStr = jsonResStr.substring(0, jsonResStr.length() - 1);
                                jsonResStr = jsonResStr.concat("]");
                                com.krawler.utils.json.base.JSONArray jArray = new com.krawler.utils.json.base.JSONArray(jsonResStr);
                                String  griddata = "{data:"+jArray+"}";
                                Map<String, String> data = new HashMap<String, String>();
                                data.put("userid", userId);
                                data.put("action", "updateGroupTask");
                                data.put("data", griddata); 
                                data.put("rowindex",rowindex);
                                ServerEventManager.publish("/projectplan/"+projId, data, this.getServletContext());    
                            } catch(Exception e) {
                                jbj.put("data", "Unable to create " + e.getMessage());
                            }
                        } 
                        else if(action.compareTo("updateParentGroupTask")==0) {
                            try {
                                String jsonstr = request.getParameter("val");
                                com.krawler.utils.json.base.JSONArray jsonArray = new com.krawler.utils.json.base.JSONArray(jsonstr);
                                com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject();
                                for (int k = 0; k < jsonArray.length(); k++) {
                                    jobj = jsonArray.getJSONObject(k); 
                                    tid = jobj.getString("taskid");
                                    projdb.UpdateTask(conn, jobj, tid, loginid);

                                    String params = userFullName + " ("+ userName +"), " +
                                                    jobj.getString("taskname") + " [" + jobj.getString("duration") + "]" +
                                                    ", " + projName;
                                    AuditTrail.insertLog(conn, "112", loginid, tid, projId, companyid, params, ipAddress, auditMode);
                                }
                                jbj.put("data", "{}");
                            } catch(Exception e) {
                                jbj.put("data", "Unable to create " + e.getMessage());
                            }
                        }
                        else if (action.compareTo("newtask") == 0) {
                            try {
                                String jsonstr = request.getParameter("val");
                                com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject(jsonstr);

                                if (jobj.getString("taskid").equals("0")) {
                                    action = "newtask";
                                    UUID ud = new UUID(2312, 4123);
                                    tid = ud.randomUUID().toString();
                                    jobj.remove("taskid");
                                    jobj.put("taskid", tid);
                                    projdb.InsertEmptyTask(conn, jobj, tid, projId, rowindex);
                                    //String tstamp = projdb.insertBuffer(conn, jobj.toString(), projId, tid, action, Integer.parseInt(rowindex),userId);
                                    String usrid=AuthHandler.getUserid(request);
                                    //LogHandler.InsertLogForProject(conn, usrid, usrid, projId, 50);
                                    com.krawler.utils.json.base.JSONObject griddata = new com.krawler.utils.json.base.JSONObject();
                                    griddata.append("data",jobj);
                                    Map<String, String> data = new HashMap<String, String>();
                                    data.put("userid", userId);
                                    data.put("action", action);
                                    data.put("data", griddata.toString()); 
                                    data.put("taskid", tid);
                                    data.put("rowindex",rowindex);
                                    ServerEventManager.publish("/projectplan/"+projId, data, this.getServletContext());

                                    String params = userFullName + " ("+ userName +"), " +
                                                    "[Empty Task]" + ", " + projName;
                                    AuditTrail.insertLog(conn, "111", loginid, tid, projId, companyid, params, ipAddress, auditMode);

                                    jbj.put("data", "{}");

                                    /*com.krawler.utils.json.base.JSONObject resp = new com.krawler.utils.json.base.JSONObject();
                                    resp.put("taskid",tid);
                                    out.print(resp.toString());*/
                                }
                            } catch (Exception e) {
                                jbj.put("data", "Unable to create " + e.getMessage());
                            }
                        }
                        else if (action.compareTo("deleteTask") == 0 || action.compareTo("deleteChildTask") == 0) {
                             try {

                                String jsonstr = request.getParameter("val");
                                com.krawler.utils.json.base.JSONArray jsonArray = new com.krawler.utils.json.base.JSONArray(jsonstr);
                                //int startRow = Integer.parseInt(rowindex);
                                String taskIDS = "[";
                                //int startingFrom = jsonArray.getJSONObject(0).getInt("taskid");
                                for (int k = 0; k < jsonArray.length(); k++) {
                                    com.krawler.utils.json.base.JSONObject taskidJObj = new com.krawler.utils.json.base.JSONObject();
                                    com.krawler.utils.json.base.JSONObject jobj = jsonArray.getJSONObject(k);
                                    String taskid = jobj.getString("taskid");
                                    taskidJObj.put("taskid",taskid);
                                    String params = userFullName + " ("+ userName +"), " +
                                                    projdb.getTaskName(conn,taskid) +
                                                    ", " + projName;
                                    AuditTrail.insertLog(conn, "113", loginid, taskid, projId, companyid, params, ipAddress, auditMode);
                                    projdb.UpdateParentStatus(conn, jobj.getString("taskid"), false);
                                    projdb.deleteTask(conn, taskid, projId);
                                    //projdb.insertBuffer(conn, jobj.toString(), projId, taskid, action, Integer.parseInt("-1"),userId);
                                    taskIDS += taskidJObj.toString() + ",";

                                    
                                }
                                if(!StringUtil.isNullOrEmpty(taskIDS))
                                    taskIDS = taskIDS.substring(0,taskIDS.length()-1).concat("]");

                                com.krawler.utils.json.base.JSONArray taskIdArray= new com.krawler.utils.json.base.JSONArray(taskIDS);
                                String  ServerTaskids = "{taskid:"+taskIdArray+"}";

                                
                                String usrid=AuthHandler.getUserid(request);
                                //LogHandler.InsertLogForProject(conn, usrid, usrid, projId, 50);
                                Map<String, String> data = new HashMap<String, String>();
                                data.put("userid", userId);
                                data.put("action", action);
                                data.put("data", ServerTaskids); 
                                data.put("rowindex",rowindex);
                                ServerEventManager.publish("/projectplan/"+projId, data, this.getServletContext());
                                    
                                jbj.put("data", "{}");
                                /*String jsonstr = request.getParameter("val");
                                com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject(jsonstr);
                                String taskid = jobj.getString("taskid");

                                dbcon.deleteTask(taskid, rowindex, projId);
                                String tstamp = dbcon.insertBuffer(jsonstr, projId, taskid, action, Integer.parseInt(rowindex),userId);
                                if(action.compareTo("deleteParentTask") == 0)
                                {    
                                    com.krawler.utils.json.base.JSONObject griddata = new com.krawler.utils.json.base.JSONObject();
                                    griddata.append("data",jobj);
                                    Map<String, String> data = new HashMap<String, String>();
                                    data.put("userid", userId);
                                    data.put("action", "delete");
                                    data.put("data", griddata.toString()); 
                                    data.put("taskid", taskid);
                                    data.put("rowindex",rowindex);
                                    ServerEventManager.publish("/projectplan/"+projId, data, this.getServletContext());
                                }*/
                            } catch (Exception e) {
                                jbj.put("data", "Unable to create " + e.getMessage());
                            }
                        } else if (action.compareTo("indent") == 0 || action.compareTo("outdent") == 0 || action.compareTo("predecessor") == 0) {
                            try {
                                String jsonstr = request.getParameter("value");
                                com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject(jsonstr);
                                String taskid = jobj.getString("taskid");

                                if (action.compareTo("indent") == 0) {
                                    projdb.UpdateParentStatus(conn, jobj.getString("parent"), true);
                                }
                                pdates = projdb.UpdateTask(conn, jobj, taskid, loginid).split(",");
                                jobj.remove("startdate");
                                jobj.put("startdate", pdates[0]);
                                jobj.remove("enddate");
                                jobj.put("enddate", pdates[1]);
                                jobj.remove("actstartdate");
                                jobj.put("actstartdate", pdates[2]);
                                //String tstamp = projdb.insertBuffer(conn, jsonstr, projId, taskid, action, Integer.parseInt(rowindex),userId);
                                String usrid=AuthHandler.getUserid(request);
                                //LogHandler.InsertLogForProject(conn, usrid, usrid, projId, 50);
                                com.krawler.utils.json.base.JSONObject griddata = new com.krawler.utils.json.base.JSONObject();
                                griddata.append("data",jobj);
                                Map<String, String> data = new HashMap<String, String>();
                                data.put("userid", userId);
                                data.put("action", action);
                                data.put("data", griddata.toString()); 
                                data.put("taskid", taskid);
                                data.put("rowindex",rowindex);
                                ServerEventManager.publish("/projectplan/"+projId, data, this.getServletContext());

                                String params = userFullName + " ("+ userName +"), " +
                                                jobj.getString("taskname") + " [" + jobj.getString("duration") + "]" +
                                                ", " + projName;
                                AuditTrail.insertLog(conn, "112", loginid, taskid, projId, companyid, params, ipAddress, auditMode);

                                jbj.put("data", "{}");
                            } catch (Exception e) {
                                jbj.put("data", "Unable to create " + e.getMessage());
                            }
                        } else if(action.compareTo("updateParentTask")==0){
                             try {
                                    tid = request.getParameter("taskid");
                                    String jsonstr = request.getParameter("val");

                                    String projectid = request.getParameter("projectid");
                                    com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject(jsonstr);

                                    projdb.UpdateTask(conn, jobj, tid, loginid);

                                    String params = userFullName + " ("+ userName +"), " +
                                                    jobj.getString("taskname") + " [" + jobj.getString("duration") + "]" +
                                                    ", " + projName;
                                    AuditTrail.insertLog(conn, "112", loginid, tid, projId, companyid, params, ipAddress, auditMode);

                                    jbj.put("data", "{}");
                                 }
                             catch (Exception e) {
                                    jbj.put("data", "Unable to create " + e.getMessage());
                             }

                        }
                        else if (action.compareTo("paste") == 0 || action.compareTo("template") == 0) {
                            try {
                                HashMap newId = new HashMap();
                               // String holidays = projdb.getNWWeekAndCompHolidays(conn, projId);
                                String jsonstr = request.getParameter("value");
                                com.krawler.utils.json.base.JSONArray jsonArray = new com.krawler.utils.json.base.JSONArray(jsonstr);
                                int startRow = Integer.parseInt(rowindex);
                                String taskIDS = "[";
                                String jString = "[";
                                int startingFrom = jsonArray.getJSONObject(0).getInt("taskid");
                                int diffDays = 0;
                                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-d");
                                if(action.compareTo("template") == 0) {
                                    java.util.Date minDate = null;
                                    for (int k = 0; k < jsonArray.length(); k++) {
                                        com.krawler.utils.json.base.JSONObject jobj = jsonArray.getJSONObject(k);
                                        if(jobj.getString("startdate").compareTo("") != 0) {
                                            if(minDate == null)
                                                minDate = sdf.parse(jobj.getString("startdate"));
                                            else {
                                                java.util.Date tempDate = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(jobj.getString("startdate"));
                                                if(tempDate.before(minDate))
                                                    minDate = tempDate;
                                            }
                                        }
                                    }
                                    java.util.Date minProjDate = sdf.parse(request.getParameter("refDate"));
                                    diffDays = (int)((minProjDate.getTime() - minDate.getTime())/(1000*60*60*24));// + weekDaysDiff;
                                }
                                for (int k = 0; k < jsonArray.length(); k++) {
                                    com.krawler.utils.json.base.JSONObject jobj = jsonArray.getJSONObject(k);
                                    com.krawler.utils.json.base.JSONObject taskidJObj = new com.krawler.utils.json.base.JSONObject();
                                    if(action.compareTo("template") == 0) {
                                        int nonworkweekArr[] = projdb.getNonWorkWeekDays(conn, projId);
                                        String holidayArr[] = projdb.getCompHolidays(conn, projId, "");
                                        if(jobj.getString("startdate").compareTo("") != 0) {
                                            java.util.Date dt = sdf.parse(jobj.getString("startdate"));
                                            Calendar cal = Calendar.getInstance();
                                            cal.setTime(dt);
                                            cal.add(Calendar.DATE, diffDays);
                                            java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd");
                                            dt = cal.getTime();
                                            boolean flag = true;
                                            while(flag){
                                                if((Arrays.binarySearch(nonworkweekArr, dt.getDay()) >= 0 || Arrays.binarySearch(holidayArr, sdf1.format(dt)) >= 0)) {
                                                    cal.setTime(dt);
                                                    cal.add(Calendar.DATE, 1);
                                                    dt = cal.getTime();
                                                    flag = true;
                                                } else {
                                                    flag = false;
                                                }
                                            }

                                            jobj.put("startdate", sdf.format(cal.getTime()));
                                            jobj.put("enddate", sdf.format(projdb.calculateEndDate_importCSV(cal.getTime(),
                                                    jobj.getString("duration"),nonworkweekArr, holidayArr)));
                                            dt = sdf.parse(jobj.getString("actstartdate"));
                                            cal.setTime(dt);
                                            cal.add(Calendar.DATE, diffDays);
                                            jobj.put("actstartdate", sdf.format(cal.getTime()));
                                            /*dt = sdf.parse(jobj.getString("enddate"));
                                            cal.setTime(dt);
                                            cal.add(Calendar.DATE, diffDays);*/
                                            //jobj.put("enddate", sdf.format(cal.getTime()));
                                        } else {
                                            jobj.put("startdate", "");
                                            jobj.put("actstartdate", "");
                                            jobj.put("enddate", "");
                                        }
                                    }
                                    UUID ud = new UUID(2312, 4123);
                                    tid = ud.randomUUID().toString();
                                    taskidJObj.put("localid",jobj.getString("taskid"));
                                    newId.put(jobj.getString("taskid"), tid);
                                    jobj.remove("taskid");
                                    jobj.put("taskid", tid);
                                    jobj.put("loginid", loginid);
                                    if (!jobj.getString("parent").equals("0") && jobj.getString("parent").length() < 3) {
                                        int recIndex = jobj.getInt("parent");
                                        jobj.remove("parent");
                                        jobj.put("parent", jsonArray.getJSONObject(recIndex - startingFrom).getString("taskid"));
                                    }
                                    if(!StringUtil.isNullOrEmpty(jobj.getString("startdate"))) {
                                        pdates = projdb.InsertTask(conn, jobj, tid, projId, String.valueOf(startRow)).split(",");
                                        jobj.remove("startdate");
                                        jobj.put("startdate", pdates[0]);
                                        jobj.remove("enddate");
                                        jobj.put("enddate", pdates[1]);
                                        jobj.remove("actstartdate");
                                        jobj.put("actstartdate", pdates[2]);
                                        String params = userFullName + " ("+ userName +"), " +
                                                        jobj.getString("taskname") + " [" + jobj.getString("duration") + "]" +
                                                        ", " + projName;
                                        AuditTrail.insertLog(conn, "111", loginid, tid, projId, companyid, params, ipAddress, auditMode);

                                    } else {
                                        projdb.InsertEmptyTask(conn, jobj, tid, projId, String.valueOf(startRow));

                                        String params = userFullName + " ("+ userName +"), " +
                                                        "[Empty Task]" + ", " + projName;
                                        AuditTrail.insertLog(conn, "111", loginid, tid, projId, companyid, params, ipAddress, auditMode);
                                    }
                                    if(!StringUtil.isNullOrEmpty(jobj.getString("resourcename"))) {
                                        String[] resourceArray = jobj.getString("resourcename").split(",");
                                        String jsonResStr ="[";
                                        for(int rescnt=0;rescnt<resourceArray.length;rescnt++) {
                                            com.krawler.utils.json.base.JSONObject resobj = new com.krawler.utils.json.base.JSONObject();
                                            resobj.put("taskid",tid);
                                            resobj.put("resourceid",resourceArray[rescnt]);
                                            jsonResStr = jsonResStr.concat(resobj.toString()).concat(",");
                                        }
                                        jsonResStr = jsonResStr.substring(0, jsonResStr.length() - 1);
                                        jsonResStr = jsonResStr.concat("]");
                                        projdb.insertTaskResource(conn, jsonResStr, tid, loginid);
                                    }
                                    //jString = jString.concat(jobj.toString()).concat(",");
                                    startRow = startRow + 1;
                                    taskidJObj.put("serverid",tid);
                                    taskIDS += taskidJObj.toString() + ",";
                                }
                                for (int k = 0; k < jsonArray.length(); k++) {
                                    com.krawler.utils.json.base.JSONObject jobj = jsonArray.getJSONObject(k);
                                    if(!StringUtil.isNullOrEmpty(jobj.getString("predecessor"))){
                                        String np = "";
                                        String[] op = jobj.getString("predecessor").split(",");
                                        for(int q = 0; q < op.length; q++){
                                            if(newId.get(op[q]) != null){
                                                np += newId.get(op[q]) + ",";
                                                projdb.addLink(conn, newId.get(op[q]).toString(), jobj.getString("taskid"));
                                            }
                                        }
                                        jobj.remove("predecessor");
                                        if(!StringUtil.isNullOrEmpty(np))
                                            jobj.put("predecessor", np.substring(0, (np.length() -1 )));
                                        else
                                            jobj.put("predecessor", "");
                                    }
                                    jString = jString.concat(jobj.toString()).concat(",");
                                }
                                jString = jString.substring(0, jString.length() - 1);
                                jString = jString.concat("]");
                                if(!StringUtil.isNullOrEmpty(taskIDS))
                                    taskIDS = taskIDS.substring(0,taskIDS.length()-1).concat("]");

                                
                                //String tstamp = projdb.insertBuffer(conn, jString, projId, tid, action, Integer.parseInt(rowindex),userId);
                                String usrid=AuthHandler.getUserid(request);
                                //LogHandler.InsertLogForProject(conn, usrid, usrid, projId, 50);
                                com.krawler.utils.json.base.JSONArray jArray = new com.krawler.utils.json.base.JSONArray(jString);
                                String  griddata = "{data:"+jArray+"}";

                                com.krawler.utils.json.base.JSONArray taskIdArray= new com.krawler.utils.json.base.JSONArray(taskIDS);
                                String  ServerTaskids = "{taskid:"+taskIdArray+"}";

                                Map<String, String> data = new HashMap<String, String>();
                                data.put("userid", userId);
                                data.put("action", action);
                                data.put("data", griddata); 
                                data.put("rowindex",rowindex);
                                data.put("ServerTaskId", ServerTaskids);
                                ServerEventManager.publish("/projectplan/"+projId, data, this.getServletContext());

                                jbj.put("data", "{taskid:"+taskIDS.toString()+"}");
                            } catch (Exception e) {
                                jbj.put("valid", true);
                                jbj.put("success", false);
                                jbj.put("data", "Unable to create " + e.getMessage());
                            }
                        } else if (action.compareTo("deletelink") == 0) {
                            try {
                                String fromTask = request.getParameter("fromTaskId");
                                String toTask = request.getParameter("toTaskId");
                                String jsonstr = request.getParameter("value");
                                String operation = request.getParameter("flag");
                                rowindex = "-1";
                                if (operation.compareTo("add") == 0) {
                                    projdb.addLink(conn, fromTask, toTask);
                                } else {
                                    projdb.deleteLink(conn, fromTask, toTask);
                                }

                                jbj.put("data", "{}");
                            } catch (Exception e) {
                                jbj.put("data", "Unable to create " + e.getMessage());
                            }

                        } else if(action.compareTo("savebaseline")==0) {
                            try {
                                String returnObj = KWLErrorMsgs.rsSuccessFalse;
                                String baselineId = request.getParameter("baselineid");
                                String baselineName = request.getParameter("baselinename");
                                String baselineDescription = request.getParameter("baselinedescription");

                                boolean isUnderLimit = projdb.checkBaseline(conn, projId);
                                if(isUnderLimit) {
                                    returnObj = projdb.saveBaseline(conn, projId, userId, baselineName, baselineDescription);

                                } else if(baselineId.compareTo("-1") == 0) {
                                    baselineId = projdb.deleteBaseline(conn, projId, baselineId);
                                    if(baselineId.compareTo("") != 0) {
                                        returnObj = projdb.saveBaseline(conn, projId, userId, baselineName, baselineDescription);
                                        if(returnObj.compareTo(KWLErrorMsgs.rsSuccessTrue) == 0) {
                                            returnObj = String.format("{\"success\":true,\"data\": [{\"baselineid\":\"%s\"}]}", baselineId);
                                        }
                                    } else {
                                        returnObj = KWLErrorMsgs.rsSuccessFalse;
                                    }

                                } else {
                                    returnObj = "{\"success\":false,\"data\": \"delete\"}";
                                }
                                jbj.put("data", returnObj);
                                String params = userFullName + " ("+ userName +"), " +
                                                baselineName + ", " + projName;
                                AuditTrail.insertLog(conn, "116", loginid, baselineId, projId, companyid, params, ipAddress, auditMode);
                            } catch (Exception e) {
                                jbj.put("data", "Unable to create Baseline.");
                            }

                        } else if(action.compareTo("deletebaseline") == 0) {

                            String returnObj = KWLErrorMsgs.rsSuccessFalse;
                            String baselineId = request.getParameter("baselineid");
                            String result = "";
                            String baselineDetails = projdb.getBaselineDetails(conn, baselineId);
                            result = projdb.deleteBaseline(conn, projId, baselineId);
                            if(result.compareTo("") != 0) {
                                returnObj = KWLErrorMsgs.rsSuccessTrue;

                                JSONObject jBsln = new JSONObject(baselineDetails);
                                String baselineName = jBsln.getJSONArray("data").getJSONObject(0).getString("baselinename");
                                String params = userFullName + " ("+ userName +"), " +
                                                baselineName + ", " + projName;
                                AuditTrail.insertLog(conn, "117", loginid, baselineId, projId, companyid, params, ipAddress, auditMode);
                            }
                            jbj.put("data", returnObj);

                        } else if(action.compareTo("updateParentTaskId")==0) {
                            String jsonstr = request.getParameter("value");
                            com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject(jsonstr);
                            try {
                                Iterator iterator = jobj.keys();
                                while(iterator.hasNext())
                                {
                                    String key = (String) iterator.next();
                                    String value = jobj.get(key).toString();
                                    projdb.updateParentFieldOfRecord(conn, key,value);
                                }    
                                    
                                jbj.put("data", "{}");
                            } catch(Exception e)
                            {
                                jbj.put("data", e.getMessage());
                            }        
                        }
                        else if(action.compareTo("importFile")==0) {
                            Map<String, String> data = new HashMap<String, String>();
                            data.put("userid", userId);
                            data.put("action", action);
                            data.put("data", "{}"); 
                            data.put("rowindex","-1");
                            ServerEventManager.publish("/projectplan/"+projId, data, this.getServletContext());
                        } else if(action.compareTo("updatepercentcomplete") == 0){
                            Map<String, String> data = new HashMap<String, String>();
                            data.put("data",projdb.updatePercentComplete(conn, request));
                            projdb.deleteProgressRequest(conn, request.getParameter("taskid"), request.getParameter("uid"));
                            data.put("userid",userId);
                            data.put("action", "updatepercentcomplete");
                            ServerEventManager.publish("/projectplan/"+projId, data, this.getServletContext());

                            String taskid = request.getParameter("taskid");
                            String taskDetails = projdb.getTaskDetails(conn, taskid);
                            JSONObject jtask = new JSONObject(taskDetails);
                            JSONObject taskStr = jtask.getJSONArray("data").getJSONObject(0);
                            String taskName = taskStr.getString("taskname");
                            String taskDuration = taskStr.getString("duration");
                            String params = userFullName + " ("+ userName +"), " +
                                            taskName + " [" + taskDuration + "]" +
                                            ", " + projName;
                            AuditTrail.insertLog(conn, "112", loginid, taskid, projId, companyid, params, ipAddress, auditMode);
                        } else if(action.compareTo("requestprogress") == 0){
                            String returnObj = KWLErrorMsgs.rsSuccessFalse;
                            String requestto = request.getParameter("requestto");
                            tid = request.getParameter("taskid");
                            String result = "";
                            result = projdb.setProgressRequest(conn, request, tid, requestto, userId);
                            if(result.compareTo("") != 0) {
                                returnObj = KWLErrorMsgs.rsSuccessTrue;
                            }
                            jbj.put("data", returnObj);
                        } else if(action.compareTo("insertFromWidget") == 0){
                            com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject();
                            action = "insert";
                            UUID ud = new UUID(2312, 4123);
                            tid = ud.randomUUID().toString();
                            String taskname = request.getParameter("taskname");
                            String startdate = request.getParameter("startdate");
                            String enddate = request.getParameter("enddate");
                            jobj = projdb.InsertTaskFromWidget(conn, taskname, startdate, enddate, tid, projId);
                            com.krawler.utils.json.base.JSONObject griddata = new com.krawler.utils.json.base.JSONObject();
                            String ri = jobj.getString("rowindex");
                            jobj.remove("rowindex");
                            griddata.append("data",jobj);
                            Map<String, String> data = new HashMap<String, String>();
                            data.put("userid", userId);
                            data.put("action", action);
                            data.put("data", griddata.toString());
                            data.put("taskid", tid);
                            data.put("rowindex",ri);
                            ServerEventManager.publish("/projectplan/"+projId, data, this.getServletContext());
                            String params = userFullName + " ("+ userName +"), " +
                                            jobj.getString("taskname") + " [" + jobj.getString("duration") + "]" +
                                            ", " + projName;
                            AuditTrail.insertLog(conn, "111", loginid, tid, projId, companyid, params, ipAddress, auditMode);
                            jbj.put("data", "{}");
                        } else if(action.compareTo("updateNotificationStatus") == 0){
                            String ret = KWLErrorMsgs.rsSuccessFalse;
                            String value = request.getParameter("value");
                            int val = Integer.parseInt(value);
                            String result = projdb.updateNotificationStatus(conn, projId, loginid, val);
                            if(result.compareTo("success") == 0){
                                ret = KWLErrorMsgs.rsSuccessTrue;
                            }
                            jbj.put("data", ret);
                        }

                        List<String> actions = Arrays.asList(new String[]{"update", "newtask", "updateGroupTask", "insertFromWidget",
                                                            "updatepercentcomplete", "importFile", "updateParentTaskId",
                                                            "deletelink", "tempate", "paste", "predecessor", "updateParentTask",
                                                            "deleteTask", "deleteChildTask", "indent", "outdent", "updateParentGroupTask"});
                        if(actions.contains(action)){
                            ChartDataServlet.updateChartDataRequestChangeFlag(conn, loginid, true);
                        }
                        
                        
		} else {
			jbj.put("valid", false);
		}
%>
<%=jbj.toString()%>
<%
            conn.commit();
	} catch (ServiceException ex) {
              DbPool.quietRollback(conn);
              out.println("{\"valid\":true,\"data\":{}}");
    } catch (JSONException jex) {
              DbPool.quietRollback(conn);  
              out.println("{\"valid\":true,\"data\":{}}");
	}  finally {
              DbPool.quietClose(conn);
    }
%>
