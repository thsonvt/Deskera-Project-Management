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
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import ="java.util.*,
        java.sql.*,
        javax.servlet.ServletRequest,
        com.krawler.utils.json.base.*,
        com.krawler.esp.database.*" %>
<%@page import="com.krawler.esp.handlers.projectReport"%>
<%@ page import="com.krawler.common.session.SessionExpiredException"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%

            String returnStr = null;
            String Str="";
            try{
                String type=request.getParameter("type");
                boolean isValidSession = sessionbean.isValidSession(request, response);
                if(request.getParameter("action").equals("startdatetasks") || request.getParameter("action").equals("enddatetasks"))
                    returnStr = projectReport.getProjectReport2Json(request, true, isValidSession);
                else
                    returnStr = projectReport.getProjectReportJson(request, true, isValidSession);
                if(type.equals("taskcompletion")){
                JSONObject jobj = new JSONObject(returnStr);
                JSONArray jarr = jobj.getJSONArray("data");
                    if( jobj.getJSONArray("data").length()!=0){
                        for(int i=0;i < jobj.getJSONArray("data").length() ; i++){
                                JSONObject temp = jarr.getJSONObject(i);
                                String tName = temp.getString("taskname");
                                if(tName.length() > 10){
                                    tName = tName.substring(0, 10) + "..";
                                }
                                int len=temp.getString("percentcomplete").length();
                                String per=temp.getString("percentcomplete").substring(0, len-1);
                                Str += tName + ";" +per+ "\n";
                         }
                    }
                }
                if(type.equals("overdue")){
                    JSONObject jobj = new JSONObject(returnStr);
                    JSONArray jarr = jobj.getJSONArray("data");
                    if( jobj.getJSONArray("data").length()!=0){
                        for(int i=0;i < jobj.getJSONArray("data").length() ; i++){
                                JSONObject temp = jarr.getJSONObject(i);
                                String tName = temp.getString("taskname");
                                if(tName.length() > 10){
                                    tName = tName.substring(0, 10) + "..";
                                }
                                int len=temp.getString("delay").length();
                                String delay=temp.getString("delay").substring(0, len);
                                Str += tName + ";" +delay+ "\n";
                         }
                    }
                }
                if(type.equals("overdueexpence")){
                    JSONObject jobj = new JSONObject(returnStr);
                    JSONArray jarr = jobj.getJSONArray("data");
                    if( jobj.getJSONArray("data").length()!=0){
                        for(int i=0;i < jobj.getJSONArray("data").length() ; i++){
                                JSONObject temp = jarr.getJSONObject(i);
                                String tName = temp.getString("taskname");
                                if(tName.length() > 10){
                                    tName = tName.substring(0, 10) + "..";
                                }
                                int len=temp.getString("expence").length();
                                String delay=temp.getString("expence");
                                Str += tName + ";" +delay+ "\n";
                         }
                    }
                }
                if(type.equals("taskcost")){
                    JSONObject jobj = new JSONObject(returnStr);
                    JSONArray jarr = jobj.getJSONArray("data");
                    String cost = "";
                    double costvary = 0;
                    if( jobj.getJSONArray("data").length()!=0){
                        for(int i=0;i < jobj.getJSONArray("data").length() ; i++){
                                JSONObject temp = jarr.getJSONObject(i);
                                String tName = temp.getString("taskname");
                                String rname = temp.getString("resourcename");
                                if(tName.length() > 10){
                                    tName = tName.substring(0, 10) + "..";
                                }
                                if(request.getParameter("action").equals("resanalysis")){
                                    cost=temp.getString("curcost");
                                    cost = request.getParameter("curr").toString() + " " + cost;
                                } else if(request.getParameter("action").equals("toplevel")){
                                    cost=temp.getString("cost");
                                } else {
                                    cost=temp.getString("cost");
                                }
                                Str += tName + ";" +cost+ "\n";
                         }
                    }
                }
                if(type.equals("resourcewisecost")){
                    JSONObject jobj = new JSONObject(returnStr);
                    String subtype = request.getParameter("subtype");
                    JSONArray jarr = jobj.getJSONArray("data");
                    if(subtype.equals("costvary")){
                        String cost = "";
                        double costvary = 0;
                        if( jobj.getJSONArray("data").length()!=0){
                            for(int i=0;i < jobj.getJSONArray("data").length() ; i++){
                                    JSONObject temp = jarr.getJSONObject(i);
                                    String rName = temp.getString("resourcename");
                                    String rnametemp = rName;
                                    if(rName.length() > 10){
                                        rName = rName.substring(0, 10) + "..";
                                    }
                                    costvary = 0;
                                    if(Str.indexOf(rName) == -1){
                                         for(int j = 0; j < jobj.getJSONArray("data").length(); j++){
                                            JSONObject temp1 = jarr.getJSONObject(j);
                                            if(rnametemp.equals(temp1.getString("resourcename"))){
                                                 if(!temp1.get("costvary").equals("NA"))
                                                      costvary +=  temp1.getDouble("costvary");
                                            }
                                         }
                                         cost = "" + costvary;
                                         Str += rName + ";" +cost+ "\n";
                                    }
                             }
                        }
                    } else {
                        String work = "";
                        double workvary = 0;
                        if( jobj.getJSONArray("data").length()!=0){
                            for(int i=0;i < jobj.getJSONArray("data").length() ; i++){
                                    JSONObject temp = jarr.getJSONObject(i);
                                    String rName = temp.getString("resourcename");
                                    String rnametemp = rName;
                                    if(rName.length() > 10){
                                        rName = rName.substring(0, 10) + "..";
                                    }
                                    workvary = 0;
                                    if(Str.indexOf(rName) == -1){
                                         for(int j = 0; j < jobj.getJSONArray("data").length(); j++){
                                            JSONObject temp1 = jarr.getJSONObject(j);
                                            if(rnametemp.equals(temp1.getString("resourcename"))){
                                                 if(!temp1.get("workvary").equals("NA"))
                                                      workvary +=  temp1.getDouble("workvary");
                                            }
                                         }
                                         work = "" + workvary;
                                         Str += rName + ";" +work+ "\n";
                                    }
                             }
                        }
                    }
                }
                if(type.equals("costcompare")){
                    JSONObject jobj = new JSONObject(returnStr);
                    JSONArray jarr = jobj.getJSONArray("data");
                    if( jobj.getJSONArray("data").length()!=0){
                        for(int i=0;i < jobj.getJSONArray("data").length() ; i++){
                                JSONObject temp = jarr.getJSONObject(i);
                                if(!temp.getString("costvary").equals("NA")){
                                    String tName = temp.getString("taskname");
                                    if(tName.length() > 10){
                                        tName = tName.substring(0, 10) + "..";
                                    }
                                    int len=temp.getString("costvary").length();
                                    String cost=temp.getString("costvary").substring(0, len);
                                    Str += tName + ";" +cost+ "\n";
                                }
                         }
                    }
                }
                if(type.equals("startdatecompare")){
                    JSONObject jobj = new JSONObject(returnStr);
                    JSONArray jarr = jobj.getJSONArray("data");
                    if( jobj.getJSONArray("data").length()!=0){
                        for(int i=0;i < jobj.getJSONArray("data").length() ; i++){
                                JSONObject temp = jarr.getJSONObject(i);
                                if(!temp.getString("startdiff").equals("NA")){
                                    String tName = temp.getString("taskname");
                                    if(tName.length() > 10){
                                        tName = tName.substring(0, 10) + "..";
                                    }
                                    int len=temp.getString("startdiff").length();
                                    String cost=temp.getString("startdiff").substring(0, len);
                                    Str += tName + ";" +cost+ "\n";
                                }
                         }
                    }
                }
                if(type.equals("enddatecompare")){
                    JSONObject jobj = new JSONObject(returnStr);
                    JSONArray jarr = jobj.getJSONArray("data");
                    if( jobj.getJSONArray("data").length()!=0){
                        for(int i=0;i < jobj.getJSONArray("data").length() ; i++){
                                JSONObject temp = jarr.getJSONObject(i);
                                if(!temp.getString("enddiff").equals("NA")){
                                    String tName = temp.getString("taskname");
                                    if(tName.length() > 10){
                                        tName = tName.substring(0, 10) + "..";
                                    }
                                    int len=temp.getString("enddiff").length();
                                    String cost=temp.getString("enddiff").substring(0, len);
                                    Str += tName + ";" +cost+ "\n";
                                }
                         }
                    }
                }
                if(type.equals("durationcompare")){
                    JSONObject jobj = new JSONObject(returnStr);
                    JSONArray jarr = jobj.getJSONArray("data");
                    if( jobj.getJSONArray("data").length()!=0){
                        for(int i=0;i < jobj.getJSONArray("data").length() ; i++){
                                JSONObject temp = jarr.getJSONObject(i);
                                if(!temp.getString("durvary").equals("NA")){
                                    String tName = temp.getString("taskname");
                                    if(tName.length() > 10){
                                        tName = tName.substring(0, 10) + "..";
                                    }
                                    int len=temp.getString("durvary").length();
                                    String cost=temp.getString("durvary").substring(0, len);
                                    Str += tName + ";" +cost+ "\n";
                                }
                         }
                    }
                }
                if(type.equals("workcompare")){
                    JSONObject jobj = new JSONObject(returnStr);
                    JSONArray jarr = jobj.getJSONArray("data");
                    if( jobj.getJSONArray("data").length()!=0){
                        for(int i=0;i < jobj.getJSONArray("data").length() ; i++){
                                JSONObject temp = jarr.getJSONObject(i);
                                if(!temp.getString("hoursvary").equals("NA")){
                                    String tName = temp.getString("taskname");
                                    if(tName.length() > 10){
                                        tName = tName.substring(0, 10) + "..";
                                    }
                                    int len=temp.getString("hoursvary").length();
                                    String cost=temp.getString("hoursvary").substring(0, len);
                                    Str += tName + ";" +cost+ "\n";
                                }
                         }
                    }
                }
                if(type.equals("duration")){
                    JSONObject jobj = new JSONObject(returnStr);
                    JSONArray jarr = jobj.getJSONArray("data");
                    if( jobj.getJSONArray("data").length()!=0){
                        for(int i=0;i < jobj.getJSONArray("data").length() ; i++){
                                JSONObject temp = jarr.getJSONObject(i);
                                String tName = temp.getString("taskname");
                                if(tName.length() > 10){
                                    tName = tName.substring(0, 10) + "..";
                                }
                                String duration=temp.getString("duration");
                                if(request.getParameter("action").equals("resanalysis"))
                                    duration=temp.getString("curduration");
                                else if(request.getParameter("action").equals("resourcewisecompare")){
                                    duration=temp.getString("workvary");
                                    if(duration.equals("NA"))
                                        duration = "0";
                                }
                                int len=duration.length();

                                if(duration.indexOf("d") != -1) {
                                    duration=duration.substring(0,duration.indexOf("d"));
                                }
                                else  if(duration.indexOf("h") != -1) {
                                    duration=duration.substring(0,duration.indexOf("h"));
                                    float dur=(Float.parseFloat(duration))/8;
                                    duration=Float.toString(dur);
                                }
                                Str += tName + ";" +duration+ "\n";
                         }
                    }
                }
                if(type.equals("projectcharts")){
                    Str = returnStr;
                }
                out.println(Str);
            }catch(Exception e){
            }
%>
