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
import com.krawler.svnwebclient.data.IDataProvider;
import com.krawler.svnwebclient.web.model.Link;
import com.krawler.svnwebclient.web.model.Navigation;
import com.krawler.svnwebclient.web.model.data.ChangeResult;
import com.krawler.svnwebclient.web.support.AbstractRequestHandler;
import com.krawler.svnwebclient.web.support.RequestHandler;

public class ChangeConfirmation extends AbstractBean {
	public static final String CHANGE_RESULT = "changeresult";

	protected ChangeResult changeResult;

	public ChangeConfirmation() {
	}

	protected boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		this.changeResult = (ChangeResult) this.state.getRequest().getSession()
				.getAttribute(ChangeConfirmation.CHANGE_RESULT);
		this.state.getRequest().getSession().removeAttribute(
				ChangeConfirmation.CHANGE_RESULT);
		return true;
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request);
	}

	public ChangeResult getChangeResult() {
		return this.changeResult;
	}

	public String getOkUrl() {
		List navigationPath = this.changeResult.getNavigation().getPath();
		Link lastElement = (Link) navigationPath.get(navigationPath.size() - 1);
		return lastElement.getUrl();
	}

	public Navigation getNavigation() {
		return this.changeResult.getNavigation();
	}

	public List getActions() {
		List ret = new ArrayList();
		return ret;
	}
}
