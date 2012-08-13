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

import com.krawler.common.locale.MessageSourceProxy;
import com.krawler.common.service.ServiceException;
import com.krawler.common.timezone.Timezone;
import com.krawler.common.util.StringUtil;
import com.krawler.database.DbPool.Connection;
import com.krawler.database.DbUtil;

import com.krawler.utils.json.base.JSONArray;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import javax.servlet.http.HttpServletRequest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.ResultSet;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;

public class AuditTrail {

    public static boolean insertLog(Connection conn, String actionId, String actionBy,
            String actionOn, String projectId, String companyId, String params,
            String ipAddress, int mode) {
        boolean result = true;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.sql.Timestamp JavaDateNow = new java.sql.Timestamp(new java.util.Date().getTime());
        try {
            int cntInsert = 0;
            String INSERT_LOG = " INSERT INTO actionlog "
                    + " (actionid, actionby, actionon, projectid, companyid, timestamp, params, ipaddress, mode) "
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ";
            cntInsert = DbUtil.executeUpdate(conn, INSERT_LOG, new Object[]{actionId, actionBy, actionOn, projectId, companyId, JavaDateNow, params, ipAddress, mode});
            if (cntInsert < 1) {
                result = false;
            }
        } catch (ServiceException ex) {
            Logger.getLogger(AuditTrail.class.getName()).log(Level.SEVERE, null, ex);
            result = false;
        } finally {
            return result;
        }
    }

