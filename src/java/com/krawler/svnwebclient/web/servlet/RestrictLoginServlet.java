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

import com.krawler.svnwebclient.SVNWebClientException;
import com.krawler.svnwebclient.authorization.impl.CredentialsManager;
import com.krawler.svnwebclient.configuration.ConfigurationProvider;
import com.krawler.svnwebclient.data.IDataProvider;
import com.krawler.svnwebclient.util.UrlUtil;
import com.krawler.svnwebclient.web.SystemInitializing;
import com.krawler.svnwebclient.web.support.RequestHandler;
import com.krawler.svnwebclient.web.support.RequestParameters;

public class RestrictLoginServlet extends AbstractServlet {
	private static final long serialVersionUID = -2180203163406605770L;

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
		this.state.getRequest().getSession().setAttribute(
				CredentialsManager.CREDENTIALS, null);
		this.state.getRequest().getSession().setAttribute(
				SystemInitializing.ID, null);

		String originalUrl = (String) this.state.getRequest().getSession()
				.getAttribute(SystemInitializing.ORIGINAL_URL);
		StringBuffer params = new StringBuffer("?");

		if (ConfigurationProvider.getInstance().isMultiRepositoryMode()) {
			params.append(RequestParameters.LOCATION).append("=").append(
					this.requestHandler.getLocation()).append("&");
		}
		params.append(RequestParameters.URL).append("=").append(
				UrlUtil.encode(this.requestHandler.getUrl()));

		if (this.requestHandler.isMultiUrlSelection()) {
			params.append("&").append(RequestParameters.MULTI_URL_SELECTION);
		}
		if (this.requestHandler.isSingleRevisionMode()) {
			params.append("&").append(RequestParameters.SINGLE_REVISION);
		}
		try {
			this.state.getResponse().sendRedirect(
					originalUrl + params.toString());
		} catch (IOException e) {
			throw new SVNWebClientException(e);
		}
	}

	protected RequestHandler getRequestHandler(HttpServletRequest request) {
		return new RequestHandler(request);
	}
}
