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
package com.krawler.common.util;

import com.krawler.common.service.ServiceException;
import com.krawler.database.DbPool;
import com.krawler.database.DbPool.Connection;
import com.krawler.esp.handlers.projdb;
import com.krawler.utils.json.KWLJsonConverter;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.krawler.esp.project.task.DurationType;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import org.apache.commons.lang.time.DateUtils;

/**
 *
 * @author Abhay
 */
public class SchedulingUtilities {

    public static double getDurationInDays(String duration) {
        double d = -1;
        boolean f = false;
        if (!StringUtil.isNullOrEmpty(duration)) {
            if (duration.contains("h")) {
                duration = duration.substring(0, duration.indexOf('h'));
                f = true;
            } else if (duration.contains("d")) {
                duration = duration.substring(0, duration.indexOf('d'));
            }
            d = Double.parseDouble(duration);
            if (f) {
                d = d / 8;
            }
        }
        return d;
    }

    public static double parseDuration(String duration) {
        double d = -1;
        if (duration.contains("h")) {
            duration = duration.substring(0, duration.indexOf('h'));
        } else if (duration.contains("d")) {
            duration = duration.substring(0, duration.indexOf('d'));
        }
        d = Double.parseDouble(duration);
        return d;
    }

    public static DurationType getDurationType(String duration){
        DurationType dT = null;
        if(duration.contains("d"))
            dT = DurationType.DAYS;
        else if(duration.contains("h"))
            dT = DurationType.HOURS;
        else
            dT = DurationType.DAYS;
        return dT;
    }
    
