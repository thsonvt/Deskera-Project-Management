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
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author Abhay Kulkarni
 */
public class CheckListCommonRetriever {

    private StringBuilder query;
    private List params;
    private int resultTotalCount;
    private CheckList cl;
    private CheckListTask clt;
    private TaskCheckListMapping tclm;

    private void resetAll() {
        params = new ArrayList();
        query = new StringBuilder();
        resultTotalCount = 0;
    }

    public int getResultTotalCount() {
        return resultTotalCount;
    }

    // ------------------------------------------------- Check List related methods -----------------------------------------------------------
    public String createCheckList(Connection conn, String cName, String cDesc, String createdBy, String updatedBy, String companyID) throws ServiceException {

        resetAll();

        String clid = UUID.randomUUID().toString();
        params.add(clid);
        params.add(cName);
        params.add(cDesc);
        params.add(createdBy);
        params.add(updatedBy);
        params.add(companyID);

        query.append("INSERT INTO checklist(checklistid, checklistname, checklistdesc, createdon, updatedon, createdby, updatedby, companyid) VALUES(?, ?, ?, now(), now(), ?, ?, ?)");

        resultTotalCount = DbUtil.executeUpdate(conn, query.toString(), params.toArray());
        return clid;
    }

    public int udpateCheckList(Connection conn, String checkListID, String cName, String cDesc, String updatedBy) throws ServiceException {

        resetAll();
        params.add(cName);
        params.add(cDesc);
        params.add(updatedBy);
        params.add(checkListID);

        query.append("UPDATE checklist SET checklistname = ?, checklistdesc = ?, updatedby = ? WHERE checklistid = ?");

        resultTotalCount = DbUtil.executeUpdate(conn, query.toString(), params.toArray());
        return resultTotalCount;
    }

    public CheckList getCheckList(Connection conn, String checkListID) throws ServiceException {

        resetAll();
        params.add(checkListID);

        query.append("SELECT * FROM checklist WHERE checklistid = ?");

        DbResults rs = DbUtil.executeQuery(conn, query.toString(), params.toArray());

        if (rs.next()) {
            cl = new CheckList();
            CheckListHelper.loadCheckList(rs, cl);
            cl.setTasks(getCheckListTasks(conn, checkListID));
        }
        resultTotalCount = 1;
        return cl;
    }

    public List<CheckList> getCheckLists(Connection conn, String companyID) throws ServiceException {

        List<CheckList> ll = new ArrayList<CheckList>();
        resetAll();

        params.add(companyID);

        query.append("SELECT * FROM checklist WHERE companyid = ? ORDER BY createdon DESC");

        DbResults rs = DbUtil.executeQuery(conn, query.toString(), params.toArray());
        while (rs.next()) {
            cl = new CheckList();
            CheckListHelper.loadCheckList(rs, cl);
            cl.setTasks(getCheckListTasks(conn, cl.getCheckListID()));
            ll.add(cl);
        }
        resultTotalCount = ll.size();
        return ll;
    }

    public int removeCheckList(Connection conn, String checkListID) throws ServiceException {

        resetAll();

        params.add(checkListID);

        query.append("DELETE FROM checklist WHERE checklistid = ?");

        resultTotalCount = DbUtil.executeUpdate(conn, query.toString(), params.toArray());
        return resultTotalCount;
    }

    // ------------------------------------------------- Check List Tasks related methods --------------------------------------------------------
    public String createCheckListTask(Connection conn, String checkListID, String taskName) throws ServiceException {

        resetAll();

        String cltid = UUID.randomUUID().toString();
        params.add(cltid);
        params.add(taskName);
        params.add(checkListID);

        query.append("INSERT INTO checklisttasks (ctaskid, ctaskname, checklistid) VALUES(?, ?, ?)");

        resultTotalCount = DbUtil.executeUpdate(conn, query.toString(), params.toArray());
        return cltid;
    }

    public int udpateCheckListTask(Connection conn, String cTaskID, String cTaskName, String checkListID) throws ServiceException {

        resetAll();

        params.add(cTaskName);
        params.add(checkListID);

        query.append("UPDATE checklisttasks SET ctaskname = ? WHERE ctaskid = ?");

        resultTotalCount = DbUtil.executeUpdate(conn, query.toString(), params.toArray());
        return resultTotalCount;
    }

    public CheckListTask getCheckListTask(Connection conn, String cTaskID) throws ServiceException {

        resetAll();
        params.add(cTaskID);

        query.append("SELECT * FROM checklisttasks ct WHERE ctaskid = ?");

        DbResults rs = DbUtil.executeQuery(conn, query.toString(), params.toArray());
        if (rs.next()) {
            clt = new CheckListTask();
            CheckListHelper.loadCheckListTask(rs, clt);
        }
        resultTotalCount = 1;
        return clt;
    }

