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
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DownloadMPXServlet extends HttpServlet {
	private static final long serialVersionUID = 4290310048982803225L;

	/**
	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
	 * methods.
	 * 
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 */
	protected void processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String path = request.getParameter("fn");

		try {
			File f = new File("C:/" + path);
			FileInputStream fstream = new FileInputStream("C:/" + path);
			byte[] data = new byte[(int) f.length()];
			fstream.read(data);
			fstream.close();
			sendFile(data, f.getName(), request, response);
			boolean success = f.delete();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendFile(byte[] data, String fileName,
			HttpServletRequest resquest, HttpServletResponse response)
			throws IOException {

		response.setHeader("Content-Disposition", "filename=\"" + fileName
				+ "\"");
		response.setContentType("application/octet-stream");
		response.setContentLength(data.length);
		int off = 0;
		int len = 4096;
		boolean done = false;
		OutputStream os = response.getOutputStream();
		if (data.length >= len) {
			while (!done) {
				os.write(data, off, len);
				os.flush();
				off += len;
				if ((off + len) >= data.length) {
					os.write(data, off, data.length - off);
					done = true;
				}
			}
		} else {
			os.write(data, off, data.length);
		}
		os.close();
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
