/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.device.mgt.analytics.wsproxy.inbound;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.device.mgt.analytics.data.publisher.DataPublisherUtil;
import org.wso2.carbon.device.mgt.analytics.data.publisher.config.AnalyticsConfiguration;
import org.wso2.carbon.device.mgt.analytics.wsproxy.exception.WSProxyException;
import org.wso2.carbon.device.mgt.analytics.wsproxy.outbound.AnalyticsClient;

import javax.websocket.CloseReason;
import javax.websocket.Session;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interface for subscription and un-subscription for web socket
 */

public class SubscriptionEndpoint {

    private static final Log log = LogFactory.getLog(SubscriptionEndpoint.class);
    private Map<String, List<AnalyticsClient>> analyticsClientsMap = new HashMap<>();

    /**
     * Web socket onOpen - When client sends a message
     *
     * @param session         - Users registered session.
     */
    public void onOpen(Session session) {
        if (log.isDebugEnabled()) {
            log.debug("WebSocket opened, for Session id: " + session.getId());
        }

        AnalyticsConfiguration analyticsConfig = AnalyticsConfiguration.getInstance();
        ArrayList<String> publisherGroups =
                DataPublisherUtil.getEndpointGroups(analyticsConfig.getAnalyticsPublisherUrl());
        List<AnalyticsClient> analyticsClients = new ArrayList<>();
        for (String publisherURLGroup : publisherGroups) {
            try {
                String[] endpoints = DataPublisherUtil.getEndpoints(publisherURLGroup);
                for (String endpoint : endpoints) {
                    try {
                        endpoint = endpoint.trim();
                        if (!endpoint.endsWith("/")) {
                            endpoint += "/";
                        }
                        endpoint += session.getRequestURI().getSchemeSpecificPart().replace("secured-websocket-proxy","");
                        AnalyticsClient analyticsClient = new AnalyticsClient(session);
                        analyticsClient.connectClient(new URI(endpoint));
                        analyticsClients.add(analyticsClient);
                    } catch (URISyntaxException e) {
                        log.error("Unable to create URL from: " + endpoint, e);
                    } catch (WSProxyException e) {
                        log.error("Unable to create WS client for: " + endpoint, e);
                    }
                }
            } catch (DataEndpointConfigurationException e) {
                log.error("Unable to obtain endpoints from receiverURLGroup: " + publisherURLGroup, e);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Configured " + analyticsClients.size() + " analytics clients for Session id: " +
                    session.getId());
        }
        analyticsClientsMap.put(session.getId(), analyticsClients);
    }

    /**
     * Web socket onClose - Remove the registered sessions
     *
     * @param session      - Users registered session.
     * @param reason       - Status code for web-socket close.
     * @param streamName   - StreamName extracted from the ws url.
     * @param version      - Version extracted from the ws url.
     * @param tenantDomain - Domain of the tenant.
     */
    public void onClose(Session session, CloseReason reason, String streamName, String version, String tenantDomain) {
        if (log.isDebugEnabled()) {
            log.debug("Closing a WebSocket due to " + reason.getReasonPhrase() + ", for session ID:" +
                    session.getId() + ", for request URI - " + session.getRequestURI());
        }
        for (AnalyticsClient analyticsClient : analyticsClientsMap.get(session.getId())) {
            if (analyticsClient != null) {
                try {
                    analyticsClient.closeConnection(reason);
                } catch (WSProxyException e) {
                    log.error("Error occurred while closing ws connection due to " + reason.getReasonPhrase() +
                            ", for session ID:" + session.getId() + ", for request URI - " + session.getRequestURI(), e);
                }
            }
        }
        analyticsClientsMap.remove(session.getId());
    }

    /**
     * Web socket onMessage - When client sens a message
     *
     * @param session - Users registered session.
     * @param message - Status code for web-socket close.
     */
    public void onMessage(Session session, String message) {
        for (AnalyticsClient analyticsClient : analyticsClientsMap.get(session.getId())) {
            if (analyticsClient != null) {
                analyticsClient.sendMessage(message);
            }
        }
    }

    /**
     * Web socket onError
     *
     * @param session      - Users registered session.
     * @param throwable    - Status code for web-socket close.
     * @param streamName   - StreamName extracted from the ws url.
     * @param version      - Version extracted from the ws url.
     * @param tenantDomain - Domain of the tenant.
     */
    public void onError(Session session, Throwable throwable, String streamName, String version, String tenantDomain) {
        log.error("Error occurred in session ID: " + session.getId() + ", for request URI - " +
                session.getRequestURI() + ", " + throwable.getMessage(), throwable);
    }

}
