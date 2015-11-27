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

package org.wso2.carbon.apimgt.webapp.publisher.config;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * This class will add, update custom permissions defined in resources.xml in webapps.
 */
public class APIResourceManager {

	private static APIResourceManager resourceManager;
	private List<APIResource> resourceList;

	private APIResourceManager(){};

	public static APIResourceManager getInstance() {
		if (resourceManager == null) {
			synchronized (APIResourceManager.class) {
				if (resourceManager == null) {
					resourceManager = new APIResourceManager();
				}
			}
		}
		return resourceManager;
	}

	public void initializeResources(InputStream resourceStream) throws APIResourceManagementException {
		try {
			if(resourceStream != null){
				/* Un-marshaling Device Management configuration */
				JAXBContext cdmContext = JAXBContext.newInstance(APIResourceConfiguration.class);
				Unmarshaller unmarshaller = cdmContext.createUnmarshaller();
				APIResourceConfiguration resourcesConfiguration = (APIResourceConfiguration)
						unmarshaller.unmarshal(resourceStream);
				if((resourcesConfiguration != null) && (resourcesConfiguration.getResources() != null)){
					this.resourceList = resourcesConfiguration.getResources();
				}
			}
		} catch (JAXBException e) {
			throw new APIResourceManagementException("Error occurred while initializing Data Source config", e);
		}
	}

	public List<APIResource> getAPIResources(){
		return resourceList;
	}
}
