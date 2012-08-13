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
package com.krawler.svnwebclient.web.model.data;

import java.util.Date;
import java.util.List;

import com.krawler.svnwebclient.util.CommentUtil;
import com.krawler.svnwebclient.util.DateFormatter;
import com.krawler.svnwebclient.util.HtmlUtil;
import com.krawler.svnwebclient.util.NumberFormatter;
import com.krawler.svnwebclient.web.model.Navigation;
import com.krawler.svnwebclient.web.resource.Images;

public class ChangeResult {
	protected String message;
	protected List elements;
	protected Navigation navigation;
	protected boolean successful;

	public static class Element {
		protected String name;
		protected String author;
		protected Date date;
		protected long revision;
		protected String comment;
		protected boolean directory;
		protected long size;

		public void setDirectory(boolean directory) {
			this.directory = directory;
		}

		public String getImage() {
			if (this.directory) {
				return Images.DIRECTORY;
			} else {
				return Images.FILE;
			}
		}

		public boolean isDirectory() {
			return this.directory;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return HtmlUtil.encode(this.name);
		}

		public void setAuthor(String author) {
			this.author = author;
		}

		public String getAuthor() {
			return HtmlUtil.encode(this.author);
		}

		public void setDate(Date date) {
			this.date = date;
		}

		public String getDate() {
			return DateFormatter.format(this.date);
		}

		public void setRevision(long revision) {
			this.revision = revision;
		}

		public String getDecoratedRevision() {
			String ret = null;
			ret = NumberFormatter.format(this.revision);
			return HtmlUtil.encode(ret);
		}

		public String getRevision() {
			return HtmlUtil.encode(Long.toString(this.revision));
		}

		public void setComment(String comment) {
			this.comment = comment;
		}

		public String getComment() {
			return HtmlUtil.encode(this.comment);
		}

		public String getFirstLine() {
			return CommentUtil.getFirstLine(this.comment);
		}

		public boolean isMultiLineComment() {
			return CommentUtil.isMultiLine(this.comment);
		}

		public String getTooltip() {
			return CommentUtil.getTooltip(this.comment);
		}

		public void setSize(long size) {
			this.size = size;
		}

		public String getSize() {
			String ret;
			if (this.directory) {
				ret = "<DIR>";
			} else {
				ret = NumberFormatter.format(this.size);
			}
			return HtmlUtil.encode(ret);
		}
	}

	public ChangeResult(boolean successful, String message, List elements,
			Navigation navigation) {
		this.successful = successful;
		this.message = message;
		this.elements = elements;
		this.navigation = navigation;
	}

	public boolean isSuccessful() {
		return this.successful;
	}

	public String getMessage() {
		return this.message;
	}

	public List getElements() {
		return this.elements;
	}

	public Navigation getNavigation() {
		return this.navigation;
	}
}
