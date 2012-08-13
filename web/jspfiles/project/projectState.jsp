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
          import="java.math.*"
          import="com.krawler.utils.json.*"%>
<%@ page import="com.krawler.esp.database.dbcon" %>
<%@ page import="com.krawler.esp.handlers.AuthHandler"%>
<%@ page import="com.krawler.common.session.SessionExpiredException"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%
    if (sessionbean.validateSession(request, response)) {
        int mode = Integer.parseInt(request.getParameter("mode"));
        String result="";
        try {
            String userid = request.getParameter("userid");
            String projid = request.getParameter("projectid");
            String featureid = request.getParameter("featureid");
            String planview = request.getParameter("planview");
            String statevar = request.getParameter("statevar");
            switch(mode){
                case 1 :
                    dbcon.saveProjState(userid, projid, featureid, planview, statevar);
                    break;
                case 2 :
                    result = dbcon.getProjState(userid, projid, featureid);
                    break;
            }
        } catch(Exception sex) {
            out.println("{\"data\": []}");
        }
        out.println(result);
    } else {
        out.println("{\"valid\": false}");
    }
%>
