function onRequest(context) {
    var deviceType = request.getParameter("type");
    return {"deviceTypePolicyEdit" : "cdmf.unit.device.type." + deviceType + ".policy-edit"};
}