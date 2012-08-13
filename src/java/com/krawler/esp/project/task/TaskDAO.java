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
import com.krawler.database.DbResults;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Abhay
 */
public interface TaskDAO {

    int getTotalCount();

    Task loadTask(DbResults rs) throws ServiceException;

    Task getTask(Connection conn, String taskID) throws ServiceException;

    TaskProgressStatus getTaskProgressStatus(Task task, int[] nonWorkingDays, String[] cmpHolidays) throws ServiceException;

    List<Task> getTasks(Connection conn, List<String> selectColumnNames, Map<String, Object> where, String extraWhereString,
            Map<String, Object> pagingSearch, List<String> orderByColumns) throws ServiceException;

    List<Task> getTasks(Connection conn, String projectID, int offset, int limit) throws ServiceException;

    List<Task> getUserTasks(Connection conn, String projectID, String userID, int offset, int limit) throws ServiceException;

    List<Task> getBaselineTasks(Connection conn, String projectID, int offset, int limit) throws ServiceException;

    List<Task> getMilestones(Connection conn, String projectID, Map<String, Object> pagingSearchParams) throws ServiceException;

    List<Task> filterTaskList(List<Task> tasks, boolean filterParentTasks, boolean filterMilestones, boolean filterNullTasks);

    List<Task> getPredecessors(Connection conn, String taskID) throws ServiceException;

    List<Task> getSuccessors(Connection conn, String taskID) throws ServiceException;

    List<Task> getOverdueTasks(Connection conn, String projectID) throws ServiceException;

    List<Task> getOverdueTasksOfMember(Connection conn, String projectID, String userID) throws ServiceException;

    int updateTaskProgress(Connection conn, String taskID, int progress) throws ServiceException;
}
