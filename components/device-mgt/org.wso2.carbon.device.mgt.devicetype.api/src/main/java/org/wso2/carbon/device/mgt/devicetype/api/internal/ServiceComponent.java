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

package org.wso2.carbon.device.mgt.devicetype.api.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.device.mgt.core.spi.DeviceManagement;

/**
 * Holds device management service
 */
@Component(name = "org.wso2.device.mgt.api.internal.ServiceComponent",
           immediate = true,
           property = {
                   "componentName=wso2-devicemgt-api-service-component"
           }
)
public class ServiceComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceComponent.class);
    private static DeviceManagement deviceManagement;

    @Reference(name = "deviceManagement",
               service = DeviceManagement.class,
               cardinality = ReferenceCardinality.MANDATORY,
               policy = ReferencePolicy.DYNAMIC,
               unbind = "unsetDeviceManagement")
    protected void setDeviceManagement(DeviceManagement deviceManagement) {
        changeDeviceManagement(deviceManagement);
        LOGGER.debug("An instance of class '{}' registered as a Device Management.",
                     deviceManagement.getClass().getName());
    }

    protected void unsetDeviceManagement(DeviceManagement deviceManagement) {
        changeDeviceManagement(null);
        LOGGER.debug("An instance of class '{}' unregistered as a Device Management.",
                     deviceManagement.getClass().getName());
    }

    private static synchronized void changeDeviceManagement(DeviceManagement deviceManagement) {
        ServiceComponent.deviceManagement = deviceManagement;
    }

    public static DeviceManagement getDeviceManagement() {
        if (deviceManagement == null) {
            throw new IllegalStateException("DeviceManagement service not available.");
        }
        return ServiceComponent.deviceManagement;
    }

    @Activate
    protected void activate(BundleContext bundleContext) {
        LOGGER.debug("Device Management API service component started");
    }

}
