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
package com.krawler.common.util;

public interface KWLErrorMsgs {
        String rsFalse = "{\"data\":[{\"result\":\"false\"}]}";
        String rsTrue = "{\"data\":[{\"result\":\"true\"}]}";
        String rsDataFailure = "{\"data\": \"Failure\"}";
        String rsSuccessFalse = "{\"success\": false}";
        String rsSuccessTrue = "{\"success\": true}";
        String rsSuccessErrTrue = "{'success':'true'}";
        String rsSuccessErrFalse = "{'success':'false'}";
        String rsSuccessDuplicate = "{'success':'Duplicate entry'}";
        String rsFalseErrMsg = "{\"success\":\"false\"},{\"errmsg\":\"";
        String rsFalseFailure = "{\"success\":\"false\",\"data\": \"Failure\"}";
        String rsTrueData = "{\"success\":\"true\",\"data\": \"";
        String rsFalseData = "{\"success\":\"false\",\"data\": \"";
        String rsFalseErr = "{\"success\":\"false\",\"errormsg\": \"";
        String rsTrueNoData = "{\"success\":\"true\",\"data\":\"\"}";
        String rsboolTruedata = "{\"success\":true,\"data\":\"\"}";
        String rsSuccessFailure = "{\"success\":failure}";
        String rsSuccessFalseNoData = "{\"success\":false,\"data\":\"\"}";
        String rsValidTrueSuccessTrueData = "{\"success\":true,\"valid\":true,\"data\":\"";
        String rsValidTrueSuccessFalseErr = "{\"success\":false,\"valid\":true,\"data\":\"";
        String rsValidTrueSuccessTrue = "{\"success\":true,\"valid\":true,\"data\":\"\"}";
        String rsValidTrueSuccessFalse = "{\"success\":false,\"valid\":true,\"data\":\"\"}";
        String rsFalseNoData = "{\"success\":false,\"data\":[]}";
        String rsSuccessTrueNoData = "{\"success\":true,\"data\":[]}";

    	String errFileInfo = "Unable to Get FileInfo:";
        String errSearchData = "Unable to Search Data :";
        String errSearchIndex = "Unable to Search Index :";
        String errAddTag = "Unable to Add Tag to file:";
        String errCheckingFile = "Problem While Checking file :";
        String errInsertTag = "Unable to Insert Tag :";
        String errLoadingData = "Problem While Loading Data :";
        String errSetStatusFile = "Unable to Set Status of File:";
        String errFillDocGrid = "Unable to fill Doc Grid  :";
        String errDeleteFileTag = "Unable to Delete file Tag :";
        String errEditFileTag  = "Unable to Edit File Tag :";
        String errInsert = "Error In Insert:";
        String errTemplate = "Failed to insert Template in Database.";
        String errGetConnections = "Unable To Get Connections:";
        String errGetConnectionsRightGrid = "Unable To Get Connections Of Right Grid :";
        String errSessionInvalid = "Session Invalidated";
        String errDataOneModerator = "{\"data\":[{\"result\":\"true\",\"value\":\"The project should have atleast one moderator\"}]}";
        String errSuccessFalseResourceExists = "{\"success\": false, \"error\": \"The Resource Type Already Exists\"}";
        String errSuccessFalseResourceInvalid = "{\"success\": false, \"error\": \"Error occurred while processing data\"}";
        String errUsernameEmailNoBlank = "{\"success\":false,\"data\":\" User name/email field can not be left blank. \"}";
        String errUseridAlreadyExists = "{\"success\":false,\"data\":\" The userid already exists. \"}";
        String errEmailidAlreadyRegistered = "{\"success\":false,\"data\":\" This emailid is already registered. \"}";
        String errPasswordMinMaxLength = "{\"success\":true,\"data\":\"Password length should be between 4 and 32 characters.\"}";
        String errProccessingData = "Error occurred while processing data";

        String exinsertBuffer = "{error:\" Exception in insertBuffer()\"}";
        String exgetResourceRelatedTask = "{error:\" Exception in getResourceRelatedTask()\"}";
        String exgetminmaxProjectDate = "{error:\" Exception in getminmaxProjectDate()\"}";
        String exgetNWWeekAndCompHolidays = "{error:\" Exception in getNWWeekAndCompHolidays()\"}";
        String exgetNonWorkWeekdays = "{error:\" Exception in getNonWorkWeekdays()\"}";
        String exgetCmpHolidaydays = "{error:\" Exception in getCmpHolidaydays()\"}";
        String exSuccessFail = "{'data':[{'Success':'Fail','Exception'}]}";
        String exMesageEmailUserInfo = "Mesage Exception While Email User Info";
        String exConfigurationEmailUserInfo = "Configuration Exception While Email User Info";

