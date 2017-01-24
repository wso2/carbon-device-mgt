package org.wso2.carbon.apimgt.integration.client.publisher.api;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.wso2.carbon.apimgt.integration.client.publisher.model.Application;


@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2017-01-24T00:03:49.624+05:30")
public interface ApplicationsApi  {


  /**
   * Get Application
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
}
