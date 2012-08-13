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
import com.krawler.database.DbPool.Connection;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Kamlesh
 */
public interface ProjectDAO {

    int getTotalCount();

    /**
     * This method gives project in which atleast one member assigned.
     * @param conn
     * @param projectid
     * @return Project Object
     * @throws ServiceException
     */
    Project getProjectById(Connection conn, String projectid) throws ServiceException;

    /**
     * This method gives project without any condition.
     * @param conn
     * @param projectid
     * @return Project Object
     * @throws ServiceException
     */
    Project getProject(Connection conn, String projectid) throws ServiceException;

    String getProjectName(Connection conn, String projectid) throws ServiceException;

    List<Project> getAllProjectByCompany(Connection conn, String companyId, String ss, int offset, int limit) throws ServiceException;

    List<Project> getAllProjectByCompany(Connection conn, String companyId, Map<String, Object> pagingSearchParams, boolean customSearch, boolean includeArchived) throws ServiceException;

    List<Project> getAllProjectByUser(Connection conn, String userid) throws ServiceException;

    List<Project> getAllProjectByUser(Connection conn, String userid, int offset, int limit) throws ServiceException;

    List<Project> getAllProjectByUser(Connection conn, String userid, String ss, int offset, int limit) throws ServiceException;

    List<Project> getAllProjectByUser(Connection conn, String userid, Map<String, Object> pagingSearchParams) throws ServiceException;

    List<Project> getQuickLinks(Connection conn, String userId) throws ServiceException;

    ProjectMember getProjectMember(Connection conn, Project project, String userID) throws ServiceException;

    ProjectMember getProjectMember(Connection conn, String projectID, String userID) throws ServiceException;

    List<ProjectMember> getProjectMembers(Connection conn, Project project) throws ServiceException;

    List<ProjectMember> getProjectMembers(Connection conn, String projectID) throws ServiceException;

    List<Project> getNonModeratorProjects(Connection conn, String companyID) throws ServiceException;
    /**
     *
     * @param countyBy users or company
     * @param archived false for non-archived project true for all(archived+non-archived) project
     * @param ss
     * @return
     * @throws ServiceException
     */
    int getProjectCountBy(Connection conn, String countBy, String searchid, boolean archived, String ss) throws ServiceException;

    void setHealthMeter(Connection conn, Project project) throws ServiceException;

    int updatePERTDiffs(Connection conn, int o_diff, int p_diff, String projectID) throws ServiceException;

    int updatePERTStatus(Connection conn, String projectID, int pertStatus) throws ServiceException;

//    int setProjectModerator(Connection conn, String projectID, String userID) throws ServiceException;

    int addProjectMember(Connection conn, ProjectMember pm) throws ServiceException;

    int addProjectMember(Connection conn, String projectID, String userID, int status, int planpermission) throws ServiceException;

    int updateProjectMember(Connection conn, ProjectMember pm) throws ServiceException;

    int updateProjectMember(Connection conn, String projectID, String userID, int status, int planpermission,
            boolean inUseFlag, boolean isQuickLink, int notificationSubscription, boolean milestoneTimelineSelection) throws ServiceException;
}
