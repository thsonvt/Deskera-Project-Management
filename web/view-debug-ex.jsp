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
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <title id="Deskeratitle">Workspace - Deskera</title>
        <script type="text/javascript">
            /*<![CDATA[*/
            //function _cC(){ _u = _gC('username'); if (!_u || _u == "") _r('../../login.html');}
            //function _gC(_c){ _ck = document.cookie; if (_ck.length > 0) { _s = _ck.indexOf(_c + "="); if (_s != -1) { _s = _s + _c.length + 1; _e = _ck.indexOf(";", _s); if (_e == -1) _e = _ck.length; return unescape(_ck.substring(_s, _e));}} return "";}
            function _r(url){ window.top.location.href = url;}
            //_cC();
            /*]]>*/
        </script>
        <!-- css -->
        <link rel="stylesheet" type="text/css" href="../../lib/resources/css/wtf-all.css?v=3"/>
        <link rel="stylesheet" type="text/css" href="../../style/proj.css?v=3"/>
        <link rel="stylesheet" type="text/css" href="../../style/portal.css?v=3"/>
        <link rel="stylesheet" type="text/css" href="../../style/docs.css?v=3"/>
        <link rel="stylesheet" type="text/css" href="../../style/view.css?v=3"/>
        <link rel="stylesheet" type="text/css" href="../../style/cal.css?v=3"/>
        <link rel="stylesheet" type="text/css" href="../../style/wizard.css"/>
        <link rel="stylesheet" type="text/css" href="../../style/dashboard.css"/>
        <link rel="stylesheet" type="text/css" href="../../style/customcolumn.css"/>
        <link rel="stylesheet" type="text/css" href="../../style/gchatwin.css"/>
        <!--[if lte IE 6]>
        <link rel="stylesheet" type="text/css" href="../../style/ielte6hax.css" />
        <![endif]-->
        <!--[if IE 7]>
            <link rel="stylesheet" type="text/css" href="../../style/ie7hax.css" />
        <![endif]-->
        <!--[if gte IE 8]>
            <link rel="stylesheet" type="text/css" href="../../style/ie8hax.css" />
        <![endif]-->
        <style>
            .custom1 .x-progress-inner {
                background:#FFFFFF none repeat scroll 0%;
                height:12px !important;
            }
            .custom1 .x-progress-bar {
                background:transparent url(../../images/custom-bar1.gif) repeat-x scroll 0pt;
                border-bottom:1px solid #EFEFEF;
                border-right:0pt none;
                border-top:1px solid #BEBEBE;
                height:10px !important;
            }
        </style>
        <!-- /css -->
        <link rel="shortcut icon" href="../../images/deskera.png" />
    </head>
    <body>
        <!-- html -->
        <div id="header">
            <div style="float:left;height:25px; overflow:hidden;">
                <!-- values for these params modified " WtfMain-ex.setValidUserVariables(); "-->
               <img id="companyLogo" title = "" src="" alt="" style="height: auto; width: auto; font-size:21px;"/>
                <img src="../../images/project-management-right-logo.gif" alt="pm" style="float:left;margin-left:4px;margin-top:1px;" />
            </div>
            <div class="userinfo" id="userinfo">
                <span id="whoami"></span><br /><a id="signout" href="#" onclick="signOut();">Sign Out</a>&nbsp;&nbsp;<a id="changepass" href="#" onclick="changePassword();">Change Password</a>&nbsp;&nbsp;
            </div>
            <div id="serchForIco"></div>
            <div id="versionLog"><a href="changeVersionLog.html" target="_blank" style="text-decoration: none">&nbsp;.</a></div>
            <div id="searchBar"></div>
            <div id="shortcuts3" class="shortcuts shortcuts23"></div>
            <div id="shortcuts2" class="shortcuts shortcuts23"></div>
            <div id="shortcuts1" class="shortcuts"></div>
            <div id="chatlistcontainer"  style="display:none;cursor:pointer;width:18px;"  class="shortcuts">
                <img id="chatList" style="width:16px;height:16px" src="../../images/Chat.png" onclick="displayChatList()" />
            </div>
            <br/>
        </div>
        <div id="loading-mask" style="width:100%;height:100%;background:#c3daf9;opacity: 0.6;filter: alpha(opacity=60);position:absolute;z-index:20000;left:0;top:0;">&#160;</div>
        <div id="loading" style="border:none !important;background:none;">
            <div id="loading-msg">
            </div>
        </div>
        <!-- /html -->
        <!-- js -->
		<script type="text/javascript" src="../../lib/adapter/wtf/wtf-base.js?v=3"></script>
		<script type="text/javascript" src="../../lib/wtf-all-debug.js?v=3"></script>
		<script type="text/javascript" src="../../scripts/WtfLibOverride.js"></script>
		<script type="text/javascript">
		/*<![CDATA[*/
            var pbar3 = new Wtf.ProgressBar({
				id:'pbar3',
				width:150,
				height:5,
				cls:'custom1',
				renderTo:'loading'
			});
			Wtf.fly('loading-msg').update('Rendering Components...');
			pbar3.wait({
				interval:10,
				duration:50000,
				increment:44
			});
		/*]]>*/
		</script>
        <script type="text/javascript" src="../../props/msgs/messages.js"></script>
        <script type="text/javascript" src="../../props/msgs/wtf-lang-locale.js"></script>
        <script type="text/javascript" src="../../scripts/common/WtfHelp.js"></script>
        <script type="text/javascript" src="../../scripts/WtfGlobal.js"></script>
        <script type="text/javascript" src="../../scripts/WtfPaging.js"></script>
        <script type="text/javascript" src="../../scripts/WtfChannel.js"></script>
        <script type="text/javascript" src="../../scripts/core/WtfBindBase.js"></script>
        <script type="text/javascript" src="../../scripts/core/WtfBind.js"></script>
        <script type="text/javascript" src="../../scripts/WtfImageStore.js"></script>
        <script type="text/javascript" src="../../scripts/WtfSettings.js"></script>
        <script type="text/javascript" src="../../scripts/CustomColumn.js"></script>
        <script type="text/javascript" src="../../scripts/common/WtfButton.js"></script>
        <script type="text/javascript" src="../../scripts/docs/components/WtfQuickSearch.js"></script>
        <script type="text/javascript" src="../../scripts/common/WtfListPanel.js"></script>
        <script type="text/javascript" src="../../scripts/common/WtfSearchBar.js"></script>
        <script type="text/javascript" src="../../scripts/common/WtfPagingPlugin.js"></script>
        <script type="text/javascript" src="../../scripts/WtfPortal.js"></script>
        <script type="text/javascript" src="../../scripts/msgforum/commonForumMSg.js"></script>
        <script type="text/javascript" src="../../scripts/msgforum/forum.js"></script>
        <script type="text/javascript" src="../../scripts/bar chart/amchart.js"></script>
        <script type="text/javascript" src="../../scripts/WtfMain-ex.js"></script>
        <script type="text/javascript" src="../../scripts/helpnotify.js"></script>
        <script type="text/javascript" src="../../scripts/common/WtfScrollPanel.js"></script>
