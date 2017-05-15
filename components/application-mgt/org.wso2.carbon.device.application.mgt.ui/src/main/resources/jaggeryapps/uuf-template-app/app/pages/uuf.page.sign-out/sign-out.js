function onRequest(context) {
    var authModuleConfigs = context.app.conf["authModule"];
    if (authModuleConfigs && (authModuleConfigs["enabled"].toString() == "true")) {
        // Auth module is enabled.
        if (context.user) {
            // User is logged in.
            response.sendRedirect(context.app.context + "/uuf/logout");
            exit();
        } else {
            // User is already logged out.
            response.sendRedirect(context.app.context + "/");
            exit();
        }
    }

}