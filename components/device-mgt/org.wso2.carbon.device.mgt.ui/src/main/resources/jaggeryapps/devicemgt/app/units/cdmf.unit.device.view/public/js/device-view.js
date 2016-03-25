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

(function () {
    $(document).ready(function () {
        $(".panel-body").removeClass("hidden");
        $("#loading-content").remove();

        if ($('#event_log').length) {
            loadOperationsLog();
        }

        if ($('#policies').length) {
            loadPolicies();
        }

        if ($('#policy_compliance').length) {
            loadPolicyCompliance();
        }


        $("#refresh-policy").click(function () {
            $('#policy-spinner').removeClass('hidden');
            loadPolicyCompliance();
        });

        $("#refresh-operations").click(function () {
            $('#operations-spinner').removeClass('hidden');
            loadOperationsLog(true);
        });

    });

    function loadOperationsLog(update) {
        var operationsLog = $("#operations-log");
        var deviceListingSrc = operationsLog.attr("src");
        var deviceId = operationsLog.data("device-id");
        var deviceType = operationsLog.data("device-type");

        $.template("operations-log", deviceListingSrc, function (template) {
            var serviceURL = "/devicemgt_admin/operations/" + deviceType + "/" + deviceId;

            var successCallback = function (data) {
                data = JSON.parse(data);
                $('#operations-spinner').addClass('hidden');
                var viewModel = {};
                viewModel.operations = data;
                if (data.length > 0) {
                    var content = template(viewModel);
                    if (!update) {
                        $("#operations-log-container").html(content);
                        $('#operations-log-table').datatables_extended();
                    } else {
                        $('#operations-log-table').dataTable().fnClearTable();
                        for (var i = 0; i < data.length; i++) {
                            var status;
                            if (data[i].status == "COMPLETED") {
                                status = "<span><i class='fw fw-ok icon-success'></i> Completed</span>";
                            } else if (data[i].status == "PENDING") {
                                status = "<span><i class='fw fw-warning icon-warning'></i> Pending</span>";
                            } else if (data[i].status == "ERROR") {
                                status = "<span><i class='fw fw-error icon-danger'></i> Error</span>";
                            } else if (data[i].status == "IN_PROGRESS") {
                                status = "<span><i class='fw fw-ok icon-warning'></i> In Progress</span>";
                            }

                            $('#operations-log-table').dataTable().fnAddData([
                                                                                 data[i].code,
                                                                                 status,
                                                                                 data[i].createdTimeStamp
                                                                             ]);
                        }
                    }
                }

            };
            invokerUtil.get(serviceURL,
                            successCallback, function (message) {
                        console.log(message);
                    });
        });

    }

    function loadPolicies() {
        var policyList = $("#policy-list");
        var policyListingSrc = policyList.attr("src");
        var deviceId = policyList.data("device-id");
        var deviceType = policyList.data("device-type");

        $.template("policy-list", policyListingSrc, function (template) {
            var serviceURL = "/devicemgt_admin/policies";

            var successCallback = function (data) {
                data = JSON.parse(data);
                $('#policy-spinner').addClass('hidden');
                var policyListFromRestEndpoint = data.responseContent;
                if (policyListFromRestEndpoint.length > 0) {
                    var viewModel = {};
                    var policyListToView = [];
                    var i, policyObjectFromRestEndpoint, policyObjectToView;
                    for (i = 0; i < policyListFromRestEndpoint.length; i++) {
                        // get list object
                        policyObjectFromRestEndpoint = policyListFromRestEndpoint[i];
                        // populate list object values to view-object
                        policyObjectToView = {};
                        policyObjectToView["id"] = policyObjectFromRestEndpoint["id"];
                        policyObjectToView["priorityId"] = policyObjectFromRestEndpoint["priorityId"];
                        policyObjectToView["name"] = policyObjectFromRestEndpoint["policyName"];
                        policyObjectToView["platform"] = policyObjectFromRestEndpoint["profile"]["deviceType"]["name"];
                        policyObjectToView["ownershipType"] = policyObjectFromRestEndpoint["ownershipType"];
                        policyObjectToView["compliance"] = policyObjectFromRestEndpoint["compliance"];

                        if (policyObjectFromRestEndpoint["active"] == true && policyObjectFromRestEndpoint["updated"] == true) {
                            policyObjectToView["status"] = "Active/Updated";
                        } else if (policyObjectFromRestEndpoint["active"] == true && policyObjectFromRestEndpoint["updated"] == false) {
                            policyObjectToView["status"] = "Active";
                        } else if (policyObjectFromRestEndpoint["active"] == false && policyObjectFromRestEndpoint["updated"] == true) {
                            policyObjectToView["status"] = "Inactive/Updated";
                        } else if (policyObjectFromRestEndpoint["active"] == false && policyObjectFromRestEndpoint["updated"] == false) {
                            policyObjectToView["status"] = "Inactive";
                        }
                        // push view-objects to list
                        policyListToView.push(policyObjectToView);
                    }
                    viewModel.policies = policyListToView;
                    var content = template(viewModel);
                    $("#policy-list-container").html(content);
                    $('#policy-table').datatables_extended();
                }
            };
            invokerUtil.get(serviceURL,
                            successCallback, function (message) {
                        console.log(message);
                    });
        });

    }

    function loadPolicyCompliance() {
        var policyCompliance = $("#policy-view");
        var policySrc = policyCompliance.attr("src");
        var deviceId = policyCompliance.data("device-id");
        var deviceType = policyCompliance.data("device-type");
        var activePolicy = null;

        $.template("policy-view", policySrc, function (template) {
            var serviceURLPolicy = "/devicemgt_admin/policies/" + deviceType + "/" + deviceId + "/active-policy"
            var serviceURLCompliance = "/devicemgt_admin/policies/" + deviceType + "/" + deviceId;

            var successCallbackCompliance = function (data) {
                var viewModel = {};
                viewModel.policy = activePolicy;
                viewModel.deviceType = deviceType;
                if (data != null && data.complianceFeatures != null && data.complianceFeatures != undefined && data.complianceFeatures.length > 0) {
                    viewModel.compliance = "NON-COMPLIANT";
                    viewModel.complianceFeatures = data.complianceFeatures;
                    var content = template(viewModel);
                    $("#policy-list-container").html(content);
                } else {
                    viewModel.compliance = "COMPLIANT";
                    var content = template(viewModel);
                    $("#policy-list-container").html(content);
                    $("#policy-compliance-table").addClass("hidden");
                }

            };

            var successCallbackPolicy = function (data) {
                data = JSON.parse(data);
                $('#policy-spinner').addClass('hidden');
                if (data != null && data.active == true) {
                    activePolicy = data;
                    invokerUtil.get(serviceURLCompliance,
                                    successCallbackCompliance, function (message) {
                                console.log(message);
                            });
                }
            };

            invokerUtil.get(serviceURLPolicy,
                            successCallbackPolicy, function (message) {
                        console.log(message);
                    });
        });

    }

}());
