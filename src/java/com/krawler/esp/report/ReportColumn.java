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
package com.krawler.esp.report;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

/**
 *
 * @author kamlesh
 */
public class ReportColumn implements Comparable<ReportColumn> {

    private String columnID;
    private String reportID, header, displayHeader, name, dataIndex, tableName, fieldName, summary, module, renderer, headerkey;
    private boolean quickSearch, isMandatory = false;
    private int type, displayOrder;
    public ReportColumn() {
    }

    public ReportColumn(String columnID, String reportID, String header, String name, String dataIndex, String tablename, String fieldname, String summary) {
        this.columnID = columnID;
        this.reportID = reportID;
        this.header = header;
        this.name = name;
        this.dataIndex = dataIndex;
        this.tableName = tablename;
        this.fieldName = fieldname;
        this.summary = summary;
    }

    @Override
    public String toString() {
        return new JSONSerializer().exclude("*.class").deepSerialize(this);
    }

    public ReportColumn JSONtoObject(String serializedJSONString) {
        return new JSONDeserializer<ReportColumn>().use(null, ReportColumn.class).deserialize(serializedJSONString);
    }

    public String getHeaderkey() {
        return headerkey;
    }

    public void setHeaderkey(String headerkey) {
        this.headerkey = headerkey;
    }

    public String getColumnID() {
        return columnID;
    }

    public void setColumnID(String columnID) {
        this.columnID = columnID;
    }

    public String getDataIndex() {
        return dataIndex;
    }

    public void setDataIndex(String dataIndex) {
        this.dataIndex = dataIndex;
    }

    public String getDisplayHeader() {
        return displayHeader;
    }

    public void setDisplayHeader(String displayHeader) {
        this.displayHeader = displayHeader;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public boolean isIsMandatory() {
        return isMandatory;
    }

    public void setIsMandatory(boolean isMandatory) {
        this.isMandatory = isMandatory;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isQuickSearch() {
        return quickSearch;
    }

    public void setQuickSearch(boolean quickSearch) {
        this.quickSearch = quickSearch;
    }

    public String getRenderer() {
        return renderer;
    }

    public void setRenderer(String renderer) {
        this.renderer = renderer;
    }

    public String getReportID() {
        return reportID;
    }

    public void setReportID(String reportID) {
        this.reportID = reportID;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int compareTo(ReportColumn o) {
        return this.displayOrder - o.displayOrder;
    }
}
