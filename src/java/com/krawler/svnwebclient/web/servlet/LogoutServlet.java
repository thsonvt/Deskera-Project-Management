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
import javax.servlet.http.HttpSession;

import com.krawler.svnwebclient.authorization.impl.CredentialsManager;
import com.krawler.svnwebclient.configuration.ConfigurationProvider;
import com.krawler.svnwebclient.web.SystemInitializing;
import com.krawler.svnwebclient.web.resource.Links;

public class LogoutServlet extends HttpServlet {

	private static final long serialVersionUID = -4086979556233654490L;

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
		HttpSession session = request.getSession();
		String id = (String) session.getAttribute(SystemInitializing.ID);

		session.setAttribute(SystemInitializing.ID, null);
		session.setAttribute(CredentialsManager.CREDENTIALS, null);
		if (id != null
				&& ConfigurationProvider.getInstance().isMultiRepositoryMode()) {
			try {
				com.krawler.svnwebclient.data.javasvn.SVNRepositoryPool
						.getInstance(id).shutdown();
				org.polarion.svncommons.commentscache.SVNRepositoryPool
						.getInstance(id).shutdown();
			} catch (Exception e) {
				throw new ServletException(e);
			}
		}

		request.getRequestDispatcher(Links.DIRECTORY_CONTENT).forward(request,
				response);

	}
}
