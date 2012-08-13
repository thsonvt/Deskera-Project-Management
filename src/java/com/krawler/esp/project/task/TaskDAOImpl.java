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
import com.krawler.common.util.StringUtil;
import com.krawler.database.DbPool.Connection;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Abhay
 */
public class TaskDAOImpl implements TaskDAO {

    private StringBuilder query;
    private List params;
    private int resultTotalCount;
    private Task task;

    @Override
    public Task loadTask(DbResults rs) throws ServiceException {
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            task = new Task();
            task.setTaskID(rs.getString("taskid"));
            task.setTaskName(rs.getString("taskname"));
            task.setTaskIndex(rs.getInt("taskindex"));
            if (rs.getObject("startdate") != null) {
                Date dt = df.parse(rs.getObject("startdate").toString());
                task.setStartDate(dt);
                dt = df.parse(rs.getObject("enddate").toString());
                task.setEndDate(dt);
                if (rs.has("actualstartdate") && rs.getObject("actualstartdate") != null) {
                    dt = df.parse(rs.getObject("actualstartdate").toString());
                    task.setActualStartDate(dt);
                }
                task.setDuration(rs.getString("duration"));
                if (rs.has("actualduration") && rs.getObject("actualduration") != null) {
                    task.setActualDuration(rs.getObject("actualduration").toString());
                }
                task.setDurationType(rs.getString("duration"));
            }
            if (rs.has("percentcomplete")) {
                task.setPercentComplete(rs.getInt("percentcomplete"));
            }
            if (rs.has("priority")) {
                task.setPriority(rs.getInt("priority"));
            }
            if (rs.has("projectid") && !StringUtil.isNullOrEmpty(rs.getString("projectid"))) {
                task.setProjectID(rs.getString("projectid"));
            }
            if (rs.has("notes") && !StringUtil.isNullOrEmpty(rs.getString("notes"))) {
                task.setNotes(StringUtil.isNullOrEmpty(rs.getString("notes")) ? "" : rs.getString("notes"));
            }
            if (rs.has("parent") && !StringUtil.isNullOrEmpty(rs.getString("parent"))) {
                task.setParent(rs.getString("parent"));
            }
            if (rs.has("level")) {
                task.setLevel(Integer.parseInt(rs.getString("level")));
            }
            if (rs.has("isparent")) {
                task.setIsParent(rs.getBoolean("isparent"));
            }
            return task;
        } catch (ParseException e) {
            throw ServiceException.FAILURE("Parse Exception while creating Task Object in getTask:: " + e.getMessage(), e);
        }
    }

    @Override
    public int getTotalCount() {
        return resultTotalCount;
    }

    @Override
    public Task getTask(Connection conn, String taskID) throws ServiceException {
        query = new StringBuilder();
        params = new ArrayList();

        String GET_TASK = "SELECT taskid, taskindex, taskname, duration, startdate, enddate, projectid, "
                + "parent, level, actualstartdate, actualduration, percentcomplete, notes, priority, isparent "
                + "FROM proj_task WHERE taskid = ?";

        params.add(taskID);
        query.append(GET_TASK);

        DbResults rs = DbUtil.executeQuery(conn, query.toString(), params.toArray());
        if (rs.next()) {
            loadTask(rs);
        }
        resultTotalCount = 1;
        return task;
    }

    @Override
    public List<Task> getTasks(Connection conn, String projectID, int offset, int limit) throws ServiceException {
        query = new StringBuilder();
        params = new ArrayList();
        List<Task> tasks = new ArrayList<Task>();

        String GET_TASKS = "SELECT taskid, projectid, taskindex as taskindex, taskname, duration, startdate, enddate, "
                + "parent, level, actualstartdate, actualduration, percentcomplete, notes, priority, isparent "
                + "FROM proj_task WHERE projectid = ? ORDER BY taskindex LIMIT ? OFFSET ?";

        if (limit == -1) {
            GET_TASKS = "SELECT taskid, projectid, taskindex as taskindex, taskname, duration, startdate, enddate, "
                    + "parent, level, actualstartdate, actualduration, percentcomplete, notes, priority, isparent "
                    + "FROM proj_task WHERE projectid = ? ORDER BY taskindex";
            params.add(projectID);
        } else {
            params.add(projectID);
            params.add(limit);
            params.add(offset);
        }
        query.append(GET_TASKS);

        DbResults rs = DbUtil.executeQuery(conn, query.toString(), params.toArray());
        while (rs.next()) {
            loadTask(rs);
            tasks.add(task);
        }
        resultTotalCount = tasks.size();
        return tasks;
    }

    @Override
    public List<Task> getTasks(Connection conn, List<String> selectColumnNames, Map<String, Object> where, String extraWhereString, Map<String, Object> pagingSearch, List<String> orderByColumns) throws ServiceException {
        query = new StringBuilder();
        params = new ArrayList();
        Iterator ite = null;
        List<Task> tasks = new ArrayList<Task>();
        String searchString = "";
        String[] searchStrObj = new String[]{"taskname"};
        int limit = 0, offset = 0;

        query.append("SELECT ");

        if (selectColumnNames.isEmpty()) {
            query.append("* ");
        } else {
            ite = selectColumnNames.listIterator();
            while (ite.hasNext()) {
                String colName = (String) ite.next();
                query.append(colName.concat(", "));
            }
            query.replace(query.lastIndexOf(","), query.lastIndexOf(",") + 1, "");
        }

        query.append(" FROM proj_task ");

        if (where != null) {
            if (!where.isEmpty()) {
                query.append("WHERE ");
                ite = where.entrySet().iterator();
                while (ite.hasNext()) {
                    Map.Entry entry = (Map.Entry) ite.next();
                    String colName = entry.getKey().toString();
                    String colValue = (String) where.get(colName);
                    query.append(colName.concat(" = "));
                    query.append("'".concat(colValue.concat("'").concat(" AND ")));
                }
                query.replace(query.lastIndexOf("AND"), query.lastIndexOf("AND") + 3, "");
            }
        }
        if (!StringUtil.isNullOrEmpty(extraWhereString)) {
            if (!query.toString().contains("WHERE")) {
                query.append("WHERE ".concat(extraWhereString));
            } else {
                query.append("AND ".concat(extraWhereString));
            }
        }

        if (pagingSearch != null) {
            if (!pagingSearch.isEmpty()) {
                query.append(" AND ");
                if (pagingSearch.containsKey("ss")) {
                    searchString = (String) pagingSearch.get("ss");
                }
                if (pagingSearch.containsKey("limit")) {
                    limit = (Integer) pagingSearch.get("limit");
                }
                if (pagingSearch.containsKey("offset")) {
                    offset = (Integer) pagingSearch.get("offset");
                }
            }
        }
        if (searchString.compareTo("") != 0 && !query.toString().contains("WHERE")) {
            query.append(" WHERE ");
        }
        searchString = StringUtil.getSearchString(searchString, "AND", searchStrObj);
        query.append(searchString);

        DbResults rs = DbUtil.executeQuery(conn, query.toString());
        resultTotalCount = rs.size();

        if (orderByColumns != null) {
            if (!orderByColumns.isEmpty()) {
                query.append(" ORDER BY ");
                ite = orderByColumns.listIterator();
                while (ite.hasNext()) {
                    String colName = (String) ite.next();
                    query.append(colName.concat(", "));
                }
                query.replace(query.lastIndexOf(","), query.lastIndexOf(",") + 1, "");
            }
        }

        if (limit != 0) {
            query.append(" LIMIT ".concat(String.valueOf(limit)).concat(" OFFSET ").concat(String.valueOf(offset)));
        }

        rs = DbUtil.executeQuery(conn, query.toString());
        while (rs.next()) {
            loadTask(rs);
            tasks.add(task);
        }
        resultTotalCount = tasks.size();
        return tasks;
    }

    @Override
    public List<Task> getUserTasks(Connection conn, String projectID, String userID, int offset, int limit) throws ServiceException {
        query = new StringBuilder();
        params = new ArrayList();
        List<Task> tasks = new ArrayList<Task>();

        String GET_TASKS = "SELECT taskid, taskindex as taskindex, taskname, duration, startdate, enddate, "
                + "parent, level, actualstartdate, actualduration, percentcomplete, notes, priority, isparent "
                + "FROM proj_task WHERE projectid = ? AND taskid IN "
                + " (SELECT taskid FROM proj_taskresourcemapping inner join userlogin on userlogin.userid = proj_taskresourcemapping.resourceid where userlogin.isactive = true and resourceid = ?) "
                + "ORDER BY taskindex LIMIT ? OFFSET ?";

        if (limit == -1) {
            GET_TASKS = "SELECT taskid, taskindex as taskindex, taskname, duration, startdate, enddate, "
                    + "parent, level, actualstartdate, actualduration, percentcomplete, notes, priority, isparent, workholidays "
                    + "FROM proj_task WHERE projectid = ? AND taskid IN "
                    + "(SELECT taskid FROM proj_taskresourcemapping inner join userlogin on userlogin.userid = proj_taskresourcemapping.resourceid where userlogin.isactive = true and resourceid = ?)"
                    + " ORDER BY taskindex";
            params.add(projectID);
            params.add(userID);
        } else {
            params.add(projectID);
            params.add(userID);
            params.add(limit);
            params.add(offset);
        }
        query.append(GET_TASKS);

        DbResults rs = DbUtil.executeQuery(conn, query.toString(), params.toArray());
        while (rs.next()) {
            loadTask(rs);
            tasks.add(task);
        }
        resultTotalCount = tasks.size();
        return tasks;
    }

    @Override
    public List<Task> getBaselineTasks(Connection conn, String baselineID, int offset, int limit) throws ServiceException {
        query = new StringBuilder();
        params = new ArrayList();
        List<Task> tasks = new ArrayList<Task>();

        String GET_TASKS = "SELECT taskid, taskindex as taskindex, taskname, duration, startdate, enddate, "
                + "parent, level, actualstartdate, actualduration, percentcomplete, notes, priority, isparent "
                + "FROM proj_baselinedata WHERE baselineid = ? ORDER BY taskindex LIMIT ? OFFSET ?";

        if (limit == -1) {
            GET_TASKS = "SELECT taskid, taskindex as taskindex, taskname, duration, startdate, enddate, "
                    + "parent, level, actualstartdate, actualduration, percentcomplete, notes, priority, isparent "
                    + "FROM proj_baselinedata WHERE baselineid = ? ORDER BY taskindex";
            params.add(baselineID);
        } else {
            params.add(baselineID);
            params.add(limit);
            params.add(offset);
        }
        query.append(GET_TASKS);

        DbResults rs = DbUtil.executeQuery(conn, query.toString(), params.toArray());
        while (rs.next()) {
            loadTask(rs);
            tasks.add(task);
        }
        resultTotalCount = tasks.size();
        return tasks;
    }

    @Override
    public List<Task> filterTaskList(List<Task> tasks, boolean filterParentTasks, boolean filterMilestones, boolean filterNullTasks) {
        List<Task> t = new ArrayList<Task>();
        Iterator ite = tasks.listIterator();
        while (ite.hasNext()) {
            boolean add = false;
            Task tempTask = (Task) ite.next();

            if (tempTask != null && tempTask.getStartDate() != null) {

                if (!filterParentTasks && !filterMilestones) {
                    add = true;
                } else if (filterParentTasks) {
                    if (!tempTask.isIsParent()) {
                        if (!filterMilestones || (filterMilestones && !tempTask.isIsMilestone())) {
                            add = true;
                        }
                    }
                } else if (filterMilestones) {
                    if (!tempTask.isIsMilestone()) {
                        if (!filterParentTasks || (filterParentTasks && !tempTask.isIsParent())) {
                            add = true;
                        }
                    }
                }
            } else {
                if (!filterNullTasks) {
                    add = true;
                }
            }
            if (add) {
                t.add(tempTask);
            }
        }
        return t;
    }

    @Override
    public List<Task> getPredecessors(Connection conn, String taskID) throws ServiceException {
        query = new StringBuilder();
        params = new ArrayList();
        List<Task> tasks = new ArrayList<Task>();

        String GET_PREDECESSORS = "SELECT fromtask FROM proj_tasklinks WHERE totask = ?";

        params.add(taskID);
        query.append(GET_PREDECESSORS);

        DbResults rs = DbUtil.executeQuery(conn, query.toString(), params.toArray());
        while (rs.next()) {
            taskID = rs.getString("fromtask");
            tasks.add(getTask(conn, taskID));
        }
        return tasks;
    }

    @Override
    public List<Task> getSuccessors(Connection conn, String taskID) throws ServiceException {
        query = new StringBuilder();
        params = new ArrayList();
        List<Task> tasks = new ArrayList<Task>();

        String GET_PREDECESSORS = "SELECT totask FROM proj_tasklinks WHERE fromtask = ?";

        params.add(taskID);
        query.append(GET_PREDECESSORS);

        DbResults rs = DbUtil.executeQuery(conn, query.toString(), params.toArray());
        while (rs.next()) {
            taskID = rs.getString("totask");
            tasks.add(getTask(conn, taskID));
        }
        return tasks;
    }

    @Override
    public List<Task> getOverdueTasks(Connection conn, String projectID) throws ServiceException {
        List<Task> tasks = new ArrayList<Task>();
        query = new StringBuilder();
        params = new ArrayList();

        java.sql.Date DateVal = new java.sql.Date(new java.util.Date().getTime());

        String GET_TASKS = "select taskid, taskindex, taskname, duration, startdate, enddate, percentcomplete from proj_task "
                + "where isparent = 0 and actualduration not in ('0', '0.0', '0.00', '0d', '0.0d', '0.00d', '0.00days', '0 days', '0.0days', '0.00h') "
                + "and duration not in ('0', '0.0', '0.00', '0d', '0.0d', '0.00d', '0.00days', '0 days', '0.0days', '0.00h') and "
                + "percentcomplete < 100 and enddate < ? and projectid = ? order by taskindex";

        query.append(GET_TASKS);
        params.add(DateVal);
        params.add(projectID);

        DbResults rs = DbUtil.executeQuery(conn, query.toString(), params.toArray());
        while (rs.next()) {
            loadTask(rs);
            tasks.add(task);
        }
        resultTotalCount = tasks.size();
        return tasks;
    }

    @Override
    public List<Task> getOverdueTasksOfMember(Connection conn, String projectID, String userID) throws ServiceException {
        List<Task> tasks = new ArrayList<Task>();
        java.sql.Date DateVal = new java.sql.Date(new java.util.Date().getTime());

        String GET_TASKS = "select p.taskid, p.taskindex, p.taskname, p.duration, p.startdate, p.enddate, p.percentcomplete from proj_task p "
                + "inner join proj_taskresourcemapping  on p.taskid = proj_taskresourcemapping.taskid "
                + "where isparent = 0 and actualduration not in ('0', '0.0', '0.00', '0d', '0.0d', '0.00d', '0.00days', '0 days', '0.0days', '0.00h') "
                + "and duration not in ('0', '0.0', '0.00', '0d', '0.0d', '0.00d', '0.00days', '0 days', '0.0days', '0.00h') and "
                + "percentcomplete < 100 and enddate < ? and projectid = ? and  proj_taskresourcemapping.resourceid=? order by taskindex";

        query = new StringBuilder();
        query.append(GET_TASKS);

        params = new ArrayList();
        params.add(DateVal);
        params.add(projectID);
        params.add(userID);

        DbResults rs = DbUtil.executeQuery(conn, query.toString(), params.toArray());
        while (rs.next()) {
            loadTask(rs);
            tasks.add(task);
        }
        resultTotalCount = tasks.size();
        return tasks;
    }

    @Override
    public List<Task> getMilestones(Connection conn, String projectID, Map<String, Object> pagingSearchParams) throws ServiceException {
        query = new StringBuilder();
        params = new ArrayList();
        List<Task> tasks = new ArrayList<Task>();

        String GET_TASKS = "SELECT * FROM proj_task WHERE duration "
                + "IN('0', '0.0', '0.00', '0d', '0.0d', '0.00d', '0.00days', '0 days', '0.0days', '0.00h') AND projectid = '"+projectID+"' ";

        String limitClause = "", ss = "";
        int offset = 0, limit = Integer.MAX_VALUE;

        if(pagingSearchParams != null){
            if (pagingSearchParams.containsKey("ss")) {
                ss = (String) pagingSearchParams.get("ss");
                ss = StringUtil.getSearchString(ss, "AND", new String[]{"proj_task.taskname"});
            }
            if (pagingSearchParams.containsKey("limit")) {
                limit = (Integer) pagingSearchParams.get("limit");
            }
            limitClause = " LIMIT " + limit;
            if (pagingSearchParams.containsKey("offset")) {
                offset = (Integer) pagingSearchParams.get("offset");
            }
            limitClause += " OFFSET " + offset;
        }

        query.append(GET_TASKS).append(ss).append(" ORDER BY taskindex ").append(limitClause);

        DbResults rs = DbUtil.executeQuery(conn, query.toString(), params.toArray());
        while (rs.next()) {
            loadTask(rs);
            tasks.add(task);
        }
        resultTotalCount = tasks.size();
        return tasks;
    }

    @Override
    public int updateTaskProgress(Connection conn, String taskID, int progress) throws ServiceException {
        query = new StringBuilder();
        params = new ArrayList();

        query.append("UPDATE proj_task SET percentcomplete = ? WHERE taskid = ?");
        params.add(progress);
        params.add(taskID);

        resultTotalCount = DbUtil.executeUpdate(conn, query.toString(), params.toArray());
        return resultTotalCount;
    }

    @Override
    public TaskProgressStatus getTaskProgressStatus(Task task, int[] nonWorkingDays, String[] cmpHolidays) throws ServiceException {
        TaskProgressStatus status = null;
        try {
            SimpleDateFormat sdfSmall = new SimpleDateFormat("yyyy-MM-dd");
            Date now = new Date();
            String newDateString = sdfSmall.format(now);
            now = sdfSmall.parse(newDateString);
            if (task != null) {
                Date sd = task.getStartDate();
                if (sd != null) {
                    Date ed = task.getEndDate();
                    String duration = task.getDuration();
                    double dur = SchedulingUtilities.getDurationInDays(duration);
                    int pc = task.getPercentComplete();
                    if (!task.isIsMilestone() && !task.isIsParent()) {
                        if (pc == 100) {
                            status = TaskProgressStatus.COMPLETED;
                        } else if (sd.after(now)) {
                            status = TaskProgressStatus.FUTURE;
                        } else if (ed.equals(now) || ed.after(now)) {
                            int progressDays = SchedulingUtilities.calculateWorkingDays(sd, now, nonWorkingDays, cmpHolidays) - 1; // progress days substracted by 1 beacuse progressdays would not include curent day.
                            double progressRelatedDays = dur * pc / 100;
                            if (progressRelatedDays >= progressDays) {
                                status = TaskProgressStatus.IN_PROGRESS;
                            } else {
                                status = TaskProgressStatus.NEED_ATTENTION;
                            }
                        } else {
                            status = TaskProgressStatus.OVERDUE;
                        }
                    }
                }
            }
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("Parse Exception", ex);
        } finally {
            return status;
        }
    }
}
