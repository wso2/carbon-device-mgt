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
import Chip from 'material-ui/Chip';
import Dropzone from 'react-dropzone';
import React, {Component} from 'react';
import Toggle from 'material-ui/Toggle';
import MenuItem from 'material-ui/MenuItem';
import TextField from 'material-ui/TextField';
import FlatButton from 'material-ui/FlatButton';
import IconButton from 'material-ui/IconButton';
import SelectField from 'material-ui/SelectField';
import RaisedButton from 'material-ui/RaisedButton';
import PlatformMgtApi from '../../api/platformMgtApi';
import Clear from 'material-ui/svg-icons/content/clear';
import {GridList, GridTile} from 'material-ui/GridList';
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
        this.onCreatePlatform = this.onCreatePlatform.bind(this);
        this.handleToggle = this.handleToggle.bind(this);
        this.addProperty = this.addProperty.bind(this);
        this.addTags = this.addTags.bind(this);
        this.clearForm = this.clearForm.bind(this);
        this.onPropertySelect = this.onPropertySelect.bind(this);
        this.handleTagChange = this.handleTagChange.bind(this);
        this.removeIcon = this.removeIcon.bind(this);
        this.onTextChange = this.onTextChange.bind(this);
        this.renderChip = this.renderChip.bind(this);
        this.removeProperty = this.removeProperty.bind(this);
        this.state = {
            tags: [],
            defValue: "",
            enabled: true,
            allTenants: false,
            files: [],
            platformProperties: [],
            selectedProperty: 0,
            name: "",
            description: "",
            property: "",
            icon: [],
            identifier: "",
            propertyTypes: [
                {key: 0, value: 'String'},
                {key: 1, value: 'Number'},
                {key: 2, value: 'Boolean'},
                {key: 3, value: 'File'}]
        };
    }

    /**
     * Handles toggle button actions.
     * One method is used for all the toggle buttons and, each toggle is identified by the id.
     * */
    handleToggle(event) {
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
    onPropertySelect(event, index, value) {
        console.log(this.state.propertyTypes[value]);
        this.setState({selectedProperty: value});
    }

    /**
     * Handles Chip delete function.
     * Removes the tag from state.tags
     * */
    handleTagDelete(key) {
        this.chipData = this.state.tags;
        const chipToDelete = this.chipData.map((chip) => chip.key).indexOf(key);
        this.chipData.splice(chipToDelete, 1);
        this.setState({tags: this.chipData});
    }

    /**
     * Create a tag on Enter key press and set it to the state.
     * Clears the tags text field.
     * Chip gets two parameters: Key and value.
     * */
    addTags(event) {
        let tags = this.state.tags;
        if (event.charCode === 13) {
            event.preventDefault();
            tags.push({key: Math.floor(Math.random() * 1000), value: event.target.value});
            this.setState({tags, defValue: ""});
        }
    }

    /**
     * Creates Chip array from state.tags.
     * */
    renderChip(data) {
        return (
            <Chip
                key={data.key}
                onRequestDelete={() => this.handleTagDelete(data.key)}
                style={this.styles.chip}
            >
                {data.value}
            </Chip>
        );
    }

    /**
     * Set the value for tag.
     * */
    handleTagChange(event) {
        let defaultValue = this.state.defValue;
        defaultValue = event.target.value;
        this.setState({defValue: defaultValue})
    }

    /**
     * Remove the selected property from the property list.
     * */
    removeProperty(property) {
        let properties = this.state.platformProperties;
        properties.splice(properties.indexOf(property), 1);
        this.setState({platformProperties: properties});
    }

    /**
     * Add a new platform property.
     * */
    addProperty() {
        let property = this.state.property;
        let selected = this.state.selectedProperty;

        this.setState({
            platformProperties:
                this.state.platformProperties.concat([
                    {
                        key: property,
                        value: this.state.propertyTypes[selected].value
                    }]),
            property: "",
            selectedProperty: 0
        });
    }

    /**
     * Triggers in onChange event of text fields.
     * Text fields are identified by their ids and the value will be persisted in the component state.
     * */
    onTextChange(event, value) {
        let property = this.state.property;
        let name = this.state.name;
        let description = this.state.description;
        let identifier = this.state.identifier;

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
            case "identifier": {
                identifier = value;
                this.setState({identifier: identifier});
            }
        }
    };

    /**
     * Create platform object and call the create platform api.
     * */
    onCreatePlatform(event) {
        //Call the platform create api.
        event.preventDefault();
        let platform = {};
        platform.identifier = this.state.identifier;
        platform.name = this.state.name;
        platform.description = this.state.description;
        platform.tags = this.state.tags;
        platform.properties = this.state.platformProperties;
        platform.icon = this.state.icon;
        platform.enabled = this.state.enabled;
        platform.allTenants = this.state.allTenants;
        platform.defaultTenantMapping = true;

        PlatformMgtApi.createPlatform(platform);

    }

    /**
     * Remove the uploaded icon.
     * */
    removeIcon(event) {
        event.preventDefault();
        this.setState({icon: []});
    }

    /**
     * Clears the user entered values in the form.
     * */
    clearForm(event) {
        event.preventDefault();
        this.setState({
            enabled: true,
            allTenants: false,
            files: [],
            platformProperties: [],
            selectedProperty: 0,
            name: "",
            description: "",
            property: "",
        })
    }

    render() {
        const {
            platformProperties,
            allTenants,
            enabled,
            selectedProperty,
            propertyTypes,
            name,
            tags,
            defValue,
            description,
            identifier,
            property
        } = this.state;

        return (
            <div className="middle createplatformmiddle">
                <Card>
                    <CardTitle title="Create Platform"/>
                    <CardActions>
                        <div className="createplatformcardaction">
                            <form>
                                <TextField
                                    hintText="Unique Identifier for Platform."
                                    id="identifier"
                                    floatingLabelText="Identifier*"
                                    floatingLabelFixed={true}
                                    value={identifier}
                                    onChange={this.onTextChange}
                                />
                                <br/>
                                <TextField
                                    hintText="Enter the Platform Name."
                                    id="name"
                                    floatingLabelText="Name*"
                                    floatingLabelFixed={true}
                                    value={name}
                                    onChange={this.onTextChange}
                                />
                                <br/>
                                <TextField
                                    id="description"
                                    hintText="Enter the Platform Description."
                                    floatingLabelText="Description*"
                                    floatingLabelFixed={true}
                                    multiLine={true}
                                    rows={2}
                                    value={description}
                                    onChange={this.onTextChange}
                                />
                                <br/>
                                <br/>
                                <Toggle
                                    id="tenant"
                                    label="Shared with all Tenants"
                                    labelPosition="right"
                                    onToggle={this.handleToggle}
                                    toggled={allTenants}
                                />
                                <br/>
                                <Toggle
                                    id="enabled"
                                    label="Enabled"
                                    labelPosition="right"
                                    onToggle={this.handleToggle}
                                    toggled={enabled}
                                />
                                <br/>
                                <TextField
                                    id="tags"
                                    hintText="Enter Platform tags.."
                                    floatingLabelText="Tags*"
                                    floatingLabelFixed={true}
                                    value={defValue}
                                    onChange={this.handleTagChange}
                                    onKeyPress={this.addTags}
                                />
                                <br/>
                                <div className="createPlatformTagWrapper">
                                    {tags.map(this.renderChip, this)}
                                </div>
                                <br/>
                                <div>
                                    <p className="createplatformproperties">Platform Properties</p>
                                    <div id="property-container">
                                        {platformProperties.map((p) => {
                                            return <div key={p.key}>{p.key} : {p.value}
                                                <IconButton onClick={this.removeProperty.bind(this, p)}>
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
                                            onChange={this.onTextChange}
                                        /> <em/>
                                        <SelectField
                                            className="createplatformpropertyselect"
                                            floatingLabelText="Property Type"
                                            value={selectedProperty}
                                            floatingLabelFixed={true}
                                            onChange={this.onPropertySelect}>
                                            {propertyTypes.map((type) => {
                                                return <MenuItem key={type.key}
                                                                 value={type.key}
                                                                 primaryText={type.value}/>
                                            })}
                                        </SelectField>
                                        <IconButton onClick={this.addProperty}>
                                            <AddCircleOutline/>
                                        </IconButton>
                                        <br/>
                                    </div>
                                </div>
                                <div>
                                    <p className="createplatformiconp">Platform Icon*:</p>
                                    <GridList className="createplatformicon" cols={1.1}>
                                        {this.state.icon.map((tile) => (
                                            <GridTile
                                                key={Math.floor(Math.random() * 1000)}
                                                title={tile.name}
                                                actionIcon={
                                                    <IconButton onClick={this.removeIcon}>
                                                        <Clear/>
                                                    </IconButton>}>
                                                <img src={tile.preview}/>
                                            </GridTile>
                                        ))}
                                        {this.state.icon.length === 0 ?
                                            <Dropzone
                                                className="createplatformdropzone"
                                                accept="image/jpeg, image/png"
                                                onDrop={(icon, rejected) => {
                                                    this.setState({icon, rejected})
                                                }}
                                            >
                                                <p className="createplatformdropzonep">+</p>
                                            </Dropzone> : <div/>}
                                    </GridList>
                                </div>
                                <br/>
                                <RaisedButton
                                    primary={true} label="Create"
                                    onClick={this.onCreatePlatform}/>
                                <FlatButton label="Cancel" onClick={this.clearForm}/>
                            </form>
                        </div>
                    </CardActions>
                </Card>
            </div>
        );
    }
}

PlatformCreate.prototypes = {};

export default PlatformCreate;
