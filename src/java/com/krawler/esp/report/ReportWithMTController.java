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
package com.krawler.esp.report;

import com.krawler.common.customcolumn.CustomColumn;
import com.krawler.common.permission.PermissionConstants;
import com.krawler.common.permission.PermissionManager;
import com.krawler.esp.report.custom.CustomReport;
import com.krawler.esp.report.custom.QueryFilter;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.KWLErrorMsgs;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.Utilities;
import com.krawler.database.DbPool.Connection;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.esp.handlers.AuditTrail;
import com.krawler.esp.handlers.AuthHandler;
import com.krawler.esp.project.meter.HealthMeterDAO;
import com.krawler.esp.project.meter.HealthMeterDAOImpl;
import com.krawler.esp.project.task.Task;
import com.krawler.esp.project.task.TaskDAO;
import com.krawler.esp.project.task.TaskDAOImpl;
import com.krawler.esp.report.custom.ReportHelper;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Abhay Kulkarni
 */
public class ReportWithMTController extends AbstractReportController {

    public ReportWithMTController(String companyid, String loginID) {
        this.loginID = loginID;
        this.companyid = companyid;
    }

    @Override
    public String getReportsColumnAll(Connection conn, HttpServletRequest request) throws ServiceException {
        String res = "";
        CustomReport cr = ReportHelper.getCustomReportWithMilestoneTimeline(companyid, loginID);
        if (request.getParameter("type") != null) {
            String type = request.getParameter("type");
            List<ReportColumn> rc = cr.getColumnsMetaData(conn, type);
            res = Utilities.listToGridJson(rc, rc.size(), ReportColumn.class);
        }
        return res;
    }

