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

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.diff.SVNDiffWindow;

import com.krawler.svnwebclient.data.model.DataDirectoryCompareItem;

public class Editor implements ISVNEditor {
	public List changedItems = new ArrayList();
	public List stack = new ArrayList();

	public List getChangedItems() {
		return this.changedItems;
	}

	public void targetRevision(long revision) throws SVNException {
	}

	public void openRoot(long revision) throws SVNException {
	}

	public void deleteEntry(String path, long revision) throws SVNException {
		this.changedItems.add(new DataDirectoryCompareItem(path,
				DataDirectoryCompareItem.OPERATION_DELETE));
	}

	public void absentDir(String path) throws SVNException {
	}

	public void absentFile(String path) throws SVNException {
	}

	public void addDir(String path, String copyFromPath, long copyFromRevision)
			throws SVNException {
		DataDirectoryCompareItem item = new DataDirectoryCompareItem(path,
				DataDirectoryCompareItem.OPERATION_ADD);
		item.setOldRevision(-1);
		item.setDirectory(true);
		this.stack.add(0, item);
	}

	public void openDir(String path, long revision) throws SVNException {
		DataDirectoryCompareItem item = new DataDirectoryCompareItem(path,
				DataDirectoryCompareItem.OPERATION_CHANGE);
		item.setOldRevision(revision);
		item.setDirectory(true);
		this.stack.add(0, item);
	}

	public void changeDirProperty(String name, String value)
			throws SVNException {
		if (this.stack.size() > 0) {
			DataDirectoryCompareItem item = (DataDirectoryCompareItem) this.stack
					.get(0);
			if (SVNProperty.COMMITTED_REVISION.equals(name)) {
				item.setNewRevision(Long.parseLong(value));
			}
		}
	}

	public void closeDir() throws SVNException {
		if (this.stack.size() > 0) {
			this.changedItems.add(this.stack.remove(0));
		}
	}

	public void addFile(String path, String copyFromPath, long copyFromRevision)
			throws SVNException {
		DataDirectoryCompareItem item = new DataDirectoryCompareItem(path,
				DataDirectoryCompareItem.OPERATION_ADD);
		item.setOldRevision(-1);
		item.setDirectory(false);
		this.stack.add(0, item);
	}

	public void openFile(String path, long revision) throws SVNException {
		DataDirectoryCompareItem item = new DataDirectoryCompareItem(path,
				DataDirectoryCompareItem.OPERATION_CHANGE);
		item.setOldRevision(revision);
		item.setDirectory(false);
		this.stack.add(0, item);
	}

	public void applyTextDelta(String path, String baseChecksum)
			throws SVNException {
	}

	public OutputStream textDeltaChunk(String path, SVNDiffWindow diffWindow)
			throws SVNException {
		return null;
	}

	public void textDeltaEnd(String path) throws SVNException {
	}

	public void changeFileProperty(String path, String name, String value)
			throws SVNException {
		if (this.stack.size() > 0) {
			DataDirectoryCompareItem item = (DataDirectoryCompareItem) this.stack
					.get(0);
			if (SVNProperty.COMMITTED_REVISION.equals(name)) {
				item.setNewRevision(Long.parseLong(value));
			}
		}
	}

	public void closeFile(String path, String textChecksum) throws SVNException {
		if (this.stack.size() > 0) {
			this.changedItems.add(this.stack.remove(0));
		}
	}

	public SVNCommitInfo closeEdit() throws SVNException {
		return null;
	}

	public void abortEdit() throws SVNException {
	}
}
