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
import {FormGroup, Input, Label} from 'reactstrap';

/**
 * The first step of the application creation wizard.
 * This contains following components:
 *      * Application Title
 *      * Store Type
 *      * Application Platform
 *
 * Parent Component: Create
 * Props:
 *      1. handleNext: {type: function, Invokes handleNext function of parent component}
 *      2. setData : {type: function, Sets current form data to the state of the parent component}
 *      3. removeData: {type: function, Invokes the removeStepData function click of parent}
 * */
class Step1 extends Component {
    constructor() {
        super();
        this.setPlatforms = this.setPlatforms.bind(this);
        this.setStepData = this.setStepData.bind(this);
        this.cancel = this.cancel.bind(this);
        this.platforms = [];
        this.state = {
            finished: false,
            stepIndex: 0,
            store: 1,
            platformSelectedIndex: 0,
            platform: "",
            platforms: [],
            stepData: [],
            title: "",
            titleError: ""
        };
        this.scriptId = "application-create-step1";
    }

    componentDidMount() {
        //Get the list of available platforms and set to the state.
        PlatformMgtApi.getPlatforms().then(response => {
            console.log(response);
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
        this.setState({platforms: tmpPlatforms, platformSelectedIndex: 0, platform: tmpPlatforms[0].name})
    }

    /**
     * Persist the current form data to the state.
     * */
    setStepData() {
        console.log("Platforms", this.state.platforms);
        let step = {
            store: this.state.store,
            platform: this.state.platforms[this.state.platformSelectedIndex]
        };
        console.log(step);
        this.props.setData("step1", {step: step});
    }

    cancel() {

    }

    /**
     * Triggers when changing the Platform selection.
     * */
    onChangePlatform(event) {
        console.log(event.target.value, this.state.platforms);
        let id = event.target.value;
        let selectedPlatform = this.state.platforms.filter((platform) => {
            return platform.identifier === id;
        });
        console.log(selectedPlatform);

        this.setState({platform: selectedPlatform});
    };

    /**
     * Triggers when changing the Store selection.
     * */
    onChangeStore(event) {
        console.log(event.target.value);
        this.setState({store: event.target.value});
    };

    render() {
        return (
            <div>

                <FormGroup>
                    <Label for="store">Store Type</Label>
                    <Input
                        type="select"
                        name="store"
                        id="store"
                        className="input-custom"
                        onChange={this.onChangeStore.bind(this)}
                    >
                        <option>Enterprise</option>
                        <option>Public</option>
                    </Input>
                </FormGroup>
                <FormGroup>
                    <Label for="store">Platform</Label>
                    <Input
                        type="select"
                        name="store"
                        id="store"
                        onChange={this.onChangePlatform.bind(this)}
                    >
                        {this.state.platforms.length > 0 ? this.state.platforms.map(platform => {
                            return (
                                <option value={platform.identifier}>
                                    {platform.name}
                                </option>
                            )
                        }) : <option>No Platforms</option>}
                    </Input>
                </FormGroup>
            </div>
        );
    }
}

Step1.propTypes = {
    handleNext: PropTypes.func,
    setData: PropTypes.func,
    removeData: PropTypes.func
};

export default Step1;
