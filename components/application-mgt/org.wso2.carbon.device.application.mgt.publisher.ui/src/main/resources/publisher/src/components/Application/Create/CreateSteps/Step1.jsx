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
import {Badge, FormGroup, Input, Label} from 'reactstrap';
import {FormattedMessage} from 'react-intl';

/**
 * The Second step of application create wizard.
 * This contains following components.
 *      * App Title
 *      * Short Description
 *      * Application Description
 *      * Application Visibility
 *      * Application Tags : {Used Material UI Chip component}
 *      * Application Category.
 *      * Platform Specific properties.
 *
 * Parent Component: Create
 * Props:
 *      * onNextClick : {type: function, Invokes onNextClick function in Parent.}
 *      * onPrevClick : {type: function, Invokes onPrevClick function in Parent}
 *      * setData : {type: function, Invokes setStepData function in Parent}
 *      * removeData : {type: Invokes removeStepData function in Parent}
 * */
class Step1 extends Component {
    constructor() {
        super();
        this.state = {
            tags: [],
            icon: [],
            title: "",
            errors: {},
            banner: [],
            defValue: "",
            category: 0,
            visibility: 0,
            description: "",
            screenshots: [],
            identifier: "",
            shortDescription: ""
        };
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
    handleRequestDelete(event) {
        this.chipData = this.state.tags;
        console.log(event.target);
        const chipToDelete = this.chipData.map((chip) => chip.value).indexOf(event.target.value);
        this.chipData.splice(chipToDelete, 1);
        this.setState({tags: this.chipData});
    };

    /**
     * Creates an object with the current step data and persist in the parent.
     * */
    setStepData() {
        let stepData = {};
        this.props.setData("step1", {step: stepData});
    };

    /**
     * Set text field values to state.
     * */
    onTextFieldChange(event, value) {
        let field = event.target.id;
        switch (field) {
            case "name": {
                this.setState({name: value});
                break;
            }
            case "shortDescription": {
                this.setState({shortDescription: value});
                break;
            }
            case "description": {
                this.setState({description: value});
                break;
            }
            case "identifier": {
                this.setState({identifier: value});
                break;
            }
        }
    };

    render() {
        return (
            <div className="createStep2Content">
                <div>
                    <div>
                        <FormGroup>
                            <Label for="app-title">
                                <FormattedMessage id='Title' defaultMessage='Title'/>*
                            </Label>
                            <Input required type="text" name="appName" id="app-title"/>
                        </FormGroup>
                        <FormGroup>
                            <Label for="app-description">
                                <FormattedMessage id='Description' defaultMessage='Description'/>*
                            </Label>
                            <Input required type="textarea" name="appDescription" id="app-description"/>
                        </FormGroup>
                        <FormGroup>
                            <Label for="app-category">
                                <FormattedMessage id='Category' defaultMessage='Category'/>
                            </Label>
                            <Input type="select" name="category" id="app-category">
                                <option>Business</option>
                            </Input>
                        </FormGroup>
                        <FormGroup>
                            <Label for="app-visibility">
                                <FormattedMessage id='Visibility' defaultMessage='Visibility'/>
                            </Label>
                            <Input type="select" name="visibility" id="app-visibility">
                                <option><FormattedMessage id='Devices' defaultMessage='Devices'/></option>
                                <option><FormattedMessage id='Roles' defaultMessage='Roles'/></option>
                                <option><FormattedMessage id='Groups' defaultMessage='Groups'/></option>
                            </Input>
                        </FormGroup>
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
                                            <Badge
                                                style={{margin: '0 2px 0 2px'}}
                                                value={tag.value}
                                                onClick={this.handleRequestDelete.bind(this)}
                                            >
                                                {tag.value}
                                            </Badge>
                                        )
                                    }
                                )}
                            </div>
                        </FormGroup>
                    </div>
                </div>
            </div>
        );
    }
}

Step1.prototypes = {
    handleNext: PropTypes.func,
    handlePrev: PropTypes.func,
    setData: PropTypes.func,
    removeData: PropTypes.func
};

export default Step1;
