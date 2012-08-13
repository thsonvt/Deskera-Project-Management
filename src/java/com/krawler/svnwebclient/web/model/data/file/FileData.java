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
package com.krawler.svnwebclient.web.model.data.file;

import java.util.ArrayList;
import java.util.List;

public class FileData {

	protected String fileContent;

	public FileData(String fileContent) {
		this.fileContent = fileContent;
	}

	public class Line {
		protected long number;
		protected String data;

		public Line(long number, String data) {
			this.number = number;
			this.data = data;
		}

		public String getNumber() {
			return Long.toString(this.number);
		}

		public String getData() throws Exception {
			return this.data;
		}
	}

	public List getLines() {
		List ret = new ArrayList();
		String content = new String(this.fileContent);
		String lines[] = content.split("\\r\\n|\\r|\\n");
		for (int i = 0; i < lines.length; i++) {
			ret.add(new Line(i + 1, lines[i]));
		}
		return ret;
	}
}
