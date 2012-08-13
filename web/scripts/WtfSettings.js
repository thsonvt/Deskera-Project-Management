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
var isPlanChanged=false;   //any change in project plan
var calLoad = false;
var docScriptLoaded = false;
var bHasChanged = false;
projAdminChange = false;
var tabRegister = []; //for finding active sub-tab
// Project global variables
var random_number = Math.random() * 100000;
//Calendar global variables
var fixHeight = 30;
var helpmode = false;
// Global variables for : SNSPortal-mail
//var portalmail_grid1;
//var portalmail_sm1;
//var portalmail_mainPanel;
//var portalmail_titleflag;
//var portalmail_folderid;
//var portalmail_actionMenuForPMsg;
//var portalmail_actionMenu;
var loginid = 0;//_gC('lid');
var loginname = "";
//var projScale = 'day';
var companyid = 0;//_gC('cid');
//var portalforum_totalPageCount;//for paging in tree
//var portalforum_pageCount = 1;//for paging in tree
//var portalforum_flag = true;//for sorting in tree

/*change by kamlesh*/
Wtf.proj.priority =["High","Moderate","Low"];
Wtf.proj.pr.HIGH = 0;
Wtf.proj.pr.MODERATE = 1;
Wtf.proj.pr.LOW = 2;
Wtf.proj.resources.type.WORK = 1;
Wtf.proj.resources.type.MATERIAL = 2;
Wtf.proj.resources.type.COST = 3;
Wtf.proj.EDIT_PERT_SHEET = 1;
Wtf.proj.SHOW_CPA_PERT_SHEET = 2;
Wtf.proj.SHOW_CPA_WITHOUT_PERT_SHEET = 3;
/*--end project plan priority*/
Wtf.BLANK_IMAGE_URL = "../../lib/resources/images/default/s.gif";
Wtf.DEFAULT_USER_URL = "../../images/user100.png";
Wtf.validateURL = /(((https?)|(ftp)):\/\/([\-\w]+\.)+\w{2,3}(\/[%\-\w]+(\.\w{2,})?)*(([\w\-\.\?\\\/+@&#;`~=%!]*)(\.\w{2,})?)*\/?)/i;
Wtf.ValidateMailPatt = /^([a-zA-Z0-9_\-\.+]+)@(([0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.)|(([a-zA-Z0-9\-]+\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})$/;
Wtf.ValidateUserid = /^\w+$/;
Wtf.ValidateUserName = /^[\w\s\'\"\.\-]+$/;
Wtf.validateHeadTitle = /^[\w\s\'\"\.\-\,\~\!\@\$\^\*\(\)\{\}\[\])]+$/;
Wtf.validatePhone = /^([0-9\(\)\/\+ \-]*)$/;
Wtf.validateBaseCampFile=/^.+(.xml|.XML)$/;
Wtf.validateCSVFile=/^.+(.csv|.CSV)$/;
Wtf.validateICSFile=/^.+(.ics|.ICS)$/;
Wtf.validateMSProjectFile=/^.+(.mpx|.MPX|.mpp|.MPP)$/;
Wtf.validateImageFile=/^.+(.jpg|.JPG|.gif|.GIF|.png|.PNG|.jpeg|.JPEG|.tif|.TIF|.tiff|.TIFF)$/;
Wtf.validateCC = /^[a-zA-Z0-9_\s]+$/;
Wtf.fixHeight = 30;
Wtf.Week = ["Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"];
Wtf.DomainPatt = /[ab]\/([^\/]*)\/(.*)/;

Wtf.hm.status = {
    ONTIME:'1',
    SLIGHTLY_BEHIND:'2',
    GRAVELY_BEHIND:'3'
}
comboConfig ={
    mode:'local',
    editable:false,
    typeAhead : true,
    triggerAction: 'all'
}
function getTopHtml(text, body,img){
     if(img===undefined) {
        img = '../../images/createuser.png';
    }
     var str =  "<div style = 'width:100%;height:100%;position:relative;float:left;'>"
                    +"<div style='float:left;height:100%;width:auto;position:relative;'>"
                    +"<img src = "+img+"  class = 'adminWinImg'></img>"
                    +"</div>"
                    +"<div style='float:left;height:100%;width:79%;position:relative;'>"
                    +"<div style='font-size:12px;font-style:bold;float:left;margin:15px 0px 0px 10px;width:100%;position:relative;'><b>"+text+"</b></div>"
                    +"<div style='font-size:10px;float:left;margin:15px 0px 10px 10px;width:100%;position:relative;'>"+body+"</div>"
                    +"</div>"
                    +"</div>" ;
     return str;
//    if(img===undefined) {
//        img = '../../images/createuser.png';
//    }
//    return("<div class = 'adminWinCont'>"
//                     +"<div class = 'adminWinImgDiv'>"
//                        +"<img src = '"+img+"' class = 'adminWinImg'></img>"
//                     +"</div>"
//                     +"<div class = 'adminWinDesc'>"
//                         +"<div class = 'adminWinTitle'><strong>" + text + "</strong></div>"
//                         +"<div class = 'adminWinAbout'>"+ body +"</div>"
//                     +"</div></div>");
}
smileyStore = new Array(':)', ':(', ';)', ':D', ';;)', '&gt;:D&lt;', ':-/', ':x', ':&gt;&gt;', ':P', ':-*', '=((', ':-O', 'X(', ':&gt;', 'B-)', ':-S', '#:-S', '&gt;:)', ':((', ':))', ':|', '/:)', '=))', 'O:-)', ':-B', '=;', ':-c');

Wtf.etype = {
    user: 0,
    comm: 1,
    proj: 2,
    home: 3,
    docs: 4,
    cal: 5,
    forum: 6,
    pmessage: 7,
    pplan: 8,
    adminpanel: 9,
    todo: 10,
    search: 11,
    projdoc: 12,
    log: 13/*,
 *
    acc: 12,
    accreports: 13,
    acccustomer: 14,
    accvendor: 15,
    accemployee: 16,
    crm: 17*/
};

/*Wtf.contenttype = {
    bulletlist: 0,
    imagelist: 1,
    plainHTML: 2
};*/

//function getCookie(c_name){
//    if (document.cookie.length > 0) {
//        c_start = document.cookie.indexOf(c_name + "=");
//        if (c_start != -1) {
//            c_start = c_start + c_name.length + 1;
//            c_end = document.cookie.indexOf(";", c_start);
//            if (c_end == -1)
//                c_end = document.cookie.length;
//            return unescape(document.cookie.substring(c_start, c_end));
//        }
//    }
//    return "";
//}
//function nameRenderer(value){
//    var resultval = value.substr(0, 1);
//    var patt1 = new RegExp("^[a-zA-Z]");
//    if (patt1.test(resultval)) {
//        return resultval.toUpperCase();
//    }
//    else
//        return "Others";
//}
//
//function sizeRenderer(value){
//    var sizeinKB = value
//    if (sizeinKB >= 1 && sizeinKB < 1024) {
//        text = "Small";
//    }
//    else
//        if (sizeinKB > 1024 && sizeinKB < 102400) {
//            text = "Medium";
//        }
//        else
//            if (sizeinKB > 102400 && sizeinKB < 1048576) {
//                text = "Large";
//            }
//            else {
//                text = "Gigantic";
//            }
//    return text;
//}
//
//function dateFieldRenderer(value){
//    if (value) {
//        var dt = new Date();
//        if ((value.getMonth() == dt.getMonth()) && (value.getYear() == dt.getYear())) {
//            if (dt.getDate() == value.getDate()) {
//                text = "Today";
//            }
//            else
//                if (value.getDate() == (dt.getDate() - 1))
//                    text = "Yesterday";
//                else
//                    if (value.getDate() <= (dt.getDate() - 7) && value.getDate() > (dt.getDate() - 14))
//                        text = "Last Week";
//        }
//        else
//            if ((value.getMonth() == (dt.getMonth() - 1)) && (value.getYear() == dt.getYear()))
//                text = "Last Month";
//            else
//                if ((value.getYear() == (dt.getYear() - 1)))
//                    text = "Last Year";
//                else
//                    text = "Older";
//    }
//    else {
//        text = WtfGlobal.getLocaleText('lang.none.text');
//    }
//    return text;
//}
//
//function permissionRenderer(value, rec){
//    var text = value.toLowerCase();
//    switch (text) {
//        case "everyone":
//            text = "Everyone on deskEra";
//            break;
//        case "connections":
//            text = "All Connections";
//            break;
//        case "none":
//            text = "Private";
//            break;
//        default:
//            text = "Selected Connections";
//            break;
//    }
//    return text;
//}
//
//function HTMLStripper(val){
//    var str = Wtf.util.Format.stripTags(val);
//    return str.replace(/"/g, '').trim();
//};
//
//function ScriptStripper(str){
//    var str = Wtf.util.Format.stripScripts(str);
//    if (str)
//        return str.replace(/"/g, '');
//    else
//        return str;
//};
function importLogLink(){
    return '<a class="guideme" style="font-weight:bold;" onclick="displayImportLog();" href="#">'+WtfGlobal.getLocaleText("pm.dashboard.widget.quicklinks.importlog")+'</a>';
}

function msgBoxShow(choice, type){
    var strobj = [];
    switch (choice) {
 case 1:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.1')];
            break;
        case 2:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.2')];
            break;
        case 3:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.3')];
            break;
        case 4:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.4')];
            break;
        //WtfAdminControl.js -->
        case 5:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.5')];
            break;
        case 6:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.6')];
            break;
        case 7:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText('pm.msg.7')];
            break;
        case 8:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.8')];
            break;
        case 9:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText('pm.msg.9')];
            break;
        case 10:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.10')];
            break;
        case 11:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.11')];
            break;
        case 12:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'),WtfGlobal.getLocaleText('pm.msg.12')];
            break;
        case 13:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText('pm.msg.13')];
            break;
        case 14:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.14')];
            break;
        case 15:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText('pm.msg.15')];
            break;
        case 16:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.16')];
            break;
        case 17:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'),WtfGlobal.getLocaleText('pm.msg.17')];
            break;
        case 18:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText('pm.msg.18')];
            break;
        case 19:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.19')];
            break;
        case 20:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.20')];
            break;
        case 21:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.21')];
            break;
        case 22:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.22')];
            break;
        case 23:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.23')];
            break;
        case 24:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText('pm.msg.24')];
            break;
        case 25:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.25')];
            break;
        case 26:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.26') ];
            break;
        case 27:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.27') ];
            break;
        //WtfAdminTab.js
        case 28:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText('pm.msg.28')];
            break;
        case 29:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'),WtfGlobal.getLocaleText('pm.msg.29')];
            break;
        case 30:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'),WtfGlobal.getLocaleText('pm.msg.30')];
            break;
        case 31:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText('pm.msg.31')];
            break;
        case 32:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.32')];
            break;
        case 33:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText('pm.msg.33')];
            break;
        case 34:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'),WtfGlobal.getLocaleText('pm.msg.34')];
            break;
        case 35:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.35')];
            break;
        case 36:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.36')];
            break;
        case 37:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.37')];
            break;
/*        //WtfContactDetails
        case 38:
            strobj = [WtfGlobal.getLocaleText('pm.contacts.editcontact'), "No record selected to edit"];
            break;
        //WtfProjContainer.js
*/        case 39:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.39')];
            break;
        case 40:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.40')];
            break;
        case 41:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.41')];
            break;
        case 42:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.42')];
            break;
        case 43:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.43')];
            break;
        case 44:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'),WtfGlobal.getLocaleText('pm.msg.44')];
            break;
        case 45:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.45')];
            break;
        case 46:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.46')];
            break;
        case 47:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'),WtfGlobal.getLocaleText('pm.msg.47')];
            break;
        case 48:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'),WtfGlobal.getLocaleText('pm.msg.48')];
            break;
        case 49:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText('pm.msg.49')];
            break;
        case 50:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.50')];
            break;
        case 51:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.51')];
            break;
        case 52:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText("pm.msg.52")];
            break;
        case 53:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'),WtfGlobal.getLocaleText('pm.msg.53')];
            break;
        case 54:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.54')];
            break;
        //WtfProjPlanner.js
        case 55:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'),WtfGlobal.getLocaleText('pm.msg.55')];
            break;
        case 56:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'),WtfGlobal.getLocaleText('pm.msg.56')];
            break;
        case 57:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'),WtfGlobal.getLocaleText('pm.msg.57')];
            break;
        case 58:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.58')];
            break;
        case 59:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'),WtfGlobal.getLocaleText('pm.msg.59')];
            break;
        case 60:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.60')];
            break;
        case 61:
            strobj = [WtfGlobal.getLocaleText('pm.msg.WARNING'), WtfGlobal.getLocaleText('pm.msg.61')];
            break;
        case 62:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.62')];
            break;
        case 63:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.63')];
            break;
        case 64:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.64')];
            break;
        case 65:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.65')];
            break;
        case 66:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.66')];
            break;
        case 67:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.67')];
            break;
        case 68:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'),WtfGlobal.getLocaleText('pm.msg.68')];
            break;
        case 69:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.69')];
            break;
        case 70:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'),WtfGlobal.getLocaleText('pm.msg.70')];
            break;
        case 71:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.71')];
            break;
        case 72:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.72')];
            break;
        case 73:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.73')];
            break;
        case 74:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'),WtfGlobal.getLocaleText('pm.msg.74')];
            break;
        case 75:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'),WtfGlobal.getLocaleText('pm.msg.75')];
            break;
        case 76:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.76')];
            break;
        case 77:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'),WtfGlobal.getLocaleText('pm.msg.77')];
            break;
        case 78:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.78')];
            break;
        case 79:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'),WtfGlobal.getLocaleText('pm.msg.79')];
            break;
        case 80:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.80')];
            break;
        case 81:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.81')];
            break;
        case 82:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText('pm.msg.82')];
            break;
