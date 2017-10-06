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
import {Button, Col, Row} from "reactstrap";
import Platform from "./Platform";
import PlatformMgtApi from "../../api/platformMgtApi";
import AuthHandler from "../../api/authHandler";
import PlatformCreate from "./PlatformCreate";

/**
 * Platform view component.
 * */
class PlatformListing extends Component {

    constructor() {
        super();
        this.onPlatformCreateClick = this.onPlatformCreateClick.bind(this);
        this.state = {
            platforms: [],
            openModal: false
        }
    }

    componentWillMount() {
        PlatformMgtApi.getPlatforms().then(response => {
            console.log(response);
            this.setState({platforms: response.data});
        }).catch(err => {
            AuthHandler.unauthorizedErrorHandler(err);
        })
    }

    onPlatformCreateClick() {
        this.setState({openModal: true});
    }

    render() {
        return (
            <div id="platform-listing">
                <Row>
                    <div className="create-platform">
                        <Button className="custom-flat grey" onClick={this.onPlatformCreateClick}>
                            <i className="fw fw-add"></i>Create Platform
                        </Button>
                    </div>
                </Row>
                <Row>
                    <div id="platform-list">
                        {this.state.platforms.map(platform => {
                            return (
                                <Platform key={platform.identifier} platform={platform}/>
                            )
                        })}
                    </div>
                </Row>
                <PlatformCreate open={this.state.openModal}/>
            </div>
        );
    }
}

export default PlatformListing;
