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
Wtf.ImageStore = function(){
    this.images = [];
};
Wtf.ImageStore.prototype = {
    getImg: function(etype, id, callback, bypassCache, size){
        if (this.images[id] && !bypassCache) {
            callback(this.images[id]);
        }
        else {
            Wtf.Ajax.request({
                scope: this,
                params: {id: id, callback: callback, size: size},
                url: Wtf.req.base + "getImgPath.jsp?etype=" + etype + "&eId=" + id,
                success: function(request, response){
                    var res = request.responseText.trim();
                    if(res != "failure"){
                        if(res == ""){
                            if(etype == 0)
                                response.params.callback("../../images/user200.png");
                            else
                                response.params.callback("../../images/projectimage.jpg");
                        }
                        else{
                            var newImage = request.responseText;//+'?v='+Math.floor(Math.random()*1000);
                            this.setImg(response.params.id, newImage);
                            response.params.callback(newImage);
                        }
                    }
                },
                failure: function(){
                }
            });
        }
    },
	
    setImg: function(index, img){
            this.images[index] = img;
    }
}
Wtf.iStore = new Wtf.ImageStore();
