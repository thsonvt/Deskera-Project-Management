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
package com.krawler.esp.project.project;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Abhay
 */
public enum PERTDiffStatus {

    COMPANY(0), PROJECT(1), TASK(2);
    private static final Map<Integer, PERTDiffStatus> lookup = new HashMap<Integer, PERTDiffStatus>();

    static {
        for (PERTDiffStatus PERTDiffStatus : EnumSet.allOf(PERTDiffStatus.class)) {
            lookup.put(PERTDiffStatus.getCode(), PERTDiffStatus);
        }
    }
    private int code;

    private PERTDiffStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static PERTDiffStatus get(int code) {
        return lookup.get(code);
    }

    @Override
    public String toString() {
        String PERTDiffStatus = super.toString();
        switch (super.ordinal()) {
            case 0:
                PERTDiffStatus = "Company";
                break;
            case 1:
                PERTDiffStatus = "Project";
                break;
            case 2:
                PERTDiffStatus = "Task";
                break;
        }
        return PERTDiffStatus;
    }
}
