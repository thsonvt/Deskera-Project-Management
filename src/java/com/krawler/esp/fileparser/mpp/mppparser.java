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
package com.krawler.esp.fileparser.mpp;

import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import net.sf.mpxj.Duration;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.Relation;
import net.sf.mpxj.Resource;
import net.sf.mpxj.Task;
import net.sf.mpxj.TimeUnit;
import net.sf.mpxj.mpp.MPPReader;
import net.sf.mpxj.mpx.MPXReader;
import net.sf.mpxj.reader.ProjectReader;
import com.krawler.common.util.KrawlerLog;
import com.krawler.common.util.StringUtil;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.Calendar;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import net.sf.mpxj.MPXJException;
import net.sf.mpxj.ProjectCalendar;
import net.sf.mpxj.ProjectCalendarException;
import net.sf.mpxj.ResourceAssignment;

public class mppparser {

    public List<projectTask> getRecords(FileInputStream fstream, int type) throws ServiceException, MPXJException {
        List<projectTask> lst = new ArrayList<projectTask>();
        List<Task> task = new ArrayList<Task>();
        HashMap idMap = new HashMap();
        try {
            ProjectReader mp = getReader(type);
            ProjectFile pf = mp.read(fstream);
            fstream.close();
            task = pf.getAllTasks();
            for (int m = 1; m < task.size(); m++) {
                Task temptask = task.get(m);
                int taskid = temptask.getUniqueID();
                idMap.put(taskid, m);
                if (temptask.getStart() != null) {
                    String taskname = temptask.getName();
                    boolean milstone = temptask.getMilestone();
                    String durn = "0d";
                    if(!milstone)
                         durn = convertDuration(temptask.getDuration());
                    Date stdt = temptask.getStart();
                    Date eddt = temptask.getFinish();
                    String ResourceName = "";
                    List<ResourceAssignment> res = temptask.getResourceAssignments();
                    String ss = "";
                    if (res.isEmpty() != true) {
                        for (int j = 0; j < res.size(); j++) {
                            ss += res.get(j).getResource().getName() + ",";
                        }
                        ResourceName = ss.substring(0, ss.length() - 1);
                    }
                    ss = "";
                    String Predecessor = "";
                    Number percom = temptask.getPercentageComplete();
                    int nors = 0;
                    boolean isparent = false;
                    int MSpriority = temptask.getPriority() != null ? temptask.getPriority().getValue() : 301;
                    int deskPriority = 1;
                    if (MSpriority <= 300) {
                        deskPriority = 2;
                    } else if (MSpriority > 700) {
                        deskPriority = 0;
                    }
                    String Notes = temptask.getNotes();
                    projectTask c = new projectTask(taskid, taskname, durn, stdt,
                            eddt, ResourceName, Predecessor, percom, nors, Notes,
                            stdt, stdt, milstone, 0, deskPriority, isparent);
                    boolean ab = lst.add(c);
                } else {
                    projectTask c = new projectTask(taskid, "", "", null, null, "", "", 0, 0, "",null, null, false, 0, 0, false);
                    boolean ab = lst.add(c);
                }
            }
            lst.get(0).setparent(0);
            lst.get(0).setlevel(0);
            lst = linkSubTask(lst, task, idMap);
            setPredecessorValues(lst, task, idMap);
        } catch (java.io.IOException e) {
            KrawlerLog.op.warn("Problem During IO Operation :" + e.toString());
            throw ServiceException.FAILURE("mppparser.getRecords", e);
        } catch (net.sf.mpxj.MPXJException m) {
            KrawlerLog.op.warn("Problem While Creating Mpx file :" + m.toString());
            throw new MPXJException(m.getMessage(), m);
        } catch (Exception e) {
            throw ServiceException.FAILURE("mppparser.getRecords", e);
        }
        return lst;
    }

    public void setPredecessorValues(List<projectTask> lst, List<Task> task, HashMap idMap) {
        for (int i = 1; i < task.size(); i++) {
            List<Relation> pred = task.get(i).getPredecessors();
            String newPred = "";
            if (pred != null) {
                for (int j = 0; j < pred.size(); j++) {
                    if (pred.get(j).getTargetTask().getUniqueID() != null) {
                        newPred += pred.get(j).getTargetTask().getUniqueID() + ",";
                    }
                }
                if (!StringUtil.isNullOrEmpty(newPred)) {
                    lst.get(i - 1).setpredecessor(newPred.substring(0, newPred.length() - 1));
                }
            }
        }
    }

