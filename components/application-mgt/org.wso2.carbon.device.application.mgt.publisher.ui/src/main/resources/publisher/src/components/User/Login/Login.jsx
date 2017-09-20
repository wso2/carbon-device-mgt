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
import {Redirect, Switch} from 'react-router-dom';
import AuthHandler from '../../../api/authHandler';
import {Button, Card, CardBlock, CardSubtitle, CardTitle, Col, Form, FormGroup, Input, Label} from 'reactstrap';

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

    handleLogin(event) {
        event.preventDefault();
        this.validateForm();
    }

    /**
     * Handles the username field change event.
     * */
    onUserNameChange(event, value) {
        console.log(event.target.value);
        this.setState(
            {
                userName: event.target.value
            }
        );
    }

    /**
     * Handles the password field change event.
     * */
    onPasswordChange(event, value) {
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

    /**
     * Validate the login form.
     * */
    validateForm() {
        let errors = {};
        let validationFailed = true;
        if (!this.state.password) {
            errors["passwordError"] = "Password is Required";
            validationFailed = true;
        } else {
            validationFailed = false;
        }

        if (!this.state.userName) {
            errors["userNameError"] = "User Name is Required";
            validationFailed = true;
        } else {
            validationFailed = false;
        }

        if (validationFailed) {
            this.setState({errors: errors}, console.log(errors));
        } else {
            let loginPromis = AuthHandler.login(this.state.userName, this.state.password);
            loginPromis.then(response => {
                console.log(AuthHandler.getUser());
                this.setState({isLoggedIn: AuthHandler.getUser()});
            })
        }
    }

    render() {

        if (!this.state.isLoggedIn) {
            return (
                <div style={{width: '50%', margin: '0 auto'}}>
                    {/*TODO: Style the components.*/}
                    <Card id="login-card">
                    <CardBlock>
                        <CardTitle>WSO2 IoT APP Publisher</CardTitle>
                        <Form onSubmit={this.handleLogin.bind(this)}>
                            <FormGroup row>
                                <Label for="userName" sm={2}>User Name:</Label>
                                <Col sm={10}>
                                    <Input type="text" name="userName" id="userName" placeholder="User Name" onChange={this.onUserNameChange.bind(this)}/>
                                </Col>

                            </FormGroup>
                            <FormGroup row>
                                <Label for="password" sm={2}>Password:</Label>
                                <Col sm={10}>
                                    <Input type="password" name="text" id="password" placeholder="Password" onChange={this.onPasswordChange.bind(this)}/>
                                </Col>
                            </FormGroup>
                            <FormGroup check row>
                                <Col sm={{ size: 10, offset: 2 }}>
                                    <Button type="submit" id="login-btn">Submit</Button>
                                </Col>
                            </FormGroup>
                        </Form>
                    </CardBlock>
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
