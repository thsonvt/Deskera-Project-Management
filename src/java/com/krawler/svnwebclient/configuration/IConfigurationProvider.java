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

import java.util.Set;

import org.polarion.svncommons.commentscache.configuration.ProxySettings;

import com.krawler.svnwebclient.decorations.IAlternativeViewProvider;
import com.krawler.svnwebclient.decorations.IAuthorDecorator;
import com.krawler.svnwebclient.decorations.IRevisionDecorator;

public interface IConfigurationProvider {
	int SVN_SSH = 1;
	int SSL = 2;
	int HTTP = 0;

	void checkConfiguration() throws ConfigurationException;

	boolean isEmbedded();

	String getRepositoryUrl();

	String getUsername();

	String getPassword();

	long getSvnConnectionsCount();

	String getTempDirectory();

	long getVersionsCount();

	boolean isPathAutodetect();

	String getTrunkName();

	String getBranchesName();

	String getTagsName();

	String getDefaultEncoding();

	String getZipEncoding();

	boolean isShowStackTrace();

	String getCacheDirectory();

	long getCachePageSize();

	long getCachePrefetchMessagesCount();

	String getBasicRealm();

	boolean isBasicAuth();

	boolean isForcedHttpAuth();

	IRevisionDecorator getRevisionDecorator();

	IAlternativeViewProvider getAlternativeViewProvider();

	IAuthorDecorator getAuthorDecorator();

	String getParentRepositoryDirectory();

	boolean isLogout();

	boolean isHidePolarionCommit();

	ConfigurationError getConfigurationError();

	String getProtocolKeyFile();

	String getProtocolPassPhrase();

	int getProtocolPortNumber();

	int getProtocolType();

	ProxySettings getProxy();

	String getRepositoryLocation(String repositoryName);

	Set getCharacterEncodings();

	// mail settings
	String getEmailFrom();

	String getEmailTo();

	String getEmailHost();

	String getEmailPort();

	String getEmailProject();
}
