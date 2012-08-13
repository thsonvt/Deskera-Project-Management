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
package com.krawler.esp.handlers;

import com.krawler.svnwebclient.configuration.ConfigurationException;
import com.krawler.svnwebclient.configuration.ConfigurationProvider;

public class StorageHandler {
	public static String GetProfileImgStorePath() throws ConfigurationException {
		return ConfigurationProvider.getInstance().getProfileImagePathBase();
	}

	public static String GetDocStorePath() throws ConfigurationException {
		return ConfigurationProvider.getInstance().getDocStorePath();
	}

	public static String GetDocStorePath(String storeIndex)
			throws ConfigurationException {
		return ConfigurationProvider.getInstance().getDocStorePath(storeIndex);
	}

	public static String GetCurrentStorageIndex() {
		return ConfigurationProvider.getInstance().getCurrentStorageIndex();
	}

	public static String GetDocIndexPath() throws ConfigurationException {
		return ConfigurationProvider.getInstance().getDocIndexPath();
	}

	public static String GetSummaryContext(String defaultContext) {
		return ConfigurationProvider.getInstance().getSummaryContext(
				defaultContext);
	}

	public static String GetSummaryLength(String defaultLength) {
		return ConfigurationProvider.getInstance().getSummaryLength(
				defaultLength);
	}

	public static String GetLmsStorePath() throws ConfigurationException {
		return ConfigurationProvider.getInstance().getLmsStorePath();
	}

	public static String GetLmsStorePath(String storeIndex)
			throws ConfigurationException {
		return ConfigurationProvider.getInstance().getLmsStorePath(storeIndex);
	}
        
        public static String GetFileSeparator(){
            return System.getProperty("file.separator");
        }
        
        public static String GetPathSeparator(){
            return System.getProperty("path.separator");
        }
        
        public static String GetLineSeparator(){
            return System.getProperty("line.separator");
        }
}
