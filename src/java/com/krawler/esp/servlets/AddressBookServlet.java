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

import com.krawler.common.locale.MessageSourceProxy;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.KWLErrorMsgs;
import com.krawler.common.util.StringUtil;
import com.krawler.database.DbPool;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.database.DbPool.Connection;
import com.krawler.esp.handlers.AuthHandler;
import com.krawler.utils.json.KWLJsonConverter;
import com.krawler.utils.json.base.JSONException;

public class AddressBookServlet extends HttpServlet {

	private static final long serialVersionUID = 3067877679580064390L;

	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Connection conn = null;
        String outMsg = "";
        int action = Integer.parseInt(request.getParameter("action"));
        boolean exc = false;
        try {
            response.setContentType("text/html;charset=UTF-8");
            String userid = AuthHandler.getUserid(request);
            conn = DbPool.getConnection();
            if (action != 3) {
                String id = request.getParameter("id");
                String phone = "";
                String name = "";
                String emailid = "";
                String address = "";
                switch (action) {
                    case 0:
                        phone = StringUtil.serverHTMLStripper(request.getParameter("phone").toString());
                        name = StringUtil.serverHTMLStripper(request.getParameter("name").toString());
                        emailid = StringUtil.serverHTMLStripper(request.getParameter("emailid").toString());
                        address = StringUtil.serverHTMLStripper(request.getParameter("address").toString());
                        if(!StringUtil.isNullOrEmpty(emailid)){
                                emailid  = emailid.trim().replace(" ", "+");
                        }
                        if(StringUtil.isNullOrEmpty(name) || StringUtil.isNullOrEmpty(emailid)) {
                            Logger.getLogger(AddressBookServlet.class.getName()).log(Level.SEVERE, null, KWLErrorMsgs.errProccessingData);
                            break;
                        }
                        if(CheckAddressBook(conn, userid, name, emailid, phone, address).equals("")){
                            outMsg = "{\"success\":\"true\",\"data\":\""+MessageSourceProxy.getMessage("pm.contacts.newcontact.exists", null, request) +"\"}";//"Contact with this email id is already present";
                        }else{
                            outMsg = "{\"success\":\"true\",\"data\":\"\"}";
                        }
                        break;
                    case 1:
                        com.krawler.utils.json.base.JSONObject jbj = new com.krawler.utils.json.base.JSONObject();
                        DeleteAddressBook(conn, id, userid);
                        jbj.put("valid", "true");
                        jbj.put("data","");
                        //response.getWriter().println(jbj.toString());
                        outMsg = jbj.toString();
                        break;
                    case 2:
                        phone = StringUtil.serverHTMLStripper(request.getParameter("phone"));
                        name = StringUtil.serverHTMLStripper(request.getParameter("name"));
                        emailid = StringUtil.serverHTMLStripper(request.getParameter("emailid"));
                        address = StringUtil.serverHTMLStripper(request.getParameter("address"));
                        if(!StringUtil.isNullOrEmpty(emailid)){
                                emailid  = emailid.trim().replace(" ", "+");
                        }
                        if(StringUtil.isNullOrEmpty(name) || StringUtil.isNullOrEmpty(emailid)) {
                            Logger.getLogger(AddressBookServlet.class.getName()).log(Level.SEVERE, null, KWLErrorMsgs.errProccessingData);
                            break;
                        }
                        if(isEditMailAvailable(conn, userid, emailid, id)){
                            outMsg = "{\"success\":\"true\",\"data\":\""+MessageSourceProxy.getMessage("pm.contacts.newcontact.exists", null, request)+"\"}";//"Contact with this email id is already present";
                        }else{
                            UpdateAddressBook(conn, id, name, phone, emailid, address, userid);
                            outMsg = "{\"success\":\"true\",\"data\":\"\"}";
                        }
                        break;
                    case 4:
                        String contactid = request.getParameter("contactid");
                        com.krawler.utils.json.base.JSONObject jbj1 = new com.krawler.utils.json.base.JSONObject();
                        jbj1.put("valid", "true");
                        jbj1.put("data"," "+getContactDetails(conn, contactid));
                        //response.getWriter().println(jbj.toString());
                        outMsg = jbj1.toString();
                        break;
                    default:
                        break;
                }
                conn.commit();
            } else {
                outMsg = getAddressBook(conn, userid);
                //out.print(getAddressBook(conn, userid));
            }



        } catch (JSONException ex) {
            exc = true;
        } catch (SQLException ex) {
            exc = true;
            DbPool.quietRollback(conn);
        } catch (ServiceException ex) {
            exc = true;
            DbPool.quietRollback(conn);
        } catch (SessionExpiredException ex) {
            exc = true;
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
            if(exc){
                if(action ==1 || action ==4){
                    outMsg = "{\"valid\":\"true\",\"data\":\""+KWLErrorMsgs.errOpFail+"\"}";//outMsg + "Error";
                }else{
                    outMsg = "{\"success\":\"true\",\"data\":\""+KWLErrorMsgs.errOpFail+"\"}";//outMsg + "Error";
                }
            }
            response.getWriter().println(outMsg);
        }
    }
    public static boolean isEditMailAvailable(Connection conn,String userid,String emailid, String id)throws ServiceException{
        boolean result = false;

        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("SELECT COUNT(*) AS count FROM addressbook WHERE emailid=? and userid = ? and contactid!=?");
            pstmt.setString(1, emailid);
            pstmt.setString(2, userid);
            pstmt.setString(3, id);
            rs = pstmt.executeQuery();
            rs.next();
            if(rs.getInt("count")>0){
                result = true;
            }
        }catch (SQLException e) {
            throw ServiceException.FAILURE("sigunup.useridIsAvailable", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return result;
    }
    public static boolean isMailAvailable(Connection conn,String userid,String emailid)throws ServiceException{
        boolean result = false;

        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("SELECT COUNT(*) AS count FROM addressbook WHERE emailid=? and userid = ?");
            pstmt.setString(1, emailid);
            pstmt.setString(2, userid);
            rs = pstmt.executeQuery();
            rs.next();
            if(rs.getInt("count")>0){
                result = true;
            }
        }catch (SQLException e) {
            throw ServiceException.FAILURE("sigunup.useridIsAvailable", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return result;
    }
    public static String CheckAddressBook(Connection conn, String userid, String name, String emailid, String phone, String address) throws ServiceException, SQLException, JSONException {
        String contactid = "";
        if(!StringUtil.isNullOrEmpty(name) && !StringUtil.isNullOrEmpty(phone) && !StringUtil.isNullOrEmpty(userid)){
            if(!isMailAvailable(conn,userid,emailid)){
                contactid = getContactID(conn, name, phone, emailid, address, userid);
                if (StringUtil.isNullOrEmpty(contactid)) {
                    contactid = UUID.randomUUID().toString();
                    DbUtil.executeUpdate(conn, "insert into addressbook(contactid,name,emailid,phone,address,userid)" + " values ( ?, ?, ?, ?, ? ,?);",
                            new Object[]{contactid, name, emailid, phone, address, userid});
                }
            }
        }
        return contactid;
     }
    public static String InsertinAddressBook(Connection conn, String userid, String name, String emailid, String phone, String address) throws ServiceException, SQLException, JSONException {
        String contactid = "";
        String str = getContactID(conn, name, phone, emailid, address, userid);
        if (!StringUtil.isNullOrEmpty(str)){
                str=userid;
        }
            if(!StringUtil.isNullOrEmpty(name) && !StringUtil.isNullOrEmpty(emailid) && !StringUtil.isNullOrEmpty(userid)){
                if(!isMailAvailable(conn,userid,emailid)){
                contactid = getContactID(conn, name, phone, emailid, address, userid);
                if (StringUtil.isNullOrEmpty(contactid)) {
                    contactid = UUID.randomUUID().toString();
                    DbUtil.executeUpdate(conn, "insert into addressbook(contactid,name,emailid,phone,address,userid)" + " values ( ?, ?, ?, ?, ? ,?);",
                            new Object[]{contactid, name, emailid, phone, address, userid});
                }
            }
        }
        return str;
    }
    public static int saveContactByOperationType(Connection conn, int type, String userid, String name, String emailid, String phone, String address) throws ServiceException, SQLException, JSONException {
        String contactid = "";
        String queryString = "";
        Object obj[] = null;
        int row = 0;
        switch (type) {
            case 1: //Only for Unique Records do not replace
                contactid = getContactID(conn, emailid, userid);
                if (StringUtil.isNullOrEmpty(contactid)) {
                    contactid = UUID.randomUUID().toString();
                } else {
                    break;
                }
                queryString = "insert into addressbook(contactid,name,emailid,phone,address,userid)" + " values ( ?, ?, ?, ?, ? ,?);";
                obj = new Object[]{contactid, name, emailid, phone, address, userid};
                row = DbUtil.executeUpdate(conn, queryString, obj);
                break;
            case 2: //duplicate entry
                contactid = UUID.randomUUID().toString();
                queryString = "insert into addressbook(contactid,name,emailid,phone,address,userid)" + " values ( ?, ?, ?, ?, ? ,?);";
                obj = new Object[]{contactid, name, emailid, phone, address, userid};
                row = DbUtil.executeUpdate(conn, queryString, obj);
                break;
            case 3: // Replace duplicate entry
                contactid = getContactID(conn, emailid, userid);
                if (StringUtil.isNullOrEmpty(contactid)) { //replace means update if contact exists
                    contactid = UUID.randomUUID().toString();
                    queryString = "insert into addressbook(contactid,name,emailid,phone,address,userid)" + " values ( ?, ?, ?, ?, ? ,?);";
                    obj = new Object[]{contactid, name, emailid, phone, address, userid};
                } else { // insert if contact not exists
                    queryString = "update addressbook set name = ? , phone = ? , address = ? where emailid = ? and userid = ?;";
                    obj = new Object[]{name, phone, address, emailid, userid};
                }
                row = DbUtil.executeUpdate(conn, queryString, obj);
                if (row > 1) {
                    row = 1;
                }
                break;
        }
        return row;
    }
    public static String GetDupinAddressBook(Connection conn, String userid, String name, String emailid, String phone, String address) throws ServiceException, JSONException {

        String contactid = "";
        String str="";
        contactid = getContactID(conn, name, phone, emailid, address, userid);
        if (!StringUtil.isNullOrEmpty(contactid)){
                str=userid;
            }
        return str;
    }
    
    public static String insertContact(Connection conn, String uid, String name, String mailid, String phone, String address, String recid) throws ServiceException{
        String ret = "";
        String contactid = UUID.randomUUID().toString();
        int temp = DbUtil.executeUpdate(conn, "INSERT INTO addressbook(contactid,name,emailid,phone,address,userid,recid) VALUES (?,?,?,?,?,?,?);",
                    new Object[]{contactid, name, mailid, phone, address, uid, Integer.parseInt(recid)});
        if(temp == 1)
            ret = contactid;
        return ret;
    }
    public static int updateContact(Connection conn, String uid, String name, String mailid, String phone, String address, String recid, String cid) throws ServiceException{
        int temp = DbUtil.executeUpdate(conn, "UPDATE addressbook SET name=? ,emailid=?,phone=?,address=?,userid=?,recid=? WHERE contactid = ?;",
                    new Object[]{name, mailid, phone, address, uid, recid, cid});
        return temp;
    }
    public static String chkRecId(Connection conn, String uid, String recid){
        String id = "";
        try{
            PreparedStatement pstmt = conn.prepareStatement("SELECT contactid FROM addressbook WHERE userid = ? AND recid = ?");
            pstmt.setString(1, uid);
            pstmt.setInt(2, Integer.parseInt(recid));
            ResultSet rs = pstmt.executeQuery();
            if(rs.next())
                id = rs.getString("contactid");
        } catch (ServiceException ex) {
            Logger.getLogger(AddressBookServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch(SQLException e){

        }
        return id;
    }
    public static String chkDuplicateRec(Connection conn,String uid, String name, String email, String pno, String add, String recid){
        String ret = "";
        try{
            PreparedStatement pstmt = conn.prepareStatement("SELECT contactid FROM addressbook " +
                    "WHERE name=? AND userid=? AND emailid=? AND phone=? AND address=? AND recid=?");
            pstmt.setString(1, name);
            pstmt.setString(2, uid);
            pstmt.setString(3, email);
            pstmt.setString(4, pno);
            pstmt.setString(5, add);
            pstmt.setInt(6, Integer.parseInt(recid));
            ResultSet r = pstmt.executeQuery();
            if(r.next())
                ret = r.getString("contactid");
        } catch (ServiceException ex) {
            Logger.getLogger(AddressBookServlet.class.getName()).log(Level.SEVERE, null, ex);
            ret = "";
        } catch(SQLException e){
            Logger.getLogger(AddressBookServlet.class.getName()).log(Level.SEVERE, null, e);
            ret = "";
        }
        return ret;
    }
    public static void UpdateAddressBook(Connection conn, String contactid, String name, String phone, String emailid, String address, String userid) throws ServiceException, SQLException {
        DbUtil.executeUpdate(conn, "Update  addressbook set name =? ,emailid = ?,phone = ?,address =? where contactid=? and userid =? ",
                new Object[]{name, emailid, phone, address, contactid, userid});
    }

    public static void DeleteAddressBook(Connection conn, String contactid, String userid) throws ServiceException, SQLException {
        String[] ids = contactid.split(",");
        for(int i=0;i<ids.length;i++){
            DbUtil.executeUpdate(conn, "delete from addressbook where contactid=? and userid=?",
                    new Object[]{ids[i], userid});
        }
    }

    public static String getAddressBook(Connection conn, String userid) throws ServiceException {
        String result = null;
        PreparedStatement pstmt = null;
        KWLJsonConverter KWL = new KWLJsonConverter();
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement("select contactid as id ,name ,phone,emailid,address from addressbook where userid = ? ");
            pstmt.setString(1, userid);
            rs = pstmt.executeQuery();
            result = KWL.GetJsonForGrid(rs);
            pstmt.close();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("SuperAdminHandler.getSUAdminData",
                    e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return result;


    }

    public static String getContactID(Connection conn, String name, String phone, String emailid, String address, String userid) throws ServiceException {
        String contactid ="";
//        DbResults rs = DbUtil.executeQuery(conn, "select Count(*) from addressbook where name = ? and phone= ? and emailid = ? and address = ? and userid=?", new Object[]{name, phone, emailid, address, userid});
//        rs.next();
//        if (rs.getInt(1) != 0) {
//            rs = DbUtil.executeQuery(conn, "select contactid from addressbook where name = ? and phone= ? and emailid = ? and address = ? and userid=?", new Object[]{name, phone, emailid, address, userid});
//            rs.next();
//            contactid = rs.getString(1);
//        }
        DbResults rs= DbUtil.executeQuery(conn, "select contactid from addressbook where name = ? and phone= ? and emailid = ? and address = ? and userid=?", new Object[]{name, phone, emailid, address, userid});
        if(rs.next()){
            contactid =rs.getString(1);
        }

        return contactid;

    }
    public static String getContactID(Connection conn, String emailid, String userid) throws ServiceException {
        String contactid = "";
        DbResults rs = DbUtil.executeQuery(conn, "select contactid from addressbook where emailid = ? and userid=?", new Object[]{emailid, userid});
        if (rs.next()) {
            contactid = rs.getString(1);
        }
        return contactid;

    }
   public static String getContactDetails(Connection conn,String contactid)throws SQLException, ServiceException {
        String result = null;
        PreparedStatement pstmt = null;
        KWLJsonConverter KWL = new KWLJsonConverter();
        ResultSet rs = null;
        pstmt = conn.prepareStatement("select contactid as id ,name ,phone,emailid,address from addressbook where contactid = ? ");
        pstmt.setString(1, contactid);
        rs = pstmt.executeQuery();
        result = KWL.GetJsonForGrid(rs);
        pstmt.close();

       return result;
   }
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }
    // </editor-fold>
}