/*        case 83:
            strobj = [WtfGlobal.getLocaleText('pm.msg.FAILURE'), 'Error connecting to the server'];
            break;
*/        case 84:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.84')];
            break;
/*        case 85:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), "Users added successfully."];
            break;
        case 86:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), "Sorry. Users could not be added."];
            break;
*/        case 88:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.88')];
            break;
        case 89:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.89')];
            break;
        case 90:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.90')];
            break;
        case 91:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), 'You cannot remove members from resources.<br/>You can drop project members from the \'Members\' tab.'];
            break;
/*        case 92:
            strobj = ['Status', 'Transaction added successfully'];
            break;
        case 93:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), 'An error occurred while connecting to server'];
            break;
        case 94:
            strobj = ['Status', 'Item created successfully'];
            break;
        case 95:
            strobj = ['Status', 'Account created successfully'];
            break;
        case 96:
            strobj = ['Status', 'Sales Tax created successfully'];
            break;
        case 97:
            strobj = ['Status', 'Onhand updated successfully'];
            break;
        case 98:
            strobj = ['Credit Error', 'You cannot use more than the credit amount'];
            break;
        case 99:
            strobj = ['Status', 'Invoice created successfully'];
            break;
        case 100:
            strobj = ['Status', 'Credit applied successfully'];
            break;
        case 101:
            strobj = ['Status', 'Amount refunded successfully'];
            break;
        case 102:
            strobj = ['Status', 'Credit memo created successfully'];
            break;
        case 103:
            strobj = ['Status', 'Deposited successfully'];
            break;
        case 104:
            strobj = ['Status', 'Payment done successfully'];
            break;
        case 105:
            strobj = ['Status', 'Sales Receipt created successfully'];
            break;
        case 106:
            strobj = ['Status', 'Statement Charge created successfully'];
            break;
        case 107:
            strobj = ['Status', 'Bill created successfully'];
            break;
        case 108:
            strobj = ['Status', 'Item Receipt created successfully'];
            break;
        case 109:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Amount entered is greater than amount due'];
            break;
        case 110:
            strobj = ['Status', 'Bill paid successfully'];
            break;
        case 111:
            strobj = ['Status', 'Purchase Order created successfully'];
            break;
        case 112:
            strobj = ['Status', 'Vendor created successfully'];
            break;
        case 113:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Please enter the valid end date for recurrence!'];
            break;
        case 114:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Please enter the no. of weeks for recurrence!'];
            break;
        case 115:
            strobj = ["Invalid Event", "Event doesn't exist!"];
            break;
*/        case 116:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.116')];
            break;
        case 117:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.117')];
            break;
