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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.krawler.svnwebclient.data.model.DataRevision;
import com.krawler.svnwebclient.decorations.IRevisionDecorator;
import com.krawler.svnwebclient.util.CommentUtil;
import com.krawler.svnwebclient.util.DateFormatter;
import com.krawler.svnwebclient.util.HtmlUtil;
import com.krawler.svnwebclient.util.NumberFormatter;
import com.krawler.svnwebclient.util.UrlGenerator;
import com.krawler.svnwebclient.util.UrlGeneratorFactory;
import com.krawler.svnwebclient.util.UrlUtil;
import com.krawler.svnwebclient.web.resource.Actions;
import com.krawler.svnwebclient.web.resource.Images;
import com.krawler.svnwebclient.web.resource.Links;
import com.krawler.svnwebclient.web.support.AbstractRequestHandler;
import com.krawler.svnwebclient.web.support.RequestParameters;
import com.krawler.svnwebclient.web.support.State;

public class RevisionDetails {
	protected DataRevision revision;
	protected long headRevision;
	protected IRevisionDecorator revisionDecorator;
	protected State state;
	protected Comparator comparator;
	protected AbstractRequestHandler requestHandler;

	public class Element {
		protected DataRevision.ChangedElement element;

		public Element(DataRevision.ChangedElement element) {
			this.element = element;
		}

		public String getName() {
			return HtmlUtil.encode(this.element.getPath());
		}

		public String getCopyPath() {
			String path = this.element.getCopyPath();
			if (path != null) {
				return HtmlUtil.encode(path).substring(1);
			} else {
				return "";
			}
		}

		public String getCopyUrl() {
			StringBuffer result = new StringBuffer(Links.REVISION);
			result.append("?url=").append(this.getCopyPath());
			result.append("&rev=").append(this.getCopyRevision());
			return result.toString();
		}

		public String getCopyRevision() {
			long revision = element.getCopyRevision();
			return (revision == -1 ? "" : Long.toString(revision));
		}

		public String getImage() {
			if (this.element.getType() == DataRevision.TYPE_ADDED) {
				return Images.RESOURCE_ADDED;
			} else if (this.element.getType() == DataRevision.TYPE_DELETED) {
				return Images.RESOURCE_DELETED;
			} else if (this.element.getType() == DataRevision.TYPE_MODIFIED) {
				return Images.RESOURCE_MODIFIED;
			} else {
				return Images.RESOURCE_ADDED;
			}
		}

		public String getUrl() {
			UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					Links.CHANGED_RESOURCE, RevisionDetails.this.requestHandler
							.getLocation());
			urlGenerator.addParameter(RequestParameters.URL, UrlUtil
					.encode(this.element.getPath()));
			if (RevisionDetails.this.requestHandler.getCurrentRevision() != -1) {
				urlGenerator.addParameter(RequestParameters.CREV, Long
						.toString(RevisionDetails.this.requestHandler
								.getCurrentRevision()));
			}
			urlGenerator.addParameter(RequestParameters.REV, Long
					.toString(RevisionDetails.this.revision.getRevision()));
			if (this.element.getType() == DataRevision.TYPE_ADDED) {
				urlGenerator
						.addParameter(RequestParameters.ACTION, Actions.ADD);
			} else if (this.element.getType() == DataRevision.TYPE_DELETED) {
				urlGenerator.addParameter(RequestParameters.ACTION,
						Actions.DELETE);
			} else if (this.element.getType() == DataRevision.TYPE_MODIFIED) {
				urlGenerator.addParameter(RequestParameters.ACTION,
						Actions.MODIFY);
			} else {
				urlGenerator.addParameter(RequestParameters.ACTION,
						Actions.REPLACE);
			}
			return urlGenerator.getUrl();
		}
	}

	public RevisionDetails(DataRevision revision, long headRevision,
			IRevisionDecorator revisionDecorator, State state,
			AbstractRequestHandler requestHandler) {
		this.revision = revision;
		this.headRevision = headRevision;
		this.revisionDecorator = revisionDecorator;
		this.state = state;
		this.requestHandler = requestHandler;
	}

	public void applySort(Comparator comparator) {
		this.comparator = comparator;
	}

	public String getDecoratedRevision() {
		return NumberFormatter.format(this.revision.getRevision());
	}

	public String getRevisionUrl() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.REVISION, this.requestHandler.getLocation());
		String url = this.requestHandler.getUrl();// TODO
		urlGenerator.addParameter(RequestParameters.URL, UrlUtil.encode(url));
		if (this.requestHandler.getCurrentRevision() != -1) {
			urlGenerator.addParameter(RequestParameters.CREV, Long
					.toString(this.requestHandler.getCurrentRevision()));
		}
		urlGenerator.addParameter(RequestParameters.REV, this.getRevision());
		return urlGenerator.getUrl();
	}

	public String getRevision() {
		return Long.toString(this.revision.getRevision());
	}

	public String getDate() {
		return DateFormatter.format(this.revision.getDate());
	}

	public String getAuthor() {
		return HtmlUtil.encode(this.revision.getAuthor());
	}

	public String getFirstLine() {
		return CommentUtil.getFirstLine(this.revision.getComment());
	}

	public boolean isMultiLineComment() {
		return CommentUtil.isMultiLine(this.revision.getComment());
	}

	public String getComment() {
		return HtmlUtil.encode(this.revision.getComment());
	}

	public String getTooltip() {
		return CommentUtil.getTooltip(this.revision.getComment());
	}

	public String getChangedElementsCount() {
		return Integer.toString(this.revision.getChangedElements().size());
	}

	public boolean isHeadRevision() {
		return this.headRevision == this.revision.getRevision();
	}

	public boolean isRevisionDecorated() {
		return this.revisionDecorator.isRevisionDecorated(this.getRevision(),
				this.state.getRequest());
	}

	public String getDecorationTitle() {
		return this.revisionDecorator.getSectionTitle();
	}

	public String getDecorationContent() {
		return this.revisionDecorator.getSectionContent(this.getRevision(),
				this.state.getRequest());
	}

	public List getChangedElements() {
		List ret = new ArrayList();
		List changedElements = this.revision.getChangedElements();
		Collections.sort(changedElements, this.comparator);
		for (Iterator i = changedElements.iterator(); i.hasNext();) {
			ret.add(new Element((DataRevision.ChangedElement) i.next()));
		}
		return ret;
	}
}
