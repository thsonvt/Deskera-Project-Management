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
import com.krawler.database.DbUtil;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.OfficeException;

/**
 *
 * @author Abhay Kulkarni
 */
public class FileConversionThread implements Runnable {

    private static final Logger logger = Logger.getLogger(FileConversionThread.class.getName());
    private File inputFile = null;
    private String docID;
    private OfficeDocumentConverter conveter = null;

    public FileConversionThread(File input, String docID, OfficeDocumentConverter documentConverter) {
        this.inputFile = input;
        this.docID = docID;
        this.conveter = documentConverter;
    }

    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    public String getDocID() {
        return docID;
    }

    public void setDocID(String docID) {
        this.docID = docID;
    }

    public OfficeDocumentConverter getConveter() {
        return conveter;
    }

    public void setConveter(OfficeDocumentConverter conveter) {
        this.conveter = conveter;
    }

    @Override
    public void run() {
        try {
            ConvertFileToSwf converter = new ConvertFileToSwf();
            String pdfPath = "";
            try {
                pdfPath = converter.convertToPDF(inputFile, getConveter());
            } catch (OfficeException e) {
                logger.log(Level.SEVERE, "Could not convert the document :: " + e.getMessage(), e);
            }
            if (!pdfPath.equals("")) {
                String swfPath = pdfPath.substring(0, pdfPath.lastIndexOf(".") + 1).concat("swf");
                converter.convertToSwf(pdfPath, swfPath);
            }
            try {
                DbUtil.executeUpdate("DELETE FROM docsToConvertList WHERE docid = ?", getDocID());
            } catch (ServiceException ex) {
                logger.log(Level.SEVERE, "Failed while updating doc status :: " + ex.getMessage(), ex);
            }

        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Unable to convert " + inputFile.getName() + " :: " + ex.getMessage(), ex);
        }

    }
}
