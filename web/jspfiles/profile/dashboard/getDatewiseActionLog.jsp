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
<%@ page import="com.krawler.esp.handlers.AuthHandler"%>
<%@ page import="com.krawler.utils.json.base.JSONArray"%>
<%@ page import="com.krawler.utils.json.base.JSONObject"%>
<%@ page import="com.krawler.utils.json.base.JSONException"%>
<%@ page import="com.krawler.common.session.SessionExpiredException"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%
	if (sessionbean.validateSession(request, response)) {
		try {
			String splitstring2 = null;
			String ulogin = null;
			JSONArray resJArray = new JSONArray();
			java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat(
					"yyyy-MM-d HH:mm:ss");
			String currDate = sdf1.format(new java.util.Date());
			ulogin = AuthHandler.getUserid(request);
			int totalCount = dbcon.getTotalActionlogDatediff(ulogin,
					currDate);
			Integer interval = 0;
			Integer dateCnt = 0;
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
					"MMMM d");
			JSONObject jobj = new JSONObject();
			while (dateCnt < 7 && totalCount >= interval) {
				if (interval == 365) {
					sdf = new java.text.SimpleDateFormat("MMMM d, yyyy");
				}
				splitstring2 = dbcon.getdatewiseactionlog(ulogin,
						currDate, interval);
				if (splitstring2.compareTo("{data:{}}") != 0) {
					jobj = new JSONObject();
					Calendar cal = Calendar.getInstance();
					cal.setTime(new java.util.Date());
					cal.add(Calendar.DATE, -interval);
					jobj.put("date", sdf.format(cal.getTime()));
					jobj.put("data", splitstring2);
					resJArray.put(jobj);
					dateCnt++;
				}
				interval += 1;
			}
			JSONObject resjobj = new JSONObject();
			resjobj.put("data", resJArray);
			//out.println(resjobj.toString());
                        com.krawler.utils.json.base.JSONObject jbj = new com.krawler.utils.json.base.JSONObject();
                        jbj.put("valid", "true");
                        jbj.put("data", resjobj.toString());
                        out.print(jbj.toString());
		} catch (SessionExpiredException sex) {
			out.println("{\"valid\": false}");
		} catch (JSONException jex) {
			out.println("{\"valid\": true,\"data\":[]}");
		}
	} else {
		out.println("{\"valid\": false}");
	}
%>
