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
package com.krawler.esp.handlers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Hashtable;

import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import com.krawler.database.DbPool;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.database.DbPool.Connection;
import com.krawler.esp.user.UserDAO;
import com.krawler.esp.user.UserDAOImpl;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;

public class FileHandler2 {
	public static void Setversion(Connection conn, String docid, String userid)
			throws ServiceException {
		// changed signature
		PreparedStatement pstmt = null;
		try {
			pstmt = conn
					.prepareStatement("update docs set version='Active' where docid=?");
			pstmt.setString(1, docid);
			pstmt.executeUpdate();
//			LogHandler
//					.makeLogentry(conn, docid, userid, 67, 62,57, null, null,null);
		} catch (SQLException e) {
			throw ServiceException.FAILURE("File.FileChk", e);
		} finally {
			DbPool.closeStatement(pstmt);
		}
	}

	public static String getDocType(Connection conn, String docid)
			throws ServiceException {

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = conn
					.prepareStatement("select doctype from docs where docid=?");
			pstmt.setString(1, docid);
			rs = pstmt.executeQuery();
			if (rs.next())
				return rs.getString(1);
			else
				return "";

		} catch (SQLException e) {
			throw ServiceException.FAILURE("FileHandler.getDocType", e);
		} finally {
			DbPool.closeStatement(pstmt);
		}
	}

	public static Hashtable<String, String> getfileinfo(Connection conn,
			String Id) throws ServiceException {
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		Hashtable<String, String> ht = new Hashtable<String, String>();
		try {
			pstmt = conn
					.prepareStatement("select docdatemod,comments,docsize,docname,version,doctype,svnname,storageindex,userid,docstatus,docper from docs where docid = ?");
			pstmt.setString(1, Id);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				ht.put("date", rs.getString(1));
				if (rs.getString(2) != null)
					ht.put("comments", rs.getString(2));
				else
					ht.put("comments", "");
				ht.put("size", rs.getString(3));
				ht.put("docname", rs.getString(4));
				ht.put("version", rs.getString(5));
				ht.put("doctype", rs.getString(6));
				ht.put("svnname", rs.getString(7));
				ht.put("storeindex", rs.getString(8));
				ht.put("userid", rs.getString(9));
				ht.put("status", rs.getString(10));
				ht.put("per", rs.getString(11));
			}

		} catch (SQLException e) {
			throw ServiceException.FAILURE("FileHandler.getfileinfo", e);

		} finally {
			DbPool.closeStatement(pstmt);
		}
		return ht;
	}

	public static ArrayList<String> getonlineusers(Connection conn)
			throws ServiceException {

		ResultSet rs = null;
		PreparedStatement pstmt = null;
		ArrayList<String> Id = new ArrayList<String>();
		try {
			pstmt = conn
					.prepareStatement("select userid from users where userstatus='online'");
			rs = pstmt.executeQuery();
			while (rs.next()) {
				Id.add(rs.getString(1));
			}

		} catch (SQLException e) {
			throw ServiceException.FAILURE("FileHandler.getfileinfo", e);

		} finally {
			DbPool.closeStatement(pstmt);
		}
		return Id;
	}

	public static ArrayList<String> getShareddocusers(Connection conn,
			String docid) throws ServiceException {

		ResultSet rs = null;
		PreparedStatement pstmt = null;
		ArrayList<String> Id = new ArrayList<String>();
		try {
			pstmt = conn
					.prepareStatement("(select docprerel.userid from docprerel where docid = ? and permission=?) "
							+ "union (select userrelations.userid2 from docprerel inner join userrelations on userrelations.userid1=docprerel.userid where docprerel.docid = ? and docprerel.permission=? and userrelations.relationid=3 ) "
							+ "union (select userrelations.userid1 from docprerel inner join userrelations on userrelations.userid2=docprerel.userid where docprerel.docid = ? and docprerel.permission=? and userrelations.relationid=3) "
							+ "union (select users.userid from docprerel inner join users on users.userid=docprerel.userid where users.userstatus='online' and docprerel.permission=? and docprerel.docid = ?)");
			pstmt.setString(1, docid);
			pstmt.setString(2, "2");
			pstmt.setString(3, docid);
			pstmt.setString(4, "1");
			pstmt.setString(5, docid);
			pstmt.setString(6, "1");
			pstmt.setString(7, "3");
			pstmt.setString(8, docid);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				Id.add(rs.getString(1));
			}

		} catch (SQLException e) {
			throw ServiceException.FAILURE("FileHandler.getfileinfo", e);

		} finally {
			DbPool.closeStatement(pstmt);
		}
		return Id;
	}

	public static Hashtable<String, Object> FileChk(Connection conn,
			String docname, String userid, String groupid, String pcid)
			throws ServiceException {

		String sql = null;
		Hashtable<String, Object> responseText = new Hashtable<String, Object>();
		String userdocid = null;
		String docownerid = null;
		DbResults rs = null;
		DbResults rs1 = null;
		DbResults rs2 = null;
		if (groupid.equals("1")) {
			sql = "select docs.docid,docs.userid from docs inner join users on users.userid=docs.userid where docname = ? and users.userid = ? and docs.groupid = ?";
			rs = DbUtil.executeQuery(conn, sql, new Object[] { docname, userid,
					groupid });
			if (rs.next()) {
				userdocid = rs.getString(1);
				docownerid = rs.getString(2);
				boolean flag = false;
				sql = "select docs.userid,userlogin.username,docs.docid from docprerel inner join docs on docs.docid=docprerel.docid inner join userlogin on userlogin.userid=docs.userid where docs.docid in (select docid from docs where docname = ? and userid !=? )and docprerel.permission=? and docprerel.userid=? and docprerel.readwrite = ? and docs.groupid = ?";
				rs1 = DbUtil.executeQuery(conn, sql, new Object[] { docname,
						userid, "2", userid, 1, groupid });

				ArrayList<String> share = new ArrayList<String>();
				ArrayList<String> id = new ArrayList<String>();
				ArrayList<String> uname = new ArrayList<String>();
				while (rs1.next()) {
					share.add(rs1.getString(1));
					id.add(rs1.getString(3));
					uname.add(rs1.getString(2));
					flag = true;
				}
				if (flag) {
					responseText.put("type", 1);
					responseText.put("userdocid", userdocid);
					responseText.put("value", share);
					responseText.put("id", id);
					responseText.put("username", uname);
				} else {
					responseText.put("type", 2);
					responseText.put("userdocid", userdocid);
					responseText.put("docownerid", docownerid);
				}
			} else {

				rs1 = DbUtil.executeQuery(conn,
						"select count(*) from docs where docname = ?", docname);
				if (rs1.next()) {
					if (rs1.getInt(1) > 0) {
						boolean flag = false;
						sql = "select docs.userid,userlogin.username,docs.docid from docprerel inner join docs on docs.docid=docprerel.docid inner join userlogin on userlogin.userid=docs.userid where docs.docid in (select docid from docs where docname = ? and userid != ? )and docprerel.permission=? and docprerel.userid= ? and docprerel.readwrite = ? and docs.groupid = ?";
						rs2 = DbUtil.executeQuery(conn, sql, new Object[] {
								docname, userid, "2", userid, 1, groupid });
						ArrayList<String> share = new ArrayList<String>();
						ArrayList<String> id = new ArrayList<String>();
						ArrayList<String> uname = new ArrayList<String>();
						while (rs2.next()) {
							share.add(rs2.getString(1));
							id.add(rs2.getString(3));
							uname.add(rs2.getString(2));
							flag = true;
						}
						if (flag) {
							responseText.put("type", 3);
							responseText.put("value", share);
							responseText.put("id", id);
							responseText.put("username", uname);
						} else {
							responseText.put("type", 4);
						}
					} else {
						responseText.put("type", 4);
					}
				}
			}
		} else {
			if (groupid.equals("2")) {
				// sql = "select docs.docid from docprerel inner join docs on
				// docs.docid=docprerel.docid where docs.docid = (select docid
				// from docs where docname = ? and groupid =? )and
				// docprerel.permission=? and docprerel.userid= (select
				// projectid from project where projectname = ?)";
				sql = "select docs.docid from docs inner join docprerel on docs.docid=docprerel.docid inner join project on project.projectid=docprerel.userid where docs.groupid=2 and docs.docname=? and project.projectid=?";
				rs = DbUtil.executeQuery(conn, sql, new Object[] { docname,
						pcid });
			} else {
				// sql = "select docs.docid from docprerel inner join docs on
				// docs.docid=docprerel.docid where docs.docid = (select docid
				// from docs where docname = ? and groupid =? )and
				// docprerel.permission=? and docprerel.userid= (select
				// communityid from community where communityname = ?)";
				sql = "select docs.docid from docs inner join docprerel on docs.docid=docprerel.docid inner join community on community.communityid=docprerel.userid where docs.groupid=3 and docs.docname=? and community.communityid=?";
				rs = DbUtil.executeQuery(conn, sql, new Object[] { docname,
						pcid });
			}
			if (rs.next()) {
				responseText.put("type", 2);
				responseText.put("userdocid", rs.getString(1));
			} else {
				responseText.put("type", 4);
			}
		}
		return responseText;

	}

	public static int getSizeKb(String size) {
		int no = ((Integer.parseInt(size)) / 1024);
		if (no >= 1)
			return no;
		else
			return 1;

	}

	public static String getFilename(String name) throws ServiceException {
		String fName = "";
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		com.krawler.database.DbPool.Connection conn = null;

		try {
			conn = DbPool.getConnection();
			pstmt = conn
					.prepareStatement("select docname from docs where docid = ?");
			String Id = name.substring(0, name.lastIndexOf("."));
			pstmt.setString(1, Id);
			rs = pstmt.executeQuery();
			rs.next();
			fName = rs.getString(1);
		} catch (SQLException e) {
			throw ServiceException.FAILURE("FileHandler.getFilename", e);
		} finally {
			DbPool.quietClose(conn);
		}
		return fName;
	}

	public static ArrayList<DbResults> getSharedDocs(Connection conn,
			String userid, String tagname) throws ServiceException {
		// DbResults[] rs = new DbResults();
		ArrayList<DbResults> rs = new ArrayList<DbResults>();
		String sql = null;
		String ownerid = "";
		if (tagname.contains("/") && !tagname.equals("")) {
			ownerid = getUserid(tagname.split("/")[1]);
		}
		if (ownerid.equals("")) {
			sql = "SELECT distinct docid, docname, docsize, doctype, docdatemod,docper,docstatus,version,userid,storageindex FROM docs where docid in "
					+ "(select docid from docprerel where permission = ? and userid!= ?); ";
			rs
					.add(DbUtil.executeQuery(conn, sql, new Object[] { "3",
							userid }));
			sql = "select distinct docs.docid,docs.docname,docs.docsize, docs.doctype,docs.docdatemod,docs.docper,docs.docstatus,docs.version,docs.userid,docs.storageindex  from docs "
					+ "where docs.docid in(select docprerel.docid from docprerel where docprerel.userid in"
					+ "(select  userid2 as userid from userrelations where userid1 = ? union select  userid1 as userid from userrelations where  userid2= ? and relationid = '3') and permission=?);";
			rs.add(DbUtil.executeQuery(conn, sql, new Object[] { userid,
					userid, "1" }));
			/*
			 * sql = "select docs.docid,docs.docname,docs.docsize,
			 * docs.doctype,docs.docdatemod,docs.docper,docs.docstatus,docs.version,docs.userid,docs.storageindex
			 * from docs " + "where docs.docid in (select docprerel.docid from
			 * docprerel where docprerel.userid = ? and docprerel.permission =
			 * ?);";
			 */
			sql = "select distinct docs.docid,docs.docname,docs.docsize, docs.doctype,docs.docdatemod,docs.docper,docs.docstatus,docs.version,docs.userid,docs.storageindex ,docprerel.readwrite from docs inner join docprerel on docprerel.docid = docs.docid"
					+ " where docs.docid in (select docprerel.docid from docprerel where docprerel.userid = ? and docprerel.permission = ?);";
			rs
					.add(DbUtil.executeQuery(conn, sql, new Object[] { userid,
							"2" }));
		} else {
			sql = "SELECT distinct docid, docname, docsize, doctype, docdatemod,docper,docstatus,version,userid,storageindex FROM docs where docid in "
					+ "(select docid from docprerel where permission = ? and userid!= ?)"
					+ "and docid in (SELECT docid from docs where userid = ?); ";
			rs.add(DbUtil.executeQuery(conn, sql, new Object[] { "3", userid,
					ownerid }));
			sql = "select distinct docs.docid,docs.docname,docs.docsize, docs.doctype,docs.docdatemod,docs.docper,docs.docstatus,docs.version,docs.userid,docs.storageindex  from docs "
					+ "where docs.docid in(select docprerel.docid from docprerel where docprerel.userid in"
					+ "(select  userid2 as userid from userrelations where userid1 = ? union select  userid1 as userid from userrelations where  userid2= ? and relationid = '3') and permission=?)"
					+ "and docid in (SELECT docid from docs where userid = ?);";
			rs.add(DbUtil.executeQuery(conn, sql, new Object[] { userid,
					userid, "1", ownerid }));
			sql = "select distinct docs.docid,docs.docname,docs.docsize, docs.doctype,docs.docdatemod,docs.docper,docs.docstatus,docs.version,docs.userid,docs.storageindex ,docprerel.readwrite from docs inner join docprerel on docprerel.docid = docs.docid "
					+ "where docs.docid in (select docprerel.docid from docprerel where docprerel.userid = ? and docprerel.permission = ?)"
					+ "and  docs.docid in (SELECT docid from docs where userid = ?);";
			rs.add(DbUtil.executeQuery(conn, sql, new Object[] { userid, "2",
					ownerid }));
		}
		return rs;
	}

	public static String getReadWritePermission(String userid, String docid)
			throws ServiceException {
		String rw = "";
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		com.krawler.database.DbPool.Connection conn = null;
		try {
			conn = DbPool.getConnection();
			pstmt = conn
					.prepareStatement("select readwrite from docprerel where userid = ? and docid = ?");
			pstmt.setString(1, userid);
			pstmt.setString(2, docid);
			rs = pstmt.executeQuery();
			rs.next();
			rw = rs.getString(1);
                        //?
//			DbPool.quietClose(conn);
		} catch (SQLException e) {
			throw ServiceException.FAILURE("FileHandler.getAuthor", e);
		} finally {
			DbPool.quietClose(conn);
		}
		return rw;

	}

	public static String fillGrid(Connection conn, String userid,
			String tagname, String groupid, String pcid)
			throws ServiceException {
		String gridString = null;
		DbResults rs = null;
		String sql = null;
		// JSONStringer j = new JSONStringer();
		ArrayList<String> DefaultTag = new ArrayList<String>();
		DefaultTag.add("shared");
		DefaultTag.add("uncategorized");
		if (groupid.equals("1")) {
			pcid = "1";
		}

		if (tagname == null || tagname.equals("*flag*")) {
			if (groupid.equals("1")) {
				sql = "SELECT docid, docname, docsize, doctype, docdatemod,docper,docstatus,version,userid,storageindex FROM docs WHERE userid = ? and groupid = ?;";
				rs = DbUtil.executeQuery(conn, sql, new Object[] { userid,
						groupid });

				ArrayList<DbResults> rs1 = getSharedDocs(conn, userid, "");
				gridString = "{data:[";
				gridString += getJString(rs, conn, userid, groupid, pcid);

				for (int i = 0; i < rs1.size(); i++) {
					gridString += getJString(rs1.get(i), conn, userid, groupid,
							pcid);

				}
				if (gridString.charAt(gridString.length() - 1) != '[') {
					gridString = gridString.substring(0,
							(gridString.length() - 1));

				}

				gridString += "]}";
			}
			if (groupid.equals("2")) {
				gridString = "{data:[";
				sql = "SELECT docid, docname, docsize, doctype, docdatemod,docper,docstatus,version,userid,storageindex FROM docs where docid in "
						+ "(select docid from docprerel where permission = ? and userid = ?); ";
				rs = DbUtil.executeQuery(conn, sql, new Object[] { "5", pcid });

				gridString += getJString(rs, conn, userid, groupid, pcid);

				if (gridString.charAt(gridString.length() - 1) != '[') {
					gridString = gridString.substring(0,
							(gridString.length() - 1));
				}

				gridString += "]}";

			}
			if (groupid.equals("3")) {
				gridString = "{data:[";
				sql = "SELECT docid, docname, docsize, doctype, docdatemod,docper,docstatus,docs.version,docs.userid,docs.storageindex FROM docs where docid in "
						+ "(select docid from docprerel where permission = ? and userid = ?); ";
				rs = DbUtil.executeQuery(conn, sql, new Object[] { "6", pcid });
				gridString += getJString(rs, conn, userid, groupid, pcid);

				if (gridString.charAt(gridString.length() - 1) != '[') {

					gridString = gridString.substring(0,
							(gridString.length() - 1));
				}

				gridString += "]}";

			}
		} else {
			gridString = "{data:[";
			String Shared = "shared";
			if (DefaultTag.contains(tagname.toLowerCase())
					|| tagname.toLowerCase().startsWith(Shared + "/")) {
				if (groupid.equals("1")
						&& (StringUtil
								.stringCompareInLowercase(Shared, tagname) || tagname
								.toLowerCase().startsWith(Shared + "/"))) {
					ArrayList<DbResults> rs1 = getSharedDocs(conn, userid,
							tagname);
					for (int i = 0; i < rs1.size(); i++) {
						gridString += getJString(rs1.get(i), conn, userid,
								groupid, pcid);
					}
				} else if (StringUtil.stringCompareInLowercase("uncategorized",
						tagname)) {
					if (groupid.equals("1")) {
						sql = "select distinct docs.docid,docs.docname,docs.docsize,docs.doctype,docs.docdatemod,docs.docper,docs.docstatus,docs.version,docs.userid,docs.storageindex "
								+ "from docs where docs.userid = ? and groupid = ? and  docs.docid not in "
								+ "(select docid from docstags where id = ?)";
						rs = DbUtil.executeQuery(conn, sql, new Object[] {
								userid, groupid, userid });
					} else {
						sql = "select distinct docs.docid,docs.docname,docs.docsize,docs.doctype,docs.docdatemod,docs.docper,docs.docstatus,docs.version,docs.userid,docs.storageindex"
								+ " from docs where groupid = ? and  docs.docid in (select distinct docprerel.docid from docprerel where docprerel.userid = ? and"
								+ " docprerel.permission = ? and docprerel.docid not in ( select docstags.docid from docstags where  docstags.id = ?))";

						if (groupid.equals("2"))
							rs = DbUtil.executeQuery(conn, sql, new Object[] {
									groupid, pcid, "5", pcid });
						else
							rs = DbUtil.executeQuery(conn, sql, new Object[] {
									groupid, pcid, "6", pcid });
					}
					gridString += getJString(rs, conn, userid, groupid, pcid);
				}

			} else {
				tagname = TagUtils.checkTag(tagname);
				if (groupid.equals("1")) {
					sql = "select distinct docs.docid,docs.docname,docs.docsize,docs.doctype,docs.docdatemod,docs.docper,docs.docstatus,docs.version,docs.userid,docs.storageindex from docs "
							+ "inner join docstags on docs.docid = docstags.docid inner join unitags on unitags.tagid = docstags.tagid "
							+ "inner join users on users.userid = docstags.id where unitags.tagname = ? and users.userid = ?";

					rs = DbUtil.executeQuery(conn, sql, new Object[] { tagname,
							userid });
				} else {
					sql = "select distinct docs.docid,docs.docname,docs.docsize,docs.doctype,docs.docdatemod,docs.docper,docs.docstatus,docs.version,docs.userid,docs.storageindex from docs "
							+ "inner join docstags on docstags.docid = docs.docid inner join unitags on unitags.tagid = docstags.tagid "
							+ " where unitags.tagname = ? and "
							+ "docstags.id = ? and docs.groupid=?;";
					rs = DbUtil.executeQuery(conn, sql, new Object[] { tagname,
							pcid, groupid });
				}
				gridString += getJString(rs, conn, userid, groupid, pcid);

			}
			if (gridString.charAt(gridString.length() - 1) != '[') {
				gridString = gridString.substring(0, (gridString.length() - 1));
			}

			gridString += "]}";

		}

		return gridString;
	}

	public static String getJString(com.krawler.database.DbResults rsforJson,
			Connection conn, String userid, String groupid, String pcid)
			throws ServiceException {
		JSONObject jobj = null;
		String gridString = "";
                UserDAO udao =new UserDAOImpl();
		try {
			while (rsforJson.next()) {
				String id = "";
				if (groupid.equals("1")) {
					id = userid;
				} else {
					id = pcid;
				}
				String tags = TagHandler.getTagsForTabPannel(conn, rsforJson
						.getString(1), id);
				if (groupid.equals("1")
						&& !userid.equals(rsforJson.getString(9))) {
					if (tags.length() > 0) {
						tags += ",Shared/"
								+ udao.getUser(conn,rsforJson.getString(9)).getUserName();
					} else {
						tags = "Shared/" + udao.getUser(conn,rsforJson.getString(9)).getUserName();
					}
				}
				if (tags.length() == 0) {
					tags += "Uncategorized";
				}
				int flag = 0;
				if (userid.equals(rsforJson.getString(9))) {
					flag = 1;
				}
				jobj = new JSONObject();
				jobj.put("Id", rsforJson.getString(1));
				jobj.put("Name", rsforJson.getString(2));
				jobj.put("Size", getSizeKb(rsforJson.getString(3)));
				jobj.put("Type", rsforJson.getString(4));
				jobj.put("DateModified", rsforJson.getObject(5));
				jobj.put("Permission", rsforJson.getObject(6));
				jobj.put("Status", rsforJson.getObject(7));
				jobj.put("version", rsforJson.getObject(8));
				jobj.put("Author", getAuthor(rsforJson.getString(1), 0));
				jobj.put("Tags", tags);
				jobj.put("Owner", flag);
				jobj.put("storeindex", rsforJson.getObject(10));
				try {
					jobj.put("readwrite", rsforJson.getObject(11));
				} catch (Exception e) {
					jobj.put("readwrite", "0");
				}
				gridString += jobj.toString();
				gridString += ",";
			}

		} catch (JSONException e) {
			throw ServiceException.FAILURE("FileHandler.getJString", e);
		}
		// return jobj.toString();
		return gridString;
	}

	public static String getAuthor(String docid, int flag)
			throws ServiceException {
		String author = "";
		if (flag == 1) {
			if (docid.contains("."))
				docid = docid.substring(0, docid.lastIndexOf("."));
		}
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		com.krawler.database.DbPool.Connection conn = null;

		try {
			conn = DbPool.getConnection();
			pstmt = conn
					.prepareStatement("select userid from docs where docid = ?");
			pstmt.setString(1, docid);
			rs = pstmt.executeQuery();
			rs.next();
			author = getAuthor(rs.getString(1));
                        //?
//			DbPool.quietClose(conn);
		} catch (SQLException e) {
			throw ServiceException.FAILURE("FileHandler.getAuthor", e);
		} finally {
			DbPool.quietClose(conn);
		}
		return author;
	}

	public static String getAuthor(String userid) throws ServiceException {
		String fname = "";
		String lname = "";
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		com.krawler.database.DbPool.Connection conn = null;

		try {
			conn = DbPool.getConnection();
			pstmt = conn
					.prepareStatement("select fname,lname from users where userid = ?");
			pstmt.setString(1, userid);
			rs = pstmt.executeQuery();
			rs.next();
			if (rs.getString(1) != null)
				fname = rs.getString(1);
			if (rs.getString(2) != null)
				lname = rs.getString(2);
                        //?
//			DbPool.quietClose(conn);
		} catch (SQLException e) {
			throw ServiceException.FAILURE("FileHandler.getAuthor", e);
		} finally {
			DbPool.quietClose(conn);
		}
		return (fname + " " + lname).trim();
	}

