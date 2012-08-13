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

import java.util.ArrayList;
import java.util.UUID;

import com.krawler.common.service.ServiceException;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.database.DbPool.Connection;

public class TagHandler {
	protected static String regx = "^([\'" + '"'
			+ "]?)\\s*([\\w]+[(/|\\{1})]?)*[\\w]\\1$";

	public static void addButtonTag(Connection conn, String userid,
			String docid, String tagname) throws ServiceException {

		String shared = "shared";
		ArrayList<String> DefaultTag = new ArrayList<String>();
		DefaultTag.add("shared");
		DefaultTag.add("uncategorized");
		String[] docidarr = docid.split(",");
		ArrayList<String> tag = TagUtils.getTagWithSlashArray(tagname);

		if (tag.size() > 0 && !DefaultTag.contains(tagname.toLowerCase())
				&& !tagname.toLowerCase().startsWith(shared + "/", 0)) {
			String tagid = TagUtils.getTagId(conn, tagname);
			for (int k = 0; k < docidarr.length; k++) {
				docid = docidarr[k];
				DbUtil.executeUpdate(conn,
						"insert into docstags (tagid,docid,id) values(?,?,?)",
						new Object[] { tagid, docid, userid });

			}
		}
	}

	public static String getTagsForTabPannel(Connection conn, String docid,
			String id) throws ServiceException {
		String allTags = "";
		DbResults rsforTab = null;
		String sql = null;

		sql = "select distinct unitags.tagname from unitags inner join docstags on unitags.tagid = docstags.tagid "
				+ "where docstags.docid = ? and docstags.id = ? ";
		rsforTab = DbUtil.executeQuery(conn, sql, new Object[] { docid, id });

		while (rsforTab.next()) {
			allTags += rsforTab.getString(1) + ",";
		}

		if (allTags.length() > 0) {
			allTags = allTags.substring(0, (allTags.length() - 1));
		}
		// allTags += "]}";
		return allTags;

	}

	public static void tagStore(Connection conn, String userid, String docid,
			String tagnames) throws ServiceException {
		String shared = "shared";
		ArrayList<String> DefaultTag = new ArrayList<String>();
		DefaultTag.add("shared");
		DefaultTag.add("uncategorized");
		DbUtil.executeUpdate(conn,
				"delete from docstags where docid= ? and id = ?", new Object[] {
						docid, userid });
		ArrayList<String> str = TagUtils.getTagWithSlashArray(tagnames.replace(
				",", " "));
		for (int i = 0; i < str.size(); i++) {
			if (!DefaultTag.contains(str.get(i).toLowerCase())
					&& !str.get(i).toLowerCase().startsWith(shared + "/", 0)) {
				String tagid = TagUtils.getTagId(conn, str.get(i));
				DbUtil.executeUpdate(conn,
						"insert into docstags (tagid,docid,id)values(?,?,?)",
						new Object[] { tagid, docid, userid });

			}
		}

	}

	public static void deleteTag(Connection conn, String userid, String tagname)
			throws ServiceException {

		String sql = null;
		tagname = TagUtils.checkTag(tagname);
		sql = "delete from docstags where id = ? and tagid in (select tagid  from unitags where"
				+ " tagname like ? or tagname like ?)";
		DbUtil.executeUpdate(conn, sql, new Object[] { userid, tagname + "/%",
				tagname });
	}

	public static void editTag(Connection conn, String userid,
			String oldTagName, String newTagName) throws ServiceException {
		DbResults rs1 = null;
		int i = 0;
		ArrayList<String> tagname1 = new ArrayList<String>();
		ArrayList<String> tagid1 = new ArrayList<String>();
		String newid = "";
		oldTagName = TagUtils.checkTag(oldTagName);
		newTagName = TagUtils.checkTag(newTagName);
		rs1 = DbUtil.executeQuery(conn,
				"select tagid from unitags where tagname = ?",
				new Object[] { newTagName });
		if (rs1.next()) {
			newid = rs1.getString(1);
		}
		rs1 = DbUtil.executeQuery(conn,
				"select tagid from unitags where tagname = ?",
				new Object[] { oldTagName });
		if (rs1.next()) {
			if (newid.equals("")) {

				newid = UUID.randomUUID().toString();
				DbUtil.executeUpdate(conn,
						"insert into unitags (tagid,tagname) values (?,?)",
						new Object[] { newid, newTagName });
			}
			DbUtil.executeUpdate(conn,
					"update docstags set tagid = ? where tagid = ? and id = ?",
					new Object[] { newid, rs1.getString(1), userid });
		}

		rs1 = DbUtil.executeQuery(conn,
				"select tagid,tagname from unitags where tagname like ?",
				new Object[] { oldTagName + "/%" });

		while (rs1.next()) {
			tagid1.add(rs1.getString(1));
			tagname1.add(rs1.getString(2).replaceFirst(oldTagName, newTagName));
		}
		for (i = 0; i < tagid1.size(); i++) {
			newid = "";
			rs1 = DbUtil.executeQuery(conn,
					"select tagid from unitags where tagname = ?",
					new Object[] { tagid1.get(i) });
			if (rs1.next()) {
				newid = rs1.getString(1);
			} else {
				newid = UUID.randomUUID().toString();
			}
			rs1 = DbUtil
					.executeQuery(
							conn,
							"select count(*) as asd from docstags where tagid = ? and id = ?",
							new Object[] { tagid1.get(i), userid });
			rs1.next();
			if (rs1.getInt(1) > 0) {
				DbUtil.executeUpdate(conn,
						"insert into unitags (tagid,tagname) values (?,?)",
						new Object[] { newid, tagname1.get(i) });
				DbUtil.executeUpdate(conn,
						"UPDATE docstags SET tagid=? WHERE tagid=? and id = ?",
						new Object[] { newid, tagid1.get(i), userid });
			}
		}
	}
}
