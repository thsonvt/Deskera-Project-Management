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
import com.krawler.common.util.KWLErrorMsgs;
import com.krawler.common.util.StringUtil;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.database.DbPool.Connection;

public class CompanyHandler {

    public static String getCompanySubdomainByUser(Connection conn,
            String userID) throws ServiceException {
        String subdomain = "";
        DbResults rs = DbUtil.executeQuery(
                conn,
                "select subdomain from company inner join users on users.companyid = company.companyid where users.userid = ?",
                new Object[]{userID});
        if (rs.next()) {
            subdomain = rs.getString("subdomain");
        }
        return subdomain;
    }

    public static String getCompanyByUser(Connection conn,
            String userID) throws ServiceException {
        String subdomain = "";
        DbResults rs = DbUtil.executeQuery(
                conn,
                "select users.companyid as companyid from company inner join users on users.companyid = company.companyid where users.userid = ?",
                new Object[]{userID});
        if (rs.next()) {
            subdomain = rs.getString("companyid");
        }
        return subdomain;
    }

    public static String getCompanyIDBySubdomain(Connection conn,
            String subdomain) throws ServiceException {
        String companyid = "";
        DbResults rs = DbUtil.executeQuery(conn,
                "select companyid from company where subdomain = ?",
                new Object[]{subdomain});
        if (rs.next()) {
            companyid = rs.getString("companyid");
        }
        return companyid;
    }

    public static String getCompanySubdomainByCompanyID(Connection conn,
            String companyID) throws ServiceException {
        String subdomain = "";
        DbResults rs = DbUtil.executeQuery(
                conn,
                "select subdomain from company where companyid = ?",
                new Object[]{companyID});
        if (rs.next()) {
            subdomain = rs.getString("subdomain");
        }
        return subdomain;
    }

    public static String getCompanyIDFromProject(Connection conn,
            String pid) throws ServiceException {
        String companyid = "";
        DbResults rs = DbUtil.executeQuery(conn, "select companyid from project where projectid = ?", new Object[]{pid});
        if (rs.next()) {
            companyid = rs.getString("companyid");
        }
        return companyid;
    }

    public static String getSysEmailIdByCompanyID(Connection conn, String cid) throws ServiceException {
        String result = KWLErrorMsgs.adminEmailId;
        DbResults rs = DbUtil.executeQuery(conn, "select company.emailid as sysemailid,users.emailid from company "
                + "inner join users on users.userid = company.creator where company.companyid = ?", cid);
        if (rs.next()) {
            if (!StringUtil.isNullOrEmpty(rs.getString("sysemailid"))) {
                result = rs.getString("sysemailid");
            } else if (!StringUtil.isNullOrEmpty(rs.getString("emailid"))) {
                result = rs.getString("emailid");
            }
        }
        return result;
    }
}
