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
import org.mockito.Mockito;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.integration.client.IntegrationClientServiceImpl;
import org.wso2.carbon.apimgt.integration.client.OAuthRequestInterceptor;
import org.wso2.carbon.apimgt.integration.client.model.OAuthApplication;
import org.wso2.carbon.apimgt.integration.client.publisher.PublisherClient;
import org.wso2.carbon.apimgt.integration.client.service.IntegrationClientService;
import org.wso2.carbon.apimgt.integration.generated.client.publisher.api.APIsApi;
import org.wso2.carbon.apimgt.integration.generated.client.publisher.model.API;
import org.wso2.carbon.apimgt.integration.generated.client.publisher.model.APIInfo;
import org.wso2.carbon.apimgt.integration.generated.client.publisher.model.APIList;
import org.wso2.carbon.apimgt.webapp.publisher.config.WebappPublisherConfig;
import org.wso2.carbon.apimgt.webapp.publisher.dto.ApiScope;
import org.wso2.carbon.apimgt.webapp.publisher.dto.ApiUriTemplate;
import org.wso2.carbon.apimgt.webapp.publisher.exception.APIManagerPublisherException;
import org.wso2.carbon.apimgt.webapp.publisher.internal.APIPublisherDataHolder;
import org.wso2.carbon.apimgt.webapp.publisher.utils.Api;

import java.lang.reflect.Field;
import java.util.*;

import static org.mockito.Mockito.doReturn;

/**
 * This is the test class for {@link APIPublisherServiceImpl}
 */
