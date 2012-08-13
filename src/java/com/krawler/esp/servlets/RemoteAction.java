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
import com.krawler.common.customcolumn.ColumnsMetaData;
import com.krawler.common.customcolumn.CustomColumn;
import com.krawler.common.permission.PermissionConstants;
import com.krawler.common.permission.PermissionManager;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.krawler.utils.json.base.JSONObject;
import com.krawler.utils.json.base.JSONException;
import com.krawler.database.DbPool.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.timezone.Timezone;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.database.DbPool;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.esp.company.CompanyDAO;
import com.krawler.esp.company.CompanyDAOImpl;
import com.krawler.esp.handlers.AuditTrail;
import com.krawler.esp.handlers.AuthHandler;
import com.krawler.esp.handlers.CompanyHandler;
import com.krawler.esp.handlers.DashboardHandler;
import com.krawler.esp.handlers.Forum;
import com.krawler.esp.handlers.SignupHandler;
import com.krawler.esp.handlers.SuperUserHandler;
import com.krawler.esp.handlers.projdb;
import com.krawler.esp.project.meter.HealthMeterDAOImpl;
import com.krawler.esp.utils.ConfigReader;
import com.krawler.utils.json.base.JSONArray;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RemoteAction extends HttpServlet {

    private static final Integer[] casesToBeCheckedForCompanyActivatedFlag = new Integer[]{2, 4, 5, 6, 7, 8, 9, 10, 11, 12};

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String result = "";
        Connection conn = null;
        boolean testParam = false;
        int action = 0;
        String validkey = ConfigReader.getinstance().get("remoteapikey");
        String remoteapikey = "";
        if(!StringUtil.isNullOrEmpty(request.getParameter("data"))){
            try {
                JSONObject jobj = new JSONObject(request.getParameter("data"));
                testParam = (jobj.has("test") && jobj.getBoolean("test"));
                if(jobj.has("remoteapikey"))
                    remoteapikey = jobj.getString("remoteapikey");
                conn = DbPool.getConnection();
                action = Integer.parseInt(request.getParameter("action"));

                boolean isCompanyActive = true;
                if (Arrays.binarySearch(casesToBeCheckedForCompanyActivatedFlag, action) > -1) {
                    isCompanyActive = isCompanyActivated(conn, jobj);
                }

                if (isCompanyActive) {
                    switch (action) {
                        case 0:     //Company exists
                            result = isCompanyExists(conn, jobj);
                            break;
                        case 1:     //User exists
                            result = isUserExists(conn, jobj);
                            break;
                        case 2:     //User creation
                            result = createUser(conn, jobj);
                            break;
                        case 3:     //Company creation
                            result = createCompany(conn, jobj);
                            break;
                        case 4:     //Delete user - Internally User Deactivated
                            result = deleteUser(conn, jobj);
                            break;
                        case 5:     //Assign Roles - Internally Activated User
                            result = assignRoles(conn, jobj);
                            break;
                        case 6:     //Activate User
                            result = activateUser(conn, jobj);
                            break;
                        case 7:     //Deactivate User
                            result = deactivateUser(conn, jobj);
                            break;
                        case 8:     //Edit Company Profile
                            result = editCompany(conn, jobj);
                            break;
                        case 9:     //Get User Updates
                            result = getUpdates(conn, jobj);
                            break;
                        case 10:     //Edit User
                            result = editUser(conn, jobj);
                            break;
                        case 11:     //Check permission for creating New Project
                            result = canCreateProject(conn, jobj);
                            break;
                        case 12:     //Create New Project
                            result = createProject(conn, jobj);
                            break;
                        case 13:     //Fetch Subscription Requests
                            result = getSubscriptionRequests(conn, jobj);
                            break;
                        case 14:     //Get Subscription Status For a Company
                            result = manageSubscriptionStatus(conn, jobj);
                            break;
                        case 15:     //Get Subscription Status For a Company
                            result = deleteCompany(conn, jobj);
                            break;
                        case 16:     //Deactivate Company
                            result = deactivateCompany(conn, jobj);
                            break;
                    }
                } else {
                    result = getMessage(2, 99);
                }
                if(!testParam  && validkey.equals(remoteapikey)) {
                    conn.commit();
                } else {
                    result = result.substring(0, (result.length() - 1));
                    result += ",\"action\": " + Integer.toString(action) + "}";
//                    result = "{success: true, action:" + Integer.toString(action) + ",data:" + getMessage(2, 2) + "}";
                }
            } catch(JSONException e){
                result = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
                if(testParam) {
                    result += ",\"action\": " + Integer.toString(action) + "}";
//                    result = "{success: false, action:" + Integer.toString(action) + ",data:" + getMessage(2, 2) + "}";
                }
                DbPool.quietRollback(conn);
            } catch(ServiceException e){
                result = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
                if(testParam){
                    result += ",\"action\": " + Integer.toString(action) + "}";
//                    result = "{success: false, action:" + Integer.toString(action) + ",data:" + getMessage(2, 2) + "}";
                }
                DbPool.quietRollback(conn);
            } finally {
                DbPool.quietClose(conn);
                out.println(result);
            }
        } else {
            out.println(getMessage(2, 1));
        }
    }

    public static String isCompanyExists(Connection conn, JSONObject jobj) throws ServiceException{
        String r = getMessage(1, 2);//"{\"success\": true, \"infocode\": \"m02\"}";
        try{
            String sql = "";
            boolean flag = false;
            String param = "";
            if(jobj.has("companyid")){
                sql = "SELECT COUNT(*) AS count FROM company WHERE companyid = ?";
                param = jobj.getString("companyid");
            } else if(jobj.has("subdomain")){
                sql = "SELECT COUNT(*) AS count FROM company WHERE subdomain = ?";
                param = jobj.getString("subdomain");
            } else {
                flag = true;
                r = getMessage(2, 1);//"{\"success\": false, \"errorcode\": \"e01\"}";
            }
            if(!flag){
                PreparedStatement p = conn.prepareStatement(sql);
                p.setString(1, param);
                ResultSet rs = p.executeQuery();
                if(rs.next() && rs.getInt("count") > 0){
                    r = getMessage(1, 1);//"{\"success\": true, \"message\": \"m01\"}";
                }
            }
        } catch(JSONException e){
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            throw ServiceException.FAILURE(r, e);
        } catch(SQLException e){
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            throw ServiceException.FAILURE(r, e);
        } catch(ServiceException e){
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            throw ServiceException.FAILURE(r, e);
        }
        return r;
    }

    public static String isUserExists(Connection conn, JSONObject jobj) throws ServiceException{
        String r = getMessage(1, 4);//"{\"success\": true, \"infocode\": \"m04\"}";
        try{
            boolean flag = false;
            PreparedStatement p = null;
            if(jobj.has("userid")){
                p = conn.prepareStatement("SELECT COUNT(*) AS count FROM users WHERE userid = ?");
                p.setString(1, jobj.getString("userid"));
            } else if(jobj.has("username")){
                if(jobj.has("companyid")){
                    p = conn.prepareStatement("SELECT COUNT(*) AS count FROM users inner join userlogin on users.userid = userlogin.userid WHERE username = ? AND companyid = " +
                        "(SELECT companyid FROM company WHERE companyid = ?)");
                    p.setString(1, jobj.getString("username"));
                    p.setString(2, jobj.getString("companyid"));
                } else if(jobj.has("subdomain")){
                    p = conn.prepareStatement("SELECT COUNT(*) AS count FROM users inner join userlogin on users.userid = userlogin.userid WHERE username = ? AND companyid = " +
                        "(SELECT companyid FROM company WHERE subdomain = ?)");
                    p.setString(1, jobj.getString("username"));
                    p.setString(2, jobj.getString("subdomain"));
                } else {
                    flag = true;
                    r = getMessage(2, 1);//"{\"success\": false, \"errorcode\": \"e01\"}";
                }
            } else {
                flag = true;
                r = getMessage(2, 1);//"{\"success\": false, \"errorcode\": \"e01\"}";
            }
            if(!flag){
                ResultSet rs = p.executeQuery();
                if(rs.next() && rs.getInt("count") > 0){
                    r = getMessage(1, 3);//"{\"success\": true, \"infocode\": \"m03\"}";
                }
            }
        } catch(JSONException e){
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            throw ServiceException.FAILURE(r, e);
        } catch(ServiceException e){
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            throw ServiceException.FAILURE(r, e);
        } catch(SQLException e){
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            throw ServiceException.FAILURE(r, e);
        }
        return r;
    }

    public static String createUser(Connection conn, JSONObject jobj) throws ServiceException{
        String r = getMessage(1, 5);//"{\"success\": true, \"infocode\": \"m05\"}";
        try{
            boolean flag = false;
            boolean compflag=false;
            boolean pwdflg = false;
            if((jobj.has("companyid") || jobj.has("subdomain")) && jobj.has("username") && jobj.has("fname") && jobj.has("lname") && jobj.has("email")){

                String standAlone = ConfigReader.getinstance().get("PMStandAlone");
                boolean sa = Boolean.parseBoolean(standAlone);
                boolean saCheck = !sa ? (jobj.has("userid") ? true : false) : true;

                if(saCheck){
                    String uid = "";
                    String cid = "";
                    String address = "";
                    String contact = "";
                    String pwd = "";
                    String subdomain = "";
                    String newPassSHA1 = "";
                    if(jobj.has("companyid")){
                        cid = jobj.getString("companyid");
                        subdomain = getSubdomain(conn, cid);
                        if(subdomain.isEmpty()){
                            compflag=true;
                        }
                    } else {
                        cid = CompanyHandler.getCompanyIDBySubdomain(conn, jobj.getString("subdomain"));
                        subdomain = jobj.getString("subdomain");
                         if(cid.isEmpty()){
                            compflag=true;
                        }
                    }
                    if(jobj.has("userid") && !StringUtil.isNullOrEmpty(jobj.getString("userid"))){
                        uid = jobj.getString("userid");
                    } else {
                        uid = UUID.randomUUID().toString();
                    }

                    DbResults rs = DbUtil.executeQuery(conn, " SELECT * FROM users WHERE userid = ? ", new Object[] {uid});
                    if(rs.next()) {
                        r = getMessage(2, 7);//"{\"success\": false, \"errorcode\": \"e07\"}";

                    } else {

                        if(jobj.has("address")){
                            address = jobj.getString("address");
                        }
                        if(jobj.has("contact")){
                            contact = jobj.getString("contact");
                        }
                        if(jobj.has("password")){
                            newPassSHA1 = jobj.getString("password");
                            pwd = "";

                        } else {
                            pwd = sa ? "welcome" : AuthHandler.generateNewPassword();
                            newPassSHA1 = AuthHandler.getSHA1(pwd);
                            pwdflg = true;
                        }
                        if(SignupHandler.useridIsAvailable(conn, jobj.getString("username"), cid).compareTo("success") != 0){
                            flag = true;
                            r = getMessage(2, 3);//"{\"success\": false, \"errorcode\": \"e03\"}";
                        }

                        if(!compflag) {
                            if(!flag){
                                DbUtil.executeUpdate(conn, "INSERT INTO userlogin(userid, username, password, authkey) values (?, ?, ?, ?)",
                                    new Object[] { uid, jobj.getString("username"), newPassSHA1, pwd });
                                DbUtil.executeUpdate(conn,"INSERT INTO users (userid, fname, lname, emailid, contactno, address ,image, companyid) "
                                    + "VALUES (?,?,?,?,?,?,?,?)", new Object[] {uid, jobj.getString("fname"),
                                            jobj.getString("lname"),jobj.getString("email"), contact,address, "", cid });
                                PermissionManager pm =new PermissionManager();
                                pm.setUserDefaultPermissions(conn, uid);
                            } else {
                                // User with same username exist in the company
                                r = getMessage(2, 3);//"{\"success\": false, \"errorcode\": \"e03\"}";
                            }

                        } else {
                            // Companyid or subdomain does not exist
                            r = getMessage(2, 4);//"{\"success\": false, \"errorcode\": \"e04\"}";
                        }
                    }
                } else {
                    r = getMessage(2, 1);//"{\"success\": false, \"errorcode\": \"e01\"}";
                }
            } else {
                r = getMessage(2, 1);//"{\"success\": false, \"errorcode\": \"e01\"}";
            }
        } catch(JSONException e){
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            throw ServiceException.FAILURE(r, e);
        } catch(ServiceException e){
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            throw ServiceException.FAILURE(r, e);
        }
        return r;
    }

    public static String createCompany(Connection conn, JSONObject jobj) throws ServiceException{
        String r = getMessage(1, 6);//"{\"success\": true, \"infocode\": \"m06\"}";
        try{
            if(jobj.has("companyid") && jobj.has("userid") && jobj.has("username")
                    && jobj.has("companyname") && jobj.has("subdomain")
                    && jobj.has("fname") && jobj.has("lname") && jobj.has("email")){

                if(SignupHandler.subdomainIsAvailable(conn, jobj.getString("subdomain")).compareTo("success") == 0){
                    String uid = "";
                    String cid = "";
                    String address = "";
                    String city = "";
                    String state = "";
                    String phone = "";
                    String fax = "";
                    String zip = "";
                    int planid = 2;
                    java.sql.Timestamp sqlPostDate = Timezone.getGmtTimestamp();
                    java.sql.Date sqldt = new java.sql.Date(new java.util.Date().getTime());
                    if(jobj.has("companyid")){
                        cid = jobj.getString("companyid");
                    }
                    DbResults rs = DbUtil.executeQuery(conn, " SELECT * FROM company WHERE companyid = ? ", new Object[] {cid});
                    if(rs.next()) {
                        r = getMessage(2, 8);//"{\"success\": false, \"errorcode\": \"e08\"}";

                    } else {
                        if(jobj.has("userid")){
                            uid = jobj.getString("userid");
                        }
                        if(jobj.has("address")){
                            address = jobj.getString("address");
                        }
                        if(jobj.has("city")){
                            city = jobj.getString("city");
                        }
                        if(jobj.has("state")){
                            state = jobj.getString("state");
                        }
                        if(jobj.has("phone")){
                            phone = jobj.getString("phone");
                        }
                        if(jobj.has("zip")){
                            zip = jobj.getString("zip");
                        }
                        if(jobj.has("fax")){
                            fax = jobj.getString("fax");
                        }
                        if(jobj.has("planid")){
                            planid = jobj.getInt("planid");
                        }
                        DbUtil.executeUpdate(conn,"INSERT INTO company (companyid, companyname,subdomain, address, city, state, " +
                            "phone, fax, zip, timezone, activated, subscriptiondate, creator,createdon,currency,planid,country, notificationtype,milestonewidget) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,23,true,?,'',?,1,?,244,1,0)", new Object[] {cid, jobj.getString("companyname"),
                                jobj.getString("subdomain"), address, city, state, phone, fax, zip,sqlPostDate, sqldt, planid});
                        jobj.put("userid", uid);
                        JSONObject temp = new JSONObject(createUser(conn, jobj));
                        if(!temp.getBoolean("success")){
                            r = temp.toString();
                        } else {
                            DbUtil.executeUpdate(conn,"UPDATE company SET creator = ? WHERE companyid = ?", new Object[] {uid, cid});
                            PermissionManager pm =new PermissionManager();
                            pm.AssignAllPermissions(conn, uid);
                        }
                        SignupHandler.setUpCompany(conn, cid, uid);
                        CompanyDAO cd = new CompanyDAOImpl();
                        cd.setCompanyPERTDefaultDifference(conn, cid, Constants.DEFAULT_PERT_DURATION_DIFF, Constants.DEFAULT_PERT_DURATION_DIFF);
                    }

                } else {
                    r = getMessage(2, 9);//"{\"success\": false, \"errorcode\": \"e09\"}";
                }

            } else {
                r = getMessage(2, 1);//"{\"success\": false, \"errorcode\": \"e01\"}";
            }
        } catch(JSONException e){
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            throw ServiceException.FAILURE(r, e);
        } catch(ServiceException e){
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            throw ServiceException.FAILURE(r, e);
        }
        return r;
    }

    public static String deleteUser(Connection conn, JSONObject jobj) throws ServiceException{
        String r = getMessage(1, 7);//"{\"success\": true, \"infocode\": \"m07\"}";
        try{
            String uid = "";
            String userid[] = null;
            boolean flag = false;
            if(jobj.has("userid")){
                uid = jobj.getString("userid");
                userid = uid.split(",");

            } else {
                flag = true;
                r = getMessage(2, 1);//"{\"success\": false, \"errorcode\": \"e01\"}";
            }
            if(!flag){
                for (int cntUser = 0; cntUser < userid.length; cntUser++) {
                    PreparedStatement pstmt = conn.prepareStatement(" UPDATE userlogin " +
                            " SET isactive = false WHERE userid = ? ");
                    pstmt.setString(1, userid[cntUser]);
                    pstmt.executeUpdate();

//                    pstmt = conn.prepareStatement(" UPDATE users " +
//                            " SET username = CONCAT(username, '_del') WHERE userid = ? ");
//                    pstmt.setString(1, userid[cntUser]);
//                    pstmt.executeUpdate();

                    pstmt = conn.prepareStatement(" UPDATE userlogin " +
                            " SET username = CONCAT(username, '_del') WHERE userid = ? ");
                    pstmt.setString(1, userid[cntUser]);
                    pstmt.executeUpdate();

                    pstmt = conn.prepareStatement(" UPDATE projectmembers " +
                            " SET inuseflag = 0 WHERE userid = ?");
                    pstmt.setString(1, userid[cntUser]);
                    pstmt.executeUpdate();

                    pstmt = conn.prepareStatement(" UPDATE proj_resources" +
                            " SET inuseflag = 0 WHERE resourceid = ?");
                    pstmt.setString(1, userid[cntUser]);
                    pstmt.executeUpdate();
                }
            }

        } catch (SQLException ex) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAction.class.getName()).log(Level.SEVERE, "SQL Exception While Deleting User", ex);
            throw ServiceException.FAILURE(r, ex);

        } catch(JSONException e) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAction.class.getName()).log(Level.SEVERE, "JSON Exception While Deleting User", e);
            throw ServiceException.FAILURE(r, e);

        } catch (ServiceException e) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAction.class.getName()).log(Level.SEVERE, "Service Exception While Deleting User", e);
            throw ServiceException.FAILURE(r, e);
        }
        return r;
    }

    public static String deactivateUser(Connection conn, JSONObject jobj) throws ServiceException {
        String r = getMessage(1, 7);//"{\"success\": true, \"infocode\": \"m07\"}";
        try {
            String uid = "";
            String userid[] = null;
            boolean flag = false;
            if (jobj.has("userid")) {
                uid = jobj.getString("userid");
                userid = uid.split(",");

            } else {
                flag = true;
                r = getMessage(2, 1);//"{\"success\": false, \"errorcode\": \"e01\"}";
            }

            if (!flag) {
                for (int cntUser = 0; cntUser < userid.length; cntUser++) {
                    PreparedStatement pstmt = conn.prepareStatement(" UPDATE userlogin " +
                            " SET isactive = false WHERE userid = ? ");
                    pstmt.setString(1, userid[cntUser]);
                    pstmt.executeUpdate();

                    pstmt = conn.prepareStatement(" UPDATE projectmembers " +
                            " SET inuseflag = 0 WHERE userid = ?");
                    pstmt.setString(1, userid[cntUser]);
                    pstmt.executeUpdate();

                    pstmt = conn.prepareStatement(" UPDATE proj_resources" +
                            " SET inuseflag = 0 WHERE resourceid = ?");
                    pstmt.setString(1, userid[cntUser]);
                    pstmt.executeUpdate();
                }
            }

        } catch (SQLException ex) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAction.class.getName()).log(Level.SEVERE, "SQL Exception While Deleting User", ex);
            throw ServiceException.FAILURE(r, ex);

        } catch(JSONException e) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAction.class.getName()).log(Level.SEVERE, "JSON Exception While Deleting User", e);
            throw ServiceException.FAILURE(r, e);

        } catch (ServiceException e) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAction.class.getName()).log(Level.SEVERE, "Service Exception While Deleting User", e);
            throw ServiceException.FAILURE(r, e);
        }
        return r;
    }

    public static String assignRoles(Connection conn, JSONObject jobj) throws ServiceException {
        String result = "";
        result = activateUser(conn, jobj);
        try {
            JSONObject res = new JSONObject(result);
            if (res.getBoolean("success")) {
                if (jobj.has("role") && StringUtil.equal(jobj.getString("role"), "10")) { // Here 10 is role comes from Apps with Remote API Call, in which 1 is prefix and 0 is roleid. it indicates user is company creator.
                    DbUtil.executeUpdate(conn, "UPDATE company SET creator = ? WHERE companyid = ?", new Object[]{jobj.getString("userid"), jobj.getString("companyid")});
                    PermissionManager pm = new PermissionManager();
                    pm.AssignAllPermissions(conn, jobj.getString("userid"));
                    result = getMessage(1, 8); // update company creator
                }
            }
        } catch (JSONException ex) {
            result = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAction.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE("JSON Execption while assigning roles", ex);
        } finally {
            return result;
        }
    }

    public static String activateUser(Connection conn, JSONObject jobj) throws ServiceException {
        String result = getMessage(1, 5);//"{\"success\": true, \"infocode\": \"m07\"}";
        try {
            String userid = "";
            if (jobj.has("userid")) {
                userid = jobj.getString("userid");

                PreparedStatement pstmt = conn.prepareStatement(" UPDATE userlogin " +
                        " SET isactive = true WHERE userid = ? ");
                pstmt.setString(1, userid);
                pstmt.executeUpdate();

                pstmt = conn.prepareStatement(" UPDATE projectmembers " +
                            " SET inuseflag = 1 WHERE userid = ?");
                pstmt.setString(1, userid);
                pstmt.executeUpdate();

                pstmt = conn.prepareStatement(" UPDATE proj_resources" +
                        " SET inuseflag = 1 WHERE resourceid = ?");
                pstmt.setString(1, userid);
                pstmt.executeUpdate();

            } else {
                result = getMessage(2, 7);//"{\"success\": false, \"errorcode\": \"e07\"}";
            }

        } catch (SQLException ex) {
            Logger.getLogger(RemoteAction.class.getName()).log(Level.SEVERE, "SQL Exception While Activating User", ex);
            result = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            throw ServiceException.FAILURE(result, ex);

        } catch(JSONException e) {
            Logger.getLogger(RemoteAction.class.getName()).log(Level.SEVERE, "JSON Exception While Activating User", e);
            result = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            throw ServiceException.FAILURE(result, e);
        }
        return result;
    }

    public static String getUserid(Connection conn, String companyid, String username) throws ServiceException{
        String uid = "";
        try{
            PreparedStatement p = conn.prepareStatement("SELECT users.userid FROM users inner join userlogin on users.userid = userlogin.userid WHERE username = ? AND companyid = ?");
            p.setString(1, username);
            p.setString(2, companyid);
            ResultSet rs = p.executeQuery();
            if(rs.next()){
                uid = rs.getString("userid");
            }
        } catch(SQLException e){
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return uid;
    }

    public static String editCompany(Connection conn, JSONObject jobj) throws ServiceException {

        String result = getMessage(1, 9);//"{\"success\": true, \"infocode\": \"m09\"}";
        try {
            if (jobj.has("companyid") && jobj.has("subdomain") && jobj.has("currency")
                    && jobj.has("country") && jobj.has("timezone")) {

                String companyid = jobj.getString("companyid");
                String newSubdomain = jobj.getString("subdomain");
                String currency = jobj.getString("currency");
                String country = jobj.getString("country");
                String timezone = jobj.getString("timezone");
                String companyname = "";
                String phone = "";
                String fax = "";
                String zip = "";
                String emailid = "";
                String website = "";
                String image = "";
                String address = "";
                String city = "";
                String state = "";

                PreparedStatement pstmt = conn.prepareStatement(" SELECT * FROM company WHERE companyid = ? ");
                pstmt.setString(1, companyid);
                ResultSet rs = pstmt.executeQuery();
                if(rs.next()) {
                    String oldSubdomain = rs.getString("subdomain");
                    boolean domainFlag = false;
                    if (oldSubdomain.compareToIgnoreCase(newSubdomain) != 0) {
                        if (SignupHandler.isNewSubdomainAvailable(conn, newSubdomain).compareTo("success") == 0) {
                            domainFlag = true;
                        }

                    } else {    // No Change In Subdomain
                        domainFlag = true;
                    }

                    companyname = jobj.isNull("companyname") ? "" : jobj.getString("companyname");
                    address = jobj.isNull("address") ? "" : jobj.getString("address");
                    city = jobj.isNull("city") ? "" : jobj.getString("city");
                    state = jobj.isNull("state") ? "" : jobj.getString("state");
                    phone = jobj.isNull("phone") ? "" : jobj.getString("phone");
                    zip = jobj.isNull("zip") ? "" : jobj.getString("zip");
                    fax = jobj.isNull("fax") ? "" : jobj.getString("fax");
                    emailid = jobj.isNull("emailid") ? "" : jobj.getString("emailid");
                    website = jobj.isNull("website") ? "" : jobj.getString("website");

                    if (jobj.has("image")) {
                        image = jobj.getString("image");

                    } else {
                        image = rs.getString("image");
                        if (StringUtil.isNullOrEmpty(image)) {
                            image = "";
                        }
                    }

                    if (domainFlag) {
                        PreparedStatement psInsert = conn.prepareStatement(" UPDATE company SET " +
                                " subdomain = ?, companyname = ?, currency = ?, country = ?, " +
                                " timezone = ?, address = ?, city = ?, state = ?, " +
                                " phone = ?, fax = ?, zip = ?, emailid = ?, website = ?, " +
                                " image = ? WHERE companyid = ? ");
                        psInsert.setString(1, newSubdomain);
                        psInsert.setString(2, companyname);
                        psInsert.setString(3, currency);
                        psInsert.setString(4, country);
                        psInsert.setString(5, timezone);
                        psInsert.setString(6, address);
                        psInsert.setString(7, city);
                        psInsert.setString(8, state);
                        psInsert.setString(9, phone);
                        psInsert.setString(10, fax);
                        psInsert.setString(11, zip);
                        psInsert.setString(12, emailid);
                        psInsert.setString(13, website);
                        psInsert.setString(14, image);
                        psInsert.setString(15, companyid);
                        psInsert.executeUpdate();

                    } else {
                        // Subdomain Not Availble
                        result = getMessage(2, 9);//"{\"success\": false, \"errorcode\": \"e09\"}";
                    }

                } else {
                    // Company Does Not Exist
                    result = getMessage(2, 8);//"{\"success\": false, \"errorcode\": \"e08\"}";
                }

            } else {
                // Insuffincient Data
                result = getMessage(2, 1);//"{\"success\": false, \"errorcode\": \"e01\"}";
            }

        } catch (SQLException ex) {
            Logger.getLogger(RemoteAction.class.getName()).log(Level.SEVERE,
                    "SQL Exception While Updating Company Profile", ex);
            // Error Occurred While Connecting To Server
            result = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            throw ServiceException.FAILURE(result, ex);

        } catch(JSONException e) {
            Logger.getLogger(RemoteAction.class.getName()).log(Level.SEVERE,
                    "JSON Exception While Updating Company Profile", e);
            // Error Occurred While Connecting To Server
            result = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            throw ServiceException.FAILURE(result, e);

        } catch(ServiceException e) {
            Logger.getLogger(RemoteAction.class.getName()).log(Level.SEVERE,
                    "Service Exception While Updating Company Profile", e);
            // Error Occurred While Connecting To Server
            result = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            throw ServiceException.FAILURE(result, e);
        }
        return result;
    }

    public static String getUpdates (Connection conn, JSONObject jobj) throws ServiceException {
        String updates = "";
        try {
            if(jobj.has("userid") && jobj.has("companyid") && jobj.has("offset") && jobj.has("limit")) {
                int offset = Integer.parseInt(jobj.getString("offset"));
                int limit = Integer.parseInt(jobj.getString("limit"));
                updates = getProjectUpdates(conn, jobj.getString("userid"), jobj.getString("companyid"), offset, limit);
                updates = "{\"valid\": true, \"success\": true, \"data\":" + updates + "}";

            } else {
                // Insuffincient Data
                updates = getMessage(2, 1);//"{\"success\": false, \"errorcode\": \"e01\"}";
            }

        } catch (JSONException jex) {
            Logger.getLogger(RemoteAction.class.getName()).log(Level.SEVERE,
                    "JSON Exception While Updating Company Profile", jex);
            // Error Occurred While Connecting To Server
            updates = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            throw ServiceException.FAILURE(updates, jex);
        }

        return updates;
    }

    public static String editUser(Connection conn, JSONObject jobj) throws ServiceException {
        String r = getMessage(1, 11);//"{\"success\": true, \"infocode\": \"m07\"}";
        try {
            String userid = "";
            boolean sa = Boolean.parseBoolean(ConfigReader.getinstance().get("PMStandAlone"));

            if (jobj.has("userid")) {
                userid = StringUtil.serverHTMLStripper(jobj.get("userid").toString());
            } else {
                r = getMessage(2, 1);//"{\"success\": false, \"errorcode\": \"e01\"}";
            }

            PreparedStatement pstmt = conn.prepareStatement(" select COUNT(*) as count from users where userid = ? ");
            pstmt.setObject(1, userid);
            ResultSet checkisUser = pstmt.executeQuery();
            checkisUser.next();
            if(checkisUser.getInt("count")==0){
                r = getMessage(2, 6);
            }
            else {
                String emailid = !sa ? (jobj.has("emailid") ? jobj.getString("emailid").trim().replace(" ", "+") : "") : (jobj.has("email") ? jobj.getString("email").trim().replace(" ", "+") : "");
                String fname = jobj.has("fname") ? StringUtil.serverHTMLStripper(jobj.get("fname").toString()) : "";
                String lname = jobj.has("lname") ? StringUtil.serverHTMLStripper(jobj.get("lname").toString()) : "";
                emailid = StringUtil.serverHTMLStripper(emailid);
                String contactno = !sa ? (jobj.has("contactno") ? StringUtil.serverHTMLStripper(jobj.get("contactno").toString()) : "") : jobj.has("contact") ? StringUtil.serverHTMLStripper(jobj.get("contact").toString()) : "";
                String address = jobj.has("address") ? StringUtil.serverHTMLStripper(jobj.get("address").toString()) : "";
                int timezone = 47;
                String tzString = "";
                if(jobj.has("timezone")){
                    tzString = jobj.get("timezone").toString();
                    if(!StringUtil.isNullOrEmpty(tzString)){
                        timezone = Integer.parseInt(tzString);
                    }
                }
                String sql = "update users set fname=?, lname=? , emailid=?, address=?,contactno=?, timezone=? where userid=?;";

                pstmt = conn.prepareStatement(sql);
                pstmt.setObject(1, fname);
                pstmt.setObject(2, lname);
                pstmt.setObject(3, emailid);
                pstmt.setObject(4, address);
                pstmt.setObject(5, contactno);
                pstmt.setObject(6, timezone);
                pstmt.setObject(7, userid);
                pstmt.executeUpdate();
            }
        } catch (SQLException ex) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAction.class.getName()).log(Level.SEVERE, "SQL Exception While Editing User", ex);
            throw ServiceException.FAILURE(r, ex);
        } catch (JSONException e) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAction.class.getName()).log(Level.SEVERE, "JSON Exception While Editing User", e);
            throw ServiceException.FAILURE(r, e);

        } catch (ServiceException e) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAction.class.getName()).log(Level.SEVERE, "Service Exception While Editing User", e);
            throw ServiceException.FAILURE(r, e);
        }
        return r;
    }

    public static String getProjectUpdates(Connection conn, String userid, String companyid, int offset, int limit) throws ServiceException {
        ResultSet rsForSubQ = null;
        String str = "";
        String projid = null;
        int sqlLimit = 15;
        JSONObject jdata = new JSONObject();
        try {
            boolean sub = DashboardHandler.isSubscribed(conn, companyid, "usrtk");
            String projectlist = DashboardHandler.getProjectList(conn, userid, 1000, 0, "");
            JSONObject projListObj = new JSONObject(projectlist);
            if (projListObj.getString("data").compareTo("{}") != 0) {
                JSONObject jDataHead = new JSONObject();
                JSONObject jData = new JSONObject();
                jDataHead.put("head", "<div style='padding:10px 0 10px 0;font-size:13px;font-weight:bold;color:#10559a;border-bottom:solid 1px #EEEEEE;'>Updates</div>");
                jData.append("data", jDataHead);
                JSONArray projList = projListObj.getJSONArray("data");
                for (int i = 0; i < projList.length(); i++) {
                    JSONObject temp = projList.getJSONObject(i);
                    projid = temp.getString("id");
                    JSONObject membership = new JSONObject(Forum.getProjectMembership(conn, userid, projid));
                    JSONObject userProjData = membership.getJSONArray("data").getJSONObject(0);
                    int pp = userProjData.getInt("planpermission");
                    boolean uT = (userProjData.getInt("connstatus") != 4 && (pp == 8 || pp  == 16) && sub);
                    String projName = projdb.getProjectName(conn, projid);
                    int mode = 3;
                    if (userProjData.getInt("connstatus") == 4) {
                        mode = 4;
                    } else if (uT) {
                        mode = 6;
                    }
                    String projectName = "<span style='color:#10559A'>" + projName + "</span> - ";
                    // Due Tasks
                    String sql = " SELECT DATEDIFF(DATE(proj_task.enddate), DATE(NOW())) AS tasklength, taskname, percentcomplete AS complete,taskid " +
                        "FROM proj_task " +
                        " WHERE projectid = ? AND (DATE(proj_task.enddate) >= DATE(NOW())) AND (DATE(proj_task.enddate) <= adddate(DATE(NOW()),7)) " +
                            " ORDER BY tasklength ASC LIMIT " + sqlLimit;
                    PreparedStatement p = conn.prepareStatement(sql);
                    p.setString(1, projid);
                    if (mode == 6) {
                        sql = "SELECT datediff(date(now()),date(proj_task.enddate)) AS tasklength,taskname, percentcomplete AS complete,proj_task.taskid AS taskid " +
                            "FROM proj_task INNER JOIN proj_taskresourcemapping ON proj_task.taskid = proj_taskresourcemapping.taskid " +
                            "WHERE projectid = ? AND proj_taskresourcemapping.resourceid = ? AND (date(proj_task.enddate) < date(now()) AND percentcomplete < 100) " +
                                "ORDER BY tasklength ASC LIMIT " + sqlLimit;
                        p = conn.prepareStatement(sql);
                        p.setString(1, projid);
                        p.setString(2, userid);
                    }
                    rsForSubQ = p.executeQuery();
                    while (rsForSubQ.next()) {
                        JSONObject jTask = new JSONObject();
                        String resrs = "";
                        String taskName = rsForSubQ.getString("taskname");
                        if (taskName.length() > 28) {
                            taskName = taskName.substring(0, 25);
                            taskName = taskName.concat("...");
                        }
                        int tasklength = Integer.parseInt(rsForSubQ.getObject("tasklength").toString());
                        if (tasklength == 0 || tasklength == 1) {
                            if (tasklength == 0) {
                                resrs = "Due today";
                            } else {
                                resrs = "Due tomorrow";
                            }
                        } else if (tasklength > 1) {
                            resrs = "Due in " + rsForSubQ.getObject("tasklength").toString() + " days";
                        }
                        jTask.put("name", "<div style='padding:0 0 5px 0;border-bottom:solid 1px #EEEEEE;'>" + projectName + taskName + " - (" + rsForSubQ.getObject("complete").toString() + "%" + " Complete) " + resrs + "</div>");
                        jdata.append("data", jTask);
                        taskName = "";
                        tasklength = 0;
                    }
                    // Over Due Tasks
                    sql = " SELECT datediff(date(now()),date(proj_task.enddate)) AS tasklength,taskname, percentcomplete AS complete,taskid " +
                        "FROM proj_task " +
                        "WHERE projectid = ? AND (date(proj_task.enddate) < date(now()) AND percentcomplete < 100) " +
                            "ORDER BY tasklength ASC LIMIT " + sqlLimit;
                    p = conn.prepareStatement(sql);
                    p.setString(1, projid);
                    if (mode == 6) {
                        sql = "SELECT datediff(date(proj_task.enddate),date(now())) AS tasklength,taskname, percentcomplete AS complete,proj_task.taskid AS taskid " +
                            "FROM proj_task INNER JOIN proj_taskresourcemapping ON proj_task.taskid = proj_taskresourcemapping.taskid " +
                            "WHERE projectid = ? AND proj_taskresourcemapping.resourceid = ? AND (date(proj_task.enddate)>=date(now())) " +
                                "AND (date(proj_task.enddate) <= adddate(date(now()),7)) " +
                                "ORDER BY tasklength ASC LIMIT " + sqlLimit;
                        p = conn.prepareStatement(sql);
                        p.setString(1, projid);
                        p.setString(2, userid);
                    }
                    rsForSubQ = p.executeQuery();
                    while (rsForSubQ.next()) {
                        JSONObject jTask = new JSONObject();
                        String overdue = "";
                        String taskName = rsForSubQ.getString("taskname");
                        if (taskName.length() > 28) {
                            taskName = taskName.substring(0, 25);
                            taskName = taskName.concat("...");
                        }
                        int tasklength = Integer.parseInt(rsForSubQ.getObject("tasklength").toString());
                        if (tasklength == 1) {
                            overdue = "<span style = 'color:#ff6666;'>Overdue by 1 day</span>";
                        } else {
                            overdue = "<span style = 'color:#ff6666;'>Overdue by " + rsForSubQ.getObject("tasklength").toString() + " days</span>";
                        }
                        overdue = " - <span style='color:gray;'>" + overdue + "</span><div style='clear:both;visibility:hidden;height:0;line-height:0;'></div>";
                        jTask.put("name", "<div style='padding:0 0 5px 0;border-bottom:solid 1px #EEEEEE;'>" + projectName + taskName + " - (" + rsForSubQ.getObject("complete").toString() + "%" + " Complete) " + overdue + "</div>");
                        jdata.append("data", jTask);
                        taskName = "";
                        tasklength = 0;
                    }
                    // New Tasks
                    sql = "SELECT datediff(date(proj_task.startdate),date(now())) AS tasklength,taskname, percentcomplete AS complete,taskid " +
                        "FROM proj_task " +
                        "WHERE projectid = ? AND (date(proj_task.startdate)>=date(now())) AND (date(proj_task.startdate) <= adddate(date(now()),7)) " +
                            "ORDER BY tasklength ASC LIMIT " + sqlLimit;
                    p = conn.prepareStatement(sql);
                    p.setString(1, projid);
                    if (mode == 6) {
                        sql = "SELECT datediff(date(proj_task.startdate),date(now())) AS tasklength,taskname, percentcomplete AS complete,proj_task.taskid AS taskid " +
                            "FROM proj_task INNER JOIN proj_taskresourcemapping ON proj_task.taskid = proj_taskresourcemapping.taskid " +
                            "WHERE projectid = ? AND proj_taskresourcemapping.resourceid = ? AND (date(proj_task.startdate)>=date(now())) " +
                                "AND (date(proj_task.startdate) <= adddate(date(now()),7)) " +
                                "ORDER BY tasklength ASC LIMIT " + sqlLimit;
                        p = conn.prepareStatement(sql);
                        p.setString(1, projid);
                        p.setString(2, userid);
                    }
                    rsForSubQ = p.executeQuery();
                    while (rsForSubQ.next()) {
                        JSONObject jTask = new JSONObject();
                        String resrs = "";
                        String taskName = rsForSubQ.getString("taskname");
                        if (taskName.length() > 28) {
                            taskName = taskName.substring(0, 25);
                            taskName = taskName.concat("...");
                        }
                        int tasklength = Integer.parseInt(rsForSubQ.getObject("tasklength").toString());
                        if (tasklength == 0 || tasklength == 1) {
                            if (tasklength == 0) {
                                resrs = "Starting today";
                            } else {
                                resrs = "Starting tomorrow";
                            }
                        } else if (tasklength > 1) {
                            resrs = "Starting in " + rsForSubQ.getObject("tasklength").toString() + " days";
                        }
                        jTask.put("name", "<div style='padding:0 0 5px 0;border-bottom:solid 1px #EEEEEE;'>" + projectName + taskName + " - (" + rsForSubQ.getObject("complete").toString() + "%" + " Complete) " + resrs + "</div>");
                        jdata.append("data", jTask);
                        taskName = "";
                        tasklength = 0;
                    }
                }
                JSONArray jDataArr = new JSONArray();
                jDataArr = jdata.getJSONArray("data");
                for (int startCnt = offset; (startCnt < limit + offset) && (startCnt < jDataArr.length()); startCnt++) {
                    JSONObject jDataLimit = new JSONObject();
                    jDataLimit.put("update", jDataArr.getJSONObject(startCnt).getString("name"));
                    jData.append("data", jDataLimit);
                }
                JSONObject chartData = new JSONObject();
                chartData = DashboardHandler.getProjectListForChart(conn, userid, "5", "0");
                if (chartData.has("count") && chartData.has("data")) {
                    JSONArray tempArr = chartData.getJSONArray("count");
                    int cntChart = tempArr.getInt(0) > 5 ? 5 : tempArr.getInt(0);
                    JSONArray jarr = chartData.getJSONArray("data");
                    if (cntChart > 0) {
                        String projNames = "";
                        String projComplete = "";
                        for (int cntData = 0; cntData < cntChart; cntData++) {
                            JSONObject temp = jarr.getJSONObject(cntData);
                            String pName = temp.getString("name");
                            if (pName.length() > 10) {
                                pName = pName.substring(0, 6) + "..";
                            }
                            projNames = projNames + "|" + pName;
                            String pComplete = temp.getString("complete");
                            if (cntData > 0) {
                                projComplete = projComplete + ",";
                            }
                            projComplete = projComplete + pComplete;
                        }
                        projNames = projNames + "|";
                        String size = "350x150";
                        if (cntChart > 3) {     // Chart size increased if no. of projects increases.
                            size = "350x200";
                        }
                        JSONObject jDataLimit = new JSONObject();
                        jDataLimit.put("graph", "<img src='http://chart.apis.google.com/chart?chxt=x,y&chxl=1:" +
                                projNames + "&cht=bhs&chd=t:" + projComplete +
                                "&chco=4D89F9&chs=" + size + "&chtt=Project+Completion+Graph&chts=10559A,15'" +
                                " style='margin-top:15px;'/>");
                        jData.append("data", jDataLimit);
                    }
                }
                jData.append("count", jDataArr.length());
                str = jData.toString();
            }/* else {
                // When no updates then default text of Deskera Project is sent in response.
                String sb = "<div><div><b>Welcome to Deskera!</b></div>";
                sb += "<div >You can start collaborating with your team members in no time on Deskera.</div><br/>";
                sb += "<div><b>Build your network</b></div>";
                sb += "</h2>";
                sb += "<div>Easily find people that you want to connect with through search-as-you-type and send them a connection request. When your friends request is accepted, the person gets automatically added to your network. It\'s that easy!</div>";
                sb += "<div>Once you have added people to your network you can chat with them, send messages, share content, and do what people do best - communicate.</div><br/>";
                sb += "<h2>";
                sb += "<div><b>Add Projects</b></div>";
                sb += "</h2>";
                sb += "<div>Working together with your team members to ensure that a project is delivered on time has never been so easy. All you need to do to create a project is - enter a name and description of the project, and add people who would be involved in the project. Team members can collaborate easily through chat, discussion board, project plan, shared documents and team calendar - all available on the project page.</div></div>";
                JSONObject jString = new JSONObject();
                jString.put("head", "<div style='padding:10px 0 10px 0;font-size:13px;font-weight:bold;color:#10559a;border-bottom:solid 1px #EEEEEE;'>Welcome</div>");
                jString.put("update", sb);
                JSONObject jData = new JSONObject();
                jData.append("data", jString);
                jData.append("count", 1);
                str = jData.toString();
            }*/
        } catch (SQLException ex) {
            str = "";
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ServiceException ex) {
            str = "";
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException ex) {
            str = "";
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return str;
    }

    public static String createProject(Connection conn, JSONObject jobj) throws ServiceException{
        String r = "";
        boolean success = false;
		try {
            if(jobj.has("userid") && jobj.has("projectname") && jobj.has("companyid")){
                JSONObject projData = new JSONObject();
                String userid = jobj.getString("userid");
                String companyid = jobj.getString("companyid");

                String projectid = UUID.randomUUID().toString();
                String projName = StringUtil.serverHTMLStripper(jobj.getString("projectname").replaceAll("[^\\w|\\s|'|\\-|\\[|\\]|\\(|\\)]","").trim());
                String nickName = AdminServlet.makeNickName(conn,projName,1);

                if(StringUtil.isNullOrEmpty(projName)) {
                    // Insuffincient Data
                    r = getMessage(2, 1);//"{\"success\": false, \"errorcode\": \"e01\"}";
                    success = false;
                } else {
                    String qry = "INSERT INTO project (projectid, projectname, description, image, companyid, nickname, createdon, startdate) VALUES (?,?,?,?,?,?,now(),now())";

                    PreparedStatement pstmt = conn.prepareStatement(qry);
                    pstmt.setString(1, projectid);
                    pstmt.setString(2, projName);
                    pstmt.setString(3, "");
                    pstmt.setString(4, "");
                    pstmt.setString(5, companyid);
                    pstmt.setString(6, nickName);
                    int cnt = pstmt.executeUpdate();
                    if(cnt == 1){
                        com.krawler.esp.handlers.Forum.setStatusProject(conn, userid, projectid, 4, 0, "", companyid);
                        AdminServlet.setDefaultWorkWeek(conn, projectid);
                        projData.put("projectid", projectid);
                        projData.put("nickname", nickName);
                        success = true;
                    }

                    if(success){
                        CustomColumn cc = CCManager.getCustomColumn(companyid);
                        ColumnsMetaData csmd = cc.getColumnsMetaData(conn, "Project");
                        Map values = new HashMap();
                        int size = csmd.getColumnCount();
                        for(int i=0; i<size; i++){
                            values.put(csmd.getCoulmnNo(i), csmd.getColumn(i).getDefaultValue());
                        }
                        cc.insertColumnsData(conn, values, "project", projectid);
                         (new HealthMeterDAOImpl()).setBaseLineMeter(conn, projectid);
                        JSONObject ret = new JSONObject();
                        ret.put("success", success);
                        ret.put("data", projData);
                        r = ret.toString();

                        // Local audit trail entry
                        String params = AuthHandler.getAuthor(conn, userid) + " (" +
                                        AuthHandler.getUserName(conn, userid) + "), " + projName;
                        AuditTrail.insertLog(conn, "321", userid, projectid, projectid, companyid, params, "", 0);
                    } else {
                        // Error while creating Project
                        r = getMessage(2, 11);//"{\"success\": false, \"errorcode\": \"e11\"}";
                    }
                }
            } else {
                // Insuffincient Data
                r = getMessage(2, 1);//"{\"success\": false, \"errorcode\": \"e01\"}";
            }
		} catch (SQLException ex) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAction.class.getName()).log(Level.SEVERE, "SQL Exception While Creating Project", ex);
            throw ServiceException.FAILURE(r, ex);
        } catch (JSONException ex) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAction.class.getName()).log(Level.SEVERE, "JSON Exception While Creating Project", ex);
            throw ServiceException.FAILURE(r, ex);
        }
		return r;
    }

    public static String canCreateProject(Connection conn, JSONObject jobj) throws ServiceException{
        String r = "";
        boolean success = false;
		try {
            if(jobj.has("userid") && jobj.has("companyid")){
                String userid = jobj.getString("userid");
                String companyid = jobj.getString("companyid");

                if(StringUtil.isNullOrEmpty(userid) && StringUtil.isNullOrEmpty(companyid)) {
                    // Insuffincient Data
                    r = getMessage(2, 1);//"{\"success\": false, \"errorcode\": \"e01\"}";
                    success = false;
                } else {
                    boolean managePerm = false;
                    String comp = isCompanyExists(conn, jobj);
                    JSONObject cj = new JSONObject(comp);

                    if(cj.has("infocode") && cj.getString("infocode").equals("m01")){
                        String usr = isUserExists(conn, jobj);
                        JSONObject uj = new JSONObject(usr);

                        if(uj.has("infocode") && uj.getString("infocode").equals("m03")){
                            String qry = "select COUNT(*) from users where userid = ? and companyid = ?";
                            PreparedStatement pstmt = conn.prepareStatement(qry);
                            pstmt.setString(1, userid);
                            pstmt.setString(2, companyid);
                            ResultSet check = pstmt.executeQuery();
                            if(check.next()){
                                success = true;
                                managePerm = new PermissionManager().isPermission(conn, userid, PermissionConstants.Feature.PROJECT_ADMINISTRATION, PermissionConstants.Activity.PROJECT_CREATE);
                            }
                        } else {
                            // User doesn't exists
                            r = getMessage(2, 6);//"{\"success\": false, \"errorcode\": \"e06\"}";
                            managePerm = false;
                        }
                    } else {
                        // Company doesn't exists
                        r = getMessage(2, 4);//"{\"success\": false, \"errorcode\": \"e06\"}";
                        managePerm = false;
                    }


                    if(success){
                        cj = new JSONObject();
                        cj.put("create", managePerm);
                        cj.put("view", true);
                        JSONObject ret = new JSONObject();
                        ret.put("success", success);
                        ret.put("permissions", cj);
                        r = ret.toString();
                    }
                }
            } else {
                // Insuffincient Data
                r = getMessage(2, 1);//"{\"success\": false, \"errorcode\": \"e01\"}";
            }
		} catch (SQLException ex) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAction.class.getName()).log(Level.SEVERE, "SQL Exception While Checking Create Project Permission", ex);
            throw ServiceException.FAILURE(r, ex);
        } catch (JSONException ex) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAction.class.getName()).log(Level.SEVERE, "JSON Exception While Checking Create Project Permission", ex);
            throw ServiceException.FAILURE(r, ex);
        }
		return r;
    }

    public static String getMessage(int type, int mode){
        String r = "";
        String temp = "";
        switch(type){
            case 1:     // success messages
                temp = "m" + String.format("%02d", mode);
                r = "{\"success\": true, \"infocode\": \"" + temp + "\"}";
                break;
            case 2:     // error messages
                temp = "e" + String.format("%02d", mode);
                r = "{\"success\": false, \"errorcode\": \"" + temp + "\"}";
                break;
        }
        return r;
    }
    public static String getSubdomain(Connection conn, String companyid) throws ServiceException{
        String subdomain = "";
        try{
            PreparedStatement p = conn.prepareStatement("SELECT subdomain FROM company WHERE companyid = ?");
            p.setString(1, companyid);
            ResultSet r = p.executeQuery();
            if(r.next()){
                subdomain = r.getString("subdomain");
            }
        } catch(SQLException e){
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return subdomain;
    }

    public static boolean isSuperCompany(Connection conn, String userid, String companyid) throws ServiceException, SQLException{
        String sql = "SELECT c.companyid, c.subdomain FROM users u INNER JOIN company c ON u.companyid = c.companyid WHERE u.userid = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, userid);
        ResultSet rs = pstmt.executeQuery();
        if(rs.next()){
            String cid = rs.getString("companyid");
            if(StringUtil.equal(cid, companyid) && StringUtil.equal(getSubdomain(conn, companyid), "admin")){
                return true;
            }
        }
        return false;
    }

    public static String getSubscriptionRequests(Connection conn, JSONObject jobj) {
        String r = "";
        try{
            if(jobj.has("userid") && jobj.has("companyid")){
                String userid = jobj.getString("userid");
                String companyid = jobj.getString("companyid");

                if(StringUtil.isNullOrEmpty(userid) && StringUtil.isNullOrEmpty(companyid)) {
                    // Insuffincient Data
                    r = getMessage(2, 1);//"{\"success\": false, \"errorcode\": \"e01\"}";
                } else {
                    if(DashboardHandler.isDeskeraAdmin(conn, userid)){

                        if(isSuperCompany(conn, userid, companyid)){

                            StringBuilder sbRequests = new StringBuilder();
                            StringBuilder sb = new StringBuilder();
                            sbRequests = DashboardHandler.getSubscriptionRequests(conn, userid);
                            sb.append("<div class=\"statuspanelouter\"><div class=\"statuspanelinner\">");
                            if (sbRequests.length() > 0)
                                sb.append(sbRequests);
                            else
                                sb.append("No new subscription requests");
                            sb.append("</div></div>");
                            r = sb.toString();
                            JSONObject jo = new JSONObject();
                            jo.put("updates", r);
                            JSONObject jtemp = new JSONObject();
                            jtemp.put("success", true);
                            jtemp.put("data", jo.toString());
                            r = jtemp.toString();
                        } else {
                            r = getMessage(2, 10);//New Case - Invalid SuperCompany ID
                        }

                    } else {
                        r = getMessage(2, 11);//New Case - Invalid SuperUser ID
                    }
                }
            } else {
                // Insuffincient Data
                r = getMessage(2, 1);//"{\"success\": false, \"errorcode\": \"e01\"}";
            }

        } catch (JSONException ex) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAction.class.getName()).log(Level.SEVERE, "JSON Exception While Fetching Subscription Requests", ex);
            throw ServiceException.FAILURE(r, ex);

        } catch (SQLException ex) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAction.class.getName()).log(Level.SEVERE, "SQL Exception While Fetching Subscription Requests", ex);
            throw ServiceException.FAILURE(r, ex);

        } finally {
            return r;
        }
    }

    public static String manageSubscriptionStatus(Connection conn, JSONObject jobj) {
        String r = "";
        try{
           String comp = isCompanyExists(conn, jobj);
            JSONObject cj = new JSONObject(comp);
            JSONObject j = new JSONObject();
            if(cj.has("infocode") && cj.getString("infocode").equals("m01")){
                String res = SuperUserHandler.manageSubscriptions(conn, jobj.getString("companyid"), "0", jobj, jobj.getInt("mode"));
                Pattern p = Pattern.compile("<[^>]*>");
                Matcher m = p.matcher(res);
                JSONObject jtemp = new JSONObject();
                if(m.find()){
                    jtemp.put("success", true);
                } else {
                    jtemp.put("success", true);
                }
                j.put("data", res);
                jtemp.put("data", j.toString());
                r = jtemp.toString();
            } else {
                // Company doesn't exists or Insufficient Data
                r = cj.toString();
            }
        } catch (JSONException ex) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAction.class.getName()).log(Level.SEVERE, "JSON Exception While Fetching/Updating Subscription Status", ex);
            throw ServiceException.FAILURE(r, ex);
        } catch (ServiceException ex) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAction.class.getName()).log(Level.SEVERE, "Service Exception While Fetching/Updating Subscription Status", ex);
            throw ServiceException.FAILURE(r, ex);
        } finally {
            return r;
        }
    }

    public static String deleteCompany(Connection conn, JSONObject jobj) {
        String r = getMessage(1, 15);
        try {
            String comp = isCompanyExists(conn, jobj);
            JSONObject cj = new JSONObject(comp);
            if (cj.has("infocode") && cj.getString("infocode").equals("m01")) {

                 String companyID = jobj.getString("companyid");
                 PreparedStatement pstmt = conn.prepareStatement("DELETE FROM ccdata WHERE referenceid IN (SELECT projectid FROM project WHERE companyid = ?)");
                 pstmt.setString(1, companyID);
                 pstmt.executeUpdate();

                 pstmt = conn.prepareStatement("DELETE FROM ccmetadata WHERE companyid = ?");
                 pstmt.setString(1, companyID);
                 pstmt.executeUpdate();

                 pstmt = conn.prepareStatement("DELETE FROM project WHERE companyid = ?");
                 pstmt.setString(1, companyID);
                 pstmt.executeUpdate();

                 pstmt = conn.prepareStatement("DELETE FROM actionlog WHERE companyid = ?");
                 pstmt.setString(1, companyID);
                 pstmt.executeUpdate();

                 pstmt = conn.prepareStatement("DELETE FROM apiresponse WHERE companyid = ?");
                 pstmt.setString(1, companyID);
                 pstmt.executeUpdate();

                 pstmt = conn.prepareStatement("DELETE FROM importlog WHERE companyid = ?");
                 pstmt.setString(1, companyID);
                 pstmt.executeUpdate();

                 pstmt = conn.prepareStatement("DELETE FROM company WHERE companyid = ?");
                 pstmt.setString(1, companyID);
                 pstmt.executeUpdate();

            } else {
                // Company doesn't exists or Insufficient Data
                r = cj.toString();
            }
        } catch (JSONException ex) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAction.class.getName()).log(Level.SEVERE, "JSON Exception while deleting company", ex);
            throw ServiceException.FAILURE(r, ex);
        } catch (ServiceException ex) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAction.class.getName()).log(Level.SEVERE, "Service Exception while deleting company", ex);
            throw ServiceException.FAILURE(r, ex);
        } finally {
            return r;
        }
    }

    private String deactivateCompany(Connection conn, JSONObject jobj) {
        String r = getMessage(1, 16);
        try {
            String comp = isCompanyExists(conn, jobj);
            JSONObject cj = new JSONObject(comp);
            if (cj.has("infocode") && cj.getString("infocode").equals("m01")) {

                String companyID = jobj.getString("companyid");
                PreparedStatement pstmt = conn.prepareStatement("UPDATE company SET activated = false WHERE companyid = ?");
                pstmt.setString(1, companyID);
                pstmt.executeUpdate();

            } else {
                // Company doesn't exists or Insufficient Data
                r = cj.toString();
            }
        } catch (JSONException ex) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAction.class.getName()).log(Level.SEVERE, "JSON Exception while deactivating company", ex);
            throw ServiceException.FAILURE(r, ex);
        } catch (ServiceException ex) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(RemoteAction.class.getName()).log(Level.SEVERE, "Service Exception while deactivating company", ex);
            throw ServiceException.FAILURE(r, ex);
        } finally {
            return r;
        }
    }

    public static boolean isCompanyActivated(Connection conn, JSONObject jobj) throws ServiceException {
        boolean result = false;
        try {
            String comp = isCompanyExists(conn, jobj);
            JSONObject cj = new JSONObject(comp);
            if (cj.has("infocode") && cj.getString("infocode").equals("m01")) {

                String sql = "SELECT activated FROM company WHERE companyid = ?";
                String param = jobj.getString("companyid");
                PreparedStatement p = conn.prepareStatement(sql);
                p.setString(1, param);
                ResultSet rs = p.executeQuery();
                if (rs.next() && rs.getBoolean("activated")) {
                    result = true;
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(RemoteAction.class.getName()).log(Level.SEVERE, "JSON Exception while checking if company is activated", ex);
            throw ServiceException.FAILURE("", ex);
        } catch (SQLException ex) {
            Logger.getLogger(RemoteAction.class.getName()).log(Level.SEVERE, "SQL Exception while checking if company is activated", ex);
            throw ServiceException.FAILURE("", ex);
        }
        return result;
    }

// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
