function onRequest() {
    var constants = require("/app/modules/constants.js");
    return session.get(constants["USER_SESSION_KEY"]);
}
