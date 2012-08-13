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

package com.krawler.common.timezone;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import com.krawler.common.service.ServiceException;
import com.krawler.common.util.SchedulingUtilities;
import com.krawler.common.util.StringUtil;
import com.krawler.database.DbPool;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.database.DbPool.Connection;
import com.krawler.esp.handlers.dateFormatHandlers;
import com.krawler.utils.json.KWLJsonConverter;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.time.DateUtils;

public class Timezone extends KWLJsonConverter{
    
    /** Creates a new instance of Timezone */
    public Timezone() {
    }
    public static void main(String[] args) {
        Connection conn = null;
        try {
            java.util.Date post_time = Timezone.getGmtDate();
             java.sql.Timestamp sqlPostDate = new java.sql.Timestamp(post_time.getTime());
            conn = DbPool.getConnection();
            String c = "examdate";
            String t = "lexamschedule";
            String tZ = "+08:00";
            Date gmtDate = getGmtDate();
            String date = "2008-05-21";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-dd-MM");
//            sdf.parse(date);
            Date newGmtDate = getGmtDate(sdf.parse(date));
            //String date = "2008-05-21 15:30:00";
            String uid = "08b18f8b-86ca-4d5b-a7a0-f576d50e7cb0";    //timezone == 2
            System.out.println(getGmtDate(conn, date, uid));
            
        } catch(Exception e) {
            System.out.println("You have an error: " + e);
        } finally {
            DbPool.quietClose(conn);
        }
    }    
    
    public static String toTimezone(Connection conn, String column, String table, String key, String value, String timeZone) throws ServiceException {
        String result = "";
        String query = "select DATE_FORMAT(CONVERT_TZ("+ column +", '+00:00' , '"+ timeZone +"'),'%Y-%m-%d %H:%i:%S') " +
                "as time_convt from "+ table +" where "+ key +" = '"+ value +"';";
        DbResults rs = DbUtil.executeQuery(conn, query);
        while(rs.next()) {
            result = rs.getString("time_convt");
        }
        return result;
    }
    
    public static String toTimezone(Connection conn, String column, String table, String key, int value, String timeZone) throws ServiceException {
        String result = "";
        String query = "select DATE_FORMAT(CONVERT_TZ("+ column +", '+00:00' , '"+ timeZone +"'),'%Y-%m-%d %H:%i:%S') " +
                "as time_convt from "+ table +" where "+ key +" = "+ value +";";
        DbResults rs = DbUtil.executeQuery(conn, query);
        while(rs.next()) {
            result = rs.getString("time_convt");
        }
        return result;
    }
    
    public static String toUserTimezone(Connection conn, String date, String userid) throws ServiceException {
//        String result = "";
////        String query = "select DATE_FORMAT(CONVERT_TZ('"+ date +"', '"+getSystemTimezone(conn)+"' , '"+ Timezone.getUserCompanyTimeZoneDifference(conn, userid) +"'),'%Y-%m-%d %H:%i:%S') as time_convt; ";
//        String query = "select DATE_FORMAT('"+ date +"','%Y-%m-%d %H:%i:%S') as time_convt; ";
//        DbResults rs = DbUtil.executeQuery(conn, query);
//        while(rs.next()) {
//            result = rs.getString("time_convt");
//        }
//        return result;
        return SchedulingUtilities.getFormattedDateString(date, "yyyy-MM-dd HH:mm:ss");
    }

    public static String toUserDefTimezone(Connection conn, String date, String tz) throws ServiceException {
        if(tz.compareTo("")==0)
            tz=Timezone.getSystemTimezone(conn);
        String result = "";
        String query = "select DATE_FORMAT(CONVERT_TZ('"+ date +"', '+00:00' , '"+ tz +"'),'%Y-%m-%d %H:%i:%S') " +
                "as time_convt; ";
        DbResults rs = DbUtil.executeQuery(conn, query);
        while(rs.next()) {
            result = rs.getString("time_convt");
        }
        return result;
    }
     public static String getSystemTimezoneId(Connection conn) throws ServiceException{
        DbResults rs = DbUtil.executeQuery(conn, "select id from systemtimezone");
        String diff = null;
        if(rs.next()){
            diff = rs.getString("id");
        }
        return diff;
    }
     public static String getSystemTimezoneName(Connection conn) throws ServiceException{
        DbResults rs = DbUtil.executeQuery(conn, "select name from systemtimezone");
        String diff = null;
        if(rs.next()){
            diff = rs.getString("name");
        }
        return diff;
    }

