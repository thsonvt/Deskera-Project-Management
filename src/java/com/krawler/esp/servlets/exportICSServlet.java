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

import com.krawler.common.service.ServiceException;
import java.io.*;
import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.krawler.common.util.KrawlerLog;
import com.krawler.database.DbPool;
import com.krawler.database.DbPool.Connection;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.Tree;
import com.krawler.esp.handlers.calEvent;

import net.fortuna.ical4j.data.*;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;

public class exportICSServlet extends HttpServlet {
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        Connection conn = null;
        response.setContentType("text/calendar");
        response.setHeader("Content-Disposition", "attachment; filename=" + "PM_iCal.ics" + ";");
        try {
            conn = DbPool.getConnection();
            String userid = request.getParameter("uid");
            Calendar cal = new Calendar();
            if(StringUtil.isNullOrEmpty(userid)) {
                String cid[];
                cid = new String[1];
                cid[0] = request.getParameter("cid");
                cal = exportCal(conn, cid);
            }
            else
                cal = exportCalAll(userid, conn);

            CalendarOutputter outputter = new CalendarOutputter();
            outputter.output(cal, response.getOutputStream());
        } catch (Exception ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn("Unable to process request: " + ex.toString());
        } finally {
            DbPool.quietClose(conn);
        }
    }

    public static Calendar exportCal(Connection conn, String cid[])
    throws ServletException, IOException {
        PreparedStatement pstmt = null;
        Calendar cal = new Calendar();
        try {
            setCalendarProperties(cal);
            ResultSet rs = null;

            for(int cnt = 0; cnt< cid.length; cnt++) {
                pstmt = conn.prepareStatement(" select calendarevents.eid, calendarevents.allday, calendarevents.subject, calendarevents.descr, " +
                    " calendarevents.location, calendarevents.startts, calendarevents.endts, " +
                    " ( select timezone.tzid from timezone where timezone.id = calendars.timezone ) as tzid " +
                    " from calendarevents inner join calendars on calendars.cid = calendarevents.cid where calendarevents.cid = ? ");
                pstmt.setString(1, cid[cnt]);
                rs = pstmt.executeQuery();
                cal = kWLCalToiCal(rs, cal);
            }
        } catch (Exception ex) {
            DbPool.closeStatement(pstmt);
            KrawlerLog.op.warn("Unable to process request: " + ex.toString());
        } finally {
            return cal;
        }
    }

    public static Calendar exportCalAll(String userid, Connection conn)
    throws ServletException, IOException {
        PreparedStatement pstmt = null;
        Calendar cal = new Calendar();
        try {
            setCalendarProperties(cal);
            ResultSet rs = null;
            
            pstmt = conn.prepareStatement(" select calendarevents.eid, calendarevents.allday, concat(calendarevents.subject,' [',project.projectname,' - ',calendars.cname,']') as subject , " +
                " calendarevents.descr, calendarevents.location, calendarevents.startts, calendarevents.endts, " +
                " (select timezone.tzid from timezone where timezone.id = calendars.timezone) as tzid " +
                " from calendarevents inner join calendars on calendars.cid = calendarevents.cid inner join project on project.projectid = calendars.userid " +
                " where project.projectid in " +
                " ( select project.projectid from project inner join projectmembers on projectmembers.projectid = project.projectid where " +
                " projectmembers.inuseflag = 1 and projectmembers.status in (3,4,5) and userid = ? ) " +
                " and calendars.cid NOT IN (select cid from uncheckedcalmap where userid = ? union select cid from networkcalendars) ");
            pstmt.setString(1, userid);
            pstmt.setString(2, userid);
            rs = pstmt.executeQuery();
            cal = kWLCalToiCal(rs, cal);
        } catch (Exception ex) {
            DbPool.closeStatement(pstmt);
            KrawlerLog.op.warn("Unable to process request: " + ex.toString());
        } finally {
            return cal;
        }
    }

    private static Calendar kWLCalToiCal(ResultSet rs, Calendar cal) throws SQLException, ParseException, ServiceException {
        DateTime startts = null, endts = null;
        VEvent vEvent = null;
        String TZID = "";
        while(rs.next()){
            TZID = rs.getString("tzid");
            if(TZID == null)
                TZID = "";
            if(!rs.getBoolean("allday")){
                startts = toiCalDate(rs.getString("startts"), TZID);
                endts = toiCalDate(rs.getString("endts"), TZID);
                vEvent = new VEvent();
                vEvent.getProperties().add(new Summary(rs.getString("subject")));
                vEvent.getProperties().add(new DtStart(new DateTime(startts)));
                vEvent.getProperties().add(new DtEnd(new DateTime(endts)));
            } else {
                String startdate = kWLToiCalDayDate(rs.getString("startts"));
                String enddate = kWLToiCalDayDate(rs.getString("endts"));
                vEvent = new VEvent(new Date(startdate), rs.getString("subject"));
                if(calEvent.getDaysDiff(rs.getString("startts"), rs.getString("endts")) != 1){
                    vEvent.getProperties().add(new DtEnd(new Date(enddate)));
                }
            }
            vEvent.getProperties().add(new Uid(rs.getString("eid")));
            if(!StringUtil.isNullOrEmpty(rs.getString("descr")))
                vEvent.getProperties().add(new Description(rs.getString("descr")));
            if(!StringUtil.isNullOrEmpty(rs.getString("location")))
                vEvent.getProperties().add(new Location(rs.getString("location")));
            cal.getComponents().add(vEvent);
        }
        return cal;
    }

    private static void setCalendarProperties(Calendar cal) throws URISyntaxException {
        cal.getProperties().add(new ProdId("-//Krawler Inc//PM//EN"));
        cal.getProperties().add(net.fortuna.ical4j.model.property.Version.VERSION_2_0);
        cal.getProperties().add(CalScale.GREGORIAN);
    }

    public static DateTime toiCalDate(String idate, String TZID) {
        DateTime dtTZ = null;
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0");

            TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
            TimeZone timezone = registry.getTimeZone(TZID);

            dtTZ = new DateTime(sdf.parse(idate));
            if (TZID.compareTo("") != 0)
                dtTZ.setTimeZone(timezone);
        } catch (ParseException ex) {
            Logger.getLogger(Tree.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dtTZ;
    }

    public static String kWLToiCalDayDate(String idate) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd");
            java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0");

            if (idate.compareTo("") != 0) {
                java.util.Date dt = sdf1.parse(idate);
                idate = sdf.format(dt);
            }
        } catch (ParseException ex) {
            idate = "";
            System.out.print(ex);
        } finally {
            return idate;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
