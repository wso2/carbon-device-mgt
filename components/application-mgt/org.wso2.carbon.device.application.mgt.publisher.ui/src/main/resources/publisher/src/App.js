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

import './App.css'
import React, {Component} from 'react';
import createHistory from 'history/createHashHistory';
import {HashRouter as Router, Redirect, Route, Switch} from 'react-router-dom'
import {BaseLayout, Create, Login, NotFound, PublisherOverview} from './components'

const history = createHistory({basename: '/publisher'});

class Base extends Component {
    constructor() {
        super();
        this.state = {
            user: "s"
        }
    }

    render() {
        if (this.state.user) {
            return (
                <div className="container">
                    <BaseLayout>
                        <Switch>
                            <Redirect exact path={"/"} to={"/overview"}/>
                            <Route exact path={"/overview"} component={PublisherOverview}/>
                            {/*<Route path={"/assets/apps"} component={}/>*/}
                            {/*<Route path={"/assets/apps/:app"} component={}/>*/}
                            <Route path={"/assets/apps/create"} component={Create}/>
                            {/*<Route path={"/assets/apps/edit/:app"} component="app edit"/>*/}
                            <Route path={"/assets/platforms/:platform"}/>
                            <Route path={"/assets/platforms/create"}/>
                            <Route path={"/assets/platforms/edit/:platform"}/>
                            <Route path={"/assets/reviews"}/>
                            <Route path={"/assets/reviews/:review"}/>
                            <Route component={NotFound}/>
                        </Switch>
                    </BaseLayout>
                </div>
            )
        }

        return (<Redirect to={"/login"}/>)
    }
}

class Publisher extends Component {
    render() {
        return (
            <div className="App">
                <Router basename="publisher" history={history}>
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

export default Publisher;
