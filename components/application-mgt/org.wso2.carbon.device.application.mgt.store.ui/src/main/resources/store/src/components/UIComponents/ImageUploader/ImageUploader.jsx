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
import Dropzone from "react-dropzone";
import {Row} from "reactstrap";
import Theme from '../../../theme';


class ImageUploader extends Component {
    constructor() {
        super();
        this.setImages = this.setImages.bind(this);
        this.state = {
            images: []
        };
        this.scriptId = "imageUploader";
    }

    componentWillMount() {
        /**
         *Loading the theme files based on the the user-preference.
         */
        Theme.insertThemingScripts(this.scriptId);
    }

    componentWillUnmount() {
        Theme.removeThemingScripts(this.scriptId);
    }

    setImages(images) {
        this.props.setImages(images);
    }

    render() {
        let {images, height, width, accepted, multiple, maxAmount} = this.props;
        return (
            <div id="screenshot-container">
                <Row>
                    {images.map((tile) => (
                            <input type="image" src={tile[0].preview} onClick=""/>
                        )
                    )}
                </Row>

                {this.state.screenshots.length < maxAmount ?
                    <Dropzone
                        className="add-image"
                        accept="image/jpeg, image/png"
                        onDrop={(accepted, rejected) => {
                            this.setImages(accepted);
                        }}
                    >
                        <p className="add-image-symbol">+</p>
                    </Dropzone> : <div/>}
            </div>
        );
    }

}

ImageUploader.prototypes = {
    height: PropTypes.string,
    width: PropTypes.string,
    accepted: PropTypes.array,
    multiple: PropTypes.bool,
    maxAmount: PropTypes.number,
    setImages: PropTypes.func
};


export default ImageUploader;
