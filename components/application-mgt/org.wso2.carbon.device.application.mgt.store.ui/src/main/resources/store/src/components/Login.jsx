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

import qs from 'qs';
import React, {Component} from 'react';
import Checkbox from 'material-ui/Checkbox';
import {Redirect, Route} from 'react-router-dom';
import RaisedButton from 'material-ui/RaisedButton';
import {Card, CardActions, CardTitle} from 'material-ui/Card';
import {TextValidator, ValidatorForm} from 'react-material-ui-form-validator';
import Store from '../App';


//todo: remove the {TextValidator, ValidatorForm} and implement it manually.


/**
 * The Login Component.
 *
 * This component contains the Login form and methods to handle field change events.
 * The user name and password will be set to the state and sent to the api.
 *
 * If the user is already logged in, it will redirect to the last point where the user was.
 * */
class Login extends Component {
    constructor() {
        super();
        this.state = {
            isLoggedIn: false,
            referrer: "/",
            userName: "",
            rememberMe: true
        }
    }

    componentDidMount() {
        let queryString = this.props.location.search;
        console.log(queryString);
        queryString = queryString.replace(/^\?/, '');
        /* With QS version up we can directly use {ignoreQueryPrefix: true} option */
        let params = qs.parse(queryString);
        if (params.referrer) {
            this.setState({referrer: params.referrer});
        }
    }

    handleLogin(event) {
        event.preventDefault();
        console.log(this.props);
        //TODO: send authentication request.
        let location = {
            pathname: this.state.referrer
        };
        let storeState = {
            store : {
                user: this.state.userName,
                notifications: 0
            }
        };
        this.props.updateState(storeState);
        this.props.history.push(location);
    }

    /**
     * Handles the username field change event.
     * */
    onUserNameChange(event) {
        this.setState(
            {
                userName: event.target.value
            }
        );
    }

    /**
     * Handles the password field change event.
     * */
    onPasswordChange(event) {
        this.setState(
            {
                password: event.target.value
            }
        );
    }

    /**
     * Handles the remember me check.
     * */
    handleRememberMe() {
        this.setState(
            {
                rememberMe: !this.state.rememberMe
            }
        );
    }

    handleSuccessfulLogin() {
        return (
            <Redirect to='/store'/>
        );
    }

    render() {
        if (!(this.state.isLoggedIn && this.state.userName)) {
            return (
                <div>

                    {/*TODO: Style the components.*/}

                    <Card>
                        <CardTitle title="WSO2 IoT App Store"/>
                        <CardActions>
                            <ValidatorForm
                                ref="form"
                                onSubmit={this.handleLogin.bind(this)}
                                onError={errors => console.log(errors)}>
                                <TextValidator
                                    floatingLabelText="User Name"
                                    floatingLabelFixed={true}
                                    onChange={this.onUserNameChange.bind(this)}
                                    name="userName"
                                    validators={['required']}
                                    errorMessages={['User Name is required']}
                                    value={this.state.userName}
                                />
                                <br/>
                                <TextValidator
                                    floatingLabelText="Password"
                                    floatingLabelFixed={true}
                                    onChange={this.onPasswordChange.bind(this)}
                                    name="password"
                                    type="password"
                                    value={this.state.password}
                                    validators={['required']}
                                    errorMessages={['Password is required']}
                                />
                                <br/>
                                <Checkbox label="Remember me."
                                          onCheck={this.handleRememberMe.bind(this)}
                                          checked={this.state.rememberMe}/>
                                <br/>
                                <RaisedButton type="submit" label="Login"/>
                            </ValidatorForm>
                        </CardActions>
                    </Card>
                </div>);
        } else {
            this.handleSuccessfulLogin();
        }
    }
}

export default Login;
