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
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.webapp.publisher.*;
import org.wso2.carbon.apimgt.webapp.publisher.config.APIResource;
import org.wso2.carbon.apimgt.webapp.publisher.config.APIResourceConfiguration;
import org.wso2.carbon.apimgt.webapp.publisher.internal.APIPublisherDataHolder;
import org.wso2.carbon.apimgt.webapp.publisher.lifecycle.util.AnnotationUtil;
import org.wso2.carbon.base.MultitenantConstants;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
    private static final String PARAM_MANAGED_API_APPLICATION = "managed-api-application";
    private static final String PARAM_SHARED_WITH_ALL_TENANTS = "isSharedWithAllTenants";
    private static final String PARAM_PROVIDER_TENANT_DOMAIN = "providerTenantDomain";
    private static final Log log = LogFactory.getLog(APIPublisherLifecycleListener.class);

    @Override
    public void lifecycleEvent(LifecycleEvent lifecycleEvent) {
        if (Lifecycle.AFTER_START_EVENT.equals(lifecycleEvent.getType())) {
            StandardContext context = (StandardContext) lifecycleEvent.getLifecycle();
            ServletContext servletContext = context.getServletContext();
            String param = servletContext.getInitParameter(PARAM_MANAGED_API_ENABLED);
            boolean isManagedApi = (param != null && !param.isEmpty()) && Boolean.parseBoolean(param);

            if (isManagedApi) {
                try {
                    AnnotationUtil annotationUtil = new AnnotationUtil(context);
                    Set<String> annotatedAPIClasses = annotationUtil.
                            scanStandardContext(org.wso2.carbon.apimgt.annotations.api.API.class.getName());
                    List<APIResourceConfiguration> apiDefinitions = annotationUtil.extractAPIInfo(servletContext, annotatedAPIClasses);

                    for (APIResourceConfiguration apiDefinition : apiDefinitions) {
                        APIConfig apiConfig = this.buildApiConfig(servletContext, apiDefinition);
                        try {
                            int tenantId = APIPublisherDataHolder.getInstance().getTenantManager().getTenantId(apiConfig.getTenantDomain());
                            boolean isTenantActive = APIPublisherDataHolder.getInstance().getTenantManager().isTenantActive(tenantId);
                            if (isTenantActive) {
                                apiConfig.init();
                                API api = APIPublisherUtil.getAPI(apiConfig);
                                APIPublisherService apiPublisherService =
                                        APIPublisherDataHolder.getInstance().getApiPublisherService();
                                if (apiPublisherService == null) {
                                    throw new IllegalStateException(
                                            "API Publisher service is not initialized properly");
                                }
                                apiPublisherService.publishAPI(api);
                            } else {
                                log.error("No tenant [" + apiConfig.getTenantDomain() + "] found when publishing the webapp");
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
                }
            }
        }
    }

    private List<APIResourceConfiguration> mergeAPIDefinitions(List<APIResourceConfiguration> inputList) {
        //TODO : Need to implemented, to merge API Definitions in cases where implementation of an API Lies in two
        // classes
        return null;
    }

    /**
     * Build the API Configuration to be passed to APIM, from a given list of URL templates
     *
     * @param servletContext
     * @return
     */
    private APIConfig buildApiConfig(ServletContext servletContext, APIResourceConfiguration apidef) {
        APIConfig apiConfig = new APIConfig();

        String name = apidef.getName();
        if (name == null || name.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("API Name not set in @API Annotation");
            }
            name = servletContext.getServletContextName();
        }
        apiConfig.setName(name);

        String version = apidef.getVersion();
        if (version == null || version.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("'API Version not set in @API Annotation'");
            }
            version = API_CONFIG_DEFAULT_VERSION;
        }
        apiConfig.setVersion(version);


        String context = apidef.getContext();
        if (context == null || context.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("'API Context not set in @API Annotation'");
            }
            context = servletContext.getContextPath();
        }
        apiConfig.setContext(context);

        String[] tags = apidef.getTags();
        if (tags == null || tags.length == 0) {
            if (log.isDebugEnabled()) {
                log.debug("'API tag not set in @API Annotation'");
            }
        } else {
            apiConfig.setTags(tags);
        }

        String tenantDomain = servletContext.getInitParameter(PARAM_PROVIDER_TENANT_DOMAIN);
        tenantDomain = (tenantDomain != null && !tenantDomain.isEmpty()) ? tenantDomain :
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        apiConfig.setTenantDomain(tenantDomain);
        String contextTemplate = context + "/" + APIConstants.VERSION_PLACEHOLDER;
        if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            contextTemplate = context + "/t/" + tenantDomain + "/" + APIConstants.VERSION_PLACEHOLDER;
        }
        apiConfig.setContextTemplate(contextTemplate);

        String endpoint = servletContext.getInitParameter(PARAM_MANAGED_API_ENDPOINT);
        if (endpoint == null || endpoint.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("'managed-api-endpoint' attribute is not configured");
            }
            String endpointContext = servletContext.getContextPath();
            endpoint = APIPublisherUtil.getApiEndpointUrl(endpointContext);
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

        String sharingValueParam = servletContext.getInitParameter(PARAM_SHARED_WITH_ALL_TENANTS);
        boolean isSharedWithAllTenants = (sharingValueParam == null || (!sharingValueParam.isEmpty()) && Boolean.parseBoolean(
                sharingValueParam));
        apiConfig.setSharedWithAllTenants(isSharedWithAllTenants);

        Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();
        Scope scope;
        for (APIResource apiResource : apidef.getResources()) {
            URITemplate template = new URITemplate();
            template.setAuthType(apiResource.getAuthType());
            template.setHTTPVerb(apiResource.getHttpVerb());
            template.setResourceURI(apiResource.getUri());
            template.setUriTemplate(apiResource.getUriTemplate());;
            template.setScope(apiResource.getScope());
            uriTemplates.add(template);
        }
        apiConfig.setUriTemplates(uriTemplates);
        return apiConfig;
    }

}
