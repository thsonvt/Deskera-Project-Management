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

import com.krawler.common.service.ServiceException;
import com.krawler.database.DbPool;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.database.DbPool.Connection;
import com.krawler.esp.database.dbcon;
import com.krawler.utils.json.KWLJsonConverter;

public class CommunityHandler {

	public CommunityHandler() {
	}

	public static String getCommunityMembers(Connection conn,
			String communityId, int offset, int pagesize)
			throws ServiceException {
		ResultSet rs = null;
		ResultSet rs1 = null;
		String splitstring2 = null;
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt = conn
				.prepareStatement("select count(*) as count from (select users.userid as id,userlogin.username as name from users inner join userlogin on users.userid=userlogin.userid inner join communitymembers on users.userid = communitymembers.userid where communityid = ? and status in (3,4,5)) as temp;");
		try {
			pstmt.setString(1, communityId);
			rs = pstmt.executeQuery();
			rs.next();
			int count1 = rs.getInt("count");
			KWLJsonConverter KWL = new KWLJsonConverter();
			pstmt1 = conn
					.prepareStatement("select users.userid as id,userlogin.username as name,image as img from users inner join userlogin on users.userid = userlogin.userid inner join communitymembers on users.userid = communitymembers.userid where communityid = ? and status in (3,4,5) ORDER BY id LIMIT ? OFFSET ?;");

			pstmt1.setString(1, communityId);
			pstmt1.setInt(2, pagesize);
			pstmt1.setInt(3, offset);

			rs1 = pstmt1.executeQuery();

			splitstring2 = KWL.GetJsonForGrid(rs1);

			splitstring2 = splitstring2.substring(0, splitstring2.length() - 1);
			splitstring2 += ",\"count\":[" + count1 + "]}";

			rs.close();
			rs1.close();

		} catch (SQLException e) {
			throw ServiceException.FAILURE("CommunityHandler.getactionlog", e);
		} finally {
			DbPool.closeStatement(pstmt);
			DbPool.closeStatement(pstmt1);
		}

		return " " + splitstring2;
	}

	public static String getMembershipStatus(Connection conn, String userid,
			String communityId) throws ServiceException {
		ResultSet rs1 = null;
		String splitstring = null;
		PreparedStatement pstmt1 = null;

		try {
			pstmt1 = conn
					.prepareStatement("select communitymembers.userid as id1, communitymembers.status as connstatus from communitymembers where userid = ? and communityid = ? ;");
			pstmt1.setString(1, userid);
			pstmt1.setString(2, communityId);

			rs1 = pstmt1.executeQuery();

			KWLJsonConverter KWL = new KWLJsonConverter();

			splitstring = KWL.GetJsonForGrid(rs1).toString();
			rs1.close();
			pstmt1.close();
		} catch (SQLException e) {
			throw ServiceException.FAILURE("CommunityHandler.getactionlog", e);
		} finally {
			DbPool.closeStatement(pstmt1);
		}

		return " " + splitstring;
	}

	public static String getRelatedCommunities(Connection conn, String ulogin,
			int offset, int pagesize) throws ServiceException {

		ResultSet rs = null;
		ResultSet rs1 = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		String splitstring2 = null;
		try {

			pstmt = conn
					.prepareStatement("select count(*) as count from (select community.communityid, communityname from community inner join communitymembers on communitymembers.communityid = community.communityid inner join userrelations on userrelations.userid1=communitymembers.userid where userrelations.relationid = 3 and userrelations.userid2 = ? union select community.communityid, communityname from community inner join communitymembers on communitymembers.communityid = community.communityid inner join userrelations on userrelations.userid1=communitymembers.userid where userrelations.relationid = 3 and userrelations.userid1 = ? and communitymembers.userid = ? ) as temp;");

			pstmt.setString(1, ulogin);
			pstmt.setString(2, ulogin);
			pstmt.setString(3, ulogin);

			rs = pstmt.executeQuery();
			rs.next();
			int count1 = rs.getInt("count");
			KWLJsonConverter KWL = new KWLJsonConverter();
			pstmt1 = conn
					.prepareStatement("select community.communityid as id, communityname as name,image as img from community inner join communitymembers on communitymembers.communityid = community.communityid inner join userrelations on userrelations.userid1=communitymembers.userid where userrelations.relationid = 3 and userrelations.userid2 = ? union select community.communityid as id, communityname as name,image as img from community inner join communitymembers on communitymembers.communityid = community.communityid inner join userrelations on userrelations.userid1=communitymembers.userid where userrelations.relationid = 3 and userrelations.userid1 = ? and communitymembers.userid = ? ORDER BY id LIMIT ? OFFSET ?;");

			pstmt1.setString(1, ulogin);
			pstmt1.setString(2, ulogin);
			pstmt1.setString(3, ulogin);
			pstmt1.setInt(4, pagesize);
			pstmt1.setInt(5, offset);

			rs1 = pstmt1.executeQuery();
			splitstring2 = KWL.GetJsonForGrid(rs1);

			splitstring2 = splitstring2.substring(0, splitstring2.length() - 1);
			splitstring2 += ",\"count\":[" + count1 + "]}";

			rs.close();
			rs1.close();

		} catch (SQLException e) {
			throw ServiceException.FAILURE("CommunityHandler.getactionlog", e);
		} finally {
			DbPool.closeStatement(pstmt);
			DbPool.closeStatement(pstmt1);
		}
		return " " + splitstring2;
	}

