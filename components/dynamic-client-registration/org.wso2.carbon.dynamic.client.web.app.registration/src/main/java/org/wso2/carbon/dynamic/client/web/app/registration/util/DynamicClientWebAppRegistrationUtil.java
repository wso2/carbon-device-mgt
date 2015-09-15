/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.dynamic.client.web.app.registration.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.dynamic.client.registration.DynamicClientRegistrationException;
import org.wso2.carbon.dynamic.client.registration.profile.RegistrationProfile;
import org.wso2.carbon.dynamic.client.web.app.registration.OAuthApp;
import org.wso2.carbon.dynamic.client.web.app.registration.internal.DynamicClientRegistrationDataHolder;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;

import javax.servlet.ServletContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;

/**
 * Holds the utility methods to be used in Dynamic client web app registration component.
 */
public class DynamicClientWebAppRegistrationUtil {

    private final static String OAUTH_PARAM_GRANT_TYPE = "grant-type";
    private final static String OAUTH_PARAM_TOKEN_SCOPE = "token-scope";
    private final static String SP_PARAM_SAAS_APP = "saas-app";

    private static final Log log =
            LogFactory.getLog(DynamicClientWebAppRegistrationUtil.class);

    public static Registry getGovernanceRegistry() throws DynamicClientRegistrationException {
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            return DynamicClientRegistrationDataHolder.getInstance().getRegistryService()
                                                      .getGovernanceSystemRegistry(
                                                              tenantId);
        } catch (RegistryException e) {
            throw new DynamicClientRegistrationException(
                    "Error in retrieving governance registry instance: " +
                    e.getMessage(), e);
        }
    }

    public static OAuthApp getOAuthApplicationData(String appName)
            throws DynamicClientRegistrationException {
        Resource resource;
        String resourcePath = DynamicClientRegistrationConstants.OAUTH_APP_DATA_REGISTRY_PATH + "/" + appName;
        try {
            resource = DynamicClientWebAppRegistrationUtil.getRegistryResource(resourcePath);
            if (resource != null) {
                JAXBContext context = JAXBContext.newInstance(OAuthApp.class);
                Unmarshaller unmarshaller = context.createUnmarshaller();
                return (OAuthApp) unmarshaller.unmarshal(
                        new StringReader(new String((byte[]) resource.getContent(), Charset
                                .forName(
                                        DynamicClientRegistrationConstants.CharSets.CHARSET_UTF8))));
            }
            return new OAuthApp();
        } catch (JAXBException e) {
            throw new DynamicClientRegistrationException(
                    "Error occurred while parsing the OAuth application data : " + appName, e);
        } catch (RegistryException e) {
            throw new DynamicClientRegistrationException(
                    "Error occurred while retrieving the Registry resource of OAuth application : " +
                    appName, e);
        }
    }

    public static boolean putOAuthApplicationData(OAuthApp oAuthApp)
            throws DynamicClientRegistrationException {
        boolean status = false;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Persisting OAuth application data in Registry");
            }
            StringWriter writer = new StringWriter();
            JAXBContext context = JAXBContext.newInstance(OAuthApp.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.marshal(oAuthApp, writer);

            Resource resource = DynamicClientWebAppRegistrationUtil.getGovernanceRegistry().newResource();
            resource.setContent(writer.toString());
            resource.setMediaType(DynamicClientRegistrationConstants.ContentTypes.MEDIA_TYPE_XML);
            String resourcePath =
                    DynamicClientRegistrationConstants.OAUTH_APP_DATA_REGISTRY_PATH + "/" +
                    oAuthApp.getWebAppName();
            status = DynamicClientWebAppRegistrationUtil.putRegistryResource(resourcePath, resource);
        } catch (RegistryException e) {
            throw new DynamicClientRegistrationException(
                    "Error occurred while persisting OAuth application data : " +
                    oAuthApp.getClientName(), e);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return status;
    }

    public static boolean putRegistryResource(String path,
                                              Resource resource)
            throws DynamicClientRegistrationException {
        boolean status;
        try {
            Registry governanceRegistry = DynamicClientWebAppRegistrationUtil
                    .getGovernanceRegistry();
            governanceRegistry.beginTransaction();
            governanceRegistry.put(path, resource);
            governanceRegistry.commitTransaction();
            status = true;
        } catch (RegistryException e) {
            throw new DynamicClientRegistrationException(
                    "Error occurred while persisting registry resource : " +
                    e.getMessage(), e);
        }
        return status;
    }

    public static Resource getRegistryResource(String path)
            throws DynamicClientRegistrationException {
        try {
            Registry governanceRegistry = DynamicClientWebAppRegistrationUtil
                    .getGovernanceRegistry();
            if (governanceRegistry.resourceExists(path)) {
                return governanceRegistry.get(path);
            }
            return null;
        } catch (RegistryException e) {
            throw new DynamicClientRegistrationException(
                    "Error in retrieving registry resource : " +
                    e.getMessage(), e);
        }
    }

    public static String getUserName(){
        String username = "";
        RealmService realmService =
                DynamicClientRegistrationDataHolder.getInstance().getRealmService();
        if(realmService != null){
            username = realmService.getBootstrapRealmConfiguration().getAdminUserName();
        }
        return username;
    }

    public static RegistrationProfile constructRegistrationProfile(ServletContext servletContext, String webAppName) {
        RegistrationProfile registrationProfile = new RegistrationProfile();
        registrationProfile.setGrantType(servletContext.getInitParameter(
                DynamicClientWebAppRegistrationUtil.OAUTH_PARAM_GRANT_TYPE));
        registrationProfile.setTokenScope(servletContext.getInitParameter(
                DynamicClientWebAppRegistrationUtil.OAUTH_PARAM_TOKEN_SCOPE));
        registrationProfile.setOwner(DynamicClientWebAppRegistrationUtil.getUserName());
        //TODO : Need to get the hostname properly
        registrationProfile.setCallbackUrl("http://localhost:9763/" + webAppName);
        registrationProfile.setClientName(webAppName);
        registrationProfile.setSaasApp(Boolean.parseBoolean(servletContext.getInitParameter(
                DynamicClientWebAppRegistrationUtil.SP_PARAM_SAAS_APP)));
        return registrationProfile;
    }

    public static boolean validateRegistrationProfile(RegistrationProfile registrationProfile) {
        boolean status = true;
        if(registrationProfile.getGrantType() == null){
            status = false;
            log.warn("Required parameter 'grant-type' is missing for initiating Dynamic-Client " +
                     "registration for webapp : " + registrationProfile.getClientName());
        }
        if(registrationProfile.getTokenScope() == null){
            status = false;
            log.warn("Required parameter 'token-scope' is missing for initiating Dynamic-Client " +
                     "registration for webapp : " + registrationProfile.getClientName());
        }
        return status;
    }
}
