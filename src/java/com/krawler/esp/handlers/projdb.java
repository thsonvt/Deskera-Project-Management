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

import com.krawler.common.locale.MessageSourceProxy;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import net.sf.mpxj.utility.DateUtility;

import com.krawler.esp.web.resource.Links;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.KWLErrorMsgs;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.database.DbPool;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.database.DbPool.Connection;
import com.krawler.esp.portalmsg.Mail;
import com.krawler.svnwebclient.configuration.ConfigurationException;
import com.krawler.utils.json.KWLJsonConverter;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.krawler.common.timezone.Timezone;
import com.krawler.common.util.Constants;
import com.krawler.common.util.SchedulingUtilities;
import com.krawler.common.util.Utilities;
import com.krawler.esp.company.CompanyDAO;
import com.krawler.esp.company.CompanyDAOImpl;
import com.krawler.esp.project.meter.HealthMeter;
import com.krawler.esp.project.meter.HealthMeterDAO;
import com.krawler.esp.project.meter.HealthMeterDAOImpl;
import com.krawler.esp.project.resource.BaselineResource;
import com.krawler.esp.project.resource.ProjectResource;
import com.krawler.esp.project.resource.Resource;
import com.krawler.esp.project.resource.ResourceDAO;
import com.krawler.esp.project.resource.ResourceDAOImpl;
import com.krawler.esp.servlets.AdminServlet;
import com.krawler.esp.servlets.importProjectPlanCSV;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import com.krawler.esp.project.checklist.CheckList;
import com.krawler.esp.project.checklist.CheckListManager;

public class projdb {

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

