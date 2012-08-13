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
package com.krawler.esp.project.project;

import com.krawler.common.customcolumn.CCManager;
import com.krawler.common.customcolumn.ColumnSet;
import com.krawler.common.customcolumn.CustomColumn;
import com.krawler.esp.project.meter.HealthMeterDAOImpl;
import com.krawler.esp.project.meter.HealthMeter;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.database.DbPool.Connection;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.esp.user.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Kamlesh
 */
public class ProjectDAOImpl implements ProjectDAO {

    private int TOTAL_COUNT = 0;
    private HealthMeter healthMeter;

    @Override
    public int getTotalCount() {
        return TOTAL_COUNT;
    }

    @Override
    public Project getProject(Connection conn, String projectid) throws ServiceException {
        Project proj = null;
        String query = "SELECT * FROM project where projectid=? ORDER BY projectname ASC";
        DbResults rs = DbUtil.executeQuery(conn, query, projectid);
        if (rs.next()) {
            proj = new Project();
            ProjectHelper.setProjectObject(rs, proj);
        }
        return proj;
    }

    @Override
    public Project getProjectById(Connection conn, String projectid) throws ServiceException {
        Project proj = null;
        String query = "SELECT t1.projectid, project.*, count AS members FROM "
                + "(SELECT projectid,count(projectid) AS count FROM projectmembers WHERE status IN (3,4,5) AND inuseflag = 1 GROUP BY(projectid)) AS t1 "
                + "INNER JOIN projectmembers ON projectmembers.projectid = t1.projectid "
                + "INNER JOIN project ON project.projectid = t1.projectid where project.projectid=?";
        DbResults rs = DbUtil.executeQuery(conn, query, projectid);
        if (rs.next()) {
            proj = new Project();
            ProjectHelper.setProjectObject(rs, proj);
        }
        return proj;
    }

    @Override
    public String getProjectName(Connection conn, String projectId) throws ServiceException {
        String projName = "";
        DbResults rs = DbUtil.executeQuery(conn, "SELECT projectname from project where projectid = ? ", projectId);
        if (rs.next()) {
            projName = rs.getString("projectname");
        }
        return projName;
    }

    @Override
    public List<Project> getAllProjectByUser(Connection conn, String userid) throws ServiceException {
        return getAllProjectByUser(conn, userid, 0, -1);
    }

    @Override
    public List<Project> getAllProjectByUser(Connection conn, String userid, int offset, int limit) throws ServiceException {
        return getAllProjectByUser(conn, userid, "", offset, limit);
    }

    @Override
    public List<Project> getAllProjectByUser(Connection conn, String userid, String ss, int offset, int limit) throws ServiceException {
        Map<String, Object> _p = new HashMap<String, Object>();
        if (!StringUtil.isNullOrEmpty(ss)) {
            _p.put("ss", ss);
        }
        if (offset != 0) {
            _p.put("Offset", offset);
        }
        if (limit > -1) {
            _p.put("limit", limit);
        }
        return getAllProjectByUser(conn, userid, _p);
    }

    @Override
    public List<Project> getAllProjectByUser(Connection conn, String userid, Map<String, Object> pagingSearchParams) throws ServiceException {
        return getAllProjectByUser(conn, userid, pagingSearchParams, false);
    }

    @Override
    public List<Project> getQuickLinks(Connection conn, String userId) throws ServiceException {
        return getAllProjectByUser(conn, userId, new HashMap<String, Object>(), true);
    }

