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
package com.krawler.esp.project.resource;

import com.krawler.common.service.ServiceException;
import com.krawler.common.util.KWLErrorMsgs;
import com.krawler.common.util.StringUtil;
import com.krawler.database.DbPool.Connection;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.esp.project.resource.Resource.ResourceCategory;
import com.krawler.esp.project.resource.Resource.ResourceType;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Abhay
 */
public class ResourceDAOImpl implements ResourceDAO{

    private StringBuilder query;
    private List params;
    private int resultTotalCount;
    private Resource resource;
    private ProjectResource projectResource;
    private BaselineResource baselineResource;
    

    private void loadResourceType(Resource r, DbResults rs){
        r.setType(getResourceTypeObject(r, rs));
    }

    private ResourceType getResourceTypeObject(Resource r, DbResults rs){
        ResourceType rt = r.new ResourceType();
        ResourceHelper.loadResourceType(rt, rs);
        return rt;
    }
    
    private void loadResourceCategory(Resource r, DbResults rs){
        r.setCategory(getResourceCategoryObject(r, rs));
    }
    
    private ResourceCategory getResourceCategoryObject(Resource r, DbResults rs){
        ResourceCategory rc = r.new ResourceCategory();
        ResourceHelper.loadResourceCategory(rc, rs);
        return rc;
    }
    
    private void loadProjectResource(DbResults rs){
        projectResource = new ProjectResource();
        loadResourceCategory(projectResource, rs);
        loadResourceType(projectResource, rs);
        ResourceHelper.loadProjectResource(projectResource, rs, null);
    }
    
    private void loadBaselineResource(DbResults rs){
        baselineResource = new BaselineResource();
        loadResourceCategory(baselineResource, rs);
        loadResourceType(baselineResource, rs);
        ResourceHelper.loadBaselineResource(baselineResource, rs, null);
    }
    
    @Override
    public Resource getResource(Connection conn, String resourceID, int resourceInWhichModule, String moduleSpecificID) throws ServiceException {
        query = new StringBuilder();
        params = new ArrayList();
        switch(resourceInWhichModule){
            case RESOURCE_FROM_PROJECT:
                query.append("SELECT pr.*, pr.projid AS projectid, prc.categoryname, prt.typename FROM proj_resources pr "
                        + "INNER JOIN proj_resourcecategory prc ON pr.categoryid = prc.categoryid "
                        + "INNER JOIN proj_resourcetype prt ON pr.typeid = prt.typeid "
                        + "WHERE pr.resourceid = ? AND pr.projid = ?");
                params.add(resourceID);
                params.add(moduleSpecificID);
                break;
            case RESOURCE_FROM_BASELINE:
                query.append("SELECT brd.*, prc.categoryname, prt.typename FROM proj_baselineresourcesdata brd "
                        + "INNER JOIN proj_resourcecategory prc ON brd.categoryid = prc.categoryid "
                        + "INNER JOIN proj_resourcetype prt ON brd.typeid = prt.typeid "
                        + "WHERE brd.baselineid = ? AND brd.resourceid = ?");
                params.add(moduleSpecificID);
                params.add(resourceID);
                break;
        }
        DbResults rs = DbUtil.executeQuery(conn, query.toString(), params.toArray());
        if (rs.next()){
            switch(resourceInWhichModule){
                case RESOURCE_FROM_PROJECT:
                    loadProjectResource(rs);
                    return projectResource;
                case RESOURCE_FROM_BASELINE:
                    loadBaselineResource(rs);
                    return baselineResource;
            }
        }
        return null;
    }

