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
import org.wso2.carbon.apimgt.integration.client.store.model.API;
import org.wso2.carbon.apimgt.integration.client.store.model.Document;
import org.wso2.carbon.apimgt.integration.client.store.model.DocumentList;


@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2017-01-24T00:03:54.991+05:30")
public interface APIindividualApi  {


  /**
   * Downloads a FILE type document/get the inline content or source url of a certain document. 
   * Downloads a FILE type document/get the inline content or source url of a certain document. 
   * @param apiId **API ID** consisting of the **UUID** of the API. The combination of the provider of the API, name of the API and the version is also accepted as a valid API ID. Should be formatted as **provider-name-version**.  (required)
   * @param documentId **Document Identifier**  (required)
   * @param xWSO2Tenant For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retirieved from.  (optional)
   * @param accept Media types acceptable for the response. Default is JSON.  (optional, default to JSON)
   * @param ifNoneMatch Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec.  (optional)
   * @param ifModifiedSince Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource.  (optional)
   * @return void
   */
  @RequestLine("GET /apis/{apiId}/documents/{documentId}/content")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "X-WSO2-Tenant: {xWSO2Tenant}",
    
    "Accept: {accept}",
    
    "If-None-Match: {ifNoneMatch}",
    
    "If-Modified-Since: {ifModifiedSince}"
  })
  void apisApiIdDocumentsDocumentIdContentGet(@Param("apiId") String apiId, @Param("documentId") String documentId,
                                              @Param("xWSO2Tenant") String xWSO2Tenant, @Param("accept") String accept,
                                              @Param("ifNoneMatch") String ifNoneMatch,
                                              @Param("ifModifiedSince") String ifModifiedSince);

  /**
   * Get a particular document associated with an API. 
   * Get a particular document associated with an API. 
   * @param apiId **API ID** consisting of the **UUID** of the API. The combination of the provider of the API, name of the API and the version is also accepted as a valid API ID. Should be formatted as **provider-name-version**.  (required)
   * @param documentId **Document Identifier**  (required)
   * @param xWSO2Tenant For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retirieved from.  (optional)
   * @param accept Media types acceptable for the response. Default is JSON.  (optional, default to JSON)
   * @param ifNoneMatch Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec.  (optional)
   * @param ifModifiedSince Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource.  (optional)
   * @return Document
   */
  @RequestLine("GET /apis/{apiId}/documents/{documentId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "X-WSO2-Tenant: {xWSO2Tenant}",
    
    "Accept: {accept}",
    
    "If-None-Match: {ifNoneMatch}",
    
    "If-Modified-Since: {ifModifiedSince}"
  })
  Document apisApiIdDocumentsDocumentIdGet(@Param("apiId") String apiId, @Param("documentId") String documentId,
                                           @Param("xWSO2Tenant") String xWSO2Tenant, @Param("accept") String accept,
                                           @Param("ifNoneMatch") String ifNoneMatch,
                                           @Param("ifModifiedSince") String ifModifiedSince);

  /**
   * Get a list of documents belonging to an API. 
   * Get a list of documents belonging to an API. 
   * @param apiId **API ID** consisting of the **UUID** of the API. The combination of the provider of the API, name of the API and the version is also accepted as a valid API ID. Should be formatted as **provider-name-version**.  (required)
   * @param limit Maximum size of resource array to return.  (optional, default to 25)
   * @param offset Starting point within the complete list of items qualified.  (optional, default to 0)
   * @param xWSO2Tenant For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retirieved from.  (optional)
   * @param accept Media types acceptable for the response. Default is JSON.  (optional, default to JSON)
   * @param ifNoneMatch Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec.  (optional)
   * @return DocumentList
   */
  @RequestLine("GET /apis/{apiId}/documents?limit={limit}&offset={offset}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "X-WSO2-Tenant: {xWSO2Tenant}",
    
    "Accept: {accept}",
    
    "If-None-Match: {ifNoneMatch}"
  })
  DocumentList apisApiIdDocumentsGet(@Param("apiId") String apiId, @Param("limit") Integer limit,
                                     @Param("offset") Integer offset, @Param("xWSO2Tenant") String xWSO2Tenant,
                                     @Param("accept") String accept, @Param("ifNoneMatch") String ifNoneMatch);

  /**
   * Get Details of API 
   * Get details of an API 
   * @param apiId **API ID** consisting of the **UUID** of the API. The combination of the provider of the API, name of the API and the version is also accepted as a valid API ID. Should be formatted as **provider-name-version**.  (required)
   * @param accept Media types acceptable for the response. Default is JSON.  (optional, default to JSON)
   * @param ifNoneMatch Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec.  (optional)
   * @param ifModifiedSince Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource.  (optional)
   * @param xWSO2Tenant For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retirieved from.  (optional)
   * @return API
   */
  @RequestLine("GET /apis/{apiId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "Accept: {accept}",
    
    "If-None-Match: {ifNoneMatch}",
    
    "If-Modified-Since: {ifModifiedSince}",
    
    "X-WSO2-Tenant: {xWSO2Tenant}"
  })
  API apisApiIdGet(@Param("apiId") String apiId, @Param("accept") String accept,
                   @Param("ifNoneMatch") String ifNoneMatch, @Param("ifModifiedSince") String ifModifiedSince,
                   @Param("xWSO2Tenant") String xWSO2Tenant);

  /**
   * Get the swagger of an API 
   * Get the swagger of an API 
   * @param apiId **API ID** consisting of the **UUID** of the API. The combination of the provider of the API, name of the API and the version is also accepted as a valid API ID. Should be formatted as **provider-name-version**.  (required)
   * @param accept Media types acceptable for the response. Default is JSON.  (optional, default to JSON)
   * @param ifNoneMatch Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec.  (optional)
   * @param ifModifiedSince Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource.  (optional)
   * @param xWSO2Tenant For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retirieved from.  (optional)
   * @return void
   */
  @RequestLine("GET /apis/{apiId}/swagger")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "Accept: {accept}",
    
    "If-None-Match: {ifNoneMatch}",
    
    "If-Modified-Since: {ifModifiedSince}",
    
    "X-WSO2-Tenant: {xWSO2Tenant}"
  })
  void apisApiIdSwaggerGet(@Param("apiId") String apiId, @Param("accept") String accept,
                           @Param("ifNoneMatch") String ifNoneMatch, @Param("ifModifiedSince") String ifModifiedSince,
                           @Param("xWSO2Tenant") String xWSO2Tenant);

  /**
   * Get the thumbnail image
   * Downloads a thumbnail image of an API 
   * @param apiId **API ID** consisting of the **UUID** of the API. The combination of the provider of the API, name of the API and the version is also accepted as a valid API ID. Should be formatted as **provider-name-version**.  (required)
   * @param accept Media types acceptable for the response. Default is JSON.  (optional, default to JSON)
   * @param ifNoneMatch Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec.  (optional)
   * @param ifModifiedSince Validator for conditional requests; based on Last Modified header of the formerly
   *                        retrieved variant of the resource.  (optional)
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
}
