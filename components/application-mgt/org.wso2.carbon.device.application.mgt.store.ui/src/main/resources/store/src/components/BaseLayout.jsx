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
import Badge from 'material-ui/Badge';
import React, {Component} from 'react';
import AppBar from 'material-ui/AppBar';
import Drawer from 'material-ui/Drawer';
import IconButton from 'material-ui/IconButton';
import {List, ListItem} from 'material-ui/List';
import Apps from 'material-ui/svg-icons/navigation/apps';
import NotificationsIcon from 'material-ui/svg-icons/social/notifications';
import ActionAccountCircle from 'material-ui/svg-icons/action/account-circle';
import {Link, withRouter} from 'react-router-dom';

/**
 * Base Layout:
 * App bar
 * Left Navigation
 * Middle content.
 * */
class BaseLayout extends Component {

    handleApplicationClick() {
        this.handleHistory('/assets/apps');
    }


    /**
     * The method to update the history.
     * to: The URL to route.
     * */
    handleHistory(to) {
        this.props.history.push(to);
    }

    handleUserLogin() {
        if (this.props.state.store.user) {
            return (
                <IconButton tooltip={this.props.state.store.user}>
                    <ActionAccountCircle/>
                </IconButton>
            );
        } else {
            return (
                <Link to='/login'> Login</Link>
            );
        }
    }

    handleNotification() {
        if (this.props.state.store.user) {
            return (
                <Badge
                    badgeContent={this.props.state.store.notifications}
                    secondary={true}
                    badgeStyle={{top: 12, right: 12}}>
                    <IconButton tooltip="Notifications">
                        <NotificationsIcon/>
                    </IconButton>
                </Badge>
            );
        }
    }

    render() {
        return (
            <div>
                <AppBar title="App Store"
                        iconElementRight={
                            <div>
                                {this.handleNotification()}
                                {this.handleUserLogin()}
                            </div>
                        }
                />
                <div>
                    <Drawer containerStyle={{height: 'calc(100% - 64px)', width: '15%', top: '13%', left: '1%'}}
                            open={true}>
                        <List>
                            <ListItem primaryText="Applications"
                                      leftIcon={<Apps/>}
                                      initiallyOpen={false}
                                      primaryTogglesNestedList={true}
                                      onClick={this.handleApplicationClick.bind(this)}
                                      nestedItems={[
                                          <ListItem
                                              key={1}
                                              primaryText="Business" //TODO: categoryies ...
                                              leftIcon={<List/>}
                                          />]}
                            />
                        </List>
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

BaseLayout.propTypes = {
    children: PropTypes.element
};

export default withRouter(BaseLayout);
