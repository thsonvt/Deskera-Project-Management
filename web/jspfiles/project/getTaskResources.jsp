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
<%@page import ="com.krawler.esp.handlers.*"%>
<%@page import ="java.util.*,
        java.sql.*,
        javax.servlet.ServletRequest,
        com.krawler.utils.json.*,
        com.krawler.esp.database.*" %>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@ page import="com.krawler.common.session.SessionExpiredException"%>
<%@ page import="com.krawler.database.DbPool"%>
<%@ page import="com.krawler.database.DbPool.Connection"%>
<%@ page import="com.krawler.common.service.ServiceException"%>
<%@page import="com.krawler.utils.json.base.JSONException"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%
        com.krawler.database.DbPool.Connection conn = null;
        if (sessionbean.validateSession(request, response)) {
            try {
                conn = DbPool.getConnection();
                String act = request.getParameter("action");
                String tid = request.getParameter("taskid");
                String pid = request.getParameter("projectid");
                String data = request.getParameter("data");
                String rowindex = request.getParameter("row");
                String userid = request.getParameter("userid");
                String res1 = "";
                if (act == null) {
                    try {
                        res1 = projdb.getTaskResources(conn, tid, pid);
                    // out.print(res1);
                    } catch (Exception e) {
                    }
                } else if (act.compareTo("insert") == 0) {
                    String loginid = AuthHandler.getUserid(request);
                    String userFullName = AuthHandler.getAuthor(conn, loginid);
                    String userName = AuthHandler.getUserName(request);
                    String projName = projdb.getProjectName(conn, pid);
                    String companyid = AuthHandler.getCompanyid(request);
                    String ipAddress = AuthHandler.getIPAddress(request);
                    int auditMode = 0;

                    //try{
                    res1 = projdb.insertTaskResource(conn, data, tid, loginid);
                    String tname = projdb.getTaskName(conn, tid);
                    //out.print(res1);

                    String params = userFullName + " ("+ userName +"), "+ tname +", " + projName;
                    AuditTrail.insertLog(conn, "112", loginid, tid, pid, companyid, params, ipAddress, auditMode);
                    String resourcenames = "";
                    if (!StringUtil.isNullOrEmpty(data)) {
                        com.krawler.utils.json.base.JSONArray jsonArray = new com.krawler.utils.json.base.JSONArray(data);
                        for (int k = 0; k < jsonArray.length(); k++) {
                            com.krawler.utils.json.base.JSONObject jobj = jsonArray.getJSONObject(k);
                            resourcenames = resourcenames.concat(jobj.getString("resourceid")).concat(",");
                        }
                        if (!StringUtil.isNullOrEmpty(resourcenames)) {
                            resourcenames = resourcenames.substring(0, resourcenames.length() - 1);
                        }
                    }
                    com.krawler.utils.json.base.JSONObject resp = new com.krawler.utils.json.base.JSONObject();
                    resp.put("resourcename", resourcenames);
                    resp.put("data", res1);
                    Map<String, String> res = new HashMap<String, String>();
                    res.put("userid", userid);
                    res.put("action", "assignresource");
                    res.put("data", resp.toString());
                    res.put("taskid", tid);
                    res.put("rowindex", rowindex);
                    ServerEventManager.publish("/projectplan/" + pid, res, this.getServletContext());
                //}
                // catch (Exception e){

                //}
                } else if (act.equals("putcolor")) {
                    try {
                        projdb.updateColorCode(conn, data, userid, pid);
                    } catch (Exception e) {
                    }
                }
                com.krawler.utils.json.base.JSONObject jbj = new com.krawler.utils.json.base.JSONObject();
                jbj.put("valid", "true");
                jbj.put("data", res1);
                out.print(jbj.toString());

                conn.commit();

            } catch (ServiceException ex) {
                DbPool.quietRollback(conn);
                out.println("{\"valid\": true, \"data\": Could not connect to database.}");
            } catch (JSONException jex) {
                DbPool.quietRollback(conn);
                out.println("{\"valid\": true, \"data\": Operation Failed. }");
            } finally {
                DbPool.quietClose(conn);
            }

        } else {
            out.println("{\"valid\": false}");
        }
%>
