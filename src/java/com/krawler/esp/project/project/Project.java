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
package com.krawler.esp.project.project;

import com.krawler.common.customcolumn.ColumnSet;
import com.krawler.esp.project.meter.HealthMeter;
import com.krawler.esp.project.resource.Resource;
import flexjson.JSON;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author kamlesh
 */
public class Project {

    private String image = "", projectID, projectName, description, companyID, nickName;
    private Date startDate, createdOn;
    private boolean archieved;
    private Resource resource;
    private HealthMeter meter;
    private int memberCount;
    ColumnSet recordSet;
    private int optimisticDiff, pessimisticDiff, pertStatus;
    private PERTDiffStatus pertDiffStatus;

    public Project() {
    }

    public Project(String image, String projectId, String companyId, Date createdOn, boolean archieved) {
        this.image = image;
        this.projectID = projectId;
        this.createdOn = new Date(createdOn.getTime());
        this.archieved = archieved;
        this.companyID = companyId;
        this.optimisticDiff = this.pessimisticDiff = 0;
        this.pertStatus = pertDiffStatus.COMPANY.getCode();
    }

    public Project(String projectId, String image, String projectName, String description, String companyId, String nickName, Date startDate, Date createdOn, boolean archieved, Resource resource) {
        this.projectID = projectId;
        this.projectName = projectName;
        this.description = description;
        this.companyID = companyId;
        this.nickName = nickName;
        this.startDate = new Date(startDate.getTime());
        this.createdOn = new Date(createdOn.getTime());
        this.archieved = archieved;
        this.resource = resource;
        this.image = image;
        this.optimisticDiff = this.pessimisticDiff = 0;
        this.pertStatus = pertDiffStatus.COMPANY.getCode();
    }

    @Override
    public String toString() {
        return new JSONSerializer().exclude("*.class").exclude("recordSet", "meter", "resource").deepSerialize(this);
    }

    public Project JSONtoObject(String serializedJSONString) {
        return new JSONDeserializer<Project>().use(null, Project.class).deserialize(serializedJSONString);
    }

    public boolean isArchieved() {
        return archieved;
    }

    public void setArchieved(boolean archieved) {
        this.archieved = archieved;
    }

    public Date getCreatedOn() {
        return new Date(createdOn.getTime());
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = new Date(createdOn.getTime());
    }

    public String getCreatedOn(String format) throws ParseException {
        DateFormat _sd = new SimpleDateFormat(format);
        return _sd.format(createdOn);
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = new Date(createdOn);
    }

    public void setCreatedOn(String createdOn, String format) throws ParseException {
        DateFormat _sd = new SimpleDateFormat(format);
        this.createdOn = new Date(_sd.parse(createdOn).getTime());
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public HealthMeter getMeter() {
        return meter;
    }

    public void setMeter(HealthMeter meter) {
        this.meter = meter;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Date getStartDate() {
        return new Date(startDate.getTime());
    }

    public void setStartDate(Date startDate) {
        this.startDate = new Date(startDate.getTime());
    }

    public String getStartDate(String format) throws ParseException {
        DateFormat _sd = new SimpleDateFormat(format);
        return _sd.format(startDate);
    }

    public void setStartDate(String startDate) {
        this.startDate = new Date(startDate);
    }

    public void setStartDate(String startDate, String format) throws ParseException {
        DateFormat _sd = new SimpleDateFormat(format);
        this.startDate = _sd.parse(startDate);
    }
    @JSON(include=false)
    public ColumnSet getRecordSet() {
        return recordSet;
    }

    public void setRecordSet(ColumnSet recordSet) {
        this.recordSet = recordSet;
    }
    public String getCompanyID() {
        return companyID;
    }

    public void setCompanyID(String companyID) {
        this.companyID = companyID;
    }

    public int getOptimisticDiff() {
        return optimisticDiff;
    }

    public void setOptimisticDiff(int optimisticDiff) {
        this.optimisticDiff = optimisticDiff;
    }

    public int getPessimisticDiff() {
        return pessimisticDiff;
    }

    public void setPessimisticDiff(int pessimisticDiff) {
        this.pessimisticDiff = pessimisticDiff;
    }

    public String getProjectID() {
        return projectID;
    }

    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }

    public int getPertStatus() {
        return pertStatus;
    }

    public void setPertStatus(int pertStatus) {
        this.pertStatus = pertStatus;
    }

    public PERTDiffStatus getPertDiffStatus() {
        return pertDiffStatus;
    }

    public void setPertDiffStatus(PERTDiffStatus pertDiffStatus) {
        this.pertDiffStatus = pertDiffStatus;
    }

    /**
     * Delegates method
     */
    public String getLinkString(int beginIndex, int endIndex) {
        if (projectName.length() > endIndex) {
            return projectName.substring(beginIndex, endIndex - 4) + "...";
        }
        return projectName;
    }
}
