/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.extensions.push.notification.provider.gcm;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.push.notification.NotificationContext;
import org.wso2.carbon.device.mgt.common.push.notification.NotificationStrategy;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationConfig;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationExecutionFailedException;
import org.wso2.carbon.device.mgt.extensions.push.notification.provider.gcm.internal.GCMDataHolder;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class GCMNotificationStrategy implements NotificationStrategy {

    private static final String GCM_TOKEN = "GCM_TOKEN";
    private final static String GCM_ENDPOINT = "https://fcm.googleapis.com/fcm/send";
    private static final String GCM_API_KEY = "gcmAPIKey";
    private static final int TIME_TO_LIVE = 60;
    private static final int HTTP_STATUS_CODE_OK = 200;
    private PushNotificationConfig config;

    public GCMNotificationStrategy(PushNotificationConfig config) {
        this.config = config;
    }

    @Override
    public void init() {

    }

    @Override
    public void execute(NotificationContext ctx) throws PushNotificationExecutionFailedException {
        try {
            Device device =
                    GCMDataHolder.getInstance().getDeviceManagementProviderService().getDevice(ctx.getDeviceId());
            this.sendWakeUpCall(ctx.getOperation().getCode(), device);
        } catch (DeviceManagementException e) {
            throw new PushNotificationExecutionFailedException("Error occurred while retrieving device information", e);
        } catch (IOException e) {
            throw new PushNotificationExecutionFailedException("Error occurred while sending push notification", e);
        }
    }

    @Override
    public NotificationContext buildContext() {
        return null;
    }

    @Override
    public void undeploy() {

    }

    private void sendWakeUpCall(String message,
                                Device device) throws IOException, PushNotificationExecutionFailedException {
        OutputStream os = null;
        byte[] bytes = getGCMRequest(message, getGCMToken(device.getProperties())).getBytes();

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(GCM_ENDPOINT).openConnection();
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "key=" + config.getProperty(GCM_API_KEY));
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            os = conn.getOutputStream();
            os.write(bytes);
        } finally {
            if (os != null) {
                os.close();
            }
        }
        int status = conn.getResponseCode();
        if (status != HTTP_STATUS_CODE_OK) {
            throw new PushNotificationExecutionFailedException("Push notification sending failed with the HTTP " +
                    "error code '" + status + "'");
        }
    }

    private static String getGCMRequest(String message, String registrationId) {
        JsonObject gcmRequest = new JsonObject();
        gcmRequest.addProperty("delay_while_idle", false);
        gcmRequest.addProperty("time_to_live", TIME_TO_LIVE);

        //Add message to GCM request
        JsonObject data = new JsonObject();
        if (message != null && !message.isEmpty()) {
            data.addProperty("data", message);
            gcmRequest.add("data", data);
        }

        //Set device reg-id
        JsonArray regIds = new JsonArray();
        regIds.add(new JsonPrimitive(registrationId));

        gcmRequest.add("registration_ids", regIds);
        return gcmRequest.toString();
    }

    private static String getGCMToken(List<Device.Property> properties) {
        String gcmToken = null;
        for (Device.Property property : properties) {
            if (GCM_TOKEN.equals(property.getName())) {
                gcmToken = property.getValue();
                break;
            }
        }
        return gcmToken;
    }

}
