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
package com.krawler.svnwebclient.authorization.impl;

import java.util.StringTokenizer;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.tmatesoft.svn.core.SVNException;

import com.krawler.svnwebclient.SVNWebClientException;
import com.krawler.svnwebclient.authorization.ICredentialsManager;
import com.krawler.svnwebclient.authorization.UserCredentials;
import com.krawler.svnwebclient.configuration.ConfigurationProvider;
import com.krawler.svnwebclient.data.javasvn.DataProvider;
import com.krawler.svnwebclient.web.SystemInitializing;
import com.krawler.svnwebclient.web.resource.Links;
import com.krawler.svnwebclient.web.support.AbstractRequestHandler;
import com.krawler.svnwebclient.web.support.RequestHandler;

public class CredentialsManager implements ICredentialsManager {
	public static final String CREDENTIALS = "credentials";

	public UserCredentials getUserCredentials(HttpServletRequest request,
			HttpServletResponse response) throws SVNWebClientException {
		UserCredentials credentials = (UserCredentials) request.getSession()
				.getAttribute(CredentialsManager.CREDENTIALS);
		String username = null;
		String password = null;
		AbstractRequestHandler requestHandler = this.getRequestHandler(request);

		if (credentials == null) {
			// BasicAuth mode
			if (ConfigurationProvider.getInstance().isBasicAuth()) {
				if (this.isBasicAuthentication(request)) {
					UserCredentials basicCredentials = this
							.getBasicAuthenticationCredentials(request);
					username = basicCredentials.getUsername();
					password = basicCredentials.getPassword();
				} else {
					username = ConfigurationProvider.getInstance()
							.getUsername();
					password = ConfigurationProvider.getInstance()
							.getPassword();
				}
			} else {
				username = requestHandler.getUsername();
				password = requestHandler.getPassword();
			}

			// download manager calls
			if (this.isBasicAuthentication(request)) {
				UserCredentials basicCredentials = this
						.getBasicAuthenticationCredentials(request);
				if (basicCredentials != null) {
					username = basicCredentials.getUsername();
					password = basicCredentials.getPassword();
				}
			}

			credentials = new UserCredentials(username, password);
			request.getSession().setAttribute(CredentialsManager.CREDENTIALS,
					credentials);

			String url = this.getRepositoryLocation(requestHandler);
			try {
				DataProvider.verify(url, credentials.getUsername(), credentials
						.getPassword());
				if ("".equals(password)
						&& request.getSession().getAttribute(
								ICredentialsManager.IS_LOGGED_IN) != null) {
					this.forceCredentialsRequest(request, response);
					return null;
				} else {
					request.getSession().setAttribute(
							ICredentialsManager.IS_LOGGED_IN, "exist");
				}
			} catch (SVNException se) {
				/*
				 * Logger.getLogger(this.getClass()).debug( "It's not allowed to
				 * enter, your credentials:\t" + "username - " +
				 * credentials.getUsername() + " , password - " +
				 * credentials.getPassword() + " url - " + url);
				 * this.forceCredentialsRequest(request, response);
				 */
				return null;
			}
		}

		Logger.getLogger(this.getClass()).debug(
				"Credentials: \nUsername: " + credentials.getUsername() + "   "
						+ credentials.getPassword());
		return credentials;
	}

	protected boolean isBasicAuthentication(HttpServletRequest request) {
		String auth = request.getHeader("Authorization");
		if (auth != null && !auth.equals("")
				&& auth.toLowerCase().startsWith("basic")) {
			return true;
		} else {
			return false;
		}
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request);
	}

	protected String getRepositoryLocation(AbstractRequestHandler requestHandler) {
		String res = "";
		if (!ConfigurationProvider.getInstance().isMultiRepositoryMode()) {
			res = ConfigurationProvider.getInstance().getRepositoryUrl();
		} else {
			res += ConfigurationProvider.getInstance()
					.getParentRepositoryDirectory()
					+ "/" + requestHandler.getRepositoryName();
		}
		return res;
	}

	protected UserCredentials getBasicAuthenticationCredentials(
			HttpServletRequest request) {
		UserCredentials res = null;
		String auth = request.getHeader("Authorization");
		auth = auth.substring(auth.lastIndexOf(" ") + 1, auth.length());
		String authStr = new String(Base64.decodeBase64(auth.getBytes()));
		StringTokenizer stringtokenizer = new StringTokenizer(authStr, ":");
		if (stringtokenizer.hasMoreTokens()) {
			res = new UserCredentials();
			res.setUsername(stringtokenizer.nextToken().toLowerCase());
		}
		if (stringtokenizer.hasMoreTokens()) {
			res.setPassword(stringtokenizer.nextToken());
		}
		return res;
	}

	protected void forceCredentialsRequest(HttpServletRequest request,
			HttpServletResponse response) {
		request.getSession().setAttribute(CredentialsManager.CREDENTIALS, null);
		request.getSession().setAttribute(SystemInitializing.ID, null);
		try {
			if (ConfigurationProvider.getInstance().isBasicAuth()) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.setHeader("WWW-Authenticate", "BASIC realm=\""
						+ ConfigurationProvider.getInstance().getBasicRealm()
						+ "\"");
				response.sendError(401);
			} else {
				RequestDispatcher dispatcher = request
						.getRequestDispatcher(Links.LOGIN);
				dispatcher.forward(request, response);
			}
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error(e, e);
		}
	}

}
