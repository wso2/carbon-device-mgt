/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
    var conf = application.get("UI_CONF");
    if (!conf) {
        conf = require("/app/conf/config.json");
        var pinch = require("/app/conf/reader/pinch.min.js")["pinch"];
        var server = require("carbon")["server"];
        pinch(conf, /^/,
            function (path, key, value) {
                if ((typeof value === "string") && value.indexOf("%https.ip%") > -1) {
                    return value.replace("%https.ip%", server.address("https"));
                } else if ((typeof value === "string") && value.indexOf("%http.ip%") > -1) {
                    return value.replace("%http.ip%", server.address("http"));
                } else if ((typeof value === "string") && value.indexOf("%date-year%") > -1) {
                    var year = new Date().getFullYear();
                    return value.replace("%date-year%", year);
                }
                return value;
            }
        );
        application.put("UI_CONF", conf);
    }
    return conf;
}();