/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.dynamic.client.web.proxy;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.dynamic.client.registration.profile.RegistrationProfile;
import org.wso2.carbon.dynamic.client.web.proxy.util.Constants;
import org.wso2.carbon.dynamic.client.web.proxy.util.DCRProxyUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * This class implements the proxy-endpoint for Dynamic-client-registration web service endpoints.
 */
public class RegistrationProxy {

    private static final Log log = LogFactory.getLog(RegistrationProxy.class);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(RegistrationProfile registrationProfile) {
        DefaultHttpClient httpClient = DCRProxyUtils.getHttpsClient();
        String host = DCRProxyUtils.getKeyManagerHost();
        Response response;
        try {
            URI uri = new URIBuilder().setScheme(Constants.RemoteServiceProperties.
                                                         DYNAMIC_CLIENT_SERVICE_PROTOCOL).setHost(host).setPath(
                    Constants.RemoteServiceProperties.DYNAMIC_CLIENT_SERVICE_ENDPOINT).build();
            Gson gson = new Gson();
            StringEntity entity = new StringEntity(gson.toJson(registrationProfile), MediaType.APPLICATION_JSON,
                                                   Constants.CharSets.CHARSET_UTF_8);
            HttpPost httpPost = new HttpPost(uri);
            httpPost.setEntity(entity);
            CloseableHttpResponse serverResponse =  httpClient.execute(httpPost);
            HttpEntity responseData = serverResponse.getEntity();
            int status = serverResponse.getStatusLine().getStatusCode();
            String resp = EntityUtils.toString(responseData, Constants.CharSets.CHARSET_UTF_8);
            response = Response.status(DCRProxyUtils.getResponseStatus(status)).entity(resp).build();
        } catch (URISyntaxException e) {
            String msg = "Server error occurred while registering client '" + registrationProfile.getClientName() + "'";
            log.error(msg, e);
            response = Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (UnsupportedEncodingException e) {
            String msg = "Request data encoding error occurred while registering client '" + registrationProfile.
                                                                                             getClientName() + "'";
            log.error(msg, e);
            response = Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).entity(msg).build();
        } catch (IOException e) {
            String msg = "Service invoke error occurred while registering client.";
            log.error(msg, e);
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } finally {
            httpClient.close();
        }
        return response;
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response unregister(@QueryParam("applicationName") String applicationName,
                               @QueryParam("userId") String userId,
                               @QueryParam("consumerKey") String consumerKey) {
        Response response;
        DefaultHttpClient httpClient = DCRProxyUtils.getHttpsClient();
        String host = DCRProxyUtils.getKeyManagerHost();
        try {
            URI uri = new URIBuilder().setScheme(Constants.RemoteServiceProperties.
                                                         DYNAMIC_CLIENT_SERVICE_PROTOCOL).setHost(host).setPath(
                    Constants.RemoteServiceProperties.DYNAMIC_CLIENT_SERVICE_ENDPOINT)
                                      .setParameter("applicationName", applicationName)
                                      .setParameter("userId", userId)
                                      .setParameter("consumerKey", consumerKey).build();
            HttpDelete httpDelete = new HttpDelete(uri);
            CloseableHttpResponse serverResponse =  httpClient.execute(httpDelete);
            HttpEntity responseData = serverResponse.getEntity();
            int status = serverResponse.getStatusLine().getStatusCode();
            String resp = EntityUtils.toString(responseData, Constants.CharSets.CHARSET_UTF_8);
            response = Response.status(DCRProxyUtils.getResponseStatus(status)).entity(resp).build();
        } catch (URISyntaxException e) {
            String msg = "Server error occurred while deleting the client '" + applicationName + "'";
            log.error(msg, e);
            response = Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (UnsupportedEncodingException e) {
            String msg = "Request data encoding error occurred while deleting the client '" + applicationName + "'";
            log.error(msg, e);
            response = Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).entity(msg).build();
        } catch (IOException e) {
            String msg = "Service invoke error occurred while deleting the client '" + applicationName + "'";
            log.error(msg, e);
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } finally {
            httpClient.close();
        }
        return response;
    }
}