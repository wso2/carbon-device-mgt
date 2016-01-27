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
package org.wso2.carbon.apimgt.webapp.publisher;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.webapp.publisher.internal.APIPublisherDataHolder;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.NetworkUtils;

import java.util.*;

public class APIPublisherUtil {

    private static final String DEFAULT_API_VERSION = "1.0.0";

    enum HTTPMethod {
        GET, POST, DELETE, PUT, OPTIONS
    }

    private static List<HTTPMethod> httpMethods;

    static {
        httpMethods = new ArrayList<HTTPMethod>(5);
        httpMethods.add(HTTPMethod.GET);
        httpMethods.add(HTTPMethod.POST);
        httpMethods.add(HTTPMethod.DELETE);
        httpMethods.add(HTTPMethod.PUT);
        httpMethods.add(HTTPMethod.OPTIONS);
    }

    public static API getAPI(APIConfig config) throws APIManagementException {
        APIProvider provider = config.getProvider();
        APIIdentifier id = new APIIdentifier(config.getOwner(), config.getName(), config.getVersion());

        API api = new API(id);
        api.setApiOwner(config.getOwner());
        api.setContext(config.getContext());
        api.setUrl(config.getEndpoint());
        api.setVisibility(APIConstants.API_GLOBAL_VISIBILITY);
        api.addAvailableTiers(provider.getTiers());
        api.setEndpointSecured(true);
        api.setStatus(APIStatus.PUBLISHED);
        api.setTransports(config.getTransports());
        api.setContextTemplate(config.getContextTemplate());
        api.setUriTemplates(config.getUriTemplates());

        Set<Tier> tiers = new HashSet<Tier>();
        tiers.add(new Tier(APIConstants.UNLIMITED_TIER));
        api.addAvailableTiers(tiers);
        api.setSubscriptionAvailability(APIConstants.SUBSCRIPTION_TO_ALL_TENANTS);
        api.setResponseCache(APIConstants.DISABLED);

        String endpointConfig = "{\"production_endpoints\":{\"url\":\" " + config.getEndpoint() + "\",\"config\":null},\"endpoint_type\":\"http\"}";
        api.setEndpointConfig(endpointConfig);

        if ("".equals(id.getVersion()) || (DEFAULT_API_VERSION.equals(id.getVersion()))) {
            api.setAsDefaultVersion(Boolean.TRUE);
            api.setAsPublishedDefaultVersion(Boolean.TRUE);
        }

        return api;
    }

    private static Set<URITemplate> getURITemplates(String endpoint, String authType) {
        Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();
        if (APIConstants.AUTH_NO_AUTHENTICATION.equals(authType)) {
            for (HTTPMethod method : httpMethods) {
                URITemplate template = new URITemplate();
                template.setAuthType(APIConstants.AUTH_NO_AUTHENTICATION);
                template.setHTTPVerb(method.toString());
                template.setResourceURI(endpoint);
                template.setUriTemplate("/*");
                uriTemplates.add(template);
            }
        } else {
            for (HTTPMethod method : httpMethods) {
                URITemplate template = new URITemplate();
                if (HTTPMethod.OPTIONS.equals(method)) {
                    template.setAuthType(APIConstants.AUTH_NO_AUTHENTICATION);
                } else {
                    template.setAuthType(APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN);
                }
                template.setHTTPVerb(method.toString());
                template.setResourceURI(endpoint);
                template.setUriTemplate("/*");
                uriTemplates.add(template);
            }
        }
        return uriTemplates;
    }

    public static String getServerBaseUrl() {
        // Hostname
        String hostName = "localhost";
        try {
            hostName = NetworkUtils.getMgtHostName();
        } catch (Exception ignored) {
        }
        // HTTPS port
        String mgtConsoleTransport = CarbonUtils.getManagementTransport();
        ConfigurationContextService configContextService =
                APIPublisherDataHolder.getInstance().getConfigurationContextService();
        int port = CarbonUtils.getTransportPort(configContextService, mgtConsoleTransport);
        int httpsProxyPort =
                CarbonUtils.getTransportProxyPort(configContextService.getServerConfigContext(),
                        mgtConsoleTransport);
        if (httpsProxyPort > 0) {
            port = httpsProxyPort;
        }
        return "https://" + hostName + ":" + port;
    }

    public static String getApiEndpointUrl(String context) {
        return getServerBaseUrl() + context;
    }

}
