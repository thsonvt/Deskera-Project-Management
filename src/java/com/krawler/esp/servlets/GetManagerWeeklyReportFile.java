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

import com.krawler.common.util.StringUtil;
import com.krawler.database.DbPool;
import com.krawler.database.DbPool.Connection;
import com.krawler.esp.company.Company;
import com.krawler.esp.company.CompanyDAO;
import com.krawler.esp.company.CompanyDAOImpl;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.esp.user.UserDAO;
import com.krawler.esp.user.UserDAOImpl;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to be accessed from /b/<subdomain> URL with a parameter u=<username>
 * Returns the Weekly Manager Report CSV created through {@link generateManagerReportFiles.jsp}
 * 
 * @author Abhay Kulkarni
 */
public class GetManagerWeeklyReportFile extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        boolean error = false;
        Connection conn = null;
        String result = "";
        try {
            conn = DbPool.getConnection();
            String userName = request.getParameter("u");

            if (!StringUtil.isNullOrEmpty(userName)) {
                String subdomain = request.getParameter("cdomain");
                if (!StringUtil.isNullOrEmpty(subdomain)) {

                    CompanyDAO cd = new CompanyDAOImpl();
                    Company c = cd.getCompanyBySubdomain(conn, subdomain);
                    if (c != null) {
                        UserDAO ud = new UserDAOImpl();
                        String userID = ud.getUserID(conn, userName, c.getCompanyID());
                        if (!StringUtil.isNullOrEmpty(userID)) {

                            String fileName = subdomain.toLowerCase() + "_" + userName.toLowerCase() + ".csv";

                            File fp = new File(StorageHandler.GetDocStorePath()
                                    + StorageHandler.GetFileSeparator()
                                    + "dailyManagerReports"
                                    + StorageHandler.GetFileSeparator()
                                    + fileName);
                            if (fp.exists()) {

                                byte[] buff = new byte[(int) fp.length()];
                                FileInputStream fis = new FileInputStream(fp);
                                fis.read(buff);

                                response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
                                response.setContentType("application/octet-stream");
                                response.setContentLength((int) fp.length());
                                response.getOutputStream().write(buff);
                                response.getOutputStream().flush();
                                fis.close();

                            } else {
                                error = true;
                                result = "File not found. Please try again tomorrow.";
                            }
                        } else {
                            error = true;
                            result = "User not found. Please append a valid user name in the URL";
                        }
                    } else {
                        error = true;
                        result = "Please append a valid user name in the URL";
                    }
                } else {
                    error = true;
                    result = "Invalid URL. Please check your company domain name.";
                }
            } else {
                error = true;
                result = "Please append a valid user name in the URL.";
            }
        } catch (Exception ex) {
            DbPool.quietRollback(conn);
            Logger.getLogger(GetManagerWeeklyReportFile.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (error) {
                response.getWriter().print(result);
            }
            DbPool.quietClose(conn);
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
