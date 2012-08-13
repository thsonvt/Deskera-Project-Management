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

import com.krawler.common.customcolumn.CustomColumn;
import com.krawler.common.util.Constants;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 *
 * @author kamlesh
 */
public class QueryFilter {

    String operator, tableName, fieldName, condition, value;
    int type;

    public QueryFilter() {
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getFilterQuery() throws ParseException {
        StringBuilder query = new StringBuilder();
        String valueFormatter = "", LIKE = "";
        query.append(operator + " ");
        if (type == CustomColumn.DATE) {
            valueFormatter = "'";
            if (tableName.equalsIgnoreCase("ccdata")) {
                DateFormat df = new SimpleDateFormat(Constants.ONLY_DATE_FORMAT);
                try{
                    Long.parseLong(value);
                } catch(NumberFormatException e){ // if value is already in long format. 
                    long l = df.parse(value).getTime();
                    value = Long.valueOf(l).toString();
                }
                valueFormatter = "";
                query.append(tableName + "." + fieldName);
            } else {
                query.append("DATE_FORMAT(" + tableName + "." + fieldName + ",'%Y-%m-%d')");
            }

        } else {
            query.append(tableName + "." + fieldName);
            valueFormatter = "'";
        }
        if (type >= CustomColumn.TEXT) {
            valueFormatter = "'";
            if (condition.contains("LIKE")) {
                LIKE = "%";
            }
        }
        query.append(" " + condition + " ").append(valueFormatter + LIKE + value + LIKE + valueFormatter);
        return query.toString();
    }
}
