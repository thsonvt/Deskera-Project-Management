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

import com.krawler.common.service.ServiceException;
import com.krawler.common.timezone.Timezone;
import com.krawler.database.DbPool;
import com.krawler.database.DbPool.Connection;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.esp.handlers.DashboardHandler;
import com.krawler.esp.utils.ConfigReader;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Abhay
 */
public class ChartDataServlet extends HttpServlet {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    
    java.sql.Timestamp JavaDateNow = new java.sql.Timestamp(new java.util.Date().getTime());
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Connection conn = null;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String data = "";
        try {
            conn = DbPool.getConnection();
            String ulogin = request.getParameter("uid");
            
            String lim = request.getParameter("limit");
            String off = request.getParameter("start");
            String type = request.getParameter("type");
            
            JSONObject j = getChartDataRequestLog(conn, ulogin, off, type);
            
            if(j != null){ // user had requested chart data previously
                boolean changeFlag = j.getBoolean("changeflag");
                if(changeFlag){ // recalculate data
                    data = DashboardHandler.getChartURL(conn, ulogin, lim, off, type);
                    updateChartDataRequestTimestamp(conn, ulogin, off);
                    updateChartDataRequestChangeFlag(conn, ulogin, false);
                } else {
                    // check for lastrequesttime whether exceeds some time-in-minutes defined in krawler-web-app.
                    int t = Integer.parseInt(ConfigReader.getinstance().get("chartDataRequestInterval"));
                    Date lastReqTime = df.parse(j.getString("lastrequesttime"));
                    String dt = Timezone.toSystemTimezone(conn, df.format(lastReqTime), ulogin);
                    lastReqTime = df.parse(dt);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(new Date());
                    cal.add(Calendar.MINUTE, (t * -1));
                    Date curDate = cal.getTime();
                    if(lastReqTime.before(curDate)){ // if user's last request was older the specified time interval, recalculate data
                        data = DashboardHandler.getChartURL(conn, ulogin, lim, off, type);
                        updateChartDataRequestTimestamp(conn, ulogin, off);
                    } else { // return stored data
                        data = j.getString("chartdata");
                    }
                }
            } else { // calculate data and make an entry into chartrequestlog
                data = DashboardHandler.getChartURL(conn, ulogin, lim, off, type);
                setChartDataRequestLog(conn, ulogin, data, off, type);
            }
            conn.commit();
        } catch (NullPointerException ex) {
            Logger.getLogger(ChartDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            DbPool.quietRollback(conn);
        } catch (JSONException ex) {
            Logger.getLogger(ChartDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            DbPool.quietRollback(conn);
        } catch (ParseException ex) {
            Logger.getLogger(ChartDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            DbPool.quietRollback(conn);
        } catch (ServiceException ex) {
            Logger.getLogger(ChartDataServlet.class.getName()).log(Level.SEVERE, null, ex);
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
            out.println(data);
            out.close();
        }
    }

    private JSONObject getChartDataRequestLog(Connection conn, String ulogin, String pageStart, String type) throws ServiceException, JSONException {
        JSONObject j = null;
        DbResults rs = DbUtil.executeQuery(conn, "SELECT * FROM chartrequestlog WHERE userid = ? AND pagestart = ? AND type = ?", 
                new Object[]{ulogin, pageStart, type});
        if(rs.next()){
            j = rs.toJSONObject(1);
        }
        return j;
    }

    private void setChartDataRequestLog(Connection conn, String userid, String data, String pageStart, String type) throws ServiceException {
        DbUtil.executeUpdate(conn, "INSERT INTO chartrequestlog VALUES(?, ?, ?, 0, ?, ?)", new Object[]{userid, data, JavaDateNow, pageStart, type});
    }

    private void updateChartDataRequestTimestamp(Connection conn, String userid, String pageStart) throws ServiceException {
        DbUtil.executeUpdate(conn, "UPDATE chartrequestlog SET lastrequesttime = ? WHERE userid = ? AND pagestart = ?", new Object[]{JavaDateNow, userid, pageStart});
    }

    public static void updateChartDataRequestChangeFlag(Connection conn, String userid, boolean changeFlag) throws ServiceException {
        DbUtil.executeUpdate(conn, "UPDATE chartrequestlog SET changeflag = ? WHERE userid = ?", new Object[]{changeFlag, userid});
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
