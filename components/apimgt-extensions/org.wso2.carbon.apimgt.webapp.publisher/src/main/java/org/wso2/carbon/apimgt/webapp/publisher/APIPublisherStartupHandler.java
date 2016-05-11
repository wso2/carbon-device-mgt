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

package org.wso2.carbon.apimgt.webapp.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.webapp.publisher.internal.APIPublisherDataHolder;
import org.wso2.carbon.core.ServerStartupObserver;

public class APIPublisherStartupHandler implements ServerStartupObserver {

    private static final Log log = LogFactory.getLog(APIPublisherStartupHandler.class);

    @Override
    public void completingServerStartup() {

    }

    @Override
    public void completedServerStartup() {
        // adding temporary due to a bug in the platform
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    log.error("Error occurred while sleeping", e);
                }
                APIPublisherDataHolder.getInstance().setServerStarted(true);
                log.info("Server has just started, hence started publishing unpublished APIs");
                if (log.isDebugEnabled()) {
                    log.debug("Total number of unpublished APIs: "
                            + APIPublisherDataHolder.getInstance().getUnpublishedApis().size());
                }
                APIPublisherService publisher = APIPublisherDataHolder.getInstance().getApiPublisherService();
                while (!APIPublisherDataHolder.getInstance().getUnpublishedApis().isEmpty()) {
                    API api = APIPublisherDataHolder.getInstance().getUnpublishedApis().pop();
                    try {
                        publisher.publishAPI(api);
                    } catch (java.lang.Exception e) {
                        log.error("Error occurred while publishing API '" + api.getId().getApiName(), e);
                    }
                }
            }
        });
        t.start();
    }
}
