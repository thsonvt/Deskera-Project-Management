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

/**
 *
 * @author Abhay
 */
import com.krawler.common.session.SessionExpiredException;

import com.krawler.common.service.ServiceException;
import com.krawler.database.DbPool.Connection;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.esp.database.dbcon;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.sql.ResultSet;
import javax.servlet.http.HttpServletRequest;

public class WidgetStateHandler {

    public static JSONObject insertDefaultWidgetState(Connection conn, HttpServletRequest request, String welcome) throws ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            String userId = AuthHandler.getUserid(request);
            String col1 = "";
            String col2 = "";
            String col3 = "";
            String query;
            try {
                String companyid = AuthHandler.getCompanyid(request, true);
                boolean pmtrue = false;
                if (DashboardHandler.isSubscribed(conn, companyid, "pm") && DashboardHandler.isFeatureView(conn, companyid, "pm")) {
                    pmtrue = true;
                }
                if (welcome.equals("false")) {
                    if (col1.equals("")) {
                        col1 += "{\"id\":\"mydocs_drag\"}";
                    } else {
                        col1 += ",{\"id\":\"mydocs_drag\"}";
                    }
                    col1 += ",{\"id\":\"chart_drag\"}";
                    if (col2.equals("") || col2.charAt(col2.length() - 1) == ',') {
                        col2 += "{\"id\":\"taskwiseprojecthealth_drag\"}";
                    } else {
                        col2 += ",{\"id\":\"taskwiseprojecthealth_drag\"}";
                    }
                    col2 += ",{\"id\":\"pm_drag\"}";
                    if (pmtrue) {
                        col3 = "{\"id\": \"quicklinks_drag\"},{\"id\":\"requests_drag\"},{\"id\":\"announcements_drag\"}";
                    } else {
                        col2 += ",{\"id\":\"requests_drag\"},{\"id\":\"announcements_drag\"}";
                        col3 = "{\"id\": \"quicklinks_drag\"}";
                    }
                } else {
                    col2 = "{\"id\": \"quicklinks_drag\"},{\"id\":\"requests_drag\"},{\"id\":\"announcements_drag\"},{\"id\":\"mydocs_drag\"},{\"id\":\"pm_drag\"}";
                    col1 = "{\"id\": \"welcome_drag\"}";
                }
            } catch (Exception e) {
                System.out.println("DashboardHandler.insertDefaultWidgetState:" + e.getMessage());
            }
            if (welcome.equals("false")) {
                jobj = new JSONObject("{'col1':" + (!col1.equals("") ? "[" + col1 + "]" : "[]") + ",'col2':" + (!col2.equals("") ? "[" + col2 + "]" : "[]") + ",'col3':" + (!col3.equals("") ? "[" + col3 + "]" : "[]") + "}");
                query = "select * from widgetmanagement where userid = ?";
                DbResults rs = DbUtil.executeQuery(conn, query, new Object[]{userId});
                if (rs.next()) {
                    if (rs.getObject("widgetstate").equals(jobj)) {
                        query = "update widgetmanagement set widgetstate = ? where userid = ?";
                        DbUtil.executeUpdate(conn, query, new Object[]{jobj.toString(), userId});
                    }
                } else {
                    query = "insert into widgetmanagement(userid,widgetstate,helpflag) values(?,?,0)";
                    DbUtil.executeUpdate(conn, query, new Object[]{userId, jobj.toString()});
                }
            } else {
                jobj = new JSONObject("{'col1':" + (!col1.equals("") ? "[" + col1 + "]" : "[]") + ",'col2':" + (!col2.equals("") ? "[" + col2 + "]" : "[]") + "}");
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("WidgetStateHandler.insertDefaultWidgetStates", ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("WidgetStateHandler.insertDefaultWidgetStates", ex);
        } finally {
            return jobj;
        }
    }

