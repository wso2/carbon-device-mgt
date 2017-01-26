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


import org.wso2.carbon.apimgt.webapp.publisher.exception.APIManagerPublisherException;

/**
 * This interface represents all methods related to API manipulation that's done as part of API-Management tasks.
 *
 */
public interface APIPublisherService {

    /**
     * This method registers an API within the underlying API-Management infrastructure.
     *
     * @param api An instance of the bean that passes metadata related to the API being published
     * @throws APIManagerPublisherException Is thrown if some unexpected event occurs while publishing the API
     */
    void publishAPI(APIConfig api) throws APIManagerPublisherException;

}
