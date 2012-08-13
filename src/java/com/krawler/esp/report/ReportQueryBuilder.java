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

import com.krawler.esp.report.custom.QueryFilter;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Kamlesh
 */
public abstract class ReportQueryBuilder {

    public abstract void addField(String tablename, String fieldname);

    public abstract void addField(String tablename, String fieldname, int type, String dataIndex);

    public abstract String getQuery();
    
    public abstract String getCountQuery();
    
    public String getCountQueryForMT(String userID){
        return "";
    }
    
    public String getQueryForMT(String userID){
        return "";
    };

    public abstract void addReferences(Set<String> tables);

    public abstract void addSearch(String tablename, String fieldname, String ss);

    public abstract List<ReportColumn> getColumns();

    public abstract void addFilter(QueryFilter qf);
}
