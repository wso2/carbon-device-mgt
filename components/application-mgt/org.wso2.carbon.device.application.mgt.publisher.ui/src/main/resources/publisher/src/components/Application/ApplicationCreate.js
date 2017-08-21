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
import Dialog from 'material-ui/Dialog';
import {withRouter} from 'react-router-dom';
import {Step1, Step2, Step3} from './Forms';
import FlatButton from 'material-ui/FlatButton';
import RaisedButton from 'material-ui/RaisedButton';
import {Card, CardActions, CardTitle} from 'material-ui/Card';
import {Step, StepLabel, Stepper,} from 'material-ui/Stepper';


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
        this.setStepData.bind(this);
        this.removeStepData.bind(this);
        this.handleSubmit.bind(this);
        this.handleCancel.bind(this);
        this.handleYes.bind(this);
        this.handleNo.bind(this);
        this.state = {
            finished: false,
            stepIndex: 0,
            stepData: [],
            isDialogOpen: false
        };
    }

    /**
     * Handles next button click event.
     * */
    handleNext = () => {
        const {stepIndex} = this.state;
        this.setState({
            stepIndex: stepIndex + 1,
            finished: stepIndex >= 2,
        });
    };

    /**
     * Handles form submit.
     * */
    handleSubmit = () => {
        console.log(this.state.stepData);
    };

    /**
     * Handles cancel button click event.
     * This will show a confirmation dialog to cancel the application creation process.
     * */
    handleCancel = () => {
        this.setState({isDialogOpen: true});
    };

    /**
     * Handled [ < Prev ] button click.
     * This clears the data in the current step and returns to the previous step.
     * */
    handlePrev = () => {
        const {stepIndex} = this.state;
        if (stepIndex > 0) {
            this.removeStepData();
            this.setState({stepIndex: stepIndex - 1});
        }
    };

    /**
     * Saves form data in each step in to the state.
     * */
    setStepData = (step, data) => {
        console.log(step, data, this.state.stepData);
        let tmpStepData = this.state.stepData;
        tmpStepData.push({step: step, data: data});

        this.setState({stepData: tmpStepData})
    };

    /**
     * Remove the last data point
     * */
    removeStepData = () => {
        let tempData = this.state.stepData;
        tempData.pop();
        this.setState({stepData: tempData});
    };

    /**
     * Handles the Yes button in app creation cancellation dialog.
     * Clears all the form data and reset the wizard.
     * */
    handleYes = () => {
        this.setState({finished: false, stepIndex: 0, stepData: [], isDialogOpen: false});
    };

    /**
     * Handles No button in app creation cancellation dialog.
     * Returns to the same step.
     * */
    handleNo = () => {
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
                return <Step1 handleNext={this.handleNext}
                              setData={this.setStepData}
                              removeData={this.removeStepData}/>;
            case 1:
                return <Step2 handleNext={this.handleNext}
                              handlePrev={this.handlePrev}
                              setData={this.setStepData}
                              removeData={this.removeStepData}/>;
            case 2:
                return <Step3 handleFinish={this.handleNext}
                              handlePrev={this.handlePrev}
                              setData={this.setStepData}
                              removeData={this.removeStepData}/>;
            default:
                return 'You\'re a long way from home sonny jim!';
        }
    }


    render() {
        const {finished, stepIndex} = this.state;
        const contentStyle = {margin: '0 16px'};

        /**
         * Defines the dialog box actions. [Yes][No]
         * */
        const actions = [
            <FlatButton
                label="Yes"
                primary={true}
                onClick={this.handleYes}
            />,
            <FlatButton
                label="No"
                secondary={true}
                onClick={this.handleNo}
            />,
        ];


        return (
            <div className="middle" style={{width: '95%', height: '100%', marginTop: '1%'}}>
                <Card>
                    <CardTitle title="Create Application"/>

                    {/**
                     * The stepper goes here.
                     */}
                    <CardActions>
                        <div style={{width: '100%', margin: 'auto'}}>
                            <Stepper activeStep={stepIndex}>
                                <Step>
                                    <StepLabel>Select Application Platform</StepLabel>
                                </Step>
                                <Step>
                                    <StepLabel>Enter Application Details</StepLabel>
                                </Step>
                                <Step>
                                    <StepLabel>Release</StepLabel>
                                </Step>
                            </Stepper>
                            <div style={contentStyle}>
                                {finished ? (
                                    <div>
                                        <p>Create App?</p>
                                        <form>
                                            <RaisedButton primary={true} label="Create" onClick={this.handleSubmit}/>
                                            <FlatButton label="Cancel" onClick={this.handleCancel}/>
                                        </form>
                                    </div>
                                ) : (
                                    <div>
                                        {this.getStepContent(stepIndex)}
                                    </div>
                                )}
                            </div>
                        </div>
                    </CardActions>
                </Card>
                <Dialog
                    actions={actions}
                    modal={false}
                    open={this.state.isDialogOpen}
                    onRequestClose={this.handleNo}
                >
                    Do you really want to cancel?
                </Dialog>
            </div>);
    }
}

export default withRouter(ApplicationCreate);