    @Override
    public String insertReport(Connection conn, HttpServletRequest request) throws ServiceException {
        String res = KWLErrorMsgs.rsFalseNoData;
        try {
            boolean isMilestones = false, isPaging = false;
            String includeMilestones = request.getParameter("includemilestones");
            if (!StringUtil.isNullOrEmpty(includeMilestones) && includeMilestones.compareTo("on") == 0) {
                isMilestones = true;
            }
            String enablePaging = request.getParameter("enablepaging");
            if (!StringUtil.isNullOrEmpty(enablePaging) && enablePaging.compareTo("on") == 0) {
                isPaging = true;
            }
            CustomReport cr = ReportHelper.getCustomReportWithMilestoneTimeline(companyid, loginID, isPaging, isMilestones);
            String rid = UUID.randomUUID().toString();
            String rname = request.getParameter("reportname");
            String desc = request.getParameter("description");
            String type = request.getParameter("type");
            String uid = AuthHandler.getUserid(request);
            cr.insertReport(conn, rid, rname, type, desc, uid);
            JSONArray jColumns = new JSONArray(request.getParameter("columns"));
            List<ReportColumn> columns = ReportHelper.getReportColumn(jColumns);
            cr.insertColumns(conn, columns, rid);
            String params = AuthHandler.getAuthor(conn, uid) + " (" + AuthHandler.getUserName(request) + ")" + "," + rname;
            AuditTrail.insertLog(conn, "406", uid, "", "", companyid, params, AuthHandler.getIPAddress(request), 0);
            res = KWLErrorMsgs.rsSuccessTrueNoData;
            this.formSubmit = true;
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("Reportcontroller.inserReport", ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("Reportcontroller.inserReport", ex);
        }
        return res;
    }

    @Override
    public String deleteReport(Connection conn, HttpServletRequest request) throws ServiceException {
        String res = KWLErrorMsgs.rsFalseNoData;
        try {
            CustomReport cr = ReportHelper.getCustomReportWithMilestoneTimeline(companyid, loginID);
            cr.deleteCustomReport(conn, request.getParameter("reportId"));
            res = KWLErrorMsgs.rsSuccessTrueNoData;
            String uid = AuthHandler.getUserid(request);
            String params = AuthHandler.getAuthor(conn, uid) + " (" + AuthHandler.getUserName(request) + ")" + "," + request.getParameter("reportname");
            AuditTrail.insertLog(conn, "408", uid, "", "", companyid, params, AuthHandler.getIPAddress(request), 0);
            this.formSubmit = true;
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("ReportController.deleteReport", ex);
        }
        return res;
    }

    @Override
    public String getAllReports(Connection conn, HttpServletRequest request) throws ServiceException {
        CustomReport cr = ReportHelper.getCustomReportWithMilestoneTimeline(companyid, loginID);
        List<Reports> rep = cr.getAllReports(conn);
        if (rep.isEmpty()) {
            return "{data:[]}";
        }
        String res = Utilities.listToGridJson(rep, rep.size(), Reports.class);
        return res;
    }

    @Override
    public boolean isFormSubmit() {
        return formSubmit;
    }
    
    @Override
    public String getReportsColumn(Connection conn, HttpServletRequest request) throws ServiceException {
        try {
            String repId = request.getParameter("reportid");
            companyid = AuthHandler.getCompanyid(request);
            CustomReport cr = ReportHelper.getCustomReportWithMilestoneTimeline(companyid, loginID);
            List<ReportColumn> columns = cr.getReportsColumns(conn, repId);
            Collections.sort(columns);
            String colJSON = Utilities.listToGridJson(columns, columns.size(), ReportColumn.class);

            Reports r = cr.getReportConfig(conn, repId);
            if (r.isMilestone()) {
                JSONObject cols = new JSONObject(colJSON);
                JSONArray colsArr = cols.getJSONArray("data");
                int mlColCount = 0;
                PermissionManager pm = new PermissionManager();
                boolean permissionForAllProjects = pm.isPermission(conn, loginID, PermissionConstants.Feature.CUSTOM_REPORTS, PermissionConstants.Activity.CUSTOM_REPORT_VIEW);
                ArrayList<String> params = new ArrayList<String>();
                if(permissionForAllProjects){
                    params.add(companyid);
                }else{
                    params.add(loginID);
                    params.add(companyid);
                }
                DbResults rs = DbUtil.executeQuery(conn, ReportsQuery.getMaxCountOfMilestone(permissionForAllProjects), params.toArray());
                if (rs.next()) {
                    if (!rs.isNull("count")) {
                        mlColCount = rs.getInt("count");
                    }
                }

                ReportColumn rcHealth = new ReportColumn();
                rcHealth.setColumnID("project_health");
                rcHealth.setReportID(repId);
                rcHealth.setHeader("Project Health");
                rcHealth.setDisplayHeader("Project Health");
                rcHealth.setName("projecthealth");
                rcHealth.setDataIndex("projecthealth");
                rcHealth.setTableName("");
                rcHealth.setFieldName("");
                rcHealth.setSummary("null");
                rcHealth.setModule("Project");
                rcHealth.setRenderer("projectHealth");
                rcHealth.setQuickSearch(false);
                rcHealth.setIsMandatory(false);
                rcHealth.setType(CustomColumn.TEXT);
                rcHealth.setDisplayOrder(colsArr.length() + 1);
                rcHealth.setHeaderkey("pm.project.home.health.text");
                JSONObject temp = new JSONObject(rcHealth.toString());
                colsArr.put(temp);

                if (mlColCount != 0) {
                    for (int i = 1; i < mlColCount + 1; i++) {
                        ReportColumn rc = new ReportColumn();
                        rc.setColumnID("defaultmilestone".concat(String.valueOf(i)));
                        rc.setReportID(repId);
                        rc.setHeader("milestone".concat(String.valueOf(i)));
                        rc.setDisplayHeader("Milestone ".concat(String.valueOf(i)));
                        rc.setName("milestone".concat(String.valueOf(i)));
                        rc.setDataIndex("milestone".concat(String.valueOf(i)));
                        rc.setTableName("");
                        rc.setFieldName("milestone".concat(String.valueOf(i)));
                        rc.setSummary("null");
                        rc.setModule("Project");
                        rc.setRenderer("milestone");
                        rc.setQuickSearch(false);
                        rc.setIsMandatory(false);
                        rc.setType(CustomColumn.TEXT);
                        rc.setDisplayOrder(columns.size() + (i - 1));
                        rc.setHeaderkey("pm.project.report.milestone");

                        temp = new JSONObject(rc.toString());
                        colsArr.put(temp);
                    }
                }
                cols.remove("data");
                cols.put("data", colsArr);
                colJSON = cols.toString();
            }
            return colJSON;
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("CustomColumnController.getReportsColumn()", ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("CustomColumnController.getReportsColumn()", ex);
        }
    }

    @Override
    public String getReport(Connection conn, HttpServletRequest request) throws ServiceException {
        DateFormat df = new SimpleDateFormat(Constants.ONLY_DATE_FORMAT);
        HealthMeterDAO healthMeterDAO = new HealthMeterDAOImpl();
        try {
            DateFormat userDateFormat = new SimpleDateFormat(Constants.LONG_DATE_FORMAT);
            Calendar c = Calendar.getInstance();
            Calendar curCal = Calendar.getInstance();
            curCal.setTime(df.parse(df.format(new Date())));
            ReportBean bean = getReport(conn, request, false);
            String reportJSONString = bean.toString();
            JSONObject jo = new JSONObject(reportJSONString);
            jo.put("count", bean.getTotalCount());

            TaskDAO taskDAO = new TaskDAOImpl();
            JSONArray projectData = jo.getJSONArray("data");
            for (int i = 0; i < projectData.length(); i++) {
                JSONObject project = projectData.getJSONObject(i);
                String projectID = project.getString("projectid");
                List<Task> ml = taskDAO.getMilestones(conn, projectID, null);
                for (int j = 0; j < ml.size(); j++) {

                    Task task = ml.get(j);
                    JSONObject tempTask = new JSONObject();
                    tempTask.put("taskname", task.getTaskName());
                    tempTask.put("taskindex", task.getTaskIndex());
                    tempTask.put("percentcomplete", task.getPercentComplete());
                    tempTask.put("startdate", userDateFormat.format(task.getStartDate()));
                    c.setTime(task.getStartDate());
                    
                    if (task.getPercentComplete() == 100) {
                        tempTask.put("completedml", true);
                    } else if (c.compareTo(curCal) < 0) {
                        tempTask.put("overdueml", true);
                    } else if (c.compareTo(curCal) == 0) {
                        tempTask.put("inprogressml", true);
                    } else if (task.getPercentComplete() == 0) {// when c > curCal
                        tempTask.put("futureml", true);
                    } else if (task.getPercentComplete() > 0) {//when c > curCal
                        tempTask.put("inprogressml", true);
                    }
                    project.put("milestone".concat(String.valueOf(j + 1)), tempTask.toString());
                }
                Map base = healthMeterDAO.getBaseLineMeter(conn, projectID);
                int healthStatus = healthMeterDAO.getHealthMeter(conn, projectID).getStatus(base);
                project.put("projecthealth", healthStatus);
            }
            jo.remove("data");
            jo.put("data", projectData);
            return jo.toString();
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("CustomColumnController.getReportsColumn()", ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("CustomColumnController.getReportsColumn()", ex);
        }
    }

    @Override
    public ByteArrayOutputStream exportReport(Connection conn, HttpServletRequest request) throws ServiceException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ReportBean bean = getReport(conn, request, true);
            os.write(bean.toCSV(true).getBytes());
            os.close();
        } catch (IOException ex) {
            throw ServiceException.FAILURE("ReportController.exportReport", ex);
        }
        return os;
    }

    @Override
    public ReportBean getReport(Connection conn, HttpServletRequest request, boolean export) throws ServiceException {
        String repId = request.getParameter("reportid");
        String ss = "";
        int limit = Constants.Default_Int_Initializer, start = 0;
        CustomReport cr = ReportHelper.getCustomReportWithMilestoneTimeline(companyid, loginID);
        List<ReportColumn> columns = cr.getReportsColumns(conn, repId);
        if (!export) {
            ReportColumn rc = new ReportColumn();
            rc.setColumnID("project_projectid");
            rc.setReportID(repId);
            rc.setHeader("projectid");
            rc.setDisplayHeader("Project ID");
            rc.setName("projectid");
            rc.setDataIndex("projectid");
            rc.setTableName("project");
            rc.setFieldName("projectid");
            rc.setSummary(null);
            rc.setModule("Project");
            rc.setRenderer("");
            rc.setQuickSearch(false);
            rc.setIsMandatory(false);
            rc.setType(CustomColumn.TEXT);
            rc.setDisplayOrder(0);
            columns.add(rc);
        }
        Reports r = cr.getReportConfig(conn, repId);
        if (!StringUtil.isNullOrEmpty(request.getParameter("ss"))) {
            ss = request.getParameter("ss");
        }
        if (r.isPaging()) {
            if (!StringUtil.isNullOrEmpty(request.getParameter("start"))) {
                start = Integer.parseInt(request.getParameter("start"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("limit"))) {
                limit = Integer.parseInt(request.getParameter("limit"));
            }
        }
        ReportQueryBuilder query = new MySqlReportQueryBuilder(columns, ss, limit, start);
        if (!StringUtil.isNullOrEmpty(request.getParameter("filter"))) {
            try {
                JSONArray jColumns = new JSONArray(request.getParameter("filter"));
                int c = jColumns.length();
                for (int i = 0; i < c; i++) {
                    JSONObject jobj = jColumns.getJSONObject(i);
                    QueryFilter qf = ReportHelper.getFilterObject(jobj);
                    query.addFilter(qf);
                }
            } catch (JSONException ex) {
                throw ServiceException.FAILURE("ReportController.getReport", ex);
            }
        }
        return cr.getReportData(conn, query);
    }
    private String loginID;
    private String companyid;
    private boolean formSubmit;

    @Override
    public String getSelectedReportConfig(Connection conn, HttpServletRequest request) throws ServiceException {
        JSONObject jo = new JSONObject();
        String reportID = "", result = "";
        try {
            jo.put("success", true);
            result = jo.toString();

            DbResults rs = DbUtil.executeQuery(conn, ReportsQuery.getSelectedReport(), loginID);
            if (rs.next()) {
                reportID = rs.getString("reportid");
            }

            if (!StringUtil.isNullOrEmpty(reportID)) {
                CustomReport cr = ReportHelper.getCustomReportWithMilestoneTimeline(companyid, loginID);
                Reports r = cr.getReportConfig(conn, reportID);
                jo.put("data", r.toString());
                result = jo.toString();
            } else {
                jo.remove("success");
                jo.put("success", false);
                result = jo.toString();
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("ReportController.getReportConfig", ex);
        }
        return result;
    }

    @Override
    public int setSelectedReport(Connection conn, HttpServletRequest request) throws ServiceException {
        String reportID = request.getParameter("reportid");
        DbUtil.executeUpdate(conn, ReportsQuery.removeSelectedReport(), new Object[]{loginID});
        return DbUtil.executeUpdate(conn, ReportsQuery.setSelectedReport(), new Object[]{loginID, reportID});
    }
}
