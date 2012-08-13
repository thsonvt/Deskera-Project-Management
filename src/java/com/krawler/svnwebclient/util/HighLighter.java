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

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import com.uwyn.jhighlight.renderer.CppXhtmlRenderer;
import com.uwyn.jhighlight.renderer.JavaXhtmlRenderer;
import com.uwyn.jhighlight.renderer.Renderer;
import com.uwyn.jhighlight.renderer.XmlXhtmlRenderer;

public class HighLighter {
	protected Map rendererMappings = new TreeMap();
	protected String encoding = "UTF-8";

	protected static HighLighter highlighter = null;

	protected HighLighter() {
		this.rendererMappings.put("java", new JavaXhtmlRenderer());
		this.rendererMappings.put("html", new XmlXhtmlRenderer());
		this.rendererMappings.put("xml", new XmlXhtmlRenderer());
		this.rendererMappings.put("jsp", new XmlXhtmlRenderer());
		this.rendererMappings.put("c", new CppXhtmlRenderer());
		this.rendererMappings.put("cpp", new CppXhtmlRenderer());
		this.rendererMappings.put("h", new CppXhtmlRenderer());
		this.rendererMappings.put("hpp", new CppXhtmlRenderer());
	}

	public static HighLighter getHighLighter() {
		if (highlighter == null) {
			highlighter = new HighLighter();
		}
		return highlighter;
	}

	public String getColorizedContent(String content, String url)
			throws IOException {
		String fileExtension = this.getExtension(url);
		if (fileExtension == null) {
			content = HtmlUtil.encodeWithSpace(content);
			return content;
		}
		Renderer renderer = this.getRenderer(fileExtension);
		if (content == null) {
			return "";
		}
		if (renderer == null) {
			content = HtmlUtil.encodeWithSpace(content);
			return content;
		}
		return renderer.highlight(null, content, encoding, true).substring(73)
				.replaceAll("<br />", "").trim();
	}

	public String getColorizedContent(byte[] content, String fileExtension)
			throws IOException {
		return this.getColorizedContent(new String(content), fileExtension);
	}

	public void setEncoding(final String encoding) {
		this.encoding = encoding;
	}

	protected Renderer getRenderer(String fileExtension) {
		return (Renderer) this.rendererMappings
				.get(fileExtension.toLowerCase());
	}

	protected String getExtension(String url) {
		String res = "";
		int index = url.lastIndexOf(".");
		if (index != -1) {
			res = url.substring(index + 1);
		}
		return res;
	}
}
