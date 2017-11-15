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

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds the details when releasing an Application to application store.
 */
public class ApplicationRelease {

    private enum ReleaseType {
        PRODUCTION, ALPHA, BETA
    }

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

    private ReleaseType releaseType;

    private Double price;

    private ImageArtifact icon;

    private ImageArtifact banner;

    private List<ImageArtifact> screenShots = new ArrayList<>();

    private String appHashValue;

    private int isSharedWithAllTenants;

    private String metaData;

    private List<Comment> comments;

    private Lifecycle lifecycle;

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

    public void setReleaseType(ReleaseType releaseType) {
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

    public ReleaseType getReleaseType() {
        return releaseType;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
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

    public List<Comment> getComments() { return comments; }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
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

    public Lifecycle getLifecycle() {
        return lifecycle;
    }

    public void setLifecycle(Lifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }
}
