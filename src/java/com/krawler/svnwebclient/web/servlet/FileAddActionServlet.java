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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.tmatesoft.svn.core.internal.wc.SVNFileUtil;

import com.krawler.common.service.ServiceException;
import com.krawler.common.util.KrawlerLog;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.database.dbcon;
import com.krawler.esp.docs.docsconversion.OpenOfficeServiceResolver;
import com.krawler.esp.handlers.AuthHandler;
import com.krawler.esp.docs.docsconversion.FileConversionThread;
import com.krawler.esp.handlers.ServerEventManager;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.esp.indexer.Fetcher;
import com.krawler.esp.utils.KrawlerApp;
import com.krawler.svnwebclient.SVNWebClientException;
import com.krawler.svnwebclient.configuration.ConfigurationProvider;
import com.krawler.svnwebclient.data.AuthenticationException;
import com.krawler.svnwebclient.data.DataProviderException;
import com.krawler.svnwebclient.data.IDataProvider;
import com.krawler.svnwebclient.data.model.DataChangedElement;
import com.krawler.svnwebclient.util.FileUtil;
import com.krawler.svnwebclient.util.Uploader;
import com.krawler.svnwebclient.web.AttributeStorage;
import com.krawler.svnwebclient.web.controller.ChangeConfirmation;
import com.krawler.svnwebclient.web.model.Navigation;
import com.krawler.svnwebclient.web.model.data.ChangeResult;
import com.krawler.svnwebclient.web.support.FormParameters;
import com.krawler.svnwebclient.web.support.RequestHandler;
import com.krawler.svnwebclient.web.support.State;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import org.artofsolving.jodconverter.OfficeDocumentConverter;

public class FileAddActionServlet extends AbstractServlet {
	ResultSet rs = null;
	Connection cn = null;
	PreparedStatement pstmt = null;
	String docid = null;
    String projectid = null;
    String ipAddress = null;
	java.util.UUID uid = new java.util.UUID(0, 5);
	private String contentType = "";
    String companyid = null;
    String loginid = null;
	String name = null;
	String svnName = null;
	String comment = null;
	String type = null;
	String pcid = null;
	String groupid = null;
    String userid = "";
	String fileExt = "";
	String FileExt = "";
	String userName = "";
	JSONObject resp1 = new JSONObject();
	String resp = "";
	protected State state;
	protected RequestHandler requestHandler;
	String destinationDirectory = new String();
	private boolean flagType;
	protected static final long CURRENT_REVISION = -1;
	protected Thread fetcher;

