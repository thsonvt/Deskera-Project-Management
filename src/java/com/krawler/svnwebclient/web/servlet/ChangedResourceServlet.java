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
import com.krawler.svnwebclient.data.model.DataRepositoryElement;
import com.krawler.svnwebclient.util.UrlGenerator;
import com.krawler.svnwebclient.util.UrlGeneratorFactory;
import com.krawler.svnwebclient.util.UrlUtil;
import com.krawler.svnwebclient.web.resource.Actions;
import com.krawler.svnwebclient.web.resource.Links;
import com.krawler.svnwebclient.web.support.RequestHandler;
import com.krawler.svnwebclient.web.support.RequestParameters;

public class ChangedResourceServlet extends AbstractServlet {
	private static final long serialVersionUID = -5619377681883035515L;

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
		String url = this.requestHandler.getUrl();
		long revision = this.requestHandler.getRevision();
		String action = this.requestHandler.getAction();
		long startRevision;
		long endRevision;
		int type;

		if (Actions.ADD.equals(action) || Actions.REPLACE.equals(action)) {
			startRevision = -1;
			endRevision = revision;
			type = dataProvider.checkUrl(url, revision);
		} else if (Actions.DELETE.equals(action)) {
			endRevision = -1;
			DataRepositoryElement dataRepositoryElement = dataProvider.getInfo(
					url, revision - 1);
			startRevision = dataRepositoryElement.getRevision();
			if (dataRepositoryElement.isDirectory()) {
				type = IDataProvider.DIRECTORY;
			} else {
				type = IDataProvider.FILE;
			}
		} else {
			endRevision = revision;
			DataRepositoryElement dataRepositoryElement = dataProvider.getInfo(
					url, revision - 1);
			startRevision = dataRepositoryElement.getRevision();
			if (dataRepositoryElement.isDirectory()) {
				type = IDataProvider.DIRECTORY;
			} else {
				type = IDataProvider.FILE;
			}
		}

		UrlGenerator urlGenerator = null;
		if (type == IDataProvider.FILE) {
			urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					Links.FILE_COMPARE, this.requestHandler.getLocation());
		} else if (type == IDataProvider.DIRECTORY) {
			urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					Links.DIRECTORY_COMPARE, this.requestHandler.getLocation());
		}
		urlGenerator.addParameter(RequestParameters.URL, UrlUtil.encode(url));
		urlGenerator.addParameter(RequestParameters.PEGREV, Long
				.toString(revision));
		if (this.requestHandler.getCurrentRevision() != -1) {
			urlGenerator.addParameter(RequestParameters.CREV, Long
					.toString(this.requestHandler.getCurrentRevision()));
		}
		urlGenerator.addParameter(RequestParameters.STARTREV, Long
				.toString(startRevision));
		urlGenerator.addParameter(RequestParameters.ENDREV, Long
				.toString(endRevision));

		this.redirect(urlGenerator.getUrl());
	}

	protected void redirect(String url) throws SVNWebClientException {
		try {
			this.state.getResponse().sendRedirect(url);
		} catch (Exception e) {
			throw new SVNWebClientException(e);
		}
	}

	protected RequestHandler getRequestHandler(HttpServletRequest request) {
		return new RequestHandler(request);
	}
}
