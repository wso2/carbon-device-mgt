/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.webapp.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.webapp.publisher.config.APIResource;
import org.wso2.carbon.apimgt.webapp.publisher.config.APIResourceConfiguration;
import org.wso2.carbon.apimgt.webapp.publisher.config.WebappPublisherConfig;
import org.wso2.carbon.apimgt.webapp.publisher.lifecycle.util.AnnotationProcessor;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.Utils;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.servlet.ServletContext;
import java.util.*;

public class APIPublisherUtil {

    public static final String API_VERSION_PARAM = "{version}";
    public static final String API_PUBLISH_ENVIRONMENT = "Production and Sandbox";
    private static final Log log = LogFactory.getLog(APIPublisherUtil.class);
    private static final String DEFAULT_API_VERSION = "1.0.0";
    private static final String API_CONFIG_DEFAULT_VERSION = "1.0.0";
    private static final String PARAM_MANAGED_API_ENDPOINT = "managed-api-endpoint";
    private static final String PARAM_MANAGED_API_TRANSPORTS = "managed-api-transports";
    private static final String PARAM_MANAGED_API_POLICY = "managed-api-policy";
    private static final String PARAM_MANAGED_API_IS_SECURED = "managed-api-isSecured";
    private static final String PARAM_SHARED_WITH_ALL_TENANTS = "isSharedWithAllTenants";
    private static final String PARAM_PROVIDER_TENANT_DOMAIN = "providerTenantDomain";

    private static final String NON_SECURED_RESOURCES = "nonSecuredEndPoints";
    private static final String AUTH_TYPE_NON_SECURED = "None";


    public static API getAPI(APIConfig config) throws APIManagementException {

        APIProvider provider = config.getProvider();
        String apiVersion = config.getVersion();
        APIIdentifier id = new APIIdentifier(replaceEmailDomain(config.getOwner()), config.getName(), apiVersion);
        API api = new API(id);

        api.setApiOwner(config.getOwner());
        String context = config.getContext();
        context = context.startsWith("/") ? context : ("/" + context);
        String providerDomain = config.getTenantDomain();
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(providerDomain)) {
            //Create tenant aware context for API
            context = "/t/" + providerDomain + context;
        }

        // This is to support the new Pluggable version strategy
        // if the context does not contain any {version} segment, we use the default version strategy.
        context = checkAndSetVersionParam(context);
        api.setContextTemplate(context);
        context = updateContextWithVersion(config.getVersion(), context);
        api.setContext(context);

        api.setUrl(config.getEndpoint());
        api.addAvailableTiers(provider.getTiers());
        api.setEndpointSecured(false);
        api.setStatus(APIStatus.CREATED);
        api.setTransports(config.getTransports());
        api.setApiLevelPolicy(config.getPolicy());
        api.setContextTemplate(config.getContextTemplate());
        Set<String> environments = new HashSet<>();
        environments.add(API_PUBLISH_ENVIRONMENT);
        api.setEnvironments(environments);
        Set<Tier> tiers = new HashSet<>();

        tiers.add(new Tier(APIConstants.UNLIMITED_TIER));
        api.addAvailableTiers(tiers);

        if (config.isSharedWithAllTenants()) {
            api.setSubscriptionAvailability(APIConstants.SUBSCRIPTION_TO_ALL_TENANTS);
            api.setVisibility(APIConstants.API_GLOBAL_VISIBILITY);
        } else {
            api.setSubscriptionAvailability(APIConstants.SUBSCRIPTION_TO_CURRENT_TENANT);
            api.setVisibility(APIConstants.API_PRIVATE_VISIBILITY);
        }
        api.setResponseCache(APIConstants.DISABLED);

        String endpointConfig = "{\"production_endpoints\":{\"url\":\"" + config.getEndpoint() +
                "\",\"config\":null},\"implementation_status\":\"managed\",\"endpoint_type\":\"http\"}";

        api.setEndpointConfig(endpointConfig);

