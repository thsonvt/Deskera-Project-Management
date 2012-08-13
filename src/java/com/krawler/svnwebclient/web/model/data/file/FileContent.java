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
package com.krawler.svnwebclient.web.model.data.file;

import com.krawler.svnwebclient.data.model.DataFileElement;
import com.krawler.svnwebclient.util.CommentUtil;
import com.krawler.svnwebclient.util.DateFormatter;
import com.krawler.svnwebclient.util.HtmlUtil;
import com.krawler.svnwebclient.util.NumberFormatter;
import com.krawler.svnwebclient.util.UrlGenerator;
import com.krawler.svnwebclient.util.UrlGeneratorFactory;
import com.krawler.svnwebclient.util.UrlUtil;
import com.krawler.svnwebclient.web.resource.Links;
import com.krawler.svnwebclient.web.support.AbstractRequestHandler;
import com.krawler.svnwebclient.web.support.RequestParameters;

public class FileContent {
	protected DataFileElement fileElement;
	protected AbstractRequestHandler requestHandler;
	protected long headRevision;
	protected String url;

	public FileContent(DataFileElement fileElement,
			AbstractRequestHandler requestHandler, long headRevision, String url) {
		this.fileElement = fileElement;
		this.requestHandler = requestHandler;
		this.headRevision = headRevision;
		this.url = url;
	}

	public String getAuthor() {
		return HtmlUtil.encode(this.fileElement.getAuthor());
	}

	public String getFirstLine() {
		return CommentUtil.getFirstLine(this.fileElement.getComment());
	}

	public boolean isMultiLineComment() {
		return CommentUtil.isMultiLine(this.fileElement.getComment());
	}

	public String getComment() {
		return HtmlUtil.encode(this.fileElement.getComment());
	}

	public String getTooltip() {
		return CommentUtil.getTooltip(this.fileElement.getComment());
	}

	public String getDate() {
		return DateFormatter.format(this.fileElement.getDate());
	}

	public String getRevision() {
		return Long.toString(this.fileElement.getRevision());
	}

	public String getRevisionUrl() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.REVISION, this.requestHandler.getLocation());
		String url = this.url;
		urlGenerator.addParameter(RequestParameters.URL, UrlUtil.encode(url));
		if (this.requestHandler.getCurrentRevision() != -1) {
			urlGenerator.addParameter(RequestParameters.CREV, Long
					.toString(this.requestHandler.getCurrentRevision()));
		}
		urlGenerator.addParameter(RequestParameters.REV, this.getRevision());
		return urlGenerator.getUrl();
	}

	public String getDecoratedRevision() {
		return NumberFormatter.format(this.fileElement.getRevision());
	}

	public boolean isHeadRevision() {
		return this.headRevision == this.fileElement.getRevision();
	}

	public String getSize() {
		return NumberFormatter.format(this.fileElement.getSize());
	}
}
