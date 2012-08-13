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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.krawler.common.service.ServiceException;
import com.krawler.database.DbPool;
import com.krawler.database.DbPool.Connection;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;

/**
 * Concrete class for news RSS document views.
 */
public class Rssview {

    /** date format */
    private static final String DATE_PARSER_FORMAT = "yyyy-MM-dd";
    private static final String FEED_TYPE = "rss_2.0";
    private static final DateFormat DATE_PARSER = new SimpleDateFormat(DATE_PARSER_FORMAT);
    public static String servPath = "";

    /**
     * Build in rss document into recieved feed
     * 
     * @param model - handles all data that needed for rss rendering
     * @param SyndFeed feed - ROME feed instance for rss representation.
     */
    public static void buildRssDocument(Map model, SyndFeed Syndfeed, String feedTitle)
            throws Exception {

        Syndfeed.setFeedType(FEED_TYPE);
        Syndfeed.setTitle(feedTitle);
        Syndfeed.setLink(servPath);
        Syndfeed.setDescription("Recent Updates");

        List<SyndEntry> entries = new ArrayList<SyndEntry>();
        SyndEntry entry;
        SyndContent description;

        List<RssFeedItem> feedList = (List<RssFeedItem>) model.get("feeds");
        if (feedList != null) {
            for (RssFeedItem feed : feedList) {
                if (feed != null) {
                    entry = new SyndEntryImpl();
                    entry.setTitle(feed.getTitle());
                    entry.setLink(feed.getLink());
                    entry.setPublishedDate(
                                        DATE_PARSER.parse(feed.getPublishedDate()));
                    description = new SyndContentImpl();
                    description.setType("text/html");
                    description.setValue(feed.getDescription());
                    entry.setDescription(description);
                    entries.add(entry);
                }
            }// for
        }

        Syndfeed.setEntries(entries);
    }

    public static Map getCompanyFeeds(String userid) {
        HashMap feedMap = new HashMap();
        com.krawler.database.DbPool.Connection conn = null;
        try {
            conn = DbPool.getConnection();
            List feeds = projdb.getCompanyFeeds(conn, userid, servPath);
            feedMap.put("feeds", feeds);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return feedMap;
    }

    public static Map getProjectFeeds(String userid, String projectid) {

        HashMap feedMap = new HashMap();
        com.krawler.database.DbPool.Connection conn = null;
        try {
            conn = DbPool.getConnection();
            List feeds = projdb.getProjectFeeds(conn, userid, projectid, servPath);
            feedMap.put("feeds", feeds);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return feedMap;
    }

    public static Map getProjEventFeeds(String userid, String projectid) {

        HashMap feedMap = new HashMap();
        com.krawler.database.DbPool.Connection conn = null;
        try {
            conn = DbPool.getConnection();
            List events = Tree.getUserProjEvents(conn, userid, projectid, servPath);
            feedMap.put("feeds", events);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return feedMap;
    }

    public static Map getCalEventFeeds(String cid, String userid) {

        HashMap feedMap = new HashMap();
        com.krawler.database.DbPool.Connection conn = null;
        try {
            conn = DbPool.getConnection();
            List events = Tree.getUserCalEvents(conn, cid, servPath, userid);
            feedMap.put("feeds", events);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return feedMap;
    }

    public static Map getTodoFeeds(String uid) {

        HashMap feedMap = new HashMap();
        com.krawler.database.DbPool.Connection conn = null;
        try {
            conn = DbPool.getConnection();
            List events = projdb.getUserTodoFeeds(conn, uid, servPath);
            feedMap.put("feeds", events);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return feedMap;
    }

    public static String getProjectName(String projid) {
        String projname = "";
        com.krawler.database.DbPool.Connection conn = null;
        try {
            conn = DbPool.getConnection();
            projname = projdb.getProjectName(conn, projid);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return projname;
    }
    
    public static String getCalendarName(String cid) {
        String calname = "";
        com.krawler.database.DbPool.Connection conn = null;
        try {
            conn = DbPool.getConnection();
            calname = Tree.getCalendarName(conn, cid);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return calname;
    }
    
    public static String getUserIdFromUsernameAndSubdomain( String userName, String subdomain){
    	String userid = "";
        Connection conn = null;
        try {
            conn = DbPool.getConnection();
            userid = AuthHandler.getUserIdFromUsernameAndSubdomain(conn, subdomain, userName);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
        } finally {
            DbPool.quietClose(conn);
        }
        return userid;
    }
}

