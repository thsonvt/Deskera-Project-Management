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
package com.krawler.svnwebclient.web.resource;

public interface Links {
	String DIRECTORY_CONTENT = "jspfiles/directoryContent.jsp";
	String PICKER_DIRECTORY_CONTENT = "pickerDirectoryContent.jsp";
	String REVISION = "jspfiles/docs/file-releated/revision/revisionDetails.jsp";
	String REVISION_LIST = "jspfiles/docs/file-releated/revision/revisionListMain.jsp";
	String FILE_CONTENT = "jspfiles/docs/file-releated/filecontent/fileContent.jsp";
	String LOGIN = "jspfiles/login.jsp";
	String SWITCH_TO_HEAD = "jspfiles/switchToHead.jsp";
	String FILE_DATA = "jspfiles/docs/file-releated/filecontent/fileData.jsp";
	String FILE_ANNOTATION = "jspfiles/docs/file-releated/filecompare/fileAnnotation.jsp";
	String FILE_DOWNLOAD = "fileDownload.jsp";
	String DIRECTORY_ADD = "jspfiles/directoryAdd.jsp";
	String DIRECTORY_ADD_ACTION = "directoryAddAction.jsp";
	String FILE_ADD = "jspfiles/docs/file-releated/filecompare/fileAdd.jsp";
	String FILE_ADD_ACTION = "jspfiles/docs/grid/temp.jsp";
	String DELETE = "jspfiles/docs/tree/delete.jsp";
	String DELETE_ACTION = "deleteAction.jsp";
	String FILE_UPDATE = "jspfiles/fileUpdate.jsp";
	String FILE_UPDATE_ACTION = "fileUpdateAction.jsp";
	String CHANGE_CONFIRMATION = "changeConfirmation.jsp";
	String DIRECTORY_COMPARE = "jspfiles/directoryCompare.jsp";
	String FILE_COMPARE = "jspfiles/docs/file-releated/filecompare/filecompare.jsp";
	String FILE_COMPARE_DATA = "jspfiles/docs/file-releated/filecompare/fileCompareData.jsp";
	String PATH_SWITCH = "jspfiles/pathSwitch.jsp";
	String CONTENT = "content.jsp";
	String CHANGED_RESOURCE = "changedResource.jsp";
	String RESTRICTED_ACCESS = "jspfiles/restrictedAccess.jsp";
	String RESTRICT_LOGIN = "restrictLogin.jsp";
	String CLEAN_EXTRA_SESSION_ATTRIBUTE = "cleanExtraSessionAttribute.jsp";
	String DOWNLOAD_DIRECTORY = "downloadDirectory.jsp";
	String ERROR = "error.jsp";
	String REVIEW = "review.jsp";
	String GOTO_PATH = "gotoPath.jsp";
	String INVALID_RESOURSE = "invalidResourse.jsp";
	String CHANGE_REVISION_MODE = "changeRevisionMode.jsp";
	String GOTO = "goto.jsp";
}
