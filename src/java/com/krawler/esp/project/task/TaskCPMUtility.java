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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Abhay
 */
public final class TaskCPMUtility{

    public static TaskCPMDAO getTaskCPM(int TYPE) throws InstantiationException, IllegalAccessException {

        if (TYPE <= 0) {
            throw new IllegalArgumentException("Incorrect type specified : " + TYPE);
        }

        Class<? extends TaskCPMDAO> fileClass = TYPE_MAP.get(TYPE);
        if (fileClass == null) {
            throw new IllegalArgumentException("Cannot create object of type : " + TYPE);
        }

        TaskCPMDAO taskDao = fileClass.newInstance();

        return (taskDao);
    }

    public static int getCPMWithoutPERTTypeID() {
        return CPM_WITHOUT_PERT;
    }
    
    public static int getCPMWithPERTTypeID() {
        return CPM_WITH_PERT;
    }
    
    private static final int CPM_WITHOUT_PERT = 1;
    private static final int CPM_WITH_PERT = 2;
    private static final Map<Integer, Class<? extends TaskCPMDAO>> TYPE_MAP = new HashMap<Integer, Class<? extends TaskCPMDAO>>();

    static {
        TYPE_MAP.put(CPM_WITHOUT_PERT, TaskCPMImpl.class);
        TYPE_MAP.put(CPM_WITH_PERT, TaskCPMWithPERTImpl.class);
    }
}
