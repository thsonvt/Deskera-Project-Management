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
import java.util.Iterator;
import java.util.List;

import com.krawler.svnwebclient.web.support.RequestParameters;

public class PageInfo {

	protected String page;
	protected String path;
	protected List parameters = new ArrayList();

	public PageInfo() {
	}

	public void init(String servletPath, String queryString) {
		if (servletPath != null) {
			if (servletPath.startsWith("/")) {
				this.page = servletPath.substring(1);
			} else {
				this.page = servletPath;
			}
		}

		if (queryString != null && !"".equals(queryString)) {
			String[] params = queryString.split("&");
			for (int i = 0; i < params.length; i++) {
				String param = params[i];
				this.processParameter(param);
			}
		}

		if (this.path == null) {
			this.path = "";
		}
	}

	protected void processParameter(String parameter) {
		String match = RequestParameters.URL + "=";
		if (parameter.startsWith(match) && match.length() < parameter.length()) {
			this.path = parameter.substring(match.length());
		} else {
			this.parameters.add(parameter);
		}
	}

	public String getPage() {
		return page;
	}

	public String getParameters() {
		if (!this.parameters.isEmpty()) {
			StringBuffer res = new StringBuffer();

			Iterator iter = this.parameters.iterator();
			while (iter.hasNext()) {
				String param = (String) iter.next();
				res.append(param);
				if (iter.hasNext()) {
					res.append("&");
				}
			}
			return res.toString();
		} else {
			return null;
		}
	}

	public String getPath() {
		return path;
	}

	public String toString() {
		StringBuffer res = new StringBuffer();
		res.append("Page: ").append(this.getPage());
		res.append(", path: ").append(this.getPath());
		res.append(", parameters: ").append(this.getParameters());
		return res.toString();
	}

	public static void main(String[] s) {
		String servletPath = "directoryContent.jsp";
		String queryString = "rev=21&url=webclient&crev=123";

		PageInfo info = new PageInfo();
		info.init(servletPath, queryString);
		System.out.println(info);

	}

}
