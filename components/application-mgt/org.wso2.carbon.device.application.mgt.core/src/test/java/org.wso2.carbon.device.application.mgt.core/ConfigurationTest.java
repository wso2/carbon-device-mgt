/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.device.application.mgt.core;

import org.junit.BeforeClass;
import org.junit.Test;
import org.wso2.carbon.device.application.mgt.common.exception.InvalidConfigurationException;
import org.wso2.carbon.device.application.mgt.core.config.Configuration;
import org.wso2.carbon.device.application.mgt.core.config.ConfigurationManager;

import java.io.File;

public class ConfigurationTest {

    @BeforeClass
    public static void init() throws InvalidConfigurationException {
        File configPath = new File("src/test/resources/application-mgt.xml");
        ConfigurationManager.setConfigLocation(configPath.getAbsolutePath());
    }

    @Test
    public void validateConfiguration() {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        Configuration configuration = configurationManager.getConfiguration();
    }
}
