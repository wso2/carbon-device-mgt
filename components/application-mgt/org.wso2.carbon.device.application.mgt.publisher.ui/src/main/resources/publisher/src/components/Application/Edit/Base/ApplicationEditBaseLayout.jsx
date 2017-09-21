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
import './baseLayout.css';
import ReleaseManager from '../../Release/ReleaseMgtBase/ReleaseManager';
import {Button, Col, Row} from "reactstrap";

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
                return ("Step 1")
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
                    <Col>Application Name</Col>
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
                        <Row >
                            <Col xs="3 offset-9">
                                <Button>Save</Button>
                            </Col>
                        </Row>
                        </div>
                    </Col>
                </Row>

                {/*<Row>*/}
                {/*/!* Contains the application Name and Save button*!/*/}
                {/*<div id="application-edit-header">*/}
                {/*<Col>*/}
                {/*<span className="application-header-text">*/}
                {/*Header*/}
                {/*</span>*/}
                {/*</Col>*/}
                {/*<Col>*/}
                {/*<Button id="app-save-btn" className="save-btn">Save</Button>*/}
                {/*</Col>*/}
                {/*</div>*/}
                {/*</Row>*/}
                {/*<div id="application-edit-main-container">*/}
                {/*<Row>*/}
                {/*<Col xs="6" sm="4">*/}

                {/*/!* Contains side bar items, General, App Release, Package Manager *!/*/}
                {/*<div className="tab">*/}
                {/*<button className={this.state.general} value={1} onClick={this.handleClick.bind(this)}>*/}
                {/*General*/}
                {/*</button>*/}
                {/*<button className={this.state.release} value={2} onClick={this.handleClick.bind(this)}>*/}
                {/*App*/}
                {/*Releases*/}
                {/*</button>*/}
                {/*<button className={this.state.pkgmgt} value={3} onClick={this.handleClick.bind(this)}>*/}
                {/*Package Manager*/}
                {/*</button>*/}
                {/*</div>*/}
                {/*</Col>*/}
                {/*<Col xs="6" sm="4">*/}
                {/*<div id="application-edit-outer-content">*/}
                {/*/!* Application edit content *!/*/}
                {/*<div id="application-edit-content">*/}
                {/*{this.getTabContent(this.state.activeTab)}*/}
                {/*</div>*/}
                {/*</div>*/}
                {/*</Col>*/}
                {/*</Row>*/}
                {/*</div>*/}
            </div>
        )
    }
}

export default ApplicationEdit;
