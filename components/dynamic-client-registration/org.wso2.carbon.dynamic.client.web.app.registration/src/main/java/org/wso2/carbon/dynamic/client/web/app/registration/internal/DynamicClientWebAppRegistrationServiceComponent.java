/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.dynamic.client.web.app.registration.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.dynamic.client.registration.DynamicClientRegistrationService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="org.wso2.carbon.device.manager" immediate="true"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setRegistryService"
 * unbind="unsetRegistryService"
 * @scr.reference name="dynamic.client.service"
 * interface="org.wso2.carbon.dynamic.client.registration.DynamicClientRegistrationService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setDynamicClientService"
 * unbind="unsetDynamicClientService"
 */
public class DynamicClientWebAppRegistrationServiceComponent {

	private static Log log = LogFactory.getLog(DynamicClientWebAppRegistrationServiceComponent.class);

	@SuppressWarnings("unused")
	protected void activate(ComponentContext componentContext) {

	}

	@SuppressWarnings("unused")
	protected void deactivate(ComponentContext componentContext) {
		//do nothing
	}

	/**
	 * Sets Realm Service.
	 *
	 * @param realmService An instance of RealmService
	 */
	protected void setRealmService(RealmService realmService) {
		if (log.isDebugEnabled()) {
			log.debug("Setting Realm Service");
		}
		DynamicClientRegistrationDataHolder.getInstance().setRealmService(realmService);
	}

	/**
	 * Unsets Realm Service.
	 *
	 * @param realmService An instance of RealmService
	 */
	protected void unsetRealmService(RealmService realmService) {
		if (log.isDebugEnabled()) {
			log.debug("Unsetting Realm Service");
		}
		DynamicClientRegistrationDataHolder.getInstance().setRealmService(null);
	}

	/**
	 * Sets Registry Service.
	 *
	 * @param registryService An instance of RegistryService
	 */
	protected void setRegistryService(RegistryService registryService) {
		if (log.isDebugEnabled()) {
			log.debug("Setting Registry Service");
		}
		DynamicClientRegistrationDataHolder.getInstance().setRegistryService(registryService);
	}

	/**
	 * Unsets Registry Service.
	 *
	 * @param registryService An instance of RegistryService
	 */
	protected void unsetRegistryService(RegistryService registryService) {
		if (log.isDebugEnabled()) {
			log.debug("Un setting Registry Service");
		}
		DynamicClientRegistrationDataHolder.getInstance().setRegistryService(null);
	}

	/**
	 * Sets Dynamic Client Registration Service.
	 *
	 * @param dynamicClientRegistrationService An instance of DynamicClientRegistrationService
	 */
	protected void setDynamicClientService(DynamicClientRegistrationService dynamicClientRegistrationService) {
		if (log.isDebugEnabled()) {
			log.debug("Setting Dynamic Client Registration Service");
		}
		DynamicClientRegistrationDataHolder.getInstance().setDynamicClientRegistrationService(
				dynamicClientRegistrationService);
	}

	/**
	 * Unsets Dynamic Client Registration Service.
	 *
	 * @param dynamicClientRegistrationService An instance of DynamicClientRegistrationService
	 */
	protected void unsetDynamicClientService(DynamicClientRegistrationService dynamicClientRegistrationService) {
		if (log.isDebugEnabled()) {
			log.debug("Un setting Dynamic Client Registration Service");
		}
		DynamicClientRegistrationDataHolder.getInstance().setDynamicClientRegistrationService(null);
	}

}
