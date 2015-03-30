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
package org.wso2.carbon.device.mgt.core.api.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.api.mgt.config.APIPublisherConfig;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;

import java.util.List;

/**
 * This particular class corresponding to the ServerStartupObserver written for publishing the set of APIs used by
 * the device management related components.
 *
 * Note: Using this particular approach is not a must, had there been a proper programming interface provided by the
 * underlying API-Management infrastructure for manipulating the APIs. Even though, there's one, its concrete
 * implementation consumes a set of OSGi declarative services for initializing some of its internal states, which
 * prevents us from, simply, instantiating the APIPublisher implementation and using for device management related
 * tasks. The aforesaid complication lead us to go for this alternative approach to get the same done.
 */
public class APIRegistrationStartupObserver implements ServerStartupObserver {

    private static final Log log = LogFactory.getLog(APIRegistrationStartupObserver.class);

    @Override
    public void completingServerStartup() {

    }

    @Override
    public void completedServerStartup() {
        /* Publish all mobile device management related JAX-RS services as APIs */
        if (log.isDebugEnabled()) {
            log.debug("Publishing all mobile device management related JAX-RS services as APIs");
        }
        List<APIConfig> apiConfigs = APIPublisherConfig.getInstance().getApiConfigs();
        for (APIConfig apiConfig : apiConfigs) {
            try {
                /* API Config is initialized at this point in order to avoid OSGi declarative services which
                the APIManagerComponent depend on, are deployed and initialized before invoking methods in
                APIManagerFactory  */
                apiConfig.init();

                API api = DeviceManagerUtil.getAPI(apiConfig);
                DeviceManagementDataHolder.getInstance().getApiPublisherService().publishAPI(api);

                log.info("Successfully published API '" + apiConfig.getName() + "' with the context '" +
                        apiConfig.getContext() + "' and version '" + apiConfig.getVersion() + "'");
            } catch (Throwable e) {
                /* Throwable is caught as none of the RuntimeExceptions that can potentially occur at this point
                does not seem to be logged anywhere else within the framework */
                log.error("Error occurred while publishing API '" + apiConfig.getName() + "' with the context '" +
                        apiConfig.getContext() + "' and version '" + apiConfig.getVersion() + "'", e);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("End of publishing all mobile device management related JAX-RS services as APIs");
        }
    }

}
