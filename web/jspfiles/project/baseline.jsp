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
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.krawler.common.session.SessionExpiredException"%>
<%@page import="com.krawler.esp.handlers.projdb"%>
<%@page import="com.krawler.esp.database.dbcon"%>
<%@page import="com.krawler.esp.handlers.AuthHandler"%>
<%@page import="com.krawler.utils.json.base.JSONObject"%>
<%@page import="com.krawler.common.util.KWLErrorMsgs"%>
<%@page import="com.krawler.common.service.ServiceException"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%
String returnStr = "";
if (sessionbean.validateSession(request, response)){
    int action = Integer.parseInt(request.getParameter("action"));
    String pid = request.getParameter("projectid");
    JSONObject jbj = new JSONObject();
    boolean formSubmit = false;
    try{
        String loginid = AuthHandler.getUserid(request);
        switch(action){
            case 1:     //Get projects all baselines
                returnStr = dbcon.getbaseline(pid, loginid);
                break;
            case 2:     //Get baseline data
                returnStr = dbcon.getBaselineData(request.getParameter("baselineid"), request.getParameter("projectid"), loginid);
                break;
            case 3:     //delete baseline
                returnStr = KWLErrorMsgs.rsSuccessFalse;
                String baselineId = request.getParameter("baselineid");
                String result = "";
                result = dbcon.deleteBaseline(pid, baselineId);
                if(result.compareTo("") != 0) {
                    returnStr = KWLErrorMsgs.rsSuccessTrue;
                }
                break;
            case 4:     //create new baseline
                returnStr = KWLErrorMsgs.rsSuccessFalse;
                String bId = request.getParameter("baselineid");
                String baselineName = request.getParameter("baselinename");
                String baselineDescription = request.getParameter("baselinedescription");
                String userId = request.getParameter("userid");
                boolean isUnderLimit = dbcon.checkBaseline(pid);
                if(isUnderLimit) {
                    returnStr = dbcon.saveBaseline(pid, userId, baselineName, baselineDescription);
                } else if(bId.compareTo("-1") == 0) {
                    bId = dbcon.deleteBaseline(pid, bId);
                    if(bId.compareTo("") != 0) {
                        returnStr = dbcon.saveBaseline(pid, userId, baselineName, baselineDescription);
                        if(returnStr.compareTo(KWLErrorMsgs.rsSuccessTrue) == 0) {
                            returnStr = String.format("{\"success\":true,\"data\": [{\"baselineid\":\"%s\"}]}", bId);
                        }
                    } else {
                        returnStr = KWLErrorMsgs.rsSuccessFalse;
                    }
                } else {
                    returnStr = "{\"success\":false,\"data\": \"delete\"}";
                }
                break;
            case 5:     //Get comparision data for comparision
                userId = request.getParameter("userid");
                returnStr = dbcon.getCompareBaselineData(request.getParameter("baselineid"), request.getParameter("projectid"), loginid);
                break;
        }
        if(!formSubmit){
            jbj.put("valid", true);
           // jbj.put("success", true);
            jbj.put("data", returnStr);
        }
    } catch(ServiceException e){
        jbj.put("valid", true);
        jbj.put("data", "{\"success\": false, \"errormsg\": \"Error occurred at server.\"}");
    }
    returnStr = jbj.toString();
} else {
    returnStr = "{\"valid\": false}";
}
%>
<%= returnStr %>
