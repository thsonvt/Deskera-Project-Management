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
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page pageEncoding="UTF-8"%>
<%@ page import="com.krawler.esp.database.dbcon" %>
<%@ page import="com.krawler.database.DbPool" %>
<%@ page import="com.krawler.database.DbUtil" %>
<%@ page import="com.krawler.database.DbResults" %>
<%@page import="com.krawler.common.session.SessionExpiredException"%>
<%@page import="com.krawler.common.util.*"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />

<%
com.krawler.utils.json.base.JSONObject jbj = new com.krawler.utils.json.base.JSONObject();
if (sessionbean.validateSession(request, response)) {
         com.krawler.database.DbPool.Connection conn = com.krawler.database.DbPool.getConnection();
         String selectPost = "Select post_text,post_id from krawlerforum_posts";  
         String updatePostQuery = "update krawlerforum_posts set post_text = ? where post_id = ?";
         String selecttopic = "Select post_text,topic_title,topic_id from krawlerforum_topics";
         String updatequery = "update krawlerforum_topics set post_text = ? where topic_id = ?";
         String selectMailMsg = "Select post_id,post_subject,post_text from mailmessages ";
         String updateMailMsg = "update mailmessages set post_text = ? where post_id = ?";
         int no = 0;
         String posttext = "";
         String posttitle = "";
         try{
         
         //Forum topic Update
         out.println("Problem occured in following Forum topic:<br>");
         DbResults rs = DbUtil.executeQuery(conn,selecttopic);
         while(rs.next()){
             try{
                 posttext = java.net.URLDecoder.decode(rs.getString("post_text"));
             }catch(java.lang.IllegalArgumentException ex) {
                 out.println(rs.getString("topic_id")+"<br>");
                 posttext = rs.getString("post_text");
             }
             
             no += DbUtil.executeUpdate(conn,updatequery,new Object[]{posttext,rs.getString("topic_id")});
             
         }
         
         //Forum post Update
         out.println("<br>Problem occured in following Forum Post:<br>");
         DbResults rs2 = DbUtil.executeQuery(conn,selectPost);
         posttext = "";
         posttitle = "";
         while(rs2.next()){
             try{
                 posttext = java.net.URLDecoder.decode(rs2.getString("post_text"));
             }catch(java.lang.IllegalArgumentException ex) {
                 out.println(rs2.getString("post_id")+"<br>");
                 posttext = rs2.getString("post_text");
             }
             
             no += DbUtil.executeUpdate(conn,updatePostQuery,new Object[]{posttext,rs2.getString("post_id")});
             
         }
         
         //Forum mail messages
         out.println("<br>Problem occured in following Mail messages:<br>");
         DbResults rs1 = DbUtil.executeQuery(conn,selectMailMsg);
         posttext = "";
         posttitle = "";
         while(rs1.next()){
             try{
             posttext = java.net.URLDecoder.decode(rs1.getString("post_text"));
             }catch(java.lang.IllegalArgumentException ex){
                 out.println(rs1.getString("post_id")+"<br>");
                 posttext = rs1.getString("post_text");
             }
            
             no += DbUtil.executeUpdate(conn,updateMailMsg,new Object[]{posttext,rs1.getString("post_id")});
             //no += DbUtil.executeUpdate(conn,insertdel,new Object[]{"del"+rs.getString("userid")});
         }
         conn.commit();
         }catch(Exception ex){
             System.out.print(ex);
             conn.rollback();
         }finally{
             conn.close();
         }
         
         out.println("{\"valid\": true}"+no);
} else {
	out.println("{\"valid\": false}");
}      
%> 
