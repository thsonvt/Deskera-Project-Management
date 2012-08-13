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

import com.krawler.common.service.ServiceException;
import com.krawler.database.DbResults;
import com.krawler.esp.user.User;
import com.krawler.esp.user.UserHelper;

/**
 *
 * @author Abhay
 */
public class ProjectHelper {
    
    public static void setProjectObject(DbResults rs, Project proj) throws ServiceException {
        proj.setProjectID(rs.getString("projectid"));
        if (rs.has("companyid")) {
            proj.setCompanyID(rs.getString("companyid"));
        }
        if (rs.has("image")) {
            proj.setImage(rs.getString("image"));
        }
        if (rs.has("archived")) {
            proj.setArchieved(rs.getBoolean("archived"));
        }
        if (rs.has("createdon")) {
            proj.setCreatedOn(rs.getDate("createdon"));
        }
        if (rs.has("description")) {
            proj.setDescription(rs.getString("description"));
        }
        if (rs.has("nickname")) {
            proj.setNickName(rs.getString("nickname"));
        }
        if (rs.has("projectname")) {
            proj.setProjectName(rs.getString("projectname"));
        }
        if (rs.has("startdate")) {
            proj.setStartDate(rs.getDate("startdate"));
        }
        if (rs.has("members")) {
            proj.setMemberCount(rs.getInt("members"));
        }
        if (rs.has("o_diff")) {
            proj.setOptimisticDiff(rs.getInt("o_diff"));
        }
        if (rs.has("p_diff")) {
            proj.setPessimisticDiff(rs.getInt("p_diff"));
        }
        if (rs.has("pertstatus")) {
            proj.setPertStatus(rs.getInt("pertstatus"));
            proj.setPertDiffStatus(PERTDiffStatus.get(proj.getPertStatus()));
        }
    }
    
    public static void setProjectMembersObject(DbResults rs, ProjectMember pm) throws ServiceException{
        Project project = new Project();
        setProjectObject(rs, project);
        User u = new User();
        UserHelper.setObject(rs, u);
        
        pm.setProject(project);
        pm.setUser(u);
        
        if(rs.has("status")){
            pm.setStatus(rs.getInt("status"));
        }
        if(rs.has("planpermission")){
            pm.setPlanPermission(rs.getInt("planpermission"));
        }
        if(rs.has("notification_subscription")){
            pm.setNotificationSubscription(rs.getInt("notification_subscription"));
        }
        if (rs.has("quicklink")) {
            pm.setQuickLink(rs.getBoolean("quicklink"));
        }
        if (rs.has("milestonestack")) {
            pm.setMilestoneTimelineSelection(rs.getBoolean("milestonestack"));
        }
        if (rs.has("inuseflag")) {
            pm.setInUseFlag(rs.getBoolean("inuseflag"));
        }
    }
    
    public static ProjectMember setProjectMembersObject(DbResults rs, Project project, User u) throws ServiceException{
        ProjectMember pm = new ProjectMember();
        pm.setProject(project);
        pm.setUser(u);
        
        if(rs.has("status")){
            pm.setStatus(rs.getInt("status"));
        }
        if(rs.has("planpermission")){
            pm.setPlanPermission(rs.getInt("planpermission"));
        }
        if(rs.has("notification_subscription")){
            pm.setNotificationSubscription(rs.getInt("notification_subscription"));
        }
        if (rs.has("quicklink")) {
            pm.setQuickLink(rs.getBoolean("quicklink"));
        }
        if (rs.has("milestonestack")) {
            pm.setMilestoneTimelineSelection(rs.getBoolean("milestonestack"));
        }
        if (rs.has("inuseflag")) {
            pm.setInUseFlag(rs.getBoolean("inuseflag"));
        }
        return pm;
    }
    
}