/*        case 118:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Please select a row to edit.'];
            break;
        case 119:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Select only one event at a time.'];
            break;
        case 120:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Please select a row to delete.'];
            break;
        case 121:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), 'You dont have sufficient privileges to delete the events!'];
            break;
        case 122:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Calendar Creation Failed!'];
            break;
        case 123:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Calendar Updation Failed!'];
            break;
        case 124:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Calendar Deletion Failed!'];
            break;
        case 125:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Event Updation Failed!'];
            break;
        case 126:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Events do not exist or have been already deleted!'];
            break;
        case 127:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), 'No event to delete.'];
            break;
        case 128:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Error occurred while importing Calendar.'];
            break;
        case 129:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), 'Calendar imported successfully.'];
            break;
        case 130:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Please select a calendar.'];
            break;
        case 131:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Problem occcured while displaying Lead'];
            break;
        case 132:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Please select two versions to compare'];
            break;
*/      case 131:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.131')];
            break;
        case 132:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText('pm.msg.132')];
            break;
        case 133:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.133')];
            break;
        case 134:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText('pm.msg.134')];
            break;
        case 135:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.135')];
            break;
/*        case 136:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), 'Events deleted successfully.'];
            break;
        case 137:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), 'Calendar deleted successfully.'];
            break;
        case 138:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Selected calendar does not exist or has been deleted!'];
            break;
*/        case 139:
            strobj = [WtfGlobal.getLocaleText('pm.msg.INFORMATION'), WtfGlobal.getLocaleText('pm.msg.139')];
            break;
        case 140:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.140')];
            break;
        case 141:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText('pm.msg.141')];
            break;
        case 142:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.142')];
            break;
        case 143:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.143')];
            break;
        case 144:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.144')];
            break;
        case 145:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.145')];
            break;
        case 146:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.146')];
            break;
        case 147:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText('pm.msg.147')];
            break;
        case 148:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText('pm.msg.148')];
            break;
        case 149:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText('pm.msg.149')];
            break;
        case 150:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText('pm.msg.150')];
            break;
        case 151:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText('pm.msg.151')];
            break;
        case 152:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.152')];
            break;
        case 153:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.153')];
            break;
        case 154:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.154')];
            break;
        case 155:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.155')];
            break;
        case 156:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.156')];
            break;
        case 157:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.157')];
            break;
        case 158:
            strobj = [WtfGlobal.getLocaleText('pm.msg.INFORMATION'), WtfGlobal.getLocaleText('pm.msg.158')];
            break;
        case 159:
            strobj = [WtfGlobal.getLocaleText('pm.msg.INFORMATION'), WtfGlobal.getLocaleText('pm.msg.159')];
            break;
        case 160:
            strobj = [WtfGlobal.getLocaleText('pm.msg.INFORMATION'), WtfGlobal.getLocaleText('pm.msg.160')];
            break;
        case 161:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.161')];
            break;
        case 162:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.162')];
            break;
        case 163:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.163')];
            break;
        case 164:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'),WtfGlobal.getLocaleText('pm.msg.164')];
            break;
        case 165:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'),WtfGlobal.getLocaleText('pm.msg.165')];
            break;
        case 166:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'),WtfGlobal.getLocaleText('pm.msg.166')];
            break;
        case 167:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.167')];
            break;
        case 168:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'),WtfGlobal.getLocaleText('pm.msg.168')];
            break;
        case 169:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'),WtfGlobal.getLocaleText('pm.msg.169')];
            break;
        case 170:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.170')];
            break;
        case 171:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.171')];
            break;
        case 172:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.172')];
            break;
        case 173:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.173')];
            break;
        case 174:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'),WtfGlobal.getLocaleText('pm.msg.174')];
            break;
        case 175:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText('pm.msg.175')];
            break;
        case 176:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.176')];
            break;
        case 177:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.177')];
            break;
        case 178:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.178')];
            break;
        case 179:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.179')];
            break;
        case 180:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'),WtfGlobal.getLocaleText('pm.msg.180')];
            break;
        case 181:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'),WtfGlobal.getLocaleText('pm.msg.181')];
            break;
        case 182:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.182')];
            break;
        case 183:
            strobj = [WtfGlobal.getLocaleText('pm.msg.WARNING'), WtfGlobal.getLocaleText('pm.msg.183')];
            break;
        case 184:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.184')];
            break;
        case 185:
            strobj = [WtfGlobal.getLocaleText('pm.msg.INFORMATION'), WtfGlobal.getLocaleText('pm.msg.185')];
            break;
        case 186:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.186')];
            break;
        case 187:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.187')];
            break;
        case 188:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.188')];
            break;
        case 189:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.189')];
            break;
        case 190:
            strobj = [WtfGlobal.getLocaleText('pm.msg.NOT_ALLOWED'),WtfGlobal.getLocaleText('pm.msg.190')];
            break;
        case 191:
            strobj = [WtfGlobal.getLocaleText('pm.msg.NOT_ALLOWED'),WtfGlobal.getLocaleText('pm.msg.191')];
            break;
        case 192:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'),WtfGlobal.getLocaleText('pm.msg.192')];
            break;
        case 193:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'),WtfGlobal.getLocaleText('pm.msg.193')];
            break;
        case 194:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'),WtfGlobal.getLocaleText('pm.msg.194')];
            break;
        case 195:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'),WtfGlobal.getLocaleText('pm.msg.195')];
            break;
        case 196:
            strobj = [WtfGlobal.getLocaleText('pm.msg.INFORMATION'), WtfGlobal.getLocaleText('pm.msg.196')];
            break;
        case 197:
            strobj = ["Cannot compare", WtfGlobal.getLocaleText('pm.msg.197')];
            break;
        case 198:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'),WtfGlobal.getLocaleText('pm.msg.198')];
            break;
        case 199:
            strobj = ['Error while importing',WtfGlobal.getLocaleText('pm.msg.199')];
            break;
        case 200:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'),WtfGlobal.getLocaleText('pm.msg.200')];
            break;
        case 201:
            strobj = [WtfGlobal.getLocaleText('pm.msg.WARNING'),WtfGlobal.getLocaleText('pm.msg.201')];
            break;
        case 202:
            strobj = [WtfGlobal.getLocaleText('pm.msg.WARNING'),WtfGlobal.getLocaleText('pm.msg.202')];
            break;
        case 203:
            strobj = [WtfGlobal.getLocaleText('pm.msg.WARNING'), WtfGlobal.getLocaleText('pm.msg.203')];
            break;
        case 204:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.204')];
            break;
        case 205:
            strobj = [WtfGlobal.getLocaleText('pm.msg.INFORMATION'), WtfGlobal.getLocaleText('pm.msg.205')];
            break;
        case 206:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.206')];
            break;
        case 207:
            strobj = [WtfGlobal.getLocaleText('pm.msg.INFORMATION'), WtfGlobal.getLocaleText('pm.msg.207')];
            break;
        case 208:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText('pm.msg.208')];
            break;
        case 209:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.209')];
            break;
        case 210:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText('pm.msg.210')];
            break;
        case 211:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.211')];
            break;
        case 212:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.212')];
            break;
        case 213:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.213')];
            break;
        case 214:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'),WtfGlobal.getLocaleText('pm.msg.214.1') +' '+importLogLink()+' '+ WtfGlobal.getLocaleText('pm.msg.214.2')];
            break;
       case 215:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.215')];
            break;
        case 216:
            strobj = [WtfGlobal.getLocaleText('pm.common.failure'),WtfGlobal.getLocaleText('pm.Help.failDelReport')];
            break;
        case 217:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText('pm.msg.217')];
            break;
        case 218:
            strobj = [WtfGlobal.getLocaleText('pm.msg.INFORMATION'),WtfGlobal.getLocaleText('pm.msg.218')];
            break;
        case 219:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.219')];
            break;
        case 220:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.291')];
            break;
       case 224:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.224')];
            break;
        case 225:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.232')];
            break;
        case 226:
            strobj = [WtfGlobal.getLocaleText('lang.error.text'), WtfGlobal.getLocaleText('pm.msg.226')];
            break;
        case 227:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.227')];
            break;
        case 228:
            strobj = [WtfGlobal.getLocaleText('pm.msg.WARNING'), WtfGlobal.getLocaleText('pm.msg.228')];
            break;
        case 229:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.229')];
            break;
        case 230:
            strobj =[WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText('pm.msg.230')];
            break;
        case 231:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.231')];
            break;
        case 232:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.232')];
            break;
        case 233:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText('pm.msg.233')];
            break;
        case 234:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.250')];
            break;
        case 235:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.244')];
            break;
        case 236:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.236')];
            break;
        case 237:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText('pm.msg.237')];
            break;
        case 238:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.238')];
            break;
        case 239:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.239')];
            break;
        case 240:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.240')];
            break;
        case 241:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.253')];
            break;
        case 242:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.242')];
            break;
        case 243:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.243')];
            break;
        case 244:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.244')];
            break;
        case 245:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.304')];
            break;
        case 247:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.247')];
            break;
        case 248:
            strobj = [WtfGlobal.getLocaleText('pm.msg.INFORMATION'), WtfGlobal.getLocaleText('pm.msg.248')]
            break;
        
        case 250:
            strobj = [0,WtfGlobal.getLocaleText('pm.msg.250')];
            break;
        case 251:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'),WtfGlobal.getLocaleText('pm.msg.251')];
            break;
        case 252:
            strobj = [WtfGlobal.getLocaleText('pm.common.failure'), WtfGlobal.getLocaleText('pm.msg.303')];
            break;
        case 253:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.253')];
            break;
        case 254:
            strobj = [WtfGlobal.getLocaleText('lang.success.text'),WtfGlobal.getLocaleText('pm.msg.254')];
            break;
        case 255:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.255')];
            break;
        case 256:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.256')];
            break;
        case 257:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.257')];
            break;
        case 258:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.258')];
            break;
        case 259:
            strobj = [WtfGlobal.getLocaleText('lang.success.text'), WtfGlobal.getLocaleText('pm.msg.259')];
            break;
        case 260:
            strobj = [WtfGlobal.getLocaleText('pm.common.failure'), WtfGlobal.getLocaleText('pm.msg.260')];
            break;
        case 262:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.262')];
            break;
        case 263:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.263')];
            break;
        case 264:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.264')];
            break;
        case 265:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.265')];
            break;
        case 266:
            strobj = [WtfGlobal.getLocaleText('pm.msg.FAILURE'), WtfGlobal.getLocaleText('pm.msg.266')];
            break;
        case 267:
            strobj = [WtfGlobal.getLocaleText('pm.msg.FAILURE'),WtfGlobal.getLocaleText('pm.msg.267')];
            break;
        case 268:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'),WtfGlobal.getLocaleText('pm.msg.268')];
            break;
        case 269:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.269')];
            break;
        case 270:
            strobj = [WtfGlobal.getLocaleText('lang.error.text'), WtfGlobal.getLocaleText('pm.msg.270')];
            break;
        case 271:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.271')];
            break;
        case 272:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.272')];
            break;
        case 273:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.273')];
            break;
        case 274:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.274')];
            break;
        case 275:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText('pm.msg.275')];
            break;
        case 276:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText('pm.msg.276')];
            break;
        case 277:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.277')];
            break;
        case 278:
            strobj = [WtfGlobal.getLocaleText('pm.report.save.text'),WtfGlobal.getLocaleText('pm.msg.278')];
            break;
        case 279:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'),WtfGlobal.getLocaleText('pm.msg.279')];
            break;
        case 280:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.231')];
            break;
        case 281:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.281')];
            break;
        case 283:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'),  WtfGlobal.getLocaleText('pm.msg.233')];
            break;
        case 284:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.284')];
            break;
        case 285:
            strobj = [WtfGlobal.getLocaleText('pm.common.failure'),WtfGlobal.getLocaleText('pm.msg.285')];
            break;
        case 286:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'),'Please select a date prior than the \'To Date\''];
            break;
        case 287:
            strobj = [WtfGlobal.getLocaleText('pm.report.save.text'),WtfGlobal.getLocaleText('pm.Help.createreport')];
            break;
        case 288:
            strobj = [WtfGlobal.getLocaleText('pm.common.failure'), WtfGlobal.getLocaleText('pm.msg.292')];
            break;
     
        case 290:
            strobj = [WtfGlobal.getLocaleText('pm.common.failure'), WtfGlobal.getLocaleText('pm.msg.290')];
            break;
        case 291:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.291')];
            break;
        case 292:
            strobj = [WtfGlobal.getLocaleText('pm.common.failure'), WtfGlobal.getLocaleText('pm.msg.292')];
            break;
        case 294:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText('pm.msg.294')];
            break;
        case 295:
            strobj = [WtfGlobal.getLocaleText('pm.common.failure'), WtfGlobal.getLocaleText('pm.msg.295')];
            break;
        case 296:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.296')];
            break;
        case 297:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText('pm.msg.276')];
            break;
       case 299:
            strobj = [WtfGlobal.getLocaleText('lang.success.text'), WtfGlobal.getLocaleText('pm.msg.299')];
            break;
        case 300:
            strobj = [WtfGlobal.getLocaleText('pm.common.failure'), WtfGlobal.getLocaleText('pm.Help.connFail')];
            break;
        case 301:
            strobj = [WtfGlobal.getLocaleText('lang.error.text'), WtfGlobal.getLocaleText('pm.msg.301')];
            break;
        case 302:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'),WtfGlobal.getLocaleText('pm.msg.302')];
            break;
        case 303:
            strobj = [WtfGlobal.getLocaleText('pm.common.failure'), WtfGlobal.getLocaleText('pm.msg.303')];
            break;
        case 304:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.304')];
            break;
        case 305:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.305')];
            break;
        case 306:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.306')];
            break;
        case 307:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.307')];
            break;
        case 308:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.308')];
            break;
        case 310:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.310')];
            break;
        case 311:
            strobj = [WtfGlobal.getLocaleText('lang.success.text'),WtfGlobal.getLocaleText('pm.Help.delReport')];
            break;
        case 313:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.313')];
            break;
        case 314:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.314'), 1];
            break;
        case 315:
            strobj = [WtfGlobal.getLocaleText('pm.msg.INFORMATION'),WtfGlobal.getLocaleText('pm.msg.315')];
            break;
        case 316:
            strobj = [WtfGlobal.getLocaleText('pm.common.projectplan'), WtfGlobal.getLocaleText('pm.msg.316')];
            break;
        case 317:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.317')];
            break;
        case 318:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'),WtfGlobal.getLocaleText('pm.msg.318')];
            break;
        case 319:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.319')];
            break;
        case 320:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.359')];
            break;
        case 321:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.msg.360')];
            break;

        default:
            strobj = [choice[0], choice[1]];
            break;
    }
	var iconType = Wtf.MessageBox.INFO;
    if(type == 0)
	    iconType = Wtf.MessageBox.INFO;
    if(type == 1)
	    iconType = Wtf.MessageBox.ERROR;
    else if(type == 2)
        iconType = Wtf.MessageBox.WARNING;
    else if(type == 3)
        iconType = Wtf.MessageBox.INFO;

    Wtf.MessageBox.show({
        title: strobj[0],
        msg: strobj[1],
        buttons: Wtf.MessageBox.OK,
        animEl: 'mb9',
        icon: iconType
    });
}

