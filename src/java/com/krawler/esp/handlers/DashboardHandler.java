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

import com.krawler.common.customcolumn.CCManager;
import com.krawler.common.customcolumn.ColumnSet;
import com.krawler.common.customcolumn.CustomColumn;
import com.krawler.common.locale.MessageSourceProxy;
import com.krawler.common.permission.PermissionConstants;
import com.krawler.common.permission.PermissionManager;
import com.krawler.svnwebclient.configuration.ConfigurationException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.krawler.common.service.ServiceException;
import com.krawler.database.DbPool;
import com.krawler.database.DbPool.Connection;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.utils.json.KWLJsonConverter;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.KWLErrorMsgs;
import java.util.Calendar;
import com.krawler.common.timezone.Timezone;
import com.krawler.common.util.SchedulingUtilities;
import com.krawler.esp.company.Company;
import com.krawler.esp.company.CompanyDAO;
import com.krawler.esp.company.CompanyDAOImpl;
import com.krawler.esp.database.dbcon;
import com.krawler.esp.portalmsg.Mail;
import com.krawler.esp.project.meter.HealthMeter;
import com.krawler.esp.project.meter.HealthMeterDAO;
import com.krawler.esp.project.meter.HealthMeterDAOImpl;
import com.krawler.esp.project.project.Project;
import com.krawler.esp.project.project.ProjectDAO;
import com.krawler.esp.project.project.ProjectDAOImpl;
import com.krawler.esp.utils.ConfigReader;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.mail.MessagingException;

public class DashboardHandler {

    public static String communityReqFormat = "Community <a href=\"#\" onclick=\"navigate('o','%s','%s','%s');\">%s</a>  has <strong>%d</strong> request%s.";
    public static String projReqFormat = "pm.dashboard.widget.request.project";
    public static String userReqFormat = "pm.dashboard.widget.request.user";
    public static String userEvtFormat = "%s : <a href=\"#\" onclick=\"navigate('%s','%s','%s','%s');\">%s</a>";
    public static String inviteCommReqFormat = "You have been invited to <a href=\"#\" onclick=\"navigate('o','%s','%s','%s');\">%s</a> Community.";
    public static String inviteProjReqFormat = "pm.dashboard.widget.request.inviteproject";
    public static String updateProgressReqFormat = "pm.dashboard.widget.request.updateprogress";

    public static String getactionlog(Connection conn, String ulogin,
            String upagesize, String upageno) throws ServiceException {
        String splitstring2 = null;
        int offset = (Integer.parseInt(upagesize) * Integer.parseInt(upageno));
        int pagesize1 = Integer.parseInt(upagesize);
        ResultSet rs = null;
        ResultSet rs1 = null;
        PreparedStatement pstmt = null;
        PreparedStatement pstmt1 = null;
        try {
            pstmt = conn.prepareStatement("select count(*) as count from actionlog"
                    + "  where `by`!=? and (`to`=?   or `to` IN(select cid from sharecalendarmap where userid= ? )   "
                    + "  or `to` IN(select projectid from projectmembers where userid=?  and status>2)   "
                    + "  or `to` IN(select communityid from communitymembers where userid=? and status>2)  "
                    + "  or `to` in(select  docid from docs where userid=? union  "
                    + "  SELECT docid FROM docs where docid in (select docid from docprerel where permission = '3' and userid!=?) union  "
                    + "  select docid from docs  where docid in(select docprerel.docid from docprerel where docprerel.userid in "
                    + "  (select  userid2 as userid from userrelations where userid1 =?  union select  userid1 as userid from userrelations where  "
                    + "  userid2= ?   and relationid = '3') and permission='1') union "
                    + "  select docs.docid from docs inner join docprerel on docprerel.docid = docs.docid where docs.docid in (select docprerel.docid from docprerel where docprerel.userid =? and docprerel.permission ='2'))"
                    + "  or `logid`  in (select logid from actionlog where  logid in (select logid from actionlogref where shwith =? ) and `by`!=? ) "
                    + "  or (`to`=  (select companyid from users where userid=?))) ");
            pstmt.setString(1, ulogin);
            pstmt.setString(2, ulogin);
            pstmt.setString(3, ulogin);
            pstmt.setString(4, ulogin);
            pstmt.setString(5, ulogin);
            pstmt.setString(6, ulogin);
            pstmt.setString(7, ulogin);
            pstmt.setString(8, ulogin);
            pstmt.setString(9, ulogin);
            pstmt.setString(10, ulogin);
            pstmt.setString(11, ulogin);
            pstmt.setString(12, ulogin);
            pstmt.setString(13, ulogin);
            rs = pstmt.executeQuery();
            rs.next();
            int count1 = rs.getInt("count");
            KWLJsonConverter KWL = new KWLJsonConverter();

            pstmt1 = conn.prepareStatement("select `by`, `to`, `logid` as id, `actiontext` as name, userid as paramid from actionlog"
                    + "  where `by`!=? and (`to`=?   or `to` IN(select cid from sharecalendarmap where userid= ? )   "
                    + "  or `to` IN(select projectid from projectmembers where userid=?  and status>2)   "
                    + "  or `to` IN(select communityid from communitymembers where userid=? and status>2)  "
                    + "  or `to` in(select  docid from docs where userid=? union  "
                    + "  SELECT docid FROM docs where docid in (select docid from docprerel where permission = '3' and userid!=?) union  "
                    + "  select docid from docs  where docid in(select docprerel.docid from docprerel where docprerel.userid in "
                    + "  (select  userid2 as userid from userrelations where userid1 =?  union select  userid1 as userid from userrelations where  "
                    + "  userid2= ?   and relationid = '3') and permission='1') union "
                    + "  select docs.docid from docs inner join docprerel on docprerel.docid = docs.docid where docs.docid in (select docprerel.docid from docprerel where docprerel.userid =? and docprerel.permission ='2'))"
                    + "  or `logid`  in (select logid from actionlog where  logid in (select logid from actionlogref where shwith =? ) and `by`!=? ) "
                    + "  or (`to`=  (select companyid from users where userid=?))) "
                    + " ORDER BY datedon desc LIMIT ? OFFSET ?");

            pstmt1.setString(1, ulogin);
            pstmt1.setString(2, ulogin);
            pstmt1.setString(3, ulogin);
            pstmt1.setString(4, ulogin);
            pstmt1.setString(5, ulogin);
            pstmt1.setString(6, ulogin);
            pstmt1.setString(7, ulogin);
            pstmt1.setString(8, ulogin);
            pstmt1.setString(9, ulogin);
            pstmt1.setString(10, ulogin);
            pstmt1.setString(11, ulogin);
            pstmt1.setString(12, ulogin);
            pstmt1.setString(13, ulogin);
            pstmt1.setInt(14, pagesize1);
            pstmt1.setInt(15, offset);

            rs1 = pstmt1.executeQuery();

            splitstring2 = KWL.GetJsonForGrid(rs1);
            splitstring2 = splitstring2.substring(0, splitstring2.length() - 1);
            splitstring2 += ",\"count\":[" + count1 + "]}";
            rs.close();
            rs1.close();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("DashboardHandler.getactionlog", e);
        } finally {
            DbPool.closeStatement(pstmt);
            DbPool.closeStatement(pstmt1);
        }
        return splitstring2;

    }

    public static String getdatewiseactionlog(Connection conn, String ulogin,
            String currDate, Integer interval) throws ServiceException {
        String splitstring2 = null;
        ResultSet rs1 = null;
        PreparedStatement pstmt1 = null;
        try {
            KWLJsonConverter KWL = new KWLJsonConverter();
            /*1-Documents    2-Projects    3-Community    4-Calendar    5-ToDoList    6-User*/
//                        pstmt1 = conn.prepareStatement("select `by`, `to`, `logid` as id, `actiontext` as name, userid as paramid, actions.groupid as gid from actionlog inner join actions on actionlog.actionid = actions.actionid"
//                        + " where `by`!=?  and DATEDIFF(DATE_SUB(DATE( ? ), INTERVAL  ?  DAY), DATE(datedon)) = 0 and ("
//                        +" `to`=?   or (`to` IN(select cid from sharecalendarmap where userid= ? )   "
//                        +" or `to` IN(select projectid from projectmembers where userid=?  and status>2)   "
//                        +" or `to` IN(select communityid from communitymembers where userid=? and status>2)  "
//                        +" or `to` in(select  docid from docs where userid=? "
//                        +" union  "
//                        +" SELECT docid FROM docs where docid in (select docid from docprerel where permission = '3' and userid!=?) "
//                        +" union  "
//                        +" select docid from docs  where docid in(select docprerel.docid from docprerel where docprerel.userid in ("
//                        +" select  userid2 as userid from userrelations where userid1 =?  "
//                        +" union "
//                        +" select  userid1 as userid from userrelations where  userid2= ?  and relationid = '3'"
//                        +" )"
//                        +" and permission='1'"
//                        +" ) "
//                        +" union "
//                        +" select docs.docid from docs inner join docprerel on docprerel.docid = docs.docid where docs.docid in (select docprerel.docid from docprerel where "
//                        +"  docprerel.userid =? and docprerel.permission ='2'))	)"
//                        +" or logid  in (select logid from actionlog where  logid in (select logid from actionlogref where shwith =? ) and `by`!=? ) "
//                        +" or (`to` In (select companyid from users where userid=?))) order by datedon desc");

            pstmt1 = conn.prepareStatement("select `by`, `to`, `logid` as id, `actiontext` as name, userid as paramid, actions.groupid as gid from actionlog "
                    + "inner join actions on actionlog.actionid = actions.actionid where `by` != ? and groupid in (2,6) "
                    + "and DATEDIFF(DATE_SUB(DATE( ? ), INTERVAL  ?  DAY), DATE(datedon)) = 0 and ( `to` = ? or "
                    + "(`to` IN(select projectid from projectmembers where userid = ? and status > 2)) "
                    + "or logid  in (select logid from actionlog where  logid in (select logid from actionlogref where shwith = ? ) and `by` != ? ) "
                    + "or (`to` In (select companyid from users where userid = ?))) order by datedon desc");
            pstmt1.setString(1, ulogin);
            pstmt1.setString(2, currDate);
            pstmt1.setInt(3, interval);
            pstmt1.setString(4, ulogin);
            pstmt1.setString(5, ulogin);
            pstmt1.setString(6, ulogin);
            pstmt1.setString(7, ulogin);
            pstmt1.setString(8, ulogin);
//			pstmt1.setString(9, ulogin);
//			pstmt1.setString(10, ulogin);
//			pstmt1.setString(11, ulogin);
//			pstmt1.setString(12, ulogin);
//			pstmt1.setString(13, ulogin);
//			pstmt1.setString(14, ulogin);
//			pstmt1.setString(15, ulogin);


            rs1 = pstmt1.executeQuery();

            splitstring2 = KWL.GetJsonForGrid(rs1);
            rs1.close();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("DashboardHandler.getactionlog", e);
        } finally {
            DbPool.closeStatement(pstmt1);
        }
        return splitstring2;

    }

