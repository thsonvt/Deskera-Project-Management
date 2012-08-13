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
package com.krawler.svnwebclient.web;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.polarion.svncommons.commentscache.configuration.ProtocolsConfiguration;

import com.krawler.svnwebclient.configuration.ConfigurationProvider;
import com.krawler.svnwebclient.configuration.WebConfigurationProvider;
import com.krawler.svnwebclient.data.DataProviderFactory;
import com.krawler.svnwebclient.data.javasvn.DataProvider;
import com.krawler.svnwebclient.web.support.MailSettingsProvider;

public class InitListener implements ServletContextListener {
	public void contextInitialized(ServletContextEvent arg0) {
		boolean isMultirepository = false;
		Map parameters = new HashMap();
		Enumeration parameterNames = arg0.getServletContext()
				.getInitParameterNames();
		while (parameterNames.hasMoreElements()) {
			String parameterName = (String) parameterNames.nextElement();
			String parameterValue = arg0.getServletContext().getInitParameter(
					parameterName);
			parameters.put(parameterName, parameterValue);
			if (WebConfigurationProvider.PARENT_REPOSITORY_DIRECTORY
					.equals(parameterName)) {
				isMultirepository = true;
			}
		}
		WebConfigurationProvider.getInstance().setParameters(parameters);
		try {
			ConfigurationProvider.getInstance().checkConfiguration();

			// protocols settings
			ProtocolsConfiguration protocolsConf = ProtocolsConfiguration
					.getInstance();
			protocolsConf.setProtocolKeyFile(ConfigurationProvider
					.getInstance().getProtocolKeyFile());
			protocolsConf.setProtocolPassPhrase(ConfigurationProvider
					.getInstance().getProtocolPassPhrase());
			protocolsConf.setProtocolPortNumber(ConfigurationProvider
					.getInstance().getProtocolPortNumber());
			protocolsConf.setProtocolType(ConfigurationProvider.getInstance()
					.getProtocolType());
			protocolsConf.setProxy(ConfigurationProvider.getInstance()
					.getProxy());

			// mail settings
			ConfigurationProvider confProvider = ConfigurationProvider
					.getInstance();
			MailSettingsProvider.init(confProvider.getEmailFrom(), confProvider
					.getEmailTo(), confProvider.getEmailHost(), confProvider
					.getEmailPort(), confProvider.getEmailProject());
		} catch (Exception ce) {
			ConfigurationProvider.getInstance().getConfigurationError()
					.setError(true);
			ConfigurationProvider.getInstance().getConfigurationError()
					.setException(ce);
			Logger.getLogger(this.getClass()).error(ce, ce);
			return;
		}

		if (!isMultirepository) {
			ConfigurationProvider provider = ConfigurationProvider
					.getInstance();
			try {
				String id = DataProvider.getID(provider.getRepositoryUrl(),
						provider.getUsername(), provider.getPassword());
				DataProviderFactory.startup(provider.getUsername(), provider
						.getPassword(), id, provider.getRepositoryUrl());
			} catch (Exception e) {
				ConfigurationProvider.getInstance().getConfigurationError()
						.setError(true);
				ConfigurationProvider.getInstance().getConfigurationError()
						.setException(e);
				Logger.getLogger(this.getClass()).error(e, e);
			}
		}
	}

	public void contextDestroyed(ServletContextEvent arg0) {
		try {
			DataProviderFactory.shutdown();
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error(e);
		}
	}
}
