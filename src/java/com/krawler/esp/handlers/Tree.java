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

import com.krawler.utils.json.base.JSONException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.timezone.Timezone;
import com.krawler.common.util.KWLErrorMsgs;
import com.krawler.common.util.KrawlerLog;
import com.krawler.database.DbPool;
import com.krawler.database.DbPool.Connection;
import com.krawler.utils.json.KWLJsonConverter;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.esp.portalmsg.Mail;
import com.krawler.svnwebclient.configuration.ConfigurationException;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import com.krawler.esp.web.resource.Links; 

/**
 * Utility method for working Calendars.
 */
public class Tree {

    /**
     * Fetches the all the calendars and its details associated with the project or the user
     * @param conn connection object used for performing database operations
     * @param uid user or project id with which the calendars are associated
     * @param userId id of the user currently requesting the calendars
     * @param latestts latestts
     * @return object containing all the calendars assciated with that project or user
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static String getCalendarlist(Connection conn, String uid, String userId,
			String latestts) throws ServiceException {
		PreparedStatement pstmt = null;
        String jsonMyCal="";
		try {
			KWLJsonConverter kjs = new KWLJsonConverter();
			ResultSet rs = null;
            String query = "select calendars.cid, calendars.cname, calendars.description, calendars.location, calendars.timezone, " +
                    " calendars.colorcode, calendars.caltype,  calendars.isdefault, calendars.userid, calendars.timestamp, " +
                    " case (select COUNT(*) as count from uncheckedcalmap where uncheckedcalmap.userid = ? and uncheckedcalmap.cid = calendars.cid ) " +
                    " when 0 then 1 else 0 end as exportCal, networkcalendars.url as url, " +
                    " IF(url IS NOT NULL, 2, '') as permissionlevel, "+
                    " IF(url IS NOT NULL, TIME_TO_SEC(TIMEDIFF(networkcalendars.nextsync, networkcalendars.lastsync))/60, -1) as syncinterval, "+
                    " case (calendars.isdefault) when 1 then 1 else case (select COUNT(*) as count from selectedcalendars where selectedcalendars.userid = ? and selectedcalendars.cid = calendars.cid) when 0 then false else true end end as lastselected "+
                    " from calendars left join networkcalendars on calendars.cid = networkcalendars.cid " +
                    " where calendars.userid = ? order by isdefault DESC ";
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, userId);
			pstmt.setString(2, userId);
            pstmt.setString(3, uid);
			rs = pstmt.executeQuery();
			jsonMyCal = kjs.GetJsonForGrid(rs);
		} catch (SQLException e) {
            KrawlerLog.calendar.warn("Tree.getCalendarlist:" + e);
			throw ServiceException.FAILURE("Tree.getCalendarlist", e);
		} finally {
			DbPool.closeStatement(pstmt);
		}
        return jsonMyCal;
	}

    /**
     * Fetches the all the shared calendars associated with the user
     * @param conn connection object used for performing database operations
     * @param uid user or project id with which the calendars are associated
     * @return object containing all the calendars assciated with that user
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static String getSharedCalendarlist(Connection conn, String uid)
			throws ServiceException {
		PreparedStatement pstmt = null;
		try {
			KWLJsonConverter kjs = new KWLJsonConverter();
			ResultSet rs = null;
			String query = "select c.cid,c.cname,c.description,c.location,c.timezone,s.colorcode,c.caltype,c.isdefault,s.userid,c.timestamp,s.permissionlevel from calendars as c inner join sharecalendarmap as s"
					+ " on c.cid=s.cid where s.userid=?";
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, uid);
			rs = pstmt.executeQuery();
			String jsonShared = kjs.GetJsonForGrid(rs);
			return jsonShared;
		} catch (SQLException e) {
            KrawlerLog.calendar.warn("Tree.getSharedCalendarlist:", e);
			throw ServiceException.FAILURE("Tree.getSharedCalendarlist", e);
		} finally {
			DbPool.closeStatement(pstmt);
		}
	}

    /**
     * Fetches the all the shared calendars and its details associated with the user
     * @param conn connection object used for performing database operations
     * @param cid id of the shared calendar
     * @param userid user or project id with which the calendars are associated
     * @return object containing all the calendars assciated with that user
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static String getSharedCalendar(Connection conn, String cid, String userid)
			throws ServiceException {
		PreparedStatement pstmt = null;
		try {
			KWLJsonConverter kjs = new KWLJsonConverter();
			ResultSet rs = null;
			String query = "select c.cid, c.cname, c.description, c.location, c.timezone, s.colorcode, c.caltype, c.isdefault, s.userid, " +
                            " c.timestamp, s.permissionlevel from calendars as c inner join sharecalendarmap as s on c.cid = s.cid " +
                            " where s.cid = ? and s.userid = ? ";
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, cid);
			pstmt.setString(2, userid);
			rs = pstmt.executeQuery();
			String jsonShared = kjs.GetJsonForGrid(rs);
			return jsonShared;
		} catch (SQLException e) {
            KrawlerLog.calendar.warn("Tree.getSharedCalendar:", e);
			throw ServiceException.FAILURE("Tree.getSharedCalendar", e);
		} finally {
			DbPool.closeStatement(pstmt);
		}
	}

    /**
     * Creates a new calendar with the specified properties
     * @param conn connection object used for performing database operations
     * @param cname name of the calendar
     * @param desc description of the calendar
     * @param location location of the calendar
     * @param timezone time zone of the calendar
     * @param colorcode color code of the calendar
     * @param caltype type of the calendar
     * @param isdefault whether the calendar is a default calendar
     * @param userid id of the project or the user with which this calendar is associated
     * @return id of the newly created calendar
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static String insertNewCalendar(Connection conn, String cname,
			String desc, String location, String timezone, String colorcode,
			int caltype, int isdefault, String userid) throws ServiceException {
        String cid = UUID.randomUUID().toString();
        int r = insertNewCalendar(conn, cid, cname, desc, location, timezone, colorcode, caltype, isdefault, userid);
        return ((r != 0) ? cid : "0");
	}
    
    /**
     * Creates a new calendar with the specified properties
     * @param conn connection object used for performing database operations
     * @param cid UUID of the calendar
     * @param cname name of the calendar
     * @param desc description of the calendar
     * @param location location of the calendar
     * @param timezone time zone of the calendar
     * @param colorcode color code of the calendar
     * @param caltype type of the calendar
     * @param isdefault whether the calendar is a default calendar
     * @param userid id of the project or the user with which this calendar is associated
     * @return id of the newly created calendar
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static int insertNewCalendar(Connection conn, String cid, String cname,
			String desc, String location, String timezone, String colorcode,
			int caltype, int isdefault, String userid) throws ServiceException {
		PreparedStatement pstmt = null;
		try {
            cname = StringUtil.serverHTMLStripper(cname);
            desc = StringUtil.serverHTMLStripper(desc);
            location = StringUtil.serverHTMLStripper(location);
            timezone = StringUtil.serverHTMLStripper(timezone);
            colorcode = StringUtil.serverHTMLStripper(colorcode);
            userid = StringUtil.serverHTMLStripper(userid);

            if(StringUtil.isNullOrEmpty(cname) || StringUtil.isNullOrEmpty(userid)) {
                return 0;
            }

			pstmt = conn
					.prepareStatement("insert into calendars (cid,cname,description,location,timezone,colorcode,caltype,isdefault,userid,timestamp) values( ?, ?, ?, ?, ?, ?, ?, ?, ?, now())");

			pstmt.setString(1, cid);
			pstmt.setString(2, cname);
			pstmt.setString(3, desc);
			pstmt.setString(4, location);
			pstmt.setString(5, timezone);
			pstmt.setString(6, colorcode);
			pstmt.setInt(7, caltype);
			pstmt.setInt(8, isdefault);
			pstmt.setString(9, userid);

			int r = pstmt.executeUpdate();
			return r;
		} catch (SQLException e) {
			throw ServiceException.FAILURE("Tree.insertNewCalendar::Insertion error", e);
		} finally {
			DbPool.closeStatement(pstmt);
		}
	}

    /**
     * Creates a new calendar with the specified properties
     * @param conn connection object used for performing database operations
     * @param url URL of the calendar to be synced from
     * @param userid id of the project or the user with which this calendar is associated
     * @return id of the newly created calendar
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static String insertInternetCalendar(Connection conn, String cid, String userid,
            String url, int interval) throws ServiceException {
		PreparedStatement pstmt = null;
        SimpleDateFormat sdfLong = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
            userid = StringUtil.serverHTMLStripper(userid);

            if(StringUtil.isNullOrEmpty(cid) || StringUtil.isNullOrEmpty(userid) || StringUtil.isNullOrEmpty(url)) {
                return "0";
            }
            Date currTime = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(currTime);
            cal.add(Calendar.MINUTE, interval);
            Date nextSyncTime = cal.getTime();
            java.sql.Timestamp t = new Timestamp(nextSyncTime.getTime());

            String companyid = CompanyHandler.getCompanyByUser(conn, userid);
			pstmt = conn.prepareStatement("insert into networkcalendars (cid, userid, url, lastsync, nextsync, companyid) values( ?, ?, ?, ?, ?, ?)");

			pstmt.setString(1, cid);
			pstmt.setString(2, userid);
			pstmt.setString(3, url);
			pstmt.setTimestamp(4, new Timestamp(currTime.getTime()));
			pstmt.setTimestamp(5, t);
			pstmt.setString(6, companyid);

			int r = pstmt.executeUpdate();
			return ((r != 0) ? cid : "0");
		} catch (SQLException e) {
			throw ServiceException.FAILURE("Tree.insertInternetCalendar::Insertion error", e);
		} finally {
			DbPool.closeStatement(pstmt);
		}
	}
    
    /**
     * Creates a new calendar with the specified properties
     * @param conn connection object used for performing database operations
     * @param url URL of the calendar to be synced from
     * @param userid id of the project or the user with which this calendar is associated
     * @param hasJustSynced id of the project or the user with which this calendar is associated
     * @return id of the newly created calendar
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static int updateInternetCalendar(String cid, int interval, boolean hasJustSynced) throws ServiceException {
        SimpleDateFormat sdfLong = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
            if(StringUtil.isNullOrEmpty(cid)) {
                return 0;
            }
            Date lastSyncTime = new Date();
            if(!hasJustSynced){
                DbResults rs = DbUtil.executeQuery("SELECT lastsync FROM networkcalendars WHERE cid = ?", cid);
                if(rs.next()){
                    lastSyncTime = sdfLong.parse(rs.getObject("lastsync").toString());
                }
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(lastSyncTime);
            cal.add(Calendar.MINUTE, interval);
            Date nextSyncTime = cal.getTime();
            java.sql.Timestamp nst = new java.sql.Timestamp(nextSyncTime.getTime());
            java.sql.Timestamp lst = new java.sql.Timestamp(lastSyncTime.getTime());

			int r = DbUtil.executeUpdate("UPDATE networkcalendars SET lastsync = ?, nextsync = ? WHERE cid = ?",
                    new Object[]{lst, nst, cid});
			return r;
		} catch (ParseException e) {
			throw ServiceException.FAILURE("Tree.updateInternetCalendar::Date parse error", e);
		}
	}

    /**
     * Updates a calendar with the specified properties
     * @param conn connection object used for performing database operations
     * @param cid id of the calendar to be updated
     * @param cname name of the calendar
     * @param desc description of the calendar
     * @param location location of the calendar
     * @param timezone time zone of the calendar
     * @param colorcode color code of the calendar
     * @param caltype type of the calendar
     * @param isdefault whether the calendar is a default calendar
     * @param userid id of the project or the user with which this calendar is associated
     * @return id of the calendar that was updated
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static String updateCalendar(Connection conn, String cid,
			String cname, String desc, String location, String timezone,
			String colorcode, int caltype, int isdefault, String userid)
			throws ServiceException {
		PreparedStatement pstmt = null;
        try {
            cname = StringUtil.serverHTMLStripper(cname);
            desc = StringUtil.serverHTMLStripper(desc);
            location = StringUtil.serverHTMLStripper(location);
            timezone = StringUtil.serverHTMLStripper(timezone);
            colorcode = StringUtil.serverHTMLStripper(colorcode);
            userid = StringUtil.serverHTMLStripper(userid);
            cid = StringUtil.serverHTMLStripper(cid);

            if(StringUtil.isNullOrEmpty(cname) || StringUtil.isNullOrEmpty(userid) || StringUtil.isNullOrEmpty(cid)) {
                if(isdefault!=1){
                    return "0";
                }
            }
			pstmt = conn
					.prepareStatement("update calendars set cname=?, description=?, location=?, timezone=?, colorcode=?, caltype=?, isdefault=?,  userid=?, timestamp=now() where cid=?");
			pstmt.setString(1, cname);
			pstmt.setString(2, desc);
			pstmt.setString(3, location);
			pstmt.setString(4, timezone);
			pstmt.setString(5, colorcode);
			pstmt.setInt(6, caltype);
			pstmt.setInt(7, isdefault);
			pstmt.setString(8, userid);
			pstmt.setString(9, cid);

			int updateResult = pstmt.executeUpdate();
			return ((updateResult != 0) ? cid : "0");
		} catch (SQLException e) {
            KrawlerLog.calendar.warn("Tree.updateCalendar:" + e);
			throw ServiceException.FAILURE(
					"Tree.updateCalendar::Updation error", e);
		} finally {
			DbPool.closeStatement(pstmt);
		}
	}

    /**
     * Deletes a calendar
     * @param conn connection object used for performing database operations
     * @param cid id of the calendar to be deleted
     * @param isdefault whether the calendar is a default calendar
     * @return count (only 1) if the calendar has been successfully deleted else 0
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static int deleteCalendar(Connection conn, String cid, int isdefault)
			throws ServiceException {
		PreparedStatement pstmt = null;
		try {
			int rs = 0;
			if (isdefault == 1){
				pstmt = conn.prepareStatement("delete from calendarevents where cid=?");
                pstmt.setString(1, cid);
            } else {
                pstmt = conn.prepareStatement("SELECT cid FROM networkcalendars WHERE cid = ?");
                pstmt.setString(1, cid);
                ResultSet r = pstmt.executeQuery();
                if(r.next()){
                    File file = new File(StorageHandler.GetDocStorePath() +"/"+ cid + ".ics");
                    if(file.exists())
                        file.delete();
                }
				pstmt = conn.prepareStatement("delete from calendars where cid=?");
                pstmt.setString(1, cid);
            }
            rs = pstmt.executeUpdate();
			return rs;
		} catch (ConfigurationException e) {
            KrawlerLog.calendar.warn("Tree.deleteCalendar:" + e);
			throw ServiceException.FAILURE("Tree.deleteCalendar", e);
        } catch (SQLException e) {
            KrawlerLog.calendar.warn("Tree.deleteCalendar:" + e);
			throw ServiceException.FAILURE("Tree.deleteCalendar", e);
		} finally {
			DbPool.closeStatement(pstmt);
		}
	}

    /**
     * updates calendar selection in the calendar tree.
     * @param conn connection object used for performing database operations
     * @param cid id of the calendar to be deleted
     * @param userid userid
     * @param selection flag telling if the calendar is selected or deselected
     * @return count no of rows updated
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static int updateCalendarSelection(Connection conn, String cid, String userid, boolean selection) throws ServiceException {
        int rs = 0;
		PreparedStatement pstmt = null;
		try {
            boolean execute = false;
            if (selection){
                pstmt = conn.prepareStatement("SELECT isdefault FROM calendars WHERE cid = ?");
                pstmt.setString(1, cid);
                ResultSet rst = pstmt.executeQuery();
                if(rst.next()){
                    if(!rst.getBoolean("isdefault")){
                        pstmt = conn.prepareStatement("INSERT INTO selectedcalendars VALUES(?, ?)");
                        pstmt.setString(1, cid);
                        pstmt.setString(2, userid);
                        execute = true;
                    }
                }
            } else {
				pstmt = conn.prepareStatement("DELETE FROM selectedcalendars WHERE cid = ? AND userid = ?");
                pstmt.setString(1, cid);
                pstmt.setString(2, userid);
                execute = true;
            }
            if(execute)
                rs = pstmt.executeUpdate();
            return rs;
		} catch (SQLException e) {
            KrawlerLog.calendar.warn("Tree.updateCalendarSelection:" + e);
			throw ServiceException.FAILURE("Tree.updateCalendarSelection : "+e.getMessage(), e);
        }
	}

    /**
     * Deletes a calendar
     * @param conn connection object used for performing database operations
     * @param cid id of the calendar to be deleted
     * @return Calendar details
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static String getCalendar(Connection conn, String cid)
			throws ServiceException {
		PreparedStatement pstmt = null;
		try {
			KWLJsonConverter kjs = new KWLJsonConverter();
			ResultSet rs = null;

            pstmt = conn
					.prepareStatement(" select calendars.cid, calendars.cname, calendars.description, calendars.location, " +
                                        " calendars.timezone, calendars.colorcode, calendars.caltype,  calendars.isdefault, calendars.userid, " +
                                        " calendars.timestamp, '' as permissionlevel, " +
                                        " case (select COUNT(*) as count from uncheckedcalmap where uncheckedcalmap.cid = calendars.cid ) when 0 then 1 else 0 end as exportCal, " +
                                        " networkcalendars.url as url, IF(url IS NOT NULL, 2, '') as permissionlevel, " +
                                        " IF(url IS NOT NULL, TIME_TO_SEC(TIMEDIFF(networkcalendars.nextsync, networkcalendars.lastsync))/60, -1) as syncinterval " +
                                        " from calendars left join networkcalendars on networkcalendars.cid = calendars.cid where calendars.cid = ? order by isdefault  DESC ");

			pstmt.setString(1, cid);
			rs = pstmt.executeQuery();
			String json = null;
			json = kjs.GetJsonForGrid(rs);
			return json;
		} catch (SQLException e) {
            KrawlerLog.calendar.warn("Tree.getCalendar:" + e);
			throw ServiceException.FAILURE("Tree.getCalendar", e);
		} finally {
			DbPool.closeStatement(pstmt);
		}
	}

    /**
     * Fetches the calendar id and name for the specified type of calendar
     * @param conn connection object used for performing database operations
     * @param userid id of the project or user with which the calendar is associated
     * @param calType type of the calendar
     * @return Calendar id and name
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static String getCalendarType(Connection conn, String userid, String calType)
			throws ServiceException {
		PreparedStatement pstmt = null;
		try {
			KWLJsonConverter kjs = new KWLJsonConverter();
			ResultSet rs = null;

            pstmt = conn
					.prepareStatement(" select calendars.cid, calendars.cname from calendars where calendars.caltype = ? " +
                                      " and calendars.cid NOT IN " +
                                      " ( select sharecalendarmap.cid from sharecalendarmap where sharecalendarmap.userid = ? ) ");

			pstmt.setString(1, calType);
			pstmt.setString(2, userid);
			rs = pstmt.executeQuery();
			String json = "";
			json = kjs.GetJsonForGrid(rs);
			return json;
		} catch (SQLException e) {
            KrawlerLog.calendar.warn("Tree.getCalendarType:" + e);
			throw ServiceException.FAILURE("Tree.getCalendarType", e);
		} finally {
			DbPool.closeStatement(pstmt);
		}
	}

    /**
     * Inserts calendar permissions
     * @param conn connection object used for performing database operations
     * @param cid id of the calendar
     * @param userid id of the user
     * @param permissionlevel the level of permission to be set
     * @return id of the calendar whose permissions have been inserted
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static String insertCalPermission(Connection conn, String cid,
			String userid, int permissionlevel) throws ServiceException {
		PreparedStatement pstmt = null;
		try {
			pstmt = conn
					.prepareStatement("insert into sharecalendarmap (cid,userid,permissionlevel,timestamp) values( ?, ?, ?, now())");
			pstmt.setString(1, cid);
			pstmt.setString(2, userid);
			pstmt.setInt(3, permissionlevel);

			int r = pstmt.executeUpdate();
			return ((r != 0) ? cid : "0");
		} catch (SQLException e) {
            KrawlerLog.calendar.warn("Tree.insertPermission:", e);
			throw ServiceException.FAILURE("Tree.insertPermission", e);
		} finally {
			DbPool.closeStatement(pstmt);
		}
	}

    /**
     * Updates calendar permissions [used for updating color-code of Holiday Calendars]
     * @param conn connection object used for performing database operations
     * @param cid id of the calendar
     * @param userid id of the user
     * @param permissionlevel the level of permission to be set
     * @return id of the calendar whose permissions have been inserted
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static String updateCalPermission(Connection conn, String cid,
			String userid, int permissionlevel, String colorcode) throws ServiceException {
		PreparedStatement pstmt = null;
		try {
			pstmt = conn
					.prepareStatement(" update sharecalendarmap set permissionlevel = ?, colorcode = ?, timestamp = now() where cid = ? and userid = ? ");
			pstmt.setInt(1, permissionlevel);
			pstmt.setString(2, colorcode);
            pstmt.setString(3, cid);
			pstmt.setString(4, userid);

			int r = pstmt.executeUpdate();
			return ((r != 0) ? cid : "0");
		} catch (SQLException e) {
            KrawlerLog.calendar.warn("Tree.updatePermission:", e);
			throw ServiceException.FAILURE("Tree.updatePermission", e);
		} finally {
			DbPool.closeStatement(pstmt);
		}
	}

    /**
     * Fetches the calendar permissions
     * @param conn connection object used for performing database operations
     * @param cid id of the calendar whose permissions are to be fetched
     * @return calendar permissions
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static String getCalendarPermission(Connection conn, String cid)
			throws ServiceException {
		PreparedStatement pstmt = null;
		try {
			KWLJsonConverter kjs = new KWLJsonConverter();
			ResultSet rs = null;

			pstmt = conn
					.prepareStatement("select * from sharecalendarmap where cid=?");
			pstmt.setString(1, cid);
			rs = pstmt.executeQuery();
			String json = null;
			json = kjs.GetJsonForGrid(rs);
			return json;
		} catch (SQLException e) {
            KrawlerLog.calendar.warn("Tree.getCalendarPermission:" + e);
			throw ServiceException.FAILURE("Tree.getCalendarPermission", e);

		} finally {
			DbPool.closeStatement(pstmt);
		}
	}

    /**
     * Fetched the ids' of the users associated with the specific calendar
     * @param conn connection object used for performing database operations
     * @param cid id of the calendar whose associated users have to be fetched
     * @return ids of the users associated with the specific calendar
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static String getCalendarSharedUserIds(Connection conn, String cid)
			throws ServiceException {
		PreparedStatement pstmt = null;
		try {
			KWLJsonConverter kjs = new KWLJsonConverter();
			ResultSet rs = null;

			pstmt = conn
					.prepareStatement("select userid from sharecalendarmap where cid=?");
			pstmt.setString(1, cid);
			rs = pstmt.executeQuery();
			String json = null;
			json = kjs.GetJsonForGrid(rs);

			return json;
		} catch (SQLException e) {
            KrawlerLog.calendar.warn("Tree.getCalendarSharedUserIds:" + e);
			throw ServiceException.FAILURE("Tree.getCalendarSharedUserIds", e);

		} finally {
			DbPool.closeStatement(pstmt);
		}
	}

    /**
     * Fetched the ids' of the users associated with the specific calendar
     * @param conn connection object used for performing database operations
     * @param cid id of the calendar whose associated users have to be fetched
     * @return ids of the users associated with the specific calendar
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static String getCalendarSharedCheck(Connection conn, String cid, String userid)
			throws ServiceException {
		PreparedStatement pstmt = null;
		try {
			KWLJsonConverter kjs = new KWLJsonConverter();
			ResultSet rs = null;

			pstmt = conn
					.prepareStatement(" select * from sharecalendarmap where cid = ? and userid = ? ");
			pstmt.setString(1, cid);
            pstmt.setString(2, userid);
			rs = pstmt.executeQuery();
			String json = null;
			json = kjs.GetJsonForGrid(rs);

			return json;
		} catch (SQLException e) {
            KrawlerLog.calendar.warn("Tree.getCalendarSharedUserIds:" + e);
			throw ServiceException.FAILURE("Tree.getCalendarSharedUserIds", e);

		} finally {
			DbPool.closeStatement(pstmt);
		}
	}

    /**
     * Deletes calendar permissions (also used for Holiday calendars)
     * @param conn connection object used for performing database operations
     * @param cid id of the calendar to be deleted
     * @param isdefault whether the calendar is a default calendar
     * @return count (only 1) if the calendar has been successfully deleted else 0
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static int deleteCalendarPermission(Connection conn, String cid)
			throws ServiceException {
		PreparedStatement pstmt = null;
		try {
			int rs = 0;
			pstmt = conn
					.prepareStatement("delete from sharecalendarmap where cid=?");
			pstmt.setString(1, cid);
			rs = pstmt.executeUpdate();
			return rs;
		} catch (SQLException e) {
            KrawlerLog.calendar.warn("Tree.deleteCalendarPermission:" + e);
			throw ServiceException.FAILURE("Tree.deleteCalendarPermission", e);
		} finally {
			DbPool.closeStatement(pstmt);
		}
	}

    /**
     * Deletes calendar permissions of the specific user or project
     * @param conn connection object used for performing database operations
     * @param userid id of the user or project
     * @return count (only 1) if the calendar permission for the specific user or project has been successfully deleted else 0
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
    public static int deleteCalendarUserPermission(Connection conn, String userid, String cid)
			throws ServiceException {
		PreparedStatement pstmt = null;
		try {
			int rs = 0;
			pstmt = conn
					.prepareStatement("delete from sharecalendarmap where userid = ? and cid = ? ");
			pstmt.setString(1, userid);
			pstmt.setString(2, cid);
			rs = pstmt.executeUpdate();
			return rs;
		} catch (SQLException e) {
            KrawlerLog.calendar.warn("Tree.deleteCalendarUserPermission:" + e);
			throw ServiceException.FAILURE("Tree.deleteCalendarUserPermission", e);
		} finally {
			DbPool.closeStatement(pstmt);
		}
	}

    /**
     * Creates export check so that the specific calendar is not exported for the specific user
     * @param conn connection object used for performing database operations
     * @param cid id of calendar not to be exported
     * @param userid id of user for whom this calendar is not to be exported
     * @return id of the calendar which will not be exported
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
    public static String insertExportCheck(Connection conn, String cid, String userid) throws ServiceException {
		PreparedStatement pstmt = null;
		try {
            cid = StringUtil.serverHTMLStripper(cid);
            userid = StringUtil.serverHTMLStripper(userid);

            if(StringUtil.isNullOrEmpty(cid) || StringUtil.isNullOrEmpty(userid)) {
                return "0";
            }

			pstmt = conn
					.prepareStatement(" insert into uncheckedcalmap (userid, cid) values (?, ?) ");
			pstmt.setString(1, userid);
			pstmt.setString(2, cid);

			int r = pstmt.executeUpdate();

            return ((r != 0) ? cid : "0");
		} catch (SQLException e) {
            KrawlerLog.calendar.warn("Tree.insertNewCalendar::insertExportCheck:" + e);
			throw ServiceException.FAILURE(
					"Tree.insertNewCalendar::insertExportCheck", e);
		} finally {
			DbPool.closeStatement(pstmt);
		}
    }

    /**
     * Updates export check so that the specific calendar is not exported for the specific user
     * @param conn connection object used for performing database operations
     * @param cid id of calendar not to be exported
     * @param userid id of user for whom this calendar is not to be exported
     * @param exportCal previous user's setting of calendar export
     * @return 0 for successful updation
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
    public static String updateExportCheck(Connection conn, String cid, String userid, boolean exportCal) throws ServiceException {
		PreparedStatement pstmt = null;
		try {
            cid = StringUtil.serverHTMLStripper(cid);
            userid = StringUtil.serverHTMLStripper(userid);
            ResultSet rs = null;
            int r = 0;

            if(StringUtil.isNullOrEmpty(cid) || StringUtil.isNullOrEmpty(userid)) {
                return "0";
            }
            if(exportCal){
                pstmt = conn
                                .prepareStatement(" select COUNT(*) as count from uncheckedcalmap where userid = ? and cid = ? ");
                pstmt.setString(1, userid);
                pstmt.setString(2, cid);
                rs = pstmt.executeQuery();

                if(rs.next()){
                    if(rs.getInt("count") > 0){
                        pstmt = conn
                                        .prepareStatement(" delete from uncheckedcalmap where userid = ? and cid = ? ");
                        pstmt.setString(1, userid);
                        pstmt.setString(2, cid);
                        r = pstmt.executeUpdate();
                    }
                }
            }
            else if(!exportCal){
                pstmt = conn
                                .prepareStatement(" select COUNT(*) as count from uncheckedcalmap where userid = ? and cid = ? ");
                pstmt.setString(1, userid);
                pstmt.setString(2, cid);
                rs = pstmt.executeQuery();

                if(rs.next()){
                    if(rs.getInt("count") == 0){
                        pstmt = conn
                                        .prepareStatement(" insert into uncheckedcalmap (userid, cid) values (?, ?) ");
                        pstmt.setString(1, userid);
                        pstmt.setString(2, cid);
                        r = pstmt.executeUpdate();
                    }
                }
            }

            return "0";
		} catch (SQLException e) {
            KrawlerLog.calendar.warn("Tree.insertNewCalendar::updateExportCheck:" + e);
			throw ServiceException.FAILURE(
					"Tree.insertNewCalendar::updateExportCheck", e);
		} finally {
			DbPool.closeStatement(pstmt);
		}
    }

    /**
     * Fetches the project name, name and id of the calendar
     * @param conn connection object used for performing database operations
     * @param userid id of user for whose export calendar settings the calendars are to be fetched
     * @return project name, name and id of the calendar for the specific user's export calendar settings
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
    public static String getUserExportData(Connection conn, String userid, String companyid) throws ServiceException {
		PreparedStatement pstmt = null;
        String json = null;
		try {
            userid = StringUtil.serverHTMLStripper(userid);
            ResultSet rs = null;
            KWLJsonConverter kjs = new KWLJsonConverter();

            pstmt = conn
                            .prepareStatement(" select projectname, cname, cid from project inner join calendars " +
                            " on project.projectid = calendars.userid where projectid IN " +
                            " (select projectid from projectmembers where userid = ? and inuseflag = 1 and status in (3,4,5) ) " +
                            " and cid NOT IN (select cid from uncheckedcalmap union select cid from networkcalendars where companyid = ?) order by projectname, cname ");
            pstmt.setString(1, userid);
            pstmt.setString(2, companyid);
            rs = pstmt.executeQuery();

			json = kjs.GetJsonForGrid(rs);
        } catch (SQLException e) {
            KrawlerLog.calendar.warn("Tree.insertNewCalendar::getUserExportData:" + e);
			throw ServiceException.FAILURE(
					"Tree.insertNewCalendar::getUserExportData", e);
		} finally {
			DbPool.closeStatement(pstmt);
		}
        return json;
    }

    /**
     * Fetches id and name of the calendar of the specific id
     * @param conn connection object used for performing database operations
     * @param cid id of calendar whose id and name are to fetched
     * @return id and name of the calendar
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
    public static String getCalendarName(Connection conn, String cid)
			throws ServiceException {
		PreparedStatement pstmt = null;
		String calName = "";
		try {
			ResultSet rs = null;
			pstmt = conn
					.prepareStatement("select calendars.cid, calendars.cname from calendars where cid = ?");
			pstmt.setString(1, cid);
			rs = pstmt.executeQuery();
			if (rs.next()) {
                calName = rs.getString("cname");
            }
			return calName;
		} catch (SQLException e) {
            KrawlerLog.calendar.warn("Tree.insertNewCalendar::getCalendarName:" + e);
			throw ServiceException.FAILURE("Tree.getCalendarName error", e);
		} finally {
			DbPool.closeStatement(pstmt);
		}
	}

    /**
     * To fetch id of the project with which the calendar is associated
     * @param conn connection object used for performing database operations
     * @param calId id of the calendar of which the details are to be fetched
     * @return String containing the id of the project
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static String getProjectId(Connection conn, String calId)
			throws ServiceException {

		String projectId = "";
		PreparedStatement calPS = null;

		try {
			String query = null;
			ResultSet rs = null;
			query = " SELECT userid FROM calendars WHERE cid = ? ";
			calPS = conn.prepareStatement(query);
			calPS.setString(1, calId);
			rs = calPS.executeQuery();
            if(rs.next()) {
                projectId = rs.getString("userid");
            }

		} catch (SQLException e) {
            KrawlerLog.calendar.warn("calEvent.getEventDetails:" + e);
			throw ServiceException.FAILURE("calEvent.getEventDetails:", e);

		} finally {
			DbPool.closeStatement(calPS);
            return projectId;
		}
	}

    /**
     * Fetches the last modified events for a specific project (user for subscribing to RSS Feeds)
     * @param conn connection object used for performing database operations
     * @param userid id of the user
     * @param projid id of the project
     * @return list of events specified along with the calendar and project name
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
    public static List getUserProjEvents(Connection conn, String userid, String projid, String link)
                    throws ServiceException {
        ArrayList list = new ArrayList();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int limit = 15;
        int offset = 0;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-d HH:mm:ss");
        java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd h:mm a");
        java.text.SimpleDateFormat sdf2 = new java.text.SimpleDateFormat("yyyy-MM-dd");
        try {
            String startts = "";
            String endts = "";
            pstmt = conn
                            .prepareStatement(" select calendarevents.eid, project.projectname, concat(calendarevents.subject,' [Calendar : ',calendars.cname,']') as subject , " +
                            " calendarevents.descr, calendarevents.location, calendarevents.startts, calendarevents.endts, calendarevents.timestamp, calendarevents.allday, now() as dtstamp " +
                            " from calendarevents inner join calendars on calendars.cid = calendarevents.cid inner join project on project.projectid = calendars.userid " +
                            " where project.projectid in " +
                            " ( select project.projectid from project inner join projectmembers on projectmembers.projectid = project.projectid where " +
                            " projectmembers.inuseflag = 1 and projectmembers.status in (3,4,5) and userid = ? and project.projectid = ?) order by calendarevents.timestamp desc LIMIT ? OFFSET ?");
            pstmt.setString(1, userid);
            pstmt.setString(2, projid);
            pstmt.setInt(3, limit);
            pstmt.setInt(4, offset);
            rs = pstmt.executeQuery();

            while(rs.next())
            {
                String timezone = "+00:00";
                timezone = Timezone.getTimeZone(conn, userid);
                timezone = timezone.substring(0,4) + "00";      // [Temp fix] rounding of the 1/2 hour time zones as they are not yet supported
                
                RssFeedItem feed = new RssFeedItem();
                feed.setLink(link);
                boolean allday = rs.getBoolean("allday");
                if(!allday)
                    allday = projdb.isTask(conn, rs.getString("eid"));
                startts = rs.getString("startts");
                startts = Timezone.toUserDefTimezone(conn, startts, timezone);
                java.util.Date dt = sdf.parse(startts);
                startts = (allday) ? sdf2.format(dt) : sdf1.format(dt);

                endts = rs.getString("endts");
                endts = Timezone.toUserDefTimezone(conn, endts, timezone);
                dt = sdf.parse(endts);
                endts = (allday) ? sdf2.format(dt) : sdf1.format(dt);

                String title = "Event - "+rs.getString("subject");
                String description ="Project : "+rs.getString("projectname");
                if(rs.getString("descr").length() > 0)
                    description +=" | Description : "+rs.getString("descr");
                description+= " | Start : "+startts + " | End : "+endts;
                feed.setTitle(title);
                String publishTime = rs.getObject("timestamp").toString();
                publishTime = Timezone.toUserDefTimezone(conn, publishTime, timezone);
                feed.setPublishedDate(publishTime);
                feed.setDescription(description);
                list.add(feed);
            }
        } catch (SQLException ex) {
            DbPool.closeStatement(pstmt);
            KrawlerLog.calendar.warn("Tree.getUserProjEvents::" + ex);
            throw ServiceException.FAILURE("Tree.getUserProjEvents", ex);
        } finally {
            return list;
        }
    }

    /**
     * Fetches last modified events of a specific calendar
     * @param conn connection object used for performing database operations
     * @param cid id of calendar whose events are to be fetched
     * @param link link to be used for RSS Feed
     * @return list of events of the specified calendar
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
    public static List getUserCalEvents(Connection conn, String cid, String link, String userid)
                    throws ServiceException {
        ArrayList list = new ArrayList();
        PreparedStatement pstmt = null;
        int limit = 15;
        int offset = 0;
        ResultSet rs = null;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-d HH:mm:ss");
        java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd h:mm a");
        java.text.SimpleDateFormat sdf2 = new java.text.SimpleDateFormat("yyyy-MM-dd");
        try {
            String startts = "";
            String endts = "";

            pstmt = conn
                            .prepareStatement(" select calendarevents.eid, project.projectname, concat(calendarevents.subject,' " +
                            " [Calendar : ',calendars.cname,']') as subject , calendarevents.descr, calendarevents.location, " +
                            " calendarevents.startts, calendarevents.endts, calendarevents.timestamp, calendarevents.allday, now() as dtstamp from calendarevents " +
                            " inner join calendars on calendars.cid = calendarevents.cid inner join project " +
                            " on project.projectid = calendars.userid where calendarevents.cid = ? order by calendarevents.timestamp desc LIMIT ? OFFSET ?");
            pstmt.setString(1, cid);
            pstmt.setInt(2, limit);
            pstmt.setInt(3, offset);
            rs = pstmt.executeQuery();
            while(rs.next())
            {
                String timezone = "+00:00";
                timezone = Timezone.getTimeZone(conn, userid);
                timezone = timezone.substring(0,4) + "00";      // [Temp fix] rounding of the 1/2 hour time zones as they are not yet supported
                RssFeedItem feed = new RssFeedItem();
                feed.setLink(link);
                boolean allday = rs.getBoolean("allday");
                if(!allday)
                    allday = projdb.isTask(conn, rs.getString("eid"));
                startts = rs.getString("startts");
                startts = Timezone.toUserDefTimezone(conn, startts, timezone);
                java.util.Date dt = sdf.parse(startts);
                startts = (allday) ? sdf2.format(dt) : sdf1.format(dt);

                endts = rs.getString("endts");
                endts = Timezone.toUserDefTimezone(conn, endts, timezone);
                dt = sdf.parse(endts);
                endts = (allday) ? sdf2.format(dt) : sdf1.format(dt);

                String title = "Event - "+rs.getString("subject");
                String description ="Project : "+rs.getString("projectname");
                if(rs.getString("descr").length() > 0)
                    description +=" | Description : "+rs.getString("descr");
                description+= " | Start : "+startts + " | End : "+endts;
                feed.setTitle(title);
                String publishTime = rs.getObject("timestamp").toString();
                publishTime = Timezone.toUserDefTimezone(conn, publishTime, timezone);
                feed.setPublishedDate(publishTime);
                feed.setDescription(description);
                list.add(feed);
            }
        } catch (SQLException ex) {
            DbPool.closeStatement(pstmt);
            KrawlerLog.calendar.warn("Tree.getUserCalEvents:" + ex);
            throw ServiceException.FAILURE("Tree.getUserCalEvents", ex);
        } finally {
            return list;
        }
    }

    public static String sendNotification(Connection conn, HttpServletRequest request)
                throws ServiceException, SessionExpiredException, JSONException{
            String currentDate[] = request.getParameter("currentdate").split(",");
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-d");
            java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String result = "true";
            int resourceCnt=0;
            String tid="";
            int auditMode=0;
            String projectid = request.getParameter("projectid").substring(0, request.getParameter("projectid").indexOf("calctrl"));
            String projname =projdb.getProjectName(conn, projectid);
            PreparedStatement pstmt = null;
            PreparedStatement pstmt1 = null;
            PreparedStatement pstmtCnt = null;
            ResultSet rs = null;
            ResultSet rs1 = null;
            ResultSet rsCnt = null;
            java.util.Date dt;
            java.util.Date dt1;
            String eventquery = "";
            String calendar = request.getParameter("calid");
            String calendarid[] = calendar.split(",");
            String stdate = "";
            String edate = "";
            String loginid = AuthHandler.getUserid(request);
            String companyid = AuthHandler.getCompanyid(request);
            boolean type = projdb.isEmailSubscribe(conn, companyid);
            if(type){
                String subdomain = CompanyHandler.getCompanySubdomainByCompanyID(conn, companyid);
                String reciver = "";
                String receiverquery =  "Select concat(fname,' ',lname) as username,emailid,users.userid as receiverid,userlogin.username as reciver" +
                                        " from users inner join userlogin on users.userid = userlogin.userid inner join projectmembers on projectmembers.userid = users.userid " +
                                        " where projectmembers.projectid = ? AND projectmembers.userid != ?";
                String userquery = "select concat(fname,' ',lname) as username,emailid,userid from users where userid = ?";
                try{
                    if(request.getParameter("subaction").equals("1")){
                      stdate = Timezone.getUserToGmtTimezone(conn, loginid, currentDate[0]);
                      dt = sdf1.parse(stdate);
                      Calendar c1 = Calendar.getInstance();
                      c1.setTime(dt);
                      c1.add(Calendar.DATE, 1);
                      edate = sdf1.format(c1.getTime());
                    } else if(request.getParameter("subaction").equals("2") || request.getParameter("subaction").equals("3")){
                        stdate = Timezone.getUserToGmtTimezone(conn, loginid, currentDate[0]);
                        edate = Timezone.getUserToGmtTimezone(conn, loginid, currentDate[1]);
                    }
                    eventquery = "SELECT calendarevents.subject, calendarevents.cid, cname, descr ,startts," +
                                 " endts, calendarevents.location, priority, allDay FROM calendarevents" +
                                 " INNER JOIN calendars ON calendars.cid =  calendarevents.cid " +
                                 "WHERE calendars.userid = ? AND calendarevents.cid = ?" +
                                 " AND calendars.cid NOT IN (SELECT cid FROM networkcalendars WHERE companyid = ?)" +
                                 " AND ( (startts BETWEEN ? AND ?) OR " +
                                 "(endts BETWEEN ? AND ?) OR (startts < ? AND ? < endts) )";
                    String resourceCountQry = " select count(*) as count from ("+receiverquery+") as tmp ";
                    pstmt = conn.prepareStatement(userquery);
                    pstmt.setString(1, AuthHandler.getUserid(request));
                    rs = pstmt.executeQuery();
                    rs.next();
                    String sendername = rs.getString("username");
//                    String sendermailid = rs.getString("emailid");
//                    String sendermailid = KWLErrorMsgs.notificationSenderId;
                    String sendermailid = CompanyHandler.getSysEmailIdByCompanyID(conn, companyid);
                    rs.close();
                    pstmt.close();
                    String username = "";
                    String mailid = "";
                    String receiverid = "";
                    pstmt = conn.prepareStatement(receiverquery);
                    pstmt.setString(1, projectid);
                    pstmt.setString(2, loginid);
                    rs = pstmt.executeQuery();
                    pstmtCnt = conn.prepareStatement(resourceCountQry);
                    pstmtCnt.setString(1, projectid);
                    pstmtCnt.setString(2, loginid);
                    rsCnt = pstmtCnt.executeQuery();
                    if(rsCnt.next()){
                        resourceCnt = rsCnt.getInt("count");
                    }
                    while(rs.next()){
                            username = rs.getString("username");
                            mailid = rs.getString("emailid");
                            reciver = rs.getString("reciver");
                            receiverid = rs.getString("receiverid");
                            String pmsg = String.format("Hi %s,",username);

                            String htmlmsg = String
                                    .format("<html><head><title>Notification - events in <b>%s</b> Team Calendar </title></head><style type='text/css'>"
                                            + "a:link, a:visited, a:active {\n"
                                            + " 	color: #03C;"
                                            + "}\n"
                                            + "body {\n"
                                            + "	font-family: tahoma, Helvetica, sans-serif;"
                                            + "	color: #000;"
                                            + "	font-size: 13px;"
                                            + "}\n"
                                            + "</style><body>"
                                            + "	<div>"
                                            + "		<p>Hi <strong>%s</strong>,</p>",projname,username);
                            String htmlmsg1="";
                            String pmsg1="";
                            int count = 1;
                            String startdateString ="";
                            String enddateString ="";
                            String df ="E, MMM dd, yyyy";
                            for(int x = 0; x < calendarid.length; x++){
                                    String calname = getCalendarName(conn, calendarid[x]);
                                    String pmsg2 = String.format("\nThe following events are present in your %s Team Calendar:",calname);
                                    String htmlmsg2 = String.format("<p>The following events are present in your %s Team Calendar:</p>",calname);
                                    pstmt1 = conn.prepareStatement(eventquery);
                                    pstmt1.setString(1, projectid);
                                    pstmt1.setString(2, calendarid[x]);
                                    pstmt1.setString(3, companyid);
                                    pstmt1.setString(4, stdate);
                                    pstmt1.setString(5, edate);
                                    pstmt1.setString(6, stdate);
                                    pstmt1.setString(7, edate);
                                    pstmt1.setString(8, stdate);
                                    pstmt1.setString(9, edate);
                                    rs1 = pstmt1.executeQuery();
                                    if(rs1.next()){
                                        pmsg+=pmsg2;
                                        htmlmsg+=htmlmsg2;
                                        rs1.beforeFirst();
                                        while(rs1.next()){
                                            String location = "";
                                           // timezone = Timezone.getTimeZone(conn, loginid);
                                            if(!rs1.getString("location").equals("")){
                                                location = rs1.getString("location");
                                            } else {
                                                location = "Not Specified";
                                            }
                                            String pri = "";
                                            if(rs1.getString("priority").equals("h")){
                                                pri = "High";
                                            } else if(rs1.getString("priority").equals("m")){
                                                pri = "Normal";
                                            } else if(rs1.getString("priority").equals("l")){
                                                pri= "Low";
                                            }
                                            if(!rs1.getBoolean("allDay")) {
                                                startdateString = rs1.getString("startts");
                                                startdateString = dateFormatHandlers.getUserPrefreanceDate(df,sdf1.parse(startdateString));
                                                enddateString = rs1.getString("endts");
                                                enddateString = dateFormatHandlers.getUserPrefreanceDate(df,sdf1.parse(enddateString));
                                            } else{
                                                startdateString = dateFormatHandlers.getUserPrefreanceDate(df,rs1.getTimestamp("startts"));
                                                Date edt= rs1.getTimestamp("endts");
                                                Calendar cal = Calendar.getInstance();
                                                cal.setTime(edt);
                                                cal.add(Calendar.DATE, -1);
                                                edt = cal.getTime();
                                                enddateString = dateFormatHandlers.getUserPrefreanceDate(df,edt);
                                            }

                                            
                                            pmsg1 = String.format("\n %s) %s - %s\n\nStart Date: %s\nEnd Date: %s\nLocation: %s",
                                                    count,rs1.getString("subject"),rs1.getString("descr"),startdateString,
                                                    enddateString, location);
                                            htmlmsg1 = String.format("<p><strong> %s) %s</strong> -%s</p><div style ='margin-left:10px;'><p>Start Date: %s</p><p>End Date: %s</p>" +
                                                    "<p>Priority: %s</p><p>Location: %s</p></div>",
                                                    count++,rs1.getString("subject"),rs1.getString("descr"),startdateString,
                                                    enddateString, pri, location);
                                            pmsg+=pmsg1;
                                            htmlmsg+=htmlmsg1;
                                        }
                                    } 
                            }
                            if(count > 1){
                                   try {
                                            String uri = URLUtil.getPageURL(request, Links.loginpageFull, subdomain);
                                            htmlmsg1 = String.format("<p>You can log in at:</p><p><a href = %s>%s</a></p><br/><p> - %s </p></div></body></html>",uri,uri,sendername);
                                            pmsg1 = String.format("\n\nYou can log in at:\n%s\n\n - %s ",uri,sendername);
                                            String insertMsg = Mail.insertMailMsg(conn, reciver, loginid, "Event Notification", htmlmsg, "1", false, "1", "", "newmsg", "", 3, "", companyid);
                                            pmsg+=pmsg1;
                                            htmlmsg+=htmlmsg1;
                                            SendMailHandler.postMail(new String[] { mailid},
                                                            "Event Notification",htmlmsg, pmsg,
                                                                sendermailid);
                                       } catch (ConfigurationException e) {
                                              result = "false";
                                              e.printStackTrace();
                                       } catch (MessagingException e) {
                                              result = "false";
                                              e.printStackTrace();
                                       } catch(java.text.ParseException e){
                                              result = "false";
                                              e.printStackTrace();
                                       } catch(JSONException e){
                                              result = "false";
                                              e.printStackTrace();
                                       }
                            } else{
                                result = "noEvent";
                            }
                            rs1.close();
                            pstmt1.close();
                       }
                            String userFullName = AuthHandler.getAuthor(conn, loginid);
                            String userName = AuthHandler.getUserName(request);
                            String params = userFullName + " (" + userName + "), " + resourceCnt+", calender";
                            String ipAddress = AuthHandler.getIPAddress(request);
                            AuditTrail.insertLog(conn, "410", loginid, tid, projectid, companyid, params, ipAddress, auditMode);
                       rs.close();
                } catch (SQLException e) {
                        result = "false";
                        throw ServiceException.FAILURE("todolist.getToDoDetails", e);
                } catch (ServiceException ex) {
                        DbPool.quietRollback(conn);
                        System.out.println("calEvent:Connection Error:"+ex.toString());
                } catch (ParseException ex) {
                        Logger.getLogger(Tree.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                        DbPool.closeStatement(pstmt);
                }
        } else{
              result = "typeError";
        }
        return result;
    }

}
