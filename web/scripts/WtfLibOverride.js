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

Wtf.namespace('Wtf', 'Wtf.cal', 'Wtf.proj','Wtf.proj.pr', 'Wtf.proj.resources.type', 'Wtf.common', 'Wtf.docs.com', 'Wtf.portal.profile', 'Wtf.ux', 'Wtf.crm', 'Wtf.reportBuilder','Wtf.cc','Wtf.hm');

function checkUA(pattern){
    ua = navigator.userAgent.toLowerCase();
    return pattern.test(ua);
}
Wtf.isOpera = checkUA(/opera/),
    Wtf.isChrome = checkUA(/chrome/),
    Wtf.isWebKit = checkUA(/webkit/),
    Wtf.isSafari = !Wtf.isChrome && checkUA(/safari/),
    Wtf.isSafari2 =  Wtf.isSafari && checkUA(/applewebkit\/4/), // unique to Safari 2
    Wtf.isSafari3 =  Wtf.isSafari && checkUA(/version\/3/),
    Wtf.isSafari4 =  Wtf.isSafari && checkUA(/version\/4/),
    Wtf.isIE = !Wtf.isOpera && checkUA(/msie/),
    Wtf.isIE7 =  Wtf.isIE && checkUA(/msie 7/),
    Wtf.isIE8 =  Wtf.isIE && checkUA(/msie 8/),
    Wtf.isIE6 =  Wtf.isIE && !Wtf.isIE7 && !Wtf.isIE8,
    Wtf.isGecko = !Wtf.isWebKit && checkUA(/gecko/),
    Wtf.isGecko2 =  Wtf.isGecko && checkUA(/rv:1\.8/),
    Wtf.isGecko3 =  Wtf.isGecko && checkUA(/rv:1\.9/),
    Wtf.isBorderBox =  Wtf.isIE && !Wtf.isStrict,
    Wtf.isWindows = checkUA(/windows|win32/),
    Wtf.isMac = checkUA(/macintosh|mac os x/),
    Wtf.isAir = checkUA(/adobeair/),
    Wtf.isLinux = checkUA(/linux/),
    //Override onDisable and onEnable function to fix bug for paging toolbar button in IE8
    Wtf.override(Wtf.Button, {
        onDisable : function(){
            if(this.el){
                if(!Wtf.isIE6 || !this.text){
                    this.el.addClass("x-item-disabled");
                }
                this.el.dom.disabled = true;
            }
            this.disabled = true;
        },
        onEnable : function(){
            if(this.el){
                if(!Wtf.isIE6 || !this.text){
                    this.el.removeClass("x-item-disabled");
                }
                this.el.dom.disabled = false;
            }
            this.disabled = false;
        }
    });

