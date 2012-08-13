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
package com.krawler.svnwebclient.web.servlet;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.krawler.svnwebclient.SVNWebClientException;
import com.krawler.svnwebclient.data.IDataProvider;
import com.krawler.svnwebclient.data.model.DataFileElement;
import com.krawler.svnwebclient.util.Encoder;
import com.krawler.svnwebclient.web.support.RequestHandler;

public class FileDownloadServlet extends AbstractServlet {
	private static final long serialVersionUID = -5061766135434675105L;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.execute(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.execute(request, response);
	}

	protected void executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		OutputStream outStream = null;
		try {
			long revision = dataProvider.getHeadRevision();
			if (this.requestHandler.getCurrentRevision() != -1) {
				revision = this.requestHandler.getCurrentRevision();
			}
			String url = this.requestHandler.getUrl();
			if (this.requestHandler.getPegRevision() != -1) {
				url = dataProvider.getLocation(this.requestHandler.getUrl(),
						this.requestHandler.getPegRevision(), revision);
			}

			DataFileElement fileElement = dataProvider.getFile(url, revision);
			String fName = com.krawler.esp.handlers.FileHandler
					.getFilename(fileElement.getName());
			fileElement.setName(fName);
			String fileName = fileElement.getName();

			String mimeType = this.getServletContext().getMimeType(
					fileName.toLowerCase());
			if (mimeType == null) {
				mimeType = "application/Octet-stream";
			}
			this.state.getResponse().setContentType(mimeType);

			String filenameAttr;
			String filenameEncoded = Encoder.encodeUTF8ASCII(fileName);
			boolean ecodingNeeded = !fileName.equals(filenameEncoded);

			String userAgent = state.getRequest().getHeader("user-agent");
			boolean IE = (userAgent != null)
					&& (userAgent.indexOf("MSIE") >= 0);

			if (!ecodingNeeded) {
				if (IE) {
					filenameAttr = "filename=" + fileName;
				} else {
					filenameAttr = "filename=\"" + fileName + "\"";
				}
			} else {
				if (!IE) {
					// RFC-2184 (http://www.faqs.org/rfcs/rfc2184.html)
					filenameAttr = "filename*=\"utf-8''" + filenameEncoded
							+ "\"";
				} else {
					// IE does not obey RFC-2184, but this works
					filenameAttr = "filename=" + filenameEncoded;
				}
			}

			String contentDisposition = null;
			if (this.requestHandler.isAttachment()) {
				contentDisposition = "attachment";
			} else {
				contentDisposition = "inline";
			}
			this.state.getResponse().setHeader("Content-Disposition",
					contentDisposition + "; " + filenameAttr);

			outStream = this.state.getResponse().getOutputStream();
			byte[] content = fileElement.getContent();
			outStream.write(content, 0, content.length);
			outStream.flush();
		} catch (SVNWebClientException ex) {
			try {
				this.state.getResponse().sendError(
						HttpServletResponse.SC_NOT_FOUND);
			} catch (Exception e) {
				throw new SVNWebClientException(e);
			}
		} catch (Exception e) {
			throw new SVNWebClientException(e);
		} finally {
			if (outStream != null) {
				try {
					outStream.close();
				} catch (Exception e) {
					Logger.getInstance(FileDownloadServlet.class).error(e, e);
				}
			}
		}
	}

	protected RequestHandler getRequestHandler(HttpServletRequest request) {
		return new RequestHandler(request);
	}
}
