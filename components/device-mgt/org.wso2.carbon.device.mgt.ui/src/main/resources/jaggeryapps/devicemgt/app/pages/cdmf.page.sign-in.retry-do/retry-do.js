function onRequest(context) {
    var Encode = Packages.org.owasp.encoder.Encode;
    session.invalidate();
    var viewModel = {};

    var stat = request.getParameter("status");
    var statusMessage = request.getParameter("statusMsg");

    if (!stat || !statusMessage) {
        stat = "Authentication Error!";
        statusMessage =  "Something went wrong during the authentication process.Please try signing in again.";
    }

    viewModel.stat = Encode.forHtmlContent(stat);
    viewModel.statusMessage = Encode.forHtmlContent(statusMessage);
    return viewModel;
}