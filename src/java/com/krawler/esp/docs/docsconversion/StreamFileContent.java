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
package com.krawler.esp.docs.docsconversion;

import com.krawler.esp.database.dbcon;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.svnwebclient.configuration.ConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Use this servlet to stream any file content (images, swf, attchments, text
 * etc.) without session.
 *
 * @param path path after doc store path
 * @param u requested by user id
 * @param type type of file content expected. Will be used for finding the file
 * by extention.
 *
 * @author Abhay Kulkarni
 */
public class StreamFileContent extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try {
            String path = request.getParameter("path");
            String type = request.getParameter("type");

            if (type.equals("swf")) {
                String storePath = StorageHandler.GetDocStorePath();

                Hashtable ht = dbcon.getfileinfo(path);
                path = storePath.concat(StorageHandler.GetFileSeparator()).concat(ht.get("userid").toString()).concat(StorageHandler.GetFileSeparator()).concat(path).concat(".swf");

                File fp = new File(path);

                String mimeType = this.getServletContext().getMimeType(path);
                if (mimeType == null) {
                    this.getServletContext().log("Could not get MIME type of " + path);
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    return;
                }
                response.setHeader("contentType", "application/x-shockwave-flash");
                response.setHeader("Content-Desposition", "inline");

                if (fp.exists()) {
                    response.setContentLength((int) fp.length());
                    FileInputStream in = new FileInputStream(fp);
                    OutputStream o = response.getOutputStream();
                    byte[] buf = new byte[4096];
                    int count = 0;
                    while ((count = in.read(buf)) >= 0) {
                        o.write(buf, 0, count);
                    }
                    in.close();
                    o.close();
                }
            }
        } catch (ConfigurationException ex) {
            Logger.getLogger(StreamFileContent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(StreamFileContent.class.getName()).log(Level.SEVERE, null, ex);
        }
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
