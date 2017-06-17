package org.wso2.carbon.device.mgt.extensions.push.notification.provider.http;

import com.google.gson.Gson;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.InvalidConfigurationException;
import org.wso2.carbon.device.mgt.common.push.notification.NotificationContext;

import java.net.UnknownHostException;

public class HTTPMessageExecutor implements Runnable {

    private String url;
    private String authorizationHeader;
    private String payload;
    private HostConfiguration hostConfiguration;
    private HttpClient httpClient;
    private static final String APPLIATION_JSON = "application/json";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final Log log = LogFactory.getLog(HTTPMessageExecutor.class);

    public HTTPMessageExecutor(NotificationContext notificationContext, String authorizationHeader, String url
            , HostConfiguration hostConfiguration, HttpClient httpClient) {
        this.url = url;
        this.authorizationHeader = authorizationHeader;
        Gson gson = new Gson();
        this.payload = gson.toJson(notificationContext);
        this.hostConfiguration = hostConfiguration;
        this.httpClient = httpClient;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAuthorizationHeader() {
        return authorizationHeader;
    }

    public void setAuthorizationHeader(String authorizationHeader) {
        this.authorizationHeader = authorizationHeader;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public void run() {
        EntityEnclosingMethod method = null;

        try {
            method = new PostMethod(this.getUrl());
            method.setRequestEntity(new StringRequestEntity(this.getPayload(), APPLIATION_JSON, "UTF-8"));
            if (authorizationHeader != null && authorizationHeader.isEmpty()) {
                method.setRequestHeader(AUTHORIZATION_HEADER, authorizationHeader);
            }

            this.getHttpClient().executeMethod(hostConfiguration, method);

        } catch (UnknownHostException e) {
            log.error("Push Notification message dropped " + url, e);
            throw new InvalidConfigurationException("invalid host: url", e);
        } catch (Throwable e) {
            log.error("Push Notification message dropped ", e);
            throw new InvalidConfigurationException("Push Notification message dropped, " + e.getMessage(), e);
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
    }
}
