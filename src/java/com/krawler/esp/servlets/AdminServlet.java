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
package com.krawler.esp.servlets;

import com.krawler.common.customcolumn.CCManager;
import com.krawler.common.customcolumn.CcUtil;
import com.krawler.common.customcolumn.ColumnSet;
import com.krawler.common.customcolumn.CustomColumn;
import com.krawler.common.locale.MessageSourceProxy;
import com.krawler.common.permission.Feature;
import com.krawler.common.permission.PermissionConstants;
import com.krawler.common.permission.PermissionManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.KWLErrorMsgs;
import com.krawler.common.util.StringUtil;
import com.krawler.database.DbPool;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.database.DbPool.Connection;
import com.krawler.esp.handlers.AuthHandler;
import com.krawler.esp.handlers.DashboardHandler;
import com.krawler.esp.handlers.Forum;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.esp.handlers.SessionHandler;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.esp.handlers.genericFileUpload;
import com.krawler.svnwebclient.configuration.ConfigurationException;
import com.krawler.utils.json.KWLJsonConverter;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.krawler.common.timezone.Timezone;
import com.krawler.common.util.Constants;
import com.krawler.common.util.SchedulingUtilities;
import com.krawler.common.util.Utilities;
import com.krawler.esp.docs.docsconversion.DocsConversionHandler;
import com.krawler.esp.docs.docsconversion.OpenOfficeServiceResolver;
import com.krawler.esp.handlers.AuditTrail;
import com.krawler.esp.handlers.FileHandler;
import com.krawler.esp.handlers.ProfileHandler;
import com.krawler.esp.handlers.WidgetStateHandler;
import com.krawler.esp.handlers.projdb;
import com.krawler.utils.json.base.JSONArray;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import com.krawler.esp.portalmsg.Mail;
import com.krawler.esp.project.checklist.CheckListManager;
import com.krawler.esp.project.meter.HealthMeter;
import com.krawler.esp.project.meter.HealthMeterDAO;
import com.krawler.esp.project.meter.HealthMeterDAOImpl;
import com.krawler.esp.project.project.*;
import com.krawler.esp.user.UserDAOImpl;
import java.util.*;

public class AdminServlet extends HttpServlet {

    private static final long serialVersionUID = -5867737526265256817L;
    private static final SimpleDateFormat paypalDtFormat = new SimpleDateFormat("HH:mm:ss MMM dd,yyyy z");

