/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.application.mgt.core.config.extensions;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ParameterAdapter extends XmlAdapter<Parameters, Map<String, String>> {

    @Override
    public Map<String, String> unmarshal(Parameters in) throws Exception {
        HashMap<String, String> hashMap = new HashMap<>();
        for (Parameter parameter : in.getParameters()) {
            hashMap.put(parameter.getName(), parameter.getValue());
        }
        return hashMap;
    }

    @Override
    public Parameters marshal(Map<String, String> map) throws Exception {
        Parameters parameters = new Parameters();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            parameters.addEntry(new Parameter(entry.getKey(), entry.getValue()));
        }
        return parameters;
    }

}