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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.dynamic.client.web.proxy.util.Constants;
import org.wso2.carbon.dynamic.client.web.proxy.util.DCRProxyUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

public class OAuthEndpointProxy {

    private static final Log log = LogFactory.getLog(OAuthEndpointProxy.class);

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public Response issueAccessToken(MultivaluedMap<String, String> paramMap) {
        DefaultHttpClient httpClient = DCRProxyUtils.getHttpsClient();
        String host = DCRProxyUtils.getKeyManagerHost();
        Response response;
        try {
            URI uri = new URIBuilder().setScheme(Constants.RemoteServiceProperties.
                                                         DYNAMIC_CLIENT_SERVICE_PROTOCOL).setHost(host).setPath(
                    Constants.RemoteServiceProperties.OAUTH2_TOKEN_ENDPOINT).build();
            HttpHost httpHost = new HttpHost(uri.toString());
            CloseableHttpResponse serverResponse =  httpClient.execute(httpHost, null);
            HttpEntity responseData = serverResponse.getEntity();
            int status = serverResponse.getStatusLine().getStatusCode();
            String resp = EntityUtils.toString(responseData, Constants.CharSets.CHARSET_UTF_8);
            response = Response.status(DCRProxyUtils.getResponseStatus(status)).entity(resp).build();
        } catch (URISyntaxException | IOException e) {
            String msg = "Service invoke error occurred while registering client";
            log.error(msg, e);
            response = Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } finally {
            httpClient.close();
        }
        return response;
    }
}
