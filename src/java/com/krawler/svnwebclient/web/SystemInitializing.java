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
package com.krawler.svnwebclient.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.svn.core.SVNException;

import com.krawler.svnwebclient.SVNWebClientException;
import com.krawler.svnwebclient.authorization.UserCredentials;
import com.krawler.svnwebclient.authorization.impl.CredentialsManager;
import com.krawler.svnwebclient.configuration.ConfigurationProvider;
import com.krawler.svnwebclient.data.DataProviderException;
import com.krawler.svnwebclient.data.DataProviderFactory;
import com.krawler.svnwebclient.data.IDataProvider;
import com.krawler.svnwebclient.data.javasvn.DataProvider;
import com.krawler.svnwebclient.web.model.LinkProviderFactory;
import com.krawler.svnwebclient.web.resource.Links;
import com.krawler.svnwebclient.web.support.AbstractRequestHandler;
import com.krawler.svnwebclient.web.support.RequestParameters;
import com.krawler.svnwebclient.web.support.State;

public class SystemInitializing {
	public static final String ORIGINAL_URL = "originalUrl";
	public static final String ID = "id";
	protected State state;
	protected boolean isPickerInstance = false;
	protected boolean isSingleRevision;
	protected boolean isMultiSelectionUrl;

	public IDataProvider init(HttpServletRequest request,
			HttpServletResponse response, AbstractRequestHandler requestHandler)
			throws SVNWebClientException {

		/*
		 * if
		 * (ConfigurationProvider.getInstance().getConfigurationError().isError()) {
		 * throw new
		 * SVNWebClientException(ConfigurationProvider.getInstance().getConfigurationError().getException()); }
		 */

		this.state = new State(request, response);
		CredentialsManager credentialsManager = new CredentialsManager();
		UserCredentials userCredentials = null;

		userCredentials = credentialsManager.getUserCredentials(request,
				response);
		if (userCredentials == null) {
			return null;
		}

		String id = (String) request.getSession().getAttribute(
				SystemInitializing.ID);

		String url = this.getRepositoryLocation(requestHandler);
		if (id == null) {
			try {
				id = DataProvider.getID(url, userCredentials.getUsername(),
						userCredentials.getPassword());
				if (ConfigurationProvider.getInstance().isMultiRepositoryMode()) {
					DataProvider.startup(userCredentials.getUsername(),
							userCredentials.getPassword(), id, url);
				}
			} catch (DataProviderException de) {
				throw new SVNWebClientException(de);

			} catch (SVNException se) {
				throw new SVNWebClientException(se);
			}
			request.getSession().setAttribute(SystemInitializing.ID, id);
			this.checkRestrictedAccess();
		}

		IDataProvider dataProvider = DataProviderFactory.getDataProvider();
		dataProvider
				.setRelativeLocation(id, requestHandler.getRepositoryName());
		dataProvider.connect(userCredentials, id, url);
		return dataProvider;
	}

	protected void checkRestrictedAccess() {
		String originalUrl = (String) this.state.getRequest().getSession()
				.getAttribute(SystemInitializing.ORIGINAL_URL);
		if (originalUrl != null) {
			String currentUri = this.state.getRequest().getServletPath()
					.substring(1);
			if (!originalUrl.equals(currentUri)) {
				AttributeStorage.getInstance().cleanSession(
						this.state.getRequest().getSession());
			}
		}
	}

	public void redirectToRestrictPage() {
		String query = this.state.getRequest().getQueryString();
		String uri = this.state.getRequest().getServletPath();
		this.state.getRequest().getSession().setAttribute(
				SystemInitializing.ORIGINAL_URL, uri.substring(1));
		try {
			String restrictUrl = Links.RESTRICTED_ACCESS + "?" + query;
			if (this.isPickerInstance) {
				restrictUrl += "&" + RequestParameters.CONTENT_MODE_TYPE + "="
						+ LinkProviderFactory.PICKER_CONTENT_MODE_VALUE;
			}
			this.state.getResponse().sendRedirect(restrictUrl);
		} catch (IOException e) {
		}
	}

	protected String getRepositoryLocation(AbstractRequestHandler requestHandler) {
		return ConfigurationProvider.getInstance().getRepositoryLocation(
				requestHandler.getRepositoryName());
	}

	public void setIsPickerInstance() {
		this.isPickerInstance = true;
	}

	public void setPickerFields(boolean isSingleRevision,
			boolean isMultiSelectionUrl) {
		this.isSingleRevision = isSingleRevision;
		this.isMultiSelectionUrl = isMultiSelectionUrl;
	}
}
