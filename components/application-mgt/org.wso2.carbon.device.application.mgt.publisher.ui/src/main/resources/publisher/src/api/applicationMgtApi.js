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

import Axios from 'axios';
import AuthHandler from './authHandler';
import Constants from '../common/constants';
import Helper from './helpers/appMgtApiHelpers';

/**
 * Api definitions related to application management.
 * TODO: Work to be done on Application release.
 * */
export default class ApplicationMgtApi {

    /**
     * Api for create an application.
     * @param: applicationData: The application data object. This contains an object array of each step data from
     * application creation wizard.
     *
     * From applicationData, the proper application object will be created and send it to the api.
     * */
    static createApplication(generalInfo, platform, screenshots, release) {
        let {application, images} = Helper.buildApplication(generalInfo, platform, screenshots, release);
        const headers = AuthHandler.createAuthenticationHeaders("application/json");
        console.log(application);
        console.log(images);
        Axios.post(Constants.appManagerEndpoints.CREATE_APP, application, {headers: headers});
    }

    /**
     * Upload the image artifacts (banner, icon, screenshots) related to the application.
     * @param appId: The application uuid of the application which the images should be uploaded to.
     * @param images: The images object. This contains icon, banner and screenshots.
     * */
    static uploadImageArtifacts(appId, images) {
        let formData = new FormData();
        formData.append('icon', images.icon);
        formData.append('banner', images.banner);
        formData.append('screenshot', images.screenshots);
        console.log("Image", formData);
        const headers = AuthHandler.createAuthenticationHeaders("multipart/form-data");
        return Axios.post(Constants.appManagerEndpoints.UPLOAD_IMAGE_ARTIFACTS + appId, formData, {headers: headers});
    }

    /**
     * Method to handle application release process.
     * */
    static releaseApplication(appId) {

    }

    /**
     * Promote the current life cycle state of the application.
     * @param appId: The uuid of the application which the state should be updated.
     * @param nextState: The next lifecycle state that the application can be updated to.
     *
     * URL Pattern : /application/1.0/
     * */
    static updateLifeCycleState(appId, nextState) {

    }

    /**
     * Get the next possible state, which the application can be promoted to.
     * @param appId: The application uuid.
     */
    static getNextLifeCycleState(appId) {

    }

    /**
     * Edit created application.
     * @param applicationData: The modified application data.
     * */
    static editApplication(applicationData) {
        let app = Helper.buildApplication(applicationData).application;
        const headers = AuthHandler.createAuthenticationHeaders("application/json");
        return Axios.put(Constants.appManagerEndpoints.CREATE_APP, app, {headers: headers});
    }

    static getApplicationArtifacts(appId, artifactName) {
        const headers = AuthHandler.createAuthenticationHeaders("image/png");
        return Axios.get(Constants.appManagerEndpoints.GET_IMAGE_ARTIFACTS + appId + "?name=" + artifactName,
            {headers: headers});
    }

    static editApplicationArtifacts(appId, images) {
        let formData = new FormData();
        formData.append('icon', images.icon);
        formData.append('banner', images.banner);
        formData.append('screenshot', images.screenshots);
        const headers = AuthHandler.createAuthenticationHeaders("application/json");
        return Axios.put(Constants.appManagerEndpoints.UPLOAD_IMAGE_ARTIFACTS + appId, formData, {headers: headers});
    }

    /**
     * Get all the created applications for the user.
     * @return Object: The response object from the axios post.
     * */
    static getApplications() {
        const headers = AuthHandler.createAuthenticationHeaders("application/json");
        return Axios.get(Constants.appManagerEndpoints.GET_ALL_APPS, {headers: headers});
    }

    /**
     * Get specific application.
     * @param appId: The application Id.
     * */
    static getApplication(appId) {
        const headers = AuthHandler.createAuthenticationHeaders("application/json");
        return Axios.get(Constants.appManagerEndpoints.GET_ALL_APPS + appId, {headers: headers});
    }

    /**
     * Delete specified application.
     * @param appId: The id of the application which is to be deleted.
     * */
    static deleteApplication(appId) {
        const headers = AuthHandler.createAuthenticationHeaders("application/json");
        return Axios.delete(Constants.appManagerEndpoints.GET_ALL_APPS + appId, {headers: headers});
    }
}