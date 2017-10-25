function onRequest(context) {
    var type = context.unit.params.type;
    switch (type) {
        case "success":
            return {icon: "ok"};
        case "info":
            return {icon: "info"};
        case "warning":
            return {icon: "warning"};
        case "danger":
            return {icon: "error"};
        default:
            return {icon: "ok"};
    }
}