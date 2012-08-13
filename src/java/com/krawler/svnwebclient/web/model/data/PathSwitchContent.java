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
package com.krawler.svnwebclient.web.model.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.krawler.svnwebclient.data.model.DataDirectoryElement;
import com.krawler.svnwebclient.data.model.DataRepositoryElement;
import com.krawler.svnwebclient.util.CommentUtil;
import com.krawler.svnwebclient.util.DateFormatter;
import com.krawler.svnwebclient.util.HtmlUtil;
import com.krawler.svnwebclient.util.NumberFormatter;
import com.krawler.svnwebclient.util.UrlGenerator;
import com.krawler.svnwebclient.util.UrlGeneratorFactory;
import com.krawler.svnwebclient.util.UrlUtil;
import com.krawler.svnwebclient.web.model.LinkProviderFactory;
import com.krawler.svnwebclient.web.resource.Images;
import com.krawler.svnwebclient.web.resource.Links;
import com.krawler.svnwebclient.web.support.AbstractRequestHandler;
import com.krawler.svnwebclient.web.support.RequestParameters;

public class PathSwitchContent {
	protected DataDirectoryElement directoryElement;
	protected String prefix;
	protected String suffix;
	protected AbstractRequestHandler requestHandler;
	protected boolean isPickerMode;

	public class Element {
		protected DataRepositoryElement repositoryElement;

		public Element(DataRepositoryElement repositoryElement) {
			this.repositoryElement = repositoryElement;
		}

		public String getImage() {
			return Images.DIRECTORY;
		}

		public String getName() {
			return HtmlUtil.encode(this.repositoryElement.getName());
		}

		public String getContentUrl() {
			UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					Links.CONTENT, PathSwitchContent.this.requestHandler
							.getLocation());

			String url = PathSwitchContent.this.prefix;
			url += "/";
			url += this.repositoryElement.getName();
			url += "/";
			url += PathSwitchContent.this.suffix;

			urlGenerator.addParameter(RequestParameters.URL, UrlUtil
					.encode(url));
			if (PathSwitchContent.this.requestHandler.getCurrentRevision() != -1) {
				urlGenerator.addParameter(RequestParameters.CREV, Long
						.toString(PathSwitchContent.this.requestHandler
								.getCurrentRevision()));
			}
			if (isPickerMode) {
				urlGenerator.addParameter(RequestParameters.CONTENT_MODE_TYPE,
						LinkProviderFactory.PICKER_CONTENT_MODE_VALUE);
				if (requestHandler.isMultiUrlSelection()) {
					urlGenerator
							.addParameter(RequestParameters.MULTI_URL_SELECTION);
				}
				if (requestHandler.isSingleRevisionMode()) {
					urlGenerator
							.addParameter(RequestParameters.SINGLE_REVISION);
				}
			}
			return urlGenerator.getUrl();
		}

		public String getDecoratedRevision() {
			String ret = null;
			ret = NumberFormatter.format(this.repositoryElement.getRevision());
			return HtmlUtil.encode(ret);
		}

		public String getRevision() {
			return HtmlUtil.encode(Long.toString(this.repositoryElement
					.getRevision()));
		}

		public String getDate() {
			return DateFormatter.format(this.repositoryElement.getDate());
		}

		public String getAuthor() {
			return HtmlUtil.encode(this.repositoryElement.getAuthor());
		}

		public String getFirstLine() {
			return CommentUtil
					.getFirstLine(this.repositoryElement.getComment());
		}

		public boolean isMultiLineComment() {
			return CommentUtil.isMultiLine(this.repositoryElement.getComment());
		}

		public String getComment() {
			return HtmlUtil.encode(this.repositoryElement.getComment());
		}

		public String getTooltip() {
			return CommentUtil.getTooltip(this.repositoryElement.getComment());
		}
	}

	public PathSwitchContent(DataDirectoryElement directoryElement,
			AbstractRequestHandler requestHandler, String prefix, String suffix) {
		this.directoryElement = directoryElement;
		this.requestHandler = requestHandler;
		this.prefix = prefix;
		this.suffix = suffix;
		this.isPickerMode = LinkProviderFactory.getLinkProvider(
				this.requestHandler.getContentMode()).isPickerMode();
	}

	public List getChilds() {
		List ret = new ArrayList();
		List childElements = this.directoryElement.getChildElements();
		for (Iterator i = childElements.iterator(); i.hasNext();) {
			DataRepositoryElement element = (DataRepositoryElement) i.next();
			if (element.isDirectory()) {
				ret.add(new Element(element));
			}
		}
		return ret;
	}
}
