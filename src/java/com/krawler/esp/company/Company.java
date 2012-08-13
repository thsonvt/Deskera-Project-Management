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

import com.krawler.common.util.Constants;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import flexjson.transformer.DateTransformer;
import java.util.Date;

/**
 *
 * @author Abhay
 */
public class Company {

    private String companyID;
    private String companyLogo;
    private String companyName;
    private String subDomain;
    private String address;
    private String city;
    private String state;
    private String zip;
    private String phone;
    private String fax;
    private String website;
    private String creator;
    private String emailID;
    private String timezone;
    private String payerID;
    private Date createdOn;
    private Date modifiedOn;
    private Date subscriptionDate;
    private Date toExpireOn;
    private int currency;
    private int country;
    private int maxUsers;
    private int maxCommunities;
    private int maxProjects;
    private int featureAccess;
    private int planID;
    private int maxFileSize;
    private int notificationType;
    private int language;
    private boolean activated;
    private boolean isexpired;
    private boolean isMilestoneWidget;
    private boolean isCheckList;
    private boolean docAccess;

    public Company() {
        currency = Constants.Default_Int_Initializer;
        country = Constants.Default_Int_Initializer;
        maxUsers = Constants.Default_Int_Initializer;
        maxCommunities = Constants.Default_Int_Initializer;
        maxProjects = Constants.Default_Int_Initializer;
        featureAccess = Constants.Default_Int_Initializer;
        planID = Constants.Default_Int_Initializer;
        maxFileSize = Constants.Default_Int_Initializer;
        notificationType = Constants.Default_Int_Initializer;
    }

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
        final Company other = (Company) obj;
        if ((this.companyID == null) ? (other.companyID != null) : !this.companyID.equals(other.companyID)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.companyID != null ? this.companyID.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return new JSONSerializer().exclude("*.class").
                transform(new DateTransformer("yyyy-MM-dd"), new String[]{"Company.createdOn", "Company.subscriptionDate", "Company.toExpireOn"}).
                transform(new DateTransformer("yyyy-MM-dd HH:mm:ss"), new String[]{"Company.modifiedOn"}).deepSerialize(this);
    }

    public static Company JSONtoObject(String serializedJSONString) {
        return new JSONDeserializer<Company>().use(null, Company.class).deserialize(serializedJSONString);
    }

    public boolean isIsCheckList() {
        return isCheckList;
    }

    public void setIsCheckList(boolean isCheckList) {
        this.isCheckList = isCheckList;
    }

    public boolean isIsMilestoneWidget() {
        return isMilestoneWidget;
    }

    public void setIsMilestoneWidget(boolean isMilestoneWidget) {
        this.isMilestoneWidget = isMilestoneWidget;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCompanyID() {
        return companyID;
    }

    public void setCompanyID(String companyID) {
        this.companyID = companyID;
    }

    public String getCompanyLogo() {
        return companyLogo;
    }

    public void setCompanyLogo(String companyLogo) {
        this.companyLogo = companyLogo;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public int getCountry() {
        return country;
    }

    public void setCountry(int country) {
        this.country = country;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public int getCurrency() {
        return currency;
    }

    public void setCurrency(int currency) {
        this.currency = currency;
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

    public int getFeatureAccess() {
        return featureAccess;
    }

    public void setFeatureAccess(int featureAccess) {
        this.featureAccess = featureAccess;
    }

    public boolean isIsexpired() {
        return isexpired;
    }

    public void setIsexpired(boolean isexpired) {
        this.isexpired = isexpired;
    }

    public int getLanguage() {
        return language;
    }

    public void setLanguage(int language) {
        this.language = language;
    }

    public int getMaxCommunities() {
        return maxCommunities;
    }

    public void setMaxCommunities(int maxCommunities) {
        this.maxCommunities = maxCommunities;
    }

    public int getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(int maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public int getMaxProjects() {
        return maxProjects;
    }

    public void setMaxProjects(int maxProjects) {
        this.maxProjects = maxProjects;
    }

    public int getMaxUsers() {
        return maxUsers;
    }

    public void setMaxUsers(int maxUsers) {
        this.maxUsers = maxUsers;
    }

    public Date getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(Date modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public int getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(int notificationType) {
        this.notificationType = notificationType;
    }

    public String getPayerID() {
        return payerID;
    }

    public void setPayerID(String payerID) {
        this.payerID = payerID;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getPlanID() {
        return planID;
    }

    public void setPlanID(int planID) {
        this.planID = planID;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSubDomain() {
        return subDomain;
    }

    public void setSubDomain(String subDomain) {
        this.subDomain = subDomain;
    }

    public Date getSubscriptionDate() {
        return subscriptionDate;
    }

    public void setSubscriptionDate(Date subscriptionDate) {
        this.subscriptionDate = subscriptionDate;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Date getToExpireOn() {
        return toExpireOn;
    }

    public void setToExpireOn(Date toExpireOn) {
        this.toExpireOn = toExpireOn;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public boolean isDocAccess() {
        return docAccess;
    }

    public void setDocAccess(boolean docAccess) {
        this.docAccess = docAccess;
    }
}
