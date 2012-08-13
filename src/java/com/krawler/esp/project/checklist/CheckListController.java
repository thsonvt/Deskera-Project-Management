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
package com.krawler.esp.project.checklist;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Abhay Kulkarni
 */
public class CheckListController extends HttpServlet {

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
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
            DbPool.Connection conn = null;
            String result = KWLErrorMsgs.rsTrueNoData;
            try {
                String callMethod = (!StringUtil.isNullOrEmpty(request.getParameter("cm"))) ? request.getParameter("cm") : "";
                if (callMethod.compareTo("") != 0) {
                    conn = DbPool.getConnection();
                    Method declaredMethod = this.getClass().getDeclaredMethod(callMethod, DbPool.Connection.class, HttpServletRequest.class);
                    Object returnDataObj = declaredMethod.invoke(this, conn, request);
                    if (returnDataObj instanceof String) {
                        result = returnDataObj.toString();
                    }
                }
                conn.commit();
            } catch (IllegalAccessException ex) {
                DbPool.quietRollback(conn);
                logger.log(Level.SEVERE, ex.toString(), ex);
                result = KWLErrorMsgs.rsFalseData + ex.getMessage() + "\"}";
            } catch (IllegalArgumentException ex) {
                DbPool.quietRollback(conn);
                logger.log(Level.SEVERE, ex.toString(), ex);
                result = KWLErrorMsgs.rsFalseData + ex.getMessage() + "\"}";
            } catch (InvocationTargetException ex) {
                DbPool.quietRollback(conn);
                logger.log(Level.SEVERE, ex.getTargetException().toString(), ex);
                result = KWLErrorMsgs.rsFalseData + ex.getTargetException().getMessage() + "\"}";
            } catch (ServiceException ex) {
                DbPool.quietRollback(conn);
                logger.log(Level.SEVERE, ex.toString(), ex);
                result = KWLErrorMsgs.rsFalseData + ex.getMessage() + "\"}";
            } catch (Exception ex) {
                DbPool.quietRollback(conn);
                logger.log(Level.SEVERE, ex.toString(), ex);
                result = KWLErrorMsgs.rsFalseData + ex.getMessage() + "\"}";
            } finally {
                DbPool.quietClose(conn);
                try {
                    JSONObject jbj = new JSONObject();
                    jbj.put("valid", "true");
                    jbj.put("data", result);
                    response.getWriter().println(jbj.toString());
                } catch (JSONException ex) {
                    logger.log(Level.SEVERE, "JSON Exception while sending response", ex);
                }
                out.close();
            }
        } else {
            out.println("{\"valid\": false}");
            out.close();
        }
    }

    private CheckListManager getCheckListManager() {
        return (new CheckListManager());
    }

    private String getCheckLists(Connection conn, HttpServletRequest request) throws SessionExpiredException, ServiceException {

        manager = getCheckListManager();
        String companyID = AuthHandler.getCompanyid(request);

        List<CheckList> cll = manager.getCheckLists(conn, companyID);

        return Utilities.listToGridJson(cll, cll.size(), CheckList.class);
    }

    private void addCheckListCommon(Connection conn, HttpServletRequest request) throws SessionExpiredException, ServiceException, JSONException {

        manager = getCheckListManager();
        String companyID = AuthHandler.getCompanyid(request);
        String loginUserID = AuthHandler.getUserid(request);

        boolean isCheckList = Boolean.parseBoolean(request.getParameter("checklist"));
        String ID = "";
        if (isCheckList) {

            String checkListName = request.getParameter("checklistname");
            ID = manager.addCheckList(conn, companyID, checkListName, "", loginUserID);
        } else {

            String taskName = request.getParameter("taskname");
            String checkListID = request.getParameter("checklistid");
            ID = manager.addCheckListTask(conn, checkListID, taskName);
        }
//        return new JSONObject().put("data", ID).toString();
    }

    private void removeCheckList(Connection conn, HttpServletRequest request) throws ServiceException {

        manager = getCheckListManager();
        String checkListID = request.getParameter("checklistid");
        manager.removeCheckList(conn, checkListID);
    }

    private String getCheckListToDos(Connection conn, HttpServletRequest request) throws ServiceException {

        manager = getCheckListManager();
        String checkListID = request.getParameter("checklistid");
        String taskID = request.getParameter("taskid");

        JSONArray ja = manager.getAssociatedCheckListTasks(conn, checkListID, taskID);

        JSONObject jo = new JSONObject();
        try {
            jo.put("success", true);
            jo.put("data", ja);
        } catch (JSONException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return jo.toString();
    }

    private String updateCheckListToDoStatus(Connection conn, HttpServletRequest request) throws ServiceException {

        manager = getCheckListManager();
        String taskID = request.getParameter("taskid");
        int status = Integer.parseInt(request.getParameter("status"));

        status = manager.updateAssociatedCheckListTask(conn, taskID, status);

        JSONObject jo = new JSONObject();
        try {
            jo.put("success", true);
            jo.put("data", status);
        } catch (JSONException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return jo.toString();
    }

    private String isAssociatedWithAnyTask(Connection conn, HttpServletRequest request) throws ServiceException {

        manager = getCheckListManager();
        String checkListID = request.getParameter("checklistid");

        boolean assoc = manager.isAssociatedWithAnyTask(conn, checkListID);

        JSONObject jo = new JSONObject();
        try {
            jo.put("success", true);
            jo.put("data", assoc);
        } catch (JSONException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return jo.toString();

    }

    private String calculateAllTasks(Connection conn, HttpServletRequest request){
        manager = getCheckListManager();
        String companyID = request.getParameter("companyid");
        JSONObject jo = new JSONObject();
        try {
            manager.calculateAllTasksProgresses(conn, companyID);
            jo.put("success", true);
        } catch (JSONException ex) {
            logger.log(Level.SEVERE, "JSON Exception while resetting progress", ex);
        } catch (ServiceException ex) {
            logger.log(Level.SEVERE, "Service Exception while resetting progress", ex);
        }
        return jo.toString();
    }

    private CheckListManager manager;
    private static final Logger logger = Logger.getLogger(CheckListController.class.getName());

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
