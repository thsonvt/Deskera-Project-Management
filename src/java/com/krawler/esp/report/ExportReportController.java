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

import java.io.ByteArrayOutputStream;

import java.io.IOException;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.database.DbPool;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.krawler.common.util.KrawlerLog;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.krawler.esp.handlers.AuthHandler;

/**
 *
 * @author Kamlesh
 */
public class ExportReportController extends HttpServlet {

    protected void processRequest(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException, ServiceException {
//        clearAll();
        com.krawler.database.DbPool.Connection conn = null;

        try {
            conn = DbPool.getConnection();
            String reportName = request.getParameter("reportname");
            AbstractReportController rc = new ReportWithMTController(AuthHandler.getCompanyid(request), AuthHandler.getUserid(request));
            String fileExt = request.getParameter("type");
            ByteArrayOutputStream os = rc.exportReport(conn, request);
            if (os != null) {
                String filename = reportName + "." + fileExt;
                response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
                response.setContentType("application/octet-stream");
                response.setContentLength(os.size());
                response.getOutputStream().write(os.toByteArray());
                response.getOutputStream().flush();
            }
            os.close();
        } catch (ServiceException xex) {
            KrawlerLog.op.warn("Service Exception ExportProjectReport.java" + xex.toString());
            DbPool.quietRollback(conn);
        } catch (SessionExpiredException ex) {
            DbPool.quietRollback(conn);
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        } finally {
            DbPool.quietClose(conn);
        }

    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (ServiceException ex) {
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request
     *            servlet request
     * @param response
     *            servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (ServiceException ex) {
        }
    }

    /**
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }
    // </editor-fold>
}