    public static String toSystemTimezone(Connection conn, String date, String userid) throws ServiceException {
        String result = "";
        String query = "select DATE_FORMAT(CONVERT_TZ('"+ date +"', '+00:00' , '"+ Timezone.getSystemTimezone(conn) +"'),'%Y-%m-%d %H:%i:%S') " +
                "as time_convt; ";
        DbResults rs = DbUtil.executeQuery(conn, query);
        while(rs.next()) {
            result = rs.getString("time_convt");
        }
        return result;
    }
    
    public static Date getGmtDate() throws ServiceException {
        Date cmpdate = new Date();
        try{
            Calendar calInstance = Calendar.getInstance();
            int gmtoffset =calInstance.get(Calendar.DST_OFFSET)
                                +calInstance.get(Calendar.ZONE_OFFSET);
            long date = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy HH:mm a");
            String cmp = sdf.format(new java.util.Date(date));
            cmpdate = new Date(sdf.parse(cmp).getTime()-gmtoffset);
        } catch(ParseException e){
            throw ServiceException.FAILURE("Timezone.getGmtDate", e);
        }
        return cmpdate;
    }
    public static java.sql.Timestamp getGmtTimestamp() throws ServiceException{
                java.util.Date post_time = getGmtDate();
                java.sql.Timestamp sqlPostDate = new java.sql.Timestamp(post_time.getTime());
                return sqlPostDate;
    }
    public static Date getGmtDate(Date userDate) throws ServiceException {
        Date cmpdate = new Date();
        //try{
            Calendar calInstance = Calendar.getInstance();
            
            int gmtoffset =calInstance.get(Calendar.DST_OFFSET)
                                +calInstance.get(Calendar.ZONE_OFFSET);
            //SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy HH:mm a");
            //String cmp = sdf.format(userDate);
            
            cmpdate = new Date(userDate.getTime() - gmtoffset);
            
        /*} catch(ParseException e){
            throw ServiceException.FAILURE("Timezone.getGmtDate", e);
        }*/
        return cmpdate;
    }
   
    public static String getTimeZone(Connection conn, String uid) throws ServiceException {
        String tzDiffQuery = " SELECT tzid FROM timezone WHERE id = ? ";
        String id = "";
        String tzIdQuery = "select timezone from users where userid = ?";
        DbResults trs1 = DbUtil.executeQuery(conn, tzIdQuery, new Object[]{uid});
        if(!trs1.next()||StringUtil.isNullOrEmpty(trs1.getString("timezone"))){
            trs1 = null;
            tzIdQuery = "select timezone from company where companyid  = (select companyid from users where userid=?)";
            trs1 = DbUtil.executeQuery(conn, tzIdQuery, new Object[]{uid});
            if(!trs1.next()||StringUtil.isNullOrEmpty(trs1.getString("timezone"))){
                trs1 = null;
                tzIdQuery = "select id as timezone from systemtimezone";
                trs1 = DbUtil.executeQuery(conn, tzIdQuery);
            }
        }
        id = trs1.getString("timezone");
        DbResults trs = DbUtil.executeQuery(conn, tzDiffQuery, new Object[]{id});
        String tzid = (trs.getString("tzid"));
        return getDSTSpecificTz(tzid);
    }
    
    public static String getUserCompanyTimeZoneDifference(Connection conn, String userid) throws ServiceException {
        String result = "";
        String tzDiffQuery = " SELECT tzid FROM timezone WHERE id = ? ";
        String tzIdQuery = "select timezone from users where userid = ?";
        DbResults trs1 = DbUtil.executeQuery(conn, tzIdQuery, new Object[]{userid});
        if(!trs1.next()||StringUtil.isNullOrEmpty(trs1.getString("timezone"))){
            trs1 = null;
            tzIdQuery = "select timezone from company where companyid  = (select companyid from users where userid=?)";
            trs1 = DbUtil.executeQuery(conn, tzIdQuery, new Object[]{userid});
            if(!trs1.next()||StringUtil.isNullOrEmpty(trs1.getString("timezone"))){
                trs1 = null;
                tzIdQuery = "select timezone from company where companyid = ?";
                trs1 = DbUtil.executeQuery(conn, tzIdQuery, new Object[]{userid});
                if(!trs1.next()||StringUtil.isNullOrEmpty(trs1.getString("timezone"))){
                    trs1 = null;
                    tzIdQuery = "select id as timezone from systemtimezone";
                    trs1 = DbUtil.executeQuery(conn, tzIdQuery);
                }
            }
        }
        String id = trs1.getString("timezone");
        DbResults trs = DbUtil.executeQuery(conn, tzDiffQuery, new Object[]{id});
        if(trs.next()) {
            result = trs.getString("tzid");
        }
        return getDSTSpecificTz(result);
    }
    
