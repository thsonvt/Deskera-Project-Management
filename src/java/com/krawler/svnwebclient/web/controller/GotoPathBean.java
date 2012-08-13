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
import com.krawler.svnwebclient.util.UrlGenerator;
import com.krawler.svnwebclient.util.UrlGeneratorFactory;
import com.krawler.svnwebclient.web.model.ILinkProvider;
import com.krawler.svnwebclient.web.model.Link;
import com.krawler.svnwebclient.web.model.LinkProviderFactory;
import com.krawler.svnwebclient.web.model.Navigation;
import com.krawler.svnwebclient.web.resource.Links;
import com.krawler.svnwebclient.web.support.AbstractRequestHandler;
import com.krawler.svnwebclient.web.support.RequestHandler;
import com.krawler.svnwebclient.web.support.RequestParameters;

public class GotoPathBean extends AbstractBean {

	protected String path;
	protected Navigation navigation;
	protected ILinkProvider linkProvider;
	protected boolean isMultiUrlSelection;

	protected boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		this.path = this.requestHandler.getUrl();
		this.linkProvider = LinkProviderFactory
				.getLinkProvider(this.requestHandler.getContentMode());
		this.isMultiUrlSelection = this.requestHandler.isMultiUrlSelection();

		this.navigation = new Navigation(this.requestHandler.getUrl(),
				this.requestHandler.getLocation(), this.requestHandler
						.getCurrentRevision(), linkProvider, false,
				isMultiUrlSelection);
		return true;
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request);
	}

	public String getPath() {
		return this.path;
	}

	public Navigation getNavigation() {
		return this.navigation;

	}

	public List getActions() {
		return null;
	}

	public String getCancelUrl() {
		List navigationPath = this.navigation.getPath();
		Link lastElement = (Link) navigationPath.get(navigationPath.size() - 1);
		return lastElement.getUrl();
	}

	public String getGotoUrl() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.GOTO, this.requestHandler.getLocation());
		if (this.linkProvider.isPickerMode()) {
			urlGenerator.addParameter(RequestParameters.CONTENT_MODE_TYPE,
					LinkProviderFactory.PICKER_CONTENT_MODE_VALUE);
			if (this.isMultiUrlSelection) {
				urlGenerator
						.addParameter(RequestParameters.MULTI_URL_SELECTION);
			}
		}
		return urlGenerator.getUrl();
	}

	public boolean isPickerMode() {
		return this.linkProvider.isPickerMode();
	}
}
