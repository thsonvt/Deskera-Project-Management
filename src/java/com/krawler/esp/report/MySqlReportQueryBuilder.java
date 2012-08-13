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
package com.krawler.esp.report;

import com.krawler.common.customcolumn.CustomColumn;
import com.krawler.common.util.Constants;
import com.krawler.esp.report.custom.QueryFilter;
import com.krawler.common.util.KrawlerArrayList;
import com.krawler.common.util.KrawlerList;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class MySqlReportQueryBuilder extends ReportQueryBuilder {

    private KrawlerList<String> fields;
    //private Set<String> references = new HashSet<String>();
    private StringBuilder whereClause;
    private StringBuilder groupBy;
    private StringBuilder orderby;
    private StringBuilder search;
    private String ss = "";
    private int limit = Constants.Default_Int_Initializer, offset = 0;
    List<ReportColumn> columns;
    List<QueryFilter> filters = new ArrayList<QueryFilter>();

    public MySqlReportQueryBuilder() {
        fields = new KrawlerArrayList<String>();
        groupBy = new StringBuilder("group by ");
        orderby = new StringBuilder("order by ");
        whereClause = new StringBuilder("WHERE companyid = ? ");
        search = new StringBuilder();
    }

    public MySqlReportQueryBuilder(List<ReportColumn> columns, String ss) {
        fields = new KrawlerArrayList<String>();
        groupBy = new StringBuilder("group by ");
        orderby = new StringBuilder("order by ");
        whereClause = new StringBuilder("WHERE project.companyid = ? ");
        search = new StringBuilder();
        this.ss = ss;
        this.columns = columns;
        setQyeryValues();
    }
    
    public MySqlReportQueryBuilder(List<ReportColumn> columns, String ss, int l, int s) {
        fields = new KrawlerArrayList<String>();
        groupBy = new StringBuilder("group by ");
        orderby = new StringBuilder("order by ");
        whereClause = new StringBuilder("WHERE project.companyid = ? ");
        search = new StringBuilder();
        this.ss = ss;
        this.columns = columns;
        this.limit = l;
        this.offset = s;
        setQyeryValues();
    }
    
    @Override
    public void addField(String tablename, String fieldname) {
        fields.add(tablename + "." + fieldname);
    }

    @Override
    public void addField(String tablename, String fieldname, int type, String dataIndex) {
        if (type == CustomColumn.DATE && !tablename.equals("ccdata")) {
            fields.add("DATE_FORMAT(" + tablename + "." + fieldname + ",'%Y-%m-%d') as " + dataIndex);
        } else {
            fields.add(tablename + "." + fieldname + " as " + dataIndex);
        }
    }

    @Override
    public String getQuery() {
        if (filters.size() > 0) {
            addWhereClause();
        }
        StringBuilder query = new StringBuilder("SELECT ");
        query.append(fields.join(",")).append(getReferences()).append(whereClause);
        if (search.length() > 1) {
            query.append(" AND (").append(search).append(") ");
        }
        if(limit != Constants.Default_Int_Initializer)
            query.append(" LIMIT ").append(limit).append(" OFFSET ").append(offset);
        return query.toString();

    }
    
    @Override
    public String getQueryForMT(String userID) {
        whereClause = new StringBuilder("WHERE project.companyid = ? ");
        if (filters.size() > 0) {
            addWhereClause();
        }
        String ref = getReferences();
        ref = ref.concat(" INNER JOIN projectmembers pm ON pm.projectid=project.projectid ");
        whereClause.append(" AND project.archived = false AND pm.status IN (3,4,5) AND pm.inuseflag = 1 AND pm.userid = '").append(userID).append("'");
        StringBuilder query = new StringBuilder("SELECT ");
        query.append(fields.join(",")).append(ref).append(whereClause);
        if (search.length() > 1) {
            query.append(" AND (").append(search).append(") ");
        }
        if(limit != Constants.Default_Int_Initializer)
            query.append(" LIMIT ").append(limit).append(" OFFSET ").append(offset);
        return query.toString();

    }
    
    @Override
    public String getCountQuery() {
        if (filters.size() > 0) {
            addWhereClause();
        }
        StringBuilder query = new StringBuilder("SELECT COUNT(*) AS count ");
        query.append(getReferences()).append(whereClause);
        if (search.length() > 1) {
            query.append(" AND (").append(search).append(") ");
        }
        return query.toString();
    }
    
    @Override
    public String getCountQueryForMT(String userID) {
        if (filters.size() > 0) {
            addWhereClause();
        }
        String ref = getReferences();
        ref = ref.concat(" INNER JOIN projectmembers pm ON pm.projectid=project.projectid ");
        whereClause.append(" AND project.archived = false AND pm.status IN (3,4,5) AND pm.inuseflag = 1 AND pm.userid = '").append(userID).append("'");
        StringBuilder query = new StringBuilder("SELECT COUNT(*) AS count ");
        query.append(ref).append(whereClause);
        if (search.length() > 1) {
            query.append(" AND (").append(search).append(") ");
        }
        return query.toString();
    }

    @Override
    public void addReferences(Set<String> tables) {
        //references = tables;
    }

    private void addWhereClause() {
        whereClause.append(" AND ").append("(");
        for (int i = 0; i < filters.size(); i++) {
            try {
                whereClause.append(filters.get(i).getFilterQuery());
            } catch (ParseException ex) {
                Logger.getLogger(MySqlReportQueryBuilder.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        whereClause.append(")");
    }

    @Override
    public void addSearch(String tablename, String fieldname, String ss) {
        if (!ss.isEmpty()) {
            if (search.length() > 2) {
                search.append(" OR ");
            }
            search.append(tablename + "." + fieldname).append(" like '%").append(ss).append("%'");
        }
    }

    @Override
    public void addFilter(QueryFilter qf) {
        filters.add(qf);
    }

    private String getReferences() {
        StringBuilder from = new StringBuilder(" FROM project inner join ccdata on project.projectid = ccdata.referenceid ");
//        if (references.contains("ccdata")) {
//            from.append("inner join ccdata on project.projectid = ccdata.referenceid ");
//        }
//        if (references.contains("users") || references.contains("userlogin")) {
//            from.append(" inner join projectmembers on project.projectid=projectmembers.projectid inner join users on projectmembers.userid=users.userid inner join userlogin on users.userid = userlogin.userid ");
//        }
        return from.toString();
    }

    private void setQyeryValues() {
        int len = columns.size();
        for (int i = 0; i < len; i++) {
            ReportColumn rc = columns.get(i);
            addField(rc.getTableName(), rc.getFieldName(), rc.getType(), rc.getDataIndex());
            //references.add(rc.getTableName());
            if (rc.isQuickSearch() && !ss.isEmpty()) {
                addSearch(rc.getTableName(), rc.getFieldName(), ss);
            }
        }
    }

    @Override
    public List<ReportColumn> getColumns() {
        return columns;
    }
}
