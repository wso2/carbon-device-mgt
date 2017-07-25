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

function initializeWithin() {
    $(".removeGeoFence").tooltip();
    var serverUrl = "/api/device-mgt/v1.0/geo-services/alerts/Within/" + deviceType + "/" + deviceId;
    invokerUtil.get(serverUrl, function (response) {
        if (response) {
            $(".fence-not-exist").hide();
            response = JSON.parse(response);
            for (var index in response) {
                var alertBean = response[index];
                $("#within-alert > tbody").append(
                    "<tr class='viewGeoFenceRow' style='cursor: pointer' data-areaName='" + alertBean.areaName  +
                    "' data-queryName='" + alertBean.queryName + "'data-geoJson="+ alertBean.geoJson +"><td>" + alertBean.areaName  + "</td>" +
                    "<td>" + alertBean.queryName + "</td><td>" + formatDate(new Date(alertBean.createdTime)) + "</td>" +
                    "<td onClick=removeGeoFence(this.parentElement,'Within') class='removeGeoFence'" +
                    " data-toggle='tooltip' title='Remove fence' ><i class='fa fa-trash-o'></i></td></tr>");
            }
        } else{
            $(".fence-not-exist").show();
            $("#within-alert").hide();
        }
        $('.viewGeoFenceRow td:not(:last-child)').click(function () {
            viewFence(this.parentElement,'WithIn');
        });
    });
}
initializeWithin();