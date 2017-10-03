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
import React, {Component} from 'react';

/**
 * Platform component.
 * In Platform listing, this component will be displayed as cards.
 * */
class Platform extends Component {

    constructor() {
        super();
    }

    render() {
        const {platform} = this.props;
        console.log(platform);
        return (
            <div id="platform-content">
                <ul>
                    <li>Name: {platform.name}</li>
                    <li>Description: {platform.description}</li>
                    <li>Status: {platform.enabled}</li>
                </ul>
            </div>
        );
    }
}

export default Platform;
