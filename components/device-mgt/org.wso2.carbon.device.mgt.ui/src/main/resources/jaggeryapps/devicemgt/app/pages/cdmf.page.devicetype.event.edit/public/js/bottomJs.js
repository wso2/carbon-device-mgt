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

/**
 * Checks if provided input is valid against RegEx input.
 *
 * @param regExp Regular expression
 * @param inputString Input string to check
 * @returns {boolean} Returns true if input matches RegEx
 */
function inputIsValid(regExp, inputString) {
    regExp = new RegExp(regExp);
    return regExp.test(inputString);
}

var validateInline = {};
var clearInline = {};

var apiBasePath = "/api/device-mgt/v1.0";
var domain = $("#domain").val();


var enableInlineError = function (inputField, errorMsg, errorSign) {
    var fieldIdentifier = "#" + inputField;
    var errorMsgIdentifier = "#" + inputField + " ." + errorMsg;
    var errorSignIdentifier = "#" + inputField + " ." + errorSign;

    if (inputField) {
        $(fieldIdentifier).addClass(" has-error has-feedback");
    }

    if (errorMsg) {
        $(errorMsgIdentifier).removeClass(" hidden");
    }

    if (errorSign) {
        $(errorSignIdentifier).removeClass(" hidden");
    }
};

var disableInlineError = function (inputField, errorMsg, errorSign) {
    var fieldIdentifier = "#" + inputField;
    var errorMsgIdentifier = "#" + inputField + " ." + errorMsg;
    var errorSignIdentifier = "#" + inputField + " ." + errorSign;

    if (inputField) {
        $(fieldIdentifier).removeClass(" has-error has-feedback");
    }

    if (errorMsg) {
        $(errorMsgIdentifier).addClass(" hidden");
    }

    if (errorSign) {
        $(errorSignIdentifier).addClass(" hidden");
    }
};

function formatRepo(user) {
    if (user.loading) {
        return user.text
    }
    if (!user.username) {
        return;
    }
    var markup = '<div class="clearfix">' +
        '<div class="col-sm-8">' +
        '<div class="clearfix">' +
        '<div class="col-sm-4">' + user.username + '</div>';
    if (user.name || user.name != undefined) {
        markup += '<div class="col-sm-8"> ( ' + user.name + ' )</div>';
    }
    markup += '</div></div></div>';
    return markup;
}

function formatRepoSelection(user) {
    return user.username || user.text;
}



$(document).ready(function () {

	var appContext = $("#app-context").data("app-context");
	var addEventButton = $('.add_event_button'); //Add button selector
	var eventWrapper = $('.event_field_wrapper'); //Input field wrapper
	$(addEventButton).click(function(){ //Once add button is clicked
		var eventFieldHtml = '<div class="row"><div class="dontfloat event-wrapper" name="deviceEvent"> ' +
			'<div class="col-xs-3"> <input type="text" class="form-control" id="event-name" placeholder="name"/> ' +
			'</div> <div class="col-xs-4"> <select class="form-control select" id="event-type"> ' +
			'<option>STRING</option> <option>LONG</option> <option>BOOL</option> <option>INT</option> <option>FLOAT</option> ' +
			'<option>DOUBLE</option> </select> </div> ' +
				'<button type="button" class="wr-btn wr-btn-horizontal wr-btn-secondary remove_event_button"><i class="fa fa-minus"></i></button> </div></div>'
		$(eventWrapper).append(eventFieldHtml); // Add field html

	});
	$(eventWrapper).on('click', '.remove_event_button', function(e){ //Once remove button is clicked
		e.preventDefault();
		$(this).parent('div').remove();
	});

    /**
     * Following click function would execute
     * when a user clicks on "Add Device type" button.
     */
    $("button#add-event-btn").click(function () {

		var errorMsgWrapper = "#devicetype-create-error-msg";
		var errorMsg = "#devicetype-create-error-msg span";
		var successMsgWrapper = "#devicetype-create-success-msg";
		var successMsg = "#devicetype-create-success-msg span";
		var deviceTypeEvent = {};
		var deviceTypeName = $("#deviceTypeName").val();
		var deviceTypeDescription = $("#deviceTypeDescription").val();
		if (!deviceTypeName || deviceTypeName.trim() == "" ) {
			$(errorMsg).text("Device Type Name Cannot be empty.");
			$(errorMsgWrapper).removeClass("hidden");
			return;
		}


		deviceTypeEvent.eventAttributes = {};

		deviceTypeEvent.transport = $("#transport").val();

		var attributes = [];
		$('div[name^="deviceEvent"]').each(function() {
			var eventName = $(this).find("#event-name").val();
			var eventType = $(this).find("#event-type").val();
			if (eventName && eventName.trim() != "" && eventType && eventType.trim() != "" && eventName != "deviceId") {
				var attribute = {};
				attribute.name = eventName.trim();
				attribute.type = eventType.trim();
				attributes.push(attribute);
			}
		});
		deviceTypeEvent.eventAttributes.attributes = attributes;

		var addEventsAPI = apiBasePath + "/events/" + deviceTypeName;

		invokerUtil.post(
			addEventsAPI,
			deviceTypeEvent,
		        function (data, textStatus, jqXHR) {
		            if (jqXHR.status == 200) {
						$("#modalDevice").modal('show');
		            }
		        },
		        function (jqXHR) {
		            if (jqXHR.status == 500) {
		                $(errorMsg).text("Failed to deploy event definition, Please Contact Administrator");
		                $(errorMsgWrapper).removeClass("hidden");
		            }

					if (jqXHR.status == 409) {
						$(errorMsg).text("Device type definition cannot be updated");
						$(errorMsgWrapper).removeClass("hidden");
					}
		        }
		    );
    });



});