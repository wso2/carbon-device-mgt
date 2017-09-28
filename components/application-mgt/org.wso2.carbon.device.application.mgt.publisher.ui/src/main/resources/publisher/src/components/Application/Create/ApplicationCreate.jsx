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
import {Button, Col, Modal, ModalBody, ModalFooter, ModalHeader, Row} from 'reactstrap';

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
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleCancel = this.handleCancel.bind(this);
        this.handleYes = this.handleYes.bind(this);
        this.handleNo = this.handleNo.bind(this);
        this.handlePrev = this.handlePrev.bind(this);
        this.handleNext = this.handleNext.bind(this);
        this.close = this.close.bind(this);
        this.state = {
            finished: false,
            stepIndex: 0,
            stepData: [],
            isDialogOpen: false
        };
    }

    componentWillReceiveProps(props, nextprops) {
        this.setState({open: props.open})
    }

    componentWillMount() {
        this.setState({open: this.props.open});
    }

    close() {
        this.props.close();
    }

    handleBack() {
        let currentStep = this.state.stepIndex;
        let nextStep = currentStep === 0 ? currentStep : currentStep - 1;
        this.setState({stepIndex: nextStep}, console.log(this.state.stepIndex));
    }

    /**
     * Handles next button click event.
     * */
    handleNext() {
        console.log("Handle Next"); //TODO: Remove this
        const {stepIndex} = this.state;
        this.setState({
            stepIndex: stepIndex + 1,
            finished: stepIndex >= 2,
        });
    };

    /**
     * Handles form submit.
     * */
    handleSubmit() {
        let stepData = this.state.stepData;
        let applicationCreationPromise = ApplicationMgtApi.createApplication(stepData);
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
    handlePrev() {
        const {stepIndex} = this.state;
        if (stepIndex > 0) {
            this.removeStepData();
            this.setState({stepIndex: stepIndex - 1});
        }
    };

    /**
     * Saves form data in each step in to the state.
     * @param step: The step number of the step data.
     * @param data: The form data of the step.
     * */
    setStepData(step, data) {
        console.log(step, data, this.state.stepData); //TODO: Remove this
        let tmpStepData = this.state.stepData;
        tmpStepData.push({step: step, data: data});

        this.setState({stepData: tmpStepData}, this.handleNext())
    };

    /**
     * Remove the last data point
     * */
    removeStepData() {
        let tempData = this.state.stepData;
        tempData.pop();
        this.setState({stepData: tempData});
    };

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
                        handleNext={this.handleNext}
                        setData={this.setStepData}
                        removeData={this.removeStepData}
                    />
                );
            case 1:
                return (
                    <Step2
                        handleNext={this.handleNext}
                        handlePrev={this.handlePrev}
                        setData={this.setStepData}
                        removeData={this.removeStepData}
                    />
                );
            case 2:
                return (
                    <Step3
                        handleFinish={this.handleNext}
                        handlePrev={this.handlePrev}
                        setData={this.setStepData}
                        removeData={this.removeStepData}
                    />
                );
            case 3: {
                return (
                    <Step4
                        handleNext={this.handleNext}
                        setData={this.setStepData}
                        removeData={this.removeStepData}
                    />
                )
            }
            default:
                return <div/>;
        }
    }

    render() {
        const {finished, stepIndex} = this.state;

        return (
            <div id="create-application-modal">
                <Modal isOpen={this.state.open} toggle={this.toggle} id="app-create-modal"
                       backdrop={'static'}>
                    <ModalHeader toggle={this.toggle}>Create Application</ModalHeader>
                    <ModalBody id="modal-body-content">
                        <Row>
                            <Col>
                                <div className="stepper-header">

                                </div>
                            </Col>
                        </Row>
                        <Row>
                            <Col>
                                {this.getStepContent(this.state.stepIndex)}
                            </Col>
                        </Row>
                    </ModalBody>
                    <ModalFooter>
                        {this.state.stepIndex === 0 ? <div/> :
                            <Button color="primary" onClick={this.handlePrev}>Back</Button>}
                        <Button color="secondary" onClick={this.close}>Cancel</Button>
                        {this.state.finished ?
                            <Button color="primary" onClick={this.handleSubmit}>Finish</Button> :
                            <Button color="primary" onClick={this.handleNext}>Continue</Button>}
                    </ModalFooter>
                </Modal>
            </div>);
    }
}

ApplicationCreate.propTypes = {};

export default withRouter(ApplicationCreate);
