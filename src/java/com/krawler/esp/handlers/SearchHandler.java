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

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;

import com.krawler.common.service.ServiceException;
import com.krawler.database.DbPool;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.database.DbPool.Connection;
import com.krawler.esp.Search.SearchBean;
import com.krawler.esp.Search.Summarizer;
import com.krawler.utils.json.KWLJsonConverter;
import com.krawler.common.timezone.Timezone;
import com.krawler.common.util.StringUtil;
import java.util.ArrayList;

public class SearchHandler {

    public static String QuickSearchData(Connection conn, String Type,
            String Keyword, String companyid) throws ServiceException {
        String ss = null;
        PreparedStatement pstmt = null;
        Pattern p = Pattern.compile("(?i)tag:['?(\\s*\\w+)'?]*",
                Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(Keyword);
        boolean tagQuery = m.matches();
        ResultSet rs = null;
        int count1 = 0;
        if (!tagQuery) {
            try {
                String MyQuery = Keyword;
                String MyQuery1 = Keyword;
                if (Keyword.length() > -1) {
                    MyQuery = Keyword + "%";
                    MyQuery1 = "% " + MyQuery;
                }
                if (Type.equals("user")) {
                    pstmt = conn.prepareStatement("select count(*) as count from users inner join userlogin on users.userid = userlogin.userid "
                            + "where users.companyid=? and userlogin.isactive = true and (userlogin.username LIKE ?  OR userlogin.username LIKE ?  OR users.lname LIKE ? OR users.lname LIKE ? OR users.fname LIKE ? OR users.fname LIKE ? OR concat(users.fname,' ',users.lname) LIKE ? OR concat(users.fname,' ',users.lname) LIKE ?)");
                    pstmt.setString(1, companyid);
                    pstmt.setString(2, MyQuery);
                    pstmt.setString(3, MyQuery1);
                    pstmt.setString(4, MyQuery);
                    pstmt.setString(5, MyQuery1);
                    pstmt.setString(6, MyQuery);
                    pstmt.setString(7, MyQuery1);
                    pstmt.setString(8, MyQuery);
                    pstmt.setString(9, MyQuery1);
                    rs = pstmt.executeQuery();
                    rs.next();
                    count1 = rs.getInt("count");
                    pstmt.clearParameters();
                    pstmt = conn.prepareStatement("select users.userid as id,concat(users.fname,' ',users.lname) as name, image as img from users "
                            + "inner join userlogin on users.userid = userlogin.userid where users.companyid=? and userlogin.isactive = true and (userlogin.username LIKE ?  OR userlogin.username LIKE ?  OR users.lname LIKE ? OR users.lname LIKE ? OR users.fname LIKE ? OR users.fname LIKE ? OR concat(users.fname,' ',users.lname) LIKE ? OR concat(users.fname,' ',users.lname) LIKE ?)");// (username
                    pstmt.setString(1, companyid);
                    pstmt.setString(2, MyQuery);
                    pstmt.setString(3, MyQuery1);
                    pstmt.setString(4, MyQuery);
                    pstmt.setString(5, MyQuery1);
                    pstmt.setString(6, MyQuery);
                    pstmt.setString(7, MyQuery1);
                    pstmt.setString(8, MyQuery);
                    pstmt.setString(9, MyQuery1);
                    rs = pstmt.executeQuery();

                } else if (Type.equals("com")) {
                    pstmt = conn.prepareStatement("select count(*) as count from community where companyid=? and communityname LIKE ? OR communityname LIKE ?");
                    pstmt.setString(1, companyid);
                    pstmt.setString(2, MyQuery);
                    pstmt.setString(3, MyQuery1);
                    rs = pstmt.executeQuery();
                    rs.next();
                    count1 = rs.getInt("count");
                    pstmt.clearParameters();
                    pstmt = conn.prepareStatement("select communityid as id,communityname as name, image as img from community where companyid=? and communityname LIKE ? OR communityname LIKE ?");
                    pstmt.setString(1, companyid);
                    pstmt.setString(2, MyQuery);
                    pstmt.setString(3, MyQuery1);
                    rs = pstmt.executeQuery();
                } else if (Type.equals("pro")) {

                    pstmt = conn.prepareStatement("select count(*) as count from project where companyid=? and archived = 0 and (projectname LIKE ? OR projectname LIKE ?)");
                    pstmt.setString(1, companyid);
                    pstmt.setString(2, MyQuery);
                    pstmt.setString(3, MyQuery1);
                    rs = pstmt.executeQuery();
                    rs.next();
                    count1 = rs.getInt("count");
                    pstmt.clearParameters();
                    pstmt = conn.prepareStatement("select projectid as id,projectname as name, image as img from project where companyid=? and archived = 0 and (projectname LIKE ? OR projectname LIKE ?)");
                    pstmt.setString(1, companyid);
                    pstmt.setString(2, MyQuery);
                    pstmt.setString(3, MyQuery1);
                    rs = pstmt.executeQuery();

                }
                KWLJsonConverter k = new KWLJsonConverter();
                ss = k.GetJsonForGrid(rs);
                ss = ss.substring(0, ss.length() - 1);
                ss += ",\"count\":[" + count1 + "]}";
            } catch (SQLException e) {
                throw ServiceException.FAILURE("SearchHandler.QuickSearchData",
                        e);
            } finally {
                DbPool.closeStatement(pstmt);
            }
        } else {
            try {
                Keyword = Keyword.replaceAll("(?i)tag:", "").trim();

                java.util.ArrayList<String> paramArray = new java.util.ArrayList<String>();
                String param = "(";
                if (!Keyword.contains(" ")) {
                    param += "?)";
                    paramArray.add(Keyword);
                } else {
                    // Keyword = Keyword.replaceAll("\\s+",",");
                    ArrayList<String> KeywordArr = com.krawler.esp.handlers.TagUtils.getTagArray(Keyword);
                    for (int i = 0; i < KeywordArr.size(); i++) {
                        param += "?,";
                        paramArray.add(com.krawler.esp.handlers.TagUtils.checkTag(KeywordArr.get(i)).trim());
                    }
                    param = param.substring(0, param.length() - 1) + ")";
                }

                if (Type.equals("user")) {
                    pstmt = conn.prepareStatement("select count(*) as count from(select distinct users.userid from users "
                            + "inner join profiletags on profiletags.id = users.userid inner join unitags  on profiletags.tagid = unitags.tagid "
                            + "inner join userlogin on users.userid = userlogin.userid where users.companyid = ? and userlogin.isactive = true and unitags.tagname in "
                            + param + ")as temp");
                    pstmt.setString(1, companyid);
                    for (int j = 0; j < paramArray.size(); j++) {
                        pstmt.setString(j + 2, paramArray.get(j));
                    }
                    rs = pstmt.executeQuery();
                    rs.next();
                    count1 = rs.getInt("count");
                    pstmt.clearParameters();
                    pstmt = conn.prepareStatement("select users.userid as id,userlogin.username as name, image as img from users inner join userlogin on users.userid = userlogin.userid "
                            + "where users.userid in (select distinct users.userid from users inner join profiletags on profiletags.id = users.userid "
                            + "inner join unitags  on profiletags.tagid = unitags.tagid where users.companyid = ? and unitags.tagname in "
                            + param + ") and userlogin.isactive = true");
                    pstmt.setString(1, companyid);
                    for (int j = 0; j < paramArray.size(); j++) {
                        pstmt.setString(j + 2, paramArray.get(j));
                    }

                    rs = pstmt.executeQuery();

                }
                if (Type.equals("com")) {
                    pstmt = conn.prepareStatement("select count(*) as count from(select distinct communityid from community inner join profiletags on profiletags.id = users.userid inner join unitags  on profiletags.tagid = unitags.tagid where unitags.tagname in "
                            + param + ")as temp");
                    for (int j = 0; j < paramArray.size(); j++) {
                        pstmt.setString(j + 1, paramArray.get(j));
                    }

                    rs = pstmt.executeQuery();
                    rs.next();
                    count1 = rs.getInt("count");
                    pstmt.clearParameters();
                    pstmt = conn.prepareStatement("select communityid as id,communityname as name, image as img from community where communityid in (select distinct communityid from community inner join profiletags on profiletags.id = users.userid inner join unitags  on profiletags.tagid = unitags.tagid where unitags.tagname in "
                            + param + ")");
                    for (int j = 0; j < paramArray.size(); j++) {
                        pstmt.setString(j + 1, paramArray.get(j));
                    }

                    rs = pstmt.executeQuery();

                }
                if (Type.equals("pro")) {
                    pstmt = conn.prepareStatement("select count(*) as count from(select distinct projectid from project inner join profiletags on profiletags.id =  project.projectid inner join unitags  on profiletags.tagid = unitags.tagid where project.companyid = ?  and archived = 0 and unitags.tagname in "
                            + param + ")as temp");
                    pstmt.setString(1, companyid);
                    for (int j = 0; j < paramArray.size(); j++) {
                        pstmt.setString(j + 2, paramArray.get(j));
                    }

                    rs = pstmt.executeQuery();
                    rs.next();
                    count1 = rs.getInt("count");
                    pstmt.clearParameters();
                    pstmt = conn.prepareStatement("select projectid as id,projectname as name, image as img from project where projectid in (select distinct projectid from project inner join profiletags on profiletags.id =  project.projectid  inner join unitags  on profiletags.tagid = unitags.tagid where project.companyid = ? and archived = 0  and unitags.tagname in "
                            + param + ")");
                    pstmt.setString(1, companyid);
                    for (int j = 0; j < paramArray.size(); j++) {
                        pstmt.setString(j + 2, paramArray.get(j));
                    }
                    rs = pstmt.executeQuery();

                }

                KWLJsonConverter k = new KWLJsonConverter();
                ss = k.GetJsonForGrid(rs);
                ss = ss.substring(0, ss.length() - 1);
                ss += ",\"count\":[" + count1 + "]}";
            } catch (SQLException e) {
                throw ServiceException.FAILURE("SearchHandler.QuickSearchData",
                        e);
            } finally {
                DbPool.closeStatement(pstmt);
            }

        }
        return ss;
    }

    public static String searchIndex(Connection conn, SearchBean bean,
            String querytxt, String numhits, String perpage, String startIn,
            String companyid, String userid) throws ServiceException,
            IOException {
        // Pattern p = Pattern.compile("(?i)tag:(\\w*\\s*)");
        Pattern p = Pattern.compile("^(?i)tag:[[\\s]*([\\w\\s]+[(/|\\{1})]?)*[\\s]*[\\w]+[\\s]*]*$");
        /*
         * "^([\'" + '"' + "]?)\\s*([\\w]+[(/|\\{1})]?)*[\\w]\\1$";
         */
        Matcher m = p.matcher(querytxt);
        boolean b = m.matches();

        if (!b) {
            String query = querytxt;
            String qfield = "PlainText";
            // TODO:numhits and hits per page to be used when paging tolbar is
            // attached to search grid
            int start = 0;
            int numofHitsPerPage = 10;
            int numofhits = 10;
            String resString = "{data:[";
            try {
                if (numhits != null) {
                    numofhits = Integer.parseInt(numhits);
                }
                if (perpage != null) {
                    numofHitsPerPage = Integer.parseInt(perpage);
                }
                if (startIn != null) {
                    start = Integer.parseInt(startIn);
                }
                Hits hitresult = null;
                String sql = "(select docid from docs where userid=?)"
                        + "union (select docs.docid from docprerel inner join docs on docs.docid=docprerel.docid where docprerel.permission=? and docprerel.userid=?) "
                        + "union (select docs.docid from docprerel inner join docs on docs.docid=docprerel.docid inner join userrelations on userrelations.userid1=docprerel.userid where docprerel.permission=? and userrelations.userid2=?) "
                        + "union (select docs.docid from docprerel inner join docs on docs.docid=docprerel.docid inner join userfriendmapping on userfriendmapping.friendid=docprerel.userid  where docprerel.permission=? and userfriendmapping.userid=?)"
                        + "union (select docs.docid from docprerel inner join docs on docs.docid=docprerel.docid inner join users on users.userid=docs.userid where docprerel.permission=? and users.companyid=?)";
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
                // TODO: change hardcoded userid value to current logged in
                // users id
                // [shri]
                pstmt.setString(1, userid);
                pstmt.setString(2, "2");
                pstmt.setString(3, userid);
                pstmt.setString(4, "1");
                pstmt.setString(5, userid);
                pstmt.setString(6, "1");
                pstmt.setString(7, userid);
                pstmt.setString(8, "3");
                pstmt.setString(9, companyid);
                java.sql.ResultSet rs = pstmt.executeQuery();
                if (query.length() > 2) {
                    query += "*";
                }
                query = qfield + ":" + query;
                java.sql.ResultSetMetaData rsmd = rs.getMetaData();
                boolean flag = true;
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    if (flag) {
                        query += " AND (";

                    } else {
                        query += " OR ";
                    }
                    query += "DocumentId:" + rs.getString("docid");
                    flag = false;

                }
                query += ")";

                if (found) {
                    hitresult = bean.skynetsearch(query, qfield);

                    Iterator itr = hitresult.iterator();
                    
                    ArrayList<String> docs = new ArrayList<String>();

                    while (itr.hasNext()) {

                        Hit hit1 = (Hit) itr.next();
                        org.apache.lucene.document.Document doc = hit1.getDocument();
                        Enumeration docfields = doc.fields();
                        docs.add(doc.get("DocumentId"));
                        Summarizer summary = new Summarizer();
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                                "EEE MMM d HH:mm:ss z yyyy");
                        java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat(
                                "yyyy-MM-dd HH:mm:ss");
                        String theTime = Timezone.toCompanyTimezone(conn, sdf1.format(sdf.parse(doc.get("DateModified"))), companyid);
                        java.util.Date d = new java.util.Date();
                        theTime = Timezone.dateTimeRenderer(conn, Timezone.toCompanyTimezone(conn, sdf1.format(d), companyid), theTime, companyid);
                        resString += "{";
                        resString += "\"FileName\":\"" + doc.get("FileName")
                                + "\",";
                        resString += "\"Author\":\"" + doc.get("Author")
                                + "\",";
                        resString += "\"Size\":\""
                                + com.krawler.esp.handlers.FileHandler.getSizeKb(doc.get("Size")) + "\",";
                        resString += "\"DateModified\":\"" + theTime
                                + "\",";
                        resString += "\"DocumentId\":\""
                                + doc.get("DocumentId") + "\",";
                        resString += "\"Type\":\"" + doc.get("Type") + "\",";
                        resString += "\"RevisionNumber\":\""
                                + doc.get("Revision No") + "\",";
                        resString += "\"Summary\":\""
                                + URLEncoder.encode(
                                summary.getSummary(
                                doc.get("PlainText"), querytxt).toString(), "UTF8").replace(
                                "+", "%20") + "\"";

                        if (itr.hasNext()) {
                            resString += "},";
                        } else {
                            resString += "}";
                        }
                    }
                    String docids = "";
                    if(!docs.isEmpty())
                        docids = docs.toString();
                    docids = docids.replace("[", "").replace("]", "").replaceAll(", ", "','");
                    docids = "'".concat(docids).concat("'");
                    DbResults drs = DbUtil.executeQuery(conn, "SELECT docid, docname, docsize, doctype, docdatemod FROM docs WHERE docname LIKE '%"+querytxt+"%' AND userid = ? AND docid NOT IN ("+docids+")", userid);
                    if(!resString.equals("{data:["))
                        resString += ",";
                    resString += getResultString(conn, drs, userid);
                }
                resString += "]}";
            } catch (ParseException e) {
                throw ServiceException.FAILURE("SearchHandler.searchIndex", e);
            } catch (SQLException e) {
                throw ServiceException.FAILURE("SearchHandler.searchIndex", e);
            }
            return resString;
        } else {
            String resString = "{data:[";
            querytxt = querytxt.replaceFirst("(?i)tag:", "");
            querytxt = querytxt.trim();
            if (querytxt.contains(" ")) {
                querytxt = querytxt.replaceAll("\\s+", ",");
            }
            resString += docTagSearch(conn, querytxt, userid);

//            if (resString.charAt(resString.length() - 1) != '[') {
//                resString = resString.substring(0, (resString.length() - 1));
//            }
            resString += "]}";
            return resString;
        }
    }

    public static String docTagSearch(Connection conn, String tagname,
            String userid) throws ServiceException, IOException {
        String resString = "";
        DbResults rs = null;
        String sql = "";
        java.util.ArrayList<Object> paramArray = new java.util.ArrayList<Object>();
        String param = "(";
        if (!tagname.contains(",")) {
            param += "?)";
            paramArray.add(tagname);
        } else {
            String[] tagArr = tagname.split(",");
            for (int i = 0; i < tagArr.length; i++) {
                param += "?,";
                paramArray.add(tagArr[i]);
            }
            param = param.substring(0, param.length() - 1) + ")";
        }
        paramArray.add(userid);

        sql = "select distinct docs.docid,docs.docname,docs.docsize,docs.doctype,docs.docdatemod"
                + " from docs inner join docstags on docs.docid = docstags.docid inner join unitags on unitags.tagid = docstags.tagid"
                + " inner join users on users.userid = docstags.id where unitags.tagname in "
                + param + " and users.userid = ?";
        rs = DbUtil.executeQuery(conn, sql, paramArray.toArray());
        resString += getResultString(conn, rs, userid);

        sql = "select distinct docs.docid,docs.docname,docs.docsize,docs.doctype,docs.docdatemod "
                + "from docs inner join docstags on docs.docid = docstags.docid inner join unitags on unitags.tagid = docstags.tagid"
                + " inner join projectmembers on projectmembers.projectid = docstags.id  where unitags.tagname in "
                + param + " and projectmembers.userid = ?";
        rs = DbUtil.executeQuery(conn, sql, paramArray.toArray());
        resString += getResultString(conn, rs, userid);

        sql = "select distinct docs.docid,docs.docname,docs.docsize,docs.doctype,docs.docdatemod "
                + "from docs inner join docstags on docs.docid = docstags.docid inner join unitags on unitags.tagid = docstags.tagid "
                + "inner join communitymembers on communitymembers.communityid = docstags.id  where unitags.tagname in "
                + param + " and communitymembers.userid = ?";

        rs = DbUtil.executeQuery(conn, sql, paramArray.toArray());
        resString += getResultString(conn, rs, userid);

        return resString;
    }

    public static String getResultString(Connection conn, com.krawler.database.DbResults rsforJson, String userid) throws ServiceException {
        com.krawler.utils.json.base.JSONObject jobj = null;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String gridString = "";
        try {
            String companyid = CompanyHandler.getCompanyByUser(conn, userid);
            while (rsforJson.next()) {
                jobj = new com.krawler.utils.json.base.JSONObject();
                jobj.put("DocumentId", rsforJson.getString(1));
                jobj.put("FileName", rsforJson.getString(2));
                jobj.put("Size", com.krawler.esp.handlers.FileHandler.getSizeKb(rsforJson.getString(3)));
                jobj.put("Type", rsforJson.getString(4));
                String theTime = Timezone.toCompanyTimezone(conn, rsforJson.getObject(5).toString(), companyid);
                java.util.Date d = new java.util.Date();
                theTime = Timezone.dateTimeRenderer(conn, Timezone.toCompanyTimezone(conn, sdf.format(d), companyid), theTime, companyid);
                jobj.put("DateModified", theTime);
                jobj.put("RevisionNumber", "-");
                jobj.put("Author", com.krawler.esp.handlers.FileHandler.getAuthor(rsforJson.getString(1), 0));
                gridString += jobj.toString();
                gridString += ",";
            }
            if(!StringUtil.isNullOrEmpty(gridString))
                gridString = gridString.substring(0, gridString.lastIndexOf(','));
        } catch (com.krawler.utils.json.base.JSONException e) {
            throw ServiceException.FAILURE("FileHandler.getJString", e);
        }

        return gridString;
    }
}
