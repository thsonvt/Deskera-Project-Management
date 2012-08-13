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
package com.krawler.svnwebclient.web.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.krawler.svnwebclient.SVNWebClientException;
import com.krawler.svnwebclient.data.AuthenticationException;
import com.krawler.svnwebclient.data.DataProviderException;
import com.krawler.svnwebclient.data.IDataProvider;
import com.krawler.svnwebclient.data.IncorrectParameterException;
import com.krawler.svnwebclient.data.model.DataChangedElement;
import com.krawler.svnwebclient.data.model.DataRepositoryElement;
import com.krawler.svnwebclient.web.AttributeStorage;
import com.krawler.svnwebclient.web.controller.ChangeConfirmation;
import com.krawler.svnwebclient.web.model.Navigation;
import com.krawler.svnwebclient.web.model.data.ChangeResult;
import com.krawler.svnwebclient.web.resource.Links;
import com.krawler.svnwebclient.web.support.FormParameters;
import com.krawler.svnwebclient.web.support.RequestHandler;

public class DirectoryAddActionServlet extends AbstractServlet {
	protected static final long CURRENT_REVISION = -1;

	private static final long serialVersionUID = -7706208408425844239L;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.execute(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.execute(request, response);
	}

	protected void executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		String name = this.state.getRequest().getParameter(
				FormParameters.DIRECTORY_NAME);
		String comment = this.state.getRequest().getParameter(
				FormParameters.COMMENT);
		if (name == null) {
			AttributeStorage handler = AttributeStorage.getInstance();
			HttpSession session = this.state.getRequest().getSession();
			name = (String) handler.getParameter(session,
					FormParameters.DIRECTORY_NAME);
			comment = (String) handler.getParameter(session,
					FormParameters.COMMENT);
		} else {
			AttributeStorage handler = AttributeStorage.getInstance();
			HttpSession session = this.state.getRequest().getSession();
			handler.addParameter(session, FormParameters.DIRECTORY_NAME, name);
			handler.addParameter(session, FormParameters.COMMENT, comment);
		}

		long revision = dataProvider.getHeadRevision();
		DataRepositoryElement dataRepositoryElement = null;

		String directoryUrl = this.requestHandler.getUrl();
		if (!directoryUrl.endsWith("/")) {
			directoryUrl += "/";
		}
		directoryUrl += name;

		ChangeResult changeResult = null;
		Navigation navigation = new Navigation(this.requestHandler.getUrl(),
				this.requestHandler.getLocation(),
				DirectoryAddActionServlet.CURRENT_REVISION, false);

		try {
			// if there's no path, handle IncorrectParameterException
			dataRepositoryElement = dataProvider
					.getInfo(directoryUrl, revision);

			List elements = new ArrayList();
			ChangeResult.Element element = new ChangeResult.Element();
			element.setAuthor(dataRepositoryElement.getAuthor());
			element.setComment(dataRepositoryElement.getComment());
			element.setDate(dataRepositoryElement.getDate());
			element.setDirectory(true);
			element.setName(dataRepositoryElement.getName());
			element.setRevision(dataRepositoryElement.getRevision());
			elements.add(element);
			String message = "Directory " + name
					+ " already exists in specified location";
			changeResult = new ChangeResult(false, message, elements,
					navigation);
		} catch (IncorrectParameterException ie) {
			DataChangedElement changedElement = dataProvider.createDirectory(
					this.requestHandler.getUrl(), name, comment);
			List elements = new ArrayList();
			ChangeResult.Element element = new ChangeResult.Element();
			element.setAuthor(changedElement.getAuthor());
			element.setComment(changedElement.getComment());
			element.setDate(changedElement.getDate());
			element.setDirectory(true);
			element.setName(changedElement.getName());
			element.setRevision(changedElement.getRevision());
			elements.add(element);
			String message = "Directory " + name + " was succesfully added";
			changeResult = new ChangeResult(true, message, elements, navigation);
		} catch (AuthenticationException ae) {
			throw ae;
		} catch (DataProviderException e) {
			AttributeStorage.getInstance().cleanSession(
					this.state.getRequest().getSession());
			throw e;
		}

		AttributeStorage.getInstance().cleanSession(
				this.state.getRequest().getSession());
		this.state.getRequest().getSession().setAttribute(
				ChangeConfirmation.CHANGE_RESULT, changeResult);
		RequestDispatcher dispatcher = this.state.getRequest()
				.getRequestDispatcher(Links.CHANGE_CONFIRMATION);
		try {
			dispatcher.forward(this.state.getRequest(), this.state
					.getResponse());
		} catch (Exception e) {
			throw new SVNWebClientException(e);
		}
	}

	protected RequestHandler getRequestHandler(HttpServletRequest request) {
		return new RequestHandler(request);
	}
}
