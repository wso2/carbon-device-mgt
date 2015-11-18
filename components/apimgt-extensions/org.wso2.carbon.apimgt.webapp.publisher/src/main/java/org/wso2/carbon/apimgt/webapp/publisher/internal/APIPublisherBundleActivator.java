/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.webapp.publisher.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.webapp.publisher.APIPublisherService;
import org.wso2.carbon.apimgt.webapp.publisher.APIPublisherServiceImpl;

public class APIPublisherBundleActivator implements BundleActivator{

    private static Log log = LogFactory.getLog(APIPublisherBundleActivator.class);

    @Override
    public void start(BundleContext bundleContext) throws Exception{
        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing device management core bundle");
            }

            /* Registering declarative service instances exposed by DeviceManagementServiceComponent */
            this.registerServices(bundleContext);

            if (log.isDebugEnabled()) {
                log.debug("Device management core bundle has been successfully initialized");
            }
        } catch (Throwable e) {
            log.error("Error occurred while initializing device management core bundle", e);
        }
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception{
    //do nothing
    }

    private void registerServices(BundleContext bundleContext) {
        if (log.isDebugEnabled()) {
            log.debug("Registering OSGi service DeviceManagementProviderServiceImpl");
        }
        APIPublisherService publisher = new APIPublisherServiceImpl();
        APIPublisherDataHolder.getInstance().setApiPublisherService(publisher);
        bundleContext.registerService(APIPublisherService.class, publisher, null);
    }

    protected void setAPIManagerConfigurationService(APIManagerConfigurationService service) {
        //do nothing
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService service) {
        //do nothing
    }
}
