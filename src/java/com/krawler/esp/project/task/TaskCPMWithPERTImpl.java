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
import com.krawler.esp.project.project.PERTDiffStatus;
import com.krawler.esp.project.project.Project;
import com.krawler.esp.project.project.ProjectDAO;
import com.krawler.esp.project.project.ProjectDAOImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistribution;
import org.apache.commons.math.distribution.NormalDistributionImpl;

/**
 *
 * @author Abhay
 */
public class TaskCPMWithPERTImpl implements TaskCPMDAO {

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
    public TaskCPM getCPMValuesOfTask(Connection conn, String taskID) throws ServiceException {
        query = new StringBuilder();
        params = new ArrayList();

        String GET_PERT_DURATIONS = "SELECT t.*, optimistic, likely, pessimistic, expected, sd, variance "
                + "FROM cpawithpert c INNER JOIN proj_task t ON t.taskid = c.taskid WHERE t.taskid = ?";

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
        getTaskDAOImpl();
        List<TaskCPM> tasksCPM = new ArrayList<TaskCPM>();
        List<Task> tasks = new ArrayList<Task>();

        Map<String, Object> where = new HashMap<String, Object>();
        where.put("projectid", projectID);
        tasks = taskDao.getTasks(conn, Arrays.asList(new String[]{"*"}), where, "startdate is not null", null, Arrays.asList(new String[]{"startdate ASC"}));
        tasks = taskDao.filterTaskList(tasks, true, false, true);

        Map<String, Integer> defaultPERTDiff = getDefaultPERTDiff(conn, projectID);

        for (Task curTask : tasks) {
            TaskCPM curTaskCPM = new TaskCPM();
            if (isTaskCPMPresent(conn, curTask.getTaskID())) {
                curTaskCPM = getCPMValuesOfTask(conn, curTask.getTaskID());
            } else {
                List<TaskCPM> tempCPM = analysePERTWithDurDiff(conn, Arrays.asList(new Task[]{curTask}), defaultPERTDiff);
                curTaskCPM = tempCPM.get(0);
            }
            tasksCPM.add(curTaskCPM);
        }
        analysePERT(conn, tasksCPM);
        Collections.sort(tasksCPM);
        return tasksCPM;
    }

    private Map<String, Integer> getDefaultPERTDiff(Connection conn, String projectID) throws ServiceException {

        ProjectDAO projectDAO = new ProjectDAOImpl();
        Project p = projectDAO.getProjectById(conn, projectID);
        Map<String, Integer> map = null;

        if (p.getPertDiffStatus() == PERTDiffStatus.COMPANY || p.getPertDiffStatus() == PERTDiffStatus.TASK) {

            DbResults rs = DbUtil.executeQuery(conn, "SELECT pc.o_diff, pc.p_diff FROM pertdefaults_company pc "
                    + "INNER JOIN project p ON p.companyid = pc.companyid WHERE p.projectid = ?", projectID);
            map = new HashMap<String, Integer>();
            if (rs.next()) {
                map.put("optimisticdiff", rs.getInt("o_diff"));
                map.put("pessimisticdiff", rs.getInt("p_diff"));
            }

        } else if (p.getPertDiffStatus() == PERTDiffStatus.PROJECT) {

            map = new HashMap<String, Integer>();
            map.put("optimisticdiff", p.getOptimisticDiff());
            map.put("pessimisticdiff", p.getPessimisticDiff());
        }
        return map;
    }

    @Override
    public int getTotalCount() {
        return resultTotalCount;
    }

