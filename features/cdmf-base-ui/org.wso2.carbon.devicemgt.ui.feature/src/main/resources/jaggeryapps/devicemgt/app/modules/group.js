/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

var groupModule = {};
(function (groupModule) {
    var log = new Log("/app/modules/group.js");

    var constants = require('/app/modules/constants.js');
    var devicemgtProps = require('/app/conf/devicemgt-props.js').config();
    var utility = require("/app/modules/utility.js").utility;

    var deviceCloudService = devicemgtProps["httpsURL"] + "/common/group_manager";

    var user = session.get(constants.USER_SESSION_KEY);
    var deviceModule = require("/app/modules/device.js").deviceModule;

    var endPoint;
    var data;

    groupModule.addGroup = function (group) {
        var name = group["name"];
        var description = group["description"];
        //URL: POST https://localhost:9443/devicecloud/group_manager/group
        endPoint = deviceCloudService + "/group";
        data = {"name": name, "username": user.username, "description": description};
        return post(endPoint, data, "json");
    };

    groupModule.updateGroup = function (groupId, group) {
        var name = group["name"];
        var description = group["description"];
        //URL: PUT https://localhost:9443/devicecloud/group_manager/group/id/{groupId}
        endPoint = deviceCloudService + "/group/id/" + groupId;
        data = {"name": name, "username": user.username, "description": description};
        return put(endPoint, data, "json");
    };

    groupModule.removeGroup = function (groupId) {
        //URL: DELETE https://localhost:9443/devicecloud/group_manager/group/id/{groupId}
        endPoint = deviceCloudService + "/group/id/" + groupId + "?username=" + user.username;
        return del(endPoint, {}, "json");
    };

    groupModule.getGroup = function (groupId) {
        //URL: GET https://localhost:9443/devicecloud/group_manager/group/id/{groupId}
        endPoint = deviceCloudService + "/group/id/" + groupId;
        data = {"username": user.username};
        return get(endPoint, data, "json");
    };

    groupModule.findGroups = function (name) {
        //URL: GET https://localhost:9443/devicecloud/group_manager/group/name/{name}
        endPoint = deviceCloudService + "/group/name/" + name;
        data = {"username": user.username};
        return get(endPoint, data, "json");
    };

    groupModule.getGroups = function () {
        //URL: GET https://localhost:9443/devicecloud/group_manager/group/all
        endPoint = deviceCloudService + "/group/user/" + user.username + "/all";
        data = {"username": user.username};
        return get(endPoint, data, "json");
    };

    groupModule.getGroupCount = function () {
        //URL: GET https://localhost:9443/devicecloud/group_manager/group/all/count
        endPoint = deviceCloudService + "/group/user/" + user.username + "/all/count";
        data = {"username": user.username};
        return get(endPoint, data, "json");
    };

    groupModule.shareGroup = function (groupId, shareUser, role) {
        //URL: POST https://localhost:9443/devicecloud/group_manager/group/id/{groupId}/share
        endPoint = deviceCloudService + "/group/id/" + groupId + "/share";
        data = {"username": user.username, "shareUser": shareUser, "role": role};
        return post(endPoint, data, "json");
    };

    groupModule.unshareGroup = function (groupId, shareUser, role) {
        //URL: POST https://localhost:9443/devicecloud/group_manager/group/id/{groupId}/unshare
        endPoint = deviceCloudService + "/group/id/" + groupId + "/unshare";
        data = {"username": user.username, "unShareUser": unShareUser, "role": role};
        return post(endPoint, data, "json");
    };

    groupModule.addRole = function (groupId, role, permissions) {
        //URL: POST https://localhost:9443/devicecloud/group_manager/group/id/{groupId}/role
        endPoint = deviceCloudService + "/group/id/" + groupId + "/role";
        data = {"username": user.username, "permissions": permissions, "role": role};
        return post(endPoint, data, "json");
    };

    groupModule.deleteRole = function (groupId, role) {
        //URL: DELETE https://localhost:9443/devicecloud/group_manager/group/id/{groupId}/role
        endPoint = deviceCloudService + "/group/id/" + groupId + "/role/" + role;
        return del(endPoint, {}, "json");
    };

    groupModule.getGroupRoles = function (groupId) {
        //URL: GET https://localhost:9443/devicecloud/group_manager/group/id/{groupId}/role/all
        endPoint = deviceCloudService + "/group/id/" + groupId + "/role/all";
        data = {"username": user.username};
        return get(endPoint, data, "json");
    };

    groupModule.getUserRoles = function (groupId, userId) {
        //URL: GET https://localhost:9443/devicecloud/group_manager/group/id/{groupId}/{user}/role/all
        endPoint = deviceCloudService + "/group/id/" + groupId + "/" + userId + "/role/all";
        data = {"username": user.username};
        return get(endPoint, data, "json");
    };

    groupModule.getRoleMapping = function (groupId, userId) {
        var allRoles = groupModule.getGroupRoles(groupId).data;
        var userRolesObj = groupModule.getUserRoles(groupId, userId);
        var userRoles = userRolesObj.data;
        var roleMap = [];
        for (var role in allRoles) {
            var objRole = {"role": allRoles[role], "assigned": false};
            for (var usrRole in userRoles) {
                if (allRoles[role] == userRoles[usrRole]) {
                    objRole.assigned = true;
                    break;
                }
            }
            roleMap.push(objRole);
        }
        var result = {};
        result.data = roleMap;
        result.xhr = userRolesObj.xhr;
        return result;
    };

    groupModule.setRoleMapping = function (groupId, userId, roleMap) {
        var result;
        for (var role in roleMap) {
            if (roleMap[role].assigned == true) {
                result = groupModule.shareGroup(groupId,userId,roleMap[role].role);
            } else {
                result = groupModule.unshareGroup(groupId,userId,roleMap[role].role);
            }
        }
        return result;
    };

    groupModule.getUsers = function (groupId) {
        //URL: GET https://localhost:9443/devicecloud/group_manager/group/id/{groupId}/user/all
        endPoint = deviceCloudService + "/group/id/" + groupId + "/user/all";
        data = {"username": user.username};
        return get(endPoint, data, "json");
    };

    groupModule.getDevices = function (groupId) {
        var result = groupModule.getGroup(groupId);
        var group = result.data;
        if (group) {
            //URL: GET https://localhost:9443/devicecloud/group_manager/group/id/{groupId}/device/all
            endPoint = deviceCloudService + "/group/id/" + groupId + "/device/all";
            data = {"username": user.username};
            result = get(endPoint, data, "json");
            var devices = result.data;
            for (var d in devices){
                devices[d].assetId = deviceModule.getAssetId( devices[d].type);
            }
            group.devices = devices;
            result.data = {group: group};
        }
        return result;
    };

    groupModule.assignDevice = function (groupId, deviceId, deviceType) {
        //URL: GET https://localhost:9443/devicecloud/group_manager/group/id/{groupId}/device/assign
        endPoint = deviceCloudService + "/group/id/" + groupId + "/device/assign";
        data = {"username": user.username, "deviceId": deviceId, "deviceType": deviceType};
        return put(endPoint, data, "json");
    };

}(groupModule));
