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

var invokerUtil = function () {

    var module = {};

    var END_POINT = window.location.origin+"/devicemgt/api/invoker/execute/";

    module.get = function (url, successCallback, errorCallback, contentType, acceptType) {
        var payload = null;
        execute("GET", url, payload, successCallback, errorCallback, contentType, acceptType);
    };
    module.post = function (url, payload, successCallback, errorCallback, contentType, acceptType) {
        execute("POST", url, payload, successCallback, errorCallback, contentType, acceptType);
    };
    module.put = function (url, payload, successCallback, errorCallback, contentType, acceptType) {
        execute("PUT", url, payload, successCallback, errorCallback, contentType, acceptType);
    };
    module.delete = function (url, successCallback, errorCallback, contentType, acceptType) {
        var payload = null;
        execute("DELETE", url, payload, successCallback, errorCallback, contentType, acceptType);
    };
    function execute (methoad, url, payload, successCallback, errorCallback, contentType, acceptType) {
        if(contentType == undefined){
            contentType = "application/json";
        }
        if(acceptType == undefined){
            acceptType = "application/json";
        }
        var data = {
            url: END_POINT,
            type: "POST",
            contentType: contentType,
            accept: acceptType,
            success: successCallback
        };
        var paramValue = {};
        paramValue.actionMethod = methoad;
        paramValue.actionUrl = url;
        paramValue.actionPayload = payload;
        if(contentType == "application/json"){
            paramValue.actionPayload = JSON.stringify(payload);
        }
        data.data = JSON.stringify(paramValue);
        $.ajax(data).fail(function (jqXHR) {
            if (jqXHR.status == "401") {
                console.log("Unauthorized access attempt!");
                $(modalPopupContent).html($('#error-msg').html());
                showPopup();
            } else {
                errorCallback(jqXHR);
            }
        });
    };
    return module;
}();
