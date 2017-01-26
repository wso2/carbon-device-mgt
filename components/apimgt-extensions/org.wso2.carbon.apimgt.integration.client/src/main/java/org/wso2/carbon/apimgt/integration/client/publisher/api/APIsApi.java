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
import org.wso2.carbon.apimgt.integration.client.publisher.model.API;
import org.wso2.carbon.apimgt.integration.client.publisher.model.APIList;
import org.wso2.carbon.apimgt.integration.client.publisher.model.FileInfo;


import java.io.File;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2017-01-24T00:03:49.624+05:30")
public interface APIsApi  {


  /**
   * Delete API
   * Delete an existing API 
   * @param apiId **API ID** consisting of the **UUID** of the API. The combination of the provider of the API, name of the API and the version is also accepted as a valid API ID. Should be formatted as **provider-name-version**.  (required)
   * @param ifMatch Validator for conditional requests; based on ETag.  (optional)
   * @param ifUnmodifiedSince Validator for conditional requests; based on Last Modified header.  (optional)
   * @return void
   */
  @RequestLine("DELETE /apis/{apiId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "If-Match: {ifMatch}",
    
    "If-Unmodified-Since: {ifUnmodifiedSince}"
  })
  void apisApiIdDelete(@Param("apiId") String apiId, @Param("ifMatch") String ifMatch,
                       @Param("ifUnmodifiedSince") String ifUnmodifiedSince);

  /**
   * Get API details
   * Get details of an API 
   * @param apiId **API ID** consisting of the **UUID** of the API. The combination of the provider of the API, name of the API and the version is also accepted as a valid API ID. Should be formatted as **provider-name-version**.  (required)
   * @param accept Media types acceptable for the response. Default is JSON.  (optional, default to JSON)
   * @param ifNoneMatch Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec.  (optional)
   * @param ifModifiedSince Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource.  (optional)
   * @return API
   */
  @RequestLine("GET /apis/{apiId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "Accept: {accept}",
    
    "If-None-Match: {ifNoneMatch}",
    
    "If-Modified-Since: {ifModifiedSince}"
  })
  API apisApiIdGet(@Param("apiId") String apiId, @Param("accept") String accept,
                   @Param("ifNoneMatch") String ifNoneMatch, @Param("ifModifiedSince") String ifModifiedSince);

  /**
   * Update an existing API
   * Update an existing API 
   * @param apiId **API ID** consisting of the **UUID** of the API. The combination of the provider of the API, name of the API and the version is also accepted as a valid API ID. Should be formatted as **provider-name-version**.  (required)
   * @param body API object that needs to be added  (required)
   * @param contentType Media type of the entity in the body. Default is JSON.  (required)
   * @param ifMatch Validator for conditional requests; based on ETag.  (optional)
   * @param ifUnmodifiedSince Validator for conditional requests; based on Last Modified header.  (optional)
   * @return API
   */
  @RequestLine("PUT /apis/{apiId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "Content-Type: {contentType}",
    
    "If-Match: {ifMatch}",
    
    "If-Unmodified-Since: {ifUnmodifiedSince}"
  })
  API apisApiIdPut(@Param("apiId") String apiId, API body, @Param("contentType") String contentType,
                   @Param("ifMatch") String ifMatch, @Param("ifUnmodifiedSince") String ifUnmodifiedSince);

  /**
   * Get API Definition
   * Get the swagger of an API 
   * @param apiId **API ID** consisting of the **UUID** of the API. The combination of the provider of the API, name of the API and the version is also accepted as a valid API ID. Should be formatted as **provider-name-version**.  (required)
   * @param accept Media types acceptable for the response. Default is JSON.  (optional, default to JSON)
   * @param ifNoneMatch Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec.  (optional)
   * @param ifModifiedSince Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource.  (optional)
   * @return void
   */
  @RequestLine("GET /apis/{apiId}/swagger")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "Accept: {accept}",
    
    "If-None-Match: {ifNoneMatch}",
    
    "If-Modified-Since: {ifModifiedSince}"
  })
  void apisApiIdSwaggerGet(@Param("apiId") String apiId, @Param("accept") String accept,
                           @Param("ifNoneMatch") String ifNoneMatch, @Param("ifModifiedSince") String ifModifiedSince);

  /**
   * Update API Definition
   * Update an existing swagger definition of an API 
   * @param apiId **API ID** consisting of the **UUID** of the API. The combination of the provider of the API, name of the API and the version is also accepted as a valid API ID. Should be formatted as **provider-name-version**.  (required)
   * @param apiDefinition Swagger definition of the API (required)
   * @param contentType Media type of the entity in the body. Default is JSON.  (required)
   * @param ifMatch Validator for conditional requests; based on ETag.  (optional)
   * @param ifUnmodifiedSince Validator for conditional requests; based on Last Modified header.  (optional)
   * @return void
   */
  @RequestLine("PUT /apis/{apiId}/swagger")
  @Headers({
    "Content-type: multipart/form-data",
    "Accept: application/json",
    "Content-Type: {contentType}",
    
    "If-Match: {ifMatch}",
    
    "If-Unmodified-Since: {ifUnmodifiedSince}"
  })
  void apisApiIdSwaggerPut(@Param("apiId") String apiId, @Param("apiDefinition") String apiDefinition,
                           @Param("contentType") String contentType, @Param("ifMatch") String ifMatch,
                           @Param("ifUnmodifiedSince") String ifUnmodifiedSince);

  /**
   * Get the thumbnail image
   * Downloads a thumbnail image of an API 
   * @param apiId **API ID** consisting of the **UUID** of the API. The combination of the provider of the API, name of the API and the version is also accepted as a valid API ID. Should be formatted as **provider-name-version**.  (required)
   * @param accept Media types acceptable for the response. Default is JSON.  (optional, default to JSON)
   * @param ifNoneMatch Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec.  (optional)
   * @param ifModifiedSince Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource.  (optional)
   * @return void
   */
  @RequestLine("GET /apis/{apiId}/thumbnail")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "Accept: {accept}",
    
    "If-None-Match: {ifNoneMatch}",
    
    "If-Modified-Since: {ifModifiedSince}"
  })
  void apisApiIdThumbnailGet(@Param("apiId") String apiId, @Param("accept") String accept,
                             @Param("ifNoneMatch") String ifNoneMatch, @Param("ifModifiedSince") String ifModifiedSince);

  /**
   * Upload a thumbnail image
   * Upload a thumbnail image to an API. 
   * @param apiId **API ID** consisting of the **UUID** of the API. The combination of the provider of the API, name of the API and the version is also accepted as a valid API ID. Should be formatted as **provider-name-version**.  (required)
   * @param file Image to upload (required)
   * @param contentType Media type of the entity in the body. Default is JSON.  (required)
   * @param ifMatch Validator for conditional requests; based on ETag.  (optional)
   * @param ifUnmodifiedSince Validator for conditional requests; based on Last Modified header.  (optional)
   * @return FileInfo
   */
  @RequestLine("POST /apis/{apiId}/thumbnail")
  @Headers({
    "Content-type: multipart/form-data",
    "Accept: application/json",
    "Content-Type: {contentType}",
    
    "If-Match: {ifMatch}",
    
    "If-Unmodified-Since: {ifUnmodifiedSince}"
  })
  FileInfo apisApiIdThumbnailPost(@Param("apiId") String apiId, @Param("file") File file,
                                  @Param("contentType") String contentType, @Param("ifMatch") String ifMatch,
                                  @Param("ifUnmodifiedSince") String ifUnmodifiedSince);

  /**
   * Change API Status
   * Change the lifecycle of an API 
   * @param action The action to demote or promote the state of the API.  Supported actions are [ **Publish, Deploy as a Prototype, Demote to Created, Demote to Prototyped, Block, Deprecate, Re-Publish, Retire **]  (required)
   * @param apiId **API ID** consisting of the **UUID** of the API. The combination of the provider of the API, name of the API and the version is also accepted as a valid API I. Should be formatted as **provider-name-version**.  (required)
   * @param lifecycleChecklist  You can specify additional checklist items by using an **\&quot;attribute:\&quot;** modifier.  Eg: \&quot;Deprecate Old Versions:true\&quot; will deprecate older versions of a particular API when it is promoted to Published state from Created state. Multiple checklist items can be given in \&quot;attribute1:true, attribute2:false\&quot; format.  Supported checklist items are as follows. 1. **Deprecate Old Versions**: Setting this to true will deprecate older versions of a particular API when it is promoted to Published state from Created state. 2. **Require Re-Subscription**: If you set this to true, users need to re subscribe to the API although they may have subscribed to an older version.  (optional)
   * @param ifMatch Validator for conditional requests; based on ETag.  (optional)
   * @param ifUnmodifiedSince Validator for conditional requests; based on Last Modified header.  (optional)
   * @return void
   */
  @RequestLine("POST /apis/change-lifecycle?action={action}&lifecycleChecklist={lifecycleChecklist}&apiId={apiId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "If-Match: {ifMatch}",
    
    "If-Unmodified-Since: {ifUnmodifiedSince}"
  })
  void apisChangeLifecyclePost(@Param("action") String action, @Param("apiId") String apiId,
                               @Param("lifecycleChecklist") String lifecycleChecklist, @Param("ifMatch") String ifMatch,
                               @Param("ifUnmodifiedSince") String ifUnmodifiedSince);

  /**
   * Copy API
   * Create a new API by copying an existing API 
   * @param newVersion Version of the new API. (required)
   * @param apiId **API ID** consisting of the **UUID** of the API. The combination of the provider of the API, name of the API and the version is also accepted as a valid API I. Should be formatted as **provider-name-version**.  (required)
   * @return void
   */
  @RequestLine("POST /apis/copy-api?newVersion={newVersion}&apiId={apiId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json"
  })
  void apisCopyApiPost(@Param("newVersion") String newVersion, @Param("apiId") String apiId);

  /**
   * Get all APIs 
   * Get a list of available APIs qualifying under a given search condition. 
   * @param limit Maximum size of resource array to return.  (optional, default to 25)
   * @param offset Starting point within the complete list of items qualified.  (optional, default to 0)
   * @param query **Search condition**.  You can search in attributes by using an **\&quot;attribute:\&quot;** modifier.  Eg. \&quot;provider:wso2\&quot; will match an API if the provider of the API contains \&quot;wso2\&quot;.  Supported attribute modifiers are [**version, context, status, description, subcontext, doc, provider**]  If no advanced attribute modifier has been specified, search will match the given query string against API Name.  (optional)
   * @param accept Media types acceptable for the response. Default is JSON.  (optional, default to JSON)
   * @param ifNoneMatch Validator for conditional requests; based on the ETag of the formerly retrieved variant of
   *                    the resourec.  (optional)
   * @return APIList
   */
  @RequestLine("GET /apis?limit={limit}&offset={offset}&query={query}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "Accept: {accept}",
    
    "If-None-Match: {ifNoneMatch}"
  })
  APIList apisGet(@Param("limit") Integer limit, @Param("offset") Integer offset, @Param("query") String query,
                  @Param("accept") String accept, @Param("ifNoneMatch") String ifNoneMatch);

  /**
   * Create a new API
   * Create a new API 
   * @param body API object that needs to be added  (required)
   * @param contentType Media type of the entity in the body. Default is JSON.  (required)
   * @return API
   */
  @RequestLine("POST /apis")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "Content-Type: {contentType}"
  })
  API apisPost(API body, @Param("contentType") String contentType);
}
