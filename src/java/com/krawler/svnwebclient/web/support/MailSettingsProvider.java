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

import org.apache.log4j.Logger;

public class MailSettingsProvider {

	protected String emailFrom;
	protected String emailTo;
	protected String emailHost;
	protected String emailPort;
	protected String emailProject;

	protected boolean isCorrectlyInitialized;

	protected static MailSettingsProvider instance;

	protected MailSettingsProvider(String emailFrom, String emailTo,
			String emailHost, String emailPort, String emailProject) {
		super();
		this.emailFrom = emailFrom;
		this.emailTo = emailTo;
		this.emailHost = emailHost;
		this.emailPort = emailPort;
		this.emailProject = emailProject;

		if (this.emailFrom != null && this.emailTo != null
				&& this.emailPort != null && this.emailHost != null
				&& this.emailProject != null) {
			this.isCorrectlyInitialized = true;
		} else {
			this.isCorrectlyInitialized = false;
			Logger log = Logger.getLogger(MailSettingsProvider.class);
			log.info("Email setings isn't correctly initialized. "
					+ "EmailFrom: " + this.emailFrom + ", emailTo: " + emailTo
					+ ", emailHost: " + emailHost + "emailPort: " + emailPort
					+ "emailProject: " + emailProject);
		}
	}

	public static MailSettingsProvider getInstance() {
		return instance;
	}

	public static void init(String emailFrom, String emailTo, String emailHost,
			String emailPort, String emailProject) {
		instance = new MailSettingsProvider(emailFrom, emailTo, emailHost,
				emailPort, emailProject);
	}

	public String getEmailFrom() {
		return emailFrom;
	}

	public String getHost() {
		return emailHost;
	}

	public String getPort() {
		return emailPort;
	}

	public String getProjectName() {
		return emailProject;
	}

	public String getEmailTo() {
		return emailTo;
	}

	public boolean isCorrectlyInitialized() {
		return this.isCorrectlyInitialized;
	}

}
