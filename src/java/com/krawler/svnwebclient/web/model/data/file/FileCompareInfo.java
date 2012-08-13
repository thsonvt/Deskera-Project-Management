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
package com.krawler.svnwebclient.web.model.data.file;

import java.util.ArrayList;
import java.util.List;

public class FileCompareInfo {
	protected List stopPoints = new ArrayList();
	protected int modifiedItemsCount;
	protected int deletedItemsCount;
	protected int addedItemsCount;

	public static class StopPoints {
		protected int lineNumber;
		protected String frameName;
		protected int type;
		protected int blockPosition;

		public StopPoints(int lineNumber, String frameName, int type,
				int blockPosition) {
			this.lineNumber = lineNumber;
			this.frameName = frameName;
			this.type = type;
			this.blockPosition = blockPosition;
		}

		public String getFrameName() {
			return this.frameName;
		}

		public int getLineNumber() {
			return this.lineNumber;
		}

		public int getBlockPosition() {
			return this.blockPosition;
		}

		public void setBlockPosition(int blockPosition) {
			this.blockPosition = blockPosition;
		}

		public int getType() {
			return this.type;
		}
	}

	public List getStopPoints() {
		return this.stopPoints;
	}

	public void setStopPoint(int lineNumber, String frameName, int type,
			int blockPosition) {
		this.stopPoints.add(new StopPoints(lineNumber, frameName, type,
				blockPosition));
	}

	public FileCompareInfo.StopPoints getLastProperElement(int position,
			int type) {
		FileCompareInfo.StopPoints point = null;
		if (this.stopPoints.size() > 0) {
			point = (FileCompareInfo.StopPoints) this.stopPoints
					.get(this.stopPoints.size() - 1);
			if (!(point.getBlockPosition() == position && point.getType() == type)) {
				point = null;
			}
		}
		return point;
	}

	public int getAddedItemsCount() {
		return this.addedItemsCount;
	}

	public void increaseAddedItemsCount() {
		this.addedItemsCount++;
	}

	public int getDeletedItemsCount() {
		return this.deletedItemsCount;
	}

	public void increaseDeletedItemsCount() {
		this.deletedItemsCount++;
	}

	public int getModifiedItemsCount() {
		return this.modifiedItemsCount;
	}

	public void increaseModifiedItemsCount() {
		this.modifiedItemsCount++;
	}

}
