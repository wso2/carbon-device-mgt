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

import Theme from '../../../theme';
import PropTypes from 'prop-types';
import React, {Component} from 'react';
import MenuItem from 'material-ui/MenuItem';
import SelectField from 'material-ui/SelectField';
import AuthHandler from "../../../api/authHandler";
import RaisedButton from 'material-ui/RaisedButton';
import PlatformMgtApi from "../../../api/platformMgtApi";

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

    componentWillMount() {
        /**
         *Loading the theme files based on the the user-preference.
         */
       Theme.insertThemingScripts(this.scriptId);
    }

    componentWillUnmount() {
        Theme.removeThemingScripts(this.scriptId);
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
        this.setState({platforms: tmpPlatforms, platformSelectedIndex: 0, platform: tmpPlatforms[0].identifier})
    }

    /**
     * Persist the current form data to the state.
     * */
    setStepData() {
        console.log("Platforms",this.state.platforms);
        let step = {
            store: this.state.store,
            platform: this.state.platforms[this.state.platformSelectedIndex]
        };
        console.log(step);
        this.props.setData("step1", {step: step});
    }

    /**
     * Handles Next button click.
     *  Validates the form.
     *  Sets the data to the state.
     *  Invokes the handleNext method of Create component.
     * */
    handleClick() {
        this.setStepData();
    }

    /**
     * Triggers when changing the Platform selection.
     * */
    onChangePlatform(event, index, value) {
        console.log(this.state.platforms[index]);
        this.setState({platform: this.state.platforms[index].identifier, platformSelectedIndex: index});
    };

    /**
     * Triggers when changing the Store selection.
     * */
    onChangeStore(event, index, value) {
        this.setState({store: value});
    };

    render() {
        return (
            <div>
                <div className="creatediv">
                    <div>
                        <div>
                            <SelectField
                                floatingLabelText="Store Type*"
                                value={this.state.store}
                                floatingLabelFixed={true}
                                onChange={this.onChangeStore.bind(this)}
                            >
                                <MenuItem value={0} primaryText="Enterprise"/>
                                <MenuItem value={1} primaryText="Public"/>
                            </SelectField>
                            <br/>
                            <SelectField
                                floatingLabelText="Platform*"
                                value={this.state.platform}
                                floatingLabelFixed={true}
                                onChange={this.onChangePlatform.bind(this)}
                            >
                                {this.state.platforms.length > 0 ? this.state.platforms.map(platform => {
                                    return (
                                        <MenuItem
                                            key={Math.random()}
                                            value={platform.identifier}
                                            primaryText={platform.name}
                                        />
                                    )
                                }) : <div/>}


                            </SelectField>
                        </div>
                        <br/>
                        <br/>
                        <div className="nextButton">
                            <RaisedButton
                                label="Next >"
                                primary={true}
                                onClick={this.handleClick.bind(this)}
                            />
                        </div>
                    </div>
                </div>
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
