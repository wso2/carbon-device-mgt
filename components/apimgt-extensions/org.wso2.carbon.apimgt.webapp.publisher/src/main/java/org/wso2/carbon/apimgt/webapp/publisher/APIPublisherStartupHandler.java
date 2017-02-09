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
import org.wso2.carbon.apimgt.webapp.publisher.exception.APIManagerPublisherException;
import org.wso2.carbon.apimgt.webapp.publisher.internal.APIPublisherDataHolder;
import org.wso2.carbon.core.ServerStartupObserver;

import java.util.Stack;

public class APIPublisherStartupHandler implements ServerStartupObserver {

    private static final Log log = LogFactory.getLog(APIPublisherStartupHandler.class);
    private static int retryTime = 2000;
    private static final int CONNECTION_RETRY_FACTOR = 2;
    private static final int MAX_RETRY_COUNT = 5;
    private static Stack<APIConfig> failedAPIsStack = new Stack<>();
    private static Stack<APIConfig> currentAPIsStack;

    private APIPublisherService publisher;

    @Override
    public void completingServerStartup() {

    }

    @Override
    public void completedServerStartup() {
        APIPublisherDataHolder.getInstance().setServerStarted(true);
        currentAPIsStack = APIPublisherDataHolder.getInstance().getUnpublishedApis();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                if (log.isDebugEnabled()) {
                    log.debug("Server has just started, hence started publishing unpublished APIs");
                    log.debug("Total number of unpublished APIs: "
                            + APIPublisherDataHolder.getInstance().getUnpublishedApis().size());
                }
                publisher = APIPublisherDataHolder.getInstance().getApiPublisherService();
                int retryCount = 0;
                while (retryCount < MAX_RETRY_COUNT && (!failedAPIsStack.isEmpty() || !currentAPIsStack.isEmpty())) {
                    try {
                        retryTime = retryTime * CONNECTION_RETRY_FACTOR;
                        Thread.sleep(retryTime);
                    } catch (InterruptedException te) {
                        //do nothing.
                    }
                    Stack<APIConfig> failedApis;
                    if (!APIPublisherDataHolder.getInstance().getUnpublishedApis().isEmpty()) {
                        publishAPIs(currentAPIsStack, failedAPIsStack);
                        failedApis = failedAPIsStack;
                    } else {
                        publishAPIs(failedAPIsStack, currentAPIsStack);
                        failedApis = currentAPIsStack;
                    }
                    retryCount++;
                    if (retryCount == MAX_RETRY_COUNT && !failedApis.isEmpty()) {
                        StringBuilder error = new StringBuilder();
                        error.append("Error occurred while publishing API ['");
                        while (!failedApis.isEmpty()) {
                            APIConfig api = failedApis.pop();
                            error.append(api.getName() + ",");
                        }
                        error.append("']");
                        log.error(error.toString());
                    }
                }

            }
        });
        t.start();
    }

    private void publishAPIs(Stack<APIConfig> apis, Stack<APIConfig> failedStack) {
        while (!apis.isEmpty()) {
            APIConfig api = apis.pop();
            try {
                publisher.publishAPI(api);
            } catch (APIManagerPublisherException e) {
                log.error("failed to publish api.", e);
                failedStack.push(api);
            }
        }
    }

}
