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
import com.krawler.svnwebclient.util.UrlGenerator;
import com.krawler.svnwebclient.util.UrlGeneratorFactory;
import com.krawler.svnwebclient.util.UrlUtil;
import com.krawler.svnwebclient.web.model.Link;
import com.krawler.svnwebclient.web.model.Navigation;
import com.krawler.svnwebclient.web.model.data.DeletedElementsList;
import com.krawler.svnwebclient.web.resource.Links;
import com.krawler.svnwebclient.web.servlet.DeleteActionServlet;
import com.krawler.svnwebclient.web.support.AbstractRequestHandler;
import com.krawler.svnwebclient.web.support.FormParameters;
import com.krawler.svnwebclient.web.support.RequestHandler;
import com.krawler.svnwebclient.web.support.RequestParameters;

public class DeleteBean extends AbstractBean {
	protected static final String DELETE_FLAG = "1";
	protected static final long CURRENT_REVISION = -1;

	protected Navigation navigation;
	protected String url;
	protected DeletedElementsList deletedElements;

	public DeleteBean() {
	}

	protected boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		this.url = this.requestHandler.getUrl();
		if (this.requestHandler.getPegRevision() != -1) {
			this.url = dataProvider.getLocation(this.requestHandler.getUrl(),
					this.requestHandler.getPegRevision(), dataProvider
							.getHeadRevision());
		}
		this.navigation = new Navigation(this.requestHandler.getUrl(),
				this.requestHandler.getLocation(), DeleteBean.CURRENT_REVISION,
				false);

		String[] flags = this.state.getRequest().getParameterValues(
				FormParameters.FLAGS);
		String[] types = this.state.getRequest().getParameterValues(
				FormParameters.TYPES);
		String[] names = this.state.getRequest().getParameterValues(
				FormParameters.NAMES);
		String[] revisions = this.state.getRequest().getParameterValues(
				FormParameters.REVISIONS);
		String[] sizes = this.state.getRequest().getParameterValues(
				FormParameters.SIZES);
		String[] dates = this.state.getRequest().getParameterValues(
				FormParameters.DATES);
		String[] authors = this.state.getRequest().getParameterValues(
				FormParameters.AUTHORS);
		String[] comments = this.state.getRequest().getParameterValues(
				FormParameters.COMMENTS);

		this.deletedElements = new DeletedElementsList();
		List elements = new ArrayList();
		List deletedElementsNames = new ArrayList();
		for (int i = 0; i < flags.length; i++) {
			if (DeleteBean.DELETE_FLAG.equals(flags[i])) {
				DeletedElementsList.Element element = new DeletedElementsList.Element();
				element.setType(types[i]);
				element.setName(names[i]);
				element.setRevision(revisions[i]);
				element.setSize(sizes[i]);
				element.setDate(dates[i]);
				element.setAuthor(authors[i]);
				element.setComment(comments[i]);
				elements.add(element);

				deletedElementsNames.add(names[i]);
			}
		}
		this.deletedElements.setDeletedElements(elements);
		this.state.getRequest().getSession().setAttribute(
				DeleteActionServlet.DELETED_ELEMENTS, deletedElementsNames);

		return true;
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request);
	}

	public DeletedElementsList getDeletedElements() {
		return this.deletedElements;
	}

	public String getOkUrl() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.DELETE_ACTION, this.requestHandler.getLocation());
		urlGenerator.addParameter(RequestParameters.URL, UrlUtil
				.encode(this.url));
		return urlGenerator.getUrl();
	}

	public String getCancelUrl() {
		List navigationPath = this.navigation.getPath();
		Link lastElement = (Link) navigationPath.get(navigationPath.size() - 1);
		return lastElement.getUrl();
	}

	public Navigation getNavigation() {
		return this.navigation;
	}

	public List getActions() {
		List ret = new ArrayList();
		return ret;
	}
}
