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
<%@page language="java" %>
<%@ page import="com.krawler.esp.database.*" %>
<%@ page import="com.krawler.esp.handlers.*" %>
<%
    if (SessionHandler.isValidSession(request,response)) {
        /*is form submit*/
        int action = Integer.valueOf(request.getParameter("action"));
        if(action == 1) {
            out.println(dbcon.updateProfile(request));

        } else if(action == 2) {
            String platformURL = this.getServletContext().getInitParameter("platformURL");
            out.println(dbcon.changePassword(platformURL,request));
        }

    } else {
          out.println("{\"valid\": false}");
    }
%>
