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
package com.krawler.esp.company;

import com.krawler.common.locale.LocaleUtils;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.KWLErrorMsgs;
import com.krawler.common.util.StringUtil;
import com.krawler.database.DbPool.Connection;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author Abhay
 */
public class CompanyDAOImpl implements CompanyDAO {

    private StringBuilder query;
    private List params;
    private int resultTotalCount;
    private Company company;

    private void loadCompanyObject(DbResults rs) {
        company = new Company();
        CompanyHelper.loadCompany(rs, company);
    }

    @Override
    public int getResultTotalCount() {
        return resultTotalCount;
    }

    @Override
    public Company getCompany(Connection conn, String companyID) throws ServiceException {
        query = new StringBuilder();
        query.append("SELECT * FROM company WHERE companyid = ?");

        params = new ArrayList();
        params.add(companyID);

        DbResults rs = DbUtil.executeQuery(conn, query.toString(), params.toArray());
        if (rs.next()) {
            loadCompanyObject(rs);
        }
        return company;
    }

    @Override
    public int setCompanyPERTDefaultDifference(Connection conn, String companyID, int optimisticDiff, int pessimisticDiff) throws ServiceException {
        query = new StringBuilder();
        query.append("INSERT INTO pertdefaults_company (companyid, o_diff, p_diff) VALUES(?,?,?)");

        params = new ArrayList();
        params.add(companyID);
        params.add(optimisticDiff);
        params.add(pessimisticDiff);

        resultTotalCount = DbUtil.executeUpdate(conn, query.toString(), params.toArray());
        return resultTotalCount;
    }

    @Override
    public Company getCompanyByUser(Connection conn, String userID) throws ServiceException {
        DbResults rs = DbUtil.executeQuery(conn, "select company.* from company inner join users on users.companyid = company.companyid where users.userid = ?", new Object[]{userID});
        if (rs.next()) {
            loadCompanyObject(rs);
        }
        return company;
    }

    public String getCompanyIDBySubdomain(Connection conn, String subdomain) throws ServiceException {
        String companyID = "";
        DbResults rs = DbUtil.executeQuery(conn, "select companyid from company where subdomain = ?", new Object[]{subdomain});
        if (rs.next()) {
            companyID = rs.getString("companyid");
        }
        return companyID;
    }

    @Override
    public Company getCompanyBySubdomain(Connection conn, String subdomain) throws ServiceException {
        DbResults rs = DbUtil.executeQuery(conn, "select * from company where subdomain = ?", new Object[]{subdomain});
        if (rs.next()) {
            loadCompanyObject(rs);
        }
        return company;
    }

    @Override
    public String getSysEmailIdByCompanyID(Connection conn, String companyID) throws ServiceException {
        String result = KWLErrorMsgs.adminEmailId;

        query = new StringBuilder();
        query.append("SELECT company.emailid AS sysemailid, users.emailid FROM company "
                + "INNER JOIN users ON users.userid = company.creator WHERE company.companyid = ?");

        params = new ArrayList();
        params.add(companyID);

        DbResults rs = DbUtil.executeQuery(conn, query.toString(), params.toArray());
        if (rs.next()) {
            if (!StringUtil.isNullOrEmpty(rs.getString("sysemailid"))) {
                result = rs.getString("sysemailid");
            } else if (!StringUtil.isNullOrEmpty(rs.getString("emailid"))) {
                result = rs.getString("emailid");
            }
        }
        return result;
    }

    @Override
    public boolean isSubscribedToEmailNotifications(Connection conn, String companyID) throws ServiceException {
        int ntype = 0, actid = 0;

        Company c = getCompany(conn, companyID);
        ntype = c.getNotificationType();

        query = new StringBuilder();
        query.append("SELECT typeid FROM notificationlist WHERE typename = 'email'");

        DbResults rs = DbUtil.executeQuery(conn, query.toString());
        if (rs.next()) {
            actid = rs.getInt("typeid");
        }
        actid = (int) Math.pow(2, actid);
        if ((ntype & actid) == actid) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Locale getCompanyLocale(Connection conn, String companyID) throws ServiceException {
        Locale l = null;
        query = new StringBuilder("SELECT l.langcode, l.countrycode FROM company c "
                + "LEFT JOIN language l ON c.language = l.id WHERE c.companyid = ?");

        DbResults rs = DbUtil.executeQuery(conn, query.toString(), companyID);
        if (rs.next() && rs.getObject("langcode") != null) {
            String localeString = LocaleUtils.getLocaleString(rs.getString("langcode"), rs.getString("countrycode"));
            l = LocaleUtils.parseLocaleString(localeString);
        } else {
            l = Locale.ENGLISH; // English Locale is the default fallback locale as of now and is hardcoded here.
        }
        return l;
    }
}
