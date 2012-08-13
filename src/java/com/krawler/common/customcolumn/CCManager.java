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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *This class is used to get the reference of CustomColumn and get the instance of Column.
 * In order to use class user need to create a pojo for Column (meta data of custom column) and need to implement Column interface.
 * following are the steps to use custom column
 * register your custom column class.( this is optional, CCManager will register default Column)
 * If user has own Column class then
 * get the reference of CustomColumn.
 * create instance of Column class and assign it to Column reference
 * set the values using setter method.
 * now user can use Customcolumn reference for database operation.
 *
 *
 * if user does not register Column then
 * get the instance of Column using getColumnInstance..
 * set the values
 * using CustomColumn reference user can database operation.
 * @author Kamlesh Kumar Sah
 * 
 */
public class CCManager {

    private static Class cn = null;
    static Map<String, CustomColumn> _cc = new HashMap<String, CustomColumn>();

    /**
     * This methos is used to get the reference of CustomColumn interface for db operation.
     * @param companyId
     * @return object of CustomColumn
     */
    public static CustomColumn getCustomColumn(String companyId) throws ServiceException {
        if (_cc.containsKey(companyId)) {
            return _cc.get(companyId);
        } else {
            return new CustomColumnImpl(companyId);
        }
    }

    /**
     * User can register own Column class for meta data by implementing Column interface.
     * after register user can get the instance of Column object using getColumnInstance method.
     * @param fullClassname full qualified class name.
     */
    public static void registerColumn(String fullClassname) {
        try {
            cn = Class.forName(fullClassname);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(CCManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * this method is used to register user's own Column class for meta data and get the instance of CustomColumn.
     * @param companyId
     * @param classname
     * @return
     */
    public static CustomColumn getCustomColumn(String companyId, String classname) throws ServiceException {
        registerColumn(classname);
        return new CustomColumnImpl(companyId, cn);
    }

    /**
     * if user does not implements the Column interface for meta data, user can get object of Column for meta data.
     * @return
     */
    public static Column getColumnInstance() {
        Column c = null;
        try {
            if (cn == null) {
                c = new CColumn();
            } else {
                c = (Column) cn.newInstance();
            }
        } catch (InstantiationException ex) {
            Logger.getLogger(CCManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(CCManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return c;
    }

    public static void closeCompanyCustomColumn(String companyId) {
        if (_cc.containsKey(companyId)) {
            _cc.remove(companyId);
        }
    }
}
