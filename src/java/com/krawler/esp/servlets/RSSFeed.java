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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.Rssview;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedOutput;

public class RSSFeed extends HttpServlet {
   
    protected static final String CONTENT_TYPE = "application/xml; charset=UTF-8";
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        Map mp = null;
        
        try {
            String subdomain = URLUtil.getDomainName2(request);
            Rssview.servPath = URLUtil.getPageURL(request, com.krawler.esp.web.resource.Links.loginpageFull_unprotected, subdomain);
            String userid = "";
            String userName = request.getParameter("u");
            if(userName != null) {
                userid = Rssview.getUserIdFromUsernameAndSubdomain(userName, subdomain);
            }
            String rssFeedTitle = subdomain; //TODO: change to companyname[brajesh@090909] 
            if(request.getParameter("m").equals("global")) {
                mp = Rssview.getCompanyFeeds(userid);
                rssFeedTitle += " - All Project Updates";
            }    
            else if(request.getParameter("m").equals("project")) {
                mp = Rssview.getProjectFeeds(userid,request.getParameter("p"));
                String proj = Rssview.getProjectName(request.getParameter("p"));
                rssFeedTitle += " - ["+proj+"] Updates";
            }    
            else if(request.getParameter("m").equals("events")) {
                if(request.getParameter("c") == null) {
                    mp = Rssview.getProjEventFeeds(userid,request.getParameter("p"));
                    String proj = Rssview.getProjectName(request.getParameter("p"));
                    rssFeedTitle += " - ["+proj+"] Event Updates";
                }    
                else {
                    mp = Rssview.getCalEventFeeds(request.getParameter("c"), userid);
                    String calname = Rssview.getCalendarName(request.getParameter("c"));
                    rssFeedTitle += " -["+calname+"] Event Updates";
                }    
            } else if(request.getParameter("m").equals("todos")) {
                mp = Rssview.getTodoFeeds(request.getParameter("p"));
                String proj = Rssview.getProjectName(request.getParameter("p"));
                rssFeedTitle += " - ["+proj+"] ToDo Updates";
            }    
            
            SyndFeed feed = new SyndFeedImpl();
            
            Rssview.buildRssDocument(mp,feed, rssFeedTitle);
            
            response.setContentType(CONTENT_TYPE);
            
            SyndFeedOutput out = new SyndFeedOutput();
            out.output(feed, response.getWriter());
            response.getWriter().close();
        } catch (Exception ex) {
            Logger.getLogger(RSSFeed.class.getName()).log(Level.SEVERE, null, ex);
        } finally { 
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
