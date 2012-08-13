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
package com.krawler.esp.Search;

import java.io.IOException;

import javax.servlet.ServletContext;

import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import com.krawler.esp.handlers.StorageHandler;
import com.krawler.svnwebclient.configuration.ConfigurationException;

public class SearchBean {

	/** Creates a new instance of SearchBean */

	public SearchBean() {
	}

	public static SearchBean get(ServletContext app) {
		SearchBean bean = (SearchBean) app.getAttribute("SkyNetSearch");
		if (bean == null) {
			bean = new SearchBean();
			app.setAttribute("SkyNetSearch", bean);

		}
		return bean;
	}

	public Hits skynetsearch(String query, String Field) {
		String indexfield = Field + ":";
		String querytext = indexfield + query.trim();
		Hits result = null;

		try {

			String[] search_fields = { Field };
			String indexPath = StorageHandler.GetDocIndexPath();
			IndexSearcher searcher = new IndexSearcher(indexPath);
			KeywordAnalyzer analyzer = new KeywordAnalyzer();
			Query lucenequery = MultiFieldQueryParser.parse(query,
					search_fields, analyzer);
			// QueryParser queryparse = new QueryParser(query,analyzer);
			// Query lucenequery = queryparse.parse(querytext);
			result = searcher.search(lucenequery);

		} catch (ConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception ex) {
			System.out.println(ex + "");
		}

		return result;
	}

}
