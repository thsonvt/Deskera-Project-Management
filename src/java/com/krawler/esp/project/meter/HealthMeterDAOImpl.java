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
package com.krawler.esp.project.meter;

import com.krawler.common.service.ServiceException;
import com.krawler.common.util.SchedulingUtilities;
import com.krawler.common.util.StringUtil;
import com.krawler.database.DbPool.Connection;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.esp.project.project.Project;
import com.krawler.esp.project.project.ProjectDAO;
import com.krawler.utils.json.JSONException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.krawler.esp.project.project.ProjectDAOImpl;
import com.krawler.esp.project.task.Task;
import com.krawler.esp.project.task.TaskDAO;
import com.krawler.esp.project.task.TaskDAOImpl;
import com.krawler.esp.project.task.TaskProgressStatus;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class HealthMeterDAOImpl implements HealthMeterDAO {

    private int[] getTimeWiseTasksCount(Connection conn, String projectid, String[] companyHolidays) throws ServiceException {
        int[] counts = new int[5];
        int[] nonWorkingDays = SchedulingUtilities.getNonWorkWeekDays(conn, projectid);
        TaskDAO taskdao = new TaskDAOImpl();
        try {
            List<Task> tasks = taskdao.getTasks(conn, projectid, 0, 10000);
            if (!tasks.isEmpty()) {
                for (int i = 0; i < tasks.size(); i++) {
                    Task task = tasks.get(i);
                    TaskProgressStatus tps = taskdao.getTaskProgressStatus(task, nonWorkingDays, companyHolidays);
                    if (tps != null) {
                        int status = tps.getCode();
                        counts[status-1] ++;
                    }
                }
            }
        } catch (ServiceException e) {
            throw ServiceException.FAILURE("projdb.getTimeWiseTasksCount : " + e.getMessage(), e);
        }
        return counts;
    }

    @Override
    public HealthMeter getHealthMeter(Connection conn, String projectid) throws ServiceException {
        Project p = new ProjectDAOImpl().getProject(conn, projectid);
        String[] CmpHoliDays = SchedulingUtilities.getCompHolidays(conn, p.getCompanyID());
        return getHealthMeter(conn, projectid, CmpHoliDays);
    }

    @Override
    public HealthMeter getHealthMeter(Connection conn, String projectid, String[] CmpHoliDays) throws ServiceException {
        HealthMeter meter = null;
        try {
            int counts[] = getTimeWiseTasksCount(conn, projectid, CmpHoliDays);

            int completed = counts[0];
            int onTime = counts[1];
            int needAttention = counts[2];
            int overdue = counts[3];
            int future = counts[4];
            meter = new HealthMeter(completed, onTime, needAttention, overdue, future);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("projdb.getTimeWiseTasksCount : " + ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(HealthMeterDAOImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
        return meter;
        }
    }

    @Override
    public HealthMeter getHealthMeter(Connection conn, Project project) throws ServiceException {
        HealthMeter meter = getHealthMeter(conn, project.getProjectID());
        meter.setProject(project);
        return meter;
    }

    @Override
    public Map<String, Object> getBaseLineMeter(Connection conn, HealthMeter meter) throws ServiceException {
        return getBaseLineMeter(conn, meter.getProject());
    }

    @Override
    public Map<String, Object> getBaseLineMeter(Connection conn, String projectId) throws ServiceException {
        Map<String, Object> map = new HashMap<String, Object>(4);
        DbResults rs = DbUtil.executeQuery(conn, "select base,ontime,slightly,gravely from PROJ_HEALTH where projectid=?", projectId);
        if (rs.next()) {
            map.put("base", rs.getString("base"));
            map.put("ontime", rs.getInt("ontime"));
            map.put("slightly", rs.getInt("slightly"));
            map.put("gravely", rs.getInt("gravely"));
        }
        return map;
    }

    @Override
    public Map<String, Object> getBaseLineMeter(Connection conn, Project project) throws ServiceException {
        return getBaseLineMeter(conn, project.getProjectID());
    }

    @Override
    public void setBaseLineMeter(Connection conn, String projectid) throws ServiceException {
        String query = "INSERT into PROJ_HEALTH(projectid,base) values(?,?)";
        DbUtil.executeUpdate(conn, query, new Object[]{projectid, "TASK"});
    }

    @Override
    public void setBaseLineMeter(Connection conn, String projectid, String base, String ontime, String slightly, String gravely) throws ServiceException {
        String query = "INSERT into PROJ_HEALTH(projectid,base,ontime,slightly,gravely) values(?,?,?,?,?)";
        DbUtil.executeUpdate(conn, query, new Object[]{projectid, base, ontime, slightly, gravely});
    }

    @Override
    public void editBaseLineMeter(Connection conn, String projectid, String base, String ontime, String slightly, String gravely) throws ServiceException {
        String query = "UPDATE PROJ_HEALTH set base=?,ontime=?,slightly=?,gravely=? where projectid=?";
        DbUtil.executeUpdate(conn, query, new Object[]{base, ontime, slightly, gravely, projectid});
    }
}
