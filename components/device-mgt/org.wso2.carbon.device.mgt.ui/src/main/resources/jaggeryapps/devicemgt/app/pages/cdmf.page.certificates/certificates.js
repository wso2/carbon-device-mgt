function onRequest(context) {
    var userModule = require("/app/modules/business-controllers/user.js")["userModule"];
    var mdmProps = require("/app/modules/conf-reader/main.js")["conf"];

    context["permissions"] = userModule.getUIPermissions();

    if (userModule.isAuthorized("/permission/admin/device-mgt/emm-admin/certificate/Get")) {
        context["removePermitted"] = true;
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/emm-admin/certificate/Get")) {
        context["viewPermitted"] = true;
    }
    context["adminUser"] = mdmProps.adminUser;
    return context;
}