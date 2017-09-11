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

import './App.css';
import React, {Component} from 'react';
import createHistory from 'history/createBrowserHistory';
import {BrowserRouter as Router, Redirect, Route, Switch} from 'react-router-dom'
import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import getMuiTheme from 'material-ui/styles/getMuiTheme';
import {
    ApplicationCreate,
    ApplicationListing,
    BaseLayout,
    Login,
    NotFound,
    PlatformCreate,
    PlatformListing
} from './components';
const history = createHistory({basename: '/publisher'});

/**
 * User can define the themes in the config.json. The themes will be loaded based on the user preference.
 */
const theme = require("./config.json").theme;
let muiTheme = null;
if (theme.current === "default") {
    let defaultTheme = require("material-ui/styles/baseThemes/" + theme.default);
    muiTheme = getMuiTheme(defaultTheme.default);
} else {
    let customTheme = require("./themes/" + theme.custom);
    muiTheme = getMuiTheme(customTheme.default);
}

/**
 * This component defines the layout and the routes for the app.
 * All the content will be loaded inside the Base component.
 * The base component includes the Core layout and the routers according to which the content will be displayed.
 *
 * The Router and Route components.
 *     The Router and Route is used for navigation.
 *     We specify the component which needs to be rendered for an URL.
 *     Ex: When navigate to publisher/overview, the overview component will be rendered inside the main layout.
 *
 * HashRouter is used because the other router types need the server to serve those urls. In hashRouter, server does
 * not want to serve the URL.
 * */
class Base extends Component {
    constructor() {
        super();
        this.state = {
            user: "admin"
        }
    }

    render() {
        if (this.state.user) {
            return (
                <div className="container">
                    <BaseLayout>
                        <Switch>
                            <Redirect exact path={"/"} to={"/assets/apps"}/>
                            <Route exact path={"/assets/apps"} component={ApplicationListing}/>
                            <Route exact path={"/assets/apps/create"} component={ApplicationCreate}/>
                            <Route exact path={"/assets/platforms"} component={PlatformListing}/>
                            <Route exact path={"/assets/platforms/create"} component={PlatformCreate}/>
                            <Route exact path={"/assets/apps/:app"} />
                            <Route exact path={"/assets/apps/:app/edit"} />
                            <Route exact path={"/assets/platforms/:platform"}/>
                            <Route exact path={"/assets/platforms/:platform/edit"}/>
                            <Route exact path={"/assets/reviews"}/>
                            <Route exact path={"/assets/reviews/:review"}/>
                            <Route component={NotFound}/>
                        </Switch>
                    </BaseLayout>
                </div>
            )
        }
        return (<Redirect to={"/login"}/>)
    }
}

/**
 * This component is referred by the index.js to initiate the application.
 * TODO: Currently the URL shows like https://localhost:9443/publisher/#/publisher/assets/apps/create. this needs to
 * be fixed as https://localhost:9443/publisher/#/assets/apps/create
 *
 * */
class Publisher extends Component {
    render() {
        return (
            <div className="App">
                <MuiThemeProvider muiTheme={muiTheme}>
                <Router basename="publisher" history={history}>
                    <Switch>
                        <Route path="/login" component={Login}/>
                        <Route path="/logout" component={Login}/>
                        <Route component={Base}/>
                    </Switch>
                </Router>
                </MuiThemeProvider>
            </div>
        );
    }
}

export default Publisher;
