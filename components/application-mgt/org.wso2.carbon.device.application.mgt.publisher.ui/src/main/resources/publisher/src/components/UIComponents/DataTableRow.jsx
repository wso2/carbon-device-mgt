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
import {TableRow, TableRowColumn} from 'material-ui/Table';
import Theme from '../../theme';

/**
 * Data table row component.
 * This component created a row in the data table according to the props.
 * */
class DataTableRow extends Component {

    constructor() {
        super();
        this.state = {
            dataItem: {}
        }
    }

    componentWillMount() {
        this.setState({dataItem: this.props.dataItem});

        /**
         *Loading the theme files based on the the user-preference.
         */
        const selected =
            (Theme.currentThemeType === Theme.defaultThemeType) ? Theme.defaultThemeType : Theme.currentThemeType;
        const dataTableCss = "data-table.css";
        const dataTableId = "data-table";
        let themePath  =  "/" + Theme.themeFolder + "/" + selected + "/" + dataTableCss;
        let promisedConfig = Theme.loadThemeFiles(themePath);
        let styleSheet = document.getElementById(dataTableId);
        let head = document.getElementsByTagName("head")[0];
        let link = document.createElement("link");
        link.type = Theme.styleSheetType;
        link.href = Theme.baseURL + "/" + Theme.appContext + themePath;
        link.id = dataTableId;
        link.rel = Theme.styleSheetRel;
        if (styleSheet !== null) {
            styleSheet.disabled = true;
            styleSheet.parentNode.removeChild(styleSheet);
        }

        promisedConfig.then(function() {
            head.appendChild(link);
        }).catch(function () {
            // If there is no customized css file, load the default one.
            themePath = "/" + Theme.themeFolder + "/" + Theme.defaultThemeType + "/" + dataTableCss;
            link.href = Theme.baseURL + "/" + Theme.appContext + themePath;
            head.appendChild(link);
        });
    }

    componentWillUnmount() {
        let styleSheet = document.getElementById("data-table");
        if (styleSheet !== null) {
            styleSheet.disabled = true;
            styleSheet.parentNode.removeChild(styleSheet);
        }
    }

    /**
     * Triggers the click event on the data table row.
     * */
    _handleClick() {
        this.props.handleClick(this.state.dataItem.id);
    }

    render() {
        const {dataItem} = this.state;
        return (
                <TableRow key={this.props.key} onClick={this._handleClick.bind(this)} >
                    {Object.keys(dataItem).map((key) => {
                        if (key !== 'id') {
                            return <TableRowColumn className = "datatableRowColumn"
                                                   key={key}>{dataItem[key]}</TableRowColumn>
                        } else {
                            return <TableRowColumn key={key}/>
                        }

                    } )}
                </TableRow>
        );
    }
}

DataTableRow.propTypes = {
    onClick: PropTypes.func,
    data: PropTypes.object
};

export default DataTableRow;
