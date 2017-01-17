/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var modalPopup = ".modal";
var modalPopupContainer = modalPopup + " .modal-content";
var modalPopupContent = modalPopup + " .modal-content";

var emmAdminBasePath = "/api/device-mgt/v1.0";

/*
 * set popup maximum height function.
 */
function setPopupMaxHeight() {
    var maxHeight = "max-height";
    var marginTop = "margin-top";
    var body = "body";
    $(modalPopupContent).css(maxHeight, ($(body).height() - ($(body).height() / 100 * 30)));
    $(modalPopupContainer).css(marginTop, (-($(modalPopupContainer).height() / 2)));
}

/*
 * show popup function.
 */
function showPopup() {
    $(modalPopup).modal('show');
}

/*
 * hide popup function.
 */
function hidePopup() {
    $(modalPopupContent).html("");
    $(modalPopupContent).removeClass("operation-data");
    $(modalPopup).modal('hide');
    $('body').removeClass('modal-open').css('padding-right','0px');
    $('.modal-backdrop').remove();
}

var updateNotificationCount = function (data, textStatus, jqXHR) {
    if (jqXHR.status == 200 && data) {
        var responsePayload = JSON.parse(data);
        var newNotificationsCount = responsePayload.count;
        if (newNotificationsCount > 0) {
            $("#notification-bubble").html(newNotificationsCount);
        }
//        } else {
//            $("#notification-bubble").html("Error");
//        }
    }
};

function loadNotificationsPanel() {
    if ("true" == $("#right-sidebar").attr("is-authorized")) {
        var serviceURL = emmAdminBasePath + "/notifications?status=NEW";
        invokerUtil.get(serviceURL, updateNotificationCount, hideNotificationCount);
        loadNewNotifications();
    } else {
        $("#notification-bubble-wrapper").remove();
    }
}

function hideNotificationCount(jqXHR) {
    if (jqXHR.status == 404) {
        // this means "no new notifications to show"
        $("#notification-bubble").hide();
    } else {
        $("#notification-bubble").html("Error");
    }
}

function loadNewNotifications() {
    var messageSideBar = ".sidebar-messages";
    if ($("#right-sidebar").attr("is-authorized") == "true") {
        var notifications = $("#notifications");
        var currentUser = notifications.data("currentUser");

        $.template("notification-listing", notifications.attr("src"), function (template) {
            var serviceURL = emmAdminBasePath + "/notifications?status=NEW";

            var successCallback = function (data, textStatus, jqXHR) {
                if (jqXHR.status == 200 && data) {
                    var viewModel = {};
                    var responsePayload = JSON.parse(data);

                    if (responsePayload.notifications) {
                        viewModel.notifications = responsePayload.notifications;
                        if (responsePayload.count > 0) {
                            $(messageSideBar).html(template(viewModel));
                        } else {
                            $(messageSideBar).html(
                                "<h4 class='text-center'>No New Notifications</h4>" +
                                "<h5 class='text-center text-muted'>" +
                                    "Check this section for error notifications<br>related to device operations" +
                                "</h5>"
                            );
                        }
                    } else {
                        $(messageSideBar).html("<h4 class ='message-danger text-center'>Unexpected error occurred while loading new notifications</h4>");
                    }
                }
            };
            var errorCallback = function (jqXHR) {
                if (jqXHR.status = 500) {
                    $(messageSideBar).html("<h4 class ='message-danger text-center'>Unexpected error occurred while trying " +
                        "to retrieve any new notifications</h4>");
                }
            };
            invokerUtil.get(serviceURL, successCallback, errorCallback);
        });
    } else {
        $(messageSideBar).html("<h4 class ='message-danger text-center'>You are not authorized to view notifications</h4>");
    }
}

/**
 * Sidebar function
 * @return {Null}
 */
