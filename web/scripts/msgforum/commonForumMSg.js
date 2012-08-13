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
Wtf.linkPanel = function(conf){
    Wtf.apply(this, conf);
    Wtf.linkPanel.superclass.constructor.call(this, conf);
}

Wtf.extend(Wtf.linkPanel, Wtf.Panel, {
    border: false,
    baseCls: 'linkPanelBorder',
    initComponent: function(config){
        Wtf.linkPanel.superclass.initComponent.call(this, config);
        this.addEvents({
            "linkClicked": true
        });
    },
    onRender: function(conf){
        Wtf.linkPanel.superclass.onRender.call(this, conf);
        this.link = document.createElement("a");
        this.link.id = "panelLink" + this.id;
        this.link.onclick = this.linkClick.createDelegate(this, [this.link]);
        this.link.innerHTML = this.text;
        this.add(this.link);
    },

    afterRender: function() {
        this.doLayout();
    },

    linkClick: function(){
        this.fireEvent("linkClicked", this.link, this);
    },
    setLinkText: function(text){
        this.link.innerHTML = text;
    },
    hideLink: function(){
        this.link.style.display = 'none';
    },
    showLink: function(){
        this.link.style.display = 'block';
    }
});


/*  Wtf.TopicStore: Start*/
Wtf.TopicStore = function(){
    var Readerrecord =  Wtf.data.Record.create(['Details', 'ID', 'flag','Attachment']);
    Wtf.TopicStore.superclass.constructor.call(this, {
        remoteSort: true,
        proxy: new Wtf.data.HttpProxy({
            url: Wtf.req.prt + 'getDetails.jsp'
        }),
        reader: new Wtf.data.KwlJsonReader({
            root: 'data',
            id: 'threadid'
        },Readerrecord)
    });
};
Wtf.extend(Wtf.TopicStore, Wtf.data.Store, {
    initComponent: function(conf) {
        Wtf.TopicStore.superclass.initComponent.call(this, conf);
        this.events = {'dataloaded': true}
    },
    loadForum: function(topicId, type, flag, userId){
        this.baseParams = {
            topicId: topicId,
            type: type,
            flag: flag,
            userId: userId
        };
        this.on('load', function(){
            this.fireEvent("dataloaded");
        }, this)
        this.load();
    }
});
/*  Wtf.TopicStore: End*/

/*  Wtf.MessagePanel: Start */
Wtf.MessagePanel = function(config){
    Wtf.apply(this, config);
    this.topicstore = new Wtf.TopicStore({});
    this.messageId = null;
    this.topicstore.on("load", this.loadData, this);
    this.topicstore.on("loadexception", this.handleException, this);
    this.messagePanelDetailsTemplate = new Wtf.Template("<div id='{divImg}' style='width:5%; height:100%; float:left;'>", "<img id='{imgDiv}' style='float:left; 'src='../../images/s.gif'></img>", "</div>", "<ul id='{dataDiv}'>", "<li><span class='head-label'>"+WtfGlobal.getLocaleText("lang.subject.text")+":</span>", "<span id='{subjectDiv}'></span>","</li>","<li style='float:left;width:40%'><span id='{msgfrom}' class='head-label'>{fromtext}:</span>", "<span id='{fromDiv}'></span>","</li>" ,"<li style='float:left;width:40%;'><span id='{msgdate}' class='head-label'>{recdtext}:</span>", "<span id='{receivedOn}'></span></li></ul>", "</div>");
    this.messagePanelContentTemplate = new Wtf.Template("<div style='margin:3px;height:90%;width:90%;'>", "<div id='{msgDiv}' style='height: auto;display:block;overflow-x:hidden; overflow-y:auto; margin-left:10px;'>"+WtfGlobal.getLocaleText("pm.personalmessages.emptytext")+"</div></div>");

    Wtf.MessagePanel.superclass.constructor.call(this, {
        id: config.id,
        closable: true,
        split: true,
        border: false,
        bodyStyle: "background:#FFFFFF;border:solid 4px #5b84ba;",
        layout: "border",
        items: [{
            region:'north',
            cls:'messagePanelHeader',
            border: false,
            height:55,
            html: this.messagePanelDetailsTemplate.applyTemplate({
                divImg: "divImg_" + config.id,
                imgDiv: "imgDiv_" + config.id,
                dataDiv: "dataDiv_" + config.id,
                subjectDiv: "subjectDiv_" + config.id,
                msgfrom: "msgfrom_" + config.id,
                fromDiv: "fromDiv_" + config.id,
                msgdate: "msgDate_" + config.id,
                receivedOn: "receivedOn_" + config.id,
                fromtext: WtfGlobal.getLocaleText('pm.personalmessages.from.text'),
                recdtext: WtfGlobal.getLocaleText('pm.personalmessages.receivedon')
            })
        }, {
            region:'center',
            cls:'messagePanelBody',
            border: false,
            html: this.messagePanelContentTemplate.applyTemplate({
                msgDiv: "msgDiv_" + config.id
            })
        }]
    });
    this.addEvents = {
        "UpdateDstore": true,
        "UpdateMailDstore": true
    };
};

