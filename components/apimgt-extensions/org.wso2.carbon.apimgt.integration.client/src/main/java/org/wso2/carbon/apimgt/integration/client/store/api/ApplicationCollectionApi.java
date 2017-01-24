package org.wso2.carbon.apimgt.integration.client.store.api;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.wso2.carbon.apimgt.integration.client.store.model.ApplicationList;


@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2017-01-24T00:03:54.991+05:30")
public interface ApplicationCollectionApi  {


  /**
   * Get a list of applications 
   * Get a list of applications 
   * @param groupId Application Group Id  (optional)
   * @param query **Search condition**.  You can search for an application by specifying the name as \&quot;query\&quot; attribute.  Eg. \&quot;app1\&quot; will match an application if the name is exactly \&quot;app1\&quot;.  Currently this does not support wildcards. Given name must exactly match the application name.  (optional)
   * @param limit Maximum size of resource array to return.  (optional, default to 25)
   * @param offset Starting point within the complete list of items qualified.  (optional, default to 0)
   * @param accept Media types acceptable for the response. Default is JSON.  (optional, default to JSON)
   * @param ifNoneMatch Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec.  (optional)
   * @return ApplicationList
   */
  @RequestLine("GET /applications?groupId={groupId}&query={query}&limit={limit}&offset={offset}")
  @Headers({
    "Content-type: application/json",
    "Accept: application/json",
    "Accept: {accept}",
    
    "If-None-Match: {ifNoneMatch}"
  })
  ApplicationList applicationsGet(@Param("groupId") String groupId, @Param("query") String query,
                                  @Param("limit") Integer limit, @Param("offset") Integer offset,
                                  @Param("accept") String accept, @Param("ifNoneMatch") String ifNoneMatch);
}
