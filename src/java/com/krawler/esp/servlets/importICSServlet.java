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
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.KWLErrorMsgs;
import com.krawler.common.util.KrawlerLog;
import com.krawler.common.util.StringUtil;
import com.krawler.svnwebclient.configuration.ConfigurationException;
import com.krawler.utils.json.base.JSONException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.krawler.database.DbPool;
import com.krawler.database.DbPool.Connection;
import com.krawler.esp.handlers.AuditTrail;
import com.krawler.esp.handlers.AuthHandler;
import com.krawler.esp.handlers.ServerEventManager;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.esp.handlers.Tree;
import com.krawler.esp.handlers.calEvent;
import com.krawler.esp.handlers.projdb;
import com.krawler.utils.json.base.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarParserImpl;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.util.CompatibilityHints;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;

public class importICSServlet extends HttpServlet {
    HashMap arrParam = new HashMap();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        Connection conn = null;
        FileInputStream fstream = null;
        String result = KWLErrorMsgs.rsSuccessFalse;
        String contentType = request.getContentType();
        String projid = request.getParameter("projid");
        String Ccname = "";
        String Cdesc = "";
        String Ccid = "";

        String Eeid = "";
        String Estartts = "";
        String Eendts = "";
        String Esubject = "";
        String Edescr = "";
        String Elocation = "";

