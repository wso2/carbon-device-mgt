/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.analytics.wsproxy.outbound;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.analytics.wsproxy.exception.WSProxyException;

import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.net.URI;

/**
 * This class holds web socket client implementation
 *
 * @since 1.0.0
 */
@javax.websocket.ClientEndpoint
public class AnalyticsClient {

    private static final Log log = LogFactory.getLog(AnalyticsClient.class);

    private WebSocketContainer container;
    private Session analyticsSession = null;
    private Session clientSession;

    /**
     * Create {@link AnalyticsClient} instance.
     */
    public AnalyticsClient(Session clientSession) {
        container = ContainerProvider.getWebSocketContainer();
        this.clientSession = clientSession;
    }

    /**
     * Create web socket client connection using {@link WebSocketContainer}.
     */
    public void connectClient(URI endpointURI) throws WSProxyException {
        try {
            analyticsSession = container.connectToServer(this, endpointURI);
        } catch (DeploymentException | IOException e) {
            String msg = "Error occurred while connecting to remote endpoint " + endpointURI.toString();
            log.error(msg, e);
            throw new WSProxyException(msg, e);
        }
    }

    /**
     * Callback hook for Connection close events.
     *
     * @param userSession the analyticsSession which is getting closed.
     * @param reason      the reason for connection close
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        if (log.isDebugEnabled()) {
            log.debug("Closing web socket session: '" + userSession.getId() + "'. Code: " +
                    reason.getCloseCode().toString() + " Reason: " + reason.getReasonPhrase());
        }
        this.analyticsSession = null;
    }

    /**
     * Callback hook for Message Events.
     *
     * <p>This method will be invoked when a client send a message.
     *
     * @param message The text message.
     */
    @OnMessage
    public void onMessage(String message) {
        this.clientSession.getAsyncRemote().sendText(message);
    }

    /**
     * Send a message.
     *
     * @param message the message which is going to send.
     */
    public void sendMessage(String message) {
        this.analyticsSession.getAsyncRemote().sendText(message);
    }

    /**
     * Close current connection.
     */
    public void closeConnection(CloseReason closeReason) throws WSProxyException {
        if (this.analyticsSession != null) {
            try {
                this.analyticsSession.close(closeReason);
            } catch (IOException e) {
                String msg = "Error on closing WS connection.";
                log.error(msg, e);
                throw new WSProxyException(msg, e);
            }
        }
    }
}
