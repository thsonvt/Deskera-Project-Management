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
<%@page import="com.krawler.esp.handlers.AuditTrail"%>
<%@page import="com.krawler.esp.project.resource.ProjectResource"%>
<%@page import="com.krawler.esp.project.resource.ResourceDAOImpl"%>
<%@page import="com.krawler.esp.project.resource.ResourceDAO"%>
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@ page import = "java.util.Calendar"%>
<%@page import ="com.krawler.esp.database.*" %>
<%@ page import="com.krawler.common.session.SessionExpiredException"%>
<%@ page import="com.krawler.database.DbPool"%>
<%@ page import="com.krawler.database.DbPool.Connection"%>
<%@ page import="com.krawler.common.service.ServiceException"%>
<%@ page import="com.krawler.common.timezone.Timezone"%>
<%@page import="com.krawler.utils.json.base.JSONException"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import = "java.text.ParseException"%>
<%@ page import = "com.krawler.esp.handlers.Tree"%>
<%@ page import = "java.util.Arrays"%>
<%@ page import = "com.krawler.esp.handlers.calEvent"%>
<%@ page import = "com.krawler.utils.json.base.JSONObject"%>
<%@page import ="com.krawler.esp.handlers.projdb"%>
<%@ page import="com.krawler.esp.handlers.AuthHandler"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%
        
