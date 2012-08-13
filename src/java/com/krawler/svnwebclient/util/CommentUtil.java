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
package com.krawler.svnwebclient.util;

public class CommentUtil {
	protected final static String TAG_BR = "<br>";
	protected final static String REGEXP = "\\r?\\n";

	public static String getFirstLine(String comment) {
		String[] mas = comment.split(CommentUtil.REGEXP);
		if (mas != null && mas.length > 0) {
			return mas[0];
		} else {
			return null;
		}
	}

	public static String getTooltip(String comment) {
		return comment.replaceAll(CommentUtil.REGEXP, CommentUtil.TAG_BR);
	}

	public static boolean isMultiLine(String comment) {
		String str = comment.replaceFirst(CommentUtil.REGEXP, " ");
		if (str.equals(comment)) {
			return false;
		} else {
			return true;
		}
	}
}