public class
APIPublisherServiceTest extends BaseAPIPublisherTest {
    private static final Log log = LogFactory.getLog(APIPublisherServiceTest.class);
    private APIPublisherServiceImpl apiPublisherService = new APIPublisherServiceImpl();

    @BeforeTest
    public void initialConfigs() throws Exception {
        initializeOAuthApplication();
        WebappPublisherConfig.init();
    }

    @Test(description = "Publishes an API | will fail if there are any exceptions")
    public void publishAPI() throws NoSuchFieldException, IllegalAccessException,
            APIManagerPublisherException {
        APIConfig apiConfig = new APIConfig();
        setApiConfigs(apiConfig, "testAPI-0");
        apiPublisherService.publishAPI(apiConfig);
    }

    @Test(description = "Testing for API status CREATED | will fail if there are any exceptions")
    public void publishCreatedAPI() throws APIManagerPublisherException, NoSuchFieldException,
            IllegalAccessException {
        APIConfig apiConfig = new APIConfig();
        setApiConfigs(apiConfig, "testAPI-1");
        APIPublisherDataHolder apiPublisherDataHolder = Mockito.mock(APIPublisherDataHolder.getInstance().
                getClass(), Mockito.CALLS_REAL_METHODS);
        IntegrationClientService integrationClientService = Mockito.mock(IntegrationClientServiceImpl.
                class, Mockito.CALLS_REAL_METHODS);
        doReturn(integrationClientService).when(apiPublisherDataHolder).getIntegrationClientService();
        PublisherClient publisherClient = APIPublisherDataHolder.getInstance().getIntegrationClientService().
                getPublisherClient();
        doReturn(publisherClient).when(integrationClientService).getPublisherClient();
        APIsApi apIsApi = Mockito.mock(Api.class, Mockito.CALLS_REAL_METHODS);
        doReturn(apIsApi).when(publisherClient).getApi();
        API api = Mockito.mock(API.class, Mockito.CALLS_REAL_METHODS);
        api.setStatus("CREATED");
        doReturn(api).when(apIsApi).apisPost(Mockito.any(), Mockito.anyString());
        apiPublisherService.publishAPI(apiConfig);
    }

    @Test(description = "createAPIListWithNoApi | will fail if there are any exceptions")
    private void publishWithNoAPIListCreated() throws APIManagerPublisherException {
        APIConfig apiConfig = new APIConfig();
        setApiConfigs(apiConfig, "testAPI-2");
        APIPublisherDataHolder apiPublisherDataHolder = Mockito.mock(APIPublisherDataHolder.getInstance().
                getClass(), Mockito.CALLS_REAL_METHODS);
        IntegrationClientService integrationClientService = Mockito.mock(IntegrationClientServiceImpl.
                class, Mockito.CALLS_REAL_METHODS);
        doReturn(integrationClientService).when(apiPublisherDataHolder).getIntegrationClientService();
        PublisherClient publisherClient = APIPublisherDataHolder.getInstance().getIntegrationClientService().
                getPublisherClient();
        doReturn(publisherClient).when(integrationClientService).getPublisherClient();
        APIsApi apIsApi = Mockito.mock(Api.class, Mockito.CALLS_REAL_METHODS);
        doReturn(apIsApi).when(publisherClient).getApi();
        API api = Mockito.mock(API.class, Mockito.CALLS_REAL_METHODS);
        api.setStatus("CREATED");
        doReturn(api).when(apIsApi).apisPost(Mockito.any(), Mockito.anyString());
        APIList apiList = Mockito.mock(APIList.class, Mockito.CALLS_REAL_METHODS);
        APIInfo apiInfo = new APIInfo();
        List<APIInfo> apiInfoList = new ArrayList<>();
        apiInfoList.add(apiInfo);
        apiList.list(apiInfoList);
        doReturn(apiList).when(apIsApi).apisGet(Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        doReturn(api).when(apIsApi).apisApiIdPut(Mockito.anyString(), Mockito.any(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        apiPublisherService.publishAPI(apiConfig);
    }

    @Test(description = "createAPIList | will fail if there are any exceptions")
    private void publishWithAPIListCreated() throws APIManagerPublisherException {
        APIConfig apiConfig = new APIConfig();
        setApiConfigs(apiConfig, "testAPI-3");
        APIPublisherDataHolder apiPublisherDataHolder = Mockito.mock(APIPublisherDataHolder.getInstance().
                getClass(), Mockito.CALLS_REAL_METHODS);
        IntegrationClientService integrationClientService = Mockito.mock(IntegrationClientServiceImpl.
                class, Mockito.CALLS_REAL_METHODS);
        doReturn(integrationClientService).when(apiPublisherDataHolder).getIntegrationClientService();
        PublisherClient publisherClient = APIPublisherDataHolder.getInstance().getIntegrationClientService().
                getPublisherClient();
        doReturn(publisherClient).when(integrationClientService).getPublisherClient();
        APIsApi apIsApi = Mockito.mock(Api.class, Mockito.CALLS_REAL_METHODS);
        doReturn(apIsApi).when(publisherClient).getApi();
        API api = Mockito.mock(API.class, Mockito.CALLS_REAL_METHODS);
        api.setStatus("CREATED");
        doReturn(api).when(apIsApi).apisPost(Mockito.any(), Mockito.anyString());
        APIList apiList = Mockito.mock(APIList.class, Mockito.CALLS_REAL_METHODS);
        APIInfo apiInfo = new APIInfo();
        apiInfo.setName("testAPI-3");
        apiInfo.setVersion("1.0.0");
        apiInfo.setId("test-one");
        List<APIInfo> apiInfoList = new ArrayList<>();
        apiInfoList.add(apiInfo);
        apiList.list(apiInfoList);
        doReturn(apiList).when(apIsApi).apisGet(Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        doReturn(api).when(apIsApi).apisApiIdPut(Mockito.anyString(), Mockito.any(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        apiConfig.setSharedWithAllTenants(false);
        apiPublisherService.publishAPI(apiConfig);
    }

    @Test(description = "publish API with scope added | will fail if there are any exceptions")
    private void publishWithAPIScope() throws APIManagerPublisherException {
        APIConfig apiConfig = new APIConfig();
        setApiConfigs(apiConfig, "testAPI-4");
        Set<ApiScope> scopes = new HashSet<>();
        ApiScope apiScope = new ApiScope();
        apiScope.setDescription("testing");
        scopes.add(apiScope);
        apiConfig.setScopes(scopes);
        apiPublisherService.publishAPI(apiConfig);
    }

    private void setApiConfigs(APIConfig apiConfig, String name) {
        apiConfig.setName(name);
        apiConfig.setContext("api/device-mgt/windows/v1.g0/admin/devices");
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
        apiConfig.setScopes(scopes);
        setAPIURITemplates(apiConfig);
    }

    private void setAPIURITemplates(APIConfig apiConfig) {
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
    }

    private void initializeOAuthApplication() throws NoSuchFieldException, IllegalAccessException {
        OAuthApplication oAuthApplication = new OAuthApplication();
        oAuthApplication.setClientName("admin_api_integration_client");
        oAuthApplication.setIsSaasApplication("true");
        oAuthApplication.setClientId("random");
        oAuthApplication.setClientSecret("random=");
        Field oAuth = OAuthRequestInterceptor.class.getDeclaredField("oAuthApplication");
        oAuth.setAccessible(true);
        oAuth.set(null, oAuthApplication);
    }

}

