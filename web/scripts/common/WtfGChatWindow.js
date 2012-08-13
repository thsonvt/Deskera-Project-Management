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
Wtf.common.WtfGChatWindow = function(config){
    Wtf.common.WtfGChatWindow.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.common.WtfGChatWindow, Wtf.Component, {
    minimized : false,
    type : true,
    mytime : false,
    remotetime : false,
    lastmsguser : '',

    //  status : offline = 0, online = 1, busy = 2
    initComponent: function(){
        Wtf.common.WtfGChatWindow.superclass.initComponent.call(this);
        this.addEvents({
            "closeclicked": true, // fired when close is clicked - should be handled in a globle function to close or hide the window, arguments passed - window object
            "minclicked": true,   // fired when minimize button is clicked, arguments passed - window object
            "maxclicked": true,   // fired when maximize button is clicked, arguments passed - window object
            "msgentered": true    // fired when return button is hit with some message in textarea , arguments passed - window object, message, towhom(userid)
        });
    },
    onRender: function(config){
        Wtf.common.WtfGChatWindow.superclass.onRender.call(this,config);
        this.elDom = Wtf.DomHelper.append(this.renderTo,{
            tag : "div",
            id : "chat_panel_div" + this.userid,
            style:"position:relative;float:right;height:100%;width:225px;margin-right:5px;z-index:10000",
            children : [{
                tag : "div",
                id : "outerdiv1" + this.userid,
                style:"width:100%",
                cls : "outerdiv-chat-win",
                children :[{
                    tag : "div",
                    cls:"l m",
                    children : [{
                        tag : "div",
                        cls:"l n",
                        children : [{
                            tag : "div",
                            id : "chat_panel_header_tools_div" + this.userid,
                            cls: "k x-panel-header x-unselectable" ,
                            children :[{
                                tag:"table",
                                cls:"headerTable",
                                children:[{
                                    tag:"tr",
                                    children:[{
                                        tag:"td",
                                        style:"width:16px",
                                        children:[{
                                            tag : "div",
                                            cls: this.getStatusImgClass(),
                                            id : "statusimg"+this.userid
                                        }]
                                    },{
                                        tag:"td",
                                        style:"margin:0px",
                                        children:[{
                                            tag : "div",
                                            id : "chat_panel_header" + this.userid,
                                            cls: "headerText x-panel-header-text" ,
                                            html : this.chatWith
                                            
                                        }]
                                    },{
                                        tag:"td",
                                        style:"text-align:right;width:16px",
                                        children:[{
                                            tag : "div",
                                            cls: "x-tool x-tool-minimize",
                                            id : "minmaxwin_"+this.userid
                                        }]
                                    },{
                                        tag:"td",
                                        style:"text-align:right;width:16px",
                                        children:[{
                                            tag : "div",
                                            cls: "x-tool x-tool-close",
                                            id : "closewin_"+this.userid
                                        }]
                                    }]
                                }]
                            }]
                        },{
                            tag : "div",
                            id : "display_chat_area_div"+this.userid ,
                            style:'background-color:#FFFFFF;',
                            children : [{
                                tag : "div",
                                id : "chat_panel_msg" + this.userid,
                                style:"overflow:auto;",
                                cls: "ko"
                            },{
                                tag : "div",
                                id : "chat_panel_status" + this.userid,
                                cls: "kstatus"
                            },{
                                tag : "div",
                                cls: "nH",
                                children : [{
                                    tag : "div",
                                    cls: "jp",
                                    children : [{
                                        tag : "div",
                                        cls: "jU",
                                        children : [{
                                            tag : "div",
                                            cls: "nH",
                                            children : [{
                                                tag : "textarea",
                                                id : "chat_panel_text" + this.userid,
                                                style:"height:36px;overflow:auto;",
                                                ignoreesc : "true",
                                                cls: "jT"
                                            }]
                                        }]
                                    }]
                                }]
                            }]
                        }]
                    }]
                }]
            }]
        });
        Wtf.get("display_chat_area_div"+this.userid).setVisibilityMode(Wtf.Element.DISPLAY);
        this.chatTextarea = this.elDom.getElementsByTagName("textarea")[0];
        Wtf.EventManager.addListener("closewin_"+this.userid, 'click', this.closeWin, this);
        Wtf.EventManager.addListener("minmaxwin_"+this.userid, 'click', this.minmaxWin, this);
        Wtf.EventManager.addListener("chat_panel_header"+this.userid, 'click', this.minmaxWin, this);
        Wtf.EventManager.addListener("statusimg"+this.userid, 'click', this.minmaxWin, this);
        Wtf.EventManager.addListener("outerdiv1" + this.userid, 'click',function(){
            Wtf.get("chat_panel_header_tools_div" + this.userid).removeClass('newmsg');
        }, this);
        Wtf.EventManager.addListener("chat_panel_text" + this.userid, 'keyup', this.keypressed, this);
        this.chatTextarea.focus();
    },
    closeWin : function(e){
        this.fireEvent("closeclicked",this.userid);
    },
    minmaxWin : function(e){
        if(this.minimized){
            Wtf.get("minmaxwin_"+this.userid).removeClass("x-tool-maximize");
            Wtf.get("minmaxwin_"+this.userid).addClass("x-tool-minimize");
            Wtf.get("chat_panel_header_tools_div" + this.userid).removeClass('newmsg');
            Wtf.get("chat_panel_header_tools_div" + this.userid).addClass('x-panel-header');
            Wtf.get("display_chat_area_div"+this.userid).show();
            this.fireEvent("maxclicked",this);
        }else{
            Wtf.get("minmaxwin_"+this.userid).removeClass("x-tool-minimize");
            Wtf.get("minmaxwin_"+this.userid).addClass("x-tool-maximize");
            Wtf.get("display_chat_area_div"+this.userid).hide();
            this.fireEvent("minclicked",this);
        }
        this.minimized = !this.minimized;
    },
    show: function(){
        if(this.minimized){
            Wtf.get("minmaxwin_"+this.userid).removeClass("x-tool-maximize");
            Wtf.get("minmaxwin_"+this.userid).addClass("x-tool-minimize");
            Wtf.get("chat_panel_header_tools_div" + this.userid).removeClass('newmsg');
            Wtf.get("chat_panel_header_tools_div" + this.userid).addClass('x-panel-header');
            Wtf.get("display_chat_area_div"+this.userid).show();
            this.fireEvent("maxclicked",this);
            this.minimized = !this.minimized;
        }
        Wtf.get("chat_panel_div" + this.userid).removeClass('x-hide-display');
    },
    hide: function(){
        Wtf.get("chat_panel_div" + this.userid).addClass('x-hide-display');
    },
    append : function(user,username,text,time){
        var chatDisDiv =  Wtf.get("chat_panel_msg"+this.userid);
        var timeStr="";
        if(time!=""){        
        timeStr = "<font color='gray' font-style='italics'> ["+time+"] </font>";
        }
        var tdiv = document.createElement('div');
        var arr = [];
        arr = text.match(/(:\(\()|(:\)\))|(:\))|(:x)|(:\()|(:P)|(:D)|(;\))|(;;\))|(&gt;:D&lt;)|(:-\/)|(:&gt;&gt;)|(:-\*)|(=\(\()|(:-O)|(X\()|(:&gt;)|(B-\))|(:-S)|(#:-S)|(&gt;:\))|(:\|\|)|(\/:\))|(=\)\))|(O:-\))|(:-B)|(=;)|(:-c)|(:\|)/g);
        tdiv.innerHTML = text;
        if (arr != null) {
            for (var i = 0; i < arr.length; i++) {
                this.smiley(tdiv, arr[i]);
            }
        }
        text = tdiv.innerHTML;
        if(this.lastmsguser==user){
            chatDisDiv.insertHtml("beforeEnd", "<p>"+text+"</p>");
        }else{
            var m = "<b>"+username+"</b>"+ timeStr +": ";
            chatDisDiv.insertHtml("beforeEnd", "<p>" + m + text+"</p>");
        }
        this.lastmsguser = user;
        chatDisDiv.scroll("down",Wtf.get("chat_panel_msg"+this.userid).getHeight());
        if(this.status==1){
            this.statusMsg("");
        }else if(this.status==0){
            this.statusMsg("is offline.");
        }
        if(this.minimized){
            Wtf.get("chat_panel_header_tools_div" + this.userid).removeClass('x-panel-header');
            Wtf.get("chat_panel_header_tools_div" + this.userid).addClass('newmsg');
        }
    },
    statusMsg : function(msg){  // this function is used to set status of remote user.
        var chatDisDiv =  Wtf.get("chat_panel_status"+this.userid);
        if(msg.length > 0){
            if(chatDisDiv)
                chatDisDiv.dom.innerHTML="<p>"+ WtfGlobal.getLocaleText({key:msg, params: this.chatWith}) +" </p>";
        }else if(chatDisDiv){
            chatDisDiv.dom.innerHTML="";
        }
    },
    keypressed : function(e){
        Wtf.get("chat_panel_header_tools_div" + this.userid).removeClass('newmsg');
        if(e){
            if(e.keyCode==13 && e.target.value.trim() != ""){
                var d = new Date();
                var time = getUserTime();
                if(this.remotetime){
                    clearTimeout(this.remotetime);
                    this.remotetime =false;
                    this.type=true;
                }
                var msg = e.target.value.trim();
                msg = Wtf.util.Format.htmlEncode(msg);
      //          msg = WtfGlobal.HTMLStripper(msg);
                this.append(loginid,"Me",msg,"");
                e.target.value='';
                this.fireEvent("msgentered",this,msg,this.chatWith);
            }else if(e.keyCode==13 && e.target.value.trim() == ""){
                e.target.value='';
            }
        }
    },
    setType : function(){
        this.type=true;
    },
    getStatusImgClass : function(){
        if(this.status == 0){
            this.statusMsg('pm.contacts.chat.isoffline');
            return "statusstrip offline";
        }else if(this.status == 1){
            this.statusMsg("pm.contacts.chat.isonline");
            return "statusstrip online";
        }
    },
    setStatus: function(sts){
        this.status = sts;
        Wtf.get("statusimg"+this.userid).dom.className = this.getStatusImgClass();
    },
    smiley: function(tdiv, emoticon){
        tdiv.innerHTML = tdiv.innerHTML.replace(emoticon, '<img src=../../images/smiley' + (smileyStore.indexOf(emoticon) +1) + '.gif style=display:inline;vertical-align:text-top;></img>');
    },
    blink: function(div){
        $(div).fadeOut('slow', function(){
            $(div).fadeIn('slow');
        });
    }
});
Wtf.reg('WtfGChatWindow', Wtf.common.WtfGChatWindow);
