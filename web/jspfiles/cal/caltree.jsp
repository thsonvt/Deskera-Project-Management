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
<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="com.krawler.esp.database.dbcon" %>
<%@ page import="com.krawler.esp.handlers.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.sql.*" %>
<%@ page import="javax.servlet.ServletRequest"%>
<%@ page import="com.krawler.utils.json.base.*" %>
<%@page import="com.krawler.common.session.SessionExpiredException"%>
<%@ page import="com.krawler.database.DbPool"%>
<%@ page import="com.krawler.database.DbPool.Connection"%>
<%@ page import="com.krawler.common.service.ServiceException"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />

<%
com.krawler.utils.json.base.JSONObject jbj = new com.krawler.utils.json.base.JSONObject();
if (sessionbean.validateSession(request, response)) {
    jbj.put("valid", true);
        String calAction = request.getParameter("action");
        int caltype=Integer.parseInt(request.getParameter("caltype"));
        String companyid = AuthHandler.getCompanyid(request);
        String ipAddress = AuthHandler.getIPAddress(request);
        String loginid = AuthHandler.getUserid(request);
        int auditMode = 0;

        if (calAction.equals("0")) {
            com.krawler.database.DbPool.Connection conn = null;
            try{
                conn = DbPool.getConnection();
                String userid = request.getParameter("userid");
                String latestts = request.getParameter("latestts");
                String jstr="";
                String myCal = Tree.getCalendarlist(conn, userid, loginid, latestts);

                if(caltype==0){
                    if(myCal.compareTo("{data:{}}") == 0) {
                        String fullName = AuthHandler.getAuthor(conn, userid);

                        if(fullName != " "){
                            jstr = Tree.insertNewCalendar(conn, fullName, "", "", "", "0",caltype,1,userid);
                            if(!jstr.equals("0")){
                                JSONObject obj = new JSONObject(jstr);
                                loginid = obj.getJSONArray("data").getJSONObject(0).getString("userid");
                                myCal = Tree.getCalendarlist(conn, userid, loginid, latestts);
                            }
                        }
                    }

                    /*String sharedCal = Tree.getSharedCalendarlist(conn, userid);
                    if(sharedCal.compareTo("{data:{}}") != 0) {
                        JSONObject jobjMy = new JSONObject(myCal);
                        JSONObject jobjSh = new JSONObject(sharedCal);
                        for (int i = 0; i < jobjSh.getJSONArray("data").length(); i++) {
                            jobjMy.append("data",jobjSh.getJSONArray("data").get(i));
                        }
                        jbj.put("data", jobjMy.toString());
                        //out.print(jobjMy.toString());
                    }else
                        jbj.put("data",myCal);
                       // out.print(myCal);*/
                }
                else if(caltype==2){
                    if(myCal.compareTo("{data:{}}") == 0) {
                        String projName = projdb.getProjectName(conn, userid);

                        if(projName != ""){
                            jstr = Tree.insertNewCalendar(conn, projName, "", "", "", "0",caltype,1,userid);
                            if(!jstr.equals("0")) {
                                myCal = Tree.getCalendarlist(conn, userid, loginid, latestts);

                                String params = AuthHandler.getAuthor(conn, loginid) + " ("+ AuthHandler.getUserName(request) +"), " +
                                                projName + ", " + projName;
                                AuditTrail.insertLog(conn, "124", loginid, jstr, userid, companyid, params, ipAddress, auditMode);
                            }
                        }
                    }

                    if(myCal.compareTo("{data:{}}") != 0) {
                        JSONObject jobjMy = new JSONObject(myCal);
                        jbj.put("data",jobjMy.toString());
                       // out.print(jobjMy.toString());
                    }else
                        jbj.put("data",myCal);
                      //  out.print(myCal);
                }

                String sharedCal = Tree.getSharedCalendarlist(conn, userid);
                JSONObject jobjMy = new JSONObject(myCal);
                JSONObject jobjSh = new JSONObject(sharedCal);
                if(sharedCal.compareTo("{data:{}}") != 0) {
                    for (int i = 0; i < jobjSh.getJSONArray("data").length(); i++) {
                        jobjMy.append("data",jobjSh.getJSONArray("data").get(i));
                    }
                }
                jbj.put("data", jobjMy.toString());
                conn.commit();
            } catch (ServiceException ex) {
                DbPool.quietRollback(conn);
                System.out.println("calEvent:Connection Error:"+ex.toString());
            } catch(Exception e) {
                DbPool.quietRollback(conn);
                System.out.println("calendar insert error:"+e.toString());
            } finally {
                DbPool.quietClose(conn);
            }
        } else if (calAction.equals("1")) {
            com.krawler.database.DbPool.Connection conn = null;
            try{
                conn = DbPool.getConnection();
                String cname = request.getParameter("cname");
                String description = request.getParameter("description");
                String location = request.getParameter("location");
                String timezone = request.getParameter("timezone");
                String colorcode = request.getParameter("colorcode");
                int isdefault=Integer.parseInt(request.getParameter("isdefault"));
                String userid = request.getParameter("userid");
                String jstr= Tree.insertNewCalendar(conn, cname, description, location, timezone, colorcode,caltype,isdefault,userid);
                if(!jstr.equals("0")){
                    conn.commit();
                    boolean exportCal = true;
                    String exportCalCheck = request.getParameter("exportCal");
                    if(exportCalCheck.compareToIgnoreCase("false") == 0) {
                        exportCal = false;
                    }
                    if(!exportCal){
                        String calId = jstr;
                        String loginId = request.getParameter("loginid");
                        String retExport = Tree.insertExportCheck(conn, calId, loginId);
                        conn.commit();
                    }
                    String returnStr = Tree.getCalendar(conn, jstr);
                    if(caltype==0){
                        String permission = request.getParameter("permission");
                        if(permission!=""){
                            String userPermission [];
                            userPermission = permission.split(",");
                            for(int i=0;i<userPermission.length;i++){
                                String permissions [];
                                permissions = userPermission[i].split("_");
                                int per = Integer.parseInt(permissions[1]);
                                String res=Tree.insertCalPermission(conn, jstr,permissions[0],per);
                                if(!res.equals("0")){
                                    if(returnStr.compareTo("{data:{}}") != 0){
                                        JSONObject jobj = new JSONObject(returnStr.toString());
                                        jobj.getJSONArray("data").getJSONObject(0).put("permissionlevel", per);
                                        Map<String, String> data = new HashMap<String, String>();
                                        data.put("action", calAction);
                                        data.put("data", jobj.toString());
                                        data.put("success","true");
                                        ServerEventManager.publish("/calTree/"+permissions[0], data, this.getServletContext());
                                    }
                                }
                            }
                         //   if(userPermission.length != 0)
                           //     LogHandler.InsertLogForCalendar(conn, userid,userid,jstr,51);
                        }
                    }
                    else{
                        try {
                            if (sessionbean.validateSession(request, response)) {
                                String usrid=AuthHandler.getUserid(request);
                                //LogHandler.InsertLogForProjectCalendar(conn, userid,usrid,usrid,jstr,73);
                            }
                        } catch (SessionExpiredException jex) {

                        }
                    }

                    if(returnStr.compareTo("{data:{}}") != 0){
                        JSONObject jobj = new JSONObject(returnStr.toString());
                        if(caltype == 4)
                            jobj.getJSONArray("data").getJSONObject(0).put("permissionlevel", 2);
                        else if(caltype == 3)
                            jobj.getJSONArray("data").getJSONObject(0).put("permissionlevel", 3);
                        else
                            jobj.getJSONArray("data").getJSONObject(0).put("permissionlevel", "");
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("action", calAction);
                        data.put("data", jobj.toString());
                        data.put("success","true");
                        ServerEventManager.publish("/calTree/"+userid, data, this.getServletContext());

                        String params = AuthHandler.getAuthor(conn, loginid) + " ("+ AuthHandler.getUserName(request) +"), " +
                                        Tree.getCalendarName(conn, jstr) + ", " + projdb.getProjectName(conn, userid);
                        AuditTrail.insertLog(conn, "124", loginid, jstr, userid, companyid, params, ipAddress, auditMode);
                    }
                    jbj.put("data","{\"success\":\"true\"}");
                   // out.print("{\"success\":\"true\"}");
                }
                else{
                    jbj.put("data","{\"success\":\"false\"}");
                    //out.print("{\"success\":\"false\"}");
                }
                conn.commit();
            } catch (ServiceException ex) {
                DbPool.quietRollback(conn);
                System.out.println("calEvent:Connection Error:"+ex.toString());
            } catch(Exception e) {
                DbPool.quietRollback(conn);
                System.out.println("calendar insert error:"+e.toString());
            } finally {
                DbPool.quietClose(conn);
            }

        } else if (calAction.equals("2")) {
            com.krawler.database.DbPool.Connection conn = null;
            try{
                conn = DbPool.getConnection();
                String cid = request.getParameter("cid");
                String cname = request.getParameter("cname");
                String description = request.getParameter("description");
                String location = request.getParameter("location");
                String timezone = request.getParameter("timezone");
                String colorcode = request.getParameter("colorcode");
                int isdefault=Integer.parseInt(request.getParameter("isdefault"));
                String userid = request.getParameter("userid");
                String permission = request.getParameter("permission");
                String jstr = "";
                String returnStr = "";

                if(caltype != 3) {
                    jstr = Tree.updateCalendar(conn, cid, cname, description, location, timezone, colorcode, caltype, isdefault, userid);
                    if(caltype == 4){
                        int interval = Integer.parseInt(request.getParameter("interval"));
                        Tree.updateInternetCalendar(cid, interval, false);
                    }
                    if(!jstr.equals("0")){
                        conn.commit();
                        boolean exportCal = true;
                        String exportCalCheck = request.getParameter("exportCal");
                        if(exportCalCheck.compareToIgnoreCase("false") == 0) {
                            exportCal = false;
                        }
						String loginId = request.getParameter("loginid");
                        String retExport = Tree.updateExportCheck(conn, cid, loginId, exportCal);
                        conn.commit();

                        returnStr = Tree.getCalendar(conn, jstr);
                        if(caltype==0){
                            String sharedUsers= Tree.getCalendarSharedUserIds(conn, cid);
                            if(sharedUsers.compareTo("{data:{}}") != 0){
                                JSONObject sharedUserIds=new JSONObject(sharedUsers);
                                for(int ind=0;ind<(sharedUserIds.getJSONArray("data").length());ind++){
                                    String usrid=sharedUserIds.getJSONArray("data").getJSONObject(ind).getString("userid");
                                    Map<String, String> data = new HashMap<String, String>();
                                    data.put("action", "3");
                                    data.put("cid", cid);
                                    data.put("success","true");
                                    ServerEventManager.publish("/calTree/"+usrid , data, this.getServletContext());
                                }
                            }
                            int del = Tree.deleteCalendarPermission(conn, cid);
                            if(permission!=""){
                                String userPermission [];
                                userPermission = permission.split(",");
                                for(int i=0;i<userPermission.length;i++){
                                    String permissions [];
                                    permissions = userPermission[i].split("_");
                                    int per = Integer.parseInt(permissions[1]);
                                    String res=Tree.insertCalPermission(conn, cid,permissions[0],per);

                                    if(!res.equals("0")){
                                        if(returnStr.compareTo("{data:{}}") != 0){
                                            JSONObject jobj = new JSONObject(returnStr.toString());
                                            jobj.getJSONArray("data").getJSONObject(0).put("permissionlevel", per);
                                            Map<String, String> data = new HashMap<String, String>();
                                            data.put("action", "1");
                                            data.put("data", jobj.toString());
                                            data.put("success","true");
                                            ServerEventManager.publish("/calTree/"+permissions[0], data, this.getServletContext());
                                        }
                                    }
                                }
                                //if(userPermission.length != 0)
                                    //LogHandler.InsertLogForCalendar(conn, userid,userid,jstr,52);
                            }
                        }
                        else{
                           try {
                                if (sessionbean.validateSession(request, response)) {
                                    String usrid=AuthHandler.getUserid(request);
                                    //LogHandler.InsertLogForProjectCalendar(conn, userid,usrid,usrid,jstr,74);
                                }
                            } catch (SessionExpiredException jex) {

                            }
                        }
                        jbj.put("data","{\"success\":\"true\"}");
                    }
                    else {
                        jbj.put("data","{\"success\":\"false\"}");
                    }
                }
                else if(caltype == 3){
                    String res=Tree.updateCalPermission(conn, cid, userid, 3, colorcode);

                    if(!res.equals("0")) {
                        returnStr = Tree.getCalendar(conn, cid);
                        jbj.put("data","{\"success\":\"true\"}");
                    }
                    else {
                        jbj.put("data","{\"success\":\"false\"}");
                    }
                }
                if(returnStr.compareTo("{data:{}}") != 0 && returnStr.compareTo("") != 0){
                    JSONObject jobj = new JSONObject(returnStr.toString());
                    if(caltype == 4)
                        jobj.getJSONArray("data").getJSONObject(0).put("permissionlevel", 2);
                    else if(caltype == 3)
                        jobj.getJSONArray("data").getJSONObject(0).put("permissionlevel", 3);
                    else
                        jobj.getJSONArray("data").getJSONObject(0).put("permissionlevel", "");
                    jobj.getJSONArray("data").getJSONObject(0).put("colorcode", colorcode);
                    jobj.getJSONArray("data").getJSONObject(0).put("userid", userid);
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("action", calAction);
                    data.put("data", jobj.toString());
                    data.put("success","true");
                    ServerEventManager.publish("/calTree/"+userid, data, this.getServletContext());

                    String params = AuthHandler.getAuthor(conn, loginid) + " ("+ AuthHandler.getUserName(request) +"), " +
                                    cname+ ", " + projdb.getProjectName(conn, userid);
                    AuditTrail.insertLog(conn, "125", loginid, jstr, userid, companyid, params, ipAddress, auditMode);
                }
                conn.commit();
            } catch (ServiceException ex) {
                DbPool.quietRollback(conn);
                System.out.println("calEvent:Connection Error:"+ex.toString());
            } catch(Exception e) {
                DbPool.quietRollback(conn);
                System.out.println("calendar update error:"+e.toString());
            } finally {
                DbPool.quietClose(conn);
            }
        }
        else if (calAction.equals("3")) {
            com.krawler.database.DbPool.Connection conn = null;
            try{
                conn = DbPool.getConnection();
                String cid = request.getParameter("cid");
                int isdefault=Integer.parseInt(request.getParameter("isdefault"));
                String userid = request.getParameter("userid");
                String calName = Tree.getCalendarName(conn, cid);
                int jstr = 0;
                String sharedUsers = "{data:{}}";

                if(caltype != 3) {
                    sharedUsers= Tree.getCalendarSharedUserIds(conn, cid);
                    jstr = Tree.deleteCalendar(conn, cid, isdefault);
                }
                else
                    jstr = Tree.deleteCalendarUserPermission(conn, userid, cid);
                if(jstr!=0 || isdefault==1){
                    if(sharedUsers.compareTo("{data:{}}") != 0){
                        JSONObject sharedUserIds=new JSONObject(sharedUsers);
                        for(int ind=0;ind<(sharedUserIds.getJSONArray("data").length());ind++){
                            String usrid=sharedUserIds.getJSONArray("data").getJSONObject(ind).getString("userid");
                            Map<String, String> data = new HashMap<String, String>();
                            data.put("action", "3");
                            data.put("cid", cid);
                            data.put("success","true");
                            ServerEventManager.publish("/calTree/"+usrid , data, this.getServletContext());
                        }
                    }

                    Map<String, String> data = new HashMap<String, String>();
                    data.put("action", "3");
                    data.put("cid", cid);
                    data.put("success","true");
                    ServerEventManager.publish("/calTree/"+userid , data, this.getServletContext());
                    jbj.put("data","{\"success\":\"true\"}");

                    if(isdefault==1){
                        String params = AuthHandler.getAuthor(conn, AuthHandler.getUserid(request)) + " ("+ AuthHandler.getUserName(request) +"), " +
                                    "[All the events], "+calName + ", " + projdb.getProjectName(conn, userid);
                        AuditTrail.insertLog(conn, "123", loginid, cid, userid, companyid, params, ipAddress, auditMode);
                    }else{
                        String params = AuthHandler.getAuthor(conn, AuthHandler.getUserid(request)) + " ("+ AuthHandler.getUserName(request) +"), " +
                                    calName + ", " + projdb.getProjectName(conn, userid);
                        AuditTrail.insertLog(conn, "126", loginid, cid, userid, companyid, params, ipAddress, auditMode);
                    }
                    conn.commit();
                }
                else
                   jbj.put("data","{\"success\":\"false\"}");
                   // out.print("{\"success\":\"false\"}");
                //out.print("{\"data\":[{\"cid\":\""+cid+"\"}],\"success\":\"true\"}");
            }
            catch (ServiceException ex) {
                DbPool.quietRollback(conn);
                System.out.println("calEvent:Connection Error:" + ex.toString());
            } catch(Exception e) {
                DbPool.quietRollback(conn);
                System.out.println("calendar update error:" + e.toString());
            } finally {
                DbPool.quietClose(conn);
            }
        }else if(calAction.equals("4")){
            String cid = request.getParameter("cid");
            String jstr = dbcon.getCalendarPermission(cid);
            jbj.put("data",jstr);
            //out.print(jstr);
        }
        else if(calAction.equals("5")){
            com.krawler.database.DbPool.Connection conn = null;
            try{
                conn = DbPool.getConnection();
                String retExport = Tree.getUserExportData(conn, loginid, companyid);
                jbj.put("data", retExport);
            } catch (ServiceException ex) {
                DbPool.quietRollback(conn);
                System.out.println("calEvent:Connection Error:"+ex.toString());
            } catch(Exception e) {
                DbPool.quietRollback(conn);
                System.out.println("calendar update error:"+e.toString());
            } finally {
                DbPool.quietClose(conn);
            }
        }
        else if(calAction.equals("6")){
            com.krawler.database.DbPool.Connection conn = null;
            try{
                conn = DbPool.getConnection();
                String retExport = Tree.getCalendarType(conn, request.getParameter("userid"), request.getParameter("caltype"));
                if(retExport.compareTo("{data:{}}") != 0) {
                    JSONObject temp = new JSONObject(retExport);
                    jbj = temp;
                }
            } catch (ServiceException ex) {
                DbPool.quietRollback(conn);
                System.out.println("calEvent:Connection Error:"+ex.toString());
            } catch(Exception e) {
                DbPool.quietRollback(conn);
                System.out.println("calendar update error:"+e.toString());
            } finally {
                DbPool.quietClose(conn);
            }
        }
        else if(calAction.equals("7")){
            com.krawler.database.DbPool.Connection conn = null;
            String subAction = request.getParameter("subaction");
            String jstr = "";
            try{
                conn = DbPool.getConnection();
                jstr = Tree.sendNotification(conn, request);
                jbj.put("data",jstr);
                conn.commit();
            } catch (ServiceException ex) {
                DbPool.quietRollback(conn);
                System.out.println("calEvent:Connection Error:"+ex.toString());
            } catch(Exception e) {
                DbPool.quietRollback(conn);
                System.out.println("calendar update error:"+e.toString());
            } finally {
                DbPool.quietClose(conn);
            }
        }
        else if(calAction.equals("8")){
            com.krawler.database.DbPool.Connection conn = null;
            boolean selection = Boolean.parseBoolean(request.getParameter("selection"));
            String cid = request.getParameter("cid");
            try{
                conn = DbPool.getConnection();
                Tree.updateCalendarSelection(conn, cid, loginid, selection);
                conn.commit();
            } catch (ServiceException ex) {
                DbPool.quietRollback(conn);
                System.out.println("calEvent:Connection Error:"+ex.toString());
            } catch(Exception e) {
                DbPool.quietRollback(conn);
                System.out.println("calendar selection update error:"+e.toString());
            } finally {
                DbPool.quietClose(conn);
            }
        }
        out.println(jbj.toString());
} else {
	out.println("{\"valid\": false}");
}
%>
