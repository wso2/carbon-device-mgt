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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.webapp.publisher.config.APIResource;
import org.wso2.carbon.apimgt.webapp.publisher.config.APIResourceConfiguration;
import org.wso2.carbon.apimgt.webapp.publisher.config.WebappPublisherConfig;
import org.wso2.carbon.apimgt.webapp.publisher.dto.ApiScope;
import org.wso2.carbon.apimgt.webapp.publisher.dto.ApiUriTemplate;
import org.wso2.carbon.apimgt.webapp.publisher.lifecycle.util.AnnotationProcessor;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.Utils;
import org.wso2.carbon.user.api.UserStoreException;

import javax.servlet.ServletContext;
import java.util.*;

public class APIPublisherUtil {

    public static final String API_VERSION_PARAM = "{version}";
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

    public static String getServerBaseUrl() {
        WebappPublisherConfig webappPublisherConfig = WebappPublisherConfig.getInstance();
        return Utils.replaceSystemProperty(webappPublisherConfig.getHost());
    }

    public static String getApiEndpointUrl(String context) {
        return getServerBaseUrl() + context;
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

        Set<ApiUriTemplate> uriTemplates = new LinkedHashSet<>();
        for (APIResource apiResource : apiDef.getResources()) {
            ApiUriTemplate template = new ApiUriTemplate();
            template.setAuthType(apiResource.getAuthType());
            template.setHttpVerb(apiResource.getHttpVerb());
            template.setResourceURI(apiResource.getUri());
            template.setUriTemplate(apiResource.getUriTemplate());
            template.setScope(apiResource.getScope());
            uriTemplates.add(template);
        }
        apiConfig.setUriTemplates(uriTemplates);
        // adding scopes to the api
        Map<String, ApiScope> apiScopes = new HashMap<>();
        if (uriTemplates != null) {
            // this creates distinct scopes list
            for (ApiUriTemplate template : uriTemplates) {
                ApiScope scope = template.getScope();
                if (scope != null) {
                    if (apiScopes.get(scope.getKey()) == null) {
                        apiScopes.put(scope.getKey(), scope);
                    }
                }
            }
            Set<ApiScope> scopes = new HashSet<>(apiScopes.values());
            // set current scopes to API
            apiConfig.setScopes(scopes);
        }

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

    public static String getSwaggerDefinition(APIConfig apiConfig) {
        Map<String, JsonObject> httpVerbsMap = new HashMap<>();
        List<ApiScope> scopes = new ArrayList<>();

        for (ApiUriTemplate uriTemplate : apiConfig.getUriTemplates()) {
            JsonObject response = new JsonObject();
            response.addProperty("200", "");

            JsonObject responses = new JsonObject();
            responses.add("responses", response);
            JsonObject httpVerbs = httpVerbsMap.get(uriTemplate.getUriTemplate());
            if (httpVerbs == null) {
                httpVerbs = new JsonObject();
            }
            JsonObject httpVerb = new JsonObject();
            httpVerb.add("responses", response);

            httpVerb.addProperty("x-auth-type", uriTemplate.getAuthType());
            httpVerb.addProperty("x-throttling-tier", "Unlimited");
            if (uriTemplate.getScope() != null) {
                httpVerb.addProperty("x-scope", uriTemplate.getScope().getKey());
                scopes.add(uriTemplate.getScope());
            }
            httpVerbs.add(uriTemplate.getHttpVerb().toLowerCase(), httpVerb);
            httpVerbsMap.put(uriTemplate.getUriTemplate(), httpVerbs);
        }

        Iterator it = httpVerbsMap.entrySet().iterator();
        JsonObject paths = new JsonObject();
        while (it.hasNext()) {
            Map.Entry<String, JsonObject> pair = (Map.Entry) it.next();
            paths.add(pair.getKey(), pair.getValue());
            it.remove();
        }

        JsonObject info = new JsonObject();
        info.addProperty("title", apiConfig.getName());
        info.addProperty("version", apiConfig.getVersion());

        JsonObject swaggerDefinition = new JsonObject();
        swaggerDefinition.add("paths", paths);
        swaggerDefinition.addProperty("swagger", "2.0");
        swaggerDefinition.add("info", info);

        // adding scopes to swagger definition
        if (!apiConfig.getScopes().isEmpty()) {
            Gson gson = new Gson();
            JsonElement element = gson.toJsonTree(apiConfig.getScopes(), new TypeToken<Set<Scope>>() {
            }.getType());
            if (element != null) {
                JsonArray apiScopes = element.getAsJsonArray();
                JsonObject apim = new JsonObject();
                apim.add("x-wso2-scopes", apiScopes);
                JsonObject wso2Security = new JsonObject();
                wso2Security.add("apim", apim);
                swaggerDefinition.add("x-wso2-security", wso2Security);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("API swagger definition: " + swaggerDefinition.toString());
        }
        return swaggerDefinition.toString();
    }


    public static void setResourceAuthTypes(ServletContext servletContext, APIConfig apiConfig) {
        List<String> resourcesList = null;
        String nonSecuredResources = servletContext.getInitParameter(NON_SECURED_RESOURCES);
        if(null != nonSecuredResources){
            resourcesList = Arrays.asList(nonSecuredResources.split(","));
        }
        Set<ApiUriTemplate> templates = apiConfig.getUriTemplates();
        if(null != resourcesList) {
            for (ApiUriTemplate template : templates) {
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
        apiConfig.setUriTemplates(templates);
    }
}
