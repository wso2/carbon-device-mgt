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
import Toggle from 'material-ui/Toggle';
import MenuItem from 'material-ui/MenuItem';
import TextField from 'material-ui/TextField';
import FlatButton from 'material-ui/FlatButton';
import SelectField from 'material-ui/SelectField';
import RaisedButton from 'material-ui/RaisedButton';
import Theme from '../../../theme';
import {Button, Form, FormGroup, Label, Input, FormText, Badge, Collapse} from 'reactstrap';
import Switch from '../../UIComponents/Switch/Switch'

/**
 * The Third step of application create wizard. {Application Release Step}
 * This step is not compulsory.
 *
 * When click finish, user will prompt to confirm the application creation.
 * User can go ahead and create the app or cancel.
 *
 * This contains following components:
 *      * Toggle to select application release. Un-hides the Application Release form.
 *
 *     Application Release Form.
 *      * Release Channel
 *      * Application Version
 *      * Upload component for application.
 *
 * Parent Component: Create
 * Props:
 *      * handleFinish : {type: function, Invokes handleNext function in Parent.}
 *      * handlePrev : {type: function, Invokes handlePrev function in Parent}
 *      * setData : {type: function, Invokes setStepData function in Parent}
 *      * removeData : {type: Invokes removeStepData function in Parent}
 * */
class Step4 extends Component {
    constructor() {
        super();
        this.handleToggle = this.handleToggle.bind(this);
        this.handlePrev = this.handlePrev.bind(this);
        this.handleToggle = this.handleToggle.bind(this);
        this.handleFinish = this.handleFinish.bind(this);
        this.state = {
            showForm: false,
            releaseChannel: 1,
            errors: {}
        };
        this.scriptId = "application-create-step3";
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
     * Handles finish button click.
     * This invokes handleNext function in parent component.
     * */
    handleFinish() {
        this.props.handleFinish();
    }

    /**
     * Invokes Prev button click.
     * */
    handlePrev() {
        this.props.handlePrev();
    }

    /**
     * Handles release application selection.
     * */
    handleToggle() {
        let hide = this.state.showForm;
        this.setState({showForm: !hide});
    }

    render() {
        return (
            <div className="applicationCreateStepMiddle">
                <div>
                    <FormGroup>
                        <div id="app-release-switch-content">
                            <div id="app-release-switch-label">
                                <Label for="app-release-switch"><strong>Add Release to Application</strong></Label>
                            </div>
                            <div id="app-release-switch-switch">
                                <Switch id="app-release-switch" onChange={this.handleToggle.bind(this)}/>
                            </div>
                        </div>
                    </FormGroup>
                    <br/>
                    <div>
                            <span style={{color: '#BBBBBB', width: '100%', justify: 'left'}}>
                                <i>Info: </i>
                                Enabling this will create a release for the current Application.
                                To upload the Application, please visit to the Release management section of
                                Application Edit View.
                            </span>
                    </div>
                    {/*If toggle is true, the release form will be shown.*/}
                    <Collapse isOpen={this.state.showForm}>
                        <FormGroup>
                            <Label for="release-channel">Release Channel</Label>
                            <Input id="input-custom" type="select" mid="release-channel" style={{
                                width: '200px',
                                border: 'none',
                                borderRadius: '0',
                                borderBottom: 'solid 1px #BDBDBD'
                            }}>
                                <option>GA</option>
                                <option>Alpha</option>
                                <option>Beta</option>
                            </Input>
                        </FormGroup>
                        <FormGroup>
                            <Label for="version">Version*</Label>
                            <Input type="text" id="version input-custom" placeholder="v1.0" required/>
                        </FormGroup>
                    </Collapse>
                </div>
            </div>
        );
    }
}

Step4.propTypes = {
    handleFinish: PropTypes.func,
    handlePrev: PropTypes.func,
    setData: PropTypes.func,
    removeData: PropTypes.func
};

export default Step4;
