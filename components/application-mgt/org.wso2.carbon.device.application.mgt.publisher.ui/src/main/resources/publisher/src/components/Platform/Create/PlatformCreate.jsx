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
import {Modal, ModalHeader} from "reactstrap";
import {FormattedMessage} from "react-intl";
import Stepper from "../../UIComponents/StepprHeader/Stepper";
import PlatformMgtApi from "../../../api/platformMgtApi";
import AuthHandler from "../../../api/authHandler";
import {General, Configure, Properties} from "./CreateSteps";

/**
 * Platform view component.
 * */
class PlatformCreate extends Component {

    constructor() {
        super();
        this.onClose = this.onClose.bind(this);
        this.setStepData = this.setStepData.bind(this);
        this.onSubmit = this.onSubmit.bind(this);
        this.onPrevClick = this.onPrevClick.bind(this);
        this.onNextClick = this.onNextClick.bind(this);
        this.state = {
            finished: false,
            stepIndex: 0,
            stepData: [],
            general: {},
            configure: {},
            platformProps: {},
            open: false
        }
    }

    componentWillReceiveProps(props, nextprops) {
        this.setState({open: props.open})
    }

    componentWillMount() {
        this.setState({open: this.props.open});
    }

    onClose() {
        this.setState({open: false, stepIndex: 0, general: {}, configure: {}, platformProps: {}})
    }

    /**
     * Handles next button click event.
     * */
    onNextClick() {
        // console.log(this.state); //TODO: Remove this
        const {stepIndex} = this.state;

        if (stepIndex + 1 > 2) {
            this.onSubmit();
        } else {
            this.setState({
                stepIndex: stepIndex + 1,
                finished: stepIndex + 1 > 1
            });
        }
    };

    /**
     * Handles form submit.
     *         platform.identifier = this.state.identifier;
     platform.name = this.state.name;
     platform.description = this.state.description;
     platform.tags = this.state.tags;
     platform.properties = this.state.platformProperties;
     platform.icon = this.state.icon;
     platform.enabled = this.state.enabled;
     platform.allTenants = this.state.allTenants;
     platform.defaultTenantMapping = true;

     *
     *
     * */
    onSubmit(platformProps) {
        let {general, configure} = this.state;
        let platformCreatePromise = PlatformMgtApi.createPlatform(general, configure, platformProps);
        platformCreatePromise.then(response => {
                console.log(response.data)
            }
        ).catch(
            function (err) {
                AuthHandler.unauthorizedErrorHandler(err);
            }
        );
    };

    /**
     * Saves form data in each step in to the state.
     * @param step: The step number of the step data.
     * @param data: The form data of the step.
     * */
    setStepData(step, data) {
        switch (step) {
            case "general": {
                this.setState({general: data}, this.onNextClick());
                break;
            }
            case "configure": {
                this.setState({configure: data}, this.onNextClick());
                break;
            }
            case "platformProps": {
                this.setState({platformProps: data}, this.onNextClick());
                break;
            }
        }
    };

    /**
     * Handled [ < Prev ] button click.
     * This clears the data in the current step and returns to the previous step.
     * */
    onPrevClick() {
        const {stepIndex} = this.state;
        if (stepIndex > 0) {
            this.setState({stepIndex: stepIndex - 1, finished: false});
        }
    };

    /**
     * Defines all the Steps in the stepper. (Wizard)
     *
     * Extension Point: If any extra steps needed, follow the instructions below.
     *                   1. Create the required form ./Forms directory.
     *                   2. Add defined case statements.
     *                   3. Define the Step in render function.
     *
     * */
    getStepContent(stepIndex) {
        switch (stepIndex) {
            case 0:
                return (
                    <General
                        defaultData={this.state.general}
                        setStepData={this.setStepData}
                        close={this.onClose}
                    />
                );
            case 1:
                return (
                    <Configure
                        defaultData={this.state.configure}
                        handlePrev={this.onPrevClick}
                        setStepData={this.setStepData}
                        close={this.onClose}
                    />
                );
            case 2:
                return (
                    <Properties
                        defaultData={this.state.properties}
                        handlePrev={this.onPrevClick}
                        onSubmit={this.onSubmit}
                        close={this.onClose}
                    />
                );
            default:
                return <div/>;
        }
    }

    getStepperHeaders() {
        return (
            [{index: 1, text: "General"},
                {index: 2, text: "Configure"},
                {index: 3, text: "Properties"},
            ]
        )
    }

    render() {
        const {stepIndex} = this.state;
        return (
            <div>
                <Modal isOpen={this.state.open} toggle={this.toggle} id="app-create-modal" backdrop={'static'}>
                    <ModalHeader className="app-create-modal-header">
                        <FormattedMessage id="Create.Platform" defaultMessage="Create.Platform"/>
                    </ModalHeader>
                    <Stepper
                        activeStep={stepIndex + 1}
                        previousStep={stepIndex}
                        stepContent={this.getStepperHeaders()}
                    />
                    {this.getStepContent(stepIndex)}
                </Modal>
            </div>
        );
    }
}

export default PlatformCreate;
