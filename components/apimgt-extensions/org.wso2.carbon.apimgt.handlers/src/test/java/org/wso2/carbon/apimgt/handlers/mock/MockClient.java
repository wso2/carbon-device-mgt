/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.apimgt.handlers.mock;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Mock implementation for CloseableHttpClient to be used in test cases.
 */
public class MockClient extends CloseableHttpClient {
    private List<CloseableHttpResponse> responses = new ArrayList<>();
    private int responseCount = 0;

    @Override
    protected CloseableHttpResponse doExecute(HttpHost httpHost, HttpRequest httpRequest, HttpContext httpContext)
            throws IOException {
        if (this.responseCount < this.responses.size()) {
            this.responseCount++;
            return this.responses.get(this.responseCount - 1);
        } else {
            return new MockHttpResponse();
        }
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public HttpParams getParams() {
        return null;
    }

    @Override
    public ClientConnectionManager getConnectionManager() {
        return null;
    }

    public void setResponse(CloseableHttpResponse reponse) {
        this.responses.add(reponse);
    }

    public void reset() {
        this.responses.clear();
        this.responseCount = 0;
    }
}
