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
package com.krawler.esp.servlets;

import com.krawler.common.service.ServiceException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.database.DbPool;
import com.krawler.esp.database.dbcon;
import com.krawler.esp.handlers.AuthHandler;
import com.krawler.esp.handlers.Forum;
import com.krawler.esp.handlers.SessionHandler;
import com.krawler.esp.handlers.projdb;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;

public class AdminHandler extends HttpServlet {

    private static final long serialVersionUID = -6529126913554103832L;
//    private static String msgUnSetModerator = "Your access as the moderator has been disabled for the project: %s.";
//    private static String msgDeactMem = "Your access to the project: %s has been deactivated.";
//    private static String msgSetModerator = "You have been assigned the role of moderator for the project: %s.";
//    private static String msgActMem = "Your access to the project: %s has been activated.";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, ServiceException {
        String result = "";
        boolean isFormSubmit = false;
        if (SessionHandler.isValidSession(request, response)) {

            try {
                String loginid = AuthHandler.getUserid(request);
                String companyid = AuthHandler.getCompanyid(request);
                String ipAddress = AuthHandler.getIPAddress(request);
                int auditMode = 0;
                String userName = AuthHandler.getUserName(request);
                String projectId = request.getParameter("projid");
                String uids = request.getParameter("uids");
                String[] userids = uids.split(",");
                String commid = request.getParameter("commid");
                String msg = "";
                JSONArray params = new JSONArray();
                boolean succ = false;
                if (request.getParameter("action").compareTo("0") == 0) {
                    int relid = 0, actionid = 0;
                    if (request.getParameter("emode").compareTo("0") == 0) {
                        relid = 3;
                        actionid = 43;
                    } else if (request.getParameter("emode").compareTo("1") == 0 || request.getParameter("emode").compareTo("2") == 0) {
                        relid = 0;
                    } else if (request.getParameter("emode").compareTo("4") == 0) {
                        relid = 2;
                    } else {
                        return;
                    }

//                    for (int i = 0; i < userids.length; i++) {
//                        dbcon.setStatusCommunity(userids[i], commid, relid, 0);
//                        if (actionid != 0) {
//                            dbcon.InsertLogForAdmin(loginid, commid, actionid, userids[i], 0);
//                        }
//                    }
                } else if (request.getParameter("action").compareTo("0") == 1) {
                    msg = "Setting for project - %s have been changed.";
                    String projName = dbcon.getProjectName(projectId);
                    msg = String.format(msg, projName);
                    int relid = 0, actionid = 0, flag = 0;
                    if (request.getParameter("emode").compareTo("0") == 0) {
                        relid = 3;
                        actionid = 44;
                        //msg = String.format(msgUnSetModerator, projName);
                        msg="msgUnSetModerator";
                        params.put(projName);
                    } else if (request.getParameter("emode").compareTo("1") == 0) {
                        relid = 0;
                    } else if (request.getParameter("emode").compareTo("2") == 0) {
                        relid = 0;
                        if (dbcon.chkModerator(userids, commid)) {
                            isFormSubmit = true;
                            result = "{\"valid\": true,\"data\":\"{\\\"error\\\":\\\"The project should have atleast one moderator.\\\",\\\"errorcode\\\":1}\"}";
                            return;
                        }
                        if (request.getParameter("chkFlag").compareTo("1") == 0 && dbcon.chkResourceDependency(userids, commid)) {
                            isFormSubmit = true;
                            result = "{\"valid\": true,\"data\":\"{\\\"error\\\":\\\"One or more member(s) you are trying to remove are assigned to tasks.<br><b>Do you want to reassign the tasks to others before removing?</b>\\\",\\\"errorcode\\\":2}\"}";
                            return;
                        }
                        if (request.getParameter("flagDrop").compareTo("1") == 0) {
                            flag = 1;
//                            msg = String.format(msgDeactMem, projName);
                            msg="msgDeactMem";
                            params.put(projName);
                        } else {
                            flag = 2;
                        }
                    } else if (request.getParameter("emode").compareTo("3") == 0) {
                        relid = 4;
//                        msg = String.format(msgSetModerator, projName);
                        msg="msgSetModerator";
                        params.put(projName);
                    } else if (request.getParameter("emode").compareTo("4") == 0) {
                        relid = 2;
                    } else if (request.getParameter("emode").compareTo("5") == 0) {
                        relid = 3;
                        flag = 1;
//                        msg = String.format(msgUnSetModerator, projName);
                        msg="msgUnSetModerator";
                        params.put(projName);
                    } else if (request.getParameter("emode").compareTo("6") == 0) {
                        relid = 21;
//                        msg = String.format(msgActMem, projName);
                        msg="msgActMem";
                        params.put(projName);
                    } else {
                        return;
                    }
                    result = dbcon.setStatusProject(userids, commid, relid, flag, loginid, companyid, actionid);
                    if (result.indexOf("true") > -1) {
                        succ = true;
                    }
//                    try{
//                        conn = DbPool.getConnection();
//                        for(int i = 0 ; i < userids.length ; i++){
//                            String params = AuthHandler.getAuthor(conn, loginid) + " ("+ userName +"), " +
//                                AuthHandler.getAuthor(conn, userids[i]) + ", " + projdb.getProjectName(conn, projectId) +"," +
//                                AdminServlet.getCompanyname(conn, companyid);
//                            AuditTrail.insertLog(conn, "152", loginid, userids[i], projectId,
//                                    companyid, params, ipAddress, auditMode);
//                        }
//                    } catch (ServiceException ex) {
//                            DbPool.quietRollback(conn);
//                    } finally {
//                            DbPool.quietClose(conn);
//                    }
                } else if (request.getParameter("action").compareTo("0") == 2) {
                    int planpermission = Integer.parseInt(request.getParameter("planpermission"));
                    result = dbcon.setPlanPermission(request, userids, commid, planpermission, loginid, companyid);
                    if (result.indexOf("true") > -1) {
                        succ = true;
                        String projName = dbcon.getProjectName(projectId);
                        params.put(projName).put(Forum.getPermissionsName(planpermission));
                        msg = "msgChangePerm";
                    }
                }
                /* Code for immediate effect to user*/
                if (succ && !StringUtil.isNullOrEmpty(msg)) {
                    Forum.publishUserActivities("projmoderator", userids, loginid, projectId, msg,params.toString(), true, this.getServletContext());
                }
            } catch (SessionExpiredException ex) {
                Logger.getLogger(AdminHandler.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (!isFormSubmit) {
                    try {
                        JSONObject jbj = new com.krawler.utils.json.base.JSONObject();
                        jbj.put("valid", "true");
                        jbj.put("data", result);
                        response.getWriter().println(jbj.toString());
                    } catch (JSONException ex) {
                        Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    response.getWriter().println(result);
                }
                response.getWriter().close();
            }


        } else { //session valid if() ends here

            response.getWriter().println("{\"valid\": false}");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (ServiceException ex) {
            Logger.getLogger(AdminHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (ServiceException ex) {
            Logger.getLogger(AdminHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /** 
     * Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }
    // </editor-fold>
}