Wtf.extend(Wtf.MessagePanel, Wtf.Panel, {
    loadData: function(obj, rec, opt){
    //if(portalmail_grid1.getSelectionModel().getSelections().length == 1){
      if(this.messageId){
            if(this.messageId.match('topic')){
                this.messageId=this.messageId.substring(5);
            }
            var recData = rec[0].data;
            if (this.messageId == recData.ID)
                document.getElementById('msgDiv_' + this.id/*this.getId()*/).innerHTML = Wtf.util.Format.stripScripts((this.parseSmiley(WtfGlobal.URLDecode(decodeURIComponent(recData.Details))))+WtfGlobal.URLDecode(recData.Attachment));//Wtf.util.Format.htmlDecode(parseSmiley(subject(rec[0].data['Details']))));
            if (recData.flag == 'Forum')
                this.fireEvent("UpdateDstore", recData.Details,recData.Attachment,recData.ID);
            else
                (recData.flag == 'Mail')
            this.fireEvent("UpdateMailDstore", recData.Details,recData.Attachment,recData.ID);
            Wtf.get("msgDiv_" + this.id).addListener("click", function(a,e){
                var tar = a.target;
                a.preventDefault();
                if(tar.tagName == 'A' && tar.href.match('http://')){
                  window.open(tar.href,'LinkFromYourFriend');
                  return false;
//                  a.preventDefault();
                }
            }, this);
            }
    },
    parseSmiley:function(str){
        str = unescape(str);
        var tdiv = document.createElement('div');
        var arr = [];
        arr = str.match(/(:\(\()|(:\)\))|(:\))|(:x)|(:\()|(:P)|(:D)|(;\))|(;;\))|(&gt;:D&lt;)|(:-\/)|(:&gt;&gt;)|(:-\*)|(=\(\()|(:-O)|(X\()|(:&gt;)|(B-\))|(:-S)|(#:-S)|(&gt;:\))|(:\|)|(\/:\))|(=\)\))|(O:-\))|(:-B)|(=;)|(:-c)/g);
        if (arr == null) {
            tdiv.innerHTML = str;
        } else {
            var i;
            var smileyStr;
            tdiv.innerHTML = str;
            for (i = 0; i < arr.length; i++) {
                this.smiley(tdiv, arr[i]);
            }
        }
        return tdiv.innerHTML;
    },
    smiley: function(tdiv, emoticon){
        tdiv.innerHTML = tdiv.innerHTML.replace(emoticon, '<img src=../../images/smiley' + (smileyStore.indexOf(emoticon) +1) + '.gif style=display:inline;vertical-align:text-top;></img>');
    },
    setData: function(sub, from, received, imgsrc, senderid,superadmin){
        document.getElementById('subjectDiv_' + this.id).innerHTML = Wtf.util.Format.ellipsis(WtfGlobal.URLDecode(decodeURIComponent(sub)), 100);
        var fromDivString = ((superadmin!=null && superadmin=='true') || from.indexOf(';') !== -1)? from:"<a class=\"attachmentlink\" href='#' onclick='javascript:openprofile(\"" + senderid + "\",\"" + from + "\",\"" + imgsrc + "\")'>" + from + "</a>";
        document.getElementById('fromDiv_' + this.id).innerHTML = "<span style=\"margin:15px;\">"+fromDivString+"</span>";
        document.getElementById('receivedOn_' + this.id).innerHTML = WtfGlobal.dateRendererForServerDate(received);
        var imgdiv = document.getElementById('divImg_' + this.id).getElementsByTagName('img')[0];
        imgdiv.src = imgsrc;
    },

    loadCacheData: function(msgData){
        document.getElementById('msgDiv_' + this.id).innerHTML = Wtf.util.Format.stripScripts(this.parseSmiley(WtfGlobal.URLDecode(decodeURIComponent(msgData))));//Wtf.util.Format.htmlDecode(parseSmiley(unescape(msgData))));
    },

    setData1: function(sub, from, received, text, imgsrc){
        document.getElementById('msgDiv_' + this.id).innerHTML = Wtf.util.Format.htmlDecode(WtfGlobal.URLDecode(decodeURIComponent(text)));
        document.getElementById('subjectDiv_' + this.id).innerHTML = Wtf.util.Format.ellipsis(WtfGlobal.URLDecode(decodeURIComponent(sub)), 100);
        document.getElementById('fromDiv_' + this.id).innerHTML = from;
        document.getElementById('receivedOn_' + this.id).innerHTML = received;
        document.getElementById('divImg_' + this.id).getElementsByTagName('img')[0].src = imgsrc;
    },

    setFromText: function(fromtxt, recdtxt){
        document.getElementById('msgfrom_' + this.id).innerHTML = fromtxt;
        document.getElementById("msgDate_" + this.id).innerHTML = recdtxt;
    },
    clearContents: function(){
        document.getElementById("msgDiv_" + this.id).innerHTML = "";
        document.getElementById('subjectDiv_' + this.id).innerHTML = "";
        document.getElementById('fromDiv_' + this.id).innerHTML = "";
        document.getElementById('receivedOn_' + this.id).innerHTML = "";
        document.getElementById('divImg_' + this.id).getElementsByTagName('img')[0].src = '../../images/s.gif';
    },
    handleException:function() {
        var obj = document.getElementById('msgDiv_' + this.id);
        if(this.topicstore.reader.jsonData === undefined)
            obj.innerHTML="<img src='lib/resources/images/default/window/icon-warning.gif' height='16px' width='16px' />"+WtfGlobal.getLocaleText("pm.personalmessages.errorloadingmsg");
        else
            obj.innerHTML="";
    }
});
/*  Wtf.MessagePanel: End*/

/*  WtfMsgEditor: Start */
Wtf.ReplyWindow = function(config){

    Wtf.apply(this, config);

    this.sendBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText('lang.Send.text'),
        tooltip: {
            title: WtfGlobal.getLocaleText('pm.userprofile.sendmessage'),
            text: WtfGlobal.getLocaleText('pm.Help.sendmsg')
        },
        iconCls: 'pwnd outbox',
        id: 'sendBtnid'
    });
    this.sendBtn.on('click', this.handleSend, this);
    this.addEvents({
        "beforeClosewindow": true,
        "loadsuccess": true
    });
    this.saveBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText('pm.common.save'),
        tooltip: {
            title: WtfGlobal.getLocaleText('pm.personammessage.save.text'),
            text: WtfGlobal.getLocaleText('pm.personalmessage.draft.text')
        },
        iconCls: 'dpwnd saveicon',
        id: 'saveBtnid'
    });
    this.saveBtn.on('click', this.handleSave, this);

    this.closeBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText('lang.close.text'),
        tooltip: {
            title: WtfGlobal.getLocaleText('lang.close.text'),
            text: WtfGlobal.getLocaleText('pm.Help.closepmsg')
        },
        iconCls: 'dpwnd closeicon',
        id: 'closeBtnid'
    });
    this.closeBtn.on('click', this.handleClose, this);


    this.tButnArr = Array();
    this.tButnArr.push(this.sendBtn);
    if (config.composeMail == 1 || config.composeMail == 5) {
        this.composeMailFlag = true;
        this.tButnArr.push(this.saveBtn);
    }
    this.tButnArr.push(this.closeBtn);
    var insertStoreReaderrecord =  Wtf.data.Record.create([{name:'Success'},
        {name:'ID'},
        {name:'Subject'},
        {name:'Received',type:'date'},
        {name:'From'},
        {name:'Details'},
        {name:'Flag'},
        {name:'Image'}]);
    this.insertStore = new Wtf.data.Store({
        proxy: new Wtf.data.HttpProxy({
            url: Wtf.req.prt + "insertNewMsg.jsp"
        }),
        panelObj: this,
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },insertStoreReaderrecord)
    });
    this.insertStore.on("loadexception", function(){
        msgBoxShow(4, 1);
        //Wtf.Msg.alert(WtfGlobal.getLocaleText('lang.error.text'), 'Error occurred while connecting to the server');
        this.ownerCt.remove(this);
        this.sendBtn.enable();
        this.closeBtn.enable();
        this.saveBtn.enable();
    }, this);
    this.insertStore.on("load", this.handleLoad, this);

    this.uvalue = config.uFieldValue;

    this.projFlag = false;
    if (config.projectFlag)
        this.projFlag = true;
    if (this.uvalue == "NewTopic" && config.projectFlag != true) {
        this.uvalue = Wtf.getCmp('sub' + config.id1.substring(4)).ownerCt.comtitle;
    } else if (this.uvalue == "NewTopic" && config.projectFlag) {
        this.uvalue = Wtf.getCmp('subtabpanelcomprojectTabs_' + config.id1.substring(4)).ownerCt.comtitle;
    }
        //Todo for auto complete
