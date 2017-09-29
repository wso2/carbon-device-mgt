/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.apimgt.webapp.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.integration.client.OAuthRequestInterceptor;
import org.wso2.carbon.apimgt.integration.client.model.OAuthApplication;
import org.wso2.carbon.apimgt.webapp.publisher.dto.ApiScope;
import org.wso2.carbon.apimgt.webapp.publisher.dto.ApiUriTemplate;
import org.wso2.carbon.apimgt.webapp.publisher.exception.APIManagerPublisherException;

import java.lang.reflect.Field;
import java.util.*;

public class
APIPublisherServiceTest extends BaseAPIPublisheTest{
    private static final Log log = LogFactory.getLog(APIPublisherServiceTest.class);



    @Test
    public void publishAPI() throws APIManagerPublisherException, NoSuchFieldException, IllegalAccessException {
        APIPublisherServiceImpl apiPublisherService = new APIPublisherServiceImpl();

        APIConfig apiConfig = new APIConfig();

        apiConfig.setName("Windows Device Management Administrative Service");
        apiConfig.setContext("/api/device-mgt/windows/v1.0/admin/devices");
        apiConfig.setOwner("admin");
        apiConfig.setEndpoint("https://localhost:9443/api/device-mgt/windows/v1.0/admin/devices");
        apiConfig.setVersion("1.0.0");
        apiConfig.setTransports("http,https");
        apiConfig.setPolicy(null);
        apiConfig.setSharedWithAllTenants(true);
        apiConfig.setTags(new String[]{"windows", "device_management"});
        apiConfig.setTenantDomain("carbon.super");
        apiConfig.setSecured(false);

        Map<String, ApiScope> apiScopes = new HashMap<>();
        Set<ApiScope> scopes = new HashSet<>(apiScopes.values());

      //  apiScopes.put("key","")




        apiConfig.setScopes(scopes);



        Set<ApiUriTemplate> uriTemplates = new LinkedHashSet<>();
        ApiUriTemplate template = new ApiUriTemplate();
        template.setAuthType("Application & Application User");
        template.setHttpVerb("POST");
        template.setResourceURI("https://localhost:9443/api/device-mgt/windows/v1.0/admin/devices/reboot");
       template.setUriTemplate("/reboot");
       ApiScope scope = new ApiScope();
       scope.setKey("perm:windows:reboot");
       scope.setName("Reboot");
       scope.setRoles("/permission/admin/device-mgt/devices/owning-device/operations/windows/reboot");
       scope.setDescription("Lock reset on Windows devices");
       template.setScope(scope);
        uriTemplates.add(template);

        apiConfig.setUriTemplates(uriTemplates);



        OAuthApplication oAuthApplication = new OAuthApplication();

        oAuthApplication.setClientName("admin_api_integration_client");
        oAuthApplication.setIsSaasApplication("true");
        oAuthApplication.setClientId("dsnkdsaxakdnfkanfdax=");
        oAuthApplication.setClientSecret("safjksajnjnkjcksancka");

       Field oAuth = OAuthRequestInterceptor.class.getDeclaredField("oAuthApplication");
       oAuth.setAccessible(true);
       oAuth.set(null,oAuthApplication);


        apiPublisherService.publishAPI(apiConfig);
    }

    @BeforeClass
    @Override
    public void init() throws Exception {
        // do nothing
    }
}
