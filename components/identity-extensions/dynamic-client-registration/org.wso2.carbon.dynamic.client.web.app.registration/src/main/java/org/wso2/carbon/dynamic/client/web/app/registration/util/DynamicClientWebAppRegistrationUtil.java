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

import com.google.gson.stream.JsonReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.dynamic.client.registration.DynamicClientRegistrationException;
import org.wso2.carbon.dynamic.client.registration.profile.RegistrationProfile;
import org.wso2.carbon.dynamic.client.web.app.registration.OAuthApp;
import org.wso2.carbon.dynamic.client.web.app.registration.OAuthSettings;
import org.wso2.carbon.dynamic.client.web.app.registration.internal.DynamicClientWebAppRegistrationDataHolder;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.NetworkUtils;

import javax.servlet.ServletContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.nio.charset.Charset;

/**
 * Holds the utility methods to be used in Dynamic client web app registration component.
 */
public class DynamicClientWebAppRegistrationUtil {

    private final static String OAUTH_PARAM_GRANT_TYPE = "grantType";
    private final static String OAUTH_PARAM_TOKEN_SCOPE = "tokenScope";
    private final static String OAUTH_PARAM_SAAS_APP = "saasApp";
    private final static String OAUTH_PARAM_CALLBACK_URL = "callbackURL";
    private static final String JAGGERY_APP_OAUTH_CONFIG_PATH =
            "config" + File.separator + "oauth.json";

    private static final Log log =
            LogFactory.getLog(DynamicClientWebAppRegistrationUtil.class);
    private static final String CHARSET_UTF_8 = "UTF-8";