    public static String getWidgetStates(Connection conn, HttpServletRequest request) throws ServiceException {
        JSONObject empty = new JSONObject();
        try {
            String userId = AuthHandler.getUserid(request);
            String query = "SELECT widgetstate, helpflag, customwidget FROM widgetmanagement WHERE userid=?";
            DbResults rs = DbUtil.executeQuery(conn, query, new Object[]{userId});
            String welcome = request.getParameter("welcome");
            if (rs.next() && welcome.compareTo("false") == 0) {
                empty = new JSONObject(rs.getString("widgetstate"));
                if (!empty.has("col3")) {
                    empty = insertDefaultWidgetState(conn, request, welcome);
                }
                empty.put("helpflag", rs.getBoolean("helpflag"));
                empty.put("customwidget", rs.getBoolean("customwidget"));
            } else {
                empty = insertDefaultWidgetState(conn, request, welcome);
                empty.put("helpflag", false);
                empty.put("customwidget",false);
            }
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("WidgetStateHandler.getWidgetStates", ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("WidgetStateHandler.getWidgetStates", ex);
        } finally {
            return empty.toString();
        }
    }

    public static String removeWidgetFromState(Connection conn, HttpServletRequest request) throws ServiceException {
        JSONObject empty = new JSONObject();
        try {
            String wid = request.getParameter("wid");
            String userId = AuthHandler.getUserid(request);
            String query = "SELECT widgetstate FROM widgetmanagement WHERE userid=?";
            DbResults rs = DbUtil.executeQuery(conn, query, new Object[]{userId});
            if (rs.next()) {
                empty = new JSONObject(rs.getString("widgetstate"));
            }
            JSONObject _state = getColumnPositionInWidgetState(empty, wid);
            String column = _state.getString("column");
            JSONObject jobj = deleteFromWidgetStateJson(conn, empty, "id", wid, column);
            updateWidgetManagementTable(conn, userId, jobj.toString());
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("WidgetStateHandler.removeWidgetFromState", ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("WidgetStateHandler.removeWidgetFromState", ex);
        } finally {
            return "{success: true}";
        }
    }

    public static JSONObject deleteFromWidgetStateJson(Connection conn, JSONObject jobj, String key, String value, String column) throws JSONException {
        com.krawler.utils.json.base.JSONArray jobj_col = deleteFromWidgetStateJsonArray(conn, jobj.getJSONArray(column), key, value);
        jobj.put(column, jobj_col);
        return jobj;
    }

    public static com.krawler.utils.json.base.JSONArray deleteFromWidgetStateJsonArray(Connection conn, com.krawler.utils.json.base.JSONArray jArr, String key, String value) throws JSONException {
        com.krawler.utils.json.base.JSONArray toReturn = new com.krawler.utils.json.base.JSONArray();
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject empty = jArr.getJSONObject(i);
            if (!empty.get("id").toString().equals(value)) {
                toReturn.put(empty);
            }
        }
        return toReturn;
    }

    public static String insertWidgetIntoState(Connection conn, HttpServletRequest request) throws ServiceException {
        JSONObject empty = new JSONObject();
        try {
            String userId = AuthHandler.getUserid(request);
            String wid = request.getParameter("wid");
            String colno = request.getParameter("colno");
            String columnToUpdate = "col" + colno;
            JSONObject jobj = new JSONObject(getWidgetStates(conn, request));
            if (jobj.has("helpflag")) {
                jobj.remove("helpflag");
            }
            JSONObject check = getColumnPositionInWidgetState(jobj, wid);
            if (!check.getBoolean("present")) {
                empty.put("id", wid);
                jobj.append(columnToUpdate, empty);
                updateWidgetManagementTable(conn, userId, jobj.toString());
            }
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("WidgetStateHandler.removeWidgetFromState", ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("WidgetStateHandler.removeWidgetFromState", ex);
        } finally {
            return "{success: true}";
        }

    }

    public static void updateWidgetManagementTable(Connection conn, String userId, String widgetState) throws ServiceException {
        String query = "update widgetmanagement set widgetstate=? where userid=?";
        DbUtil.executeUpdate(conn, query, new Object[]{widgetState, userId});
    }

    public static String changeWidgetStateOnDrop(Connection conn, HttpServletRequest request) throws ServiceException {
        try {
            String wid = request.getParameter("wid");
            String userId = AuthHandler.getUserid(request);
            String colno = request.getParameter("colno");
            String columnToEdit = "col" + colno;
            int position = Integer.parseInt(request.getParameter("position"));
            JSONObject jobj = new JSONObject(getWidgetStates(conn, request));
            if (jobj.has("helpflag")) {
                jobj.remove("helpflag");
            }
            JSONObject previous_details = getColumnPositionInWidgetState(jobj, wid);
            String pre_column = previous_details.getString("column");
            int pre_position = previous_details.getInt("position");
            if (pre_position < position && columnToEdit.equals(pre_column)) {
                position--;
            }
            jobj = deleteFromWidgetStateJson(conn, jobj, "id", wid, pre_column);
            com.krawler.utils.json.base.JSONArray jobj_col = jobj.getJSONArray(columnToEdit);
            JSONObject empty = new JSONObject();
            empty.put("id", wid);
            jobj_col = insertIntoJsonArray(jobj_col, position, empty);
            //jobj_col.put(position, empty);
            jobj.put(columnToEdit, jobj_col);
            updateWidgetManagementTable(conn, userId, jobj.toString());
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("WidgetStateHandler.removeWidgetFromState", ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("WidgetStateHandler.removeWidgetFromState", ex);
        } finally {
            return "{success: true}";
        }
    }

    public static JSONArray insertIntoJsonArray(JSONArray jArr, int position, JSONObject newjObj) throws JSONException {
        JSONArray toReturn = new JSONArray();
        Boolean added = false;
        for (int i = 0; i < jArr.length(); i++) {
            if (i == position) {
                toReturn.put(newjObj);
                added = true;

            }
            JSONObject empty = jArr.getJSONObject(i);
            toReturn.put(empty);
        }
        if (!added) {
            toReturn.put(newjObj);
        }
        return toReturn;
    }

    public static JSONObject getColumnPositionInWidgetState(JSONObject jobj, String wid) throws JSONException {
        JSONObject toReturn = new JSONObject();
        for (int i = 1; i <= 3; i++) {
            String column = "col" + String.valueOf(i);
            com.krawler.utils.json.base.JSONArray jArr = jobj.getJSONArray(column);
            for (int j = 0; j < jArr.length(); j++) {
                JSONObject empty = jArr.getJSONObject(j);
                if (empty.get("id").toString().equals(wid)) {
                    toReturn.put("column", column);
                    toReturn.put("position", j);
                    toReturn.put("present", true);
                    return toReturn;
                }
            }
        }
        toReturn.put("present", false);
        return toReturn;
    }

    public static int updateCustomWidgetFlag(Connection conn, String userID, boolean isCustomWidget) throws ServiceException {
        return DbUtil.executeUpdate(conn, "UPDATE widgetmanagement SET customwidget = ? WHERE userid = ?", new Object[]{isCustomWidget, userID});
    }
    
    public static int updateCustomWidgetSetting(Connection conn, String companyID, boolean isCustomWidget) throws ServiceException {
        return DbUtil.executeUpdate(conn, "UPDATE widgetmanagement SET customwidget = ? WHERE userid IN (SELECT userid FROM users WHERE companyid = ?)", new Object[]{isCustomWidget, companyID});
    }
}
