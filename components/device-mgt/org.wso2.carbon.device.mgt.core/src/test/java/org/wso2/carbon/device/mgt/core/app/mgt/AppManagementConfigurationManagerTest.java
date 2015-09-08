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

package org.wso2.carbon.device.mgt.core.app.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.core.app.mgt.config.AppManagementConfig;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;

public class AppManagementConfigurationManagerTest {

	private static final Log log = LogFactory.getLog(AppManagementConfigurationManagerTest.class);
	private static final String MALFORMED_TEST_CONFIG_LOCATION_NO_ENABLED =
			"./src/test/resources/config/malformed-app-management-config-no-enabled.xml";
	private static final String MALFORMED_TEST_CONFIG_LOCATION_NO_CLIENT_KEY =
			"./src/test/resources/config/malformed-app-management-config-no-client-key-secret.xml";
	private static final String TEST_CONFIG_SCHEMA_LOCATION =
			"./src/test/resources/config/schema/app-mgt-config-schema.xsd";

	private Schema schema;

	@BeforeClass
	private void initSchema() {
		File deviceManagementSchemaConfig = new File(AppManagementConfigurationManagerTest.TEST_CONFIG_SCHEMA_LOCATION);
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		try {
			schema = factory.newSchema(deviceManagementSchemaConfig);
		} catch (SAXException e) {
			Assert.fail("Invalid schema found", e);
		}
	}

	@Test()
	public void testMandateEnabledElement() {
		File malformedConfig =
				new File(AppManagementConfigurationManagerTest.MALFORMED_TEST_CONFIG_LOCATION_NO_ENABLED);
		this.validateMalformedConfig(malformedConfig);
	}

	@Test()
	public void testMandateClientKeySecretElement() {
		File malformedConfig =
				new File(AppManagementConfigurationManagerTest.MALFORMED_TEST_CONFIG_LOCATION_NO_CLIENT_KEY);
		this.validateMalformedConfig(malformedConfig);
	}

	private void validateMalformedConfig(File malformedConfig) {
		try {
			JAXBContext ctx = JAXBContext.newInstance(AppManagementConfig.class);
			Unmarshaller um = ctx.createUnmarshaller();
			um.setSchema(this.getSchema());
			um.unmarshal(malformedConfig);
			Assert.assertTrue(false);
		} catch (JAXBException e) {
			Throwable linkedException = e.getLinkedException();
			if (!(linkedException instanceof SAXParseException)) {
				log.error("Unexpected error occurred while unmarshalling app management config", e);
				Assert.assertTrue(false);
			}
			log.error("JAXB parser occurred while unmarsharlling app management config", e);
			Assert.assertTrue(true);
		}
	}

	private Schema getSchema() {
		return schema;
	}
}
