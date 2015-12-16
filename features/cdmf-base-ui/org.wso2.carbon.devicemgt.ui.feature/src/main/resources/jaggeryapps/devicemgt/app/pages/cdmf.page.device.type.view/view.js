function onRequest(context) {
    /**
     * {{#itr context}}key : {{key}} value : {{value}}{{/itr}}
     */
    context.handlebars.registerHelper("itr", function (obj, options) {
        var key, buffer = '';
        for (key in obj) {
            if (obj.hasOwnProperty(key)) {
                buffer += options.fn({key: key, value: obj[key]});
            }
        }
        return buffer;
    });

    return {
        "deviceTypeViewUnitName": "cdmf.unit.device.type." + context.uriParams.deviceType
                                  + ".type-view"
    };

}