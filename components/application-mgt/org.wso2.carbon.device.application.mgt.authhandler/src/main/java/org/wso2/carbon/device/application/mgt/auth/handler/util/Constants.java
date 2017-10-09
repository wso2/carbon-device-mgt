/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.application.mgt.auth.handler.util;

public class Constants {
    public static final String SCOPES = "perm:application:get perm:application:create perm:application:update " +
            "perm:application-mgt:login perm:application:delete perm:platform:add perm:platform:remove " +
            "perm:roles:view perm:devices:view perm:platform:get perm:admin:devices:view perm:roles:add " +
            "perm:roles:add-users perm:roles:update perm:roles:permissions perm:roles:details perm:roles:view" +
            " perm:roles:create-combined-role perm:roles:delete perm:dashboard:vulnerabilities " +
            "perm:dashboard:non-compliant-count perm:dashboard:non-compliant perm:dashboard:by-groups " +
            "perm:dashboard:device-counts perm:dashboard:feature-non-compliant perm:dashboard:count-overview " +
            "perm:dashboard:filtered-count perm:dashboard:details perm:get-activity perm:devices:delete " +
            "perm:devices:applications perm:devices:effective-policy perm:devices:compliance-data " +
            "perm:devices:features perm:devices:operations perm:devices:search perm:devices:details " +
            "perm:devices:update perm:devices:view perm:view-configuration perm:manage-configuration " +
            "perm:policies:remove perm:policies:priorities perm:policies:deactivate perm:policies:get-policy-details" +
            " perm:policies:manage perm:policies:activate perm:policies:update perm:policies:changes " +
            "perm:policies:get-details perm:users:add perm:users:details perm:users:count perm:users:delete " +
            "perm:users:roles perm:users:user-details perm:users:credentials perm:users:search perm:users:is-exist " +
            "perm:users:update perm:users:send-invitation perm:admin-users:view perm:groups:devices perm:groups:update " +
            "perm:groups:add perm:groups:device perm:groups:devices-count perm:groups:remove perm:groups:groups " +
            "perm:groups:groups-view perm:groups:share perm:groups:count perm:groups:roles perm:groups:devices-remove " +
            "perm:groups:devices-add perm:groups:assign perm:device-types:features perm:device-types:types " +
            "perm:applications:install perm:applications:uninstall perm:admin-groups:count perm:admin-groups:view" +
            " perm:notifications:mark-checked perm:notifications:view perm:admin:certificates:delete " +
            "perm:admin:certificates:details perm:admin:certificates:view perm:admin:certificates:add " +
            "perm:admin:certificates:verify perm:admin perm:devicetype:deployment perm:device-types:events " +
            "perm:device-types:events:view perm:admin:device-type perm:device:enroll perm:geo-service:analytics-view " +
            "perm:geo-service:alerts-manage";

    public static final String[] TAGS = {"device_management"};
    public static final String USER_NAME = "userName";
    public static final String PUBLISHER_APPLICATION_NAME = "applicationmgt_publisher";
    public static final String STORE_APPLICATION_NAME = "applicationmgt_store";
    public static final String PASSWORD_GRANT_TYPE = "password";
    public static final String REFRESH_GRANT_TYPE = "refresh_token";
    public static final String APPLICATION_INFO = "application_info";


}
