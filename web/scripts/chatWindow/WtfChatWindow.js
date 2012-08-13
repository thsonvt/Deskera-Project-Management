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
Wtf.chatListItem = function(conf){
    Wtf.apply(this, conf);
    Wtf.chatListItem.superclass.constructor.call(this, conf);
}

Wtf.extend(Wtf.chatListItem, Wtf.Panel, {
    border: false,
    baseCls: 'linkPanelBorder1',
    initComponent: function(conf){
        Wtf.chatListItem.superclass.initComponent.call(this, conf);
        this.addEvents({
            "panelClick": true
        });
    },

    onRender: function(conf){
        Wtf.chatListItem.superclass.onRender.call(this, conf);
        this.mainDiv = document.createElement("div");
        this.mainDiv.style.cursor = "pointer";
        var img = document.createElement("img");
        img.src = "../../images/Chat.png";
        img.style.verticalAlign = "top";
        var name = document.createElement("span");
        name.style.paddingLeft = "5px";
        name.innerHTML = this.text;
        this.mainDiv.appendChild(img);
        this.mainDiv.appendChild(name);
        this.mainDiv.onclick = this.buttonClicked.createDelegate(this, [this]);
        this.add(this.mainDiv);
    },

    buttonClicked: function(){
        this.fireEvent("panelClick", this);
    }
});

//-----------Chat List Window-----------------

Wtf.ChatListWindow = function(config){
    Wtf.apply(this, config);
    Wtf.ChatListWindow.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.ChatListWindow, Wtf.Window, {
    usernames: [],
    userIds: [],
    //    initComponent: function(){
    //        Wtf.ChatListWindow.superclass.initComponent.call(this);
    //    },

    onRender: function(config){
        Wtf.ChatListWindow.superclass.onRender.call(this, config);
        this.innerpanel = this.add(new Wtf.Panel({
            layout: 'fit',
            bodyStyle : "padding:5px;background-color:#f1f1f1",
            border:false
        }));
    },
    addChat:function(userid,username){
        /*var addPanel = new Wtf.Panel({
                html: '<div >' +
                '<img src="../../images/Chat.png" onclick= \'openChatWin("' + userid + '")\' >' +
                '<h>'+username+'<h>'+
                '</div>'
        })*/
        this.usernames.push(username);
        this.userIds.push(userid);
        if(this.innerpanel!=null){
            this.setHeight(this.getSize().height + 25);
            //            var addPanel = new Wtf.Panel({
            var addPanel = new Wtf.chatListItem({
                autoHeight: true,
                uid: userid,
                //                height:20,
                border:false,
                id:"minChat"+userid,
                text: username
            //                html: "<div onclick= \"openChatWin('" + userid + "','"+ this.id + "')\" style='cursor:pointer;'>" +
            //                "<img src='../../images/Chat.png' style='vertical-align:top;' >" +
            //                '<span>'+' '+username+'<span>'+
            //                '</div>'
            });
            addPanel.on("panelClick", this.openChatWin, this);
            this.innerpanel.add(addPanel);
            this.doLayout();
        }
    /*this.innerpanel.add(addPanel);
        this.innerpanel.doLayout();*/
    },
    showChatList:function(){
        for(var i=0;i<this.usernames.length;i++){
            this.setHeight(this.getSize().height - 20);
            this.innerpanel.remove("minChat"+this.userIds[i],true);
        }
        for(var i=0;i<this.usernames.length;i++){
            this.setHeight(this.getSize().height + 25);
            //            var addPanel = new Wtf.Panel({
            var addPanel = new Wtf.chatListItem({
                //                height:20,
                autoHeight: true,
                border:false,
                uid: this.userIds[i],
                id:"minChat"+this.userIds[i],
                text: this.usernames[i]
            //                html: "<div onclick= \"openChatWin('" + this.userIds[i] + "','"+ this.id + "')\" style='cursor:pointer;'>" +
            //                "<img src='../../images/Chat.png' style='vertical-align:top;' >" +
            //                '<span>'+' '+this.usernames[i]+'<span>'+
            //                '</div>'
            });
            addPanel.on("panelClick", this.openChatWin, this);
            this.innerpanel.add(addPanel);
        }
        this.doLayout();
    },
    openChatWin:function(pObj){
        Wtf.getCmp("chatWin" + pObj.uid).show();
        this.hide();
    }
});

