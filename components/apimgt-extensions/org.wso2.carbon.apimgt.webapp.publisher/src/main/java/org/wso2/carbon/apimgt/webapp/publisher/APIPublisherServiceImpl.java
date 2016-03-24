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

import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.webapp.publisher.internal.APIPublisherDataHolder;
import org.wso2.carbon.governance.lcm.util.CommonUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * This class represents the concrete implementation of the APIPublisherService that corresponds to providing all
 * API publishing related operations.
 */
public class APIPublisherServiceImpl implements APIPublisherService {

    private static final Log log = LogFactory.getLog(APIPublisherServiceImpl.class);

    @Override
    public void publishAPI(API api) throws APIManagementException, FaultGatewaysException {
        if (log.isDebugEnabled()) {
            log.debug("Publishing API '" + api.getId() + "'");
        }
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(api.getApiOwner());
            int tenantId =
                    APIPublisherDataHolder.getInstance().getRealmService().getTenantManager().getTenantId(tenantDomain);
            // Below code snippet is added load API Lifecycle in tenant mode, where in it does not load when tenant is loaded.
            RegistryService registryService = APIPublisherDataHolder.getInstance().getRegistryService();
            CommonUtil.addDefaultLifecyclesIfNotAvailable(registryService.getConfigSystemRegistry(tenantId),
                                                          CommonUtil.getRootSystemRegistry(tenantId));
            APIProvider provider = APIManagerFactory.getInstance().getAPIProvider(api.getApiOwner());
            MultitenantUtils.getTenantDomain(api.getApiOwner());
            if (provider != null) {
                if (!provider.isAPIAvailable(api.getId())) {
                    provider.addAPI(api);
                    log.info("Successfully published API '" + api.getId().getApiName() + "' with context '" +
                                     api.getContext() + "' and version '" + api.getId().getVersion() + "'");
                } else {
                    provider.updateAPI(api);
                    log.info("An API already exists with the name '" + api.getId().getApiName() + "', context '" +
                                     api.getContext() + "' and version '" + api.getId().getVersion() +
                                     "'. Thus, the API config is updated");
                }
                provider.saveSwagger20Definition(api.getId(), createSwaggerDefinition(api));
            } else {
                throw new APIManagementException("API provider configured for the given API configuration is null. " +
                                                         "Thus, the API is not published");
            }
        } catch (UserStoreException e) {
            throw new APIManagementException("Failed to get the tenant id for the user " + api.getApiOwner(), e);
        } catch (FileNotFoundException e) {
            throw new APIManagementException("Failed to retrieve life cycle file ", e);
        } catch (RegistryException e) {
            throw new APIManagementException("Failed to access the registry ", e);
        } catch (XMLStreamException e) {
            throw new APIManagementException("Failed parsing the lifecycle xml.", e);
        }
    }

    private String createSwaggerDefinition(API api) {
        //{"paths":{"/controller/*":{"get":{"responses":{"200":{}}}},"/manager/*":{"get":{"responses":{"200":{}}}}},
        // "swagger":"2.0","info":{"title":"RaspberryPi","version":"1.0.0"}}
        JsonObject swaggerDefinition = new JsonObject();

        JsonObject paths = new JsonObject();
        for (URITemplate uriTemplate : api.getUriTemplates()) {
            JsonObject response = new JsonObject();
            response.addProperty("200", "");

            JsonObject responses = new JsonObject();
            responses.add("responses", response);

            JsonObject httpVerb = new JsonObject();
            httpVerb.add(uriTemplate.getHTTPVerb().toLowerCase(), responses);

            JsonObject path = new JsonObject();
            path.add(uriTemplate.getUriTemplate(), httpVerb);

            paths.add(uriTemplate.getUriTemplate(), httpVerb);
        }
        swaggerDefinition.add("paths", paths);
        swaggerDefinition.addProperty("swagger", "2.0");

        JsonObject info = new JsonObject();
        info.addProperty("title", api.getId().getApiName());
        info.addProperty("version", api.getId().getVersion());
        swaggerDefinition.add("info", info);

        return swaggerDefinition.toString();
        //return "{\"paths\":{\"/controller/*\":{\"get\":{\"responses\":{\"200\":{}}}},
        // \"/manager/*\":{\"get\":{\"responses\":{\"200\":{}}}}},\"swagger\":\"2.0\",
        // \"info\":{\"title\":\"RaspberryPi\",\"version\":\"1.0.0\"}}";
    }

    @Override
    public void removeAPI(APIIdentifier id) throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Removing API '" + id.getApiName() + "'");
        }
        APIProvider provider = APIManagerFactory.getInstance().getAPIProvider(id.getProviderName());
        provider.deleteAPI(id);
        if (log.isDebugEnabled()) {
            log.debug("API '" + id.getApiName() + "' has been successfully removed");
        }
    }

    @Override
    public void publishAPIs(List<API> apis) throws APIManagementException, FaultGatewaysException {
        if (log.isDebugEnabled()) {
            log.debug("Publishing a batch of APIs");
        }
        for (API api : apis) {
            try {
                this.publishAPI(api);
            } catch (APIManagementException e) {
                log.error("Error occurred while publishing API '" + api.getId().getApiName() + "'", e);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("End of publishing the batch of APIs");
        }
    }
}
