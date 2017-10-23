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
import {Button, FormFeedback, FormGroup, Input, Label, ModalBody, ModalFooter} from "reactstrap";
import {FormattedMessage} from "react-intl";
import * as validator from '../../../../common/validator';
import Dropzone from "react-dropzone";

/**
 * Name
 * Description
 * Identifier
 * Icon
 * */
class General extends Component {
    constructor() {
        super();
        this.onTextFieldChange = this.onTextFieldChange.bind(this);
        this.onNextClick = this.onNextClick.bind(this);
        this.onCancelClick = this.onCancelClick.bind(this);
        this.state = {
            name: "",
            description: "",
            identifier: "",
            errors: {},
            icon: []
        }

    }

    onNextClick() {
        const {name, description, identifier, icon} = this.state;

        let general = {
            name: name,
            description: description,
            identifier: identifier,
            icon: icon
        };

        let {errorCount, errors} = this.validate();

        if (errorCount !== 0) {
            this.setState({errors: errors});
        } else {
            this.props.setStepData("general", general);
        }

    }

    onCancelClick() {
        this.props.close();
    }

    onTextFieldChange(event) {
        let field = event.target.name;
        let value = event.target.value;

        switch (field) {
            case("platformName") : {
                this.setState({name: value});
                break;
            }
            case("platformDescription") : {
                this.setState({description: value});
                break;
            }
            case("platformId") : {
                this.setState({identifier: value});
                break;
            }
        }
    }

    validate() {
        const {name, identifier, description} = this.state;
        let errorCount = 0;
        let errors = {};

        if (validator.validateNull(name)) {
            errorCount++;
            errors.name = "Platform Name is Required!"
        }

        if (validator.validateNull(identifier)) {
            errorCount++;
            errors.identifier = "Platform Identifier is Required!"
        }

        if (validator.validateNull(description)) {
            errorCount++;
            errors.description = "Platform Desciption is Required!"
        }
        return {errorCount, errors};
    }

    render() {
        return (
            <div>
                <ModalBody>
                    <FormGroup>
                        <Label for="platform-name">
                            <FormattedMessage id="Name" defaultMessage="Name"/>*
                        </Label>
                        <Input required type="text" name="platformName" id="platform-name"
                               onChange={this.onTextFieldChange}/>
                        <FormFeedback id="form-error">{this.state.errors.name}</FormFeedback>
                    </FormGroup>
                    <FormGroup>
                        <Label for="platform-description">
                            <FormattedMessage id="Description" defaultMessage="Description"/>*
                        </Label>
                        <Input required type="textarea" name="platformDescription" id="platform-description"
                               onChange={this.onTextFieldChange}/>
                        <FormFeedback id="form-error">{this.state.errors.description}</FormFeedback>
                    </FormGroup>
                    <FormGroup>
                        <Label for="platform-id">
                            <FormattedMessage id="Identifier" defaultMessage="Identifier"/>*
                        </Label>
                        <Input
                            required
                            type="text"
                            name="platformId"
                            id="platform-id"
                            onChange={this.onTextFieldChange}/>
                        <FormFeedback id="form-error">{this.state.errors.identifier}</FormFeedback>
                    </FormGroup>
                    <FormGroup>
                        <Label for="app-icon">
                            <FormattedMessage id='Icon' defaultMessage='Icon'/>
                        </Label>
                        <span className="image-sub-title"> (512 X 512 32 bit PNG)</span>
                        <div id="app-icon-container">
                            {this.state.icon.map((tile) => (
                                <div id="app-image-icon">
                                    <img src={tile.preview} height={200} width={200}/>
                                </div>
                            ))}

                            {this.state.icon.length === 0 ?
                                <Dropzone
                                    className="application-create-icon-dropzone"
                                    accept="image/jpeg, image/png"
                                    onDrop={(icon, rejected) => {
                                        this.setState({icon, rejected});
                                    }}
                                >
                                    <i className="fw fw-add"></i>
                                </Dropzone> : <div/>}
                        </div>
                        <FormFeedback id="form-error">{this.state.errors.icon}</FormFeedback>
                    </FormGroup>
                </ModalBody>
                <ModalFooter>
                    <Button className="custom-flat danger-flat" onClick={this.onCancelClick}>
                        <FormattedMessage id="Cancel" defaultMessage="Cancel"/>
                    </Button>
                    <Button className="custom-raised primary" onClick={this.onNextClick}>
                        <FormattedMessage id="Next" defaultMessage="Next"/>
                    </Button>
                </ModalFooter>
            </div>
        )
    }

}

export default General;
