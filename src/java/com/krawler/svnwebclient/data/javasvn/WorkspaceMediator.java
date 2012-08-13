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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.ISVNWorkspaceMediator;

public class WorkspaceMediator implements ISVNWorkspaceMediator {
	private Map myTmpFiles = new HashMap();

	public String getWorkspaceProperty(String path, String name)
			throws SVNException {
		return null;
	}

	public void setWorkspaceProperty(String path, String name, String value)
			throws SVNException {
	}

	public OutputStream createTemporaryLocation(String path, Object id)
			throws SVNException {
		ByteArrayOutputStream tempStorageOS = new ByteArrayOutputStream();
		myTmpFiles.put(id, tempStorageOS);
		return tempStorageOS;
	}

	public InputStream getTemporaryLocation(Object id) throws SVNException {
		return new ByteArrayInputStream(((ByteArrayOutputStream) myTmpFiles
				.get(id)).toByteArray());
	}

	public long getLength(Object id) throws SVNException {
		ByteArrayOutputStream tempStorageOS = (ByteArrayOutputStream) myTmpFiles
				.get(id);
		if (tempStorageOS != null) {
			return tempStorageOS.size();
		}
		return 0;
	}

	public void deleteTemporaryLocation(Object id) {
		myTmpFiles.remove(id);
	}
}
