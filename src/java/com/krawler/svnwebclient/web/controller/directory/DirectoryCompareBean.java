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
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.krawler.svnwebclient.SVNWebClientException;
import com.krawler.svnwebclient.data.DataProviderException;
import com.krawler.svnwebclient.data.IDataProvider;
import com.krawler.svnwebclient.data.model.DataDirectoryCompareItem;
import com.krawler.svnwebclient.data.model.DataDirectoryElement;
import com.krawler.svnwebclient.data.model.DataRepositoryElement;
import com.krawler.svnwebclient.data.model.DataRevision;
import com.krawler.svnwebclient.util.UrlGenerator;
import com.krawler.svnwebclient.util.UrlGeneratorFactory;
import com.krawler.svnwebclient.util.UrlUtil;
import com.krawler.svnwebclient.web.controller.AbstractBean;
import com.krawler.svnwebclient.web.model.Navigation;
import com.krawler.svnwebclient.web.model.data.RevisionDetails;
import com.krawler.svnwebclient.web.model.data.directory.DirectoryCompare;
import com.krawler.svnwebclient.web.model.sort.DirectoryCompareSortManager;
import com.krawler.svnwebclient.web.resource.Links;
import com.krawler.svnwebclient.web.support.AbstractRequestHandler;
import com.krawler.svnwebclient.web.support.RequestException;
import com.krawler.svnwebclient.web.support.RequestHandler;
import com.krawler.svnwebclient.web.support.RequestParameters;

public class DirectoryCompareBean extends AbstractBean {
	protected long headRevision;
	protected long revision;
	protected List compareItems;
	protected DataRevision startRevision;
	protected DataRevision endRevision;
	protected DirectoryCompareSortManager sortManager;

	public DirectoryCompareBean() {
	}

	protected boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		this.headRevision = dataProvider.getHeadRevision();
		if (this.requestHandler.getPegRevision() != -1) {
			this.revision = this.requestHandler.getPegRevision();
		} else if (this.requestHandler.getCurrentRevision() != -1) {
			this.revision = this.requestHandler.getCurrentRevision();
		} else {
			this.revision = this.headRevision;
		}

		this.sortManager = new DirectoryCompareSortManager();

		if (this.requestHandler.getStartRevision() != -1) {
			this.startRevision = dataProvider
					.getRevisionInfo(this.requestHandler.getStartRevision());
		}
		if (this.requestHandler.getEndRevision() != -1) {
			this.endRevision = dataProvider.getRevisionInfo(this.requestHandler
					.getEndRevision());
		}

		if ((this.requestHandler.getStartRevision() != -1)
				&& (this.requestHandler.getEndRevision() != -1)) {
			String location = dataProvider.getLocation(this.requestHandler
					.getUrl(), this.revision, this.requestHandler
					.getStartRevision());
			this.compareItems = dataProvider.compareDirectoryRevisions(
					location, this.requestHandler.getStartRevision(),
					this.requestHandler.getEndRevision());
		} else {
			if (this.requestHandler.getStartRevision() == -1) {
				this.compareItems = this.getDirectoryContent(dataProvider,
						this.requestHandler.getUrl(), this.requestHandler
								.getEndRevision(), true);
			} else if (this.requestHandler.getEndRevision() == -1) {
				this.compareItems = this.getDirectoryContent(dataProvider,
						this.requestHandler.getUrl(), this.requestHandler
								.getStartRevision(), false);
			}
		}

		return true;
	}

	protected List getDirectoryContent(IDataProvider dataProvider, String url,
			long revision, boolean added) throws DataProviderException {
		List ret = new ArrayList();
		DataDirectoryElement directory = dataProvider.getDirectory(url,
				revision, true);
		for (Iterator i = directory.getChildElements().iterator(); i.hasNext();) {
			DataRepositoryElement element = (DataRepositoryElement) i.next();
			DataDirectoryCompareItem compareItem = null;
			if (added) {
				compareItem = new DataDirectoryCompareItem(element.getName(),
						DataDirectoryCompareItem.OPERATION_ADD);
				compareItem.setDirectory(element.isDirectory());
				compareItem.setOldRevision(-1);
				compareItem.setNewRevision(revision);
			} else {
				compareItem = new DataDirectoryCompareItem(element.getName(),
						DataDirectoryCompareItem.OPERATION_DELETE);
				compareItem.setDirectory(element.isDirectory());
				compareItem.setOldRevision(revision);
				compareItem.setNewRevision(-1);
			}
			ret.add(compareItem);
		}
		return ret;
	}

	public RevisionDetails getStartRevisionInfo() {
		if (this.startRevision == null) {
			return null;
		}
		return new RevisionDetails(this.startRevision, this.headRevision, null,
				null, null);
	}

	public RevisionDetails getEndRevisionInfo() {
		if (this.endRevision == null) {
			return null;
		}
		return new RevisionDetails(this.endRevision, this.headRevision, null,
				null, null);
	}

	public String getStartRevisionUrl() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.REVISION, this.requestHandler.getLocation());
		String url = this.requestHandler.getUrl();
		urlGenerator.addParameter(RequestParameters.URL, UrlUtil.encode(url));
		if (this.requestHandler.getCurrentRevision() != -1) {
			urlGenerator.addParameter(RequestParameters.CREV, Long
					.toString(this.requestHandler.getCurrentRevision()));
		}
		urlGenerator.addParameter(RequestParameters.REV, Long
				.toString(this.requestHandler.getStartRevision()));
		return urlGenerator.getUrl();
	}

	public String getEndRevisionUrl() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.REVISION, this.requestHandler.getLocation());
		String url = this.requestHandler.getUrl();
		urlGenerator.addParameter(RequestParameters.URL, UrlUtil.encode(url));
		if (this.requestHandler.getCurrentRevision() != -1) {
			urlGenerator.addParameter(RequestParameters.CREV, Long
					.toString(this.requestHandler.getCurrentRevision()));
		}
		urlGenerator.addParameter(RequestParameters.REV, Long
				.toString(this.requestHandler.getEndRevision()));
		return urlGenerator.getUrl();
	}

	public DirectoryCompare getCompareResult() {
		DirectoryCompare ret = new DirectoryCompare(this.compareItems,
				this.requestHandler, this.requestHandler.getUrl(),
				this.requestHandler.getStartRevision(), this.requestHandler
						.getEndRevision());
		ret.applySort(this.sortManager.getComparator());
		return ret;
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request) {
			public void check() throws RequestException {
				this.checkLong(RequestParameters.STARTREV);
				this.checkLong(RequestParameters.ENDREV);
			}
		};
	}

	public Navigation getNavigation() {
		return new Navigation(this.requestHandler.getUrl(), this.requestHandler
				.getLocation(), this.requestHandler.getCurrentRevision(), true);
	}

	public List getActions() {
		List ret = new ArrayList();
		return ret;
	}
}
