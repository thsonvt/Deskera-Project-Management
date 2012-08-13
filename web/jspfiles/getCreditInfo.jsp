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
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.krawler.esp.database.dbcon"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="com.krawler.database.*"%>
<%@ page import="java.util.Calendar"%>
<%
    com.krawler.database.DbPool.Connection conn = null;
            conn = DbPool.getConnection();
    String rate = "12";
    int projCnt = 5;
    String companyid = com.krawler.esp.handlers.AuthHandler.getCompanyid(request);
    DbResults rs = DbUtil.executeQuery(conn, "select payplan.projrate from payplan inner join company on company.planid = payplan.planid where companyid = ?",new Object[]{companyid});
    while(rs.next()){
       rate = rs.getObject("projrate").toString();
    }
    String newform = "<div style='width: 70%; text-align: left; display: block; clear: both; position: relative; float: left; padding-top: 5px;'>"
                                       + "<label for='Field' style = ' font-weight:bold;vertical-align:middle;padding-bottom:10px;'>Projects : </label>"
                                       + "<input type='text' name='projcnt' id='subscrproj' style = ' width:100px;' onchange='subscriprojval(this)' value="+projCnt+">"
                                       + "<span class='subscrilabel'> X "+rate+" = </span>"
                                       + "<input type='text' name='amount' id='subscramt' style = ' width:100px;' style='color:#666;text-align:right' value="+com.krawler.esp.handlers.projectReport.numberFormatter((Double.parseDouble(rate) * projCnt),"$")+" disabled/>"
                                       + "<input type=\"hidden\" id =\"deskerarate\" value="+rate+">";

     String CCForm = "<form action='jspfiles/authorizeddotnet.jsp' method=\"post\">"
                        +"<div>"
                        +"<label for='start_date' style = ' font-weight:bold;vertical-align:middle;padding-bottom:10px;'>Start Date : </label>"
                        +"<input type='text' id='start_date' maxlength='32' style='width: 100px;' name='start_date' value=''>"
                        +"</div>"
                        +"<div>"
                        +"<label for='occurance' style = 'font-weight:bold;vertical-align:middle;padding-bottom:10px;'>Occurance : </label>"
                        +"<input type='text' id='occurance' maxlength='32' style='width: 100px;' name='occurance' value=''>"
                        +"</div>"
                        +"<div>"
                        +"<label for='first_name' style = 'font-weight:bold;vertical-align:middle;padding-bottom:10px;'>First Name : </label>"
                        +"<input type='text' id='first_name' maxlength='32' style='width: 100px;' name='first_name' value=''>"
                        +"<div class='inputNote'><span class='small'>(as it appears on card)</span></div>"
                        +"</div>"
                        +"<div>"
                        +"<label for='last_name'>Last Name:</label>"
                        +"<input type='text' id='last_name' maxlength='32' style='width: 100px;' name='last_name' value=''>"
                        +"<div class='inputNote'><span class='small'>(as it appears on card)</span></div>"
                        +"</div>"
                        +"<div>"
                        +"<label for='cc_number'>Card Number:</label>"
                        +"<input type='text' id='cc_number' maxlength='19' style='width: 100px;' name='cc_number' value=''>"
                        +"</div>"
                        +"<div>"
                        +"<label for='credit_card_type'>Card Type:</label>"
                        +"<select id='credit_card_type' name='credit_card_type'" // onChange='showMoreFields(this);'>"
                        +"<option value=' '>Select Card</option>"
                        +"<option value='M'>MasterCard</option>"
                        +"<option value='V'>Visa</option>"
                        +"<option value='A'>American Express</option>"
                        +"</select>"
                        +"</div>"
                        +"<div>"
                        +"<label for='expdate_month'>Expiration Date:</label>"
                        +"<select id='expdate_month' name='expdate_month'>"
                        +"<option value='1'>01</option>"
                        +"<option value='2'>02</option>"
                        +"<option value='3'>03</option>"
                        +"<option value='4'>04</option>"
                        +"<option value='5'>05</option>"
                        +"<option value='6'>06</option>"
                        +"<option value='7'>07</option>"
                        +"<option value='8'>08</option>"
                        +"<option value='9'>09</option>"
                        +"<option value='10'>10</option>"
                        +"<option value='11'>11</option>"
                        +"<option value='12'>12</option>"
                        +"</select>"
                        +"<select id='expdate_year' name='expdate_year'>";
                         java.util.Date dt = new java.util.Date();
                         String DATE_FORMAT = "yyyy";
                         SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
                         Calendar c1 = Calendar.getInstance();
                         for(int cnt = 0 ;cnt<20;cnt++){
                             CCForm +="<option value='2009'>"+sdf.format(c1.getTime())+"</option>" ;
                             c1.add(java.util.Calendar.YEAR, 1);
                         }
                        CCForm +="</select>"
                        +"</div>"
                        +"<div>"
                        +"<label for='cvv2_number'>Card Security Code:</label>"
                        +"<input type='text' id='cvv2_number' size='3' maxlength='4' name='cvv2_number' value=''/>"
                        +"</div>"
                        +"<p>"
                        +"<div id='input-submit'>"
                        +"<input id='SubmitButton' type='submit' value='Submit' name='SubmitButton'/>"
                        +"</div>"
                        +"</p>"
                        +"</form>";
       out.println(newform+CCForm);

%>
