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
<%@ page contentType="text/html"%>
<%@ page pageEncoding="UTF-8"%>
<%@ page language="java"%>
<%@ page import="java.util.*"%>
<%@ page import="com.krawler.esp.database.*"%>
<%@ page import="com.krawler.database.DbPool"%>
<%@ page import="com.krawler.database.DbPool.Connection"%>
<%@ page import="com.krawler.esp.handlers.DashboardHandler"%>
<%@ page import="com.krawler.common.service.ServiceException"%>
<%
    Connection conn = null;
    String ulogin = request.getParameter("uid");
    String data = "";
    try{
        conn = DbPool.getConnection();
        String lim = request.getParameter("limit");
        String off = request.getParameter("start");
        String type = request.getParameter("type");
        data = DashboardHandler.getChartURL(conn, ulogin, lim, off, type);
    } catch(ServiceException ex){
        
    } finally {
        DbPool.quietClose(conn);
    }
    out.println(data);
%>
