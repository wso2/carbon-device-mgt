/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.mgt.analytics.common;

import java.util.HashMap;
import java.util.Map;

public class AnalyticsDataRecord {
	private Map<String, Object> values;
	private long timestamp;

	public AnalyticsDataRecord() {
		values = new HashMap<>();
	}

	public AnalyticsDataRecord(Map<String, Object> values) {
		this.values = values;
	}

	public Map<String, Object> getValues() {
		return this.values;
	}

	public void setValue(String name, Object value) {
		values.put(name, value);
	}

	public Object getValue(String name) {
		return this.values.get(name);
	}

	public void setValues(Map<String, Object> values) {
		this.values = values;
	}

	public long getTimestamp() {
		return this.timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
}
