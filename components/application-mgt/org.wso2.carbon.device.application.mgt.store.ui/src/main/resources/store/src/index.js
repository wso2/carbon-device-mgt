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

import React from 'react';
import Store from './App';
import ReactDOM from 'react-dom';
import 'bootstrap/dist/css/bootstrap.css';
import registerServiceWorker from './registerServiceWorker';
import {IntlProvider, addLocaleData, defineMessages} from 'react-intl';
import Axios from 'axios';
import Constants from './common/constants';
import Configuration from './common/configuration';

function loadStore() {
    const possibleLocale = navigator.language.split("-")[0];
    let loadLocaleFile = Axios.create({
        baseURL: Configuration.hostConstants.baseURL + "/" + Configuration.hostConstants.appContext + "/locales/"
        + possibleLocale + ".json"
    }).get();


    /**
     * This is the base js file of the app. All the content will be rendered in the root element.
     * */
    loadLocaleFile.then(response => {
        const messages = defineMessages(response.data);
        addLocaleData(require('react-intl/locale-data/' + possibleLocale));
        ReactDOM.render(<IntlProvider locale={possibleLocale}
                                      messages={messages}><Store/></IntlProvider>, document.getElementById('root'));
        registerServiceWorker();
    }).catch(error => {
        addLocaleData(require('react-intl/locale-data/en'));
        let defaultLocale = Axios.create({
            baseURL: Configuration.hostConstants.baseURL + "/" + Configuration.hostConstants.appContext + "/locales/"
            + Constants.defaultLocale + ".json"
        }).get();
        defaultLocale.then(response => {
            const messages = defineMessages(response.data);
            ReactDOM.render(<IntlProvider locale={possibleLocale}
                                          messages={messages}><Store/></IntlProvider>, document.getElementById('root'));
            registerServiceWorker();
        }).catch(error => {
        });
    });
}

Configuration.loadConfiguration(loadStore);