/*function getColumnHeader(columnModel, st){
    var cH = [];
    for (var i = st; i < columnModel.getColumnCount(); i++) {
        cH.push(columnModel.getColumnHeader(i));
    }
    return cH.join(',');
}

function getColumnDataIndex(columnModel, st){
    var cDI = [];
    for (var i = st; i < columnModel.getColumnCount(); i++) {
        cDI.push(columnModel.getDataIndex(i));
    }
    return cDI.join(',');
}

function getColumnWidth(columnModel, st){
    var cW = [];
    var totalWidth = columnModel.getTotalWidth(false);
    for (var i = st; i < columnModel.getColumnCount(); i++) {
        cW.push((columnModel.getColumnWidth(i) / totalWidth) * 100);
    }
    return cW.join(',');
}

function dataStoreconverter(dstore, cname){
    var aa = [];
    var colnm = cname.split(",");
    dstore.each(function(record){
        for (var j = 0; j < colnm.length; j++) {
            if (record.fields.items[j].mapping == null) {
                record.set(colnm[j], "");
            }
        }
        aa.push(Wtf.encode(record.data));
    });
    return String.format("[{0}]", aa.join(','));
}

function getActiveSubTab(mainTabid){
    var subTabPanelId = tabRegister[mainTabid];
    var activetabPanel = Wtf.getCmp(subTabPanelId);
    if (!activetabPanel) {
        return Wtf.getCmp(mainTabid);
    }
    return activetabPanel.getActiveTab();
    
}*/

