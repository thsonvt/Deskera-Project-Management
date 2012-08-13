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
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.timezone.Timezone;
import com.krawler.common.util.StringUtil;
import com.krawler.database.DbPool.Connection;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Abhay
 */
public class ImportLogHandler { 

    public static int updateTaskLog(Connection conn, String taskid, String log){
        int cnt = 0;
        try {
            cnt = DbUtil.executeUpdate(conn, "UPDATE tempImportData SET log = ? WHERE taskid = ?", new Object[]{log, taskid});
        } catch (ServiceException ex) {
            cnt = 0;
            throw ServiceException.FAILURE("ImportLogHandler.updateTaskLog : "+ex.getMessage(), ex);
        } finally {
            return cnt;
        }
    }

    public static int insertImportLog(Connection conn, String id, String fileRealName,
            String svnNamePath, String type, String log, String moduleName,
            String userid, String companyid, String projectid, String logkey, String moduleNameKey) {
        int cnt = 0;
        try {
            java.sql.Timestamp JavaDateNow = new java.sql.Timestamp(new java.util.Date().getTime());
            cnt = DbUtil.executeUpdate(conn, "INSERT INTO importlog VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                new Object[]{id, fileRealName, svnNamePath, type, log, -1, -1, -1,JavaDateNow, moduleName, userid, projectid, companyid, logkey, moduleNameKey});
        } catch (ServiceException ex) {
            cnt = 0;
            throw ServiceException.FAILURE("ImportLogHandler.insertImportLog : "+ex.getMessage(), ex);
        } finally {
            return cnt;
        }
    }
    public static int insertImportLog(Connection conn, String id, String fileRealName,
            String svnNamePath, String type, String log, String moduleName, int totalRecords,
            int importedRecords, int rejectedRecords, String userid, String companyid,
            String projectid, String logkey, String moduleNameKey) {
        int cnt = 0;
        try {
            java.sql.Timestamp JavaDateNow = new java.sql.Timestamp(new java.util.Date().getTime());
            cnt = DbUtil.executeUpdate(conn, "INSERT INTO importlog VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    new Object[]{id, fileRealName, svnNamePath, type, log, totalRecords, importedRecords, rejectedRecords, JavaDateNow, moduleName, userid, projectid, companyid, logkey, moduleNameKey});
        } catch (ServiceException ex) {
            cnt = 0;
            throw ServiceException.FAILURE("ImportLogHandler.insertImportLog : " + ex.getMessage(), ex);
        } finally {
            return cnt;
        }
    }

    public static int updateRecordCount(Connection conn, String id, String logMsg, String logkey, int total, int rejected, int errorInProcess){
        int cnt = 0;
        try {
            cnt = DbUtil.executeUpdate(conn, "UPDATE importlog SET totalrecs = ?, rejected = ?, errorcount = ?, log = ?, logkey = ? WHERE id = ?",
                    new Object[]{total, rejected, errorInProcess, logMsg, logkey, id});
        } catch (ServiceException ex) {
            cnt = 0;
            throw ServiceException.FAILURE("ImportLogHandler.updateRecordCount : "+ex.getMessage(), ex);
        } finally {
            return cnt;
        }
    }

    public static Object[] getRecordCount(Connection conn, String id) throws ServiceException {
        Object[] data = new Object[3];
        DbResults dr = DbUtil.executeQuery(conn, "SELECT totalrecs, rejected, logkey FROM importlog WHERE id = ?", new Object[]{id});
        if(dr.next()){
            data[0] = dr.getInt("totalrecs");
            data[1] = dr.getInt("rejected");
            data[2] = dr.getString("logkey");
        }
        return data;
    }
    
    public static int updateErrorsCount(Connection conn, String id, int errorInProcess){
        int cnt = 0;
        try {
            cnt = DbUtil.executeUpdate(conn, "UPDATE importlog SET errorcount = ? WHERE id = ?",
                new Object[]{errorInProcess, id});
        } catch (ServiceException ex) {
            cnt = 0;
            throw ServiceException.FAILURE("ImportLogHandler.updateErrorsCount : "+ex.getMessage(), ex);
        } finally {
            return cnt;
        }
    }

    public static String getImportLog(Connection conn, HttpServletRequest request) throws ServiceException{
        String returnStr = "";
        PreparedStatement pstmtCount = null, pstmtData = null;
        String searchString = request.getParameter("ss");
        String limit = request.getParameter("limit");
        String offset = request.getParameter("start");
        JSONObject resobj = new JSONObject();
        ResultSet rs = null;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Date d = new java.util.Date();
        JSONArray jarr = new JSONArray();
        try{
            String companyid = AuthHandler.getCompanyid(request);
            String loginid = AuthHandler.getUserid(request);
            String[] searchStrObj = new String[]{"filename", "log", "modulename"};
            String myLikeString = StringUtil.getMySearchString(searchString, "and", searchStrObj);
            int count = 0;
            if(StringUtil.isNullOrEmpty(limit))
                limit = "0";
            String dateString = AuditTrail.getDateString(conn, request, loginid);

            List l = DashboardHandler.checkProjectsForImportLog(conn, loginid, companyid);

            String IN = "";
            if(!l.isEmpty()){
                IN = " AND projectid IN(";
                Iterator ite = l.iterator();
                while (ite.hasNext()) {
                    String projid = (String)ite.next();
                    IN += "\'" + projid + "\',";
                }
                IN = IN.substring(0, IN.length()-1);
                IN = IN.concat(") ");
            }
            pstmtCount = conn.prepareStatement("SELECT count(*) AS count FROM importlog WHERE companyid = ? " + IN + myLikeString + dateString);

            String query = "(SELECT * FROM importlog WHERE companyid = ? AND userid=?) UNION (SELECT * FROM importlog WHERE companyid = ? " + IN + myLikeString + dateString + ") ORDER BY timestamp DESC limit ? offset ?";
            pstmtData = conn.prepareStatement(query);

            pstmtCount.setString(1, companyid);
            StringUtil.insertParamSearchString(2, pstmtCount, searchString, searchStrObj.length);
            rs = pstmtCount.executeQuery();
            if(rs.next())
                count = rs.getInt("count");

            pstmtData.setString(1, companyid);
            pstmtData.setString(2, loginid);
            pstmtData.setString(3, companyid);
            int cnt = StringUtil.insertParamSearchString(4, pstmtData, searchString, searchStrObj.length);
            pstmtData.setInt(cnt++, Integer.parseInt(limit));
            pstmtData.setInt(cnt++, Integer.parseInt(offset));

            rs = pstmtData.executeQuery();
            while(rs.next()){
                boolean flag = false;
                String name = "";
                if(!StringUtil.isNullOrEmpty(rs.getString("projectid"))){
                    name = projdb.getProjectName(conn, rs.getString("projectid")); // check if project is present
                    if(StringUtil.isNullOrEmpty(name))
                        flag = false;
                    else
                        flag = true;
                } else {
                    name = "--";
                    flag = true;
                }
                if(flag){
                    String postTime = Timezone.toCompanyTimezone(conn, rs.getObject("timestamp").toString(), companyid);
//                    postTime = Timezone.dateTimeRenderer(conn, Timezone.toUserTimezone(conn, sdf.format(d), loginid), postTime, loginid);
//                    postTime = Timezone.convertToUserPref(conn, postTime, loginid);
                    JSONObject tempobj = new JSONObject();
                    tempobj.put("projectname", name);
                    int importedrec = rs.getInt("totalrecs") - rs.getInt("rejected");
                    tempobj.put("id",rs.getString("id"));
                    tempobj.put("filename", rs.getString("filename"));
                    tempobj.put("svnname", rs.getString("svnname"));
                    tempobj.put("type", rs.getString("type"));
                    tempobj.put("log", rs.getString("log"));
                    tempobj.put("logkey", rs.getString("logkey"));
                    tempobj.put("totalrecs", rs.getInt("totalrecs"));
                    tempobj.put("rejected", rs.getInt("rejected"));
                    tempobj.put("timestamp", postTime);
                    tempobj.put("importedrec", importedrec);
                    tempobj.put("errorcount", rs.getInt("errorcount"));
                    tempobj.put("modulename", rs.getString("modulenamekey"));
                    name = AuthHandler.getAuthor(conn, rs.getString("userid"));
                    tempobj.put("username", name);
                    jarr.put(tempobj);
                }
            }
            resobj.put("data", jarr);
            resobj.put("count", count);
            returnStr = resobj.toString();
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("ImportLogHandler.getImportLog : "+ex.getMessage(), ex);
        } catch (SQLException ex) {
            throw ServiceException.FAILURE("ImportLogHandler.getImportLog : "+ex.getMessage(), ex);
        } catch(JSONException ex) {
            throw ServiceException.FAILURE("ImportLogHandler.getImportLog : "+ex.getMessage(), ex);
        } catch(NullPointerException ex) {
            throw ServiceException.FAILURE("ImportLogHandler.getImportLog : "+ex.getMessage(), ex);
        } finally {
            return returnStr;
        }
    }

    public static int deleteLog(Connection conn, String id){
        int cnt = 0;
        try {
            cnt = DbUtil.executeUpdate(conn, "DELETE FROM importlog WHERE id = ?", id);
        } catch (ServiceException ex) {
            cnt = 0;
            throw ServiceException.FAILURE("ImportLogHandler.deleteLog : "+ex.getMessage(), ex);
        } finally {
            return cnt;
        }
    }
}
