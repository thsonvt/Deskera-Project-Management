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
import com.krawler.common.util.SchedulingUtilities;
import com.krawler.database.DbPool.Connection;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.esp.project.project.Project;
import com.krawler.esp.project.project.ProjectDAOImpl;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Abhay
 */
public class TaskCPMImpl implements TaskCPMDAO {

    private StringBuilder query;
    private List params;
    private int resultTotalCount;
    private Task taskObj;
    private TaskCPM taskCPMObj;
    private TaskDAO taskDao;

    private void getTaskDAOImpl() {
        taskDao = new TaskDAOImpl();
    }

    private TaskCPM loadTaskCPMObj(DbResults rs) throws ServiceException {
        taskCPMObj = new TaskCPM();
        getTaskDAOImpl();
        taskObj = taskDao.loadTask(rs);
        taskCPMObj.setTask(taskObj);
        if (rs.has("optimistic")) {
            taskCPMObj.setOptimistic(rs.getDouble("optimistic"));
        }
        if (rs.has("pessimistic")) {
            taskCPMObj.setPessimistic(rs.getDouble("pessimistic"));
        }
        if (rs.has("likely")) {
            taskCPMObj.setLikely(rs.getDouble("likely"));
        }
        if (rs.has("expected")) {
            taskCPMObj.setExpected(rs.getDouble("expected"));
        }
        if (rs.has("sd")) {
            taskCPMObj.setSd(rs.getDouble("sd"));
        }
        if (rs.has("variance")) {
            taskCPMObj.setVariance(rs.getDouble("variance"));
        }
        if (rs.has("es")) {
            taskCPMObj.setEs(rs.getDouble("es"));
        }
        if (rs.has("ef")) {
            taskCPMObj.setEf(rs.getDouble("ef"));
        }
        if (rs.has("ls")) {
            taskCPMObj.setLs(rs.getDouble("ls"));
        }
        if (rs.has("lf")) {
            taskCPMObj.setLf(rs.getDouble("lf"));
        }
        if (rs.has("slack")) {
            taskCPMObj.setSlack(rs.getDouble("slack"));
        }
        if (rs.has("iscritical")) {
            taskCPMObj.setIsCritical(rs.getBoolean("iscritical"));
        }
        return taskCPMObj;
    }

    private TaskCPM getTaskCPMValuesObject(Task task, List<TaskCPM> tasks) {
        Iterator ite = tasks.listIterator();
        while (ite.hasNext()) {
            TaskCPM temp = (TaskCPM) ite.next();
            if (temp.getTask().equals(task)) {
                return temp;
            }
        }
        return null;
    }

    @Override
    public List<TaskCPM> calculateCP(Connection conn, String projectID) throws ServiceException {
        getTaskDAOImpl();
        List<TaskCPM> tasksCPM = new ArrayList<TaskCPM>();
        List<Task> tasks = new ArrayList<Task>();
        Map<String, Object> where = new HashMap<String, Object>();
        where.put("projectid", projectID);
        tasks = taskDao.getTasks(conn, Arrays.asList(new String[]{"*"}), where, "startdate is not null", null, Arrays.asList(new String[]{"startdate ASC"}));
        tasks = taskDao.filterTaskList(tasks, true, false, true);
        for (Task task : tasks) {
            tasksCPM.add(new TaskCPM(task));
        }
        return calculateCP(conn, tasksCPM);
    }

    @Override
    public List<TaskCPM> calculateCP(Connection conn, List<TaskCPM> tasksCPM) throws ServiceException {
        performCPA(conn, tasksCPM);
        resultTotalCount = tasksCPM.size();
        Collections.sort(tasksCPM);
        return tasksCPM;
    }

