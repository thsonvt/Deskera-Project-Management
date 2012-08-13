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
import com.krawler.common.session.SessionExpiredException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.krawler.common.util.KrawlerLog;
import com.krawler.common.util.StringUtil;
import com.krawler.database.DbPool;
import com.krawler.esp.database.dbcon;
import com.krawler.esp.handlers.AuditTrail;
import com.krawler.esp.handlers.AuthHandler;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.svnwebclient.configuration.ConfigurationException;
import com.krawler.svnwebclient.configuration.ConfigurationProvider;

public class FileDownloadServlet extends HttpServlet {
	private static final long serialVersionUID = -7262043406413106392L;

	protected void processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, ServiceException{
		try {
            boolean docs = true;
            String type = request.getParameter("type");
            if(StringUtil.equal(type, "importedfiles"))
                docs = false;
            String sep = StorageHandler.GetFileSeparator();
            String src = "";
            Hashtable ht = null;
            String userName = AuthHandler.getUserName(request);
            String companyid = AuthHandler.getCompanyid(request);
            String ipAddress = AuthHandler.getIPAddress(request);
            String userid=AuthHandler.getUserid(request);
            int auditMode = 0;
            String fname = "";
            if(docs){ 
                ht = dbcon.getfileinfo(request.getParameter("url"));
                // File fp = new
                // File(ConfigurationProvider.getInstance().getTempDirectory() +
                // "/56a08913-f7df-4409-93fe-ba5c8f31f6fe/"+ht.get("svnname"));
                // File fp = new File(
                // com.krawler.esp.handlers.StorageHandler.GetDocStorePath(ht.get("storeindex").toString())
                // + "/56a08913-f7df-4409-93fe-ba5c8f31f6fe/"+ht.get("svnname"));
                src = StorageHandler.GetDocStorePath(ht.get("storeindex").toString())+ sep;
                if (request.getParameter("mailattch") != null) {
                    src = src+"attachment"+ sep + ht.get("svnname");
                } else {
                     src = src+ ht.get("userid").toString()+ sep + ht.get("svnname");
                }
            } else {
                src = com.krawler.esp.handlers.StorageHandler.GetDocStorePath();
                String fid = request.getParameter("fileid");
                String format = request.getParameter("format");
                fname = request.getParameter("filename");
                String module = request.getParameter("module").toLowerCase();

                if(module.contains("plan")){
                    src = src + sep + "importplans";
                }else if(module.contains("contact")){
                    src = src + sep + "importcontacts"+sep+userid;
                }

                if(format.contains("original")) {
                    src = src + sep + fid;
                } else if (format.contains("error")) {
                    src = src + sep +"error_" + fid;
                    fname = "Errors_in_" + fname;
                } else if(format.contains("rejected")) {
                    src = src + sep +"reject_" + fid;
                    fname = "Rejected_from_" + fname;
                }
            }
            
			File fp = new File(src);
			byte[] buff = new byte[(int) fp.length()];
			FileInputStream fis = new FileInputStream(fp);
			int read = fis.read(buff);
            
            if(docs){
                fname = ht.get("docname").toString();
                javax.activation.FileTypeMap mmap = new javax.activation.MimetypesFileTypeMap();
                // response.setContentType(mmap.getContentType(ConfigurationProvider.getInstance().getTempDirectory()
                // + "/56a08913-f7df-4409-93fe-ba5c8f31f6fe/"+ht.get("svnname")));
                response.setContentType(mmap.getContentType(ConfigurationProvider
                        .getInstance().getTempDirectory()
                        + sep
                        + ht.get("userid").toString()
                        + sep
                        + ht.get("svnname")));
                response.setHeader("Content-Disposition", request.getParameter("dtype") + "; filename=\"" + fname + "\";");
            } else {
                response.setHeader("Content-Disposition", "attachment; filename=\"" + fname +"\"");
                response.setContentType("application/octet-stream");
            }
            
            response.setContentLength((int) fp.length());
            response.getOutputStream().write(buff);
            response.getOutputStream().flush();

            if(docs){
                com.krawler.database.DbPool.Connection conn = null;
                try {
                    conn = DbPool.getConnection();
                    String documentid  = ht.get("svnname").toString();
                    String loginid = ht.get("userid").toString();
                    String docId = "";
                    if(documentid.contains("."))
                        docId = documentid.substring(0,documentid.indexOf("."));
                    String params = AuthHandler.getAuthor(conn, loginid) + " ("+ userName +"), " +
                                     ht.get("docname").toString();
                    String projectid = request.getParameter("pid");
                    if(StringUtil.isNullOrEmpty(projectid)){
                        AuditTrail.insertLog(conn, "213", loginid, documentid, "",
                                        companyid, params, ipAddress, auditMode);
                    } else {
                        AuditTrail.insertLog(conn, "342", loginid, documentid, projectid,
                                        companyid, params, ipAddress, auditMode);
                    }
                    conn.commit();
                } catch (ServiceException ex) {
                    DbPool.quietRollback(conn);
                } finally {
                    DbPool.quietClose(conn);
                }
            }
		} catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("FileDownloadServlet", ex);
        } catch (ConfigurationException ex) {
			KrawlerLog.op.warn("Unable To Download File :" + ex.toString());
		}

	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (ServiceException ex) {
            Logger.getLogger(FileDownloadServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (ServiceException ex) {
            Logger.getLogger(FileDownloadServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
	}

	public String getServletInfo() {
		return "Short description";
	}
}