    @Override
    public List<ProjectResource> getResourcesOnProject(Connection conn, String projectID, Map<String, Object> pagingSearchParams) throws ServiceException {

        String GET_COUNT = "SELECT COUNT(resourceid) AS count FROM proj_resources WHERE projid=?";

        String GET_RESOURCES = "SELECT prt.typename, proj_resources.*, "
                + "CASE WHEN inuseflag = 0 THEN 'Inactive' ELSE proj_resourcecategory.categoryname END AS categoryname, "
                + "colorcode, resourcename AS Name, wuvalue FROM proj_resources "
                + "INNER JOIN proj_resourcecategory ON proj_resourcecategory.categoryid=proj_resources.categoryid "
                + "INNER JOIN proj_resourcetype prt ON prt.typeid=proj_resources.typeid "
                + "WHERE projid=?";

        String searchString = "";
        String[] searchStrObj = new String[]{"proj_resources.resourcename"};
        int limit = 100, offset = 0;

        if (pagingSearchParams.containsKey("ss")) {
            searchString = (String) pagingSearchParams.get("ss");
        }
        if (pagingSearchParams.containsKey("limit")) {
            limit = (Integer) pagingSearchParams.get("limit");
        }
        if (pagingSearchParams.containsKey("offset")) {
            offset = (Integer) pagingSearchParams.get("offset");
        }

        searchString = StringUtil.getSearchString(searchString, "AND", searchStrObj);

        query = new StringBuilder();
        query.append(GET_COUNT);
        query.append(searchString);

        params = new ArrayList();
        params.add(projectID);

        DbResults rs = DbUtil.executeQuery(conn, query.toString(), params.toArray());
        if (rs.next()) {
            resultTotalCount = rs.getInt("count");
        }

        query = new StringBuilder();
        query.append(GET_RESOURCES);
        query.append(searchString);
        query.append(" LIMIT ? OFFSET ? ");

        params.clear();
        params.add(projectID);
        params.add(limit);
        params.add(offset);

        rs = DbUtil.executeQuery(conn, query.toString(), params.toArray());
        List<ProjectResource> ll = new ArrayList<ProjectResource>(rs.size());
        while(rs.next()){
            loadProjectResource(rs);
            ll.add(projectResource);
        }
        return ll;
    }
    
    @Override
    public List<ProjectResource> getResourcesOnProject(Connection conn, String projectID) throws ServiceException {

        String GET_RESOURCES = "SELECT * FROM " +
                "((SELECT (CASE inuseflag WHEN '1' THEN CONCAT(resourcename,'[',proj_resourcecategory.categoryname,']') ELSE CONCAT(resourcename,'[Inactive]')END) AS resourcename, " +
                        "proj_resources.resourceid, stdrate, proj_resources.typeid as type, proj_resourcetype.typename, colorcode, billable, resourcename AS nickname, inuseflag, wuvalue " +
                    "FROM proj_resources INNER JOIN proj_resourcetype ON proj_resourcetype.typeid=proj_resources.typeid " +
                    "INNER JOIN proj_resourcecategory ON proj_resourcecategory.categoryid=proj_resources.categoryid " +
                    "WHERE projid= ? AND proj_resources.resourceid NOT IN (SELECT userid FROM projectmembers WHERE projectid = ?))" +
                    "UNION" +
                "(SELECT (CASE inuseflag WHEN '1' THEN CONCAT(users.fname,' ',users.lname,'[',proj_resourcecategory.categoryname,']') ELSE CONCAT(users.fname,' ',users.lname,'[Inactive]') END) AS resourcename," +
                    "proj_resources.resourceid, stdrate, proj_resources.typeid as type, proj_resourcetype.typename, colorcode, billable, userlogin.username AS nickname, inuseflag, wuvalue " +
                    "FROM proj_resources INNER JOIN users ON proj_resources.resourceid = users.userid inner join userlogin on users.userid = userlogin.userid " +
                    "INNER JOIN proj_resourcetype ON proj_resourcetype.typeid=proj_resources.typeid " +
                    "INNER JOIN proj_resourcecategory ON proj_resourcecategory.categoryid=proj_resources.categoryid " +
                    "WHERE projid= ?)) AS temp ORDER BY inuseflag DESC;";

        query = new StringBuilder();
        query.append(GET_RESOURCES);

        params = new ArrayList();
        params.add(projectID);
        params.add(projectID);
        params.add(projectID);

        DbResults rs = DbUtil.executeQuery(conn, query.toString(), params.toArray());
        List<ProjectResource> ll = new ArrayList<ProjectResource>(rs.size());
        while(rs.next()){
            loadProjectResource(rs);
            ll.add(projectResource);
        }
        resultTotalCount = rs.size();
        return ll;
    }

    @Override
    public int getTotalCount() {
        return resultTotalCount;
    }

