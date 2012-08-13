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

import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import com.krawler.database.DbPool.Connection;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import java.sql.PreparedStatement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is implementation of CustomColumn.
 *
 * @author Kamlesh Kumar Sah
 */
class CustomColumnImpl implements CustomColumn {

    private String companyId;
    private Class cn = null;
    private CCQuery qh = new CcMySqlQuery();
    private ColumnsMetaData rsmd = null;

    CustomColumnImpl(String companyId) throws ServiceException {
        this.companyId = companyId;
        this.rsmd = getColumnsMetaData(companyId);
    }

    CustomColumnImpl(String companyId, Class cn) throws ServiceException {
        this.companyId = companyId;
        this.cn = cn;
        this.rsmd = getColumnsMetaData(companyId);
    }

    @Override
    public ColumnsMetaData getColumnsMetaData(Connection conn) {
        return rsmd;
    }

    @Override
    public ColumnsMetaData getColumnsMetaData(Connection conn, String module) {
        ColumnsMDImpl csmd = new ColumnsMDImpl();
        for (int i = 0; i < rsmd.getColumnCount(); i++) {
            if (rsmd.getColumnModule(i).equals(module)) {
                csmd.addColumn(rsmd.getColumn(i));
            }
        }
        return csmd;
    }

    /**
     * flow :
     * set the value for offset according data type e.g c1,c11,c21 etc...
     * get all columnno for that range e.g c11 to c20.
     * search from availabel columnno from offset .
     * if not found then set the value for columnno= lmax value for that data type of comapany
     */
    @Override
    public boolean insertColumn(Connection conn, Column column) throws ServiceException {
        boolean res = false;
        try {
            int columnType = column.getType();
            String _px = CcHelper.getPrefix(columnType);
            int limit = CcHelper.getColumnLimit(_px);
            int colno = 1;
            String[] colnos = rsmd.getColumnNos(_px);
            int len = colnos.length;
            if (len == limit) {
                throw ServiceException.FAILURE("Max", null);    // columns are not availabe
            }
            int iColnos[] = CcHelper.getColumnNos(colnos);
            Arrays.sort(iColnos);
            for (int i = 0; i < len; i++) {
                //code for generate column no.
                if (colno != iColnos[i]) {
                    break;
                }
                colno++;
            }
            column.setNo(_px + colno);
            DbUtil.executeUpdate(conn, qh.insertColumnQuery(), new Object[]{column.getColumnId(), companyId, column.getModule(), column.getName(), column.getHeader(), column.getDataIndex(), column.isMandatory(), column.isVisible(), column.getDefaultValue(),
                        column.isEnabled(), column.getRenderer(), column.getEditor(), column.getType(), column.getNo(), column.getCreator(), column.getMasterString()});

            /* insert into all old project */
            // Object defValue = getParamsValue("c"+start, column.getDefaultValue());   //eg "c"+start is  c31
            if ((column.getType() != 0 || column.getType() != 2) && !StringUtil.isNullOrEmpty(column.getDefaultValue())) {
                DbUtil.executeUpdate(conn, "update ccdata set " + column.getNo() + " = ? where module=? and referenceid in (select projectid from project where companyid = ?)", new Object[]{column.getDefaultValue(), column.getModule(),companyId});
            }
            CCManager.closeCompanyCustomColumn(companyId);
            res = true;
        } catch (ServiceException ex) {
            if (ex.getMessage().indexOf("Max") > -1) {
                throw ex;
            }
            throw ServiceException.FAILURE("CustomColumn.insertColumn() : " + ex.getMessage(), ex);
        }

        return res;
    }

    @Override
    public boolean deleteColumn(Connection conn, String columnId) throws ServiceException {
        boolean res = false;
        try {
            Column col = rsmd.getColumnById(columnId);
            String val = "null";
            if (col.getType() == CustomColumn.BOOLEAN) {
                val = "false";
            }
            DbUtil.executeUpdate(conn, "update ccdata set " + col.getNo() + "  =" + val+" where referenceid in (select projectid from project where companyid = ?)", companyId); //column name is c1,c2 etc
            String sql = "delete from ccmetadata where columnid= ? ";
            if (DbUtil.executeUpdate(conn, sql, new Object[]{columnId}) > 0) {
                res = true;
            }
            CCManager.closeCompanyCustomColumn(companyId);
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("CustomColumn.deleteColumn", ex);
        }
        return res;
    }

    @Override
    public boolean editColumn(Connection conn, Column column) throws ServiceException {
        boolean res = false;
        try {
            int c = DbUtil.executeUpdate(conn, qh.editColumnQuery(), new Object[]{column.getName(), column.getHeader(), column.getDataIndex(), /*column.isMandatory(),*/ column.isVisible(), /*column.getDefaultValue(),*/
                        column.isEnabled(), column.getRenderer(), column.getEditor(), column.getCreator(), column.getMasterString(), column.getColumnId()});
            if (c > 0) {
                res = true;
            }
            CCManager.closeCompanyCustomColumn(companyId);
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("CustomColumn.editColumn()", ex);
        } finally {
        }
        return res;
    }

