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
import PropTypes from 'prop-types';
import React, {Component} from 'react';
import DataTableRow from './DataTableRow';
import DataTableHeader from './DataTableHeader';
import RaisedButton from 'material-ui/RaisedButton';
import {Table, TableBody, TableHeader, TableRow} from 'material-ui/Table';

/**
 * The Custom Table Component.
 * This component wraps the material-ui Table component and add some extra functionalities.
 * 1. Table header click. (For sorting)
 * 2. Table row click.
 *
 * The main sort function is defined in the component where the data table is created and passed to the
 * DataTable component via props.
 *
 * Following are the DataTable proptypes.
 *      1. Headers: Table headers. This is an array of Json Objects.
 *                  An Header Object contains the properties of each header. Currently following properties
 *                  are supported.
 *                  * sortable: boolean : whether the table column is sortable or not.
 *                  * sort: func : If sortable, the sort function.
 *                  * sort: func : If sortable, the sort function.
 *                  * sort: func : If sortable, the sort function.
 *                  * label: String: The Table header string.
 *                  * id: String: Unique id for header.
 *
 *      2. Data: The list of data that needs to be displayed in the table.
 *               This is also a json array of data objects.
 *               The Json object should contain key: value pair where the key is the header id.
 *
 * */
class DataTable extends Component {

    constructor() {
        super();
        this.handleRowClick = this.handleRowClick.bind(this);
        this.handleBtnClick = this.handleBtnClick.bind(this);
        this.state = {
            data: [],
            headers: [],
        };
        this.scriptId = "data-table"
    };

    componentWillMount() {
        console.log("Will mount", this.props.data); //TODO: Remove this
        this.setState({data: this.props.data, headers: this.props.headers}, Theme.insertThemingScripts(this.scriptId));

        /**
         *Loading the theme files based on the the user-preference.
         */
    }

    componentWillUnmount() {
        Theme.removeThemingScripts(this.scriptId);
    }

    shouldComponentUpdate(nextProps, nextState) {
        if (!nextProps.data) {
            this.setState({data: nextState.data});
            return true;
        }
        this.setState({data: nextProps.data});
        return true;
    }

    /**
     * Triggers when user click on table row.
     * This method invokes the parent method handleRowClick, which is passed via props.
     * */
    handleRowClick(id) {
        this.props.handleRowClick(id);
    }

    handleBtnClick(id) {
        this.props.handleButtonClick(id);
    }

    render() {
        const {data, headers} = this.state;

        //TODO: Remove this
        console.log(data);

        let noDataContent = null;

        if (this.props.noDataMessage.type === 'button') {
            noDataContent = <div><RaisedButton label={this.props.noDataMessage.text}/></div>
        }

        if (data) {
            return (<Table
                selectable={false}>
                <TableHeader displaySelectAll={false} adjustForCheckbox={false}>
                    <TableRow>
                        {headers.map((header) => {
                                return (
                                    <DataTableHeader
                                        key={header.data_id}
                                        className="datatableRowColumn"
                                        header={header}
                                    />
                                )}
                        )}
                    </TableRow>
                </TableHeader>
                <TableBody>
                    {data.map((dataItem) => {
                        return (
                            <DataTableRow
                                key={dataItem.id}
                                dataItem={dataItem}
                                handleButtonClick={this.handleBtnClick}
                                handleClick={this.handleRowClick}
                            />
                        )
                    })}
                </TableBody>
            </Table>)
        }
        return (<div>{noDataContent}</div>);
    }
}

DataTable.prototypes = {
    data: PropTypes.arrayOf(Object),
    headers: PropTypes.arrayOf(Object),
    sortData: PropTypes.func,
    handleRowClick: PropTypes.func,
    noDataMessage: PropTypes.object
};

export default DataTable;
