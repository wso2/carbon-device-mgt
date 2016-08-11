/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.device.mgt.common.app.mgt;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Properties;

@ApiModel(value = "Application", description = "This class carries all information related application")
public class Application implements Serializable {

    private static final long serialVersionUID = 1998101711L;

    @ApiModelProperty(name = "id", value = "The ID given to the application when it is stored in the EMM database", required = true)
    private int id;
    @ApiModelProperty(name = "platform", value = "The mobile device platform. It can be android, ios or windows", required = true)
    private String platform;
    @ApiModelProperty(name = "category", value = "The application category", required = true)
    private String category;
    @ApiModelProperty(name = "name", value = "The application's name", required = true)
    private String name;

    private String locationUrl;
    @ApiModelProperty(name = "imageUrl", value = "The icon url of the application", required = true)
    private String imageUrl;
    @ApiModelProperty(name = "version", value = "The application's version", required = true)
    private String version;
    @ApiModelProperty(name = "type", value = "The application type", required = true)
    private String type;
    @ApiModelProperty(name = "appProperties", value = "The properties of the application", required = true)
    private Properties appProperties;
    @ApiModelProperty(name = "applicationIdentifier", value = "The application identifier", required = true)
    private String applicationIdentifier;
    @ApiModelProperty(name = "memoryUsage", value = "Amount of memory used by the application", required = true)
    private int memoryUsage;
    @ApiModelProperty(name = "isActive", value = "Is the application actively running", required = true)
    private boolean isActive;


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


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getLocationUrl() {
        return locationUrl;
    }

    public void setLocationUrl(String locationUrl) {
        this.locationUrl = locationUrl;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getApplicationIdentifier() {
        return applicationIdentifier;
    }

    public void setApplicationIdentifier(String applicationIdentifier) {
        this.applicationIdentifier = applicationIdentifier;
    }

    public int getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(int memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Application that = (Application) o;
        if (applicationIdentifier != null ? !applicationIdentifier.equals(that.applicationIdentifier) : that.applicationIdentifier != null) {
            return false;
        }
        if (version != null ? !version.equals(that.version) : that.version != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (applicationIdentifier != null ? applicationIdentifier.hashCode() : 0);
        return result;
    }

    public Properties getAppProperties() {
        return appProperties;
    }

    public void setAppProperties(Properties appProperties) {
        this.appProperties = appProperties;
    }


    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
