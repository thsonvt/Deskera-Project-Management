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
public class SubActivity implements Serializable {

    private int subActivityID;
    private String subActivityName;
    private String subActivityDisplayName;
    private int subActivityValue;
    private boolean permission;

    public SubActivity(int subActivityID, String subActivityName, String subActivityDisplayName, int subActivityValue, boolean permission) {
        this.subActivityID = subActivityID;
        this.subActivityName = subActivityName;
        this.subActivityDisplayName = subActivityDisplayName;
        this.subActivityValue = subActivityValue;
        this.permission = permission;
    }

    public SubActivity() {
        this(0, "", "", 0, false);
    }

    public int getSubActivityID() {
        return subActivityID;
    }

    public void setSubActivityID(int subActivityID) {
        this.subActivityID = subActivityID;
    }

    public String getSubActivityName() {
        return subActivityName;
    }

    public void setSubActivityName(String subActivityName) {
        this.subActivityName = subActivityName;
    }

    public String getSubActivityDisplayName() {
        return subActivityDisplayName;
    }

    public void setSubActivityDisplayName(String subActivityDisplayName) {
        this.subActivityDisplayName = subActivityDisplayName;
    }

    public int getSubActivityValue() {
        return (int) Math.pow(2, this.subActivityID);
    }

    public boolean isPermission() {
        return permission;
    }

    public void setPermission(boolean permission) {
        this.permission = permission;
    }

    @Override
    public String toString() {
        return new JSONSerializer().exclude("*.class").deepSerialize(this);
    }

    public SubActivity JSONtoObject(String serializedJSONString) {
        return new JSONDeserializer<SubActivity>().use(null, SubActivity.class).deserialize(serializedJSONString);
    }
}
