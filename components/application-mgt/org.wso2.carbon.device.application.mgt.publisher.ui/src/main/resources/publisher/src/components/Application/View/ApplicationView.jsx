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
                                    {/*TODO: Remove this*/}
                                    <img
                                        className="app-view-image"
                                        src={app.icon}
                                    />
                                </div>
                            </Col>
                            <Col>
                                <Row>
                                    <p className="app-view-field">{app.name}</p>
                                </Row>
                                <Row>
                                    <span className="app-updated-date app-view-text">
                                        <FormattedMessage id="Last.Updated"
                                                          defaultMessage="Last.Updated"/> {app.modifiedAt}</span>
                                </Row>
                            </Col>
                        </Row>
                    </div>
                    <div id="application-view-row">
                        <Row>
                            <Col>
                                <span className="app-install-count app-view-text">
                                    2k <FormattedMessage id="Installs" defaultMessage="Installs"/>
                                </span>
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
                                <p className="app-view-text">
                                    <a href="#">
                                        <FormattedMessage id="View.In.Store" defaultMessage="View.In.Store"/>
                                    </a>
                                </p>
                            </Col>
                        </Row>
                    </div>
                    <hr/>
                    <div id="application-view-row">
                        <Row>
                            <Col>
                                <p className="app-view-field">
                                    <FormattedMessage id="Description" defaultMessage="Description"/>:
                                </p>
                            </Col>
                            <Col>
                                <p className="app-view-text">{app.description}</p>
                            </Col>
                        </Row>
                        <Row>
                            <Col>
                                <p className="app-view-field">
                                    <FormattedMessage id="Tags" defaultMessage="Tags"/>:
                                </p>
                            </Col>
                            <Col>
                                <p className="app-view-text">[list of tags...]</p>
                            </Col>
                        </Row>
                        <Row>
                            <Col>
                                <p className="app-view-field">
                                    <FormattedMessage id="Release" defaultMessage="Release"/>:
                                </p>
                            </Col>
                            <Col>
                                <p className="app-view-text">Production</p>
                            </Col>
                        </Row>
                        <Row>
                            <Col>
                                <p className="app-view-field">
                                    <FormattedMessage id="Version" defaultMessage="Version"/>:
                                </p>
                            </Col>
                            <Col>
                                <p className="app-view-text">v1.0</p>
                            </Col>
                        </Row>
                    </div>
                </div>

            );

        }
    }
}

export default withRouter(ApplicationView);
