function onRequest(context) {
    var log = new Log("detail.js");
    var deviceType = context.uriParams.deviceType;
    var deviceId = request.getParameter("id");

    if (deviceType != null && deviceType != undefined && deviceId != null && deviceId != undefined) {
        var deviceModule = require("/modules/device.js").deviceModule;
        var device = deviceModule.viewDevice(deviceType, deviceId);

        if (device) {
            var viewModel = {};
            var deviceInfo = device.properties.DEVICE_INFO;
            if (deviceInfo != undefined && String(deviceInfo.toString()).length > 0) {
                deviceInfo = parse(stringify(deviceInfo));
                viewModel.internal_memory = {};
                viewModel.external_memory = {};
                viewModel.location = {
                    latitude: device.properties.LATITUDE,
                    longitude: device.properties.LONGITUDE
                };
                viewModel.imei = device.properties.IMEI;
                viewModel.model = device.properties.DEVICE_MODEL;
                viewModel.vendor = device.properties.VENDOR;
                device.viewModel = viewModel;
            }
        }
        context.device = device;
        return context;
    }
}