        try {
            int auditMode = 0;

            if ((contentType != null) && (contentType.indexOf("multipart/form-data") >= 0)) {
                String loginid = AuthHandler.getUserid(request);
                String companyid = AuthHandler.getCompanyid(request);
                String ipAddress = AuthHandler.getIPAddress(request);
                conn = DbPool.getConnection();
                if (request.getParameter("mode").compareToIgnoreCase("new") == 0) {
                    Ccname = request.getParameter("calName");
                    File f1 = getfile(request);
                    fstream = new FileInputStream(f1);
                    Calendar cal = null;

                    setSystemProperties();
                    CalendarBuilder bldr = new CalendarBuilder(new CalendarParserImpl());
                    cal = bldr.build(fstream);

                    Ccid = Tree.insertNewCalendar(conn, Ccname, Cdesc, "", "", "0", 2, 0, projid);

                    for (Iterator i = cal.getComponents().iterator(); i.hasNext();) {
                        Component component = (Component) i.next();
                        Eeid = "";
                        Esubject = "";
                        Estartts = "";
                        Eendts = "";
                        Elocation = "";
                        Edescr = "";
                        for (Iterator j = component.getProperties().iterator(); j.hasNext();) {
                            Property property = (Property) j.next();
                            if (property.getName().equals("SUMMARY"))
                                Esubject = StringUtil.serverHTMLStripper(property.getValue());
                            if (property.getName().equals("DTSTART"))
                                Estartts = StringUtil.serverHTMLStripper(property.getValue());
                            if (property.getName().equals("DTEND"))
                                Eendts = StringUtil.serverHTMLStripper(property.getValue());
                            if (property.getName().equals("LOCATION"))
                                Elocation = StringUtil.serverHTMLStripper(property.getValue());
                            if (property.getName().equals("DESCRIPTION"))
                                Edescr = StringUtil.serverHTMLStripper(property.getValue());
                        }
                        //Property Without End Date
                        if (!StringUtil.isNullOrEmpty(Esubject) && !StringUtil.isNullOrEmpty(Estartts) && StringUtil.isNullOrEmpty(Eendts)) {
                            //Only date given no time
                            if (Estartts.length() < 9)
                                Estartts = iCaltoKWLDayDate(Estartts);
                            //Date and time specified with "TimeZone", hence no "Z" at the end.
                            else if (!Estartts.endsWith("Z")) {
                                Estartts = Estartts.concat("Z");
                                Estartts = iCaltoStartDate(Estartts);
                            } //Date and time in iCalendar format.
                            else
                                Estartts = iCaltoStartDate(Estartts);
                            Eendts = getNextDay(Estartts);//Setting end time as next day

                            if (!StringUtil.isNullOrEmpty(Estartts) || !StringUtil.isNullOrEmpty(Eendts))
                                Eeid = calEvent.insertAllDayEvent(conn, Ccid, Estartts, Eendts, Esubject, Edescr, Elocation, "b", "m", "", "1970-01-01 00:00:00", "", "");
                        } //Property With End Date
                        else if (!StringUtil.isNullOrEmpty(Esubject) || !StringUtil.isNullOrEmpty(Estartts) || !StringUtil.isNullOrEmpty(Eendts)) {
                            //Only date given no time
                            if (Estartts.length() < 9 && Eendts.length() < 9) {
                                Estartts = iCaltoKWLDayDate(Estartts);
                                Eendts = iCaltoKWLEndDayDate(Eendts);
                                Eendts = getTimeHourDiff(Estartts, Eendts);
                                if (!StringUtil.isNullOrEmpty(Estartts) || !StringUtil.isNullOrEmpty(Eendts)) {
                                    Eeid = calEvent.insertAllDayEvent(conn, Ccid, Estartts, Eendts, Esubject, Edescr, Elocation, "b", "m", "", "1970-01-01 00:00:00", "", "");
                                }
                            } //Date and time specified with "TimeZone", hence no "Z" at the end.
                            else {
                                if (!Estartts.endsWith("Z") && !Eendts.endsWith("Z")) {
                                    Estartts = Estartts.concat("Z");
                                    Eendts = Eendts.concat("Z");
                                    Estartts = iCaltoKWLDate(Estartts);
                                    Eendts = iCaltoKWLDate(Eendts);
                                } //Date and time in iCalendar format.
                                else {
                                    Estartts = iCaltoKWLDate(Estartts);
                                    Eendts = iCaltoKWLDate(Eendts);
                                }
                                Eendts = getTimeHourDiff(Estartts, Eendts);
                                if (!StringUtil.isNullOrEmpty(Estartts) || !StringUtil.isNullOrEmpty(Eendts)) {
                                    Eeid = calEvent.insertEvent(conn, Ccid, Estartts, Eendts, Esubject, Edescr, Elocation, "b", "m", "", "1970-01-01 00:00:00", "", "0", "0", "");
                                }
                            }
                        }
                    }
                    String calDetails = Tree.getCalendar(conn, Ccid);
                    com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject(calDetails);
                    jobj.getJSONArray("data").getJSONObject(0).put("permissionlevel", "");
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("action", "1");
                    data.put("data", jobj.toString());
                    data.put("success","true");
                    ServerEventManager.publish("/calTree/" + projid, data, this.getServletContext());

                    String params = AuthHandler.getAuthor(conn, loginid) + " ("+ AuthHandler.getUserName(request) +"), " +
                                    Ccname + ", " + projdb.getProjectName(conn, projid);
                    AuditTrail.insertLog(conn, "127", loginid, Ccid, projid, companyid, params, ipAddress, auditMode);

                    conn.commit();
                    fstream.close();
                    result = KWLErrorMsgs.rsSuccessTrue;

                } else if (request.getParameter("mode").compareToIgnoreCase("merge") == 0) {
                    File f1 = getfile(request);
                    fstream = new FileInputStream(f1);
                    Calendar cal = null;
                    String eventDetails = "";
                    String cid = "";
                    int cnt = 0;
                    Ccid = request.getParameter("calId");
                    Ccname = Tree.getCalendarName(conn, Ccid);

                    setSystemProperties();
                    CalendarBuilder bldr = new CalendarBuilder(new CalendarParserImpl());
                    cal = bldr.build(fstream);

                    for (Iterator i = cal.getComponents().iterator(); i.hasNext();) {
                        Component component = (Component) i.next();
                        Eeid = "";
                        Esubject = "";
                        Estartts = "";
                        Eendts = "";
                        Elocation = "";
                        Edescr = "";
                        for (Iterator j = component.getProperties().iterator(); j.hasNext();) {
                            Property property = (Property) j.next();
                            if (property.getName().equals("UID"))
                                Eeid = StringUtil.serverHTMLStripper(property.getValue());
                            if (property.getName().equals("SUMMARY"))
                                Esubject = StringUtil.serverHTMLStripper(property.getValue());
                            if (property.getName().equals("DTSTART"))
                                Estartts = StringUtil.serverHTMLStripper(property.getValue());
                            if (property.getName().equals("DTEND"))
                                Eendts = StringUtil.serverHTMLStripper(property.getValue());
                            if (property.getName().equals("LOCATION"))
                                Elocation = StringUtil.serverHTMLStripper(property.getValue());
                            if (property.getName().equals("DESCRIPTION"))
                                Edescr = StringUtil.serverHTMLStripper(property.getValue());
                        }
                        //Property Without End Date
                        if (!StringUtil.isNullOrEmpty(Esubject) && !StringUtil.isNullOrEmpty(Estartts) && StringUtil.isNullOrEmpty(Eendts)) {
                            //Only date given no time
                            if (Estartts.length() < 9)
                                Estartts = iCaltoKWLDayDate(Estartts);
                            //Date and time specified with "TimeZone", hence no "Z" at the end.
                            else if (!Estartts.endsWith("Z")) {
                                Estartts = Estartts.concat("Z");
                                Estartts = iCaltoStartDate(Estartts);
                            } //Date and time in iCalendar format.
                            else
                                Estartts = iCaltoStartDate(Estartts);
                            Eendts = getNextDay(Estartts);//Setting end time as next day

                            if (!StringUtil.isNullOrEmpty(Estartts) || !StringUtil.isNullOrEmpty(Eendts)) {
                                eventDetails = calEvent.getEventDetails(conn, Eeid).toString();
                                if (eventDetails.compareTo("{data:{}}") == 0)
                                    Eeid = calEvent.insertAllDayEvent(conn, Ccid, Estartts, Eendts, Esubject, Edescr, Elocation, "b", "m", "", "1970-01-01 00:00:00", "", "");
                                else if(eventDetails.compareTo("{data:{}}") != 0) {
                                    com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject(eventDetails);
                                    cid = jobj.getJSONArray("data").getJSONObject(0).getString("cid");
                                    if(cid.compareTo(Ccid) == 0) {
                                        cnt = calEvent.updateAllDayEvent(conn, Ccid, Estartts, Eendts, Esubject, Edescr, Elocation, "b", "m", "", "1970-01-01 00:00:00", "", Eeid);
                                    }
                                    else
                                        Eeid = calEvent.insertAllDayEvent(conn, Ccid, Estartts, Eendts, Esubject, Edescr, Elocation, "b", "m", "", "1970-01-01 00:00:00", "", "");
                                }
                            }
                        }
                        //Property With End Date
                        else if (!StringUtil.isNullOrEmpty(Esubject) || !StringUtil.isNullOrEmpty(Estartts) || !StringUtil.isNullOrEmpty(Eendts)) {
                            //Only date given no time
                            if (Estartts.length() < 9 && Eendts.length() < 9) {
                                Estartts = iCaltoKWLDayDate(Estartts);
                                Eendts = iCaltoKWLEndDayDate(Eendts);
                                if (!StringUtil.isNullOrEmpty(Estartts) || !StringUtil.isNullOrEmpty(Eendts)) {
                                    eventDetails = calEvent.getEventDetails(conn, Eeid).toString();
                                    Eendts = getTimeHourDiff(Estartts, Eendts);
                                    if(eventDetails.compareTo("{data:{}}") == 0)
                                        Eeid = calEvent.insertAllDayEvent(conn, Ccid, Estartts, Eendts, Esubject, Edescr, Elocation, "b", "m", "", "1970-01-01 00:00:00", "", "");
                                    else if(eventDetails.compareTo("{data:{}}") != 0) {
                                        com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject(eventDetails);
                                        cid = jobj.getJSONArray("data").getJSONObject(0).getString("cid");
                                        if(cid.compareTo(Ccid) == 0)
                                            cnt = calEvent.updateAllDayEvent(conn, Ccid, Estartts, Eendts, Esubject, Edescr, Elocation, "b", "m", "", "1970-01-01 00:00:00", "", Eeid);
                                        else
                                            Eeid = calEvent.insertAllDayEvent(conn, Ccid, Estartts, Eendts, Esubject, Edescr, Elocation, "b", "m", "", "1970-01-01 00:00:00", "", "");
                                    }
                                }
                            } //Date and time specified with "TimeZone", hence no "Z" at the end.
                            else {
                                if (!Estartts.endsWith("Z") && !Eendts.endsWith("Z")) {
                                    Estartts = Estartts.concat("Z");
                                    Eendts = Eendts.concat("Z");
                                    Estartts = iCaltoKWLDate(Estartts);
                                    Eendts = iCaltoKWLDate(Eendts);
                                } //Date and time in iCalendar format.
                                else {
                                    Estartts = iCaltoKWLDate(Estartts);
                                    Eendts = iCaltoKWLDate(Eendts);
                                }

                                if (!StringUtil.isNullOrEmpty(Estartts) || !StringUtil.isNullOrEmpty(Eendts)) {
                                    eventDetails = calEvent.getEventDetails(conn, Eeid).toString();
                                    Eendts = getTimeHourDiff(Estartts, Eendts);
                                    if(eventDetails.compareTo("{data:{}}") == 0)
                                        Eeid = calEvent.insertEvent(conn, Ccid, Estartts, Eendts, Esubject, Edescr, Elocation, "b", "m", "", "1970-01-01 00:00:00", "", "0", "0", "");
                                    else if(eventDetails.compareTo("{data:{}}") != 0) {
                                        com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject(eventDetails);
                                        cid = jobj.getJSONArray("data").getJSONObject(0).getString("cid");
                                        if(cid.compareTo(Ccid) == 0)
                                            cnt = calEvent.updateEvent(conn, Ccid, Estartts, Eendts, Esubject, Edescr, Elocation, "b", "m", "", "1970-01-01 00:00:00", "", Eeid, "0", "0");
                                        else
                                            Eeid = calEvent.insertEvent(conn, Ccid, Estartts, Eendts, Esubject, Edescr, Elocation, "b", "m", "", "1970-01-01 00:00:00", "", "0", "0","");
                                    }
                                }
                            }
                        }
                    }
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("action", "4");
                    data.put("success", "true");
                    data.put("cid", Ccid);
                    ServerEventManager.publish("/calEvent/" + Ccid, data, this.getServletContext());

                    String params = AuthHandler.getAuthor(conn, loginid) + " ("+ AuthHandler.getUserName(request) +"), " +
                                    Ccname + ", " + projdb.getProjectName(conn, projid);
                    AuditTrail.insertLog(conn, "128", loginid, Ccid, projid, companyid, params, ipAddress, auditMode);

                    conn.commit();
                    fstream.close();
                    result = KWLErrorMsgs.rsSuccessTrue;

                } else if (request.getParameter("mode").compareToIgnoreCase("browse") == 0) {
                    String calId = request.getParameter("calId");
                    Ccname = Tree.getCalendarName(conn, calId);
                    String calSharedCheck = Tree.getCalendarSharedCheck(conn, calId, projid);
                    if(calSharedCheck.compareTo("{data:{}}") == 0) {
                        String res = Tree.insertCalPermission(conn, calId, projid, 3);
                        if(!res.equals("0")) {
                            String returnStr = Tree.getSharedCalendar(conn, res, projid);
                            if(returnStr.compareTo("{data:{}}") != 0) {
                                JSONObject jobj = new JSONObject(returnStr.toString());
                                Map<String, String> data = new HashMap<String, String>();
                                data.put("action", "1");
                                data.put("data", jobj.toString());
                                data.put("success", "true");
                                ServerEventManager.publish("/calTree/"+ projid, data, this.getServletContext());

                                String params = AuthHandler.getAuthor(conn, loginid) + " ("+ AuthHandler.getUserName(request) +"), " +
                                                Ccname + ", " + projdb.getProjectName(conn, projid);
                                AuditTrail.insertLog(conn, "129", loginid, Ccid, projid, companyid, params, ipAddress, auditMode);

                                conn.commit();
                                result = KWLErrorMsgs.rsSuccessTrue;
                            }
                        }
                    }
                    else {
                        result = KWLErrorMsgs.rsSuccessFalse;
                    }
                }
            } else {
                if (request.getParameter("mode").compareToIgnoreCase("url") == 0) {
                    String loginid = AuthHandler.getUserid(request);
                    String companyid = AuthHandler.getCompanyid(request);
                    String ipAddress = AuthHandler.getIPAddress(request);
                    conn = DbPool.getConnection();
                    String url = request.getParameter("url");
                    Ccname = request.getParameter("cname");
                    Cdesc = request.getParameter("description");
                    String CcolorCode = request.getParameter("colorcode");
                    int interval = Integer.parseInt(request.getParameter("interval"));

                    result = KWLErrorMsgs.rsSuccessFalse;
                    String ID = UUID.randomUUID().toString();
                    Calendar cal = setUpICal(url, ID, interval);
                    if(cal != null){
                        Ccid = ID;
                        int cnt = Tree.insertNewCalendar(conn, Ccid, Ccname, Cdesc, "", "", CcolorCode, 4, 0, projid);
                        if(cnt != 0){
                            Tree.insertInternetCalendar(conn, Ccid, loginid, url, interval);
                            conn.commit();
                            result = KWLErrorMsgs.rsSuccessTrue;

                            String calDetails = Tree.getCalendar(conn, Ccid);
                            JSONObject jobj = new JSONObject(calDetails);
                            jobj.getJSONArray("data").getJSONObject(0).put("permissionlevel", "2");
                            Map<String, String> data = new HashMap<String, String>();
                            data.put("action", "1");
                            data.put("data", jobj.toString());
                            data.put("success", "true");
                            ServerEventManager.publish("/calTree/" + projid, data, this.getServletContext());

                            String params = AuthHandler.getAuthor(conn, loginid) + " ("+ AuthHandler.getUserName(request) +"), " +
                                        Tree.getCalendarName(conn, Ccid) + ", " + projdb.getProjectName(conn, projid);
                            AuditTrail.insertLog(conn, "124", loginid, Ccid, projid, companyid, params, ipAddress, auditMode);
                        }
                    }
                } else if (request.getParameter("mode").compareToIgnoreCase("uploadHoliday") == 0) {
                    String src = "";
                    conn = DbPool.getConnection();
                    Calendar cal = null;
                    String[] names = new String[]{"Indian_Holidays", "Italian_Holidays", "Dutch_Holidays", "Canadian_Holidays", "Indonesian_Holidays",
                        "Malaysian_Holidays", "New_Zealand_Holidays", "South_Africa_Holidays", "US_Holidays", "UK_Holidays", "Irish_Holidays", "Mexican_Holidays",
                        "Brazilian_Holidays", "Swedish_Holidays", "Danish_Holidays", "China_Holidays", "Australian_Holidays", "Hong_Kong_C_Holidays",
                        "Finnish_Holidays", "French_Holidays", "German_Holidays", "Hong_Kong_Holidays", "Japanese_Holidays", "Norwegian_Holidays",
                        "Philippines_Holidays", "Portuguese_Holidays", "South_Korean_Holidays", "Spain_Holidays", "Taiwan_Holidays", "Thai_Holidays", "Jewish_Holidays", "Islamic_Holidays"};

                    for (int n = 0; n <= names.length; n++) {
                        src = StorageHandler.GetDocStorePath();
                        src = src + StorageHandler.GetFileSeparator() + "HolidayCalendarFiles" + StorageHandler.GetFileSeparator() + names[n] + ".ics";

                        fstream = new FileInputStream(src);
                        setSystemProperties();
                        CalendarBuilder bldr = new CalendarBuilder(new CalendarParserImpl());
                        cal = bldr.build(fstream);

                        Ccid = names[n];

                        for (Iterator i = cal.getComponents().iterator(); i.hasNext();) {
                            Component component = (Component) i.next();
                            Eeid = "";
                            Esubject = "";
                            Estartts = "";
                            Eendts = "";
                            Elocation = "";
                            Edescr = "";
                            for (Iterator j = component.getProperties().iterator(); j.hasNext();) {
                                Property property = (Property) j.next();
                                if (property.getName().equals("SUMMARY")) {
                                    Esubject = StringUtil.serverHTMLStripper(property.getValue());
                                }
                                if (property.getName().equals("DTSTART")) {
                                    Estartts = StringUtil.serverHTMLStripper(property.getValue());
                                }
                                if (property.getName().equals("DTEND")) {
                                    Eendts = StringUtil.serverHTMLStripper(property.getValue());
                                }
                                if (property.getName().equals("LOCATION")) {
                                    Elocation = StringUtil.serverHTMLStripper(property.getValue());
                                }
                                if (property.getName().equals("DESCRIPTION")) {
                                    Edescr = StringUtil.serverHTMLStripper(property.getValue());
                                }
                            }
                            //Property Without End Date
                            if (!StringUtil.isNullOrEmpty(Esubject) && !StringUtil.isNullOrEmpty(Estartts) && StringUtil.isNullOrEmpty(Eendts)) {
                                //Only date given no time
                                if (Estartts.length() < 9) {
                                    Estartts = iCaltoKWLDayDate(Estartts);
                                } //Date and time specified with "TimeZone", hence no "Z" at the end.
                                else if (!Estartts.endsWith("Z")) {
                                    Estartts = Estartts.concat("Z");
                                    Estartts = iCaltoStartDate(Estartts);
                                } //Date and time in iCalendar format.
                                else {
                                    Estartts = iCaltoStartDate(Estartts);
                                }
                                Eendts = getNextDay(Estartts);//Setting end time as next day

                                if (!StringUtil.isNullOrEmpty(Estartts) || !StringUtil.isNullOrEmpty(Eendts)) {
                                    Eeid = calEvent.insertAllDayEvent(conn, Ccid, Estartts, Eendts, Esubject, "", Elocation, "b", "m", "", "1970-01-01 00:00:00", "", "");
                                }
                            } //Property With End Date
                            else if (!StringUtil.isNullOrEmpty(Esubject) || !StringUtil.isNullOrEmpty(Estartts) || !StringUtil.isNullOrEmpty(Eendts)) {
                                //Only date given no time
                                if (Estartts.length() < 9 && Eendts.length() < 9) {
                                    Estartts = iCaltoKWLDayDate(Estartts);
                                    Eendts = iCaltoKWLEndDayDate(Eendts);
                                    Eendts = getTimeHourDiff(Estartts, Eendts);
                                    if (!StringUtil.isNullOrEmpty(Estartts) || !StringUtil.isNullOrEmpty(Eendts)) {
                                        Eeid = calEvent.insertAllDayEvent(conn, Ccid, Estartts, Eendts, Esubject, "", Elocation, "b", "m", "", "1970-01-01 00:00:00", "", "");
                                    }
                                } //Date and time specified with "TimeZone", hence no "Z" at the end.
                                else {
                                    if (!Estartts.endsWith("Z") && !Eendts.endsWith("Z")) {
                                        Estartts = Estartts.concat("Z");
                                        Eendts = Eendts.concat("Z");
                                        Estartts = iCaltoKWLDate(Estartts);
                                        Eendts = iCaltoKWLDate(Eendts);
                                    } //Date and time in iCalendar format.
                                    else {
                                        Estartts = iCaltoKWLDate(Estartts);
                                        Eendts = iCaltoKWLDate(Eendts);
                                    }
                                    Eendts = getTimeHourDiff(Estartts, Eendts);
                                    if (!StringUtil.isNullOrEmpty(Estartts) || !StringUtil.isNullOrEmpty(Eendts)) {
                                        Eeid = calEvent.insertEvent(conn, Ccid, Estartts, Eendts, Esubject, "", Elocation, "b", "m", "", "1970-01-01 00:00:00", "", "0", "0", "");
                                    }
                                }
                            }
                        }

                        conn.commit();
                        fstream.close();
                        result = KWLErrorMsgs.rsSuccessTrue;
                    }
                }

            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(importICSServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            DbPool.quietRollback(conn);
            Logger.getLogger(importICSServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            DbPool.quietRollback(conn);
            String msg = ex.getMessage();
            if (msg.contains(":")) {
                msg = msg.substring(msg.indexOf(":")+2, msg.length());
            }
            result = KWLErrorMsgs.rsValidTrueSuccessFalseErr + MessageSourceProxy.getMessage(msg, null, request) + "\"}";
            Logger.getLogger(importICSServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserException ex) {
            DbPool.quietRollback(conn);
            Logger.getLogger(importICSServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            DbPool.quietRollback(conn);
            Logger.getLogger(importICSServlet.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DbPool.quietClose(conn);
            response.getWriter().println(result);
            response.getWriter().close();
        }
    }

    public static Calendar setUpICal(String url, String ID, int interval) throws ServiceException {
        Calendar calByUrl = null;
        try {
            setSystemProperties();
            boolean fileCreated = false;
            
            File file = new File(StorageHandler.GetDocStorePath() +StorageHandler.GetFileSeparator()+ ID + ".ics");

            if (file.exists()) { // if this calendar file exists in the store?
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(new Date());
                cal.add(java.util.Calendar.MINUTE, interval);
                if (new Date(file.lastModified()).before(cal.getTime()) || new Date(file.lastModified()).equals(cal.getTime())) { // if its older than given time?
                    fileCreated = getICalFileFromURL(file, url, true);
                    if(fileCreated){
                        interval = interval * -1;
                        Tree.updateInternetCalendar(ID, interval, true);
                        file.setLastModified(new Date().getTime());
                    }
                } else {
                    fileCreated = true;
                }
            } else { // file does not exists. it has to be created.
                fileCreated = getICalFileFromURL(file, url, false);
                if(fileCreated)
                    file.setLastModified(new Date().getTime());
            }
            if(fileCreated){
                FileInputStream fip = new FileInputStream(file);
                CalendarBuilder cb = new CalendarBuilder(new CalendarParserImpl());
                calByUrl = cb.build(fip);
                fip.close();
                file.setReadOnly();
                if(!calByUrl.toString().contains(Organizer.ORGANIZER)){
                    Property p = new Organizer("MAILTO:calendar@deskera.com");
                    calByUrl.getProperties().add(p);
                }
                calByUrl.validate();
            }
        } catch (FileNotFoundException e) {
            throw ServiceException.FAILURE(KWLErrorMsgs.calFileEx, e);
        } catch (ValidationException e) {
            throw ServiceException.FAILURE(KWLErrorMsgs.calValidationEx, e);
        } catch (ParserException e) {
            throw ServiceException.FAILURE(KWLErrorMsgs.calParseEx, e);
        } catch (ConfigurationException e) {
            throw ServiceException.FAILURE(KWLErrorMsgs.calFileEx, e);
        } catch (URISyntaxException e) {
            throw ServiceException.FAILURE(KWLErrorMsgs.calURLEx, e);
        } catch (IOException e) {
            throw ServiceException.FAILURE(KWLErrorMsgs.calIOEx, e);
        } catch (Exception e) {
            throw ServiceException.FAILURE(KWLErrorMsgs.calIOEx, e);
        }
        return calByUrl;
    }

    private static boolean getICalFileFromURL(File file, String url, boolean deleteOlderAndCreateNew) throws ServiceException {
        boolean success = false;
        InputStream is = null;
        try{
            URL u = new URL(url);
            URLConnection uc = u.openConnection();
            is = uc.getInputStream();
            if (uc.getContentType().contains("text/calendar")) {
                if(deleteOlderAndCreateNew){
                    file.delete(); // delete the file in store as it is an older one
                }
                file.createNewFile();
                FileOutputStream fop = new FileOutputStream(file);
                byte[] b = new byte[4096];
                int count = 0;
                while ((count = is.read(b)) >= 0) {
                    fop.write(b, 0, count);
                }
                fop.close();
                closeInputStream(is);
                success = true;
            } else {
                closeInputStream(is);
                throw ServiceException.FAILURE("Given calendar URL is not a valid internet calendar.", new Throwable(url));
            }
        } catch (MalformedURLException ex) {
            throw ServiceException.FAILURE(KWLErrorMsgs.calURLEx, ex);
        } catch (FileNotFoundException ex) {
            throw ServiceException.FAILURE(KWLErrorMsgs.calFileEx, ex);
        } catch (IOException ex) {
            closeInputStream(is);
            throw ServiceException.FAILURE(KWLErrorMsgs.calIOEx, ex);
        } catch (Exception ex) {
            closeInputStream(is);
            throw ServiceException.FAILURE(KWLErrorMsgs.calIOEx, ex);
        }
        return success;
    }

    private static void closeInputStream(InputStream is) throws ServiceException {
        if(is != null){
            try {
                is.close();
            } catch (IOException ex) {
                throw ServiceException.FAILURE(KWLErrorMsgs.calIOEx, ex);
            }
        }
    }

    public static String getIEventJson(Calendar cal, String cid) throws ServiceException {
        try{
            JSONObject jobj = new JSONObject();
            for (Iterator i = cal.getComponents().iterator(); i.hasNext();) {
                JSONObject temp = new JSONObject();
                Component component = (Component) i.next();
                String Eeid = "";
                String Esubject = "";
                String Estartts = "";
                String Eendts = "";
                String Elocation = "";
                String Edescr = "";
                boolean allDay = false;
                for (Iterator j = component.getProperties().iterator(); j.hasNext();) {
                    Property property = (Property) j.next();
                    if (property.getName().equals("SUMMARY")) {
                        Esubject = StringUtil.serverHTMLStripper(property.getValue());
                    }
                    if (property.getName().equals("DTSTART")) {
                        Estartts = StringUtil.serverHTMLStripper(property.getValue());
                    }
                    if (property.getName().equals("DTEND")) {
                        Eendts = StringUtil.serverHTMLStripper(property.getValue());
                    }
                    if (property.getName().equals("LOCATION")) {
                        Elocation = StringUtil.serverHTMLStripper(property.getValue());
                    }
                    if (property.getName().equals("DESCRIPTION")) {
                        Edescr = StringUtil.serverHTMLStripper(property.getValue());
                    }
                }
                //Property Without End Date
                if (!StringUtil.isNullOrEmpty(Esubject) && !StringUtil.isNullOrEmpty(Estartts) && StringUtil.isNullOrEmpty(Eendts)) {
                    //Only date given no time
                    if (Estartts.length() < 9) {
                        Estartts = iCaltoKWLDayDate(Estartts);
                    } else if (!Estartts.endsWith("Z")) {
                        Estartts = Estartts.concat("Z");
                        Estartts = iCaltoStartDate(Estartts);
                    } //Date and time in iCalendar format.
                    else {
                        Estartts = iCaltoStartDate(Estartts);
                    }
                    Eendts = getNextDay(Estartts); //Setting end time as next day
                    if (!StringUtil.isNullOrEmpty(Estartts) || !StringUtil.isNullOrEmpty(Eendts)) {
                        allDay = true;
    //                    Eeid = calEvent.insertAllDayEvent(conn, Ccid, Estartts, Eendts, Esubject, Edescr, Elocation, "b", "m", "", "1970-01-01 00:00:00", "", "");
                    }
                } //Property With End Date
                else if (!StringUtil.isNullOrEmpty(Esubject) || !StringUtil.isNullOrEmpty(Estartts) || !StringUtil.isNullOrEmpty(Eendts)) {
                    //Only date given no time
                    if (Estartts.length() < 9 && Eendts.length() < 9) {
                        Estartts = iCaltoKWLDayDate(Estartts);
                        Eendts = iCaltoKWLEndDayDate(Eendts);
                        Eendts = getTimeHourDiff(Estartts, Eendts);
                        if (!StringUtil.isNullOrEmpty(Estartts) || !StringUtil.isNullOrEmpty(Eendts)) {
                            allDay = true;
//                          Eeid = calEvent.insertAllDayEvent(conn, Ccid, Estartts, Eendts, Esubject, Edescr, Elocation, "b", "m", "", "1970-01-01 00:00:00", "", "");
                        }
                    } //Date and time specified with "TimeZone", hence no "Z" at the end.
                    else {
                        if (!Estartts.endsWith("Z") && !Eendts.endsWith("Z")) {
                            Estartts = Estartts.concat("Z");
                            Eendts = Eendts.concat("Z");
                            Estartts = iCaltoKWLDate(Estartts);
                            Eendts = iCaltoKWLDate(Eendts);
                        } //Date and time in iCalendar format.
                        else {
                            Estartts = iCaltoKWLDate(Estartts);
                            Eendts = iCaltoKWLDate(Eendts);
                        }
                        Eendts = getTimeHourDiff(Estartts, Eendts);
                        if (!StringUtil.isNullOrEmpty(Estartts) || !StringUtil.isNullOrEmpty(Eendts)) {
                            allDay = false;
//                            Eeid = calEvent.insertEvent(conn, Ccid, Estartts, Eendts, Esubject, Edescr, Elocation, "b", "m", "", "1970-01-01 00:00:00", "", "0", "0", "");
                        }
                    }
                }
                Eeid = UUID.randomUUID().toString();
                temp.put("eid", Eeid);
                temp.put("peid", Eeid);
                temp.put("cid", cid);
                temp.put("startts", Estartts);
                temp.put("endts", Eendts);
                temp.put("subject", Esubject);
                temp.put("descr", Edescr);
                temp.put("location", Elocation);
                temp.put("showas", "b");
                temp.put("priority", "m");
                temp.put("recpattern", "");
                temp.put("recend", "1970-01-01 00:00:00");
                temp.put("resources", "");
                temp.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                temp.put("allday", allDay);
                temp.put("flagEvent", 0);
                jobj.append("data", temp);
            }
            return jobj.toString();
        } catch (JSONException e) {
            throw ServiceException.FAILURE(KWLErrorMsgs.calJSONEx, e);
        }
    }

    public static void setSystemProperties() throws Exception {
        try {
            CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_VALIDATION, true);
            CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
            CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY, true);
        } catch (Throwable t) {
        }
    }

    public static String iCaltoKWLDate(String idate) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
            java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd HH:00:00.00");

            if (idate.compareTo("") != 0) {
                java.util.Date dt = sdf.parse(idate);
                idate = sdf1.format(dt);
            }
        } catch (ParseException ex) {
            idate = "";
        } finally {
            return idate;
        }
    }

    public static String iCaltoStartDate(String idate) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
            java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd 00:00:00.00");

            if (idate.compareTo("") != 0) {
                java.util.Date dt = sdf.parse(idate);
                idate = sdf1.format(dt);
            }
        } catch (ParseException ex) {
            idate = "";
        } finally {
            return idate;
        }
    }

    public static String iCaltoKWLDayDate(String idate) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd");
            java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd 00:00:00.00");

            if (idate.compareTo("") != 0) {
                java.util.Date dt = sdf.parse(idate);
                idate = sdf1.format(dt);
            }
        } catch (ParseException ex) {
            idate = "";
        } finally {
            return idate;
        }
    }

    public static String iCaltoKWLEndDayDate(String idate) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd");
            java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd 00:00:00.00");

            if (idate.compareTo("") != 0) {
                java.util.Date dt = sdf.parse(idate);
                idate = sdf1.format(dt);
            }
        } catch (ParseException ex) {
            idate = "";
        } finally {
            return idate;
        }
    }

    public static String getTimeHourDiff(String Estartts, String Eendts) {
        long hours = 0;
        long diffInMilleseconds = 0;
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:00:00.00");

            if (Estartts.compareTo("") != 0 && Eendts.compareTo("") != 0) {
                java.util.Date sdt = sdf.parse(Estartts);
                java.util.Date edt = sdf.parse(Eendts);

                diffInMilleseconds = edt.getTime() - sdt.getTime();
                hours = diffInMilleseconds / (60 * 60 * 1000);
                if (hours < 1) {
                    if (edt.getHours() == 0) {
                        Eendts = getNextDay(Estartts);
                    } else {
                        diffInMilleseconds = edt.getTime() + (60 * 60 * 1000);
                        edt.setTime(diffInMilleseconds);
                        Eendts = sdf.format(edt);
                    }
                }
            }
        } catch (ParseException ex) {
            hours = 0;
        } finally {
            return Eendts;
        }
    }

    public static String getNextDay(String iDate) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH");
            java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd 00:00:00");

            if (iDate.compareTo("") != 0) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(sdf.parse(iDate));

                cal.add(java.util.Calendar.DATE, 1);

                iDate = sdf1.format(cal.getTime());
            }
        } catch (ParseException ex) {
            iDate = "";
        } finally {
            return iDate;
        }
    }

    private File getfile(HttpServletRequest request) {
        DiskFileUpload fu = new DiskFileUpload();
        String Ext = null;
        File uploadFile = null;
        List fileItems = null;
        try {
            fileItems = fu.parseRequest(request);
        } catch (FileUploadException e) {
            KrawlerLog.op.warn("Problem While Uploading file :" + e.toString());
        }
        for (Iterator i = fileItems.iterator(); i.hasNext();) {
            FileItem fi = (FileItem) i.next();
            if (!fi.isFormField()) {
                String fileName = null;
                try {
                    fileName = new String(fi.getName().getBytes(), "UTF8");
                    if (fileName.contains(".")) {
                        Ext = fileName.substring(fileName.lastIndexOf("."));
                    }
                    if (fi.getSize() != 0) {
                        uploadFile = File.createTempFile("iCalDeskeraTemp", ".ics");
                        fi.write(uploadFile);
                    }
                } catch (Exception e) {
                    KrawlerLog.op.warn("Problem While Reading file :" + e.toString());
                }
            } else {
                arrParam.put(fi.getFieldName(), fi.getString());
            }
        }
        return uploadFile;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            processRequest(request, response);
        }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            processRequest(request, response);
        }

    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
