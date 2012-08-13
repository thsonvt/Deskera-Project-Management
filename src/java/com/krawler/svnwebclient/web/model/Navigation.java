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
package com.krawler.svnwebclient.web.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.krawler.svnwebclient.configuration.ConfigurationProvider;
import com.krawler.svnwebclient.util.UrlGenerator;
import com.krawler.svnwebclient.util.UrlGeneratorFactory;
import com.krawler.svnwebclient.util.UrlUtil;
import com.krawler.svnwebclient.web.resource.Images;
import com.krawler.svnwebclient.web.resource.Links;
import com.krawler.svnwebclient.web.support.RequestParameters;

public class Navigation {
	public static final String REPOSITORY = "Repository";

	protected List path = new ArrayList();
	protected List buttons = new ArrayList();
	protected long currentRevision;
	protected String currentPathMode;
	protected String urlPrefix;
	protected String urlSuffix;
	protected String url;
	protected String location;
	protected ILinkProvider linkProvider;
	protected boolean isSingleRevision;
	protected boolean isMultiUrlSelection;

	protected Navigation(String url, String location, long currentRevision,
			Boolean lastFile, ILinkProvider linkProvider,
			boolean isSingleRevision, boolean isMultiUrlSelection) {
		this.url = url;
		this.location = location;
		this.currentRevision = currentRevision;
		this.linkProvider = linkProvider;
		this.isSingleRevision = isSingleRevision;
		this.isMultiUrlSelection = isMultiUrlSelection;

		String linkName = ConfigurationProvider.getInstance()
				.isMultiRepositoryMode() ? location : Navigation.REPOSITORY;
		this.path.add(new Link(this.getNavigationUrl("", this.currentRevision,
				new Boolean(false)), linkName));

		List pathChain = UrlUtil.getPathChain(url);
		for (Iterator i = pathChain.iterator(); i.hasNext();) {
			String pathElement = (String) i.next();
			String pathElementName = UrlUtil.getLastPathElement(pathElement);
			if (i.hasNext()) {
				this.path.add(new Link(this.getNavigationUrl(pathElement,
						this.currentRevision, new Boolean(false)),
						pathElementName));
			} else {
				this.path.add(new Link(this.getNavigationUrl(pathElement,
						this.currentRevision, lastFile), pathElementName));
			}
		}

		if (ConfigurationProvider.getInstance().isPathAutodetect()) {
			for (int i = pathChain.size() - 1; i >= 0; i--) {
				String pathElement = (String) pathChain.get(i);
				String pathElementName = UrlUtil
						.getLastPathElement(pathElement);
				if (ConfigurationProvider.getInstance().getTrunkName().equals(
						pathElementName)) {
					this.currentPathMode = PathSwitch.TRUNK;
				} else if (ConfigurationProvider.getInstance()
						.getBranchesName().equals(pathElementName)) {
					this.currentPathMode = PathSwitch.BRANCHES;
				} else if (ConfigurationProvider.getInstance().getTagsName()
						.equals(pathElementName)) {
					this.currentPathMode = PathSwitch.TAGS;
				}

				if (this.currentPathMode != null) {
					if (pathElement.length() < url.length()) {
						this.urlSuffix = url
								.substring(pathElement.length() + 1);
					} else {
						this.urlSuffix = "";
					}

					if (PathSwitch.TAGS.equals(this.currentPathMode)
							|| PathSwitch.BRANCHES.equals(this.currentPathMode)) {
						this.urlSuffix = UrlUtil
								.getNextLevelFullPath(this.urlSuffix);
					}

					this.urlPrefix = UrlUtil.getPreviousFullPath(pathElement);
					break;
				}
			}
		}

		if (!isSingleRevision) {
			UrlGenerator urlGenen = UrlGeneratorFactory.getUrlGenerator(
					Links.GOTO_PATH, this.location);
			urlGenen.addParameter(RequestParameters.URL, UrlUtil.encode(url));
			urlGenen.addParameter(RequestParameters.PEGREV, Long
					.toString(this.currentRevision));
			if (isMultiUrlSelection) {
				urlGenen.addParameter(RequestParameters.MULTI_URL_SELECTION);
			}
			this.addExtraParams(urlGenen);
			this.buttons.add(new Button(urlGenen.getUrl(), Images.BROWSE,
					"Navigate to path"));
		}
		if (this.currentRevision != -1) {
			UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					Links.SWITCH_TO_HEAD, this.location);
			urlGenerator.addParameter(RequestParameters.URL, UrlUtil
					.encode(url));
			urlGenerator.addParameter(RequestParameters.PEGREV, Long
					.toString(this.currentRevision));
			if (isMultiUrlSelection) {
				urlGenerator
						.addParameter(RequestParameters.MULTI_URL_SELECTION);
			}
			if (isSingleRevision) {
				urlGenerator.addParameter(RequestParameters.SINGLE_REVISION);
			}
			this.addExtraParams(urlGenerator);
			this.buttons.add(new Button(urlGenerator.getUrl(), Images.HEAD,
					"Switch to head revision"));
		}
		if (this.path.size() > 1) {
			Link previousLink = (Link) this.path.get(this.path.size() - 2);
			this.buttons.add(new Button(previousLink.getUrl(), Images.UP,
					"Go to one level up"));
		}

