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
<%--<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@ page  language="java" %>
<%@ page  import="java.io.*"
          import="java.sql.*" 
          import="java.math.*"
          import="com.krawler.utils.json.*"%>
<%@ page import="com.krawler.esp.database.*" %>
<%@ page import="com.krawler.esp.handlers.Forum" %>

<%
    String ulogin = null;
    String upagesize = "0";
    String upageno = "0";
    String count="0";
    
    ulogin =request.getParameter("login");
    upagesize = request.getParameter("pageSize");
    upageno = request.getParameter("pageno");
    
    int offset = (Integer.parseInt(upagesize)*Integer.parseInt(upageno));
    int pagesize1 = Integer.parseInt(upagesize);
    
    String splitString=dbcon.getRelatedCommunities(ulogin,offset,pagesize1);
    out.println(splitString);
%>--%>