/*        this.field1 = new Wtf.form.ComboBox({
            tabIndex:1,
            fieldLabel: config.uLabel,
                        autoCreate:{tag: "input", type: "text", autocomplete: "on"},
            allowBlank: false,
            editable:true,
//                        id:this.parent.id+'country',
            store:Wtf.userStore,
            displayField:'name',
            emptyText:WtfGlobal.getLocaleText('pm.common.UserId'),
                        valueField:'id',
                        maxHeight:200,
            typeAhead: true,
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true,
            width:'95%'
                    }); */
//    this.field1 = new Wtf.form.TextField({
//        fieldLabel: config.uLabel,
//        name: config.uLabel,
////        id: 'WinSendTo',
//        value: this.uvalue,
//        emptyText: WtfGlobal.getLocaleText('pm.common.UserId'),
//        tabIndex:1,
//        disabled: config.tdisabled,
//        width: '95%'/*,
//        allowBlank:false*/
//        });
    var Rec = new Wtf.data.Record.create([
        {name: 'userid'},
        {name: 'username'},
        {name: "fullname"},
        {name: "emailid"},
        {name: 'image'}
    ]);
   var contactStore = new Wtf.data.Store({
        url: '../../admin.jsp?action=0&mode=0',
        id: 'toStore',
        reader: new Wtf.data.KwlJsonReader({
            totalProperty: 'count',
            root: "data"
        },Rec),
        method: 'GET'
   });
   var resultTpl = new Wtf.XTemplate(
        '<tpl for="."><div class="search-item">',
            '<img src="{[this.f(values)]}">',
            '<div><h3><span>{fullname} - ({username})</span></h3><br clear="both">',
            '<div class="search-item-email">{emailid}</div></div>',
        '</div></tpl>', {
        f: function(val){
            if(val.image == "")
                val.image = "../../images/user35.png";
            return val.image;
        },
        scope: this
    });
   this.field1 = new Wtf.form.ComboBox({
        store: contactStore,
        fieldLabel: config.uLabel,
        name: config.uLabelName,
        value: this.uvalue,
        emptyText: WtfGlobal.getLocaleText('pm.user.search.emtytext'),
        tabIndex:1,
        disabled: config.tdisabled,
        cls: 'search-username-combo',
        displayField: 'username',
        typeAhead: false,
        loadingText: 'Searching...',
        pageSize:3,
        hideTrigger:true,
        tpl: resultTpl,
        itemSelector: 'div.search-item',
        minChars: 1,
        onSelect: function(record){ // override default onSelect to do redirect
            var v = this.getValue().toString();
            if(v.indexOf(record.data['username']) == -1){
                if(v.charAt(v.length) == ';')
                    this.setValue(v+record.data['username'] + ';');
                else{
                    var temp = '';
                    if(v.indexOf(';') !== -1)
                        temp = v.substring(0, v.lastIndexOf(';')+1);
                    else
                        temp = '';
                    this.reset();
                    this.setValue(temp + record.data['username'] + ';');
                }
            } else {
                //need to check ; at the end of username, if ; is found then error msg otherwise add ;
                var l=v.indexOf(record.data['username'])+record.data['username'].length;
                if(v.charAt(l)==';'){
                    msgBoxShow(243, 1);
                } else{
                    this.setValue(v+';');
                }
            }
            this.collapse();
            this.focus();
        }
    });

    this.field1.on('beforequery', function(q){
        var qt = q.query.trim();
        var curr_q = qt.substr(qt.lastIndexOf(';')+1);
        curr_q = WtfGlobal.HTMLStripper(curr_q);
        q.query = curr_q;
    }, this)

    this.field2 = new Wtf.form.TextField({
        fieldLabel: config.bLabel,
        name: config.bLabelName,
        value: WtfGlobal.URLDecode(decodeURIComponent(config.bFieldValue)),
        maxLength:255,
        tabIndex:1,
        maxLengthText:WtfGlobal.getLocaleText({key:"pm.personalmessages.errormaxlength", params:[config.bLabel]}),
        width:'95%'
    });
    this.field2.on("change", function(){
        this.field2.setValue(WtfGlobal.HTMLStripper(this.field2.getValue()));
    }, this);

    this.attachPanel = new Wtf.linkPanel({
        text: WtfGlobal.getLocaleText('pm.project.document.attachfile'),
        id: 'attachmentlink',
        height: 20
    });
    this.attachPanel.on("linkClicked", this.Attachfile, this);
    this.hedit = new Wtf.newHTMLEditor({
//        id: 'newHTMLEditor',
        height: 240,
        border: false,
        enableLists: false,
        enableSourceEdit: false,
        enableAlignments: true,
        hideLabel: true
	});
    this.hiddenHtml= new Wtf.form.Hidden({
        name:'ptxt'
    });
    this.hiddentitle= new Wtf.form.Hidden({
        name:'title'
    });
    this.hiddenHtml.hidden = true;
    this.draftSendFlag = false;
    if(config.details!=null)
         this.hedit.setValue(WtfGlobal.URLDecode(escape(config.details)));
    if (config.composeMail == 5) {
       // this.hedit.setValue(unescape(config.details));
        this.draftSendFlag = true;
    }

    this.type = config.type;
    this.sendflag = config.sendFlag;
    this.replyToId = config.replytoId;
    this.userId = config.userId;
    this.groupId = config.groupId;
    this.firstReply = config.firstReply;
    this.fid = config.fid;

   /*this.top = new Wtf.FormPanel({
        frame: false,
        bodyStyle: 'padding:5px 5px 0',
        border: false,
        height: 65,
        layout: 'form',
        items: [{
            layout: 'column',
            border: false,
            items: [{
                columnWidth: 0.99,
                layout: 'form',
                labelWidth: 75,
                border: false,
                defaultType: 'textfield',
                items: [this.field1, this.field2]
          }]
        }]
    });*/
   // var itemarr = [this.field1, this.field2,this.hiddenHtml];
    this.count = 1;
    //itemarr.push(this.attachPanel);
    this.attachheight = 100;
    var disable = config.tdisabled?"disabled":"";
