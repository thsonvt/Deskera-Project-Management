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
package com.krawler.esp.project.task;

import com.krawler.common.util.SchedulingUtilities;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import flexjson.transformer.DateTransformer;
import java.util.Date;

/**
 *
 * @author Abhay
 */
public class Task {

    private String taskID;
    private String taskName;
    private int taskIndex;
    private String duration;
    private String actualDuration;
    private DurationType durationType;
    private Date startDate;
    private Date endDate;
    private Date actualStartDate;
    private int percentComplete;
    private String notes;
    private int priority;
    private int level;
    private String parent;
    private String projectID;
    private boolean isParent;
    private boolean isMilestone;

    public Task() {
        notes = "";
    }

    public Task(String taskID, String taskName, int taskIndex, String duration, String actualDuration, Date startDate, Date endDate, Date actualStartDate, int percentComplete, String notes, int priority, int level, String parent, String projectID, boolean isParent) {
        this.taskID = taskID;
        this.taskName = taskName;
        this.taskIndex = taskIndex;
        this.duration = duration;
        this.actualDuration = actualDuration;
        this.startDate = startDate != null ? new Date(startDate.getTime()) : null;
        this.endDate = endDate != null ? new Date(endDate.getTime()) : null;
        this.actualStartDate = actualStartDate != null ? new Date(actualStartDate.getTime()) : null;
        this.percentComplete = percentComplete;
        this.notes = notes;
        this.priority = priority;
        this.level = level;
        this.parent = parent;
        this.projectID = projectID;
        this.isParent = isParent;
        this.isMilestone = false;
    }

    public Task(String taskID, String taskName, int taskIndex, String duration, Date startDate, Date endDate, String projectID) {
        this.taskID = taskID;
        this.taskName = taskName;
        this.taskIndex = taskIndex;
        this.duration = duration;
        this.startDate = startDate != null ? new Date(startDate.getTime()) : null;
        this.endDate = endDate != null ? new Date(endDate.getTime()) : null;
        this.projectID = projectID;

        this.actualDuration = duration;
        this.actualStartDate = startDate != null ? new Date(startDate.getTime()) : null;
        this.percentComplete = 0;
        this.notes = "";
        this.priority = 0;
        this.level = 0;
        this.parent = "0";
        this.isParent = false;
        this.isMilestone = false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        final Task other = (Task) obj;
        if ((this.taskID == null) ? (other.taskID != null) : !this.taskID.equals(other.taskID)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (this.taskID != null ? this.taskID.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return new JSONSerializer().exclude("*.class")
            .transform(new DateTransformer("yyyy-MM-dd"), new String[] {"task.startDate", "task.endDate", "task.actualStartDate"})
            .deepSerialize(this);
    }

    public static Task JSONtoObject(String serializedJSONString) {
        return new JSONDeserializer<Task>().use(null, Task.class).deserialize(serializedJSONString);
    }

    public String getActualDuration() {
        return actualDuration;
    }

    public void setActualDuration(String actualDuration) {
        this.actualDuration = actualDuration;
    }

    public Date getActualStartDate() {
        return actualStartDate != null ? new Date(actualStartDate.getTime()) : null;
    }

    public void setActualStartDate(Date actualStartDate) {
        this.actualStartDate = actualStartDate != null ? new Date(actualStartDate.getTime()) : null;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
        double d = SchedulingUtilities.parseDuration(duration);
        if(d == 0)
            this.isMilestone = true;
        else
            this.isMilestone = false;
    }

    public Date getEndDate() {
        return endDate != null ? new Date(endDate.getTime()) : null;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate != null ? new Date(endDate.getTime()) : null;
    }

    public boolean isIsParent() {
        return isParent;
    }

    public void setIsParent(boolean isParent) {
        this.isParent = isParent;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public int getPercentComplete() {
        return percentComplete;
    }

    public void setPercentComplete(int percentComplete) {
        this.percentComplete = percentComplete;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getProjectID() {
        return projectID;
    }

    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }

    public Date getStartDate() {
        return startDate != null ? new Date(startDate.getTime()) : null;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate != null ? new Date(startDate.getTime()) : null;
    }

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public int getTaskIndex() {
        return taskIndex;
    }

    public void setTaskIndex(int taskIndex) {
        this.taskIndex = taskIndex;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public DurationType getDurationType() {
        DurationType dT = null;
        if (duration.contains("h")) {
            dT = DurationType.HOURS;
        } else if (duration.contains("d")) {
            dT = DurationType.DAYS;
        } else {
            dT = DurationType.DAYS;
        }
        return dT;
    }

    public void setDurationType(DurationType durationType) {
        this.durationType = durationType;
    }

    public void setDurationType(String duration) {
        setDuration(duration);
        this.durationType = getDurationType();
    }

    public boolean isIsMilestone() {
        return isMilestone;
    }

    public void setIsMilestone(boolean isMilestone) {
        this.isMilestone = isMilestone;
    }
}
