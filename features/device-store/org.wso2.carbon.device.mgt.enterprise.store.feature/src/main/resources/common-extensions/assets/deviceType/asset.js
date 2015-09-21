asset.configure = function() {
    return {
        meta: {
            lifecycle: {
                name: 'DeviceLifeCycle',
                defaultAction: 'Create',
                defaultLifecycleEnabled: true
            }
        }
    };
};