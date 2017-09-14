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
import SelectField from 'material-ui/SelectField';
import RaisedButton from 'material-ui/RaisedButton';
import Theme from '../../../theme';

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
            stepData: [],
            title: "",
            titleError: ""
        };
    }

    componentWillMount() {
        /**
         *Loading the theme files based on the the user-preference.
         */
        const selected =
            (Theme.currentThemeType === Theme.defaultThemeType) ? Theme.defaultThemeType : Theme.currentTheme;
        const applicationCreateStep1Css = "application-create-step1.css";
        const applicationCreateStep1Id = "application-create-step1";
        let themePath  =  "/" + Theme.themeFolder + "/" + selected + "/" + applicationCreateStep1Css;
        let promisedConfig = Theme.loadThemeFiles(themePath);
        let styleSheet = document.getElementById(applicationCreateStep1Id);
        let head = document.getElementsByTagName("head")[0];
        let link = document.createElement("link");
        link.type = Theme.styleSheetType;
        link.href = Theme.baseURL + "/" + Theme.appContext + themePath;
        link.id = applicationCreateStep1Id;
        link.rel = Theme.styleSheetRel;
        if (styleSheet !== null) {
            styleSheet.disabled = true;
            styleSheet.parentNode.removeChild(styleSheet);
        }

        promisedConfig.then(function () {
            head.appendChild(link);
        }).catch(function () {
            // If there is no customized css file, load the default one.
            themePath = "/" + Theme.themeFolder + "/" + Theme.defaultThemeType + "/" + applicationCreateStep1Css;
            link.href = Theme.baseURL + "/" + Theme.appContext + themePath;
            head.appendChild(link);
        });
    }

    componentWillUnmount() {
        let styleSheet = document.getElementById("application-create-step1");
        if (styleSheet !== null) {
            styleSheet.disabled = true;
            styleSheet.parentNode.removeChild(styleSheet);
        }
    }

    /**
     * Invokes the handleNext function in Create component.
     * */
    _handleNext = () => {
        this.props.handleNext();
    };

    /**
     * Persist the current form data to the state.
     * */
    _setStepData() {
        var step = {
            store: this.state.store,
            platform: this.state.platform
        };
        this.props.setData("step1", {step: step});
    }

    /**
     * Handles Next button click.
     *  Validates the form.
     *  Sets the data to the state.
     *  Invokes the handleNext method of Create component.
     * */
    _handleClick() {
        this._setStepData();
    }

    /**
     * Triggers when changing the Platform selection.
     * */
    _onChangePlatform = (event, index, value) => {
        console.log(value);
        this.setState({platform: value});
    };

    /**
     * Triggers when changing the Store selection.
     * */
    _onChangeStore = (event, index, value) => {
        this.setState({store: value});
    };

    /**
     * Triggers when user types on Title text field.
     * */
    _onChangeTitle = (event, value) => {
        this.setState({title: value});
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
                                onChange={this._onChangeStore.bind(this)}
                            >
                                <MenuItem value={1} primaryText="Enterprise"/>
                                <MenuItem value={2} primaryText="Public"/>
                            </SelectField> <br/>
                            <SelectField
                                floatingLabelText="Platform*"
                                value={this.state.platform}
                                floatingLabelFixed={true}
                                onChange={this._onChangePlatform.bind(this)}
                            >
                                <MenuItem value={1} primaryText="Android"/>
                                <MenuItem value={2} primaryText="iOS"/>
                                <MenuItem value={{name: "Web", id:3}} primaryText="Web"/>
                            </SelectField>
                        </div>

                        <br/>
                        <br/>
                        <div className="nextButton">
                            <RaisedButton
                                label="Next >"
                                primary={true}
                                onClick={this._handleClick.bind(this)}
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