    private void performCPA(Connection conn, List<TaskCPM> taskCPMValues) throws ServiceException {
        getTaskDAOImpl();
        double max = 0;

        List<Task> tasks = new ArrayList<Task>(taskCPMValues.size());
        for (TaskCPM tempTaskCPM : taskCPMValues) {
            tasks.add(tempTaskCPM.getTask());
        }

        // forward pass. Here, the list has to be sorted according to startdates of the tasks

        Task t = new Task("Finish", "dummyFinish", -1, "0", new Date(), new Date(), "");
        tasks.add(t);
        TaskCPM tc = new TaskCPM(t);
        taskCPMValues.add(tc);

        for (Task temp : tasks) {
            if (!temp.isIsParent()) {
                TaskCPM taskCpm = getTaskCPMValuesObject(temp, taskCPMValues);
                List<Task> preds = taskDao.getPredecessors(conn, temp.getTaskID());
                for (Task predTask : preds) {
                    TaskCPM predTaskCpm = getTaskCPMValuesObject(predTask, taskCPMValues);
                    if(predTaskCpm != null){
                        if (taskCpm.getEs() < predTaskCpm.getEf()) {
                            taskCpm.setEs(predTaskCpm.getEf());
                        }
                    }
                }
                if (max < taskCpm.getEs() + SchedulingUtilities.getDurationInDays(temp.getDuration())) {
                    max = taskCpm.getEs() + SchedulingUtilities.getDurationInDays(temp.getDuration());
                }
                taskCpm.setEf(taskCpm.getEs() + SchedulingUtilities.getDurationInDays(temp.getDuration()));
            }
        }
        tc.setEs(max);
        tc.setEf(max);
        tc.setLs(max);
        tc.setLf(max);

        // backward pass
        for (int i = taskCPMValues.size() - 2; i >= 0; i--) {
            TaskCPM taskCPM = taskCPMValues.get(i);
            Task task = taskCPM.getTask(); 
            if (!task.isIsParent()) {
                List<Task> succ = taskDao.getSuccessors(conn, task.getTaskID());
                if (succ.isEmpty()) {
                    succ.add(t);
                }
                for (Task succTask : succ) {
                    TaskCPM succTaskCpm = getTaskCPMValuesObject(succTask, taskCPMValues);
                    if (succTaskCpm != null) {
                        if (taskCPM.getLf() == 0) {
                            taskCPM.setLf(succTaskCpm.getLs());
                        } else if (taskCPM.getLf() > succTaskCpm.getLs()) {
                            taskCPM.setLf(succTaskCpm.getLs());
                        }
                    }
                }
                taskCPM.setLs(taskCPM.getLf() - SchedulingUtilities.getDurationInDays(task.getDuration()));
            }
        }

        tasks.remove(t);
        taskCPMValues.remove(tc);
        markCriticalPathTasks(conn, taskCPMValues);
    }

    public void markCriticalPathTasks(Connection conn, List<TaskCPM> taskCPValues) throws ServiceException {
        int[] nonWorkingDays = new int[]{};
        String[] cmpHolidays=null;
        if(taskCPValues != null && !taskCPValues.isEmpty()){
            String projID = taskCPValues.get(0).getTask().getProjectID();
            nonWorkingDays = SchedulingUtilities.getNonWorkWeekDays(conn, projID);
            Project project = new ProjectDAOImpl().getProject(conn, projID);
            cmpHolidays = SchedulingUtilities.getCompHolidays(conn, project.getCompanyID());
        }
        for (TaskCPM taskCPM : taskCPValues) {
            Task t = taskCPM.getTask();
            if (!t.isIsParent()) {
                double slack = taskCPM.getLs() - taskCPM.getEs();
                if (slack == 0) {
                    taskCPM.setIsCritical(true);
                    taskCPM.setTaskStatus(getCriticalPathTaskStatus(taskCPM,nonWorkingDays, cmpHolidays));
                } else {
                    taskCPM.setIsCritical(false);
                }
                taskCPM.setSlack(slack);
            }
        }
    }

    private String getCriticalPathTaskStatus(TaskCPM taskCPM, int[] nonWorkingDays, String[] cmpHolidays) throws ServiceException {
        Task task = taskCPM.getTask();
        TaskDAO tdao = new TaskDAOImpl();
        TaskProgressStatus tps = tdao.getTaskProgressStatus(task, nonWorkingDays, cmpHolidays);
        String status ="";
        if(tps != null)
            status = tps.toString();
        return status;
    }
    
    @Override
    public int getTotalCount() {
        return resultTotalCount;
    }

    @Override
    public TaskCPM getCPMValuesOfTask(Connection conn, String taskID) throws ServiceException {
        query = new StringBuilder();
        params = new ArrayList();

        String GET_PERT_DURATIONS = "SELECT taskid, es, ef, ls, lf, slack FROM cpadata WHERE taskid = ?";

        params.add(taskID);
        query.append(GET_PERT_DURATIONS);

        DbResults rs = DbUtil.executeQuery(conn, GET_PERT_DURATIONS.toString(), params.toArray());
        if (rs.next()) {
            loadTaskCPMObj(rs);
        }
        resultTotalCount = 1;
        return taskCPMObj;
    }

    @Override
    public List<TaskCPM> getCPMValuesOfProject(Connection conn, String projectID) throws ServiceException {
        query = new StringBuilder();
        params = new ArrayList();
        List<TaskCPM> tasks = new ArrayList<TaskCPM>();

        String GET_PERT_DURATIONS = "SELECT t.*, es, ef, ls, lf, slack "
                + "FROM cpadata c INNER JOIN proj_task t on t.taskid = c.taskid WHERE t.projectid = ?";

        params.add(projectID);
        query.append(GET_PERT_DURATIONS);

        DbResults rs = DbUtil.executeQuery(conn, GET_PERT_DURATIONS.toString(), params.toArray());
        while (rs.next()) {
            loadTaskCPMObj(rs);
            tasks.add(taskCPMObj);
        }
        Collections.sort(tasks);
        resultTotalCount = tasks.size();
        return tasks;
    }

