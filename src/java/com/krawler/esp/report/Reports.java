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
import java.util.Date;

/**
 *
 * @author Kamlesh
 */
public class Reports {

    String reportID, companyID, reportName, type, description, createdBy, category;
    Date createdON, modifiedON;
    private boolean isPaging, isMilestone;

    public Reports() {
    }

    public Reports(String reportId, String companyId, String reportName, String type, String description, String createdby, Date createdOn, Date modifiedOn) {
        this.reportID = reportId;
        this.companyID = companyId;
        this.reportName = reportName;
        this.type = type;
        this.description = description;
        this.createdBy = createdby;
        this.createdON = new Date(createdOn.getTime());
        this.modifiedON = new Date(modifiedOn.getTime());
    }

    @Override
    public String toString() {
        return new JSONSerializer().exclude("*.class").deepSerialize(this);
    }

    public Reports JSONtoObject(String serializedJSONString) {
        return new JSONDeserializer<Reports>().use(null, Reports.class).deserialize(serializedJSONString);
    }

    public String getCompanyID() {
        return companyID;
    }

    public void setCompanyID(String companyId) {
        this.companyID = companyId;
    }

    public Date getCreatedON() {
        return createdON;
    }

    public void setCreatedON(Date createdOn) {
        this.createdON = new Date(createdOn.getTime());
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdby) {
        this.createdBy = createdby;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getModifiedON() {
        return modifiedON;
    }

    public void setModifiedON(Date modifiedOn) {
        this.modifiedON = new Date(modifiedOn.getTime());
    }

    public String getReportID() {
        return reportID;
    }

    public void setReportID(String reportId) {
        this.reportID = reportId;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isMilestone() {
        return isMilestone;
    }

    public void setMilestone(boolean isMilestone) {
        this.isMilestone = isMilestone;
    }

    public boolean isPaging() {
        return isPaging;
    }

    public void setPaging(boolean isPaging) {
        this.isPaging = isPaging;
    }
    
}
