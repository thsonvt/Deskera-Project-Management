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

import com.krawler.common.service.ServiceException;
import com.krawler.database.DbPool.Connection;
import java.io.ByteArrayOutputStream;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Kamlesh
 */
public abstract class AbstractReportController {

    /**
     * this is used to get list of all possible columns for report bulider.
     * @param conn
     * @param request
     * @return
     * @throws ServiceException
     */
    public abstract String getReportsColumnAll(Connection conn, HttpServletRequest request) throws ServiceException;

    public abstract String insertReport(Connection conn, HttpServletRequest request) throws ServiceException;

    public abstract String getAllReports(Connection conn, HttpServletRequest request) throws ServiceException;

    /**
     * this is used to get config of a report defined by user.
     * @param conn
     * @param request
     * @return
     * @throws ServiceException
     */
    public abstract String getReportsColumn(Connection conn, HttpServletRequest request) throws ServiceException;

    /**
     * this is used to get report data with header
     * @return
     */
    public abstract String getReport(Connection conn, HttpServletRequest request) throws ServiceException;

    public abstract ReportBean getReport(Connection conn, HttpServletRequest request, boolean export) throws ServiceException;
    
    public abstract ByteArrayOutputStream exportReport(Connection conn, HttpServletRequest request) throws ServiceException;

    public abstract String deleteReport(Connection conn, HttpServletRequest request) throws ServiceException;

    public abstract boolean isFormSubmit();
    
    public abstract String getSelectedReportConfig(Connection conn, HttpServletRequest request) throws ServiceException;
    
    public abstract int setSelectedReport(Connection conn, HttpServletRequest request) throws ServiceException;
}
