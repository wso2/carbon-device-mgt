/*
 * Copyright 2005-2015 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.device.mgt.tenancy.listener.core;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.exception.StratosException;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;

import java.io.IOException;

public class IoTTenantManagementListener implements TenantMgtListener {

    private static final Log log = LogFactory.getLog(IoTTenantManagementListener.class);

    private String ioTBackendURL;

    public IoTTenantManagementListener(String ioTBackendURL) {
        this.ioTBackendURL = ioTBackendURL;
    }

    @Override
    public void onTenantCreate(TenantInfoBean tenantInfoBean) throws StratosException {
        PostMethod postMethod = null;
        try {
            int tenantId = tenantInfoBean.getTenantId();
            String tenantDomain = tenantInfoBean.getTenantDomain();

            if (log.isDebugEnabled()) {
                String msg = "Publishing tenant creation notification for tenant: " + tenantId + " - " + tenantDomain;
                log.debug(msg);
            }

            JSONObject tenantDetails = new JSONObject();
            tenantDetails.put("tenantId", tenantId);
            tenantDetails.put("tenantDomain", tenantDomain);
            tenantDetails.put("tenantAdmin", tenantInfoBean.getAdmin() + "@" + tenantDomain);
            String content = tenantDetails.toString();

            HttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();
            HttpClient httpClient = new HttpClient(httpConnectionManager);

            postMethod = new PostMethod(ioTBackendURL);
            StringRequestEntity contentEntity = new StringRequestEntity(content, "application/json", "UTF-8");
            postMethod.setRequestEntity(contentEntity);
            httpClient.executeMethod(postMethod);

        } catch (IOException | JSONException e) {
            String msg = "Failed to publish tenant creation notification to IoTS backend for tenant: " +
                    tenantInfoBean.getTenantId() + " - " + tenantInfoBean.getTenantDomain();
            log.error(msg, e);
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
            }
        }
    }

    @Override
    public void onTenantUpdate(TenantInfoBean tenantInfoBean) throws StratosException {

    }

    @Override
    public void onTenantDelete(int i) {

    }

    @Override
    public void onTenantRename(int i, String s, String s1) throws StratosException {

    }

    @Override
    public void onTenantInitialActivation(int i) throws StratosException {

    }

    @Override
    public void onTenantActivation(int i) throws StratosException {

    }

    @Override
    public void onTenantDeactivation(int i) throws StratosException {

    }

    @Override
    public void onSubscriptionPlanChange(int i, String s, String s1) throws StratosException {

    }

    @Override
    public int getListenerOrder() {
        return 0;
    }

    @Override
    public void onPreDelete(int i) throws StratosException {

    }

}
