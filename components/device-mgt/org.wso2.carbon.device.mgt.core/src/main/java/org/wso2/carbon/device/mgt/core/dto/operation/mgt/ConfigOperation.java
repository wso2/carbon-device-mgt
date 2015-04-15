/*
 *
 *  *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  *  WSO2 Inc. licenses this file to you under the Apache License,
 *  *  Version 2.0 (the "License"); you may not use this file except
 *  *  in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *  http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing,
 *  *  software distributed under the License is distributed on an
 *  *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  *  KIND, either express or implied.  See the License for the
 *  *  specific language governing permissions and limitations
 *  *  under the License.
 *  *
 *
 */
package org.wso2.carbon.device.mgt.core.dto.operation.mgt;

import java.util.ArrayList;
import java.util.List;

public class ConfigOperation extends Operation {

    private List<Property> properties;

    public ConfigOperation() {
        properties = new ArrayList<Property>();
    }

    public List<Property> getConfigProperties() {
        return properties;
    }

    public void addConfigProperty(String name, Object value, Class<?> type) {
        properties.add(new Property(name, value, type));
    }

    public class Property {
        private String name;
        private Object value;
        private Class<?> type;

        public Property(String name, Object value, Class<?> type) {
            this.name = name;
            this.value = value;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public Class<?> getType() {
            return type;
        }

        public void setType(Class<?> type) {
            this.type = type;
        }
    }

    public Type getType() {
        return Type.CONFIG;
    }

}
