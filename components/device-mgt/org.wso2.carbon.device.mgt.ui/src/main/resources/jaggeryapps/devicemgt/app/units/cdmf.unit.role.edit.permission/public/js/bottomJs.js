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

/**
 * Tree view function
 * @return {Null}
 */
var modalPopup = ".modal";
var modalPopupContent = modalPopup + " .modal-content";
var errorMsgWrapper = "#permission-add-error-msg";
var errorMsg = "#permission-add-error-msg span";

var apiBasePath = "/api/device-mgt/v1.0";

/*
 * hide popup function.
 */
function hidePopup() {
    $(modalPopupContent).html('');
    $(modalPopup).modal('hide');
    $('body').removeClass('modal-open').css('padding-right','0px');
    $('.modal-backdrop').remove();
}

/*
 * show popup function.
 */
function showPopup() {
    $(modalPopup).modal('show');
    //setPopupMaxHeight();
}
$.fn.tree_view = function(){
    var tree = $(this);
    tree.find('li').has("ul").each(function () {
        var branch = $(this); //li with children ul
        branch.prepend('<i class="icon"></i>');
        branch.addClass('branch');
        branch.on('click', function (e) {
            if (this == e.target) {
                var icon = $(this).children('i:first');
                icon.closest('li').toggleAttr('aria-expanded', 'true', 'false');
            }
        });
    });

    tree.find('.branch .icon').each(function(){
        $(this).on('click', function () {
            $(this).closest('li').click();
        });
    });

    tree.find('.branch > a').each(function () {
        $(this).on('click', function (e) {
            $(this).closest('li').click();
            e.preventDefault();
        });
    });

    tree.find('.branch > button').each(function () {
        $(this).on('click', function (e) {
            $(this).closest('li').click();
            e.preventDefault();
        });
    });
};

$.fn.toggleAttr = function (attr, val, val2) {
    return this.each(function () {
        var self = $(this);
        if (self.attr(attr) == val) self.attr(attr, val2); else self.attr(attr, val);
    });
};
$(document).ready(function () {

    if(get('wizard') == 'true') {
        $("#role_wizard_header").removeClass("hidden");
    }

    var listPartialSrc = $("#list-partial").attr("src");
    var treeTemplateSrc = $("#tree-template").attr("src");
    var roleName = $("#permissionList").data("currentrole");
    var userStore;
    if (roleName.indexOf('/') > 0) {
        userStore = roleName.substr(0, roleName.indexOf('/'));
        roleName = roleName.substr(roleName.indexOf('/') + 1);
    }
    var serviceUrl = apiBasePath + "/roles/" +encodeURIComponent(roleName)+"/permissions";
    if (userStore) {
        serviceUrl += "?user-store=" + userStore;
    }
    $.registerPartial("list", listPartialSrc, function(){
        $.template("treeTemplate", treeTemplateSrc, function (template) {
            invokerUtil.get(serviceUrl,
                function(data){
                    data = JSON.parse(data);
                    var treeData = data;
                    if(treeData.nodeList.length > 0){
                        treeData = { nodeList: treeData.nodeList };
                        var content = template(treeData);
                        $("#permissionList").html(content);
                        $("#permissionList").on("click", ".permissionTree .permissionItem", function(){
                            var parentValue = $(this).prop('checked');
                            $(this).closest("li").find("li input").each(function () {
                                $(this).prop('checked',parentValue);
                            });
                        });
                    }
                    $("#permissionList li input").click(function(){
                        var parentInput = $(this).parents("ul:eq(1) > li").find('input:eq(0)');
                        if(parentInput && parentInput.is(':checked')){
                            $(modalPopupContent).html($('#child-deselect-error-content').html());
                            showPopup();
                            $("a#child-deselect-error-link").click(function () {
                                hidePopup();
                            });
                            return false;
                        }
                    });
                    $('#permissionList').tree_view();
                }, function(message){
                    console.log(message);
                });
        });
    });

    /**
     * Following click function would execute
     * when a user clicks on "Add Role" button
     * on Add Role page in WSO2 MDM Console.
     */
    $("button#update-permissions-btn").click(function() {
        var roleName = $("#permissionList").data("currentrole");
        var userStore;
        if (roleName.indexOf('/') > 0) {
            userStore = roleName.substr(0, roleName.indexOf('/'));
            roleName = roleName.substr(roleName.indexOf('/') + 1);
        }
        var updateRolePermissionAPI = apiBasePath + "/roles/" + roleName;
        var updateRolePermissionData = {};
        var perms = [];
        $("#permissionList li input:checked").each(function(){
            perms.push($(this).data("resourcepath"));
        });
        if (userStore) {
            updateRolePermissionAPI += "?user-store=" + userStore;
            updateRolePermissionData.roleName = userStore + "/" + roleName;
        } else {
            updateRolePermissionData.roleName = roleName;
        }
        updateRolePermissionData.permissions = perms;
        invokerUtil.put(
            updateRolePermissionAPI,
            updateRolePermissionData,
            function (data, textStatus, jqXHR) {
                if (jqXHR.status == 200) {
                    // Refreshing with success message
                    $("#role-create-form").addClass("hidden");
                    $("#role-created-msg").removeClass("hidden");
                }
            }, function (data) {
                var payload = JSON.parse(data.responseText);
                $(errorMsg).text(payload.message);
                $(errorMsgWrapper).removeClass("hidden");
            }
        );
    });
});

function get(name){
    if(name=(new RegExp('[?&]'+encodeURIComponent(name)+'=([^&]*)')).exec(location.search))
        return decodeURIComponent(name[1]);
}
