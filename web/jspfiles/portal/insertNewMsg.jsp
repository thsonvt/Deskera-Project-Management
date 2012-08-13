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
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page pageEncoding="UTF-8"%>
<%@ page import="com.krawler.esp.portalmsg.*" %>
<%@ page import="com.krawler.esp.database.dbcon" %>
<%@ page import=" com.krawler.esp.handlers.ServerEventManager"%>
<%@ page import=" com.krawler.esp.handlers.AuthHandler"%>
<%@ page import="com.krawler.utils.json.base.JSONObject"%>
<%@ page import="com.krawler.utils.json.base.JSONArray"%>
<%@ page import="com.krawler.utils.json.base.JSONException"%>
<%@page import="com.krawler.common.session.SessionExpiredException"%>
<%@page import="com.krawler.common.util.*"%>
<%@page import = "org.apache.commons.fileupload.*"%>
<%@page import = "com.krawler.common.service.*"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />

<%
com.krawler.utils.json.base.JSONObject jbj = new com.krawler.utils.json.base.JSONObject();
if (sessionbean.validateSession(request, response)) {
    jbj.put("valid", true);         
         
          String type = request.getParameter("type");
          String companyid = AuthHandler.getCompanyid(request);
          String sendflag = "";
          if(type.compareTo("Mail")==0) {
              int draft=Integer.parseInt(request.getParameter("draft"));
               sendflag = request.getParameter("sendflag");
               String msgFor = "";
               String[] tos = {};
               if(sendflag.compareTo("reply")==0) {               
                    msgFor =forummsgdbcon.getUserFromPost(request.getParameter("repto"));
               }
               else if(sendflag.compareTo("newmsg")==0) {
                    msgFor= request.getParameter("repto");
                    tos = msgFor.split(";");
               }
              String insertMsg = forummsgdbcon.insertMailMsg(request,companyid);
                JSONObject jpub = new JSONObject();                
                jpub.append("data",insertMsg);
                Map<String, String> data = new HashMap<String, String>();
                data.put("data", jpub.toString()); 
                data.put("module", "mail");
                if((draft>1)){
                     if(tos.length >= 1){
                        for(int i = 0; i<tos.length; i++){
                            String msgForid = dbcon.getUserid(tos[i],AuthHandler.getCompanyid(request));
                            msgForid = (!msgForid.equals(""))?msgForid:msgFor;
                            ServerEventManager.publish("/"+msgForid+"/inbox", data, this.getServletContext());
                            ServerEventManager.publish("/useractivities/" + msgForid, data, this.getServletContext());
                        }
                    } else {
                        ServerEventManager.publish("/"+msgFor+"/inbox", data, this.getServletContext());
                        ServerEventManager.publish("/useractivities/" + msgFor, data, this.getServletContext());
                    }
                }
                 out.println("{\"success\": true,\"data\":"+insertMsg+"}");                         
          } else {
            String insertMsg = forummsgdbcon.insertForumPost(request);
            JSONObject jpub = new JSONObject();
            jpub.append("data",insertMsg);
            sendflag = request.getParameter("sendflag");
            int firstReply = Integer.parseInt(request.getParameter("firstReply"));
            Map<String, String> data = new HashMap<String, String>();
            data.put("data", jpub.toString()); 
             ServerEventManager.publish("/"+request.getParameter("groupId")+"/forum", data, this.getServletContext());
            out.println("{\"success\": true,\"data\":"+insertMsg+"}");

            JSONObject jobj = new JSONObject(insertMsg);
            JSONObject jDetails = jobj.getJSONArray("data").getJSONObject(0);
            if(jDetails.getString("Success").compareTo("Success") == 0){
                String companyId = AuthHandler.getCompanyid(request);
                String loginid = AuthHandler.getUserid(request);
                String userName = AuthHandler.getUserName(request);
                String ipAddress = AuthHandler.getIPAddress(request);
                String projectId = request.getParameter("groupId");
                String topicId = jDetails.getString("ID");
                String details = jDetails.getString("Subject");
                String params = details;
                int mode = 0;
                if(sendflag.compareTo("reply")==0) {
                    if(firstReply != 0){
                        String pid = jDetails.getString("Parent");
                        String parentSub = "";
                        if(pid.contains("topic")){
                            pid = pid.substring(5);
                            parentSub = Forum.getPostSubject(pid, "topic");
                        } else {
                            parentSub = Forum.getPostSubject(pid, "post");
                        }
                        params = params.concat(", ").concat(parentSub);
                    }
                    dbcon.InsertAuditLog("142", loginid, topicId, projectId, companyId, params, ipAddress, userName, mode);
                }else{
                    if(firstReply == 0)
                        topicId = topicId.substring(5);
                    dbcon.InsertAuditLog("141", loginid, topicId, projectId, companyId, params, ipAddress, userName, mode);
                }
            }
        }

} else {
	out.println("{\"valid\": false}");
}
%>