    @Override
    public List<ProjectResource> getResourcesOnProject(Connection conn, String projectID, boolean fetchBillableOnly) throws ServiceException {
        query = new StringBuilder();
        String GET_BILLABLE_RESOURCES = "SELECT pr.*, pr.resourcename as Name, prt.typename, prc.categoryname FROM proj_resources pr "
                + "INNER JOIN proj_resourcetype prt ON prt.typeid = pr.typeid "
                + "INNER JOIN proj_resourcecategory prc ON prc.categoryid = pr.categoryid "
                + "WHERE projid=? AND pr.inuseflag = 1 ORDER BY pr.typeid";
     
        if(fetchBillableOnly)
            GET_BILLABLE_RESOURCES = "SELECT pr.*, pr.resourcename as Name, prt.typename, prc.categoryname FROM proj_resources pr "
                + "INNER JOIN proj_resourcetype prt ON prt.typeid = pr.typeid "
                + "INNER JOIN proj_resourcecategory prc ON prc.categoryid = pr.categoryid "
                + "WHERE projid=? AND pr.billable = 1 AND pr.inuseflag = 1 ORDER BY pr.typeid";
            
        query.append(GET_BILLABLE_RESOURCES);

        params = new ArrayList();
        params.add(projectID);

        DbResults rs = DbUtil.executeQuery(conn, query.toString(), params.toArray());
        List<ProjectResource> ll = new ArrayList<ProjectResource>(rs.size());
        while(rs.next()){
            loadProjectResource(rs);
            ll.add(projectResource);
        }
        resultTotalCount = rs.size();
        return ll;
    }
    
    @Override
    public List<BaselineResource> getResourcesOnBaseline(Connection conn, String baselineID, boolean fetchBillableOnly) throws ServiceException {
        query = new StringBuilder();
        String GET_BILLABLE_RESOURCES = "SELECT brd.*, prt.typename, prc.categoryname FROM proj_baselineresourcesdata brd "
                + "INNER JOIN proj_resourcetype prt ON prt.typeid = brd.typeid "
                + "INNER JOIN proj_resourcecategory prc ON prc.categoryid = brd.categoryid "
                + "WHERE baselineid=? AND brd.inuseflag = 1 ORDER BY brd.typeid";
     
        if(fetchBillableOnly)
            GET_BILLABLE_RESOURCES = "SELECT brd.*, prt.typename, prc.categoryname FROM proj_baselineresourcesdata brd "
                + "INNER JOIN proj_resourcetype prt ON prt.typeid = brd.typeid "
                + "INNER JOIN proj_resourcecategory prc ON prc.categoryid = brd.categoryid "
                + "WHERE baselineid=? AND brd.inuseflag = 1 ORDER BY brd.typeid";
            
        query.append(GET_BILLABLE_RESOURCES);

        params = new ArrayList();
        params.add(baselineID);

        DbResults rs = DbUtil.executeQuery(conn, query.toString(), params.toArray());
        List<BaselineResource> ll = new ArrayList<BaselineResource>(rs.size());
        while(rs.next()){
            loadBaselineResource(rs);
            ll.add(baselineResource);
        }
        resultTotalCount = rs.size();
        return ll;
    }

    @Override
    public List<ResourceCategory> getResourcesCategories(Connection conn) throws ServiceException {
        query = new StringBuilder("SELECT categoryid, categoryname FROM proj_resourcecategory");
        
        DbResults rs = DbUtil.executeQuery(conn, query.toString());
        List<ResourceCategory> ll = new ArrayList<ResourceCategory>(rs.size());
        while(rs.next()){
            projectResource = new ProjectResource();
            ResourceCategory rc = getResourceCategoryObject(projectResource, rs);
            ll.add(rc);
        }
        resultTotalCount = rs.size();
        return ll;
    }

    @Override
    public ResourceCategory getResourcesCategory(Connection conn, String categoryID) throws ServiceException {
        query = new StringBuilder("SELECT categoryid, categoryname FROM proj_resourcecategory WHERE categoryid = ?");
        ResourceCategory rc = null;
        
        params = new ArrayList();
        params.add(categoryID);
        
        DbResults rs = DbUtil.executeQuery(conn, query.toString(), params.toArray());
        if(rs.next()){
            projectResource = new ProjectResource();
            rc = getResourceCategoryObject(projectResource, rs);
        }
        return rc;
    }

