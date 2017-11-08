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

/**
 * Application represents the an Application in Application Store.
 */
public class DeviceType {

    @Exclude
    private int id;

    private String type;

    private String name;

    private Date lastUpdated;

    private String providerTenantID;

    private int sharedWithAllTenants;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getProviderTenantID() {
        return providerTenantID;
    }

    public void setProviderTenantID(String providerTenantID) {
        this.providerTenantID = providerTenantID;
    }

    public int getSharedWithAllTenants() {
        return sharedWithAllTenants;
    }

    public void setSharedWithAllTenants(int sharedWithAllTenants) {
        this.sharedWithAllTenants = sharedWithAllTenants;
    }
}
