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
package com.krawler.common.permission;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Utilities;
import com.krawler.database.DbPool;
import com.krawler.database.DbPool.Connection;
import com.krawler.esp.handlers.AuthHandler;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Vipin Gupta
 */
public class PermissionController extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Connection con = null;
        String res = "";
        try {
            con = DbPool.getConnection();
            PermissionDAO permdao = new PermissionDAOImpl();
            PermissionManager pm = new PermissionManager(permdao);
            if (request.getParameter("doAction") != null) {
                String action = request.getParameter("doAction");
                if (action.equalsIgnoreCase("assignAll")) {
                    res = assignAllPermission(con, request, pm);
                } else if (action.equalsIgnoreCase("getPermission")) {
                    res = getPermission(con, request, pm);
                } else if (action.equalsIgnoreCase("assignToMember")) {
                    res = assignPermissionToMember(con, request, pm);
                }
            }
            con.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(con);
            Logger.getLogger(PermissionController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            DbPool.quietRollback(con);
            Logger.getLogger(PermissionController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (JSONException ex) {
            DbPool.quietRollback(con);
            Logger.getLogger(PermissionController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            DbPool.quietClose(con);
        }
    }

    private String assignAllPermission(Connection con, HttpServletRequest request, PermissionManager pm) throws ServiceException, JSONException {
        String res = "";
        String userID = request.getParameter("userID");
        int i = pm.AssignAllPermissions(con, userID);
        if (i > 0) {
            res = "{\"msg\":\"Default Permission successfully Assigned\"}";
        }
        return res;
    }

    private String getPermission(Connection con, HttpServletRequest request, PermissionManager pm) throws SessionExpiredException, ServiceException, JSONException {
        String res = "";
        if (request.getParameter("who") != null) {
            String person = request.getParameter("who");
            if (person.equalsIgnoreCase("self")) {
                res = getUserPermission(con, AuthHandler.getUserid(request), pm);
            } else if (person.equalsIgnoreCase("members")) {
                res = getAllMembersPermission(con, request, pm);
            } else if (person.equalsIgnoreCase("member")) {
                res = getMemberPermission(con, request, pm);
            }
        }
        return res;
    }

    private String assignPermissionToMember(Connection con, HttpServletRequest request, PermissionManager pm) throws JSONException, ServiceException {
        String res = "";
        String memberID = request.getParameter("memberID");
        JSONObject jObj = new JSONObject(request.getParameter("modifiedFeature"));
        List<Feature> features = pm.getFeatureListFromJSONObject(jObj);
        int i = pm.setUserPermissions(con, features, memberID);
        if (i > 0) {
            res = "{\"msg\":\"Permission successfully Assigned\"}";
        }
        return res;
    }

    private String getUserPermission(Connection con, String userID, PermissionManager pm) throws ServiceException, JSONException {
        JSONObject userPermission = getUserPermissionJSON(con, userID, pm);
        return userPermission.toString();
    }

    private String getAllMembersPermission(Connection con, HttpServletRequest request, PermissionManager pm) throws SessionExpiredException, ServiceException, JSONException {
        String userid = AuthHandler.getUserid(request);
        List<String> memberIDs = null;
        JSONArray membersPermissions = new JSONArray();
        membersPermissions.put(getUserPermissionJSON(con, userid, pm));
        for (int i = 0; i < memberIDs.size(); i++) {
            JSONObject jObj = getUserPermissionJSON(con, memberIDs.get(i), pm);
            membersPermissions.put(jObj);
        }
        return membersPermissions.toString();
    }

    private String getMemberPermission(Connection con, HttpServletRequest request, PermissionManager pm) throws JSONException, ServiceException {
        String memberID = request.getParameter("memberID");
        List<Feature> features = pm.getFeaturePermissionsForUser(con, memberID);
        String feauresString = Utilities.listToGridJson(features, features.size(), Feature.class);
        return feauresString;
    }

    private JSONObject getUserPermissionJSON(Connection con, String userID, PermissionManager pm) throws JSONException, ServiceException {
        Map permission = pm.getUserPermissionList(con, userID);
        JSONObject userPermission = new JSONObject();
        JSONObject obj = new JSONObject(permission);
        userPermission.put("userID", userID);
        userPermission.put("permissions", obj);
        return userPermission;
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
