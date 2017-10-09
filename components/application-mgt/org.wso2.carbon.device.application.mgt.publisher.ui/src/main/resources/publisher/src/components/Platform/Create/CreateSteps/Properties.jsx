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
import {Button, Col, FormGroup, Input, Label, ModalBody, ModalFooter, Row} from "reactstrap";
import {FormattedMessage} from "react-intl";

/**
 * key : value +
 * */
class Properties extends Component {
    constructor() {
        super();
        this.onCancelClick = this.onCancelClick.bind(this);
        this.onBackClick = this.onBackClick.bind(this);
        this.submitForm = this.submitForm.bind(this);
        this.onAddPropertyClick = this.onAddPropertyClick.bind(this);
        this.onKeyFieldChange = this.onKeyFieldChange.bind(this);
        this.onValueFieldChange = this.onValueFieldChange.bind(this);
        this.onPropertyDelete = this.onPropertyDelete.bind(this);
        this.state = {
            key: "",
            value: "",
            properties: []
        }
    }

    onCancelClick() {
        this.props.close();
    }

    onBackClick() {
        this.props.handlePrev();
    }

    submitForm() {
        let platformProps = {
            "properties": this.state.properties
        };
        this.props.onSubmit(platformProps);
    }

    onAddPropertyClick() {
        let {key, value, properties} = this.state;
        let property = {
            name: key,
            defaultValue: value,
            optional: false
        };

        properties.push(property);

        this.setState({property: properties, key: "", value: ""})
    }

    onKeyFieldChange(event) {
        this.setState({key: event.target.value});
    }

    onValueFieldChange(event) {
        this.setState({value: event.target.value});
    }

    onPropertyDelete(key) {
        let properties = this.state.properties;
        const propertyToDelete = properties.map((property) => property.name).indexOf(key);
        properties.splice(propertyToDelete, 1);
        this.setState({properties: properties});
    }

    render() {
        return (
            <div>
                <ModalBody>
                    <FormGroup>
                        <Label for="platform-properties">
                            <FormattedMessage id="Platform.Properties" defaultMessage="Platform.Properties"/></Label>
                        <Row>
                            <Col>
                                <Input
                                    placeholder="Key"
                                    type="text"
                                    name="app-tags"
                                    id="app-tags"
                                    value={this.state.key}
                                    onChange={this.onKeyFieldChange}
                                />
                            </Col>
                            <Col>
                                <Input
                                    placeholder="value"
                                    type="text"
                                    name="app-tags"
                                    id="app-tags"
                                    value={this.state.value}
                                    onChange={this.onValueFieldChange}
                                />
                            </Col>
                            <Col>
                                <Button className="custom-flat circle-btn-add" onClick={this.onAddPropertyClick}>
                                    <i className="fw fw-add"></i>
                                </Button>
                            </Col>
                        </Row>
                        <div className="platform-property-container">
                            {this.state.properties.map(
                                property => {
                                    return (
                                        <Row key={property.name} className="platform-property-row">
                                            <Col key={property.name}>
                                                {property.name}
                                            </Col>
                                            <Col>
                                                {property.defaultValue}
                                            </Col>
                                            <Col>
                                                <Button
                                                    className="custom-flat circle-btn-clear"
                                                    onClick={() => {
                                                        this.onPropertyDelete(property.name)
                                                    }}
                                                >
                                                    <i className="fw fw-error"></i>
                                                </Button>
                                            </Col>
                                        </Row>
                                    )
                                }
                            )}
                        </div>
                    </FormGroup>
                </ModalBody>
                <ModalFooter className="custom-footer row">
                    <div className="footer-back-btn col">
                        <Button className="custom-flat primary-flat" onClick={this.onBackClick}>
                            <FormattedMessage id="Back" defaultMessage="Back"/>
                        </Button>
                    </div>
                    <div className="footer-main-btn col">
                        <Button className="custom-flat danger-flat" onClick={this.onCancelClick}>
                            <FormattedMessage id="Cancel" defaultMessage="Cancel"/>
                        </Button>
                        <Button className="custom-flat primary-flat" onClick={this.submitForm}>
                            <FormattedMessage id="Create" defaultMessage="Create"/>
                        </Button>
                    </div>
                </ModalFooter>
            </div>
        )
    }

}

export default Properties;
