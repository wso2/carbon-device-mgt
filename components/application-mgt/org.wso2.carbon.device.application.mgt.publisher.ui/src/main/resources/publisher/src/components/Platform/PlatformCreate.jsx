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
import Clear from 'material-ui/svg-icons/content/clear';
import {GridList, GridTile} from 'material-ui/GridList';
import Close from 'material-ui/svg-icons/navigation/close';
import {Card, CardActions, CardTitle} from 'material-ui/Card';
import AddCircleOutline from 'material-ui/svg-icons/content/add-circle-outline';
<<<<<<< HEAD
import Theme from '../../theme';
=======
import Theme from '../../themes/theme';
>>>>>>> parent of 8f3d11f... refactoring theming support

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
            platformProperties: [],
            selectedProperty: 0,
            name: "",
            description: "",
            property: "",
            icon: [],
            propertyTypes: [
                {key: 0, value: 'String'},
                {key: 1, value: 'Number'},
                {key: 2, value: 'Boolean'},
                {key: 3, value: 'File'}]
        }
    }

    componentWillMount() {
<<<<<<< HEAD
        /**
         *Loading the theme files based on the the user-preference.
         */
        const selected =
            (Theme.currentThemeType === Theme.defaultThemeType) ? Theme.defaultThemeType : Theme.currentTheme;
        const platformCreateCss = "platform-create.css";
        const platformCreateId = "application-listing";
        let themePath  =  "/" + Theme.themeFolder + "/" + selected + "/" + platformCreateCss;
        let promisedConfig = Theme.loadThemeFiles(themePath);
        let styleSheet = document.getElementById(platformCreateId);
        let head = document.getElementsByTagName("head")[0];
        let link = document.createElement("link");
        link.type = Theme.styleSheetType;
        link.href = Theme.baseURL + "/" + Theme.appContext + themePath;
        link.id = platformCreateId;
        link.rel = Theme.styleSheetRel;

        if (styleSheet !== null) {
            styleSheet.disabled = true;
            styleSheet.parentNode.removeChild(styleSheet);
        }

        promisedConfig.then(function(){
            head.appendChild(link);
        }).catch(function () {
            // If there is no customized css file, load the default one.
            themePath = "/" + Theme.themeFolder + "/" + Theme.defaultThemeType + "/" + platformCreateCss;
            link.href = Theme.baseURL + "/" + Theme.appContext + themePath;
            head.appendChild(link);
        });
    }

    componentWillUnmount() {
        let styleSheet = document.getElementById("platform-create");
        if (styleSheet !== null) {
            styleSheet.disabled = true;
            styleSheet.parentNode.removeChild(styleSheet);
=======
        let selected = Theme.selectedTheme;
        if (Theme.currentTheme === "default") {
            require("../../themes/default/platform-create.css");
        } else {
            try {
                require("../../themes/" + selected + "/platform-create.css");
            } catch (ex) {
                // If the particular customized file does not exist, use the default one.
                require("../../themes/default/platform-create.css");
            }
>>>>>>> parent of 8f3d11f... refactoring theming support
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
     * Triggers the onChange action on property type selection.
     * */
    _onPropertySelect = (event, index, value) => {
        console.log(this.state.propertyTypes[value]);
        this.setState({selectedProperty: value});
    };

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
     * */
    _addProperty() {
        let property = this.state.property;
        let selected = this.state.selectedProperty;

        this.setState({platformProperties:
            this.state.platformProperties.concat([
                {
                    key: property,
                    value: this.state.propertyTypes[selected].value
                }]),
            property: "",
            selectedProperty: 0});
    }

    /**
     * Triggers in onChange event of text fields.
     * Text fields are identified by their ids and the value will be persisted in the component state.
     * */
    _onTextChange = (event, value) => {
        let property = this.state.property;
        let name = this.state.name;
        let description = this.state.description;

        switch (event.target.id) {
            case "name": {
                name = value;
                this.setState({name: name});
                break;
            }

            case "description": {
                description = value;
                this.setState({description: description});
                break;
            }

            case "property": {
                property = value;
                this.setState({property: property});
                break;
            }
        }
    };

    _onCreatePlatform() {

    }

    /**
     * Remove the uploaded icon.
     * */
    _removeIcon(event) {
        this.setState({icon: []});
    }

    /**
     * Clears the user entered values in the form.
     * */
    _clearForm() {
        this.setState({enabled: true,
            allTenants: false,
            files: [],
            platformProperties: [],
            selectedProperty: 0,
            name: "",
            description: "",
            property: "",})
    }

    render() {
        const {
            platformProperties,
            allTenants,
            enabled,
            selectedProperty,
            propertyTypes,
            name,
            description,
            property} = this.state;

        return (
            <div className="middle createplatformmiddle">
                <Card>
                    <CardTitle title="Create Platform"/>

                    <CardActions>
                        <div className="createplatformcardaction">
                            <form>
                                <TextField
                                    hintText="Enter the Platform Name."
                                    id="name"
                                    floatingLabelText="Name*"
                                    floatingLabelFixed={true}
                                    value={name}
                                    onChange={this._onTextChange.bind(this)}
                                /><br/>
                                <TextField
                                    id="description"
                                    hintText="Enter the Platform Description."
                                    floatingLabelText="Description*"
                                    floatingLabelFixed={true}
                                    multiLine={true}
                                    rows={2}
                                    value={description}
                                    onChange={this._onTextChange.bind(this)}
                                /><br/><br/>
                                <Toggle
                                    id="tenant"
                                    label="Shared with all Tenants"
                                    labelPosition="right"
                                    onToggle={this._handleToggle.bind(this)}
                                    toggled={allTenants}
                                /> <br/>
                                <Toggle
                                    id="enabled"
                                    label="Enabled"
                                    labelPosition="right"
                                    onToggle={this._handleToggle.bind(this)}
                                    toggled={enabled}
                                /> <br/>
                                <div>
                                    <p className="createplatformproperties">Platform Properties</p>
                                    <div id="property-container">
                                        {platformProperties.map((p) => {
                                            return <div key={p.key}>{p.key} : {p.value}
                                                <IconButton onClick={this._removeProperty.bind(this, p)}>
                                                    <Close className="createplatformpropertyclose"/>
                                                </IconButton>
                                            </div>
                                        })}
                                    </div>
                                    <div className="createplatformproperty">
                                        <TextField
                                            id="property"
                                            hintText="Property Name"
                                            floatingLabelText="Platform Property*"
                                            floatingLabelFixed={true}
                                            value={this.state.property}
                                            onChange={this._onTextChange.bind(this)}
                                        /> <em/>
                                        <SelectField
                                            className="createplatformpropertyselect"
                                            floatingLabelText="Property Type"
                                            value={selectedProperty}
                                            floatingLabelFixed={true}
                                            onChange={this._onPropertySelect.bind(this)}>
                                            {propertyTypes.map((type) => {
                                                return  <MenuItem key={type.key}
                                                                  value={type.key}
                                                                  primaryText={type.value}/>
                                            })}
                                        </SelectField>
                                        <IconButton onClick={this._addProperty.bind(this)}>
                                            <AddCircleOutline/>
                                        </IconButton>
                                        <br/>
                                    </div>
                                </div>
                                <div>
                                    <p className="createplatformiconp">Platform Icon*:</p>
                                    <GridList className="createplatformicon" cols={1.1}>
                                        {this.state.icon.map((tile) => (
                                            <GridTile key={Math.floor(Math.random() * 1000)}
                                                      title={tile.name}
                                                      actionIcon={
                                                          <IconButton onClick={this._removeIcon.bind(this)}>
                                                              <Clear />
                                                          </IconButton>}>
                                                <img src={tile.preview}/>
                                            </GridTile>
                                        ))}
                                        {this.state.icon.length === 0 ?
                                            <Dropzone className="createplatformdropzone"
                                                      accept="image/jpeg, image/png"
                                                      onDrop={(icon, rejected) => {this.setState({icon, rejected})}}>
                                                <p className="createplatformdropzonep">+</p>
                                            </Dropzone> : <div />}
                                    </GridList>
                                </div>
                                <br/>
                                <RaisedButton primary={true} label="Create"
                                              onClick={this._onCreatePlatform.bind(this)}/>
                                <FlatButton label="Cancel" onClick={this._clearForm.bind(this)}/>
                            </form>
                        </div>
                    </CardActions>
                </Card>
            </div>
        );
    }
}

PlatformCreate.prototypes = {
};

export default PlatformCreate;
