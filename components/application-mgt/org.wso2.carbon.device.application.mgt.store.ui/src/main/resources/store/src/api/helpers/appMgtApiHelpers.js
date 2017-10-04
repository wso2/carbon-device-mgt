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
 * Helper methods for app store.
 * */
export default class Helper {

    /**
     * Generate application object from form data passed.
     * @param appData: Application data from the application creation form.
     * @return {Object, Object}: The application object and the set of images related to the application.
     * */
    static buildApplication(appData) {

        let application = {};
        let images = {};

        for (let step in appData) {
            let tmpData = appData[step].data.step;
            for (let prop in tmpData) {
                if (prop === 'banner' || prop === 'screenshots' || prop === 'icon') {
                    images[prop] = tmpData[prop];
                } else if(prop === 'tags') {
                    application[prop] = Helper.stringifyTags(tmpData[prop]);
                } else {
                    application[prop] = tmpData[prop];
                }
            }
        }
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

}
