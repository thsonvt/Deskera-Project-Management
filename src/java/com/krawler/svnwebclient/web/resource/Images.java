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

public interface Images {
	String LOCATION = "../../images/";

	String FILE = Images.LOCATION + "file.gif";
	String DIRECTORY = Images.LOCATION + "directory.gif";

	String UP = Images.LOCATION + "up.gif";
	String BACK = Images.LOCATION + "back.jpg";
	String HEAD = Images.LOCATION + "head.jpg";
	String BROWSE = Images.LOCATION + "browse.gif";

	String REVISION_LIST = Images.LOCATION + "revisions_btn.gif";
	String ADD_DIRECTORY = Images.LOCATION + "add_dir_btn.gif";
	String ADD_FILE = Images.LOCATION + "add_btn.gif";
	String DELETE = Images.LOCATION + "remove_btn.gif";
	String UPDATE = Images.LOCATION + "update_btn.gif";
	String DOWNLOAD = Images.LOCATION + "download_btn.gif";
	String COMPARE = Images.LOCATION + "compareico.gif";
	String ANNOTATE = Images.LOCATION + "blame_btn.gif";

	String ADDED = Images.LOCATION + "added_ico.gif";
	String DELETED = Images.LOCATION + "removed_ico.gif";
	String MODIFIED = Images.LOCATION + "changed_ico.gif";

	String RESOURCE_ADDED = Images.LOCATION + "resource_added.gif";
	String RESOURCE_DELETED = Images.LOCATION + "resource_removed.gif";
	String RESOURCE_MODIFIED = Images.LOCATION + "resource_changed.gif";

	String DIRECTORY_ADDED = Images.LOCATION + "directory_added.gif";
	String DIRECTORY_DELETED = Images.LOCATION + "directory_removed.gif";
	String DIRECTORY_MODIFIED = Images.LOCATION + "directory_changed.gif";

	String FILE_ADDED = Images.LOCATION + "file_added.gif";
	String FILE_DELETED = Images.LOCATION + "file_removed.gif";
	String FILE_MODIFIED = Images.LOCATION + "file_changed.gif";

	String EMPTY = Images.LOCATION + "pixel.gif";

	String ASC = Images.LOCATION + "ascending.gif";
	String DESC = Images.LOCATION + "descending.gif";
	String DOWNLOAD_DIRECTORY = Images.LOCATION + "download_dir_btn.gif";
}
