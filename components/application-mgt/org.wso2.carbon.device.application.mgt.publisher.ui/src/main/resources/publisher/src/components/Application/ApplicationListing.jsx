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
import TextField from 'material-ui/TextField';
import DataTable from '../UIComponents/DataTable';
import {Card, CardActions, CardTitle} from 'material-ui/Card';
<<<<<<< HEAD
import Theme from '../../theme';
=======
import Theme from '../../themes/theme';
>>>>>>> parent of 8f3d11f... refactoring theming support

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
        this.state = {
            data: [],
            asc: true
        }
    }

    data = [
        {
            id: Math.random(),
            applicationName:"Cne",
            platform:'Android',
            category:"Public",
            status: "Created"
        },
        {
            id: Math.random(),
            applicationName:"Gone",
            platform:'IOS',
            category:"Public",
            status: "Created"
        },
        {
            id: Math.random(),
            applicationName:"Ane",
            platform:'Android',
            category:"Public",
            status: "Created"
        },
        {
            id: Math.random(),
            applicationName:"one",
            platform:'Android',
            category:"Public",
            status: "Created"
        },
        {
            id: Math.random(),
            applicationName:"one",
            platform:'Android',
            category:"Public",
            status: "Created"
        },
    ];

    headers = [
        {
            data_id: "image",
            data_type: "image",
            sortable: false,
            label: ""},
        {
            data_id: "applicationName",
            data_type: "string",
            sortable: true,
            label: "Application Name",
            sort: this._sortData.bind(this)
        },
        {
            data_id: "platform",
            data_type: "image_array",
            sortable: false,
            label: "Platform"},
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
        //Fetch all the applications from backend and create application objects.
        this.setState({data: this.data});
<<<<<<< HEAD
        /**
         *Loading the theme files based on the the user-preference.
         */
        const selected =
            (Theme.currentThemeType === Theme.defaultThemeType) ? Theme.defaultThemeType : Theme.currentTheme;
        const applicationListingCss = "application-listing.css";
        const applicationListingId = "application-listing";
        let themePath  =  "/" + Theme.themeFolder + "/" + selected + "/" + applicationListingCss;
        let promisedConfig = Theme.loadThemeFiles(themePath);
        let styleSheet = document.getElementById(applicationListingId);
        let head = document.getElementsByTagName("head")[0];
        let link = document.createElement("link");
        link.type = Theme.styleSheetType;
        link.href = Theme.baseURL + "/" + Theme.appContext + themePath;
        link.id = applicationListingId;
        link.rel = Theme.styleSheetRel;

        if (styleSheet !== null) {
            styleSheet.disabled = true;
            styleSheet.parentNode.removeChild(styleSheet);
        }
        promisedConfig.then(function () {
            head.appendChild(link);
        }).catch(function () {
            // If there is no customized css file, load the default one.
            themePath = "/" + Theme.themeFolder + "/" + Theme.defaultThemeType + "/" + applicationListingCss;
            link.href = Theme.baseURL + "/" + Theme.appContext + themePath;
            head.appendChild(link);
        });
    }

    componentWillUnmount() {
        let styleSheet = document.getElementById("application-listing");
        if (styleSheet !== null) {
            styleSheet.disabled = true;
            styleSheet.parentNode.removeChild(styleSheet);
=======

        let selected = Theme.selectedTheme;
        if (Theme.currentTheme === "default") {
            require("../../themes/default/application-listing.css");
        } else {
            try {
                require("../../themes/" + selected + "/application-listing.css");
            } catch (ex) {
                // If the particular customized file does not exist, use the default one.
                require("../../themes/default/application-listing.css");
            }
>>>>>>> parent of 8f3d11f... refactoring theming support
        }
    }


    /**
     * Handles the search action.
     * When typing in the search bar, this method will be invoked.
     * */
    _searchApplications(event, word) {
        let searchedData;
            if (word){
                searchedData = this.data.filter((dataItem) => {
                        return dataItem.applicationName.includes(word);
                    });
            } else {
                searchedData = this.data;
            }

        this.setState({data: searchedData}, console.log("Searched data ", this.state.data));

    }

    /**
     * Handles sort data function and toggles the asc state.
     * asc: true : sort in ascending order.
     * */
    _sortData() {
        let isAsc = this.state.asc;
        let datas = isAsc?this.data.sort(this._compare):this.data.reverse();
        this.setState({data: datas, asc: !isAsc});
    }

    _compare(a, b) {
        if (a.applicationName < b.applicationName)
            return -1;
        if (a.applicationName > b.applicationName)
            return 1;
        return 0;
    }

    _onRowClick(id) {
        this.props.history.push("apps/"+id);
    }

    render() {
        return (
            <div className="middle applicationListingMiddle">
                <Card className="applicationListingCard">
                    <TextField hintText="Search" className="applicationListingSearch"
                               onChange={this._searchApplications.bind(this)}/>
                    <CardTitle title="Applications" className="applicationListTitle"/>
                    <CardActions>

                    </CardActions>
                    <DataTable headers={this.headers}
                               data={this.state.data}
                               handleRowClick={this._onRowClick.bind(this)}
                               noDataMessage={{type: 'button', text: 'Create Application'}}/>
                </Card>

            </div>);
    }
}

ApplicationListing.propTypes = {};

export default withRouter(ApplicationListing);