function calLoadControl(pid){
    if (!Wtf.getCmp(pid + 'Calendar')) {
        var datePicker = new Wtf.DatePicker({
            id: pid + 'calctrlcalpopup1',
            cls: 'datepicker',
            autoWidth: true,
            border: false,
            defaults: {
                autoHeight: true,
                autoScroll: true
            },
            renderTo: 'calendarcontainer'
        });
        var calTree = new Wtf.CalendarTree({
            id: pid + "Calendar",
            url: "../../jspfiles/cal/caltree.jsp",
            ownerid: {
                type: 0,
                userid: loginid
            },
            parentid: pid,
            renderTo: "calendartree-container",
            calControl: null,
            parentTabId: pid,
            datePicker: datePicker
        });
    }
}


function toggleMainCal(state){
    //FIXME: mainpanel id hardcoded "as"
    var mainDatePicker = Wtf.getCmp('ascalctrlcalpopup1');
    var mainCalTree = Wtf.getCmp('asCalendar');
    if (state) {
        if (mainDatePicker) {
            mainDatePicker.show();
        }
        if (mainCalTree) {
            mainCalTree.show();
            mainCalTree.getSelectionModel().clearSelections();
        }
    }
    else {
        if (mainDatePicker) {
            mainDatePicker.hide();
        }
        if (mainCalTree) {
            mainCalTree.hide();
        }
    }
}

Wtf.req = {
    base: "../../jspfiles/",
    adm: "../../jspfiles/admin/",
    cal: "../../jspfiles/cal/",
    doc: "../../jspfiles/docs/",
    lms: "../../jspfiles/lms/",
    prt: "../../jspfiles/portal/",
    prf: "../../jspfiles/profile/",
    prj: "../../jspfiles/project/",
    crm: "../../scripts/crm/json/",
    widget: "../../jspfiles/"
};

