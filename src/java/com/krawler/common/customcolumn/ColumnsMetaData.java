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

import java.util.List;

/**
 *
 * @author kamlesh kumar sah
 */
public interface ColumnsMetaData {

    int getColumnCount();

    List<Column> getAllColumn();

    Column getColumn(int index);

    Column getColumnById(String id);

    String getColumnName(int index);

    int getColumnType(int index);

    boolean isColumnNull(int index);

    String getColumnId(int index);

    String getColumnHeader(int index);

    String getColumnModule(int index);

    String getCoulmnNo(int index);
    String[] getColumnNos(String prefix);
    String[] getColumnNos();

    List<Object> getColumnNosList();
}
