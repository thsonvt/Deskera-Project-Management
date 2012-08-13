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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ElapsedTime {
	protected int years = 0;
	protected int months = 0;
	protected int weeks = 0;
	protected int days = 0;
	protected int hours = 0;
	protected int minutes = 0;
	protected int seconds = 0;

	public ElapsedTime(Date first, Date second) {
		if (!first.equals(second)) {
			GregorianCalendar lower = new GregorianCalendar();
			GregorianCalendar greater = new GregorianCalendar();
			if (first.before(second)) {
				lower.setTime(first);
				greater.setTime(second);
			} else {
				lower.setTime(second);
				greater.setTime(first);
			}

			this.processElapsedYears(lower, greater);
			this.processElapsedMonths(lower, greater);
			this.processElapsedWeeks(lower, greater);
			this.processElapsedDays(lower, greater);
			this.processElapsedHours(lower, greater);
			this.processElapsedMinutes(lower, greater);
			this.processElapsedSeconds(lower, greater);
		}
	}

	public int getYears() {
		return this.years;
	}

	public int getMonths() {
		return this.months;
	}

	public int getWeeks() {
		return this.weeks;
	}

	public int getDays() {
		return this.days;
	}

	public int getHours() {
		return this.hours;
	}

	public int getMinutes() {
		return this.minutes;
	}

	public int getSeconds() {
		return this.seconds;
	}

	protected void processElapsedYears(GregorianCalendar lower,
			GregorianCalendar greater) {
		GregorianCalendar tmp = (GregorianCalendar) lower.clone();
		int dayOfMonth = tmp.get(Calendar.DAY_OF_MONTH);
		do {
			tmp.set(Calendar.DAY_OF_MONTH, 1);
			tmp.add(Calendar.YEAR, 1);
			if (tmp.getActualMaximum(Calendar.DAY_OF_MONTH) >= dayOfMonth) {
				tmp.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			} else {
				tmp.set(Calendar.DAY_OF_MONTH, tmp
						.getActualMaximum(Calendar.DAY_OF_MONTH));
			}
			if (tmp.equals(greater) || tmp.before(greater)) {
				lower.set(Calendar.DAY_OF_MONTH, 1);
				lower.add(Calendar.YEAR, 1);
				if (lower.getActualMaximum(Calendar.DAY_OF_MONTH) >= dayOfMonth) {
					lower.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				} else {
					lower.set(Calendar.DAY_OF_MONTH, lower
							.getActualMaximum(Calendar.DAY_OF_MONTH));
				}
				this.years++;
			}
		} while (tmp.before(greater));
	}

	protected void processElapsedMonths(GregorianCalendar lower,
			GregorianCalendar greater) {
		GregorianCalendar tmp = (GregorianCalendar) lower.clone();
		int dayOfMonth = tmp.get(Calendar.DAY_OF_MONTH);
		do {
			tmp.set(Calendar.DAY_OF_MONTH, 1);
			tmp.add(Calendar.MONTH, 1);
			if (tmp.getActualMaximum(Calendar.DAY_OF_MONTH) >= dayOfMonth) {
				tmp.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			} else {
				tmp.set(Calendar.DAY_OF_MONTH, tmp
						.getActualMaximum(Calendar.DAY_OF_MONTH));
			}
			if (tmp.equals(greater) || tmp.before(greater)) {
				lower.set(Calendar.DAY_OF_MONTH, 1);
				lower.add(Calendar.MONTH, 1);
				if (lower.getActualMaximum(Calendar.DAY_OF_MONTH) >= dayOfMonth) {
					lower.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				} else {
					lower.set(Calendar.DAY_OF_MONTH, lower
							.getActualMaximum(Calendar.DAY_OF_MONTH));
				}
				this.months++;
			}
		} while (tmp.before(greater));
	}

	protected void processElapsedWeeks(GregorianCalendar lower,
			GregorianCalendar greater) {
		GregorianCalendar tmp = (GregorianCalendar) lower.clone();
		do {
			tmp.add(Calendar.WEEK_OF_YEAR, 1);
			if (tmp.equals(greater) || tmp.before(greater)) {
				lower.add(Calendar.WEEK_OF_YEAR, 1);
				this.weeks++;
			}
		} while (tmp.before(greater));
	}

	protected void processElapsedDays(GregorianCalendar lower,
			GregorianCalendar greater) {
		GregorianCalendar tmp = (GregorianCalendar) lower.clone();
		do {
			tmp.add(Calendar.DAY_OF_YEAR, 1);
			if (tmp.equals(greater) || tmp.before(greater)) {
				lower.add(Calendar.DAY_OF_YEAR, 1);
				this.days++;
			}
		} while (tmp.before(greater));
	}

	protected void processElapsedHours(GregorianCalendar lower,
			GregorianCalendar greater) {
		GregorianCalendar tmp = (GregorianCalendar) lower.clone();
		do {
			tmp.add(Calendar.HOUR_OF_DAY, 1);
			if (tmp.equals(greater) || tmp.before(greater)) {
				lower.add(Calendar.HOUR_OF_DAY, 1);
				this.hours++;
			}
		} while (tmp.before(greater));
	}

	protected void processElapsedMinutes(GregorianCalendar lower,
			GregorianCalendar greater) {
		GregorianCalendar tmp = (GregorianCalendar) lower.clone();
		do {
			tmp.add(Calendar.MINUTE, 1);
			if (tmp.equals(greater) || tmp.before(greater)) {
				lower.add(Calendar.MINUTE, 1);
				this.minutes++;
			}
		} while (tmp.before(greater));
	}

	protected void processElapsedSeconds(GregorianCalendar lower,
			GregorianCalendar greater) {
		GregorianCalendar tmp = (GregorianCalendar) lower.clone();
		do {
			tmp.add(Calendar.SECOND, 1);
			if (tmp.equals(greater) || tmp.before(greater)) {
				lower.add(Calendar.SECOND, 1);
				this.seconds++;
			}
		} while (tmp.before(greater));
	}

	public static void main(String[] args) {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"dd.MM.yyyy HH:mm:ss");
			Date first = dateFormat.parse("29.02.2000 23:16:45");
			Date second = dateFormat.parse("28.02.2006 23:16:45");
			ElapsedTime elapsedTime = new ElapsedTime(first, second);
			System.out.println("Years: " + elapsedTime.getYears());
			System.out.println("Month: " + elapsedTime.getMonths());
			System.out.println("Weeks: " + elapsedTime.getWeeks());
			System.out.println("Days: " + elapsedTime.getDays());
			System.out.println("Hours: " + elapsedTime.getHours());
			System.out.println("Minutes: " + elapsedTime.getMinutes());
			System.out.println("Seconds: " + elapsedTime.getSeconds());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
