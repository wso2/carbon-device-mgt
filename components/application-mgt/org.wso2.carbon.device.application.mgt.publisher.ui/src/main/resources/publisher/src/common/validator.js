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
 * This Utility class will contain basic methods for form validation.
 * */
export const validateURL = (value) => {

};

export const validateNull = (value) => {
    return !value;
};

export const validateEmpty = (array) => {
    return array.length > 0;
};

export const validateEmptyObject = (object) => {
    return Object.keys(object).length > 0;
};
