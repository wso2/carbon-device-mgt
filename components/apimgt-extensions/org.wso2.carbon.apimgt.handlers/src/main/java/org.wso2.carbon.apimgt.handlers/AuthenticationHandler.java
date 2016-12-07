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

import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.namespace.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.util.Base64;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.handlers.invoker.RESTInvoker;
import org.wso2.carbon.apimgt.handlers.invoker.RESTResponse;
import org.wso2.carbon.apimgt.handlers.utils.AuthConstants;
import org.wso2.carbon.apimgt.handlers.utils.CoreUtils;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthenticationHandler implements Handler {
    private static final Log log = LogFactory.getLog(AuthenticationHandler.class);
    private static HandlerDescription EMPTY_HANDLER_METADATA = new HandlerDescription("API Security Handler");
    private HandlerDescription handlerDesc;
    private ArrayList<String> apiList;
    private RESTInvoker restInvoker;

    /**
     * Setting up configurations at the constructor
     */
    public AuthenticationHandler() {
        log.info("Engaging API Security Handler");
        apiList = CoreUtils.readApiFilterList();
        restInvoker = new RESTInvoker();
        this.handlerDesc = EMPTY_HANDLER_METADATA;
    }

    /**
     * Handles incoming http/s requests
     *
     * @param messageContext
     * @return response
     * @throws AxisFault
     */
    public InvocationResponse invoke(MessageContext messageContext) throws AxisFault {
        boolean validateRequest = messageContext.getTo() != null;

        if (validateRequest && isSecuredAPI(messageContext)) {
            String ctxPath = messageContext.getTo().getAddress().trim();
            CoreUtils.debugLog(log, "Authentication handler invoked by: ", ctxPath);
            Map<?, ?> headers = (Map<?, ?>) messageContext.getProperty(MessageContext.TRANSPORT_HEADERS);

            if (headers.containsKey(AuthConstants.MDM_SIGNATURE)) {
                String mdmSignature = headers.get(AuthConstants.MDM_SIGNATURE).toString();

                try {
                    CoreUtils.debugLog(log, "Verify Cert:\n", mdmSignature);

                    URI dcrUrl = new URI(AuthConstants.HTTPS + "://" + CoreUtils.getHost() + ":" + CoreUtils
                            .getHttpsPort() + "/dynamic-client-web/register");
                    String dcrContent = "{\n" +
                            "\"owner\":\"" + CoreUtils.getUsername() + "\",\n" +
                            "\"clientName\":\"emm\",\n" +
                            "\"grantType\":\"refresh_token password client_credentials\",\n" +
                            "\"tokenScope\":\"default\"\n" +
                            "}";
                    Map<String, String> drcHeaders = new HashMap<String, String>();
                    drcHeaders.put("Content-Type", "application/json");

                    RESTResponse response = restInvoker.invokePOST(dcrUrl, drcHeaders, null,
                            null, dcrContent);
                    CoreUtils.debugLog(log, "DCR response:", response.getContent());
                    JSONObject jsonResponse = new JSONObject(response.getContent());
                    String clientId = jsonResponse.getString("client_id");
                    String clientSecret = jsonResponse.getString("client_secret");

                    URI tokenUrl = new URI(AuthConstants.HTTPS + "://" + CoreUtils.getHost() + ":" + CoreUtils
                            .getHttpsPort() + "/oauth2/token");
                    String tokenContent = "grant_type=password&username=" + CoreUtils.getUsername() + "&password=" +
                            CoreUtils.getPassword() + "&scope=activity-view";
                    String tokenBasicAuth = "Basic " + Base64.encode((clientId + ":" + clientSecret).getBytes());
                    Map<String, String> tokenHeaders = new HashMap<String, String>();
                    tokenHeaders.put("Authorization", tokenBasicAuth);
                    tokenHeaders.put("Content-Type", "application/x-www-form-urlencoded");

                    response = restInvoker.invokePOST(tokenUrl, tokenHeaders, null,
                            null, tokenContent);
                    CoreUtils.debugLog(log, "Token response:", response.getContent());
                    jsonResponse = new JSONObject(response.getContent());
                    String accessToken = jsonResponse.getString("access_token");

                    URI certVerifyUrl = new URI(AuthConstants.HTTPS + "://" + CoreUtils.getHost() + ":" + CoreUtils
                            .getHttpsPort() + "/api/certificate-mgt/v1.0/admin/certificates/verify/ios");
                    Map<String, String> certVerifyHeaders = new HashMap<String, String>();
                    certVerifyHeaders.put("Authorization", "Bearer " + accessToken);
                    certVerifyHeaders.put("Content-Type", "application/json");
                    String certVerifyContent = "{\n" +
                            "\"pem\":\"" + mdmSignature + "\",\n" +
                            "\"tenantId\": \"-1234\",\n" +
                            "\"serial\":\"\"\n" +
                            "}";

                    response = restInvoker.invokePOST(certVerifyUrl, certVerifyHeaders, null,
                            null, certVerifyContent);
                    CoreUtils.debugLog(log, "Verify response:", response.getContent());

                    if (!response.getContent().contains("invalid")) {
                        return InvocationResponse.CONTINUE;
                    }
                    log.warn("Unauthorized request for api: " + ctxPath);
                    setFaultCodeAndThrowAxisFault(messageContext, new Exception("Unauthorized!"));
                    return InvocationResponse.SUSPEND;

                } catch (Exception e) {
                    log.error("Error while processing certificate.", e);
                    setFaultCodeAndThrowAxisFault(messageContext, e);
                    return InvocationResponse.SUSPEND;
                }
            } else {
                log.warn("Unauthorized request for api: " + ctxPath);
                setFaultCodeAndThrowAxisFault(messageContext, new Exception("SSL required"));
                return InvocationResponse.SUSPEND;
            }
        } else {
            return InvocationResponse.CONTINUE;
        }

    }

    /**
     * API filter
     *
     * @param messageContext
     * @return boolean
     */
    private boolean isSecuredAPI(MessageContext messageContext) {
        if (messageContext.getTransportIn() != null &&
                messageContext.getTransportIn().getName().toLowerCase().equals(AuthConstants.HTTPS)) {
            for (String path : apiList) {
                if (messageContext.getTo().getAddress().trim().contains(path)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void setFaultCodeAndThrowAxisFault(MessageContext msgContext, Exception e) throws AxisFault {

        msgContext.setProperty(AuthConstants.SEC_FAULT, Boolean.TRUE);
        String soapVersionURI = msgContext.getEnvelope().getNamespace().getNamespaceURI();
        QName faultCode = null;
        /*
         * Get the faultCode from the thrown WSSecurity exception, if there is one
         */
        if (e instanceof WSSecurityException) {
            faultCode = ((WSSecurityException) e).getFaultCode();
        }
        /*
         * Otherwise default to InvalidSecurity
         */
        if (faultCode == null) {
            faultCode = new QName(WSConstants.INVALID_SECURITY.getNamespaceURI(),
                    WSConstants.INVALID_SECURITY.getLocalPart(), AuthConstants.WSSE);
        }

        if (soapVersionURI.equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {

            throw new AxisFault(faultCode, e.getMessage(), e);

        } else if (soapVersionURI.equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {

            List subfaultCodes = new ArrayList();
            subfaultCodes.add(faultCode);
            throw new AxisFault(Constants.FAULT_SOAP12_SENDER, subfaultCodes, e.getMessage(), e);

        }

    }

    public void cleanup() {
    }

    public void init(HandlerDescription handlerDescription) {
        this.handlerDesc = handlerDescription;
    }

    public void flowComplete(MessageContext messageContext) {
    }

    public HandlerDescription getHandlerDesc() {
        return this.handlerDesc;
    }

    public String getName() {
        return "API security inflow handler";
    }

    public Parameter getParameter(String name) {
        return this.handlerDesc.getParameter(name);
    }
}
