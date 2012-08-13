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
import com.krawler.common.util.StringUtil;
import com.krawler.database.DbPool.Connection;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class APICallHandler {

    private static String apistr = "remoteapi.jsp";

    public static JSONObject callApp (Connection conn, String appURL, JSONObject jData,String companyid, String action) {
        JSONObject resObj = new JSONObject();
        boolean result = false;
        try {
            PreparedStatement pstmt = null;
            String uid = UUID.randomUUID().toString();
            String sql = " INSERT INTO apiresponse (id, companyid, request, status) VALUES (?, ?, ?, ?) ";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, uid);
            pstmt.setString(2, companyid);
            pstmt.setString(3, "action=" + action + "&data=" + jData.toString());
            pstmt.setInt(4, 0);
            pstmt.executeUpdate();

            String res = "{}";
            InputStream iStream = null;
            try {
                String strSandbox = appURL + apistr;
                URL u = new URL(strSandbox);
                URLConnection uc = u.openConnection();
                uc.setDoOutput(true);
                uc.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
                java.io.PrintWriter pw = new java.io.PrintWriter(uc.getOutputStream());
                pw.println("action=" + action + "&data=" + jData.toString());
                pw.close();
                iStream = uc.getInputStream();
                java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(iStream));
                res = in.readLine();
                in.close();
                iStream.close();
            } catch (IOException iex) {
                Logger.getLogger(APICallHandler.class.getName()).log(Level.SEVERE,"IO Exception In API Call", iex);
            } finally {
                if(iStream != null) {
                    try {
                        iStream.close();
                    } catch(Exception e) {
                    }
                }
            }
            resObj = new JSONObject(res);
//            if (!resObj.isNull("success") && resObj.getBoolean("success")) {
//                result = true;
//
//            } else {
//                result = false;
//            }

            sql = " UPDATE apiresponse SET response = ?, status = ? WHERE id = ? ";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, res);
            pstmt.setInt(2, 1);
            pstmt.setString(3, uid);
            pstmt.executeUpdate();

        } catch (ServiceException ex) {
            Logger.getLogger(APICallHandler.class.getName()).log(Level.SEVERE,"Service Exception In API Call", ex);
            result = false;

        } catch (JSONException ex) {
            Logger.getLogger(APICallHandler.class.getName()).log(Level.SEVERE,"JSON Exception In API Call", ex);
            result = false;

        } catch (SQLException ex) {
            Logger.getLogger(APICallHandler.class.getName()).log(Level.SEVERE,"SQL Exception In API Call", ex);
            result = false;
        }
        return resObj;
    }
}