    @Override
    public ColumnSet getColumnsData(Connection conn, String module, String referenceId) throws ServiceException {
        ColumnSet rec = null;
        Map<Column, Object> records = new HashMap<Column, Object>();

        if (rsmd.getColumnCount() == 0) {
            return new ColumnSetImpl(); // if there is no custom field then set Columnset null and next() false
        }
        String query = qh.getCCDataQuery(referenceId, rsmd);
        PreparedStatement ps = conn.prepareStatement(query);
        try {
            ResultSet rs = ps.executeQuery();
            //i is counter for columnno which is name of second table
            if (rs.next()) {
                int c = rsmd.getColumnCount();
                for (int i = 0; i < c; i++) {
                    Column col = rsmd.getColumn(i);
                    String cno = col.getNo();
                    if (CcHelper.isDouble(cno)) {   // for double
                        records.put(col, rs.getString(col.getNo()));
                    } else if (CcHelper.isBoolean(cno)) {  // for boolean value
                        records.put(col, rs.getBoolean(col.getNo()));
                    } else if (CcHelper.isDate(cno)) { //for date field
                        records.put(col, rs.getLong(col.getNo()));
                    } else if (CcHelper.isString(cno)) {
                        records.put(col, rs.getString(col.getNo()));
                    }
                }
            }
            rec = new ColumnSetImpl(records);
        } catch (SQLException ex) {
            throw ServiceException.FAILURE("CustomColumn.getColumnsData()", ex);
        } finally {
            try {
                ps.close();
            } catch (SQLException ex) {
                Logger.getLogger(CustomColumnImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return rec;
    }

    @Override
    public int insertColumnsData(Connection conn, Map<String, String> params, String module, String referenceId) throws ServiceException {
        StringBuilder query = new StringBuilder("INSERT INTO ccdata(referenceid,module");
        StringBuilder valuesString = new StringBuilder(" values(?,?");
        Object[] o = new Object[params.size() + 2];
        o[0] = referenceId;
        o[1] = module;
        Set<String> keys = params.keySet();
        Iterator it = keys.iterator();
        int i = 2;
        try {
            while (it.hasNext()) {
                String _k = it.next().toString();
                query.append(",").append(_k);
                valuesString.append(",?");
                o[i] = CcHelper.getParamsValue(_k, params.get(_k));
                i++;
            }
            query.append(")");
            valuesString.append(")");
            query.append(valuesString);
            return DbUtil.executeUpdate(conn, query.toString(), o);
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("CustomColumn.insertColumnsData()", ex);
        }
    }

    @Override
    public void editColumnsData(Connection conn, Map<String, String> params, String referenceId) throws ServiceException {
        if (params.size() > 0) {
            StringBuilder query = new StringBuilder("update ccdata set ");
            Object[] o = new Object[params.size() + 1];
            Set<String> keys = params.keySet();
            Iterator it = keys.iterator();
            int i = 0;
            try {
                while (it.hasNext()) {
                    String _k = it.next().toString();
                    query.append(_k).append("= ? ,");
                    o[i++] = CcHelper.getParamsValue(_k, params.get(_k));
                }
                query.deleteCharAt(query.lastIndexOf(",")).append(" where referenceid = ?");
                o[i] = referenceId;
                DbUtil.executeUpdate(conn, query.toString(), o);
            } catch (ServiceException ex) {
                throw ServiceException.FAILURE("customColumn.editColumnsData()", ex);
            }
        }
    }

    @Override
    public void deleteColumnsData(Connection conn, String referenceId) throws ServiceException {
        try {
            DbUtil.executeUpdate(conn, "delete from ccdata where referenceid= ? ", referenceId);
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("CustomColumn.deleteColumnsData()", ex);
        }
    }

    private ColumnsMetaData getColumnsMetaData(String cid) throws ServiceException {
        ColumnsMDImpl csmd = null;
        try {
            csmd = new ColumnsMDImpl();
            String query = "select * from ccmetadata where companyid= ? ";
            DbResults rs = DbUtil.executeQuery(query, cid);
            while (rs.next()) {
                Column c = CcHelper.getObject(rs);
                csmd.addColumn(c);
            }
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("CustomColumn.getColumnsMetaData()", ex);
        }
        return csmd;
    }

    @Override
    public List getReferences(Connection conn, String module, String ss) throws ServiceException {
        String x[] = rsmd.getColumnNos();
//        String query = qh.getSearchQuery(module, x, ss);
        List<String> _rf = new ArrayList<String>();
//        DbResults rs = DbUtil.executeQuery(conn, query);
//        while (rs.next()) {
//            _rf.add(rs.getString("referenceid"));
//        }
        return _rf;
    }
}
