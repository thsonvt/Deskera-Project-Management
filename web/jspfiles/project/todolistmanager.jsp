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
<%@ page  language="java" %>
<%@ page  import="java.io.*"
          import="java.sql.*"
          import="com.krawler.utils.json.base.*"%>
<%@ page import="com.krawler.esp.database.*" %>
<%@ page import="com.krawler.esp.handlers.*" %>
<%@ page import="com.krawler.common.session.SessionExpiredException"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%
if (sessionbean.validateSession(request, response)) {
            String action = request.getParameter("action");
            String uid = request.getParameter("userid");
            String gtype = request.getParameter("grouptype");
            String loginid = AuthHandler.getUserid(request);
            String userName = AuthHandler.getUserName(request);
            String companyid = AuthHandler.getCompanyid(request);
            String ipAddress = AuthHandler.getIPAddress(request);
            int auditMode = 0;
            int groupType = 0;

            if (gtype != null) {
                groupType = Integer.parseInt(gtype);
            }
            String stus = request.getParameter("status");
            int status = 0;
            if (stus != null) {
                status = Integer.parseInt(stus);
            }
            String taskname = request.getParameter("taskname");

            String torder = request.getParameter("taskorder");
            int taskorder = 0;
            if (torder != null) {
                taskorder = Integer.parseInt(torder);
            }
            String tstatus = request.getParameter("tstatus");
            String task_order = request.getParameter("task_order");
            String parentId = request.getParameter("parentId");
            String taskid = request.getParameter("taskid");
            String localid = request.getParameter("localid");
            String assignedto = request.getParameter("assignedto");
            String priority = request.getParameter("priority");
            String duedate = request.getParameter("duedate");
            String desc = request.getParameter("desc");
            boolean leafflag = Boolean.parseBoolean(request.getParameter("leafflag"));
            String result = "{\"success\":true}";
            if (action != null) {
                switch (Integer.parseInt(action)) {

                    case 1:
                        result = dbcon.getTodoTask(uid, groupType, loginid);
                        break;
                    case 2:
                        String tempdate = duedate.replace('-', '/');
                        Boolean reassign = false;
                        String userid = AuthHandler.getUserid(request);
                     //   java.text.SimpleDateFormat sdf = new java.tString tempdate = duedate.replace('-', '/');ext.SimpleDateFormat("yyyy-MM-d");
                        String prevassigned = null;
                        if(groupType == 2 && leafflag && userid.compareTo(assignedto) != 0){
                            String res=dbcon.getToDoDetails(taskid);
                            if(res.compareTo("{data:{}}") != 0){
                                com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject(res);
                                prevassigned = jobj.getJSONArray("data").getJSONObject(0).getString("assignedto");
                                if(prevassigned.compareTo(assignedto) != 0){
                                    reassign = true;
                                }
                            }
                        }
                        if(tempdate == ""){
                            tempdate = "1970/01/01 00:00:00";
                        }
                        result = dbcon.updateToDotask(taskname, taskorder, status, parentId, taskid, assignedto, tempdate, desc ,priority, loginid);
                        com.krawler.utils.json.base.JSONObject jobjup = new com.krawler.utils.json.base.JSONObject(result);
                        String uptaskId = jobjup.getJSONArray("data").getJSONObject(0).getString("remoteid");
                        if(uptaskId.compareTo("")==0){
                            break;
                        }
                        if(reassign){
                        }
                        else if(groupType == 2 && leafflag && userid.compareTo(assignedto) != 0 && !reassign){
                            //Action Log Id=77 for updating todo task action log 
                        }
                        if(leafflag){
                            dbcon.InsertAuditLogForTodo(taskid, userName, "132", loginid, companyid, ipAddress, auditMode);
                        }else{
                            dbcon.InsertAuditLogForTodo(taskid, userName, "135", loginid, companyid, ipAddress, auditMode);
                        }
                        break;
                    case 3:
                        String due = "1970/01/01 00:00:00";
                        if(duedate.compareTo("") != 0)
                            due = duedate;
                        result = dbcon.insertToDotask(localid, taskname, taskorder, status, parentId, taskid, uid, groupType,due,leafflag,priority, loginid,desc);
                        com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject(result);
                        String taskId = jobj.getJSONArray("data").getJSONObject(0).getString("remoteid");
                        if(taskId.compareTo("")==0){
                           break;
                        }
                        if(groupType == 2 && leafflag && result != null){
                            
                            //String taskId = jobj.getString("remoteid");
                            
                            //Action Log Id=76 for inserting todo task action log
                      //      dbcon.InsertLogForTodo(taskId, uid, AuthHandler.getUserid(request), assignedto, null, taskname, 76);

                            dbcon.InsertAuditLogForTodo(taskId, userName, "131", loginid, companyid, ipAddress, auditMode);

                        } else {
                            
                            dbcon.InsertAuditLogForTodo(taskId, userName, "134", loginid, companyid, ipAddress, auditMode);
                        }
                        break;
                    case 4:
                        /*if(leafflag){
                            dbcon.InsertAuditLogForTodo(taskid, userName, "133", loginid, companyid, ipAddress, auditMode);
                        } else {
                            dbcon.InsertAuditLogForTodo(taskid, userName, "136", loginid, companyid, ipAddress, auditMode);
                        }*/
                        String leafFlags=request.getParameter("leafflag");
                        result = dbcon.deleteToDotask(taskid,leafFlags,userName,loginid,companyid,ipAddress,auditMode);
                        
                        break;
                    case 5:
                        result = dbcon.sendToDoNotification(request);
                        break;
                    case 6:
                        result = dbcon.updateToDoTask_Appendchild(taskname, task_order, tstatus, parentId, taskid, duedate, priority, loginid);
                        break;
                    case 7:
                        result = dbcon.updateToDoTask_changestatus(status, taskid);
                        break;
                    case 8:
                         result = dbcon.addToDoInProjectplan(request);
                         com.krawler.utils.json.base.JSONObject jResult = new com.krawler.utils.json.base.JSONObject(result);
                         java.util.Map<String, String> map = new java.util.HashMap<String, String>();
                         if(jResult.has("userid")){
                             map.put("userid", jResult.getString("userid"));
                             map.put("action", "insertFromToDo");
                             map.put("data", jResult.getString("data"));
                             map.put("rowindex", jResult.getString("rowindex"));
                             ServerEventManager.publish("/projectplan/"+jResult.getString("projid"),  map, this.getServletContext());
                         } else {
                            result = jResult.toString();
                         }
                        break;
                }
            }
            JSONObject jbj = new JSONObject();
            jbj.put("valid", "true");
            jbj.put("data", result);
            out.print(jbj.toString());
} else {
    out.println("{\"valid\": false}");
}
%>
