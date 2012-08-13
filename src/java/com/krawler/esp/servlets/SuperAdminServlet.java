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
package com.krawler.esp.servlets;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;

import com.krawler.common.service.ServiceException;
import com.krawler.database.DbPool;
import com.krawler.database.DbUtil;
import com.krawler.database.DbPool.Connection;
import com.krawler.esp.handlers.AuthHandler;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.esp.handlers.genericFileUpload;
import com.krawler.svnwebclient.configuration.ConfigurationException;
import com.krawler.utils.json.KWLJsonConverter;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.sql.Timestamp;

public class SuperAdminServlet extends HttpServlet {

	private static final long serialVersionUID = 4227463129548906351L;

	protected void processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");

		String result = "";
		Connection conn = null;

		try {
			conn = DbPool.getConnection();
			int mode = Integer.parseInt(request.getParameter("mode"));
			switch (mode) {
			case 0:
				result = getCompanyList(conn, request.getParameter("start"),
						request.getParameter("limit"));
				break;

			case 1:
				createCompany(conn, request, response);
				break;

			case 2:
				editCompany(conn, request, response);
				break;

			case 3:
				deleteCompany(conn, request.getParameter("companyid"), "false");
				break;
                        case 4:
                                signUp(conn, request, response);
                                break;
                                
			default:
				break;
			}
			conn.commit();
		} catch (ServiceException ex) {
			DbPool.quietRollback(conn);
		} finally {
			DbPool.quietClose(conn);
			response.getWriter().println(result);
			response.getWriter().close();
		}
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	@Override
	public String getServletInfo() {
		return "All Super Admin Related Functionality Implemented Here";
	}

	public static String getCompanyList(Connection conn, String start,
			String limit) throws ServiceException {
		String result = null;
		PreparedStatement pstmt = null;
		KWLJsonConverter KWL = new KWLJsonConverter();
		ResultSet rs = null;
		String tp = null;
		try {
			pstmt = conn
					.prepareStatement("SELECT company.companyid, company.image, company.companyname, company.createdon, company.address, company.city, company.modifiedon,"
							+ "company.state, company.country, company.phone, company.fax, company.zip, company.timezone, company.website, company.activated, "
							+ "count(companyusers.companyid) AS members FROM company LEFT JOIN companyusers ON company.companyid=companyusers.companyid "
							+ "GROUP BY company.companyid LIMIT ? OFFSET ?;");
			pstmt.setInt(1, Integer.parseInt(limit));
			pstmt.setInt(2, Integer.parseInt(start));
			rs = pstmt.executeQuery();
			result = KWL.GetJsonForGrid(rs);
			pstmt.close();
			pstmt = conn
					.prepareStatement("select count(*) as count from company;");
			rs = pstmt.executeQuery();
			rs.next();
			int count1 = rs.getInt("count");
			result = result.substring(1);
			tp = "{\"count\":" + count1 + "," + result;
		} catch (SQLException e) {
			throw ServiceException.FAILURE("SuperAdminHandler.getSUAdminData",
					e);
		} finally {
			DbPool.closeStatement(pstmt);
		}
		return tp;
	}

