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
package com.krawler.esp.user;

import com.krawler.common.util.StringUtil;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import flexjson.transformer.DateTransformer;

/**
 *
 * @author Abhay
 */
public class User {

    private String userID;
    private String userName;
    private String image;
    private String firstName;
    private String lastName;
    private String fullName;
    private int roleID;
    private String emailID;
    private String address;
    private String designation;
    private String contactNumber;
    private String aboutUser;
    private String userStatus;
    private String timeZone;
    private String companyID;
    private String fax;
    private String alternateContactNumber;
    private int phpBBID;
    private String panNumber;
    private String ssnNumber;

    public User() {
        this.userID = "";
        this.userName = "";
        this.firstName = "";
        this.lastName = "";
        this.fullName = "";
        this.emailID = "";
    }
    private int dateFormat;
    private int country;
    private boolean notification;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        final User other = (User) obj;
        if ((this.userID == null) ? (other.userID != null) : !this.userID.equals(other.userID)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.userID != null ? this.userID.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return new JSONSerializer().exclude("*.class").transform(new DateTransformer("yyyy-MM-dd"), new String[]{"user.createdOn", "user.modifiedOn"}).deepSerialize(this);
    }

    public static User JSONtoObject(String serializedJSONString) {
        return new JSONDeserializer<User>().use(null, User.class).deserialize(serializedJSONString);
    }

    public String getAboutUser() {
        return aboutUser;
    }

    public void setAboutUser(String aboutUser) {
        this.aboutUser = aboutUser;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAlternateContactNumber() {
        return alternateContactNumber;
    }

    public void setAlternateContactNumber(String alternateContactNumber) {
        this.alternateContactNumber = alternateContactNumber;
    }

    public String getCompanyID() {
        return companyID;
    }

    public void setCompanyID(String companyID) {
        this.companyID = companyID;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public int getCountry() {
        return country;
    }

    public void setCountry(int country) {
        this.country = country;
    }

    public int getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(int dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getEmailID() {
        return emailID;
    }

    public void setEmailID(String emailID) {
        this.emailID = emailID;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean getNotification() {
        return notification;
    }

    public void setNotification(boolean notification) {
        this.notification = notification;
    }

    public String getPanNumber() {
        return panNumber;
    }

    public void setPanNumber(String panNumber) {
        this.panNumber = panNumber;
    }

    public int getPhpBBID() {
        return phpBBID;
    }

    public void setPhpBBID(int phpBBID) {
        this.phpBBID = phpBBID;
    }

    public int getRoleID() {
        return roleID;
    }

    public void setRoleID(int roleID) {
        this.roleID = roleID;
    }

    public String getSsnNumber() {
        return ssnNumber;
    }

    public void setSsnNumber(String ssnNumber) {
        this.ssnNumber = ssnNumber;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    @Deprecated
    public String getUserName() {
        return userName;
    }

    @Deprecated
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public String getStringObj(String objName) {
        String obj = "";
        if (objName.equals("fname")) {
            obj = this.firstName;
        }
        if (objName.equals("lname")) {
            obj = this.lastName;
        }
        if (objName.equals("phone")) {
            obj = this.contactNumber;
        }
        if (objName.equals("email")) {
            obj = this.emailID;
        }
        return obj;
    }

    public String getFullName() {
        return this.firstName.concat(" ").concat(this.lastName);
    }

    public void setFullName(String fullName) {
        if(!StringUtil.isNullOrEmpty(firstName))
            this.fullName = fullName;
        else {
            this.fullName = this.firstName.concat(" ").concat(this.lastName);
        }
    }
}
