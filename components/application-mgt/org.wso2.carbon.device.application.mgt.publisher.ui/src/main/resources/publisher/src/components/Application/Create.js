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

class Create extends Component {
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

    handleNext = () => {
        const {stepIndex} = this.state;
        this.setState({
            stepIndex: stepIndex + 1,
            finished: stepIndex >= 2,
        });
    };

    handleSubmit = () => {
        console.log(this.state.stepData);
    };

    handleCancel = () => {
        this.setState({isDialogOpen: true});
    };

    handlePrev = () => {
        const {stepIndex} = this.state;
        if (stepIndex > 0) {
            this.removeStepData();
            this.setState({stepIndex: stepIndex - 1});
        }
    };

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

    handleYes = () => {
        this.setState({finished: false, stepIndex: 0, stepData: [], isDialogOpen: false});
    };

    handleNo = () => {
        this.setState({isDialogOpen: false});
    };

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
                                        <form onSubmit={this.handleSubmit}>
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

export default withRouter(Create);