//	public static String getUserName(String userid) throws ServiceException {
//		String username = "";
//		ResultSet rs = null;
//		PreparedStatement pstmt = null;
//		com.krawler.database.DbPool.Connection conn = null;
//
//		try {
//			conn = DbPool.getConnection();
//			pstmt = conn
//					.prepareStatement("select username from userlogin where userid = ?");
//			pstmt.setString(1, userid);
//			rs = pstmt.executeQuery();
//			rs.next();
//			if (rs.getString(1) != null)
//				username = rs.getString(1);
//		} catch (SQLException e) {
//			throw ServiceException.FAILURE("FileHandler.getAuthor", e);
//		} finally {
//			DbPool.quietClose(conn);
//		}
//		return username;
//	}

	public static String getUserid(String userName) throws ServiceException {
		String userid = "";
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		com.krawler.database.DbPool.Connection conn = null;

		try {
			conn = DbPool.getConnection();
			pstmt = conn
					.prepareStatement("select userid from userlogin where username = ?");
			pstmt.setString(1, userName);
			rs = pstmt.executeQuery();
			rs.next();
			if (rs.getString(1) != null)
				userid = rs.getString(1);
		} catch (SQLException e) {
			throw ServiceException.FAILURE("FileHandler.getAuthor", e);
		} finally {
			DbPool.quietClose(conn);
		}
		return userid;
	}

	public static void deleteDoc(Connection conn, String docid, String userid,
			int flag,String companyid) throws ServiceException {
		if (flag == 1) {
                        String docname=FileHandler2.getDocName(conn, docid);
//			LogHandler.makeLogentry(conn, docid, userid, 69, 64, 58, docname, null,companyid);
			DbUtil.executeUpdate(conn, "Delete from docs WHERE docid=?", docid);
		} else {
			DbUtil.executeUpdate(conn,
					"Delete from docprerel WHERE docid = ? and userid = ?",
					new Object[] { docid, userid });
			DbUtil.executeUpdate(conn,
					"Delete from doctagrel where docid = ? and userid = ?",
					new Object[] { docid, userid });
		}
	}

	public static void fildoc(Connection conn, String docid, String docname,
			String docsize, String docdatemod, String permission,
			String status, String docrevision, String pcid, String groupid,
			String userid, String type, String comment, String tags,
			String svnName) throws ServiceException {

		DbResults rs = null;
		// String pcid = "1";
		String sql = null;
		java.util.Date docdt = null;
		String currentStoreIndex = StorageHandler.GetCurrentStorageIndex();
		if (comment.length() > 80) {
			comment = comment.substring(0, 79);
		}
		try {
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
					"EEE MMM d HH:mm:ss z yyyy");
			docdt = sdf.parse(docdatemod);

			if (groupid.equals("2")) {
				sql = "select count(*) from docs ds,docprerel dp where ds.docname =? and "
						+ "ds.userid =? and ds.groupid =? and ds.docid =dp.docid and  "
						+ "dp.userid=? and dp.permission='5'";

			} else if (groupid.equals("3")) {
				sql = "select count(*) from docs ds,docprerel dp where ds.docname =? and "
						+ "ds.userid =? and ds.groupid =? and ds.docid =dp.docid and  "
						+ "dp.userid=? and dp.permission='6'";

			} else if (groupid.equals("1")) {
				sql = "select count(*) from docs where  docname = ? and userid =? and groupid =?";
			}

			if (groupid.equals("2") || groupid.equals("3")) {
				rs = DbUtil.executeQuery(conn, sql, new Object[] { docname,
						userid, groupid, pcid });
			} else {
				rs = DbUtil.executeQuery(conn, sql, new Object[] { docname,
						userid, groupid });
			}
			rs.next();
			if (rs.getInt(1) == 0) {

				sql = "INSERT INTO docs(docid, docname, docsize,docdatemod, "
						+ "userid,docper,docstatus,docrevision,groupid,doctype,comments,version,svnname,storageindex) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				DbUtil.executeUpdate(conn, sql, new Object[] { docid, docname,
						docsize, docdt, userid, permission, status,
						docrevision, groupid, type, comment, "Inactive",
						svnName, currentStoreIndex });
				if (!tags.equals("")) {
					String id = "";
					if (groupid.equals("1")) {
						id = userid;
					} else {
						id = pcid;
					}
					TagHandler.addButtonTag(conn, id, docid, tags);
				}
				if (groupid.endsWith("1")) {
					pcid = "1";
				}

				if (groupid.equals("2")) {

					DbUtil
							.executeUpdate(
									conn,
									"insert into docprerel (docid,userid,permission) values(?,?,?)",
									new Object[] { docid, pcid, "5" });
					// changes made by kedar
//					LogHandler.InsertLogForDocs(conn, userid, userid, docid,
//							66, docname, pcid);
                                       //ActionId 28 indicates that New file added in project  
                                        
				}
				if (groupid.equals("3")) {

					DbUtil
							.executeUpdate(
									conn,
									"insert into docprerel (docid,userid,permission) values(?,?,?)",
									new Object[] { docid, pcid, "6" });
//					LogHandler.InsertLogForDocs(conn, userid, userid, docid,
//							61, docname, pcid);
				}

			} else {
				DbUtil
						.executeUpdate(
								conn,
								"UPDATE docs SET docrevision = ?,comments=?,docdatemod=?,docsize=? WHERE docid= ?;",
								new Object[] { docrevision, comment, docdt,
										docsize, docid });
//				LogHandler.makeLogentry(conn, docid, userid, 72, 71, 56,docname, pcid,null);
			}
		} catch (ParseException e) {
			throw ServiceException.FAILURE("FileHandler.fildoc", e);
		}

	}
        
        public static String getDocName(Connection conn, String docid)
			throws ServiceException {

		PreparedStatement pstmt = null;
		ResultSet rs = null;
                String docname=null;
		try {
			pstmt = conn
					.prepareStatement("select docname from docs where docid=?");
			pstmt.setString(1, docid);
			rs = pstmt.executeQuery();
                        rs.next();
			docname=rs.getString(1);
		} catch (SQLException e) {
			throw ServiceException.FAILURE("FileHandler.getDocType", e);
		} finally {
			DbPool.closeStatement(pstmt);
		}
                return docname; 
                
	}
        

}