if (sessionbean.validateSession(request, response)) {
        String returnStr = null;
        com.krawler.database.DbPool.Connection conn = null;
        com.krawler.utils.json.base.JSONObject jtemp = new com.krawler.utils.json.base.JSONObject();
        ResourceDAO rd = new ResourceDAOImpl();
	try {
            conn = DbPool.getConnection();
            String userid = AuthHandler.getUserid(request);
            String companyid = AuthHandler.getCompanyid(request);
            if (request.getParameter("seq").compareTo("1") == 0) {
                int  offset = Integer.parseInt(request.getParameter("start"));
                int limit =Integer.parseInt(request.getParameter("limit")); 
                String projid = request.getParameter("projectid");
                returnStr = projdb.getProjectTasks(conn, projid, userid, companyid, offset, limit, true);
            } 

            else if (request.getParameter("seq").compareTo("2") == 0)  {
                String minmaxProjectDate = projdb.getminmaxProjectDate(conn, request.getParameter("projectid"), userid);
                String minDate,maxDate;
                String stDate = "";
                  java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-d HH:mm:ss");
                //java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-d");
                com.krawler.utils.json.base.JSONObject datesObj = new com.krawler.utils.json.base.JSONObject();
                if(minmaxProjectDate !=null)
                {   
                    String[] dates = minmaxProjectDate.split(","); 
                    java.util.Date dt = sdf.parse(dates[0]);
                    minDate = sdf.format(dt);
                    dt = sdf.parse(dates[1]);
                    maxDate = sdf.format(dt);
                    if(dates.length > 2){
                        dt = sdf.parse(dates[2]);
                        stDate = sdf.format(dt);
                    }
                }
                else {
                   String projStartDate = projdb.getProjectStartDate(conn, request.getParameter("projectid"), userid);
                   minDate = projStartDate;
                   maxDate = projStartDate;
                   stDate = projStartDate;
                }
                datesObj.put("mindate", minDate);
                datesObj.put("maxdate", maxDate);
                datesObj.put("startdate", stDate);
                String companyHolidays = projdb.getNWWeekAndCompHolidays(conn, request.getParameter("projectid"));
                returnStr = "[{data:"+datesObj.toString()+"},{"+companyHolidays.substring(1)+"]";
            }else if (request.getParameter("seq").compareToIgnoreCase("3")==0){
                String pid = request.getParameter("projectid");
                String startDate = request.getParameter("startDate");
                String option = request.getParameter("option");
                returnStr = projdb.setStartDate(conn, pid, startDate, userid, option);

            }else if (request.getParameter("seq").compareTo("4") == 0)  {
                returnStr = projdb.sendtaskNotification(conn,request);
            }else if (request.getParameter("seq").compareTo("5") == 0)  {
                returnStr = projdb.sendtaskNotificationToAll(conn,request);
            } else if(request.getParameter("seq").compareTo("8") == 0) {
                String companyHolidays = projdb.getNWWeekAndCompHolidays(conn, request.getParameter("projectid"));
                returnStr = "["+companyHolidays+"]";

            } else if(request.getParameter("seq").compareTo("9") == 0) {
                int cntResult = 0;
                int taskPriority = 0;
                int cntRet = 0;
                int cntRes = 0;

                String projTasks = "{data:{}}";
                String projCal = "{data:{}}";
                String projCid = "";
                String eventDetails = "";
                String taskid = "";
                String taskname = "";
                String notes = "";
                String percentcomplete = "";
                String resourcename = "";
                String startdate = "";
                String enddate = "";
                String duration = "";
                String priority = "";
                String descr = "";
                String retEid = "";
                String resourceAll = "";
                String checkResourceName = "";
                java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd");
                String projid = request.getParameter("pid");
                projTasks = projdb.getProjectTasks(conn, projid, userid, companyid, 0, -1, false);
                if(projTasks.compareTo("{data:{}}") != 0) {

                    projCal = calEvent.getDefaultCalendar(conn, projid, 1).toString();
                    if(projCal.compareTo("{data:{}}") != 0) {
                        com.krawler.utils.json.base.JSONObject jobjCal = new com.krawler.utils.json.base.JSONObject(projCal);
                        projCid = jobjCal.getJSONArray("data").getJSONObject(0).getString("cid");
                    } else {
                        String projName = projdb.getProjectName(conn, projid);
                        if(projName != "") {
                            projCid = Tree.insertNewCalendar(conn, projName, "", "", "", "0", 2, 1, projid);
                        }
                    }
                    if(projCid.compareTo("") != 0) {
                        com.krawler.utils.json.base.JSONObject jobjProj = new com.krawler.utils.json.base.JSONObject(projTasks);
                        com.krawler.utils.json.base.JSONArray jArrProj = new com.krawler.utils.json.base.JSONArray();
                        jArrProj = jobjProj.getJSONArray("data");
                        for(cntResult = 0; cntResult < jArrProj.length(); cntResult++) {

                            taskid = jArrProj.getJSONObject(cntResult).getString("taskid");
                            taskname = jArrProj.getJSONObject(cntResult).getString("taskname");
                            notes = jArrProj.getJSONObject(cntResult).getString("notes");
                            percentcomplete = jArrProj.getJSONObject(cntResult).getString("percentcomplete");
                            resourcename = jArrProj.getJSONObject(cntResult).getString("resourcename");
                            startdate = jArrProj.getJSONObject(cntResult).getString("startdate");
                            enddate = jArrProj.getJSONObject(cntResult).getString("enddate");
                            duration = jArrProj.getJSONObject(cntResult).getString("duration");
                            taskPriority = jArrProj.getJSONObject(cntResult).getInt("priority");

                            if(startdate.compareTo("") == 0 || enddate.compareTo("") == 0) {
                                continue;
                            }
                            /*double dur = 0;
                            if(!StringUtil.isNullOrEmpty(duration)){
                                dur = projdb.getDuration(duration);
                            }*/
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd 00:00:00");
                            java.text.SimpleDateFormat sdfYMD = new java.text.SimpleDateFormat("yyyy-MM-dd");
                            Calendar startCal = Calendar.getInstance();
                            startCal.setTime(sdfYMD.parse(startdate));
                            startdate = sdfYMD.format(startCal.getTime());
                            java.util.Date stdate = startCal.getTime();

                            int noofdays = 0;
                            Calendar c1 = Calendar.getInstance();
                            Calendar endCal = Calendar.getInstance();
                            endCal.setTime(sdfYMD.parse(enddate));
                            //if(dur == 1)
                                endCal.add(Calendar.DATE, 1);
                            enddate = sdfYMD.format(endCal.getTime());
                            java.util.Date edate = endCal.getTime();
                            int cnt = 0;
                            if(taskPriority == 0) {
                                priority = "h";
                            }
                            else if(taskPriority == 2) {
                                priority = "l";
                            }
                            else {
                                priority = "m";
                            }

                            taskname = "[" + duration + "] " + taskname;

                            if(resourcename.compareTo("") != 0) {
                                String[] resources = resourcename.split(",");
                                for(cntRes = 0; cntRes < resources.length; cntRes++) {
                                    ProjectResource pr = (ProjectResource) rd.getResource(conn, resources[cntRes], ResourceDAO.RESOURCE_FROM_PROJECT, projid);
                                    checkResourceName = pr.getResourceName();
                                    if(checkResourceName.compareTo("") != 0) {
                                        if(cntRes == 0) {
                                            resourceAll = checkResourceName;
                                        }
                                        else {
                                            resourceAll = resourceAll + ", " + checkResourceName;
                                        }
                                    }
                                    else {
                                        continue;
                                    }
                                }
                            }
                            if(resourceAll.compareTo("") != 0)
                                taskname = taskname + " (" + resourceAll + ")";
                            if(notes.compareTo("") != 0) {
                                descr += "Notes: " + notes + "  ";
                            }
                            else if(percentcomplete.compareTo("") != 0) {
                                descr += "Percent Complete: " + percentcomplete + "%  ";
                            }

                            eventDetails = calEvent.getEventDetails(conn, taskid).toString();
                            if(eventDetails.compareTo("{data:{}}") == 0) {
                                 retEid = calEvent.insertAllDayEvent(conn, projCid, startdate, enddate, taskname, descr, "", "b", priority, "", "1970-01-01 00:00:00", resourceAll, taskid);
                            } else {
                                 cntRet = calEvent.updateAllDayEvent(conn, projCid, startdate, enddate, taskname, descr, "", "b", priority, "", "1970-01-01 00:00:00", resourceAll, taskid);
                            }
                            startdate = "";
                            enddate = "";
                            taskname = "";
                            descr = "";
                            priority = "";
                            resourceAll = "";
                            taskid = "";
                            returnStr = "{data:{}}";
                        }
                    }
                    else {
                        returnStr = "{data:{}}";
                    }
                }
                else {
                    returnStr = "{data:" + projTasks + "}";
                }
                    String loginid = AuthHandler.getUserid(request);
                    String projName = projdb.getProjectName(conn, projid);
                    String userFullName = AuthHandler.getAuthor(conn, loginid);
                    String userName = AuthHandler.getUserName(request);
                    String params = userFullName + "(" + userName + ")," +"("+projName+")";
                    String ipAddress = AuthHandler.getIPAddress(request);
                    AuditTrail.insertLog(conn, "1112", loginid, taskid, projid, companyid, params, ipAddress, 0);
            } else if(request.getParameter("seq").compareTo("10") == 0) {

               returnStr = projdb.getBaseline(conn, request.getParameter("projectid"), userid);

            } else if(request.getParameter("seq").compareTo("11") == 0) {

               returnStr = projdb.getBaselineData(conn, request.getParameter("baselineid"), request.getParameter("projectid"), userid);
               
            } else if(request.getParameter("seq").compareTo("12") == 0) {

                String projid = request.getParameter("projectid");
                String resourceid = request.getParameter("resourceid");
                String[] resources = resourceid.split(",");
                returnStr = projdb.getResourcesTasks(conn, resources, projid, companyid);
            }
            com.krawler.utils.json.base.JSONObject jbj = new com.krawler.utils.json.base.JSONObject();
            jbj.put("valid", "true");
            jbj.put("data", returnStr);
            out.print(jbj.toString());
        conn.commit();
    } catch(SessionExpiredException e){
        DbPool.quietRollback(conn);
        out.println("{\"valid\": false}");
    } catch (ServiceException ex) {
          DbPool.quietRollback(conn);
          jtemp.put("valid", "true");
          jtemp.put("success", "false");
          jtemp.put("data", "Operation Failed. Cannot access Database.");
          out.print(jtemp.toString());
    } catch (JSONException jex) {
          DbPool.quietRollback(conn);
          jtemp.put("valid", "true");
          jtemp.put("success", "false");
          jtemp.put("data", "Operation Failed");
          out.print(jtemp.toString());
    } catch (ParseException ex) {
          DbPool.quietRollback(conn);
          jtemp.put("valid", "true");
          jtemp.put("success", "false");
          jtemp.put("data", "Operation Failed");
          out.print(jtemp.toString());
    } finally {
          DbPool.quietClose(conn);
    }
} else {
    out.println("{\"valid\": false}");
}
%>
