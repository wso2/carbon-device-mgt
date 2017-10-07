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
import './stepper.css';
import {FormattedMessage} from "react-intl";

class Step extends Component {

    render() {
        const {index, text, active, passed, finalStep, optional} = this.props;

        let activeStep = active ? "stepper-active-index" : "stepper-inactive-index";
        let activeStepText = active ? " stepper-active-step-text" : "";
        let stepPassed = index === passed || index < passed ? " stepper-passed-index" : "";
        let stepPassedText = index === passed || index < passed ? " stepper-passed-step-text" : "";

        let indexClassNames = "step-index " + activeStep + stepPassed;
        let indexTextClassNames = "step-text " + activeStepText + stepPassedText;

        let stepIndexContent = index === passed || index < passed ? <i className="fw fw-check"></i> : index;

        return (
            <div className="step">
                <div className="step-content">
                    <div className={indexClassNames}>
                            <span>
                                {stepIndexContent}
                             </span>
                    </div>
                    <div className={indexTextClassNames}>
                        <div>
                            {text} {!finalStep? <i className="stepper-next-arrow fw fw-right-arrow"></i> : <i/>}
                        </div>
                        {optional ?
                            <div className="stepper-optional-text">
                                (<FormattedMessage id="Optional" defaultMessage="Optional"/>)
                            </div>: <div/>}
                    </div>

                </div>
            </div>
        )
    }
}

Step.propTypes = {
    index: PropTypes.number,
    text: PropTypes.node,
    active: PropTypes.bool,
    passed: PropTypes.number,
    finalStep: PropTypes.bool,
    optional: PropTypes.bool
}

export default Step;