	public static String getCommunityDetails(Connection conn, String ulogin)
			throws ServiceException {

		String tp = null;
		ResultSet rs1 = null;

		PreparedStatement pstmt1 = null;
		try {
			pstmt1 = conn
					.prepareStatement("select community.communityid, communityname, aboutcommunity as about from community where community.communityid = ?");
			pstmt1.setString(1, ulogin);

			rs1 = pstmt1.executeQuery();

			KWLJsonConverter KWL1 = new KWLJsonConverter();

			tp = KWL1.GetJsonForGrid(rs1).toString();

			// out.println(" "+tp);

			rs1.close();

		} catch (SQLException e) {
			throw ServiceException.FAILURE("CommunityHandler.getactionlog", e);
		} finally {
			DbPool.closeStatement(pstmt1);
		}
		return " " + tp;
	}

	public static String setStatusCommunity(Connection conn, String userid,
			String comid, int urelationid, int actionid)
			throws ServiceException {
		String query = null;
		PreparedStatement pstmt = null;
		DbResults rs = null;
		ResultSet r = null;
		String actionName;

		try {
			pstmt = conn
					.prepareStatement("SELECT status FROM communitymembers WHERE communityid=? AND userid=?");
			pstmt.setString(1, comid);
			pstmt.setString(2, userid);
			r = pstmt.executeQuery();
			if (r.next()) {
				if (urelationid == 0) {
                                        /*if(actionid==1)
                                        dbcon.InsertLogForAdmin(userid,comid,46,userid,0);     */   
					query = "DELETE FROM communitymembers WHERE userid=? AND communityid=?";
					DbUtil.executeUpdate(conn, query, new Object[] { userid,
							comid });
				} else {
					query = "update communitymembers set status= ? where userid = ? and communityid = ? ";
					DbUtil.executeUpdate(conn, query, new Object[] {
							urelationid, userid, comid });
				}
			} else {
				query = "INSERT INTO communitymembers (communityid, userid, status) VALUES (?,?,?);";
				DbUtil.executeUpdate(conn, query, new Object[] { comid, userid,
						urelationid });
			}
		} catch (SQLException ex) {
			throw ServiceException.FAILURE("ComunitHandler.setStatusCommunity",
					ex);
		}

		/*query = "select users.username from users where users.userid = ? ";
		rs = DbUtil.executeQuery(conn, query, userid);
		rs.next();
		String username1 = rs.getString("username");

		query = "select community.communityname from community where community.communityid = ? ";
		rs = DbUtil.executeQuery(conn, query, comid);
		rs.next();
		String username2 = rs.getString("communityname");

		query = "select actions.actionname from actions where actions.actionid = ? ";
		rs = DbUtil.executeQuery(conn, query, actionid);
		rs.next();
		actionName = rs.getString("actionname");
		actionName = actionName.replace("1", "<u>" + username1 + "</u>");
		actionName = actionName.replace("2", "<c>" + username2 + "</c>");

		query = "insert actionlog (userid, datedon, `by`, actionid, `to`, actiontext) values ( ?, now(), ?, ?, ?, ?)";
		DbUtil.executeUpdate(conn, query, new Object[] { userid, userid,
				actionid, comid, actionName });*/

		return "{\"data\":[{\"result\":\"true\"}]}";
	}
}
