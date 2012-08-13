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
import java.util.UUID;

import com.krawler.common.timezone.Timezone;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.KrawlerLog;
import com.krawler.database.DbPool;
import com.krawler.database.DbPool.Connection;
import com.krawler.utils.json.KWLJsonConverter;
import com.krawler.common.util.StringUtil;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.esp.servlets.importICSServlet;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Utility method for working calendar events.
 */
public class calEvent {

    /**
     * Fetches the events of the specific calendars in that time period.
     * @param conn connection object used for performing database operations
     * @param cid array of calendar ids'
     * @param viewdt1 start date from when to fetch the events
     * @param viewdt2 end date till when to fetch the events
     * @param loginid user id, used to get the time zone
     * @return object containing all the events
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
    public static Object fetchEvent(Connection conn, String[] cid,
			String viewdt1, String viewdt2, String loginid) throws ServiceException {
		Object jobj = null;
		PreparedStatement calPS = null;
		try {
			String query = null;
            ResultSet rs = null;
            boolean projectPlanEvent = true;
            JSONObject resobj = new JSONObject();
            JSONArray resarr = new JSONArray();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String para = new String();
            String startts = "";
            String endts = "";
            String timezone = "+00:00";

            timezone = Timezone.getTimeZone(conn, loginid);

			for (int i = 0; i < cid.length; i++) {
				para += "?";
				if ((i + 1) != cid.length)
					para += ",";
			}
            query = " select calendarevents.* " +
                    " from calendarevents where calendarevents.cid IN ( select cid from calendars ) and " +
                    " calendarevents.cid IN(" + para + ") and ((startts >= ? and startts <= ?) or (endts >= ? and endts <= ?) " +
                    " or (startts <= ? and endts >= ?)) " +
                    " ORDER BY calendarevents.startts ASC, calendarevents.endts - calendarevents.startts DESC ";
            calPS = conn.prepareStatement(query);
            int j = 0;
			for (int i = 0; i < (cid.length); i++) {
                calPS.setString(++j, cid[i]);
			}
            Calendar vdtCal1 = Calendar.getInstance();
            vdtCal1.setTime(sdf.parse(viewdt1));

            Calendar vdtCal2 = Calendar.getInstance();
            vdtCal2.setTime(sdf.parse(viewdt2));

            vdtCal1.add(Calendar.DATE, -1);   // Fetching events from (viewDt_Start - 1day) to (viewDt_End + 1day)
            vdtCal2.add(Calendar.DATE, 1);   // for checking whether events after conversion to time zone lie

            String vdt1Str = sdf.format(vdtCal1.getTime()).toString();           // in the specified range
            String vdt2Str = sdf.format(vdtCal2.getTime()).toString();

            vdtCal1.add(Calendar.DATE, 1);   // Fetching events from (viewDt_Start - 1day) to (viewDt_End + 1day)
            vdtCal2.add(Calendar.DATE, -1);   // for checking whether events after conversion to time zone lie

			calPS.setObject(j + 1, vdt1Str);
			calPS.setObject(j + 2, vdt2Str);
			calPS.setObject(j + 3, vdt1Str);
			calPS.setObject(j + 4, vdt2Str);
            calPS.setObject(j + 5, vdt1Str);
			calPS.setObject(j + 6, vdt2Str);
			rs = calPS.executeQuery();
            while(rs.next()) {
                if(!rs.getBoolean("allDay")) {
                    projectPlanEvent = projdb.isTask(conn, rs.getString("eid"));
                        timezone = timezone.substring(0,4) + "00";
                        startts = rs.getString("startts");
                        startts = Timezone.toUserDefTimezone(conn, startts, timezone);
                        endts = rs.getString("endts");
                        endts = Timezone.toUserDefTimezone(conn, endts, timezone);
                        if(projectPlanEvent){
                            String projid = Tree.getProjectId(conn, cid[0]);
                            String projectWorkTime = projdb.getProjectWorkTime(conn, projid);
                            String projdate[] = projectWorkTime.split(",");
                            startts = startts.split(" ")[0]+" "+projdate[0];
                            endts = endts.split(" ")[0]+" "+projdate[1];
                        } 
                } else {
                     startts = rs.getString("startts");
                     endts = rs.getString("endts");
                }
                Calendar stCalDt = Calendar.getInstance();
                stCalDt.setTime(sdf.parse(startts));
                Calendar enCalDt = Calendar.getInstance();
                enCalDt.setTime(sdf.parse(endts));
                if(!((vdtCal1.compareTo(stCalDt) <= 0 && vdtCal2.compareTo(stCalDt) >= 0) || (vdtCal1.compareTo(enCalDt) <= 0 && vdtCal2.compareTo(enCalDt) >= 0) || (stCalDt.compareTo(vdtCal1) <= 0 && enCalDt.compareTo(vdtCal2) >= 0)))
                    continue;

                String eventData = BreakEvent(rs.getString("eid"), rs.getString("cid"), startts, endts, rs.getString("subject"),
                    rs.getString("descr"), rs.getString("location"), rs.getString("showas"), rs.getString("priority"), rs.getString("recpattern"),
                    rs.getString("recend"), rs.getString("resources"), rs.getString("timestamp"), rs.getBoolean("allday"), "0").toString();

                JSONObject resbrk = new JSONObject(eventData);
                if(resbrk.toString().compareTo("{}") != 0){
                    resarr = resbrk.getJSONArray("data");
                    for(int cnt = 0; cnt< resarr.length(); cnt++) {
                        resobj.append("data", resarr.getJSONObject(cnt));
                    }
                }
            }
            for(int c = 0; c < cid.length; c++) {
                String id = cid[c];
                String data = fetchICalEvents(id);
                if(data.compareTo("") != 0){
                    JSONObject j1 = new JSONObject(data);
                    for(int i=0; i<j1.getJSONArray("data").length(); i++){

                        JSONObject rs1 = j1.getJSONArray("data").getJSONObject(i);
                        String eventData = BreakEvent(rs1.getString("eid"), rs1.getString("cid"), rs1.getString("startts"), rs1.getString("endts"), rs1.getString("subject"),
                            rs1.getString("descr"), rs1.getString("location"), rs1.getString("showas"), rs1.getString("priority"), rs1.getString("recpattern"),
                            rs1.getString("recend"), rs1.getString("resources"), rs1.getString("timestamp"), rs1.getBoolean("allday"), "1").toString();

                        JSONObject resbrk = new JSONObject(eventData);
                        if(resbrk.toString().compareTo("{}") != 0){
                            resarr = resbrk.getJSONArray("data");
                            for(int cnt = 0; cnt< resarr.length(); cnt++) {
                                resobj.append("data", resarr.getJSONObject(cnt));
                            }
                        }
                    }
                }
            }
            if(resobj.has("data")) {
                jobj = resobj.toString();
            }
            else {
                jobj = "{\"data\":{}}";
            }
			return jobj;
        } catch (SQLException e) {
            KrawlerLog.calendar.warn("calEvent.fetchEvent():" + e);
			throw ServiceException.FAILURE("calEvent.fetchEvent::fetch error",e);
		} catch (ParseException e) {
            KrawlerLog.calendar.warn("calEvent.fetchEvent():" + e);
			throw ServiceException.FAILURE("calEvent.fetchEvent::fetch error",e);
		} catch (JSONException e) {
            KrawlerLog.calendar.warn("calEvent.fetchEvent():" + e);
			throw ServiceException.FAILURE("calEvent.fetchEvent::fetch error",e);
		} catch (ServiceException e) {
            KrawlerLog.calendar.warn("calEvent.fetchEvent():" + e);
			throw ServiceException.FAILURE("calEvent.fetchEvent():" + e.getMessage(), e);
		} catch (Exception e) {
            KrawlerLog.calendar.warn("calEvent.fetchEvent():" + e);
			throw ServiceException.FAILURE("calEvent.fetchEvent::fetch error",e);
        } finally {
			DbPool.closeStatement(calPS);
		}
	}

     /**
     * Fetches the events of the internet calendars
     * @param conn connection object used for performing database operations
     * @param cid calendar id to be checked for being a network calendar and fetched events of
     * @return String containing all the events
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
    private static String fetchICalEvents(String id) throws ServiceException {
        String data = "";
        DbResults rs = DbUtil.executeQuery("SELECT url, TIME_TO_SEC(TIMEDIFF(nextsync, lastsync))/60 as syncinterval FROM networkcalendars WHERE cid = ?", id);
        if(rs.next()){
            String calUrl = rs.getString("url");
            int interval = rs.getInt("syncinterval") * -1;
            net.fortuna.ical4j.model.Calendar cal = importICSServlet.setUpICal(calUrl, id, interval);
            if(cal != null){
                data = importICSServlet.getIEventJson(cal, id);
            }
        }
        return data;
    }

    /**
     * Breaks the events day wise
     * @param viewdt1 start date from where the events are to be fetched
     * @param viewdt2 end date till where the events are to be fetched
     * @param eid event id
     * @param cid calendar id
     * @param startts event's start date
     * @param endts event's end date
     * @param subject subject of the event
     * @param descr description of the event
     * @param location location of the event
     * @param showas event's property
     * @param priority event's property
     * @param recpattern event's property
     * @param recend event's property
     * @param resources resources which the event requires
     * @param timestamp when this event was last modified
     * @param allday All day flag
     * @return object containing the event broken day-wise
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
    public static Object BreakEvent(String eid, String cid, String startts, String endts,
        String subject, String descr, String location, String showas, String priority, String recpattern, String recend,
        String resources, String timestamp, Boolean allday, String deleteFlag) throws ServiceException {
        com.krawler.utils.json.base.JSONObject resobj = new com.krawler.utils.json.base.JSONObject();
        try {
            int cntDays = 0;
            int flagEvent = -1;

            Calendar startdt = Calendar.getInstance();
            Calendar enddt = Calendar.getInstance();

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:00");

            startdt.setTime(sdf.parse(startts));
            enddt.setTime(sdf.parse(endts));

            cntDays = getDaysDiff(startts, endts);

            if(allday || ((startdt.compareTo(enddt) != 0) && !(enddt.get(Calendar.HOUR_OF_DAY) == 0 && cntDays < 1))) {
                if(allday && (cntDays == 1)) {
                    flagEvent = 0;      // Event is a single day all day event, so no "more left/right" image to be shown
                }
                else {
                    flagEvent = 1;      // Event spans over multiple days
                }
            }

            if(allday) {
                enddt.add(Calendar.DATE, -1);
                endts = sdf.format(enddt.getTime());
                cntDays--;
            }
            if(!allday && enddt.get(Calendar.HOUR_OF_DAY) == 0) {
                cntDays--;
            }

            for(int cnt = 0; cnt<= cntDays; cnt++) {
                if(cnt > 0 && cnt == (cntDays-1)) {
                    flagEvent = 3;      // Last day of the event, so "more left" to be shown
                }
                else if(cnt > 0) {
                    flagEvent = 2;      // Other days of the event so "more right" to be shown
                }

                com.krawler.utils.json.base.JSONObject temp = new com.krawler.utils.json.base.JSONObject();
                temp.put("eid", eid + (cnt>0? "CNT_"+ cnt : ""));   // If count greated than 0, then append 'CNT_count" to the id the event
                temp.put("peid", eid);
                temp.put("cid", cid);
                temp.put("startts", startts);
                temp.put("endts", endts);
                temp.put("subject", subject);
                temp.put("descr", descr);
                temp.put("location", location);
                temp.put("showas", showas);
                temp.put("priority", priority);
                temp.put("recpattern", recpattern);
                temp.put("recend", recend);
                temp.put("resources", resources);
                temp.put("timestamp", timestamp);
                temp.put("allday", allday);
                temp.put("flagEvent", flagEvent);
                temp.put("deleteFlag", deleteFlag);
                resobj.append("data", temp);
                flagEvent = 0;
            }
        } catch (ParseException e) {
            KrawlerLog.calendar.warn("calEvent.breakEvent():" + e);
			throw ServiceException.FAILURE("calEvent.breakEvent::break error", e);
		} catch (JSONException e) {
            KrawlerLog.calendar.warn("calEvent.breakEvent():" + e);
			throw ServiceException.FAILURE("calEvent.breakEvent::break error", e);
        } finally {
			return resobj;
		}
    }

    /**
     * Returns the difference in days
     * @param Estartts start date
     * @param Eendts end date
     * @return differnce in dates
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
    public static int getDaysDiff(String Estartts, String Eendts) throws ServiceException {
        double days = 0;
        double diffInMilleseconds = 0;
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:00");
            java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd 00:00:00");

            if(Estartts.compareTo("") != 0 && Eendts.compareTo("") != 0) {
                java.util.Date sdt = sdf.parse(Estartts);
                java.util.Date edt = sdf.parse(Eendts);

                Estartts = sdf1.format(sdt);
                Eendts = sdf1.format(edt);

                java.util.Date dt1 = sdf1.parse(Estartts);
                java.util.Date dt2 = sdf1.parse(Eendts);

                diffInMilleseconds = dt2.getTime() - dt1.getTime();
                days = Math.round(diffInMilleseconds / (1000 * 60 * 60 * 24));
            }
        } catch (ParseException ex) {
            days = 0;
            KrawlerLog.calendar.warn("calEvent.getDayDiff():" + ex);
			throw ServiceException.FAILURE("calEvent.getDayDiff::get diff error", ex);
        } finally {
            return (int) days;
        }
    }

    /**
     * To create new event (not an all day event)
     * @param conn connection object used for performing database operations
     * @param cid calendar id
     * @param startts event's start date
     * @param endts event's end date
     * @param subject subject of the event
     * @param descr description of the event
     * @param location location of the event
     * @param showas event's property
     * @param priority event's property
     * @param recpattern event's property
     * @param recend event's property
     * @param resources resources which the event requires
     * @param userid required to get user's time zone
     * @param companyid required to get the time zone of the company incase the user's time zone is not available
     * @return the newly created event's id
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static String insertEvent(Connection conn, String cid,
			String startts, String endts, String subject, String descr,
			String location, String showas, String priority, String recpattern,
			String recend, String resources, String userid, String companyid, String eventId) throws ServiceException {
		int r = 0;
		String result = "";
        String timezone = "+00:00";
		PreparedStatement calPS = null;
        ResultSet rs = null;
		try {
            cid = StringUtil.serverHTMLStripper(cid);
            startts = StringUtil.serverHTMLStripper(startts);
            endts = StringUtil.serverHTMLStripper(endts);
            if(subject.length() > 1023)
                subject = StringUtil.serverHTMLStripper(subject).substring(0, 1023);
            else
                subject = StringUtil.serverHTMLStripper(subject);
            descr = StringUtil.serverHTMLStripper(descr);
            if(location.length() > 1023)
                location = StringUtil.serverHTMLStripper(location).substring(0, 1023);
            else
                location = StringUtil.serverHTMLStripper(location);
            showas = StringUtil.serverHTMLStripper(showas);
            priority = StringUtil.serverHTMLStripper(priority);
            recpattern = StringUtil.serverHTMLStripper(recpattern);
            recend = StringUtil.serverHTMLStripper(recend);
            resources = StringUtil.serverHTMLStripper(resources);

            if(StringUtil.isNullOrEmpty(cid) || StringUtil.isNullOrEmpty(startts) || StringUtil.isNullOrEmpty(endts) || StringUtil.isNullOrEmpty(subject) || StringUtil.isNullOrEmpty(showas) || StringUtil.isNullOrEmpty(priority)) {
                return "0";
            }
            if(userid.compareToIgnoreCase("0") != 0 && companyid.compareToIgnoreCase("0") != 0) {
                timezone = Timezone.getTimeZone(conn, userid);
                timezone = timezone.substring(0,4) + "00";      // [Temp fix] rounding of the 1/2 hour time zones as they are not yet supported
                if(timezone.startsWith("-"))
                    timezone = "+"+timezone.substring(1);
                else if(timezone.startsWith("+"))
                    timezone = "-"+timezone.substring(1);
                startts = Timezone.toUserDefTimezone(conn, startts, timezone);
                endts = Timezone.toUserDefTimezone(conn, endts, timezone);
//                startts = Timezone.getUserToGmtTimezone(conn, userid, startts);
//                endts = Timezone.getUserToGmtTimezone(conn, userid, endts);
            }
            String query = null;
            query = "insert into calendarevents (eid, cid, startts, endts, subject, descr, location, showas, priority, recpattern, recend, resources, timestamp, allday) values( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), ?)";
            calPS = conn.prepareStatement(query);
            if(eventId.compareTo("") == 0)
                eventId = UUID.randomUUID().toString();
            calPS.setString(1, eventId);
            calPS.setString(2, cid);
            calPS.setString(3, startts);
            calPS.setString(4, endts);
            calPS.setString(5, subject);
            calPS.setString(6, descr);
            calPS.setString(7, location);
            calPS.setString(8, showas);
            calPS.setString(9, priority);
            calPS.setString(10, recpattern);
            calPS.setString(11, recend);
            calPS.setString(12, resources);
            calPS.setBoolean(13, false);
            r = calPS.executeUpdate();
            result = String.valueOf(r);
            if (r > 0) {
                result = eventId;
            }
            return result;
		} catch (SQLException e) {
            KrawlerLog.calendar.warn("calEvent.insertEvent():" + e);
            throw ServiceException.FAILURE("calEvent.insertEvent:", e);
		} finally {
            DbPool.closeStatement(calPS);
		}
	}

    /**
     * To create new event (an all day event)
     * @param conn connection object used for performing database operations
     * @param cid calendar id
     * @param startts event's start date
     * @param endts event's end date
     * @param subject subject of the event
     * @param descr description of the event
     * @param location location of the event
     * @param showas event's property
     * @param priority event's property
     * @param recpattern event's property
     * @param recend event's property
     * @param resources resources which the event requires
     * @return the newly created event's id
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static String insertAllDayEvent(Connection conn, String cid,
			String startts, String endts, String subject, String descr,
			String location, String showas, String priority, String recpattern,
			String recend, String resources, String eventId) throws ServiceException {
		int r = 0;
		String result = "";
		PreparedStatement calPS = null;
		try {
            cid = StringUtil.serverHTMLStripper(cid);
            startts = StringUtil.serverHTMLStripper(startts);
            endts = StringUtil.serverHTMLStripper(endts);
            if(subject.length() > 1023)
                subject = StringUtil.serverHTMLStripper(subject).substring(0, 1023);
            else
                subject = StringUtil.serverHTMLStripper(subject);
            descr = StringUtil.serverHTMLStripper(descr);
            if(location.length() > 1023)
                location = StringUtil.serverHTMLStripper(location).substring(0, 1023);
            else
                location = StringUtil.serverHTMLStripper(location);
            showas = StringUtil.serverHTMLStripper(showas);
            priority = StringUtil.serverHTMLStripper(priority);
            recpattern = StringUtil.serverHTMLStripper(recpattern);
            recend = StringUtil.serverHTMLStripper(recend);
            resources = StringUtil.serverHTMLStripper(resources);
            eventId = StringUtil.serverHTMLStripper(eventId);

            if(StringUtil.isNullOrEmpty(cid) || StringUtil.isNullOrEmpty(startts) || StringUtil.isNullOrEmpty(endts) || StringUtil.isNullOrEmpty(subject) || StringUtil.isNullOrEmpty(showas) || StringUtil.isNullOrEmpty(priority)) {
                return "0";
            }

            String query = null;
            query = "insert into calendarevents (eid,cid,startts,endts,subject,descr,location,showas,priority,recpattern,recend,resources,timestamp,allday) values( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(),?)";
            calPS = conn.prepareStatement(query);
            if(eventId.compareTo("") == 0)
                eventId = UUID.randomUUID().toString();
            calPS.setString(1, eventId);
            calPS.setString(2, cid);
            calPS.setString(3, startts);
            calPS.setString(4, endts);
            calPS.setString(5, subject);
            calPS.setString(6, descr);
            calPS.setString(7, location);
            calPS.setString(8, showas);
            calPS.setString(9, priority);
            calPS.setString(10, recpattern);
            calPS.setString(11, recend);
            calPS.setString(12, resources);
            calPS.setBoolean(13, true);
            r = calPS.executeUpdate();
            result = String.valueOf(r);
            if (r > 0) {
                result = eventId;
            }
            return result;
		} catch (SQLException e) {
            KrawlerLog.calendar.warn("calEvent.insertAllDayEvent():" + e);
            throw ServiceException.FAILURE("calEvent.insertAllDayEvent:", e);
		} finally {
            DbPool.closeStatement(calPS);
		}
	}

    /**
     * To update an event (not an all day event)
     * @param conn connection object used for performing database operations
     * @param cid calendar id
     * @param startts event's start date
     * @param endts event's end date
     * @param subject subject of the event
     * @param descr description of the event
     * @param location location of the event
     * @param showas event's property
     * @param priority event's property
     * @param recpattern event's property
     * @param recend event's property
     * @param resources resources which the event requires
     * @param userid required to get user's time zone
     * @param companyid required to get the time zone of the company incase the user's time zone is not available
     * @return count (only 1) if the guest has been successfully deleted else 0
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static int updateEvent(Connection conn, String cid, String startts,
			String endts, String subject, String descr, String location,
			String showas, String priority, String recpattern, String recend,
			String resources, String eid, String userid, String companyid) throws ServiceException {
		int r = 0;
        String timezone = "+00:00";
		PreparedStatement calPS = null;
        ResultSet rs = null;
		try {
            cid = StringUtil.serverHTMLStripper(cid);
            startts = StringUtil.serverHTMLStripper(startts);
            endts = StringUtil.serverHTMLStripper(endts);
            subject = StringUtil.serverHTMLStripper(subject);
            descr = StringUtil.serverHTMLStripper(descr);
            location = StringUtil.serverHTMLStripper(location);
            showas = StringUtil.serverHTMLStripper(showas);
            priority = StringUtil.serverHTMLStripper(priority);
            recpattern = StringUtil.serverHTMLStripper(recpattern);
            recend = StringUtil.serverHTMLStripper(recend);
            resources = StringUtil.serverHTMLStripper(resources);
            eid = StringUtil.serverHTMLStripper(eid);

            if(StringUtil.isNullOrEmpty(cid) || StringUtil.isNullOrEmpty(startts) || StringUtil.isNullOrEmpty(endts) || StringUtil.isNullOrEmpty(subject) || StringUtil.isNullOrEmpty(showas) || StringUtil.isNullOrEmpty(priority)) {
                return 0;
            }

            timezone = Timezone.getTimeZone(conn, userid);
            timezone = timezone.substring(0,4) + "00";      // [Temp fix] rounding of the 1/2 hour time zones as they are not yet supported
            if(timezone.startsWith("-"))
                timezone = "+"+timezone.substring(1);
            else if(timezone.startsWith("+"))
                timezone = "-"+timezone.substring(1);
            startts = Timezone.toUserDefTimezone(conn, startts, timezone);
            endts = Timezone.toUserDefTimezone(conn, endts, timezone);

			String query = null;
			query = "update calendarevents set cid=?, startts=?, endts=?, subject=?, descr=?, location=?, showas=?,priority=?,recpattern=?,recend=?,resources=?, timestamp=now(), allday=? where eid=?";

			calPS = conn.prepareStatement(query);
			calPS.setString(1, cid);
			calPS.setString(2, startts);
			calPS.setString(3, endts);
			calPS.setString(4, subject);
			calPS.setString(5, descr);
			calPS.setString(6, location);
			calPS.setString(7, showas);
			calPS.setString(8, priority);
			calPS.setString(9, recpattern);
			calPS.setString(10, recend);
			calPS.setString(11, resources);
            calPS.setBoolean(12, false);
			calPS.setString(13, eid);
			r = calPS.executeUpdate();
			return r;
		} catch (SQLException e) {
            KrawlerLog.calendar.warn("calEvent:updateEvent:" + e);
            throw ServiceException.FAILURE("calEvent.updateEvent:", e);
		} finally {
            DbPool.closeStatement(calPS);
		}
	}

    /**
     * To update an event (an all day event)
     * @param conn connection object used for performing database operations
     * @param cid calendar id
     * @param startts event's start date
     * @param endts event's end date
     * @param subject subject of the event
     * @param descr description of the event
     * @param location location of the event
     * @param showas event's property
     * @param priority event's property
     * @param recpattern event's property
     * @param recend event's property
     * @param resources resources which the event requires
     * @return count (only 1) if the guest has been successfully deleted else 0
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static int updateAllDayEvent(Connection conn, String cid, String startts,
			String endts, String subject, String descr, String location,
			String showas, String priority, String recpattern, String recend,
			String resources, String eid) throws ServiceException {
		int r = 0;
		PreparedStatement calPS = null;
		try {
            cid = StringUtil.serverHTMLStripper(cid);
            startts = StringUtil.serverHTMLStripper(startts);
            endts = StringUtil.serverHTMLStripper(endts);
            subject = StringUtil.serverHTMLStripper(subject);
            descr = StringUtil.serverHTMLStripper(descr);
            location = StringUtil.serverHTMLStripper(location);
            showas = StringUtil.serverHTMLStripper(showas);
            priority = StringUtil.serverHTMLStripper(priority);
            recpattern = StringUtil.serverHTMLStripper(recpattern);
            recend = StringUtil.serverHTMLStripper(recend);
            resources = StringUtil.serverHTMLStripper(resources);
            eid = StringUtil.serverHTMLStripper(eid);

            if(StringUtil.isNullOrEmpty(cid) || StringUtil.isNullOrEmpty(startts) || StringUtil.isNullOrEmpty(endts) || StringUtil.isNullOrEmpty(subject) || StringUtil.isNullOrEmpty(showas) || StringUtil.isNullOrEmpty(priority)) {
                return 0;
            }

			String query = null;
			query = "update calendarevents set cid=?, startts=?, endts=?, subject=?, descr=?, location=?, showas=?,priority=?,recpattern=?,recend=?,resources=?, timestamp=now(), allday=? where eid=?";

			calPS = conn.prepareStatement(query);
			calPS.setString(1, cid);
			calPS.setString(2, startts);
			calPS.setString(3, endts);
			calPS.setString(4, subject);
			calPS.setString(5, descr);
			calPS.setString(6, location);
			calPS.setString(7, showas);
			calPS.setString(8, priority);
			calPS.setString(9, recpattern);
			calPS.setString(10, recend);
			calPS.setString(11, resources);
            calPS.setBoolean(12, true);
			calPS.setString(13, eid);
			r = calPS.executeUpdate();
			return r;
		} catch (SQLException e) {
            KrawlerLog.calendar.warn("calEvent:updateAllDayEvent:" + e);
            throw ServiceException.FAILURE("calEvent.updateAllDayEvent:", e);
		} finally {
            DbPool.closeStatement(calPS);
		}
	}

     /**
     * To delete an event
     * @param conn connection object used for performing database operations
     * @param eid id of the event to be deleted
     * @return count (only 1) if the guest has been successfully deleted else 0
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static int deleteEvent(Connection conn, String eid)
			throws ServiceException {
		int r = 0;
		PreparedStatement calPS = null;
		try {
			String query = null;
			query = "delete from calendarevents where eid=?";
			calPS = conn.prepareStatement(query);
			calPS.setString(1, eid);
			r = calPS.executeUpdate();
			return r;
		} catch (SQLException e) {
            KrawlerLog.calendar.warn("calEvent:deleteEvent:" + e);
			throw ServiceException.FAILURE("calEvent.deleteEvent:", e);
		} finally {
			DbPool.closeStatement(calPS);
		}
	}

	// Agenda functions

     /**
     * To update an event (not an all day event)
     * @param conn connection object used for performing database operations
     * @param eid Array of event ids' of the events to be deleted
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static int deleteAgendaEvent(Connection conn, String[] eid)
			throws ServiceException {
		int r = 0;
		PreparedStatement calPS = null;
		try {
			String query = null;
			String para = new String();
			for (int i = 0; i < eid.length; i++) {
				para += "?";
				if ((i + 1) != eid.length)
					para += ",";
			}
			query = "delete from calendarevents where eid in(" + para + ")";
			calPS = conn.prepareStatement(query);
			for (int i = 0; i < (eid.length); i++) {
				calPS.setString(i + 1, eid[i]);
			}
			r = calPS.executeUpdate();
			return r;
		} catch (SQLException e) {
            KrawlerLog.calendar.warn("calEvent:deleteAgendaEvent:" + e);
			throw ServiceException.FAILURE("calEvent.deleteAgendaEvent:", e);
		} finally {
			DbPool.closeStatement(calPS);
		}
	}

    /**
     * Fetches the events of the specific calendars in that time period for agenda view
     * @param conn connection object used for performing database operations
     * @param cid array of calendar ids'
     * @param viewdt1 start date from when to fetch the events
     * @param viewdt2 end date till when to fetch the events
     * @param limit the number records to be fetched
     * @param offset the offset from which the records are to be fetched
     * @param loginid user id, used to get the time zone
     * @return object array containing all the events
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static Object[] fetchAgendaEvent(Connection conn, String[] cidList,
			String viewdt1, String viewdt2, int limit, int offset, String loginid)
			throws ServiceException {
		Object[] jobj = new Object[2];
		PreparedStatement calPS = null;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			String para = new String();
			for (int i = 0; i < cidList.length; i++) {
				para += "?";
				if ((i + 1) != cidList.length)
					para += ",";
			}

			String query = " select calendarevents.eid, calendarevents.cid, " +
                            " DATE_FORMAT(CONVERT_TZ( calendarevents.startts, '+00:00' , ?),'%Y-%m-%d %H:%i:%S') as startts, " +
                            " DATE_FORMAT(CONVERT_TZ( calendarevents.endts, '+00:00' , ?),'%Y-%m-%d %H:%i:%S') as endts, " +
                            " calendarevents.subject, calendarevents.descr, calendarevents.location, calendarevents.showas, " +
                            " calendarevents.priority, calendarevents.recpattern, calendarevents.recend, calendarevents.resources, " +
                            " calendarevents.timestamp, calendarevents.allday from calendarevents " +
                            " where allday <> true and calendarevents.cid IN ( select cid from calendars ) " +
                            " and calendarevents.cid IN (" + para + ") " +
                            " and ((DATE_FORMAT(CONVERT_TZ( calendarevents.startts, '+00:00' , ?),'%Y-%m-%d %H:%i:%S') >= ? " +
                            " and DATE_FORMAT(CONVERT_TZ( calendarevents.startts, '+00:00' , ?),'%Y-%m-%d %H:%i:%S') < ?) " +
                            " or (DATE_FORMAT(CONVERT_TZ( calendarevents.endts, '+00:00' , ?),'%Y-%m-%d %H:%i:%S') > ? " +
                            " and DATE_FORMAT(CONVERT_TZ( calendarevents.endts, '+00:00' , ?),'%Y-%m-%d %H:%i:%S') <= ?) " +
                            " or (DATE_FORMAT(CONVERT_TZ( calendarevents.startts, '+00:00' , ?),'%Y-%m-%d %H:%i:%S') <= ? " +
                            " and DATE_FORMAT(CONVERT_TZ( calendarevents.endts, '+00:00' , ?),'%Y-%m-%d %H:%i:%S') >= ?)) " +
                            " union " +
                            " select calendarevents.eid, calendarevents.cid, startts, endts, calendarevents.subject, calendarevents.descr, " +
                            " calendarevents.location, calendarevents.showas, calendarevents.priority, calendarevents.recpattern, " +
                            " calendarevents.recend, calendarevents.resources, calendarevents.timestamp, calendarevents.allday " +
                            " from calendarevents where allday = true and calendarevents.cid IN ( select cid from calendars ) " +
                            " and calendarevents.cid IN (" + para + ") " +
                            " and ((startts >= ? and startts < ?) " +
                            " or (endts > ? and endts <= ?) " +
                            " or (startts <= ? and endts >= ?)) ";

			ResultSet rs = null;
            int count = 0;

            com.krawler.utils.json.base.JSONObject resobj = new com.krawler.utils.json.base.JSONObject();
            String startts = "";
            String endts = "";
            Boolean allDay = true;
            String timezone = Timezone.getTimeZone(conn, loginid);
            timezone = timezone.substring(0,4) + "00";      // [Temp fix] rounding of the 1/2 hour time zones as they are not yet supported

            String queryCnt = " select COUNT(*) as count from ( " + query + " ) as tmpCnt ";

            calPS = conn.prepareStatement(queryCnt);
            int j = 0;
            calPS.setString(++j, timezone);
            calPS.setString(++j, timezone);
			for (int i = 0; i < (cidList.length); i++) {
                calPS.setString(++j, cidList[i]);
			}
            calPS.setString(++j, timezone);
			calPS.setObject(++j, viewdt1);
            calPS.setString(++j, timezone);
			calPS.setObject(++j, viewdt2);
            calPS.setString(++j, timezone);
			calPS.setObject(++j, viewdt1);
            calPS.setString(++j, timezone);
			calPS.setObject(++j, viewdt2);
            calPS.setString(++j, timezone);
			calPS.setObject(++j, viewdt1);
            calPS.setString(++j, timezone);
			calPS.setObject(++j, viewdt2);

            for (int i = 0; i < (cidList.length); i++) {
                calPS.setString(++j, cidList[i]);
			}
            calPS.setObject(++j, viewdt1);
			calPS.setObject(++j, viewdt2);
			calPS.setObject(++j, viewdt1);
			calPS.setObject(++j, viewdt2);
			calPS.setObject(++j, viewdt1);
			calPS.setObject(++j, viewdt2);

			rs = calPS.executeQuery();
            if(rs.next()) {
                count = rs.getInt(1);
			}

            String queryMain = query + " ORDER BY startts ASC, endts - startts DESC LIMIT ? OFFSET ? ";

            calPS = conn.prepareStatement(queryMain);
            j = 2;
            calPS.setString(1, timezone);
            calPS.setString(2, timezone);
			for (int i = 0; i < (cidList.length); i++) {
                calPS.setString(++j, cidList[i]);
			}
			calPS.setString(++j, timezone);
			calPS.setObject(++j, viewdt1);
            calPS.setString(++j, timezone);
			calPS.setObject(++j, viewdt2);
            calPS.setString(++j, timezone);
			calPS.setObject(++j, viewdt1);
            calPS.setString(++j, timezone);
			calPS.setObject(++j, viewdt2);
            calPS.setString(++j, timezone);
			calPS.setObject(++j, viewdt1);
            calPS.setString(++j, timezone);
			calPS.setObject(++j, viewdt2);

			for (int i = 0; i < (cidList.length); i++) {
                calPS.setString(++j, cidList[i]);
			}
			calPS.setObject(++j, viewdt1);
			calPS.setObject(++j, viewdt2);
			calPS.setObject(++j, viewdt1);
			calPS.setObject(++j, viewdt2);
			calPS.setObject(++j, viewdt1);
			calPS.setObject(++j, viewdt2);
            calPS.setInt(++j, limit);
			calPS.setInt(++j, offset);

			rs = calPS.executeQuery();
            while(rs.next()) {
                startts = rs.getString("startts");
                endts = rs.getString("endts");
                allDay = rs.getBoolean("allday");
                if(allDay) {
                    Calendar endCal = Calendar.getInstance();
                    endCal.setTime(sdf.parse(endts));
                    endCal.add(Calendar.DATE, -1);
                    endts = sdf.format(endCal.getTime());
                }

                com.krawler.utils.json.base.JSONObject temp = new com.krawler.utils.json.base.JSONObject();
                temp.put("eid", rs.getString("eid"));
                temp.put("cid", rs.getString("cid"));
                temp.put("startts", startts);
                temp.put("endts", endts);
                temp.put("subject", rs.getString("subject"));
                temp.put("descr", rs.getString("descr"));
                temp.put("location", rs.getString("location"));
                temp.put("showas", rs.getString("showas"));
                temp.put("priority", rs.getString("priority"));
                temp.put("recpattern", rs.getString("recpattern"));
                temp.put("recend", rs.getString("recend"));
                temp.put("resources", rs.getString("resources"));
                temp.put("timestamp", rs.getString("timestamp"));
                temp.put("allday", allDay);
                temp.put("deleteFlag", "0");
                resobj.append("data", temp);
            }

            int len = 0, actualCount = 0;
            for(int c = 0; c < cidList.length; c++){
                String id = cidList[c];
                String data = fetchICalEvents(id);
                if (data.compareTo("") != 0) {
                    JSONObject j1 = new JSONObject(data);
                    len = j1.getJSONArray("data").length();
                    for (int i = 0; i < len; i++) {
                        JSONObject rs1 = j1.getJSONArray("data").getJSONObject(i);
                        String st = rs1.getString("startts");
                        String et = rs1.getString("endts");
                        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
                        if (!StringUtil.isNullOrEmpty(st) && !StringUtil.isNullOrEmpty(et)) {
                            Date sdt = sdfDate.parse(st);
                            Date edt = sdfDate.parse(et);
                            if ((sdt.equals(sdfDate.parse(viewdt1)) || sdt.after(sdfDate.parse(viewdt1)))
                                    && (edt.equals(sdfDate.parse(viewdt2)) || edt.before(sdfDate.parse(viewdt2)))) {
                                com.krawler.utils.json.base.JSONObject temp = new com.krawler.utils.json.base.JSONObject();
                                temp.put("eid", rs1.getString("eid"));
                                temp.put("cid", rs1.getString("cid"));
                                temp.put("startts", st);
                                temp.put("endts", et);
                                temp.put("subject", rs1.getString("subject"));
                                temp.put("descr", rs1.getString("descr"));
                                temp.put("location", rs1.getString("location"));
                                temp.put("showas", rs1.getString("showas"));
                                temp.put("priority", rs1.getString("priority"));
                                temp.put("recpattern", rs1.getString("recpattern"));
                                temp.put("recend", rs1.getString("recend"));
                                temp.put("resources", rs1.getString("resources"));
                                temp.put("timestamp", rs1.getString("timestamp"));
                                temp.put("allday", rs1.getBoolean("allday"));
                                temp.put("deleteFlag", "1");
                                resobj.append("data", temp);
                                actualCount++;
                            }
                        }
                    }
                }
            }
            if(resobj.has("data")) {
                jobj[1] = resobj;
            }
            else {
                jobj[1] = "{\"data\":{}}";
            }
            jobj[0] = count;
            if(actualCount != 0)
                jobj[0] = count+len;
			return jobj;
        } catch (SQLException e) {
            KrawlerLog.calendar.warn("calEvent:fetchAgendaEvent:" + e);
			throw ServiceException.FAILURE("calEvent.fetchAgendaEvent:", e);
		} catch (ParseException e) {
            KrawlerLog.calendar.warn("calEvent:fetchAgendaEvent:" + e);
			throw ServiceException.FAILURE("calEvent.fetchAgendaEvent:", e);
		} catch (JSONException e) {
            KrawlerLog.calendar.warn("calEvent:fetchAgendaEvent:" + e);
			throw ServiceException.FAILURE("calEvent.fetchAgendaEvent:", e);
        } catch (ServiceException e) {
            KrawlerLog.calendar.warn("calEvent.fetchAgendaEvent:" + e);
			throw ServiceException.FAILURE("calEvent.fetchAgendaEvent:" + e.getMessage(), e);
        } catch (Exception e) {
            KrawlerLog.calendar.warn("calEvent.fetchAgendaEvent:" + e);
			throw ServiceException.FAILURE("calEvent.fetchAgendaEvent:",e);
        } finally {
			DbPool.closeStatement(calPS);
		}
	}

    /**
     * To invite someone to view your event
     * @param conn connection object used for performing database operations
     * @param eid id of the event to be shown to the guest
     * @param userid the id of the user
     * @param status status
     * @return id of the event
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static String insertGuest(Connection conn, String eid,
			String userid, String status) throws ServiceException {
		int r = 0;
		String result = "";
		PreparedStatement calPS = null;
		try {
			String query = null;
			query = "insert into eventguestlist (eid,userid,status,timestamp) values( ?, ?, ?, now())";
			calPS = conn.prepareStatement(query);
			calPS.setString(1, eid);
			calPS.setString(2, userid);
			calPS.setString(3, status);
			r = calPS.executeUpdate();
			result = String.valueOf(r);
			if (r > 0) {
				result = eid;
			}
			return result;
		} catch (SQLException e) {
            KrawlerLog.calendar.warn("calEvent:insertGuest:" + e);
			throw ServiceException.FAILURE("calEvent.insertGuest:", e);
		} finally {
			DbPool.closeStatement(calPS);
		}
	}

    /**
     * To delete the particular guest
     * @param conn connection object used for performing database operations
     * @param eid id of the event of which the guest is to be deleted
     * @return count (only 1) if the guest has been successfully deleted else 0
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static int deleteGuest(Connection conn, String eid)
			throws ServiceException {
		int r = 0;
		PreparedStatement calPS = null;
		try {
			String query = null;
			query = "delete from eventguestlist where eid=?";
			calPS = conn.prepareStatement(query);
			calPS.setString(1, eid);
			r = calPS.executeUpdate();
			return r;
		} catch (SQLException e) {
            KrawlerLog.calendar.warn("calEvent:deleteGuest:" + e);
			throw ServiceException.FAILURE("calEvent.deleteGuest:", e);
		} finally {
			DbPool.closeStatement(calPS);
		}
	}

    /**
     * To fetch the details of the guest
     * @param conn connection object used for performing database operations
     * @param eid id of the event
     * @return object contaning the id of the user and the status of the event
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static Object selectGuest(Connection conn, String eid)
			throws ServiceException {
		Object jobj = null;
		PreparedStatement calPS = null;
		try {
			String query = null;
			ResultSet rs = null;
			KWLJsonConverter KWL = new KWLJsonConverter();
			query = "select userid,status from eventguestlist where eid=?";
			calPS = conn.prepareStatement(query);
			calPS.setString(1, eid);
			rs = calPS.executeQuery();
			jobj = KWL.GetJsonForGrid(rs);
			return jobj;
		} catch (SQLException e) {
            KrawlerLog.calendar.warn("calEvent.selectGuest:" + e);
			throw ServiceException.FAILURE("calEvent.selectGuest:", e);
		} finally {
			DbPool.closeStatement(calPS);
		}
	}

    /**
     * To create a new reminder
     * @param conn connection object used for performing database operations
     * @param eid id of the event for which the reminder is to be created
     * @param rtype rtype
     * @param rtime rtime
     * @return id of the event for which the reminder was created
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static String insertReminder(Connection conn, String eid,
			String rtype, int rtime) throws ServiceException {
		int r = 0;
		String result = "";
		PreparedStatement calPS = null;
		try {
			String query = null;
			query = "insert into eventreminder (rid,eid,rtype,rtime,timestamp) values( ?, ?, ?, ?, now())";
			String reminderId = UUID.randomUUID().toString();
			calPS = conn.prepareStatement(query);
			calPS.setString(1, reminderId);
			calPS.setString(2, eid);
			calPS.setString(3, rtype);
			calPS.setInt(4, rtime);
			r = calPS.executeUpdate();
			result = String.valueOf(r);
			if (r > 0) {
				result = eid;
			}
			return result;
		} catch (SQLException e) {
            KrawlerLog.calendar.warn("calEvent.insertReminder:" + e);
			throw ServiceException.FAILURE("calEvent.insertReminder:", e);
		} finally {
			DbPool.closeStatement(calPS);
		}
	}

    /**
     * To delete the reminder
     * @param eid id of the event whose reminder is to be deleted
     * @return count (only 1) if the guest has been successfully deleted else 0
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static int deleteReminder(Connection conn, String eid)
			throws ServiceException {
		int r = 0;
		PreparedStatement calPS = null;
		try {
			String query = null;
			query = "delete from eventreminder where eid=?";
			calPS = conn.prepareStatement(query);
			calPS.setString(1, eid);
			r = calPS.executeUpdate();
			return r;
		} catch (SQLException e) {
            KrawlerLog.calendar.warn("calEvent.deleteReminder:" + e);
			throw ServiceException.FAILURE("calEvent.deleteReminder:", e);
		} finally {
			DbPool.closeStatement(calPS);
		}
	}

    /**
     * To fetch the reminder details
     * @param conn connection object used for performing database operations
     * @param eid id of the event of which the reminder is to be fetched
     * @return object containing the rtype and the rtime of the reminder
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static Object selectReminder(Connection conn, String eid)
			throws ServiceException {
		Object jobj = null;
		PreparedStatement calPS = null;
		try {
			String query = null;
			ResultSet rs = null;
			KWLJsonConverter KWL = new KWLJsonConverter();
			query = "select rtype,rtime from eventreminder where eid=?";
			calPS = conn.prepareStatement(query);
			calPS.setString(1, eid);
			rs = calPS.executeQuery();
			jobj = KWL.GetJsonForGrid(rs);
			return jobj;
		} catch (SQLException e) {
            KrawlerLog.calendar.warn("calEvent.selectReminder:" + e);
			throw ServiceException.FAILURE("calEvent.selectReminder:", e);
		} finally {
			DbPool.closeStatement(calPS);
		}
	}

    /**
     * To fetch the response or the status of the guest
     * @param conn connection object used for performing database operations
     * @param eid id of the event
     * @param userid id of the user
     * @return object containing the status of the event and the guest relation
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static Object getResponseEvent(Connection conn, String eid,
			String userid) throws ServiceException {
		Object jobj = null;
		PreparedStatement calPS = null;
		try {
			String query = null;
			ResultSet rs = null;
			KWLJsonConverter KWL = new KWLJsonConverter();
			query = "select status from eventguestlist where eid=? and userid=?";
			calPS = conn.prepareStatement(query);
			calPS.setString(1, eid);
			calPS.setString(2, userid);
			rs = calPS.executeQuery();
			jobj = KWL.GetJsonForGrid(rs);
			return jobj;
		} catch (SQLException e) {
            KrawlerLog.calendar.warn("calEvent.getResponseEvent:" + e);
			throw ServiceException.FAILURE("calEvent.getResponseEvent:", e);
		} finally {
			DbPool.closeStatement(calPS);
		}
	}

    /**
     * To update the response or the status of the guest
     * @param conn connection object used for performing database operations
     * @param eid id of the event
     * @param userid id of the user
     * @param setStatus the new status of the relation between the event and the guest
     * @return object containing the status of the event and the guest relation
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static int updateResponse(Connection conn, String eid,
			String userid, String setStatus) throws ServiceException {
		int result = 0;
		PreparedStatement calPS = null;
		try {
			String query = null;
			query = "update eventguestlist set status=? where eid=? and userid=?";
			calPS = conn.prepareStatement(query);
			calPS.setString(1, setStatus);
			calPS.setString(2, eid);
			calPS.setString(3, userid);
			result = calPS.executeUpdate();
			return result;
		} catch (SQLException e) {
            KrawlerLog.calendar.warn("calEvent:updateResponse:" + e);
			throw ServiceException.FAILURE("calEvent.updateResponse:", e);
		} finally {
			DbPool.closeStatement(calPS);
		}
	}

    /**
     * To fetch the default calendar of the project or the user
     * @param conn connection object used for performing database operations
     * @param userid id of the user or the project
     * @param isdefault isdefault flag of the calendar
     * @return object containing id of the default calendar
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static Object getDefaultCalendar(Connection conn, String userid,
			int isdefault) throws ServiceException {
		Object jobj = null;
		PreparedStatement calPS = null;
		try {
			String query = null;
			ResultSet rs = null;
			KWLJsonConverter KWL = new KWLJsonConverter();
			query = "select cid from calendars where userid=? and isdefault=?";
			calPS = conn.prepareStatement(query);
			calPS.setString(1, userid);
			calPS.setInt(2, isdefault);
			rs = calPS.executeQuery();
			jobj = KWL.GetJsonForGrid(rs);
			return jobj;
		} catch (SQLException e) {
            KrawlerLog.calendar.warn("calEvent.getDefaultCalendar:" + e);
			throw ServiceException.FAILURE("calEvent.getDefaultCalendar:", e);
		} finally {
			DbPool.closeStatement(calPS);
		}
	}

    /**
     * To fetch the details of the event
     * @param conn connection object used for performing database operations
     * @param eid id of the event of which the details are to be fetched
     * @return object containing the details of the event
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
	public static Object getEventDetails(Connection conn, String eid)
			throws ServiceException {
		Object jobj = null;
		PreparedStatement calPS = null;
		try {
			String query = null;
			ResultSet rs = null;
			KWLJsonConverter KWL = new KWLJsonConverter();
			query = "select * from calendarevents where eid=?";
			calPS = conn.prepareStatement(query);
			calPS.setString(1, eid);
			rs = calPS.executeQuery();
			jobj = KWL.GetJsonForGrid(rs);
			return jobj;
		} catch (SQLException e) {
            KrawlerLog.calendar.warn("calEvent.getEventDetails:" + e);
			throw ServiceException.FAILURE("calEvent.getEventDetails:", e);
		} finally {
			DbPool.closeStatement(calPS);
		}
	}
    /**
     * Check if calendar has any events or not for exporting it
     * @param conn connection object used for performing database operations
     * @param eid id of the event of which the details are to be fetched
     * @return object containing the details of the event
     * @throws ServiceException is used to handle all the exceptions thrown internally.
     */
    public static boolean checkForExport(Connection conn, String cid) throws ServiceException {
        PreparedStatement calPS = null;
        boolean b = false;
        try {
            String query = null;
            ResultSet rs = null;
            query = "select * from calendarevents where cid=?";
            calPS = conn.prepareStatement(query);
            calPS.setString(1, cid);
            rs = calPS.executeQuery();
            if(rs.next())
                b = true;
            return b;
        } catch (SQLException e) {
            KrawlerLog.calendar.warn("calEvent.checkForExport:" + e);
            throw ServiceException.FAILURE("calEvent.checkForExport:", e);
        } finally {
            DbPool.closeStatement(calPS);
        }
    }
}