//     name: config.bLabel,
//        value: config.bFieldValue,
    this.resPanel = new Wtf.Panel({
        border :false,
        height:'20px',
       // html:'<input type="text" name="'+ config.uLabel+'" id = "receipttextBox"/>'
       html:'<label style="width:80px !important;float:left;">'+ config.uLabel+':</label><input class="idcheckedEnabled" type="text"  style="width: 85%" '+disable+' value="'+ this.uvalue+'" /><br/>'+
            '<div style="margin-top:2pt; height:18pt;"><label style="width:80px !important;float:left;">'+ config.bLabel+':</label><input type="text" style="width:85%" value="'+config.bFieldValue+'"  name="'+config.bLabel+'" /><br /></div>'/*+

            "<div style='margin-left:54pt'><a id = 'attachmentlink"+this.id+"'class='attachmentlink' href=\"#\" onclick=\"Attachfile(\'"+this.id+"\')\">Attach a file</a></div>"*/
    });
    this.smartInputFloater = {
        xtype : 'panel',
        border: false,
        id:'smartInputFloater12',
        html: '<table id="smartInputFloater" style = "left:64pt;top:20pt"class="floater" cellpadding="0" cellspacing="0"><tr><td id="smartInputFloaterContent" nowrap="nowrap">'
            +'<\/td><\/tr><\/table>'
    };

    this.top = new Wtf.FormPanel({
        frame: false,
        bodyStyle: 'padding:5px 5px 0',
        border: false,
        fileUpload: true,//this.type=="Forum"?true:false,
        height: this.attachheight,
        layout: 'form',
//        id:"containerofmsg",
        // labelWidth: 75,
//       html:'<table id="smartInputFloater" class="floater" cellpadding="0" cellspacing="0"><tr><td id="smartInputFloaterContent" nowrap="nowrap">'
//            +'<\/td><\/tr><\/table>',
        items: [{
                border: false,
                html: '<span class="attachmentInfo">'+ WtfGlobal.getLocaleText("pm.personalmessages.attachments.info") +'</span>'
        },/*this.field3,this.resPanel,*/
        this.field1,/*this.resPanel,*//* autoPanel*/
        this.field2,
        this.attachPanel, /*this.smartInputFloater,*/
        this.hiddenHtml,
        this.hiddentitle]
    });


    Wtf.ReplyWindow.superclass.constructor.call(this, {
        layout: 'border',
        plain: true,
        border: false,
        iconCls: 'pwnd mail_generic',
        items: [{
            region: 'center',
            border: false,
            layout: 'fit',
            items: [this.hedit]
        }, {
            region: 'north',
            layout: 'fit',
            border: false,
            height: this.attachheight,
            items: [this.top]
        }],
        resizable: true,
        tbar: this.tButnArr
    });
    this.on("destroy", function(){
        if(this.hedit.smileyWindow !== undefined)
            this.hedit.smileyWindow.destroy();
        if(Wtf.getCmp("emails" + this.replyToId)!=null){
            Wtf.getCmp("emails" + this.replyToId).topicstore.loadForum(this.replyToId, "-1", "mail","");
        }
    }, this);
    /*this.on("hide", function(){
        if(this.hedit.smileyWindow !== undefined)
            this.hedit.smileyWindow.hide();
    }, this);*/
}

