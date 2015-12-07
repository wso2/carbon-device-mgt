function onRequest (context) {
    var log = new Log("detail.js");
    var deviceType = request.getParameter("type");
    var deviceId = request.getParameter("id");
    log.info("###### loading overview section!");

    if (deviceType != null && deviceType != undefined && deviceId != null && deviceId != undefined) {
        var deviceModule = require("/modules/device.js").deviceModule;
        var device = deviceModule.viewDevice(deviceType, deviceId);

        if (device) {
            var viewModel = {};
            var deviceInfo = device.properties.DEVICE_INFO;
            if (deviceInfo != undefined && String(deviceInfo.toString()).length > 0) {
                deviceInfo = parse(stringify(deviceInfo));
                if (device.type == "ios") {
                    viewModel.imei = device.properties.IMEI;
                    viewModel.phoneNumber = deviceInfo.PhoneNumber;
                    viewModel.udid = deviceInfo.UDID;
                } else if (device.type == "android") {
                    viewModel.imei = device.properties.IMEI;
                    viewModel.model = device.properties.DEVICE_MODEL;
                    viewModel.vendor = device.properties.VENDOR;
                } else if (device.type == "windows") {
                    viewModel.imei = device.properties.IMEI;
                    viewModel.model = device.properties.DEVICE_MODEL;
                    viewModel.vendor = device.properties.VENDOR;
                }
                device.viewModel = viewModel;
            }
        }
        context.device = device;

        return context;
    }
}