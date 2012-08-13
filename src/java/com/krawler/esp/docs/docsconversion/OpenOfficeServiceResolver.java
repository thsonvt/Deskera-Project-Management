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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.servlet.ServletContext;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;

/**
 * This class loads and starts open office OS service. (check soffice.bin in the
 * OS process manager) Killing or manually stopping the service won't help. It
 * will stopped only when context is destroyed.
 *
 * @author Abhay Kulkarni
 */
public class OpenOfficeServiceResolver {

    private static final String KEY = OpenOfficeServiceResolver.class.getName();
    private final int nThreads = 10;
    private final OfficeManager manager;
    private final OfficeDocumentConverter converter;
    private final ExecutorService conversionThreadPool;

    public OpenOfficeServiceResolver(ServletContext context) {

        String officeDirPath = context.getInitParameter("OpenOfficePath");

        manager = new DefaultOfficeManagerConfiguration().setOfficeHome(officeDirPath).buildOfficeManager();
        converter = new OfficeDocumentConverter(manager);
        conversionThreadPool = Executors.newFixedThreadPool(nThreads);

    }

    protected static void init(ServletContext servletContext) {
        OpenOfficeServiceResolver instance = new OpenOfficeServiceResolver(servletContext);
        servletContext.setAttribute(KEY, instance);
        instance.manager.start();
    }

    protected static void destroy(ServletContext servletContext) {
        OpenOfficeServiceResolver instance = get(servletContext);
        instance.conversionThreadPool.shutdown();
        instance.manager.stop();
    }

    public static OpenOfficeServiceResolver get(ServletContext servletContext) {
        return (OpenOfficeServiceResolver) servletContext.getAttribute(KEY);
    }

    public OfficeManager getOfficeManager() {
        return manager;
    }

    public OfficeDocumentConverter getDocumentConverter() {
        return converter;
    }

    public ExecutorService getConversionThreadPool() {
        return conversionThreadPool;
    }
}
