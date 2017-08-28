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
import Chip from 'material-ui/Chip';
import Dropzone from 'react-dropzone';
import React, {Component} from 'react';
import MenuItem from 'material-ui/MenuItem';
import TextField from 'material-ui/TextField';
import FlatButton from 'material-ui/FlatButton';
import SelectField from 'material-ui/SelectField';
import RaisedButton from 'material-ui/RaisedButton';

/**
 * The Second step of application create wizard.
 * This contains following components.
 *      * App Title
 *      * Short Description
 *      * Application Description
 *      * Application Visibility
 *      * Application Tags : {Used Material UI Chip component}
 *      * Application Category.
 *      * Platform Specific properties.
 *      * Screenshots
 *      * Banner
 *      * Icon
 *
 * Parent Component: Create
 * Props:
 *      * handleNext : {type: function, Invokes handleNext function in Parent.}
 *      * handlePrev : {type: function, Invokes handlePrev function in Parent}
 *      * setData : {type: function, Invokes setStepData function in Parent}
 *      * removeData : {type: Invokes removeStepData function in Parent}
 * */
class Step2 extends Component {
    constructor() {
        super();
        this.state = {
            tags: [],
            defValue: "",
            category: 1,
            errors: {}
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

    /**
     * Create a tag on Enter key press and set it to the state.
     * Clears the tags text field.
     * Chip gets two parameters: Key and value.
     * */
    _addTags(event) {
        let tags = this.state.tags;
        if (event.charCode === 13) {
            event.preventDefault();
            tags.push({key: Math.floor(Math.random() * 1000), value: event.target.value});
            this.setState({tags, defValue: ""});
        }
    }

    /**
     * Set the value for tag.
     * */
    _handleTagChange(event) {
        let defaultValue = this.state.defValue;
        defaultValue = event.target.value;
        this.setState({defValue: defaultValue})
    }

    /**
     * Invokes the handleNext function in Create component.
     * */
    _handleNext() {
        this.props.handleNext();
    }

    /**
     * Invokes the handlePrev function in Create component.
     * */
    _handlePrev() {
        this.props.handlePrev();
    }

    /**
     * Handles Chip delete function.
     * Removes the tag from state.tags
     * */
    _handleRequestDelete = (key) => {
        this.chipData = this.state.tags;
        const chipToDelete = this.chipData.map((chip) => chip.key).indexOf(key);
        this.chipData.splice(chipToDelete, 1);
        this.setState({tags: this.chipData});
    };

    /**
     * Creates Chip array from state.tags.
     * */
    _renderChip(data) {
        return (
            <Chip
                key={data.key}
                onRequestDelete={() => this._handleRequestDelete(data.key)}
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
                            errorText={this.state.errors["title"]}
                            floatingLabelText="Title*"
                            floatingLabelFixed={true}
                        /><br/>
                        <TextField
                            hintText="Enter a short description for your application."
                            errorText={this.state.errors["shortDesc"]}
                            floatingLabelText="Short Description*"
                            floatingLabelFixed={true}
                            multiLine={true}
                            rows={2}

                        /><br/>
                        <TextField
                            errorText={this.state.errors["description"]}
                            hintText="Enter the description."
                            floatingLabelText="Description*"
                            floatingLabelFixed={true}
                            multiLine={true}
                            rows={4}
                        /><br/>
                        <TextField
                            hintText="Select the application visibility"
                            floatingLabelText="Visibility"
                            floatingLabelFixed={true}
                        /><br/>
                        <TextField
                            errorText={this.state.errors["tags"]}
                            hintText="Enter application tags.."
                            floatingLabelText="Tags*"
                            floatingLabelFixed={true}
                            value={this.state.defValue}
                            onChange={this._handleTagChange.bind(this)}
                            onKeyPress={this._addTags.bind(this)}
                        /><br/>
                        <div style={this.styles.wrapper}>
                            {this.state.tags.map(this._renderChip, this)}
                        </div>
                        <br/>
                        <SelectField
                            floatingLabelText="Category*"
                            value={this.state.category}
                            floatingLabelFixed={true}
                        >
                            <MenuItem value={1} primaryText="Business"/>
                        </SelectField> <br/>
                        {/*Platform Specific Properties.*/}
                        <div style={{border: 'solid #BDBDBD 1px'}}>
                            <p style={{color:'#BDBDBD'}}>Platform Specific Properties</p>
                        </div><br/>
                        <div>
                            <p style={{color:'#BDBDBD'}}>Screenshots*:</p>
                            <Dropzone style={{width:'100px', height:'100px', border: 'dashed #BDBDBD 1px'}}>
                                <p style={{margin: '40px 40px 40px 50px'}}>+</p>
                            </Dropzone>
                        </div><br/>
                        <div>
                            <p style={{color:'#BDBDBD'}}>Banner*:</p>
                            <Dropzone style={{width:'100px', height:'100px', border: 'dashed #BDBDBD 1px'}}>
                                <p style={{margin: '40px 40px 40px 50px'}}>+</p>
                            </Dropzone>
                        </div><br/>
                        <div>
                            <p style={{color:'#BDBDBD'}}>Icon*:</p>
                            <Dropzone style={{width:'100px', height:'100px', border: 'dashed #BDBDBD 1px'}}>
                                <p style={{margin: '40px 40px 40px 50px'}}>+</p>
                            </Dropzone>
                        </div><br/>
                    </div>

                    <br />
                    <br />
                    <div style={{marginTop: 12}}>
                        <FlatButton
                            label="< Back"
                            disabled={false}
                            onClick={this._handlePrev.bind(this)}
                            style={{marginRight: 12}}
                        />
                        <RaisedButton
                            label="Next >"
                            primary={true}
                            onClick={this._handleNext.bind(this)}
                        />
                    </div>
                </div>
            </div>
        );
    }
}

Step2.prototypes = {
    handleNext: PropTypes.func,
    handlePrev: PropTypes.func,
    setData: PropTypes.func,
    removeData: PropTypes.func
};

export default Step2;
