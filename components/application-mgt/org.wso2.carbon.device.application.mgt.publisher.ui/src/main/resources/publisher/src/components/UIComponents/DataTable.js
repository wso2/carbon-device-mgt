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
import PropTypes from 'prop-types';
import {Table, TableBody} from 'material-ui/Table';
import DataTableHeader from './DataTableHeader';
import DataTableRow from './DataTableRow';
/**
 * Error page.
 * */
class DataTable extends Component {

    constructor() {
        super();
    }

    render() {
        return (
            <Table>
                <DataTableHeader>
                    {this.props.headers.map((header) => {

                    })}
                </DataTableHeader>
                <TableBody>

                </TableBody>
            </Table>
        );
    }
}

DataTable.prototypes = {
    data: PropTypes.arrayOf(Object),
    headers: PropTypes.arrayOf(String)
};

export default DataTable;
