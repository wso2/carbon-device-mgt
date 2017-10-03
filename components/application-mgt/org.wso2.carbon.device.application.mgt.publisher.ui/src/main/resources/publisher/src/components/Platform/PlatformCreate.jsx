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
import {Button, FormGroup, Label, Modal, ModalBody, ModalFooter, ModalHeader, Input} from "reactstrap";

/**
 * Platform view component.
 * */
class PlatformCreate extends Component {

    constructor() {
        super();
        this.onCancelClick = this.onCancelClick.bind(this);
        this.state = {
            open: false
        }
    }


    componentWillReceiveProps(props, nextprops) {
        this.setState({open: props.open})
    }

    componentWillMount() {
        this.setState({open: this.props.open});
    }

    onCancelClick() {
        this.setState({open: false})
    }

    render() {
        return (
            <div>
                <Modal isOpen={this.state.open} toggle={this.toggle} id="platform-create-modal" backdrop={'static'}>
                    <ModalHeader>Create Platform</ModalHeader>
                    <ModalBody>
                        <FormGroup>
                            <Label for="platform-name">Name*</Label>
                            <Input required type="text" name="appName" id="platform-name"/>
                        </FormGroup>
                        <FormGroup>
                            <Label for="platform-description">Description*</Label>
                            <Input required type="textarea" name="appName" id="platform-description"/>
                        </FormGroup>
                    </ModalBody>
                    <ModalFooter>
                        <Button onClick={this.onCancelClick}>Cancel</Button>
                        <Button color="primary">Create</Button>
                    </ModalFooter>
                </Modal>
            </div>
        );
    }
}

export default PlatformCreate;
