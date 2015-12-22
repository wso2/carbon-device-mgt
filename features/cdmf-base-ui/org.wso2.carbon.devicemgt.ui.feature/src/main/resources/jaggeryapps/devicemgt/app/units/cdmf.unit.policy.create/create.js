function onRequest(context) {
    var userModule = require("/app/modules/user.js")["userModule"];
    var utility = require('/app/modules/utility.js').utility;
    var response = userModule.getRoles();
    if (response["status"] == "success") {
        context["roles"] = response["content"];
    }

    var typesListResponse = userModule.getPlatforms();
    if (typesListResponse["status"] == "success") {
        for (var type in typesListResponse["content"]) {
            typesListResponse["content"][type]["icon"] = utility.getDeviceThumb(typesListResponse["content"][type]["name"]);
        }
        context["types"] = typesListResponse["content"];
    }
    return context;
}