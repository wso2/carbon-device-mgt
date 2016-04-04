/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.extensions.feature.mgt.lifecycle.listener;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.GenericFeatureManager;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.DeviceType;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.util.AnnotationUtil;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public class FeatureManagementLifecycleListener implements LifecycleListener {

    private static final String API_CONFIG_DEFAULT_VERSION = "1.0.0";

    private static final String PARAM_MANAGED_API_ENABLED = "managed-api-enabled";

    private static final Log log = LogFactory.getLog(FeatureManagementLifecycleListener.class);
    private static final String UNLIMITED = "Unlimited";

    @Override
    public void lifecycleEvent(LifecycleEvent lifecycleEvent) {
        if (Lifecycle.AFTER_START_EVENT.equals(lifecycleEvent.getType())) {
            StandardContext context = (StandardContext) lifecycleEvent.getLifecycle();
            ServletContext servletContext = context.getServletContext();
            String param = servletContext.getInitParameter(PARAM_MANAGED_API_ENABLED);
            boolean isManagedApi = (param != null && !param.isEmpty()) && Boolean.parseBoolean(param);
            if (isManagedApi) {
                try {
                    AnnotationUtil annotationUtil = new AnnotationUtil(context);
                    Set<String> annotatedAPIClasses = annotationUtil.scanStandardContext(DeviceType.class.getName());
                    Map<String, List<Feature>> features = annotationUtil.extractFeatures(annotatedAPIClasses);
                    if (features != null && !features.isEmpty()) {
                        GenericFeatureManager.getInstance().addFeatures(features);
                    }
                } catch (IOException e) {
                    log.error("Error enconterd while discovering annotated classes.", e);
                } catch (ClassNotFoundException e) {
                    log.error("Error while scanning class for annotations.", e);
                }
            }
        }
    }

}
