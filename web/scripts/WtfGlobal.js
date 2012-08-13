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
WtfGlobal = {
    sqlToJsDate : function(dt){
        var r = null;
        if (dt) {
            var tempdt;
            var t=dt.lastIndexOf('.');
            if(t==-1){
                tempdt=dt;
            }
            else{
                tempdt = dt.substring(0, t);
            }
            var dateNTime1 = tempdt.split(" ");
            var date1 = dateNTime1[0].split("-");
            if(dateNTime1[1] !== undefined){
                var time1 = dateNTime1[1].split(":");
                r = new Date(date1[1] + "/" + date1[2] + "/" + date1[0] + " " + time1[0] + ":" + time1[1] + ":" + time1[2]);
            } else {
                r = new Date(date1[1] + "/" + date1[2] + "/" + date1[0]);
            }
        }
        return r;
    },

    getCookie: function(c_name){
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
    },

    nameRenderer: function(value){
        var resultval = value.substr(0, 1);
        var patt1 = new RegExp("^[a-zA-Z]");
        if (patt1.test(resultval)) {
            return resultval.toUpperCase();
        }
        else
            return WtfGlobal.getLocaleText('lang.others.text');
    },

    sizeRenderer: function(value){
        var sizeinKB = value
        if (sizeinKB >= 1 && sizeinKB < 1024) {
            text = WtfGlobal.getLocaleText('lang.small.text');
        } else if (sizeinKB > 1024 && sizeinKB < 102400) {
            text = WtfGlobal.getLocaleText('lang.medium.text');
        } else if (sizeinKB > 102400 && sizeinKB < 1048576) {
            text = WtfGlobal.getLocaleText('lang.large.text');
        } else {
            text = WtfGlobal.getLocaleText('lang.gigantic.text');
        }
        return text;
    },

    getDateFormat: function() {
        return Wtf.pref.DateFormat;
    },

    dateFieldRenderer: function(value){
        var text = "";
        if (value && typeof value !== 'object'){
             var temp = Date.parseDate(value, 'Y-m-d H:i:s');
            if(!temp)
                temp = Date.parseDate(value, WtfGlobal.getDateFormat());
            if(temp && typeof temp == 'object')
                value = temp;
            else
                return value;
        }
        var dt = new Date();
        if(dt.getDate() == value.getDate() && value.getMonth() == dt.getMonth() && value.getYear() == dt.getYear())
            text = WtfGlobal.getLocaleText('pm.cal.today.text');
        else if ((value.getMonth() == dt.getMonth()) && (value.getYear() == dt.getYear())) {
            if(value > (new Date().add(Date.DAY, -7)) && value < dt)
                text = WtfGlobal.getLocaleText('pm.cal.week.this');
            else if (value <= (new Date().add(Date.DAY, -7)) && value > (new Date().add(Date.DAY, -14)))
                text = WtfGlobal.getLocaleText('pm.cal.week.last');
            else
                text = WtfGlobal.getLocaleText('pm.cal.month.this');
        } else if ((value.getMonth() == ((dt.getMonth() == 1 ? 12 : dt.getMonth()) - 1)) && (value.getYear() == dt.getYear()))
            text = WtfGlobal.getLocaleText('pm.cal.month.last');
        else if (value.getYear() == dt.getYear())
            text = WtfGlobal.getLocaleText('pm.cal.year.this');
        else if ((value.getYear() == (dt.getYear() - 1)))
            text = WtfGlobal.getLocaleText('pm.cal.year.last');
        else
            text = WtfGlobal.getLocaleText('pm.cal.older.text');
        return text;
    },

    dateRenderer: function(value){
        var text = "";
        if (value) {
            var days, hrs, min;
            var dt = new Date();
            var timediff = dt.getTime() - value.getTime();
            if(timediff > 0){
                days = parseInt(timediff/(1000 * 3600 * 24));
                if(days > 0){
                    if(days <= 7){
                        text = (days == 1) ? days + ' day ago': days + ' days ago ';
                    } else {
                        text = value.format(WtfGlobal.getDateFormat());
                    }
                } else {
                    hrs = 0;// = parseInt(timediff/(1000 * 3600));
                    min = parseInt(timediff /(60 * 1000));
                    if(min > 60){
                        hrs = hrs + parseInt(min / 60);
                        min = min - parseInt(min - (min % 60));
                    }
                    if(hrs > 0)
                        text = (hrs > 1) ? hrs + " hours " : hrs + " hour ";
                    text += (min == 1) ? min + " minute ago " : min + " minutes ago";
                }
            } else {
                days = parseInt(Math.abs(timediff)/(1000 * 3600 * 24));
                if(days > 0){
                    if(days <= 7){
                        text = (days == 1) ? 'in' + days + ' day': 'in' + days + ' days ';
                    } else {
                        text = value.format(WtfGlobal.getDateFormat());
                    }
                } else {
                    hrs = 0;// = parseInt(timediff/(1000 * 3600));
                    min = parseInt(Math.abs(timediff) /(60 * 1000));
                    if(min > 60){
                        hrs = hrs + parseInt(min / 60);
                        min = min - parseInt(min - (min % 60));
                    }
                    text = 'in ';
                    if(hrs > 0)
                        text = (hrs > 1) ? hrs + " hours " : hrs + " hour ";
                    text += (min == 1) ? min + " minute " : min + " minutes";
                }
            }
        }
        return text;
    },

    getDateFromString: function(dt){
        if(typeof dt !== 'object'){
            var d = Date.parseDate(dt, 'Y-m-j H:i:s');
            if(typeof d == 'undefined' || !typeof d == 'object')
                d = Date.parseDate(dt, WtfGlobal.getDateFormat());
            if(typeof d == 'undefined' || !typeof d == 'object'){
                d = Date.parse(dt);
                d = new Date(d);
            }
        } else
            d = dt;
        return d;
    },

    dateRendererForServerDate: function(value){
        if(typeof value !== 'object'){
            var temp = Date.parseDate(value, 'Y-m-j H:i:s')
            if(temp)
                return temp.format(WtfGlobal.getDateFormat());
            else
                return value;
        } else {
            return value;
        }
    },

    dateRendererForOnlyDate: function(value){
        var temp = Date.parseDate(value, 'Y-m-j H:i:s')
        if(temp)
            return temp.format(WtfGlobal.getOnlyDateFormat());
        else
            return value;
    },

    convertDate: function(dt, offset) {

        if(typeof dt !== 'object'){
            var d = Date.parseDate(dt, 'Y-m-j H:i:s');
            if(typeof d == 'undefined' || !typeof d == 'object')
                d = Date.parseDate(dt, WtfGlobal.getDateFormat());
            if(typeof d == 'undefined' || !typeof d == 'object'){
                d = Date.parse(dt);
                d = new Date(d);
            }
        } else
            d = dt;
        var utc = d.getTime();
        //add milisec * offset in the given time and get a date
        var nd = new Date(utc + (3600000*offset));
        //if LocaleString gives correct time then do it
        if(new Date(nd.toLocaleString()) == new Date(nd)){
            return nd.toLocaleString()
        } else
            return nd;

    },

    getNotificationSubscription: function(){
        return (Wtf.pref.Notification == "1") ? true : false;
    },

    getCompanyNotificationSubscription: function(){
        return Wtf.pref.CompanyNotification;
    },

    getCheckListModuleStatus: function(){
        return Wtf.pref.CheckListModule;
    },

    getDocAccessStatus: function(){
        return Wtf.pref.DocAccess;
    },

    getSeperatorPos: function() {
        return Wtf.pref.DateFormatSeparatorPosition;
    },

    getOnlyDateFormat: function() {
        var pos=WtfGlobal.getSeperatorPos();
        var fmt=WtfGlobal.getDateFormat();
        if(pos<=0)
            return "Y-m-d";
        return fmt.substring(0,pos);
    },

    getOnlyTimeFormat: function() {
        var pos=WtfGlobal.getSeperatorPos();
        var fmt=WtfGlobal.getDateFormat();
        if(pos>=fmt.length)
            return "H:i:s";
        return fmt.substring(pos);
    },

    userPrefDateRenderer: function(v) {
        if(!v) return v;
        return v.format(WtfGlobal.getDateFormat());
    },

    dateTimeRenderer: function(v) {
        if(!v) return v;
        var vd = new Date(v);
        return vd.format(WtfGlobal.getDateFormat());
    },

    onlyTimeRenderer: function(v) {
        if(!v) return v;
        return v.format(WtfGlobal.getOnlyTimeFormat());
    },

    onlyDateRenderer: function(v) {
        if(!v) return v;
        return v.format(WtfGlobal.getOnlyDateFormat());
    },

    convertToGenericDate:function(value){
        if(!value) return "";
        return value.format("M d, Y h:i:s A");
    },

    convertToOnlyDate:function(value){
        if(!value) return value;
        return value.format("Y-m-d");
    },

    permissionConRenderer: function(value, rec){
        var text = value.toLowerCase();
        switch (text) {
            case "everyone":
                text = WtfGlobal.getLocaleText("pm.document.everyone.text");
                break;
            case "all my connections":
                text = WtfGlobal.getLocaleText("pm.document.allconnections.text");
                break;
            case "selected connections":
                text = WtfGlobal.getLocaleText('pm.common.selectedconnection');
                break;
            default:
                text = WtfGlobal.getLocaleText('lang.private.text');
                break;
        }
        return text;
    },
    permissionProjRenderer: function(value, rec){
        var text = value.toLowerCase();
        switch (text) {
            case "all my projects":
                text = WtfGlobal.getLocaleText('pm.dashboard.widget.quicklinks.allmyprojects');
                break;
            case "selected projects":
                text = WtfGlobal.getLocaleText('pm.project.selecte.text');
                break;

            default:
                text = WtfGlobal.getLocaleText('lang.none.text');
                break;
        }
        return text;
    },

    formatDuration: function(value) {
        value = WtfGlobal.HTMLStripper(value);
        var temp = new String(value);
        var index = temp.indexOf('h')
        if (index >= 0) {
            temp = temp.substr(0, index)
            if (temp != '') {
                if (temp == '1')
                    return temp + ' hr';
                else
                    return temp + ' hrs';
            }
        } else {
            temp = new String(value);
            index = temp.indexOf('d');
            if (index != -1)
                temp = temp.substr(0, index);
            if (temp != '') {
                var idx = temp.indexOf('?');
                if (idx > 0) {
                    if (temp == '1?')
                        return temp.substr(0, idx) + ' day?';
                    else
                        return temp.substr(0, idx) + ' days?';
                }else {
                    if (temp == '1')
                        return temp + ' day';
                    else
                        return temp + ' days';
                }
            }
        }
    },

    HTMLStripper: function(val){
        var str = Wtf.util.Format.stripTags(val);
        return str.replace(/"/g, '').trim();
    },

    ScriptStripper: function(str){
        str = Wtf.util.Format.stripScripts(str);
        if (str)
            return str.replace(/"/g, '');
        else
            return str;
    },

    URLDecode: function(str){
        str=str.replace(new RegExp('\\+','g'),' ');
        return unescape(str);
    },

    validateEmail: function(value){
        return Wtf.ValidateMailPatt.test(value);
    },

    validatePhone: function(value){
        return Wtf.validatePhone.test(value);
    },

    renderEmailTo: function(value,p,record){
        return "<div class='mailTo'><a href=mailto:"+value+">"+value+"</a></div>";
    },

    validateHTField:function(value){
        return Wtf.validateHeadTitle.test(value.trim());
    },

    renderContactToSkype: function(value,p,record){
        return "<div class='mailTo'><a href=skype:"+value+"?call>"+value+"</a></div>";
    },

    validateUserid: function(value){
        return Wtf.ValidateUserid.test(value);
    },

    validateUserName: function(value){
        return Wtf.ValidateUserName.test(value.trim());
    },

    validateBaseCampFile:function(value){
        return Wtf.validateBaseCampFile.test(value);
    },

    validateCSVFile:function(value){
        return Wtf.validateCSVFile.test(value);
    },

    validateICSFile:function(value){
        return Wtf.validateICSFile.test(value);
    },

    validateMSProjectFile:function(value){
        return Wtf.validateMSProjectFile.test(value.trim());
    },

    validateImageFile:function(value){
        return Wtf.validateImageFile.test(value.trim());
    },

    getInstrMsg: function(msg){
        return "<span style='font-size:10px !important;color:gray !important;'>"+msg+"</span>"
    },
    validateURL: function(str){
        var finalStr = "";
        if(str!= null || str!= "" ){
            if(Wtf.validateURL.test(str.trim())){
                if(/<br>/i.test(str.trim()))
                    var strArr = str.split("<br>");
                else if(/\n/ig.test(str.trim()))
                    strArr = str.split("\n");
                else
                    strArr = str.split(" ");
                var  regex = "http://";
                for(var j=0;j<strArr.length;j++){
                    if(strArr[j].match(regex) != null )
                        strArr[j] =  '<a href="'+ strArr[j] +'" target="_blank">'+strArr[j] + '</a>';
                }
                for(var j=0;j<strArr.length;j++){
                    finalStr += strArr[j] + " ";
                }
                finalStr = finalStr.substr(0, finalStr.length - 1);
             }else{
                finalStr = str;
            }
        }
        return finalStr;
    },
    chkFirstRun: function(){
        return WtfGlobal.getCookie("lastlogin") == "1990-01-01 00:00:00.0";
    },

    EnableDisable: function(userpermcode, permcode){
        if (userpermcode && permcode) {
            if ((userpermcode & permcode) == permcode)
                return false;
        }
        return true;
    },

    getLocaleText:function(key, basename, def){
    	var base=window[basename||"messages"];
        var params=[].concat(key.params||[]);
        key = key.key||key;
        if(base){
            if(base[key]){
                params.splice(0, 0, base[key]);
                return String.format.apply(this,params);
            } else
                clog("Locale spacific text not found for ["+key+"]");
        } else {
            clog("Locale spacific base ("+basename+") not available");
        }
        return def||key;
    },

    loadScript: function(src, callback, scope){
        var scriptTag = document.createElement("script");
        scriptTag.type = "text/javascript";
        if(typeof callback == "function"){
            scriptTag.onreadystatechange= function () {
                if (this.readyState == 'complete')
                    callback.call(scope || this || window);
            }
            scriptTag.onload= callback.createDelegate(scope || this || window);
        }
        scriptTag.src = src;
        document.getElementsByTagName("head")[0].appendChild(scriptTag);
    },

    loadStyleSheet: function(ref){
        var styleTag = document.createElement("link");
        styleTag.setAttribute("rel", "stylesheet");
        styleTag.setAttribute("type", "text/css");
        styleTag.setAttribute("href", ref);
        document.getElementsByTagName("head")[0].appendChild(styleTag);
    },

    isDefined : function(v){
        return typeof v !== 'undefined';
    },

    isDate : function(v){
        return toString.apply(v) === '[object Date]';
    },

    isArray : function(v){
        return toString.apply(v) === '[object Array]';
    },

    isBoolean : function(v){
        return typeof v === 'boolean';
    },

    isViewableFileType: function(data){
        return (data.Type == 'Microsoft Word Document' || data.Type == 'Microsoft Excel Worksheet' || data.Type == 'PDF File' || data.Type == 'Microsoft PowerPoint Presentation'
            || data.Type == 'HTML File' || data.Name.indexOf('.odt') > 0 || data.Name.indexOf('.ods') > 0 || data.Name.indexOf('.odp') > 0
            || data.Name.indexOf('.rtf') > 0 || data.Name.indexOf('.swf') > 0 || data.Name.indexOf('.csv') > 0 || data.Name.indexOf('.tsv') > 0
            || data.Name.indexOf('.sxw') > 0 || data.Name.indexOf('.sxc') > 0 || data.Name.indexOf('.sxi') > 0 || data.Name.indexOf('.odg') > 0
            || data.Name.indexOf('.svg') > 0);
    }
};

Wtf.util.clone = function(obj){
    var str = "";
    if(obj && WtfGlobal.isDefined(obj)){
        if(typeof obj == 'object'){
            str = Wtf.encode(obj);
            return Wtf.decode(str);
        } else
            return obj;
    } else
        return obj;
}

/*  WtfHTMLEditor: Start    */
Wtf.newHTMLEditor = function(config){
    Wtf.apply(this, config);
    this.createLinkText = WtfGlobal.getLocaleText('pm.htmleditor.link.tip')+':';
    this.defaultLinkValue = 'http:/'+'/';
    this.smileyel = null;
    this.SmileyArray = [" ", ":)", ":(", ";)", ":D", ";;)", ">:D<", ":-/", ":x", ":>>", ":P", ":-*", "=((", ":-O", "X(", ":>", "B-)", ":-S", "#:-S", ">:)", ":((", ":))", ":|", "/:)", "=))", "O:-)", ":-B", "=;", ":-c", ":)]", "~X("];
    this.tpl = new Wtf.Template('<div id="{curid}smiley{count}" style="float:left; height:20px; width:20px; background: #ffffff;padding-left:4px;padding-top:4px;"  ><img id="{curid}smiley{count}" src="{url}" style="height:16px; width:16px"></img></div>');
    this.tbutton = new Wtf.Toolbar.Button({
        minWidth: 30,
        disabled:true,
        enableToggle: true,
        iconCls: 'smiley',
        tooltip:{
            title: WtfGlobal.getLocaleText('pm.chat.smileys'),
            text: WtfGlobal.getLocaleText('pm.chat.smily.add')
        }
    });
    this.eventSetFlag=false;
    this.tbutton.on("click", this.handleSmiley, this);
    this.smileyWindow = new Wtf.Window({
        width: 185,
        height: 116,
        minWidth: 200,
        plain: true,
        cls: 'replyWind',
        shadow: false,
        buttonAlign: 'center',
        draggable: false,
        header: false,
        closable  : true,
        closeAction : 'hide',
        resizable: false
    });
    this.smileyWindow.on("deactivate", this.closeSmileyWindow, this);
    Wtf.newHTMLEditor.superclass.constructor.call(this, {});
    this.on("render", this.addSmiley, this);
    this.on("activate", this.enableSmiley, this);
    this.on("hide", this.hideSmiley, this);
}

Wtf.extend(Wtf.newHTMLEditor, Wtf.form.HtmlEditor, {
    enableSmiley:function(){
        this.tbutton.enable();
    },
    hideSmiley: function(){
        //        alert("hide");
        if(this.smileyWindow !== undefined && this.smileyWindow.el !== undefined)
            this.smileyWindow.hide();
    },
    addSmiley: function(editorObj){
        editorObj.getToolbar().addSeparator();
        editorObj.getToolbar().addButton(this.tbutton);

    },
    createLink : function(){
        var url = prompt(this.createLinkText, this.defaultLinkValue);
        if(url && url != 'http:/'+'/'){
            var tmpStr = url.substring(0,7);
            if(tmpStr!='http:/'+'/')
                url = 'http:/'+'/'+url;
            this.win.focus();
            var selTxt = "";
            if(Wtf.isIE == true)
                selTxt = this.doc.selection.createRange().duplicate().text;
            else
                selTxt = this.doc.getSelection().trim();
            selTxt = selTxt =="" ? url : selTxt;
            if(this.SmileyArray.join().indexOf(selTxt)==-1) {
                this.insertAtCursor("<a href = '"+url+"' target='_blank'>"+selTxt+" </a>");
                this.deferFocus();
            } else {
                msgBoxShow(170,1);
            }
        }
    },
    //  FIXME: ravi: When certain smilies are used in a pattern, the resultant from this function does not conform to regex used to decode smilies in messenger.js.

    writeSmiley: function(e){
        var obj=e;
        this.insertAtCursor(this.SmileyArray[obj.target.id.substring(this.id.length + 6)]+" ");
        this.smileyWindow.hide();
        this.tbutton.toggle(false);
    },

    handleSmiley: function(buttonObj, e){
        if(this.tbutton.pressed) {
            this.smileyWindow.setPosition(e.getPageX(), e.getPageY());
            this.smileyWindow.show();
            if(!this.eventSetFlag){
                for (var i = 1; i < 29; i++) {
                    var divObj = {
                        url: '../../images/smiley' + i + '.gif',
                        count: i,
                        curid: this.id
                    };
                    this.tpl.append(this.smileyWindow.body, divObj);
                    this.smileyel = Wtf.get(this.id + "smiley" + i);
                    this.smileyel.on("click", this.writeSmiley, this);
                    this.eventSetFlag=true;
                }
            }
        } else {
            this.smileyWindow.hide();
            this.tbutton.toggle(false);
        }
    },

    closeSmileyWindow: function(smileyWindow){
        this.smileyWindow.hide();
        this.tbutton.toggle(false);
    }
});

Wtf.newHTMLEditor.override({
    insertAtCursor : function(text){
        if(!this.activated){
            return;
        }
        if(Wtf.isIE){
            this.win.focus();
            var doc = this.doc,
                r = doc.selection.createRange();
            if(r){
                r.pasteHTML(text);
                this.syncValue();
                this.deferFocus();
            }
        }else{
            this.win.focus();
            this.execCmd('InsertHTML', text);
            this.deferFocus();
        }
    }
});
Wtf.taskDetail = Wtf.extend(Wtf.Component, {

    tplMarkup: ['<div id="mask" style="display:block;width:100%;z-index:9000;background-color:#CCCCCC;height:100%;left:0;opacity:0.3;top:0;"></div><div id="fcue-360" class="fcue-outer" style="visibility: visible; display: block; position: absolute; z-index:2000000; left: 188px; top: 12px;">'+
    '<div class="fcue-inner">'+
    '<div class="fcue-t"></div>'+
    '<div class="fcue-content">'+
    '<div class="bd">'+
    '<a onkeypress="" onclick="closeCue()" href="javascript:;" id="fcue-close"></a>'+
    '</div>'+
    '<div class="ft ftnux"><p>'+
    '</p><span id="titlehelp" style="font-weight:bold;">Welcome Help Dialog</span><p></p><span id="titledesc">sssdd</span><p></p>'+
    '<p><a class="cta-1 cta helpbuttons" onclick="goToNextCue();" href="javascript:;"><img id="nextHelp" src="../../images/next.gif" width="80" height="30"></a></p>'+
    ''+
    '</div>'+
    '</div>'+
    '</div>'+
    '<div class="fcue-b">'+
    '<div></div>'+
    '</div>'+
    ''+
    '</div>'+
    '</div>',
    // left - top
    '<div id="mask" style="display:block;width:100%;z-index:9000;background-color:#CCCCCC;height:100%;left:0;opacity:0.3;top:0;"></div><div id="fcue-360" class="fcue-outer" style="visibility: visible; display: block; position: absolute; z-index:2000000; left: 188px; top: 12px;">'+
    '<div class="fcue-inner">'+
    '<div class="fcue-t"></div>'+
    '<div class="fcue-content">'+
    '<div class="bd">'+
    '<a onkeypress="" onclick="closeCue()" href="javascript:;" id="fcue-close"></a>'+
    '</div>'+
    '<div class="ft ftnux"><p>'+
    '</p><span id="titlehelp" style="font-weight:bold;">Welcome Help Dialog</span><p></p><span id="titledesc">sssdd</span><p></p>'+
    '<a class="cta-1 cta helpbuttons" onclick="closeCue();" href="javascript:;"><img id="closeHelp" src="../../images/close.jpg" width="80" height="30" style="display:none;"></a>'+
    '<a class="cta-1 cta helpbuttons" onclick="goToNextCue();" href="javascript:;"><img id="nextHelp" src="../../images/next.gif" width="80" height="30"></a>'+
    '<a class="cta-1 cta helpbuttons" onclick="goToPrevCue();" href="javascript:;"><img src="../../images/previous.gif" width="80" height="30"></a>'+
    ''+
    '</div>'+
    '</div>'+
    '</div>'+
    '<div class="fcue-b">'+
    '<div></div>'+
    '</div>'+
    '<div class="fcue-pnt fcue-pnt-lf-t">'+
    '</div>'+
    '</div>',
    // left - bottom
    '<div id="mask" style="display:block;width:100%;z-index:9000;background-color:#CCCCCC;height:100%;left:0;opacity:0.3;top:0;"></div><div id="fcue-360" class="fcue-outer" style="visibility: visible; display: block; position: absolute; z-index:2000000; left: 188px; top: 12px;">'+
    '<div class="fcue-inner">'+
    '<div class="fcue-t"></div>'+
    '<div class="fcue-content">'+
    '<div class="bd">'+
    '<a onkeypress="" onclick="closeCue()" href="javascript:;" id="fcue-close"></a>'+
    '</div>'+
    '<div class="ft ftnux"><p>'+
    '</p><span id="titlehelp" style="font-weight:bold;">Welcome Help Dialog</span><p></p><span id="titledesc">sssdd</span><p></p>'+
    '<a class="cta-1 cta helpbuttons" onclick="closeCue();" href="javascript:;"><img id="closeHelp" src="../../images/close.jpg" width="80" height="30" style="display:none;"></a>'+
    '<a class="cta-1 cta helpbuttons" onclick="goToNextCue();" href="javascript:;"><img id="nextHelp" src="../../images/next.gif" width="80" height="30"></a>'+
    '<a class="cta-1 cta helpbuttons" onclick="goToPrevCue();" href="javascript:;"><img src="../../images/previous.gif" width="80" height="30"></a>'+
    ''+
    '</div>'+
    '</div>'+
    '</div>'+
    '<div class="fcue-b">'+
    '<div></div>'+
    '</div>'+
    '<div class="fcue-pnt fcue-pnt-lf-b">'+
    '</div>'+
    '</div>',

    // bottom - left
    '<div id="mask" style="display:block;width:100%;z-index:9000;background-color:#CCCCCC;height:100%;left:0;opacity:0.3;top:0;"></div><div id="fcue-360" class="fcue-outer" style="visibility: visible; display: block; position: absolute; z-index:2000000; left: 188px; top: 12px;">'+
    '<div class="fcue-inner">'+
    '<div class="fcue-t"></div>'+
    '<div class="fcue-content">'+
    '<div class="bd">'+
    '<a onkeypress="" onclick="closeCue(0)" href="javascript:;" id="fcue-close"></a>'+
    '</div>'+
    '<div class="ft ftnux"><p>'+
    '</p><span id="titlehelp" style="font-weight:bold;">Welcome Help Dialog</span><p></p><span id="titledesc">sssdd</span><p></p>'+
    '<a class="cta-1 cta helpbuttons" onclick="closeCue(0);" href="javascript:;"><img id="closeHelp" src="../../images/close.jpg" width="80" height="30" style="display:none;"></a>'+
    '<a class="cta-1 cta helpbuttons" onclick="goToNextCue();" href="javascript:;"><img id="nextHelp" src="../../images/next.gif" width="80" height="30"></a>'+
    '<a class="cta-1 cta helpbuttons" onclick="goToPrevCue();" href="javascript:;"><img src="../../images/previous.gif" width="80" height="30"></a>'+
    ''+
    '</div>'+
    '</div>'+
    '</div>'+
    '<div class="fcue-b">'+
    '<div></div>'+
    '</div>'+
    '<div id="pointerdiv" class="fcue-pnt fcue-pnt-bm-l">'+
    '</div>'+
    '</div>',
    // bottom - right
    '<div id="mask" style="display:block;width:100%;z-index:9000;background-color:#CCCCCC;height:100%;left:0;opacity:0.3;top:0;"></div><div id="fcue-360" class="fcue-outer" style="visibility: visible; display: block; position: absolute; z-index:2000000; left: 188px; top: 12px;">'+
    '<div class="fcue-inner">'+
    '<div class="fcue-t"></div>'+
    '<div class="fcue-content">'+
    '<div class="bd">'+
    '<a onkeypress="" onclick="closeCue()" href="javascript:;" id="fcue-close"></a>'+
    '</div>'+
    '<div class="ft ftnux"><p>'+
    '</p><span id="titlehelp" style="font-weight:bold;">Welcome Help Dialog</span><p></p><span id="titledesc">sssdd</span><p></p>'+
    '<a class="cta-1 cta helpbuttons" onclick="closeCue();" href="javascript:;"><img id="closeHelp" src="../../images/close.jpg" width="80" height="30" style="display:none;"></a>'+
    '<a class="cta-1 cta helpbuttons" onclick="goToNextCue();" href="javascript:;"><img id="nextHelp" src="../../images/next.gif" width="80" height="30"></a>'+
    '<a class="cta-1 cta helpbuttons" onclick="goToPrevCue();" href="javascript:;"><img src="../../images/previous.gif" width="80" height="30"></a>'+
    ''+
    '</div>'+
    '</div>'+
    '</div>'+
    '<div class="fcue-b">'+
    '<div></div>'+
    '</div>'+
    '<div id="pointerdiv" class="fcue-pnt fcue-pnt-bm-r">'+
    '</div>'+
    '</div>',
    // top - left
    '<div id="mask" style="display:block;width:100%;z-index:9000;background-color:#CCCCCC;height:100%;left:0;opacity:0.3;top:0;"></div><div id="fcue-360" class="fcue-outer" style="visibility: visible; display: block; position: absolute; z-index:2000000; left: 188px; top: 12px;">'+
    '<div class="fcue-inner">'+
    '<div class="fcue-t"></div>'+
    '<div class="fcue-content">'+
    '<div class="bd">'+
    '<a onkeypress="" onclick="closeCue(0)" href="javascript:;" id="fcue-close"></a>'+
    '</div>'+
    '<div class="ft ftnux"><p>'+
    '</p><span id="titlehelp" style="font-weight:bold;">Welcome Help Dialog</span><p></p><span id="titledesc">sssdd</span><p></p>'+
    '<a class="cta-1 cta helpbuttons" onclick="closeCue(0);" href="javascript:;"><img id="closeHelp" src="../../images/close.jpg" width="80" height="30" style="display:none;"></a>'+
    '<a class="cta-1 cta helpbuttons" onclick="goToNextCue();" href="javascript:;"><img id="nextHelp" src="../../images/next.gif" width="80" height="30"></a>'+
    '<a class="cta-1 cta helpbuttons" onclick="goToPrevCue();" href="javascript:;"><img src="../../images/previous.gif" width="80" height="30"></a>'+
    ''+
    '</div>'+
    '</div>'+
    '</div>'+
    '<div class="fcue-b">'+
    '<div></div>'+
    '</div>'+
    '<div class="fcue-pnt fcue-pnt-t-l">'+
    '</div>'+
    '</div>',
    // top - right
    '<div id="mask" style="display:block;width:100%;z-index:9000;background-color:#CCCCCC;height:100%;left:0;opacity:0.3;top:0;"></div><div id="fcue-360" class="fcue-outer" style="visibility: visible; display: block; position: absolute; z-index:2000000; left: 188px; top: 12px;">'+
    '<div class="fcue-inner">'+
    '<div class="fcue-t"></div>'+
    '<div class="fcue-content">'+
    '<div class="bd">'+
    '<a onkeypress="" onclick="closeCue()" href="javascript:;" id="fcue-close"></a>'+
    '</div>'+
    '<div class="ft ftnux"><p>'+
    '</p><span id="titlehelp" style="font-weight:bold;">Welcome Help Dialog</span><p></p><span id="titledesc">sssdd</span><p></p>'+
    '<a class="cta-1 cta helpbuttons" onclick="closeCue();" href="javascript:;"><img id="closeHelp" src="../../images/close.jpg" width="80" height="30" style="display:none;"></a>'+
    '<a class="cta-1 cta helpbuttons" onclick="goToNextCue();" href="javascript:;"><img id="nextHelp" src="../../images/next.gif" width="80" height="30"></a>'+
    '<a class="cta-1 cta helpbuttons" onclick="goToPrevCue();" href="javascript:;"><img src="../../images/previous.gif" width="80" height="30"></a>'+
    ''+
    '</div>'+
    '</div>'+
    '</div>'+
    '<div class="fcue-b">'+
    '<div></div>'+
    '</div>'+
    '<div class="fcue-pnt fcue-pnt-t-r">'+
    '</div>'+
    '</div>',
    '<div id="mask" style="display:block;width:100%;z-index:9000;background-color:#CCCCCC;height:100%;left:0;opacity:0.3;top:0;"></div><div id="fcue-360" class="fcue-outer" style="visibility: visible; display: block; position: absolute; z-index:2000000; left: 188px; top: 12px;">'+
    '<div class="fcue-inner">'+
    '<div class="fcue-t"></div>'+
    '<div class="fcue-content">'+
    '<div class="bd">'+
    '<a onkeypress="" onclick="closeCue()" href="javascript:;" id="fcue-close"></a>'+
    '</div>'+
    '<div class="ft ftnux"><p>'+
    '</p><span id="titlehelp" style="font-weight:bold;">Welcome Help Dialog</span><p></p><span id="titledesc">sssdd</span><p></p>'+
    '<p><a class="cta-1 cta helpbuttons" onclick="closeCue();" href="javascript:;"><img id="closeHelp" src="../../images/close.jpg" width="80" height="30";"></a></p>'+
    ''+
    '</div>'+
    '</div>'+
    '</div>'+
    '<div class="fcue-b">'+
    '<div></div>'+
    '</div>'+
    ''+
    '</div>'+
    '</div>'
],

    startingMarkup: 'Please select a module to see details',
    id : 'helpdialog',
    helpIndex : 0,
    initComponent: function(config) {
        Wtf.taskDetail.superclass.initComponent.call(this, config);
    },

    getPositions: function(comppos, dir){
        var succ = false;
        var cmpnt = "abc";
        this.helpIndex = this.helpIndex - 1;
        do{
            this.helpIndex = this.helpIndex + 1;
            if(helpStore.find("helpindex", this.helpIndex) != -1){
                var rec  = helpStore.getAt(helpStore.find("helpindex", this.helpIndex));
                _helpContent[0] = rec.data.compid;
                _helpContent[1] = rec.data.title;
                _helpContent[2] = rec.data.descp;
            } else {
                _helpContent = null;
            }
            if(_helpContent){
                if(Wtf.get(_helpContent[0])){
                    cmpnt = Wtf.get(_helpContent[0]);
                    succ = true;
                } else if(Wtf.getCmp(_helpContent[0])){
                    if(Wtf.getCmp(_helpContent[0]).rendered == true){
                        cmpnt = Wtf.getCmp(_helpContent[0]).getEl();
                    }
                    else
                        continue;
                    succ = true;
                }
                if(!succ){
                    var try1 = showHelpCompId+_helpContent[0];
                    var try2 = _helpContent[0]+showHelpCompId;
                    if(Wtf.get(try1)){
                        cmpnt = Wtf.get(try1);
                        succ = true;
                    }
                    else if(Wtf.getCmp(try1)){
                        if(Wtf.getCmp(try1).rendered == true){
                            cmpnt = Wtf.getCmp(try1).getEl();
                        }
                        else
                            continue;
                        succ = true;
                    }
                    if(!succ){
                        if(Wtf.get(try2)){
                            cmpnt = Wtf.get(try2);
                            succ = true;
                        }
                        else if(Wtf.getCmp(try2)){
                            if(Wtf.getCmp(try2).rendered == true){
                                cmpnt = Wtf.getCmp(try2).getEl();
                            }
                            else
                                continue;
                            succ = true;
                        }
                    }
                }
            } else {
                break;
            }
        }while(succ != true);
        if(activeTab == 'dashboard')
            this.showInViewport();
        if(typeof cmpnt !== 'string')
            comppos = cmpnt.getXY();
        return comppos;
    },
    showInViewport: function(){
        switch(_helpContent[0]){
            case 'announcements_drag':
                Wtf.getCmp('announcements_drag').el.dom.scrollIntoView(false);
                break;
            case 'chart_drag':
                Wtf.getCmp('chart_drag').el.dom.scrollIntoView(false);
                break;
            default:
                Wtf.getCmp('quicklinks_drag').el.dom.scrollIntoView(false);
                break;
        }
    },

    welcomeHelp: function() {
        var data = _helpContent[2];
        if(helpStore.getCount()==1)
            this.tpl = new Wtf.Template(this.tplMarkup[7]);
        else
            this.tpl = new Wtf.Template(this.tplMarkup[0]);
        var ht = this.tpl.append(document.body,{});
        document.getElementById('titlehelp').innerHTML = _helpContent[1];
        document.getElementById('titledesc').innerHTML = data;
        if(helpStore.find("helpindex", this.helpIndex+1) == -1 && helpStore.getCount()!=1){
            document.getElementById('nextHelp').style.display = "none";
        }
        Wtf.get('fcue-360').setXY([400,150]);
    },

    getAdjustedPositions: function(comppos, index, helpSize){
        var pos = comppos;
        switch(index) {
            case 1: //left-top
                pos[1] -= 37;
                pos[0] += 100;
                break;
            case 2: //left-bottom
                break;
            case 3: //bottom-left
                switch(activeTab){
                    case 'dashboard':
                        switch(_helpContent[0]){
                            case 'dashadmin':
                                pos[1] -= 285;
                                pos[0] += 20;
                                break;
                            default:
                                pos[1] -= 200;
                                pos[0] += 20;
                                break;
                        }
                        break;
                    case 'home':
                        switch(_helpContent[0]){
                            case 'teamCal':
                                pos[1] -= 300;
                                pos[0] += 20;
                                break;
                            default:
                                pos[1] -= 200;
                                pos[0] += 20;
                                break;
                        }
                        break;
                    case 'document':
                        pos[1] -= 180;
                        pos[0] += 20;
                        break;
                    case 'adminmember':
                        switch(_helpContent[0]){
                            case 'dropmem':
                                pos[1] -=250;
                                pos[0] += 20;
                                break;
                            case 'activateMember':
                                pos[1] -=200;
                                pos[0] += 20;
                                break;
                            case 'setmod':
                                pos[1] -=220;
                                pos[0] += 20;
                                break;
                            case 'remmod':
                                pos[1] -=170;
                                pos[0] += 20;
                                break;
                            case 'chgperm':
                                pos[1] -=310;
                                pos[0] += 20;
                                break;
                        }
                        break;
                    case 'admininvite':
                        pos[1] -=190;
                        pos[0] += 10;
                        break;
                    case 'adminrequest':
                        pos[1] -=170;
                        pos[0] += 10;
                        break;
                    case 'adminresource':
                        switch(_helpContent[0]){
                            case 'manageResMenu':
                                pos[1] -=290;
                                pos[0] += 10;
                                break;
                            case 'billable':
                                pos[1] -=200;
                                pos[0] += 10;
                                break;
                        }
                        break;
                    case 'projectadministration':
                        switch(_helpContent[0]){
                            case 'manageuser':
                                pos[1] -= 220;
                                pos[0] += 20;
                                break;
                            default:
                                pos[1] -= 200;
                                pos[0] += 20;
                                break;
                        }
                        break;
                    case 'companyadministration':
                        pos[1] -= 210;
                        pos[0] += 20;
                        break;
                    case 'project':
                        pos[1] -= 170;
                        pos[0] += 20;
                        break;
                    case 'mydocuments':
                        switch(_helpContent[0]){
                            case 'del_butt':
                                pos[1] -= 180;
                                pos[0] -= 20;
                                break;
                            case 'toggleBttn':
                                pos[1] -= 210;
                                pos[0] += 20;
                                break;
                            case 'ver_butt':
                                pos[1] -= 250;
                                pos[0] += 20;
                                break;
                            case 'revlist_butt':
                                pos[1] -= 220;
                                pos[0] += 20;
                                break;
                            default:
                                pos[1] -= 200;
                                pos[0] += 20;
                                break;
                        }
                        break;
                    default:
                        pos[1] -= 200;
                        pos[0] += 20;
                        break;
                }
                document.getElementById('pointerdiv').style.top = ((helpSize.height-20)+'px');// 22px - bottom div height
                break;
            case 4: //bottom-right
                pos[1] -= (helpSize.height);
                pos[0] -= (helpSize.width-22-32); // 22px - left div width and 32px - pointer position at inner side
                document.getElementById('pointerdiv').style.top = ((helpSize.height-20)+'px');// 22px - bottom div height
                break;
            case 5: //top - left
                switch(_helpContent[0]){
                    case 'topic-grid':
                        pos[1] += 60;
                        pos[0] -= 30;
                        break;
                    default:
                        pos[1] += 40;
                        pos[0] -= 30;
                        break;
                }
                break;
            case 6: //top - right
                pos[1] += 35;
                pos[0] -= 320;
                break;

        }
        return pos;
    },

    updateToNextDetail: function(data) {
        _nexthelpContent = [];
        this.helpIndex = this.helpIndex+1;
        var rec  = helpStore.getAt(helpStore.find("helpindex", this.helpIndex));
        _helpContent[0] = rec.data.compid;
        _helpContent[1] = rec.data.title;
        _helpContent[2] = rec.data.descp;
        if(helpStore.find("helpindex", this.helpIndex+1) != -1){
            var nextrec  = helpStore.getAt(helpStore.find("helpindex", this.helpIndex+1));
            _nexthelpContent[0] = nextrec.data.compid;
            _nexthelpContent[1] = nextrec.data.title;
            _nexthelpContent[2] = nextrec.data.descp;
        } else {
            _nexthelpContent = null;
        }
        if(_helpContent){
            var comppos = "";
            comppos = this.getPositions(comppos);
            if(comppos != ""){
                var index = this.getTemplateIndex(comppos);

                this.tpl = new Wtf.Template(this.tplMarkup[index]);
                var ht = this.tpl.append(document.body,{});

                var helpDiv = Wtf.get('fcue-360');
                document.getElementById('titlehelp').innerHTML = _helpContent[1];
                document.getElementById('titledesc').innerHTML = _helpContent[2];
                if(!_nexthelpContent){
                    document.getElementById('nextHelp').style.display = "none";
                    document.getElementById('closeHelp').style.display = "block";
                }
                else
                    document.getElementById('closeHelp').style.display = "none";
                var helpSize = helpDiv.getSize();
                var pos = this.getAdjustedPositions(comppos, index, helpSize);
                helpDiv.setXY(pos);
                if(activeTab == 'dashboard')
                    Wtf.get('mask').remove();
//                document.getElementById('fcue-360').scrollIntoView(false);
            }
        }
    },

    updateToPrevDetail: function(data) {
        var flag = 0;
        this.helpIndex = this.helpIndex-1;
        if(this.helpIndex == 0){
            if(helpStore.find("helpindex", this.helpIndex) != -1){
                var rec  = helpStore.getAt(helpStore.find("helpindex", this.helpIndex));
                _helpContent[0] = rec.data.compid;
                _helpContent[1] = rec.data.title;
                _helpContent[2] = rec.data.descp;
            } else {
                _helpContent = null;
            }
            this.welcomeHelp();
            flag =1;
        }
        if(flag != 1){
            //_helpContent = Wtf.Help[activeTab + this.helpIndex];
            if(helpStore.find("helpindex", this.helpIndex) != -1){
                var rec  = helpStore.getAt(helpStore.find("helpindex", this.helpIndex));
                _helpContent[0] = rec.data.compid;
                _helpContent[1] = rec.data.title;
                _helpContent[2] = rec.data.descp;
            } else {
                _helpContent = null;
            }
            if(_helpContent){
                var comppos = "";
                comppos = this.getPositions(comppos);

                var index = this.getTemplateIndex(comppos);

                this.tpl = new Wtf.Template(this.tplMarkup[index]);
                var ht = this.tpl.append(document.body,{});

                var helpDiv = Wtf.get('fcue-360');
                document.getElementById('titlehelp').innerHTML = _helpContent[1];
                document.getElementById('titledesc').innerHTML = _helpContent[2];
                var helpSize = helpDiv.getSize();
                var pos = this.getAdjustedPositions(comppos, index, helpSize);
                helpDiv.setXY(pos);
                if(activeTab == 'dashboard')
                    Wtf.get('mask').remove();
//                document.getElementById('fcue-360').scrollIntoView(false);
            }
        }
    },

    blankDetail : function() {
        this.bltpl.overwrite(this.body,"");
    },

    getTemplateIndex : function(comppos) {
        var index = 0;
        var xPos = comppos[0];
        var yPos = comppos[1];
        var flag = 0;
        var myWidth = 0, myHeight = 0;
        if( typeof( window.innerWidth ) == 'number' ) {
            //Non-IE
            myWidth = window.innerWidth;
            myHeight = window.innerHeight;
        } else if( document.documentElement && ( document.documentElement.clientWidth || document.documentElement.clientHeight ) ) {
            //IE 6+ in 'standards compliant mode'
            myWidth = document.documentElement.clientWidth;
            myHeight = document.documentElement.clientHeight;
        } else if( document.body && ( document.body.clientWidth || document.body.clientHeight ) ) {
            //IE 4 compatible
            myWidth = document.body.clientWidth;
            myHeight = document.body.clientHeight;
        }

        if(xPos < 150){
            if(yPos > 60 && yPos < (myHeight - 150))
                index = 1;
            else if(yPos > (myHeight - 150))
                index = 3;
            else if(yPos < 60)
                index = 1;
        } else if(xPos > 150 && xPos < (myWidth - 375)){
            if(yPos > 60 && yPos < (myHeight - 150))
                index = 5;
            else if(yPos > (myHeight - 150))
                index = 3;
            else if(yPos < 60)
                index = 5;
        } else if(xPos > (myWidth - 375)){
            if(yPos > 60 && yPos < (myHeight - 150))
                index = 6;
            else if(yPos > (myHeight - 150))
                index = 4;
            else if(yPos < 60)
                index = 6;
        }
        return index;
    }

});

function closeCue () {
    Wtf.get('fcue-360').remove();
    if(Wtf.get('mask'))
        Wtf.get('mask').remove();
}

function goToNextCue() {
    closeCue();
    Wtf.getCmp('helpdialog').updateToNextDetail();
}

function goToPrevCue() {
    closeCue();
    Wtf.getCmp('helpdialog').updateToPrevDetail();
}

function getHelthStatusImage(status){
     var _i=["ontime.gif","slightly.gif","gravely.gif"];
     return "../../images/health_status/"+_i[status-1];
}


function getHealthStatus(status){
     var _s = [WtfGlobal.getLocaleText('pm.project.home.health.ontime'),WtfGlobal.getLocaleText('pm.project.home.health.slightly'),WtfGlobal.getLocaleText('pm.project.home.health.gravely')];
     return _s[status-1];
}
