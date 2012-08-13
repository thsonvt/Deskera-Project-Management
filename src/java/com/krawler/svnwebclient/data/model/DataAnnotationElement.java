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

import java.util.Date;

import com.krawler.svnwebclient.configuration.ConfigurationProvider;

public class DataAnnotationElement {
	protected long revision;
	protected Date date;
	protected String author;
	protected String line;

	public DataAnnotationElement(long revision, Date date, String author,
			String line) {
		this.revision = revision;
		this.date = date;
		this.author = author;
		this.line = line;
	}

	public long getRevision() {
		return this.revision;
	}

	public Date getDate() {
		return this.date;
	}

	public String getAuthor() {
		return ConfigurationProvider.getInstance().getAuthorDecorator()
				.getAuthorName(this.author);
	}

	public String getLine() {
		return this.line;
	}

	public void setLine(String line) {
		this.line = line;
	}
}
