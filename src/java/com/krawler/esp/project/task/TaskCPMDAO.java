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

import com.krawler.common.service.ServiceException;
import com.krawler.database.DbPool.Connection;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Abhay
 */
public interface TaskCPMDAO {

    // Common methods for CPM
    TaskCPM getCPMValuesOfTask(Connection conn, String taskID) throws ServiceException;

    List<TaskCPM> getCPMValuesOfProject(Connection conn, String projectID) throws ServiceException;

    int setCPMValuesForTask(Connection conn, TaskCPM obj) throws ServiceException;

    int updateCPMValuesForTask(Connection conn, TaskCPM obj) throws ServiceException;

    List<TaskCPM> calculateCP(Connection conn, String projectID) throws ServiceException;

    List<TaskCPM> calculateCP(Connection conn, List<TaskCPM> tasksCPM) throws ServiceException;

    List<TaskCPM> analysePERT(Connection conn, List<TaskCPM> taskCPM) throws ServiceException;

    List<TaskCPM> analysePERTWithDurDiff(Connection conn, List<Task> tasks, Map<String, Integer> defaultPERTDiff) throws ServiceException;

    List<TaskCPM> getFreeSlackPerTask(Connection conn, String projectID) throws ServiceException;

    double getProbability(Connection conn, String projectID, double desiredDuration, double sumOfExpected, double sumOfVariance) throws ServiceException;

    int getTotalCount();

    boolean isTaskCPMPresent(Connection conn, String taskID) throws ServiceException;
}
