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
package com.krawler.svnwebclient.data.javasvn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.polarion.svncommons.commentscache.CommentsCache;
import org.polarion.svncommons.commentscache.CommentsCacheException;
import org.polarion.svncommons.commentscache.authentication.SVNAuthenticationManagerFactory;
import org.polarion.svncommons.commentscache.configuration.ProtocolsConfiguration;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

import com.krawler.svnwebclient.authorization.UserCredentials;
import com.krawler.svnwebclient.data.DataProviderException;

public class SVNRepositoryPool {
	private static Map instances = Collections.synchronizedMap(new HashMap());

	protected long size;
	protected String url;
	protected boolean initialized;
	protected String id;
	protected List repositories = new ArrayList();
	protected int activeUsersCount;

	public static synchronized void init(String id, long size, String url,
			String userName, String password) throws DataProviderException {
		SVNRepositoryPool instance = (SVNRepositoryPool) instances.get(id);
		if (instance == null) {
			SVNRepositoryPool pool = null;
			try {
				pool = new SVNRepositoryPool(id, size, url, userName, password);
				pool.activeUsersCount = 1;
				instances.put(id, pool);
			} catch (DataProviderException e) {
				throw new DataProviderException(e);
			}
		} else {
			instance.activeUsersCount++;
		}
	}

	public static synchronized SVNRepositoryPool getInstance(String id)
			throws DataProviderException {
		SVNRepositoryPool pool = (SVNRepositoryPool) instances.get(id);
		if (pool == null) {
			throw new DataProviderException(SVNRepositoryPool.class.getName()
					+ " must be initialized before first usage");
		}
		return pool;
	}

	private SVNRepositoryPool(String id, long size, String url,
			String userName, String password) throws DataProviderException {
		this.id = id;
		this.initialized = true;
		this.url = url;
		this.size = size;

		try {
			for (int i = 0; i < size; i++) {
				SVNRepository repository = SVNRepositoryFactory.create(SVNURL
						.parseURIDecoded(url), CommentsCache.getInstance(id,
						userName, password));
				this.repositories.add(repository);
			}
		} catch (SVNException e) {
			throw new DataProviderException(e);
		} catch (CommentsCacheException e) {
			throw new DataProviderException(e);
		}
	}

	public synchronized void releaseRepository(SVNRepository repository) {
		if (repository != null) {
			this.repositories.add(repository);
			this.notifyAll();
		}
	}

	public void shutdown() {
		this.activeUsersCount--;
		if (this.activeUsersCount == 0) {
			for (Iterator i = this.repositories.iterator(); i.hasNext();) {
				SVNRepository repository = (SVNRepository) i.next();
				try {
					repository.closeSession();
				} catch (Exception ex) {

				}
			}
		}
	}

	public static void terminate() {
		Iterator it = instances.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String id = (String) entry.getKey();
			SVNRepositoryPool pool = (SVNRepositoryPool) entry.getValue();
			for (Iterator i = pool.repositories.iterator(); i.hasNext();) {
				SVNRepository repository = (SVNRepository) i.next();
				try {
					repository.closeSession();
				} catch (Exception ex) {

				}
				instances.remove(id);
			}
		}
	}

	public String getRepositoryName() {
		return this.url;
	}

	public synchronized SVNRepository getRepository(UserCredentials credentials) {
		while (this.repositories.size() == 0) {
			try {
				this.wait();
			} catch (InterruptedException e) {
			}
		}
		SVNRepository ret = (SVNRepository) this.repositories.remove(0);
		ISVNAuthenticationManager authManager = SVNAuthenticationManagerFactory
				.getSVNAuthenticationManager(credentials.getUsername(),
						credentials.getPassword(), ProtocolsConfiguration
								.getInstance());
		ret.setAuthenticationManager(authManager);
		return ret;
	}
}
