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
package com.krawler.esp.project.checklist;

import com.krawler.common.service.ServiceException;
import com.krawler.database.DbPool.Connection;
import com.krawler.esp.handlers.todolist;
import com.krawler.esp.project.task.Task;
import com.krawler.esp.project.task.TaskDAO;
import com.krawler.esp.project.task.TaskDAOImpl;
import com.krawler.esp.project.task.TaskPriority;
import com.krawler.utils.json.base.JSONArray;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Abhay Kulkarni
 */
public class CheckListManager {

    private CheckListCommonRetriever retriever;
    private TaskDAO taskDAO;

    public void attachCheckListWithTask(Connection conn, String taskID, String checkListID, String loginUserID) throws ServiceException {

        retriever = new CheckListCommonRetriever();
        taskDAO = new TaskDAOImpl();

        CheckList cl = retriever.getCheckList(conn, checkListID);
        Task t = taskDAO.getTask(conn, taskID);

        String mappingID = retriever.mapCheckListWithTask(conn, checkListID, taskID, loginUserID);

        int success = todolist.createCheckListToDoGroup(conn, mappingID, cl.getCheckListName(), t.getProjectID(), t);
        if (success == 1) {

            List<CheckListTask> cltasks = retriever.getCheckListTasks(conn, checkListID);
            for (CheckListTask clt : cltasks) {

                todolist.createCheckListToDoTask(conn, mappingID, clt.getcTaskName(),
                        TaskPriority.get(t.getPriority()).toString(), t.getProjectID());
            }
            taskDAO.updateTaskProgress(conn, taskID, 0);
        }
    }

    public void removeAssociatedCheckListFromTask(Connection conn, String taskID, String checkListID) throws ServiceException {

        retriever = new CheckListCommonRetriever();
        taskDAO = new TaskDAOImpl();

        TaskCheckListMapping tcm = retriever.getTaskCheckListMapping(conn, checkListID, taskID);

        retriever.removeTaskCheckListMapping(conn, checkListID, taskID);

        todolist.deleteToDoTask(conn, tcm.getMappingID());

        taskDAO.updateTaskProgress(conn, taskID, 0);
    }

    public boolean isAssociatedWithAnyTask(Connection conn, String checkListID) throws ServiceException {

        retriever = new CheckListCommonRetriever();

        List<TaskCheckListMapping> tclmList = retriever.getCheckListMappings(conn, checkListID);
        return !tclmList.isEmpty();
    }

    public int updateAssociatedTaskProgress(Connection conn, String toDoID, boolean isCheckListMappingID) throws ServiceException {

        int perc = 0;
        retriever = new CheckListCommonRetriever();
        taskDAO = new TaskDAOImpl();

        String mappingID = "";

        mappingID = isCheckListMappingID ? toDoID : todolist.getParentID(conn, toDoID);

        if (mappingID != null && !mappingID.equals("")) {

            TaskCheckListMapping tclm = retriever.getTaskCheckListMapping(conn, mappingID);
            List<String> childs = Arrays.asList(todolist.getChilds(conn, mappingID));

            int total = 0;
            for (String todo : childs) {
                total += todolist.getToDoStatus(conn, todo);
            }

            perc = Math.round((total * 100) / childs.size());
            taskDAO.updateTaskProgress(conn, tclm.getTaskID(), perc);
        }
        return perc;
    }

    public List<CheckList> getCheckLists(Connection conn, String companyID) throws ServiceException {
        retriever = new CheckListCommonRetriever();
        return retriever.getCheckLists(conn, companyID);
    }

    public String addCheckList(Connection conn, String companyID, String checkListName, String description, String mappedBy) throws ServiceException {
        retriever = new CheckListCommonRetriever();
        return retriever.createCheckList(conn, checkListName, description, mappedBy, mappedBy, companyID);
    }

    public String addCheckListTask(Connection conn, String checkListID, String taskName) throws ServiceException {

        retriever = new CheckListCommonRetriever();
        taskDAO = new TaskDAOImpl();

        String ID = retriever.createCheckListTask(conn, checkListID, taskName);
        List<TaskCheckListMapping> checkListMappings = retriever.getCheckListMappings(conn, checkListID);

        for (TaskCheckListMapping mapping : checkListMappings) {

            Task t = taskDAO.getTask(conn, mapping.getTaskID());
            int temp = todolist.createCheckListToDoTask(conn, mapping.getMappingID(), taskName,
                    TaskPriority.get(t.getPriority()).toString(), t.getProjectID());

            conn.commit();
            updateAssociatedTaskProgress(conn, mapping.getMappingID(), true);

        }
        return ID;
    }

    public int removeCheckList(Connection conn, String checkListID) throws ServiceException {
        retriever = new CheckListCommonRetriever();
        return retriever.removeCheckList(conn, checkListID);
    }

    public JSONArray getAssociatedCheckListTasks(Connection conn, String checkListID, String taskID) throws ServiceException {

        retriever = new CheckListCommonRetriever();
        taskDAO = new TaskDAOImpl();

        TaskCheckListMapping tclm = retriever.getTaskCheckListMapping(conn, checkListID, taskID);
        Task t = taskDAO.getTask(conn, taskID);

        JSONArray ja = new JSONArray();
        if (tclm != null) {
            ja = todolist.getCheckListToDos(conn, tclm.getMappingID(), t.getProjectID());
        }

        return ja;
    }

    public CheckList getAssociatedCheckList(Connection conn, String taskID) throws ServiceException {

        retriever = new CheckListCommonRetriever();

        TaskCheckListMapping tclm = retriever.getTaskCheckListMapping(conn, null, taskID);
        CheckList cl = null;
        if (tclm != null) {
            cl = retriever.getCheckList(conn, tclm.getCheckListID());
        }
        return cl;
    }

    public int updateAssociatedCheckListTask(Connection conn, String taskID, int status) throws ServiceException {

        todolist.updateToDoTaskStatus(conn, taskID, status);
        return updateAssociatedTaskProgress(conn, taskID, false);
    }

    public void calculateAllTasksProgresses(Connection conn, String companyID) throws ServiceException {

        retriever = new CheckListCommonRetriever();

        List<CheckList> checkLists = retriever.getCheckLists(conn, companyID);

        for (CheckList checkList : checkLists) {

            List<TaskCheckListMapping> checkListMappings = retriever.getCheckListMappings(conn, checkList.getCheckListID());
            for (TaskCheckListMapping mapping : checkListMappings) {

                updateAssociatedTaskProgress(conn, mapping.getMappingID(), true);
            }
        }
    }
}