	public static String createCompany(Connection conn,
			HttpServletRequest request, HttpServletResponse response)
			throws ServiceException {
		String status = "";
		DiskFileUpload fu = new DiskFileUpload();
                ResultSet frs = null, ars = null;
                PreparedStatement pstmt = null, fpstmt = null, apstmt = null;
                double val = 0.0;
		List fileItems = null;
		try {
			fileItems = fu.parseRequest(request);
		} catch (FileUploadException e) {
			throw ServiceException
					.FAILURE("SuperAdminHandler.createCompany", e);
		}

		HashMap arrParam = new HashMap();
		for (Iterator k = fileItems.iterator(); k.hasNext();) {
			FileItem fi1 = (FileItem) k.next();
			arrParam.put(fi1.getFieldName(), fi1.getString());
		}
		String companyid = UUID.randomUUID().toString();
		/*DbUtil
				.executeUpdate(
						conn,
						"insert into company(companyid, companyname, createdon, address, city, state, country, phone, fax, zip, "
								+ "timezone, website) values (?, ?, now(), ?, ?, ?, ?, ?, ?, ?, ?, ?);",
						new Object[] { companyid, arrParam.get("companyname"),
								arrParam.get("address"), arrParam.get("city"),
								arrParam.get("state"), arrParam.get("country"),
								arrParam.get("phone"), arrParam.get("fax"),
								arrParam.get("zip"), arrParam.get("timezone"),
								arrParam.get("website") });*/
                java.util.Date d = new java.util.Date();
                java.sql.Timestamp today = new Timestamp(d.getTime());
                DbUtil.executeUpdate(conn,"INSERT INTO company(companyid, companyname, address, createdon, website) values (?,?,?,?,?);",
						new Object[] { companyid, arrParam.get("companyname"),
								arrParam.get("address"),today,arrParam.get("website") });
                String userid = UUID.randomUUID().toString();
        		String newPass = AuthHandler.generateNewPassword();
        		String newPassSHA1 = AuthHandler.getSHA1(newPass);
                DbUtil.executeUpdate(conn,"insert into userlogin(userid, username, password, authkey) values (?,?,?,?);",
						new Object[] { userid, arrParam.get("username"),newPassSHA1,newPass});
                DbUtil.executeUpdate(conn,"insert into users(userid, companyid, image) values (?,?,?);",
						new Object[] { userid, companyid, ""});
                try {
                    fpstmt = conn.prepareStatement("SELECT featureid FROM featurelist");
                    frs = fpstmt.executeQuery();
                    while(frs.next()){
                        apstmt = conn.prepareStatement("SELECT activityid FROM activitieslist WHERE featureid=?");
                        apstmt.setInt(1, frs.getInt("featureid"));
                        ars = apstmt.executeQuery();
                        while(ars.next()){
                            val += Math.pow(2, Double.parseDouble(ars.getString("activityid")));
                        }
                        pstmt = conn.prepareStatement("INSERT INTO userpermissions (userid, featureid, permissions) VALUES (?,?,?)");
                        pstmt.setString(1, userid);
                        pstmt.setInt(2, frs.getInt("featureid"));
                        pstmt.setInt(3, (int) val);                    
                        pstmt.executeUpdate();

                        val = 0.0;
                    }
                }
                catch (SQLException e) {
                    throw ServiceException.FAILURE("signup.confirmSignup", e);
                } finally {
                    DbPool.closeStatement(pstmt);
                }
		if (arrParam.get("image").toString().length() != 0) {
			genericFileUpload uploader = new genericFileUpload();
			try {
				uploader.doPost(fileItems, companyid, StorageHandler
						.GetProfileImgStorePath());
			} catch (ConfigurationException e) {
				throw ServiceException.FAILURE(
						"SuperAdminHandler.createCompany", e);
			}
			if (uploader.isUploaded()) {
				DbUtil.executeUpdate(conn,
						"update company set image=? where companyid = ?",
						new Object[] {
								ProfileImageServlet.ImgBasePath + companyid
										+ uploader.getExt(), companyid,
								companyid });
			}
		}
		status = "success";
		return status;
	}

