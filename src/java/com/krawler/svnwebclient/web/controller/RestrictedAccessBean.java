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
import com.krawler.svnwebclient.configuration.ConfigurationProvider;
import com.krawler.svnwebclient.data.IDataProvider;
import com.krawler.svnwebclient.util.UrlGenerator;
import com.krawler.svnwebclient.util.UrlGeneratorFactory;
import com.krawler.svnwebclient.web.model.ILinkProvider;
import com.krawler.svnwebclient.web.model.LinkProviderFactory;
import com.krawler.svnwebclient.web.model.Navigation;
import com.krawler.svnwebclient.web.resource.Links;
import com.krawler.svnwebclient.web.support.AbstractRequestHandler;
import com.krawler.svnwebclient.web.support.RequestException;
import com.krawler.svnwebclient.web.support.RequestHandler;
import com.krawler.svnwebclient.web.support.RequestParameters;

public class RestrictedAccessBean extends AbstractBean {
	protected String url;
	protected String location;
	protected ILinkProvider linkProvider;
	protected boolean isMultiSelectionUrl;
	protected boolean isSingleRevision;

	public RestrictedAccessBean() {
	}

	protected boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		this.url = this.requestHandler.getUrl();
		this.location = this.requestHandler.getLocation();
		this.linkProvider = LinkProviderFactory
				.getLinkProvider(this.requestHandler.getContentMode());
		this.isMultiSelectionUrl = this.requestHandler.isMultiUrlSelection();
		this.isSingleRevision = this.requestHandler.isSingleRevisionMode();
		return true;
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request) {
			public void check() throws RequestException {
			}
		};
	}

	public String getFullRootPageUrl() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.CLEAN_EXTRA_SESSION_ATTRIBUTE, this.requestHandler
						.getLocation());
		if (ConfigurationProvider.getInstance().isMultiRepositoryMode()) {
			urlGenerator
					.addParameter(RequestParameters.LOCATION, this.location);
		}
		if (this.linkProvider.isPickerMode()) {
			urlGenerator.addParameter(RequestParameters.CONTENT_MODE_TYPE,
					LinkProviderFactory.PICKER_CONTENT_MODE_VALUE);
			if (this.isMultiSelectionUrl) {
				urlGenerator
						.addParameter(RequestParameters.MULTI_URL_SELECTION);
			}
			if (this.isSingleRevision) {
				urlGenerator.addParameter(RequestParameters.SINGLE_REVISION);
			}
		}
		return urlGenerator.getUrl();
	}

	public String getPageLocation() {
		return this.location;
	}

	public Navigation getNavigation() {
		return null;
	}

	public List getActions() {
		return null;
	}

	public String getPageUrl() {
		return this.url;
	}

	public String getRestrictLoginUrl() {
		StringBuffer res = new StringBuffer();
		res.append(Links.RESTRICT_LOGIN);
		if (ConfigurationProvider.getInstance().isMultiRepositoryMode()) {
			res.append("?").append(RequestParameters.LOCATION).append("=")
					.append(this.location).append("&").append(
							RequestParameters.URL).append("=");
		} else {
			res.append("?").append(RequestParameters.URL).append("=");
		}
		res.append(this.url);
		if (this.isMultiSelectionUrl) {
			res.append("&").append(RequestParameters.MULTI_URL_SELECTION);
		}
		if (this.isSingleRevision) {
			res.append("&").append(RequestParameters.SINGLE_REVISION);
		}
		return res.toString();
	}

	public boolean isPickerMode() {
		return this.linkProvider.isPickerMode();
	}
}
