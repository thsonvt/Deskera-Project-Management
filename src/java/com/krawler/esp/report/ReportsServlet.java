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
package com.krawler.esp.report;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.KWLErrorMsgs;
import com.krawler.common.util.StringUtil;
import com.krawler.database.DbPool;
import com.krawler.database.DbPool.Connection;
import com.krawler.esp.handlers.AuthHandler;

import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Kamlesh Kumar
 */
public class ReportsServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Connection conn = null;
        Object result = null;
        String mname = "";
        AbstractReportController factory = null;
        try {
            conn = DbPool.getConnection();
            if (!StringUtil.isNullOrEmpty(request.getParameter("m"))) {   // m is method name
                mname = request.getParameter("m");
                factory = new ReportWithMTController(AuthHandler.getCompanyid(request), AuthHandler.getUserid(request));
                Method methodObj = factory.getClass().getMethod(mname, new Class[]{Connection.class, HttpServletRequest.class});
                result = methodObj.invoke(factory, conn, request);
                conn.commit();
            }
        } catch (SessionExpiredException ex) {
            DbPool.quietRollback(conn);
            result = KWLErrorMsgs.rsFalseData + ex.getMessage() + "\"}";
        } catch (IllegalArgumentException ex) {
            DbPool.quietRollback(conn);
            result = KWLErrorMsgs.rsFalseData + ex.getMessage() + "\"}";
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            result = KWLErrorMsgs.rsFalseData + ex.getMessage() + "\"}";
        } catch (InvocationTargetException ex) {
            DbPool.quietRollback(conn);
            result = KWLErrorMsgs.rsFalseData + ex.getTargetException().getMessage() + "\"}";
        } catch (IllegalAccessException ex) {
            DbPool.quietRollback(conn);
            result = KWLErrorMsgs.rsFalseData + ex.getMessage() + "\"}";
        } catch (NoSuchMethodException ex) {
            DbPool.quietRollback(conn);
            result = KWLErrorMsgs.rsFalseData + ex.getMessage() + "\"}";
        } catch (SecurityException ex) {
            DbPool.quietRollback(conn);
            result = KWLErrorMsgs.rsFalseData + ex.getMessage() + "\"}";
        } finally {
            DbPool.quietClose(conn);
            try {
                if (!factory.isFormSubmit()) {
                    JSONObject jbj = new com.krawler.utils.json.base.JSONObject();
                    jbj.put("valid", "true");
                    jbj.put("data", result.toString());
                    out.println(jbj.toString());
                } else {
                    out.println(result);
                }
            } catch (JSONException ex) {
                Logger.getLogger(ReportsServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
            out.close();
        }
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