Wtf.extend(Wtf.ReplyWindow, Wtf.Panel, {
    /*afterRender:function(config){
        Wtf.ReplyWindow.superclass.afterRender.call(this, config);
        this.field1.addSmartInputFloater(this.top.id);
          if(!Wtf.StoreMgr.containsKey("userstore")){
                    Wtf.userStore.load();
                    Wtf.userStore.on("load",this.onUserStoreLoad,this);
                    Wtf.StoreMgr.add("userstore",Wtf.countryStore);
        }else{
            this.createCollection();
        }

    },

    onUserStoreLoad:function(){
        this.createCollection();
    },

    createCollection :function() {
        var collection = new Array();
        for(var i=0;i<Wtf.userStore.getCount();i++){
            var rec = Wtf.userStore.getAt(i);
            var userString ='"'+rec.data.name+'" <'+ rec.data.username+'>';
            collection.push(userString);
        }
        this.field1.setCollectionData(collection);
    },*/

    removeFile:function(linkDom, linkPanel){
        this.top.remove(linkPanel, true);
        this.attachheight -=27;
        this.top.ownerCt.setHeight(this.attachheight);
        this.top.setHeight(this.attachheight);
        this.count--;
        if(this.top.items.length < 10)
            this.attachPanel.showLink();
        if(this.top.items.length==6)
            this.attachPanel.setLinkText(WtfGlobal.getLocaleText('pm.project.document.attachfile'));
        this.doLayout();
    },
    Attachfile:function(){
        var textfield = new Wtf.form.TextField({
            fieldLabel: '',
            labelSeparator:'',
            name: 'attach' + (this.count++),
            inputType: 'file'
        });
        var pid = 'fileattach_' + this.count + "_" + this.id;
        var lp = new Wtf.linkPanel({
//            id : pid,
            cls:'fileattachremove',
            border:false,
            text: WtfGlobal.getLocaleText('lang.remove.text'),
            items:textfield
        });
        this.top.insert(this.count, lp);
        lp.on("linkClicked", this.removeFile, this);
        this.attachheight = this.attachheight+27;
        this.top.ownerCt.setHeight(this.attachheight);
        this.top.setHeight(this.attachheight);
        this.attachPanel.setLinkText(WtfGlobal.getLocaleText('pm.personalmessages.attachanother'));
//        if(this.count > 5)
        if(this.top.items.length >= 10)
            this.attachPanel.hideLink();
        this.doLayout();
    },
    handleLoad: function(obj, rec, opt){
        this.fireEvent("loadsuccess", obj, rec, opt);
    	this.sendBtn.enable();
        this.closeBtn.enable();
        this.saveBtn.enable();
        this.ownerCt.remove(this);
        if(this.replyds!=null){
            this.replyds.reload();
        }
    },
    handleClose: function(bobj, e){
        if(this.replyds!=null){
            this.replyds.reload();
        }
        if(this.ownerCt) {
            this.fireEvent("beforeClosewindow", this);
            this.ownerCt.remove(this);
        }
    },
    sendMail:function(reptoName,draftFlag){
        var msg = this.hedit.getValue();
        msg = msg.replace(/<STRONG>/gi,"<b>");
        msg = msg.replace(/<\/STRONG>/gi,"</b>");
        msg = msg.replace(/<em>/gi,"<i>");
        msg = msg.replace(/<\/em>/gi,"</i>");
        this.hiddenHtml.setValue(encodeURIComponent(msg));
        this.hiddentitle.setValue(encodeURIComponent(this.field2.getValue()))
        this.top.form.submit({
            url:Wtf.req.prt + "insertNewMsg.jsp?type="+this.type+"&sendflag="+this.sendflag+"&repto="+ reptoName+"&userId="+this.userId+"&groupId="+ this.groupId+"&firstReply=" +this.firstReply+"&draft="+ draftFlag+"&fid="+ this.fid+"&postid="+ this.postid+"",
            waitMsg :WtfGlobal.getLocaleText("pm.common.sending") + '...',
            scope:this,
            success: function (res, request) {
        //                              alert("Msg has been send");
              if(request.result.data.data[0].Success){
                    if (request.result.data.data[0].Success.match('Success')) {
                        msgBoxShow(141, 0);
                        //Wtf.Msg.alert('Message Sent', 'Message has been sent successfully.');
                        this.handleClose();
                    }
                    else if (request.result.data.data[0].Success.match('Fail')) {
                        if(request.result.data.data[0].msg!=null){
                            msgBoxShow([WtfGlobal.getLocaleText('pm.msg.ERROR'),request.result.data.data[0].msg], 1);
                        }else{
                            msgBoxShow(142, 1);
                        }
                        this.sendBtn.enable();
                        this.closeBtn.enable();
                        this.saveBtn.enable();
//                         this.handleClose();
                            //Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Error occurred while sending message.');
                    }
                    else if (request.result.data.data[0].Success.match('Draft')) {
                            msgBoxShow(147, 0);
                            //Wtf.Msg.alert('Saved', 'Message saved to drafts successfully.');
                            this.handleClose();
                    }
                    else if (request.result.data.data[0].Success.match('userfail')) {
                            msgBoxShow([WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText({key: "pm.personalmessages.errorinvaliduser", params:[request.result.data.data[0].Subject]})], 1);
                             this.handleClose();
                            //Wtf.Msg.alert('Delivery Failure', 'Message to user '+rec[0].data['Subject'] +' is invalid.');
                    }
                 }
            },
            failure: function ( result, request) {
        //                              alert("Msg has not been send");
              this.handleClose();
            }
        },this)

        if(this.mailDS)
            this.mailDS.reload();
    },
    handleSend: function(bobj, edfd){
        //FIXME: msg sending problem from saved drafts
        var draftFlag = 0;
        var reptoName = this.replyToId;
        this.sendBtn.disable();
        this.closeBtn.disable();
        this.saveBtn.disable();
        if (this.composeMailFlag) {
            if (this.draftSendFlag) {
                draftFlag = 2;
            }
            else {
                draftFlag = 3;
           }
            if(reptoName=='-1'){ //this is send to now...
                reptoName = this.field1.getValue();
//                if(reptoName != "")
//                    reptoName = getSelectedUserid(reptoName.trim());

            }
        }
        if(!(this.field1.isValid())||this.field1.getValue().trim()==""){
                msgBoxShow(144, 1);
                //Wtf.Msg.alert('Alert', 'Please specify atleast one recipient.');
                this.sendBtn.enable();
                this.closeBtn.enable();
                this.saveBtn.enable();
                return false;
        }

        if(this.field2.getValue().trim()==""){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText('lang.confirm.text'), WtfGlobal.getLocaleText({key:"pm.personalmessages.confirmempty", params:[this.field2.name]}),function(btn){
                  if (btn == "yes") {
                      this.sendMail(reptoName,draftFlag);
                  }
                  else {
                    this.sendBtn.enable();
                    this.closeBtn.enable();
                    if(this.field1.disabled)
                        this.saveBtn.disable();
                    else
                        this.saveBtn.enable();
                    return false;
                  }
                }, this);
        } else this.sendMail(reptoName,draftFlag);
    },

    handleSave: function(bobj, edfd){
        var reptoName = this.replyToId;
        if(this.replyToId=='-1'){ //this is send to now...
            reptoName = this.field1.getValue();
            var i = reptoName.split(/;/g).length - 1;
            if(i < 0 || i > 1){
                msgBoxShow(281, 1);
                return;
            } else {
                this.closeBtn.disable();
                this.saveBtn.disable();
                this.sendBtn.disable();
            }
//            reptoName = getSelectedUserid(reptoName.trim());
        }
//

        this.hiddenHtml.setValue(encodeURIComponent(this.hedit.getValue()));
//        var sub = this.resPanel.el.dom.childNodes[0].childNodes[0].childNodes[3].childNodes[1].value;
         this.hiddentitle.setValue(encodeURIComponent(this.field2.getValue()))
         this.top.form.submit({
            url:Wtf.req.prt + "insertNewMsg.jsp?type="+this.type+"&sendflag="+this.sendflag+"&repto="+ reptoName+"&userId="+this.userId+"&groupId="+ this.groupId+"&firstReply=" +this.firstReply+"&draft=1"+"&fid="+ this.fid+"&postid="+this.postid+"",
            waitMsg :WtfGlobal.getLocaleText("pm.common.saving")+'...',
            scope:this,
            success: function (res, request) {
//                              alert("Msg has been send");
              if(request.result.data.data[0].Success){
                    if (request.result.data.data[0].Success.match('Success')) {
                        msgBoxShow(141, 0);
                        //Wtf.Msg.alert('Message Sent', 'Message has been sent successfully.');
                        this.handleClose();
                    }
                    else if (request.result.data.data[0].Success.match('Fail')) {
                        if(request.result.data.data[0].msg!=null){
                            msgBoxShow([WtfGlobal.getLocaleText('pm.msg.ERROR'),request.result.data.data[0].msg], 1);
                        }
                        msgBoxShow(142, 1);
                         this.handleClose();
                            //Wtf.Msg.alert(WtfGlobal.getLocaleText('pm.msg.ERROR'), 'Error occurred while sending message.');
                    }
                    else if (request.result.data.data[0].Success.match('Draft')) {
                            msgBoxShow(147, 0);
                            //Wtf.Msg.alert('Saved', 'Message saved to drafts successfully.');
                            this.handleClose();
                    }
                    else if (request.result.data.data[0].Success.match('userfail')) {
                            msgBoxShow([WtfGlobal.getLocaleText('pm.msg.ERROR'), WtfGlobal.getLocaleText({key: "pm.personalmessages.errorinvaliduser", params:[request.result.data.data[0].Subject]})], 1);
                             this.handleClose();
                            //Wtf.Msg.alert('Delivery Failure', 'Message to user '+rec[0].data['Subject'] +' is invalid.');
                    }
                 }
            },
            failure: function ( result, request) {
//                              alert("Msg has not been send");
              this.handleClose();
            }
        },this)

    }
});
/*  WtfMsgEditor: End   */
/*  MsgHandler: Start   */
function openprofile(theId, from, imgsrc){
    mainPanel.loadTab("../../user.html", "   " + theId, from, "navareadashboard", Wtf.etype.user, true);
}

function Attachfile(objid){
    Wtf.getCmp(objid).Attachfile();
}

function removefile(objid,thisid){
    Wtf.getCmp(objid).destroy();
    Wtf.getCmp(thisid).removeFile();
}
