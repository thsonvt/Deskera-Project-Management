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

import com.krawler.svnwebclient.util.HtmlUtil;
import com.krawler.svnwebclient.util.NumberFormatter;
import com.krawler.svnwebclient.util.UrlGenerator;
import com.krawler.svnwebclient.util.UrlGeneratorFactory;
import com.krawler.svnwebclient.util.UrlUtil;
import com.krawler.svnwebclient.web.resource.Links;
import com.krawler.svnwebclient.web.support.RequestParameters;

public class SwitchToHead {
	protected String originalUrl;
	protected long revision;
	protected long headRevision;
	protected String urlInRevision;
	protected String urlInHead;
	protected boolean redirectToDirectory;
	protected String location;

	public SwitchToHead(String location, String originalUrl,
			String urlInRevision, String urlInHead, boolean redirectToDirectory) {
		this.location = location;
		this.originalUrl = originalUrl;
		this.urlInRevision = urlInRevision;
		this.urlInHead = urlInHead;
		this.redirectToDirectory = redirectToDirectory;
	}

	public void setRevision(long revision) {
		this.revision = revision;
	}

	public void setHeadRevision(long headRevision) {
		this.headRevision = headRevision;
	}

	public String getOriginalUrl() {
		return HtmlUtil.encode(this.originalUrl);
	}

	public String getRevision() {
		return Long.toString(this.revision);
	}

	public String getDecoratedRevision() {
		return NumberFormatter.format(this.revision);
	}

	public String getHeadRevision() {
		return Long.toString(this.headRevision);
	}

	public String getUrlInRevision() {
		return HtmlUtil.encode(this.urlInRevision);
	}

	public String getUrlInHead() {
		return HtmlUtil.encode(this.urlInHead);
	}

	public String getUrl() {
		String ret = "";
		if (this.urlInHead != null) {
			UrlGenerator urlGenerator;
			if (this.redirectToDirectory) {
				urlGenerator = UrlGeneratorFactory.getUrlGenerator(
						Links.DIRECTORY_CONTENT, this.location);
			} else {
				urlGenerator = UrlGeneratorFactory.getUrlGenerator(
						Links.FILE_CONTENT, this.location);
			}

			urlGenerator.addParameter(RequestParameters.URL, UrlUtil
					.encode(this.urlInHead));
			ret = urlGenerator.getUrl();
		}
		return ret;
	}
}