    public static String getNextWorkingDay(String currDate, int[] NWD, String[] CH) {
        Calendar c1 = Calendar.getInstance();
        Date stdate = c1.getTime();
        java.text.SimpleDateFormat sdfLong = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        int n = 1;
        try {
            c1.setTime(sdfLong.parse(currDate));
            while (n > 0) {
                c1.add(Calendar.DATE, 1);
                if (Arrays.binarySearch(NWD, (c1.get(Calendar.DAY_OF_WEEK) - 1)) > -1 || Arrays.binarySearch(CH, sdf.format(c1.getTime())) > -1) {
                    n++;
                } else {
                    n = 0;
                }
                stdate = c1.getTime();
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("Exception while calculating next working day : " + e.getMessage(), e);
        } finally {
            return sdfLong.format(stdate);
        }
    }

    public static Date calculateEndDate(Date sDate, String duration, int[] NonWorkDays,
            String[] CmpHoliDays, String userid) throws ParseException, ServiceException {
        Date dt = new Date();
        java.text.SimpleDateFormat sdfLong = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String edate = "";
        Calendar c = Calendar.getInstance();
        dt = sDate;
        double d = getDurationInDays(duration);
        c.setTime(dt);
        if (d > 0) {
            c.add(Calendar.DATE, (int) (d - 1));
        } else {
            c.add(Calendar.DATE, (int) d);
        }
        Date nDate = dt;
        int flag = 0;
        int nwd = nonWorkingDays(nDate, c.getTime(), NonWorkDays, CmpHoliDays);
        while (nwd != 0) {
            nDate = c.getTime();
            if (nwd == 1 && flag == 0) {
                c.add(Calendar.DATE, nwd);
                flag = 1;
            } else {
                c.add(Calendar.DATE, nwd - 1);
            }
            nwd = nonWorkingDays(nDate, c.getTime(), NonWorkDays, CmpHoliDays);
        }
        dt = c.getTime();
        edate = sdfLong.format(dt);
        if (Arrays.binarySearch(NonWorkDays, (c.get(Calendar.DAY_OF_WEEK) - 1)) > -1) {
            edate = getNextWorkingDay(edate, NonWorkDays, CmpHoliDays);
        }
        dt = sdfLong.parse(edate);
        return dt;
    }

    public static int nonWorkingDays(java.util.Date stdate, java.util.Date enddate, int[] NonWorkDays, String[] CmpHoliDays)
            throws ServiceException {
        Calendar c1 = Calendar.getInstance();
        int NonWorkingDaysBetween = 0;
        Date StDate = stdate;
        Date EndDate = enddate;
        try {
            c1.setTime(stdate);
            while ((stdate.compareTo(enddate)) < 0) {
                if (Arrays.binarySearch(NonWorkDays, (c1.get(Calendar.DAY_OF_WEEK) - 1)) > -1) {
                    NonWorkingDaysBetween += 1;
                }
                c1.add(Calendar.DATE, 1);
                stdate = c1.getTime();
            }
            NonWorkingDaysBetween += CountCmpHolidays(StDate, EndDate, CmpHoliDays);
        } catch (Exception e) {
            throw ServiceException.FAILURE("Exception while calculating non working days : " + e.getMessage(), e);
        }
        return NonWorkingDaysBetween;
    }

    public static int CountCmpHolidays(Date stdate, Date enddate, String[] CmpHoliDays) {
        int NonWorkingDays = 0;
        Calendar c1 = Calendar.getInstance();
        DateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        try {
            c1.setTime(stdate);
            while ((stdate.compareTo(enddate)) < 0) {
                if (Arrays.binarySearch(CmpHoliDays, sdf1.format(c1.getTime())) >= 0) {
                    NonWorkingDays += 1;
                }
                c1.add(Calendar.DATE, 1);
                stdate = c1.getTime();
            }
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
        return NonWorkingDays;
    }

    public static int calculateWorkingDays(Date stdate, Date enddate, int[] NonWorkDays, String[] CmpHoliDays) throws ServiceException {
        int nonworkingDays = nonWorkingDays(stdate, enddate, NonWorkDays, CmpHoliDays);
        int diff = (int) ((enddate.getTime() - stdate.getTime()) / (1000 * 60 * 60 * 24)) + 1;
        diff = diff - nonworkingDays;
        return diff;
    }

    public static int calculateWorkingDays(Connection conn, String projectID, Date stdate, Date enddate) throws ServiceException {
        int[] NonWorkDays = projdb.getNonWorkWeekDays(conn, projectID);
        String[] CmpHoliDays = projdb.getCompHolidays(conn, projectID, "");
        int diff = calculateWorkingDays(stdate, enddate, NonWorkDays, CmpHoliDays);
        return diff;
    }

    public static int getDaysDiff(Date endDate, Date minStartDate) {
        long diffInMilleseconds = endDate.getTime() - minStartDate.getTime();
        int days = Math.round(diffInMilleseconds / (1000 * 60 * 60 * 24));
        return days;
    }

    /* optimized */
    public static int[] getNonWorkWeekDays(Connection conn, String projid) throws ServiceException {
        int[] nonworkweekArr = {};
        try {
            String nonworkdays = getNonWorkWeekdays(conn, projid);
            JSONObject nmweekObj = new JSONObject(nonworkdays);
            nonworkweekArr = new int[nmweekObj.getJSONArray("data").length()];
            for (int cnt = 0; cnt < nmweekObj.getJSONArray("data").length(); cnt++) {
                nonworkweekArr[cnt] = Integer.parseInt(nmweekObj.getJSONArray("data").getJSONObject(cnt).getString("day"));
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("projdb.getNonWorkWeekDays : " + ex.getMessage(), ex);
        } finally {
            return nonworkweekArr;
        }
    }

    public static String getNonWorkWeekdays(Connection conn, String projid) throws ServiceException {
        String retStr = null;
        PreparedStatement pstmt = null;
        try {
            // week holidays
            pstmt = conn.prepareStatement("select day from proj_workweek where isholiday = true and projectid = ?");
            pstmt.setString(1, projid);
            ResultSet rs = pstmt.executeQuery();
            KWLJsonConverter kjs = new KWLJsonConverter();
            retStr = kjs.GetJsonForGrid(rs);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("SchedulUtilities.getNonWorkWeekdays error", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return retStr;
    }

    public static String[] getCompHolidays(Connection conn, String companyId) {
        String[] holidayArr = new String[1];
        try {
            String Holidays = getCmpHolidaydays(conn, companyId);
            holidayArr[0] = "";
            if (Holidays.compareTo("{data:{}}") != 0) {
                JSONObject nmweekObj = new JSONObject(Holidays);
                holidayArr = new String[nmweekObj.getJSONArray("data").length()];
                for (int cnt = 0; cnt < nmweekObj.getJSONArray("data").length(); cnt++) {
                    holidayArr[cnt] = nmweekObj.getJSONArray("data").getJSONObject(cnt).getString("holiday");
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("projdb.getCompHolidays : " + ex.getMessage(), ex);
        } finally {
            return holidayArr;
        }
    }

    public static String getCmpHolidaydays(Connection conn, String companyId) throws ServiceException {
        String res = "";
        try {
            String qry = "SELECT holiday, description FROM companyholidays WHERE companyid = ?";
            PreparedStatement pstmt = conn.prepareStatement(qry);
            pstmt.setString(1, companyId);
            ResultSet rs = pstmt.executeQuery();
            KWLJsonConverter j = new KWLJsonConverter();
            res = j.GetJsonForGrid(rs);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("SchedulingUtilities.getCmpHolidaydays : " + e.getMessage(), e);
        }
        return res;
    }
    
    public static String getFormattedDateString(String date, String format){
        Date d = null;
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if(!StringUtil.isNullOrEmpty(format))
            sdf = new SimpleDateFormat(format);
        try {
            d = DateUtils.parseDate(date, new String[]{"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd"});
        } catch (ParseException e) {
            throw ServiceException.FAILURE("Date Parse Exception :: " + e.getMessage(), e);
        } finally {
            if(d != null)
                return sdf.format(d);
            else 
                return date;
        }
    }
    
    public static Date getDate(String date){
        Date d = null;
        try {
            d = DateUtils.parseDate(date, new String[]{"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd"});
        } catch (ParseException e) {
            throw ServiceException.FAILURE("Date Parse Exception :: " + e.getMessage(), e);
        } finally {
            return d;
        }
    }
}
