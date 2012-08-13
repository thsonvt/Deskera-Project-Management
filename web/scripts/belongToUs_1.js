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
var LOGIN_PREFIX = 'jspfiles/auth.jsp';

// variables
var http = getHTTPObject();

function validateLogin(u, p, demo){
    var d = new Date().getTime();
    var p = 't=a&u=' + encodeURI(u) + '&p=' + encodeURI(hex_sha1(p));
    if(demo)
        p += "&demo=true";
    http.open('POST', LOGIN_PREFIX, true);
    http.setRequestHeader("Content-length", p.length);
    http.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    http.onreadystatechange = handleHttpValidateLogin;
    http.send(p);
    setLoading(1);
    setMsg("Loading", 1);
}

function SetCookie(name, value){
    document.cookie = name + "=" + value + ";path=/;";
}

function getCookie(c_name){
    if (document.cookie.length > 0) {
        c_start = document.cookie.indexOf(c_name + "=");
        if (c_start != -1) {
            c_start = c_start + c_name.length + 1;
            c_end = document.cookie.indexOf(";", c_start);
            if (c_end == -1)
                c_end = document.cookie.length;
            return unescape(document.cookie.substring(c_start, c_end));
        }
    }
    return "";
}

function handleHttpValidateLogin(){
    if (http.readyState == NORMAL_STATE) {
        if (http.responseText && http.responseText.length > 0) {
            var results = eval("(" + trimStr(http.responseText) + ")");
            if (results.success == true) {
                SetCookie("lid", results.lid);
                SetCookie("username", results.username);
				SetCookie("lastlogin",results.lastlogin);
                if(results.demo)
                    redirect("demo=true");
                else
                    redirect();
            }
            else {
                setMsg(results.message, 0);
            }
        }
        else {
            setMsg("An error occurred while connecting to service.", 0);
        }
        setLoading(0);
    }
}

function redirect(param){
    var link = "./";
    if(param !== undefined)
        link = "../../view-debug-ex.html?" + param;
    window.top.location.href = link;
}

function setLoading(status){
    var lBtn = document.getElementById('LoginButton');
    var pwd = document.getElementById('Password');
    switch (status) {
        case 0:
            pwd.value = "";
            lBtn.disabled = false;
            break;
        case 1:
            lBtn.disabled = true;
            break;
    }
}

function lValidateEmpty(){
    var usr = document.getElementById('UserName');
    var pwd = document.getElementById('Password');
    var usrReq = document.getElementById('UserNameRequired');
    var pwdReq = document.getElementById('PasswordRequired');
    var bL = (!usr.value || trimStr(usr.value).length == 0);
    var bP = (!pwd.value || pwd.value.length == 0);
    setVisibility(bL, usrReq);
    setVisibility(bP, pwdReq);
    if (!(bL || bP)) {
        validateLogin(trimStr(usr.value), pwd.value);
    }
}

function checkCookie(){
    u = getCookie('username');
    if (!(u == null || u == "")) {
        redirect();
    }
}

function formFocus(){
    var f = document.getElementById("loginForm");
    if (f) {
        if (f.UserName.value == null || f.UserName.value == "") {
            f.UserName.focus();
        }
        else {
            f.Password.focus();
        }
    }
    var page = window.location.href.split('?')[1];
    if (page !== undefined) {
        if (page == "timeout") {
            setMsg("Session Timed Out", 0);
        }
    }
}
