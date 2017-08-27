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
import Toggle from 'material-ui/Toggle';
import MenuItem from 'material-ui/MenuItem';
import TextField from 'material-ui/TextField';
import FlatButton from 'material-ui/FlatButton';
import IconButton from 'material-ui/IconButton';
import SelectField from 'material-ui/SelectField';
import RaisedButton from 'material-ui/RaisedButton';
import Close from 'material-ui/svg-icons/navigation/close';
import {Card, CardActions, CardTitle} from 'material-ui/Card';
import AddCircleOutline from 'material-ui/svg-icons/content/add-circle-outline';

/**
 * Platform Create component.
 * Contains following components:
 *      * Platform Name
 *      * Platform Description
 *      * Platform Icon
 *      * Whether the platform needs an app to be installed.
 *      * Whether the platform is enabled by default.
 *      * Whether the platform is shared with tenants.
 * */
class PlatformCreate extends Component {

    constructor() {
        super();
        this.state = {
            enabled: true,
            allTenants: false,
            files: [],
            platformProperties: [{key:"Enabled", value: "Boolean"}, {key:"Access Token", value:"String"}]
        }
    }

    /**
     * Handles toggle button actions.
     * One method is used for all the toggle buttons and, each toggle is identified by the id.
     * */
    _handleToggle(event) {
        switch (event.target.id) {
            case "enabled" : {
                let enabled = this.state.enabled;
                this.setState({enabled: !enabled});
                break;
            }
            case "tenant" : {
                let allTenants = this.state.allTenants;
                this.setState({allTenants: !allTenants});
                break;
            }
        }
    }

    /**
     * Remove the selected property from the property list.
     * */
    _removeProperty(property) {
        let properties = this.state.platformProperties;
        properties.splice(properties.indexOf(property), 1);
        this.setState({platformProperties: properties});
    }

    /**
     * Add a new platform property.
     * TODO: Create a property object and send to the endpoint.
     * */
    _addProperty() {

    }

    render() {
        return (
            <div className="middle" style={{width: '95%', height: '100%', marginTop: '1%'}}>
                <Card>
                    <CardTitle title="Create Platform"/>

                    {/**
                     * The stepper goes here.
                     */}
                    <CardActions>
                        <div style={{width: '100%', margin: 'auto', paddingLeft: '10px'}}>
                            <form>
                                <TextField
                                    hintText="Enter the Platform Name."
                                    floatingLabelText="Name*"
                                    floatingLabelFixed={true}
                                /><br/>
                                <TextField
                                    hintText="Enter the Platform Description."
                                    floatingLabelText="Description*"
                                    floatingLabelFixed={true}
                                    multiLine={true}
                                    rows={2}
                                /><br/><br/>
                                <Toggle
                                    id="tenant"
                                    label="Shared with all Tenants"
                                    labelPosition="right"
                                    onToggle={this._handleToggle.bind(this)}
                                    defaultToggled={this.state.allTenants}
                                /> <br/>
                                <Toggle
                                    id="enabled"
                                    label="Enabled"
                                    labelPosition="right"
                                    onToggle={this.handleToggle.bind(this)}
                                    defaultToggled={this.state.enabled}
                                /> <br/>
                                <div>
                                    <p style={{color: '#BDBDBD'}}>Platform Properties</p>
                                    <div id="property-container">
                                            {this.state.platformProperties.map((p) => {
                                                return <div key={p.key}>{p.key} : {p.value}
                                                    <IconButton onClick={this._removeProperty.bind(this, p)}>
                                                        <Close style={{height:'10px', width:'10px'}}/>
                                                    </IconButton>
                                                </div>})}
                                    </div>
                                    <TextField
                                        hintText=""
                                        floatingLabelText="Platform Property*"
                                        floatingLabelFixed={true}
                                    /> <em/>
                                    <SelectField
                                        floatingLabelText="Property Type"
                                        value={this.state.store}
                                        floatingLabelFixed={true}>
                                        <MenuItem value={1} primaryText="String"/>
                                        <MenuItem value={2} primaryText="Number"/>
                                        <MenuItem value={3} primaryText="Boolean"/>
                                        <MenuItem value={4} primaryText="File"/>
                                    </SelectField>
                                    <IconButton onClick={this._addProperty.bind(this)}>
                                        <AddCircleOutline/>
                                    </IconButton>
                                    <br/>
                                </div>
                                <div>
                                    <p style={{color: '#BDBDBD'}}>Platform Icon*:</p>
                                    <Dropzone style={{width: '100px', height: '100px', border: 'dashed #BDBDBD 1px'}}>
                                        <p style={{margin: '40px 40px 40px 50px', color: '#BDBDBD'}}>+</p>
                                    </Dropzone>
                                </div>
                                <br/>
                                <RaisedButton primary={true} label="Create"/>
                                <FlatButton label="Cancel"/>
                            </form>
                        </div>
                    </CardActions>
                </Card>
            </div>
        );
    }
}

PlatformCreate.prototypes = {
    enabled: PropTypes.bool,
    allTenants: PropTypes.bool,
    files: PropTypes.array,
    platformProperties: PropTypes.object,
    handleToggle: PropTypes.func
};

export default PlatformCreate;
