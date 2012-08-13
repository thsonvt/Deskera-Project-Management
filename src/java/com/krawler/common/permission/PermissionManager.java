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
import com.krawler.common.util.Utilities;
import com.krawler.database.DbPool.Connection;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Vipin Gupta
 */
public class PermissionManager {

    private PermissionDAO permdao;

    public PermissionManager(PermissionDAO permdao) {
        this.permdao = permdao;
    }

    public PermissionManager() {
        this(new PermissionDAOImpl());
    }

    /**
     * This method convert a json object to List of Feature, by giving
     * JSONObject.
     *
     * @param jobj (JSONObject of Features List) which has root name "data".
     * @return List&lt;{@link Feature}&gt;
     * @throws JSONException
     */
    public List<Feature> getFeatureListFromJSONObject(JSONObject jobj) throws JSONException {
        JSONArray jarr = jobj.getJSONArray("data");
        int len = jarr.length();
        List<Feature> features = new ArrayList<Feature>();
        for (int i = 0; i < len; i++) {
            Feature feature = Utilities.JSONtoObject(jarr.getJSONObject(i).toString(), Feature.class);
            features.add(feature);
        }
        return features;
    }

    /**
     * This method gives all Features list with Permission false, by giving
     * database connection.
     *
     * @param con database connection.
     * @return List&lt;{@link Feature}&gt; list of Feature object which have
     * permissions false.
     * @throws ServiceException
     */
    public List<Feature> getAllSystemFeatures(Connection con) throws ServiceException {
        List<Feature> features = this.permdao.getAllFeatures(con);
        return features;
    }

    /**
     * This method gives List of Permission Object for a user which contains
     * permission values(Integer), by giving database connection and userid of
     * that user.
     *
     * @param con database connection
     * @param userID userid of user.
     * @return List&lt;{@link Permission}&gt; list of Feature object according
     * to user permissions.
     * @throws ServiceException
     */
    private List<Permission> getUserPermissions(Connection con, String userID) throws ServiceException {
        List<Permission> permissions = this.permdao.getUserPermissionCode(con, userID);
        return permissions;
    }

    /**
     * This method gives All Features with its permission values(Boolean)
     * according to its user's permission values(Integer), by giving database
     * connection and userid of that user.
     *
     * @param con database connection
     * @param userID userid of user.
     * @return List&lt;{@link Feature}&gt; list of Feature object.
     * @throws ServiceException
     */
    public List<Feature> getFeaturePermissionsForUser(Connection con, String userID) throws ServiceException {
        List<Permission> permissions = getUserPermissions(con, userID);
        List<Feature> features = getAllSystemFeatures(con);
        int permissionsSize = permissions.size();
        for (int i = 0, a = 0; i < permissionsSize; i++) {
            Permission permission = permissions.get(i);
            Feature feature = features.get(i + a);
            if (feature.getFeatureID() != permission.getFeatureID()) {
                a++;
                i--;
                continue;
            }
            int m = 0;
            int activityPermissionCode = permission.getUserActivityPermissionValue();
            List<SubActivityPermission> userSubActivityPermissionList = permission.getUserSubActivityPermission();
            List<Activity> activities = feature.getActivities();
            int activitiesSize = activities.size();
            for (int j = 0; j < activitiesSize; j++) {
                Activity activity = activities.get(j);
                if (activity.isSubActivity()) {
                    if (userSubActivityPermissionList.size() > 0) {
                        SubActivityPermission subActivityPermission = userSubActivityPermissionList.get(m++);
                        int subActivityPermissionCode = subActivityPermission.getSubActivityValue();
                        List<SubActivity> subActivities = activity.getSubActivities();
                        int subActivitiesSize = subActivities.size();
                        for (int k = 0; k < subActivitiesSize; k++) {
                            SubActivity subActivity = subActivities.get(k);
                            int subActivityValue = subActivity.getSubActivityValue();
                            if ((subActivityPermissionCode & subActivityValue) == subActivityValue) {
                                subActivity.setPermission(true);
                                activity.setPermission(true);
                                feature.setPermission(true);
                            } else {
                                subActivity.setPermission(false);
                            }
                        }
                    }
                } else if ((activityPermissionCode & activity.getActivityValue()) == activity.getActivityValue()) {
                    activity.setPermission(true);
                    feature.setPermission(true);
                } else {
                    activity.setPermission(false);
                }
            }
        }
        return features;
    }

