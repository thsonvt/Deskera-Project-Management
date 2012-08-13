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
<%@page import="com.krawler.esp.utils.ConfigReader"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.krawler.esp.handlers.ProfileHandler"%>
<%@ page import="com.krawler.common.util.URLUtil"%>
<%@ page import="com.krawler.utils.json.base.JSONObject"%>
<%@ page import="com.krawler.esp.handlers.AuthHandler"%>
<%@ page import="com.krawler.utils.json.base.JSONException"%>
<%@ page import="com.krawler.esp.database.dbcon"%>
<%@ page import="com.krawler.database.DbPool"%>
<%@ page import="com.krawler.esp.handlers.DashboardHandler"%>
<%@ page import="com.krawler.common.session.SessionExpiredException"%>
<%@ page import="com.krawler.common.util.StringUtil"%>
<%@ page import="com.krawler.common.service.ServiceException"%>
<%@ page import="com.krawler.esp.handlers.ServerEventManager"%>
<%@ page import="com.krawler.esp.handlers.CompanyHandler" %>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.HashMap"%>
<%@ page import="com.krawler.esp.web.resource.Links"%>

<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%
    com.krawler.database.DbPool.Connection conn = null;
	try {
        JSONObject jret = new JSONObject();
        JSONObject jbj = new JSONObject();
        String username = request.getRemoteUser();
        String result = "";
        String subdomain = URLUtil.getDomainName2(request);
        boolean isValidUser = false;
        conn = DbPool.getConnection();
        if (!StringUtil.isNullOrEmpty(username)) {
            boolean toContinue = true;
            if (sessionbean.validateSession(request, response)) {
                String companyidsession = AuthHandler.getCompanyid(request);
                String subdomainFromSession = CompanyHandler.getCompanySubdomainByCompanyID(conn, companyidsession);
                if( !subdomain.equalsIgnoreCase(subdomainFromSession)){
                    result = "alreadyloggedin";
                    toContinue = false;
                }
            }
            if(toContinue){
                jbj = AuthHandler.verifyLogin(conn, username, subdomain);
                if (jbj.has("success") && (jbj.get("success").equals(true))) {
                    sessionbean.createUserSession(request, response, jbj.getString("username"),
                            jbj.getString("lid"), jbj.getString("companyid"), jbj.getString("company"));
                    sessionbean.setLocale(request, response, jbj.optString("language", null));
                    conn.commit();
                    isValidUser = true;
                } else {
                    result = "noaccess";
                }
            }
        } else {
            if (sessionbean.validateSession(request, response)) {
                isValidUser = true;
                username =  AuthHandler.getUserName(request);
                jbj.put("username", username);
                jbj.put("companyid", AuthHandler.getCompanyid(request));
                jbj.put("company", AuthHandler.getCompanyName(request));
                isValidUser = true;
            }  else{
                result = "timeout";
            }
        }
        if (isValidUser) {
            jbj.put("lid", AuthHandler.getUserid(request));
            jbj.put("perm", DashboardHandler.getPermissions(conn,AuthHandler.getUserid(request)));
            jbj.put("modsub", DashboardHandler.getSubscriptionDetails(conn,AuthHandler.getUserid(request)));
            jbj.put("featureView", DashboardHandler.getFeaturesView(conn, AuthHandler.getCompanyid(request)));
            jbj.put("preferences", AuthHandler.getPreferences(conn,AuthHandler.getUserid(request),AuthHandler.getCompanyid(request)));
            jbj.put("validuseroptions", ProfileHandler.getUserAuthInfo(conn,request));
            jbj.put("superuser", DashboardHandler.isSuperUser(conn, AuthHandler.getCompanyid(request),AuthHandler.getUserid(request)));
            jbj.put("subdomain", subdomain);
            String fullname = ProfileHandler.getUserFullName(conn,request);
            jbj.put("fullname", (fullname == null || fullname == "") ? username : fullname );
            jbj.put("forum_base_url", URLUtil.baseForumURL);
            jbj.put("base_url", URLUtil.getPageURL(request, Links.loginpageFull_unprotected, subdomain));
            jbj.put("standalone", Boolean.parseBoolean(ConfigReader.getinstance().get("PMStandAlone")));

            jret.put("data", jbj.toString());
            jret.put("valid", true);
            try {
                String sid = AuthHandler.getUserid(request);
                Object[] remoteUserIds = dbcon.getFriendListArray(sid);
                for (int i = 0; i < remoteUserIds.length; i++) {
                    String contactUserId = remoteUserIds[i].toString();
                    JSONObject jpub = new JSONObject();

                    jpub.append("data", "{'data':[{mode :'online'},{status:'request',userid:'" + sid + "'}]}");
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("data", jpub.toString());

                    ServerEventManager.publish("/" + contactUserId + "/chat", data, this.getServletContext());
                }
            } catch (Exception e) {

            }
        } else {
            JSONObject j = new JSONObject();
            j.put("reason", result);
            jret.put("valid", false);
            jret.put("data", j);
        }
%>
<%=jret.toString()%>
<%
	} catch (JSONException e) {
        DbPool.quietRollback(conn);
        out.println("{\"valid\":false,\"data\":{}}");
    } catch(ServiceException sE){
        DbPool.quietRollback(conn);
        out.println("{\"valid\":false,\"data\":{}}");
    } finally {
        DbPool.quietClose(conn);
    }
%>