        String msgTempPassword = "Hi %s,\n\nHere is your temporary password: %s\n\nPlease change your password when you login next time. Please keep your password safe to prevent unauthorized access.\n\n\nYou can login at:\n%s\n\n\nSee you back on Deskera!\n\n - The Deskera Team\n";
        String msgMailPassword = "<html><head><title>Deskera - Account Password</title>" +
                "<style type='text/css'>a:link, a:visited, a:active {color: #03C;}" +
                "body {font-family: Arial, Helvetica, sans-serif;	color: #000; font-size: 13px;}" +
                "</style><body>" +
                "<div><p>Hi <strong>%s</strong>,</p>" +
                "<p>Here is your temporary password: <strong>%s</strong></p>" +
                "<p>Please change your password when you login next time. Please keep your password safe to prevent unauthorized access.</p><br>" +
                "<p>You can login at: <a href='%s'>%s</a></p><br/><br/><p>See you back on Deskera!</p><p> - The Deskera Team</p>" +
                "</div></body></html>";
        String msgMailSubjectPassword = "[Deskera] Your Account Password";
        String msgWelcomeDeskera = "[Deskera] Welcome to Deskera";
        String msgFurtherInstr = "For further instructions, please check your email at ";
        String msgIncorrectDetails = "Invalid Username or Email";
        String msgMailInvite = "Hi %s,\n\n%s has created an account for you at Deskera.\n\nDeskera is a Project Management Tool which you'll love using.\n\nUsername: %s \nPassword: %s\n\nYou can log in at:\n%s\n\n\nSee you on Deskera\n\n - %s and The Deskera Team";
        String newUserAdminPasswordPlain = "Hi %s,\n\nA new account has been created for you at Deskera.\n\nDeskera is a Project Management Tool which you'll love using.\n\nUsername: %s \nPassword: %s\n\nYou can log in at:\n%s\n\nSee you on Deskera\n\n - The Deskera Team";
        String newUserAdminPlain = "Hi %s,\n\nA new account has been created for you at Deskera.\n\nDeskera is a Project Management Tool which you'll love using.\n\nUsername: %s \n\nYou can log in at:\n%s\n\nSee you on Deskera\n\n - The Deskera Team";
        String msgWelcomeSigningAddress = "Hi %s,\n\nWelcome to Deskera and thanks for signing up!\n\n\nBookmark your login page - %s\n\nThis is the address where you'll log in to your account for now on\n\n - The Deskera Team\n";
        String msgMailInviteUsernamePassword = "<html><head><title>Deskera - Your Deskera Account</title></head>" +
            "<style type='text/css'>a:link, a:visited, a:active {color: #03C;}" +
            "body {font-family: Arial, Helvetica, sans-serif;color: #000;font-size: 13px;}</style>" +
            "<body><div><p>Hi <strong>%s</strong>,</p>" +
            "<p>%s has created an account for you at Deskera, for the company %s</p>" +
            "<p>Deskera is a Project Management Tool which you'll love using.</p>" +
            "<p>Username: <strong>%s</strong></p>" +
            "<p>Password: <strong>%s</strong></p>" +
            "<p>You can log in to Deskera at: <a href=%s>%s</a>.</p><br/><br/>" +
            "<p>See you on Deskera!</p><p> - %s and The Deskera Team</p>" +
            "</div></body></html>";
        String newUserAdminPasswordHTML = "<html><head><title>Deskera - Your Deskera Account</title></head>" +
            "<style type='text/css'>a:link, a:visited, a:active {color: #03C;}" +
            "body {font-family: Arial, Helvetica, sans-serif;color: #000;font-size: 13px;}</style>" +
            "<body><div><p>Hi <strong>%s</strong>,</p>" +
            "<p>A new account has been created for you at Deskera, for the company %s</p>" +
            "<p>Deskera is a Project Management Tool which you'll love using.</p>" +
            "<p>Username: <strong>%s</strong></p>" +
            "<p>Password: <strong>%s</strong></p>" +
            "<p>You can log in to Deskera at: <a href=%s>%s</a>.</p><br/><br/>" +
            "<p>See you on Deskera!</p><p> The Deskera Team</p>" +
            "</div></body></html>";
        String newUserAdminHTML = "<html><head><title>Deskera - Your Deskera Account</title></head>" +
            "<style type='text/css'>a:link, a:visited, a:active {color: #03C;}" +
            "body {font-family: Arial, Helvetica, sans-serif;color: #000;font-size: 13px;}</style>" +
            "<body><div><p>Hi <strong>%s</strong>,</p>" +
            "<p>A new account has been created for you at Deskera, for the company %s</p>" +
            "<p>Deskera is a Project Management Tool which you'll love using.</p>" +
            "<p>Username: <strong>%s</strong></p>" +
            "<p>You can log in to Deskera at: <a href=%s>%s</a>.</p><br/><br/>" +
            "<p>See you on Deskera!</p><p> The Deskera Team</p>" +
            "</div></body></html>";

