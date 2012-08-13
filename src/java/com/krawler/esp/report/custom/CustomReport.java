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
import com.krawler.esp.report.ReportBean;
import com.krawler.esp.report.ReportQueryBuilder;
import com.krawler.common.service.ServiceException;
import com.krawler.database.DbPool.Connection;
import java.util.List;

/**
 *
 * @author krawler
 */
public interface CustomReport {

    List<Reports> getAllReports(Connection conn) throws ServiceException;

    int insertReport(Connection conn, String reportId, String reportname, String reportType, String description, String userid) throws ServiceException;

    public void insertColumns(Connection conn, List<ReportColumn> columns, String rid) throws ServiceException;

    /** this is used to get all posible column for the report */
    public List<ReportColumn> getColumnsMetaData(Connection conn, String type) throws ServiceException;

    public List<ReportColumn> getReportsColumns(Connection conn, String reportid) throws ServiceException;

    public int insertReportsColumn(Connection conn, String reportid, ReportColumn column) throws ServiceException;

    public int editReportsColumn(Connection conn, String reportid, ReportColumn column) throws ServiceException;

    public ReportBean getReportData(Connection conn, ReportQueryBuilder query) throws ServiceException;

    public void deleteCustomReport(Connection conn, String reportId) throws ServiceException;
    
    public Reports getReportConfig(Connection conn, String reportID) throws ServiceException;
    
}
