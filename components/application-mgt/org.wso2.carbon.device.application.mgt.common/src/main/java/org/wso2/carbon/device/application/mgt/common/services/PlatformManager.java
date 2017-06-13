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
package org.wso2.carbon.device.application.mgt.common.services;

import org.wso2.carbon.device.application.mgt.common.Platform;
import org.wso2.carbon.device.application.mgt.common.exception.PlatformManagementException;

import java.util.List;

/**
 * Platform manager is responsible for handling platforms, which will be used to as a registry of platforms.
 * And will be able to provide the platforms related informations to other classes which requires.
 */
public interface PlatformManager {

    void initialize(String tenantDomain) throws PlatformManagementException;

    List<Platform> getPlatforms(String tenantDomain) throws PlatformManagementException;

    void register(String tenantDomain, Platform platform) throws PlatformManagementException;

    void unregister(String tenantDomain, String platformCode, boolean isFileBased) throws PlatformManagementException;

    void addMapping(String tenantDomain, List<String> platformCode) throws PlatformManagementException;

    void addMapping(String tenantDomain, String platformCode) throws PlatformManagementException;

    void removeMapping(String tenantDomain, String platformCode) throws PlatformManagementException;

}