        String adminEmailId = "admin@deskera.com";
        String notificationSenderId = "no-reply@deskera.com";
        String dataWelcome = "{\"count\":[0],\"data\":[{\"project\":{\"task\":[{\"name\":\"\"}],\"projectname\":\"\","
                              +"\"noprojects\":\""
                              +"Welcome! There seems to be no projects that you are associated with."
                              +" Alternatively there are no new or due tasks assigned to you."
                              +" Once you are added to a project or are assigned tasks,"
                              +" please visit this section to get an overview of new or due tasks that you are working on. "
                              +"Thank You."
                              +"\"}}]}";
        String dataErrorConn = "{\"count\":[0],\"data\":[{\"project\":{\"task\":[{\"name\":\"\"}],\"projectname\":\"\","
                              +"\"noprojects\":\""
                              +"An error occurred while connecting to server"
                              +"\"}}]}";

        String mailSystemFooter = "<br><br>-----------------This is a system generated message-----------------";
        String passwordNotChanged = " However, password could not be changed";
        String passwordMinMaxLength = " However, password length should be between 4 and 32 characters.";
        String percentOrderStartDate = "percentcomplete = 0 order by startdate";
        String percentOrderTaskIndex = "percentcomplete = 100 order by taskindex";
        String percent1to99OrderTaskIndex = "percentcomplete between 1 and 99 order by taskindex";
        String errEmailidForExtContacts = "This emailid is already there in your contact list";
        String errOpFail = "Operation Failed";
        String errTAskNameTrun = "Some of the task name has been truncated, as upto 512 characters are allowed. You can use task notes to describe them further.";
        String errMsgImportBasecamp = "Problem occurred while importing projects from base camp";
        String errMsgImportMPX = "Problem occurred while importing file. Please try again.";
        String errMsgImportMPXInvalidFileFormat = "There is some problem with the file format you are trying to import.";
//        String errMsgFileFormatBAsecamp = "Error occurred while parsing file. Please uplaod proper xml file";
        String errMsgFileFormatBAsecamp = "Sorry we could not read this file. Please upload the original .xml file which you exported from Basecamp";
        String errPasswordDoNotMatch = "{\"success\":true,\"data\":\"The entered values for password and retype password do not match.\"}";
        String errPasswordOld = "{\"success\":true,\"data\":\"Old password entered is incorrect.\"}";

        String importCSVInvFormat = "One of the fields is a required field and is blank or is incorrectly formatted.";
        String importCSVInvDateFormat = "Either start date or end date is wrong or is in incorrect format.";
        String importCSVTaskFailure = "Failed while creating task.";
        String iLPredNotFound = "Predecessor task not found. Check specified predecessor index.";
        String iLPredInvalidDate = "Predecessor task has invalid date. it may be blank or ending after successor start.";
        String iLParentNotFound = "Parent task does not exist. Check specified parent index.";
        String iLParentInvalidIndex = "Parent index is invalid. It must be either before or immediately before current task.";
        String iLExRes = "Error occurred while saving task resource.";
        String iLExPred = "Error occurred while saving task predecessor";
        String iLExParent = "Error occurred while saving task parent.";
        String iLExBase = "Error occurred while creating baseline.";
        
        String iLConInvalidEmail = "Email-ID is either blank or is invalid.";
        String iLConExists = "Contact already exists and duplicates were not allowed.";
        String iLConInvalidMapping = "CSV discarded due to incorrect data/mapping.";
        String iLConNoNewCont = "There are no new contacts to import and duplicates contacts were not allowed.";
        String iLConDiscarded = "Some rows have been discarded.";
        String iLConImportSuccess = "Contacts imported successfully.";

        String calIOEx = "pm.project.calendar.ical.IOEx";
        String calURLEx = "pm.project.calendar.ical.URLEx";
        String calFileEx = "pm.project.calendar.ical.FileEx";
        String calValidationEx = "pm.project.calendar.ical.ValidationEx";
        String calParseEx = "pm.project.calendar.ical.ParseEx";
        String calGenEx = "pm.project.calendar.ical.GenEx";
        String calJSONEx = "pm.project.calendar.ical.JSONEx";
        
        String taskNotificationSubject = "Task Alerts";
}
