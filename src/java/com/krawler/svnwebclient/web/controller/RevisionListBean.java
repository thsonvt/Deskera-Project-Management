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
package com.krawler.svnwebclient.web.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.krawler.svnwebclient.SVNWebClientException;
import com.krawler.svnwebclient.configuration.ConfigurationProvider;
import com.krawler.svnwebclient.data.DataProviderException;
import com.krawler.svnwebclient.data.IDataProvider;
import com.krawler.svnwebclient.data.IncorrectParameterException;
import com.krawler.svnwebclient.data.model.DataRepositoryElement;
import com.krawler.svnwebclient.data.model.DataRevision;
import com.krawler.svnwebclient.util.UrlGenerator;
import com.krawler.svnwebclient.util.UrlGeneratorFactory;
import com.krawler.svnwebclient.util.UrlUtil;
import com.krawler.svnwebclient.web.model.Button;
import com.krawler.svnwebclient.web.model.Navigation;
import com.krawler.svnwebclient.web.model.data.RevisionList;
import com.krawler.svnwebclient.web.resource.Images;
import com.krawler.svnwebclient.web.resource.Links;
import com.krawler.svnwebclient.web.support.AbstractRequestHandler;
import com.krawler.svnwebclient.web.support.RequestHandler;
import com.krawler.svnwebclient.web.support.RequestParameters;

public class RevisionListBean extends AbstractBean {
	public static final String ADVANCED_NAVIGATION_ATTRIBUTE = "advancedNavigation";
	protected static final int SHOW_OTHER_MODE = 0;
	protected static final int SHOW_NEXT_MODE = 1;
	protected static final int SHOW_PREVIOUS_MODE = 2;

	protected long currentRevision;
	protected long headRevision;
	protected String url;
	protected List revisions = new ArrayList();
	protected DataRepositoryElement info;

	protected boolean isNextDisabled;
	protected boolean isPreviousDisabled;

	protected long startRevision;
	protected long endRevision;
	protected long revisionCount;
	protected boolean isReverseOrder;

	protected ButtonUrls buttonUrls = new ButtonUrls();

	protected int show_mode;
	protected long readRevisionCount;
	protected boolean isHidePolarionCommit;

	public class ButtonUrls {
		protected String next;
		protected String previous;
		protected long start;
		protected long end;

		protected long getNextStartRevision() {
			return this.start;
		}

		protected long getPreviousEndRevision() {
			return this.end;
		}

		protected UrlGenerator getUrlGenerator() {
			UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					Links.REVISION_LIST, requestHandler.getLocation());
			urlGenerator.addParameter(RequestParameters.URL, url);
			if (currentRevision != headRevision) {
				urlGenerator.addParameter(RequestParameters.CREV, Long
						.toString(currentRevision));
			}
			if (state.getRequest().getParameter(
					RequestParameters.HIDE_POLARION_COMMIT) != null) {
				urlGenerator
						.addParameter(RequestParameters.HIDE_POLARION_COMMIT);
			}
			return urlGenerator;
		}

		protected void setNext(long revision) {
			this.start = revision;
			UrlGenerator urlGenerator = this.getUrlGenerator();
			urlGenerator.addParameter(RequestParameters.START_REVISION, Long
					.toString(revision));
			this.next = urlGenerator.getUrl();
		}

		protected void setPrevious(long revision) {
			this.end = revision;
			UrlGenerator urlGenerator = this.getUrlGenerator();
			urlGenerator.addParameter(RequestParameters.END_REVISION, Long
					.toString(revision));
			this.previous = urlGenerator.getUrl();
		}

		public String getNext() {
			return this.next;
		}

		public String getPrevious() {
			return this.previous;
		}

		public String getUrl() {
			return this.getUrlGenerator().getUrl();
		}

		public String getRange() {
			UrlGenerator urlGenerator = this.getUrlGenerator();
			urlGenerator.addParameter(RequestParameters.START_REVISION, Long
					.toString(this.start));
			urlGenerator.addParameter(RequestParameters.END_REVISION, Long
					.toString(this.end));
			return urlGenerator.getUrl();
		}

