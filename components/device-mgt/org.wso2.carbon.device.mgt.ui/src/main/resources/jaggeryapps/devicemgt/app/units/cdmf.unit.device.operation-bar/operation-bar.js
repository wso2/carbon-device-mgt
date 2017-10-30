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

function onRequest(context) {
    var log = new Log("operation.js");
    var operationModule = require("/app/modules/business-controllers/operation.js")["operationModule"];
    var device = context.unit.params.device;
    var autoCompleteParams = context.unit.params.autoCompleteParams;
    var encodedFeaturePayloads=context.unit.params.encodedFeaturePayloads;
    var controlOperations = operationModule.getControlOperations(device.type);
    var queryParams = [];
    var formParams = [];
    var pathParams = [];
    for (var i = 0; i < controlOperations.length; i++) {
        var currentParamList = controlOperations[i]["params"];
        for (var j = 0; j < currentParamList.length; j++) {
            var currentParam = currentParamList[j];
            currentParamList[j]["formParams"] = processParams(currentParam["formParams"], autoCompleteParams);
            currentParamList[j]["queryParams"] = processParams(currentParam["queryParams"], autoCompleteParams);
            currentParamList[j]["pathParams"] = processParams(currentParam["pathParams"], autoCompleteParams);
        }
        controlOperations[i]["params"] = currentParamList;
        if (encodedFeaturePayloads) {
            controlOperations[i]["payload"] = getPayload(encodedFeaturePayloads, controlOperations[i]["operation"]);
        }
    }
    return {"control_operations": controlOperations, "device": device};
}

function processParams(paramsList, autoCompleteParams) {
    for (var i = 0; i < paramsList.length; i++) {
        var paramName = paramsList[i];
        var paramValue = "";
        var paramType = "text";
        for (var k = 0; k < autoCompleteParams.length; k++) {
            if (paramName == autoCompleteParams[k].name) {
                paramValue = autoCompleteParams[k].value;
                paramType = "hidden";
            }
        }
        paramsList[i] = {"name": paramName, "value": paramValue, "type": paramType};
    }
    return paramsList;
}

function getPayload(featuresPayload, featureCode){
    var featuresJSONPayloads = JSON.parse(featuresPayload);
    return featuresJSONPayloads[featureCode];
}