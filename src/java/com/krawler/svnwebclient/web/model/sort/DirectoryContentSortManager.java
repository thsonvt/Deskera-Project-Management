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
package com.krawler.svnwebclient.web.model.sort;

import java.util.Comparator;

import javax.servlet.http.Cookie;

import com.krawler.svnwebclient.data.model.DataRepositoryElement;
import com.krawler.svnwebclient.web.resource.Images;
import com.krawler.svnwebclient.web.support.AbstractRequestHandler;
import com.krawler.svnwebclient.web.support.RequestParameters;
import com.krawler.svnwebclient.web.support.State;

public class DirectoryContentSortManager {
	protected static final String COOKIE_FIELD_NAME = "DirectoryContentSortField";
	protected static final String COOKIE_SORT_ORDER = "DirectoryContentSortOrder";

	public static final String ORDER_ASC = "asc";
	public static final String ORDER_DESC = "desc";

	public static final String FIELD_NAME = "name";
	public static final String FIELD_REVISION = "revision";
	public static final String FIELD_DATE = "date";
	public static final String FIELD_AUTHOR = "author";
	public static final String FIELD_SIZE = "size";

	protected State state;
	protected AbstractRequestHandler requestHandler;
	protected String sortField;
	protected boolean sortAscending;

	public DirectoryContentSortManager(State state,
			AbstractRequestHandler requestHandler) {
		this.state = state;
		this.requestHandler = requestHandler;

		this.sortField = this.requestHandler.getSortField();
		if (this.sortField == null) {
			String sortOrder = null;
			Cookie[] cookies = this.state.getRequest().getCookies();
			if (cookies != null) {
				for (int i = 0; i < cookies.length; i++) {
					if (COOKIE_FIELD_NAME.equals(cookies[i].getName())) {
						this.sortField = cookies[i].getValue();
					} else if (COOKIE_SORT_ORDER.equals(cookies[i].getName())) {
						sortOrder = cookies[i].getValue();
					}
				}
			}

			if (this.sortField == null) {
				this.sortField = DirectoryContentSortManager.FIELD_NAME;
			}
			this.initSortOrder(sortOrder);
		} else {
			this.initSortOrder(this.requestHandler.getSortOrder());
		}

		this.saveSettingsInCookies();
	}

	protected void initSortOrder(String sortOrder) {
		if (DirectoryContentSortManager.ORDER_ASC.equals(sortOrder)) {
			this.sortAscending = true;
		} else if (DirectoryContentSortManager.ORDER_DESC.equals(sortOrder)) {
			this.sortAscending = false;
		} else {
			this.sortAscending = true;
		}
	}

	protected void saveSettingsInCookies() {
		Cookie fieldCookie = new Cookie(
				DirectoryContentSortManager.COOKIE_FIELD_NAME, this.sortField);
		this.state.getResponse().addCookie(fieldCookie);
		Cookie orderCookie;
		if (this.sortAscending) {
			orderCookie = new Cookie(
					DirectoryContentSortManager.COOKIE_SORT_ORDER,
					DirectoryContentSortManager.ORDER_ASC);
		} else {
			orderCookie = new Cookie(
					DirectoryContentSortManager.COOKIE_SORT_ORDER,
					DirectoryContentSortManager.ORDER_DESC);
		}
		this.state.getResponse().addCookie(orderCookie);
	}

	public boolean hasSortIcon(String field) {
		return this.sortField.equals(field);
	}

	public String getSortIcon(String field) {
		if (this.sortField.equals(field)) {
			if (this.sortAscending) {
				return Images.ASC;
			} else {
				return Images.DESC;
			}
		} else {
			return null;
		}
	}

