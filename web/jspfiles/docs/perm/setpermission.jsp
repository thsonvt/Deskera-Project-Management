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
<%@page import="com.krawler.esp.database.*" import="java.*"%>
<%@page import="com.krawler.common.session.SessionExpiredException"%>
<%@page import="com.krawler.esp.handlers.*"%>
<%@page import="java.util.*"%>
<%@page import="com.krawler.utils.json.base.JSONObject,com.krawler.common.util.KrawlerLog"%>
<%@page import="com.krawler.database.DbPool.Connection"%>
<%@page import="com.krawler.database.DbPool"%>
<%@page import="com.krawler.database.DbUtil"%>
<%@page import="com.krawler.esp.handlers.FilePermission"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="com.krawler.esp.handlers.AuthHandler"%>
<%@page import="com.krawler.utils.json.base.JSONException"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%
        JSONObject jbj = new JSONObject();
        JSONObject resp1 = new JSONObject();
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            if (sessionbean.validateSession(request, response)) {
                String docid = request.getParameter("docid");
                String userid = AuthHandler.getUserid(request);
                String userName = AuthHandler.getUserName(request);
                String companyid = AuthHandler.getCompanyid(request);
                String ipAddress = AuthHandler.getIPAddress(request);
                int auditMode = 0;
                String o = FilePermission.getDocumentOwner(conn, docid);
                if(!StringUtil.isNullOrEmpty(o) && userid.equals(o)){
                    try {
                        String pmode = request.getParameter("pmode");
                        String cmode = request.getParameter("cmode");
                        FilePermission.Delete(conn, docid);
                        if(cmode.equals("2")){
                            String[] cdata = request.getParameter("cdata").split(",");
                            for(int i = 0; i < cdata.length; i++){
                                String[] val = cdata[i].split("_");
                                int rw = 0;
                                if(Boolean.parseBoolean(val[1])){
                                    rw = 1;
                                }
                                FilePermission.Insert(conn, docid, val[0], "2", rw);
                            }
                        } else {
                            FilePermission.Insert(conn, docid, userid, cmode, 0);
                        }
                        if(pmode.equals("9")){
                            if(!StringUtil.isNullOrEmpty(request.getParameter("pdata"))){
                                String[] pdata = request.getParameter("pdata").split(",");
                                for(int i = 0; i < pdata.length; i++){
                                    String[] val = pdata[i].split("_");
                                    int rw = 0;
                                    if(Boolean.parseBoolean(val[1])){
                                        rw = 1;
                                    }
                                    FilePermission.Insert(conn, docid, val[0], "9", rw);
                                }
                           }
                        } else {
                            String[] pdata = projdb.getProjectListForChart(conn, userid).split(",");
                            for(int i = 0; i < pdata.length; i++){
                                FilePermission.Insert(conn, docid, pdata[i], pmode, 0);
                            }
                        }
                        String cPerm = "";
                        switch(Integer.parseInt(cmode)) {
                            case 1:
                                cPerm = "All My Connections";
                                break;
                            case 2:
                                cPerm = "Selected Connections";
                                break;
                            case 3:
                                cPerm = "Everyone";
                                break;
                            case 4:
                                cPerm = "None";
                                break;
                        }
                        String pPerm = "";
                        switch(Integer.parseInt(pmode)) {
                            case 10:
                                pPerm = "All My Projects";
                                break;
                            case 9:
                                pPerm = "Selected Projects";
                                break;
                            case 11:
                                pPerm = "None";
                                break;
                        }
                        FilePermission.updatePermission(conn, docid, cPerm, pPerm);
                        String params = AuthHandler.getAuthor(conn, userid) + " ("+ userName +"), " +
                             dbcon.getDocName(docid);
                        AuditTrail.insertLog(conn, "214", userid, docid, "",
                                companyid, params, ipAddress, auditMode);
                        jbj.put("valid", true);
                    } catch (Exception e) {
                        KrawlerLog.op.warn("Unable to Set Permission :" + e.toString());
                        resp1.put("res", 1);
                    } finally {
                        jbj.put("data", resp1.toString());
                    }
                } else {
                    jbj.put("valid", true);
                    jbj.put("error", "You do not have enough privilage to perform this operation");
                }
            } else {
                jbj.put("valid", false);
            }
%>
<%=jbj.toString()%>
<%
        } catch (JSONException jex) {
            out.println("{}");
        } finally {
            conn.commit();
            DbPool.quietClose(conn);
        }
%>
