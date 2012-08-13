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

import com.krawler.common.permission.PermissionManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import com.krawler.common.service.ServiceException;
import com.krawler.database.DbPool;
import com.krawler.database.DbUtil;
import com.krawler.database.DbPool.Connection;
import com.krawler.common.timezone.Timezone;
import com.krawler.utils.json.base.JSONException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SignupHandler {

    public static String confirmSignup(Connection conn, String id, HttpServletRequest request)
            throws ServiceException {
        String result = null;
        ResultSet rs = null, frs = null, ars = null;
        PreparedStatement pstmt = null, fpstmt = null, apstmt = null;
        java.sql.Date sqldt = new java.sql.Date(new java.util.Date().getTime());
        double val = 0.0;
        try {
            /*pstmt = conn
            .prepareStatement("SELECT loginid, pass, emailid, companyname, fname, lname, subdomain FROM csignups WHERE signupid=?;");
            pstmt.setString(1, id);
            rs = pstmt.executeQuery();*/
            //if (rs.next()) {
            String username = request.getParameter("u").toString();
            String pwd = request.getParameter("p").toString();
            String companyName = request.getParameter("c").toString();
            String emailid = request.getParameter("e").toString();
            emailid = emailid.replace(" ", "+");
            String subdomain = request.getParameter("cdomain").toString();
            String fname = request.getParameter("fname").toString();
            String lname = request.getParameter("lname").toString();

            String uid = UUID.randomUUID().toString();
            String cid = UUID.randomUUID().toString();

            pstmt = conn.prepareStatement("INSERT INTO userlogin (userid, username, password, authkey) VALUES (?,?,?,?)");
            pstmt.setString(1, uid);
            pstmt.setString(2, username);
            pstmt.setString(3, pwd);
            pstmt.setString(4, "");
            pstmt.executeUpdate();

            java.sql.Timestamp sqlPostDate = Timezone.getGmtTimestamp();
            pstmt = conn.prepareStatement("INSERT INTO company (companyid, companyname,subdomain, address, city, state, country, phone, fax, zip, timezone, activated, subscriptiondate, creator,createdon,currency,planid) VALUES (?,?,?,'','','',244,'','','',23,true,?,?,?,1,2)");
            pstmt.setString(1, cid);
            pstmt.setString(2, companyName);
            pstmt.setString(3, subdomain);
            pstmt.setDate(4, sqldt);
            pstmt.setString(5, uid);
            pstmt.setObject(6, sqlPostDate);
            pstmt.executeUpdate();

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-d HH:mm:ss");
            String timestamp1 = sdf.format(new java.util.Date());
            DbUtil.executeUpdate(conn, "INSERT INTO accountcompany (accid, companyid, balance, timestamp) SELECT accid, '"
                    + cid + "', 0, '" + timestamp1 + "' from account where accountName in ( 'Inventory Assets', "
                    + " 'Accounts Receivable', 'Sales Tax Payable', 'Fixed Asset', 'Payroll Expense', 'Expense', "
                    + " 'Undeposited Funds', 'Fees', 'Services', 'Cost of goods sold', 'Opening Balance Equity', "
                    + " 'Retained Earnings', 'Net Income', 'Accounts Payable' )");

            pstmt = conn.prepareStatement("INSERT INTO users (userid, emailid, companyid, image, fname, lname) VALUES (?,?,?,'',?,?)");
            pstmt.setString(1, uid);
            pstmt.setString(2, emailid);
            pstmt.setString(3, cid);
            pstmt.setString(4, fname);
            pstmt.setString(5, lname);
            pstmt.executeUpdate();

            setUpCompany(conn, cid, uid);
            /*
            pstmt = conn.prepareStatement(" SELECT moduleid, nickname FROM companymodules WHERE isdefault = true ");
            rs = pstmt.executeQuery();
            while(rs.next()) {
            int modVal = 2;
            int modSubVal = 0;
            
            PreparedStatement psSub = conn.prepareStatement(" SELECT submoduleid, subnickname " +
            " FROM companysubmodules WHERE parentmoduleid = ? " +
            " AND isdefault = true ");
            psSub.setInt(1, rs.getInt("moduleid"));
            ResultSet rsSub = psSub.executeQuery();
            while(rsSub.next()) {
            modSubVal += Math.pow(2, rsSub.getInt("submoduleid"));
            }
            
            pstmt = conn.prepareStatement(" INSERT INTO companymodulesubscription " +
            " (subscriptionid, moduleid, submodulesubscriptionid, companyid) " +
            " VALUES (?, ?, ?, ?) ");
            pstmt.setInt(1, modVal);
            pstmt.setInt(2, rs.getInt("moduleid"));
            pstmt.setInt(3, modSubVal);
            pstmt.setString(4, cid);
            pstmt.executeUpdate();
            }
            
            pstmt = conn.prepareStatement(" insert into featureslistview (featureid, companyid) values (?, ?) ");
            pstmt.setInt(1, 110);
            pstmt.setString(2, cid);
            pstmt.executeUpdate();
            
             */
//				pstmt = conn
//						.prepareStatement("DELETE FROM csignups WHERE signupid=?");
//				pstmt.setString(1, id);
//				pstmt.executeUpdate();

            /*				fpstmt = conn
            .prepareStatement("SELECT featureid FROM featurelist");
            frs = fpstmt.executeQuery();
            while (frs.next()) {
            apstmt = conn
            .prepareStatement("SELECT activityid FROM activitieslist WHERE featureid=?");
            apstmt.setInt(1, frs.getInt("featureid"));
            ars = apstmt.executeQuery();
            while (ars.next()) {
            val += Math.pow(2, Double.parseDouble(ars
            .getString("activityid")));
            }
            pstmt = conn
            .prepareStatement("INSERT INTO userpermissions (userid, featureid, permissions) VALUES (?,?,?)");
            pstmt.setString(1, uid);
            pstmt.setInt(2, frs.getInt("featureid"));
            pstmt.setInt(3, (int) val);
            pstmt.executeUpdate();
            
            val = 0.0;
            }*/
            Calendar cal = Calendar.getInstance();
            cal.set(sqldt.getYear(), sqldt.getMonth(), sqldt.getDate());
            cal.add(Calendar.DAY_OF_MONTH, 30);
            result = cal.getTime().toString();
            /*}
            else
            result = "false";*/
            //rs.close();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("signup.confirmSignup", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }

        return result;
    }

    public static void setUpCompany(Connection conn, String companyid, String userid) throws ServiceException {
        try {
            PreparedStatement pstmt = conn.prepareStatement(" SELECT moduleid, nickname FROM companymodules WHERE isdefault = true ");
            ResultSet rs = pstmt.executeQuery();
            double val = 0.0;
            while (rs.next()) {
                int modVal = 2;
                int modSubVal = 0;
                PreparedStatement psSub = conn.prepareStatement(" SELECT submoduleid, subnickname "
                        + " FROM companysubmodules WHERE parentmoduleid = ? AND isdefault = true ");
                psSub.setInt(1, rs.getInt("moduleid"));
                ResultSet rsSub = psSub.executeQuery();
                while (rsSub.next()) {
                    modSubVal += Math.pow(2, rsSub.getInt("submoduleid"));
                }
                pstmt = conn.prepareStatement(" INSERT INTO companymodulesubscription "
                        + " (subscriptionid, moduleid, submodulesubscriptionid, companyid) VALUES (?, ?, ?, ?) ");
                pstmt.setInt(1, modVal);
                pstmt.setInt(2, rs.getInt("moduleid"));
                pstmt.setInt(3, modSubVal);
                pstmt.setString(4, companyid);
                pstmt.executeUpdate();
            }
            pstmt = conn.prepareStatement(" insert into featureslistview (featureid, companyid) values (?, ?) ");
            pstmt.setInt(1, 110);
            pstmt.setString(2, companyid);
            pstmt.executeUpdate();
            PermissionManager pm =new PermissionManager();
            pm.AssignAllPermissions(conn, userid);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ServiceException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } catch (SQLException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }

    public static String useridIsAvailable(Connection conn, String id) throws ServiceException {
        String result = "failure";
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("SELECT COUNT(*) AS count FROM userlogin WHERE username=?");
            pstmt.setString(1, id);
            rs = pstmt.executeQuery();
            rs.next();
            if (rs.getInt("count") == 0) {
                pstmt = conn.prepareStatement("SELECT COUNT(*) AS count FROM csignups WHERE loginid=?");
                pstmt.setString(1, id);
                rs = pstmt.executeQuery();
                rs.next();
                if (rs.getInt("count") == 0) {
                    result = "success";
                }
            }
            rs.close();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("sigunup.useridIsAvailable", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return result;
    }

    public static String useridIsAvailable(Connection conn, String userid, String companyid) throws ServiceException {
        String result = "failure";
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("SELECT COUNT(*) AS count FROM userlogin inner join users on users.userid = userlogin.userid "
                    + " WHERE userlogin.username=? and companyid = ?");
            pstmt.setString(1, userid);
            pstmt.setString(2, companyid);
            rs = pstmt.executeQuery();
            rs.next();
            if (rs.getInt("count") == 0) {
                result = "success";
            }
            rs.close();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("sigunup.useridIsAvailable", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return result;
    }

    public static String subdomainIsAvailable(Connection conn, String id) throws ServiceException {
        String result = "failure";
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("SELECT COUNT(*) AS count FROM defaultdomains WHERE name = ?"); // check in defaultdomain list
            pstmt.setString(1, id);
            rs = pstmt.executeQuery();
            rs.next();
            if (rs.getInt("count") == 0) {
                pstmt = conn.prepareStatement("SELECT COUNT(*) AS count FROM company WHERE subdomain = ?"); // check in registered company list
                pstmt.setString(1, id);
                rs = pstmt.executeQuery();
                rs.next();
                if (rs.getInt("count") == 0) {
                    pstmt = conn.prepareStatement("SELECT COUNT(*) AS count FROM csignups WHERE subdomain=?"); // check in pending signup request
                    pstmt.setString(1, id);
                    rs = pstmt.executeQuery();
                    rs.next();
                    if (rs.getInt("count") == 0) {
                        result = "success";
                    } else {
                        result = "success";
                    }
                } else {
                    pstmt = conn.prepareStatement("UPDATE company SET subdomain = CONCAT('old_', subdomain) WHERE subdomain = ?");
                    pstmt.setString(1, id);
                    int count = pstmt.executeUpdate();
                    if (count == 1) {
                        result = "success";
                    }
                }
            }
            rs.close();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("sigunup.subdomainIsAvailable", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return result;
    }

    public static String isNewSubdomainAvailable(Connection conn, String subdomain) throws ServiceException {
        String result = "failure";
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("SELECT COUNT(*) AS count FROM defaultdomains WHERE name = ?"); // check in defaultdomain list
            pstmt.setString(1, subdomain);
            rs = pstmt.executeQuery();
            rs.next();
            if (rs.getInt("count") == 0) {
                pstmt = conn.prepareStatement("SELECT COUNT(*) AS count FROM company WHERE subdomain = ?"); // check in registered company list
                pstmt.setString(1, subdomain);
                rs = pstmt.executeQuery();
                rs.next();
                if (rs.getInt("count") == 0) {
                    pstmt = conn.prepareStatement("SELECT COUNT(*) AS count FROM csignups WHERE subdomain=?"); // check in pending signup request
                    pstmt.setString(1, subdomain);
                    rs = pstmt.executeQuery();
                    rs.next();
                    if (rs.getInt("count") == 0) {
                        result = "success";
                    }
                }
            }
            rs.close();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("sigunup.isNewSubdomainAvailable", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return result;
    }
}
