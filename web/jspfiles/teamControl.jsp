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
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%
        com.krawler.database.DbPool.Connection conn = null;
        try {
            conn = DbPool.getConnection();
            JSONObject jbj = new JSONObject();
            if (sessionbean.validateSession(request, response)) {
                String action = request.getParameter("action");
                String projId = request.getParameter("projectid");
                String companyid = AuthHandler.getCompanyid(request, true);
                String teamid = "";
                String result = "{\"success\":true,\"data\":\"";
                if (action != null) {
                    switch (Integer.parseInt(action)) {
                        case 1:
                            teamid = projdb.createTeam(conn, request);
                            result += teamid.concat("\"}");
                            break;
                        case 2:
                            teamid = request.getParameter("teamid");
                            projId = request.getParameter("projectids");
                            projdb.assignTeam(conn, request, teamid, projId);
                            result += "\"}";
                            break;
                        case 3:
                            result = DashboardHandler.getAllProjects(conn, companyid);
                            break;
                    }

                }
                jbj.put("valid", true);
                jbj.put("data", result);

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
        } finally {
            DbPool.quietClose(conn);
        }
%>
