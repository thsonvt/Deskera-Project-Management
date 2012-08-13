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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.krawler.svnwebclient.data.model.DataDirectoryCompareItem;
import com.krawler.svnwebclient.util.HtmlUtil;
import com.krawler.svnwebclient.util.UrlGenerator;
import com.krawler.svnwebclient.util.UrlGeneratorFactory;
import com.krawler.svnwebclient.util.UrlUtil;
import com.krawler.svnwebclient.web.resource.Images;
import com.krawler.svnwebclient.web.resource.Links;
import com.krawler.svnwebclient.web.support.AbstractRequestHandler;
import com.krawler.svnwebclient.web.support.RequestParameters;

public class DirectoryCompare {
	protected List items;
	protected AbstractRequestHandler requestHandler;
	protected String url;
	protected long startRevision;
	protected long endRevision;
	protected Comparator comparator;

	public class Element {
		protected DataDirectoryCompareItem data;

		public Element(DataDirectoryCompareItem data) {
			this.data = data;
		}

		public String getImage() {
			if (this.data.isDirectory()) {
				if (DataDirectoryCompareItem.OPERATION_ADD == this.data
						.getOperation()) {
					return Images.DIRECTORY_ADDED;
				} else if (DataDirectoryCompareItem.OPERATION_DELETE == data
						.getOperation()) {
					return Images.DIRECTORY_DELETED;
				} else {
					return Images.DIRECTORY_MODIFIED;
				}
			} else {
				if (DataDirectoryCompareItem.OPERATION_ADD == this.data
						.getOperation()) {
					return Images.FILE_ADDED;
				} else if (DataDirectoryCompareItem.OPERATION_DELETE == data
						.getOperation()) {
					return Images.FILE_DELETED;
				} else {
					return Images.FILE_MODIFIED;
				}
			}
		}

		public String getName() {
			return HtmlUtil.encode(this.data.getPath());
		}

		public String getUrl() {
			if (this.data.isDirectory()) {
				return null;
			} else {
				UrlGenerator urlGenerator = UrlGeneratorFactory
						.getUrlGenerator(Links.FILE_COMPARE,
								DirectoryCompare.this.requestHandler
										.getLocation());
				String url = DirectoryCompare.this.url;
				if (url.length() != 0) {
					url += "/";
				}
				url += this.data.getPath();
				urlGenerator.addParameter(RequestParameters.URL, UrlUtil
						.encode(url));
				if (DirectoryCompare.this.requestHandler.getCurrentRevision() != -1) {
					urlGenerator.addParameter(RequestParameters.CREV, Long
							.toString(DirectoryCompare.this.requestHandler
									.getCurrentRevision()));
				}
				if (DataDirectoryCompareItem.OPERATION_ADD != this.data
						.getOperation()) {
					urlGenerator.addParameter(RequestParameters.STARTREV, Long
							.toString(DirectoryCompare.this.startRevision));
				}
				if (DataDirectoryCompareItem.OPERATION_DELETE != this.data
						.getOperation()) {
					urlGenerator.addParameter(RequestParameters.ENDREV, Long
							.toString(DirectoryCompare.this.endRevision));
				}

				if (DataDirectoryCompareItem.OPERATION_ADD == this.data
						.getOperation()) {
					urlGenerator.addParameter(RequestParameters.PEGREV, Long
							.toString(DirectoryCompare.this.endRevision));
				}
				if (DataDirectoryCompareItem.OPERATION_DELETE == this.data
						.getOperation()) {
					urlGenerator.addParameter(RequestParameters.PEGREV, Long
							.toString(DirectoryCompare.this.startRevision));
				}
				return urlGenerator.getUrl();
			}
		}

		public boolean isDirectory() {
			return this.data.isDirectory();
		}
	}

	public DirectoryCompare(List items, AbstractRequestHandler requestHandler,
			String url, long startRevision, long endRevision) {
		this.items = items;
		this.requestHandler = requestHandler;
		this.url = url;
		this.startRevision = startRevision;
		this.endRevision = endRevision;
	}

	public void applySort(Comparator comparator) {
		this.comparator = comparator;
	}

	public List getElements() {
		List ret = new ArrayList();
		Collections.sort(this.items, this.comparator);
		for (Iterator i = this.items.iterator(); i.hasNext();) {
			ret.add(new Element((DataDirectoryCompareItem) i.next()));
		}
		return ret;
	}
}
