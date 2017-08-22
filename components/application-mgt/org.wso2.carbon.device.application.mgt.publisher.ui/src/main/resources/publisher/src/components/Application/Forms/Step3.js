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
class Step3 extends Component {
    constructor() {
        super();
        this.state = {
            showForm: false,
            releaseChannel: 1,
            errors: {}
        }
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
        const contentStyle = {margin: '0 16px'};
        return (
            <div style={contentStyle}>
                <div>
                    <Toggle
                        label="Release the Application"
                        labelPosition="right"
                        onToggle={this.handleToggle.bind(this)}
                        defaultToggled={this.state.showForm}
                    />
                    {/*If toggle is true, the release form will be shown.*/}
                    {!this.state.showForm ? <div/> : <div>
                        <SelectField
                            floatingLabelText="Select Release Channel*"
                            value={this.state.releaseChannel}
                            floatingLabelFixed={true}
                        >
                            <MenuItem value={1} primaryText="Alpha"/>
                            <MenuItem value={2} primaryText="Beta"/>
                            <MenuItem value={3} primaryText="GA"/>
                        </SelectField> <br/>
                        <TextField
                            hintText="1.0.0"
                            floatingLabelText="Version*"
                            errorText={this.state.errors["title"]}
                            floatingLabelFixed={true}
                        /><br/>
                    </div>}

                    <div style={{marginTop: 12}}>
                        <FlatButton
                            label="< Back"
                            disabled={false}
                            onClick={this.handlePrev.bind(this)}
                            style={{marginRight: 12}}
                        />
                        <RaisedButton
                            label="Finish"
                            primary={true}
                            onClick={this.handleFinish.bind(this)}
                        />
                    </div>
                </div>
            </div>
        );
    }
}

Step3.propTypes = {
    handleFinish: PropTypes.func,
    handlePrev: PropTypes.func,
    setData: PropTypes.func,
    removeData: PropTypes.func
};

export default Step3;
