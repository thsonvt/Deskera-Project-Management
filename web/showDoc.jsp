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
    Document   : showDoc
    Created on : Apr 6, 2012, 4:38:34 PM
    Author     : Abhay
--%>

<%@page import="com.krawler.common.util.URLUtil"%>
<%@page import="com.krawler.esp.database.dbcon"%>
<%@page import="java.util.Hashtable"%>
<%@page import="com.krawler.esp.handlers.CompanyHandler"%>
<%@page import="com.krawler.utils.json.base.JSONObject"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="com.krawler.esp.handlers.FileHandler"%>
<%@page import="com.krawler.common.service.ServiceException"%>
<%@page import="com.krawler.common.session.SessionExpiredException"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="java.util.logging.Level"%>
<%@page import="com.krawler.svnwebclient.configuration.ConfigurationException"%>
<%@page import="java.io.File"%>
<%@page import="com.krawler.esp.handlers.StorageHandler"%>
<%@page import="com.krawler.database.DbUtil"%>
<%@page import="com.krawler.database.DbResults"%>
<%@page import="com.krawler.esp.handlers.AuthHandler"%>
<%@page import="com.krawler.database.DbPool"%>
<%@page import="com.krawler.database.DbPool.Connection"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler"/>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title></title>
        <link rel="shortcut icon" href="images/deskera.png" />
    </head>
    <body>
        <%
            Connection conn = null;
            boolean docFound = false, hasAccess = false, localSessionExists = false;
            String dest = "", docName = "Document is unavailable";
            String docid = "", loginID = "";
            String msg = "The document you are trying to access is not available. It may have been deleted from the system or the URL might be incorrect.";
            String companyid = null;
            try {
                conn = DbPool.getConnection();
                docid = request.getParameter("d");

                JSONObject jbj = new JSONObject();
                String username = request.getRemoteUser();
                String subdomain = URLUtil.getDomainName2(request);
                if (!StringUtil.isNullOrEmpty(username)) { // remote session exists
                    boolean toContinue = true;
                    if (sessionbean.validateSession(request, response)) { // check if local session exists
                        //result = "alreadyloggedin";
                        loginID = AuthHandler.getUserid(request);
                        localSessionExists = true;
                        toContinue = false;
                    }
                    if (toContinue) { // if does not exists, create a new local session. Else continue.
                        jbj = AuthHandler.verifyLogin(conn, username, subdomain);
                        if (jbj.has("success") && (jbj.get("success").equals(true))) {
                            sessionbean.createUserSession(request, response, jbj.getString("username"),
                                    jbj.getString("lid"), jbj.getString("companyid"), jbj.getString("company"));
                            //sessionbean.setLocale(request, response, jbj.optString("language", null));
                            conn.commit();
                            localSessionExists = true;
                            loginID = jbj.getString("lid");
                        } else {
                            msg = "You access creadentials cannot be validated to be authentic.";
                        }
                    }
                } else { // remote session does not exists
                    if (sessionbean.validateSession(request, response)) { // validate local session
                        loginID = AuthHandler.getUserid(request);
                        localSessionExists = true;
                    } else {
                        localSessionExists = false;
                        msg = "You do not have access to system as of now. Please log-in to the application and try again.";
                    }
                }

                if (!StringUtil.isNullOrEmpty(docid)) {
                    Hashtable ht = FileHandler.getfileinfo(conn, docid);
                    if (localSessionExists && !ht.isEmpty()) {
                        companyid = CompanyHandler.getCompanyByUser(conn, loginID);

                        String docOwnerId = ht.get("userid").toString();

                        boolean converted = dbcon.isConversionComplete(docid);
                        if (!converted) { // doc yet in queue
                            msg = "The Document you are trying to access is not available as of now. Please try after some time.";
                        } else {
                            dest = StorageHandler.GetDocStorePath().concat(StorageHandler.GetFileSeparator()).concat(docOwnerId).concat(StorageHandler.GetFileSeparator()).concat(docid).concat(".swf");
                            File f = new File(dest);
                            if (f.exists()) {
                                docFound = true;
                                dest = StorageHandler.GetFileSeparator().concat(docOwnerId).concat(StorageHandler.GetFileSeparator()).concat(docid);
                                docName = FileHandler.getDocName(conn, docid);
                            } else {
                                docName = "Document Not Found.";
                            }
                        }

                        if (docFound) {

                            String gridString = FileHandler.fillGrid(conn, loginID, "*flag*", "1", "1", companyid, new JSONObject().toString());

                            if (gridString.contains(docid)) {
                                hasAccess = true;
                            }
                            if (!hasAccess) {
                                docFound = false;
                                msg = "You do not have necessary permissions to access this document. Please contact your administrator.";
                            }
                        }
                    }
                } else {
                    msg = "Invalid document access link.";
                }
            } catch (ConfigurationException ex) {
                Logger.getLogger(getServletName()).log(Level.SEVERE, null, ex);
            } catch (ServiceException ex) {
                Logger.getLogger(getServletName()).log(Level.SEVERE, null, ex);
            } finally {
                DbPool.quietClose(conn);
            }

        %>
        <div id="header" style="text-align: center;border-bottom: 2px solid #BBBBBB;height: 35px;">
            <div id ="projectname" style="height:25px; padding:5px 5px 5px 0px; display:block;width:12%; float:left;"></div>
            <label style="float: left; font-family:tahoma,sans-serif; margin-left: 33%;"><%=docName%></label>
        </div>
        <%
            if (docFound) {
        %>
        <div style="height:700px;">
            <embed width="100%" height="100%" quality="high" bgcolor="#FFFFFF" wmode="transparent" name="krwdoc" id="krwdoc" style=""
                   src="content.stream?path=<%=docid%>&type=swf"
                   type="application/x-shockwave-flash"/>
        </div>
        <%
        } else {
        %>
        <div style="text-align: center; background-color:#267AC3;color:#FFFFFF;border-top:3px solid #D8DFEA;margin-top:10px;width:99%;font-family:Tahoma,sans-serif;padding:15px 0px 10px 10px;font-weight: bold;"><%=msg%></div>

        <%            }
            //out.close();
%>
        <script type="text/javascript">
            document.title = '<%=docName%>';
        </script>

    </body>
</html>
