function onRequest(context){
    context.handlebars.registerHelper('equal', function (lvalue, rvalue, options) {
        if (arguments.length < 3)
            throw new Error("Handlebars Helper equal needs 2 parameters");
        if( lvalue!=rvalue ) {
            return options.inverse(this);
        } else {
            return options.fn(this);
        }
    });

    return {"deviceViewUnitName" : "cdmf.unit.device.type." + context.uriParams.deviceType + ".device-view"};
}