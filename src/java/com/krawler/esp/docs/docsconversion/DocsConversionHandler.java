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
package com.krawler.esp.docs.docsconversion;

import com.krawler.common.service.ServiceException;
import com.krawler.database.DbPool;
import com.krawler.database.DbPool.Connection;
import com.krawler.database.DbResults;
import com.krawler.database.DbUtil;
import com.krawler.esp.handlers.FileHandler;
import com.krawler.esp.handlers.StorageHandler;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import org.artofsolving.jodconverter.OfficeDocumentConverter;

/**
 * @author Abhay Kulkarni Used for converting Documents to swf from inside the
 * system.
 */
public final class DocsConversionHandler {

    public static final String validFileExt = "^.+(.pdf|.PDF|.htm|.HTM|.html|.HTML|"
            + ".odt|.ODT|.sxw|.SXW|.doc|.DOC|.docx|.DOCX|.xls|.XLS|.xlsx|.XLSX|.ppt|.PPT|.pptx|.PPTX|.rtf|.RTF|.wpd|.WPD|"
            + ".txt|.TXT|.wiki|.WIKI|.ods|.ODS|.sxc|.SXC|.csv|.CSV|.tsv|.TSV|.odp|.ODP|.sxi|.SXI|.odg|.ODG|.svg|.SVG)$";

    public static void convertDocs(String companyID, OpenOfficeServiceResolver resolver) throws ServiceException {

        Connection conn = null;
        List<File> docs = new ArrayList<File>();
        try {
            conn = DbPool.getConnection();

            String q = "SELECT userid FROM users WHERE companyid = ?";
            DbResults rs = DbUtil.executeQuery(conn, q, companyID);
            while (rs.next()) {

                String pathToFolder = StorageHandler.GetDocStorePath() + StorageHandler.GetFileSeparator() + rs.getString("userid");
                File dir = new File(pathToFolder);
                if (dir.isDirectory()) {

                    List<File> ll = Arrays.asList(dir.listFiles());
                    for (File f : ll) {

                        if (f.getName().matches(validFileExt)) {

                            String docID = f.getName().substring(0, f.getName().lastIndexOf("."));
                            if (!FileHandler.isFileExists(docID, ".swf")) {

                                DbResults results = DbUtil.executeQuery(conn, "SELECT 1 FROM docsToConvertList WHERE docid = ?", docID);
                                if (!results.next()) {

                                    docs.add(f);
                                    DbUtil.executeUpdate(conn, "INSERT INTO docsToConvertList VALUES(?)", docID);
                                }
                            }
                        }
                    }
                }
            }
            conn.commit();
        } catch (Exception e) {
            DbPool.quietRollback(conn);
            throw ServiceException.FAILURE("Exception while checking for docs conversion", e);
        } finally {
            DbPool.quietClose(conn);
        }

        try {
            OfficeDocumentConverter converter = resolver.getDocumentConverter();
            ExecutorService es = resolver.getConversionThreadPool();

            if (!docs.isEmpty()) {
                for (File f : docs) {
                    String docID = f.getName().substring(0, f.getName().lastIndexOf("."));
                    Runnable converterRunnable = new FileConversionThread(f, docID, converter);
                    es.execute(converterRunnable);
                }
            }
        } catch (Exception e) {
        }


    }
}
