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

import com.krawler.common.service.ServiceException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.krawler.common.util.StringUtil;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.KrawlerLog;
import com.krawler.esp.database.dbcon;
import com.krawler.esp.handlers.AuthHandler;
import com.krawler.esp.handlers.FileHandler;
import com.krawler.esp.handlers.ServerEventManager;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.esp.indexer.KrawlerIndexCreator;
import com.krawler.svnwebclient.SVNWebClientException;
import com.krawler.svnwebclient.configuration.ConfigurationException;
import com.krawler.svnwebclient.data.AuthenticationException;
import com.krawler.svnwebclient.data.DataProviderException;
import com.krawler.svnwebclient.data.IDataProvider;
import com.krawler.svnwebclient.data.model.DataChangedElement;
import com.krawler.svnwebclient.web.AttributeStorage;
import com.krawler.svnwebclient.web.controller.ChangeConfirmation;
import com.krawler.svnwebclient.web.model.Navigation;
import com.krawler.svnwebclient.web.model.data.ChangeResult;
import com.krawler.svnwebclient.web.model.data.DeletedElementsList;
import com.krawler.svnwebclient.web.support.RequestHandler;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;

public class DeleteActionServlet extends AbstractServlet {
	protected static final long CURRENT_REVISION = -1;
	public static final String DELETED_ELEMENTS = "deletedelements";
	String[] names = null;
	String[] docids = null;
    String projectid = null;
	KrawlerIndexCreator kwli = null;
	int resp;
	JSONObject deleteddocid = null;