    public List<CheckListTask> getCheckListTasks(Connection conn, String checkListID) throws ServiceException {

        List<CheckListTask> ll = new ArrayList<CheckListTask>();
        resetAll();

        params.add(checkListID);

        query.append("SELECT * FROM checklisttasks ct INNER JOIN checklist c on c.checklistid = ct.checklistid WHERE c.checklistid = ? ORDER BY ct.checklistid");

        DbResults rs = DbUtil.executeQuery(conn, query.toString(), params.toArray());
        while (rs.next()) {
            clt = new CheckListTask();
            CheckListHelper.loadCheckListTask(rs, clt);
            ll.add(clt);
        }
        resultTotalCount = ll.size();
        return ll;
    }

    public int removeCheckListTask(Connection conn, String taskID) throws ServiceException {

        resetAll();

        params.add(taskID);

        query.append("DELETE FROM checklisttasks WHERE ctaskid = ?");

        resultTotalCount = DbUtil.executeUpdate(conn, query.toString(), params.toArray());
        return resultTotalCount;
    }

    // ------------------------------------------------- Task Check List Mapping related methods --------------------------------------------------
    public String mapCheckListWithTask(Connection conn, String checkListID, String taskID, String mappedBy) throws ServiceException {

        resetAll();

        String mappingid = UUID.randomUUID().toString();
        params.add(mappingid);
        params.add(taskID);
        params.add(checkListID);
        params.add(mappedBy);

        query.append("INSERT INTO taskchecklistmapping (mappingid, taskid, checklistid, mappedby) VALUES(?, ?, ?, ?)");

        resultTotalCount = DbUtil.executeUpdate(conn, query.toString(), params.toArray());
        return mappingid;

    }

    public TaskCheckListMapping getTaskCheckListMapping(Connection conn, String checkListID, String taskID) throws ServiceException {

        resetAll();
        params.add(taskID);

        query.append("SELECT * FROM taskchecklistmapping WHERE taskid = ?");

        if(checkListID != null && !checkListID.equals("")){
            query.append(" AND checklistid = ?");
            params.add(checkListID);
        }

        DbResults rs = DbUtil.executeQuery(conn, query.toString(), params.toArray());
        if (rs.next()) {
            tclm = new TaskCheckListMapping();
            CheckListHelper.loadTaskCheckListMapping(rs, tclm);
        }
        resultTotalCount = 1;
        return tclm;
    }

    public TaskCheckListMapping getTaskCheckListMapping(Connection conn, String mappingID) throws ServiceException {

        resetAll();
        params.add(mappingID);

        query.append("SELECT * FROM taskchecklistmapping WHERE mappingid = ?");

        DbResults rs = DbUtil.executeQuery(conn, query.toString(), params.toArray());
        if (rs.next()) {
            tclm = new TaskCheckListMapping();
            CheckListHelper.loadTaskCheckListMapping(rs, tclm);
        }
        resultTotalCount = 1;
        return tclm;
    }

    public List<TaskCheckListMapping> getCheckListMappings(Connection conn, String checkListID) throws ServiceException {

        List<TaskCheckListMapping> tclmList = new ArrayList<TaskCheckListMapping>();
        resetAll();

        params.add(checkListID);

        query.append("SELECT * FROM taskchecklistmapping WHERE checklistid = ?");

        DbResults rs = DbUtil.executeQuery(conn, query.toString(), params.toArray());
        while (rs.next()) {
            tclm = new TaskCheckListMapping();
            CheckListHelper.loadTaskCheckListMapping(rs, tclm);
            tclmList.add(tclm);
        }
        resultTotalCount = tclmList.size();
        return tclmList;
    }

    public int removeTaskCheckListMapping(Connection conn, String mappingID) throws ServiceException {

        resetAll();

        params.add(mappingID);

        query.append("DELETE FROM taskchecklistmapping WHERE mappingid = ?");

        resultTotalCount = DbUtil.executeUpdate(conn, query.toString(), params.toArray());
        return resultTotalCount;
    }

    public int removeTaskCheckListMapping(Connection conn, String checkListID, String taskID) throws ServiceException {

        resetAll();
        params.add(taskID);

        query.append("DELETE FROM taskchecklistmapping WHERE taskid = ?");

        if(checkListID != null && !checkListID.equals("")){
            query.append(" AND checklistid = ?");
            params.add(checkListID);
        }

        resultTotalCount = DbUtil.executeUpdate(conn, query.toString(), params.toArray());
        return resultTotalCount;
    }
}
