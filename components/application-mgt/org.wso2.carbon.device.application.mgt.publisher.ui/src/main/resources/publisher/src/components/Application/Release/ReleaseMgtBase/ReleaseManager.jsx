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

import PropTypes from 'prop-types';
import React, {Component} from 'react';
import {Button, Col, Row} from "reactstrap";
import CreateRelease from "../Create/CreateRelease";

class ReleaseManager extends Component {

    constructor() {
        super();
        this.getNoReleaseContent = this.getNoReleaseContent.bind(this);
        this.createRelease = this.createRelease.bind(this);
        this.handleBackPress = this.handleBackPress.bind(this);
        this.state = {
            createRelease: false,
            onGoing: ""
        }
    }

    createRelease(event) {
        event.preventDefault();
        this.setState({createRelease: true, onGoing: event.target.value})
    }

    handleBackPress() {
        this.setState({createRelease: false});
    }

    /**
     * Holds a generic message saying there are no current release in the specified release channel.
     * */
    getNoReleaseContent(release) {
        return (
            <div>
                <Row>
                    <Col sm="12" md={{size: 8, offset: 4}}>
                        <p>You have no on-going {release} Releases!</p>
                    </Col>
                </Row>
                <Row>
                    <Col sm="12" md={{size: 8, offset: 5}}>
                        <Button
                            className="button-add"
                            id={release.toLowerCase()}
                            value={release}
                            onClick={this.createRelease}
                        >
                            Create a Release
                        </Button>
                    </Col>
                </Row>
            </div>
        );
    }

    render() {
        return (
            <div>
                {this.state.createRelease ?
                    <CreateRelease
                        channel={this.state.onGoing}
                        handleBack={this.handleBackPress}
                    /> :
                    <div id="release-mgt-content">
                        <Row>
                            <Col sm="12">
                                <div className="release" id="production">
                                    <span>Production Releases</span>
                                    <div className="release-content">
                                        <div className="release-inner">
                                            {this.getNoReleaseContent("Production")}
                                        </div>
                                    </div>
                                </div>
                            </Col>
                        </Row>
                        <Row>
                            <Col sm="12">
                                <div className="release" id="beta">
                                    <span>Beta Releases</span>
                                    <div className="release-content">
                                        <div className="release-inner">
                                            {this.getNoReleaseContent("Beta")}
                                        </div>
                                    </div>
                                </div>
                            </Col>
                        </Row>
                        <Row>
                            <Col sm="12">
                                <div className="release" id="alpha">
                                    <span>Alpha Releases</span>
                                    <div className="release-content">
                                        <div className="release-inner">
                                            {this.getNoReleaseContent("Alpha")}
                                        </div>
                                    </div>
                                </div>
                            </Col>
                        </Row>
                    </div>
                }
            </div>
        )
    }
}

export default ReleaseManager;