    /**
     * This method saves the user permissions by giving database connection,
     * List of features and userid of that user
     *
     * @param con database connection
     * @param updatedfeatures {@link Feature} object which is updated by
     * assigned permissions.
     * @param userID userid of user
     * @return greater than 0 for save successfully.
     * @throws ServiceException
     */
    public int setUserPermissions(Connection con, List<Feature> updatedfeatures, String userID) throws ServiceException {
        List<Permission> permissions = modifyPermissionsList(updatedfeatures, userID);
        int row = 0;
        for (int i = 0; i < permissions.size(); i++) {
            Permission permission = permissions.get(i);
            row += permdao.setUserPermissionCode(con, permission);
        }
        return row;
    }

    /**
     * This method gives List of Permission with Permission values(Integer) for
     * a user which is modify according to the Features values(Boolean) by
     * giving List of features and userid of that user.
     *
     * @param features List of {@link Feature}.
     * @param userID userid of user.
     * @return List&lt;{@link Permission}&gt; list of Permission object which is
     * modify according to Feature object.
     * @throws ServiceException
     */
    private List<Permission> modifyPermissionsList(List<Feature> features, String userID) throws ServiceException {
        List<Permission> permissions = new ArrayList();

        for (int i = 0; i < features.size(); i++) {
            Permission permission = new Permission();
            Feature feature = features.get(i);
            List<Activity> activities = feature.getActivities();
            int activityPermissionValue = 0;
            List<SubActivityPermission> subActivityPermissions = new ArrayList();
            for (int j = 0; j < activities.size(); j++) {
                Activity activity = activities.get(j);
                if (activity.isSubActivity()) {
                    int subActivityPermissionValue = 0;
                    SubActivityPermission subActivityPermission = new SubActivityPermission();
                    List<SubActivity> subActivities = activity.getSubActivities();
                    for (int k = 0; k < subActivities.size(); k++) {
                        SubActivity subActivity = subActivities.get(k);
                        if (subActivity.isPermission()) {
                            subActivityPermissionValue += subActivity.getSubActivityValue();
                        }
                    }
                    if (subActivityPermissionValue > 0) {
                        activityPermissionValue += activity.getActivityValue();
                    }
                    subActivityPermission.setUserID(userID);
                    subActivityPermission.setParentfeatureID(feature.getFeatureID());
                    subActivityPermission.setParentActivityID(activity.getActivityID());
                    subActivityPermission.setActivityValue(subActivityPermissionValue);//create pojo for all fields
                    subActivityPermissions.add(subActivityPermission);
                } else if (activity.isPermission()) {
                    activityPermissionValue += activity.getActivityValue();
                }
            }
            permission.setUserID(userID);
            permission.setFeatureID(feature.getFeatureID());
            permission.setUserActivityPermissionValue(activityPermissionValue);
            permission.setUserSubActivityPermission(subActivityPermissions); //create pojo for all fields

            permissions.add(permission);
        }
        return permissions;
    }

    /**
     * This method sets all permissions true for a user, by giving database
     * connection and userid of that user.
     *
     * @param con database connection.
     * @param userId userid of user.
     * @return greater than 0 for save successfully.
     * @throws ServiceException
     * @throws JSONException
     */
    public int AssignAllPermissions(Connection con, String userId) throws ServiceException, JSONException {
        List<Feature> features = getAllSystemFeatures(con);

        for (int i = 0; i < features.size(); i++) {
            Feature feature = features.get(i);
            feature.setPermission(true);
            List<Activity> activities = feature.getActivities();
            for (int j = 0; j < activities.size(); j++) {
                Activity activity = activities.get(j);
                activity.setPermission(true);
                if (activity.isSubActivity()) {
                    List<SubActivity> subActivities = activity.getSubActivities();
                    for (int k = 0; k < subActivities.size(); k++) {
                        SubActivity subActivity = subActivities.get(k);
                        if (feature.getFeatureID() == PermissionConstants.Feature.PROJECT_ADMINISTRATION && activity.getActivityID() == PermissionConstants.Activity.PROJECT_MANAGE_MEMBER && subActivity.getSubActivityID() == PermissionConstants.SubActivity.PROJECT_MANAGE_MEMBER_ALL_PROJECTS) {
                            subActivity.setPermission(true);
                            break;
                        } else {
                            subActivity.setPermission(true);
                        }
                    }
                }
            }
        }
        String str = features.toString();
        return setUserPermissions(con, features, userId);
    }

