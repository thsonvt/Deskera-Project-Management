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

/**
 * A place to keep commonly-used constants.
 */
public class Constants {

    public static final long MILLIS_PER_SECOND = 1000;
    public static final long MILLIS_PER_MINUTE = MILLIS_PER_SECOND * 60;
    public static final long MILLIS_PER_HOUR = MILLIS_PER_MINUTE * 60;
    public static final long MILLIS_PER_DAY = MILLIS_PER_HOUR * 24;
    public static final long MILLIS_PER_WEEK = MILLIS_PER_DAY * 7;
    public static final long MILLIS_PER_MONTH = MILLIS_PER_DAY * 31;
    public static final int SECONDS_PER_MINUTE = 60;
    public static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * 60;
    public static final int SECONDS_PER_DAY = SECONDS_PER_HOUR * 24;
    public static final int SECONDS_PER_WEEK = SECONDS_PER_DAY * 7;
    public static final int SECONDS_PER_MONTH = SECONDS_PER_DAY * 31;
    
    public static final int WORK_RESOURCE = 1;
    public static final int MATERIAL_RESOURCE = 2;
    public static final int COST_RESOURCE = 3;
    public static final int WORK_HOURS = 8;
    
    public static final int LOW_PRIORITY = 0;
    public static final int MODERATE_PRIORITY = 1;
    public static final int HIGH_PRIORITY = 2;
    
    public static final int PERT_DURATION_DIFF_COMPANY = 0;
    public static final int PERT_DURATION_DIFF_PROJECT = 1;
    public static final int PERT_DURATION_DIFF_TASK = 2;
    public static final int DEFAULT_PERT_DURATION_DIFF = 1;
    
    
    public static final int PROJECT_MEMBER_STATUS_REQUESTED = 1;
    public static final int PROJECT_MEMBER_STATUS_INVITED = 2;
    public static final int PROJECT_MEMBER_STATUS_MEMBER = 3;
    public static final int PROJECT_MEMBER_STATUS_MODERATOR = 4;
    
    public static final int Default_Int_Initializer = -999;

    //Full MS-OUTLOOK CSV Header
    //public static final String[] CSV_HEADER_MSOUTLOOK = {"Title","First Name","Middle Name","Last Name","Suffix","Company","Department","Job Title","Business Street","Business Street 2","Business Street 3","Business City","Business State","Business Postal Code","Business Country/Region","Home Street","Home Street 2","Home Street 3","Home City","Home State","Home Postal Code","Home Country/Region","Other Street","Other Street 2","Other Street 3","Other City","Other State","Other Postal Code","Other Country/Region","Assistant's Phone","Business Fax","Business Phone","Business Phone 2","Callback","Car Phone","Company Main Phone","Home Fax","Home Phone","Home Phone 2","ISDN","Mobile Phone","Other Fax","Other Phone","Pager","Primary Phone","Radio Phone","TTY/TDD Phone","Telex","Account","Anniversary","Assistant's Name","Billing Information","Birthday","Business Address PO Box","Categories","Children","Directory Server","E-mail Address","E-mail Type","E-mail Display Name","E-mail 2 Address","E-mail 2 Type","E-mail 2 Display Name","E-mail 3 Address","E-mail 3 Type","E-mail 3 Display Name","Gender","Government ID Number","Hobby","Home Address PO Box","Initials","Internet Free Busy","Keywords","Language","Location","Manager's Name","Mileage","Notes","Office Location","Organizational ID Number","Other Address PO Box","Priority","Private","Profession","Referred By","Sensitivity","Spouse","User 1","User 2","User 3","User 4","Web Page"};
    //Header used for our contact export
    public static final String[] CSV_HEADER_MSOUTLOOK = {"\"Name\"", "\"E-mail ID\"", "\"Contact No\"", "\"Address\""};

    public static final String ONLY_DATE_FORMAT="yyyy-MM-dd";
    public static final String LONG_DATE_FORMAT="yyyy-MM-dd HH:mm:ss";
}
