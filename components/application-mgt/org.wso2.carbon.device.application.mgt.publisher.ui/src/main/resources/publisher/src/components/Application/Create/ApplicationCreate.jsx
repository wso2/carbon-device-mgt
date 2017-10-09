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
import {withRouter} from 'react-router-dom';
import AuthHandler from "../../../api/authHandler";
import {Step1, Step2, Step3, Step4} from './CreateSteps/index';
import ApplicationMgtApi from '../../../api/applicationMgtApi';
import {Modal, ModalHeader} from 'reactstrap';
import {FormattedMessage} from 'react-intl';
import Stepper from "../../UIComponents/StepprHeader/Stepper";


/**
 * The App Create Component.
 *
 * Application creation is handled through a Wizard. (We use Material UI Stepper.)
 *
 * In each step, data will be set to the state separately.
 * When the wizard is completed, data will be arranged and sent to the api.
 * */
class ApplicationCreate extends Component {
    constructor() {
        super();
        this.scriptId = "application-create";
        this.setStepData = this.setStepData.bind(this);
        this.removeStepData = this.removeStepData.bind(this);
        this.onSubmit = this.onSubmit.bind(this);
        this.handleCancel = this.handleCancel.bind(this);
        this.handleYes = this.handleYes.bind(this);
        this.handleNo = this.handleNo.bind(this);
        this.onPrevClick = this.onPrevClick.bind(this);
        this.onNextClick = this.onNextClick.bind(this);
        this.onClose = this.onClose.bind(this);
        this.state = {
            finished: false,
            stepIndex: 0,
            stepData: [],
            isDialogOpen: false,
            generalInfo: {},
            platform: {},
            screenshots: {},
            release: {}
        };
    }

    componentWillReceiveProps(props, nextprops) {
        this.setState({open: props.open})
    }

    componentWillMount() {
        this.setState({open: this.props.open});
    }

    /**
     * Resets the form and closes the modal.
     * */
    onClose() {
        this.setState({stepIndex: 0, generalInfo: {}, platform: {}, screenshots: {}, release: {}}, this.props.close());
    }

    /**
     * Handles next button click event.
     * */
    onNextClick() {
        console.log(this.state.stepIndex); //TODO: Remove this
        const {stepIndex} = this.state;
        this.setState({
            stepIndex: stepIndex + 1,
            finished: stepIndex >= 2,
        });
    };

    /**
     * Handles form submit.
     * */
    onSubmit() {
        let {generalInfo, platform, screenshots, release} = this.state;
        let applicationCreationPromise = ApplicationMgtApi.createApplication(generalInfo, platform, screenshots, release);
        applicationCreationPromise.then(response => {
                this.handleYes();
            }
        ).catch(
            function (err) {
                AuthHandler.unauthorizedErrorHandler(err);
            }
        );
    };

    /**
     * Handles cancel button click event.
     * This will show a confirmation dialog to cancel the application creation process.
     * */
    handleCancel() {
        this.setState({isDialogOpen: true});
    };

    /**
     * Handled [ < Prev ] button click.
     * This clears the data in the current step and returns to the previous step.
     * */
    onPrevClick() {
        console.log(this.state.stepIndex);
        const {stepIndex} = this.state;
        if (stepIndex > 0) {
            this.setState({stepIndex: stepIndex - 1, finished: false});
        }
    };

    /**
     * Saves form data in each step in to the state.
     * @param step: The step number of the step data.
     * @param data: The form data of the step.
     * */
    setStepData(step, data) {
        console.log(step, data, this.state); //TODO: Remove this
        switch (step) {
            case "generalInfo": {
                this.setState({generalInfo: data}, this.onNextClick());
                break;
            }
            case "platform": {
                this.setState({platform: data}, this.onNextClick());
                break;
            }
            case "screenshots": {
                this.setState({screenshots: data}, this.onNextClick());
                break;
            }
            case "release": {
                this.setState({release: data}, this.onNextClick());
                break;
            }
        }
    };

    /**
     * Defines the application creation stepper.
     * */
    getStepperHeaders() {
        return [
            {index: 1, text: <FormattedMessage id="General.Info" defaultMessage="General.Info"/>},
            {index: 2, text: <FormattedMessage id="Select.Platform" defaultMessage="Select.Platform"/>},
            {index: 3, text: <FormattedMessage id="Screenshots" defaultMessage="Screenshots"/>},
            {index: 4, text: <FormattedMessage id="Release" defaultMessage="Release"/>, optional: true}
        ];
    }

    /**
     * Remove the last data point
     * */
    removeStepData() {
        let tempData = this.state.stepData;
        tempData.pop();
        this.setState({stepData: tempData, stepIndex: 0});
    };

    /* ----------------- Deprecated ----------------- */
    /**
     * Handles the Yes button in app creation cancellation dialog.
     * Clears all the form data and reset the wizard.
     * */
    handleYes() {
        this.setState({finished: false, stepIndex: 0, stepData: [], isDialogOpen: false});
    };

    /**
     * Handles No button in app creation cancellation dialog.
     * Returns to the same step.
     * */
    handleNo() {
        this.setState({isDialogOpen: false});
    };

    /* ---------------------------------------------- */

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
                    <Step1
                        defaultData={this.state.generalInfo}
                        setStepData={this.setStepData}
                        close={this.onClose}
                    />
                );
            case 1:
                return (
                    <Step2
                        defaultData={this.state.platform}
                        handlePrev={this.onPrevClick}
                        setStepData={this.setStepData}
                        close={this.onClose}
                    />
                );
            case 2:
                return (
                    <Step3
                        defaultData={this.state.screenshots}
                        handlePrev={this.onPrevClick}
                        setStepData={this.setStepData}
                        close={this.onClose}
                    />
                );
            case 3: {
                return (
                    <Step4
                        defaultData={this.state.release}
                        handlePrev={this.onPrevClick}
                        onSubmit={this.onSubmit}
                        close={this.onClose}
                    />
                )
            }
            default:
                return <div/>;
        }
    }

    setStepHeader(stepIndex) {

    }

    render() {
        const {finished, stepIndex} = this.state;

        return (
            <div id="create-application-modal">
                <Modal isOpen={this.state.open} toggle={this.toggle} id="app-create-modal"
                       backdrop={'static'}>
                    <ModalHeader toggle={this.toggle} className="app-create-modal-header">
                        <FormattedMessage id="Create.Application" defaultMessage="Create Application"/>
                    </ModalHeader>
                    <div className="container app-create-modal-content">
                        <Stepper
                            activeStep={stepIndex + 1}
                            previousStep={stepIndex}
                            stepContent={this.getStepperHeaders()}
                        />
                        {this.getStepContent(stepIndex)}
                    </div>
                </Modal>
            </div>);
    }
}

ApplicationCreate.propTypes = {};

export default withRouter(ApplicationCreate);
