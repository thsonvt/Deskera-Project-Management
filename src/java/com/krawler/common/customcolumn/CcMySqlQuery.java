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
package com.krawler.common.customcolumn;

import com.krawler.common.util.StringUtil;

/**
 *
 * @author Kamlesh Kumar sah
 */
class CcMySqlQuery extends CCQuery {

    public CcMySqlQuery() {
    }

    @Override
    public String getAllMetaData() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getCCDataQuery(String referenceId, ColumnsMetaData csmd) {
        StringBuilder query = new StringBuilder("SELECT ");
        int l = csmd.getColumnCount();
        for (int i = 0; i < l; i++) {
            query.append(csmd.getColumn(i).getNo()).append(",");
        }
        query.deleteCharAt(query.length() - 1).append(" FROM ccdata where referenceid = '").append(referenceId).append("'");
        return query.toString();
    }

    @Override
    public String getCcAllDataQuery(String module, int[] c) {
        StringBuilder query = new StringBuilder("SELECT ");
        for (int i = 0; i < c.length; i++) {
            query.append("c").append(c[i]).append(",");
        }
        query.deleteCharAt(query.length() - 1).append(" FROM ccdata where module = '").append(module).append("'");
        return query.toString();
    }

    @Override
    public String insertColumnQuery() {
        return "insert into ccmetadata(columnid,companyid,module,name,header,dataindex,ismandatory,visible,defaultvalue,"
                + "enabled," + "renderer,editor,type,columnno,userid,masterdata,createdon,modifiedon) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,current_timestamp,current_timestamp) ";
    }

   @Override
    public String editColumnQuery() {
        return "update ccmetadata set name =?, header=?, dataindex=?, visible=?, enabled=?, " /* defaultvalue=?,*/  /* ismandatory=?,*/
                + "renderer=?, editor=?, userid=?, masterdata=? where columnid = ? ";
    }

    @Override
    public String getSearchQuery(String module, String[] colno, String ss) {
        StringBuilder query = new StringBuilder("SELECT referenceid from ccdata where module='" + module + "' ");
        ss = StringUtil.getSearchString(ss, "AND", colno);
        return query.append(ss).toString();
    }
}
