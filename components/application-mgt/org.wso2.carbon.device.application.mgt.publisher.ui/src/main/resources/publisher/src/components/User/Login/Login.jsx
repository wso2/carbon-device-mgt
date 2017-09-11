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
import PropTypes from 'prop-types';
import React, {Component} from 'react';
import Checkbox from 'material-ui/Checkbox';
import TextField from 'material-ui/TextField';
import {Redirect, Switch} from 'react-router-dom';
import RaisedButton from 'material-ui/RaisedButton';
import {Card, CardActions, CardTitle} from 'material-ui/Card';

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
            password: "",
            rememberMe: true,
            errors: {}
        }
    }

    componentWillMount() {
        console.log("IN Login")
    }

    componentDidMount() {
        console.log("in Login")
        // let queryString = this.props.location.search;
        // console.log(queryString);
        // queryString = queryString.replace(/^\?/, '');
        // /* With QS version up we can directly use {ignoreQueryPrefix: true} option */
        // let params = qs.parse(queryString);
        // if (params.referrer) {
        //     this.setState({referrer: params.referrer});
        // }
    }

    _handleLogin(event) {
        event.preventDefault();
        this._validateForm();
    }

    /**
     * Handles the username field change event.
     * */
    _onUserNameChange(event, value) {
        this.setState(
            {
                userName: value
            }
        );
    }

    /**
     * Handles the password field change event.
     * */
    _onPasswordChange(event, value) {
        this.setState(
            {
                password: value
            }
        );
    }

    /**
     * Handles the remember me check.
     * */
    _handleRememberMe() {
        this.setState(
            {
                rememberMe: !this.state.rememberMe
            }
        );
    }

    /**
     * Validate the login form.
     * */
    _validateForm() {
        let errors = {};
        if (!this.state.password) {
            errors["passwordError"] = "Password is Required";
        }

        if (!this.state.userName) {
            errors["userNameError"] = "User Name is Required";
        }

        this.setState({errors: errors}, console.log(errors));
    }

    render() {

        if (!this.state.isLoggedIn) {
            return (
                <div>

                    {/*TODO: Style the components.*/}

                    <Card>
                        <CardTitle title="WSO2 IoT App Publisher"/>
                        <CardActions>
                            <form onSubmit={this._handleLogin.bind(this)}>
                                <TextField
                                    hintText="Enter the User Name."
                                    id="username"
                                    errorText={this.state.errors["userNameError"]}
                                    floatingLabelText="User Name*"
                                    floatingLabelFixed={true}
                                    value={this.state.userName}
                                    onChange={this._onUserNameChange.bind(this)}
                                /><br/>
                                <TextField
                                    hintText="Enter the Password."
                                    id="password"
                                    type="password"
                                    errorText={this.state.errors["passwordError"]}
                                    floatingLabelText="Password*"
                                    floatingLabelFixed={true}
                                    value={this.state.password}
                                    onChange={this._onPasswordChange.bind(this)}
                                /><br/>
                                <Checkbox label="Remember me."
                                          onCheck={this._handleRememberMe.bind(this)}
                                          checked={this.state.rememberMe}/>
                                <br/>
                                <RaisedButton type="submit" label="Login"/>
                            </form>
                        </CardActions>
                    </Card>
                </div>);
        } else {
            return (
                <Switch>
                    <Redirect to={this.state.referrer}/>
                </Switch>
            );
        }
    }
}

export default Login;
