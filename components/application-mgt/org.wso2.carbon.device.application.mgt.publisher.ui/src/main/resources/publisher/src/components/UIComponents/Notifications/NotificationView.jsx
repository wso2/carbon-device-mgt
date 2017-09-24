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
import {Col, Row} from "reactstrap";
import './notification.css';

class NotificationView extends Component {
    constructor() {
        super();
    }

    render() {
        return (
            <div id="notification-view-content">
                <div>
                    <Row id="notification-content">
                        <Col xs="3">
                            <div className="notification-app-icon medium">

                            </div>
                        </Col>
                        <Col xs="9">
                            <Row>
                                <span><strong>Application Name</strong></span>
                            </Row>
                            <Row>
                                <span>Version 1.0</span>
                            </Row>
                            <Row>
                                <p id="app-reject-msg">Your Application was rejected</p>
                            </Row>
                        </Col>
                    </Row>
                    <hr/>
                    <Row id="notification-content">
                        <Col xs="12">
                            <p>Following validations were detected in your review submission.
                               Please attend to them and re-submit</p>
                            <ul>
                                <li>sdjjfsdfsdfkjs shdfjhlkds hflkhfdslkd </li>
                                <li>sdfkds jfdsljfklsdfjksdjlksdjdlkf</li>
                                <li>sfksdf slkjskd jfjds lkfjdsfdsfdslkf sjf lkdsf</li>
                                <li>skfjslkdjfsdjfjksldjf sdkl jflkds jfkslfjs</li>
                                <li>ksdf jks;kshflk hlkjhds lkjhdsklhsd lkf</li>
                                <li> jsdljflksd jfklsdfskljfkjshf;ks ldj</li>
                            </ul>
                        </Col>
                    </Row>
                </div>
            </div>
        );
    }
}

export default NotificationView;
