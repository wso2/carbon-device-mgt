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

$(document).ready(function() {
    $('#loading-content').show();
    var path = window.location.href;
    $('ul.nav a').each(function() {
        var url = this.href;
        if (url.indexOf("/devicemgt/platform-configuration") !== -1) {
            $(this).addClass('active');
        }
    });

    var configParams = {
        "NOTIFIER_TYPE": "notifierType",
        "NOTIFIER_FREQUENCY": "notifierFrequency"
    };

    var responseCodes = {
        "CREATED": "Created",
        "SUCCESS": "201",
        "INTERNAL_SERVER_ERROR": "Internal Server Error"
    };

    /**
     * Checks if provided input is valid against RegEx input.
     *
     * @param regExp Regular expression
     * @param inputString Input string to check
     * @returns {boolean} Returns true if input matches RegEx
     */
    function isPositiveInteger(str) {
        return /^\+?(0|[1-9]\d*)$/.test(str);
    }

    invokerUtil.get(
        "/api/device-mgt/v1.0/configuration",
        function(data) {
            data = JSON.parse(data);
            if (data && data.configuration) {
                for (var i = 0; i < data.configuration.length; i++) {
                    var config = data.configuration[i];
                    if (config.name == configParams["NOTIFIER_FREQUENCY"]) {
                        $("input#monitoring-config-frequency").val(config.value / 1000);
                    }
                }
            }
        },
        function(data) {
            console.log(data);
        });

    /**
     * Following click function would execute
     * when a user clicks on "Save" button
     * on General platform configuration page in WSO2 EMM Console.
     */
    $("button#save-general-btn").click(function() {
        var notifierFrequency = $("input#monitoring-config-frequency").val();
        var errorMsgWrapper = "#email-config-error-msg";
        var errorMsg = "#email-config-error-msg span";

        if (!notifierFrequency) {
            $(errorMsg).text("Monitoring frequency is a required field. It cannot be empty.");
            $(errorMsgWrapper).removeClass("hidden");
        } else if (!isPositiveInteger(notifierFrequency)) {
            $(errorMsg).text("Provided monitoring frequency is invalid. ");
            $(errorMsgWrapper).removeClass("hidden");
        } else {
            var addConfigFormData = {};
            var configList = new Array();

            var monitorFrequency = {
                "name": configParams["NOTIFIER_FREQUENCY"],
                "value": String((notifierFrequency * 1000)),
                "contentType": "text"
            };

            configList.push(monitorFrequency);
            addConfigFormData.configuration = configList;

            var addConfigAPI = "/api/device-mgt/v1.0/configuration";
            invokerUtil.put(
                addConfigAPI,
                addConfigFormData,
                function(data, textStatus, jqXHR) {
                    data = jqXHR.status;
                    if (data == 200) {
                        $("#config-save-form").addClass("hidden");
                        $("#record-created-msg").removeClass("hidden");
                        $('.breadcrumb').hide();
                    } else if (data == 500) {
                        $(errorMsg).text("Exception occurred at backend.");
                    } else if (data == 403) {
                        $(errorMsg).text("Action was not permitted.");
                    } else {
                        $(errorMsg).text("An unexpected error occurred.");
                    }

                    $(errorMsgWrapper).removeClass("hidden");
                },
                function(data) {
                    data = data.status;
                    if (data == 500) {
                        $(errorMsg).text("Exception occurred at backend.");
                    } else if (data == 403) {
                        $(errorMsg).text("Action was not permitted.");
                    } else {
                        $(errorMsg).text("An unexpected error occurred.");
                    }
                    $(errorMsgWrapper).removeClass("hidden");
                }
            );
        }
    });
    $('#loading-content').remove();
});

// Start of HTML embedded invoke methods
var showAdvanceOperation = function(operation, button) {
    $(button).addClass('selected');
    $(button).siblings().removeClass('selected');
    var hiddenOperation = ".wr-hidden-operations-content > div";
    $(hiddenOperation + '[data-operation="' + operation + '"]').show();
    $(hiddenOperation + '[data-operation="' + operation + '"]').siblings().hide();
};