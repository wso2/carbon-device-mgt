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
package org.wso2.carbon.device.mgt.analytics.data.publisher;


import org.apache.sling.testing.mock.osgi.MockOsgi;
import org.osgi.service.component.ComponentContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.analytics.data.publisher.internal.DataPublisherServiceComponent;
import org.wso2.carbon.device.mgt.analytics.data.publisher.util.TestComponentContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This tesclass will be validating the behaviour of {@link DataPublisherServiceComponent}
 */
public class DataPublisherServiceComponentTest extends BaseAnalyticsDataPublisherTest {
    private DataPublisherServiceComponent serviceComponent;

    @BeforeClass
    public void initTest() {
        this.serviceComponent = new DataPublisherServiceComponent();
    }

    @Test (description = "Test bundle activation with exception thrown when service resgistration")
    public void activateWithException() throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        this.activate(new TestComponentContext());
    }

    @Test(dependsOnMethods = "activateWithException", description = "Test the bundle activation with succesful path")
    public void activateWithoutException() throws NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {
        this.activate(MockOsgi.newComponentContext());
    }

    @Test(dependsOnMethods = "activateWithoutException", description = "Test bundle deactivation")
    public void deActivate() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = this.serviceComponent.getClass().getDeclaredMethod("deactivate", ComponentContext.class);
        method.setAccessible(true);
        method.invoke(this.serviceComponent, MockOsgi.newComponentContext());
    }

    private void activate(ComponentContext componentContext) throws NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {
        Method method = this.serviceComponent.getClass().getDeclaredMethod("activate", ComponentContext.class);
        method.setAccessible(true);
        method.invoke(this.serviceComponent, componentContext);
    }
}
