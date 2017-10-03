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
import {withRouter} from 'react-router-dom';
import {Col, Row} from "reactstrap";
import {FormattedMessage} from 'react-intl';

/**
 * Application view component.
 * Shows the details of the application.
 * */
class ApplicationView extends Component {
    constructor() {
        super();
        this.state = {
            application: {}
        }
    }

    componentWillReceiveProps(props, nextProps) {
        this.setState({application: props.application});
        console.log(props.application, nextProps)
    }

    componentDidMount() {
        //TODO: Download image artifacts.
    }

    handleEdit() {
        this.props.history.push("/assets/apps/edit/" + this.state.application.uuid);
    }

    render() {
        if (this.state.application.length === 0) {
            return <div/>
        } else {
            const app = this.state.application;
            console.log(app);
            return (
                <div id="application-view-content">
                    <div id="application-view-row">
                        <Row>
                            <Col>
                                <div id="app-icon">
                                </div>
                            </Col>
                            <Col>
                                <Row>
                                    <span><strong>{app.name}</strong></span>
                                </Row>
                                <Row>
                                    <span className="app-updated-date">Last updated on {app.modifiedAt}</span>
                                </Row>
                            </Col>
                        </Row>
                    </div>
                    <div id="application-view-row">
                        <Row>
                            <Col>
                                <span className="app-install-count">2k Installs</span>
                            </Col>
                        </Row>
                        <Row>
                            <Col>
                                <i className="fw fw-star"></i>
                                <i className="fw fw-star"></i>
                                <i className="fw fw-star"></i>
                                <i className="fw fw-star"></i>
                            </Col>
                            <Col>
                                <p><a href="#">View in Store</a></p>
                            </Col>
                        </Row>
                    </div>
                    <hr/>
                    <div id="application-view-row">
                        <Row>
                            <Col>
                                <span><strong>
                                    <FormattedMessage id="Description" defaultMessage="Description"/>:
                                </strong></span>
                            </Col>
                            <Col>
                                <p>{app.description}</p>
                            </Col>
                        </Row>
                        <Row>
                            <Col>
                                <span><strong>
                                    <FormattedMessage id="Tags" defaultMessage="Tags"/>:
                                </strong></span>
                            </Col>
                            <Col>
                                <p>[list of tags...]</p>
                            </Col>
                        </Row>
                        <Row>
                            <Col>
                                <span><strong>
                                    <FormattedMessage id="Release" defaultMessage="Release"/>:
                                </strong></span>
                            </Col>
                            <Col>
                                <p>Production</p>
                            </Col>
                        </Row>
                        <Row>
                            <Col>
                                <span><strong>
                                    <FormattedMessage id="Version" defaultMessage="Version"/>:
                                </strong></span>
                            </Col>
                            <Col>
                                <p>v1.0</p>
                            </Col>
                        </Row>
                    </div>
                </div>

            );

        }
    }
}

export default withRouter(ApplicationView);
