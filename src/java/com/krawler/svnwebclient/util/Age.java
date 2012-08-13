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

public class Age {
	protected static final String YEAR = "year";
	protected static final String YEARS = "years";
	protected static final String MONTH = "month";
	protected static final String MONTHS = "months";
	protected static final String WEEK = "week";
	protected static final String WEEKS = "weeks";
	protected static final String DAY = "day";
	protected static final String DAYS = "days";
	protected static final String HOUR = "hour";
	protected static final String HOURS = "hours";
	protected static final String MINUTE = "minute";
	protected static final String MINUTES = "minutes";

	protected String firstDimension = "";
	protected String secondDimension = "";

	public Age(int years, int months, int weeks, int days, int hours,
			int minutes) {
		if (years != 0) {
			this.firstDimension = Integer.toString(years);
			if (years == 1) {
				this.firstDimension += " " + Age.YEAR;
			} else {
				this.firstDimension += " " + Age.YEARS;
			}
			if (months != 0) {
				this.secondDimension = Integer.toString(months);
				if (months == 1) {
					this.secondDimension += " " + Age.MONTH;
				} else {
					this.secondDimension += " " + Age.MONTHS;
				}
			}
		} else if (months != 0) {
			this.firstDimension = Integer.toString(months);
			if (months == 1) {
				this.firstDimension += " " + Age.MONTH;
			} else {
				this.firstDimension += " " + Age.MONTHS;
			}
			if (weeks != 0) {
				this.secondDimension = Integer.toString(weeks);
				if (weeks == 1) {
					this.secondDimension += " " + Age.WEEK;
				} else {
					this.secondDimension += " " + Age.WEEKS;
				}
			}
		} else if (weeks != 0) {
			this.firstDimension = Integer.toString(weeks);
			if (weeks == 1) {
				this.firstDimension += " " + Age.WEEK;
			} else {
				this.firstDimension += " " + Age.WEEKS;
			}
			if (days != 0) {
				this.secondDimension = Integer.toString(days);
				if (days == 1) {
					this.secondDimension += " " + Age.DAY;
				} else {
					this.secondDimension += " " + Age.DAYS;
				}
			}
		} else if (days != 0) {
			this.firstDimension = Integer.toString(days);
			if (days == 1) {
				this.firstDimension += " " + Age.DAY;
			} else {
				this.firstDimension += " " + Age.DAYS;
			}
			if (hours != 0) {
				this.secondDimension = Integer.toString(hours);
				if (hours == 1) {
					this.secondDimension += " " + Age.HOUR;
				} else {
					this.secondDimension += " " + Age.HOURS;
				}
			}
		} else if (hours != 0) {
			this.firstDimension = Integer.toString(hours);
			if (hours == 1) {
				this.firstDimension += " " + Age.HOUR;
			} else {
				this.firstDimension += " " + Age.HOURS;
			}
			if (minutes != 0) {
				this.secondDimension = Integer.toString(minutes);
				if (minutes == 1) {
					this.secondDimension += " " + Age.MINUTE;
				} else {
					this.secondDimension += " " + Age.MINUTES;
				}
			}
		} else if (minutes != 0) {
			this.firstDimension = Integer.toString(minutes);
			if (minutes == 1) {
				this.firstDimension += " " + Age.MINUTE;
			} else {
				this.firstDimension += " " + Age.MINUTES;
			}
		} else {
			this.firstDimension = "0 minutes";
		}
	}

	public String getFirstDimension() {
		return this.firstDimension;
	}

	public String getSecondDimension() {
		return this.secondDimension;
	}

	public String toString() {
		return this.firstDimension + " " + this.secondDimension;
	}
}
