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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.krawler.common.service.ServiceException;
import com.krawler.database.DbPool;
import com.krawler.database.DbPool.Connection;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
public class dateFormatHandlers {
    public static String getDateFormat(Connection conn,int id) throws ServiceException{
        String query = "select javaform from dateformat where id = ?";
        java.sql.ResultSet rs = null;
        String retstr="";
        java.sql.PreparedStatement pstmt = null;
            try {
                    pstmt = conn.prepareStatement(query);
                    pstmt.setInt(1,id);
                    rs = pstmt.executeQuery();
                    while (rs.next()) {
                        retstr = rs.getString("javaform");
                    }
                    rs.close();
                    //retstr = jobj.toString();
            } catch (java.sql.SQLException e) {
                    throw ServiceException.FAILURE("ProfileHandler.getCmpCurrFromProj", e);
            } finally {
                    DbPool.closeStatement(pstmt);
            }
            return retstr;
    }
    public static String getScriptDateFormat(Connection conn,int id) throws ServiceException{
        String query = "select scriptform from dateformat where id = ?";
        java.sql.ResultSet rs = null;
        String retstr="";
        java.sql.PreparedStatement pstmt = null;
        try {
                pstmt = conn.prepareStatement(query);
                pstmt.setInt(1,id);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    retstr = rs.getString("scriptform");
                }
                rs.close();
                //retstr = jobj.toString();
        } catch (java.sql.SQLException e) {
                throw ServiceException.FAILURE("dateFormatHandler:getScriptDateFormat", e);
        } finally {
                DbPool.closeStatement(pstmt);
        }
        return retstr;
    }
    public static String getUserPrefreanceDate(Connection conn,Date dt,int prefid) throws ServiceException, ParseException{
        SimpleDateFormat formatter = new SimpleDateFormat(getDateFormat(conn,prefid));
        return formatter.format(dt);
    }
    public static String getUserPrefreanceDate(String format,Date dt){
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(dt);
    }
    public static String sampleDateString(Connection conn) throws ServiceException{
        String result = "";
        String query = "select javaform,id,scriptform,sseppos from dateformat";
        java.sql.ResultSet rs = null;
        String retstr="";
        Date dt = new Date();
        java.sql.PreparedStatement pstmt = null;
        JSONObject res = new JSONObject();
        try {
                pstmt = conn.prepareStatement(query);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    JSONObject temp = new JSONObject();
                    temp.put("name", getUserPrefreanceDate(rs.getString("javaform"),dt));
                    temp.put("id",rs.getInt("id"));
                    temp.put("dateformat",rs.getString("scriptform"));
                    temp.put("seppos",rs.getString("sseppos"));
                    res.append("data", temp);
                }
                rs.close();
                result = res.toString();
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("ProfileHandler.getCmpCurrFromProj", ex);
        } catch (java.sql.SQLException e) {
            throw ServiceException.FAILURE("ProfileHandler.getCmpCurrFromProj", e);
        } finally {
                DbPool.closeStatement(pstmt);
        }

        return result;
    }
    public static String getUserDateFormat(Connection conn,String userid) throws ServiceException{
        String dateFormat = "";
         String query = "select dateformat.javaform from dateformat inner join users on users.dateformat = dateformat.id where userid = ?";
        java.sql.ResultSet rs = null;
        Date dt = new Date();
        java.sql.PreparedStatement pstmt = null;
         try {
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1,userid);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    dateFormat = rs.getString("javaform");
                }
                rs.close();
        } catch (java.sql.SQLException e) {
            throw ServiceException.FAILURE("ProfileHandler.getCmpCurrFromProj", e);
        } finally {
                DbPool.closeStatement(pstmt);
        }
        return dateFormat;
    }
    public static int getUserDateFormatid(Connection conn,String userid)throws ServiceException{
        int dateFormatid = 1;
         String query = "select dateformat from users where userid = ?";
        java.sql.ResultSet rs = null;
        Date dt = new Date();
        java.sql.PreparedStatement pstmt = null;
         try {
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1,userid);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    dateFormatid = rs.getInt("dateformat");
                }
                rs.close();
        } catch (java.sql.SQLException e) {
            throw ServiceException.FAILURE("ProfileHandler.getCmpCurrFromProj", e);
        } finally {
                DbPool.closeStatement(pstmt);
        }
        return dateFormatid;
    }
    public static String getOnlyDateFormats(Connection conn) throws ServiceException{
        String result = "";
        String query = "select javaform, id, scriptform, sseppos from dateformat where onlydateformat = 1";
        java.sql.ResultSet rs = null;
        Date dt = new Date();
        java.sql.PreparedStatement pstmt = null;
        JSONObject res = new JSONObject();
        try {
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                JSONObject temp = new JSONObject();
                temp.put("name", getUserPrefreanceDate(rs.getString("javaform"), dt));
                temp.put("id", rs.getInt("id"));
                temp.put("dateformat", rs.getString("scriptform"));
                temp.put("seppos", rs.getString("sseppos"));
                res.append("data", temp);
            }
            rs.close();
            result = res.toString();
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("dateFormatHandlers.getOnlyDateFormat : "+ex.getMessage(), ex);
        } catch (java.sql.SQLException ex) {
            throw ServiceException.FAILURE("dateFormatHandlers.getOnlyDateFormat : "+ex.getMessage(), ex);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return result;
    }
            
    public static void main(String[] a){
        try{
            Connection conn = DbPool.getConnection();
            String df="yyyy-MM-dd HH:mm:ss";
            Date today = new Date();
            getUserDateFormatid(conn,"56a08913-f7df-4409-93fe-ba5c8f31f6fe");
            getUserDateFormat(conn,"56a08913-f7df-4409-93fe-ba5c8f31f6fe");
            String result;
            SimpleDateFormat formatter;
            for(int i=1;i<=16;i++){
               result = getUserPrefreanceDate(conn,today,i);
   //            result = getUserPrefreanceDateString(conn,today,i);
                System.out.println(today.toString()+"              "+result);
            }
            conn.close();
            
            
        }catch(Exception e){
                System.out.println(e);
        }
    }

}
