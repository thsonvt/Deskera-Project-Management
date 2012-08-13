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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.krawler.svnwebclient.SVNWebClientException;
import com.krawler.svnwebclient.configuration.ConfigurationProvider;
import com.krawler.svnwebclient.data.IDataProvider;
import com.krawler.svnwebclient.data.model.DataDirectoryElement;
import com.krawler.svnwebclient.util.UrlGenerator;
import com.krawler.svnwebclient.util.UrlGeneratorFactory;
import com.krawler.svnwebclient.util.UrlUtil;
import com.krawler.svnwebclient.web.model.ILinkProvider;
import com.krawler.svnwebclient.web.model.Link;
import com.krawler.svnwebclient.web.model.LinkProviderFactory;
import com.krawler.svnwebclient.web.model.Navigation;
import com.krawler.svnwebclient.web.model.PathSwitch;
import com.krawler.svnwebclient.web.model.data.PathSwitchContent;
import com.krawler.svnwebclient.web.support.AbstractRequestHandler;
import com.krawler.svnwebclient.web.support.RequestException;
import com.krawler.svnwebclient.web.support.RequestHandler;
import com.krawler.svnwebclient.web.support.RequestParameters;

public class PathSwitchBean extends AbstractBean {
	protected long revision;
	protected long headRevision;
	protected DataDirectoryElement directoryElement;
	protected String prefix;
	protected ILinkProvider linkProvider;

	protected boolean isMultiSelectionUrl;
	protected boolean isSingleRevision;

	public PathSwitchBean() {
	}

	protected boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		this.headRevision = dataProvider.getHeadRevision();
		this.revision = this.headRevision;
		this.isMultiSelectionUrl = this.requestHandler.isMultiUrlSelection();
		this.isSingleRevision = this.requestHandler.isSingleRevisionMode();

		if (this.requestHandler.getCurrentRevision() != -1) {
			this.revision = this.requestHandler.getCurrentRevision();
		}
		this.linkProvider = LinkProviderFactory
				.getLinkProvider(this.requestHandler.getContentMode());

		if (PathSwitch.TRUNK.equals(this.requestHandler.getType())) {
			String trunkUrl = this.requestHandler.getUrlPrefix() + "/"
					+ ConfigurationProvider.getInstance().getTrunkName();
			String url = trunkUrl + "/" + this.requestHandler.getUrlSuffix();

			int resourceType = dataProvider.checkUrl(url, this.revision);
			if (resourceType == IDataProvider.NOT_EXIST) {
				String redirectPage = trunkUrl;
				int resType = dataProvider.checkUrl(trunkUrl, this.revision);
				if (resType == IDataProvider.NOT_EXIST) {
					redirectPage = this.requestHandler.getUrlPrefix();
				}
				UrlGenerator urlGenerator = UrlGeneratorFactory
						.getUrlGenerator(this.linkProvider
								.getDirectoryContentLink(), this.requestHandler
								.getLocation());
				urlGenerator.addParameter(RequestParameters.URL, UrlUtil
						.encode(redirectPage));
				if (this.requestHandler.getCurrentRevision() != -1) {
					urlGenerator
							.addParameter(RequestParameters.CREV, Long
									.toString(this.requestHandler
											.getCurrentRevision()));
				}
				if (this.isMultiSelectionUrl) {
					urlGenerator
							.addParameter(RequestParameters.MULTI_URL_SELECTION);
				}
				if (this.isSingleRevision) {
					urlGenerator
							.addParameter(RequestParameters.SINGLE_REVISION);
				}

				this.redirect(urlGenerator.getUrl());
				return false;
			} else {
				UrlGenerator urlGenerator;
				if (resourceType == IDataProvider.DIRECTORY) {
					urlGenerator = UrlGeneratorFactory.getUrlGenerator(
							this.linkProvider.getDirectoryContentLink(),
							this.requestHandler.getLocation());
				} else {
					urlGenerator = UrlGeneratorFactory.getUrlGenerator(
							this.linkProvider.getFileContentLink(),
							this.requestHandler.getLocation());
				}
				urlGenerator.addParameter(RequestParameters.URL, UrlUtil
						.encode(url));
				if (this.requestHandler.getCurrentRevision() != -1) {
					urlGenerator
							.addParameter(RequestParameters.CREV, Long
									.toString(this.requestHandler
											.getCurrentRevision()));
				}
				if (this.isMultiSelectionUrl) {
					urlGenerator
							.addParameter(RequestParameters.MULTI_URL_SELECTION);
				}
				if (this.isSingleRevision) {
					urlGenerator
							.addParameter(RequestParameters.SINGLE_REVISION);
				}
				this.redirect(urlGenerator.getUrl());
				return false;
			}
		} else {
			this.prefix = this.requestHandler.getUrlPrefix() + "/";
			if (PathSwitch.BRANCHES.equals(this.requestHandler.getType())) {
				this.prefix += ConfigurationProvider.getInstance()
						.getBranchesName();
			} else if (PathSwitch.TAGS.equals(this.requestHandler.getType())) {
				this.prefix += ConfigurationProvider.getInstance()
						.getTagsName();
			}

			int resourceType = dataProvider
					.checkUrl(this.prefix, this.revision);
			if (resourceType == IDataProvider.NOT_EXIST) {
				this.directoryElement = null;
			} else {
				this.directoryElement = dataProvider.getDirectory(this.prefix,
						this.revision);
			}
		}

