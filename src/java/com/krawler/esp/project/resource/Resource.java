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
package com.krawler.esp.project.resource;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.io.Serializable;

/**
 *
 * @author Abhay
 */
public class Resource implements Serializable{
    
    protected String resourceID;
    protected String resourceName;
    protected boolean billable;
    protected ResourceCategory category;
    protected ResourceType type;
    protected double stdRate;
    protected String colorCode;
    protected boolean inUseFlag;
    protected double wuvalue;

    public Resource(String resourceID, String resourceName, boolean billable, double stdRate, String colorCode, boolean inUseFlag) {
        this.resourceID = resourceID;
        this.resourceName = resourceName;
        this.billable = billable;
        this.stdRate = stdRate;
        this.colorCode = colorCode;
        this.inUseFlag = inUseFlag;
    }

    public Resource() {
    }

    @Override
    public String toString() {
        return new JSONSerializer().exclude("*.class").deepSerialize(this);
    }

    public Resource JSONtoObject(String serializedJSONString){
        return new JSONDeserializer<Resource>().deserialize(serializedJSONString);
    }
    
    public boolean isBillable() {
        return billable;
    }

    public void setBillable(boolean billable) {
        this.billable = billable;
    }

    public ResourceCategory getCategory() {
        return category;
    }

    public void setCategory(ResourceCategory categoryID) {
        this.category = categoryID;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public boolean isInUseFlag() {
        return inUseFlag;
    }

    public void setInUseFlag(boolean inUseFlag) {
        this.inUseFlag = inUseFlag;
    }

    public String getResourceID() {
        return resourceID;
    }

    public void setResourceID(String resourceID) {
        this.resourceID = resourceID;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public double getStdRate() {
        return stdRate;
    }

    public void setStdRate(double stdRate) {
        this.stdRate = stdRate;
    }

    public ResourceType getType() {
        return type;
    }

    public void setType(ResourceType typeID) {
        this.type = typeID;
    }

    public double getWuvalue() {
        return wuvalue;
    }

    public void setWuvalue(double wuvalue) {
        this.wuvalue = wuvalue;
    }

    public class ResourceCategory {
    
        private int categoryID;
        private String categoryName;

        public ResourceCategory() {
        }

        public ResourceCategory(int categoryID, String categoryName) {
            this.categoryID = categoryID;
            this.categoryName = categoryName;
        }

        public ResourceCategory(int categoryID) {
            this.categoryID = categoryID;
        }

        @Override
        public String toString() {
            return new JSONSerializer().exclude("*.class").deepSerialize(this);
        }
        
        public int getCategoryID() {
            return categoryID;
        }

        public void setCategoryID(int categoryID) {
            this.categoryID = categoryID;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

    }
    
    public class ResourceType {

        private int typeID;
        private String typeName;

        public ResourceType() {
        }

        public ResourceType(int typeID, String typeName) {
            this.typeID = typeID;
            this.typeName = typeName;
        }

        public ResourceType(int typeID) {
            this.typeID = typeID;
        }

        @Override
        public String toString() {
            return new JSONSerializer().exclude("*.class").deepSerialize(this);
        }
        
        public int getTypeID() {
            return typeID;
        }

        public void setTypeID(int typeID) {
            this.typeID = typeID;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

    }
}
