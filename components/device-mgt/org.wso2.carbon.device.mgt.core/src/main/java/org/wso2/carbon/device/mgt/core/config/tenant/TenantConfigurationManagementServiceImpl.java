/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.core.config.tenant;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationManagementException;
import org.wso2.carbon.device.mgt.common.configuration.mgt.TenantConfiguration;
import org.wso2.carbon.device.mgt.common.configuration.mgt.TenantConfigurationManagementService;
import org.wso2.carbon.device.mgt.core.config.ConfigurationManagerConstants;
import org.wso2.carbon.device.mgt.core.config.util.ConfigurationManagerUtil;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.api.RegistryException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;

/**
 * This class implements all the functionality exposed as part of the TenantConfigurationManagementService. Main usage of
 * this module is, saving/retrieving tenant configurations to the registry.
 */
public class TenantConfigurationManagementServiceImpl
		implements TenantConfigurationManagementService {

	private static final Log log = LogFactory.getLog(TenantConfigurationManagementServiceImpl.class);

	@Override
	public boolean saveConfiguration(TenantConfiguration tenantConfiguration, String resourcePath)
			throws ConfigurationManagementException {
		boolean status;
		try {
			if (log.isDebugEnabled()) {
				log.debug("Persisting tenant configurations in Registry");
			}
			StringWriter writer = new StringWriter();
			JAXBContext context = JAXBContext.newInstance(TenantConfiguration.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.marshal(tenantConfiguration, writer);

			Resource resource = ConfigurationManagerUtil.getConfigurationRegistry().newResource();
			resource.setContent(writer.toString());
			resource.setMediaType(ConfigurationManagerConstants.ContentTypes.MEDIA_TYPE_XML);
			ConfigurationManagerUtil.putRegistryResource(resourcePath, resource);
			status = true;
		} catch (RegistryException e) {
			throw new ConfigurationManagementException(
					"Error occurred while persisting the Registry resource of Tenant Configuration : " + e.getMessage(), e);
		} catch (JAXBException e) {
			throw new ConfigurationManagementException(
					"Error occurred while parsing the Tenant configuration : " + e.getMessage(), e);
		}
		return status;
	}

	@Override
	public TenantConfiguration getConfiguration(String resourcePath)
			throws ConfigurationManagementException {
		Resource resource;
		try {
			resource = ConfigurationManagerUtil.getRegistryResource(resourcePath);
			if(resource != null){
				JAXBContext context = JAXBContext.newInstance(TenantConfiguration.class);
				Unmarshaller unmarshaller = context.createUnmarshaller();
				return (TenantConfiguration) unmarshaller.unmarshal(
						new StringReader(new String((byte[]) resource.getContent(), Charset
								.forName(ConfigurationManagerConstants.CharSets.CHARSET_UTF8))));
			}
			return new TenantConfiguration();
		} catch (JAXBException e) {
			throw new ConfigurationManagementException(
					"Error occurred while parsing the Tenant configuration : " + e.getMessage(), e);
		} catch (RegistryException e) {
			throw new ConfigurationManagementException(
					"Error occurred while retrieving the Registry resource of Tenant Configuration : " + e.getMessage(), e);
		}
	}
}
