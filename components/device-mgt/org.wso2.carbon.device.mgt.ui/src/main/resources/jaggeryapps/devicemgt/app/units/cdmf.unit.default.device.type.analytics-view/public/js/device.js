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

var InitiateViewOption = null;
var deviceId = null;
var deviceType = null;
var fromTime = null;
var toTime = null;
var keys = null;

function drawTable(from, to) {
	var device = $("#device-details");
    deviceId = device.data("deviceid");
	deviceType = device.data("devicetype");
	keys = device.data("attributes").split(",");
	fromTime = from * 1000;
	toTime = to * 1000;
	if ( $.fn.dataTable.isDataTable( '#stats-table' ) ) {
		var table = $('#stats-table').DataTable();
		table.clear().draw();
		table.ajax.reload();
	}
	else {
		$("#stats-table").datatables_extended({
			serverSide: true,
			processing: false,
			searching: false,
			ordering: false,
			pageLength: 100,
			order: [],
			ajax: {
				url: "/devicemgt/api/stats/paginate",
				data: buildAjaxData
			}
		});
	}
}

function buildAjaxData (){
	var settings = $("#stats-table").dataTable().fnSettings();

	var obj = {
		//default params
		"draw" : settings.iDraw,
		"start" : settings._iDisplayStart,
		"length" : settings._iDisplayLength,
		"columns" : "",
		"order": "",
		"deviceType" : deviceType,
		"deviceId" : deviceId,
		"from": fromTime,
		"to"  : toTime,
		"attributes" : JSON.stringify(keys)
	};

	return obj;


}