		public String getHidePolarionCommitUrl() {
			UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					Links.REVISION_LIST, requestHandler.getLocation());
			Enumeration names = state.getRequest().getParameterNames();
			while (names.hasMoreElements()) {
				String paramName = (String) names.nextElement();
				if (!RequestParameters.HIDE_POLARION_COMMIT.equals(paramName)
						&& isAllowedHidePolarionCommit()) {
					urlGenerator.addParameter(paramName, state.getRequest()
							.getParameter(paramName));
				}
			}
			if (state.getRequest().getParameter(
					RequestParameters.HIDE_POLARION_COMMIT) == null) {
				urlGenerator
						.addParameter(RequestParameters.HIDE_POLARION_COMMIT);
			}
			return urlGenerator.getUrl();
		}
	}

	public RevisionListBean() {
		startRevision = -2;
		endRevision = -2;
		revisionCount = 0;
		isReverseOrder = false;
	}

	protected boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		this.headRevision = dataProvider.getHeadRevision();
		this.currentRevision = this.headRevision;
		if (this.requestHandler.getCurrentRevision() != -1) {
			this.currentRevision = this.requestHandler.getCurrentRevision();
		}
		this.url = this.requestHandler.getUrl();
		if (this.requestHandler.getPegRevision() != -1) {
			this.url = dataProvider.getLocation(this.requestHandler.getUrl(),
					this.requestHandler.getPegRevision(), this.currentRevision);
		}

		this.setRevisionsStatus();
		this.info = dataProvider.getInfo(this.url, this.currentRevision);
		if (revisionCount == 0) {
			revisionCount = ConfigurationProvider.getInstance()
					.getVersionsCount();
		}

		this.readRevisions(dataProvider, this.startRevision, this.endRevision);

		if (this.isReverseOrder) {
			Collections.reverse(this.revisions);
		}
		this.setNextDisabled(dataProvider);
		this.setPreviousDisabled(dataProvider);
		return true;
	}

	protected void readRevisions(IDataProvider dataProvider, long start,
			long end) throws SVNWebClientException {
		List pageRevisions = dataProvider.getRevisions(this.url, start, end,
				this.revisionCount);
		if (this.isAllowedHidePolarionCommit()
				&& this.isHidePolarionCommit == true) {
			long lastReadRevision = this.filterComments(pageRevisions);
			if (this.show_mode == RevisionListBean.SHOW_NEXT_MODE
					|| this.show_mode == RevisionListBean.SHOW_PREVIOUS_MODE) {
				if (this.revisions.size() == this.revisionCount) {
					return;
				} else {
					if (this.show_mode == RevisionListBean.SHOW_NEXT_MODE) {
						this.setNextDisabled(dataProvider, lastReadRevision);
						if (this.isNextDisabled) {
							return;
						} else {
							this.readRevisions(dataProvider, this.buttonUrls
									.getNextStartRevision(), (long) 0);
						}
					} else {
						this
								.setPreviousDisabled(dataProvider,
										lastReadRevision);
						if (this.isPreviousDisabled) {
							return;
						} else {
							this.readRevisions(dataProvider, this.buttonUrls
									.getPreviousEndRevision(),
									this.currentRevision);
						}
					}
				}
			}
		} else {
			this.revisions = pageRevisions;
		}
	}

	protected long filterComments(List pageRevisions) {
		Iterator it = pageRevisions.iterator();
		long lastRevision = 0;
		while (it.hasNext()) {
			if (this.revisionCount == 0 || this.revisionCount == -1
					|| this.revisions.size() < this.revisionCount) {
				DataRevision rev = (DataRevision) it.next();
				lastRevision = rev.getRevision();
				String comment = rev.getComment();
				if (this.isPolarionComment(comment)) {
					it.remove();
				} else {
					this.revisions.add(rev);
				}
			} else {
				break;
			}
		}
		return lastRevision;
	}

	protected boolean isPolarionComment(String comment) {
		Pattern pattern = Pattern.compile("Polarion commit");
		Matcher matcher = pattern.matcher(comment);
		return matcher.find();
	}

	protected void setRevisionsStatus() {
		this.isHidePolarionCommit = state.getRequest().getParameter(
				RequestParameters.HIDE_POLARION_COMMIT) == null ? false : true;
		if (this.state.getRequest().getParameter(
				RequestParameters.START_REVISION) != null) {
			this.startRevision = Long.parseLong(this.state.getRequest()
					.getParameter(RequestParameters.START_REVISION));
		}
		if (this.state.getRequest()
				.getParameter(RequestParameters.END_REVISION) != null) {
			this.endRevision = Long.parseLong(this.state.getRequest()
					.getParameter(RequestParameters.END_REVISION));
		}
		this.revisionCount = this.requestHandler.getRevisionCount();

		if (this.startRevision != -2 && this.endRevision == -2) {
			// next
			this.endRevision = 0;
			this.show_mode = RevisionListBean.SHOW_NEXT_MODE;
		} else if (this.startRevision != -2 && this.endRevision != -2) {
			// range
			revisionCount = -1;
			if (startRevision < endRevision) {
				isReverseOrder = true;
			}
			this.show_mode = RevisionListBean.SHOW_OTHER_MODE;
		} else if (this.startRevision == -2 && this.endRevision != -2) {
			// previous
			this.startRevision = this.endRevision;
			this.endRevision = this.currentRevision;
			this.isReverseOrder = true;
			this.show_mode = RevisionListBean.SHOW_PREVIOUS_MODE;
		} else {
			startRevision = this.currentRevision;
			endRevision = 0;
			revisionCount = -1;
			this.show_mode = RevisionListBean.SHOW_OTHER_MODE;
		}
	}

	protected long getSibblingRevision(long startRevision, long endREvision,
			IDataProvider dataProvider) throws SVNWebClientException {
		try {
			long count = 2;
			List revList = dataProvider.getRevisions(this.url, startRevision,
					endREvision, count);
			if (revList.size() == 1) {
				return ((DataRevision) revList.get(0)).getRevision();
			} else {
				return ((DataRevision) revList.get(1)).getRevision();
			}
		} catch (IncorrectParameterException ie) {
			// it was copy operation
			return startRevision;
		} catch (DataProviderException de) {
			throw new SVNWebClientException(de);
		}
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request);
	}

	public RevisionList getRevisionList() {
		RevisionList revisionList = new RevisionList(this.revisions,
				this.requestHandler, this.currentRevision, this.url, this.info,
				ConfigurationProvider.getInstance().getRevisionDecorator());
		revisionList.setState(this.state);
		return revisionList;
	}

	public Navigation getNavigation() {
		return new Navigation(this.url, this.requestHandler.getLocation(),
				this.requestHandler.getCurrentRevision(), !this.info
						.isDirectory());
	}

	public List getActions() {
		List ret = new ArrayList();
		UrlGenerator urlGenerator;
		if (this.info.isDirectory()) {
			urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					Links.DIRECTORY_COMPARE, this.requestHandler.getLocation());
		} else {
			urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					Links.FILE_COMPARE, this.requestHandler.getLocation());
		}
		urlGenerator.addParameter(RequestParameters.URL, UrlUtil
				.encode(this.url));
		if (this.requestHandler.getCurrentRevision() != -1) {
			urlGenerator.addParameter(RequestParameters.CREV, Long
					.toString(this.requestHandler.getCurrentRevision()));
		}
		ret.add(new Button("javascript:compareRevisions('"
				+ urlGenerator.getUrl() + "')", Images.COMPARE, "Compare"));
		return ret;
	}

	public String getEndRevision() {
		return Long.toString(((DataRevision) this.revisions.get(this.revisions
				.size() - 1)).getRevision());
	}

	public String getStartRevision() {
		return Long.toString(((DataRevision) this.revisions.get(0))
				.getRevision());
	}

	protected void setNextDisabled(IDataProvider dataProvider)
			throws SVNWebClientException {
		this.setNextDisabled(dataProvider, -2);
	}

	protected void setPreviousDisabled(IDataProvider dataProvider)
			throws SVNWebClientException {
		this.setPreviousDisabled(dataProvider, -2);
	}

	protected void setNextDisabled(IDataProvider dataProvider, long start)
			throws SVNWebClientException {
		long nextRevision = 0;
		if (start == -2) {
			start = Long.parseLong(this.getEndRevision());
		}
		if (start == 0) {
			this.isNextDisabled = true;
		} else {
			nextRevision = this.getSibblingRevision(start, (long) 0,
					dataProvider);
			if (nextRevision == start) {
				this.isNextDisabled = true;
			} else {
				this.isNextDisabled = false;
			}
		}
		this.buttonUrls.setNext(nextRevision);
	}

	protected void setPreviousDisabled(IDataProvider dataProvider, long start)
			throws SVNWebClientException {
		long previousRevision = 0;
		if (start == -2) {
			start = Long.parseLong(this.getStartRevision());
		}
		if (start == this.currentRevision) {
			this.isPreviousDisabled = true;
		} else {
			previousRevision = this.getSibblingRevision(start,
					this.currentRevision, dataProvider);
			if (previousRevision == start) {
				this.isPreviousDisabled = true;
			} else {
				this.isPreviousDisabled = false;
			}
		}
		this.buttonUrls.setPrevious(previousRevision);
	}

	public boolean isNextDisabled() {
		return this.isNextDisabled;
	}

	public boolean isPreviousDisabled() {
		return this.isPreviousDisabled;
	}

	public String getUrl() {
		return this.url;
	}

	public String getRangeStartRevision() {
		return (this.state.getRequest().getParameter(
				RequestParameters.START_REVISION) == null ? "" : this.state
				.getRequest().getParameter(RequestParameters.START_REVISION));
	}

	public String getRangeEndRevision() {
		return (this.state.getRequest().getParameter(
				RequestParameters.END_REVISION) == null ? "" : this.state
				.getRequest().getParameter(RequestParameters.END_REVISION));
	}

	public ButtonUrls getButtonUrl() {
		return this.buttonUrls;
	}

	public String getSelectUrl() {
		return Links.CHANGE_REVISION_MODE + "?"
				+ this.state.getRequest().getQueryString();
	}

	public boolean isHidePolarionCommit() {
		return this.isHidePolarionCommit;
	}

	public boolean isAllowedHidePolarionCommit() {
		return (ConfigurationProvider.getInstance().isEmbedded() && ConfigurationProvider
				.getInstance().isHidePolarionCommit());
	}
}
