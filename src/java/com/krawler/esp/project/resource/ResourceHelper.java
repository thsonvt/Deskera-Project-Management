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

import com.krawler.common.util.StringUtil;
import com.krawler.database.DbResults;

/**
 *
 * @author Abhay
 */
public class ResourceHelper {
    
    public static void loadResource(Resource r, DbResults rs){
        r.setResourceID(rs.getString("resourceid"));
        r.setResourceName(rs.getString("resourcename"));
        r.setBillable(rs.getBoolean("billable"));
        r.setColorCode(rs.getString("colorcode"));
        r.setInUseFlag(rs.getBoolean("inuseflag"));
        r.setStdRate(rs.getDouble("stdrate"));
        r.setWuvalue(rs.getDouble("wuvalue"));
    }

    public static void loadResourceType(Resource.ResourceType rt, DbResults rs){
        rt.setTypeID(rs.getInt("typeid"));
        rt.setTypeName(rs.getString("typename"));
    }
    
    public static void loadResourceCategory(Resource.ResourceCategory rc, DbResults rs){
        rc.setCategoryID(rs.getInt("categoryid"));
        rc.setCategoryName(rs.getString("categoryname"));
    }
    
    public static void loadProjectResource(ProjectResource r, DbResults rs, String projectID){
        loadResource(r, rs);
        if(!StringUtil.isNullOrEmpty(projectID))
            r.setProjectID(projectID);
        else if(rs.has("projectid"))
            r.setProjectID(rs.getString("projectid"));
        else if(rs.has("projid"))
            r.setProjectID(rs.getString("projid"));
    }
    
    public static void loadBaselineResource(BaselineResource r, DbResults rs, String baselineID){
        loadResource(r, rs);
        if(!StringUtil.isNullOrEmpty(baselineID))
            r.setBaselineID(baselineID);
        else if(rs.has("baselineid"))
            r.setBaselineID(rs.getString("baselineid"));
    }
    
}
