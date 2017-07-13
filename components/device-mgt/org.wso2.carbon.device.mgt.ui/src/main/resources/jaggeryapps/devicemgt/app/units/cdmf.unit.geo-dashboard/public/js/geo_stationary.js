/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

function initStationaryAlert() {
    // var serverUrl = "/portal/store/carbon.super/fs/gadget/geo-dashboard/controllers/get_alerts.jag?executionPlanType=Stationery&deviceId=" + deviceId;
    var serverUrl = "/api/device-mgt/v1.0/geo-services/alerts/Stationery/" + deviceType + "/" + deviceId;

    $(".removeGeoFence").tooltip();
    invokerUtil.get(serverUrl, function (response) {
        if (response) {
            response = JSON.parse(response);
            $(".fence-not-exist").hide();
            for (var index in response) {
                var alert = response[index];
                $("#stationary-alert-table > tbody").append(
                    "<tr class='viewGeoFenceRow'style='cursor: pointer' data-stationeryTime='" + alert.stationaryTime +
                    "'data-fluctuationRadius='" + alert.fluctuationRadius + "'data-areaName='" + alert.areaName +
                    "'data-queryName='" + alert.queryName + "'data-geoJson=" + alert.geoJson + ">" +
                    "<td>" + alert.areaName + "</td><td>" + alert.stationaryTime + "</td><td>" + alert.fluctuationRadius +
                    "<td>" + alert.queryName + "</td><td>" + formatDate(new Date(alert.createdTime)) + "</td><td" +
                    " onClick=removeGeoFence(this.parentElement,'Stationery') data-toggle=" +
                    " 'tooltip' title='Remove fence' ><i class='fa fa-trash-o'></i></td></tr>")
            }
        } else{
            $(".fence-not-exist").show();
            $("#stationary-alert-table").hide();
        }
        $('.viewGeoFenceRow td:not(:last-child)').click(function () {
            viewFence(this.parentElement,'Stationery');
        });
    });
}
initStationaryAlert();