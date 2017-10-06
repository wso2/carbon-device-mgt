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
import * as validator from '../../../../common/validator';
import {Button, FormFeedback, FormGroup, Label, ModalFooter} from 'reactstrap';
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
        this.setStepData = this.setStepData.bind(this);
        this.onBackClick = this.onBackClick.bind(this);
        this.validate = this.validate.bind(this);
        this.onCancelClick = this.onCancelClick.bind(this);
        this.state = {
            icon: [],
            errors: {},
            banner: [],
            screenshots: [],
        };
    }

    componentWillMount() {
        const {defaultData} = this.props;

        this.setState(defaultData);
    }

    /**
     * Creates an object with the current step data and persist in the parent.
     * */
    setStepData() {

        const {icon, banner, screenshots} = this.state;

        let stepData = {
            icon: icon,
            banner: banner,
            screenshots: screenshots
        };

        const {errorCount, errors} = this.validate();

        if (errorCount > 0) {
            this.setState({errors: errors})
        } else {
            this.props.setStepData("screenshots", stepData);
        }

    };

    onCancelClick() {
        this.props.close();
    }

    onBackClick() {
        this.props.handlePrev();
    }

    validate() {
        const {icon, banner, screenshots} = this.state;
        let errors = {}, errorCount = 0;

        if (!validator.validateEmpty(icon)) {
            errorCount++;
            errors.icon = "You must upload an icon image!"
        }

        if (!validator.validateEmpty(banner)) {
            errorCount++;
            errors.banner = "You must upload a banner image!"
        }

        if (!validator.validateEmpty(screenshots)) {
            errorCount++;
            errors.screenshots = "You must upload at least one screenshot image!"
        }

        return {errorCount, errors};
    }

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
                                        this.setState({
                                            screenshots: tmpScreenshots
                                        });
                                    }}
                                >
                                    <i className="fw fw-add"></i>
                                </Dropzone> : <div/>}
                        </div>
                        <FormFeedback id="form-error">{this.state.errors.screenshots}</FormFeedback>
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
                            <FormFeedback id="form-error">{this.state.errors.icon}</FormFeedback>
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
                            <FormFeedback id="form-error">{this.state.errors.banner}</FormFeedback>
                        </FormGroup>
                    </div>
                </div>
                <ModalFooter>
                    <Button className="custom-flat primary-flat" onClick={this.onBackClick}>
                        <FormattedMessage id="Back" defaultMessage="Back"/>
                    </Button>
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

Step3.prototypes = {
    handleNext: PropTypes.func,
    handlePrev: PropTypes.func,
    setData: PropTypes.func,
    removeData: PropTypes.func
};

export default Step3;