Wtf.ChatHTMLEditor = function(config){
    // Wtf.apply(this,config);
    Wtf.ChatHTMLEditor.superclass.constructor.call(this, config);
    this.addEvents = {
        "enterKeyPressed": true
    };
}
Wtf.extend(Wtf.ChatHTMLEditor,Wtf.newHTMLEditor,{
    applyCommand : function(e){
        var temp=e;
        if(e.ctrlKey){
            var c = e.getCharCode(), cmd;
            if (c > 0) {
                c = String.fromCharCode(c);
                switch (c) {
                    case 'b':
                        cmd = 'bold';
                        break;
                    case 'i':
                        cmd = 'italic';
                        break;
                    case 'u':
                        cmd = 'underline';
                        break;
                }
                if (cmd) {
                    this.win.focus();
                    this.execCmd(cmd);
                    this.deferFocus();
                    e.preventDefault();
                }
            }
        }
        if(temp.getKey() == 13){
            e.preventDefault();
            this.fireEvent("enterKeyPressed");
        }
    }

//    fixKeys : function(){
//        if(Wtf.isIE){
//            return function(e){
//                var k = e.getKey(), r;
//                if(k == e.TAB){
//                    e.stopEvent();
//                    r = this.doc.selection.createRange();
//                    if(r){
//                        r.collapse(true);
//                        r.pasteHTML('&nbsp;&nbsp;&nbsp;&nbsp;');
//                        this.deferFocus();
//                    }
//                }else if(k == e.ENTER){
//                    e.stopEvent();
//                    r = this.doc.selection.createRange();
//                    this.fireEvent("enterKeyPressed");
//                    if(r){
//                        var target = r.parentElement();
//                        if(!target || target.tagName.toLowerCase() != 'li'){
//                            e.stopEvent();
//                            //  r.pasteHTML('<br />');
//                            r.collapse(false);
//                            r.select();
//                        }
//                    }
//                }
//            };
//        }else if(Wtf.isOpera){
//            return function(e){
//                var k = e.getKey();
//                if(k == e.TAB){
//                    e.stopEvent();
//                    this.win.focus();
//                    this.execCmd('InsertHTML','&nbsp;&nbsp;&nbsp;&nbsp;');
//                    this.deferFocus();
//                }
//            };
//        }else if(Wtf.isWebKit){
//            return function(e){
//                var k = e.getKey();
//                if(k == e.TAB){
//                    e.stopEvent();
//                    this.execCmd('InsertText','\t');
//                    this.deferFocus();
//                }
//            };
//        }
//    }
});


/*  WtfChaWin: Start    */
Wtf.ChatWindow = function(config){
    Wtf.apply(this, config);
    //Wtf.ChatWindow.superclass.constructor.call(this, config);
    Wtf.ChatWindow.superclass.constructor.call(this, {
        layout: 'fit',
        width: 500,
        height: 300,
        id: config.id,
        maximizable: true,
        minimizable: true,
        plain: true,
        closable: true,
        iconCls: config.iconCls,
        title: config.title,
        shadow: true
    });
    this.addEvents({
        "afterMessageReceived" : true
    });
    this.remotepersonid = config.remotepersonid;
    this.node = config.node;
    if (config.iconCls == "K-icon")
        this.userStatus = "online";
    else
        this.userStatus = "offline";
    this.editor = new Wtf.ChatHTMLEditor({
        //        id:this.id+"chat",
        enableLists: false,
        enableSourceEdit: false,
        enableAlignments: false,
        enableFonts: true,
        hideLabel: true,
        border: false,
        deferHeight: true
    });

    this.editor.on("activate", function(){
        this.show();
        clearInterval(this.timeoutId);
        document.title = titleText;
        focusOn(this.editor.id);
    }, this);
    this.editor.on("enterKeyPressed", this.handleEnterEvent, this);
    this.on("afterMessageReceived",this.handleTitleBar,this);
    this.on("show",function(){
        Wtf.get('chatWinReadArea'+this.id).addListener("click", function(a,e){
            var tar = a.target;
            a.preventDefault();
            if(tar.tagName == 'A' && tar.href.match('http://')) {
                window.open(tar.href,'LinkFromYourFriend');
                return false;
            //                a.preventDefault();
            }
            if(this.timeoutId){
                clearInterval(this.timeoutId);
                document.title = titleText;
            }
        }, this);
    },this);
};


