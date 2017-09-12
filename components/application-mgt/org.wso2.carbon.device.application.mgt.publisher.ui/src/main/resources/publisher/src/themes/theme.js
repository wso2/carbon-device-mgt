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

/**
 * This class will read through the configuration file and saves the theme names for the usage in other files.
 * User can define the themes in the config.json. The themes will be loaded based on the user preference.
 */
class Theme {
    constructor() {
        const theme = require("../config.json").theme;
        this.currentTheme = theme.type;
        if (this.currentTheme === "default") {
            this.selectedTheme = theme.value;
        } else {
            this.selectedTheme = theme.value;
        }
    }
}

export default (new Theme);
