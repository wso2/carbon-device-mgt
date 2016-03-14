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
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.webapp.publisher.APIConfig;
import org.wso2.carbon.apimgt.webapp.publisher.APIPublisherService;
import org.wso2.carbon.apimgt.webapp.publisher.APIPublisherUtil;
import org.wso2.carbon.apimgt.webapp.publisher.internal.APIPublisherDataHolder;

import javax.servlet.ServletContext;

@SuppressWarnings("unused")
public class APIPublisherLifecycleListener implements LifecycleListener {

    private static final String API_CONFIG_DEFAULT_VERSION = "1.0.0";

    private static final String PARAM_MANAGED_API_ENABLED = "managed-api-enabled";
    private static final String PARAM_MANAGED_API_NAME = "managed-api-name";
    private static final String PARAM_MANAGED_API_VERSION = "managed-api-version";
    private static final String PARAM_MANAGED_API_CONTEXT = "managed-api-context";
    private static final String PARAM_MANAGED_API_ENDPOINT = "managed-api-endpoint";
    private static final String PARAM_MANAGED_API_OWNER = "managed-api-owner";
    private static final String PARAM_MANAGED_API_TRANSPORTS = "managed-api-transports";
    private static final String PARAM_MANAGED_API_IS_SECURED = "managed-api-isSecured";

    private static final Log log = LogFactory.getLog(APIPublisherLifecycleListener.class);

    @Override
    public void lifecycleEvent(LifecycleEvent lifecycleEvent) {
        if (Lifecycle.AFTER_START_EVENT.equals(lifecycleEvent.getType())) {
            StandardContext context = (StandardContext) lifecycleEvent.getLifecycle();
            ServletContext servletContext = context.getServletContext();


            String param = servletContext.getInitParameter(PARAM_MANAGED_API_ENABLED);
            boolean isManagedApi = (param != null && !param.isEmpty()) && Boolean.parseBoolean(param);

            if (isManagedApi) {
                APIConfig apiConfig = this.buildApiConfig(servletContext);
                try {
                    apiConfig.init();
                    API api = APIPublisherUtil.getAPI(apiConfig);
                    APIPublisherService apiPublisherService =
                            APIPublisherDataHolder.getInstance().getApiPublisherService();
                    if (apiPublisherService == null) {
                        throw new IllegalStateException("API Publisher service is not initialized properly");
                    }
                    apiPublisherService.publishAPI(api);
                } catch (Throwable e) {
                    /* Throwable is caught as none of the RuntimeExceptions that can potentially occur at this point
                    does not seem to be logged anywhere else within the framework */
                    log.error("Error occurred while publishing API '" + apiConfig.getName() + "' with the context '" +
                            apiConfig.getContext() + "' and version '" + apiConfig.getVersion() + "'", e);
                }
            }
        }
    }

    private APIConfig buildApiConfig(ServletContext servletContext) {
        APIConfig apiConfig = new APIConfig();

        String name = servletContext.getInitParameter(PARAM_MANAGED_API_NAME);
        if (name == null || name.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("'managed-api-name' attribute is not configured. Therefore, using the default, " +
                        "which is the name of the web application");
            }
            name = servletContext.getServletContextName();
        }
        apiConfig.setName(name);

        String version = servletContext.getInitParameter(PARAM_MANAGED_API_VERSION);
        if (version == null || version.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("'managed-api-version' attribute is not configured. Therefore, using the " +
                        "default, which is '1.0.0'");
            }
            version = API_CONFIG_DEFAULT_VERSION;
        }
        apiConfig.setVersion(version);

        String context = servletContext.getInitParameter(PARAM_MANAGED_API_CONTEXT);
        if (context == null || context.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("'managed-api-context' attribute is not configured. Therefore, using the default, " +
                        "which is the original context assigned to the web application");
            }
            context = servletContext.getContextPath();
        }
        apiConfig.setContext(context);
        apiConfig.setContextTemplate(context);

        String endpoint = servletContext.getInitParameter(PARAM_MANAGED_API_ENDPOINT);
        if (endpoint == null || endpoint.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("'managed-api-endpoint' attribute is not configured");
            }
            endpoint = APIPublisherUtil.getApiEndpointUrl(context);
        }
        apiConfig.setEndpoint(endpoint);

        String owner = servletContext.getInitParameter(PARAM_MANAGED_API_OWNER);
        if (owner == null || owner.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("'managed-api-owner' attribute is not configured");
            }
        }
        apiConfig.setOwner(owner);

        String isSecuredParam = servletContext.getInitParameter(PARAM_MANAGED_API_IS_SECURED);
        boolean isSecured;
        if (isSecuredParam == null || isSecuredParam.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("'managed-api-isSecured' attribute is not configured. Therefore, using the default, " +
                        "which is 'true'");
            }
            isSecured = false;
        } else {
            isSecured = Boolean.parseBoolean(isSecuredParam);
        }
        apiConfig.setSecured(isSecured);

        String transports = servletContext.getInitParameter(PARAM_MANAGED_API_TRANSPORTS);
        if (transports == null || transports.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("'managed-api-transports' attribute is not configured. Therefore using the default, " +
                        "which is 'https'");
            }
            transports = "https";
        }
        apiConfig.setTransports(transports);

        return apiConfig;
    }

}
