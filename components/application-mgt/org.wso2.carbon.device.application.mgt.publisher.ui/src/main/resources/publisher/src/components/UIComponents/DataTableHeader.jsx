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
import FlatButton from 'material-ui/FlatButton';
import {TableHeaderColumn} from 'material-ui/Table';
<<<<<<< HEAD
import Theme from '../../theme';
=======
import Theme from '../../themes/theme';
>>>>>>> parent of 8f3d11f... refactoring theming support

/**
 * Data Table header component.
 * This component creates the header elements of the table.
 * */
class DataTableHeader extends Component {

    constructor() {
        super();
    }

    componentWillMount() {
<<<<<<< HEAD
        /**
         *Loading the theme files based on the the user-preference.
         */
        const selected =
            (Theme.currentThemeType === Theme.defaultThemeType) ? Theme.defaultThemeType : Theme.currentTheme;
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
=======
        let selected = Theme.selectedTheme;
        if (Theme.currentTheme === "default") {
            require("../../themes/default/data-table.css");
        } else {
            try {
                require("../../themes/" + selected + "/data-table.css");
            } catch (ex) {
                // If the particular customized file does not exist, use the default one.
                require("../../themes/default/data-table.css");
            }
>>>>>>> parent of 8f3d11f... refactoring theming support
        }
    }

    /**
     * The onClick function of the table header.
     * Invokes the function passed in the header object.
     * */
    _tableHeaderClick() {
        this.props.header.sort();
    }

    render() {
        let headerCell = null;

        /**
         * If the header is sortable, create a button with onClick handler.
         * else create a span element with label as the table header.
         * */
        if (this.props.header.sortable) {
            headerCell = <FlatButton label={this.props.header.label}
                                    onClick={this._tableHeaderClick.bind(this)} className="sortableHeaderCell"/>;
        } else {
            headerCell = <span className="notsortableHeaderCell">{this.props.header.label}</span>;
        }

        return (
            <TableHeaderColumn key={this.props.header.id} className="datatableHeaderColumn" >
                {headerCell}
            </TableHeaderColumn>
        );
    }
}

DataTableHeader.prototypes = {
    header: PropTypes.object
};

export default DataTableHeader;