Wtf.extend(Wtf.ChatWindow, Wtf.Window, {
    initComponent: function(){
        Wtf.ChatWindow.superclass.initComponent.call(this);
    },

    onRender: function(config){
        document.getElementById('chatlistcontainer').style.display = "block";
        if(Wtf.getCmp('chatListWindow')==null){
            new Wtf.ChatListWindow({
                wizard:false,
                closeAction : 'hide',
                layout: 'fit',
                closable: false,
                width : 200,
                //                heigth:450,
                autoScroll:true,
                id:"chatListWindow"/*,
                autoHeight: true*/
            });
        }
        Wtf.getCmp('chatListWindow').addChat(this.remotepersonid,this.remotepersonname);
        Wtf.ChatWindow.superclass.onRender.call(this, config);
        this.innerpanel = this.add(new Wtf.Panel({
            layout: 'border',
            items: [{
                id:'chatWinReadArea'+this.id,
                region: 'center',
                border: false,
                height: '70%',
                html: '<div id="' + this.id + '_readArea_" class="readAreaClass" ></div>'
            }, {
                region: 'south',
                height: 75,
                border: false,
                split: true,
                layout: 'fit',
                items: this.editor
            }],
            tbar: [{
                text: WtfGlobal.getLocaleText('pm.contacts.viewprofile'),
                scope: this,
                iconCls: 'pwnd profile',
                handler: function(){
                    var id = this.remotepersonid;
                    var name =  this.remotepersonname;
                    mainPanel.loadTab("../../user.html","   " +id, name, "navareadashboard",Wtf.etype.user,true);
                //mainPanel.loadTab("../../userProfile.html","mainuserProfile_"+id+"_disp", name, "navareadashboard",Wtf.etype.user);
                }
            }],
            buttons: [{
                text: WtfGlobal.getLocaleText('lang.Send.text'),
                scope: this,
                handler: function(){
                    var str = this.editor.getValue();
                    if (str != "") {
                        this.insertmsg(str, 1);
                        this.editor.setValue(null);
                        var msgstr = encodeURIComponent(str);
                        Wtf.Ajax.requestEx({
                            url: Wtf.req.prt + "chatmessage.jsp",
                            params: {
                                user: loginid,
                                remoteUser: this.remotepersonid,
                                chatMessage: msgstr,
                                rstatus: this.userStatus,
                                rUserName: this.remotepersonname
                            }
                        }, this);
                    }
                }
            }]
        }));
        this.on("minimize", this.handleChatMinimize, this);
        this.on("close",this.handleClose,this);
        this.header.on('click', this.clearTitleBar, this);
    },
    handleChatMinimize: function(obj){
        //obj.node.getUI().getTextEl().innerHTML += '<span id="chatTreeMin"><img style="vertical-align:text-top;" src="../../images/Chat.png" style="height:12px;"/></span>';
        this.hide();
    },
    handleClose:function(){
        var _obj = Wtf.getCmp("chatListWindow");
        if(Wtf.getCmp("minChat"+this.remotepersonid)){
            _obj.innerpanel.remove(Wtf.getCmp("minChat"+this.remotepersonid),true)
            _obj.setHeight(_obj.getSize().height - 20);
            _obj.innerpanel.doLayout();
        }
        _obj.userIds.remove(this.remotepersonid);
        _obj.usernames.remove(this.remotepersonname);
        if(_obj.usernames.length==0){
            document.getElementById('chatlistcontainer').style.display = "none";
            if(_obj.innerpanel!=null){
                _obj.close();
            }
        }
    },
    handleEnterEvent: function(){
        this.editor.syncValue();
        var str = this.editor.getValue();
        if (str != "") {
            this.insertmsg(str, 1);
            this.editor.setValue(null);
            var msgstr = encodeURIComponent(str);
            Wtf.Ajax.requestEx({
                url: Wtf.req.prt + "chatmessage.jsp",
                params: {
                    user: loginid,
                    remoteUser: this.remotepersonid,
                    chatMessage: msgstr,
                    rstatus: this.userStatus,
                    rUserName: this.remotepersonname
                }
            }, this, function(result, req){
                }, function(result, req){
                });
        }
    },
    handleMultipleWin: function(){
        if(chatWinPosition.length == 0){
            this.show();
            var WinPosition = this.getPosition();
            chatWinPosition.push([this.id, WinPosition[0],WinPosition[1]]);
        }else{
            var WinPosition = chatWinPosition[chatWinPosition.length -1];
            clearInterval(Wtf.getCmp(WinPosition[0]).timeoutId);
            document.title = titleText;
            this.setPosition(WinPosition[1] + 10,WinPosition[2] + 30);
            chatWinPosition.push([this.id, WinPosition[1] + 10,WinPosition[2] + 30]);
            this.show();
        }
    },
    winMove : function(win,x,y){
        for(var i=0;i<chatWinPosition.length;i++){
            if(this.id == chatWinPosition[i][0]){
                chatWinPosition[i][1]=x;
                chatWinPosition[i][2]=y;
                break;
            }
        }
    },
    winDestroy : function(panel){
        for(var i=0;i<chatWinPosition.length;i++){
            if(this.id == chatWinPosition[i][0]){
                chatWinPosition.splice(i,1);
                 clearInterval(this.timeoutId);
                 document.title = titleText;
            }
        }
    },
    insertmsg: function(msg,mflag,time){
        var rA = document.getElementById(this.id + "_readArea_");
        var imtdiv = document.createElement('div');
        var arr = [];
        if (mflag == 1) {
            name = "Me";
            this.clearTitleBar();
        } else if (mflag == 2) {
            name = this.remotepersonname;
            msg = WtfGlobal.URLDecode(decodeURIComponent(msg));
            this.fireEvent("afterMessageReceived",name);
            name = name +" "+ '<font color = \"#AAAAAA\">'+"(on "+ time + ")"+'</font>';
        }
        arr = msg.match(/(:\(\()|(:\)\))|(:\))|(:x)|(:\()|(:P)|(:D)|(;\))|(;;\))|(&gt;:D&lt;)|(:-\/)|(:&gt;&gt;)|(:-\*)|(=\(\()|(:-O)|(X\()|(:&gt;)|(B-\))|(:-S)|(#:-S)|(&gt;:\))|(:\|)|(\/:\))|(=\)\))|(O:-\))|(:-B)|(=;)|(:-c)/g);
        msg = msg.replace(/<STRONG>/gi,"<b>");
        msg = msg.replace(/<\/STRONG>/gi,"</b>");
        msg = msg.replace(/<em>/gi,"<i>");
        msg = msg.replace(/<\/em>/gi,"</i>");
        if (arr == null) {
            imtdiv.innerHTML = '<p><strong><font color = \"#006699\">' + name + '</font>:</strong> ' + msg;
        } else {
            var i;
            var smileyStr;
            imtdiv.innerHTML = '<p><strong><font color = \"#006699\">' + name + '</font>:</strong> ' + msg;
            for (i = 0; i < arr.length; i++) {
                smiley(imtdiv, arr[i]);
            }
        }
        rA.innerHTML += imtdiv.innerHTML;
        rA.scrollTop = rA.scrollHeight;
    },
    handleTitleBar: function(ename){
        if(titleText !== document.title)
            document.title = titleText;
        if(this.timeoutId){
            clearInterval(this.timeoutId);
            this.timeoutId = null;
        }
        var msg = ename+ " "+ "says...";
        this.timeoutId = setInterval(function(){
            document.title = document.title == msg ? titleText : msg;
        }, 2000);
    },
    clearTitleBar: function(win){
        if(this.timeoutId){
            clearInterval(this.timeoutId);
            document.title = titleText;
        }
    }
 });

/*  WtfChatWIn: End */

/*****************************************************************
 function to replace the smiley definition by smiley image
 defined smileys are-
 :) , :P ,:D ,:X,;) , :(
 ******************************************************************/
function smiley(tdiv, emoticon){
    tdiv.innerHTML = tdiv.innerHTML.replace(emoticon, '<img src=../../images/smiley' + (smileyStore.indexOf(emoticon) +1) + '.gif style=display:inline;vertical-align:text-top;></img>');
}


/********************this is nop******/
