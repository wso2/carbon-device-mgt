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

	var addButton = $('.add_button'); //Add button selector
	var wrapper = $('.attribute_field_wrapper'); //Input field wrapper
	var fieldHTML = $('#add-attribute-field').html(); //New input field html
	$(addButton).click(function(){ //Once add button is clicked
		$(wrapper).append(fieldHTML); // Add field html
	});
	$(wrapper).on('click', '.remove_button', function(e){ //Once remove button is clicked
		e.preventDefault();
		$(this).parent('div').remove(); //Remove field html
	});

	var addOperationButton = $('.add_operation_button'); //Add button selector
	var operationWrapper = $('.operation_field_wrapper'); //Input field wrapper
	var operationFieldHTML = $('#add-operation-field').html(); //New input field html
	$(addOperationButton).click(function(){ //Once add button is clicked
		$(operationWrapper).append(operationFieldHTML); // Add field html
	});
	$(operationWrapper).on('click', '.remove_operation_button', function(e){ //Once remove button is clicked
		e.preventDefault();
		$(this).parent('div').remove();
	});


	var addFeatureButton = $('.add_feature_button'); //Add button selector
	var featureWrapper = $('.feature_field_wrapper'); //Input field wrapper
	$(addFeatureButton).click(function(){ //Once add button is clicked
		var featureFieldHtml = '<div class="row"><div class="dontfloat feature-wrapper" name ="deviceFeature"> <div class="col-xs-3"> <input type="text"' +
				' class="form-control" id="feature-name" placeholder="name"/> </div> <div class="col-xs-4"> ' +
				'<input type="text" class="form-control" id="feature-code" placeholder="code"/> </div> ' +
				'<div class="col-xs-4"> <textarea aria-describedby="basic-addon1" type="text" ' +
				'id="feature-description" placeholder="description"data-error-msg="invalid ' +
				'feature description"class="form-control" rows="1" cols="30"></textarea> </div> ' +
				'<button type="button" class="wr-btn wr-btn-horizontal wr-btn-secondary remove_feature_button"><i class="fa fa-minus"></i></button> </div></div>'
		$(featureWrapper).append(featureFieldHtml); // Add field html

	});
	$(featureWrapper).on('click', '.remove_feature_button', function(e){ //Once remove button is clicked
		e.preventDefault();
		$(this).parent('div').remove(); //Remove field html
		op--; //Decrement field counter
	});

    /**
     * Following click function would execute
     * when a user clicks on "Add Device type" button.
     */
    $("button#add-devicetype-btn").click(function () {

		var errorMsgWrapper = "#devicetype-create-error-msg";
		var errorMsg = "#devicetype-create-error-msg span";
		var successMsgWrapper = "#devicetype-create-success-msg";
		var successMsg = "#devicetype-create-success-msg span";
		var deviceType = {};
		var deviceTypeName = $("#deviceTypeName").val();
		var deviceTypeDescription = $("#deviceTypeDescription").val();
		if (!deviceTypeName || deviceTypeName.trim() == "" ) {
			$(errorMsg).text("Device Type Name Cannot be empty.");
			$(errorMsgWrapper).removeClass("hidden");
			return;
		}

		if (!deviceTypeDescription || deviceTypeDescription.trim() == "" ) {
			$(errorMsg).text("Device Type Description Cannot be empty.");
			$(errorMsgWrapper).removeClass("hidden");
			return
		}

		deviceType.name = deviceTypeName.trim();
		deviceType.deviceTypeMetaDefinition = {}
		deviceType.deviceTypeMetaDefinition.description = deviceTypeDescription.trim();

		var pushNotification = $("#pushNotification").val();
		if (pushNotification != "NONE") {
			deviceType.deviceTypeMetaDefinition.pushNotificationConfig = {};
			deviceType.deviceTypeMetaDefinition.pushNotificationConfig.scheduled = true;
			deviceType.deviceTypeMetaDefinition.pushNotificationConfig.type = pushNotification;
		}

		var propertyValues = [];
		$('input[name^="attribute"]').each(function() {
			var propertyValue = $(this).val();
			if (propertyValue.trim() != "") {
				propertyValues.push(propertyValue.trim());
			}
		});
		deviceType.deviceTypeMetaDefinition.properties = propertyValues;

		var operationValues = [];
		$('input[name^="operation"]').each(function() {
			var operationValue = $(this).val();
			if (operationValue.trim() != "") {
				operationValues.push(operationValue.trim());
			}
		});
		if (operationValues.length > 0) {
			deviceType.deviceTypeMetaDefinition.initialOperationConfig = {};
			deviceType.deviceTypeMetaDefinition.initialOperationConfig.operations = operationValues;
		}

		var features = [];
		$('div[name^="deviceFeature"]').each(function() {
			var featureName = $(this).find("#feature-name").val();
			var featureCode = $(this).find("#feature-code").val();
			if (featureName && featureName.trim() != "" && featureCode && featureCode.trim() != "") {
				var feature = {};
				feature.name = featureName.trim();
				feature.code = featureCode.trim();
				feature.description = $("#feature-description").val();
				features.push(feature);
			}
		});
		deviceType.deviceTypeMetaDefinition.features = features;

		var addRoleAPI = apiBasePath + "/admin/device-types";

		invokerUtil.put(
		        addRoleAPI,
			    deviceType,
		        function (data, textStatus, jqXHR) {
		            if (jqXHR.status == 200) {
						$("#modalDevice").modal('show');
		            }
		        },
		        function (jqXHR) {
		            if (jqXHR.status == 500) {
		                $(errorMsg).text("Unexpected error.");
		                $(errorMsgWrapper).removeClass("hidden");
		            }

					if (jqXHR.status == 409) {
						$(errorMsg).text("Device type already exists");
						$(errorMsgWrapper).removeClass("hidden");
					}
		        }
		    );
    });

});