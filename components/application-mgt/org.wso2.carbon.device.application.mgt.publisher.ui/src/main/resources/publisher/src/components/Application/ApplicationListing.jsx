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
import ApplicationMgtApi from '../../api/applicationMgtApi';
import {withRouter} from 'react-router-dom';
import TextField from 'material-ui/TextField';
import DataTable from '../UIComponents/DataTable';
import {Card, CardActions, CardTitle} from 'material-ui/Card';
import Theme from '../../theme';
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
        this.setData = this.setData.bind(this);
        this.sortData = this.sortData.bind(this);
        this.compare = this.compare.bind(this);
        this.state = {
            searchedApplications: [],
            applications: [],
            asc: true
        };
        this.scriptId = "application-listing";
    }

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
            label: "Application Name",
            sort: this.sortData
        },
        {
            data_id: "platform",
            data_type: "image_array",
            sortable: false,
            label: "Platform"
        },
        {
            data_id: "category",
            data_type: "string",
            sortable: false,
            label: "Category"
        },
        {
            data_id: "status",
            data_type: "string",
            sortable: false,
            label: "Status"
        }
    ];

    componentWillMount() {

        /**
         *Loading the theme files based on the the user-preference.
         */
        Theme.insertThemingScripts(this.scriptId);
    }

    componentWillUnmount() {
        Theme.removeThemingScripts(this.scriptId);
        // this.setState({data: this.data});
    }

    componentDidMount() {
        let getApps = ApplicationMgtApi.getApplications();
        getApps.then(response => {
            let apps = this.setData(response.data.applications);
            console.log(apps);
            this.setState({searchedApplications: apps});
            // console.log(this.setState({data: response.data}), console.log(this.state));
        }).catch(err => {
            AuthHandler.unauthorizedErrorHandler(err);
        });
    }

    /**
     * Extract application from application list and update the state.
     * */
    setData(applications) {
        let apps = [];
        for (let app in applications) {
            let application = {};
            application.id = applications[app].uuid;
            application.applicationName = applications[app].name;
            application.platform = applications[app].platform.name;
            application.category = applications[app].category.id;
            application.status = applications[app].currentLifecycle.lifecycleState.name;
            apps.push(application);
        }

        this.setState({searchedApplications: apps});
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

    onRowClick(id) {
        ApplicationMgtApi.getApplication(id).then(response => {
            console.log(response);
        }).catch(err => {
            console.log(err)
        });
        // this.props.history.push("apps/" + id);
    }

    render() {
        return (
            <div className="middle applicationListingMiddle">
                <Card className="applicationListingCard">
                    <TextField
                        hintText="Search"
                        className="applicationListingSearch"
                        onChange={this.searchApplications}/>
                    <CardTitle title="Applications" className="applicationListTitle"/>
                    <DataTable
                        headers={this.headers}
                        data={this.state.searchedApplications}
                        handleRowClick={this.onRowClick}
                        noDataMessage={{type: 'button', text: 'Create Application'}}
                    />
                </Card>
            </div>
        );
    }
}

ApplicationListing.propTypes = {};

export default withRouter(ApplicationListing);