	private static com.krawler.esp.indexer.Fetcher fetchFile;
	private static final long serialVersionUID = -744812068956415395L;
	protected static String DESTINATION_DIRECTORY = "destinationDirectory";

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.execute(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		resp = "{'msg':'1','success':true";
		boolean flg = true;
		try {
            companyid = AuthHandler.getCompanyid(request);
            loginid = AuthHandler.getUserid(request);
            ipAddress = AuthHandler.getIPAddress(request);
            this.state = new State(request, response);
            projectid = request.getParameter("projectid");
            if(StringUtil.isNullOrEmpty(projectid)){
                projectid = "";
            }
			this.state.getResponse().setContentType("text/html");
			this.requestHandler = this.getRequestHandler(request);
			Map parameters = null;
			userid = AuthHandler.getUserid(request);
            userName = AuthHandler.getUserName(request);
			String temporaryDirectory = ConfigurationProvider.getInstance()
					.getTempDirectory();
			// destinationDirectory = temporaryDirectory +
			// "/56a08913-f7df-4409-93fe-ba5c8f31f6fe";
			/*
			 * destinationDirectory = com.krawler.esp.handlers.StorageHandler
			 * .GetDocStorePath() +
			 * "/"+com.krawler.esp.handlers.AuthHandler.getUserid(request);
			 */
			if (this.state.getRequest().getParameter("fileadd").compareTo(
					"false") == 0) {
				Uploader uploader = new Uploader();
				uploader.doPost(this.state.getRequest(), this.state
						.getResponse(), destinationDirectory,
						temporaryDirectory);
				if (uploader.isUploaded()) {
					parameters = uploader.getParameters();
					name = (String) parameters.get(FormParameters.FILE_NAME);
                    comment = StringUtil.serverHTMLStripper((String) parameters.get(FormParameters.COMMENT));
					pcid = (String) parameters.get(FormParameters.PCID);
					groupid = (String) parameters.get(FormParameters.GROUPID);
					type = (String) parameters.get(FormParameters.TYPE);
					docid = (String) parameters.get(FormParameters.DOCID);
					svnName = (String) parameters.get("svnName");
					FileExt = (String) parameters.get("fileExt");
					destinationDirectory = (String) parameters
							.get("destinationDirectory");
				} else {
					com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject();
					jobj.put("success", "true");
					jobj.put("msg", uploader.errorMessage());
					flg = false;
					this.state.getResponse().getWriter().print(jobj.toString());

				}
				flagType = uploader.isFlagType();
			} else {
                int auditMode = 0;
				execute(request, response);
                dbcon.InsertAuditLogForDocument(docid, userName, "212", loginid, companyid, ipAddress, auditMode, projectid);
				return;
			}
			if (type.compareTo("1") == 0 || type.compareTo("2") == 0) {

				execute(request, response);
			} else if (type.compareTo("3") == 0) {
				if (!flagType)
					execute(request, response);
				else {
					IDataProvider dataProvider = null;
					executeSVNOperation(dataProvider);
				}
			} else if (type.compareTo("4") == 0) {
				IDataProvider dataProvider = null;
				executeSVNOperation(dataProvider);
			}
		} catch (Exception e) {
			resp = "{'msg':'Problem While Loading file','success':true}";
			KrawlerLog.op.warn("Problem While Uploading file :" + e.toString());

		} finally {
			// response.getOutputStream().print(resp);
			if (flg)
				this.state.getResponse().getWriter().print(resp1.toString());
		}
	}

