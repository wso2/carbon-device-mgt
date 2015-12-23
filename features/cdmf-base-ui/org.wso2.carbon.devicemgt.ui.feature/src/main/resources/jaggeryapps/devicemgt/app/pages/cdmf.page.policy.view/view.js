function onRequest(context) {
    var deviceType = request.getParameter("type");
    return {"deviceTypePolicyView" : "cdmf.unit.device.type." + deviceType + ".policy-view"};
}