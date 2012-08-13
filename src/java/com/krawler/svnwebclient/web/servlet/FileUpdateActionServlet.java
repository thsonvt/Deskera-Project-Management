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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.util.UUID;

import com.krawler.svnwebclient.SVNWebClientException;
import com.krawler.svnwebclient.configuration.ConfigurationProvider;
import com.krawler.svnwebclient.data.AuthenticationException;
import com.krawler.svnwebclient.data.DataProviderException;
import com.krawler.svnwebclient.data.IDataProvider;
import com.krawler.svnwebclient.data.model.DataChangedElement;
import com.krawler.svnwebclient.util.FileUtil;
import com.krawler.svnwebclient.util.Uploader;
import com.krawler.svnwebclient.util.UrlGenerator;
import com.krawler.svnwebclient.util.UrlGeneratorFactory;
import com.krawler.svnwebclient.util.UrlUtil;
import com.krawler.svnwebclient.web.AttributeStorage;
import com.krawler.svnwebclient.web.controller.ChangeConfirmation;
import com.krawler.svnwebclient.web.model.Navigation;
import com.krawler.svnwebclient.web.model.data.ChangeResult;
import com.krawler.svnwebclient.web.resource.Links;
import com.krawler.svnwebclient.web.support.FormParameters;
import com.krawler.svnwebclient.web.support.RequestHandler;
import com.krawler.svnwebclient.web.support.RequestParameters;

public class FileUpdateActionServlet extends AbstractServlet {
	protected static final long CURRENT_REVISION = -1;

	private static final long serialVersionUID = 613754791814175593L;

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
		ChangeResult changeResult = null;
		String temporaryDirectory = ConfigurationProvider.getInstance()
				.getTempDirectory();
		String destinationDirectory = temporaryDirectory + "/" + UUID.getUUID();
		Map parameters = null;
		String name = null;
		String comment = null;
		try {

			Uploader uploader = new Uploader();
			uploader.doPost(this.state.getRequest(), this.state.getResponse(),
					destinationDirectory, temporaryDirectory);
			parameters = uploader.getParameters();
			name = (String) parameters.get(FormParameters.FILE_NAME);

			if (uploader.isUploaded()) {
				comment = (String) parameters.get(FormParameters.COMMENT);
				AttributeStorage handler = AttributeStorage.getInstance();

				HttpSession session = this.state.getRequest().getSession();
				handler.addParameter(session, FormParameters.FILE_NAME, name);
				handler.addParameter(session, FormParameters.COMMENT, comment);
				handler.addParameter(session,
						FileAddActionServlet.DESTINATION_DIRECTORY,
						destinationDirectory);
			} else {
				this.retryAgain(name);
				return;
			}
		} catch (Exception e) {
			AttributeStorage stotage = AttributeStorage.getInstance();
			HttpSession session = this.state.getRequest().getSession();
			name = (String) stotage.getParameter(session,
					FormParameters.FILE_NAME);
			comment = (String) stotage.getParameter(session,
					FormParameters.COMMENT);
			destinationDirectory = (String) stotage.getParameter(session,
					FileAddActionServlet.DESTINATION_DIRECTORY);
		}
		try {
			Navigation navigation = new Navigation(
					this.requestHandler.getUrl(), this.requestHandler
							.getLocation(),
					FileUpdateActionServlet.CURRENT_REVISION, true);

			DataChangedElement changedElement = dataProvider.commitFile(
					this.requestHandler.getUrl(), destinationDirectory + "/"
							+ name, comment);
			List elements = new ArrayList();
			ChangeResult.Element element = new ChangeResult.Element();
			element.setAuthor(changedElement.getAuthor());
			element.setComment(changedElement.getComment());
			element.setDate(changedElement.getDate());
			element.setDirectory(false);
			element.setName(changedElement.getName());
			element.setRevision(changedElement.getRevision());
			elements.add(element);
			String message = "File " + name + " was succesfully commited";
			changeResult = new ChangeResult(true, message, elements, navigation);
		} catch (AuthenticationException ae) {
			throw ae;
		} catch (DataProviderException e) {
			AttributeStorage.getInstance().cleanSession(
					this.state.getRequest().getSession());
			FileUtil.deleteDirectory(new File(destinationDirectory));
			throw e;
		}

		AttributeStorage.getInstance().cleanSession(
				this.state.getRequest().getSession());
		FileUtil.deleteDirectory(new File(destinationDirectory));
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

	protected void retryAgain(String fileName) throws SVNWebClientException {
		String errorMessage = "Did not update "
				+ fileName
				+ " as the file was empty or didn't exist. Please, retry again.";

		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.FILE_ADD, this.requestHandler.getLocation());
		urlGenerator.addParameter(RequestParameters.URL, UrlUtil
				.encode(this.requestHandler.getUrl()));
		urlGenerator.addParameter(RequestParameters.RETRY_AGAIN,
				RequestParameters.VALUE_TRUE);
		urlGenerator.addParameter(RequestParameters.ERROR_MESSAGE, UrlUtil
				.encode(errorMessage));
		try {
			this.state.getResponse().sendRedirect(urlGenerator.getUrl());
		} catch (IOException e) {
			throw new SVNWebClientException(e);
		}
	}

	protected RequestHandler getRequestHandler(HttpServletRequest request) {
		return new RequestHandler(request);
	}
}