    @Override
    public List<ResourceType> getResourcesTypes(Connection conn) throws ServiceException {
        query = new StringBuilder("SELECT typeid, typename FROM proj_resourcetype");
        
        DbResults rs = DbUtil.executeQuery(conn, query.toString());
        List<ResourceType> ll = new ArrayList<ResourceType>(rs.size());
        while(rs.next()){
            projectResource = new ProjectResource();
            ResourceType rt = getResourceTypeObject(projectResource, rs);
            ll.add(rt);
        }
        resultTotalCount = rs.size();
        return ll;
    }
    
    @Override
    public double getResourcesWUValue(Connection conn, String resourceID) throws ServiceException {
        double val = 0;
        query = new StringBuilder("SELECT wuvalue FROM proj_resource WHERE resourceid = ?");
        
        params = new ArrayList();
        params.add(resourceID);
        
        DbResults rs = DbUtil.executeQuery(conn, query.toString(), params.toArray());
        if(rs.next()){
            val = rs.getDouble("wuvalue");
        }
        return val;
    }

    @Override
    public void setBillable(Connection conn, String[] resIDS, String projectID, boolean billable) throws ServiceException {
        query = new StringBuilder("UPDATE proj_resources SET billable = ? WHERE resourceid=? and projid = ?");
        resultTotalCount = 0;

        params = new ArrayList();
        
        for (int i = 0; i < resIDS.length; i++) {
            params.clear();
            params.add(billable);
            
            String resourceID = resIDS[i];
            params.add(resourceID);
            params.add(projectID);
            
            resultTotalCount += DbUtil.executeUpdate(conn, query.toString(), params.toArray());
        }
    }

    @Override
    public String createResourceCategory(Connection conn, String categoryName) throws ServiceException {
        String result = KWLErrorMsgs.rsSuccessFalse;
        String typename = StringUtil.serverHTMLStripper(categoryName);
        if(StringUtil.isNullOrEmpty(typename)){
            result = KWLErrorMsgs.errSuccessFalseResourceInvalid;
            return result;
        }
        
        params = new ArrayList();
        params.add(categoryName);
        
        DbResults rs = DbUtil.executeQuery(conn, "SELECT COUNT(categoryid) AS cnt FROM proj_resourcecategory WHERE categoryname = ?", params.toArray());
        int num = 0;
        if(rs.next())
            num = rs.getInt("cnt");
        if(num == 0){
            num = DbUtil.executeUpdate(conn, "INSERT INTO proj_resourcecategory (categoryname) VALUES (?)", params.toArray());
            if(num == 1)
                result = KWLErrorMsgs.rsSuccessTrue;
        } else
            result = KWLErrorMsgs.errSuccessFalseResourceExists;
        return result;
    }

    @Override
    public ResourceType getResourcesType(Connection conn, String typeID) throws ServiceException {
        query = new StringBuilder("SELECT typeid, typename FROM proj_resourcetype WHERE typeid = ?");
        ResourceType rt = null;
        
        params = new ArrayList();
        params.add(typeID);
        
        DbResults rs = DbUtil.executeQuery(conn, query.toString(), params.toArray());
        if(rs.next()){
            projectResource = new ProjectResource();
            rt = getResourceTypeObject(projectResource, rs);
        }
        return rt;
    }

    @Override
    public String getResourcesCategories(Connection conn, String projectID) throws ServiceException {
        try{
            JSONObject jobj = new JSONObject();
            query = new StringBuilder("SELECT DISTINCT categoryid, count, categoryname FROM "
                    + "(SELECT DISTINCT prc.categoryid, COUNT(pr.categoryid) as count, categoryname FROM proj_resourcecategory prc "
                    + "INNER JOIN proj_resources pr ON prc.categoryid = pr.categoryid WHERE pr.projid = ? GROUP BY pr.categoryid) as t");

            params = new ArrayList();
            params.add(projectID);

            DbResults rs = DbUtil.executeQuery(conn, query.toString(), params.toArray());
            while(rs.next()){
                JSONObject jtemp = new JSONObject();
                jtemp.put("categoryname", rs.getString("categoryname"));
                jtemp.put("count", rs.getInt("count"));
                jobj.append("data", jtemp);
            }
            String returnStr = jobj.toString();

            resultTotalCount = rs.size();
            return returnStr;
        } catch(JSONException e){
            throw ServiceException.FAILURE("Error while creating string ", e);
        }
    }

