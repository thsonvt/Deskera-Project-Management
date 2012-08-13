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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kamlesh kumar sah
 */
public class ColumnSetImpl implements ColumnSet {

    private Map<Column, Object> data = new HashMap<Column, Object>();
    private Column colIndex[];
    int rowNum = -1;
    int size = 0;

    ColumnSetImpl(Map<Column, Object> records) {
        this.data = records;
        size = data.size();
        colIndex = new CColumn[size];
        colIndex = data.keySet().toArray(colIndex);
    }

    ColumnSetImpl() {
    }

    @Override
    public Object get(String columnName) {
        Object o = null;
        try {
            Method m = colIndex[rowNum].getClass().getMethod(columnName, new Class[]{});
            o = m.invoke(colIndex[rowNum], new Object[]{});
        } catch (IllegalAccessException ex) {
            Logger.getLogger(ColumnSetImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ColumnSetImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(ColumnSetImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(ColumnSetImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(ColumnSetImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return o;
    }

    @Override
    public boolean next() {
        rowNum++;
        if (rowNum < size) {
            return true;
        }
        return false;
    }

    @Override
    public Object getObject(Column col) {
        return data.get(col);
    }

    @Override
    public Object getObject() {
        return data.get(colIndex[rowNum]);
    }

    @Override
    public Column getColumn() {
        return colIndex[rowNum];

    }

    @Override
    public String getDataIndex() {
        return colIndex[rowNum].getDataIndex();
    }

    @Override
    public ColumnsMetaData getRecordSetMetaData() {
        return new ColumnsMDImpl(Arrays.asList(colIndex));
    }
}
