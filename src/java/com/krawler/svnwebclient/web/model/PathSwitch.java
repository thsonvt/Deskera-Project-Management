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
package com.krawler.svnwebclient.web.model;

import java.util.ArrayList;
import java.util.List;

import com.krawler.svnwebclient.util.UrlGenerator;
import com.krawler.svnwebclient.util.UrlGeneratorFactory;
import com.krawler.svnwebclient.util.UrlUtil;
import com.krawler.svnwebclient.web.resource.Links;
import com.krawler.svnwebclient.web.support.RequestParameters;

public class PathSwitch {
	public static final String TRUNK = "Trunk";
	public static final String BRANCHES = "Branches";
	public static final String TAGS = "Tags";

	protected String urlPrefix;
	protected String urlSuffix;
	protected String selected;
	protected long currentRevision;
	protected String url;
	protected String location;
	protected boolean isPickerMode;
	protected boolean isSingleRevision;
	protected boolean isMultiSelectionUrl;

	public static class Element {
		protected String name;
		protected String url;
		protected boolean selected;

		public Element(String name, String url, boolean selected) {
			this.name = name;
			this.url = url;
			this.selected = selected;
		}

		public String getName() {
			return this.name;
		}

		public String getUrl() {
			return this.url;
		}

		public boolean isSelected() {
			return this.selected;
		}
	}

	public PathSwitch(String location, String urlPrefix, String urlSuffix,
			String selected, String url, long currentRevision) {
		this(location, urlPrefix, urlSuffix, selected, url, currentRevision,
				false, false, false);
	}

	public PathSwitch(String location, String urlPrefix, String urlSuffix,
			String selected, String url, long currentRevision,
			boolean isPickerMode, boolean isSingleRevision,
			boolean isMultipleSelectionUrl) {
		this.location = location;
		this.urlPrefix = urlPrefix;
		this.urlSuffix = urlSuffix;
		this.selected = selected;
		this.currentRevision = currentRevision;
		this.url = url;
		this.isPickerMode = isPickerMode;
		this.isSingleRevision = isSingleRevision;
		this.isMultiSelectionUrl = isMultipleSelectionUrl;

	}

	public List getElements() {
		List ret = new ArrayList();
		ret.add(new Element(PathSwitch.TRUNK, this
				.generateUrl(PathSwitch.TRUNK), PathSwitch.TRUNK
				.equals(this.selected)));
		ret.add(new Element(PathSwitch.BRANCHES, this
				.generateUrl(PathSwitch.BRANCHES), PathSwitch.BRANCHES
				.equals(this.selected)));
		ret.add(new Element(PathSwitch.TAGS, this.generateUrl(PathSwitch.TAGS),
				PathSwitch.TAGS.equals(this.selected)));
		return ret;
	}

	protected String generateUrl(String type) {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.PATH_SWITCH, this.location);
		urlGenerator.addParameter(RequestParameters.PREFIX, UrlUtil
				.encode(this.urlPrefix));
		urlGenerator.addParameter(RequestParameters.SUFFIX, UrlUtil
				.encode(this.urlSuffix));
		urlGenerator.addParameter(RequestParameters.TYPE, type);
		urlGenerator.addParameter(RequestParameters.URL, UrlUtil
				.encode(this.url));
		if (this.isPickerMode) {
			urlGenerator.addParameter(RequestParameters.CONTENT_MODE_TYPE,
					LinkProviderFactory.PICKER_CONTENT_MODE_VALUE);
			if (this.isMultiSelectionUrl) {
				urlGenerator
						.addParameter(RequestParameters.MULTI_URL_SELECTION);
			}
			if (this.isSingleRevision) {
				urlGenerator.addParameter(RequestParameters.SINGLE_REVISION);
			}
		}
		if (this.currentRevision != -1) {
			urlGenerator.addParameter(RequestParameters.CREV, Long
					.toString(this.currentRevision));
		}
		return urlGenerator.getUrl();
	}
}
