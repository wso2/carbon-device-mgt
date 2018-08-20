/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.mgt.analytics.data.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.device.mgt.analytics.data.publisher.config.AnalyticsConfiguration;
import org.wso2.carbon.device.mgt.analytics.data.publisher.exception.DataPublisherConfigurationException;
import org.wso2.carbon.device.mgt.analytics.data.publisher.service.EventsPublisherServiceImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * This is used to manage data publisher per tenant.
 */
public class DeviceDataPublisher {

    private static Log log = LogFactory.getLog(EventsPublisherServiceImpl.class);

    private Map<String, DataPublisher> dataPublishers;
    private static DeviceDataPublisher deviceDataPublisher;

    private DeviceDataPublisher() {
        dataPublishers = new HashMap<>();
    }

    public static DeviceDataPublisher getInstance() {
        if (deviceDataPublisher == null) {
            synchronized (DeviceDataPublisher.class) {
                if (deviceDataPublisher == null) {
                    deviceDataPublisher = new DeviceDataPublisher();
                }
            }
        }
        return deviceDataPublisher;
    }

    /**
     * This returns the data publisher for the tenant based on the analytics node id.
     *
     * @param analyticsConfig Analytics configurations
     * @param receiverURLSet Data receiver URL set as string
     * @return instance of data publisher
     * @throws DataPublisherConfigurationException on exception
     */
    public DataPublisher getDataPublisher(AnalyticsConfiguration analyticsConfig, String receiverURLSet)
            throws DataPublisherConfigurationException {
        synchronized (this) {
            if (this.dataPublishers.containsKey(receiverURLSet)) {
                return this.dataPublishers.get(receiverURLSet);
            } else {
                String analyticsServerUrlGroups = analyticsConfig.getReceiverServerUrl();
                String analyticsServerUsername = analyticsConfig.getAdminUsername();
                String analyticsServerPassword = analyticsConfig.getAdminPassword();

                try {
                    DataPublisher dataPublisher = new DataPublisher(receiverURLSet, analyticsServerUsername,
                            analyticsServerPassword);
                    this.dataPublishers.put(receiverURLSet, dataPublisher);
                    return dataPublisher;
                } catch (DataEndpointAgentConfigurationException e) {
                    String msg = "Configuration Exception on data publisher for " +
                            "ReceiverGroup = " + analyticsServerUrlGroups + " for username " + analyticsServerUsername;
                    log.error(msg, e);
                    throw new DataPublisherConfigurationException(msg, e);
                } catch (DataEndpointException e) {
                    String msg = "Invalid ReceiverGroup = " + analyticsServerUrlGroups;
                    log.error(msg, e);
                    throw new DataPublisherConfigurationException(msg, e);
                } catch (DataEndpointConfigurationException e) {
                    String msg = "Invalid Data endpoint configuration.";
                    log.error(msg, e);
                    throw new DataPublisherConfigurationException(msg, e);
                } catch (DataEndpointAuthenticationException e) {
                    String msg = "Authentication Failed for user " + analyticsServerUsername;
                    log.error(msg, e);
                    throw new DataPublisherConfigurationException(msg, e);
                } catch (TransportException e) {
                    String msg = "Error occurred while retrieving data publisher";
                    log.error(msg, e);
                    throw new DataPublisherConfigurationException(msg, e);
                }
            }
        }
    }

}