    protected void processRequest(HttpServletRequest request,
            HttpServletResponse response) throws IOException, SessionExpiredException, ParseException {
        boolean isFormSubmit = false;
        if (SessionHandler.isValidSession(request, response)) {
            response.setContentType("text/html;charset=UTF-8");

            String companyid = AuthHandler.getCompanyid(request, true);
            if (companyid.equals("")) {
                companyid = request.getParameter("companyid");
            }
            String result = "";
            Connection conn = null;

            try {
                String userid = AuthHandler.getUserid(request);
                String ip = AuthHandler.getIPAddress(request);
                conn = DbPool.getConnection();
                int action = Integer.parseInt(request.getParameter("action"));
                int mode = Integer.parseInt(request.getParameter("mode"));
                String ss = "";
                if (request.getParameter("ss") != null) {
                    ss = request.getParameter("ss");
                }
                if (ss.equals("") && request.getParameter("query") != null) {
                    ss = request.getParameter("query");
                }

                switch (action) {
                    case 0:
                        switch (mode) {
                            case 0:
                                result = getAdminUserData(conn, request, companyid, ss, true);
                                break;

                            case 1:
                                result = getAdminCommData(conn, request, companyid);
                                break;

                            case 2:
                                result = getAdminProjData(conn, request, companyid, ss);
                                break;

                            case 3:
                                result = getPermissionSet(conn, request);
                                break;

                            case 4:
                                result = getCommunitiesList(conn, request, companyid);
                                break;

                            case 5:
                                isFormSubmit = true;
                                result = getProjectsList(conn, request, companyid);
                                break;

                            case 6:
                                result = getCommunityMemberDetails(conn, request, companyid);
                                break;

                            case 7:
                                //                            if(Integer.parseInt(request.getParameter("status")) == 3) {
                                //                                String pid = request.getParameter("featureid").toString();
                                //                                String projectMemberList = com.krawler.esp.database.dbcon.getProjectMembers(pid, 100, 0);
                                //                                com.krawler.esp.database.dbcon.checkForProjectMeemberInProj_resource(pid,
                                //                                                projectMemberList);
                                //                            }
                                result = getProjectMemberDetails(conn, request, companyid, ss);
                                break;

                            case 8:
                                result = createAnnouncementsForUser(conn, request, companyid, userid);
                                break;
                            case 9:
                                result = getUserPermissions(conn, request);
                                break;
                            case 10:
                                result = getCompanyDetails(conn, request);
                                break;
                            case 11:
                                result = getCompanySubscriptionDetails(conn, request, companyid);
                                break;
                            case 12:
                                result = getCountryList(conn);
                                isFormSubmit = true;
                                break;
                            case 13:
                                result = getTimeZoneList(conn);
                                isFormSubmit = true;
                                break;
                            case 14:
                                isFormSubmit = true;
                                result = getWorkWeek(conn, request);
                                break;
                            case 15:
                                isFormSubmit = true;
                                result = getCompanyHolidays(conn, request);
                                break;
                            case 16:
                                result = getAssignedProjctMembers(conn, request);
                                break;
                            case 17:
                                result = getUnassignedProjectMembers(conn, request);
                                break;
                            case 18:
                                isFormSubmit = true;
                                result = getCurrencies(conn);
                                break;
                            //                        case 19:
                            //                            result = checkProject(conn, request);
                            //                            break;
                            case 20:
                                isFormSubmit = true;
                                result = com.krawler.esp.handlers.dateFormatHandlers.sampleDateString(conn);
                                break;
                            case 21:
                                isFormSubmit = true;
                                result = getCompanyUsers(conn, companyid);
                                break;
                            //                        case 22:
                            //                            result = getNotificationStatus(conn,companyid);
                            //                            break;
                            //                        case 22:
                            //                            isFormSubmit=true;
                            //                            result = getArchivedProjList(conn, request);
                            //                            break;
                            case 23:
                                result = getModuleSubscription(conn, companyid);
                                break;

                            case 24:
                                result = getFeatureView(conn, companyid);
                                break;
                            case 25:
                                isFormSubmit = true;
                                result = com.krawler.esp.handlers.dateFormatHandlers.getOnlyDateFormats(conn);
                                break;
                            case 26:
                                //isFormSubmit = true; //Health meter base data
                                result = getHealthMeterBaseData(conn, request.getParameter("pid"));
                                break;
                            case 27:
                                //isFormSubmit = true;
                                String pid = request.getParameter("pid");
                                result = getStatusForHealthMeterchart(conn, pid);//Health meter base line data
                                break;
                        }
                        break;

                    case 1:
                        int emode = Integer.parseInt(request.getParameter("emode"));
                        String res = null;
                        switch (mode) {
                            // User Operations
                            case 0:
                                switch (emode) {
                                    case 3:
                                        // Add Users To Project/Community
                                        result = addUsersTo(conn, request);
                                        break;

                                    case 4:
                                        // Assign Permissions To User
                                        isFormSubmit = true;
                                        String loginid = AuthHandler.getUserid(request);
                                        String[] selectedUsers = request.getParameter("users").split(",");
                                        res = assignUserPermissions(conn, request);
                                        if (StringUtil.equal("success", res)) {
                                            result = KWLErrorMsgs.rsSuccessTrue;
                                            JSONArray params=new JSONArray();
                                            Forum.publishUserActivities("userpermission", selectedUsers, loginid, "", "msgChangeUserPerm","", isFormSubmit, this.getServletContext());
                                        } else {
                                            result = "{\"success\":false,\"data\":\"" + res + "\"}";
                                        }
                                        break;
                                }
                                break;

                            case 1:
                                // Community Operations
//                                switch (emode) {
//                                    case 0:
//                                        // Create New Community
//                                        res = createCommunity(conn, request, companyid);
//                                        if (StringUtil.equal("success", res)) {
//                                            result = KWLErrorMsgs.rsSuccessTrue;
//                                        } else {
//                                            result = "{\"success\":false,\"data\":\"" + res + "\"}";
//                                        }
//                                        break;
//
//                                    case 1:
//                                        // Edit Community
//                                        result = editCommunity(conn, request, companyid);
//                                        break;
//
//                                    case 2:
//                                        // Delete Community
//                                        deleteCommunity(conn, request, companyid);
//                                        break;
//                                }
                                break;

                            case 2:
                                // Project Operations
                                switch (emode) {
                                    case 0:
                                        // Create New Project
                                        isFormSubmit = true;
                                        res = createProject(conn, request, companyid);
                                        JSONObject jobj = new JSONObject(res);
                                        String stat = jobj.get("status").toString();
                                        String projid = jobj.get("projectid").toString();
                                        if (StringUtil.equal("success", stat)) {
                                            result = "{\"success\":true,\"data\":\"" + projid + "\"}";
                                        } else {
                                            result = "{\"success\":false,\"data\":\"" + res + "\"}";
                                        }
                                        break;

                                    case 1:
                                        // Edit Project
                                        isFormSubmit = true;
                                        result = editProject(conn, request, companyid);
                                        break;

                                    case 2:
                                        // Delete Project
                                        deleteProject(conn, request, companyid, userid, ip);
                                        break;
                                    case 3:
                                        //change in holiday calender
                                        String loginid = AuthHandler.getUserid(request);
                                        projid = request.getParameter("projid");
                                        JSONArray jusers = new JSONObject(Forum.getProjectMembers(conn, projid, 100, 0)).getJSONArray("data");
                                        int len = jusers.length();
                                        String[] selectedUsers = new String[len];
                                        for (int i = 0; i < len; i++) {
                                            selectedUsers[i] = jusers.getJSONObject(i).getString("id");
                                        }
                                        result = updateWorkWeek(conn, request);
                                        if (StringUtil.equal("success", result)) {
                                            Forum.publishUserActivities("projcalender", selectedUsers, loginid, projid, "msgSetWorkWeek","", true, this.getServletContext());
                                        } else {
                                            result = "{\"success\":false,\"data\":\"" + res + "\"}";
                                        }
                                        break;
                                    case 4:
                                        result = manageProjectMembers(conn, request);
                                        break;
                                    case 5:
                                        isFormSubmit = true;
                                        result = editHealthMeterData(conn, request);
                                        break;
                                    //                                case 5:
                                    //                                    result = changeProjectStatus(conn, request);
                                    //                                    break;
                                }
                                break;
                            case 3:
                                switch (emode) {
                                    case 0:
                                        isFormSubmit = true;
                                        res = editCompanyDetails(conn, request, companyid);
                                        if (StringUtil.equal("success", res.split(",")[0])) {
                                            if (res.split(",").length > 1) {
                                                result = "{\"success\":true,\"data\":\"" + res.split(",")[1] + "\"}";
                                            } else {
                                                result = KWLErrorMsgs.rsSuccessTrue;
                                            }
                                        } else {
                                            result = "{\"success\":false,\"data\":\"" + res + "\"}";
                                        }
                                        break;
                                    case 1:
                                        res = requestSubscription(conn, request, companyid);
                                        break;

                                    case 2:
                                        res = requestSubModuleSubscription(conn, request, companyid);
                                        break;

                                    case 3:
                                        res = updateFeatureView(conn, request, companyid);
                                        String users[] = getAllUsersId(conn,request);
                                        if (!res.equals("false")) {
                                            Forum.publishUserActivities("featureview", users, AuthHandler.getUserid(request), "", res,"", true, this.getServletContext());
                                        } else {
                                            result = "{\"success\":false,\"data\":\"" + res + "\"}";
                                        }
                                        break;
                                }
                        }
                        break;
                    case 2:
                        switch (mode) {
                            case 1:
                                setMaxUsers(conn, request, companyid);
                                break;
                            case 2:
                                setMaxProjects(conn, request, companyid);
                                break;
                            case 3:
                                setMaxCommunities(conn, request, companyid);
                                break;
                        }
                        break;
                }
                conn.commit();
            } catch (ServiceException ex) {
                result = KWLErrorMsgs.rsSuccessFalse;
                DbPool.quietRollback(conn);
            } catch (SessionExpiredException e) {
                result = KWLErrorMsgs.rsSuccessFalse;
                DbPool.quietRollback(conn);
            } catch (Exception e) {
                result = KWLErrorMsgs.rsSuccessFalse;
                DbPool.quietRollback(conn);
            } finally {
                DbPool.quietClose(conn);
                if (!isFormSubmit) {
                    try {
                        JSONObject jbj = new com.krawler.utils.json.base.JSONObject();
                        jbj.put("valid", "true");
                        jbj.put("data", result);
                        response.getWriter().println(jbj.toString());
                    } catch (JSONException ex) {
                        Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    response.getWriter().println(result);
                }
                response.getWriter().close();
            }

        } else { //session valid if() ends here
            response.getWriter().println("{\"valid\": false}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            try {
                processRequest(request, response);
            } catch (ParseException ex) {
                Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SessionExpiredException e) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            try {
                processRequest(request, response);
            } catch (ParseException ex) {
                Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SessionExpiredException e) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    @Override
    public String getServletInfo() {
        return "All Admin Related Functionality Implemented Here";
    }

    public static String getCompanyname(Connection conn, String companyid) {
        String cname = "";
        try {
            PreparedStatement p = conn.prepareStatement("SELECT companyname FROM company WHERE companyid = ?");
            p.setString(1, companyid);
            ResultSet r = p.executeQuery();
            if (r.next()) {
                cname = r.getString("companyname");
            }
        } catch (ServiceException e) {
        } catch (SQLException e) {
        }
        return cname;
    }

    public static String getUserName(Connection conn, String userid) throws ServiceException {
        String username = "";
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("select username from userlogin where userid = ?");
            pstmt.setString(1, userid);
            rs = pstmt.executeQuery();
            rs.next();
            if (rs.getString(1) != null) {
                username = rs.getString(1);
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("FileHandler.getAuthor", e);
        } finally {
        }
        return username;
    }

    public static String editCompanyDetails(Connection conn, HttpServletRequest request,
            String companyid) throws ServiceException, SessionExpiredException {
        String status = "failure";
        // int notificationduration = 0;
        int notificationtype = 0;
        double activity = 0;
        String sd = "";
        String companyEmail = "";
        DiskFileUpload fu = new DiskFileUpload();
        HashMap arrParam = new HashMap();
        FileItem fi1 = null;
        String logouploadmsg = "";

        List fileItems = null;
        try {
            fileItems = fu.parseRequest(request);
        } catch (FileUploadException e) {
            throw ServiceException.FAILURE("Admin.createUser", e);
        }
        for (Iterator k = fileItems.iterator(); k.hasNext();) {
            fi1 = (FileItem) k.next();
            arrParam.put(fi1.getFieldName(), fi1.getString());
        }
        try {
            String ipAddress = AuthHandler.getIPAddress(request);
            int auditMode = 0;
            String loginid = AuthHandler.getUserid(request);
            String params = AuthHandler.getAuthor(conn, loginid) + " ("
                    + AuthHandler.getUserName(request) + ")";
            int editAll = Integer.parseInt(request.getParameter("editAll"));
            // if(editAll >= 1) {
//                PreparedStatement pstmt = conn.prepareStatement("SELECT subdomain FROM company WHERE companyid=?");
//                pstmt.setString(1, companyid);
//                ResultSet rs = pstmt.executeQuery();
//                String subdomain = "";
//                if (rs.next()) {
//                    subdomain = rs.getString("subdomain");
//                }
//                boolean domainflag = true;
//                sd = StringUtil.serverHTMLStripper(arrParam.get("domainname").toString().toLowerCase());
//                if (!subdomain.equals(arrParam.get("domainname"))) {
//                    if (SignupHandler.subdomainIsAvailable(conn, sd).equalsIgnoreCase("failure") || StringUtil.isNullOrEmpty(sd)) {
//                        domainflag = false;
//                    } else {
//                        //mailtoAllOnSubdomainChange(conn, subdomain, sd, request); TODO: commented for now. need to implement. - brajesh@090909
//                    }
//                }
//                companyEmail = StringUtil.serverHTMLStripper(arrParam.get("mail").toString().trim());
//                String companyname = StringUtil.serverHTMLStripper(arrParam.get("companyname").toString());
//                String address = StringUtil.serverHTMLStripper(arrParam.get("address").toString());
//                String city = StringUtil.serverHTMLStripper(arrParam.get("city").toString());
//                String state = StringUtil.serverHTMLStripper(arrParam.get("state").toString());
//                String country = StringUtil.serverHTMLStripper(arrParam.get("country").toString());
//                String phone = StringUtil.serverHTMLStripper(arrParam.get("phone").toString());
//                String fax = StringUtil.serverHTMLStripper(arrParam.get("fax").toString());
//                String zip = StringUtil.serverHTMLStripper(arrParam.get("zip").toString());
//                String timezone = StringUtil.serverHTMLStripper(arrParam.get("timezone").toString());
//                String website = StringUtil.serverHTMLStripper(arrParam.get("website").toString());
//                String currency = StringUtil.serverHTMLStripper(arrParam.get("currency").toString());
            //  notificationduration = Integer.parseInt(arrParam.get("dur").toString());
            if (arrParam.containsKey("1")) {
                activity = Double.parseDouble("1");
                activity = Math.pow(2, activity);
                notificationtype += activity;
            }
            if (arrParam.containsKey("2")) {
                activity = Double.parseDouble("2");
                activity = Math.pow(2, activity);
                notificationtype += activity;
            }
//                if (!(StringUtil.isNullOrEmpty(sd)) && !(StringUtil.isNullOrEmpty(companyname)) && !(StringUtil.isNullOrEmpty(country)) && !(StringUtil.isNullOrEmpty(timezone)) && !(StringUtil.isNullOrEmpty(currency))) {
//                    if (domainflag) {
            DbUtil.executeUpdate(conn, "update company set notificationtype=? where companyid=?;",
                    new Object[]{notificationtype, companyid});
            int o_diff = Constants.DEFAULT_PERT_DURATION_DIFF, p_diff = Constants.DEFAULT_PERT_DURATION_DIFF;
            if (arrParam.containsKey("optimisticdiff")) {
                o_diff = Integer.parseInt(arrParam.get("optimisticdiff").toString());
            }
            if (arrParam.containsKey("pessimisticdiff")) {
                p_diff = Integer.parseInt(arrParam.get("pessimisticdiff").toString());
            }

            DbUtil.executeUpdate(conn, "update pertdefaults_company set o_diff=?, p_diff=? where companyid=?",
                    new Object[]{o_diff, p_diff, companyid});

            boolean val = false;
            if(arrParam.containsKey("milestonewidget")){
                if("on".equals(arrParam.get("milestonewidget").toString()))
                    val = true;
            }
            DbUtil.executeUpdate(conn, "UPDATE company SET milestonewidget = ? WHERE companyid = ?", new Object[]{val, companyid});
            WidgetStateHandler.updateCustomWidgetSetting(conn, companyid, val);

            val = false;
            if(arrParam.containsKey("checklist")) {
                if ("on".equals(arrParam.get("checklist").toString())) {
                    val = true;
                    new CheckListManager().calculateAllTasksProgresses(conn, companyid);
                }
            }
            DbUtil.executeUpdate(conn, "UPDATE company SET checklist = ? WHERE companyid = ?", new Object[]{val, companyid});
            val = false;
            if(arrParam.containsKey("docaccess")){
                if("on".equals(arrParam.get("docaccess").toString())){
                    val = true;
                    OpenOfficeServiceResolver resolver = OpenOfficeServiceResolver.get(request.getServletContext());
                    DocsConversionHandler.convertDocs(companyid, resolver);
                }
            }
            DbUtil.executeUpdate(conn, "UPDATE company SET docaccess = ? WHERE companyid = ?", new Object[]{val, companyid});
//                        pst = conn.prepareStatement("Select activityid,featureid from activitieslist where activityname=?");
//                        pst.setString(1, "ChangeCompanyLogo");
//                        ResultSet rset = pst.executeQuery();
//                        if (rset.next()) {
//                            int actid = rset.getInt("activityid");
//                            int featid = rset.getInt("featureid");
//                            pst = conn.prepareStatement("SELECT permissions FROM userpermissions WHERE userid = ? and featureid=?");
//                            pst.setString(1, AuthHandler.getUserid(request));
//                            pst.setInt(2, featid);
//                            ResultSet rset1 = pst.executeQuery();
//                            if (rset1.next()) {
//                                int perm = rset1.getInt("permissions");
//                                int num = (int) Math.pow(2, actid);
//                                if ((perm & num) == num) {
//                                    res = true;
//                                }
//                            }
//                        }

//                        if (res && editAll == 1) {
//                            if (arrParam.get("logo").toString().length() != 0) {
//                                genericFileUpload uploader = new genericFileUpload();
//                                uploader.doPostCompay(fileItems, companyid, StorageHandler.GetProfileImgStorePath());
//                                if (uploader.isUploaded()) {
//                                    DbUtil.executeUpdate(conn, "UPDATE company set image=? where companyid = ?",
//                                            new Object[]{ProfileImageServlet.ImgBasePath + companyid + uploader.getCompanyImageExt(), companyid});
//                                }
//                                logouploadmsg = uploader.ErrorMsg;
//
//                                AuditTrail.insertLog(conn, "331", loginid, "", "", companyid,
//                                                params, ipAddress, auditMode);
//                            }
//                        }
            String holidaysJson = arrParam.get("holidays").toString();
            com.krawler.utils.json.base.JSONObject holidays = new JSONObject(holidaysJson);
            String qry1 = "SELECT holiday,description FROM companyholidays where companyid=?";
            DbResults rs = DbUtil.executeQuery(conn, qry1, companyid);
            List hDays = new ArrayList();
            while (rs.next()) {
                hDays.add(rs.getObject("holiday").toString());
            }
            DbUtil.executeUpdate(conn, "DELETE FROM companyholidays WHERE companyid = ?", new Object[]{companyid});
            String qry = "INSERT INTO companyholidays (companyid, holiday, description) VALUES (?,?,?)";
            com.krawler.utils.json.base.JSONArray jarr = holidays.getJSONArray("data");
            for (int k = 0; k < jarr.length(); k++) {
                com.krawler.utils.json.base.JSONObject jobj = jarr.getJSONObject(k);
                DbUtil.executeUpdate(conn, qry, new Object[]{companyid, jobj.getString("day"), jobj.getString("description")});
            }
            if(hDays.size()!=jarr.length()){
            AuditTrail.insertLog(conn, "333", loginid, "", "", companyid, params, ipAddress, auditMode);
            }

            AuditTrail.insertLog(conn, "332", loginid, "", "", companyid,
                    params, ipAddress, auditMode);

            /*
            // notification config options
            String updateqry = "UPDATE notification set notifysum = ? where companyid = ? and nid = ?";
            String insertqry = "INSERT INTO notification (companyid, nid, notifysum) VALUES (?,?,?)";
            qry = "SELECT count(*) as count from notification where companyid = ? and nid = ?";
            String notifyJson = arrParam.get("notifyconf").toString();
            JSONObject notifyJObj = new JSONObject(notifyJson);
            jarr = notifyJObj.getJSONArray("data");
            for (int k = 0; k < jarr.length(); k++) {
            JSONObject jobj = jarr.getJSONObject(k);
            String nid = jobj.getString("nid");
            int type = 1;
            int sum = 0;
            while (true) {
            if (jobj.has(String.valueOf(type))) {
            if (jobj.getBoolean(String.valueOf(type))) {
            sum += Math.pow(2, type);
            }

            type++;
            } else {
            break;
            }
            }
            pstmt = conn.prepareStatement(qry);
            pstmt.setString(1, companyid);
            pstmt.setString(2, nid);
            ResultSet notifySet = pstmt.executeQuery();
            if (notifySet.next()) {
            if (notifySet.getInt("count") > 0) {
            DbUtil.executeUpdate(conn, updateqry, new Object[]{sum, companyid, nid});
            } else {
            DbUtil.executeUpdate(conn, insertqry, new Object[]{companyid, nid, sum});
            }
            }
            }
             */
//                        status = "success" + "," + logouploadmsg;
//                    } else {
//                        status = "success" + "," + "Subdomain is already registered.";
//                    }
//                }

//            } else if (arrParam.get("logo").toString().length() != 0) {
//                status = editCompanyLogo(conn, fileItems, companyid, AuthHandler.getUserid(request));
//                AuditTrail.insertLog(conn, "331", loginid, "", "", companyid,
//                        params, ipAddress, auditMode);
//            }
            status = "success";
        } catch (JSONException ex) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    public static String editCompanyLogo(Connection conn, List fileItems, String companyid, String userid) {
        String status = "";
        try {
            String userFullName = AuthHandler.getAuthor(conn, userid);
            String userName = AuthHandler.getUserName(conn, userid);
            boolean res = false;
            PreparedStatement pst = null;
            pst = conn.prepareStatement("Select activityid,featureid from activitieslist where activityname=?");
            pst.setString(1, "ChangeCompanyLogo");
            ResultSet rset = pst.executeQuery();
            if (rset.next()) {
                pst = conn.prepareStatement("SELECT permissions FROM userpermissions WHERE userid = ? and featureid=?");
                int actid = rset.getInt("activityid");
                int featid = rset.getInt("featureid");
                pst.setString(1, userid);
                pst.setInt(2, featid);
                ResultSet rset1 = pst.executeQuery();
                if (rset1.next()) {
                    int perm = rset1.getInt("permissions");
                    int num = (int) Math.pow(2, actid);
                    if ((perm & num) == num) {
                        res = true;
                    }
                }
            }
            if (res) {
                genericFileUpload uploader = new genericFileUpload();
                uploader.doPostCompay(fileItems, companyid, StorageHandler.GetProfileImgStorePath());
                if (uploader.isUploaded()) {
                    DbUtil.executeUpdate(conn, "UPDATE company set image=? where companyid = ?", new Object[]{ProfileImageServlet.ImgBasePath + companyid + uploader.getCompanyImageExt(), companyid});
                    String params = userFullName + " (" + userName + "), ";
                    AuditTrail.insertLog(conn, "112", userid, companyid, "", companyid, params, "", 0);
                }
                status = "success" + "," + uploader.ErrorMsg;
            }

        } catch (ConfigurationException ex) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE, null, ex);

        } catch (SQLException ex) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE, null, ex);

        } catch (ServiceException ex) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    public static boolean newCompanyHoliday(Connection conn, String holiday, String desc, String companyid) throws ServiceException {
        boolean isNew = true;
        PreparedStatement pstmt = null;
        try {
            int newVal = 0;
            pstmt = conn.prepareStatement("SELECT count(*) as count from companyholidays where holiday = ? and companyid=?");
            pstmt.setString(1, holiday);
            pstmt.setString(2, companyid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                newVal = rs.getInt("count");
                if (newVal == 0) {
                    String qry = "INSERT INTO companyholidays (companyid, holiday, description) VALUES (?,?,?)";
                    pstmt = conn.prepareStatement(qry);
                    pstmt.setString(1, companyid);
                    pstmt.setString(2, holiday);
                    pstmt.setString(3, desc);
                    pstmt.executeUpdate();
                } else {
                    isNew = false;
                }
            }
        } catch (SQLException e) {
            isNew = false;
        }
        return isNew;
    }

    public static boolean setMaxUsers(Connection conn, HttpServletRequest request, String companyid) throws ServiceException {
        boolean status = false;
        PreparedStatement pstmt = null;
        try {
            int newVal = 0;
            pstmt = conn.prepareStatement("SELECT maxusers FROM company WHERE companyid=?");
            pstmt.setString(1, companyid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                newVal = rs.getInt("maxusers") + Integer.parseInt(request.getParameter("mu"));
                int res = DbUtil.executeUpdate(conn, "update company set maxusers=? where companyid=?;", new Object[]{newVal, companyid});
                if (res != 0) {
                    status = true;
                }
            }
        } catch (SQLException e) {
            status = false;
        }
        return status;
    }

    public static boolean setMaxProjects(Connection conn, HttpServletRequest request, String companyid) throws ServiceException {
        boolean status = false;
        PreparedStatement pstmt = null;
        try {
            int newVal = 0;
            pstmt = conn.prepareStatement("SELECT maxprojects FROM company WHERE companyid=?");
            pstmt.setString(1, companyid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                newVal = rs.getInt("maxprojects") + Integer.parseInt(request.getParameter("mu"));
                int res = DbUtil.executeUpdate(conn, "update company set maxprojects=? where companyid=?;", new Object[]{newVal, companyid});
                if (res != 0) {
                    status = true;
                }
            }
        } catch (SQLException e) {
            status = false;
        }
        return status;
    }

    public static boolean setMaxCommunities(Connection conn, HttpServletRequest request, String companyid) throws ServiceException {
        boolean status = false;
        PreparedStatement pstmt = null;
        try {
            int newVal = 0;
            pstmt = conn.prepareStatement("SELECT maxcommunities FROM company WHERE companyid=?");
            pstmt.setString(1, companyid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                newVal = rs.getInt("maxcommunities") + Integer.parseInt(request.getParameter("mu"));
                int res = DbUtil.executeUpdate(conn, "update company set maxcommunities=? where companyid=?;", new Object[]{newVal, companyid});
                if (res != 0) {
                    status = true;
                }
            }
        } catch (SQLException e) {
            status = false;
        }
        return status;
    }

    public static String getCompanySubscriptionDetails(Connection conn, HttpServletRequest request, String companyid) throws ServiceException {
        PreparedStatement pstmt = null;
        String res = null;
        try {
            KWLJsonConverter KWL = new KWLJsonConverter();
            JSONObject resObj = new JSONObject();
            boolean flg = false;
            pstmt = conn.prepareStatement("SELECT featurelist.featureid, featurename, subscriptiondate, expdate "
                    + "FROM companysubscription INNER JOIN featurelist ON companysubscription.featureid=featurelist.featureid "
                    + "WHERE companyid = ?");
            pstmt.setString(1, companyid);
            ResultSet rs = pstmt.executeQuery();
            pstmt = conn.prepareStatement("SELECT * FROM featurelist");
            ResultSet rs1 = pstmt.executeQuery();
            while (rs1.next()) {
                JSONObject temp = new JSONObject();
                int fid = rs1.getInt("featureid");
                while (rs.next()) {
                    if (fid == rs.getInt("featureid")) {
                        temp.put("featureid", fid);
                        temp.put("subscriptiondate", rs.getDate("subscriptiondate"));
                        temp.put("featurename", rs.getString("featurename"));
                        temp.put("expdate", rs.getDate("expdate"));
                        temp.put("subscribed", true);
                        flg = true;
                        break;
                    }
                }
                if (!flg) {
                    temp.put("featureid", fid);
//                    temp.put("subscriptiondate", rs.getDate("subscriptiondate"));
                    temp.put("featurename", rs1.getString("featurename"));
//                    temp.put("expdate", rs.getDate("expdate"));
                    temp.put("subscribed", false);
                }
                rs.first();
                flg = false;
                resObj.append("data", temp);
            }
            res = resObj.toString();
        } catch (SQLException e) {
            res = KWLErrorMsgs.rsSuccessFalse;
//            System.out.print("dfg");
        } catch (JSONException ex) {
            res = KWLErrorMsgs.rsSuccessFalse;
        }
        return res;
    }

    public static String manageProjectMembers(Connection conn, HttpServletRequest request) {
        String res = "success";
        try {
            String pid = request.getParameter("projectid");
            String userid = AuthHandler.getUserid(request);
            String companyid = AuthHandler.getCompanyid(request);
            DbResults dbr = DbUtil.executeQuery(conn, "SELECT projectname FROM project WHERE projectid = ?", new Object[]{pid});
            String projName = "";
            if (dbr.next()) {
                projName = dbr.getString("projectname");
            }
            com.krawler.utils.json.base.JSONObject jobj = new JSONObject(request.getParameter("data").toString());
            dbr = DbUtil.executeQuery(conn, "SELECT userid FROM projectmembers WHERE projectid = ? AND inuseflag = 1 AND status >= 3", new Object[]{pid});
            HashMap members = new HashMap();
            int k = 0;
            while (dbr.next()) {
                members.put(dbr.getString("userid"), k);
                k++;
            }
//                DbUtil.executeUpdate(conn, "UPDATE projectmembers SET inuseflag = 0 WHERE projectid = ?", new Object[] {pid});
            com.krawler.utils.json.base.JSONArray jarr = jobj.getJSONArray("data");
            String subjectActive = "[" + projName + "] Access to the project activated.";
            String mailFooter = KWLErrorMsgs.mailSystemFooter;
            String msgActiveString = "Your access to the project : " + projName + " has been activated." + mailFooter;
            String chkQry = "SELECT status, inuseflag FROM projectmembers WHERE userid = ? AND projectid = ?";
            String updateQry = "UPDATE projectmembers SET inuseflag = 1, status = ? WHERE projectid = ? AND userid = ?";
            String insertQry = "INSERT INTO projectmembers (projectid, userid, status, inuseflag) VALUES (?,?,3,1)";
            String insertRes = "INSERT INTO proj_resources (resourceid, resourcename, projid) VALUES (?,?,?)";
            for (k = 0; k < jarr.length(); k++) {
                com.krawler.utils.json.base.JSONObject obj = jarr.getJSONObject(k);
                DbResults rs = DbUtil.executeQuery(conn, chkQry, new Object[]{obj.getString("userid"), pid});
                if (rs.next()) {
                    if (rs.getInt("status") == 4) {
                        DbUtil.executeUpdate(conn, updateQry, new Object[]{4, pid, obj.getString("userid")});
                    } else {
                        DbUtil.executeUpdate(conn, updateQry, new Object[]{3, pid, obj.getString("userid")});
                        if (rs.getInt("status") < 3) {
//                            dbr = DbUtil.executeQuery(conn, "SELECT username FROM users WHERE userid = ?", new Object[]{obj.getString("userid")});
//                            String resName = "";
//                            if (dbr.next()) {
                                String resName = getUserName(conn, obj.getString("userid"));//dbr.getString("username");
//                            }
                            DbUtil.executeUpdate(conn, insertRes, new Object[]{obj.getString("userid"), resName, pid});
                        }
                    }
                } else {
                    DbUtil.executeUpdate(conn, insertQry, new Object[]{pid, obj.getString("userid")});
//                    dbr = DbUtil.executeQuery(conn, "SELECT username FROM users WHERE userid = ?", new Object[]{obj.getString("userid")});
//                    String resName = "";
//                    if (dbr.next()) {
                       String resName = getUserName(conn, obj.getString("userid"));//dbr.getString("username");
//                    }
                    DbUtil.executeUpdate(conn, insertRes, new Object[]{obj.getString("userid"), resName, pid});
                    Mail.insertMailMsg(conn, resName, userid, subjectActive, msgActiveString, "1", false, "1", "", "newmsg", "", 3, "", companyid);
                }
                members.remove(obj.getString("userid"));
            }
            Object[] obj = members.keySet().toArray();
            for (k = 0; k < obj.length; k++) {
                subjectActive = "Your access to the project: " + projName + " has been deactivated.";
                String[] ids = {obj[k].toString()};
                if (!Forum.chkResourceDependency(conn, ids, pid)) {
                    DbUtil.executeUpdate(conn, "DELETE FROM projectmembers WHERE projectid = ? AND userid = ?", new Object[]{pid, obj[k]});
                    DbUtil.executeUpdate(conn, "DELETE FROM proj_resources WHERE projid = ? AND resourceid = ?", new Object[]{pid, obj[k]});
                } else {
                    DbUtil.executeUpdate(conn, "UPDATE projectmembers SET inuseflag = 0, status = 0 WHERE projectid = ? AND userid = ?", new Object[]{pid, obj[k]});
                    DbUtil.executeUpdate(conn, "UPDATE proj_resources SET inuseflag = 0 WHERE projid = ? AND resourceid = ?", new Object[]{pid, obj[k]});
                }
//                dbr = DbUtil.executeQuery(conn, "SELECT username FROM users WHERE userid = ?", new Object[]{obj[k]});
//                String resName = "";
//                if (dbr.next()) {
                    String resName = getUserName(conn,obj[k].toString());//dbr.getString("username");
//                }
                msgActiveString = /*resName + */ "Your access to the project: " + projName + " has been deactivated. " + mailFooter;
                Mail.insertMailMsg(conn, resName, request.getParameter("lid"), subjectActive, msgActiveString, "1", false, "1", "", "newmsg", "", 3, "", companyid);
            }

            String ipAddress = AuthHandler.getIPAddress(request);
            int auditMode = 0;
            String loginid = AuthHandler.getUserid(request);
            String params = AuthHandler.getAuthor(conn, loginid)
                    + " (" + AuthHandler.getUserName(request)
                    + "), " + projName;

            AuditTrail.insertLog(conn, "324", loginid, pid, pid, companyid,
                    params, ipAddress, auditMode);

        } catch (ServiceException ex) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE,
                    "Service Exception While Manging Members In Project", ex);
            res = ex.getMessage();

        } catch (ParseException ex) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE,
                    "Parse Exception While Manging Members In Project", ex);
            res = ex.getMessage();

        } catch (JSONException ex) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE,
                    "JSON Exception While Manging Members In Project", ex);
            res = ex.getMessage();

        } catch (SessionExpiredException ex) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE,
                    "Session Expired Exception While Manging Members In Project", ex);
            res = ex.getMessage();
        }
        return res;
    }

    public static String getAssignedProjctMembers(Connection conn, HttpServletRequest request) {
        String res = "";
        try {
            PreparedStatement pstmt = conn.prepareStatement("SELECT projectmembers.userid, userlogin.username, projectmembers.status "
                    + "FROM projectmembers INNER JOIN users ON users.userid = projectmembers.userid "
                    + "INNER JOIN userlogin ON projectmembers.userid = userlogin.userid "
                    + "WHERE projectid = ? AND inuseflag = 1 AND status IN (3,4,5) and userlogin.isactive = 1");
            pstmt.setString(1, request.getParameter("projid"));
            ResultSet rs = pstmt.executeQuery();
            KWLJsonConverter j = new KWLJsonConverter();
            res = j.GetJsonForGrid(rs);
        } catch (Exception ex) {
            res = ex.getMessage();
        }
        return res;
    }

    public static String getUnassignedProjectMembers(Connection conn, HttpServletRequest request) {
        String res = "";
        try {
            PreparedStatement pstmt = conn.prepareStatement("SELECT users.userid, userlogin.username, 0 AS status FROM users "
                    + "INNER JOIN userlogin ON users.userid = userlogin.userid WHERE users.userid NOT IN "
                    + "(SELECT userid FROM projectmembers WHERE projectid = ? AND inuseflag = 1 AND status IN (3,4,5)) AND companyid = "
                    + "(SELECT companyid FROM project WHERE projectid = ?) and userlogin.isactive = 1");
            pstmt.setString(1, request.getParameter("projid"));
            pstmt.setString(2, request.getParameter("projid"));
            ResultSet rs = pstmt.executeQuery();
            KWLJsonConverter k = new KWLJsonConverter();
            res = k.GetJsonForGrid(rs);
        } catch (Exception ex) {
            res = ex.getMessage();
        }
        return res;
    }

    public static String getWorkWeek(Connection conn, HttpServletRequest request) throws ServiceException {
        String res = "";
        try {
            JSONObject jObj = new JSONObject();
            PreparedStatement pstmt = conn.prepareStatement("SELECT day,intime,outtime,isholiday FROM proj_workweek WHERE projectid = ? ORDER BY day");
            pstmt.setString(1, request.getParameter("projid"));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                JSONObject rObj = new JSONObject();
                rObj.put("day", rs.getObject("day").toString());
                rObj.put("intime", rs.getString("intime"));
                rObj.put("outtime", rs.getString("outtime"));
                rObj.put("isholiday", rs.getObject("isholiday").toString());
                jObj.append("data", rObj);
            }
            res = jObj.toString();
        } catch (Exception ex) {
            res = ex.getMessage();
        }
        return res;
    }

    public static String getCompanyHolidays(Connection conn, HttpServletRequest request) throws ServiceException {
        String res = "";
        String qry = "SELECT holiday, description FROM companyholidays WHERE companyid = (SELECT companyid FROM project WHERE projectid = ?)";
        String id = "";
        if (StringUtil.isNullOrEmpty(request.getParameter("projid"))) {
            qry = "SELECT holiday, description FROM companyholidays WHERE companyid = ?";
            id = request.getParameter("companyid");
        } else {
            id = request.getParameter("projid");
        }
        com.krawler.utils.json.base.JSONObject robj = new JSONObject();
        try {
            PreparedStatement pstmt = conn.prepareStatement(qry);
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            KWLJsonConverter j = new KWLJsonConverter();
            res = j.GetJsonForGrid(rs);
            robj.put("valid", true);
            robj.put("data", res);
        } catch (Exception ex) {
            res = ex.getMessage();
        }
        return robj.toString();
    }

    public static String getCurrencies(Connection conn) throws ServiceException {
        ResultSet rs = null;
        String str = null;
        PreparedStatement pstmt = null;
        pstmt = conn.prepareStatement("select currencyid,currencyname,symbol,htmlcode from currency");
        try {
            rs = pstmt.executeQuery();
            KWLJsonConverter KWL = new KWLJsonConverter();
            str = KWL.GetJsonForGrid(rs).toString();
            rs.close();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("commonHandler.getCurrencies", e);
        }
        return str;
    }

    public static String updateWorkWeek(Connection conn, HttpServletRequest request) throws ServiceException {
        String res = "failure";
        try {
            boolean isHolidayBeingChanged = Boolean.parseBoolean(request.getParameter("holidayChange"));
            String projectid = request.getParameter("projid");
            String qry = "UPDATE proj_workweek SET intime = ?, outtime = ?, isholiday = ? WHERE day = ? AND projectid = ?";
            int val = 0;
            boolean ih = false;
            if (StringUtil.equal(request.getParameter("isholiday"), "on")) {
                ih = true;
            }
            val = DbUtil.executeUpdate(conn, qry, new Object[]{request.getParameter("intime"), request.getParameter("outtime"), ih, Integer.parseInt(request.getParameter("day")), projectid});
            if (val != 0 && isHolidayBeingChanged) {
                res = "success";
                String loginid = AuthHandler.getUserid(request);
                String userFullName = AuthHandler.getAuthor(conn, loginid);
                String userName = AuthHandler.getUserName(request);
                String projName = projdb.getProjectName(conn, projectid);
                String params = userFullName + " (" + userName + "), " + request.getParameter("dayLabel") + ", " + projName;
                String actionid = "";
                if (ih) {
                    actionid = "155";
                } else {
                    actionid = "156";
                }
                String companyid = AuthHandler.getCompanyid(request);
                String ipAddress = AuthHandler.getIPAddress(request);
                AuditTrail.insertLog(conn, actionid, loginid, projectid, projectid, companyid, params, ipAddress, 0);
            }
        } catch (Exception ex) {
            res = ex.getMessage();
        }
        return res;
    }

    public static String updateWorkWeek(Connection conn, int day, boolean ih, String projid) throws ServiceException {
        String res = "failure";
        try {
            String qry = "UPDATE proj_workweek SET isholiday = ? WHERE day = ? AND projectid = ?";
            int val = 0;
            val = DbUtil.executeUpdate(conn, qry, new Object[]{ih, day, projid});
            if (val != 0) {
                res = "success";
            }
        } catch (Exception ex) {
            res = ex.getMessage();
        }
        return res;
    }

    public static String makeNickName(Connection conn, String name, int flag) throws ServiceException {
        String nickName = name.toLowerCase().trim().replaceAll("\\s+", "-").replaceAll("[^\\w|\\-]", "");
        String sql = "select nickName from " + (flag == 1 ? "project" : "community") + " where nickName like ?";
        DbResults rs = DbUtil.executeQuery(conn, sql, nickName + "%");
        java.util.ArrayList<String> namesArray = new java.util.ArrayList<String>();
        while (rs.next()) {
            namesArray.add(rs.getString(1));
        }
        int i = 0;
        while (namesArray.indexOf(nickName) != -1) {
            namesArray.remove(nickName);
            i++;
            nickName = nickName + i;
        }
        return nickName;
    }
