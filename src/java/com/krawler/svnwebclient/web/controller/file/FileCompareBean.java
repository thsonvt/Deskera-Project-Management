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
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.w3c.util.UUID;

import com.krawler.svnwebclient.SVNWebClientException;
import com.krawler.svnwebclient.configuration.ConfigurationProvider;
import com.krawler.svnwebclient.data.DataProviderException;
import com.krawler.svnwebclient.data.IDataProvider;
import com.krawler.svnwebclient.data.model.DataFile;
import com.krawler.svnwebclient.data.model.DataRevision;
import com.krawler.svnwebclient.util.FileUtil;
import com.krawler.svnwebclient.util.UrlGenerator;
import com.krawler.svnwebclient.util.UrlGeneratorFactory;
import com.krawler.svnwebclient.util.UrlUtil;
import com.krawler.svnwebclient.util.contentencoding.ContentEncodingHelper;
import com.krawler.svnwebclient.web.controller.AbstractBean;
import com.krawler.svnwebclient.web.model.Navigation;
import com.krawler.svnwebclient.web.model.data.RevisionDetails;
import com.krawler.svnwebclient.web.model.data.file.FileCompareInfo;
import com.krawler.svnwebclient.web.model.data.file.FileCompareResult;
import com.krawler.svnwebclient.web.resource.Links;
import com.krawler.svnwebclient.web.support.AbstractRequestHandler;
import com.krawler.svnwebclient.web.support.DifferenceLine;
import com.krawler.svnwebclient.web.support.DifferenceModel;
import com.krawler.svnwebclient.web.support.RequestHandler;
import com.krawler.svnwebclient.web.support.RequestParameters;

public class FileCompareBean extends AbstractBean {
	public static final String START_REVISION_CONTENT = "startrevision";
	public static final String END_REVISION_CONTENT = "endrevision";
	public static final String START_REVISION_BINARY = "startrevisionbinary";
	public static final String END_REVISION_BINARY = "endrevisionbinary";
	public static final String PARAM_EXTENSION = "extension";

	public static int ADD = 0;
	public static int DEL = 1;
	public static int MOD = 2;

	protected long headRevision;
	protected long revision;
	protected String url;
	protected DataRevision startRevision;
	protected DataRevision endRevision;
	protected String extension;
	byte[] startByte;
	byte[] endByte;
	String flag = "-1";

	protected boolean executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		flag = this.state.getRequest().getParameter("flag");
		this.headRevision = dataProvider.getHeadRevision();
		com.krawler.esp.fileparser.word.MSWordParser mspares = new com.krawler.esp.fileparser.word.MSWordParser();
		this.setExtension();

		if (this.requestHandler.getPegRevision() != -1) {
			this.revision = this.requestHandler.getPegRevision();
		} else if (this.requestHandler.getCurrentRevision() != -1) {
			this.revision = this.requestHandler.getCurrentRevision();
		} else {
			this.revision = this.headRevision;
		}