    public static Registry getGovernanceRegistry() throws DynamicClientRegistrationException {
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            return DynamicClientWebAppRegistrationDataHolder.getInstance().getRegistryService()
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
        String resourcePath =
                DynamicClientWebAppRegistrationConstants.OAUTH_APP_DATA_REGISTRY_PATH + "/" + appName;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Retrieving OAuth application " + appName + " data from Registry");
            }
            resource = DynamicClientWebAppRegistrationUtil.getRegistryResource(resourcePath);
            if (resource != null) {
                JAXBContext context = JAXBContext.newInstance(OAuthApp.class);
                Unmarshaller unmarshaller = context.createUnmarshaller();
                return (OAuthApp) unmarshaller.unmarshal(
                        new StringReader(new String((byte[]) resource.getContent(), Charset
                                .forName(
                                        DynamicClientWebAppRegistrationConstants.CharSets.CHARSET_UTF8))));
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
        boolean status;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Persisting OAuth application data in Registry");
            }
            StringWriter writer = new StringWriter();
            JAXBContext context = JAXBContext.newInstance(OAuthApp.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.marshal(oAuthApp, writer);

            Resource resource =
                    DynamicClientWebAppRegistrationUtil.getGovernanceRegistry().newResource();
            resource.setContent(writer.toString());
            resource.setMediaType(DynamicClientWebAppRegistrationConstants.ContentTypes.MEDIA_TYPE_XML);
            String resourcePath =
                    DynamicClientWebAppRegistrationConstants.OAUTH_APP_DATA_REGISTRY_PATH + "/" +
                    oAuthApp.getWebAppName();
            status =
                    DynamicClientWebAppRegistrationUtil.putRegistryResource(resourcePath, resource);
        } catch (RegistryException e) {
            throw new DynamicClientRegistrationException(
                    "Error occurred while persisting OAuth application data : " +
                    oAuthApp.getClientName(), e);
        } catch (JAXBException e) {
            throw new DynamicClientRegistrationException(
                    "Error occurred while parsing the OAuth application data : " +
                    oAuthApp.getWebAppName(), e);
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

    public static String getUserName() {
        String username = "";
        RealmService realmService =
                DynamicClientWebAppRegistrationDataHolder.getInstance().getRealmService();
        if (realmService != null) {
            username = realmService.getBootstrapRealmConfiguration().getAdminUserName();
        }
        return username;
    }

    public static RegistrationProfile constructRegistrationProfile(ServletContext servletContext,
                                                                   String webAppName) {
        RegistrationProfile registrationProfile;
        registrationProfile = new RegistrationProfile();
        registrationProfile.setGrantType(servletContext.getInitParameter(
                DynamicClientWebAppRegistrationUtil.OAUTH_PARAM_GRANT_TYPE));
        registrationProfile.setTokenScope(servletContext.getInitParameter(
                DynamicClientWebAppRegistrationUtil.OAUTH_PARAM_TOKEN_SCOPE));
        registrationProfile.setOwner(DynamicClientWebAppRegistrationUtil.getUserName());
        String callbackURL = servletContext.getInitParameter(
                DynamicClientWebAppRegistrationUtil.OAUTH_PARAM_CALLBACK_URL);
        if ((callbackURL != null) && !callbackURL.isEmpty()) {
            registrationProfile.setCallbackUrl(callbackURL);
        } else {
            registrationProfile.setCallbackUrl(DynamicClientWebAppRegistrationUtil.getCallbackUrl(
                    webAppName));
        }
        registrationProfile.setClientName(webAppName);
        registrationProfile.setSaasApp(Boolean.parseBoolean(servletContext.getInitParameter(
                DynamicClientWebAppRegistrationUtil.OAUTH_PARAM_SAAS_APP)));

        return registrationProfile;
    }

    public static RegistrationProfile constructRegistrationProfile(
            OAuthSettings oAuthSettings, String webAppName) {
        RegistrationProfile registrationProfile = new RegistrationProfile();
        if (oAuthSettings != null) {
            registrationProfile.setGrantType(oAuthSettings.getGrantType());
            registrationProfile.setTokenScope(oAuthSettings.getTokenScope());
            registrationProfile.setClientName(webAppName);
            registrationProfile.setSaasApp(oAuthSettings.isSaasApp());
            registrationProfile.setOwner(DynamicClientWebAppRegistrationUtil.getUserName());
            if (oAuthSettings.getCallbackURL() != null) {
                registrationProfile.setCallbackUrl(oAuthSettings.getCallbackURL());
            } else {
                registrationProfile.setCallbackUrl(
                        DynamicClientWebAppRegistrationUtil.getCallbackUrl(webAppName));
            }
        } else {
            log.warn(
                    "Please configure OAuth settings properly for jaggery app : " + webAppName);
        }
        return registrationProfile;
    }

    public static boolean validateRegistrationProfile(RegistrationProfile registrationProfile) {
        boolean status = true;
        if (registrationProfile.getGrantType() == null) {
            status = false;
            log.warn("Required parameter 'grantType' is missing for initiating Dynamic-Client " +
                     "registration for webapp : " + registrationProfile.getClientName());
        }
        if (registrationProfile.getTokenScope() == null) {
            status = false;
            log.warn("Required parameter 'tokenScope' is missing for initiating Dynamic-Client " +
                     "registration for webapp : " + registrationProfile.getClientName());
        }
        return status;
    }

    public static OAuthSettings getJaggeryAppOAuthSettings(ServletContext servletContext) {
        OAuthSettings oAuthSettings = new OAuthSettings();
        try {
            InputStream inputStream =
                    servletContext.getResourceAsStream(JAGGERY_APP_OAUTH_CONFIG_PATH);
            if (inputStream != null) {
                JsonReader reader =
                        new JsonReader(new InputStreamReader(inputStream, CHARSET_UTF_8));
                reader.beginObject();
                while (reader.hasNext()) {
                    String key = reader.nextName();
                    switch (key) {
                        case DynamicClientWebAppRegistrationConstants.DYNAMIC_CLIENT_REQUIRED_FLAG:
                            oAuthSettings.setRequireDynamicClientRegistration(reader.nextBoolean());
                            break;
                        case DynamicClientWebAppRegistrationUtil.OAUTH_PARAM_GRANT_TYPE:
                            oAuthSettings.setGrantType(reader.nextString());
                            break;
                        case DynamicClientWebAppRegistrationUtil.OAUTH_PARAM_TOKEN_SCOPE:
                            oAuthSettings.setTokenScope(reader.nextString());
                            break;
                        case DynamicClientWebAppRegistrationUtil.OAUTH_PARAM_SAAS_APP:
                            oAuthSettings.setSaasApp(reader.nextBoolean());
                            break;
                        case DynamicClientWebAppRegistrationUtil.OAUTH_PARAM_CALLBACK_URL:
                            oAuthSettings.setCallbackURL(reader.nextString());
                            break;
                    }
                }
                return oAuthSettings;
            }
        } catch (UnsupportedEncodingException e) {
            log.error("Error occurred while initializing OAuth settings for the Jaggery app.", e);
        } catch (IOException e) {
            log.error("Error occurred while initializing OAuth settings for the Jaggery app.", e);
        }
        return oAuthSettings;
    }

    public static String getServerBaseUrl() {
        // Hostname
        String hostName = "localhost";
        try {
            hostName = NetworkUtils.getMgtHostName();
        } catch (Exception ignored) {
        }
        // HTTPS port
        String mgtConsoleTransport = CarbonUtils.getManagementTransport();
        ConfigurationContextService configContextService =
                DynamicClientWebAppRegistrationDataHolder.getInstance().getConfigurationContextService();
        int port = CarbonUtils.getTransportPort(configContextService, mgtConsoleTransport);
        int httpsProxyPort =
                CarbonUtils.getTransportProxyPort(configContextService.getServerConfigContext(),
                                                  mgtConsoleTransport);
        if (httpsProxyPort > 0) {
            port = httpsProxyPort;
        }
        return "https://" + hostName + ":" + port;
    }

    public static String getCallbackUrl(String context) {
        return getServerBaseUrl() + "/" + context;
    }

    public static void addClientCredentialsToWebContext(OAuthApp oAuthApp,
                                                        ServletContext servletContext) {
        if(oAuthApp != null){
            //Check for client credentials
            if ((oAuthApp.getClientKey() != null && !oAuthApp.getClientKey().isEmpty()) &&
                (oAuthApp.getClientSecret() != null && !oAuthApp.getClientSecret().isEmpty())) {
                servletContext.setAttribute(DynamicClientWebAppRegistrationConstants.OAUTH_CLIENT_KEY,
                                            oAuthApp.getClientKey());
                servletContext.setAttribute(DynamicClientWebAppRegistrationConstants.OAUTH_CLIENT_SECRET,
                                            oAuthApp.getClientSecret());
            } else {
                log.warn("Client credentials not found for web app : " + oAuthApp.getWebAppName());
            }
        }
    }
}