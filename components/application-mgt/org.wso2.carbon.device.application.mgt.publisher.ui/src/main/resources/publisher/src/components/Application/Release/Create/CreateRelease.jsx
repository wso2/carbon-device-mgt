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
import {Button, FormGroup, FormText, Input, Label, Row} from "reactstrap";
import UploadPackage from "./UploadPackage";

class CreateRelease extends Component {
    constructor() {
        super();
        this.onTestMethodChange = this.onTestMethodChange.bind(this);
        this.showUploadArtifacts = this.showUploadArtifacts.bind(this);
        this.handleBack = this.handleBack.bind(this);
        this.backToRelease = this.backToRelease.bind(this);
        this.state = {
            open: true,
            hiddenMain: false
        }
    }

    onTestMethodChange(event) {
        let type = event.target.value;
        if (type !== 'open') {
            this.setState({open: false})
        } else {
            this.setState({open: true})
        }
    }

    showUploadArtifacts() {
        this.setState({hiddenMain: true})
    }

    handleBack() {
        this.props.handleBack();
    }

    backToRelease() {
        this.setState({hiddenMain: false});
    }

    render() {
        const {channel} = this.props;
        console.log(channel);
        return (
            <div>

                {this.state.hiddenMain ?
                    <div>
                        <UploadPackage
                            backToRelease={this.backToRelease}
                            selectedChannel={channel}
                        />
                    </div> :

                    <div>
                        <Row>
                            <div className="release-header">
                                <a onClick={this.handleBack}>{"<-"}</a>
                                <span id="create-release-header">
                            <strong>{channel} Release</strong>
                        </span>
                            </div>
                        </Row>
                        <Row>
                            <div className="release-create">
                                <div>
                            <span>
                                <strong>Create Release</strong>
                            </span>
                                    <p>
                                        {channel === 'Production' ? "" :
                                            "You could create " + channel + " release for your application and let " +
                                            "the test users to test the application for it's stability."}
                                    </p>
                                </div>
                                <div>
                                    <Button id="create-release-btn" onClick={this.showUploadArtifacts}>Create a {channel} Release</Button>
                                </div>
                            </div>
                        </Row>
                        {channel !== 'Production' ?
                            <Row>
                                <div>
                            <span>
                                <strong>Manage Test Method</strong>
                            </span>
                                    <p>
                                        This section allows you to change the test method and the users who would be
                                        able to test your application.
                                    </p>
                                    <div>
                                        <form>
                                            <FormGroup>
                                                <Label for="test-method">Test Method*</Label>
                                                <Input
                                                    required
                                                    type="select"
                                                    name="testMethod"
                                                    id="test-method"
                                                    onChange={this.onTestMethodChange}
                                                >
                                                    <option value="open">Open {channel}</option>
                                                    <option value="closed">Closed {channel}</option>
                                                </Input>
                                            </FormGroup>
                                            {!this.state.open ? (
                                                <FormGroup>
                                                    <Label for="user-list">Users List*</Label>
                                                    <Input
                                                        required
                                                        name="userList"
                                                        id="user-list"
                                                        type="text"
                                                    />
                                                    <FormText color="muted">
                                                        Provide a comma separated list of email
                                                        addresses.
                                                    </FormText>
                                                </FormGroup>
                                            ) : <div/>}
                                            <FormGroup>
                                                <Label for="app-title">Feedback Method*</Label>
                                                <Input
                                                    required
                                                    name="appName"
                                                    id="app-title"
                                                />
                                                <FormText color="muted">
                                                    Provide an Email address or a URL for your users to provide
                                                    feedback on the application.
                                                </FormText>
                                            </FormGroup>
                                            <div>
                                                <Button className="form-btn">Save</Button>
                                            </div>
                                        </form>
                                    </div>
                                </div>
                            </Row> :
                            <div/>
                        }
                    </div>
                }

            </div>
        );
    }
}

CreateRelease.propTypes = {
    channel: PropTypes.string,
    handleBack: PropTypes.func
};

export default CreateRelease;
