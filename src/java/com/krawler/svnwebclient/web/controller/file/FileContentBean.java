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
package com.krawler.svnwebclient.web.controller.file;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.krawler.svnwebclient.SVNWebClientException;
import com.krawler.svnwebclient.configuration.ConfigurationProvider;
import com.krawler.svnwebclient.data.IDataProvider;
import com.krawler.svnwebclient.data.model.DataFileElement;
import com.krawler.svnwebclient.decorations.BaseViewProvider;
import com.krawler.svnwebclient.decorations.IAlternativeViewProvider;
import com.krawler.svnwebclient.util.UrlGenerator;
import com.krawler.svnwebclient.util.UrlGeneratorFactory;
import com.krawler.svnwebclient.util.UrlUtil;
import com.krawler.svnwebclient.util.contentencoding.ContentEncodingHelper;
import com.krawler.svnwebclient.web.controller.AbstractBean;
import com.krawler.svnwebclient.web.model.Button;
import com.krawler.svnwebclient.web.model.Navigation;
import com.krawler.svnwebclient.web.model.data.file.FileContent;
import com.krawler.svnwebclient.web.resource.Images;
import com.krawler.svnwebclient.web.resource.Links;
import com.krawler.svnwebclient.web.support.AbstractRequestHandler;
import com.krawler.svnwebclient.web.support.RequestException;
import com.krawler.svnwebclient.web.support.RequestHandler;
import com.krawler.svnwebclient.web.support.RequestParameters;

public class FileContentBean extends AbstractBean {
	public static final String FILE_CONTENT = "filecontent";

	protected long headRevision;
	protected long revision;
	protected long line;
	protected String url;
	protected DataFileElement fileElement;

	public FileContentBean() {
	}

	protected boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		this.headRevision = dataProvider.getHeadRevision();
		this.revision = this.headRevision;
		if (this.requestHandler.getCurrentRevision() != -1) {
			this.revision = this.requestHandler.getCurrentRevision();
		}
		this.line = this.requestHandler.getLine();
		this.url = this.requestHandler.getUrl();
		if (this.requestHandler.getPegRevision() != -1) {
			this.url = dataProvider.getLocation(this.requestHandler.getUrl(),
					this.requestHandler.getPegRevision(), this.revision);
		}
		this.fileElement = (DataFileElement) dataProvider.getInfo(this.url,
				this.revision);
		return true;
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request) {
			public void check() throws RequestException {
				this.checkNotNullOrEmpty(RequestParameters.URL);
			}
		};
	}

	public FileContent getFileContent() {
		return new FileContent(this.fileElement, this.requestHandler,
				this.headRevision, this.url);
	}

	public Map getViews() {
		Map ret = new HashMap();

		BaseViewProvider baseViewProvider = new BaseViewProvider(
				this.requestHandler.getLocation());
		String[] baseViews = baseViewProvider.getAvailableAlternativeViews(
				this.url, this.revision);
		if (baseViews != null) {
			for (int i = 0; i < baseViews.length; i++) {
				ret.put(baseViews[i], baseViewProvider
						.getAlternativeViewContentUrl(this.url, this.revision,
								line, baseViews[i]));
			}
		}

		IAlternativeViewProvider alternativeViewProvider = ConfigurationProvider
				.getInstance().getAlternativeViewProvider();
		String[] alternativeViews = alternativeViewProvider
				.getAvailableAlternativeViews(this.url, this.revision);
		if (alternativeViews != null) {
			for (int i = 0; i < alternativeViews.length; i++) {
				ret.put(alternativeViews[i], alternativeViewProvider
						.getAlternativeViewContentUrl(this.url, this.revision,
								line, alternativeViews[i]));
			}
		}

		return ret;
	}

	public String getSelectedView() {
		String ret = this.requestHandler.getView();
		if (ret == null) {
			ret = BaseViewProvider.CONTENT;
		}
		return ret;
	}

	public String getSelectedViewUrl() {
		return (String) this.getViews().get(this.getSelectedView());
	}

	public Navigation getNavigation() {
		return new Navigation(this.url, this.requestHandler.getLocation(),
				this.requestHandler.getCurrentRevision(), true);
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
					Links.FILE_UPDATE, this.requestHandler.getLocation());
			urlGenerator.addParameter(RequestParameters.URL, UrlUtil
					.encode(this.url));
			ret.add(new Button(urlGenerator.getUrl(), Images.UPDATE, "Commit"));
		}

		urlGenerator = UrlGeneratorFactory.getUrlGenerator(Links.FILE_DOWNLOAD,
				this.requestHandler.getLocation());
		urlGenerator.addParameter(RequestParameters.URL, UrlUtil
				.encode(this.url));
		if (this.requestHandler.getCurrentRevision() != -1) {
			urlGenerator.addParameter(RequestParameters.CREV, Long
					.toString(this.requestHandler.getCurrentRevision()));
		}
		urlGenerator.addParameter(RequestParameters.ATTACHMENT,
				RequestParameters.VALUE_TRUE);
		ret.add(new Button(urlGenerator.getUrl(), Images.DOWNLOAD, "Download"));

		return ret;
	}

	public Collection getCharacterEncodings() {
		return ContentEncodingHelper.getCharacterEncodings();
	}

	public boolean isSelectedCharacterEncoding(String encoding) {
		return ContentEncodingHelper.isSelectedCharacterEncoding(this.state,
				encoding);
	}
}
