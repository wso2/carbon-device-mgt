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
package org.wso2.carbon.device.mgt.common.group.mgt;


/**
 * Holds Device Group constants and expose to external access
 */
public class DeviceGroupConstants {
    public enum RolePermissions {
        DEFAULT_ADMIN_ROLE(Roles.DEFAULT_ADMIN_ROLE),
        DEFAULT_OPERATOR_ROLE(Roles.DEFAULT_OPERATOR_ROLE),
        DEFAULT_STATS_MONITOR_ROLE(Roles.DEFAULT_STATS_MONITOR_ROLE),
        DEFAULT_VIEW_POLICIES(Roles.DEFAULT_VIEW_POLICIES),
        DEFAULT_MANAGE_POLICIES(Roles.DEFAULT_MANAGE_POLICIES),
        DEFAULT_VIEW_EVENTS(Roles.DEFAULT_VIEW_EVENTS);

        private String value;
        private String[] permissions;

        RolePermissions(String value) {
            this.value = value;
        }

        static {
            DEFAULT_ADMIN_ROLE.permissions = Permissions.DEFAULT_ADMIN_PERMISSIONS;
            DEFAULT_OPERATOR_ROLE.permissions = Permissions.DEFAULT_OPERATOR_PERMISSIONS;
            DEFAULT_STATS_MONITOR_ROLE.permissions = Permissions.DEFAULT_STATS_MONITOR_PERMISSIONS;
            DEFAULT_VIEW_POLICIES.permissions = Permissions.DEFAULT_MANAGE_POLICIES_PERMISSIONS;
            DEFAULT_MANAGE_POLICIES.permissions = Permissions.DEFAULT_VIEW_POLICIES_PERMISSIONS;
            DEFAULT_VIEW_EVENTS.permissions = Permissions.DEFAULT_VIEW_EVENTS_PERMISSIONS;
        }

        @Override
        public String toString(){
            return this.value;
        }

        public String[] getPermissions(){
            return permissions;
        }
    }

    public static class Roles {
        public static final String DEFAULT_ADMIN_ROLE = "admin";
        public static final String DEFAULT_OPERATOR_ROLE = "invoke-device-operations";
        public static final String DEFAULT_STATS_MONITOR_ROLE = "view-statistics";
        public static final String DEFAULT_VIEW_POLICIES = "view-policies";
        public static final String DEFAULT_MANAGE_POLICIES = "mange-policies";
        public static final String DEFAULT_VIEW_EVENTS = "view-events";
    }

    public static class Permissions {
        public static final String[] DEFAULT_ADMIN_PERMISSIONS =
                {"/permission/device-mgt/groups", "/permission/device-mgt/user/groups"};
        public static final String[] DEFAULT_OPERATOR_PERMISSIONS =
                {"/permission/device-mgt/user/groups/device_operation"};
        public static final String[] DEFAULT_STATS_MONITOR_PERMISSIONS =
                {"/permission/device-mgt/user/groups/device_monitor"};
        public static final String[] DEFAULT_MANAGE_POLICIES_PERMISSIONS =
                {"/permission/device-mgt/user/groups/device_policies/add"};
        public static final String[] DEFAULT_VIEW_POLICIES_PERMISSIONS =
                {"/permission/device-mgt/user/groups/device_policies/view"};
        public static final String[] DEFAULT_VIEW_EVENTS_PERMISSIONS =
                {"/permission/device-mgt/user/groups/device_events"};
    }
}
