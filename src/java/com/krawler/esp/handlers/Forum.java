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

import com.krawler.common.customcolumn.CCManager;
import com.krawler.common.customcolumn.ColumnSet;
import com.krawler.common.customcolumn.CustomColumn;
import com.krawler.common.locale.MessageSourceProxy;
import com.krawler.utils.json.base.JSONObject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import com.krawler.common.service.ServiceException;
import com.krawler.common.timezone.Timezone;
import com.krawler.database.DbPool;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.database.DbPool.Connection;
import com.krawler.utils.json.KWLJsonConverter;
import com.krawler.utils.json.base.JSONException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.krawler.common.util.KWLErrorMsgs;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.company.CompanyDAO;
import com.krawler.esp.company.CompanyDAOImpl;
import com.krawler.esp.database.dbcon;
import com.krawler.esp.portalmsg.Mail;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletContext;

public class Forum {

    public static String getProjectMembersfordashboard(Connection conn, String ulogin,
            int pagesize, int offset) throws ServiceException {
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        String splitstring2 = null;
        try {
            pstmt = conn.prepareStatement("select count(*) as count from (select users.userid ,users.fname from users "
                    + "inner join projectmembers on users.userid = projectmembers.userid "
                    + "inner join userlogin on users.userid = userlogin.userid where userlogin.isactive = true and projectid = ? and inuseflag = 1 and status in (3,4,5)) as temp;");

            pstmt.setString(1, ulogin);
            rs = pstmt.executeQuery();
            rs.next();
            int count1 = rs.getInt("count");
            KWLJsonConverter KWL = new KWLJsonConverter();

            pstmt = conn.prepareStatement("select users.userid as id,concat(users.fname,' ',users.lname) as name,image as img from users "
                    + "inner join projectmembers on users.userid = projectmembers.userid "
                    + "inner join userlogin on users.userid = userlogin.userid where userlogin.isactive = true and projectid = ? and status IN (3,4,5) and inuseflag = 1 ORDER BY id LIMIT ? OFFSET ?;");

            pstmt.setString(1, ulogin);
            pstmt.setInt(2, pagesize);
            pstmt.setInt(3, offset);

            rs = pstmt.executeQuery();

            splitstring2 = KWL.GetJsonForGrid(rs);
            splitstring2 = splitstring2.substring(0, splitstring2.length() - 1);
            splitstring2 += ",\"count\":[" + count1 + "]}";

            splitstring2 = ProfileHandler.getAppsImageInJSON(splitstring2, "id", "img", 100).toString();
            rs.close();
        } catch (JSONException e) {
            throw ServiceException.FAILURE("Forum.getProjectMembers", e);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("Forum.getProjectMembers", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return " " + splitstring2;
    }

    public static String getProjectMembers(Connection conn, String ulogin,
            int pagesize, int offset) throws ServiceException {
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        String splitstring2 = null;
        try {
            pstmt = conn.prepareStatement("select count(*) as count from (select users.userid ,userlogin.username from users "
                    + "inner join projectmembers on users.userid = projectmembers.userid "
                    + "inner join userlogin on users.userid = userlogin.userid where userlogin.isactive = true and projectid = ? and inuseflag = 1 and status in (3,4,5)) as temp;");

            pstmt.setString(1, ulogin);
            rs = pstmt.executeQuery();
            rs.next();
            int count1 = rs.getInt("count");
            KWLJsonConverter KWL = new KWLJsonConverter();

            pstmt = conn.prepareStatement("select users.userid as id,userlogin.username as name,image as img from users "
                    + "inner join projectmembers on users.userid = projectmembers.userid "
                    + "inner join userlogin on users.userid = userlogin.userid where userlogin.isactive = true and projectid = ? and status IN (3,4,5) and inuseflag = 1 ORDER BY id LIMIT ? OFFSET ?;");

            pstmt.setString(1, ulogin);
            pstmt.setInt(2, pagesize);
            pstmt.setInt(3, offset);

            rs = pstmt.executeQuery();

            splitstring2 = KWL.GetJsonForGrid(rs);
            splitstring2 = splitstring2.substring(0, splitstring2.length() - 1);
            splitstring2 += ",\"count\":[" + count1 + "]}";

            rs.close();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("Forum.getProjectMembers", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return " " + splitstring2;
    }

    public static String getProjectMembership(Connection conn, String userid,
            String communityId) throws ServiceException {
        ResultSet rs1 = null;
        String splitstring = null;
        PreparedStatement pstmt1 = conn.prepareStatement("SELECT archived,projectmembers.userid AS id1, projectmembers.status AS connstatus, "
                + "planpermission, notification_subscription as notifSubVal "
                + "FROM projectmembers INNER JOIN project ON project.projectid = projectmembers.projectid "
                + "WHERE userid = ? AND project.projectid = ?");
        try {
            pstmt1.setString(1, userid);
            pstmt1.setString(2, communityId);
            rs1 = pstmt1.executeQuery();
            KWLJsonConverter KWL = new KWLJsonConverter();
            splitstring = KWL.GetJsonForGrid(rs1).toString();
            rs1.close();

        } catch (SQLException e) {
            throw ServiceException.FAILURE("Forum.getProjectMembership", e);
        } finally {
            DbPool.closeStatement(pstmt1);
        }
        return " " + splitstring;
    }

    public static boolean isInUse(Connection conn, String userid) throws ServiceException {
        ResultSet rs;
        boolean str = false;
        PreparedStatement pstmt = conn.prepareStatement("select isactive from userlogin where userid = ?");
        try {
            pstmt.setString(1, userid);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                str = rs.getBoolean("isactive");
            }
            rs.close();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("Forum.isInUse:" + e.getMessage(), e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return str;
    }

    public static String getProjectMemberStatus(Connection conn, String userid,
            String projid) throws ServiceException {
        ResultSet rs = null;
        String str = "";
        PreparedStatement pstmt = conn.prepareStatement("select status from projectmembers where userid = ? and projectid = ? and inuseflag = 1");
        try {
            pstmt.setString(1, userid);
            pstmt.setString(2, projid);
            rs = pstmt.executeQuery();
            KWLJsonConverter KWL = new KWLJsonConverter();
            str = KWL.GetJsonForGrid(rs);
            rs.close();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("Forum.getProjectMemberStatus", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return str;
    }

    public static String getRelatedProjects(Connection conn, String ulogin,
            int offset, int pagesize) throws ServiceException {
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        String splitstring2 = null;
        try {
            pstmt = conn.prepareStatement("select count(*) as count from "
                    + "(select project.projectid, projectname from project "
                    + "inner join projectmembers on projectmembers.projectid = project.projectid "
                    + "inner join userrelations on userrelations.userid1=projectmembers.userid "
                    + "inner join userlogin on userlogin.userid = projectmembers.userid "
                    + "where userlogin.isactive = true and userrelations.relationid = 3 and userrelations.userid2 = ? "
                    + "union "
                    + "select project.projectid, projectname from project "
                    + "inner join projectmembers on projectmembers.projectid = project.projectid "
                    + "inner join userrelations on userrelations.userid1=projectmembers.userid "
                    + "inner join userlogin on userlogin.userid = projectmembers.userid"
                    + "where userlogin.isactive = true and userrelations.relationid = 3 and userrelations.userid1 = ? and projectmembers.userid = ? ) as temp;");
            pstmt.setString(1, ulogin);
            pstmt.setString(2, ulogin);
            pstmt.setString(3, ulogin);
            rs = pstmt.executeQuery();
            rs.next();
            int count1 = rs.getInt("count");
            KWLJsonConverter KWL = new KWLJsonConverter();
            pstmt = conn.prepareStatement("select project.projectid as id, projectname as name,image as img "
                    + "from project inner join projectmembers on projectmembers.projectid = project.projectid "
                    + "inner join userrelations on userrelations.userid1=projectmembers.userid "
                    + "inner join userlogin on userlogin.userid = projectmembers.userid "
                    + "where userlogin.isactive = true and userrelations.relationid = 3 and userrelations.userid2 = ? "
                    + "union "
                    + "select project.projectid as id, projectname as name,image as img from project "
                    + "inner join projectmembers on projectmembers.projectid = project.projectid "
                    + "inner join userrelations on userrelations.userid1=projectmembers.userid "
                    + "inner join userlogin on userlogin.userid = projectmembers.userid "
                    + "where userlogin.isactive = true and userrelations.relationid = 3 and userrelations.userid1 = ? and projectmembers.userid = ? ORDER BY id LIMIT ? OFFSET ?;");
            pstmt.setString(1, ulogin);
            pstmt.setString(2, ulogin);
            pstmt.setString(3, ulogin);
            pstmt.setInt(4, pagesize);
            pstmt.setInt(5, offset);
            rs = pstmt.executeQuery();
            splitstring2 = KWL.GetJsonForGrid(rs);
            splitstring2 = splitstring2.substring(0, splitstring2.length() - 1);
            splitstring2 += ",\"count\":[" + count1 + "]}";
            rs.close();

        } catch (SQLException e) {
            throw ServiceException.FAILURE("Forum.getRelatedProjects", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return " " + splitstring2;
    }

    public static String getProjectDetails(Connection conn, String projectId, String loginid)
            throws ServiceException {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JSONObject jobj = new JSONObject();
        PreparedStatement pstmt1 = conn.prepareStatement("select companyid,project.projectid, projectname, description as about, createdon, startdate, nickname from project where project.projectid = ?");
        try {
            pstmt1.setString(1, projectId);
            ResultSet rs1 = pstmt1.executeQuery();
            if (rs1.next()) {
                    JSONObject jtemp = new JSONObject();
                    String companyId = rs1.getString("companyid");
                    CompanyDAO cd = new CompanyDAOImpl();
                    Locale locale = cd.getCompanyLocale(conn, companyId);
                    jtemp.put("projectid", rs1.getString("projectid"));
                    jtemp.put("projectname", rs1.getString("projectname"));
                    jtemp.put("about", rs1.getString("about"));
                    jtemp.put("nickname", rs1.getString("nickname"));
                    if (rs1.getObject("startdate") != null) {
                        String sdtStr = Timezone.toCompanyTimezone(conn, rs1.getObject("startdate").toString(), companyId);
                        java.util.Date dt = sdf.parse(sdtStr);
                        sdtStr = sdf.format(dt);
                        jtemp.put("startdate", sdtStr);
                    } else {
                        String[] dates = projdb.getminmaxProjectDate(conn, projectId, loginid).split(",");
                        java.util.Date dt = sdf1.parse(dates[0]);
                        String sdtStr = sdf1.format(dt);
                        pstmt1 = conn.prepareStatement("update project set startdate = ? where projectid = ?");
                        pstmt1.setString(1, dates[0]);
                        pstmt1.setString(2, projectId);
                        int c = pstmt1.executeUpdate();
                        if (c == 1) {
                            dt = sdf.parse(dates[0]);
                            sdtStr = sdf.format(dt);
                            jtemp.put("startdate", sdtStr);
                        }
                    }
                    String ctdStr = Timezone.toCompanyTimezone(conn, rs1.getObject("createdon").toString(), companyId);
                    if (ctdStr.compareTo("") != 0) {
                        java.util.Date dt = sdf.parse(ctdStr);
                        ctdStr = sdf.format(dt);
                        jtemp.put("createdon", ctdStr);
                    }
                    CustomColumn cc = CCManager.getCustomColumn(companyId);
                    ColumnSet cs = cc.getColumnsData(conn, "Project", projectId);
                    while(cs.next()){
                        if(cs.getObject()!=null){
                            if(cs.getColumn().getType() == CustomColumn.DATE){
                                long l = Long.parseLong(cs.getObject().toString());
                                java.util.Date d = new java.util.Date(l);
                                ctdStr = sdf.format(d);
                                jtemp.put(cs.getDataIndex(), ctdStr);
                            } else if(cs.getColumn().getType() == CustomColumn.BOOLEAN){
                                jtemp.put(cs.getDataIndex(), ((Boolean)cs.getObject())?MessageSourceProxy.getMessage("lang.yes.text", null, locale) 
                                        :MessageSourceProxy.getMessage("lang.yes.text", null, locale));
                            } else {
                                jtemp.put(cs.getDataIndex(), cs.getObject());
                            }
                        }
                    }
                    jobj.append("data", jtemp);
            }
            rs1.close();
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("Forum.getProjectDetails", ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("Forum.getProjectDetails", ex);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("Forum.getProjectDetails", e);
        } finally {
            DbPool.closeStatement(pstmt1);
        }
        return jobj.toString();
    }

    /* // not invoked
    public static void deleteFronProjTask(String value) throws ServiceException {
    String query = "delete from proj_task where taskid=?";
    com.krawler.database.DbPool.Connection conn = DbPool.getConnection();
    Object[] params = { null };
    params[0] = value;
    try {
    DbUtil.executeUpdate(conn, query, value);
    conn.commit();
    } catch (ServiceException e) {
    
    } finally {
    DbPool.quietClose(conn);
    }
    
    }
     */
    /*
    public static String getResources() throws ServiceException, SQLException {
    String query = "select resourcename,colorcode from proj_resources";
    com.krawler.database.DbPool.Connection conn = DbPool.getConnection();
    ResultSet rs = null;
    KWLJsonConverter KWL = new KWLJsonConverter();
    try {
    PreparedStatement pstmt = conn.prepareStatement(query);
    rs = pstmt.executeQuery();
    
    } catch (ServiceException e) {
    
    } finally {
    DbPool.quietClose(conn);
    }
    return KWL.GetJsonForGrid(rs);
    
    }
     */
    public static boolean chkResourceDependency(Connection conn, String[] userids, String projid) {
        boolean result = false;
        PreparedStatement pstmt = null;
        try {
            for (int i = 0; i < userids.length; i++) {
                pstmt = conn.prepareStatement("SELECT COUNT(*) AS count FROM proj_taskresourcemapping WHERE resourceid = ? AND taskid IN"
                        + " (SELECT taskid FROM proj_task WHERE projectid = ?)");
                pstmt.setString(1, userids[i]);
                pstmt.setString(2, projid);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next() && rs.getInt("count") != 0) {
                    return true;
                }
            }
        } catch (Exception ex) {
            result = true;
        }
        return result;
    }

    public static boolean chkModerator(Connection conn, String[] userids, String projid) {
        boolean result = false;
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("SELECT count(userid) AS count FROM projectmembers WHERE status =4 AND projectid = ?");
            pstmt.setString(1, projid);
            ResultSet rs = pstmt.executeQuery();
            int modCnt = 0;
            if (rs.next()) {
                modCnt = rs.getInt("count");
            }
            String uids = "";
            for (int cnt = 0; cnt < userids.length; cnt++) {
                uids += "'" + userids[cnt] + "',";
            }
            uids = uids.substring(0, (uids.length() - 1));
            pstmt = conn.prepareStatement("SELECT count(userid) AS count FROM projectmembers WHERE status =4 AND projectid = ? AND userid IN (" + uids + ")");
            pstmt.setString(1, projid);
            rs = pstmt.executeQuery();
            int usrCnt = 0;
            if (rs.next()) {
                usrCnt = rs.getInt("count");
            }
            if (modCnt == usrCnt) {
                result = true;
            }
            rs.close();
        } catch (Exception ex) {
            result = true;
        }
        return result;
    }

    public static int getStatusProject(Connection conn, String userid, String projectid) throws ServiceException {
        int status = 0;
        try {
            PreparedStatement p = conn.prepareStatement("SELECT status FROM projectmembers WHERE projectid = ? AND userid = ?");
            p.setString(1, projectid);
            p.setString(2, userid);
            ResultSet r = p.executeQuery();
            if (r.next()) {
                status = r.getInt("status");
            }
            r.close();
        } catch (ServiceException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } catch (SQLException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return status;
    }

    public static String setStatusProject(Connection conn, String userid, String projid,
            int urelationid, int flg, String lid, String companyid) throws ServiceException {
        String query = null;
        PreparedStatement pstmt = null;
        DbResults rs = null;
        ResultSet r = null;
        String msgString = "";
        String subjectString = "";
        String userName = "";
        String projName = "";
        String resVal = KWLErrorMsgs.rsTrue;
        int status = 0;
        String luserName = "";
        String[] uids = {userid};
        String insertMsg = "";
        String params = "";
        String actid = "";
        String userFullName = "";
        String lname = "";
        try {
            if (!lid.equals("")) {
                pstmt = conn.prepareStatement("SELECT username FROM userlogin WHERE userid=? and isactive = true");
                pstmt.setString(1, lid);
                r = pstmt.executeQuery();
                r.next();
                luserName = r.getString("username");
                r.close();
                pstmt.close();
                userFullName = AuthHandler.getAuthor(conn, lid);
                lname = AuthHandler.getUserName(conn, lid);
            }
            pstmt = conn.prepareStatement("SELECT username FROM userlogin WHERE userid=? and isactive = true");
            pstmt.setString(1, userid);
            r = pstmt.executeQuery();
            r.next();
            userName = r.getString("username");
            r.close();
            pstmt.close();
            pstmt = conn.prepareStatement("SELECT projectname FROM project WHERE projectid=?");
            pstmt.setString(1, projid);
            r = pstmt.executeQuery();
            r.next();
            projName = r.getString("projectname");
            r.close();
            pstmt.close();
            pstmt = conn.prepareStatement("SELECT status FROM projectmembers inner join userlogin on userlogin.userid = projectmembers.userid WHERE userlogin.isactive = true and projectid=? AND userlogin.userid=?");
            pstmt.setString(1, projid);
            pstmt.setString(2, userid);
            r = pstmt.executeQuery();
            String mailFooter = KWLErrorMsgs.mailSystemFooter;
            String userFName = AuthHandler.getAuthor(conn, userid);
            CompanyDAO cd = new CompanyDAOImpl();
            Locale locale = cd.getCompanyLocale(conn, companyid);
            //String mailFooter = "<-----------------This is a system generated message----------------->";
            if (r.next()) {
                status = r.getInt("status");
                if (urelationid == 0) {
                    if (status == 4 && isLastModerator(conn, userid, projid)) {
                        resVal = "{\"error\":\"Can not perform this operation.\"}";
                    } else {
                        query = "UPDATE projectmembers SET inuseflag = 0, status = 0 WHERE userid = ? AND projectid = ?";
                        String query2 = "UPDATE  proj_resources SET inuseflag = 0 WHERE  resourceid  = ? AND projid = ?";
                        if (!chkResourceDependency(conn, uids, projid)) {
                            query = "DELETE FROM projectmembers WHERE userid = ? AND projectid = ?";
                            query2 = "DELETE FROM proj_resources WHERE resourceid = ? AND projid = ?";
                        }
                        DbUtil.executeUpdate(conn, query, new Object[]{userid, projid});
                        DbUtil.executeUpdate(conn, query2, new Object[]{userid, projid});
                        if (status == 3 || status == 4) {
                            if (flg == 2) {
                                params = userFullName + " (" + lname + "), " + projName;
                                actid = "158";
                                subjectString = "[" + projName + "] " + userName + " has left the project.";
                                msgString = userName + " has left the project: " + projName + ". <br>You can no longer assign any tasks to " + userName + "." + mailFooter;
                            } else {
                                try {
                                    String msgDropString = "Your access to the project: " + projName + " has been deactivated." + mailFooter;
                                    String subjectDropString = "[" + projName + "] Dropped from the project.";
                                    insertMsg = Mail.insertMailMsg(conn, userName, lid, subjectDropString, msgDropString, "1", false, "1", "", "newmsg", "", 3, "", companyid);

                                    String msgToModString = "The access account " + userName + " has been deactivated for the project: " + projName + ". <br>You can no longer assign any tasks to " + userName + "." + mailFooter;
                                    String subjectToModString = "[" + projName + "] " + userName + " dropped from the project.";
                                    String GET_MODERATORS_ID = "select projectmembers.userid as modId, userlogin.username as modName from projectmembers inner join userlogin on userlogin.userid = projectmembers.userid WHERE userlogin.isactive = true and status = 4 and projectid = ?";
                                    DbResults rsId = DbUtil.executeQuery(conn, GET_MODERATORS_ID, new Object[]{projid});
                                    while (rsId.next()) {
                                        insertMsg = Mail.insertMailMsg(conn, rsId.getString("modName"), lid, subjectToModString, msgToModString, "1", false, "1", "", "newmsg", "", 3, "", companyid);
                                    }
                                    params = userFName + " (" + userName + "), " + projName;
                                    actid = "159";
                                } catch (ParseException je) {
                                    throw ServiceException.FAILURE(je.getMessage(), je);
                                } catch (JSONException je) {
                                    throw ServiceException.FAILURE(je.getMessage(), je);
                                }
                            }
                        } else if (status == 2) {
                            params = userFName + " (" + userName + "),"+ projName;
                            actid = "154";
                            subjectString = "[" + projName + "] " + userName + " has rejected the request to join";
                            msgString = userName + " rejected the request to join " + projName + "." + mailFooter;
                        } else if (status == 1) {
                            params = userFName + " (" + userName + "),"+ projName;
                            actid = "154";
                            DbUtil.executeUpdate(conn, "UPDATE projectmembers SET status = 0 WHERE projectid=? AND userid = ?", new Object[]{projid, userid});
                            subjectString = "[" + projName + "] Request to join has been rejected.";
                            msgString = "Your  Request to join " + projName + " has been rejected." + mailFooter;
                        }
                    }
                } else if (urelationid == 21) {
                    query = "UPDATE projectmembers SET inuseflag = 1, status=3 WHERE userid = ? AND projectid = ?";
                    DbUtil.executeUpdate(conn, query, new Object[]{userid, projid});
                    insertIntoResources(conn, userid, projid, userName);
                    String msgActiveString = "Your access to the project : " + projName + " has been activated." + mailFooter;
                    String subjectActive = "[" + projName + "] Access to the project activated.";
                    try {
                        query = "select username as uname from userlogin where userid = ?";
                        DbResults rsNameActive = DbUtil.executeQuery(conn, query, new Object[]{userid});
                        insertMsg = Mail.insertMailMsg(conn, rsNameActive.getString("uname"), lid, subjectActive, msgActiveString, "1", false, "1", "", "newmsg", "", 3, "", companyid);
                    } catch (ParseException je) {
                    } catch (JSONException je) {
                    }
                } else if (urelationid == 1) {
                    try {
                        DbUtil.executeUpdate(conn, "UPDATE projectmembers SET status = 1 WHERE projectid=? AND userid = ?", new Object[]{projid, userid});
                        String GET_MODERATORS_ID = "select projectmembers.userid as modId, userlogin.username as modName from projectmembers inner join userlogin on userlogin.userid = projectmembers.userid WHERE userlogin.isactive = true and status = 4 and projectid = ?";
                        DbResults rsId = DbUtil.executeQuery(conn, GET_MODERATORS_ID, new Object[]{projid});
                        while (rsId.next()) {
                            String myMsgProjReq = userName + " has requested permission to join the project : " + projName + ". <br>You can approve or reject this request in the Project\'s Settings tab." + mailFooter;
                            String mySubjectProjReq = "[" + projName + "] " + userName + " has requested to join the project.";
                            insertMsg = Mail.insertMailMsg(conn, rsId.getString("modName"), userid, mySubjectProjReq, myMsgProjReq, "1", false, "1", "", "newmsg", "", 3, "", companyid);
                        }
                    } catch (ParseException je) {
                        throw ServiceException.FAILURE(je.getMessage(), je);
                    } catch (JSONException je) {
                        throw ServiceException.FAILURE(je.getMessage(), je);
                    }
                } else {
                    if (flg == 1) {
                        query = "select count(projectmembers.userid) as count from projectmembers inner join userlogin on userlogin.userid = projectmembers.userid WHERE userlogin.isactive = true and status = 4 and projectid = ?";
                        rs = DbUtil.executeQuery(conn, query, new Object[]{projid});
                        rs.next();
                        if (rs.getInt("count") == 1) {
                            //return "{\"data\":[{\"result\":\"true\",\"value\":\"The project should have atleast one moderator\"}]}";
                            return "{\"data\":[{\"result\":\"true\",\"value\":\""+MessageSourceProxy.getMessage("pm.msg.35", null, locale) +"\"}]}";
                        }
                    }
                    query = "update projectmembers set inuseflag = 1,status = ? where userid = ? and projectid = ? ";
                    DbUtil.executeUpdate(conn, query, new Object[]{urelationid, userid, projid});
                    if (urelationid >= 3) {
                        insertIntoResources(conn, userid, projid, userName);
                    }
                    try {
                        if (urelationid == 4) {
                            params = userFName + " (" + userName + "), " + projName;
                            actid = "1550";
                            String myMsgString = "You have been assigned role of moderator for the project : " + projName + ". <br>You can now manage members, resources and moderate project discussions." + mailFooter;
                            String mySubjectString = "[" + projName + "] You have been set as the moderator.";

                            insertMsg = Mail.insertMailMsg(conn, userName, lid, mySubjectString, myMsgString, "1", false, "1", "", "newmsg", "", 3, "", companyid);

                            String GET_MODERATORS_ID = "select projectmembers.userid as modId, userlogin.username as modName from projectmembers inner join userlogin on userlogin.userid = projectmembers.userid WHERE userlogin.isactive = true and status = 4 and projectid = ?";
                            DbResults rsId = DbUtil.executeQuery(conn, GET_MODERATORS_ID, new Object[]{projid});

                            while (rsId.next()) {
                                myMsgString = userName + " has been assigned role of moderator for the project : " + projName + ". <br>" + userName + " can now manage members, resources and moderate project discussions." + mailFooter;
                                mySubjectString = "[" + projName + "] " + userName + " has been set as the moderator.";

                                if (userName.compareTo(rsId.getString("modName")) != 0) {
                                    insertMsg = Mail.insertMailMsg(conn, rsId.getString("modName"), lid, mySubjectString, myMsgString, "1", false, "1", "", "newmsg", "", 3, "", companyid);
                                }
                            }
                        } else if (urelationid == 3 && status >= 3) {
                            params = userFName + " (" + userName + "), " + projName;
                            actid = "1551";
                            String myMsgString = "Your access as the moderator of the project " + projName + " has been disabled." + mailFooter;
                            String mySubjectString = "[" + projName + "] Your access as the moderator has been disabled";

                            insertMsg = Mail.insertMailMsg(conn, userName, lid, mySubjectString, myMsgString, "1", false, "1", "", "newmsg", "", 3, "", companyid);

                            String GET_MODERATORS_ID = "select projectmembers.userid as modId, userlogin.username as modName from projectmembers inner join userlogin on userlogin.userid = projectmembers.userid WHERE userlogin.isactive = true and status = 4 and projectid = ?";
                            DbResults rsId = DbUtil.executeQuery(conn, GET_MODERATORS_ID, new Object[]{projid});

                            while (rsId.next()) {
                                myMsgString = "The access account for " + userName + " as the moderator of the project " + projName + " has been disabled." + mailFooter;
                                mySubjectString = "[" + projName + "] " + userName + "'s access as the moderator has been disabled";

                                insertMsg = Mail.insertMailMsg(conn, rsId.getString("modName"), lid, mySubjectString, myMsgString, "1", false, "1", "", "newmsg", "", 3, "", companyid);
                            }
                        }
                    } catch (ParseException je) {
                        throw ServiceException.FAILURE(je.getMessage(), je);
                    } catch (JSONException je) {
                        throw ServiceException.FAILURE(je.getMessage(), je);
                    }

                    if (status == 2 && urelationid != 1) {
                        params = userFName + " (" + userName + "),"+ projName;
                        actid = "153";
                        subjectString = "[" + projName + "] " + userName + " has joined.";
                        msgString = userName + " has joined " + projName + "." + mailFooter;
                    } else if (status == 1 && urelationid != 1) {
                        params = userFName + " (" + userName + "),"+ projName;
                        actid = "153";
                        subjectString = "[" + projName + "] Request to join has been approved.";
                        msgString = " Your  Request to join " + projName + " has been accepted by " + luserName + "." + mailFooter;
                    }
                }
            } else {
                if (isInUse(conn, userid)) {
                    query = "INSERT INTO projectmembers (projectid, userid, status) VALUES (?,?,?);";
                    DbUtil.executeUpdate(conn, query, new Object[]{projid, userid, urelationid});
                    if (!userFullName.equals("")) {
                        params = userFullName + " (" + lname + "), " + userFName + " (" + userName + "), " + projName;
                        actid = "152";
                    }
                    if (urelationid == 1) {
                        String GET_MODERATORS = "select projectmembers.userid as modId, userlogin.username as modName from projectmembers inner join userlogin on userlogin.userid = projectmembers.userid WHERE userlogin.isactive = true and status = 4 and projectid = ?";
                        DbResults rsId = DbUtil.executeQuery(conn, GET_MODERATORS, new Object[]{projid});
                        try {
                            while (rsId.next()) {
                                String reqMsgString = userName + " has requested permission to join the project : " + projName + ". <br>You can approve or reject this request in the Project\'s Settings tab." + mailFooter;
                                String reqSubjectString = "[" + projName + "] " + userName + " has requested to join the project.";

                                insertMsg = Mail.insertMailMsg(conn, rsId.getString("modName"), lid, reqSubjectString, reqMsgString, "1", false, "1", "", "newmsg", "", 3, "", companyid);
                                params = "";
                            }
                        } catch (ParseException je) {
                            throw ServiceException.FAILURE(je.getMessage(), je);
                        } catch (JSONException je) {
                            throw ServiceException.FAILURE(je.getMessage(), je);
                        }
                    }
                    if (urelationid >= 3) {
                        insertIntoResources(conn, userid, projid, userName);
                    }
                }
            }
            if (!msgString.equals("")) {
                try {
                    if (status != 1) {
                        query = "SELECT username FROM userlogin inner join projectmembers on projectmembers.userid = userlogin.userid WHERE projectid  = ? and status = 4";
                        DbResults r1 = DbUtil.executeQuery(conn, query, new Object[]{projid});
                        while (r1.next()) {
                            insertMsg = Mail.insertMailMsg(conn, r1.getString("username"), userid, subjectString, msgString, "1", false, "1", "", "newmsg", "", 3, "", companyid);
                        }
                    } else {
                        insertMsg = Mail.insertMailMsg(conn, userName, lid, subjectString, msgString, "1", false, "1", "", "newmsg", "", 3, "", companyid);
                    }
                } catch (ParseException je) {
                    throw ServiceException.FAILURE(je.getMessage(), je);
                } catch (JSONException je) {
                    throw ServiceException.FAILURE(je.getMessage(), je);
                }
            }
            if (params.compareTo("") != 0) {
                AuditTrail.insertLog(conn, actid, lid, userid, projid, companyid, params, "", 0);
            }
        } catch (SQLException ex) {
            throw ServiceException.FAILURE("Forum.setProjectCommunity", ex);
        }
        return resVal;
        //return "{\"data\":[{\"result\":\"true\"}]}";
    }

    public static boolean isLastModerator(Connection conn, String userid, String pid) {
        boolean res = true;
        try {
            PreparedStatement pstmt = conn.prepareStatement("SELECT count(userid) AS count FROM projectmembers "
                    + "WHERE inuseflag = 1 AND status = 4 AND projectid = ? AND userid <> ?");
            pstmt.setString(1, pid);
            pstmt.setString(2, userid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt("count") > 0) {
                res = false;
            }
            rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(Forum.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(Forum.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }

    public static void insertIntoResources(Connection conn, String userid, String projid, String resName) {
        try {
            DbResults dbr = DbUtil.executeQuery(conn, "SELECT resourceid, resourcename, typeid FROM proj_resources WHERE resourceid=? AND projid =?", new Object[]{userid, projid});
            Object[] parmas = null;
            String query = "";
            if (dbr.next()) {
                if (dbr.getInt("typeid") == 1) {
                    if (isInUse(conn, userid)) {
                        query = "UPDATE proj_resources SET inuseflag = 1 WHERE  resourceid  = ? AND projid = ?";
                        parmas = new Object[]{userid, projid};
                    }
                } else {
                    query = "UPDATE proj_resources SET inuseflag = 1 WHERE  resourceid  = ? AND projid = ?";
                    parmas = new Object[]{userid, projid};
                }
            } else {
                query = "INSERT INTO proj_resources (resourceid, resourcename, projid) VALUES (?,?,?)";
                parmas = new Object[]{userid, resName, projid};
            }
            DbUtil.executeUpdate(conn, query, parmas);
        } catch (Exception e) {
            String res = e.getMessage();
        }
    }
//Removed original method and devided into two separate methods.

    public static String setPlanPermission(Connection conn, String userid, String projid, int permission, String lid, String companyid)
            throws ServiceException {
        String resVal = KWLErrorMsgs.rsSuccessTrue;
        try {
            String mailFooter = KWLErrorMsgs.mailSystemFooter;
            int oldPermVal = getPlanPermission(conn, userid, projid);
            String query = "update projectmembers set planpermission = ? where userid = ? and projectid = ? and status!=?";
            int c = DbUtil.executeUpdate(conn, query, new Object[]{permission, userid, projid, 4});
            if (c > 0) {
                try {
                    String userName = AuthHandler.getUserName(conn, userid);
                    String projName = projdb.getProjectName(conn, projid);
                    String oldperm = getPermissionsName(oldPermVal);
                    String newperm = getPermissionsName(permission);
                    String msgDropString = "Moderator has changed [" + projName + "] project plan permission from " + oldperm + " to " + newperm + "." + mailFooter;
                    String subjectDropString = "[" + projName + "] Changed Project Plan Permission";
                    Mail.insertMailMsg(conn, userName, lid, subjectDropString, msgDropString, "1", false, "1", "", "newmsg", "", 3, "", companyid);
                } catch (ParseException je) {
                } catch (JSONException je) {
                }
            } else {
                resVal = KWLErrorMsgs.rsSuccessFalse;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Forum.class.getName()).log(Level.SEVERE, null, ex);
        }
        return resVal;
    }

    public static int getPlanPermission(Connection conn, String userid, String projid) throws ServiceException, SQLException {
        DbResults r = null;
        r = DbUtil.executeQuery(conn, "SELECT projectmembers.planpermission from projectmembers "
                + " WHERE userid = ? AND projectmembers.projectid = ?", new Object[]{userid, projid});
        if (r.next()) {
            return r.getInt("planpermission");
        }
        return 0;
    }

    public static String getPermissionsName(int permval) {
        String oldperm = "";
        switch (permval) {
            case 2:
                oldperm = "'View All Tasks'";
                break;
            case 10:
                oldperm = "'View All Tasks'";
                break;
            case 4:
                oldperm = "'Modify All Tasks'";
                break;
            case 16:
                oldperm = "'Modify Assigned Tasks - % Complete Only'";
                break;
            case 8:
                oldperm = "'View Assigned Tasks'";
                break;
            case 18:
                oldperm = "'View All - Modify Assigned'";
                break;
        }
        return oldperm;
    }

    /**
     * This method is used to publish user depending on module.
     * @author Kamlesh Kumar
     */
    public static void publishUserActivities(String module, String[] users, String publishBy, String projid, String msg,String params, boolean success, ServletContext context) {
        try {
            String userFullName = FileHandler.getAuthor(publishBy);
            JSONObject jobj = new JSONObject();
            for (int i = 0; i < users.length; i++) {
                if (!publishBy.equals(users[i])) {
                    JSONObject json = new JSONObject();
                    jobj.put("userid", users[i]);
//                    jobj.put("permission", true);
                    json.append("data", jobj);
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("userid", users[i]);
                    data.put("username", userFullName);
                    data.put("success", String.valueOf(success));
                    if (!StringUtil.isNullOrEmpty(projid)) {
                        data.put("projname", dbcon.getProjectName(projid));
                    }
                    data.put("data", json.toString());
                    data.put("msg", msg);
                    data.put("module", module);
                    data.put("params", params);
                    ServerEventManager.publish("/useractivities/" + users[i], data, context);
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(Forum.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(Forum.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//end of method
}