$.sidebar_toggle = function(action, target, container) {
    var elem = '[data-toggle=sidebar]',
        button,
        container,
        conrainerOffsetLeft,
        conrainerOffsetRight,
        target,
        targetOffsetLeft,
        targetOffsetRight,
        targetWidth,
        targetSide,
        relationship,
        pushType,
        buttonParent;

    /**
     * Dynamically adjust the height of sidebar to fill parent
     */
    function sidebarHeightAdjust(){
        $('.sidebar-wrapper').each(function(){
            var elemOffsetBottom = $(this).data('offset-bottom'),
                scrollBottom = ($(document).height() - $(window).height()),
                offesetBottom = 0,
                getBottomOffset = elemOffsetBottom - (scrollBottom - ($(window).scrollTop()-elemOffsetBottom) - elemOffsetBottom);

            if(getBottomOffset > 0){
                offesetBottom = getBottomOffset;
            }

            $(this).height(($(window).height() - ($(this).offset().top - $(window).scrollTop())) - offesetBottom);

            if((typeof $.fn.nanoScroller == 'function') && ($('.nano-content', this).length > 0)){
                $(".nano-content").parent()[0].nanoscroller.reset();
            }
        });
    };

    var sidebar_window = {
        update: function(target, container, button){
            conrainerOffsetLeft = $(container).data('offset-left') ? $(container).data('offset-left') : 0,
                conrainerOffsetRight = $(container).data('offset-right') ? $(container).data('offset-right') : 0,
                targetTop = $(target).data('top') ? $(target).data('top') : 0,
                targetOffsetLeft = $(target).data('offset-left') ? $(target).data('offset-left') : 0,
                targetOffsetRight = $(target).data('offset-right') ? $(target).data('offset-right') : 0,
                targetWidth = $(target).data('width'),
                targetSide = $(target).data("side"),
                pushType = $(container).parent().is('body') == true ? 'padding' : 'padding'; //TODO: Remove if works everywhere

            $(container).addClass('sidebar-target');

            if(button !== undefined){
                relationship = button.attr('rel') ? button.attr('rel') : '';
                buttonParent = $(button).parent();
            }

            $(target).css('top', targetTop);

            sidebarHeightAdjust();
        },
        show: function(){
            if($(target).data('sidebar-fixed') == true) {
                $(target).css('top',$(target).data('fixed-offset') + 'px');
                $(target).height($(window).height() - $(target).data('fixed-offset'));
            }

            $(target).off('webkitTransitionEnd otransitionend oTransitionEnd msTransitionEnd transitionend');
            $(target).trigger('show.sidebar');

            if(targetWidth !== undefined) {
                $(target).css('width', targetWidth);
            }

            $(target).addClass('toggled');

            if(button !== undefined){
                if(relationship !== ''){
                    // Removing active class from all relative buttons
                    $(elem+'[rel='+relationship+']:not([data-handle=close])').removeClass("active");
                    $(elem+'[rel='+relationship+']:not([data-handle=close])').attr('aria-expanded', 'false');
                }

                // Adding active class to button
                if(button.attr('data-handle') !== 'close'){
                    button.addClass("active");
                    button.attr('aria-expanded', 'true');
                }

                if(buttonParent.is('li')) {
                    if(relationship !== ''){
                        $(elem+'[rel='+relationship+']:not([data-handle=close])').parent().removeClass("active");
                        $(elem+'[rel='+relationship+']:not([data-handle=close])').parent().attr('aria-expanded', 'false');
                    }
                    buttonParent.addClass("active");
                    buttonParent.attr('aria-expanded', 'true');
                }
            }

            // Sidebar open function
            if (targetSide == 'left'){
                if ($(target).attr('data-container-divide')){
                    $(container).css(pushType+'-'+targetSide, targetWidth + targetOffsetLeft);
                    $(target).css(targetSide, targetOffsetLeft);
                }
                else if ($(target).attr('data-container-push')){
                    $(container).css(targetSide,  Math.abs(targetWidth + targetOffsetLeft));
                    $(target).css(targetSide, -Math.abs(targetWidth + targetOffsetLeft));
                }
                else {
                    $(target).css(targetSide, Math.abs(targetOffsetLeft));
                }
            }
            else if (targetSide == 'right'){
                if ($(target).attr('data-container-divide')){
                    $(container).css(pushType+'-'+targetSide, targetWidth + targetOffsetRight);
                    $(target).css(targetSide, targetOffsetRight);
                }
                else if ($(target).attr('data-container-push')){
                    $(container).css(targetSide, Math.abs(targetWidth + targetOffsetRight));
                    $(target).css(targetSide,  -Math.abs(targetWidth + targetOffsetRight));
                }
                else {
                    $(target).css(targetSide, Math.abs(targetOffsetRight));
                }
            }

            $(target).trigger('shown.sidebar');
        },
        hide: function(){
            $(target).trigger('hide.sidebar');
            $(target).removeClass('toggled');

            if(button !== undefined){
                if(relationship !== ''){
                    // Removing active class from all relative buttons
                    $(elem+'[rel='+relationship+']:not([data-handle=close])').removeClass("active");
                    $(elem+'[rel='+relationship+']:not([data-handle=close])').attr('aria-expanded', 'false');
                }
                // Removing active class from button
                if(button.attr('data-handle') !== 'close'){
                    button.removeClass("active");
                    button.attr('aria-expanded', 'false');
                }

                if($(button).parent().is('li')){
                    if(relationship !== ''){
                        $(elem+'[rel='+relationship+']:not([data-handle=close])').parent().removeClass("active");
                        $(elem+'[rel='+relationship+']:not([data-handle=close])').parent().attr('aria-expanded', 'false');
                    }
                }
            }

            // Sidebar close function
            if (targetSide == 'left'){
                if($(target).attr('data-container-divide')){
                    $(container).css(pushType+'-'+targetSide, targetOffsetLeft);
                    $(target).css(targetSide, -Math.abs(targetWidth + targetOffsetRight));
                }
                else if($(target).attr('data-container-push')){
                    $(container).css(targetSide, targetOffsetLeft);
                    $(target).css(targetSide, -Math.abs(targetWidth + targetOffsetLeft));
                }
                else {
                    $(target).css(targetSide, -Math.abs(targetWidth + targetOffsetLeft));
                }
            }
            else if (targetSide == 'right'){
                if($(target).attr('data-container-divide')){
                    $(container).css(pushType+'-'+targetSide, targetOffsetRight);
                    $(target).css(targetSide, -Math.abs(targetWidth + targetOffsetRight));
                }
                else if($(target).attr('data-container-push')){
                    $(container).css(targetSide, targetOffsetRight);
                    $(target).css(targetSide, -Math.abs(targetWidth + targetOffsetRight));
                }
                else {
                    $(target).css(targetSide, -Math.abs(targetWidth + targetOffsetRight));
                }
            }

            $(target).trigger('hidden.sidebar');
            $(target).on('webkitTransitionEnd otransitionend oTransitionEnd msTransitionEnd transitionend', function(e) {
                $(container).removeClass('sidebar-target');
            });
        }
    };

    if (action === 'show') {
        sidebar_window.update(target, container);
        sidebar_window.show();
    }
    if (action === 'hide') {
        sidebar_window.update(target, container);
        sidebar_window.hide();
    }

    // binding click function
    $('body').off('click', elem);
    $('body').on('click', elem, function(e) {
        e.preventDefault();

        button = $(this);
        target = button.data('target');
        container = $(target).data('container');
        sidebar_window.update(target, container, button);

        /**
         * Sidebar function on data container divide
         * @return {Null}
         */
        if(button.attr('aria-expanded') == 'false'){
            sidebar_window.show();
        }
        else if (button.attr('aria-expanded') == 'true') {
            sidebar_window.hide();
        }

    });

    $(window)
        .load(sidebarHeightAdjust)
        .resize(sidebarHeightAdjust)
        .scroll(sidebarHeightAdjust);

};

