function onRequest(context) {
    var authModuleConfigs = context.app.conf["authModule"];
    if (authModuleConfigs && (authModuleConfigs["enabled"].toString() == "true")) {
        // Auth module is enabled.
        if (context.user) {
            // User is already logged in.
            response.sendRedirect(context.app.context + "/");
            exit();
        } else {
            // User is not logged in.
            var ssoConfigs = authModuleConfigs["sso"];
            if (ssoConfigs && (ssoConfigs["enabled"].toString() == "true")) {
                // SSO is enabled in Auth module.
                var redirectUri = context.app.context + "/uuf/login";
                var queryString = request.getQueryString();
                if (queryString && (queryString.length > 0)) {
                    redirectUri = redirectUri + "?" + queryString;
                }
                response.sendRedirect(encodeURI(redirectUri));
                exit();
            } else {
                // Generic login process is enabled.
                return {
                    message: request.getParameter("error"),
                    referer: request.getParameter("referer")
                };
            }
        }
    }
}