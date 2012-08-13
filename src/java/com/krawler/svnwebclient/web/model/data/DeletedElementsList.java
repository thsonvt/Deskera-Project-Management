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

import java.util.List;

import com.krawler.svnwebclient.util.CommentUtil;
import com.krawler.svnwebclient.util.HtmlUtil;
import com.krawler.svnwebclient.util.NumberFormatter;

public class DeletedElementsList {
	protected List deletedElements;

	public static class Element {
		protected String type;
		protected String name;
		protected String revision;
		protected String size;
		protected String date;
		protected String author;
		protected String comment;

		public String getType() {
			return this.type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getName() {
			return HtmlUtil.encode(this.name);
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getDecoratedRevision() {
			String ret = null;
			ret = NumberFormatter.format(Long.parseLong(this.revision));
			return HtmlUtil.encode(ret);
		}

		public String getRevision() {
			return HtmlUtil.encode(this.revision);
		}

		public void setRevision(String revision) {
			this.revision = revision;
		}

		public String getSize() {
			return this.size;
		}

		public void setSize(String size) {
			this.size = size;
		}

		public String getDate() {
			return this.date;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public String getAuthor() {
			return HtmlUtil.encode(this.author);
		}

		public void setAuthor(String author) {
			this.author = author;
		}

		public String getFirstLine() {
			return CommentUtil.getFirstLine(this.comment);
		}

		public boolean isMultiLineComment() {
			return CommentUtil.isMultiLine(this.comment);
		}

		public String getComment() {
			return HtmlUtil.encode(this.comment);
		}

		public String getTooltip() {
			return CommentUtil.getTooltip(this.comment);
		}

		public void setComment(String comment) {
			this.comment = comment;
		}
	}

	public List getDeletedElements() {
		return this.deletedElements;
	}

	public void setDeletedElements(List elements) {
		this.deletedElements = elements;
	}
}
