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
 * @author krawler
 */
public enum TaskProgressStatus {

    COMPLETED(1), IN_PROGRESS(2), NEED_ATTENTION(3), OVERDUE(4), FUTURE(5);
    private static final Map<Integer, TaskProgressStatus> lookup = new HashMap<Integer, TaskProgressStatus>();

    static {
        for (TaskProgressStatus taskProgressStatus : EnumSet.allOf(TaskProgressStatus.class)) {
            lookup.put(taskProgressStatus.getCode(), taskProgressStatus);
        }
    }
    private int code;

    private TaskProgressStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static TaskProgressStatus get(int code) {
        return lookup.get(code);
    }

    @Override
    public String toString() {
        String taskProgressStatus;
        switch (this.code) {
            case 1:
                taskProgressStatus = "completed";
                break;
            case 2:
                taskProgressStatus = "inprogress";
                break;
            case 3:
                taskProgressStatus = "needattention";
                break;
            case 4:
                taskProgressStatus = "overdue";
                break;
            case 5:
                taskProgressStatus = "future";
                break;
            default:
                taskProgressStatus = "";
                break;
        }
        return taskProgressStatus;
    }
}
