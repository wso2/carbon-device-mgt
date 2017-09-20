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
import {withRouter} from 'react-router-dom';
import IconButton from 'material-ui/IconButton'
import Create from 'material-ui/svg-icons/content/create';

/**
 * Application view component.
 * Shows the details of the application.
 * */
class ApplicationView extends Component{
    constructor() {
        super();
        this.state = {
            application: {}
        }
    }

    componentWillReceiveProps(props, nextProps) {
        this.setState({application: props.application});
        console.log(props.application, nextProps)
    }

    componentDidMount() {
        //Download image artifacts.
        // this.setState({application: this.props.application});
    }

    handleEdit() {
        this.props.history.push("/assets/apps/edit/" + this.state.application.uuid);
    }

    render() {
        const platform = this.state.application;
        console.log(platform);

        return (
            <div>

                <label>Application Name : {this.state.application.name}</label>
                <br/>
                <label>Description: {this.state.application.description}</label>
                <br/>

                <IconButton onClick={this.handleEdit.bind(this)}>
                    <Create/>
                </IconButton>

            </div>
        );
    }
}

export default withRouter(ApplicationView);
