function onRequest(context) {

    var deviceMgtProps = require("/app/modules/conf-reader/main.js")["conf"];
    var viewModel = {};
    viewModel.isCloud = deviceMgtProps.isCloud;
    return viewModel;

}