    private List<Project> getAllProjectByUser(Connection conn, String userid, Map<String, Object> pagingSearchParams, boolean isLink) throws ServiceException {
        StringBuilder query = null;
        List<Project> projList = null;
        String ss = "";
        int limit = 100, offset = 0;
        String linkQuery = "";
        if (isLink) {
            linkQuery = " and project.archived=0 and quicklink=1";
        }
        String countQuery = "SELECT count(1) as count FROM project inner join projectmembers on project.projectid = projectmembers.projectid ";

        String whereClause = "where projectmembers.userid = ? and projectmembers.status in (3,4,5) and inuseflag = 1 " + linkQuery;

        String getQuery = "SELECT project.*, count AS members FROM "
                + "(SELECT projectid,count(projectid) AS count FROM projectmembers WHERE status IN (3,4,5) AND inuseflag = 1 GROUP BY(projectid)) AS t1 "
                + "INNER JOIN projectmembers ON projectmembers.projectid = t1.projectid INNER JOIN project ON project.projectid = t1.projectid ";
        String limitClasue = "";
        if (pagingSearchParams.containsKey("ss")) {
            ss = (String) pagingSearchParams.get("ss");
            ss = StringUtil.getSearchString(ss, "AND", new String[]{"project.projectname"});
        }
        if (pagingSearchParams.containsKey("limit")) {
            limit = (Integer) pagingSearchParams.get("limit");
            limitClasue = " LIMIT " + limit;
        }
        if (pagingSearchParams.containsKey("offset")) {
            offset = (Integer) pagingSearchParams.get("offset");
            limitClasue += " OFFSET " + offset;
        }

        query = new StringBuilder(countQuery).append(whereClause).append(ss);
        DbResults rs = DbUtil.executeQuery(conn, query.toString(), userid);
        if (rs.next()) {
            TOTAL_COUNT = rs.getInt(1); // count
        }

        query = new StringBuilder(getQuery).append(whereClause).append(ss).append(" order by projectname").append(limitClasue);
        rs = DbUtil.executeQuery(conn, query.toString(), userid);
        projList = new ArrayList<Project>(rs.size());
        while (rs.next()) {
            Project proj = new Project();
            ProjectHelper.setProjectObject(rs, proj);
            if (isLink) {
                setHealthMeter(conn, proj);
            }
            projList.add(proj);
        }
        return projList;
    }

    @Override
    public void setHealthMeter(Connection conn, Project project) throws ServiceException {
        healthMeter = new HealthMeterDAOImpl().getHealthMeter(conn, project);
        project.setMeter(healthMeter);
    }

    @Override
    public List<Project> getAllProjectByCompany(Connection conn, String companyId, String ss, int offset, int limit) throws ServiceException {
        Map<String, Object> _p = new HashMap<String, Object>();
        if (!StringUtil.isNullOrEmpty(ss)) {
            _p.put("ss", ss);
        }
        if (offset != 0) {
            _p.put("offset", offset);
        }
        if (limit > -1) {
            _p.put("limit", limit);
        }
        return getAllProjectByCompany(conn, companyId, _p, false, true);
    }

    @Override
    public List<Project> getAllProjectByCompany(Connection conn, String companyid, Map<String, Object> pagingSearchParams, 
            boolean customSearch, boolean includeArchived) throws ServiceException {
        CustomColumn cc = CCManager.getCustomColumn(companyid);
        StringBuilder query = null;
        List<Project> projList = null;
        String ss = "";
        int limit = 100, offset = 0;
        String countQuery = "SELECT count(1) as count FROM project ";

        String whereClause = "where project.companyid= ? ";
        whereClause += (includeArchived) ? " AND project.archived = false " : "";

        String getQuery = "SELECT project.*, t1.count AS members FROM "
                + "(SELECT projectid,count(projectid) AS count FROM projectmembers WHERE status IN (3,4,5) AND inuseflag = 1 GROUP BY(projectid)) AS t1 "
                + "INNER JOIN project ON project.projectid = t1.projectid ";

        String limitClasue = "";

        if (pagingSearchParams.containsKey("ss")) {
            ss = (String) pagingSearchParams.get("ss");
            ss = StringUtil.getSearchString(ss, "AND", new String[]{"project.projectname"});
        }
        if (pagingSearchParams.containsKey("limit")) {
            limit = (Integer) pagingSearchParams.get("limit");
        }
        limitClasue = " LIMIT " + limit;
        if (pagingSearchParams.containsKey("offset")) {
            offset = (Integer) pagingSearchParams.get("offset");
        }
        limitClasue += " OFFSET " + offset;

        query = new StringBuilder(countQuery).append(whereClause).append(ss);
        DbResults rs = DbUtil.executeQuery(conn, query.toString(), companyid);
        if (rs.next()) {
            TOTAL_COUNT = rs.getInt(1);
        }

        query = new StringBuilder(getQuery).append(whereClause).append(ss).append(limitClasue);
        rs = DbUtil.executeQuery(conn, query.toString(), companyid);
        projList = new ArrayList<Project>(rs.size());
        while (rs.next()) {
            Project proj = new Project();
            ProjectHelper.setProjectObject(rs, proj);
            ColumnSet rec = cc.getColumnsData(conn, "Project", proj.getProjectID());
            proj.setRecordSet(rec);
            projList.add(proj);
        }
        return projList;
    }