Wtf.common.Uid = function(_userid, _type){
    this.userid = _userid;
    this.type = _type;
}
function getHelpContent() {
    var str ="<div style='float:left;height:100%;width:97%;position:relative;'>"
    +"<div style='font-size:12px;float:left;margin:4px 0px 10px 10px;width:100%;position:relative;'>"+
    "<ul style='padding-left:15px;'><li type='disc'>Access some useful tips to <b>Get you started</b>, especially if you are a new user. Click on the <b>WtfGlobal.getLocaleText('pm.help.text')</b> link near the search bar while in any module to view tips.</li>"+
    "<br/><li type='disc'>Unsure about how to use a button or link? Relax! Just point the mouse on it. Count one Mississippi, two...  and a message explaining the button or link will appear (as shown below). No need to search anywhere else! </li> <br/><center><img src='../../images/quickhelp.gif'></center><br/>"+
    "<br/><li type='disc'><a class='guideme' href='mailto:support@deskera.com' target='_blank'>Contact Support</a>. We are here to help you!</li><br/><li type='disc'>Discuss on <a id='forum' href='http://forum.deskera.com/' target='_blank' class='guideme'>Forum</a>. Find useful tips and tricks on <a id='blog' href='http://blog.deskera.com/' target='_blank' class='guideme'>Blog</a></li></ul>"+
    "</div>"+"</div>" ;
    return str;
}
function guestResponse(eid, userid, response){
    Wtf.Ajax.request({
        url: Wtf.req.cal + 'guestStatus.jsp',
        method: 'GET',
        params: ({
            eid: eid,
            userid: userid,
            response: response
        }),
        scope: this,
        success: function(result, req){
            var nodeobj = eval("(" + result.responseText.trim() + ")");
            if (nodeobj.success == "Invalid")
                showMsgBox(150, 1);
                //Wtf.Msg.alert('Status', 'Invalid operation ');
            else 
                if (nodeobj.success == "deleted") 
                    showMsgBox(151, 1);
                    //Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Event deleted');
                else 
                    if (nodeobj.success == "true") 
                        showMsgBox(152, 0);
                        //Wtf.Msg.alert('Status', 'Event shared successfully');
                    else 
                        if (nodeobj.success == "false") 
                            showMsgBox(93, 1);  
                            //Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Error in sharing event');
        },
        failure: function(){
            msgBoxShow(4, 1);
            //Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Error occurred while connecting to the server');
        }
    });
}

function arrayUniq(b){
    var a = [], i, l = b.length;
    for (i = 0; i < l; i++) {
        if (a.indexOf(b[i]) == -1) {
            a.push(b[i]);
        }
    }
    return a;
}

function setDownloadUrl(u){
    //setDldUrl(Wtf.req.doc + "file-releated/filecontent/fdownload.jsp?url=" + u + "&dtype=attachment");
    setDldUrl("../../fdownload.jsp?url=" + u + "&dtype=attachment");
}
function setDownloadUrlZip(u){   // this function handler request to download files in zipped format 
    //setDldUrl(Wtf.req.doc + "file-releated/filecontent/fdownload.jsp?url=" + u + "&dtype=attachment");
    setDldUrl("../../ZipFileServlet.jsp?url=" + u + "&dtype=attachment");
}   

function setDldUrl(u){
    document.getElementById('downloadframe').src = u;
}

//function EnableDisable(userpermcode, permcode){
//    if (userpermcode && permcode) {
//        if ((userpermcode & permcode) == permcode) {
//            return false;
//        }
//    }
//    return true;
//}

Wtf.Perm = {};
Wtf.UPerm = {};
Wtf.subscription = {};
Wtf.modules = {};
Wtf.featuresView = {};

Wtf.getSep = function(){
    return (Wtf.isIE ? 'javascript:void(0)' : '#')
};

    var calContacts_Record = Wtf.data.Record.create([{
        name: 'userid'
    }, {
        name: 'username'
    }, {
        name: 'emailid'
    }, {
        name: 'fullname'
    }, {
        name: 'userstatus'
    }, {
        name: 'messagetext'
    }, {
        name: 'image'
    }]);
    
    var calContacts_Reader = new Wtf.data.JsonReader({
        root: "data"
    }, calContacts_Record);
    
    var calContacts_Store = new Wtf.data.Store({
        proxy: new Wtf.data.HttpProxy({
            url: Wtf.req.prt + "messenger/getFriendListDetails.jsp"
        }),
        reader: calContacts_Reader
    });
//    calContacts_Store.load({ params: {
//                login: loginid
//            }});

/*function unsubscribe(){
    alert(WtfGlobal.getLocaleText('lang.unsubscribe.text'));
}

function subscribe(){
    alert(WtfGlobal.getLocaleText('lang.subscribe.text'));
}*/

function requestMore(objid) {
    var obj = Wtf.getCmp(objid);
    obj.requestMore();
}

//todo for auto complete
//Wtf.userStore = new Wtf.data.JsonStore({
//    url: "../../admin.jsp?mode=21&action=0",
//    root:'data', 
//    fields : ['id','name']
//});
// Wtf.userStore.load();
Wtf.countryStore = new Wtf.data.JsonStore({
    url: "../../admin.jsp?mode=12&action=0",
    root:'data', 
    fields : ['id','name']
});
Wtf.timezoneStore = new Wtf.data.JsonStore({
    url:"../../admin.jsp?mode=13&action=0",
    root:'data', 
    fields : ['id','name']
});
Wtf.dateFormatStore = new Wtf.data.JsonStore({
    url:"../../admin.jsp?mode=20&action=0",
    root:'data',
    fields : ['id','name','dateformat', 'seppos']
});

/*function setPP(){
    var pp = new Wtf.personalPref({
    });    
    pp.show();
}*/
function getTimeZone(){
    if(!Wtf.StoreMgr.containsKey("timezone")){
        Wtf.timezoneStore.load();
        Wtf.timezoneStore.on("load", function() {
            Wtf.StoreMgr.add("timezone", Wtf.timezoneStore);
            getTZ();
        });
    }
}

function getTZ(){
    for(var i = 0; i < Wtf.timezoneStore.data.length; i++){
        if(Wtf.timezoneStore.getAt(i).data.id == Wtf.timezoneName){
            Wtf.timezoneName = Wtf.timezoneStore.getAt(i).data.name;
        }
    }
}

function getCountryName(){
    if(!Wtf.StoreMgr.containsKey("country")){
        Wtf.countryStore.load();
        Wtf.countryStore.on("load", function() {
           Wtf.StoreMgr.add("country",Wtf.countryStore);
            getCN();
        });
    }
}
function getCN(){
    for(var i = 0; i < Wtf.countryStore.data.length; i++){
        if(Wtf.countryStore.getAt(i).data.id == Wtf.countryName){
            Wtf.countryName = Wtf.countryStore.getAt(i).data.name;
        }
    }
}
function deleteHoliday(obj, admin){
    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText('pm.msg.326'), function(btn){
        if(btn == "yes")
            Wtf.getCmp(admin).deleteHoliday(obj.id.substring(4));
        },
    this);
}

function onNotifyChange(obj, admin,inputid){
    Wtf.getCmp(admin).onNotifyChange(inputid);
}

function cancelSubsci(obj, winobj){
    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), 
       'Are you sure you want to cancel subscription?<br>This cancellation will be effective from next billing cycle.', 
       function(btn){
        if(btn == "yes"){
            Wtf.getCmp(winobj).cancelSubsci(obj.id.substring(4));
        }
       },
      this);    
}

