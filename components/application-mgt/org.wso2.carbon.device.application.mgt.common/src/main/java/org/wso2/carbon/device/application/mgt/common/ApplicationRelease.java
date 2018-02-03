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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * This class holds the details when releasing an Application to application store.
 */
public class ApplicationRelease {

    @Exclude
    private int id;

    private String version;

    private String tenantId;

    private String uuid;

    private String appStoredLoc;

    private String bannerLoc;

    private String screenshotLoc1;

    private String screenshotLoc2;

    private String screenshotLoc3;

    private String applicationCreator;

    private String releaseType;

    private Double price;

    private Timestamp createdAt;

    private String publishedBy;

    private Timestamp publishedAt;

    private int starts;

    private  String modifiedBy;

    private Timestamp modifiedAt;

    private ImageArtifact icon;

    private String iconLoc;

    private ImageArtifact banner;

    private String currentState;

    private String previousState;

    private String stateModifiedBy;

    private Timestamp stateModifiedAt;

    private List<ImageArtifact> screenShots = new ArrayList<>();

    private String appHashValue;

    private int isSharedWithAllTenants;

    private String metaData;

    private int noOfRatedUsers;

    private int stars;

    public int getNoOfRatedUsers() {
        return noOfRatedUsers;
    }

    public void setNoOfRatedUsers(int noOfRatedUsers) {
        this.noOfRatedUsers = noOfRatedUsers;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getReleaseType() {
        return releaseType;
    }

    public void setReleaseType(String releaseType) {
        this.releaseType = releaseType;
    }

    public void setIcon(ImageArtifact icon) {
        this.icon = icon;
    }

    public void setBanner(ImageArtifact banner) {
        this.banner = banner;
    }

    public void setScreenShots(List<ImageArtifact> screenShots) {
        this.screenShots = screenShots;
    }

    public void setAppHashValue(String appHashValue) {
        this.appHashValue = appHashValue;
    }

    public void setIsSharedWithAllTenants(int isSharedWithAllTenants) { this.isSharedWithAllTenants = isSharedWithAllTenants; }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    public int getId() { return id; }

    public String getVersion() {
        return version;
    }

    public String getTenantId() {
        return tenantId;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    public String getPreviousState() {
        return previousState;
    }

    public void setPreviousState(String previousState) {
        this.previousState = previousState;
    }

    public String getStateModifiedBy() {
        return stateModifiedBy;
    }

    public void setStateModifiedBy(String stateModifiedBy) {
        this.stateModifiedBy = stateModifiedBy;
    }

    public Timestamp getStateModifiedAt() {
        return stateModifiedAt;
    }

    public void setStateModifiedAt(Timestamp stateModifiedAt) {
        this.stateModifiedAt = stateModifiedAt;
    }

    public ImageArtifact getIcon() {
        return icon;
    }

    public ImageArtifact getBanner() {
        return banner;
    }

    public List<ImageArtifact> getScreenShots() {
        return screenShots;
    }

    public String getAppHashValue() {
        return appHashValue;
    }

    public int getIsSharedWithAllTenants() {
        return isSharedWithAllTenants;
    }

    public String getMetaData() {
        return metaData;
    }

    public String getAppStoredLoc() {
        return appStoredLoc;
    }

    public void setAppStoredLoc(String appStoredLoc) {
        this.appStoredLoc = appStoredLoc;
    }

    public String getBannerLoc() {
        return bannerLoc;
    }

    public void setBannerLoc(String bannerLoc) {
        this.bannerLoc = bannerLoc;
    }

    public String getScreenshotLoc1() {
        return screenshotLoc1;
    }

    public void setScreenshotLoc1(String screenshotLoc1) {
        this.screenshotLoc1 = screenshotLoc1;
    }

    public String getScreenshotLoc2() {
        return screenshotLoc2;
    }

    public void setScreenshotLoc2(String screenshotLoc2) {
        this.screenshotLoc2 = screenshotLoc2;
    }

    public String getScreenshotLoc3() {
        return screenshotLoc3;
    }

    public void setScreenshotLoc3(String screenshotLoc3) {
        this.screenshotLoc3 = screenshotLoc3;
    }

    public String getApplicationCreator() {
        return applicationCreator;
    }

    public void setApplicationCreator(String applicationCreator) {
        this.applicationCreator = applicationCreator;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getPublishedBy() {
        return publishedBy;
    }

    public void setPublishedBy(String publishedBy) {
        this.publishedBy = publishedBy;
    }

    public Timestamp getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Timestamp publishedAt) {
        this.publishedAt = publishedAt;
    }

    public int getStarts() {
        return starts;
    }

    public void setStarts(int starts) {
        this.starts = starts;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Timestamp getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Timestamp modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public String getIconLoc() {
        return iconLoc;
    }

    public void setIconLoc(String iconLoc) {
        this.iconLoc = iconLoc;
    }
}
