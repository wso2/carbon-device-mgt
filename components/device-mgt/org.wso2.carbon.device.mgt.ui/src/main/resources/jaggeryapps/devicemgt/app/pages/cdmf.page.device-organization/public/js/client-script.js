/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var nodes = null;
var edges = null;
var backendApiUrlNodes = "/api/device-mgt/v1.0/device-organization/1.0.0/visualization/nodes";
var successCallbackNodes = function (dataNodes) {
    if (dataNodes) {
        nodes = JSON.parse(dataNodes);
        // console.log(nodes);
    }
};
invokerUtil.get(backendApiUrlNodes, successCallbackNodes, function (messageNodes) {
});

var backendApiUrlEdges = "/api/device-mgt/v1.0/device-organization/1.0.0/visualization/edges";
var successCallbackEdges = function (dataEdges) {
    if (dataEdges) {
        edges = JSON.parse(dataEdges);
        // console.log(edges);
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
network = new vis.Network(container, data, options);
network.once("beforeDrawing", function() {
    network.focus("server", {
        scale: 33
    });
});
network.once("afterDrawing", function() {
    network.fit({
        animation: {
            duration: 3000,
            easingFunction: "easeInOutCubic"
        }
    });
});
