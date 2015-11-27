function onRequest(context) {
    var constants = require("/modules/constants.js");
    var user = session.get(constants.USER_SESSION_KEY);
    if (user) {
        var mdmProps = require('/app/conf/devicemgt-props.js').config();
        var localLogoutURL = mdmProps.appContext + "api/user/logout";
        var ssoLogoutURL = mdmProps.appContext + "sso/logout";
        var logoutURL = mdmProps.ssoConfiguration.enabled ? ssoLogoutURL : localLogoutURL;
        return {
            userName: user.username,
            subMenus: [{text: "Settings", url: "/settings"}, {text: "Sign out", url: logoutURL}],
            showUserMenu: true
        }
    } else {
        return {
            showUserMenu: false
        };
    }

}