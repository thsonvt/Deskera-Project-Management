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
<%@ page import="com.krawler.esp.database.dbcon"%>
<%@ page import="com.krawler.esp.handlers.AuthHandler"%>
<%@ page import="com.krawler.esp.portalmsg.Forum"%>
<%@ page import="com.krawler.esp.portalmsg.forummsgdbcon"%>
<%@page import="com.krawler.common.session.SessionExpiredException"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />

<%
com.krawler.utils.json.base.JSONObject jbj = new com.krawler.utils.json.base.JSONObject();
if (sessionbean.validateSession(request, response)) {
    jbj.put("valid", true);
    String result = "";
    String flag=request.getParameter("flag");
    if(flag.compareTo("delmsg")==0){
        String id = request.getParameter("deleteId");
        String deleteMsg[] =id.split(",");
        String subs[] = new String[deleteMsg.length];
        for(int cntId = 0; cntId < deleteMsg.length; cntId++) {
            String tempid = deleteMsg[cntId];
            if(deleteMsg[cntId].contains("topic")) {
                tempid = tempid.substring(5);
                subs[cntId] = Forum.getPostSubject(tempid, "topic");
            } else {
                subs[cntId] = Forum.getPostSubject(tempid, "post");
            }
        }
        String delOut= forummsgdbcon.deleteMsg(deleteMsg);
        result = delOut;
        if(result.indexOf("success") >= 0 && result.indexOf("true") >= 0) {
            for(int cntId = 0; cntId < deleteMsg.length; cntId++) {
                String companyId = AuthHandler.getCompanyid(request);
                String loginid = AuthHandler.getUserid(request);
                String userName = AuthHandler.getUserName(request);
                String ipAddress = AuthHandler.getIPAddress(request);
                String projectId = request.getParameter("groupId");
                String topicId = deleteMsg[cntId];
                String params = subs[cntId];
                int mode = 0;
                dbcon.InsertAuditLog("143", loginid, topicId, projectId, companyId, params, ipAddress, userName, mode);
            }
        }
        //out.print(delOut);
    }
    else if(flag.compareTo("forumflag")==0){
        String postid = request.getParameter("postid");
        String userid = AuthHandler.getUserid(request);
        int flag1=Integer.parseInt(request.getParameter("value"));
        //out.print(Forum.markForumFlag(userid, postid, flag1));
        result = Forum.markForumFlag(userid, postid, flag1);
    }
    jbj.put("data",result);
    out.println(jbj.toString());
} else {
	out.println("{\"valid\": false}");
}
        
%> 
