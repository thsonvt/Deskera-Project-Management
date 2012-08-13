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
public class Feature implements Serializable {

    private int featureID;
    private String featureName;
    private String featureDisplayName;
    private boolean permission;
    private List<Activity> activities;

    public Feature(int featureID, String featureName, String featureDisplayName, boolean permission, List<Activity> activities) {
        this.featureID = featureID;
        this.featureName = featureName;
        this.featureDisplayName = featureDisplayName;
        this.permission = permission;
        this.activities = activities;
    }

    public Feature() {
        this(0, "", "", false, null);
    }

    public int getFeatureID() {
        return featureID;
    }

    public void setFeatureID(int featureID) {
        this.featureID = featureID;
    }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public String getFeatureDisplayName() {
        return featureDisplayName;
    }

    public void setFeatureDisplayName(String featureDisplayName) {
        this.featureDisplayName = featureDisplayName;
    }

    public boolean isPermission() {
        return permission;
    }

    public void setPermission(boolean permission) {
        this.permission = permission;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }

    @Override
    public String toString() {
        return new JSONSerializer().exclude("*.class").prettyPrint(false).deepSerialize(this);
    }

    public Feature JSONtoObject(String serializedJSONString) {
        return new JSONDeserializer<Feature>().use(null, Feature.class).deserialize(serializedJSONString);
    }
}
