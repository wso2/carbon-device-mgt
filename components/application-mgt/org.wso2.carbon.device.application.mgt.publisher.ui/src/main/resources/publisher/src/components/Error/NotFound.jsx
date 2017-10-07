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

/**
 * Error page.
 * */
class Error extends Component {

    constructor() {
        super();
    }

    render() {
        return (
            <div className="error-page">
                <Row>
                    <Col>
                        <div className="error-code">
                            <p>404</p>
                        </div>
                    </Col>
                </Row>
                <Row>
                    <Col>
                        <div className="error-text">
                            <p>The page you are looking for doesn't exist or error occurred.</p>
                            <p>Please click <a href="/publisher">here</a> to go to App publisher home page.</p>
                        </div>
                    </Col>
                </Row>
            </div>
        );
    }
}

export default Error;
