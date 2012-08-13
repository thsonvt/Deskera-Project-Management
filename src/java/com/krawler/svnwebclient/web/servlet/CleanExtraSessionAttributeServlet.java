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
import com.krawler.svnwebclient.configuration.ConfigurationProvider;
import com.krawler.svnwebclient.data.IDataProvider;
import com.krawler.svnwebclient.web.AttributeStorage;
import com.krawler.svnwebclient.web.model.ILinkProvider;
import com.krawler.svnwebclient.web.model.LinkProviderFactory;
import com.krawler.svnwebclient.web.support.RequestHandler;
import com.krawler.svnwebclient.web.support.RequestParameters;

public class CleanExtraSessionAttributeServlet extends AbstractServlet {

	private static final long serialVersionUID = 69501650251931262L;

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
		ILinkProvider linkProvider = LinkProviderFactory
				.getLinkProvider(this.requestHandler.getContentMode());

		AttributeStorage.getInstance().cleanSession(
				this.state.getRequest().getSession());
		StringBuffer url = new StringBuffer();
		if (ConfigurationProvider.getInstance().isMultiRepositoryMode()) {
			url.append(linkProvider.getDirectoryContentLink()).append("?")
					.append(RequestParameters.LOCATION).append("=").append(
							this.requestHandler.getLocation()).append("&")
					.append(RequestParameters.URL).append("=");
		} else {
			url.append(linkProvider.getDirectoryContentLink()).append("?")
					.append(RequestParameters.URL).append("=");
		}
		if (this.requestHandler.isMultiUrlSelection()) {
			url.append("&").append(RequestParameters.MULTI_URL_SELECTION);
		}
		if (this.requestHandler.isSingleRevisionMode()) {
			url.append("&").append(RequestParameters.SINGLE_REVISION);
		}
		try {
			this.state.getResponse().sendRedirect(url.toString());
		} catch (IOException e) {
			throw new SVNWebClientException(e);
		}
	}

	protected RequestHandler getRequestHandler(HttpServletRequest request) {
		return new RequestHandler(request);
	}
}
