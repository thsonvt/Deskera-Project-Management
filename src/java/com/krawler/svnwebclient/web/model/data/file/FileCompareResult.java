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
import java.util.Iterator;
import java.util.List;

import com.krawler.svnwebclient.web.resource.Images;
import com.krawler.svnwebclient.web.support.DifferenceLine;

public class FileCompareResult {
	protected List diffLines;

	public class Line {
		protected DifferenceLine data;

		public Line(DifferenceLine data) {
			this.data = data;
		}

		public int getChangeType() {
			return this.data.getType();
		}

		public String getImage() {
			if (this.data.getType() == DifferenceLine.ADDED) {
				return Images.ADDED;
			} else if (this.data.getType() == DifferenceLine.DELETED) {
				return Images.DELETED;
			} else if (this.data.getType() == DifferenceLine.MODIFIED) {
				return Images.MODIFIED;
			} else {
				return Images.EMPTY;
			}
		}

		public String getNumber() {
			if (this.data.getNumber() == DifferenceLine.EMPTY_NUMBER) {
				return "&nbsp;";
			} else {
				return Integer.toString(this.data.getNumber() + 1);
			}
		}

		public String getLine() throws Exception {
			return this.data.getLine();
		}

		public String getBackground() {
			if (this.data.getType() == DifferenceLine.ADDED) {
				return "#E0FFE0";
			} else if (this.data.getType() == DifferenceLine.DELETED) {
				return "#FFE3E3";
			} else if (this.data.getType() == DifferenceLine.MODIFIED) {
				return "#FEFFB2";
			} else {
				return "#FFFFFF";
			}
		}
	}

	public FileCompareResult(List diffLines) {
		this.diffLines = diffLines;
	}

	public List getLines() {
		List ret = new ArrayList();
		for (Iterator i = this.diffLines.iterator(); i.hasNext();) {
			ret.add(new Line((DifferenceLine) i.next()));
		}
		return ret;
	}
}
