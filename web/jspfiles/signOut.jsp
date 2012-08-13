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
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.krawler.common.session.SessionExpiredException"%>
<%@ page import="com.krawler.common.util.StringUtil" %>
<%@ page import="com.krawler.esp.database.*" %>
<%@ page import="com.krawler.esp.handlers.AuthHandler"%>
<%@ page import="com.krawler.utils.json.base.JSONObject,java.util.*"%>
<%@page import ="com.krawler.esp.handlers.*"%>
<%@ page import="com.krawler.esp.handlers.ServerEventManager"%>
<%@ page import="com.krawler.common.util.URLUtil"%>
<%@page import="java.net.URLEncoder"%>
<%@ page import="com.krawler.database.DbPool"%>
<%@ page import="com.krawler.database.DbPool.Connection"%>
<%@ page import="com.krawler.esp.web.resource.Links"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler"/>
<%
	String _sO = request.getParameter("type");
	String subdomain = URLUtil.getDomainName2(request);
	String uri = URLUtil.getPageURL(request, Links.loginpageFull, subdomain);
	String redirectUri = "";
	String logoutUrl = this.getServletContext().getInitParameter(
			"casServerLogoutUrl");
    com.krawler.database.DbPool.Connection conn = null;
    
	if (sessionbean.validateSession(request, response)) {
		try {
			conn = DbPool.getConnection();
			String loginid = AuthHandler.getUserid(request);
		    String userFullName = AuthHandler.getAuthor(conn, loginid);
		    String userName = AuthHandler.getUserName(request);
		    String companyid = AuthHandler.getCompanyid(request);
		    String ipAddress = AuthHandler.getIPAddress(request);
			if (loginid != null) {
                String sid = AuthHandler.getUserid(request);
				com.krawler.esp.handlers.ProfileHandler
						.updateUserStatus(sid, "offline");
                Object[] remoteUserIds = dbcon.getFriendListArray(sid);
                
                for (int i = 0; i < remoteUserIds.length; i++) {
                    String contactUserId = remoteUserIds[i].toString();
                    JSONObject jpub = new JSONObject();
                    jpub.append("data",
                            "{'data':[{mode :'offline'},{status:'request',userid:'"
                                    + sid + "'}]}");
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("data", jpub.toString());

                    ServerEventManager.publish("/" + contactUserId
                            + "/chat", data, this
                            .getServletContext());
                }
                String params = userFullName + " ("+ userName +"), ";
                AuditTrail.insertLog(conn, "12", loginid, "", "", companyid, params, ipAddress, 0);
                conn.commit();
			}
		} catch (Exception ex) {
            DbPool.quietRollback(conn);
			System.out.print(ex.toString());
		} finally {
            DbPool.quietClose(conn);
        }
	}
	if( StringUtil.isNullOrEmpty(logoutUrl)){
		redirectUri = uri + "login.html";
		if (!StringUtil.isNullOrEmpty(_sO)){
			redirectUri += ("?" + _sO);
		}
	}
	else{
		redirectUri = logoutUrl + String.format("?url=%s&subdomain=%s",URLEncoder.encode(uri, "UTF-8"), subdomain, _sO);
		if (!StringUtil.isNullOrEmpty(_sO)){
			redirectUri += ("&type=" + _sO);
		}
	}
	sessionbean.destroyUserSession(request, response);
	response.sendRedirect(redirectUri);
%>