    @Override
    public List<ProjectResource> getResourcesOnTask(Connection conn, String projectID, String taskID, boolean fetchBillableOnly) throws ServiceException {

        String GET_RESOURCES = "select proj_resources.*, proj_resourcecategory.categoryname, proj_resourcetype.typename from proj_resources "
                    + "inner join proj_taskresourcemapping on proj_resources.resourceid = proj_taskresourcemapping.resourceid "
                    + "inner join proj_resourcecategory on proj_resources.categoryid = proj_resourcecategory.categoryid "
                    + "inner join proj_resourcetype on proj_resources.typeid = proj_resourcetype.typeid "
                    + "where proj_taskresourcemapping.taskid = ? and proj_resources.projid = ? and proj_resources.inuseflag = 1";

        if(fetchBillableOnly)
            GET_RESOURCES = "select proj_resources.*, proj_resourcecategory.categoryname, proj_resourcetype.typename from proj_resources "
                    + "inner join proj_taskresourcemapping on proj_resources.resourceid = proj_taskresourcemapping.resourceid "
                    + "inner join proj_resourcecategory on proj_resources.categoryid = proj_resourcecategory.categoryid "
                    + "inner join proj_resourcetype on proj_resources.typeid = proj_resourcetype.typeid "
                    + "where proj_taskresourcemapping.taskid = ? and proj_resources.projid = ? and proj_resources.inuseflag = 1 and proj_resources.billable = 1";
        
        query = new StringBuilder();
        query.append(GET_RESOURCES);

        params = new ArrayList();
        params.add(taskID);
        params.add(projectID);

        DbResults rs = DbUtil.executeQuery(conn, query.toString(), params.toArray());
        List<ProjectResource> ll = new ArrayList<ProjectResource>(rs.size());
        while(rs.next()){
            loadProjectResource(rs);
            ll.add(projectResource);
        }
        resultTotalCount = rs.size();
        return ll;
    }

    @Override
    public boolean isResourceExists(Connection conn, String resourceName, int resourceInWhichModule, String moduleSpecificID) throws ServiceException {
        query = new StringBuilder();
        
        switch(resourceInWhichModule){
            case RESOURCE_FROM_PROJECT:
                query.append("SELECT * FROM proj_resources WHERE resourcename = ? AND projid = ?");
                break;
            case RESOURCE_FROM_BASELINE:
                query.append("SELECT * FROM proj_baselineresourcesdata WHERE resourcename = ? AND baselineid = ?");
                break;
        }
        params = new ArrayList();
        params.add(resourceName);
        params.add(moduleSpecificID);
        
        DbResults rs = DbUtil.executeQuery(conn, query.toString(), params.toArray());
        return rs.next();
    }

    @Override
    public int createProjectResource(Connection conn, String resourceID, String resourceName, String stdRate, int typeID, String colorCode, int categoryID, int wuValue, String projectID) throws ServiceException {
        query = new StringBuilder();
        query.append("INSERT INTO proj_resources (resourceid, resourcename, projid, stdrate, typeid, colorcode, categoryid, wuvalue) VALUES (?,?,?,?,?,?,?,?)");
        
        params = new ArrayList();
        params.add(resourceID);
        params.add(resourceName);
        params.add(projectID);
        params.add(stdRate);
        params.add(typeID);
        params.add(colorCode);
        params.add(categoryID);
        params.add(wuValue);

        int count = DbUtil.executeUpdate(conn, query.toString(), params.toArray());
        return count;
    }
    
    @Override
    public int editProjectResource(Connection conn, String resourceID, String stdRate, int typeID, String colorCode, int categoryID, int wuValue, String projectID) throws ServiceException {
        query = new StringBuilder();
        query.append("UPDATE proj_resources SET stdrate=?, typeid=?, colorcode=?, categoryid=?, wuvalue=?"
                    + " WHERE proj_resources.resourceid=? and projid = ?");
        
        params = new ArrayList();
        params.add(stdRate);
        params.add(typeID);
        params.add(colorCode);
        params.add(categoryID);
        params.add(wuValue);
        params.add(resourceID);
        params.add(projectID);

        int count = DbUtil.executeUpdate(conn, query.toString(), params.toArray());
        return count;
    }

