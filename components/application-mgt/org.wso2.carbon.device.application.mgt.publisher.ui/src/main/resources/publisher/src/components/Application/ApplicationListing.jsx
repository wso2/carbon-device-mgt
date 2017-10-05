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
import {withRouter} from 'react-router-dom';
import {Button, Col, Row} from 'reactstrap';
import Drawer from '../UIComponents/Drawer/Drawer';
import ApplicationView from './View/ApplicationView';
import {FormattedMessage} from 'react-intl';
import ApplicationMgtApi from "../../api/applicationMgtApi";
import AuthHandler from "../../api/authHandler";

/**
 * The App Create Component.
 *
 * Application creation is handled through a Wizard. (We use Material UI Stepper.)
 *
 * In each step, data will be set to the state separately.
 * When the wizard is completed, data will be arranged and sent to the api.
 * */
class ApplicationListing extends Component {
    constructor() {
        super();
        this.searchApplications = this.searchApplications.bind(this);
        this.onRowClick = this.onRowClick.bind(this);
        this.sortData = this.sortData.bind(this);
        this.compare = this.compare.bind(this);
        this.onAppEditClick = this.onAppEditClick.bind(this);
        this.getSelectedApplication = this.getSelectedApplication.bind(this);
        this.state = {
            searchedApplications: [],
            applications: [],
            asc: true,
            open: false,
            application: {},
            drawer: {},
            appListStyle: {},
            //TODO: Remove this declaration.
            image: [{id: "1", src: "https://www.greenfoot.org/images/logos/macos.png"},
                {
                    id: "2",
                    src: "http://dl1.cbsistatic.com/i/r/2016/08/08/0e67e43a-5a45-41ab-b81d-acfba8708044/resize/736x552/0c0ee669677b5060a0fa1bfb0c7873b4/android-logo-promo-470.png"
                }]
        };
    }

    applications = [
        {
            id: "3242342ffww3423",
            applicationName: "Facebook",
            platform: "android",
            category: "Business",
            status: "Published"
        },
        {
            icon: "http://dl1.cbsistatic.com/i/r/2016/08/08/0e67e43a-5a45-41ab-b81d-acfba8708044/resize/736x552/0c0ee669677b5060a0fa1bfb0c7873b4/android-logo-promo-470.png",
            id: "324234233423423",
            applicationName: "Twitter",
            platform: "android",
            category: "Business",
            status: "Created"
        },
        {
            icon: "https://www.greenfoot.org/images/logos/macos.png",
            id: "3242d3423423423",
            applicationName: "Massenger",
            platform: "android",
            category: "Business",
            status: "In Review"
        },
    ];

    headers = [
        {
            data_id: "image",
            data_type: "image",
            sortable: false,
            label: ""
        },
        {
            data_id: "applicationName",
            data_type: "string",
            sortable: true,
            locale: "Application.name",
            label: "Application Name",
            sort: this.sortData
        },
        {
            data_id: "platform",
            data_type: "image_array",
            sortable: false,
            locale: "Platform",
            label: "Platform"
        },
        {
            data_id: "category",
            data_type: "string",
            sortable: false,
            locale: "Category",
            label: "Category"
        },
        {
            data_id: "status",
            data_type: "string",
            sortable: false,
            locale: "Status",
            label: "Status"
        },
        {
            data_id: "edit",
            data_type: "button",
            sortable: false,
            label: ""
        }
    ];

    componentWillMount() {

        let getApps = ApplicationMgtApi.getApplications();
        getApps.then(response => {
            console.log(response);
            this.setState({searchedApplications: response.data.applications});
        }).catch(err => {
            AuthHandler.unauthorizedErrorHandler(err);
        });
    }

    /**
     * Handles the search action.
     * When typing in the search bar, this method will be invoked.
     * @param event: The event triggered from typing in the search box.
     * @param searchText: The text that typed in the search box.
     * */
    searchApplications(event, searchText) {
        let searchedData;
        if (searchText) {
            searchedData = this.state.applications.filter((dataItem) => {
                return dataItem.applicationName.includes(searchText);
            });
        } else {
            searchedData = this.state.applications;
        }

        //TODO: Remove the console log.
        this.setState({searchedApplications: searchedData}, console.log("Searched data ", this.state.searchedApplications));
    }

    /**
     * Handles sort data function and toggles the asc state.
     * asc: true : sort in ascending order.
     * */
    sortData() {
        console.log(this.state);
        let isAsc = this.state.asc;
        let sortedData = isAsc ? this.state.searchedApplications.sort(this.compare) : this.data.reverse();
        this.setState({searchedApplications: sortedData, asc: !isAsc});
    }

    compare(a, b) {
        if (a.applicationName < b.applicationName)
            return -1;
        if (a.applicationName > b.applicationName)
            return 1;
        return 0;
    }

    onRowClick(uuid) {
        let selectedApp = this.getSelectedApplication(uuid);
        let style = {
            width: '550px',
            marginLeft: '550px'
        };

        let appListStyle = {
            marginRight: '550px',
        };

        this.setState({drawer: style, appListStyle: appListStyle, application: selectedApp[0]});
    }

    onAppEditClick(uuid) {
        this.props.history.push("apps/edit/" + uuid);
    }

    closeDrawer() {
        let style = {
            width: '0',
            marginLeft: '0'
        };

        let appListStyle = {
            marginRight: '0',
        };
        this.setState({drawer: style, appListStyle: appListStyle});
    }

    getSelectedApplication(uuid) {
        return this.state.searchedApplications.filter(application => {
            return application.uuid === uuid;
        });
    }

    render() {
        //TODO: Move this to a data table component.
        return (
            <div id="application-list" style={this.state.appListStyle}>
                <Row className="app-list-table-header">
                    {this.headers.map(header => {
                        if (header.data_id === "applicationName") {
                            return (
                                <Col xs="5">{header.label}</Col>)
                        } else if (header.data_id === "image") {
                            return (<Col xs="1">{header.label}</Col>)
                        }
                        return (<Col>{header.label}</Col>)
                    })}
                </Row>
                <hr/>
                {this.state.searchedApplications.map(application => {
                    return (
                        <Row className="app-table-row" onClick={() => {
                            this.onRowClick(application.uuid)
                        }}>
                            <Col xs="1">
                                <img
                                    className="app-list-icon"
                                    src={application.icon}
                                />
                            </Col>
                            <Col xs="5" className="data-table-row-cell"><strong>{application.name}</strong></Col>
                            <Col className="data-table-row-cell">{application.platform.name}</Col>
                            <Col className="data-table-row-cell">{application.category.name}</Col>
                            <Col
                                className="data-table-row-cell">{application.currentLifecycle.lifecycleState.name}
                            </Col>
                            <Col>
                                <Button id="secondary-button" onClick={() => this.onAppEditClick(application.uuid)}>
                                    <i className="fw fw-edit"></i>
                                </Button>
                            </Col>
                        </Row>
                    )
                })}
                <Drawer onClose={this.closeDrawer.bind(this)} style={this.state.drawer}>
                    <ApplicationView application={this.state.application}/>
                </Drawer>
            </div>
        );
    }
}

ApplicationListing.propTypes = {};

export default withRouter(ApplicationListing);
