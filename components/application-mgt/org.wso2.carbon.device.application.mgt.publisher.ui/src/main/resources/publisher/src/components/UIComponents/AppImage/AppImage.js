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
import Theme from '../../../theme';

/**
 * Component for holding uploaded image.
 * This component has the feature to remove selected image from the array.
 * */
class AppImage extends Component {

    constructor() {
        super();
        this.removeImage = this.removeImage.bind(this);
        this.scriptId = "appImage";
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

    /**
     * Triggers the parent method to remove the selected image.
     * @param event: The click event of the component.
     * */
    removeImage(event) {
        event.preventDefault();
        this.props.onRemove(event.target.id);
    }

    render() {
        const {image, imageId} = this.props;
        return (
            <div className="image-container" style={this.props.imageStyles}>
                <img src={image} className="image" id={imageId}/>
                <div className="btn-content">
                    <i className="close-btn" id={imageId} onClick={this.removeImage}>X</i>
                </div>
            </div>
        )
    }
}

AppImage.propTypes = {
    image: PropTypes.string,
    imageId: PropTypes.string,
    onRemove: PropTypes.func,
    imageStyles: PropTypes.object
};

export default AppImage;
