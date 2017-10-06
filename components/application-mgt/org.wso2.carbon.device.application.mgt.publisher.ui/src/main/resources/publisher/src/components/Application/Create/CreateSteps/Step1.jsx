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
import {FormattedMessage} from 'react-intl';
import {Button, Form, FormFeedback, FormGroup, Input, Label, ModalFooter} from 'reactstrap';
import * as validator from '../../../../common/validator';
import Chip from "../../../UIComponents/Chip/Chip";

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
        this.onTextFieldChange = this.onTextFieldChange.bind(this);
        this.setStepData = this.setStepData.bind(this);
        this.onCancelClick = this.onCancelClick.bind(this);
        this.onVisibilityChange = this.onVisibilityChange.bind(this);
        this.onVisibilityItemSelect = this.onVisibilityItemSelect.bind(this);
        this.handleRequestDelete = this.handleRequestDelete.bind(this);
        this.validate = this.validate.bind(this);
        this.state = {
            tags: [],
            name: "",
            errors: {},
            defValue: "",
            category: {},
            visibility: {type: "PUBLIC", allowedList: []},
            description: "",
            shortDescription: ""
        };
    }

    componentWillMount() {
        const defaultVals = this.props.defaultData;

        if (defaultVals) {
            this.setState(defaultVals);
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

    /**
     * Creates an object with the current step data and persist in the parent.
     * */
    setStepData() {
        const {name, description, tags, visibility, shortDescription} = this.state;
        let stepData = {
            name: name,
            description: description,
            tags: tags,
            visibility: visibility,
            shortDescription: shortDescription,
            category: {id: 1, name: "business"}
        };

        let {errorCount, errors} = this.validate();

        if (errorCount !== 0) {
            this.setState({errors: errors});
        } else {
            this.props.setStepData("generalInfo", stepData);
        }
    };

    onCancelClick() {
        this.props.close();
    }

    /**
     * Validate the form fields.
     * */
    validate() {
        const {name, description, tags, shortDescription} = this.state;
        let errorCount = 0;
        let errors = {};
        if (validator.validateNull(name)) {
            errorCount++;
            errors.name = "Application Title is Required!";
        }

        if (validator.validateNull(description)) {
            errorCount++;
            errors.description = "Description is Required!"
        }

        if (validator.validateNull(shortDescription)) {
            errorCount++;
            errors.shortDescription = "Short Description is Required!"
        }

        if (!validator.validateEmpty(tags)) {
            errorCount++;
            errors.tags = "You need to enter at least one tag!"
        }
        return {errorCount, errors};
    }

    /**
     * Set text field values to state.
     * */
    onTextFieldChange(event) {
        let field = event.target.name;
        switch (field) {
            case "appName": {
                this.setState({name: event.target.value});
                break;
            }
            case "appDescription": {
                this.setState({description: event.target.value});
                break;
            }
            case "appShortDescription": {
                this.setState({shortDescription: event.target.value});
            }
        }
    };

    onVisibilityChange(event) {
        console.log(event.target.value);
        this.setState({visibility: event.target.value});
    }

    onVisibilityItemSelect(event) {

    }

    render() {

        const {visibility} = this.state;

        let visibilityItem = () => {
            switch (visibility) {
                case("public"): {
                    return <div/>
                }
                case("roles"): {
                    return <FormGroup>
                        <Input
                            type="select"
                            name="visibility-item"
                            id="app-visibility-item"
                            onChange={this.onVisibilityItemSelect}
                        >
                            <option id="app-visibility-default" disabled selected>Select the Roles.</option>
                            <option><Input type="checkbox"/>Role1</option>
                            <option>Role2</option>
                        </Input>
                    </FormGroup>
                }
                case ("groups"): {
                    return <FormGroup>
                        <Input
                            type="select"
                            name="visibility-item"
                            id="app-visibility-item"
                            onChange={this.onVisibilityItemSelect}
                        >
                            <option id="app-visibility-default" disabled selected>Select the Groups.</option>
                            <option>Group1</option>
                            <option>Group2</option>
                        </Input>
                    </FormGroup>
                }
                default: {
                    return <div/>
                }
            }
        };

        return (
            <div>
                <div>
                    <div>
                        <FormGroup>
                            <Label for="app-title">
                                <FormattedMessage id='Title' defaultMessage='Title'/>*
                            </Label>
                            <Input
                                required
                                type="text"
                                name="appName"
                                id="app-title"
                                value={this.state.name}
                                onChange={this.onTextFieldChange}
                            />
                            <FormFeedback id="form-error">{this.state.errors.name}</FormFeedback>
                        </FormGroup>
                        <FormGroup>
                            <Label for="app-short-description">
                                <FormattedMessage id="ShortDescription" defaultMessage="ShortDescription"/>*
                            </Label>
                            <Input
                                required
                                type="textarea"
                                name="appShortDescription"
                                id="app-short-description"
                                value={this.state.shortDescription}
                                onChange={this.onTextFieldChange}
                            />
                            <FormFeedback id="form-error">{this.state.errors.shortDescription}</FormFeedback>
                        </FormGroup>
                        <FormGroup>
                            <Label for="app-description">
                                <FormattedMessage id='Description' defaultMessage='Description'/>*
                            </Label>
                            <Input
                                required
                                type="textarea"
                                name="appDescription"
                                id="app-description"
                                value={this.state.description}
                                onChange={this.onTextFieldChange}
                            />
                            <FormFeedback id="form-error">{this.state.errors.description}</FormFeedback>
                        </FormGroup>
                        <FormGroup>
                            <Label for="app-category">
                                <FormattedMessage id='Category' defaultMessage='Category'/>
                            </Label>
                            <Input
                                type="select"
                                name="category"
                                id="app-category"
                            >
                                <option key={0} value={{id: 0, name: "business"}}>Business</option>
                            </Input>
                        </FormGroup>
                        <FormGroup>
                            <Label for="app-visibility">
                                <FormattedMessage id='Visibility' defaultMessage='Visibility'/>
                            </Label>
                            <Form inline>
                                <FormGroup>
                                    <Input
                                        type="select"
                                        name="visibility"
                                        id="app-visibility"
                                        onChange={this.onVisibilityChange}
                                    >
                                        <option id="app-visibility-default" disabled selected>Select the App Visibility
                                                                                              Option.
                                        </option>
                                        <option key={1}><FormattedMessage id='Devices' defaultMessage='Devices'/>
                                        </option>
                                        <option key={2}><FormattedMessage id='Roles' defaultMessage='Roles'/></option>
                                        <option key={3}><FormattedMessage id='Groups' defaultMessage='Groups'/></option>
                                    </Input>
                                </FormGroup>
                                {visibilityItem()}
                            </Form>
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
                                            <Chip
                                                key={tag.key}
                                                content={tag}
                                                onDelete={this.handleRequestDelete}
                                            />
                                        )
                                    }
                                )}
                            </div>
                            <FormFeedback id="form-error">{this.state.errors.tags}</FormFeedback>
                        </FormGroup>
                    </div>
                </div>
                <ModalFooter>
                    <Button className="custom-flat danger-flat" onClick={this.onCancelClick}>
                        <FormattedMessage id="Cancel" defaultMessage="Cancel"/>
                    </Button>
                    <Button className="custom-raised primary" onClick={this.setStepData}>
                        <FormattedMessage id="Continue" defaultMessage="Continue"/>
                    </Button>
                </ModalFooter>
            </div>
        );
    }
}

Step1.prototypes = {
    handleNext: PropTypes.func,
    handlePrev: PropTypes.func,
    setData: PropTypes.func
};

export default Step1;
