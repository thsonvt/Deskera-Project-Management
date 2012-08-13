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

import java.util.Date;

/**
 * This class implements all method of column.
 * Using column interface user can get all attribute of custom column
 * property of this classess are visible from outside
 * @author Kamlesh Kumar Sah
 */
public interface Column {

    /**
     * This method is used to Columnid for Column object
     * @return columnId
     */
    String getColumnId();

    /**
     * if this column is visible then this method will return true otherwise false
     * @return true/false
     */
    boolean isVisible();

    /**
     * This method returns true if column is enabled and user can chage the value of column on form.
     * @return
     */
    boolean isEnabled();

    /**
     * This method sets the value for enabled field.
     * @param enabled
     */
    void setEnabled(boolean enabled);

    /**
     * This method returns all meta data of column in json format.
     * @return
     */
    @Override
    String toString();

    /**
     *
     * @param serializedJSONString
     * @return
     */
    public CColumn JSONtoObject(String serializedJSONString);

    /**
     * get the value for createddate for column
     * @return
     */
    public Date getCreatedDate();

    /**
     * used to set the value for field createddate
     * @return
     */
    public String getCreator();

    /**
     *
     * @return
     */
    public String getDataIndex();

    /**
     *
     * @return
     */
    public String getDefaultValue();

    /**
     * get the default value for the column
     * @return
     */
    public String getEditor();

    /**
     * get the editor used at clien side to use in grid.
     * @return
     */
    public String getHeader();

    /**
     * get the value for header
     * @return
     */
    public boolean isMandatory();

    /**
     * get the value for modified date.
     * @return
     */
    public Date getModifiedDate();

    /**
     * get the value for modifieddate
     * @return
     */
    public String getModule();

    /**
     * get the field value of module
     * @return
     */
    public String getName();

    /**
     * get the value for name field
     * @return
     */
    public String getNo();

    /**
     * get the value for no field
     * the no field is the column number of the company.
     * this no is used to store the value for custom column
     * the columns are represented as c1,c2,c3 and so on.......
     *
     * @return
     */
    public String getRenderer();

    public int getType();

    public void setColumnId(String columnId);

    public void setCreatedDate(Date createdDate);

    public void setCreator(String creator);

    public void setDataIndex(String dataIndex);

    public void setDefaultValue(String defaultValue);

    public void setEditor(String editor);

    public void setHeader(String header);

    public void setMandatory(boolean mandatory);

    public void setModifiedDate(Date modifiedDate);

    public void setModule(String module);

    public void setName(String name);

    public void setNo(String no);

    public void setRenderer(String renderer);

    public abstract void setType(int type);

    public void setVisible(boolean visible);

    public String[] getMasterdata();

    public void setMasterdata(String[] masterdata);

    public void setFields();
    /* delegates methods*/

    public void setMasterString(String data);

    public String getMasterString();
}
