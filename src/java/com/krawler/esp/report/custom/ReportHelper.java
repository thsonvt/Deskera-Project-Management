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
package com.krawler.esp.report.custom;

import com.krawler.database.DbResults;
import com.krawler.esp.report.ReportBean;
import com.krawler.esp.report.ReportColumn;
import com.krawler.esp.report.Reports;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author kamlesh
 */
public class ReportHelper {

    public static CustomReport getCustomReport(String companyId) {
        return new CustomReportImpl(companyId);
    }

    public static CustomReport getCustomReportWithMilestoneTimeline(String companyId, String loginID, boolean isPaging, boolean isMilestoneTL) {
        return new CustomReportWithMilestoneTimelineImpl(companyId, loginID, isPaging, isMilestoneTL);
    }

    public static CustomReport getCustomReportWithMilestoneTimeline(String companyId, String loginID) {
        return new CustomReportWithMilestoneTimelineImpl(companyId, loginID);
    }

    public static ReportBean setReportsBean(ResultSet rs, ReportBean bean) throws SQLException {
        if (rs == null) {
            throw new IllegalArgumentException("resultSet cannot be null");
        }
        int numcolumn = bean.getCoumnCount();
        while (rs.next()) {
            Object[] row = new Object[numcolumn];
            for (int i = 0; i < numcolumn; i++) {
                row[i] = rs.getObject(i + 1);
            }
            bean.addRow(row);
        }
        rs.close();
        return bean;
    }

    public static Reports getReportsObject(DbResults rs) {
        Reports r = new Reports();
        if (rs.has("reportid")) {
            r.setReportID(rs.getString("reportid"));
        }
        if (rs.has("companyid")) {
            r.setCompanyID(rs.getString("companyid"));
        }
        if (rs.has("reportname")) {
            r.setReportName(rs.getString("reportname"));
        }
        if (rs.has("type")) {
            r.setType(rs.getString("type"));
        }
        if (rs.has("description")) {
            r.setDescription(rs.getString("description"));
        }
        if (rs.has("createdby")) {
            r.setCreatedBy(rs.getString("createdby"));
        }
        if (rs.has("category")) {
            r.setCategory(rs.getString("category"));
        }
        if (rs.has("createdon")) {
            r.setCreatedON(rs.getDate("createdon"));
        }
        if (rs.has("modifiedon")) {
            r.setModifiedON(rs.getDate("modifiedon"));
        }
        if (rs.has("paging")) {
            r.setPaging(rs.getBoolean("paging"));
        }
        if (rs.has("milestone")) {
            r.setMilestone(rs.getBoolean("milestone"));
        }
        return r;
    }

    public static ReportColumn getReportColumnObject(DbResults obj) {
        ReportColumn rc = new ReportColumn();
        if (obj.has("columnid")) {
            rc.setColumnID(obj.getString("columnid"));
        }
        if (obj.has("reportid")) {
            rc.setReportID(obj.getString("reportid"));
        }
        if (obj.has("dataindex")) {
            rc.setDataIndex(obj.getString("dataindex"));
        }
        if (obj.has("tablename")) {
            rc.setTableName(obj.getString("tablename"));
        }
        if (obj.has("fieldname")) {
            rc.setFieldName(obj.getString("fieldname"));
        }
        if (obj.has("header")) {
            rc.setHeader(obj.getString("header"));
        }
        if (obj.has("displayheader")) {
            rc.setDisplayHeader(obj.getString("displayheader"));
        }
        if (obj.has("name")) {
            rc.setName(obj.getString("name"));
        }
        if (obj.has("module")) {
            rc.setModule(obj.getString("module"));
        }
        if (obj.has("renderer")) {
            rc.setRenderer(obj.getString("renderer"));
        }
        if (obj.has("headerkey")) {
            rc.setHeaderkey(obj.getString("headerkey"));
        }
        if (obj.has("quicksearch")) {
            rc.setQuickSearch(obj.getBoolean("quicksearch"));
        }
        if (obj.has("ismandatory")) {
            rc.setIsMandatory(obj.getBoolean("ismandatory"));
        }
        if (obj.has("summary")) {
            rc.setSummary(obj.getString("summary"));
        }
        if (obj.has("tablename")) {
            rc.setTableName(obj.getString("tablename"));
        }
        if (obj.has("displayorder")) {
            rc.setDisplayOrder(obj.getInt("displayorder"));
        }
        if (obj.has("type")) {
            rc.setType(obj.getInt("type"));
        }
        return rc;
    }

    public static List<ReportColumn> getReportColumn(JSONArray jColumns) throws JSONException {
        int c = jColumns.length();
        List<ReportColumn> col = new ArrayList<ReportColumn>(c);
        for (int i = 0; i < c; i++) {
            ReportColumn rc = getReportColumn(jColumns.getJSONObject(i));
            col.add(rc);
        }
        return col;
    }

    public static ReportColumn getReportColumn(JSONObject obj) throws JSONException {
        ReportColumn rc = new ReportColumn();
        rc.setColumnID(obj.getString("columnID"));
        rc.setDataIndex(obj.getString("dataIndex"));
        rc.setFieldName(obj.getString("name"));
        rc.setHeader(obj.getString("header"));
        rc.setDisplayHeader(obj.getString("displayHeader"));
        rc.setName(obj.getString("name"));
        rc.setQuickSearch(obj.getBoolean("quickSearch"));
        rc.setSummary(obj.getString("summary"));
        rc.setTableName(obj.getString("tableName"));
        rc.setDisplayOrder(obj.getInt("displayOrder"));
        return rc;
    }

    public static QueryFilter getFilterObject(JSONObject jobj) throws JSONException {
        QueryFilter q = new QueryFilter();
        q.setOperator(jobj.getString("op"));
        q.setTableName(jobj.getString("tableName"));
        q.setFieldName(jobj.getString("fieldName"));
        q.setCondition(jobj.getString("condition"));
        q.setType(Integer.parseInt(jobj.getString("type")));
        q.setValue(jobj.getString("value"));
        return q;

    }
}
