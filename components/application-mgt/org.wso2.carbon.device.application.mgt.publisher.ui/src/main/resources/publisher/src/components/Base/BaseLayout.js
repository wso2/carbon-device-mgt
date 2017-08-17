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
import AppBar from 'material-ui/AppBar';
import Drawer from 'material-ui/Drawer';
import MenuItem from 'material-ui/MenuItem';
import Menu from 'material-ui/Menu';
import IconButton from 'material-ui/IconButton';
import Notifications from 'material-ui/svg-icons/social/notifications';
import ActionAccountCircle from 'material-ui/svg-icons/action/account-circle';
import {withRouter} from 'react-router-dom'


/**
 * Base Layout:
 * App bar
 * Left Navigation
 * Middle content.
 * */
class BaseLayout extends Component {

    handleApplicationClick() {
        console.log("Application");
        window.location = '/publisher/assets/apps';
    }

    render() {
        return (
            <div>
                <AppBar title="App Publisher"
                        iconElementRight={
                            <div>
                                <IconButton>
                                    <Notifications/>
                                </IconButton>
                                <IconButton onClick={() => {
                                    console.log("Clicked")
                                }}>
                                    <ActionAccountCircle/>
                                </IconButton>
                            </div>
                        }
                />
                <div>
                    <Drawer containerStyle={{height: 'calc(100% - 64px)', width: '15%', top: 64}} open={true}>
                        <Menu>
                            <MenuItem onClick={this.handleApplicationClick.bind(this)}>Applications</MenuItem>
                            <MenuItem>Platforms</MenuItem>
                            <MenuItem>Reviews</MenuItem>
                        </Menu>
                    </Drawer>
                </div>
                <div style=
                         {
                             {
                                 height: 'calc(100% - 64px)',
                                 marginLeft: '16%',
                                 width: 'calc(100%-15%)',
                                 top: 64,
                                 left: "-100px"
                             }
                         }>
                    {this.props.children}
                </div>
            </div>);
    }

}

export default withRouter(BaseLayout);