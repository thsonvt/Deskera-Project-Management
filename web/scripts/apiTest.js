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
var http = getHTTPObject();
var LOGIN_PREFIX = "http://192.168.0.72:8084/KrawlerESP/remoteapi.jsp";


function testFunction(action){
    var p = "action=" + action + "&data=" + getTestParam(action);
    http.open('POST', LOGIN_PREFIX, true);
    http.setRequestHeader("Content-length", p.length);
    http.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    http.onreadystatechange = handleResponse;
    http.send(p);
}

function getTestParam(action){
    var param = "{}";
    switch(action){
        case 0:
            param = "{test:true,subdomain:demo}";
            break;
        case 1:
            param = "{test:true,subdomain:demo,username:john}";
            break;
        case 2:
            param = "{test:true,subdomain:demo,username:remotetest,fname:remoteapi,lname:test,email:anup@mailinator.com}";
            break;
        case 3:
            param = "{test:true,subdomain:demo,username:remotetest,fname:remoteapi,lname:test,email:anup@mailinator.com,companyname:apitestcompany}";
            break;
        case 4:
            param = "{test:true,subdomain:demo,username:john}";
            break;
    }
    return param;
}

function handleResponse(){
    if(http.readyState == NORMAL_STATE) {
        if(http.responseText && http.responseText.length > 0) {
            var results = eval("(" + trimStr(http.responseText) + ")");
            var dom;
            switch(results.action){
                case 0:
                    dom = document.getElementById("companyCheck_result");
                    break;
                case 1:
                    dom = document.getElementById("userCheck_result");
                    break;
                case 2:
                    dom = document.getElementById("userCreate_result");
                    break;
                case 3:
                    dom = document.getElementById("companyCreate_result");
                    break;
                case 4:
                    dom = document.getElementById("deleteUser_result");
                    break;
            }
            if(dom !== undefined){
                if(results.success) {
                    dom.innerHTML = results.infocode;
                } else {
                    dom.innerHTML = results.errorcode;
                }
            }
        }
    }
}
