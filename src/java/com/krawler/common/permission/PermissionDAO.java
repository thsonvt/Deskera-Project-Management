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

import com.krawler.common.service.ServiceException;
import com.krawler.database.DbPool.Connection;
import java.util.List;

/**
 *
 * @author Vipin Gupta
 */
public interface PermissionDAO {

    /**
     * This method gives the List of permission object from database which
     * contains permission values(Integer).
     *
     * @param con database connection
     * @param userID userid of user
     * @return List&lt;{@link Permission}&gt; list of {@link Permission} which
     * contains {@link SubActivityPermission}.
     * @throws ServiceException
     */
    List<Permission> getUserPermissionCode(Connection con, String userID) throws ServiceException;

    /**
     * This method saves the permission object for a user to database.
     *
     * @param con database connection
     * @param permisssion
     * @return int
     * @throws ServiceException
     */
    int setUserPermissionCode(Connection con, Permission permisssion) throws ServiceException;

    /**
     * This method gives the List of user's subactivity permission object from
     * database which contains permission values(Integer)
     *
     * @param con database connection
     * @param userID userid of that user.
     * @param featureID feature id of {@link Feature}
     * @return List&lt;{@link SubActivityPermission}&gt; list of {@link SubActivityPermission}.
     * @throws ServiceException
     */
    List<SubActivityPermission> getUserSubActivityPermissionCode(Connection con, String userID, int featureID) throws ServiceException;

    /**
     * This method saves user's SubActivity Permission object in database.
     *
     * @param con database connection
     * @param subActivityPermisssion
     * @return int
     * @throws ServiceException
     */
    int setUserSubActivityPermissionCode(Connection con, SubActivityPermission subActivityPermisssion) throws ServiceException;

    /**
     * This method gives Permission object for one feature.
     *
     * @param con database connection
     * @param userID userid of user
     * @param featureID id of a {@link Feature}.
     * @return {@link Permission}
     * @throws ServiceException
     */
    Permission getFeaturePermission(Connection con, String userID, int featureID) throws ServiceException;

    /**
     * This method gives SubActivityPermission object for a Ativity.
     *
     * @param con database connection.
     * @param userID userid of user.
     * @param featureID id of {@link Feature}.
     * @param activityID id of {@link Activity}.
     * @return {@link SubActivityPermission}.
     * @throws ServiceException
     */
    SubActivityPermission getSubActivityPermissionCode(Connection con, String userID, int featureID, int activityID) throws ServiceException;

    /**
     * This method gives List of Features with its default permission : false.
     *
     * @param con database connection
     * @return List&lt;{@link Feature}&gt;
     * @throws ServiceException
     */
    List<Feature> getAllFeatures(Connection con) throws ServiceException;

    /**
     * This method gives only one Feature.
     *
     * @param con database connection
     * @param featureID id of {@link Feature}.
     * @return {@link Feature}
     * @throws ServiceException
     */
    Feature getFeature(Connection con, int featureID) throws ServiceException;

    /**
     * This method gives all Activities of a Feature.
     *
     * @param con database connection
     * @param featureID id of {@link Feature}.
     * @return List&lt;{@link Activity}&gt;
     * @throws ServiceException
     */
    List<Activity> getAllActivities(Connection con, int featureID) throws ServiceException;

    /**
     * This method gives only one Activity of a Feature.
     *
     * @param con database connection
     * @param featureID id of {@link Feature}.
     * @param activityID id of {@link Activity}.
     * @return {@link Activity}.
     * @throws ServiceException
     */
    Activity getActivity(Connection con, int featureID, int activityID) throws ServiceException;

    /**
     * This method gives all the subActivities of a Activity and Feature.
     *
     * @param con database connection
     * @param featureID id of {@link Feature}.
     * @param activityID id of {@link Activity}.
     * @return List&lt;{@link SubActivity}&gt;.
     * @throws ServiceException
     */
    List<SubActivity> getAllSubActivities(Connection con, int featureID, int activityID) throws ServiceException;

    /**
     * This method gives Only one SubActivity of a Activity and Feature.
     *
     * @param con database connection
     * @param featureID id of {@link Feature}.
     * @param activityID id of {@link Activity}.
     * @param subActivityID id of {@link SubActivity}.
     * @return {@link SubActivity}
     * @throws ServiceException
     */
    SubActivity getSubActivity(Connection con, int featureID, int activityID, int subActivityID) throws ServiceException;

    /**
     * This method Checks user permission for a Feature Only, and return true or
     * false.
     *
     * @param con database connection.
     * @param userID user id of user.
     * @param featureID id of {@link Feature}.
     * @return Boolean true/false
     * @throws ServiceException
     */
    boolean isUserActivityPermissionExist(Connection con, String userID, int featureID) throws ServiceException;

    /**
     * This method Checks user permission for a Activity Only, and return true
     * or false.
     *
     * @param con database connection.
     * @param userID userid of user
     * @param featureID id of {@link Feature}.
     * @param activityID id of {@link Activity}.
     * @return Boolean true/false
     * @throws ServiceException
     */
    boolean isUserSubActivityPermissionExist(Connection con, String userID, int featureID, int activityID) throws ServiceException;
}
