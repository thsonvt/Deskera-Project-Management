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

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatter {
	public static final int ABSOLUT = 0;
	public static final int RELATIVE = 1;

	protected static final String ABSOLUT_FORMAT = "dd.MM.yyyy HH:mm:ss";

	public static String format(Date date) {
		return DateFormatter.format(date, DateFormatter.RELATIVE);
	}

	public static String format(Date date, int type) {
		if (date == null) {
			return "";
		}
		if (type == DateFormatter.ABSOLUT) {
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					DateFormatter.ABSOLUT_FORMAT);
			return dateFormat.format(date);
		} else if (type == DateFormatter.RELATIVE) {
			return DateFormatter.getAge(date).toString();
		} else {
			return date.toString();
		}
	}

	protected static Age getAge(Date date) {
		ElapsedTime elapsedTime = new ElapsedTime(date, new Date());
		return new Age(elapsedTime.getYears(), elapsedTime.getMonths(),
				elapsedTime.getWeeks(), elapsedTime.getDays(), elapsedTime
						.getHours(), elapsedTime.getMinutes());
	}
}