    private static String getDSTSpecificTz(String tzid){
        Calendar calTZ = Calendar.getInstance(TimeZone.getTimeZone(tzid));
        TimeZone tz = calTZ.getTimeZone();
        int miliseconds = tz.getOffset(calTZ.getTimeInMillis());
        String time = "";
        String signNum = "+";

        if(miliseconds < 0) {
            miliseconds = miliseconds * (-1);
            signNum = "-";
        }

        int seconds = miliseconds/1000;

        String min = "";        // Calculating minutes
        if(seconds > 60) {
            min = String.valueOf(seconds/60%60);
            if((seconds/60%60)<10) {
                min = "0" + String.valueOf(seconds/60%60);
            }

            if(min.compareTo("0") != 0 && min.compareTo("30") != 0) {       // Checking whether the minimum unit of minutes is 30 mins.
                if(min.compareTo("45") == 0) {
                    min = "30";

                } else {
                    min = "00";
                }
            }

        } else {
            min = "00";
        }

        String hours = "";      // Calculating hours

        if(seconds/60 > 60) {
            hours = String.valueOf(seconds/60/60);
            if((seconds/60/60) < 10) {
                hours = "0" + String.valueOf(seconds/60/60);
            }

        } else {
           hours = "00";
        }

        time = signNum + hours + ":" + min;

        return time;
    }

    public static String getSystemTimezone(Connection conn) throws ServiceException{
        DbResults rs = DbUtil.executeQuery(conn, "select tzid from systemtimezone");
        String diff = null;
        if(rs.next()){
            diff = rs.getString("tzid");
        }
        return getDSTSpecificTz(diff);
    }
    
    public static String getSystemTimezoneSName(Connection conn) throws ServiceException{
        DbResults rs = DbUtil.executeQuery(conn, "select sname from systemtimezone");
        String diff = null;
        if(rs.next()){
            diff = rs.getString("sname");
        }
        return diff;
    }
    
    public static String[] getVenueTimezone(Connection conn,String venueid) throws ServiceException{
        DbResults rs = DbUtil.executeQuery(conn, "select difference,sname   from timezone inner join lvenue on timezone.id=lvenue.timezone where lvenue.venueid = ?",venueid);
        String[] result = new String[2];
        result[0]="";
        result[1]="";
        if(rs.next()){
            result[0] = rs.getString("difference");
            result[1] = rs.getString("sname");
        }
        if(result[1].compareTo("")==0)
            result[1]=Timezone.getSystemTimezoneSName(conn);
        return result;
    }
    
    public static String[] getGmtDate(Connection conn, String date1, String userid) throws ServiceException, ParseException {
        
        String tzUser = getTimeZone(conn, userid);
        
        Calendar calInstance = Calendar.getInstance();
        calInstance.setTimeZone(java.util.TimeZone.getTimeZone("GMT" + tzUser));
        TimeZone tz = calInstance.getTimeZone();
        int temp = tz.getRawOffset();
        java.text.SimpleDateFormat format0 = new SimpleDateFormat("yyyy-MM-d HH:mm:ss");
        java.text.SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-d HH:mm:ss");
        java.util.Calendar cal0 = Calendar.getInstance(new SimpleTimeZone(0, "GMT"));
        java.util.Calendar cal1 = Calendar.getInstance(new SimpleTimeZone(temp, tzUser));
        format0.setCalendar(cal0);
        format1.setCalendar(cal1);
        String dateArr[] = date1.split(" ");
        if(dateArr.length == 1 || dateArr[1].equals("00:00:00")) {
            date1 = dateArr[0] + " " + "00:00:01";
        }
        java.util.Date date = format1.parse(date1);
        String result = format0.format(date);
        
        String[] results = result.split(" ");
        return results;
    }
    