		// Disable Back button
		// this.buttons.add(new Button("javascript:history.go(-1);",
		// Images.BACK, "Back to previous page"));
	}

	protected void addExtraParams(UrlGenerator urlGenen) {
		if (this.linkProvider != null && this.linkProvider.isPickerMode()) {
			urlGenen.addParameter(RequestParameters.CONTENT_MODE_TYPE,
					LinkProviderFactory.PICKER_CONTENT_MODE_VALUE);
			;
		}
	}

	public Navigation(String url, String location, long currentRevision,
			boolean lastFile, ILinkProvider linkProvider, boolean isSingle,
			boolean isMultiUrlSelection) {
		this(url, location, currentRevision, new Boolean(lastFile),
				linkProvider, isSingle, isMultiUrlSelection);
	}

	public Navigation(String url, String location, long currentRevision,
			ILinkProvider linkProvider, boolean isSingleRevision,
			boolean isMultiUrlSelection) {
		this(url, location, currentRevision, null, linkProvider, false,
				isMultiUrlSelection);
	}

	public Navigation(String url, String location, long currentRevision) {
		this(url, location, currentRevision, null, null, false, false);
	}

	public Navigation(String url, String location, long currentRevision,
			boolean lastFile) {
		this(url, location, currentRevision, new Boolean(lastFile), null,
				false, false);
	}

	protected String getNavigationUrl(String url, long currentRevision,
			Boolean file) {
		UrlGenerator urlGenerator;
		if (file == null) {
			urlGenerator = UrlGeneratorFactory.getUrlGenerator(Links.CONTENT,
					this.location);
			if (this.linkProvider.isPickerMode()) {
				urlGenerator.addParameter(RequestParameters.CONTENT_MODE_TYPE,
						LinkProviderFactory.PICKER_CONTENT_MODE_VALUE);
			}
		} else if (file.booleanValue()) {
			urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					this.linkProvider == null ? Links.FILE_CONTENT
							: this.linkProvider.getFileContentLink(),
					this.location);
		} else {
			urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					this.linkProvider == null ? Links.DIRECTORY_CONTENT
							: this.linkProvider.getDirectoryContentLink(),
					this.location);
		}
		if (url.length() > 0) {
			urlGenerator.addParameter(RequestParameters.URL, UrlUtil
					.encode(url));
		}
		if (currentRevision != -1) {
			urlGenerator.addParameter(RequestParameters.CREV, Long
					.toString(currentRevision));
		}
		if (this.isSingleRevision) {
			urlGenerator.addParameter(RequestParameters.SINGLE_REVISION);
		}
		if (this.isMultiUrlSelection) {
			urlGenerator.addParameter(RequestParameters.MULTI_URL_SELECTION);
		}
		return urlGenerator.getUrl();
	}

	public List getPath() {
		return this.path;
	}

	public List getButtons() {
		return this.buttons;
	}

	public String getCurrentRevision() {
		if (this.currentRevision == -1) {
			return null;
		} else {
			return Long.toString(this.currentRevision);
		}
	}

	public PathSwitch getPathSwitch() {
		PathSwitch res = null;
		if (this.currentPathMode == null) {
			res = null;
		} else if (this.linkProvider == null) {
			res = new PathSwitch(this.location, this.urlPrefix, this.urlSuffix,
					this.currentPathMode, this.url, this.currentRevision);
		} else {
			res = new PathSwitch(this.location, this.urlPrefix, this.urlSuffix,
					this.currentPathMode, this.url, this.currentRevision,
					this.linkProvider.isPickerMode(), this.isSingleRevision,
					this.isMultiUrlSelection);
		}
		return res;
	}

	public boolean isShowPathSwitch() {
		return this.currentPathMode != null;
	}
}
