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
        com.krawler.utils.json.*,
        com.krawler.esp.database.*" %>
<%@page import="com.krawler.utils.json.base.JSONObject"%>
<%@page import="com.krawler.esp.handlers.AuthHandler"%>
<%@page import="com.krawler.utils.json.base.JSONException"%>
<%@ page import="com.krawler.database.DbPool"%>
<%@ page import="com.krawler.database.DbPool.Connection"%>
<%@ page import="com.krawler.common.service.ServiceException"%>
<%@ page import="com.krawler.esp.handlers.projdb"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%
        com.krawler.database.DbPool.Connection conn = null;
	try {
                conn = DbPool.getConnection();
		JSONObject jbj = new JSONObject();
		if (sessionbean.validateSession(request, response)) {
			jbj.put("valid", true);
                        
                        String act = request.getParameter("action");
                        String taskid = request.getParameter("taskid");
                        if(act == null){
                            jbj.put("data", projdb.getPredecessor(conn, taskid));
                        }
                        /*else if(act.compareTo("insert") == 0){
                            try{
                                String r = dbcon.insertPredecessor(data,tid);
                                out.print(r);
                            }
                            catch (Exception e){

                            }
                        }*/
		} else {
			jbj.put("valid", false);
		}
%>
<%=jbj.toString()%>
<%
            //conn.commit();
	} catch (ServiceException ex) {
              DbPool.quietRollback(conn);
              out.println("{\"valid\":false,\"data\":{}}");
        } catch (JSONException jex) {
              DbPool.quietRollback(conn);
	      out.println("{}");
	} finally {
              DbPool.quietClose(conn);
        }
%>
