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

<%@page contentType="text/html;charset=UTF-8"%>
<%@ page import="com.krawler.svnwebclient.web.model.data.file.*,
                 java.util.List,
                 java.util.Iterator,
                 com.krawler.common.util.KrawlerLog"%>
 <%
    String beanName = "bean";
 %>
 <jsp:useBean id="bean" scope="request" class="com.krawler.svnwebclient.web.controller.file.FileCompareDataBean"/>                 
              
<%
    try{
       // int maxLineNo = 500;
    if (bean.execute(request, response)) {
        FileCompareResult result = bean.getResult();
        String flag = request.getParameter("flag");    
        //maxLineNo = Integer.parseInt(request.getParameter("page"))*maxLineNo;
%>
<html>
        <body>
        <table cellspacing="0" cellpadding="0" width="100%" height="0%" valign="top">
<%
        if (result == null) {
%>
            <tr valign="middle">
                <td align="center" height="100%" style="font-size:11px;">
<%
            if (bean.isBinary()) {
%>                                
                    <b>File content is binary</b>
<%
            } else {
%>                
                    <b>There are no data to show</b>
<%
            }                    
%>            
                </td>
            </tr>                    
<%
        } else {
            if(flag==null){
            boolean validline = true;
%>
<div id="totalNoOfLine" style="display:none"> <%=result.getLines().size()%> </div>  
<%
            for (Iterator i = result.getLines().iterator(); i.hasNext(); ) {
                FileCompareResult.Line line = (FileCompareResult.Line) i.next();
                /*try{
                    if(Integer.parseInt(line.getNumber())>=maxLineNo+500){
                        validline = false;
                    }
                }catch(Exception ex){
                    validline = true;
                }
                if(validline){*/
%>
            <tr valign="middle">
                <td style="height: 15px;!important;font-size:11px;background: #E9F3FA;padding-left: 2px;padding-right: 2px;border-bottom: 1px solid #C0DAEA;border-right: 1px solid #C0DAEA;">
                    <img style="margin:0;padding:0;border:0;" src="<%=line.getImage()%>" border="0" width="16px"/>
                </td>            
                <td style="height: 15px;!important;font-size:11px;background: #E9F3FA;padding-left: 5px;padding-right: 5px;padding-top: 2px;padding-bottom: 2px;border-bottom: 1px solid #C0DAEA;border-right: 1px solid #C0DAEA;">
                        <%=line.getNumber()%>
                </td>
                <td nowrap="true" align="left" width="100%" style="height:15px;!important;font-size:11px;background:<%=line.getBackground()%>;padding-left: 5px;padding-right: 5px;padding-top: 2px;padding-bottom: 2px;border-bottom: 1px solid white;border-right: 1px solid white;">
                    <%=line.getLine()%>
                </td>                
            </tr>    
<%
          /*  }else{
                    break;
            }*/}}else{                    
            for (Iterator i = result.getLines().iterator(); i.hasNext(); ) {
            FileCompareResult.Line line = (FileCompareResult.Line) i.next();
%>
            <tr valign="middle">
                <td style="height:15px;!important;font-size:11px;background: #E9F3FA;padding-left: 5px;padding-right: 5px;padding-top: 2px;padding-bottom: 2px;border-bottom: 1px solid #C0DAEA;border-right: 1px solid #C0DAEA;">
                        <%=line.getNumber()%>
                </td>
                <td nowrap="true" align="left" width="100%" style="height:15px;!important;font-size:11px;background:<%=line.getBackground()%>;padding-left: 5px;padding-right: 5px;padding-top: 2px;padding-bottom: 2px;border-bottom: 1px solid white;border-right: 1px solid white;">
                    <%=line.getLine()%>&nbsp;
                </td>                
            </tr>    
<%
            
            }
            }
%>

                
<%                        
        }
%>        
        </table>
    </body>
</html>
<%
    }
 }catch(Exception e){
   KrawlerLog.op.warn("Unable to Compare file Content :" +e.toString());
 }
%>    
