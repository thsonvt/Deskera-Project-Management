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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.krawler.common.util.KrawlerLog;
import com.krawler.esp.database.dbcon;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.svnwebclient.web.controller.file.FileDataBean;
import com.krawler.svnwebclient.web.model.data.file.FileData;

public class FileDataServlet extends HttpServlet {
	private static final long serialVersionUID = -6284732015881370617L;

	protected void processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		ServletOutputStream out = response.getOutputStream();
		try {
			FileDataBean bean = new FileDataBean();
			String str = bean.getFile(request, response);
			String dtype = "attachment";
			if (str.compareTo("") != 0 && str.compareTo("error") != 0) {
				Hashtable ht = dbcon.getfileinfo(str);
				File fp = new File(com.krawler.esp.handlers.StorageHandler
						.GetDocStorePath(ht.get("storeindex").toString())
						+ StorageHandler.GetFileSeparator()
						+ ht.get("userid").toString()
						+ StorageHandler.GetFileSeparator()
						+ ht.get("svnname"));
				byte buff[] = new byte[(int) fp.length()];
				FileInputStream fis = new FileInputStream(fp);
				int read = fis.read(buff);
				String mimeType = com.krawler.esp.utils.KrawlerApp
						.getContentType("", ht.get("svnname").toString()
								.toLowerCase(), buff);
				response.setContentType(mimeType);
				if (mimeType != null) {
					dtype = "inline";
				}
				response.setContentLength((int) fp.length());
				response.setHeader("Content-Disposition", dtype
						+ "; filename=\"" + ht.get("docname") + "\";");
				out.write(buff);
			} else if (str.compareTo("error") == 0) {
				throw new Exception();
			} else {
                String docid = request.getParameter("url");
                Hashtable ht = dbcon.getfileinfo(docid);
                docid = StorageHandler.GetDocStorePath().concat(StorageHandler.GetFileSeparator()).concat(ht.get("userid").toString())
                        .concat(StorageHandler.GetFileSeparator()).concat(docid).concat(".swf");
                File f = new File(docid);
                out.print("<html><body>");
                if(f.exists()){
                    out.print("<embed width=\"100%\" height=\"100%\" quality=\"high\" bgcolor=\"#FFFFFF\" wmode=\"transparent\" name=\"krwdoc\" id=\"krwdoc\" src=\"content.stream?path="+docid+"&type=swf\" type=\"application/x-shockwave-flash\"/>");
                } else {
                    FileData content = bean.getFileData();
                    List lines = content.getLines();
                    out
                            .print("<table cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"><tr><td>");
                    for (Iterator i = lines.iterator(); i.hasNext();) {
                                        FileData.Line line = (FileData.Line) i.next();
                                        out.print("<tr valign=\"middle\">");
                                        out.print("<td style=\"height:15px;!important;font-size:11px;background: #E9F3FA;padding-left: 5px;padding-right: 5px;padding-top: 2px;padding-bottom: 2px;border-bottom: 1px solid #C0DAEA;border-right: 1px solid #C0DAEA;\">");
                                        out.print(line.getNumber());
                                        out.print("</td>");
                                        out.print(" <td nowrap=\"true\" align=\"left\" width=\"100%\" style=\"height:15px;!important;font-size:11px;background:<%=line.getBackground()%>;padding-left: 5px;padding-right: 5px;padding-top: 2px;padding-bottom: 2px;border-bottom: 1px solid white;border-right: 1px solid white;\">");
                                        String a =null;
                                        try{
                                        a=new String(line.getData().getBytes(),"UTF-8");
                                        out.print(a);
                                        }catch(java.io.CharConversionException e){

                                        out.print((char)216);
                                        }
                                        out.print("</td></tr>");
                                    }
                    out.print("</td></tr></table>");
                }
				out.print("</body></html>");
                  	}
		} catch (Exception e) {
			KrawlerLog.op.warn("Unable To Show File Content :" + e.toString());
			response.setContentType("text/html");
			out.print("<strong>Unable To Show Content</strong>");
		}

	}

	// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on
	// the + sign on the left to edit the code.">
	/**
	 * Handles the HTTP <code>GET</code> method.
	 *
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 *
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Returns a short description of the servlet.
	 */
	public String getServletInfo() {
		return "Short description";
	}
	// </editor-fold>
}
