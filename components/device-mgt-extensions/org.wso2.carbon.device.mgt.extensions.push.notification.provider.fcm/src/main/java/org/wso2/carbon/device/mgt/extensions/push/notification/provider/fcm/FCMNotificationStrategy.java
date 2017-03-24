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
package org.wso2.carbon.device.mgt.extensions.push.notification.provider.fcm;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.push.notification.NotificationContext;
import org.wso2.carbon.device.mgt.common.push.notification.NotificationStrategy;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationConfig;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationExecutionFailedException;
import org.wso2.carbon.device.mgt.extensions.push.notification.provider.fcm.internal.FCMDataHolder;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class FCMNotificationStrategy implements NotificationStrategy {

    private static final String FCM_TOKEN = "FCM_TOKEN";
    private static final String FCM_ENDPOINT = "https://fcm.googleapis.com/fcm/send";
    private static final String FCM_API_KEY = "fcmAPIKey";
    private static final int TIME_TO_LIVE = 60;
    private static final int HTTP_STATUS_CODE_OK = 200;
    private PushNotificationConfig config;

    public FCMNotificationStrategy(PushNotificationConfig config) {
        this.config = config;
    }

    @Override
    public void init() {

    }

    @Override
    public void execute(NotificationContext ctx) throws PushNotificationExecutionFailedException {
        try {
            Device device =
                    FCMDataHolder.getInstance().getDeviceManagementProviderService().getDevice(ctx.getDeviceId());
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
        byte[] bytes = getFCMRequest(message, getFCMToken(device.getProperties())).getBytes();

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(FCM_ENDPOINT).openConnection();
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "key=" + config.getProperty(FCM_API_KEY));
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

    private static String getFCMRequest(String message, String registrationId) {
        JsonObject fcmRequest = new JsonObject();
        fcmRequest.addProperty("delay_while_idle", false);
        fcmRequest.addProperty("time_to_live", TIME_TO_LIVE);

        //Add message to FCM request
        JsonObject data = new JsonObject();
        if (message != null && !message.isEmpty()) {
            data.addProperty("data", message);
            fcmRequest.add("data", data);
        }

        //Set device reg-id
        JsonArray regIds = new JsonArray();
        regIds.add(new JsonPrimitive(registrationId));

        fcmRequest.add("registration_ids", regIds);
        return fcmRequest.toString();
    }

    private static String getFCMToken(List<Device.Property> properties) {
        String fcmToken = null;
        for (Device.Property property : properties) {
            if (FCM_TOKEN.equals(property.getName())) {
                fcmToken = property.getValue();
                break;
            }
        }
        return fcmToken;
    }

}
