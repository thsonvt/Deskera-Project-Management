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
package com.krawler.svnwebclient.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.krawler.svnwebclient.util.MailDelivery;
import com.krawler.svnwebclient.web.resource.Links;

public class SendEmailServlet extends HttpServlet {
	private static final long serialVersionUID = 6388822684170646448L;

	public static final String NAME = "name";
	public static final String EMAIL = "email";
	public static final String DESCRIPTION = "description";
	public static final String STACKTRACE = "stacktrace";
	protected static final String LETTER_CONTENT = "letter";
	public static final String PRESSED_BUTTON = "review";

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.execute(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.execute(request, response);
	}

	protected void execute(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String pressedButton = request
				.getParameter(SendEmailServlet.PRESSED_BUTTON);
		if ("Back".equals(pressedButton)) {
			request.getRequestDispatcher(Links.ERROR)
					.forward(request, response);
			return;
		} else {
			String message = request
					.getParameter(SendEmailServlet.LETTER_CONTENT);
			message = message.replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
			String reportId = request.getParameter("reportId");
			request.getSession().removeAttribute("reportId");
			if (MailDelivery.sendEmail(message, reportId)) {
				response.sendRedirect(Links.DIRECTORY_CONTENT);
			} else {
				request.getRequestDispatcher(Links.REVIEW).forward(request,
						response);
			}
		}
	}
}
