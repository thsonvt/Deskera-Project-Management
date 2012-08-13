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
package com.krawler.svnwebclient.web.support;

import javax.servlet.http.HttpServletRequest;

import com.krawler.svnwebclient.configuration.ConfigurationProvider;
import com.krawler.svnwebclient.util.UrlUtil;

public abstract class AbstractRequestHandler {
	protected HttpServletRequest request;

	public AbstractRequestHandler(HttpServletRequest request) {
		this.request = request;
	}

	public String getUrl() {
		String value = this.request.getParameter(RequestParameters.URL);
		if (value == null) {
			value = "";
		}
		return value;
	}

	public String getLocation() {
		String res = "";
		if (ConfigurationProvider.getInstance().isMultiRepositoryMode()) {
			res = this.request.getParameter(RequestParameters.LOCATION);
			if (res == null) {
				res = "";
			}
		}
		return res;
	}

	public long getCurrentRevision() {
		long ret = -1;
		String value = this.request.getParameter(RequestParameters.CREV);
		if (value != null) {
			try {
				ret = Long.parseLong(value);
			} catch (NumberFormatException e) {
			}
		}
		return ret;
	}

	public long getPegRevision() {
		long ret = -1;
		String value = this.request.getParameter(RequestParameters.PEGREV);
		if (value != null) {
			try {
				ret = Long.parseLong(value);
			} catch (NumberFormatException e) {
			}
		}
		return ret;
	}

	public long getRevision() {
		long ret = -1;
		String value = this.request.getParameter(RequestParameters.REV);
		if (value != null) {
			try {
				ret = Long.parseLong(value);
			} catch (NumberFormatException e) {
			}
		}
		return ret;
	}

	public long getRevisionCount() {
		long ret = 0;
		String value = this.request.getParameter(RequestParameters.REVCOUNT);
		if (value != null) {
			try {
				ret = Long.parseLong(value);
			} catch (NumberFormatException e) {
			}
		}
		return ret;
	}

	public String getView() {
		return this.request.getParameter(RequestParameters.VIEW);
	}

	public long getStartRevision() {
		long ret = -1;
		String value = this.request.getParameter(RequestParameters.STARTREV);
		if (value != null) {
			try {
				ret = Long.parseLong(value);
			} catch (NumberFormatException e) {
			}
		}
		return ret;
	}

	public long getEndRevision() {
		long ret = -1;
		String value = this.request.getParameter(RequestParameters.ENDREV);
		if (value != null) {
			try {
				ret = Long.parseLong(value);
			} catch (NumberFormatException e) {
			}
		}
		return ret;
	}

	public String getUrlPrefix() {
		String value = this.request.getParameter(RequestParameters.PREFIX);
		if (value == null) {
			value = "";
		}
		return UrlUtil.decode(value);
	}

	public String getUrlSuffix() {
		String value = this.request.getParameter(RequestParameters.SUFFIX);
		if (value == null) {
			value = "";
		}
		return UrlUtil.decode(value);
	}

	public String getType() {
		return this.request.getParameter(RequestParameters.TYPE);
	}

	public String getSortOrder() {
		return this.request.getParameter(RequestParameters.SORT_ORDER);
	}

	public String getSortField() {
		return this.request.getParameter(RequestParameters.SORT_FIELD);
	}

	public String getAction() {
		return this.request.getParameter(RequestParameters.ACTION);
	}

	public String getUsername() {
		String value = this.request.getParameter(RequestParameters.USERNAME);
		if (value == null) {
			value = "";
		}
		return value;
	}

	public String getPassword() {
		String value = this.request.getParameter(RequestParameters.PASSWORD);
		if (value == null) {
			value = "";
		}
		return value;
	}

	public String getRepositoryName() {
		String value = this.request.getParameter(RequestParameters.LOCATION);
		if (value == null) {
			value = "";
		}
		return value;
	}

	public boolean isAttachment() {
		String value = this.request.getParameter(RequestParameters.ATTACHMENT);
		if (RequestParameters.VALUE_TRUE.equals(value)) {
			return true;
		} else {
			return false;
		}
	}

	public abstract void check() throws RequestException;

	protected void checkNotNull(String parameterName) throws RequestException {
		String value = this.request.getParameter(parameterName);
		if (value == null) {
			throw new RequestException(parameterName
					+ " request parameter must be defined");
		}
	}

	protected void checkNotNullOrEmpty(String parameterName)
			throws RequestException {
		String value = this.request.getParameter(parameterName);
		if (value == null) {
			throw new RequestException(parameterName
					+ " request parameter must be defined");
		}
		value = value.trim();
		if (value.length() == 0) {
			throw new RequestException("Invalid value \"" + value + "\" of "
					+ parameterName + " request parameter. "
					+ "It must be not empty string");
		}
	}

	protected void checkLong(String parameterName) throws RequestException {
		String value = this.request.getParameter(parameterName);
		if (value == null) {
			throw new RequestException(parameterName
					+ " request parameter must be defined");
		}
		try {
			Long.parseLong(value);
		} catch (NumberFormatException e) {
			throw new RequestException("Invalid value \"" + value + "\" of "
					+ parameterName + " request parameter. "
					+ "It must be numeric");
		}
	}

	public long getLine() {
		long line = 0;
		String lineParam = this.request.getParameter(RequestParameters.LINE);
		if (lineParam != null) {
			try {
				line = Integer.parseInt(lineParam);
			} catch (NumberFormatException e) {
			}
		}
		return line;
	}

	public String getContentMode() {
		return this.request.getParameter(RequestParameters.CONTENT_MODE_TYPE);
	}

	public boolean isSingleRevisionMode() {
		boolean isSingle = false;
		if (this.request.getParameter(RequestParameters.SINGLE_REVISION) != null) {
			isSingle = true;
		}
		return isSingle;
	}

	public boolean isMultiUrlSelection() {
		boolean isMulti = false;
		if (this.request.getParameter(RequestParameters.MULTI_URL_SELECTION) != null) {
			isMulti = true;
		}
		return isMulti;
	}

}
