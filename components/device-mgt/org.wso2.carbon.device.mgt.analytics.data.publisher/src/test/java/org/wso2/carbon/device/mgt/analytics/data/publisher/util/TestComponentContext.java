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
package org.wso2.carbon.device.mgt.analytics.data.publisher.util;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentInstance;

import java.util.Dictionary;

public class TestComponentContext implements ComponentContext {
    @Override
    public Dictionary getProperties() {
        return null;
    }

    @Override
    public Object locateService(String s) {
        return null;
    }

    @Override
    public Object locateService(String s, ServiceReference serviceReference) {
        return null;
    }

    @Override
    public Object[] locateServices(String s) {
        return new Object[0];
    }

    @Override
    public BundleContext getBundleContext() {
        return null;
    }

    @Override
    public Bundle getUsingBundle() {
        return null;
    }

    @Override
    public ComponentInstance getComponentInstance() {
        return null;
    }

    @Override
    public void enableComponent(String s) {

    }

    @Override
    public void disableComponent(String s) {

    }

    @Override
    public ServiceReference getServiceReference() {
        return null;
    }
}
