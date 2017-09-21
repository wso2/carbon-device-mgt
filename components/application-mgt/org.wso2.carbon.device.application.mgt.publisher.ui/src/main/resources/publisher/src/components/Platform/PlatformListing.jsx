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

import Theme from '../../theme';
import React, {Component} from 'react';
import {withRouter} from 'react-router-dom';
import TextField from 'material-ui/TextField';
import AuthHandler from "../../api/authHandler";
import DataTable from '../UIComponents/DataTable/DataTable';
import PlatformMgtApi from "../../api/platformMgtApi";
import {Card, CardActions, CardTitle} from 'material-ui/Card';

/**
 * The App Create Component.
 *
 * Application creation is handled through a Wizard. (We use Material UI Stepper.)
 *
 * In each step, data will be set to the state separately.
 * When the wizard is completed, data will be arranged and sent to the api.
 * */
class PlatformListing extends Component {
    constructor() {
        super();
        this.setPlatforms = this.setPlatforms.bind(this);
        this.state = {
            platforms: [],
            asc: true
        };
        this.scriptId = "platform-listing";
    }

    headers = [
        {
            data_id: "image",
            data_type: "image",
            sortable: false,
            label: ""
        },
        {
            data_id: "platformName",
            data_type: String,
            sortable: true,
            label: "Platform Name",
            sort: this.sortData
        },
        {
            data_id: "enabled",
            data_type: String,
            sortable: false,
            label: "Enabled"
        },
        {
            data_id: "fileBased",
            data_type: String,
            sortable: false,
            label: "File Based"
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
    }

    componentDidMount() {
        let platformsPromise = PlatformMgtApi.getPlatforms();
        platformsPromise.then(
            response => {
                let platforms = this.setPlatforms(response.data);
                this.setState({platforms: platforms});
            }
        ).catch(
            err => {
                AuthHandler.unauthorizedErrorHandler(err);
            }
        )
    }

    /**
     * Create platform objects from the response which can be displayed in the table.
     * */
    setPlatforms(platforms) {
        let tmpPlatforms = [];

        for (let index in platforms) {
            let platform = {};
            platform.id = platforms[index].identifier;
            platform.platformName = platforms[index].name;
            platform.enabled = platforms[index].enabled.toString();
            platform.fileBased = platforms[index].fileBased.toString();
            tmpPlatforms.push(platform)
        }

        return tmpPlatforms;
    }

    /**
     * Handles the search action.
     * When typing in the search bar, this method will be invoked.
     * */
    searchApplications(word) {
        let searchedData = [];
    }

    /**
     * Handles sort data function and toggles the asc state.
     * asc: true : sort in ascending order.
     * */
    sortData() {
        let isAsc = this.state.asc;
        let datas = isAsc ? this.data.sort(this.compare) : this.data.reverse();
        this.setState({data: datas, asc: !isAsc});
    }

    compare(a, b) {
        if (a.applicationName < b.applicationName)
            return -1;
        if (a.applicationName > b.applicationName)
            return 1;
        return 0;
    }

    onRowClick(id) {
        //TODO: Remove this
        console.log(id)
    }

    render() {
        return (
            <div className='middle listingplatformmiddle'>
                <Card className='listingplatformcard'>
                    <TextField hintText="Search" onChange={this.searchApplications.bind(this)}
                               className='listingplatformsearch'/>
                    <CardTitle title="Platforms" className='listingplatformTitle'/>
                    <CardActions>

                    </CardActions>
                    <DataTable
                        headers={this.headers}
                        data={this.state.platforms}
                        handleRowClick={this.onRowClick.bind(this)}
                        noDataMessage={{type: 'button', text: 'Create Platform'}}/>
                </Card>
            </div>
        );
    }
}

PlatformListing.propTypes = {};

export default withRouter(PlatformListing);
