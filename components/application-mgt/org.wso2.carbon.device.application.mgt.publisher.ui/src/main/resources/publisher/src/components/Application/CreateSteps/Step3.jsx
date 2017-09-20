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
import Theme from '../../../theme';
import Chip from 'material-ui/Chip';
import Dropzone from 'react-dropzone';
import React, {Component} from 'react';
import MenuItem from 'material-ui/MenuItem';
import SelectField from 'material-ui/SelectField';
import {FormGroup, Label} from 'reactstrap';


/**
 * The Third step of application create wizard.
 * This contains following components.
 *      * Screenshots
 *      * Banner
 *      * Icon
 *
 * Parent Component: Create
 * Props:
 *      * handleNext : {type: function, Invokes handleNext function in Parent.}
 *      * handlePrev : {type: function, Invokes handlePrev function in Parent}
 *      * setData : {type: function, Invokes setStepData function in Parent}
 *      * removeData : {type: Invokes removeStepData function in Parent}
 * */
class Step3 extends Component {
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

    componentWillMount() {
        /**
         *Loading the theme files based on the the user-preference.
         */
        Theme.insertThemingScripts(this.scriptId);
    }

    componentWillUnmount() {
        Theme.removeThemingScripts(this.scriptId);
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
     * Creates Chip array from state.tags.
     * */
    renderChip(data) {
        return (
            <Chip
                key={data.key}
                onRequestDelete={() => this.handleRequestDelete(data.key)}
                className="applicationCreateChip">
                {data.value}
            </Chip>
        );
    }

    onVisibilitySelect(event, index, value) {
        console.log(value);
        let comp = <SelectField> <MenuItem value={0} primaryText="Public"/>
            <MenuItem value={1} primaryText="Roles"/>
            <MenuItem value={2} primaryText="Devices"/> </SelectField>;
        if (value === 1) {
            this.setState({visibilityComponent: comp});
        } else if (value === 2) {

        } else {

        }
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
        let stepData = {
            icon: this.state.icon,
            name: this.state.name,
            tags: this.state.tags,
            banner: this.state.banner,
            category: this.categories[this.state.category],
            identifier: this.state.identifier,
            screenshots: this.state.screenshots,
            description: this.state.description,
            shortDescription: this.state.shortDescription
        };

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

    /**
     * Removed user uploaded banner.
     * */
    removeBanner(event, d) {
        console.log(event, d);
        this.setState({banner: []});
    };

    /**
     * Removes uploaded icon.
     * */
    removeIcon(event) {
        this.setState({icon: []});
    };

    /**
     * Removes selected screenshot.
     * */
    removeScreenshot(event) {
        console.log(event.target)
    };

    render() {
        console.log(this.state.visibilityComponent);
        return (
            <div className="createStep2Content">
                <div>
                    <div>
                        <div>
                            <FormGroup>
                                <Label for="app-screenshots">Screenshots*</Label>
                                <span className="image-sub-title"> (600 X 800 32 bit PNG)</span>
                                <div id="screenshot-container">
                                    {this.state.screenshots.map((tile) => (
                                        <button id="img-btn-screenshot" style={{height: '210px', width: '410px'}}
                                                onMouseEnter={() => {
                                                    console.log("Mouse Entered")
                                                }}>
                                            {console.log(tile[0].preview)}
                                            <img style={{height: '200px', width: '400px'}} src={tile[0].preview}/>
                                        </button>
                                    ))}
                                    {this.state.screenshots.length < 3 ?
                                        <Dropzone
                                            className="applicationCreateScreenshotDropZone"
                                            accept="image/jpeg, image/png"
                                            onDrop={(screenshots, rejected) => {
                                                let tmpScreenshots = this.state.screenshots;
                                                tmpScreenshots.push(screenshots);
                                                console.log(screenshots);
                                                this.setState({
                                                    screenshots: tmpScreenshots
                                                });
                                            }}
                                        >
                                            <p className="applicationCreateScreenshotp">+</p>
                                        </Dropzone> : <div/>}
                                </div>
                            </FormGroup>
                        </div>
                        <div style={{display: 'flex'}}>
                            <div style={{float: 'left', marginRight: '15px'}}>
                                <FormGroup>
                                    <Label for="app-icon">Icon*</Label>
                                    <span className="image-sub-title"> (512 X 512 32 bit PNG)</span>
                                    <div id="app-icon-container">
                                        {this.state.icon.map((tile) => (
                                            <button onMouseEnter={() => {
                                                console.log("Mouse Entered")
                                            }}>
                                                <img style={{height: '200px', width: '200px'}} src={tile.preview}/>
                                            </button>
                                        ))}
                                    </div>

                                    {this.state.icon.length === 0 ?
                                        <Dropzone
                                            className="applicationCreateIconDropZone"
                                            accept="image/jpeg, image/png"
                                            onDrop={(icon, rejected) => {
                                                this.setState({icon, rejected});
                                            }}
                                        >
                                            <p className="applicationCreateIconp">+</p>
                                        </Dropzone> : <div/>}
                                </FormGroup>
                            </div>
                            <div style={{marginLeft: '15px'}}>
                                <FormGroup>
                                    <Label for="app-banner">Banner*</Label>
                                    <span className="image-sub-title"> (1000 X 400 32 bit PNG)</span>
                                    <div id="app-banner-container">
                                        {this.state.banner.map((tile) => (
                                            <button onMouseEnter={() => {
                                                console.log("Mouse Entered")
                                            }}>
                                                <img style={{height: '200px', width: '400px'}} src={tile.preview}/>
                                            </button>
                                        ))}
                                        {this.state.banner.length === 0 ?
                                            <Dropzone
                                                className="applicationCreateBannerDropZone"
                                                accept="image/jpeg, image/png"
                                                onDrop={(banner, rejected) => {
                                                    this.setState({banner, rejected});
                                                }}
                                            >
                                                <p className="applicationCreateBannerp">+</p>
                                            </Dropzone> : <div/>
                                        }
                                    </div>
                                </FormGroup>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

Step3.prototypes = {
    handleNext: PropTypes.func,
    handlePrev: PropTypes.func,
    setData: PropTypes.func,
    removeData: PropTypes.func
};

export default Step3;