Wtf.override(Wtf.form.HtmlEditor,{

    createToolbar:function(editor){
        var tipsEnabled = Wtf.QuickTips && Wtf.QuickTips.isEnabled();

        function btn(id, toggle, handler){
            return {
                itemId : id,
                cls : 'x-btn-icon x-edit-'+id,
                enableToggle:toggle !== false,
                scope: editor,
                handler:handler||editor.relayBtnCmd,
                clickEvent:'mousedown',
                tooltip: tipsEnabled ? editor.buttonTips[id] || undefined : undefined,
                overflowText: editor.buttonTips[id].title || undefined,
                tabIndex:-1
            };
        }

        // build the toolbar
        var tb = new Wtf.Toolbar({
            renderTo:this.wrap.dom.firstChild
        });

        // stop form submits
        tb.on('click', function(e){
            e.preventDefault();
        });

        if(this.enableFont && !Wtf.isSafari2){
            this.fontSelect = tb.el.createChild({
                tag:'select',
                cls:'x-font-select',
                html: this.createFontOptions()
            });
            this.fontSelect.on('change', function(){
                var font = this.fontSelect.dom.value;
                this.relayCmd('fontname', font);
                this.deferFocus();
            }, this);

            tb.add(
                this.fontSelect.dom,
                '-'
                );
        }

        if(this.enableFormat){
            tb.add(
                btn('bold'),
                btn('italic'),
                btn('underline')
                );
        }

        if(this.enableFontSize){
            tb.add(
                '-',
                btn('increasefontsize', false, this.adjustFont),
                btn('decreasefontsize', false, this.adjustFont)
                );
        }

        if(this.enableColors){
            tb.add(
                '-', {
                    itemId:'forecolor',
                    cls:'x-btn-icon x-edit-forecolor',
                    clickEvent:'mousedown',
                    tooltip: tipsEnabled ? editor.buttonTips.forecolor || undefined : undefined,
                    tabIndex:-1,
                    menu : new Wtf.menu.ColorMenu({
                        allowReselect: true,
                        focus: Wtf.emptyFn,
                        value:'000000',
                        plain:true,
                        listeners: {
                            scope: this,
                            select: function(cp, color){
                                this.execCmd('forecolor', Wtf.isWebKit || Wtf.isIE ? '#'+color : color);
                                this.deferFocus();
                            }
                        },
                        clickEvent:'mousedown'
                    })
                }, {
                    itemId:'backcolor',
                    cls:'x-btn-icon x-edit-backcolor',
                    clickEvent:'mousedown',
                    tooltip: tipsEnabled ? editor.buttonTips.backcolor || undefined : undefined,
                    tabIndex:-1,
                    menu : new Wtf.menu.ColorMenu({
                        focus: Wtf.emptyFn,
                        value:'FFFFFF',
                        plain:true,
                        allowReselect: true,
                        listeners: {
                            scope: this,
                            select: function(cp, color){
                                if(Wtf.isGecko){
                                    this.execCmd('useCSS', false);
                                    this.execCmd('hilitecolor', color);
                                    this.execCmd('useCSS', true);
                                    this.deferFocus();
                                }else{
                                    this.execCmd(Wtf.isOpera ? 'hilitecolor' : 'backcolor', Wtf.isWebKit || Wtf.isIE ? '#'+color : color);
                                    this.deferFocus();
                                }
                            }
                        },
                        clickEvent:'mousedown'
                    })
                }
                );
        }

        if(this.enableAlignments){
            tb.add(
                '-',
                btn('justifyleft'),
                btn('justifycenter'),
                btn('justifyright')
                );
        }

        if(!Wtf.isSafari2){
            if(this.enableLinks){
                tb.add(
                    '-',
                    btn('createlink', false, this.createLink)
                    );
            }

            if(this.enableLists){
                tb.add(
                    '-',
                    btn('insertorderedlist'),
                    btn('insertunorderedlist')
                    );
            }
            if(this.enableSourceEdit){
                tb.add(
                    '-',
                    btn('sourceedit', true, function(btn){
                        this.toggleSourceEdit(!this.sourceEditMode);
                    })
                    );
            }
        }

        this.tb = tb;
    },
    getDoc : function(){
        return Wtf.isIE ? this.getWin().document : (this.iframe.contentDocument || this.getWin().document);
    },


    getWin : function(){
        return Wtf.isIE ? this.iframe.contentWindow : window.frames[this.iframe.name];
    },
    adjustFont: function(btn){
        var adjust = btn.getItemId() == 'increasefontsize' ? 1 : -1,
        doc = this.getDoc(),
        v = parseInt(doc.queryCommandValue('FontSize') || 2, 10);
        if((Wtf.isSafari && !Wtf.isSafari2) || Wtf.isChrome ){


            if(v <= 10){
                v = 1 + adjust;
            }else if(v <= 13){
                v = 2 + adjust;
            }else if(v <= 16){
                v = 3 + adjust;
            }else if(v <= 18){
                v = 4 + adjust;
            }else if(v <= 24){
                v = 5 + adjust;
            }else {
                v = 6 + adjust;
            }
            v = v.constrain(1, 6);
        } else {
            if(Wtf.isSafari){
                adjust *= 2;
            }
            v = Math.max(1, v+adjust) + (Wtf.isSafari ? 'px' : 0);
        }
        this.execCmd('FontSize', v);

    },

    fixKeys : function(){
        if(Wtf.isIE){
            return function(e){
                var k = e.getKey(), r;
                if(k == e.TAB){
                    e.stopEvent();
                    r = this.doc.selection.createRange();
                    if(r){
                        r.collapse(true);
                        r.pasteHTML('&nbsp;&nbsp;&nbsp;&nbsp;');
                        this.deferFocus();
                    }
                }else if(k == e.ENTER){
                    r = this.doc.selection.createRange();
                    if(r){
                        var target = r.parentElement();
                        this.fireEvent("enterKeyPressed");
                        if(!target || target.tagName.toLowerCase() != 'li'){
                            e.stopEvent();
                            r.pasteHTML('<br />');
                            r.collapse(false);
                            r.select();
                        }
                    }
                }
            };
        }else if(Wtf.isOpera){
            return function(e){
                var k = e.getKey();
                if(k == e.TAB){
                    e.stopEvent();
                    this.win.focus();
                    this.execCmd('InsertHTML','&nbsp;&nbsp;&nbsp;&nbsp;');
                    this.deferFocus();
                }
            };
        }else if(Wtf.isWebKit){
            return function(e){
                var k = e.getKey();
                if(k == e.TAB){
                    e.stopEvent();
                    this.execCmd('InsertText','\t');
                    this.deferFocus();
                }else if(k == e.ENTER){
                    //                    e.stopEvent();
                    this.fireEvent("enterKeyPressed");
                //                    this.deferFocus();
                }
            };
        }
    }
});

