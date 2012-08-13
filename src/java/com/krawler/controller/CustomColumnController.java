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
package com.krawler.controller;

import com.krawler.common.customcolumn.CCManager;
import com.krawler.common.customcolumn.CcUtil;
import com.krawler.common.customcolumn.Column;
import com.krawler.common.customcolumn.ColumnsMetaData;
import com.krawler.common.customcolumn.CustomColumn;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.KWLErrorMsgs;
import com.krawler.common.util.Utilities;
import com.krawler.database.DbPool.Connection;
import com.krawler.esp.handlers.AuditTrail;
import com.krawler.esp.handlers.AuthHandler;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Kamlesh kumar sah
 */
public class CustomColumnController extends CustomColumnFactory {

    private boolean formSubmit;

    @Override
    public String addColumn(Connection conn, HttpServletRequest request) throws ServiceException {
        String res = KWLErrorMsgs.rsFalseNoData;
        try {
            String cid = AuthHandler.getCompanyid(request);
            CustomColumn cc = CCManager.getCustomColumn(cid);
            Column column = CcUtil.getObject(request);
            column.setColumnId(UUID.randomUUID().toString());
            column.setCreator(AuthHandler.getUserid(request));
            column.setFields();
            if (cc.insertColumn(conn, column)) {
                res = KWLErrorMsgs.rsSuccessTrueNoData;
                String uid = AuthHandler.getUserid(request);
                String params = AuthHandler.getAuthor(conn, uid) + " (" + AuthHandler.getUserName(request) + ")" + "," + column.getHeader();
                AuditTrail.insertLog(conn, "401", uid, "", "", cid, params, AuthHandler.getIPAddress(request), 0);

            }
            this.formSubmit = true;
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("CustomColumnController.addColumn()", ex);
        }
        return res;
    }

    @Override
    public String editColumn(Connection conn, HttpServletRequest request) throws ServiceException {
        String res = KWLErrorMsgs.rsFalseNoData;
        try {
            String cid = AuthHandler.getCompanyid(request);
            CustomColumn cc = CCManager.getCustomColumn(cid);
            Column column = CcUtil.getObject(request);
            column.setColumnId(request.getParameter("columnId"));
            column.setFields();
            if (cc.editColumn(conn, column)) {
                res = KWLErrorMsgs.rsSuccessTrueNoData;
                String uid = AuthHandler.getUserid(request);
                String params = AuthHandler.getAuthor(conn, uid) + " (" + AuthHandler.getUserName(request) + ")" + "," + column.getHeader();
                AuditTrail.insertLog(conn, "402", uid, "", "", cid, params, AuthHandler.getIPAddress(request), 0);

            }
            formSubmit = true;
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("CustomColumnController.editColumn()", ex);
        }
        return res;
    }

    @Override
    public String deleteColumn(Connection conn, HttpServletRequest request) throws ServiceException {
        String result = KWLErrorMsgs.rsFalseNoData;
        try {
            String cid = AuthHandler.getCompanyid(request);
            CustomColumn cc = CCManager.getCustomColumn(cid);
            if (cc.deleteColumn(conn, request.getParameter("columnId"))) {
                result = KWLErrorMsgs.rsSuccessTrueNoData;
                String uid = AuthHandler.getUserid(request);
                String params = AuthHandler.getAuthor(conn, uid) + " (" + AuthHandler.getUserName(request) + ")" + "," + request.getParameter("header");
                AuditTrail.insertLog(conn, "403", uid, "", "", cid, params, AuthHandler.getIPAddress(request), 0);
            }
            this.formSubmit = true;
        } catch (Exception ex) {
            throw ServiceException.FAILURE("CustomColumnController.deleteColumn()", ex);
        }
        return result;
    }

    @Override
    public String getAllColumn(Connection conn, HttpServletRequest request) throws ServiceException {
        try {
            CustomColumn cc = CCManager.getCustomColumn(AuthHandler.getCompanyid(request));
            ColumnsMetaData rsmd = cc.getColumnsMetaData(conn);
            String result = Utilities.listToGridJson(rsmd.getAllColumn(), rsmd.getColumnCount(), Column.class);
            return result;
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("CustomColumnController.getAllColumn()", ex);
        }

    }

    @Override
    public boolean isFormSubmit() {
        return formSubmit;
    }
    }
