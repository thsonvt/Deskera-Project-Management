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
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Vipin Gupta
 */
public class PermissionDAOImpl implements PermissionDAO {

    private StringBuilder query;
    private List params;
    private Permission permission;
    private SubActivityPermission subActivityPermission;
    private Feature feature;
    private Activity activity;
    private SubActivity subActivity;
    private List<Permission> permissions;
    private List<SubActivityPermission> subActivityPermissions;
    private List<Feature> features;
    private List<Activity> activities;
    private List<SubActivity> subActivities;
    private final String FEATURES_TABLE = "featurelist";
    private final String ACTIVITIES_TABLE = "activitieslist";
    private final String SUBACTIVITIES_TABLE = "subactivitylist";
    private final String ACTIVITY_PERMISSION_TABLE = "userpermissions";
    private final String SUBACTIVITY_PERMISSION_TABLE = "userpermissions_subactivity";

    private void loadPermissionObject(DbResults rs) throws ServiceException {
        permission = new Permission();
        PermissionDAOHelper.loadPermissions(rs, permission);
    }

    private void loadSubActivityPermissionObject(DbResults rs) throws ServiceException {
        subActivityPermission = new SubActivityPermission();
        PermissionDAOHelper.loadSubActivityPermissions(rs, subActivityPermission);
    }

    private void loadFeatureObject(DbResults rs) throws ServiceException {
        feature = new Feature();
        PermissionDAOHelper.loadFeatures(rs, feature);
    }

    private void loadActivityObject(DbResults rs) throws ServiceException {
        activity = new Activity();
        PermissionDAOHelper.loadActivities(rs, activity);
    }

    private void loadSubActivityObject(DbResults rs) throws ServiceException {
        subActivity = new SubActivity();
        PermissionDAOHelper.loadSubActivities(rs, subActivity);
    }

    @Override
    public List<Permission> getUserPermissionCode(Connection con, String userID) throws ServiceException {
        permissions = new ArrayList();
        query = new StringBuilder();
        query.append("select * from " + ACTIVITY_PERMISSION_TABLE + " where userid = ? order by featureid asc");

        params = new ArrayList();
        params.add(userID);

        DbResults rs = DbUtil.executeQuery(con, query.toString(), params.toArray());
        while (rs.next()) {
            loadPermissionObject(rs);
            subActivityPermissions = getUserSubActivityPermissionCode(con, userID, permission.getFeatureID());
            permission.setUserSubActivityPermission(subActivityPermissions);
            permissions.add(permission);
        }
        return permissions;
    }

    @Override
    public int setUserPermissionCode(Connection con, Permission permission) throws ServiceException {
        if (isUserActivityPermissionExist(con, permission.getUserID(), permission.getFeatureID())) {
            query = new StringBuilder();
            query.append("update " + ACTIVITY_PERMISSION_TABLE + " set permissions = ? where userid = ? and featureid = ?");
        } else {
            query = new StringBuilder();
            query.append("insert into " + ACTIVITY_PERMISSION_TABLE + " (permissions, userid, featureid) values(?,?,?)");
        }
        params = new ArrayList();
        params.add(permission.getUserActivityPermissionValue());
        params.add(permission.getUserID());
        params.add(permission.getFeatureID());

        int row = DbUtil.executeUpdate(con, query.toString(), params.toArray());
        if (permission.getUserSubActivityPermission() != null) {
            List<SubActivityPermission> subActivityPermissions = permission.getUserSubActivityPermission();
            for (int i = 0; i < subActivityPermissions.size(); i++) {
                row += setUserSubActivityPermissionCode(con, subActivityPermissions.get(i));
            }
        }
        return row;
    }

