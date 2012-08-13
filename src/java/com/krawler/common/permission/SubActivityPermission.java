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

/**
 *
 * @author Vipin Gupta
 */
public class SubActivityPermission implements Serializable {

    private String userID;
    private int subActivityValue;
    private int parentActivityID;
    private int parentfeatureID;

    public SubActivityPermission(String userID, int subActivityValue, int parentActivityID) {
        this.userID = userID;
        this.subActivityValue = subActivityValue;
        this.parentActivityID = parentActivityID;
    }

    public SubActivityPermission() {
        this("", 0, 0);
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public int getSubActivityValue() {
        return subActivityValue;
    }

    public void setActivityValue(int activityValue) {
        this.subActivityValue = activityValue;
    }

    public int getParentActivityID() {
        return parentActivityID;
    }

    public void setParentActivityID(int parentActivityID) {
        this.parentActivityID = parentActivityID;
    }

    public int getParentfeatureID() {
        return parentfeatureID;
    }

    public void setParentfeatureID(int parentfeatureID) {
        this.parentfeatureID = parentfeatureID;
    }

    @Override
    public String toString() {
        return new JSONSerializer().exclude("*.class").deepSerialize(this);
    }

    public SubActivityPermission JSONtoObject(String serializedJSONString) {
        return new JSONDeserializer<SubActivityPermission>().use(null, SubActivityPermission.class).deserialize(serializedJSONString);
    }
}
