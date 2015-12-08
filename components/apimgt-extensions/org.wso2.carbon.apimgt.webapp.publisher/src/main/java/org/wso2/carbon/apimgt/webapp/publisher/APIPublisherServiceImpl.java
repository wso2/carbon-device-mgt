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
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;

import java.util.Date;
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
        APIProvider provider = APIManagerFactory.getInstance().getAPIProvider(api.getApiOwner());
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
    public int createApplication(Application application, String apiOwner)
            throws APIManagementException, FaultGatewaysException {
        if (log.isDebugEnabled()) {
            log.debug("Publishing API '" + application.getId() + "'");
        }
        APIConsumer consumer = APIManagerFactory.getInstance().getAPIConsumer(apiOwner);
        if (consumer != null) {
            log.info("Successfully created application wit id : " + application.getId());
            return consumer.addApplication(application, apiOwner);
        } else {
            throw new APIManagementException("API provider configured for the given API configuration is null. " +
                                                     "Thus, the API is not published");
        }
    }

    @Override
    public void addSubscription(APIIdentifier apiId, int applicationId, String userName)
            throws APIManagementException, FaultGatewaysException {
        if (log.isDebugEnabled()) {
            log.debug("Creating subscription for API " + apiId);
        }
        APIConsumer consumer = APIManagerFactory.getInstance().getAPIConsumer(userName);
        if (consumer != null) {
            consumer.addSubscription(apiId, userName, applicationId);
            log.info("Successfully created subscription for API : " + apiId + " from application : " + applicationId);
        } else {
            throw new APIManagementException("API provider configured for the given API configuration is null. " +
                                                     "Thus, the API is not published");
        }
    }

    @Override
    public void adddSubscriber(String subscriberName, String groupId)
            throws APIManagementException, FaultGatewaysException {
        if (log.isDebugEnabled()) {
            log.debug("Creating subscriber with name  " + subscriberName);
        }
        APIConsumer consumer = APIManagerFactory.getInstance().getAPIConsumer(subscriberName);
        if (consumer != null) {
            Subscriber subscriber = new Subscriber((String) subscriberName);
            subscriber.setSubscribedDate(new Date());
            //TODO : need to set the proper email
            subscriber.setEmail("");
            subscriber.setTenantId(-1234);
            consumer.addSubscriber(subscriber, groupId);
            log.info("Successfully created subscriber with name : " + subscriberName + " with groupID : " + groupId);
        } else {
            throw new APIManagementException("API provider configured for the given API configuration is null. " +
                                                     "Thus, the API is not published");
        }
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