//
//    public static String createCommunity(Connection conn,
//            HttpServletRequest request, String companyid)
//            throws ServiceException {
//        String status = "failure";
//        String communityid = UUID.randomUUID().toString();
//        DiskFileUpload fu = new DiskFileUpload();
//        List fileItems = null;
//        FileItem fi1 = null;
//        JSONObject j = new JSONObject();
//        try {
//            fileItems = fu.parseRequest(request);
//        } catch (FileUploadException e) {
//            throw ServiceException.FAILURE("Admin.createCommunity", e);
//        }
//
//        HashMap arrParam = new HashMap();
//        for (Iterator k = fileItems.iterator(); k.hasNext();) {
//            fi1 = (FileItem) k.next();
//            arrParam.put(fi1.getFieldName(), fi1.getString());
//        }
//        try {
//            PreparedStatement pstmt = null;
//            pstmt = conn.prepareStatement("select count(communityid) from community where companyid =?");
//            pstmt.setString(1, companyid);
//            ResultSet rs = pstmt.executeQuery();
//            int noCommunity = 0;
//            int maxCommunity = 0;
//            if (rs.next()) {
//                noCommunity = rs.getInt(1);
//            }
//            pstmt = conn.prepareStatement("select maxcommunities from company where companyid =?");
//            pstmt.setString(1, companyid);
//            rs = pstmt.executeQuery();
//            if (rs.next()) {
//                maxCommunity = rs.getInt(1);
//            }
//            if (noCommunity == maxCommunity) {
//                return "The maximum limit for communities for this company has already reached";
//            }
//        } catch (SQLException e) {
//            throw ServiceException.FAILURE("ProfileHandler.getPersonalInfo", e);
//        }
//
//        try {
//            String userid = AuthHandler.getUserid(request);
//            String commName = arrParam.get("communityname").toString().replaceAll("[^\\w|\\s|'|\\-|\\[|\\]|\\(|\\)]", "").trim();
//            String nickName = makeNickName(conn, commName, 0);
//            DbUtil.executeUpdate(
//                    conn,
//                    "INSERT INTO community (communityid,communityname,aboutcommunity,image, companyid, nickname) VALUES (?,?,?,?,?,?)",
//                    new Object[]{communityid,
//                        commName,
//                        arrParam.get("aboutcommunity"), "",
//                        companyid, nickName});
//
//            if (arrParam.get("image").toString().length() != 0) {
//                genericFileUpload uploader = new genericFileUpload();
//                uploader.doPost(fileItems, communityid, StorageHandler.GetProfileImgStorePath());
//                if (uploader.isUploaded()) {
//                    DbUtil.executeUpdate(
//                            conn,
//                            "update community set image=? where communityid = ? and companyid=?",
//                            new Object[]{
//                                ProfileImageServlet.ImgBasePath
//                                + communityid
//                                + uploader.getExt(),
//                                communityid, companyid});
//                }
//                j.put("image", communityid + uploader.getExt());
//            }
////            com.krawler.esp.handlers.CommunityHandler.setStatusCommunity(conn, userid, communityid, 4, 0);
//            j.put("id", communityid);
//            status = "success";
//        } catch (JSONException e) {
//            status = "failure";
//            throw ServiceException.FAILURE("Admin.createCommunity", e);
//        } catch (ConfigurationException e) {
//            status = "failure";
//            throw ServiceException.FAILURE("Admin.createCommunity", e);
//        } catch (com.krawler.common.session.SessionExpiredException e) {
//            status = "failure";
//            throw ServiceException.FAILURE("Admin.createCommunity", e);
//        }
//        return status;
//    }
//
//    public static String editCommunity(Connection conn,
//            HttpServletRequest request, String companyid)
//            throws ServiceException {
//        DiskFileUpload fu = new DiskFileUpload();
//        List fileItems = null;
//        FileItem fi1 = null;
//        String status = "failure";
//        try {
//            fileItems = fu.parseRequest(request);
//        } catch (FileUploadException e) {
//            throw ServiceException.FAILURE("Admin.editCommunity", e);
//        }
//
//        HashMap arrParam = new HashMap();
//        for (Iterator k = fileItems.iterator(); k.hasNext();) {
//            fi1 = (FileItem) k.next();
//            arrParam.put(fi1.getFieldName(), fi1.getString());
//        }
//
//        DbUtil.executeUpdate(
//                conn,
//                "update community  set communityname = ?, aboutcommunity = ? where communityid=? and companyid=?",
//                new Object[]{arrParam.get("communityname"),
//                    arrParam.get("aboutcommunity"),
//                    arrParam.get("communityid"), companyid});
//
//        if (arrParam.get("image").toString().length() != 0) {
//            genericFileUpload uploader = new genericFileUpload();
//            try {
//                uploader.doPost(fileItems, arrParam.get("communityid").toString(), StorageHandler.GetProfileImgStorePath());
//            } catch (ConfigurationException e) {
//                throw ServiceException.FAILURE("Admin.editCommunity", e);
//            }
//            if (uploader.isUploaded()) {
//                DbUtil.executeUpdate(
//                        conn,
//                        "update community set image= ? where communityid = ? and companyid=?",
//                        new Object[]{
//                            ProfileImageServlet.ImgBasePath
//                            + arrParam.get("communityid").toString()
//                            + uploader.getExt(),
//                            arrParam.get("communityid"), companyid});
//            }
//        }
//        try {
//            JSONObject j = new JSONObject();
//            PreparedStatement pstmt = null;
//            pstmt = conn.prepareStatement("select * from community where communityid=? and companyid=?");
//            pstmt.setString(1, arrParam.get("communityid").toString());
//            pstmt.setString(2, companyid);
//            ResultSet rs = pstmt.executeQuery();
//            if (rs.next()) {
//                j.put("communityname", rs.getString("communityname"));
//                j.put("description", rs.getString("aboutcommunity"));
//                j.put("image", rs.getString("image"));
//            }
//            pstmt = conn.prepareStatement("select count(userid) from communitymembers where communityid = ?");
//            pstmt.setString(1, arrParam.get("communityid").toString());
//            rs = pstmt.executeQuery();
//            if (rs.next()) {
//                j.put("members", rs.getInt(1));
//            }
//            status = "{\"success\":true,\"data\":" + j.toString() + "}";
//        } catch (JSONException ex) {
//            throw ServiceException.FAILURE("Admin.editUser", ex);
//        } catch (SQLException ex) {
//            throw ServiceException.FAILURE("Admin.editUser", ex);
//        }
//        return status;
//    }
//
//    public static void deleteCommunity(Connection conn,
//            HttpServletRequest request, String companyid)
//            throws ServiceException {
//        String[] ids = request.getParameter("commId").split(",");
//        for (int i = 0; i < ids.length; i++) {
//            DbUtil.executeUpdate(
//                    conn,
//                    "Delete from community where communityid=? and companyid=?",
//                    new Object[]{ids[i], companyid});
//            DbUtil.executeUpdate(
//                    conn,
//                    "Delete from actionlog where actionlog.by=? or actionlog.to=? or actionlog.userid=?",
//                    new Object[]{ids[i], ids[i], ids[i]});
//        }
//    }

    public static String createProject(Connection conn,
            HttpServletRequest request, String companyid)
            throws ServiceException {
        String status = "";
        DiskFileUpload fu = new DiskFileUpload();
        List fileItems = null;
        String imageName = "";
        JSONObject jres = new JSONObject();
        try {
            fileItems = fu.parseRequest(request);
        } catch (FileUploadException e) {
            throw ServiceException.FAILURE("Admin.createProject", e);
        }

        HashMap arrParam = new HashMap();
        for (Iterator k = fileItems.iterator(); k.hasNext();) {
            try {
                FileItem fi1 = (FileItem) k.next();
                arrParam.put(fi1.getFieldName(), fi1.getString("UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                throw ServiceException.FAILURE("Admin.createProject", ex);
            }
        }
        try {
            String userid = AuthHandler.getUserid(request);
            String projectid = UUID.randomUUID().toString();
            String projName = StringUtil.serverHTMLStripper(arrParam.get("projectname").toString().replaceAll("[^\\w|\\s|'|\\-|\\[|\\]|\\(|\\)]", "").trim());
            String nickName = makeNickName(conn, projName, 1);
            if (StringUtil.isNullOrEmpty(projName)) {
                status = "failure";
            } else {
                java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy/MM/dd hh:00:00");
                java.util.Date sd = sdf1.parse(sdf1.format(new Date()));
                java.sql.Timestamp sqlPostDate = new java.sql.Timestamp(new java.util.Date().getTime());
                String qry = "INSERT INTO project (projectid,projectname,description,image,companyid, nickname, createdon, startdate) VALUES (?,?,?,?,?,?,?,?)";
                DbUtil.executeUpdate(conn, qry, new Object[]{projectid, projName, arrParam.get("aboutproject"), imageName, companyid, nickName, sqlPostDate, sd});
                if (arrParam.get("image").toString().length() != 0) {
                    genericFileUpload uploader = new genericFileUpload();
                    uploader.doPost(fileItems, projectid, StorageHandler.GetProfileImgStorePath());
                    if (uploader.isUploaded()) {
                        DbUtil.executeUpdate(conn,
                                "update project set image=? where projectid = ?",
                                new Object[]{
                                    ProfileImageServlet.ImgBasePath + projectid
                                    + "_200" + uploader.getExt(), projectid});
                        imageName = projectid + "_200" + uploader.getExt();
                    }
                }
                com.krawler.esp.handlers.Forum.setStatusProject(conn, userid, projectid, 4, 0, "", companyid);
                status = "success";
                setDefaultWorkWeek(conn, projectid);
                HealthMeterDAO daoHM = new HealthMeterDAOImpl();
                daoHM.setBaseLineMeter(conn, projectid);
                /* inser custom fields value */
                Map<String, String> fields = CcUtil.getAllfields(arrParam);
                CustomColumn cc = CCManager.getCustomColumn(companyid);
                cc.insertColumnsData(conn, fields, "Project", projectid);
                String ipAddress = AuthHandler.getIPAddress(request);
                int auditMode = 0;
                String loginid = AuthHandler.getUserid(request);
                String params = AuthHandler.getAuthor(conn, loginid) + " ("
                        + AuthHandler.getUserName(request) + "), " + projName;

                AuditTrail.insertLog(conn, "321", loginid, projectid, projectid, companyid,
                        params, ipAddress, auditMode);
                jres.put("status", status);
                jres.put("projectid", projectid);
                ChartDataServlet.updateChartDataRequestChangeFlag(conn, userid, true);
            }
        } catch (JSONException e) {
            status = "failure";
            throw ServiceException.FAILURE("Admin.createProject", e);
        } catch (ConfigurationException e) {
            status = "failure";
            throw ServiceException.FAILURE("Admin.createProject", e);
        } catch (com.krawler.common.session.SessionExpiredException e) {
            status = "failure";
            throw ServiceException.FAILURE("Admin.createCommunity", e);
        } catch (ParseException e) {
            status = "failure";
            throw ServiceException.FAILURE("Admin.createProject", e);
        }
        return jres.toString();
    }

    public static void setDefaultWorkWeek(Connection conn, String pid) throws ServiceException {
        String qry = "INSERT INTO proj_workweek (projectid,day,intime,outtime,isholiday) VALUES(?,?,?,?,?)";
        try {
            for (int cnt = 0; cnt < 7; cnt++) {
                boolean holiday = (cnt == 0 || cnt == 6);
                DbUtil.executeUpdate(conn, qry, new Object[]{pid, cnt, "10:00:00", "18:00:00", holiday});
            }
        } catch (ServiceException ex) {
            ServiceException.FAILURE("WORK WEEK", ex);
        }
    }

    public static String editProject(Connection conn,
            HttpServletRequest request, String companyid)
            throws ServiceException {
        String status = "{\"success\":failure}";
        DiskFileUpload fu = new DiskFileUpload();
        JSONObject j = new JSONObject();
        JSONObject j1 = new JSONObject();
        List fileItems = null;
        try {
            fileItems = fu.parseRequest(request);
        } catch (FileUploadException e) {
            // Logger.getInstance(Admin.class).error(e, e);
            throw ServiceException.FAILURE("Admin.editProject", e);
        }

        HashMap arrParam = new HashMap();
        for (Iterator k = fileItems.iterator(); k.hasNext();) {
            try {
                FileItem fi1 = (FileItem) k.next();
                arrParam.put(fi1.getFieldName(), fi1.getString("UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                throw ServiceException.FAILURE("Admin.editProject", ex);
            }
        }
        try {
            String projectname = StringUtil.serverHTMLStripper(arrParam.get("projectname").toString());
            String projectid = StringUtil.serverHTMLStripper(arrParam.get("projectid").toString());
            String aboutproject = StringUtil.serverHTMLStripper(arrParam.get("aboutproject").toString());
            if (StringUtil.isNullOrEmpty(projectname) || StringUtil.isNullOrEmpty(projectid)) {
                status = KWLErrorMsgs.rsSuccessFailure;
                return status;
            }
            DbUtil.executeUpdate(conn, "update project  set projectname = ?, description = ? where projectid=? and companyid=?",
                    new Object[]{projectname, aboutproject, projectid, companyid});
            if (arrParam.get("image").toString().length() != 0) {
                genericFileUpload uploader = new genericFileUpload();
                uploader.doPost(fileItems, arrParam.get("projectid").toString(), StorageHandler.GetProfileImgStorePath());
                if (uploader.isUploaded()) {
                    DbUtil.executeUpdate(conn, "update project set image=? where projectid = ? and companyid=?",
                            new Object[]{ProfileImageServlet.ImgBasePath + projectid + "_200" + uploader.getExt(),
                                projectid, companyid});
                    j.put("image", projectid + "_200" + uploader.getExt());
                }
            }
            HealthMeterDAO daoHM = new HealthMeterDAOImpl();
            daoHM.editBaseLineMeter(conn, projectid, "TASK", arrParam.get("ontime").toString(), arrParam.get("slightly").toString(), arrParam.get("slightly").toString());
            Map<String, String> fields = CcUtil.getAllfields(arrParam);
            CustomColumn cc = CCManager.getCustomColumn(companyid);
            cc.editColumnsData(conn, fields, projectid);
            PreparedStatement pstmt = null;
            pstmt = conn.prepareStatement("select * from project where projectid=? and companyid=?");
            pstmt.setString(1, arrParam.get("projectid").toString());
            pstmt.setString(2, companyid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                j.put("projectname", rs.getString("projectname"));
                j.put("description", rs.getString("description"));
                j.put("image", rs.getString("image"));
            }
            pstmt = conn.prepareStatement("select count(userid) from projectmembers where projectid = ? and inuseflag = 1");
            pstmt.setString(1, arrParam.get("projectid").toString());
            rs = pstmt.executeQuery();
            if (rs.next()) {
                j.put("members", rs.getInt(1));
            }
            status = "{\"success\":true,\"data\":" + j.toString() + "}";

            String ipAddress = AuthHandler.getIPAddress(request);
            int auditMode = 0;
            String loginid = AuthHandler.getUserid(request);
            String params = AuthHandler.getAuthor(conn, loginid) + " ("
                    + AuthHandler.getUserName(request) + "), ";
            AuditTrail.insertLog(conn, "323", loginid, projectid, projectid, companyid,
                    params + projectname, ipAddress, auditMode);

        } catch (SessionExpiredException ex) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE, null, ex);
            status = KWLErrorMsgs.rsSuccessFailure;
            throw ServiceException.FAILURE("Admin.editProject", ex);

        } catch (JSONException e) {
            status = KWLErrorMsgs.rsSuccessFailure;
            throw ServiceException.FAILURE("Admin.editProject", e);
        } catch (ConfigurationException e) {
            status = KWLErrorMsgs.rsSuccessFailure;
            throw ServiceException.FAILURE("Admin.editProject", e);
        } catch (SQLException e) {
            status = KWLErrorMsgs.rsSuccessFailure;
            throw ServiceException.FAILURE("Admin.editProject", e);
        }
        return status;
    }

    public static String deleteProject(Connection conn,
            HttpServletRequest request, String companyid, String userid, String ip)
            throws ServiceException {

        String status = "";
        String[] ids = request.getParameter("projId").split(",");
        int length = ids.length;
        JSONObject j = new JSONObject();
        try {
            int auditMode = 0;
            String params = AuthHandler.getAuthor(conn, userid) + " ("
                    + AuthHandler.getUserName(conn, userid) + "), ";
            CustomColumn cc = CCManager.getCustomColumn(companyid);
            for (int i = 0; i < length; i++) {
                String projName = projdb.getProjectName(conn, ids[i]);
                DbUtil.executeUpdate(
                        conn,
                        "Delete from project where projectid=? and companyid=?",
                        new Object[]{ids[i], companyid});
//				DbUtil
//						.executeUpdate(
//								conn,
//								"Delete from actionlog where actionlog.actionby=?",
//								new Object[] { ids[i] });
                j.append("projectid", ids[i]);

                FileHandler.deleteProjectSpecificDoc(conn, ids[i]);
                cc.deleteColumnsData(conn, ids[i]);

                AuditTrail.insertLog(conn, "322", userid, ids[i], ids[i], companyid,
                        params + projName, ip, auditMode);
            }
            status = j.toString();

        } catch (JSONException e) {
            status = "failure";
            throw ServiceException.FAILURE("Admin.deleteProject", e);
        }
        return status;
    }
    /*
    public static String addUserProject(Connection conn, String projId,
    String userId, String role, String colorcode)
    throws ServiceException {

    ResultSet rs = null;
    PreparedStatement pstmt = null;
    String status = "";

    try {
    pstmt = conn
    .prepareStatement("INSERT INTO projectmembers (projectid,status,userid,colorcode) VALUES (?,?,?,?)");
    pstmt.setString(1, projId);
    pstmt.setString(2, role);
    pstmt.setString(3, userId);
    pstmt.setString(4, colorcode);
    rs = pstmt.executeQuery();
    rs.close();
    status = "success";
    } catch (SQLException e) {
    status = "failure";
    throw ServiceException.FAILURE("Admin.addUserProject", e);
    } finally {
    DbPool.closeStatement(pstmt);
    }
    return status;
    }
     */
    /*	public static String deleteUserProject(Connection conn, String projId,
    String userId) throws ServiceException {

    ResultSet rs = null;
    PreparedStatement pstmt = null;
    String status = "";

    try {
    pstmt = conn
    .prepareStatement("Delete from projectmembers where projectid=? and userid=?");
    pstmt.setString(1, projId);
    pstmt.setString(1, userId);
    rs = pstmt.executeQuery();
    rs.close();
    status = "success";
    } catch (SQLException e) {
    status = "failure";
    throw ServiceException.FAILURE("Admin.deleteUserProject", e);
    } finally {
    DbPool.closeStatement(pstmt);
    }
    return status;
    }
     */

    public static String addUserCommunity(Connection conn, String commId,
            String userId, String role) throws ServiceException {

        ResultSet rs = null;
        PreparedStatement pstmt = null;
        String status = "";

        try {
            pstmt = conn.prepareStatement("INSERT INTO communitymembers (communityid,status,userid,updatedon) VALUES (?,?,?,now())");
            pstmt.setString(1, commId);
            pstmt.setString(1, role);
            pstmt.setString(1, userId);
            rs = pstmt.executeQuery();
            rs.close();
            status = "success";
        } catch (SQLException e) {
            status = "failure";
            throw ServiceException.FAILURE("Admin.addUserCommunity", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return status;
    }

    public static String deleteUserCommunity(Connection conn, String commId,
            String userId) throws ServiceException {

        ResultSet rs = null;
        PreparedStatement pstmt = null;
        String status = "";

        try {
            pstmt = conn.prepareStatement("Delete from communitymembers where communityid=? and userid=?");
            pstmt.setString(1, commId);
            pstmt.setString(2, userId);
            rs = pstmt.executeQuery();
            rs.close();
            status = "success";
        } catch (SQLException e) {
            status = "failure";
            throw ServiceException.FAILURE("Admin.deleteUserCommunity", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return status;
    }

    public static String updateUserCommunity(Connection conn, String commId,
            String userId, String role) throws ServiceException {

        ResultSet rs = null;
        PreparedStatement pstmt = null;
        String status = "";

        try {
            pstmt = conn.prepareStatement("update communitymembers set status=?  where communityid=? and userid=?");
            pstmt.setString(1, role);
            pstmt.setString(2, commId);
            pstmt.setString(3, userId);
            rs = pstmt.executeQuery();
            rs.close();
            status = "success";
        } catch (SQLException e) {
            status = "failure";
            throw ServiceException.FAILURE("Admin.updateUserCommunity", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return status;
    }
    /*
    public static String updateUserProject(Connection conn, String commId,
    String userId, String role, String colorcode)
    throws ServiceException {

    ResultSet rs = null;
    PreparedStatement pstmt = null;
    String status = "";
    try {
    pstmt = conn
    .prepareStatement("update projectmembers set status=?, colorcode=?  where projectid=? and userid=?");
    pstmt.setString(1, role);
    pstmt.setString(2, colorcode);
    pstmt.setString(3, commId);
    pstmt.setString(4, userId);
    rs = pstmt.executeQuery();
    rs.close();
    status = "success";
    } catch (SQLException e) {
    status = "failure";
    throw ServiceException.FAILURE("Admin.updateUserProject", e);
    } finally {
    DbPool.closeStatement(pstmt);
    }
    return status;
    }
     */

    public static String getAdminUserData(Connection conn,
            HttpServletRequest request, String companyid, String searchString, boolean fromHere)
            throws ServiceException {

        int count = 0;
        String data = "";
        PreparedStatement pstmt = null;
        JSONObject res = new JSONObject();
        String[] searchStrObj = new String[]{"concat(users.fname, ' ',users.lname)", "userlogin.username"};
        String myLikeString = StringUtil.getMySearchString(searchString, "and", searchStrObj);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Date d = new java.util.Date();

        String COUNT_USERS = " SELECT COUNT(*) as count "
                + " FROM users INNER JOIN userlogin ON users.userid=userlogin.userid "
                + " WHERE companyid=? and userlogin.isactive = 1";

        String GET_USERS = " SELECT image, users.userid, userlogin.username, fname, lname, emailid, aboutuser, address, contactno, CONCAT(fname, ' ', lname) as fullname, "
                + " case "
                + " when date(lastactivitydate) <= date('1990-01-01 00:00:00') then '' "
                + " else lastactivitydate end as lastactivitydate "
                + " FROM users INNER JOIN userlogin ON users.userid=userlogin.userid "
                + " WHERE companyid=? and userlogin.isactive = 1"
                + myLikeString
                + " LIMIT ? OFFSET ? ";

        String GET_MAX_USERS = " SELECT maxusers,costperuser "
                + " FROM company INNER JOIN planedition ON planedition.planid=company.planid "
                + " WHERE company.companyid=? ";

        ResultSet rs = null;
        try {
            String loginid = "";
            try {
                loginid = AuthHandler.getUserid(request);
            } catch (SessionExpiredException e) {
                loginid = "";
            }
            pstmt = conn.prepareStatement(COUNT_USERS + myLikeString);

            pstmt.setString(1, companyid);
            StringUtil.insertParamSearchString(2, pstmt, searchString, searchStrObj.length);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt("count");
            }


            if (count > 0) {
                rs = null;
                pstmt = null;

                pstmt = conn.prepareStatement(GET_USERS);
                pstmt.setString(1, companyid);
                int cnt = StringUtil.insertParamSearchString(2, pstmt, searchString, searchStrObj.length);
                pstmt.setInt(cnt++, Integer.parseInt(request.getParameter("limit")));
                pstmt.setInt(cnt++, Integer.parseInt(request.getParameter("start")));
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    JSONObject temp = new JSONObject();
                    String img = StringUtil.getAppsImagePath(rs.getString("userid"), 35);
                    temp.put("image", img);
                    temp.put("userid", rs.getString("userid"));
                    temp.put("username", rs.getString("username"));
                    temp.put("fname", rs.getString("fname"));
                    temp.put("lname", rs.getString("lname"));
                    temp.put("fullname", rs.getString("fullname"));
                    temp.put("emailid", rs.getString("emailid"));
                    String lastLogin = rs.getObject("lastactivitydate").toString();
                    if (fromHere) {
//                        lastLogin = Timezone.dateTimeRenderer(conn, Timezone.toUserTimezone(conn, sdf.format(d), loginid), lastLogin, loginid);
                        lastLogin = Timezone.toCompanyTimezone(conn, lastLogin, companyid);
                    }
                    temp.put("lastlogin", lastLogin);// rs.getObject("lastactivitydate"));
                    temp.put("aboutuser", rs.getString("aboutuser"));
                    temp.put("address", rs.getString("address"));
                    temp.put("contactno", rs.getString("contactno"));
                    String userid = rs.getString("userid");
                    PermissionManager pm = new PermissionManager();
                    List<String> allPermissions = pm.getUserActivePermissionList(conn, userid);
                    Iterator itr = allPermissions.iterator();
                    String permissions ="";
                    if(allPermissions.isEmpty()){
                        String un=rs.getString("fullname");
                        permissions = "[<i>"+MessageSourceProxy.getMessage("pm.permission.msg.notassigned", new Object[]{un}, request)+"</i>]";
                    }
                    while(itr.hasNext()){
                        String tempStr = (String) itr.next();
                        permissions += MessageSourceProxy.getMessage("pm.permission."+tempStr,null,request) +"<br>";
                    }
                    temp.put("permissions", permissions);
                    res.append("data", temp);
                }
                pstmt.close();
            } else {
                res.put("data", "");
            }
            pstmt = conn.prepareStatement(GET_MAX_USERS);
            pstmt.setString(1, companyid);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                res.put("count", count);
                res.put("maxusers", rs.getInt("maxusers"));
                res.put("costperuser", rs.getInt("costperuser"));
            }
            data = res.toString();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("Admin.getAdminUserData", e);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("Admin.getAdminUserData", ex);
        } catch (ServiceException ex){
            throw ServiceException.FAILURE("Admin.getAdminUserData", ex);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return data;
    }

    public static String getAdminCommData(Connection conn,
            HttpServletRequest request, String companyid)
            throws ServiceException {
        String result = null;
        PreparedStatement pstmt = null;
        KWLJsonConverter KWL = new KWLJsonConverter();
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement("select community.image, community.communityid, community.communityname, community.aboutcommunity, community.createdon,"
                    + "count(communitymembers.communityid) as count from community inner join communitymembers on "
                    + "community.communityid=communitymembers.communityid where communitymembers.userid = ? group by communityid order by count LIMIT ? OFFSET ?;");
            pstmt.setString(1, request.getParameter("userid"));
            pstmt.setInt(2, Integer.parseInt(request.getParameter("limit")));
            pstmt.setInt(3, Integer.parseInt(request.getParameter("start")));
            rs = pstmt.executeQuery();
            result = KWL.GetJsonForGrid(rs);
            pstmt.close();
            pstmt = conn.prepareStatement("select count(*) as count from community inner join communitymembers on community.communityid=communitymembers.communityid where communitymembers.userid = ?");
            pstmt.setString(1, request.getParameter("userid"));
            rs = pstmt.executeQuery();
            rs.next();
            int count1 = rs.getInt("count");
            result = result.substring(1);
            pstmt = conn.prepareStatement("select maxcommunities from company where companyid= ?");
            pstmt.setString(1, companyid);
            rs = pstmt.executeQuery();
            rs.next();
            result = "{\"count\":" + count1 + ",\"maxcommunities\":" + rs.getInt("maxcommunities") + "," + result;
        } catch (SQLException e) {
            throw ServiceException.FAILURE("Admin.getAdminUserData", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return result;
    }

    public static String getAdminProjData(Connection conn, HttpServletRequest request, String companyid, String searchString)
            throws ServiceException {
        String result = "";
        DbResults res = null;
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObj = new JSONObject();
        ProjectDAO daoObj = new ProjectDAOImpl();
        HealthMeterDAO daoHM = new HealthMeterDAOImpl();
        try {
            Map<String, Object> pagingParams = new HashMap<String, Object>();
            if (!StringUtil.isNullOrEmpty(request.getParameter("start"))) {
                pagingParams.put("offset", Integer.parseInt(request.getParameter("start")));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("limit"))) {
                pagingParams.put("limit", Integer.parseInt(request.getParameter("limit")));
            }
            pagingParams.put("ss", searchString);
            String loginid = AuthHandler.getUserid(request);
            List<Project> projList = daoObj.getAllProjectByCompany(conn, companyid, pagingParams, false, false);
            int size = projList.size();
            int count = daoObj.getTotalCount();
            String holiday[] = SchedulingUtilities.getCompHolidays(conn, companyid);
            for (int i = 0; i < size; i++) {
                JSONObject temp = new JSONObject();
                Project project = projList.get(i);
                boolean moderator = false;
                ProjectMember pm = daoObj.getProjectMember(conn, project, loginid);
                if(pm != null){
                    if (pm.getStatus() == ProjectMemberStatus.MODERATOR.getCode()) {
                        moderator = true;
                    }
                }
                Map baseValues = daoHM.getBaseLineMeter(conn, project);
                String createdon = Timezone.toCompanyTimezone(conn, project.getCreatedOn("yyyy-MM-dd HH:mm:ss"), companyid);
                project.setCreatedOn(createdon, "yyyy-MM-dd HH:mm:ss");
                temp.put("moderator", moderator);
                temp.put("projectid", project.getProjectID());
                temp.put("projectname", project.getProjectName());
                temp.put("createdon", project.getCreatedOn("yyyy-MM-dd HH:mm:ss"));
                temp.put("image", project.getImage());
                temp.put("count", project.getMemberCount());
                temp.put("archived", project.isArchieved());
                temp.put("description", project.getDescription());
                temp.put("health", daoHM.getHealthMeter(conn, project.getProjectID(),holiday).getStatus(baseValues));
                ColumnSet rs = project.getRecordSet();
                while (rs.next()) {
                    temp.put(rs.getDataIndex(), rs.getObject()); //CColumn c = rs.getColumn();
                }
                jsonArray.put(temp);
            }
            //result = Utilities.listToGridJson(projList, count, Project.class);
            jsonObj.put("count", count);
            jsonObj.put("data", jsonArray);

        } catch (JSONException ex) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jsonObj.toString();
    }

//    public static String getAdminProjData(Connection conn, HttpServletRequest request, String companyid, String searchString)
//            throws ServiceException {
//        String result = null;
//        JSONObject res = new JSONObject();
//        try {
//            HealthMeterDAO daoObj = new HealthMeterDAOImpl();
//            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            java.util.Date d = new java.util.Date();
//            String loginid = AuthHandler.getUserid(request);
//            String resData = getProjData(conn, request, companyid, searchString);
//            JSONObject jobj = new JSONObject(resData);
//            JSONArray projArr = jobj.getJSONArray("data");
//            if (jobj.getInt("totalcount") > 0) {
//                for(int i = 0; i < projArr.length(); i++){
//                    JSONObject temp = projArr.getJSONObject(i);
//                    String createdon = Timezone.toCompanyTimezone(conn, temp.getString("createdon"), companyid);
////                    createdon = Timezone.dateTimeRenderer(conn, Timezone.toUserTimezone(conn, sdf.format(d), loginid), createdon, loginid);
//                    Map baseValue = daoObj.getBaseLineMeter(conn, temp.getString("projectid"));
//                    int status = daoObj.getHealthMeter(conn, temp.getString("projectid")).getStatus(baseValue);
//                    temp.put("health", status);
//                    temp.put("createdon", createdon);
//                    res.append("data", temp);
//    }
//                result = res.toString().substring(1);
//                result = "{\"totalcount\":" + Integer.toString(jobj.getInt("totalcount")) +
//                        ",\"count\":" + Integer.toString(jobj.getInt("count")) +
////                        ",\"maxprojects\":" + Integer.toString(jobj.getInt("maxprojects")) +
//                        /*",\"costperproject\":" + Integer.toString(jobj.getInt("costperproject")) + */"," + result;
//            } else
//                result = "{\"totalcount\":" + Integer.toString(jobj.getInt("totalcount")) +
//                        ",\"count\":" + Integer.toString(jobj.getInt("count")) +
////                        ",\"maxprojects\":" + Integer.toString(jobj.getInt("maxprojects")) +
//                        /*",\"costperproject\":" + Integer.toString(jobj.getInt("costperproject")) + */", data:{}}";
//        } catch (Exception e) {
//            throw ServiceException.FAILURE("Admin.getAdminUserData", e);
//        } finally {
//        }
//        return result;
//    }
    public static String getProjData(Connection conn, HttpServletRequest request, String companyid, String searchString) throws ServiceException {
        String result = null;
        int myCount = 0;
        int count1 = 0;
        int allprojCnt = 0;
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        KWLJsonConverter KWL = new KWLJsonConverter();
        String[] searchStrObj = new String[]{"project.projectname"};
        String myLikeString = StringUtil.getMySearchString(searchString, "and", searchStrObj);
        String PROJ_COUNT = "SELECT COUNT(*) as myCount FROM project " + " WHERE project.companyid=? and project.archived=0";
        String AllPROJ_COUNT = " SELECT COUNT(*) as allCount FROM project WHERE project.companyid = ? " + myLikeString;
        String GET_PROJ = "SELECT project.image,project.archived, project.projectid, project.projectname, project.description, project.createdon, "
                + "COUNT(projectmembers.projectid) AS count FROM project INNER JOIN projectmembers ON project.projectid=projectmembers.projectid "
                + "WHERE project.companyid=? AND inuseflag = 1 AND status >= 3" + myLikeString + " GROUP BY projectid ORDER BY count LIMIT ? OFFSET ? ";
        String GET_COUNT = "select count(*) as count from project inner join projectmembers on project.projectid=projectmembers.projectid where projectmembers.userid=? and inuseflag = 1;";

        try {
            pstmt = conn.prepareStatement(PROJ_COUNT);
            pstmt.setString(1, companyid);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                myCount = rs.getInt("myCount");
            }
            pstmt = conn.prepareStatement(AllPROJ_COUNT);
            pstmt.setString(1, companyid);
            StringUtil.insertParamSearchString(2, pstmt, searchString, searchStrObj.length);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                allprojCnt = rs.getInt("allCount");
            }
            if (allprojCnt > 0) {
                pstmt = conn.prepareStatement(GET_PROJ);
                pstmt.setString(1, companyid);
                int paramCnt = StringUtil.insertParamSearchString(2, pstmt, searchString, searchStrObj.length);
                pstmt.setInt(paramCnt++, Integer.parseInt(request.getParameter("limit")));
                pstmt.setInt(paramCnt++, Integer.parseInt(request.getParameter("start")));
                rs = pstmt.executeQuery();
                result = KWL.GetJsonForGrid(rs);
                pstmt.close();
                pstmt = conn.prepareStatement(GET_COUNT);
                pstmt.setString(1, request.getParameter("userid"));
                rs = pstmt.executeQuery();
                rs.next();
                count1 = rs.getInt("count");
                result = result.substring(1);
                result = "{\"totalcount\":" + allprojCnt + ",\"count\":" + myCount + "," + result;
            } else {
                result = "{\"totalcount\":" + allprojCnt + ",\"count\":" + myCount + ", data:[]}";
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("Admin.getAdminUserData", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return result;
    }

    public static String getCompanyDetails(Connection conn, HttpServletRequest request)
            throws ServiceException {
        String result = null;
        int notificationtype = 0;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ResultSet rs1 = null;
        JSONObject res = new JSONObject();
        try {
            pstmt = conn.prepareStatement("SELECT companyid,companyname,createdon,address,"
                    + "city,state,country,phone,fax,zip,timezone,website,activated,maxusers,"
                    + "maxprojects,image,featureaccess,planid,subscriptiondate,payerid,emailid,"
                    + "creator,modifiedon,isexpired,maxcommunities,currency,subdomain,"
                    + "notificationtype,milestonewidget,checklist,docaccess FROM company WHERE companyid = ?");
            pstmt.setString(1, request.getParameter("cid"));
            rs = pstmt.executeQuery();
            while (rs.next()) {
                JSONObject temp = new JSONObject();
                temp.put("companyid", rs.getString("companyid"));
                temp.put("companyname", rs.getObject("companyname"));
                temp.put("createdon", rs.getObject("createdon").toString());
                temp.put("address", rs.getObject("address"));
                temp.put("city", rs.getObject("city"));
                temp.put("state", rs.getObject("state"));
                String country = getCmpCountry(conn, rs.getString("country"));
                temp.put("country", country);
                temp.put("phone", rs.getObject("phone"));
                temp.put("fax", rs.getObject("fax"));
                temp.put("zip", rs.getObject("zip"));
                String tz = getCmpTz(conn, rs.getString("timezone"));
                temp.put("timezone", tz);
                temp.put("website", rs.getObject("website"));
                temp.put("activated", rs.getObject("activated"));
                temp.put("image", rs.getObject("image"));
                temp.put("featureaccess", rs.getObject("featureaccess"));
                temp.put("planid", rs.getObject("planid"));
                temp.put("subscriptiondate", rs.getObject("subscriptiondate"));
                temp.put("payerid", rs.getObject("payerid"));
                temp.put("emailid", rs.getObject("emailid"));
                temp.put("creator", rs.getObject("creator"));
                temp.put("modifiedon", rs.getObject("modifiedon"));
                temp.put("isexpired", rs.getObject("isexpired"));
                temp.put("maxcommunities", rs.getObject("maxcommunities"));
                temp.put("milestonewidget", rs.getObject("milestonewidget"));
                temp.put("checklist", rs.getObject("checklist"));
                temp.put("docaccess", rs.getObject("docaccess"));
                String curr = getCmpCurr(conn, rs.getString("currency"));
                temp.put("currency", curr);
                temp.put("subdomain", rs.getObject("subdomain"));
                notificationtype = rs.getInt("notificationtype");
                DbResults r = DbUtil.executeQuery(conn, "SELECT o_diff, p_diff FROM pertdefaults_company WHERE companyid = ?", request.getParameter("cid"));
                if (r.next()) {
                    temp.put("optimisticdiff", r.getInt("o_diff"));
                    temp.put("pessimisticdiff", r.getInt("p_diff"));
                }
                //      temp.put("notificationduration", rs.getObject("notificationduration"));
                res.append("data", temp);
            }
            pstmt = conn.prepareStatement("SELECT typeid from notificationlist");
            rs1 = pstmt.executeQuery();
            JSONObject alltype = new JSONObject();
            while (rs1.next()) {
                JSONObject temp = new JSONObject();
                int tempInt = rs1.getInt("typeid");
                temp.put("typeid", tempInt);
                int actid = (int) Math.pow(2, rs1.getInt("typeid"));
                if ((notificationtype & actid) == actid) {
                    temp.put("permission", true);
                } else {
                    temp.put("permission", false);
                }
                alltype.append("data", temp);
            }
            res.append("notification", alltype);
            result = res.toString();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("Admin.getCompanyDetails", e);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Admin.getCompanyDetails", ex);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return result;
    }

    public static String getCmpCurr(Connection conn, String curr)
            throws ServiceException {
        String retstr = "";
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("select currencyname, htmlcode from currency where currencyid = ?");
            pstmt.setString(1, curr);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                retstr = rs.getString("currencyname") + " - " + rs.getString("htmlcode");
            }
            rs.close();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("AdminHandler.getCmpCurr: " + e.getMessage(), e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return retstr;
    }

    public static String getCmpTz(Connection conn, String tz)
            throws ServiceException {
        String retstr = "";
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("select name from timezone where id = ?");
            pstmt.setString(1, tz);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                retstr = rs.getString("name");
            }
            rs.close();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("AdminHandler.getCmpTz: " + e.getMessage(), e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return retstr;
    }

    public static String getCmpCountry(Connection conn, String cnt)
            throws ServiceException {
        String retstr = "";
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("select countryname from country where countryid = ?");
            pstmt.setString(1, cnt);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                retstr = rs.getString("countryname");
            }
            rs.close();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("AdminHandler.getCmpCountry: " + e.getMessage(), e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return retstr;
    }

    public static String getUserPermissions(Connection conn, HttpServletRequest request)
            throws ServiceException {
        String result = null;
        PreparedStatement pstmt = null;
        KWLJsonConverter KWL = new KWLJsonConverter();
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement("SELECT * FROM userpermissions WHERE userid = ?;");
            pstmt.setString(1, request.getParameter("uid"));
            rs = pstmt.executeQuery();
            result = KWL.GetJsonForGrid(rs);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("Admin.getUserPermission", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return result;
    }

    public static String getPermissionSet(Connection conn, HttpServletRequest request)
            throws ServiceException {
        String result = "";

        if (!StringUtil.isNullOrEmpty(request.getParameter("uid"))) {
            String uid = request.getParameter("uid");
            String[] uids;
            if (uid.contains(",")) {
                uids = uid.split(",");
            } else {
                uids = new String[1];
                uids[0] = uid;
            }
            for (String u : uids) {
                if (isCreator(conn, u)) {
                    String creatorFName = new UserDAOImpl().getUser(conn, u).getFullName();
                    result = "{\"error\":\"creator\",\"data\":\"" + MessageSourceProxy.getMessage("pm.permission.msg.creatorperm", new Object[]{creatorFName}, request) + "\"}";
                    break;
                }
            }
            if (StringUtil.isNullOrEmpty(result)) {
                PermissionManager pm = new PermissionManager();
                List<Feature> features = pm.getFeaturePermissionsForUser(conn, uid);
                result = Utilities.listToGridJson(features, features.size(), Feature.class);
            }
        }
        return result;
    }

    public static String getCommunitiesList(Connection conn,
            HttpServletRequest request, String companyid)
            throws ServiceException {
        String result = null;
        PreparedStatement pstmt = null;
        KWLJsonConverter KWL = new KWLJsonConverter();
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement("SELECT community.communityid AS id, community.communityname AS name FROM community WHERE companyid=?;");
            pstmt.setString(1, companyid);
            rs = pstmt.executeQuery();
            result = KWL.GetJsonForGrid(rs);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("Admin.getCommunitiesList", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return result;
    }

    public static String getProjectsList(Connection conn, HttpServletRequest request, String companyid)
            throws ServiceException, SessionExpiredException {
        String result = null;
        PreparedStatement pstmt = null;
        String userid = "";
        if (!StringUtil.isNullOrEmpty(request.getParameter("userid"))) {
            userid = (request.getParameter("userid").contains(",")) ? "" : request.getParameter("userid");
        }
        String loginID = AuthHandler.getUserid(request);
        KWLJsonConverter KWL = new KWLJsonConverter();
        ResultSet rs = null;
        PermissionManager pm = new PermissionManager();
        int featureID = PermissionConstants.Feature.PROJECT_ADMINISTRATION;
        int activityID = PermissionConstants.Activity.PROJECT_MANAGE_MEMBER;
        int subActivityID = PermissionConstants.SubActivity.PROJECT_MANAGE_MEMBER_ALL_PROJECTS;
        boolean flag = pm.isPermission(conn, loginID, featureID, activityID, subActivityID);
        try {
            if(flag){
                pstmt = conn.prepareStatement("SELECT project.projectid AS id, project.projectname AS name "
                        + "FROM project WHERE companyid=? and archived = 0 and project.projectid "
                        + "NOT IN (select projectid from projectmembers where userid = ? and inuseflag = 1);");
                pstmt.setString(1, companyid);
                pstmt.setString(2, userid);
            } else {
                pstmt = conn.prepareStatement("SELECT project.projectid AS id, project.projectname AS name "
                        + "FROM project WHERE companyid=? and archived = 0 "
                        + "AND project.projectid IN (select projectid from projectmembers where userid = ? and inuseflag = 1 and status = ? "
                        + "AND projectid NOT IN (select projectid from projectmembers where userid = ? and inuseflag = 1));");
                pstmt.setString(1, companyid);
                pstmt.setString(2, loginID);
                pstmt.setInt(3, ProjectMemberStatus.MODERATOR.getCode());
                pstmt.setString(4, userid);
            }
            rs = pstmt.executeQuery();
            result = KWL.GetJsonForGrid(rs);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("Admin.getProjectsList", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return result;
    }

    public static String addUsersTo(Connection conn, HttpServletRequest request)
            throws ServiceException, SessionExpiredException {
        String result = "success";
        String[] featureidslist = request.getParameter("featureidslist").split(",");
        String[] userslist = request.getParameter("userslist").split(",");
        String featureid = request.getParameter("feature");
        result = addUsersOntoProjects(conn, request, featureidslist, userslist, featureid);
        return result;
    }

    public static String addUsersOntoProjects(Connection conn, HttpServletRequest request, String[] featureidslist, String[] userslist, String featureid)
            throws SessionExpiredException, ServiceException {
        DbResults rs = null;
        String chkquery = null;
        String insertquery = null;
        String updatequery = null;
        String insertRes = null;
        String subjectActive = "";
        String projName = "";
        String userName = "";
        String result = "success";
        String mailFooter = KWLErrorMsgs.mailSystemFooter;
        String msgActiveString = "";
        String ipAddress = AuthHandler.getIPAddress(request);
        String loginid = AuthHandler.getUserid(request);
        String companyid = AuthHandler.getCompanyid(request);
        String loginName = AuthHandler.getAuthor(conn, loginid) + " ("
                + AuthHandler.getUserName(request) + "), ";
        int auditMode = 0;
        int cntUpdate = 0;
        if (featureid.equals("1")) {
            chkquery = "SELECT status FROM communitymembers WHERE communityid=? AND userid=?;";
            insertquery = "INSERT INTO communitymembers (communityid, userid, status, updatedon) VALUES (?,?,3,now());";
        } else {
            chkquery = "SELECT status, inuseflag FROM projectmembers WHERE projectid=? AND userid=?;";
            updatequery = "UPDATE projectmembers SET status=3, inuseflag=1 WHERE projectid=? and userid=?";
            insertquery = "INSERT INTO projectmembers (projectid, userid, status) VALUES (?,?,3);";
            insertRes = "INSERT INTO proj_resources (resourceid, resourcename, projid) VALUES (?,?,?)";
        }
        try {
            for (int cnt1 = 0; cnt1 < featureidslist.length; cnt1++) {
                for (int cnt2 = 0; cnt2 < userslist.length; cnt2++) {
                    projName = "";
                    userName = "";
                    cntUpdate = 0;
                    Object[] obj = new Object[]{featureidslist[cnt1], userslist[cnt2]};
                    rs = DbUtil.executeQuery(conn, chkquery, obj);
                    if (rs.next()) {
                        if (!rs.getBoolean("inuseflag") || rs.getInt("status") < 3) {
                            cntUpdate = DbUtil.executeUpdate(conn, updatequery, obj);
                            DbResults dbr = DbUtil.executeQuery(conn, "SELECT projectname FROM project WHERE projectid = ?", new Object[]{featureidslist[cnt1]});

                            if (dbr.next()) {
                                projName = dbr.getString("projectname");
                                DbResults r = DbUtil.executeQuery(conn, "SELECT username FROM userlogin WHERE userid=?", new Object[]{userslist[cnt2]});
                                if (r.next()) {
                                    userName = r.getString("username");
                                    subjectActive = "[" + projName + "] Access to the project activated.";
                                    msgActiveString = "Your access to the project : " + projName + " has been activated." + mailFooter;
                                    Mail.insertMailMsg(conn, r.getString("username"), loginid, subjectActive, msgActiveString, "1", false, "1", "", "newmsg", "", 3, "", companyid);
                                }
                            }
                        }
                    } else {
                        DbUtil.executeUpdate(conn, insertquery, obj);
                        if (featureid.equals("0")) {
                            DbResults r = DbUtil.executeQuery(conn, "SELECT username FROM userlogin WHERE userid=?", new Object[]{userslist[cnt2]});
                            if (r.next()) {
                                userName = r.getString("username");
                                cntUpdate = DbUtil.executeUpdate(conn, insertRes, new Object[]{userslist[cnt2], userName, featureidslist[cnt1]});
                                DbResults dbr = DbUtil.executeQuery(conn, "SELECT projectname FROM project WHERE projectid = ?", new Object[]{featureidslist[cnt1]});

                                if (dbr.next()) {
                                    projName = dbr.getString("projectname");
                                    subjectActive = "[" + projName + "] Access to the project activated.";
                                    msgActiveString = "Your access to the project : " + projName + " has been activated." + mailFooter;
                                    Mail.insertMailMsg(conn, userName, loginid, subjectActive, msgActiveString, "1", false, "1", "", "newmsg", "", 3, "", companyid);
                                }
                            }
                        }
                    }

                    if (cntUpdate > 0) {
                        String userFullName = AuthHandler.getAuthor(conn, userslist[cnt2])
                                + " (" + userName + "), " + projName;
                        String params = loginName + userFullName;

                        AuditTrail.insertLog(conn, "316", loginid, userslist[cnt2], featureidslist[cnt1], companyid,
                                params, ipAddress, auditMode);
                    }
                }
            }

        } catch (ParseException ex) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE,
                    "Parse Exception While Adding Users To Project", ex);
            result = "failure";
            throw ServiceException.FAILURE("Admin.addUsersTo", ex);

        } catch (JSONException ex) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE,
                    "JSON Exception While Adding Users To Project", ex);
            result = "failure";
            throw ServiceException.FAILURE("Admin.addUsersTo", ex);

        } catch (ServiceException e) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE,
                    "SQL Exception While Adding Users To Project", e);
            result = "failure";
            throw ServiceException.FAILURE("Admin.addUsersTo", e);
        }
        return result;
    }

    public static String assignUserPermissions(Connection conn, HttpServletRequest request) throws ServiceException {
        String result = "success";
        String[] buf = null;
        try {
            Map arrParam = null;
            arrParam = request.getParameterMap();
            String[] selectedUsers = request.getParameter("users").split(",");

            int auditMode = 0;
            String ipAddress = AuthHandler.getIPAddress(request);
            String loginid = AuthHandler.getUserid(request);
            String companyid = AuthHandler.getCompanyid(request);

            PermissionManager pm = new PermissionManager();
            List<String> permissionNames = new ArrayList<String>();
            Set<String> keys= arrParam.keySet();
            Iterator itr = keys.iterator();
            while(itr.hasNext()){
                permissionNames.add(itr.next().toString());
            }
            List<Feature> features = pm.updateFeatures(conn, permissionNames);
            for (int i = 0; i < selectedUsers.length; i++) {
                if (isCreator(conn, selectedUsers[i])) {
                    java.lang.Throwable cause = new java.lang.Throwable();
                    throw ServiceException.FAILURE("1", cause);
                }
                int row = pm.setUserPermissions(conn, features, selectedUsers[i]);
                String selUserName = AuthHandler.getAuthor(conn, selectedUsers[i]) + " (" + AuthHandler.getUserName(conn, selectedUsers[i]) + ")";
                String author = AuthHandler.getAuthor(conn, loginid) + " (" + AuthHandler.getUserName(request) + "), ";
                String params = author + selUserName;
                AuditTrail.insertLog(conn, "314", loginid, loginid, "", companyid, params, ipAddress, auditMode);
            }
            conn.commit();

        } catch (SessionExpiredException ex) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE,
                    "Session Expired Exception While Assigning Permissions To Users", ex);
            result = ex.getMessage();
            conn.rollback();

        } catch (ServiceException e) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE,
                    "Service Exception While Assigning Permissions To Users", e);

            if (StringUtil.equal(e.getMessage(), "system failure: 1")) {
                result = "Can not change the COMPANY CREATOR'S permissions";
            }
            conn.rollback();
        }
        return result;
    }

    public static boolean isCreator(Connection conn, String uid) throws ServiceException {
        boolean res = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement("SELECT creator FROM company WHERE companyid = (SELECT companyid FROM users WHERE userid = ?)");
            pstmt.setString(1, uid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                if (rs.getString("creator").compareTo(uid) == 0) {
                    res = true;
//                    java.lang.Throwable cause = new java.lang.Throwable();
//                    throw ServiceException.FAILURE("Can not change the PROJECT CREATOR'S permissions", cause);
                }
            }
        } catch (Exception ex) {
//            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return res;
    }

    public static String getCommunityMemberDetails(Connection conn,
            HttpServletRequest request, String companyid)
            throws ServiceException {
        PreparedStatement pstmt = null;
        String jsonstringgrid = null;
        ResultSet rs = null;
        String communityId = request.getParameter("featureid").toString();
        String status = request.getParameter("status").toString();
        String start = request.getParameter("start").toString();
        String limit = request.getParameter("limit").toString();
        try {

            if (status.matches("3")) {
                pstmt = conn.prepareStatement("select users.userid as id,userlogin.username as username,image as img,fname as Name,emailid as email, communitymembers.status as status from users inner join userlogin on users.userid=userlogin.userid inner join communitymembers on users.userid = communitymembers.userid where communityid = ?  and status >= 3 ORDER BY id LIMIT ? OFFSET ?;");
                pstmt.setString(1, communityId);
                pstmt.setInt(2, Integer.parseInt(limit));
                pstmt.setInt(3, Integer.parseInt(start));
            } else if (status.matches("4")) {
                pstmt = conn.prepareStatement("select users.userid as id,userlogin.username as username,image as img,fname as Name,emailid as email from users inner join userlogin on users.userid=userlogin.userid where companyid = ? and users.userid not in (select users.userid from users inner join communitymembers on users.userid = communitymembers.userid where communityid = ? and companyid = ? and status IN (1,2,3,4,5)) ORDER BY id LIMIT ? OFFSET ?;");
                pstmt.setString(1, companyid);
                pstmt.setString(2, communityId);
                pstmt.setString(3, companyid);
                pstmt.setInt(4, Integer.parseInt(limit));
                pstmt.setInt(5, Integer.parseInt(start));
            } else {
                pstmt = conn.prepareStatement("select users.userid as id,userlogin.username as username,image as img,fname as Name,emailid as email, communitymembers.status as status from users inner join userlogin on users.userid = userlogin.userid inner join communitymembers on users.userid = communitymembers.userid where communityid = ?  and status = ? ORDER BY id LIMIT ? OFFSET ?;");
                pstmt.setString(1, communityId);
                pstmt.setInt(2, Integer.parseInt(status));
                pstmt.setInt(3, Integer.parseInt(limit));
                pstmt.setInt(4, Integer.parseInt(start));
            }
            rs = pstmt.executeQuery();
            KWLJsonConverter KWLJson = new KWLJsonConverter();
            jsonstringgrid = KWLJson.GetJsonForGrid(rs);
            rs.close();

        } catch (SQLException e) {
            throw ServiceException.FAILURE("Admin.getactionlog", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return jsonstringgrid;
    }

    public static String getProjectMemberDetails(Connection conn,
            HttpServletRequest request, String companyid, String searchString)
            throws ServiceException {
        PreparedStatement pstmt = null;
        int count = 0;
        String jsonstringgrid = "{\"count\":";
        ResultSet rs = null;
        String projId = request.getParameter("featureid").toString();
        String status = request.getParameter("status").toString();
        String start = request.getParameter("start").toString();
        String limit = request.getParameter("limit").toString();
        try {

            if (status.matches("3")) {

                String GET_COUNT3 = " select COUNT(*) as count from users inner join projectmembers on "
                        + " users.userid = projectmembers.userid inner join userlogin on users.userid = userlogin.userid where projectid = ? and status in (0,3,4,5) and userlogin.isactive = 1";

                String GET_USERS3 = " select users.userid as id, "
                        + " image as img, concat(fname,' ',lname) as Name, emailid as email, projectmembers.status as status, "
                        + " (case projectmembers.inuseflag when '1' then 'Active' else 'Inactive' end) as inuse,projectmembers.planpermission "
                        + " from users inner join projectmembers on users.userid = projectmembers.userid "
                        + " inner join userlogin on users.userid = userlogin.userid where projectid = ? "
                        + " and status in (0,3,4,5) and userlogin.isactive = 1";
                String[] searchStrObj = new String[]{"concat(users.fname,' ', users.lname)", "userlogin.username"};
                String myLikeString = StringUtil.getMySearchString(searchString, "and", searchStrObj);

                pstmt = conn.prepareStatement(GET_COUNT3 + myLikeString);
                pstmt.setString(1, projId);
                StringUtil.insertParamSearchString(2, pstmt, searchString, searchStrObj.length);
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    count = rs.getInt("count");
                }
                pstmt = conn.prepareStatement(GET_USERS3 + myLikeString + " LIMIT ? OFFSET ? ");
                pstmt.setString(1, projId);
                int paramCnt = StringUtil.insertParamSearchString(2, pstmt, searchString, searchStrObj.length);
                pstmt.setInt(paramCnt++, Integer.parseInt(limit));
                pstmt.setInt(paramCnt++, Integer.parseInt(start));
            } else if (status.matches("4")) {

                String GET_COUNT4 = " SELECT COUNT(users.userid) as count from users inner join userlogin on users.userid = userlogin.userid where userlogin.isactive = true and companyid = ? and users.userid "
                        + " not in (select users.userid from users inner join projectmembers on users.userid = projectmembers.userid "
                        + " where projectid = ? and companyid = ? and status IN (1,2,3,4,5) and inuseflag = 1 ) ";

                String GET_USER4 = " select users.userid as id,image as img, concat(fname,' ',lname) as Name, emailid as email "
                        + " from users inner join userlogin on users.userid = userlogin.userid where userlogin.isactive = true and companyid = ? and users.userid not in (select users.userid from users "
                        + " inner join projectmembers on users.userid = projectmembers.userid where projectid = ? and companyid = ? "
                        + " and status IN (1,2,3,4,5) and inuseflag = 1) ORDER BY id LIMIT ? OFFSET ? ";

                pstmt = conn.prepareStatement(GET_COUNT4);
                pstmt.setString(1, companyid);
                pstmt.setString(2, projId);
                pstmt.setString(3, companyid);
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    count = rs.getInt("count");
                }
                pstmt = conn.prepareStatement(GET_USER4);
                pstmt.setString(1, companyid);
                pstmt.setString(2, projId);
                pstmt.setString(3, companyid);
                pstmt.setInt(4, Integer.parseInt(limit));
                pstmt.setInt(5, Integer.parseInt(start));
            } else if (status.matches("2") || status.matches("1")) {

                String GET_COUNT12 = " SELECT COUNT(users.userid) AS count FROM users INNER JOIN projectmembers "
                        + " ON users.userid = projectmembers.userid inner join userlogin on users.userid = userlogin.userid where userlogin.isactive = true and projectid = ? AND status = ? AND inuseflag = 1 ";

                String GET_USER12 = " select users.userid as id,image as img, concat(fname,' ',lname) as Name, "
                        + " emailid as email, projectmembers.status as status, projectmembers.planpermission from users inner join projectmembers on "
                        + " users.userid = projectmembers.userid inner join userlogin on users.userid = userlogin.userid where userlogin.isactive = true and projectid = ?  and status = ?";
                String[] searchStrObj = new String[]{"concat(users.fname,' ', users.lname)", "userlogin.username"};
                String myLikeString = StringUtil.getMySearchString(searchString, "and", searchStrObj);
                pstmt = conn.prepareStatement(GET_COUNT12 + myLikeString);
                pstmt.setString(1, projId);
                pstmt.setInt(2, Integer.parseInt(status));
                StringUtil.insertParamSearchString(3, pstmt, searchString, searchStrObj.length);
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    count = rs.getInt("count");
                }
                pstmt = conn.prepareStatement(GET_USER12 + myLikeString + " LIMIT ? OFFSET ? ");
                pstmt.setString(1, projId);
                pstmt.setInt(2, Integer.parseInt(status));
                int paramCnt = StringUtil.insertParamSearchString(3, pstmt, searchString, searchStrObj.length);
                pstmt.setInt(paramCnt++, Integer.parseInt(limit));
                pstmt.setInt(paramCnt++, Integer.parseInt(start));
            } else {
                String GET_COUNT_ELSE = " SELECT COUNT(users.userid) AS count FROM users INNER JOIN projectmembers "
                        + " ON users.userid = projectmembers.userid inner join userlogin on users.userid = userlogin.userid where userlogin.isactive = true and projectid = ? AND status = ? "
                        + " AND inuseflag = 1";

                String GET_USER_ELSE = " select users.userid as id,userlogin.username as username,image as img,fname as Name, "
                        + " emailid as email, projectmembers.status as status from users inner join projectmembers "
                        + " on users.userid = projectmembers.userid inner join userlogin on users.userid = userlogin.userid where userlogin.isactive = true and projectid = ?  and status = ? and inuseflag = 1 "
                        + " ORDER BY id LIMIT ? OFFSET ? ";

                pstmt = conn.prepareStatement(GET_COUNT_ELSE);
                pstmt.setString(1, projId);
                pstmt.setInt(2, Integer.parseInt(status));
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    count = rs.getInt("count");
                }
                pstmt = conn.prepareStatement(GET_USER_ELSE);
                pstmt.setString(1, projId);
                pstmt.setInt(2, Integer.parseInt(status));
                pstmt.setInt(3, Integer.parseInt(limit));
                pstmt.setInt(4, Integer.parseInt(start));
            }
            jsonstringgrid += Integer.toString(count);
            rs = pstmt.executeQuery();
            KWLJsonConverter KWLJson = new KWLJsonConverter();
            String temp = KWLJson.GetJsonForGrid(rs);
            if (temp.equals("{data:{}}")) {
                jsonstringgrid += "," + "data:[]}";
            } else {
                jsonstringgrid += "," + temp.substring(1);
            }
            jsonstringgrid = ProfileHandler.getAppsImageInJSON(jsonstringgrid, "id", "img", 35);
            rs.close();
        } catch (JSONException e) {
            throw ServiceException.FAILURE("Admin.getactionlog", e);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("Admin.getactionlog", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return jsonstringgrid;
    }

    public static String createAnnouncementsForUser(Connection conn,
            HttpServletRequest request, String companyid, String loginid)
            throws ServiceException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int index = -1;
        String announcement = StringUtil.serverHTMLStripper(request.getParameter("announcement").toString());
        String fromdate = StringUtil.serverHTMLStripper(request.getParameter("fromdate").toString());
        String todate = StringUtil.serverHTMLStripper(request.getParameter("todate").toString());
        companyid = StringUtil.serverHTMLStripper(companyid);
        if (StringUtil.isNullOrEmpty(announcement) || StringUtil.isNullOrEmpty(fromdate) || StringUtil.isNullOrEmpty(todate) || StringUtil.isNullOrEmpty(companyid)) {
            return KWLErrorMsgs.errProccessingData;
        }
        String buf = StringUtil.serverHTMLStripper(request.getParameter("featureid").toString());
        if (StringUtil.isNullOrEmpty(buf)) {
            buf = companyid;
        }
        String[] userid = buf.split(",");
        try {
//            fromdate = Timezone.toCompanyTimezone(conn, fromdate,companyid);
//            todate = Timezone.toCompanyTimezone(conn, todate,companyid);
            pstmt = conn.prepareStatement("INSERT INTO announcements (announceval, `from`, `to`) VALUES (?, ?, ?)");
            pstmt.setString(1, announcement);
            pstmt.setString(2, fromdate);
            pstmt.setString(3, todate);
            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();
            rs.next();
            index = rs.getInt(1);

            if (index != -1) {
                for (int cnt = 0; cnt < userid.length; cnt++) {
                    pstmt = conn.prepareStatement("INSERT INTO userannouncements (userid, announceid, companyid) VALUES (?,?,?)");
                    pstmt.setString(1, userid[cnt]);
                    pstmt.setInt(2, index);
                    pstmt.setString(3, companyid);
                    pstmt.executeUpdate();
                }
            }

        } catch (SQLException e) {
            throw ServiceException.FAILURE("Admin.createAnnouncementsForUser", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return "true";
    }

    public static String createSingleProject(Connection conn, String ProjectName, String companyid, String userid)
            throws ServiceException {
        String status = "";
        String projectid = UUID.randomUUID().toString();
        DbResults rs = DbUtil.executeQuery("select count(projectid) from project where companyid =?", companyid);
        int noProjects = 0;
        int maxProjects = 0;
        if (rs.next()) {
            noProjects = rs.getInt(1);
        }
        rs = DbUtil.executeQuery("select maxprojects from company where companyid =?", companyid);
        if (rs.next()) {
            maxProjects = rs.getInt(1);
        }
        if (noProjects == maxProjects) {
            return "The maximum limit for projects for this company has already reached";
        }

        try {

            String projName = ProjectName.toString().replaceAll("[^\\w|\\s|'|\\-|\\[|\\]|\\(|\\)]", "").trim();
            String nickName = makeNickName(conn, projName, 1);
            DbUtil.executeUpdate(
                    conn,
                    "INSERT INTO project (projectid,projectname,description,image,companyid, nickname) VALUES (?,?,?,?,?,?)",
                    new Object[]{projectid,
                        projName,
                        "", "",
                        companyid, nickName});
            com.krawler.esp.handlers.Forum.setStatusProject(conn, userid, projectid, 4, 0, "", companyid);
            status = "success";
//                        conn.commit();
        } catch (ServiceException e) {
            status = "failure";
            throw ServiceException.FAILURE("KickStart.CreateSingleProject", e);
        } finally {
//                    DbPool.quietClose(conn);
        }
        return projectid;
    }

    public static String getCompanyUsers(Connection conn, String companyid) throws ServiceException {
        ResultSet rs = null;
        String str = null;
        PreparedStatement pstmt = null;
        pstmt = conn.prepareStatement("select users.userid, userlogin.username, users.emailid, concat_ws(' ',users.fname,users.lname) as name, "
                + "users.fname as firstName, users.lname as lastName from users "
                + "inner join userlogin on userlogin.userid = users.userid where users.companyid = ? and userlogin.isactive = true");
        try {
            pstmt.setString(1, companyid);
            rs = pstmt.executeQuery();
            KWLJsonConverter KWL = new KWLJsonConverter();
            str = KWL.GetJsonForGrid(rs).toString();
            rs.close();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("AdminHandler.getCompanyUsers", e);
        }
        return str;
    }

    public static String getCountryList(Connection conn) throws ServiceException {
        ResultSet rs = null;
        String str = null;
        PreparedStatement pstmt = null;
        pstmt = conn.prepareStatement("select countryid as id ,countryname as name from country");
        try {
            rs = pstmt.executeQuery();
            KWLJsonConverter KWL = new KWLJsonConverter();
            str = KWL.GetJsonForGrid(rs).toString();
            rs.close();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("AdminHandler.getCountryList", e);
        }
        return str;
    }

    public static String getTimeZoneList(Connection conn) throws ServiceException {
        ResultSet rs = null;
        String str = null;
        PreparedStatement pstmt = null;
        pstmt = conn.prepareStatement("select id,name,difference from timezone order by sortorder");
        try {
            rs = pstmt.executeQuery();
            KWLJsonConverter KWL = new KWLJsonConverter();
            str = KWL.GetJsonForGrid(rs).toString();
            rs.close();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("AdminHandler.getTimezoneList", e);
        }
        return str;
    }

    public static String getModuleSubscription(Connection conn, String companyid) {
        String ret = "";
        try {
            PreparedStatement p = conn.prepareStatement("SELECT moduleid, modulename, nickname FROM companymodules");
            ResultSet r = p.executeQuery();
            JSONObject robj = new JSONObject();
            while (r.next()) {
                String status = "";
                String moduleName = r.getString("modulename");
                String nickName = r.getString("nickname");
                int moduleId = r.getInt("moduleid");

                p = conn.prepareStatement(" SELECT status FROM newsubscriptionrequest "
                        + " WHERE companyid = ? AND moduleid = ? AND submoduleid = 0 ");
                p.setString(1, companyid);
                p.setInt(2, moduleId);
                ResultSet tr = p.executeQuery();
                if (tr.next()) {
                    status = "Request.Pending";
                } else if (DashboardHandler.isSubscribed(conn, companyid, r.getString("nickname"))) {
                    status = "Subscribed";
                } else {
                    status = "Unsubscribed";
                }

                JSONObject temp = new JSONObject();
                temp.put("modulename", moduleName);
                temp.put("status", status);
                temp.put("parentmod", "");
                temp.put("modulenickname", nickName);
                robj.append("data", temp);

                p = conn.prepareStatement(" SELECT submodulename, submoduleid, subnickname "
                        + " FROM companysubmodules where parentmoduleid = ? ");
                p.setInt(1, moduleId);
                ResultSet rsSub = p.executeQuery();
                while (rsSub.next()) {
                    status = "";
                    int subModuleId = rsSub.getInt("submoduleid");
                    p = conn.prepareStatement(" SELECT status FROM newsubscriptionrequest "
                            + " WHERE companyid = ? AND moduleid = ? AND submoduleid = ? ");
                    p.setString(1, companyid);
                    p.setInt(2, moduleId);
                    p.setInt(3, subModuleId);
                    ResultSet rsSubReq = p.executeQuery();
                    if (rsSubReq.next()) {
                        status = "Request.Pending";

                    } else if (DashboardHandler.isSubscribed(conn, companyid, rsSub.getString("subnickname"))) {
                        status = "Subscribed";

                    } else {
                        status = "Unsubscribed";
                    }

                    temp = new JSONObject();
                    temp.put("modulename", rsSub.getString("submodulename"));
                    temp.put("status", status);
                    temp.put("parentmod", nickName);
                    temp.put("modulenickname", rsSub.getString("subnickname"));
                    robj.append("data", temp);
                }
            }
            ret = robj.toString();
        } catch (ServiceException e) {
        } catch (SQLException e) {
        } catch (JSONException e) {
        }
        return ret;
    }

    public static String requestSubscription(Connection conn, HttpServletRequest request, String companyid) {
        String ret = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
        SimpleDateFormat sdfDateTime = new SimpleDateFormat("MMMMM dd, yyyy 'at' hh:mm aaa");
        try {
            int status = Integer.parseInt(request.getParameter("status"));
            String module = request.getParameter("module");
            PreparedStatement p = conn.prepareStatement("SELECT moduleid FROM companymodules WHERE modulename = ?");
            p.setString(1, module);
            ResultSet r = p.executeQuery();
            int modid = 0;
            if (r.next()) {
                modid = r.getInt("moduleid");
            }
            if (modid != 0) {
                p = conn.prepareStatement("SELECT count(*) AS count FROM newsubscriptionrequest WHERE companyid = ? AND moduleid = ?");
                p.setString(1, companyid);
                p.setInt(2, modid);
                r = p.executeQuery();
                int cnt = 0;
                if (r.next()) {
                    cnt = r.getInt("count");
                }
                if (cnt == 0) {
                    p = conn.prepareStatement("INSERT INTO newsubscriptionrequest (companyid, status, moduleid, submoduleid, requestuserid, requesttime) VALUES (?, ?, ?, ?, ?, now())");
                    p.setString(1, companyid);
                    p.setInt(2, status);
                    p.setInt(3, modid);
                    p.setInt(4, 0);
                    p.setString(5, request.getParameter("userid"));
                    cnt = p.executeUpdate();
                    if (cnt == 1) {
                        ret = "Request sent successfully";

                        PreparedStatement pstmt = conn.prepareStatement(" SELECT status, companymodules.moduleid, "
                                + " company.companyid AS cid, companyname, modulename, requesttime, company.emailid, company.subdomain "
                                + " FROM newsubscriptionrequest INNER JOIN companymodules "
                                + " ON newsubscriptionrequest.moduleid = companymodules.moduleid "
                                + " INNER JOIN company ON newsubscriptionrequest.companyid = company.companyid ");

                        ResultSet rs = pstmt.executeQuery();

                        while (rs.next()) {
                            String stat = "SUBSCRIPTION";
                            String auditID = "334";
                            String companyname = rs.getString("companyname");
                            String subdomain = rs.getString("subdomain");
                            String modulename = rs.getString("modulename");

                            if (rs.getInt("status") == 0) {
                                stat = "UNSUBSCRIPTION";
                                auditID = "335";
                            }

//                            String requestTime = Timezone.toUserTimezone(conn, rs.getString("requesttime"), AuthHandler.getUserid(request));
                            String requestTime = rs.getString("requesttime");
                            Calendar onCal = Calendar.getInstance();

                            onCal.setTime(sdf.parse(requestTime));

                            String onString = sdfDateTime.format(onCal.getTime());

                            String infoHTML = " <HTML><HEAD><TITLE>[Deskera] " + stat + " request</TITLE></HEAD> "
                                    + " <style type='text/css'>a:link, a:visited, a:active {color: #03C;} "
                                    + " body {font-family: Arial, Helvetica, sans-serif;color: #000;font-size: 13px;}</style> "
                                    + " <BODY> <div><p>Hi,</p> "
                                    + companyname + "(" + subdomain + ")"
                                    + " has requested for " + stat + " of " + modulename
                                    + " on " + onString + "."
                                    + " <br/><br/><p>See you back on Deskera!</p><p> - The Deskera Team</p> "
                                    + " </div> </BODY></HTML> ";

                            String infoString = "Hi,\n\n"
                                    + companyname + "(" + subdomain + ") has requested for "
                                    + stat + " of " + modulename + " on " + onString + "."
                                    + " \n\n See you back on Deskera! \n\n - The Deskera Team ";

                            String emailidCreator = rs.getString("emailid");
                            String pmsg = infoString;
                            String htmlmsg = infoHTML;

                            try {
                                SendMailHandler.postMail(new String[]{KWLErrorMsgs.adminEmailId},
                                        "[Deskera] " + stat + " request", htmlmsg, pmsg, emailidCreator);

                            } catch (MessagingException ex) {
                                Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            String ipAddress = AuthHandler.getIPAddress(request);
                            int auditMode = 0;
                            String loginid = AuthHandler.getUserid(request);
                            String params = AuthHandler.getAuthor(conn, loginid)
                                    + " (" + AuthHandler.getUserName(request)
                                    + "), " + modulename;

                            AuditTrail.insertLog(conn, auditID, loginid, "", "", companyid,
                                    params, ipAddress, auditMode);
                        }
                    }
                }
            }

        } catch (SessionExpiredException ex) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE,
                    "Session Expired Exception While Requesting Subscription", ex);
            ret = "Could not send request";

        } catch (ParseException ex) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE,
                    "Parse Exception While Requesting Subscription", ex);
            ret = "Could not send request";

        } catch (ConfigurationException ex) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE,
                    "Configuration Exception While Requesting Subscription", ex);
            ret = "Could not send request";

        } catch (ServiceException e) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE,
                    "Service Exception While Requesting Subscription", e);
            ret = "Could not send request";

        } catch (SQLException e) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE,
                    "SQL Exception While Requesting Subscription", e);
            ret = "Could not send request";
        }
        return ret;
    }

    public static String requestSubModuleSubscription(Connection conn, HttpServletRequest request, String companyid) {
        String ret = "";
        try {
            int status = Integer.parseInt(request.getParameter("status"));
            String module = request.getParameter("module");
            PreparedStatement p = conn.prepareStatement(" SELECT submoduleid, parentmoduleid FROM companysubmodules WHERE submodulename = ? ");
            p.setString(1, module);
            ResultSet r = p.executeQuery();
            int parentmodid = 0;
            int submodid = 0;
            if (r.next()) {
                submodid = r.getInt("submoduleid");
                parentmodid = r.getInt("parentmoduleid");
            }

            if (submodid != 0 && parentmodid != 0) {
                p = conn.prepareStatement(" SELECT count(*) AS count FROM newsubscriptionrequest WHERE companyid = ? AND moduleid = ? AND submoduleid = ? ");
                p.setString(1, companyid);
                p.setInt(2, parentmodid);
                p.setInt(3, submodid);
                r = p.executeQuery();
                int cnt = 0;
                if (r.next()) {
                    cnt = r.getInt("count");
                }

                if (cnt == 0) {
                    p = conn.prepareStatement(" INSERT INTO newsubscriptionrequest (companyid, status, moduleid, submoduleid, requestuserid, requesttime) VALUES (?, ?, ?, ?, ?, now()) ");
                    p.setString(1, companyid);
                    p.setInt(2, status);
                    p.setInt(3, parentmodid);
                    p.setInt(4, submodid);
                    p.setString(5, request.getParameter("userid"));
                    cnt = p.executeUpdate();
                    if (cnt == 1) {
                        ret = "Request sent successfully";

                        String auditID = "334";

                        if (status == 0) {
                            auditID = "335";
                        }
                        String ipAddress = AuthHandler.getIPAddress(request);
                        int auditMode = 0;
                        String loginid = AuthHandler.getUserid(request);
                        String params = AuthHandler.getAuthor(conn, loginid)
                                + " (" + AuthHandler.getUserName(request)
                                + "), " + module;

                        AuditTrail.insertLog(conn, auditID, loginid, "", "", companyid,
                                params, ipAddress, auditMode);
                    }
                }
            }

        } catch (SessionExpiredException ex) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE,
                    "Session Expired Exception While Updating Feature View", ex);
            ret = "Could not send request";

        } catch (ServiceException e) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE,
                    "Service Exception While Updating Feature View", e);
            ret = "Could not send request";

        } catch (SQLException e) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE,
                    "SQL Exception While Updating Feature View", e);
            ret = "Could not send request";
        }
        return ret;
    }

    public static String getFeatureView(Connection conn, String companyid) {
        String ret = "";
        try {
            PreparedStatement pstmt = conn.prepareStatement(" SELECT featureid, featurename, featureshortname, issubscribed FROM featureslist ");
            ResultSet rs = pstmt.executeQuery();
            JSONObject robj = new JSONObject();

            while (rs.next()) {
                String view = "Hide";

                if (!rs.getBoolean("issubscribed")) {
                    if (DashboardHandler.isFeatureView(conn, companyid, rs.getString("featureshortname"))) {
                        view = "Show";
                    }

                } else if (DashboardHandler.isSubscribed(conn, companyid, rs.getString("featureshortname"))) {
                    if (DashboardHandler.isFeatureView(conn, companyid, rs.getString("featureshortname"))) {
                        view = "Show";
                    }
                } else {
                    continue;
                }
                JSONObject temp = new JSONObject();
                temp.put("featurename", rs.getString("featurename"));
                temp.put("featureshortname", rs.getString("featureshortname"));
                temp.put("view", view);
                robj.append("data", temp);
            }
            ret = robj.toString();

        } catch (ServiceException e) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE,
                    "Service While Updating Feature View", e);

        } catch (SQLException e) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE,
                    "SQL Exception While Updating Feature View", e);

        } catch (JSONException e) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE,
                    "JSON Exception While Updating Feature View", e);

        }
        return ret;
    }

    public static String updateFeatureView(Connection conn, HttpServletRequest request, String companyid) {
        String ret = "false";
        try {
            int view = Integer.parseInt(request.getParameter("view"));
            String featurename = request.getParameter("featurename");
            String featureShortName = "";
            PreparedStatement p = conn.prepareStatement(" SELECT featureid, featureshortname FROM featureslist WHERE featurename = ? ");
            p.setString(1, featurename);
            ResultSet r = p.executeQuery();
            int featureid = 0;
            int featureVal = 0;
            int featureUpdate = 0;
            String auditID = "336";

            if (r.next()) {
                featureid = r.getInt("featureid");
                featureShortName = r.getString("featureshortname");
                if (featureid > 0) {
                    featureVal += Math.pow(2, featureid);
                }
            }

            p = conn.prepareStatement(" SELECT featureid FROM featureslistview WHERE companyid = ? ");
            p.setString(1, companyid);
            r = p.executeQuery();

            if (r.next()) {
                featureid = r.getInt("featureid");
                if (view == 0) {                             // Hiding the feature
                    featureUpdate = featureid - featureVal;
                    auditID = "337";

                } else {                                    // Showing the feature
                    featureUpdate = featureid + featureVal;
                }
            }
            p = conn.prepareStatement(" UPDATE featureslistview SET featureid = ? WHERE companyid = ? ");
            p.setInt(1, featureUpdate);
            p.setString(2, companyid);
            int cnt = p.executeUpdate();
            if (cnt == 1) {
                ret = (view==0 ? MessageSourceProxy.getMessage("lang.hidden.text", null, request)+ " ":
                        MessageSourceProxy.getMessage("lang.activated.text", null, request)+ " ")
                        + MessageSourceProxy.getMessage("pm.featureslist."+featureShortName, null, request)+ " "
                        + MessageSourceProxy.getMessage("pm.common.module.text", null, request);

                String ipAddress = AuthHandler.getIPAddress(request);
                int auditMode = 0;
                String loginid = AuthHandler.getUserid(request);
                String params = AuthHandler.getAuthor(conn, loginid)
                        + " (" + AuthHandler.getUserName(request)
                        + "), " + featurename;

                AuditTrail.insertLog(conn, auditID, loginid, "", "", companyid,
                        params, ipAddress, auditMode);
            }

        } catch (SessionExpiredException ex) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE,
                    "Session Expired Exception While Updating Feature View", ex);
            ret = "false";

        } catch (ServiceException e) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE,
                    "Service Exception While Updating Feature View", e);
            ret = "false";

        } catch (SQLException e) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE,
                    "SQL Exception While Updating Feature View", e);
            ret = "false";
        }
        return ret;
    }

    private String getStatusForHealthMeterchart(Connection conn, String pid) throws ServiceException {
        HealthMeterDAO daoObj = new HealthMeterDAOImpl();
        HealthMeter meter = daoObj.getHealthMeter(conn, pid);
        Map<String, Object> _s = daoObj.getBaseLineMeter(conn, pid);
        int status = meter.getStatus(_s);
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("value", status);
            //jsonObj.put("projid", pid);
            jsonObj.put("needattention", meter.getNeedAttentaion());
            jsonObj.put("overdue", meter.getOverdue());
            jsonObj.put("completed", meter.getCompleted());
            jsonObj.put("future", meter.getFuture());
            jsonObj.put("ontime", meter.getOntime());
            jsonObj.put("total", meter.getTotal());
            JSONObject bv = new JSONObject();
            bv.put("ontime", _s.get("ontime"));
            bv.put("slightly", _s.get("slightly"));
            jsonObj.put("basevalues", bv);
        } catch (JSONException ex) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jsonObj.toString();
    }

    private String getHealthMeterBaseData(Connection conn, String projectId) throws ServiceException {

        HealthMeterDAO daoObj = new HealthMeterDAOImpl();
        Map<String, Object> _m = daoObj.getBaseLineMeter(conn, projectId);
        JSONObject jsonObj = new JSONObject(_m);
        return jsonObj.toString();
    }

    private String editHealthMeterData(Connection conn, HttpServletRequest request) {
        String status = "{\"success\":true,\"data\":[]}";
        try {
            HealthMeterDAO daoObj = new HealthMeterDAOImpl();
            String pid = request.getParameter("pid");
            daoObj.editBaseLineMeter(conn, pid, "TASK", request.getParameter("ontime"), request.getParameter("slightly"), request.getParameter("slightly"));
            status = "{\"success\":true,\"data\":[]}";
        } catch (ServiceException ex) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
        }
        return status;
    }

    private String[] getAllUsersId(Connection conn, HttpServletRequest request) throws ServiceException {
        String[] list = null;
        try {
            DbResults rs = DbUtil.executeQuery(conn, "SELECT users.userid FROM users INNER JOIN userlogin ON users.userid=userlogin.userid " + " WHERE companyid=? and userlogin.isactive = 1", AuthHandler.getCompanyid(request));
            list = new String[rs.size()];
            int i=0;
            while(rs.next()){
                list[i]=new String(rs.getString("userid"));
                i++;
            }

        } catch (SessionExpiredException ex) {
            Logger.getLogger(AdminServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }
}
