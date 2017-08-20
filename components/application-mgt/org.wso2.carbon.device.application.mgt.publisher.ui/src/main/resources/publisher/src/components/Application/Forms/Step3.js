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

import React, {Component} from 'react';
import Toggle from 'material-ui/Toggle';
import MenuItem from 'material-ui/MenuItem';
import TextField from 'material-ui/TextField';
import FlatButton from 'material-ui/FlatButton';
import SelectField from 'material-ui/SelectField';
import RaisedButton from 'material-ui/RaisedButton';

class Step3 extends Component {
    constructor() {
        super();
        this.state = {
            showForm: false,
            releaseChannel: 1
        }
    }

    handleFinish() {
        this.props.handleFinish();
    }

    handlePrev() {
        this.props.handlePrev();
    }

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
                    {!this.state.showForm ? <div></div> : <div>
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

export default Step3;