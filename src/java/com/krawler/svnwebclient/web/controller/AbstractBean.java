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
package com.krawler.svnwebclient.web.controller;

import java.io.IOException;

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
import com.krawler.svnwebclient.util.UrlUtil;
import com.krawler.svnwebclient.web.SystemInitializing;
import com.krawler.svnwebclient.web.controller.directory.PickerDirectoryContentBean;
import com.krawler.svnwebclient.web.model.LinkProviderFactory;
import com.krawler.svnwebclient.web.model.PageInfo;
import com.krawler.svnwebclient.web.resource.Links;
import com.krawler.svnwebclient.web.support.AbstractRequestHandler;
import com.krawler.svnwebclient.web.support.RequestParameters;
import com.krawler.svnwebclient.web.support.State;

public abstract class AbstractBean implements IBean {
	protected State state;
	protected AbstractRequestHandler requestHandler;

	public boolean execute(HttpServletRequest request,
			HttpServletResponse response) throws SVNWebClientException {
		this.state = new State(request, response);
		this.requestHandler = this.getRequestHandler(request);
		IDataProvider dataProvider = null;
		SystemInitializing initialize = new SystemInitializing();
		if (this.isPickerInstance()) {
			initialize.setIsPickerInstance();
			initialize.setPickerFields(this.isSingleRevision(), this
					.isMultiSelectionUrl());
		}

		try {
			dataProvider = initialize.init(request, response,
					this.requestHandler);
			if (dataProvider == null) {
				return false;
			} else {
				return this.executeSVNOperation(dataProvider);
			}
		} catch (AuthenticationException e) {
			initialize.redirectToRestrictPage();
			return false;
		} catch (IncorrectParameterException ie) {
			request.getSession()
					.setAttribute(InvalidResourceBean.INVALID_RESOURSE,
							ie.getExceptionInfo());
			try {
				response.sendRedirect(this.getInvalidResourceUrl());
			} catch (IOException e) {
			}
			return false;
		} catch (SVNWebClientException e) {
			Logger.getLogger(this.getClass()).error(e, e);
			throw new SVNWebClientException(e);
		} finally {
			if (dataProvider != null) {
				try {
					dataProvider.close();
				} catch (DataProviderException e) {
				}
			}
		}
	}

	public PageInfo getCurrentPageInfo() {
		String servletPath = (String) this.state.getRequest().getAttribute(
				"javax.servlet.forward.servlet_path");
		if (servletPath == null) {
			servletPath = this.state.getRequest().getServletPath();
		}

		String queryString = (String) this.state.getRequest().getAttribute(
				"javax.servlet.forward.query_string");
		if (queryString == null) {
			String notDecodedQueryString = this.state.getRequest()
					.getQueryString();
			if (notDecodedQueryString != null) {
				queryString = UrlUtil.decode(notDecodedQueryString);
			}
		}

		PageInfo pageInfo = new PageInfo();
		pageInfo.init(servletPath, queryString);
		return pageInfo;
	}

	protected boolean isPickerMode() {
		boolean isPickerMode = false;
		if (this.isPickerInstance()) {
			isPickerMode = true;
		} else if (LinkProviderFactory.PICKER_CONTENT_MODE_VALUE
				.equals(this.state.getRequest().getParameter(
						RequestParameters.CONTENT_MODE_TYPE))) {
			isPickerMode = true;
		}
		return isPickerMode;
	}

	protected boolean isPickerInstance() {
		boolean isPickerInstance = false;
		if (this instanceof PickerDirectoryContentBean) {
			isPickerInstance = true;
		}
		return isPickerInstance;
	}

	protected String getInvalidResourceUrl() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.INVALID_RESOURSE, this.requestHandler.getLocation());
		if (this.isPickerMode()) {
			urlGenerator.addParameter(RequestParameters.CONTENT_MODE_TYPE,
					LinkProviderFactory.PICKER_CONTENT_MODE_VALUE);
			if (this.isSingleRevision()) {
				urlGenerator.addParameter(RequestParameters.SINGLE_REVISION);
			}
			if (this.isMultiSelectionUrl()) {
				urlGenerator
						.addParameter(RequestParameters.MULTI_URL_SELECTION);
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

	protected abstract boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException;

	protected abstract AbstractRequestHandler getRequestHandler(
			HttpServletRequest request);
}
