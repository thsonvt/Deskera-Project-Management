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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author kamlesh kumar sah
 */
class ColumnsMDImpl implements ColumnsMetaData {

    List<Column> columns;

    ColumnsMDImpl(List<Column> columns) {
        this.columns = columns;
    }

    public ColumnsMDImpl() {
        columns = new ArrayList<Column>();
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public List<Column> getAllColumn() {
        return columns;
    }

    @Override
    public Column getColumn(int index) {
        return columns.get(index);
    }

    @Override
    public String getColumnName(int index) {
        return columns.get(index).getName();
    }

    @Override
    public int getColumnType(int index) {
        return columns.get(index).getType();
    }

    @Override
    public boolean isColumnNull(int index) {
        return columns.get(index).isMandatory();
    }

    @Override
    public String getColumnId(int index) {
        return columns.get(index).getColumnId();
    }

    @Override
    public String getColumnHeader(int index) {
        return columns.get(index).getHeader();
    }

    @Override
    public String getColumnModule(int index) {
        return columns.get(index).getModule();
    }

    public void addColumn(Column c) {
        columns.add(c);
    }

    @Override
    public String getCoulmnNo(int index) {
        return columns.get(index).getNo();
    }

    @Override
    public String[] getColumnNos() {
        String c[] = new String[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            c[i] = columns.get(i).getNo();
        }
        return c;
    }
     public String[] getColumnNos(String prefix) {
          String s="";
          int size = columns.size();
          for (int i = 0; i < size; i++) {
              if(columns.get(i).getNo().indexOf(prefix)>-1)
                   s+=columns.get(i).getNo()+",";
          }
          if(!s.isEmpty())
              return s.substring(0,s.length()-1).split(",");
        return new String[0];
    }

    @Override
    public List<Object> getColumnNosList() {
        String c[] = getColumnNos();
        List l = Arrays.asList(c);
        return l;
    }

    @Override
    public Column getColumnById(String id) {
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).getColumnId().equals(id)) {
                return columns.get(i);
            }
        }
        return null;
    }
}
