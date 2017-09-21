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
import '../../css/font-wso2.css';
import NotificationsIcon from 'material-ui/svg-icons/social/notifications';
import ActionAccountCircle from 'material-ui/svg-icons/action/account-circle';
import ApplicationCreate from '../Application/Create/ApplicationCreate';
import {Button, Input, InputGroup,} from 'reactstrap';


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

    render() {
        return (
            <div id="container">
                <div id="header-content">
                    <div id="header">
                        <span id="header-text">
                            WSO2 IoT App Publisher
                        </span>
                        <div id="header-btn">
                            <Button id="btn"><NotificationsIcon/></Button>
                            <Button id="btn"><ActionAccountCircle/></Button>
                        </div>
                    </div>
                    <div id="search-box">
                        <InputGroup>
                            <Input
                                id="search"
                                name="search"
                                placeholder={'Search for Applications'}
                                onChange={(event) => console.log(event.target.value)}
                            />
                        </InputGroup>
                    </div>
                    <div id="add-btn-container">
                        <Button
                            id="add-btn"
                            onClick={this.handleApplicationCreateClick.bind(this)}
                        >
                            <h3>
                                <strong>+</strong>
                            </h3>
                        </Button>
                    </div>
                </div>

                <div id="application-content" style={this.state.style}>
                    {this.props.children}
                </div>
                <ApplicationCreate open={this.state.openModal}/>
            </div>
        );
    }
}

BaseLayout.propTypes = {
    children: PropTypes.element
};

export default withRouter(BaseLayout);
