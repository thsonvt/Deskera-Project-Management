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

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.krawler.common.service.ServiceException;
import com.krawler.database.DbPool;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.database.DbPool.Connection;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;

public class CountryList extends HttpServlet {
   
    /** 
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
    * @param request servlet request
    * @param response servlet response
    */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            JSONObject jobj = new JSONObject();
            DbResults rs = null;
            Connection conn = null;
            try {
                conn = DbPool.getConnection();
                if(request.getParameter("mode").matches("country")) {
                 rs = DbUtil.executeQuery(conn, "select countryid, countryname from country order by countryname");
                    while(rs.next()) {
                        JSONObject jtemp = new JSONObject();
                        jtemp.put("id", rs.getInt("countryid"));
                        jtemp.put("name", rs.getString("countryname"));
                        jobj.append("data", jtemp);
                    }

                }else if(request.getParameter("mode").matches("timezone")) {
                        rs = DbUtil.executeQuery(conn, "select id,name from timezone" );
                    while(rs.next()) {
                        JSONObject jtemp = new JSONObject();
                        jtemp.put("id", rs.getString("id"));
                        jtemp.put("name", rs.getString("name"));
                        jobj.append("data", jtemp);
                    }
                }
                
                out.print(jobj.toString());
            } catch (ServiceException e) {
                DbPool.quietRollback(conn);
            } catch (JSONException e) {
                DbPool.quietRollback(conn);
            } finally {
                DbPool.quietClose(conn);
            }
            
        } finally { 
            out.close();
        }
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
