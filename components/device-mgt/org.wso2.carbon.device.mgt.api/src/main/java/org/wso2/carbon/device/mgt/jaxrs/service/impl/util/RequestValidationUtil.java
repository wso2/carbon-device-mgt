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
package org.wso2.carbon.device.mgt.jaxrs.service.impl.util;

import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.notification.mgt.Notification;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;

import java.util.ArrayList;
import java.util.List;

public class RequestValidationUtil {

    /**
     * Checks if multiple criteria are specified in a conditional request.
     *
     * @param type      Device type upon which the selection is done
     * @param user      Device user upon whom the selection is done
     * @param roleName  Role name upon which the selection is done
     * @param ownership Ownership type upon which the selection is done
     * @param status    Enrollment status upon which the selection is done
     */
    public static void validateSelectionCriteria(final String type, final String user, final String roleName,
                                                 final String ownership, final String status) {
        List<String> inputs = new ArrayList<String>() {{
            add(type);
            add(user);
            add(roleName);
            add(ownership);
            add(status);
        }};

//        boolean hasOneSelection = false;
//        for (String i : inputs) {
//            if (i == null) {
//                continue;
//            }
//            hasOneSelection = !hasOneSelection;
//            if (!hasOneSelection) {
//                break;
//            }
//        }
        int count = 0;
        for (String i : inputs) {
            if (i == null) {
                continue;
            }
            count++;
            if (count > 1) {
                break;
            }
        }
        if (count > 1) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("The incoming request has " +
                            "more than one selection criteria defined through query parameters").build());
        }

    }


    public static void validateDeviceIdentifier(String type, String id) {
        boolean isErroneous = false;
        ErrorResponse.ErrorResponseBuilder error = new ErrorResponse.ErrorResponseBuilder();
        if (id == null) {
            isErroneous = true;
            error.addErrorItem(null, "Device identifier cannot be null");
        }
        if (type == null) {
            isErroneous = true;
            error.addErrorItem(null, "Device type cannot be null");
        }
        if (isErroneous) {
            throw new InputValidationException(error.setCode(400l).setMessage("Invalid device identifier").build());

        }
    }

    public static void validateStatus(String status) {
        if (status == null) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage(
                            "Enrollment status type cannot be null").build());
        }
        switch (status) {
            case "ACTIVE":
            case "INACTIVE":
            case "UNCLAIMED":
            case "UNREACHABLE":
            case "SUSPENDED":
            case "DISENROLLMENT_REQUESTED":
            case "REMOVED":
            case "BLOCKED":
            case "CREATED":
                return;
            default:
                throw new InputValidationException(
                        new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Invalid enrollment status type " +
                                "received. Valid status types are ACTIVE | INACTIVE | " +
                                "UNCLAIMED | UNREACHABLE | SUSPENDED | DISENROLLMENT_REQUESTED | REMOVED | " +
                                "BLOCKED | CREATED").build());
        }
    }

    public static void validateOwnershipType(String ownership) {
        if (ownership == null) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage(
                            "Ownership type cannot be null").build());
        }
        switch (ownership) {
            case "BYOD":
            case "COPE":
                return;
            default:
                throw new InputValidationException(
                        new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage(
                                "Invalid ownership type received. " +
                                        "Valid ownership types are BYOD | COPE").build());
        }
    }

    public static void validateNotificationStatus(String status) {
        if (status == null) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage(
                            "Notification status type cannot be null").build());
        }
        switch (status) {
            case "NEW":
            case "CHECKED":
                return;
            default:
                throw new InputValidationException(
                        new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Invalid notification status type " +
                                "received. Valid status types are NEW | CHECKED").build());
        }
    }

    public static void validateNotificationId(int id) {
        if (id <= 0) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Invalid notification id. " +
                            "Only positive integers are accepted as valid notification Ids").build());
        }
    }

    public static void validateNotification(Notification notification) {
        if (notification == null) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Notification content " +
                            "cannot be null").build());
        }
    }

    public static void validateTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Timestamp value " +
                            "cannot be null or empty").build());
        }
        try {
            Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage(
                            "Invalid timestamp value").build());
        }
    }

    public static void validateActivityId(String activityId) {
        if (activityId == null || activityId.isEmpty()) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Activity Id " +
                            "cannot be null or empty. It should be in the form of " +
                            "'[ACTIVITY][_][any-positive-integer]' instead").build());
        }
        String[] splits = activityId.split("_");
        if (splits == null || splits[0] == null || splits[0].isEmpty() || !"ACTIVITY".equals(splits[0]) ||
                splits[1] == null || splits[0].isEmpty()) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage(
                            "Activity Id should be in the form of '[ACTIVITY][_][any-positive-integer]'").build());
        }
        try {
            Long.parseLong(splits[1]);
        } catch (NumberFormatException e) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage(
                            "Activity Id should be in the form of '[ACTIVITY][_][any-positive-integer]'").build());
        }
    }

}
