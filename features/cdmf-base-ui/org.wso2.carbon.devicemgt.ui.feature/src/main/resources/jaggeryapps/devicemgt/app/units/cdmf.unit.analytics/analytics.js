function onRequest(context){
    var groupId = request.getParameter("groupId");
    var title;
    if (groupId){
        context.groupId = groupId;
        title = request.getParameter("name");
    }else{
        context.groupId = 0;
        var deviceModule = require("/modules/device.js").deviceModule;
        var deviceId = request.getParameter("deviceId");
        var deviceType = request.getParameter("deviceType");
        var deviceName = request.getParameter("deviceName");

        //title = deviceModule.getDevice(deviceType, deviceId).name;
        title = deviceName;
    }
    context.title = title + " Analytics";

    return context;
}
