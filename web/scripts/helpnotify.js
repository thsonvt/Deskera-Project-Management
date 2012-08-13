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
Wtf.QuickHelp=function(){
    return{
        register:function(tt,scope){
            Wtf.each(tt,function(element){
                Wtf.QuickHelp.attachHelpTip(element,scope,"beforeclick");
        });
        },
        attachHelpTip:function(element,scope,event){
       if((typeof element == 'object') && (typeof element.handler == 'function')){
            element.on(event,function(b,e){
                if(helpmode){
                var sd = Wtf.getCmp("help_mode_tip");
                if(sd){
                    sd.destroy();
                }
                sd=new Wtf.Tip({
                    closable:true,
                    id:'help_mode_tip',
                    width:250,
                    title:typeof element.tooltip == "object" ? element.tooltip.title : element.tooltip,
                    items:new Wtf.Panel({
                   buttonAlign:'right',
                   border:false,
                    buttons:[new Wtf.Button({
                        text:WtfGlobal.getLocaleText('lang.go.text'),
                        handler:element.handler,
                        scope:scope
                    })],
                    html:element.detailTip
                
            
                    }),
                    target:element
                })
                var el = Wtf.get(element.id) != null ?  Wtf.get(element.id) :  Wtf.get(element.el);
                
                //alert("left:"+left+",w:"+w+",top:"+top+",h:"+h);
                var al = "";
                
                 sd.show();
                 var w = Wtf.getBody().getWidth();
                var h = Wtf.getBody().getHeight();
                var top = el.getTop()+el.getHeight()+sd.getEl().getHeight()+4; 
                var left  = el.getLeft()+el.getWidth()+sd.getEl().getWidth()+4;
                var x1 =0;
                var y1=0;
                if(left>=w){
                    al="tr-tr";
                    x1 = -25;
                }
                if(top>=h){
                    al="tl-tl";
                    y1= -sd.getEl().getHeight();
                }
                 sd.getEl().alignTo(el,al,[x1,y1]);
                 
                
                }
                return !helpmode;
                
            });
            }else if(element.menu){
                Wtf.each(element.menu.items.items,function(element1){
                        Wtf.QuickHelp.attachHelpTip(element1,scope,"click");    
                });
            }
        }
    }
}();
