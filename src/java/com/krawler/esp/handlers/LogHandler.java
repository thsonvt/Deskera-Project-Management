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
package com.krawler.esp.handlers;

import java.sql.PreparedStatement;
import java.util.ArrayList;

import com.krawler.common.service.ServiceException;
import com.krawler.database.DbPool;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.database.DbPool.Connection;
import com.krawler.esp.user.UserDAO;
import com.krawler.esp.user.UserDAOImpl;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class LogHandler {
	public static void InsertLogForCalendar(Connection conn, String loginId,
			String byId, String toId, int actionId) throws ServiceException {
		PreparedStatement pstmt = null;
		String query = null;
		java.util.Date dateval = new java.util.Date(System.currentTimeMillis());
		java.text.SimpleDateFormat dtformat = new java.text.SimpleDateFormat(
				"yyyy-MM-d HH:mm:ss");
		String currTs = dtformat.format(dateval);
		try {
			query = "select count(*) as count from actionlog where `by` = ? and `to` = ? and actionid = ?";
			DbResults res = DbUtil.executeQuery(conn, query, new Object[] {
					byId, toId, actionId });
			res.next();
			int count = res.getInt("count");

			query = "select actionname from actions where actionid = ? ";
			res = DbUtil.executeQuery(conn, query, actionId);
			res.next();
			String actionName = res.getString("actionname");

//			query = "select username from userlogin where userid = ? ";
//			res = DbUtil.executeQuery(conn, query, loginId);
//			res.next();
			String username = new UserDAOImpl().getUser(conn, loginId).getUserName();//res.getString("username");

			query = "select cname, caltype, userid from calendars where cid = ? ";
			res = DbUtil.executeQuery(conn, query, toId);
			res.next();
			String cname = res.getString("cname");
			String projid = res.getString("userid");
			int caltype = res.getInt("caltype");

                        HashMap info=new HashMap(); 
                        String [] ob= new String[3];
                        info.put("id",loginId);
                        info.put("value",username);
                        ob[0]=getTags("u",info);
                        info.clear();
                        
                        info.put("id",toId);
                        info.put("value",cname);
                        ob[1]=getTags("y",info);
                        info.clear();
                        
                        ob[2]=getActionLogTime(dateval);
                        actionName = String.format(actionName, (Object[])ob);                        
                        
			if (count == 0) {
				query = "insert into actionlog (`userid`, `datedon`, `by`, `actionid`, `to`, `actiontext`) values ( ?, ?, ?, ?, ?, ?)";
				DbUtil.executeUpdate(conn, query, new Object[] { toId, currTs,
						byId, actionId, toId, actionName });
			} else {
				query = "update actionlog set `datedon` = ?, actiontext = ? where `by` = ? and `to`= ? and actionid= ?";
				DbUtil.executeUpdate(conn, query, new Object[] { currTs,
						actionName, byId, toId, actionId });
			}
		} catch (Exception e) {
			throw ServiceException
					.FAILURE("LogHandler.InsertLogForCalendar", e);
		} finally {
			DbPool.closeStatement(pstmt);
		}
	}

	public static void InsertLogForProjectCalendar(Connection conn,
			String projid, String loginId, String byId, String toId,
			int actionId) throws ServiceException {
		PreparedStatement pstmt = null;
		String query = null;
		java.util.Date dateval = new java.util.Date(System.currentTimeMillis());
		java.text.SimpleDateFormat dtformat = new java.text.SimpleDateFormat(
				"yyyy-MM-d HH:mm:ss");
		String currTs = dtformat.format(dateval);
		try {
			query = "select count(*) as count from actionlog where `by` = ? and `to` = ? and actionid = ? and userid = ?";
			DbResults res = DbUtil.executeQuery(conn, query, new Object[] {
					byId, projid, actionId, toId });
			res.next();
			int count = res.getInt("count");

			query = "select actionname from actions where actionid = ? ";
			res = DbUtil.executeQuery(conn, query, actionId);
			res.next();
			String actionName = res.getString("actionname");

//			query = "select username from userlogin where userid = ? ";
//			res = DbUtil.executeQuery(conn, query, loginId);
//			res.next();
			String username =new UserDAOImpl().getUser(conn, loginId).getUserName();// res.getString("username");

			query = "select cname, caltype, userid from calendars where cid = ? ";
			res = DbUtil.executeQuery(conn, query, toId);
			res.next();
			String cname = res.getString("cname");
			String projectid = res.getString("userid");
			int caltype = res.getInt("caltype");

			query = "SELECT projectname FROM project WHERE projectid=?";
			res = DbUtil.executeQuery(conn, query, new Object[] { projectid });
			res.next();
			String projname = res.getString("projectname");
                        
                        HashMap info=new HashMap(); 
                        String [] ob= new String[4];
                        info.put("id",loginId);
                        info.put("value",username);
                        ob[0]=getTags("u",info);
                        info.clear();
                        
                        info.put("id",toId);
                        info.put("ti",projectid);
                        info.put("tt",projname);                        
                        info.put("value",cname);
                        ob[1]=getTags("x",info);
                        info.clear();
                        
                        info.put("id",projid);
                        info.put("value",projname);
                        ob[2]=getTags("q",info);
                        info.clear();
                        
                        ob[3]=getActionLogTime(dateval);                        
                        actionName = String.format(actionName, (Object[])ob);                        

			if (count == 0) {
				query = "insert into actionlog (`userid`, `datedon`, `by`, `actionid`, `to`, `actiontext`) values ( ?, ?, ?, ?, ?, ?)";
				DbUtil.executeUpdate(conn, query, new Object[] { toId, currTs,
						loginId, actionId, projectid, actionName });
			} else {
				query = "update actionlog set `datedon` = ?, actiontext = ? where `by` = ? and `to`= ? and actionid= ?";
				DbUtil.executeUpdate(conn, query, new Object[] { currTs,
						actionName, loginId, projectid, actionId });
			}
		} catch (Exception e) {
			throw ServiceException
					.FAILURE("LogHandler.InsertLogForCalendar", e);
		} finally {
			DbPool.closeStatement(pstmt);
		}
	}

	public static void InsertLogForProject(Connection conn, String loginId,
			String byId, String toId, int actionId) throws ServiceException {
		PreparedStatement pstmt = null;
		String query = null;
		java.util.Date dateval = new java.util.Date(System.currentTimeMillis());
		java.text.SimpleDateFormat dtformat = new java.text.SimpleDateFormat(
				"yyyy-MM-d HH:mm:ss");
		String currTs = dtformat.format(dateval);
		try {
			query = "select count(*) as count from actionlog where actionlog.by = ? and actionlog.to = ? and actionlog.actionid = ?";
			DbResults res = DbUtil.executeQuery(conn, query, new Object[] {
					byId, toId, actionId });

			res.next();
			int count = res.getInt("count");

			query = "select actions.actionname from actions where actions.actionid = ? ";
			res = DbUtil.executeQuery(conn, query, actionId);
			res.next();
			String actionName = res.getString("actionname");

//			query = "select username from userlogin where userid = ? ";
//			res = DbUtil.executeQuery(conn, query, loginId);
//			res.next();
			String username =new UserDAOImpl().getUser(conn, loginId).getUserName();// res.getString("username");

			query = "select projectname from project where projectid = ? ";
			res = DbUtil.executeQuery(conn, query, toId);
			res.next();
			String projectname = res.getString("projectname");

                        HashMap info=new HashMap(); 
                        String [] ob= new String[3];
                        info.put("id",loginId);
                        info.put("value",username);
                        ob[0]=getTags("u",info);
                        info.clear();
                        
                        info.put("id",toId);
                        info.put("value",projectname);
                        ob[1]=getTags("p",info);
                        info.clear();
                        
                        ob[2]=getActionLogTime(dateval);
                        actionName = String.format(actionName, (Object[])ob);                        
                        
			if (count == 0) {
				query = "insert into actionlog (`userid`, `datedon`, `by`, `actionid`, `to`, `actiontext`) values ( ?, ?, ?, ?, ?, ?)";
				DbUtil.executeUpdate(conn, query, new Object[] { byId, currTs,
						byId, actionId, toId, actionName });
			} else {
				query = "update actionlog set actionlog.datedon = ?, actionlog.actiontext = ? where actionlog.userid = ? and actionlog.by = ? and actionlog.to= ? and actionlog.actionid= ?";
				DbUtil.executeUpdate(conn, query, new Object[] { currTs,
						actionName, byId, byId, toId, actionId });
			}
		} catch (Exception e) {
			throw ServiceException.FAILURE("LogHandler.InsertLogForProject", e);
		} finally {
			DbPool.closeStatement(pstmt);
		}
	}

	public static void InsertLogForDocs(Connection conn, String loginId,
			String byId, String toId, int actionId, String docname, String pcid)
			throws ServiceException {
		PreparedStatement pstmt = null;
		String query = null;
		java.util.Date dateval = new java.util.Date(System.currentTimeMillis());
		java.text.SimpleDateFormat dtformat = new java.text.SimpleDateFormat(
				"yyyy-MM-d HH:mm:ss");
		String currTs = dtformat.format(dateval);
               try {
			query = "select count(*) as count from actionlog where actionlog.by = ? and actionlog.to = ? and actionlog.actionid = ?";
			DbResults res = DbUtil.executeQuery(conn, query, new Object[] {
					byId, toId, actionId });
			res.next();
			int count = res.getInt("count");

			query = "select actions.actionname from actions where actions.actionid = ? ";
			res = DbUtil.executeQuery(conn, query, actionId);
			res.next();
			String actionName = res.getString("actionname");

//			query = "select userlogin.username from userlogin where userlogin.userid = ? ";
//			DbResults res1 = DbUtil.executeQuery(conn, query, loginId);
//			res1.next();
			String username = new UserDAOImpl().getUser(conn, loginId).getUserName();//res1.getString("username");
			if (docname == null) {
				query = "select docname from docs where docid = ? ";
				DbResults res1 = DbUtil.executeQuery(conn, query, toId);
				res1.next();
				docname = res1.getString("docname");
			}
			actionName = getactionName(conn, byId, toId, actionId, actionName,
					username, docname, dateval, pcid);
			if (count == 0) {
				query = "insert into actionlog (`userid`, `datedon`, `by`, `actionid`, `to`, `actiontext`) values ( ?, ?, ?, ?, ?, ?)";
				if (pcid != null) {
					toId = pcid;
				}
				DbUtil.executeUpdate(conn, query, new Object[] { byId, currTs,
						byId, actionId, toId, actionName });
			} else {
				query = "update actionlog set actionlog.datedon = ?, actionlog.actiontext = ? where actionlog.userid = ? and actionlog.by = ? and actionlog.to= ? and actionlog.actionid= ?";
				if (pcid != null) {
					toId = pcid;
				}
				DbUtil.executeUpdate(conn, query, new Object[] { currTs,
						actionName, byId, byId, toId, actionId });
			}
		} catch (Exception e) {
			throw ServiceException.FAILURE("LogHandler.insertLog", e);
		} finally {
			DbPool.closeStatement(pstmt);
		}
	}

	public static String getactionName(Connection conn, String userid,
			String docId, int actionId, String actionName, String username,
			String docname, java.util.Date currTs, String pcid) throws ServiceException {
		String query = null;
		// pcid is used to determine whether document is related to community or
		// project
		if (pcid == null) {
			if (actionId >= 61 && actionId <= 65 || actionId == 71) {

				query = "select communityname,communityid from community where communityid=(select userid from docprerel where docid=? and permission =?)";
				DbResults res = DbUtil.executeQuery(conn, query, new Object[] {
						docId, "6" });
				res.next();
				String actionname = res.getString("communityname");

                                String compid = res.getString("communityid");
                                //String ab = getActionTag("c",compid,"tab"+compid,actionname,actionname);
                             HashMap info=new HashMap(); 
                             String [] ob= new String[3];
                              info.put("id",userid);
                              info.put("value",username);
                              ob[0]=getTags("u",info);
                              info.clear();
                              info.put("id",compid);
                              info.put("tt",actionname);
                              info.put("value",docname);
                              ob[1]=getTags("n",info);
                              info.clear();
                              info.put("id",compid);
                              info.put("value",actionname);
                              ob[2]=getTags("c",info); 
                              actionName = String.format(actionName, (Object[])ob) ; 
			
			} else if (actionId >= 66 && actionId <= 70 || actionId ==72) {
				query = "select projectname,projectid  from project where projectid=(select userid from docprerel where docid=? and permission =?)";
				DbResults res = DbUtil.executeQuery(conn, query, new Object[] {
						docId, "5" });
				res.next();
				String actionname = res.getString("projectname");
                                String compid = res.getString("projectid");
                                 HashMap info=new HashMap(); 
                                    String [] ob= new String[3];
                                  info.put("id",userid);
                                  info.put("value",username);
                                  ob[0]=getTags("u",info);
                                  info.clear();
                                  info.put("id",compid);
                                  info.put("tt",actionname);
                                  info.put("value",docname);
                                  ob[1]=getTags("m",info); 
                                  info.clear();
                                  info.put("id",compid);
                                  info.put("value",actionname);
                                  ob[2]=getTags("q",info); 
                                  actionName =String.format(actionName,(Object[])ob);
                              } else {
                                  HashMap info=new HashMap(); 
                                  String [] ob= new String[3];
                                  info.put("id",userid);
                                  info.put("value",username);
                                  ob[0]=getTags("u",info);
                                  info.clear();
                                  info.put("id",docId);
                                  info.put("tt","My Documents");
                                  info.put("value",docname);
                                  ob[1]=getTags("d",info);
                                  ob[2]=getActionLogTime(currTs);                                  
                                actionName = String.format(actionName, (Object[])ob) ;    
                            
			}
		} else {
			boolean flg = false;
                        
			if (actionId >=61 && actionId <=65 || actionId ==71) {
				query = "select communityname as name from community where communityid=?";
				flg = true;
			} else if (actionId >= 66 && actionId <= 70 || actionId ==72) {
				query = "select projectname as name from project where projectid=?";
			}
			DbResults res = DbUtil.executeQuery(conn, query,
					new Object[] { pcid });
			res.next();
			String actionname = res.getString("name");
                        HashMap info=new HashMap(); 
                                  String [] ob= new String[3];
                                  info.put("id",userid);
                                  info.put("value",username);
                                  ob[0]=getTags("u",info);
                                  info.clear();
                        
			if (flg) {
                                  info.put("id",pcid);
                                  info.put("value",docname);
                                  info.put("tt",actionname);
                                  ob[1]=getTags("n",info);
                                  info.clear();
                                  info.put("id",pcid);
                                  info.put("value",actionname);
                                  ob[2]=getTags("c",info); 
			
			} else {
                                  info.put("id",pcid);
                                  info.put("value",docname);
                                  info.put("tt",actionname);
                                  ob[1]=getTags("m",info); 
                                  info.put("id",pcid);
                                  info.put("value",actionname);
                                  ob[2]=getTags("q",info); 
			}
                        actionName = String.format(actionName, (Object[])ob) ;  
		}
		return actionName;

	}

	public static ArrayList<String> getdocGroup(Connection conn, String docid)
			throws ServiceException {
		// PreparedStatement pstmt = null;
		String query = null;
		ArrayList<String> perm = new ArrayList<String>();
		query = "select permission ,userid from docprerel where docid=?";
		DbResults res = DbUtil
				.executeQuery(conn, query, new Object[] { docid });
		res.next();
		perm.add(res.getString("permission"));
		perm.add(res.getString("userid"));
		return perm;
	}

	public static boolean isShared(Connection conn, String docid)
			throws ServiceException {
		// PreparedStatement pstmt = null;
		String query = null;
		query = "select count(*) as cnt from docprerel  where docid=?";// and
																		// permission
																		// not
																		// in
																		// ('5','6');";
		DbResults res = DbUtil
				.executeQuery(conn, query, new Object[] { docid });
		res.next();
		Integer cnt = res.getInt("cnt");
		if (cnt == 0) {
			return false;
		} else {
			return true;
		}
	}

	public static void makeLogentry(Connection conn, String docid,
			String userid, Integer proj, Integer comm, Integer shared,
			String docname, String pcid,String companyid) throws ServiceException {

		if (isShared(conn, docid)) {
			ArrayList<String> tmp = getdocGroup(conn, docid);
			String group = tmp.get(0);
			if (group.compareTo("5") == 0) {
				// project
				if (pcid == null) {
					pcid = tmp.get(1);
				}
				InsertLogForDocs(conn, userid, userid, docid, proj, docname,
						pcid);

			} else if (group.compareTo("6") == 0) {
				// community
				if (pcid == null) {
					pcid = tmp.get(1);
				}
				InsertLogForDocs(conn, userid, userid, docid, comm, docname,
						pcid);
			} else {
				// shared docs
				if(shared==20){  
                                      InsertLogForDocs(conn, userid, userid,companyid, shared, docname, pcid);
                                 }
                               else{
                                     InsertLogForDocs(conn, userid, userid, docid, shared, docname, pcid);
                              }
			}
		}
	}

	public static Integer getLogID(Connection conn, String docid,
			String userid, int actionid) throws ServiceException {
		String query = null;
		query = "select logid from actionlog where `to`= ? and `by`= ? and userid =? and actionid =? ";
		DbResults res = DbUtil.executeQuery(conn, query, new Object[] { docid,
				userid, userid, actionid });
		res.next();
		Integer perm = res.getInt("logid");
		return perm;
	}

	public static void insertInActionlogRef(Connection conn, String userid,
			Integer logid) throws ServiceException {
		String query = "insert into actionlogref (`shwith`, `logid`) values ( ?, ?)";
		DbUtil.executeUpdate(conn, query, new Object[] { userid, logid });
	}

	public static void deleteFromActionlogRef(Connection conn, Integer logid)
			throws ServiceException {

		String query = "delete from actionlogref where logid = ?";
		DbUtil.executeUpdate(conn, query, new Object[] { logid });
	}
    
	public static ArrayList<String> getList(ArrayList<String> tmp1,
			ArrayList<String> tmp) {
		ArrayList<String> a = new ArrayList<String>();
		for (int i = 0; i < tmp1.size(); i++) {
			if (tmp.indexOf(tmp1.get(i)) == -1) {
				a.add(tmp1.get(i).toString());
			}
		}
		return a;
	}

	public static String getDocName(Connection conn, String docid)
			throws ServiceException {

		String query = "select docname from docs where docid =? ";
		DbResults res = DbUtil
				.executeQuery(conn, query, new Object[] { docid });
		res.next();
		String name = res.getString("docname");
		return name;

	}

	public static String getDocpermission(Connection conn, String docid)
			throws ServiceException {

		String query = "select docper from docs where docid =? ";
		DbResults res = DbUtil
				.executeQuery(conn, query, new Object[] { docid });
		res.next();
		String per = res.getString("docper");
		return per;

	}

	public static ArrayList getSharedusers(Connection conn, String userid)
			throws ServiceException {
		ArrayList Id = new ArrayList();
		String query = "select userid from users where companyid = (select companyid from users where userid=?) and userid !=? ";
		DbResults res = DbUtil.executeQuery(conn, query, new Object[] { userid,
				userid });
		while (res.next()) {
			Id.add(res.getString("userid"));
		}
		return Id;
	}
        
        public static String getTags(String tagname, HashMap info) throws ServiceException{
          Set keys = info.keySet();         // The set of keys in the map.
           Iterator keyIter = keys.iterator();
           String text="<" + tagname + "" ;
           String text1="";
           while (keyIter.hasNext()) {
               Object key = keyIter.next();  // Get the next key.
               Object value = info.get(key);  // Get the value for that key.
               if(key.toString().compareTo("value")==0){
                    text1=">"+value.toString()+"</"+tagname+">";
                }
                else if(key.toString().compareTo("tag")!=0){
                  text+=" "+key.toString()+"='"+value.toString()+"'"; 
                }
          }
        return text.concat(text1) ;    
       
        }
        
        public static String getActionLogTime(java.util.Date dt) throws ServiceException {
            String actionTime = null;
            try {
                java.text.SimpleDateFormat dtformat = new java.text.SimpleDateFormat("h:mm a");
                actionTime = dtformat.format(dt);
            } catch (Exception e) {
                throw ServiceException.FAILURE("LogHandler.getActionLogTime", e);
            }
            return actionTime;
        }       

	public static void InsertLogForTodo(Connection conn, String taskId, String projId,
                    String creatorId, String assignedTo, String prevAssigned, String description, int actionId) throws ServiceException {
		String query = null;
		java.util.Date dateval = new java.util.Date(System.currentTimeMillis());
		java.text.SimpleDateFormat dtformat = new java.text.SimpleDateFormat(
				"yyyy-MM-d HH:mm:ss");
		String currTs = dtformat.format(dateval);
                DbResults res = null;
                String logid = null;
                try{
                    //TODO:implement the following using regex
                    description=description.substring((description.indexOf(":"))+1,description.length());
                    
                    query = "select actionlog.logid as logid1 from actionlog inner join actionlogref on actionlog.logid = actionlogref.logid where actionlogref.shwith = ? and actionlog.actionid = ?";
                    res = DbUtil.executeQuery(conn, query, new Object[] { taskId, actionId });
                    if(res.next()){
                        logid = res.getObject("logid1").toString();
                    }
                    
                    query = "select actionname from actions where actionid = ? ";
                    res = DbUtil.executeQuery(conn, query, actionId);
                    res.next();
                    String actionName = res.getString("actionname");

//                    query = "select username from userlogin where userid = ? ";
//                    res = DbUtil.executeQuery(conn, query, creatorId);
//                    res.next();
                    UserDAO udao = new UserDAOImpl();
                    String username =udao.getUser(conn, creatorId).getUserName();// res.getString("username");
                    
                    query = "SELECT projectname FROM project WHERE projectid=?";
                    res = DbUtil.executeQuery(conn, query, new Object[] { projId });
                    res.next();
                    String projname = res.getString("projectname");
                    
                    //Action Log Id=78 for reassigning todo task
                    if(actionId == 78){
//                        query = "select username from userlogin where userid = ?";
//                        res = DbUtil.executeQuery(conn, query, assignedTo);
//                        res.next();
                        String reassignedUser = udao.getUser(conn, assignedTo).getUserName();//res.getString("username");
                        
                        HashMap info=new HashMap(); 
                        String [] ob= new String[4];
                        info.put("id",creatorId);
                        info.put("value",username);
                        ob[0]=getTags("u",info);
                        info.clear();
                        
                        info.put("id",projId);
                        info.put("tt",projname);                        
                        info.put("value",description);
                        ob[1]=getTags("t",info);
                        info.clear();

                        info.put("id",assignedTo);
                        info.put("value",reassignedUser);
                        ob[2]=getTags("u",info);
                        info.clear();                        
                        
                        ob[3]=getActionLogTime(dateval);
                        actionName = String.format(actionName, (Object[])ob); 
                        
                        assignedTo = prevAssigned;
                    }
                    else{
                        HashMap info=new HashMap(); 
                        String [] ob= new String[3];
                        info.put("id",creatorId);
                        info.put("value",username);
                        ob[0]=getTags("u",info);
                        info.clear();
                        
                        info.put("id",projId);
                        info.put("tt",projname);                        
                        info.put("value",description);
                        ob[1]=getTags("t",info);
                        info.clear();

                        ob[2]=getActionLogTime(dateval);
                        actionName = String.format(actionName, (Object[])ob);                        
                    }
                    
                    if (logid == null) {
                        query = "insert into actionlog (`userid`, `datedon`, `by`, `actionid`, `to`, `actiontext`) values ( ?, ?, ?, ?, ?, ?)";
                        int rows = DbUtil.executeUpdate(conn, query, new Object[] { projId, currTs,
                                        creatorId, actionId, assignedTo, actionName });
                        if(rows>0){
                            query = "select logid from actionlog where `userid`=? and `datedon`=? and `by`= ? and `actionid`= ? and `to`= ? and `actiontext`= ?";
                            res=DbUtil.executeQuery(conn, query, new Object[] { projId, currTs,
                                            creatorId, actionId, assignedTo, actionName });
                            res.next();
                            if(res.getObject("logid") != null) {
                                logid = res.getObject("logid").toString();
                            }
                            
                            query = "insert into actionlogref (`shwith`, `logid`) values ( ?, ?)";
                            DbUtil.executeUpdate(conn, query, new Object[] { taskId, logid});
                        }
                    } else {
                        query = "update actionlog set `datedon` = ?, actiontext = ?, `to` = ? where `logid` = ?";
                        DbUtil.executeUpdate(conn, query, new Object[] { currTs, actionName, assignedTo, logid});
                    }
		} catch (Exception e) {
                    throw ServiceException.FAILURE("LogHandler.InsertLogForTodo", e);
		}
	}
        
    public static void InsertLogForAdmin(Connection conn, String loginId,
            String toId, int actionId, String byId, int flg)
            throws ServiceException {
        PreparedStatement pstmt = null;
        String query = null;
        java.util.Date dateval = new java.util.Date(System.currentTimeMillis());
        java.text.SimpleDateFormat dtformat = new java.text.SimpleDateFormat(
                "yyyy-MM-d HH:mm:ss");
        String currTs = dtformat.format(dateval);
        try {
            query = "select count(*) as count from actionlog where actionlog.by = ? and actionlog.to = ? and actionlog.actionid = ?";
            DbResults res = DbUtil.executeQuery(conn, query, new Object[]{
                byId,toId, actionId
            });
            res.next();
            int count = res.getInt("count");

            query = "select actions.actionname from actions where actions.actionid = ? ";
            res = DbUtil.executeQuery(conn, query, actionId);
            res.next();
            String actionName = res.getString("actionname");
//            query = "select username from userlogin where userid = ? ";
//            res = DbUtil.executeQuery(conn, query,byId);
//            res.next();
            String username =new UserDAOImpl().getUser(conn, byId).getUserName();// res.getString("username");
            HashMap info = new HashMap();
            String[] ob = new String[3];
            info.put("id", byId);
            info.put("value", username);
            ob[0] = getTags("u", info);
            info.clear();
            String tag = null;
            if (flg == 0) {
                query = "select communityname as name from community where communityid=?";
                tag = "c";

            } else {
                query = "select projectname as name from project where projectid=?";
                tag = "q";
            }
            res = DbUtil.executeQuery(conn, query,
                    new Object[]{toId});
            res.next();
            String pcname = res.getString("name");
            info.put("id", toId);
            info.put("value", pcname);
            ob[1] = getTags(tag, info);
            info.clear();
            ob[2] = getActionLogTime(dateval);
            actionName = String.format(actionName, (Object[])ob);
            if (count == 0) {
                query = "insert into actionlog (`userid`, `datedon`, `by`, `actionid`, `to`, `actiontext`) values ( ?, ?, ?, ?, ?, ?)";
                DbUtil.executeUpdate(conn, query, new Object[]{loginId, currTs,
                    byId, actionId, toId, actionName
                });
            } else {
                query = "update actionlog set actionlog.datedon = ?, actionlog.actiontext = ? where actionlog.userid = ? and actionlog.by = ? and actionlog.to= ? and actionlog.actionid= ?";

                DbUtil.executeUpdate(conn, query, new Object[]{currTs,
                    actionName, loginId,byId, toId, actionId
                });
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("LogHandler.insertLog", e);
        } finally {
            DbPool.closeStatement(pstmt);
        }
    }

}