    /**
     * This method gives the Map of user permissions which contains permission
     * name(String) and permission value(Boolean), by giving database connection
     * and userid of that user.
     *
     * @param con database connection.
     * @param userID userid of user.
     * @return Map&lt;{@link String},{@link Boolean}&gt; map contains
     * permissionName as key and true/false as value, true for permission
     * assigned and false for permission not assigned.
     * @throws ServiceException
     */
    public Map<String, Boolean> getUserPermissionList(Connection con, String userID) throws ServiceException {
        Map<String, Boolean> permissionMap = new HashMap();
        List<Feature> features = getFeaturePermissionsForUser(con, userID);
        for (int i = 0; i < features.size(); i++) {
            Feature feature = features.get(i);
            permissionMap.put(feature.getFeatureName(), feature.isPermission());
            List<Activity> activities = feature.getActivities();
            for (int j = 0; j < activities.size(); j++) {
                Activity activity = activities.get(j);
                permissionMap.put(activity.getActivityName(), activity.isPermission());
                if (activity.isSubActivity()) {
                    List<SubActivity> subActivities = activity.getSubActivities();
                    for (int k = 0; k < subActivities.size(); k++) {
                        SubActivity subActivity = subActivities.get(k);
                        permissionMap.put(subActivity.getSubActivityName(), subActivity.isPermission());
                    }
                }
            }
        }
        return permissionMap;
    }

    /**
     * This method gives only the List of Active(true) Permissions Display Name,
     * by giving database connection and userid of that user.
     *
     * @param con database connection.
     * @param userID userid of user.
     * @return List&lt;{@link String}&gt; list of assigned permissions(display
     * name of permission) only.
     * @throws ServiceException
     */
    public List<String> getUserActivePermissionList(Connection con, String userID) throws ServiceException {
        List<String> permissionList = new ArrayList<String>();
        List<Feature> features = getFeaturePermissionsForUser(con, userID);
        for (int i = 0; i < features.size(); i++) {
            Feature feature = features.get(i);
            List<Activity> activities = feature.getActivities();
            for (int j = 0; j < activities.size(); j++) {
                Activity activity = activities.get(j);
                if (activity.isSubActivity()) {
                    List<SubActivity> subActivities = activity.getSubActivities();
                    for (int k = 0; k < subActivities.size(); k++) {
                        SubActivity subActivity = subActivities.get(k);
                        if (subActivity.isPermission()) {
                            permissionList.add(subActivity.getSubActivityName());
                        }
                    }
                } else if (activity.isPermission()) {
                    permissionList.add(activity.getActivityName());
                }

            }
        }
        return permissionList;
    }

    /**
     * This method gives user permission true or false for a Feature directly,
     * by giving database connection, userid of that user and featureid
     * (PermissionConstants.Feature)
     *
     * @param con database connection.
     * @param userID userid of user.
     * @param featureID id of {@link Feature}.
     * @return true if Feature permission is assigned.
     * @throws ServiceException
     */
    public boolean isPermission(Connection con, String userID, int featureID) throws ServiceException {
        boolean flag = false;
        if (this.permdao.isUserActivityPermissionExist(con, userID, featureID)) {
            Permission permission = this.permdao.getFeaturePermission(con, userID, featureID);
            if (permission.getUserActivityPermissionValue() > 0) {
                flag = true;
            }
        }
        return flag;
    }

    /**
     * This method gives user permission true or false for an Activity directly,
     * by giving database connection, userid of that user, featureid
     * (PermissionConstants.Feature) and activityid
     * (PermissionConstants.Activity).
     *
     * @param con database connection
     * @param userID userid of user.
     * @param featureID id of {@link Feature}.
     * @param activityID id of {@link Activity}.
     * @return true if Activity permission is assigned.
     * @throws ServiceException
     */
    public boolean isPermission(Connection con, String userID, int featureID, int activityID) throws ServiceException {
        boolean flag = false;
        if (this.permdao.isUserActivityPermissionExist(con, userID, featureID)) {
            Permission permission = this.permdao.getFeaturePermission(con, userID, featureID);
            Activity activity = this.permdao.getActivity(con, featureID, activityID);
            if (!activity.isSubActivity()) {
                if ((activity.getActivityValue() & permission.getUserActivityPermissionValue()) == activity.getActivityValue()) {
                    flag = true;
                }
            } else {
                List<SubActivity> subActivities = activity.getSubActivities();
                for (int i = 0; i < subActivities.size(); i++) {
                    flag = isPermission(con, userID, featureID, activityID, subActivities.get(i).getSubActivityID());
                    if (flag) {
                        break;
                    }
                }
            }
        }
        return flag;
    }