    @Override
    public int getProjectCountBy(Connection conn, String countBy, String searchId, boolean archived, String ss) throws ServiceException {
        String whereClause = "where ";
        String sarchString = "";
        StringBuilder query = new StringBuilder();
        if (countBy.equals("user")) {
            whereClause += "projectmembers.userid =? and inuseflag = 1 ";
            query.append("select count(1) as count from project inner join projectmembers on project.projectid=projectmembers.projectid ");
        } else if (countBy.equals("company")) {
            query.append("SELECT COUNT(1) as count FROM project ");
            whereClause += "project.companyid=? ";
        }
        if (!StringUtil.isNullOrEmpty(ss)) {
            sarchString = StringUtil.getSearchString(ss, "AND", new String[]{"project.projectname"});
        }
        if (!archived) {
            whereClause += " AND archived =0";
        }
        query.append(whereClause).append(sarchString);
        DbResults rs = DbUtil.executeQuery(conn, query.toString(), searchId);
        if (rs.next()) {
            return rs.getInt("count");
        }

        return 0;
    }

    @Override
    public int updatePERTDiffs(Connection conn, int o_diff, int p_diff, String projectID) throws ServiceException {
        int count = DbUtil.executeUpdate(conn, "UPDATE project SET o_diff = ?, p_diff = ? WHERE projectid = ?",
                new Object[]{o_diff, p_diff, projectID});
        updatePERTStatus(conn, projectID, PERTDiffStatus.PROJECT.getCode());
        return count;
    }

    @Override
    public int updatePERTStatus(Connection conn, String projectID, int pertStatus) throws ServiceException {
        int count = DbUtil.executeUpdate(conn, "UPDATE project SET pertstatus = ? WHERE projectid = ?",
                new Object[]{pertStatus, projectID});
        return count;
    }

    @Override
    public List<ProjectMember> getProjectMembers(Connection conn, Project project) throws ServiceException {
        List<ProjectMember> pms = null;
        DbResults rs = DbUtil.executeQuery(conn, "SELECT pm.*,p.*,u.*,ul.username FROM projectmembers pm "
                + "INNER JOIN project p ON p.projectid = pm.projectid "
                + "INNER JOIN users u ON pm.userid = u.userid inner join userlogin ul on u.userid=ul.userid WHERE pm.projectid = ?", project.getProjectID());
        pms = new ArrayList<ProjectMember>(rs.size());
        while (rs.next()) {
            ProjectMember pm = new ProjectMember();
            ProjectHelper.setProjectMembersObject(rs, pm);
            pms.add(pm);
        }
        return pms;
    }

    @Override
    public ProjectMember getProjectMember(Connection conn, Project project, String userID) throws ServiceException {
        ProjectMember pm = null;
        DbResults rs = DbUtil.executeQuery(conn, "SELECT p.*, pm.*, u.*, ul.username FROM projectmembers pm "
                + "INNER JOIN project p ON p.projectid = pm.projectid "
                + "INNER JOIN users u ON pm.userid = u.userid inner join userlogin ul on u.userid=ul.userid WHERE pm.projectid = ? and u.userid = ?", new Object[]{project.getProjectID(), userID});
        if (rs.next()) {
            pm = new ProjectMember();
            ProjectHelper.setProjectMembersObject(rs, pm);
        }
        return pm;
    }

    @Override
    public ProjectMember getProjectMember(Connection conn, String projectID, String userID) throws ServiceException {
        Project p = getProjectById(conn, projectID);
        return getProjectMember(conn, p, userID);
    }

    @Override
    public List<ProjectMember> getProjectMembers(Connection conn, String projectID) throws ServiceException {
        Project p = getProjectById(conn, projectID);
        return getProjectMembers(conn, p);
    }

