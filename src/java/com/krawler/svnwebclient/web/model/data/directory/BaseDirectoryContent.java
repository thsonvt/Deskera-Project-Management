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

import java.util.Comparator;

import com.krawler.svnwebclient.data.model.DataDirectoryElement;
import com.krawler.svnwebclient.data.model.DataRepositoryElement;
import com.krawler.svnwebclient.decorations.IIconDecoration;
import com.krawler.svnwebclient.decorations.IRevisionDecorator;
import com.krawler.svnwebclient.util.CommentUtil;
import com.krawler.svnwebclient.util.DateFormatter;
import com.krawler.svnwebclient.util.HtmlUtil;
import com.krawler.svnwebclient.util.NumberFormatter;
import com.krawler.svnwebclient.util.UrlGenerator;
import com.krawler.svnwebclient.util.UrlGeneratorFactory;
import com.krawler.svnwebclient.util.UrlUtil;
import com.krawler.svnwebclient.web.resource.Images;
import com.krawler.svnwebclient.web.resource.Links;
import com.krawler.svnwebclient.web.support.AbstractRequestHandler;
import com.krawler.svnwebclient.web.support.RequestParameters;
import com.krawler.svnwebclient.web.support.State;

public class BaseDirectoryContent {
	protected DataDirectoryElement directoryElement;
	protected AbstractRequestHandler requestHandler;
	protected long headRevision;
	protected String url;
	protected IRevisionDecorator revisionDecorator;
	protected State state;
	protected Comparator comparator;

	public class BaseElement {
		protected DataRepositoryElement repositoryElement;

		public BaseElement(DataRepositoryElement repositoryElement) {
			this.repositoryElement = repositoryElement;
		}

		public String getImage() {
			if (this.repositoryElement.isDirectory()) {
				return Images.DIRECTORY;
			} else {
				return Images.FILE;
			}
		}

		public String getName() {
			return HtmlUtil.encode(this.repositoryElement.getName());
		}

		public String getDecoratedRevision() {
			String ret = null;
			ret = NumberFormatter.format(this.repositoryElement.getRevision());
			return ret;
		}

		public String getRevision() {
			return HtmlUtil.encode(Long.toString(this.repositoryElement
					.getRevision()));
		}

		public boolean isHeadRevision() {
			return BaseDirectoryContent.this.headRevision == this.repositoryElement
					.getRevision();
		}

		public boolean isRevisionDecorated() {
			return BaseDirectoryContent.this.revisionDecorator
					.isRevisionDecorated(this.getRevision(),
							BaseDirectoryContent.this.state.getRequest());
		}

		public IIconDecoration getRevisionDecoration() {
			return BaseDirectoryContent.this.revisionDecorator
					.getIconDecoration(this.getRevision(),
							BaseDirectoryContent.this.state.getRequest());
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

		public String getSize() {
			String ret;
			if (this.repositoryElement.isDirectory()) {
				ret = "<DIR>";
			} else {
				ret = NumberFormatter.format(this.repositoryElement.getSize());
			}
			return HtmlUtil.encode(ret);
		}

		public boolean isRestricted() {
			return this.repositoryElement.isRestricted();
		}
	}

	public BaseDirectoryContent(DataDirectoryElement directoryElement,
			AbstractRequestHandler requestHandler,
			IRevisionDecorator revisionDecorator) {
		this.directoryElement = directoryElement;
		this.requestHandler = requestHandler;
		this.revisionDecorator = revisionDecorator;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setState(State state) {
		this.state = state;
	}

	public void setHeadRevision(long headRevision) {
		this.headRevision = headRevision;
	}

	public void applySort(Comparator comparator) {
		this.comparator = comparator;
	}

	public String getAuthor() {
		return HtmlUtil.encode(this.directoryElement.getAuthor());
	}

	public String getComment() {
		return HtmlUtil.encode(this.directoryElement.getComment());
	}

	public String getTooltip() {
		return CommentUtil.getTooltip(this.directoryElement.getComment());
	}

	public String getFirstLine() {
		return CommentUtil.getFirstLine(this.directoryElement.getComment());
	}

	public boolean isMultiLineComment() {
		return CommentUtil.isMultiLine(this.directoryElement.getComment());
	}

	public String getDate() {
		return DateFormatter.format(this.directoryElement.getDate());
	}

	public String getRevision() {
		return Long.toString(this.directoryElement.getRevision());
	}

	public String getDecoratedRevision() {
		return NumberFormatter.format(this.directoryElement.getRevision());
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

	public boolean isHeadRevision() {
		return this.headRevision == this.directoryElement.getRevision();
	}

	public String getChildCount() {
		return Integer
				.toString(this.directoryElement.getChildElements().size());
	}
}
