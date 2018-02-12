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

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.webapp.publisher.config.APIResource;
import org.wso2.carbon.apimgt.webapp.publisher.config.APIResourceConfiguration;
import org.wso2.carbon.apimgt.webapp.publisher.config.WebappPublisherConfig;
import org.wso2.carbon.apimgt.webapp.publisher.dto.ApiScope;
import org.wso2.carbon.apimgt.webapp.publisher.dto.ApiUriTemplate;
import org.wso2.carbon.apimgt.webapp.publisher.exception.APIManagerPublisherException;
import org.wso2.carbon.apimgt.webapp.publisher.utils.MockServletContext;
import org.wso2.carbon.apimgt.webapp.publisher.utils.TestUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.realm.InMemoryRealmService;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;

import javax.servlet.ServletContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.wso2.carbon.apimgt.webapp.publisher.APIPublisherUtil.buildApiConfig;
import static org.wso2.carbon.apimgt.webapp.publisher.APIPublisherUtil.getApiEndpointUrl;
import static org.wso2.carbon.apimgt.webapp.publisher.APIPublisherUtil.setResourceAuthTypes;

/**
 * This is the test class for {@link APIPublisherUtil}
 */
public class APIPublisherUtilTest extends BaseAPIPublisherTest {

    private static final String AUTH_TYPE_NON_SECURED = "None";

    @BeforeTest
    public void initialConfigs() throws WebappPublisherConfigurationFailedException,
            org.wso2.carbon.user.core.UserStoreException, RegistryException {
        WebappPublisherConfig.init();
        setUserRealm();
    }

    @Test(description = "test buildAPIConfig method and ensures an APIConfig is created")
    public void buildApiConfigTest() throws UserStoreException, RegistryException {
        try {
            startTenantFlowAsTestTenant();
            ServletContext servletContext = new MockServletContext();
            APIResourceConfiguration apiDef = new APIResourceConfiguration();
            List<APIResource> resources = new ArrayList<>();
            apiDef.setResources(resources);
            APIConfig apiConfig = buildApiConfig(servletContext, apiDef);
            Assert.assertNotNull(apiConfig, "API configuration is null.");
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Test(description = "test buildAPIConfig method as SuperTenant and ensures" +
            " an APIConfig is created")
    public void buildApiConfigAsSuperTenant() throws UserStoreException {
        ServletContext servletContext = new MockServletContext();
        APIResourceConfiguration apiDef = new APIResourceConfiguration();
        List<APIResource> resources = new ArrayList<>();
        apiDef.setResources(resources);
        APIConfig apiConfig = buildApiConfig(servletContext, apiDef);
        Assert.assertNotNull(apiConfig, "API configuration is null.");
    }

    @Test(description = "test buildAPIConfig with API tags specified and ensures " +
            "an APIConfig is created")
    public void buildApiConfigTestWithTags() throws UserStoreException {
        ServletContext servletContext = new MockServletContext();
        APIResourceConfiguration apiDef = new APIResourceConfiguration();
        List<APIResource> resources = new ArrayList<>();
        APIResource apiResource = new APIResource();
        resources.add(apiResource);
        apiDef.setResources(resources);
        apiDef.setTags(new String[]{"windows", "device_management"});
        APIConfig apiConfig = buildApiConfig(servletContext, apiDef);
        Assert.assertNotNull(apiConfig, "API configuration is null.");
    }

    @Test(description = "test buildAPIConfig method with API scopes specified and " +
            "ensures an APIConfig is created")
    public void buildApiConfigTestWithScope() throws UserStoreException, APIManagerPublisherException {
        ServletContext servletContext = new MockServletContext();
        APIResourceConfiguration apiDef = new APIResourceConfiguration();
        List<APIResource> resources = new ArrayList<>();
        APIResource apiResource = new APIResource();
        ApiScope apiScope = new ApiScope();
        apiScope.setDescription("testing");
        apiResource.setScope(apiScope);
        resources.add(apiResource);
        apiDef.setResources(resources);
        apiDef.setTags(new String[]{"windows", "device_management"});
        APIConfig apiConfig = buildApiConfig(servletContext, apiDef);
        Assert.assertNotNull(apiConfig, "API configuration is null.");
    }

    @Test(description = "test method for setResourceAuthTypes")
    public void testSetResourceAuthTypes() throws UserStoreException {
        ServletContext servletContext = new MockServletContext();
        APIResourceConfiguration apiDef = new APIResourceConfiguration();
        List<APIResource> resources = new ArrayList<>();
        apiDef.setResources(resources);
        APIConfig apiConfig = buildApiConfig(servletContext, apiDef);
        apiConfig.setContext("/*");
        TestUtils util = new TestUtils();
        util.setAPIURITemplates(apiConfig, "/*");
        Assert.assertNotNull(apiConfig, "API configuration is null.");
        setResourceAuthTypes(servletContext, apiConfig);
        Set<ApiUriTemplate> templates = apiConfig.getUriTemplates();
        Assert.assertEquals(templates.iterator().next().getAuthType(), AUTH_TYPE_NON_SECURED, "Resource " +
                "auth type is not properly set");
    }

    @Test(description = "test the method getApiEndpointUrl")
    public void testGetApiEndpointUrl() {
        String context = "/reboot";
        String url = getApiEndpointUrl(context);
        Assert.assertEquals(url, "https://localhost:9445/reboot", "Expected url " +
                "is not same as actual url");
    }

    @Test(expectedExceptions = WebappPublisherConfigurationFailedException.class, description =
            "this tests the method convertToDocument with a undefined file name and ensures an " +
                    "exception occurs ")
    public void testConvertToDocumentForException() throws WebappPublisherConfigurationFailedException {
        WebappPublisherUtil.convertToDocument(null);
    }

    private void setUserRealm() throws RegistryException, org.wso2.carbon.user.core.UserStoreException {
        RealmConfiguration configuration = new RealmConfiguration();
        UserRealm userRealm = new InMemoryRealmService().getUserRealm(configuration);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUserRealm(userRealm);
    }

    private void startTenantFlowAsTestTenant() throws org.wso2.carbon.user.core.UserStoreException, RegistryException {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(1212);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("test.com");
        setUserRealm();
    }
}