    @Override
    public int setCPMValuesForTask(Connection conn, TaskCPM obj) throws ServiceException {
        query = new StringBuilder();
        params = new ArrayList();

        String SET_PERT_DURATIONS = "INSERT INTO cpadata (taskid, es, ef, ls, lf, slack) "
                + "VALUES (?,?,?,?,?,?)";

        params.add(obj.getTask().getTaskID());
        params.add(obj.getEs());
        params.add(obj.getEf());
        params.add(obj.getLs());
        params.add(obj.getLf());
        params.add(obj.getSlack());

        query.append(SET_PERT_DURATIONS);
        resultTotalCount = DbUtil.executeUpdate(conn, SET_PERT_DURATIONS.toString(), params.toArray());
        return resultTotalCount;
    }

    @Override
    public int updateCPMValuesForTask(Connection conn, TaskCPM obj) throws ServiceException {
        query = new StringBuilder();
        params = new ArrayList();

        String SET_PERT_DURATIONS = "UPDATE cpadata SET es=?, ef=?, ls=?, lf=?, slack=? "
                + "WHERE taskid = ?";

        params.add(obj.getEs());
        params.add(obj.getEf());
        params.add(obj.getLs());
        params.add(obj.getLf());
        params.add(obj.getSlack());
        params.add(obj.getTask().getTaskID());

        query.append(SET_PERT_DURATIONS);
        resultTotalCount = DbUtil.executeUpdate(conn, SET_PERT_DURATIONS.toString(), params.toArray());
        return resultTotalCount;
    }

    @Override
    public boolean isTaskCPMPresent(Connection conn, String taskID) throws ServiceException {
        DbResults rs = DbUtil.executeQuery(conn, "SELECT 1 FROM cpadata WHERE taskid = ?", taskID);
        if (rs.size() == 1) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<TaskCPM> analysePERT(Connection conn, List<TaskCPM> taskCPM) throws ServiceException {
        throw new UnsupportedOperationException("This operation in not supported in Critical Path Analysis without PERT");
    }

    @Override
    public List<TaskCPM> analysePERTWithDurDiff(Connection conn, List<Task> tasks, Map<String, Integer> map) throws ServiceException {
        throw new UnsupportedOperationException("This operation in not supported in Critical Path Analysis without PERT");
    }

    @Override
    public List<TaskCPM> getFreeSlackPerTask(Connection conn, String projectID) throws ServiceException {
        getTaskDAOImpl();
        List<Task> tasks = new ArrayList<Task>();
        Date maxEndDate = null, ed = null;
        List<TaskCPM> taskCPMValues = calculateCP(conn, projectID);
        for (TaskCPM taskcpm : taskCPMValues) {
            ed = taskcpm.getTask().getEndDate();
            if (maxEndDate == null || ed.after(maxEndDate)) {
                maxEndDate = ed;
            }
            tasks.add(taskcpm.getTask());
        }
        for (Task task : tasks) {
            if (!task.isIsParent() && !task.isIsMilestone()) {
                TaskCPM taskCPM = getTaskCPMValuesObject(task, taskCPMValues);
                List<Task> succ = taskDao.getSuccessors(conn, task.getTaskID());
                Date endDate = task.getEndDate();
                Date minStartDate = null;
                if (!succ.isEmpty()) {
                    minStartDate = succ.get(0).getStartDate();
                    for (Task succTask : succ) {
                        Date startDate = succTask.getStartDate();
                        if (startDate.before(minStartDate) || startDate.equals(minStartDate)) {
                            minStartDate = startDate;
                        }
                    }
                } else {
                    Calendar c = Calendar.getInstance();
                    c.setTime(maxEndDate);
                    c.add(Calendar.DATE, 1);
                    minStartDate = c.getTime();
                }
                int freeSlack = SchedulingUtilities.calculateWorkingDays(conn, projectID, endDate, minStartDate);
                freeSlack = (freeSlack <= 1) ? 0 : freeSlack - 2;
                taskCPM.setSlack(freeSlack);

                freeSlack = SchedulingUtilities.getDaysDiff(minStartDate, endDate);
                taskCPM.setExpected(freeSlack);
            }
        }
        resultTotalCount = taskCPMValues.size();
        return taskCPMValues;
    }

    @Override
    public double getProbability(Connection conn, String projectID, double desiredDuration, double sumOfExpected, double sumOfVariance) throws ServiceException {
        throw new UnsupportedOperationException("This operation in not supported in Critical Path Analysis without PERT");
    }
}
