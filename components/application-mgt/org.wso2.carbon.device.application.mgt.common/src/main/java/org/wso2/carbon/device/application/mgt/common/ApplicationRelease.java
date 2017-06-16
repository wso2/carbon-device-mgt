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

import java.util.Date;
import java.util.Map;

public class ApplicationRelease {

    private enum Channel {
        PRODUCTION, ALPHA, BETA
    }

    private int id;

    private int versionId;

    private String versionName;

    private String resource;

    private Channel releaseChannel;

    private String releaseDetails;

    private Date createdAt;

    private Application application;

    private Date lifecycleStateModifiedAt;

    private String lifecycleStateModifiedBy;

    private Map<String, String> properties;

    private boolean isDefault;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVersionId() {
        return versionId;
    }

    public void setVersionId(int versionId) {
        this.versionId = versionId;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public Channel getReleaseChannel() {
        return releaseChannel;
    }

    public void setReleaseChannel(Channel releaseChannel) {
        this.releaseChannel = releaseChannel;
    }

    public String getReleaseDetails() {
        return releaseDetails;
    }

    public void setReleaseDetails(String releaseDetails) {
        this.releaseDetails = releaseDetails;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Date getLifecycleStateModifiedAt() {
        return lifecycleStateModifiedAt;
    }

    public void setLifecycleStateModifiedAt(Date lifecycleStateModifiedAt) {
        this.lifecycleStateModifiedAt = lifecycleStateModifiedAt;
    }

    public String getLifecycleStateModifiedBy() {
        return lifecycleStateModifiedBy;
    }

    public void setLifecycleStateModifiedBy(String lifecycleStateModifiedBy) {
        this.lifecycleStateModifiedBy = lifecycleStateModifiedBy;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}
