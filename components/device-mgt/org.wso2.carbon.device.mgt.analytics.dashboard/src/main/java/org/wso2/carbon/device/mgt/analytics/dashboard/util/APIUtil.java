package org.wso2.carbon.device.mgt.analytics.dashboard.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;

import java.net.SocketException;


/**
 * This class provides utility functions used by REST-API.
 */
public class APIUtil {

	private static Log log = LogFactory.getLog(APIUtil.class);

	public static String getAuthenticatedUser() {
		PrivilegedCarbonContext threadLocalCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
		String username = threadLocalCarbonContext.getUsername();
		String tenantDomain = threadLocalCarbonContext.getTenantDomain();
		return username + "@" + tenantDomain;
	}

	public static String getAuthenticatedUserTenantDomain() {
		PrivilegedCarbonContext threadLocalCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
		return threadLocalCarbonContext.getTenantDomain();
	}

	public static DeviceManagementProviderService getDeviceManagementService() {
		PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
		DeviceManagementProviderService deviceManagementProviderService =
				(DeviceManagementProviderService) ctx.getOSGiService(DeviceManagementProviderService.class, null);
		if (deviceManagementProviderService == null) {
			String msg = "Device Management service has not initialized.";
			log.error(msg);
			throw new IllegalStateException(msg);
		}
		return deviceManagementProviderService;
	}

	public static String getServerUrl() {
		try {
			return org.apache.axis2.util.Utils.getIpAddress();
		} catch (SocketException e) {
			log.warn("Failed retrieving the hostname, therefore set to localhost", e);
            return "localhost";
		}
	}
}
