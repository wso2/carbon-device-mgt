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
import {Badge, Button, FormGroup, Input, Label, Row} from 'reactstrap';
import Dropzone from 'react-dropzone';
import {FormattedMessage} from 'react-intl';

class GeneralInfo extends Component {

    constructor() {
        super();
        this.state = {
            defValue: "",
            tags: [],
            screenshots: [],
            icon: [],
            banner: []
        }
    }

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
                            <Input
                                required
                                type="text"
                                name="appName"
                                id="app-title"
                            />
                        </FormGroup>
                        <FormGroup>
                            <Label for="app-title">
                                <FormattedMessage id="Description" defaultMessage="Description"/>*
                            </Label>
                            <Input required type="textarea" multiline name="appName" id="app-title"/>
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
                            <Input required type="text" value={this.state.defValue} name="app-tags" id="app-tags"/>
                            <div id="batch-content">
                                {this.state.tags.map(tag => {
                                        return (
                                            <Badge
                                                style={{margin: '0 2px 0 2px'}}
                                                value={tag.value}
                                            >
                                                {tag.value}
                                            </Badge>
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
                                        <button id="img-btn-screenshot" style={{height: '210px', width: '410px'}}
                                                onMouseEnter={() => {
                                                    console.log("Mouse Entered")
                                                }}>
                                            {console.log(tile[0].preview)}
                                            <img style={{height: '200px', width: '400px'}} src={tile[0].preview}/>
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
                                                <img style={{height: '200px', width: '200px'}} src={tile.preview}/>
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
                                                <img style={{height: '200px', width: '400px'}} src={tile.preview}/>
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
                            <Button>
                                <FormattedMessage id="Save" defaultMessage="Save"/>
                            </Button>
                        </div>
                    </form>
                </Row>
            </div>
        )
    }
}

export default GeneralInfo;

