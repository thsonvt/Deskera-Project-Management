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
<%@ page import="com.krawler.common.session.SessionExpiredException"%>
<%@ page import="com.krawler.esp.handlers.AuthHandler"%>
<%@ page import="com.krawler.esp.database.dbcon"%>
<%@ page import="com.krawler.utils.json.base.JSONObject"%>
<%@ page import="com.krawler.utils.json.base.JSONArray"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%
        String message = "{\"data\":{}}";

        JSONObject res = new JSONObject();
        if (sessionbean.validateSession(request, response)) {
            try {
                int flag = 0, mode = 0;
                String projectid = request.getParameter("projectid");
                String limit = request.getParameter("limit");
                String offset = request.getParameter("start");
                String userid = AuthHandler.getUserid(request);
                String companyid = AuthHandler.getCompanyid(request);
                if(request.getParameter("flag")!=null)
                    flag = Integer.parseInt(request.getParameter("flag"));
                if(request.getParameter("mode")!=null)
                    mode = Integer.parseInt(request.getParameter("mode"));
                switch (flag) {
                    case 0: // get Project Updates
                        mode = Integer.parseInt(request.getParameter("mode"));
                        switch(mode){
                            case 0 : //project plan
                                 message = dbcon.gettaskupdates(userid, companyid, projectid, limit, offset);
                                 break;
                            case 1 ://to-do
                            case 2 ://calendar
                            case 3 : //documents
                            case 4 : //admin
                            case 6 : //Discussion
                                 message = dbcon.getWidgetUpdates(userid, companyid, projectid, mode, limit, offset);
                                 break;
                            case 5 : //reports
                                 message = dbcon.getReportWidgetLinks(userid, companyid, projectid, limit, offset);
                                 break;
                        }
                        break;
                    case 1:// Quicklinks
                        message = dbcon.getQuickLinks(userid, companyid);
                        break;
                    case 2:// Request
                        message = dbcon.getrequest(userid, companyid);
                        break;
                    case 3:// Announcement
                        message = dbcon.getannouncement(userid, companyid);
                        break;
                    case 4:// Chart
                        message = dbcon.getChartForWidget(userid, companyid, request.getParameter("projCount"), limit, offset);
                        break;
                    case 5:// pm
                        message = dbcon.getPMUpdates(userid, projectid, limit, offset);
                        break;
					case 6:// Mydocs
                        message = dbcon.getMyDocsWidget(userid, companyid, limit, offset);
                                //"{\"count\":0,\"data\":[]}";
                        break;
                    case 7://projects with health meter and time progress
                        message = dbcon.getProjectsByTimeProgress(userid, companyid, "10000", "0");
                        break;                                      
                    case -1:
                        message = dbcon.getProjectListMember(userid);
                        break;
                    case 100:
                        String projCount = request.getParameter("projCount");
                        message = dbcon.insertDefaultWidgetState(request);
                        message = dbcon.getAllWidgetsData(message, userid, companyid, projCount);
                        break;
                    case -99:
                        message=dbcon.removeWidgetFromState(request);
                        break;
                    case 99:
                        message=dbcon.insertWidgetIntoState(request);
                        break;
                    case -100:
                        message=dbcon.changeWidgetStateOnDrop(request);
                        break;
                    case 101:
                        message=dbcon.getWelcomeText(userid, companyid);
                        break;
                    case -101:
                        message=dbcon.dontShowHelpMsg(userid);
                        break;
                    case 102:
                        String state = request.getParameter("quicklinksstate");
                        message=dbcon.updateQuickLinksStates(userid, state);
                        break;
                    case 103:
                        String projList = request.getParameter("projList");
                        message = dbcon.getMileStonesForChart(projList, userid, companyid);
                        break;
                    case 104:
                        String mtState = request.getParameter("mtstate");
                        message = dbcon.updateMilestoneTimelineSelection(userid, mtState);
                        break;
                    case 105:                        
                        message = dbcon.getMaintainanceDetails(companyid);
                        break;
                    case 106:
                        message = dbcon.getCustomizedTopLinks(companyid);
                        break;
                    case 107:
                        boolean b = Boolean.parseBoolean(request.getParameter("customwidget"));
                        int count = dbcon.updateCustomWidgetFlag(userid, b);
                        message = (count == 1) ? "{\"success\": true}" : "{\"success\": false}";
                        break;
                }
            } catch (Exception e) {
                res.put("valid", true);
                res.put("data", e);
                message = res.toString();
            } finally {
                res.put("valid", true);
                res.put("data", message);
                message = res.toString();
            }
        } else {
            res.put("valid", false);
            message = res.toString();
        }
        out.println(message);
%>
       
