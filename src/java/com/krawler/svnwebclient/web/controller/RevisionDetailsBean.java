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
import com.krawler.svnwebclient.data.model.DataRevision;
import com.krawler.svnwebclient.web.model.Navigation;
import com.krawler.svnwebclient.web.model.data.RevisionDetails;
import com.krawler.svnwebclient.web.model.sort.RevisionDetailsSortManager;
import com.krawler.svnwebclient.web.support.AbstractRequestHandler;
import com.krawler.svnwebclient.web.support.RequestException;
import com.krawler.svnwebclient.web.support.RequestHandler;
import com.krawler.svnwebclient.web.support.RequestParameters;

public class RevisionDetailsBean extends AbstractBean {
	protected DataRevision dataRevision;
	protected long headRevision;
	protected RevisionDetailsSortManager sortManager;

	public RevisionDetailsBean() {
	}

	protected boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		this.sortManager = new RevisionDetailsSortManager();
		this.headRevision = dataProvider.getHeadRevision();
		this.dataRevision = dataProvider.getRevisionInfo(this.requestHandler
				.getRevision());
		return true;
	}

	public RevisionDetails getRevision() {
		RevisionDetails ret = new RevisionDetails(this.dataRevision,
				this.headRevision, ConfigurationProvider.getInstance()
						.getRevisionDecorator(), this.state,
				this.requestHandler);
		ret.applySort(this.sortManager.getComparator());
		return ret;
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request) {
			public void check() throws RequestException {
				this.checkLong(RequestParameters.REV);
			}
		};
	}

	public Navigation getNavigation() {
		return new Navigation(this.requestHandler.getUrl(), this.requestHandler
				.getLocation(), this.requestHandler.getCurrentRevision(), false);
	}

	public List getActions() {
		List ret = new ArrayList();
		return ret;
	}
}
