/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.core.dao;

import org.wso2.carbon.device.mgt.core.dto.DeviceType;

import java.util.List;

/**
 * This class represents the key operations associated with persisting device type related
 * information.
 */
public interface DeviceTypeDAO {

	/**
	 * @param deviceType       device that needs to be added
	 * @param providerTenantId provider tenant id whom the device type is associated with.
	 * @param isSharedWithAllTenants is this a shared device type or not.
	 * @throws DeviceManagementDAOException
	 */
	void addDeviceType(DeviceType deviceType, int providerTenantId, boolean isSharedWithAllTenants)
			throws DeviceManagementDAOException;

	/**
	 * @param deviceType       deviceType that needs to be updated.
	 * @param providerTenantId provider tenant id whom the device type is associated with.
	 * @throws DeviceManagementDAOException
	 */
	void updateDeviceType(DeviceType deviceType, int providerTenantId) throws DeviceManagementDAOException;

	/**
	 * @param tenantId get device type detail of a specific tenant.
	 * @return list of all device types that are associated with the tenant this includes the shared device types.
	 * @throws DeviceManagementDAOException
	 */
	List<DeviceType> getDeviceTypes(int tenantId) throws DeviceManagementDAOException;

	/**
	 * @param tenantId of the device type provider.
	 * @return return only the device types that are associated with the provider tenant.
	 * @throws DeviceManagementDAOException
	 */
	List<String> getDeviceTypesByProvider(int tenantId) throws DeviceManagementDAOException;

	/**
	 * @return sharedWithAllDeviceTypes This returns public shared device types.
	 * @throws DeviceManagementDAOException
	 */
	List<String> getSharedDeviceTypes() throws DeviceManagementDAOException;

	/**
	 * @param id retrieve the device type with its id.
	 * @return the device type associated with the id.
	 * @throws DeviceManagementDAOException
	 */
	DeviceType getDeviceType(int id) throws DeviceManagementDAOException;

	/**
	 * @param name     retreive the device type with it name.
	 * @param tenantId retreive the device type with its tenant id.
	 * @return the device type associated with its name and tenant id.
	 * @throws DeviceManagementDAOException
	 */
	DeviceType getDeviceType(String name, int tenantId) throws DeviceManagementDAOException;

	/**
	 * remove the device type from tenant.
	 *
	 * @param name     remove the device type with it name.
	 * @param tenantId remove the device type with its tenant id.
	 * @throws DeviceManagementDAOException
	 */
	void removeDeviceType(String name, int tenantId) throws DeviceManagementDAOException;

}
