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
package com.krawler.esp.project.task;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Abhay
 */
public enum DurationType {

    DAYS(0, "d"), HOURS(1, "h");
    private static final Map<Integer, DurationType> lookup = new HashMap<Integer, DurationType>();

    static {
        for (DurationType DurationType : EnumSet.allOf(DurationType.class)) {
            lookup.put(DurationType.getCode(), DurationType);
        }
    }
    private int code;
    private String suffix;

    private DurationType(int code, String suffix) {
        this.code = code;
        this.suffix = suffix;
    }

    public int getCode() {
        return code;
    }

    public String getSuffix() {
        return suffix;
    }

    public static DurationType get(int code) {
        return lookup.get(code);
    }

    @Override
    public String toString() {
        String DurationType = super.toString();
        switch (super.ordinal()) {
            case 0:
                DurationType = " d";
                break;
            case 1:
                DurationType = " h";
                break;
        }
        return DurationType;
    }
}
