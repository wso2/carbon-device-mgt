function onRequest(context) {
    var log = new Log("detail.js");
    var deviceType = request.getParameter("type");
    var deviceId = request.getParameter("id");

    if (deviceType != null && deviceType != undefined && deviceId != null && deviceId != undefined) {
        var deviceModule = require("/app/modules/device.js").deviceModule;
        var device = deviceModule.viewDevice(deviceType, deviceId);

        if (device) {
            var viewModel = {};
            var deviceInfo = device.properties.DEVICE_INFO;
            if (deviceInfo != undefined && String(deviceInfo.toString()).length > 0) {
                deviceInfo = parse(stringify(deviceInfo));
                if (device.type == "ios") {
                    viewModel.BatteryLevel = Math.round(deviceInfo.BatteryLevel * 100);
                    viewModel.DeviceCapacity = Math.round(deviceInfo.DeviceCapacity * 100) / 100;
                    viewModel.AvailableDeviceCapacity = Math.round(deviceInfo.AvailableDeviceCapacity * 100) / 100;
                    viewModel.DeviceCapacityUsed = Math.round((viewModel.DeviceCapacity
                        - viewModel.AvailableDeviceCapacity) * 100) / 100;
                    viewModel.DeviceCapacityPercentage = Math.round(viewModel.DeviceCapacityUsed
                        / viewModel.DeviceCapacity * 10000) / 100;
                } else if (device.type == "android") {
                    viewModel.internal_memory = {};
                    viewModel.external_memory = {};
                    viewModel.location = {
                        latitude: device.properties.LATITUDE,
                        longitude: device.properties.LONGITUDE
                    };
                    var info = {};
                    var infoList = parse(deviceInfo);
                    if (infoList != null && infoList != undefined) {
                        for (var j = 0; j < infoList.length; j++) {
                            info[infoList[j].name] = infoList[j].value;
                        }
                    }
                    deviceInfo = info;
                    viewModel.BatteryLevel = deviceInfo.BATTERY_LEVEL;
                    viewModel.internal_memory.FreeCapacity = Math.round((deviceInfo.INTERNAL_TOTAL_MEMORY -
                        deviceInfo.INTERNAL_AVAILABLE_MEMORY) * 100) / 100;
                    viewModel.internal_memory.DeviceCapacityPercentage = Math.round(deviceInfo.INTERNAL_AVAILABLE_MEMORY
                        / deviceInfo.INTERNAL_TOTAL_MEMORY * 10000) / 100;
                    viewModel.external_memory.FreeCapacity = Math.round((deviceInfo.EXTERNAL_TOTAL_MEMORY -
                        deviceInfo.EXTERNAL_AVAILABLE_MEMORY) * 100) / 100;
                    viewModel.external_memory.DeviceCapacityPercentage = Math.round(deviceInfo.EXTERNAL_AVAILABLE_MEMORY
                        / deviceInfo.EXTERNAL_TOTAL_MEMORY * 10000) / 100;
                } else if (device.type == "windows") {
                    viewModel.internal_memory = {};
                    viewModel.external_memory = {};
                    viewModel.location = {
                        latitude: device.properties.LATITUDE,
                        longitude: device.properties.LONGITUDE
                    };
                }else if (device.type == "TemperatureController") {
                    viewModel.internal_memory = {};
                    viewModel.external_memory = {};
                }
                device.viewModel = viewModel;
            }
        }
        context.device = device;
        return context;
    }
}