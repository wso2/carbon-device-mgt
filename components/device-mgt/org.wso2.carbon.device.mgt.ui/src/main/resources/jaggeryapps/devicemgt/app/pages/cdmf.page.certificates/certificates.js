function onRequest(context) {
    var userModule = require("/app/modules/business-controllers/user.js")["userModule"];
    var mdmProps = require("/app/modules/conf-reader/main.js")["conf"];
    var viewModel = {};


    if (userModule.isAuthorized("/permission/admin/device-mgt/certificates/manage")) {
        viewModel["removePermitted"] = true;
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/certificates/view")) {
        viewModel["viewPermitted"] = true;
    }

    viewModel.adminUser = mdmProps.adminUser;
    return viewModel;
}