        public static String signUp(Connection conn,
			HttpServletRequest request, HttpServletResponse response)
        throws ServiceException {
        String status = "";
        ResultSet frs = null, ars = null;
        PreparedStatement pstmt = null, fpstmt = null, apstmt = null;
        double val = 0.0;
        String companyid = UUID.randomUUID().toString();
        java.util.Date d = new java.util.Date();
        java.sql.Timestamp today = new Timestamp(d.getTime());
        DbUtil.executeUpdate(conn,"insert into company(companyid, companyname, createdon) values (?,?,?);",
                new Object[] { companyid, request.getParameter("company-name"), today});
        String userid = UUID.randomUUID().toString();
        DbUtil.executeUpdate(conn,"insert into userlogin(userid, username, password, authkey) values (?,?,?,?);",
                new Object[] { userid, request.getParameter("newuserid"), request.getParameter("newpassword"), ""});
        DbUtil.executeUpdate(conn,"insert into users(userid,companyid, emailid, image) values (?,?,?,?);",
                new Object[] { userid, companyid, request.getParameter("email"), ""});
        try {
            fpstmt = conn.prepareStatement("SELECT featureid FROM featurelist");
            frs = fpstmt.executeQuery();
            while(frs.next()){
                apstmt = conn.prepareStatement("SELECT activityid FROM activitieslist WHERE featureid=?");
                apstmt.setInt(1, frs.getInt("featureid"));
                ars = apstmt.executeQuery();
                while(ars.next()){
                    val += Math.pow(2, Double.parseDouble(ars.getString("activityid")));
                }
                pstmt = conn.prepareStatement("INSERT INTO userpermissions (userid, featureid, permissions) VALUES (?,?,?)");
                pstmt.setString(1, userid);
                pstmt.setInt(2, frs.getInt("featureid"));
                pstmt.setInt(3, (int) val);
                pstmt.executeUpdate();

                val = 0.0;
            }
        }
        catch (SQLException e) {
            throw ServiceException.FAILURE("signup.confirmSignup", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        status = "success";
        return status;
	}
        
	public static String editCompany(Connection conn,
			HttpServletRequest request, HttpServletResponse response)
			throws ServiceException {

		String status = "";
		DiskFileUpload fu = new DiskFileUpload();
		JSONObject j = new JSONObject();
		JSONObject j1 = new JSONObject();
		List fileItems = null;
		FileItem fi1 = null;
		try {
			fileItems = fu.parseRequest(request);
		} catch (FileUploadException e) {
			throw ServiceException.FAILURE("SuperAdminHandler.editCompany", e);
		}

		HashMap arrParam = new HashMap();
		for (Iterator k = fileItems.iterator(); k.hasNext();) {
			fi1 = (FileItem) k.next();
			arrParam.put(fi1.getFieldName(), fi1.getString());
		}
		try {
			DbUtil
					.executeUpdate(
							conn,
							"update company set companyname=?, address=?, city=?, state=?, country=?, phone=?, fax=?, "
									+ "zip=?, timezone=?, website=? where companyid=?;",
							new Object[] { arrParam.get("companyname"),
									arrParam.get("address"),
									arrParam.get("city"),
									arrParam.get("state"),
									arrParam.get("country"),
									arrParam.get("phone"), arrParam.get("fax"),
									arrParam.get("zip"),
									arrParam.get("timezone"),
									arrParam.get("website"),
									arrParam.get("companyid") });
			j.put("companyname", arrParam.get("companyname"));
			j.put("address", arrParam.get("address"));
			j.put("city", arrParam.get("city"));
			j.put("state", arrParam.get("state"));
			j.put("country", arrParam.get("country"));
			j.put("phone", arrParam.get("phone"));
			j.put("fax", arrParam.get("fax"));
			j.put("zip", arrParam.get("zip"));
			j.put("timezone", arrParam.get("timezone"));
			j.put("website", arrParam.get("website"));
			j.put("companyid", arrParam.get("companyid"));
			if (arrParam.get("image").toString().length() != 0) {
				genericFileUpload uploader = new genericFileUpload();
				try {
					uploader.doPost(fileItems, arrParam.get("companyid")
							.toString(), StorageHandler
							.GetProfileImgStorePath());
				} catch (ConfigurationException e) {
					throw ServiceException.FAILURE(
							"SuperAdminHandler.createCompany", e);
				}
				if (uploader.isUploaded()) {
					DbUtil.executeUpdate(conn,
							"update company set image=? where companyid = ?",
							new Object[] {
									ProfileImageServlet.ImgBasePath
											+ arrParam.get("companyid")
													.toString()
											+ uploader.getExt(),
									arrParam.get("companyid") });
					j.put("image", arrParam.get("companyid")
							+ uploader.getExt());
				}
			}
			j1.append("data", j);
			status = j1.toString();
		} catch (JSONException e) {
			status = "failure";
			throw ServiceException.FAILURE("SuperAdminHandler.editCompany", e);
		}
		return status;
	}

	public static String deleteCompany(Connection conn, String compIds,
			String activate) throws ServiceException {
		String status = null;
		DbUtil.executeUpdate(conn,
				"update company set activated = ? where companyid in ("
						+ compIds + ");", Boolean.parseBoolean(activate));
		status = "success";
		return status;
	}
}
