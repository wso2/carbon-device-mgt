/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.jaxrs.beans;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Properties;

/**
 * This class represents the generic mobile AuthenticationImpl information
 * which is used by AppM.
 */
@ApiModel(value = "MobileApp", description = "Details of a mobile application.")
public class MobileApp {

    @ApiModelProperty(name = "id", value = "Id of the app used internally.", required = true)
    private String id;
    @ApiModelProperty(name = "name", value = "The name of the application.", required = true)
    private String name;
    @ApiModelProperty(name = "type", value = "The type of the application. The following types of applications are " +
            "supported: enterprise, public and webapp..", required = true)
    private MobileAppTypes type;
    @ApiModelProperty(name = "platform", value = "Platform the app can be installed on  .", required = true)
    private String platform;
    @ApiModelProperty(name = "version", value = "Version of the application.", required = true)
    private String version;
    @ApiModelProperty(name = "identifier", value = "The package name of the application.", required = true)
    private String identifier;
    @ApiModelProperty(name = "iconImage", value = "Link to the icon of the app.", required = true)
    private String iconImage;
    @ApiModelProperty(name = "packageName", value = "Define the exact name of the application package. You can use one " +
            "of the following methods to get the package name.\n" +
            "Go to the respective application in the play store and copy the" +
            " ID or package name from the URL.\n" +
            "Example: The play store application URL for the Viber app is " +
            "https://play.google.com/store/apps/details?id=com.viber.voip&hl=en." +
            " Therefore, the package name or " +
            "the application ID is: id=com.viber.voip \n" +
            "Download the System Info for Android to your device from the" +
            " play store. \n" +
            "Once the application is successfully installed go to the Tasks " +
            "tab and you will see the package name under the respective " +
            "application..", required = true)
    private String packageName;
    @ApiModelProperty(name = "appIdentifier", value = "The package name of the application.", required = true)
    private String appIdentifier;
    private String location;
    @ApiModelProperty(name = "properties", value = "List of meta data.", required = true)
    private Properties properties;

    public MobileAppTypes getType() {
        return type;
    }

    public void setType(MobileAppTypes type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getIconImage() {
        return iconImage;
    }

    public void setIconImage(String iconImage) {
        this.iconImage = iconImage;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getAppIdentifier() {
        return appIdentifier;
    }

    public void setAppIdentifier(String appIdentifier) {
        this.appIdentifier = appIdentifier;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

}
