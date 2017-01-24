package org.wso2.carbon.apimgt.integration.client.store.api;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.wso2.carbon.apimgt.integration.client.store.model.Tier;


@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2017-01-24T00:03:54.991+05:30")
public interface TierindividualApi  {


  /**
   * Get tier details 
   * Get tier details 
   * @param tierName Tier name  (required)
   * @param tierLevel List API or Application type tiers.  (required)
   * @param xWSO2Tenant For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retirieved from.  (optional)
   * @param accept Media types acceptable for the response. Default is JSON.  (optional, default to JSON)
   * @param ifNoneMatch Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec.  (optional)
   * @param ifModifiedSince Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource.  (optional)
   * @return Tier
   */
  @RequestLine("GET /tiers/{tierLevel}/{tierName}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "X-WSO2-Tenant: {xWSO2Tenant}",
    
    "Accept: {accept}",
    
    "If-None-Match: {ifNoneMatch}",
    
    "If-Modified-Since: {ifModifiedSince}"
  })
  Tier tiersTierLevelTierNameGet(@Param("tierName") String tierName, @Param("tierLevel") String tierLevel,
                                 @Param("xWSO2Tenant") String xWSO2Tenant, @Param("accept") String accept,
                                 @Param("ifNoneMatch") String ifNoneMatch,
                                 @Param("ifModifiedSince") String ifModifiedSince);
}