    /**
     * This method gives user permission true or false for a SubActivity
     * directly, by giving database connection, userid of that user, featureid
     * (PermissionConstants.Feature), activityid (PermissionConstants.Activity)
     * and subActivityid (PermissionConstants.SubActivity).
     *
     * @param con database connection
     * @param userID userid of user.
     * @param featureID id of {@link Feature}.
     * @param activityID id of {@link Activity}.
     * @param subActivityID id of {@link SubActivity}.
     * @return true if SubActivity Permission is assigned.
     * @throws ServiceException
     */
    public boolean isPermission(Connection con, String userID, int featureID, int activityID, int subActivityID) throws ServiceException {
        boolean flag = false;
        if (this.permdao.isUserSubActivityPermissionExist(con, userID, featureID, activityID)) {
            SubActivityPermission subActivityPermission = this.permdao.getSubActivityPermissionCode(con, userID, featureID, activityID);
            SubActivity subActivity = this.permdao.getSubActivity(con, featureID, activityID, subActivityID);
            if ((subActivity.getSubActivityValue() & subActivityPermission.getSubActivityValue()) == subActivity.getSubActivityValue()) {
                flag = true;
            }
        }
        return flag;
    }

    /**
     * This method gives List of updated Features by giving database connection
     * and Map of permission which contains feature name only of active(true)
     * permissions.
     *
     * @param con database connection.
     * @param permissionNames contains the name of assigned permissions only.
     * @return List&lt;{@link Feature}&gt; contains the list of features which
     * is modified according to assigned permissions.
     * @throws ServiceException
     */
    public List<Feature> updateFeatures(Connection con, List<String> permissionNames) throws ServiceException {
        List<Feature> features = getAllSystemFeatures(con);
        for (int i = 0; i < features.size(); i++) {
            Feature feature = features.get(i);
            List<Activity> activities = feature.getActivities();
            for (int j = 0; j < activities.size(); j++) {
                Activity activity = activities.get(j);
                if (activity.isSubActivity()) {
                    List<SubActivity> subActivities = activity.getSubActivities();
                    for (int k = 0; k < subActivities.size(); k++) {
                        SubActivity subActivity = subActivities.get(k);
                        String name = subActivity.getSubActivityName();
                        if (permissionNames.contains(name)) {
                            subActivity.setPermission(true);
                        }
                    }
                } else {
                    String name = activity.getActivityName();
                    if (permissionNames.contains(name)) {
                        activity.setPermission(true);
                    }
                }
            }
        }
        return features;
    }

    /**
     *
     * @param con
     * @return list of permission names from DB.
     * @throws ServiceException
     */
    private List<String> constructDefaultPermissionList(Connection con) throws ServiceException{
        List<String> permissionNames = new ArrayList<String>();
        Activity activity;
        SubActivity subActivity;

        activity = this.permdao.getActivity(con, PermissionConstants.Feature.PROJECT_ADMINISTRATION,PermissionConstants.Activity.PROJECT_CREATE);
        if(activity != null)
            permissionNames.add(activity.getActivityName());
        activity = this.permdao.getActivity(con, PermissionConstants.Feature.PROJECT_ADMINISTRATION,PermissionConstants.Activity.PROJECT_EDIT);
        if(activity != null)
            permissionNames.add(activity.getActivityName());
        activity = this.permdao.getActivity(con, PermissionConstants.Feature.PROJECT_ADMINISTRATION,PermissionConstants.Activity.PROJECT_DELETE);
        if(activity != null)
            permissionNames.add(activity.getActivityName());
        subActivity = this.permdao.getSubActivity(con, PermissionConstants.Feature.PROJECT_ADMINISTRATION,PermissionConstants.Activity.PROJECT_MANAGE_MEMBER, PermissionConstants.SubActivity.PROJECT_MANAGE_MEMBER_ONLY_ASSIGNED);
        if(subActivity != null)
            permissionNames.add(subActivity.getSubActivityName());
        activity = this.permdao.getActivity(con, PermissionConstants.Feature.CUSTOM_REPORTS,PermissionConstants.Activity.CUSTOM_REPORT_CREATE_DELETE);
        if(activity != null)
            permissionNames.add(activity.getActivityName());

        return permissionNames;
    }

    /**
     * This method used to set user's default permissions.
     * @param con
     * @param userID
     * @return greater than 0 if default permissions are set.
     * @throws ServiceException
     */
    public int setUserDefaultPermissions(Connection con, String userID) throws ServiceException{
        List<String> permissionNames  = constructDefaultPermissionList(con);
        List<Feature> features = updateFeatures(con, permissionNames);
        int i = setUserPermissions(con, features, userID);
        return i;
    }
}
