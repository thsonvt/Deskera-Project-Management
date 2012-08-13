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
<%@page import="com.krawler.esp.database.*"%>
<%@page import = "com.krawler.esp.handlers.AuthHandler"%>
<%@ page import="com.krawler.esp.database.dbcon"%>
<%@ page import="com.krawler.database.DbPool"%>
<%@ page import="com.krawler.common.session.SessionExpiredException"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%
if (sessionbean.validateSession(request, response)) {
        String pid = request.getParameter("projid");
        String result = "";
        boolean flag = false;
        int action = Integer.parseInt(request.getParameter("action"));
        try {
            switch(action){
/*                case 1:
                    String projectMemberList = dbcon.getProjectMembers(pid, 100, 0);
                    dbcon.checkForProjectMeemberInProj_resource(pid, projectMemberList);
                    String projectResourceList = dbcon.getProjResources(pid);
                    out.print(projectResourceList);
                    break;*/
                case 2:
                    int limit = Integer.parseInt(request.getParameter("limit"));
                    int start = Integer.parseInt(request.getParameter("start"));
                    String ss = request.getParameter("ss");
                    result = dbcon.getProjectResources(pid, limit, start, ss);
                    //out.print(projectResourceList);
                    break;
                case 3:
                    result = dbcon.getResourceCategories();
                    //out.print(projectResourceList);
                    break;
                case 4:
                    flag = true;
                    result = dbcon.insertResource(request);
                    out.print(result);
                    break;
                case 5:
                    flag = true;
                    result = dbcon.editResource(request);
                    out.print(result);
                    break;
                case 6:
                    result = dbcon.deleteResource(request);
                    //out.print(projectResourceList);
                    break;
                case 7:
                    flag = true;
                    result = dbcon.createResType(request);
                    out.print(result);
                    break;
                case 8:
                    result = dbcon.setActive(request);
                    break;
                case 9:
                    result = dbcon.setBillable(request);
                    break;
                case 10:
                    result = dbcon.getProjectResources(pid);
                    //out.print(projectResourceList);
                    break;
                case 11: // create resource/member while importing plan
                    flag = true;
                    result = "{\"success\": false}";
                    com.krawler.database.DbPool.Connection conn =null;
                    try {
                        conn = DbPool.getConnection();
                        result =  com.krawler.esp.handlers.projdb.insertResource(conn,request);
                        if(result.equals("{\"success\": true}") && Boolean.parseBoolean(request.getParameter("creatememflag"))
                                && request.getParameter("typeid").equals("1")) { // check if resource type is member
                            result = "{\"success\": false}";
                            //TODO: call create resourse function : brajesh@090909
                        }
                        conn.commit();
                    } catch(Exception ex) {
                        DbPool.quietRollback(conn);
                    } finally {
                    DbPool.quietClose(conn);
                    }
                    out.print(result);
                    break;
                case 12: // resource conflict
                    result = dbcon.resourceConflict(request);
                    break;
//                case 12: // resource mapping while importing plan
//                    com.krawler.esp.servlets.FileImporterServlet.mapImportedRes(request);
//                    break;
                case 13:
                    result = dbcon.preDeleteResource(request);
                    //out.print(projectResourceList);
                    break;
                case 14: // resource conflict
                    result = dbcon.getTaskConflict(request);
                    break;
               case 15: // available resources after conflict
                    result = dbcon.availableResourcesInProject(request);
                    break;
               case 16:
                   result = dbcon.getAllResourcesInCompany(request);
                   break;
               case 17:
                    result = dbcon.getResourceTypes();
                    break;
               case 18:
                    String bid = request.getParameter("baselineid");
                    result = dbcon.getBaselineResources(bid);
                    break;
            }
	} catch (Exception e) {

	}
        if(!flag) {
            com.krawler.utils.json.base.JSONObject jbj = new com.krawler.utils.json.base.JSONObject();
            jbj.put("valid", "true");
            jbj.put("data", result);
            out.print(jbj.toString());
        }
} else {
    out.println("{\"valid\": false}");
}
%>
