function onRequest(context){
    var log = new Log("listing.js");
    var DTYPE_CONF_DEVICE_TYPE_KEY = "deviceType";
    var DTYPE_CONF_DEVICE_TYPE_LABEL_KEY = "label";
    var DTYPE_UNIT_NAME_PREFIX = "cdmf.unit.device.type.";
    var DTYPE_UNIT_NAME_SUFFIX = ".type-view";
    var DTYPE_UNIT_CONFIG_PATH = "/private/conf/device-type.json";
    var DTYPE_UNIT_LISTING_TEMPLATE_PATH = "/public/templates/listing.hbs";

    var viewModel = {};
    var deviceModule = require("/app/modules/device.js").deviceModule;
    var data = deviceModule.getDeviceTypes();

    if(data.data){
        var deviceTypes = data.data;
        var deviceTypesList = [];
        for (var i = 0; i < deviceTypes.length; i++) {

            var deviceTypeLabel = deviceTypes[i].name;
            var deviceTypeConfigFile = getFile("../" + DTYPE_UNIT_NAME_PREFIX + deviceTypes[i].name + DTYPE_UNIT_NAME_SUFFIX + DTYPE_UNIT_CONFIG_PATH);
            if(deviceTypeConfigFile) {
                var configs = require(deviceTypeConfigFile.getPath());
                if (configs[DTYPE_CONF_DEVICE_TYPE_KEY][DTYPE_CONF_DEVICE_TYPE_LABEL_KEY]) {
                    deviceTypeLabel = configs[DTYPE_CONF_DEVICE_TYPE_KEY][DTYPE_CONF_DEVICE_TYPE_LABEL_KEY];
                }
            }

            var deviceTypeListingTemplateFile = getFile("../" + DTYPE_UNIT_NAME_PREFIX + deviceTypes[i].name + DTYPE_UNIT_NAME_SUFFIX + DTYPE_UNIT_LISTING_TEMPLATE_PATH);
            if(deviceTypeListingTemplateFile) {
                deviceTypesList.push({"hasCustTemplate" : true, "deviceTypeLabel" : deviceTypeLabel, "deviceTypeName" : deviceTypes[i].name, "deviceTypeId" : deviceTypes[i].id});
            } else {
                deviceTypesList.push({"hasCustTemplate" : false, "deviceTypeLabel" : deviceTypeLabel, "deviceTypeName" : deviceTypes[i].name, "deviceTypeId" : deviceTypes[i].id});
            }
        }
        viewModel.deviceTypesList = deviceTypesList;
    } else {
        log.error("Unable to fetch device types data");
        throw new Error("Unable to fetch device types!");
    }

    return viewModel;
}