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

import org.apache.log4j.Logger;

import com.krawler.svnwebclient.SVNWebClientException;
import com.krawler.svnwebclient.data.AuthenticationException;
import com.krawler.svnwebclient.data.DataProviderException;
import com.krawler.svnwebclient.data.IDataProvider;
import com.krawler.svnwebclient.data.IncorrectParameterException;
import com.krawler.svnwebclient.util.UrlGenerator;
import com.krawler.svnwebclient.util.UrlGeneratorFactory;
import com.krawler.svnwebclient.web.SystemInitializing;
import com.krawler.svnwebclient.web.controller.InvalidResourceBean;
import com.krawler.svnwebclient.web.model.LinkProviderFactory;
import com.krawler.svnwebclient.web.resource.Links;
import com.krawler.svnwebclient.web.support.RequestHandler;
import com.krawler.svnwebclient.web.support.RequestParameters;
import com.krawler.svnwebclient.web.support.State;

public abstract class AbstractServlet extends HttpServlet {
	protected State state;
	protected RequestHandler requestHandler;

	protected void execute(HttpServletRequest request,
			HttpServletResponse response) throws ServletException {
		this.state = new State(request, response);
		this.requestHandler = this.getRequestHandler(request);
		IDataProvider dataProvider = null;
		SystemInitializing initialize = new SystemInitializing();
		initialize.setPickerFields(this.isSingleRevision(), this
				.isMultiSelectionUrl());

		try {
			dataProvider = initialize.init(request, response,
					this.requestHandler);
			if (dataProvider == null) {
				return;
			} else {
				this.executeSVNOperation(dataProvider);
			}
		} catch (AuthenticationException e) {
			initialize.redirectToRestrictPage();
			return;
		} catch (IncorrectParameterException ie) {
			request.getSession()
					.setAttribute(InvalidResourceBean.INVALID_RESOURSE,
							ie.getExceptionInfo());
			try {
				response.sendRedirect(this.getInvalidResourceUrl());
			} catch (IOException e) {
			}
		} catch (SVNWebClientException e) {
			Logger.getLogger(this.getClass()).error(e, e);
			throw new ServletException(e);
		} finally {
			if (dataProvider != null) {
				try {
					dataProvider.close();
				} catch (DataProviderException e) {
				}
			}
		}
	}

	protected boolean isPickerMode() {
		boolean isPickerMode = false;
		if (LinkProviderFactory.PICKER_CONTENT_MODE_VALUE
				.equals(this.state.getRequest().getParameter(
						RequestParameters.CONTENT_MODE_TYPE))) {
			isPickerMode = true;
		}
		return isPickerMode;
	}

	protected String getInvalidResourceUrl() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.INVALID_RESOURSE, this.requestHandler.getLocation());
		if (this.isPickerMode()) {
			urlGenerator.addParameter(RequestParameters.CONTENT_MODE_TYPE,
					LinkProviderFactory.PICKER_CONTENT_MODE_VALUE);
			if (this.isMultiSelectionUrl()) {
				urlGenerator
						.addParameter(RequestParameters.MULTI_URL_SELECTION);
			}
			if (this.isSingleRevision()) {
				urlGenerator.addParameter(RequestParameters.SINGLE_REVISION);
			}
		}

		return urlGenerator.getUrl();
	}

	protected boolean isMultiSelectionUrl() {
		return this.requestHandler.isMultiUrlSelection();
	}

	protected boolean isSingleRevision() {
		return this.requestHandler.isSingleRevisionMode();
	}

	protected abstract void executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException;

	protected abstract RequestHandler getRequestHandler(
			HttpServletRequest request);
}
