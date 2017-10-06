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
import {Button, Col, Collapse, Row} from "reactstrap";
import {FormattedMessage} from "react-intl";

/**
 * Platform component.
 * In Platform listing, this component will be displayed as cards.
 * */
class Platform extends Component {

    constructor() {
        super();
        this.unFold = this.unFold.bind(this);
        this.state = {
            isOpen: false
        }
    }

    unFold() {
        let isOpen = this.state.isOpen;
        this.setState({isOpen: !isOpen})
    }

    render() {
        const {platform} = this.props;
        return (
            <div className="platform-content">
                <Row>
                    <Col>
                        <div className="platform-text-container">
                            <p className="app-view-field">{platform.name}</p>
                            <p className="app-view-text">{platform.enabled ? "Active" : "Disabled"}</p>
                        </div>
                    </Col>
                    <Col>
                        <div className="platform-icon-container">
                            <p className="platform-icon-letter">{platform.name.charAt(0)}</p>
                        </div>
                    </Col>
                </Row>
                <Row>
                    <div className="platform-content-footer">
                        <Button className="custom-flat grey">{platform.enabled ?
                            <FormattedMessage id="Disable" defaultMessage="Disable"/> :
                            <FormattedMessage id="Activate" defaultMessage="Activate"/>}
                        </Button>
                        <Button className="custom-flat grey">
                            <FormattedMessage id="Share.With.Tenants" defaultMessage="Share.With.Tenants"/>
                        </Button>
                        <Button className="custom-flat grey circle-button" onClick={this.unFold}>
                            {this.state.isOpen ? <i className="fw fw-up"></i> : <i className="fw fw-down"></i>}
                        </Button>
                    </div>
                </Row>
                <div className="platform-content-more-outer">
                    <Row>
                        <Col>
                            <Collapse isOpen={this.state.isOpen}>
                                <div className="platform-content-more">
                                    <Row>
                                        <Col>
                                            <p className="app-view-field">
                                                <FormattedMessage id="Description" defaultMessage="Description"/>
                                            </p>
                                        </Col>
                                        <Col><p className="app-view-text">{platform.description}</p></Col>
                                    </Row>
                                    <Row>
                                        <Col>
                                            <p className="app-view-field">
                                                <FormattedMessage id="File.Based" defaultMessage="File.Based"/>
                                            </p>
                                        </Col>
                                        <Col>
                                            <p className="app-view-text">{platform.fileBased ?
                                                <FormattedMessage id="Yes" defaultMessage="Yes"/>
                                                : <FormattedMessage id="No" defaultMessage="No"/>}</p>
                                        </Col>
                                    </Row>
                                    <Row>
                                        <Col><p className="app-view-field">
                                            <FormattedMessage id="Tags" defaultMessage="Tags"/>
                                        </p></Col>
                                        <Col>
                                            <p className="app-view-text">
                                                {platform.tags.length > 0 ?
                                                    platform.tags :
                                                    <FormattedMessage
                                                        id="No.Platform.Tags"
                                                        defaultMessage="No.Platform.Tags"/>
                                                }
                                            </p>
                                        </Col>
                                    </Row>
                                </div>
                            </Collapse>
                        </Col>
                    </Row>
                </div>
            </div>
        );
    }
}

export default Platform;
