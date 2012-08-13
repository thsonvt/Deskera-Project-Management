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
public class CheckListTask {

    private String cTaskID;
    private String cTaskName;

    public CheckListTask() {
    }

    @Override
    public String toString() {
        return new JSONSerializer().exclude("*.class").deepSerialize(this);
    }

    public CheckListTask JSONtoObject(String serializedJSONString) {
        return new JSONDeserializer<CheckListTask>().use(null, CheckListTask.class).deserialize(serializedJSONString);
    }

    public String getcTaskID() {
        return cTaskID;
    }

    public void setcTaskID(String cTaskID) {
        this.cTaskID = cTaskID;
    }

    public String getcTaskName() {
        return cTaskName;
    }

    public void setcTaskName(String cTaskName) {
        this.cTaskName = cTaskName;
    }
}
