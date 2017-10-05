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
import AuthHandler from "../../../../api/authHandler";
import PlatformMgtApi from "../../../../api/platformMgtApi";
import {Button, FormFeedback, FormGroup, Input, Label, ModalFooter} from 'reactstrap';
import {FormattedMessage} from 'react-intl';
import * as validator from '../../../../common/validator';

/**
 * The first step of the application creation wizard.
 * This contains following components:
 *      * Application Title
 *      * Store Type
 *      * Application Platform
 *
 * Parent Component: Create
 * Props:
 *      1. onNextClick: {type: function, Invokes onNextClick function of parent component}
 *      2. setData : {type: function, Sets current form data to the state of the parent component}
 *      3. removeData: {type: function, Invokes the removeStepData function click of parent}
 * */
class Step2 extends Component {
    constructor() {
        super();
        this.setPlatforms = this.setPlatforms.bind(this);
        this.setStepData = this.setStepData.bind(this);
        this.onCancelClick = this.onCancelClick.bind(this);
        this.onBackClick = this.onBackClick.bind(this);
        this.validate = this.validate.bind(this);
        this.platforms = [];
        this.state = {
            errors: {},
            store: 1,
            platformSelectedIndex: 0,
            platform: {},
            platforms: []
        };
    }

    componentWillMount() {
        const {defaultData} = this.props;

        if (defaultData) {
            this.setState(defaultData);
        }
    }

    componentDidMount() {
        //Get the list of available platforms and set to the state.
        PlatformMgtApi.getPlatforms().then(response => {
            this.setPlatforms(response.data);
        }).catch(err => {
            AuthHandler.unauthorizedErrorHandler(err);
        })
    }

    /**
     * Extract the platforms from the response data and populate the state.
     * @param platforms: The array returned as the response.
     * */
    setPlatforms(platforms) {
        let tmpPlatforms = [];
        for (let index in platforms) {
            let platform = {};
            platform = platforms[index];
            tmpPlatforms.push(platform);
        }
        this.setState({platforms: tmpPlatforms, platformSelectedIndex: 0})
    }

    /**
     * Persist the current form data to the state.
     * */
    setStepData() {
        const {store, platform} = this.state;
        let data = {
            store: store,
            platform: platform[0]
        };

        const {errorCount, errors} = this.validate();

        if (errorCount > 0) {
            this.setState({errors: errors})
        } else {
            this.props.setStepData("platform", data);
        }
    }

    onCancelClick() {
        this.props.close();
    }

    onBackClick() {
        this.props.handlePrev();
    }

    validate() {
        const {store, platform} = this.state;
        let errors = {};
        let errorCount = 0;
        if (!validator.validateEmptyObject(platform)) {
            errorCount++;
            errors.platform = "You must select an application platform!"
        }
        return {errorCount, errors};
    }

    /**
     * Triggers when changing the Platform selection.
     * */
    onChangePlatform(event) {
        let id = event.target.value;
        let selectedPlatform = this.state.platforms.filter((platform) => {
            return platform.identifier === id;
        });
        this.setState({platform: selectedPlatform});
    };

    /**
     * Triggers when changing the Store selection.
     * */
    onChangeStore(event) {
        this.setState({store: event.target.value});
    };

    render() {
        return (
            <div>

                <FormGroup>
                    <Label for="store">Store Type</Label>
                    <Input type="select" name="store" className="input-custom" onChange={this.onChangeStore.bind(this)}>
                        <option>Enterprise</option>
                        <option>Public</option>
                    </Input>
                </FormGroup>
                <FormGroup>
                    <Label for="store"><FormattedMessage id='Platform' defaultMessage='Platform'/></Label>
                    <Input
                        required
                        type="select"
                        name="store"
                        onChange={this.onChangePlatform.bind(this)}
                    >
                        <option id="app-visibility-default" disabled selected>Select the Application Platform</option>
                        {this.state.platforms.length > 0 ? this.state.platforms.map(platform => {
                            return (
                                <option value={platform.identifier} key={platform.identifier}>
                                    {platform.name}
                                </option>
                            )
                        }) : <option><FormattedMessage id='No.Platform' defaultMessage='No Platforms'/></option>}
                    </Input>
                    <FormFeedback id="form-error">{this.state.errors.platform}</FormFeedback>
                </FormGroup>
                <ModalFooter>
                    <Button className="custom-flat primary-flat" onClick={this.onBackClick}>Back</Button>
                    <Button className="custom-flat danger-flat" onClick={this.onCancelClick}>Cancel</Button>
                    <Button className="custom-raised primary" onClick={this.setStepData}>Continue</Button>
                </ModalFooter>
            </div>
        );
    }
}

Step2.propTypes = {
    handleNext: PropTypes.func,
    setData: PropTypes.func,
    removeData: PropTypes.func
};

export default Step2;
