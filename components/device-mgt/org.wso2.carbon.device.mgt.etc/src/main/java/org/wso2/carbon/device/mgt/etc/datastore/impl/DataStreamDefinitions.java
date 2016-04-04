/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.etc.datastore.impl;

public class DataStreamDefinitions {

	public final class StreamTypeLabel {

		public static final String TEMPERATURE = "TEMPERATURE";
		public static final String FAN = "FAN";
		public static final String BULB = "BULB";
		public static final String SONAR = "SONAR";
		public static final String LIGHT = "LIGHT";
		public static final String MOTION = "MOTION";

	}

	public final static String TEMPERATURE_STREAM_DEFINITION =
			"{" + "     'name':'org.wso2.iot.devices.temperature'," + "     'version':'1.0.0',"
					+ "     'nickName': 'Temperature Data',"
					+ "     'description': 'Temperature data received from the Device',"
					+ "     'tags': ['iot', 'temperature']," + "     'metaData': ["
					+ "                     {'name':'owner','type':'STRING'},"
					+ "                     {'name':'deviceType','type':'STRING'},"
					+ "                     {'name':'deviceId','type':'STRING'},"
					+ "                     {'name':'time','type':'LONG'}" + "                 " +
					"]," +
					"     "
					+ "     'payloadData':  [" +
					"                     {'name':'temperature','type':'STRING'}"
					+ "                     ]" + "}";

	public final static String BULB_STREAM_DEFINITION =
			"{" + "     'name':'org_wso2_iot_devices_bulb'," + "     'version':'1.0.0',"
					+ "     'nickName': 'Bulb Status',"
					+ "     'description': 'State of the bulb attached to a Device'," +
					"     'tags': ['iot', 'bulb'],"
					+ "     'metaData': [" +
					"                     {'name':'owner','type':'STRING'},"
					+ "                     {'name':'deviceType','type':'STRING'},"
					+ "                     {'name':'deviceId','type':'STRING'},"
					+ "                     {'name':'time','type':'LONG'}" + "                 " +
					"]," +
					"     "
					+ "     'payloadData':  [" +
					"                     {'name':'status','type':'STRING'}"
					+ "                     ]" + "}";

	public final static String FAN_STREAM_DEFINITION =
			"{" + "     'name':'org_wso2_iot_devices_fan'," + "     'version':'1.0.0',"
					+ "     'nickName': 'Fan Status'," +
					"     'description': 'State of the Fan attached to a Device',"
					+ "     'tags': ['iot', 'fan']," + "     'metaData': ["
					+ "                     {'name':'owner','type':'STRING'},"
					+ "                     {'name':'deviceType','type':'STRING'},"
					+ "                     {'name':'deviceId','type':'STRING'},"
					+ "                     {'name':'time','type':'LONG'}" + "                 " +
					"]," +
					"     "
					+ "     'payloadData':  [" +
					"                     {'name':'status','type':'STRING'}"
					+ "                     ]" + "}";

	public final static String MOTION_STREAM_DEFINITION =
			"{" + "     'name':'org_wso2_iot_devices_motion'," + "     'version':'1.0.0',"
					+ "     'nickName': 'Motion Data'," +
					"     'description': 'Motion data received from the Device',"
					+ "     'tags': ['iot', 'motion']," + "     'metaData': ["
					+ "                     {'name':'owner','type':'STRING'},"
					+ "                     {'name':'deviceType','type':'STRING'},"
					+ "                     {'name':'deviceId','type':'STRING'},"
					+ "                     {'name':'time','type':'LONG'}" + "                 " +
					"]," +
					"     "
					+ "     'payloadData':  [" +
					"                     {'name':'motion','type':'STRING'}"
					+ "                     ]" + "}";

	public final static String SONAR_STREAM_DEFINITION =
			"{" + "     'name':'org_wso2_iot_devices_sonar'," + "     'version':'1.0.0',"
					+ "     'nickName': 'Sonar Data'," +
					"     'description': 'Sonar data received from the Device',"
					+ "     'tags': ['iot', 'sonar']," + "     'metaData': ["
					+ "                     {'name':'owner','type':'STRING'},"
					+ "                     {'name':'deviceType','type':'STRING'},"
					+ "                     {'name':'deviceId','type':'STRING'},"
					+ "                     {'name':'time','type':'LONG'}" + "                 " +
					"]," +
					"     "
					+ "     'payloadData':  [" +
					"                     {'name':'sonar','type':'STRING'}"
					+ "                     ]" + "}";

	public final static String LIGHT_STREAM_DEFINITION =
			"{" + "     'name':'org_wso2_iot_devices_light'," + "     'version':'1.0.0',"
					+ "     'nickName': 'Light Data'," +
					"     'description': 'Light data received from the Device',"
					+ "     'tags': ['iot', 'light']," + "     'metaData': ["
					+ "                     {'name':'owner','type':'STRING'},"
					+ "                     {'name':'deviceType','type':'STRING'},"
					+ "                     {'name':'deviceId','type':'STRING'},"
					+ "                     {'name':'time','type':'LONG'}" + "                 " +
					"]," +
					"     "
					+ "     'payloadData':  [" +
					"                     {'name':'light','type':'STRING'}"
					+ "                     ]" + "}";

}