    public static String[] getTzonetoGmt(String date1, String tzUser) throws ServiceException, ParseException {
        
        
        Calendar calInstance = Calendar.getInstance();
        calInstance.setTimeZone(java.util.TimeZone.getTimeZone("GMT" + tzUser));
        TimeZone tz = calInstance.getTimeZone();
        int temp = tz.getRawOffset();
        java.text.SimpleDateFormat format0 = new SimpleDateFormat("yyyy-MM-d HH:mm:ss");
        java.text.SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-d HH:mm:ss");
        java.util.Calendar cal0 = Calendar.getInstance(new SimpleTimeZone(0, "GMT"));
        java.util.Calendar cal1 = Calendar.getInstance(new SimpleTimeZone(temp, tzUser));
        format0.setCalendar(cal0);
        format1.setCalendar(cal1);
        String dateArr[] = date1.split(" ");
        if(dateArr[1].equals("00:00:00")) {
            date1 = dateArr[0] + " " + "00:00:01";
        }
        java.util.Date date = format1.parse(date1);
        String result = format0.format(date);
        
        String[] results = result.split(" ");
            if(results[1].equals("00:00:00")){
                results[1] = "00:00:01";
            }
        return results;
    }
    
    public static String toTimezone(Connection conn, String column, String table, String key1, String key2, String value1, String value2,String timeZone) throws ServiceException {
        String result = "";
        String query = "select DATE_FORMAT(CONVERT_TZ("+ column +", '+00:00' , '"+ timeZone +"'),'%m/%d/%Y %h:%i:%S %p') " +
                "as time_convt from "+ table +" where "+ key1 +" = '"+ value1 +"' and "+ key2 +"='"+value2+"';";
        DbResults rs = DbUtil.executeQuery(conn, query);
        while(rs.next()) {
            result = rs.getString("time_convt");
        }
        return result;
    }

    public static String getUserCompanyTimeZone(Connection conn, String userid, String companyid) throws ServiceException {
        String result = "";
        String query = "select timezone.id as tmzid from timezone where timezone.id = (select CASE" +
                "(IFNULL((select timezone from users where userid = ? ),0)) WHEN 0 THEN (select timezone from company" +
                " where companyid = ?) ELSE (select id from systemtimezone) END)";
        DbResults rs = DbUtil.executeQuery(conn, query, new Object[]{userid, companyid});
        if(rs.next()) {
            result = String.valueOf(rs.getInt("tmzid"));
        }
        return result;
    }

    public static String toGmtTimezone(Connection conn, String userid, String date) throws ServiceException{
        String userDate = "";
//        String timezone = getUserCompanyTimeZoneDifference(conn, userid);
//        if(timezone.startsWith("-"))
//              timezone = "+"+timezone.substring(1);
//        else if(timezone.startsWith("+"))
//              timezone = "-"+timezone.substring(1);
        userDate = toUserDefTimezone(conn, date, "+00:00");//user to gmt
        return userDate;
    }

    public static String getUserToGmtTimezone(Connection conn, String userid, String date) throws ServiceException{
//        String userDate = date;
//        userDate = toGmtTimezone(conn, userid, date);
//        userDate = toUserDefTimezone(conn, userDate, "+00:00");// gmt to db
//        return userDate;
        return SchedulingUtilities.getFormattedDateString(date, "yyyy-MM-dd HH:mm:ss");
    }

    public static Date getUserToGmtTimezoneDate(Connection conn, String userid, String date) throws ServiceException {
        return SchedulingUtilities.getDate(date);
    }

    public static Timestamp getUserToGmtTimezoneTimestamp(Connection conn, String userid, String date) throws ServiceException{
        java.util.Date d = new java.util.Date();
        Timestamp t = null;
        try {
            String dt = getUserToGmtTimezone(conn, userid, date);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            d = sdf.parse(dt);
            t = new Timestamp(d.getTime());
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("Timezone.getUserToGmtTimezone", ex);
        } finally {
            return t;
        }
    }

