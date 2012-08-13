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
<%@ page contentType="text/html"%>
<%@ page pageEncoding="UTF-8"%>
<%@ page import="com.krawler.esp.portalmsg.*" %> 
<%@ page import="com.krawler.esp.handlers.AuthHandler" %> 
<%@page import="com.krawler.common.session.SessionExpiredException"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />

<%
com.krawler.utils.json.base.JSONObject jbj = new com.krawler.utils.json.base.JSONObject();
if (sessionbean.validateSession(request, response)) {
    jbj.put("valid", true);         
    String mailflag = request.getParameter("mailflag");        
    if(mailflag.compareTo("fetch")==0)
    {
        String i = request.getParameter("flag");
        String loginid = request.getParameter("loginid");               
        int offset = Integer.parseInt(request.getParameter("start"));
        int limit= Integer.parseInt(request.getParameter("limit"));
        int tCount=forummsgdbcon.fetchMailCount(loginid,i,offset,limit);
        String tp=forummsgdbcon.fetchMail(loginid,i,offset,limit);
        
        boolean scriptTag = false;
        String cb = request.getParameter("callback");
        if (cb != null) 
        {
            scriptTag = true;
            response.setContentType("text/javascript");
        } 
        else 
        {
            response.setContentType("application/x-json");
        }

        if (scriptTag) 
        {
              jbj.put("data",cb + "("+tp+");");
           // out.print(cb + "("+tp);
           // out.print(");");
        }
        else
        {
            int l=tp.length();
            tp=tp.substring(1,(l-1));
            jbj.put("data","{\"totalCount\":"+tCount+","+tp+"}");
           //>{"totalCount":<out.print(tCount);>,<out.print(tp);>}<
        }
    }        
    else if(mailflag.compareTo("movemails")==0)
    {
        try
        {
            String post_id = request.getParameter("post_id");
            String last_folder_id = request.getParameter("last_folder_id");
            String dest_folder_id = request.getParameter("dest_folder_id");
            String loginid = AuthHandler.getUserid(request);
            String status = forummsgdbcon.MoveMails(post_id,last_folder_id,dest_folder_id,loginid);
            jbj.put("data",status);
            //out.print(status);    
        }
        catch(Exception e)
        {                    
            jbj.put("data","Failure");
            //out.print("Failure");
        }            
    }
    else if(mailflag.compareTo("starchange")==0)
    {    
        try 
        {
            String post_id = request.getParameter("post_id");
            boolean flag = Boolean.parseBoolean(request.getParameter("flag")); 
            
            String status = forummsgdbcon.StarChangeForMail(post_id,flag);
            jbj.put("data",status);
            //out.print(status);                
        }
        catch(Exception e)
        {
            jbj.put("data","Failure");
            //out.print("Failure");
        }            
    }
    else if(mailflag.compareTo("searchmails")==0)
    {
        String searchtext = java.net.URLDecoder.decode(request.getParameter("searchtext").trim());
        String searchtext1= searchtext;
        if(searchtext.length()>= 1){
            searchtext = searchtext+"%";
            searchtext1 = "% "+searchtext;
        }
        String folder_id = request.getParameter("folder_id");
        String loginid = request.getParameter("loginid");
        int offset = Integer.parseInt(request.getParameter("start"));
        int limit= Integer.parseInt(request.getParameter("limit"));
        int tCount=forummsgdbcon.searchMailCount(searchtext1,searchtext,folder_id,loginid,offset,limit);
        String tp=forummsgdbcon.searchMail(searchtext1,searchtext,folder_id,loginid,offset,limit);
        
        boolean scriptTag = false;
        String cb = request.getParameter("callback");
        if (cb != null) 
        {
            scriptTag = true;
            response.setContentType("text/javascript");
        } 
        else
        {
            response.setContentType("application/x-json");
        }
        if (scriptTag) 
        {
            jbj.put("data",cb + "("+tp+");");
            //out.print(cb + "("+tp);
            //out.print(");");
        }
        else
        {
            int l=tp.length();
            tp=tp.substring(1,(l-1));
            jbj.put("data","{\"totalCount\":"+tCount+","+tp+"}");
            //>{"totalCount":<out.print(tCount);>,<out.print(tp);>}<
        }
            
            
    }
    else if(mailflag.compareTo("deleteforever")==0)
    {       
        String post_id = request.getParameter("post_id");            
        try 
        {                
            post_id = forummsgdbcon.deleteforeverMailMsg(post_id);                
        }
        catch(Exception e)
        {                
            post_id = "-1";
        }
        jbj.put("data",post_id);
       // out.print(post_id);            
    }
    else if(mailflag.compareTo("restoremsg")==0)
    {
        String post_id = request.getParameter("post_id");            
        String last_folder_id = "-1"; 
        try 
        {   
            post_id = forummsgdbcon.restoreMailMsg(post_id,last_folder_id);                                
        }
        catch(Exception e)
        {                
            post_id = "-1";
        }
         jbj.put("data",post_id);
       // out.print(post_id);            
    }
	else if(mailflag.compareTo("updatereadflag")==0)
    {
            try{
                String post_id = request.getParameter("post_id");                    

                String status = forummsgdbcon.UpdateMailReadflag(post_id);
                jbj.put("data",status);
                //out.print(status);    
            }
            catch(Exception e){   
                jbj.put("data","Failure");
                // out.print("Failure");
            }            
    }   
     out.println(jbj.toString());
} else {
	out.println("{\"valid\": false}");
}        
%>

