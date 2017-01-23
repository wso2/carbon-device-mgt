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

    var deviceId = $(".device-id");
    var deviceIdentifier = deviceId.data("deviceid");
    var deviceType = deviceId.data("type");
    var deviceOwner = deviceId.data("owner");

    $(document).ready(function () {
        $(".panel-body").removeClass("hidden");
        $("#loading-content").remove();

        if ($('#event_log').length) {
            loadOperationsLog();
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
        var operationsLogTable = "#operations-log-table";

        if (update) {
            operationTable = $(operationsLogTable).DataTable();
            $("#operations-spinner").removeClass("hidden");
            operationTable.ajax.reload(function ( json ) {
                $("#operations-spinner").addClass("hidden");
            }, false);
            return;
        }
        operationTable = $(operationsLogTable).datatables_extended({
            serverSide: true,
            processing: false,
            searching: false,
            ordering:  false,
            pageLength : 10,
            order: [],
            ajax: {

                url: "/devicemgt/api/operation/paginate",
                data: {deviceId : deviceIdentifier, deviceType: deviceType, owner: deviceOwner},
                dataSrc: function (json) {
                    $("#operations-spinner").addClass("hidden");
                    $("#operations-log-container").empty();
                    return json.data;
                }
            },
            columnDefs: [
                {targets: 0, data: "code" },
                {targets: 1, data: "status", render:
                    function (status) {
                        var html;
                        switch (status) {
                            case "COMPLETED" :
                                html = "<span><i class='fw fw-success icon-success'></i> Completed</span>";
                                break;
                            case "PENDING" :
                                html = "<span><i class='fw fw-warning icon-warning'></i> Pending</span>";
                                break;
                            case "ERROR" :
                                html = "<span><i class='fw fw-error icon-danger'></i> Error</span>";
                                break;
                            case "IN_PROGRESS" :
                                html = "<span><i class='fw fw-success icon-warning'></i> In Progress</span>";
                                break;
                            case "REPEATED" :
                                html = "<span><i class='fw fw-success icon-warning'></i> Repeated</span>";
                                break;
                        }
                        return html;
                    }
                },
                {targets: 2, data: "createdTimeStamp", render:
                    function (date) {
                        var value = String(date);
                        return value.slice(0, 16);
                    }
                }
            ],
            "createdRow": function(row, data) {

                $(row).attr("data-type", "selectable");
                $(row).attr("data-id", data["id"]);
                $.each($("td", row),
                    function(colIndex) {
                        switch(colIndex) {
                            case 1:
                                $(this).attr("data-grid-label", "Code");
                                $(this).attr("data-display", data["code"]);
                                break;
                            case 2:
                                $(this).attr("data-grid-label", "Status");
                                $(this).attr("data-display", data["status"]);
                                break;
                            case 3:
                                $(this).attr("data-grid-label", "Created Timestamp");
                                $(this).attr("data-display", data["createdTimeStamp"]);
                                break;
                        }
                    }
                );
            }
        });
    }

    function loadPolicyCompliance() {
        var policyCompliance = $("#policy-view");
        var policyComplianceTemplate = policyCompliance.attr("src");
        var deviceId = policyCompliance.data("device-id");
        var deviceType = policyCompliance.data("device-type");
        var activePolicy = null;

        $.template(
            "policy-view",
            policyComplianceTemplate,
            function (template) {
                var getEffectivePolicyURL = "/api/device-mgt/v1.0/devices/" + deviceType + "/" + deviceId + "/effective-policy";
                var getDeviceComplianceURL = "/api/device-mgt/v1.0/devices/" + deviceType + "/" + deviceId + "/compliance-data";
                invokerUtil.get(
                    getEffectivePolicyURL,
                    // success-callback
                    function (data, textStatus, jqXHR) {
                        if (jqXHR.status == 200) {
                            $("#policy-spinner").addClass("hidden");
                            if(data){
                                data = JSON.parse(data);
                                if (data["active"] == true) {
                                    activePolicy = data;
                                    invokerUtil.get(
                                        getDeviceComplianceURL,
                                        // success-callback
                                        function (data, textStatus, jqXHR) {
                                            if (jqXHR.status == 200 && data) {
                                                var viewModel = {};
                                                viewModel["policy"] = activePolicy;
                                                viewModel["deviceType"] = deviceType;
                                                viewModel["deviceId"] = deviceId;
                                                viewModel["appContext"] = context;
                                                data = JSON.parse(data);
                                                var content;
                                                if (data["complianceData"]) {
                                                    if (data["complianceData"]["complianceFeatures"] &&
                                                        data["complianceData"]["complianceFeatures"].length > 0) {
                                                        viewModel["compliance"] = "NON-COMPLIANT";
                                                        viewModel["complianceFeatures"] = data["complianceData"]["complianceFeatures"];
                                                        content = template(viewModel);
                                                        $("#policy-list-container").html(content);
                                                    } else {
                                                        viewModel["compliance"] = "COMPLIANT";
                                                        content = template(viewModel);
                                                        $("#policy-list-container").html(content);
                                                        $("#policy-compliance-table").addClass("hidden");
                                                    }
                                                } else {
                                                    $("#policy-list-container").
                                                    html("<div class='panel-body'><br><p class='fw-warning'> This device " +
                                                        "has no policy applied.<p></div>");
                                                }
                                            }
                                        },
                                        // error-callback
                                        function () {
                                            $("#policy-list-container").
                                            html("<div class='panel-body'><br><p class='fw-warning'> Loading policy compliance related data " +
                                                "was not successful. please try refreshing data in a while.<p></div>");
                                        }
                                    );
                                }
                            }
                        }
                    },
                    // error-callback
                    function () {
                        $("#policy-list-container").
                        html("<div class='panel-body'><br><p class='fw-warning'> Loading policy compliance related data " +
                            "was not successful. please try refreshing data in a while.<p></div>");
                    }
                );
            }
        );
    }
