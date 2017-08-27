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

import PropTypes from 'prop-types';
import React, {Component} from 'react';
import DataTableRow from './DataTableRow';
import DataTableHeader from './DataTableHeader';
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
        this.state = {
            data: [],
            headers: [],
        }

    };

    componentWillMount() {
        this.setState({data: this.props.data, headers: this.props.headers})
    }

    /**
     * Triggers when user click on table row.
     * This method invokes the parent method handleRowClick, which is passed via props.
     * */
    _handleRowClick(id) {
        this.props.handleRowClick(id);
    }


    render() {
        const {data, headers} = this.state;
        if (data.length > 0) {
            return (<Table
                selectable={ false }>
                <TableHeader displaySelectAll={ false }
                             adjustForCheckbox={ false }>
                    <TableRow>
                        {headers.map((header) => {
                                return (<DataTableHeader style={{display: 'flex'}} key={header.id} header={header}/>)
                            }
                        )}
                    </TableRow>
                </TableHeader>
                <TableBody>
                    {data.map((dataItem) =>{
                        return (<DataTableRow key={dataItem.id} dataItem={dataItem} handleClick={this._handleRowClick.bind(this)}/>)
                    })}
                </TableBody>
            </Table>)}

            return (<div>No apps</div>);

    }
}

DataTable.prototypes = {
    data: PropTypes.arrayOf(Object),
    headers: PropTypes.arrayOf(Object),
    sortData: PropTypes.func,
    handleRowClick: PropTypes.func
};

export default DataTable;
