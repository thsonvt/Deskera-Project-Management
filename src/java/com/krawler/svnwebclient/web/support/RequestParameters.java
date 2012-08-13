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

public interface RequestParameters {
	String URL = "url";
	String LOCATION = "location";
	String CREV = "crev";
	String PEGREV = "pegrev";
	String REV = "rev";
	String REVCOUNT = "revcount";
	String VIEW = "view";
	String ATTACHMENT = "attachment";
	String STARTREV = "startrev";
	String ENDREV = "endrev";
	String PREFIX = "prefix";
	String SUFFIX = "suffix";
	String TYPE = "type";
	String SORT_ORDER = "sortorder";
	String SORT_FIELD = "sortfield";
	String ACTION = "action";
	String LINE = "line";

	String RETRY_AGAIN = "retryagain";
	String ERROR_MESSAGE = "errormessage";

	String USERNAME = "username";
	String PASSWORD = "password";
	String VALUE_TRUE = "true";
	String VALUE_FALSE = "false";

	String START_REVISION = "startrevision";
	String END_REVISION = "endrevision";
	String REV_COUNT = "revcount";
	String HIDE_POLARION_COMMIT = "hidepolarioncommit";

	// picker
	String CONTENT_MODE_TYPE = "contentmodetype";
	String SINGLE_REVISION = "singlerevision";
	String MULTI_URL_SELECTION = "multiurlselection";

	String CHARACTER_ENCODING = "encoding";

	// session params
	String DEFAULT_CHARACTER_ENCODING = "defaultCharacterEncoding";
}
