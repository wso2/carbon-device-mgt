function onRequest(context) {
    return {
        userName: "Administrator",
        subMenus: [{text: "Settings", url: "/settings"}, {text: "Sign out", url: "/logout"}],
        showUserMenu : false
    }
}