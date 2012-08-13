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
package com.krawler.svnwebclient.web.controller.directory;

import java.util.ArrayList;
import java.util.List;

import com.krawler.svnwebclient.configuration.ConfigurationProvider;
import com.krawler.svnwebclient.util.UrlGenerator;
import com.krawler.svnwebclient.util.UrlGeneratorFactory;
import com.krawler.svnwebclient.util.UrlUtil;
import com.krawler.svnwebclient.web.model.Button;
import com.krawler.svnwebclient.web.model.Navigation;
import com.krawler.svnwebclient.web.model.data.directory.DirectoryContent;
import com.krawler.svnwebclient.web.resource.Images;
import com.krawler.svnwebclient.web.resource.Links;
import com.krawler.svnwebclient.web.support.RequestParameters;

public class DirectoryContentBean extends BaseDirectoryContentBean {

	public DirectoryContentBean() {
	}

	protected boolean executeExtraFunctionality() {
		return true;
	}

	public DirectoryContent getDirectoryContent() {
		DirectoryContent ret = new DirectoryContent(this.directoryElement,
				this.requestHandler, ConfigurationProvider.getInstance()
						.getRevisionDecorator());
		ret.setState(this.state);
		ret.setHeadRevision(this.headRevision);
		ret.setUrl(this.url);
		ret.applySort(this.sortManager.getComparator());
		return ret;
	}

	public String getDeleteUrl() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.DELETE, this.requestHandler.getLocation());
		urlGenerator.addParameter(RequestParameters.URL, UrlUtil
				.encode(this.url));
		return urlGenerator.getUrl();
	}

	public List getActions() {
		List ret = new ArrayList();
		UrlGenerator urlGenerator;

		urlGenerator = UrlGeneratorFactory.getUrlGenerator(Links.REVISION_LIST,
				this.requestHandler.getLocation());
		urlGenerator.addParameter(RequestParameters.URL, UrlUtil
				.encode(this.url));
		long startRevision = -1;
		if (this.requestHandler.getCurrentRevision() != -1) {
			startRevision = this.requestHandler.getCurrentRevision();
			urlGenerator.addParameter(RequestParameters.CREV, Long
					.toString(this.requestHandler.getCurrentRevision()));
		} else {
			startRevision = this.headRevision;
		}

		urlGenerator.addParameter(RequestParameters.START_REVISION, Long
				.toString(startRevision));
		ret.add(new Button(urlGenerator.getUrl(), Images.REVISION_LIST,
				"Revision list"));

		if (this.requestHandler.getCurrentRevision() == -1) {
			urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					Links.DIRECTORY_ADD, this.requestHandler.getLocation());
			urlGenerator.addParameter(RequestParameters.URL, UrlUtil
					.encode(this.url));
			ret.add(new Button(urlGenerator.getUrl(), Images.ADD_DIRECTORY,
					"Add directory"));

			urlGenerator = UrlGeneratorFactory.getUrlGenerator(Links.FILE_ADD,
					this.requestHandler.getLocation());
			urlGenerator.addParameter(RequestParameters.URL, UrlUtil
					.encode(this.url));
			ret.add(new Button(urlGenerator.getUrl(), Images.ADD_FILE,
					"Add file"));

			urlGenerator = new UrlGenerator("javascript:checkDelete()");
			ret.add(new Button(urlGenerator.getUrl(), Images.DELETE,
					"Delete selected elements"));
		}

		try {
			urlGenerator = UrlGeneratorFactory
					.getUrlGenerator(Links.DOWNLOAD_DIRECTORY,
							this.requestHandler.getLocation());
			urlGenerator.addParameter(RequestParameters.URL, UrlUtil
					.encode(this.url));
			if (this.requestHandler.getCurrentRevision() != -1) {
				urlGenerator.addParameter(RequestParameters.CREV, Long
						.toString(this.requestHandler.getCurrentRevision()));
			}
			ret.add(new Button(urlGenerator.getUrl(),
					Images.DOWNLOAD_DIRECTORY,
					"Download current directory as ZIP"));
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		return ret;
	}

	public Navigation getNavigation() {
		return new Navigation(this.url, this.requestHandler.getLocation(),
				this.requestHandler.getCurrentRevision(), false);
	}
}
