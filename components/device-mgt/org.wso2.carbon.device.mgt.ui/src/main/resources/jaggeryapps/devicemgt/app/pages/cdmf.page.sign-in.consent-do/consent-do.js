function onRequest(context) {
    var Encode = Packages.org.owasp.encoder.Encode;
    var viewModel = {};
    viewModel.appName = Encode.forHtml(request.getParameter("sp"));
    var mandatoryClaims = [];
    var requestedClaims = [];
    var singleMandatoryClaim = false;

    var mandatoryClaimsList, requestedClaimsList;
    var i, j, partOne, partTwo;
    if (request.getParameter("mandatoryClaims")) {
        mandatoryClaimsList = request.getParameter("mandatoryClaims").split(",");
        singleMandatoryClaim = (mandatoryClaimsList.length === 1);
        for (j = 0; j < mandatoryClaimsList.length; j++) {
            var mandatoryClaimsStr = mandatoryClaimsList[j];
            i = mandatoryClaimsStr.indexOf('_');
            partOne = mandatoryClaimsStr.slice(0, i);
            partTwo = mandatoryClaimsStr.slice(i + 1, mandatoryClaimsStr.length);
            mandatoryClaims.push(
                {"claimId": Encode.forHtmlAttribute(partOne), "displayName": Encode.forHtmlAttribute(partTwo)}
            );
        }
    }
    if (request.getParameter("requestedClaims")) {
        requestedClaimsList = request.getParameter("requestedClaims").split(",");
        for (j = 0; j < requestedClaimsList.length; j++) {
            var requestedClaimsStr = requestedClaimsList[j];
            i = requestedClaimsStr.indexOf('_');
            partOne = requestedClaimsStr.slice(0, i);
            partTwo = requestedClaimsStr.slice(i + 1, requestedClaimsStr.length);
            requestedClaims.push(
                {"claimId": Encode.forHtmlAttribute(partOne), "displayName": Encode.forHtmlAttribute(partTwo)}
            );
        }
    }
    viewModel.mandatoryClaims = mandatoryClaims;
    viewModel.requestedClaims = requestedClaims;
    viewModel.singleMandatoryClaim = singleMandatoryClaim;
    viewModel.sessionDataKey = Encode.forHtmlAttribute(request.getParameter("sessionDataKey"));
    return viewModel;
}