    @Override
    public List<BaselineResource> getResourcesOnBaselineTask(Connection conn, String baselineID, String taskID, String companyID, boolean fetchBillableOnly) throws ServiceException {
        query = new StringBuilder();
        String GET_BASELINE_RESOURCES = " SELECT brd.*, prt.typename, prc.categoryname FROM proj_baselineresourcesdata brd "
                + "INNER JOIN proj_baselinetaskresources btr ON brd.resourceid = btr.resourceid "
                + "INNER JOIN proj_resourcecategory prc ON prc.categoryid = brd.categoryid "
                + "INNER JOIN proj_resourcetype prt ON prt.typeid = brd.typeid "
                + "WHERE brd.resourceid NOT IN (SELECT userlogin.userid FROM userlogin "
                + "INNER JOIN users ON users.userid = userlogin.userid WHERE users.companyid = ? AND userlogin.isactive = false) "
                + "AND btr.taskid = ? AND brd.baselineid = ? AND btr.baselineid = ? "
                + "ORDER BY brd.resourcename ";
        
        if(fetchBillableOnly)
            GET_BASELINE_RESOURCES = " SELECT brd.*, prt.typename, prc.categoryname FROM proj_baselineresourcesdata brd "
                + "INNER JOIN proj_baselinetaskresources btr ON brd.resourceid = btr.resourceid "
                + "INNER JOIN proj_resourcecategory prc ON prc.categoryid = brd.categoryid "
                + "INNER JOIN proj_resourcetype prt ON prt.typeid = brd.typeid "
                + "WHERE brd.resourceid NOT IN (SELECT userlogin.userid FROM userlogin "
                + "INNER JOIN users ON users.userid = userlogin.userid WHERE users.companyid = ? AND userlogin.isactive = false) "
                + "AND btr.taskid = ? AND brd.baselineid = ? AND btr.baselineid = ? AND brd.billable = true "
                + "ORDER BY brd.resourcename ";
        
        query.append(GET_BASELINE_RESOURCES);
        
        params = new ArrayList();
        params.add(companyID);
        params.add(taskID);
        params.add(baselineID);
        params.add(baselineID);
        
        DbResults rs = DbUtil.executeQuery(conn, query.toString(), params.toArray());
        List<BaselineResource> ll = new ArrayList<BaselineResource>(rs.size());
        while(rs.next()){
            loadBaselineResource(rs);
            ll.add(baselineResource);
        }
        resultTotalCount = rs.size();
        return ll;
    }
    
    @Override
    public int updateResource(Connection conn, Resource r, int resourceInWhichModule) throws ServiceException{
        int res = 0;
        String moduleID = "";
        query = new StringBuilder();
        switch(resourceInWhichModule){
            case RESOURCE_FROM_PROJECT:
                ProjectResource pr = (ProjectResource) r;
                moduleID = pr.getProjectID();
                query.append("UPDATE proj_resources SET stdrate=?, typeid=?, colorcode=?, categoryid=?, wuvalue=?"
                    + " WHERE proj_resources.resourceid = ? and projid = ?");
                break;
            case RESOURCE_FROM_BASELINE:
                BaselineResource br = (BaselineResource) r;
                moduleID = br.getBaselineID();
                query.append("UPDATE proj_resources SET stdrate=?, typeid=?, colorcode=?, categoryid=?, wuvalue=?"
                    + " WHERE proj_resources.resourceid = ? and baselineid = ?");
                break;
        }
        if(!StringUtil.isNullOrEmpty(moduleID) && !StringUtil.isNullOrEmpty(query.toString())){
            res = DbUtil.executeUpdate(conn, query.toString(), new Object[]{r.getStdRate(), r.getType().getTypeID(), r.getColorCode(),
                                    r.getCategory().getCategoryID(), r.getWuvalue(), r.getResourceID(), moduleID});
        }
        return res;
    }
    
}
