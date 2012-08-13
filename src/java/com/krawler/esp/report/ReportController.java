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

import com.krawler.esp.report.custom.CustomReport;
import com.krawler.esp.report.custom.QueryFilter;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.KWLErrorMsgs;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.Utilities;
import com.krawler.database.DbPool.Connection;
import com.krawler.esp.handlers.AuditTrail;
import com.krawler.esp.handlers.AuthHandler;
import com.krawler.esp.report.custom.ReportHelper;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Kamlesh Kumar Sah
 */
public class ReportController extends AbstractReportController {

    public ReportController(String companyid) {
        this.companyid = companyid;
    }

    @Override
    public String getReportsColumnAll(Connection conn, HttpServletRequest request) throws ServiceException {
        String res = "";
        CustomReport cr = ReportHelper.getCustomReport(companyid);
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
            CustomReport cr = ReportHelper.getCustomReport(companyid);
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
            CustomReport cr = ReportHelper.getCustomReport(companyid);
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
        try {
            CustomReport cr = ReportHelper.getCustomReport(AuthHandler.getCompanyid(request));
            List<Reports> rep = cr.getAllReports(conn);
            if (rep.size() == 0) {
                return "{data:[]}";
            }
            String res = Utilities.listToGridJson(rep, rep.size(), Reports.class);
            return res;
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("CustomColumnController.getAllReports()", ex);
        }
    }

    @Override
    public boolean isFormSubmit() {
        return formSubmit;
    }

    @Override
    public String getReportsColumn(Connection conn, HttpServletRequest request) throws ServiceException {
        try {
            String repId = request.getParameter("reportid");
            CustomReport cr = ReportHelper.getCustomReport(AuthHandler.getCompanyid(request));
            List<ReportColumn> columns = cr.getReportsColumns(conn, repId);
            Collections.sort(columns);
            return Utilities.listToGridJson(columns, columns.size(), ReportColumn.class);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("CustomColumnController.getReportsColumn()", ex);
        }
    }

    @Override
    public String getReport(Connection conn, HttpServletRequest request) throws ServiceException {
        return getReport(conn, request, false).toString();
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
        CustomReport cr = ReportHelper.getCustomReport(companyid);
        List<ReportColumn> columns = cr.getReportsColumns(conn, repId);
        if (!StringUtil.isNullOrEmpty(request.getParameter("ss"))) {
            ss = request.getParameter("ss");
        }
        ReportQueryBuilder query = new MySqlReportQueryBuilder(columns, ss);
        //query.addSearch("project", "projectname", ss);
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
    private String companyid;
    private boolean formSubmit;

    @Override
    public String getSelectedReportConfig(Connection conn, HttpServletRequest request) throws ServiceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int setSelectedReport(Connection conn, HttpServletRequest request) throws ServiceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
