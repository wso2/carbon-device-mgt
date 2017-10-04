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
import Dropzone from 'react-dropzone';
import React, {Component} from 'react';
import {FormGroup, Label} from 'reactstrap';
import AppImage from "../../../UIComponents/AppImage/AppImage";
import {FormattedMessage} from 'react-intl';

/**
 * The Third step of application create wizard.
 * This contains following components.
 *      * Screenshots
 *      * Banner
 *      * Icon
 *
 * Parent Component: Create
 * Props:
 *      * onNextClick : {type: function, Invokes onNextClick function in Parent.}
 *      * onPrevClick : {type: function, Invokes onPrevClick function in Parent}
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
    }

    /**
     * Handles Chip delete function.
     * Removes the tag from state.tags
     * */
    handleRequestDelete(event) {
        this.chipData = this.state.tags;
        console.log(event.target); //TODO: Remove Console log.
        const chipToDelete = this.chipData.map((chip) => chip.value).indexOf(event.target.value);
        this.chipData.splice(chipToDelete, 1);
        this.setState({tags: this.chipData});
    };

    /**
     * Creates an object with the current step data and persist in the parent.
     * */
    setStepData() {
        let stepData = {
            icon: this.state.icon,
            banner: this.state.banner,
            screenshots: this.state.screenshots
        };

        this.props.setData("step2", {step: stepData});
    };

    /**
     * Removed user uploaded banner.
     * */
    removeBanner(event, d) {
        console.log(event, d); //TODO: Remove this
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
        console.log(event.target) //TODO: Remove this.
    };

    //TODO: Remove inline css.
    render() {
        return (
            <div className="createStep2Content">
                <div>
                    <FormGroup>
                        <Label for="app-screenshots">
                            <FormattedMessage id='Screenshots' defaultMessage='Screenshots'/>*
                        </Label>
                        <span className="image-sub-title"> (600 X 800 32 bit PNG)</span>
                        <div id="screenshot-container">
                            {this.state.screenshots.map((tile) => (
                                <div id="app-image-screenshot">
                                    <AppImage image={tile[0].preview}/>
                                </div>
                            ))}
                            {this.state.screenshots.length < 3 ?
                                <Dropzone
                                    className="application-create-screenshot-dropzone"
                                    accept="image/jpeg, image/png"
                                    onDrop={(screenshots, rejected) => {
                                        let tmpScreenshots = this.state.screenshots;
                                        tmpScreenshots.push(screenshots);
                                        console.log(screenshots); //TODO: Remove this
                                        this.setState({
                                            screenshots: tmpScreenshots
                                        });
                                    }}
                                >
                                    <i className="fw fw-add"></i>
                                </Dropzone> : <div/>}
                        </div>
                    </FormGroup>
                </div>
                <div style={{display: 'flex'}}>
                    <div style={{float: 'left', marginRight: '15px'}}>
                        <FormGroup>
                            <Label for="app-icon">
                                <FormattedMessage id='Screenshots' defaultMessage='Screenshots'/>*
                            </Label>
                            <span className="image-sub-title"> (512 X 512 32 bit PNG)</span>
                            <div id="app-icon-container">
                                {this.state.icon.map((tile) => (
                                    <div id="app-image-icon">
                                        <AppImage image={tile.preview}/>
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
                        </FormGroup>
                    </div>
                    <div style={{marginLeft: '15px'}}>
                        <FormGroup>
                            <Label for="app-banner">
                                <FormattedMessage id='Icon' defaultMessage='Icon'/>*
                            </Label>
                            <span className="image-sub-title"> (1000 X 400 32 bit PNG)</span>
                            <div id="app-banner-container">
                                {this.state.banner.map((tile) => (
                                    <div id="app-image-banner">
                                        <AppImage image={tile.preview}/>
                                    </div>
                                ))}
                                {this.state.banner.length === 0 ?
                                    <Dropzone
                                        className="application-create-banner-dropzone"
                                        accept="image/jpeg, image/png"
                                        onDrop={(banner, rejected) => {
                                            this.setState({banner, rejected});
                                        }}
                                    >
                                        <i className="fw fw-add"></i>
                                    </Dropzone> : <div/>
                                }
                            </div>
                        </FormGroup>
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
