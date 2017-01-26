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
import org.wso2.carbon.apimgt.integration.client.store.model.Application;
import org.wso2.carbon.apimgt.integration.client.store.model.ApplicationKey;
import org.wso2.carbon.apimgt.integration.client.store.model.ApplicationKeyGenerateRequest;


@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2017-01-24T00:03:54.991+05:30")
public interface ApplicationindividualApi  {


  /**
   * Remove an application 
   * Remove an application 
   * @param applicationId **Application Identifier** consisting of the UUID of the Application.  (required)
   * @param ifMatch Validator for conditional requests; based on ETag.  (optional)
   * @param ifUnmodifiedSince Validator for conditional requests; based on Last Modified header.  (optional)
   * @return void
   */
  @RequestLine("DELETE /applications/{applicationId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "If-Match: {ifMatch}",
    
    "If-Unmodified-Since: {ifUnmodifiedSince}"
  })
  void applicationsApplicationIdDelete(@Param("applicationId") String applicationId, @Param("ifMatch") String ifMatch,
                                       @Param("ifUnmodifiedSince") String ifUnmodifiedSince);

  /**
   * Get application details 
   * Get application details 
   * @param applicationId **Application Identifier** consisting of the UUID of the Application.  (required)
   * @param accept Media types acceptable for the response. Default is JSON.  (optional, default to JSON)
   * @param ifNoneMatch Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec.  (optional)
   * @param ifModifiedSince Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource.  (optional)
   * @return Application
   */
  @RequestLine("GET /applications/{applicationId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "Accept: {accept}",
    
    "If-None-Match: {ifNoneMatch}",
    
    "If-Modified-Since: {ifModifiedSince}"
  })
  Application applicationsApplicationIdGet(@Param("applicationId") String applicationId, @Param("accept") String accept,
                                           @Param("ifNoneMatch") String ifNoneMatch,
                                           @Param("ifModifiedSince") String ifModifiedSince);

  /**
   * Update application details 
   * Update application details 
   * @param applicationId **Application Identifier** consisting of the UUID of the Application.  (required)
   * @param body Application object that needs to be updated  (required)
   * @param contentType Media type of the entity in the body. Default is JSON.  (required)
   * @param ifMatch Validator for conditional requests; based on ETag.  (optional)
   * @param ifUnmodifiedSince Validator for conditional requests; based on Last Modified header.  (optional)
   * @return Application
   */
  @RequestLine("PUT /applications/{applicationId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "Content-Type: {contentType}",
    
    "If-Match: {ifMatch}",
    
    "If-Unmodified-Since: {ifUnmodifiedSince}"
  })
  Application applicationsApplicationIdPut(@Param("applicationId") String applicationId, Application body,
                                           @Param("contentType") String contentType, @Param("ifMatch") String ifMatch,
                                           @Param("ifUnmodifiedSince") String ifUnmodifiedSince);

  /**
   * Generate keys for application 
   * Generate keys for application 
   * @param applicationId **Application Identifier** consisting of the UUID of the Application.  (required)
   * @param body Application object the keys of which are to be generated  (required)
   * @param contentType Media type of the entity in the body. Default is JSON.  (required)
   * @param ifMatch Validator for conditional requests; based on ETag.  (optional)
   * @param ifUnmodifiedSince Validator for conditional requests; based on Last Modified header.  (optional)
   * @return ApplicationKey
   */
  @RequestLine("POST /applications/generate-keys?applicationId={applicationId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "Content-Type: {contentType}",
    
    "If-Match: {ifMatch}",
    
    "If-Unmodified-Since: {ifUnmodifiedSince}"
  })
  ApplicationKey applicationsGenerateKeysPost(@Param("applicationId") String applicationId,
                                              ApplicationKeyGenerateRequest body,
                                              @Param("contentType") String contentType,
                                              @Param("ifMatch") String ifMatch,
                                              @Param("ifUnmodifiedSince") String ifUnmodifiedSince);

  /**
   * Create a new application. 
   * Create a new application. 
   * @param body Application object that is to be created.  (required)
   * @param contentType Media type of the entity in the body. Default is JSON.  (required)
   * @return Application
   */
  @RequestLine("POST /applications")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "Content-Type: {contentType}"
  })
  Application applicationsPost(Application body, @Param("contentType") String contentType);
}
