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
function setLoading2(msg, elm, cls){
    $('#' + elm)[0].innerHTML = msg;
    $('#' + elm)[0].className = cls;
}

function calculate(){
    var nouser = $("#num-user")[0].value;
    if(nouser <= 10){
        $("#num-user")[0].value = 20;
        nouser=20;
    }
    var noproj = $("#num-proj")[0].value;
    if(noproj == ""){
        $("#num-proj")[0].value = 0;
    }
    $("#payable-amount")[0].value = (nouser * usercost) + (noproj * projcost);
}

function validateSubscribe(){
    calculate();
    $("#confirmFieldset")[0].innerHTML = "<div class=\"descDiv\">You have selected STANDARD subscription.<br>Your subscription details are as <br>"+
    "<span class=\"detailsdiv descspan\">NUMBER OF USERS :</span><span class=\"detailsdiv descspan\">"+$("#numUser")[0].value+"</span><br>"+
    "<span class=\"detailsdiv descspan\">NUMBER OF PROJECTS :</span><span class=\"detailsdiv descspan\">"+$("#numProject")[0].value+"</span><br>" +
    "<span class=\"detailsdiv descspan\">NUMBER OF COMMUNITIES :</span><span class=\"detailsdiv descspan\">"+$("#numCommunity")[0].value+"</span><br>" +
    "<span class=\"detailsdiv descspan\">MYDOCUMENTS :</span><span class=\"detailsdiv descspan\">"+$("#numDocs")[0].value+"</span><br>" +
    "<span class=\"detailsdiv descspan\">The total payable amount is :</span><span class=\"detailsdiv descspan\">"+$("#amount")[0].value+"</span></div><br>";
    $("#payment-form").fadeIn('normal');
}
function PayPalRedirect(){
    var amt = 99;
    if($("#amount")[0] != null)
        amt = $("#amount")[0].value;
    $("#formAmount")[0].value = amt;
    $("#subscribeForm")[0].submit();
}

function position(){
    $('#master').css({
        top: ($(window).height() - $('#master').height()) / 2,
        left: ($(window).width() - $('#master').width()) / 2
    })
    
    $('#payment-form').css({
        top: ($(window).height() - $('#payment-form').height()) / 2,
        left: ($(window).width() - $('#payment-form').width()) / 2 - 15
    })
}

$(function(){
    position()
    if (jQuery.browser.version == '6.0') {
        $('#tabs-bottom').css({
            bottom: 11
        })
    }
    $(window).resize(position);
    $('#link-edit').click(function(){
        $('#payment-form').fadeOut('normal');
    })
})
