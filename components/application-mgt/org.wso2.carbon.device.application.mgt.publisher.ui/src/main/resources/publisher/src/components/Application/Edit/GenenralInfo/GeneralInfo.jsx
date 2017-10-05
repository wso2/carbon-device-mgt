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
import {Button, FormGroup, Input, Label, Row} from 'reactstrap';
import Dropzone from 'react-dropzone';
import {FormattedMessage} from 'react-intl';
import Chip from "../../../UIComponents/Chip/Chip";

class GeneralInfo extends Component {

    constructor() {
        super();
        this.onTextFieldChange = this.onTextFieldChange.bind(this);
        this.addTags = this.addTags.bind(this);
        this.handleRequestDelete = this.handleRequestDelete.bind(this);
        this.handleTagChange = this.handleTagChange.bind(this);
        this.state = {
            defValue: "",
            title: "",
            description: "",
            shortDescription: "",
            tags: [],
            screenshots: [],
            icon: [],
            banner: []
        }
    }

    /**
     * Set text field values to state.
     * */
    onTextFieldChange(event) {
        let field = event.target.name;
        console.log(event.target.value);
        switch (field) {
            case "appName": {
                this.setState({name: event.target.value});
                break;
            }
            case "appDescription": {
                this.setState({description: event.target.value});
                break;
            }
            case "appShortDescription": {
                this.setState({shortDescription: event.target.value});
            }
        }
    };

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
            this.setState({tags, defValue: ""}, console.log(tags));
        }
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
     * Handles Chip delete function.
     * Removes the tag from state.tags
     * */
    handleRequestDelete(key) {
        let chipData = this.state.tags;
        const chipToDelete = chipData.map((chip) => chip.key).indexOf(key);
        chipData.splice(chipToDelete, 1);
        this.setState({tags: chipData});
    };


    //TODO: Remove Console logs.
    render() {
        return (
            <div className="app-edit-general-info">
                <Row>
                    <form>
                        <FormGroup>
                            <Label for="app-title">
                                <FormattedMessage id="Title" defaultMessage="Title"/>*
                            </Label>
                            <Input required type="text" name="appName" id="app-title"
                                   onChange={this.onTextFieldChange}/>
                        </FormGroup>
                        <FormGroup>
                            <Label for="app-short-description">
                                <FormattedMessage id="shortDescription" defaultMessage="shortDescription"/>*
                            </Label>
                            <Input
                                required
                                type="textarea"
                                name="appShortDescription"
                                id="app-short-description"
                                onChange={this.onTextFieldChange}
                            />
                        </FormGroup>
                        <FormGroup>
                            <Label for="app-title">
                                <FormattedMessage id="Description" defaultMessage="Description"/>*
                            </Label>
                            <Input
                                required
                                type="textarea"
                                name="appDescription"
                                id="app-description"
                                onChange={this.onTextFieldChange}/>
                        </FormGroup>
                        <FormGroup>
                            <Label for="app-category">
                                <FormattedMessage id="Category" defaultMessage="Category"/>
                            </Label>
                            <Input type="select" name="category" id="app-category">
                                <option>Business</option>
                            </Input>
                        </FormGroup>
                        <FormGroup>
                            <Label for="app-visibility">
                                <FormattedMessage id="Visibility" defaultMessage="Visibility"/>
                            </Label>
                            <Input type="select" name="visibility" id="app-visibility">
                                <option><FormattedMessage id="Devices" defaultMessage="Devices"/></option>
                                <option><FormattedMessage id="Roles" defaultMessage="Roles"/></option>
                                <option><FormattedMessage id="Groups" defaultMessage="Groups"/></option>
                            </Input>
                        </FormGroup>
                        <FormGroup>
                            <Label for="app-tags">
                                <FormattedMessage id="Tags" defaultMessage="Tags"/>*
                            </Label>
                            <Input
                                required
                                type="text"
                                value={this.state.defValue}
                                name="app-tags"
                                id="app-tags"
                                onChange={this.handleTagChange.bind(this)}
                                onKeyPress={this.addTags.bind(this)}
                            />
                            <div id="batch-content">
                                {this.state.tags.map(tag => {
                                        return (
                                            <Chip
                                                key={tag.key}
                                                content={tag}
                                                onDelete={this.handleRequestDelete}
                                            />
                                        )
                                    }
                                )}
                            </div>
                        </FormGroup>
                        <div>
                            <FormGroup>
                                <Label for="app-screenshots">
                                    <FormattedMessage id="Screenshots" defaultMessage="Screenshots"/>*
                                </Label>
                                <span className="image-sub-title"> (600 X 800 32 bit PNG)</span>
                                <div id="screenshot-container">
                                    {this.state.screenshots.map((tile) => (
                                        <button id="img-btn-screenshot"
                                                style={{height: '210px', width: '410px'}}
                                                onMouseEnter={() => {
                                                    console.log("Mouse Entered")
                                                }}>
                                            {console.log(tile[0].preview)}
                                            <img style={{height: '200px', width: '400px'}}
                                                 src={tile[0].preview}/>
                                        </button>
                                    ))}
                                    {this.state.screenshots.length < 3 ?
                                        <Dropzone
                                            className="application-create-screenshot-dropzone"
                                            accept="image/jpeg, image/png"
                                            onDrop={(screenshots, rejected) => {
                                                let tmpScreenshots = this.state.screenshots;
                                                tmpScreenshots.push(screenshots);
                                                console.log(screenshots);
                                                this.setState({
                                                    screenshots: tmpScreenshots
                                                });
                                            }}
                                        >
                                            <i className="fw fw-add"></i>
                                        </Dropzone> : <div/>}
                                </div>
                            </FormGroup>
                        </div>
                        <div style={{display: 'flex'}}>
                            <div style={{float: 'left', marginRight: '15px'}}>
                                <FormGroup>
                                    <Label for="app-icon">
                                        <FormattedMessage id="Icon" defaultMessage="Icon"/>*
                                    </Label>
                                    <span className="image-sub-title"> (512 X 512 32 bit PNG)</span>
                                    <div id="app-icon-container">
                                        {this.state.icon.map((tile) => (
                                            <button onMouseEnter={() => {
                                                console.log("Mouse Entered")
                                            }}>
                                                <img style={{height: '200px', width: '200px'}}
                                                     src={tile.preview}/>
                                            </button>
                                        ))}
                                        {this.state.icon.length === 0 ?
                                            <Dropzone
                                                className="application-create-icon-dropzone"
                                                accept="image/jpeg, image/png"
                                                onDrop={(icon, rejected) => {
                                                    this.setState({icon, rejected});
                                                }}
                                            >
                                                <i className="fw fw-add"></i>
                                            </Dropzone> : <div/>}
                                    </div>
                                </FormGroup>
                            </div>
                            <div style={{marginLeft: '15px'}}>
                                <FormGroup>
                                    <Label for="app-banner">
                                        <FormattedMessage id="Banner" defaultMessage="Banner"/>*
                                    </Label>
                                    <span className="image-sub-title"> (1000 X 400 32 bit PNG)</span>
                                    <div id="app-banner-container">
                                        {this.state.banner.map((tile) => (
                                            <button onMouseEnter={() => {
                                                console.log("Mouse Entered")
                                            }}>
                                                <img style={{height: '200px', width: '400px'}}
                                                     src={tile.preview}/>
                                            </button>
                                        ))}
                                        {this.state.banner.length === 0 ?
                                            <Dropzone
                                                className="application-create-banner-dropzone"
                                                accept="image/jpeg, image/png"
                                                onDrop={(banner, rejected) => {
                                                    this.setState({banner, rejected});
                                                }}
                                            >
                                                <i className="fw fw-add"></i>
                                            </Dropzone> : <div/>
                                        }
                                    </div>
                                </FormGroup>
                            </div>
                        </div>
                        <div className="save-info">
                            <Button className="custom-flat danger-flat">Cancel</Button>
                            <Button className="custom-raised primary">Save</Button>
                        </div>
                    </form>
                </Row>
            </div>
        )
    }
}

export default GeneralInfo;

