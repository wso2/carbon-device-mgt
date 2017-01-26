/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.integration.client.store.api;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.wso2.carbon.apimgt.integration.client.store.model.SubscriptionList;


@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2017-01-24T00:03:54.991+05:30")
public interface SubscriptionCollectionApi  {


  /**
   * Get subscription list. 
   * Get subscription list. The API Identifier or Application Identifier the subscriptions of which are to be returned are passed as parameters. 
   * @param apiId **API ID** consisting of the **UUID** of the API. The combination of the provider of the API, name of the API and the version is also accepted as a valid API I. Should be formatted as **provider-name-version**.  (required)
   * @param applicationId **Application Identifier** consisting of the UUID of the Application.  (required)
   * @param groupId Application Group Id  (optional)
   * @param offset Starting point within the complete list of items qualified.  (optional, default to 0)
   * @param limit Maximum size of resource array to return.  (optional, default to 25)
   * @param accept Media types acceptable for the response. Default is JSON.  (optional, default to JSON)
   * @param ifNoneMatch Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec.  (optional)
   * @return SubscriptionList
   */
  @RequestLine("GET /subscriptions?apiId={apiId}&applicationId={applicationId}&groupId={groupId}&offset={offset}&limit={limit}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "Accept: {accept}",
    
    "If-None-Match: {ifNoneMatch}"
  })
  SubscriptionList subscriptionsGet(@Param("apiId") String apiId, @Param("applicationId") String applicationId,
                                    @Param("groupId") String groupId, @Param("offset") Integer offset,
                                    @Param("limit") Integer limit, @Param("accept") String accept,
                                    @Param("ifNoneMatch") String ifNoneMatch);
}
