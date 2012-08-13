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
import com.krawler.common.util.StringUtil;
import com.krawler.esp.database.dbcon;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.svnwebclient.configuration.ConfigurationException;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author krawler
 * This servlet caters all download requests which need files to be downloaded in Zip format .
 */
public class ZipFileServlet extends HttpServlet {

    private static final int DEFAULT_BUFFER_SIZE = 1024;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException,ServiceException{
        response.setContentType("text/html;charset=UTF-8");

        try {
            String sep = StorageHandler.GetFileSeparator();
            Hashtable ht[] = dbcon.getfilesinfo(request.getParameter("url"));
            ZipOutputStream output = new ZipOutputStream(new BufferedOutputStream(response.getOutputStream(), DEFAULT_BUFFER_SIZE));
            for (int i = 0; i < ht.length; i++) {
                boolean docs = true;
                String type = request.getParameter("type");
                if (StringUtil.equal(type, "importedfiles")) {
                    docs = false;
                }
                String src = "";
                String fname = "";
                if (docs) {
                    src = StorageHandler.GetDocStorePath(ht[i].get("storeindex").toString()) + sep;
                    src = src + ht[i].get("userid").toString() + sep + ht[i].get("svnname");
                } else {
                    src = StorageHandler.GetDocStorePath();
                }
                response.setContentType("application/zip");
                response.setHeader("Content-Disposition", "attachment; filename=\"MyDocuments.zip\"");

                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                if (docs) {
                    fname = ht[i].get("docname").toString();
                    if (fname == null) {
                        continue;
                    }
                    InputStream input = null;
                    FileInputStream in = new FileInputStream(src);

                    output.putNextEntry(new ZipEntry(fname));
                    for (int length = 0; (length = in.read(buffer)) > 0;) {
                        output.write(buffer, 0, length);
                    }
                    output.closeEntry();
                    in.close();
                }
            }
            if (output != null) {
                try {
                    output.close();
                } catch (IOException logOrIgnore) {
                }
            }

        }catch (ConfigurationException ex) {
            Logger.getLogger(ZipFileServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            Logger.getLogger(ZipFileServlet.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (ServiceException ex) {
            Logger.getLogger(ZipFileServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (ServiceException ex) {
            Logger.getLogger(ZipFileServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
    //
