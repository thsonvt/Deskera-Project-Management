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

//import com.krawler.common.locale.LocaleUtils;
import com.krawler.common.locale.LocaleUtils;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.RandomStringUtils;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.KWLErrorMsgs;
import com.krawler.database.DbPool;
import com.krawler.database.DbPool.Connection;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.krawler.common.timezone.Timezone;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.esp.company.Company;
import com.krawler.esp.company.CompanyDAO;
import com.krawler.esp.company.CompanyDAOImpl;
import java.util.Date;

public class AuthHandler {

    public static JSONObject verifyLogin(Connection cn, String username,
            String passwd, String subdomain) throws ServiceException {//called from auth.jsp
        boolean bFound = false;
        JSONObject jobj = new JSONObject();
        String message = "Authentication failed";
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            try {
                CompanyDAO cd = new CompanyDAOImpl();
                Company c = cd.getCompanyBySubdomain(cn, subdomain);
                boolean isExpired = false;
                if (c != null) {
                    if (c.getToExpireOn() != null) {
                        isExpired = !(c.getToExpireOn().after(new Date()) || c.getToExpireOn().equals(new Date()));
                    }
                    if (!isExpired) {
                        String userid = "";
                        String SELECT_USER_INFO = " Select userlogin.lastactivitydate,userlogin.userid, userlogin.username, "
                                + " users.companyid, company.image, company.companyname, lan.langcode, lan.countrycode from users"
                                + " inner join userlogin on users.userid = userlogin.userid"
                                + " inner join company on company.companyid = users.companyid"
                                + " left join language lan on company.language = lan.id"
                                + " where userlogin.username = ? and userlogin.password = ? "
                                + " AND company.subdomain = ? AND userlogin.isactive = true AND company.activated = true";
                        DbResults rs = DbUtil.executeQuery(cn, SELECT_USER_INFO, new Object[]{username, passwd, subdomain});
                        if (rs.next()) {
                            userid = rs.getString("userid");
                            jobj.put("success", true);
                            jobj.put("lid", userid);
                            jobj.put("username", rs.getString("username"));
                            jobj.put("companyid", rs.getString("companyid"));
                            jobj.put("lastlogin", Timezone.toCompanyTimezone(cn, rs.getObject("lastactivitydate").toString(), rs.getString("companyid")));
                            jobj.put("companylogo", rs.getString("image"));
                            jobj.put("company", rs.getString("companyname"));
                            jobj.put("language", LocaleUtils.getLocaleString(rs.getString("langcode"),rs.getString("countrycode")));
                            java.sql.Timestamp JavaDateNow = new java.sql.Timestamp(new java.util.Date().getTime());
                            String query1 = "UPDATE userlogin SET lastactivitydate=? WHERE userid=?";
                            int rs1 = DbUtil.executeUpdate(cn, query1, new Object[]{JavaDateNow, userid});
                            bFound = true;

                            String params = getAuthor(cn, userid) + " (" + getUserName(cn, userid) + ")";
                            AuditTrail.insertLog(cn, "11", userid, "", "", rs.getString("companyid"), params, "", 0);
                        }
                    } else {
                        message = "Your trial account has expired";
                    }
                } else {
                    message = "subdomain does not exist";
                }
            } catch (JSONException e) {
                throw ServiceException.FAILURE("Auth.verifyLogin", e);
            }
        }
        if (bFound == false) {
            try {
                jobj.put("failure", true);
                jobj.put("message", message);
            } catch (JSONException e) {
                throw ServiceException.FAILURE("Auth.verifyLogin", e);
            }
        }
        return jobj;
    }

    public static JSONObject verifyLogin(Connection cn, String username, String subdomain) throws ServiceException {
        //called from validate.jsp
        boolean bFound = false;
        JSONObject jobj = new JSONObject();
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            try {
                String userid = "";
                java.util.Date d = new java.util.Date();
                String SELECT_USER_INFO = " Select userlogin.lastactivitydate,userlogin.userid, userlogin.username, "
                        + " users.companyid, company.image, company.companyname, lan.langcode, lan.countrycode from users"
                        + " inner join userlogin on users.userid = userlogin.userid"
                        + " inner join company on company.companyid = users.companyid"
                        + " left join language lan on company.language = lan.id"
                        + " where userlogin.username = ? and company.subdomain = ? AND company.activated = true"
                        + " AND userlogin.isactive = true ";
                DbResults rs = DbUtil.executeQuery(cn, SELECT_USER_INFO, new Object[]{username, subdomain});
                if (rs.next()) {
                    userid = rs.getString("userid");
                    jobj.put("success", true);
                    jobj.put("lid", userid);
                    jobj.put("username", rs.getString("username"));
                    jobj.put("companyid", rs.getString("companyid"));
                    jobj.put("lastlogin", Timezone.toCompanyTimezone(cn, rs.getObject("lastactivitydate").toString(), rs.getString("companyid")));
                    jobj.put("companylogo", rs.getString("image"));
                    jobj.put("company", rs.getString("companyname"));
                    jobj.put("language",LocaleUtils.getLocaleString(rs.getString("langcode"),rs.getString("countrycode")));
                    java.sql.Timestamp JavaDateNow = new java.sql.Timestamp(new java.util.Date().getTime());
                    String query1 = "UPDATE userlogin SET lastactivitydate=? WHERE userid=?";
                    int rs1 = DbUtil.executeUpdate(cn, query1, new Object[]{JavaDateNow, userid});
                    bFound = true;
                }
            } catch (JSONException e) {
                throw ServiceException.FAILURE("Auth.verifyLogin", e);
            }
        }
        if (bFound == false) {
            try {
                jobj.put("failure", true);
            } catch (JSONException e) {
                throw ServiceException.FAILURE("Auth.verifyLogin", e);
            }
        }
        return jobj;
    }

    public static String getUserTimezone(Connection conn, String tzid) throws ServiceException {
        PreparedStatement pstmt = null;
        String diff = "", tz = "";
        try {
            String q = "select difference from timezone where id = ?";
            pstmt = conn.prepareStatement(q);
            pstmt.setString(1, tzid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                diff = rs.getString("difference");
                String temp = diff.split(":")[1];
                if (Integer.valueOf(temp) != 0) {
                    temp = "5";
                }
                tz = diff.split(":")[0].concat(".").concat(temp);
            } else {
                tz = "0.0";
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("AuthHandler.getUserTimezone", e);
        } finally {
            DbPool.closeStatement(pstmt);
            return tz;
        }
    }

    public static String getSystemTimezone(Connection conn) throws ServiceException {
        PreparedStatement pstmt = null;
        String diff = "", tz = "";
        try {
            String q = "select difference from systemtimezone";
            pstmt = conn.prepareStatement(q);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                diff = rs.getString("difference");
                String temp = diff.split(":")[1];
                if (Integer.valueOf(temp) != 0) {
                    temp = "5";
                }
                if (diff.startsWith("-")) {
                    diff = "+" + diff.substring(1);
                } else if (diff.startsWith("+")) {
                    diff = "-" + diff.substring(1);
                }
                tz = diff.split(":")[0].concat(".").concat(temp);
            } else {
                tz = "0.0";
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("AuthHandler.getUserTimezone", e);
        } finally {
            DbPool.closeStatement(pstmt);
            return tz;
        }
    }

    public static JSONArray getPreferences(Connection cn, String userid, String companyid)
            throws ServiceException {
        PreparedStatement pstmt = null;
        JSONArray preferences = new JSONArray();
        try {
            Company c = new CompanyDAOImpl().getCompany(cn, companyid);
            String tzid = "";
            String query = "select timezone, country, dateformat.scriptform as dateformat,dateformat.id, dateformat.sseppos as seppos, users.notification "
                    + "from users inner join  dateformat on dateformat.id = users.dateformat where userid = ?";
            pstmt = cn.prepareStatement(query);
            pstmt.setString(1, userid);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                JSONObject j = new JSONObject();
                if (!StringUtil.isNullOrEmpty(rs.getString("timezone"))) {
                    tzid = rs.getString("timezone");
                } else {
                    tzid = ProfileHandler.setUserTimezone(cn, userid, companyid);
                }
                String tz = getUserTimezone(cn, tzid);
                j.put("timezonediff", tz);
                tz = getSystemTimezone(cn);
                j.put("systemtz", tz);
                j.put("DateFormat", rs.getObject("dateformat"));
                j.put("DateFormatid", rs.getObject("id"));
                j.put("DateFormatSeparatorPosition", rs.getObject("seppos"));
                j.put("Countryid", rs.getString("country"));
                j.put("Notification", rs.getString("notification"));
                j.put("CompanyNotification", projdb.isEmailSubscribe(cn, companyid));
                j.put("CheckListModule", c.isIsCheckList());
                j.put("DocAccess", c.isDocAccess());
                preferences.put(j);
            }
        } catch (SQLException sex) {
            throw ServiceException.FAILURE("AuthHandler.getPreferences", sex);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("AuthHandler.getPreferences", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return preferences;
    }

    public static String getUserid(HttpServletRequest request)
            throws SessionExpiredException {
        String userId = NullCheckAndThrow(request.getSession().getAttribute("userid"), SessionExpiredException.USERID_NULL);
        return userId;
    }

    public static String getUserName(HttpServletRequest request)
            throws SessionExpiredException {
        String userName = NullCheckAndThrow(request.getSession().getAttribute("username"), SessionExpiredException.USERNAME_NULL);
        return userName;
    }

    public static String getCompanyid(HttpServletRequest request,
            boolean bThrowNoError) {
        Object cidObj = request.getSession().getAttribute("companyid");
        if (cidObj == null) {
            return "";
        } else {
            return cidObj.toString();
        }
    }

    public static String getCompanyid(HttpServletRequest request)
            throws SessionExpiredException {
        String companyID = NullCheckAndThrow(request.getSession().getAttribute("companyid"), SessionExpiredException.COMPANYID_NULL);
        return companyID;
    }

    public static String getCompanyName(HttpServletRequest request)
            throws SessionExpiredException {
        String companyID = NullCheckAndThrow(request.getSession().getAttribute("company"), SessionExpiredException.COMPANYNAME_NULL);
        return companyID;
    }

    private static String NullCheckAndThrow(Object objToCheck, String errorCode)
            throws SessionExpiredException {
        if (objToCheck != null) {
            String oStr = objToCheck.toString();
            if (!StringUtil.isNullOrEmpty(oStr)) {
                return oStr;
            }
        }
        throw new SessionExpiredException(KWLErrorMsgs.errSessionInvalid, errorCode);
    }

    public static String generateNewPassword() {
        return RandomStringUtils.random(8, true, true);
    }

    public static String getSHA1(String inStr) throws ServiceException {
        String outStr = inStr;
        try {
            byte[] theTextToDigestAsBytes = inStr.getBytes("utf-8");

            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            byte[] digest = sha.digest(theTextToDigestAsBytes);

            StringBuffer sb = new StringBuffer();
            for (byte b : digest) {
                String h = Integer.toHexString(b & 0xff);
                if (h.length() == 1) {
                    sb.append("0" + h);
                } else {
                    sb.append(h);
                }
            }
            outStr = sb.toString();
        } catch (UnsupportedEncodingException e) {
            throw ServiceException.FAILURE("Auth.getSHA1", e);
        } catch (NoSuchAlgorithmException e) {
            throw ServiceException.FAILURE("Auth.getSHA1", e);
        }
        return outStr;
    }

    public static String getCompanyName(Connection con, String domainName)
            throws ServiceException {
        String companyname = "";
        PreparedStatement pstmt = null;
        try {
            ResultSet rs = null;
            pstmt = con.prepareStatement("Select companyname from company where subdomain = ?");
            pstmt.setString(1, domainName);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                companyname = rs.getString("companyname");
            }
        } catch (SQLException ex) {
            throw ServiceException.FAILURE("Auth.getCompanyName", ex);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return companyname;
    }

    public static String getUserIdFromUsernameAndSubdomain(Connection con, String domainName, String username)
            throws ServiceException {
        String userid = "";
        PreparedStatement pstmt = null;
        try {
            ResultSet rs = null;
            pstmt = con.prepareStatement("select users.userid from users inner join userlogin on users.userid = userlogin.userid where userlogin.username = ? and companyid ="
                    + "(select companyid from company where subdomain = ?)");
            pstmt.setString(1, username);
            pstmt.setString(2, domainName);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                userid = rs.getString("userid");
            }
        } catch (SQLException ex) {
            throw ServiceException.FAILURE("Auth.getUserId", ex);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return userid;
    }

    public static String getUserName(Connection con, String userid) throws ServiceException {

        String username = "";
        PreparedStatement pstmt = null;
        try {
            ResultSet rs = null;
            pstmt = con.prepareStatement(" SELECT username FROM userlogin WHERE userid = ? ");
            pstmt.setString(1, userid);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                username = rs.getString("username");
            }

        } catch (SQLException ex) {
            throw ServiceException.FAILURE("Auth.getUserName", ex);

        } finally {
            DbPool.closeStatement(pstmt);
        }
        return username;
    }

    public static String getAuthor(Connection conn, String userid) throws ServiceException {
        String fname = "";
        String lname = "";
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("select fname,lname from users where userid = ?");
            pstmt.setString(1, userid);
            rs = pstmt.executeQuery();
            rs.next();
            if (rs.getString(1) != null) {
                fname = rs.getString(1);
            }
            if (rs.getString(2) != null) {
                lname = rs.getString(2);
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("AuthHandler.getAuthor", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return (fname + " " + lname).trim();
    }

    public static String getIPAddress(HttpServletRequest request) {

        String ipaddr = null;
        if (StringUtil.isNullOrEmpty(request.getHeader("x-real-ip"))) {
            ipaddr = request.getRemoteAddr();

        } else {
            ipaddr = request.getHeader("x-real-ip");
        }

        return ipaddr;
    }
}