	protected void executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		ChangeResult changeResult = null;
		DataChangedElement changedElement = null;
		String fileUrl = "";
		boolean isnew = true;
		Map<String, String> data = new HashMap<String, String>();
		try {
			if (this.state.getRequest().getParameter("fileadd").compareTo(
					"true") == 0) {
				boolean addFlag = true;
				java.util.Hashtable ht = com.krawler.esp.database.dbcon
						.getfileinfo(this.state.getRequest().getParameter(
								"fileid"));
				if (!ht.get("userid").toString().equals(
						AuthHandler.getUserid(this.state.getRequest()))
						&& this.state.getRequest().getParameter("groupid")
								.compareTo("1") == 0) {
					if (!com.krawler.esp.handlers.FileHandler
							.getReadWritePermission(
									AuthHandler.getUserid(this.state
											.getRequest()),
									this.state.getRequest().getParameter(
											"fileid")).equals("1"))
						addFlag = false;
				}
				if (addFlag) {
					String destinationDirectoryForActivate = StorageHandler
							.GetDocStorePath(ht.get("storeindex").toString())
							+ StorageHandler.GetFileSeparator() + ht.get("userid").toString();

					changedElement = dataProvider.addFile(this.requestHandler
							.getUrl(), destinationDirectoryForActivate + StorageHandler.GetFileSeparator()
							+ this.state.getRequest().getParameter("filename"),
							this.getComment(ht.get("userid").toString(),
									(String) ht.get("comments")));
					String did = this.state.getRequest().getParameter(
							"filename");
					if (did.contains(".")) {
						did = did.substring(0, did.lastIndexOf("."));
					}

					if (changedElement != null) {
						com.krawler.esp.indexer.KrawlerIndexCreator kwl = new com.krawler.esp.indexer.KrawlerIndexCreator();
						kwl.updateDocument(did, "Revision No", changedElement
								.getRevision()
								+ "");
						dbcon.Setversion(did, ht.get("userid").toString());
					}
					data.put("action", "svnadd");
					data.put("data", did);
					resp1.put("msg", "1");
					resp1.put("success", true);
					resp1.put("action", "svnadd");
					resp1.put("data", did);
					if (this.state.getRequest().getParameter("groupid")
							.compareTo("1") == 0) {
						/*
						 * ServerEventManager.publish("/" +
						 * "56a08913-f7df-4409-93fe-ba5c8f31f6fe" + "/" +
						 * this.state.getRequest().getParameter("groupid") +
						 * "/docs", data, this.getServletContext());
						 */
						/*
						 * ServerEventManager.publish("/" +
						 * com.krawler.esp.handlers.AuthHandler
						 * .getUserid(this.state.getRequest()) + "/" +
						 * this.state.getRequest().getParameter("groupid") +
						 * "/docs", data, this.getServletContext());
						 */
						ArrayList list = dbcon.getShareddocusers(did);
						for (int i = 0; i < list.size(); i++) {
							ServerEventManager.publish("/"
									+ list.get(i)
									+ "/"
									+ this.state.getRequest().getParameter(
											"groupid") + "/docs", data, this
									.getServletContext());
						}
					} else {
						ServerEventManager.publish("/"
								+ this.state.getRequest().getParameter("pcid")
								+ "/"
								+ this.state.getRequest().getParameter(
										"groupid") + "/docs", data, this
								.getServletContext());
					}
					return;
				}
			}

			AttributeStorage handler = AttributeStorage.getInstance();
			HttpSession session = this.state.getRequest().getSession();
			handler.addParameter(session, FormParameters.FILE_NAME, name);
			handler.addParameter(session, FormParameters.COMMENT, comment);
			handler.addParameter(session,
					FileAddActionServlet.DESTINATION_DIRECTORY,
					destinationDirectory);

		} catch (Exception e) {
			AttributeStorage stotage = AttributeStorage.getInstance();
			HttpSession session = this.state.getRequest().getSession();
			name = (String) stotage.getParameter(session,
					FormParameters.FILE_NAME);
			comment = (String) stotage.getParameter(session,
					FormParameters.COMMENT);
			destinationDirectory = (String) stotage.getParameter(session,
					FileAddActionServlet.DESTINATION_DIRECTORY);
		}
		try {
			boolean svnFlag = false;
			fileUrl = this.requestHandler.getUrl();
			if (!fileUrl.endsWith("/") || !fileUrl.endsWith("\\")) {
				fileUrl += StorageHandler.GetFileSeparator();
			}

			/*
			 * if(name.contains(".")) fileExt =
			 * name.substring(name.lastIndexOf(".")); else fileExt = "";
			 */
			fileUrl += svnName;
			// fileUrl += docid+fileExt;
			Navigation navigation = new Navigation(
					this.requestHandler.getUrl(), this.requestHandler
							.getLocation(),
					FileAddActionServlet.CURRENT_REVISION, false);
			if (type.compareTo("1") == 0 || type.compareTo("2") == 0) {
				isnew = false;
				// Hashtable ht =
				// com.krawler.esp.database.dbcon.getfileinfo(docid+fileExt);
				Hashtable ht = com.krawler.esp.database.dbcon
						.getfileinfo(docid);
				String ver = (String) ht.get("version");
				if (ver.compareTo("Active") == 0) {
					changedElement = dataProvider.commitFile(svnName,
							destinationDirectory + StorageHandler.GetFileSeparator() + svnName, this
									.getComment(this.userid, comment));
					svnFlag = true;
				}

				// changedElement = dataProvider.commitFile(docid+fileExt,
				// destinationDirectory + "/" + docid+fileExt, comment);
			} else if (type.compareTo("3") == 0) {
				if (flagType) {
					/*
					 * uploadNewFile(changeResult, changedElement,
					 * destinationDirectory, comment, navigation, name);
					 */
					isnew = true;
				} else {
					isnew = false;
					// Hashtable ht =
					// com.krawler.esp.database.dbcon.getfileinfo(docid+fileExt);
					Hashtable ht = com.krawler.esp.database.dbcon
							.getfileinfo(docid);
					String ver = (String) ht.get("version");
					if (ver.compareTo("Active") == 0) {
						changedElement = dataProvider.commitFile(svnName,
								destinationDirectory + StorageHandler.GetFileSeparator() + svnName, this
										.getComment(this.userid, comment));
						svnFlag = true;
					}
					// changedElement = dataProvider.commitFile(docid+fileExt,
					// destinationDirectory + "/" + docid+fileExt, comment);
				}
			} else if (type.compareTo("4") == 0) {
				isnew = true;
				/*
				 * uploadNewFile(changeResult, changedElement,
				 * destinationDirectory, comment, navigation, name);
				 */
			}
			uploadNewFile(changeResult, changedElement, destinationDirectory,
					comment, navigation, name, svnFlag);
		} catch (AuthenticationException ae) {
			String message = "{\"failure\":true}";
			changeResult = new ChangeResult(false, message, null, null);
			return;
			// throw ae;
		} catch (DataProviderException e) {
			String message = "{\"failure\":true}";
			changeResult = new ChangeResult(false, message, null, null);

			AttributeStorage.getInstance().cleanSession(
					this.state.getRequest().getSession());
			FileUtil.deleteDirectory(new File(destinationDirectory));
			return;
			// throw e;
		} catch (ServiceException ex) {
			String message = "{\"failure\":true}";
			changeResult = new ChangeResult(false, message, null, null);
			return;
		}

