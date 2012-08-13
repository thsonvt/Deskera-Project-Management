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
package com.krawler.esp.project.checklist;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Abhay Kulkarni
 */
public class CheckList {

    private String checkListID;
    private String checkListName;
    private String checkListDescription;
    private String companyID;
    private Date createdOn;
    private Date updatedOn;
    private String createdBy;
    private String updatedBy;
    private List<CheckListTask> tasks;

    public CheckList() {
    }

    @Override
    public String toString() {
        return new JSONSerializer().exclude("*.class").deepSerialize(this);
    }

    public CheckList JSONtoObject(String serializedJSONString) {
        return new JSONDeserializer<CheckList>().use(null, CheckList.class).deserialize(serializedJSONString);
    }

    public List<CheckListTask> getTasks() {
        return tasks;
    }

    public void setTasks(List<CheckListTask> tasks) {
        this.tasks = tasks;
    }

    public CheckListTask getTask(String cTaskID) {
        CheckListTask task = null;
        if (tasks.isEmpty()) {
            return task;
        } else {
            for (CheckListTask t : tasks) {
                if (t.getcTaskID().equals(cTaskID)) {
                    task = t;
                }
            }
        }
        return task;
    }

    public String getCompanyID() {
        return companyID;
    }

    public void setCompanyID(String companyID) {
        this.companyID = companyID;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getCreatedOn(String format) throws ParseException {
        DateFormat _sd = new SimpleDateFormat(format);
        return _sd.format(createdOn);
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreatedOn(String createdOn, String format) throws ParseException {
        DateFormat _sd = new SimpleDateFormat(format);
        this.createdOn = new Date(_sd.parse(createdOn).getTime());
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public String getUpdatedOn(String format) throws ParseException {
        DateFormat _sd = new SimpleDateFormat(format);
        return _sd.format(updatedOn);
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    public void setUpdatedOn(String updatedOn, String format) throws ParseException {
        DateFormat _sd = new SimpleDateFormat(format);
        this.updatedOn = new Date(_sd.parse(updatedOn).getTime());
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public String getCheckListDescription() {
        return checkListDescription;
    }

    public void setCheckListDescription(String checkListDescription) {
        this.checkListDescription = checkListDescription;
    }

    public String getCheckListName() {
        return checkListName;
    }

    public void setCheckListName(String checkListName) {
        this.checkListName = checkListName;
    }

    public String getCheckListID() {
        return checkListID;
    }

    public void setCheckListID(String checkListID) {
        this.checkListID = checkListID;
    }
}
