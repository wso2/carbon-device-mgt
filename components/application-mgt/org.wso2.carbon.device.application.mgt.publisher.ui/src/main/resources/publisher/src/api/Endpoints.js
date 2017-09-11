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

import Helper from './helpers/AppMgtApiHelpers';

/**
 * Application related apis
 * */
export default class Endpoint{

    /**
     * Api for create an application.
     * @param: applicationData: The application data object. This contains an object array of each step data from
     * application creation wizard.
     *
     * From that data array, the proper application object is created and send it to the api.
     * */
    static createApplication(applicationData) {

        console.log("In application create application", applicationData);
        Helper.buildApplication(applicationData);

    }

    /**
     * Method to handle application release process.
     * */
    static releaseApplication() {

    }

    /**
     * Edit created application.
     * @param applicationData: The modified application data.
     * */
    static editApplication(applicationData) {

    }

    /**
     * Get all the created applications for the user.
     * */
    static getApplications() {

    }

    /**
     * Get specific application.
     * @param appId : The application Id.
     * */
    static getApplication(appId) {

    }

    /**
     * Delete specified application.
     * @param appId: The id of the application which is to be deleted.
     * */
    static deleteApplication(appId) {

    }


    /**
     * Platform related apis
     * */
    /**
     * Create a new Platform
     * @param platformData: The platform data object.
     * */
    static createPlatform(platformData) {
        // /api/application-mgt/v1.0/platforms/1.0.0/
        // {
        //     identifier: "${platform_identifier}",
        //     name: "New Platform",
        //     description : "New Platform"
        // }
    }

    /**
     * Get available platforms
     * */
    static getPlatforms() {

    }

    /**
     * Get the user specified platform
     * @param platformId: The identifier of the platform
     * */
    static getPlatform(platformId) {

    }

    /**
     * Delete specified platform
     * @param platformId: The id of the platform which is to be deleted.
     * */
    static deletePlatform(platformId) {

    }


}
