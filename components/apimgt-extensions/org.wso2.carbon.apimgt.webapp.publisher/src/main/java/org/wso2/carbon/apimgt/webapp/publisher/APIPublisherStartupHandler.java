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

import java.util.Stack;

public class APIPublisherStartupHandler implements ServerStartupObserver {

    private static final Log log = LogFactory.getLog(APIPublisherStartupHandler.class);
    private static int retryTime = 2000;
    private static final int CONNECTION_RETRY_FACTOR = 2;
    private static final int MAX_RETRY_COUNT = 5;
    private static Stack<API> failedAPIsStack = new Stack<>();
    private static Stack<API> currentAPIsStack;

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

                //coap client bound to the server[the server is in the static default port for now]
                APIPublisherDataHolder.getInstance().setClient(new CoAPResourceDirectoryClient());
//                if (APIPublisherDataHolder.getInstance().getClient().isServerConnected()) {
//                    if (log.isDebugEnabled()) {
//                        log.debug("Client set to the started coap server @ " + APIPublisherDataHolder.getInstance().getClient().getURI());
//                    }
//                } else {
//                    if (log.isDebugEnabled()) {
//                        log.debug("Coap server not connected");
//                    }
//                }

                publisher = APIPublisherDataHolder.getInstance().getApiPublisherService();
                int retryCount = 0;
                while (retryCount < MAX_RETRY_COUNT && (!failedAPIsStack.isEmpty() || !currentAPIsStack.isEmpty())) {
                    try {
                        retryTime = retryTime * CONNECTION_RETRY_FACTOR;
                        Thread.sleep(retryTime);
                    } catch (InterruptedException te) {
                        //do nothing.
                    }
                    Stack<API> failedApis;
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
                            API api = failedApis.pop();
                            error.append(api.getId().getApiName() + ",");
                        }
                        error.append("']");
                        log.error(error.toString());
                    }
                }

            }
        });
        t.start();
    }

    private void publishAPIs(Stack<API> apis, Stack<API> failedStack) {
        while (!apis.isEmpty()) {
            API api = apis.pop();
            try {
                publisher.publishAPI(api);
            } catch (Exception e) {
                failedStack.push(api);
            }
        }
    }

}
