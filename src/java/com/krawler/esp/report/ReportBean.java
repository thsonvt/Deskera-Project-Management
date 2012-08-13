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
package com.krawler.esp.report;

import com.krawler.common.customcolumn.CustomColumn;
import com.krawler.common.util.Constants;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kamlesh Kumar Sah
 */
public class ReportBean {
    
    List<ReportColumn> columns;
    Map columnIndex = new HashMap();
    List data = new ArrayList();
    int totalCount = 0;

    public ReportBean() {
        columns = new ArrayList<ReportColumn>();
    }
    
    public ReportBean(List<ReportColumn> columns) {
        this.columns = columns;
        //Initialize the column map
        int numCols = columns.size();
        for (int i = 0; i < numCols; i++) {
            columnIndex.put(columns.get(i).getFieldName(), new Integer(i));
        }
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public void setColumns(List<ReportColumn> columns) {
        this.columns = columns;
    }

    public List<ReportColumn> getColumns() {
        return columns;
    }

    @Override
    public String toString() {
        int len = columns.size();
        int s = data.size();
        JSONObject jdata = new JSONObject();
        try {
            JSONArray ja = new JSONArray();
            for (int i = 0; i < s; i++) {
                Object o[] = (Object[]) data.get(i);
                JSONObject jobj = new JSONObject();
                for (int j = 0; j < len; j++) {
                    jobj.put(columns.get(j).getDataIndex(), o[j]);
                }
                ja.put(jobj);
            }
//            if (s == 0) {
//                return "{data:[]}";
//            }
            jdata.put("data", ja);
        } catch (JSONException ex) {
            Logger.getLogger(ReportBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jdata.toString();
    }

    public int getCoumnCount() {
        return columns.size();
    }

    public void addRow(Object[] row) {
        data.add(row);
    }

    public String toCSV(boolean withHeader) {
        int[] dataIndex = sortedDataColumnArray();
        StringBuilder csv = new StringBuilder();
        int s = columns.size();
        if (withHeader) {
            for (int i = 0; i < s - 1; i++) {
                csv.append("\"").append(columns.get(i).getDisplayHeader()).append("\",");
            }
            if (s != 0) {
                csv.append("\"").append(columns.get(s - 1).getDisplayHeader()).append("\"").append("\n");
            }
        }
        int rowCnt = data.size();
        for (int row = 0; row < rowCnt; row++) {
            Object o[] = (Object[]) data.get(row);
            for (int col = 0; col < s; col++) {
                int index=dataIndex[col];
                if (o[index] != null && !o[index].equals("")) {
                    if (columns.get(col).getType() == CustomColumn.DATE && columns.get(col).getTableName().equals("ccdata")) {
                        DateFormat df = new SimpleDateFormat(Constants.ONLY_DATE_FORMAT);
                        Date d = new Date((Long) o[index]);
                        csv.append("\"").append(df.format(d)).append("\"").append(",");
                    } else if (columns.get(col).getType() == CustomColumn.BOOLEAN) {
                        String v;
                        if ((Boolean) o[index]) {
                            v = "Yes";
                        } else {
                            v = "No";
                        }
                        csv.append("\"").append(v).append("\"").append(",");
                    } else {
                        csv.append("\"").append(o[index]).append("\"").append(",");
                    }
                } else {
                    csv.append("\"\"").append(",");
                }
            }
            csv.append("\n");
        }
        return csv.toString();
    }

    private int[] sortedDataColumnArray() {// it gives index Array of data according to sorted columns for csv export.
        int colSize=columns.size();
        int[] arr=new int[colSize];
        for(int i=0;i<colSize;i++){
            arr[columns.get(i).getDisplayOrder()]=i;
        }
        Collections.sort(columns);
        return arr;
    }
}
