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

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.OfficeException;

/**
 *
 * @author Abhay
 */
public class ConvertFileToSwf {

    private static final Logger logger = Logger.getLogger(ConvertFileToSwf.class.getName());
    private boolean deleteTheFile = true;

    public String convertToPDF(File inputFile, OfficeDocumentConverter converter) throws OfficeException {

        String filePath = inputFile.getAbsolutePath();

        File outputFile = null;

        outputFile = new File(filePath.substring(0, filePath.lastIndexOf(".") + 1) + "pdf");

        if (!inputFile.getName().contains(".pdf")) {
            converter.convert(inputFile, outputFile);
        } else {
            deleteTheFile = false;
        }
        return outputFile.getAbsolutePath();
    }

    public String convertToSwf(String inputPdfAbsFilePath, String outputAbsFilePath) throws IOException {

        String inputFilePath = inputPdfAbsFilePath;

        if (!inputPdfAbsFilePath.contains(".swf")) {

            inputPdfAbsFilePath = "pdf2swf -o " + outputAbsFilePath + " " + inputPdfAbsFilePath;
            System.out.println("\nExecuted this command: " + inputPdfAbsFilePath);

            synchronized (this) {
                Runtime runtime = Runtime.getRuntime();
                Process p = runtime.exec(inputPdfAbsFilePath);
                try {
                    if (p.waitFor() == 0) {

                        File outputFile = new File(outputAbsFilePath);
                        if (!outputFile.exists()) {
                            outputAbsFilePath = "";
                        }
                    }
                    File f = new File(inputFilePath);
                    if (f.exists() && deleteTheFile) {
                        f.delete();
                    }
                } catch (InterruptedException ex) {
                    logger.log(Level.SEVERE, "Interrupted while converting to SWF", ex);
                }
                p.destroy();
            }
        } else {
            outputAbsFilePath = inputPdfAbsFilePath;
        }
        return outputAbsFilePath;
    }
}
