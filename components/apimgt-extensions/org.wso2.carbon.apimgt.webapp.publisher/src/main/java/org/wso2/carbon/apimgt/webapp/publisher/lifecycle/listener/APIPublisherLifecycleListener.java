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
package org.wso2.carbon.apimgt.webapp.publisher.lifecycle.listener;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.webapp.publisher.APIConfig;
import org.wso2.carbon.apimgt.webapp.publisher.APIPublisherService;
import org.wso2.carbon.apimgt.webapp.publisher.APIPublisherUtil;
import org.wso2.carbon.apimgt.webapp.publisher.config.APIResourceConfiguration;
import org.wso2.carbon.apimgt.webapp.publisher.config.WebappPublisherConfig;
import org.wso2.carbon.apimgt.webapp.publisher.internal.APIPublisherDataHolder;
import org.wso2.carbon.apimgt.webapp.publisher.lifecycle.util.AnnotationProcessor;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserStoreException;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class APIPublisherLifecycleListener implements LifecycleListener {

    private static final Log log = LogFactory.getLog(APIPublisherLifecycleListener.class);
    private static final String PARAM_MANAGED_API_ENABLED = "managed-api-enabled";
    public static final String PROPERTY_PROFILE = "profile";
    public static final String PROFILE_DT_WORKER = "dtWorker";
    public static final String PROFILE_DEFAULT = "default";

    @Override
    public void lifecycleEvent(LifecycleEvent lifecycleEvent) {
        if (Lifecycle.AFTER_START_EVENT.equals(lifecycleEvent.getType()) && WebappPublisherConfig.getInstance()
                .isPublished()) {
            StandardContext context = (StandardContext) lifecycleEvent.getLifecycle();
            ServletContext servletContext = context.getServletContext();
            String param = servletContext.getInitParameter(PARAM_MANAGED_API_ENABLED);
            boolean isManagedApi = (param != null && !param.isEmpty()) && Boolean.parseBoolean(param);

            String profile = System.getProperty(PROPERTY_PROFILE);
            if (WebappPublisherConfig.getInstance().getProfiles().getProfile().contains(profile.toLowerCase())
                    && isManagedApi) {
                try {
                    AnnotationProcessor annotationProcessor = new AnnotationProcessor(context);
                    Set<String> annotatedSwaggerAPIClasses = annotationProcessor.
                            scanStandardContext(io.swagger.annotations.SwaggerDefinition.class.getName());
                    List<APIResourceConfiguration> apiDefinitions = annotationProcessor.extractAPIInfo(servletContext,
                            annotatedSwaggerAPIClasses);
                    for (APIResourceConfiguration apiDefinition : apiDefinitions) {
                        APIConfig apiConfig = APIPublisherUtil.buildApiConfig(servletContext, apiDefinition);
                        APIPublisherUtil.setResourceAuthTypes(servletContext,apiConfig);
                        try {
                            int tenantId = APIPublisherDataHolder.getInstance().getTenantManager().
                                    getTenantId(apiConfig.getTenantDomain());

                            boolean isTenantActive = APIPublisherDataHolder.getInstance().
                                    getTenantManager().isTenantActive(tenantId);
                            if (isTenantActive) {
                                boolean isServerStarted = APIPublisherDataHolder.getInstance().isServerStarted();
                                if (isServerStarted) {
                                    APIPublisherService apiPublisherService =
                                            APIPublisherDataHolder.getInstance().getApiPublisherService();
                                    if (apiPublisherService == null) {
                                        throw new IllegalStateException(
                                                "API Publisher service is not initialized properly");
                                    }
                                    apiPublisherService.publishAPI(apiConfig);
                                } else {
                                    if (log.isDebugEnabled()) {
                                        log.debug("Server has not started yet. Hence adding API '" +
                                                apiConfig.getName() + "' to the queue");
                                    }
                                    APIPublisherDataHolder.getInstance().getUnpublishedApis().push(apiConfig);
                                }
                            } else {
                                log.error("No tenant [" + apiConfig.getTenantDomain() + "] " +
                                        "found when publishing the Web app");
                            }
                        } catch (Throwable e) {
                            log.error("Error occurred while publishing API '" + apiConfig.getName() +
                                    "' with the context '" + apiConfig.getContext() +
                                    "' and version '" + apiConfig.getVersion() + "'", e);
                        }
                    }
                } catch (IOException e) {
                    log.error("Error encountered while discovering annotated classes", e);
                } catch (ClassNotFoundException e) {
                    log.error("Error while scanning class for annotations", e);
                } catch (UserStoreException e) {
                    log.error("Error while retrieving tenant admin user for the tenant domain"
                                      + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(), e);
                } catch (Throwable e) {
                    // This is done to stop tomcat failure if a webapp failed to publish apis.
                    log.error("Failed to Publish api from " + servletContext.getContextPath(), e);
                }
            }
        }
    }
}
