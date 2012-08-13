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

import javax.servlet.http.HttpServletRequest;

import com.krawler.svnwebclient.SVNWebClientException;
import com.krawler.svnwebclient.data.IDataProvider;
import com.krawler.svnwebclient.data.model.DataDirectoryElement;
import com.krawler.svnwebclient.web.controller.AbstractBean;
import com.krawler.svnwebclient.web.model.sort.DirectoryContentSortManager;
import com.krawler.svnwebclient.web.support.AbstractRequestHandler;
import com.krawler.svnwebclient.web.support.RequestHandler;

public abstract class BaseDirectoryContentBean extends AbstractBean {
	protected long headRevision;
	protected long revision;
	protected String url;
	protected DataDirectoryElement directoryElement;
	protected DirectoryContentSortManager sortManager;

	public BaseDirectoryContentBean() {
	}

	protected boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		this.headRevision = dataProvider.getHeadRevision();
		this.revision = this.headRevision;
		if (this.requestHandler.getCurrentRevision() != -1) {
			this.revision = this.requestHandler.getCurrentRevision();
		}
		this.url = this.requestHandler.getUrl();
		if (this.requestHandler.getPegRevision() != -1) {
			this.url = dataProvider.getLocation(this.requestHandler.getUrl(),
					this.requestHandler.getPegRevision(), this.revision);
		}
		this.sortManager = new DirectoryContentSortManager(this.state,
				this.requestHandler);
		this.directoryElement = dataProvider.getDirectory(this.url,
				this.revision);

		if (this.executeExtraFunctionality()) {
			return true;
		} else {
			return false;
		}
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request);
	}

	public DirectoryContentSortManager getSortManager() {
		return this.sortManager;
	}

	protected abstract boolean executeExtraFunctionality();
}
