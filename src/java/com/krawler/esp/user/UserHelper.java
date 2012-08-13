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
package com.krawler.esp.user;

import com.krawler.common.service.ServiceException;
import com.krawler.database.DbResults;

/**
 *
 * @author Abhay
 */
public class UserHelper {
    
    public static void setObject(DbResults rs, User u) throws ServiceException {
        u.setUserID(rs.getString("userid"));
        if (rs.has("username")) {
            u.setUserName(rs.getString("username"));
        }
        if (rs.has("companyid")) {
            u.setCompanyID(rs.getString("companyid"));
        }
        if (rs.has("fname")) {
            u.setFirstName(rs.getString("fname"));
        }
        if (rs.has("lname")) {
            u.setLastName(rs.getString("lname"));
        }
        if (rs.has("emailid")) {
            u.setEmailID(rs.getString("emailid"));
        }
        if (rs.has("address")) {
            u.setAddress(rs.getString("address"));
        }
        if (rs.has("designation")) {
            u.setDesignation(rs.getString("designation"));
        }
        if (rs.has("contactno")) {
            u.setContactNumber(rs.getString("contactno"));
        }
        if (rs.has("aboutuser")) {
            u.setAboutUser(rs.getString("aboutuser"));
        }
        if (rs.has("userstatus")) {
            u.setUserStatus(rs.getString("userstatus"));
        }
        if (rs.has("timezone")) {
            u.setTimeZone(rs.getString("timezone"));
        }
        if (rs.has("phpbbid")) {
            u.setPhpBBID(rs.getInt("phpbbid"));
        }
        if (rs.has("dateformat")) {
            u.setDateFormat(rs.getInt("dateformat"));
        }
        if (rs.has("country")) {
            u.setCountry(rs.getInt("country"));
        }
        if (rs.has("fax")) {
            u.setFax(rs.getString("fax"));
        }
        if (rs.has("altcontactno")) {
            u.setAlternateContactNumber(rs.getString("altcontactno"));
        }
        if (rs.has("panno")) {
            u.setPanNumber(rs.getString("panno"));
        }
        if (rs.has("ssnno")) {
            u.setSsnNumber(rs.getString("ssnno"));
        }
        if (rs.has("roleid")) {
            u.setRoleID(rs.getInt("roleid"));
        }
        if (rs.has("notification")) {
            u.setNotification(rs.getBoolean("notification"));
        }
    }
}
