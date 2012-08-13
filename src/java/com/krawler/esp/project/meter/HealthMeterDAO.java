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
import com.krawler.database.DbPool.Connection;
import com.krawler.esp.project.project.Project;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface HealthMeterDAO {

    public static int ONTIME = 1;
    public static int SL_BH_SH = 2;
    public static int GR_BH_SH = 3;

    HealthMeter getHealthMeter(Connection conn, String projectid) throws ServiceException;

    HealthMeter getHealthMeter(Connection conn, String projectid, String[] CmpHoliDays) throws ServiceException;

    HealthMeter getHealthMeter(Connection conn, Project project) throws ServiceException;

    Map<String, Object> getBaseLineMeter(Connection conn, HealthMeter meter) throws ServiceException;

    Map<String, Object> getBaseLineMeter(Connection conn, Project project) throws ServiceException;

    public Map<String, Object> getBaseLineMeter(Connection conn, String projectId) throws ServiceException;

    public void setBaseLineMeter(Connection conn, String projectid) throws ServiceException;

    public void setBaseLineMeter(Connection conn, String projectid, String base, String ontime, String slightly, String gravely) throws ServiceException;

    public void editBaseLineMeter(Connection conn, String projectid, String base, String ontime, String slightly, String gravely) throws ServiceException;
}
