/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
    var listPartialSrc = $("#list-partial").attr("src");
    var treeTemplateSrc = $("#tree-template").attr("src");
    var roleName = $("#permissionList").data("currentrole");
    var serviceUrl = "/devicemgt_admin/roles/" + roleName;
    $.registerPartial("list", listPartialSrc, function(){
        $.template("treeTemplate", treeTemplateSrc, function (template) {
            invokerUtil.get(serviceUrl,
                function(data){
                    data = JSON.parse(data);
                    var treeData = data.responseContent.permissionList;
                    if(treeData.nodeList.length > 0){
                        treeData = { nodeList: treeData.nodeList };
                        var content = template(treeData);
                        $("#permissionList").html(content);
                        $("#permissionList").on("click", ".permissionTree .permissionItem", function(){
                            $(this).closest("li").find("li input").each(function(){
                                var check = $(this).prop('checked');
                                check = !check;
                                $(this).prop('checked', check);
                            });
                        });
                    }
                    $('#permissionList').tree_view();
                }, function(message){
                    console.log(message);
                });
        });
    });
    /**
     * Following click function would execute
     * when a user clicks on "Add Role" button
     * on Add Role page in WSO2 Devicemgt Console.
     */
    $("button#update-permissions-btn").click(function() {
        var roleName = $("#permissionList").data("currentrole");
        var updateRolePermissionAPI = "/devicemgt_admin/roles/" + roleName;
        var updateRolePermissionData = {};
        var perms = [];
        $("#permissionList li input:checked").each(function(){
            perms.push($(this).data("resourcepath"));
        })
        updateRolePermissionData.permissions = perms;
        invokerUtil.put(
            updateRolePermissionAPI,
            updateRolePermissionData,
            function (data, status, jqXHR) {
                if (jqXHR.status == 200) {
                    // Refreshing with success message
                    $("#role-create-form").addClass("hidden");
                    $("#role-created-msg").removeClass("hidden");
                }
            }, function () {
                $(errorMsg).text("An unexpected error occurred. Please try again later.");
                $(errorMsgWrapper).removeClass("hidden");
            }
        );
    });
});