    public static projectTask getProjTask(int temp, List<projectTask> lst) {
        projectTask t = null;
        for (int c = 0; c < lst.size(); c++) {
            if (lst.get(c).gettaskid() == temp) {
                t = lst.get(c);
                break;
            }
        }
        return t;
    }

    public static int getUID(int tid, List<Task> task) {
        int uid = -1;
        for (int i = 0; i < task.size(); i++) {
            if (task.get(i).getID() == tid) {
                uid = task.get(i).getUniqueID();
                break;
            }
        }
        return uid;
    }

    public List<projectTask> linkSubTask(List<projectTask> lst, List<Task> task, HashMap idMap) {
        List<Task> childTasks;
        for (int i = 1; i < task.size(); i++) {
            projectTask pTask = getProjectTask(lst, task.get(i).getUniqueID());
            if (pTask != null) {
                childTasks = task.get(i).getChildTasks();
                for (int j = 0; j < childTasks.size(); j++) {
                    Task temp = childTasks.get(j);
                    projectTask cTask = getProjectTask(lst, temp.getUniqueID());
                    if (cTask != null) {
                        pTask.setisparent(true);
                        cTask.setparent(pTask.gettaskid());
                        cTask.setlevel(pTask.getlevel() + 1);
                    }
                }
            }
        }
        return lst;
    }

    public projectTask getProjectTask(List<projectTask> lst, int id) {
        projectTask temp = null;
        for (int i = 0; i < lst.size(); i++) {
            if (lst.get(i).gettaskid() == id) {
                temp = lst.get(i);
                break;
            }
        }
        return temp;
    }

    public List<String> getResourceList(String DestinationPath, int type) throws MPXJException {
        List<String> a = new ArrayList<String>();
        try {
            ProjectReader mp = getReader(type);
            FileInputStream fstream = new FileInputStream(DestinationPath);
            ProjectFile pf = mp.read(fstream);
            List<Resource> rs = pf.getAllResources();
            for (int i = 1; i < rs.size(); i++) {
                a.add(rs.get(i).getName());
            }
            fstream.close();
        } catch (java.io.IOException e) {
            KrawlerLog.op.warn("Problem During IO Operation :" + e.toString());
        } catch (net.sf.mpxj.MPXJException m) {
            KrawlerLog.op.warn("Problem While Creating Mpx file :" + m.toString());
            throw new MPXJException(m.getMessage(), m);
        }
        return a;
    }

    public String getResourceInfo(FileInputStream fstream, int type) throws ServiceException, MPXJException {
        String retStr = "";
        JSONObject resobj = new JSONObject();
        try {
            ProjectReader mp = getReader(type);
            ProjectFile pf = mp.read(fstream);
            List<Resource> rs = pf.getAllResources();
            for (int i = 0; i < rs.size(); i++) {
                com.krawler.utils.json.base.JSONObject jobj = new JSONObject();
                Resource r = rs.get(i);
                if (!StringUtil.isNullOrEmpty(r.getName())) {
                    jobj.put("name", r.getName());
                    if (!StringUtil.isNullOrEmpty(r.getGroup())) {
                        jobj.put("category", r.getGroup());
                    }
                    if (!StringUtil.isNullOrEmpty(r.getEmailAddress())) {
                        jobj.put("email", r.getEmailAddress());
                    }
                    if (r.getStandardRate() != null) {
                        jobj.put("rate", r.getStandardRate().getAmount());
                    } else {
                        jobj.put("rate", 0);
                    }
                    if(type == 0){
                        int typeid = 0;
                        String typeName = r.getType().toString();
                        if(typeName.compareToIgnoreCase("work") == 0)
                            typeid = Constants.WORK_RESOURCE;
                        else if(typeName.compareToIgnoreCase("material") == 0)
                            typeid = Constants.MATERIAL_RESOURCE;
                        else if(typeName.compareToIgnoreCase("cost") == 0)
                            typeid = Constants.COST_RESOURCE;
                        jobj.put("type", typeid);
                        jobj.put("typename", typeName);
                    } else {
                        if(r.getGroup().compareToIgnoreCase("member") == 0){
                            jobj.put("type", Constants.WORK_RESOURCE);
                            jobj.put("typename", "Work");
                        } else {
                            jobj.put("type", Constants.MATERIAL_RESOURCE);
                            jobj.put("typename", "Material");
                        }
                    }
                    resobj.append("data", jobj);
                }
            }
            fstream.close();
            if (resobj.has("data")) {
                retStr = resobj.toString();
            } else {
                retStr = "{data:[]}";
            }
        } catch (java.io.IOException e) {
            KrawlerLog.op.warn("Problem During IO Operation :" + e.toString());
            throw ServiceException.FAILURE("mppparser.getResourceInfo error", e);
        } catch (net.sf.mpxj.MPXJException m) {
            KrawlerLog.op.warn("Problem While Reading Mpx file :" + m.toString());
            throw new MPXJException(m.getMessage(), m);
        } catch (JSONException m) {
            KrawlerLog.op.warn("Problem Creating Json Object:" + m.toString());
            throw ServiceException.FAILURE("mppparser.getResourceInfo error", m);
        }
        return retStr;
    }

