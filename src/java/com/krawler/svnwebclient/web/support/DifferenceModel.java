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
package com.krawler.svnwebclient.web.support;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DifferenceModel {
	protected List areas = new ArrayList();

	protected static class DifferenceArea {
		protected int leftIndex;
		protected int leftSize;
		protected int rightIndex;
		protected int rightSize;
		protected List data = new ArrayList();

		public DifferenceArea(int leftIndex, int leftSize, int rightIndex,
				int rightSize) {
			this.leftIndex = leftIndex;
			this.leftSize = leftSize;
			this.rightIndex = rightIndex;
			this.rightSize = rightSize;
		}

		public int getLeftIndex() {
			return this.leftIndex;
		}

		public int getLeftSize() {
			return this.leftSize;
		}

		public int getRightIndex() {
			return this.rightIndex;
		}

		public int getRightSize() {
			return this.rightSize;
		}

		public void addElement(String element) {
			this.data.add(element);
		}

		public List getLeftElements() {
			List ret = new ArrayList();
			int number = this.leftIndex;
			String previousOperation = " ";
			for (Iterator i = this.data.iterator(); i.hasNext();) {
				String line = (String) i.next();
				String operation = line.substring(0, 1);
				line = line.substring(1);
				if (" ".equals(operation)) {
					ret.add(new DifferenceLine(number++,
							DifferenceLine.NOT_CHANGED, line));
					previousOperation = " ";
				} else if ("-".equals(operation)) {
					ret.add(new DifferenceLine(number++,
							DifferenceLine.DELETED, line));
					previousOperation = "-";
				} else if ("+".equals(operation)) {
					if ("-".equals(previousOperation)) {
						DifferenceLine l = (DifferenceLine) ret
								.get(ret.size() - 1);
						l.setType(DifferenceLine.MODIFIED);
					} else {
						ret.add(new DifferenceLine(DifferenceLine.EMPTY_NUMBER,
								DifferenceLine.NOT_CHANGED, ""));
					}
					previousOperation = "+";
				}
			}
			return ret;
		}

		public List getRightElements() {
			List ret = new ArrayList();
			int number = this.rightIndex;
			String previousOperation = " ";
			for (Iterator i = this.data.iterator(); i.hasNext();) {
				String line = (String) i.next();
				String operation = line.substring(0, 1);
				line = line.substring(1);
				if (" ".equals(operation)) {
					ret.add(new DifferenceLine(number++,
							DifferenceLine.NOT_CHANGED, line));
					previousOperation = " ";
				} else if ("-".equals(operation)) {
					ret.add(new DifferenceLine(DifferenceLine.EMPTY_NUMBER,
							DifferenceLine.NOT_CHANGED, ""));
					previousOperation = "-";
				} else if ("+".equals(operation)) {
					if ("-".equals(previousOperation)) {
						DifferenceLine l = (DifferenceLine) ret
								.get(ret.size() - 1);
						l.setLine(line);
						l.setType(DifferenceLine.MODIFIED);
						l.setNumber(number++);
					} else {
						ret.add(new DifferenceLine(number++,
								DifferenceLine.ADDED, line));
					}
					previousOperation = "+";
				}
			}
			return ret;
		}
	}

	public DifferenceModel(String difference) {
		Pattern header = Pattern
				.compile("@@ -(\\d+)(,\\d+)? \\+(\\d+)(,\\d+)? @@");
		String[] lines = difference.split("\\r\\n|\\r|\\n");
		DifferenceArea area = null;
		for (int i = 0; i < lines.length; i++) {

			Matcher matcher = header.matcher(lines[i]);
			if (matcher.matches()) {
				String leftIndex = this.checkGroup(matcher.group(1));
				String leftSize = this.checkGroup(matcher.group(2));
				String rightIndex = this.checkGroup(matcher.group(3));
				String rightSize = this.checkGroup(matcher.group(4));

				area = new DifferenceArea(Integer.parseInt(leftIndex) - 1,
						Integer.parseInt(leftSize), Integer
								.parseInt(rightIndex) - 1, Integer
								.parseInt(rightSize));
				this.areas.add(area);
			} else {
				if (area != null) {
					area.addElement(lines[i]);
				}
			}
		}
	}

	protected String checkGroup(String num) {
		String res = null;
		if (num == null) {
			res = "1";
		} else {
			int index = num.indexOf(",");
			if (index != -1) {
				res = num.substring(1);
			} else {
				res = num;
			}
		}

		return res;
	}

	public List getLeftLines(String left) {
		List ret = new ArrayList();
		String[] lines = left.split("\\r\\n|\\r|\\n");
		int index = 0;
		for (Iterator i = this.areas.iterator(); i.hasNext();) {
			DifferenceArea area = (DifferenceArea) i.next();
			for (int j = index; j < area.getLeftIndex(); j++) {
				ret.add(new DifferenceLine(j, DifferenceLine.NOT_CHANGED,
						lines[j]));
			}
			ret.addAll(area.getLeftElements());
			index = area.getLeftIndex() + area.getLeftSize();
		}
		if (index >= 0) {
			for (int i = index; i < lines.length; i++) {
				ret.add(new DifferenceLine(i, DifferenceLine.NOT_CHANGED,
						lines[i]));
			}
		}
		return ret;
	}

	public List getRightLines(String right) {
		List ret = new ArrayList();
		String[] lines = right.split("\\r\\n|\\r|\\n");
		int index = 0;
		for (Iterator i = this.areas.iterator(); i.hasNext();) {
			DifferenceArea area = (DifferenceArea) i.next();
			for (int j = index; j < area.getRightIndex(); j++) {
				ret.add(new DifferenceLine(j, DifferenceLine.NOT_CHANGED,
						lines[j]));
			}
			ret.addAll(area.getRightElements());
			index = area.getRightIndex() + area.getRightSize();
		}
		if (index >= 0) {
			for (int i = index; i < lines.length; i++) {
				ret.add(new DifferenceLine(i, DifferenceLine.NOT_CHANGED,
						lines[i]));
			}
		}
		return ret;
	}

	public static List getUntouchedLines(String content) {
		List ret = new ArrayList();
		String[] lines = content.split("\\r\\n|\\r|\\n");
		for (int i = 0; i < lines.length; i++) {
			ret
					.add(new DifferenceLine(i, DifferenceLine.NOT_CHANGED,
							lines[i]));
		}
		return ret;
	}
}
