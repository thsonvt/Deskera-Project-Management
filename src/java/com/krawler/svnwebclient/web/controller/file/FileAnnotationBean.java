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
package com.krawler.svnwebclient.web.controller.file;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.krawler.svnwebclient.SVNWebClientException;
import com.krawler.svnwebclient.data.IDataProvider;
import com.krawler.svnwebclient.data.model.DataAnnotationElement;
import com.krawler.svnwebclient.util.HighLighter;
import com.krawler.svnwebclient.util.contentencoding.ContentEncodingHelper;
import com.krawler.svnwebclient.web.controller.AbstractBean;
import com.krawler.svnwebclient.web.model.Navigation;
import com.krawler.svnwebclient.web.model.data.file.FileAnnotation;
import com.krawler.svnwebclient.web.support.AbstractRequestHandler;
import com.krawler.svnwebclient.web.support.RequestHandler;

public class FileAnnotationBean extends AbstractBean {
	protected List annotation;
	protected boolean binary;

	public FileAnnotationBean() {
	}

	protected boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		this.binary = dataProvider.isBinaryFile(this.requestHandler.getUrl(),
				this.requestHandler.getCurrentRevision());
		if (!this.binary) {
			String encoding = ContentEncodingHelper.getEncoding(this.state);

			this.annotation = dataProvider.getAnnotation(this.requestHandler
					.getUrl(), this.requestHandler.getCurrentRevision(),
					encoding);
			Iterator it = this.annotation.iterator();
			StringBuffer mergedContent = new StringBuffer();
			String colorizedContent = null;
			while (it.hasNext()) {
				DataAnnotationElement element = (DataAnnotationElement) it
						.next();
				String line = element.getLine();
				mergedContent.append(line).append("\n");
			}
			try {
				colorizedContent = HighLighter.getHighLighter()
						.getColorizedContent(mergedContent.toString(),
								this.requestHandler.getUrl());
				String lines[] = colorizedContent.split("\\r\\n|\\r|\\n");
				for (int i = 0; i < lines.length; i++) {
					((DataAnnotationElement) this.annotation.get(i))
							.setLine(lines[i]);
				}

			} catch (IOException ie) {
				throw new SVNWebClientException(ie);
			}
		}
		return true;
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request);
	}

	public boolean isBinaryFile() {
		return this.binary;
	}

	public FileAnnotation getFileAnnotation() {
		return new FileAnnotation(this.annotation);
	}

	public Navigation getNavigation() {
		return null;
	}

	public List getActions() {
		return null;
	}
}
