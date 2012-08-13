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

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.krawler.svnwebclient.SVNWebClientException;
import com.krawler.svnwebclient.data.IDataProvider;
import com.krawler.svnwebclient.data.IncorrectParameterException;
import com.krawler.svnwebclient.util.UrlGenerator;
import com.krawler.svnwebclient.util.UrlGeneratorFactory;
import com.krawler.svnwebclient.web.model.ILinkProvider;
import com.krawler.svnwebclient.web.model.LinkProviderFactory;
import com.krawler.svnwebclient.web.model.Navigation;
import com.krawler.svnwebclient.web.support.AbstractRequestHandler;
import com.krawler.svnwebclient.web.support.RequestHandler;
import com.krawler.svnwebclient.web.support.RequestParameters;

public class InvalidResourceBean extends AbstractBean {
	public static final String INVALID_RESOURSE = "invalidResourse";
	protected IncorrectParameterException.ExceptionInfo info;
	protected ILinkProvider linkRpovider;

	protected boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		this.info = (IncorrectParameterException.ExceptionInfo) this.state
				.getRequest().getSession().getAttribute(
						InvalidResourceBean.INVALID_RESOURSE);
		this.state.getRequest().getSession().removeAttribute(
				InvalidResourceBean.INVALID_RESOURSE);
		this.linkRpovider = LinkProviderFactory
				.getLinkProvider(this.requestHandler.getContentMode());
		return true;
	}

	public String getMessage() {
		if (info != null) {
			return this.info.getMessage();
		} else {
			return "";
		}
	}

	public String getDescription() {
		if (this.info != null) {
			return this.info.getDescription();
		} else {
			return "";
		}
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request);
	}

	public Navigation getNavigation() {
		return null;
	}

	public List getActions() {
		return null;
	}

	public String getActionUrl() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				this.linkRpovider.getDirectoryContentLink(),
				this.requestHandler.getLocation());
		if (this.requestHandler.isMultiUrlSelection()) {
			urlGenerator.addParameter(RequestParameters.MULTI_URL_SELECTION);
		}
		if (this.requestHandler.isSingleRevisionMode()) {
			urlGenerator.addParameter(RequestParameters.SINGLE_REVISION);
		}

		return urlGenerator.getUrl();
	}

	public boolean isPickerMode() {
		return this.linkRpovider.isPickerMode();
	}
}
