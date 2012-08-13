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
import com.krawler.database.DbPool.Connection;
import com.krawler.esp.utils.ConfigReader;
import java.util.List;
import java.util.Map;

/**
 *This interface is used to select, insert, edit and update in column's meta data and value
 * User can get the reference of CustomColumn using CCManager class.
 * user can register users's own Column class by implementing Column interface(optional).
 *
 * @author Kamlesh Kumar Sah
 */
public interface CustomColumn {

    int TEXT = 3, DOUBLE = 0, BOOLEAN = 1, DATE = 2;
    static final int MAX_NO_OF_TYPE = 10; // max number of field of each type in data base
    static final int MAX_NO_OF_NUM = ConfigReader.getinstance().getInt("max_cc_num_field",MAX_NO_OF_TYPE);
    static final int MAX_NO_OF_BOOL =ConfigReader.getinstance().getInt("max_cc_bool_field",MAX_NO_OF_TYPE) ;
    static final int MAX_NO_OF_DATE =ConfigReader.getinstance().getInt("max_cc_date_field",MAX_NO_OF_TYPE) ;
    static final int MAX_NO_OF_TEXT =ConfigReader.getinstance().getInt("max_cc_text_field",20) ;

    /**
     * This method is used to get the value of all column with ColumnMetaData object.
     * using recorset interface we can access value and metadata of all column simillar to resultset interface of java.sql
     * @param conn
     * @return set of all column object which contains all columns.
     * @throws ServiceException
     */
    public ColumnSet getColumnsData(Connection conn, String module, String referenceId) throws ServiceException;

    /**
     * This method is used to get meta data of all custom column.
     * @param conn
     * @return object of ColumnSet.
     * @throws ServiceException
     */
    ColumnsMetaData getColumnsMetaData(Connection conn) throws ServiceException;

    /**
     * 
     * @param conn
     * @param module
     * @return
     */
    ColumnsMetaData getColumnsMetaData(Connection conn, String module) throws ServiceException;

    /**
     * this methood is used to insert a column object(meta data of a column).
     * @param conn Connection object
     * @param column column object to be inserted
     * @return true/false
     */
    boolean insertColumn(Connection conn, Column column) throws ServiceException;

    /**
     * This method is used to delete custom column.
     * @param conn
     * @param module
     * @param columnId
     * @return
     */
    boolean deleteColumn(Connection conn, String columnId) throws ServiceException;

    /**
     * This method is used to edit a custom column
     * @param conn
     * @param column
     * @return
     */
    public boolean editColumn(Connection conn, Column column) throws ServiceException;

    /**
     * this method is simillar to executeupdate of resultset
     * @param conn
     * @param params is parameter name and value for the parameter
     * @param projectid
     * @return
     */
    public int insertColumnsData(Connection conn, Map<String, String> params, String module, String referenceId) throws ServiceException;

    public void deleteColumnsData(Connection conn, String string) throws ServiceException;

    public void editColumnsData(Connection conn, Map<String, String> params, String referenceId) throws ServiceException;

    public List getReferences(Connection conn, String string, String ss) throws ServiceException;
}