    @Override
    public boolean isTaskCPMPresent(Connection conn, String taskID) throws ServiceException {
        DbResults rs = DbUtil.executeQuery(conn, "SELECT 1 FROM cpawithpert WHERE taskid = ?", taskID);
        if (rs.size() == 1) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int setCPMValuesForTask(Connection conn, TaskCPM obj) throws ServiceException {
        query = new StringBuilder();
        params = new ArrayList();

        String SET_PERT_DURATIONS = "INSERT INTO cpawithpert (taskid, optimistic, likely, pessimistic, expected, sd, variance) "
                + "VALUES (?,?,?,?,?,?,?)";

        params.add(obj.getTask().getTaskID());
        params.add(obj.getOptimistic());
        params.add(obj.getLikely());
        params.add(obj.getPessimistic());
        params.add(obj.getExpected());
        params.add(obj.getSd());
        params.add(obj.getVariance());

        query.append(SET_PERT_DURATIONS);
        resultTotalCount = DbUtil.executeUpdate(conn, SET_PERT_DURATIONS.toString(), params.toArray());
        return resultTotalCount;
    }

    @Override
    public int updateCPMValuesForTask(Connection conn, TaskCPM obj) throws ServiceException {
        query = new StringBuilder();
        params = new ArrayList();

        String SET_PERT_DURATIONS = "UPDATE cpawithpert SET optimistic=?, likely=?, pessimistic=?, expected=?, sd=?, variance=? "
                + "WHERE taskid = ?";

        params.add(obj.getOptimistic());
        params.add(obj.getLikely());
        params.add(obj.getPessimistic());
        params.add(obj.getExpected());
        params.add(obj.getSd());
        params.add(obj.getVariance());
        params.add(obj.getTask().getTaskID());

        query.append(SET_PERT_DURATIONS);
        resultTotalCount = DbUtil.executeUpdate(conn, SET_PERT_DURATIONS.toString(), params.toArray());
        return resultTotalCount;
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
                    if (predTaskCpm != null) {
                        if (taskCpm.getEs() < predTaskCpm.getEf()) {
                            taskCpm.setEs(predTaskCpm.getEf());
                        }
                    }
                }
                if (max < taskCpm.getEs() + taskCpm.getExpected()) {
                    max = taskCpm.getEs() + taskCpm.getExpected();
                }
                taskCpm.setEf(taskCpm.getEs() + taskCpm.getExpected());
            }
        }
        tc.setEs(max);
        tc.setEf(max);
        tc.setLs(max);
        tc.setLf(max);

        // backward pass
        for (int i = taskCPMValues.size() - 2; i >= 0; i--) {
            TaskCPM taskCPM = taskCPMValues.get(i);
            Task task = tasks.get(i);
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
                taskCPM.setLs(taskCPM.getLf() - taskCPM.getExpected());
            }
        }
        tasks.remove(t);
        taskCPMValues.remove(tc);
        markCriticalPathTasks(taskCPMValues);
    }

    private void markCriticalPathTasks(List<TaskCPM> taskCPValues) {
        for (TaskCPM taskCPM : taskCPValues) {
            Task t = taskCPM.getTask();
            if (!t.isIsParent()) {
                double slack = taskCPM.getLs() - taskCPM.getEs();
                if (slack == 0) {
                    taskCPM.setIsCritical(true);
                } else {
                    taskCPM.setIsCritical(false);
                }
                taskCPM.setSlack(slack);
            }
        }
    }

    @Override
    public List<TaskCPM> calculateCP(Connection conn, String projectID) throws ServiceException {
        getTaskDAOImpl();
        List<TaskCPM> tasksCPM = getCPMValuesOfProject(conn, projectID);
        return calculateCP(conn, tasksCPM);
    }

    @Override
    public List<TaskCPM> calculateCP(Connection conn, List<TaskCPM> tasksCPM) throws ServiceException {
        performCPA(conn, tasksCPM);
        resultTotalCount = tasksCPM.size();
        Collections.sort(tasksCPM);
        return tasksCPM;
    }

    @Override
    public List<TaskCPM> analysePERT(Connection conn, List<TaskCPM> taskCPM) throws ServiceException {
        for (TaskCPM taskCPMtemp : taskCPM) {

            Task t = taskCPMtemp.getTask();
            double dur = SchedulingUtilities.getDurationInDays(t.getDuration());
            taskCPMtemp.setLikely(dur);

            double o = taskCPMtemp.getOptimistic();
            double p = taskCPMtemp.getPessimistic();
            double l = taskCPMtemp.getLikely();
            double e = (o + (l * 4) + p) / 6;
            e = checkNegative(e);
            taskCPMtemp.setExpected(e);
            e = (p - o) / 6;
            e = checkNegative(e);
            taskCPMtemp.setSd(e);
            taskCPMtemp.setVariance(Math.pow(e, 2));
        }
        resultTotalCount = taskCPM.size();
        return taskCPM;
    }

    @Override
    public List<TaskCPM> analysePERTWithDurDiff(Connection conn, List<Task> tasks, Map<String, Integer> map) throws ServiceException {
        List<TaskCPM> taskCPM = new ArrayList<TaskCPM>();
        int o = map.get("optimisticdiff");
        int p = map.get("pessimisticdiff");
        for (Task task : tasks) {
            if (task.getStartDate() != null) {
                TaskCPM curTaskCPM = new TaskCPM(task);

                double dur = SchedulingUtilities.getDurationInDays(task.getDuration());
                curTaskCPM.setLikely(dur);

                double newOpt = dur - o;
                newOpt = checkNegative(newOpt);
                double newPes = dur + p;
                if(task.isIsMilestone()){
                    newOpt = 0;
                    newPes = 0;
                }
                newOpt = SchedulingUtilities.getDurationInDays(Double.toString(newOpt));
                newPes = SchedulingUtilities.getDurationInDays(Double.toString(newPes));

                curTaskCPM.setOptimistic(newOpt);
                curTaskCPM.setPessimistic(newPes);

                taskCPM.add(curTaskCPM);
            }
        }
        analysePERT(conn, taskCPM);
        resultTotalCount = tasks.size();
        return taskCPM;
    }

    private double checkNegative(double val) {
        return (val < 0) ? 0 : val;
    }

    @Override
    public List<TaskCPM> getFreeSlackPerTask(Connection conn, String projectID) throws ServiceException {
        throw new UnsupportedOperationException("This operation in not yet supported in Critical Path Analysis with PERT");
    }

    @Override
    public double getProbability(Connection conn, String projectID, double desiredDuration, double sumOfExpected, double sumOfVariance) throws ServiceException {
        try {
            NormalDistribution nd = new NormalDistributionImpl(sumOfExpected, Math.sqrt(sumOfVariance));
            double z = nd.cumulativeProbability(desiredDuration);
            return z;
        } catch (MathException ex) {
            throw ServiceException.FAILURE("Math Exception while calculating probability :: " + ex.getMessage(), ex);
        }
    }
}
