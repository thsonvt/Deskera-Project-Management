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

import com.krawler.svnwebclient.data.model.DataAnnotationElement;
import com.krawler.svnwebclient.util.DateFormatter;
import com.krawler.svnwebclient.util.HtmlUtil;
import com.krawler.svnwebclient.util.NumberFormatter;

public class FileAnnotation {
	protected List annotationElements;

	public class AnnotatedLine {
		protected DataAnnotationElement annotationElement;

		public AnnotatedLine(DataAnnotationElement annotationElement) {
			this.annotationElement = annotationElement;
		}

		public String getDecoratedRevision() {
			String ret = null;
			ret = NumberFormatter.format(this.annotationElement.getRevision());
			return HtmlUtil.encode(ret);
		}

		public String getRevision() {
			return HtmlUtil.encode(Long.toString(this.annotationElement
					.getRevision()));
		}

		public String getDate() {
			return DateFormatter.format(this.annotationElement.getDate());
		}

		public String getAuthor() {
			return HtmlUtil.encode(this.annotationElement.getAuthor());
		}

		public String getLine() throws Exception {
			return this.annotationElement.getLine();
		}
	}

	public FileAnnotation(List annotationElements) {
		this.annotationElements = annotationElements;
	}

	public List getAnnotatedLines() {
		List ret = new ArrayList();
		for (Iterator i = this.annotationElements.iterator(); i.hasNext();) {
			ret.add(new AnnotatedLine((DataAnnotationElement) i.next()));
		}
		return ret;
	}
}
