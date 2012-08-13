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
import com.krawler.common.timezone.Timezone;
import com.krawler.database.DbPool;
import com.krawler.database.DbPool.Connection;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Abhay
 */
public class SuperUserHandler {

    public static String manageSubscriptions(Connection conn, String companyid, String loginid, JSONObject jobj, int mode) throws ServiceException{
        String msg = "";
        PreparedStatement pstmt1 = null;
        PreparedStatement pstmt2 = null;
        String data = "";
        try {
            pstmt1 = conn.prepareStatement("SELECT companyname,subdomain,createdon,concat(fname, ' ', lname) AS name,users.emailid as email " +
                            "FROM company INNER JOIN users ON company.creator = users.userid WHERE company.companyid=?");
            pstmt1.setString(1, companyid);
            ResultSet r = pstmt1.executeQuery();
            if (r.next()) {
                data = "<TR>" +
                    "<TD class='projectTD'>" + r.getString("companyname") + "</TD>" +
                    "<TD class='projectTD'>" + r.getString("subdomain") + "</TD>" +
                    "<TD class='projectTD'>" + Timezone.toUserTimezone(conn, r.getString("createdon"), loginid) + "</TD>" +
                    "<TD class='projectTD'>" + r.getString("email") + "</TD>" +
                    "<TD class='projectTD'>" + r.getString("name") + "</TD>" +
                    "</TR>";
                msg += "Company Details<table style='margin: auto; font-size: 0.9em; width: 95%;' class='kwlAdminTab'>" +
                    "<thead><tr>" +
                    "<th><b>Company Name</b></th>" +
                    "<th><b>Subdomain</b></th>" +
                    "<th><b>Created On</b></th>" +
                    "<th><b>e-mail id</b></th>" +
                    "<th><b>Created By</b></th>" +
                    "</tr></thead><tbody>" + data + "</tbody></table>";
                msg += "Subscription Status";
            }
            pstmt2 = conn.prepareStatement("SELECT moduleid, modulename, nickname FROM companymodules");
            ResultSet rs2 = pstmt2.executeQuery();
            if(mode == 2){
                int subVal = 2;     // Default value for a subscribed module
                while(rs2.next()){
                    int moduleId = rs2.getInt("moduleid");
                    if(Boolean.parseBoolean(jobj.getString(rs2.getString("nickname")))){

                        int subModVal = 0;
                        PreparedStatement psSubMod1 = conn.prepareStatement(" SELECT submoduleid, submodulename, subnickname FROM companysubmodules WHERE parentmoduleid = ? ");
                        psSubMod1.setInt(1, moduleId);
                        ResultSet rsSubMod1 = psSubMod1.executeQuery();
                        while(rsSubMod1.next()) {
                            if(Boolean.parseBoolean(jobj.getString(rsSubMod1.getString("subnickname")))) {
                                subModVal += Math.pow(2, rsSubMod1.getInt("submoduleid"));
                            }
                        }
                        DashboardHandler.changeSubscription(conn, companyid, moduleId, subVal, subModVal);

                    } else {
                        DashboardHandler.changeSubscription(conn, companyid, moduleId, 0, 0);
                    }
                }
            }
            data = "";
            rs2.beforeFirst();
            while(rs2.next()){
                String chkBx = "";
                if(DashboardHandler.isSubscribed(conn, companyid, rs2.getString("nickname"))){
                    chkBx = "<INPUT type='checkbox' checked name='" + rs2.getString("nickname") + "'>";
                } else {
                    chkBx = "<INPUT type='checkbox' name='" + rs2.getString("nickname") + "'>";
                }
                data += "<TR><TD>" + rs2.getString("modulename") + "</TD><TD style='width: 30px; text-align: center'>" + chkBx + "</TD>";

                PreparedStatement psSubMod2 = conn.prepareStatement(" SELECT submoduleid, submodulename, subnickname " +
                                                                " FROM companysubmodules WHERE parentmoduleid = ? ");
                psSubMod2.setString(1, rs2.getString("moduleid"));
                ResultSet rsSubMod2 = psSubMod2.executeQuery();
                while(rsSubMod2.next()) {
                    if(DashboardHandler.isSubscribed(conn, companyid, rsSubMod2.getString("subnickname"))) {
                        chkBx = "<INPUT type='checkbox' checked name='" + rsSubMod2.getString("subnickname") + "'>";

                    } else {
                        chkBx = "<INPUT type='checkbox' name='" + rsSubMod2.getString("subnickname") + "'>";
                    }
                    data += "<TR><TD style='padding-left:50px;'>" + rsSubMod2.getString("submodulename") + "</TD><TD style='width: 30px; text-align: center'>" + chkBx + "</TD><TR>";
                }
                data += "<TR>";
            }
            data += "<TR><TD class='noBorder'></TD>" +
                "<TD class='noBorder'><img src='../../images/button.png' style='cursor: pointer; float: right; margin-top: 10px;' onclick = 'subscribemodeule()'/></TD></TR>";
            msg += "<TABLE id='companySubscriptionStatusTable' style='margin: 20px auto auto auto; width: 30%' class='kwlAdminTab'>" + data + "</TABLE></FORM>";
        } catch (SQLException e) {
            msg = e.getMessage();
            throw ServiceException.FAILURE("SuperUser: "+e.getMessage(), e);
        } catch (ServiceException e){
            msg = e.getMessage();
            throw ServiceException.FAILURE("SuperUser: "+e.getMessage(), e);
        } catch (JSONException e){
            msg = e.getMessage();
            throw ServiceException.FAILURE("SuperUser: "+e.getMessage(), e);
        } catch (Exception e){
            msg = e.getMessage();
            throw ServiceException.FAILURE("SuperUser: "+e.getMessage(), e);
        } finally {
            DbPool.closeStatement(pstmt1);
            DbPool.closeStatement(pstmt2);
            return msg;
        }
    }

    public static String manageSubsctiptions(Connection conn, HttpServletRequest request) throws ServiceException{
        String msg = "";
        try {
            String companyid = request.getParameter("companyid");
            String loginid = request.getParameter("loginid");
            int mode = Integer.parseInt(request.getParameter("mode"));

            PreparedStatement pstmt = conn.prepareStatement("SELECT moduleid, modulename, nickname FROM companymodules");
            ResultSet rs = pstmt.executeQuery();
            JSONObject jobj = new JSONObject();
            
            while (rs.next()) {
                String moduleNickName = rs.getString("nickname");
                if (!jobj.has(moduleNickName)) {
                    jobj.put(moduleNickName, request.getParameter(moduleNickName));

                    PreparedStatement psSubMod1 = conn.prepareStatement(" SELECT submoduleid, submodulename, subnickname FROM companysubmodules WHERE parentmoduleid = ? ");
                    psSubMod1.setInt(1, rs.getInt("moduleid"));
                    ResultSet rsSubMod1 = psSubMod1.executeQuery();
                    
                    while (rsSubMod1.next()) {
                        String subModuleNickName = rsSubMod1.getString("submodulnickname");
                        if (!jobj.has(subModuleNickName)) {
                            jobj.put(subModuleNickName, request.getParameter(subModuleNickName));
                        }
                    }
                }
            }
            msg = manageSubscriptions(conn, companyid, loginid, jobj, mode);
            pstmt.close();
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("SuperUser: "+ex.getMessage(), ex);
        } catch (SQLException ex) {
            throw ServiceException.FAILURE("SuperUser: "+ex.getMessage(), ex);
        } finally{
            return msg;
        }
    }
}
