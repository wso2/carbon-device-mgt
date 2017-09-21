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
import './createRelease.css';
import {Button, Col, FormGroup, Input, Label, Row} from "reactstrap";

class UploadPackage extends Component {

    constructor() {
        super();
        this.handleBack = this.handleBack.bind(this)
    }

    handleBack() {
        this.props.backToRelease();
    }

    render() {
        const {selectedChannel} = this.props;
        return (
            <div>
                <Row>
                    <div className="release-header">
                        <a onClick={this.handleBack}>{"<-"}</a>
                        <span id="create-release-header">
                            <strong>New Release for {selectedChannel}</strong>
                        </span>
                    </div>
                </Row>
                <Row>
                    <div className="release-header">
                        <span id="create-release-header">
                            <strong>Upload Package File</strong>
                        </span>
                    </div>
                </Row>
                <Row>
                    <Col xs="3">
                        <Button>Upload</Button>
                    </Col>
                    <Col xs="3">
                        <Button>Select from package library</Button>
                    </Col>
                </Row>
                <Row>
                    <div className="release-detail-content">
                        <form>
                            <FormGroup>
                                <Label>Release Name *</Label>
                                <Input
                                    required
                                    type="text"
                                />
                            </FormGroup>
                            <FormGroup>
                                <Label>Release Notes *</Label>
                                <Input
                                    required
                                    type="textarea"
                                />
                            </FormGroup>
                            <div className="form-btn">
                                <Button>Send for Review</Button>
                            </div>
                        </form>
                    </div>
                </Row>
            </div>
        );
    }
}

UploadPackage.protoTypes = {
    backToRelease: PropTypes.func,
    channel: PropTypes.string
};

export default UploadPackage;
