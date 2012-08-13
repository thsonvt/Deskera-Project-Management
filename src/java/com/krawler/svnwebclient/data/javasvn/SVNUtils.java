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
package com.krawler.svnwebclient.data.javasvn;

import java.io.InputStream;

import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator;

public class SVNUtils {

	public static SVNCommitInfo modifyFile(ISVNEditor editor, String dirPath,
			String filePath, InputStream is, long size) throws SVNException {
		try {
			SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
			editor.openRoot(-1);
			editor.openDir(dirPath, -1);
			editor.openFile(filePath, -1);
			editor.applyTextDelta(filePath, null);

			String chksm = deltaGenerator.sendDelta(filePath, is, editor, true);

			// editor.textDeltaEnd(filePath);
			editor.closeFile(filePath, chksm);

			/*
			 * Closes the directory.
			 */
			editor.closeDir();
			/*
			 * Closes the root directory.
			 */
			editor.closeDir();
			return editor.closeEdit();
		} catch (SVNException e) {
			if (editor != null) {
				try {
					editor.abortEdit();
				} catch (Exception ex) {
				}
			}
			throw e;
		}
	}
}
