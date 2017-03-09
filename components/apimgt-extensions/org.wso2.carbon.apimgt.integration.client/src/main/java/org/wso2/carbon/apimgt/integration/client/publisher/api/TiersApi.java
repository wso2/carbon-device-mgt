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
package org.wso2.carbon.apimgt.integration.client.publisher.api;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.wso2.carbon.apimgt.integration.client.publisher.model.Tier;
import org.wso2.carbon.apimgt.integration.client.publisher.model.TierList;
import org.wso2.carbon.apimgt.integration.client.publisher.model.TierPermission;


import java.util.List;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2017-01-24T00:03:49.624+05:30")
public interface TiersApi  {


  /**
   * List Tiers
   * Get available tiers 
   * @param tierLevel List API or Application or Resource type tiers.  (required)
   * @param limit Maximum size of resource array to return.  (optional, default to 25)
   * @param offset Starting point within the complete list of items qualified.  (optional, default to 0)
   * @param accept Media types acceptable for the response. Default is JSON.  (optional, default to JSON)
   * @param ifNoneMatch Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec.  (optional)
   * @return TierList
   */
  @RequestLine("GET /tiers/{tierLevel}?limit={limit}&offset={offset}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "Accept: {accept}",
    
    "If-None-Match: {ifNoneMatch}"
  })
  TierList tiersTierLevelGet(@Param("tierLevel") String tierLevel, @Param("limit") Integer limit,
                             @Param("offset") Integer offset, @Param("accept") String accept,
                             @Param("ifNoneMatch") String ifNoneMatch);

  /**
   * Add a new Tier
   * Add a new tier 
   * @param body Tier object that should to be added  (required)
   * @param tierLevel List API or Application or Resource type tiers.  (required)
   * @param contentType Media type of the entity in the body. Default is JSON.  (required)
   * @return Tier
   */
  @RequestLine("POST /tiers/{tierLevel}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "Content-Type: {contentType}"
  })
  Tier tiersTierLevelPost(Tier body, @Param("tierLevel") String tierLevel, @Param("contentType") String contentType);

  /**
   * Delete a Tier
   * Remove a tier 
   * @param tierName Tier name  (required)
   * @param tierLevel List API or Application or Resource type tiers.  (required)
   * @param ifMatch Validator for conditional requests; based on ETag.  (optional)
   * @param ifUnmodifiedSince Validator for conditional requests; based on Last Modified header.  (optional)
   * @return void
   */
  @RequestLine("DELETE /tiers/{tierLevel}/{tierName}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "If-Match: {ifMatch}",
    
    "If-Unmodified-Since: {ifUnmodifiedSince}"
  })
  void tiersTierLevelTierNameDelete(@Param("tierName") String tierName, @Param("tierLevel") String tierLevel,
                                    @Param("ifMatch") String ifMatch,
                                    @Param("ifUnmodifiedSince") String ifUnmodifiedSince);

  /**
   * Get a Tier
   * Get tier details 
   * @param tierName Tier name  (required)
   * @param tierLevel List API or Application or Resource type tiers.  (required)
   * @param accept Media types acceptable for the response. Default is JSON.  (optional, default to JSON)
   * @param ifNoneMatch Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec.  (optional)
   * @param ifModifiedSince Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource.  (optional)
   * @return Tier
   */
  @RequestLine("GET /tiers/{tierLevel}/{tierName}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "Accept: {accept}",
    
    "If-None-Match: {ifNoneMatch}",
    
    "If-Modified-Since: {ifModifiedSince}"
  })
  Tier tiersTierLevelTierNameGet(@Param("tierName") String tierName, @Param("tierLevel") String tierLevel,
                                 @Param("accept") String accept, @Param("ifNoneMatch") String ifNoneMatch,
                                 @Param("ifModifiedSince") String ifModifiedSince);

  /**
   * Update a Tier
   * Update tier details 
   * @param tierName Tier name  (required)
   * @param body Tier object that needs to be modified  (required)
   * @param tierLevel List API or Application or Resource type tiers.  (required)
   * @param contentType Media type of the entity in the body. Default is JSON.  (required)
   * @param ifMatch Validator for conditional requests; based on ETag.  (optional)
   * @param ifUnmodifiedSince Validator for conditional requests; based on Last Modified header.  (optional)
   * @return Tier
   */
  @RequestLine("PUT /tiers/{tierLevel}/{tierName}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "Content-Type: {contentType}",
    
    "If-Match: {ifMatch}",
    
    "If-Unmodified-Since: {ifUnmodifiedSince}"
  })
  Tier tiersTierLevelTierNamePut(@Param("tierName") String tierName, Tier body, @Param("tierLevel") String tierLevel,
                                 @Param("contentType") String contentType, @Param("ifMatch") String ifMatch,
                                 @Param("ifUnmodifiedSince") String ifUnmodifiedSince);

  /**
   * Update Tier Permission
   * Update tier permission 
   * @param tierName Name of the tier  (required)
   * @param tierLevel List API or Application or Resource type tiers.  (required)
   * @param ifMatch Validator for conditional requests; based on ETag.  (optional)
   * @param ifUnmodifiedSince Validator for conditional requests; based on Last Modified header.  (optional)
   * @param permissions  (optional)
   * @return List<Tier>
   */
  @RequestLine("POST /tiers/update-permission?tierName={tierName}&tierLevel={tierLevel}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "If-Match: {ifMatch}",
    
    "If-Unmodified-Since: {ifUnmodifiedSince}"
  })
  List<Tier> tiersUpdatePermissionPost(@Param("tierName") String tierName, @Param("tierLevel") String tierLevel,
                                       @Param("ifMatch") String ifMatch,
                                       @Param("ifUnmodifiedSince") String ifUnmodifiedSince, TierPermission permissions);
}
