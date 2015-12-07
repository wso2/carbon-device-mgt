package org.wso2.carbon.device.mgt.analytics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.carbon.device.mgt.analytics.exception.DataPublisherAlreadyExistsException;
import org.wso2.carbon.device.mgt.analytics.exception.DataPublisherConfigurationException;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.analytics.AnalyticsConfigurations;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DeviceDataPublisher {
	private static final Log log = LogFactory.getLog(DeviceDataPublisher.class);
	private static Map<String, DataPublisher> dataPublisherMap;
	private static DeviceDataPublisher deviceDataPublisher;

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

	private DeviceDataPublisher() {
		dataPublisherMap = new ConcurrentHashMap<String, DataPublisher>();

	}

	private DataPublisher getDataPublisher() throws DataPublisherConfigurationException{

		String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

		//Get LoadBalancingDataPublisher which has been registered for the tenant.
		DataPublisher dataPublisher = getDataPublisher(tenantDomain);


		//If a LoadBalancingDataPublisher had not been registered for the tenant.
		if (dataPublisher == null) {
			AnalyticsConfigurations analyticsConfig = DeviceConfigurationManager.getInstance().getDeviceManagementConfig().
					getDeviceManagementConfigRepository().getAnalyticsConfigurations();

			if(!analyticsConfig.isEnable()) return null;

			String analyticsServerUrlGroups = analyticsConfig.getReceiverServerUrl();
			String analyticsServerUser = analyticsConfig.getAdminUsername();
			String analyticsServerPassword = analyticsConfig.getAdminPassword();


			//Create new DataPublisher for the tenant.
			try {
				dataPublisher = new DataPublisher(analyticsServerUrlGroups, analyticsServerUser,
												  analyticsServerPassword);
			} catch (Exception e) {
				String errorMsg = "Configuration Exception on data publisher for ReceiverGroup = " +
						analyticsServerUrlGroups + " for username " + analyticsServerUser;
				log.error(errorMsg);
				throw new DataPublisherConfigurationException(errorMsg, e);
			}

			try {
				//Add created DataPublisher.
				addDataPublisher(tenantDomain, dataPublisher);
			} catch (DataPublisherAlreadyExistsException e) {
				log.warn("Attempting to register a data publisher for the tenant " + tenantDomain +
								 " when one already exists. Returning existing data publisher");
				return getDataPublisher(tenantDomain);
			}
		}

		return dataPublisher;
	}

	/**
	 * Fetch the data publisher which has been registered under the tenant domain.
	 *
	 * @param tenantDomain - The tenant domain under which the data publisher is registered
	 * @return - Instance of the DataPublisher which was registered. Null if not registered.
	 */
	public  DataPublisher getDataPublisher(String tenantDomain) {
		if (dataPublisherMap.containsKey(tenantDomain)) {
			return dataPublisherMap.get(tenantDomain);
		}
		return null;
	}

	/**
	 * Adds a LoadBalancingDataPublisher to the data publisher map.
	 *
	 * @param tenantDomain  - The tenant domain under which the data publisher will be registered.
	 * @param dataPublisher - Instance of the LoadBalancingDataPublisher
	 * @throws DataPublisherAlreadyExistsException -
	 * If a data publisher has already been registered under the
	 *                                                                                         tenant domain
	 */
	public void addDataPublisher(String tenantDomain,
										DataPublisher dataPublisher)
			throws DataPublisherAlreadyExistsException {
		if (dataPublisherMap.containsKey(tenantDomain)) {
			throw new DataPublisherAlreadyExistsException(
					"A DataPublisher has already been created for the tenant " +
							tenantDomain);
		}

		dataPublisherMap.put(tenantDomain, dataPublisher);
	}

	public boolean publishEvent(String streamName, String version, Object[] metaDataArray,
			Object[] correlationDataArray, Object[] payloadDataArray) throws DataPublisherConfigurationException {


		DataPublisher dataPublisher = getDataPublisher();
		if (dataPublisher != null) {
			String streamId = DataBridgeCommonsUtils.generateStreamId(streamName, version);
			dataPublisher.tryPublish(streamId, System.currentTimeMillis(),metaDataArray, correlationDataArray,
									 payloadDataArray);

		} else {
			return false;
		}

		return true;
	}

}
