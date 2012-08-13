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

/**
 *
 * @author Abhay Kulkarni
 */
public class TaskCheckListMapping {

    private String mappingID;
    private String taskID;
    private String checkListID;
    private String mappedBy;

    public TaskCheckListMapping() {
    }

    @Override
    public String toString() {
        return new JSONSerializer().exclude("*.class").deepSerialize(this);
    }

    public TaskCheckListMapping JSONtoObject(String serializedJSONString) {
        return new JSONDeserializer<TaskCheckListMapping>().use(null, TaskCheckListMapping.class).deserialize(serializedJSONString);
    }

    public String getMappedBy() {
        return mappedBy;
    }

    public void setMappedBy(String mappedBy) {
        this.mappedBy = mappedBy;
    }

    public String getCheckListID() {
        return checkListID;
    }

    public void setCheckListID(String checkListID) {
        this.checkListID = checkListID;
    }

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String cTaskID) {
        this.taskID = cTaskID;
    }

    public String getMappingID() {
        return mappingID;
    }

    public void setMappingID(String mappingID) {
        this.mappingID = mappingID;
    }
}
