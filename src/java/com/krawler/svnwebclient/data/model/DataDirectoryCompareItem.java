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
package com.krawler.svnwebclient.data.model;

public class DataDirectoryCompareItem {
	public static final int OPERATION_ADD = 0;
	public static final int OPERATION_CHANGE = 1;
	public static final int OPERATION_DELETE = 2;

	protected String path;
	protected int operation;
	protected boolean directory;
	protected long oldRevision;
	protected long newRevision;

	public DataDirectoryCompareItem(String path, int operation) {
		this.path = path;
		this.operation = operation;
	}

	public String getPath() {
		return this.path;
	}

	public int getOperation() {
		return this.operation;
	}

	public void setDirectory(boolean directory) {
		this.directory = directory;
	}

	public boolean isDirectory() {
		return this.directory;
	}

	public void setOldRevision(long oldRevision) {
		this.oldRevision = oldRevision;
	}

	public long getOldRevision() {
		return this.oldRevision;
	}

	public void setNewRevision(long newRevision) {
		this.newRevision = newRevision;
	}

	public long getNewRevision() {
		return this.newRevision;
	}
}
