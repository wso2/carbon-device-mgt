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
import Switch from "../../../UIComponents/Switch/Switch";
import Chip from "../../../UIComponents/Chip/Chip";


/**
 * Enable : switch
 * Share between tenants: switch
 * Tags: input
 * */
class Configure extends Component {
    constructor() {
        super();
        this.onCancelClick = this.onCancelClick.bind(this);
        this.onBackClick = this.onBackClick.bind(this);
        this.setStepData = this.setStepData.bind(this);
        this.state = {
            defValue: "",
            tags: [],
            enabled: false,
            shared: false,
            defaultTenantMapping: true,
            errors: {}
        }
    }


    /**
     * Create a tag on Enter key press and set it to the state.
     * Clears the tags text field.
     * Chip gets two parameters: Key and value.
     * */
    addTags(event) {
        let tags = this.state.tags;
        if (event.charCode === 13) {
            event.preventDefault();
            tags.push({key: Math.floor(Math.random() * 1000), value: event.target.value});
            this.setState({tags, defValue: ""}, console.log(tags));
        }
    }

    /**
     * Set the value for tag.
     * */
    handleTagChange(event) {
        let defaultValue = this.state.defValue;
        defaultValue = event.target.value;
        this.setState({defValue: defaultValue})
    }

    /**
     * Handles Chip delete function.
     * Removes the tag from state.tags
     * */
    handleRequestDelete(key) {
        let chipData = this.state.tags;
        const chipToDelete = chipData.map((chip) => chip.key).indexOf(key);
        chipData.splice(chipToDelete, 1);
        this.setState({tags: chipData});
    };

    onCancelClick() {
        this.props.close();
    }

    onBackClick() {
        this.props.handlePrev();
    }

    setStepData() {
        const {shared, enabled, tags, defaultTenantMapping} = this.state;

        let config = {
            shared: shared,
            enabled: enabled,
            defaultTenantMapping: defaultTenantMapping,
            tags: tags
        };

        this.props.setStepData("configure", config);
    }

    onEnabledChanged() {
        let enabled = this.state.enabled;
        this.setState({enabled: !enabled});
    }

    onAllTenantsChanged() {
        let allTenants = this.state.allTenants;
        this.setState({allTenants: !allTenants});
    }

    render() {
        return (
            <div>
                <ModalBody>
                    <FormGroup>
                        <div id="app-release-switch-content">
                            <div id="app-release-switch-label">
                                <Label for="app-release-switch">
                                    <FormattedMessage id="Platform.Enable" defaultMessage="Platform.Enable"/>
                                </Label>
                            </div>
                            <div id="app-release-switch">
                                <Switch
                                    name="enabled"
                                    id="app-release-switch"
                                    onChange={this.onEnabledChanged.bind(this)}
                                />
                            </div>
                        </div>
                    </FormGroup>
                    <br/>
                    <FormGroup>
                        <div id="app-release-switch-content">
                            <div id="app-release-switch-label">
                                <Label for="app-release-switch">
                                    <FormattedMessage id="Share.with.Tenants" defaultMessage="Share.with.Tenants"/>
                                </Label>
                            </div>
                            <div id="app-release-switch">
                                <Switch
                                    name="share"
                                    id="app-release-switch"
                                    onChange={this.onAllTenantsChanged.bind(this)}
                                />
                            </div>
                        </div>
                    </FormGroup>
                    <br/>
                    <FormGroup>
                        <Label for="app-tags"><FormattedMessage id='Tags' defaultMessage='Tags'/>*</Label>
                        <Input
                            required
                            type="text"
                            value={this.state.defValue}
                            name="app-tags"
                            id="app-tags"
                            onChange={this.handleTagChange.bind(this)}
                            onKeyPress={this.addTags.bind(this)}
                        />
                        <div id="batch-content">
                            {this.state.tags.map(tag => {
                                    return (
                                        <Chip
                                            key={tag.key}
                                            content={tag}
                                            onDelete={this.handleRequestDelete.bind(this)}
                                        />
                                    )
                                }
                            )}
                        </div>
                        <FormFeedback id="form-error">{this.state.errors.tags}</FormFeedback>
                    </FormGroup>
                </ModalBody>
                <ModalFooter className="custom-footer row">
                    <div className="footer-back-btn col">
                        <Button className="custom-flat primary-flat" onClick={this.onBackClick}>
                            <FormattedMessage id="Back" defaultMessage="Back"/>
                        </Button>
                    </div>
                    <div className="footer-main-btn col">
                        <Button className="custom-flat danger-flat" onClick={this.onCancelClick}>
                            <FormattedMessage id="Cancel" defaultMessage="Cancel"/>
                        </Button>
                        <Button className="custom-flat primary-flat" onClick={this.setStepData}>
                            <FormattedMessage id="Next" defaultMessage="Next"/>
                        </Button>
                    </div>
                </ModalFooter>
            </div>
        )
    }

}

export default Configure;
