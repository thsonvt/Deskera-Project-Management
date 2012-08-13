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
Wtf.BLANK_IMAGE_URL = "../../lib/resources/images/default/s.gif";
Wtf.fixHeight = (Wtf.isIE8() || Wtf.isWebKit || Wtf.isIE7) ? 32 : 30;
Wtf.namespace('Wtf', 'Wtf.cal', 'Wtf.ux');

Wtf.calReq = {
    cal: Wtf.req.cal
};

Wtf.getCalDateFormat = function() {
    return WtfGlobal.getDateFormat();
}

function getInstrMsg(msg) {
    return "<span style='font-size:10px !important;color:gray !important;'>"+msg+"</span>";
}

function calMsgBoxShow(choice, type) {
    var strobj = [];
    switch (choice) {
        case 4:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.calmsg.4')];
            break;
        case 56:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'),WtfGlobal.getLocaleText('pm.calmsg.56')];
            break;
        case 113:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.calmsg.113')];
            break;
        case 114:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.calmsg.114')];
            break;
        case 115:
            strobj = ["Invalid Event", WtfGlobal.getLocaleText('pm.calmsg.115')];
            break;
        case 116:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.calmsg.116')];
            break;
        case 117:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.calmsg.117')];
            break;
        case 118:
            strobj = [WtfGlobal.getLocaleText('pm.msg.INFORMATION'), WtfGlobal.getLocaleText('pm.calmsg.118')];
            break;
        case 119:
            strobj = [WtfGlobal.getLocaleText('pm.msg.INFORMATION'), WtfGlobal.getLocaleText('pm.calmsg.119')];
            break;
        case 120:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.calmsg.120')];
            break;
        case 121:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.calmsg.121')];
            break;
        case 122:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.calmsg.122')];
            break;
        case 123:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.calmsg.123')];
            break;
        case 124:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.calmsg.124')];
            break;
        case 125:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.calmsg.125')];
            break;
        case 126:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.calmsg.126')];
            break;
        case 127:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.calmsg.127')];
            break;
        case 128:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.calmsg.128')];
            break;
        case 129:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText('pm.calmsg.129')];
            break;
        case 130:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.calmsg.130')];
            break;
        case 136:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText('pm.calmsg.136')];
            break;
        case 137:
            strobj = [WtfGlobal.getLocaleText('pm.msg.SUCCESS'), WtfGlobal.getLocaleText('pm.calmsg.137')];
            break;
        case 138:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.calmsg.138')];
            break;
        case 153:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.calmsg.153')];
            break;
        case 154:
            strobj = ['Status', 'Invalid operation '];
            break;
        case 155:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.calmsg.155')];
            break;
        case 156:
            strobj = ['Status', WtfGlobal.getLocaleText('pm.calmsg.156')];
            break;
        case 157:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.calmsg.157')];
            break;
        case 158:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.calmsg.158')];
            break;
        case 159:
            strobj = [WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText('pm.calmsg.159')];
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

function guestResponse(eid, userid, response){
    Wtf.Ajax.request({
        url: Wtf.calReq.cal + 'guestStatus.jsp',
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
                calShowMsgBox(154, 1);
            else
                if (nodeobj.success == "deleted")
                    calShowMsgBox(155, 1);
                else
                    if (nodeobj.success == "true")
                        calShowMsgBox(156, 0);
                    else
                        if (nodeobj.success == "false")
                            calShowMsgBox(157, 1);
        },
        failure: function(){
            calMsgBoxShow(4, 1);
        }
    });
}