    public static String importCalendar(FileInputStream fstream, HttpServletRequest request) throws ServiceException, MPXJException {
        JSONObject resobj = new JSONObject();
        try {
            ProjectReader pr = mppparser.getReader(Integer.parseInt(request.getParameter("type")));
            ProjectFile pf = pr.read(fstream);
            fstream.close();
            String calname = pf.getProjectHeader().getCalendarName();
            ProjectCalendar projCal = pf.getBaseCalendar(calname);
            if (request.getParameter("isweek").equals("1")) {
                int[] projdays = projCal.getDays();
                for (int i = 0; i < projdays.length; i++) {
                    boolean ih = false;
                    if (projdays[i] == 0) {
                        ih = true;
                    }
                    com.krawler.utils.json.base.JSONObject jobj = new JSONObject();
                    jobj.put("day", i);
                    jobj.put("ish", ih);
                    resobj.append("workweek", jobj);
                }
            }
            if (request.getParameter("isholiday").equals("1")) {
                java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd");
                List<ProjectCalendarException> exceptions = projCal.getCalendarExceptions();
                for (int m = 0; m < exceptions.size(); m++) {
                    Date fromdate = exceptions.get(m).getFromDate();
                    Date todate = exceptions.get(m).getToDate();
                    Calendar c1 = Calendar.getInstance();
                    while ((fromdate.compareTo(todate)) < 0) {
                        if (!projCal.isWorkingDate(fromdate)) {
                            JSONObject jobj = new JSONObject();
                            jobj.put("date", sdf1.format(fromdate));
                            resobj.append("holiday", jobj);
                        }
                        c1.set(fromdate.getYear() + 1900, fromdate.getMonth(), fromdate.getDate());
                        c1.add(Calendar.DATE, 1);
                        fromdate = sdf1.parse(sdf1.format(c1.getTime()));
                    }
                }
            }
        } catch (java.io.IOException e) {
            KrawlerLog.op.warn("Problem During IO Operation :" + e.toString());
            throw ServiceException.FAILURE("mppparser.getRecords", e);
        } catch (net.sf.mpxj.MPXJException m) {
            KrawlerLog.op.warn("Problem While Creating Mpx file :" + m.toString());
            throw new MPXJException(m.getMessage(), m);
        } catch (Exception e) {
            throw ServiceException.FAILURE("mppparser.getRecords", e);
        }
        return resobj.toString();
    }

    public String convertDuration(Duration d) {
        String dur = d.getUnits().toString();
        if (dur.compareToIgnoreCase("w") == 0) {
            dur = Duration.getInstance(d.getDuration() * 5, TimeUnit.DAYS).toString();
        } else if (dur.compareToIgnoreCase("mo") == 0) {
            dur = Duration.getInstance(d.getDuration() * 30, TimeUnit.DAYS).toString();
        } else if (dur.compareToIgnoreCase("m") == 0) {
            dur = Duration.getInstance(d.getDuration() / 60, TimeUnit.HOURS).toString();
        } else if ((dur.compareToIgnoreCase("d") == 0) || (dur.compareToIgnoreCase("h") == 0)) {
            dur = d.toString();
        }
        return dur;
    }

    private static ProjectReader getReader(int type) {
        ProjectReader result;
        if (type == 0) {
            result = new MPPReader();
        } else {
            result = new MPXReader();
        }
        return result;
    }
}