		return true;
	}

	protected void redirect(String url) throws SVNWebClientException {
		try {
			this.state.getResponse().sendRedirect(url);
		} catch (Exception e) {
			throw new SVNWebClientException(e);
		}
	}

	public PathSwitchContent getSwitchContent() {
		if (this.directoryElement == null) {
			return null;
		} else {
			return new PathSwitchContent(this.directoryElement,
					this.requestHandler, this.prefix, this.requestHandler
							.getUrlSuffix());
		}
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request) {
			public void check() throws RequestException {
				this.checkNotNull(RequestParameters.PREFIX);
				this.checkNotNull(RequestParameters.SUFFIX);
				this.checkNotNullOrEmpty(RequestParameters.TYPE);
			}
		};
	}

	public String getName() {
		String ret = null;
		if (PathSwitch.BRANCHES.equals(this.requestHandler.getType())) {
			ret = "branch";
		} else if (PathSwitch.TAGS.equals(this.requestHandler.getType())) {
			ret = "tag";
		}
		return ret;
	}

	public String getPluralName() {
		String ret = null;
		if (PathSwitch.BRANCHES.equals(this.requestHandler.getType())) {
			ret = "branches";
		} else if (PathSwitch.TAGS.equals(this.requestHandler.getType())) {
			ret = "tags";
		}
		return ret;
	}

	public Navigation getNavigation() {
		return new Navigation(this.requestHandler.getUrl(), this.requestHandler
				.getLocation(), this.requestHandler.getCurrentRevision(),
				this.linkProvider, this.isSingleRevision,
				this.isMultiSelectionUrl);
	}

	public String getCancelUrl() {
		List navigationPath = this.getNavigation().getPath();
		Link lastElement = (Link) navigationPath.get(navigationPath.size() - 1);
		String returnUrl = lastElement.getUrl();
		int index = returnUrl.indexOf("?");
		if (index == -1) {
			returnUrl += "?";
		}
		if (this.linkProvider.isPickerMode()) {
			returnUrl += "&" + RequestParameters.CONTENT_MODE_TYPE + "="
					+ LinkProviderFactory.PICKER_CONTENT_MODE_VALUE;

			if (this.isMultiSelectionUrl) {
				returnUrl += "&" + RequestParameters.MULTI_URL_SELECTION;
			}
			if (this.isSingleRevision) {
				returnUrl += "&" + RequestParameters.SINGLE_REVISION;
			}
		}

		return returnUrl;
	}

	public List getActions() {
		List ret = new ArrayList();
		return ret;
	}

	public boolean isPickerMode() {
		return this.linkProvider.isPickerMode();
	}
}
