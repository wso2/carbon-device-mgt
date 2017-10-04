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
import AuthHandler from './api/authHandler';
import createHistory from 'history/createBrowserHistory';
import {BrowserRouter as Router, Redirect, Route, Switch} from 'react-router-dom'
import {
    ApplicationCreate,
    ApplicationEdit,
    ApplicationListing,
    BaseLayout,
    Login,
    NotFound,
    PlatformCreate,
    PlatformListing
} from './components';


const history = createHistory({basename: '/store'});

/**
 * This component defines the layout and the routes for the app.
 * All the content will be loaded inside the Base component.
 * The base component includes the Core layout and the routers according to which the content will be displayed.
 *
 * The Router and Route components.
 *     The Router and Route is used for navigation.
 *     We specify the component which needs to be rendered for an URL.
 *     Ex: When navigate to store/overview, the overview component will be rendered inside the main layout.
 *
 * HashRouter is used because the other router types need the server to serve those urls. In hashRouter, server does
 * not want to serve the URL.
 * */
class Base extends Component {
    constructor() {
        super();
        this.state = {
            user: null
        }
    }

    componentWillMount() {
        let user = AuthHandler.getUser();
        if (user) {
            if (!AuthHandler.isTokenExpired()) {
                this.setState({user: user});
            } else {
                this.setState({user: null});
            }
        }
    }

    render() {
        console.log('in app.jsx');
        if (this.state.user !== null) {
            return (
                <div>
                    <BaseLayout user={this.state.user}>
                        <Switch>
                            <Redirect exact path={"/"} to={"/assets/apps"}/>
                            <Route exact path={"/assets/apps"} component={ApplicationListing}/>
                            <Route exact path={"/assets/apps/create"} component={ApplicationCreate}/>
                            <Route exact path={"/assets/platforms"} component={PlatformListing}/>
                            <Route exact path={"/assets/platforms/create"} component={PlatformCreate}/>
                            {/*<Route exact path={"/assets/apps/:app"}/>*/}
                            <Route exact path={"/assets/apps/edit/:app"} component={ApplicationEdit}/>
                            <Route exact path={"/assets/platforms/:platform"}/>
                            <Route exact path={"/assets/platforms/:platform/edit"}/>
                            <Route exact path={"/assets/reviews"}/>
                            <Route exact path={"/assets/reviews/:review"}/>
                            <Route component={NotFound}/>
                        </Switch>
                    </BaseLayout>
                </div>
            );
        } else {
            return (
                <div>
                    <BaseLayout>
                        <Switch>
                            <Redirect exact path={"/"} to={"/assets/apps"}/>
                            <Route exact path={"/assets/apps"} component={ApplicationListing}/>
                            <Route component={NotFound}/>
                        </Switch>
                    </BaseLayout>
                </div>
            );
        }

    }
}

/**
 * This component is referred by the index.js to initiate the application.
 * TODO: Currently the URL shows like https://localhost:9443/store/#/store/assets/apps/create. this needs to
 * be fixed as https://localhost:9443/store/#/assets/apps/create
 *
 * */
class Store extends Component {
    constructor() {
        super();
    }

    render() {
        return (
            <div className="App">
                <Router basename="store" history={history}>
                    <Switch>
                        <Route path="/login" component={Login}/>
                        <Route path="/logout" component={Login}/>
                        <Route component={Base}/>
                    </Switch>
                </Router>
            </div>
        );
    }
}

export default Store;
