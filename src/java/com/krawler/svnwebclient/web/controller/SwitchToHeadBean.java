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
import com.krawler.svnwebclient.data.DataProviderException;
import com.krawler.svnwebclient.data.IDataProvider;
import com.krawler.svnwebclient.data.model.DataRepositoryElement;
import com.krawler.svnwebclient.util.UrlGenerator;
import com.krawler.svnwebclient.util.UrlGeneratorFactory;
import com.krawler.svnwebclient.util.UrlUtil;
import com.krawler.svnwebclient.web.model.ILinkProvider;
import com.krawler.svnwebclient.web.model.LinkProviderFactory;
import com.krawler.svnwebclient.web.model.Navigation;
import com.krawler.svnwebclient.web.model.data.SwitchToHead;
import com.krawler.svnwebclient.web.support.AbstractRequestHandler;
import com.krawler.svnwebclient.web.support.RequestException;
import com.krawler.svnwebclient.web.support.RequestParameters;

public class SwitchToHeadBean extends AbstractBean {
	protected long headRevision;
	protected String originalUrl;
	protected String urlInRevision;
	protected String urlInHead;
	protected boolean redirectToDirectory;
	protected DataRepositoryElement repositoryElement;
	protected ILinkProvider linkProvider;
	protected boolean isMultiSelectionUrl;
	protected boolean isSingleRevision;

	public SwitchToHeadBean() {
	}

	protected boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		this.urlInHead = "";
		this.urlInRevision = "";

		this.isMultiSelectionUrl = this.requestHandler.isMultiUrlSelection();
		this.isSingleRevision = this.requestHandler.isSingleRevisionMode();

		this.headRevision = dataProvider.getHeadRevision();
		this.originalUrl = this.requestHandler.getUrl();
		this.linkProvider = LinkProviderFactory
				.getLinkProvider(this.requestHandler.getContentMode());

		this.repositoryElement = dataProvider.getInfo(this.urlInRevision,
				this.requestHandler.getPegRevision());

		List pathChain = UrlUtil.getPathChain(this.originalUrl);
		boolean found = false;
		for (int i = pathChain.size() - 1; i >= 0; i--) {
			try {
				this.urlInRevision = (String) pathChain.get(i);
				this.urlInHead = dataProvider
						.getLocation(this.urlInRevision, this.requestHandler
								.getPegRevision(), this.headRevision);
				found = true;
				break;
			} catch (DataProviderException e) {
				this.urlInHead = null;
				this.urlInRevision = null;
			}
		}

		if (this.originalUrl.equals(this.urlInRevision)) {
			DataRepositoryElement repositoryElement = dataProvider.getInfo(
					this.urlInHead, this.headRevision);
			if (repositoryElement.isDirectory()) {
				this.redirect(this.linkProvider.getDirectoryContentLink(),
						this.urlInHead);
			} else {
				this.redirect(this.linkProvider.getFileContentLink(),
						this.urlInHead);
			}
			return false;
		}

		if (found) {
			DataRepositoryElement repositoryElement = dataProvider.getInfo(
					this.urlInHead, this.headRevision);
			if (repositoryElement.isDirectory()) {
				this.redirectToDirectory = true;
			} else {
				this.redirectToDirectory = false;
			}
		}
		return true;
	}

	protected void redirect(String base, String url)
			throws SVNWebClientException {
		try {
			UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					base, this.requestHandler.getLocation());
			urlGenerator.addParameter(RequestParameters.URL, UrlUtil
					.encode(url));
			if (this.isMultiSelectionUrl) {
				urlGenerator
						.addParameter(RequestParameters.MULTI_URL_SELECTION);
			}
			if (this.isSingleRevision) {
				urlGenerator.addParameter(RequestParameters.SINGLE_REVISION);
			}
			this.state.getResponse().sendRedirect(urlGenerator.getUrl());
		} catch (Exception e) {
			throw new SVNWebClientException(e);
		}
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new AbstractRequestHandler(request) {
			public void check() throws RequestException {
				this.checkLong(RequestParameters.PEGREV);
			}
		};

	}

	public SwitchToHead getSwitchToHead() {
		SwitchToHead switchToHead = new SwitchToHead(this.requestHandler
				.getLocation(), this.originalUrl, this.urlInRevision,
				this.urlInHead, this.redirectToDirectory);
		switchToHead.setHeadRevision(this.headRevision);
		switchToHead.setRevision(this.requestHandler.getPegRevision());
		return switchToHead;
	}

	public Navigation getNavigation() {
		return new Navigation(this.originalUrl, this.requestHandler
				.getLocation(), this.requestHandler.getPegRevision(),
				!this.repositoryElement.isDirectory(), this.linkProvider,
				this.isSingleRevision, this.isMultiSelectionUrl);
	}

	public List getActions() {
		return null;
	}

	public boolean isPickerMode() {
		return this.linkProvider.isPickerMode();
	}
}
