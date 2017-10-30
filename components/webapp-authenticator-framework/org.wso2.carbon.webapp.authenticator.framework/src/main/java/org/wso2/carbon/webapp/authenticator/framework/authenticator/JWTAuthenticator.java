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

package org.wso2.carbon.webapp.authenticator.framework.authenticator;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticationInfo;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticatorFrameworkDataHolder;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * This authenticator authenticates HTTP requests using JWT header.
 */
public class JWTAuthenticator implements WebappAuthenticator {

    private static final Log log = LogFactory.getLog(JWTAuthenticator.class);
    private static final String SIGNED_JWT_AUTH_USERNAME = "http://wso2.org/claims/enduser";
    private static final String SIGNED_JWT_AUTH_TENANT_ID = "http://wso2.org/claims/enduserTenantId";
    private static final String JWT_AUTHENTICATOR = "JWT";
    private static final String JWT_ASSERTION_HEADER = "X-JWT-Assertion";
    private static final String DEFAULT_TRUST_STORE_LOCATION = "Security.TrustStore.Location";
    private static final String DEFAULT_TRUST_STORE_PASSWORD = "Security.TrustStore.Password";

    private static final Map<IssuerAlias, PublicKey> publicKeyHolder = new HashMap<>();
    private Properties properties;

    private static void loadTenantRegistry(int tenantId) throws RegistryException {
        TenantRegistryLoader tenantRegistryLoader = AuthenticatorFrameworkDataHolder.getInstance().
                getTenantRegistryLoader();
        AuthenticatorFrameworkDataHolder.getInstance().getTenantIndexingLoader().loadTenantIndex(tenantId);
        tenantRegistryLoader.loadTenantRegistry(tenantId);
    }

    @Override
    public void init() {

    }

    @Override
    public boolean canHandle(Request request) {
        String authorizationHeader = request.getHeader(JWTAuthenticator.JWT_ASSERTION_HEADER);
        return (authorizationHeader != null) && !authorizationHeader.isEmpty();
    }

    @Override
    public AuthenticationInfo authenticate(Request request, Response response) {
        String requestUri = request.getRequestURI();
        AuthenticationInfo authenticationInfo = new AuthenticationInfo();
        if (requestUri == null || "".equals(requestUri)) {
            authenticationInfo.setStatus(Status.CONTINUE);
        }
        if (requestUri == null) {
            requestUri = "";
        }
        StringTokenizer tokenizer = new StringTokenizer(requestUri, "/");
        String context = tokenizer.nextToken();
        if (context == null || "".equals(context)) {
            authenticationInfo.setStatus(Status.CONTINUE);
        }

        try {
            String authorizationHeader = request.getHeader(JWT_ASSERTION_HEADER);

            SignedJWT jwsObject = SignedJWT.parse(authorizationHeader);
            String username = jwsObject.getJWTClaimsSet().getStringClaim(SIGNED_JWT_AUTH_USERNAME);
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            int tenantId = Integer.parseInt(jwsObject.getJWTClaimsSet().getStringClaim(SIGNED_JWT_AUTH_TENANT_ID));
            String issuer = jwsObject.getJWTClaimsSet().getIssuer();
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
            IssuerAlias issuerAlias = new IssuerAlias(issuer, tenantDomain);
            PublicKey publicKey =  publicKeyHolder.get(issuerAlias);
            if (publicKey == null) {
                loadTenantRegistry(tenantId);
                KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(tenantId);
                if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    String alias = properties.getProperty(issuer);
                    if (alias != null && !alias.isEmpty()) {
                        ServerConfiguration serverConfig = CarbonUtils.getServerConfiguration();
                        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                        String trustStorePath = serverConfig.getFirstProperty(DEFAULT_TRUST_STORE_LOCATION);
                        String trustStorePassword = serverConfig.getFirstProperty(
                                DEFAULT_TRUST_STORE_PASSWORD);
                        keyStore.load(new FileInputStream(trustStorePath), trustStorePassword.toCharArray());
                        publicKey = keyStore.getCertificate(alias).getPublicKey();
                    } else {
                        authenticationInfo.setStatus(Status.FAILURE);
                        return  authenticationInfo;
                    }
                } else {
                    String ksName = tenantDomain.trim().replace('.', '-');
                    String jksName = ksName + ".jks";
                    publicKey = keyStoreManager.getKeyStore(jksName).getCertificate(tenantDomain).getPublicKey();
                }
                if (publicKey != null) {
                    issuerAlias = new IssuerAlias(tenantDomain);
                    publicKeyHolder.put(issuerAlias, publicKey);
                }
            }

            //Get the filesystem keystore default primary certificate
            JWSVerifier verifier = new RSASSAVerifier((RSAPublicKey) publicKey);
            if (jwsObject.verify(verifier)) {
                username = MultitenantUtils.getTenantAwareUsername(username);
                if (tenantId == -1) {
                    log.error("tenantDomain is not valid. username : " + username + ", tenantDomain " +
                            ": " + tenantDomain);
                } else {
                    UserStoreManager userStore = AuthenticatorFrameworkDataHolder.getInstance().getRealmService().
                            getTenantUserRealm(tenantId).getUserStoreManager();
                    if (userStore.isExistingUser(username)) {
                        authenticationInfo.setTenantId(tenantId);
                        authenticationInfo.setUsername(username);
                        authenticationInfo.setTenantDomain(tenantDomain);
                        authenticationInfo.setStatus(Status.CONTINUE);
                    }
                }
            } else {
                authenticationInfo.setStatus(Status.FAILURE);
            }
        } catch (UserStoreException e) {
            log.error("Error occurred while obtaining the user.", e);
        } catch (ParseException e) {
            log.error("Error occurred while parsing the JWT header.", e);
        } catch (JOSEException e) {
            log.error("Error occurred while verifying the JWT header.", e);
        } catch (Exception e) {
            log.error("Error occurred while verifying the JWT header.", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return authenticationInfo;
    }

    @Override
    public String getName() {
        return JWTAuthenticator.JWT_AUTHENTICATOR;
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public String getProperty(String name) {
        if (this.properties == null) {
            return null;
        }
        return this.properties.getProperty(name);
    }

    private class IssuerAlias {

        private String issuer;
        private String tenantDomain;
        private final String DEFAULT_ISSUER = "default";

        public  IssuerAlias(String tenantDomain) {
            this.issuer = DEFAULT_ISSUER;
            this.tenantDomain = tenantDomain;
        }

        public  IssuerAlias(String issuer, String tenantDomain) {
            this.issuer = issuer;
            this.tenantDomain = tenantDomain;
        }

        @Override
        public int hashCode() {
            int result = this.issuer.hashCode();
            result = 31 * result + ("@" + this.tenantDomain).hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof IssuerAlias) && issuer.equals(
                    ((IssuerAlias) obj).issuer) && tenantDomain == ((IssuerAlias) obj).tenantDomain;
        }
    }
}
