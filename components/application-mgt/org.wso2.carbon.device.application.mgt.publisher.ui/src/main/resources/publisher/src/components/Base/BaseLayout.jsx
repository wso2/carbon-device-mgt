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
import {withRouter} from 'react-router-dom';
import IconButton from 'material-ui/IconButton';
import {List, ListItem} from 'material-ui/List';
import Apps from 'material-ui/svg-icons/navigation/apps';
import Add from 'material-ui/svg-icons/content/add-circle';
import Feedback from 'material-ui/svg-icons/action/feedback';
import DevicesOther from 'material-ui/svg-icons/hardware/devices-other';
import NotificationsIcon from 'material-ui/svg-icons/social/notifications';
import ActionAccountCircle from 'material-ui/svg-icons/action/account-circle';
import Theme from '../../theme';


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
            user: 'Admin'
        }
    }

    componentWillMount() {
        /**
         *Loading the theme files based on the the user-preference.
         */
        console.log(Theme.currentThemeType);
        const selected =
            (Theme.currentThemeType === Theme.defaultThemeType) ? Theme.defaultThemeType : Theme.currentTheme;
        const basicLayoutCss = "basic-layout.css";
        const basicLayoutId = "basic-layout";
        let themePath  =  "/" + Theme.themeFolder + "/" + selected + "/" + basicLayoutCss;
        let themeFilefig = Theme.loadThemeFiles(themePath);
        let styleSheet = document.getElementById(basicLayoutId);
        let head = document.getElementsByTagName("head")[0];
        let link = document.createElement("link");
        link.type = Theme.styleSheetType;
        link.href = Theme.baseURL + "/" + Theme.appContext + themePath;
        link.id = basicLayoutId;
        link.rel = Theme.styleSheetRel;

        if (styleSheet !== null) {
            styleSheet.disabled = true;
            styleSheet.parentNode.removeChild(styleSheet);
        }

        themeFilefig.then(function() {
            head.appendChild(link);
        }).catch(function () {
            // If there is no customized css file, load the default one.
            themePath = "/" + Theme.themeFolder + "/" + Theme.defaultThemeType + "/" + basicLayoutCss;
            link.href = Theme.baseURL + "/" + Theme.appContext + themePath;
            head.appendChild(link);
        });

    }

    componentWillUnmount() {
        let styleSheet = document.getElementById("basic-layout");
        if (styleSheet !== null) {
            styleSheet.disabled = true;
            styleSheet.parentNode.removeChild(styleSheet);
        }
    }

    handleApplicationClick() {
        this.handleHistory('/assets/apps');
    }

    handleOverviewClick() {
        this.handleHistory('/overview');
    }

    handleApplicationCreateClick() {
        this.handleHistory('/assets/apps/create');
    }

    handlePlatformClick() {
        this.handleHistory('/assets/platforms');
    }

    handlePlatformCreateClick() {
        this.handleHistory('/assets/platforms/create');
    }

    handleReviewClick() {
        this.handleHistory('/assets/reviews');
    }

    /**
     * The method to update the history.
     * to: The URL to route.
     * */
    handleHistory(to) {
        this.props.history.push(to);
    }

    render() {
        return (

            <div>
                <AppBar title="App Publisher"
                        iconElementRight={
                            <div>
                                <Badge
                                    badgeContent={this.state.notifications}
                                    secondary={true}
                                    badgeStyle={{top: 12, right: 12}}
                                >
                                    <IconButton tooltip="Notifications">
                                        <NotificationsIcon/>
                                    </IconButton>
                                </Badge>
                                <IconButton onClick={() => {
                                    console.log("Clicked")
                                }}>
                                    <ActionAccountCircle/>
                                </IconButton>
                            </div>
                        }
                />
                <div>
                    <Drawer containerStyle={{height: 'calc(100% - 64px)', width: '15%', top: '10%'}} open={true}>
                        <List>
                           <ListItem primaryText="Applications"
                                      leftIcon={<Apps/>}
                                      initiallyOpen={false}
                                      primaryTogglesNestedList={true}
                                      onClick={this.handleApplicationClick.bind(this)}
                                      nestedItems={[
                                          <ListItem
                                              key={1}
                                              primaryText="Create"
                                              onClick={this.handleApplicationCreateClick.bind(this)}
                                              leftIcon={<Add/>}
                                          />]}
                            />
                            <ListItem primaryText="Platforms"
                                      leftIcon={<DevicesOther/>}
                                      initiallyOpen={false}
                                      primaryTogglesNestedList={true}
                                      onClick={this.handlePlatformClick.bind(this)}
                                      nestedItems={[
                                          <ListItem
                                              key={1}
                                              primaryText="Create"
                                              onClick={this.handlePlatformCreateClick.bind(this)}
                                              leftIcon={<Add/>}
                                          />]}
                            />
                            <ListItem primaryText="Reviews"
                                      onClick={this.handleReviewClick.bind(this)}
                                      leftIcon={<Feedback/>}/>
                        </List>
                    </Drawer>
                </div>
                <div className="basicLayoutDiv">
                    {this.props.children}
                </div>
            </div>);
    }

}

BaseLayout.propTypes = {
    children: PropTypes.element
};

export default withRouter(BaseLayout);
