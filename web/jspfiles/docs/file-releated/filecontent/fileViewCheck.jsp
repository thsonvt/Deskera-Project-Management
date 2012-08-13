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
<%--
    Document   : fileViewCheck
    Created on : May 7, 2012, 3:29:51 PM
    Author     : Abhay Kulkarni
    Purpose    : For checking in advance if the file is available for viewing as swf.
--%>

<%@page import="org.tmatesoft.svn.core.internal.wc.SVNFileUtil"%>
<%@page import="java.io.File"%>
<%@page import="java.util.Hashtable"%>
<%@page import="com.krawler.esp.handlers.FileHandler"%>
<%@page import="com.krawler.esp.database.dbcon"%>
<%@page import="com.krawler.utils.json.base.JSONException"%>
<%@page import="com.krawler.utils.json.base.JSONObject"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler"/>
<%
    JSONObject jbj = new JSONObject();
    try {
        if (sessionbean.validateSession(request, response)) {
            jbj.put("valid", true);
            String docID = request.getParameter("docid");
            JSONObject jobj = new JSONObject();

            boolean fileExists = FileHandler.isFileExists(docID, ".swf");
            jobj.put("exists", fileExists);

            Hashtable ht = dbcon.getfileinfo(request.getParameter("docid"));
            File fp = new File(com.krawler.esp.handlers.StorageHandler.GetDocStorePath(ht.get("storeindex").toString()) + "/" + ht.get("userid").toString() + "/" + ht.get("svnname"));
            String mimetype = SVNFileUtil.detectMimeType(fp);
            boolean isexe = SVNFileUtil.isExecutable(fp);
            if (isexe || mimetype != null) {
                String doctype = (String) ht.get("doctype");
                if (doctype.compareTo("Image") == 0) {
                    jobj.put("download", "no");
                } else {
                    jobj.put("download", "yes");
                }
            } else {
                jobj.put("download", "no");
            }

            boolean converted = dbcon.isConversionComplete(docID);
            jobj.put("converted", converted);

            jbj.put("data", jobj.toString());
        } else {
            jbj.put("valid", false);
        }
%>
<%=jbj.toString()%>
<%
    } catch (JSONException jex) {
        out.println("{}");
    }
%>
