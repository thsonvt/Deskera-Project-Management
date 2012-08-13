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

import com.krawler.database.DbResults;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Kamlesh
 */
class CcHelper {

    public static Column getObject(DbResults rs) {
        Column c = CCManager.getColumnInstance();
        c.setColumnId(rs.getString("columnid"));
        if (rs.has("module")) {
            c.setModule(rs.getString("module"));
        }
        if (rs.has("name")) {
            c.setName(rs.getString("name"));
        }
        if (rs.has("header")) {
            c.setHeader(rs.getString("header"));
        }
        if (rs.has("type")) {
            c.setType(rs.getInt("type"));
        }
        if (rs.has("columnno")) {
            c.setNo(rs.getString("columnno"));
        }
        if (rs.has("userid")) {
            c.setCreator(rs.getString("userid"));
        }
        if (rs.has("enabled")) {
            c.setEnabled(rs.getBoolean("enabled"));
        }
        if (rs.has("visible")) {
            c.setVisible(rs.getBoolean("visible"));
        }
        if (rs.has("ismandatory")) {
            c.setMandatory(rs.getBoolean("ismandatory"));
        }
        if (rs.has("masterdata")) {
            c.setMasterString(rs.getString("masterdata"));
        }
        if (rs.has("dataindex")) {
            c.setDataIndex(rs.getString("dataindex"));
        }
        if (rs.has("createdon")) {
            c.setCreatedDate(rs.getDate("createdon"));
        }
        if (rs.has("modifiedon")) {
            c.setModifiedDate(rs.getDate("modifiedon"));
        }
        if (rs.has("renderer")) {
            c.setRenderer(rs.getString("renderer"));
        }
        if (rs.has("editor")) {
            c.setEditor(rs.getString("editor"));
        }
        if (rs.has("defaultvalue")) {
            c.setDefaultValue(rs.getString("defaultvalue"));
        }
        return c;
    }

    /**
     * @param _k is columnno (c1,c2 etc)
     * @param o is the value
     * @return
     */
    public static Object getParamsValue(String _k, Object o) {
        if (o == null || isString(_k)) {
            return o;
        }
        String _s = o.toString().trim(); //if o is not null or it is not an string
        if (isDouble(_k)) { // case for number/double
            if (_s.isEmpty()) {
                return null;
            } else {
                return _s;
            }
        }
        if (isBoolean(_k)) {
            return _s.equals("on");
        }
        if (isDate(_k)) {  //case for date field in long
            if (_s.isEmpty()) {
                return null;
            } else {
                try {
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    Date d = df.parse(o.toString());
                    return d.getTime();
                } catch (ParseException ex) {
                    Logger.getLogger(CcHelper.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return o;
    }

    public static String getMethodName(String type, String fieldName) {
        return type + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    /**
     * get columnnos in format c1,c2....   for specific range. range is used for specific column type
     * @param colno
     * @param start
     * @param end
     * @return
     */
    public static String[] getFormatedColumnNo(int[] colno) {
        int _l = colno.length;
        String _s[] = new String[_l];
        for (int i = 0; i < _l; i++) {
                _s[i] = "c" + colno[i];
        }
        return _s;
    }
    public static int[] getColumnNos(String colnos[]){
        int l = colnos.length;
        int c[] = new int[l];
        for(int i=0; i<l; i++)
            c[i] = Integer.parseInt(colnos[i].substring(2));
        return c;
    }
   
    public static String getPrefix(int type) {
        String _ps[] = {"cd", "cb", "ct", "cs", "cs", "cs", "cs", "cs"};
        return _ps[type];
    }

    static int getColumnNo(String cno) {
        return Integer.parseInt(cno.substring(2));
    }

    static boolean isDouble(String cno) {
        return cno.charAt(1)=='d';
    }

    static boolean isBoolean(String cno) {
        return cno.charAt(1) == 'b';
    }

    static boolean isDate(String cno) {
        return cno.charAt(1) == 't';
    }

    static boolean isString(String cno) {
        return cno.charAt(1) == 's';
    }

    static int getColumnLimit(String prefix) {
        if(isBoolean(prefix))
             return CustomColumn.MAX_NO_OF_BOOL;
        else if(isDouble(prefix))
            return CustomColumn.MAX_NO_OF_NUM;
        else if(isDate(prefix))
            return CustomColumn.MAX_NO_OF_DATE;
        else if(isString(prefix))
            return CustomColumn.MAX_NO_OF_TEXT;
        else
            return CustomColumn.MAX_NO_OF_TYPE;
         
    }

    static void sort(String[] colnos) {
        int x[] = getColumnNos(colnos);
        Arrays.sort(x);
        colnos = getFormatedColumnNo(x);
    }
}
