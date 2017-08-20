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

import Chip from 'material-ui/Chip';
import React, {Component} from 'react';
import MenuItem from 'material-ui/MenuItem';
import TextField from 'material-ui/TextField';
import FlatButton from 'material-ui/FlatButton';
import SelectField from 'material-ui/SelectField';
import RaisedButton from 'material-ui/RaisedButton';

class Step2 extends Component {
    constructor() {
        super();
        this.state = {
            tags: [],
            defValue: "",
            category: 1
        };

        this.styles = {
            chip: {
                margin: 4,
            },
            wrapper: {
                display: 'flex',
                flexWrap: 'wrap',
            },
        };

    }

    addTags(event) {
        let tags = this.state.tags;
        if (event.charCode === 13) {
            event.preventDefault();
            tags.push({key: Math.floor(Math.random() * 1000), value: event.target.value});
            this.setState({tags, defValue: ""}, console.log(this.state.tags));
        }
    }

    handleTagChange(event) {
        let defaultValue = this.state.defValue;
        defaultValue = event.target.value;
        this.setState({defValue: defaultValue})
    }

    handleNext() {
        this.props.handleNext();
    }

    handlePrev() {
        this.props.handlePrev();
    }


    handleRequestDelete = (key) => {
        if (key === 3) {
            alert('Why would you want to delete React?! :)');
            return;
        }

        this.chipData = this.state.tags;
        const chipToDelete = this.chipData.map((chip) => chip.key).indexOf(key);
        this.chipData.splice(chipToDelete, 1);
        this.setState({tags: this.chipData});
    };

    renderChip(data) {
        console.log(data);
        return (
            <Chip
                key={data.key}
                onRequestDelete={() => this.handleRequestDelete(data.key)}
                style={this.styles.chip}
            >
                {data.value}
            </Chip>
        );
    }

    render() {
        const contentStyle = {margin: '0 16px'};
        return (
            <div style={contentStyle}>
                <div>
                    <div>
                        <TextField
                            hintText="Enter a title for your application."
                            floatingLabelText="Title*"
                            floatingLabelFixed={true}
                        /><br/>
                        <TextField
                            hintText="Enter a short description for your application."
                            floatingLabelText="Short Description*"
                            floatingLabelFixed={true}
                            multiLine={true}
                            rows={2}
                        /><br/>
                        <TextField
                            hintText="Enter the description."
                            floatingLabelText="Description*"
                            floatingLabelFixed={true}
                            multiLine={true}
                            rows={4}
                        /><br/>
                        <TextField
                            hintText="Select the application visibility"
                            floatingLabelText="Visibility*"
                            floatingLabelFixed={true}
                        /><br/>
                        <TextField
                            hintText="Enter application tags.."
                            floatingLabelText="Tags*"
                            floatingLabelFixed={true}
                            value={this.state.defValue}
                            onChange={this.handleTagChange.bind(this)}
                            onKeyPress={this.addTags.bind(this)}
                        /><br/>
                        <div style={this.styles.wrapper}>
                            {this.state.tags.map(this.renderChip, this)}
                        </div>
                        <br/>
                        <SelectField
                            floatingLabelText="Category*"
                            value={this.state.category}
                            floatingLabelFixed={true}
                        >
                            <MenuItem value={1} primaryText="Business"/>
                        </SelectField> <br/>
                    </div>

                    <div style={{marginTop: 12}}>
                        <FlatButton
                            label="< Back"
                            disabled={false}
                            onClick={this.handlePrev.bind(this)}
                            style={{marginRight: 12}}
                        />
                        <RaisedButton
                            label="Next >"
                            primary={true}
                            onClick={this.handleNext.bind(this)}
                        />
                    </div>
                </div>
            </div>
        );
    }
}

export default Step2;