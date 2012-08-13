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
package com.krawler.common.util;

import flexjson.JSONDeserializer;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author Abhay
 */
public class Utilities {

    public static String listToGridJson(List ll, int count, Class toCast) {
        StringBuilder temp = new StringBuilder();
        try {
            boolean hasData = false;
            Iterator ite = ll.listIterator();
            temp.append("{\"count\":");
            temp.append(Integer.toString(count));
            temp.append(", \"data\":[");
            while (ite.hasNext()) {
                Object newInstance = toCast.cast(ite.next());
                temp.append(newInstance.toString());
                temp.append(",");
                hasData = true;
            }
            if(hasData)
                temp.deleteCharAt(temp.toString().lastIndexOf(','));
            temp.append("]}");
        } catch (Exception e) {
            System.out.print(e.getMessage());
        } finally {
            return temp.toString();
        }
    }

    public static <T extends Object> T JSONtoObject(String serializedJSONString, Class<T> ofType) {
        return new JSONDeserializer<T>().use(null, ofType).deserialize(serializedJSONString);
    }

    public static String format(double values, boolean doNotFormat) {
        if (!doNotFormat) {
            Locale currentLocale = Locale.US;
            NumberFormat numberFormatter = NumberFormat.getNumberInstance(currentLocale);
            numberFormatter.setMinimumFractionDigits(0);
            numberFormatter.setMaximumFractionDigits(2);
            return numberFormatter.format(values);
        } else {
            return Double.toString(values);
        }
    }

    public static String listToJsonArray(List ll, Class toCast, String jsonArrayKey) {
        StringBuilder temp = new StringBuilder();
        try {
            boolean hasData = false;
            Iterator ite = ll.listIterator();
            temp.append("{\"");
            temp.append(jsonArrayKey);
            temp.append("\":[");
            while (ite.hasNext()) {
                Object newInstance = toCast.cast(ite.next());
                temp.append(newInstance.toString());
                temp.append(",");
                hasData = true;
            }
            if(hasData)
                temp.deleteCharAt(temp.toString().lastIndexOf(','));
            temp.append("]}");
        } catch (Exception e) {
            System.out.print(e.getMessage());
        } finally {
            return temp.toString();
        }
    }

    /**
     *  Function to round off a double value upto a specific decimal place.
     * e.g. roundDoubleTo(4.45,1) will give 4.5
     * @param val - the value to be rounded off
     * @param pow - the number of places after the decimal upto which val should be rounded
     * @return
     */
    public static double roundDoubleTo(double val, int pow) {
        double p = (double) Math.pow(10, pow);
        val = val * p;
        double tmp = (double) (Math.round(val));
        return (Double) tmp / p;
    }
}