    public static int getTotalActionlogDatediff(Connection conn, String ulogin, String currDate) throws ServiceException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count1 = 0;
        try {
//                        pstmt = conn.prepareStatement("select datediff( ? , min(date(datedon))) as count from actionlog");

            /*1-Documents    2-Projects    3-Community    4-Calendar    5-ToDoList    6-User*/
//                        pstmt = conn.prepareStatement("select datediff( ? , min(date(datedon))) as count from actionlog inner join actions on actionlog.actionid = actions.actionid"
//                        + " where `by`!=? and ("
//                        +" `to`=?   or (`to` IN(select cid from sharecalendarmap where userid= ? )   "
//                        +" or `to` IN(select projectid from projectmembers where userid=?  and status>2)   "
//                        +" or `to` IN(select communityid from communitymembers where userid=? and status>2)  "
//                        +" or `to` in(select  docid from docs where userid=? "
//                        +" union  "
//                        +" SELECT docid FROM docs where docid in (select docid from docprerel where permission = '3' and userid!=?) "
//                        +" union  "
//                        +" select docid from docs  where docid in(select docprerel.docid from docprerel where docprerel.userid in ("
//                        +" select  userid2 as userid from userrelations where userid1 =?  "
//                        +" union "
//                        +" select  userid1 as userid from userrelations where  userid2= ?  and relationid = '3'"
//                        +" )"
//                        +" and permission='1'"
//                        +" ) "
//                        +" union "
//                        +" select docs.docid from docs inner join docprerel on docprerel.docid = docs.docid where docs.docid in (select docprerel.docid from docprerel where "
//                        +"  docprerel.userid =? and docprerel.permission ='2'))	)"
//                        +" or logid  in (select logid from actionlog where  logid in (select logid from actionlogref where shwith =? ) and `by`!=? ) "
//                        +" or (`to` In (select companyid from users where userid=?))) order by datedon desc");

            pstmt = conn.prepareStatement("select datediff( ? , min(date(datedon))) as count from actionlog "
                    + "inner join actions on actionlog.actionid = actions.actionid where `by`!= ? and ( "
                    + "`to`= ? or (`to` IN(select projectid from projectmembers where userid = ? and status > 2) ) "
                    + "or logid  in (select logid from actionlog where  logid in (select logid from actionlogref where shwith = ? ) and `by` != ? ) "
                    + "or (`to` In (select companyid from users where userid = ? ))) order by datedon desc");

            pstmt.setString(1, currDate);
            pstmt.setString(2, ulogin);
            pstmt.setString(3, ulogin);
            pstmt.setString(4, ulogin);
            pstmt.setString(5, ulogin);
            pstmt.setString(6, ulogin);
            pstmt.setString(7, ulogin);
//			pstmt.setString(8, ulogin);
//			pstmt.setString(9, ulogin);
//			pstmt.setString(10, ulogin);
//			pstmt.setString(11, ulogin);
//			pstmt.setString(12, ulogin);
//			pstmt.setString(13, ulogin);
//			pstmt.setString(14, ulogin);

            rs = pstmt.executeQuery();
            rs.next();
            count1 = rs.getInt("count");
        } catch (SQLException e) {
            throw ServiceException.FAILURE("DashboardHandler.getalerts", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return count1;
    }

    public static String getalerts(String ulogin, String upagesize,
            String upageno) throws ServiceException {
        String splitstring2 = null;
        Connection conn = null;
        conn = DbPool.getConnection();
        PreparedStatement pstmt = null;
        PreparedStatement pstmt1 = null;
        int offset = (Integer.parseInt(upagesize) * Integer.parseInt(upageno));
        int pagesize1 = Integer.parseInt(upagesize);
        ResultSet rs = null;
        ResultSet rs1 = null;
        try {

            pstmt = conn.prepareStatement("select count(*) as count from (select alerts.alertid as id, alertval as name from alerts inner join useralerts on alerts.alertid = useralerts.alertid where useralerts.userid = ?) as temp;");

            pstmt.setString(1, ulogin);

            rs = pstmt.executeQuery();
            rs.next();
            int count1 = rs.getInt("count");
            KWLJsonConverter KWL = new KWLJsonConverter();

            pstmt1 = conn.prepareStatement("select alerts.alertid as id, alertval as name from alerts inner join useralerts on alerts.alertid = useralerts.alertid where useralerts.userid = ? ORDER BY id LIMIT ? OFFSET ?;");

            pstmt1.setString(1, ulogin);
            pstmt1.setInt(2, pagesize1);
            pstmt1.setInt(3, offset);

            rs1 = pstmt1.executeQuery();

            splitstring2 = KWL.GetJsonForGrid(rs1);
            splitstring2 = splitstring2.substring(0, splitstring2.length() - 1);
            splitstring2 += ",\"count\":[" + count1 + "]}";

            rs.close();
            rs1.close();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("DashboardHandler.getalerts", e);
        } finally {
            DbPool.closeStatement(pstmt);
            DbPool.closeStatement(pstmt1);
            DbPool.quietClose(conn);
        }
        return splitstring2;
    }

    public static JSONArray getAnnouncements(Connection conn, String ulogin,
            int pagesize1, int upageno, String companyid)
            throws ServiceException {
        PreparedStatement pstmt1 = null;
        JSONArray jA = new JSONArray();
        JSONObject j = new JSONObject();
        int offset = pagesize1 * upageno;
        ResultSet rs1 = null;
        try {
            CompanyDAO cd = new CompanyDAOImpl();
            Locale locale = cd.getCompanyLocale(conn, companyid);
            String tempdate = "";
            java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date newdate = new Date();
            tempdate = sdf1.format(newdate);
            tempdate = Timezone.toCompanyTimezone(conn, tempdate, companyid);
//            tempdate = Timezone.toUserTimezone(conn, tempdate, ulogin);
            Date todate = sdf1.parse(tempdate);
            Timestamp tzstamp = new Timestamp(todate.getTime());
            StringBuilder sb = new StringBuilder();
            pstmt1 = conn.prepareStatement("SELECT DISTINCT announcements.announceid AS id, announcements.announceval AS name FROM announcements "
                    + "INNER JOIN userannouncements ON announcements.announceid=userannouncements.announceid "
                    + "WHERE (userannouncements.userid=? OR userannouncements.userid=? "
                    + "OR userannouncements.userid IN (SELECT projectid FROM projectmembers WHERE userid=? AND inuseflag = 1 AND status >= 3)) "
                    + "AND ? >= `from` and ? <= `to` ORDER BY id LIMIT ? OFFSET ?");

            pstmt1.setString(1, ulogin);
            pstmt1.setString(2, companyid);
            pstmt1.setString(3, ulogin);
            pstmt1.setTimestamp(4, tzstamp);
            pstmt1.setTimestamp(5, tzstamp);
            pstmt1.setInt(6, pagesize1);
            pstmt1.setInt(7, offset);
            rs1 = pstmt1.executeQuery();
            boolean hasValue = false;
            while (rs1.next()) {
                j = new JSONObject();
                sb.delete(0, sb.length());
                hasValue = true;
                sb.append("<ul id=\"requestsUL\">");
                sb.append("<li class=\"announceLI\"><div style=\"margin-left:20px;\">");
                sb.append(rs1.getString("name"));
                sb.append("</div></li>");
                sb.append("</ul>");
                j.put("update", sb.toString());
                jA.put(j);
            }
            if (!hasValue) {
                sb.append(MessageSourceProxy.getMessage("pm.dashboard.widget.announcement.emptytext", null, locale));
                j.put("update", sb.toString());
                jA.put(j);
            }
            rs1.close();
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("DashboardHandler.getAnnouncements", ex);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("DashboardHandler.getAnnouncements", e);
        } catch (ParseException px) {
            throw ServiceException.FAILURE("DashboardHandler.getAnnouncements", px);
        } finally {
            DbPool.closeStatement(pstmt1);
            return jA;
        }
    }

    public static String getChartURL(Connection conn, String projectID, String ulogin, String type) {
        String data = "";
        try {
            ProjectDAO dao = new ProjectDAOImpl();
            String projName = dao.getProjectName(conn, projectID);
            String s = projdb.getProgressChartData(conn, projectID, ulogin, type);
            if (s.compareTo("") != 0 && s.contains(";") && s.endsWith("\n")) {
                String[] proj = s.split(";");
                if (projName.length() > 18) {
                    projName = projName.substring(0, 15).concat("...");
                }
                proj[0] = projName;
                data += StringUtil.join(";", proj);
            } else {
                data += s;
            }

        } catch (ServiceException ex) {
            Logger.getLogger(DashboardHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
        }
        return data;
    }
    
    public static String getChartURL(Connection conn, String ulogin, String limit, String offset, String type) {
//        String imgTag = "";
        String data = "";
        try {
            JSONObject res = getProjectListForChart(conn, ulogin, limit, offset);
            if (res.has("count") && res.has("data")) {
                JSONArray jarr = res.getJSONArray("data");
//                    String chartTitle="Projects+Progress+Chart";
//                    String projTitles="";
//                    String chartWidth = "325";
//                    String projCompAxis="";
//                    String barColor="";
//                    String textFontColor = "000000";
//                    String chartTitleStyle = "ffffff,12";
//                    String barBackColor = "bg,s,efefef00";
//                    String[] colorpicker = { "056492", "BFBFBF", "418CF0", "FCB441", "B03208" };
//                    String projCompPercent= (count==1)?"0,":"";
//                    int barWidth = 20;
//                    int barSpacing = 10;
//                    if(count>3) {
//                        barWidth=7;
//                        barSpacing=5;
//                    }
//                    int chartHeight = (barWidth+barSpacing)*count+70;
//                    int j=-1;
                for (int i = 0; i < jarr.length(); i++) {
                    JSONObject temp = jarr.getJSONObject(i);
//                        String pName = temp.getString("name");
//                        if(pName.length() > 13){
//                            pName = pName.substring(0, 10) + "..";
//                        }
//                        data += pName + ";" + temp.getString("complete") + "\n";
//                        projTitles += temp.getString("name") + "|";
//                        projCompAxis += "%20"+temp.getString("complete")+"%"+"|";
//                        j = (j>3) ? 0 : (j+=1);
//                        barColor +=  colorpicker[j]+"|";
//                    for(j=count-1;j>= 0 ; j--){
//                        JSONObject temp = jarr.getJSONObject(j);
//                        projCompPercent += temp.getString("complete")+",";
//                    }
//                    projTitles = projTitles.substring(0,projTitles.length()-1);
//                    barColor = barColor.substring(0,barColor.length()-1);
//                    projCompPercent = projCompPercent.substring(0,projCompPercent.length()-1);
//                    String URL = "http://chart.apis.google.com/chart?cht=bhg"
//                        +"&chco="+barColor
//                        +"&chd=t:"+projCompPercent
//                        +"&chf="+barBackColor
//                        +"&chs="+chartWidth+"x"+Integer.toString(chartHeight)
//                        +"&chxt=y,x,x,r&chxl=0:|"+projTitles+"|2:||||Overall%20Progress|||"+"|3:|"+projCompAxis
//                        +"&chxs=0,"+textFontColor+",10|1,"+textFontColor+",10|2,"+textFontColor+",12|3,"+textFontColor+",10"
//                        +"&chbh="+Integer.toString(barWidth)+","+Integer.toString(barSpacing)
//                        +"&chtt="+chartTitle
//                        +"&chts="+chartTitleStyle;
//                    imgTag = "<div><img src=\"" + URL + "\"/></div>";
                    String s = projdb.getProgressChartData(conn, temp.getString("projectid"), ulogin, type);
                    if (s.compareTo("") != 0 && s.contains(";") && s.endsWith("\n")) {
                        String[] proj = s.split(";");
                        String nm = temp.getString("name");
                        if (nm.length() > 18) {
                            nm = nm.substring(0, 15).concat("...");
                        }
                        proj[0] = nm;
                        data += StringUtil.join(";", proj);
                    } else {
                        data += s;
                    }

                }
            }
        } catch (ServiceException ex) {
        } catch (JSONException ex) {
            Logger.getLogger(DashboardHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
        }
//        return imgTag;
        return data;
    }

    public static String getFeaturedCommunities(Connection conn, String ulogin,
            int offset, int pagesize) throws ServiceException {
        ResultSet rs = null;
        ResultSet rs1 = null;
        PreparedStatement pstmt = null;
        PreparedStatement pstmt1 = null;
        String splitstring2 = null;
        try {
            pstmt = conn.prepareStatement("select count(*) as count from (select community.communityid as id,community.communityname as name,image as img, count(userid) from community inner join communitymembers on community.communityid=communitymembers.communityid group by communityname having count(userid) >= 2) as temp;");

            rs = pstmt.executeQuery();
            rs.next();
            int count1 = rs.getInt("count");
            KWLJsonConverter KWL = new KWLJsonConverter();
            pstmt1 = conn.prepareStatement("select community.communityid as id,community.communityname as name,image as img, count(userid) from community inner join communitymembers on community.communityid=communitymembers.communityid group by communityname having count(userid) >= 2 LIMIT ? OFFSET ?;");

            pstmt1.setInt(1, pagesize);
            pstmt1.setInt(2, offset);

            rs1 = pstmt1.executeQuery();
            splitstring2 = KWL.GetJsonForGrid(rs1);
            splitstring2 = splitstring2.substring(0, splitstring2.length() - 1);
            splitstring2 += ",\"count\":[" + count1 + "]}";

            rs.close();
            rs1.close();

        } catch (SQLException e) {
            throw ServiceException.FAILURE(
                    "DashboardHandler.getFeaturedCommunities", e);
        } finally {
            DbPool.closeStatement(pstmt);
            DbPool.closeStatement(pstmt1);
        }
        return " " + splitstring2;
    }

    // fetch only active project, use for dashboard project list
    public static String getProjectListMember(Connection conn, String ulogin,
            int pagesize, int offset) throws ServiceException {
        ResultSet rs1 = null;
        PreparedStatement pstmt1 = null;
        String splitstring2 = null;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-d HH:mm:ss");
        try {
            KWLJsonConverter KWL = new KWLJsonConverter();
            String query = "SELECT t1.projectid AS id,projectname AS name,status,image AS img, description,createdon,count AS members, projectmembers.quicklink "
                    + "FROM (SELECT projectid,count(projectid) AS count "
                    + "FROM projectmembers WHERE status IN (3,4,5) AND inuseflag = 1 GROUP BY(projectid)) AS t1 "
                    + "INNER JOIN projectmembers ON projectmembers.projectid = t1.projectid "
                    + "INNER JOIN project ON project.projectid = t1.projectid where projectmembers.userid = ? "
                    + " and projectmembers.status in (3,4,5) and inuseflag = 1 and project.archived=0 GROUP BY (projectmembers.projectid) ORDER BY projectname LIMIT ? OFFSET ?";
//            String query = "SELECT t1.projectid AS id,projectname AS name,status,image AS img, description,createdon,count AS members " +
//                    "FROM (SELECT projectid,count(projectid) AS count FROM projectmembers WHERE status IN (3,4,5) AND inuseflag = 1 GROUP BY(projectid)) AS t1 " +
//                    "INNER JOIN (SELECT project.projectid,description,createdon, status,image, projectname FROM projectmembers INNER JOIN project ON project.projectid = projectmembers.projectid WHERE userid = ? AND status IN (3,4,5) AND inuseflag=1) AS t2 " +
//                    "ON t1.projectid = t2.projectid ORDER BY members DESC LIMIT ? OFFSET ?";
            pstmt1 = conn.prepareStatement(query);
            pstmt1.setString(1, ulogin);
            pstmt1.setInt(2, pagesize);
            pstmt1.setInt(3, offset);
            rs1 = pstmt1.executeQuery();
            splitstring2 = KWL.GetJsonForGrid(rs1);
            if (splitstring2.compareTo("{data:{}}") != 0) {
                JSONObject jobj = new JSONObject(splitstring2);
                for (int i = 0; i < jobj.getJSONArray("data").length(); i++) {
                    String sdtStr = jobj.getJSONArray("data").getJSONObject(i).getString("createdon");
//                    sdtStr = Timezone.toUserTimezone(conn, sdtStr, ulogin);
                    if (sdtStr.compareTo("") != 0) {
                        java.util.Date dt = sdf.parse(sdtStr);
                        sdtStr = sdf.format(dt);
                        jobj.getJSONArray("data").getJSONObject(i).remove("createdon");
                        jobj.getJSONArray("data").getJSONObject(i).put("createdon", sdtStr);
                    }
                }
            }
            rs1.close();
            pstmt1 = conn.prepareStatement("SELECT count(projectid) AS count FROM project "
                    + "WHERE projectid IN (SELECT projectid FROM projectmembers WHERE userid = ? AND status IN (3,4,5)) AND archived = 0");
            pstmt1.setString(1, ulogin);
            rs1 = pstmt1.executeQuery();
            int prjCnt = 0;
            if (rs1.next()) {
                prjCnt = rs1.getInt("count");
            }
            splitstring2 = "{count: " + Integer.toString(prjCnt) + "," + splitstring2.substring(1, (splitstring2.length() - 1)) + "}";
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("DashboardHandler.getProjectListMember", ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("DashboardHandler.getProjectListMember", ex);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("DashboardHandler.getProjectListMember", e);
        } finally {
            DbPool.closeStatement(pstmt1);
        }
        return splitstring2;
    }

    public static String getProjectsOfMember(Connection conn, String ulogin) throws ServiceException {
        ResultSet rs1 = null;
        PreparedStatement pstmt1 = null;
        String[] dates = {};
        com.krawler.utils.json.base.JSONObject resobj = new JSONObject();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        try {
            JSONArray jo = new JSONArray();
            boolean tz = false;
            String companyid = CompanyHandler.getCompanyByUser(conn, ulogin);
            String query = "SELECT t1.projectid AS id,projectname AS name,status,image AS img, description,createdon,count AS members,"
                    + "startdate, nickname, quicklink, notification_subscription as notifSubVal, milestonestack "
                    + "FROM (SELECT projectid,count(projectid) AS count "
                    + "FROM projectmembers WHERE status IN (3,4,5) AND inuseflag = 1 GROUP BY(projectid)) AS t1 "
                    + "INNER JOIN projectmembers ON projectmembers.projectid = t1.projectid "
                    + "INNER JOIN project ON project.projectid = t1.projectid "
                    + "INNER JOIN userlogin ON userlogin.userid = projectmembers.userid "
                    + "WHERE projectmembers.userid = ? and userlogin.isactive = true "
                    + "and projectmembers.status in (3,4,5) and inuseflag = 1 and project.archived=0 GROUP BY (projectmembers.projectid) ORDER BY projectname";
            pstmt1 = conn.prepareStatement(query);
            pstmt1.setString(1, ulogin);
            rs1 = pstmt1.executeQuery();
            while (rs1.next()) {
                JSONObject j = new JSONObject();
                JSONObject jbj = new JSONObject();
                String perm = Forum.getProjectMembership(conn, ulogin, rs1.getString("id"));
                j.put("permission", perm);
                String result = projdb.getProjState(conn, ulogin, rs1.getString("id"), "2");
                JSONObject json = new JSONObject(result);
                String planview = json.getString("planview");
                j.put("planview", planview);
//                String companyHolidays = projdb.getNWWeekAndCompHolidays(conn,  rs1.getString("id"));
//                String returnStr = "["+companyHolidays+"]";
                String nWWDays = projdb.getNonWorkWeekdays(conn, rs1.getString("id"));
                j.put("nonWorkWeekDays", nWWDays);
                String startdateString = "";
                String createdon = rs1.getString("createdon");
                String id = rs1.getString("id");
                pstmt1 = conn.prepareStatement("update project set startdate = ? where projectid = ?");
                if (StringUtil.isNullOrEmpty(rs1.getString("startdate"))) {
                    String temp = projdb.getminmaxProjectDate(conn, rs1.getString("id"), ulogin);
                    if (!StringUtil.isNullOrEmpty(temp)) {
                        dates = temp.split(",");
                        if (dates != null) {
                            pstmt1.setString(1, dates[0]);
                            pstmt1.setString(2, id);
                            startdateString = dates[0];
                            tz = true;
                        } else {
                            pstmt1.setString(1, createdon);
                            pstmt1.setString(2, id);
                            startdateString = createdon;
                        }
                        pstmt1.executeUpdate();
                    } else {
                        pstmt1.setString(1, createdon);
                        pstmt1.setString(2, id);
                        startdateString = createdon;
                        pstmt1.executeUpdate();
                    }
                } else {
                    startdateString = rs1.getString("startdate");
                }
                if (!tz) {
                    startdateString = Timezone.toCompanyTimezone(conn, startdateString, companyid);
                }
                CustomColumn  cc = CCManager.getCustomColumn(companyid);
                ColumnSet ccRecordSet =  cc.getColumnsData(conn, "Project",id);
                j.put("startdate", startdateString);
                j.put("id", id);
                j.put("name", rs1.getString("name"));
                j.put("img", rs1.getString("img"));
                createdon = Timezone.toCompanyTimezone(conn, createdon, companyid);
                java.util.Date tempDt = sdf.parse(createdon);
                j.put("createdon", sdf.format(tempDt));
                j.put("about", rs1.getString("description"));
                j.put("nickname", rs1.getString("nickname"));
                j.put("quicklink", rs1.getBoolean("quicklink"));
                j.put("milestonestack", rs1.getBoolean("milestonestack"));
                j.put("notifSubVal", rs1.getInt("notifSubVal"));
                JSONObject jCC = new JSONObject();
                while(ccRecordSet.next()){
                    if(ccRecordSet.getObject()!=null)
                        jCC.put(ccRecordSet.getDataIndex(),ccRecordSet.getObject().toString());
                }
                j.put("cusCol", jCC);
                jo.put(j);
            }
            rs1.close();
            pstmt1 = conn.prepareStatement("SELECT count(projectid) AS count FROM project "
                    + "WHERE projectid IN (SELECT projectid FROM projectmembers WHERE userid = ? AND status IN (3,4,5)) AND archived = 0");
            pstmt1.setString(1, ulogin);
            rs1 = pstmt1.executeQuery();
            int prjCnt = 0;
            if (rs1.next()) {
                prjCnt = rs1.getInt("count");
            }
            String companyHolidays = projdb.getCmpHolidaydays(conn, companyid);
            resobj.put("count", prjCnt);
            resobj.put("data", jo);
            resobj.put("companyHolidays", companyHolidays);
        } catch (ParseException e) {
            throw ServiceException.FAILURE("Forum.getProjectOfMember", e);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("Forum.getProjectOfMember", e);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("Forum.getProjectOfMember", e);
        } finally {
            DbPool.closeStatement(pstmt1);
        }
        return resobj.toString();
    }

    public static JSONObject getProjectListForChart(Connection conn, String ulogin, String limit, String offset) throws ServiceException {
        DbResults projListRS = null;
        DbResults taskPercentRS = null;
        JSONObject jobj = new JSONObject();
        int totalProjs = 0;
        String GET_PROJECTS_COUNT = "SELECT COUNT(*) as count from projectmembers "
                + "INNER JOIN project ON projectmembers.projectid=project.projectid WHERE userid = ? and status in ( 3, 4, 5) and inuseflag = 1 and project.archived=0";
        String SELECT_PROJECTS = "SELECT pid, name FROM (SELECT project.projectid as pid,projectname as name from projectmembers "
                + "INNER JOIN project ON projectmembers.projectid=project.projectid WHERE userid = ? and status in ( 3, 4, 5) and inuseflag = 1 and project.archived=0 ORDER BY name limit ? offset ?) "
                + "AS r1 ORDER BY name DESC";
        String SELECT_TASKS = "SELECT CEIL((sum(percentcomplete) / COUNT(*))) as complete FROM proj_task where projectid = ?";
        try {
            projListRS = DbUtil.executeQuery(conn, GET_PROJECTS_COUNT, new Object[]{ulogin});
            if (projListRS.next()) {
                totalProjs = projListRS.getInt("count");
            }
            projListRS = null;
            projListRS = DbUtil.executeQuery(conn, SELECT_PROJECTS, new Object[]{ulogin, Integer.parseInt(limit), Integer.parseInt(offset)});
            while (projListRS.next()) {
                com.krawler.utils.json.base.JSONObject jtemp = new com.krawler.utils.json.base.JSONObject();
                jtemp.put("name", projListRS.getString("name"));
                jtemp.put("projectid", projListRS.getString("pid"));
                taskPercentRS = DbUtil.executeQuery(conn, SELECT_TASKS, new Object[]{projListRS.getString("pid")});
                if (taskPercentRS.next() && taskPercentRS.getObject("complete") != null) {
                    jtemp.put("complete", taskPercentRS.getObject("complete").toString());
                } else {
                    jtemp.put("complete", 0);
                }
                jobj.append("data", jtemp);
            }
            jobj.append("count", totalProjs);
        } catch (Exception ex) {
            Logger.getLogger(DashboardHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }

    public static String getTasksByProjectPriority(Connection conn, String userid)//not in use
            throws ServiceException, SQLException {
        DbResults rs = null;
        DbResults rsForSubQ = null;
        DbResults rsForSubQCount = null;
        String str = null;
        String projid = null;
        int count = 0, sno = 0;
        com.krawler.utils.json.base.JSONObject jdata = new com.krawler.utils.json.base.JSONObject();
        String COUNT_TASK_STARTING_BY_DAYS = " select count(*) as count"
                + " from proj_task p"
                + " inner join project on project.projectid = p.projectid"
                + " inner join users on project.companyid = users.companyid"
                + " where (date(p.startdate) = date(now()) or date(p.startdate) = adddate(date(now()),1))"
                + " and project.projectid = ? and users.userid = ? ;";

        String COUNT_TASK_ENDING_BY_DAYS = " select count(*) as count"
                + " from proj_task p"
                + " inner join project on project.projectid = p.projectid"
                + " inner join users on project.companyid = users.companyid"
                + " where (date(p.enddate) = date(now()) or date(p.enddate) = adddate(date(now()),1))"
                + " and project.projectid = ? and users.userid = ? ;";

        String SELECT_TASK_STARTING_BY_DAYS = " select taskname,"
                //                + " p.percentcomplete as complete"
                + " case date(p.startdate)"
                + " when  date(now())  then 'today'"
                + " when  adddate(date(now()),1)  then 'tommorow'"
                + " end as taskdate"
                + " from proj_task p"
                + " inner join project on project.projectid = p.projectid"
                + " inner join users on project.companyid = users.companyid"
                + " where (date(p.startdate) = date(now()) or date(p.startdate) = adddate(date(now()),1))"
                + " and project.projectid = ? and users.userid = ? order by taskdate;";

        String SELECT_TASK_ENDING_BY_DAYS = " select taskname , p.percentcomplete as complete,"
                + " case date(p.enddate)"
                + " when  date(now())  then 'today'"
                + " when  adddate(date(now()),1)  then 'tommorow'"
                + " end as taskdate"
                + " from proj_task p"
                + " inner join project on project.projectid = p.projectid"
                + " inner join users on project.companyid = users.companyid"
                + " where (date(p.enddate) = date(now()) or date(p.enddate) = adddate(date(now()),1))"
                + " and project.projectid = ? and users.userid = ? order by taskdate;";

        String GET_PROJECTS_COUNT = "SELECT COUNT(*) as count from projectmembers "
                + " INNER JOIN project ON projectmembers.projectid=project.projectid WHERE userid = ?  and status in ( 3, 4, 5) and inuseflag = 1";
        String SELECT_PROJECTS = "SELECT project.projectid as pid,projectname as name from projectmembers "
                + " INNER JOIN project ON projectmembers.projectid=project.projectid WHERE userid = ?  and status in ( 3, 4, 5) and inuseflag = 1 ORDER BY name";
        boolean isEndNone = false;
        boolean isStNone = false;
        boolean isDataPresent = false;
        try {
            rs = DbUtil.executeQuery(conn, GET_PROJECTS_COUNT, new Object[]{userid});
            count = rs.getInt("count");
            if (count > 0) {
                rs = DbUtil.executeQuery(conn, SELECT_PROJECTS, new Object[]{userid});
                while (rs.next()) {
                    JSONObject jproj = new JSONObject();
                    JSONObject jtask = new JSONObject();
                    JSONObject jtaskHeadSt = new JSONObject();
                    JSONObject jtaskHeadEn = new JSONObject();


                    projid = rs.getString("pid");
                    if (StringUtil.isNullOrEmpty(projid)) {
                        projid = "%";
                    }
                    isEndNone = false;
                    isStNone = false;

                    /*Count number of tasks skip div creation if none...*/
                    rsForSubQCount = DbUtil.executeQuery(conn,
                            COUNT_TASK_STARTING_BY_DAYS,
                            new Object[]{projid, userid});
                    if (rsForSubQCount.getInt("count") > 0) {
                        /*Tasks starting today,tomrrow*/
                        sno = 0;
                        rsForSubQ = DbUtil.executeQuery(conn,
                                SELECT_TASK_STARTING_BY_DAYS,
                                new Object[]{projid, userid});
                        jtaskHeadSt.put("name", "<li>New Tasks</li>");
                        jtask.append("task", jtaskHeadSt);
                        while (rsForSubQ.next()) {
                            String taskName = rsForSubQ.getString("taskname");
                            if (taskName.length() >= 30) {
                                taskName = taskName.substring(0, 30);
                                taskName += "...";
                            }
                            com.krawler.utils.json.base.JSONObject jtempst = new com.krawler.utils.json.base.JSONObject();
                            jtempst.put("name", "<div  class=\"anElem\" style=\"margin-left:16px\"><li class=\"tasklist\" style=\"padding-left: 16px;\">"
                                    + taskName + "&nbsp;-"
                                    + "&nbsp;<i>" + rsForSubQ.getObject("taskdate").toString() + "</i>"
                                    //                                    + "&nbsp;(" + rsForSubQ.getObject("complete").toString() + "%" + " Complete)"
                                    + "</li></div>");
                            jtask.append("task", jtempst);
                            taskName = "";
                        }
                    } else {
                        isStNone = true;
                    }

                    /*Count number of tasks skip div creation if none...*/
                    rsForSubQCount = DbUtil.executeQuery(conn,
                            COUNT_TASK_ENDING_BY_DAYS,
                            new Object[]{projid, userid});
                    if (rsForSubQCount.getInt("count") > 0) {
                        sno = 0;
                        /*Tasks ending today,tomrrow*/
                        rsForSubQ = DbUtil.executeQuery(conn,
                                SELECT_TASK_ENDING_BY_DAYS,
                                new Object[]{projid, userid});
                        jtaskHeadEn.put("name", "<li>Tasks Due</li>");
                        jtask.append("task", jtaskHeadEn);
                        while (rsForSubQ.next()) {
                            String taskName = rsForSubQ.getString("taskname");
                            if (taskName.length() >= 30) {
                                taskName = taskName.substring(0, 30);
                                taskName += "...";
                            }
                            com.krawler.utils.json.base.JSONObject jtempen = new com.krawler.utils.json.base.JSONObject();
                            jtempen.put("name", "<div class=\"anElem\" style=\"margin-left:16px;\"><li class=\"tasklist\" style=\"padding-left:16px\">"
                                    + taskName + "&nbsp;-"
                                    + "&nbsp;<i>" + rsForSubQ.getObject("taskdate").toString() + "</i>"
                                    + "&nbsp;(" + rsForSubQ.getObject("complete").toString() + "%" + " Complete)"
                                    + "</li></div>");
                            jtask.append("task", jtempen);
                            taskName = "";
                        }
                    } else {
                        isEndNone = true;
                    }

                    if (!(isEndNone && isStNone)) {
                        jtask.put("projectname", rs.getString("name"));
                        jtask.put("noprojects", "");
                        jproj.put("project", jtask);
                        jdata.append("data", jproj);
                        isDataPresent = true;
                    }
                }

                jdata.append("count", count);
                str = jdata.toString();
            }

            if (!isDataPresent) {
                str = KWLErrorMsgs.dataWelcome;
                /*str =  "{\"count\":[0],\"data\":[{\"project\":{\"task\":[{\"name\":\"\"}],\"projectname\":\"\","
                +"\"noprojects\":\""
                +"Welcome! There seems to be no projects that you are associated with."
                +" Alternatively there are no new or due tasks assigned to you."
                +" Once you are added to a project or are assigned tasks,"
                +" please visit this section to get an overview of new or due tasks that you are working on."
                +"Thank You."
                +"\"}}]}";*/
            }
        } catch (JSONException jE) {
            Logger.getLogger(DashboardHandler.class.getName()).log(Level.SEVERE, null, jE);
            str = KWLErrorMsgs.dataErrorConn;
            /*str = "{\"count\":[0],\"data\":[{\"project\":{\"task\":[{\"name\":\"\"}],\"projectname\":\"\","
            +"\"noprojects\":\""
            +"An error occurred while connecting to server"
            +"\"}}]}";*/
        }
        return str;
    }

    public static String getUpdatesForDashboard(Connection conn, String userid) throws ServiceException, JSONException {// not in use
        DbResults rs = null;
        int count = 0;
        String COUNT_UPDATES_LAST7DAYS = "select datediff( date(now()) , min(date(datedon))) as count from actionlog "
                + " inner join actions on actionlog.actionid = actions.actionid where `by`!= ? and ( "
                + " `to`= ? or (`to` IN(select projectid from projectmembers where userid = ? and status > 2) ) "
                + " or logid  in (select logid from actionlog "
                + " where  logid in (select logid from actionlogref where shwith = ? ) and `by` != ? ) "
                + " or (`to` In (select companyid from users where userid = ? ))) order by datedon desc";



        String GET_UPDATES_LAST7DAYS = "select `by`, `to`, `logid` as id, `actiontext` as name, userid as paramid, actions.groupid as gid "
                + " from actionlog "
                + " inner join actions on actionlog.actionid = actions.actionid where `by` != ? and groupid in (2,6) "
                + " and DATEDIFF(DATE_SUB(DATE( now() ), INTERVAL  ?  DAY), DATE(datedon)) = 0 and ( `to` = ? or "
                + " (`to` IN(select projectid from projectmembers where userid = ? and status > 2)) "
                + " or logid  in (select logid from actionlog "
                + " where logid in (select logid from actionlogref where shwith = ? ) and `by` != ? ) "
                + " or (`to` In (select companyid from users where userid = ?))) order by datedon desc";
        int dateCnt = 0;
        int interval = 0;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMMM d");
        com.krawler.utils.json.base.JSONObject jtempen = null;
        com.krawler.utils.json.base.JSONObject jupdates = new com.krawler.utils.json.base.JSONObject();

        rs = DbUtil.executeQuery(conn, COUNT_UPDATES_LAST7DAYS, new Object[]{userid, userid, userid, userid, userid, userid});
        if (rs.next()) {
            count = rs.getInt("count");
        }
        String result = null;
        try {
            while (dateCnt < 7 && count >= interval) {
                if (interval == 365) {
                    sdf = new java.text.SimpleDateFormat("MMMM d, yyyy");
                }

                rs = DbUtil.executeQuery(conn, GET_UPDATES_LAST7DAYS, new Object[]{userid, interval, userid, userid, userid, userid, userid});
                if (rs.next()) {
                    jtempen = new com.krawler.utils.json.base.JSONObject();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(new java.util.Date());
                    cal.add(Calendar.DATE, -interval);
                    jtempen.put("date", sdf.format(cal.getTime()));
                    jtempen.put("name", rs.getObject("name").toString());
                    jtempen.put("to", rs.getObject("to").toString());
                    jtempen.put("by", rs.getObject("by").toString());
                    jtempen.put("id", rs.getObject("id").toString());
                    jtempen.put("paramid", rs.getObject("paramid").toString());
                    jtempen.put("gid", rs.getObject("gid").toString());
                    jtempen.put("type", "update");
                    jupdates.append("updates", jtempen);
                    dateCnt++;
                }
                interval += 1;
            }
            result = jupdates.getJSONArray("updates").toString();
        } catch (JSONException je) {
            result = "{}";
            Logger.getLogger(DashboardHandler.class.getName()).log(Level.SEVERE, null, je);
        }
        return result;
    }

    public static String getContentDiv(String typeStr) {
        String div = "<div class=\"statuspanelcontentitemouter\"><div class=\"statuspanelcontentiteminner\"><div class=\"statusitemimg "
                + typeStr + " pwnd"
                + "\">&nbsp;</div><div class=\"statusitemcontent\">";
        return div;
    }
    /*public static String getContentTR(String typeStr, String[] data){
    String tr = "";
    tr= "<tr>" +
    "<td><div class=\"statusitemimg " +typeStr +"\">&nbsp;</div><div class=\"statusitemcontent\">";
    for(int i = 0; i < data.length; i++){
    
    }
    return tr;
    }*/

    public static String getContentMessage(String projStr, String projid, String projname, String taskName, String complete) {
        String info = String.format(projStr,
                new Object[]{projid, projid, com.krawler.svnwebclient.util.UrlUtil.encode(projname), taskName}) + complete;
        return info;
    }

    public static String getContentMessageForReport(String projStr, String projectname, String projectid, String taskName) {
        String info = "<div class=\"contentmsgspan\">" + String.format(projStr,
                new Object[]{taskName, projectname, projectid, taskName})
                + "</div>";
        return info;
    }

    public static String getContentSpan(String textStr) {
        String className = "litetext";
        String span = " - <span class=\"" + className + "\">"
                + textStr
                + "</span></div><div class=\"statusclr\"></div>";
        return span;
    }

    public static StringBuilder getDashboardTasksList(Connection conn, String userid, String companyid) throws ServiceException {
        String projid = null;
        StringBuilder finalString = new StringBuilder();
        StringBuilder dueTask = new StringBuilder();
        StringBuilder newTask = new StringBuilder();
        StringBuilder overdue = new StringBuilder();
        try {
            boolean sub = isSubscribed(conn, companyid, "usrtk");
            String projectlist = getProjectList(conn, userid, 1000, 0, "");
            JSONObject projListObj = new JSONObject(projectlist);
            JSONArray projList = projListObj.getJSONArray("data");
            for (int i = 0; i < projList.length(); i++) {
                JSONObject temp = projList.getJSONObject(i);
                projid = temp.getString("id");
                JSONObject membership = new JSONObject(Forum.getProjectMembership(conn, userid, projid));
                JSONObject userProjData = membership.getJSONArray("data").getJSONObject(0);
                boolean uT = (userProjData.getInt("connstatus") != 4 && userProjData.getInt("planpermission") == 2 && sub);
                String projName = projdb.getProjectName(conn, projid);
                int mod = 3;
                if (userProjData.getInt("connstatus") == 4) {
                    mod = 4;
                } else if (uT) {
                    mod = 6;
                }
                getDueTasks(conn, dueTask, projid, mod, userid, projName);
                getNewTasks(conn, newTask, projid, mod, userid, projName);
                getOverDueTasks(conn, overdue, projid, mod, userid, projName);
                projid = "";
            }
//            finalString.append(getDashTable("Updates", 3));
            finalString.append(newTask);
            finalString.append(dueTask);
            finalString.append(overdue);
        } catch (JSONException e) {
        } catch (Exception ex) {
            finalString.append(ex.getMessage());
        }
        return finalString;
    }

    public static void getOverDueTasks(Connection conn, StringBuilder overdue, String projid, int mode, String userid, String projname)
            throws ServiceException {
        try {
            ResultSet rsForSubQ = null;
            int limit = 15;
            Object param = new Object[]{projid};
            String projStr = "<a href=\"#\" onclick=\"navigate('q','%s','%s','%s','projectplanclicked');\">%s</a>";
            String sql = "SELECT datediff(date(now()),date(proj_task.enddate)) AS tasklength,"
                    + "taskname, percentcomplete AS complete,taskid FROM proj_task "
                    + "WHERE projectid = ? AND (date(proj_task.enddate) < date(now()) AND percentcomplete < 100) "
                    + "ORDER BY tasklength ASC LIMIT " + limit;
            PreparedStatement p = conn.prepareStatement(sql);
            p.setString(1, projid);
            if (mode == 6) {
                sql = "SELECT datediff(date(now()),date(proj_task.enddate)) AS tasklength,"
                        + "taskname, percentcomplete AS complete,proj_task.taskid AS taskid "
                        + "FROM proj_task INNER JOIN proj_taskresourcemapping ON proj_task.taskid = proj_taskresourcemapping.taskid "
                        + "WHERE projectid = ? AND proj_taskresourcemapping.resourceid = ? AND (date(proj_task.enddate) < date(now()) "
                        + "AND percentcomplete < 100) "
                        + "ORDER BY tasklength ASC LIMIT " + limit;
                p = conn.prepareStatement(sql);
                p.setString(1, projid);
                p.setString(2, userid);
            }
            rsForSubQ = p.executeQuery();
            while (rsForSubQ.next()) {
                String resrs = "";
                String taskName = rsForSubQ.getString("taskname");
                int tasklength = Integer.parseInt(rsForSubQ.getObject("tasklength").toString());
                overdue.append(getContentDiv("overduetask"));
                overdue.append(getContentMessage(projStr, projid, projname, taskName,
                        " - (" + rsForSubQ.getObject("complete").toString() + "%" + " Complete)"));
                if (tasklength == 1) {
                    resrs = "<span class = 'redtext'>Overdue by 1 day</span>";
                } else {
                    resrs = "<span class = 'redtext'>Overdue by " + rsForSubQ.getObject("tasklength").toString() + " days</span>";
                }
                if (mode == 4) {
                    String r = getTaskResources(conn, rsForSubQ.getString("taskid"), projid, userid);
                    r = getResourceLinks(conn, r);
                    if (!StringUtil.isNullOrEmpty(r)) {
                        resrs += " [ Assigned to : " + r + " ]";
                    }
                }
                overdue.append(getContentSpan(resrs));
                taskName = "";
                tasklength = 0;
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }

    public static void getDueTasks(Connection conn, StringBuilder dueTask, String projid, int mode, String userid, String projname)
            throws ServiceException {
        try {
            ResultSet rsForSubQ = null;
            int limit = 15;
            String projStr = "<a href=\"#\" onclick=\"navigate('q','%s','%s','%s','projectplanclicked');\">%s</a>";
            Object param = new Object[]{projid};
            String sql = "SELECT datediff(date(proj_task.enddate),date(now())) AS tasklength, "
                    + "taskname, percentcomplete AS complete,taskid FROM proj_task "
                    + "WHERE projectid = ? AND (date(proj_task.enddate)>=date(now())) AND (date(proj_task.enddate) <= adddate(date(now()),7)) "
                    + "ORDER BY tasklength ASC LIMIT " + limit;
            PreparedStatement p = conn.prepareStatement(sql);
            p.setString(1, projid);
            if (mode == 6) {
                sql = "SELECT datediff(date(proj_task.enddate),date(now())) AS tasklength, "
                        + "taskname, percentcomplete AS complete,proj_task.taskid AS taskid "
                        + "FROM proj_task INNER JOIN proj_taskresourcemapping ON proj_task.taskid = proj_taskresourcemapping.taskid "
                        + "WHERE projectid = ? AND proj_taskresourcemapping.resourceid = ? AND (date(proj_task.enddate)>=date(now())) "
                        + "AND (date(proj_task.enddate) <= adddate(date(now()),7)) "
                        + "ORDER BY tasklength ASC LIMIT " + limit;
                p = conn.prepareStatement(sql);
                p.setString(1, projid);
                p.setString(2, userid);
            }
            rsForSubQ = p.executeQuery();
            while (rsForSubQ.next()) {
                String resrs = "";
                String taskName = rsForSubQ.getString("taskname");
                int tasklength = Integer.parseInt(rsForSubQ.getObject("tasklength").toString());
                if (tasklength == 0 || tasklength == 1) {
                    dueTask.append(getContentDiv("duetask"));
                    dueTask.append(getContentMessage(projStr, projid, projname, taskName,
                            " - (" + rsForSubQ.getObject("complete").toString() + "%" + " Complete)"));
                    if (tasklength == 0) {
                        resrs = "Due today";
                    } else {
                        resrs = "Due tomorrow";
                    }
                } else if (tasklength > 1) {
                    dueTask.append(getContentDiv("duetask"));
                    dueTask.append(getContentMessage(projStr, projid, projname, taskName,
                            " - (" + rsForSubQ.getObject("complete").toString() + "%" + " Complete)"));
                    resrs = "Due in " + rsForSubQ.getObject("tasklength").toString() + " days";
                }
                if (mode == 4) {
                    String r = getTaskResources(conn, rsForSubQ.getString("taskid"), projid, userid);
                    r = getResourceLinks(conn, r);
                    if (!StringUtil.isNullOrEmpty(r)) {
                        resrs += " [ Assigned to : " + r + " ]";
                    }
                }
                dueTask.append(getContentSpan(resrs));
                taskName = "";
                tasklength = 0;
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }

    public static void getNewTasks(Connection conn, StringBuilder newTask, String projid, int mode, String userid, String projname)
            throws ServiceException {
        try {
            ResultSet rsForSubQ = null;
            int limit = 15;
            Object param = new Object[]{projid};
            String sql = "SELECT datediff(date(proj_task.startdate),date(now())) AS tasklength, "
                    + "taskname, percentcomplete AS complete,taskid FROM proj_task "
                    + "WHERE projectid = ? AND (date(proj_task.startdate)>=date(now())) AND (date(proj_task.startdate) <= adddate(date(now()),7)) "
                    + "ORDER BY tasklength ASC LIMIT " + limit;
            PreparedStatement p = conn.prepareStatement(sql);
            p.setString(1, projid);
            if (mode == 6) {
                sql = "SELECT datediff(date(proj_task.startdate),date(now())) AS tasklength, "
                        + "taskname, percentcomplete AS complete,proj_task.taskid AS taskid "
                        + "FROM proj_task INNER JOIN proj_taskresourcemapping ON proj_task.taskid = proj_taskresourcemapping.taskid "
                        + "WHERE projectid = ? AND proj_taskresourcemapping.resourceid = ? AND (date(proj_task.startdate)>=date(now())) "
                        + "AND (date(proj_task.startdate) <= adddate(date(now()),7)) "
                        + "ORDER BY tasklength ASC LIMIT " + limit;
                p = conn.prepareStatement(sql);
                p.setString(1, projid);
                p.setString(2, userid);
            }
            String projStr = "<a href=\"#\" onclick=\"navigate('q','%s','%s','%s','projectplanclicked');\">%s</a>";
            rsForSubQ = p.executeQuery();
            while (rsForSubQ.next()) {
                String resrs = "";
                String taskName = rsForSubQ.getString("taskname");
                int tasklength = Integer.parseInt(rsForSubQ.getObject("tasklength").toString());
                if (tasklength == 0 || tasklength == 1) {
                    newTask.append(getContentDiv("newtask"));
                    newTask.append(getContentMessage(projStr, projid, projname, taskName, ""));
                    if (tasklength == 0) {
                        resrs = "Starting today";
                    } else {
                        resrs = "Starting tomorrow";
                    }
                } else if (tasklength > 1) {
                    newTask.append(getContentDiv("newtask"));
                    newTask.append(DashboardHandler.getContentMessage(projStr, projid, projname, taskName, ""));
                    resrs = "Starting in " + rsForSubQ.getObject("tasklength").toString() + " days";
                }
                if (mode == 4) {
                    String r = getTaskResources(conn, rsForSubQ.getString("taskid"), projid, userid);
                    r = getResourceLinks(conn, r);
                    if (!StringUtil.isNullOrEmpty(r)) {
                        resrs += " [ Assigned to : " + r + " ]";
                    }
                }
                newTask.append(getContentSpan(resrs));
                taskName = "";
                tasklength = 0;
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }

    public static String getResourceLinks(Connection conn, String resids) {
        String resourceLink = "";
        try {
            JSONObject res = new JSONObject(resids);
            JSONArray jarr = res.getJSONArray("data");
            String temp = "<a href='#' onclick=\"navigate('u','%s','','%s','')\">%s</a>";
            for (int i = 0; i < jarr.length(); i++) {
                JSONObject resource = jarr.getJSONObject(i);
                PreparedStatement pstmt = conn.prepareStatement("SELECT count(userid) AS count FROM projectmembers WHERE userid=?");
                pstmt.setString(1, resource.getString("resourceid"));
                ResultSet rs = pstmt.executeQuery();
                if (rs.next() && rs.getInt("count") > 0) {
                    if (Forum.isInUse(conn, resource.getString("resourceid"))) {
                        resourceLink += String.format(temp, new Object[]{resource.getString("resourceid"), resource.getString("username"), resource.getString("resourcename")}) + ", ";
                    } else {
                        resourceLink += "";
                    }
                } else {
                    resourceLink += "<span style='color: #666666;'>" + resource.getString("resourcename") + "</span>" + ", ";
                }
            }
            resourceLink = resourceLink.substring(0, (resourceLink.length() - 2));
        } catch (SQLException ex) {
            Logger.getLogger(DashboardHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(DashboardHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException e) {
        }
        return resourceLink;
    }

    public static String getTaskResources(Connection conn, String taskid, String pid, String uid) {
        String resources = "";
        try {
            ProjectDAO pd = new ProjectDAOImpl();
            Project p = pd.getProjectById(conn, pid);
            String companyid = p.getCompanyID();
            CompanyDAO cd = new CompanyDAOImpl();
            Locale locale = cd.getCompanyLocale(conn, companyid);
            PreparedStatement pstmt = null;
            pstmt = conn.prepareStatement("SELECT concat(users.fname,' ', users.lname) AS username,resourcename,proj_resources.resourceid AS resourceid "
                    + "FROM proj_taskresourcemapping INNER JOIN proj_resources ON proj_taskresourcemapping.resourceid = proj_resources.resourceid "
                    + "LEFT JOIN users ON users.userid=proj_taskresourcemapping.resourceid "
                    + "WHERE proj_taskresourcemapping.resourceid not in "
                    + "(select userlogin.userid from userlogin inner join users on users.userid = userlogin.userid where userlogin.isactive = false AND users.companyid = ?) AND taskid = ? AND proj_resources.projid = ?;");
            pstmt.setString(1, companyid);
            pstmt.setString(2, taskid);
            pstmt.setString(3, pid);
            ResultSet rs = pstmt.executeQuery();
            JSONObject res = new JSONObject();
            while (rs.next()) {
                JSONObject temp = new JSONObject();
                if (StringUtil.equal(uid, rs.getString("resourceid"))) {
                    resources = MessageSourceProxy.getMessage("lang.me.text", null, locale);
                } else {
                    resources = rs.getString("resourcename");
                }
                temp.put("resourcename", resources);
                temp.put("username", rs.getString("username"));
                temp.put("resourceid", rs.getString("resourceid"));
                res.append("data", temp);
            }
            resources = res.toString();
        } catch (JSONException ex) {
            Logger.getLogger(DashboardHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(DashboardHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException e) {
            Logger.getLogger(DashboardHandler.class.getName()).log(Level.SEVERE, null, e);
        }
        return resources;
    }

    public static boolean isModerator(Connection conn, String uid, String pid) {
        boolean moderator = false;
        try {
            PreparedStatement pstmt = null;
            pstmt = conn.prepareStatement("SELECT status FROM projectmembers WHERE projectid = ? AND userid = ?");
            pstmt.setString(1, pid);
            pstmt.setString(2, uid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt("status") == 4) {
                moderator = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DashboardHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException e) {
            Logger.getLogger(DashboardHandler.class.getName()).log(Level.SEVERE, null, e);
        }
        return moderator;
    }

    // TODO -- Usage of this function is only for retriving project ids of a user. Must remove rest of the code for optimization.
    public static String getProjectList(Connection conn, String ulogin,
            int pagesize, int offset, String ss) throws ServiceException {
        ResultSet rs = null;
        ResultSet rs1 = null;
        PreparedStatement pstmt = null;
        PreparedStatement pstmt1 = null;
        String[] searchStrObj = new String[]{"projectname"};
        String subquery = StringUtil.getMySearchString(ss, "and", searchStrObj);
        String splitstring2 = null;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd");
        try {
            pstmt = conn.prepareStatement("SELECT COUNT(*) AS count FROM projectmembers inner join project on "
                    + " project.projectid = projectmembers.projectid "
                    + " INNER JOIN userlogin ON userlogin.userid = projectmembers.userid"
                    + " WHERE userlogin.isactive = true and userlogin.userid=? AND inuseflag = 1 AND status >=3 AND project.archived = 0 " + subquery);

            pstmt.setString(1, ulogin);
            StringUtil.insertParamSearchString(2, pstmt, ss, searchStrObj.length);
            rs = pstmt.executeQuery();
            rs.next();
            int count1 = rs.getInt("count");
            KWLJsonConverter KWL = new KWLJsonConverter();
            if (!StringUtil.isNullOrEmpty(subquery.trim())) {
                subquery = " where " + StringUtil.getMySearchString(ss, "", new String[]{"projectname"});
            }
            String query = "select t1.projectid AS id,projectname AS name,status,image AS img, description,createdon,count as members from "
                    + " (select projectid,count(projectid) as count"
                    + " from projectmembers "
                    + " where status in (3,4,5) and inuseflag = 1 group by(projectid)) as t1 "
                    + " inner join "
                    + " (select project.projectid,description,createdon, status,image, projectname "
                    + " from projectmembers inner join project on project.projectid = projectmembers.projectid "
                    + " inner join userlogin on userlogin.userid = projectmembers.userid"
                    + " where userlogin.isactive = true and userlogin.userid = ? and status >= 3 and project.archived = 0) as t2 "
                    + " on t1.projectid = t2.projectid " + subquery + " ORDER BY name LIMIT ? OFFSET ? ";
            pstmt1 = conn.prepareStatement(query);
            pstmt1.setString(1, ulogin);
            int i = StringUtil.insertParamSearchString(2, pstmt1, ss, searchStrObj.length);
            pstmt1.setInt(i++, pagesize);
            pstmt1.setInt(i++, offset);

            rs1 = pstmt1.executeQuery();
            JSONObject jobj = new JSONObject();
            splitstring2 = KWL.GetJsonForGrid(rs1);
            HealthMeterDAO objHM = new HealthMeterDAOImpl();
            if (splitstring2.compareTo("{\"data\":{}}") != 0) {
                jobj = new JSONObject(splitstring2);
                for (i = 0; i < jobj.getJSONArray("data").length(); i++) {
                    String createdOn = jobj.getJSONArray("data").getJSONObject(i).getString("createdon");
                    createdOn = Timezone.toCompanyTimezone(conn, createdOn, CompanyHandler.getCompanyByUser(conn, ulogin));
                    if (createdOn.compareTo("") != 0) {
                        java.util.Date dt = sdf.parse(createdOn);
                        createdOn = sdf.format(dt);
                        jobj.getJSONArray("data").getJSONObject(i).remove("createdon");
                        jobj.getJSONArray("data").getJSONObject(i).put("createdon", createdOn);
                    }
                    String pid = jobj.getJSONArray("data").getJSONObject(i).getString("id");
                    HealthMeter hm = objHM.getHealthMeter(conn, pid);
                    int status = hm.getStatus(objHM.getBaseLineMeter(conn, pid));
//                    CustomColumn cc = CCManager.getCustomColumn(projdb.getprojCompanyId(conn, pid));
//                    ColumnSet cs = cc.getColumnsData(conn, "Project", pid);
//                    while(cs.next()){
//                        if(cs.getObject()!=null){
//                            jobj.getJSONArray("data").getJSONObject(i).put(cs.getDataIndex(), cs.getObject());
//                        }
//                    }
                    jobj.getJSONArray("data").getJSONObject(i).put("health", status);
                }
            }
            splitstring2 = jobj.toString().substring(0, jobj.toString().length() - 1);
            splitstring2 += ",\"count\":[" + count1 + "]}";
            rs.close();
            rs1.close();
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("DashboardHandler.getProjectList", ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("DashboardHandler.getProjectList", ex);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("DashboardHandler.getProjectList", e);
        } finally {
            DbPool.closeStatement(pstmt);
            DbPool.closeStatement(pstmt1);
        }
        return " " + splitstring2;
    }

    public static String getAllProjects(Connection conn, String companyid) throws ServiceException {
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        String splitstring2 = "";
        try {
            KWLJsonConverter KWL = new KWLJsonConverter();
            String query = "select projectid, projectname, description from project where companyid = ? and archived = 0";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, companyid);
            rs = pstmt.executeQuery();
            splitstring2 = KWL.GetJsonForGrid(rs);
            rs.close();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("Forum.getProjectList", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
        return " " + splitstring2;
    }

    public static String getProjectNameList(Connection conn, String companyid) throws ServiceException {
        ResultSet rs1 = null;
        PreparedStatement pstmt1 = null;
        String splitstring2 = null;
        try {
            KWLJsonConverter KWL = new KWLJsonConverter();
            String query = "select projectname from project where companyid=? and archived=0";
            pstmt1 = conn.prepareStatement(query);
            pstmt1.setString(1, companyid);
            rs1 = pstmt1.executeQuery();
            splitstring2 = KWL.GetJsonForGrid(rs1);
            rs1.close();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("Forum.getProjectList", e);
        } finally {
            DbPool.closeStatement(pstmt1);
        }
        return " " + splitstring2;
    }

    public static String getArchivedProjList(Connection conn, String companyid,String ulogin,
            int pagesize, int offset, String ss) throws ServiceException {
        ResultSet rs = null;
        ResultSet rs1 = null;
        PreparedStatement pstmt = null;
        PreparedStatement pstmt1 = null;
        String[] searchStrObj = new String[]{"projectname"};
        String subquery = StringUtil.getMySearchString(ss, "and", searchStrObj);
        String splitstring2 = null;
        JSONObject res = new JSONObject();

        try {
            pstmt = conn.prepareStatement("SELECT COUNT(*) AS count FROM projectmembers inner join project on "
                    + " project.projectid = projectmembers.projectid WHERE userid=? AND inuseflag = 1 AND status >= 3 AND project.archived = 1 " + subquery);

            pstmt.setString(1, ulogin);
            StringUtil.insertParamSearchString(2, pstmt, ss, searchStrObj.length);
            rs = pstmt.executeQuery();
            rs.next();
            int count1 = rs.getInt("count");
            KWLJsonConverter KWL = new KWLJsonConverter();
            if (!StringUtil.isNullOrEmpty(subquery.trim())) {
                subquery = " where " + StringUtil.getMySearchString(ss, "", new String[]{"projectname"});
            }
            String query = "select t1.projectid AS id,projectname AS name,status,image AS img, description,createdon,count as members from "
                    + " (select projectid,count(projectid) as count"
                    + " from projectmembers "
                    + " where status in (3,4,5) and inuseflag = 1 group by(projectid)) as t1 "
                    + " inner join "
                    + " (select project.projectid,description,createdon, status,image, projectname "
                    + " from projectmembers inner join project on project.projectid = projectmembers.projectid "
                    + " where userid = ? and status >= 3 and project.archived = 1) as t2 "
                    + " on t1.projectid = t2.projectid " + subquery + " ORDER BY name LIMIT ? OFFSET ? ";
            pstmt1 = conn.prepareStatement(query);
            pstmt1.setString(1, ulogin);
            int i = StringUtil.insertParamSearchString(2, pstmt1, ss, searchStrObj.length);
            pstmt1.setInt(i++, pagesize);
            pstmt1.setInt(i++, offset);

            rs1 = pstmt1.executeQuery();
            while (rs1.next()) {
                String createdon = Timezone.toCompanyTimezone(conn, rs1.getObject("createdon").toString(), CompanyHandler.getCompanyByUser(conn, ulogin));
                JSONObject temp = new JSONObject();
                temp.put("id", rs1.getObject("id"));
                temp.put("name", rs1.getObject("name"));
                temp.put("status", rs1.getObject("status"));
                temp.put("img", rs1.getObject("img"));
                temp.put("description", rs1.getObject("description"));
                temp.put("createdon", createdon);
                temp.put("members", rs1.getObject("members"));
//                CustomColumn cc = CCManager.getCustomColumn(companyid);
//                ColumnSet cs = cc.getColumnsData(conn, "Project", rs1.getString("id"));
//                while (cs.next()) {
//                    if (cs.getObject() != null) {
//                        temp.put(cs.getDataIndex(), cs.getObject());
//                    }
//                }
                res.append("data", temp);
            }
//            String createdon = com.krawler.esp.handlers.Timezone.toUserTimezone(conn, temp.getString("createdon"), loginid);
            if (res.has("data")) {
                splitstring2 = res.toString();//KWL.GetJsonForGrid(rs1);
                splitstring2 = splitstring2.substring(0, splitstring2.length() - 1);
            } else {
                splitstring2 = "{\"data\" : []";
            }
            splitstring2 += ",\"count\":[" + count1 + "]}";

            rs.close();
            rs1.close();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("Forum.getProjectList", e);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("Forum.getProjectList", ex);
        } finally {
            DbPool.closeStatement(pstmt);
            DbPool.closeStatement(pstmt1);
        }
        return " " + splitstring2;
    }

    public static String getSubscriptionDetails(Connection conn, String userid) {
        String ret = "";
        try {
            JSONObject subObj = new JSONObject();
            PreparedStatement p = conn.prepareStatement("SELECT companyid FROM users WHERE userid = ?");
            p.setString(1, userid);
            ResultSet r = p.executeQuery();
            String cid = "";
            if (r.next()) {
                cid = r.getString("companyid");
            }
            p = conn.prepareStatement("SELECT moduleid, modulename, nickname FROM companymodules");
            r = p.executeQuery();
            while (r.next()) {
                JSONObject modObj = new JSONObject();
                boolean isSubscribed = isSubscribed(conn, cid, r.getString("nickname"));
                modObj.put("subscription", isSubscribed);
                if (isSubscribed) {
                    p = conn.prepareStatement(" SELECT submoduleid, submodulename, subnickname "
                            + " FROM companysubmodules WHERE parentmoduleid = ? ");
                    p.setString(1, r.getString("moduleid"));
                    ResultSet rsSub = p.executeQuery();
                    JSONObject subModObj = new JSONObject();
                    boolean isSubMod = false;
                    while (rsSub.next()) {
                        subModObj.put(rsSub.getString("subnickname"), isSubscribed(conn, cid, rsSub.getString("subnickname")));
                        isSubMod = true;
                    }

                    if (isSubMod) {
                        modObj.put("subModule", subModObj);
                    }
                }
                subObj.put(r.getString("nickname"), modObj);
            }
            ret = subObj.toString();
        } catch (ServiceException ex) {
            Logger.getLogger(DashboardHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException e) {
        } catch (SQLException e) {
        }
        return ret;
    }

    public static String getFeaturesView(Connection conn, String companyid) {
        String result = "";
        try {
            JSONObject featureObj = new JSONObject();

            PreparedStatement pstmt = conn.prepareStatement(" SELECT featureid FROM featureslistview WHERE companyid = ? ");
            pstmt.setString(1, companyid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {

                pstmt = conn.prepareStatement(" SELECT featureid, featurename, featureshortname, issubscribed FROM featureslist ");
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    if (!rs.getBoolean("issubscribed")) {
                        featureObj.put(rs.getString("featureshortname"), isFeatureView(conn, companyid, rs.getString("featureshortname")));

                    } else if (isSubscribed(conn, companyid, rs.getString("featureshortname"))) {
                        featureObj.put(rs.getString("featureshortname"), isFeatureView(conn, companyid, rs.getString("featureshortname")));

                    } else {
                        featureObj.put(rs.getString("featureshortname"), false);
                    }
                }

                JSONObject resultJSON = new JSONObject();
                resultJSON.put("featureView", featureObj);
                result = resultJSON.toString();

            } else {
                result = insertNewFeatureView(conn, companyid);
            }

        } catch (ServiceException ex) {
            Logger.getLogger(DashboardHandler.class.getName()).log(Level.SEVERE, null, ex);

        } catch (JSONException ex) {
            Logger.getLogger(DashboardHandler.class.getName()).log(Level.SEVERE, null, ex);

        } catch (SQLException ex) {
            Logger.getLogger(DashboardHandler.class.getName()).log(Level.SEVERE, null, ex);

        }
        return result;
    }

    public static String getPermissions(Connection conn, String userid)
            throws ServiceException {
        String result = "";
        ResultSet rs1 = null;
        PreparedStatement pstmt1 = null;
        try {
            if (!isDeskeraAdmin(conn, userid)) {
//                JSONObject fjson = new JSONObject();
                JSONObject opsjson = new JSONObject();
                PermissionManager pm = new PermissionManager();
                Map<String,Boolean> userPermissions = pm.getUserPermissionList(conn, userid);
                JSONObject ujson = new JSONObject(userPermissions);
                
//                pstmt1 = conn.prepareStatement("SELECT featurelist.featurename, activitieslist.activityname, POW(2,activitieslist.activityid) AS code FROM featurelist INNER JOIN activitieslist ON featurelist.featureid=activitieslist.featureid ORDER BY activitieslist.featureid,activitieslist.activityid;");
//                rs1 = pstmt1.executeQuery();
//                while (rs1.next()) {
//                    if (!fjson.has(rs1.getString("featurename"))) {
//                        fjson.put(rs1.getString("featurename"), new JSONObject().put(rs1.getString("activityname"), rs1.getString("code")));
//                    } else {
//                        fjson.getJSONObject(rs1.getString("featurename")).put(rs1.getString("activityname"), rs1.getString("code"));
//                    }
//                }
//                rs1.close();
//                pstmt1 = conn.prepareStatement("select featurelist.featurename, userpermissions.permissions from userpermissions inner join featurelist on userpermissions.featureid=featurelist.featureid where userpermissions.userid=?;");
//                pstmt1.setString(1, userid);
//                rs1 = pstmt1.executeQuery();
//                while (rs1.next()) {
//                    ujson.put(rs1.getString("featurename"), rs1.getString("permissions"));
//                }
//                pstmt1 = conn.prepareStatement("select featureaccess from company inner join companyusers where companyusers.userid = ?");
//                pstmt1.setString(1, userid);
//                rs1 = pstmt1.executeQuery();
//                while (rs1.next()) {
//                    ujson.put("Features", rs1.getString("featureaccess"));
//                }
//                opsjson.put("Perm", fjson);
                opsjson.put("UPerm", ujson);
                result = opsjson.toString();
            } else {
                JSONObject temp = new JSONObject();
                temp.put("deskeraadmin", true);
                result = temp.toString();
            }
//        } catch (SQLException e) {
//            throw ServiceException.FAILURE("DashboardHandler.getPermissions", e);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("DashboardHandler.getPermissions", e);
        } finally {
            DbPool.closeStatement(pstmt1);
        }
        return result;
    }

    public static boolean isDeskeraAdmin(Connection conn, String userid)
            throws ServiceException {
        boolean result = false;
        ResultSet rs1 = null;
        PreparedStatement pstmt1 = null;
        try {
            pstmt1 = conn.prepareStatement("SELECT count(userid) AS count FROM superuser WHERE userid = ?");
            pstmt1.setString(1, userid);
            rs1 = pstmt1.executeQuery();
            if (rs1.next() && rs1.getInt("count") > 0) {
                result = true;
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("DashboardHandler.getPermissions", e);
        } finally {
            DbPool.closeStatement(pstmt1);
        }
        return result;
    }

    public static boolean isSuperUser(Connection conn, String companyid, String userid)
            throws ServiceException {
        boolean result = false;
        ResultSet rs1 = null;
        PreparedStatement pstmt1 = null;
        try {
            pstmt1 = conn.prepareStatement("SELECT creator FROM company WHERE companyid = ?");
            pstmt1.setString(1, companyid);
            rs1 = pstmt1.executeQuery();
            if (rs1.next()) {
                result = StringUtil.equal(rs1.getString("creator"), userid);
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("DashboardHandler.getPermissions", e);
        } finally {
            DbPool.closeStatement(pstmt1);
        }
        return result;
    }

    public static String getUserEvents(Connection conn, String userId, String startVal, String endVal) throws ServiceException {
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        String result = null;
        KWLJsonConverter KWL = new KWLJsonConverter();
        try {
            String query = "select * from" + " (select calendarevents.*,'' as projId,'' as projName from "
                    + "calendarevents inner join sharecalendarmap on calendarevents.cid=sharecalendarmap.cid"
                    + " where sharecalendarmap.userid=?" + " union" + " select calendarevents.*,'' as projId"
                    + ",'' as projName from calendarevents inner join calendars on calendarevents.cid=calendars.cid"
                    + " where calendars.userid=?" + " union" + " select calendarevents.*,project.projectid as "
                    + "projId,project.projectname as projName from calendarevents inner join calendars on "
                    + "calendarevents.cid=calendars.cid" + " inner join project on project.projectid=calendars.userid"
                    + " where calendars.userid IN(select pm.projectid from projectmembers as pm where pm.userid=?)) as "
                    + "tab" + " where startts>=? and startts<=? order by startts,`timestamp`";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, userId);
            pstmt.setString(2, userId);
            pstmt.setString(3, userId);
            pstmt.setString(4, startVal);
            pstmt.setString(5, endVal);
            rs = pstmt.executeQuery();
            result = KWL.GetJsonForGrid(rs);
            return result;
        } catch (SQLException e) {
            throw ServiceException.FAILURE("DashboardHandler.getUserEvents", e);
        } finally {
//            DbPool.quietClose(conn);
            DbPool.closeStatement(pstmt);
        }
    }

    public static String getComProjRequest(Connection conn, int flag, int status, String userid) throws ServiceException {
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        String tp = null;
        KWLJsonConverter KWL = new KWLJsonConverter();
        try {
            if (flag == 0) {
                pstmt = conn.prepareStatement("select c.communityname,u.username,u.userid,c.communityid,count(c.communityid) as counter  from communitymembers "
                        + "as cm inner join userlogin as u on u.userid=cm.userid inner join community as c on c.communityid=cm.communityid where cm.status=? and "
                        + "cm.communityid in (select communitymembers.communityid from communitymembers where  communitymembers.userid=? "
                        + "and communitymembers.status in (3,4,5)) group by c.communityid");

                pstmt.setInt(1, status);
                pstmt.setString(2, userid);
            } else if (flag == 1) {
                pstmt = conn.prepareStatement("select c.projectname,u.userid,"
                        + "c.projectid,count(c.projectid) as counter  from projectmembers as cm"
                        + " inner join users as u on u.userid=cm.userid inner join project as c on "
                        + "c.projectid=cm.projectid  where cm.status=? and c.archived = false and cm.projectid in "
                        + "(select projectmembers.projectid from projectmembers where projectmembers.userid=? and projectmembers.status in (4,5)) group by c.projectid");

                pstmt.setInt(1, status);
                pstmt.setString(2, userid);
            }
            rs = pstmt.executeQuery();
            tp = KWL.GetJsonForGrid(rs).toString();
            return tp;
        } catch (SQLException e) {
            throw ServiceException.FAILURE("DashboardHandler.getComProjRequest", e);
        } finally {
//            DbPool.quietClose(conn);
            DbPool.closeStatement(pstmt);
        }
    }

    public static String getAdminPemission(Connection conn, String userid, int featureid, String activityname) throws ServiceException {
        ResultSet rs = null;

        PreparedStatement pstmt = null;
        String tp = "0";
        int a;
        int b;
        try {
            pstmt = conn.prepareStatement("select permissions from userpermissions " + "where userid = ? and featureid = ?");
            pstmt.setString(1, userid);
            pstmt.setInt(2, featureid);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                a = Integer.parseInt(rs.getString(1));
                pstmt = conn.prepareStatement("select activityid from " + "activitieslist where featureid = ? and activityname = ?");
                pstmt.setInt(1, featureid);
                pstmt.setString(2, activityname);
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    b = Integer.parseInt(rs.getString(1));
                    double d = Math.pow(featureid, b);
                    int c = ((int) d) & a;
                    if (c == ((int) d)) {
                        tp = "1";
                    }
                }
            }
            return tp;
        } catch (SQLException e) {
            throw ServiceException.FAILURE("FileHandler.getAuthor", e);
        } finally {
//            DbPool.quietClose(conn);
            DbPool.closeStatement(pstmt);
        }
    }

    public static String getCommProjInvite(Connection conn, String userid, int flag) throws ServiceException {
        ResultSet rs = null;
        String tp = null;
        PreparedStatement pstmt = null;
        KWLJsonConverter KWL = new KWLJsonConverter();
        try {
            if (flag == 0) {
                pstmt = conn.prepareStatement("select community.communityid, community.communityname from community inner join communitymembers " + "on community.communityid=communitymembers.communityid where communitymembers.userid = ? and communitymembers.status = ?");
                pstmt.setString(1, userid);
                pstmt.setInt(2, 2);

            } else if (flag == 1) {
                pstmt = conn.prepareStatement(" select project.projectid, project.projectname from project inner join projectmembers "
                        + " on project.projectid=projectmembers.projectid where projectmembers.userid = ? and projectmembers.status = ? "
                        + " and projectmembers.inuseflag=1 and project.archived = false ");
                pstmt.setString(1, userid);
                pstmt.setInt(2, 2);
            }

            rs = pstmt.executeQuery();
            tp = KWL.GetJsonForGrid(rs).toString();
            return tp;
        } catch (Exception e) {
            throw ServiceException.FAILURE("FileHandler.getAuthor", e);
        } finally {
//            DbPool.quietClose(conn);
            DbPool.closeStatement(pstmt);
        }
    }

    public static void getSectionHeader(StringBuilder sb, String headerText) {
        sb.append("<div class=\"statuspanelheader\"><span class=\"statuspanelheadertext\">");
        sb.append(headerText);
        sb.append("</span></div>");
    }

    public static StringBuilder getQuickLinks(Connection conn, String userid) {
        StringBuilder sb = new StringBuilder();
        sb.append("<li>");
        String projid = "";
        boolean bSuccess = false;
        try {
            if (!isDeskeraAdmin(conn, userid)) {
                CompanyDAO cd = new CompanyDAOImpl();
                Company c = cd.getCompanyByUser(conn, userid);
                String cid = c.getCompanyID();
                Locale locale = cd.getCompanyLocale(conn, cid);
                boolean calSubscription = isSubscribed(conn, cid, "cal");
//                JSONObject projList = new JSONObject(getProjectListMember(conn, userid, 100, 0));
//                int prc = projList.getInt("count");
//                int qlcount = 0;
//                if (prc > 0) {
//                    JSONArray projArray = projList.getJSONArray("data");
//                    StringBuilder temp = new StringBuilder();
//                    sb.append("<span id='dashproject'>Projects </span>");
//                    for (int i = 0; i < projArray.length(); i++) {
//                        boolean isQl = false;
//                        projid = projArray.getJSONObject(i).getString("id");
//                        DbResults dr = DbUtil.executeQuery(conn, "select quicklink from projectmembers where projectid = ? and userid = ? group by projectid",
//                                new Object[]{projid, userid});
//                        if (dr.next()) {
//                            isQl = dr.getBoolean("quicklink");
//                        }
//                        if (isQl) {
//                            String projname = projArray.getJSONObject(i).getString("name");
//                            if (projname.length() > 22) {
//                                projname = projname.substring(0, 18);
//                                projname = projname.concat("...");
//                            }
//                            String projMembers = projArray.getJSONObject(i).getString("members");
//                            int status = projArray.getJSONObject(i).getInt("status");
//                            temp.append(getProjectLink(conn, projname, projid, projMembers, status, calSubscription, cid));
//                            qlcount++;
//                        }
//                    }
//                    if (prc > qlcount) {
//                        sb.append("(<a href=\"#\" wtf:qtip=\"View all projects.\" onclick=\"navigate(\'mp\')\">All My Projects</a>)");
//                    }
//                    sb.append("<ul class=\"projlist\">");
//                    sb.append(temp);
//                    sb.append("</ul>");
//                    bSuccess = true;
//                }
                ProjectDAO daoObj = new ProjectDAOImpl();
                List<Project> projList = daoObj.getQuickLinks(conn, userid);
                int size = projList.size();
                int totProjcount = daoObj.getProjectCountBy(conn, "user", userid, false, "");
                StringBuilder temp = new StringBuilder();
                Map<String, Object> statusMap = new HashMap<String, Object>();
                if (size > 0) {
                    temp = new StringBuilder();
                    sb.append("<span id='dashproject'>").append(MessageSourceProxy.getMessage("pm.projects.text", null, locale)).append(" </span>");
                    DbResults rs = DbUtil.executeQuery(conn, "select projectid,status from projectmembers where userid=? and status in(3,4,5)", new Object[]{userid});
                    while (rs.next()) {
                        statusMap.put(rs.getString("projectid"), rs.getObject("status"));
                    }
                    for (int i = 0; i < size; i++) {
                        Project proj = projList.get(i);
                        String projectid = proj.getProjectID();
                        String projName = proj.getLinkString(0, 22);
                        String projMembers = String.valueOf(proj.getMemberCount());
                        int status = (Integer) statusMap.get(projectid);
                        Map<String, Object> baseValue = new HealthMeterDAOImpl().getBaseLineMeter(conn, proj);
                        String img = proj.getMeter().getImage(baseValue);
                        temp.append(getProjectLink(conn, projName, projectid, projMembers, status, calSubscription, c.getCompanyID(), img, locale));
                    }
                    if(totProjcount>size)
                        sb.append("(<a href=\"#\" wtf:qtip=\"").append(MessageSourceProxy.getMessage("pm.dashboard.widget.quicklinks.allmyprojects", null, locale)).append("\" onclick=\"navigate(\'mp\')\">").append(MessageSourceProxy.getMessage("pm.dashboard.widget.quicklinks.allmyprojects", null, locale)).append("</a>)");
                }
                sb.append("<ul class=\"projlist\">");
                sb.append(temp);
                sb.append("</ul>");
                bSuccess = true;
                //        if (bSuccess) {
                //            String adminStr = "<li class='%s'>" + "<a href=\"#\" onclick=\"loadQuickInsert('%s');\">%s</a>" + "</li>";
                //            sb.append("<li>");
                //            sb.append("Quick Insert ");
                //            sb.append("<ul>");
                //            sb.append(String.format(adminStr, new Object[]{"QITask", "1", "New task"}));
                //            sb.append(String.format(adminStr, new Object[]{"QIToDo", "2", "New to-do"}));
                //            sb.append(String.format(adminStr, new Object[]{"QIEvent", "3", "New event"}));
                //            sb.append("</ul></li>");
                //        }
                if (!bSuccess) {
                    sb.append("<a href=\"#\" onclick=\"navigate(\'mp\')\">").append(MessageSourceProxy.getMessage("pm.projects.text", null, locale)).append("</a> <img class=\"imgMid\" alt=\"\" src=\"../../images/project12.gif\"/>");
                }
                
                PermissionManager pm = new PermissionManager();
                boolean isAdmin = false;
                sb.append("</li>");
                StringBuilder sbAdmin = new StringBuilder();
                String adminStr = "<li class='%s'>" + "<a href=\"#\" onclick=\"loadAdminPage('%s');\" wtf:qtip=\"%s\">%s</a>" + "</li>";

                if (pm.isPermission(conn, userid, PermissionConstants.Feature.USER_ADMINISTRATION)) {
                    isAdmin = true;
                        sbAdmin.append(String.format(adminStr, new Object[]{"adminlinkuser", "1", 
                            MessageSourceProxy.getMessage("pm.help.useradmin", null, locale), MessageSourceProxy.getMessage("pm.dashboard.widget.quicklinks.useradministration", null, locale)}));
                }
                if (pm.isPermission(conn, userid, PermissionConstants.Feature.PROJECT_ADMINISTRATION)) {
                    isAdmin = true;
                        sbAdmin.append(String.format(adminStr, new Object[]{"adminlinkproj", "2", 
                            MessageSourceProxy.getMessage("pm.help.projectadmin", null, locale), MessageSourceProxy.getMessage("pm.dashboard.widget.quicklinks.projectadministration", null, locale)}));
                }
                if (pm.isPermission(conn, userid, PermissionConstants.Feature.COMPANY_ADMINISTRATION)) {
                    isAdmin = true;
                        sbAdmin.append(String.format(adminStr, new Object[]{"adminlinkcomp", "3", 
                            MessageSourceProxy.getMessage("pm.help.companyadmin", null, locale), MessageSourceProxy.getMessage("pm.dashboard.widget.quicklinks.companyadministration", null, locale)}));
                }

                if (isAdmin) {
                    sb.append("<li>");
                    sb.append("<span id='dashadmin'>").append(MessageSourceProxy.getMessage("pm.administration.text", null, locale)).append(" </span>");
                    sb.append("<ul>");

                    sb.append(sbAdmin);

                    sb.append("</ul></li>");
                }
                StringBuilder cr = new StringBuilder();
                String crStr = "<li class='%s'>" + "<a href=\"#\" onclick=\"%s;\" wtf:qtip=\"%s\">%s</a>" + "</li>";
                boolean isReport = false;
                if (pm.isPermission(conn, userid, PermissionConstants.Feature.CUSTOM_REPORTS,PermissionConstants.Activity.CUSTOM_REPORT_CREATE_DELETE)) {
                    isReport = true;
                    cr.append(String.format(crStr, new Object[]{"adminlinkuser", "addCustomReports()", 
                        MessageSourceProxy.getMessage("pm.Help.newcustomreports", null, locale), MessageSourceProxy.getMessage("pm.admin.project.customrep.add", null, locale)}));
                }
                List<Project> compProjList = daoObj.getAllProjectByCompany(conn, cid, "", 0, 1);
                if(size > 0 ||(compProjList.size() > 0 && pm.isPermission(conn, userid, PermissionConstants.Feature.CUSTOM_REPORTS,PermissionConstants.Activity.CUSTOM_REPORT_VIEW))){
                    isReport = true;
                    cr.append(String.format(crStr, new Object[]{"adminlinkproj", "showReportsWindow()", 
                        MessageSourceProxy.getMessage("pm.Help.opencustomreport", null, locale), MessageSourceProxy.getMessage("pm.admin.project.customrep.open", null, locale)}));
                }
                if (isReport) {
                    sb.append("<li>");
                    sb.append("<span id='dashCR'>" + MessageSourceProxy.getMessage("pm.admin.project.customreport", null, locale) + "</span>");
                    sb.append("<ul>");

                    sb.append(cr);

                    sb.append("</ul></li>");
                }
                if (pm.isPermission(conn, userid, PermissionConstants.Feature.PROJECT_ADMINISTRATION, PermissionConstants.Activity.PROJECT_MANAGE_MEMBER, PermissionConstants.SubActivity.PROJECT_MANAGE_MEMBER_ALL_PROJECTS)) {
                    sb.append("<li><a href=\"#\" onclick=\"navigate(\'team\',\'").append(projid).append("\')\" wtf:qtip=\"").append(MessageSourceProxy.getMessage("pm.help.teamproject", null, locale)).append("\">").append(MessageSourceProxy.getMessage("pm.dashboard.widget.quicklinks.teamprojectrelationship", null, locale)).append("</a> <img class=\"imgMid\" src=\"../../images/team.gif\" alt=\"\"/></li>");
                }
                sb.append("<li><a href=\"#\" onclick=\"navigate(\'mn\')\" wtf:qtip=\"").append(MessageSourceProxy.getMessage("pm.help.network", null, locale)).append("\">").append(MessageSourceProxy.getMessage("pm.dashboard.widget.quicklinks.network", null, locale)).append("</a> <img class=\"imgMid\" alt=\"\" src=\"../../images/network12.png\"/></li>");
                if (isSubscribed(conn, cid, "docs") && isFeatureView(conn, cid, "docs")) {
                    sb.append("<li class='adminlinkcomp'><a href=\"#\" onclick=\"navigate('docs');\" wtf:qtip=\"").append(MessageSourceProxy.getMessage("pm.help.mydocuments", null, locale)).append("\">").append(MessageSourceProxy.getMessage("pm.dashboard.widget.mydocuments.text", null, locale)).append("</a><img class=\"imgMid\" alt=\"\" src=\"../../images/doc.png\"/ style=\"margin-left: 2px;\"></li>");
                }
                StringBuilder append = sb.append("<li><a href=\"#\" onclick=\"navigate(\'armp\')\" wtf:qtip=\"").append(MessageSourceProxy.getMessage("pm.help.archived", null, locale)).append("\">").append(MessageSourceProxy.getMessage("pm.dashboard.widget.quicklinks.archivedprojects", null, locale)).append(" </a><img class=\"imgMid\" alt=\"\" src=\"../../images/zip.gif\"/ style=\"margin-left: 2px;\"></li>");
                if (isSuperUser(conn, cid, userid)) {
                    sb.append("<li><a href=\"#\" onclick=\"showImportLog();\" wtf:qtip=\"").append(MessageSourceProxy.getMessage("pm.help.importlog", null, locale)).append("\">").append(MessageSourceProxy.getMessage("pm.dashboard.widget.quicklinks.importlog", null, locale)).append(" </a><img class=\"imgMid\" alt=\"\" src=\"../../images/import.gif\"/ style=\"margin-left: 2px;\"></li>");
                } else if (!checkProjectsForImportLog(conn, userid, cid).isEmpty()) {
                    sb.append("<li><a href=\"#\" onclick=\"showImportLog();\" wtf:qtip=\"").append(MessageSourceProxy.getMessage("pm.help.importlog", null, locale)).append("\">").append(MessageSourceProxy.getMessage("pm.dashboard.widget.quicklinks.importlog", null, locale)).append(" </a><img class=\"imgMid\" alt=\"\" src=\"../../images/import.gif\"/ style=\"margin-left: 2px;\"></li>");
                }
            } else {
                sb.append("<li><a href='#' onclick=\"superuser(\'status\')\">List of companies</a></li>");
                sb.append("<li><a href='#' onclick=\"superuser(\'subdomain\')\">List of subdomain</a></li>");
                sb.append("<li><a href='#' onclick=\"superuser(\'subdetails\')\">Subscription details</a></li>");
                sb.append("<li><a href='#' onclick=\"superuser(\'managesub\')\">Manage subscription</a></li>");
                sb.append("<li><a href='#' onclick=\"superuser(\'managepay\')\">Manage subscription Payment</a></li>");
            }
        } catch (ServiceException sex) {
            Logger.getLogger(DashboardHandler.class.getName()).log(Level.SEVERE, null, sex);
        }
        return sb;
    }

    public static String getProjectLink(Connection conn, String projname, String projid,
            String projMembers, int status, boolean calSubscription, String companyid, String img, Locale locale) throws ServiceException {

        String ret = "";
        String projStr = "<li style='background:url(" + img + ") no-repeat scroll left bottom !important;'><a href=\"#\" onclick=\"navigate('q','%s','%s','%s','%s');\">%s</a> <span wtf:qtitle=\""+ MessageSourceProxy.getMessage("pm.help.memcountqtitle", null, locale) +"\""
                + " wtf:qtip=\""+ MessageSourceProxy.getMessage("pm.help.memcountqtip", null, locale) +"\">(%s)</span>  ";
        ArrayList projObj = new ArrayList();
        projObj.add(projid);
        projObj.add(projid);
        projObj.add(com.krawler.svnwebclient.util.UrlUtil.encode(projname));
        projObj.add("home");
        projObj.add(projname);
        projObj.add(projMembers);

        if (status >= 3) {
            if (isSubscribed(conn, companyid, "proj") && isFeatureView(conn, companyid, "proj")) {
                projStr += "<a href=\"#\" onclick=\"navigate('q','%s','%s','%s','%s');\">%s</a>  ";

                projObj.add(projid);
                projObj.add(projid);
                projObj.add(com.krawler.svnwebclient.util.UrlUtil.encode(projname));
                projObj.add("projectplanclicked");
                projObj.add("<img src='../../images/Notes12.gif' class='imgMid' wtf:qtitle=\""+ MessageSourceProxy.getMessage("pm.common.projectplan", null, locale) +"\" wtf:qtip=\""+ MessageSourceProxy.getMessage("pm.help.planqtip", null, locale) +"\">");
            }
            if (isFeatureView(conn, companyid, "disc")) {
                projStr += "<a href=\"#\" onclick=\"navigate('q','%s','%s','%s','%s');\">%s</a>  ";

                projObj.add(projid);
                projObj.add(projid);
                projObj.add(com.krawler.svnwebclient.util.UrlUtil.encode(projname));
                projObj.add("discussionclicked");
                projObj.add("<img src='../../images/discussion12.gif' class='imgMid' wtf:qtitle=\""+ MessageSourceProxy.getMessage("pm.module.discussion", null, locale) +"\" wtf:qtip=\""+ MessageSourceProxy.getMessage("pm.help.dicussionqtip", null, locale) +"\">");
            }
            if (isFeatureView(conn, companyid, "todo")) {
                projStr += "<a href=\"#\" onclick=\"navigate('q','%s','%s','%s','%s');\">%s</a>  ";

                projObj.add(projid);
                projObj.add(projid);
                projObj.add(com.krawler.svnwebclient.util.UrlUtil.encode(projname));
                projObj.add("todoclicked");
                projObj.add("<img src='../../images/to-do-list12.gif' class='imgMid' wtf:qtitle=\""+ MessageSourceProxy.getMessage("pm.module.todo", null, locale) +"\" wtf:qtip=\""+ MessageSourceProxy.getMessage("pm.help.todo", null, locale) +"\">");
            }
            if (isSubscribed(conn, companyid, "cal") && isFeatureView(conn, companyid, "cal")) {
                projStr += "<a href=\"#\" onclick=\"navigate('q','%s','%s','%s','%s');\">%s</a>  ";

                projObj.add(projid);
                projObj.add(projid);
                projObj.add(com.krawler.svnwebclient.util.UrlUtil.encode(projname));
                projObj.add("teamcalclicked");
                projObj.add("<img src='../../images/team-calender12.gif' class='imgMid' wtf:qtitle=\""+ MessageSourceProxy.getMessage("pm.module.teamcalendar", null, locale) +"\" wtf:qtip=\""+ MessageSourceProxy.getMessage("pm.help.calendar", null, locale) +"\">");
            }
            if (isSubscribed(conn, companyid, "docs") && isFeatureView(conn, companyid, "docs")) {
                projStr += "<a href=\"#\" onclick=\"navigate('q','%s','%s','%s','%s');\">%s</a>  ";

                projObj.add(projid);
                projObj.add(projid);
                projObj.add(com.krawler.svnwebclient.util.UrlUtil.encode(projname));
                projObj.add("projnewdocclicked");
                projObj.add("<img src='../../images/document16.gif' class='imgMid' wtf:qtitle=\""+ MessageSourceProxy.getMessage("pm.module.documents", null, locale) +"\" wtf:qtip=\""+ MessageSourceProxy.getMessage("pm.help.documents", null, locale) +"\">");
            }
            if (status == 4) {
                projStr += "<a href=\"#\" onclick=\"navigate('q','%s','%s','%s','%s');\">%s</a>  ";

                projObj.add(projid);
                projObj.add(projid);
                projObj.add(com.krawler.svnwebclient.util.UrlUtil.encode(projname));
                projObj.add("adminpageclicked");
                projObj.add("<img src='../../images/administration12.gif' class='imgMid' wtf:qtitle=\""+ MessageSourceProxy.getMessage("pm.module.projectsettings", null, locale) +"\" wtf:qtip=\""+ MessageSourceProxy.getMessage("pm.help.projectsettings", null, locale) +"\">");
            }

        } else {
            projStr = "<li>" + "<a href=\"#\" onclick=\"navigate('q','%s','%s','%s');\">%s</a> <span wtf:qtitle=\""+ MessageSourceProxy.getMessage("pm.help.memcountqtitle", null, locale) +"\""
                    + " wtf:qtip=\""+ MessageSourceProxy.getMessage("pm.help.memcountqtip", null, locale) +"\">(%s)</span>  </li>";
            projObj.add(projid);
            projObj.add(projid);
            projObj.add(com.krawler.svnwebclient.util.UrlUtil.encode(projname));
            projObj.add(projMembers);
        }
        ret = String.format(projStr, projObj.toArray());
        return ret;
    }

    public static boolean changeSubscription(Connection conn, String companyid, int moduleId,
            int subscriptionNum, int subModSubscriptionNum) throws ServiceException {

        boolean f = false;
        try {
            PreparedStatement p = conn.prepareStatement(" SELECT subscriptionid, submodulesubscriptionid FROM companymodulesubscription WHERE companyid = ? AND moduleid = ? ");
            p.setString(1, companyid);
            p.setInt(2, moduleId);
            ResultSet r = p.executeQuery();
            if (r.next()) {
                p = conn.prepareStatement(" UPDATE companymodulesubscription SET subscriptionid = ?, submodulesubscriptionid = ? WHERE companyid = ? AND moduleid = ? ");
                p.setInt(1, subscriptionNum);
                p.setInt(2, subModSubscriptionNum);
                p.setString(3, companyid);
                p.setInt(4, moduleId);
                int i = p.executeUpdate();
                if (i > 0) {
                    f = true;
                }

            } else {
                p = conn.prepareStatement(" INSERT INTO companymodulesubscription (moduleid, subscriptionid, submodulesubscriptionid, companyid) VALUES (?, ?, ?, ?) ");
                p.setInt(1, moduleId);
                p.setInt(2, subscriptionNum);
                p.setInt(3, subModSubscriptionNum);
                p.setString(4, companyid);
                int i = p.executeUpdate();
                if (i > 0) {
                    f = true;
                }
            }

            p = conn.prepareStatement(" SELECT status, nickname, requestuserid, submoduleid,"
                    + " companymodules.moduleid, companymodules.modulename, "
                    + " company.emailid, company.subdomain, company.companyname, "
                    + " users.emailid as requestemailid, users.fname "
                    + " FROM newsubscriptionrequest "
                    + " INNER JOIN companymodules ON newsubscriptionrequest.moduleid = companymodules.moduleid "
                    + " INNER JOIN company ON newsubscriptionrequest.companyid = company.companyid "
                    + " INNER JOIN users ON requestuserid = users.userid "
                    + " WHERE newsubscriptionrequest.companyid = ? AND companymodules.moduleid = ? ");
            p.setString(1, companyid);
            p.setInt(2, moduleId);
            r = p.executeQuery();
            if (r.next()) {
                int stat = r.getInt("status");
                String requestEmailid = r.getString("requestemailid");
                String shortName = r.getString("nickname");
                String companyName = r.getString("companyname");
                String subdomain = r.getString("subdomain");
                String moduleName = r.getString("modulename");
                String emailidCreator = r.getString("emailid");
                int subModId = r.getInt("submoduleid");

                boolean sub = isSubscribed(conn, companyid, shortName);

                if ((stat == 0 && !sub) || (stat == 1 && sub) || subModId > 0) {
                    p = conn.prepareStatement("DELETE FROM newsubscriptionrequest WHERE companyid = ? AND moduleid = ?");
                    p.setString(1, companyid);
                    p.setInt(2, r.getInt("moduleid"));
                    p.executeUpdate();

                    String status = "subscription";

                    if (stat == 0) {
                        status = "unsubscription";
                    }

                    int featureid = 0;
                    int featureUpdate = 0;
                    int featureVal = 0;

                    if (subModId == 0) {
                        boolean isFeature = isFeatureView(conn, companyid, shortName);

                        if ((!isFeature && stat != 0) || (isFeature && stat == 0)) {
                            p = conn.prepareStatement(" SELECT featureid FROM featureslist WHERE featureshortname = ? ");
                            p.setString(1, shortName);
                            r = p.executeQuery();

                            if (r.next()) {
                                featureid = r.getInt("featureid");

                                if (featureid > 0) {
                                    featureVal += Math.pow(2, featureid);

                                    p = conn.prepareStatement(" SELECT featureid FROM featureslistview WHERE companyid = ? ");
                                    p.setString(1, companyid);
                                    r = p.executeQuery();

                                    if (r.next()) {
                                        featureid = r.getInt("featureid");

                                        if (!isFeature) {
                                            featureUpdate = featureid + featureVal;

                                        } else {
                                            featureUpdate = featureid - featureVal;
                                        }

                                        p = conn.prepareStatement(" UPDATE featureslistview SET featureid = ? WHERE companyid = ? ");
                                        p.setInt(1, featureUpdate);
                                        p.setString(2, companyid);
                                        p.executeUpdate();
                                    }
                                }
                            }
                        }
                    }

                    if (subModId > 0) {
                        p = conn.prepareStatement(" SELECT submodulename FROM companysubmodules WHERE submoduleid = ? ");
                        p.setInt(1, subModId);
                        r = p.executeQuery();

                        if (r.next()) {
                            String subModName = r.getString("submodulename");
                            moduleName = subModName + " from " + moduleName + " module";
                        }
                    }

                    String infoHTML = " <HTML><HEAD><TITLE>" + status + " request approved</TITLE></HEAD> "
                            + " <style type='text/css'>a:link, a:visited, a:active {color: #03C;} "
                            + " body {font-family: Arial, Helvetica, sans-serif;color: #000;font-size: 13px;}</style> "
                            + " <BODY> "
                            + " <div><p>Hi,</p>"
                            + companyName + "(" + subdomain + ")"
                            + " request for " + status + " of " + moduleName + " has been approved."
                            + " <br/><br/><p>See you back on Project Management!</p><p> - PM Team</p> "
                            + " </div> </BODY></HTML> ";

                    String infoString = "Hi,\n\n"
                            + companyName + "(" + subdomain + ") request for "
                            + status + " of " + moduleName + " has been approved."
                            + " \n\n See you back on Project Management! \n\n - PM Team ";

                    String pmsg = infoString;
                    String htmlmsg = infoHTML;

                    SendMailHandler.postMail(new String[]{requestEmailid}, status + " request approved", htmlmsg, pmsg, KWLErrorMsgs.adminEmailId);
                    SendMailHandler.postMail(new String[]{emailidCreator}, status + " request approved", htmlmsg, pmsg, KWLErrorMsgs.adminEmailId);
                }
            }

        } catch (MessagingException ex) {
            Logger.getLogger(DashboardHandler.class.getName()).log(Level.SEVERE, null, ex);

        } catch (ConfigurationException ex) {
            Logger.getLogger(DashboardHandler.class.getName()).log(Level.SEVERE, null, ex);

        } catch (SQLException e) {
            f = false;
            throw ServiceException.FAILURE(e.getMessage(), e);
        } catch (ServiceException e) {
            f = false;
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return f;
    }

    public static boolean isSubscribed(Connection conn, String companyid, String subscription) {
        boolean isSubscribed = false;
        try {
            boolean isMod = true;       // Is subscription for module or submodule

            PreparedStatement p = conn.prepareStatement(" SELECT moduleid FROM companymodules "
                    + " WHERE nickname = ? ");
            p.setString(1, subscription);
            ResultSet r = p.executeQuery();
            int modid = 0;
            int submodid = 0;
            if (r.next()) {
                modid = r.getInt("moduleid");
                isMod = true;

            } else {
                p = conn.prepareStatement(" SELECT parentmoduleid, submoduleid "
                        + " FROM companysubmodules WHERE subnickname = ? ");
                p.setString(1, subscription);
                r = p.executeQuery();
                modid = 0;
                submodid = 0;
                if (r.next()) {
                    modid = r.getInt("parentmoduleid");
                    submodid = r.getInt("submoduleid");
                    isMod = false;
                }
            }
            if (modid != 0) {
                p = conn.prepareStatement(" SELECT subscriptionid, submodulesubscriptionid FROM companymodulesubscription "
                        + " WHERE companyid = ? AND moduleid = ? ");
                p.setString(1, companyid);
                p.setInt(2, modid);
                r = p.executeQuery();
                int subValue = 0;
                int subModValue = 0;
                if (r.next()) {
                    if (isMod) {
                        subValue = r.getInt("subscriptionid");

                    } else {
                        subValue = r.getInt("subscriptionid");
                        subModValue = r.getInt("submodulesubscriptionid");
                    }
                }

                if (subValue > 0) {
                    isSubscribed = true;
                }
                if (!isMod && isSubscribed) {
                    submodid = (int) Math.pow(2, submodid);
                    if ((subModValue & submodid) == submodid) {
                        isSubscribed = true;

                    } else {
                        isSubscribed = false;
                    }

                } else if (!isMod && !isSubscribed) {
                    isSubscribed = false;
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(DashboardHandler.class.getName()).log(Level.SEVERE, null, ex);

        } catch (ServiceException e) {
            Logger.getLogger(DashboardHandler.class.getName()).log(Level.SEVERE, null, e);
        }
        return isSubscribed;
    }

    public static String insertNewFeatureView(Connection conn, String companyid) throws ServiceException {
        String result = "";
        try {
            int featureNum = 0;
            JSONObject featureObj = new JSONObject();

            PreparedStatement pstmt = conn.prepareStatement(" SELECT featureid, featurename, featureshortname, issubscribed FROM featureslist ");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                if (!rs.getBoolean("issubscribed")) {
                    featureNum += (int) Math.pow(2, rs.getInt("featureid"));
                    featureObj.put(rs.getString("featureshortname"), true);

                } else if (isSubscribed(conn, companyid, rs.getString("featureshortname"))) {
                    featureNum += (int) Math.pow(2, rs.getInt("featureid"));
                    featureObj.put(rs.getString("featureshortname"), true);

                } else {
                    featureObj.put(rs.getString("featureshortname"), false);
                }
            }

            pstmt = conn.prepareStatement(" INSERT INTO featureslistview (featureid, companyid) VALUES (?, ?) ");

            pstmt.setInt(1, featureNum);
            pstmt.setString(2, companyid);
            int i = pstmt.executeUpdate();
            if (i > 0) {
                JSONObject resultJSON = new JSONObject();
                resultJSON.put("featureView", featureObj);
                result = resultJSON.toString();

                conn.commit();
            }

        } catch (JSONException ex) {
            Logger.getLogger(DashboardHandler.class.getName()).log(Level.SEVERE, null, ex);

        } catch (SQLException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);

        } catch (ServiceException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return result;
    }

    public static boolean isFeatureView(Connection conn, String companyid, String featureShortName) {
        boolean result = false;
        int featureValue = 0;
        try {
            PreparedStatement p = conn.prepareStatement(" SELECT featureid FROM featureslist WHERE featureshortname = ?");
            p.setString(1, featureShortName);
            ResultSet r = p.executeQuery();
            int featureid = 0;
            if (r.next()) {
                featureid = r.getInt("featureid");
            }

            if (featureid > 0) {
                PreparedStatement pstmt = conn.prepareStatement(" SELECT featureid FROM featureslistview WHERE companyid = ? ");
                pstmt.setString(1, companyid);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    featureValue = rs.getInt("featureid");
                }
                featureid = (int) Math.pow(2, featureid);
                if ((featureValue & featureid) == featureid) {
                    result = true;
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(DashboardHandler.class.getName()).log(Level.SEVERE, null, ex);

        } catch (ServiceException e) {
        }
        return result;
    }

    public static String getProgressRequest(Connection conn, String userid, Locale locale) throws ServiceException {
        PreparedStatement pstmt = null;
        StringBuilder sb = new StringBuilder();
        try {
            ResultSet rs = null;
            ResultSet rs1 = null;
            String query = "select * from task_progressrequest where requestto = '" + userid + "'";
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String reqBy = rs.getString("requestby");
                String username = AuthHandler.getAuthor(conn, reqBy);
                String taskname = "";
                String projid = "";
                query = "select taskname, projectid from proj_task where taskid = '" + rs.getString("taskid") + "'";
                pstmt = conn.prepareStatement(query);
                rs1 = pstmt.executeQuery();
                if (rs1.next()) {
                    taskname = rs1.getString("taskname");
                    projid = rs1.getString("projectid");
                }
                String projname = projdb.getProjectName(conn, projid);
                String getlink = MessageSourceProxy.getMessage(updateProgressReqFormat, new Object[]{username, taskname, projid, projid, projname, projname}, locale);
                sb.append(getlink);
                sb.append("\n");
            }
            rs.close();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("projdb.setProgressRequest", e);
        } finally {
            DbPool.closeStatement(pstmt);
            return sb.toString();
        }
    }

    public static JSONArray getNewRequests(Connection conn, String userid) {
        StringBuilder sb = new StringBuilder();
        boolean hasValue = false;
        StringBuilder sb1 = new StringBuilder();
        JSONArray jA = new JSONArray();
        JSONObject j = new JSONObject();
        Locale locale = Locale.ENGLISH;
        try {
            CompanyDAO cd = new CompanyDAOImpl();
            Company c = cd.getCompanyByUser(conn, userid);
            locale = cd.getCompanyLocale(conn, c.getCompanyID());
            String reqid = ProfileHandler.getUserRequest(conn, userid);
            String projReaquest = DashboardHandler.getComProjRequest(conn, 1, 1, userid);
            JSONObject jobj2 = new JSONObject(projReaquest);

            if (!"{}".equals(jobj2.getString("data"))) {
                for (int i = 0; i < jobj2.getJSONArray("data").length(); i++) {
                    hasValue = true;
                    j = new JSONObject();
                    String projId = jobj2.getJSONArray("data").getJSONObject(i).getString("projectid");
                    String projName = jobj2.getJSONArray("data").getJSONObject(i).getString("projectname");
                    int numReqs = jobj2.getJSONArray("data").getJSONObject(i).getInt("counter");
                    String reqStr = MessageSourceProxy.getMessage(DashboardHandler.projReqFormat,
                            new Object[]{projId, projId, com.krawler.svnwebclient.util.UrlUtil.encode(projName), projName, numReqs,
                                ((numReqs > 1) ? "s" : "")}, locale);
                    sb1.append("<div class=\"requestsLI\"><div style=\"margin-left:20px;\">");
                    sb1.append(reqStr);
                    sb1.append("</div></div>");
                    j.put("update", sb1.toString());
                    jA.put(j);
                }
            }

            String projectRequest = DashboardHandler.getCommProjInvite(conn, userid, 1);
            JSONObject jobj3 = new JSONObject(projectRequest);
            if (!"{}".equals(jobj3.getString("data"))) {
                for (int i = 0; i < jobj3.getJSONArray("data").length(); i++) {
                    hasValue = true;
                    sb1.delete(0, sb1.length());
                    j = new JSONObject();
                    String projId = jobj3.getJSONArray("data").getJSONObject(i).getString("projectid");
                    String projName = jobj3.getJSONArray("data").getJSONObject(i).getString("projectname");
                    String reqStr = MessageSourceProxy.getMessage(DashboardHandler.inviteProjReqFormat,
                            new Object[]{projId, projId, com.krawler.svnwebclient.util.UrlUtil.encode(projName), projName}, locale);
                    sb1.append("<div class=\"requestsLI\"><div style=\"margin-left:20px;\">");
                    sb1.append(reqStr);
                    sb1.append("<img src=\"../../images/cancel16.png\" onClick=\"handleAcceptReject('")
                            .append(projId).append("','").append(projName).append("','reject','project');\" class=\"acceptreject\"" + " wtf:qtitle=\"")
                            .append(MessageSourceProxy.getMessage("pm.help.projreqrejqtitle", null, locale))
                            .append("\" wtf:qtip=\"").append(MessageSourceProxy.getMessage("pm.help.projreqrejqtip", null, locale))
                            .append("\">" + "<img src=\"../../images/check16.png\" onClick=\"handleAcceptReject('")
                            .append(projId).append("','").append(projName)
                            .append("','accept', 'project');\" class=\"acceptreject\" wtf:qtitle=\"")
                            .append(MessageSourceProxy.getMessage("pm.help.projreqaccqtitle", null, locale)).append("\" wtf:qtip=\"")
                            .append(MessageSourceProxy.getMessage("pm.help.projreqaccqtip", null, locale)).append("\"></div></div>");
                    j.put("update", sb1.toString());
                    jA.put(j);
                }
            }

            if (reqid.compareTo("{data:{}}") != 0) {
                JSONObject jobj = new JSONObject(reqid);
                String[] uids = new String[jobj.getJSONArray("data").length()];
                for (int i = 0; i < jobj.getJSONArray("data").length(); i++) {
                    uids[i] = jobj.getJSONArray("data").getJSONObject(i).getString("userid1");
                }
                String tp = FileHandler.getRequestAuthor(conn, uids);
                JSONObject jobj1 = new JSONObject(tp);
                if (!"{}".equals(jobj1.getString("data"))) {
                    for (int i = 0; i < jobj1.getJSONArray("data").length(); i++) {
                        hasValue = true;
                        sb1.delete(0, sb1.length());
                        j = new JSONObject();
                        String userId = jobj1.getJSONArray("data").getJSONObject(i).getString("userid");
                        String userName = jobj1.getJSONArray("data").getJSONObject(i).getString("username");
                        String userFullname = (jobj1.getJSONArray("data").getJSONObject(i).getString("fname")
                                + " " + jobj1.getJSONArray("data").getJSONObject(i).getString("lname")).trim();
                        String reqStr = MessageSourceProxy.getMessage(DashboardHandler.userReqFormat,
                                new Object[]{userId, userId, userFullname, userFullname}, locale);
                        sb1.append("<div class=\"requestsLI\"><div style=\"margin-left:20px;\">");
                        sb1.append(reqStr);
                        sb1.append("<img src=\"../../images/cancel16.png\" onClick=\"handleAcceptReject('").append(userId).append("','")
                                .append(userFullname).append("','reject','user');\" class=\"acceptreject\" wtf:qtitle=\"")
                                .append(MessageSourceProxy.getMessage("pm.help.conreqrejqtitle", null, locale)).append("\" wtf:qtip=\"")
                                .append(MessageSourceProxy.getMessage("pm.help.conreqrejqtip", null, locale))
                                .append("\"/>" + "<img src=\"../../images/check16.png\" onClick=\"handleAcceptReject('")
                                .append(userId).append("','").append(userFullname).append("','accept','user');\" class=\"acceptreject\" wtf:qtitle=\"")
                                .append(MessageSourceProxy.getMessage("pm.help.conreqaccqtitle", null, locale))
                                .append("\" wtf:qtip=\"").append(MessageSourceProxy.getMessage("pm.help.conreqaccqtip", null, locale)).append("\"/></div></div>");
                        j.put("update", sb1.toString());
                        jA.put(j);
                    }
                }
            }
            String req = "";
            req = DashboardHandler.getProgressRequest(conn, userid, locale);
            if (req.contains("\n")) {
                String reqtext[] = req.split("\n");
                for (int i = 0; i < reqtext.length; i++) {
                    hasValue = true;
                    sb1.delete(0, sb1.length());
                    j = new JSONObject();
                    sb1.append("<div class=\"requestsLI\"><div style=\"margin-left:20px;width: 85%;\">");
                    sb1.append(reqtext[i]);
                    sb1.append("</div></div>");
                    j.put("update", sb1.toString());
                    jA.put(j);
                }
            }
        } catch (JSONException jex) {
            hasValue = false;
        } catch (ServiceException e) {
            hasValue = false;
        }
        if (!hasValue) {
            try {
                sb.append(MessageSourceProxy.getMessage("pm.dashboard.widget.request.emptytext", null, locale));
                j.put("update", sb.toString());
                jA.put(j);
            } catch (JSONException ex) {
                jA = new JSONArray();
            }
        }
        return jA;

    }

    public static StringBuilder getSubscriptionRequests(Connection conn, String userid) {
        StringBuilder ret = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdfDisplayDate = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm aaa");

        try {
            String projStr = "<a href=\"#\" onclick=\"superuser('managesub','%s');\">%s</a>";

            PreparedStatement psSub = conn.prepareStatement(" SELECT newsubscriptionrequest.companyid AS cid, "
                    + " newsubscriptionrequest.status, newsubscriptionrequest.moduleid, "
                    + " newsubscriptionrequest.submoduleid, newsubscriptionrequest.requesttime, "
                    + " company.companyid, company.companyname, "
                    + " companymodules.modulename "
                    + " FROM newsubscriptionrequest INNER JOIN company "
                    + " ON newsubscriptionrequest.companyid = company.companyid "
                    + " INNER JOIN companymodules ON newsubscriptionrequest.moduleid = companymodules.moduleid ORDER BY companyname, requesttime");
            ResultSet rsSub = psSub.executeQuery();

            while (rsSub.next()) {
                String stat = "SUBSCRIPTION";
                String typeStr = "subscribe";
                if (rsSub.getInt("status") == 0) {
                    stat = "UNSUBSCRIPTION";
                    typeStr = "unsubscribe";
                }

                String info = "<div class=\"statuspanelcontentitemouter\"><div class=\"statuspanelcontentiteminner\">"
                        + "<div class=\"statusitemimg " + typeStr + " pwnd" + "\">&nbsp;</div>"
                        + "<div class=\"statusitemcontent\">";

                info += String.format(projStr, new Object[]{rsSub.getString("cid"), rsSub.getString("companyname")});

//                String requestTime = Timezone.toUserTimezone(conn, rsSub.getString("requesttime"), userid);
                String requestTime = rsSub.getString("requesttime");
                Calendar onCal = Calendar.getInstance();

                onCal.setTime(sdf.parse(requestTime));

                String onString = sdfDisplayDate.format(onCal.getTime());

                String requestModule = "<b>" + rsSub.getString("modulename") + "</b>";
                int subModId = rsSub.getInt("submoduleid");

                if (subModId != 0) {
                    PreparedStatement psModSub = conn.prepareStatement(" SELECT submodulename FROM companysubmodules WHERE submoduleid = ? ");
                    psModSub.setInt(1, subModId);
                    ResultSet rsModSub = psModSub.executeQuery();
                    if (rsModSub.next()) {
                        requestModule = "<b>" + rsModSub.getString("submodulename") + "</b> from <b>" + requestModule + "</b> module ";
                    }
                }

                info += " has requested for " + stat + " of " + requestModule
                        + " on " + onString + "</div></div></div><div class=\"statusclr\"></div>";

                ret.append(info);
            }

        } catch (ParseException ex) {
            Logger.getLogger(DashboardHandler.class.getName()).log(Level.SEVERE, null, ex);

        } catch (ServiceException e) {
        } catch (SQLException e) {
        }
        return ret;
    }

    public static String getHelpContent(Connection conn, String module) throws ServiceException, JSONException {
        ResultSet rs = null;
        String tp = null;
        PreparedStatement pstmt = null;
        KWLJsonConverter KWL = new KWLJsonConverter();
        try {
            pstmt = conn.prepareStatement("SELECT * FROM helpcontent WHERE activetab = ? ORDER BY helpindex");
            pstmt.setString(1, module);
            rs = pstmt.executeQuery();
            tp = KWL.GetJsonForGrid(rs);
            return tp;
        } catch (Exception e) {
            throw ServiceException.FAILURE("DashboardHandler.getHelpContent", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

    public static String dontShowHelpMsg(Connection conn, String userid) throws ServiceException {
        String tp = "";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("update widgetmanagement set helpflag = 1 where userid = ?");
            pstmt.setString(1, userid);
            int cnt = pstmt.executeUpdate();
            if (cnt == 1) {
                tp = KWLErrorMsgs.rsSuccessTrue;
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("DashboardHandler.dontShowHelpMsg:" + e.getMessage(), e);
        } finally {
            DbPool.closeStatement(pstmt);
            return tp;
        }
    }

    public static JSONArray getOverDueTasksForWidget(Connection conn, String projid, int mode, String userid, String projname,
            int limit, int offset, JSONArray jA, Locale locale)
            throws ServiceException {
        JSONObject j = null;
        StringBuilder overdue = new StringBuilder();
        try {
            ResultSet rsForSubQ = null;
            String projStr = "<div class='taskStatusWidget'><a href=\"#\" onclick=\"navigate('q','%s','%s','%s','projectplanclicked');\">%s</a>";
            String sql = "SELECT datediff(date(now()),date(proj_task.enddate)) AS tasklength,"
                    + "taskname, percentcomplete AS complete,taskid FROM proj_task "
                    + "WHERE projectid = ? AND (date(proj_task.enddate) < date(now()) AND percentcomplete < 100) "
                    + "ORDER BY tasklength ASC LIMIT ? OFFSET ?";
            PreparedStatement p = conn.prepareStatement(sql);
            p.setString(1, projid);
            p.setInt(2, limit);
            p.setInt(3, offset);
            if (mode == 6) {
                sql = "SELECT datediff(date(now()),date(proj_task.enddate)) AS tasklength,"
                        + "taskname, percentcomplete AS complete,proj_task.taskid AS taskid "
                        + "FROM proj_task INNER JOIN proj_taskresourcemapping ON proj_task.taskid = proj_taskresourcemapping.taskid "
                        + "WHERE projectid = ? AND proj_taskresourcemapping.resourceid = ? AND (date(proj_task.enddate) < date(now()) "
                        + "AND percentcomplete < 100) ORDER BY tasklength ASC LIMIT ? OFFSET ?";
                p = conn.prepareStatement(sql);
                p.setString(1, projid);
                p.setString(2, userid);
                p.setInt(3, limit);
                p.setInt(4, offset);
            }
            rsForSubQ = p.executeQuery();
            while (rsForSubQ.next()) {
                overdue.delete(0, overdue.length());
                String resrs = "";
                String taskName = rsForSubQ.getString("taskname");
                int tasklength = Integer.parseInt(rsForSubQ.getObject("tasklength").toString());
                overdue.append(getContentDivForWidget("overduetask"));
                overdue.append(getContentMessage(projStr, projid, projname, taskName,
                        " - (" + rsForSubQ.getObject("complete").toString() + "% " + MessageSourceProxy.getMessage("pm.dashboard.widget.project.complete", null, locale) + ")"));
                if (tasklength == 1) {
                    resrs = "<span class = 'redtext'>"+ MessageSourceProxy.getMessage("pm.dashboard.widget.project.overdueby", null, locale) + " 1 " + MessageSourceProxy.getMessage("lang.smallday.text", null, locale) + "</span>";
                } else {
                    resrs = "<span class = 'redtext'>"+ MessageSourceProxy.getMessage("pm.dashboard.widget.project.overdueby", null, locale) + " " + rsForSubQ.getObject("tasklength").toString() + " " + MessageSourceProxy.getMessage("lang.smalldays.text", null, locale) + "</span>";
                }
                if (mode == 4) {
                    String r = getTaskResources(conn, rsForSubQ.getString("taskid"), projid, userid);
                    r = getResourceLinks(conn, r);
                    if (!StringUtil.isNullOrEmpty(r)) {
                        resrs += "<br>[" + MessageSourceProxy.getMessage("pm.dashboard.widget.project.assignedto", null, locale) + " : " + r + "]";
                    }
                }
                taskName = "";
                tasklength = 0;
                overdue.append(getContentSpan(resrs));
                String tempString = overdue.toString();
                j = new JSONObject();
                j.put("update", tempString);
                jA.put(j);
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } finally {
            return jA;
        }
    }

    public static JSONArray getDueTasksForWidget(Connection conn, String projid, int mode, String userid, 
            String projname, int limit, int offset, Locale locale)
            throws ServiceException {
        JSONObject j = null;
        JSONArray jA = new JSONArray();
        StringBuilder dueTask = new StringBuilder();
        try {
            ResultSet rsForSubQ = null;
            String projStr = "<div class='taskStatusWidget'><a href=\"#\" onclick=\"navigate('q','%s','%s','%s','projectplanclicked');\">%s</a>";
            Object param = new Object[]{projid};
            String sql = "SELECT datediff(date(proj_task.enddate),date(now())) AS tasklength, "
                    + "taskname, percentcomplete AS complete,taskid FROM proj_task "
                    + "WHERE projectid = ? AND (date(proj_task.enddate)>=date(now())) AND (date(proj_task.enddate) <= adddate(date(now()),5)) "
                    + "ORDER BY tasklength ASC LIMIT ? offset ?";
            PreparedStatement p = conn.prepareStatement(sql);
            p.setString(1, projid);
            p.setInt(2, limit);
            p.setInt(3, offset);
            if (mode == 6) {
                sql = "SELECT datediff(date(proj_task.enddate),date(now())) AS tasklength, "
                        + "taskname, percentcomplete AS complete,proj_task.taskid AS taskid "
                        + "FROM proj_task INNER JOIN proj_taskresourcemapping ON proj_task.taskid = proj_taskresourcemapping.taskid "
                        + "WHERE projectid = ? AND proj_taskresourcemapping.resourceid = ? AND (date(proj_task.enddate)>=date(now())) "
                        + "AND (date(proj_task.enddate) <= adddate(date(now()),5)) "
                        + "ORDER BY tasklength ASC LIMIT ? OFFSET ?";
                p = conn.prepareStatement(sql);
                p.setString(1, projid);
                p.setString(2, userid);
                p.setInt(3, limit);
                p.setInt(4, offset);
            }
            rsForSubQ = p.executeQuery();
            while (rsForSubQ.next()) {
                dueTask.delete(0, dueTask.length());
                String resrs = "";
                String taskName = rsForSubQ.getString("taskname");
                int tasklength = Integer.parseInt(rsForSubQ.getObject("tasklength").toString());
                if (tasklength == 0 || tasklength == 1) {
                    dueTask.append(getContentDivForWidget("duetask"));
                    dueTask.append(getContentMessage(projStr, projid, projname, taskName,
                            " - (" + rsForSubQ.getObject("complete").toString() + "% " + MessageSourceProxy.getMessage("pm.dashboard.widget.project.complete", null, locale) + ")"));
                    if (tasklength == 0) {
                        resrs = MessageSourceProxy.getMessage("pm.dashboard.widget.project.duetoday", null, locale);
                    } else {
                        resrs = MessageSourceProxy.getMessage("pm.dashboard.widget.project.duetomorrow", null, locale);
                    }
                } else if (tasklength > 1) {
                    dueTask.append(getContentDivForWidget("duetask"));
                    dueTask.append(getContentMessage(projStr, projid, projname, taskName,
                            " - (" + rsForSubQ.getObject("complete").toString() + "% " + MessageSourceProxy.getMessage("pm.dashboard.widget.project.complete", null, locale) + ")"));
                    resrs = MessageSourceProxy.getMessage("pm.dashboard.widget.project.duein", null, locale) +" "+ rsForSubQ.getObject("tasklength").toString() + " " + MessageSourceProxy.getMessage("lang.smalldays.text", null, locale);
                }
                if (mode == 4) {
                    String r = getTaskResources(conn, rsForSubQ.getString("taskid"), projid, userid);
                    r = getResourceLinks(conn, r);
                    if (!StringUtil.isNullOrEmpty(r)) {
                        resrs += "<br>[" + MessageSourceProxy.getMessage("pm.dashboard.widget.project.assignedto", null, locale) + " : " + r + "]";
                    }
                }
                taskName = "";
                tasklength = 0;
                dueTask.append(getContentSpan(resrs));
                String tempString = dueTask.toString();
                j = new JSONObject();
                j.put("update", tempString);
                jA.put(j);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (SQLException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } finally {
            return jA;
        }
    }

    public static JSONArray getNewTasksForWidget(Connection conn, String projid, int mode, String userid, String projname,
            int limit, int offset, JSONArray jA, Locale locale)
            throws ServiceException {
        JSONObject j = null;
        StringBuilder newTask = new StringBuilder();
        try {
            ResultSet rsForSubQ = null;
            Object param = new Object[]{projid};
            String sql = "SELECT datediff(date(proj_task.startdate),date(now())) AS tasklength, "
                    + "taskname, percentcomplete AS complete,taskid FROM proj_task "
                    + "WHERE projectid = ? AND (date(proj_task.startdate)>=date(now())) AND (date(proj_task.startdate) <= adddate(date(now()),5)) "
                    + "ORDER BY tasklength ASC LIMIT ? OFFSET ?";
            PreparedStatement p = conn.prepareStatement(sql);
            p.setString(1, projid);
            p.setInt(2, limit);
            p.setInt(3, offset);
            if (mode == 6) {
                sql = "SELECT datediff(date(proj_task.startdate),date(now())) AS tasklength, "
                        + "taskname, percentcomplete AS complete,proj_task.taskid AS taskid "
                        + "FROM proj_task INNER JOIN proj_taskresourcemapping ON proj_task.taskid = proj_taskresourcemapping.taskid "
                        + "WHERE projectid = ? AND proj_taskresourcemapping.resourceid = ? AND (date(proj_task.startdate)>=date(now())) "
                        + "AND (date(proj_task.startdate) <= adddate(date(now()),5)) "
                        + "ORDER BY tasklength ASC LIMIT ? OFFSET ?";
                p = conn.prepareStatement(sql);
                p.setString(1, projid);
                p.setString(2, userid);
                p.setInt(3, limit);
                p.setInt(4, offset);
            }
            String projStr = "<div class='taskStatusWidget'><a href=\"#\" onclick=\"navigate('q','%s','%s','%s','projectplanclicked');\">%s</a>";
            rsForSubQ = p.executeQuery();
            while (rsForSubQ.next()) {
                newTask.delete(0, newTask.length());
                String resrs = "";
                String taskName = rsForSubQ.getString("taskname");
                int tasklength = Integer.parseInt(rsForSubQ.getObject("tasklength").toString());
                if (tasklength == 0 || tasklength == 1) {
                    newTask.append(getContentDivForWidget("newtask"));
                    newTask.append(getContentMessage(projStr, projid, projname, taskName, ""));
                    if (tasklength == 0) {
                        resrs = MessageSourceProxy.getMessage("pm.dashboard.widget.project.duetoday", null, locale);
                    } else {
                        resrs = MessageSourceProxy.getMessage("pm.dashboard.widget.project.duetoday", null, locale);
                    }
                } else if (tasklength > 1) {
                    newTask.append(getContentDivForWidget("newtask"));
                    newTask.append(DashboardHandler.getContentMessage(projStr, projid, projname, taskName, ""));
                    resrs = MessageSourceProxy.getMessage("pm.dashboard.widget.project.startingin", null, locale) + " " + rsForSubQ.getObject("tasklength").toString() + " " + MessageSourceProxy.getMessage("lang.smalldays.text", null, locale);
                }
                if (mode == 4) {
                    String r = getTaskResources(conn, rsForSubQ.getString("taskid"), projid, userid);
                    r = getResourceLinks(conn, r);
                    if (!StringUtil.isNullOrEmpty(r)) {
                        resrs += "<br>[" + MessageSourceProxy.getMessage("pm.dashboard.widget.project.assignedto", null, locale) + " : " + r + "]";
                    }
                }
                taskName = "";
                tasklength = 0;
                newTask.append(getContentSpan(resrs));
                String tempString = newTask.toString();
                j = new JSONObject();
                j.put("update", tempString);
                jA.put(j);
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } finally {
            return jA;
        }
    }

    public static String getTasksListForWidget(Connection conn, String userid, String companyid, String projectid, String Limit, String Offset) throws ServiceException {
        JSONArray jA = new JSONArray();
        JSONObject j = new JSONObject();
        try {
            CompanyDAO cd = new CompanyDAOImpl();
            Locale locale = cd.getCompanyLocale(conn, companyid);
            boolean sub = isSubscribed(conn, companyid, "usrtk");
            int count = 0, duecount = 0, newcount = 0, overduecount = 0;
            int limitfornew = 0, limitforoverdue = 0, offsetfornew = 0, offsetforoverdue = 0;
            int offset = Integer.parseInt(Offset);
            int limit = Integer.parseInt(Limit);
            JSONObject membership = new JSONObject(Forum.getProjectMembership(conn, userid, projectid));
            JSONObject userProjData = membership.getJSONArray("data").getJSONObject(0);
            int pp = userProjData.getInt("planpermission");
            boolean uT = (userProjData.getInt("connstatus") != 4 && (pp == 8 || pp == 16) && sub);
            String projName = projdb.getProjectName(conn, projectid);
            int mod = 3;
            if (userProjData.getInt("connstatus") == 4) {
                mod = 4;
            } else if (uT) {
                mod = 6;
            }
            if (isFeatureView(conn, companyid, "proj")) {
                if (mod == 6) {
                    duecount = getCount(conn, projectid, userid, 1);
                    newcount = getCount(conn, projectid, userid, 2);
                    overduecount = getCount(conn, projectid, userid, 3);
                } else {
                    duecount = getCount(conn, projectid, "", 1);
                    newcount = getCount(conn, projectid, "", 2);
                    overduecount = getCount(conn, projectid, "", 3);
                }
                count = duecount + newcount + overduecount;
                if (duecount > offset) {
                    if (duecount > offset + limit) {
                        jA = getDueTasksForWidget(conn, projectid, mod, userid, projName, limit, offset, locale);
                    } else {
                        jA = getDueTasksForWidget(conn, projectid, mod, userid, projName, limit, offset, locale);
                        limitfornew = offset + limit - duecount;
                        offsetfornew = 0;//offset + duecount;
                        if (duecount > offsetfornew) {
                            if (newcount > offsetfornew + limitfornew) {
                                jA = getNewTasksForWidget(conn, projectid, mod, userid, projName, limitfornew, offsetfornew, jA, locale);
                            } else {
                                jA = getNewTasksForWidget(conn, projectid, mod, userid, projName, limitfornew, offsetfornew, jA, locale);
                                limitforoverdue = offset + limit - newcount - duecount;
                                offsetforoverdue = 0;
                                jA = getOverDueTasksForWidget(conn, projectid, mod, userid, projName, limitforoverdue, offsetforoverdue, jA, locale);
                            }
                        } else {
                            jA = getOverDueTasksForWidget(conn, projectid, mod, userid, projName, limitforoverdue, offsetforoverdue, jA, locale);
                        }
                    }
                } else {
                    limitfornew = limit;
                    offsetfornew = offset - duecount;
                    if (newcount > offsetfornew) {
                        if (newcount > offset + limit) {
                            jA = getNewTasksForWidget(conn, projectid, mod, userid, projName, limitfornew, offsetfornew, jA, locale);
                        } else {
                            jA = getNewTasksForWidget(conn, projectid, mod, userid, projName, limitfornew, offsetfornew, jA, locale);
                            limitforoverdue = offset + limit - newcount - duecount;
                            offsetforoverdue = 0;
                            jA = getOverDueTasksForWidget(conn, projectid, mod, userid, projName, limitforoverdue, offsetforoverdue, jA, locale);
                        }
                    } else {
                        limitforoverdue = limitfornew;
                        offsetforoverdue = offsetfornew - newcount;
                        jA = getOverDueTasksForWidget(conn, projectid, mod, userid, projName, limitforoverdue, offsetforoverdue, jA, locale);
                    }
                }
            }
            j.put("data", jA);
            j.put("count", count);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return j.toString();
    }

    public static String getContentDivForWidget(String typeStr) {
        String div = "<div class=\"statusitemimg " + typeStr + " pwnd\">&nbsp;</div>";
        return div;
    }

    public static int getCount(Connection conn, String projectid, String userid, int type)
            throws ServiceException {
        int count = 0;
        try {
            ResultSet rsForSubQ = null;
            String sql1 = "";
            PreparedStatement p1 = null;
            switch (type) {
                case 1:
                    if (userid.equals("")) {
                        sql1 = "SELECT COUNT(*) as totalcount FROM proj_task "
                                + "WHERE projectid = ? AND (date(proj_task.enddate)>=date(now())) AND (date(proj_task.enddate) <= adddate(date(now()),5))";
                        p1 = conn.prepareStatement(sql1);
                        p1.setString(1, projectid);
                    } else {
                        sql1 = "SELECT COUNT(*) as totalcount FROM proj_task INNER JOIN proj_taskresourcemapping ON proj_task.taskid = proj_taskresourcemapping.taskid "
                                + "WHERE projectid = ? AND proj_taskresourcemapping.resourceid = ? AND (date(proj_task.enddate)>=date(now())) "
                                + "AND (date(proj_task.enddate) <= adddate(date(now()),5))";
                        p1 = conn.prepareStatement(sql1);
                        p1.setString(1, projectid);
                        p1.setString(2, userid);
                    }
                    break;
                case 2:
                    if (userid.equals("")) {
                        sql1 = "SELECT COUNT(*) as totalcount FROM proj_task "
                                + "WHERE projectid = ? AND (date(proj_task.startdate)>=date(now())) AND (date(proj_task.startdate) <= adddate(date(now()),5)) ";
                        p1 = conn.prepareStatement(sql1);
                        p1.setString(1, projectid);
                    } else {
                        sql1 = "SELECT COUNT(*) AS totalcount FROM proj_task INNER JOIN proj_taskresourcemapping ON proj_task.taskid = proj_taskresourcemapping.taskid "
                                + "WHERE projectid = ? AND proj_taskresourcemapping.resourceid = ? AND (date(proj_task.startdate)>=date(now())) "
                                + "AND (date(proj_task.startdate) <= adddate(date(now()),5))";
                        p1 = conn.prepareStatement(sql1);
                        p1.setString(1, projectid);
                        p1.setString(2, userid);
                    }
                    break;
                case 3:
                    if (userid.equals("")) {
                        sql1 = "SELECT COUNT(*) as totalcount FROM proj_task "
                                + "WHERE projectid = ? AND (date(proj_task.enddate) < date(now()) AND percentcomplete < 100) ";
                        p1 = conn.prepareStatement(sql1);
                        p1.setString(1, projectid);
                    } else {
                        sql1 = "SELECT COUNT(*) AS totalcount FROM proj_task INNER JOIN proj_taskresourcemapping ON proj_task.taskid = proj_taskresourcemapping.taskid "
                                + "WHERE projectid = ? AND proj_taskresourcemapping.resourceid = ? AND (date(proj_task.enddate) < date(now()) "
                                + "AND percentcomplete < 100)";
                        p1 = conn.prepareStatement(sql1);
                        p1.setString(1, projectid);
                        p1.setString(2, userid);
                    }
                    break;
            }
            rsForSubQ = p1.executeQuery();
            if (rsForSubQ.next()) {
                count = rsForSubQ.getInt("totalcount");
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return count;
    }

    public static String[] getFormattedOp(Connection conn, String[] params, int mode, String projId, String projName, String userId, String docId) {
        String projStr = "<a href=\"#\" onclick=\"navigate('q','%s','%s','%s','%s');\">%s</a>";//id, id, encode projname, whichFor, name to show
        String usrStr = "<a href='#' onclick=\"navigate('u','%s','','%s','')\">%s</a>";//id, name, name to show
        String projHomeStr = "<a href=\"#\" onclick=\"navigate('q','%s','%s','%s','home');\">%s</a>";// id, id, encode projname, name to show
        String whichMode = "";
        String usr = "";
        String proj = "";
        String projHome = "";
        String uN = "";
        if (mode == 1) {
            whichMode = "todoclicked";
        } else if (mode == 2) {
            whichMode = "teamcalclicked";
        } else if (mode == 3) {
            whichMode = "projnewdocclicked";
        } else if (mode == 4) {
            whichMode = "adminpageclicked";
        } else if (mode == 6) {
            whichMode = "discussionclicked";
        }
        uN = params[0].substring(0, params[0].indexOf("("));
        usr = String.format(usrStr, new Object[]{userId, uN, params[0]});
        params[0] = usr;
        switch (params.length) {
            case 2:
                proj = String.format(projStr, new Object[]{projId, projId, projName, whichMode, params[1]});
                params[1] = proj;
                break;
            case 4:
            case 3:
                if (mode != 4 && mode != 6) {
                    proj = String.format(projStr, new Object[]{projId, projId, projName, whichMode, params[1]});
                    if (params.length == 4 && mode != 2) {
                        projHome = String.format(projHomeStr, new Object[]{projId, projId, projName, params[2]});
                        params[2] = projHome;
                    } else if (params.length == 4 && mode == 2) {
                        projHome = String.format(projHomeStr, new Object[]{projId, projId, projName, params[3]});
                        params[3] = projHome;
                    } else {
                        projHome = String.format(projHomeStr, new Object[]{projId, projId, projName, params[2]});
                        params[2] = projHome;
                    }
                    params[1] = proj;
                } else if (mode == 6) {
                    proj = String.format(projStr, new Object[]{projId, projId, projName, whichMode, params[1]});
                    params[1] = proj;
                    if (params.length == 4) {
                        proj = String.format(projStr, new Object[]{projId, projId, projName, whichMode, params[2]});
                        params[2] = proj;
                        projHome = String.format(projHomeStr, new Object[]{projId, projId, projName, params[3]});
                        params[3] = projHome;
                    } else {
                        projHome = String.format(projHomeStr, new Object[]{projId, projId, projName, params[2]});
                        params[2] = projHome;
                    }
                } else {
                    proj = String.format(projStr, new Object[]{projId, projId, projName, projName, params[2]});
                    params[2] = proj;
                }
                break;
        }
        return params;
    }

    public static String getWidgetUpdates(Connection conn, String userid, String companyid, String projectid, int mode, String limit, String offset) throws ServiceException {
        ResultSet rs = null;
        ResultSet rs2 = null;
        PreparedStatement pstmt = null;
        String projStr = "";
        JSONObject j = null;
        JSONObject jo = new JSONObject();
        JSONArray jA = new JSONArray();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Date d = new java.util.Date();
        String likeClause = "";
        try {
            CompanyDAO cd = new CompanyDAOImpl();
            Locale locale = cd.getCompanyLocale(conn, companyid);
            int count1 = 0;
            String projname = projdb.getProjectName(conn, projectid);
            if (mode == 1) {
                projStr = "<a href=\"#\" onclick=\"navigate('q','%s','%s','%s','todoclicked');\">%s</a>";
                likeClause = "'13%'";
            } else if (mode == 2) {
                projStr = "<a href=\"#\" onclick=\"navigate('q','%s','%s','%s','teamcalclicked');\">%s</a>";
                likeClause = "'12%'";
            } else if (mode == 3) {
                projStr = "<a href=\"#\" onclick=\"navigate('q','%s','%s','%s','projnewdocclicked');\">%s</a>";
                likeClause = "'34%'";
            } else if (mode == 4) {
                projStr = "<a href=\"#\" onclick=\"navigate('q','%s','%s','%s','adminpageclicked');\">%s</a>";
                likeClause = "'15%'";
            } else if (mode == 6) {
                projStr = "<a href=\"#\" onclick=\"navigate('q','%s','%s','%s','discussionclicked');\">%s</a>";
                likeClause = "'14%'";
            }
            pstmt = conn.prepareStatement("select COUNT(*) AS count from actionlog where projectid = ? and actionid like " + likeClause);
            pstmt.setString(1, projectid);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                count1 = rs.getInt("count");
            }
            String query = "select logid, actionid, actionon, actionby, params, timestamp from actionlog where projectid = ? and actionid like " + likeClause + " ORDER BY timestamp DESC limit ? offset ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, projectid);
            pstmt.setInt(2, Integer.parseInt(limit));
            pstmt.setInt(3, Integer.parseInt(offset));
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String params = rs.getString("params");
                String userparam[] = params.split(",");
                userparam = getFormattedOp(conn, userparam, mode, projectid, projname, rs.getString("actionby"), rs.getString("actionon"));
                String query2 = "select textkey from actions where actionid = ?";
                pstmt = conn.prepareStatement(query2);
                pstmt.setString(1, rs.getString("actionid"));
                rs2 = pstmt.executeQuery();
                Object[] strParam = new Object[userparam.length];
                System.arraycopy(userparam, 0, strParam, 0, userparam.length);
                rs2.next();
//                String action = rs2.getString("actiontext");
                String action = rs2.getString("textkey");
                String useraction = MessageSourceProxy.getMessage(action, strParam,locale);
                String postTime = Timezone.toCompanyTimezone(conn, rs.getString("timestamp"), companyid);
                postTime = Timezone.dateTimeRenderer(conn, Timezone.toCompanyTimezone(conn, sdf.format(d), companyid), postTime, companyid);
                postTime = Timezone.convertToUserPref(conn, postTime, userid);
                String temp = "<img src=\"../../images/bullet2.gif\" style=\"float:left;margin-right:2px;\">";
                useraction = temp + "<div style=\"width:90%; float:left;\">" + DashboardHandler.getContentMessage(projStr, projectid, projname, useraction, "");
                useraction += "  <br><div style='color:#A1A1A1;font-size:11px;'>" + postTime + "</div></div>";
                j = new JSONObject();
                j.put("update", useraction);
                jA.put(j);
            }
            jo.put("data", jA);
            jo.put("count", count1);
        } catch (ServiceException ex) {
            Logger.getLogger(AuditTrail.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("DashboardHandler.getWidgetpdates", ex);
        } catch (SQLException e) {
            throw ServiceException.FAILURE("DashboardHandler.getWidgetpdates", e);
        }
        return jo.toString();
    }

    public static String getPMUpdates(Connection conn, String userid, String projectid, String limit, String offset) throws ServiceException {
        JSONObject jo = new JSONObject();
        JSONArray jA = new JSONArray();
        JSONObject j = new JSONObject();
        try {
            String companyid = CompanyHandler.getCompanyByUser(conn, userid);
            CompanyDAO cd = new CompanyDAOImpl();
            Locale locale = cd.getCompanyLocale(conn, companyid);
            String msg = Mail.fetchMail(conn, userid, "0", Integer.parseInt(offset), Integer.parseInt(limit));
            JSONObject jM = new JSONObject(msg);
            int count = jM.getInt("totalCount");
            if (jM.getString("data").compareTo("") != 0) {
                for (int i = 0; i < jM.getJSONArray("data").length(); i++) {
                    String finalString = "";
                    JSONObject temp = jM.getJSONArray("data").getJSONObject(i);
                    String senderid = temp.getString("senderid");
                    String uN = AuthHandler.getUserName(conn, senderid);
                    String uFN = AuthHandler.getAuthor(conn, senderid);
                    String r = "";
                    String img = "";
                    if (temp.getString("readflag").compareTo("false") == 0) {
                        //r = "[ Unread ]";
                        img = "<img class='pmImage' wtf:qtip = '"+MessageSourceProxy.getMessage("pm.help.unreadmsg", null, locale)+"' src='../../images/unread.gif'></span><div style='float:left; margin-left:10px; width:80%;'>";
                    } else {
                        img = "<img class='pmImage' src='../../images/read.gif'></span><div style='float:left; margin-left:10px; width:80%;'>";
                    }
                    finalString = "<div style='font-weight:bold; margin-left:20px;'>" + r + "</div>";
                    finalString += "<div class='pmUpdateDiv'>" + img + MessageSourceProxy.getMessage("pm.personalmessages.from.text", null, locale) + " - ";
                    String[] str = {uFN + " (" + uN + ")"};
                    str = getFormattedOp(conn, str, 0, projectid, "", senderid, "");
                    finalString += str[0];
                    str[0] = "<a href=\"#\" onclick=\"gotoPM('" + temp.getString("post_id") + "');\">" + com.krawler.svnwebclient.util.UrlUtil.decode(temp.getString("post_subject")) + "</a>";
                    finalString += "<br clear='all'>"+MessageSourceProxy.getMessage("lang.subject.text", null, locale)+" - " + str[0] + "<br clear='all'>";
                    String imgSrc = "../../images/FlagGrey.gif";
                    String qt = MessageSourceProxy.getMessage("pm.help.flagit", null, locale);
                    if (temp.getBoolean("flag") == true) {
                        qt = MessageSourceProxy.getMessage("pm.help.unflagit", null, locale);
                        imgSrc = "../../images/FlagRed.gif";
                    }
                    String theTime = temp.getString("post_time");
                    theTime = Timezone.dateTimeRenderer(conn, Timezone.toCompanyTimezone(conn, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), companyid), theTime, companyid);
                    theTime = Timezone.convertToUserPref(conn, theTime, userid);
                    finalString += "<span style='color:#A1A1A1;font-size:11px;'>"+MessageSourceProxy.getMessage("pm.personalmessages.receivedon", null, locale)+" - " + theTime + "</span></div><img src='" + imgSrc + "' class='pmFlagGray' id='pmFlag" + i + "' onclick=\"handleFlagChange('" + temp.getString("post_id") + "','" + i + "','" + imgSrc + "')\" wtf:qtip='" + qt + "'></img><br clear='all'></div>";
                    j = new JSONObject();
                    j.put("update", finalString);
                    jA.put(j);
                }
            }
            jo.put("count", count);
            jo.put("data", jA);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("DashboardHandler.getPMUpdates", ex);
        } finally {
            return jo.toString();
        }
    }

    public static String getMyDocsWidget(Connection conn, String userid, String companyid, String limit1, String offset) throws ServiceException {
        String gridString = null;
        JSONObject j = null;
        JSONObject jo = new JSONObject();
        JSONArray jA = new JSONArray();
        int start = Integer.parseInt(offset);
        int limit = Integer.parseInt(limit1);
        int count1 = 0;
        try {
            CompanyDAO cd = new CompanyDAOImpl();
            Locale locale = cd.getCompanyLocale(conn, companyid);
            String down = MessageSourceProxy.getMessage("pm.help.downloaddoc", null, locale);
            String projStr = "<a href=\"#\" onclick=\"myDocClicked('%s');\">%s</a>";
            gridString = FileHandler.fillGrid(conn, userid, "*flag*", "1", "1", companyid, new JSONObject().toString());
            com.krawler.utils.json.base.JSONObject jobj = new com.krawler.utils.json.base.JSONObject(gridString);
            if (jobj.getJSONArray("data").length() > 0) {
                count1 = jobj.getJSONArray("data").length();
            }
            if (jobj.getJSONArray("data").length() > 0) {
                int i = start;
                for (int cnt = 0; cnt < limit; cnt++) {
                    if (jobj.getJSONArray("data").length() > i) {
                        String userdocument = "";
                        String docname = jobj.getJSONArray("data").getJSONObject(i).getString("Name");
                        String author = jobj.getJSONArray("data").getJSONObject(i).getString("Author");
                        String datemod = jobj.getJSONArray("data").getJSONObject(i).getString("DateModified");
                        String docid = jobj.getJSONArray("data").getJSONObject(i).getString("Id");
                        String temp = "<img src=\"../../images/doc.png\" style=\"float:left;margin-top:3px;\">";
                        docname = String.format(projStr, docid, docname);
                        userdocument = docname;
                        datemod = Timezone.dateTimeRenderer(conn, Timezone.toCompanyTimezone(conn, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), companyid), datemod, companyid);
                        String postTime = Timezone.convertToUserPref(conn, datemod, userid);
                        userdocument += "  <br><div style='color:#A1A1A1;font-size:11px;'>"+MessageSourceProxy.getMessage("pm.dashboard.widget.mydocuments.added", null, locale)+" : " + postTime + "</div>";
                        userdocument += "  <div style='color:#A1A1A1;font-size:11px;'>"+MessageSourceProxy.getMessage("lang.author.text", null, locale)+" : " + author + "</div>";
                        userdocument = temp + "<div style=\"float:left;margin-left:10px;width:85%;\">" + userdocument + "</div><a href = '#'><img class='widgetdoc' src=\"../../images/download.png\" id='mydocwidget" + i + "' onclick=\"downloadDocWidget('" + docid + "')\" wtf:qtip='" + down + "'></img></a>";
                        j = new JSONObject();
                        j.put("update", userdocument);
                        jA.put(j);
                        i++;
                    } else {
                        break;
                    }
                }
            }
            jo.put("data", jA);
            jo.put("count", count1);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("DashboardHandler.getMyDocsWidget:" + ex.getMessage(), ex);
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("DashboardHandler.getMyDocsWidget:" + ex.getMessage(), ex);
        } finally {
            return jo.toString();
        }
    }

    public static String getReportWidgetLinks(Connection conn, String userid, String companyid, String projectid, String limit1, String offset) throws ServiceException {
        String jdata = "";
        JSONObject j = null;
        JSONObject jo = new JSONObject();
        JSONArray jA = new JSONArray();
        try {
            int start = Integer.parseInt(offset);
            int limit = Integer.parseInt(limit1);
            conn = DbPool.getConnection();
            String projectName = projdb.getProjectName(conn, projectid);
            CompanyDAO cd = new CompanyDAOImpl();
            Locale locale = cd.getCompanyLocale(conn, companyid);
            String[] reportArr = {
                MessageSourceProxy.getMessage("pm.project.plan.reports.overview.cashflow", null, locale),
                MessageSourceProxy.getMessage("pm.project.plan.reports.overview.topleveltasks", null, locale),
                MessageSourceProxy.getMessage("pm.project.plan.reports.overview.milestones", null, locale),
                MessageSourceProxy.getMessage("pm.project.plan.reports.overview.overdue", null, locale),
                MessageSourceProxy.getMessage("pm.project.plan.reports.activities.completed", null, locale),
                MessageSourceProxy.getMessage("pm.project.plan.reports.activities.inprogress", null, locale),
                MessageSourceProxy.getMessage("pm.project.plan.reports.activities.starting", null, locale),
                MessageSourceProxy.getMessage("pm.project.plan.reports.activities.ending", null, locale),
                MessageSourceProxy.getMessage("pm.project.plan.reports.activities.unstarted", null, locale),
                MessageSourceProxy.getMessage("pm.project.plan.reports.workload.resourceusage", null, locale),
                MessageSourceProxy.getMessage("pm.project.plan.reports.workload.taskusage", null, locale),
                MessageSourceProxy.getMessage("pm.project.plan.reports.workload.resourceanalysis", null, locale),
                MessageSourceProxy.getMessage("pm.project.plan.reports.baseline.projectsummary", null, locale),
                MessageSourceProxy.getMessage("pm.project.plan.reports.baseline.duration", null, locale),
                MessageSourceProxy.getMessage("pm.project.plan.reports.baseline.date", null, locale),
                MessageSourceProxy.getMessage("pm.project.plan.reports.baseline.cost", null, locale),
                MessageSourceProxy.getMessage("pm.project.plan.reports.baseline.resource", null, locale),
                MessageSourceProxy.getMessage("pm.project.plan.reports.baseline.resourcewise", null, locale)
            };
            String projStr = "<a href=\"#\" onclick=\"getReportName('%s','%s','%s');\">%s</a>";
            int i = start;
            int Arrlength = reportArr.length;
            if (!DashboardHandler.isSubscribed(conn, companyid, "bsln")) {
                Arrlength -= 6;
            }
            for (int k = 0; k < limit; k++) {
                if (Arrlength > i) {
                    String temp = "<img src=\"../../images/bullet2.gif\" style=\"float:left;margin-right:10px;\">";
                    String report = temp + getContentMessageForReport(projStr, projectName, projectid, reportArr[i]);
                    j = new JSONObject();
                    j.put("update", report);
                    jA.put(j);
                    i++;
                } else {
                    break;
                }
            }
            jo.put("data", jA);
            jo.put("count", Arrlength);
            jdata = jo.toString();
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("DashboardHandler.getReportWidgetLinks:" + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("DashboardHandler.getReportWidgetLinks:" + ex.getMessage(), ex);
        } finally {
            DbPool.quietClose(conn);
            return jdata;
        }
    }

    public static String getAllWidgetsData(Connection conn, String responseText, String userid, String companyid, String projCount) {
        String ret = "";
        String limit = "5";
        String offset = "0";
        try {
            JSONObject r = new JSONObject();
            JSONObject jobj = new JSONObject(responseText);
            for (int i = 0; i < jobj.getJSONArray("col1").length(); i++) {
                String id = jobj.getJSONArray("col1").getJSONObject(i).getString("id");
                String data = getWidgetData(conn, id, userid, companyid, limit, offset, projCount);
                jobj.append(id, data);
            }
            for (int i = 0; i < jobj.getJSONArray("col2").length(); i++) {
                String id = jobj.getJSONArray("col2").getJSONObject(i).getString("id");
                String data = getWidgetData(conn, id, userid, companyid, limit, offset, projCount);
                jobj.append(id, data);
            }
            if (jobj.has("col3")) {
                for (int i = 0; i < jobj.getJSONArray("col3").length(); i++) {
                    String id = jobj.getJSONArray("col3").getJSONObject(i).getString("id");
                    String data = getWidgetData(conn, id, userid, companyid, limit, offset, projCount);
                    jobj.append(id, data);
                }
            }
            ret = jobj.toString();
        } finally {
            return ret;
        }
    }

    private static String getWidgetData(Connection conn, String id, String userid, String companyid, String limit, String offset, String projCount)
            throws ServiceException {
        String ret = "";
        JSONArray jA = new JSONArray();
        JSONObject jo = new JSONObject();
        if (id.compareTo("announcements_drag") == 0) {
            ret = dbcon.getannouncement(userid, companyid);
        }  else if(id.compareTo("taskwiseprojecthealth_drag") == 0){
            ret = getProjectsByTimeProgress(conn, userid, companyid, "10000", "0");
        } else if (id.compareTo("requests_drag") == 0) {
            ret = dbcon.getrequest(userid, companyid);
        } else if (id.compareTo("chart_drag") == 0) {
            ret = dbcon.getChartForWidget(userid, companyid, projCount, limit, offset);
        } else if (id.compareTo("pm_drag") == 0) {
            ret = getPMUpdates(conn, userid, ret, limit, offset);
        } else if (id.compareTo("mydocs_drag") == 0) {
            ret = getMyDocsWidget(conn, userid, companyid, limit, offset);
        } else if (id.compareTo("quicklinks_drag") == 0) {
            ret = dbcon.getQuickLinks(userid, companyid);
        } else if (id.compareTo("welcome_drag") == 0) {
            ret = dbcon.getWelcomeText(userid, companyid);
        } else {
            String projid = id.split("_")[0];
            ret = dbcon.gettaskupdates(userid, companyid, projid, limit, offset);
        }
        return ret;
    }

    public static String getProjectsByTimeProgress(Connection conn, String userID, String companyID, String limit, String offset) {
        String data = KWLErrorMsgs.rsSuccessFalse;
        JSONObject jo = new JSONObject();
        JSONArray jArr =  new JSONArray();
        ProjectDAO projdao = new ProjectDAOImpl();
        HealthMeterDAO hmdao = new HealthMeterDAOImpl();
        try{
            int newOffset = Integer.parseInt(offset);
            int newLimit = Integer.parseInt(limit);
            String[] cmpHolidays = SchedulingUtilities.getCompHolidays(conn, companyID);
            List<Project> projects = projdao.getAllProjectByUser(conn, userID, newOffset, newLimit);
            for (int i = 0; i < projects.size(); i++) {
                Project project = projects.get(i);
                if (!project.isArchieved()) {
                    String projectID = project.getProjectID();
                    String projectName = project.getProjectName();
                    
                    Map baseValues = hmdao.getBaseLineMeter(conn, project);
                    HealthMeter hm = hmdao.getHealthMeter(conn, projectID,cmpHolidays);

                    JSONObject jObj = new JSONObject();
                    jObj.put("projectid", projectID);
                    jObj.put("projectname", projectName);
                    jObj.put("health", hm.getStatus(baseValues));
                    jObj.put("completed", hm.getCompleted());
                    jObj.put("inprogress", hm.getOntime());
                    jObj.put("needattention", hm.getNeedAttentaion());
                    jObj.put("overdue", hm.getOverdue());
                    jObj.put("future", hm.getFuture());
                    jArr.put(jObj);
                }
            }
            jo.put("data", jArr);
            jo.put("count", jArr.length());
            data = jo.toString();
        } catch (JSONException ex) {
            Logger.getLogger(DashboardHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(DashboardHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(DashboardHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            return data;
        }
        
    }

    public static String updateQuickLinksStates(Connection conn, String userid, String state) throws ServiceException {
        String data = KWLErrorMsgs.rsSuccessFalse;
        try {
            JSONObject jobj = new JSONObject(state);
            Iterator i = jobj.keys();
            while (i.hasNext()) {
                String projectid = (String) i.next();
                boolean val = jobj.getBoolean(projectid);
                DbUtil.executeUpdate(conn, "UPDATE projectmembers SET quicklink = ? WHERE projectid = ? AND userid = ?",
                        new Object[]{val, projectid, userid});
            }
            data = KWLErrorMsgs.rsSuccessTrue;
        } catch (JSONException ex) {
            Logger.getLogger(DashboardHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return data;
        }
    }

    /**
     * Find a project where user is a moderator, owner or a member with modify permission.
     * @return List of projects. if its empty user has no projects where he/she can perform import operation
     */
    public static List checkProjectsForImportLog(Connection conn, String userid, String companyid) throws ServiceException {
        List<String> ll = new ArrayList<String>();
        try {
            DbResults drs = DbUtil.executeQuery(conn, "SELECT p.projectid FROM project p INNER JOIN projectmembers pm "
                    + "ON p.projectid = pm.projectid WHERE pm.userid = ? AND p.companyid = ? "
                    + "AND pm.status IN (3,4,5) AND pm.inuseflag = 1 AND pm.planpermission = 4",
                    new Object[]{userid, companyid});
            while (drs.next()) {
                String projectid = drs.getString("projectid");
                if (!StringUtil.isNullOrEmpty(projectid)) {
                    ll.add(projectid);
                }
            }
        } finally {
            return ll;
        }
    }

    public static String updateMilestoneTimelineSelection(Connection conn, String userid, String state) {
        String data = KWLErrorMsgs.rsSuccessFalse;
        try {
            JSONObject jobj = new JSONObject(state);
            Iterator i = jobj.keys();
            while (i.hasNext()) {
                String projectid = (String) i.next();
                boolean val = jobj.getBoolean(projectid);
                DbUtil.executeUpdate(conn, "UPDATE projectmembers SET milestonestack = ? WHERE projectid = ? AND userid = ?",
                        new Object[]{val, projectid, userid});
            }
            data = KWLErrorMsgs.rsSuccessTrue;
        } catch (JSONException ex) {
            Logger.getLogger(DashboardHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return data;
        }
    }
    
    public static String getMaintainanceDetails(Connection conn,String companyid){ 
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";        
        try{
            JSONArray jarr=null;            
            String platformURL = ConfigReader.getinstance().get("platformURL");
            String pmURL = ConfigReader.getinstance().get("pmURL");            
            String action = "9";            
            JSONObject userData = new JSONObject();
            userData.put("remoteapikey",ConfigReader.getinstance().get("remotapikey"));
            userData.put("companyid",companyid);
            userData.put("requesturl",pmURL);            
            JSONObject resObj = APICallHandler.callApp(conn,platformURL, userData, companyid, action);
            if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                jarr=resObj.getJSONArray("data");
            }
            if (jarr!=null&&jarr.length()>0) {
                jobj.put("data", jarr);
                msg="Data fetched successfully";
                issuccess = true;
            } else {
                msg="Error occurred while fetching data ";
            }
        } catch (Exception ex){
            Logger.getLogger(DashboardHandler.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                 Logger.getLogger(DashboardHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj.toString();
    }

    public static String getCustomizedTopLinks(Connection conn, String companyID) throws ServiceException, JSONException {
        CompanyDAO cd = new CompanyDAOImpl();
        Company c = cd.getCompany(conn, companyID);
        String platformURL = String.format(ConfigReader.getinstance().get("platformURL"), c.getSubDomain());
        JSONObject jo = new JSONObject();
        jo.put("companyid", companyID);
        JSONObject resObj = APICallHandler.callApp(conn, platformURL, jo, companyID, "14");
        jo = new JSONObject();
        if (resObj.has("pstatus")) {
            jo.put("data", resObj.get("data"));
            jo.put("success", true);
        }
        return jo.toString();
    }
}
