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
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.webapp.publisher.internal.APIPublisherDataHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.lcm.util.CommonUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class represents the concrete implementation of the APIPublisherService that corresponds to providing all
 * API publishing related operations.
 */
public class APIPublisherServiceImpl implements APIPublisherService {

    private static final Log log = LogFactory.getLog(APIPublisherServiceImpl.class);
	private static final String PUBLISH_ACTION = "Publish";

	@Override
	public void publishAPI(final API api) throws APIManagementException, FaultGatewaysException {
		String tenantDomain = MultitenantUtils.getTenantDomain(api.getApiOwner());
		PrivilegedCarbonContext.startTenantFlow();
		PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
		try {
			int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
			// Below code snippet is added to load API Lifecycle in tenant mode.
			RegistryService registryService = APIPublisherDataHolder.getInstance().getRegistryService();
			CommonUtil.addDefaultLifecyclesIfNotAvailable(registryService.getConfigSystemRegistry(tenantId),
														  CommonUtil.getRootSystemRegistry(tenantId));
			APIProvider provider = APIManagerFactory.getInstance().getAPIProvider(api.getApiOwner());
			MultitenantUtils.getTenantDomain(api.getApiOwner());
			if (provider != null) {
				if (provider.isDuplicateContextTemplate(api.getContext())) {
					throw new APIManagementException(
							"Error occurred while adding the API. A duplicate API" +
									" context already exists for " + api.getContext());
				}
				if (!provider.isAPIAvailable(api.getId())) {
					provider.addAPI(api);
					provider.changeLifeCycleStatus(api.getId(), PUBLISH_ACTION);
					if (log.isDebugEnabled()) {
						log.debug("Successfully published API '" + api.getId().getApiName() +
										  "' with context '" + api.getContext() + "' and version '"
										  + api.getId().getVersion() + "'");
					}
				} else {
					api.setStatus(APIStatus.PUBLISHED);
					provider.updateAPI(api);
					if (log.isDebugEnabled()) {
						log.debug("An API already exists with the name '" + api.getId().getApiName() +
										  "', context '" + api.getContext() + "' and version '"
										  + api.getId().getVersion() + "'. Thus, the API config is updated");
					}
				}
				provider.saveSwagger20Definition(api.getId(), createSwaggerDefinition(api));
			} else {
				throw new APIManagementException("API provider configured for the given API configuration " +
														 "is null. Thus, the API is not published");
			}
		} catch (FileNotFoundException e) {
			throw new APIManagementException("Failed to retrieve life cycle file ", e);
		} catch (RegistryException e) {
			throw new APIManagementException("Failed to access the registry ", e);
		} catch (XMLStreamException e) {
			throw new APIManagementException("Failed parsing the lifecycle xml.", e);
		} finally {
			PrivilegedCarbonContext.endTenantFlow();
		}
	}

	private String createSwaggerDefinition(API api) {
		Map<String, JsonObject> httpVerbsMap = new HashMap<>();

		for (URITemplate uriTemplate : api.getUriTemplates()) {
			JsonObject response = new JsonObject();
			response.addProperty("200", "");

			JsonObject responses = new JsonObject();
			responses.add("responses", response);
			JsonObject httpVerbs = httpVerbsMap.get(uriTemplate.getUriTemplate());
			if (httpVerbs == null) {
				httpVerbs = new JsonObject();
			}
			httpVerbs.add(uriTemplate.getHTTPVerb().toLowerCase(), responses);
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
		info.addProperty("title", api.getId().getApiName());
		info.addProperty("version", api.getId().getVersion());

		JsonObject swaggerDefinition = new JsonObject();
		swaggerDefinition.add("paths", paths);
		swaggerDefinition.addProperty("swagger", "2.0");
		swaggerDefinition.add("info", info);

		return swaggerDefinition.toString();
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
