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

import java.util.Date;
import java.util.List;
import java.util.Map;

public class Application {

    @Exclude
    private int id;

    private String uuid;

    private String name;

    private String shortDescription;

    private String description;

    private String iconName;

    private String bannerName;

    private String videoName;

    private List<String> screenshots;

    private List<String> tags;

    private List<Subscription> subscriptions;

    private Platform platform;

    private Category category;

    private Map<String, String> properties;

    private String createdBy;

    private Date createdAt;

    private Date modifiedAt;

    private boolean published;

    private LifecycleState lifecycleState;

    private Date lifecycleStateModifiedAt;

    private Date getLifecycleStateModifiedBy;

    private boolean freeApp;

    private String paymentCurrency;

    private Float paymentPrice;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public String getBannerName() {
        return bannerName;
    }

    public void setBannerName(String bannerName) {
        this.bannerName = bannerName;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public List<String> getScreenshots() {
        return screenshots;
    }

    public void setScreenshots(List<String> screenshots) {
        this.screenshots = screenshots;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public LifecycleState getLifecycleState() {
        return lifecycleState;
    }

    public void setLifecycleState(LifecycleState lifecycleState) {
        this.lifecycleState = lifecycleState;
    }

    public Date getLifecycleStateModifiedAt() {
        return lifecycleStateModifiedAt;
    }

    public void setLifecycleStateModifiedAt(Date lifecycleStateModifiedAt) {
        this.lifecycleStateModifiedAt = lifecycleStateModifiedAt;
    }

    public Date getGetLifecycleStateModifiedBy() {
        return getLifecycleStateModifiedBy;
    }

    public void setGetLifecycleStateModifiedBy(Date getLifecycleStateModifiedBy) {
        this.getLifecycleStateModifiedBy = getLifecycleStateModifiedBy;
    }

    public boolean isFreeApp() {
        return freeApp;
    }

    public void setFreeApp(boolean freeApp) {
        this.freeApp = freeApp;
    }

    public String getPaymentCurrency() {
        return paymentCurrency;
    }

    public void setPaymentCurrency(String paymentCurrency) {
        this.paymentCurrency = paymentCurrency;
    }

    public Float getPaymentPrice() {
        return paymentPrice;
    }

    public void setPaymentPrice(Float paymentPrice) {
        this.paymentPrice = paymentPrice;
    }
}