function subscriprojval(obj) {
    var projcnt = document.getElementById(obj.id).value;
    if(projcnt.match(/^[1-9][0-9]*$/)!=projcnt) {
        document.getElementById(obj.id).value = '5';
        projcnt = '5';
    }    
    var amtObj = document.getElementById('subscramt');
    var amt = projcnt*(document.getElementById("deskerarate").value)
    amtObj.value = "$"+amt+".00";
    document.getElementById('subscria3').value = amt;
    document.getElementById("subscricustom").value = companyid+"_"+projcnt;
    document.getElementById("subscritemname").value = "Subscription for "+projcnt+" Deskera project"+(parseInt(projcnt) > 1 ? "s" : "");
}
function cancelHoliday(){
    Wtf.get("addHoliday").dom.style.display = 'none';
}
function addHoliday(admin){
    Wtf.getCmp(admin).addHoliday();
}
//Wtf.getDateFormat = function() {
//    return Wtf.pref.DateFormat;
//}
//
//Wtf.validateEmail = function(value){
//    return Wtf.ValidateMailPatt.test(value);
//}
//Wtf.renderEmailTo = function(value,p,record){
//     return "<div class='mailTo'><a href=mailto:"+value+">"+value+"</a></div>";
//}
//Wtf.renderContactToSkype = function(value,p,record){
//     return "<div class='mailTo'><a href=skype:"+value+"?call>"+value+"</a></div>";
//}
//Wtf.validateUserid = function(value){
//    return Wtf.ValidateUserid.test(value);
//}
//Wtf.validateUserName = function(value){
//    return Wtf.ValidateUserName.test(value.trim());
//}
//function getInstrMsg(msg){
//    return "<span style='font-size:10px !important;color:gray !important;'>"+msg+"</span>"
//}
function navigateSubtab(path, subTabType, parentid){
    var parentObj = Wtf.getCmp("subtabpanelcomprojectTabs_" + parentid);
    var projCont = Wtf.getCmp("subtabproj" + parentid);
    var subtab = "";
    switch(path){
        case 'p':
            switch(subTabType){
                case 'plan':
//                    subtab = parentid + "_projplanPane";
                    projCont.openprojectplan();
                    break;
                case 'todo':
//                    subtab = 'list_conainer' + parentid;
                    projCont.opentodotab();
                    break;
                case 'cal':
//                    subtab = parentid + "calctrl";
                    projCont.openteamcaltab();
                    break;
                case 'req':
                    projCont.openadmintab();
                    break;
            }
            break;
    }
}

//function URLDecode(str){
//     str=str.replace(new RegExp('\\+','g'),' ');
//     return unescape(str);
//}

function subscribeModule(contid){
    var _cont = Wtf.get(contid);
    var status = 1;
    var subTxt = "subscription";
    if(_cont.hasClass("SUB")){
        _cont.removeClass("SUB");
        status = 0;
        subTxt = "unsubscription";
    } else
        _cont.removeClass("UNS");
    _cont.dom.firstChild.nextSibling.innerHTML = WtfGlobal.getLocaleText('pm.admin.company.modulesub.requestpending');
    _cont.dom.lastChild.innerHTML = "";
    var mod = contid.split("_")[1];
    Wtf.Ajax.requestEx({
        url: "../../admin.jsp",
        params: {
            status: status,
            action: 1,
            module: mod,
            mode: 3,
            emode: 1,
            userid: loginid
        }
    }, this, function(){
        msgBoxShow([WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText({key:"pm.admin.company.subscription.msg", params: [subTxt, mod]})], 0);
        bHasChanged = true;
        if(refreshDash.join().indexOf("all") == -1)
            refreshDash[refreshDash.length] = 'all';
    }, function(){
        msgBoxShow(238, 1);
    });
}

function subscribeSubModule(contid) {
    var _cont = Wtf.get(contid);
    var status = 1;
    var subTxt = "subscription";
    if(_cont.hasClass("SUB")){
        _cont.removeClass("SUB");
        status = 0;
        subTxt = "unsubscription";
    } else
        _cont.removeClass("UNS");
    _cont.dom.firstChild.nextSibling.innerHTML = WtfGlobal.getLocaleText('pm.admin.company.modulesub.requestpending');
    _cont.dom.lastChild.innerHTML = "";
    var mod = contid.split("_")[1];
    Wtf.Ajax.requestEx({
        url: "../../admin.jsp",
        params: {
            status: status,
            action: 1,
            module: mod,
            mode: 3,
            emode: 2,
            userid: loginid
        }
    }, this, function(){
        msgBoxShow([WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText({key:"pm.admin.company.subscription.msg", params: [subTxt, mod]})], 0);
        bHasChanged = true;
        if(refreshDash.join().indexOf("all") == -1)
            refreshDash[refreshDash.length] = 'all';
    }, function(){
        msgBoxShow(238, 1);
    });
}

function subscribeModuleErrorMsg() {
    msgBoxShow(296, 1);
}

/*
function unsubscribeModule(contid){
    var _cont = Wtf.get(contid);
    _cont.removeClass("UNS");
    _cont.dom.firstChild.nextSibling.innerHTML = "Waiting";
    _cont.dom.lastChild.innerHTML = "";
}*/

function featureViewUpdate(contid, contkey) {
    var _feature = Wtf.get(contid);
    var view = 1;
    var featurename = contid.split("_")[1];
    var id = "featureDiv_" + featurename;

    if(_feature.hasClass("SUB")){
        _feature.removeClass("SUB");
        _feature.addClass("UNS");
        view = 0;

    } else {
        _feature.removeClass("UNS");
        _feature.addClass("SUB");
    }

    if(view == 0) {
        _feature.dom.innerHTML = " <div class='subscriptionDiv'> " + WtfGlobal.getLocaleText("pm.featureslist."+contkey) + " </div> " +
            " <a href='#' onclick=\"featureViewUpdate('" + id + "', '"+ contkey +"')\" class='sublink'>"+WtfGlobal.getLocaleText('Show')+"</a> " +
            " <div class = 'statusDiv'>"+WtfGlobal.getLocaleText('Hide')+"</div> ";

    } else if(view == 1) {
        _feature.dom.innerHTML = " <div class='subscriptionDiv'> " + WtfGlobal.getLocaleText("pm.featureslist."+contkey) + " </div> " +
            " <div class = 'statusDiv'>"+WtfGlobal.getLocaleText('Show')+"</div> " +
            " <a href='#' onclick=\"featureViewUpdate('" + id + "', '"+ contkey +"')\" class='sublink'>"+WtfGlobal.getLocaleText('Hide')+"</a> ";
    }

    Wtf.Ajax.requestEx({
        url: "../../admin.jsp",
        params: {
            view: view,
            action: 1,
            featurename: featurename,
            mode: 3,
            emode: 3
        }
    }, this, function() {
        msgBoxShow([WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText({key:"pm.admin.company.subscription.msg", params: [(view == 0 ? WtfGlobal.getLocaleText("Hide") : WtfGlobal.getLocaleText("Show")), WtfGlobal.getLocaleText("pm.featureslist."+contkey)]})], 0);
        var task = new Wtf.util.DelayedTask(function(){
            location.reload(true);
        }, this);
        task.delay(2000);
    }, function() {
        msgBoxShow(227, 1);
    });
}

