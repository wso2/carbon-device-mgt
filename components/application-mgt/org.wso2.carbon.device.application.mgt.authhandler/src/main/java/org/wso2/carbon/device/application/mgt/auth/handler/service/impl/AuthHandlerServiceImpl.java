/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.application.mgt.auth.handler.service.impl;

import feign.Client;
import feign.Feign;
import feign.auth.BasicAuthRequestInterceptor;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.jaxrs.JAXRSContract;
import org.json.JSONObject;
import org.wso2.carbon.device.application.mgt.auth.handler.service.AuthHandlerService;
import org.wso2.carbon.device.application.mgt.auth.handler.util.Constants;
import org.wso2.carbon.device.application.mgt.auth.handler.util.dto.AccessTokenInfo;
import org.wso2.carbon.device.application.mgt.auth.handler.util.dto.ApiApplicationKey;
import org.wso2.carbon.device.application.mgt.auth.handler.util.dto.ApiApplicationRegistrationService;
import org.wso2.carbon.device.application.mgt.auth.handler.util.dto.ApiRegistrationProfile;
import org.wso2.carbon.device.application.mgt.auth.handler.util.dto.TokenIssuerService;
import org.wso2.carbon.device.application.mgt.auth.handler.util.dto.TokenRevokeService;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

@Path("/auth")
public class AuthHandlerServiceImpl implements AuthHandlerService {

    private TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
    };

    private Client disableHostnameVerification = new Client.Default(getTrustedSSLSocketFactory(),
            new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            }
    );

    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Response login(@QueryParam("userName") String userName, @QueryParam("password") String password) {

        try {
            ApiApplicationRegistrationService apiApplicationRegistrationService = Feign.builder()
                    .client(disableHostnameVerification)
                    .requestInterceptor(new BasicAuthRequestInterceptor(userName, password))
                    .contract(new JAXRSContract()).encoder(new JacksonEncoder()).decoder(new JacksonDecoder())
                    .target(ApiApplicationRegistrationService.class, Constants.API_APPLICATION_ENDPOINT);
            ApiRegistrationProfile apiRegistrationProfile = new ApiRegistrationProfile();
            apiRegistrationProfile.setApplicationName(Constants.APPLICATION_NAME);
            apiRegistrationProfile.setIsAllowedToAllDomains(false);
            apiRegistrationProfile.setIsMappingAnExistingOAuthApp(false);
            apiRegistrationProfile.setTags(Constants.TAGS);
            ApiApplicationKey apiApplicationKey = apiApplicationRegistrationService.register(apiRegistrationProfile);

            //PasswordGrantType
            TokenIssuerService tokenIssuerService = Feign.builder().client(disableHostnameVerification)
                    .requestInterceptor(new BasicAuthRequestInterceptor(apiApplicationKey.getConsumerKey(),
                            apiApplicationKey.getConsumerSecret()))
                    .contract(new JAXRSContract()).encoder(new JacksonEncoder()).decoder(new JacksonDecoder())
                    .target(TokenIssuerService.class, Constants.TOKEN_ENDPOINT);
            AccessTokenInfo accessTokenInfo = tokenIssuerService.getToken(Constants.PASSWORD_GRANT_TYPE,
                    userName, password, Constants.SCOPES);
            JSONObject loginInfo = new JSONObject(accessTokenInfo);
            loginInfo.append(Constants.USER_NAME, userName);
            loginInfo.append(Constants.APPLICATION_INFO, new JSONObject(apiApplicationKey));
            return Response.status(200).entity(loginInfo.toString()).build();
        } catch (Exception e) {
            //return Response.status(500).build();
        }
        return Response.status(200).build();
    }

    @POST
    @Path("/refresh")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Response refresh(@QueryParam("refreshToken") String refreshToken, @QueryParam("clientId") String clientId,
                            @QueryParam("clientSecret") String clientSecret) {
        try {
            TokenIssuerService tokenIssuerService = Feign.builder().client(disableHostnameVerification)
                    .requestInterceptor(new BasicAuthRequestInterceptor(clientId, clientSecret))
                    .contract(new JAXRSContract()).encoder(new JacksonEncoder()).decoder(new JacksonDecoder())
                    .target(TokenIssuerService.class, Constants.TOKEN_ENDPOINT);
            AccessTokenInfo accessTokenInfo = tokenIssuerService.getRefreshToken(Constants.REFRESH_GRANT_TYPE,
                    refreshToken);
            return Response.status(200).entity(new JSONObject(accessTokenInfo)).build();
        } catch (Exception e) {
            return Response.status(500).build();
        }
    }


    @POST
    @Path("/logout")
    @Override
    public Response logout(@QueryParam("token") String token, @QueryParam("clientId") String clientId,
                           @QueryParam("clientSecret") String clientSecret) {
        try {
            TokenRevokeService tokenRevokeService = Feign.builder().client(disableHostnameVerification)
                    .requestInterceptor(new BasicAuthRequestInterceptor(clientId, clientSecret))
                    .contract(new JAXRSContract()).encoder(new JacksonEncoder()).decoder(new JacksonDecoder())
                    .target(TokenRevokeService.class, Constants.TOKEN_ENDPOINT);
            tokenRevokeService.revoke(token);

            return Response.status(200).build();
        } catch (Exception e) {
            return Response.status(500).build();
        }
    }

    private SSLSocketFactory getTrustedSSLSocketFactory() {
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            return sc.getSocketFactory();
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            return null;
        }
    }
}
