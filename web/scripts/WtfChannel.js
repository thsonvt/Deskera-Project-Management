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
Wtf.lib.Ajax._queue = [];

/**
 * Stores the number of XMLHttpRequests being processed
 */
Wtf.lib.Ajax._activeRequests = 0;

/**
 * Overwritten so pending XMLHttpRequests in the queue will be removed 
 */
Wtf.lib.Ajax.abort=function(o, callback, isTimeout)
{
    if (this.isCallInProgress(o)) {
        o.conn.abort();
        window.clearInterval(this.poll[o.tId]);
        delete this.poll[o.tId];
        if (isTimeout) {
            delete this.timeout[o.tId];
        }

        this.handleTransactionResponse(o, callback, true);

        return true;
    }
    else {
        
        // check if the connection is pending and delete it
        for (var i = 0, max_i = this._queue.length; i < max_i; i++) {
            if (this._queue[i].o.tId == o.tId) {
                this._queue.splice(i, 1);
                break;
            }
        }
        
        return false;
    }
};

/**
 * Pushes the XMLHttpRequests into the queue and processes the queue afterwards.
 *
 */
Wtf.lib.Ajax.asyncRequest = function(method, uri, callback, postData)
{
    var o = this.getConnectionObject();

    if (!o) {
        return null;
    }
    else {
        
        this._queue.push({
           o : o,
           method: method,
           uri: uri,
           callback: callback,
           postData : postData 
        });

        this._processQueue();
        
        return o;
    }
};

/**
 * Peeks into the queue and will process the first XMLHttpRequest found, if, and only if
 * there are not more than 2 simultaneously XMLHttpRequests already processing.
 */
Wtf.lib.Ajax._processQueue = function()
{
    var to = this._queue[0];
    
    if (to && this._activeRequests < 1) {
        to = this._queue.shift();
        this._asyncRequest(to.o, to.method, to.uri, to.callback, to.postData);
    }
    
};

/**
 * Executes a XMLHttpRequest and updates the _activeRequests property to match the
 * number of concurrent ajax calls.
 */
Wtf.lib.Ajax._asyncRequest = function(o, method, uri, callback, postData)
{
    this._activeRequests++;
    o.conn.open(method, uri, true);
    
    if (this.useDefaultXhrHeader) {
        if (!this.defaultHeaders['X-Requested-With']) {
            this.initHeader('X-Requested-With', this.defaultXhrHeader, true);
        }
    }
    
    if(postData && this.useDefaultHeader){
        this.initHeader('Content-Type', this.defaultPostHeader);
    }
    
     if (this.hasDefaultHeaders || this.hasHeaders) {
        this.setHeader(o);
    }
    
    this.handleReadyState(o, callback);
    o.conn.send(postData || null);    
    
};

/**
 * Called after a XMLHttpRequest finishes. Updates the number of ongoing ajax calls
 * and checks afterwards if there are still requests pending.
 */
Wtf.lib.Ajax.releaseObject = function(o)
{
    o.conn = null;
    o = null;
    
    this._activeRequests--;
    this._processQueue();
};

Wtf.Ajax.requestEx = function(config, scope, successCallback, failureCallback){
    Wtf.Ajax.request({
        method: "POST",
        url: config.url,
        scope: scope,
        params: config.params,
        success: function(request, response){
            var res = null;
            try{
                var restext = request.responseText.trim();
                if(restext && (restext.length > 0)){
                    res = eval( '(' + restext + ')');
                    if(res && res.valid){
                        try{
                            if(successCallback){
                                successCallback.call(this, res.data.trim(), response);
                            }
                        } catch (e){
                            clog(e);
                        }
                    }
                    else if(res && (res.valid == false)){
                        if (res.data && res.data.reason) {
                            signOut(res.data.reason)
                        } else {
                            signOut("timeout");
                        }
                    }
                }
            } catch (e){
                clog(e);
                if(failureCallback)
                    failureCallback.call(this, request, response);                
            }
        },
        failure: function(request, response){
            if(failureCallback)
                failureCallback.call(this, request, response);
        }
    });
}

function clog(e){
    if(console && console.debug && e){
        console.debug(e.toString());
    }
}
