/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.application.mgt.common;


import org.wso2.carbon.device.application.mgt.common.jaxrs.Exclude;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import java.util.List;

/**
 * Application represents the an Application in Application Store.
 */
public class Application {

    @Exclude
    private int id;

    private String name;

    private String appCategory;

    private String type;

    private int isFree;

    private String paymentCurrency;

    private List<Tag> tags;

    private User user;

    private List<UnrestrictedRole> unrestrictedRoles;

    private int isRestricted;

    private ApplicationRelease releaseVersion;

    private DeviceType devicetype;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }

    public String getAppCategory() {
        return appCategory;
    }

    public void setAppCategory(String appCategory) {
        this.appCategory = appCategory;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<UnrestrictedRole> getUnrestrictedRoles() {
        return unrestrictedRoles;
    }

    public void setUnrestrictedRoles(List<UnrestrictedRole> unrestrictedRoles) {
        this.unrestrictedRoles = unrestrictedRoles;
    }

    public ApplicationRelease getReleaseVersion() {
        return releaseVersion;
    }

    public void setReleaseVersion(ApplicationRelease releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getIsFree() {
        return isFree;
    }

    public void setIsFree(int isFree) {
        this.isFree = isFree;
    }

    public String getPaymentCurrency() {
        return paymentCurrency;
    }

    public void setPaymentCurrency(String paymentCurrency) {
        this.paymentCurrency = paymentCurrency;
    }

    public int getIsRestricted() {
        return isRestricted;
    }

    public void setIsRestricted(int isRestricted) {
        this.isRestricted = isRestricted;
    }

    public DeviceType getDevicetype() {
        return devicetype;
    }

    public void setDevicetype(DeviceType devicetype) {
        this.devicetype = devicetype;
    }
}
