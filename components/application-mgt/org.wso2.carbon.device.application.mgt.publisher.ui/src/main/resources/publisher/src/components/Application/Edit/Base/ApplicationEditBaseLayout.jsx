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

import './baseLayout.css';
import {Col, Row} from "reactstrap";
import React, {Component} from 'react';
import GeneralInfo from "../GeneralInfo";
import ReleaseManager from '../../Release/ReleaseMgtBase/ReleaseManager';

class ApplicationEdit extends Component {

    constructor() {
        super();
        this.getTabContent = this.getTabContent.bind(this);
        this.state = {
            general: "active",
            release: "",
            pkgmgt: "",
            activeTab: 1
        }
    }

    handleClick(event) {
        event.stopPropagation();
        console.log(typeof event.target.value);
        const key = event.target.value;

        switch (key) {
            case "1": {
                console.log("Step1");
                this.setState({activeTab: 1, general: "active", release: "", pkgmgt: ""});
                break;
            }
            case "2": {
                this.setState({activeTab: 2, general: "", release: "active", pkgmgt: ""});
                break;
            }
            case "3": {
                this.setState({activeTab: 3, general: "", release: "", pkgmgt: "active"});
                break;
            }
            default: {
                return "No Content";
            }
        }
    }

    getTabContent(tab) {
        switch (tab) {
            case 1: {
                return <GeneralInfo/>
            }
            case 2: {
                return <ReleaseManager/>
            }
            case 3: {
                return ("Step3")
            }
        }
    }

    render() {
        console.log(this.state);
        return (
            <div id="application-edit-base">
                <Row id="application-edit-header">
                    <Col xs="3">
                        <a className="back-to-app"><i className="fw fw-left"></i></a>
                    </Col>
                    <Col>
                        Application Name
                    </Col>
                </Row>
                <Row id="application-edit-main-container">
                    <Col xs="3">
                        <div className="tab">
                            <button className={this.state.general} value={1} onClick={this.handleClick.bind(this)}>
                                General
                            </button>
                            <button className={this.state.release} value={2} onClick={this.handleClick.bind(this)}>
                                App
                                Releases
                            </button>
                            <button className={this.state.pkgmgt} value={3} onClick={this.handleClick.bind(this)}>
                                Package Manager
                            </button>
                        </div>
                    </Col>
                    <Col xs="9">
                        <div id="app-edit-content">
                            <Row>
                                <Col xs="12">
                                    <div id="application-edit-outer-content">
                                        {/* Application edit content */}
                                        <div id="application-edit-content">
                                            {this.getTabContent(this.state.activeTab)}
                                        </div>
                                    </div>
                                </Col>
                            </Row>
                        </div>
                    </Col>
                </Row>
            </div>
        )
    }
}

export default ApplicationEdit;
