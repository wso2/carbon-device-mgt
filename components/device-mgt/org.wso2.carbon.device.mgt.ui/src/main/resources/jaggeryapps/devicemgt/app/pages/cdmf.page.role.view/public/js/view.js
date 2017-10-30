$(document).ready(function() {
    var path = window.location.href;
    $('ul.nav a').each(function() {
        var url = this.href;
        if (url.indexOf("/devicemgt/roles") !== -1) {
            $(this).addClass('active');
        }
    });
    $(".roles-remove-link").click(function() {
        var rolesList = [];
        rolesList[0] = $('.roles-remove-link').data('role');
        console.log(rolesList);

        var serviceURL = "/api/device-mgt/v1.0/roles/deleteRoles";
        if (rolesList.length == 0) {
            modalDialog.header('Action cannot be performed !');
            modalDialog.content('Please select a roles or a list of policies to remove.');
            modalDialog.footer('<div class="buttons"><a href="javascript:modalDialog.hide()" ' +
                'class="btn-operations">Ok</a></div>');
            modalDialog.showAsAWarning();
        } else {
            modalDialog.header('Do you really want to this role?');
            modalDialog.footer('<div class="buttons"><a href="#" id="remove-roles-yes-link" class=' +
                '"btn-operations">Remove</a> <a href="#" id="remove-roles-cancel-link" ' +
                'class="btn-operations btn-default">Cancel</a></div>');
            modalDialog.show();
        }

        // on-click function for roles removing "yes" button
        $("a#remove-roles-yes-link").click(function() {
            invokerUtil.post(
                serviceURL,
                rolesList,
                // on success
                function(data, textStatus, jqXHR) {
                    if (jqXHR.status == 200) {
                        modalDialog.header('Done. This role was successfully removed.');
                        modalDialog.footer('<div class="buttons"><a href="#" id="remove-roles-success-link" ' +
                            'class="btn-operations">Ok</a></div>');
                        $("a#remove-roles-success-link").click(function() {
                            modalDialog.hide();
                            location.href = "../../roles";
                        });
                    }
                },
                // on error
                function(jqXHR) {
                    console.log(stringify(jqXHR.data));
                    modalDialog.header('An unexpected error occurred. Please try again later.');
                    modalDialog.footer('<div class="buttons"><a href="#" id="remove-roles-error-link" ' +
                        'class="btn-operations">Ok</a></div>');
                    modalDialog.showAsError();
                    $("a#remove-roles-error-link").click(function() {
                        modalDialog.hide();
                    });
                }
            );
        });

        // on-click function for roles removing "cancel" button
        $("a#remove-roles-cancel-link").click(function() {
            modalDialog.hide();
        });

    });

});