	public String getSortUrl(String field) {
		String sortOrder;
		if (this.sortAscending) {
			sortOrder = DirectoryContentSortManager.ORDER_DESC;
		} else {
			sortOrder = DirectoryContentSortManager.ORDER_ASC;
		}
		StringBuffer url = this.state.getRequest().getRequestURL();
		String queryString = this.state.getRequest().getQueryString();
		boolean firstParameter = true;
		if (queryString != null) {
			String[] parameters = queryString.split("&");
			for (int i = 0; i < parameters.length; i++) {
				String[] data = parameters[i].split("=");
				String parameterName = data[0];
				if (!RequestParameters.SORT_FIELD.equals(parameterName)
						&& !RequestParameters.SORT_ORDER.equals(parameterName)) {
					if (firstParameter) {
						firstParameter = false;
						url.append("?");
					} else {
						url.append("&");
					}
					url.append(parameterName);
					if (data.length > 1) {
						url.append("=");
						url.append(data[1]);
					}
				}
			}
		}
		if (firstParameter) {
			firstParameter = false;
			url.append("?");
		} else {
			url.append("&");
		}
		url.append(RequestParameters.SORT_FIELD);
		url.append("=");
		url.append(field);
		url.append("&");
		url.append(RequestParameters.SORT_ORDER);
		url.append("=");
		url.append(sortOrder);
		return url.toString();
	}

	public Comparator getComparator() {
		Comparator ret = null;
		if (DirectoryContentSortManager.FIELD_NAME.equals(this.sortField)) {
			ret = new Comparator() {
				public int compare(Object o1, Object o2) {
					if (o1 == null || o2 == null) {
						return 0;
					}
					int ret;
					if (((DataRepositoryElement) o1).isDirectory() == ((DataRepositoryElement) o2)
							.isDirectory()) {
						ret = ((DataRepositoryElement) o1).getName()
								.toLowerCase().compareTo(
										((DataRepositoryElement) o2).getName()
												.toLowerCase());
						if (!DirectoryContentSortManager.this.sortAscending) {
							ret = -ret;
						}
					} else {
						ret = ((DataRepositoryElement) o1).isDirectory() ? -1
								: 1;
					}
					return ret;
				}
			};
		} else if (DirectoryContentSortManager.FIELD_REVISION
				.equals(this.sortField)) {
			ret = new Comparator() {
				public int compare(Object o1, Object o2) {
					if (o1 == null || o2 == null) {
						return 0;
					}
					int ret;
					if (((DataRepositoryElement) o1).getRevision() == ((DataRepositoryElement) o2)
							.getRevision()) {
						ret = 0;
					} else {
						ret = (((DataRepositoryElement) o1).getRevision() > ((DataRepositoryElement) o2)
								.getRevision()) ? 1 : -1;
					}
					if (!DirectoryContentSortManager.this.sortAscending) {
						ret = -ret;
					}
					return ret;
				}
			};
		} else if (DirectoryContentSortManager.FIELD_DATE
				.equals(this.sortField)) {
			ret = new Comparator() {
				public int compare(Object o1, Object o2) {
					if (o1 == null || o2 == null) {
						return 0;
					}
					int ret = ((DataRepositoryElement) o1).getDate().compareTo(
							((DataRepositoryElement) o2).getDate());
					if (!DirectoryContentSortManager.this.sortAscending) {
						ret = -ret;
					}
					return ret;
				}
			};
		} else if (DirectoryContentSortManager.FIELD_AUTHOR
				.equals(this.sortField)) {
			ret = new Comparator() {
				public int compare(Object o1, Object o2) {
					if (o1 == null || o2 == null) {
						return 0;
					}
					int ret = ((DataRepositoryElement) o1).getAuthor()
							.toLowerCase().compareTo(
									((DataRepositoryElement) o2).getAuthor()
											.toLowerCase());
					if (!DirectoryContentSortManager.this.sortAscending) {
						ret = -ret;
					}
					return ret;
				}
			};
		} else if (DirectoryContentSortManager.FIELD_SIZE
				.equals(this.sortField)) {
			ret = new Comparator() {
				public int compare(Object o1, Object o2) {
					if (o1 == null || o2 == null) {
						return 0;
					}
					if (((DataRepositoryElement) o1).getSize() == ((DataRepositoryElement) o2)
							.getSize()) {
						return 0;
					}
					boolean greater = ((DataRepositoryElement) o1).getSize() > ((DataRepositoryElement) o2)
							.getSize();
					int ret;
					if (DirectoryContentSortManager.this.sortAscending) {
						ret = (greater) ? 1 : -1;
					} else {
						ret = (greater) ? -1 : 1;
					}
					return ret;
				}
			};
		}

		return ret;
	}
}
