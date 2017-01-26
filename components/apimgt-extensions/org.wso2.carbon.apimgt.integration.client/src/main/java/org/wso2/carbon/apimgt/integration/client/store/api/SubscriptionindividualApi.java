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
import org.wso2.carbon.apimgt.integration.client.store.model.Subscription;


@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2017-01-24T00:03:54.991+05:30")
public interface SubscriptionindividualApi  {


  /**
   * Add a new subscription 
   * Add a new subscription 
   * @param body Subscription object that should to be added  (required)
   * @param contentType Media type of the entity in the body. Default is JSON.  (required)
   * @return Subscription
   */
  @RequestLine("POST /subscriptions")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "Content-Type: {contentType}"
  })
  Subscription subscriptionsPost(Subscription body, @Param("contentType") String contentType);

  /**
   * Remove subscription 
   * Remove subscription 
   * @param subscriptionId Subscription Id  (required)
   * @param ifMatch Validator for conditional requests; based on ETag.  (optional)
   * @param ifUnmodifiedSince Validator for conditional requests; based on Last Modified header.  (optional)
   * @return void
   */
  @RequestLine("DELETE /subscriptions/{subscriptionId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "If-Match: {ifMatch}",
    
    "If-Unmodified-Since: {ifUnmodifiedSince}"
  })
  void subscriptionsSubscriptionIdDelete(@Param("subscriptionId") String subscriptionId,
                                         @Param("ifMatch") String ifMatch,
                                         @Param("ifUnmodifiedSince") String ifUnmodifiedSince);

  /**
   * Get subscription details 
   * Get subscription details 
   * @param subscriptionId Subscription Id  (required)
   * @param accept Media types acceptable for the response. Default is JSON.  (optional, default to JSON)
   * @param ifNoneMatch Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec.  (optional)
   * @param ifModifiedSince Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource.  (optional)
   * @return Subscription
   */
  @RequestLine("GET /subscriptions/{subscriptionId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "Accept: {accept}",
    
    "If-None-Match: {ifNoneMatch}",
    
    "If-Modified-Since: {ifModifiedSince}"
  })
  Subscription subscriptionsSubscriptionIdGet(@Param("subscriptionId") String subscriptionId,
                                              @Param("accept") String accept, @Param("ifNoneMatch") String ifNoneMatch,
                                              @Param("ifModifiedSince") String ifModifiedSince);
}