	private static final long serialVersionUID = 3790513931839705409L;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.execute(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try {
			resp = 0;
                        String sep = StorageHandler.GetFileSeparator();
			deleteddocid = new JSONObject();
			JSONObject resp1 = new JSONObject();
            projectid = request.getParameter("PROJECTID");
            if(StringUtil.isNullOrEmpty(projectid)){
                projectid = "";
            }
			// String name = request.getParameter("DELETED_NAME");
			String docid = request.getParameter("DOCID");
			// String userid = request.getParameter("USERID");
			String userid = AuthHandler.getUserid(request);
            String companyid = AuthHandler.getCompanyid(request);
			// names = name.split(",");
			docids = docid.split(",");
			boolean isSvn = false;
			try {
				kwli = new KrawlerIndexCreator();
			} catch (Exception ex) {
				resp = 1;
				throw new ServletException(ex);
			}

			for (int i = 0; i < docids.length; i++) {
				Hashtable ht = com.krawler.esp.database.dbcon.getfileinfo(docids[i]);
                ArrayList<Integer> permission = FileHandler.getSharedPermission(docid);
                boolean isProjectPerm = false;
                for(int cntPerm = 0; cntPerm < permission.size(); cntPerm++) {
                    if(permission.get(cntPerm) == 9 || permission.get(cntPerm) == 10) {
                        isProjectPerm = true;
                    }
                }

				if (userid.equals(ht.get("userid").toString())
						|| request.getParameter("GROUPID").compareTo("1") != 0
                        || isProjectPerm) {
					try {
						Map<String, String> data = new HashMap<String, String>();
						data.put("action", "delete");
						data.put("data", docids[i]);
						ArrayList list = dbcon.getShareddocusers(docids[i]);
						String ver = (String) ht.get("version");
						/*
						 * File fp = new
						 * File(com.krawler.esp.handlers.StorageHandler
						 * .GetDocStorePath(ht.get("storeindex").toString()) +
						 * "/" + "56a08913-f7df-4409-93fe-ba5c8f31f6fe" + "/" +
						 * ht.get("svnname").toString());
						 */
						File fp = new File(
								com.krawler.esp.handlers.StorageHandler
										.GetDocStorePath(ht.get("storeindex")
												.toString())
										+ sep
										+ ht.get("userid").toString()
										+ sep + ht.get("svnname").toString());
						if (fp.exists()) {
							fp.delete();
						}

						if (ver.compareTo("Inactive") == 0) {
							dbcon.deleteDoc(docids[i],userid, 1, companyid, projectid);
							kwli.DeleteIndex(docids[i]);
                            FileHandler.deleteConvertedDoc(userid, docids[i]);
							deleteddocid.append("docid", docids[i]);
							docids[i] = "-1";
						} else {
							isSvn = true;
						}

						if (request.getParameter("GROUPID").compareTo("1") == 0) {
							/*
							 * ServerEventManager.publish( "/" + userid + "/" +
							 * request.getParameter("GROUPID") + "/docs", data,
							 * this .getServletContext());
							 */

							for (int j = 0; j < list.size(); j++) {
								ServerEventManager.publish("/" + list.get(j)
										+ "/" + request.getParameter("GROUPID")
										+ "/docs", data, this
										.getServletContext());
							}
						} else {
							ServerEventManager.publish(
									"/" + request.getParameter("PCID") + "/"
											+ request.getParameter("GROUPID")
											+ "/docs", data, this
											.getServletContext());
						}
					} catch (ConfigurationException ex) {
						resp = 1;
						Logger.getLogger(DeleteActionServlet.class.getName())
								.log(Level.SEVERE, null, ex);
					} catch (JSONException ex) {
						resp = 1;
						Logger.getLogger(DeleteActionServlet.class.getName())
								.log(Level.SEVERE, null, ex);
					}
				} else {
					try {
						if (request.getParameter("GROUPID").compareTo("1") == 0) {
							dbcon.deleteDoc(docids[i], userid, 0, companyid, projectid);
                            FileHandler.deleteConvertedDoc(userid, docids[i]);
							Map<String, String> data = new HashMap<String, String>();
							deleteddocid.append("docid", docids[i]);
							data.put("action", "delete");
							data.put("data", docids[i]);
							docids[i] = "-1";
							ServerEventManager.publish(
									"/" + userid + "/"
											+ request.getParameter("GROUPID")
											+ "/docs", data, this
											.getServletContext());
						}
					} catch (ConfigurationException ex) {
                        resp = 1;
                        Logger.getLogger(DeleteActionServlet.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (JSONException ex) {
						resp = 1;
						Logger.getLogger(DeleteActionServlet.class.getName())
								.log(Level.SEVERE, null, ex);
					}
				}
			}
			if (isSvn) {
				try {
					execute(request, response);
				} catch (ServletException e) {
					KrawlerLog.op.warn("Unable Delete file :" + e.toString());
					resp = 1;
				}
			}
			try {
				resp1.put("res", resp);
				resp1.put("docids", deleteddocid.toString());
				response.getOutputStream().print(resp1.toString());
			} catch (JSONException ex) {
				KrawlerLog.op.warn("Unable to Delete file :" + ex.toString());
				response.getOutputStream().print("{}");
			}

		} catch (ServiceException ex) {
			KrawlerLog.op.warn("Unable to Delete file :" + ex.toString());
			response.getOutputStream().print("{}");

		} catch (SessionExpiredException sex) {
			KrawlerLog.op.warn("Unable to Delete file :" + sex.toString());
			response.getOutputStream().print("{}");
		}
	}

	protected void executeSVNOperation(IDataProvider dataProvider)
			throws SVNWebClientException {
		String comment = this.state.getRequest().getParameter("COMMENT");
		List deletedElements = (List) this.state.getRequest().getSession()
				.getAttribute(DeleteActionServlet.DELETED_ELEMENTS);
		String comments = this.state.getRequest().getParameter("COMMENT");
		int cnt = Integer.parseInt(this.state.getRequest()
				.getParameter("COUNT"));
		List elements1 = new ArrayList();
		List deletedElementsNames = new ArrayList();
		DeletedElementsList.Element element1 = new DeletedElementsList.Element();
		for (int i = 0; i < cnt; i++) {
			if (docids[i].compareTo("-1") != 0) {
				Hashtable ht = com.krawler.esp.database.dbcon
						.getfileinfo(docids[i]);
				element1.setName(ht.get("svnname").toString());
				element1.setComment(comments);
				elements1.add(element1);
				deletedElementsNames.add(ht.get("svnname").toString());
			}
		}

		this.state.getRequest().getSession().setAttribute(
				DeleteActionServlet.DELETED_ELEMENTS, deletedElementsNames);

		deletedElements = deletedElementsNames;

		if (comment == null) {
			comment = (String) AttributeStorage.getInstance().getParameter(
					this.state.getRequest().getSession(), "COMMENT");
		} else {
			AttributeStorage.getInstance().addParameter(
					this.state.getRequest().getSession(), "COMMENT", comment);
		}

		ChangeResult changeResult = null;
		Navigation navigation = new Navigation(this.requestHandler.getUrl(),
				this.requestHandler.getLocation(),
				DeleteActionServlet.CURRENT_REVISION, false);
		try {
                        String userid = AuthHandler.getUserid(this.state.getRequest());
			String companyid = AuthHandler.getCompanyid(this.state.getRequest());

                        for (int i = 0; i < cnt; i++) {
				if (docids[i].compareTo("-1") != 0) {
					dbcon.deleteDoc(docids[i],userid, 1,companyid, projectid);
					kwli.DeleteIndex(docids[i]);
                    FileHandler.deleteConvertedDoc(userid, docids[i]);
					deleteddocid.append("docid", docids[i]);
				}
			}
			DataChangedElement changedElement = dataProvider.delete(
					this.requestHandler.getUrl(), deletedElements, comment);

			List elements = new ArrayList();
			ChangeResult.Element element = new ChangeResult.Element();
			String elementName = changedElement.getName();
			if (elementName.length() == 0) {
				elementName = Navigation.REPOSITORY;
			}
			element.setName(elementName);
			element.setAuthor(changedElement.getAuthor());
			element.setComment(changedElement.getComment());
			element.setDate(changedElement.getDate());
			element.setDirectory(true);
			element.setRevision(changedElement.getRevision());
			elements.add(element);
			String message = "Elements were succesfully deleted";
			changeResult = new ChangeResult(true, message, elements, navigation);
		} catch (SessionExpiredException ex) {
                            resp = 1;
                } catch (AuthenticationException ae) {
			resp = 1;
			throw ae;
		} catch (DataProviderException e) {
			resp = 1;
			AttributeStorage.getInstance().cleanSession(
					this.state.getRequest().getSession());
			this.state.getRequest().getSession().removeAttribute(
					DeleteActionServlet.DELETED_ELEMENTS);
			throw new SVNWebClientException("Delete was failed", e);
		} catch (JSONException ex) {
			AttributeStorage.getInstance().cleanSession(
					this.state.getRequest().getSession());
			this.state.getRequest().getSession().removeAttribute(
					DeleteActionServlet.DELETED_ELEMENTS);
			throw new SVNWebClientException("Delete was failed", ex);
		}
		AttributeStorage.getInstance().cleanSession(
				this.state.getRequest().getSession());
		this.state.getRequest().getSession().removeAttribute(
				DeleteActionServlet.DELETED_ELEMENTS);
		this.state.getRequest().getSession().setAttribute(
				ChangeConfirmation.CHANGE_RESULT, changeResult);
		/*
		 * RequestDispatcher dispatcher = this.state.getRequest()
		 * .getRequestDispatcher(Links.CHANGE_CONFIRMATION); try {
		 * dispatcher.forward(this.state.getRequest(), this.state
		 * .getResponse()); } catch (Exception e) { resp = 1;
		 * KrawlerLog.op.warn("Unable to Delete file :" + e.toString()); throw
		 * new SVNWebClientException(e); }
		 */
	}

	protected RequestHandler getRequestHandler(HttpServletRequest request) {
		return new RequestHandler(request);
	}
}
