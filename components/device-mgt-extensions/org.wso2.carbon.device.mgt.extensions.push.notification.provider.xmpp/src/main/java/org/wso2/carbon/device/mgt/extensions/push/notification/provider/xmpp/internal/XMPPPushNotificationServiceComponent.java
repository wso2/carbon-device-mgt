/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.extensions.push.notification.provider.xmpp.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;

/**
 * @scr.component name="org.wso2.carbon.device.mgt.extensions.push.notification.provider.fcm.internal.XMPPPushNotificationServiceComponent" immediate="true"
 * @scr.reference name="carbon.device.mgt.provider"
 * interface="org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setDeviceManagementProviderService"
 * unbind="unsetDeviceManagementProviderService"
 * @scr.reference name="event.output.adapter.service"
 * interface="org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setOutputEventAdapterService"
 * unbind="unsetOutputEventAdapterService"
 */
public class XMPPPushNotificationServiceComponent {

    private static final Log log = LogFactory.getLog(XMPPPushNotificationServiceComponent.class);

    @SuppressWarnings("unused")
    protected void activate(ComponentContext componentContext) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing XMPP based push notification provider implementation bundle");
            }
            //Do nothing
            if (log.isDebugEnabled()) {
                log.debug("XMPP based push notification provider implementation bundle has been successfully " +
                        "initialized");
            }
        } catch (Throwable e) {
            log.error("Error occurred while initializing XMPP based push notification provider " +
                    "implementation bundle", e);
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        //Do nothing
    }

    protected void setDeviceManagementProviderService(
            DeviceManagementProviderService deviceManagementProviderService) {
        XMPPDataHolder.getInstance().setDeviceManagementProviderService(deviceManagementProviderService);
    }

    protected void unsetDeviceManagementProviderService(
            DeviceManagementProviderService deviceManagementProviderService) {
        XMPPDataHolder.getInstance().setDeviceManagementProviderService(deviceManagementProviderService);
    }

    protected void setOutputEventAdapterService(OutputEventAdapterService outputEventAdapterService){
        XMPPDataHolder.getInstance().setOutputEventAdapterService(outputEventAdapterService);
    }

    protected void unsetOutputEventAdapterService(OutputEventAdapterService outputEventAdapterService){
        XMPPDataHolder.getInstance().setOutputEventAdapterService(null);
    }
}
