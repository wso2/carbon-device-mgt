package org.wso2.carbon.apimgt.integration.client.publisher.api;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.wso2.carbon.apimgt.integration.client.publisher.model.EnvironmentList;


@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2017-01-24T00:03:49.624+05:30")
public interface EnvironmentsApi  {


  /**
   * Get gateway environments
   * Get a list of gateway environments configured previously. 
   * @param apiId Will return environment list for the provided API.  (optional)
   * @return EnvironmentList
   */
  @RequestLine("GET /environments?apiId={apiId}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json"
  })
  EnvironmentList environmentsGet(@Param("apiId") String apiId);
}
