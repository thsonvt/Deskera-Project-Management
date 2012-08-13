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

import com.krawler.svnwebclient.data.model.DataRepositoryElement;
import com.krawler.svnwebclient.data.model.DataRevision;
import com.krawler.svnwebclient.decorations.IIconDecoration;
import com.krawler.svnwebclient.decorations.IRevisionDecorator;
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
import com.krawler.svnwebclient.web.support.State;

public class RevisionList {
	protected List revisions;
	protected AbstractRequestHandler requestHandler;
	protected long headRevision;
	protected String url;
	protected DataRepositoryElement info;
	protected IRevisionDecorator revisionDecorator;
	protected State state;

	public class Element {
		protected DataRevision revision;

		public Element(DataRevision revision) {
			this.revision = revision;
		}

		public String getDecoratedRevision() {
			String ret = null;
			ret = NumberFormatter.format(this.revision.getRevision());
			return HtmlUtil.encode(ret);
		}

		public String getRevision() {
			return HtmlUtil.encode(Long.toString(this.revision.getRevision()));
		}

		public String getRevisionUrl() {
			UrlGenerator urlGenerator;
			if (RevisionList.this.info.isDirectory()) {
				urlGenerator = UrlGeneratorFactory.getUrlGenerator(
						Links.DIRECTORY_CONTENT,
						RevisionList.this.requestHandler.getLocation());
			} else {
				urlGenerator = UrlGeneratorFactory.getUrlGenerator(
						Links.FILE_CONTENT, RevisionList.this.requestHandler
								.getLocation());
			}

			urlGenerator.addParameter(RequestParameters.URL, UrlUtil
					.encode(RevisionList.this.url));
			if (RevisionList.this.requestHandler.getCurrentRevision() != -1) {
				urlGenerator.addParameter(RequestParameters.PEGREV, Long
						.toString(RevisionList.this.requestHandler
								.getCurrentRevision()));
			} else {
				urlGenerator.addParameter(RequestParameters.PEGREV, Long
						.toString(RevisionList.this.headRevision));
			}
			urlGenerator.addParameter(RequestParameters.CREV, this
					.getRevision());
			return urlGenerator.getUrl();
		}

		public String getDate() {
			return DateFormatter.format(this.revision.getDate());
		}

		public String getAuthor() {
			return HtmlUtil.encode(this.revision.getAuthor());
		}

		public String getComment() {
			return HtmlUtil.encode(this.revision.getComment());
		}

		public String getFirstLine() {
			return CommentUtil.getFirstLine(this.revision.getComment());
		}

		public boolean isMultiLineComment() {
			return CommentUtil.isMultiLine(this.revision.getComment());
		}

		public String getTooltip() {
			return CommentUtil.getTooltip(this.revision.getComment());
		}

		public String getRevisionInfoUrl() {
			UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					Links.REVISION, RevisionList.this.requestHandler
							.getLocation());
			urlGenerator.addParameter(RequestParameters.URL, UrlUtil
					.encode(RevisionList.this.url));
			if (RevisionList.this.requestHandler.getCurrentRevision() != -1) {
				urlGenerator.addParameter(RequestParameters.CREV, Long
						.toString(RevisionList.this.requestHandler
								.getCurrentRevision()));
			}
			urlGenerator
					.addParameter(RequestParameters.REV, this.getRevision());
			return urlGenerator.getUrl();
		}

		public String getDownloadUrl() {
			UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					"fileDownload.jsp", RevisionList.this.requestHandler
							.getLocation());
			urlGenerator.addParameter(RequestParameters.URL, UrlUtil
					.encode(RevisionList.this.url));
			if (RevisionList.this.requestHandler.getCurrentRevision() != -1) {
				urlGenerator.addParameter(RequestParameters.PEGREV, Long
						.toString(RevisionList.this.requestHandler
								.getCurrentRevision()));
			} else {
				urlGenerator.addParameter(RequestParameters.PEGREV, Long
						.toString(RevisionList.this.headRevision));
			}
			urlGenerator.addParameter(RequestParameters.CREV, this
					.getRevision());
			urlGenerator.addParameter(RequestParameters.ATTACHMENT,
					RequestParameters.VALUE_TRUE);
			return urlGenerator.getUrl();
		}

		public boolean isRevisionDecorated() {
			return RevisionList.this.revisionDecorator.isRevisionDecorated(this
					.getRevision(), RevisionList.this.state.getRequest());
		}

		public IIconDecoration getRevisionDecoration() {
			return RevisionList.this.revisionDecorator.getIconDecoration(this
					.getRevision(), RevisionList.this.state.getRequest());
		}

		public boolean isHeadRevision() {
			return RevisionList.this.headRevision == this.revision
					.getRevision();
		}
	}

	public RevisionList(List revisions, AbstractRequestHandler requestHandler,
			long headRevision, String url, DataRepositoryElement info,
			IRevisionDecorator revisionDecorator) {
		this.revisions = revisions;
		this.requestHandler = requestHandler;
		this.headRevision = headRevision;
		this.url = url;
		this.info = info;
		this.revisionDecorator = revisionDecorator;
	}

	public String getRevisionsCount() {
		return Integer.toString(this.revisions.size());
	}

	public String getHeadRevision() {
		return NumberFormatter.format(this.headRevision);
	}

	public String getHeadRevisionUrl() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.REVISION, this.requestHandler.getLocation());
		String url = this.url;
		urlGenerator.addParameter(RequestParameters.URL, UrlUtil.encode(url));
		if (this.requestHandler.getCurrentRevision() != -1) {
			urlGenerator.addParameter(RequestParameters.CREV, Long
					.toString(this.requestHandler.getCurrentRevision()));
		}
		urlGenerator.addParameter(RequestParameters.REV, Long
				.toString(this.headRevision));
		return urlGenerator.getUrl();
	}

	public List getRevisions() {
		List ret = new ArrayList();
		for (Iterator i = this.revisions.iterator(); i.hasNext();) {
			ret.add(new Element((DataRevision) i.next()));
		}
		return ret;
	}

	public boolean isDirectory() {
		return this.info.isDirectory();
	}

	public void setState(State state) {
		this.state = state;
	}
}