    @Override
    public List<Project> getNonModeratorProjects(Connection conn, String companyID) throws ServiceException {
        List<Project> projects = null;
        List<Project> NonModProjects = new ArrayList<Project>();
        projects = getAllProjectByCompany(conn, companyID, "", 0, 1000);

        for (Project project : projects) {
            List<ProjectMember> pms = getProjectMembers(conn, project);
            boolean modFound = false;
            if (!pms.isEmpty()) {
                for (ProjectMember pm : pms) {
                    if (pm.getStatus() == Constants.PROJECT_MEMBER_STATUS_MODERATOR) {
                        modFound = true;
                        break;
                    }
                }
            }
            if (!modFound) {
                NonModProjects.add(project);
            }
        }
        return NonModProjects;
    }

//    @Override
//    public int setProjectModerator(Connection conn, String projectID, String userID) throws ServiceException {
//        ProjectMember pm = getProjectMember(conn, projectID, userID);
//        ResourceDAO rd = new ResourceDAOImpl();
//        int count = 0;
//        if (pm != null) {
//            pm.setInUseFlag(true);
//            pm.setPlanPermission(ProjectMemberPlanPermission.MODIFY_ALL.getCode());
//            pm.setStatus(ProjectMemberStatus.MODERATOR.getCode());
//            count = updateProjectMember(conn, pm);
//            if(count == 1){
//                ProjectResource pr = (ProjectResource) rd.getResource(conn, userID, ResourceDAOImpl.RESOURCE_FROM_PROJECT, projectID);
//                pr.setInUseFlag(true);
//                count = rd.updateResource(conn, pr, ResourceDAOImpl.RESOURCE_FROM_PROJECT);
//            }
//        } else {
//            count = addProjectMember(conn, projectID, userID, ProjectMemberStatus.MODERATOR.getCode(), ProjectMemberPlanPermission.MODIFY_ALL.getCode());
//        }
//        return count;
//    }
    @Override
    public int addProjectMember(Connection conn, String projectID, String userID, int status, int planpermission) throws ServiceException {
        String query = "INSERT INTO projectmembers(projectid, status, userid, inuseflag, planpermission) "
                + " values (?,?,?,?,?,?,?,?)";
        return DbUtil.executeUpdate(conn, query, new Object[]{projectID, status, userID, true, planpermission});
    }

    @Override
    public int addProjectMember(Connection conn, ProjectMember pm) throws ServiceException {
        return addProjectMember(conn, pm.getProject().getProjectID(), pm.getUser().getUserID(), pm.getStatus(), pm.getPlanPermission());
    }

    @Override
    public int updateProjectMember(Connection conn, String projectID, String userID, int status, int planpermission,
            boolean inUseFlag, boolean isQuickLink, int notificationSubscription, boolean milestoneTimelineSelection) throws ServiceException {
        ProjectMember pm = new ProjectMember();
        Project p = new Project();
        p.setProjectID(projectID);
        User u = new User();
        u.setUserID(userID);

        pm.setProject(p);
        pm.setUser(u);
        pm.setStatus(status);
        pm.setPlanPermission(planpermission);
        pm.setInUseFlag(inUseFlag);
        pm.setQuickLink(isQuickLink);
        pm.setNotificationSubscription(notificationSubscription);
        pm.setMilestoneTimelineSelection(milestoneTimelineSelection);

        return updateProjectMember(conn, pm);
    }

    @Override
    public int updateProjectMember(Connection conn, ProjectMember pm) throws ServiceException {
        String query = "UPDATE projectmembers SET status = ?, planpermission = ?, quicklink = ?, "
                + "inuseflag = ?, notification_subscription = ?, milestonestack = ? "
                + "WHERE projectid = ? AND userid = ?";
        return DbUtil.executeUpdate(conn, query, new Object[]{pm.getStatus(), pm.getPlanPermission(), pm.isQuickLink(), pm.isInUseFlag(),
                    pm.getNotificationSubscription(), pm.isMilestoneTimelineSelection(), pm.getProject().getProjectID(), pm.getUser().getUserID()});
    }
}
