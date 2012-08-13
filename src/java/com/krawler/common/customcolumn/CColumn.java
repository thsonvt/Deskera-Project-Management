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

import com.krawler.common.util.StringUtil;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import flexjson.transformer.DateTransformer;
import java.util.Date;

/**
 * This class implements all method of column.
 * Using column interface user can get all attribute of custom column
 * property of this classess are visible from outside
 * @author Kamlesh Kumar Sah
 */
public class CColumn implements Column {

    private String columnId, module, name, header, dataIndex, defaultValue, renderer, editor, creator, no;
    private boolean mandatory, visible;
    private Date createdDate, modifiedDate;
    private int type;
    private boolean enabled;
    String[] masterdata;

    @Override
    public String getColumnId() {
        return columnId;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public CColumn(String id, String module, String name, String header, String dataIndex, String defaultValue, boolean enabled, String renderer, String editor, int type, String no, String creator, boolean mandatory, boolean visible, Date createdDate, Date modifiedDate) {
        this.columnId = id;
        this.module = module;
        this.name = name;
        this.header = header;
        this.dataIndex = dataIndex;
        this.defaultValue = defaultValue;
        this.enabled = enabled;
        this.renderer = renderer;
        this.editor = editor;
        this.type = type;
        this.no = no;
        this.creator = creator;
        this.mandatory = mandatory;
        this.visible = visible;
        this.createdDate = new Date(createdDate.getTime());
        this.modifiedDate = new Date(modifiedDate.getTime());
    }

    public CColumn(String id, String module, String name, String header, int type, String no, String creator) {
        this.columnId = id;
        this.module = module;
        this.name = name;
        this.header = header;
        this.type = type;
        this.no = no;
        this.creator = creator;
    }

    public CColumn() {
    }

    @Override
    public String toString() {
        return new JSONSerializer().exclude("*.class").exclude("masterstring").transform(new DateTransformer("yyyy-MM-dd"), new String[]{"createdDate", "modifiedDate"}).deepSerialize(this);
    }

    @Override
    public CColumn JSONtoObject(String serializedJSONString) {
        return new JSONDeserializer<CColumn>().use(null, CColumn.class).deserialize(serializedJSONString);
    }

    @Override
    public Date getCreatedDate() {
        return createdDate;
    }

    @Override
    public String getCreator() {
        return creator;
    }

    @Override
    public String getDataIndex() {
        return dataIndex;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String getEditor() {
        return editor;
    }

    @Override
    public String getHeader() {
        return header;
    }

    @Override
    public boolean isMandatory() {
        return mandatory;
    }

    @Override
    public Date getModifiedDate() {
        return modifiedDate;
    }

    @Override
    public String getModule() {
        return module;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getNo() {
        return no;
    }

    @Override
    public String getRenderer() {
        return renderer;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    @Override
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public void setCreator(String creator) {
        this.creator = creator;
    }

    @Override
    public void setDataIndex(String dataIndex) {
        this.dataIndex = dataIndex;
    }

    @Override
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public void setEditor(String editor) {
        this.editor = editor;
    }

    @Override
    public void setHeader(String header) {
        this.header = header;
    }

    @Override
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    @Override
    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    @Override
    public void setModule(String module) {
        this.module = module;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setNo(String no) {
        this.no = no;
    }

    @Override
    public void setRenderer(String renderer) {
        this.renderer = renderer;
    }

    @Override
    public void setType(int type) {
        this.type = type;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public String[] getMasterdata() {
        return masterdata;
    }

    @Override
    public void setMasterdata(String[] masterdata) {
        this.masterdata = masterdata;
    }

    @Override
    public void setFields() {

        this.name = this.header.replaceAll(" ", "").toLowerCase();
        this.dataIndex = this.name;
        String[] _t = this.header.split(" ");
        for (int i = 0; i < _t.length; i++) {
            _t[i] = Character.toUpperCase(_t[i].charAt(0)) + _t[i].substring(1);
        }
        this.header = StringUtil.join(" ", _t);
        switch (type) {
            case CustomColumn.DOUBLE:
                this.renderer = "decimal";
                break;
            case CustomColumn.BOOLEAN:
                this.renderer = "booleanfield";
                break;
            case CustomColumn.DATE:
                this.renderer = "date";
                break;
            case CustomColumn.TEXT:
//combo
            case 4:
//textfield
            case 5:
//richtext
            case 6:
//textarea
            case 7:
                //multi select combo
                this.renderer = "textfield";
                break;
            case 8:
                this.renderer = "file";
                break;
        }


    }

    /* delegates methods*/
    @Override
    public void setMasterString(String data) {
        this.masterdata = data.split(";");
    }

    @Override
    public String getMasterString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < masterdata.length; i++) {
            s.append(masterdata[i]).append(";");
        }
        if (s.length() > 1) {
            return s.substring(0, s.lastIndexOf(";")).toString();
        } else {
            return "";
        }
    }
}
