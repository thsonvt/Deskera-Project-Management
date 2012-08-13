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

import com.krawler.common.service.ServiceException;
import com.krawler.database.DbPool.Connection;
import javax.servlet.http.HttpServletRequest;

/**
 * This interface is used as a controller for custom column
 * @author kamlesh kumar sah
 */
public abstract class CustomColumnFactory {

    public abstract String addColumn(Connection conn, HttpServletRequest request) throws ServiceException;

    public abstract String editColumn(Connection conn, HttpServletRequest request) throws ServiceException;

    public abstract String deleteColumn(Connection conn, HttpServletRequest request) throws ServiceException;

    public abstract String getAllColumn(Connection conn, HttpServletRequest request) throws ServiceException;

    public abstract boolean isFormSubmit();
}
