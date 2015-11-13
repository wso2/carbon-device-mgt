/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.device.mgt.common.api.controlqueue;

import org.wso2.carbon.device.mgt.common.api.exception.DeviceControllerException;

import java.util.HashMap;

// TODO: Auto-generated Javadoc

/**
 * The Interface ControlQueueConnector.
 *
 */
public interface ControlQueueConnector {

	/**
	 * Initializes the control queue.
	 * This method loads the initial configurations relevant to the
	 * Control-Queue implementation
	 *
	 * @return A status message according to the outcome of the
	 *         method execution.
	 */
	void initControlQueue() throws DeviceControllerException;

	/**
	 * Pushes the control messages received to the implemented queue
	 *
	 * @param deviceControls
	 *            A Hash Map which contains the parameters relevant to the
	 *            control message and the actual control message to be pushed to
	 *            the queue
	 * @return A status message according to the outcome of the
	 *         method execution.
	 */
	void enqueueControls(HashMap<String, String> deviceControls) throws
																		DeviceControllerException;
}