    @Override
    public List<SubActivityPermission> getUserSubActivityPermissionCode(Connection con, String userID, int featureID) throws ServiceException {
        subActivityPermissions = new ArrayList();
        query = new StringBuilder();
        query.append("select * from " + SUBACTIVITY_PERMISSION_TABLE + " where userid = ? and featureid =? order by activityid asc");

        params = new ArrayList();
        params.add(userID);
        params.add(featureID);

        DbResults rs = DbUtil.executeQuery(con, query.toString(), params.toArray());
        while (rs.next()) {
            loadSubActivityPermissionObject(rs);
            subActivityPermissions.add(subActivityPermission);
        }
        return subActivityPermissions;
    }

    @Override
    public int setUserSubActivityPermissionCode(Connection con, SubActivityPermission subActivityPermission) throws ServiceException {
        if (isUserSubActivityPermissionExist(con, subActivityPermission.getUserID(), subActivityPermission.getParentfeatureID(), subActivityPermission.getParentActivityID())) {
            query = new StringBuilder();
            query.append("update " + SUBACTIVITY_PERMISSION_TABLE + " set permissions = ? where userid = ? and featureid = ? and activityid = ?");
        } else {
            query = new StringBuilder();
            query.append("insert into " + SUBACTIVITY_PERMISSION_TABLE + " (permissions, userid, featureid, activityid) values(?,?,?,?)");
        }
        params = new ArrayList();
        params.add(subActivityPermission.getSubActivityValue());
        params.add(subActivityPermission.getUserID());
        params.add(subActivityPermission.getParentfeatureID());
        params.add(subActivityPermission.getParentActivityID());

        int row = DbUtil.executeUpdate(con, query.toString(), params.toArray());
        return row;
    }

    @Override
    public Permission getFeaturePermission(Connection con, String userID, int featureID) throws ServiceException {
        query = new StringBuilder();
        query.append("select * from " + ACTIVITY_PERMISSION_TABLE + " where userid = ? and featureid = ? order by featureid asc");

        params = new ArrayList();
        params.add(userID);
        params.add(featureID);

        DbResults rs = DbUtil.executeQuery(con, query.toString(), params.toArray());
        if (rs.next()) {
            loadPermissionObject(rs);
            subActivityPermissions = getUserSubActivityPermissionCode(con, userID, permission.getFeatureID());
            permission.setUserSubActivityPermission(subActivityPermissions);
        } else {
            permission = new Permission();
        }
        return permission;
    }

    @Override
    public List<Feature> getAllFeatures(Connection con) throws ServiceException {
        features = new ArrayList();
        query = new StringBuilder();
        query.append("select * from " + FEATURES_TABLE + " order by featureid asc");

        DbResults rs = DbUtil.executeQuery(con, query.toString());
        while (rs.next()) {
            loadFeatureObject(rs);
            List<Activity> activities = getAllActivities(con, feature.getFeatureID());
            feature.setActivities(activities);
            features.add(feature);
        }
        return features;
    }

    @Override
    public Feature getFeature(Connection con, int featureID) throws ServiceException {
        query = new StringBuilder();
        query.append("select * from " + FEATURES_TABLE + " where featureid =?");

        params = new ArrayList();
        params.add(featureID);

        DbResults rs = DbUtil.executeQuery(con, query.toString(), params.toArray());
        if (rs.next()) {
            loadFeatureObject(rs);
            List<Activity> activities = getAllActivities(con, feature.getFeatureID());
            feature.setActivities(activities);
        } else {
            feature = new Feature();
        }
        return feature;
    }

    @Override
    public List<Activity> getAllActivities(Connection con, int featureID) throws ServiceException {
        activities = new ArrayList();
        query = new StringBuilder();
        query.append("select * from " + ACTIVITIES_TABLE + " where featureid = ? order by activityid asc");

        params = new ArrayList();
        params.add(featureID);

        DbResults rs = DbUtil.executeQuery(con, query.toString(), params.toArray());
        while (rs.next()) {
            loadActivityObject(rs);
            if (activity.isSubActivity()) {
                List<SubActivity> subActivities = getAllSubActivities(con, featureID, activity.getActivityID());
                activity.setSubActivities(subActivities);
            }
            activities.add(activity);
        }
        return activities;
    }

