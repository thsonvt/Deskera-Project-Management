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
package com.krawler.esp.project.project;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.KWLErrorMsgs;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.Utilities;
import com.krawler.database.DbPool;
import com.krawler.database.DbPool.Connection;
import com.krawler.esp.handlers.AuthHandler;
import com.krawler.esp.handlers.SessionHandler;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Abhay
 */
public class ProjectController extends HttpServlet {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        if (SessionHandler.isValidSession(request, response)) {
            Connection conn = null;
            String result = KWLErrorMsgs.rsTrueNoData;
            try {
                String callMethod = (!StringUtil.isNullOrEmpty(request.getParameter("cm"))) ? request.getParameter("cm") : "";
                if (callMethod.compareTo("") != 0) {
                    conn = DbPool.getConnection();
                    Method declaredMethod = this.getClass().getDeclaredMethod(callMethod, Connection.class, HttpServletRequest.class);
                    Object returnDataObj = declaredMethod.invoke(this, conn, request);
                    if (returnDataObj instanceof String) {
                        result = returnDataObj.toString();
                    }
                }
                conn.commit();
            } catch (IllegalAccessException ex) {
                DbPool.quietRollback(conn);
                result = KWLErrorMsgs.rsFalseData + ex.getMessage() + "\"}";
            } catch (IllegalArgumentException ex) {
                DbPool.quietRollback(conn);
                result = KWLErrorMsgs.rsFalseData + ex.getMessage() + "\"}";
            } catch (InvocationTargetException ex) {
                DbPool.quietRollback(conn);
                result = KWLErrorMsgs.rsFalseData + ex.getTargetException().getMessage() + "\"}";
            } catch (ServiceException ex) {
                DbPool.quietRollback(conn);
                result = KWLErrorMsgs.rsFalseData + ex.getMessage() + "\"}";
            } catch (Exception ex) {
                DbPool.quietRollback(conn);
                result = KWLErrorMsgs.rsFalseData + ex.getMessage() + "\"}";
            } finally {
                DbPool.quietClose(conn);
                try {
                    JSONObject jbj = new JSONObject();
                    jbj.put("valid", "true");
                    jbj.put("data", result);
                    response.getWriter().println(jbj.toString());
                } catch (JSONException ex) {
                    Logger.getLogger(ProjectCPAController.class.getName()).log(Level.SEVERE, null, ex);
                }
                out.close();
            }
        } else {
            out.println("{\"valid\": false}");
            out.close();
        }
    }

    private String checkForNonModeratorProjects(Connection conn, HttpServletRequest request)
            throws SessionExpiredException, ServiceException, JSONException {
        String companyid = AuthHandler.getCompanyid(request);
        String result = checkForNonModeratorProjects(conn, companyid);
        return result;
    }

    private String checkForNonModeratorProjects(Connection conn, String companyid) throws ServiceException, JSONException {
        ProjectDAO pd = new ProjectDAOImpl();
        List<Project> projects = pd.getNonModeratorProjects(conn, companyid);
        JSONArray ja = new JSONArray();
        JSONObject jo = new JSONObject();
        for (Project project : projects) {
            List<ProjectMember> pms = pd.getProjectMembers(conn, project);
            List<ProjectMember> pms1 = new ArrayList<ProjectMember>(pms.size() + 1);
            pms1.addAll(pms);
            for (ProjectMember pm : pms) {
                if (!pm.isInUseFlag() || !(pm.getStatus() == Constants.PROJECT_MEMBER_STATUS_MEMBER || pm.getStatus() == Constants.PROJECT_MEMBER_STATUS_MODERATOR)) {
                    pms1.remove(pm);
                }
            }
            String s = Utilities.listToGridJson(pms1, pms1.size(), ProjectMember.class);
            JSONObject temp = new JSONObject(project.toString());
            temp.put("memberdata", s);
            ja.put(temp);
        }
        jo.put("data", ja);
        return jo.toString();
    }

    private String getProjectMembers(Connection conn, HttpServletRequest request) throws SessionExpiredException, ServiceException {
        if (!StringUtil.isNullOrEmpty(request.getParameter("projectid"))) {

            String result = request.getParameter("projectid");
            result = getProjectMembers(conn, result);
            return result;

        } else {
            throw new IllegalArgumentException("One or more parameters in call to this method are missing :: getProjectMembers");
        }
    }

    private String getProjectMembers(Connection conn, String projectID) throws ServiceException {
        ProjectDAO pd = new ProjectDAOImpl();
        List<ProjectMember> pms = pd.getProjectMembers(conn, projectID);
        String s = Utilities.listToGridJson(pms, pms.size(), ProjectMember.class);
        return s;
    }

    private int setProjectModerators(Connection conn, HttpServletRequest request) throws SessionExpiredException, ServiceException, JSONException {
        if (!StringUtil.isNullOrEmpty(request.getParameter("moderatorsJson"))) {

            String result = request.getParameter("moderatorsJson");
            int count = setProjectModerators(conn, result);
            return count;

        } else {
            throw new IllegalArgumentException("One or more parameters in call to this method are missing :: getProjectMembers");
        }
    }

    private int setProjectModerators(Connection conn, String moderatorsJson) throws ServiceException, JSONException {
        int count = 0;
        ProjectDAO pd = new ProjectDAOImpl();
        JSONObject data = new JSONObject(moderatorsJson);
        Iterator ite = data.keys();
        while (ite.hasNext()) {
            String projectID = (String) ite.next();
            String[] users = data.getString(projectID).split(",");
            for (int i = 0; i < users.length; i++) {
                String userID = users[i];
                count += pd.updateProjectMember(conn, projectID, userID, ProjectMemberStatus.MODERATOR.getCode(), ProjectMemberPlanPermission.MODIFY_ALL.getCode(), true, true, 0, false);
            }
        }
        return count;
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
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
     * Handles the HTTP <code>POST</code> method.
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
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
