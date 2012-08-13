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
package com.krawler.esp.project.task;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Abhay Kulkarni
 */
public enum TaskPriority {

    HIGH(0), MODERATE(1), LOW(2);
    private static final Map<Integer, TaskPriority> lookup = new HashMap<Integer, TaskPriority>();

    static {
        for (TaskPriority TaskPriority : EnumSet.allOf(TaskPriority.class)) {
            lookup.put(TaskPriority.getCode(), TaskPriority);
        }
    }
    private int code;

    private TaskPriority(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static TaskPriority get(int code) {
        return lookup.get(code);
    }

    @Override
    public String toString() {
        String TaskPriority = super.toString();
        switch (super.ordinal()) {
            case 0:
                TaskPriority = "High";
                break;
            case 1:
                TaskPriority = "Moderate";
                break;
            case 2:
                TaskPriority = "Low";
                break;
        }
        return TaskPriority;
    }
}
