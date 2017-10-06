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

import axios from 'axios';

/**
 * This class will read through the configuration file and saves the theme names for the usage in other files.
 * User can define the themes in the config.json. The themes will be loaded based on the user preference.
 */
class Theme {
    constructor() {
        this.defaultThemeType =  "default";
        this.currentThemeType =  this.defaultThemeType;
        this.currentTheme = "lightBaseTheme";
        this.themeFolder =  "themes";
        this.styleSheetType = "text/css";
        this.styleSheetRel = "stylesheet";
        this.selectedTheme = this.defaultThemeType;

        //TODO Need to get the app context properly when the server is ready
        this.baseURL = window.location.origin;
        this.appContext = window.location.pathname.split("/")[1];
        this.loadThemeConfigs = this.loadThemeConfigs.bind(this);
        this.loadThemeFiles = this.loadThemeFiles.bind(this);
        this.insertThemingScripts =  this.insertThemingScripts.bind(this);
        this.removeThemingScripts = this.removeThemingScripts.bind(this);
    }

    /**
     * To load the theme related configurations from the configuration file.
     * @returns the http response.
     */
    loadThemeConfigs () {
        let httpClient = axios.create({
            baseURL: this.baseURL + "/" + this.appContext + "/config.json",
            timeout: 2000
        });
        httpClient.defaults.headers.post['Content-Type'] = 'application/json';
        return httpClient.get();
    }

    /**
     * To load the particular theme file from the path.
     * @param path Path to load the theme files
     * @returns Http response from the particular file.
     */
    loadThemeFiles (path) {
        let httpClient = axios.create({
            baseURL: this.baseURL + "/" + this.appContext +  path,
            timeout: 2000
        });
        return httpClient.get();
    }

    /**
     * To insert the css files to the document.
     * @param scriptId ID of the script that need to be inserted
     */
    insertThemingScripts(scriptId) {
        const script = scriptId + ".css";
        let themePath  =  "/" + this.themeFolder + "/" + this.selectedTheme + "/" + script;
        let themeFile = this.loadThemeFiles(themePath);
        let head = document.getElementsByTagName("head")[0];
        let link = document.createElement("link");
        link.type = this.styleSheetType;
        link.href = this.baseURL + "/" + this.appContext + themePath;
        link.id = scriptId;
        link.rel = this.styleSheetRel;
        this.removeThemingScripts(scriptId);

        themeFile.then(function () {
            head.appendChild(link);
        }).catch(error => {
            // If there is no customized css file, load the default one.
            themePath = "/" + this.themeFolder + "/" + this.defaultThemeType + "/" + script;
            link.href = this.baseURL + "/" + this.appContext + themePath;
            head.appendChild(link);
        });
    }

    /**
     * To remove the css scripts that are inserted before.
     * @param scriptId Id of the script that need to be removed
     */
    removeThemingScripts(scriptId) {
        let styleSheet = document.getElementById(scriptId);
        if (styleSheet !== null) {
            styleSheet.disabled = true;
            styleSheet.parentNode.removeChild(styleSheet);
        }
    }

}

export default (new Theme);