        if ("".equals(id.getVersion()) || (DEFAULT_API_VERSION.equals(id.getVersion()))) {
            api.setAsDefaultVersion(Boolean.TRUE);
            api.setAsPublishedDefaultVersion(Boolean.TRUE);
        }
        if (config.getTags() != null && config.getTags().length > 0) {
            Set<String> tags = new HashSet<>(Arrays.asList(config.getTags()));
            api.addTags(tags);
        }

        // adding scopes to the api
        Set<URITemplate> uriTemplates = config.getUriTemplates();
        Map<String, Scope> apiScopes = new HashMap<>();
        if (uriTemplates != null) {
            // this creates distinct scopes list
            for (URITemplate template : uriTemplates) {
                Scope scope = template.getScope();
                if (scope != null) {
                    if (apiScopes.get(scope.getKey()) == null) {
                        apiScopes.put(scope.getKey(), scope);
                    }
                }
            }
            Set<Scope> scopes = new HashSet<>(apiScopes.values());
            // set current scopes to API
            api.setScopes(scopes);

            // this has to be done because of the use of pass by reference
            // where same object reference of scope should be available for both
            // api scope and uri template scope
            for (Scope scope : scopes) {
                for (URITemplate template : uriTemplates) {
                    if (template.getScope() != null && scope.getKey().equals(template.getScope().getKey())) {
                        template.setScope(scope);
                    }
                }
            }
            api.setUriTemplates(uriTemplates);
        }
        api.setCorsConfiguration(APIUtil.getDefaultCorsConfiguration());
        return api;
    }

    public static String getServerBaseUrl() {
        WebappPublisherConfig webappPublisherConfig = WebappPublisherConfig.getInstance();
        return Utils.replaceSystemProperty(webappPublisherConfig.getHost());
    }

    public static String getApiEndpointUrl(String context) {
        return getServerBaseUrl() + context;
    }

    /**
     * When an input is having '@',replace it with '-AT-'
     * [This is required to persist API data in registry,as registry paths don't allow '@' sign.]
     *
     * @param input inputString
     * @return String modifiedString
     */
    private static String replaceEmailDomain(String input) {
        if (input != null && input.contains(APIConstants.EMAIL_DOMAIN_SEPARATOR)) {
            input = input.replace(APIConstants.EMAIL_DOMAIN_SEPARATOR, APIConstants.EMAIL_DOMAIN_SEPARATOR_REPLACEMENT);
        }
        return input;
    }

    private static String updateContextWithVersion(String version, String context) {
        // This condition should not be true for any occasion but we keep it so that there are no loopholes in
        // the flow.
        context = context.replace(API_VERSION_PARAM, version);
        return context;
    }

    private static String checkAndSetVersionParam(String context) {
        // This is to support the new Pluggable version strategy
        // if the context does not contain any {version} segment, we use the default version strategy.
        if (!context.contains(API_VERSION_PARAM)) {
            if (!context.endsWith("/")) {
                context = context + "/";
            }
            context = context + API_VERSION_PARAM;
        }
        return context;
    }

    /**
     * Build the API Configuration to be passed to APIM, from a given list of URL templates
     *
     * @param servletContext
     * @param apiDef
     * @return
     */
    public static APIConfig buildApiConfig(ServletContext servletContext, APIResourceConfiguration apiDef)
            throws UserStoreException {
        APIConfig apiConfig = new APIConfig();

        String name = apiDef.getName();
        if (name == null || name.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("API Name not set in @SwaggerDefinition Annotation");
            }
            name = servletContext.getServletContextName();
        }
        apiConfig.setName(name);

        String version = apiDef.getVersion();
        if (version == null || version.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("'API Version not set in @SwaggerDefinition Annotation'");
            }
            version = API_CONFIG_DEFAULT_VERSION;
        }
        apiConfig.setVersion(version);


        String context = apiDef.getContext();
        if (context == null || context.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("'API Context not set in @SwaggerDefinition Annotation'");
            }
            context = servletContext.getContextPath();
        }
        apiConfig.setContext(context);

        String[] tags = apiDef.getTags();
        if (tags == null || tags.length == 0) {
            if (log.isDebugEnabled()) {
                log.debug("'API tag not set in @SwaggerDefinition Annotation'");
            }
        } else {
            apiConfig.setTags(tags);
        }

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        servletContext.setAttribute(PARAM_PROVIDER_TENANT_DOMAIN, tenantDomain);
        tenantDomain = (tenantDomain != null && !tenantDomain.isEmpty()) ? tenantDomain :
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        apiConfig.setTenantDomain(tenantDomain);
        String contextTemplate = context + "/" + APIConstants.VERSION_PLACEHOLDER;
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            contextTemplate = context + "/t/" + tenantDomain + "/" + APIConstants.VERSION_PLACEHOLDER;
        }
        apiConfig.setContextTemplate(contextTemplate);

        String endpoint = servletContext.getInitParameter(PARAM_MANAGED_API_ENDPOINT);
        if (endpoint == null || endpoint.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("'managed-api-endpoint' attribute is not configured");
            }
            String endpointContext = apiDef.getContext();
            endpoint = APIPublisherUtil.getApiEndpointUrl(endpointContext);
        }
        apiConfig.setEndpoint(endpoint);

        String owner = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm().getRealmConfiguration()
                .getAdminUserName();
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            owner = owner + "@" + tenantDomain;
        }
        if (owner == null || owner.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("'managed-api-owner' attribute is not configured");
            }
        }
        apiConfig.setOwner(owner);

        apiConfig.setSecured(false);

        String transports = servletContext.getInitParameter(PARAM_MANAGED_API_TRANSPORTS);
        if (transports == null || transports.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("'managed-api-transports' attribute is not configured. Therefore using the default, " +
                        "which is 'https'");
            }
            transports = "https,http";
        }
        apiConfig.setTransports(transports);

        String sharingValueParam = servletContext.getInitParameter(PARAM_SHARED_WITH_ALL_TENANTS);
        boolean isSharedWithAllTenants = Boolean.parseBoolean(sharingValueParam);
        if (isSharedWithAllTenants && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            isSharedWithAllTenants = false;
        }
        apiConfig.setSharedWithAllTenants(isSharedWithAllTenants);

        Set<URITemplate> uriTemplates = new LinkedHashSet<>();
        for (APIResource apiResource : apiDef.getResources()) {
            URITemplate template = new URITemplate();
            template.setAuthType(apiResource.getAuthType());
            template.setHTTPVerb(apiResource.getHttpVerb());
            template.setResourceURI(apiResource.getUri());
            template.setUriTemplate(apiResource.getUriTemplate());
            template.setScope(apiResource.getScope());
            uriTemplates.add(template);
        }
        apiConfig.setUriTemplates(uriTemplates);

        String policy = servletContext.getInitParameter(PARAM_MANAGED_API_POLICY);
        if (policy == null || policy.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("'managed-api-policy' attribute is not configured. Therefore using the default, " +
                        "which is 'null'");
            }
            policy = null;
        }
        apiConfig.setPolicy(policy);

        return apiConfig;
    }


    public static void setResourceAuthTypes(ServletContext servletContext, APIConfig apiConfig) {
        List<String> resourcesList = null;
        String nonSecuredResources = servletContext.getInitParameter(NON_SECURED_RESOURCES);
        if(null != nonSecuredResources){
            resourcesList = Arrays.asList(nonSecuredResources.split(","));
        }
        Set<URITemplate> templates = apiConfig.getUriTemplates();
        if(null != resourcesList) {
            for (URITemplate template : templates) {
                String fullPaath = "";
                if( template.getUriTemplate() != AnnotationProcessor.WILD_CARD ) {
                    fullPaath = apiConfig.getContext() + template.getUriTemplate();
                }
                else{
                    fullPaath = apiConfig.getContext();
                }
                for(String context : resourcesList) {
                    if (context.trim().equals(fullPaath)) {
                        template.setAuthType(AUTH_TYPE_NON_SECURED);
                    }
                }
            }
        }
    }
}
