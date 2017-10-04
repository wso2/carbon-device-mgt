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
import './chip.css';

class Chip extends Component {

    constructor() {
        super();
        this.onDeleteClick = this.onDeleteClick.bind(this);
    }

    onDeleteClick() {
        this.props.onDelete(this.props.content.key);
    }

    render() {
        return (
            <div className="chip">
                    <div className="chip-text">{this.props.content.value}</div>
                    <div className="chip-close-btn" onClick={this.onDeleteClick}>
                        <i className="fw fw-uncheck"></i>
                    </div>
            </div>
        )
    }
}

Chip.propTypes = {
    onDelete: PropTypes.func,
    content: PropTypes.object
};

export default Chip;
