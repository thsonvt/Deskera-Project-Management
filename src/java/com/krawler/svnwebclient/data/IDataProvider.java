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
package com.krawler.svnwebclient.data;

import java.util.List;

import com.krawler.svnwebclient.authorization.UserCredentials;
import com.krawler.svnwebclient.data.model.DataChangedElement;
import com.krawler.svnwebclient.data.model.DataDirectoryElement;
import com.krawler.svnwebclient.data.model.DataFile;
import com.krawler.svnwebclient.data.model.DataFileElement;
import com.krawler.svnwebclient.data.model.DataRepositoryElement;
import com.krawler.svnwebclient.data.model.DataRevision;

public interface IDataProvider {
	int NOT_EXIST = 0;
	int FILE = 1;
	int DIRECTORY = 2;
	int UNKNOWN = 3;

	void connect(UserCredentials credentials, String id, String url)
			throws DataProviderException;

	void close() throws DataProviderException;

	long getHeadRevision() throws DataProviderException;

	DataDirectoryElement getDirectory(String url, long revision)
			throws DataProviderException;

	DataDirectoryElement getDirectory(String url, long revision,
			boolean recusive) throws DataProviderException;

	List getRevisions(String url, long fromRevision, long toRevision, long count)
			throws DataProviderException;

	String getLocation(String url, long pegRevision, long revision)
			throws DataProviderException;

	DataRepositoryElement getInfo(String url, long revision)
			throws DataProviderException;

	DataFileElement getFile(String url, long revision)
			throws DataProviderException;

	DataFile getFileData(String url, long revision)
			throws DataProviderException;

	List getAnnotation(String url, long revision, String encoding)
			throws DataProviderException;

	DataRevision getRevisionInfo(long revision) throws DataProviderException;

	boolean isBinaryFile(String url, long revision)
			throws DataProviderException;

	void testConnection() throws AuthenticationException;

	DataChangedElement createDirectory(String url, String name, String comment)
			throws DataProviderException;

	DataChangedElement addFile(String url, String path, String comment)
			throws DataProviderException;

	DataChangedElement delete(String url, List elements, String comment)
			throws DataProviderException;

	DataChangedElement commitFile(String url, String path, String comment)
			throws DataProviderException;

	List compareDirectoryRevisions(String url, long startRevision,
			long endRevision) throws DataProviderException;

	String getFileDifference(String url, long startRevision, long endRevision,
			String pathToStart, String pathToEnd, String encoding)
			throws DataProviderException;

	int checkUrl(String url, long revision) throws DataProviderException;

	public void setRelativeLocation(String id, String url)
			throws DataProviderException;

	public void doshutdown();
}
