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
var LOGIN_PREFIX = "remoteapi.jsp";
var act;

function testFunction(action){
    act=action;
    var p = "action=" + action + "&data=" + getTestParam(action);
    http.open('POST', LOGIN_PREFIX, true);
    http.setRequestHeader("Content-length", p.length);
    http.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    http.onreadystatechange = handleResponse;
    http.send(p);
}

function getTestParam(action){
    var param = "{}";
    var element="";
    var email_check=document.apiform.email_check[0].checked;
    var commit_check=document.apiform.commit_check[0].checked;
    var element1="",element2="",element3="",element4="",element8="" ;
    switch(action){
        case 0://Company Exist
            element = document.getElementById('C_E_companyCheck').value;
            if(element.length>0){
                param = '{test:true,companyid:"'+element+'"}';
            }else{
                element = document.getElementById('C_E_S_companyCheck').value;
                if(element.length>0){
                    param = '{test:true,subdomain:"'+element+'"}';
                }
                else{}
                   // alert("PLEASE FILL MANDATORY FIELDS");
            }
            break;

        case 1://User Exist
            element = document.getElementById('U_E_userIdCheck').value;
            if(element.length>0){
                param = '{test:true,userid:"'+element+'"}';
            }else{
                element = document.getElementById('U_E_username').value;
                if(element.length>0){
                    element1 = document.getElementById('U_E_companyid').value;
                    if(element1.length>0){
                        param = '{test:true,companyid:"'+element1+'",username:"'+element+'"}';
                    }
                    else{
                        element1 = document.getElementById('U_E_subdomain').value;
                        param = '{test:true,subdomain:"'+element1+'",username:"'+element+'"}';
                    }
                 }
                 else{
                 
                 }
            }
            break;

        case 2://Create User
            element1 = document.getElementById('C_U_username').value;
            element2 = document.getElementById('C_U_email').value;
            element3 = document.getElementById('C_U_firstName').value;
            element4 = document.getElementById('C_U_lastName').value;
            var element5=document.getElementById('C_U_companyid').value;
            if(element1.length>0 && element2.length>0 && element3.length>0 && element4.length>0 && element5.length>0)
                param = '{username:"'+element1+'",fname:"'+element3+'",lname:"'+element4+'",email:"'+element2+'",companyid:"'+element5+'",sendmail:'+email_check+',test:'+commit_check+'}';
            else{
                 var element5=document.getElementById('C_U_subdomain').value;
                 param = '{username:"'+element1+'",fname:"'+element3+'",lname:"'+element4+'",email:"'+element2+'",subdomain:"'+element5+'",sendmail:'+email_check+',test:'+commit_check+'}';
            }

            break;

        case 3://Create Comapany
            element1 = document.getElementById('C_C_companyname').value;
            element2 = document.getElementById('C_C_email').value;
            element4 = document.getElementById('C_C_firstName').value;
            element5 = document.getElementById('C_C_lastName').value;
            element8 = document.getElementById('C_C_subdomain').value;
           var element7 = document.getElementById('C_C_username').value;
           element3 = document.getElementById('C_C_phone').value;
           var element6 = document.getElementById('C_C_address').value;
            if(element8.length>0 && element1.length>0 && element2.length>0 &&  element4.length>0 &&  element5.length>0 &&  element7.length>0)
              param = '{subdomain:"'+element8+'",companyname:"'+element1+'",email:"'+element2+'",fname:"'+element4+'",lname:"'+element5+'",username:"'+element7+'",phno:"'+element3+'",address:"'+element6+'",sendmail:"'+email_check+'",test:'+commit_check+'}';
            else{}
             //   alert("PLEASE FILL MANDATORY FIELDS");
            break;

        case 4://Delete User
            element = document.getElementById('U_E_D_userIdCheck').value;
            if(element.length>0){
                param = '{test:'+commit_check+',userid:"'+element+'"}';
            }else{
                element = document.getElementById('U_E_D_username').value;
                if(element.length>0){
                    element1 = document.getElementById('U_E_D_companyid').value;
                    if(element1.length>0){
                        param = '{test:'+commit_check+',companyid:"'+element1+'",username:"'+element+'"}';
                    }else{
                        element1=document.getElementById('U_E_D_subdomain').value;
                        param = '{test:'+commit_check+',subdomain:"'+element1+'",username:"'+element+'"}';
                    }
                }
                else{
               //     alert("PLEASE FILL MANDATORY FIELDS");
                }
            }
            break;

    }
    return param;
}

function fun1(){
			a=document.getElementById('companyCheck_result');
            b=document.getElementById('userCheck_result');
            c=document.getElementById('userCreate_result');
            d=document.getElementById('companyCreate_result');
            e=document.getElementById('deleteUser_result');
			a.style.visibility='hidden';
            b.style.visibility='hidden';
            c.style.visibility='hidden';
            d.style.visibility='hidden';
            e.style.visibility='hidden';
}

function handleResponse(){
    if(http.readyState == NORMAL_STATE) {
        if(http.responseText && http.responseText.length > 0) {
            var results = eval("(" + trimStr(http.responseText) + ")");
         //   alert(http.responseText);
            var dom = "";
            var responseMessage = "";
            switch(act){
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
                    switch(results.infocode){
                    case "m01":
                        responseMessage = "Company exists.";
                        break;
                    case "m02":
                        responseMessage = "Company doesn't exist."; onClick="javashide()"
                        break;
                    case "m03":
                        responseMessage = "User exists.";
                        break;
                    case "m04":
                        responseMessage = "User doesn't exist.";
                        break;
                    case "m05":
                        responseMessage = "User created successfully.";
                        break;
                    case "m06":
                        responseMessage = "Company created successfully.";
                        break;
                    case "m07":
                        responseMessage = "User deleted successfully.";
                        break;
                    case "m08":
                        responseMessage = "Role assigned successfully.";
                        break;
                    }
                   // dom.innerHTML = responseMessage;
                } else {
                       switch(results.errorcode){
                        case "e01":
                            responseMessage = "Insufficient data.";
                            break;
                        case "e02":
                            responseMessage = "Error connecting to server.";
                            break;
                        case "e03":
                            responseMessage = "User with same username already exists.";
                            break;
                        case "e04":
                            responseMessage = "Company does not exist.";
                            break;
                        case "e05":
                            responseMessage = "Error while sending mail.";
                            break;
                        case "e06":
                            responseMessage = "User doesn't exist.";
                            break;
                        case "e07":
                            responseMessage = "Subdomain already exists.";
                            break;
                    }
                    
                }
                dom.style.visibility='visible';
                dom.innerHTML = responseMessage;
             }
        }
    }
}
