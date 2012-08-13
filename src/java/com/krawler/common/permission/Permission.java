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
public class Permission implements Serializable {

    private int featureID;
    private String userID;
    private int userActivityPermissionValue;
    private List<SubActivityPermission> userSubActivityPermission;

    public Permission(int featureID, int userActivityPermissionValue, List<SubActivityPermission> userSubActivityPermission) {
        this.featureID = featureID;
        this.userActivityPermissionValue = userActivityPermissionValue;
        this.userSubActivityPermission = userSubActivityPermission;
    }

    public Permission() {
        this(0, 0, null);
    }

    public int getFeatureID() {
        return featureID;
    }

    public void setFeatureID(int featureID) {
        this.featureID = featureID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public int getUserActivityPermissionValue() {
        return userActivityPermissionValue;
    }

    public void setUserActivityPermissionValue(int userActivityPermissionValue) {
        this.userActivityPermissionValue = userActivityPermissionValue;
    }

    public List<SubActivityPermission> getUserSubActivityPermission() {
        return userSubActivityPermission;
    }

    public void setUserSubActivityPermission(List<SubActivityPermission> userSubActivityPermission) {
        this.userSubActivityPermission = userSubActivityPermission;
    }

    @Override
    public String toString() {
        return new JSONSerializer().exclude("*.class").prettyPrint(false).deepSerialize(this);
    }

    public Permission JSONtoObject(String serializedJSONString) {
        return new JSONDeserializer<Permission>().use(null, Permission.class).deserialize(serializedJSONString);
    }
}