Wtf.override(Wtf.tree.MultiSelectionModel, {
    onNodeClick : function(node, e){
        if(e.ctrlKey){
            if(node.isSelected())
                this.unselect(node);
            else
                this.select(node, e, true);
        } else {
            if(!node.isSelected())
                this.select(node, e, false);
        }
    }
})

if(Wtf.isWebKit){
    Wtf.override(Wtf.form.Radio, {
        onClick : function(){
            if(this.el.dom.checked != this.checked || Wtf.isWebKit){
                this.setValue(this.el.dom.checked);
            }
        }
    });
}

Wtf.override(Wtf.form.Field, {
    setFieldLabel : function(text) {
        if (this.rendered) {
            this.el.up('.x-form-item', 10, true).child('.x-form-item-label').update(text);
        }
        this.fieldLabel = text;
    }
});

// This code is used for application to work in IE 9
if ((typeof Range !== "undefined") && !Range.prototype.createContextualFragment){
    Range.prototype.createContextualFragment = function(html)
    {
        var frag = document.createDocumentFragment(),
        div = document.createElement("div");
        frag.appendChild(div);
        div.outerHTML = html;
        return frag;
    };
}

Wtf.override(Wtf.Element, {

    getAttributeNS : function(ns, name){
        return this.getAttribute(name, ns);
    },

    getAttribute: (function(){
        var test = document.createElement('table'),
        isBrokenOnTable = false,
        hasGetAttribute = 'getAttribute' in test,
        unknownRe = /undefined|unknown/;

        if (hasGetAttribute) {

            try {
                test.getAttribute('wtf:qtip');
            } catch (e) {
                isBrokenOnTable = true;
            }

            return function(name, ns) {
                var el = this.dom,
                value;

                if (el.getAttributeNS) {
                    value  = el.getAttributeNS(ns, name) || null;
                }

                if (value == null) {
                    if (ns) {
                        if (isBrokenOnTable && el.tagName.toUpperCase() == 'TABLE') {
                            try {
                                value = el.getAttribute(ns + ':' + name);
                            } catch (e) {
                                value = '';
                            }
                        } else {
                            value = el.getAttribute(ns + ':' + name);
                        }
                    } else {
                        value = el.getAttribute(name) || el[name];
                    }
                }
                return value || '';
            };
        } else {
            return function(name, ns) {
                var el = this.om,
                value,
                attribute;

                if (ns) {
                    attribute = el[ns + ':' + name];
                    value = unknownRe.test(typeof attribute) ? undefined : attribute;
                } else {
                    value = el[name];
                }
                return value || '';
            };
        }
        test = null;
    })()
});
/*
Wtf.override(Wtf.data.DataProxy, {
    destroy: function() {
        this.purgeListeners();
    }
});

Wtf.override(Wtf.data.HttpProxy, {
    destroy: function() {
        // abort current request, if any
        if (this.activeRequest) {
            Wtf.Ajax.abort(this.activeRequest);
        }
        Wtf.data.HttpProxy.superclass.destroy.call(this);
    }
});

Wtf.override(Wtf.data.Store, {
    destroy : function() {
        if (this.storeId || this.id) {
            Wtf.StoreMgr.unregister(this);
        }
        if (this.proxy) {
            Wtf.destroy(this.proxy);
        }
        this.data = null;
        this.purgeListeners();
    }
});

Wtf.override(Wtf.grid.EditorGridPanel, {
    onDestroy: function() {
        var cols = this.getColumnModel().config;
        for(var i = 0, len = cols.length; i < len; i++){
            var c = cols[i];
            Wtf.destroy(c.editor);
        }
        Wtf.grid.EditorGridPanel.superclass.onDestroy.call(this);
    }
});
*/
Wtf.form.Checkbox.override({
    onRender : function(ct, position){
        Wtf.form.Checkbox.superclass.onRender.call(this, ct, position);
        if(this.inputValue !== undefined){
            this.el.dom.value = this.inputValue;
        }
        if(this.tooltip){
            if(typeof this.tooltip == 'object'){
                this.el.dom.setAttribute("wtf:qtip", this.tooltip.text);
            }
        }
        this.wrap = this.el.wrap({
            cls: "x-form-check-wrap"
        });
        if(this.boxLabel){
            this.wrap.createChild({
                tag: 'label', 
                htmlFor: this.el.id, 
                cls: 'x-form-cb-label', 
                html: this.boxLabel
                });
        }
        if(this.checked){
            this.setValue(true);
        } else {
            this.checked = this.el.dom.checked;
        }
    }
})