		String tempDirectoryPath = ConfigurationProvider.getInstance()
				.getTempDirectory()
				+ "/" + UUID.getUUID();
		try {
			File tempDirectory = new File(tempDirectoryPath);
			if (!tempDirectory.exists()) {
				tempDirectory.mkdirs();
			}

			DataFile startRevisionData = null;
			DataFile endRevisionData = null;
			boolean isBinary = false;

			if (this.requestHandler.getStartRevision() != -1) {
				this.startRevision = dataProvider
						.getRevisionInfo(this.requestHandler.getStartRevision());
				String startRevisionLocation = this.requestHandler.getUrl();
				if (this.requestHandler.getEndRevision() != -1) {
					// if file was deleted we could'n know its location, because
					// it doesn't present in current revision
					// so in this case we think that location is correct
					startRevisionLocation = dataProvider.getLocation(
							this.requestHandler.getUrl(), this.revision,
							this.requestHandler.getStartRevision());
				}
				startRevisionData = dataProvider.getFileData(
						startRevisionLocation, this.requestHandler
								.getStartRevision());
				isBinary = startRevisionData.isBinary();
			}

			if (this.requestHandler.getEndRevision() != -1) {
				this.endRevision = dataProvider
						.getRevisionInfo(this.requestHandler.getEndRevision());
				// if (!isBinary)
				{
					String endRevisionLocation = dataProvider.getLocation(
							this.requestHandler.getUrl(), this.revision,
							this.requestHandler.getEndRevision());
					endRevisionData = dataProvider.getFileData(
							endRevisionLocation, this.requestHandler
									.getEndRevision());
					isBinary = endRevisionData.isBinary();
				}
			}
			// encoding
    			//String encoding = ContentEncodingHelper.getEncoding(this.state);
                        String encoding = "UTF-8";
                        String fileType = this.state.getRequest().getParameter("fileType");
			/*if (!isBinary )*/ {
				if ((startRevisionData != null) && (endRevisionData != null)) {
                                       	String startPath = tempDirectoryPath + "/" + UUID.getUUID();
					String endPath = tempDirectoryPath + "/" + UUID.getUUID();
                                        File startFile = new File(startPath);
                                        File endFile = new File(endPath);
                                        this.writeFile(startPath, startRevisionData.getContent());
                                        this.writeFile(endPath, endRevisionData.getContent());
					if(fileType.compareTo("PDF File")==0){
                                            com.krawler.esp.fileparser.pdf.KWLPdfParser pdfparser = new  com.krawler.esp.fileparser.pdf.KWLPdfParser();  
                                            startByte =pdfparser.getPlaintextpdf(startPath).getBytes("UTF-8");
                                            endByte = pdfparser.getPlaintextpdf(endPath).getBytes("UTF-8");
                                            
                                        }else if(fileType.compareTo("Microsoft Word Document")==0){
                                            startByte = mspares.getByteArray(startPath);
                                            endByte = mspares.getByteArray(endPath);
                                            
                                        }else{
                                            startByte = startRevisionData.getContent();
                                            endByte = endRevisionData.getContent();
                                        }
                                        this.writeFile(startPath,startByte);
                                        this.writeFile(endPath, endByte);
					String difference = dataProvider.getFileDifference(
							this.requestHandler.getUrl(), this.requestHandler
									.getStartRevision(), this.requestHandler
									.getEndRevision(), startPath, endPath,
							encoding);
					DifferenceModel model = new DifferenceModel(difference);

					String startRevisionContent = new String(startByte);
					String endRevisionContent = new String(endByte);

					this.state.getRequest().getSession().setAttribute(
							FileCompareBean.START_REVISION_CONTENT,
							model.getLeftLines(startRevisionContent));
					this.state.getRequest().getSession().setAttribute(
							FileCompareBean.END_REVISION_CONTENT,
							model.getRightLines(endRevisionContent));
                                        startFile.delete();
                                        endFile.delete();
				} else {
					if (startRevisionData == null) {
						String endRevisionContent = new String(endByte, encoding);
						this.state.getRequest().getSession().setAttribute(
								FileCompareBean.END_REVISION_CONTENT,
								DifferenceModel
										.getUntouchedLines(endRevisionContent));
					} else if (endRevisionData == null) {
                                                String startPath = tempDirectoryPath + "/" + UUID.getUUID();
                                                File startFile = new File(startPath);
                                                this.writeFile(startPath, startRevisionData.getContent());
                                                if(fileType.compareTo("PDF File")==0||fileType.compareTo("application/pdf")==0){
                                                     com.krawler.esp.fileparser.pdf.KWLPdfParser pdfparser = new  com.krawler.esp.fileparser.pdf.KWLPdfParser();  
                                                     startByte =pdfparser.getPlaintextpdf(startPath).getBytes("UTF-8");
                                                }else if(fileType.compareTo("Microsoft Word Document")==0||fileType.compareTo("application/msword")==0){
                                                    startByte = mspares.getByteArray(startPath);
                                                }else{
                                                    startByte = startRevisionData.getContent();
                                                }
                                                startFile.delete();
						String startRevisionContent = new String(
								startByte, encoding);
						this.state
								.getRequest()
								.getSession()
								.setAttribute(
										FileCompareBean.START_REVISION_CONTENT,
										DifferenceModel
												.getUntouchedLines(startRevisionContent));
					}
				}
			} /*else if (true) {
				if ((startRevisionData != null) && (endRevisionData != null)) {

					String str = UUID.getUUID();
					String startPath = tempDirectoryPath + "/" + str;
					String startPath1 = tempDirectoryPath + "/a" + str;
                                        this.writeFile(startPath, startRevisionData.getContent());
					String endPath = tempDirectoryPath + "/" + UUID.getUUID();
                                        this.writeFile(endPath, endRevisionData.getContent());
                                       // String fileType = this.state.getRequest().getParameter("fileType");
                                        if(fileType.compareTo("PDF File")==0){
                                            com.krawler.esp.fileparser.pdf.KWLPdfParser pdfparser = new  com.krawler.esp.fileparser.pdf.KWLPdfParser();  
                                            startByte=pdfparser.getPlaintextpdf(startPath).getBytes("UTF-8");
                                            endByte = pdfparser.getPlaintextpdf(endPath).getBytes("UTF-8");
                                        }else{
                                            startByte = mspares.getByteArray(startPath);
                                            endByte = mspares.getByteArray(endPath);
                                        }
					// / word extract

					String mimeType = SVNFileUtil
							.detectMimeType(new ByteArrayInputStream(
									endRevisionData.getContent()));
					this.writeFile(startPath, startByte);
					this.writeFile(endPath, endByte);

					String difference = dataProvider.getFileDifference(
							this.requestHandler.getUrl(), this.requestHandler
									.getStartRevision(), this.requestHandler
									.getEndRevision(), startPath, endPath,
							encoding);
					DifferenceModel model = new DifferenceModel(difference);

					String startRevisionContent = new String(startByte,
							encoding);
					String endRevisionContent = new String(endByte, encoding);

					this.state.getRequest().getSession().setAttribute(
							FileCompareBean.START_REVISION_CONTENT,
							model.getLeftLines(startRevisionContent));
					this.state.getRequest().getSession().setAttribute(
							FileCompareBean.END_REVISION_CONTENT,
							model.getRightLines(endRevisionContent));
				} else {
					if (startRevisionData == null) {
						String endRevisionContent = new String(endByte,
								encoding);
						this.state.getRequest().getSession().setAttribute(
								FileCompareBean.END_REVISION_CONTENT,
								DifferenceModel
										.getUntouchedLines(endRevisionContent));
					} else if (endRevisionData == null) {
						String str = UUID.getUUID();
						String startPath = tempDirectoryPath + "/" + str;
						this.writeFile(startPath, startRevisionData
								.getContent());
						startByte = mspares.getByteArray(startPath);
						String startRevisionContent = new String(startByte,
								encoding);
						this.state
								.getRequest()
								.getSession()
								.setAttribute(
										FileCompareBean.START_REVISION_CONTENT,
										DifferenceModel
												.getUntouchedLines(startRevisionContent));
					}
				}
			}

			else {
				this.state.getRequest().getSession().setAttribute(
						FileCompareBean.START_REVISION_BINARY,
						new Boolean(true));
				this.state.getRequest().getSession().setAttribute(
						FileCompareBean.END_REVISION_BINARY, new Boolean(true));
				this.state.getRequest().getSession().setAttribute(
						FileCompareBean.START_REVISION_BINARY,
						new Boolean(true));
				this.state.getRequest().getSession().setAttribute(
						FileCompareBean.END_REVISION_BINARY, new Boolean(true));
			}**/
		} catch (DataProviderException e) {
			throw e;
		} catch (Exception ex) {
			throw new SVNWebClientException(ex);
		} finally {
			FileUtil.deleteDirectory(new File(tempDirectoryPath));
		}
		return true;
	}

	public String getStartRevisionViewUrl() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.FILE_COMPARE_DATA, this.requestHandler.getLocation());
		urlGenerator.addParameter(RequestParameters.STARTREV, Long
				.toString(this.requestHandler.getStartRevision()));
		urlGenerator.addParameter(FileCompareBean.PARAM_EXTENSION,
				this.extension);
		return urlGenerator.getUrl();
	}

	public String getEndRevisionViewUrl() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.FILE_COMPARE_DATA, this.requestHandler.getLocation());
		urlGenerator.addParameter(RequestParameters.ENDREV, Long
				.toString(this.requestHandler.getEndRevision()));
		urlGenerator.addParameter(FileCompareBean.PARAM_EXTENSION,
				this.extension);
		return urlGenerator.getUrl();
	}

	public RevisionDetails getStartRevisionInfo() {
		if (this.startRevision == null) {
			return null;
		}
		return new RevisionDetails(this.startRevision, this.headRevision, null,
				null, null);
	}

	public RevisionDetails getEndRevisionInfo() {
		if (this.endRevision == null) {
			return null;
		}
		return new RevisionDetails(this.endRevision, this.headRevision, null,
				null, null);
	}

	public String getStartRevisionUrl() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.REVISION, this.requestHandler.getLocation());
		String url = this.requestHandler.getUrl();
		urlGenerator.addParameter(RequestParameters.URL, UrlUtil.encode(url));
		if (this.requestHandler.getCurrentRevision() != -1) {
			urlGenerator.addParameter(RequestParameters.CREV, Long
					.toString(this.requestHandler.getCurrentRevision()));
		}
		urlGenerator.addParameter(RequestParameters.REV, Long
				.toString(this.requestHandler.getStartRevision()));
		return urlGenerator.getUrl();
	}

	public String getEndRevisionUrl() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.REVISION, this.requestHandler.getLocation());
		String url = this.requestHandler.getUrl();
		urlGenerator.addParameter(RequestParameters.URL, UrlUtil.encode(url));
		if (this.requestHandler.getCurrentRevision() != -1) {
			urlGenerator.addParameter(RequestParameters.CREV, Long
					.toString(this.requestHandler.getCurrentRevision()));
		}
		urlGenerator.addParameter(RequestParameters.REV, Long
				.toString(this.requestHandler.getEndRevision()));
		return urlGenerator.getUrl();
	}

	protected void writeFile(String path, byte[] content) throws Exception {
		File file = new File(path);
		FileOutputStream stream = null;
		try {
			stream = new FileOutputStream(file);
			stream.write(content);
		} finally {
			if (stream != null) {
				try {
					stream.flush();
				} catch (Exception e) {
				}

				try {
					stream.close();
				} catch (Exception e) {
				}
			}
		}
	}

	public String getCurrentUrlWithParameters() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.FILE_COMPARE, requestHandler.getLocation());

		Iterator iter = this.state.getRequest().getParameterMap().entrySet()
				.iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String paramName = (String) entry.getKey();

			if (!RequestParameters.CHARACTER_ENCODING.equals(paramName)) {
				String[] values = (String[]) entry.getValue();
				if (values != null && values.length > 0) {
					for (int i = 0; i < values.length; i++) {
						String paramValue = values[i];
						urlGenerator.addParameter(paramName, paramValue);
					}
				} else {
					urlGenerator.addParameter(paramName);
				}
			}
		}
		return urlGenerator.getUrl();
	}

	protected AbstractRequestHandler getRequestHandler(
			HttpServletRequest request) {
		return new RequestHandler(request);
	}

	public Navigation getNavigation() {
		return new Navigation(this.requestHandler.getUrl(), this.requestHandler
				.getLocation(), this.requestHandler.getCurrentRevision(), true);
	}

	public List getActions() {
		List ret = new ArrayList();
		return ret;
	}

	public FileCompareInfo getChangeSummary() {
		List dataStart = (List) this.state.getRequest().getSession()
				.getAttribute(FileCompareBean.START_REVISION_CONTENT);
		List dataEnd = (List) this.state.getRequest().getSession()
				.getAttribute(FileCompareBean.END_REVISION_CONTENT);
		FileCompareInfo info = new FileCompareInfo();
		FileCompareResult endResult = null;
		FileCompareResult startResult = null;
		Iterator itEnd = null;
		Iterator itStart = null;
		if ((dataStart == null || dataEnd == null) && flag.equals("0")) {
			return null;
		}
		if (dataStart != null) {
			startResult = new FileCompareResult(dataStart);
			itStart = startResult.getLines().iterator();
		}
		if (flag.equals("0")) {
			endResult = new FileCompareResult(dataEnd);
			itEnd = endResult.getLines().iterator();
		} else if (flag.equals("1"))
			return info;

		while (itStart.hasNext()) {
			FileCompareResult.Line startLine = (FileCompareResult.Line) itStart
					.next();
			FileCompareResult.Line endLine = (FileCompareResult.Line) itEnd
					.next();
			if (startLine.getChangeType() == DifferenceLine.MODIFIED) {
				this.stopPointsSettings(startLine, DifferenceLine.MODIFIED,
						FileCompareBean.MOD, info);
			} else if (endLine.getChangeType() == DifferenceLine.ADDED) {
				this.stopPointsSettings(endLine, DifferenceLine.ADDED,
						FileCompareBean.ADD, info);
			} else if (startLine.getChangeType() == DifferenceLine.DELETED) {
				this.stopPointsSettings(startLine, DifferenceLine.DELETED,
						FileCompareBean.DEL, info);
			}
		}
		return info;
	}

	public Collection getCharacterEncodings() {
		return ContentEncodingHelper.getCharacterEncodings();
	}

	public boolean isSelectedCharacterEncoding(String encoding) {
		return ContentEncodingHelper.isSelectedCharacterEncoding(this.state,
				encoding);
	}

	protected void stopPointsSettings(FileCompareResult.Line line,
			int differenceType, int summaryType, FileCompareInfo info) {
		int number = new Integer(line.getNumber()).intValue();
		FileCompareInfo.StopPoints point = info.getLastProperElement(
				number - 1, summaryType);
		if (point != null) {
			point.setBlockPosition(number);
		} else {
			String url = summaryType == FileCompareBean.ADD ? this
					.getEndRevisionViewUrl() : this.getStartRevisionViewUrl();
			info.setStopPoint(number, url, summaryType, number);
			if (summaryType == FileCompareBean.ADD) {
				info.increaseAddedItemsCount();
			} else if (summaryType == FileCompareBean.DEL) {
				info.increaseDeletedItemsCount();
			} else if (summaryType == FileCompareBean.MOD) {
				info.increaseModifiedItemsCount();
			}
		}
	}

	protected void setExtension() {
		String url = this.requestHandler.getUrl();
		int index = url.lastIndexOf(".");
		if (index != -1) {
			this.extension = url.substring(index + 1);
		}
	}
}
