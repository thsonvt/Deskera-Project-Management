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

/**
 *
 * @author kamlesh
 */
public class ReportsQuery {

    public static String getReportsMetaDataQuery(String type) {
        return "select columnid,header,header as displayheader,name,dataindex,tablename,fieldname, module,if(ismandatory=0, false, true) as ismandatory,renderer,type,headerkey "
                + " from reportscolumn " +//where type = '"+type+"'" +
                " union select columnid,header,header as displayheader, name, dataIndex, 'ccdata' as tablename, columnno as fieldname, module, "
                + " false as ismandatory, renderer, type,header as headerkey from ccmetadata where companyid = ? and visible = 1";
    }

    public static String getReportsColumn(String reportid) {
        return "SELECT rcm.columnid,displayheader,header,rcm.reportid,quicksearch,summary,module,name,dataindex,renderer,type,tablename,fieldname,displayorder,headerkey"
                + " from reportscolumn as rc inner join reportscolumnmapping as rcm on rc.columnid = rcm.columnid where rcm.reportid = '" + reportid + "'"
                + " union "
                + "SELECT rcm.columnid,displayheader,header,rcm.reportid,quicksearch,summary,module,name,dataindex,renderer,type,'ccdata' as tablename,columnno as fieldname,displayorder, '' as headerkey"
                + " from ccmetadata as rc inner join reportscolumnmapping as rcm on rc.columnid = rcm.columnid where rcm.reportid = '" + reportid + "'";
    }

    /**
     * get query for insert columns for a report
     * the column will be inserted into reportscolumnmapping table
     * @param columns
     * @return
     */
    public static String getInsertReportsColumn() {
        return "insert into reportscolumnmapping(columnid,reportid,displayheader,quicksearch, summary,displayorder) values(?,?,?,?,?,?)";
    }

    public static String getAllReports() {
        return "SELECT * FROM customreports where companyid = ? ";
    }

    public static String getReport() {
        return "SELECT * FROM customreports where companyid = ? and reportid = ?";
    }

    public static String getMaxCountOfMilestone(boolean permissionForAllProjects) {
        if(permissionForAllProjects){
            return "SELECT MAX(B.milestone) AS count FROM "
                + "(SELECT COUNT(A.taskid) milestone, A.projectid FROM "
                + "(SELECT taskid,p.projectid FROM proj_task "
                + "INNER JOIN project p ON proj_task.projectid = p.projectid "
                + "WHERE duration IN('0', '0.0', '0.00', '0d', '0.0d', '0.00d', '0.00days', '0 days', '0.0days', '0.00h') "
                + "AND p.archived = false AND p.companyid = ?) "
                + "A GROUP BY projectid) B";
        }else{
            return "SELECT MAX(B.milestone) AS count FROM "
                + "(SELECT COUNT(A.taskid) milestone, A.projectid FROM "
                + "(SELECT taskid,p.projectid FROM proj_task "
                + "INNER JOIN project p ON proj_task.projectid = p.projectid "
                + "INNER JOIN projectmembers pm ON p.projectid = pm.projectid "
                + "WHERE duration IN('0', '0.0', '0.00', '0d', '0.0d', '0.00d', '0.00days', '0 days', '0.0days', '0.00h') "
                + "AND pm.userid = ? AND pm.status IN (3,4,5) AND p.archived = false AND pm.inuseflag = 1 AND p.companyid = ?) "
                + "A GROUP BY projectid) B";
        } 
    }
    
    public static String getSelectedReport(){
        return "SELECT * FROM selectedreport WHERE userid = ?";
    } 
    
    public static String setSelectedReport(){
        return "INSERT INTO selectedreport(userid, reportid) VALUES(?,?)";
    } 
    
    public static String removeSelectedReport(){
        return "DELETE FROM selectedreport WHERE userid = ?";
    } 
}
