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

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.timezone.Timezone;
import com.krawler.common.util.Constants;
import com.krawler.common.util.KrawlerLog;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.Utilities;
import com.krawler.database.DbPool;
import com.krawler.database.DbPool.Connection;
import com.krawler.esp.project.resource.ResourceDAO;
import com.krawler.esp.project.resource.ResourceDAOImpl;
import com.krawler.esp.servlets.importICSServlet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Arrays;
import java.text.NumberFormat;
import javax.servlet.http.HttpServletRequest;
import com.krawler.utils.json.base.*;
import com.krawler.utils.json.base.JSONException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.ParseException;
import java.util.Date;

public class projectReport {

    public static java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-d HH:mm:ss");
    public static java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd");
    public static String currSymbol = "";
    
    public static String TASK_TOTAL_COST_TEXT = "Cost of Task";
    public static String TASK_TOTAL_WORK_TEXT = "Task total Work";
    //public
    // isValidSession must be true if call to this method is made from inside the app while in session. false for embedded report graphs.

    public static String getProjectReportJson(HttpServletRequest request, boolean returnForGraph, boolean isValidSession) throws ServiceException, SQLException {
        String returnStr = null;
        com.krawler.database.DbPool.Connection conn = null;
        try {
            conn = DbPool.getConnection();
            String userid = "", companyid = "";
            String projectid = request.getParameter("projid") == null ? request.getParameter("projectid") : request.getParameter("projid");
            if (isValidSession) {
                userid = AuthHandler.getUserid(request);
                companyid = AuthHandler.getCompanyid(request);
            } else {
                companyid = CompanyHandler.getCompanyIDFromProject(conn, projectid);
                userid = request.getParameter("userid");
            }
            String cmpcurr = ProfileHandler.getCmpCurrFromProj(conn, projectid);
            try {
                char a1 = (char) Integer.parseInt(cmpcurr, 16);
                currSymbol = Character.toString(a1);
            } catch (Exception e) {
                currSymbol = cmpcurr;
            }
            if (request.getParameter("action") == null) {
                returnStr = projectReport.getReportData(conn, request, userid);
            } else if (request.getParameter("action").compareTo("projsummary") == 0) {
                String baselineid = request.getParameter("baselineid");
                returnStr = projdb.getProjectSummaryData(conn, request.getParameter("projectid"), baselineid, userid);
                returnStr = getProjectSummaryString(returnStr);
                //out.print(returnStr);
            } else if (request.getParameter("action").compareTo("milestones") == 0 || (request.getParameter("rtype")!=null && request.getParameter("rtype").compareTo("milestones") == 0)) {
                returnStr = projdb.getMilestones(conn, request.getParameter("projid"), userid);
                if (returnStr.compareTo("{data:{}}") != 0) {
                    JSONObject jobj = new JSONObject(returnStr);

                    for (int i = 0; i < jobj.getJSONArray("data").length(); i++) {
                        String sdtStr = jobj.getJSONArray("data").getJSONObject(i).getString("startdate");
                        String edtStr = jobj.getJSONArray("data").getJSONObject(i).getString("enddate");

                        if (sdtStr.compareTo("") != 0) {
                            java.util.Date dt = sdf.parse(sdtStr);
                            sdtStr = sdf1.format(dt);
                            jobj.getJSONArray("data").getJSONObject(i).remove("startdate");
                            jobj.getJSONArray("data").getJSONObject(i).put("startdate", sdtStr);
                        }
                        if (edtStr.compareTo("") != 0) {
                            java.util.Date dt = sdf.parse(edtStr);
                            edtStr = sdf1.format(dt);
                            jobj.getJSONArray("data").getJSONObject(i).remove("enddate");
                            jobj.getJSONArray("data").getJSONObject(i).put("enddate", edtStr);
                        }
                        String predecessor = projdb.getPredecessorIndex(conn, jobj.getJSONArray("data").getJSONObject(i).getString("taskid"));
                        jobj.getJSONArray("data").getJSONObject(i).put("predecessor", predecessor);

                        String resources = projdb.getTaskResourcesNames(conn, jobj.getJSONArray("data").getJSONObject(i).getString("taskid"), request.getParameter("projid"));
                        jobj.getJSONArray("data").getJSONObject(i).put("resourcename", resources);
                    }
                    returnStr = jobj.toString();
                }
                //out.print(returnStr);
            } else if (request.getParameter("action").compareTo("overdue") == 0 ||(request.getParameter("rtype")!=null && request.getParameter("rtype").compareTo("overdue") == 0)) {
                returnStr = projdb.getOverdueTask(conn, projectid, userid);
                if (returnStr.compareTo("{data:{}}") != 0) {
                    JSONObject jobj = new JSONObject(returnStr);
                    double cost = 0, work = 0;
                    int diff = 0;
                    String edtstr1 = "";
                    int nonworkweekArr[] = projdb.getNonWorkWeekDays(conn, projectid);
                    String holidayArr[] = projdb.getCompHolidays(conn, projectid, "");
                    for (int i = 0; i < jobj.getJSONArray("data").length(); i++) {
                        String sdtStr = jobj.getJSONArray("data").getJSONObject(i).getString("startdate");
                        String edtStr = jobj.getJSONArray("data").getJSONObject(i).getString("enddate");
                        java.util.Date dt = new Date();
                        if (sdtStr.compareTo("") != 0) {
                            dt = sdf.parse(sdtStr);
                            sdtStr = sdf1.format(dt);
                            jobj.getJSONArray("data").getJSONObject(i).remove("startdate");
                            jobj.getJSONArray("data").getJSONObject(i).put("startdate", sdtStr);
                        }
                        if (edtStr.compareTo("") != 0) {
                            dt = sdf.parse(edtStr);
                            edtStr = sdf1.format(dt);
                            edtstr1 = sdf.format(dt);
                            jobj.getJSONArray("data").getJSONObject(i).remove("enddate");
                            jobj.getJSONArray("data").getJSONObject(i).put("enddate", edtStr);
                        }
                        String predecessor = projdb.getPredecessorIndex(conn, jobj.getJSONArray("data").getJSONObject(i).getString("taskid"));
                        jobj.getJSONArray("data").getJSONObject(i).put("predecessor", predecessor);

                        String resources = projdb.getTaskResourcesNames(conn, jobj.getJSONArray("data").getJSONObject(i).getString("taskid"), request.getParameter("projid"));
                        jobj.getJSONArray("data").getJSONObject(i).put("resourcename", resources);
                        String nextday = importICSServlet.getNextDay(edtstr1);
                        dt = sdf.parse(nextday);
                        diff = projectReport.calculateWorkingDays(dt, new Date(), nonworkweekArr, holidayArr);
                        jobj.getJSONArray("data").getJSONObject(i).put("delay", diff);
                        double[] data = projdb.getTaskCostWorkForOverdueTask(conn, jobj.getJSONArray("data").getJSONObject(i).getString("taskid"),
                                request.getParameter("projid"), diff, dt, userid, companyid);
                        cost = data[0];
                        work = data[1];
                        jobj.getJSONArray("data").getJSONObject(i).put("expence", Utilities.format(cost, returnForGraph));
                    }
                    returnStr = jobj.toString();
                }
                //out.print(returnStr);
            } else if (request.getParameter("action").compareTo("progressreport") == 0 || (request.getParameter("rtype")!=null && request.getParameter("rtype").compareTo("taskinprogress") == 0)) {
                returnStr = projdb.getProgressReport(conn, Integer.parseInt(request.getParameter("percent")), request.getParameter("projid"));
                if (returnStr.compareTo("{data:{}}") != 0) {
                    JSONObject jobj = new JSONObject(returnStr);

                    for (int i = 0; i < jobj.getJSONArray("data").length(); i++) {
                        String sdtStr = jobj.getJSONArray("data").getJSONObject(i).getString("startdate");
                        String edtStr = jobj.getJSONArray("data").getJSONObject(i).getString("enddate");

                        if (sdtStr.compareTo("") != 0) {
                            java.util.Date dt = sdf.parse(sdtStr);
                            sdtStr = sdf1.format(dt);
                            jobj.getJSONArray("data").getJSONObject(i).remove("startdate");
                            jobj.getJSONArray("data").getJSONObject(i).put("startdate", sdtStr);
                        }
                        if (edtStr.compareTo("") != 0) {
                            java.util.Date dt = sdf.parse(edtStr);
                            edtStr = sdf1.format(dt);
                            jobj.getJSONArray("data").getJSONObject(i).remove("enddate");
                            jobj.getJSONArray("data").getJSONObject(i).put("enddate", edtStr);
                        }
                        String predecessor = projdb.getPredecessorIndex(conn, jobj.getJSONArray("data").getJSONObject(i).getString("taskid"));
                        jobj.getJSONArray("data").getJSONObject(i).put("predecessor", predecessor);

                        String resources = projdb.getTaskResourcesNames(conn, jobj.getJSONArray("data").getJSONObject(i).getString("taskid"), request.getParameter("projid"));
                        jobj.getJSONArray("data").getJSONObject(i).put("resourcename", resources);

                        if (request.getParameter("percent").compareTo("0") != 0) {
                            double[] data = projdb.getTaskCostWork(conn, jobj.getJSONArray("data").getJSONObject(i).getString("taskid"),
                                    request.getParameter("projid"), companyid, Integer.parseInt(request.getParameter("percent")));
                            jobj.getJSONArray("data").getJSONObject(i).put("cost", Utilities.format(data[0], returnForGraph));
                            jobj.getJSONArray("data").getJSONObject(i).put("work", Utilities.format(data[1], returnForGraph));
                        }
                    }
                    returnStr = jobj.toString();
                }
                //out.print(returnStr);
            } else if (request.getParameter("action").compareTo("toplevel") == 0) {
                returnStr = projdb.getTask(conn, request.getParameter("projid"), 0, 1000);
                if (returnStr.compareTo("{data:{}}") != 0) {
                    double j = 0, cost = 0, work = 0;
                    JSONObject jobj = new JSONObject(returnStr);
                    JSONObject jobj1 = new JSONObject();
                    JSONObject jtemp = new JSONObject();

                    for (int i = 0; i < jobj.getJSONArray("data").length(); i++) {
                        if (jobj.getJSONArray("data").getJSONObject(i).getString("level").compareTo("0") == 0) {
                            String sdtStr = jobj.getJSONArray("data").getJSONObject(i).getString("startdate");
                            String edtStr = jobj.getJSONArray("data").getJSONObject(i).getString("enddate");
                            if (sdtStr.compareTo("") != 0) {
                                if (j > 0) {
                                    jtemp.put("cost", Utilities.format(cost, returnForGraph));
                                    jtemp.put("work", Utilities.format(work, returnForGraph));
                                    jobj1.append("data", jtemp);
                                    jtemp = new JSONObject();
                                    cost = 0;
                                    work = 0;
                                }
                                jtemp.put("taskid", jobj.getJSONArray("data").getJSONObject(i).getString("taskid").toString());
                                jtemp.put("taskindex", jobj.getJSONArray("data").getJSONObject(i).getString("taskindex").toString());
                                jtemp.put("taskname", jobj.getJSONArray("data").getJSONObject(i).getString("taskname").toString());
                                jtemp.put("duration", projdb.getDuration(jobj.getJSONArray("data").getJSONObject(i).getString("duration")));

                                if (sdtStr.compareTo("") != 0) {
                                    java.util.Date dt = sdf.parse(sdtStr);
                                    sdtStr = sdf1.format(dt);
                                    jtemp.put("startdate", sdtStr);
                                }
                                if (edtStr.compareTo("") != 0) {
                                    java.util.Date dt = sdf.parse(edtStr);
                                    edtStr = sdf1.format(dt);
                                    jtemp.put("enddate", edtStr);
                                }
                                jtemp.put("percentcomplete", jobj.getJSONArray("data").getJSONObject(i).getString("percentcomplete").toString() + "%");
                                String predecessor = projdb.getPredecessorIndex(conn, jobj.getJSONArray("data").getJSONObject(i).getString("taskid"));
                                jtemp.put("predecessor", predecessor);

                                String resources = projdb.getTaskResourcesNames(conn, jobj.getJSONArray("data").getJSONObject(i).getString("taskid"), request.getParameter("projid"));
                                jtemp.put("resourcename", resources);
                                jtemp.put("isparent", jobj.getJSONArray("data").getJSONObject(i).getBoolean("isparent"));
                                j++;
                            }
                        }
                        double[] data = projdb.getTaskCostWork(conn, jobj.getJSONArray("data").getJSONObject(i).getString("taskid"), request.getParameter("projid"), companyid, -99);
                        cost += data[0];
                        work += data[1];
                    }
                    jtemp.put("cost", Utilities.format(cost, returnForGraph));
                    jtemp.put("work", Utilities.format(work, returnForGraph));
                    jobj1.append("data", jtemp);
                    returnStr = jobj1.toString();
                }
            } else if (request.getParameter("action").compareTo("startdatetasks") == 0 || request.getParameter("action").compareTo("enddatetasks") == 0) {
                java.sql.Date date1 = new java.sql.Date(sdf.parse(request.getParameter("date1")).getTime());
                java.sql.Date date2 = new java.sql.Date(sdf.parse(request.getParameter("date2")).getTime());
                returnStr = projdb.getDateWiseTasks(conn, request.getParameter("projid"), date1, date2, request.getParameter("action"), userid);
                if (returnStr.compareTo("{data:{}}") != 0) {
                    JSONObject jobj = new JSONObject(returnStr);

                    for (int i = 0; i < jobj.getJSONArray("data").length(); i++) {
                        String sdtStr = jobj.getJSONArray("data").getJSONObject(i).getString("startdate");
                        String edtStr = jobj.getJSONArray("data").getJSONObject(i).getString("enddate");

                        if (sdtStr.compareTo("") != 0) {
                            java.util.Date dt = sdf.parse(sdtStr);
                            sdtStr = sdf1.format(dt);
                            jobj.getJSONArray("data").getJSONObject(i).remove("startdate");
                            jobj.getJSONArray("data").getJSONObject(i).put("startdate", sdtStr);
                        }
                        if (edtStr.compareTo("") != 0) {
                            java.util.Date dt = sdf.parse(edtStr);
                            edtStr = sdf1.format(dt);
                            jobj.getJSONArray("data").getJSONObject(i).remove("enddate");
                            jobj.getJSONArray("data").getJSONObject(i).put("enddate", edtStr);
                        }
                        String predecessor = projdb.getPredecessorIndex(conn, jobj.getJSONArray("data").getJSONObject(i).getString("taskid"));
                        jobj.getJSONArray("data").getJSONObject(i).put("predecessor", predecessor);

                        String resources = projdb.getTaskResourcesNames(conn, jobj.getJSONArray("data").getJSONObject(i).getString("taskid"), request.getParameter("projid"));
                        jobj.getJSONArray("data").getJSONObject(i).put("resourcename", resources);

                        if (request.getParameter("action").compareTo("enddatetasks") == 0||request.getParameter("action").compareTo("startdatetasks") == 0) {
                            double[] data = projdb.getTaskCostWork(conn, jobj.getJSONArray("data").getJSONObject(i).getString("taskid"),
                                    request.getParameter("projid"), companyid, -99);
                            jobj.getJSONArray("data").getJSONObject(i).put("cost", Utilities.format(data[0], returnForGraph));
                            jobj.getJSONArray("data").getJSONObject(i).put("work", Utilities.format(data[1], returnForGraph));
                        }
                    }
                    returnStr = jobj.toString();
                }
                //out.print(returnStr);
            } else if (request.getParameter("action").compareTo("resanalysis") == 0) {
                java.sql.Date date1 = new java.sql.Date(sdf1.parse(request.getParameter("date1")).getTime());
                java.sql.Date date2 = new java.sql.Date(sdf1.parse(request.getParameter("date2")).getTime());
                java.util.Date d1 = sdf1.parse(request.getParameter("date1"));
                java.util.Date d2 = sdf1.parse(request.getParameter("date2"));
                returnStr = projdb.getDateWiseTasks(conn, request.getParameter("projid"), userid, date1, date2);
                if (returnStr.compareTo("{data:{}}") != 0) {
                    JSONObject jobj = new JSONObject(returnStr);

                    for (int i = 0; i < jobj.getJSONArray("data").length(); i++) {
                        String sdtStr = jobj.getJSONArray("data").getJSONObject(i).getString("startdate");
                        String edtStr = jobj.getJSONArray("data").getJSONObject(i).getString("enddate");

                        if (sdtStr.compareTo("") != 0) {
                            java.util.Date dt = sdf.parse(sdtStr);
                            String sd = sdf1.format(dt);
                            jobj.getJSONArray("data").getJSONObject(i).remove("startdate");
                            jobj.getJSONArray("data").getJSONObject(i).put("startdate", sd);
                        }
                        if (edtStr.compareTo("") != 0) {
                            java.util.Date dt = sdf.parse(edtStr);
                            String ed = sdf1.format(dt);
                            jobj.getJSONArray("data").getJSONObject(i).remove("enddate");
                            jobj.getJSONArray("data").getJSONObject(i).put("enddate", ed);
                        }
                        String predecessor = projdb.getPredecessorIndex(conn, jobj.getJSONArray("data").getJSONObject(i).getString("taskid"));
                        jobj.getJSONArray("data").getJSONObject(i).put("predecessor", predecessor);

                        String resources = projdb.getTaskResourcesNames(conn, jobj.getJSONArray("data").getJSONObject(i).getString("taskid"), request.getParameter("projid"));
                        jobj.getJSONArray("data").getJSONObject(i).put("resourcename", resources);

                        double[] data = projdb.getDateWiseTaskCostWork(conn, jobj.getJSONArray("data").getJSONObject(i).getString("taskid"), projectid, sdf.parse(sdtStr), sdf.parse(edtStr), userid, false);
                        jobj.getJSONArray("data").getJSONObject(i).put("actcost", Utilities.format(data[0], returnForGraph));
                        jobj.getJSONArray("data").getJSONObject(i).remove("duration");
                        jobj.getJSONArray("data").getJSONObject(i).put("duration", Utilities.format(data[1], returnForGraph));

                        double[] curdata = projdb.getDateWiseTaskCostWork(conn, jobj.getJSONArray("data").getJSONObject(i).getString("taskid"),
                                request.getParameter("projid"), d1, d2, userid, false);
                        jobj.getJSONArray("data").getJSONObject(i).put("curcost", Utilities.format(curdata[0], returnForGraph));
                        jobj.getJSONArray("data").getJSONObject(i).put("curduration", Utilities.format(curdata[1], returnForGraph));
                            
                    }
                    returnStr = jobj.toString();
                }
            } else if (request.getParameter("action").compareTo("durationcompare") == 0) {
                returnStr = projdb.getDurationCompareData(conn, request.getParameter("projid"), request.getParameter("baselineid"), userid, returnForGraph);
            } else if (request.getParameter("action").compareTo("costcompare") == 0) {
                returnStr = projdb.getDurationCompareData(conn, request.getParameter("projid"), request.getParameter("baselineid"), userid, returnForGraph);
            } else if (request.getParameter("action").compareTo("datecompare") == 0) {
                returnStr = projdb.getDateCompareData(conn, request.getParameter("projid"), request.getParameter("baselineid"), userid);
            } else if (request.getParameter("action").compareTo("resourcecompare") == 0) {
                returnStr = projdb.getResourceCompareData(conn, request.getParameter("projid"), request.getParameter("baselineid"));
            } else if (request.getParameter("action").compareTo("resourcewisecompare") == 0) {
                String baselineid = request.getParameter("baselineid");
                returnStr = projdb.getResourceTasks(conn, request.getParameter("projid"), baselineid, userid, companyid);
                ResultSet rs = null;
                double[] baseline = null;
                double baselinecost = 0;
                double[] actual = null;
                double actualcost = 0;
                String baselineResult = "";
                double actualwork = 0;
                JSONObject jobj = null;
                double costvary = 0;
                double workvary = 0;
                java.text.SimpleDateFormat sdf2 = new java.text.SimpleDateFormat("yyyy-MM-d HH:mm:ss");
                double baselinework = 0;
                PreparedStatement pstmt = null;
                if (returnStr.compareTo("{data:{}}") != 0) {
                    jobj = new JSONObject(returnStr);
                    for (int i = 0; i < jobj.getJSONArray("data").length(); i++) {
                        String taskid = jobj.getJSONArray("data").getJSONObject(i).getString("taskid");
                        String sdtStr = jobj.getJSONArray("data").getJSONObject(i).getString("startdate");
                        String edtStr = jobj.getJSONArray("data").getJSONObject(i).getString("enddate");
                        if (sdtStr.compareTo("") != 0 && edtStr.compareTo("") != 0) {
                            java.util.Date dt = sdf.parse(sdtStr);
                            java.util.Date temp = dt;
                            dt = sdf.parse(edtStr);
                            actual = projdb.getResourceWiseTaskCostWork(conn, taskid, projectid, jobj.getJSONArray("data").getJSONObject(i).getString("resourceid"), temp, dt, userid, companyid);
                            if (actual[0] == 0 && actual[1] == 0) {
                                actualcost = actual[0];
                                actualwork = actual[1];
                            } else {
                                actualcost = actual[0];
                                actualwork = actual[1];
                            }
                        }
                        baselineResult = projdb.checkForBaselineTask(conn, baselineid, taskid);
                        if (!baselineResult.equals("")) {
                            String basedata[] = baselineResult.split(",");
                            java.util.Date dt = sdf.parse(basedata[1]);
                            java.util.Date temp2 = sdf.parse(basedata[2]);
                            baseline = projdb.getResourceWiseTaskCostWorkForBaseline(conn, basedata[0], basedata[3], baselineid,
                                    projectid, jobj.getJSONArray("data").getJSONObject(i).getString("resourceid"), dt, temp2, userid);
                            baselinecost = baseline[0];
                            baselinework = baseline[1];
                            costvary = actual[0] - baseline[0];
                            workvary = actual[1] - baseline[1];
                            jobj.getJSONArray("data").getJSONObject(i).put("workvary", Utilities.format(workvary, returnForGraph));
                            jobj.getJSONArray("data").getJSONObject(i).put("costvary", Utilities.format(costvary, returnForGraph));
                            if(jobj.getJSONArray("data").getJSONObject(i).getString("taskname").compareToIgnoreCase(basedata[4]) != 0){
                                jobj.getJSONArray("data").getJSONObject(i).remove("taskname");
                                jobj.getJSONArray("data").getJSONObject(i).put("taskname", basedata[4]);
                            }
                            jobj.getJSONArray("data").getJSONObject(i).put("costvary", Utilities.format(costvary, returnForGraph));
                        } else {
                            jobj.getJSONArray("data").getJSONObject(i).put("workvary", "NA");
                            jobj.getJSONArray("data").getJSONObject(i).put("costvary", "NA");
                        }
                        jobj.getJSONArray("data").getJSONObject(i).put("duration", Utilities.format(actualwork, returnForGraph));
                        jobj.getJSONArray("data").getJSONObject(i).put("actcost", Utilities.format(actualcost, returnForGraph));
                        pstmt = conn.prepareStatement("select resourcename from proj_resources where resourceid = ?");
                        pstmt.setString(1, jobj.getJSONArray("data").getJSONObject(i).getString("resourceid"));
                        rs = pstmt.executeQuery();
                        if (rs.next()) {
                            jobj.getJSONArray("data").getJSONObject(i).put("resourcename", rs.getString("resourcename"));
                        } else {
                            jobj.getJSONArray("data").getJSONObject(i).put("resourcename", "");
                        }
                    }
                }
                returnStr = (jobj != null) ? jobj.toString() : returnStr;
            } else if (request.getParameter("action").compareTo("projectcharts") == 0) {
                String subtype = request.getParameter("subtype");
                returnStr = projdb.getProgressChartData(conn, request.getParameter("projid"), userid, subtype);
            }
            conn.commit();
        } catch (ServiceException xex) {
            KrawlerLog.op.warn("Service Exception projectreport.java:" + xex.toString());
            DbPool.quietRollback(conn);
        } catch (JSONException jE) {
            returnStr = "Json Exception: " + jE.getMessage();
            DbPool.quietRollback(conn);
        } catch (ParseException datePE) {
            returnStr = "Date Parse Exception: " + datePE.getMessage();
            DbPool.quietRollback(conn);
        } catch (Exception e) {
            returnStr = "Exception: " + e.getMessage();
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return returnStr;
    }

    // isValidSession must be true if call to this method is made from inside the app while in session. false for embedded report graphs.
    public static String getProjectReport2Json(HttpServletRequest request, boolean returnForGraph, boolean isValidSession) throws ServiceException {
        String returnStr = null;
        com.krawler.database.DbPool.Connection conn = null;
        try {
            conn = DbPool.getConnection();
            String userid = "", companyid = "", projectid = request.getParameter("projid");
            if (isValidSession) {
                userid = AuthHandler.getUserid(request);
                companyid = AuthHandler.getCompanyid(request);
            } else {
                companyid = CompanyHandler.getCompanyIDFromProject(conn, projectid);
                userid = request.getParameter("userid");
            }
//            String projectid = request.getParameter("projid") == null ? request.getParameter("projectid") : request.getParameter("projid");
//            String cmpcurr = ProfileHandler.getCmpCurrFromProj(conn, projectid);
//            char a1 = (char) Integer.parseInt(cmpcurr, 16);
//            String currSymbol = Character.toString(a1);

            java.sql.Date date1 = new java.sql.Date(sdf1.parse(request.getParameter("date1")).getTime());
            java.sql.Date date2 = new java.sql.Date(sdf1.parse(request.getParameter("date2")).getTime());
            returnStr = projdb.getDateWiseTasks(conn, projectid, date1, date2, request.getParameter("action"), userid);
            if (returnStr.compareTo("{data:{}}") != 0) {
                JSONObject jobj = new JSONObject(returnStr);

                for (int i = 0; i < jobj.getJSONArray("data").length(); i++) {
                    String sdtStr = jobj.getJSONArray("data").getJSONObject(i).getString("startdate");
                    String edtStr = jobj.getJSONArray("data").getJSONObject(i).getString("enddate");

                    if (sdtStr.compareTo("") != 0) {
                        java.util.Date dt = sdf.parse(sdtStr);
                        sdtStr = sdf1.format(dt);
                        jobj.getJSONArray("data").getJSONObject(i).remove("startdate");
                        jobj.getJSONArray("data").getJSONObject(i).put("startdate", sdtStr);
                    }
                    if (edtStr.compareTo("") != 0) {
                        java.util.Date dt = sdf.parse(edtStr);
                        edtStr = sdf1.format(dt);
                        jobj.getJSONArray("data").getJSONObject(i).remove("enddate");
                        jobj.getJSONArray("data").getJSONObject(i).put("enddate", edtStr);
                    }
                    String predecessor = projdb.getPredecessorIndex(conn, jobj.getJSONArray("data").getJSONObject(i).getString("taskid"));
                    jobj.getJSONArray("data").getJSONObject(i).put("predecessor", predecessor);

                    String resources = projdb.getTaskResourcesNames(conn, jobj.getJSONArray("data").getJSONObject(i).getString("taskid"), projectid);
                    jobj.getJSONArray("data").getJSONObject(i).put("resourcename", resources);

                    if (request.getParameter("action").compareTo("enddatetasks") == 0) {
                        double[] data = projdb.getTaskCostWork(conn, jobj.getJSONArray("data").getJSONObject(i).getString("taskid"),
                                projectid, companyid, -99);
                        jobj.getJSONArray("data").getJSONObject(i).put("cost", Utilities.format(data[0], returnForGraph));
                        jobj.getJSONArray("data").getJSONObject(i).put("work", Utilities.format(data[1], returnForGraph));
                    }
                }
                returnStr = jobj.toString();
                //out.print(returnStr);
            }
            conn.commit();
        } catch (SessionExpiredException ex) {
            returnStr = "{\"valid\":false}";
            DbPool.quietRollback(conn);
        } catch (ServiceException xex) {
            KrawlerLog.op.warn("Service Exception projectreport.java:" + xex.toString());
            DbPool.quietRollback(conn);
        }/* catch (UnsupportedEncodingException ex) {
        Logger.getLogger(projectReport.class.getName()).log(Level.SEVERE, null, ex);
        } */ catch (JSONException jE) {
            returnStr = "Json Exception";
            DbPool.quietRollback(conn);
        } catch (ParseException datePE) {
            returnStr = "Date Parse Exception";
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return returnStr;
    }

    // isValidSession must be true if call to this method is made from inside the app while in session. false for embedded report graphs.
    public static String getProjectReport1Json(HttpServletRequest request, boolean returnForGraph, boolean isValidSession) throws ServiceException {
        String returnRes = "";
        com.krawler.database.DbPool.Connection conn = null;
        try {
            conn = DbPool.getConnection();
            String userid = "", companyid = "";
            String projectid = request.getParameter("projid") == null ? request.getParameter("projectid") : request.getParameter("projid");
            if (isValidSession) {
                userid = AuthHandler.getUserid(request);
                companyid = AuthHandler.getCompanyid(request);
            } else {
                companyid = CompanyHandler.getCompanyIDFromProject(conn, projectid);
                userid = request.getParameter("userid");
            }
            if (request.getParameter("action") == null) {
                String groupby = request.getParameter("groupby");
                String reporttype = request.getParameter("reporttype");
                String period = request.getParameter("period");

                String cmpcurr = ProfileHandler.getCmpCurrFromProj(conn, projectid);
                try {
                    char a1 = (char) Integer.parseInt(cmpcurr, 16);
                    currSymbol = Character.toString(a1);
                } catch (Exception e) {
                    currSymbol = cmpcurr;
                }
                ////////////////////////////////////////////

                if (groupby.compareTo("task") == 0) {
                    String tasks = projdb.getTask(conn, projectid, 0, 1000);
                    if (tasks.compareTo("{data:{}}") != 0) {
                        try {
                            JSONObject jobj = new JSONObject(tasks);
                            int nonworkweekArr[] = projdb.getNonWorkWeekDays(conn, projectid);
                            String holidayArr[] = projdb.getCompHolidays(conn, projectid, "");
                            returnRes += "[";

                            for (int i = 0; i < jobj.getJSONArray("data").length(); i++) {
                                String tempStr = "";
                                float cost = 0;
                                JSONObject retjObj = new JSONObject();
                                retjObj.put("name", jobj.getJSONArray("data").getJSONObject(i).getString("taskname"));
                                Map<String, Float> periodWiseDuration = new HashMap<String, Float>();
                                if (period.equals("Daily")) {
                                    periodWiseDuration = projectReport.getDailyDateWiseDuration(conn, jobj.getJSONArray("data").getJSONObject(i), nonworkweekArr, holidayArr, userid);
                                } else if (period.equals("Weekly")) {
                                    periodWiseDuration = projectReport.getWeekWiseDuration(conn, jobj.getJSONArray("data").getJSONObject(i), nonworkweekArr, holidayArr, userid);
                                } else if (period.equals("Monthly")) {
                                    periodWiseDuration = projectReport.getMonthWiseDuration(conn, jobj.getJSONArray("data").getJSONObject(i), nonworkweekArr, holidayArr, userid);
                                } else if (period.equals("Yearly")) {
                                    periodWiseDuration = projectReport.getYearWiseDuration(jobj.getJSONArray("data").getJSONObject(i), nonworkweekArr, holidayArr);
                                }
                                String level = jobj.getJSONArray("data").getJSONObject(i).getString("level");

                                float[] rateArray = new float[periodWiseDuration.size()];
                                for (int aCnt = 0; aCnt < periodWiseDuration.size(); aCnt++) {
                                    rateArray[aCnt] = 0;
                                }
                                String resources = projdb.getBillableTaskResources(conn, jobj.getJSONArray("data").getJSONObject(i).getString("taskid"), projectid);
                                JSONObject resourcejObj = new JSONObject(resources);
                                if (resources.compareTo("{data:{}}") != 0) {
                                    cost = 0;
                                    for (int cnt = 0; cnt < resourcejObj.getJSONArray("data").length(); cnt++) {
                                        JSONObject retjObj1 = new JSONObject();
                                        retjObj1.put("info", resourcejObj.getJSONArray("data").getJSONObject(cnt).getString("resourceName"));
                                        Iterator iterator = periodWiseDuration.entrySet().iterator();
                                        int k = 0;
                                        Map.Entry entry = null;
                                        float actValue = Float.parseFloat(resourcejObj.getJSONArray("data").getJSONObject(cnt).getString("wuvalue")) / 100;
                                        while (iterator.hasNext()) {
                                            float value = 0;
                                            entry = (Map.Entry) iterator.next();
                                            if(resourcejObj.getJSONArray("data").getJSONObject(cnt).getJSONObject("type").getInt("typeID") == Constants.WORK_RESOURCE){
                                                if (reporttype.compareTo("timeline") == 0) {
                                                    value = Float.parseFloat(entry.getValue().toString());
                                                    value = actValue * value;
                                                    retjObj1.put(entry.getKey().toString(), value + " hrs");
                                                } else {
                                                    value = Float.parseFloat(resourcejObj.getJSONArray("data").getJSONObject(cnt).getString("stdRate")) * Float.parseFloat(entry.getValue().toString());
                                                    value = actValue * value;
                                                    retjObj1.put(entry.getKey().toString(), value);
                                                    rateArray[k] += value;
                                                    k++;
                                                    cost += value;
                                                }
                                            } else if(resourcejObj.getJSONArray("data").getJSONObject(cnt).getJSONObject("type").getInt("typeID") == Constants.MATERIAL_RESOURCE){
                                                if (reporttype.compareTo("timeline") == 0) {
                                                    retjObj1.put(entry.getKey().toString(), 0);
                                                } else {
                                                    value = Float.parseFloat(resourcejObj.getJSONArray("data").getJSONObject(cnt).getString("stdRate")) 
                                                                * Float.parseFloat(resourcejObj.getJSONArray("data").getJSONObject(cnt).getString("wuvalue"));
                                                    retjObj1.put(entry.getKey().toString(),  value);
                                                    rateArray[k] += value;
                                                    cost += value;
                                                    k++;
                                                    break;
                                                }
                                            }
                                       }
                                        retjObj1.put("level", Integer.parseInt(level) + 1);
                                        retjObj1.put("flag", false);
                                        tempStr += retjObj1.toString() + ",";
                                    }
                                    if (reporttype.equals("timeline")) {
                                        Iterator iterator = periodWiseDuration.entrySet().iterator();
                                        while (iterator.hasNext()) {
                                            Map.Entry entry = (Map.Entry) iterator.next();
                                            retjObj.put(entry.getKey().toString(), entry.getValue().toString() + " hrs");
                                        }
                                    } else if (reporttype.equals("costline")) {
                                        Iterator iterator = periodWiseDuration.entrySet().iterator();
                                        int k = 0;
                                        while (iterator.hasNext()) {
                                            Map.Entry entry = (Map.Entry) iterator.next();
                                            retjObj.put(entry.getKey().toString(), rateArray[k]);
                                            k++;
                                        }
                                    }
                                }
                                retjObj.put("cost", cost);
                                returnRes += retjObj.toString() + ",";
                            }
                            returnRes = returnRes.substring(0, returnRes.length() - 1);
                            returnRes = returnRes.concat("]");
                            returnRes = "{data:" + returnRes + "}";

                        } catch (Exception e) {
                            System.out.print(e.getMessage());
                        }
                    } else {
                        returnRes = "{data:[]}";
                    }

                } else if (groupby.compareTo("resource") == 0) {

                    if (period.equals("Daily") || period.equals("Weekly") || period.equals("Monthly") || period.equals("Yearly")) {
                        if (reporttype.compareTo("timeline") == 0 || reporttype.compareTo("costline") == 0) {
                            //                    String resourcenames = dbcon.getProjResources(projectid);
                            String resourcenames = "{data:{}}";
                            if (reporttype.compareTo("timeline") == 0) {
                                resourcenames = projdb.getProjectResources(conn, projectid, false);
                            } else {
                                resourcenames = projdb.getProjectResources(conn, projectid, true);
                            }
                            JSONObject resourcejObj = new JSONObject(resourcenames);
                            if (resourcenames.compareTo("{data:{}}") != 0) {
                                int nonworkweekArr[] = projdb.getNonWorkWeekDays(conn, projectid);
                                String holidayArr[] = projdb.getCompHolidays(conn, projectid, "");

                                returnRes += "[";
                                for (int i = 0; i < resourcejObj.getJSONArray("data").length(); i++) {
                                    String resource = resourcejObj.getJSONArray("data").getJSONObject(i).getString("resourceID");
                                    JSONObject tempObj = new JSONObject();

                                    JSONObject resObj = new JSONObject();
                                    resObj.put("name", resourcejObj.getJSONArray("data").getJSONObject(i).getString("resourceName"));
                                    float cost = 0;
                                    float time = 0;

                                    String taskinfo = projdb.getResourceRelatedTask(conn, resource, projectid, companyid);
                                    if (taskinfo.compareTo("{data:{}}") != 0) {
                                        JSONObject taskObj = new JSONObject(taskinfo);
                                        cost = 0;
                                        time = 0;
                                        for (int j = 0; j < taskObj.getJSONArray("data").length(); j++) {
                                            tempObj = new JSONObject();
                                            Map<String, Float> periodWiseDuration = new HashMap<String, Float>();
                                            if (period.equals("Daily")) {
                                                periodWiseDuration = projectReport.getDailyDateWiseDuration(conn, taskObj.getJSONArray("data").getJSONObject(j), nonworkweekArr, holidayArr, userid);
                                            } else if (period.equals("Weekly")) {
                                                periodWiseDuration = projectReport.getWeekWiseDuration(conn, taskObj.getJSONArray("data").getJSONObject(j), nonworkweekArr, holidayArr, userid);
                                            } else if (period.equals("Monthly")) {
                                                periodWiseDuration = projectReport.getMonthWiseDuration(conn, taskObj.getJSONArray("data").getJSONObject(j), nonworkweekArr, holidayArr, userid);
                                            } else if (period.equals("Yearly")) {
                                                periodWiseDuration = projectReport.getYearWiseDuration(taskObj.getJSONArray("data").getJSONObject(j), nonworkweekArr, holidayArr);
                                            }

                                            Iterator iterator = periodWiseDuration.entrySet().iterator();
                                            Map.Entry entry = null;
                                            while (iterator.hasNext()) {
                                                entry = (Map.Entry) iterator.next();
                                                float value = 0;
                                                float actValue = Float.parseFloat(resourcejObj.getJSONArray("data").getJSONObject(i).getString("wuvalue")) / 100;
                                                if(resourcejObj.getJSONArray("data").getJSONObject(i).getJSONObject("type").getInt("typeID") == Constants.WORK_RESOURCE){
                                                    if (reporttype.compareTo("timeline") == 0) {
                                                        value = Float.parseFloat(entry.getValue().toString());
                                                        value = actValue * value;
                                                        time += value;
                                                    } else {
                                                        value = Float.parseFloat(resourcejObj.getJSONArray("data").getJSONObject(i).getString("stdRate")) * 
                                                                Float.parseFloat(entry.getValue().toString());
                                                        value = actValue * value;
                                                        cost += value;
                                                    }
                                                } else if(resourcejObj.getJSONArray("data").getJSONObject(i).getJSONObject("type").getInt("typeID") == Constants.MATERIAL_RESOURCE){
                                                    if (reporttype.compareTo("timeline") == 0) {
                                                        time += 0;
                                                    } else {
                                                        value = Float.parseFloat(resourcejObj.getJSONArray("data").getJSONObject(i).getString("stdRate")) 
                                                                * Float.parseFloat(resourcejObj.getJSONArray("data").getJSONObject(i).getString("wuvalue"));
                                                        cost += value;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if(resourcejObj.getJSONArray("data").getJSONObject(i).getJSONObject("type").getInt("typeID") == Constants.COST_RESOURCE){
                                        float value = Float.parseFloat(resourcejObj.getJSONArray("data").getJSONObject(i).getString("stdRate"));
                                        float actValue = Float.parseFloat(resourcejObj.getJSONArray("data").getJSONObject(i).getString("wuvalue")) / 100;
                                        if (reporttype.compareTo("timeline") == 0) {
                                            time += 0;
                                        } else {
                                            value = actValue * value;
                                            cost += value;
                                        }
                                    }
                                    if (reporttype.compareTo("timeline") == 0) {
                                        resObj.put("time", time);
                                        returnRes += resObj.toString() + ",";
                                    } else {
                                        resObj.put("cost", cost);
                                        returnRes += resObj.toString() + ",";
                                    }
                                }
                                returnRes = returnRes.substring(0, returnRes.length() - 1);
                                returnRes = returnRes.concat("]");
                                returnRes = "{data:" + returnRes + "}";
                            }
                        }
                    }
                }
            }
            conn.commit();
        } catch (ServiceException xex) {
            KrawlerLog.op.warn("Service Exception projectreport.java:" + xex.toString());
            DbPool.quietRollback(conn);
        } catch (SessionExpiredException ex) {
            returnRes = "{\"valid\": false, \"data\":{}}";
            KrawlerLog.op.warn("Service Exception projectreport.java:" + ex.toString());
            DbPool.quietRollback(conn);
        } catch (JSONException jE) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return returnRes;
    }

    public static Map<String, Float> getDailyDateWiseDuration(Connection conn,
            JSONObject jObj,int []NonWorkDays,String []holidays, String userid) {
        Map<String, Float> periodWiseDuration = new HashMap<String, Float>();
        Calendar c1 = Calendar.getInstance();
        try {
            String sdtStr = jObj.getString("startdate");
            String edtStr = jObj.getString("enddate");
            java.util.Date stdate = sdf.parse(sdtStr);
            java.util.Date enddate = sdf.parse(edtStr);
            int cnt = 0;
            int noofdays = 0;
            if (!jObj.getString("duration").contains("h")) {
                enddate = sdf.parse(sdf.format(enddate));
                stdate = sdf.parse(sdf.format(stdate));
                double dur = projdb.getDuration(jObj.getString("duration"));
                while (stdate.compareTo(enddate) <= 0 && dur != 0) {
                    if ((Arrays.binarySearch(NonWorkDays, stdate.getDay()) < 0 && Arrays.binarySearch(holidays, sdf1.format(stdate)) < 0) || ((Arrays.binarySearch(NonWorkDays, stdate.getDay()) >= 0 || Arrays.binarySearch(holidays, sdf1.format(stdate)) >= 0) && cnt == 0) /*|| (stdate.getDay() == 0 && cnt == 1)*/) {
                        if (stdate.compareTo(enddate) == 0) {
                            String duration = jObj.getString("duration");
                            String d = duration.contains("d") ? duration.substring(0, duration.indexOf("d")) : duration;
                            d = String.valueOf(Float.parseFloat(d));
                            d = (d.contains(".")) ? d.substring(d.indexOf(".") + 1) : "0";
                            if (!d.equals("0")) {
                                d = "0." + d;
                                int val = (int) (Float.parseFloat(d) * 8);
                                periodWiseDuration.put(sdf1.format(stdate), (float) val);
                            } else {
                                periodWiseDuration.put(sdf1.format(stdate), 8f);
                            }
                        } else {
                            periodWiseDuration.put(sdf1.format(stdate), 8f);
                        }
                        noofdays++;
                    }
                    c1.setTime(stdate);
                    c1.add(Calendar.DATE, 1);
                    stdate = sdf.parse(sdf.format(c1.getTime()));
                    cnt++;
                }
                if (stdate.compareTo(enddate) == 0 && cnt == 0) {
                    String duration = jObj.getString("duration");
                    if (duration.contains("d")) {
                        duration = duration.substring(0, duration.indexOf("d"));
                    } else if (duration.contains("h")) {
                        duration = duration.substring(0, duration.indexOf("h"));
                        duration = String.valueOf(Float.parseFloat(duration) / 8);
                    }
                    if (cnt != 0) {
                        duration = String.valueOf(Float.parseFloat(duration) - noofdays);
                    }
                    if (((Arrays.binarySearch(NonWorkDays, stdate.getDay()) < 0 && Arrays.binarySearch(holidays, sdf1.format(stdate)) < 0) || ((Arrays.binarySearch(NonWorkDays, stdate.getDay()) >= 0 || Arrays.binarySearch(holidays, sdf1.format(stdate)) >= 0) && cnt == 0) /*|| (stdate.getDay() == 0 && cnt == 1)*/) && Float.parseFloat(duration) > 0) {
                        periodWiseDuration.put(sdf1.format(stdate), Float.parseFloat(duration) * 8);
                    }
                }
            } else {
                String duration = jObj.getString("duration");
                duration = duration.substring(0, duration.indexOf("h"));
                duration = String.valueOf(Float.parseFloat(duration) / 8);
                periodWiseDuration.put(sdf1.format(sdf1.parse(sdf.format(stdate))), Float.parseFloat(duration) * 8);
            }
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
        return periodWiseDuration;

    }

    public static Map<String, Float> getWeekWiseDuration(Connection conn,
            JSONObject jObj,int []NonWorkDays,String []holidays, String userid) {
        Map<String, Float> weeklydata = new HashMap<String, Float>();
        Calendar c1 = Calendar.getInstance();
        try {
            String sdtStr = jObj.getString("startdate");
            String edtStr = jObj.getString("enddate");
            java.util.Date taskStdate = sdf1.parse(sdtStr);
            java.util.Date taskEnddate = sdf1.parse(edtStr);
            int cnt = 0;
            int noofdays = 0;
            float totalWeekHours = 0;
            while (taskStdate.compareTo(taskEnddate) < 0) {
                if (Arrays.binarySearch(NonWorkDays, taskStdate.getDay()) < 0 && Arrays.binarySearch(holidays, sdf1.format(taskStdate)) < 0) {
                    //if (taskStdate.getDay() != 6) {
                    noofdays++;
                    totalWeekHours += 8;
                } else if ((Arrays.binarySearch(NonWorkDays, taskStdate.getDay()) >= 0 || Arrays.binarySearch(holidays, sdf1.format(taskStdate)) >= 0) && cnt == 0) {
                    totalWeekHours += 8;
                    noofdays++;
                } else {
                    c1.set(taskStdate.getYear() + 1900, taskStdate.getMonth(),
                            taskStdate.getDate());
                    c1.add(Calendar.DATE, -7);
                    java.util.Date previousDate = sdf1.parse(sdf1.format(c1.getTime()));
                    weeklydata.put(sdf1.format(sdf1.parse(previousDate.toString())), totalWeekHours);
                    if (cnt == 0 || cnt == 1) {
                        totalWeekHours = 8;
                        noofdays++;
                    } else {
                        totalWeekHours = 0;
                    }
                }
                c1.set(taskStdate.getYear() + 1900, taskStdate.getMonth(),
                        taskStdate.getDate());
                c1.add(Calendar.DATE, 1);
                taskStdate = sdf1.parse(sdf1.format(c1.getTime()));
                cnt++;
            }
            if (taskStdate.compareTo(taskEnddate) == 0) {
                String duration = jObj.getString("duration");
                if (duration.contains("d")) {
                    duration = duration.substring(0, duration.indexOf("d"));
                } else if (duration.contains("h")) {
                    duration = duration.substring(0, duration.indexOf("h"));
                    duration = String.valueOf(Float.parseFloat(duration) / 8);
                }
                if (cnt != 0) {
                    duration = String.valueOf(Float.parseFloat(duration) - noofdays);
                }

                totalWeekHours += Float.parseFloat(duration) * 8;
                duration = String.valueOf(totalWeekHours) + " hrs";
                //if ((taskStdate.getDay() != 0 && taskStdate.getDay() != 6) || (((taskStdate.getDay() == 0 || taskStdate.getDay() == 6) && cnt == 0) || (taskStdate.getDay() == 0 && cnt == 1))) {
                if ((Arrays.binarySearch(NonWorkDays, taskStdate.getDay()) < 0 && Arrays.binarySearch(holidays, sdf1.format(taskStdate)) < 0) || ((Arrays.binarySearch(NonWorkDays, taskStdate.getDay()) >= 0 || Arrays.binarySearch(holidays, sdf1.format(taskStdate)) >= 0) && cnt == 0) /*|| (Arrays.binarySearch(NonWorkDays, taskStdate.getDay())>0 && cnt == 1)*/) {
                    c1.set(taskStdate.getYear() + 1900, taskStdate.getMonth(),
                            taskStdate.getDate());
                    c1.add(Calendar.DATE, -taskStdate.getDay());
                    java.util.Date previousDate = sdf1.parse(sdf1.format(c1.getTime()));
                    weeklydata.put(sdf1.format(sdf1.parse(previousDate.toString())), totalWeekHours);
                }
            }

        } catch (Exception e) {
        }
        return weeklydata;
    }

    public static Map<String, Float> getMonthWiseDuration(Connection conn,
            JSONObject jObj,int []NonWorkDays,String []holidays, String userid) {
        Map<String, Float> monthlydata = new HashMap<String, Float>();
        java.text.SimpleDateFormat monthlyDateFomrat = new java.text.SimpleDateFormat("MMM-yyyy");
        Calendar c1 = Calendar.getInstance();
        try {
            String sdtStr = jObj.getString("startdate");
            String edtStr = jObj.getString("enddate");
            java.util.Date taskStdate = sdf1.parse(sdtStr);
            java.util.Date taskEnddate = sdf1.parse(edtStr);

            if (taskStdate.getMonth() == taskEnddate.getMonth() && taskStdate.getYear() == taskEnddate.getYear()) {
                String duration = jObj.getString("duration");
                if (duration.contains("d")) {
                    duration = duration.substring(0, duration.indexOf("d"));
                } else if (duration.contains("h")) {
                    duration = duration.substring(0, duration.indexOf("h"));
                    duration = String.valueOf(Float.parseFloat(duration) / 8);
                }
                float days = Float.parseFloat(duration) * 8;
                monthlydata.put(monthlyDateFomrat.format(sdf1.parse(taskStdate.toString())), days);
            } else {
                c1.set(taskStdate.getYear() + 1900, taskStdate.getMonth(),
                        taskStdate.getDate());
                int lastDateOfMonth = c1.getActualMaximum(Calendar.DATE);
                c1.set(taskStdate.getYear() + 1900, taskStdate.getMonth(),
                        lastDateOfMonth);
                java.util.Date lastMonthDate = sdf1.parse(sdf1.format(c1.getTime()));
                float partialWorkingdays = calculateWorkingDays(taskStdate,
                        lastMonthDate, NonWorkDays, holidays);
                monthlydata.put(monthlyDateFomrat.format(sdf1.parse(taskStdate.toString())),
                        (partialWorkingdays * 8));
                c1.add(Calendar.DATE, 1);
                taskStdate = sdf1.parse(sdf1.format(c1.getTime()));
                while (taskStdate.compareTo(taskEnddate) <= 0) {
                    if (((int) ((taskEnddate.getTime() - taskStdate.getTime()) / (1000 * 60 * 60 * 24))) < c1.getActualMaximum(Calendar.DATE)) {
                        partialWorkingdays = calculateWorkingDays(taskStdate,
                                taskEnddate, NonWorkDays, holidays);
                        c1.set(taskEnddate.getYear() + 1900, taskEnddate.getMonth(), taskEnddate.getDate());
                    } else {
                        lastDateOfMonth = c1.getActualMaximum(Calendar.DATE);
                        c1.set(taskStdate.getYear() + 1900, taskStdate.getMonth(), lastDateOfMonth);
                        lastMonthDate = sdf1.parse(sdf1.format(c1.getTime()));
                        partialWorkingdays = calculateWorkingDays(taskStdate,
                                lastMonthDate, NonWorkDays, holidays);
                    }
                    monthlydata.put(monthlyDateFomrat.format(sdf1.parse(taskStdate.toString())),
                            (partialWorkingdays * 8));
                    c1.add(Calendar.DATE, 1);
                    taskStdate = sdf1.parse(sdf1.format(c1.getTime()));
                }
            }
        } catch (Exception e) {
        }
        return monthlydata;
    }

    public static Map<String, Float> getYearWiseDuration(
            JSONObject jObj,int []NonWorkDays,String []holidays) {
        Map<String, Float> yearlydata = new HashMap<String, Float>();
        java.text.SimpleDateFormat yearlyDateFomrat = new java.text.SimpleDateFormat("yyyy");
        Calendar c1 = Calendar.getInstance();
        try {
            String sdtStr = jObj.getString("startdate");
            String edtStr = jObj.getString("enddate");
            java.util.Date taskStdate = sdf1.parse(sdtStr);
            java.util.Date taskEnddate = sdf1.parse(edtStr);

            if (taskStdate.getYear() == taskEnddate.getYear()) {
                String duration = jObj.getString("duration");
                if (duration.contains("d")) {
                    duration = duration.substring(0, duration.indexOf("d"));
                } else if (duration.contains("h")) {
                    duration = duration.substring(0, duration.indexOf("h"));
                    duration = String.valueOf(Float.parseFloat(duration) / 8);
                }
                float days = Float.parseFloat(duration) * 8;
                yearlydata.put(yearlyDateFomrat.format(taskStdate), days);
            } else {
                c1.set(taskStdate.getYear() + 1900, 11, 31);
                java.util.Date lastDateOfYear = sdf1.parse(sdf1.format(c1.getTime()));
                float partialWorkingdays = calculateWorkingDays(taskStdate,
                        lastDateOfYear, NonWorkDays, holidays);
                yearlydata.put(yearlyDateFomrat.format(taskStdate),
                        (partialWorkingdays * 8));
                c1.add(Calendar.DATE, 1);
                taskStdate = sdf1.parse(sdf1.format(c1.getTime()));
                while (taskStdate.compareTo(taskEnddate) <= 0) {
                    if (((int) ((taskEnddate.getTime() - taskStdate.getTime()) / (1000 * 60 * 60 * 24))) < c1.getActualMaximum(Calendar.DAY_OF_YEAR)) {
                        partialWorkingdays = calculateWorkingDays(taskStdate,
                                taskEnddate, NonWorkDays, holidays);
                        c1.set(taskEnddate.getYear() + 1900, taskEnddate.getMonth(), taskEnddate.getDate());
                    } else {
                        c1.set(taskStdate.getYear() + 1900, 11, 31);
                        lastDateOfYear = sdf1.parse(sdf1.format(c1.getTime()));
                        partialWorkingdays = calculateWorkingDays(taskStdate,
                                lastDateOfYear, NonWorkDays, holidays);
                    }
                    yearlydata.put(yearlyDateFomrat.format(taskStdate),
                            (partialWorkingdays * 8));
                    c1.add(Calendar.DATE, 1);
                    taskStdate = sdf1.parse(sdf1.format(c1.getTime()));
                }
            }
        } catch (Exception e) {
        }
        return yearlydata;
    }

    public static String ColumnHeader(Connection conn, String projectid, String period, String userid, String groupby, String reporttype) {
        String returnColumnHeader = "";
        java.text.SimpleDateFormat periodWiseFormat = null;
        Calendar c1 = Calendar.getInstance();
        try {
            if (period.equals("Monthly")) {
                periodWiseFormat = new java.text.SimpleDateFormat("MMM-yyyy");
            } else if (period.equals("Yearly")) {
                periodWiseFormat = new java.text.SimpleDateFormat("yyyy");
            }
            int NWD[] = projdb.getNonWorkWeekDays(conn, projectid);
            String CH[] = projdb.getCompHolidays(conn, projectid, "");
            String minmaxProjectDate = projdb.getminmaxProjectDate(conn, projectid, userid);
            if (minmaxProjectDate != null) {
                JSONObject jObj = new JSONObject();
                String[] dates = minmaxProjectDate.split(",");
                java.util.Date stdate = sdf1.parse(dates[0]);
                java.util.Date enddate = sdf1.parse(dates[1]);
                if (period.equals("Daily")) {
                    c1.setTime(stdate);
                    while (c1.getTime().compareTo(enddate) <= 0) {
                        jObj = new JSONObject();
                        if (Arrays.binarySearch(NWD, (c1.get(Calendar.DAY_OF_WEEK) - 1)) > -1 || Arrays.binarySearch(CH, sdf.format(c1.getTime())) > -1) {
                            jObj.put("0", sdf1.format(c1.getTime()));
                        } else {
                            jObj.put("1", sdf1.format(c1.getTime()));
                        }

                        returnColumnHeader += jObj.toString() + ",";
                        c1.add(Calendar.DATE, 1);
                        stdate = sdf1.parse(sdf1.format(c1.getTime()));
                    }
                } else if (period.equals("Weekly")) {
                    c1.set(stdate.getYear() + 1900, stdate.getMonth(), stdate.getDate());
                    c1.add(Calendar.DATE, -stdate.getDay());
                    stdate = sdf1.parse(sdf1.format(c1.getTime()));
                    while (stdate.compareTo(enddate) <= 0) {
                        jObj = new JSONObject();
                        jObj.put("0", sdf1.format(stdate));

                        returnColumnHeader += jObj.toString() + ",";
                        c1.set(stdate.getYear() + 1900, stdate.getMonth(),
                                stdate.getDate());
                        c1.add(Calendar.DATE, 7);
                        stdate = sdf1.parse(sdf1.format(c1.getTime()));
                    }
                } else if (period.equals("Monthly")) {
                    c1.set(stdate.getYear() + 1900, stdate.getMonth(), stdate.getDate());
                    stdate = sdf1.parse(sdf1.format(c1.getTime()));
                    while (stdate.compareTo(enddate) <= 0) {
                        jObj = new JSONObject();
                        jObj.put("0", periodWiseFormat.format(stdate));
                        returnColumnHeader += jObj.toString() + ",";
                        int lastDateOfMonth = c1.getActualMaximum(Calendar.DATE);
                        c1.set(stdate.getYear() + 1900, stdate.getMonth(),
                                lastDateOfMonth);
                        c1.add(Calendar.DATE, 1);
                        stdate = sdf1.parse(sdf1.format(c1.getTime()));
                    }
                } else if (period.equals("Yearly")) {
                    c1.set(stdate.getYear() + 1900, stdate.getMonth(), stdate.getDate());
                    stdate = sdf1.parse(sdf1.format(c1.getTime()));
                    while (stdate.compareTo(enddate) <= 0) {
                        jObj = new JSONObject();
                        jObj.put("0", periodWiseFormat.format(stdate));
                        returnColumnHeader += jObj.toString() + ",";
                        c1.set(stdate.getYear() + 1900, 11, 31);
                        c1.add(Calendar.DATE, 1);
                        stdate = sdf1.parse(sdf1.format(c1.getTime()));
                    }
                }
                jObj = new JSONObject();
                if (groupby.compareTo("task") == 0) {
                    if (reporttype.compareTo("timeline") == 0) 
                        jObj.put("1", TASK_TOTAL_WORK_TEXT);
                    else
                        jObj.put("1", TASK_TOTAL_COST_TEXT);
                } else {
                    if (reporttype.compareTo("timeline") == 0) 
                        jObj.put("1", TASK_TOTAL_WORK_TEXT);
                    else
                        jObj.put("1", TASK_TOTAL_COST_TEXT);
                }
                returnColumnHeader += jObj.toString() + ",";
            }
            if (returnColumnHeader.length() > 0) {
                returnColumnHeader = returnColumnHeader.substring(0,
                        returnColumnHeader.length() - 1);
            }
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
        return returnColumnHeader;
    }

    public static int nonWorkingDays(java.util.Date stdate, java.util.Date enddate, int[] NonWorkDays, String[] CmpHoliDays) {
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
            System.out.print(e.getMessage());
        }
        return NonWorkingDaysBetween;
    }

    public static int CountCmpHolidays(Date stdate, Date enddate, String[] CmpHoliDays) {
        int NonWorkingDays = 0;
        Calendar c1 = Calendar.getInstance();
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
    /*public static int nonWorkingDays(java.util.Date stdate, java.util.Date enddate,int []NonWorkDays,String []CmpHoliDays) {
    Calendar c1 = Calendar.getInstance();
    int NonWorkingDaysBetween = 0;
    try {
    c1.set(enddate.getYear() + 1900, enddate.getMonth(), enddate.getDate());
    c1.add(Calendar.DATE, 1);
    enddate = sdf1.parse(sdf1.format(c1.getTime()));
    int NumWeeks = (int) ((enddate.getTime() - stdate.getTime()) / (1000 * 60 * 60 * 24)) / 7;
    NonWorkingDaysBetween = NumWeeks * NonWorkDays.length;
    c1.set(stdate.getYear() + 1900, stdate.getMonth(), stdate.getDate());
    c1.add(Calendar.DATE, NumWeeks * 7);
    stdate = sdf1.parse(sdf1.format(c1.getTime()));
    int difference = (int) ((enddate.getTime() - stdate.getTime()) / (1000 * 60 * 60 * 24));
    while ((stdate.compareTo(enddate)) < 0) {
    if (Arrays.binarySearch(NonWorkDays, stdate.getDay())>=0 || Arrays.binarySearch(CmpHoliDays, sdf1.format(stdate))>=0) {
    NonWorkingDaysBetween += 1;
    }
    c1.set(stdate.getYear() + 1900, stdate.getMonth(), stdate.getDate());
    c1.add(Calendar.DATE, 1);
    stdate = sdf1.parse(sdf1.format(c1.getTime()));
    }
    } catch (Exception e) {
    System.out.print(e.getMessage());
    }
    return NonWorkingDaysBetween;
    }
     */

    public static int calculateWorkingDays(java.util.Date stdate,
            java.util.Date enddate, int[] NonWorkDays, String[] CmpHoliDays) {
        int nonworkingDays = nonWorkingDays(stdate, enddate, NonWorkDays, CmpHoliDays);
        int diff = (int) ((enddate.getTime() - stdate.getTime()) / (1000 * 60 * 60 * 24)) + 1;
        diff = diff - nonworkingDays;
        return diff;
    }

    public static String getReportData(Connection conn, String groupby, String reporttype,
            String period, String projectid, String userid) throws JSONException, ServiceException, JSONException {
        String returnStr = null;
        String cmpcurr = ProfileHandler.getCmpCurrFromProj(conn, projectid);
        String companyid = CompanyHandler.getCompanyByUser(conn, userid);
        String currSymbol = "";
        ResourceDAO rd = new ResourceDAOImpl();
        try{
            char a1= (char) Integer.parseInt(cmpcurr,16);
            currSymbol = Character.toString(a1);
        } catch (Exception e) {
            currSymbol = cmpcurr;
        }
        int count = 0;
        Map<String, Float> dayTotal = new HashMap<String, Float>();
        if (groupby.compareTo("task") == 0) {
            String tasks = projdb.getTask(conn, projectid, 0, 1000);
            if (tasks.compareTo("{data:{}}") != 0) {
                try {
                    JSONObject jobj = new JSONObject(tasks);
                    int nonworkweekArr[] = projdb.getNonWorkWeekDays(conn, projectid);
                    String holidayArr[] = projdb.getCompHolidays(conn, projectid, "");
                    returnStr = "[";
                    for (int i = 0; i < jobj.getJSONArray("data").length(); i++) {
                        String tempStr = "";
                        String dur = jobj.getJSONArray("data").getJSONObject(i).getString("duration");
                        if(!StringUtil.isNullOrEmpty(dur)){
                            JSONObject retjObj = new JSONObject();
                            retjObj.put("info", jobj.getJSONArray("data").getJSONObject(i).getString("taskname"));
                            Map<String, Float> periodWiseDuration = new HashMap<String, Float>();
                            if (period.equals("Daily")) {
                                periodWiseDuration = projectReport.getDailyDateWiseDuration(conn, jobj.getJSONArray("data").getJSONObject(i), nonworkweekArr, holidayArr, userid);
                            } else if (period.equals("Weekly")) {
                                periodWiseDuration = projectReport.getWeekWiseDuration(conn, jobj.getJSONArray("data").getJSONObject(i), nonworkweekArr, holidayArr, userid);
                            } else if (period.equals("Monthly")) {
                                periodWiseDuration = projectReport.getMonthWiseDuration(conn, jobj.getJSONArray("data").getJSONObject(i), nonworkweekArr, holidayArr, userid);
                            } else if (period.equals("Yearly")) {
                                periodWiseDuration = projectReport.getYearWiseDuration(jobj.getJSONArray("data").getJSONObject(i), nonworkweekArr, holidayArr);
                            }
                            String level = jobj.getJSONArray("data").getJSONObject(i).getString("level");
                            if (level.compareTo("") == 0) {
                                level = "0";
                            }
                            retjObj.put("level", level);
                            retjObj.put("flag", true);

                            float[] rateArray = new float[periodWiseDuration.size()];
                            for (int aCnt = 0; aCnt < periodWiseDuration.size(); aCnt++) {
                                rateArray[aCnt] = 0;
                            }
                            float duration = (float) projdb.getDuration(dur);
                            String resources = projdb.getBillableTaskResources(conn, jobj.getJSONArray("data").getJSONObject(i).getString("taskid"), projectid);
                            JSONObject resourcejObj = new JSONObject(resources);
                            if (resources.compareTo("{data:{}}") != 0) {
                                for (int cnt = 0; cnt < resourcejObj.getJSONArray("data").length(); cnt++) {
                                    JSONObject retjObj1 = new JSONObject();
                                    retjObj1.put("info", resourcejObj.getJSONArray("data").getJSONObject(cnt).getString("resourceName"));
                                    Iterator iterator = periodWiseDuration.entrySet().iterator();
                                    int k = 0;
                                    Map.Entry entry = null;
                                    float taskTotal = 0;
                                    while (iterator.hasNext()) {
                                        float value = 0;
                                        entry = (Map.Entry) iterator.next();
                                        float actValue = Float.parseFloat(resourcejObj.getJSONArray("data").getJSONObject(cnt).getString("wuvalue")) / 100;
                                        if(resourcejObj.getJSONArray("data").getJSONObject(cnt).getJSONObject("type").getInt("typeID") == Constants.WORK_RESOURCE){
                                            if (reporttype.compareTo("timeline") == 0) {
                                                value = Float.parseFloat(entry.getValue().toString());
                                                value = actValue * value;
                                                retjObj1.put(entry.getKey().toString(), value + " hrs");
                                            } else {
                                                value = Float.parseFloat(resourcejObj.getJSONArray("data").getJSONObject(cnt).getString("stdRate"))  * Float.parseFloat(entry.getValue().toString());
                                                value = actValue * value;
                                                retjObj1.put(entry.getKey().toString(), numberFormatter(value,currSymbol));
                                                rateArray[k] += value;
                                                k++;
                                            }
                                        } else if(resourcejObj.getJSONArray("data").getJSONObject(cnt).getJSONObject("type").getInt("typeID") == Constants.MATERIAL_RESOURCE){
                                            if (reporttype.compareTo("timeline") == 0) {
                                                retjObj1.put(entry.getKey().toString(), "NA");
                                            } else {
                                                value = Float.parseFloat(resourcejObj.getJSONArray("data").getJSONObject(cnt).getString("stdRate")) 
                                                            * Float.parseFloat(resourcejObj.getJSONArray("data").getJSONObject(cnt).getString("wuvalue"));
                                                value = value / duration;
                                                if((Float)entry.getValue() != Constants.WORK_HOURS)
                                                    value = value * ((Float)entry.getValue() / Constants.WORK_HOURS);
                                                retjObj1.put(entry.getKey().toString(),  numberFormatter(value,currSymbol));
                                                rateArray[k] += value;
                                                k++;
                                            }
                                        }
                                        if(!dayTotal.containsKey(entry.getKey().toString())){
                                            dayTotal.put(entry.getKey().toString(), (Float) value);
                                        } else {
                                            float totalVal = dayTotal.get(entry.getKey().toString());
                                            totalVal = totalVal + value;
                                            dayTotal.put(entry.getKey().toString(), totalVal);
                                        }
                                        taskTotal += value;
                                    }
                                    retjObj1.put("level", Integer.parseInt(level) + 1);
                                    retjObj1.put("flag", false);
                                    if (reporttype.compareTo("timeline") == 0)
                                        retjObj1.put(TASK_TOTAL_WORK_TEXT, Utilities.format(taskTotal, false) + " hrs");
                                    else
                                        retjObj1.put(TASK_TOTAL_COST_TEXT, numberFormatter(taskTotal, currSymbol));
                                    tempStr += retjObj1.toString() + ",";
                                }
                                float daySum = 0;
                                if (reporttype.equals("timeline")) {
                                    Iterator iterator = periodWiseDuration.entrySet().iterator();
                                    while (iterator.hasNext()) {
                                        Map.Entry entry = (Map.Entry) iterator.next();
                                        retjObj.put(entry.getKey().toString(), entry.getValue().toString() + " hrs");
                                        daySum += (Float) entry.getValue();
                                    }
                                } else if (reporttype.equals("costline")) {
                                    Iterator iterator = periodWiseDuration.entrySet().iterator();
                                    int k = 0;
                                    while (iterator.hasNext()) {
                                        Map.Entry entry = (Map.Entry) iterator.next();
                                        retjObj.put(entry.getKey().toString(), numberFormatter(rateArray[k], currSymbol));
                                        daySum += rateArray[k];
                                        k++;
                                    }
                                }
                                if (reporttype.equals("timeline"))
                                    retjObj.put(TASK_TOTAL_WORK_TEXT, Utilities.format(daySum, false) + " hrs");
                                else
                                    retjObj.put(TASK_TOTAL_COST_TEXT, numberFormatter(daySum, currSymbol));

                            }
                            tempStr = retjObj.toString() + "," + tempStr;
                            returnStr += tempStr;
                        }
                    }
                    float grandTotal = 0;
                    returnStr = returnStr.substring(0, returnStr.length() - 1);
                    returnStr = returnStr.concat("]");
                    String resourcenames = projdb.getProjectResources(conn, projectid, true);
                    JSONObject resourcejObj = new JSONObject(resourcenames);
                    JSONArray jarr = new JSONArray(returnStr);
                    JSONObject tempObj = new JSONObject();
                    tempObj.put("info", "Total Cost/day");
                    tempObj.put("level", "0");
                    tempObj.put("totalRow", true);
                    tempObj.put("flag", true);
                    Iterator iterator = dayTotal.entrySet().iterator();
                    Map.Entry entry = null;
                    while (iterator.hasNext()) {
                        entry = (Map.Entry) iterator.next();
                        if (reporttype.compareTo("timeline") == 0)
                            tempObj.put(entry.getKey().toString(), Utilities.format((Float)entry.getValue(), false) + " hrs");
                        else
                            tempObj.put(entry.getKey().toString(), numberFormatter((Float)entry.getValue(), currSymbol));
                        grandTotal += (Float)entry.getValue();
                    }
                    double cost = 0;
                    for (int i = 0; i < resourcejObj.getJSONArray("data").length(); i++) {
                        JSONObject temp = resourcejObj.getJSONArray("data").getJSONObject(i);
                        if(temp.getJSONObject("type").getInt("typeID") == Constants.COST_RESOURCE){
                            cost += (temp.getDouble("stdRate") * (temp.getDouble("wuvalue") /100));
                        }
                    }
                    grandTotal += cost;
                    if (reporttype.compareTo("timeline") == 0)
                        tempObj.put(TASK_TOTAL_WORK_TEXT, Utilities.format(grandTotal, false) + " hrs");
                    else
                        tempObj.put(TASK_TOTAL_COST_TEXT, numberFormatter(grandTotal, currSymbol));

                    jarr.put(tempObj);
                    returnStr = jarr.toString();
                    returnStr = "{data:" + returnStr + ",";
                    returnStr += "columnheader:[" + projectReport.ColumnHeader(conn, projectid, period, userid, groupby, reporttype) + "]}";
                    // return returnStr;
                } catch (Exception e) {
                    System.out.print(e.getMessage());
                }
            } else {
                returnStr = "{data:[],columnheader:[]}";
            }

        } else if (groupby.compareTo("resource") == 0) {
            if (period.equals("Daily") || period.equals("Weekly") || period.equals("Monthly") || period.equals("Yearly")) {
                if (reporttype.compareTo("timeline") == 0 || reporttype.compareTo("costline") == 0) {
//                    String resourcenames = dbcon.getProjResources(projectid);
                    String resourcenames = "{data:{}}";
                    if (reporttype.compareTo("timeline") == 0)
                        resourcenames = projdb.getProjectResources(conn, projectid, false);
                    else
                        resourcenames = projdb.getProjectResources(conn, projectid, true);
                    JSONObject resourcejObj = new JSONObject(resourcenames);
                    if (resourcenames.compareTo("{data:{}}") != 0) {
                        int nonworkweekArr[] = projdb.getNonWorkWeekDays(conn, projectid);
                        String holidayArr[] = projdb.getCompHolidays(conn, projectid, "");
                        returnStr = "[";
                        for (int i = 0; i < resourcejObj.getJSONArray("data").length(); i++) {
                            String resource = resourcejObj.getJSONArray("data").getJSONObject(i).getString("resourceID");
                            JSONObject tempObj = new JSONObject();
                            tempObj.put("info", resourcejObj.getJSONArray("data").getJSONObject(i).getString("resourceName"));
                            tempObj.put("level", "0");
                            tempObj.put("flag", true);
                            if(resourcejObj.getJSONArray("data").getJSONObject(i).getJSONObject("type").getInt("typeID") == Constants.COST_RESOURCE){
                                float value = Float.parseFloat(resourcejObj.getJSONArray("data").getJSONObject(i).getString("stdRate"));
                                float actValue = Float.parseFloat(resourcejObj.getJSONArray("data").getJSONObject(i).getString("wuvalue")) / 100;
                                if (reporttype.compareTo("timeline") == 0) {
                                    tempObj.put(TASK_TOTAL_WORK_TEXT, "0 hrs");
                                } else {
                                    value = actValue * value;
                                    tempObj.put(TASK_TOTAL_COST_TEXT, numberFormatter(value, currSymbol));
                                }
                            }
                            returnStr += tempObj.toString() + ",";
                            String taskinfo = projdb.getResourceRelatedTask(conn, resource, projectid, companyid);
                            if (taskinfo.compareTo("{data:{}}") != 0) {
                                JSONObject taskObj = new JSONObject(taskinfo);

                                for (int j = 0; j < taskObj.getJSONArray("data").length(); j++) {
                                    tempObj = new JSONObject();
                                    Map<String, Float> periodWiseDuration = new HashMap<String, Float>();
                                    if (period.equals("Daily")) {
                                        periodWiseDuration = projectReport.getDailyDateWiseDuration(conn, taskObj.getJSONArray("data").getJSONObject(j), nonworkweekArr, holidayArr, userid);
                                    } else if (period.equals("Weekly")) {
                                        periodWiseDuration = projectReport.getWeekWiseDuration(conn, taskObj.getJSONArray("data").getJSONObject(j), nonworkweekArr, holidayArr, userid);
                                    } else if (period.equals("Monthly")) {
                                        periodWiseDuration = projectReport.getMonthWiseDuration(conn, taskObj.getJSONArray("data").getJSONObject(j), nonworkweekArr, holidayArr, userid);
                                    } else if (period.equals("Yearly")) {
                                        periodWiseDuration = projectReport.getYearWiseDuration(taskObj.getJSONArray("data").getJSONObject(j), nonworkweekArr, holidayArr);
                                    }
                                    String dur = taskObj.getJSONArray("data").getJSONObject(j).getString("duration");
                                    float duration = (float) projdb.getDuration(dur);
                                    Iterator iterator = periodWiseDuration.entrySet().iterator();
                                    Map.Entry entry = null;
                                    float taskTotal = 0;
                                    while (iterator.hasNext()) {
                                        entry = (Map.Entry) iterator.next();
                                        float value = 0;
                                        float actValue = Float.parseFloat(resourcejObj.getJSONArray("data").getJSONObject(i).getString("wuvalue")) / 100;
                                        if(resourcejObj.getJSONArray("data").getJSONObject(i).getJSONObject("type").getInt("typeID") == Constants.WORK_RESOURCE){
                                            if (reporttype.compareTo("timeline") == 0) {
                                                value = Float.parseFloat(entry.getValue().toString());
                                                value = actValue * value;
                                                tempObj.put(entry.getKey().toString(), value + " hrs");
                                            } else {
                                                value = Float.parseFloat(resourcejObj.getJSONArray("data").getJSONObject(i).getString("stdRate")) * 
                                                        Float.parseFloat(entry.getValue().toString());
                                                value = actValue * value;
                                                tempObj.put(entry.getKey().toString(),  numberFormatter(value,currSymbol));
                                            }
                                        } else if(resourcejObj.getJSONArray("data").getJSONObject(i).getJSONObject("type").getInt("typeID") == Constants.MATERIAL_RESOURCE){
                                            if (reporttype.compareTo("timeline") == 0) {
                                                tempObj.put(entry.getKey().toString(), "0 hrs");
                                            } else {
                                                value = Float.parseFloat(resourcejObj.getJSONArray("data").getJSONObject(i).getString("stdRate")) 
                                                        * Float.parseFloat(resourcejObj.getJSONArray("data").getJSONObject(i).getString("wuvalue"));
                                                value = value / duration;
                                                if((Float)entry.getValue() != Constants.WORK_HOURS)
                                                    value = value * ((Float)entry.getValue() / Constants.WORK_HOURS);
                                                tempObj.put(entry.getKey().toString(),  numberFormatter(value,currSymbol));
                                            }
                                        }
                                        if(!dayTotal.containsKey(entry.getKey().toString())){
                                            dayTotal.put(entry.getKey().toString(), (Float) value);
                                        } else {
                                            float totalVal = dayTotal.get(entry.getKey().toString());
                                            totalVal = totalVal + value;
                                            dayTotal.put(entry.getKey().toString(), totalVal);
                                        }
                                        taskTotal += value;
                                    }
                                    tempObj.put("info", taskObj.getJSONArray("data").getJSONObject(j).getString("taskname"));
                                    tempObj.put("level", "1");
                                    tempObj.put("flag", false);
                                    if (reporttype.compareTo("timeline") == 0)
                                        tempObj.put(TASK_TOTAL_WORK_TEXT, Utilities.format(taskTotal, false) + " hrs");
                                    else
                                        tempObj.put(TASK_TOTAL_COST_TEXT, numberFormatter(taskTotal, currSymbol));
                                    returnStr += tempObj.toString() + ",";
                                }
                            }
                        }
                        float grandTotal = 0;
                        returnStr = returnStr.substring(0, returnStr.length() - 1);
                        returnStr = returnStr.concat("]");
                        JSONArray jarr = new JSONArray(returnStr);
                        JSONObject tempObj = new JSONObject();
                        tempObj.put("info", "Total Cost/day");
                        tempObj.put("level", "0");
                        tempObj.put("totalRow", true);
                        tempObj.put("flag", true);
                        Iterator iterator = dayTotal.entrySet().iterator();
                        Map.Entry entry = null;
                        while (iterator.hasNext()) {
                            entry = (Map.Entry) iterator.next();
                            if (reporttype.compareTo("timeline") == 0)
                                tempObj.put(entry.getKey().toString(), Utilities.format((Float)entry.getValue(), false) + " hrs");
                            else
                                tempObj.put(entry.getKey().toString(), numberFormatter((Float)entry.getValue(), currSymbol));
                            grandTotal += (Float)entry.getValue();
                        }
                        double cost = 0;
                        for (int i = 0; i < resourcejObj.getJSONArray("data").length(); i++) {
                            JSONObject temp = resourcejObj.getJSONArray("data").getJSONObject(i);
                            if(temp.getJSONObject("type").getInt("typeID") == Constants.COST_RESOURCE){
                                cost += (temp.getDouble("stdRate") * (temp.getDouble("wuvalue") / 100));
                            }
                        }
                        grandTotal += cost;
                        if (reporttype.compareTo("timeline") == 0)
                            tempObj.put(TASK_TOTAL_WORK_TEXT, Utilities.format(grandTotal, false) + " hrs");
                        else
                            tempObj.put(TASK_TOTAL_COST_TEXT, numberFormatter(grandTotal, currSymbol));
                       
                        jarr.put(tempObj);
                        returnStr = jarr.toString();
                        returnStr = "{data:" + returnStr + ",";
                        returnStr += "columnheader:[" + projectReport.ColumnHeader(conn, projectid, period, userid, groupby, reporttype) + "]}";

                    }
                }
            }
        }
        return returnStr;
    }

    public static String getReportData(Connection conn, HttpServletRequest request, String userid)
            throws JSONException, ServiceException {
        String groupby = request.getParameter("groupby");
        String reporttype = request.getParameter("reporttype");
        String period = request.getParameter("period");
        String projectid = request.getParameter("projectid");
        return projectReport.getReportData(conn, groupby, reporttype, period,
                projectid, userid);
    }

    public static String numberFormatter(double values, String compSymbol) {
        NumberFormat numberFormatter;
        java.util.Locale currentLocale = java.util.Locale.US;
        numberFormatter = NumberFormat.getNumberInstance(currentLocale);
        numberFormatter.setMinimumFractionDigits(0);
        numberFormatter.setMaximumFractionDigits(2);
        return (compSymbol + " " + numberFormatter.format(values));
    }
    
    public static String getProjectSummaryString(String data) throws JSONException {
        String returnStr = "", temp = "";
        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd");
        java.util.Date today = new java.util.Date();
        try {
            JSONObject jobj = new JSONObject(data);
            int variance = 0;
            for (int i = 0; i < jobj.getJSONArray("data").length(); i++) {
                returnStr = "<br><div style=\"width:100%;\" align=\"center\"><b>Project Summary as on " + df.format(today) + " </b></div><br>";
                returnStr += "<div id=\"table1\" style=\"margin:20px 20px 20px 20px;\"><table style=\"border:1px solid #EEEEEE; width:100%; background-color: #EEEEEE; color: #000000\">"
                        + "<tr style=\"width:100%;\"><td style=\"width:100px; border-right:1px solid #EEEEEE;\"><div style=\"padding-left:10px;\"><b> Dates</b></div></td></table>"
                        + "<table style=\"border:1px solid #EEEEEE; width:100%; color: #000000\">"
                        + "<tr style=\"width:100%;\">"
                        + "<td style=\"width:25%; \">Project Start</td>"
                        + "<td style=\"width:25%; text-align:right; padding-right:30px;\">" + jobj.getJSONArray("data").getJSONObject(i).getString("Project Start Date") + "</td>"
                        + "<td style=\"width:25%; \">Project Finish</td>"
                        + "<td style=\"width:25%; text-align:right;\">" + jobj.getJSONArray("data").getJSONObject(i).getString("Project End Date") + "</td></tr>"
                        + "<tr style=\"width:100%;\">"
                        + "<td style=\"width:25%;\">Baseline Start</td>"
                        + "<td style=\"width:25%; text-align:right; padding-right:30px;\">" + jobj.getJSONArray("data").getJSONObject(i).getString("Baseline Start Date") + "</td>"
                        + "<td style=\"width:25%;\">Baseline Finish</td>"
                        + "<td style=\"width:25%; text-align:right;\">" + jobj.getJSONArray("data").getJSONObject(i).getString("Baseline End Date") + "</td></tr>"
                        + "<tr style=\"width:100%;\">"
                        + "<td style=\"width:25%; border-bottom:1px solid #000000; padding-bottom:5px;\">Actual Start</td>"
                        + "<td style=\"width:25%; text-align:right; padding-right:30px;  border-bottom:1px solid #000000; padding-bottom:5px;\">" + jobj.getJSONArray("data").getJSONObject(i).getString("Project Actual Start Date") + "</td>"
                        + "<td style=\"width:25%; border-bottom:1px solid #000000; padding-bottom:5px;\">Actual Finish</td>"
                        + "<td style=\"width:25%; text-align:right; border-bottom:1px solid #000000; padding-bottom:5px;\">" + jobj.getJSONArray("data").getJSONObject(i).getString("Project End Date") + "</td></tr>";

                variance = projdb.getDaysDiff(jobj.getJSONArray("data").getJSONObject(i).getString("projectstartdateforvariance"), jobj.getJSONArray("data").getJSONObject(i).getString("baselinestartdateforvariance"));
                returnStr += "<tr style=\"width:100%;\">"
                        + "<td style=\"width:25%; padding-top:3px;\">Start Variance</td>"
                        + "<td style=\"width:25%; text-align:right; padding-right:30px; padding-top:3px;\">" + variance + " days</td>";

                if (jobj.getJSONArray("data").getJSONObject(i).getString("projectenddateforvariance").compareTo("NA") == 0 || jobj.getJSONArray("data").getJSONObject(i).getString("baselineenddateforvariance").compareTo("NA") == 0) {
                    temp = "NA";
                } else {
                    variance = projdb.getDaysDiff(jobj.getJSONArray("data").getJSONObject(i).getString("projectenddateforvariance"), jobj.getJSONArray("data").getJSONObject(i).getString("baselineenddateforvariance"));
                    temp = "" + variance + " days";
                }
                returnStr += "<td style=\"width:25%; padding-top:3px;\">End Variance</td>"
                        + "<td style=\"width:25%; text-align:right; padding-top:3px;\">" + temp + "</td>"
                        + "</tr></table></div>";
                returnStr = getSummaryFor(returnStr, jobj.getJSONArray("data").getJSONObject(i), "duration");
                returnStr = getSummaryFor(returnStr, jobj.getJSONArray("data").getJSONObject(i), "cost");
                returnStr = getSummaryFor(returnStr, jobj.getJSONArray("data").getJSONObject(i), "work");
                variance = (Integer.parseInt(jobj.getJSONArray("data").getJSONObject(i).getString("Unstarted Tasks"))
                        + Integer.parseInt(jobj.getJSONArray("data").getJSONObject(i).getString("Completed Tasks"))
                        + Integer.parseInt(jobj.getJSONArray("data").getJSONObject(i).getString("Inprogress Tasks")));
                variance = Integer.parseInt(jobj.getJSONArray("data").getJSONObject(0).getString("Total Tasks")) - variance;
                returnStr += "<div id=\"table_bottom_left\" style=\"margin:20px 0px 20px 0px; float:left; width:49%;\">"
                        + "<table style=\"border:1px solid #EEEEEE; width:100%; background-color: #EEEEEE; color: #000000\">"
                        + "<tr style=\"width:100%;\"><td style=\"width:100px; border-right:1px solid #EEEEEE;\"><div style=\"padding-left:20px;\"><b> Tasks</b></div></td>"
                        + "</table><table style=\"border:1px solid #EEEEEE; width:100%; color: #000000\">"
                        + "<tr style=\"width:100%;\">"
                        + "<td style=\"width:50%; padding-left:20px;\">Unstarted Tasks</td>"
                        + "<td style=\"width:50%; text-align:right; padding-right:30px; \">" + jobj.getJSONArray("data").getJSONObject(i).getString("Unstarted Tasks") + "</td></tr>"
                        + "<tr style=\"width:100%;\">"
                        + "<td style=\"width:50%; padding-left:20px;\">Completed Tasks</td>"
                        + "<td style=\"width:50%; text-align:right; padding-right:30px; \">" + jobj.getJSONArray("data").getJSONObject(i).getString("Completed Tasks") + "</td></tr>"
                        + "<tr style=\"width:100%;\">"
                        + "<td style=\"width:50%; padding-left:20px;\">Inprogress Tasks</td>"
                        + "<td style=\"width:50%; text-align:right; padding-right:30px; \">" + jobj.getJSONArray("data").getJSONObject(i).getString("Inprogress Tasks")
                        + "</td></tr>"
                        + "<tr style=\"width:100%;\">"
                        + "<td style=\"width:50%; border-bottom:1px solid #000000; padding-bottom:3px; padding-left:20px;\">Parent Tasks</td>"
                        + "<td style=\"width:50%; text-align:right; padding-right:30px; padding-bottom:3px; border-bottom:1px solid #000000;\">" + variance
                        + "</td></tr>"
                        + "<tr style=\"width:100%;\">"
                        + "<td style=\"width:50%; padding-top:3px; padding-left:20px;\">Total Tasks</td>";
                returnStr += "<td style=\"width:50%; text-align:right; padding-right:30px; padding-top:3px;\">" + Integer.parseInt(jobj.getJSONArray("data").getJSONObject(i).getString("Total Tasks")) + "</td>"
                        + "</tr></table></div>";
            }
            variance = 0;
            returnStr += "<div id=\"table_bottom_right\" style=\"margin:20px 0px 20px 0px;float:right; width:51%;\">"
                    + "<table style=\"border:1px solid #EEEEEE; width:100%; background-color: #EEEEEE; color: #000000\">"
                    + "<tr style=\"width:100%;\"><td style=\"width:100px; border-right:1px solid #EEEEEE;\"><div style=\"padding-left:10px;\"><b> Resources</b></div></td></table>"
                    + "<table style=\"border:1px solid #EEEEEE; width:100%; color: #000000\">";
            for (int i = 0; i < jobj.getJSONArray("resources").length(); i++) {
                if (i + 1 == jobj.getJSONArray("resources").length()) {
                    returnStr += "<tr style=\"width:100%;\">"
                            + "<td style=\"width:50%; border-bottom:1px solid #000000; padding-bottom:3px;padding-left:5px;\">" + jobj.getJSONArray("resources").getJSONObject(i).getString("type") + "</td>"
                            + "<td style=\"width:50%; text-align:right; padding-right:30px; padding-bottom:3px; border-bottom:1px solid #000000;\">" + jobj.getJSONArray("resources").getJSONObject(i).getString("count") + "</td></tr>";
                    variance += Integer.parseInt(jobj.getJSONArray("resources").getJSONObject(i).getString("count"));
                } else {
                    returnStr += "<tr style=\"width:100%;\">"
                            + "<td style=\"width:50%; padding-bottom:3px;padding-left:5px;\">" + jobj.getJSONArray("resources").getJSONObject(i).getString("type") + "</td>"
                            + "<td style=\"width:50%; text-align:right; padding-right:30px; padding-bottom:3px;\">" + jobj.getJSONArray("resources").getJSONObject(i).getString("count") + "</td></tr>";
                    variance += Integer.parseInt(jobj.getJSONArray("resources").getJSONObject(i).getString("count"));
                }
            }
            returnStr += "<tr style=\"width:100%;\">"
                    + "<td style=\"width:50%; padding-top:3px; padding-left:5px;\">Total Resources</td>"
                    + "<td style=\"width:50%; text-align:right; padding-right:30px; padding-top:3px;\">" + variance + "</td>"
                    + "</tr></table></div>";

        } catch (JSONException e) {
            returnStr = " ";
        } finally {
            return returnStr;
        }
    }

    public static String getSummaryFor(String returnStr, JSONObject jobj, String whatfor) throws JSONException {
        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd");
        String header = "", suffix = "", sched = "", rem = "", act = "", basel = "", percent = "", varStr = "";
        int tableno = 0;
        double variance = 0.0;
        try {
            if (whatfor.equals("duration")) {
                header = "Duration";
                tableno = 1;
                suffix = " days";
                sched = jobj.getString("Scheduled Duration");
                rem = jobj.getString("Remaining Duration");
                act = jobj.getString("Actual Duration");
                basel = jobj.getString("Baseline Duration");
            } else if (whatfor.equals("cost")) {
                header = "Cost";
                tableno = 3;
                suffix = currSymbol + " ";
                sched = jobj.getString("Scheduled Cost");
                rem = jobj.getString("Remaining Cost");
                act = jobj.getString("Actual Cost");
                basel = jobj.getString("Baseline Cost");
            } else {
                header = "Work";
                tableno = 2;
                suffix = " hrs";
                sched = jobj.getString("Scheduled Work");
                rem = jobj.getString("Remaining Work");
                act = jobj.getString("Actual Work");
                basel = jobj.getString("Baseline Work");
                percent = jobj.getString("Percent Complete");
            }
            variance = Double.parseDouble(sched) - Double.parseDouble(basel);
            if (whatfor.equals("duration") || whatfor.equals("work")) {
                sched = sched + suffix;
                rem = rem + suffix;
                if (whatfor.equals("duration") && rem.compareTo("NA days") == 0) {
                    rem = rem.substring(0, rem.indexOf(" days"));
                }
                act = act + suffix;
                basel = basel + suffix;
                percent = percent.concat("%");
                varStr = variance + suffix;
            } else {
                sched = suffix + sched;
                rem = suffix + rem;
                act = suffix + act;
                basel = suffix + basel;
                varStr = suffix + variance;
            }
            returnStr += "<div id=\"table" + tableno + "\" style=\"margin:20px 20px 20px 20px;\">"
                    + "<table style=\"border:1px solid #EEEEEE; width:100%; background-color: #EEEEEE; color: #000000\">"
                    + "<tr style=\"width:100%;\"><td style=\"width:100px; border-right:1px solid #EEEEEE;\"><div style=\"padding-left:10px;\"><b>" + header + "</b></div></td></table>"
                    + "<table style=\"border:1px solid #EEEEEE; width:100%; color: #000000\">"
                    + "<tr style=\"width:100%;\">"
                    + "<td style=\"width:25%;\">Scheduled</td>"
                    + "<td style=\"width:25%; text-align:right; padding-right:30px; \">" + sched + "</td>"
                    + "<td style=\"width:25%;\">Remaining</td>"
                    + "<td style=\"width:25%; text-align:right;\">" + rem + "</td></tr>"
                    + "<tr style=\"width:100%;\">"
                    + "<td style=\"width:25%; border-bottom:1px solid #000000; padding-bottom:5px;\">Baseline</td>"
                    + "<td style=\"width:25%; text-align:right; padding-right:30px; border-bottom:1px solid #000000; padding-bottom:5px;\">" + basel + "</td>"
                    + "<td style=\"width:25%;\">Actual</td>"
                    + "<td style=\"width:25%; text-align:right;\">" + act + "</td></tr>"
                    + "<tr style=\"width:100%;\">"
                    + "<td style=\"width:25%; padding-top:3px;\">" + header + " Variance</td>"
                    + "<td style=\"width:25%; text-align:right; padding-right:30px; padding-top:3px;\">" + varStr + "</td>";
            if (whatfor.equals("work")) {
                returnStr += "<td style=\"width:25%;\">Percent Complete</td>"
                        + "<td style=\"width:25%; text-align:right;\">" + percent + "</td>";
            }
            returnStr += "</tr></table></div>";
        } catch (Exception e) {
            returnStr = " ";
        }
        return returnStr;
    }
}
