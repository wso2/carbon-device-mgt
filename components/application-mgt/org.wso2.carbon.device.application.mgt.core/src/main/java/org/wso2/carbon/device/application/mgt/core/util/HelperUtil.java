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
package org.wso2.carbon.device.application.mgt.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;

import java.util.List;
import java.util.UUID;

/**
 * Utility methods used in the Application Management.
 */
public class HelperUtil {

    private static Log log = LogFactory.getLog(HelperUtil.class);

    private static DeviceManagementProviderService deviceManagementProviderService;
    private static GroupManagementProviderService groupManagementProviderService;

    public static String generateApplicationUuid() {
        return UUID.randomUUID().toString();
    }

    public static DeviceManagementProviderService getDeviceManagementProviderService() {
        if (deviceManagementProviderService == null) {
            synchronized (HelperUtil.class) {
                if (deviceManagementProviderService == null) {
                    PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    deviceManagementProviderService = (DeviceManagementProviderService) ctx
                            .getOSGiService(DeviceManagementProviderService.class, null);
                    if (deviceManagementProviderService == null) {
                        String msg = "Device management provider service has not initialized.";
                        log.error(msg);
                        throw new IllegalStateException(msg);
                    }
                }
            }
        }
        return deviceManagementProviderService;
    }

    public static GroupManagementProviderService getGroupManagementProviderService() {
        if (groupManagementProviderService == null) {
            synchronized (HelperUtil.class) {
                if (groupManagementProviderService == null) {
                    PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    groupManagementProviderService = (GroupManagementProviderService) ctx
                            .getOSGiService(GroupManagementProviderService.class, null);
                    if (groupManagementProviderService == null) {
                        String msg = "Group management provider service has not initialized.";
                        log.error(msg);
                        throw new IllegalStateException(msg);
                    }
                }
            }
        }
        return groupManagementProviderService;
    }
}
