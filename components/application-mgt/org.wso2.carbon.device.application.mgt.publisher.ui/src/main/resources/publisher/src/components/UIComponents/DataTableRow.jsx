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
import {TableRow, TableRowColumn} from 'material-ui/Table';

/**
 * Data table row component.
 * This component created a row in the data table according to the props.
 * */
class DataTableRow extends Component {

    constructor() {
        super();
        this.state = {
            dataItem: {}
        };
        this.scriptId = "data-table";
    }

    componentWillMount() {
        this.setState({dataItem: this.props.dataItem});

        /**
         *Loading the theme files based on the the user-preference.
         */
        Theme.insertThemingScripts(this.scriptId);
    }

    componentWillUnmount() {
        Theme.removeThemingScripts(this.scriptId);
    }

    /**
     * Triggers the click event on the data table row.
     * */
    handleClick() {
        this.props.handleClick(this.state.dataItem.id);
    }

    render() {
        const {dataItem} = this.state;
        return (
            <TableRow
                key={this.props.key}
                onClick={this.handleClick.bind(this)}
            >
                {Object.keys(dataItem).map((key) => {
                    if (key !== 'id') {
                        return (
                            <TableRowColumn
                                className="datatableRowColumn"
                                key={key}
                            >
                                {dataItem[key]}
                            </TableRowColumn>)
                    } else {
                        return <TableRowColumn key={key}/>
                    }

                })}
            </TableRow>
        );
    }
}

DataTableRow.propTypes = {
    onClick: PropTypes.func,
    data: PropTypes.object
};

export default DataTableRow;
