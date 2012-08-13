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
package com.krawler.svnwebclient.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.log4j.Logger;

import com.krawler.common.session.SessionExpiredException;
import com.krawler.esp.handlers.AuthHandler;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.svnwebclient.configuration.ConfigurationException;
import com.krawler.svnwebclient.web.support.FormParameters;

public class Uploader {

	protected Map parameters = new HashMap();
	protected boolean isUploaded;
	protected String errorMessage;
	private boolean flagType;

	public void doPost(HttpServletRequest request,
			HttpServletResponse responce, String destinationDirectory,
			String tempDirectory) throws SessionExpiredException {
		File tempDir = new File(tempDirectory);
                String sep = StorageHandler.GetFileSeparator();
		if (!tempDir.exists()) {
			tempDir.mkdirs();
		}
		DiskFileUpload fu = new DiskFileUpload();
		// maximum size before a FileUploadException will be thrown
		fu.setSizeMax(-1);
		// maximum size that will be stored in memory
		fu.setSizeThreshold(4096);
		// the location for saving data that is larger than getSizeThreshold()
		fu.setRepositoryPath(tempDirectory);
		List fileItems = null;
		try {
			fileItems = fu.parseRequest(request);
		} catch (FileUploadException e) {
			Logger.getInstance(Uploader.class).error(e, e);
		}
		String docid1 = null;
		String docownerid = null;
		for (Iterator k = fileItems.iterator(); k.hasNext();) {
			FileItem fi1 = (FileItem) k.next();
			try {
				if (fi1.getFieldName().toString().equals("docid")) {
					docid1 = new String(fi1.getString().getBytes(), "UTF8");
				}
				if (fi1.getFieldName().toString().equals("docownerid")) {
					docownerid = new String(fi1.getString().getBytes(), "UTF8");
				}
			} catch (UnsupportedEncodingException e) {
				// Logger.getInstance(Uploader.class).error(e, e);
			}
		}
		if (docid1.equals("")) {
			docid1 = UUID.randomUUID().toString();
			this.setFlagType(true);
			docownerid = AuthHandler.getUserid(request);
		} else {
			this.setFlagType(false);
		}
		try {
			if (docownerid.equalsIgnoreCase("my")) {
				docownerid = AuthHandler.getUserid(request);
			}
			destinationDirectory = com.krawler.esp.handlers.StorageHandler
					.GetDocStorePath()
					+ sep + docownerid;
		} catch (ConfigurationException ex) {
			this.isUploaded = false;
			this.errorMessage = "Problem occurred while uploading file";
			return;
		}
		File destDir = new File(destinationDirectory);
		if (!destDir.exists()) {
			destDir.mkdirs();
		}
		this.parameters.put("destinationDirectory", destinationDirectory);
		for (Iterator i = fileItems.iterator(); i.hasNext();) {
			FileItem fi = (FileItem) i.next();

			/*
			 * try{ String docid1 = fi.getString("docid"); }catch(Exception e){}
			 */
			if (fi.isFormField()) {
				try {
					if (fi.getFieldName().toString().equals("docid")
							&& new String(fi.getString().getBytes(), "UTF8")
									.equals("")) {
						this.parameters.put(fi.getFieldName(), docid1);
					} else {
						this.parameters.put(fi.getFieldName(), new String(fi
								.getString().getBytes(), "UTF8"));
					}
				} catch (UnsupportedEncodingException e) {
					Logger.getInstance(Uploader.class).error(e, e);
				}
			} else {
				// filename on the client
				String fileName = null;

				try {
					fileName = new String(fi.getName().getBytes(), "UTF8");
					// org.tmatesoft.svn.core.internal.wc.SVNFileUtil.isExecutable(fi);
					String filext = "";
					if (fileName.contains("."))
						filext = fileName.substring(fileName.lastIndexOf("."));

					if (fi.getSize() != 0) {
						this.isUploaded = true;

						// write the file
						File uploadFile = new File(destinationDirectory + sep
								+ FileUtil.getLastPathElement(docid1 + filext));
						fi.write(uploadFile);
						this.parameters.put("svnName", uploadFile.getName());
						if (filext.equals("")) {
							this.parameters.put("fileExt", filext);
						} else {
							this.parameters.put("fileExt", filext.substring(1));
						}
					} else {
						this.isUploaded = false;
						this.errorMessage = "Cannot upload a 0 byte file";
					}
				} catch (Exception e) {
					this.isUploaded = false;
					this.errorMessage = "Problem occurred while uploading file";
					Logger.getInstance(Uploader.class).error(e, e);
				}
				this.parameters.put(FormParameters.FILE_NAME, FileUtil
						.getLastPathElement(fileName));
			}
		}
	}

	public boolean isUploaded() {
		return isUploaded;
	}

	public String errorMessage() {
		return errorMessage;
	}

	public Map getParameters() {
		return parameters;
	}

	public boolean isFlagType() {
		return flagType;
	}

	public void setFlagType(boolean flagType) {
		this.flagType = flagType;
	}
}
