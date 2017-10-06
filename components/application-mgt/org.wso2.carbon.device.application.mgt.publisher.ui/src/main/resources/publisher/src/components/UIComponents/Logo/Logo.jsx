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
import Axios from 'axios';

const imageLocation = "/images/";

class Logo extends Component {

    constructor() {
        super();
        this.state = {
            image: ""
        }
    }

    componentWillMount() {
        let url = imageLocation + this.props.image_name;
        Axios.get(url, {responseType: 'arraybuffer'}).then(
            response => {
                let image = "data:image/jpeg;base64," + new Buffer(response.data, 'binary').toString('base64');
                this.setState({image: image});
            }
        ).catch(err => {
            console.log(err);
        });
    }

    render() {
        return (
            <img className={this.props.className} src={this.state.image} />
        )
    }

}

Logo.prototypes = {
    className: PropTypes.string,
    image_name: PropTypes.string
};

export default Logo;
