/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.apimgt.webapp.publisher.utils;

import org.wso2.carbon.apimgt.integration.generated.client.publisher.api.APIsApi;
import org.wso2.carbon.apimgt.integration.generated.client.publisher.model.API;
import org.wso2.carbon.apimgt.integration.generated.client.publisher.model.APIList;
import org.wso2.carbon.apimgt.integration.generated.client.publisher.model.FileInfo;

import java.io.File;

/**
 * Class to create Api for testing.
 */
public class Api implements APIsApi {

    @Override
    public void apisApiIdDelete(String apiId, String ifMatch, String ifUnmodifiedSince) {

    }

    @Override
    public API apisApiIdGet(String apiId, String accept, String ifNoneMatch, String ifModifiedSince) {
        return null;
    }

    @Override
    public API apisApiIdPut(String apiId, API body, String contentType, String ifMatch, String ifUnmodifiedSince) {
        return null;
    }

    @Override
    public void apisApiIdSwaggerGet(String apiId, String accept, String ifNoneMatch, String ifModifiedSince) {

    }

    @Override
    public void apisApiIdSwaggerPut(String apiId, String apiDefinition, String contentType, String ifMatch, String ifUnmodifiedSince) {

    }

    @Override
    public void apisApiIdThumbnailGet(String apiId, String accept, String ifNoneMatch, String ifModifiedSince) {

    }

    @Override
    public FileInfo apisApiIdThumbnailPost(String apiId, File file, String contentType, String ifMatch, String ifUnmodifiedSince) {
        return null;
    }

    @Override
    public void apisChangeLifecyclePost(String action, String apiId, String lifecycleChecklist, String ifMatch, String ifUnmodifiedSince) {

    }

    @Override
    public void apisCopyApiPost(String newVersion, String apiId) {

    }

    @Override
    public APIList apisGet(Integer limit, Integer offset, String query, String accept, String ifNoneMatch) {
        return null;
    }

    @Override
    public API apisPost(API body, String contentType) {
        return new API();
    }
}