    public static String dateTimeRenderer(Connection conn, String refDate, String datestring, String userid){
        String dur = "";
        java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try{
            java.util.Date usrdt = sdf1.parse(datestring);
            java.util.Date curdt = sdf1.parse(refDate);
            long millis = curdt.getTime() - usrdt.getTime();
            int duration = (int) ((curdt.getTime() - usrdt.getTime())/1000);
            int days = duration/(3600 * 24);
            if(days > 0){
                if(days <= 7){
                    dur = (days == 1) ? Integer.toString(days) + " day ago" : Integer.toString(days) + " days ago";
                    usrdt = new java.util.Date(millis);
                    usrdt = sdf1.parse(toCompanyTimezone(conn, sdf1.format(usrdt), userid));
                    dur += " (" + new java.text.SimpleDateFormat("hh:mm a").format(usrdt) + ") ";
                } else {
                    dur = datestring;
                    java.util.Date dt = sdf1.parse(dur);
                    dur = sdf1.format(dt);
                }
            } else {
                int hours = duration / 3600;
                int min = (duration % 3600) / 60;
                if(hours > 0)
                    dur = (hours == 1) ? Integer.toString(hours) + " hour" : Integer.toString(hours) + " hours";
                dur += " " + ((min == 1) ? Integer.toString(min) + " minute ago" : Integer.toString(min) + " minutes ago");
            }
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("Timezone.dateTimeRenderer", ex);
        } finally{
            return dur;
        }
    }

    public static String convertToUserPref(Connection conn, String dateStr, String userid){
        String ret = "";
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(dateFormatHandlers.getUserDateFormat(conn, userid));
            java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                java.util.Date dt = sdf1.parse(dateStr);
                ret = sdf.format(dt);
            } catch (ParseException ex) {
                ret = dateStr;
            }
        } catch (ServiceException ex) {
            Logger.getLogger(Timezone.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return ret;
        }
    }
    
    public static String getCompanyTimeZoneDifference(Connection conn, String companyid) throws ServiceException {
        String result = "";
        String tzDiffQuery = " SELECT tzid FROM timezone WHERE id = ? ";
        DbResults trs1 = null;
        String tzIdQuery = "select timezone from company where companyid = ?";
        trs1 = DbUtil.executeQuery(conn, tzIdQuery, new Object[]{companyid});
        if(!trs1.next()||StringUtil.isNullOrEmpty(trs1.getString("timezone"))){
            trs1 = null;
            tzIdQuery = "select id as timezone from systemtimezone";
            trs1 = DbUtil.executeQuery(conn, tzIdQuery);
        }
        String id = trs1.getString("timezone");
        DbResults trs = DbUtil.executeQuery(conn, tzDiffQuery, new Object[]{id});
        if(trs.next()) {
            result = trs.getString("tzid");
        }
        return getDSTSpecificTz(result);
    }
    
    public static String toCompanyTimezone(Connection conn, String date, String companyid) throws ServiceException {
        String result = "";
        String query = "select DATE_FORMAT(CONVERT_TZ('"+ date +"', '"+getSystemTimezone(conn)+"' , '"+ getCompanyTimeZoneDifference(conn, companyid) +"'),'%Y-%m-%d %H:%i:%S') as time_convt; ";
        DbResults rs = DbUtil.executeQuery(conn, query);
        while(rs.next()) {
            result = rs.getString("time_convt");
        }
        return result;
    }
    
    public static String fromCompanyToSystem(Connection conn, String date, String companyid) throws ServiceException{
        String result = "";
        String query = "select DATE_FORMAT(CONVERT_TZ('"+ date +"' , '"+ getCompanyTimeZoneDifference(conn, companyid) +"', '"+getSystemTimezone(conn) +"'),'%Y-%m-%d %H:%i:%S') as time_convt; ";
        DbResults rs = DbUtil.executeQuery(conn, query);
        while(rs.next()) {
            result = rs.getString("time_convt");
        }
        return result;
    }
    
    public static Timestamp fromCompanyToSystemTimestamp(Connection conn, String date, String companyid) throws ServiceException{
        java.util.Date d = new java.util.Date();
        Timestamp t = null;
        try {
            String dt = fromCompanyToSystem(conn, date, companyid);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            d = sdf.parse(dt);
            t = new Timestamp(d.getTime());
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("Timezone.getUserToGmtTimezone", ex);
        } finally {
            return t;
        }
    }
   
}
