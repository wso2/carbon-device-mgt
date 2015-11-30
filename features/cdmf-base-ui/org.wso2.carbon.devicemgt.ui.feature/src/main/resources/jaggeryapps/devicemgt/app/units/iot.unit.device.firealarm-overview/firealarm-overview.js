function onRequest (context) {
    var log = new Log("detail.js");
    var deviceType = request.getParameter("type");
    var deviceId = request.getParameter("id");
    log.info("###### loading Firealarm Overview!");

    if (deviceType != null && deviceType != undefined && deviceId != null && deviceId != undefined) {
        var deviceModule = require("/modules/device.js").deviceModule;
        var device = deviceModule.viewDevice(deviceType, deviceId);

        if (device) {
            var viewModel = {};
            var deviceInfo = device.properties.DEVICE_INFO;
            if (deviceInfo != undefined && String(deviceInfo.toString()).length > 0) {
                deviceInfo = parse(stringify(deviceInfo));
                    viewModel.system = device.properties.IMEI;
                    viewModel.machine = "Virtual Firealarm";
                    viewModel.vendor = device.properties.VENDOR;
                }
                device.viewModel = viewModel;
        }
        context.device = device;

        return context;
    }
}