    public static JSONObject getJsonForAction(Connection conn, PreparedStatement pstmt, ResultSet rs, HttpServletRequest request, String callerFlag)
            throws ServiceException {
        java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JSONObject jobj = new JSONObject();
        ResultSet rs2 = null;
        try {
            int aid = rs.getInt("actionid");
            String author = AuthHandler.getAuthor(conn, rs.getString("actionby"));
            String params = rs.getString("params");
            String userparam[] = params.split(",");
            String query2 = "select textkey from actions where actionid = ?";
            pstmt = conn.prepareStatement(query2);
            pstmt.setInt(1, aid);
            rs2 = pstmt.executeQuery();
            Object[] strParam = new Object[userparam.length];
            for (int i = 0; i < userparam.length; i++) {
                strParam[i] = userparam[i];
            }
            rs2.next();
            String action = rs2.getString("textkey");
            String useraction = MessageSourceProxy.getMessage(action, strParam, request);
            String sdtStr = Timezone.toCompanyTimezone(conn, rs.getTimestamp("timestamp").toString(), AuthHandler.getCompanyid(request));
            jobj.put("logid", rs.getInt("logid"));
            jobj.put("actionby", author);
            jobj.put("description", useraction);
            if (callerFlag.equals("all")||callerFlag.equals("projname")) {
                String projectname = "";
                String projid = rs.getString("projectid");
                if (!StringUtil.isNullOrEmpty(projid)) {
                    projectname = projdb.getProjectName(conn, projid);
                }
                jobj.put("projname", projectname);
            }
            jobj.put("timestamp", sdtStr);
        } catch (ServiceException ex) {
            Logger.getLogger(AuditTrail.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("AuditTrail.getJsonForAction", ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AuditTrail.getJsonForAction", ex);
        } finally {
            return jobj;
        }
    }

    public static String getLog(Connection conn, HttpServletRequest request, String companyid, String limit, String offset, String searchString, String dateString, String flag)
            throws ServiceException {
        ResultSet rs = null;
        ResultSet rs1 = null;
        String returnStr = "";
        PreparedStatement pstmt = null;
        String[] searchStrObj = new String[]{"params", "actiontext"};
        String myLikeString = StringUtil.getMySearchString(searchString, "and", searchStrObj);
        JSONObject resobj = new JSONObject();
        JSONObject tempobj = new JSONObject();
        JSONArray jarr = new JSONArray();
        try {
            String projectname = request.getParameter("projectname");
            String query = "select projectid from project where projectname = ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, projectname);
            rs = pstmt.executeQuery();
            rs.next();
            String projectid = rs.getString("projectid");
            pstmt = conn.prepareStatement("select count(*) AS count from actionlog al inner join actions a on al.actionid = a.actionid where companyid = ? and projectid = ?" + myLikeString + dateString);
            pstmt.setString(1, companyid);
            pstmt.setString(2, projectid);
            StringUtil.insertParamSearchString(3, pstmt, searchString, searchStrObj.length);
            rs = pstmt.executeQuery();
            int count1 = 0;
            if (rs.next()) {
                count1 = rs.getInt("count");
            }
            String query1 = "select logid, al.actionid, actionby, params,al.projectid, timestamp from actionlog al inner join actions a on al.actionid = a.actionid where companyid = ? and projectid = ? " + myLikeString + dateString + " order by timestamp DESC limit ? offset ?";
            pstmt = conn.prepareStatement(query1);
            pstmt.setString(1, companyid);
            pstmt.setString(2, projectid);
            int cnt = StringUtil.insertParamSearchString(3, pstmt, searchString, searchStrObj.length);
            pstmt.setInt(cnt++, Integer.parseInt(limit));
            pstmt.setInt(cnt++, Integer.parseInt(offset));
            rs1 = pstmt.executeQuery();
            while (rs1.next()) {
                tempobj = getJsonForAction(conn, pstmt, rs1, request, flag);
                jarr.put(tempobj);
            }
            resobj.put("data", jarr);
            resobj.append("count", count1);
            returnStr = resobj.toString();
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("AuditTrail.getLog", ex);
        } finally {
            return returnStr;
        }
    }

    public static String getAllLog(Connection conn, HttpServletRequest request, String companyid, String limit, String offset, String searchString, String dateString)
            throws ServiceException {
        ResultSet rs1 = null;
        String returnStr = "";
        String[] searchStrObj = new String[]{"params", "actiontext"};
        String myLikeString = StringUtil.getMySearchString(searchString, "and", searchStrObj);
        PreparedStatement pstmt = null;
        JSONObject resobj = new JSONObject();
        JSONObject tempobj = new JSONObject();
        JSONArray jarr = new JSONArray();
        try {
            int count1 = 0;
            pstmt = conn.prepareStatement("select count(*) AS count from actionlog al inner join actions a on al.actionid = a.actionid where companyid = ?" + myLikeString + dateString);
            pstmt.setString(1, companyid);
            StringUtil.insertParamSearchString(2, pstmt, searchString, searchStrObj.length);
            rs1 = pstmt.executeQuery();
            if (rs1.next()) {
                count1 = rs1.getInt("count");
            }
            String query1 = "select logid, al.actionid, actionby, projectid, params, timestamp from actionlog al inner join actions a on al.actionid = a.actionid where companyid = ?" + myLikeString + dateString + " order by timestamp DESC limit ? offset ?";
            pstmt = conn.prepareStatement(query1);
            pstmt.setString(1, companyid);
            int cnt = StringUtil.insertParamSearchString(2, pstmt, searchString, searchStrObj.length);
            pstmt.setInt(cnt++, Integer.parseInt(limit));
            pstmt.setInt(cnt++, Integer.parseInt(offset));
            rs1 = pstmt.executeQuery();
            while (rs1.next()) {
                tempobj = getJsonForAction(conn, pstmt, rs1, request, "all");
                jarr.put(tempobj);
            }
            resobj.put("data", jarr);
            resobj.put("count", count1);
            returnStr = resobj.toString();
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("AuditTrail.getAllLog", ex);
        } catch (SQLException ex) {
            throw ServiceException.FAILURE("AuditTrail.getAllLog", ex);
        } finally {
            return returnStr;
        }
    }

    public static String getDateString(Connection conn, HttpServletRequest request, String loginid)
            throws ServiceException {
        String d = "";
        try {
            String fromdate = request.getParameter("fromdate"), todate = request.getParameter("todate");
            if (!StringUtil.isNullOrEmpty(fromdate) && StringUtil.isNullOrEmpty(todate)) {
                fromdate = Timezone.fromCompanyToSystem(conn, fromdate, loginid);
                d = " and timestamp >= TIMESTAMP('" + fromdate + "')";
            } else if (!StringUtil.isNullOrEmpty(todate) && StringUtil.isNullOrEmpty(fromdate)) {
                todate = Timezone.fromCompanyToSystem(conn, todate, loginid);
                d = " and timestamp <= TIMESTAMP('" + todate + "')";
            } else if (!StringUtil.isNullOrEmpty(todate) && !StringUtil.isNullOrEmpty(fromdate)) {
                fromdate = Timezone.fromCompanyToSystem(conn, fromdate, loginid);
                todate = Timezone.fromCompanyToSystem(conn, todate, loginid);
                d = " and timestamp BETWEEN TIMESTAMP('" + fromdate + "') and TIMESTAMP('" + todate + "')";
            } else {
                d = "";
            }
        } catch (Exception e) {
            d = "";
        } finally {
            return d;
        }
    }

    public static String getActionLog(Connection conn, HttpServletRequest request) throws ServiceException {
        ResultSet rs = null;
        String returnStr = "";
        PreparedStatement pstmt = null;
        String searchString = request.getParameter("ss");
        String limit = request.getParameter("limit");
        String offset = request.getParameter("start");
        JSONObject resobj = new JSONObject();
        JSONObject tempobj = new JSONObject();
        JSONArray jarr = new JSONArray();
        String likeClause = "";
        try {
            String companyid = AuthHandler.getCompanyid(request);
            String loginid = AuthHandler.getUserid(request);
            String[] searchStrObj = new String[]{"params", "actiontext"};
            String myLikeString = StringUtil.getMySearchString(searchString, "and", searchStrObj);
            String flag = "";
            int count1 = 0;
            String type = request.getParameter("type");
            String dateSting = getDateString(conn, request, loginid);
            if (StringUtil.isNullOrEmpty(type)) {
                flag = "no";
                returnStr = getAllLog(conn, request, companyid, limit, offset, searchString, dateSting);
            } else {
                switch (Integer.parseInt(type)) {
                    case 0:
                        flag = "projname";
                        returnStr = getLog(conn, request, companyid, limit, offset, searchString, dateSting, flag);
                        flag = "no";
                        break;
                    case 1:
                        String admintype = request.getParameter("admintype");
                        if (admintype.equals("0")) {
                            likeClause = "and al.actionid like '31%' ";
                        } else if (admintype.equals("1")) {
                            likeClause = "and al.actionid like '32%' ";
                        } else {
                            likeClause = "and al.actionid like '33%' or al.actionid like '40%' ";
                        }
                        flag = "admin";
                        break;
                    case 2:
                        flag = "doc";
                        likeClause = "and al.actionid like '21%' ";
                        break;
                    case 3:
                        flag = "all";
                        likeClause = "and projectid != '' ";
                        break;
                    case 4:
                        flag = "admin";
                        likeClause = "and (al.actionid like '31%' or al.actionid like '32?' or al.actionid like '33?' or al.actionid like '40%')";
                        break;
                    case 5:
                        flag = "all";
                        likeClause = "and (al.actionid like '41%')";
                        break;
                }
            }
            if (!flag.equals("no")) {
                count1 = getCountQuery(conn, pstmt, companyid, likeClause, myLikeString, searchStrObj, searchString, dateSting);
                String query = "select logid, al.actionid, actionby, params, timestamp, projectid from actionlog al inner join actions a on al.actionid = a.actionid where companyid = ? "
                        + likeClause + myLikeString + dateSting + " order by timestamp DESC limit ? offset ?";
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, companyid);
                int cnt = StringUtil.insertParamSearchString(2, pstmt, searchString, searchStrObj.length);
                pstmt.setInt(cnt++, Integer.parseInt(limit));
                pstmt.setInt(cnt++, Integer.parseInt(offset));
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    tempobj = getJsonForAction(conn, pstmt, rs, request, flag);
                    jarr.put(tempobj);
                }
                resobj.put("data", jarr);
                resobj.append("count", count1);
                returnStr = resobj.toString();
            }
        } catch (ServiceException ex) {
            Logger.getLogger(AuditTrail.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AuditTrail.getActionLog :" + ex.getMessage(), ex);
        } finally {
            return returnStr;
        }
    }

    private static int getCountQuery(Connection conn, PreparedStatement pstmt, String companyid, String likeClause,
            String myLikeString, String[] searchStrObj, String ss, String dateString) throws ServiceException, SQLException {
        int cnt = 0;
        ResultSet rs = null;
        pstmt = conn.prepareStatement("select COUNT(*) AS count from actionlog al inner join actions a on al.actionid = a.actionid "
                + "where companyid = ? " + likeClause + myLikeString + dateString);
        pstmt.setString(1, companyid);
        StringUtil.insertParamSearchString(2, pstmt, ss, searchStrObj.length);
        rs = pstmt.executeQuery();
        if (rs.next()) {
            cnt = rs.getInt("count");
        }
        return cnt;
    }
}
