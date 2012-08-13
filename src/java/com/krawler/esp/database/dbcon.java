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
package com.krawler.esp.database;

import com.krawler.common.locale.MessageSourceProxy;
import com.krawler.common.permission.PermissionConstants;
import com.krawler.common.permission.PermissionManager;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.timezone.Timezone;
import com.krawler.common.util.KrawlerLog;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.KWLErrorMsgs;
import com.krawler.database.DbPool;
import com.krawler.database.DbPool.Connection;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.esp.Search.SearchBean;
import com.krawler.esp.company.CompanyDAO;
import com.krawler.esp.company.CompanyDAOImpl;
import com.krawler.esp.handlers.AuditTrail;
import com.krawler.esp.handlers.AuthHandler;
import com.krawler.esp.handlers.CompanyHandler;
import com.krawler.esp.handlers.DashboardHandler;
import com.krawler.esp.handlers.FileHandler;
import com.krawler.esp.handlers.FilePermission;
import com.krawler.esp.handlers.Forum;
import com.krawler.esp.handlers.MessengerHandler;
import com.krawler.esp.handlers.ProfileHandler;
import com.krawler.esp.handlers.SearchHandler;
import com.krawler.esp.handlers.SignupHandler;
import com.krawler.esp.handlers.TagHandler;
import com.krawler.esp.handlers.Tree;
import com.krawler.esp.handlers.WidgetStateHandler;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.esp.handlers.calEvent;
import com.krawler.esp.handlers.projdb;
import com.krawler.esp.handlers.todolist;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class dbcon {

    public static Hashtable getfileinfo(String name) {
        Connection conn = null;
        Hashtable ht = new Hashtable();
        try {
            conn = DbPool.getConnection();
            ht = FileHandler.getfileinfo(conn, name);
            conn.commit();
        } catch (ServiceException ex) {
            KrawlerLog.op.warn(KWLErrorMsgs.errFileInfo + ex.toString());
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return ht;
    }

    public static Hashtable[] getfilesinfo(String name) {
        Connection conn = null;
        String nameTemp = name.substring(0, name.length() - 1);
        String len[] = nameTemp.split(",");
        Hashtable ht1[] = new Hashtable[len.length];
        try {
            conn = DbPool.getConnection();
            Hashtable ht[] = FileHandler.getfilesinfo(conn, nameTemp);
            ht1 = ht;
            conn.commit();
        } catch (ServiceException ex) {
            KrawlerLog.op.warn(KWLErrorMsgs.errFileInfo + ex.toString());
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }

        return ht1;
    }

    public static ArrayList<String> getFriendList(String name) {
        Connection conn = null;
        ArrayList<String> ht = new ArrayList<String>();
        try {
            conn = DbPool.getConnection();
            ht = ProfileHandler.getFriendList(conn, name);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return ht;
    }

    public static ArrayList<String> getonlineusers() {
        Connection conn = null;
        ArrayList<String> ht = new ArrayList<String>();
        try {
            conn = DbPool.getConnection();
            ht = FileHandler.getonlineusers(conn);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return ht;
    }

    public static ArrayList<String> getShareddocusers(String docname) {
        Connection conn = null;
        ArrayList<String> ht = new ArrayList<String>();
        try {
            conn = DbPool.getConnection();
            ht = FileHandler.getShareddocusers(conn, docname);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return ht;
    }

    public static String QuickSearchData(String Type, String Keyword,
            String companyid) {
        Connection conn = null;
        String str = null;
        try {
            conn = DbPool.getConnection();
            str = SearchHandler.QuickSearchData(conn, Type, Keyword, companyid);
            // conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn(KWLErrorMsgs.errSearchData + ex.toString());
        } finally {
            DbPool.quietClose(conn);
        }
        return str;
    }

    public static String searchIndex(SearchBean bean, String querytxt,
            String numhits, String perpage, String startIn, String companyid,
            String userid) throws IOException {
        Connection conn = null;
        String responseText = null;
        try {
            conn = DbPool.getConnection();
            responseText = SearchHandler.searchIndex(conn, bean, querytxt,
                    numhits, perpage, startIn, companyid, userid);
            // conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn(KWLErrorMsgs.errSearchIndex + ex.toString());
        } finally {
            DbPool.quietClose(conn);
        }
        return responseText;
    }

    public static String getProjectMembers(String ulogin, int pagesize,
            int offset) {
        Connection conn = null;
        String responseText = null;
        try {
            conn = DbPool.getConnection();
            responseText = Forum.getProjectMembers(conn, ulogin, pagesize,
                    offset);
            // conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return responseText;
    }

    public static String getProjectMembersfordashboard(String ulogin, int pagesize,
            int offset) {
        Connection conn = null;
        String responseText = null;
        try {
            conn = DbPool.getConnection();
            responseText = Forum.getProjectMembersfordashboard(conn, ulogin, pagesize,
                    offset);
            // conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return responseText;
    }

    public static String getProjectResources(String pid, int pagesize,
            int offset, String ss) throws JSONException {
        Connection conn = null;
        String responseText = null;
        try {
            conn = DbPool.getConnection();
            responseText = projdb.getProjectResources(conn, pid, offset, pagesize, ss);
            // conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } catch (Exception ex) {
            System.out.print(ex.toString());
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return responseText;
    }

    public static void saveProjState(String userid, String projid, String featureid, String planview, String statevar) {
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            projdb.saveProjState(conn, userid, projid, featureid, planview, statevar);
            conn.commit();
        } catch (ServiceException e) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
    }

    public static String getProjState(String userid, String projid, String featureid) {
        String ret = "";
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            ret = projdb.getProjState(conn, userid, projid, featureid);
        } catch (ServiceException e) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }

    public static String getAllResourcesInCompany(HttpServletRequest request) {
        String ret = "";
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            ret = projdb.getAllResourcesInCompany(conn, request);
        } catch (ServiceException e) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }

    public static String getProjectResources(String pid) {
        Connection conn = null;
        String responseText = null;
        try {
            conn = DbPool.getConnection();
            responseText = projdb.getProjectResources(conn, pid);
            // conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return responseText;
    }
//        public static String getbillableProjectResources(String pid) throws ServiceException {
//		Connection conn = null;
//		String responseText = null;
//		try {
//			conn = DbPool.getConnection();
//			responseText = projdb.getbillableProjectResources(conn, pid);
////			 conn.commit();
//		} catch (ServiceException ex) {
//			DbPool.quietRollback(conn);
//                    KrawlerLog.op.warn("dbcon.gettbillableProjectResources():");
//                    throw ServiceException.FAILURE("",ex);
//		} finally {
//			DbPool.quietClose(conn);
//		}
//		return responseText;
//	}

    public static String getResourceCategories() {
        Connection conn = null;
        String responseText = null;
        try {
            conn = DbPool.getConnection();
            responseText = projdb.getResourceCategories(conn);
            // conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return responseText;
    }

    public static String getResourceTypes() {
        Connection conn = null;
        String responseText = null;
        try {
            conn = DbPool.getConnection();
            responseText = projdb.getResourceTypes(conn);
            // conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return responseText;
    }

    public static String getBaselineResources(String baselineID) {
        Connection conn = null;
        String responseText = null;
        try {
            conn = DbPool.getConnection();
            responseText = projdb.getBaselineResources(conn, baselineID);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return responseText;
    }

    public static String insertResource(HttpServletRequest request) throws SessionExpiredException {
        Connection conn = null;
        String responseText = "";
        try {
            conn = DbPool.getConnection();
            responseText = projdb.insertResource(conn, request);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return responseText;
    }

    public static String editResource(HttpServletRequest request) {
        Connection conn = null;
        String responseText = null;
        try {
            conn = DbPool.getConnection();
            responseText = projdb.editResource(conn, request);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return responseText;
    }

    public static String deleteResource(HttpServletRequest request) {
        Connection conn = null;
        String responseText = null;
        try {
            conn = DbPool.getConnection();
            responseText = projdb.deleteResource(conn, request);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return responseText;
    }

    public static String preDeleteResource(HttpServletRequest request) {
        Connection conn = null;
        String responseText = null;
        try {
            conn = DbPool.getConnection();
            responseText = projdb.preDeleteResource(conn, request);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return responseText;
    }

    public static String createResType(HttpServletRequest request) {
        Connection conn = null;
        String responseText = null;
        try {
            conn = DbPool.getConnection();
            responseText = projdb.createResourceCategory(conn, request);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return responseText;
    }

    public static String setActive(HttpServletRequest request) {
        Connection conn = null;
        String responseText = null;
        try {
            conn = DbPool.getConnection();
            responseText = projdb.setActive(conn, request);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return responseText;
    }

    public static String setBillable(HttpServletRequest request) {
        Connection conn = null;
        String responseText = null;
        try {
            conn = DbPool.getConnection();
            responseText = projdb.setBillable(conn, request);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return responseText;
    }

    public static String getProjectMembership(String userid, String communityId) {
        Connection conn = null;
        String responseText = null;
        try {
            conn = DbPool.getConnection();
            responseText = Forum.getProjectMembership(conn, userid, communityId);
            // conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return responseText;
    }

    public static String getProjectMemberStatus(String userid, String projid) {
        Connection conn = null;
        String responseText = null;
        try {
            conn = DbPool.getConnection();
            responseText = Forum.getProjectMemberStatus(conn, userid, projid);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return responseText;
    }

    public static String getRelatedProjects(String ulogin, int offset,
            int pagesize) {
        Connection conn = null;
        String responseText = null;
        try {
            conn = DbPool.getConnection();
            responseText = Forum.getRelatedProjects(conn, ulogin, offset,
                    pagesize);
            // conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return responseText;
    }

    public static String getProjectDetails(String ulogin, String loginid) {
        Connection conn = null;
        String responseText = null;
        try {
            conn = DbPool.getConnection();
            responseText = Forum.getProjectDetails(conn, ulogin, loginid);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return responseText;
    }

    public static String getDocType(String docid) {
        Connection conn = null;
        String responseText = null;
        try {
            conn = DbPool.getConnection();
            responseText = FileHandler.getDocType(conn, docid);
            // conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return responseText;
    }

    public static int getNextRow() {
        int ret = -1;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            ret = projdb.getNextRow(conn);
            // conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }

    public static String getMaxTimestamp() {
        String ret = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            ret = projdb.getMaxTimestamp(conn);
            // conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }

    public static String getTask(String projectid, int offset, int limit) throws ServiceException {
        String ret = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            ret = projdb.getTask(conn, projectid, offset, limit);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn("dbcon.getTask():");
            throw ServiceException.FAILURE("", ex);
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }

    public static String getNWWeekAndCompHolidays(String projectid) throws ServiceException {
        String ret = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            ret = projdb.getNWWeekAndCompHolidays(conn, projectid);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn("dbcon.getNWWeekAndCompHolidays():");
            System.out.print(ex);
            throw ServiceException.FAILURE(KWLErrorMsgs.exgetNWWeekAndCompHolidays, ex);
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }

    public static String getNonWorkWeekdays(String projectid) throws ServiceException {
        String ret = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            ret = projdb.getNonWorkWeekdays(conn, projectid);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            System.out.print(ex);
            throw ServiceException.FAILURE(KWLErrorMsgs.exgetNonWorkWeekdays, ex);
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }

    public static String getCmpHolidaydays(String projectid) throws ServiceException {
        String ret = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            ret = projdb.getCmpHolidaydays(conn, projectid);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            System.out.print(ex);
            throw ServiceException.FAILURE(KWLErrorMsgs.exgetCmpHolidaydays, ex);
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }

    public static void InsertTask(com.krawler.utils.json.base.JSONObject jobj,
            String taskid, String projId, String rowindex) throws ServiceException {
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            projdb.InsertTask(conn, jobj, taskid, projId, rowindex);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn("dbcon.InsertTask():");
            throw ServiceException.FAILURE("", ex);
        } finally {
            DbPool.quietClose(conn);
        }
    }

    public static void UpdateParentStatus(String taskid, boolean flag) throws ServiceException {
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            projdb.UpdateParentStatus(conn, taskid, flag);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn("dbcon.UpdateParentStatus():");
            throw ServiceException.FAILURE("", ex);
        } finally {
            DbPool.quietClose(conn);
        }
    }

    public static void updateColorCode(String color, String resid, String projid) throws ServiceException {
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            projdb.updateColorCode(conn, color, resid, projid);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn("dbcon.updateColorCode():");
            throw ServiceException.FAILURE("", ex);
        } finally {
            DbPool.quietClose(conn);
        }
    }

    public static String getTaskResources(String taskId, String projid) throws ServiceException {
        String ret = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            ret = projdb.getTaskResources(conn, taskId, projid);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn("dbcon.getTaskResources():");
            throw ServiceException.FAILURE("", ex);
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }

    public static String getBillableTaskResources(String taskId, String projid) throws ServiceException {
        String ret = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            ret = projdb.getBillableTaskResources(conn, taskId, projid);
//			conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn("dbcon.getBillableTaskResources():");
            throw ServiceException.FAILURE("", ex);
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }

//	public static String getResourceRelatedTask(String resourcename,
//			String projectid) throws ServiceException {
//		String ret = null;
//		Connection conn = null;
//		try {
//			conn = DbPool.getConnection();
//			ret = projdb.getResourceRelatedTask(conn, resourcename, projectid);
//			conn.commit();
//		} catch (ServiceException ex) {
//			DbPool.quietRollback(conn);
//                    KrawlerLog.op.warn("dbcon.getResourceRelatedTask():");
//			throw ServiceException.FAILURE(KWLErrorMsgs.exgetResourceRelatedTask,ex);
//		} finally {
//			DbPool.quietClose(conn);
//		}
//		return ret;
//	}
    public static String getTaskResourcesOnDataload(String taskId, String projid) throws ServiceException {
        String ret = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            ret = projdb.getTaskResourcesOnDataload(conn, taskId, projid);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn("dbcon.getTaskResourcesOnDataload():");
            throw ServiceException.FAILURE("", ex);
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }

    public static String insertTemplate(String templateid, String jsonstr,
            String name, String uid, String pid, String description, String userid) {
        String ret = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            ret = projdb.insertTemplate(conn, templateid, jsonstr, name, uid,
                    pid, description, userid);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }

    public static String insertReportTemplate(String templateid, String jsonstr,
            String name, String uid, String description) {
        String ret = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            ret = projdb.insertReportTemplate(conn, templateid, jsonstr, name, uid,
                    description);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }

    public static String getAllTemplates(String projid, String companyid) {
        String ret = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            ret = projdb.getAllTemplates(conn, projid, companyid);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }

    public static String getAllReportTemplates() {
        String ret = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            ret = projdb.getAllReportTemplates(conn);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }

    public static String getTemplate(String tempid, String userid) {
        String ret = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            ret = projdb.getTemplate(conn, tempid, userid);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }

    public static String deleteTemplate(String tid) {
        Connection conn = null;
        String result = "";
        try {
            conn = DbPool.getConnection();
            result = projdb.deleteTemplate(conn, tid);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } catch (JSONException exp) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }
    /*
    public static String getProjResources(String projId) {
    String ret = null;
    Connection conn = null;
    try {
    conn = DbPool.getConnection();
    ret = projdb.getProjResources(conn, projId);
    // conn.commit();
    } catch (ServiceException ex) {
    DbPool.quietRollback(conn);
    } finally {
    DbPool.quietClose(conn);
    }
    return ret;
    }
     */
    /*	public static void checkForProjectMeemberInProj_resource(String projectId,
    String projectMemberList) {
    Connection conn = null;
    try {
    conn = DbPool.getConnection();
    projdb.checkForProjectMeemberInProj_resource(conn, projectId,
    projectMemberList);
    conn.commit();
    } catch (ServiceException ex) {
    DbPool.quietRollback(conn);
    } finally {
    DbPool.quietClose(conn);
    }
    }
     */

    public static String getPredecessor(String taskid) throws ServiceException {
        String ret = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            ret = projdb.getPredecessor(conn, taskid);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn("dbcon.getPredecessor():");
            throw ServiceException.FAILURE("", ex);
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }

    public static String getPredecessorIndex(String taskid) throws ServiceException {
        String ret = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            ret = projdb.getPredecessorIndex(conn, taskid);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn("dbcon.getPredecessorIndex():");
            throw ServiceException.FAILURE("", ex);
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }

//	public static double[] getTaskCostWork(String taskid, String projid, int percent) throws ServiceException {
//		double[] ret = null;
//		Connection conn = null;
//		try {
//			conn = DbPool.getConnection();
//			ret = projdb.getTaskCostWork(conn, taskid, projid, percent);
//		} catch (ServiceException ex) {
//			DbPool.quietRollback(conn);
//                    KrawlerLog.op.warn("dbcon.getTaskCostWork():");
//                    throw ServiceException.FAILURE("",ex);
//		} finally {
//			DbPool.quietClose(conn);
//		}
//		return ret;
//	}
    public static void addlink(String fromId, String toId) throws ServiceException {
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            projdb.addLink(conn, fromId, toId);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn("dbcon.addlink():");
            throw ServiceException.FAILURE("", ex);
        } finally {
            DbPool.quietClose(conn);
        }
    }

    public static void deletelink(String fromId, String toId) throws ServiceException {
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            projdb.deleteLink(conn, fromId, toId);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn("dbcon.deletelink():");
            throw ServiceException.FAILURE("", ex);
        } finally {
            DbPool.quietClose(conn);
        }
    }

    public static void deleteTask(String taskid, String projectId) throws ServiceException {
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            projdb.deleteTask(conn, taskid, projectId);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn("dbcon.deleteTask():");
            throw ServiceException.FAILURE("", ex);
        } finally {
            DbPool.quietClose(conn);
        }
    }

    public static void updateParentFieldOfRecord(String taskid,
            String parenttaskId) throws ServiceException {
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            projdb.updateParentFieldOfRecord(conn, taskid, parenttaskId);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn("dbcon.updateParentFieldOfRecord():");
            throw ServiceException.FAILURE("", ex);
        } finally {
            DbPool.quietClose(conn);
        }
    }

    public static String saveBaseline(String projectId, String userId, String baselineName,
            String baselineDesc) throws ServiceException {

        Connection conn = null;
        String res = "";
        try {
            conn = DbPool.getConnection();
            res = projdb.saveBaseline(conn, projectId, userId, baselineName, baselineDesc);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn("dbcon.saveBaseline():");
            throw ServiceException.FAILURE("", ex);
        } finally {
            DbPool.quietClose(conn);
        }
        return res;
    }

    public static String getbaseline(String projectId, String userid) throws ServiceException {
        Connection conn = null;
        String ret = null;
        try {
            conn = DbPool.getConnection();
            ret = projdb.getBaseline(conn, projectId, userid);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }

//    public static String getCompareProjectData(String projectid) throws ServiceException{
//        Connection conn = null;
//        String r = "";
//        try{
//            conn = DbPool.getConnection();
//            r = projdb.getCompareProjectData(conn, projectid);
//        } catch(ServiceException e){
//            throw ServiceException.FAILURE(e.getMessage(), e);
//        } finally {
//			DbPool.quietClose(conn);
//		}
//        return r;
//    }
    public static String getCompareBaselineData(String baselineid, String projectid, String userid) throws ServiceException, ParseException {
        Connection conn = null;
        String r = "";
        try {
            conn = DbPool.getConnection();
            r = projdb.getCompareBaselineData(conn, baselineid, projectid, userid);
        } catch (ServiceException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } catch (ParseException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } finally {
            DbPool.quietClose(conn);
        }
        return r;
    }

    public static String getBaselineData(String baselineid, String projectId, String userid) throws ServiceException {
        Connection conn = null;
        String ret = null;
        try {
            conn = DbPool.getConnection();
            ret = projdb.getBaselineData(conn, baselineid, projectId, userid);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }

    public static String deleteBaseline(String projid, String baselineid) throws ServiceException {
        Connection conn = null;
        String ret = null;
        try {
            conn = DbPool.getConnection();
            ret = projdb.deleteBaseline(conn, projid, baselineid);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }

    public static boolean checkBaseline(String projid) throws ServiceException {
        Connection conn = null;
        boolean ret = false;
        try {
            conn = DbPool.getConnection();
            ret = projdb.checkBaseline(conn, projid);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }

    public static void deleteAllRecords(String projectId) throws ServiceException {
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            projdb.deleteAllRecords(conn, projectId);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn("dbcon.deleteAllRecords():");
            throw ServiceException.FAILURE("", ex);
        } finally {
            DbPool.quietClose(conn);
        }
    }

    public static boolean addButtonTag(String userid, String docid,
            String tagname) {
        Connection conn = null;
        boolean flg = true;
        try {
            conn = DbPool.getConnection();
            TagHandler.addButtonTag(conn, userid, docid, tagname);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn(KWLErrorMsgs.errAddTag + ex.toString());
            flg = false;
        } finally {
            DbPool.quietClose(conn);
        }
        return flg;
    }

    public static Hashtable FileChk(String docname, String userid,
            String groupid, String pcid) {
        Connection conn = null;
        Hashtable responseText = null;
        try {
            conn = DbPool.getConnection();
            responseText = FileHandler.FileChk(conn, docname, userid, groupid,
                    pcid);
            // conn.commit();
        } catch (ServiceException ex) {
            KrawlerLog.op.warn(KWLErrorMsgs.errCheckingFile + ex.toString());
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return responseText;
    }

    public static boolean tagStore(String userid, String docid, String tagnames) {
        Connection conn = null;
        boolean flg = true;
        try {
            conn = DbPool.getConnection();
            TagHandler.tagStore(conn, userid, docid, tagnames);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn(KWLErrorMsgs.errInsertTag + ex.toString());
            flg = false;
        } finally {
            DbPool.quietClose(conn);
        }
        return flg;
    }

    public static String fillGrid(String userid, String tagname,
            String groupid, String pcid, String companyid, String searchJson) {
        Connection conn = null;
        String gridString = null;
        try {
            conn = DbPool.getConnection();
            gridString = FileHandler.fillGrid(conn, userid, tagname, groupid, pcid, companyid, searchJson);
            // conn.commit();
        } catch (ServiceException ex) {
            KrawlerLog.op.warn(KWLErrorMsgs.errLoadingData + ex.toString());
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return gridString;
    }

    public static String fillGridForSpecificProject(String userid, String tagname,
            String groupid, String pcid, String companyid, String pid, String searchJson) {
        Connection conn = null;
        String gridString = null;
        try {
            conn = DbPool.getConnection();
            gridString = FileHandler.fillGridForSpecificProject(conn, userid, tagname, groupid, pcid, companyid, pid, searchJson);
            // conn.commit();
        } catch (ServiceException ex) {
            KrawlerLog.op.warn(KWLErrorMsgs.errLoadingData + ex.toString());
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return gridString;
    }

    public static boolean updateStatus(String userid, String docid,
            String status) {

        Connection conn = null;
        boolean stat = true;
        try {
            conn = DbPool.getConnection();
            FilePermission.updateStatus(conn, userid, docid, status);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn(KWLErrorMsgs.errSetStatusFile + ex.toString());
            stat = false;
        } finally {
            DbPool.quietClose(conn);
        }
        return stat;
    }

    public static void deleteDoc(String docid, String userid, int flag, String companyid, String projectid) {
        Connection conn = null;

        try {
            conn = DbPool.getConnection();
            String docname = FileHandler.getDocName(conn, docid);
            FileHandler.deleteDoc(conn, docid, userid, flag, companyid);
            String params = AuthHandler.getAuthor(conn, userid) + " (" + AuthHandler.getUserName(conn, userid) + "), "
                    + docname;
            if (projectid.equals("")) {
                AuditTrail.insertLog(conn, "215", userid, docid, "",
                        companyid, params, "", 0);
            } else {
                AuditTrail.insertLog(conn, "343", userid, docid, projectid,
                        companyid, params, "", 0);
            }
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
    }

    public static void Setversion(String docid, String userid) {
        Connection conn = null;

        try {
            conn = DbPool.getConnection();
            FileHandler.Setversion(conn, docid, userid);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
    }

    public static void fildoc(String docid, String docname, String docsize,
            String docdatemod, String permission, String status,
            String docrevision, String pcid, String groupid, String userid,
            String type, String comment, String tags, String svnName, String projectid) {
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            FileHandler.fildoc(conn, docid, docname, docsize, docdatemod,
                    permission, status, docrevision, pcid, groupid, userid,
                    type, comment, tags, svnName, projectid);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn(KWLErrorMsgs.errFillDocGrid + ex.toString());
        } finally {
            DbPool.quietClose(conn);
        }
    }

    public static boolean deleteTag(String userid, String tagname) {
        Connection conn = null;
        boolean flg = true;
        try {
            conn = DbPool.getConnection();
            TagHandler.deleteTag(conn, userid, tagname);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn(KWLErrorMsgs.errDeleteFileTag + ex.toString());
            flg = false;
        } finally {
            DbPool.quietClose(conn);
        }
        return flg;
    }

    public static boolean editTag(String userid, String oldTagName,
            String newTagName) {
        Connection conn = null;
        boolean flg = true;
        try {
            conn = DbPool.getConnection();
            TagHandler.editTag(conn, userid, oldTagName, newTagName);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn(KWLErrorMsgs.errEditFileTag + ex.toString());
            flg = false;
        } finally {
            DbPool.quietClose(conn);
        }
        return flg;
    }

    public static void Delete(String docid) {
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            FilePermission.Delete(conn, docid);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
    }

    public static void Insert(String docid, String userid, String type, int rw) {

        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            FilePermission.Insert(conn, docid, userid, type, rw);
            conn.commit();
        } catch (ServiceException ex) {
            KrawlerLog.op.warn(KWLErrorMsgs.errInsert + ex.toString());
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }

    }

    public static String getConnections(String userid, String docid) {
        Connection conn = null;
        String str = null;
        try {
            conn = DbPool.getConnection();
            str = FilePermission.getConnections(conn, userid, docid);
            // conn.commit();
        } catch (ServiceException ex) {
            KrawlerLog.op.warn(KWLErrorMsgs.errGetConnections + ex.toString());
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return str;
    }

    public static String getConnectionsRightGrid(String userid, String docid) {
        Connection conn = null;
        String str = null;
        try {
            conn = DbPool.getConnection();
            str = FilePermission.getConnectionsRightGrid(conn, userid, docid);
            // conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn(KWLErrorMsgs.errGetConnectionsRightGrid
                    + ex.toString());
        } finally {
            DbPool.quietClose(conn);
        }
        return str;
    }

    public static String getProjects(String userid, String docid) {
        Connection conn = null;
        String str = null;
        try {
            conn = DbPool.getConnection();
            str = FilePermission.getProjects(conn, userid, docid);
            // conn.commit();
        } catch (ServiceException ex) {
            KrawlerLog.op.warn(KWLErrorMsgs.errGetConnections + ex.toString());
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return str;
    }

    public static String getProjectsRightGrid(String userid, String docid) {
        Connection conn = null;
        String str = null;
        try {
            conn = DbPool.getConnection();
            str = FilePermission.getProjectsRightGrid(conn, userid, docid);
            // conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn(KWLErrorMsgs.errGetConnectionsRightGrid
                    + ex.toString());
        } finally {
            DbPool.quietClose(conn);
        }
        return str;
    }

    public static String getFriendlistDetails(String loginid) {
        String result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = MessengerHandler.getFriendlistDetails(conn, loginid);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String getFriendlistDetail(String loginid) {
        String result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = MessengerHandler.getFriendlistDetail(conn, loginid);
        } catch (JSONException ex) {
            DbPool.quietRollback(conn);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static Object[] getFriendListArray(String loginid) throws ServiceException {
        Object[] result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = MessengerHandler.getFriendListArray(conn, loginid);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn("dbcon.getFriendListArray():");
            throw ServiceException.FAILURE("", ex);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String insertChatmessages(String sendid, String receiveid,
            String message, int rsflag, int readflag) {
        String result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = MessengerHandler.insertChatmessages(conn, sendid,
                    receiveid, message, rsflag, readflag);
        } catch (Exception ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String getFriendList(String ulogin, int pagesize, int offset, String ss) {
        String result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = ProfileHandler.getFriendList(conn, ulogin, pagesize,
                    offset, ss);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String addContacts(String userid, String requestto) {
        Connection conn = null;
        String result = null;
        try {
            conn = DbPool.getConnection();
            result = ProfileHandler.addContacts(conn, userid, requestto);
            conn.commit();
        } catch (JSONException ex) {
            Logger.getLogger(dbcon.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String getNewContacts(String userid, String companyid, int limit, int start, String ss) {
        Connection conn = null;
        String result = null;
        try {
            conn = DbPool.getConnection();
            result = ProfileHandler.getNewContacts(conn, userid, companyid, limit, start, ss);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String getMyContacts(String userid, int limit, int start, String ss) {
        Connection conn = null;
        String result = null;
        try {
            conn = DbPool.getConnection();
            result = ProfileHandler.getMyContacts(conn, userid, limit, start, ss);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } catch (JSONException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String getAllContacts(String userid) {
        Connection conn = null;
        String result = null;
        try {
            conn = DbPool.getConnection();
            result = ProfileHandler.getAllContacts(conn, userid);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } catch (JSONException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String newAddress(String userid, String username, String emailid, String address, String contactno) {
        Connection conn = null;
        String result = null;
        try {
            conn = DbPool.getConnection();
            String contactid = UUID.randomUUID().toString();
            result = ProfileHandler.newAddress(conn, contactid, userid, username, emailid, address, contactno);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } catch (SQLException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String repContact(String userMap) {
        Connection conn = null;
        String result = null;
        try {
            conn = DbPool.getConnection();
            result = ProfileHandler.repContact(conn, userMap);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } catch (JSONException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String acceptContacts(String userid, String requestto) {
        Connection conn = null;
        String result = null;
        try {
            conn = DbPool.getConnection();
            result = ProfileHandler.acceptContacts(conn, userid, requestto);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } catch (JSONException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String deleteContacts(String userid, String requestto) {
        Connection conn = null;
        String result = null;
        try {
            conn = DbPool.getConnection();
            result = ProfileHandler.deleteContacts(conn, userid, requestto);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } catch (JSONException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String getUserCommunities(String ulogin, int pagesize,
            int offset) {
        String result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = ProfileHandler.getUserCommunities(conn, ulogin, pagesize,
                    offset);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String getUserCommunitiesMember(String ulogin, int pagesize,
            int offset) {
        String result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = ProfileHandler.getUserCommunitiesMember(conn, ulogin, pagesize,
                    offset);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String getMyTags(String ulogin) {
        String result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = ProfileHandler.getMyTags(conn, ulogin);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String getUserRelation(String userid1, String userid2) {
        String result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = ProfileHandler.getUserRelation(conn, userid1, userid2);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String getUserDetails(String ulogin) {
        String result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = ProfileHandler.getUserDetails(conn, ulogin);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String setUserrelation(String loginid, String companyid, String uuserid1,
            String uuserid2, String relationid1, int urelationid, int actionid,
            java.util.Date dateval, java.text.SimpleDateFormat dtformat) {
        String result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = ProfileHandler.setUserrelation(conn, loginid, companyid, uuserid1,
                    uuserid2, relationid1, urelationid, actionid, dateval,
                    dtformat);
            conn.commit();
        } catch (ParseException ex) {
            DbPool.quietRollback(conn);
        } catch (JSONException ex) {
            DbPool.quietRollback(conn);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

//    public static String getCommunityMembers(String communityId, int offset,
//            int pagesize) {
//        String result = null;
//        Connection conn = null;
//        try {
//            conn = DbPool.getConnection();
//            result = CommunityHandler.getCommunityMembers(conn, communityId,
//                    offset, pagesize);
//        } catch (ServiceException ex) {
//            DbPool.quietRollback(conn);
//        } finally {
//            DbPool.quietClose(conn);
//        }
//        return result;
//    }
//
//    public static String getMembershipStatus(String userid, String communityId) {
//        String result = null;
//        Connection conn = null;
//        try {
//            conn = DbPool.getConnection();
//            result = CommunityHandler.getMembershipStatus(conn, userid,
//                    communityId);
//        } catch (ServiceException ex) {
//            DbPool.quietRollback(conn);
//        } finally {
//            DbPool.quietClose(conn);
//        }
//        return result;
//    }
//
//    public static String getRelatedCommunities(String ulogin, int offset,
//            int pagesize) {
//        String result = null;
//        Connection conn = null;
//        try {
//            conn = DbPool.getConnection();
//            result = CommunityHandler.getRelatedCommunities(conn, ulogin,
//                    offset, pagesize);
//        } catch (ServiceException ex) {
//            DbPool.quietRollback(conn);
//        } finally {
//            DbPool.quietClose(conn);
//        }
//        return result;
//    }
//
//    public static String getCommunityDetails(String ulogin) {
//        String result = null;
//        Connection conn = null;
//        try {
//            conn = DbPool.getConnection();
//            result = CommunityHandler.getCommunityDetails(conn, ulogin);
//        } catch (ServiceException ex) {
//            DbPool.quietRollback(conn);
//        } finally {
//            DbPool.quietClose(conn);
//        }
//        return result;
//    }
//
//    public static String setStatusCommunity(String userid, String comid,
//            int urelationid, int actionid) {
//        String result = null;
//        Connection conn = null;
//        try {
//            conn = DbPool.getConnection();
//            result = CommunityHandler.setStatusCommunity(conn, userid, comid,
//                    urelationid, actionid);
//            conn.commit();
//        } catch (ServiceException ex) {
//            DbPool.quietRollback(conn);
//        } finally {
//            DbPool.quietClose(conn);
//        }
//        return result;
//    }

    public static String getFeaturedCommunities(String ulogin, int offset,
            int pagesize) {
        String result = KWLErrorMsgs.rsFalse;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = DashboardHandler.getFeaturedCommunities(conn, ulogin,
                    offset, pagesize);

        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String setStatusProject(String userids, String projid,
            int urelationid, int actionid, String loginid, String companyid) {
        String result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = Forum.setStatusProject(conn, userids, projid,
                    urelationid, actionid, loginid, companyid);
            conn.commit();

        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String setStatusProject(String[] userids, String projid,
            int urelationid, int actionid, String loginid, String companyid, int flag) {
        String result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            for (int i = 0; i < userids.length; i++) {
                result = Forum.setStatusProject(conn, userids[i], projid,
                        urelationid, actionid, loginid, companyid);
                if (!result.equalsIgnoreCase(KWLErrorMsgs.rsTrue)) {
                    DbPool.quietRollback(conn);
                } else {
                    if (flag != 0) {
                        //           LogHandler.InsertLogForAdmin(conn,loginid,projid,flag,userids[i],1);
                    }
                }
//                else{
//                    if(flag!=0){
//                        //LogHandler.InsertLogForAdmin(conn,loginid,projid,flag,userids[i],1);
//                    }
//                }
            }
            conn.commit();

        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String setPlanPermission(HttpServletRequest request, String[] userids, String projid,
            int permission, String loginid, String companyid) {
        String result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            String ipAddress = AuthHandler.getIPAddress(request);
            int auditMode = 0;
            String userName = AuthHandler.getUserName(request);
            for (int i = 0; i < userids.length; i++) {
                result = Forum.setPlanPermission(conn, userids[i], projid,
                        permission, loginid, companyid);
                if (!result.equalsIgnoreCase(KWLErrorMsgs.rsSuccessTrue)) {
                    DbPool.quietRollback(conn);
                } else {
                    String params = AuthHandler.getAuthor(conn, loginid) + " (" + userName + "), "
                            + AuthHandler.getAuthor(conn, userids[i]) + ", " + projdb.getProjectName(conn, projid);
                    AuditTrail.insertLog(conn, "157", loginid, userids[i], projid,
                            companyid, params, ipAddress, auditMode);
                }
            }
            conn.commit();
        } catch (SessionExpiredException ex) {
            DbPool.quietRollback(conn);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static boolean chkResourceDependency(String[] userids, String comid) {
        boolean res = false;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            res = Forum.chkResourceDependency(conn, userids, comid);
        } catch (Exception ex) {
            res = false;
        } finally {
            DbPool.quietClose(conn);
        }
        return res;
    }

    public static boolean chkModerator(String[] userids, String comid) {
        boolean res = false;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            res = Forum.chkModerator(conn, userids, comid);
        } catch (Exception ex) {
            res = false;
        } finally {
            DbPool.quietClose(conn);
        }
        return res;
    }

    public static String getProjectList(String ulogin, int pagesize, int offset, String ss) {
        String result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = DashboardHandler.getProjectList(conn, ulogin, pagesize,
                    offset, ss);

        } catch (ServiceException ex) {
            result = "{\"data\": {},\"count\":[0]}";
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String getProjectNameList(String ulogin) {
        String result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = DashboardHandler.getProjectNameList(conn, ulogin);

        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String getArchiveProjList(String ulogin,String companyid, int pagesize, int offset, String ss) {
        String result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = DashboardHandler.getArchivedProjList(conn,companyid,ulogin, pagesize,
                    offset, ss);

        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String getProjectListForChart(String ulogin) {
        String result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
//			result = DashboardHandler.getProjectListForChart(conn, ulogin);

        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String getTasksByProjectPriority(String ulogin) {
        String result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = DashboardHandler.getTasksByProjectPriority(conn, ulogin);

        } catch (ServiceException ex) {
            Logger.getLogger(dbcon.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(dbcon.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String getactionlog(String ulogin, String upagesize,
            String upageno) {
        String result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = DashboardHandler.getactionlog(conn, ulogin, upagesize,
                    upageno);

        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String getdatewiseactionlog(String ulogin, String currDate, Integer interval) {
        String result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = DashboardHandler.getdatewiseactionlog(conn, ulogin, currDate, interval);

        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static int getTotalActionlogDatediff(String ulogin, String currDate) {
        int result = 0;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = DashboardHandler.getTotalActionlogDatediff(conn, ulogin, currDate);

        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static com.krawler.utils.json.base.JSONObject AuthUser(String uname,
            String passwd, String subdomain) throws ServiceException {
        com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject();
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            jobj = AuthHandler.verifyLogin(conn, uname, passwd, subdomain);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn("dbcon.AuthUser():");
            throw ServiceException.FAILURE("", ex);
        } finally {
            DbPool.quietClose(conn);
        }
        return jobj;
    }

    public static String getTodoTask(String uid, int groupType, String loginid) {
        String ret = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            ret = todolist.getToDoTask(conn, uid, groupType, loginid);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            ret = "{error:" + ex.toString() + "}";
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }

    public static String addToDoInProjectplan(HttpServletRequest req) {
        String ret = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            ret = todolist.addToDoInProjectplan(conn,req);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            ret = "{error:" + ex.toString() + "}";
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }

    public static String updateToDotask(String taskname, int taskorder,
            int status, String parentId, String taskid, String assignedto, String dueDate, String desc,
            String priority, String userid) {
        String result = "";
        Connection conn = null;


        try {

            conn = DbPool.getConnection();
            result = todolist.updateToDoTask(conn, taskname, taskorder, status,
                    parentId, taskid, assignedto, dueDate, desc, priority, userid);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } catch (JSONException exp) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String updateToDoTask_Appendchild(String taskname, String task_order,
            String tstatus, String parentId, String taskid, String dueDate, String priority, String userid) {
        String result = "";
        Connection conn = null;


        try {

            conn = DbPool.getConnection();
            result = todolist.updateToDoTask_Appendchild(conn, taskname, task_order, tstatus,
                    parentId, taskid, dueDate, priority, userid);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } catch (JSONException exp) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String updateToDoTask_changestatus(int status, String taskid) {
        String result = "";
        Connection conn = null;


        try {

            conn = DbPool.getConnection();
            result = todolist.updateToDoTask_changestatus(conn, status, taskid);

            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String sendToDoNotification(HttpServletRequest request) throws SessionExpiredException {

        Connection conn = null;
        String type = "";
        try {
            conn = DbPool.getConnection();
            type = todolist.sendToDoNotification(conn, request);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } catch (SessionExpiredException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return type;
    }

    public static String insertToDotask(String localid, String taskname,
            int taskorder, int status, String parentId, String taskid,
            String userid, int grouptype, String due, boolean leafflag,
            String priority, String loginid, String desc) {
        String result = "";
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = todolist.insertToDoTask(conn, taskname, taskorder,
                    status, parentId, taskid, userid, grouptype, due,
                    leafflag, localid, priority, loginid, desc);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } catch (JSONException exp) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String deleteToDotask(String taskid, String leafFlags, String userName, String loginid, String companyid, String ipAddress, int auditMode) {
        Connection conn = null;
        String result = "";
        try {
            conn = DbPool.getConnection();
            result = todolist.deleteToDoTask(conn, taskid, leafFlags, userName, loginid, companyid, ipAddress, auditMode);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } catch (JSONException exp) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String getToDoDetails(String taskid) {
        String ret = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            ret = todolist.getToDoDetails(conn, taskid);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            ret = "{error:" + ex.toString() + "}";
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }

    public static Object fetchEvent(String[] cid, String viewdt1, String viewdt2, String loginid) throws ServiceException {
        Object result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = calEvent.fetchEvent(conn, cid, viewdt1, viewdt2, loginid);

        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn("dbcon.fetchEvent():");
            throw ServiceException.FAILURE("", ex);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

//	public static String insertEvent(String cid, String startts, String endts,
//			String subject, String descr, String location, String showas,
//			String priority, String recpattern, String recend, String resources, String userid, String companyid) throws ServiceException {
//		String result = "";
//		Connection conn = null;
//		try {
//			conn = DbPool.getConnection();
//			result = calEvent.insertEvent(conn, cid, startts, endts, subject,
//					descr, location, showas, priority, recpattern, recend,
//					resources, userid, companyid);
//			conn.commit();
//		} catch (ServiceException ex) {
//			DbPool.quietRollback(conn);
//                    KrawlerLog.op.warn("dbcon.insertEvent():");
//                    throw ServiceException.FAILURE("",ex);
//		} finally {
//			DbPool.quietClose(conn);
//		}
//		return result;
//	}
    public static int updateEvent(String cid, String startts, String endts,
            String subject, String descr, String location, String showas,
            String priority, String recpattern, String recend,
            String resources, String eid, String userid, String companyid) throws ServiceException {
        int result = 0;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = calEvent.updateEvent(conn, cid, startts, endts, subject,
                    descr, location, showas, priority, recpattern, recend,
                    resources, eid, userid, companyid);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn("dbcon.updateEvent():");
            throw ServiceException.FAILURE("", ex);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static int deleteEvent(String eid) {
        int result = 0;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = calEvent.deleteEvent(conn, eid);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static int deleteAgendaEvent(String[] eid) {
        int result = 0;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = calEvent.deleteAgendaEvent(conn, eid);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String insertGuest(String eid, String userid, String status) throws ServiceException {
        String result = "";
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = calEvent.insertGuest(conn, eid, userid, status);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn("dbcon.insertGuest():");
            throw ServiceException.FAILURE("", ex);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static int deleteGuest(String eid) throws ServiceException {
        int result = 0;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = calEvent.deleteGuest(conn, eid);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn("dbcon.deleteGuest():");
            throw ServiceException.FAILURE("", ex);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static Object selectGuest(String eid) {
        Object result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = calEvent.selectGuest(conn, eid);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String insertReminder(String eid, String rtype, int rtime) throws ServiceException {
        String result = "";
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = calEvent.insertReminder(conn, eid, rtype, rtime);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn("dbcon.insertReminder():");
            throw ServiceException.FAILURE("", ex);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static int deleteReminder(String eid) throws ServiceException {
        int result = 0;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = calEvent.deleteReminder(conn, eid);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn("dbcon.deleteReminder():");
            throw ServiceException.FAILURE("", ex);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static Object selectReminder(String eid) {
        Object result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = calEvent.selectReminder(conn, eid);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static Object[] fetchAgendaEvent(String[] cidList, String viewdt1,
            String viewdt2, int limit, int offset, String loginid) {
        Object[] result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = calEvent.fetchAgendaEvent(conn, cidList, viewdt1, viewdt2,
                    limit, offset, loginid);

        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String getCalendarList(String uid, String userId, String latestts) {
        String result = null;
        Connection conn = null;
        try {

            conn = DbPool.getConnection();
            result = Tree.getCalendarlist(conn, uid, userId, latestts);

        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            result = "{error:" + ex.toString() + "}";
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String getSharedCalendarList(String uid) {
        String result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = Tree.getSharedCalendarlist(conn, uid);

        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            result = "{error:" + ex.toString() + "}";
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String getProjectName(String projid) {
        String result = "";
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = projdb.getProjectName(conn, projid);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String insertNewCalendar(String cname, String desc,
            String location, String timezone, String colorcode, int caltype,
            int isdefault, String userid) {
        String result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = Tree.insertNewCalendar(conn, cname, desc, location,
                    timezone, colorcode, caltype, isdefault, userid);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            result = KWLErrorMsgs.rsFalseErrMsg + ex.toString()
                    + "\"";
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String updateCalList(String cid, String cname, String desc,
            String location, String timezone, String colorcode, int caltype,
            int isdefault, String userid) {
        String result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = Tree.updateCalendar(conn, cid, cname, desc, location,
                    timezone, colorcode, caltype, isdefault, userid);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            result = KWLErrorMsgs.rsFalseErrMsg + ex.toString()
                    + "\"";
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static int deleteCalendar(String cid, int isdefault) {
        int result = 0;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = Tree.deleteCalendar(conn, cid, isdefault);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;

    }

    public static String getCalendar(String cid) {
        String result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = Tree.getCalendar(conn, cid);
            //conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            result = "{error:" + ex.toString() + "}";
        } finally {
            DbPool.quietClose(conn);
        }
        return result;

    }

    public static String insertCalPermission(String cid, String userid,
            int permissionlevel) {
        String result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = Tree.insertCalPermission(conn, cid, userid,
                    permissionlevel);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String getCalendarPermission(String cid) {
        String result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = Tree.getCalendarPermission(conn, cid);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String getCalendarSharedUserIds(String cid) {
        String result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = Tree.getCalendarSharedUserIds(conn, cid);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static int deleteCalendarPermission(String cid) {
        int result = 0;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = Tree.deleteCalendarPermission(conn, cid);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static Object getResponseEvent(String eid, String userid) {
        Object result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = calEvent.getResponseEvent(conn, eid, userid);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static int updateResponse(String eid, String userid, String setStatus) {
        int result = 0;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = calEvent.updateResponse(conn, eid, userid, setStatus);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static Object getDefaultCalendar(String userid, int isdefault) {
        Object result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = calEvent.getDefaultCalendar(conn, userid, isdefault);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static Object getEventDetails(String eid) {
        Object result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = calEvent.getEventDetails(conn, eid);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String getMyMessage(String ulogin, String currentts) {
        String result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = MessengerHandler.getMyMessage(conn, ulogin, currentts);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String getImagePath(String eId, String eType, int size) {
        String imgPath = "";
        DbResults rs = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            if (eType.equals("0")) {
                rs = DbUtil.executeQuery(conn,
                        "Select image from users where userid=?", eId);
            } else if (eType.equals("1")) {
                rs = DbUtil.executeQuery(conn,
                        "Select image from community where communityid=?", eId);
            } else if (eType.equals("2")) {
                rs = DbUtil.executeQuery(conn,
                        "Select image from project where projectid=?", eId);
            }
            if (rs.next()) {
                imgPath = rs.getString("image");
                if (eType.equals("0")) {
                    imgPath = StringUtil.getAppsImagePath(eId, size);
                }
            }
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return imgPath;
    }

    public static String updateProfile(HttpServletRequest request) {
        String result = "";
        DbPool.Connection conn = null;
        try {
            conn = DbPool.getConnection();
            String jdata = ProfileHandler.updateProfile(conn, request);
            if (!StringUtil.isNullOrEmpty(jdata)) {
                result = KWLErrorMsgs.rsTrueData + jdata + "\"}";
            } else {
                result = KWLErrorMsgs.rsTrueNoData;
            }
            conn.commit();
        } catch (ServiceException ex) {
            result = KWLErrorMsgs.rsFalseFailure;
            DbPool.quietRollback(conn);
        } catch (SessionExpiredException e) {
            result = KWLErrorMsgs.rsFalseFailure;
            DbPool.quietRollback(conn);
        } catch (Exception exp) {
            result = KWLErrorMsgs.rsFalseFailure;
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String changePassword(String platformURL, HttpServletRequest request) {
        String result = "";
        DbPool.Connection conn = null;
        JSONObject jobj = new JSONObject();
        try {
            jobj.put("valid", true);
            jobj.put("data", "{\"data\": \"{\"flag\": 0}\"}");
            conn = DbPool.getConnection();
            String jdata = ProfileHandler.changePassword(conn, request, platformURL);
            if (!StringUtil.isNullOrEmpty(jdata)) {
                jobj = new JSONObject();
                jobj.put("valid", true);
                jobj.put("data", jdata);
                result = jobj.toString();
            } else {
                result = jobj.toString();
            }
            conn.commit();
        } catch (JSONException ex) {
            result = jobj.toString();
            DbPool.quietRollback(conn);

        } catch (ServiceException ex) {
            result = jobj.toString();
            DbPool.quietRollback(conn);

        } catch (SessionExpiredException e) {
            result = jobj.toString();
            DbPool.quietRollback(conn);

        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String getPersonalInfo(HttpServletRequest request, String platformURL) {
        String result = null;
        DbPool.Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = ProfileHandler.getPersonalInfo(conn, request, platformURL);
            conn.commit();
        } catch (ServiceException ex) {
            result = KWLErrorMsgs.rsDataFailure;
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

//	public static String getPermissions(DbPool.Connection conn,String userid) {
////		Connection conn = null;
//		String result = "{}";
//		try {
////			conn = DbPool.getConnection();
//			result = DashboardHandler.getPermissions(conn, userid);
////			conn.commit();
//		} catch (ServiceException e) {
////			DbPool.quietRollback(conn);
////		} finally {
////			DbPool.quietClose(conn);
//		}
//		return result;
//	}
    public static String getTags(String profileId) {
        Connection conn = null;
        String responseText = null;
        try {
            conn = DbPool.getConnection();
            responseText = ProfileHandler.getTags(conn, profileId);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return responseText;
    }

    public static String setTags(String profileId, String tagStr) {
        Connection conn = null;
        String responseText = null;
        try {
            conn = DbPool.getConnection();
            ProfileHandler.setTags(conn, profileId, tagStr);
            conn.commit();
            responseText = ProfileHandler.getTags(conn, profileId);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return responseText;
    }

    public static void InsertAuditLogForTodo(String todoId, String userName, String actionId,
            String loginid, String companyId, String ipAddress, int mode) {

        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            String todoDetails = todolist.getToDoDetails(conn, todoId);
            JSONObject jtodo = new JSONObject(todoDetails);
            JSONObject jData = new JSONObject();
            jData = jtodo.getJSONArray("data").getJSONObject(0);

            String todoName = jData.getString("taskname");
            String projectId = jData.getString("userid");

            String params = AuthHandler.getAuthor(conn, loginid) + " (" + userName + "), "
                    + todoName + ", " + projdb.getProjectName(conn, projectId);
            AuditTrail.insertLog(conn, actionId, loginid, todoId, projectId,
                    companyId, params, ipAddress, mode);
            conn.commit();

        } catch (JSONException ex) {
            Logger.getLogger(dbcon.class.getName()).log(Level.SEVERE, null, ex);
            DbPool.quietRollback(conn);

        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);

        } finally {
            DbPool.quietClose(conn);
        }
    }

    public static void InsertAuditLogForTemplate(String tempId, String userName, String actionId,
            String loginid, String companyId, String ipAddress, int mode) {

        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            String tempDetails = projdb.getTemplatesDetail(conn, tempId, loginid);
            JSONObject jtemp = new JSONObject(tempDetails);
            JSONObject jData = new JSONObject();
            jData = jtemp.getJSONArray("data").getJSONObject(0);

            String tempName = jData.getString("tempname");
            String projectId = jData.getString("projid");

            String params = AuthHandler.getAuthor(conn, loginid) + " (" + userName + "), "
                    + tempName + ", " + projdb.getProjectName(conn, projectId);
            AuditTrail.insertLog(conn, actionId, loginid, tempId, projectId,
                    companyId, params, ipAddress, mode);
            conn.commit();

        } catch (JSONException ex) {
            Logger.getLogger(dbcon.class.getName()).log(Level.SEVERE, null, ex);
            DbPool.quietRollback(conn);

        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);

        } finally {
            DbPool.quietClose(conn);
        }
    }

    public static void InsertAuditLogForDocument(String docId, String userName, String actionId,
            String loginid, String companyId, String ipAddress, int mode, String projectid) {

        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            String docDetails = FileHandler.getDocDetails(conn, docId, loginid);
            JSONObject jtodo = new JSONObject(docDetails);
            JSONObject jData = new JSONObject();
            jData = jtodo.getJSONArray("data").getJSONObject(0);

            String docName = jData.getString("docname");
            String params = AuthHandler.getAuthor(conn, loginid) + " (" + userName + "), "
                    + docName;
            AuditTrail.insertLog(conn, actionId, loginid, docId, projectid,
                    companyId, params, ipAddress, mode);
            conn.commit();

        } catch (JSONException ex) {
            Logger.getLogger(dbcon.class.getName()).log(Level.SEVERE, null, ex);
            DbPool.quietRollback(conn);

        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);

        } finally {
            DbPool.quietClose(conn);
        }
    }

    public static void InsertAuditLog(String actionId, String actionBy,
            String actionOn, String projectId, String companyId, String params,
            String ipAddress, String userName, int mode) {

        Connection conn = null;
        try {
            conn = DbPool.getConnection();

            String nameParams = AuthHandler.getAuthor(conn, actionBy);
            String projName = projdb.getProjectName(conn, projectId);

            params = nameParams + " (" + userName + "), " + params + ", " + projName;
            AuditTrail.insertLog(conn, actionId, actionBy, actionOn, projectId,
                    companyId, params, ipAddress, mode);
            conn.commit();

        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);

        } finally {
            DbPool.quietClose(conn);
        }
    }

    public static String getDocName(String docid) {
        Connection conn = null;
        String docname = null;
        try {
            conn = DbPool.getConnection();
            docname = FileHandler.getDocName(conn, docid);

        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return docname;

    }

//    public static String getDocpermission(String docid) {
//        Connection conn = null;
//        String docper = null;
//        try {
//            conn = DbPool.getConnection();
//            docper = LogHandler.getDocpermission(conn, docid);
//        } catch (ServiceException ex) {
//            DbPool.quietRollback(conn);
//        } finally {
//            DbPool.quietClose(conn);
//        }
//        return docper;
//    }
//
//    public static ArrayList getSharedusers(String userid) {
//        Connection conn = null;
//        ArrayList docper = null;
//        try {
//            conn = DbPool.getConnection();
//            docper = LogHandler.getSharedusers(conn, userid);
//        } catch (ServiceException ex) {
//            DbPool.quietRollback(conn);
//        } finally {
//            DbPool.quietClose(conn);
//        }
//        return docper;
//    }

    public static String getUserRequest(String userid) throws ServiceException {
        String result = null;
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = ProfileHandler.getUserRequest(conn, userid);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn("dbcon.getUserRequest():");
            throw ServiceException.FAILURE("", ex);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String getRequestAuthor(String[] userid) throws ServiceException {
        Connection conn = null;
        String result = null;
        try {
            conn = DbPool.getConnection();
            result = FileHandler.getRequestAuthor(conn, userid);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn("dbcon.getRequestAuthor():");
            throw ServiceException.FAILURE("", ex);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

//    public static void InsertLogForAdmin(String loginId, String toId, int actionId, String pcid, int flg) {
//        Connection conn = null;
//        try {
//            conn = DbPool.getConnection();
////                    LogHandler.InsertLogForAdmin(conn, loginId, toId, actionId, pcid, flg);
//            conn.commit();
//        } catch (ServiceException ex) {
//            DbPool.quietRollback(conn);
//        } finally {
//            DbPool.quietClose(conn);
//        }
//    }

    public static String getAdminPemission(String userid, int featureid, String activityname) throws ServiceException {
        Connection conn = null;
        String result = null;
        try {
            conn = DbPool.getConnection();
            result = DashboardHandler.getAdminPemission(conn, userid, featureid, activityname);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn("dbcon.getAdminPemission():");
            throw ServiceException.FAILURE("", ex);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String getUserEvents(String userId, String startVal, String endVal) {
        Connection conn = null;
        String result = null;
        try {
            conn = DbPool.getConnection();
            result = DashboardHandler.getUserEvents(conn, userId, startVal, endVal);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String getComProjRequest(int flag, int status, String userid) throws ServiceException {
        Connection conn = null;
        String result = null;
        try {
            conn = DbPool.getConnection();
            result = DashboardHandler.getComProjRequest(conn, flag, status, userid);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn("dbcon.getComProjRequest():");
            throw ServiceException.FAILURE("", ex);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String getCommProjInvite(String userid, int flag) throws ServiceException {
        Connection conn = null;
        String result = null;
        try {
            conn = DbPool.getConnection();
            result = DashboardHandler.getCommProjInvite(conn, userid, flag);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn("dbcon.getCommProjInvite():");
            throw ServiceException.FAILURE("", ex);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String confirmSignup(String id, HttpServletRequest request) throws ServiceException {
        Connection conn = null;
        String bSuccess = "";
        try {
            conn = DbPool.getConnection();
            bSuccess = SignupHandler.confirmSignup(conn, id, request);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return bSuccess;
    }

    public static boolean setPersonalPrefs(String userid, String timezone, String dateFormat) throws ServiceException {
        Connection conn = null;
        boolean result = false;
        try {
            conn = DbPool.getConnection();
            result = ProfileHandler.setPersonalPrefs(conn, userid, timezone, dateFormat);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static void chkProjDateOnImport(String projectid) throws ServiceException {
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            projdb.chkProjDateOnImport(conn, projectid);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn("dbcon.chkProjDateOnImport():");
            throw ServiceException.FAILURE("", ex);
        } finally {
            DbPool.quietClose(conn);
        }
    }

    public static String getCmpCurrFromProj(String projid) throws ServiceException {
        Connection conn = null;
        String result = "{}";
        try {
            conn = DbPool.getConnection();
            result = ProfileHandler.getCmpCurrFromProj(conn, projid);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            KrawlerLog.op.warn("dbcon.getCmpCurrFromProj():");
            throw ServiceException.FAILURE("", ex);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String getCompanyid(String domainName) {
        String result = "";
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            result = CompanyHandler.getCompanyIDBySubdomain(conn, domainName);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return result;
    }

    public static String getCompanySubdomainByCompanyID(String companyID) {
        String subdomain = "";
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            subdomain = CompanyHandler.getCompanySubdomainByCompanyID(conn, companyID);
        } catch (ServiceException e) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return subdomain;
    }

    public static String getQuickLinks(String userid, String companyid) {
        String strDashboardData = null;
        Connection conn = null;
        JSONArray jobj = new JSONArray();
        JSONObject j = new JSONObject();
        try {
            conn = DbPool.getConnection();
            StringBuilder sbTemp = new StringBuilder();
            StringBuilder sb = new StringBuilder();
            sb.append("<ul id=\"quicklinksUL\">");
            sbTemp = DashboardHandler.getQuickLinks(conn, userid);
            if (sbTemp.length() > 0) {
                sb.append(sbTemp);
            }
            sb.append("</ul>");
            strDashboardData = sb.toString();
            j.put("update", strDashboardData);
            jobj.put(j);
        } catch (JSONException ex) {
            DbPool.quietRollback(conn);
        } catch (ServiceException e) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return jobj.toString();
    }

    public static String getWelcomeText(String userid, String companyid) {
        StringBuilder sb = new StringBuilder();
        Connection conn = null;
        JSONArray jA = new JSONArray();
        try {
            conn = DbPool.getConnection();
            boolean da = DashboardHandler.isDeskeraAdmin(conn, userid);
            CompanyDAO cd = new CompanyDAOImpl();
            Locale locale = cd.getCompanyLocale(conn, companyid);
            PermissionManager pm = new PermissionManager();
            if (!da) {
                sb.append("<div class=\'welcomeTxt\'><div class=\'wtElem\'>").append(MessageSourceProxy.getMessage("pm.welcometext", null, locale)).append("</div>");
                sb.append("<div class=\'wtElem\'>").append(MessageSourceProxy.getMessage("pm.dashboard.widget.welcome.subheader", null, locale)).append("</div><br/>");
                sb.append("<div class=\'centered\'><img alt=\'start by creating a project \' src=\'../../images/welcome-widget.gif\' width=\'289\' height=\'179\'/></div><br/><h2>");
                if (pm.isPermission(conn, userid, PermissionConstants.Feature.PROJECT_ADMINISTRATION, PermissionConstants.Activity.PROJECT_CREATE)) {
                    sb.append("<a href='#' onclick=\"getCreateProjectWindow()\">").append(MessageSourceProxy.getMessage("pm.dashboard.widget.welcome.addprojects", null, locale)).append("</a>");
                } else {
                    sb.append(MessageSourceProxy.getMessage("pm.dashboard.widget.welcome.addprojects", null, locale));
                }
                sb.append("</h2>");
                sb.append("<div class=\'wtElem\'>").append(MessageSourceProxy.getMessage("pm.dashboard.widget.welcome.addprojects.text1", null, locale)).append("</div>");
                sb.append("<div class=\'wtElem\'>").append(MessageSourceProxy.getMessage("pm.dashboard.widget.welcome.addprojects.text2", null, locale)).append("</div><br/>");
                sb.append("<h2>");
                getDashboardLink(pm.isPermission(conn, userid, PermissionConstants.Feature.USER_ADMINISTRATION), MessageSourceProxy.getMessage("pm.dashboard.widget.welcome.network", null, locale), 1, sb);
                sb.append("</h2>");
                sb.append("<div class=\'wtElem\'>").append(MessageSourceProxy.getMessage("pm.dashboard.widget.welcome.network.text1", null, locale)).append("</div>");
                sb.append("<div class=\'wtElem\'>").append(MessageSourceProxy.getMessage("pm.dashboard.widget.welcome.network.text2", null, locale)).append("</div><br/>");
                sb.append("<h2>");
                getContactsLink(MessageSourceProxy.getMessage("pm.dashboard.widget.welcome.collaborate", null, locale), sb);
                sb.append("</h2>");
                sb.append("<div class=\'wtElem\'>").append(MessageSourceProxy.getMessage("pm.dashboard.widget.welcome.collaborate.text1", null, locale)).append("</div>");
                sb.append("<div class=\'wtElem\'>").append(MessageSourceProxy.getMessage("pm.dashboard.widget.welcome.collaborate.text2", null, locale)).append("</div>");
                sb.append("<div class=\'wtElem\'>").append(MessageSourceProxy.getMessage("pm.dashboard.widget.welcome.collaborate.text3", null, locale)).append("</div></div>");
            } else {
                StringBuilder sbTasks = new StringBuilder();
                sbTasks = DashboardHandler.getSubscriptionRequests(conn, userid);
                sb.append("<div class=\"statuspanelouter\"><div class=\"statuspanelinner\">");
                if (sbTasks.length() > 0) {
                    sb.append(sbTasks);
                } else {
                    sb.append("No new subscription requests");
                }
                sb.append("</div></div>");
            }
            JSONObject jo = new JSONObject();
            jo.put("update", sb.toString());
            jA.put(jo);
        } catch (JSONException ex) {
            DbPool.quietRollback(conn);
        } catch (ServiceException e) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
            return jA.toString();
        }
    }

    public static String setProjStatus(HttpServletRequest request) {
        String res = "";
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            res = projdb.setProjStatus(conn, request);
            conn.commit();
        } catch (ServiceException ex) {
            Logger.getLogger(dbcon.class.getName()).log(Level.SEVERE, null, ex);
            DbPool.quietRollback(conn);
            res = "{\"valid\": false}";
        } finally {
            DbPool.quietClose(conn);
        }
        return res;
    }

    private static void getDashboardLink(boolean isSuperUser, String linkText, int linkId, StringBuilder sb) {
        if (isSuperUser) {
            sb.append("<a href=\'#\' onclick=\'loadAdminPage(" + linkId + ");\'>" + linkText + "</a>");
        } else {
            sb.append(linkText);
        }
    }

    private static void getContactsLink(String linkText, StringBuilder sb) {
        sb.append("<a href=\'#\' onclick=\'navigate(\"con\");\'>" + linkText + "</a>");
    }

    public static String getUserid(String userName, String companyid) {
        Connection conn = null;
        String userid = "";
        try {
            conn = DbPool.getConnection();
            // folderString=Mail.getFolderidForMailuser(conn,userid,foldername);
            userid = ProfileHandler.getUserid(conn, userName, companyid);

        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return userid;
    }

    public static String resourceConflict(HttpServletRequest request) {
        Connection conn = null;
        String responseText = null;
        try {
            conn = DbPool.getConnection();
            responseText = projdb.resourceConflict(conn, request);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return responseText;
    }

    public static String getTaskConflict(HttpServletRequest request) throws SessionExpiredException {
        Connection conn = null;
        String responseText = null;
        try {
            conn = DbPool.getConnection();
            responseText = projdb.getTaskConflict(conn, request);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return responseText;
    }

    public static String availableResourcesInProject(HttpServletRequest request) {
        Connection conn = null;
        String responseText = null;
        try {
            conn = DbPool.getConnection();
            responseText = projdb.availableResourcesInProject(conn, request);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return responseText;
    }

    public static String getHelpContent(String module) throws JSONException {
        Connection conn = null;
        String responseText = null;
        try {
            conn = DbPool.getConnection();
            responseText = DashboardHandler.getHelpContent(conn, module);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return responseText;
    }

    public static Date getUserToGmtTimezoneDate(String userid, String date) throws ServiceException {
        java.util.Date d = new java.util.Date();
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            d = Timezone.getUserToGmtTimezoneDate(conn, userid, date);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
            return d;
        }
    }

    public static String getUserToGmtTimezone(String userid, String date) throws ServiceException {
        String d = "";
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            d = Timezone.getUserToGmtTimezone(conn, userid, date);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
            return d;
        }
    }

    public static String fromCompanyToSystem(String companyid, String date) throws ServiceException {
        String d = "";
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            d = Timezone.fromCompanyToSystem(conn, date, companyid);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
            return d;
        }
    }

    public static String toUserTimezone(String date, String userid) throws ServiceException {
        String op = "";
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            op = Timezone.toUserTimezone(conn, date, userid);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
            return op;
        }
    }

    public static String toCompanyTimezone(String date, String companyid) throws ServiceException {
        String op = "";
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            op = Timezone.toCompanyTimezone(conn, date, companyid);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
            return op;
        }
    }

    public static String getProjectListMember(String userid) {
        Connection conn = null;
        String responseText = null;
        try {
            conn = DbPool.getConnection();
            responseText = DashboardHandler.getProjectsOfMember(conn, userid);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return responseText;
    }

    public static String insertDefaultWidgetState(HttpServletRequest request) {
        Connection conn = null;
        String ret = "";
        try {
            conn = DbPool.getConnection();
            ret = WidgetStateHandler.getWidgetStates(conn, request);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }

    public static String removeWidgetFromState(HttpServletRequest request) {
        Connection conn = null;
        String responseText = null;
        try {
            conn = DbPool.getConnection();
            responseText = WidgetStateHandler.removeWidgetFromState(conn, request);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return responseText;
    }

    public static String insertWidgetIntoState(HttpServletRequest request) {
        Connection conn = null;
        String responseText = null;
        try {
            conn = DbPool.getConnection();
            responseText = WidgetStateHandler.insertWidgetIntoState(conn, request);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return responseText;
    }

    public static String changeWidgetStateOnDrop(HttpServletRequest request) {
        Connection conn = null;
        String responseText = null;
        try {
            conn = DbPool.getConnection();
            responseText = WidgetStateHandler.changeWidgetStateOnDrop(conn, request);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return responseText;
    }

    public static String getrequest(String userid, String companyid) {
        Connection conn = null;
        JSONArray jobj = new JSONArray();
        JSONObject j = new JSONObject();
        try {
            conn = DbPool.getConnection();
            jobj = DashboardHandler.getNewRequests(conn, userid);
            j.put("data", jobj);
            conn.commit();
        } catch (ServiceException e) {
            DbPool.quietRollback(conn);
        } catch (JSONException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return j.toString();
    }

    public static String getannouncement(String userid, String companyid) {
        Connection conn = null;
        JSONArray jobj = new JSONArray();
        JSONObject j = new JSONObject();
        try {
            conn = DbPool.getConnection();
            jobj = DashboardHandler.getAnnouncements(conn, userid, 20, 0, companyid);
            j.put("data", jobj);
            conn.commit();
        } catch (ServiceException e) {
            DbPool.quietRollback(conn);
        } catch (JSONException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
            return j.toString();
        }
    }

    public static String getChartForWidget(String userid, String companyid, String projCount, String limit, String offset) {
        String strDashboardData = null;
        JSONArray jobj = new JSONArray();
        JSONObject j = new JSONObject();
        JSONObject j1 = new JSONObject();
        try {
            limit = (Integer.parseInt(limit) < 5) ? String.valueOf(5) : limit;
            int o = Integer.parseInt(offset);
            int newOffset = (o != 0) ? (o * 5) : 0;
            strDashboardData = "<div id='projChart1' style='display:block;background-color:#ffffff;padding:1%;float:left;width:98%;min-height:100px;height:90%;'>";
            strDashboardData += "<script type='text/javascript'>createNewChart('scripts/bar chart/krwcolumn.swf', 'krwcolumn', '100%', '320', '8', '#FFFFFF','scripts/bar chart/progresschart_settings.xml','jspfiles/profile/dashboard/chartData.jsp?uid=" + userid + "&limit=" + limit + "&start=" + String.valueOf(newOffset) + "&type=progress', 'projChart1');";
            strDashboardData += "</script></div><div id='projChart2' style='display:block;background-color:#ffffff;padding:1%;float:left;width:98%;min-height:100px;height:90%;'>";
            strDashboardData += "<script type='text/javascript'>createNewChart('scripts/bar chart/krwcolumn.swf', 'krwcolumn', '100%', '350', '8', '#FFFFFF','scripts/bar chart/timechart_settings.xml','jspfiles/profile/dashboard/chartData.jsp?uid=" + userid + "&limit=" + limit + "&start=" + String.valueOf(newOffset) + "&type=time', 'projChart2');";
            strDashboardData += "</script></div>";
            int pc = Integer.parseInt(projCount);
            int c = (pc != 5) ? (pc / 5) + 1 : 1;
            j1.put("count", String.valueOf(c));
            j.put("update", strDashboardData);
            jobj.put(j);
            j1.put("data", jobj);
        } catch (JSONException ex) {
            j1 = new JSONObject();
        } finally {
            return j1.toString();
        }
    }

    public static String getMyDocsWidget(String userid, String companyid, String limit1, String offset) {
        Connection conn = null;
        String responseText = null;
        try {
            conn = DbPool.getConnection();
            responseText = DashboardHandler.getMyDocsWidget(conn, userid, companyid, limit1, offset);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
            return responseText;
        }
    }
    /**
     * 
     * @param userid
     * @param companyid
     * @param limit
     * @param offset
     * @return JSON Object String that contains JSON Array of projectid, projectname, health, number of completed tasks, inprogress tasks, needattention tasks, overdue tasks, future tasks accept milestones.
     */
    public static String getProjectsByTimeProgress(String userid, String companyid, String limit, String offset) {
        Connection conn = null;
        String responseText = null;
        try {
            conn = DbPool.getConnection();
            responseText = DashboardHandler.getProjectsByTimeProgress(conn, userid, companyid, limit, offset);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
            return responseText;
        }
    }

    public static String getReportWidgetLinks(String userid, String companyid, String projectid, String limit1, String offset) {
        Connection conn = null;
        String responseText = null;
        try {
            conn = DbPool.getConnection();
            responseText = DashboardHandler.getReportWidgetLinks(conn, userid, companyid, projectid, limit1, offset);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
            return responseText;
        }
    }

    public static String gettaskupdates(String userid, String companyid, String projectid, String limit, String offset) {
        Connection conn = null;
        String x = "";
        try {
            conn = DbPool.getConnection();
            x = DashboardHandler.getTasksListForWidget(conn, userid, companyid, projectid, limit, offset);
            conn.commit();
        } catch (ServiceException e) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
            return x;
        }
    }

    public static String getWidgetUpdates(String userid, String companyid, String projectid, int mode, String limit, String offset) throws ServiceException {
        String returnStr = "";
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            returnStr = DashboardHandler.getWidgetUpdates(conn, userid, companyid, projectid, mode, limit, offset);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return returnStr;
    }

    public static String getPMUpdates(String userid, String projectid, String limit, String offset) throws ServiceException {
        String returnStr = "";
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            returnStr = DashboardHandler.getPMUpdates(conn, userid, projectid, limit, offset);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return returnStr;
    }

    public static String getProjectLevelUpdates(String projectid, String loginid, String companyid, int l, int o) throws ServiceException {
        String returnStr = "";
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            returnStr = projdb.getProjectLevelUpdates(conn, projectid, loginid, companyid, l, o);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return returnStr;
    }

    public static String dontShowHelpMsg(String userid) throws ServiceException {
        String returnStr = "";
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            returnStr = DashboardHandler.dontShowHelpMsg(conn, userid);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return returnStr;
    }

    public static String getAllWidgetsData(String responseText, String userid, String companyid, String projCount) {
        Connection conn = null;
        String ret = "";
        try {
            conn = DbPool.getConnection();
            ret = DashboardHandler.getAllWidgetsData(conn, responseText, userid, companyid, projCount);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }

    public static String updateQuickLinksStates(String userid, String state) {
        Connection conn = null;
        String ret = "";
        try {
            conn = DbPool.getConnection();
            ret = DashboardHandler.updateQuickLinksStates(conn, userid, state);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }

    public static String getCompanyIDFromProject(String projectid) {
        Connection conn = null;
        String ret = "";
        try {
            conn = DbPool.getConnection();
            ret = CompanyHandler.getCompanyIDFromProject(conn, projectid);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }

    public static String getMileStonesForChart(String projList, String userid, String companyid) {
        Connection conn = null;
        String ret = "";
        try {
            conn = DbPool.getConnection();
            ret = projdb.getMileStonesForChart(conn, projList, userid, companyid);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }

    public static String updateMilestoneTimelineSelection(String userid, String state) {
        Connection conn = null;
        String ret = "";
        try {
            conn = DbPool.getConnection();
            ret = DashboardHandler.updateMilestoneTimelineSelection(conn, userid, state);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }

    public static String getMaintainanceDetails(String companyid) {
        com.krawler.database.DbPool.Connection conn = null;
        String ret = "";
        try {
            conn = DbPool.getConnection();
            ret = DashboardHandler.getMaintainanceDetails(conn,companyid);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }

    public static String getCustomizedTopLinks(String companyID) {
        Connection conn = null;
        String ret = "";
        try {
            conn = DbPool.getConnection();
            ret = DashboardHandler.getCustomizedTopLinks(conn, companyID);
        } catch (JSONException ex) {
            DbPool.quietRollback(conn);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }

    public static int updateCustomWidgetFlag(String userID, boolean isCustomWidget) {
        Connection conn = null;
        int ret = -1;
        try {
            conn = DbPool.getConnection();
            ret = WidgetStateHandler.updateCustomWidgetFlag(conn, userID, isCustomWidget);
            conn.commit();
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }

    public static boolean isConversionComplete(String docID) {
        Connection conn = null;
        boolean ret = false;
        try {
            conn = DbPool.getConnection();
            ret = FileHandler.isConversionComplete(conn, docID);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return ret;
    }
}
