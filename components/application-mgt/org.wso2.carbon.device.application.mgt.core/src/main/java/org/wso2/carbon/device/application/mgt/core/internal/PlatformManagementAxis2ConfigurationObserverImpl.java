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

package org.wso2.carbon.device.application.mgt.core.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.application.mgt.common.exception.PlatformManagementException;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

/**
 * PlatformManagementAxis2ConfigurationObserverImpl is responsible for adding relevant platform mapping of shared
 * platforms during the tenant creation time.
 */
public class PlatformManagementAxis2ConfigurationObserverImpl extends AbstractAxis2ConfigurationContextObserver {
    private static Log log = LogFactory.getLog(PlatformManagementAxis2ConfigurationObserverImpl.class);

    /**
     * Whenever a new tenant creation happens, shared platforms need to be added for the relevant tenant.
     * @param tenantId Id of the tenant that is being created
     */
    @Override
    public void creatingConfigurationContext(int tenantId) {
        try {
            DataHolder.getInstance().getPlatformManager().initialize(tenantId);
        } catch (PlatformManagementException e) {
            log.error("Error while trying add platforms to the newly created tenant " + tenantId, e);
        }
    }

    /**
     * Whenever terminating a tenant,the platforms added by the tenant need to be removed.
     * @param configContext Configuration context.
     */
    @Override
    public void terminatingConfigurationContext(ConfigurationContext configContext) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        try {
            DataHolder.getInstance().getPlatformManager().removePlatforms(tenantId);
        } catch (PlatformManagementException e) {
            log.error("Error while removing shared platforms while removing the tenant: " + tenantId, e);
        }
    }
}