function printInvoice(invoicenum,subid,flg,refnum){
    if(flg == 1)
        setDldUrl('../../PrintInvoice.jsp?&invoicenum=' + invoicenum + '&invoicedate='+refnum+'&subid='+subid+'&flg='+flg);
    else if(flg == 2)
        setDldUrl('../../PrintInvoice.jsp?&rnum=' + invoicenum + '&subid='+subid+'&flg='+flg+'&invoicenum='+refnum);// here invoicenum is receiptnum
}
Wtf.isIE8 = function(){
    if(Wtf.isIE && !Wtf.isIE6 && !Wtf.isIE7){
        return true;
    }
    return false;
}

function check(r){
    var ua = navigator.userAgent.toLowerCase();
    return r.test(ua);
}

Wtf.isWebKit = check(/webkit/);

function getMyPlanPermission(projectid, seq){
    var perm, connstat, newPerm, newPermObj;
    if(seq){
        perm = projects[seq].permissions.data[0].planpermission;
        connstat = projects[seq].permissions.data[0].connstatus;
        newPerm = projects[seq].permissions;
    } else {
        for(var c = 0; c < projects.length; c++){
            if(projects[c].id == projectid){
                perm = projects[c].permissions.data[0].planpermission;
                connstat = projects[c].permissions.data[0].connstatus;
                newPerm = projects[c].permissions;
                break;
            }
        }
    }
    perm = Wtf.num(perm, -1);
    var perms = new Object();
    perms = Wtf.util.clone(newPerm);
    newPermObj = perms.data[0];
    if(connstat != 4){
        switch(perm){
            case 4:
                break;
            case 2:
                newPermObj.archived = true;
                break;
            case 10:
                newPermObj.archived = true;
                break;
            case 16:
                if(Wtf.subscription.proj.subModule.usrtk)
                    newPermObj.connstatus = 6;
                else{
                    newPermObj.planpermission = 2;
                    newPermObj.archived = true;
                }
                break;
            case 8:
                if(Wtf.subscription.proj.subModule.usrtk)
                    newPermObj.connstatus = 7;
                else{
                    newPermObj.planpermission = 2;
                }
                newPermObj.archived = true;
                break;
            case 18:
                if(Wtf.subscription.proj.subModule.usrtk)
                    newPermObj.connstatus = 8;
                else{
                    newPermObj.planpermission = 2;
                    newPermObj.archived = true;
                }
                break;
        }
    }
    return perms;
}

function hideMainLoadMask(){
    if(!mainPanel.loadMask.hidden){
        mainPanel.loadMask.hide();
        mainPanel.loadMask.msg = WtfGlobal.getLocaleText("pm.loading.text")+'...';
    }
}

function showMainLoadMask(msg){
    var mask = [];
    if(document.getElementsByClassName){ // for firefox all, safari 3.1 above, opera 9.1 above, chrome 3 above
        mask = document.getElementsByClassName('wtf-el-mask');
    } else { // fix for IE
        var ele = document.getElementsByTagName('div');
        for(var i = 0; i<ele.length; i++){
            if(ele[i].className == 'wtf-el-mask'){
                mask[0] = ele[i];
            }
        }
    }
    if(mask){
        if(mask.length == 0)
            var show = true;
        else if(mask[0].style.display == 'none')
            show = true;
        else
            show = false;
    } else {
        show = true;
    }

    mainPanel.loadMask.msg = msg;
    mainPanel.loadMask.show();
  
}

function focusOn(id){
    var el = Wtf.getCmp(id);
    if(el){
        el.focus(false, 20);
        el.clearInvalid();
    }
}

function removeTabsAndRefreshDashoard(tabs, refresh){
    var tabPanel = Wtf.getCmp('as');
    if(typeof tabs == 'object'){
        if(!Wtf.isEmpty(tabs[0])){
            for(var i = 0; i<tabs.length; i++){
                var temp = tabs[i];
                if(temp)
                    tabPanel.remove(temp, true);
            }
        } else {
            temp = Wtf.getCmp(tabs)
            if(temp)
                tabPanel.remove(temp, true);
        }
    } else if(typeof tabs == 'string'){
        temp = Wtf.getCmp(tabs)
        if(temp)
            tabPanel.remove(temp, true);
    }
    if(refresh)
        tabPanel.setActiveTab('tabdashboard');
}

function getSizedImagePath(path, size){
    if (path.match("/store")) {
        var idx = path.lastIndexOf("_");
        var sizeIdx = path.lastIndexOf("_200");
        var fpath = path.substr(0, idx);
        var idxDot = path.lastIndexOf(".");
        var ext = path.substr(idxDot);
        if(idx != -1){
            if(sizeIdx !== -1)
                path = fpath+"_"+size+""+ext;
        }
    }
    return path;
}

function startPermissionBot(){
    dojo.cometd.subscribe("/useractivities/"+loginid,this,"onUserActivitiesChange");
}

 function stopPermissionBot() {
    dojo.cometd.unsubscribe("/useractivities/"+loginid);
}

function onUserActivitiesChange(msg){
    var module = msg.data.module;
    if(module == "mail"){
        if(Wtf.getCmp('pm_drag'))
            Wtf.getCmp('pm_drag').callRequest();
    } else {
        var name = msg.data.username;
        var key = msg.data.msg;
        var params=eval(msg.data.params);
        if(msg.data.success == "true"){
            if(module == "userpermission"){
                msgBoxShow([WtfGlobal.getLocaleText('msg.permission.changed.title'), WtfGlobal.getLocaleText({key:key,params:[name]})+" <br/><b>"+WtfGlobal.getLocaleText('pm.msg.publish.refresh')+"</b>"], 0);
            }else if(module == "projcalender"){
                msgBoxShow([WtfGlobal.getLocaleText('pm.common.projectcalender'), WtfGlobal.getLocaleText({key:key,params:[name,msg.data.projname]})+". <br/><b>"+WtfGlobal.getLocaleText('pm.msg.publish.refresh')+" </b>"], 0);
            }else if(module == "projmoderator"){
                msgBoxShow([WtfGlobal.getLocaleText('pm.module.projectsettings'), WtfGlobal.getLocaleText({key:key,params:params})+"<br/><b>"+WtfGlobal.getLocaleText('pm.msg.publish.refresh')+"</b>"], 0);
            }else if(module=='featureview'){
                msgBoxShow([WtfGlobal.getLocaleText('pm.msg.SUCCESS'),WtfGlobal.getLocaleText({key:'pm.msg.publish', params:key})], 0);
            }
            task = new Wtf.util.DelayedTask(function(){
                location.reload(true);
            }, this);
            task.delay(10000);
        }
    }
}

// This function removes non-numeric characters from a formatted number
function stripNonNumeric(str){
    str += '';
    var rgx = /^\d|\.|-$/;
    var out = '';
    for( var i = 0; i < str.length; i++ ){
        if( rgx.test( str.charAt(i) ) ){
            if( !( ( str.charAt(i) == '.' && out.indexOf( '.' ) != -1 ) ||
                ( str.charAt(i) == '-' && out.length != 0 ) ) ){
                out += str.charAt(i);
            }
        }
    }
    return out;
}

function numberFormat(nStr){
    nStr += '';
    var x = nStr.split('.');
    var x1 = x[0];
    var x2 = x.length > 1 ? '.' + x[1] : '';
    var rgx = /(\d+)(\d{3})/;
    while (rgx.test(x1))
        x1 = x1.replace(rgx, '$1' + ',' + '$2');
    return x1 + x2;
}
