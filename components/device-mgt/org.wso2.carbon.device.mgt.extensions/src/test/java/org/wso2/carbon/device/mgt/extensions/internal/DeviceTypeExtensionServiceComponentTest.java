/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.extensions.internal;

import org.apache.sling.testing.mock.osgi.MockOsgi;
import org.testng.annotations.Test;

/**
 * This is a test case for {@link DeviceTypeExtensionServiceComponent}.
 */
public class DeviceTypeExtensionServiceComponentTest {
    @Test(description = "This test case tests the behaviour of the Service Component when there is a possible "
            + "exception")
    public void testActivateWithException() {
        DeviceTypeExtensionServiceComponent deviceTypeExtensionServiceComponent = new
                DeviceTypeExtensionServiceComponent();
        deviceTypeExtensionServiceComponent.activate(null);
    }

    @Test(description = "This test case tests the behaviour of the Service Component when the pre-conditions are "
            + "satisfied")
    public void testActivateWithoutException() {
        DeviceTypeExtensionServiceComponent deviceTypeExtensionServiceComponent = new
                DeviceTypeExtensionServiceComponent();
        deviceTypeExtensionServiceComponent.activate(MockOsgi.newComponentContext());
    }
}
