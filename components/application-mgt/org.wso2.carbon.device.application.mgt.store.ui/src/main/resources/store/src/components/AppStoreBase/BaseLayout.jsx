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
import {withRouter} from 'react-router-dom';
import AuthHandler from "../../api/authHandler";
import {Col, Container, Input, Row,} from 'reactstrap';
import FloatingButton from "../UIComponents/FloatingButton/FloatingButton";

/**
 * Base Layout:
 * App bar
 * Left Navigation
 * Middle content.
 * */
class BaseLayout extends Component {

    constructor() {
        super();
        this.state = {
            notifications: 0,
            user: 'Admin',
            openModal: false
        };
        this.logout = this.logout.bind(this);
        this.closeModal = this.closeModal.bind(this);
    }

    handleApplicationClick() {
        this.handleHistory('/assets/apps');
    }

    handleApplicationCreateClick(event) {
        event.preventDefault();
        event.stopPropagation();
        this.setState({openModal: true});
    }

    /**
     * The method to update the history.
     * to: The URL to route.
     * */
    handleHistory(to) {
        this.props.history.push(to);
    }

    logout(event, index, value) {
        AuthHandler.logout();
    }

    closeModal() {
        this.setState({openModal: false});
    }

    render() {
        return (
            <Container noGutters fluid id="container">
                <div id="header-content">
                    <div id="header">
                        <span id="header-text">
                            WSO2 IoT App Store
                        </span>
                        <div id="header-btn-container">
                            <i className="fw fw-notification btn-header"></i>
                            <i className="fw fw-user btn-header"></i>
                        </div>
                        <div id="search-box">
                            <i className="fw fw-search search-icon">
                            </i>
                            <Input
                                id="search"
                                name="search"
                                placeholder={'Search for Applications'}
                                onChange={(event) => console.log(event.target.value)} //TODO: Remove this
                            />
                        </div>
                    </div>
                    <div id="add-btn-container">
                        <FloatingButton
                            className="add-btn small"
                            onClick={this.handleApplicationCreateClick.bind(this)}
                        />
                    </div>
                </div>
                <div id="application-content" style={this.state.style}>
                    <Row>
                        <Col>
                            {this.props.children}
                        </Col>
                    </Row>
                </div>
            </Container>
        );
    }
}

BaseLayout.propTypes = {
    children: PropTypes.element
};

export default withRouter(BaseLayout);
