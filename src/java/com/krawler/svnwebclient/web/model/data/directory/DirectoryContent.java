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
package com.krawler.svnwebclient.web.model.data.directory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.krawler.svnwebclient.data.model.DataDirectoryElement;
import com.krawler.svnwebclient.data.model.DataRepositoryElement;
import com.krawler.svnwebclient.decorations.IRevisionDecorator;
import com.krawler.svnwebclient.util.UrlGenerator;
import com.krawler.svnwebclient.util.UrlGeneratorFactory;
import com.krawler.svnwebclient.util.UrlUtil;
import com.krawler.svnwebclient.web.resource.Links;
import com.krawler.svnwebclient.web.support.AbstractRequestHandler;
import com.krawler.svnwebclient.web.support.RequestParameters;

public class DirectoryContent extends BaseDirectoryContent {

	public DirectoryContent(DataDirectoryElement directoryElement,
			AbstractRequestHandler requestHandler,
			IRevisionDecorator revisionDecorator) {
		super(directoryElement, requestHandler, revisionDecorator);
	}

	public class Element extends BaseDirectoryContent.BaseElement {

		public Element(DataRepositoryElement repositoryElement) {
			super(repositoryElement);
		}

		public String getRevisionUrl() {
			UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					Links.REVISION, requestHandler.getLocation());
			String strUrl = url;
			if (strUrl.length() != 0) {
				strUrl += "/";
			}
			strUrl += this.repositoryElement.getName();

			urlGenerator.addParameter(RequestParameters.URL, UrlUtil
					.encode(strUrl));
			if (requestHandler.getCurrentRevision() != -1) {
				urlGenerator.addParameter(RequestParameters.CREV, Long
						.toString(requestHandler.getCurrentRevision()));
			}
			urlGenerator
					.addParameter(RequestParameters.REV, this.getRevision());
			return urlGenerator.getUrl();
		}

		public String getRevisionListUrl() {
			UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					Links.REVISION_LIST, requestHandler.getLocation());

			String strUrl = url;
			if (strUrl.length() != 0) {
				strUrl += "/";
			}
			strUrl += this.repositoryElement.getName();

			urlGenerator.addParameter(RequestParameters.URL, UrlUtil
					.encode(strUrl));
			long startRevision = -1;
			if (requestHandler.getCurrentRevision() != -1) {
				startRevision = requestHandler.getCurrentRevision();
				urlGenerator.addParameter(RequestParameters.CREV, Long
						.toString(requestHandler.getCurrentRevision()));
			} else {
				startRevision = headRevision;
			}
			urlGenerator.addParameter(RequestParameters.START_REVISION, Long
					.toString(startRevision));
			return urlGenerator.getUrl();
		}

		public String getContentUrl() {
			UrlGenerator urlGenerator;
			if (this.repositoryElement.isDirectory()) {
				urlGenerator = UrlGeneratorFactory.getUrlGenerator(
						Links.DIRECTORY_CONTENT, requestHandler.getLocation());
			} else {
				urlGenerator = UrlGeneratorFactory.getUrlGenerator(
						Links.FILE_CONTENT, requestHandler.getLocation());
			}

			String strUrl = url;
			if (strUrl.length() != 0) {
				strUrl += "/";
			}
			strUrl += this.repositoryElement.getName();

			urlGenerator.addParameter(RequestParameters.URL, UrlUtil
					.encode(strUrl));
			if (requestHandler.getCurrentRevision() != -1) {
				urlGenerator.addParameter(RequestParameters.CREV, Long
						.toString(requestHandler.getCurrentRevision()));
			}
			return urlGenerator.getUrl();
		}
	}

	public List getChilds() {
		List ret = new ArrayList();
		List childElements = this.directoryElement.getChildElements();
		Collections.sort(childElements, this.comparator);
		for (Iterator i = childElements.iterator(); i.hasNext();) {
			ret.add(new Element((DataRepositoryElement) i.next()));
		}
		return ret;
	}
}
