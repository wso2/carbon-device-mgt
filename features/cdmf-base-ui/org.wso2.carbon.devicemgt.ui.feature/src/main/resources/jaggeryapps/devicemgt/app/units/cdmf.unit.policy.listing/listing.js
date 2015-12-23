function onRequest(context) {
    var log = new Log("policy-listing.js");
    var policyModule = require("/app/modules/policy.js")["policyModule"];
    var response = policyModule.getAllPolicies();
    var pageData = {};
    if (response["status"] == "success") {
        var policyListToView = response["content"];
        pageData["policyListToView"] = policyListToView;
        var policyCount = policyListToView.length;
        if (policyCount == 0) {
            pageData["saveNewPrioritiesButtonEnabled"] = false;
            pageData["noPolicy"] = true;
        } else if (policyCount == 1) {
            pageData["saveNewPrioritiesButtonEnabled"] = false;
            pageData["isUpdated"] = response["updated"];
        } else {
            pageData["saveNewPrioritiesButtonEnabled"] = true;
            pageData["isUpdated"] = response["updated"];
        }
    } else {
        // here, response["status"] == "error"
        pageData["policyListToView"] = [];
        pageData["saveNewPrioritiesButtonEnabled"] = false;
        pageData["noPolicy"] = true;
    }
    log.info(pageData);
    return pageData;
}
