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

package org.wso2.carbon.dynamic.client.web.proxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.dynamic.client.registration.DynamicClientRegistrationException;
import org.wso2.carbon.dynamic.client.registration.profile.RegistrationProfile;
import org.wso2.carbon.dynamic.client.web.proxy.util.Constants;
import org.wso2.carbon.dynamic.client.web.proxy.util.DCRProxyUtils;
import org.wso2.carbon.dynamic.client.web.proxy.util.RemoteDCRClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Created by harshan on 12/10/15.
 */

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RegistrationProxy {

    private static final Log log = LogFactory.getLog(RegistrationProxy.class);

    @POST
    public Response register(RegistrationProfile profile) {
        Response response;
        try {
            CloseableHttpResponse serverResponse = RemoteDCRClient.createOAuthApplication(profile);
            HttpEntity responseData = serverResponse.getEntity();
            int status = serverResponse.getStatusLine().getStatusCode();
            String resp = EntityUtils.toString(responseData, Constants.CharSets.CHARSET_UTF8);
            response = Response.status(DCRProxyUtils.getResponseStatus(status)).entity(resp).build();
        } catch (DynamicClientRegistrationException e) {
            String msg = "Server error occurred while registering client '" + profile.getClientName() + "'";
            log.error(msg, e);
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (IOException e) {
            String msg = "Service invoke error occurred while registering client '" + profile.getClientName() + "'";
            log.error(msg, e);
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        return response;
    }

    @DELETE
    public Response unregister(@QueryParam("applicationName") String applicationName,
                               @QueryParam("userId") String userId,
                               @QueryParam("consumerKey") String consumerKey) {
        Response response;
        try {
            CloseableHttpResponse serverResponse = RemoteDCRClient.deleteOAuthApplication(userId, applicationName,
                                                                                          consumerKey);
            HttpEntity responseData = serverResponse.getEntity();
            int status = serverResponse.getStatusLine().getStatusCode();
            String resp = EntityUtils.toString(responseData, Constants.CharSets.CHARSET_UTF8);
            response = Response.status(DCRProxyUtils.getResponseStatus(status)).entity(resp).build();
        } catch (DynamicClientRegistrationException e) {
            String msg = "Server error occurred while deleting the client '" + applicationName + "'";
            log.error(msg, e);
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (IOException e) {
            String msg = "Service invoke error occurred while deleting the client '" + applicationName + "'";
            log.error(msg, e);
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        return response;
    }
}