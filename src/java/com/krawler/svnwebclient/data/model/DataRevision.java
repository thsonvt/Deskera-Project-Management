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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.krawler.svnwebclient.configuration.ConfigurationProvider;

public class DataRevision {
	public static final int TYPE_ADDED = 0;
	public static final int TYPE_MODIFIED = 1;
	public static final int TYPE_REPLACED = 2;
	public static final int TYPE_DELETED = 3;

	protected long revision;
	protected String author;
	protected Date date;
	protected String comment;
	protected List changedElements = new ArrayList();

	public class ChangedElement {
		protected int type;
		protected String path;
		protected String copyPath;
		protected long copyRevision;

		public ChangedElement(int type, String path, String copyPath,
				long copyRevison) {
			this.type = type;
			this.path = path;
			this.copyPath = copyPath;
			this.copyRevision = copyRevison;
		}

		public String getCopyPath() {
			return this.copyPath;
		}

		public long getCopyRevision() {
			return this.copyRevision;
		}

		public int getType() {
			return this.type;
		}

		public String getPath() {
			return this.path;
		}
	}

	public long getRevision() {
		return this.revision;
	}

	public void setRevision(long revision) {
		this.revision = revision;
	}

	public String getAuthor() {
		return ConfigurationProvider.getInstance().getAuthorDecorator()
				.getAuthorName(this.author);
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Date getDate() {
		return this.date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void addChangedElement(int type, String path, String copyPath,
			long copyRevison) {
		this.changedElements.add(new ChangedElement(type, path, copyPath,
				copyRevison));
	}

	public List getChangedElements() {
		return this.changedElements;
	}
}
