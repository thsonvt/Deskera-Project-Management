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

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.polarion.svncommons.commentscache.configuration.ProxySettings;

import com.krawler.svnwebclient.decorations.IAlternativeViewProvider;
import com.krawler.svnwebclient.decorations.IAuthorDecorator;
import com.krawler.svnwebclient.decorations.IRevisionDecorator;

public class ConfigurationProvider implements IConfigurationProvider {
	protected static ConfigurationProvider instance;

	protected IRevisionDecorator revisionDecorator;
	protected IAlternativeViewProvider alternativeViewProvider;
	protected IAuthorDecorator authorDecorator;

	protected ConfigurationError error = new ConfigurationError();

	private ConfigurationProvider() {
	}

    @Override
	public ConfigurationError getConfigurationError() {
		return this.error;
	}

	public static synchronized ConfigurationProvider getInstance() {
		if (ConfigurationProvider.instance == null) {
			ConfigurationProvider.instance = new ConfigurationProvider();
		}
		return ConfigurationProvider.instance;
	}

    @Override
	public void checkConfiguration() throws ConfigurationException {
		String parentUrl = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.PARENT_REPOSITORY_DIRECTORY);
		String url = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.REPOSITORY_URL);
		if ((parentUrl == null && url == null)
				|| (parentUrl != null && url != null)) {
			throw new ConfigurationException(
					"You have to specify only either ParentRepositoryDirectory or RepositoryUrl in web.xml, don't mix them.");
		}
		if (url != null) {
			this.checkNotNullOrEmpty(WebConfigurationProvider.REPOSITORY_URL);
		} else if (parentUrl != null) {
			this
					.checkNotNullOrEmpty(WebConfigurationProvider.PARENT_REPOSITORY_DIRECTORY);
		}

		this.checkLong(WebConfigurationProvider.SVN_CONNECTIONS_COUNT);
		this.checkLong(WebConfigurationProvider.VERSIONS_COUNT);

		this.checkBoolean(WebConfigurationProvider.PATH_AUTODETECT);
		if (this.isPathAutodetect()) {
			this.checkNotNullOrEmpty(WebConfigurationProvider.TRUNK_NAME);
			this.checkNotNullOrEmpty(WebConfigurationProvider.BRANCHES_NAME);
			this.checkNotNullOrEmpty(WebConfigurationProvider.TAGS_NAME);
		}

		this.checkNotNullOrEmpty(WebConfigurationProvider.DEFAULT_ENCODING);
		this.checkNotNullOrEmpty(WebConfigurationProvider.ZIP_ENCODING);

		this.checkLong(WebConfigurationProvider.CACHE_PAGE_SIZE);
		this.checkLong(WebConfigurationProvider.CACHE_PREFETCH_MESSAGES_COUNT);

		if (WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.SHOW_STACK_TRACE) != null) {
			this.checkBoolean(WebConfigurationProvider.SHOW_STACK_TRACE);
		}

		this.checkBoolean(WebConfigurationProvider.EMBEDDED);
		this.checkBoolean(WebConfigurationProvider.HIDE_POLARION_COMMIT);
		this.checkBoolean(WebConfigurationProvider.LOGOUT);

		this.checkBoolean(WebConfigurationProvider.BASIC_AUTH);
		if (this.isBasicAuth()) {
			this.checkNotNullOrEmpty(WebConfigurationProvider.BASIC_REALM);
		}

		this.checkNotNullOrEmpty(WebConfigurationProvider.REVISION_DECORATOR);
		this
				.checkNotNullOrEmpty(WebConfigurationProvider.ALTERNATIVE_VIEW_PROVIDER);

		if (IConfigurationProvider.SVN_SSH == this.getProtocolType()) {
			this.checkInt(WebConfigurationProvider.PROTOCOL_PORT_NUMBER);
			this.checkNotNull(WebConfigurationProvider.USERNAME);
			if (this.getProtocolKeyFile() != null) {
				this
						.checkNotNull(WebConfigurationProvider.PROTOCOL_PASS_PHRASE);
				this
						.checkNotNullOrEmpty(WebConfigurationProvider.PROTOCOL_KEY_FILE);
			} else {
				this.checkNotNull(WebConfigurationProvider.PASSWORD);
			}
		} else if (IConfigurationProvider.SSL == this.getProtocolType()) {
			this.checkNotNull(WebConfigurationProvider.PROTOCOL_PASS_PHRASE);
			this
					.checkNotNullOrEmpty(WebConfigurationProvider.PROTOCOL_KEY_FILE);
		} else {
			// http, svn
			if (url != null) {
				this.checkNotNull(WebConfigurationProvider.USERNAME);
				this.checkNotNull(WebConfigurationProvider.PASSWORD);
			}
		}
		this.checkBoolean(WebConfigurationProvider.PROXY_SUPPORTED);
		if (new Boolean(WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.PROXY_SUPPORTED)).booleanValue() == true) {
			this.checkInt(WebConfigurationProvider.PROXY_PORT_NUMBER);
			this.checkNotNullOrEmpty(WebConfigurationProvider.PROXY_HOST);
		}
	}

    @Override
	public String getEmailFrom() {
		String emailFrom = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.EMAIL_FROM);
		return emailFrom;
	}

    @Override
	public String getEmailTo() {
		String emailTo = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.EMAIL_TO);
		return emailTo;
	}

    @Override
	public String getEmailHost() {
		String emailHost = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.EMAIL_HOST);
		return emailHost;
	}

    @Override
	public String getEmailPort() {
		String emailPort = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.EMAIL_PORT);
		return emailPort;
	}

    @Override
	public String getEmailProject() {
		String emailProject = WebConfigurationProvider.getInstance()
				.getParameter(WebConfigurationProvider.EMAIL_PROJECT_NAME);
		return emailProject;
	}

    @Override
	public boolean isEmbedded() {
		String value = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.EMBEDDED);
		value = value.trim();
		if (WebConfigurationProvider.VALUE_TRUE.equalsIgnoreCase(value)) {
			return true;
		} else {
			return false;
		}
	}

    @Override
	public boolean isBasicAuth() {
		String value = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.BASIC_AUTH);
		value = value.trim();
		if (WebConfigurationProvider.VALUE_TRUE.equalsIgnoreCase(value)) {
			return true;
		} else {
			return false;
		}
	}

    @Override
	public boolean isForcedHttpAuth() {
		String value = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.FORCED_HTTP_AUTH);
		value = value.trim();
		if (WebConfigurationProvider.VALUE_TRUE.equalsIgnoreCase(value)) {
			return true;
		} else {
			return false;
		}
	}

    @Override
	public boolean isHidePolarionCommit() {
		String value = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.HIDE_POLARION_COMMIT);
		value = value.trim();
		if (WebConfigurationProvider.VALUE_TRUE.equalsIgnoreCase(value)) {
			return true;
		} else {
			return false;
		}
	}

    @Override
	public String getBasicRealm() {
		String value = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.BASIC_REALM);
		return value.trim();
	}

    @Override
	public String getParentRepositoryDirectory() {
		String value = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.PARENT_REPOSITORY_DIRECTORY);
		if (value == null) {
			return null;
		} else {
			value = value.trim();
			if (value.endsWith("/")) {
				value = value.substring(0, value.length() - 1);
			}
			return value.trim();
		}
	}

	public boolean isMultiRepositoryMode() {
		return this.getParentRepositoryDirectory() == null ? false : true;
	}

    @Override
	public String getRepositoryUrl() {
		String value = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.REPOSITORY_URL);
		if (value.endsWith("/")) {
			value = value.substring(0, value.length() - 1);
		}
		return value.trim();
	}

    @Override
	public String getUsername() {
		String value = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.USERNAME);
		return value.trim();
	}

    @Override
	public String getPassword() {
		String value = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.PASSWORD);
		return value.trim();
	}

    @Override
	public long getSvnConnectionsCount() {
		String value = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.SVN_CONNECTIONS_COUNT);
		return Long.parseLong(value);
	}

    @Override
	public String getTempDirectory() {
		String result = null;
		String value = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.TEMP_DIRECTORY);
		if (value == null) {
			result = this.getOSTempDir();
			int index = result
					.lastIndexOf(System.getProperty("file.separator"));
			if (index != -1) {
				result = result.substring(0, index);
			}
		} else {
			result = value.trim();
		}
		return result;
	}

    @Override
	public long getVersionsCount() {
		String value = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.VERSIONS_COUNT);
		return Long.parseLong(value);
	}

    @Override
	public boolean isPathAutodetect() {
		String value = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.PATH_AUTODETECT);
		value = value.trim();
		if (WebConfigurationProvider.VALUE_TRUE.equalsIgnoreCase(value)) {
			return true;
		} else {
			return false;
		}
	}

    @Override
	public String getTrunkName() {
		String value = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.TRUNK_NAME);
		if (value != null) {
			value = value.trim();
		}
		return value;
	}

    @Override
	public String getBranchesName() {
		String value = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.BRANCHES_NAME);
		if (value != null) {
			value = value.trim();
		}
		return value;
	}

    @Override
	public String getTagsName() {
		String value = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.TAGS_NAME);
		if (value != null) {
			value = value.trim();
		}
		return value;
	}

    @Override
	public String getDefaultEncoding() {
		String value = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.DEFAULT_ENCODING);
		return value.trim();
	}

    @Override
	public String getZipEncoding() {
		String value = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.ZIP_ENCODING);
		return value.trim();
	}

    @Override
	public boolean isShowStackTrace() {
		String value = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.SHOW_STACK_TRACE);
		value = value.trim();
		if (WebConfigurationProvider.VALUE_TRUE.equalsIgnoreCase(value)) {
			return true;
		} else {
			return false;
		}
	}

	public String getCacheDirectory() {
		String result = null;
		String value = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.CACHE_DIRECTORY);
		if (value == null) {
			String tmpDir = this.getOSTempDir();
			result = tmpDir + "cache";
		} else {
			result = value.trim();
		}
		return result;
	}

	public long getCachePageSize() {
		String value = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.CACHE_PAGE_SIZE);
		return Long.parseLong(value);
	}

	public long getCachePrefetchMessagesCount() {
		String value = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.CACHE_PREFETCH_MESSAGES_COUNT);
		return Long.parseLong(value);
	}

	public boolean isLogout() {
		String value = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.LOGOUT);
		return new Boolean(value).booleanValue();
	}

	public String getProtocolKeyFile() {
		return WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.PROTOCOL_KEY_FILE);
	}

	public String getProtocolPassPhrase() {
		return WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.PROTOCOL_PASS_PHRASE);
	}

	public int getProtocolPortNumber() {
		String value = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.PROTOCOL_PORT_NUMBER);
		return Integer.parseInt(value);
	}

	public ProxySettings getProxy() {
		ProxySettings proxy = new ProxySettings();
		String isSupported = WebConfigurationProvider.getInstance()
				.getParameter(WebConfigurationProvider.PROXY_SUPPORTED);
		proxy.setProxySupported(new Boolean(isSupported).booleanValue());
		if (new Boolean(isSupported).booleanValue() == true) {
			proxy.setHost(WebConfigurationProvider.getInstance().getParameter(
					WebConfigurationProvider.PROXY_HOST));
			proxy.setUserName(WebConfigurationProvider.getInstance()
					.getParameter(WebConfigurationProvider.PROXY_USER_NAME));
			proxy.setPassword(WebConfigurationProvider.getInstance()
					.getParameter(WebConfigurationProvider.PROXY_PASSWORD));
			String port = WebConfigurationProvider.getInstance().getParameter(
					WebConfigurationProvider.PROXY_PORT_NUMBER);
			if (port != null) {
				proxy.setPort(Integer.parseInt(port));
			}
		}
		return proxy;
	}

	public IRevisionDecorator getRevisionDecorator() {
		if (this.revisionDecorator == null) {
			this.revisionDecorator = (IRevisionDecorator) this
					.instantiate(WebConfigurationProvider
							.getInstance()
							.getParameter(
									WebConfigurationProvider.REVISION_DECORATOR));
		}
		return this.revisionDecorator;
	}

	public IAlternativeViewProvider getAlternativeViewProvider() {
		if (this.alternativeViewProvider == null) {
			this.alternativeViewProvider = (IAlternativeViewProvider) this
					.instantiate(WebConfigurationProvider
							.getInstance()
							.getParameter(
									WebConfigurationProvider.ALTERNATIVE_VIEW_PROVIDER));
		}
		return this.alternativeViewProvider;
	}

	public IAuthorDecorator getAuthorDecorator() {
		if (this.authorDecorator == null) {
			this.authorDecorator = (IAuthorDecorator) this
					.instantiate(WebConfigurationProvider.getInstance()
							.getParameter(
									WebConfigurationProvider.AUTHOR_DECORATOR));
		}
		return this.authorDecorator;
	}

	public int getProtocolType() {
		String url = null;
		if (this.isMultiRepositoryMode()) {
			url = this.getParentRepositoryDirectory();
		} else {
			url = this.getRepositoryUrl();
		}

		if (url.indexOf("svn+ssh://") != -1) {
			return IConfigurationProvider.SVN_SSH;
		} else if (url.indexOf("https://") != -1) {
			return IConfigurationProvider.SSL;
		} else {
			return IConfigurationProvider.HTTP;
		}
	}

	protected void checkNotNull(String parameterName)
			throws ConfigurationException {
		String value = WebConfigurationProvider.getInstance().getParameter(
				parameterName);
		if (value == null) {
			throw new ConfigurationException(parameterName
					+ " configuration parameter must be defined");
		}
	}

	protected Object instantiate(String className) {
		try {
			Class clazz = Class.forName(className);
			return clazz.newInstance();
		} catch (ClassNotFoundException e) {
			Logger.getLogger(this.getClass()).error(e, e);
		} catch (InstantiationException e) {
			Logger.getLogger(this.getClass()).error(e, e);
		} catch (IllegalAccessException e) {
			Logger.getLogger(this.getClass()).error(e, e);
		}
		return null;
	}

	protected void checkNotNullOrEmpty(String parameterName)
			throws ConfigurationException {
		String value = WebConfigurationProvider.getInstance().getParameter(
				parameterName);
		if (value == null) {
			throw new ConfigurationException(parameterName
					+ " configuration parameter must be defined");
		}
		value = value.trim();
		if (value.length() == 0) {
			throw new ConfigurationException("Invalid value \"" + value
					+ "\" of " + parameterName + " configuration parameter. "
					+ "It must be not empty string");
		}
	}

	protected void checkBoolean(String parameterName)
			throws ConfigurationException {
		String value = WebConfigurationProvider.getInstance().getParameter(
				parameterName);
		if (value == null) {
			throw new ConfigurationException(parameterName
					+ " configuration parameter must be defined");
		}
		value = value.trim();
		if (!(WebConfigurationProvider.VALUE_TRUE.equalsIgnoreCase(value) || WebConfigurationProvider.VALUE_FALSE
				.equalsIgnoreCase(value))) {
			throw new ConfigurationException("Invalid value \"" + value
					+ "\" of " + parameterName + " configuration parameter. "
					+ "Only " + WebConfigurationProvider.VALUE_TRUE + " and "
					+ WebConfigurationProvider.VALUE_FALSE + " are allowed");
		}
	}

	protected void checkLong(String parameterName)
			throws ConfigurationException {
		String value = WebConfigurationProvider.getInstance().getParameter(
				parameterName);
		if (value == null) {
			throw new ConfigurationException(parameterName
					+ " configuration parameter must be defined");
		}

		try {
			Long.parseLong(value);
		} catch (NumberFormatException e) {
			throw new ConfigurationException("Invalid value \"" + value
					+ "\" of " + parameterName + " configuration parameter. "
					+ "It must be numeric");
		}
	}

	protected void checkInt(String parameterName) throws ConfigurationException {
		String value = WebConfigurationProvider.getInstance().getParameter(
				parameterName);
		if (value == null) {
			throw new ConfigurationException(parameterName
					+ " configuration parameter must be defined");
		}

		try {
			Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new ConfigurationException("Invalid value \"" + value
					+ "\" of " + parameterName + " configuration parameter. "
					+ "It must be numeric");
		}
	}

	protected String getOSTempDir() {
		String tempdir = System.getProperty("java.io.tmpdir");
		if (!(tempdir.endsWith("/") || tempdir.endsWith("\\"))) {
			tempdir = tempdir + System.getProperty("file.separator");
		}
		return tempdir;
	}

	public String getRepositoryLocation(String repositoryName) {
		String res = "";
		if (!this.isMultiRepositoryMode()) {
			res = this.getRepositoryUrl();
		} else {
			res += this.getParentRepositoryDirectory() + "/" + repositoryName;
		}
		return res;
	}

	public Set getCharacterEncodings() {
		Set res = new HashSet();

		String strEncodings = WebConfigurationProvider.getInstance()
				.getParameter(WebConfigurationProvider.CHARACTER_ENCODINGS);
		if (strEncodings != null) {
			strEncodings = strEncodings.trim();

			String[] encodings = strEncodings.split(",");
			for (int i = 0; i < encodings.length; i++) {
				String encoding = encodings[i];
				res.add(encoding.trim());
			}
		}
		return res;
	}

	public String getProfileImagePathBase() throws ConfigurationException {
		String value = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.PROFILE_IMAGE_PATH_BASE);
		if (value == null) {
			throw new ConfigurationException(
					WebConfigurationProvider.PROFILE_IMAGE_PATH_BASE
							+ " configuration parameter must be defined");
		} else {
			return value;
		}
	}

	public String getDocStorePath() throws ConfigurationException {
		return getDocStorePath(getCurrentStorageIndex());
	}

	public String getDocStorePath(String storeIndex)
			throws ConfigurationException {
		String docStorePathKey = WebConfigurationProvider.DOC_STORE_PATH_KEY
				+ storeIndex;
		String docStorePath = WebConfigurationProvider.getInstance()
				.getParameter(docStorePathKey);
		if (docStorePath == null) {
			throw new ConfigurationException(docStorePathKey
					+ " configuration parameter must be defined");
		} else {
			return docStorePath;
		}
	}

	public String getSMTPPath() {
		String smtpPath = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.SMTP_PATH);
		return smtpPath;
	}

	public String getSMTPPort() {
		String smtpPort = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.SMTP_PORT);
		return smtpPort;
	}

	public String getCurrentStorageIndex() {
		String currentStoreIndex = WebConfigurationProvider.getInstance()
				.getParameter(WebConfigurationProvider.CURRENT_STORE_INDEX);
		if (currentStoreIndex == null) {
			currentStoreIndex = "0";
		}
		return currentStoreIndex;
	}

	public String getDocIndexPath() throws ConfigurationException {
		String value = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.INDEXSER_STORE_PATH);
		if (value == null) {
			throw new ConfigurationException(
					WebConfigurationProvider.INDEXSER_STORE_PATH
							+ " configuration parameter must be defined");
		} else {
			return value;
		}
	}

	public String getLmsStorePath() throws ConfigurationException {
		return getLmsStorePath(getCurrentStorageIndex());
	}

	public String getLmsStorePath(String storeIndex)
			throws ConfigurationException {
		String lmsStorePathKey = WebConfigurationProvider.LMS_STORE_PATH_KEY
				+ storeIndex;
		String lmsStorePath = WebConfigurationProvider.getInstance()
				.getParameter(lmsStorePathKey);
		if (lmsStorePath == null) {
			throw new ConfigurationException(lmsStorePathKey
					+ " configuration parameter must be defined");
		} else {
			return lmsStorePath;
		}
	}

	public String getSummaryLength(String defaultLength) {
		String value = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.SUMMARY_LENGTH);
		if (value == null) {
			value = defaultLength;
		}
		return value;
	}

	public String getSummaryContext(String defaultContext) {
		String value = WebConfigurationProvider.getInstance().getParameter(
				WebConfigurationProvider.SUMMARY_CONTEXT);
		if (value == null) {
			value = defaultContext;
		}
		return value;
	}

}
