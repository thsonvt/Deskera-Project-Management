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
package com.krawler.svnwebclient.decorations;

import com.krawler.svnwebclient.util.UrlGenerator;
import com.krawler.svnwebclient.util.UrlGeneratorFactory;
import com.krawler.svnwebclient.util.UrlUtil;
import com.krawler.svnwebclient.web.resource.Links;
import com.krawler.svnwebclient.web.support.RequestParameters;

public class BaseViewProvider implements IAlternativeViewProvider {
	public static final String CONTENT = "Content";
	public static final String ANNOTATE = "Annotate";
	protected String location;

	public BaseViewProvider(String location) {
		this.location = location;
	}

	public String[] getAvailableAlternativeViews(String resourceUrl,
			long revision) {
		String[] ret = new String[2];
		ret[0] = BaseViewProvider.CONTENT;
		ret[1] = BaseViewProvider.ANNOTATE;
		return ret;
	}

	public String getAlternativeViewContentUrl(String resourceUrl,
			long revision, long line, String viewName) {
		String ret = null;
		if (BaseViewProvider.CONTENT.equals(viewName)) {
			UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					Links.FILE_DATA, this.location);
			urlGenerator.addParameter(RequestParameters.URL, UrlUtil
					.encode(resourceUrl));
			urlGenerator.addParameter(RequestParameters.CREV, Long
					.toString(revision));
			if (line > 0) {
				urlGenerator.setAnchor(Long.toString(line));
			}
			ret = urlGenerator.getUrl();
		} else if (BaseViewProvider.ANNOTATE.equals(viewName)) {
			UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
					Links.FILE_ANNOTATION, this.location);
			urlGenerator.addParameter(RequestParameters.URL, UrlUtil
					.encode(resourceUrl));
			urlGenerator.addParameter(RequestParameters.CREV, Long
					.toString(revision));
			ret = urlGenerator.getUrl();
		}
		return ret;
	}
}