    public static String[] getCompHolidays(Connection conn, String projid, String nonworkdays) {
        String[] holidayArr = new String[1];
        try {
            String Holidays = getCmpHolidaydays(conn, projid);
            if (nonworkdays.compareTo("") == 0) {
                nonworkdays = getNonWorkWeekdays(conn, projid);
            }
            JSONObject nmweekObj = new JSONObject(nonworkdays);
            holidayArr[0] = "";
            if (Holidays.compareTo("{data:{}}") != 0) {
                nmweekObj = new JSONObject(Holidays);
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

    public static int getNextRow(Connection conn) throws ServiceException {//not in use
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try {

            pstmt = conn.prepareStatement("select count(*) from pro_task");
            rs = pstmt.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getNextRow", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

    public static String getMaxTimestamp(Connection conn)// not in use
            throws ServiceException {
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("select max(lastupdated) from proj_buffer");
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getTimestamp(1).toString();
            } else {
                return "terror";
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getMaxTimestamp", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

    public static String getTask(Connection conn, String projectid, int offset,
            int limit) throws ServiceException {
        PreparedStatement pstmt = null;
        try {
            KWLJsonConverter kjs = new KWLJsonConverter();
            ResultSet rs = null;
            if (limit == -1) {
                pstmt = conn.prepareStatement("SELECT taskid, taskindex as taskindex, taskname, duration, startdate, enddate, "
                        + "parent, level, actualstartdate, actualduration, percentcomplete, notes, priority, isparent, workholidays "
                        + "FROM proj_task WHERE projectid = ? ORDER BY taskindex");
                pstmt.setString(1, projectid);
            } else {
                pstmt = conn.prepareStatement("SELECT taskid, taskindex as taskindex, taskname, duration, startdate, enddate, "
                        + "parent, level, actualstartdate, actualduration, percentcomplete, notes, priority, isparent, workholidays "
                        + "FROM proj_task WHERE projectid = ? ORDER BY taskindex LIMIT ? OFFSET ?");
                pstmt.setString(1, projectid);
                pstmt.setInt(2, limit);
                pstmt.setInt(3, offset);
            }
            rs = pstmt.executeQuery();
            String json = null;
            json = kjs.GetJsonForGrid(rs);
            return json;
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getTask", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

    public static String getBaselineTask(Connection conn, String baselineid, int offset,
            int limit) throws ServiceException {
        PreparedStatement pstmt = null;
        try {
            KWLJsonConverter kjs = new KWLJsonConverter();
            ResultSet rs = null;
            if (limit == -1) {
                pstmt = conn.prepareStatement("SELECT taskid, taskindex as taskindex, taskname, duration, startdate, enddate, "
                        + "parent, level, actualstartdate, actualduration, percentcomplete, notes, priority, isparent, workholidays "
                        + "FROM proj_baselinedata WHERE baselineid = ? ORDER BY taskindex");
                pstmt.setString(1, baselineid);
            } else {
                pstmt = conn.prepareStatement("SELECT taskid, taskindex as taskindex, taskname, duration, startdate, enddate, "
                        + "parent, level, actualstartdate, actualduration, percentcomplete, notes, priority, isparent, workholidays "
                        + "FROM proj_baselinedata WHERE baselineid = ? ORDER BY taskindex LIMIT ? OFFSET ?");
                pstmt.setString(1, baselineid);
                pstmt.setInt(2, limit);
                pstmt.setInt(3, offset);
            }
            rs = pstmt.executeQuery();
            String json = null;
            json = kjs.GetJsonForGrid(rs);
            return json;
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getBaselineTask", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

    public static String getUserTasks(Connection conn, String projectid, String userid, int offset,
            int limit) throws ServiceException {
        PreparedStatement pstmt = null;
        try {
            KWLJsonConverter kjs = new KWLJsonConverter();
            ResultSet rs = null;
            if (limit == -1) {
                pstmt = conn.prepareStatement("SELECT taskid, taskindex as taskindex, taskname, duration, startdate, enddate, "
                        + "parent, level, actualstartdate, actualduration, percentcomplete, notes, priority, isparent, workholidays "
                        + "FROM proj_task WHERE projectid = ? AND taskid IN "
                        + "(SELECT taskid FROM proj_taskresourcemapping inner join userlogin on userlogin.userid = proj_taskresourcemapping.resourceid where userlogin.isactive = true and resourceid = ?)"
                        + " ORDER BY taskindex");
                pstmt.setString(1, projectid);
                pstmt.setString(2, userid);
            } else {
                pstmt = conn.prepareStatement("SELECT taskid, taskindex as taskindex, taskname, duration, startdate, enddate, "
                        + "parent, level, actualstartdate, actualduration, percentcomplete, notes, priority, isparent, workholidays "
                        + "FROM proj_task WHERE projectid = ? AND taskid IN "
                        + " (SELECT taskid FROM proj_taskresourcemapping inner join userlogin on userlogin.userid = proj_taskresourcemapping.resourceid where userlogin.isactive = true and resourceid = ?) "
                        + "ORDER BY taskindex LIMIT ? OFFSET ?");
                pstmt.setString(1, projectid);
                pstmt.setString(2, userid);
                pstmt.setInt(3, limit);
                pstmt.setInt(4, offset);
            }
            rs = pstmt.executeQuery();
            String json = null;
            json = kjs.GetJsonForGrid(rs);
            return json;
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getTask", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

    public static String getDateWiseTasks(Connection conn, String projectid, java.sql.Date date1, java.sql.Date date2, String action, String userid) throws ServiceException, JSONException {
        PreparedStatement pstmt = null;
        try {
            KWLJsonConverter kjs = new KWLJsonConverter();
            date1 = new java.sql.Date(Timezone.getUserToGmtTimezoneDate(conn, userid, date1.toString()).getTime());
            date2 = new java.sql.Date(Timezone.getUserToGmtTimezoneDate(conn, userid, date2.toString()).getTime());
            ResultSet rs = null;
            pstmt = conn.prepareStatement("select taskid, taskindex as taskindex, taskname, duration, startdate, enddate, "
                    + "concat(percentcomplete, '%') as percentcomplete from proj_task where projectid = ? and "
                    + action.substring(0, action.length() - 5) + " <= ? and "
                    + action.substring(0, action.length() - 5) + " >= ? and startdate is not null order by taskindex");
            pstmt.setString(1, projectid);
            pstmt.setDate(2, date2);
            pstmt.setDate(3, date1);
            rs = pstmt.executeQuery();
            String json = null;
            json = kjs.GetJsonForGrid(rs);
            if (json.compareTo("{data:{}}") != 0) {
                JSONObject job;
                job = new JSONObject(json);
                for (int i = 0; i < job.getJSONArray("data").length(); i++) {
                    String dtStr = job.getJSONArray("data").getJSONObject(i).getString("startdate");
                    job.getJSONArray("data").getJSONObject(i).remove("startdate");
                    job.getJSONArray("data").getJSONObject(i).put("startdate", dtStr);
                    dtStr = job.getJSONArray("data").getJSONObject(i).getString("enddate");
                    job.getJSONArray("data").getJSONObject(i).remove("enddate");
                    job.getJSONArray("data").getJSONObject(i).put("enddate", dtStr);
                }
                json = job.toString();
            }
            return json;
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getDateWiseTasks", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

    public static String checkForBaselineTask(Connection conn, String baselineid, String taskid) throws ServiceException {
        JSONObject j = null;
        PreparedStatement pstmt = null;
        String ret = "";
        try {
            ResultSet rs = null;
            pstmt = conn.prepareStatement("select taskname, taskid, startdate, enddate, duration from proj_baselinedata"
                    + " where baselineid = ? and taskid = ?");
            pstmt.setString(1, baselineid);
            pstmt.setString(2, taskid);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                ret = rs.getString("taskid");
                ret += "," + rs.getString("startdate");
                ret += "," + rs.getString("enddate");
                ret += "," + rs.getString("duration");
                ret += "," + rs.getString("taskname");
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.checkForBaselineTaskResource", e);
        } finally {
            DbPool.closeStatement(pstmt);
            return ret;
        }
    }

    public static String getDateWiseTasks(Connection conn, String projectid, String userid, java.sql.Date sdate, java.sql.Date edate) throws ServiceException, JSONException {
        PreparedStatement pstmt = null;
        try {
            KWLJsonConverter kjs = new KWLJsonConverter();
            ResultSet rs = null;
            sdate = new java.sql.Date(Timezone.getUserToGmtTimezoneDate(conn, userid, sdate.toString()).getTime());
            edate = new java.sql.Date(Timezone.getUserToGmtTimezoneDate(conn, userid, edate.toString()).getTime());
            pstmt = conn.prepareStatement("select DISTINCT taskid, taskindex, taskname, duration, startdate, enddate, concat(percentcomplete, '%') as percentcomplete from proj_task"
                    + " where projectid = ? "
                    + " and ((startdate <= date(?) and enddate >= date(?))"
                    + " or (startdate BETWEEN date(?) and date(?) "
                    + " or enddate BETWEEN date(?) and date(?))) order by taskindex");
            pstmt.setString(1, projectid);
            pstmt.setDate(2, sdate);
            pstmt.setDate(3, edate);
            pstmt.setDate(4, sdate);
            pstmt.setDate(5, edate);
            pstmt.setDate(6, sdate);
            pstmt.setDate(7, edate);
            rs = pstmt.executeQuery();
            String json = null;
            json = kjs.GetJsonForGrid(rs);
            if (json.compareTo("{data:{}}") != 0) {
                JSONObject job;
                job = new JSONObject(json);
                for (int i = 0; i < job.getJSONArray("data").length(); i++) {
                    String dtStr = job.getJSONArray("data").getJSONObject(i).getString("startdate");
                    job.getJSONArray("data").getJSONObject(i).remove("startdate");
                    job.getJSONArray("data").getJSONObject(i).put("startdate", dtStr);
                    dtStr = job.getJSONArray("data").getJSONObject(i).getString("enddate");
                    job.getJSONArray("data").getJSONObject(i).remove("enddate");
                    job.getJSONArray("data").getJSONObject(i).put("enddate", dtStr);
                }
                json = job.toString();
            }
            return json;
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getDateWiseTasks", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

    public static String getResourceTasks(Connection conn, String projectid, String baselineid, String userid, String companyid) throws ServiceException, JSONException {
        PreparedStatement pstmt = null;
        int flag = 0;
        try {
            KWLJsonConverter kjs = new KWLJsonConverter();
            ResultSet rs = null;
            ResultSet rs1 = null;
            pstmt = conn.prepareStatement("select proj_task.taskid, taskindex, resourceid, startdate, enddate "
                    + ",taskname from proj_task inner join proj_taskresourcemapping on "
                    + "proj_task.taskid = proj_taskresourcemapping.taskid and resourceid not in"
                    + "(select users.userid from userlogin inner join users on userlogin.userid = users.userid where users.companyid = ? and userlogin.isactive = false)"
                    + "and projectid = ?"
                    + "union "
                    + "select proj_task.taskid, taskindex,resourceid, startdate, enddate ,taskname from proj_task "
                    + "inner join proj_baselinetaskresources on proj_baselinetaskresources.taskid = proj_task.taskid "
                    + "and resourceid not in (select users.userid from userlogin inner join users on userlogin.userid = users.userid where users.companyid = ? and userlogin.isactive = false) and projectid = ? and baselineid = ?");
            pstmt.setString(1, companyid);
            pstmt.setString(2, projectid);
            pstmt.setString(3, companyid);
            pstmt.setString(4, projectid);
            pstmt.setString(5, baselineid);
            rs = pstmt.executeQuery();
            String json = null;
            json = kjs.GetJsonForGrid(rs);
            return json.toString();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getResourceTasks", e);
        } catch (Exception e) {
            throw ServiceException.FAILURE("projdb.getResourceTasks", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

   public static String getMilestones(Connection conn, String projectid, String userid) throws ServiceException, JSONException {
        PreparedStatement pstmt = null;
        try {
            KWLJsonConverter kwl = new KWLJsonConverter();
            ResultSet rs = null;
            pstmt = conn.prepareStatement("select taskid, taskindex as taskindex, taskname, duration, startdate, enddate,concat(percentcomplete, '%') as percentcomplete "
                    + "from proj_task where duration in ('0', '0.0', '0.00', '0d', '0.0d', '0.00d', '0.00days', '0 days', '0.0days', '0.00h') and projectid = ? order by taskindex");
            pstmt.setString(1, projectid);
            rs = pstmt.executeQuery();
            String json = null;
            json = kwl.GetJsonForGrid(rs);
            if (json.compareTo("{data:{}}") != 0) {
                JSONObject job;
                job = new JSONObject(json);
                for (int i = 0; i < job.getJSONArray("data").length(); i++) {
                    String dtStr = job.getJSONArray("data").getJSONObject(i).getString("startdate");
                    job.getJSONArray("data").getJSONObject(i).remove("startdate");
                    job.getJSONArray("data").getJSONObject(i).put("startdate", dtStr);
                    dtStr = job.getJSONArray("data").getJSONObject(i).getString("enddate");
                    job.getJSONArray("data").getJSONObject(i).remove("enddate");
                    job.getJSONArray("data").getJSONObject(i).put("enddate", dtStr);
                }
                json = job.toString();
            }
            return json;
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getMilestones", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

    public static String getProjectSummaryData(Connection conn, String projectid, String baselineid, String userid) throws ServiceException {
        PreparedStatement pstmt = null;
        String returnStr = "";
        String projstartdate = "";
        String projstartdate1 = "";
        String projminmaxdate = "";
        String projactualstartdate = "";
        String projactualstartdate1 = "";
        String baselinestartdateforvariance = "";
        String baselineenddateforvariance = "";
        String projenddate = "";
        String projenddate1 = "";
        String baselinestartdate = "";
        int averageProgress = 0;
        String baselineenddate = "";
        String Systemcurr_date = "";
        int projectRemainingDays = 0;
        int projectActualDays = 0;
        int projectdatediff = 0;
        String actual = "";
        int projectActualduration = 0;
        String baselinestartdateformat = "";
        String baselineenddateformat = "";
        String Systemprojenddate = "";
        String curr_date = "";
        double totalCost = 0;
        double totalBaselineCost = 0;
        double totalWork = 0;
        double totalBaselineWork = 0;
        double actcost = 0;
        double actwork = 0;
        double[] ret = null;
        double[] retBaseline = null;
        double[] remain = null;
        double[] actuals = null;
        double remainingcost = 0;
        double remainingWork = 0;
        String remainingProjectDuration = "";
        Boolean overdue = false;
        Boolean projectcomplete = false;
        int baselineduration = 0;
        java.util.Date d = new java.util.Date();
        try {
            ResourceDAO r = new ResourceDAOImpl();
            com.krawler.utils.json.base.JSONObject jobj = new JSONObject();
            com.krawler.utils.json.base.JSONObject jres = null;
            com.krawler.utils.json.base.JSONObject resobj = new JSONObject();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-d HH:mm:ss");
            java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd");
            ResultSet rs = null;
            pstmt = conn.prepareStatement("select min(startdate) as bstartdate,max(enddate) as benddate from proj_baselinedata where baselineid = ?");
            pstmt.setString(1, baselineid);
            rs = pstmt.executeQuery();
            if (rs.next()) {
//                baselinestartdate = Timezone.toUserTimezone(conn, rs.getString("bstartdate"), userid);
//                baselineenddate = Timezone.toUserTimezone(conn, rs.getString("benddate"), userid);
                baselinestartdate = rs.getString("bstartdate");
                baselineenddate = rs.getString("benddate");
                baselinestartdateforvariance = sdf.format(sdf.parse(baselinestartdate));
                baselineenddateforvariance = sdf.format(sdf.parse(baselineenddate));
                baselinestartdate = baselinestartdate.split(" ")[0];
                baselineenddate = baselineenddate.split(" ")[0];
                baselinestartdateformat = sdf.format(rs.getDate("bstartdate"));
                baselineenddateformat = sdf.format(rs.getDate("benddate"));
                baselineduration = getDaysDiff(baselinestartdateformat, baselineenddateformat);
            }
            projminmaxdate = getProjectDatesForReport(conn, projectid, userid);
            String projdate[] = projminmaxdate.split(",");
            if (projdate.length != 3) {
                projstartdate = projdate[0];
            } else {
                projstartdate = projdate[2];
            }
            java.util.Date projactualstd = sdf.parse(projdate[0]);
            projactualstartdate = sdf.format(projactualstd);
            projactualstartdate1 = sdf1.format(projactualstd);
            projenddate = projdate[1];
            java.util.Date dt = sdf.parse(projstartdate);
            projstartdate = sdf.format(dt);
            projstartdate1 = Timezone.toCompanyTimezone(conn, projstartdate, CompanyHandler.getCompanyByUser(conn, userid));
            java.util.Date dt1 = sdf.parse(projenddate);
            dt = sdf1.parse(projenddate);
            projenddate = sdf.format(dt);
            Systemprojenddate = sdf.format(dt1);
            projenddate1 = sdf1.format(dt);
            d = sdf.parse(sdf.format(d));
            java.sql.Date SystemcurrentDate = new java.sql.Date(new Date().getTime());
            curr_date = Timezone.getUserToGmtTimezone(conn, userid, sdf.format(new Date()));
            Systemcurr_date = sdf.format(new Date());
            pstmt = conn.prepareStatement("select taskid from proj_task where projectid = ? and percentcomplete < 100");
            pstmt.setString(1, projectid);
            rs = pstmt.executeQuery();
            if (rs.next() && dt1.before(new Date()) && projactualstd.before(new Date())) {
                overdue = true;
            }
            pstmt = conn.prepareStatement("select avg(percentcomplete) as avgprogress from proj_task where projectid = ?");
            pstmt.setString(1, projectid);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                if (rs.getInt("avgprogress") == 100) {
                    projectcomplete = true;
                }
                averageProgress = rs.getInt("avgprogress");
            }
            jobj.put("Percent Complete", averageProgress);
            jobj.put("Baseline Start Date", baselinestartdate);
            jobj.put("Baseline End Date", baselineenddate);
            jobj.put("baselinestartdateforvariance", baselinestartdateforvariance);
            jobj.put("baselineenddateforvariance", baselineenddateforvariance);
            jobj.put("Baseline Duration", baselineduration);
            jobj.put("Project Start Date", sdf1.format(sdf1.parse(projstartdate1)));
            jobj.put("projectstartdateforvariance", projstartdate);
            jobj.put("Project Actual Start Date", projactualstartdate1);
            if (!overdue) {
                jobj.put("Project End Date", projenddate1);
                jobj.put("projectenddateforvariance", projenddate);
            } else {
                jobj.put("Project End Date", "NA");
                jobj.put("projectenddateforvariance", "NA");
            }
            if (!projectcomplete) {
                if (overdue) {
                    remainingProjectDuration = "NA";
                } else {
                    projectRemainingDays = getDaysDiff(curr_date, projenddate);
                    remainingProjectDuration = "" + projectRemainingDays;
                    pstmt = conn.prepareStatement("select taskid, startdate, enddate from proj_task where projectid = ? and startdate is not null");
                    pstmt.setString(1, projectid);
                    rs = pstmt.executeQuery();
                    while (rs.next()) {
                        java.util.Date temp = dt;
                        java.util.Date currentDate = sdf1.parse(sdf.format(d));
                        if (rs.getDate("enddate").after(SystemcurrentDate) && rs.getDate("startdate").before(SystemcurrentDate)) {
                            remain = projdb.getDateWiseTaskCostWork(conn, rs.getString("taskid"), projectid, currentDate, temp, userid, false);
                            remainingcost += remain[0];
                            remainingWork += remain[1];
                        } else {
                            remainingcost += 0.0;
                            remainingWork += 0.0;
                        }
                    }
                }
                projectActualDays = getDaysDiff(projactualstartdate, Systemcurr_date);
            } else {
                remainingProjectDuration = "0";
                projectActualDays = getDaysDiff(projactualstartdate, Systemprojenddate);
            }
            actual = "" + projectActualDays;
            projectActualduration = getDaysDiff(projstartdate, projenddate);
            jobj.put("Actual Duration", actual);
            jobj.put("Scheduled Duration", projectActualduration);
            jobj.put("Remaining Duration", remainingProjectDuration);
            int totalCount = 0;
            pstmt = conn.prepareStatement("select taskid from proj_task where projectid = ?");
            pstmt.setString(1, projectid);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                java.util.Date temp1 = sdf.parse(projstartdate);
                java.util.Date temp2 = dt;
                ret = projdb.getDateWiseTaskCostWork(conn, rs.getString("taskid"), projectid, temp1, temp2, userid, false);
                totalCost += ret[0];
                totalWork += ret[1];
                totalCount++;
            }
            jobj.put("Scheduled Cost", totalCost);
            jobj.put("Scheduled Work", totalWork);
            if (sdf.parse(projstartdate).after(SystemcurrentDate) && sdf.parse(projactualstartdate).after(SystemcurrentDate)) {
                remainingWork = totalWork;
                remainingcost = totalCost;
            }
            jobj.put("Remaining Work", remainingWork);
            jobj.put("Remaining Cost", remainingcost);
            int unstarttask = getTaskReport(conn, 0, projectid);
            int completetask = getTaskReport(conn, 100, projectid);
            int Inprogresstask = getTaskReport(conn, -99, projectid);
            jobj.put("Unstarted Tasks", unstarttask);
            jobj.put("Completed Tasks", completetask);
            jobj.put("Inprogress Tasks", Inprogresstask);
            jobj.put("Total Tasks", totalCount);
            pstmt = conn.prepareStatement("select taskid, duration from proj_baselinedata where baselineid = ?");
            pstmt.setString(1, baselineid);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                if (!StringUtil.isNullOrEmpty(rs.getString("duration"))) {
                    java.util.Date temp1 = sdf1.parse(baselinestartdateformat);
                    java.util.Date temp2 = sdf1.parse(baselineenddateformat);
                    retBaseline = projdb.getTaskCostWorkForBaseline(conn, rs.getString("taskid"), rs.getString("duration"), baselineid, projectid, temp1, temp2, userid);
                    totalBaselineCost += retBaseline[0];
                    totalBaselineWork += retBaseline[1];
                }
                dt = sdf.parse(projactualstartdate);
                java.util.Date temp = dt;
                java.util.Date currentDate = d;
                actuals = projdb.getDateWiseTaskCostWork(conn, rs.getString("taskid"), projectid, temp, currentDate, userid, true);
                actcost += actuals[0];
                actwork += actuals[1];

            }
            jobj.put("Baseline Cost", totalBaselineCost);
            jobj.put("Baseline Work", totalBaselineWork);
            jobj.put("Actual Cost", actcost);
            jobj.put("Actual Work", actwork);

            returnStr = r.getResourcesCategories(conn, projectid);
            jres = new JSONObject(returnStr);
            int length = jres.getJSONArray("data").length();
            for (int i = 0; i < length; i++) {
                com.krawler.utils.json.base.JSONObject jtemp = new JSONObject();
                jtemp.put("count", jres.getJSONArray("data").getJSONObject(i).getInt("count"));
                jtemp.put("type", jres.getJSONArray("data").getJSONObject(i).getString("categoryname"));
                resobj.append("resources", jtemp);
            }
            resobj.append("data", jobj);
            if (resobj.has("data")) {
                returnStr = resobj.toString();
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getProjectSummaryData", e);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("projdb.getProjectSummaryData", ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("projdb.getProjectSummaryData", ex);
        } finally {
            DbPool.closeStatement(pstmt);
            return returnStr;
        }
    }

    public static JSONObject getAllProjectSummaryData(String reportJson) throws ServiceException {
        JSONObject j = null;
        JSONObject tempData = null;
        int enddatevariance = 0;
        String temp = "";
        double variance = 0.0;
        double costvariance = 0.0;
        double workvariance = 0.0;
        int totaltask = 0;
        int totalresource = 0;
        try {
            j = new JSONObject(reportJson);
            int startdatevariance = projdb.getDaysDiff(j.getJSONArray("data").getJSONObject(0).getString("projectstartdateforvariance"), j.getJSONArray("data").getJSONObject(0).getString("baselinestartdateforvariance"));
            if (j.getJSONArray("data").getJSONObject(0).getString("projectenddateforvariance").compareTo("NA") == 0 || j.getJSONArray("data").getJSONObject(0).getString("baselineenddateforvariance").compareTo("NA") == 0) {
                temp = "NA";
            } else {
                enddatevariance = projdb.getDaysDiff(j.getJSONArray("data").getJSONObject(0).getString("projectenddateforvariance"), j.getJSONArray("data").getJSONObject(0).getString("baselineenddateforvariance"));
                temp = "" + enddatevariance + "";
            }
            variance = Double.parseDouble(j.getJSONArray("data").getJSONObject(0).getString("Scheduled Duration")) - Double.parseDouble(j.getJSONArray("data").getJSONObject(0).getString("Baseline Duration"));
            costvariance = Double.parseDouble(j.getJSONArray("data").getJSONObject(0).getString("Scheduled Cost")) - Double.parseDouble(j.getJSONArray("data").getJSONObject(0).getString("Baseline Cost"));
            workvariance = Double.parseDouble(j.getJSONArray("data").getJSONObject(0).getString("Scheduled Work")) - Double.parseDouble(j.getJSONArray("data").getJSONObject(0).getString("Baseline Work"));
            totaltask = (Integer.parseInt(j.getJSONArray("data").getJSONObject(0).getString("Unstarted Tasks"))
                    + Integer.parseInt(j.getJSONArray("data").getJSONObject(0).getString("Completed Tasks"))
                    + Integer.parseInt(j.getJSONArray("data").getJSONObject(0).getString("Inprogress Tasks")));
            totaltask = Integer.parseInt(j.getJSONArray("data").getJSONObject(0).getString("Total Tasks")) - totaltask;
            j.getJSONArray("data").getJSONObject(0).put("Parent Tasks", totaltask);
            for (int i = 0; i < j.getJSONArray("resources").length(); i++) {
                if (i + 1 == j.getJSONArray("resources").length()) {
                    totalresource += Integer.parseInt(j.getJSONArray("resources").getJSONObject(i).getString("count"));
                } else {
                    totalresource += Integer.parseInt(j.getJSONArray("resources").getJSONObject(i).getString("count"));
                }
            }
            tempData = j.getJSONArray("data").getJSONObject(0);//.("Start Date Variance",startdatevariance);
            tempData.put("Start Variance", startdatevariance);
            j.remove("data");
            j.append("data", tempData);
            tempData = j.getJSONArray("data").getJSONObject(0);//.append("End Date Variance",enddatevariance);
            tempData.put("End Variance", enddatevariance);
            tempData.put("Duration Variance", variance);
            tempData.put("Cost Variance", costvariance);
            tempData.put("Work Variance", workvariance);
//            tempData.put("Total Tasks", totaltask);
            tempData.put("Total Resources", totalresource);
            j.remove("data");
            j.append("data", tempData);
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("projdb.getResourceTypeCount", ex);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("projdb.getResourceTypeCount", e);
        } finally {
            return j;
        }
    }
    public static int getDaysDiff(String Estartts, String Eendts) throws ServiceException {
        double days = 0;
        double diffInMilleseconds = 0;
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd 00:00:00");

            if (Estartts.compareTo("") != 0 && Eendts.compareTo("") != 0) {
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
            throw ServiceException.FAILURE("projdb.getDayDiff::get diff error", ex);
        } finally {
            return (int) days;
        }
    }

    public static String getOverdueTask(Connection conn, String projectid, String userid) throws ServiceException, JSONException {
        PreparedStatement pstmt = null;
        try {
            KWLJsonConverter kwl = new KWLJsonConverter();
            java.sql.Date DateVal = new java.sql.Date(new java.util.Date().getTime());
            ResultSet rs = null;
            pstmt = conn.prepareStatement("select taskid, taskindex as taskindex, taskname, duration, startdate, enddate, concat(percentcomplete, '%') as percentcomplete "
                    + "from proj_task where isparent = 0 and "
                    + "actualduration not in ('0', '0.0', '0.00', '0d', '0.0d', '0.00d', '0.00days', '0 days', '0.0days', '0.00h') and "
                    + "duration not in ('0', '0.0', '0.00', '0d', '0.0d', '0.00d', '0.00days', '0 days', '0.0days', '0.00h') and "
                    + "percentcomplete < 100 and enddate < ? and projectid = ? order by taskindex");
            pstmt.setDate(1, DateVal);
            pstmt.setString(2, projectid);
            rs = pstmt.executeQuery();
            String json = kwl.GetJsonForGrid(rs);
            if (json.compareTo("{data:{}}") != 0) {
                JSONObject job = new JSONObject(json);
                for (int i = 0; i < job.getJSONArray("data").length(); i++) {
                    String dtStr = job.getJSONArray("data").getJSONObject(i).getString("startdate");
                    job.getJSONArray("data").getJSONObject(i).remove("startdate");
                    job.getJSONArray("data").getJSONObject(i).put("startdate", dtStr);
                    dtStr = job.getJSONArray("data").getJSONObject(i).getString("enddate");
                    job.getJSONArray("data").getJSONObject(i).remove("enddate");
                    job.getJSONArray("data").getJSONObject(i).put("enddate", dtStr);
                }
                json = job.toString();
            }
            return json;
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getOverdueTask", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

    public static String getDurationCompareData(Connection conn, String projectid, String baselineid, String userid, boolean returnForGraph) throws ServiceException, ParseException, JSONException {
        PreparedStatement pstmt = null;
        String returnStr = "";
        try {
            ResultSet rs = null;
            ResultSet rs1 = null;
            double[] baseline = null;
            double[] actuals = null;
            double[] currentCost = null;
            double costvary = 0;
            double actualduration = 0;
            double baselineduration = 0;
            double differInDuration = 0;
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-d HH:mm:ss");
            java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd");
            java.util.Date d = new java.util.Date();
            d = sdf.parse(sdf.format(d));
            java.util.Date currentDate = d;
            com.krawler.utils.json.base.JSONObject resobj = new JSONObject();
            pstmt = conn.prepareStatement("select taskid, taskindex, taskname, duration, startdate, enddate from proj_task "
                    + "where projectid = ? and startdate is not null order by taskindex");
            pstmt.setString(1, projectid);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String tid = rs.getString("taskid");
                com.krawler.utils.json.base.JSONObject jobj = new JSONObject();
                java.util.Date temp1 = sdf.parse(rs.getString("startdate"));
                java.util.Date temp2 = sdf.parse(rs.getString("enddate"));
                actuals = getDateWiseTaskCostWork(conn, tid, projectid, temp1, temp2, userid, false);
                if (temp1.before(currentDate)) {
                    if(currentDate.after(temp2))
                        currentCost = getDateWiseTaskCostWork(conn, tid, projectid, temp1, currentDate, userid, false);
                    else
                        currentCost = getDateWiseTaskCostWork(conn, tid, projectid, temp1, currentDate, userid, true);
                    jobj.put("actualcost", Utilities.format(currentCost[0], returnForGraph));
                } else {
                    jobj.put("actualcost", "NA");
                }
                pstmt = conn.prepareStatement("select taskid, duration, startdate, enddate from proj_baselinedata where taskid = ? and baselineid = ? and startdate is not null");
                pstmt.setString(1, tid);
                pstmt.setString(2, baselineid);
                rs1 = pstmt.executeQuery();
                if (rs1.next()) {
                    temp1 = sdf1.parse(rs1.getString("startdate"));
                    temp2 = sdf1.parse(rs1.getString("enddate"));
                    baseline = getTaskCostWorkForBaseline(conn, rs1.getString("taskid"), rs1.getString("duration"), baselineid, projectid, temp1, temp2, userid);
                    double vary = actuals[1] - baseline[1];
                    costvary = actuals[0] - baseline[0];
                    double diff = 0;
                    String durationPlan = rs.getString("duration");
                    String durationBase = rs1.getString("duration");
                    if (durationPlan.contains("h")) {
                        actualduration = Double.parseDouble(durationPlan.substring(0, durationPlan.indexOf("h")));
                        if (durationBase.contains("h")) {
                            baselineduration = Double.parseDouble(durationBase.substring(0, durationBase.indexOf("h")));
                        } else if (durationBase.contains("d")) {
                            baselineduration = Double.parseDouble(durationBase.substring(0, durationBase.indexOf("d")));
                            baselineduration = baselineduration * 8;
                        } else {
                            baselineduration = Double.parseDouble(durationBase);
                            baselineduration = baselineduration * 8;
                        }
                    } else if (durationPlan.contains("d")) {
                        actualduration = Double.parseDouble(durationPlan.substring(0, durationPlan.indexOf("d")));
                        actualduration = actualduration * 8;
                        if (durationBase.contains("h")) {
                            baselineduration = Double.parseDouble(durationBase.substring(0, durationBase.indexOf("h")));
                        } else if (durationBase.contains("d")) {
                            baselineduration = Double.parseDouble(durationBase.substring(0, durationBase.indexOf("d")));
                            baselineduration = baselineduration * 8;
                        } else {
                            baselineduration = Double.parseDouble(durationBase);
                            baselineduration = baselineduration * 8;
                        }
                    } else {
                        actualduration = Double.parseDouble(durationPlan);
                        actualduration = actualduration * 8;
                        if (durationBase.contains("h")) {
                            baselineduration = Double.parseDouble(durationBase.substring(0, durationBase.indexOf("h")));
                        } else if (durationBase.contains("d")) {
                            baselineduration = Double.parseDouble(durationBase.substring(0, durationBase.indexOf("d")));
                            baselineduration = baselineduration * 8;
                        } else {
                            baselineduration = Double.parseDouble(durationBase);
                            baselineduration = baselineduration * 8;
                        }
                    }
                    diff = actualduration - baselineduration;
                    differInDuration = diff / 8;
                    jobj.put("durvary", Utilities.format(differInDuration, returnForGraph));
                    jobj.put("hoursvary", Utilities.format(vary, returnForGraph));
                    jobj.put("costvary", Utilities.format(costvary, returnForGraph));
                } else {
                    jobj.put("durvary", "NA");
                    jobj.put("hoursvary", "NA");
                    jobj.put("costvary", "NA");
                }
                jobj.put("taskname", rs.getString("taskname"));
                jobj.put("duration", rs.getString("duration"));
                jobj.put("work", Utilities.format(actuals[1], returnForGraph));
                jobj.put("cost", Utilities.format(actuals[0], returnForGraph));

                jobj.put("taskindex", rs.getInt("taskindex"));
                jobj.put("taskid", rs.getString("taskid"));
                resobj.append("data", jobj);
            }
            if (resobj.has("data")) {
                returnStr = resobj.toString();
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getMilestones", e);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("projdb.getProgressReport", ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("projdb.getProgressReport", ex);
        } finally {
            DbPool.closeStatement(pstmt);
            return returnStr;
        }
    }

    public static String getDateCompareData(Connection conn, String projectid, String baselineid, String userid) throws ServiceException, ParseException, JSONException {
        PreparedStatement pstmt = null;
        String returnStr = "";
        int startdiff = 0;
        int enddiff = 0;
        try {
            ResultSet rs = null;
            ResultSet rs1 = null;
            com.krawler.utils.json.base.JSONObject resobj = new JSONObject();
            pstmt = conn.prepareStatement("select taskid, taskindex, taskname, startdate, enddate from proj_task "
                    + "where projectid = ? and startdate is not null order by taskindex");
            pstmt.setString(1, projectid);
            rs = pstmt.executeQuery();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-d HH:mm:ss");
            java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd");
            while (rs.next()) {
                String tid = rs.getString("taskid");
                com.krawler.utils.json.base.JSONObject jobj = new JSONObject();
                pstmt = conn.prepareStatement("select taskid, startdate, enddate from proj_baselinedata where taskid = ? and baselineid = ? and startdate is not null");
                pstmt.setString(1, tid);
                pstmt.setString(2, baselineid);
                rs1 = pstmt.executeQuery();
                if (rs1.next()) {
                    startdiff = getDaysDiff(rs.getString("startdate"), rs1.getString("startdate"));
                    enddiff = getDaysDiff(rs.getString("enddate"), rs1.getString("enddate"));
                    jobj.put("startdiff", startdiff);
                    jobj.put("enddiff", enddiff);
                } else {
                    jobj.put("startdiff", "NA");
                    jobj.put("enddiff", "NA");
                }
                String sDate = sdf1.format(sdf1.parse(rs.getString("startdate")));
                String eDate = sdf1.format(sdf1.parse(rs.getString("enddate")));
                jobj.put("taskindex", rs.getString("taskindex"));
                jobj.put("taskname", rs.getString("taskname"));
                jobj.put("startdate", sDate);
                jobj.put("enddate", eDate);
                jobj.put("taskid", tid);
                resobj.append("data", jobj);
            }
            if (resobj.has("data")) {
                returnStr = resobj.toString();
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getDateCompareData", e);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("projdb.getDateCompareData", ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("projdb.getDateCompareData", ex);
        } finally {
            DbPool.closeStatement(pstmt);
            return returnStr;
        }
    }

    public static String getResourceCompareData(Connection conn, String projectid, String baselineid) throws ServiceException, ParseException, JSONException {
        PreparedStatement pstmt = null;
        String returnStr = "";
        try {
            ResultSet rs = null;
            ResultSet rs1 = null;
            com.krawler.utils.json.base.JSONObject resobj = new JSONObject();
            com.krawler.utils.json.base.JSONObject temp = null;
            String taskRes = "", baseTaskResNames = "", taskResNames = "";
            pstmt = conn.prepareStatement("select taskid, taskindex, taskname, startdate, enddate from proj_task "
                    + "where projectid = ? order by taskindex");
            pstmt.setString(1, projectid);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                taskResNames = "";
                String tid = rs.getString("taskid");
                com.krawler.utils.json.base.JSONObject jobj = new JSONObject();
                pstmt = conn.prepareStatement("select taskid, startdate, enddate from proj_baselinedata where taskid = ? and baselineid = ?");
                pstmt.setString(1, tid);
                pstmt.setString(2, baselineid);
                rs1 = pstmt.executeQuery();
                if (!rs1.next()) {
                    jobj.put("resourcename", getTaskResourcesNames(conn, tid, projectid));
                    jobj.put("baseresources", "");
                } else {
                    taskRes = getTaskResources(conn, tid, projectid);
                    temp = new JSONObject(taskRes);
                    if (temp.getInt("count") != 0) {
                        for (int i = 0; i < temp.getJSONArray("data").length(); i++) {
                            taskResNames += temp.getJSONArray("data").getJSONObject(i).getString("resourceName") + ",";
                        }
                        taskResNames = taskResNames.substring(0, taskResNames.length() - 1);
                    }
                    baseTaskResNames = getBaselineTaskResources(conn, tid, projectid, baselineid);
                    jobj.put("resourcename", taskResNames);
                    jobj.put("baseresources", baseTaskResNames);
                }
                jobj.put("taskindex", rs.getString("taskindex"));
                jobj.put("taskname", rs.getString("taskname"));
                jobj.put("taskid", tid);
                resobj.append("data", jobj);
            }
            if (resobj.has("data")) {
                returnStr = resobj.toString();
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getResourceCompareData", e);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("projdb.getResourceCompareData", ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("projdb.getResourceCompareData", ex);
        } finally {
            DbPool.closeStatement(pstmt);
            return returnStr;
        }
    }

    public static int getTaskReport(Connection conn, Integer percent, String projectid) throws ServiceException {
        PreparedStatement pstmt = null;
        try {
            int task = 0;
            ResultSet rs = null;
            String progress = "";
            if (percent == 0) {
                progress = KWLErrorMsgs.percentOrderStartDate;
            } else if (percent == 100) {
                progress = KWLErrorMsgs.percentOrderTaskIndex;
            } else if (percent == -99) {
                progress = KWLErrorMsgs.percent1to99OrderTaskIndex;
            }
            pstmt = conn.prepareStatement("select COUNT(taskid) as countOfTask from proj_task "
                    + "where isparent = 0 and projectid = ? and startdate <> '' and " + progress);
            pstmt.setString(1, projectid);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                task = rs.getInt("countOfTask");
            }
            return task;
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getTaskReport", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

    public static String getTaskDetails(Connection conn, String taskid)
            throws ServiceException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String returnStr = null;
        try {
            pstmt = conn.prepareStatement(" SELECT * FROM proj_task WHERE taskid = ? ");
            pstmt.setString(1, taskid);
            rs = pstmt.executeQuery();
            KWLJsonConverter kjc = new KWLJsonConverter();
            returnStr = kjc.GetJsonForGrid(rs);

        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getTaskDetails", e);

        } finally {
            DbPool.closeStatement(pstmt);
        }
        return returnStr;
    }

    public static String getProgressReport(Connection conn, Integer percent, String projectid) throws ServiceException {
        PreparedStatement pstmt = null;
        try {
            String returnStr = "{data:{}}";
            KWLJsonConverter kwl = new KWLJsonConverter();
            com.krawler.utils.json.base.JSONObject resobj = new JSONObject();
            ResultSet rs = null;
            String progress = "";
            if (percent == 0) {
                progress = KWLErrorMsgs.percentOrderStartDate;
            } else if (percent == 100) {
                progress = KWLErrorMsgs.percentOrderTaskIndex;
            } else if (percent == -99) {
                progress = KWLErrorMsgs.percent1to99OrderTaskIndex;
            }
            pstmt = conn.prepareStatement("select taskid, taskindex as taskindex, taskname, duration, startdate, "
                    + "enddate, concat(percentcomplete, '%') as percentcomplete from proj_task "
                    + "where isparent = 0 and projectid = ? and startdate <> '' and " + progress);
            pstmt.setString(1, projectid);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                com.krawler.utils.json.base.JSONObject jobj = new JSONObject();
                jobj.put("taskid", rs.getString("taskid"));
                jobj.put("taskindex", rs.getInt("taskindex"));
                jobj.put("taskname", rs.getString("taskname"));
                jobj.put("duration", rs.getString("duration"));
                jobj.put("startdate", rs.getTimestamp("startdate"));
                jobj.put("enddate", rs.getTimestamp("enddate"));
                jobj.put("percentcomplete", rs.getString("percentcomplete"));
                jobj.put("flag", true);
                resobj.append("data", jobj);
            }
            if (resobj.has("data")) {
                returnStr = resobj.toString();
            }
            return returnStr;
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getProgressReport", e);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("projdb.getProgressReport", ex);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

    public static String getProjectResources(Connection conn, String projectid, int offset,
            int limit, String searchString) throws ServiceException {
        try {
            StringBuilder temp = new StringBuilder();
            ResourceDAO resourceDAO = new ResourceDAOImpl();
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("ss", searchString);
            params.put("offset", offset);
            params.put("limit", limit);
            List ll = resourceDAO.getResourcesOnProject(conn, projectid, params);
            int count = resourceDAO.getTotalCount();
            temp.append(Utilities.listToGridJson(ll, count, ProjectResource.class));
            JSONObject jobj = new JSONObject(temp.toString());
            count = jobj.getJSONArray("data").length();
            String teamnames = "";
            for(int i=0; i<count; i++){
                teamnames = "";
                JSONObject jtemp = jobj.getJSONArray("data").getJSONObject(i);
                DbResults rs = DbUtil.executeQuery(conn, "select team.teamname as teamname, team.teamid as teamid from team " +
                        "inner join teammembers on team.teamid = teammembers.teamid " +
                        "inner join team_projectmapping on team.teamid = team_projectmapping.teamid where teammembers.resourceid = ? and team_projectmapping.projectid = ?"
                        , new Object[]{jtemp.getString("resourceID"), projectid});
                while(rs.next()){
                    teamnames += rs.getString("teamname") + ",";
                }
                if (!teamnames.equals("")) {
                    teamnames = teamnames.substring(0, teamnames.length() - 1);
                } else
                    teamnames = "--";
                jobj.getJSONArray("data").getJSONObject(i).put("teamname", teamnames);
            }
            return jobj.toString();
        } catch (JSONException e) {
            throw ServiceException.FAILURE("projdb.getProjectResources", e);
        }
    }

    public static String getProjectResources(Connection conn, String projectid, boolean billableOnly) throws ServiceException {
        StringBuilder temp = new StringBuilder();
        ResourceDAO resourceDAO = new ResourceDAOImpl();
        List ll = resourceDAO.getResourcesOnProject(conn, projectid, billableOnly);
        int count = resourceDAO.getTotalCount();
        temp.append(Utilities.listToGridJson(ll, count, ProjectResource.class));
        return temp.toString();
    }

    public static String getAllResourcesInCompany(Connection conn, HttpServletRequest request) throws ServiceException{
        String ret = "";
        PreparedStatement pstmt = null;
        ResultSet rsteam;
        try {
            ResourceDAO r = new ResourceDAOImpl();
            int count = 0;
            JSONObject jobj = new JSONObject();
            String compid = request.getParameter("companyid");
            String teamnames = "";
            ResultSet rs = null;
            pstmt = conn.prepareStatement("select proj_resources.resourceid, proj_resources.resourcename, " +
                    "proj_resources.projid, proj_resources.billable, proj_resources.stdrate, " +
                    "proj_resources.categoryid, proj_resources.typeid, proj_resources.inuseflag, proj_resources.wuvalue from proj_resources " +
                    "inner join project on proj_resources.projid = project.projectid " +
                    "left join userlogin on userlogin.userid=proj_resources.resourceid " +
                    "where project.companyid = ? group by proj_resources.resourceid ORDER BY typeid ASC");
            pstmt.setString(1, compid);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                JSONObject temp = new JSONObject();
                boolean b = true;
                int categoryid = rs.getInt("categoryid");
                int typeid = rs.getInt("typeid");
                if(categoryid == 1)
                    b = Forum.isInUse(conn, rs.getString("resourceid"));
                if (b) {
                    temp.put("resourceid", rs.getString("resourceid"));
                    String projname = getProjectName(conn, rs.getString("projid"));
                    String resname = rs.getString("resourcename");
                    if (rs.getBoolean("inuseflag") == false) {
                        resname = resname.concat("[Inactive on ").concat(projname).concat("]");
                    }
                    temp.put("resourcename", resname);
                    temp.put("projectname", projname);
                    temp.put("categoryid", categoryid);
                    temp.put("typeid", typeid);
                    temp.put("billable", rs.getString("billable"));
                    temp.put("wuvalue", rs.getString("wuvalue"));
                    temp.put("stdrate", rs.getString("stdrate"));
                    temp.put("categoryname", r.getResourcesCategory(conn, Integer.toString(categoryid)).getCategoryName());
                    temp.put("typename", r.getResourcesType(conn, Integer.toString(typeid)).getTypeName());
                    teamnames = "";
                    pstmt = conn.prepareStatement("select team.teamname as teamname, team.teamid from team "
                            + "inner join teammembers on team.teamid = teammembers.teamid where teammembers.resourceid = ?");
                    pstmt.setString(1, rs.getString("resourceid"));
                    rsteam = pstmt.executeQuery();
                    while (rsteam.next()) {
                        teamnames += " " + rsteam.getString("teamname").concat(",");
                    }
                    if (!teamnames.equals("")) {
                        teamnames = teamnames.substring(0, teamnames.length() - 1);
                    }
                    temp.put("teamname", teamnames);
                    jobj.append("data", temp);
                    count++;
                }
            }
            jobj.put("count", count);
            if (count == 0) {
                jobj.put("data", new JSONObject());
            }
            ret = jobj.toString();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getAllResourcesInCompany", e);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("projdb.getAllResourcesInCompany", e);
        } catch (Exception e) {
            throw ServiceException.FAILURE("projdb.getAllResourcesInCompany", e);
        } finally {
            DbPool.closeStatement(pstmt);
            return ret;
        }

    }

    public static String getProjectResources(Connection conn, String projectid) throws ServiceException {
        PreparedStatement pstmt = null;
        try {
            KWLJsonConverter kjs = new KWLJsonConverter();
            ResultSet rs = null;
            pstmt = conn.prepareStatement("SELECT * FROM " +
                "((SELECT (CASE inuseflag WHEN '1' THEN CONCAT(resourcename,'[',proj_resourcecategory.categoryname,']') ELSE CONCAT(resourcename,'[Inactive]')END) AS resourcename, " +
                    "proj_resources.resourceid, stdrate, proj_resources.typeid as type, proj_resourcetype.typename, colorcode, billable, " +
                    "resourcename AS nickname, inuseflag, wuvalue, proj_resourcecategory.categoryname, proj_resourcecategory.categoryid " +
                    "FROM proj_resources INNER JOIN proj_resourcetype ON proj_resourcetype.typeid=proj_resources.typeid " +
                    "INNER JOIN proj_resourcecategory ON proj_resourcecategory.categoryid=proj_resources.categoryid " +
                    "WHERE projid= ? AND proj_resources.resourceid NOT IN (SELECT userid FROM projectmembers WHERE projectid = ?) AND proj_resources.typeid != ? AND proj_resources.inuseflag = 1) " +
                    "UNION " +
                "(SELECT (CASE inuseflag WHEN '1' THEN CONCAT(users.fname,' ',users.lname,'[',proj_resourcecategory.categoryname,']') ELSE CONCAT(users.fname,' ',users.lname,'[Inactive]') END) AS resourcename," +
                    "proj_resources.resourceid, stdrate, proj_resources.typeid as type, proj_resourcetype.typename, colorcode, billable, "+
                    "userlogin.username AS nickname, inuseflag, wuvalue, proj_resourcecategory.categoryname, proj_resourcecategory.categoryid " +
                    "FROM proj_resources INNER JOIN users ON proj_resources.resourceid = users.userid INNER JOIN userlogin on users.userid = userlogin.userid " +
                    "INNER JOIN proj_resourcetype ON proj_resourcetype.typeid=proj_resources.typeid " +
                    "INNER JOIN proj_resourcecategory ON proj_resourcecategory.categoryid=proj_resources.categoryid " +
                    "WHERE projid= ? AND proj_resources.inuseflag = 1)) AS temp ORDER BY inuseflag DESC;");
            pstmt.setString(1, projectid);
            pstmt.setString(2, projectid);
            pstmt.setInt(3, Constants.COST_RESOURCE);
            pstmt.setString(4, projectid);
            rs = pstmt.executeQuery();
            String temp = kjs.GetJsonForGrid(rs);
            return temp;
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getTask", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

    public static String getResourceCategories(Connection conn) throws ServiceException {
        StringBuilder temp = new StringBuilder();
        ResourceDAO rd = new ResourceDAOImpl();
        List ll = rd.getResourcesCategories(conn);
        int count = rd.getTotalCount();
        temp.append(Utilities.listToGridJson(ll, count, Resource.ResourceCategory.class));
        return temp.toString();
    }

    public static String getResourceTypes(Connection conn) throws ServiceException {
        StringBuilder temp = new StringBuilder();
        ResourceDAO rd = new ResourceDAOImpl();
        List ll = rd.getResourcesTypes(conn);
        int count = rd.getTotalCount();
        temp.append(Utilities.listToGridJson(ll, count, Resource.ResourceType.class));
        return temp.toString();
    }

    public static String insertResource(Connection conn, HttpServletRequest request) throws ServiceException {
        PreparedStatement pstmt = null;
        String result = "{\"success\": false}";
        try {
            ResourceDAO rd = new ResourceDAOImpl();
            ResultSet rs = null;
            boolean flag = Boolean.parseBoolean(request.getParameter("chkflag"));
            String resourcename = StringUtil.serverHTMLStripper(request.getParameter("resourcename").toString());
            String projectid = StringUtil.serverHTMLStripper(request.getParameter("projectid").toString());
            if (flag) {
                 flag = rd.isResourceExists(conn, resourcename, ResourceDAO.RESOURCE_FROM_PROJECT, projectid);
            }
            if (!flag) {
                String stdrate = StringUtil.serverHTMLStripper(request.getParameter("stdrate").toString());
                String typeid = StringUtil.serverHTMLStripper(request.getParameter("typeid").toString());
                String categoryid = StringUtil.serverHTMLStripper(request.getParameter("categoryid").toString());
                String wu = StringUtil.serverHTMLStripper(request.getParameter("wuvalue").toString());
                String colorcode = StringUtil.serverHTMLStripper(request.getParameter("colorcode").toString());
                String resID = UUID.randomUUID().toString();
                if (StringUtil.isNullOrEmpty(resourcename) || StringUtil.isNullOrEmpty(projectid) || StringUtil.isNullOrEmpty(typeid) || StringUtil.isNullOrEmpty(categoryid) || StringUtil.isNullOrEmpty(colorcode)) {
                    return result;
                }
                int res = 0;
                res = rd.createProjectResource(conn, resID, resourcename, stdrate,
                        Integer.parseInt(typeid), colorcode, Integer.parseInt(categoryid), Integer.parseInt(wu), projectid);
                if (res == 1) {
                    result = "{\"success\": true,\"resid\":\"" + resID + "\"}";
                    String loginid = AuthHandler.getUserid(request);
                    String userFullName = AuthHandler.getAuthor(conn, loginid);
                    String userName = AuthHandler.getUserName(request);
                    String projName = projdb.getProjectName(conn, projectid);
                    String params = userFullName + " (" + userName + "), " + resourcename + ", " + projName;
                    String companyid = AuthHandler.getCompanyid(request);
                    String ipAddress = AuthHandler.getIPAddress(request);
                    AuditTrail.insertLog(conn, "151", loginid, projectid, projectid, companyid, params, ipAddress, 0);
                }
                //?passed param commit here
//                            conn.commit();
            } else if (!rs.getBoolean("inuseflag")) {
                result = "{\"success\": false,\"error\":\"A resource is already present but is not in use for this project.\",\"resourceid\":\"" + rs.getString("resourceid") + "\",\"errcode\": 1}";
            } else {
                int cnt = 1;
                String rname = resourcename + "(" + Integer.toString(cnt) + ")";
                do {
                    flag = false;
                    pstmt = conn.prepareStatement("SELECT * FROM proj_resources WHERE resourcename = ? AND projid=?");
                    pstmt.setString(1, rname);
                    pstmt.setString(2, projectid);
                    rs = pstmt.executeQuery();
                    if (rs.next()) {
                        cnt++;
                        rname = resourcename + "(" + Integer.toString(cnt) + ")";
                        flag = true;
                    }
                } while (flag);
                result = "{\"success\": false,"
                        + "\"error\":\"A resource named " + resourcename + " is already present.\","
                        + "\"errcode\": 2,"
                        + "\"colorcode\":\"" + request.getParameter("colorcode")
                        + "\",\"resourcename\":\"" + rname
                        + "\",\"categoryid\":\"" + request.getParameter("categoryid")
                        + "\",\"typeid\":\"" + request.getParameter("typeid")
                        + "\",\"stdrate\":\"" + request.getParameter("stdrate") + "\"}";
            }
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("projdb.getResourceType", ex);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getResourceType", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return result;
    }

    public static String editResource(Connection conn, HttpServletRequest request) throws ServiceException {
        String result = KWLErrorMsgs.rsSuccessFalse;
        ResourceDAO rd = new ResourceDAOImpl();
        String resourceid = StringUtil.serverHTMLStripper(request.getParameter("resourceid"));
        String projectid = StringUtil.serverHTMLStripper(request.getParameter("projectid"));
        String stdrate = StringUtil.serverHTMLStripper(request.getParameter("stdrate"));
        String typeid = StringUtil.serverHTMLStripper(request.getParameter("typeid"));
        String categoryid = StringUtil.serverHTMLStripper(request.getParameter("categoryid").toString());
        String wu = StringUtil.serverHTMLStripper(request.getParameter("wuvalue").toString());
        String colorcode = StringUtil.serverHTMLStripper(request.getParameter("colorcode"));
        if (StringUtil.isNullOrEmpty(projectid) || StringUtil.isNullOrEmpty(typeid) || StringUtil.isNullOrEmpty(categoryid) || StringUtil.isNullOrEmpty(colorcode)) {
            return result;
        }
        int res = rd.editProjectResource(conn, resourceid, stdrate,
                    Integer.parseInt(typeid), colorcode, Integer.parseInt(categoryid), Integer.parseInt(wu), projectid);
        if (res > 0) {
            result = KWLErrorMsgs.rsSuccessTrue;
        }
        try {
            String loginid = AuthHandler.getUserid(request);
            String projName = projdb.getProjectName(conn, projectid);
            String userFullName = AuthHandler.getAuthor(conn, loginid);
            String companyid = AuthHandler.getCompanyid(request);
            String userName = AuthHandler.getUserName(request);
            String params = userFullName + "(" + userName + ")," + getResourceName(conn, resourceid) + ","+ projName;
            String ipAddress = AuthHandler.getIPAddress(request);
            AuditTrail.insertLog(conn, "150", loginid, loginid, projectid, companyid, params, ipAddress, 0);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(todolist.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public static String deleteResource(Connection conn, HttpServletRequest request) throws ServiceException {
        PreparedStatement pstmt = null;
        String result = "failure";
        String params = "";
        try {
            String lid = AuthHandler.getUserid(request);
            String companyid = AuthHandler.getCompanyid(request);
            String resId = request.getParameter("resid");
            String[] rid = resId.split(",");
            String projid = request.getParameter("projid");
            String projName = getProjectName(conn, projid);
            String userFName = "";
            String actionId = "";
            for (int i = 0; i < rid.length; i++) {
                if (isResourceMember(conn, rid[i], projid)) {
                    if (!DashboardHandler.isModerator(conn, rid[i], projid)) {
                        dropMemberResource(conn, rid[i], projid, lid);
                        userFName = AuthHandler.getAuthor(conn, rid[i]);
                        String userName = AuthHandler.getUserName(conn, rid[i]);
                        params = userFName + " (" + userName + "), " + projName;
                        actionId = "159";
                    }
                } else {
                    userFName = AuthHandler.getAuthor(conn, lid);
                    String userName = AuthHandler.getUserName(conn, lid);
                    String ResName = getResourceName(conn, rid[i]);
                    params = userFName + " (" + userName + "), " + ResName + ", "+ projName;
                    actionId = "1510";
                    dropResource(conn, rid[i], projid);
                }
                if (params.compareTo("") != 0) {
                    AuditTrail.insertLog(conn, actionId, lid, projid, projid, companyid, params, "", 0);
                }
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } finally {
            result = "{\"data\":[{\"success\": true}]}";
            DbPool.closeStatement(pstmt);
        }
        return result;
    }

    public static String preDeleteResource(Connection conn, HttpServletRequest request) throws ServiceException {
        PreparedStatement pstmt = null;
        JSONObject jbj = new JSONObject();
        try {
            ResourceDAO rd = new ResourceDAOImpl();
            String resId = request.getParameter("resid");
            String[] rid = resId.split(",");
            String projid = request.getParameter("projid");
            for (int i = 0; i < rid.length; i++) {
                JSONObject temp = new JSONObject();
                ProjectResource pr = (ProjectResource) rd.getResource(conn, rid[i], ResourceDAO.RESOURCE_FROM_PROJECT, projid);
                temp.put("name", pr.getResourceName());
                temp.put("id", rid[i]);
                String msg = "";
                    if(Forum.chkResourceDependency(conn, new String[] {rid[i]}, projid)){
                        msg = " " + MessageSourceProxy.getMessage("pm.project.settings.resources.delete.assigned", null, request);
                        temp.put("assigned", true);
                    }
                    if(isResourceMember(conn, rid[i], projid) && DashboardHandler.isModerator(conn, rid[i], projid)){
                        msg = " " + MessageSourceProxy.getMessage("pm.project.settings.resources.delete.assigned", new Object[]{pr.getResourceName()}, request);
                        temp.put("block", true);
                    }
                    if(StringUtil.isNullOrEmpty(msg)){
                        msg = " " + MessageSourceProxy.getMessage("pm.project.settings.resources.delete.removed", null, request);
                    }
                temp.put("msg", msg);
                jbj.append("data", temp);
            }
        } catch (JSONException ex) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return jbj.toString();
    }

    public static void dropResource(Connection conn, String resourceid, String projectid) {
        try {
            String query = "DELETE FROM proj_resources WHERE resourceid = ? AND projid = ?";
            if (Forum.chkResourceDependency(conn, new String[]{resourceid}, projectid)) {
                query = "UPDATE proj_resources SET inuseflag = 0 WHERE resourceid = ? AND projid = ?";
            }
            PreparedStatement p = conn.prepareStatement(query);
            p.setString(1, resourceid);
            p.setString(2, projectid);
            int t = p.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException e) {
        }
    }

    public static void dropMemberResource(Connection conn, String userid, String projectid, String loginid) throws ServiceException {
        try {
            String query1 = "DELETE FROM projectmembers WHERE userid = ? AND projectid = ?";
            String query2 = "DELETE FROM proj_resources WHERE resourceid = ? AND projid = ?";
            if (Forum.chkResourceDependency(conn, new String[]{userid}, projectid)) {
                query1 = "UPDATE projectmembers SET inuseflag=0,status=0 WHERE userid=? AND projectid=?";
                query2 = "UPDATE proj_resources SET inuseflag = 0 WHERE resourceid = ? AND projid = ?";
            }
            PreparedStatement p = conn.prepareStatement(query1);
            p.setString(1, userid);
            p.setString(2, projectid);
            int t = p.executeUpdate();
            p = conn.prepareStatement(query2);
            p.setString(1, userid);
            p.setString(2, projectid);
            t = p.executeUpdate();
            p = conn.prepareStatement("SELECT projectname FROM project WHERE projectid = ?");
            p.setString(1, projectid);
            ResultSet r = p.executeQuery();
            String pName = "";
            if (r.next()) {
                pName = r.getString("projectname");
            }
            String subjectActive = "[" + pName + "] Access deactivated from the project.";
            p = conn.prepareStatement("SELECT username FROM userlogin WHERE userid = ?");
            p.setString(1, userid);
            r = p.executeQuery();
            String resName = "";
            if (r.next()) {
                resName = r.getString("username");
            }
            p = conn.prepareStatement("SELECT companyid FROM users WHERE userid = ?");
            p.setString(1, loginid);
            r = p.executeQuery();
            String cid = "";
            if (r.next()) {
                cid = r.getString("companyid");
            }
            String msgActiveString = "Your access to the project: " + pName + " has been deactivated." + KWLErrorMsgs.mailSystemFooter;
            Mail.insertMailMsg(conn, resName, loginid, subjectActive, msgActiveString, "1", false, "1", "", "newmsg", "", 3, "", cid);

            String msgToModString = "The access account " + resName + " has been deactivated for the project: " + pName + ". <br>You can no longer assign any tasks to " + resName + "." + KWLErrorMsgs.mailSystemFooter;
            String subjectToModString = "[" + pName + "] " + resName + " dropped from the project.";
            String GET_MODERATORS_ID = "select userid as modId from projectmembers where status = 4 and projectid = ?";
            DbResults rsId = DbUtil.executeQuery(conn, GET_MODERATORS_ID, new Object[]{projectid});
            while (rsId.next()) {
                String GET_MODERATORS_NAME = "select username as modName from userlogin where userid = ?";
                DbResults rsName = DbUtil.executeQuery(conn, GET_MODERATORS_NAME, new Object[]{rsId.getString("modId")});
                Mail.insertMailMsg(conn, rsName.getString("modName"), loginid, subjectToModString, msgToModString, "1", false, "1", "", "newmsg", "", 3, "", cid);
            }
        } catch (ParseException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (SQLException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    public static String getResourceName(Connection conn, String userid) {
        String username = "";
        try {
            PreparedStatement p = conn.prepareStatement("SELECT resourcename FROM proj_resources WHERE resourceid = ?");
            p.setString(1, userid);
            ResultSet r = p.executeQuery();
            if (r.next()) {
                username = r.getString("resourcename");
            }
        } catch (SQLException ex) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException e) {
        }
        return username;
    }

    public static boolean isResourceMember(Connection conn, String resourceid, String projectid) {
        boolean f = false;
        try {
            PreparedStatement p = conn.prepareStatement("SELECT COUNT(userid) AS count FROM projectmembers WHERE userid=? AND projectid=?");
            p.setString(1, resourceid);
            p.setString(2, projectid);
            ResultSet r = p.executeQuery();
            if (r.next() && r.getInt("count") == 1) {
                f = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException e) {
        }
        return f;
    }

    public static String setActive(Connection conn, HttpServletRequest request) throws ServiceException {
        PreparedStatement pstmt = null;
        String result = "failure";
        String projectid = request.getParameter("projid");
        String userid = request.getParameter("resid");
        try {
            String loginid = AuthHandler.getUserid(request);
            String[] rid = request.getParameter("resid").split(",");
            String pid = request.getParameter("projid");
            for (int i = 0; i < rid.length; i++) {
                if (isResourceMember(conn, rid[i], pid)) {
                    pstmt = conn.prepareStatement("UPDATE projectmembers SET inuseflag = 1, status = 3 WHERE userid=? and projectid = ?");
                    pstmt.setString(1, rid[i]);
                    pstmt.setString(2, pid);
                    pstmt.executeUpdate();
                }
                pstmt = conn.prepareStatement("UPDATE proj_resources SET inuseflag = 1 WHERE resourceid=? and projid = ?");
                pstmt.setString(1, rid[i]);
                pstmt.setString(2, pid);
                pstmt.executeUpdate();
            }
            result = "success";

            pstmt = conn.prepareStatement(" SELECT projectname FROM project WHERE projectid = ? ");
            pstmt.setString(1, projectid);
            ResultSet r = pstmt.executeQuery();
            String projName = "";
            if (r.next()) {
                projName = r.getString("projectname");
            }

            pstmt = conn.prepareStatement("SELECT username FROM userlogin WHERE userid = ?");
            pstmt.setString(1, userid);
            r = pstmt.executeQuery();
            String resName = "";
            if (r.next()) {
                resName = r.getString("username");

                pstmt = conn.prepareStatement("SELECT companyid FROM users WHERE userid = ?");
                pstmt.setString(1, loginid);
                r = pstmt.executeQuery();
                String cid = "";
                if (r.next()) {
                    cid = r.getString("companyid");
                }

                String msgActiveString = "Your access to the project: " + projName + " has been activated." + KWLErrorMsgs.mailSystemFooter;
                String subjectActive = "[" + projName + "] Access to the project activated.";

                Mail.insertMailMsg(conn, resName, loginid, subjectActive, msgActiveString, "1", false, "1", "", "newmsg", "", 3, "", cid);

                String msgToModString = resName + "'s access to the project: " + projName + " has been activated. <br>" + KWLErrorMsgs.mailSystemFooter;
                String subjectToModString = "[" + projName + "] " + resName + "'s access to project activated.";
                String GET_MODERATORS_ID = "select userid as modId from projectmembers where status = 4 and projectid = ?";
                DbResults rsId = DbUtil.executeQuery(conn, GET_MODERATORS_ID, new Object[]{projectid});
                while (rsId.next()) {
                    String GET_MODERATORS_NAME = " select username as modName from userlogin where userid = ? ";
                    DbResults rsName = DbUtil.executeQuery(conn, GET_MODERATORS_NAME, new Object[]{rsId.getString("modId")});
                    Mail.insertMailMsg(conn, rsName.getString("modName"), loginid, subjectToModString, msgToModString, "1", false, "1", "", "newmsg", "", 3, "", cid);
                }
            }
        } catch (SessionExpiredException ex) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);

        } catch (ParseException ex) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);

        } catch (JSONException ex) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);

        } catch (SQLException e) {
            result = e.getMessage();
            throw ServiceException.FAILURE("projdb.setActive", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return result;
    }

    public static String setBillable(Connection conn, HttpServletRequest request) throws ServiceException {
        String ids [] = request.getParameterValues("uids");
        String projid = request.getParameter("projid");
        boolean billable = Boolean.parseBoolean(request.getParameter("billable"));
        int num = ids.length;
        ResourceDAO rd = new ResourceDAOImpl();
        rd.setBillable(conn, ids, projid, billable);
        String result = "failure";
        if(num == rd.getTotalCount()){
            result= "success";
        }
        return result;
    }

    public static String createResourceCategory(Connection conn, HttpServletRequest request) throws ServiceException {
        String typename = StringUtil.serverHTMLStripper(request.getParameter("categoryname").toString());
        ResourceDAO rd = new ResourceDAOImpl();
        return rd.createResourceCategory(conn, typename);
    }

    public static String InsertTask(Connection conn,
            com.krawler.utils.json.base.JSONObject jobj, String taskid,
            String projId, String rowindex) throws ServiceException {
        PreparedStatement pstmt = null;
        String pDates = "";
        try {
            taskid = StringUtil.serverHTMLStripper(taskid);
            String taskname = StringUtil.serverHTMLStripper(jobj.getString("taskname"));
            if (taskname.length() >= 512) {
                taskname = taskname.substring(0, 511);
            }
            String duration = StringUtil.serverHTMLStripper(jobj.getString("duration"));
            String userid = StringUtil.serverHTMLStripper(jobj.getString("loginid"));
            String enddate = StringUtil.serverHTMLStripper(jobj.getString("enddate"));
            projId = StringUtil.serverHTMLStripper(projId);
            rowindex = StringUtil.serverHTMLStripper(rowindex);
            String level = StringUtil.serverHTMLStripper(jobj.getString("level"));
            String parent = StringUtil.serverHTMLStripper(jobj.getString("parent"));
            String actstartdate = StringUtil.serverHTMLStripper(jobj.getString("actstartdate"));
            String percentcomplete = StringUtil.serverHTMLStripper(jobj.getString("percentcomplete"));
            String notes = StringUtil.serverHTMLStripper(jobj.getString("notes"));
            String priority = StringUtil.serverHTMLStripper(jobj.getString("priority"));
            String links = "";
            if (jobj.has("links")) {
                links = StringUtil.serverHTMLStripper(jobj.getString("links"));
            }
            if (StringUtil.isNullOrEmpty(percentcomplete)) {
                percentcomplete = "0";
            }
            if (StringUtil.isNullOrEmpty(duration) || StringUtil.isNullOrEmpty(enddate) || StringUtil.isNullOrEmpty(projId) || StringUtil.isNullOrEmpty(rowindex)) {
            } else {
                rowindex = String.valueOf(Integer.parseInt(rowindex) + 1);
                pstmt = conn.prepareStatement("update proj_task set taskindex = taskindex+1 where taskindex>=? and projectid=?");
                pstmt.setString(1, rowindex);
                pstmt.setString(2, projId);
                pstmt.executeUpdate();
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                        "yyyy-MM-d HH:mm:ss");
                String str = jobj.getString("startdate");
                str = Timezone.getUserToGmtTimezone(conn, userid, str);
                java.util.Date DateVal = sdf.parse(str);
                Timestamp ts = new Timestamp(DateVal.getTime());

                String sts = sdf.format(ts);
                str = Timezone.getUserToGmtTimezone(conn, userid, enddate);
                DateVal = sdf.parse(str);
                Timestamp endts = new Timestamp(DateVal.getTime());

                String ets = sdf.format(endts);
                str = Timezone.getUserToGmtTimezone(conn, userid, actstartdate);
                DateVal = sdf.parse(str);
                Timestamp actts = new Timestamp(DateVal.getTime());

                String ats = sdf.format(actts);
                pstmt = conn.prepareStatement("INSERT INTO proj_task(taskid, taskname, duration, startdate, enddate, projectid, "
                        + "taskindex, level, parent, actualstartdate, actualduration, percentcomplete, notes, priority, "
                        + "isparent) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                pstmt.setString(1, taskid);
                pstmt.setString(2, taskname);
                pstmt.setString(3, duration);
                pstmt.setTimestamp(4, ts);
                pstmt.setTimestamp(5, endts);
                pstmt.setString(6, projId);
                pstmt.setString(7, rowindex);
                pstmt.setString(8, level);
                pstmt.setString(9, parent);
                pstmt.setTimestamp(10, actts);
                if (jobj.has("actualduration")) {
                    pstmt.setString(11, StringUtil.serverHTMLStripper(jobj.getString("actualduration")));
                } else {
                    pstmt.setString(11, duration);
                }
                pstmt.setString(12, percentcomplete);
                pstmt.setString(13, notes);
                int prt = 1;
                if (priority != null) {
                    prt = Integer.parseInt(priority);
                }
                pstmt.setInt(14, prt);
                pstmt.setBoolean(15, Boolean.parseBoolean(jobj.getString("isparent")));
                pstmt.execute();
                insertLinks(conn, taskid, links);
                pDates = sts.concat(",").concat(ets).concat(",").concat(ats);
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.InsertTask", e);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("projdb.InsertTask", e);
        } catch (ParseException e) {
            throw ServiceException.FAILURE("projdb.InsertTask", e);
        } finally {
            DbPool.closeStatement(pstmt);
            return pDates;
        }
    }

    public static JSONObject InsertTaskFromWidget(Connection conn,
            String taskname, String startdate, String enddate, String taskid,
            String projId) throws ServiceException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject();
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-d");
            java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            int[] nonworkweekArr = getNonWorkWeekDays(conn, projId);
            String holidayArr[] = getCompHolidays(conn, projId, "");
            taskid = StringUtil.serverHTMLStripper(taskid);
            taskname = StringUtil.serverHTMLStripper(taskname);
            String rowindex = "0";
            int rowind = 0;
            if (taskname.length() >= 512) {
                taskname = taskname.substring(0, 511);
            }
            pstmt = conn.prepareStatement("SELECT COUNT(taskindex) as totaltask FROM proj_task where projectid = ?");
            pstmt.setString(1, projId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                rowind = rs.getInt("totaltask");
                rowind += 1;
            }
            java.util.Date SDateVal = sdf.parse(startdate);
            java.util.Date EDateVal = sdf.parse(enddate);
            String startdateString = sdf1.format(SDateVal);
            String enddateString = sdf1.format(EDateVal);
            String acduration = importProjectPlanCSV.getActualDuration_importCSV(SDateVal, EDateVal, nonworkweekArr, holidayArr, "");
            enddate = StringUtil.serverHTMLStripper(enddate);
            projId = StringUtil.serverHTMLStripper(projId);
            String actstartdate = StringUtil.serverHTMLStripper(startdate);
            String duration = "" + (getDaysDiff(startdate, enddate) + 1);
            String percentcomplete = "0";
            if (StringUtil.isNullOrEmpty(duration) || StringUtil.isNullOrEmpty(enddate) || StringUtil.isNullOrEmpty(projId) || StringUtil.isNullOrEmpty(rowindex)) {
            } else {
                rowindex = "" + rowind;
                String str = startdate;
                java.util.Date DateVal = sdf.parse(str);
                Timestamp ts = new Timestamp(DateVal.getTime());
                pstmt = conn.prepareStatement("INSERT INTO proj_task(taskid, taskname, duration, startdate, enddate, projectid, "
                        + "taskindex, level, parent, actualstartdate, actualduration, percentcomplete, notes, priority, "
                        + "isparent) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                pstmt.setString(1, taskid);
                pstmt.setString(2, taskname);
                pstmt.setString(3, acduration);
                pstmt.setTimestamp(4, ts);
                DateVal = sdf.parse(enddate);
                ts = new Timestamp(DateVal.getTime());
                pstmt.setTimestamp(5, ts);
                pstmt.setString(6, projId);
                pstmt.setString(7, rowindex);
                pstmt.setString(8, "0");
                pstmt.setString(9, "0");
                DateVal = sdf.parse(actstartdate);
                ts = new Timestamp(DateVal.getTime());
                pstmt.setTimestamp(10, ts);
                pstmt.setString(11, acduration);
                pstmt.setString(12, percentcomplete);
                pstmt.setString(13, "");
                pstmt.setInt(14, 1);
                pstmt.setBoolean(15, false);
                pstmt.execute();
            }
            jobj.put("percentcomplete", "0");
            jobj.put("taskid", taskid);
            jobj.put("resourcename", "");
            jobj.put("actualduration", acduration);
            jobj.put("links", "");
            jobj.put("isparent", "false");
            jobj.put("predecessor", "");
            jobj.put("duration", duration);
            jobj.put("startdate", startdateString);
            jobj.put("level", 0);
            jobj.put("priority", "1");
            jobj.put("actstartdate", startdateString);
            jobj.put("ismilestone", "false");
            jobj.put("notes", "");
            jobj.put("taskname", taskname);
            jobj.put("enddate", enddateString);
            jobj.put("rowindex", rowind - 1);
            return jobj;
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.InsertTask", e);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("projdb.InsertTask", e);
        } catch (ParseException e) {
            throw ServiceException.FAILURE("projdb.InsertTask", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

    public static void InsertEmptyTask(Connection conn,
            com.krawler.utils.json.base.JSONObject jobj, String taskid,
            String projId, String rowindex) throws ServiceException {
        PreparedStatement pstmt = null;
        try {
            taskid = StringUtil.serverHTMLStripper(taskid);
            projId = StringUtil.serverHTMLStripper(projId);
            rowindex = StringUtil.serverHTMLStripper(rowindex);
            String parent = StringUtil.serverHTMLStripper(jobj.getString("parent").toString());
            if (StringUtil.isNullOrEmpty(taskid) || StringUtil.isNullOrEmpty(projId) || StringUtil.isNullOrEmpty(rowindex) || StringUtil.isNullOrEmpty(parent)) {
            } else {
                rowindex = String.valueOf(Integer.parseInt(rowindex) + 1);
                pstmt = conn.prepareStatement("update proj_task set taskindex = taskindex+1 where taskindex>=? and projectid=?");
                pstmt.setString(1, rowindex);
                pstmt.setString(2, projId);
                pstmt.executeUpdate();
                pstmt = conn.prepareStatement("INSERT INTO proj_task(taskid,projectid,level,parent,taskindex) VALUES (?,?,?,?,?)");
                pstmt.setString(1, taskid);
                pstmt.setString(2, projId);
                pstmt.setInt(3, jobj.getInt("level"));
                pstmt.setString(4, parent);
                pstmt.setString(5, rowindex);
                pstmt.execute();
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.InsertEmptyTask", e);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("projdb.InsertEmptyTask", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

    public static void insertLinks(Connection conn, String taskid, String links) throws ServiceException {
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("delete from task_urlmap where taskid = ?");
            pstmt.setString(1, taskid);
            pstmt.executeUpdate();
            pstmt = conn.prepareStatement("INSERT INTO task_urlmap(taskid, url) values (?,?)");
            pstmt.setString(1, taskid);
            String[] link = links.split(",");
            if (link.length != 0) {
                for (int i = 0; i < link.length; i++) {
                    pstmt.setString(2, link[i]);
                    pstmt.execute();
                }
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.InsertEmptyTask", e);
        }
    }

    public static String insertTaskResource(Connection conn, String jsonstr,
            String taskid, String userid) throws ServiceException {
        String r = "";
        try {
            JSONObject temp = new JSONObject();
            ResultSet rs = null;
            PreparedStatement pstmt = null;
            pstmt = conn.prepareStatement("delete from proj_taskresourcemapping where taskid = ?");
            pstmt.setString(1, taskid);
            pstmt.executeUpdate();
            if (!StringUtil.isNullOrEmpty(jsonstr)) {
                com.krawler.utils.json.base.JSONArray resArray = new com.krawler.utils.json.base.JSONArray(
                        jsonstr);
                for (int i = 0; i < resArray.length(); i++) {
                    int dur = 0;
                    com.krawler.utils.json.base.JSONObject jobj = resArray.getJSONObject(i);
                    String rid = jobj.getString("resourceid");
                    pstmt = conn.prepareStatement("insert into proj_taskresourcemapping (taskid,resourceid,resduration) values(?,?,?)");
                    pstmt.setString(1, taskid);
                    pstmt.setString(2, rid);
                    pstmt.setInt(3, dur);
                    pstmt.execute();
                }
            }
//			pstmt = conn
//					.prepareStatement("select max(lastupdated) from proj_buffer");
            pstmt = conn.prepareStatement("SELECT isparent,taskindex,notes, taskid, taskname, startdate, level, enddate, actualstartdate, duration, percentcomplete, projectid "
                    + "FROM proj_task WHERE taskid = ?");
            pstmt.setString(1, taskid);
            rs = pstmt.executeQuery();
            java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-d");
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-d HH:mm:ss");
            if (rs.next()) {
                temp.put("taskname", rs.getString("taskname"));
                temp.put("taskid", rs.getString("taskid"));
                if (!StringUtil.isNullOrEmpty(rs.getString("actualstartdate"))) {
                    java.util.Date dt = sdf.parse(rs.getString("actualstartdate"));
                    temp.put("actstartdate", sdf.format(dt));
                } else {
                    temp.put("actstartdate", "");
                }
                if (!StringUtil.isNullOrEmpty(rs.getString("enddate"))) {
                    java.util.Date dt = sdf.parse(rs.getString("enddate"));
                    temp.put("enddate", sdf.format(dt));
                } else {
                    temp.put("enddate", "");
                }
                if (!StringUtil.isNullOrEmpty(rs.getString("startdate"))) {
                    java.util.Date dt = sdf.parse(rs.getString("startdate"));
                    temp.put("startdate", sdf.format(dt));
                } else {
                    temp.put("startdate", "");
                }
                temp.put("taskindex", rs.getString("taskindex"));
                temp.put("duration", rs.getString("duration"));
                temp.put("percentcomplete", rs.getString("percentcomplete"));
                temp.put("notes", rs.getString("notes"));
                temp.put("isparent", rs.getString("isparent"));
                temp.put("resources", getTaskResourcesOnDataload(conn, taskid, rs.getString("projectid")));
                temp.put("predecessor", getPredecessor(conn, taskid));
            }
//            r = rs.getTimestamp(1).toString();
            r = temp.toString();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.insertTaskResource", e);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("projdb.insertTaskResource", e);
        } catch (ParseException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return r;
    }

    public static void updateColorCode(Connection conn, String color, String resid,
            String projid) throws ServiceException {
        try {
            PreparedStatement pstmt = null;
            pstmt = conn.prepareStatement("update proj_resources set colorcode = ? where resourceid = ? and projid = ?");
            pstmt.setString(1, color);
            pstmt.setString(2, resid);
            pstmt.setString(3, projid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.updateColorCode", e);
        }
    }

//    public static String getCompareProjectData(Connection conn, String projectid) throws ServiceException{
//        String pdata = "";
//        try{
//            pdata = getTask(conn, projectid, 0, -1);
//        } catch (ServiceException e){
//            throw ServiceException.FAILURE(e.getMessage(), e);
//        }
//        return pdata;
//    }
    public static String getCompanyidOfUser(Connection conn, String userid) throws ServiceException {
        String cid = "";
        DbResults r = DbUtil.executeQuery(conn, "SELECT companyid FROM users WHERE userid = ?", new Object[]{userid});
        if (r.next()) {
            cid = r.getString("companyid");
        }
        return cid;
    }

    public static String getCompareBaselineData(Connection conn, String baselineid, String projectid, String userid) throws ServiceException, ParseException {
        JSONObject jobj = new JSONObject();
        String pdata = "{\"data1\":{}, \"data2\":{}}";
        try {
            int deletetaskind = 0;
            int taskind = 0;
            String sdtStr = "";
            String edtStr = "";
            String sdtStrbs = "";
            String edtStrbs = "";
            String _bl = getBaselineTask(conn, baselineid, 0, -1);
            JSONArray baseline = new JSONObject(_bl).getJSONArray("data");
            String _pD = getTask(conn, projectid, 0, -1);
            JSONArray project = new JSONObject(_pD).getJSONArray("data");
            for (int i = 0; i < baseline.length(); i++) {
                JSONObject temp_bl = baseline.getJSONObject(i);
                if (project.length() > i) {
                    JSONObject temp_proj = project.getJSONObject(i);
                    if (temp_bl.getInt("taskindex") == temp_proj.getInt("taskindex")) {
                        if (temp_bl.getString("taskid").compareTo(temp_proj.getString("taskid")) == 0) {
//                            compare task contents
                            temp_proj = compareTasks(temp_bl, temp_proj);
                            sdtStr = temp_proj.getString("startdate");
                            edtStr = temp_proj.getString("enddate");
                            sdtStrbs = temp_bl.getString("startdate");
                            edtStrbs = temp_bl.getString("enddate");
                            if (!sdtStr.equals("") && !sdtStrbs.equals("")) {
                                project = insertDateInObj(conn, temp_proj, project, sdtStr, edtStr, i, userid);
                            }
                            if (!sdtStrbs.equals("")) {
                                baseline = insertDateInObj(conn, temp_bl, baseline, sdtStrbs, edtStrbs, i, userid);
                            }
                        } else {
                            //                        show task as deleted in project data
                            JSONArray tempproject = new JSONArray();

                            for (int x = i; x < project.length(); x++) {
                                tempproject.put(x, project.getJSONObject(x));
                            }
                            int projlength = project.length();
                            deletetaskind = i + 1;
                            taskind = i;
                            int newindex = temp_proj.getInt("taskindex");
                            for (int j = i; j < projlength; j++, deletetaskind++) {
                                project.put(j + 1, tempproject.getJSONObject(j));
                                int jindex = project.getJSONObject(j + 1).getInt("taskindex");
                                project.getJSONObject(j + 1).remove("taskindex");
                                project.getJSONObject(j + 1).put("taskindex", jindex + 1);
                            }
                            JSONObject emptytask = makeDeletedTasks(newindex);
                            project.put(taskind, emptytask);
                            sdtStrbs = temp_bl.getString("startdate");
                            edtStrbs = temp_bl.getString("enddate");
                            if (!sdtStrbs.equals("")) {
                                temp_bl.put("diff", "remove");
                                baseline = insertDateInObj(conn, temp_bl, baseline, sdtStrbs, edtStrbs, i, userid);
                            }
                        }
                    } else {
                        //show deleted task
                        if (temp_proj.getInt("taskindex") < temp_bl.getInt("taskindex")) {
                            deletetaskind = temp_proj.getInt("taskindex");
                            JSONObject emptytask = makeDeletedTasks(deletetaskind);
                            project.getJSONObject(deletetaskind).put("diff", "remove");
                            baseline.put(deletetaskind, emptytask);
                        } else {
                            deletetaskind = temp_bl.getInt("taskindex");
                            JSONObject emptytask = makeDeletedTasks(deletetaskind);
                            baseline.getJSONObject(deletetaskind).put("diff", "remove");
                            project.put(deletetaskind, emptytask);
                        }
                    }
                } else {
//                    show task as deleted in plan
                    for (int m = project.length(); m < baseline.length(); m++) {
                        //                    show task as newly added task in project data
                        JSONObject temp_pro = baseline.getJSONObject(m);
                        sdtStr = temp_pro.getString("startdate");
                        edtStr = temp_pro.getString("enddate");
                        if (!sdtStr.equals("") && !sdtStrbs.equals("")) {
                            temp_pro.put("diff", "remove");
                            baseline = insertDateInObj(conn, temp_pro, baseline, sdtStr, edtStr, m, userid);
                        }
                        JSONObject emptytask = makeDeletedTasks(deletetaskind);
                        project.put(m, emptytask);
                    }
                    break;
                }
            }
            if (project.length() > baseline.length()) {
                for (int i = baseline.length(); i < project.length(); i++) {
//                    show task as newly added task in project data
                    JSONObject temp_project = project.getJSONObject(i);
                    sdtStr = temp_project.getString("startdate");
                    edtStr = temp_project.getString("enddate");
                    if (!sdtStr.equals("") && !sdtStrbs.equals("")) {
                        temp_project.put("diff", "add");
                        project = insertDateInObj(conn, temp_project, project, sdtStr, edtStr, i, userid);
                    }
                    JSONObject emptytask = makeDeletedTasks(deletetaskind);
                    baseline.put(i, emptytask);
                }
            }
            jobj.put("success", true);
            jobj.put("data1", baseline);
            jobj.put("data2", project);
            pdata = jobj.toString();

        } catch (ServiceException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } catch (ParseException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return pdata;
    }

    public static JSONArray insertDateInObj(Connection conn, JSONObject task, JSONArray taskarray,
            String startdate, String enddate, int pos, String userid) throws ParseException, JSONException, ServiceException {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-d HH:mm:ss");
        java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-d");
        java.util.Date dt = new Date();
        try {
            dt = sdf.parse(startdate);
            startdate = sdf1.format(dt);
            dt = sdf.parse(enddate);
            enddate = sdf1.format(dt);
            task.remove("startdate");
            task.put("startdate", startdate);
            task.remove("enddate");
            task.put("enddate", enddate);
            taskarray.put(pos, task);
            return taskarray;
        } catch (ParseException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }

    public static JSONObject compareTasks(JSONObject baseline_task, JSONObject proj_task) throws ServiceException {
        try {
            if (baseline_task.getString("taskname").compareTo(proj_task.getString("taskname")) != 0 || baseline_task.getString("duration").compareTo(proj_task.getString("duration")) != 0
                    || baseline_task.getString("startdate").compareTo(proj_task.getString("startdate")) != 0 || baseline_task.getString("enddate").compareTo(proj_task.getString("enddate")) != 0
                    || baseline_task.getString("percentcomplete").compareTo(proj_task.getString("percentcomplete")) != 0) {
                proj_task.put("diff", "update");
            }
            return proj_task;
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }

    public static JSONObject makeDeletedTasks(int index) throws ServiceException {
        try {
            JSONObject tempobj = new JSONObject();
            tempobj.put("taskindex", "");
            tempobj.put("taskid", "");
            tempobj.put("taskname", "");
            tempobj.put("parent", "");
            tempobj.put("duration", "");
            tempobj.put("startdate", "");
            tempobj.put("enddate", "");
            tempobj.put("percentcomplete", "");
            tempobj.put("notes", "");
            tempobj.put("priority", "");
            tempobj.put("workholidays", "");
            tempobj.put("actualduration", "");
            tempobj.put("isparent", "");
            tempobj.put("actualstartdate", "");
            tempobj.put("level", "");
            return tempobj;
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }

    public static String insertTemplate(Connection conn, String templateid,
            String jstr, String name, String uid, String projid, String desc, String userid)
            throws ServiceException {
        PreparedStatement pstmt = null;
        try {
            templateid = StringUtil.serverHTMLStripper(templateid);
            name = StringUtil.serverHTMLStripper(name);
            jstr = StringUtil.serverHTMLStripper(jstr);
            uid = StringUtil.serverHTMLStripper(uid);
            projid = StringUtil.serverHTMLStripper(projid);
            desc = StringUtil.serverHTMLStripper(desc);

            if (StringUtil.isNullOrEmpty(templateid) || StringUtil.isNullOrEmpty(name) || StringUtil.isNullOrEmpty(jstr) || StringUtil.isNullOrEmpty(uid) || StringUtil.isNullOrEmpty(projid)) {
                return "";
            }
            jstr = "{\"data\":[" + jstr.substring(1, (jstr.length() - 1)) + "]}";
            //JSONArray json = new JSONArray(jstr);
            JSONObject json = new JSONObject(jstr);
            for (int i = 0; i < json.getJSONArray("data").length(); i++) {
                String dtStr = json.getJSONArray("data").getJSONObject(i).getString("startdate");
                dtStr = Timezone.getUserToGmtTimezone(conn, userid, dtStr);
                json.getJSONArray("data").getJSONObject(i).remove("startdate");
                json.getJSONArray("data").getJSONObject(i).put("startdate", dtStr);
                dtStr = json.getJSONArray("data").getJSONObject(i).getString("enddate");
                dtStr = Timezone.getUserToGmtTimezone(conn, userid, dtStr);
                json.getJSONArray("data").getJSONObject(i).remove("enddate");
                json.getJSONArray("data").getJSONObject(i).put("enddate", dtStr);
                dtStr = json.getJSONArray("data").getJSONObject(i).getString("actstartdate");
                dtStr = Timezone.getUserToGmtTimezone(conn, userid, dtStr);
                json.getJSONArray("data").getJSONObject(i).remove("actstartdate");
                json.getJSONArray("data").getJSONObject(i).put("actstartdate", dtStr);
            }
            jstr = json.toString();
            jstr = jstr.substring(8, jstr.length() - 1);

            pstmt = conn.prepareStatement("insert into proj_template (tempid,tempname,jsonstr,userid,projid,description) values(?,?,?,?,?,?)");
            pstmt.setString(1, templateid);
            pstmt.setString(2, name);
            pstmt.setString(3, jstr);
            pstmt.setString(4, uid);
            pstmt.setString(5, projid);
            pstmt.setString(6, desc);
            pstmt.execute();
            return templateid;
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.insertTemplate", e);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("projdb.insertTemplate", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

    public static String insertReportTemplate(Connection conn, String templateid,
            String jstr, String name, String uid, String desc)
            throws ServiceException {
        try {
            templateid = StringUtil.serverHTMLStripper(templateid);
            name = StringUtil.serverHTMLStripper(name);
            jstr = StringUtil.serverHTMLStripper(jstr);
            uid = StringUtil.serverHTMLStripper(uid);
            desc = StringUtil.serverHTMLStripper(desc);

            if (StringUtil.isNullOrEmpty(templateid) || StringUtil.isNullOrEmpty(name) || StringUtil.isNullOrEmpty(jstr) || StringUtil.isNullOrEmpty(uid)) {
                return "";
            }
            PreparedStatement pstmt = null;
            pstmt = conn.prepareStatement("insert into projreport_template (tempid,tempname,configstr,userid,description) values(?,?,?,?,?)");
            pstmt.setString(1, templateid);
            pstmt.setString(2, name);
            pstmt.setString(3, jstr);
            pstmt.setString(4, uid);
            pstmt.setString(5, desc);
            pstmt.execute();
            return templateid;
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.insertReportTemplate", e);
        }
    }

    public static String getAllTemplates(Connection conn, String projectid, String companyid)
            throws ServiceException {
        PreparedStatement pstmt = null;
        try {
            KWLJsonConverter kjs = new KWLJsonConverter();
            ResultSet rs = null;
            pstmt = conn.prepareStatement("select tempid, tempname, description from proj_template where projid in (select projectid from project where companyid = ?)");
            pstmt.setString(1, companyid);
//			 pstmt.setString(1, projectid);
//			 pstmt.setString(2, userid);
            rs = pstmt.executeQuery();
            String json = null;
            json = kjs.GetJsonForGrid(rs);
            return json;
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getAllTemplates", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

    public static String getTemplatesDetail(Connection conn, String tempid, String userid)
            throws ServiceException, JSONException {
        PreparedStatement pstmt = null;
        try {
            KWLJsonConverter kjs = new KWLJsonConverter();
            ResultSet rs = null;
            pstmt = conn.prepareStatement("select * from proj_template where tempid  = ?");
            pstmt.setString(1, tempid);
            rs = pstmt.executeQuery();
            String json = null;
            json = kjs.GetJsonForGrid(rs);
            return json;
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getTemplatesDetail", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

    public static String getAllReportTemplates(Connection conn)
            throws ServiceException {
        PreparedStatement pstmt = null;
        try {
            KWLJsonConverter kjs = new KWLJsonConverter();
            ResultSet rs = null;
            pstmt = conn.prepareStatement("select tempid,tempname,description,configstr from projreport_template");
//                        pstmt.setString(1, companyid);
//			 pstmt.setString(1, projectid);
//			 pstmt.setString(2, userid);
            rs = pstmt.executeQuery();
            String json = null;
            json = kjs.GetJsonForGrid(rs);
            return json;
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getAllReportTemplates", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

    public static String getTemplate(Connection conn, String tempid, String userid)
            throws ServiceException {
        PreparedStatement pstmt = null;
        try {
            // KWLJsonConverter kjs = new KWLJsonConverter();
            ResultSet rs = null;

            pstmt = conn.prepareStatement("select jsonstr from proj_template where tempid=?");
            pstmt.setString(1, tempid);
            rs = pstmt.executeQuery();
            rs.next();
            String jstr = rs.getString(1);
            jstr = "{\"data\":[" + jstr.substring(1, (jstr.length() - 1)) + "]}";
            JSONObject json = new JSONObject(jstr);
            for (int i = 0; i < json.getJSONArray("data").length(); i++) {
                if (json.getJSONArray("data").getJSONObject(i).has("startdate")) {
                    String dtStr = json.getJSONArray("data").getJSONObject(i).getString("startdate");
                    json.getJSONArray("data").getJSONObject(i).remove("startdate");
                    json.getJSONArray("data").getJSONObject(i).put("startdate", dtStr.split(" ")[0]);
                    dtStr = json.getJSONArray("data").getJSONObject(i).getString("enddate");
                    json.getJSONArray("data").getJSONObject(i).remove("enddate");
                    json.getJSONArray("data").getJSONObject(i).put("enddate", dtStr.split(" ")[0]);
                    dtStr = json.getJSONArray("data").getJSONObject(i).getString("actstartdate");
                    json.getJSONArray("data").getJSONObject(i).remove("actstartdate");
                    json.getJSONArray("data").getJSONObject(i).put("actstartdate", dtStr.split(" ")[0]);
                }
            }
            jstr = json.toString();
            jstr = jstr.substring(8, jstr.length() - 1);
            return jstr;
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getTemplate", e);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("projdb.getTemplate", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

    public static String deleteTemplate(Connection conn, String tempid)
            throws ServiceException, JSONException {
        PreparedStatement pstmt = null;
        com.krawler.utils.json.base.JSONObject jtemp = new com.krawler.utils.json.base.JSONObject();
        com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject();
        String[] taskidArr = tempid.split(",");
        String query = "delete from proj_template where tempid = ?";
        try {
            for (int i = 0; i < taskidArr.length; i++) {
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, taskidArr[i]);
                pstmt.executeUpdate();
                pstmt.close();
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("todolist.deleteToDoTask", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        jtemp.put("remoteid", tempid);
        jobj.append("data", jtemp);
        return jobj.toString();
    }

    public static String UpdateTask(Connection conn,
            com.krawler.utils.json.base.JSONObject jobj, String taskid, String userid)
            throws ServiceException, ParseException {
        PreparedStatement pstmt = null;
        String ret = "";
        try {
            taskid = StringUtil.serverHTMLStripper(taskid);
            String taskname = StringUtil.serverHTMLStripper(jobj.getString("taskname"));
            String duration = StringUtil.serverHTMLStripper(jobj.getString("duration"));
            String startdate = StringUtil.serverHTMLStripper(jobj.getString("startdate"));
            startdate = Timezone.getUserToGmtTimezone(conn, userid, startdate);
            String enddate = StringUtil.serverHTMLStripper(jobj.getString("enddate"));
            enddate = Timezone.getUserToGmtTimezone(conn, userid, enddate);
            String level = StringUtil.serverHTMLStripper(jobj.getString("level"));
            String parent = StringUtil.serverHTMLStripper(jobj.getString("parent"));
            String actualduration = StringUtil.serverHTMLStripper(jobj.getString("actualduration"));
            String actstartdate = StringUtil.serverHTMLStripper(jobj.getString("actstartdate"));
            actstartdate = Timezone.getUserToGmtTimezone(conn, userid, actstartdate);
            String notes = StringUtil.serverHTMLStripper(jobj.getString("notes"));
            String priority = StringUtil.serverHTMLStripper(jobj.getString("priority"));
            String links = jobj.getString("links");

            if (StringUtil.isNullOrEmpty(taskid) || StringUtil.isNullOrEmpty(duration) || StringUtil.isNullOrEmpty(startdate) || StringUtil.isNullOrEmpty(enddate)) {
            } else {

                notes = notes.replace("!NL?", "\n");
                pstmt = conn.prepareStatement("update proj_task set taskname = ? , duration = ?, startdate = ?, enddate = ?, "
                        + "level = ?, parent = ?, percentcomplete = ?, actualstartdate = ?, actualduration = ?, "
                        + "priority = ?, notes = ?, isparent = ? where taskid = ?");
                pstmt.setString(1, taskname);
                pstmt.setString(2, duration);
                pstmt.setString(3, startdate);
                pstmt.setString(4, enddate);
                pstmt.setString(5, level);
                pstmt.setString(6, parent);
                pstmt.setInt(7, jobj.getInt("percentcomplete"));
                pstmt.setString(8, actstartdate);
                pstmt.setString(9, actualduration);
                pstmt.setString(10, priority);
                pstmt.setString(11, notes);
                pstmt.setBoolean(12, Boolean.parseBoolean(jobj.getString("isparent")));
                pstmt.setString(13, taskid);
                pstmt.execute();
                insertLinks(conn, taskid, links);
                ret = startdate.concat(",").concat(enddate).concat(",").concat(actstartdate);
                checkPredecessorNotification(conn, userid, taskid, jobj.getInt("percentcomplete"));
                CheckListManager cm = new CheckListManager();
                CheckList cl = cm.getAssociatedCheckList(conn, taskid);
                String Cl = jobj.getString("checklist");
                Cl = Cl.compareTo("-") == 0 ? "" : Cl;
                if(cl != null){
                    if(StringUtil.isNullOrEmpty(Cl)){
                        cm.removeAssociatedCheckListFromTask(conn, taskid, cl.getCheckListID());
                    } else if (!cl.getCheckListID().equals(Cl)){
                        cm.removeAssociatedCheckListFromTask(conn, taskid, cl.getCheckListID());
                        cm.attachCheckListWithTask(conn, taskid, Cl, userid);
                    }
                } else {
                    if(!StringUtil.isNullOrEmpty(Cl)){
                        cm.attachCheckListWithTask(conn, taskid, Cl, userid);
                    }
                }
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.UpdateTask", e);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("projdb.UpdateTask", e);
        } finally {
            DbPool.closeStatement(pstmt);
            return ret;
        }
    }

    public static void UpdateParentStatus(Connection conn, String taskid, boolean flag)
            throws ServiceException {
        PreparedStatement pstmt = null;
        boolean more = false;
        try {
            if (flag == false) {
                pstmt = conn.prepareStatement("select taskid, parent from proj_task where parent in ( select parent "
                        + "from proj_task where taskid = ? and parent <> '0') and taskid <> ?");
                pstmt.setString(1, taskid);
                pstmt.setString(2, taskid);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    more = true;
                } else {
                    taskid = DbUtil.executeQuery(conn, "select parent from proj_task where taskid = ?",
                            taskid).getString("parent");
                }
            }
            if (!more) {
                pstmt = conn.prepareStatement("update proj_task set isparent = ? where taskid = ?");
                pstmt.setBoolean(1, flag);
                pstmt.setString(2, taskid);
                pstmt.execute();
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.UpdateParentStatus", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

//	public static String insertBuffer(Connection conn, String jsonstr,
//			String projectid, String taskid, String action, int rowindex,
//			String userId) throws ServiceException {
//		PreparedStatement pstmt = null;
//		String timestamp = "";
//		try {
//			pstmt = conn
//					.prepareStatement("insert into proj_buffer (projectid,taskid,action,json,rowindex,userid) values(?,?,?,?,?,?)");
//			pstmt.setString(1, projectid);
//			pstmt.setString(2, taskid);
//			pstmt.setString(3, action);
//			pstmt.setString(4, jsonstr);
//			pstmt.setInt(5, rowindex);
//			pstmt.setString(6, userId);
//			pstmt.executeUpdate();
//			pstmt = conn
//					.prepareStatement("select max(lastupdated) from proj_buffer");
//			ResultSet rs = pstmt.executeQuery();
//			rs.next();
//			timestamp = rs.getTimestamp(1).toString();
//		} catch (SQLException e) {
//			throw ServiceException.FAILURE("projdb.insertBuffer", e);
//		} catch (Exception e) {
//			throw ServiceException.FAILURE("projdb.insertBuffer", e);
//		} finally {
//			DbPool.closeStatement(pstmt);
//		}
//		return timestamp;
//	}
    public static String getPredecessor(Connection conn, String taskId)
            throws ServiceException {
        String predecessor = "";
        PreparedStatement pstmt = null;
        try {
            ResultSet rs = null;

            // pstmt = conn
            // .prepareStatement("select taskindex from proj_task where taskid
            // in ( select fromtask from proj_tasklinks where totask = ? )");
            pstmt = conn.prepareStatement("select fromtask from proj_tasklinks where totask = ?");
            pstmt.setString(1, taskId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String Fromtaskid = rs.getString(1);
                boolean flag = true;
                String[] stringArr = predecessor.split(",");
                for (int i = 0; i < stringArr.length; i++) {
                    if (Fromtaskid.equals(stringArr[i])) {
                        flag = false;
                    }
                }
                if (flag) {
                    predecessor = predecessor.concat(Fromtaskid).concat(",");
                }
            }
            if (predecessor.length() != 0) {
                predecessor = predecessor.substring(0, predecessor.length() - 1);
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getPredecessor", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return predecessor;
    }

    public static String getLinks(Connection conn, String taskId)
            throws ServiceException {
        String links = "";
        PreparedStatement pstmt = null;
        try {
            ResultSet rs = null;
            pstmt = conn.prepareStatement("select url from task_urlmap where taskid = ?");
            pstmt.setString(1, taskId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String url = rs.getString("url");
                boolean flag = true;
                String[] stringArr = links.split(",");
                for (int i = 0; i < stringArr.length; i++) {
                    if (url.equals(stringArr[i])) {
                        flag = false;
                    }
                }
                if (flag) {
                    links = links.concat(url).concat(",");
                }
            }
            if (links.length() != 0) {
                links = links.substring(0, links.length() - 1);
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getLinks", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return links;
    }

    public static String getPredecessorIndex(Connection conn, String taskId)
            throws ServiceException {
        String predecessor = "";
        PreparedStatement pstmt = null;
        try {
            ResultSet rs = null;
            pstmt = conn.prepareStatement("select taskindex as taskindex from proj_tasklinks inner join proj_task "
                    + "on proj_task.taskid = proj_tasklinks.fromtask where totask = ?");
            pstmt.setString(1, taskId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String Fromtaskid = rs.getString(1);
                boolean flag = true;
                String[] stringArr = predecessor.split(",");
                for (int i = 0; i < stringArr.length; i++) {
                    if (Fromtaskid.equals(stringArr[i])) {
                        flag = false;
                    }
                }
                if (flag) {
                    predecessor = predecessor.concat(Fromtaskid).concat(",");
                }
            }
            if (predecessor.length() != 0) {
                predecessor = predecessor.substring(0, predecessor.length() - 1);
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getPredecessorIndex", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return predecessor;
    }

    private static String getDecimalsInDuration(String duration, String durType, String decimalPart) {
        if (!decimalPart.equals("0")) {
            duration = duration.substring(0, duration.indexOf(durType));
            duration = duration.substring(0, duration.indexOf("."));
            duration = duration.concat(decimalPart);
        }
        return duration;
    }

    private static String stripDecimals(String decimal) {
        if (decimal.indexOf("d") != -1) {
            decimal = decimal.substring(0, decimal.indexOf("d"));
        } else if (decimal.indexOf("h") != -1) {
            decimal = decimal.substring(0, decimal.indexOf("h"));
        }
        return decimal;
    }

    public static double[] getTaskCostWork(Connection conn, String taskId, String projid, String companyid, int percent)
            throws ServiceException {
        double[] expense = new double[2];
        PreparedStatement pstmt = null;
        try {
            String duration = "0";
            double cost = 0d, work = 0d, materialCost = 0d;
            int[] nonworkweekArr = getNonWorkWeekDays(conn, projid);
            String holidayArr[] = getCompHolidays(conn, projid, "");
            ResourceDAO rd = new ResourceDAOImpl();

            PreparedStatement pstmt1 = conn.prepareStatement("select duration, startdate, enddate from proj_task "
                    + "where taskid = ? and startdate is not null and projectid = ?");
            pstmt1.setString(1, taskId);
            pstmt1.setString(2, projid);
            ResultSet rs1 = pstmt1.executeQuery();
            if(rs1.next()){
                duration = rs1.getString("duration");
                if (!duration.contains("h")) {
                    java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    java.util.Date stdate = sdf1.parse(rs1.getString("startdate"));
                    java.util.Date enddate = sdf1.parse(rs1.getString("enddate"));
                    java.util.Date curdate = new java.util.Date();
                    if (curdate.before(stdate)) {
                        duration = "0";
                    } else if (curdate.before(enddate)) {
                        if (stdate.equals(enddate)) {
                            duration = rs1.getString("duration");
                        } else {
                            duration = projectReport.calculateWorkingDays(stdate, curdate, nonworkweekArr, holidayArr) + "";
                        }
                    }
                }
            }
            double dur = getDuration(duration);
            if(dur != 0){
                List ll = rd.getResourcesOnTask(conn, projid, taskId, true);
                Iterator i = ll.listIterator();
                while(i.hasNext()){
                    double tempW = 0d, tempC = 0d;
                    ProjectResource pr = (ProjectResource) i.next();
                    if (pr.getType().getTypeID() == Constants.WORK_RESOURCE){
                        tempW = (dur * Constants.WORK_HOURS) * (pr.getWuvalue() / 100);
                        tempC = pr.getStdRate() * tempW;
                    } else if (pr.getType().getTypeID() == Constants.MATERIAL_RESOURCE){
                        tempW = 0;
                        tempC = pr.getStdRate() * pr.getWuvalue();
                    }
                    work += tempW;
                    cost += tempC;
                }
            }
            expense[0] = cost + materialCost;
            expense[1] = work;
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getTaskCostWork", e);
        } catch (Exception e) {
            throw ServiceException.FAILURE("projdb.getTaskCostWork", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return expense;
    }

    public static double[] getDateWiseTaskCostWork(Connection conn, String taskId, String projid,
            java.util.Date sdate, java.util.Date edate, String userid, boolean actual)
            throws ServiceException {
        double[] expense = new double[2];
        PreparedStatement pstmt = null;
        try {
            String duration = "0", decimal = ".0";
            double cost = 0d, work = 0d, materialCost = 0d;
            boolean TaskEnds = false;
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            int nonworkweekArr[] = getNonWorkWeekDays(conn, projid);
            String holidayArr[] = getCompHolidays(conn, projid, "");
            ResourceDAO rd = new ResourceDAOImpl();

            PreparedStatement pstmt1 = conn.prepareStatement("select duration, startdate, enddate from proj_task "
                        + "where taskid = ? and projectid = ? and startdate is not null");
            pstmt1.setString(1, taskId);
            pstmt1.setString(2, projid);
            ResultSet rs1 = pstmt1.executeQuery();
            if(rs1.next()){
                duration = rs1.getString("duration");
                decimal = (duration.contains(".")) ? duration.substring(duration.indexOf(".")) : ".0";
                if (!duration.contains("h") && !duration.equals("0")) {
                    java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    java.util.Date stdate = sdf1.parse(rs1.getString("startdate"));
                    java.util.Date enddate = sdf1.parse(rs1.getString("enddate"));
                    sdate = sdf1.parse(sdf1.format(sdate));
                    edate = sdf1.parse(sdf1.format(edate));
                    if (stdate.before(sdate) || stdate.equals(sdate)) {
                        if (enddate.after(edate) || enddate.equals(edate)) {
                            duration = projectReport.calculateWorkingDays(sdate, edate, nonworkweekArr, holidayArr) + "";
                        } else {
                            duration = projectReport.calculateWorkingDays(sdate, enddate, nonworkweekArr, holidayArr) + "";
                            TaskEnds = true;
                        }
                    } else {
                        if (enddate.after(edate) || enddate.equals(edate)) {
                            duration = projectReport.calculateWorkingDays(stdate, edate, nonworkweekArr, holidayArr) + "";
                        } else {
                            duration = projectReport.calculateWorkingDays(stdate, enddate, nonworkweekArr, holidayArr) + "";
                            TaskEnds = true;
                        }
                    }
                    if (!actual || stdate.before(sdf1.parse(sdf.format(new Date())))) {
                        if (!actual) {
                            int dec = Integer.parseInt(decimal.indexOf("d") != -1
                                    ? decimal.substring(decimal.indexOf(".") + 1, decimal.indexOf("d")) : decimal.substring(decimal.indexOf(".") + 1));
                            if (dec != 0) {
                                duration = String.valueOf(Integer.parseInt(duration) - 1);
                                duration = duration.concat(decimal);
                            }
                        }
                    } else {
                        duration = "0";
                    }
                }
            }

            double dur = 0;
            if (duration.contains("h")) {
                duration = getDecimalsInDuration(duration, "h", decimal);
                dur = Double.parseDouble(duration.substring(0, duration.indexOf("h")));
                dur = dur / 8;
            } else if (duration.contains("d")) {
                dur = Double.parseDouble(duration.substring(0, duration.indexOf("d")));
            } else {
                if (TaskEnds && !duration.contains(".")) {
                    decimal = stripDecimals(decimal);
                    duration = duration.concat(decimal);
                }
                dur = Double.parseDouble(duration);
            }

            if(dur != 0){
                List ll = rd.getResourcesOnTask(conn, projid, taskId, true);
                Iterator i = ll.listIterator();
                while(i.hasNext()){
                    double tempW = 0d, tempC = 0d;
                    ProjectResource pr = (ProjectResource) i.next();
                    if (pr.getType().getTypeID() == Constants.WORK_RESOURCE){
                        tempW = (dur * Constants.WORK_HOURS) * (pr.getWuvalue() / 100);
                        tempC = pr.getStdRate() * tempW;
                    } else if (pr.getType().getTypeID() == Constants.MATERIAL_RESOURCE){
                        tempW = 0;
                        tempC = pr.getStdRate() * pr.getWuvalue();
                    }
                    work += tempW;
                    cost += tempC;
                }
            }
            expense[0] = cost + materialCost;
            expense[1] = work;
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getDateWiseTaskCostWork", e);
        } catch (Exception e) {
            throw ServiceException.FAILURE("projdb.getDateWiseTaskCostWork", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return expense;
    }

    public static double[] getResourceWiseTaskCostWork(Connection conn, String taskId, String projid, String resourceid,
            java.util.Date sdate, java.util.Date edate, String userid, String companyid) throws ServiceException {
        double[] expense = new double[2];
        PreparedStatement pstmt = null;
        try {
            String duration = "0", decimal = ".0";
            double cost = 0d, work = 0d, materialCost = 0d;
            boolean TaskEnds = false;
            int nonworkweekArr[] = getNonWorkWeekDays(conn, projid);
            String holidayArr[] = getCompHolidays(conn, projid, "");
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdate = sdf.parse(sdf.format(sdate));
            edate = sdf.parse(sdf.format(edate));

            ResourceDAO rd = new ResourceDAOImpl();
            Resource r = rd.getResource(conn, resourceid, ResourceDAO.RESOURCE_FROM_PROJECT, projid);

            PreparedStatement pstmt1 = conn.prepareStatement("select duration, startdate, enddate from proj_task "
                    + "where taskid = ? and projectid = ?");
            pstmt1.setString(1, taskId);
            pstmt1.setString(2, projid);
            ResultSet rs1 = pstmt1.executeQuery();
            if(rs1.next()){
                duration = rs1.getString("duration");
                decimal = (duration.contains(".")) ? duration.substring(duration.indexOf(".")) : ".0";
                if (!duration.contains("h") && !duration.equals("0")) {
                    java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-d");
                    java.util.Date stdate = sdf1.parse(rs1.getString("startdate"));
                    java.util.Date enddate = sdf1.parse(rs1.getString("enddate"));
                    sdate = sdf1.parse(sdf1.format(sdate));
                    edate = sdf1.parse(sdf1.format(edate));
                    if (stdate.before(sdate) || stdate.equals(sdate)) {
                        if (enddate.after(edate) || enddate.equals(edate)) {
                            duration = projectReport.calculateWorkingDays(sdate, edate, nonworkweekArr, holidayArr) + "";
                        } else {
                            duration = projectReport.calculateWorkingDays(sdate, enddate, nonworkweekArr, holidayArr) + "";
                            TaskEnds = true;
                        }
                    } else {
                        if (enddate.after(edate) || enddate.equals(edate)) {
                            duration = projectReport.calculateWorkingDays(stdate, edate, nonworkweekArr, holidayArr) + "";
                        } else {
                            duration = projectReport.calculateWorkingDays(stdate, enddate, nonworkweekArr, holidayArr) + "";
                            TaskEnds = true;
                        }
                    }
                    int dec = Integer.parseInt(decimal.indexOf("d") != -1
                            ? decimal.substring(decimal.indexOf(".") + 1, decimal.indexOf("d")) : decimal.substring(decimal.indexOf(".") + 1));
                    if (dec != 0) {
                        duration = String.valueOf(Integer.parseInt(duration) - 1);
                        duration = duration.concat(decimal);
                    }
                }
            }
            double dur = 0;
            if (duration.contains("h")) {
                duration = getDecimalsInDuration(duration, "h", decimal);
                dur = Double.parseDouble(duration.substring(0, duration.indexOf("h")));
                dur = dur / 8;
            } else if (duration.contains("d")) {
                dur = Double.parseDouble(duration.substring(0, duration.indexOf("d")));
            } else {
                if (TaskEnds && !duration.contains(".")) {
                    decimal = stripDecimals(decimal);
                    duration = duration.concat(decimal);
                }
                dur = Double.parseDouble(duration);
            }

            if(r != null){
                if(r.isInUseFlag() && r.isBillable() && dur != 0) {
                    if(r.getType().getTypeID() == 1){
                        work = (r.getWuvalue()/100) * (Constants.WORK_HOURS * dur);
                        cost = work * r.getStdRate();
                    } else if(r.getType().getTypeID() == 2){
                        materialCost = r.getStdRate() * r.getWuvalue();
                    }
                }
            }
            expense[0] = cost + materialCost;
            expense[1] = work;
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getResourceWiseTaskCostWork", e);
        } catch (Exception e) {
            throw ServiceException.FAILURE("projdb.getResourceWiseTaskCostWork", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return expense;
    }

    public static double[] getTaskCostWorkForBaseline(Connection conn, String taskId, String duration, String baselineid,
            String projid, java.util.Date sdate, java.util.Date edate, String userid) throws ServiceException {
        double[] expense = new double[2];
        PreparedStatement pstmt = null;
        boolean TaskEnds = false;
        String decimal = ".0";
        try {
            double cost = 0d, work = 0d, materialCost = 0d;
            int nonworkweekArr[] = getNonWorkWeekDays(conn, projid);
            String holidayArr[] = getCompHolidays(conn, projid, "");
            String companyid = CompanyHandler.getCompanyIDFromProject(conn, projid);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdate = sdf.parse(sdf.format(sdate));
            edate = sdf.parse(sdf.format(edate));

            ResourceDAO rd = new ResourceDAOImpl();

            PreparedStatement pstmt1 = conn.prepareStatement("select duration, startdate, enddate from proj_baselinedata "
                        + "where taskid = ? and baselineid = ?");
            pstmt1.setString(1, taskId);
            pstmt1.setString(2, baselineid);
            ResultSet rs1 = pstmt1.executeQuery();
            if (rs1.next()) {
                duration = rs1.getString("duration");
                decimal = (duration.contains(".")) ? duration.substring(duration.indexOf(".")) : ".0";
                if (!duration.contains("h") && !duration.equals("0")) {
                    java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-d");
                    if (!StringUtil.isNullOrEmpty(rs1.getString("startdate"))) {
                        java.util.Date stdate = sdf1.parse(rs1.getString("startdate"));
                        java.util.Date enddate = sdf1.parse(rs1.getString("enddate"));
                        sdate = sdf1.parse(sdf1.format(sdate));
                        edate = sdf1.parse(sdf1.format(edate));
                        if (stdate.before(sdate) || stdate.equals(sdate)) {
                            if (enddate.after(edate) || enddate.equals(edate)) {
                                duration = projectReport.calculateWorkingDays(sdate, edate, nonworkweekArr, holidayArr) + "";
                            } else {
                                duration = projectReport.calculateWorkingDays(sdate, enddate, nonworkweekArr, holidayArr) + "";
                                TaskEnds = true;
                            }
                        } else {
                            if (enddate.after(edate) || enddate.equals(edate)) {
                                duration = projectReport.calculateWorkingDays(stdate, edate, nonworkweekArr, holidayArr) + "";
                            } else {
                                duration = projectReport.calculateWorkingDays(stdate, enddate, nonworkweekArr, holidayArr) + "";
                                TaskEnds = true;
                            }
                        }
                        int dec = Integer.parseInt(decimal.indexOf("d") != -1
                                ? decimal.substring(decimal.indexOf(".") + 1, decimal.indexOf("d")) : decimal.substring(decimal.indexOf(".") + 1));
                        if (dec != 0) {
                            duration = String.valueOf(Integer.parseInt(duration) - 1);
                            duration = duration.concat(decimal);
                        }
                    } else {
                        duration = "0";
                    }
                }
            }
            double dur = 0;
            if (duration.contains("h")) {
                duration = getDecimalsInDuration(duration, "h", decimal);
                dur = Double.parseDouble(duration.substring(0, duration.indexOf("h")));
                dur = dur / 8;
            } else if (duration.contains("d")) {
                dur = Double.parseDouble(duration.substring(0, duration.indexOf("d")));
            } else {
                if (TaskEnds && !duration.contains(".")) {
                    decimal = stripDecimals(decimal);
                    duration = duration.concat(decimal);
                }
                dur = Double.parseDouble(duration);
            }

            if(dur != 0){
                List ll = rd.getResourcesOnBaselineTask(conn, baselineid, taskId, companyid, true);
                Iterator i = ll.listIterator();
                while(i.hasNext()){
                    double tempW = 0d, tempC = 0d;
                    BaselineResource pr = (BaselineResource) i.next();
                    if (pr.getType().getTypeID() == Constants.WORK_RESOURCE){
                        tempW = (dur * Constants.WORK_HOURS) * (pr.getWuvalue() / 100);
                        tempC = pr.getStdRate() * tempW;
                    } else if (pr.getType().getTypeID() == Constants.MATERIAL_RESOURCE){
                        tempW = 0;
                        tempC = pr.getStdRate() * pr.getWuvalue();
                    }
                    work += tempW;
                    cost += tempC;
                }
            }
            expense[0] = cost + materialCost;
            expense[1] = work;
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getTaskCostWorkForBaseline", e);
        } catch (Exception e) {
            throw ServiceException.FAILURE("projdb.getTaskCostWorkForBaseline", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return expense;
    }

    public static double[] getResourceWiseTaskCostWorkForBaseline(Connection conn, String taskId, String duration, String baselineid,
            String projid, String resourceid, java.util.Date sdate, java.util.Date edate, String userid)
            throws ServiceException {
        double[] expense = new double[2];
        PreparedStatement pstmt = null;
        try {
            String decimal = ".0";
            double cost = 0d, work = 0d, materialCost = 0d;
            boolean TaskEnds = true;
            int nonworkweekArr[] = getNonWorkWeekDays(conn, projid);
            String holidayArr[] = getCompHolidays(conn, projid, "");
            String companyid = CompanyHandler.getCompanyIDFromProject(conn, projid);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdate = sdf.parse(sdf.format(sdate));
            edate = sdf.parse(sdf.format(edate));

            ResourceDAO rd = new ResourceDAOImpl();
            Resource r = rd.getResource(conn, resourceid, ResourceDAO.RESOURCE_FROM_BASELINE, baselineid);

            PreparedStatement pstmt1 = conn.prepareStatement("select duration, startdate, enddate from proj_baselinedata "
                        + "where taskid = ? and baselineid = ?");
            pstmt1.setString(1, taskId);
            pstmt1.setString(2, baselineid);
            ResultSet rs1 = pstmt1.executeQuery();
            if (rs1.next()) {
                duration = rs1.getString("duration");
                decimal = (duration.contains(".")) ? duration.substring(duration.indexOf(".")) : ".0";
                if (!duration.contains("h") && !duration.equals("0")) {
                    java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-d");
                    if (!StringUtil.isNullOrEmpty(rs1.getString("startdate"))) {
                        java.util.Date stdate = sdf1.parse(rs1.getString("startdate"));
                        java.util.Date enddate = sdf1.parse(rs1.getString("enddate"));
                        sdate = sdf1.parse(sdf1.format(sdate));
                        edate = sdf1.parse(sdf1.format(edate));
                        if (stdate.before(sdate) || stdate.equals(sdate)) {
                            if (enddate.after(edate) || enddate.equals(edate)) {
                                duration = projectReport.calculateWorkingDays(sdate, edate, nonworkweekArr, holidayArr) + "";
                            } else {
                                duration = projectReport.calculateWorkingDays(sdate, enddate, nonworkweekArr, holidayArr) + "";
                                TaskEnds = true;
                            }
                        } else {
                            if (enddate.after(edate) || enddate.equals(edate)) {
                                duration = projectReport.calculateWorkingDays(stdate, edate, nonworkweekArr, holidayArr) + "";
                            } else {
                                duration = projectReport.calculateWorkingDays(stdate, enddate, nonworkweekArr, holidayArr) + "";
                                TaskEnds = true;
                            }
                        }
                        int dec = Integer.parseInt(decimal.indexOf("d") != -1
                                ? decimal.substring(decimal.indexOf(".") + 1, decimal.indexOf("d")) : decimal.substring(decimal.indexOf(".") + 1));
                        if (dec != 0) {
                            duration = String.valueOf(Integer.parseInt(duration) - 1);
                            duration = duration.concat(decimal);
                        }
                    } else {
                        duration = "0";
                    }
                }
            }

            double dur = 0;
            if (duration.contains("h")) {
                duration = getDecimalsInDuration(duration, "h", decimal);
                dur = Double.parseDouble(duration.substring(0, duration.indexOf("h")));
                dur = dur / 8;
            } else if (duration.contains("d")) {
                dur = Double.parseDouble(duration.substring(0, duration.indexOf("d")));
            } else {
                if (TaskEnds && !duration.contains(".")) {
                    decimal = stripDecimals(decimal);
                    duration = duration.concat(decimal);
                }
                dur = Double.parseDouble(duration);
            }

            List ll = rd.getResourcesOnBaselineTask(conn, baselineid, taskId, companyid, true);
            boolean found = false;
            if(r != null){
                BaselineResource br = (BaselineResource) r;
                Iterator ite = ll.listIterator();
                while(ite.hasNext()){
                    BaselineResource brtemp = (BaselineResource) ite.next();
                    if(br.getResourceID().compareTo(brtemp.getResourceID()) == 0){
                        found = true;
                        break;
                    }
                }
                if(found){
                    if(r.isInUseFlag() && r.isBillable() && dur != 0){
                        if(r.getType().getTypeID() == 1){
                            work = (r.getWuvalue()/100) * (Constants.WORK_HOURS * dur);
                            cost = work * r.getStdRate();
                        } else if(r.getType().getTypeID() == 2){
                            materialCost = r.getStdRate() * r.getWuvalue();
                        }
                    }
                }
            }
            expense[0] = cost + materialCost;
            expense[1] = work;
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getResourceWiseTaskCostWorkForBaseline", e);
        } catch (Exception e) {
            throw ServiceException.FAILURE("projdb.getResourceWiseTaskCostWorkForBaseline", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return expense;
    }

    public static double[] getTaskCostWorkForOverdueTask(Connection conn, String taskId, String projid,
            int diff, java.util.Date enddate, String userid, String companyid) throws ServiceException {
        double[] expense = new double[2];
        PreparedStatement pstmt = null;
        try {
            String duration = "0";
            double cost = 0d, work = 0d;
            java.text.SimpleDateFormat sdfLong = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            enddate = Timezone.getUserToGmtTimezoneDate(conn, userid, sdfLong.format(enddate));
            java.util.Date curdate = new java.util.Date();
            int nonworkweekArr[] = getNonWorkWeekDays(conn, projid);
            String holidayArr[] = getCompHolidays(conn, projid, "");
            duration = projectReport.calculateWorkingDays(enddate, curdate, nonworkweekArr, holidayArr) + "";

            ResourceDAO rd = new ResourceDAOImpl();
            double dur = Double.parseDouble(duration);

            if (dur != 0) {
                List ll = rd.getResourcesOnTask(conn, projid, taskId, true);
                Iterator i = ll.listIterator();
                while(i.hasNext()) {
                    double tempW = 0d, tempC = 0d;
                    ProjectResource pr = (ProjectResource) i.next();
                    if (pr.getType().getTypeID() == Constants.WORK_RESOURCE){
                        tempW = (dur * Constants.WORK_HOURS) * (pr.getWuvalue() / 100);
                        tempC = pr.getStdRate() * tempW;
                    } else if (pr.getType().getTypeID() == Constants.MATERIAL_RESOURCE){
                        tempW = 0;
                        tempC = pr.getStdRate() * pr.getWuvalue();
                    }
                    work += tempW;
                    cost += tempC;
                }
            }

            expense[0] = cost;
            expense[1] = work;
        } catch (Exception e) {
            throw ServiceException.FAILURE("projdb.getTaskCostWorkForOverdueTask", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return expense;
    }

    public static String updatePercentComplete(Connection conn, HttpServletRequest request) throws ServiceException {
        String r = "";
        try {
            PreparedStatement p = conn.prepareStatement("UPDATE proj_task SET percentcomplete = ? WHERE taskid = ?");
            p.setString(1, request.getParameter("val"));
            p.setString(2, request.getParameter("taskid"));
            int i = p.executeUpdate();
            if (i > 0) {
                r = "{data: {taskid:'" + request.getParameter("taskid") + "',percentcomplete:" + request.getParameter("val") + "}}";
                int pc = Integer.parseInt(request.getParameter("val"));
                checkPredecessorNotification(conn, AuthHandler.getUserid(request), request.getParameter("taskid"), pc);
            }
        } catch (NumberFormatException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } catch (SessionExpiredException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } catch (SQLException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return r;
    }

    public static Date calculateEndDate(Connection conn, Date sDate, String duration, int[] NonWorkDays,
            String[] CmpHoliDays, String userid) throws ParseException {
        Date dt = new Date();
        java.text.SimpleDateFormat sdfLong = new java.text.SimpleDateFormat("yyyy-MM-d HH:mm:ss");
        String edate = "";
        Calendar c = Calendar.getInstance();
        dt = sDate;
        double d = getDuration(duration);
        c.setTime(dt);
        if (d > 0) {
            c.add(Calendar.DATE, (int) (d - 1));
        } else {
            c.add(Calendar.DATE, (int) d);
        }
        Date nDate = dt;
        int flag = 0;
        int nwd = projectReport.nonWorkingDays(nDate, c.getTime(), NonWorkDays, CmpHoliDays);
        while (nwd != 0) {
            nDate = c.getTime();
            if (nwd == 1 && flag == 0) {
                c.add(Calendar.DATE, nwd);
                flag = 1;
            } else {
                c.add(Calendar.DATE, nwd - 1);
            }
            nwd = projectReport.nonWorkingDays(nDate, c.getTime(), NonWorkDays, CmpHoliDays);
        }
        dt = c.getTime();
        edate = sdfLong.format(dt);
        if (Arrays.binarySearch(NonWorkDays, (c.get(Calendar.DAY_OF_WEEK) - 1)) > -1) {
            edate = getNextWorkingDay(edate, NonWorkDays, CmpHoliDays);
        }
        dt = sdfLong.parse(edate);
        return dt;
    }

    public static Date calculateEndDate_importCSV(Date sDate, String duration, int[] NonWorkDays, String[] CmpHoliDays)
            throws ParseException {
        Date dt = new Date();
        java.text.SimpleDateFormat sdfLong = new java.text.SimpleDateFormat("yyyy-MM-d HH:mm:ss");
        String edate = "";
        Calendar c = Calendar.getInstance();
        dt = sDate;
        double d = getDuration(duration);
        c.setTime(dt);
        if (d > 0) {
            c.add(Calendar.DATE, (int) (d - 1));
        } else {
            c.add(Calendar.DATE, (int) d);
        }
        Date nDate = dt;
        int flag = 0;
        int nwd = projectReport.nonWorkingDays(nDate, c.getTime(), NonWorkDays, CmpHoliDays);
        while (nwd != 0) {
            nDate = c.getTime();
            if (nwd == 1 && flag == 0) {
                c.add(Calendar.DATE, nwd);
                flag = 1;
            } else {
                c.add(Calendar.DATE, nwd - 1);
            }
            nwd = projectReport.nonWorkingDays(nDate, c.getTime(), NonWorkDays, CmpHoliDays);
        }
        dt = c.getTime();
        edate = sdfLong.format(dt);
        if (Arrays.binarySearch(NonWorkDays, (c.get(Calendar.DAY_OF_WEEK) - 1)) > -1) {
            edate = getNextWorkingDay(edate, NonWorkDays, CmpHoliDays);
        }
        dt = sdfLong.parse(edate);
        return dt;
    }
    /**
     *
     * @param duration
     * @return duration in days
     */
    public static double getDuration(String duration) {
        double d = -1;
        if (!StringUtil.isNullOrEmpty(duration)) {
            boolean f = false;
            if (duration.contains("hrs")) {
                duration = duration.substring(0, duration.length() - 3);
                f = true;
            } else if (duration.contains("hr")) {
                duration = duration.substring(0, duration.length() - 2);
                f = true;
            } else if (duration.contains("h")) {
                duration = duration.substring(0, duration.length() - 1);
                f = true;
            } else if (duration.contains("days")) {
                duration = duration.substring(0, duration.length() - 4);
            } else if (duration.contains("day")) {
                duration = duration.substring(0, duration.length() - 3);
            } else if (duration.contains("d")) {
                duration = duration.substring(0, duration.length() - 1);
            }
            d = Double.parseDouble(duration);
            if (f) {
                d = d / 8;
            }
        }
        return d;
    }

    public static String getNextWorkingDay(String currDate, int[] NWD, String[] CH) {
        Calendar c1 = Calendar.getInstance();
        Date stdate = c1.getTime();
        java.text.SimpleDateFormat sdfLong = new java.text.SimpleDateFormat("yyyy-MM-d HH:mm:ss");
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
            throw ServiceException.FAILURE("projdb.getNextWorkingDay : " + e.getMessage(), e);
        } finally {
            return sdfLong.format(stdate);
        }
    }

    public static String getTaskResources(Connection conn, String taskId, String projid) throws ServiceException {
        String resources = "";
        ResourceDAO rd = new ResourceDAOImpl();
        List ll = rd.getResourcesOnTask(conn, projid, taskId, false);
        resources = Utilities.listToGridJson(ll, rd.getTotalCount(), ProjectResource.class);
        return resources;
    }

    public static String getBillableTaskResources(Connection conn, String taskId, String projid) throws ServiceException {
        String resources = "";
        ResourceDAO rd = new ResourceDAOImpl();
        List ll = rd.getResourcesOnTask(conn, projid, taskId, true);
        resources = Utilities.listToGridJson(ll, rd.getTotalCount(), ProjectResource.class);
        return resources;
    }

    public static String getTaskResourcesOnDataload(Connection conn, String taskId, String projid) throws ServiceException {
        String resources = "";
        try {
            ResultSet rs = null;
            PreparedStatement pstmt = null;
            pstmt = conn.prepareStatement("SELECT proj_taskresourcemapping.resourceid, resourcename, stdrate, billable FROM proj_resources "
                    + "INNER JOIN proj_taskresourcemapping ON proj_resources.resourceid = proj_taskresourcemapping.resourceid "
                    + "WHERE proj_resources.resourceid not in (select userid from userlogin where isactive = false) "
                    + "AND proj_taskresourcemapping.taskid = ? and proj_resources.projid = ?");
            pstmt.setString(1, taskId);
            pstmt.setString(2, projid);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String resname = rs.getString("resourceid");
                resources = resources.concat(resname).concat(",");
            }
            if (resources.length() != 0) {
                resources = resources.substring(0, resources.length() - 1);
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getTaskResourcesOnDataload", e);
        }
        return resources;
    }

    public static String getTaskResourcesNames(Connection conn, String taskId, String projid) throws ServiceException {
        String resources = "";
        try {
            String resData = getTaskResources(conn, taskId, projid);
            JSONObject temp = new JSONObject(resData);
            if (temp.has("data") && temp.getJSONArray("data").toString().compareTo("[]") != 0) {
                for (int i = 0; i < temp.getJSONArray("data").length(); i++) {
                    resources += temp.getJSONArray("data").getJSONObject(i).getString("resourceName") + ",";
                }
                resources = resources.substring(0, resources.length() - 1);
            }
        } catch (JSONException e) {
            throw ServiceException.FAILURE("projdb.getTaskResourcesNames", e);
        }
        return resources;
    }

    public static String getResourceRelatedTask(Connection conn, String resourceid, String projId, String companyid) throws ServiceException {
        String resources = null;
        try {
            ResultSet rs = null;
            PreparedStatement pstmt = null;
            pstmt = conn.prepareStatement("select proj_taskresourcemapping.taskid,proj_task.taskname,proj_task.startdate,proj_task.enddate,proj_task.duration,proj_resources.stdrate from proj_taskresourcemapping"
                    + " inner join proj_task on proj_taskresourcemapping.taskid = proj_task.taskid"
                    + " inner join proj_resources on proj_resources.resourceid = proj_taskresourcemapping.resourceid"
                    + " where proj_taskresourcemapping.resourceid = ? and proj_resources.projid= ? and"
                    + " proj_taskresourcemapping.taskid in (select taskid from proj_task where projectid = ?)"
                    + " and proj_resources.resourceid not in (select userlogin.userid from userlogin"
                    + " inner join users on users.userid = userlogin.userid where users.companyid = ? and userlogin.isactive = false)");
            pstmt.setString(1, resourceid);
            pstmt.setString(2, projId);
            pstmt.setString(3, projId);
            pstmt.setString(4, companyid);
            rs = pstmt.executeQuery();
            KWLJsonConverter KWL = new KWLJsonConverter();
            resources = KWL.GetJsonForGrid(rs);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getResourceRelatedTask", e);
        }
        return resources;
    }

    public static String getResourcesTasks(Connection conn, String[] resources, String projId, String companyid) throws ServiceException {
        String res = "";
        try {
            ResultSet rs = null;
            PreparedStatement pstmt = null;
            for (int i = 0; i < resources.length; i++) {
                res += "'" + resources[i] + "',";
            }
            res = res.substring(0, res.lastIndexOf(","));
            pstmt = conn.prepareStatement("select DISTINCT ptm.taskid, pt.taskname, pt.taskindex from proj_taskresourcemapping ptm"
                    + " inner join proj_task pt on ptm.taskid = pt.taskid"
                    + " inner join proj_resources pr on pr.resourceid = ptm.resourceid"
                    + " where ptm.resourceid in(" + res + ") and pr.projid= ? and"
                    + " ptm.taskid in (select taskid from proj_task where projectid = ?)"
                    + " and pr.resourceid not in (select userlogin.userid from userlogin"
                    + " inner join users on users.userid = userlogin.userid where users.companyid = ?"
                    + " and userlogin.isactive = false)");
            pstmt.setString(1, projId);
            pstmt.setString(2, projId);
            pstmt.setString(3, companyid);
            rs = pstmt.executeQuery();
            JSONArray ja = new JSONArray();
            JSONObject ret = new JSONObject();
            while (rs.next()) {
                JSONObject jo = new JSONObject();
                String taskid = rs.getString("taskid");
                jo.put("taskid", taskid);
                jo.put("taskname", rs.getString("taskname"));
                jo.put("taskindex", rs.getInt("taskindex"));
                String resnames = getTaskResourcesOnDataload(conn, taskid, projId);
                jo.put("existingres", resnames);
                jo.put("newres", resnames);
                ja.put(jo);
            }
            ret.put("data", ja);
            res = ret.toString();
        } catch (JSONException e) {
            throw ServiceException.FAILURE("projdb.getResourcesTasks : " + e.getMessage(), e);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getResourcesTasks : " + e.getMessage(), e);
        }
        return res;
    }

    public static void addLink(Connection conn, String fromId, String toId)
            throws ServiceException {
        PreparedStatement pstmt = null;
        try {

            pstmt = conn.prepareStatement("select count(*) from proj_tasklinks where fromtask = ? and totask = ?");
            pstmt.setString(1, fromId);
            pstmt.setString(2, toId);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            if (rs.getInt(1) == 0) {
                pstmt = conn.prepareStatement("insert into proj_tasklinks(fromtask,totask) values(?,?)");
                pstmt.setString(1, fromId);
                pstmt.setString(2, toId);
                pstmt.execute();
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.addLink", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

    public static void deleteLink(Connection conn, String fromId, String toId)
            throws ServiceException {
        PreparedStatement pstmt = null;
        try {

            pstmt = conn.prepareStatement("delete from proj_tasklinks where fromtask=? and totask=?");
            pstmt.setString(1, fromId);
            pstmt.setString(2, toId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.deleteLink", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

    public static String getTaskName(Connection conn, String taskid)
            throws ServiceException {
        PreparedStatement pstmt = null;
        String taskName = "";
        try {
            ResultSet rs = null;
            pstmt = conn.prepareStatement("select taskname from proj_task where taskid = ?");
            pstmt.setString(1, taskid);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                if (rs.getString("taskname") != null) {
                    taskName = rs.getString("taskname");
                }
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getTaskName error", e);
        } finally {
            DbPool.closeStatement(pstmt);
            return taskName;
        }
    }

    public static void deleteTask(Connection conn, String taskId,
            String projectid) throws ServiceException {
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("select taskindex from proj_task where taskid =?");
            pstmt.setString(1, taskId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {

                CheckListManager manager = new CheckListManager();
                CheckList cl = manager.getAssociatedCheckList(conn, taskId);
                if(cl != null){
                    manager.removeAssociatedCheckListFromTask(conn, taskId, cl.getCheckListID());
                }

                String rowindex = rs.getString("taskindex");
                pstmt = conn.prepareStatement("delete from proj_task where taskid=?");
                pstmt.setString(1, taskId);
                pstmt.executeUpdate();

                pstmt = conn.prepareStatement("update proj_task set taskindex = taskindex-1 where taskindex>? and projectid=?");
                pstmt.setString(1, rowindex);
                pstmt.setString(2, projectid);
                pstmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.deleteTask", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

    public static String getminmaxProjectDate(Connection conn, String projectid, String userid)
            throws ServiceException {
        PreparedStatement pstmt = null;
        String returnStr = null;
        Date projectStDate = null;
        Date minDate = null;
        ResultSet rs = null;
        try {

            pstmt = conn.prepareStatement("select startdate from project where projectid=?");
            pstmt.setString(1, projectid);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                if (rs.getObject("startdate") != null) {
                    projectStDate = rs.getTimestamp("startdate");
                    minDate = projectStDate;
                }
            }

            pstmt = conn.prepareStatement("select min(startdate),max(enddate) from proj_task where projectid=?");
            pstmt.setString(1, projectid);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                if (rs.getString(1) != null) {
                    Date temp = rs.getTimestamp(1);
                    if (projectStDate != null) {
                        if (DateUtility.compare(projectStDate, temp) == 1) {
                            minDate = temp;
                        }
                    } else {
                        minDate = rs.getTimestamp(1);
                    }
                    returnStr = minDate.toString();
                    returnStr += "," + rs.getTimestamp(2).toString();
                    if (projectStDate != null) {
                        String cid = CompanyHandler.getCompanyIDFromProject(conn, projectid);
                        returnStr += "," + Timezone.toCompanyTimezone(conn, projectStDate.toString(), cid);
                    }
                }
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getminmaxProjectDate", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return returnStr;
    }

    public static String getProjectStartDate(Connection conn, String projectid, String userid)
            throws ServiceException {
        PreparedStatement pstmt = null;
        String returnStr = null;
        Date projectStDate = null;
        Date minDate = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement("select startdate, createdon from project where projectid=?");
            pstmt.setString(1, projectid);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                if (rs.getObject("startdate") != null) {
                    projectStDate = rs.getTimestamp("startdate");
                    returnStr = projectStDate.toString();
                } else {
                    projectStDate = rs.getTimestamp("createdon");
                    returnStr = projectStDate.toString();
                }
            }
            String companyid = CompanyHandler.getCompanyIDFromProject(conn, projectid);
            returnStr = Timezone.toCompanyTimezone(conn, returnStr, companyid);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getminmaxProjectDate", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return returnStr;
    }

    public static String getProjectDatesForReport(Connection conn, String projectid, String userid) throws ServiceException {
        PreparedStatement pstmt = null;
        String returnStr = null;
        ResultSet rs = null;
        java.util.Date date = null;
        java.util.Date stdate = null;
        String startdate = "";
        boolean emptyProj = false;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-d HH:mm:ss");
        try {
            String temp;
            pstmt = conn.prepareStatement("select startdate from project where projectid=?");
            pstmt.setString(1, projectid);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                if (rs.getDate("startdate") != null) {
                    temp = rs.getString("startdate");
                    date = sdf.parse(temp);
                    stdate = date;
                    startdate = sdf.format(date);
                }
            }
            pstmt.close();
            java.util.Date mindate = null;
            pstmt = conn.prepareStatement("select min(startdate) as mindate,max(enddate) as maxdate from proj_task where projectid=?");
            pstmt.setString(1, projectid);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                if (rs.getString("mindate") != null && rs.getString("maxdate") != null) {
                    temp = rs.getString("mindate");
                    returnStr = sdf.format(sdf.parse(temp));
                    temp = rs.getString("maxdate");
                    returnStr += "," + sdf.format(sdf.parse(temp));
                } else
                    emptyProj = true;
            }
            if(startdate.compareTo("") != 0){
                if(emptyProj)
                    returnStr = sdf.format(stdate);
                returnStr += "," + sdf.format(stdate);
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("projdb.getProjectDatesForReport", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return returnStr;
    }

    public static String setStartDate(Connection conn, String projectid,
            String startDate, String userid, String option) throws ServiceException {
        String result = "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-d HH:mm:ss");
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String errormsg = null;
        String mindate = null;
        boolean flag = true;
        int num = 0;
        try {
            String sdt = startDate;
            String companyid = CompanyHandler.getCompanyByUser(conn, userid);
            startDate = Timezone.fromCompanyToSystem(conn, sdt, companyid);
            java.util.Date sd = sdf.parse(startDate);

            pstmt = conn.prepareStatement("select min(startdate) as mindate,max(enddate) from proj_task where projectid=?");
            pstmt.setString(1, projectid);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                if (rs.getString(1) != null) {
                    mindate = rs.getString("mindate");
                }
            }
            if (StringUtil.isNullOrEmpty(mindate)) {
                mindate = startDate;
            }
            if (option.equalsIgnoreCase("yes")) {
                result = shiftProjectStartDate(conn, projectid, userid, sd);
            } else {
                pstmt = conn.prepareStatement("update project set startdate = ? where projectid = ?");
                pstmt.setObject(1, sd);
                pstmt.setString(2, projectid);
                num = pstmt.executeUpdate();
                if (num == 1) {
                    result = "{\"success\": true}";
                }
            }


            if (!flag) {
                result = "{\"success\": false,\"msg\":\"" + errormsg + "\"}";
            }

        } catch (JSONException ex) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DbPool.closeStatement(pstmt);
            return result;
        }
    }

    public static String shiftProjectStartDate(Connection conn, String projectid, String userid, Date sd)
            throws ServiceException, JSONException, ParseException, SQLException {

        String result = "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-d HH:mm:ss");
        java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-d");

        PreparedStatement pstmt = null;
        int num = 0;

        String companyid = CompanyHandler.getCompanyIDFromProject(conn, projectid);
        String tasks = getProjectTasks(conn, projectid, userid, companyid, 0, -1, false);
        JSONArray jsonArray = new JSONObject(tasks).getJSONArray("data");
        Date minDate = null;
        for (int k = 0; k < jsonArray.length(); k++) {
            JSONObject jobj = jsonArray.getJSONObject(k);
            if (jobj.getString("startdate").compareTo("") != 0) {
                if (minDate == null) {
                    minDate = sdf.parse(jobj.getString("startdate"));
                } else {
                    Date tempDate = sdf1.parse(jobj.getString("startdate"));
                    if (tempDate.before(minDate)) {
                        minDate = tempDate;
                    }
                }
            }
        }
        Date minProjDate = sd;
        int diffDays = (int) ((minProjDate.getTime() - minDate.getTime()) / (1000 * 60 * 60 * 24));

        for (int k = 0; k < jsonArray.length(); k++) {
            JSONObject jobj = jsonArray.getJSONObject(k);
            int nonworkweekArr[] = projdb.getNonWorkWeekDays(conn, projectid);
            String holidayArr[] = projdb.getCompHolidays(conn, projectid, "");
            if (jobj.getString("startdate").compareTo("") != 0) {
                Date dt = sdf.parse(jobj.getString("startdate"));
                Calendar cal = Calendar.getInstance();
                cal.setTime(dt);
                cal.add(Calendar.DATE, diffDays);
                dt = cal.getTime();
                boolean f = true;
                while (f) {
                    if ((Arrays.binarySearch(nonworkweekArr, dt.getDay()) >= 0 || Arrays.binarySearch(holidayArr, sdf1.format(dt)) >= 0)) {
                        cal.setTime(dt);
                        cal.add(Calendar.DATE, 1);
                        dt = cal.getTime();
                        f = true;
                    } else {
                        f = false;
                    }
                }

                jobj.put("startdate", sdf.format(cal.getTime()));
                jobj.put("enddate", sdf.format(projdb.calculateEndDate_importCSV(cal.getTime(),
                        jobj.getString("duration"), nonworkweekArr, holidayArr)));
                dt = sdf.parse(jobj.getString("actualstartdate"));
                cal.setTime(dt);
                cal.add(Calendar.DATE, diffDays);
                jobj.put("actstartdate", sdf.format(cal.getTime()));
            } else {
                jobj.put("startdate", "");
                jobj.put("actstartdate", "");
                jobj.put("enddate", "");
            }

            UpdateTask(conn, jobj, jobj.getString("taskid"), userid);
        }
        pstmt = conn.prepareStatement("update project set startdate = ? where projectid = ?");
        pstmt.setObject(1, sd);
        pstmt.setString(2, projectid);
        num = pstmt.executeUpdate();
        if (num == 1) {
            result = "{\"success\": true}";
        }
        return result;
    }

    public static boolean checkBaseline(Connection conn, String pid) throws ServiceException {
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(" SELECT count(*) AS count FROM proj_baselinemap WHERE projectid = ? ");
            pstmt.setString(1, pid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int countBaseline = rs.getInt("count");
                int baselineLimit = Integer.parseInt(com.krawler.esp.utils.ConfigReader.getinstance().get("baselineLimit"));
                if (countBaseline < baselineLimit) {
                    return true;
                }
            }
            return false;

        } catch (SQLException ex) {
            throw ServiceException.FAILURE("projdb.checkBaseline", ex);

        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

    public static String saveBaseline(Connection conn, String projId, String userId,
            String baselineName, String baselineDescription) throws ServiceException {

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String res = KWLErrorMsgs.rsSuccessTrue;
        String baselineId = UUID.randomUUID().toString();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            pstmt = conn.prepareStatement(" SELECT * FROM proj_task where projectid = ? ");
            pstmt.setString(1, projId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                pstmt = conn.prepareStatement(" INSERT INTO proj_baselinemap "
                        + " (baselineid, projectid, userid, createdon, baselinename, description) "
                        + " VALUES (?, ?, ?, ?, ?, ?) ");
                pstmt.setString(1, baselineId);
                pstmt.setString(2, projId);
                pstmt.setString(3, userId);
                pstmt.setTimestamp(4, new Timestamp(new Date().getTime()));
                pstmt.setString(5, baselineName);
                pstmt.setString(6, baselineDescription);
                pstmt.executeUpdate();

                pstmt = conn.prepareStatement(" SELECT taskindex, taskid, taskname, duration, "
                        + " durationtype, startdate, enddate, percentcomplete, notes, "
                        + " priority, estimated, barvisstatus, deadline, constrainttype, "
                        + " constraintdate, tasktype, caltype, level, parent, "
                        + " actualduration, actualstartdate, timestamp, predecessor, "
                        + " isparent, workholidays FROM proj_task WHERE projectid = ? ");

                pstmt.setString(1, projId);
                rs = pstmt.executeQuery();
                while (rs.next()) {

                    pstmt = conn.prepareStatement(" INSERT INTO proj_baselinedata "
                            + " ( baselineid, taskindex, taskid, taskname, duration, durationtype, "
                            + " startdate, enddate, percentcomplete, notes, priority, "
                            + " estimated, barvisstatus, deadline, constrainttype, "
                            + " constraintdate, tasktype, caltype, level, parent, "
                            + " actualduration, actualstartdate, timestamp, predecessor, "
                            + " isparent, workholidays ) VALUES "
                            + " ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,"
                            + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) ");

                    pstmt.setString(1, baselineId);
                    pstmt.setString(2, rs.getString("taskindex"));
                    pstmt.setString(3, rs.getString("taskid"));
                    pstmt.setString(4, rs.getString("taskname"));
                    pstmt.setString(5, rs.getString("duration"));
                    pstmt.setString(6, rs.getString("durationtype"));

                    String str = rs.getString("startdate");
                    if (!StringUtil.isNullOrEmpty(str)) {
                        java.util.Date DateVal = sdf1.parse(str);
                        Timestamp ts = new Timestamp(DateVal.getTime());
                        pstmt.setTimestamp(7, ts);
                    } else {
                        pstmt.setTimestamp(7, null);
                    }
                    if (!StringUtil.isNullOrEmpty(rs.getString("enddate"))) {
                        java.util.Date DateVal = sdf1.parse(rs.getString("enddate"));
                        Timestamp ts = new Timestamp(DateVal.getTime());
                        pstmt.setTimestamp(8, ts);
                    } else {
                        pstmt.setTimestamp(8, null);
                    }

                    pstmt.setString(9, rs.getString("percentcomplete"));

                    pstmt.setString(10, rs.getString("notes"));
                    int prt = 1;
                    if (rs.getString("priority") != null) {
                        prt = Integer.parseInt(rs.getString("priority"));
                    }
                    pstmt.setInt(11, prt);
                    pstmt.setString(12, rs.getString("estimated"));
                    pstmt.setString(13, rs.getString("barvisstatus"));
                    pstmt.setString(14, rs.getString("deadline"));
                    pstmt.setString(15, rs.getString("constrainttype"));
                    pstmt.setString(16, rs.getString("constraintdate"));
                    pstmt.setString(17, rs.getString("tasktype"));
                    pstmt.setString(18, rs.getString("caltype"));
                    pstmt.setString(19, rs.getString("level"));
                    pstmt.setString(20, rs.getString("parent"));
                    pstmt.setString(21, rs.getString("actualduration"));

                    if (!StringUtil.isNullOrEmpty(rs.getString("actualstartdate"))) {
                        java.util.Date DateVal = sdf1.parse(rs.getString("actualstartdate"));
                        Timestamp ts = new Timestamp(DateVal.getTime());
                        pstmt.setTimestamp(22, ts);
                    } else {
                        pstmt.setTimestamp(22, null);
                    }
                    if (!StringUtil.isNullOrEmpty(rs.getString("timestamp"))) {
                        java.util.Date DateVal = sdf.parse(rs.getString("timestamp"));
                        Timestamp ts = new Timestamp(DateVal.getTime());
                        pstmt.setTimestamp(23, ts);
                    } else {
                        pstmt.setTimestamp(23, null);
                    }

                    pstmt.setString(24, rs.getString("predecessor"));
                    pstmt.setBoolean(25, rs.getBoolean("isparent"));
                    pstmt.setString(26, rs.getString("workholidays"));

                    pstmt.executeUpdate();

                    pstmt = conn.prepareStatement(" SELECT * FROM proj_tasklinks WHERE totask = ? ");
                    pstmt.setString(1, rs.getString("taskid"));
                    ResultSet rsLinks = pstmt.executeQuery();
                    while (rsLinks.next()) {
                        pstmt = conn.prepareStatement(" INSERT INTO proj_baselinetasklinks "
                                + " ( baselineid, fromtask, totask, timestamp ) "
                                + " VALUES ( ?, ?, ?, ?) ");
                        pstmt.setString(1, baselineId);
                        pstmt.setString(2, rsLinks.getString("fromtask"));
                        pstmt.setString(3, rsLinks.getString("totask"));
                        pstmt.setString(4, rsLinks.getString("timestamp"));

                        pstmt.executeUpdate();
                    }

                    pstmt = conn.prepareStatement(" SELECT * FROM proj_taskresourcemapping "
                            + " INNER JOIN proj_resources "
                            + " ON proj_taskresourcemapping.resourceid = proj_resources.resourceid "
                            + " WHERE taskid = ? and projid = ? ");
                    pstmt.setString(1, rs.getString("taskid"));
                    pstmt.setString(2, projId);
                    ResultSet rsResources = pstmt.executeQuery();
                    while (rsResources.next()) {
                        pstmt = conn.prepareStatement(" INSERT INTO proj_baselinetaskresources "
                                + " ( baselineid, taskid, resourceid ) "
                                + " VALUES (?, ?, ?) ");
                        pstmt.setString(1, baselineId);
                        pstmt.setString(2, rsResources.getString("taskid"));
                        pstmt.setString(3, rsResources.getString("resourceid"));

                        pstmt.executeUpdate();
                    }
                }
                pstmt = conn.prepareStatement(" SELECT * FROM proj_resources WHERE projid = ?");
                pstmt.setString(1, projId);
                ResultSet rsResourcesData = pstmt.executeQuery();
                while (rsResourcesData.next()) {
                    pstmt = conn.prepareStatement("INSERT INTO proj_baselineresourcesdata "
                            + " ( baselineid, resourceid, resourcename, stdrate, typeid, colorcode, wuvalue, categoryid ) "
                            + " VALUES ( ?, ?, ?, ?, ?, ?, ?, ? ) ");
                    pstmt.setString(1, baselineId);
                    pstmt.setString(2, rsResourcesData.getString("resourceid"));
                    pstmt.setString(3, rsResourcesData.getString("resourcename"));
                    pstmt.setInt(4, rsResourcesData.getInt("stdrate"));
                    pstmt.setInt(5, rsResourcesData.getInt("typeid"));
                    pstmt.setString(6, rsResourcesData.getString("colorcode"));
                    pstmt.setInt(7, rsResourcesData.getInt("wuvalue"));
                    pstmt.setInt(8, rsResourcesData.getInt("categoryid"));

                    pstmt.executeUpdate();
                }

            } else {
                res = "{\"success\":false,\"data\": \"noTask\"}";
            }

        } catch (ParseException ex) {
            res = KWLErrorMsgs.rsSuccessFalse;
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);

        } catch (SQLException e) {
            res = KWLErrorMsgs.rsSuccessFalse;
            throw ServiceException.FAILURE("projdb.saveBaseline", e);

        } catch (Exception e) {
            res = KWLErrorMsgs.rsSuccessFalse;
            throw ServiceException.FAILURE("projdb.saveBaseline", e);

        } finally {
            DbPool.closeStatement(pstmt);
        }
        return res;
    }

    public static String getBaseline(Connection conn, String projId, String userid)
            throws ServiceException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String returnStr = null;
        try {
            pstmt = conn.prepareStatement(" SELECT proj_baselinemap.*, "
                    + " CONCAT(users.fname, ' ', users.lname) AS createdby "
                    + " FROM proj_baselinemap INNER JOIN users "
                    + " ON proj_baselinemap.userid = users.userid "
                    + " WHERE projectid = ? ORDER BY createdon ");
            pstmt.setString(1, projId);
            rs = pstmt.executeQuery();
            KWLJsonConverter kjc = new KWLJsonConverter();
            returnStr = kjc.GetJsonForGrid(rs);
            JSONObject job;
            if (returnStr.compareTo("{data:{}}") != 0) {
                job = new JSONObject(returnStr);
                for (int i = 0; i < job.getJSONArray("data").length(); i++) {
                    String dtStr = job.getJSONArray("data").getJSONObject(i).getString("createdon");
                    if (dtStr.compareTo("") != 0) {
                        dtStr = Timezone.toCompanyTimezone(conn, dtStr, userid);
                    }
                    job.getJSONArray("data").getJSONObject(i).remove("createdon");
                    job.getJSONArray("data").getJSONObject(i).put("createdon", dtStr);
                }
                returnStr = job.toString();
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getBaseline", e);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("projdb.getBaseline", ex);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return returnStr;
    }

    public static String getBaselineDetails(Connection conn, String baselineId)
            throws ServiceException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String returnStr = null;
        try {
            pstmt = conn.prepareStatement(" SELECT * FROM proj_baselinemap WHERE baselineid = ? ");
            pstmt.setString(1, baselineId);
            rs = pstmt.executeQuery();
            KWLJsonConverter kjc = new KWLJsonConverter();
            returnStr = kjc.GetJsonForGrid(rs);

        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getBaselineDetails", e);

        } finally {
            DbPool.closeStatement(pstmt);
        }
        return returnStr;
    }

    public static String getBaselineData(Connection conn, String baselineId, String projId, String userid)
            throws ServiceException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String returnStr = null;
        try {
            pstmt = conn.prepareStatement(" SELECT * FROM proj_baselinedata WHERE baselineid = ? ORDER BY taskindex ");
            pstmt.setString(1, baselineId);
            rs = pstmt.executeQuery();
            KWLJsonConverter kjc = new KWLJsonConverter();
            returnStr = kjc.GetJsonForGrid(rs);

            if (returnStr.compareTo("{data:{}}") != 0) {
                com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject(returnStr);
                com.krawler.utils.json.base.JSONArray jArray = jobj.getJSONArray("data");
                for (int i = 0; i < jArray.length(); i++) {
                    String sdtStr = jArray.getJSONObject(i).getString("startdate");
                    String edtStr = jArray.getJSONObject(i).getString("enddate");
                    String actsdt = jArray.getJSONObject(i).getString("actualstartdate");
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-d HH:mm:ss");
                    java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-d");
                    if (sdtStr.compareTo("") != 0) {
                        java.util.Date dt = sdf.parse(sdtStr);
                        sdtStr = sdf1.format(dt);
                        jArray.getJSONObject(i).remove("startdate");
                        jArray.getJSONObject(i).put("startdate", sdtStr);
                    }
                    if (edtStr.compareTo("") != 0) {
                        java.util.Date dt = sdf.parse(edtStr);
                        edtStr = sdf1.format(dt);
                        jArray.getJSONObject(i).remove("enddate");
                        jArray.getJSONObject(i).put("enddate", edtStr);
                    }
                    if (actsdt.compareTo("") != 0) {
                        java.util.Date dt = sdf.parse(actsdt);
                        actsdt = sdf1.format(dt);
                        jArray.getJSONObject(i).remove("actualstartdate");
                        jArray.getJSONObject(i).put("actstartdate", actsdt);
                    }
                    String predecessor = getBaselinePredecessor(conn, jArray.getJSONObject(i).getString("taskid"), baselineId);
                    jArray.getJSONObject(i).put("predecessor", predecessor);
                    String resources = getBaselineTaskResources(conn, jArray.getJSONObject(i).getString("taskid"), projId, baselineId);
                    jArray.getJSONObject(i).put("resourcename", resources);
                }
                returnStr = jobj.toString();
            }

        } catch (ParseException ex) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);

        } catch (JSONException ex) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);

        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getBaselineData", e);

        } finally {
            DbPool.closeStatement(pstmt);
        }
        return returnStr;
    }

    public static String getBaselinePredecessor(Connection conn, String taskId, String baselineId)
            throws ServiceException {
        String predecessor = "";
        PreparedStatement pstmt = null;
        try {
            ResultSet rs = null;

            pstmt = conn.prepareStatement(" SELECT taskindex FROM proj_baselinedata WHERE taskid IN "
                    + " ( SELECT fromtask FROM proj_baselinetasklinks WHERE totask = ? AND baselineid = ? ) "
                    + " AND baselineid = ? ORDER BY taskindex ");
            pstmt.setString(1, taskId);
            pstmt.setString(2, baselineId);
            pstmt.setString(3, baselineId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String Fromtaskid = rs.getString(1);
                boolean flag = true;
                String[] stringArr = predecessor.split(",");
                for (int i = 0; i < stringArr.length; i++) {
                    if (Fromtaskid.equals(stringArr[i])) {
                        flag = false;
                    }
                }
                if (flag) {
                    predecessor = predecessor.concat(Fromtaskid).concat(",");
                }
            }
            if (predecessor.length() != 0) {
                predecessor = predecessor.substring(0, predecessor.length() - 1);
            }

        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getPredecessor", e);

        } finally {
            DbPool.closeStatement(pstmt);
        }
        return predecessor;
    }

    public static String getBaselineTaskResources(Connection conn, String taskId, String projid, String baselineId) throws ServiceException {
        String resources = "";
        ResourceDAO rd = new ResourceDAOImpl();
        String companyid = CompanyHandler.getCompanyIDFromProject(conn, projid);
        List ll = rd.getResourcesOnBaselineTask(conn, baselineId, taskId, companyid, false);
        Iterator ite = ll.listIterator();
        while (ite.hasNext()) {
            BaselineResource br = (BaselineResource) ite.next();
            String resname = br.getResourceName();
            resources = resources.concat(resname).concat(",");
        }
        if (resources.length() != 0) {
            resources = resources.substring(0, resources.length() - 1);
        }
        return resources;
    }

    public static String getBaselineResources(Connection conn, String baselineId) throws ServiceException {
        String resources = "";
        ResourceDAO rd = new ResourceDAOImpl();
        List ll = rd.getResourcesOnBaseline(conn, baselineId, false);
        resources = Utilities.listToGridJson(ll, rd.getTotalCount(), BaselineResource.class);
        return resources;
    }

    public static String deleteBaseline(Connection conn, String projId, String baselineId)
            throws ServiceException {

        int cntUpdate = 0;
        String resultStr = "";

        PreparedStatement pstmt = null;

        String DELETE_BASELINE = " DELETE FROM proj_baselinemap WHERE baselineid = ? ";
        String GET_FIRST_BASELINE = " SELECT baselineid FROM proj_baselinemap WHERE projectid = ? ORDER BY createdon ASC LIMIT 1 ";

        try {
            if (baselineId.compareTo("-1") == 0) {
                pstmt = conn.prepareStatement(GET_FIRST_BASELINE);
                pstmt.setString(1, projId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    baselineId = rs.getString("baselineid");
                }
            }

            if (baselineId.compareTo("-1") != 0) {
                pstmt = conn.prepareStatement(DELETE_BASELINE);
                pstmt.setString(1, baselineId);
                cntUpdate = pstmt.executeUpdate();
            }

            if (cntUpdate > 0) {
                resultStr = baselineId;
            }

        } catch (SQLException ex) {
            throw ServiceException.FAILURE("projdb.deleteBaseline", ex);

        } catch (ServiceException sx) {
            throw ServiceException.FAILURE("projdb.deleteBaseline", sx);

        } finally {
            DbPool.closeStatement(pstmt);
        }
        return resultStr;
    }

//        public static boolean checkBaseline(Connection conn, String pid, String uid) throws ServiceException{
//            PreparedStatement pstmt = null;
//            try{
//                pstmt = conn.prepareStatement("SELECT count(*) AS count FROM proj_baseline WHERE projectid=? AND userid = ?");
//                pstmt.setString(1, pid);
//                pstmt.setString(2, uid);
//                ResultSet rs = pstmt.executeQuery();
//                if(rs.next()){
//                    if(rs.getInt("count") > 0){
//                        return true;
//                    }
//                }
//                return false;
//            } catch (SQLException ex) {
//                throw ServiceException.FAILURE("projdb.saveBaseline", ex);
//            } finally {
//                DbPool.closeStatement(pstmt);
//            }
//        }
//
//	public static String saveBaseline(Connection conn, String projId,
//			String userId) throws ServiceException {
//		PreparedStatement pstmt = null;
//        String res = "{\"success\": false}";
//		try {
//            pstmt = conn.prepareStatement("SELECT * FROM proj_task WHERE projectid = ?");
//            pstmt.setString(1, projId);
//            ResultSet rs = pstmt.executeQuery();
//            KWLJsonConverter kjc = new KWLJsonConverter();
//            String jsonStr = kjc.GetJsonForGrid(rs);
//            pstmt = conn.prepareStatement("DELETE FROM proj_baseline WHERE projectid = ? AND userid = ?");
//            pstmt.setString(1, projId);
//            pstmt.setString(2, userId);
//            int num = pstmt.executeUpdate();
//			pstmt = conn.prepareStatement("INSERT INTO proj_baseline(projectid,userid,json) VALUES (?,?,?)");
//			pstmt.setString(1, projId);
//			pstmt.setString(2, userId);
//			pstmt.setString(3, jsonStr);
//			num = pstmt.executeUpdate();
//                        if(num==1){
//                            res = "{\"success\": true}";
//                        }
//                        //?passed param commit here
////                        conn.commit();
//		} catch (SQLException e) {
//			throw ServiceException.FAILURE("projdb.saveBaseline", e);
//		} finally {
//			DbPool.closeStatement(pstmt);
//		}
//                return res;
//	}
//
//	public static String getbaseline(Connection conn, String projId)
//			throws ServiceException {
//		PreparedStatement pstmt = null;
//		ResultSet rs = null;
//		String returnStr = null;
//		try {
//
//			pstmt = conn
//					.prepareStatement("select json from proj_baseline where projectid = ?");
//			pstmt.setString(1, projId);
//			rs = pstmt.executeQuery();
//			rs.next();
//			returnStr = rs.getString(1);
//		} catch (SQLException e) {
//			throw ServiceException.FAILURE("projdb.getbaseline", e);
//		} finally {
//			DbPool.closeStatement(pstmt);
//		}
//		return returnStr;
//	}
    public static void updateParentFieldOfRecord(Connection conn,
            String taskid, String parenttaskId) throws ServiceException {
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("update proj_task set parent = ? where taskid = ?");
            pstmt.setString(1, parenttaskId);
            pstmt.setString(2, taskid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.updateParentFieldOfRecord",
                    e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }
    /*
    public static void checkForProjectMeemberInProj_resource(Connection conn,
    String projid, String projectMemberList) throws ServiceException {
    DbResults rs = null;
    String query = null;
    try {
    com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject(
    projectMemberList);
    for (int i = 0; i < jobj.getJSONArray("data").length(); i++) {
    String resource_id = jobj.getJSONArray("data").getJSONObject(i)
    .getString("id");
    query = "select * from proj_resources where resourceid = ?  and projid = ?";
    Object[] params = new Object[] { resource_id, projid };
    rs = DbUtil.executeQuery(conn, query, params);
    rs.next();
    if (rs.size() == 0) {
    //					query = "insert into proj_resources (resourceid,resourcename,projid) values (?, ?, ?)";
    //					params = new Object[] {
    //							resource_id,
    //							jobj.getJSONArray("data").getJSONObject(i)
    //									.getString("name"), projid };
    //					DbUtil.executeUpdate(conn, query, params);
    }
    }
    } catch (ServiceException e) {
    throw ServiceException.FAILURE("projdb.updateParentFieldOfRecord",
    e);
    } catch (JSONException e) {
    throw ServiceException.FAILURE("projdb.getBuffer", e);
    } finally {
    }
    }
     */

    public static String getProjectName(Connection conn, String projid)
            throws ServiceException {
        PreparedStatement pstmt = null;
        String projName = "";
        try {
            ResultSet rs = null;
            pstmt = conn.prepareStatement("select projectname from project where projectid = ?");
            pstmt.setString(1, projid);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                if (rs.getString("projectname") != null) {
                    projName = rs.getString("projectname");
                }
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getProjectName error", e);
        } finally {
            DbPool.closeStatement(pstmt);
            return projName;
        }
    }

    public static void deleteAllRecords(Connection conn, String projid)
            throws ServiceException {
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("delete from proj_task where projectid = ?");
            pstmt.setString(1, projid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.deleteAllRecords error", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

    public static String getNWWeekAndCompHolidays(Connection conn, String projid)
            throws ServiceException {
        String retStr = null;
        JSONObject retJObj = new JSONObject();
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try {
            // week holidays
            pstmt = conn.prepareStatement("select day from proj_workweek where isholiday = true and projectid = ?");
            pstmt.setString(1, projid);
            rs = pstmt.executeQuery();
            JSONObject jobj = new JSONObject();

            while (rs.next()) {
                JSONObject jtemp = new JSONObject();
                jtemp.put("day", rs.getString("day"));
                jobj.append("nonworkweekdays", jtemp);
            }

            // Public Holidays
            pstmt = conn.prepareStatement("SELECT holiday, description FROM companyholidays WHERE companyid = (SELECT companyid FROM project WHERE projectid = ?)");
            pstmt.setString(1, projid);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                JSONObject jtemp = new JSONObject();
                jtemp.put("holiday", rs.getString("holiday"));
                retJObj.append("companyholidays", jtemp);
            }
            retStr = retJObj.toString() + "," + jobj.toString();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getNWWeekAndCompHolidays", e);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("projdb.getNWWeekAndCompHolidays", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return retStr;
    }

    public static String getNonWorkWeekdays(Connection conn, String projid)
            throws ServiceException {
        String retStr = null;
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try {
            // week holidays
            pstmt = conn.prepareStatement("select day from proj_workweek where isholiday = true and projectid = ?");
            pstmt.setString(1, projid);
            rs = pstmt.executeQuery();
            KWLJsonConverter kjs = new KWLJsonConverter();
            retStr = kjs.GetJsonForGrid(rs);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getNonWorkWeekdays error", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return retStr;
    }

    public static String getCmpHolidaydays(Connection conn, String projid) throws ServiceException {
        String res = "";
        String qry = "SELECT holiday, description FROM companyholidays WHERE companyid = (SELECT companyid FROM project WHERE projectid = ?)";
        try {
            PreparedStatement pstmt = conn.prepareStatement(qry);
            pstmt.setString(1, projid);
            ResultSet rs = pstmt.executeQuery();
            KWLJsonConverter j = new KWLJsonConverter();
            res = j.GetJsonForGrid(rs);
            if (res.compareTo("{data:{}}") == 0) {
                qry = "SELECT holiday, description FROM companyholidays WHERE companyid = ?";
                pstmt = conn.prepareStatement(qry);
                pstmt.setString(1, projid);
                rs = pstmt.executeQuery();
                res = j.GetJsonForGrid(rs);
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getCmpHolidaydays : " + e.getMessage(), e);
        }
        return res;
    }

    public static void chkProjDateOnImport(Connection conn, String projid)
            throws ServiceException {
        PreparedStatement pstmt = null;
        java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-d");
        String projstdate = "";
        String mindate = null;
        try {
            ResultSet rs = null;
            pstmt = conn.prepareStatement("select min(startdate),max(enddate) from proj_task where projectid=?");
            pstmt.setString(1, projid);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                if (rs.getString(1) != null) {
                    mindate = rs.getString(1);
                }
            }
            pstmt = null;
            pstmt = conn.prepareStatement("select startdate from project where projectid=?");
            pstmt.setString(1, projid);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                if (rs.getObject("startdate") != null) {
                    if (mindate != null && sdf1.parse(mindate).before(sdf1.parse(rs.getString("startdate")))) {
                        projstdate = rs.getString("startdate");
                        pstmt = conn.prepareStatement("update project set startdate = ? where projectid = ?");
                        pstmt.setObject(1, sdf1.parse(mindate));
                        pstmt.setString(2, projid);
                        pstmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.chkProjDateOnImport error", e);
        } catch (ParseException e) {
            throw ServiceException.FAILURE("projdb.chkProjDateOnImport error", e);
        } catch (Exception ex) {
            DbPool.quietRollback(conn);
            Logger.getLogger(importProjectPlanCSV.class.getName()).log(Level.SEVERE, null, ex);
        }finally {
            DbPool.closeStatement(pstmt);
        }
    }

    public static int getProjectTasksCount(Connection conn, String projid, String userid, boolean isUserTask) throws ServiceException {
        int count = 0;
        try {
            String query = "";
            PreparedStatement pstmt = null;
            if (isUserTask) {
                query = "SELECT COUNT(taskid) as totaltasks FROM proj_task WHERE projectid = ? AND taskid IN " + "(SELECT taskid FROM proj_taskresourcemapping WHERE resourceid = ?)";
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, projid);
                pstmt.setString(2, userid);
            } else {
                query = "SELECT COUNT(*) as totaltasks FROM proj_task WHERE projectid = ?";
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, projid);
            }
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt("totaltasks");
            }
        } catch (SQLException ex) {
            throw ServiceException.FAILURE("projdb.getProjectTaskCount : " + ex.getMessage(), ex);
        }
        return count;
    }

    public static String getProjectTasks(Connection conn, String projid, String userid, String companyid, int offset, int limit, boolean convertTimezone) {
        String returnStr = "";
        try {
            CheckListManager cm = new CheckListManager();
            JSONObject membership = new JSONObject(Forum.getProjectMembership(conn, userid, projid));
            JSONObject userProjData = membership.getJSONArray("data").getJSONObject(0);
            int pp = userProjData.getInt("planpermission");
            boolean uT = (userProjData.getInt("connstatus") != 4 && (pp == 8 || pp == 16) && DashboardHandler.isSubscribed(conn, companyid, "usrtk"));
            if (uT) {
                // Fetch user specfic tasks
                returnStr = getUserTasks(conn, projid, userid, offset, limit);

            } else {
                returnStr = getTask(conn, projid, offset, limit);
            }
            if (returnStr.compareTo("{data:{}}") != 0) {
                com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject(returnStr);
                com.krawler.utils.json.base.JSONArray jArray = jobj.getJSONArray("data");
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject temp = jArray.getJSONObject(i);
                    if (convertTimezone) {
                        String sdtStr = temp.getString("startdate");
                        String edtStr = temp.getString("enddate");
                        String actsdt = temp.getString("actualstartdate");
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-d HH:mm:ss");
                        java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-d");
                        if (sdtStr.compareTo("") != 0) {
                            java.util.Date dt = sdf.parse(sdtStr);
                            sdtStr = sdf1.format(dt);
                            temp.remove("startdate");
                            temp.put("startdate", sdtStr);
                        }
                        if (edtStr.compareTo("") != 0) {
                            java.util.Date dt = sdf.parse(edtStr);
                            edtStr = sdf1.format(dt);
                            temp.remove("enddate");
                            temp.put("enddate", edtStr);
                        }
                        if (actsdt.compareTo("") != 0) {
                            java.util.Date dt = sdf.parse(actsdt);
                            actsdt = sdf1.format(dt);
                            temp.remove("actualstartdate");
                            temp.put("actstartdate", actsdt);
                        }
                    }
                    if (!uT) {
                        String predecessor = getPredecessor(conn, temp.getString("taskid"));
                        temp.put("predecessor", predecessor);
                    } else {
                        temp.put("predecessor", "");
                    }
                    String resources = getTaskResourcesOnDataload(conn, temp.getString("taskid"), projid);
                    temp.put("resourcename", resources);

                    String links = getLinks(conn, temp.getString("taskid"));
                    temp.put("links", links);

                    CheckList cl = cm.getAssociatedCheckList(conn, temp.getString("taskid"));
                    String clid = "";
                    if(cl != null)
                        clid = cl.getCheckListID();
                    temp.put("checklist", clid);
                }
                returnStr = jobj.toString();
            }
        }/* catch (SQLException ex) {
        Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);
        }*/ catch (ServiceException ex) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnStr;
    }

    public static StringBuilder getProjectHomeData(Connection conn, String userid, String projectid, int act) throws ServiceException, SQLException {
        DbResults rs = null;
        StringBuilder finalString = new StringBuilder();
        String updateStr = "";
        switch (act) {
            case 1:
                rs = DbUtil.executeQuery(conn, "SELECT announceval "
                        + "FROM userannouncements INNER JOIN announcements ON announcements.announceid = userannouncements.announceid "
                        + "WHERE userid = ? AND date(`from`) <= date(now()) AND date(`to`) >= date(now())", new Object[]{projectid});
                while (rs.next()) {
                    finalString.append(DashboardHandler.getContentDiv("announcements"));
                    finalString.append(rs.getString("announceval"));
                    finalString.append(DashboardHandler.getContentSpan(""));
                }
                break;
            case 2:
                updateStr = "<a href=\"#\" onclick=\"navigateSubtab('p','req', '%s');\">%s</a>";
                rs = DbUtil.executeQuery(conn, "SELECT concat(fname,' ',lname) AS uname "
                        + "FROM projectmembers INNER JOIN users ON users.userid=projectmembers.userid "
                        + "WHERE projectmembers.status=1 AND projectmembers.projectid = ?", new Object[]{projectid});
                while (rs.next()) {
                    finalString.append(DashboardHandler.getContentDiv("newtask"));
                    finalString.append(String.format(updateStr, new Object[]{projectid, rs.getString("uname")}));
                    finalString.append(DashboardHandler.getContentSpan(" has requested to join this project."));
                }
                break;
            case 3:
                finalString = getProjectTaskUpdates(conn, userid, projectid);
                break;
            case 4:
                updateStr = "<a href=\"#\" onclick=\"navigateSubtab('p','todo','%s');\">%s</a>";
                rs = DbUtil.executeQuery(conn, "SELECT * FROM todotask WHERE userid = ? AND parentId != '' AND assignedto=? AND status=0",
                        new Object[]{projectid, userid});
                while (rs.next()) {
                    String[] desc = rs.getString("description").split(":");
                    finalString.append(DashboardHandler.getContentDiv("duetodo"));
                    finalString.append(String.format(updateStr, new Object[]{projectid, desc[1]}));
                    finalString.append(DashboardHandler.getContentSpan("Pending to-do"));
                }
                break;
            case 5:
                updateStr = "<a href=\"#\" onclick=\"navigateSubtab('p','cal','%s');\">%s</a>";
                rs = DbUtil.executeQuery(conn, "SELECT datediff(date(startts), date(now())) as diff,eid,cid,startts,endts,subject,descr "
                        + "FROM calendarevents WHERE cid IN "
                        + "(SELECT cid FROM calendars WHERE userid = ?) AND date(startts) >= date(now()) ORDER BY startts LIMIT 5", new Object[]{projectid});
                while (rs.next()) {
                    finalString.append(DashboardHandler.getContentDiv("dueevent"));
                    finalString.append(String.format(updateStr, new Object[]{projectid, rs.getString("subject")}));
                    if (rs.getInt("diff") == 0) {
                        finalString.append(DashboardHandler.getContentSpan("Todays event"));
                    } else if (rs.getInt("diff") == 1) {
                        finalString.append(DashboardHandler.getContentSpan("Tomorrows event"));
                    } else {
                        finalString.append(DashboardHandler.getContentSpan("Event starting in " + Integer.toString(rs.getInt("diff")) + " days"));
                    }
                }
                break;
        }
        return finalString;
    }

    public static StringBuilder getProjectTaskUpdates(Connection conn, String userid, String projectid) throws ServiceException, SQLException {
        DbResults rs = null;
        StringBuilder fsb = new StringBuilder();
        StringBuilder sbDueNow = new StringBuilder();
        StringBuilder sbDueLater = new StringBuilder();
        StringBuilder sbNewNow = new StringBuilder();
        StringBuilder sbNewLater = new StringBuilder();
        StringBuilder sbOverdue = new StringBuilder();
        boolean isModerator = false;
        int status = 0;
        rs = DbUtil.executeQuery(conn, "SELECT status FROM projectmembers WHERE userid=? AND projectid=?", new Object[]{userid, projectid});
        if (rs.next()) {
            status = rs.getInt("status");
        }
        if (status >= 3) {
            String tids = "";
            rs = DbUtil.executeQuery(conn, "(SELECT taskid FROM proj_task WHERE projectid=? AND taskid NOT IN "
                    + "(SELECT taskid FROM proj_taskresourcemapping)) "
                    + "UNION "
                    + "(SELECT taskid FROM proj_taskresourcemapping WHERE resourceid = ?)", new Object[]{projectid, userid});
            while (rs.next()) {
                tids += "'" + rs.getString("taskid") + "',";
            }
            tids = tids.length() > 0 ? tids.substring(0, (tids.length() - 1)) : "";
            if (status == 4) {
                isModerator = true;
            }
            PreparedStatement pstmt = null;
            String taskName = null;
            String taskid = "";
            String taskStr = "<a href=\"#\" onclick=\"navigateSubtab('p','plan','%s','%s');\">%s</a>";
            int tasklength = 0;
            String limit = "5";
            String duein48 = "duetask";
            String newin48 = "newtask";
            String overdueTask = "overduetask";
            String SELECT_DUE_TASKS = "SELECT datediff(date(proj_task.enddate),date(now())) AS tasklength";
            String SELECT_NEW_TASKS = "SELECT datediff(date(proj_task.startdate),date(now())) AS tasklength";
            String SELECT_OVERDUE_TASKS = "SELECT datediff(date(now()),date(proj_task.enddate)) AS tasklength";
            String columns = ", taskname, percentcomplete AS complete, proj_task.taskid, taskindex";
            String FROM_ = "";
            String WHERE_ = "";
            String due_tasks_date_range = " AND (date(proj_task.enddate)>=date(now()))";
            String new_tasks_date_range = " AND (date(proj_task.startdate)>=date(now())) AND (date(proj_task.startdate) <= adddate(date(now()),7))";
            String overdue_date_range = " AND (date(proj_task.enddate) < date(now()) AND percentcomplete < 100)";
            String newOrderBy = " ORDER BY tasklength asc LIMIT " + limit;
            if (isModerator) {
                FROM_ = " FROM proj_task ";
                WHERE_ = " WHERE proj_task.projectid = ? AND taskid IN (" + tids + ")";
            } else {
                FROM_ = " FROM proj_task INNER JOIN proj_taskresourcemapping ON proj_task.taskid = proj_taskresourcemapping.taskid";
                WHERE_ = " WHERE proj_task.projectid = ? AND proj_taskresourcemapping.resourceid = ? AND proj_task.taskid IN (" + tids + ")";
            }
            pstmt = conn.prepareStatement(SELECT_DUE_TASKS + columns + FROM_ + WHERE_ + due_tasks_date_range + newOrderBy);
            pstmt.setString(1, projectid);
            if (!isModerator) {
                pstmt.setString(2, userid);
            }
            ResultSet rsForSubQ = pstmt.executeQuery();
            while (rsForSubQ.next()) {
                taskName = rsForSubQ.getString("taskname");
                taskid = rsForSubQ.getString("taskid");
                tasklength = Integer.parseInt(rsForSubQ.getObject("tasklength").toString());
                if (tasklength == 0 || tasklength == 1) {
                    sbDueNow.append(DashboardHandler.getContentDiv(duein48));
                    sbDueNow.append(String.format(taskStr, new Object[]{projectid, taskid, taskName}));
                    if (tasklength == 0) {
                        sbDueNow.append(DashboardHandler.getContentSpan("Due today"));
                    } else {
                        sbDueNow.append(DashboardHandler.getContentSpan("Due tomorrow"));
                    }
                } else if (tasklength > 1) {
                    sbDueLater.append(DashboardHandler.getContentDiv(duein48));
                    sbDueLater.append(String.format(taskStr, new Object[]{projectid, taskid, taskName}));
                    sbDueLater.append(DashboardHandler.getContentSpan("Due in " + rsForSubQ.getObject("tasklength").toString() + " days"));
                }
            }

            pstmt = conn.prepareStatement(SELECT_NEW_TASKS + columns + FROM_ + WHERE_ + new_tasks_date_range + newOrderBy);
            pstmt.setString(1, projectid);
            if (!isModerator) {
                pstmt.setString(2, userid);
            }
            rsForSubQ = pstmt.executeQuery();
            while (rsForSubQ.next()) {
                taskName = rsForSubQ.getString("taskname");
                taskid = rsForSubQ.getString("taskid");
                tasklength = Integer.parseInt(rsForSubQ.getObject("tasklength").toString());
                if (tasklength == 0 || tasklength == 1) {
                    sbNewNow.append(DashboardHandler.getContentDiv(newin48));
                    sbNewNow.append(String.format(taskStr, new Object[]{projectid, taskid, taskName}));
                    if (tasklength == 0) {
                        sbNewNow.append(DashboardHandler.getContentSpan("Starting today"));
                    } else {
                        sbNewNow.append(DashboardHandler.getContentSpan("Starting tomorrow"));
                    }
                } else if (tasklength > 1) {
                    sbNewLater.append(DashboardHandler.getContentDiv(newin48));
                    sbNewLater.append(String.format(taskStr, new Object[]{projectid, taskid, taskName}));
                    sbNewLater.append(DashboardHandler.getContentSpan("Starting in " + rsForSubQ.getObject("tasklength").toString() + " days"));
                }
            }
            pstmt = conn.prepareStatement(SELECT_OVERDUE_TASKS + columns + FROM_ + WHERE_ + overdue_date_range + newOrderBy);
            pstmt.setString(1, projectid);
            if (!isModerator) {
                pstmt.setString(2, userid);
            }
            rsForSubQ = pstmt.executeQuery();
            while (rsForSubQ.next()) {
                taskName = rsForSubQ.getString("taskname");
                taskid = rsForSubQ.getString("taskid");
                tasklength = Integer.parseInt(rsForSubQ.getObject("tasklength").toString());
                sbOverdue.append(DashboardHandler.getContentDiv(overdueTask));
                sbOverdue.append(String.format(taskStr, new Object[]{projectid, taskid, taskName}));
                if (tasklength == 1) {
                    sbOverdue.append(DashboardHandler.getContentSpan("Overdue by 1 day"));
                } else {
                    sbOverdue.append(DashboardHandler.getContentSpan("Overdue by " + rsForSubQ.getObject("tasklength").toString() + " days"));
                }
            }
        }
        fsb.append(sbNewNow);
        fsb.append(sbDueNow);
        fsb.append(sbNewLater);
        fsb.append(sbDueLater);
        fsb.append(sbOverdue);
        return fsb;
    }

    public static boolean isEmailSubscribe(Connection conn, String companyid) throws ServiceException {
        PreparedStatement pstmt = null;
        int ntype = 0;
        try {
            ResultSet rs = null;
            pstmt = conn.prepareStatement("select notificationtype from company where companyid = ?");
            pstmt.setString(1, companyid);
            rs = pstmt.executeQuery();
            rs.next();
            ntype = rs.getInt("notificationtype");
            pstmt = conn.prepareStatement("select typeid from notificationlist where typename = 'email'");
            rs = pstmt.executeQuery();
            rs.next();
            int actid = (int) Math.pow(2, rs.getInt("typeid"));
            if ((ntype & actid) == actid) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getNotificationtype", e);
        } catch (Exception e) {
            throw ServiceException.FAILURE("projdb.getNotificationtype", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

    public static String sendtaskNotification(Connection conn, HttpServletRequest request)
            throws ServiceException, SessionExpiredException, ParseException {
        String result = "true";
        String loginid = AuthHandler.getUserid(request);
        String companyid = AuthHandler.getCompanyid(request);
        boolean type = isEmailSubscribe(conn, companyid);
        int auditMode = 0;
        if (type) {
            String subdomain = CompanyHandler.getCompanySubdomainByCompanyID(conn, companyid);
            String reciver = "";
            String tid ="";
            int resourceCnt = 0;
            String projid = request.getParameter("pid");
            String[] taskid = request.getParameter("idstr").split(",");
            String[] assignarr = request.getParameter("assignidstr").split(",");
            List l = Arrays.asList(assignarr);
            ArrayList<String> as = new ArrayList<String>();
            HashSet<String> hs = new HashSet<String>();
            hs.addAll(l);
            as.addAll(hs);
            String taskquery = "Select proj_task.taskname,proj_task.enddate,proj_task.percentcomplete from proj_task inner join proj_taskresourcemapping on "
                    + "proj_taskresourcemapping.taskid = proj_task.taskid where proj_taskresourcemapping.taskid in (" + request.getParameter("idstr") + ") "
                    + "and proj_taskresourcemapping.resourceid = ? and proj_task.percentcomplete<100 "
                    + "and proj_taskresourcemapping.resourceid not in (select userlogin.userid from userlogin inner join users on users.userid = userlogin.userid where users.companyid = ? and userlogin.isactive = false)";
            String userquery = "select concat(fname,' ',lname) as username,emailid,users.userid,userlogin.username as reciver from users inner join userlogin on users.userid = userlogin.userid where users.userid = ?";
            String projectname = request.getParameter("pname");
            String resourceCountQry = " select count(*) as count from ("+userquery+") as tmp ";
            PreparedStatement pstmt = null;
            PreparedStatement pstmtCnt = null;
            java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                ResultSet rs = null;
                ResultSet rsCnt = null;
                pstmt = conn.prepareStatement(userquery);
                pstmt.setString(1, AuthHandler.getUserid(request));
                rs = pstmt.executeQuery();
                rs.next();
                String sendername = rs.getString("username");
//                    String sendermailid = rs.getString("emailid");
               // String sendermailid = KWLErrorMsgs.notificationSenderId;
                String sendermailid = CompanyHandler.getSysEmailIdByCompanyID(conn, companyid);
                rs.close();
                pstmt.close();
                String username = "";
                String mailid = "";
                String receiverid = "";
                for (int i = 0; i < as.size(); i++) {
                    pstmt = conn.prepareStatement(userquery);
                    pstmt.setString(1, as.get(i).toString());
                    rs = pstmt.executeQuery();
                    pstmtCnt = conn.prepareStatement(resourceCountQry);
                    pstmtCnt.setString(1, as.get(i).toString());
                    rsCnt = pstmtCnt.executeQuery();
                    if(rsCnt.next()){
                        int Cnt = rsCnt.getInt("count");
                        resourceCnt=resourceCnt+Cnt;
                    }
                    if (rs.next()) {
                        username = rs.getString("username");
                        mailid = rs.getString("emailid");
                        reciver = rs.getString("reciver");
                        receiverid = rs.getString("userid");
                        rs.close();
                        pstmt.close();
                        pstmt = conn.prepareStatement(taskquery);
                        pstmt.setString(1, as.get(i).toString());
                        pstmt.setString(2, companyid);
                        rs = pstmt.executeQuery();
                        String pmsg = String.format("Hi %s,\n\nYou have been assigned the following Tasks in the project: %s\n\nTasks:", username, projectname);

                        String htmlmsg = String.format("<html><head><title>Notification - Tasks for %s </title></head><style type='text/css'>"
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
                                + "		<p>Hi <strong>%s</strong>,</p>"
                                + "		<p>You have been assigned the following Tasks in the project: <b>%s</b></p>", projectname, username, projectname);

                        String htmlmsg1 = "";
                        String pmsg1 = "";
                        int count = 1;
                        String dateString = "";
                        String df = "E, MMM dd, yyyy";//dateFormatHandlers.getUserDateFormat(conn,as.get(i).toString());
                        while (rs.next()) {
                            dateString = rs.getString("enddate");
                            dateString = dateFormatHandlers.getUserPrefreanceDate(df, sdf1.parse(dateString));
                            //dateString = dateFormatHandlers.getUserPrefreanceDate(df,rs.getDate("enddate"));
                            String pecentComplete = "" + rs.getInt("percentcomplete") + "%";
                            pmsg1 = String.format("\n %s) %s (%s Complete)- Due by %s", count, rs.getString("taskname"), pecentComplete, dateString);
                            htmlmsg1 = String.format("<p> %s) %s (%s Complete)- Due by %s </p>", count++, rs.getString("taskname"), pecentComplete, dateString);
                            pmsg += pmsg1;
                            htmlmsg += htmlmsg1;
                        }
                        if (count > 1) {
                            try {
                                String uri = URLUtil.getPageURL(request, Links.loginpageFull, subdomain);
                                htmlmsg1 = String.format("<p>You can log in at:</p><p>%s</p><br/><p> - %s </p></div></body></html>",uri , sendername);
                                pmsg1 = String.format("\n\nYou can log in at:\n%s\n\n - %s ", uri, sendername);
                                String insertMsg = Mail.insertMailMsg(conn, reciver, loginid, "Task Notification", htmlmsg, "1", false, "1", "", "newmsg", "", 3, "", companyid);
                                pmsg += pmsg1;
                                htmlmsg += htmlmsg1;

                                SendMailHandler.postMail(new String[]{mailid},
                                        KWLErrorMsgs.taskNotificationSubject, htmlmsg, pmsg,
                                        sendermailid);
                            } catch (ConfigurationException e) {
                                result = "false";
                                e.printStackTrace();
                            } catch (MessagingException e) {
                                result = "false";
                                e.printStackTrace();
                            } catch (java.text.ParseException e) {
                                result = "false";
                                e.printStackTrace();
                            } catch (JSONException e) {
                                result = "false";
                                e.printStackTrace();
                            }
                        }
                        rs.close();
                        pstmt.close();
                        if (i == 0) {
                            result = username;
                        } else if (i == (as.size() - 1) && i != 0) {
                            result = result + " and " + username;
                        } else {
                            result = result + ", " + username;
                        }
                    }
                }
                    String userFullName = AuthHandler.getAuthor(conn, loginid);
                    String userName = AuthHandler.getUserName(request);
                    String params = userFullName + " (" + userName + "), " + resourceCnt +", project plan";
                    String ipAddress = AuthHandler.getIPAddress(request);
                    AuditTrail.insertLog(conn, "410", loginid, tid, projid, companyid, params, ipAddress, auditMode);
            } catch (SQLException e) {
                result = "false";
                throw ServiceException.FAILURE("todolist.getToDoDetails", e);
            } finally {
                DbPool.closeStatement(pstmt);
            }
        } else {
            result = "typeError";
        }
        return result;
    }

    public static String sendtaskNotificationToAll(Connection conn, HttpServletRequest request)
            throws ServiceException, SessionExpiredException, ParseException {
        String loginid = AuthHandler.getUserid(request);
        String result = "true";
        String companyid = AuthHandler.getCompanyid(request);
        int resourceCnt = 0;
        boolean type = isEmailSubscribe(conn, companyid);
        int auditMode=0;
        java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (type) {
            String subdomain = CompanyHandler.getCompanySubdomainByCompanyID(conn, companyid);
            String reciver = "";
            String pid = request.getParameter("pid");
            String resourcequery = "Select concat(fname,' ',lname) as username,emailid,users.userid,userlogin.username as reciver from users inner join proj_resources " +
                    "on proj_resources.resourceid = users.userid inner join userlogin on users.userid = userlogin.userid "
                    + " where proj_resources.projid = ? and proj_resources.resourceid not in (select userlogin.userid from userlogin "
                    + "inner join users on users.userid = userlogin.userid where users.companyid = ? and userlogin.isactive = false)";
            String resourceCountQry = " select count(*) as count from ("+resourcequery+") as tmp ";
            String taskquery = "Select proj_task.taskname,proj_task.enddate,proj_task.percentcomplete from proj_task inner join proj_taskresourcemapping on "
                    + "proj_taskresourcemapping.taskid = proj_task.taskid where proj_taskresourcemapping.taskid in (select taskid from proj_task where projectid = ?) "
                    + "and proj_taskresourcemapping.resourceid = ? and proj_task.percentcomplete<100 ";
            String userquery = "select concat(fname,' ',lname) as username,emailid,userid from users where userid = ?";
            String projectname = request.getParameter("pname");
            PreparedStatement pstmt = null;
            PreparedStatement pstmt1 = null;
            PreparedStatement pstmtCnt = null;
            String tid = "";
            String projid = request.getParameter("pid");
            try {
                ResultSet rs = null;
                ResultSet rs1 = null;
                ResultSet rsCnt = null;

                pstmt = conn.prepareStatement(userquery);
                pstmt.setString(1, AuthHandler.getUserid(request));
                rs = pstmt.executeQuery();
                rs.next();
                String sendername = rs.getString("username");
//                    String sendermailid = rs.getString("emailid");
               // String sendermailid = KWLErrorMsgs.notificationSenderId;
                String sendermailid = CompanyHandler.getSysEmailIdByCompanyID(conn, companyid);
                rs.close();
                pstmt.close();
                String username = "";
                String mailid = "";
                String receiverid = "";
                pstmt = conn.prepareStatement(resourcequery);
                pstmt.setString(1, request.getParameter("pid"));
                pstmt.setString(2, companyid);
                rs = pstmt.executeQuery();

                pstmtCnt = conn.prepareStatement(resourceCountQry);
                pstmtCnt.setString(1, request.getParameter("pid"));
                pstmtCnt.setString(2, companyid);
                rsCnt = pstmtCnt.executeQuery();
                if(rsCnt.next()){
                    resourceCnt = rsCnt.getInt("count");
                }

                while (rs.next()) {
                    username = rs.getString("username");
                    mailid = rs.getString("emailid");
                    reciver = rs.getString("reciver");
                    receiverid = rs.getString("userid");
                    pstmt1 = conn.prepareStatement(taskquery);
                    pstmt1.setString(1, pid);
                    pstmt1.setString(2, rs.getString("userid"));
                    rs1 = pstmt1.executeQuery();
                    String pmsg = String.format("Hi %s,\n\nYou have been assigned the following Tasks in the project: <b>%s</b>\n\nTasks:", username, projectname);

                    String htmlmsg = String.format("<html><head><title>Notification - Tasks for %s </title></head><style type='text/css'>"
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
                            + "		<p>Hi <strong>%s</strong>,</p>"
                            + "		<p>You have been assigned the following Tasks in the project: %s</p>", projectname, username, projectname);

                    String htmlmsg1 = "";
                    String pmsg1 = "";
                    int count = 1;
                    String dateString = "";
                    //String df =dateFormatHandlers.getUserDateFormat(conn,rs.getString("userid"));
                    String df = "E, MMM dd, yyyy";
                    while (rs1.next()) {
                        dateString = rs1.getString("enddate");
                        dateString = dateFormatHandlers.getUserPrefreanceDate(df, sdf1.parse(dateString));
                        //       dateString = dateFormatHandlers.getUserPrefreanceDate(df,rs1.getDate("enddate"));
                        String pecentComplete = "" + rs1.getInt("percentcomplete") + "%";
                        pmsg1 = String.format("\n %s) %s (%s Complete)- Due by %s", count, rs1.getString("taskname"), pecentComplete, dateString);
                        htmlmsg1 = String.format("<p> %s) %s (%s Complete)- Due by %s </p>", count++, rs1.getString("taskname"), pecentComplete, dateString);
                        pmsg += pmsg1;
                        htmlmsg += htmlmsg1;
                    }
                    if (count > 1) {
                        try {
                            String uri = URLUtil.getPageURL(request, com.krawler.esp.web.resource.Links.loginpageFull, subdomain);
                            htmlmsg1 = String.format("<p>You can log in at:</p><p>%s</p><br/><p>See you on Project Management!</p><p> - %s </p></div></body></html>", uri, sendername);
                            pmsg1 = String.format("\n\nYou can log in at:\n%s\n\nSee you on Project Management!\n\n - %s ", uri, sendername);
                            String insertMsg = Mail.insertMailMsg(conn, reciver, loginid, KWLErrorMsgs.taskNotificationSubject, htmlmsg, "1", false, "1", "", "newmsg", "", 3, "", companyid);
                            pmsg += pmsg1;

                            SendMailHandler.postMail(new String[]{mailid},
                                    KWLErrorMsgs.taskNotificationSubject, htmlmsg, pmsg,
                                    sendermailid);
                        } catch (ConfigurationException e) {
                            result = "false";
                            e.printStackTrace();
                        } catch (MessagingException e) {
                            result = "false";
                            e.printStackTrace();
                        } catch (java.text.ParseException e) {
                            result = "false";
                            e.printStackTrace();
                        } catch (JSONException e) {
                            result = "false";
                            e.printStackTrace();
                        }
                    }
                    rs1.close();
                    pstmt1.close();
                }
                            String userFullName = AuthHandler.getAuthor(conn, loginid);
                            String userName = AuthHandler.getUserName(request);
                            String params = userFullName + " (" + userName + "), " + resourceCnt+", project plan";
                            String ipAddress = AuthHandler.getIPAddress(request);
                            AuditTrail.insertLog(conn, "410", loginid, tid, projid, companyid, params, ipAddress, auditMode);
                rs.close();
            } catch (SQLException e) {
                result = "false";
                throw ServiceException.FAILURE("todolist.getToDoDetails", e);
            } catch (Exception e) {
                result = "false";
                throw ServiceException.FAILURE("todolist.getToDoDetails", e);
            } finally {
                DbPool.closeStatement(pstmt);
            }
        } else {
            result = "typeError";
        }
        return result;
    }

    public static int checkPredecessorNotification(Connection conn, String loginid, String taskid, int newPerComp)
            throws ServiceException {
        int result = -1;
        int cnt=0;
        int auditmode=0;
        java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String userid = "";
        try {
            String companyid = CompanyHandler.getCompanyByUser(conn, loginid);
            boolean isPCChanged = false;
            DbResults rsPC = DbUtil.executeQuery("SELECT percentcomplete FROM proj_task WHERE taskid = ?", taskid);
            if (rsPC.next()) {
                if (newPerComp != rsPC.getInt("percentcomplete") && newPerComp == 100) {
                    isPCChanged = true;
                }
            }
            if (isPCChanged) {
                if (isEmailSubscribe(conn, companyid)) {

                    String subdomain = CompanyHandler.getCompanySubdomainByCompanyID(conn, companyid);
                    String projectid = "", projectname = "";
                    DbResults rsProject = DbUtil.executeQuery(conn, "SELECT p.projectid, p.projectname FROM project p WHERE p.projectid = (SELECT projectid FROM proj_task WHERE taskid = ?)", taskid);
                    if (rsProject.next()) {
                        projectid = rsProject.getString("projectid");
                        projectname = rsProject.getString("projectname");
                    }

                    String res = getProjectResources(conn, projectid, 0, 100, "");
                    JSONObject resources = new JSONObject(res);
                    int length = resources.getJSONArray("data").length();
                    for (int i = 0; i < length; i++) {
                         userid = resources.getJSONArray("data").getJSONObject(i).getString("resourceID");
                        int type = resources.getJSONArray("data").getJSONObject(i).getJSONObject("type").getInt("typeID");
                        if (type == 1) {
                            if (ProfileHandler.isUserSubscribedToNotification(conn, userid)) {

                                if (ProfileHandler.isUserSubToNotifOnProjModules(conn, userid, projectid, 1)) {

                                    String senderFullName = AuthHandler.getAuthor(conn, loginid);
                                    String receiverUserName = AuthHandler.getUserName(conn, userid);
                                    String receiverMailID = DbUtil.executeQuery(conn, "SELECT emailid FROM users WHERE userid = ?", userid).getString("emailid");
                                    cnt=cnt+1;
                                 //   String senderMailID = KWLErrorMsgs.notificationSenderId;
                                    String senderMailID = CompanyHandler.getSysEmailIdByCompanyID(conn, companyid);
                                    String pmsg = String.format("Hi %s,\n\nThe task %s (Project: %s) has just been marked completed. "
                                            + "You may start the following dependent tasks - \n", receiverUserName, getTaskName(conn, taskid), projectname);

                                    String htmlmsg = String.format("<html><head><title>Notification - Tasks for %s </title></head><style type='text/css'>"
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
                                            + "		<p>Hi <strong>%s</strong>,</p>"
                                            + "		<p>The task <b>%s (Project: %s)</b> has just been marked completed. You may start the following dependant tasks -</p>",
                                            projectname, receiverUserName, getTaskName(conn, taskid), projectname);

                                    DbResults rsTask = DbUtil.executeQuery(conn, "SELECT pt.taskid, pt.taskname, pt.startdate, pt.enddate, pt.priority "
                                            + "FROM proj_task pt INNER JOIN proj_taskresourcemapping ptm ON pt.taskid = ptm.taskid "
                                            + "INNER JOIN proj_tasklinks ptl ON pt.taskid = ptl.totask "
                                            + "WHERE ptl.fromtask = ? AND pt.projectid = ? AND ptm.resourceid = ? AND pt.percentcomplete < 100",
                                            new Object[]{taskid, projectid, userid});

                                    String htmlmsg1 = "";
                                    String pmsg1 = "";
                                    int count = 1;
                                    String df = "E, MMM dd, yyyy";

                                    while (rsTask.next()) {
                                        String st = rsTask.getObject("startdate").toString();
                                        st = dateFormatHandlers.getUserPrefreanceDate(df, sdf1.parse(st));
                                        String et = rsTask.getObject("enddate").toString();
                                        et = dateFormatHandlers.getUserPrefreanceDate(df, sdf1.parse(et));

                                        String priority = "Low";
                                        int p = rsTask.getInt("priority");
                                        if (p == 2) {
                                            priority = "High";
                                        } else if (p == 1) {
                                            priority = "Moderate";
                                        }

                                        pmsg1 = String.format("\n %s) %s - \nStart Date - %s\nDue by - %s\nPriority - %s\n",
                                                count, rsTask.getString("taskname"), st, et, priority);
                                        htmlmsg1 = String.format("<p> %s) %s - <br><div style=\"margin-left:15px;\">Start Date - %s<br>Due by - %s<br>Priority - %s</div></p>",
                                                count++, rsTask.getString("taskname"), st, et, priority);
                                        pmsg += pmsg1;
                                        htmlmsg += htmlmsg1;
                                    }

                                    if (count > 1) {
                                        try {
                                            htmlmsg1 = String.format("<p>Please log in at %s to access your tasks.</p><br/> - %s </div></body></html>",
                                                    URLUtil.getDomainURL(subdomain, true), senderFullName);
                                            pmsg1 = String.format("\n\nPlease log in at %s to access your tasks.\n\n - %s ",
                                                    URLUtil.getDomainURL(subdomain, true), senderFullName);
                                            Mail.insertMailMsg(conn, receiverUserName, loginid, "Task Notification", htmlmsg, "1", false, "1", "", "newmsg", "", 3, "", companyid);
                                            pmsg += pmsg1;
                                            htmlmsg += htmlmsg1;
                                            SendMailHandler.postMail(new String[]{receiverMailID}, KWLErrorMsgs.taskNotificationSubject, htmlmsg, pmsg, senderMailID);
                                            result = 1;
                                        } catch (ConfigurationException e) {
                                            result = 0;
                                            e.printStackTrace();
                                        } catch (MessagingException e) {
                                            result = 0;
                                            e.printStackTrace();
                                        } catch (java.text.ParseException e) {
                                            result = 0;
                                            e.printStackTrace();
                                        } catch (JSONException e) {
                                            result = 0;
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }
                        String userFullName = AuthHandler.getAuthor(conn, loginid);
                        String userName = AuthHandler.getUserName(conn, loginid);
                        String params = userFullName + " (" + userName + "), "  + cnt +", project plan";
                        AuditTrail.insertLog(conn, "410", loginid, taskid, projectid, companyid, params, "", auditmode);
                } else {
                    result = 0;
                }
            } else {
                result = 0;
            }
        } catch (ParseException ex) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return result;
        }
    }

    public static void saveProjState(Connection conn, String userid, String projid, String featureid, String planview, String statevar) throws ServiceException {
        try {

            int fid = Integer.parseInt(featureid);
            PreparedStatement p = conn.prepareStatement("DELETE FROM userstatemap WHERE userid = ? AND featureid = ? AND projectid = ?");
            p.setString(1, userid);
            p.setInt(2, fid);
            p.setString(3, projid);
            p.executeUpdate();
            p = conn.prepareStatement("INSERT INTO userstatemap (userid, projectid, featureid, state,planview) "
                    + "VALUES (?,?,?,?,?)");
            p.setString(1, userid);
            p.setString(2, projid);
            p.setInt(3, fid);
            p.setString(4, statevar);
            p.setString(5, planview);
            p.executeUpdate();
        } catch (ServiceException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } catch (SQLException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }

    public static String getProjState(Connection conn, String userid, String projid, String featureid) throws ServiceException {
        String ret = "";
        try {
            JSONObject rObj = new JSONObject();
            PreparedStatement p = conn.prepareStatement("SELECT state, planview FROM userstatemap WHERE userid = ? AND projectid = ? AND featureid =?");
            p.setString(1, userid);
            p.setString(2, projid);
            p.setInt(3, Integer.parseInt(featureid));
            ResultSet r = p.executeQuery();
            String state = "\"none\"";
            String pview = "day";
            while (r.next()) {
                state = r.getString("state");
                pview = r.getString("planview");
            }
            rObj.put("data", state);
            rObj.put("planview", pview);
            rObj.put("valid", true);
            ret = rObj.toString();
        } catch (ServiceException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } catch (SQLException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return ret;
    }

    public static List getCompanyFeeds(Connection conn, String userid, String link)
            throws ServiceException {
        ArrayList list = new ArrayList();
        try {
            String projectlist = DashboardHandler.getProjectList(conn, userid, 1000, 0, "");
            JSONObject projListObj = new JSONObject(projectlist);
            JSONArray projList = projListObj.getJSONArray("data");
            for (int i = 0; i < projList.length(); i++) {
                JSONObject temp = projList.getJSONObject(i);
                String projid = temp.getString("id");

                List li = getProjectFeeds(conn, userid, projid, link);
                list.addAll(li);
            }
        } catch (ServiceException ex) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("projdb.getCompanyFeeds", ex);
        }
        return list;
    }

    public static List getProjectFeeds(Connection conn, String userid, String projid, String link)
            throws ServiceException {
        ArrayList list = new ArrayList();
        try {
            List li = getUserProjNewTask(conn, userid, projid, link); // new tasks
            list.addAll(li);

            li = getUserProjDueTask(conn, userid, projid, link); // due tasks
            list.addAll(li);

            li = Tree.getUserProjEvents(conn, userid, projid, link);// events
            list.addAll(li);

            li = getUserTodoFeeds(conn, projid, link); // todos
            list.addAll(li);
        } catch (Exception ex) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("projdb.getProjectFeeds", ex);
        }
        return list;
    }

    public static List getUserProjNewTask(Connection conn, String userid, String projid, String link)
            throws ServiceException {
        String projname = "";
        DbResults rsForSubQ = null;
        String SELECT_NEW_TASKS = "SELECT datediff(date(proj_task.startdate),date(now())) AS tasklength";
        String columns = " ,project.projectid pid, projectname AS name, taskname,proj_task.timestamp as publishdate , percentcomplete AS complete,taskid";
        String from_where = " FROM project INNER JOIN proj_task ON proj_task.projectid = project.projectid WHERE project.projectid = ?";
        String new_tasks_date_range = " AND (date(proj_task.startdate)>=date(now())) AND (date(proj_task.startdate) <= adddate(date(now()),7))";
        String newOrderBy = " ORDER BY tasklength ASC";
        ArrayList list = new ArrayList();
        try {
            boolean moderator = DashboardHandler.isModerator(conn, userid, projid);
            rsForSubQ = DbUtil.executeQuery(conn, SELECT_NEW_TASKS + columns + from_where + new_tasks_date_range + newOrderBy, new Object[]{projid});
            while (rsForSubQ.next()) {
                RssFeedItem feed = new RssFeedItem();
                feed.setLink(link);

                String taskName = rsForSubQ.getString("taskname");
                projname = rsForSubQ.getString("name");
                String title = "New Task - " + taskName + " : ";
                String description = "Project: " + projname;
                String resrs = "";
                int tasklength = Integer.parseInt(rsForSubQ.getObject("tasklength").toString());
                if (tasklength == 0 || tasklength == 1) {
                    if (tasklength == 0) {
                        resrs = "Starting today";
                    } else {
                        resrs = "Starting tomorrow";
                    }
                } else if (tasklength > 1) {
                    resrs = "Starting in " + rsForSubQ.getObject("tasklength").toString() + " days";
                }
                if (moderator) {
                    String r = getTaskResourcesNames(conn, rsForSubQ.getString("taskid"), projid);
                    if (!StringUtil.isNullOrEmpty(r)) {
                        resrs += " [Assigned to : " + r + "]";
                        description += " | Responsible : " + r;
                    }
                }
                title += resrs;
                feed.setTitle(title);
                String publishTime = rsForSubQ.getObject("publishdate").toString();
                feed.setPublishedDate(publishTime);
                feed.setDescription(description);
                list.add(feed);
            }
        } catch (ServiceException ex) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("projdb.getUserProjNewTask", ex);
        }
        return list;
    }

    public static List getUserProjDueTask(Connection conn, String userid, String projid, String link)
            throws ServiceException {
        String projname = "";
        DbResults rsForSubQ = null;
        String SELECT_DUE_TASKS = "SELECT datediff(date(proj_task.enddate),date(now())) AS tasklength";
        String columns = " ,project.projectid pid, projectname AS name, taskname, proj_task.timestamp as publishdate, percentcomplete AS complete,taskid";
        String from_where = " FROM project INNER JOIN proj_task ON proj_task.projectid = project.projectid WHERE project.projectid = ?";
        String due_tasks_date_range = " AND (date(proj_task.enddate)>=date(now())) AND (date(proj_task.enddate) <= adddate(date(now()),7))";
        String newOrderBy = " ORDER BY tasklength ASC";
        ArrayList list = new ArrayList();
        try {
            boolean moderator = DashboardHandler.isModerator(conn, userid, projid);
            rsForSubQ = DbUtil.executeQuery(conn, SELECT_DUE_TASKS + columns + from_where + due_tasks_date_range + newOrderBy, new Object[]{projid});
            while (rsForSubQ.next()) {
                RssFeedItem feed = new RssFeedItem();
                feed.setLink(link);
                String taskName = rsForSubQ.getString("taskname");
                projname = rsForSubQ.getString("name");
                String title = "Due Task - " + taskName + " : [" + rsForSubQ.getObject("complete").toString() + "%" + " Complete]";
                String description = "Project: " + projname;
                String resrs = "";
                int tasklength = Integer.parseInt(rsForSubQ.getObject("tasklength").toString());
                if (tasklength == 0 || tasklength == 1) {
                    if (tasklength == 0) {
                        resrs = " Due today";
                    } else {
                        resrs = " Due tomorrow";
                    }
                } else if (tasklength > 1) {
                    resrs = " Due in " + rsForSubQ.getObject("tasklength").toString() + " days";
                }
                if (moderator) {
                    String r = getTaskResourcesNames(conn, rsForSubQ.getString("taskid"), projid);
                    if (!StringUtil.isNullOrEmpty(r)) {
                        resrs += " [Assigned to : " + r + "]";
                        description += " | Responsible : " + r;
                    }
                }
                title += resrs;
                feed.setTitle(title);
                String publishTime = rsForSubQ.getObject("publishdate").toString();
                feed.setPublishedDate(publishTime);
                feed.setDescription(description);
                list.add(feed);
            }
        } catch (ServiceException ex) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("projdb.getUserProjDueTask", ex);
        }
        return list;
    }

//    public static List  getUserProjEvents(Connection conn, String userid,String projid,String link)
//                    throws ServiceException {
//        ArrayList list = new ArrayList();
//        PreparedStatement pstmt = null;
//        ResultSet rs = null;
//        int limit = 15;
//        int offset = 0;
//        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-d HH:mm:ss");
//        java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd h:mm a");
//        try {
//
//
//                String startts = "";
//                String endts = "";
//                pstmt = conn
//                                .prepareStatement(" select calendarevents.eid, project.projectname, concat(calendarevents.subject,' [Calendar : ',calendars.cname,']') as subject , " +
//                                " calendarevents.descr, calendarevents.location, calendarevents.startts, calendarevents.endts, calendarevents.timestamp, now() as dtstamp " +
//                                " from calendarevents inner join calendars on calendars.cid = calendarevents.cid inner join project on project.projectid = calendars.userid " +
//                                " where project.projectid in " +
//                                " ( select project.projectid from project inner join projectmembers on projectmembers.projectid = project.projectid where " +
//                                " projectmembers.inuseflag = 1 and projectmembers.status in (3,4,5) and userid = ? and project.projectid = ?) order by calendarevents.timestamp desc LIMIT ? OFFSET ?");
//                pstmt.setString(1, userid);
//                pstmt.setString(2, projid);
//                pstmt.setInt(3, limit);
//                pstmt.setInt(4, offset);
//                rs = pstmt.executeQuery();
//
//                while(rs.next())
//                {
//                    String timezone = "+00:00";
//                    timezone = Timezone.getTimeZone(conn, userid);
//                    timezone = timezone.substring(0,4) + "00";
//                    RssFeedItem feed = new RssFeedItem();
//                    feed.setLink(link);
//                    startts = rs.getString("startts");
//                    startts = Timezone.toUserDefTimezone(conn, startts, timezone);
//                    java.util.Date dt = sdf.parse(startts);
//                    startts = sdf1.format(dt);
//
//                    endts = rs.getString("endts");
//                    endts = Timezone.toUserDefTimezone(conn, endts, timezone);
//                    dt = sdf.parse(endts);
//                    endts = sdf1.format(dt);
//
//                    String title = "Event - "+rs.getString("subject");
//                    String description ="Project : "+rs.getString("projectname");
//                    if(rs.getString("descr").length() > 0)
//                        description +=" | Description : "+rs.getString("descr");
//                    description+= " | Start : "+startts + " | End : "+endts;
//                    feed.setTitle(title);
//                    String publishTime = rs.getObject("timestamp").toString();
//                    publishTime = Timezone.toUserDefTimezone(conn, publishTime, timezone);
//                    feed.setPublishedDate(publishTime);
//                    feed.setDescription(description);
//                    list.add(feed);
//                }
//        } catch (Exception ex) {
//                DbPool.closeStatement(pstmt);
//                Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);
//                throw ServiceException.FAILURE("projdb.getUserProjEvents", ex);
//        } finally {
//            return list;
//        }
//    }
    public static List getUserTodoFeeds(Connection conn, String uid, String link)
            throws ServiceException {
        ArrayList list = new ArrayList();
        PreparedStatement pstmt = null;
        int groupType = 2;
        int limit = 15;
        int offset = 0;
        String description = "";
        ResultSet rs = null;
        try {

            pstmt = conn.prepareStatement("select todotask.taskname,taskid,taskorder,status,parentId,assignedto,(select project.projectname from project where project.projectid = todotask.userid) as projectname,"
                    + " (select concat(fname,' ',lname) from users inner join userlogin on users.userid = userlogin.userid where users.userid = todotask.assignedto and userlogin.isactive = true) as username,todotask.timestamp from todotask "
                    + " where userid = ? and grouptype= ?  and  leafflag = true  LIMIT ? OFFSET ?");
            pstmt.setString(1, uid);
            pstmt.setInt(2, groupType);
            pstmt.setInt(3, limit);
            pstmt.setInt(4, offset);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                RssFeedItem feed = new RssFeedItem();
                feed.setLink(link);
                String title = "";
                if (rs.getInt("status") == 0) {
                    title = "ToDo added - ";
                } else {
                    title = "ToDo completed - ";
                }
                title += rs.getString("taskname");
                if (rs.getString("username") != null) {
                    description = "Project : " + rs.getString("projectname") + " | Responsible : " + rs.getString("username");
                } else {
                    description = "Project : " + rs.getString("projectname") + " | Responsible : None";
                }
                feed.setTitle(title);
                String publishTime = rs.getObject("timestamp").toString();
                feed.setPublishedDate(publishTime);
                feed.setDescription(description);
                list.add(feed);
            }
        } catch (Exception ex) {
            DbPool.closeStatement(pstmt);
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("projdb.getUserTodoFeeds", ex);
        } finally {
            return list;
        }
    }

    public static String setProjStatus(Connection conn, HttpServletRequest request) {
        String res = "";
        PreparedStatement pstmt = null;
        try {
            String act = request.getParameter("action");
            int archive = 0;
            String auditID = "327";
            String[] projID = request.getParameter("projid").split(",");
            if (StringUtil.equal(act, "Archive")) {
                archive = 1;
                auditID = "326";
            }
            for (int cnt = 0; cnt < projID.length; cnt++) {
                pstmt = conn.prepareStatement("UPDATE project SET archived = ? WHERE projectid = ?");
                pstmt.setInt(1, archive);
                pstmt.setString(2, projID[cnt]);
                pstmt.executeUpdate();

                String ipAddress = AuthHandler.getIPAddress(request);
                int auditMode = 0;
                String loginid = AuthHandler.getUserid(request);
                String companyid = AuthHandler.getCompanyid(request);
                String projName = projdb.getProjectName(conn, projID[cnt]);
                String params = AuthHandler.getAuthor(conn, loginid) + " ("
                        + AuthHandler.getUserName(request) + "), ";
                AuditTrail.insertLog(conn, auditID, loginid, projID[cnt], projID[cnt], companyid,
                        params + projName, ipAddress, auditMode);
            }
            res = "{\"success\":true,\"res\" :\"" + Integer.toString(archive) + "\"}";

            JSONObject jbj = new JSONObject();
            jbj.put("valid", "true");
            jbj.put("data", res);
            res = jbj.toString();

        } catch (SessionExpiredException ex) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);
            res = "{\"valid\": true}" + "{\"success\":false,\"res\" :\"Operation Failed.\"}";

        } catch (ServiceException ex) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);
            res = "{\"valid\": true}" + "{\"success\":false,\"res\" :\"Could not connect to Database.\"}";
        } catch (SQLException e) {
            res = "{\"valid\": true}" + "{\"success\":false,\"res\" :\"Operation Failed. Check your query.\"}";
        } catch (JSONException e) {
            res = "{\"valid\": true}" + "{\"success\":false,\"res\" :\"Operation Failed.\"}";
        }
        return res;
    }

    public static void insertimportedtaskResources(Connection conn, String taskids, String resourceid) {
        try {
            ResultSet rs = null;
            PreparedStatement pstmt = null;
            String[] taskidArray = taskids.split(",");
            for (int i = 0; i < taskidArray.length; i++) {
                int dur = 0;
                String tid = taskidArray[i].trim();
                pstmt = conn.prepareStatement("insert into proj_taskresourcemapping (taskid,resourceid,resduration) values(?,?,?)");
                pstmt.setString(1, tid);
                pstmt.setString(2, resourceid);
                pstmt.setInt(3, dur);
                pstmt.execute();
            }
        } catch (Exception e) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public static String getprojCompanyId(Connection conn, String projid) {
        String companyid = "";
        try {
            ResultSet rs = null;
            PreparedStatement pstmt = null;
            pstmt = conn.prepareStatement("select companyid from project where projectid = ?");
            pstmt.setString(1, projid);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                companyid = rs.getString("companyid");
            }
        } catch (Exception e) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            return companyid;
        }
    }

    public static String quickInsertTask(Connection conn, HttpServletRequest request) throws ServiceException {
        String res = "";
        try {
            PreparedStatement pstmt = conn.prepareStatement("SELECT max(taskindex) AS taskindex FROM proj_task where projectid = ?");
            pstmt.setString(1, request.getParameter("projid"));
            ResultSet rs = pstmt.executeQuery();
            String taskid = UUID.randomUUID().toString();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy/MM/dd");
            int index = 0;
            if (rs.next()) {
                index = rs.getInt("taskindex") + 1;
            }
            pstmt = conn.prepareStatement("INSERT INTO proj_task (taskindex, taskname, startdate, enddate, duration,projectid, taskid) VALUES (?,?,?,?,?,?,?)");
            pstmt.setInt(1, index);
            pstmt.setString(2, StringUtil.serverHTMLStripper(request.getParameter("name")));
            java.util.Date DateVal = sdf.parse(request.getParameter("startdate"));
            Timestamp ts = new Timestamp(DateVal.getTime());
            pstmt.setTimestamp(3, ts);
            DateVal = sdf.parse(request.getParameter("enddate"));
            ts = new Timestamp(DateVal.getTime());
            pstmt.setTimestamp(4, ts);
            pstmt.setString(5, request.getParameter("duration"));
            pstmt.setString(6, request.getParameter("projid"));
            pstmt.setString(7, taskid);
            int update = pstmt.executeUpdate();
            if (update > 0) {
                res = "{\"success\": true,\"data\":\"Task inserted successfully.\"}";
            }
        } catch (ParseException ex) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);
            res = "{\"success\": true,\"data\":\"Error occured while inserting a task.\"}";
            throw ServiceException.FAILURE("projdb.quickInsertTask", ex);
        } catch (SQLException ex) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);
            res = "{\"success\": true,\"data\":\"Error occured while inserting a task.\"}";
            throw ServiceException.FAILURE("projdb.quickInsertTask", ex);
        } catch (ServiceException e) {
            res = "{\"success\": true,\"data\":\"Error occured while inserting a task.\"}";
            throw ServiceException.FAILURE("projdb.quickInsertTask", e);
        }
        return res;
    }

    public static int getLastTaskIndex(Connection con, String projectid) {
        int index = 0;
        try {
            DbResults r = DbUtil.executeQuery(con, "SELECT max(taskindex) AS taskindex FROM proj_task WHERE projectid = ?", new Object[]{projectid});
            if (r.next()) {
                if (!r.isNull("taskindex")) {
                    index = r.getInt("taskindex");
                } else {
                    index = 0;
                }
            }
        } catch (ServiceException ex) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);
        }
        return index;
    }

    public static String getProjectListForChart(Connection conn, String ulogin) throws ServiceException {
        DbResults projListRS = null;
        String ProjectList = "";
        String SELECT_PROJECTS = "SELECT project.projectid as pid from projectmembers "
                + "INNER JOIN project ON projectmembers.projectid=project.projectid "
                + "WHERE userid = ? and userid not in (select userid from userlogin where isactive = false) and status in ( 3, 4, 5) and inuseflag = 1 and project.archived=0";
        try {
            projListRS = DbUtil.executeQuery(conn, SELECT_PROJECTS, new Object[]{ulogin});
            while (projListRS.next()) {
                ProjectList = ProjectList.concat(projListRS.getString("pid")).concat(",");
            }
        } catch (Exception ex) {
            Logger.getLogger(DashboardHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ProjectList;
    }

    public static String resourceConflict(Connection conn, HttpServletRequest request) throws ServiceException {
        PreparedStatement pstmt = null;
        String result = "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-d HH:mm:ss");
        java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-d");
        try {
            String pid = request.getParameter("pid");
            String userid = AuthHandler.getUserid(request);
            String[] projArray = getProjectListForChart(conn, AuthHandler.getUserid(request)).split(",");
            List projList = Arrays.asList(projArray);
            String cid = AuthHandler.getCompanyid(request);
            result = getTask(conn, pid, 0, -1);
            if (result.compareTo("{data:{}}") != 0) {
                JSONObject resjObj = new JSONObject();
                JSONObject jobj = new JSONObject(result);
                com.krawler.utils.json.base.JSONArray jArray = jobj.getJSONArray("data");
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject taskObj = jArray.getJSONObject(i);
                    String taskid = taskObj.getString("taskid");
                    String sdtStr = taskObj.getString("startdate");
                    String edtStr = taskObj.getString("enddate");
                    if (sdtStr.compareTo("") != 0 && edtStr.compareTo("") != 0) {
                        JSONObject jtemp = new JSONObject();
                        String qry = "select distinct(proj_task.taskid), proj_task.taskindex as taskindex,project.projectid,projectname,taskname,proj_task.startdate,proj_task.enddate,resourcename from proj_task "
                                + "INNER JOIN proj_taskresourcemapping ON proj_task.taskid = proj_taskresourcemapping.taskid "
                                + "INNER JOIN proj_resources ON proj_resources.resourceid = proj_taskresourcemapping.resourceid "
                                + "INNER JOIN project ON proj_task.projectid = project.projectid "
                                + "WHERE proj_task.projectid in(select projectid from project where companyid = ?) and proj_task.taskid !=? and "
                                + "proj_resources.resourcename in(select resourcename from proj_resources inner join proj_taskresourcemapping ON proj_resources.resourceid = proj_taskresourcemapping.resourceid where taskid = ?) "
                                + "and proj_resources.resourceid not in (select userlogin.userid from userlogin inner join users on users.userid = userlogin.userid where userlogin.isactive = false and users.companyid = ?)"
                                + "and ((proj_task.startdate >= ? and proj_task.startdate <=? ) or (proj_task.enddate >=? and proj_task.enddate<=? )) order by resourcename,taskindex";
                        pstmt = conn.prepareStatement(qry);
                        pstmt.setString(1, cid);
                        pstmt.setString(2, taskid);
                        pstmt.setString(3, taskid);
                        pstmt.setString(4, cid);
                        pstmt.setString(5, sdtStr);
                        pstmt.setString(6, edtStr);
                        pstmt.setString(7, sdtStr);
                        pstmt.setString(8, edtStr);
                        ResultSet rs = pstmt.executeQuery();
                        String nextRes = "";
                        while (rs.next()) {
                            if (rs.isFirst()) {
                                jtemp = new JSONObject();
                                jtemp.put("level", 0);
                                jtemp.put("taskindex", taskObj.getString("taskindex"));
                                jtemp.put("taskname", taskObj.getString("taskname"));
                                jtemp.put("startdate", sdf1.format(sdf.parse(sdtStr)));
                                jtemp.put("enddate", sdf1.format(sdf.parse(edtStr)));
                                resjObj.append("data", jtemp);
                            }
                            if (!nextRes.equals(rs.getString("resourcename"))) {
                                jtemp = new JSONObject();
                                jtemp.put("level", 1);
                                jtemp.put("taskname", rs.getString("resourcename"));
                                resjObj.append("data", jtemp);
                                nextRes = rs.getString("resourcename");
                            }
                            jtemp = new JSONObject();
                            jtemp.put("level", 2);
                            jtemp.put("taskindex", rs.getString("taskindex"));
                            if (projList.contains(rs.getString("projectid"))) {
                                jtemp.put("taskname", String.format("[%s] %s", rs.getString("projectname"), rs.getString("taskname")));
                            } else {
                                jtemp.put("taskname", String.format("[%s] %s", rs.getString("projectname"), "Access Denied"));
                            }
                            jtemp.put("startdate", sdf1.format(sdf.parse(rs.getString("startdate"))));
                            jtemp.put("enddate", sdf1.format(sdf.parse(rs.getString("enddate"))));
                            resjObj.append("data", jtemp);
                        }
                    }
                }
                result = resjObj.toString();
            }

        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.resourceConflict", e);
        } finally {
            DbPool.closeStatement(pstmt);
            return result;
        }
    }

    public static String getTaskConflict(Connection conn, HttpServletRequest request) throws ServiceException, SessionExpiredException {
        PreparedStatement pstmt = null;
        String json = "";
        try {
            ResultSet rs = null;
            String loginid = AuthHandler.getUserid(request);
            String projectid = request.getParameter("projectid");
            String cid = AuthHandler.getCompanyid(request);
            String resourceid = request.getParameter("resourceid");
            if (resourceid.equals("all")) {
                pstmt = conn.prepareStatement("SELECT taskid, taskindex, taskname, duration, startdate as startdate, enddate as enddate, "
                        + "parent, level, actualstartdate, actualduration, percentcomplete, notes, priority, isparent, workholidays "
                        + "FROM proj_task WHERE projectid = ? and startdate is not null ORDER BY taskindex");
                pstmt.setString(1, projectid);

            } else {
                pstmt = conn.prepareStatement("SELECT proj_task.taskid, taskindex, taskname, duration, startdate as startdate, enddate as enddate, "
                        + " parent, level, actualstartdate, actualduration, percentcomplete, notes, priority, isparent, workholidays "
                        + " FROM proj_task inner join  proj_taskresourcemapping as prm on proj_task.taskid=prm.taskid "
                        + " WHERE projectid = ? and prm.resourceid = ? and prm.resourceid not in "
                        + "(select users.userid from userlogin inner join users on users.userid = userlogin.userid where isactive = false and companyid = ?) ORDER BY taskindex");
                pstmt.setString(1, projectid);
                pstmt.setString(2, resourceid);
                pstmt.setString(3, cid);
            }
            rs = pstmt.executeQuery();
            com.krawler.utils.json.base.JSONObject resjObj = new JSONObject();
            int totalTasks = 0;
            while (rs.next()) {
                totalTasks++;
                com.krawler.utils.json.base.JSONObject jtemp = new JSONObject();
                String taskid = rs.getString("taskid");
                jtemp.put("taskid", taskid);
                jtemp.put("taskindex", rs.getString("taskindex"));
                jtemp.put("taskname", rs.getString("taskname"));
                String sdtStr = (rs.getString("startdate"));
                String edtStr = (rs.getString("enddate"));
                sdtStr = sdtStr.split(" ")[0];
                edtStr = edtStr.split(" ")[0];
                jtemp.put("startdate", sdtStr);
                jtemp.put("enddate", edtStr);
                String Conflicted = "{}";
                if (!resourceid.equals("all")) {
                    if (sdtStr.compareTo("") != 0 && edtStr.compareTo("") != 0) {
                        Conflicted = resourceTaskConflictInProject(conn, cid, resourceid, taskid, sdtStr, edtStr, loginid);
                    }
                }
//                JSONObject confltemp = new JSONObject(Conflicted);
                jtemp.put("conflictedtasks", Conflicted);
                String resources = getTaskResourcesOnDataload(conn, taskid, projectid);
                jtemp.put("resourcename", resources);
                resjObj.append("data", jtemp);
            }

            if (totalTasks == 0) {
                resjObj.put("data", "");
            }
            json = resjObj.toString();

        } catch (SessionExpiredException e) {
            throw ServiceException.FAILURE("projdb.getTaskConflict", e);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("projdb.getTaskConflict", e);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getTaskConflict", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return json;
    }

    public static String resourceTaskConflictInProject(Connection conn, String cid, String resourceid,
            String taskid, String sdtStr, String edtStr, String loginid) throws ServiceException {
        PreparedStatement pstmt = null;

        String result = "{}";

        try {
            com.krawler.utils.json.base.JSONObject jtemp = new JSONObject();
            com.krawler.utils.json.base.JSONObject resjObj = new JSONObject();
            sdtStr = sdtStr.split(" ")[0];
            edtStr = edtStr.split(" ")[0];
            String qry = "select proj_task.taskid,proj_task.taskindex,project.projectid,projectname,taskname,"
                    + " date(proj_task.startdate)as startdate,date(proj_task.enddate)as enddate,resourcename from proj_task "
                    + " INNER JOIN proj_taskresourcemapping ON proj_task.taskid = proj_taskresourcemapping.taskid "
                    + " INNER JOIN proj_resources ON proj_resources.resourceid = proj_taskresourcemapping.resourceid "
                    + " INNER JOIN project ON proj_task.projectid = project.projectid "
                    + " WHERE proj_task.projectid in(SELECT project.projectid as pid from projectmembers INNER JOIN project ON projectmembers.projectid=project.projectid WHERE status in (3, 4, 5) and inuseflag = 1 and project.archived=0 and project.companyid = ?) "
                    + " and proj_resources.resourceid not in (select users.userid from userlogin inner join users on users.userid = userlogin.userid where isactive = false and companyid = ?)"
                    + " and proj_task.taskid !=? and proj_resources.resourceid= ?"
                    + " and ((proj_task.startdate >= ? and proj_task.startdate <= ? ) or (proj_task.enddate >= ? and proj_task.enddate <= ? ) or (proj_task.startdate <= ? and proj_task.enddate >= ? )) group by taskid order by taskindex ";
            pstmt = conn.prepareStatement(qry);
            pstmt.setString(1, cid);
            pstmt.setString(2, cid);
            pstmt.setString(3, taskid);
            pstmt.setString(4, resourceid);
            pstmt.setString(5, sdtStr);
            pstmt.setString(6, edtStr);
            pstmt.setString(7, sdtStr);
            pstmt.setString(8, edtStr);
            pstmt.setString(9, sdtStr);
            pstmt.setString(10, edtStr);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                jtemp = new JSONObject();
                jtemp.put("taskid", rs.getString("taskid"));
                jtemp.put("taskindex", rs.getString("taskindex"));
                jtemp.put("taskname", rs.getString("taskname"));
                jtemp.put("startdate", rs.getString("startdate"));
                jtemp.put("enddate", rs.getString("enddate"));
                jtemp.put("projectname", rs.getString("projectname"));
                jtemp.put("projectid", rs.getString("projectid"));
                String resources = getTaskResourcesOnDataload(conn, rs.getString("taskid"), rs.getString("projectid"));
                jtemp.put("resourcename", resources);
                resjObj.append("data", jtemp);
            }
            result = resjObj.toString();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.resourceConflict", e);
        } finally {
            DbPool.closeStatement(pstmt);
            return result;
        }
    }

    public static String availableResourcesInProject(Connection conn, HttpServletRequest request) throws ServiceException {
        PreparedStatement pstmt = null;
        String result = "{}";
        try {
            String projectid = request.getParameter("projectid");
            String sdtStr = request.getParameter("startdate");
            String edtStr = request.getParameter("enddate");
            String taskid = request.getParameter("taskid");
            String cid = AuthHandler.getCompanyid(request);
            com.krawler.utils.json.base.JSONObject jtemp = new JSONObject();
            com.krawler.utils.json.base.JSONObject resjObj = new JSONObject();

            String qry = "SELECT distinct(projectmembers.userid),concat(users.fname,' ',users.lname)as username, proj_resources.resourcename as nickname, proj_resources.colorcode , proj_resources.inuseflag "
                    + " FROM projectmembers inner join users on projectmembers.userid=users.userid"
                    + " inner join proj_resources on proj_resources.resourceid = projectmembers.userid"
                    + " WHERE projectmembers.projectid= ?  and proj_resources.projid = ? and projectmembers.inuseflag='1' and projectmembers.status in (3, 4, 5)"
                    + " and  projectmembers.userid not in"
                    + " (SELECT distinct(projectmembers.userid) FROM projectmembers"
                    + " inner join proj_resources on proj_resources.resourceid = projectmembers.userid"
                    + " inner join proj_taskresourcemapping on proj_taskresourcemapping.resourceid = proj_resources.resourceid"
                    + " inner join proj_task on proj_task.taskid = proj_taskresourcemapping.taskid"
                    + " WHERE projectmembers.projectid=? and projectmembers.inuseflag='1' and projectmembers.status in (3, 4, 5) and proj_task.taskid != ?"
                    + " and ((proj_task.startdate >= ? and proj_task.startdate <= ?  )  or (proj_task.enddate >= ?  and proj_task.enddate <= ?  ) or (proj_task.startdate <= ?  and proj_task.enddate >= ? )))"
                    + " and proj_resources.resourceid not in (select userlogin.userid from userlogin inner join users on users.userid = userlogin.userid where userlogin.isactive = false and users.companyid = ?)";
            pstmt = conn.prepareStatement(qry);
            pstmt.setString(1, projectid);
            pstmt.setString(2, projectid);
            pstmt.setString(3, projectid);
            pstmt.setString(4, taskid);
            pstmt.setString(5, sdtStr);
            pstmt.setString(6, edtStr);
            pstmt.setString(7, sdtStr);
            pstmt.setString(8, edtStr);
            pstmt.setString(9, sdtStr);
            pstmt.setString(10, edtStr);
            pstmt.setString(11, cid);
            ResultSet rs = pstmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                count++;
                String name = "<span style='color: green;'>" + rs.getString("username") + "</span>";
                jtemp = new JSONObject();
                jtemp.put("resourceid", rs.getString("userid"));
                jtemp.put("resourcename", name);
                jtemp.put("nickname", rs.getString("nickname"));
                jtemp.put("colorcode", rs.getString("colorcode"));
                jtemp.put("inuseflag", rs.getString("inuseflag"));
                resjObj.append("data", jtemp);
            }

            if (count == 0) {
                resjObj.put("data", "");
            }
            result = resjObj.toString();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.availableResourcesInProject", e);
        } finally {
            DbPool.closeStatement(pstmt);
            return result;
        }
    }

    public static String setProgressRequest(Connection conn, HttpServletRequest request, String tid, String requestto, String userid) throws ServiceException {
        PreparedStatement pstmt = null;
        String result = "";
        String query = "";
        try {
            int rows = 0;
            String requestToIds[] = requestto.split(",");
            String taskid = tid;
            for (int i = 0; i < requestToIds.length; i++) {
                if (Forum.isInUse(conn, requestToIds[i])) {
                    JSONObject membership = new JSONObject(Forum.getProjectMembership(conn, requestToIds[i], request.getParameter("projectid")));
                    JSONObject userProjData = membership.getJSONArray("data").getJSONObject(0);
                    if (userProjData.getInt("planpermission") == 4) {
                        query = "INSERT INTO task_progressrequest (requestid, requestby, requestto, taskid, requestdate) VALUES (?,?,?,?,?)";
                        String requestid = UUID.randomUUID().toString();
                        java.sql.Timestamp timestamp = new java.sql.Timestamp(new java.util.Date().getTime());
                        pstmt = conn.prepareStatement(query);
                        pstmt.setString(1, requestid);
                        pstmt.setString(2, userid);
                        pstmt.setString(3, requestToIds[i]);
                        pstmt.setString(4, taskid);
                        pstmt.setTimestamp(5, timestamp);
                        rows += pstmt.executeUpdate();
                    }
                }
            }
            if (rows == requestToIds.length) {
                result = "success";
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.setProgressRequest", e);
        } finally {
            DbPool.closeStatement(pstmt);
            return result;
        }
    }

    public static String deleteProgressRequest(Connection conn, String taskid, String userid) throws ServiceException {
        PreparedStatement pstmt = null;
        String result = "";
        String query = "";
        try {
            ResultSet rs = null;
            query = "select requestid from task_progressrequest where taskid=? and requestto=?";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, taskid);
            pstmt.setString(2, userid);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                query = "delete from task_progressrequest where requestid=?";
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, rs.getString("requestid"));
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.setProgressRequest", e);
        } finally {
            conn.commit();
            DbPool.closeStatement(pstmt);
            return result;
        }
    }

    public static String createTeam(Connection conn, HttpServletRequest request) throws ServiceException {
        PreparedStatement pstmt = null;
        String query = "";
        String teamid = "";
        try {
            teamid = UUID.randomUUID().toString();
            query = "insert into team(teamid, teamname, description, companyid) values (?, ?, ?, ?)";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, teamid);
            pstmt.setString(2, request.getParameter("teamname"));
            pstmt.setString(3, request.getParameter("desc"));
            pstmt.setString(4, AuthHandler.getCompanyid(request, true));
            pstmt.execute();

            query = "insert into teammembers values (?,?)";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, teamid);
            String[] resid = request.getParameter("resids").toString().split(",");
            for (int i = 0; i < resid.length; i++) {
                pstmt.setString(2, resid[i]);
                pstmt.execute();
            }
        } catch (SQLException e) {
            teamid = "";
            throw ServiceException.FAILURE("projdb.createTeam", e);
        } catch (Exception e) {
            teamid = "";
            throw ServiceException.FAILURE("projdb.createTeam", e);
        } finally {
            DbPool.closeStatement(pstmt);
            return teamid;
        }
    }

    public static String getTeamMembers(Connection conn, String teamid) throws ServiceException {
        PreparedStatement pstmt = null;
        String res = "";
        try {
            int count = 0;
            ResultSet rs = null;
            pstmt = conn.prepareStatement("select * from teammembers where teamid = ?");
            pstmt.setString(1, teamid);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                res += rs.getString("resourceid") + ",";
                count++;
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getTeamMembers", e);
        } finally {
            DbPool.closeStatement(pstmt);
            return res;
        }
    }

    public static boolean insertResourceAsTeamMember(Connection conn, String resid, String projid, String resourcename,
            boolean billable, int stdrate, int typeid, int wuvalue, int categoryid, String colorcode) throws ServiceException {
        PreparedStatement pstmt = null;
        boolean b = false;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement("INSERT INTO proj_resources (resourceid, resourcename, projid, "
                    + "stdrate, typeid, categoryid, wuvalue, colorcode) VALUES (?,?,?,?,?,?,?,?)");
            pstmt.setString(1, resid);
            pstmt.setString(2, resourcename);
            pstmt.setString(3, projid);
            pstmt.setInt(4, stdrate);
            pstmt.setInt(5, typeid);
            pstmt.setInt(6, categoryid);
            pstmt.setInt(7, wuvalue);
            pstmt.setString(8, colorcode);
            int res = pstmt.executeUpdate();
            if (res == 1) {
                b = true;
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getResourceType", e);
        } finally {
            DbPool.closeStatement(pstmt);
            return b;
        }
    }

    public static void assignTeam(Connection conn, HttpServletRequest request, String teamid, String projects) throws ServiceException {
        PreparedStatement pstmt = null;
        try {
            int count = 0;
            String[] res = getTeamMembers(conn, teamid).split(",");
            String[] projid = projects.split(",");
            count = res.length;

            for (int j = 0; j < projid.length; j++) {
                String projectid = projid[j];
                for (int i = 0; i < count; i++) {
                    String resid = res[i];
                    DbResults rsRes = DbUtil.executeQuery(conn, "select typeid, categoryid from proj_resources where resourceid = ? and projid = ?",
                            new Object[]{resid, projectid});
                    if (rsRes.next()) {
                        int resType = rsRes.getInt("typeid");
                        int rescategory = rsRes.getInt("categoryid");
                        if (resType == Constants.WORK_RESOURCE && rescategory == 1) { // is a member in this project?
                            String[] projs = {projectid};
                            String[] users = {resid};
                            AdminServlet.addUsersOntoProjects(conn, request, projs, users, "0");
                        } else { // not a member type
                            DbUtil.executeUpdate(conn, "UPDATE proj_resources SET inuseflag = 1 WHERE resourceid = ? AND projid = ?",
                                    new Object[]{resid, projectid});
                        }
                    } else {
                        rsRes = DbUtil.executeQuery(conn, "select * from proj_resources where resourceid = ? group by resourceid", resid);
                        if (rsRes.next()) {
                            int resType = rsRes.getInt("typeid");
                            int rescategory = rsRes.getInt("categoryid");
                            if (resType == Constants.WORK_RESOURCE && rescategory == 1) { // is a member in this project?
                                String[] projs = {projectid};
                                String[] users = {resid};
                                AdminServlet.addUsersOntoProjects(conn, request, projs, users, "0");
                            } else { // not a member type
                                insertResourceAsTeamMember(conn, resid, projectid, rsRes.getString("resourcename"), rsRes.getBoolean("billable"), rsRes.getInt("stdrate"), rsRes.getInt("typeid"), rsRes.getInt("wuvalue"), rsRes.getInt("categoryid"), "#FF0000");
                            }
                        }
                    }
                }
            }
            for (int j = 0; j < projid.length; j++) {
                String query = "insert into team_projectmapping (teamid, projectid) values (?,?)";
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, teamid);
                pstmt.setString(2, projid[j]);
                pstmt.execute();
            }
        } catch (SessionExpiredException e) {
            throw ServiceException.FAILURE("projdb.assignTeam", e);
        } catch (Exception e) {
            throw ServiceException.FAILURE("projdb.assignTeam", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

    public static String getAllTeams(Connection conn, HttpServletRequest request) throws ServiceException, JSONException {
        PreparedStatement pstmt = null;
        String query = "";
        ResultSet rs = null;
        try {
            int count = 0, rows = 0;
            query = "select * from team where companyid = ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, AuthHandler.getCompanyid(request, true));
            rs = pstmt.executeQuery();
            KWLJsonConverter kjc = new KWLJsonConverter();
            query = kjc.GetJsonForGrid(rs);
            query = query.substring(1);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.createTeam", e);
        } finally {
            DbPool.closeStatement(pstmt);
            return query;
        }
    }

    public static boolean isTask(Connection conn, String eventid) throws ServiceException {
        PreparedStatement pstmt = null;
        String query = "";
        ResultSet rs = null;
        try {
            query = "select 1 from proj_task where taskid = ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, eventid);
            rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.isTask", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

    public static String getProjectWorkTime(Connection conn, String projectid) throws ServiceException {
        PreparedStatement pstmt = null;
        String query = "";
        ResultSet rs = null;
        try {
            query = "select intime, outtime from proj_workweek where projectid = ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, projectid);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                query = rs.getString("intime") + ",";
                query += rs.getString("outtime");
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.getProjectWorkTime", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return query;
    }

    public static String getProjectLevelUpdates(Connection conn, String projectid, String loginid, String companyid, int l, int o) throws ServiceException {
        ResultSet rs = null;
        ResultSet rs2 = null;
        PreparedStatement pstmt = null;
        JSONObject j = null;
        JSONObject jo = new JSONObject();
        JSONArray jA = new JSONArray();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Date d = new java.util.Date();
        int count = 0;
        int mod = 3;
        try {
            CompanyDAO cd = new CompanyDAOImpl();
            Locale locale = cd.getCompanyLocale(conn, companyid);
            boolean sub = DashboardHandler.isSubscribed(conn, companyid, "usrtk");
            JSONObject membership = new JSONObject(Forum.getProjectMembership(conn, loginid, projectid));
            if (!membership.toString().equals("{\"data\":{}}")) {
                JSONObject userProjData = membership.getJSONArray("data").getJSONObject(0);
                int pp = userProjData.getInt("planpermission");
                boolean uT = (userProjData.getInt("connstatus") != 4 && (pp == 8 || pp == 10 || pp == 16) && sub);
                String ut = "";
                if (userProjData.getInt("connstatus") == 4) {
                    mod = 4;
                } else if (uT) {
                    mod = 6;
                    ut = getUserTasks(conn, projectid, loginid, 0, 1000);
                }
                pstmt = conn.prepareStatement("select COUNT(*) AS count from actionlog where projectid = ?");
                pstmt.setString(1, projectid);
                rs = pstmt.executeQuery();
                if (rs.next() && mod != 6) {
                    count = rs.getInt("count");
                }
                if (count > (l * 25)) {
                    count = l * 25;
                }
                String query = "select logid, actionid, actionon, actionby, params, timestamp from actionlog where projectid = ? ORDER BY timestamp DESC limit ? offset ?";
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, projectid);
                pstmt.setInt(2, l);
                pstmt.setInt(3, o);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    boolean flag = false;
                    String useraction = "";
                    if (mod == 6) {
                        if (ut.contains(rs.getString("actionon"))) {
                            flag = true;
                        }
                    }
                    if ((mod == 6 && flag == true) || mod != 6) {
                        if (mod == 6) {
                            count++;
                        }
                        String params = rs.getString("params");
                        String userparam[] = params.split(",");
                        String query2 = "select textkey from actions where actionid = ?";
                        pstmt = conn.prepareStatement(query2);
                        pstmt.setString(1, rs.getString("actionid"));
                        rs2 = pstmt.executeQuery();
                        Object[] strParam = new Object[userparam.length];
                        System.arraycopy(userparam, 0, strParam, 0, userparam.length);
                        rs2.next();
                        String action = rs2.getString("textkey");
                        useraction = MessageSourceProxy.getMessage(action, strParam,locale);
                        String postTime = Timezone.toCompanyTimezone(conn, rs.getString("timestamp"), companyid);
                        postTime = Timezone.dateTimeRenderer(conn, Timezone.toCompanyTimezone(conn, sdf.format(d), companyid), postTime, companyid);
                        postTime = Timezone.convertToUserPref(conn, postTime, loginid);
                        String temp = "<img src=\"../../images/bullet2.gif\" style=\"float:left;margin-right:5px;\">";
                        useraction = temp + useraction;
                        useraction += "  <br><div style='color:#A1A1A1;margin-left:20px;margin-bottom:5px;font-size:11px;'>" + postTime + "</div>";
                        j = new JSONObject();
                        j.put("update", useraction);
                        jA.put(j);
                    }
                }
            }
            jo.put("data", jA);
            jo.put("count", count);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("DashboardHandler.getWidgetpdates", ex);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("DashboardHandler.getWidgetpdates", e);
        }
        return jo.toString();
    }

    public static JSONObject addUserOnCRMProject(HttpServletRequest request) throws ServiceException {
        JSONObject ret = new JSONObject();
        try {
            String userid = AuthHandler.getUserid(request);
            String nickname = request.getParameter("nickname");
            DbResults rs = DbUtil.executeQuery("SELECT * from project where nickname = ?", new Object[]{nickname});
            if (rs.next()) {
                JSONObject jobj = new JSONObject();
                JSONObject perm = new JSONObject();
                perm.put("id1", userid);
                perm.put("connstatus", 1);
                perm.put("archived", false);
                perm.put("planpermission", 2);
                JSONObject jo = rs.toJSONObject(1);
                String dt = jo.getString("startdate");
                jo.remove("startdate");
                jo.put("startdate", dt.substring(0, dt.length() - 2));
                dt = jo.getString("createdon");
                jo.remove("createdon");
                jo.put("createdon", dt.substring(0, dt.length() - 2));
                jobj.put("data", jo.toString());
                JSONArray ja = new JSONArray();
                ja.put(perm);
                jo = new JSONObject();
                jo.put("data", ja);
                jobj.put("perm", jo.toString());
                ret = jobj;
            }
        } catch (SessionExpiredException ex) {
            Logger.getLogger(projdb.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("DashboardHandler.addUserOnCRMProject : " + ex.getMessage(), ex);
        }
        return ret;
    }

    public static String updateNotificationStatus(Connection conn, String projectid, String userid, int val) throws ServiceException {
        String ret = "failure";
        int cnt = DbUtil.executeUpdate(conn, "update projectmembers set notification_subscription = ? where userid = ? and projectid = ?",
                new Object[]{val, userid, projectid});
        if (cnt == 1) {
            ret = "success";
        }
        return ret;
    }

    public static String getMileStonesForChart(Connection conn, String projList, String userid, String companyid) throws ServiceException {
        double pixel = 5;
        int fixedLength = 900;
        int length = 0;
        JSONObject newObj = null;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            boolean sub = DashboardHandler.isSubscribed(conn, companyid, "usrtk");
            Date stackStartDate = null;
            String ssd = "", sed = "";
            String[] projects = projList.split(",");
            newObj = new JSONObject();
            JSONObject resobj = new JSONObject();
            JSONObject j = new JSONObject();
            JSONObject temp = new JSONObject();

            DbResults rs = DbUtil.executeQuery(conn, "SELECT DATEDIFF( DATE_FORMAT( DATE_ADD(curdate(), INTERVAL 6 MONTH),'%Y-%m-01 %T'),DATE_FORMAT(curdate(), '%Y-%m-01 %T')) * ? AS length,"
                    + " DATE_FORMAT( DATE_ADD(curdate(), INTERVAL 6 MONTH),'%Y-%m-01 %T') as stackFinishDate,"
                    + " DATE_FORMAT(curdate(), '%Y-%m-01 %T') as stackStartDate", pixel);
            if (rs.next()) {
                length = rs.getInt("length");
                ssd = rs.getString("stackStartDate");
                sed = rs.getString("stackFinishDate");
            }
            stackStartDate = sdf.parse(ssd);
            Date stackFinishDate = sdf.parse(sed);
            Calendar c = Calendar.getInstance();

//            String systemTZ = Timezone.getSystemTimezone(conn);
//            String userTZ = Timezone.getUserCompanyTimeZoneDifference(conn, userid);

            for (int cnt = 0; cnt < projects.length; cnt++) {

                j = new JSONObject();
                String projectid = projects[cnt];
                JSONObject membership = new JSONObject(Forum.getProjectMembership(conn, userid, projectid));
                JSONObject userProjData = membership.getJSONArray("data").getJSONObject(0);
                int pp = userProjData.getInt("planpermission");
                boolean uT = (userProjData.getInt("connstatus") != 4 && (pp == 8 || pp == 16) && sub);
                int mode = 3;
                if (userProjData.getInt("connstatus") == 4) {
                    mode = 4;
                } else if (uT) {
                    mode = 6;
                }

                JSONArray jArray = new JSONArray();
                String projdate[] = getProjectDatesForReport(conn, projectid, userid).split(",");
                String sd = "";
                if (projdate.length != 3) {
                    sd = projdate[0];
                } else {
                    sd = projdate[2];
                }
//                sd = Timezone.toCompanyTimezone(conn, sd, userid);
                String ed = projdate[1];

                stackStartDate = sdf.parse(sd);
                stackFinishDate = sdf.parse(ed);

                ssd = sdf.format(stackStartDate);
                sed = sdf.format(stackFinishDate);

                int days = SchedulingUtilities.getDaysDiff(stackFinishDate, stackStartDate);
                if(days <= 0)
                    days = 1;
                pixel = (double) fixedLength / days;
                length = fixedLength;

//                String query = "SELECT taskindex, taskname, startdate, "
//                        + " DATEDIFF(DATE_FORMAT(CONVERT_TZ(startdate, ?, ?),'%Y-%m-%d %H:%i:%S'), DATE_FORMAT(curdate(), '%Y-%m-01 %T')) * ? AS position"
//                        + " FROM proj_task WHERE duration IN ('0', '0.0', '0.0d', '0.00h') AND projectid = ?"
//                        + " AND CONVERT_TZ(startdate, ?, ?) BETWEEN DATE_FORMAT(curdate(), '%Y-%m-01 %T') AND DATE_ADD(DATE_FORMAT(curdate(), '%Y-%m-01 %T'), INTERVAL 6 MONTH)"
//                        + " ORDER BY position";
                String query = "SELECT taskindex, taskname, DATE_FORMAT(startdate,'%Y-%m-%d %H:%i:%S') as startdate, "
                        + " DATEDIFF(DATE_FORMAT(startdate,'%Y-%m-%d %H:%i:%S'), ?) * ? AS position, percentcomplete"
                        + " FROM proj_task WHERE duration IN ('0', '0.0', '0.0d', '0.00h') AND projectid = ?"
                        + " ORDER BY position";
                Object[] obj = new Object[]{ssd, pixel, projectid};
                if (mode == 6) {
//                    query = "SELECT taskindex, taskname, duration, startdate, "
//                            + " DATEDIFF(DATE_FORMAT(CONVERT_TZ(startdate, ?, ?),'%Y-%m-%d %H:%i:%S'),DATE_FORMAT(curdate(), '%Y-%m-01 %T')) * ? AS position"
//                            + " FROM proj_task INNER JOIN proj_taskresourcemapping ptm ON proj_task.taskid = ptm.taskid"
//                            + " WHERE duration IN ('0', '0.0', '0.0d', '0.00h') AND proj_task.projectid = ? AND ptm.resourceid = ?"
//                            + " AND CONVERT_TZ(startdate, ?, ?) BETWEEN DATE_FORMAT(curdate(), '%Y-%m-01 %T') AND DATE_ADD(DATE_FORMAT(curdate(), '%Y-%m-01 %T'), INTERVAL 6 MONTH)"
//                            + " ORDER BY position";
                    query = "SELECT taskindex, taskname, duration, DATE_FORMAT(startdate,'%Y-%m-%d %H:%i:%S') as startdate , "
                            + " DATEDIFF(DATE_FORMAT(startdate,'%Y-%m-%d %H:%i:%S'), ?) * ? AS position, percentcomplete"
                            + " FROM proj_task INNER JOIN proj_taskresourcemapping ptm ON proj_task.taskid = ptm.taskid"
                            + " WHERE duration IN ('0', '0.0', '0.0d', '0.00h') AND proj_task.projectid = ? AND ptm.resourceid = ?"
                            + " ORDER BY position";
                    obj = new Object[]{ssd, pixel, projectid, userid};
                }
                rs = DbUtil.executeQuery(conn, query, obj);
                while (rs.next()) {
                    JSONObject jobj = new JSONObject();
                    jobj.put("position", rs.getInt("position"));
                    jobj.put("index", rs.getInt("taskindex"));
                    jobj.put("name", rs.getString("taskname"));
                    String stdate = rs.getObject("startdate").toString();
                    jobj.put("startdate", stdate);
                    int pc = rs.getInt("percentcomplete");
                    jobj.put("progress", pc);
                    String img = "";
                    if(sdf.parse(stdate).before(new Date()) && pc != 100)
                        img = "_red";
                    else if(pc == 100)
                        img = "_green";
                    jobj.put("image", img);
                    jArray.put(jobj);
                }

                double monthMarkerPosition = 0;

//                Date projectEndDate = sdf.parse(ed);
//                Date projectStartDate = sdf.parse(sd);

                JSONObject projectID = new JSONObject();
                projectID.put("milestones", jArray);

//                if (projectEndDate.after(stackStartDate) && projectEndDate.before(stackFinishDate)) {
//                    int diff = getDaysDiff(ssd, ed);
//                    monthMarkerPosition = diff * pixel;
//                    projectID.put("end", ed);
//                    projectID.put("endmarker", monthMarkerPosition);
//                }
//
//                if (projectStartDate.after(stackStartDate) && projectStartDate.before(stackFinishDate)) {
//                    int diff = getDaysDiff(ssd, sd);
//                    monthMarkerPosition = diff * pixel;
//                    projectID.put("start", sd);
//                    projectID.put("startmarker", monthMarkerPosition);
//                }
//
//                if(!projectID.has("end") && !projectID.has("start")){
//                    if(projectStartDate.before(stackStartDate) && projectEndDate.after(stackFinishDate))
//                        projectID.put("ongoing", true);
//                    else
//                        projectID.put("ongoing", false);
//                }
                int months_Between = monthsBetween(stackFinishDate, stackStartDate);
                int years_Between = yearsBetween(stackFinishDate, stackStartDate);
                projectID.put("ongoing", true);

                c.setTime(stackStartDate);
                monthMarkerPosition = 0;

                if(months_Between > 12 && months_Between <= 48){ // Project is more than 1 year to 4 year long
                    Calendar cal_temp = Calendar.getInstance();
                    cal_temp.setTime(stackStartDate);
                    for (int i = 0; i < (months_Between / 6); i++) {
                        JSONObject o = new JSONObject();
                        int days_in_month = c.getActualMaximum(Calendar.DAY_OF_MONTH);
                        if(i == 0){
                            int days_to_be_added = cal_temp.getActualMaximum(Calendar.DATE) - cal_temp.get(Calendar.DATE);
                            cal_temp.add(Calendar.DATE, days_to_be_added + 1);
                        }
                        cal_temp.add(Calendar.MONTH, 6);
                        days_in_month = SchedulingUtilities.getDaysDiff(cal_temp.getTime(), c.getTime());
                        c.setTime(cal_temp.getTime());
                        monthMarkerPosition += days_in_month * pixel;
                        o.put("position", (int) monthMarkerPosition);
                        o.put("month", new java.text.SimpleDateFormat("MMM''yy").format(c.getTime()));
                        j.put(Integer.toString(i), o);
                    }
                    j.put("months_between", (months_Between / 6));
                } else if(months_Between <= 12){ // Project is less than a year
                    for (int i = 0; i < months_Between; i++) {
                        JSONObject o = new JSONObject();
                        int days_in_month = c.getActualMaximum(Calendar.DAY_OF_MONTH);
                        if(i == 0){
                            days_in_month = c.getActualMaximum(Calendar.DATE) - c.get(Calendar.DATE);
                        }
                        monthMarkerPosition += days_in_month * pixel;
                        o.put("position", (int) monthMarkerPosition);
                        c.add(Calendar.MONTH, 1);
                        o.put("month", new java.text.SimpleDateFormat("MMM").format(c.getTime()));
                        j.put(Integer.toString(i), o);
                    }
                    j.put("months_between", months_Between);
                } else { // Project is more than 4 year long
                    Calendar cal_temp = Calendar.getInstance();
                    cal_temp.setTime(stackStartDate);
                    for (int i = 0; i < years_Between; i++) {
                        JSONObject o = new JSONObject();
                        int days_in_month = c.getActualMaximum(Calendar.DAY_OF_YEAR);
                        if(i == 0){
                            int days_to_be_added = cal_temp.getActualMaximum(Calendar.DAY_OF_YEAR) - cal_temp.get(Calendar.DAY_OF_YEAR);
                            cal_temp.add(Calendar.DATE, days_to_be_added + 1);
                        }
                        cal_temp.add(Calendar.YEAR, 1);
                        days_in_month = SchedulingUtilities.getDaysDiff(cal_temp.getTime(), c.getTime());
                        c.setTime(cal_temp.getTime());
                        monthMarkerPosition += days_in_month * pixel;
                        o.put("position", (int) monthMarkerPosition);
                        o.put("month", new java.text.SimpleDateFormat("yyyy").format(c.getTime()));
                        j.put(Integer.toString(i), o);
                    }
                    j.put("months_between", years_Between);
                }
                JSONObject stackDetails = new JSONObject();
                stackDetails.put("monthmarkers", j);
                stackDetails.put("stackStartDate", ssd);
                stackDetails.put("stackFinishDate", sed);
                stackDetails.put("length", length);
                projectID.put("stackdetails", stackDetails);
                temp.put(projectid, projectID);
                resobj.put("data", temp);
            }
            newObj.put("milestonedata", resobj);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("projdb.getMileStoneForChart : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("projdb.getMileStoneForChart : " + ex.getMessage(), ex);
        }
        return newObj.toString();
    }

    public static int monthsBetween(Date minuend, Date subtrahend) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(minuend);
        int minuendMonth = cal.get(Calendar.MONTH);
        int minuendYear = cal.get(Calendar.YEAR);
        cal.setTime(subtrahend);
        int subtrahendMonth = cal.get(Calendar.MONTH);
        int subtrahendYear = cal.get(Calendar.YEAR);
        return ((minuendYear - subtrahendYear) * (cal.getMaximum(Calendar.MONTH) + 1))
                + (minuendMonth - subtrahendMonth);
    }

    public static int yearsBetween(Date minuend, Date subtrahend) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(minuend);
        int minuendYear = cal.get(Calendar.YEAR);
        cal.setTime(subtrahend);
        int subtrahendYear = cal.get(Calendar.YEAR);
        return minuendYear - subtrahendYear;
    }

    /**
     *
     * @param conn
     * @param projectid
     * @return Array of time progress count like as [completed, ontime, needattention, overdue, future]
     * @throws ServiceException
     */

    public static String getProgressChartData(Connection conn, String projectid, String userid, String reportType) {
        String data = "";
        try {
            int type = (reportType.compareTo("progress") == 0) ? 1 : 0;
            double pendingCount = 0, inProgressCount = 0, completedCount = 0,
                    totalCount = 0, needAttentionCount = 0, futureCount = 0;
            JSONObject jo = null;
            if (type == 1) {
                data = projdb.getProgressReport(conn, -99, projectid);
                jo = new JSONObject(data);
                if (!StringUtil.equal(data, "{data:{}}")) {
                    inProgressCount = jo.getJSONArray("data").length();
                }
                data = projdb.getProgressReport(conn, 0, projectid);
                jo = new JSONObject(data);
                if (!StringUtil.equal(data, "{data:{}}")) {
                    pendingCount = jo.getJSONArray("data").length();
                }
                data = projdb.getProgressReport(conn, 100, projectid);
                jo = new JSONObject(data);
                if (!StringUtil.equal(data, "{data:{}}")) {
                    completedCount = jo.getJSONArray("data").length();
                }
            } else {
                HealthMeterDAO hmdao = new HealthMeterDAOImpl();
                String[] cmpHolidays = getCompHolidays(conn, projectid, "");
                HealthMeter hm = hmdao.getHealthMeter(conn, projectid,cmpHolidays);
                inProgressCount = hm.getOntime();
                needAttentionCount = hm.getNeedAttentaion();
                futureCount = hm.getFuture();
                pendingCount = hm.getOverdue();
                completedCount = hm.getCompleted();
                totalCount = completedCount + inProgressCount + needAttentionCount + pendingCount + futureCount;
            }
            if (type == 1) {
                totalCount = pendingCount + inProgressCount + completedCount;
            }
            pendingCount = Math.round((pendingCount / totalCount) * 100);
            inProgressCount = Math.round((inProgressCount / totalCount) * 100);
            completedCount = Math.round((completedCount / totalCount) * 100);
            if (type == 1) {
                int diff = (int) ((pendingCount + inProgressCount + completedCount + futureCount + needAttentionCount) - 100);
                if (diff > 0 && pendingCount != 0) {
                    pendingCount--;
                    diff--;
                } else if (diff < 0 && pendingCount != 0) {
                    pendingCount++;
                    diff++;
                }

                if (diff > 0 && inProgressCount != 0) {
                    inProgressCount--;
                    diff--;
                } else if (diff < 0 && inProgressCount != 0) {
                    inProgressCount++;
                    diff++;
                }

                if (diff > 0 && completedCount != 0) {
                    completedCount--;
                    diff--;
                } else if (diff < 0 && completedCount != 0) {
                    completedCount++;
                    diff++;
                }
                data = "Progress;";
                data += completedCount + ";" + inProgressCount + ";" + pendingCount + "\n";

            } else {

                futureCount = Math.round((futureCount / totalCount) * 100);
                needAttentionCount = Math.round((needAttentionCount / totalCount) * 100);

                int diff = (int) ((pendingCount + inProgressCount + completedCount + futureCount + needAttentionCount) - 100);
                if (diff > 0 && pendingCount != 0) {
                    pendingCount--;
                    diff--;
                } else if (diff < 0 && pendingCount != 0) {
                    pendingCount++;
                    diff++;
                }

                if (diff > 0 && inProgressCount != 0) {
                    inProgressCount--;
                    diff--;
                } else if (diff < 0 && inProgressCount != 0) {
                    inProgressCount++;
                    diff++;
                }

                if (diff > 0 && completedCount != 0) {
                    completedCount--;
                    diff--;
                } else if (diff < 0 && completedCount != 0) {
                    completedCount++;
                    diff++;
                }

                if (diff > 0 && futureCount != 0) {
                    futureCount--;
                    diff--;
                } else if (diff < 0 && futureCount != 0) {
                    futureCount++;
                    diff++;
                }

                if (diff > 0 && needAttentionCount != 0) {
                    needAttentionCount--;
                    diff--;
                } else if (diff < 0 && needAttentionCount != 0) {
                    needAttentionCount++;
                    diff++;
                }
                data = "Time;";
                data += futureCount + ";" + completedCount + ";" + inProgressCount + ";" + needAttentionCount + ";" + pendingCount + "\n";
            }
        } catch (JSONException ex) {
            data = "";
        } catch (ServiceException ex) {
            data = "";
        } finally {
            return data;
        }
    }
}
