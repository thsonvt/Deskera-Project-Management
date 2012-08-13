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
package com.krawler.svnwebclient.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UrlGenerator {
	protected String url;
	protected List parameters = new ArrayList();
	private String anchor = null;

	public UrlGenerator(String url) {
		this.url = url;
	}

	public void addParameter(String name, String value) {
		this.parameters.add(name + "=" + value);
	}

	public void addParameter(String name) {
		this.parameters.add(name);
	}

	public void setAnchor(String anchor) {
		this.anchor = anchor;
	}

	public String getUrl() {
		StringBuffer ret = new StringBuffer(this.url);
		if (this.parameters.size() > 0) {
			ret.append("?");
			for (Iterator i = this.parameters.iterator(); i.hasNext();) {
				String parameter = (String) i.next();
				ret.append(parameter);
				if (i.hasNext()) {
					ret.append("&");
				}
			}
		}
		if (anchor != null) {
			ret.append("#" + anchor);
		}
		return ret.toString();
	}

	public String getUrl1() {
		StringBuffer ret = new StringBuffer(this.url);
		if (this.parameters.size() > 0) {
			ret.append("?");
			for (Iterator i = this.parameters.iterator(); i.hasNext();) {
				String parameter = (String) i.next();
				ret.append(parameter);
				if (i.hasNext()) {
					ret.append("%26");
				}
			}
		}
		if (anchor != null) {
			ret.append("#" + anchor);
		}
		return ret.toString();
	}
}