    @Override
    public Activity getActivity(Connection con, int featureID, int activityID) throws ServiceException {
        query = new StringBuilder();
        query.append("select * from " + ACTIVITIES_TABLE + " where featureid = ? and activityid = ? order by activityid asc");

        params = new ArrayList();
        params.add(featureID);
        params.add(activityID);

        DbResults rs = DbUtil.executeQuery(con, query.toString(), params.toArray());
        if (rs.next()) {
            loadActivityObject(rs);
            if (activity.isSubActivity()) {
                List<SubActivity> subActivities = getAllSubActivities(con, featureID, activity.getActivityID());
                activity.setSubActivities(subActivities);
            }
        } else {
            activity = new Activity();
        }
        return activity;
    }

    @Override
    public List<SubActivity> getAllSubActivities(Connection con, int featureID, int activityID) throws ServiceException {
        subActivities = new ArrayList();
        query = new StringBuilder();
        query.append("select * from " + SUBACTIVITIES_TABLE + " where parentfeatureid = ? and parentactivityid = ? order by parentactivityid asc");

        params = new ArrayList();
        params.add(featureID);
        params.add(activityID);

        DbResults rs = DbUtil.executeQuery(con, query.toString(), params.toArray());
        while (rs.next()) {
            loadSubActivityObject(rs);
            subActivities.add(subActivity);
        }
        return subActivities;
    }

    @Override
    public SubActivity getSubActivity(Connection con, int featureID, int activityID, int subActivityID) throws ServiceException {
        query = new StringBuilder();
        query.append("select * from " + SUBACTIVITIES_TABLE + " where parentfeatureid = ? and parentactivityid = ? and activityid = ? order by parentactivityid asc");

        params = new ArrayList();
        params.add(featureID);
        params.add(activityID);
        params.add(subActivityID);

        DbResults rs = DbUtil.executeQuery(con, query.toString(), params.toArray());
        if (rs.next()) {
            loadSubActivityObject(rs);
        } else {
            subActivity = new SubActivity();
        }
        return subActivity;
    }

    @Override
    public boolean isUserActivityPermissionExist(Connection con, String userID, int featureID) throws ServiceException {
        boolean flag = false;
        query = new StringBuilder();
        query.append("select * from " + ACTIVITY_PERMISSION_TABLE + " where userid = ? and featureid =?");

        params = new ArrayList();
        params.add(userID);
        params.add(featureID);

        DbResults rs = DbUtil.executeQuery(con, query.toString(), params.toArray());
        if (rs.next()) {
            flag = true;
        }
        return flag;
    }

    @Override
    public boolean isUserSubActivityPermissionExist(Connection con, String userID, int featureID, int activityID) throws ServiceException {
        boolean flag = false;
        query = new StringBuilder();
        query.append("select * from " + SUBACTIVITY_PERMISSION_TABLE + " where userid = ? and featureid = ? and activityid = ?");

        params = new ArrayList();
        params.add(userID);
        params.add(featureID);
        params.add(activityID);

        DbResults rs = DbUtil.executeQuery(con, query.toString(), params.toArray());
        if (rs.next()) {
            flag = true;
        }
        return flag;
    }

    @Override
    public SubActivityPermission getSubActivityPermissionCode(Connection con, String userID, int featureID, int activityID) throws ServiceException {
        query = new StringBuilder();
        query.append("select * from " + SUBACTIVITY_PERMISSION_TABLE + " where userid = ? and featureid =? and activityid = ? order by activityid asc");

        params = new ArrayList();
        params.add(userID);
        params.add(featureID);
        params.add(activityID);

        DbResults rs = DbUtil.executeQuery(con, query.toString(), params.toArray());
        if (rs.next()) {
            loadSubActivityPermissionObject(rs);
        } else {
            subActivityPermission = new SubActivityPermission();
        }
        return subActivityPermission;
    }
}
