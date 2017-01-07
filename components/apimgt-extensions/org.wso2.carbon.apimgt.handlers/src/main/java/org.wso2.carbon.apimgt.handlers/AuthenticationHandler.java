/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.carbon.apimgt.handlers;

import com.google.gson.Gson;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.wso2.carbon.apimgt.handlers.beans.Certificate;
import org.wso2.carbon.apimgt.handlers.beans.ValidationResponce;
import org.wso2.carbon.apimgt.handlers.config.IOTServerConfiguration;
import org.wso2.carbon.apimgt.handlers.invoker.RESTInvoker;
import org.wso2.carbon.apimgt.handlers.invoker.RESTResponse;
import org.wso2.carbon.apimgt.handlers.utils.AuthConstants;
import org.wso2.carbon.apimgt.handlers.utils.Utils;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class AuthenticationHandler extends AbstractHandler {
    private static final Log log = LogFactory.getLog(AuthenticationHandler.class);
    private static HandlerDescription EMPTY_HANDLER_METADATA = new HandlerDescription("API Security Handler");
    private HandlerDescription handlerDesc;
    private RESTInvoker restInvoker;

    private IOTServerConfiguration iotServerConfiguration;

    /**
     * Setting up configurations at the constructor
     */
    public AuthenticationHandler() {
        log.info("Engaging API Security Handler..........");
        restInvoker = new RESTInvoker();
        this.handlerDesc = EMPTY_HANDLER_METADATA;
        this.iotServerConfiguration = Utils.initConfig();
    }

    @Override
    public boolean handleRequest(org.apache.synapse.MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axisMC = ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        String ctxPath = messageContext.getTo().getAddress().trim();

        if (log.isDebugEnabled()) {
            log.debug("Authentication handler invoked by: " + ctxPath);
        }
        Map<String, String> headers = (Map<String, String>) axisMC.getProperty(MessageContext.TRANSPORT_HEADERS);
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            RESTResponse response;
            if (headers.containsKey(AuthConstants.MDM_SIGNATURE)) {

                String mdmSignature = headers.get(AuthConstants.MDM_SIGNATURE).toString();
                if (log.isDebugEnabled()) {
                    log.debug("Verify Cert:\n" + mdmSignature);
                }
                String accessToken = Utils.getAccessToken(iotServerConfiguration);

                String deviceType = this.getDeviceType(messageContext.getTo().getAddress().trim());
                URI certVerifyUrl = new URI(iotServerConfiguration.getVerificationEndpoint() + deviceType);

                Map<String, String> certVerifyHeaders = new HashMap<>();
                certVerifyHeaders.put("Authorization", "Bearer " + accessToken);
                certVerifyHeaders.put("Content-Type", "application/json");

                Certificate certificate = new Certificate();
                certificate.setPem(mdmSignature);
                certificate.setTenantId(tenantId);
                certificate.setSerial("");

                Gson gson = new Gson();
                String certVerifyContent = gson.toJson(certificate);
                response = restInvoker.invokePOST(certVerifyUrl, certVerifyHeaders, null,
                        null, certVerifyContent);

                String str = response.getContent();
                if (str.contains("JWTToken")) {
                    ValidationResponce validationResponce = gson.fromJson(str, ValidationResponce.class);
                    // TODO: send the JWT token with user details.
                    // headers.put("X-JWT-Assertion", validationResponce.getJWTToken());
                }
                if (log.isDebugEnabled()) {
                    log.debug("Verify response:" + response.getContent());
                    log.debug("Response String : " + str);
                }

            } else if (headers.containsKey(AuthConstants.PROXY_MUTUAL_AUTH_HEADER)) {
                String subjectDN = headers.get(AuthConstants.PROXY_MUTUAL_AUTH_HEADER).toString();

                if (log.isDebugEnabled()) {
                    log.debug("Verify subject DN: " + subjectDN);
                }
                String accessToken = Utils.getAccessToken(iotServerConfiguration);
                String deviceType = this.getDeviceType(messageContext.getTo().getAddress().trim());
                URI certVerifyUrl = new URI(iotServerConfiguration.getVerificationEndpoint() + deviceType);
                Map<String, String> certVerifyHeaders = new HashMap<>();
                certVerifyHeaders.put("Authorization", "Bearer " + accessToken);
                certVerifyHeaders.put("Content-Type", "application/json");
                Certificate certificate = new Certificate();
                certificate.setPem(subjectDN);
                certificate.setTenantId(tenantId);
                certificate.setSerial(AuthConstants.PROXY_MUTUAL_AUTH_HEADER);

                Gson gson = new Gson();
                String certVerifyContent = gson.toJson(certificate);
                response = restInvoker.invokePOST(certVerifyUrl, certVerifyHeaders, null,
                        null, certVerifyContent);
                if (log.isDebugEnabled()) {
                    log.debug("Verify response:" + response.getContent());
                }
            } else if (headers.containsKey(AuthConstants.ENCODED_PEM)) {
                String encodedPem = headers.get(AuthConstants.ENCODED_PEM).toString();
                if (log.isDebugEnabled()) {
                    log.debug("Verify Cert:\n" + encodedPem);
                }
                String accessToken = Utils.getAccessToken(iotServerConfiguration);
                URI certVerifyUrl = new URI(iotServerConfiguration.getVerificationEndpoint() + "android");
                Map<String, String> certVerifyHeaders = new HashMap<>();
                certVerifyHeaders.put("Authorization", "Bearer " + accessToken);
                certVerifyHeaders.put("Content-Type", "application/json");

                Certificate certificate = new Certificate();
                certificate.setPem(encodedPem);
                certificate.setTenantId(tenantId);
                certificate.setSerial("");
                Gson gson = new Gson();
                String certVerifyContent = gson.toJson(certificate);
                response = restInvoker.invokePOST(certVerifyUrl, certVerifyHeaders, null,
                        null, certVerifyContent);
                if (log.isDebugEnabled()) {
                    log.debug("Verify response:" + response.getContent());
                }
            } else {
                log.warn("Unauthorized request for api: " + ctxPath);
                return false;
            }
            if (response != null && !response.getContent().contains("invalid")) {
                return true;
            }
            log.warn("Unauthorized request for api: " + ctxPath);
            return false;
        } catch (IOException e) {
            log.error("Error while processing certificate.", e);
            return false;
        } catch (URISyntaxException e) {
            log.error("Error while processing certificate.", e);
            return false;
        } catch (APIMCertificateMGTException e) {
            log.error("Error while processing certificate.", e);
            return false;
        }

    }

    @Override
    public boolean handleResponse(org.apache.synapse.MessageContext messageContext) {
        return true;
    }


    // TODO : take this from the url.
    private String getDeviceType(String url) {
        if (url.contains("ios")) {
            return "ios";
        } else if (url.contains("android")) {
            return "android";
        } else return null;

    }
}