var sideWrapper = $('.sidebar-wrapper');

$(document).on('affix.bs.affix','.sidebar-wrapper',function(){
    sideWrapper.css('top',$('.navbar-wrapper').height());
    sideWrapper.data('top',$('.navbar-wrapper').height());
    sideWrapper.data('fixed-offset', $('.navbar-wrapper').height());
});

$(document).on('affix-top.bs.affix','.sidebar-wrapper',function(){
    sideWrapper.css('top',$('.navbar-wrapper').height() + $('.header').height());
    sideWrapper.data('top',$('.navbar-wrapper').height() + $('.header').height());
    sideWrapper.data('fixed-offset', $('.navbar-wrapper').height() + $('.header').height());
});


$.fn.collapse_nav_sub = function () {
    var navSelector = 'ul.nav';

    if (!$(navSelector).hasClass('collapse-nav-sub')) {
        $(navSelector + ' > li', this).each(function () {
            var position = $(this).offset().left - $(this).parent().scrollLeft();
            $(this).attr('data-absolute-position', (position + 5));
        });

        $(navSelector + ' li', this).each(function () {
            if ($('ul', this).length !== 0) {
                $(this).addClass('has-sub');
            }
        });

        $(navSelector + ' > li', this).each(function () {
            $(this).css({
                'left': $(this).data('absolute-position'),
                'position': 'absolute'
            });
        });

        $(navSelector + ' li.has-sub', this).on('click', function () {
            var elem = $(this);
            if (elem.attr('aria-expanded') !== 'true') {
                elem.siblings().fadeOut(100, function () {
                    elem.animate({'left': '15'}, 200, function () {
                        $(elem).first().children('ul').fadeIn(200);
                    });
                });
                elem.siblings().attr('aria-expanded', 'false');
                elem.attr('aria-expanded', 'true');
            } else {
                $(elem).first().children('ul').fadeOut(100, function () {
                    elem.animate({'left': $(elem).data('absolute-position')}, 200, function () {
                        elem.siblings().fadeIn(100);
                    });
                });
                elem.siblings().attr('aria-expanded', 'false');
                elem.attr('aria-expanded', 'false');
            }
        });

        $(navSelector + ' > li.has-sub ul', this).on('click', function (e) {
            e.stopPropagation();
        });
        $(navSelector).addClass('collapse-nav-sub');
    }
};

$(document).ready(function () {
    loadNotificationsPanel();

    $("#right-sidebar").on("click", ".new-notification", function () {
        var notificationId = $(this).data("id");
        var redirectUrl = $(this).data("url");
        var markAsReadNotificationsAPI = "/mdm-admin/notifications/" + notificationId + "/CHECKED";
        var messageSideBar = ".sidebar-messages";

        invokerUtil.put(
            markAsReadNotificationsAPI,
            null,
            function (data) {
                data = JSON.parse(data);
                if (data.statusCode == responseCodes["ACCEPTED"]) {
                    location.href = redirectUrl;
                }
            }, function () {
                var content = "<li class='message message-danger'><h4><i class='icon fw fw-error'></i>Warning</h4>" +
                    "<p>Unexpected error occurred while loading notification. Please refresh the page and" +
                    " try again</p></li>";
                $(messageSideBar).html(content);
            }
        );
    });

    if (typeof $.fn.collapse == 'function') {
        $('.navbar-collapse.tiles').on('shown.bs.collapse', function () {
            $(this).collapse_nav_sub();
        });
    }
});
