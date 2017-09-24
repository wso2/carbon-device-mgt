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
import './drawer.css';
import {Row} from "reactstrap";

/**
 * Custom React component for Application View.
 * */
class Drawer extends Component {

    constructor() {
        super();
        this.closeDrawer = this.closeDrawer.bind(this);
    }

    /**
     * Closes the drawer.
     * */
    closeDrawer() {
        this.props.onClose();
    }

    render() {
        return (
            <div>
                <div id="app-view" className="app-view-drawer" style={this.props.style}>
                    <a onClick={this.closeDrawer} className="drawer-close-btn">&times;</a>
                    {this.props.children}
                </div>
            </div>
        );
    }
}

Drawer.propTypes = {
    style: PropTypes.object,
    children: PropTypes.node,
    onClose: PropTypes.func
};

export default Drawer;
