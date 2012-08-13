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

import com.krawler.database.DbResults;

/**
 *
 * @author Abhay Kulkarni
 */
public class CheckListHelper {

    public static CheckList loadCheckList(DbResults rs, CheckList cl) {

        cl.setCheckListID(rs.getString("checklistid"));

        if (rs.has("checklistname")) {
            cl.setCheckListName(rs.getString("checklistname"));
        }
        if (rs.has("checklistdesc")) {
            cl.setCheckListDescription(rs.getString("checklistdesc"));
        }
        if (rs.has("createdby")) {
            cl.setCreatedBy(rs.getString("createdby"));
        }
        if (rs.has("updatedby")) {
            cl.setUpdatedBy(rs.getString("updatedby"));
        }
        if (rs.has("createdon")) {
            cl.setCreatedOn(rs.getDate("createdon"));
        }
        if (rs.has("updatedon")) {
            cl.setUpdatedOn(rs.getDate("updatedon"));
        }
        if (rs.has("companyid")) {
            cl.setCompanyID(rs.getString("companyid"));
        }
        return cl;
    }

    public static CheckListTask loadCheckListTask(DbResults rs, CheckListTask clt) {

        if (rs.has("ctaskid")) {
            clt.setcTaskID(rs.getString("ctaskid"));
        }
        if (rs.has("ctaskname")) {
            clt.setcTaskName(rs.getString("ctaskname"));
        }
        return clt;
    }

    public static TaskCheckListMapping loadTaskCheckListMapping(DbResults rs, TaskCheckListMapping tclm) {

        if (rs.has("mappingid")) {
            tclm.setMappingID(rs.getString("mappingid"));
        }
        if (rs.has("taskid")) {
            tclm.setTaskID(rs.getString("taskid"));
        }
        if (rs.has("checklistid")) {
            tclm.setCheckListID(rs.getString("checklistid"));
        }
        if (rs.has("mappedby")) {
            tclm.setMappedBy(rs.getString("mappedby"));
        }
        return tclm;
    }
}
