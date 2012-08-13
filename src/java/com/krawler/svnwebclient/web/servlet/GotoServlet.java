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
import com.krawler.svnwebclient.data.IDataProvider;
import com.krawler.svnwebclient.util.UrlGenerator;
import com.krawler.svnwebclient.util.UrlGeneratorFactory;
import com.krawler.svnwebclient.util.UrlUtil;
import com.krawler.svnwebclient.web.model.ILinkProvider;
import com.krawler.svnwebclient.web.model.LinkProviderFactory;
import com.krawler.svnwebclient.web.resource.Links;
import com.krawler.svnwebclient.web.support.RequestHandler;
import com.krawler.svnwebclient.web.support.RequestParameters;

public class GotoServlet extends AbstractServlet {
	private static final long serialVersionUID = -4637591501688567428L;

	protected static final String FILEPATH_PARAM = "filepath";
	protected static final String SET_REVISION_PARAM = "setRevision";
	protected static final String INPUT_REVISION_PARAM = "inputRevision";

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
		HttpServletRequest request = this.state.getRequest();
		String path = request.getParameter(GotoServlet.FILEPATH_PARAM);
		String setRevision = request
				.getParameter(GotoServlet.SET_REVISION_PARAM);
		long revision = -1;
		if ("HEAD".equals(setRevision)) {
			revision = -1;
		} else {
			try {
				revision = Long.parseLong(request
						.getParameter(GotoServlet.INPUT_REVISION_PARAM));
			} catch (Exception e) {
			}
		}

		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.CONTENT, this.requestHandler.getLocation());
		urlGenerator.addParameter(RequestParameters.URL, UrlUtil.encode(path));
		urlGenerator.addParameter(RequestParameters.CREV, Long
				.toString(revision));

		ILinkProvider linkProvider = LinkProviderFactory
				.getLinkProvider(this.requestHandler.getContentMode());
		if (linkProvider.isPickerMode()) {
			urlGenerator.addParameter(RequestParameters.CONTENT_MODE_TYPE,
					LinkProviderFactory.PICKER_CONTENT_MODE_VALUE);
			;
			if (this.requestHandler.isMultiUrlSelection()) {
				urlGenerator
						.addParameter(RequestParameters.MULTI_URL_SELECTION);
			}
		}
		try {
			this.state.getResponse().sendRedirect(urlGenerator.getUrl());
		} catch (IOException ie) {
			throw new SVNWebClientException(ie);
		}
	}

	protected RequestHandler getRequestHandler(HttpServletRequest request) {
		return new RequestHandler(request);
	}
}
