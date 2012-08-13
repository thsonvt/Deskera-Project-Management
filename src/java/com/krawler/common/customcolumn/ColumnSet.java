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

/**
 *This interface is used to access value from custom column
 * @author kamlesh kumar sah
 */
public interface ColumnSet {

    /**
     * used to get the property of column such as dataIndex, name, defaultValue etc...
     * @param columnName
     * @return
     */
    Object get(String columnName);

    ColumnsMetaData getRecordSetMetaData();

    /**
     * get the value of custom column
     * @param col
     * @return
     */
    Object getObject(Column col);

    public boolean next();

    /**
     * get the column object
     * @return
     */
    public Column getColumn();

    /**
     * get the value of column
     * @return
     */
    public Object getObject();

    /**
     * This method get the dataIndex of the column 
     * @return
     */
    public String getDataIndex();
}
