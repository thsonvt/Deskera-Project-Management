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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.svn.core.internal.wc.SVNFileUtil;

import com.krawler.esp.database.dbcon;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.svnwebclient.SVNWebClientException;
import com.krawler.svnwebclient.data.IDataProvider;
import com.krawler.svnwebclient.data.model.DataFile;
import com.krawler.svnwebclient.util.HighLighter;
import com.krawler.svnwebclient.util.UrlGenerator;
import com.krawler.svnwebclient.util.UrlGeneratorFactory;
import com.krawler.svnwebclient.util.UrlUtil;
import com.krawler.svnwebclient.util.contentencoding.ContentEncodingHelper;
import com.krawler.svnwebclient.web.controller.AbstractBean;
import com.krawler.svnwebclient.web.model.Navigation;
import com.krawler.svnwebclient.web.model.data.file.FileData;
import com.krawler.svnwebclient.web.resource.Links;
import com.krawler.svnwebclient.web.support.AbstractRequestHandler;
import com.krawler.svnwebclient.web.support.RequestHandler;
import com.krawler.svnwebclient.web.support.RequestParameters;
import com.krawler.svnwebclient.web.support.State;

public class FileDataBean extends AbstractBean {

	protected FileData fileData;

	public FileDataBean() {
	}

	public String getFile(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		DataFile file = new DataFile();
		boolean isDisplay = false;
		try {
			String str = request.getParameter("url");
                        String sep = StorageHandler.GetFileSeparator();
			java.util.Hashtable ht = dbcon.getfileinfo(str);
			String doctype = (String) ht.get("doctype");
			/*if (doctype.compareTo("Microsoft Word Document") == 0) {
				com.krawler.esp.fileparser.word.MSWordParser mspares = new com.krawler.esp.fileparser.word.MSWordParser();
				byte buff[] = mspares.getByteArray(StorageHandler
						.GetDocStorePath(ht.get("storeindex").toString())
						+ "/"
						+ (String) ht.get("userid")
						+ "/"
						+ ht.get("svnname"));
				file.setContent(buff);
				isDisplay = true;
			} else if (doctype.compareTo("Microsoft Excel Worksheet") == 0) {
				com.krawler.esp.fileparser.excel.MsExcelParser mspares = new com.krawler.esp.fileparser.excel.MsExcelParser();
				String bytestr = mspares.extractText(StorageHandler
						.GetDocStorePath(ht.get("storeindex").toString())
						+ "/"
						+ (String) ht.get("userid")
						+ "/"
						+ ht.get("svnname"));
				file.setContent(bytestr.getBytes());
				isDisplay = true;
			} else if(doctype.compareTo("PDF File")== 0){
                            com.krawler.esp.fileparser.pdf.KWLPdfParser pdfparser = new  com.krawler.esp.fileparser.pdf.KWLPdfParser();  
                           String  plaintext = new String(pdfparser.getPlaintextpdf(StorageHandler
						.GetDocStorePath(ht.get("storeindex").toString())
						+ "/"
						+ (String) ht.get("userid")
						+ "/"
						+ ht.get("svnname")).getBytes(),"UTF-8");
                                  file.setContent(plaintext.getBytes());              
                                  isDisplay = true;
                        }
                        else*/
            {
				File fp = new File(StorageHandler.GetDocStorePath(ht.get(
						"storeindex").toString())
						+ sep
						+ (String) ht.get("userid")
						+ sep
						+ ht.get("svnname"));
				byte buff[] = new byte[(int) fp.length()];
				FileInputStream fis = new FileInputStream(fp);
				int read = fis.read(buff);
				file.setContent(buff);
			}

			File fp = new File(StorageHandler.GetDocStorePath(ht.get(
					"storeindex").toString())
					+ sep
					+ (String) ht.get("userid")
					+ sep
					+ request.getParameter("url"));
			String mimetype = SVNFileUtil.detectMimeType(fp);
			boolean isexe = SVNFileUtil.isExecutable(fp);
			String mtype = (String) ht.get("doctype");
			// String mimetype =
			// getContentType("",ConfigurationProvider.getInstance().getTempDirectory()
			// +
			// "/56a08913-f7df-4409-93fe-ba5c8f31f6fe/"+request.getParameter("url"),file.getContent());
			if (!isexe) {
				// String submtype =
				// mimetype.substring(0,mimetype.indexOf("/"));

				if ((org.tmatesoft.svn.core.SVNProperty
						.isBinaryMimeType(mimetype) && !isDisplay)
						|| mtype.compareTo("Image") == 0
                        || mtype.compareTo("PDF File") == 0
                        || mtype.compareTo("Microsoft Word Document") == 0
                        || mtype.compareTo("Microsoft Excel Worksheet") == 0) {
					return request.getParameter("url");
				} else {
					String encoding = ContentEncodingHelper
							.getEncoding(new State(request, response));
					String content = new String(file.getContent(),"UTF-8");
					String fileContent = HighLighter.getHighLighter()
							.getColorizedContent(content,
									request.getParameter("url"));
					this.fileData = new FileData(content);
					return "";
				}
			} else {
				return request.getParameter("url");
			}

		} catch (IOException e) {
			// KrawlerLog.op.warn("Unable to Show file Content :"
			// +e.toString());
			return "error";
		}
	}

	public boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		DataFile file = dataProvider.getFileData(this.requestHandler.getUrl(),
				this.requestHandler.getCurrentRevision());

		if (file.isBinary()) {
			// this.redirectToDownload();
			try {

			} catch (Exception e) {

			}

			return false;
		} else {
			try {
				String encoding = ContentEncodingHelper.getEncoding(this.state);
				String content = ContentEncodingHelper.encodeBytes(file
						.getContent(), encoding);
				String fileContent = HighLighter.getHighLighter()
						.getColorizedContent(content,
								this.requestHandler.getUrl());
				this.fileData = new FileData(fileContent);
			} catch (IOException ie) {
				throw new SVNWebClientException(ie);
			}
			return true;
		}
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request);
	}

	protected void redirectToDownload() throws SVNWebClientException {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.FILE_DOWNLOAD, this.requestHandler.getLocation());
		urlGenerator.addParameter(RequestParameters.URL, UrlUtil
				.encode(this.requestHandler.getUrl()));
		urlGenerator.addParameter(RequestParameters.CREV, Long
				.toString(this.requestHandler.getCurrentRevision()));
		try {
			this.state.getResponse().sendRedirect(urlGenerator.getUrl());

		} catch (IOException e) {
			throw new SVNWebClientException(e);
		}
	}

	public FileData getFileData() {
		return this.fileData;
	}

	public Navigation getNavigation() {
		return null;
	}

	public List getActions() {
		return null;
	}
}
