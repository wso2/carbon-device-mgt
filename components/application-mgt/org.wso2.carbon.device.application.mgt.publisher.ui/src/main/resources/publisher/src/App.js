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

import React, { Component } from 'react';
import {BrowserRouter as Router, Route, Switch, Redirect} from 'react-router-dom'
import './App.css';
import {Login, BaseLayout} from './components'

class Base extends Component {
    constructor() {
        super();
        this.state = {
            user: ""
        }
    }

    render() {
        if (this.state.user) {
            return(<BaseLayout>
                    <Switch>
                        <Route path={"assets/apps"}/>
                        <Route path={"assets/apps/:app"}/>
                        <Route path={"assets/apps/create"}/>
                        <Route path={"assets/apps/edit"}/>
                        <Route path={"assets/platform/:platform"}/>
                        <Route path={"assets/platform/create"}/>
                        <Route path={"assets/platform/edit"}/>
                        <Route path={"assets/reviews"}/>
                        <Route path={"assets/reviews/:review"}/>
                    </Switch>
                </BaseLayout>
            )
        }

        return (<Redirect to={"/login"}/>)
    }
}

class Publisher extends Component {
  render() {
    return (
      <div className="App">
          <Router basename="publisher">
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
