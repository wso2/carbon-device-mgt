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

package org.wso2.carbon.device.application.mgt.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.application.mgt.api.beans.ErrorResponse;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationReleaseManager;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationStorageManager;
import org.wso2.carbon.device.application.mgt.common.services.LifecycleStateManager;
import org.wso2.carbon.device.application.mgt.common.services.PlatformManager;
import org.wso2.carbon.device.application.mgt.common.services.SubscriptionManager;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;

import javax.ws.rs.core.Response;


/**
 * Holds util methods required for Application-Mgt API component.
 */
public class APIUtil {

    private static Log log = LogFactory.getLog(APIUtil.class);

    private static ApplicationManager applicationManager;
    private static PlatformManager platformManager;
    private static LifecycleStateManager lifecycleStateManager;
    private static ApplicationReleaseManager applicationReleaseManager;
    private static ApplicationStorageManager applicationStorageManager;
    private static SubscriptionManager subscriptionManager;

    public static ApplicationManager getApplicationManager() {
        if (applicationManager == null) {
            synchronized (APIUtil.class) {
                if (applicationManager == null) {
                    PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    applicationManager =
                            (ApplicationManager) ctx.getOSGiService(ApplicationManager.class, null);
                    if (applicationManager == null) {
                        String msg = "Application Manager service has not initialized.";
                        log.error(msg);
                        throw new IllegalStateException(msg);
                    }
                }
            }
        }

        return applicationManager;
    }

    public static PlatformManager getPlatformManager() {
        if (platformManager == null) {
            synchronized (APIUtil.class) {
                if (platformManager == null) {
                    PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    platformManager =
                            (PlatformManager) ctx.getOSGiService(PlatformManager.class, null);
                    if (platformManager == null) {
                        String msg = "Platform Manager service has not initialized.";
                        log.error(msg);
                        throw new IllegalStateException(msg);
                    }
                }
            }
        }
        return platformManager;
    }

    public static LifecycleStateManager getLifecycleStateManager() {
        if (lifecycleStateManager == null) {
            synchronized (APIUtil.class) {
                if (lifecycleStateManager == null) {
                    PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    lifecycleStateManager =
                            (LifecycleStateManager) ctx.getOSGiService(LifecycleStateManager.class, null);
                    if (lifecycleStateManager == null) {
                        String msg = "Lifecycle Manager service has not initialized.";
                        log.error(msg);
                        throw new IllegalStateException(msg);
                    }
                }
            }
        }
        return lifecycleStateManager;
    }

    /**
     * To get the Application Release Manager from the osgi context.
     *
     * @return ApplicationRelease Manager instance in the current osgi context.
     */
    public static ApplicationReleaseManager getApplicationReleaseManager() {
        if (applicationReleaseManager == null) {
            synchronized (APIUtil.class) {
                if (applicationReleaseManager == null) {
                    PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    applicationReleaseManager = (ApplicationReleaseManager) ctx
                            .getOSGiService(ApplicationReleaseManager.class, null);
                    if (applicationReleaseManager == null) {
                        String msg = "Application Release Manager service has not initialized.";
                        log.error(msg);
                        throw new IllegalStateException(msg);
                    }
                }
            }
        }
        return applicationReleaseManager;
    }

    /**
     * To get the Application Storage Manager from the osgi context.
     * @return ApplicationStoreManager instance in the current osgi context.
     */
    public static ApplicationStorageManager getApplicationStorageManager() {
        if (applicationStorageManager == null) {
            synchronized (APIUtil.class) {
                if (applicationStorageManager == null) {
                    PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    applicationStorageManager = (ApplicationStorageManager) ctx
                            .getOSGiService(ApplicationStorageManager.class, null);
                    if (applicationStorageManager == null) {
                        String msg = "Application Storage Manager service has not initialized.";
                        log.error(msg);
                        throw new IllegalStateException(msg);
                    }
                }
            }
        }
        return applicationStorageManager;
    }
    public static Response getResponse(ApplicationManagementException ex, Response.Status status) {
        return getResponse(ex.getMessage(), status);
    }

    public static Response getResponse(String message, Response.Status status) {
        ErrorResponse errorMessage = new ErrorResponse();
        errorMessage.setMessage(message);
        if (status == null) {
            status = Response.Status.INTERNAL_SERVER_ERROR;
        }
        errorMessage.setCode(status.getStatusCode());
        return Response.status(status).entity(errorMessage).build();
    }

    /**
     * To get the Subscription Manager from the osgi context.
     * @return SubscriptionManager instance in the current osgi context.
     */
    public static SubscriptionManager getSubscriptionManager() {
        if (subscriptionManager == null) {
            synchronized (APIUtil.class) {
                if (subscriptionManager == null) {
                    PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    subscriptionManager =
                            (SubscriptionManager) ctx.getOSGiService(SubscriptionManager.class, null);
                    if (subscriptionManager == null) {
                        String msg = "Subscription Manager service has not initialized.";
                        log.error(msg);
                        throw new IllegalStateException(msg);
                    }
                }
            }
        }

        return subscriptionManager;
    }
}
