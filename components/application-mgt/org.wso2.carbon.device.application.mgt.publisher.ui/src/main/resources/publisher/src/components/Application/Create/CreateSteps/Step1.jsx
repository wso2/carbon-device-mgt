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
 *      * handleNext : {type: function, Invokes handleNext function in Parent.}
 *      * handlePrev : {type: function, Invokes handlePrev function in Parent}
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
        this.scriptId = "application-create-step2";
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
     * Invokes the handleNext function in Create component.
     * */
    handleNext() {
        let fields = [{name: "Title", value: this.state.title},
            {name: "Short Description", value: this.state.shortDescription},
            {name: "Description", value: this.state.description},
            {name: "Banner", value: this.state.banner},
            {name: "Screenshots", value: this.state.screenshots},
            {name: "Identifier", value: this.state.identifier},
            {name: "Icon", value: this.state.icon}];
        this.validate(fields);
    }

    /**
     * Invokes the handlePrev function in Create component.
     * */
    handlePrev() {
        this.props.handlePrev();
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
     * Validate the form.
     * */
    validate(fields) {
        let errors = {};
        let errorsPresent = false;
        fields.forEach(function (field) {
            switch (field.name) {
                case 'Title': {
                    if (field.value === "") {
                        errors[field.name] = field.name + " is required!";
                        errorsPresent = true;
                    } else {
                        errorsPresent = false;
                    }
                    break;
                }
                case 'Identifier': {
                    if (field.value === "") {
                        errors[field.name] = field.name + " is required!";
                        errorsPresent = true;
                    } else {
                        errorsPresent = false;
                    }
                    break;
                }
                case 'Short Description': {
                    if (field.value === "") {
                        errors[field.name] = field.name + " is required!";
                        errorsPresent = true;
                    } else {
                        errorsPresent = false;
                    }
                    break;
                }
                case 'Description': {
                    if (field.value === "") {
                        errors[field.name] = field.name + " is required!";
                        errorsPresent = true;
                    } else {
                        errorsPresent = false;
                    }
                    break;
                }
                case 'Banner': {
                    if (field.value.length === 0) {
                        errors[field.name] = field.name + " is required!";
                        errorsPresent = true;
                    } else {
                        errorsPresent = false;
                    }
                    break;
                }
                case 'Icon': {
                    if (field.value.length === 0) {
                        errors[field.name] = field.name + " is required!";
                        errorsPresent = true;
                    } else {
                        errorsPresent = false;
                    }
                    break;
                }
                case 'Screenshots': {
                    if (field.value.length < 3) {
                        errors[field.name] = "3 " + field.name + " are required!";
                        errorsPresent = true;
                    } else {
                        errorsPresent = false;
                    }
                    break;
                }
            }
        });

        if (!errorsPresent) {
            this.setStepData();
        } else {
            this.setState({errors: errors}, console.log(errors));
        }
    }

    /**
     * Creates an object with the current step data and persist in the parent.
     * */
    setStepData() {
        let stepData = {};

        this.props.setData("step2", {step: stepData});
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
                            <Label for="app-title">Title*</Label>
                            <Input
                                required
                                type="text"
                                name="appName"
                                id="app-title"
                            />
                        </FormGroup>
                        <FormGroup>
                            <Label for="app-description">Description*</Label>
                            <Input
                                required
                                type="textarea"
                                name="appDescription"
                                id="app-description"
                            />
                        </FormGroup>
                        <FormGroup>
                            <Label for="app-category">Category</Label>
                            <Input
                                type="select"
                                name="category"
                                id="app-category"
                            >
                                <option>Business</option>
                            </Input>
                        </FormGroup>
                        <FormGroup>
                            <Label for="app-visibility">Visibility</Label>
                            <Input
                                type="select"
                                name="visibility"
                                id="app-visibility"
                            >
                                <option>Devices</option>
                                <option>Roles</option>
                                <option>Groups</option>
                            </Input>
                        </FormGroup>
                        <FormGroup>
                            <Label for="app-tags">Tags*</Label>
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
