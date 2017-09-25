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

$(document).ready(function() {
    $(".panel-body").removeClass("hidden");
    $("#loading-content").remove();

    if ($('#event_log').length) {
        loadOperationsLog();
    }

    if ($('#policy_compliance').length) {
        loadPolicyCompliance();
    }

    $("#refresh-policy").click(function() {
        $('#policy-spinner').removeClass('hidden');
        loadPolicyCompliance();
    });

    $("#refresh-operations").click(function() {
        $('#operations-spinner').removeClass('hidden');
        loadOperationsLog(true);
    });

    $("#refresh-apps").click(function() {
        $('#apps-spinner').removeClass('hidden');
        loadApplicationsList();
    });

});

function getLogStatusIcon(entry) {
    switch (entry) {
        case 'COMPLETED':
            return 'fw-success';
        case 'PENDING':
            return 'fw-pending';
        case 'ERROR':
            return 'fw-error';
        default:
            return 'fw-info'
    }
}

function loadOperationsLog(update) {
    var operationsLogTable = "#operation-log";
    if (update) {
        operationTable = $(operationsLogTable).DataTable();
        $("#operations-spinner").removeClass("hidden");
        operationTable.ajax.reload(function(json) {
            $("#operations-spinner").addClass("hidden");
        }, false);
        return;
    }
    var table = $('#operation-log').DataTable({
        serverSide: true,
        processing: false,
        searching: false,
        ordering: false,
        pageLength: 10,
        order: [],
        autoWidth: false,
        ajax: {
            url: "/devicemgt/api/operation/paginate",
            data: {
                deviceId: deviceIdentifier,
                deviceType: deviceType,
                owner: deviceOwner
            },
            dataSrc: function(json) {
                $("#operations-spinner").addClass("hidden");
                return json.data;
            }
        },
        columnDefs: [{
                targets: 0,
                data: "code",
                class: "icon-only content-fill"
            },
            {
                targets: 1,
                data: "createdTimeStamp",
                class: "text-right",
                render: function(date) {
                    var value = String(date);
                    return value.slice(0, 16);
                }
            },
            {
                targets: 2,
                data: "status",
                class: "text-right extended-log-data log-record-status",
                render: function(data, type, full, meta) {
                    return '<i class="icon fw ' + getLogStatusIcon(data) +  '"> </i><span> ' + data + ' </span><i class="icon fw fw-down"></i>';
                },
                width: "100%"
            }
        ],
        fnCreatedRow: function(nRow, aData, iDataIndex) {
            $('td:eq(0)', nRow)
                .attr('data-search', aData.Device_Type)
                .attr('data-display', aData.Device_Type)
                .addClass(' icon-only content-fill');

            $('td:eq(1), td:eq(2)', nRow).addClass('text-right');
            $('td:eq(2)', nRow).addClass('log-record-status')

        }
    });

    $('#operation-log tbody').on('click', 'td.extended-log-data', function() {
        var tr = $(this).closest('tr');
        var row = table.row(tr);
        var rowData = row.data()
        var deviceid = $('.device-id').data('deviceid');
        var deviceType = $('.device-id').data('type');
        var uri = "/api/device-mgt/v1.0/activities/" + rowData.activityId + "/" + deviceType + "/" + deviceid;
        var contentType = "application/json";
        var index = row[0][0];

        if (row.child.isShown()) {
            row.child.hide();
            tr.find('i.fw-up').removeClass('fw-up').addClass('fw-down');
            $(row.child()).removeClass('log-data-row');
            tr.removeClass('shown');
        } else {
            invokerUtil.get(uri,(payload) => {
                //update the parent status
                var payloadObject = JSON.parse(payload);
                if ( payloadObject["activityStatus"][0]["status"] != rowData["status"] ) {
                    rowData["status"] = payloadObject["activityStatus"][0]["status"];
                    $('#operation-log').dataTable().fnUpdate(rowData,index,undefined,false);
                }

                row.child(renderLogDetails(row.data(),payload)).show();
                tr.find('i.fw-down').removeClass('fw-down').addClass('fw-up');
                $(row.child()).addClass('log-data-row');
                tr.addClass('shown');
            },(error) => {

            },contentType);
        }

    });

    $("a[data-toggle=\"tab\"]").on("shown.bs.tab", function (e) {
        $("#operation-log").DataTable().columns.adjust().responsive.recalc();
    });

    function renderLogDetails(obj,data) {
        var payload = JSON.parse(data);
        var logStream = '<div class="log-data">';
        var activityStatus = payload.activityStatus;
        var responseMsg = null;

        if (activityStatus['0'].status == "ERROR") {
            responseMsg = activityStatus['0'].responses['0'].response;
        }

        Object.entries(activityStatus).forEach(
            ([key, entry]) => {
                logStream += '<div class="row log-entry">' +
                    '<div class="col-lg-8">' +
                    '<div class="log-status"><i class="icon fw ' + getLogStatusIcon(entry.status) + ' "></i>' +
                    '<span>' + ((responseMsg == null) ? entry.status : responseMsg) + '</span></div>' +
                    '</div>' +
                    '<div class="col-lg-4">' +
                    '<div class="log-time text-right"><span>' + entry.updatedTimestamp + '</span></div>' +
                    '</div>' +
                    '</div>';
            }
        );
        logStream += '</div></div>';
        return logStream;

        function getLogStatusIcon(entry) {
            switch (entry) {
                case 'COMPLETED':
                    return 'fw-success';
                case 'PENDING':
                    return 'fw-pending';
                case 'ERROR':
                    return 'fw-error';
                default:
                    return 'fw-info'
            }
        }
    }
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
        function(template) {
            var getEffectivePolicyURL = "/api/device-mgt/v1.0/devices/" + deviceType + "/" + deviceId + "/effective-policy";
            var getDeviceComplianceURL = "/api/device-mgt/v1.0/devices/" + deviceType + "/" + deviceId + "/compliance-data";
            invokerUtil.get(
                getEffectivePolicyURL,
                // success-callback
                function(data, textStatus, jqXHR) {
                    if (jqXHR.status == 200) {
                        $("#policy-spinner").addClass("hidden");
                        if (data) {
                            data = JSON.parse(data);
                            if (data["active"] == true) {
                                activePolicy = data;
                                invokerUtil.get(
                                    getDeviceComplianceURL,
                                    // success-callback
                                    function(data, textStatus, jqXHR) {
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
                                    function() {
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
                function() {
                    $("#policy-list-container").
                    html("<div class='panel-body'><br><p class='fw-warning'> Loading policy compliance related data " +
                        "was not successful. please try refreshing data in a while.<p></div>");
                }
            );
        }
    );
}

function loadApplicationsList() {
    var applicationsList = $("#applications-list");
    var applicationListingTemplate = applicationsList.attr("src");
    var deviceId = applicationsList.data("device-id");
    var deviceType = applicationsList.data("device-type");

    $.template("application-list", applicationListingTemplate, function (template) {
        var serviceURL = "/api/device-mgt/v1.0/devices/" + deviceType + "/" + deviceId + "/applications";
        invokerUtil.get(
            serviceURL,
            // success-callback
            function (data, textStatus, jqXHR) {
                if (jqXHR.status == 200 && data) {
                    data = JSON.parse(data);
                    $("#apps-spinner").addClass("hidden");
                    if (data.length > 0) {
                        for (var i = 0; i < data.length; i++) {
                            data[i]["name"] = decodeURIComponent(data[i]["name"]);
                            data[i]["platform"] = deviceType;
                        }

                        var viewModel = {};
                        viewModel["applications"] = data;
                        viewModel["deviceType"] = deviceType;
                        viewModel["deviceId"] = deviceId;
                        viewModel["appContext"] = context;
                        var content = template(viewModel);
                        $("#applications-list-container").html(content);
                        var iconSource = $("#applications-list-container").data("public-uri") + "/img/android_app_icon.png";
                        $("#applications-list-container img").attr("src",iconSource);
                    } else {
                        $("#applications-list-container").html("<div class='message message-info'><h4><i class='icon fw fw-info'></i>No applications found.</h4>" +
                            "<p>Please try refreshing the list in a while.</p></div>");
                    }
                }
            },
            // error-callback
            function () {
                $("#applications-list-container").html("<div class='panel-body'><br><p class='fw-warning'>&nbsp;Loading application list " +
                    "was not successful. please try refreshing the list in a while.<p></div>");
            });
    });
}
