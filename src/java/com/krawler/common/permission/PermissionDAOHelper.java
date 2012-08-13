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

import com.krawler.database.DbResults;

/**
 *
 * @author Vipin Gupta
 */
class PermissionDAOHelper {

    static void loadPermissions(DbResults rs, Permission permission) {

        if (rs.has("featureid")) {
            permission.setFeatureID(rs.getInt("featureid"));
        }
        if (rs.has("userid")) {
            permission.setUserID(rs.getString("userid"));
        }
        if (rs.has("permissions")) {
            permission.setUserActivityPermissionValue(rs.getInt("permissions"));
        }
    }

    static void loadSubActivityPermissions(DbResults rs, SubActivityPermission subActivityPermission) {

        if (rs.has("userid")) {
            subActivityPermission.setUserID(rs.getString("userid"));
        }
        if (rs.has("permissions")) {
            subActivityPermission.setActivityValue(rs.getInt("permissions"));
        }
        if (rs.has("activityid")) {
            subActivityPermission.setParentActivityID(rs.getInt("activityid"));
        }
        if (rs.has("featureid")) {
            subActivityPermission.setParentfeatureID(rs.getInt("featureid"));
        }

    }

    static void loadFeatures(DbResults rs, Feature feature) {
        if (rs.has("featureid")) {
            feature.setFeatureID(rs.getInt("featureid"));
        }
        if (rs.has("featurename")) {
            feature.setFeatureName(rs.getString("featurename"));
        }
        if (rs.has("displayfeaturename")) {
            feature.setFeatureDisplayName(rs.getString("displayfeaturename"));
        }
    }

    static void loadActivities(DbResults rs, Activity activity) {
        if (rs.has("activityid")) {
            activity.setActivityID(rs.getInt("activityid"));
        }
        if (rs.has("activityname")) {
            activity.setActivityName(rs.getString("activityname"));
        }
        if (rs.has("displayactivityname")) {
            activity.setActivityDisplayName(rs.getString("displayactivityname"));
        }
        if (rs.has("havesubactivity")) {
            activity.setSubActivity(rs.getBoolean("havesubactivity"));
        }

    }

    static void loadSubActivities(DbResults rs, SubActivity subActivity) {
        if (rs.has("activityid")) {
            subActivity.setSubActivityID(rs.getInt("activityid"));
        }
        if (rs.has("activityname")) {
            subActivity.setSubActivityName(rs.getString("activityname"));
        }
        if (rs.has("displayactivityname")) {
            subActivity.setSubActivityDisplayName(rs.getString("displayactivityname"));
        }
    }
}
