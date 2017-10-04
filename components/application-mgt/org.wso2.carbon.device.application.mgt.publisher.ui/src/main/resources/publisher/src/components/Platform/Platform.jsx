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
 * Platform component.
 * In Platform listing, this component will be displayed as cards.
 * */
class Platform extends Component {

    constructor() {
        super();
    }

    render() {
        const {platform} = this.props;
        return (
            <div className="platform-content">
                <Row>
                    <Col>
                        <div className="platform-icon-container">
                            <p className="platform-icon-letter">{platform.name.charAt(0)}</p>
                        </div>
                    </Col>
                    <Col>
                        <div className="platform-text-container">
                            <p className="app-view-field">{platform.name}</p>
                            <p className="app-view-text">{platform.enabled ? "Active" : "Disabled"}</p>
                        </div>
                    </Col>

                </Row>
                <hr/>
                <Row style={{fontSize: '12px'}}>
                    <Col xs="3"><a>{platform.enabled ? "Disable" : "Activate"}</a></Col>
                    <Col xs="6"><a>Share With Tenants</a></Col>
                    <Col xs="1"><i className="fw fw-down"></i></Col>
                </Row>
            </div>
        );
    }
}

export default Platform;
