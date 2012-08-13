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
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="com.krawler.esp.database.dbcon" %>
<%@ page import="com.krawler.esp.handlers.*" %>
<%@ page import="com.krawler.database.*" %>
<%@page import="com.krawler.common.service.ServiceException"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
                 <link rel="shortcut icon" href="images/deskera.png" />
                 <script>

                        var pplan = null;
                        var id = '<%=request.getParameter("id")%>'
                        var columns = '<%=request.getParameter("col")%>'
                        var df = '<%=request.getParameter("df")%>'
						var rid = '<%=request.getParameter("rid")%>'
                        <%
                             String name=dbcon.getProjectName(request.getParameter("id"));
                        %>
                        var projname = "<%=name%>"
                        if(projname.length>0) {
                            document.write('<script type="text/javascript" src="lib/adapter/wtf/wtf-base.js?v=3"><\/script>');
                            document.write('<script type="text/javascript" src="scripts/embedplan/WtfEmbedlib.js"><\/script>');
                            document.write('<link rel="stylesheet" type="text/css" href="lib/resources/css/wtf-all.css?v=3"/>');
                            document.write('<link rel="stylesheet" type="text/css" href="style/embedplan.css?v=3"/>');
                            if(rid == 'null'){
                                document.write('<script type="text/javascript" src="scripts/common/WtfGraphics.js"><\/script>');
                                document.write('<script type="text/javascript" src="scripts/common/WtfScrollPanel.js" ><\/script>');
                                document.write('<script type="text/javascript" src="scripts/embedplan/WtfEmbedPlan.js"><\/script>');
                            } else {
                                document.write('<script type="text/javascript" src="../../scripts/bar chart/amchart.js"><\/script>');
                                document.write('<script type="text/javascript" src="scripts/proj/Components/WtfProjReportColumn.js"><\/script>');
                                var pc = '<%=request.getParameter("pc")%>', rptname = '<%=request.getParameter("rnm")%>', bid = '<%=request.getParameter("bid")%>'
                                u = '<%=request.getParameter("u")%>', d1 = '<%=request.getParameter("d1")%>', d2 = '<%=request.getParameter("d2")%>'
                                document.write('<script type="text/javascript" src="scripts/embedplan/WtfEmbedReport.js"><\/script>');
                            }
                         } else {
                            document.write('<div style="border-top:3px solid #D8DFEA;margin-top:10px;width:50%;font-family:arial,sans-serif;padding-top:15px;padding-left:50px'
                                +'"><div style="background-color:#267AC3;color:#FFFFFF;font-family:Arial,Helvetica,sans-serif;margin:0px 0px 8px;padding:2px 0px 2px 5px;font-weight:bold">Parameter Error</div>Invalid Project ID</div>');
                         }
                         function Calllayout(){
                            if(rid != 'null'){
                                Wtf.getCmp(id +'_'+ rid +'_reportPanel').doLayout();
                            } else {
                                Wtf.getCmp('<%=request.getParameter("id")%>_projplanPane').doLayout();
                            }
                         }

                </script>
       </head>
       <body onload="Calllayout()">
                <div id="header" style="text-align: center;">
			<!--div style="float:left;height:25px; width:130px;">
                            <img id="companyLogo" src="images/store/?company=true" alt="" style="height: auto; width: auto; font-size:21px;"/>
			</div>
                        <div id ="projectname" style="height:20px; float:left; text-align: center;"></div>
			<div style="float:left;height:25px; width:130px;">&nbsp;</div-->
			<div id ="projectname" style="height:25px; padding:5px 5px 5px 0px; display:block;width:100%; background: url('<%=com.krawler.esp.utils.ConfigReader.getinstance().get("platformURL")%>b/<%=com.krawler.common.util.URLUtil.getDomainName2(request)%>/images/store/?company=true') no-repeat;"></div>
		</div>


        </body>
</html>
