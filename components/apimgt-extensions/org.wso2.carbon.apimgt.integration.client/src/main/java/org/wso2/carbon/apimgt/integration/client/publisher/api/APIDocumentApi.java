package org.wso2.carbon.apimgt.integration.client.publisher.api;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.wso2.carbon.apimgt.integration.client.publisher.model.Document;
import org.wso2.carbon.apimgt.integration.client.publisher.model.DocumentList;


import java.io.File;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2017-01-24T00:03:49.624+05:30")
public interface APIDocumentApi  {


  /**
   * Get document content
   * Downloads a FILE type document/get the inline content or source url of a certain document. 
   * @param apiId **API ID** consisting of the **UUID** of the API. The combination of the provider of the API, name of the API and the version is also accepted as a valid API ID. Should be formatted as **provider-name-version**.  (required)
   * @param documentId **Document Identifier**  (required)
   * @param accept Media types acceptable for the response. Default is JSON.  (optional, default to JSON)
   * @param ifNoneMatch Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec.  (optional)
   * @param ifModifiedSince Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource.  (optional)
   * @return void
   */
  @RequestLine("GET /apis/{apiId}/documents/{documentId}/content")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "Accept: {accept}",
    
    "If-None-Match: {ifNoneMatch}",
    
    "If-Modified-Since: {ifModifiedSince}"
  })
  void apisApiIdDocumentsDocumentIdContentGet(@Param("apiId") String apiId, @Param("documentId") String documentId,
                                              @Param("accept") String accept, @Param("ifNoneMatch") String ifNoneMatch,
                                              @Param("ifModifiedSince") String ifModifiedSince);

  /**
   * Update API document content.
   * Upload a file to a document or add inline content to the document.  Document&#39;s source type should be **FILE** in order to upload a file to the document using **file** parameter. Document&#39;s source type should be **INLINE** in order to add inline content to the document using **inlineContent** parameter.  Only one of **file** or **inlineContent** can be specified at one time. 
   * @param apiId **API ID** consisting of the **UUID** of the API. The combination of the provider of the API, name of the API and the version is also accepted as a valid API ID. Should be formatted as **provider-name-version**.  (required)
   * @param documentId **Document Identifier**  (required)
   * @param contentType Media type of the entity in the body. Default is JSON.  (required)
   * @param file Document to upload (optional)
   * @param inlineContent Inline content of the document (optional)
   * @param ifMatch Validator for conditional requests; based on ETag.  (optional)
   * @param ifUnmodifiedSince Validator for conditional requests; based on Last Modified header.  (optional)
   * @return Document
   */
  @RequestLine("POST /apis/{apiId}/documents/{documentId}/content")
  @Headers({
    "Content-type: multipart/form-data",
    "Accept: application/json",
    "Content-Type: {contentType}",
    
    "If-Match: {ifMatch}",
    
    "If-Unmodified-Since: {ifUnmodifiedSince}"
  })
  Document apisApiIdDocumentsDocumentIdContentPost(@Param("apiId") String apiId, @Param("documentId") String documentId,
                                                   @Param("contentType") String contentType, @Param("file") File file,
                                                   @Param("inlineContent") String inlineContent,
                                                   @Param("ifMatch") String ifMatch,
                                                   @Param("ifUnmodifiedSince") String ifUnmodifiedSince);

  /**
   * Delete an API Document
   * Delete a document of an API 
   * @param apiId **API ID** consisting of the **UUID** of the API. The combination of the provider of the API, name of the API and the version is also accepted as a valid API ID. Should be formatted as **provider-name-version**.  (required)
   * @param documentId **Document Identifier**  (required)
   * @param ifMatch Validator for conditional requests; based on ETag.  (optional)
   * @param ifUnmodifiedSince Validator for conditional requests; based on Last Modified header.  (optional)
   * @return void
   */
  @RequestLine("DELETE /apis/{apiId}/documents/{documentId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "If-Match: {ifMatch}",
    
    "If-Unmodified-Since: {ifUnmodifiedSince}"
  })
  void apisApiIdDocumentsDocumentIdDelete(@Param("apiId") String apiId, @Param("documentId") String documentId,
                                          @Param("ifMatch") String ifMatch,
                                          @Param("ifUnmodifiedSince") String ifUnmodifiedSince);

  /**
   * Get an API Document
   * Get a particular document associated with an API. 
   * @param apiId **API ID** consisting of the **UUID** of the API. The combination of the provider of the API, name of the API and the version is also accepted as a valid API ID. Should be formatted as **provider-name-version**.  (required)
   * @param documentId **Document Identifier**  (required)
   * @param accept Media types acceptable for the response. Default is JSON.  (optional, default to JSON)
   * @param ifNoneMatch Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec.  (optional)
   * @param ifModifiedSince Validator for conditional requests; based on Last Modified header of the formerly
   *                        retrieved variant of the resource.  (optional)
   * @return Document
   */
  @RequestLine("GET /apis/{apiId}/documents/{documentId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "Accept: {accept}",
    
    "If-None-Match: {ifNoneMatch}",
    
    "If-Modified-Since: {ifModifiedSince}"
  })
  Document apisApiIdDocumentsDocumentIdGet(@Param("apiId") String apiId, @Param("documentId") String documentId,
                                           @Param("accept") String accept, @Param("ifNoneMatch") String ifNoneMatch,
                                           @Param("ifModifiedSince") String ifModifiedSince);

  /**
   * Update an API Document
   * Update document details. 
   * @param apiId **API ID** consisting of the **UUID** of the API. The combination of the provider of the API, name of the API and the version is also accepted as a valid API ID. Should be formatted as **provider-name-version**.  (required)
   * @param documentId **Document Identifier**  (required)
   * @param body Document object that needs to be added  (required)
   * @param contentType Media type of the entity in the body. Default is JSON.  (required)
   * @param ifMatch Validator for conditional requests; based on ETag.  (optional)
   * @param ifUnmodifiedSince Validator for conditional requests; based on Last Modified header.  (optional)
   * @return Document
   */
  @RequestLine("PUT /apis/{apiId}/documents/{documentId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "Content-Type: {contentType}",
    
    "If-Match: {ifMatch}",
    
    "If-Unmodified-Since: {ifUnmodifiedSince}"
  })
  Document apisApiIdDocumentsDocumentIdPut(@Param("apiId") String apiId, @Param("documentId") String documentId,
                                           Document body, @Param("contentType") String contentType,
                                           @Param("ifMatch") String ifMatch,
                                           @Param("ifUnmodifiedSince") String ifUnmodifiedSince);

  /**
   * Get API Documents
   * Get a list of documents belonging to an API. 
   * @param apiId **API ID** consisting of the **UUID** of the API. The combination of the provider of the API, name of the API and the version is also accepted as a valid API ID. Should be formatted as **provider-name-version**.  (required)
   * @param limit Maximum size of resource array to return.  (optional, default to 25)
   * @param offset Starting point within the complete list of items qualified.  (optional, default to 0)
   * @param accept Media types acceptable for the response. Default is JSON.  (optional, default to JSON)
   * @param ifNoneMatch Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec.  (optional)
   * @return DocumentList
   */
  @RequestLine("GET /apis/{apiId}/documents?limit={limit}&offset={offset}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "Accept: {accept}",
    
    "If-None-Match: {ifNoneMatch}"
  })
  DocumentList apisApiIdDocumentsGet(@Param("apiId") String apiId, @Param("limit") Integer limit,
                                     @Param("offset") Integer offset, @Param("accept") String accept,
                                     @Param("ifNoneMatch") String ifNoneMatch);

  /**
   * Add a new document
   * Add a new document to an API 
   * @param apiId **API ID** consisting of the **UUID** of the API. The combination of the provider of the API, name of the API and the version is also accepted as a valid API ID. Should be formatted as **provider-name-version**.  (required)
   * @param body Document object that needs to be added  (required)
   * @param contentType Media type of the entity in the body. Default is JSON.  (required)
   * @return Document
   */
  @RequestLine("POST /apis/{apiId}/documents")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "Content-Type: {contentType}"
  })
  Document apisApiIdDocumentsPost(@Param("apiId") String apiId, Document body, @Param("contentType") String contentType);
}