<!--        <script type="text/javascript" src="../../scripts/common/kComponents.js"></script>-->
        <script type="text/javascript" src="../../scripts/WtfTeamProjectView.js"></script>
        <script type="text/javascript" src="../../scripts/common/Select.js"></script>
        <script type="text/javascript" src="../../scripts/common/WtfWizard.js"></script>
        <script type="text/javascript" src="../../scripts/common/WtfDateTimePicker.js"></script>
        <script type="text/javascript" src="../../scripts/common/WtfCreateProject.js"></script>
        <script type="text/javascript" src="../../scripts/common/WtfGChatWindow.js"></script>
        <script type="text/javascript" src="../../scripts/WtfSelectProject.js"></script>
        <script type="text/javascript" src="../../scripts/common/WtfGridAdvancedSearch.js"></script>
        <script type="text/javascript" src="../../scripts/common/WtfGridSummary.js"></script>
        <script type="text/javascript" src="../../scripts/common/WtfGroupSummary.js"></script>
        <script type="text/javascript" src="../../scripts/common/WtfAssignModeratorWindow.js"></script>
        <script type="text/javascript" src="../../scripts/WtfImportLog.js"></script>
        <script type="text/javascript" src="../../scripts/common/KWLTagSearch.js"></script>
        <script type="text/javascript" src="../../scripts/common/WtfCheckList.js"></script>
        <script type="text/javascript">
            /*<![CDATA[*/
            PostProcessLoad = function(){
                setTimeout(function(){Wtf.get('loading').hide(); Wtf.get('loading-mask').fadeOut({remove: true});}, 550);
                Wtf.EventManager.un(window, "load", PostProcessLoad);
            }
            Wtf.EventManager.on(window, "load", PostProcessLoad);
            /*]]>*/
        </script>
        <!-- /js -->
        <div style="display:none;">
            <iframe id="downloadframe"></iframe>
        </div>
        <iframe id="html2convert"  style="width: 1px; height: 1px; visibility: hidden;"></iframe>
    	<div class="dw" id="chatFrame-zIndexed"></div>
    </body>
</html>
