function onRequest (context) {
    // var log = new Log("operation-bar.js");
    var operationModule = require("../modules/operation.js").operationModule;
    var control_operations = operationModule.getControlOperations("virtual_firealarm");
    var monitor_operations = JSON.stringify(operationModule.getMonitorOperations("virtual_firealarm"));

    context.control_operations = control_operations;
    context.monitor_operations = monitor_operations;

    return context;
}