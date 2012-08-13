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
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.krawler.svnwebclient.SVNWebClientException;
import com.krawler.svnwebclient.configuration.ConfigurationProvider;
import com.krawler.svnwebclient.data.DataProviderException;
import com.krawler.svnwebclient.data.IDataProvider;
import com.krawler.svnwebclient.data.model.DataDirectoryElement;
import com.krawler.svnwebclient.data.model.DataFileElement;
import com.krawler.svnwebclient.data.model.DataRepositoryElement;
import com.krawler.svnwebclient.util.contentencoding.ContentEncodingHelper;
import com.krawler.svnwebclient.web.support.RequestHandler;

public class ArchiveDownloadServlet extends AbstractServlet {

	private static final long serialVersionUID = 3025332777443975638L;

	protected String url;
	protected long revision;

	protected RequestHandler getRequestHandler(HttpServletRequest request) {
		return new RequestHandler(request);
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.execute(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);
	}

	protected void executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		ZipOutputStream zos = null;
		try {
			String encoding = ConfigurationProvider.getInstance()
					.getZipEncoding();
			try {
				String tmp = "";
				tmp.getBytes(encoding);
			} catch (UnsupportedEncodingException ue) {
				Logger.getLogger(this.getClass()).error(ue, ue);
				encoding = "ISO-8859-1";
			}

			DataRepositoryElement info = this
					.getRepositiryElementInfo(dataProvider);

			HttpServletResponse response = this.state.getResponse();
			OutputStream out = response.getOutputStream();

			zos = new ZipOutputStream(out);
			zos.setMethod(ZipOutputStream.DEFLATED);

			String mimeType = "application/octet-stream";
			response.setContentType(mimeType);

			String fileName = info.getName();
			fileName = fileName + "-r" + Long.toString(info.getRevision())
					+ ".zip";

			String defaultEncoding = ContentEncodingHelper
					.getEncoding(this.state);
			fileName = new String(fileName.getBytes(defaultEncoding),
					"ISO-8859-1");

			if (this.state.getRequest().getHeader("User-Agent") != null) {
				if (this.state.getRequest().getHeader("User-Agent").indexOf(
						"MSIE") < 0) {
					fileName = "\"" + fileName + "\"";
				}
			}
			response.setHeader("Content-Disposition", "attachment; filename="
					+ fileName);

			DataDirectoryElement dir = dataProvider.getDirectory(this.url,
					this.revision, false);
			this.processFolder(dataProvider, zos, dir, "");

			zos.flush();
		} catch (Exception ie) {
			throw new SVNWebClientException(ie);
		} finally {
			if (zos != null) {
				try {
					zos.close();
				} catch (Exception e) {
				}
			}
		}
	}

	protected void processFolder(IDataProvider dataProvider,
			ZipOutputStream zip, DataDirectoryElement dir, String path)
			throws SVNWebClientException, IOException {
		if (dir.isRestricted() || dir.getDate() == null) {
			// try to access the directory in case we are logged in.
			try {
				dir = dataProvider.getDirectory(url + path, dir.getRevision());
			} catch (Exception ex) {
				// We probably have insufficient rights to view this directory.
				// System.out.println("Restricted dir: /skip " + url + path + "
				// " + ex.getMessage());
				return;
			}
		} else {
			// normal directory
			// if you load all children, the next line is not needed. However,
			// that causes trouble with restricted folders
			try {
				dir = dataProvider.getDirectory(url + path, dir.getRevision());
			} catch (DataProviderException e) {
				dir = dataProvider.getDirectory(url + path, -1);
			}
		}

		List children = dir.getChildElements();
		if (children == null || children.size() == 0) {
			// empty folder
			if (path.length() == 0) {
				return;
			} else {
				ZipEntry ze = new ZipEntry(path);
				ze.setComment("Empty directory");
				zip.putNextEntry(ze);
				// zip.closeEntry();
				return;
			}
		} else {
			Iterator it = children.iterator();
			while (it.hasNext()) {
				// write files first
				DataRepositoryElement el = (DataRepositoryElement) it.next();
				if (el.isDirectory()) {
					// add folder
					String np = path + el.getName() + "/";
					this.processFolder(dataProvider, zip,
							((DataDirectoryElement) el), np);
				} else {
					// export file
					DataFileElement dfe = (DataFileElement) el;
					this.processFile(dataProvider, zip, dfe, path);
				}
			}
		}
	}

	protected void processFile(IDataProvider dataProvider, ZipOutputStream zip,
			DataFileElement file, String path) throws SVNWebClientException,
			IOException {
		String filePath = null;
		if (url == null || url.length() == 0) {
			filePath = url + path + file.getName();
		} else {
			filePath = url + path + file.getName();
		}

		try {
			file = dataProvider.getFile(filePath, file.getRevision());
		} catch (DataProviderException de) {
			file = dataProvider.getFile(filePath, -1);
		}
		// System.out.println("file entry: " + filePath);
		// add file
		String entryName = null;
		if ("".equals(path)) {
			entryName = file.getName();
		} else {
			entryName = path + file.getName();
		}

		ZipEntry ze = new ZipEntry(entryName);
		byte[] data = file.getContent();

		zip.putNextEntry(ze);
		zip.write(data, 0, data.length);
	}

	protected DataRepositoryElement getRepositiryElementInfo(
			IDataProvider dataProvider) throws SVNWebClientException {
		if (this.requestHandler.getCurrentRevision() != -1) {
			this.revision = this.requestHandler.getCurrentRevision();
		} else {
			this.revision = dataProvider.getHeadRevision();
		}
		String strUrl = this.requestHandler.getUrl();
		if (strUrl.length() > 0 && !strUrl.endsWith("/"))
			strUrl = strUrl + '/';

		if (this.requestHandler.getPegRevision() != -1) {
			strUrl = dataProvider.getLocation(this.requestHandler.getUrl(),
					this.requestHandler.getPegRevision(), this.revision);
		}

		this.url = strUrl;
		DataRepositoryElement info = dataProvider.getInfo(this.url, revision);
		return info;
	}
}
