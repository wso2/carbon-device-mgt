function onRequest(context) {
    var devicemgtProps = require("/app/modules/conf-reader/main.js")["conf"];
    var authModuleConfigs = context.app.conf["authModule"];
    var sessionDataKey = request.getParameter("sessionDataKey");

    //if sso enabled and sessionDataKey is empty redirect
    var ssoConfigs = authModuleConfigs["sso"];
    if (ssoConfigs && (ssoConfigs["enabled"].toString() == "true") && !sessionDataKey) {
        // SSO is enabled in Auth module.
        var redirectUri = context.app.context + "/uuf/login";
        var queryString = request.getQueryString();
        if (queryString && (queryString.length > 0)) {
            redirectUri = redirectUri + "?" + queryString;
        }
        response.sendRedirect(encodeURI(redirectUri));
        exit();
    }

    var viewModel = {};
    var loginActionUrl = context.app.context + "/uuf/login";
    if (sessionDataKey) {
        loginActionUrl = devicemgtProps["httpsURL"] + "/commonauth";
    }

    viewModel.sessionDataKey = sessionDataKey;
    viewModel.loginActionUrl = loginActionUrl;
    return viewModel;
}