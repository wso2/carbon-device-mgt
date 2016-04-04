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
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import java.util.List;

/**
 * This interface represents all methods related to API manipulation that's done as part of API-Management tasks.
 *
 * Note: Ideally, this has to come from the API-Management  components. However, due to lack of clean APIs
 * (as OSGi declarative services, etc) provided for API publishing and related tasks, this was introduced at the device
 * management core implementation layer.
 */
public interface APIPublisherService {

    /**
     * This method registers an API within the underlying API-Management infrastructure.
     *
     * @param api An instance of the bean that passes metadata related to the API being published
     * @throws APIManagementException Is thrown if some unexpected event occurs while publishing the API
     */
    void publishAPI(API api) throws APIManagementException, FaultGatewaysException;

    /**
     * This method removes an API that's already published within the underlying API-Management infrastructure.
     *
     * @param id An instance of the bean that carries API identification related metadata
     * @throws APIManagementException Is thrown if some unexpected event occurs while removing the API
     */
    void removeAPI(APIIdentifier id) throws APIManagementException;

    /**
     * This method registers a collection of APIs within the underlying API-Management infrastructure.
     *
     * @param apis A list of the beans that passes metadata related to the APIs being published
     * @throws APIManagementException Is thrown if some unexpected event occurs while publishing the APIs
     */
    void publishAPIs(List<API> apis) throws APIManagementException, FaultGatewaysException;
}
