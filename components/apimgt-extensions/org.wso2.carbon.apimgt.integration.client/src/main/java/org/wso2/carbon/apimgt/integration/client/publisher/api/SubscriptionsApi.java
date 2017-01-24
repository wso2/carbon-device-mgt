package org.wso2.carbon.apimgt.integration.client.publisher.api;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.wso2.carbon.apimgt.integration.client.publisher.model.Subscription;
import org.wso2.carbon.apimgt.integration.client.publisher.model.SubscriptionList;


@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2017-01-24T00:03:49.624+05:30")
public interface SubscriptionsApi  {


  /**
   * Block a subscription
   * Block a subscription. 
   * @param subscriptionId Subscription Id  (required)
   * @param blockState Subscription block state.  (required)
   * @param ifMatch Validator for conditional requests; based on ETag.  (optional)
   * @param ifUnmodifiedSince Validator for conditional requests; based on Last Modified header.  (optional)
   * @return void
   */
  @RequestLine("POST /subscriptions/block-subscription?subscriptionId={subscriptionId}&blockState={blockState}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "If-Match: {ifMatch}",
    
    "If-Unmodified-Since: {ifUnmodifiedSince}"
  })
  void subscriptionsBlockSubscriptionPost(@Param("subscriptionId") String subscriptionId,
                                          @Param("blockState") String blockState, @Param("ifMatch") String ifMatch,
                                          @Param("ifUnmodifiedSince") String ifUnmodifiedSince);

  /**
   * Get All Subscriptions
   * Get subscription list. The API Identifier and corresponding Application Identifier the subscriptions of which are to be returned are passed as parameters. 
   * @param apiId **API ID** consisting of the **UUID** of the API. The combination of the provider of the API, name of the API and the version is also accepted as a valid API I. Should be formatted as **provider-name-version**.  (required)
   * @param limit Maximum size of resource array to return.  (optional, default to 25)
   * @param offset Starting point within the complete list of items qualified.  (optional, default to 0)
   * @param accept Media types acceptable for the response. Default is JSON.  (optional, default to JSON)
   * @param ifNoneMatch Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec.  (optional)
   * @return SubscriptionList
   */
  @RequestLine("GET /subscriptions?apiId={apiId}&limit={limit}&offset={offset}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "Accept: {accept}",
    
    "If-None-Match: {ifNoneMatch}"
  })
  SubscriptionList subscriptionsGet(@Param("apiId") String apiId, @Param("limit") Integer limit,
                                    @Param("offset") Integer offset, @Param("accept") String accept,
                                    @Param("ifNoneMatch") String ifNoneMatch);

  /**
   * Get a Subscription
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

  /**
   * Unblock a Subscription
   * Unblock a subscription. 
   * @param subscriptionId Subscription Id  (required)
   * @param ifMatch Validator for conditional requests; based on ETag.  (optional)
   * @param ifUnmodifiedSince Validator for conditional requests; based on Last Modified header.  (optional)
   * @return void
   */
  @RequestLine("POST /subscriptions/unblock-subscription?subscriptionId={subscriptionId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "If-Match: {ifMatch}",
    
    "If-Unmodified-Since: {ifUnmodifiedSince}"
  })
  void subscriptionsUnblockSubscriptionPost(@Param("subscriptionId") String subscriptionId,
                                            @Param("ifMatch") String ifMatch,
                                            @Param("ifUnmodifiedSince") String ifUnmodifiedSince);
}
