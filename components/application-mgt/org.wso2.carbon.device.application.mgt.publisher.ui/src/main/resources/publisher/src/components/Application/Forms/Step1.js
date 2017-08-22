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
import MenuItem from 'material-ui/MenuItem';
import TextField from 'material-ui/TextField';
import SelectField from 'material-ui/SelectField';
import RaisedButton from 'material-ui/RaisedButton';

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
        this.state = {
            finished: false,
            stepIndex: 0,
            store: 1,
            platform: 1,
            stepData: []
        };
    }

    /**
     * Invokes the handleNext function in Create component.
     * */
    handleNext = () => {
        this.props.handleNext();
    };

    /**
     * Persist the current form data to the state.
     * */
    setStepData() {
        this.props.setData("step1", {step: "Dfds"});
        this.handleNext.bind(this);
    }

    /**
     * Handles Next button click.
     *  Validates the form.
     *  Sets the data to the state.
     *  Invokes the handleNext method of Create component.
     * */
    handleClick() {
        this.setStepData();
        this.handleNext();
    }

    /**
     * Triggers when changing the Platform selection.
     * */
    onChangePlatform = (event, index, value) => {
        this.setState({platform: value});
    };

    /**
     * Triggers when changing the Store selection.
     * */
    onChangeStore = (event, index, value) => {
        this.setState({store: value});
    };

    render() {
        const contentStyle = {margin: '0 16px'};
        return (
            <div>
                <div style={contentStyle}>
                    <div>
                        <div>
                            <TextField
                                hintText="Enter a title for your application."
                                floatingLabelText="Title*"
                                floatingLabelFixed={true}
                            /><br/>
                            <SelectField
                                floatingLabelText="Store Type*"
                                value={this.state.store}
                                floatingLabelFixed={true}
                                onChange={this.onChangeStore.bind(this)}
                            >
                                <MenuItem value={1} primaryText="Enterprise"/>
                                <MenuItem value={2} primaryText="Public"/>
                            </SelectField> <br/>
                            <SelectField
                                floatingLabelText="Platform*"
                                value={this.state.platform}
                                floatingLabelFixed={true}
                                onChange={this.onChangePlatform.bind(this)}
                            >
                                <MenuItem value={1} primaryText="Android"/>
                                <MenuItem value={2} primaryText="iOS"/>
                                <MenuItem value={3} primaryText="Web"/>
                            </SelectField>
                        </div>

                        <br/>
                        <br/>
                        <div style={{marginTop: 12}}>
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
