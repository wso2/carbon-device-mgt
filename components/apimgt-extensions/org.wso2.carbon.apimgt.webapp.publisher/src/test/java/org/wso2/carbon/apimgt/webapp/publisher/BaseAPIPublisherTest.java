/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.apimgt.webapp.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.Mockito;
import org.testng.annotations.BeforeSuite;
import org.wso2.carbon.apimgt.integration.client.IntegrationClientServiceImpl;
import org.wso2.carbon.apimgt.integration.client.internal.APIIntegrationClientDataHolder;
import org.wso2.carbon.apimgt.integration.client.publisher.PublisherClient;
import org.wso2.carbon.apimgt.integration.client.service.IntegrationClientService;
import org.wso2.carbon.apimgt.integration.generated.client.publisher.api.APIsApi;
import org.wso2.carbon.apimgt.webapp.publisher.internal.APIPublisherDataHolder;
import org.wso2.carbon.apimgt.webapp.publisher.utils.MockApi;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.internal.OSGiDataHolder;
import org.wso2.carbon.identity.jwt.client.extension.JWTClient;
import org.wso2.carbon.identity.jwt.client.extension.dto.AccessTokenInfo;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.identity.jwt.client.extension.internal.JWTClientExtensionDataHolder;
import org.wso2.carbon.identity.jwt.client.extension.service.JWTClientManagerService;
import org.wso2.carbon.identity.jwt.client.extension.service.JWTClientManagerServiceImpl;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryDataHolder;
import org.wso2.carbon.registry.core.jdbc.realm.InMemoryRealmService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;

import static org.mockito.Mockito.doReturn;

/**
 * Base class which initializes carbonContext and needed services.
 */
public abstract class BaseAPIPublisherTest {

    private static final Log log = LogFactory.getLog(BaseAPIPublisherTest.class);

    @BeforeSuite
    public void initialize() throws Exception {
        this.initializeCarbonContext();
        this.initServices();
    }

    private void initializeCarbonContext() throws RegistryException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resourceUrl = classLoader.getResource("carbon-home");
        if (resourceUrl != null) {
            File carbonHome = new File(resourceUrl.getFile());
            System.setProperty("carbon.home", carbonHome.getAbsolutePath());
        }
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.
                SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
    }

    private void initServices() throws NoSuchFieldException, IllegalAccessException,
            JWTClientException, UserStoreException, RegistryException {

        IntegrationClientService integrationClientService = Mockito.mock(IntegrationClientServiceImpl.class,
                Mockito.CALLS_REAL_METHODS);
        APIPublisherDataHolder.getInstance().setIntegrationClientService(integrationClientService);
        PublisherClient publisherClient = Mockito.mock(PublisherClient.class, Mockito.CALLS_REAL_METHODS);
        doReturn(publisherClient).when(integrationClientService).getPublisherClient();

        APIsApi api = new MockApi();
        Field field = PublisherClient.class.getDeclaredField("api");
        field.setAccessible(true);
        field.set(publisherClient, api);

        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        final String REQUIRED_SCOPE =
                "apim:api_create apim:api_view apim:api_publish apim:subscribe apim:tier_view apim:tier_manage " +
                        "apim:subscription_view apim:subscription_block";
        accessTokenInfo.setScopes(REQUIRED_SCOPE);

        JWTClientManagerService jwtClientManagerService = Mockito.mock(JWTClientManagerServiceImpl.class,
                Mockito.CALLS_REAL_METHODS);
        JWTClient jwtClient = Mockito.mock(JWTClient.class, Mockito.CALLS_REAL_METHODS);
        doReturn(accessTokenInfo).when(jwtClient).getAccessToken(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString());
        doReturn(jwtClient).when(jwtClientManagerService).getJWTClient();

        APIIntegrationClientDataHolder.getInstance().setJwtClientManagerService(jwtClientManagerService);
        RegistryService registryService = this.getRegistryService();
        OSGiDataHolder.getInstance().setRegistryService(registryService);
        JWTClientExtensionDataHolder.getInstance().setRegistryService(registryService);
    }

    private RegistryService getRegistryService() throws RegistryException, UserStoreException {
        RealmService realmService = new InMemoryRealmService();
        APIPublisherDataHolder.getInstance().setRealmService(realmService);
        RegistryDataHolder.getInstance().setRealmService(realmService);
        JWTClientExtensionDataHolder.getInstance().setRealmService(realmService);
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("carbon-home/repository/" +
                "conf/registry.xml");
        RegistryContext context = RegistryContext.getBaseInstance(is, realmService);
        context.setSetup(true);
        return context.getEmbeddedRegistryService();
    }
}
