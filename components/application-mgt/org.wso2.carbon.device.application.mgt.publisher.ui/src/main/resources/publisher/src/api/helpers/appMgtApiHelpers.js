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

'use strict';

/**
 * Helper methods for app publisher.
 * */
export default class Helper {

    /**
     * Generate application object from form data passed.
     * @param generalInfo: Application data from the application creation form.
     * @param platform
     * @param screenshots
     * @param release
     * @return {Object, Object}: The application object and the set of images related to the application.
     * */
    static buildApplication(generalInfo, platform, screenshots, release) {

        let images = screenshots;
        let application = Object.assign({}, generalInfo, platform);
        for (let prop in application) {
            if (prop === 'tags') {
                application[prop] = Helper.stringifyTags(generalInfo[prop]);
            }
        }
        console.log(application);
        return {application, images};
    }

    /**
     * Creates a String array from tags array.
     * */
    static stringifyTags(tags) {
        let tmpTags = [];
        for (let tag in tags) {
            console.log(tag);
            tmpTags.push(tags[tag].value);
        }

        return tmpTags;
    }

    static buildPlatform(general, config, properties) {
        let platform = Object.assign({}, general, config, properties);

        let icon = platform.icon[0];
        delete platform.icon;

        platform.tags = Helper.stringifyTags(platform.tags);

        console.log(platform, icon);

        let tempData = {
            "platform": platform,
            "icon": icon
        };

        return tempData;
    }

}
