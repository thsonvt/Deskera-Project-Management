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

/**
 *
 * @author krawler
 */
public final class PermissionConstants {

    public static final int NONE = 0;

    public class Feature {

        public static final int USER_ADMINISTRATION = 1;
        public static final int PROJECT_ADMINISTRATION = 2;
        public static final int COMPANY_ADMINISTRATION = 3;
        public static final int AUDIT_TRAIL = 4;
        public static final int CUSTOM_REPORTS = 5;
    }

    public class Activity {

        public static final int USER_MANAGE_PERMISSION = 1;
        public static final int USER_ASSIGN_PROJECT = 2;
        public static final int PROJECT_CREATE = 1;
        public static final int PROJECT_EDIT = 2;
        public static final int PROJECT_DELETE = 3;
        public static final int PROJECT_ARCHIVE_ACTIVATE = 4;
        public static final int PROJECT_MANAGE_MEMBER = 5;
        public static final int COMPANY_NOTIFICATIONS = 1;
        public static final int COMPANY_FEATURES = 2;
        public static final int COMPANY_CUSTOM_COLUMN = 3;
        public static final int COMPANY_HOLIDAYS = 4;
        public static final int COMPANY_MODULE_SUBSCRIPTION = 5;
        public static final int AUDIT_TRAIL = 1;
        public static final int CUSTOM_REPORT_CREATE_DELETE = 1;
        public static final int CUSTOM_REPORT_VIEW = 2;
    }

    public class SubActivity {

        public static final int PROJECT_MANAGE_MEMBER_ALL_PROJECTS = 1;
        public static final int PROJECT_MANAGE_MEMBER_ONLY_ASSIGNED = 2;
    }
}
