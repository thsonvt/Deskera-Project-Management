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
<%@page import ="com.krawler.esp.handlers.*"%>
<%@page import ="com.krawler.esp.handlers.AuditTrail"%>
<%@ page import="com.krawler.common.session.SessionExpiredException"%>
<%@ page import="com.krawler.database.DbPool"%>
<%@ page import="com.krawler.database.DbPool.Connection"%>
<%@ page import="com.krawler.common.service.ServiceException"%>
<%@page import="com.krawler.common.util.*"%>
<%@ page import ="com.krawler.esp.portalmsg.Mail" %>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%
com.krawler.utils.json.base.JSONObject jbj = new com.krawler.utils.json.base.JSONObject();
if (sessionbean.validateSession(request, response)) {
    com.krawler.database.DbPool.Connection conn = null;
    try {
    	  String loginid = AuthHandler.getUserid(request);
          String companyid = AuthHandler.getCompanyid (request);
                jbj.put("valid", true);
                conn = DbPool.getConnection();
                String calView=request.getParameter("calView");
                String calAction=request.getParameter("action");
                String ipAddress = AuthHandler.getIPAddress(request);
                int auditMode = 0;
                if(calAction.equals("0")){
                    try{
                        if(calView.equals("0")){
                            String[] cid=request.getParameterValues("cid");
                            String startts=request.getParameter("startts");
                            String endts=request.getParameter("endts");
                            Object returnStr = calEvent.fetchEvent(conn, cid, startts, endts, loginid);
                            com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject(returnStr.toString());
                            //out.print(jobj.toString());
                            jbj.put("data", jobj.toString());
                        }
                        else if(calView.equals("1")){
                            String[] cidList=request.getParameterValues("cidList");
                            String viewdt1=request.getParameter("viewdt1");
                            String viewdt2=request.getParameter("viewdt2");
                            //int limit=Integer.parseInt(request.getParameter("limit"));
                            //int start=Integer.parseInt(request.getParameter("start"));
                            Object[] returnStr = calEvent.fetchAgendaEvent(conn, cidList, viewdt1, viewdt2, 50, 0, loginid);
                            com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject(returnStr[1].toString());
                            jobj.put("totalCount", returnStr[0].toString());
                            jbj.put("data", jobj.toString());
                        }
                    }
                    catch(ServiceException e){
                        System.out.println("calEvent:Fetch error:"+e.toString());
                        jbj.put("data", "{\"success\":\"false\"}");
                    }
                }
                else if(calAction.equals("1")){
                    try{
                        if(calView.equals("0")){
                            String resCount = "";
                            String eid=request.getParameter("eid");
                            String cid=request.getParameter("cid");
                            String startts=request.getParameter("startts");
                            String endts=request.getParameter("endts");
                            String subject=request.getParameter("subject");
                            String descr=request.getParameter("descr");
                            String location=request.getParameter("location");
                            String showas=request.getParameter("showas");
                            String priority=request.getParameter("priority");
                            String recpattern=request.getParameter("recpattern");
                            String recend=request.getParameter("recend");
                            String resources=request.getParameter("resources");
                            boolean allDay = false;
                            if(request.getParameter("allDay").compareToIgnoreCase("false") == 0 || request.getParameter("allDay").compareToIgnoreCase("") == 0)
                                resCount = calEvent.insertEvent(conn, cid, startts, endts, subject, descr, location, showas, priority, recpattern, recend, resources, loginid, companyid, "");
                            else {
                                allDay = true;
                                resCount = calEvent.insertAllDayEvent(conn, cid, startts, endts, subject, descr, location, showas, priority, recpattern, recend, resources, "");
                            }
                            if(!resCount.equals("0")){
                                String userid =request.getParameter("userid");
                                if(userid!="" && userid != null){
                                    String userList[];
                                    userList= userid.split(",");
                                    for (int i=0;i<userList.length;i++){
                                        String userResponse[];
                                        String username[];
                                        userResponse=userList[i].split("_");
                                        username = userResponse[0].split("/");
                                        calEvent.insertGuest(conn, resCount,username[0], userResponse[1]);
                                        //String loginid = AuthHandler.getUserid(request);
                                        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-d HH:mm:ss" );
                                        SimpleDateFormat sdf2 = new SimpleDateFormat( "h a" );
                                        SimpleDateFormat sdf1 = new SimpleDateFormat( "dd MMM" );
                                        SimpleDateFormat sdf3 = new SimpleDateFormat( "EEE" );
                                        Date dt1=sdf.parse(startts);
                                        Date dt2=sdf.parse(endts);
                                        String datefield = sdf1.format(dt1)+" - "+sdf1.format(dt2);
                                        String timefield = sdf2.format(dt1)+" - "+sdf2.format(dt2);
                                        String titleString = "[Invitation] "+subject+"@"+sdf3.format(dt1)+"  "+datefield+" , "+timefield+"("+username[1]+")";
                                        String text_message = "<div  style='border-style: solid; border-color: rgb(204, 204, 204); border-width: 1px 1px 0pt; margin: 0pt auto; padding: 15px 15px 5px; background: rgb(211, 224, 242) none repeat scroll 0% 50%; width: 370px; -moz-background-clip: -moz-initial; -moz-background-origin: -moz-initial; -moz-background-inline-policy: -moz-initial;'>" +
                                        "<p  style='margin: 0pt;'>Hi "+username[1]+" ,</p>"+
                                        "<p style='margin: 0pt 10px;'>You are invited to ,</p><br><h2  style='margin: 5px 0pt; font-size: 18px; line-height: 1.4;'>"+subject+
                                        "</h2><br><p  style='margin: 0pt 0pt 0.5em;'><span style=''>( "+sdf3.format(dt1)+"  "+datefield+" , "+timefield+" )</span><br><br>" +
                                        "</span></p><br><p  style='margin: 0pt 0pt 1em; white-space: -moz-pre-wrap ! important;'>" +
                                        "<div  style='margin: 0.5em 0pt 0pt;height:20px;'><div style='width:100px;float:left;'>Will you attend? </div><div style='color: #153E7E;cursor: pointer;text-decoration:underline;width:40px;float:left;' onclick='javascript:guestResponse(\""+resCount+"\",\""+username[0]+"\",\""+1+"\")'>Accept</div><div style='color: #153E7E;cursor: pointer;text-decoration:underline;width:40px;float:left;' onclick='javascript: guestResponse(\""+resCount+"\",\""+username[0]+"\",\""+0+"\")'>Reject</div></div></div>";
                                        //String text_message = "Hi "+username[1]+", You are invited to "+subject+" on date "+startts+" - "+endts+" <a href=jspfiles/guestStatus?eid="+eid+"&userid="+username[0]+"&response=1>Accept</a>&nbsp;<a href=jspfiles/guestStatus?eid="+eid+"&userid="+username[0]+"&response=0>Reject</a>";
                                        String insertMsg = Mail.insertMailMsg(conn, username[0],loginid,titleString,text_message,"1",false,"1","","newmsg","",3,"",companyid);
                                    }
                                }
                                String reminders = request.getParameter("reminders");
                                if(reminders!=""){
                                    String reminder[];
                                    reminder = reminders.split(",");
                                    for(int i=0;i<reminder.length;i++){
                                        String reminderMenu[];
                                        reminderMenu = reminder[i].split("_");
                                        int reminderTime=Integer.parseInt(reminderMenu[1]);
                                        calEvent.insertReminder(conn, resCount,reminderMenu[0],reminderTime);
                                    }
                                }

                                //com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject();
                                //com.krawler.utils.json.base.JSONObject jobj1 = new com.krawler.utils.json.base.JSONObject();
                                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                java.util.Date currentDate = new Date();
                                String timestamp = sdf.format(currentDate);
                                String eventData = calEvent.BreakEvent(resCount, cid, startts, endts, subject, descr, location, showas, priority, recpattern, recend, resources, timestamp, allDay, "0").toString();
                                com.krawler.utils.json.base.JSONObject jobj1 = new com.krawler.utils.json.base.JSONObject(eventData);
                                /*jobj.put("eid",resCount);
                                jobj.put("cid",cid);
                                jobj.put("startts",startts);
                                jobj.put("endts",endts);
                                jobj.put("subject",subject);
                                jobj.put("descr",descr);
                                jobj.put("location",location);
                                jobj.put("showas",showas);
                                jobj.put("priority",priority);
                                jobj.put("recpattern",recpattern);
                                jobj.put("recend",recend);
                                jobj.put("resources",resources);
                                jobj.put("allday", allDay);
                                jobj1.append("data",jobj);*/

                                Map<String, String> data = new HashMap<String, String>();
                                data.put("action", calAction);
                                data.put("calView", calView);
                                data.put("success","true");
                                data.put("data", jobj1.toString());
                                ServerEventManager.publish("/calEvent/"+cid, data, this.getServletContext());
                                jbj.put("data", "{\"success\":\"true\"}");

                                String projectId = Tree.getProjectId(conn, cid);
                                String params = AuthHandler.getAuthor(conn, loginid) + " ("+ AuthHandler.getUserName(request) +"), " +
                                                subject + ", " + Tree.getCalendarName(conn, cid) +
                                                ", " + projdb.getProjectName(conn, projectId);
                                AuditTrail.insertLog(conn, "121", loginid, projectId, projectId, companyid, params, ipAddress, auditMode);

                            } else {
                                jbj.put("data", "{\"success\":\"false\"}");
                            }
                        }
                    }
                    catch(ServiceException e){
                        System.out.println("calEvent:Insertion error:"+e.toString());
                        jbj.put("data", "{\"success\":\"false\"}");
                    }
                }
                else if(calAction.equals("2")){
                    try{
                        if(calView.equals("0")){
                            int resCount = 0;
                            String eid=request.getParameter("eid");
                            String cid=request.getParameter("cid");
                            String startts=request.getParameter("startts");
                            String endts=request.getParameter("endts");
                            String subject=request.getParameter("subject");
                            String descr=request.getParameter("descr");
                            String location=request.getParameter("location");
                            String showas=request.getParameter("showas");
                            String priority=request.getParameter("priority");
                            String recpattern=request.getParameter("recpattern");
                            String recend=request.getParameter("recend");
                            String resources=request.getParameter("resources");
                            String fullUpdate = request.getParameter("fullupdate");
                            boolean allDay = false;
                            if(request.getParameter("allDay").compareToIgnoreCase("false") == 0 || request.getParameter("allDay").compareToIgnoreCase("") == 0)
                                resCount = calEvent.updateEvent(conn, cid, startts, endts, subject, descr, location, showas, priority, recpattern, recend, resources, eid, loginid, companyid);
                            else {
                                allDay = true;
                                resCount = calEvent.updateAllDayEvent(conn, cid, startts, endts, subject, descr, location, showas, priority, recpattern, recend, resources, eid);
                            }
                            if(resCount!=0){
                                if(fullUpdate.equals("1")){
                                    String userid =request.getParameter("userid");
                                    calEvent.deleteGuest(conn, eid);
                                    if(userid!="" && userid != null){
                                        String userList[];
                                        userList= userid.split(",");
                                        for (int i=0;i<userList.length;i++){
                                            String userResponse[];
                                            String username[];
                                            userResponse=userList[i].split("_");
                                            username = userResponse[0].split("/");
                                            calEvent.insertGuest(conn, eid,username[0], userResponse[1]);
                                            if(userResponse[1].compareTo("p") == 0){
                                                //String loginid = AuthHandler.getUserid(request);
                                                SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-d HH:mm:ss" );
                                                SimpleDateFormat sdf2 = new SimpleDateFormat( "h a" );
                                                SimpleDateFormat sdf1 = new SimpleDateFormat( "dd MMM" );
                                                SimpleDateFormat sdf3 = new SimpleDateFormat( "EEE" );
                                                Date dt1=sdf.parse(startts);
                                                Date dt2=sdf.parse(endts);
                                                String datefield = sdf1.format(dt1)+" - "+sdf1.format(dt2);
                                                String timefield = sdf2.format(dt1)+" - "+sdf2.format(dt2);
                                                String titleString = "[Invitation] "+subject+"@"+sdf3.format(dt1)+"  "+datefield+" , "+timefield+"("+username[1]+")";
                                                String text_message = "<div  style='border-style: solid; border-color: rgb(204, 204, 204); border-width: 1px 1px 0pt; margin: 0pt auto; padding: 15px 15px 5px; background: rgb(211, 224, 242) none repeat scroll 0% 50%; width: 370px; -moz-background-clip: -moz-initial; -moz-background-origin: -moz-initial; -moz-background-inline-policy: -moz-initial;'>" +
                                                "<p  style='margin: 0pt;'>Hi "+username[1]+" ,</p>"+
                                                "<p style='margin: 0pt 10px;'>You are invited to ,</p><br><h2  style='margin: 5px 0pt; font-size: 18px; line-height: 1.4;'>"+subject+
                                                "</h2><br><p  style='margin: 0pt 0pt 0.5em;'><span style=''>( "+sdf3.format(dt1)+"  "+datefield+" , "+timefield+" )</span><br><br>" +
                                                "</span></p><br><p  style='margin: 0pt 0pt 1em; white-space: -moz-pre-wrap ! important;'>" +
                                                "<div  style='margin: 0.5em 0pt 0pt;height:20px;'><div style='width:100px;float:left;'>Will you attend? </div><div style='color: #153E7E;cursor: pointer;text-decoration:underline;width:40px;float:left;' onclick='javascript:guestResponse(\""+eid+"\",\""+username[0]+"\",\""+1+"\")'>Accept</div><div style='color: #153E7E;cursor: pointer;text-decoration:underline;width:40px;float:left;' onclick='javascript: guestResponse(\""+eid+"\",\""+username[0]+"\",\""+0+"\")'>Reject</div></div></div>";
                                                //String text_message = "Hi "+username[1]+", You are invited to "+subject+" on date "+startts+" - "+endts+" <a href=/jspfiles/guestStatus?eid="+eid+"&userid="+username[0]+"&response=1>Accept</a>&nbsp;<a href=/jspfiles/guestStatus?eid="+eid+"&userid="+username[0]+"&response=0>Reject</a>";
                                                String insertMsg = Mail.insertMailMsg(conn, username[0],loginid,titleString,text_message,"1",false,"1","","newmsg","",3,"", companyid);
                                            }
                                        }
                                    }
                                    String reminders = request.getParameter("reminders");
                                    calEvent.deleteReminder(conn, eid);
                                    if(reminders!=""){
                                        String reminder[];
                                        reminder = reminders.split(",");
                                        for(int i=0;i<reminder.length;i++){
                                            String reminderMenu[];
                                            reminderMenu = reminder[i].split("_");
                                            int reminderTime=Integer.parseInt(reminderMenu[1]);
                                            calEvent.insertReminder(conn, eid,reminderMenu[0],reminderTime);
                                        }
                                    }
                                }

                                //com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject();
                                //com.krawler.utils.json.base.JSONObject jobj1 = new com.krawler.utils.json.base.JSONObject();
                                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                java.util.Date currentDate = new Date();
                                String timestamp = sdf.format(currentDate);
                                String eventData = calEvent.BreakEvent(eid, cid, startts, endts, subject, descr, location, showas, priority, recpattern, recend, resources, timestamp, allDay, "0").toString();
                                com.krawler.utils.json.base.JSONObject jobj1 = new com.krawler.utils.json.base.JSONObject(eventData);
                                /*jobj.put("eid",eid);
                                jobj.put("cid",cid);
                                jobj.put("startts",startts);
                                jobj.put("endts",endts);
                                jobj.put("subject",subject);
                                jobj.put("descr",descr);
                                jobj.put("location",location);
                                jobj.put("showas",showas);
                                jobj.put("priority",priority);
                                jobj.put("recpattern",recpattern);
                                jobj.put("recend",recend);
                                jobj.put("resources",resources);
                                jobj.put("allday", allDay);
                                jobj1.append("data",jobj);*/

                                Map<String, String> data = new HashMap<String, String>();
                                data.put("action", calAction);
                                data.put("calView", calView);
                                data.put("success","true");
                                data.put("data", jobj1.toString());
                                ServerEventManager.publish("/calEvent/"+cid, data, this.getServletContext());
                                jbj.put("data", "{\"success\":\"true\"}");

                                String projectId = Tree.getProjectId(conn, cid);
                                String params = AuthHandler.getAuthor(conn, loginid) + " ("+ AuthHandler.getUserName(request) +"), " +
                                                subject + ", " + Tree.getCalendarName(conn, cid) +
                                                ", " + projdb.getProjectName(conn, projectId);
                                AuditTrail.insertLog(conn, "122", loginid, projectId, projectId, companyid, params, ipAddress, auditMode);

                            } else {
                                jbj.put("data", "{\"success\":\"false\"}");
                            }
                        }
                        else if(calView.equals("1")){
                            int resCount = 0;
                            String eid=request.getParameter("eid");
                            String cid=request.getParameter("cid");
                            String startts=request.getParameter("startts");
                            String endts=request.getParameter("endts");
                            String subject=request.getParameter("subject");
                            String descr=request.getParameter("descr");
                            String location=request.getParameter("location");
                            String showas=request.getParameter("showas");
                            String priority=request.getParameter("priority");
                            String recpattern=request.getParameter("recpattern");
                            String recend=request.getParameter("recend");
                            String resources=request.getParameter("resources");
                            boolean allDay = false;
                            if(request.getParameter("allDay").compareToIgnoreCase("false") == 0 || request.getParameter("allDay").compareToIgnoreCase("") == 0)
                                resCount = calEvent.updateEvent(conn, cid, startts, endts, subject, descr, location, showas, priority, recpattern, recend, resources, eid, loginid, companyid);
                            else {
                                allDay = true;
                                resCount = calEvent.updateAllDayEvent(conn, cid, startts, endts, subject, descr, location, showas, priority, recpattern, recend, resources, eid);
                            }
                            if(resCount!=0){
                                calEvent.deleteGuest(conn, eid);
                                calEvent.deleteReminder(conn, eid);
                                String userid =request.getParameter("userid");
                                if(!com.krawler.common.util.StringUtil.isNullOrEmpty(userid)){
                                    String userList[];
                                    userList= userid.split(",");
                                     for (int i=0;i<userList.length;i++){
                                            String userResponse[];
                                            userResponse =userList[i].split("_");
                                            calEvent.insertGuest(conn, eid,userResponse[0],userResponse[1]);
                                    }
                                }
                                String reminders = request.getParameter("reminders");
                                if(reminders!=""){
                                    String reminder[];
                                    reminder = reminders.split(",");
                                    for(int i=0;i<reminder.length;i++){
                                        String reminderMenu[];
                                        reminderMenu = reminder[i].split("_");
                                        int reminderTime=Integer.parseInt(reminderMenu[1]);
                                        calEvent.insertReminder(conn, eid,reminderMenu[0],reminderTime);
                                    }
                                }

                                //com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject();
                                //com.krawler.utils.json.base.JSONObject jobj1 = new com.krawler.utils.json.base.JSONObject();
                                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                java.util.Date currentDate = new Date();
                                String timestamp = sdf.format(currentDate);
                                String eventData = calEvent.BreakEvent(eid, cid, startts, endts, subject, descr, location, showas, priority, recpattern, recend, resources, timestamp, allDay, "0").toString();
                                com.krawler.utils.json.base.JSONObject jobj1 = new com.krawler.utils.json.base.JSONObject(eventData);
                                /*jobj.put("eid",eid);
                                jobj.put("cid",cid);
                                jobj.put("startts",startts);
                                jobj.put("endts",endts);
                                jobj.put("subject",subject);
                                jobj.put("descr",descr);
                                jobj.put("location",location);
                                jobj.put("showas",showas);
                                jobj.put("priority",priority);
                                jobj.put("recpattern",recpattern);
                                jobj.put("recend",recend);
                                jobj.put("resources",resources);
                                jobj.put("allday", allDay);
                                jobj1.append("data",jobj);*/

                                Map<String, String> data = new HashMap<String, String>();
                                data.put("action", calAction);
                                data.put("calView", calView);
                                data.put("success","true");
                                data.put("data", jobj1.toString());
                                ServerEventManager.publish("/calEvent/"+cid, data, this.getServletContext());
                                jbj.put("data", "{\"success\":\"true\"}");

                                String projectId = Tree.getProjectId(conn, cid);
                                String params = AuthHandler.getAuthor(conn, loginid) + " ("+ AuthHandler.getUserName(request) +"), " +
                                                subject + ", " + Tree.getCalendarName(conn, cid) +
                                                ", " + projdb.getProjectName(conn, projectId);
                                AuditTrail.insertLog(conn, "122", loginid, projectId, projectId, companyid, params, ipAddress, auditMode);

                            } else {
                                jbj.put("data", "{\"success\":\"false\"}");
                            }
                        }
                    }
                    catch(Exception e){
                        System.out.println("calEvent:Updation error:"+e.toString());
                        jbj.put("data", "{\"success\":\"false\"}");
                    }
                }
                else if(calAction.equals("3")){
                    try{
                        if(calView.equals("0")){
                            String eid=request.getParameter("eid");
                            String cid=request.getParameter("cid");
                            String eventDetails = calEvent.getEventDetails(conn, eid).toString();

                            int resCount = calEvent.deleteEvent(conn, eid);

                            if(resCount!=0){
                                com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject();
                                Map<String, String> data = new HashMap<String, String>();
                                data.put("action", calAction);
                                data.put("calView", calView);
                                data.put("eid", eid);
                                data.put("success","true");
                                ServerEventManager.publish("/calEvent/"+cid, data, this.getServletContext());
                                jbj.put("data", "{\"success\":\"true\"}");
                                //out.print("{\"success\":\"true\"}");

                                if (eventDetails.compareTo("{data:{}}") != 0) {
                                    com.krawler.utils.json.base.JSONObject jobjEvent = new com.krawler.utils.json.base.JSONObject(eventDetails);
                                    String subject = jobjEvent.getJSONArray("data").getJSONObject(0).getString("subject");

                                    String projectId = Tree.getProjectId(conn, cid);
                                    String params = AuthHandler.getAuthor(conn, loginid) + " ("+ AuthHandler.getUserName(request) +"), " +
                                                    subject + ", " + Tree.getCalendarName(conn, cid) +
                                                    ", " + projdb.getProjectName(conn, projectId);
                                    AuditTrail.insertLog(conn, "123", loginid, projectId, projectId, companyid, params, ipAddress, auditMode);
                                }

                            } else {
                                jbj.put("data", "{\"success\":\"false\"}");
                            }

                        } else if(calView.equals("1")) {
                            String eid=request.getParameter("eid");
                            String cid=request.getParameter("cid");
                            String[] delList= eid.split(",");
                            String[] cList= cid.split(",");

                            int resCount=0;
                            resCount = calEvent.deleteAgendaEvent(conn,delList);

                            if(resCount!=0){
                                for(int j=0;j<delList.length;j++){
                                    com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject();
                                    Map<String, String> data = new HashMap<String, String>();
                                    data.put("action", calAction);
                                    data.put("calView", calView);
                                    data.put("eid",delList[j]);
                                    data.put("success","true");
                                    ServerEventManager.publish("/calEvent/"+cList[j], data, this.getServletContext());
                                    }
                                jbj.put("data", "{\"success\":\"true\"}");
                                //out.print("{\"success\":\"true\"}");
                                }
                            else{
                                jbj.put("data", "{\"success\":\"false\"}");
                                //out.print("{\"success\":\"false\"}");
                            }
                        }
                    }
                    catch(Exception e){
                        System.out.println("calEvent:Deletion error:"+e.toString());
                        jbj.put("data", "{\"success\":\"false\"}");
                    }
                }else if(calAction.equals("4")){
                    try{
                        String eid=request.getParameter("eid");
                        Object returnGuest = calEvent.selectGuest(conn, eid);
                        com.krawler.utils.json.base.JSONObject jobjGuest = new com.krawler.utils.json.base.JSONObject(returnGuest.toString());
                        Object returnStr = calEvent.selectReminder(conn, eid);
                        com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject(returnStr.toString());
                        jobjGuest.put("Data1", jobj);
                        jbj.put("data",jobjGuest.toString());

                    } catch(Exception e) {
                        System.out.println("calEvent:Selection error:"+e.toString());
                        jbj.put("data", "{\"success\":\"false\"}");
                    }
                }else if(calAction.equals("5")){
                    try{
                        String companyHolidays = projdb.getNWWeekAndCompHolidays(conn, request.getParameter("projectid"));
                        jbj.put("data", companyHolidays);

                    } catch(Exception e) {
                        System.out.println("calEvent:Selection error:"+e.toString());
                        jbj.put("data", "{\"success\":\"false\"}");
                    }
                } else if(calAction.equals("6")){
                    try {
                        boolean b = calEvent.checkForExport(conn, request.getParameter("cid"));
                        jbj.put("data", "{\"exportCheck\":\""+Boolean.toString(b) +"\"}");
                    } catch(Exception e) {
                        System.out.println("calEvent:Selection error:"+e.toString());
                        jbj.put("data", "{\"exportCheck\":\"false\"}");
                    }
                }
            conn.commit();
            out.println(jbj.toString());
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            System.out.println("calEvent:Connection Error:"+ex.toString());
        } catch(Exception e){
            DbPool.quietRollback(conn);
            System.out.println("calEvent: error:"+e.toString());
        } finally {
            DbPool.quietClose(conn);
        }

} else {
	out.println("{\"valid\": false}");
}
 %>
