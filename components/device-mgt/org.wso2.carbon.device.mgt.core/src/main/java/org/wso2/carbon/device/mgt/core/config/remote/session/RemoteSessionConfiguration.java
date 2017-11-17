/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.config.remote.session;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents the information related to Remote Session configuration.
 */
@XmlRootElement(name = "RemoteSessionConfiguration")
public class RemoteSessionConfiguration {

    private String remoteSessionServerUrl;
    private boolean enabled;
    private int maxHTTPConnectionPerHost;
    private int maxTotalHTTPConnections;
    private int maxMessagesPerSecond;
    private int sessionIdleTimeOut;
    private int maxSessionDuration;
    private int sessionBufferSize;

    public void setRemoteSessionServerUrl(String remoteSessionServerUrl) {
        this.remoteSessionServerUrl = remoteSessionServerUrl;
    }

    /**
     * Remote session server url
     * @return
     */
    @XmlElement(name = "RemoteSessionServerUrl", required = true)
    public String getRemoteSessionServerUrl() {
        return remoteSessionServerUrl;
    }

    /**
     * Remote session enabled
     * @return
     */
    @XmlElement(name = "Enabled", required = true)
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Maximum connections per host for external http invocations
     * @return
     */
    @XmlElement(name = "MaximumHTTPConnectionPerHost", required = true, defaultValue = "2")
    public int getMaxHTTPConnectionPerHost() {
        return maxHTTPConnectionPerHost;
    }

    public void setMaxHTTPConnectionPerHost(int maxHTTPConnectionPerHost) {
        this.maxHTTPConnectionPerHost = maxHTTPConnectionPerHost;
    }

    /**
     *  Maximum total connections  for external http invocation
     */
    @XmlElement(name = "MaximumTotalHTTPConnections", required = true, defaultValue = "100")
    public int getMaxTotalHTTPConnections() {
        return maxTotalHTTPConnections;
    }

    public void setMaxTotalHTTPConnections(int maxTotalHTTPConnections) {
        this.maxTotalHTTPConnections = maxTotalHTTPConnections;
    }

    /**
     * This is for protect device from message spamming. Throttling limit in term of messages  for device
     * @return
     */
    @XmlElement(name = "MaximumMessagesPerSecond", required = true, defaultValue = "10")
    public int getMaxMessagesPerSession() {
        return maxMessagesPerSecond;
    }

    public void setMaxMessagesPerSession(int maxMessagesPerSession) {
        this.maxMessagesPerSecond = maxMessagesPerSession;
    }

    /**
     * Maximum idle timeout in minutes
     * @return
     */
    @XmlElement(name = "SessionIdleTimeOut", required = true, defaultValue = "5")
    public int getSessionIdleTimeOut() {
        return sessionIdleTimeOut;
    }

    public void setSessionIdleTimeOut(int sessionIdleTimeOut) {
        this.sessionIdleTimeOut = sessionIdleTimeOut;
    }

    /**
     * Maximum session duration in minutes
     * @return
     */
    @XmlElement(name = "MaximumSessionDuration", required = true, defaultValue = "15")
    public int getMaxSessionDuration() {
        return maxSessionDuration;
    }

    public void setMaxSessionDuration(int maxSessionDuration) {
        this.maxSessionDuration = maxSessionDuration;
    }

    /**
     * Maximum session buffer size in kilo bytes
     * @return
     */
    @XmlElement(name = "SessionBufferSize", required = true, defaultValue = "640")
    public int getSessionBufferSize() {
        return sessionBufferSize;
    }

    public void setSessionBufferSize(int sessionBufferSize) {
        this.sessionBufferSize = sessionBufferSize;
    }
}


