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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.krawler.svnwebclient.SVNWebClientException;
import com.krawler.svnwebclient.data.IDataProvider;
import com.krawler.svnwebclient.data.IncorrectParameterException;
import com.krawler.svnwebclient.web.model.ILinkProvider;
import com.krawler.svnwebclient.web.model.LinkProviderFactory;
import com.krawler.svnwebclient.web.support.RequestHandler;

public class ContentServlet extends AbstractServlet {
	private static final long serialVersionUID = 6366555973363747975L;

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
		long headRevision = dataProvider.getHeadRevision();
		long revision = headRevision;
		if (this.requestHandler.getCurrentRevision() != -1) {
			revision = this.requestHandler.getCurrentRevision();
		}
		String url = this.requestHandler.getUrl();
		if (this.requestHandler.getPegRevision() != -1) {
			url = dataProvider.getLocation(this.requestHandler.getUrl(),
					this.requestHandler.getPegRevision(), revision);
		}

		ILinkProvider linkProvider = LinkProviderFactory
				.getLinkProvider(this.requestHandler.getContentMode());
		int type = dataProvider.checkUrl(url, revision);
		if (type == IDataProvider.DIRECTORY) {
			this.forward(linkProvider.getDirectoryContentLink());
		} else if (type == IDataProvider.FILE) {
			this.forward(linkProvider.getFileContentLink());
		} else {
			String description = "HTTP Path Not Found";
			String message = "Url: " + url + ", revision: " + revision;
			throw new IncorrectParameterException(message, description);
		}
	}

	protected void forward(String page) throws SVNWebClientException {
		try {
			RequestDispatcher dispatcher = this.state.getRequest()
					.getRequestDispatcher(page);
			dispatcher.forward(this.state.getRequest(), this.state
					.getResponse());
		} catch (Exception e) {
			throw new SVNWebClientException(e);
		}
	}

	protected RequestHandler getRequestHandler(HttpServletRequest request) {
		return new RequestHandler(request);
	}
}
