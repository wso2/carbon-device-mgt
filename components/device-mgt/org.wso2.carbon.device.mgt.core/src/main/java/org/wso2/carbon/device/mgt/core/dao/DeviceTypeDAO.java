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
	 * add a device type to the specific tenant id.
	 * @param deviceType
	 * @param deviceTypeProviderTenantId provider tenant id
	 * @param sharedWithAllTenants set to true if its visible to all tenants
	 * @return
	 * @throws DeviceManagementDAOException
	 */
	int addDeviceType(DeviceType deviceType, int deviceTypeProviderTenantId,
					  boolean sharedWithAllTenants) throws DeviceManagementDAOException;

	/**
	 * update device type details of a specfic tenants.
	 * @param deviceType
	 * @param deviceTypeProviderTenantId
	 * @throws DeviceManagementDAOException
	 */
	void updateDeviceType(DeviceType deviceType, int deviceTypeProviderTenantId)
			throws DeviceManagementDAOException;

	/**
	 * get device type detail of a specific tenant.
	 * @param tenantId
	 * @return
	 * @throws DeviceManagementDAOException
	 */
	List<DeviceType> getDeviceTypes(int tenantId) throws DeviceManagementDAOException;

	/**
	 * retrieve the device type with its id.
	 * @param id
	 * @return
	 * @throws DeviceManagementDAOException
	 */
	DeviceType getDeviceType(int id) throws DeviceManagementDAOException;

	/**
	 * retreive the device type with it name and tenant id.
	 * @param name
	 * @param tenantId
	 * @return
	 * @throws DeviceManagementDAOException
	 */
	DeviceType getDeviceType(String name,int tenantId) throws DeviceManagementDAOException;

	/**
	 * remove the device type from tenant.
	 * @param name
	 * @param tenantId
	 * @throws DeviceManagementDAOException
	 */
	void removeDeviceType(String name,int tenantId) throws DeviceManagementDAOException;

	/**
	 * share a specific device type between set of tenants.
	 *
	 * @param id
	 * @param tenantId
	 * @throws DeviceManagementDAOException
	 */
	void shareDeviceType(int id,int tenantId[]) throws DeviceManagementDAOException;

	/**
	 * get only the shared device types of the specific tenant.
	 *
	 * @param tenantId
	 * @return
	 * @throws DeviceManagementDAOException
	 */
	List<DeviceType> getSharedDeviceType(int tenantId) throws DeviceManagementDAOException;

	/**
	 * remove shared tenants for the device id.
	 *
	 * @param id
	 * @param tenantId
	 * @throws DeviceManagementDAOException
	 */
	void removeSharedDeviceType(int id,int tenantId[]) throws DeviceManagementDAOException;

	/**
	 * This method only returns the tenantIds that are shared specifically
	 * not supported for shared between all scenario.
	 *
	 * @param name
	 * @param providerTenantId
	 * @return
	 * @throws DeviceManagementDAOException
	 */
	List<Integer> getSharedTenantId(String name,int providerTenantId) throws DeviceManagementDAOException;


}
