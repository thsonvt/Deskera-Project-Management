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
package com.krawler.database;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.krawler.common.util.Log;
import com.krawler.common.util.LogFactory;
import com.krawler.esp.stats.KrawlerPerf;
import com.krawler.esp.stats.RealtimeStatsCallback;

/**
 * Callback <code>Accumulator</code> that returns current values for important
 * database statistics.
 */
class DbStats implements RealtimeStatsCallback {

	private static Log sLog = LogFactory.getLog(DbStats.class);
	private static final Pattern PATTERN_BP_HIT_RATE = Pattern
			.compile("hit rate (\\d+)");

	public Map<String, Object> getStatData() {
		Map<String, Object> data = new HashMap<String, Object>();

		try {
			data.put(KrawlerPerf.RTS_DB_POOL_SIZE, DbPool.getSize());

			// Parse innodb status output
			DbResults results = DbUtil.executeQuery("SHOW INNODB STATUS");
			BufferedReader r = new BufferedReader(new StringReader(results
					.getString(1)));
			String line = null;
			while ((line = r.readLine()) != null) {
				Matcher m = PATTERN_BP_HIT_RATE.matcher(line);
				if (m.find()) {
					data.put(KrawlerPerf.RTS_INNODB_BP_HIT_RATE, m.group(1));
				}
			}
		} catch (Exception e) {
			sLog.warn("An error occurred while getting current database stats",
					e);
		}

		return data;
	}
}
