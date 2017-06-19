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

package org.wso2.carbon.device.mgt.analytics.data.publisher.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.carbon.device.mgt.analytics.data.publisher.DeviceDataPublisher;
import org.wso2.carbon.device.mgt.analytics.data.publisher.exception.DataPublisherConfigurationException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * This is the implementation of Osgi Service which can be used to publish and retireved
 * event/records.
 */
public class EventsPublisherServiceImpl implements EventsPublisherService {
	private static Log log = LogFactory.getLog(EventsPublisherServiceImpl.class);

	/**
	 * @param streamName           is the name of the stream that the data needs to pushed
	 * @param version              is the version of the stream
	 * @param metaDataArray        - meta data that needs to pushed
	 * @param correlationDataArray - correlation data that needs to be pushed
	 * @param payloadDataArray     - payload data that needs to be pushed
	 * @return if success returns true
	 * @throws DataPublisherConfigurationException
	 */
	@Override
	public boolean publishEvent(String streamName, String version, Object[] metaDataArray,
								Object[] correlationDataArray,
								Object[] payloadDataArray) throws DataPublisherConfigurationException {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            if (metaDataArray == null || metaDataArray.length == 0) {
                throw new DataPublisherConfigurationException("meta data[0] should have the device Id field");
            } else {
                metaDataArray[0] = tenantDomain + "@" + metaDataArray[0];
            }
        }

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
        try {
            DataPublisher dataPublisher = DeviceDataPublisher.getInstance().getDataPublisher();
            if (dataPublisher != null) {
                String streamId = DataBridgeCommonsUtils.generateStreamId(streamName, version);
                return dataPublisher.tryPublish(streamId, System.currentTimeMillis(), metaDataArray,
                                                correlationDataArray,
                                                payloadDataArray);
            } else {
                return false;
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
}