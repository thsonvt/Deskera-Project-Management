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
import com.krawler.common.util.KWLErrorMsgs;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.Utilities;
import com.krawler.database.DbPool;
import com.krawler.database.DbPool.Connection;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.esp.handlers.SessionHandler;
import com.krawler.esp.project.task.TaskCPM;
import com.krawler.esp.project.task.TaskCPMDAO;
import com.krawler.esp.project.task.TaskCPMUtility;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
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
 * @author krawler
 */
public class ProjectCPAController extends HttpServlet {

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

    private String getPERTSheet(Connection conn, HttpServletRequest request)
            throws ServiceException, InstantiationException, IllegalAccessException, JSONException {
        if (!StringUtil.isNullOrEmpty(request.getParameter("projectid"))) {

            String result = request.getParameter("projectid");
            result = getPERTSheetData(conn, result);
            return result;

        } else {
            throw new IllegalArgumentException("One or more parameters in call to this method are missing :: getPERTSheet");
        }
    }

    private String getPERTSheetData(Connection conn, String projectID)
            throws ServiceException, InstantiationException, IllegalAccessException, JSONException {

        TaskCPMDAO taskCPMDao = TaskCPMUtility.getTaskCPM(TaskCPMUtility.getCPMWithPERTTypeID());
        List<TaskCPM> tasksCPValues = taskCPMDao.getCPMValuesOfProject(conn, projectID);
        String result = Utilities.listToGridJson(tasksCPValues, taskCPMDao.getTotalCount(), TaskCPM.class);
        return result;

    }

    private int savePERTSheet(Connection conn, HttpServletRequest request)
            throws ServiceException, InstantiationException, IllegalAccessException, JSONException {
        if (!StringUtil.isNullOrEmpty(request.getParameter("projectid")) && !StringUtil.isNullOrEmpty(request.getParameter("data"))) {

            String result = request.getParameter("projectid");
            String data = request.getParameter("data");
            int count = savePERTSheetData(conn, result, data);
            return count;

        } else {
            throw new IllegalArgumentException("One or more parameters in call to this method are missing :: savePERTSheet");
        }
    }

    private int savePERTSheetData(Connection conn, String projectID, String data)
            throws ServiceException, InstantiationException, IllegalAccessException, JSONException {

        TaskCPMDAO taskCPMDao = TaskCPMUtility.getTaskCPM(TaskCPMUtility.getCPMWithPERTTypeID());
        JSONArray ja = new JSONArray(data);
        int count = 0;
        for (int i = 0; i < ja.length(); i++) {
            JSONObject jo = ja.getJSONObject(i);
            TaskCPM taskCpm = TaskCPM.JSONtoObject(jo.toString());
            if (taskCpm != null) {
                boolean b = taskCPMDao.isTaskCPMPresent(conn, taskCpm.getTask().getTaskID());
                if (b) {
                    count += taskCPMDao.updateCPMValuesForTask(conn, taskCpm);
                } else {
                    count += taskCPMDao.setCPMValuesForTask(conn, taskCpm);
                }
            }
        }
        updateDefaultPERTDiffStatus(conn, projectID, PERTDiffStatus.TASK.getCode());
        return count;
    }

    private String getCPAWithoutPERT(Connection conn, HttpServletRequest request)
            throws InstantiationException, IllegalAccessException, ServiceException {
        if (!StringUtil.isNullOrEmpty(request.getParameter("projectid"))) {

            String result = request.getParameter("projectid");
            result = getCPAWithoutPERTData(conn, result);
            return result;

        } else {
            throw new IllegalArgumentException("One or more parameters in call to this method are missing :: getCPAWithoutPERT");
        }
    }

    private String getCPAWithoutPERTData(Connection conn, String projectID)
            throws InstantiationException, IllegalAccessException, ServiceException {

        TaskCPMDAO taskCPMDao = TaskCPMUtility.getTaskCPM(TaskCPMUtility.getCPMWithoutPERTTypeID());
        List<TaskCPM> taskCPMList = taskCPMDao.calculateCP(conn, projectID);
        String result = Utilities.listToGridJson(taskCPMList, taskCPMDao.getTotalCount(), TaskCPM.class);
        return result;
    }

    private String getCPAWithPERT(Connection conn, HttpServletRequest request)
            throws InstantiationException, IllegalAccessException, ServiceException, JSONException {
        if (!StringUtil.isNullOrEmpty(request.getParameter("projectid"))) {

            String result = request.getParameter("projectid");
            result = getCPAWithPERTData(conn, result);
            return result;

        } else {
            throw new IllegalArgumentException("One or more parameters in call to this method are missing :: getCPAWithPERT");
        }
    }

    private String getCPAWithPERTData(Connection conn, String projectID)
            throws InstantiationException, IllegalAccessException, ServiceException, JSONException {

        TaskCPMDAO taskCPMDao = TaskCPMUtility.getTaskCPM(TaskCPMUtility.getCPMWithPERTTypeID());
        List<TaskCPM> tasksCPValues = new ArrayList<TaskCPM>();
        tasksCPValues = taskCPMDao.calculateCP(conn, projectID);
        String str = Utilities.listToGridJson(tasksCPValues, taskCPMDao.getTotalCount(), TaskCPM.class);
        return str;
    }

