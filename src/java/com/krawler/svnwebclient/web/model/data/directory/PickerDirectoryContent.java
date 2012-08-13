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

import com.krawler.svnwebclient.configuration.ConfigurationProvider;
import com.krawler.svnwebclient.data.model.DataDirectoryElement;
import com.krawler.svnwebclient.data.model.DataRepositoryElement;
import com.krawler.svnwebclient.decorations.IRevisionDecorator;
import com.krawler.svnwebclient.util.UrlGenerator;
import com.krawler.svnwebclient.util.UrlGeneratorFactory;
import com.krawler.svnwebclient.util.UrlUtil;
import com.krawler.svnwebclient.web.resource.Links;
import com.krawler.svnwebclient.web.support.AbstractRequestHandler;
import com.krawler.svnwebclient.web.support.RequestParameters;

public class PickerDirectoryContent extends BaseDirectoryContent {

	public PickerDirectoryContent(DataDirectoryElement directoryElement,
			AbstractRequestHandler requestHandler,
			IRevisionDecorator revisionDecorator) {
		super(directoryElement, requestHandler, revisionDecorator);
	}

	public class Element extends BaseDirectoryContent.BaseElement {

		public Element(DataRepositoryElement repositoryElement) {
			super(repositoryElement);
		}

		public boolean isDirectory() {
			return repositoryElement.isDirectory();
		}

		public String getContentUrl() {
			UrlGenerator urlGenerator = null;
			if (this.repositoryElement.isDirectory()) {
				urlGenerator = UrlGeneratorFactory.getUrlGenerator(
						Links.PICKER_DIRECTORY_CONTENT, requestHandler
								.getLocation());
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
			if (requestHandler.isSingleRevisionMode()) {
				urlGenerator.addParameter(RequestParameters.SINGLE_REVISION);
			}
			if (requestHandler.isMultiUrlSelection()) {
				urlGenerator
						.addParameter(RequestParameters.MULTI_URL_SELECTION);
			}
			return urlGenerator.getUrl();
		}

		public String getPickerFullUrl() {
			StringBuffer pickerUrl = new StringBuffer(ConfigurationProvider
					.getInstance().getRepositoryLocation(
							requestHandler.getRepositoryName()));
			pickerUrl.append("/");
			String shortUrl = url;
			if (!"".equals(shortUrl)) {
				pickerUrl.append(shortUrl).append("/");
			}
			pickerUrl.append(this.repositoryElement.getName());
			return pickerUrl.toString();
		}

		public String getPickerSelectUrlSrcipt() {
			String url = this.getPickerFullUrl();
			url = url.replaceAll("'", "&comma");

			String res = "\"" + url + "\"";
			return res;
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
