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
package com.krawler.esp.company;

import com.krawler.common.util.StringUtil;
import com.krawler.database.DbResults;

/**
 *
 * @author Abhay
 */
public class CompanyHelper {

    public static void loadCompany(DbResults rs, Company c){
        c.setCompanyID(rs.getString("companyid"));
        if(rs.has("companyname")){
            c.setCompanyName(rs.getString("companyname"));
        }
        if(rs.has("image")){
            if(!StringUtil.isNullOrEmpty(rs.getString("image")))
                c.setCompanyLogo(rs.getString("image"));
        }
        if(rs.has("companylogo")){
            if(!StringUtil.isNullOrEmpty(rs.getString("companylogo")))
                c.setCompanyLogo(rs.getString("companylogo"));
        }
        if(rs.has("subdomain")){
            c.setSubDomain(rs.getString("subdomain"));
        }
        if(rs.has("address")){
            c.setAddress(rs.getString("address"));
        }
        if(rs.has("city")){
            c.setCity(rs.getString("city"));
        }
        if(rs.has("state")){
            c.setState(rs.getString("state"));
        }
        if(rs.has("zip")){
            c.setZip(rs.getString("zip"));
        }
        if(rs.has("phone")){
            c.setPhone(rs.getString("phone"));
        }
        if(rs.has("fax")){
            c.setFax(rs.getString("fax"));
        }
        if(rs.has("website")){
            c.setWebsite(rs.getString("website"));
        }
        if(rs.has("creator")){
            c.setCreator(rs.getString("creator"));
        }
        if(rs.has("emailid")){
            c.setEmailID(rs.getString("emailid"));
        }
        if(rs.has("timezone")){
            c.setTimezone(rs.getString("timezone"));
        }
        if(rs.has("payerid")){
            c.setPayerID(rs.getString("payerid"));
        }

        if(rs.has("createdon")){
            c.setCreatedOn(rs.getDate("createdon"));
        }
        if(rs.has("modifiedon")){
            c.setModifiedOn(rs.getDate("modifiedon"));
        }
        if(rs.has("subscriptiondate")){
            c.setSubscriptionDate(rs.getDate("subscriptiondate"));
        }
        if(rs.has("toexpireon")){
            c.setToExpireOn(rs.getDate("toexpireon"));
        }

        if(rs.has("currency") && !rs.isNull("currency")){
            c.setCurrency(rs.getInt("currency"));
        }
        if(rs.has("country") && !rs.isNull("country")){
            c.setCountry(rs.getInt("country"));
        }
        if(rs.has("maxusers") && !rs.isNull("maxusers")){
            c.setMaxUsers(rs.getInt("maxusers"));
        }
        if(rs.has("maxcommunities") && !rs.isNull("maxcommunities")){
            c.setMaxCommunities(rs.getInt("maxcommunities"));
        }
        if(rs.has("maxprojects") && !rs.isNull("maxprojects")){
            c.setMaxProjects(rs.getInt("maxprojects"));
        }
        if(rs.has("featureaccess") && !rs.isNull("featureaccess")){
            c.setFeatureAccess(rs.getInt("featureaccess"));
        }
        if(rs.has("planid") && !rs.isNull("planid")){
            c.setPlanID(rs.getInt("planid"));
        }
        if(rs.has("maxfilesize") && !rs.isNull("maxfilesize")){
            c.setMaxFileSize(rs.getInt("maxfilesize"));
        }
        if(rs.has("notificationtype") && !rs.isNull("notificationtype")){
            c.setNotificationType(rs.getInt("notificationtype"));
        }

        if(rs.has("activated")){
            c.setActivated(rs.getBoolean("activated"));
        }
        if(rs.has("isexpired")){
            c.setIsexpired(rs.getBoolean("isexpired"));
        }
        if(rs.has("milestonewidget")){
            c.setIsMilestoneWidget(rs.getBoolean("milestonewidget"));
        }
        if(rs.has("checklist")){
            c.setIsCheckList(rs.getBoolean("checklist"));
        }
        if(rs.has("docaccess")){
            c.setDocAccess(rs.getBoolean("docaccess"));
        }
    }

}
