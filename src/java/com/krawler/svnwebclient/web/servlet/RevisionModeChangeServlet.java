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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.krawler.svnwebclient.SVNWebClientException;
import com.krawler.svnwebclient.data.IDataProvider;
import com.krawler.svnwebclient.web.controller.RevisionListBean;
import com.krawler.svnwebclient.web.resource.Links;
import com.krawler.svnwebclient.web.support.RequestHandler;

public class RevisionModeChangeServlet extends AbstractServlet {
	private static final long serialVersionUID = 1758390832915586758L;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.execute(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.execute(request, response);
	}

	protected void executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		HttpSession session = this.state.getRequest().getSession();
		if (session
				.getAttribute(RevisionListBean.ADVANCED_NAVIGATION_ATTRIBUTE) == null) {
			session.setAttribute(
					RevisionListBean.ADVANCED_NAVIGATION_ATTRIBUTE,
					RevisionListBean.ADVANCED_NAVIGATION_ATTRIBUTE);
		} else {
			session
					.removeAttribute(RevisionListBean.ADVANCED_NAVIGATION_ATTRIBUTE);
		}
		try {
			this.state.getResponse().sendRedirect(
					Links.REVISION_LIST + "?"
							+ this.state.getRequest().getQueryString());
		} catch (IOException ie) {
			throw new SVNWebClientException(ie);
		}
	}

	protected RequestHandler getRequestHandler(HttpServletRequest request) {
		return new RequestHandler(request);
	}

}
