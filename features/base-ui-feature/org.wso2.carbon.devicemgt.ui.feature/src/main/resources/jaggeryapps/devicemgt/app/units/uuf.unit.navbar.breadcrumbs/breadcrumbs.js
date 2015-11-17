function onRequest(context) {
    var mappingsFile = getFile("private/mappings.json");
    if (!mappingsFile) {
        return null;
    }

    var requestUri = request.getRequestURI(); // /appName/{one}/{two}/{three}/{four}
    var pageUri = requestUri.substring(1 + context.app.name.length); // /{one}/{two}/{three}/{four}
    var uriMatcher = new URIMatcher(pageUri);

    var mappings = require(mappingsFile.getPath());
    var uriPatterns = Object.keys(mappings);
    var numberOfUriPatterns = uriPatterns.length;
    for (var i = 0; i < numberOfUriPatterns; i++) {
        var uriPattern = uriPatterns[i];
        if (uriMatcher.match(uriPattern)) {
            return {
                items: mappings[uriPattern]
            };
        }
    }
    return null;
}