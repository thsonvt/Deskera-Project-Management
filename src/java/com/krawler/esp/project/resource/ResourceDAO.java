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

import com.krawler.common.service.ServiceException;
import com.krawler.database.DbPool.Connection;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Abhay
 */
public interface ResourceDAO {

    public static final int RESOURCE_FROM_PROJECT = 1;
    public static final int RESOURCE_FROM_BASELINE = 2;

    Resource getResource(Connection conn, String resourceID, int resourceInWhichModule, String moduleSpecificID) throws ServiceException;

    List<ProjectResource> getResourcesOnProject(Connection conn, String moduleSpecificID, Map<String, Object> pagingSearchParams) throws ServiceException;

    List<ProjectResource> getResourcesOnProject(Connection conn, String projectID) throws ServiceException;

    List<ProjectResource> getResourcesOnProject(Connection conn, String projectID, boolean fetchBillableOnly) throws ServiceException;

    List<BaselineResource> getResourcesOnBaseline(Connection conn, String baselineID, boolean fetchBillableOnly) throws ServiceException;

    List<BaselineResource> getResourcesOnBaselineTask(Connection conn, String baselineID, String taskID, String companyID, boolean fetchBillableOnly) throws ServiceException;

    List<ProjectResource> getResourcesOnTask(Connection conn, String projectID, String taskID, boolean fetchBillableOnly) throws ServiceException;

    List<Resource.ResourceCategory> getResourcesCategories(Connection conn) throws ServiceException;

    List<Resource.ResourceType> getResourcesTypes(Connection conn) throws ServiceException;

    String getResourcesCategories(Connection conn, String projectID) throws ServiceException;

    Resource.ResourceCategory getResourcesCategory(Connection conn, String categoryID) throws ServiceException;

    Resource.ResourceType getResourcesType(Connection conn, String typeID) throws ServiceException;

    double getResourcesWUValue(Connection conn, String resourceID) throws ServiceException;

    boolean isResourceExists(Connection conn, String resourceName, int resourceInWhichModule, String moduleSpecificID) throws ServiceException;

    int getTotalCount();

    void setBillable(Connection conn, String[] resIDS, String projectID, boolean billable) throws ServiceException;

    String createResourceCategory(Connection conn, String categoryName) throws ServiceException;

    int createProjectResource(Connection conn, String resourceID, String resourceName, String stdRate, int typeID, String colorCode, int categoryID, int wuValue, String projectID) throws ServiceException;

    int editProjectResource(Connection conn, String resourceID, String stdRate, int typeID, String colorCode, int categoryID, int wuValue, String projectID) throws ServiceException;

    int updateResource(Connection conn, Resource r, int resourceInWhichModule) throws ServiceException;
}
