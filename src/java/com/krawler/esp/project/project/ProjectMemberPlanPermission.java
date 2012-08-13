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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Abhay
 */
public enum ProjectMemberPlanPermission {

    VIEW_ALL(2), MODIFY_ALL(4), VIEW_ASSIGNED(8), VIEW_ALL_VIEW_ASSIGNED(10), MODIFY_ASSIGNED(16), VIEW_ALL_MODIFY_ASSIGNED(18);
    private static final Map<Integer, ProjectMemberPlanPermission> lookup = new HashMap<Integer, ProjectMemberPlanPermission>();

    static {
        for (ProjectMemberPlanPermission ProjectMemberPlanPermission : EnumSet.allOf(ProjectMemberPlanPermission.class)) {
            lookup.put(ProjectMemberPlanPermission.getCode(), ProjectMemberPlanPermission);
        }
    }
    private int code;

    private ProjectMemberPlanPermission(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ProjectMemberPlanPermission get(int code) {
        return lookup.get(code);
    }

    @Override
    public String toString() {
        String oldperm = super.toString();
        switch (super.ordinal()) {
            case 2:
                oldperm = "'View All Tasks'";
                break;
            case 10:
                oldperm = "'View All Tasks'";
                break;
            case 4:
                oldperm = "'Modify All Tasks'";
                break;
            case 16:
                oldperm = "'Modify Assigned Tasks - % Complete Only'";
                break;
            case 8:
                oldperm = "'View Assigned Tasks'";
                break;
            case 18:
                oldperm = "'View All - Modify Assigned'";
                break;
        }
        return oldperm;
    }
}