		AttributeStorage.getInstance().cleanSession(
				this.state.getRequest().getSession());

		this.state.getRequest().getSession().setAttribute(
				ChangeConfirmation.CHANGE_RESULT, changeResult);

		if (docid == null) {
			docid = "";
		}
		// File file = new File(destinationDirectory + "/" + docid+fileExt);
		File file = new File(destinationDirectory + StorageHandler.GetFileSeparator() + svnName);
		java.util.Date dt = new java.util.Date(file.lastModified());
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			addDoc(docid, name, Long.toString(file.length()), sdf.format(dt),
					"0", pcid, groupid, userid, this.contentType, comment,
					isnew, svnName, projectid);
		} catch (SQLException ex) {
			System.out.println("MyError: " + ex);
		} catch (JSONException ex) {
			System.out.println("MyError: " + ex);
		} catch (ServiceException ex) {
			System.out.println("MyError: " + ex);
		} catch (ParseException ex) {
			System.out.println("MyError: " + ex);
		}
	}

	private void uploadNewFile(ChangeResult changeResult,
			DataChangedElement changedElement, String destinationDirectory,
			String comment, Navigation navigation, String fname, boolean svnFlag) {
		try {

			// changedElement =
			// dataProvider.addFile(this.requestHandler.getUrl(),
			// destinationDirectory + "/" + name, comment);

			List elements = new ArrayList();
			ChangeResult.Element element = new ChangeResult.Element();
			/*
			 * element.setAuthor(changedElement.getAuthor());
			 * element.setSize(changedElement.getSize());
			 * element.setComment(changedElement.getComment());
			 * element.setDate(changedElement.getDate());
			 * element.setDirectory(false); //
			 * element.setName(changedElement.getName());
			 * element.setRevision(changedElement.getRevision());
			 */

			// File file = new File(destinationDirectory + "/" + docid+fileExt);
			File file = new File(destinationDirectory + StorageHandler.GetFileSeparator() + svnName);
			java.util.Date dt = new java.util.Date(file.lastModified());
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dt = sdf.parse(dbcon.fromCompanyToSystem(companyid, sdf.format(dt)));
			element.setAuthor(userName);
			element.setSize(file.length());
			element.setComment(comment);
			element.setDate(dt);
			element.setDirectory(false);
			// element.setRevision(changedElement.getRevision());
			elements.add(element);

			int flag = 0;
			try {
				FileInputStream fin = new FileInputStream(file);
				byte[] b = new byte[(int) file.length()];
				fin.read(b);
				fin.close();
				// this.contentType =
				// getContentType(contentType,destinationDirectory + "/" +
				// docid+fileExt,b);
				this.contentType = KrawlerApp.getContentType(contentType,
						destinationDirectory + StorageHandler.GetFileSeparator() + svnName, b);
				String mimetype = SVNFileUtil.detectMimeType(file);
				if (!this.contentType.equals("")) {
					if (this.contentType.equals("application/vnd.ms-excel")) {
						flag = 1;
					} else if (this.contentType.equals("application/msword")) {
						flag = 1;
					} else if (this.contentType.equals("application/pdf")) {
						flag = 1;
                    } else if(this.contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                        flag = 1;
                    } else if(this.contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                        flag = 1;
                    } else if(this.contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")) {
                        flag = 1;
					} else if (mimetype == null) {
						flag = 1;
					}
				}
			} catch (Exception ex) {
				System.out.print(ex);
			}

			if (flag == 1) {
				fetchFile = Fetcher.get(this.getServletConfig()
						.getServletContext());
				/*
				 * java.util.UUID uuid = new java.util.UUID(0,5); docid =
				 * uuid.randomUUID().toString();
				 */
				Hashtable ht = new Hashtable();
				ht.put("FilePath", destinationDirectory + StorageHandler.GetFileSeparator() + svnName);
				ht.put("FileName", fname);
				ht.put("Author", userName);
				ht.put("DateModified", dt);
				ht.put("Size", file.length());
				ht.put("Type", this.contentType);
				ht.put("DocumentId", docid);
				if (svnFlag)
					ht.put("Revision No", changedElement.getRevision());
				else
					ht.put("Revision No", "-");
				if (fetchFile.isWorking()) {
					fetchFile.add(ht);
				} else {
					fetcher = new Thread(fetchFile);
					fetchFile.add(ht);
					fetcher.start();
				}
			}

            OpenOfficeServiceResolver resolver = OpenOfficeServiceResolver.get(this.getServletContext());
            OfficeDocumentConverter converter = resolver.getDocumentConverter();
            Runnable converterRunnable = new FileConversionThread(file, docid, converter);
            Thread t = new Thread(converterRunnable);
            t.setPriority(6);
            resolver.getConversionThreadPool().execute(t);

			String message = "{\"success\":true}";
			changeResult = new ChangeResult(true, message, elements, navigation);
		} catch (Exception e) {
			System.out.println("MyError: " + e);
		}

	}

	public void addDoc(String docid, String docname, String docsize,
			String docdatemod, String docrevision, String pcid, String groupid,
			String userid, String contentType, String comment, boolean isnew,
			String svnName ,String projectid) throws ServiceException, ParseException,
			JSONException, SQLException {

		try {
			Map<String, String> data = new HashMap<String, String>();
			dbcon.fildoc(docid, docname, docsize, docdatemod, "None", "None",
					docrevision, pcid, groupid, userid,
					getCommonTypeName(contentType), comment, FileExt, svnName, projectid);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
					"EEE MMM d HH:mm:ss z yyyy");
			java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			java.util.Date dt = sdf1.parse(docdatemod);
			if (isnew) {
				JSONObject jobj = new JSONObject();
				jobj.put("Id", docid);
				jobj.put("Name", docname);
				jobj.put("Size", com.krawler.esp.handlers.FileHandler
						.getSizeKb(docsize));
				jobj.put("Type", getCommonTypeName(contentType));

				jobj.put("DateModified", sdf1.format(dt));
				jobj.put("Permission", "None");
				jobj.put("Status", "None");
				jobj.put("Author", com.krawler.esp.handlers.FileHandler
						.getAuthor(userid));
				jobj.put("Owner", "1");
				jobj.put("version", "Inactive");
				if (!FileExt.equals(""))
					jobj.put("Tags", FileExt);
				else
					jobj.put("Tags", "Uncategorized");

				JSONObject jpub = new JSONObject();
				jpub.append("data", jobj);

				data.put("action", "add");
				data.put("data", jpub.toString());
				resp1.put("msg", "1");
				resp1.put("success", true);
				resp1.put("action", "add");
				resp1.put("data", jpub.toString());
				if (groupid.compareTo("1") != 0) {
					// jobj.put("action", "add");
					/*
					 * resp = "{'msg':'1','success':true"; resp1.put("msg","1");
					 * resp1.put("success",true); resp1.put("action","add");
					 * resp1.put("data",jpub.toString());
					 */
					/*
					 * ServerEventManager.publish("/" + userid + "/" + groupid +
					 * "/docs", data, this.getServletContext());
					 */
					// } else {
					ServerEventManager.publish("/" + pcid + "/" + groupid
							+ "/docs", data, this.getServletContext());
				}
			} else {
				JSONObject jobj = new JSONObject();
				jobj.put("Id", docid);
				jobj.put("Size", com.krawler.esp.handlers.FileHandler
						.getSizeKb(docsize));
				jobj.put("DateModified", sdf1.format(dt));

				data.put("action", "commit");
				data.put("data", jobj.toString());
				resp1.put("msg", "1");
				resp1.put("success", true);
				resp1.put("action", "commit");
				resp1.put("data", jobj.toString());
				if (groupid.compareTo("1") == 0) {
					/*
					 * ServerEventManager.publish("/" + userid + "/" + groupid +
					 * "/docs", data, this.getServletContext());
					 */
					ArrayList list = dbcon.getShareddocusers(docid);
					for (int i = 0; i < list.size(); i++) {
						ServerEventManager.publish("/" + list.get(i) + "/"
								+ groupid + "/docs", data, this
								.getServletContext());
					}
				} else {
					ServerEventManager.publish("/" + pcid + "/" + groupid
							+ "/docs", data, this.getServletContext());
				}
			}
            int auditMode = 0;
            if(projectid.equals("")){
                dbcon.InsertAuditLogForDocument(docid, userName, "211", loginid, companyid, ipAddress, auditMode, "");
            } else {
                dbcon.InsertAuditLogForDocument(docid, userName, "341", loginid, companyid, ipAddress, auditMode, projectid);
            }
		} catch (Exception e) {
			System.out.println("MyError: " + e);
		}

	}

	protected RequestHandler getRequestHandler(HttpServletRequest request) {
		return new RequestHandler(request);
	}

	private String getCommonTypeName(String type) {
		String result = "File";
		if (type.equals("application/vnd.ms-excel") || type.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
			result = "Microsoft Excel Worksheet";

		} else if (type.equals("application/msword") || type.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
			result = "Microsoft Word Document";

		} else if (type.equals("application/pdf")) {
			result = "PDF File";

		} else if (type.equals("text/plain")) {
			result = "Text Document";
		} else if (type.equals("text/xml")) {
			result = "XML Document";
		} else if (type.equals("text/xml")) {
			result = "XML Document";
		} else if (type.equals("text/css")) {
			result = "Cascading Style Sheet Document";
		} else if (type.equals("text/html")) {
			result = "HTML File";
		} else if (type.equals("text/cs")) {
			result = "Visual C# Source";
		} else if (type.equals("text/x-javascript")) {
			result = "JScript Script File";
		} else if (type.equals("image/jpeg") || type.equals("image/gif")
				|| type.equals("image/png") || type.equals("image/bmp")) {
			result = "Image";
		} else if (type.equals("image/photoshop")) {
			result = "Adobe Photoshop Image";
		}
		if (type.equals("application/vnd.ms-powerpoint") || type.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")) {
			result = "Microsoft PowerPoint Presentation";

		}
		return result;
	}

	public String getComment(String userid, String Comment)
			throws ServiceException {
		if (comment == null) {
			comment = "";
		}
		return com.krawler.esp.handlers.FileHandler.getAuthor(userid)
				+ "*userName*" + comment;
	}
}
