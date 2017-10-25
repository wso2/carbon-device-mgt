/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

var conf = function () {
    var cloudConf = application.get("CLOUD_CONF");
    if (!cloudConf) {
        cloudConf = require("/app/conf/toplink-menu.json");
        var pinch = require("/app/modules/conf-reader/pinch.min.js")["pinch"];
        var server = require("carbon")["server"];
        var process = require("process");
        pinch(cloudConf, /^/,
            function (path, key, value) {
                if ((typeof value === "string") && value.indexOf("%https.ip%") > -1) {
                    //noinspection JSUnresolvedFunction
                    return value.replace("%https.ip%", server.address("https"));
                } else if ((typeof value === "string") && value.indexOf("%http.ip%") > -1) {
                    //noinspection JSUnresolvedFunction
                    return value.replace("%http.ip%", server.address("http"));
                } else if ((typeof value === "string") && value.indexOf("%date-year%") > -1) {
                    var year = new Date().getFullYear();
                    return value.replace("%date-year%", year);
                } else if ((typeof value === "string") && value.indexOf("%server.ip%") > -1) {
                    var getProperty = require("process").getProperty;
                    return value.replace("%server.ip%", getProperty("carbon.local.ip"));
                } else {
                    var paramPattern = new RegExp("%(.*?)%", "g");
                    var out = value;
                    while ((matches = paramPattern.exec(value)) !== null) {
                        // This is necessary to avoid infinite loops with zero-width matches
                        if (matches.index === paramPattern.lastIndex) {
                            paramPattern.lastIndex++;
                        }
                        if (matches.length == 2) {
                            var property = process.getProperty(matches[1]);
                            if (property) {
                                out = out.replace(new RegExp("%" + matches[1] + "%", "g"), property);
                            }
                        }
                    }
                    return out;
                }
            }
        );
        application.put("CLOUD_CONF", cloudConf);
    }
    return cloudConf;
}();