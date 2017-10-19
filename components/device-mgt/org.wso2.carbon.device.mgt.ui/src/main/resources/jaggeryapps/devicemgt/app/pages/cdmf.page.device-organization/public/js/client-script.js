var nodes = null;
var edges = null;
var backendApiUrlNodes = "/api/device-mgt/v1.0/device-organization/1.0.0/nodes";
var successCallbackNodes = function (dataNodes) {
    if (dataNodes) {
        console.log(JSON.parse(dataNodes));
        nodes = JSON.parse(dataNodes);
        console.log(nodes);
    }
};
invokerUtil.get(backendApiUrlNodes, successCallbackNodes, function (messageNodes) {
});

var backendApiUrlEdges = "/api/device-mgt/v1.0/device-organization/1.0.0/edges";
var successCallbackEdges = function (dataEdges) {
    if (dataEdges) {
        console.log(JSON.parse(dataEdges));
        edges = JSON.parse(dataEdges);
        console.log(edges);
    }
};
invokerUtil.get(backendApiUrlEdges, successCallbackEdges, function (messageEdges) {
});

var container = document.getElementById('mynetwork');
var data = {
    nodes: nodes,
    edges: edges
};
var options = {
    layout: {
        randomSeed: 100
    },
    nodes: {
        shape: 'dot',
        size: 25,
        font: {
            size: 15,
            color: '#ffffff'
        },
        borderWidth: 2
    },
    edges: {
        width: 2,
        color: '#ffff00',
    },
    interaction: {
        dragNodes: false
    }
};
network = new vis.Network(container, data, options);
