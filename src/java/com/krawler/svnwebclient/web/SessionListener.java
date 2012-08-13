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

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;

import com.krawler.svnwebclient.configuration.ConfigurationProvider;

public class SessionListener implements HttpSessionListener {

	public void sessionCreated(HttpSessionEvent arg0) {
	}

	public void sessionDestroyed(HttpSessionEvent arg0) {
		if (!ConfigurationProvider.getInstance().isMultiRepositoryMode()) {
			return;
		}
		HttpSession session = arg0.getSession();
		String id = (String) session.getAttribute(SystemInitializing.ID);
		if (id != null) {
			try {
				com.krawler.svnwebclient.data.javasvn.SVNRepositoryPool
						.getInstance(id).shutdown();
				org.polarion.svncommons.commentscache.SVNRepositoryPool
						.getInstance(id).shutdown();
			} catch (Exception e) {
				Logger.getLogger(this.getClass()).error(e, e);
				e.printStackTrace();
			}
		}
	}
}