    private String getProbability(Connection conn, HttpServletRequest request)
            throws InstantiationException, IllegalAccessException, ServiceException, JSONException {
        if (!StringUtil.isNullOrEmpty(request.getParameter("projectid")) && !StringUtil.isNullOrEmpty(request.getParameter("desiredduration"))
                && !StringUtil.isNullOrEmpty(request.getParameter("sumofvariance")) && !StringUtil.isNullOrEmpty(request.getParameter("sumofexpected"))) {

            String result = request.getParameter("projectid");
            double desiredDuration = Double.parseDouble(request.getParameter("desiredduration"));
            double sumOfVariance = Double.parseDouble(request.getParameter("sumofvariance"));
            double sumOfExpected = Double.parseDouble(request.getParameter("sumofexpected"));
            result = getProbability(conn, result, desiredDuration, sumOfExpected, sumOfVariance);
            return result;

        } else {
            throw new IllegalArgumentException("One or more parameters in call to this method are missing :: getFreeSlackWithoutPERT");
        }
    }

    private String getProbability(Connection conn, String projectID, double desiredDuration, double sumOfExpected, double sumOfVariance)
            throws InstantiationException, IllegalAccessException, ServiceException, JSONException {
        TaskCPMDAO taskCPMDAO = TaskCPMUtility.getTaskCPM(TaskCPMUtility.getCPMWithPERTTypeID());
        double val = taskCPMDAO.getProbability(conn, projectID, desiredDuration, sumOfExpected, sumOfVariance);
        JSONObject jo = new JSONObject();
        jo.put("success", true);
        jo.put("probability", val);
        return jo.toString();
    }

    private String getFreeSlackWithoutPERT(Connection conn, HttpServletRequest request)
            throws InstantiationException, ServiceException, IllegalAccessException {
        if (!StringUtil.isNullOrEmpty(request.getParameter("projectid"))) {

            String result = request.getParameter("projectid");
            result = getFreeSlackWithoutPERTData(conn, result);
            return result;

        } else {
            throw new IllegalArgumentException("One or more parameters in call to this method are missing :: getFreeSlackWithoutPERT");
        }
    }

    private String getFreeSlackWithoutPERTData(Connection conn, String projectID)
            throws InstantiationException, IllegalAccessException, ServiceException {

        TaskCPMDAO taskCpmDAO = TaskCPMUtility.getTaskCPM(TaskCPMUtility.getCPMWithoutPERTTypeID());
        List<TaskCPM> tasksCPM = taskCpmDAO.getFreeSlackPerTask(conn, projectID);
        String result = Utilities.listToGridJson(tasksCPM, taskCpmDAO.getTotalCount(), TaskCPM.class);
        return result;

    }

    private String getPERTDiff(Connection conn, HttpServletRequest request)
            throws InstantiationException, IllegalAccessException, ServiceException, JSONException {
        if (!StringUtil.isNullOrEmpty(request.getParameter("projectid"))) {

            String result = request.getParameter("projectid");
            result = getPERTDiffForProject(conn, result);
            return result;

        } else {
            throw new IllegalArgumentException("One or more parameters in call to this method are missing :: getPERTDiff");
        }
    }

    private String getPERTDiffForProject(Connection conn, String projectID) throws ServiceException, JSONException {

        ProjectDAO projectDAO = new ProjectDAOImpl();
        Project p = projectDAO.getProjectById(conn, projectID);
        return new JSONObject().put("data", p.toString()).toString();
    }

    private Map<String, Integer> getPERTDiffForCompany(Connection conn, String companyID) throws ServiceException, JSONException {

        DbResults rs = DbUtil.executeQuery(conn, "SELECT o_diff, p_diff FROM pertdefaults_company WHERE companyid = ?", companyID);
        Map<String, Integer> map = new HashMap<String, Integer>();
        if (rs.next()) {
            map.put("optimisticdiff", rs.getInt("o_diff"));
            map.put("pessimisticdiff", rs.getInt("p_diff"));
        }
        return map;
    }

    private int setDefaultPERTDiff(Connection conn, HttpServletRequest request)
            throws InstantiationException, IllegalAccessException, ServiceException {
        if (!StringUtil.isNullOrEmpty(request.getParameter("projectid"))
                && request.getParameter("optimisticdiff") != null && request.getParameter("pessimisticdiff") != null) {

            String result = request.getParameter("projectid");
            int o = Integer.parseInt(request.getParameter("optimisticdiff")), p = Integer.parseInt(request.getParameter("pessimisticdiff"));
            int count = setDefaultPERTDiffForProject(conn, o, p, result);
            return count;

        } else {
            throw new IllegalArgumentException("One or more parameters in call to this method are missing :: setDefaultPERTDiff");
        }
    }

    private int setDefaultPERTDiffForProject(Connection conn, int o_diff, int p_diff, String projectID)
            throws InstantiationException, IllegalAccessException, ServiceException {

        ProjectDAO projectDAO = new ProjectDAOImpl();
        int count = projectDAO.updatePERTDiffs(conn, o_diff, p_diff, projectID);
        updateDefaultPERTDiffStatus(conn, projectID, PERTDiffStatus.PROJECT.getCode());
        return count;
    }

    private int updateDefaultPERTDiffStatus(Connection conn, String projectID, int pertStatus) throws ServiceException {

        ProjectDAO projectDAO = new ProjectDAOImpl();
        return projectDAO.updatePERTStatus(conn, projectID, pertStatus);
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
