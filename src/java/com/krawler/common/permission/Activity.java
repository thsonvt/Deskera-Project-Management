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
package com.krawler.common.permission;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Vipin Gupta
 */
public class Activity implements Serializable {

    private int activityID;
    private String activityName;
    private String activityDisplayName;
    private int activityValue;
    private boolean subActivity;
    private boolean permission;
    private List<SubActivity> subActivities;

    public Activity(int activityID, String activityName, String activityDisplayName, int activityValue, boolean subActivity, boolean permission, List<SubActivity> subActivities) {
        this.activityID = activityID;
        this.activityName = activityName;
        this.activityDisplayName = activityDisplayName;
        this.activityValue = activityValue;
        this.subActivity = subActivity;
        this.permission = permission;
        this.subActivities = subActivities;
    }

    public Activity() {
        this(0, "", "", 0, false, false, null);
    }

    public int getActivityID() {
        return activityID;
    }

    public void setActivityID(int activityID) {
        this.activityID = activityID;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getActivityDisplayName() {
        return activityDisplayName;
    }

    public void setActivityDisplayName(String activityDisplayName) {
        this.activityDisplayName = activityDisplayName;
    }

    public int getActivityValue() {
        return (int) Math.pow(2, this.activityID);
    }

    public boolean isSubActivity() {
        return subActivity;
    }

    public void setSubActivity(boolean subActivity) {
        this.subActivity = subActivity;
    }

    public boolean isPermission() {
        return permission;
    }

    public void setPermission(boolean permission) {
        this.permission = permission;
    }

    public List<SubActivity> getSubActivities() {
        return subActivities;
    }

    public void setSubActivities(List<SubActivity> subActivities) {
        this.subActivities = subActivities;
    }

    @Override
    public String toString() {
        return new JSONSerializer().exclude("*.class").deepSerialize(this);
    }

    public Activity JSONtoObject(String serializedJSONString) {
        return new JSONDeserializer<Activity>().use(null, Activity.class).deserialize(serializedJSONString);
    }
}
