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

public class IncorrectParameterException extends DataProviderException {
	private static final long serialVersionUID = -8897831942817087245L;

	protected ExceptionInfo info;

	public class ExceptionInfo {
		protected String message;
		protected String description;

		public ExceptionInfo(String message, String description) {
			this.message = message;
			this.description = description;
		}

		public String getDescription() {
			return this.description;
		}

		public String getMessage() {
			return this.message;
		}
	}

	public IncorrectParameterException() {
		super();
	}

	public IncorrectParameterException(String message, String description) {
		super();
		this.info = new ExceptionInfo(message, description);
	}

	public IncorrectParameterException(String message) {
		super(message);
	}

	public IncorrectParameterException(Throwable cause) {
		super(cause);
	}

	public IncorrectParameterException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExceptionInfo getExceptionInfo() {
		return this.info;
	}
}
