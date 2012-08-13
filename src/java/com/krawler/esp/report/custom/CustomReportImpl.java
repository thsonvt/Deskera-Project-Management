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
package com.krawler.esp.report.custom;

import com.krawler.esp.report.ReportColumn;
import com.krawler.esp.report.Reports;
import com.krawler.esp.report.ReportQueryBuilder;
import com.krawler.esp.report.ReportBean;
import com.krawler.common.service.ServiceException;
import com.krawler.database.DbPool.Connection;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.esp.report.ReportsQuery;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kamlesh
 */
class CustomReportImpl implements CustomReport {

    private String companyId;
    ReportQueryBuilder qb;

    public CustomReportImpl(String companyId) {
        this.companyId = companyId;
    }

    @Override
    public int insertReport(Connection conn, String reportId, String reportname, String reportType, String description, String userid) throws ServiceException {
        DbResults rs = DbUtil.executeQuery(conn, "select 1 from customreports where reportname = ? and companyid = ?", new Object[]{reportname,companyId});
        if (rs.next()) {
            throw ServiceException.FAILURE("Duplicate Report Name", null);
        }
        String query = "insert into customreports(reportid,companyid,reportname,type,description,category,createdby,createdon,modifiedon) values(?,?,?,?,?,'Custom',?,now(),now())";
        return DbUtil.executeUpdate(conn, query, new Object[]{reportId, companyId, reportname, reportType, description, userid});
    }

    @Override
    public void insertColumns(Connection conn, List<ReportColumn> columns, String reportid) throws ServiceException {
        int size = columns.size();
        if (size > 0) {
            String query = ReportsQuery.getInsertReportsColumn();
            for (int i = 0; i < size; i++) {
                ReportColumn rc = columns.get(i);
                DbUtil.executeUpdate(conn, query, new Object[]{rc.getColumnID(), reportid, rc.getDisplayHeader(), rc.isQuickSearch(), rc.getSummary(), rc.getDisplayOrder()});
            }
        }
    }

    @Override
    public List getAllReports(Connection conn) throws ServiceException {
        DbResults rs = DbUtil.executeQuery(conn, ReportsQuery.getAllReports(), companyId);
        List<Reports> list = new ArrayList<Reports>(rs.size());
        while (rs.next()) {
            list.add((Reports) ReportHelper.getReportsObject(rs));
        }
        return list;
    }

    @Override
    public List<ReportColumn> getColumnsMetaData(Connection conn, String type) throws ServiceException {
        String query = ReportsQuery.getReportsMetaDataQuery(type);
        DbResults rs = DbUtil.executeQuery(conn, query, companyId);
        List<ReportColumn> list = new ArrayList<ReportColumn>(rs.size());
        while (rs.next()) {
            list.add((ReportColumn) ReportHelper.getReportColumnObject(rs));
        }
        return list;
    }

    @Override
    public List<ReportColumn> getReportsColumns(Connection conn, String reportid) throws ServiceException {
        List<ReportColumn> list = new ArrayList<ReportColumn>();
        DbResults rs = DbUtil.executeQuery(conn, ReportsQuery.getReportsColumn(reportid));
        while (rs.next()) {
            list.add((ReportColumn) ReportHelper.getReportColumnObject(rs));
        }

        return list;
    }

    @Override
    public int insertReportsColumn(Connection conn, String reportid, ReportColumn column) throws ServiceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int editReportsColumn(Connection conn, String reportid, ReportColumn column) throws ServiceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ReportBean getReportData(Connection conn, ReportQueryBuilder query) throws ServiceException {
        PreparedStatement ps = null;
        ReportBean bean = new ReportBean(query.getColumns());
        try {
            ps = conn.prepareStatement(query.getQuery());
            ps.setString(1, companyId);
            ResultSet rs = ps.executeQuery();
            ReportHelper.setReportsBean(rs, bean);
        } catch (SQLException ex) {
            throw ServiceException.FAILURE("CustomeReportImpl.getReportData", ex);
        }
        return bean;
    }

    @Override
    public void deleteCustomReport(Connection conn, String reportId) throws ServiceException {
        DbUtil.executeUpdate(conn, "delete from customreports where reportid = ? and companyid =?", new Object[]{reportId, companyId});
        DbUtil.executeUpdate(conn, "delete from reportscolumnmapping where reportid = ?", new Object[]{reportId});
    }

    @Override
    public Reports getReportConfig(Connection conn, String reportID) throws ServiceException {
        DbResults rs = DbUtil.executeQuery(conn, ReportsQuery.getReport(), new Object[]{companyId, reportID});
        Reports rep = null;
        while (rs.next()) {
            rep = (Reports) ReportHelper.getReportsObject(rs);
        }
        return rep;
    }
}
