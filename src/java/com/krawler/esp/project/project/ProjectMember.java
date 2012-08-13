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

import com.krawler.esp.user.User;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

/**
 *
 * @author Abhay
 */
public class ProjectMember {

    Project project;
    User user;
    int status;
    int planPermission;
    boolean quickLink;
    boolean inUseFlag;
    int notificationSubscription;
    boolean milestoneTimelineSelection;
    ProjectMemberStatus memberStatus;
    ProjectMemberPlanPermission memberPermission;

    public ProjectMember(Project project, User user, int status, int planPermission, boolean quickLink, boolean inUseFlag, int notificationSubscription, boolean milestoneTimelineSelection) {
        this.project = project;
        this.user = user;
        this.status = status;
        this.planPermission = planPermission;
        this.quickLink = quickLink;
        this.inUseFlag = inUseFlag;
        this.notificationSubscription = notificationSubscription;
        this.milestoneTimelineSelection = milestoneTimelineSelection;
    }

    public ProjectMember() {
        this.project = null;
        this.user = null;
        this.status = 0;
        this.planPermission = 0;
        this.quickLink = false;
        this.inUseFlag = false;
        this.notificationSubscription = 0;
        this.milestoneTimelineSelection = false;
    }

    @Override
    public String toString() {
        return new JSONSerializer().exclude("*.class").deepSerialize(this);
    }

    public Project JSONtoObject(String serializedJSONString) {
        return new JSONDeserializer<Project>().use(null, ProjectMember.class).deserialize(serializedJSONString);
    }

    public boolean isInUseFlag() {
        return inUseFlag;
    }

    public void setInUseFlag(boolean inUseFlag) {
        this.inUseFlag = inUseFlag;
    }

    public boolean isMilestoneTimelineSelection() {
        return milestoneTimelineSelection;
    }

    public void setMilestoneTimelineSelection(boolean milestoneTimelineSelection) {
        this.milestoneTimelineSelection = milestoneTimelineSelection;
    }

    public int getNotificationSubscription() {
        return notificationSubscription;
    }

    public void setNotificationSubscription(int notificationSubscription) {
        this.notificationSubscription = notificationSubscription;
    }

    public int getPlanPermission() {
        return planPermission;
    }

    public void setPlanPermission(int planPermission) {
        this.planPermission = planPermission;
        memberPermission = ProjectMemberPlanPermission.get(planPermission);
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public boolean isQuickLink() {
        return quickLink;
    }

    public void setQuickLink(boolean quickLink) {
        this.quickLink = quickLink;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
        memberStatus = ProjectMemberStatus.get(status);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
}
