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
package com.krawler.svnwebclient.data;

import com.krawler.svnwebclient.data.javasvn.DataProvider;

public class DataProviderFactory {

	public static void startup(String userName, String password, String id,
			String url) throws DataProviderException {
		DataProvider.startup(userName, password, id, url);
	}

	public static void shutdown() throws DataProviderException {
		DataProvider.shutdown();
	}

	public static IDataProvider getDataProvider() {
		return new DataProvider();
	}
}
