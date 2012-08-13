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
package com.krawler.svnwebclient.configuration;

import java.util.HashMap;
import java.util.Map;

public class WebConfigurationProvider {
	public static final String EMBEDDED = "Embedded";
	public static final String BASIC_AUTH = "BasicAuth";
	public static final String BASIC_REALM = "BasicRealm";
	public static final String FORCED_HTTP_AUTH = "ForcedHttpAuth";
	public static final String REPOSITORY_URL = "RepositoryUrl";
	public static final String USERNAME = "Username";
	public static final String PASSWORD = "Password";
	public static final String SVN_CONNECTIONS_COUNT = "SvnConnectionsCount";
	public static final String TEMP_DIRECTORY = "TempDirectory";
	public static final String VERSIONS_COUNT = "VersionsCount";
	public static final String PATH_AUTODETECT = "PathAutodetect";
	public static final String TRUNK_NAME = "TrunkName";
	public static final String BRANCHES_NAME = "BranchesName";
	public static final String TAGS_NAME = "TagsName";
	public static final String DEFAULT_ENCODING = "DefaultEncoding";
	public static final String ZIP_ENCODING = "ZipEncoding";
	public static final String SHOW_STACK_TRACE = "ShowStackTrace";
	public static final String CACHE_DIRECTORY = "CacheDirectory";
	public static final String CACHE_PAGE_SIZE = "CachePageSize";
	public static final String CACHE_PREFETCH_MESSAGES_COUNT = "CachePrefetchMessagesCount";
	public static final String REVISION_DECORATOR = "RevisionDecoratorClassName";
	public static final String ALTERNATIVE_VIEW_PROVIDER = "AlternativeViewProviderClassName";
	public static final String AUTHOR_DECORATOR = "AuthorDecoratorClassName";

	public static final String PARENT_REPOSITORY_DIRECTORY = "ParentRepositoryDirectory";
	public static final String LOGOUT = "Logout";
	public static final String HIDE_POLARION_COMMIT = "HidePolarionCommit";

	public static final String PROTOCOL_KEY_FILE = "ProtocolKeyFile";
	public static final String PROTOCOL_PASS_PHRASE = "ProtocolPassPhrase";
	public static final String PROTOCOL_PORT_NUMBER = "ProtocolPortNumber";
	// proxy
	public static final String PROXY_SUPPORTED = "ProxySupported";
	public static final String PROXY_HOST = "ProxyHost";
	public static final String PROXY_PORT_NUMBER = "ProxyPortNumber";
	public static final String PROXY_USER_NAME = "ProxyUserName";
	public static final String PROXY_PASSWORD = "ProxyPassword";

	public static final String CHARACTER_ENCODINGS = "CharacterEncodings";

	// picker
	public static final String PICKER_MULTI_URL = "PickerMultiUrl";

	// email settings
	public static final String EMAIL_FROM = "EmailFrom";
	public static final String EMAIL_TO = "EmailTo";
	public static final String EMAIL_PROJECT_NAME = "EmailProjectName";
	public static final String EMAIL_HOST = "EmailHost";
	public static final String EMAIL_PORT = "EmailPort";

	public static final String VALUE_TRUE = "true";
	public static final String VALUE_FALSE = "false";

	// file store setting
	public static final String PROFILE_IMAGE_PATH_BASE = "ProfileImagePathBase";
	public static final String DOC_STORE_PATH_KEY = "DocStorePath";
	public static final String CURRENT_STORE_INDEX = "CurrentStoreIndex";
	public static final String INDEXSER_STORE_PATH = "indexpath";
	public static final String SUMMARY_LENGTH = "Summary-length";
	public static final String SUMMARY_CONTEXT = "Summary-context";
	public static final String LMS_STORE_PATH_KEY = "LmsStorePath";
	public static final String SMTP_PATH = "SMTPPath";
	public static final String SMTP_PORT = "SMTPPort";

	private static WebConfigurationProvider instance;

	protected Map parameters = new HashMap();

	private WebConfigurationProvider() {
	}

	public static synchronized WebConfigurationProvider getInstance() {
		if (WebConfigurationProvider.instance == null) {
			WebConfigurationProvider.instance = new WebConfigurationProvider();
		}

		return WebConfigurationProvider.instance;
	}

	public void setParameters(Map parameters) {
		this.parameters = parameters;
	}

	public Map getParameters() {
		return this.parameters;
	}

	public void setParameter(String name, String value) {
		this.parameters.put(name, value);
	}

	public String getParameter(String name) {
		return (String) this.parameters.get(name);
	}
}
