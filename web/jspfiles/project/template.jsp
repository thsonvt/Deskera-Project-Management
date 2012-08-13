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
<%@page import ="java.util.*"%>
<%@page import="com.krawler.esp.database.*" %>
<%@page import="com.krawler.common.util.KWLErrorMsgs"%>
<%@ page import="com.krawler.esp.handlers.AuthHandler"%>
<%@ page import="com.krawler.common.session.SessionExpiredException"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />

<%
if (sessionbean.validateSession(request, response)) {
        String result ="";
        String act = request.getParameter("action");
        String pid = request.getParameter("projid");
        String loginid = AuthHandler.getUserid(request);
        String userName = AuthHandler.getUserName(request);
        String companyid = AuthHandler.getCompanyid(request);
        String ipAddress = AuthHandler.getIPAddress(request);
        int auditMode = 0;
        if(act == null){
            String tid = request.getParameter("tempid");
            String r = dbcon.getTemplate(tid, loginid);
            com.krawler.utils.json.base.JSONArray jsonArray = new com.krawler.utils.json.base.JSONArray(r);
            String jString = "[";
            for (int k = 0; k < jsonArray.length(); k++) {
                com.krawler.utils.json.base.JSONObject jobj = jsonArray.getJSONObject(k);
                //UUID ud = new UUID(2312, 4123);
                //tid = ud.randomUUID().toString();
                //jobj.remove("taskid");
                //jobj.put("taskid", tid);
                /*if (!jobj.getString("parent").equals("0")) {
                    int recIndex = jobj.getInt("parent");
                    jobj.remove("parent");
                    jobj.put("parent", jsonArray.getJSONObject(recIndex - 1).getString("taskid"));
                }*/
                //dbcon.InsertTask(jobj, tid, pid, String.valueOf(startRow));
                jString = jString.concat(jobj.toString()).concat(",");
    //            startRow = startRow + 1;
    //            taskIDS += tid + ",";
            }
            jString = jString.substring(0, jString.length() - 1);
            jString = jString.concat("]");
            com.krawler.utils.json.base.JSONArray jArray = new com.krawler.utils.json.base.JSONArray(jString);
            String  griddata = "{data:"+jArray+"}";
            result = griddata;
        }
        else if(act.compareTo("getall") == 0){
            try {                
                result = dbcon.getAllTemplates(pid,AuthHandler.getCompanyid(request));
            } catch (Exception e) {
            }
        }
        else if(act.compareTo("getallReportTemp") == 0){
            try {
                result = dbcon.getAllReportTemplates();
            } catch (Exception e) {
            }
        }
        else if(act.compareTo("insert") == 0){
            try{
                String name = request.getParameter("name");
                String data = request.getParameter("data");
                String uid = request.getParameter("userid");
                String desc = request.getParameter("desc");
                UUID ud = new UUID(2312, 4123);
                String templateid = ud.randomUUID().toString();
                //com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject(data);
                result = dbcon.insertTemplate(templateid, data, name, uid, pid, desc, loginid);
                dbcon.InsertAuditLogForTemplate(templateid, userName, "114", loginid, companyid, ipAddress, auditMode);
                if(result.compareTo("") == 0)
                    result = KWLErrorMsgs.errTemplate;
            }
            catch (Exception e){
                System.out.print(e);
            }
        }
        else if(act.compareTo("getallReportTemp") == 0){
            try {
                result = dbcon.getAllReportTemplates();
            } catch (Exception e) {
            }
        }
        else if(act.compareTo("DeleteTemp") == 0){
            try {
                String tempid = request.getParameter("tempid");
                result = dbcon.deleteTemplate(tempid);
            } catch (Exception e) {
            }
        }
        else if(act.compareTo("saveReportTemplate") == 0){
            try{
                String name = request.getParameter("name");
                String data = request.getParameter("data");
                String uid = request.getParameter("userid");
                String desc = request.getParameter("desc");
                UUID ud = new UUID(2312, 4123);
                String templateid = ud.randomUUID().toString();
                //com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject(data);
                result = dbcon.insertReportTemplate(templateid,data,name,uid,desc);
                if(result.compareTo("") == 0)
                    result = KWLErrorMsgs.errTemplate;
            }
            catch (Exception e){
                System.out.print(e);
            }
        }
        com.krawler.utils.json.base.JSONObject jbj = new com.krawler.utils.json.base.JSONObject();
        jbj.put("valid", "true");
        jbj.put("data", result);
        out.print(jbj.toString());
} else {
    out.println("{\"valid\": false}");
}
%>
