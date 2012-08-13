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
package com.krawler.svnwebclient.decorations;

import javax.servlet.http.HttpServletRequest;

public interface IRevisionDecorator {
	/**
	 * @return Title for this decorator section on revision details screen. The
	 *         title should be plain text, it will be formatted according to the
	 *         UI guidelines automatically.
	 */
	String getSectionTitle();

	/**
	 * @param revision
	 * @param request
	 *            The request, which can be used for decorator to cache some
	 *            data in the session.
	 * @return The content of this decorator section on revision details screen.
	 *         It will be rendered under the title. It should be well formed
	 *         HTML and suitable for inserting into table cell. Can be just
	 *         plain text, if no formating is necessary. <br>
	 *         Sample:
	 * 
	 * <pre>
	 *             My revision decoration, with &lt;b&gt;BOLD&lt;/b&gt; text inside.
	 * </pre>
	 */
	String getSectionContent(String revision, HttpServletRequest request);

	/**
	 * @param revision -
	 *            revision of resource
	 * @param request
	 *            The request, which can be used for decorator to cache some
	 *            data in the session.
	 * @return true - if decorations are present for this revision; false -
	 *         otherwise
	 */
	boolean isRevisionDecorated(String revision, HttpServletRequest request);

	/**
	 * Returns the information for decoration which will be rendered next to the
	 * revision number on file/revision listings.
	 * 
	 * @param revision
	 * @param request
	 *            The request, which can be used for decorator to cache some
	 *            data in the session.
	 * @return IIconDecoration implemetation
	 */
	IIconDecoration getIconDecoration(String revision,
			HttpServletRequest request);
}
