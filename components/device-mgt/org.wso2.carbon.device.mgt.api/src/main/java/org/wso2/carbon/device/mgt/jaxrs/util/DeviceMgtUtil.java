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

package org.wso2.carbon.device.mgt.jaxrs.util;

import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorListItem;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.beans.ProfileFeature;
import org.wso2.carbon.device.mgt.jaxrs.exception.BadRequestException;
import org.wso2.carbon.device.mgt.common.policy.mgt.Profile;

import javax.validation.ConstraintViolation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DeviceMgtUtil {

    public static Profile convertProfile(org.wso2.carbon.device.mgt.jaxrs.beans.Profile mdmProfile) {
        Profile profile = new Profile();
        profile.setTenantId(mdmProfile.getTenantId());
        profile.setCreatedDate(mdmProfile.getCreatedDate());
        profile.setDeviceType(mdmProfile.getDeviceType());

        List<org.wso2.carbon.device.mgt.common.policy.mgt.ProfileFeature> profileFeatures =
                new ArrayList<org.wso2.carbon.device.mgt.common.policy.mgt.ProfileFeature>(mdmProfile.getProfileFeaturesList().size());
        for (ProfileFeature mdmProfileFeature : mdmProfile.getProfileFeaturesList()) {
            profileFeatures.add(convertProfileFeature(mdmProfileFeature));
        }
        profile.setProfileFeaturesList(profileFeatures);
        profile.setProfileId(mdmProfile.getProfileId());
        profile.setProfileName(mdmProfile.getProfileName());
        profile.setUpdatedDate(mdmProfile.getUpdatedDate());
        return profile;
    }

    public static org.wso2.carbon.device.mgt.common.policy.mgt.ProfileFeature convertProfileFeature(ProfileFeature
                                                                                                 mdmProfileFeature) {

        org.wso2.carbon.device.mgt.common.policy.mgt.ProfileFeature profileFeature =
                new org.wso2.carbon.device.mgt.common.policy.mgt.ProfileFeature();
        profileFeature.setProfileId(mdmProfileFeature.getProfileId());
        profileFeature.setContent(mdmProfileFeature.getPayLoad());
        profileFeature.setDeviceType(mdmProfileFeature.getDeviceTypeId());
        profileFeature.setFeatureCode(mdmProfileFeature.getFeatureCode());
        profileFeature.setId(mdmProfileFeature.getId());
        return profileFeature;

    }

    public static List<Scope> convertScopesListToAPIScopes(List<String> scopes, String roleName) {
        List<Scope> convertedScopes = new ArrayList<>();
        Scope convertedScope;
        for (String scope : scopes) {
            convertedScope = new Scope();
            convertedScope.setKey(scope);
            convertedScope.setRoles(roleName);
            convertedScopes.add(convertedScope);
        }
        return convertedScopes;
    }

    public static List<org.wso2.carbon.device.mgt.jaxrs.beans.Scope> convertAPIScopestoScopes(List<Scope> scopes) {
        List<org.wso2.carbon.device.mgt.jaxrs.beans.Scope> convertedScopes = new ArrayList<>();
        org.wso2.carbon.device.mgt.jaxrs.beans.Scope convertedScope;
        for (Scope scope : scopes) {
            convertedScope = new org.wso2.carbon.device.mgt.jaxrs.beans.Scope();
            convertedScope.setKey(scope.getKey());
            convertedScope.setName(scope.getName());
            convertedScope.setDescription(scope.getDescription());
            convertedScopes.add(convertedScope);
        }
        return convertedScopes;
    }

    public static List<String> convertAPIScopesToScopeKeys(List<Scope> scopes) {
        List<String> convertedScopes = new ArrayList<>();
        for (Scope scope : scopes) {
            convertedScopes.add(scope.getKey());
        }
        return convertedScopes;
    }
    /**
     * Returns a new BadRequestException
     *
     * @param description description of the exception
     * @return a new BadRequestException with the specified details as a response DTO
     */
    public static BadRequestException buildBadRequestException(String description) {
        ErrorResponse errorResponse = getErrorResponse(Constants.
                ErrorMessages.STATUS_BAD_REQUEST_MESSAGE_DEFAULT,400l, description);
        return new BadRequestException(errorResponse);
    }

    /**
     * Returns generic ErrorResponse.
     * @param message specific error message
     * @param code
     * @param description
     * @return generic Response with error specific details.
     */
    public static ErrorResponse getErrorResponse(String message, Long code, String description) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setCode(code);
        errorResponse.setMoreInfo("");
        errorResponse.setMessage(message);
        errorResponse.setDescription(description);
        return errorResponse;
    }

    public static <T> ErrorResponse getConstraintViolationErrorDTO(Set<ConstraintViolation<T>> violations) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setDescription("Validation Error");
        errorResponse.setMessage("Bad Request");
        errorResponse.setCode(400l);
        errorResponse.setMoreInfo("");
        List<ErrorListItem> errorListItems = new ArrayList<>();
        for (ConstraintViolation violation : violations) {
            ErrorListItem errorListItemDTO = new ErrorListItem();
            errorListItemDTO.setCode(400 + "_" + violation.getPropertyPath());
            errorListItemDTO.setMessage(violation.getPropertyPath() + ": " + violation.getMessage());
            errorListItems.add(errorListItemDTO);
        }
        errorResponse.setErrorItems(errorListItems);
        return errorResponse;
    }
}